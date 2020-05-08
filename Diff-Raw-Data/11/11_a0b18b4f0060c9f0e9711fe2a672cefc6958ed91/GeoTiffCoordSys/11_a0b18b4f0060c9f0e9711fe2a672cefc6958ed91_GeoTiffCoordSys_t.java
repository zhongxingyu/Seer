 package ucar.nc2.iosp.geotiff;
 
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import javax.imageio.metadata.IIOMetadata;
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 import ucar.nc2.iosp.geotiff.cs.GeogCSGridMappingAdapter;
 import ucar.nc2.iosp.geotiff.cs.GridMappingAdapter;
 import ucar.nc2.iosp.geotiff.cs.ProjCSGridMappingAdapter;
 import ucar.nc2.iosp.geotiff.epsg.GTDatum;
 import ucar.nc2.iosp.geotiff.epsg.EPSGFactory;
 import ucar.nc2.iosp.geotiff.epsg.EPSGFactoryManager;
 import ucar.nc2.iosp.geotiff.epsg.GTGeogCS;
 import ucar.nc2.iosp.geotiff.epsg.GTProjCS;
 import static ucar.nc2.iosp.geotiff.GeoTiffIIOMetadataAdapter.*;
 
 public class GeoTiffCoordSys {
 
     // harvested from ProJ documentation and libgeotiff geo_normalize.c
     enum ProjCoordTrans {
         CT_TransverseMercator(1, 9807),
         CT_TransvMercator_Modified_Alaska(2),
         CT_ObliqueMercator(3, 9812, 9815),
         CT_ObliqueMercator_Laborde(4, 9813),
         CT_ObliqueMercator_Rosenmund(5, 9814),
         CT_ObliqueMercator_Spherical(6),
         CT_Mercator(7, 9804, 9805, 9841, 1024),
         CT_LambertConfConic_2SP(8, 9802, 9803),
         CT_LambertConfConic_Helmert(9, 9801),
         CT_LambertAzimEqualArea(10, 9820),
         CT_AlbersEqualArea(11, 9822),
         CT_AzimuthalEquidistant(12),
         CT_EquidistantConic(13, 9823, 9824),
         CT_Stereographic(14),
         CT_PolarStereographic(15, 9810),
         CT_ObliqueStereographic(16, 9809),
         CT_Equirectangular(17),
         CT_CassiniSoldner(18, 9806),
         CT_Gnomonic(19),
         CT_MillerCylindrical(20),
         CT_Orthographic(21),
         CT_Polyconic(22),
         CT_Robinson(23),
         CT_Sinusoidal(24),
         CT_VanDerGrinten(25),
         CT_NewZealandMapGrid(26, 9811),
         CT_TransvMercator_SouthOriented(27, 9808),
         CT_CylindricalEqualArea(28),
         Unknown(-1);
         private int tiffCode;
         private int[] epsgCodes;
         ProjCoordTrans(int tiffCode, int... epsgCodes) {
             this.tiffCode = tiffCode;
             this.epsgCodes = epsgCodes == null ? new int[0] : epsgCodes;
         }
 
         public int getTiffCode() {
             return tiffCode;
         }
         public int[] getEpsgCodes() {
             return epsgCodes;
         }
         public static ProjCoordTrans findByGeoTIFFCode(int tiffCode) {
             if (tiffCode > values().length || tiffCode < 1) {
                 return Unknown;
             } else {
                 return values()[tiffCode - 1];
             }
         }
         public static ProjCoordTrans findByEPSGCode(int epsgCode) {
             ProjCoordTrans pct = Unknown;
             for (int pctIndex = 0; pctIndex < values().length && pct == Unknown; ++pctIndex) {
                 int[] epsgCodes = values()[pctIndex].epsgCodes;
                 for (int epsgIndex = 0; epsgIndex < epsgCodes.length && pct == Unknown; ++epsgIndex) {
                     if (epsgCode == epsgCodes[epsgIndex]) {
                         pct = values()[pctIndex];
                     }
                 }
             }
             return pct;
         }
     }
 
     public final static int EPSGNatOriginLat = 8801;
     public final static int EPSGNatOriginLong = 8802;
     public final static int EPSGNatOriginScaleFactor = 8805;
     public final static int EPSGFalseEasting = 8806;
     public final static int EPSGFalseNorthing = 8807;
     public final static int EPSGProjCenterLat = 8811;
     public final static int EPSGProjCenterLong = 8812;
     public final static int EPSGAzimuth = 8813;
     public final static int EPSGAngleRectifiedToSkewedGrid = 8814;
     public final static int EPSGInitialLineScaleFactor = 8815;
     public final static int EPSGProjCenterEasting = 8816;
     public final static int EPSGProjCenterNorthing = 8817;
     public final static int EPSGPseudoStdParallelLat = 8818;
     public final static int EPSGPseudoStdParallelScaleFactor = 8819;
     public final static int EPSGFalseOriginLat = 8821;
     public final static int EPSGFalseOriginLong = 8822;
     public final static int EPSGStdParallel1Lat = 8823;
     public final static int EPSGStdParallel2Lat = 8824;
     public final static int EPSGFalseOriginEasting = 8826;
     public final static int EPSGFalseOriginNorthing = 8827;
     public final static int EPSGSphericalOriginLat = 8828;
     public final static int EPSGSphericalOriginLong = 8829;
     public final static int EPSGInitialLongitude = 8830;
     public final static int EPSGZoneWidth = 8831;
 
     private int width;
     private int height;
 
     private double[] pixelScales;
     private double[] tiePoints;
     private double[] transformation;
 
     private int modelType;
     private int geographicType;
     private int rasterType;
 
     private GridMappingAdapter gridMappingAdapter;
     
     final private EPSGFactory epsgFactory;
     
     public GeoTiffCoordSys(IIOMetadata metadata, int width, int height) {
 
         this.width = width;
         this.height = height;
 
         GeoTiffIIOMetadataAdapter metadataAdapter = new GeoTiffIIOMetadataAdapter(metadata);
 
         pixelScales = metadataAdapter.getModelPixelScales();
         tiePoints = metadataAdapter.getModelTiePoints();
         transformation = metadataAdapter.getModelTransformation();
 
         modelType = toInt(metadataAdapter.getGeoKey(GTModelTypeGeoKey));
         geographicType = toInt(metadataAdapter.getGeoKey(GeographicTypeGeoKey));
         rasterType = toInt(metadataAdapter.getGeoKey(GTRasterTypeGeoKey));
         
         epsgFactory = EPSGFactoryManager.getInstance().getEPSGFactory();
 
         if (modelType != ModelTypeGeocentric) {
             GeogCSGridMappingAdapter geogCSGridMappingAdapter = generateGeogCSHandler(metadataAdapter);
             if (modelType == ModelTypeProjected) {
                 gridMappingAdapter = generateProjCSHandler(metadataAdapter, geogCSGridMappingAdapter);
             } else {
                 gridMappingAdapter = geogCSGridMappingAdapter;
             }
         } // else { /* don't support geocentric yet. */ }
     }
 
     private GeogCSGridMappingAdapter generateGeogCSHandler(GeoTiffIIOMetadataAdapter metadata) {
         GTGeogCS geogCS = null;
         geogCS = epsgFactory.findGeogCSByCode(geographicType);
         if (geogCS == null) {
             int datumCode = toInt(metadata.getGeoKey(GeogGeodeticDatumGeoKey));
             if (datumCode != Integer.MIN_VALUE) { // no data code
                 GTDatum datum = epsgFactory.findDatumByCode(datumCode);
                 if (datum != null) {
                     geogCS = epsgFactory.findGeogCSByDatum(datum);
                 }
             }
         }
         return (geogCS == null) ?
             null :
             new GeogCSGridMappingAdapter(geogCS);
     }
 
     private ProjCSGridMappingAdapter generateProjCSHandler(
             GeoTiffIIOMetadataAdapter metadata,
             GeogCSGridMappingAdapter geogCSGridMappingAdapter) {
 
         ProjCSGridMappingAdapter pcsh = generateProjCSHandlerFromProjectedCSType(
                 metadata, geogCSGridMappingAdapter);
         // TCK - 2012.01.02 - don't remember why this wasn't pursued, maybe required data wasn't in our CSV EPSG dumps?
 //        if (pcsh == null) {
 //            pcsh = generateProjCSHandlerFromProjection(
 //                  metadata, geogCSGridMappingAdapter);
 //        }
         if (pcsh == null) {
             pcsh = generateProjCSHandlerFromProjCoordTrans(
                     metadata, geogCSGridMappingAdapter);
         }
         return pcsh;
     }
 
     private ProjCSGridMappingAdapter generateProjCSHandlerFromProjectedCSType(
             GeoTiffIIOMetadataAdapter metadata,
             GeogCSGridMappingAdapter geogCSHandler) {
 
         int projectedCSType = toInt(metadata.getGeoKey(ProjectedCSTypeGeoKey));
 
         String gridMappingName = null;
         Map<String, Double> pMap = new LinkedHashMap<String, Double>();
         GTProjCS projCS = epsgFactory.findProjCSByCode(projectedCSType);
         if (projCS == null) {
             return null;
         }
        // START - QUICK FIX  Maybe refactor this method/class?  It appears it's
        // premature to expect a GeogCS to be known as it may be a property of 
        // the Projection and not explicitly defined in the GeoTIFF metadata
        if (geogCSHandler == null) {
            GTGeogCS geogCS = projCS.getSourceGeogCS();
            if (geogCS == null) {
                return null;
            }
            geogCSHandler = new GeogCSGridMappingAdapter(geogCS);
        }
        // END - QUICK FIX
         ProjCoordTrans pct = ProjCoordTrans.findByEPSGCode(projCS.getCoordOpMethodCode());
         switch(pct) {
             case CT_AlbersEqualArea:
                 gridMappingName = "albers_conical_equal_area";
                 pMap.put("standard_parallel1",
                         projCS.getParameterValueByCode(EPSGStdParallel1Lat));
                 pMap.put("standard_parallel2",
                         projCS.getParameterValueByCode(EPSGStdParallel2Lat));
                 pMap.put("latitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGFalseOriginLat));
                 pMap.put("longitude_of_central_meridian",
                         projCS.getParameterValueByCode(EPSGFalseOriginLong));
                 pMap.put("false_easting",
                         projCS.getParameterValueByCode(EPSGFalseOriginEasting));
                 pMap.put("false_northing",
                         projCS.getParameterValueByCode(EPSGFalseOriginNorthing));
                 break;
             case CT_LambertAzimEqualArea:
                 gridMappingName = "lambert_azimuthal_equal_area";
                 pMap.put("latitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginLat));
                 pMap.put("longitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginLong));
                 pMap.put("false_easting",
                         projCS.getParameterValueByCode(EPSGFalseEasting));
                 pMap.put("false_northing",
                         projCS.getParameterValueByCode(EPSGFalseNorthing));
                 break;
             case CT_LambertConfConic_2SP:
                 gridMappingName = "lambert_conformal_conic";
                 pMap.put("latitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGFalseOriginLat));
                 pMap.put("longitude_of_central_meridian",
                         projCS.getParameterValueByCode(EPSGFalseOriginLong));
                 pMap.put("standard_parallel1",
                         projCS.getParameterValueByCode(EPSGStdParallel1Lat));
                 pMap.put("standard_parallel2",
                         projCS.getParameterValueByCode(EPSGStdParallel2Lat));
                 pMap.put("false_easting",
                         projCS.getParameterValueByCode(EPSGFalseOriginEasting));
                 pMap.put("false_northing",
                         projCS.getParameterValueByCode(EPSGFalseOriginNorthing));
                 break;
             case CT_Mercator:
                 gridMappingName = "mercator";
                 pMap.put("latitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginLat));
                 pMap.put("longitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginLong));
                 pMap.put("scale_factor_at_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginScaleFactor));
                 pMap.put("standard_parallel",
                         projCS.getParameterValueByCode(EPSGStdParallel1Lat));
                 pMap.put("false_easting",
                         projCS.getParameterValueByCode(EPSGFalseEasting));
                 pMap.put("false_northing",
                         projCS.getParameterValueByCode(EPSGFalseNorthing));
                 break;
             case CT_PolarStereographic:
                 gridMappingName = "polar_stereographic";
                 pMap.put("latitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginLat));
                 pMap.put("straight_vertical_longitude_from_pole",
                         projCS.getParameterValueByCode(EPSGNatOriginLong));
                 pMap.put("scale_factor_at_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginScaleFactor));
                 pMap.put("false_easting",
                         projCS.getParameterValueByCode(EPSGFalseEasting));
                 pMap.put("false_northing",
                         projCS.getParameterValueByCode(EPSGFalseNorthing));
                 break;
             case CT_TransverseMercator:
                 gridMappingName = "transverse_mercator";
                 pMap.put("latitude_of_projection_origin",
                         projCS.getParameterValueByCode(EPSGNatOriginLat));
                 pMap.put("longitude_of_central_meridian",
                         projCS.getParameterValueByCode(EPSGNatOriginLong));
                 pMap.put("scale_factor_at_central_meridian",
                         projCS.getParameterValueByCode(EPSGNatOriginScaleFactor));
                 pMap.put("false_easting",
                         projCS.getParameterValueByCode(EPSGFalseEasting));
                 pMap.put("false_northing",
                         projCS.getParameterValueByCode(EPSGFalseNorthing));
                 break;
             default:
         }
         return (gridMappingName == null) ?
             null :
             new ProjCSGridMappingAdapter(geogCSHandler, gridMappingName, pMap);
     }
 
     private ProjCSGridMappingAdapter generateProjCSHandlerFromProjCoordTrans(
             GeoTiffIIOMetadataAdapter metadata,
             GeogCSGridMappingAdapter geogCSHandler) {
 
         int projCoordTrans = toInt(metadata.getGeoKey(ProjCoordTransGeoKey));
 
         String gridMappingName = null;
         Map<String, Double> pMap = new LinkedHashMap<String, Double>();
         ProjCoordTrans pct = ProjCoordTrans.findByGeoTIFFCode(projCoordTrans);
         double lat;
         double lon;
         switch(pct) {
             case CT_AlbersEqualArea:
                 gridMappingName = "albers_conical_equal_area";
                 pMap.put("standard_parallel1",
                         toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                 pMap.put("standard_parallel2",
                         toDouble(metadata.getGeoKey(ProjStdParallel2GeoKey)));
                 pMap.put("latitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey)));
                 pMap.put("longitude_of_central_meridian",
                         toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_AzimuthalEquidistant:
                 gridMappingName = "azimuthal_equidistant";
                 pMap.put("latitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjCenterLatGeoKey)));
                 pMap.put("longitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjCenterLongGeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_LambertAzimEqualArea:
                 gridMappingName = "lambert_azimuthal_equal_area";
                 // libgeotiff source and docs say to use ProjCenter but Intergraph
                 // samples use ProjNat, eh...
                 lat = toDouble(metadata.getGeoKey(ProjCenterLatGeoKey));
                 if (Double.isNaN(lat)) {
                     lat = toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey));
                 }
                 lon = toDouble(metadata.getGeoKey(ProjCenterLongGeoKey));
                 if (Double.isNaN(lon)) {
                     lon = toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey));
                 }
                 pMap.put("latitude_of_projection_origin", lat);
                 pMap.put("longitude_of_projection_origin", lon);
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_LambertConfConic_2SP:
                 gridMappingName = "lambert_conformal_conic";
                 // libgeotiff source and docs say to use ProjFalseOrigin but
                 // Intergraph samples use ProjNat, eh...
                 lat = toDouble(metadata.getGeoKey(ProjFalseOriginLatGeoKey));
                 if (Double.isNaN(lat)) {
                     lat = toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey));
                 }
                 lon = toDouble(metadata.getGeoKey(ProjFalseOriginLongGeoKey));
                 if (Double.isNaN(lon)) {
                     lon = toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey));
                 }
                 pMap.put("latitude_of_projection_origin", lat);
                 pMap.put("longitude_of_central_meridian", lon);
                 pMap.put("standard_parallel1",
                         toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                 pMap.put("standard_parallel2",
                         toDouble(metadata.getGeoKey(ProjStdParallel2GeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseOriginEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseOriginNorthingGeoKey)));
                 break;
             case CT_CylindricalEqualArea:
                 gridMappingName = "lambert_cylindrical_equal_area";
                 pMap.put("longitude_of_central_meridian",
                         toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                 pMap.put("standard_parallel",
                         toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_Mercator:
                 gridMappingName = "mercator";
                 pMap.put("longitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                 pMap.put("standard_parallel",
                         toDouble(metadata.getGeoKey(ProjStdParallel1GeoKey)));
                 pMap.put("scale_factor_at_projection_origin",
                         toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_Orthographic:
                 gridMappingName = "orthographic";
                 // libgeotiff source and docs say to use ProjCenter but
                 // Intergraph samples use ProjNat, eh...
                 lat = toDouble(metadata.getGeoKey(ProjCenterLatGeoKey));
                 if (Double.isNaN(lat)) {
                     lat = toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey));
                 }
                 lon = toDouble(metadata.getGeoKey(ProjCenterLongGeoKey));
                 if (Double.isNaN(lon)) {
                     lon = toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey));
                 }
                 pMap.put("latitude_of_projection_origin", lat);
                 pMap.put("longitude_of_projection_origin", lon);
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_PolarStereographic:
                 gridMappingName = "polar_stereographic";
                 pMap.put("latitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey)));
                 pMap.put("straight_vertical_longitude_from_pole",
                         toDouble(metadata.getGeoKey(ProjStraightVertPoleLongGeoKey)));
                 pMap.put("scale_factor_at_projection_origin",
                         toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_Stereographic:
                 gridMappingName = "stereographic";
                 pMap.put("latitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjCenterLatGeoKey)));
                 pMap.put("longitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjCenterLongGeoKey)));
                 pMap.put("scale_factor_at_projection_origin",
                         toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             case CT_TransverseMercator:
                 gridMappingName = "transverse_mercator";
                 pMap.put("latitude_of_projection_origin",
                         toDouble(metadata.getGeoKey(ProjNatOriginLatGeoKey)));
                 pMap.put("longitude_of_central_meridian",
                         toDouble(metadata.getGeoKey(ProjNatOriginLongGeoKey)));
                 pMap.put("scale_factor_at_central_meridian",
                         toDouble(metadata.getGeoKey(ProjScaleAtNatOriginGeoKey)));
                 pMap.put("false_easting",
                         toDouble(metadata.getGeoKey(ProjFalseEastingGeoKey)));
                 pMap.put("false_northing",
                         toDouble(metadata.getGeoKey(ProjFalseNorthingGeoKey)));
                 break;
             default:
         }
         return (gridMappingName == null) ?
             null :
             new ProjCSGridMappingAdapter(geogCSHandler, gridMappingName, pMap);
     }
 
     boolean isSupported() {
         return width > -1 && height > -1 &&
                 pixelScales != null &&
                 tiePoints != null &&
                 gridMappingAdapter != null;
     }
 
     public GridMappingAdapter getGridMappingAdapter() {
         return gridMappingAdapter;
     }
     
     public CoordSysAdapter getCoordSysAdapter() {
         return new CoordSysAdapter();
     }
 
     private static int toInt(String string) {
         if (string != null && string.length() > 0) {
             try {
                 return Integer.parseInt(string);
             } catch (NumberFormatException e) {
                 // error handler on fall through
             }
         }
         return Integer.MIN_VALUE;
     }
 
     private static double toDouble(String string) {
         if (string != null && string.length() > 0) {
             try {
                 return Double.parseDouble(string);
             } catch (NumberFormatException e) {
                 // error handler on fall through
             }
         }
         return Double.NaN;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final GeoTiffCoordSys other = (GeoTiffCoordSys) obj;
         if (this.width != other.width) {
             return false;
         }
         if (this.height != other.height) {
             return false;
         }
         if (!Arrays.equals(this.pixelScales, other.pixelScales)) {
             return false;
         }
         if (!Arrays.equals(this.tiePoints, other.tiePoints)) {
             return false;
         }
         if (!Arrays.equals(this.transformation, other.transformation)) {
             return false;
         }
         if (this.gridMappingAdapter != other.gridMappingAdapter && (this.gridMappingAdapter == null || !this.gridMappingAdapter.equals(other.gridMappingAdapter))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 17 * hash + this.width;
         hash = 17 * hash + this.height;
         hash = 17 * hash + Arrays.hashCode(this.pixelScales);
         hash = 17 * hash + Arrays.hashCode(this.tiePoints);
         hash = 17 * hash + Arrays.hashCode(this.transformation);
         hash = 17 * hash + (this.gridMappingAdapter != null ? this.gridMappingAdapter.hashCode() : 0);
         return hash;
     }
 
     public class CoordSysAdapter {
 
         private String dimensionsAsString;
         private String coordinatesAsString;
 
         private CoordSysAdapter() {
         }
 
         public void generate(NetcdfFile ncFile, int index) {
 
             Dimension xDim = ncFile.addDimension(
                     null, new Dimension("x" + index, width));
             Dimension yDim = ncFile.addDimension(
                     null, new Dimension("y" + index, height));
 
             double tiePointXCenter = rasterType == RasterPixelIsPoint ?
                     tiePoints[3] :
                     tiePoints[3] + pixelScales[0] / 2d;
             double tiePointYCenter = rasterType == RasterPixelIsPoint ?
                     tiePoints[4] :
                     tiePoints[4] - pixelScales[1] / 2d;
 
             String xName = null;
             String yName = null;
             String xUnits = null;
             String yUnits = null;
             String xStandardName = null;
             String yStandardName = null;
             if (modelType == ModelTypeProjected) {
                 xName = "geoX" + index;
                 yName = "geoY" + index;
                 xUnits = "m";
                 yUnits = "m";
                 xStandardName = "projection_x_coordinate";
                 yStandardName = "projection_y_coordinate";
             } else {
                 xName = "lon" + index;
                 yName = "lat" + index;
                 xUnits = "degrees_east";
                 yUnits = "degrees_north";
                 xStandardName = "longitude";
                 yStandardName = "latitude";
             }
 
             Variable xVar = new Variable(ncFile, null, null, xName);
             xVar.setDataType(DataType.DOUBLE);
             xVar.setDimensions(xDim.getName());
             xVar.addAttribute(new Attribute("units", xUnits));
             xVar.addAttribute(new Attribute("standard_name", xStandardName));
             xVar.setCachedData(
                     Array.makeArray(
                         DataType.DOUBLE,
                         width,
                         tiePointXCenter,
                         pixelScales[0]),
                     false);
             ncFile.addVariable(null, xVar);
 
             Variable yVar = new Variable(ncFile, null, null, yName);
             yVar.setDataType(DataType.DOUBLE);
             yVar.setDimensions(yDim.getName());
             yVar.addAttribute(new Attribute("units", yUnits));
             yVar.addAttribute(new Attribute("standard_name", yStandardName));
             yVar.setCachedData(
                     Array.makeArray(
                         DataType.DOUBLE,
                         height,
                         tiePointYCenter,
                         -pixelScales[1]),
                     false);
             ncFile.addVariable(null, yVar);
 
             dimensionsAsString = yDim.getName() + " " + xDim.getName();
             coordinatesAsString = yVar.getName() + " " + xVar.getName();
         }
 
         public String getDimensionsAsString() {
             if (dimensionsAsString == null) {
                 throw new IllegalStateException("Coordinate system not generated for this instance");
             }
             return dimensionsAsString;
         }
 
         public String getCoordinatesAsString() {
             if (coordinatesAsString == null) {
                 throw new IllegalStateException("Coordinate system not generated for this instance");
             }
             return coordinatesAsString;
         }
 
     }
 
 }
