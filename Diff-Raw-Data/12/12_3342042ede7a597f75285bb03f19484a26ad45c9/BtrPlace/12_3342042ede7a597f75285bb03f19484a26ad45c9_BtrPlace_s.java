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
 
 import btrpsl.BtrPlaceVJobBuilder;
 import btrpsl.constraint.ConstraintsCatalog;
 import btrpsl.constraint.ConstraintsCatalogBuilderFromProperties;
 import btrpsl.template.VirtualMachineTemplateStub;
 import entropy.PropertiesHelper;
 import entropy.configuration.Configuration;
 import entropy.configuration.Node;
 import entropy.configuration.SimpleManagedElementSet;
 import entropy.configuration.VirtualMachine;
 import entropy.configuration.parser.PlainTextConfigurationSerializer;
 import entropy.plan.DefaultTimedReconfigurationPlan;
 import entropy.plan.TimedReconfigurationPlan;
 import entropy.plan.action.*;
 import entropy.plan.action.Shutdown;
 import entropy.plan.choco.ChocoCustomRP;
 import entropy.plan.durationEvaluator.DurationEvaluator;
 import entropy.plan.durationEvaluator.FastDurationEvaluatorFactory;
 import entropy.platform.DefaultPlatformFactory;
 import entropy.template.DefaultVirtualMachineTemplateFactory;
 import entropy.vjob.DefaultVJob;
 import entropy.vjob.PlacementConstraint;
 import entropy.vjob.VJob;
 import entropy.vjob.builder.DefaultVJobElementBuilder;
 import entropy.vjob.builder.VJobElementBuilder;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.*;
 import java.io.BufferedReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Simili Resource to check then solve non-viable configurations.
  *
  * @author Fabien Hermenier
  */
 @Path("/inspect")
 public class BtrPlace {
 
     private DurationEvaluator durEv;
 
     private ConstraintsCatalog catalog;
 
     private DefaultVirtualMachineTemplateFactory vtpls;
 
     private DefaultPlatformFactory ptpls;
 

     private static PlainTextConfigurationSerializer confReader = PlainTextConfigurationSerializer.getInstance();
 
    public BtrPlace() {
         try {
            PropertiesHelper p = new PropertiesHelper(this.getClass().getClassLoader().getResourceAsStream("../../config/durations.properties"));
             durEv = FastDurationEvaluatorFactory.readFromProperties(p);
            PropertiesHelper p2 = new PropertiesHelper(this.getClass().getClassLoader().getResourceAsStream("../../config/catalog.properties"));
             catalog = new ConstraintsCatalogBuilderFromProperties(p2).build();
         } catch (Exception e) {
             e.printStackTrace();
         }
         vtpls = new DefaultVirtualMachineTemplateFactory();
         vtpls.add(new VirtualMachineTemplateStub("mockVM"));
 
         ptpls = new DefaultPlatformFactory();
 
     }
 
     private String complete(Configuration cfg, String constraints) {
         StringBuilder n = new StringBuilder();
         n.append("namespace sandbox;\n");
         for (VirtualMachine vm : cfg.getAllVirtualMachines()) {
             n.append(vm.getName().substring(vm.getName().indexOf('.') + 1)).append(" : mockVM;\n");
         }
         n.append(constraints).append("\n");
 
 
         String s = n.toString();
         //Add the '@' before each node name.
         for (Node node : cfg.getAllNodes()) {
             s = s.replaceAll(node.getName(), "@" + node.getName());
         }
         return s;
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     public Response check(@FormParam("cfg") String cfg, @FormParam("script") String script) {
         List<Integer> nonViables = new ArrayList<Integer>();
         List<PlacementConstraint> cstrs = new ArrayList<PlacementConstraint>();
         TimedReconfigurationPlan plan = null;
         List<String> errors = new ArrayList<String>();
         Configuration src;
         try {
             src = confReader.unSerialize(new BufferedReader(new StringReader(cfg)));
         } catch (Exception e) {
             System.err.println(e.getMessage());
             return Response.status(400).build();
         }
 
         if (!script.isEmpty()) {
             String[] constraints = script.split("\n");
 
             VJobElementBuilder eb = new DefaultVJobElementBuilder(vtpls, ptpls);
 
             BtrPlaceVJobBuilder vjobBuilder = new BtrPlaceVJobBuilder(eb, catalog);
 
             int nb = 0;
 
             VJob vjob = new DefaultVJob("sandbox");
             vjobBuilder.getElementBuilder().useConfiguration(src);
             for (String cstr : constraints) {
                 String buffer = complete(src, cstr);
                 try {
                     VJob v = vjobBuilder.build(buffer);
                     PlacementConstraint c = v.getConstraints().iterator().next();
                     if (!c.isSatisfied(src)) {
                         nonViables.add(nb);
                     }
                     vjob.addConstraint(c);
                     cstrs.add(c);
                     nb++;
                 } catch (Exception e) {
                     for (String m : e.getMessage().split("\n")) {
                         errors.add(simplifyErrorMessage(src, m));
                     }
                 }
             }
             if (errors.isEmpty() && !nonViables.isEmpty()) {
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
                     errors.add("no solution");
                 }
             } else {
                 plan = new DefaultTimedReconfigurationPlan(src);
             }
         } else {
             plan = new DefaultTimedReconfigurationPlan(src);
         }
         try {
             return Response.ok(buildReponse(src, errors, cstrs, nonViables, plan).toString()).build();
         } catch (Exception x) {
             return Response.status(400).build();
         }
     }
 
     private JSONObject buildReponse(Configuration src, List<String> errors, List<PlacementConstraint> cstrs, List<Integer> nonViables, TimedReconfigurationPlan plan) throws JSONException {
         JSONObject o = new JSONObject();
         List<List<Integer>> status = new ArrayList<List<Integer>>();
         o.put("errors", errors);
         if (plan == null) {
 
             List<Integer> stat = new ArrayList<Integer>();
             int j = 1;
             for (PlacementConstraint c : cstrs) {
                 if (!c.isSatisfied(src)) {
                     stat.add(-1 * j);
                 } else {
                     stat.add(j);
                 }
                 j++;
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
                 int j = 1;
                 for (PlacementConstraint c : cstrs) {
                     if (!c.isSatisfied(cur)) {
                         stat.add(-1 * j);
                     } else {
                         stat.add(j);
                     }
                     j++;
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
         return o;
     }
 
     private String name(VirtualMachine vm) {
         return vm.getName().substring(vm.getName().indexOf('.') + 1);
     }
 
     private String name(Node n) {
         return n.getName();
     }
 
     private static Pattern syntaxError = Pattern.compile("\\((\\d+):(\\d+)\\)\\ssandbox:\\s(.+)");
     private static Pattern lexError = Pattern.compile("line\\s(\\d+):(-?\\d+)\\s(.+)");
 
     private String simplifyErrorMessage(Configuration cfg, String s) {
         //remove any "sandbox."
         String res = s.replaceAll("sandbox\\.", "");
 
         //Remove the list of available constraints.
         if (res.contains("Unknown constraint")) {
             res = res.substring(0, res.indexOf('.'));
         }
 
         //Remove the vjob identifier and shift the lines
         int shift = cfg.getAllVirtualMachines().size() + 1; //number of VMs + namespace declaration + blank line - 1 (lines start at 1)
         //System.err.println("Shift= " + shift);
 
         Matcher m = syntaxError.matcher(res);
 
         if (m.matches()) {
             return "(" + (Integer.parseInt(m.group(1)) - shift) + ":" + m.group(2) + ") " + m.group(3);
         } else {
             m = lexError.matcher(res);
             if (m.matches()) {
                 return "(" + (Integer.parseInt(m.group(1)) - shift) + ":" + m.group(2) + ") " + m.group(3);
             }
         }
 
         return res;
     }
 
     private static ActionComparator cmp = new ActionComparator(ActionComparator.Type.start);
 }
