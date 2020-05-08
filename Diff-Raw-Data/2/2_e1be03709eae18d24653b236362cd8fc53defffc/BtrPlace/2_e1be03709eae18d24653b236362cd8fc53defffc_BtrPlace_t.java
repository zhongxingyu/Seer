 /*
  * Copyright (c) 2012 University of Nice Sophia-Antipolis
  *
  * This file is part of btrplace-sandbox.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package btrplace.sandbox;
 
 
 import btrplace.btrpsl.*;
 import btrplace.btrpsl.constraint.ConstraintsCatalog;
 import btrplace.btrpsl.constraint.DefaultConstraintsCatalog;
 import btrplace.json.JSONConverterException;
 import btrplace.json.model.ModelConverter;
 import btrplace.json.plan.ReconfigurationPlanConverter;
 import btrplace.model.*;
 import btrplace.model.constraint.SatConstraint;
 import btrplace.model.view.ShareableResource;
 import btrplace.plan.DependencyBasedPlanApplier;
 import btrplace.plan.ReconfigurationPlan;
 import btrplace.plan.TimeBasedPlanApplier;
 import btrplace.solver.SolverException;
 import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
 import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
 import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
 import net.minidev.json.JSONArray;
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 import net.minidev.json.parser.JSONParser;
 
 //import org.codehaus.jettison.json.JSONObject;
 //import sun.org.mozilla.javascript.internal.ErrorReporter;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Simili Resource to check then solve non-viable configurations.
  *
  * @author Fabien Hermenier
  */
 @Path("/inspect")
 public class BtrPlace {
 
     private DurationEvaluators durEv;
 
     private ConstraintsCatalog catalog;
 
     /*private DefaultVirtualMachineTemplateFactory vtpls;
 
     private DefaultPlatformFactory ptpls;
 
     private static PlainTextConfigurationSerializer confReader = PlainTextConfigurationSerializer.getInstance();
 
     private static ActionComparator cmp = new ActionComparator(ActionComparator.Type.start);   */
 
     public BtrPlace(@Context ServletContext context) {
         catalog = new DefaultConstraintsCatalog();
         /*try {
             PropertiesHelper p = new PropertiesHelper(context.getRealPath("config/durations.properties"));
             durEv = FastDurationEvaluatorFactory.readFromProperties(p);
             PropertiesHelper p2 = new PropertiesHelper(context.getRealPath("config/catalog.properties"));
             catalog = new ConstraintsCatalogBuilderFromProperties(p2).build();
         } catch (Exception e) {
             e.printStackTrace();
         }
         vtpls = new DefaultVirtualMachineTemplateFactory();
         vtpls.add(new VirtualMachineTemplateStub("mockVM"));
 
         ptpls = new DefaultPlatformFactory();
         */
 
     }
 
 
     private String complete(Model model, String constraints, int padding) {
 		StringBuilder n = new StringBuilder();
 
 		NamingService namingService = (NamingService) model.getView("btrpsl.ns");
 
         n.append("namespace sandbox;\n");
 		
 		for(Node node : model.getNodes()){
 			String nodeRealID = namingService.resolve(node);
 			constraints = constraints.replaceAll(nodeRealID.substring(1), nodeRealID);
 			//n.append(nodeRealID).append(" : xen<boot=60>;\n");
 		}
 
 
 		padding = 1;
 		for (int i = 0; i <= padding; i++) {
 			n.append('\n');
 		}
 
 		n.append(constraints).append("\n");
 
         String s = n.toString(); 
 
         return s;
     }
 
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     public Response check(@FormParam("cfg") String cfg, @FormParam("script") String scriptInput) {
 		//Model model = gs.makeModel();
 
 		// Create an empty model
 		Model model = new DefaultModel();
 
 		// Add a NamingService to the model
 		NamingService namingService = new InMemoryNamingService(model);
 		model.attach(namingService);
 
 		// Create the resources
 		ShareableResource rcCPU = new ShareableResource("cpu", 8, 0);
 		ShareableResource rcMem = new ShareableResource("mem", 7, 0);
 
 		// Get the mapping
 		Mapping mapping = model.getMapping();
 
 		// Load the nodes
 		JSONArray config = (JSONArray) JSONValue.parse(cfg);
 		for(Object nodeObject : config){
 			JSONObject node = (JSONObject) nodeObject;
 			// Get the ID number (without 'N') of the Node
 			Node n = null ;
 
 			// Register the node
 			try {
 				n = (Node) namingService.register("@"+node.get("id")).getElement();
 				System.out.println("Node : "+"@"+node.get("id")+" <=> "+n.id());
                 model.getAttributes().put(n, "btrpsl.id", node.get("id").toString());
 			} catch (NamingServiceException e) {
 				e.printStackTrace();
 			}
 
 			// Setting capacities
 			// Node CPU
 			int cpu = (Integer) node.get("cpu");
 			rcCPU.setCapacity(n, cpu);
 			// Node Mem
 			int mem = (Integer) node.get("mem");
 			rcMem.setCapacity(n, mem);
 
 			// Add the node to the map
 			boolean online = (Boolean) node.get("online");
 			if( online ) mapping.addOnlineNode(n);
 			else mapping.addOfflineNode(n);
 
 			// Add the VMs of the node
 			JSONArray vmsIDs = (JSONArray) node.get("vms");
 			for(Object vmObject : vmsIDs){
 				JSONObject vm = (JSONObject) vmObject;
 
 				// Create the VM object
 				VM v = null ;
 				// Register the VM
 				try {
 					v = (VM) namingService.register("sandbox."+vm.get("id")).getElement();
                     model.getAttributes().put(v, "btrpsl.id", vm.get("id").toString());
 				} catch (NamingServiceException e) {
 					e.printStackTrace();
 				}
 
 				// Consumptions
 				// CPU
 				int VMCpu = (Integer) vm.get("cpu");
 				rcCPU.setConsumption(v, VMCpu);
 				// Mem
 				int VMMem = (Integer) vm.get("mem");
 				rcMem.setConsumption(v, VMMem);
 
 				// Add the VM to the map
 				mapping.addRunningVM(v, n);
 			}
 		}
 
 		// Attach the views
   		model.attach(rcCPU);
 		model.attach(rcMem);
 
 		System.out.println("############### MODEL BUILT SUCCESSFULLY! ###################");
 
 		JSONObject response = new JSONObject();
 		response.put("errors",null);
 		response.put("solution",null);
 		//System.out.println("CLIENT Config : \n"+cfg);
 		System.out.println("CLIENT INPUT : \n"+scriptInput);
 
 		// End of previous code
         System.out.println("======= Sending mock response to client.");
 
 		//GettingStarted gs = new GettingStarted();
         //gs.run();
 
 		System.out.println("=== SNAPSHOT 1 : "+model.getVMs().size()+" VMs in the model.");
 
 		// Fixing the script to match BtrpSL requirements
 		int initialLength = scriptInput.split("\n").length;
 		scriptInput = complete(model, scriptInput,2);
 		// Number of lines added by the 'complete' method
 		int addedLinesNum = scriptInput.split("\n").length-initialLength;
 
 		System.out.println("=========== Resulting script : \n"+scriptInput+"\n===================");
 		ScriptBuilder scriptBuilder = new ScriptBuilder(model);
 		Script script = null ;
 		try {
 			System.out.println("=== SNAPSHOT 2 : "+model.getVMs().size()+" VMs in the model.");
 			script = scriptBuilder.build(scriptInput);
 			System.out.println("=== SNAPSHOT 3 : "+model.getVMs().size()+" VMs in the model.");
 
 			/*for(VM vm : model.getVMs()){
 				System.out.println("=== Test : VM:  "+vm.toString());
 			}
 			for(Node n : model.getNodes()){
 				System.out.println("=== Test : Node:  "+n.toString());
 			} */
 
 			System.out.println("=== SNAPSHOT 4 : "+model.getVMs().size()+" VMs in the model.");
 			//if(true)return null;
 			System.out.println("=== SCRIPT : \n"+script.toString());
 
 		} catch (ScriptBuilderException sbe){
 			List<ErrorMessage> errorsList = sbe.getErrorReporter().getErrors();
 			List<JSONObject> errors = new ArrayList<JSONObject>();
 
 			for(ErrorMessage error : errorsList){
 				System.out.println("Error at line "+error.lineNo());
 				JSONObject e = new JSONObject();
				e.put("row",error.lineNo() - addedLinesNum);
 				e.put("column",error.colNo());
 				e.put("message",error.message());
 				errors.add(e);
 			}
 
 			response.put("errors",errors);
 			System.out.println("==== Sending JSON of errors: \n"+response.toString());
 			return Response.ok(response).build();
 		}
 
         //List<SatConstraint> constraints = gs.makeConstraints();
         List<SatConstraint> constraints = new ArrayList(script.getConstraints());
 
 
         ArrayList<Integer> unsatisfiedConstrains = new ArrayList<Integer>();
         Integer currentConstrain = 0 ;
         for(SatConstraint c : constraints){
             if(!c.isSatisfied(model)){
                 unsatisfiedConstrains.add(currentConstrain);
             }
             currentConstrain++;
         }
 
         ChocoReconfigurationAlgorithm ra = new DefaultChocoReconfigurationAlgorithm();
 
 		System.out.println("Going to solve problem with: " + model.getVMs().size() + " VMS, " + model.getNodes().size() + " nodes");
 
 		model.detach(namingService);
 
         try {
 			System.out.println("SNAPSHOT 4.1");
             ReconfigurationPlan plan = ra.solve(model, constraints);
 			System.out.println("SNAPSHOT 4.2");
             System.out.println("=========== PLAN JSON ==========");
             ReconfigurationPlanConverter planConverter = new ReconfigurationPlanConverter();
             try {
                 JSONObject responseSolution = planConverter.toJSON(plan);
 				JSONArray actionsJSON = (JSONArray) responseSolution.get("actions");
 				for(Object actionObject : actionsJSON){
 					JSONObject actionJSON = (JSONObject) actionObject;
 					if( actionJSON.keySet().contains("vm") ){
 						// Converting the BtrPlace ID of the VM to the BtrpSL ID.
 						int btrplaceID =Integer.parseInt(actionJSON.get("vm").toString());
 						Element vm = (Element) model.getVMs().toArray()[btrplaceID];
 						int btrpSLID = Integer.parseInt(model.getAttributes().get(vm, "btrpsl.id").toString().substring(2)) ;
 						actionJSON.put("vm", btrpSLID);
 						System.out.println("Converted ID "+btrplaceID+" to "+btrpSLID);
 					}
 				}
 				response.put("actions",actionsJSON);
                 System.out.println(response.toString());
                 return Response.ok(response).build();
             } catch (JSONConverterException e) {
 				System.err.println("[ERROR] Could not convert Plan to JSON.");
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
             System.out.println("=== Time-based plan: ====");
             new TimeBasedPlanApplier().apply(plan);
             System.out.println(new TimeBasedPlanApplier().toString(plan));
             System.out.println("\n ====== Dependency based plan: ===== ");
             System.out.println(new DependencyBasedPlanApplier().toString(plan));
             //return (plan != null);
         } catch (SolverException ex) {
 			ex.printStackTrace();
             System.err.println(ex.getMessage());
             //return false;
         }
 
         /*
         System.out.println("CLIENT REQUEST ! Parsing : \n"+cfg);
 
         ModelConverter modelConverter = new ModelConverter();
         JSONObject json = new JSONObject();
 
         try {
             Model model = modelConverter.fromJSON(cfg);
             System.out.println("Parsing done.");
             System.out.println(model.toString());
         } catch (IOException e) {
             System.err.println("IO Exception");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (JSONConverterException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         if( ! script.isEmpty() ){
             String[] constraints = script.split("\n");
         }
         */
 
        /* List<Integer> nonViables = new ArrayList<Integer>();
         List<PlacementConstraint> cstrs = new ArrayList<PlacementConstraint>();
         TimedReconfigurationPlan plan = null;
         ErrorReporter errReporter = new JSonErrorReporter();
         Configuration src;
         try {
             src = confReader.unSerialize(new BufferedReader(new StringReader(cfg)));
         } catch (Exception e) {
             System.err.println(e.getMessage());
             return Response.status(400).build();
         }
 
         Map<PlacementConstraint, Integer> cstrToLine = new HashMap<PlacementConstraint, Integer>();
 
         if (!script.isEmpty()) {
 
             String[] constraints = script.split("\n");
             VJobElementBuilder eb = new DefaultVJobElementBuilder(vtpls, ptpls);
             BtrPlaceVJobBuilder vjobBuilder = new BtrPlaceVJobBuilder(eb, catalog);
 
             VJob vjob = new DefaultVJob("sandbox");
             vjobBuilder.getElementBuilder().useConfiguration(src);
 
             for (int nb = 0; nb < constraints.length; nb++) {
                 String cstr = constraints[nb];
                 if (cstr != null && !cstr.trim().isEmpty()) {
                 String buffer = complete(src, cstr, nb);
                 try {
                     VJob v = vjobBuilder.build(buffer);
                     PlacementConstraint c = v.getConstraints().iterator().next();
                     cstrToLine.put(c, nb + 1);
                     if (!c.isSatisfied(src)) {
                         nonViables.add(nb);
                     }
 
                     vjob.addConstraint(c);
                     cstrs.add(c);
                 } catch (BtrpPlaceVJobBuilderException e) {
                     ErrorReporter rep = e.getErrorReporter();
                     if (rep != null) {
                         errReporter.getErrors().addAll(rep.getErrors());
                     }
                 }
                 }
             }
             if (errReporter.getErrors().isEmpty() && !nonViables.isEmpty()) {
                 ChocoCustomRP rp = new ChocoCustomRP(durEv);
                 rp.doOptimize(false);
                 rp.setRepairMode(false);
                 rp.setTimeLimit(10);
                 List<VJob> vjobs = new ArrayList<VJob>(1);
                 vjobs.add(vjob);
                 try {
                     plan = rp.compute(src, src.getRunnings(),
                             src.getWaitings(),
                             src.getSleepings(),
                             new SimpleManagedElementSet<VirtualMachine>(),
                             new SimpleManagedElementSet<Node>(),
                             new SimpleManagedElementSet<Node>(),
                             vjobs);
                 } catch (Exception e) {
                         errReporter.append(0, 0, "no solution");
                 }
             } else {
                 plan = new DefaultTimedReconfigurationPlan(src);
             }
         } else {
             plan = new DefaultTimedReconfigurationPlan(src);
         }
         try {
             return Response.ok(buildResponse(src, errReporter, cstrs, nonViables, plan, cstrToLine).toString()).build();
         } catch (Exception x) {
             x.printStackTrace();
             return Response.status(400).build();
         }
         */
         return null;
     }
 
     /*private JSONObject buildResponse(Configuration src, ErrorReporter errors, List<PlacementConstraint> cstrs, List<Integer> nonViables, TimedReconfigurationPlan plan, Map<PlacementConstraint, Integer> cstrToLine) throws JSONException {
     //private JSONObject buildResponse() throws JSONException {
 >>>>>>> tomtom/feature-editConfig
         JSONObject o = new JSONObject();
       /*List<List<Integer>> status = new ArrayList<List<Integer>>();
         int shift = src.getAllVirtualMachines().size() + 2; //number of VMs + namespace declaration + blank line - 1 (lines start at 1)
         for (ErrorMessage err : errors.getErrors()) {
             err.message = err.message.replaceAll("sandbox\\.", "");
             err.lineNo -= shift;
         }
         o.put("errors", errors);
         if (plan == null) {
 
             List<Integer> stat = new ArrayList<Integer>();
             for (PlacementConstraint c : cstrs) {
                 if (!c.isSatisfied(src)) {
                     stat.add(-1 * cstrToLine.get(c));
                 } else {
                     stat.add(cstrToLine.get(c));
                 }
             }
             status.add(stat);
         } else {
             List<Action> longActions = new ArrayList<Action>();
             for (Action a : plan.getActions()) {
                 longActions.add(a);
             }
             Collections.sort(longActions, cmp);
 
             List<String> actions = new ArrayList<String>();
             for (Action a : longActions) {
                 if (a instanceof Migration) {
                     Migration m = (Migration) a;
                     actions.add(m.getStartMoment() + " M " + name(m.getVirtualMachine()) + " " + name(m.getHost()) + " " + name(m.getDestination()));
                 } else if (a instanceof Shutdown) {
                     Shutdown s = (Shutdown) a;
                     actions.add(s.getStartMoment() + " H " + name(s.getNode()));
                 } else if (a instanceof Startup) {
                     Startup s = (Startup) a;
                     actions.add(s.getStartMoment() + " S " + name(s.getNode()));
                 }
             }
             o.put("actions", actions);
 
             //Apply each action sequentially
             //After each move, check all the actions,
             int i = 0;
             Configuration cur = src.clone();
             while (true) {
                 List<Integer> stat = new ArrayList<Integer>();
                 for (PlacementConstraint c : cstrs) {
                     if (!c.isSatisfied(cur)) {
                         stat.add(-1 * cstrToLine.get(c));
                     } else {
                         stat.add(cstrToLine.get(c));
                     }
                 }
                 status.add(stat);
                 if (i == longActions.size()) {
                     break;
                 }
                 Action a = longActions.get(i++);
                 a.apply(cur);
 
             }
         }
         o.put("status", status);
         System.out.println(o);
 
         return o;
     }
     */
 
     /*
     private String name(VirtualMachine vm) {
         return vm.getName().substring(vm.getName().indexOf('.') + 1);
     }
 
     private String name(Node n) {
         return n.getName();
     }                */
 }
