 package org.systemsbiology.gaggle.core.datatypes;
 
 import net.sf.ezmorph.test.ArrayAssertions;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import org.apache.commons.collections.iterators.ArrayListIterator;
 import org.apache.commons.collections.iterators.ObjectArrayIterator;
 import org.apache.commons.collections.map.HashedMap;
 
 import java.io.Serializable;
 import java.util.*;
 import java.util.logging.Logger;
 
 import static org.systemsbiology.gaggle.core.datatypes.JSONConstants.KEY_GAGGLE_DATA;
 import static org.systemsbiology.gaggle.core.datatypes.JSONConstants.KEY_WORKFLOW_EDGES;
 import static org.systemsbiology.gaggle.core.datatypes.JSONConstants.KEY_WORKFLOW_NODES;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ning Jiang
  * Date: 10/12/12
  * Time: 9:39 AM
  * A workflow is a graph of workflow components and edges
  * Each key is the ID of a parent component
  * Each element has two lists of workflow components that are children of the parent component.
  * One list for parallel components, where different types of data (specified by the edge data type)
  * is passed from the source node to all of them at once. No user intervention is required once
  * the user triggers the "Next" operation from the source component.
  * The other list is for sequential components, where data is passed from the source node to each of the
  * component one by one. User intervention is required on the source node to trigger processing of each
  * component. This allows the user to make changes on the source node before passing data to following
  * components.
  *
  *
  * The first element of the parallel list is the source component
  * Input data is stored in param[0] of the parent component.
  * For its children, the edge type (type of data passed from source to a child)
  * is stored in their param[0] fields.
  */
 public class Workflow implements Serializable, GaggleData {
 
     protected HashMap<String, ArrayList<ArrayList<WorkflowComponent>>> workflowMap = null;
     // There can be multiple starting nodes (nodes with 0 in-degree, assuming there is no cycle!)
     protected ArrayList<String> startNodeIDs = new ArrayList<String>();
     protected String workflowID;
 
     public String getWorkflowID() { return workflowID; }
 
     public Workflow(JSONObject jsonWorkflow)
     {
         System.out.println(jsonWorkflow.toString());
         System.out.println("Instantiating workflow object...");
         workflowMap = new HashMap<String, ArrayList<ArrayList<WorkflowComponent>>>();
 
         // Parse the json object into the hashmap
         // For now we hard code it
         try
         {
             System.out.println("Populating nodes...");
 
             workflowID = jsonWorkflow.getString(JSONConstants.WORKFLOW_ID);
             HashMap<String, WorkflowComponent> nodeMap = new HashMap<String, WorkflowComponent>();
 
             // Calculate the indegree of nodes. Nodes with 0 indgrees are the starting nodes
             HashMap<String, Integer> nodeIndegree = new HashMap<String, Integer>();
 
             JSONObject nodeJSONObj = jsonWorkflow.getJSONObject(KEY_WORKFLOW_NODES);
             System.out.println("Node JSON string: " + nodeJSONObj.toString());
             for (Object key: nodeJSONObj.keySet())
             {
                 System.out.println("Node ID: " + (String)key);
                 JSONObject jsonnode = nodeJSONObj.getJSONObject((String)key);
                 System.out.println("Node JSON string: " + jsonnode.toString());
 
                 HashMap<String, Object> params = new HashMap<String, Object>();
                 params.put(WorkflowComponent.ParamNames.SubTarget.getValue(), jsonnode.getString("subaction"));
                 ArrayList<Object> datalist = new ArrayList<Object>();
                String datauri = jsonnode.getString("datauri");
                if (datauri != null && datauri.length() > 0)
                    datalist.add(datauri);
                 params.put(WorkflowComponent.ParamNames.Data.getValue(), datalist);
                 WorkflowComponent node = new WorkflowComponent(jsonnode.getString("id"),
                                                         jsonnode.getString("wfnodeid"),
                                                         jsonnode.getString("name"),
                                                         jsonnode.getString("goosename"),
                                                         "", // TODO add version info
                                                         jsonnode.getString("serviceuri"),
                                                         jsonnode.getString("arguments"),
                                                         params);
                 System.out.println("Added a node " + jsonnode.getString("id"));
                 nodeMap.put(jsonnode.getString("id"), node);
 
                 ArrayList<ArrayList<WorkflowComponent>> componentarrays = new ArrayList<ArrayList<WorkflowComponent>>();
                 ArrayList<WorkflowComponent> parallelarray = new ArrayList<WorkflowComponent>();
                 System.out.println("Adding node " + node.getComponentID() + " to parallel array");
                 parallelarray.add(node);  // The first node of the parallelarray is always the source node itself
                 ArrayList<WorkflowComponent> sequentialarray = new ArrayList<WorkflowComponent>();
                 componentarrays.add(parallelarray);
                 componentarrays.add(sequentialarray);
                 workflowMap.put(jsonnode.getString("id"), componentarrays);
                 //edgeMaps.put(jsonnode.getString("id"), componentarrays);
 
                 nodeIndegree.put(jsonnode.getString("id"), new Integer(0));
             }
 
             for (String key : workflowMap.keySet())
             {
                 ArrayList<ArrayList<WorkflowComponent>> componentlist = workflowMap.get(key);
                 ArrayList<WorkflowComponent> parallellist = componentlist.get(0);
                 ArrayList<WorkflowComponent> sequentiallist = componentlist.get(1);
                 System.out.println("WorkflowMap Node: " + key + " has "
                         + parallellist.size() + " parallel nodes and "
                         + sequentiallist.size() + sequentiallist.size() + " sequential nodes");
 
                 WorkflowComponent node = nodeMap.get(key);
                 System.out.println("NodeMap has node: " + node.getComponentID());
             }
 
             System.out.println("Populating edges...");
             JSONObject edgeJSONObj = jsonWorkflow.getJSONObject(KEY_WORKFLOW_EDGES);
             for (Object key: edgeJSONObj.keySet())
             {
                 // Get all the edges for each node
                 String keyv = (String)key;
                 if (keyv.contains("sourceid"))
                 {
                     String sourceid = edgeJSONObj.getString(keyv);
                     System.out.println("===>Source node: " + sourceid);
                     if (workflowMap.containsKey(sourceid))
                     {
                         ArrayList<WorkflowComponent> parallelarray = workflowMap.get(sourceid).get(0);
                         ArrayList<WorkflowComponent> sequentialarray = workflowMap.get(sourceid).get(1);
 
                         // Populate the target node
                         String[] splitted = keyv.split("_");
                         System.out.println("Node index: " + splitted[1]);
                         String targetid = edgeJSONObj.getString("targetid_" + splitted[1]);
                         System.out.println("===>Target node: " + targetid);
                         WorkflowComponent target = new WorkflowComponent(nodeMap.get(targetid));
                         if (target != null)
                         {
                             // Increment the indegree of target node
                             Integer indgree = nodeIndegree.get(targetid);
                             int degree = indgree.intValue() + 1;
                             System.out.println("Indegree of target node " + degree);
                             nodeIndegree.put(targetid, new Integer(degree));
 
                             String datatype = edgeJSONObj.getString("datatype_" + splitted[1]);
                             // Save edge data type to target
                             target.addParam(WorkflowComponent.ParamNames.EdgeType.getValue(), datatype.toLowerCase());
                             String isparallel = edgeJSONObj.getString("isparallel_" + splitted[1]);
                             System.out.println("Parallel: " + isparallel);
                             if (isparallel.compareTo("1") == 0)
                             {
                                 System.out.println("Added a parallel node " + targetid + " for " + sourceid);
                                 parallelarray.add(target);
                             }
                             else
                             {
                                 System.out.println("Added a sequential node " + targetid + " for " + sourceid);
                                 sequentialarray.add(target);
                             }
                         }
                         else
                         {
                             System.out.println("======Failed to clone the target node!");
                         }
                     }
                     else
                     {
                         System.out.println("Error: failed to find source node " + sourceid);
                     }
                 }
             }
 
             // Now we need to decide the starting nodes
             String startnodeKey = jsonWorkflow.getString("startNode");
             startNodeIDs.add(startnodeKey);
 
             for (String key: nodeIndegree.keySet())
             {
                 Integer indegree = nodeIndegree.get(key);
                 if (indegree.intValue() == 0)
                 {
                     System.out.println("Found a starting node: " + key);
                     if (!startNodeIDs.contains(key))
                         startNodeIDs.add(key);
                 }
             }
         }
         catch (Exception e)
         {
             System.out.println("Failed to parse Workflow JSON object: " + e.getMessage());
         }
 
         /*
         HashMap paras = new HashMap();
         //paras[0] = "http://baliga.systemsbiology.net/cmonkey/enigma/cmonkey_4.8.2_dvu_3491x739_11_Mar_02_17:37:51/htmls/cluster0001.html";
         paras.put(WorkflowComponent.ParamNames.Data.getValue(), "file://C/Users/Ning Jiang/Downloads/inf.cys");
         WorkflowComponent c1 = new WorkflowComponent("wf_1_component_1",
                 "Cytoscape",
                 "2.8.1",
                 "C:\\\\Program Files\\\\Cytoscape_v2.8.3\\\\Cytoscape.exe",
                 paras);
 
         HashMap paras1  = new HashMap();
         paras1.put(WorkflowComponent.ParamNames.EdgeType.getValue(), WorkflowAction.DataType.DataMatrix);
         WorkflowComponent c2 = new WorkflowComponent("wf_1_component_2",
                 "Firegoose",
                 "2.0",
                 "C:\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe",
                 paras1);
 
         HashMap paras2  = new HashMap();
         paras2.put(WorkflowComponent.ParamNames.EdgeType.getValue(), WorkflowAction.DataType.DataMatrix);
         paras2.put(WorkflowComponent.ParamNames.SubTarget.getValue(), "KEGG Pathway");
         WorkflowComponent c3 = new WorkflowComponent("wf_1_component_3",
                 "Firegoose",
                 "2.0",
                 "C:\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe",
                 paras2);
 
         HashMap paras3  = new HashMap();
         paras3.put(WorkflowComponent.ParamNames.EdgeType.getValue(), WorkflowAction.DataType.DataMatrix);
         paras3.put(WorkflowComponent.ParamNames.SubTarget.getValue(), "DAVID");
         WorkflowComponent c4 = new WorkflowComponent("wf_1_component_4",
                 "Firegoose",
                 "2.0",
                 "C:\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox.exe",
                 paras3);
 
         ArrayList<ArrayList<WorkflowComponent>> componentarrays1 = new ArrayList<ArrayList<WorkflowComponent>>();
         ArrayList<WorkflowComponent> parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
         ArrayList<WorkflowComponent> sequentialarray = new ArrayList<WorkflowComponent>();
         parallelarray.add(c1);
         parallelarray.add(c2);
         componentarrays1.add(parallelarray);
         componentarrays1.add(sequentialarray);
 
         ArrayList<ArrayList<WorkflowComponent>> componentarrays2 = new ArrayList<ArrayList<WorkflowComponent>>();
         parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
         sequentialarray = new ArrayList<WorkflowComponent>();
         parallelarray.add(c2);
         parallelarray.add(c3);
         parallelarray.add(c4);
         componentarrays2.add(parallelarray);
         componentarrays2.add(sequentialarray);
 
         ArrayList<ArrayList<WorkflowComponent>> componentarrays3 = new ArrayList<ArrayList<WorkflowComponent>>();
         parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
         sequentialarray = new ArrayList<WorkflowComponent>();
         parallelarray.add(c3);
         componentarrays3.add(parallelarray);
         componentarrays3.add(sequentialarray);
 
         ArrayList<ArrayList<WorkflowComponent>> componentarrays4 = new ArrayList<ArrayList<WorkflowComponent>>();
         parallelarray = new ArrayList<WorkflowComponent>(); // parallel children
         sequentialarray = new ArrayList<WorkflowComponent>();
         parallelarray.add(c4);
         componentarrays4.add(parallelarray);
         componentarrays4.add(sequentialarray);
 
         this.startNodeIDs = new String[]{"wf_1_component_1"};
         workflowMap.put("wf_1_component_1", componentarrays1);
         workflowMap.put("wf_1_component_2", componentarrays2);
         workflowMap.put("wf_1_component_3", componentarrays3);
         workflowMap.put("wf_1_component_4", componentarrays4);  */
     }
 
     public String getName() { return "Workflow"; }
     public String getSpecies() { return ""; }
     public Tuple getMetadata() { return null; }
     public ArrayList<String> getStartNodeIDs() { return startNodeIDs; }
     public HashMap<String, ArrayList<ArrayList<WorkflowComponent>>> getWorkflow() { return workflowMap; }
 }
