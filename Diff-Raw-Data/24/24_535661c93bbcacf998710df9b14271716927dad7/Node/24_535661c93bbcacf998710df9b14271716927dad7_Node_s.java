 package local.analytics;
 
 import local.analytics.lib.db.*;
 import org.json.*;
 import jdsl.graph.api.*;
 import jdsl.graph.ref.*;
 import jdsl.graph.algo.*;
 import jdsl.core.api.ObjectIterator;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.util.concurrent.*;
 import java.util.*;
 import local.analytics.lib.data.*;
 import local.analytics.lib.maths.*;
 
 public class Node{
     protected static transient final Logger logger = Logger.getLogger(Node.class.getPackage().getName());
 	private String nodePath = null;
 
     private static ScheduledThreadPoolExecutor executor = null;
     private ConcurrentHashMap<String, Vector<ProcType>> units = null;
     //units -> nodes (that produce data of that unit type)
     private ConcurrentHashMap<String, ConcurrentHashMap<String, TSDataset>> unitBuffers = null;
 	private int maxBufSize = 10;
     private int bufEltCnt = 0;
     private static Router router = null;
     private String objectId = null;
     private boolean isSymlink = false;
 
     protected static OlapMongoDBDriver olapDBDriver = null;
     protected String objectid = null;
     private String lastDataPt = null;
 
 	public Node(String path, int maxBufferSize, String id, Router rtr){
 		nodePath = path;
 		maxBufSize = maxBufferSize;
         objectId = id;
         unitBuffers = new ConcurrentHashMap<String, ConcurrentHashMap<String, TSDataset>>();
         units = new ConcurrentHashMap<String, Vector<ProcType>>();
         if(executor == null)
             executor = new ScheduledThreadPoolExecutor(10);
         if(router ==null)
             router = rtr;
 
         if(olapDBDriver==null)
             olapDBDriver = new OlapMongoDBDriver();
 	}
 
     public boolean isSymlink(){
         return isSymlink;
     }
 
     public void setSymlinkFlag(boolean flag){
         isSymlink = flag;
     }
 
     public String getObjectId(){
         return objectId;
     }
 
 	public void push(String fromPath, String data, String unitsLabel){
         System.out.println("Received data @"  + nodePath  + " from " + fromPath + ", type=" + unitsLabel);
 		try {
             if(!units.containsKey(unitsLabel)){
                 logger.warning("No buffer for units::" + unitsLabel);
                 return;
             }
             if(fromPath.equals(nodePath))
                 router.sendDataToParents(nodePath, data, unitsLabel);
 
             Vector<TSDataset> signals = null;
             synchronized(this){
                 logger.fine("Checking if " + fromPath + " is child of " + nodePath);
                 if(router.isChild(fromPath, nodePath)){
                     logger.fine(fromPath + " is child of " +nodePath);
                     JSONObject dataObj= new JSONObject(data);
 
                     //check if there are buffers for the given set of units
                     //System.out.println("Checking for " + unitsLabel);
                     if(unitBuffers.containsKey(unitsLabel)){
                         ConcurrentHashMap<String, TSDataset> bufferMap = unitBuffers.get(unitsLabel);
                         if(bufferMap.containsKey(fromPath)){
                             TSDataset ds = bufferMap.get(fromPath);
                             ds.put(dataObj.getLong("ts"), dataObj.getDouble("v"));
                             bufferMap.replace(fromPath, ds);
                             unitBuffers.replace(unitsLabel, bufferMap);
                         } else {
                             TSDataset ds = new TSDataset();
                             ds.put(dataObj.getLong("ts"), dataObj.getDouble("v"));
                             bufferMap.put(fromPath, ds);
                             unitBuffers.replace(unitsLabel, bufferMap);
                         }
                         bufEltCnt += 1;
                         //System.out.println("Buffer_Size=" + bufEltCnt);
                         if(bufEltCnt>=maxBufSize){
                             Enumeration<String> paths = bufferMap.keys();
                             signals = new Vector<TSDataset>(bufEltCnt);
                             while(paths.hasMoreElements()){
                                 String thisChildPath = paths.nextElement();
                                 TSDataset thisChildDS = bufferMap.get(thisChildPath);
                                 signals.add(thisChildDS);
                             }
                             scheduleProcTasks(signals, unitsLabel);
                             bufferMap.clear();
                             unitBuffers.replace(unitsLabel, bufferMap);
                             bufEltCnt=0;
                         } 
                     } else { //create buffer for this unitLabel
                         TSDataset ds = new TSDataset();
                         ds.put(dataObj.getLong("ts"), dataObj.getDouble("v"));
                         ConcurrentHashMap<String, TSDataset> bufferMap = new ConcurrentHashMap<String, TSDataset>();
                         bufferMap.put(fromPath, ds);
                         unitBuffers.put(unitsLabel, bufferMap);
                         bufEltCnt += 1;
                         //System.out.println("Buffer_Size=" + bufEltCnt);
                     }
                 }
             }
 		} catch (Exception e){
             e.printStackTrace();
 		}
 	}
 
 	public String pull(String units, ProcType procType, String query){
         String queryResults = "{}";
 
         if(this.isAggPoint(units, procType) && query!=null){
             try {
                 JSONObject queryObj = new JSONObject(query);
                 queryObj.put("path", nodePath);
                 logger.info("OlapQuery::" + queryObj.toString());
                 net.sf.json.JSONObject qresObj = olapDBDriver.query(queryObj.toString());
                 queryResults = qresObj.toString();
             } catch(Exception e){
                logger.log(Level.WARNING, "", e); 
             }
             return queryResults;
        } else if(this.isAggPoint(units, procType) && query!=null){
             try {
                 JSONObject error=new JSONObject();
                 String errormsg = "No aggregation point for: [ " + units + 
                                     ", LINEAR_INTERP_SUM ]";
                 error.put("error_msg", errormsg);
                 return error.toString();
             } catch(Exception e){
                 logger.log(Level.WARNING, "", e);
                 return "{\"error\":\"true\"}";
             }
         } else if(this.isAggPoint(units, procType) && query==null){
             try {
                 JSONObject resp = new JSONObject();
                 //resp.put("path", nodePath);
                 if(lastDataPt == null)
                     lastDataPt = olapDBDriver.getLastResult(nodePath);
                 resp.put("head", lastDataPt);
                 return resp.toString();
             } catch(Exception e){
                 logger.log(Level.WARNING, "", e);
             }
         }
         
         try {
             JSONObject resp = new JSONObject();
             resp.put("error", nodePath + " is not an aggregation point for [" + units + ", LINEAR_INTERP]");
             queryResults = resp.toString();
         } catch(Exception e){
             logger.log(Level.WARNING, "", e);
         }
 
 
         return queryResults;
 	}
 
     private void scheduleProcTasks(Vector<TSDataset> signals, String unitsLabel){
         Vector<ProcType> procTypesVec = units.get(unitsLabel);
         if(procTypesVec != null && procTypesVec.size()>0){
             for(int i=0; i<procTypesVec.size(); i++){
                 switch(procTypesVec.elementAt(i)){
                     case AGGREGATE:
                         System.out.println("Schedule Aggregation Task");
                         AggTask aggTask = new AggTask(signals);
                         executor.execute(aggTask);
                         break;
                     default:
                         break;
                 }
             }
         }
     }
 
     public boolean isAggPoint(){
         if(units.keySet().size()>0)
             return true;
         return false;
     }
 
     public boolean isAggPoint(String unit){
         if(units.containsKey(unit))
             return true;
         return false;
     }
 
     public boolean isAggPoint(String unit, ProcType proctype){
         if(units.containsKey(unit)){
             Vector<ProcType> v = units.get(unit);
             if(v.contains(proctype))
                     return true;
         }
         return false;
     }
 
     public void addUnit(String unit, ProcType procType){
         if(unit !=null){
             if(units.containsKey(unit)){
                 Vector<ProcType> procTypes = units.get(unit);
                 if(!procTypes.contains(procType)){
                     procTypes.add(procType);
                     units.replace(unit, procTypes);
                 } 
             } else {
                 Vector<ProcType> procTypes = new Vector<ProcType>(1);
                 procTypes.add(procType);
                 units.put(unit,procTypes);
                 System.out.println("Added " + unit + " to units[]");
             }
         }
     }
 
     public void removeUnitProcType(String unit, ProcType procType){
         if(unit !=null && units.containsKey(unit)){
             Vector<ProcType> procTypes = units.get(unit);
             if(procTypes.contains(procType)){
                 procTypes.remove(procType);
                 units.replace(unit, procTypes);
             } 
         }
     }
 
     public void removeUnit(String unit, ProcType procType){
         units.remove(unit);
     }
 
     private class AggTask implements Runnable{
 		private Vector<TSDataset> sigs = null;
 
 		public AggTask(Vector<TSDataset> signals){
 			sigs = signals;
 		}
 
 		public void run(){
 			Interpolator.interp(sigs);
             TSDataset aggSignal = aggregateAll(sigs);
             //saveAndReportResults(aggSignal);
             System.out.print("[" + nodePath + "]    ");
             for(int i=0; i<sigs.size(); i++)
                 System.out.println(sigs.get(i).getDataset().toString());
             System.out.println("\nSum [" + nodePath  + "]: " + aggSignal.getDataset().toString());
             saveAndReportResults(aggSignal);
 			return;
 		}
 
         private TSDataset aggregateAll(Vector<TSDataset> sigs){
 
             TSDataset aggSumDs = null;
             if(sigs != null && sigs.size()>0){
                 long[] tss = sigs.get(0).timestamps();
                 double[] aggVals = new double[tss.length];
                 for(int i=0; i<tss.length; i++){
                     double thisSum = 0;
                     for(int j=0; j<sigs.size(); j++)
                         thisSum += sigs.get(j).value(tss[i]);
                     aggVals[i] = thisSum;
                 }
                 aggSumDs = new TSDataset(tss, aggVals);
             }
             return aggSumDs;
         }
 
         private void saveAndReportResults(TSDataset aggSignal){
             //save it to database
             System.out.println("Saving to database");
             if(isSymlink){
                 //don't save anything, the hardlink will do that
             } else {
                 JSONArray datajarray  = aggSignal.getDataset();
                 ArrayList<net.sf.json.JSONObject> datapts = new ArrayList<net.sf.json.JSONObject>();
                 long maxTs =0;
                 for(int i=0; i<datajarray.length(); ++i){
                     try {
                         //logger.info(i + "::" + datajarray.get(i).toString());
                         JSONArray dataptArray = (JSONArray)datajarray.get(i);
                         JSONObject thisDatapt = new JSONObject();
                         thisDatapt.put("ts",dataptArray.get(0));
                         thisDatapt.put("v", dataptArray.get(1));
                         if(((Long)dataptArray.get(0)).longValue()>maxTs){
                             maxTs = ((Long)dataptArray.get(0)).longValue();
                             lastDataPt = thisDatapt.toString();
                         }
                         thisDatapt.put("path",nodePath);
                         datapts.add((net.sf.json.JSONObject) net.sf.json.JSONSerializer.toJSON(thisDatapt.toString()));
                     } catch(Exception e){
                         logger.log(Level.WARNING, "", e);
                     }
                 }
                 //save it!
                 olapDBDriver.putOlapTsEntries(datapts);
             }
             
             //push each result to parent nodes
             try {
                 System.out.println("Send to parent");
                 long[] tss = aggSignal.timestamps();
                 for(int i=0; i<tss.length; i++){
                     JSONObject thisData = new JSONObject();
                     thisData.put("ts",tss[i]);
                     thisData.put("value",aggSignal.value(tss[i]));
                     router.sendDataToParents(nodePath, thisData.toString(), "KW");
                 }
             } catch(Exception e){
                 e.printStackTrace();
             }
         }
 	}
 
 }
