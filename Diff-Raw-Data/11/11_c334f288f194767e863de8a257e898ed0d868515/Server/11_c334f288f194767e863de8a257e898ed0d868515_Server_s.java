 package server;
 
 import java.io.*;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import btrplace.model.*;
 import btrplace.model.constraint.*;
 import btrplace.model.view.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 @Path("/server")
 public class Server {
 
     Model model = new DefaultModel();
     Mapping map = model.getMapping();
     ArrayList<String> resourceList = new ArrayList<>();
     ArrayList<ShareableResource> sr = new ArrayList<>();
     HashMap<String, HashMap<String, Double>> overbookNodes;
     HashMap<String, Integer> cRunningCap;
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response repond() throws FileNotFoundException, IOException, JSONException {
         JSONObject data = null;
         JSONObject dataStruct = null;
         JSONObject dataConst = null;
         String chaine;
         String path = "./src/main/ressources/g5kMock.json";
         System.out.println("--------------------------------------------------------------------------BEGIN");
         try (FileInputStream stream = new FileInputStream(new File(path)); FileChannel fc = stream.getChannel()) {
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             chaine = Charset.defaultCharset().decode(bb).toString();
         }
 
         try {
             data = new JSONObject(chaine);
             String keys[] = {"struct"};
             String keys2[] = {"const"};
             dataStruct = new JSONObject(data, keys);
             dataConst = new JSONObject(data, keys2);
 
         } catch (JSONException JSe) {
             System.out.println("pbs JSON file");
         }
         
         overbookNodes = getOverbookNodes(dataConst.optJSONObject("const"));
         cRunningCap = getCRCNodes(dataConst.optJSONObject("const"));
         
         //Mapping of VMs and Nodes
         mapBuild(dataStruct.optJSONObject("struct"), overbookNodes);
         
         data.put("resources", resourceList);
 
         // attach shareable resources
         for(ShareableResource share : sr) {
             model.attach(share);
         }
 
         Set<VM> vms = map.getAllVMs();
         Set<Node> nodes = map.getAllNodes();
         //System.out.println(vms.size() + " VMs");
         //System.out.println(nodes.size() + " Nodes");
         
         //Build constraints and writes satisfaction to structure JSON
         buildConstraints(vms, nodes, dataConst.optJSONObject("const"), dataStruct.optJSONObject("struct"));
         return Response.ok(data.toString()).build();
 
     }
 
     public HashMap<String, HashMap<String, Double>> getOverbookNodes(JSONObject joConst) {
         
         JSONArray consts = joConst.optJSONArray("list");
         HashMap<String, HashMap<String, Double>> res = new HashMap<>();
         
         for(int i = 0; i< consts.length(); i++){
             JSONObject constr = consts.optJSONObject(i);
             if(constr.optString("id").equals("Overbook")){
                 JSONArray jnodes = constr.optJSONArray("Nodes").optJSONObject(0).optJSONArray("Nodes");
                 for (int j = 0; j < jnodes.length(); j++) {
                     HashMap<String, Double> rcIdAmount = new HashMap<>();
                     rcIdAmount.put( constr.optString("rcid"), constr.optDouble("amount"));
                     res.put(jnodes.optString(j), rcIdAmount);
                 }
             }
             
         }
         
         return res;
     }
     
     public HashMap<String, Integer> getCRCNodes(JSONObject joConst) {
         
         JSONArray consts = joConst.optJSONArray("list");
         HashMap<String, Integer> res = new HashMap<>();
         
         for(int i = 0; i< consts.length(); i++){
             JSONObject constr = consts.optJSONObject(i);
             if(constr.optString("id").equals("CumulatedRunningCapacity")){
                 JSONArray jnodes = constr.optJSONArray("Nodes").optJSONObject(0).optJSONArray("Nodes");
                 for (int j = 0; j < jnodes.length(); j++) {
                     res.put(jnodes.optString(j), constr.optInt("amount"));
                 }
             }
             
         }
         
         return res;
     }
     
     public JSONObject mapBuild(JSONObject jo, HashMap<String, HashMap<String, Double>> overbookNodes) throws JSONException {
         JSONArray children = jo.optJSONArray("children");
         if (isServer(jo)) {
             ArrayList<Integer> totalCap = new ArrayList<>();
             Node node = model.newNode();
             map.addOnlineNode(node);
             JSONObject resources = jo.optJSONObject("resources");
             Iterator it = resources.keys();
             ArrayList<String> rcName = new ArrayList();
             while (it.hasNext()) {
 
                 String rname = (String) it.next();
                 if (resourceList.indexOf(rname) == -1) {
                     resourceList.add(rname);
                     sr.add(new ShareableResource(rname));
                 }
                 rcName.add(rname);
             }
             
             for(String rName : rcName){
                 for(ShareableResource srIt : sr) {
                     if(srIt.getResourceIdentifier().equals(rName)) {
                         srIt.setCapacity(node, resources.optInt(rName));    
                     }
                     
                 }
                 if(overbookNodes.containsKey(jo.optString("UUID"))) {
                         if(overbookNodes.get(jo.optString("UUID")).containsKey(rName)) {
                             totalCap.add((int)(resources.optInt(rName) * overbookNodes.get(jo.optString("UUID")).get(rName)) );
                             
                         }
                         else {
                             totalCap.add((int)resources.optInt(rName));
                         }
                 }
                 totalCap.add(resources.optInt(rName));
             }
             
             int[] totalCons = new int[rcName.size()];
             for(int d : totalCons)
                 d = 0;
             
             VM vm;
             for (int i = 0; i < children.length(); i++) {
                 
                 vm = model.newVM();
                 map.addRunningVM(vm, node);
                 children.optJSONObject(i).put("btrpID", vm.id());
                 resources = children.optJSONObject(i).optJSONObject("resources");
                 
                 Iterator itChild = resources.keys();
                 rcName = new ArrayList<>();
                 while (itChild.hasNext()) {
                     String childRName = (String) itChild.next();
                     if (resourceList.indexOf(childRName) == -1) {
                         resourceList.add(childRName);
                         sr.add(new ShareableResource(childRName));
                     }
                     rcName.add(childRName);
                 }
                 for(int j =0; j< rcName.size(); j++) {
                     totalCons[j] += resources.optInt(rcName.get(j));
                 }
                 
                 for(String rName : rcName){
                     for(ShareableResource srIt : sr) {
                         if(srIt.getResourceIdentifier().equals(rName)) {
                             srIt.setConsumption(vm, resources.optInt(rName));
                         }
                     }
                 }
             }
             jo.put("btrpID", node.id());
             
             if(cRunningCap.containsKey(jo.optString("UUID"))) {
                 int tmp = cRunningCap.get(jo.optString("UUID")) - children.length();
                 if(tmp <= 0)
                     tmp = 0;
                 
                 jo.put("CumulatedRunningCapacity", tmp);
             }
             
             
             boolean free = false;
             for(int j = 0; j< totalCons.length; j++) {
                 if(totalCons[j] < totalCap.get(j)){
                     free = true;
                     break;
                 }
             }
             
             if(free){
                 JSONObject freeVM = new JSONObject();
                 JSONObject freeResources = new JSONObject();
                 freeVM.put("name", "free");
                 freeVM.put("UUID", "free");
                 freeVM.put("type", "free");
 
                 for(int j=0; j< totalCons.length; j++){
                     int tmp = totalCap.get(j) - totalCons[j];
                     if(tmp < 0)
                         tmp = 0;
                     freeResources.put(rcName.get(j), tmp);
                 }
                 freeVM.put("resources", freeResources);
 
                 children.put(freeVM);
             }
             
             
             
         } else {
             if (!isVM(jo)) {
                 for (int i = 0; i < children.length(); i++) {
                     mapBuild(children.optJSONObject(i), overbookNodes);
                 }
             }
         }
 
         return jo;
     }
 
     public void buildConstraints(Set<VM> vms, Set<Node> nodes, JSONObject joConst, JSONObject struct) throws JSONException {
 
         JSONArray consts = joConst.optJSONArray("list");
 
         for (int i = 0; i < consts.length(); i++) {
             JSONObject constr = consts.optJSONObject(i);
             switch (constr.optString("id")) {
 
                 case "Ban": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Ban ban = new Ban(vmList, nodeList);
                     boolean satisfied = ban.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Fence": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Fence fence = new Fence(vmList, nodeList);
                     boolean satisfied = fence.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Gather": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Gather gather = new Gather(vmList);
                     boolean satisfied = gather.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
                     //btrpConstraints.add(new Gather(vmList));
                     break;
                 }
                 case "Killed": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Killed killed = new Killed(vmList);
                     boolean satisfied = killed.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Lonely": {
 
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Set<VM> set = new HashSet<>(vmList);
                     Lonely lonely = new Lonely(set);
                     boolean satisfied = lonely.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Ready": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Ready ready = new Ready(vmList);
                     boolean satisfied = ready.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Root": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Root root = new Root(vmList);
                     boolean satisfied = root.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Running": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Running running = new Running(vmList);
                     boolean satisfied = running.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "SequentialVMTransitions": {
 
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     SequentialVMTransitions svt = new SequentialVMTransitions((List) vmList);
                     boolean satisfied = svt.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Sleeping": {
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Sleeping sleeping = new Sleeping(vmList);
                     boolean satisfied = sleeping.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Spread": {
 
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Set<VM> set = new HashSet<>(vmList);
                     Spread spread = new Spread(set);
                     boolean satisfied = spread.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "CumulatedResourceCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     String rc = constr.optString("rcid");
                     int amount = constr.optInt("amount");
 
                     Set<Node> set = new HashSet<>(nodeList);
                     CumulatedResourceCapacity crc = new CumulatedResourceCapacity(set, rc, amount);
                     boolean satisfied = crc.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "SingleResourceCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     String rc = constr.optString("rcid");
                     int amount = constr.optInt("amount");
 
                     Set<Node> set = new HashSet<>(nodeList);
                     SingleResourceCapacity src = new SingleResourceCapacity(set, rc, amount);
                     boolean satisfied = src.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "CumulatedRunningCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     int amount = constr.optInt("amount");
 
                     Set<Node> set = new HashSet<>(nodeList);
                     CumulatedRunningCapacity crc = new CumulatedRunningCapacity(set, amount);
                     boolean satisfied = crc.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "SingleRunningCapacity": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     int amount = constr.optInt("amount");
 
                     Set<Node> set = new HashSet<>(nodeList);
                     SingleRunningCapacity src = new SingleRunningCapacity(set, amount);
                     boolean satisfied = src.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Offline": {
 
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Offline offline = new Offline(nodeList);
 
                     boolean satisfied = offline.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
                     //btrpConstraints.add(ban);
                     break;
                 }
                 case "Online": {
 
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Online online = new Online(nodeList);
 
                     boolean satisfied = online.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
                     //btrpConstraints.add(ban);
                     break;
                 }
                 case "Quarantine": {
 
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     Quarantine quarantine = new Quarantine(nodeList);
 
                     boolean satisfied = quarantine.isSatisfied(model);
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                     }
                     //btrpConstraints.add(ban);
                     break;
                 }
                 case "Overbook": {
                     Collection<Node> nodeList = getNodeList(constr, nodes, struct);
                     ArrayList<Node> a = (ArrayList)nodeList;
                     String rc = constr.optString("rcid");
                     double amount = constr.optDouble("amount");
 
                     Set<Node> set = new HashSet<>(nodeList);
                     Overbook overbook = new Overbook(set, rc, amount);
                     boolean satisfied = overbook.isSatisfied(model);
 
                     for (Node n : nodeList) {
                         addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                         addRatioToJSON(struct, n, rc, amount);
                     }
 
                     break;
                 }
                 case "Preserve": {
 
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     String rc = constr.optString("rcid");
                     int amount = constr.optInt("amount");
                     Preserve preserve = new Preserve(vmList, rc, amount);
                     boolean satisfied = preserve.isSatisfied(model);
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     break;
                 }
                 case "Among": {
 
                     Collection<VM> vmList = getVMList(constr, vms, struct);
                     Collection<Collection<Node>> nodeParts = getNodeParts(constr, nodes, struct);
                     Among among = new Among(vmList, nodeParts);
                     boolean satisfied = among.isSatisfied(model);
 
                     for (VM v : vmList) {
                         addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                     }
 
                     for (Collection<Node> colN : nodeParts) {
                         for (Node n : colN) {
                             addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                         }
                     }
 
                     break;
                 }
                 case "Split": {
 
                     Collection<Collection<VM>> vmParts = getVMParts(constr, vms, struct);
                     Split split = new Split(vmParts);
                     boolean satisfied = split.isSatisfied(model);
                     for (Collection<VM> colV : vmParts) {
                         for (VM v : colV) {
                             addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                         }
                     }
 
                     break;
                 }
                 case "SplitAmong": {
 
                     Collection<Collection<VM>> vmParts = getVMParts(constr, vms, struct);
                     Collection<Collection<Node>> nodeParts = getNodeParts(constr, nodes, struct);
                     SplitAmong sa = new SplitAmong(vmParts, nodeParts);
                     boolean satisfied = sa.isSatisfied(model);
                     for (Collection<VM> colV : vmParts) {
                         for (VM v : colV) {
                             addConstraintToJSON(struct, v, constr.optString("id"), constr.optString("name"), satisfied);
                         }
                     }
 
                     for (Collection<Node> colN : nodeParts) {
                         for (Node n : colN) {
                             addConstraintToJSON(struct, n, constr.optString("id"), constr.optString("name"), satisfied);
                         }
                     }
 
 
                     break;
                 }
             }
 
         }
 
     }
 
     public Collection<VM> getVMList(JSONObject constr, Set<VM> vms, JSONObject struct) throws JSONException {
         JSONArray jVMs = constr.optJSONArray("VMs").optJSONObject(0).optJSONArray("VMs");
         Collection<VM> vmList = new ArrayList<>();
         for (int j = 0; j < jVMs.length(); j++) {
             vmList.add(getVM(vms, getBtrpVMID(struct, jVMs.optString(j))));
         }
         return vmList;
     }
 
     public Collection<Node> getNodeList(JSONObject constr, Set<Node> nodes, JSONObject struct) throws JSONException {
         JSONArray jnodes = constr.optJSONArray("Nodes").optJSONObject(0).optJSONArray("Nodes");
         Collection<Node> nodeList = new ArrayList<>();
         for (int j = 0; j < jnodes.length(); j++) {
             nodeList.add(getNode(nodes, getBtrpServerID(struct, jnodes.optString(j))));
         }
         return nodeList;
     }
 
     public Collection<Collection<Node>> getNodeParts(JSONObject constr, Set<Node> nodes, JSONObject struct) throws JSONException {
         Collection<Collection<Node>> nodeList = new ArrayList<>();
         for (int i = 0; i < constr.optJSONArray("Nodes").length(); i++) {
             JSONObject jnodes = constr.optJSONArray("Nodes").optJSONObject(i);
             JSONArray servers = jnodes.optJSONArray("Nodes");
             ArrayList<Node> groupNode = new ArrayList<>();
             for (int j = 0; j < servers.length(); j++) {
                 groupNode.add(getNode(nodes, getBtrpServerID(struct, servers.optString(j))));
             }
             nodeList.add(groupNode);
         }
 
         return nodeList;
     }
 
     public Collection<Collection<VM>> getVMParts(JSONObject constr, Set<VM> allVMs, JSONObject struct) throws JSONException {
         Collection<Collection<VM>> vmList = new ArrayList<>();
         for (int i = 0; i < constr.optJSONArray("VMs").length(); i++) {
             JSONObject jvms = constr.optJSONArray("VMs").optJSONObject(i);
 
             JSONArray vms = jvms.optJSONArray("VMs");
             ArrayList<VM> groupVM = new ArrayList<>();
             for (int j = 0; j < vms.length(); j++) {
                 groupVM.add(getVM(allVMs, getBtrpVMID(struct, vms.optString(j))));
             }
             vmList.add(groupVM);
 
         }
 
 
         return vmList;
     }
 
     public void addConstraintToJSON(JSONObject jo, VM vm, String constraintID, String constraintName, boolean satisfied) throws JSONException {
         int vmID = vm.id();
         int fatherID = map.getVMLocation(vm).id();
         JSONArray children = jo.optJSONArray("children");
         if (isServer(jo)) {
             //found the father node
             if (jo.optInt("btrpID") == fatherID) {
                 for (int i = 0; i < children.length(); i++) {
                     //found the VM
                     if (children.optJSONObject(i).optInt("btrpID") == vmID) {
                         writeToJson(children.optJSONObject(i), constraintID, constraintName, satisfied);
                     }
                 }
             }
         } else {
             if (!isVM(jo)) {
                 for (int i = 0; i < children.length(); i++) {
                     //Recursively check sons
                     addConstraintToJSON(children.optJSONObject(i), vm, constraintID, constraintName, satisfied);
                 }
             }
         }
 
     }
 
     public void addConstraintToJSON(JSONObject jo, Node node, String constraintID, String constraintName, boolean satisfied) throws JSONException {
         int nodeID = node.id();
         JSONArray children = jo.optJSONArray("children");
         if (isServer(jo)) {
             //found the node
             if (jo.optInt("btrpID") == nodeID) {
                 writeToJson(jo, constraintID, constraintName, satisfied);
             }
         } else {
             if (!isVM(jo)) {
                 for (int i = 0; i < children.length(); i++) {
                     //Recursively check sons
                     addConstraintToJSON(children.optJSONObject(i), node, constraintID, constraintName, satisfied);
                 }
             }
         }
 
     }
     
     public void addRatioToJSON(JSONObject jo, Node node, String resource, double amount) throws JSONException {
         int nodeID = node.id();
         JSONArray children = jo.optJSONArray("children");
         if (isServer(jo)) {
             //found the node
             if (jo.optInt("btrpID") == nodeID) {
                 writeRatioToJson(jo, resource, amount);
             }
         } else {
             if (!isVM(jo)) {
                 for (int i = 0; i < children.length(); i++) {
                     //Recursively check sons
                     addRatioToJSON(children.optJSONObject(i), node,resource, amount);
                 }
             }
         }
 
     }
     
     public void writeRatioToJson(JSONObject children, String resource, double amount) throws JSONException {
         //Have already some constraints set
         if (children.has("ratio")) {
            JSONArray constList = children.optJSONArray("ratio");
            JSONObject c = new JSONObject();
            c.put(resource, amount);
            constList.put(c);
 
             // This is the first Constraint to be added
         } else {
 
            JSONArray constList = new JSONArray();
             JSONObject c = new JSONObject();
             c.put(resource, amount);
            constList.put(c);
            children.put("ratio", constList);
 
         }
     }
 
     public void writeToJson(JSONObject children, String constraintID, String constraintName, boolean satisfied) throws JSONException {
         //Have already some constraints set
         if (children.has("Constraints")) {
             JSONArray constList = children.optJSONArray("Constraints");
             JSONObject c = new JSONObject();
             c.put("name", "" + constraintName);
             c.put("satisfied", satisfied);
             c.put("type", constraintID);
             constList.put(c);
 
             // This is the first Constraint to be added
         } else {
 
             JSONArray constList = new JSONArray();
             JSONObject c = new JSONObject();
             c.put("name", "" + constraintName);
             c.put("satisfied", satisfied);
             c.put("type", constraintID);
             constList.put(c);
             children.put("Constraints", constList);
 
         }
     }
 
     public VM getVM(Set<VM> vms, int id) {
         for (VM vm : vms) {
             if (vm.id() == id) {
                 return vm;
             }
         }
         return null;
     }
 
     public Node getNode(Set<Node> nodes, int id) {
         for (Node node : nodes) {
             if (node.id() == id) {
                 return node;
             }
         }
         return null;
     }
 
     public boolean isVM(JSONObject jo) {
 
         if (jo.has("type")) {
             if (jo.optString("type").equals("vm")) {
                 return true;
             }
         }
 
         return false;
     }
 
     //returns true if node is a server
     public boolean isServer(JSONObject jo) {
 
         if (jo.has("type")) {
             if (jo.optString("type").equals("node")) {
                 return true;
             }
         }
         return false;
     }
 
     public int getBtrpServerID(JSONObject jo, String uuid) throws JSONException {
 
         if (isServer(jo)) {
             if (jo.get("UUID").equals(uuid)) {
                 return jo.getInt("btrpID");
             }
 
         }
 
         if (!isVM(jo)) {
             if (jo.has("children")) {
                 JSONArray children = jo.getJSONArray("children");
                 for (int i = 0; i < children.length(); i++) {
                     int res = getBtrpServerID(children.optJSONObject(i), uuid);
                     if (res != -1) {
                         return res;
                     }
                 }
             }
         }
 
         return -1;
     }
 
     public int getBtrpVMID(JSONObject jo, String uuid) throws JSONException {
 
         if (isVM(jo)) {
             if (jo.get("UUID").equals(uuid)) {
                 return jo.getInt("btrpID");
             }
 
         } else {
             if (jo.has("children")) {
                 JSONArray children = jo.getJSONArray("children");
                 for (int i = 0; i < children.length(); i++) {
                     int res = getBtrpVMID(children.optJSONObject(i), uuid);
                     if (res != -1) {
                         return res;
                     }
                 }
             }
         }
 
         return -1;
     }
 }
