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
 import btrplace.json.JSONConverterException;
 import btrplace.json.plan.ReconfigurationPlanConverter;
 import btrplace.model.*;
 import btrplace.model.constraint.SatConstraint;
 import btrplace.model.view.ShareableResource;
 import btrplace.plan.ReconfigurationPlan;
 import btrplace.plan.event.MigrateVM;
 import btrplace.solver.SolverException;
 import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
 import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
 import btrplace.solver.choco.durationEvaluator.LinearToAResourceActionDuration;
 import net.minidev.json.JSONArray;
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.*;
 import java.util.*;
 
 /**
  * Simili Resource to check then solve non-viable configurations.
  *
  * @author Fabien Hermenier
  */
 @Path("/inspect")
 public class BtrPlace {
 
     public BtrPlace(@Context ServletContext context) {
 
     }
 
 	/**
 	 * Prepends an initial script with more data to get it valid as a BtrpSL script.
 	 * This allows the client-side script to be more lightweight and easier to read.
 	 * @param model The model concerned about the script.
 	 * @param constraints The client constraints script.
 	 * @return The completed and valid script.
 	 */
     private String complete(Model model, String constraints) {
 		StringBuilder n = new StringBuilder();
 
 		NamingService namingService = (NamingService) model.getView("btrpsl.ns");
 
         n.append("namespace sandbox;\n");
 		
 		for(Node node : model.getNodes()){
 			String nodeRealID = namingService.resolve(node);
 			constraints = constraints.replaceAll(nodeRealID.substring(1), nodeRealID);
 		}
 
 		n.append('\n');
 
 		n.append(constraints).append("\n");
 
         return n.toString();
     }
 
 
 	/**
 	 * Look for a solution for a given configuration and a given constraints script.
 	 * Issue a Response to client.
 	 * @param cfg The client's configuration (Nodes,VMs)
 	 * @param scriptInput Constraints script input from client.
 	 * @return The server computed solution.
 	 */
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
 				//System.out.println("Node : "+"@"+node.get("id")+" <=> "+n.id());
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
 
 		System.out.println("Model built successfully");
 
 		// Preparing the response
 		JSONObject response = new JSONObject();
 		response.put("errors",null);
 		response.put("solution",null);
 
 
 		// Fixing the script to match BtrpSL requirements
 		int initialLength = scriptInput.split("\n").length;
 		scriptInput = complete(model, scriptInput);
 		// Number of lines added by the 'complete' method
 		int addedLinesNum = scriptInput.split("\n").length-initialLength;
 
 		ScriptBuilder scriptBuilder = new ScriptBuilder(model);
 		Script script ;
 		try {
 			script = scriptBuilder.build(scriptInput);
 
 
 		} catch (ScriptBuilderException sbe){
 			List<ErrorMessage> errorsList = sbe.getErrorReporter().getErrors();
 			List<JSONObject> errors = new ArrayList<JSONObject>();
 
 			for(ErrorMessage error : errorsList){
 				JSONObject e = new JSONObject();
 				e.put("row",error.lineNo() - addedLinesNum);
 				e.put("column",error.colNo());
 				e.put("message",error.message());
 				errors.add(e);
 			}
 
 			response.put("errors",errors);
 			return Response.ok(response).build();
 		}
 
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
 
 		//System.out.println("Going to solve problem with: " + model.getVMs().size() + " VMS, " + model.getNodes().size() + " nodes");
 
 		model.detach(namingService);
 
 		ra.getDurationEvaluators().register(MigrateVM.class, new LinearToAResourceActionDuration("mem", 0.5));
 
         try {
             ReconfigurationPlan plan = ra.solve(model, constraints);
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
 					}
 				}
 				response.put("actions",actionsJSON);
                 return Response.ok(response).build();
             } catch (JSONConverterException e) {
 				System.err.println("[ERROR] Could not convert Plan to JSON.");
                 e.printStackTrace();
             }
         } catch (SolverException ex) {
 			System.err.println("[ERROR] Could not find a solution.");
 			ex.printStackTrace();
			return Response.ok(response).build();
         }
 		return Response.serverError().build();
     }
 }
