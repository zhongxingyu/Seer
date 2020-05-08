 package gov.usgs.gdp.analysis;
 
 import java.io.IOException;
 import java.lang.String;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Formatter;
 import java.util.LinkedList;
 import java.util.List;
 
 import thredds.catalog.InvAccess;
 import thredds.catalog.InvCatalog;
 import thredds.catalog.InvDataset;
 import thredds.catalog.ServiceType;
 import ucar.nc2.VariableSimpleIF;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dt.grid.GeoGrid;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.nc2.ft.FeatureCollection;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.nc2.ft.FeatureDatasetPoint;
 import ucar.nc2.ft.PointFeatureIterator;
 import ucar.nc2.ft.StationTimeSeriesFeature;
 import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
 import ucar.nc2.units.DateRange;
 import ucar.nc2.util.NamedObject;
 
 public class NetCDFUtility {
 	
 	public static List<InvAccess> getOpenDapResources(InvCatalog catalog) {
 		if (catalog == null) return null;
 		List<InvAccess> result = new LinkedList<InvAccess>();
 
         for (InvDataset dataset : catalog.getDatasets()) {
             result.addAll(NetCDFUtility.getOpendapResourcesAux(dataset));
         }
         return result;
 	}
 
 	public static Collection<InvAccess> getOpendapResourcesAux(
 			InvDataset dataset) {
 		if (dataset == null) return null;
 		List<InvAccess> result = new LinkedList<InvAccess>();
 
         for (InvAccess resource : dataset.getAccess()) {
             if (resource.getService().getServiceType() == ServiceType.OPENDAP) {
             	result.add(resource);
             }
         }
 
         for (InvDataset nestedDataset : dataset.getDatasets()) {
         	result.addAll(getOpendapResourcesAux(nestedDataset));
         }
 
         return result;
 	}
 
     public static List<String> getDataVariableNames(String location) throws IOException {
 
         if (location == null) {
             throw new IllegalArgumentException("location can't be null");
         }
 
         List<String> variableNames = new ArrayList<String>();
         FeatureDataset dataset = null;
         try {
             dataset = FeatureDatasetFactoryManager.open(
                     null, location, null, new Formatter());
             if(dataset.getFeatureType() == FeatureType.STATION) {
                 for (VariableSimpleIF variable : dataset.getDataVariables()) {
                     String variableName = variable.getName();
                     if(!variableName.startsWith("station.")) {
                         variableNames.add(variable.getName());
                     }
                 }
             } else {
                 for (VariableSimpleIF variable : dataset.getDataVariables()) {
                     variableNames.add(variable.getName());
                 }
             }
         } finally {
             if (dataset != null) {
                 dataset.close();
             }
         }
         return variableNames;
     }
 
     public static List<String> getDateRange(String location, String variableName) throws IOException {
     	if (location == null) {
     		throw new IllegalArgumentException("location can't be null");
     	}
 
     	List<String> dateRange = new ArrayList<String>(2);
    	dateRange.add(null);
    	dateRange.add(null);
     	FeatureDataset dataset = null;
     	try {
     		dataset = FeatureDatasetFactoryManager.open(
     				null, location, null, new Formatter());
     		if(dataset.getFeatureType() == FeatureType.GRID) {
     			GeoGrid grid = ((GridDataset)dataset).findGridByName(variableName);
     			if (grid == null) return dateRange;
     			List<NamedObject> times = grid.getTimes();
     			if (times.isEmpty()) return dateRange;
    			NamedObject namedObject  = times.get(0); 
    			String time = namedObject.getName();
    			dateRange.set(0, time);
    			dateRange.set(1, times.get(times.size() - 1).getName());
     		} else if (dataset.getFeatureType() == FeatureType.STATION) {
     			DateRange dr = null;
     			List<FeatureCollection> list =
     				((FeatureDatasetPoint)dataset).getPointFeatureCollectionList();
     			for(FeatureCollection fc : list) {
     				if(fc instanceof StationTimeSeriesFeatureCollection) {
     					StationTimeSeriesFeatureCollection stsfc =
     						(StationTimeSeriesFeatureCollection)fc;
     					//stsfc = stsfc.subset(boundingBox);
     					while (dr == null && stsfc.hasNext()) {
     						StationTimeSeriesFeature stsf = stsfc.next();
     						System.out.println(stsf.getName() +  "" + stsf.size());
     						PointFeatureIterator pfi = stsf.getPointFeatureIterator(1 << 20);
     						while(pfi.hasNext()) {
     							pfi.next();
     						}
     					}
     					while (stsfc.hasNext()) {
     						dr.extend(stsfc.next().getDateRange());
     					}
     				}
     			}
     			if (dr == null) {
     				dr = dataset.getDateRange();
     			}
     			if( dr != null) {
     				dateRange.set(0, dr.getEnd().toString());
     				dateRange.set(0, dr.getEnd().toString());
     			}
     		}
     	} finally {
     		if (dataset != null) {
     			dataset.close();
     		}
     	}
     	return dateRange;
     	//
     	//            Formatter errorLog = new Formatter();
     	//            FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(
     	//                    null, location, null, errorLog);
     	//
     	//            if (featureDataset != null) {
     	//
     	//                List<String> timeSelectItemList = new ArrayList<String>();
     	//                if (featureDataset instanceof GridDataset) {
     	//                    GeoGrid grid = ((GridDataset)featureDataset).findGridByName(gridSelection);
     	//
     	//                    for (NamedObject time : grid.getTimes()) {
     	//                        timeSelectItemList.add(time.getName());
     	//                    }
     	//                } else {
     	//                    // TODO:
     	//                }
     	//            }
 
     	//                    FeatureDatasetPoint fdp = (FeatureDatasetPoint) dataset;
     	//            for (FeatureCollection fc : fdp.getPointFeatureCollectionList()) {
     	//                StationTimeSeriesFeatureCollection sc = (StationTimeSeriesFeatureCollection) fc;
     	//                for (Station s : sc.getStations()) {
     	//                    System.out.println("  " + s.getName());
     	//                }
     	//            }
     }
 
     public static void main(String[] args) {
         try {
             String location = null;
             List<String> list = null;
             // Grid Data
 //            System.out.println("Example Grid");
 //            list = getDataVariableNames("http://motherlode.ucar.edu:8080/thredds/dodsC/fmrc/NCEP/GFS/Hawaii_160km/NCEP-GFS-Hawaii_160km_fmrc.ncd");
 //            for(String name : list) {
 //                System.out.println("  " + name);
 //            }
             // Point/Station data
             System.out.println("Example Station");
 
             location = "http://motherlode.ucar.edu:8080/thredds/dodsC/station/metar/Surface_METAR_20100114_0000.nc";
             //location = "/Users/tkunicki/Downloads/oceansites/station.ncml";
             //location = "http://130.11.161.213:8080/thredds/dodsC/points/monthly.dods";
 //            list = getDataVariableNames(location);
             list = getDateRange(location, null);
             for (String name : list) {
                 System.out.println("  " + name);
             }
         } catch (IOException e) {
             // don't care...
         }
     }
 }
