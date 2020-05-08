 package gov.usgs.cida.gdp.coreprocessing.analysis.grid;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.measure.converter.UnitConverter;
 import javax.measure.quantity.Angle;
 import javax.measure.quantity.Length;
 import javax.measure.unit.SI;
 import javax.measure.unit.Unit;
 import org.geotools.metadata.iso.extent.ExtentImpl;
 import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
 import org.geotools.parameter.DefaultParameterDescriptorGroup;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.referencing.crs.DefaultProjectedCRS;
 import org.geotools.referencing.cs.DefaultCartesianCS;
 import org.geotools.referencing.cs.DefaultEllipsoidalCS;
 import org.geotools.referencing.datum.DefaultEllipsoid;
 import org.geotools.referencing.datum.DefaultGeodeticDatum;
 import org.geotools.referencing.datum.DefaultPrimeMeridian;
 import org.geotools.referencing.operation.DefaultMathTransformFactory;
 import org.geotools.referencing.operation.transform.AbstractMathTransform;
 import org.geotools.referencing.operation.transform.ConcatenatedTransform;
 import org.geotools.referencing.crs.DefaultDerivedCRS;
 import org.opengis.metadata.extent.GeographicExtent;
 import org.opengis.parameter.GeneralParameterDescriptor;
 import org.opengis.parameter.ParameterDescriptorGroup;
 import org.opengis.parameter.ParameterValueGroup;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.IdentifiedObject;
 import org.opengis.referencing.NoSuchIdentifierException;
 import org.opengis.referencing.ReferenceSystem;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.crs.GeographicCRS;
 import org.opengis.referencing.crs.ProjectedCRS;
 import org.opengis.referencing.cs.CartesianCS;
 import org.opengis.referencing.datum.Ellipsoid;
 import org.opengis.referencing.datum.GeodeticDatum;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.NoninvertibleTransformException;
 import org.opengis.referencing.operation.TransformException;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDatatype;
 import ucar.unidata.geoloc.ProjectionImpl;
 import ucar.unidata.geoloc.ProjectionRect;
 import ucar.unidata.util.Parameter;
 
 /**
  *
  * @author tkunicki
  */
 public class CRSUtility {
 
 	public static CoordinateReferenceSystem getCRSFromGridDatatype(GridDatatype gd) {
 		return getCRSFromGridCoordSystem(gd.getCoordinateSystem());
 	}
 
 	public static CoordinateReferenceSystem getCRSFromGridCoordSystem(GridCoordSystem gcs) {
 
 		ProjectionImpl projection = gcs.getProjection();
 		if (projection == null) {
 			return DefaultGeographicCRS.WGS84;
 		}
 	
 		Map<String, Parameter> parameterMap = new HashMap<String, Parameter>();
 		for (Parameter p : projection.getProjectionParameters()) {
 			parameterMap.put(p.getName(), p);
 		}
 
 		GeodeticDatum datum = generateGeodeticDatum(parameterMap);
 
 		Parameter gridMappingNameParameter = parameterMap.get("grid_mapping_name");
 
 		String gridMappingName = gridMappingNameParameter == null ?
 				"LatLon" :
 				gridMappingNameParameter.getStringValue();
 
 
 		GeographicCRS geographicCRS = (datum == DefaultGeodeticDatum.WGS84) ?
 				DefaultGeographicCRS.WGS84 :
 				new DefaultGeographicCRS(
 					"CF-Derived Geographic CRS",
 					datum,
 					DefaultEllipsoidalCS.GEODETIC_2D);
 
 		if ("LatLon".equals(gridMappingName) || "latitude_longitude".equals(gridMappingName)) {
 
             ProjectionRect projectionRect = gcs.getBoundingBox();
             boolean longitude360 = projectionRect.getMaxX() > 180;
             if (longitude360) {
                 
                 Map<String, Object> crsPoperties = new HashMap<String, Object>();
                 
                 crsPoperties.put(IdentifiedObject.NAME_KEY, "CRS LON [0,360]");
 
                 ExtentImpl extent = new ExtentImpl();
                 List<GeographicExtent> extentList = new ArrayList<GeographicExtent>();
                 extentList.add(new GeographicBoundingBoxImpl(0, 360, -90, 90));
                 extent.setGeographicElements(extentList);
                 crsPoperties.put(ReferenceSystem.DOMAIN_OF_VALIDITY_KEY, extent);
 
                 return new DefaultDerivedCRS(
                     "CF-Derived CRS LON [0,360]",
                     geographicCRS,
                     longitudeCenter180Modulo360Transform,
                     DefaultEllipsoidalCS.GEODETIC_2D);
             } else {
                 return geographicCRS;
             }
 		}
 
 		try {
 
             Unit<Length> axisLengthUnit = generateAxisLengthUnit(gcs);
 
 			DefaultMathTransformFactory transformFactory =
 						new DefaultMathTransformFactory();
 
 			ParameterValueGroup parameterGroup = null;
 		
 			if ("albers_conical_equal_area".equals(gridMappingName)) {
 
 				parameterGroup =
 						transformFactory.getDefaultParameters("Albers_Conic_Equal_Area");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("longitude_of_central_meridian", "longitude_of_center");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_center");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				adapter.transferStandardParallels();
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("azimuthal_equidistant".equals(gridMappingName)) {
 
 				throw new RuntimeException("azimuthal_equidistant projection is not supported");
 
 			} else if ("lambert_azimuthal_equal_area".equals(gridMappingName)) {
 
 				parameterGroup =
 						transformFactory.getDefaultParameters("Lambert_Azimuthal_Equal_Area");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("longitude_of_projection_origin", "longitude_of_center");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_center");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("lambert_conformal_conic".equals(gridMappingName)) {
 
                 boolean is2SP = ProjectionParameterAdapter.getStandardParallelCount(parameterMap) == 2;
 				parameterGroup = is2SP ?
                         transformFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP") :
 						transformFactory.getDefaultParameters("Lambert_Conformal_Conic_1SP");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("longitude_of_central_meridian", "central_meridian");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_origin");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
                 if (is2SP) { 
                     adapter.transferStandardParallels();
                 } // else { /* scale_factor parameter is default of 1.0. */ }
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("lambert_cylindrical_equal_area".equals(gridMappingName)) {
 
 				throw new RuntimeException("lambert_cylindrical_equal_area projection is not supported");
 
 			} else if ("mercator".equals(gridMappingName)) {
 
 				// presence of "standard_parallel" indicates Mercator_2SP while
 				// "scale_factor_at_projection_origin" indicates Mercator_1SP
                 boolean is2SP = parameterMap.containsKey("standard_parallel");
 				parameterGroup = is2SP ?
 						transformFactory.getDefaultParameters("Mercator_2SP") :
 						transformFactory.getDefaultParameters("Mercator_1SP");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("longitude_of_projection_origin", "central_meridian");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				if (is2SP) {
                     adapter.transferStandardParallels();
                 } else {
                     adapter.transferUnitless("scale_factor_at_projection_origin", "scale_factor");
                 }
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("orthographic".equals(gridMappingName)) {
 
 				parameterGroup =
 						transformFactory.getDefaultParameters("Orthographic");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("longitude_of_projection_origin", "central_meridian");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_origin");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("polar_stereographic".equals(gridMappingName)) {
 
 				parameterGroup =
 						transformFactory.getDefaultParameters("Polar_Stereographic");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("straight_vertical_longitude_from_pole", "central_meridian");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_origin");
 				adapter.transferUnitless("scale_factor_at_projection_origin", "scale_factor");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				adapter.transferStandardParallels();
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("rotated_latitude_longitude".equals(gridMappingName)) {
 
 				throw new RuntimeException("rotated_latitude_longitude projection is not supported");
 
 			} else if ("stereographic".equals(gridMappingName)) {
 
 				parameterGroup =
 						transformFactory.getDefaultParameters("Stereographic");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferAngle("longitude_of_projection_origin", "central_meridian");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_origin");
 				adapter.transferUnitless("scale_factor_at_projection_origin", "scale_factor");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("transverse_mercator".equals(gridMappingName)) {
 
 				parameterGroup =
 						transformFactory.getDefaultParameters("Transverse_Mercator");
 				ProjectionParameterAdapter adapter = new ProjectionParameterAdapter(axisLengthUnit, parameterMap, parameterGroup);
 				adapter.transferUnitless("scale_factor_at_central_meridian", "scale_factor");
 				adapter.transferAngle("longitude_of_central_meridian", "central_meridian");
 				adapter.transferAngle("latitude_of_projection_origin", "latitude_of_origin");
 				adapter.transferLength("false_easting", "false_easting");
 				adapter.transferLength("false_northing", "false_northing");
 				adapter.transferEllipsoid(datum.getEllipsoid());
 
 			} else if ("vertical_perspective".equals(gridMappingName)) {
 
 				throw new RuntimeException("vertical_perspective projection is not supported");
 
             }
 
 			if (parameterGroup == null) {
 				throw new RuntimeException("unknown grid_mapping_name");
 			}
 
 			CartesianCS cartesianCS = DefaultCartesianCS.PROJECTED.usingUnit(axisLengthUnit);
 
 			/* DOESN'T WORK - settings axis units is not enough, must also perform operation
 			 * in transform.  see below...
 			MathTransform transform = transformFactory.createParameterizedTransform(parameterGroup);
 			ProjectedCRS projectedCRS = new DefaultProjectedCRS(
 					"CF-Derived Projected CRS",
 					geographicCRS,
 					transform,
 					cartesianCS);
 			 */
 
 			/*  WORKAROUND - PART 1:  This call will generate ConcatenatedTransform
 			 *  instance with ability to scale values as needed.
 			 */
 			MathTransform transform = transformFactory.createBaseToDerived(
 						geographicCRS,
 						parameterGroup,
 						cartesianCS);
 
 			/*  WORKAROUND - PART 2: Wrap ConcatenatedTransform instance so
 			 *  that MathTransform.getParameterDescriptors() returns something
 			 *  other than null...
 			 */
 			if (! (transform instanceof ConcatenatedTransform)) {
 				throw new RuntimeException("Expected instance of ConcatenatedTransform");
 			}
 			MathTransform wrapped = new ConcatenatedTransformAdapter((ConcatenatedTransform)transform);
 
 			/*  WORKAROUND - PART 3: Create ProjectedCRS instance around
 			 *  wrapped ConcatenatedTransform instance with same axis used
 			 *  to generate ConcatenatedTransform
 			 */
 			ProjectedCRS projectedCRS = new DefaultProjectedCRS(
 				"CF-Derived Projected CRS",
 				geographicCRS,
 				wrapped,
 				cartesianCS);
 
 			return projectedCRS;
 
 		} catch (NoSuchIdentifierException e) {
 			throw new RuntimeException(e);
 		} catch (FactoryException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static Unit<Length> generateAxisLengthUnit(GridCoordSystem gcs) {
 		CoordinateAxis xAxis = gcs.getXHorizAxis();
 		CoordinateAxis yAxis = gcs.getYHorizAxis();
 		String xUnits = xAxis.getUnitsString();
 		String yUnits = yAxis.getUnitsString();
 		if (xUnits == null || yUnits == null) {
 			throw new RuntimeException("One or more axes missing units.");
 		}
 		if (!xUnits.equals(yUnits)) {
 			throw new RuntimeException("Axis units mismatch.");
 		}
         try {
             return Unit.valueOf(xUnits).asType(Length.class);
         } catch (ClassCastException e) {
             throw new RuntimeException("axit unit is not length unit.");
         }
 	}
 
 	private static DefaultEllipsoid generateEllipsoid(Map<String, Parameter> parameterMap) {
         
 		Parameter earthRadiusParameter = parameterMap.get("earth_radius");
 		Parameter semiMajorAxisParameter =  parameterMap.get("semi_major_axis");
 		Parameter semiMinorAxisParameter = parameterMap.get("semi_minor_axis");
 		Parameter inverseFlatteningParameter = parameterMap.get("inverse_flattening");
 
 		double earthRadius = earthRadiusParameter == null ? Double.NaN : earthRadiusParameter.getNumericValue();
 		double semiMajorAxis = semiMajorAxisParameter == null ? Double.NaN : semiMajorAxisParameter.getNumericValue();
 		double semiMinorAxis = semiMinorAxisParameter == null ? Double.NaN : semiMinorAxisParameter.getNumericValue();
 		double inverseFlattening = inverseFlatteningParameter == null ? Double.NaN : inverseFlatteningParameter.getNumericValue();
 
 		if (earthRadius == earthRadius) {
			return DefaultEllipsoid.createEllipsoid("CF-Derived Sphere", earthRadius, earthRadius, earthRadius < 10000 ? SI.KILOMETER : SI.METER);
 		}
 		if (semiMajorAxis == semiMajorAxis && semiMinorAxis == semiMinorAxis) {
			return DefaultEllipsoid.createEllipsoid("CF-Derived Ellipsoid", semiMajorAxis, semiMinorAxis, semiMajorAxis < 10000 ? SI.KILOMETER : SI.METER);
 		}
 		if (semiMajorAxis == semiMajorAxis && inverseFlattening == inverseFlattening) {
			return DefaultEllipsoid.createFlattenedSphere("CF-Derived Ellipsoid", semiMajorAxis, inverseFlattening, semiMajorAxis < 10000 ? SI.KILOMETER : SI.METER);
 		}
 		if (semiMinorAxis == semiMinorAxis && inverseFlattening == inverseFlattening) {
 			semiMajorAxis = semiMinorAxis / ( 1d - 1d / inverseFlattening );
			return DefaultEllipsoid.createEllipsoid("CF-Derived Ellipsoid", semiMajorAxis, semiMinorAxis, semiMinorAxis < 10000 ? SI.KILOMETER : SI.METER);
 		}
 		return DefaultEllipsoid.WGS84;
 	}
 
 	private static DefaultPrimeMeridian generatePrimeMeridian(Map<String, Parameter> parameterMap) {
 		Parameter primeMeridianParameter =  parameterMap.get("longitude_of_prime_meridian");
 		double primeMeridian = primeMeridianParameter == null ? Double.NaN : primeMeridianParameter.getNumericValue();
 		if (primeMeridian == primeMeridian) {
 			return new DefaultPrimeMeridian("CF-Derived Prime Meridian", primeMeridian, Angle.UNIT);
 		}
 		return DefaultPrimeMeridian.GREENWICH;
 	}
 
 	private static DefaultGeodeticDatum generateGeodeticDatum(Map<String, Parameter> parameterMap) {
 		DefaultEllipsoid ellipsoid = generateEllipsoid(parameterMap);
 		DefaultPrimeMeridian primeMeridian = generatePrimeMeridian(parameterMap);
 		if (ellipsoid == DefaultEllipsoid.WGS84 && primeMeridian == DefaultPrimeMeridian.GREENWICH) {
 			return DefaultGeodeticDatum.WGS84;
 		}
 		return new DefaultGeodeticDatum(
 				"CF-Derived Datum",
 				ellipsoid,
 				primeMeridian);
 	}
 
 	private static class ProjectionParameterAdapter {
 
         private final UnitConverter lengthUnitConverter;
         private final UnitConverter angleUnitConverter; // placeholder now...
 		private final Map<String, Parameter> parameterMap;
 		private final ParameterValueGroup parameterValueGroup;
 
 		public ProjectionParameterAdapter(
                 Unit<Length> lengthUnitCF,
 				Map<String, Parameter> parameterMapCF,
 				ParameterValueGroup parameterValueGroup) {
             this.lengthUnitConverter = lengthUnitCF.getConverterTo(SI.METER);
             this.angleUnitConverter = UnitConverter.IDENTITY; // placeholder now...
 			this.parameterMap = parameterMapCF;
 			this.parameterValueGroup = parameterValueGroup;
 		}
 
 		public void transferUnitless(String from, String to) {
 			Parameter parameter = parameterMap.get(from);
 			if (parameter != null) {
 				double value = parameter.getNumericValue();
 				parameterValueGroup.parameter(to).setValue(value);
 			}
 		}
 
         public void transferAngle(String from, String to) {
 			Parameter parameter = parameterMap.get(from);
 			if (parameter != null) {
 				double value = parameter.getNumericValue();
 				parameterValueGroup.parameter(to).setValue(angleUnitConverter.convert(value));
 			}
 		}
 
         public void transferLength(String from, String to) {
 			Parameter parameter = parameterMap.get(from);
 			if (parameter != null) {
 				double value = parameter.getNumericValue();
 				parameterValueGroup.parameter(to).setValue(lengthUnitConverter.convert(value));
 			}
 		}
 
 		public void transferStandardParallels() {
 			Parameter parameter = parameterMap.get("standard_parallel");
 			if (parameter != null) {
 				double[] values = parameter.getNumericValues();
 				parameterValueGroup.parameter("standard_parallel_1").setValue(
                         angleUnitConverter.convert(values[0]));
 				if (values.length > 1 && values[0] != values[1]) {
 					parameterValueGroup.parameter("standard_parallel_2").setValue(
                             angleUnitConverter.convert(values[1]));
 				}
 			}
 		}
 
 		public void transferEllipsoid(Ellipsoid ellipsoid) {
 			if (ellipsoid != null) {
                 double semiMajorAxis = ellipsoid.getSemiMajorAxis();
                 double semiMinorAxis = ellipsoid.getSemiMinorAxis();
                 Unit<Length> axisLengthUnit = ellipsoid.getAxisUnit();
                 if (!axisLengthUnit.equals(SI.METER)) {
                     UnitConverter converter = axisLengthUnit.getConverterTo(SI.METER);
                     semiMajorAxis = converter.convert(semiMajorAxis);
                     semiMinorAxis = converter.convert(semiMinorAxis);
                 }
 				parameterValueGroup.parameter("semi_major").setValue(semiMajorAxis);
 				parameterValueGroup.parameter("semi_minor").setValue(semiMinorAxis);
 			}
 		}
 
         public static int getStandardParallelCount(Map<String, Parameter> parameterMap) {
             Parameter parameter = parameterMap.get("standard_parallel");
 			if (parameter != null) {
 				double[] values = parameter.getNumericValues();
 				if (values == null || values.length == 0) {
                     return 0;
                 }
 				if (values.length == 1) {
                     return 1;
 				}
                 if (values.length == 2) {
                     return values[0] == values[1] ? 1 : 2;
                 }
 			}
             return 0;
         }
 	}
 
     private final static ParameterDescriptorGroup EMPTY_PARAMETER_DESCRIPTORS =
 				new DefaultParameterDescriptorGroup("", new GeneralParameterDescriptor[0] );
 
 	private static class ConcatenatedTransformAdapter extends ConcatenatedTransform {
 
 		public ConcatenatedTransformAdapter(ConcatenatedTransform delegate) {
 			super(delegate.transform1, delegate.transform2);
 		}
 
 		@Override
 		public ParameterDescriptorGroup getParameterDescriptors() {
 			if (transform1 instanceof AbstractMathTransform) {
 				// seems to preserve WKT produced by ProjectedCRS, but is it valid???
 				return ((AbstractMathTransform)transform1).getParameterDescriptors();
 			}
 			return EMPTY_PARAMETER_DESCRIPTORS;
 		}
 	}
 
     // Transform longitude from [-180...180] to [0...360]
     private final static MathTransform longitudeCenter180Modulo360Transform = new AbstractMathTransform() {
 
         @Override public int getSourceDimensions() { return 2; }
         @Override public int getTargetDimensions() { return 2; }
 
         @Override
         public ParameterDescriptorGroup getParameterDescriptors() {
 			return EMPTY_PARAMETER_DESCRIPTORS;
 		}
 
         @Override
         public MathTransform inverse() throws NoninvertibleTransformException {
             return longitudeCenter0Modulo360Transform;
         }
 
         @Override
         public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws TransformException {
             int count = numPts * 2;
             for (int index = 0; index < count; index += 2) {
                 dstPts[dstOff + index] = transformLongitudeCenter180Modulo360(srcPts[srcOff + index]);
                 dstPts[dstOff + index + 1] = srcPts[srcOff + index + 1];
             }
         }
     };
 
     // Transform longitude from [0...360] to [-180...180]
     private final static MathTransform longitudeCenter0Modulo360Transform = new AbstractMathTransform() {
 
         @Override public int getSourceDimensions() { return 2; }
         @Override public int getTargetDimensions() { return 2; }
 
         @Override
 		public ParameterDescriptorGroup getParameterDescriptors() {
 			return EMPTY_PARAMETER_DESCRIPTORS;
 		}
 
         @Override
         public MathTransform inverse() throws NoninvertibleTransformException {
             return longitudeCenter180Modulo360Transform;
         }
 
         @Override
         public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws TransformException {
             int count = numPts * 2;
             for (int index = srcOff; index < count; index += 2) {
                 dstPts[dstOff + index] = transformLongitudeCenter0Modulo360(srcPts[srcOff + index]);
                 dstPts[dstOff + index + 1] = srcPts[srcOff + index + 1];
             }
         }
     };
 
     // Transform longitude from [-180...180] to [0...360]
     private static double transformLongitudeCenter180Modulo360(double longitude) {
         return 180.0 + Math.IEEEremainder(longitude - 180.0, 360.0);
     }
 
     // Transform longitude from [0...360] to [-180...180]
     private static double transformLongitudeCenter0Modulo360(double longitude) {
         return Math.IEEEremainder(longitude + 180.0, 360.0) - 180.0;
     }
 }
