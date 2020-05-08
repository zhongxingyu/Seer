 package edu.mit.cci.simulation.model;
 
 import org.apache.log4j.Logger;
 import org.hibernate.annotations.OrderBy;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import javax.persistence.CascadeType;
 import javax.persistence.ManyToMany;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.*;
 import java.util.List;
 import java.util.Set;
 
 @RooJavaBean
 @RooToString
 @RooEntity
 @XmlRootElement(name="CompositeSimulation")
 @XmlAccessorType(XmlAccessType.NONE)
 public class CompositeSimulation extends DefaultSimulation {
 
     @ManyToMany(cascade = CascadeType.ALL)
    @javax.persistence.OrderBy("order_")
     private List<Step> steps = new ArrayList<Step>();
 
     @ManyToMany(cascade = CascadeType.ALL)
     private Set<CompositeStepMapping> stepMapping = new HashSet<CompositeStepMapping>();
 
     @Transient
     private static Logger log = Logger.getLogger(CompositeSimulation.class);
 
     public Scenario run(List<Tuple> siminputs) throws SimulationException {
         CompositeScenario result = new CompositeScenario();
         result.setSimulation(this);
         Collections.sort(steps, new Comparator<Step>() {
             public int compare(Step step, Step step1) {
                 return (int)Math.signum(step.getOrder_()-step1.getOrder_());
             }
         });
 
         for (Step s : steps) {
             CompositeStepMapping m = null;
             for (CompositeStepMapping mapping : stepMapping) {
                 if (s.equals(mapping.getToStep())) {
                     m = mapping;
                     break;
                 }
             }
 
             if (m == null) {
                 throw new SimulationException("Missing a mapping step; cannot reach step " + s.getOrder_());
             }
 
             List<Tuple> fromPriorStep = new ArrayList<Tuple>();
             if (m.getFromStep() == null) {
                 fromPriorStep.addAll(siminputs);
             } else {
                 ScenarioList sl = result.getChildScenarios().get(m.getFromStep());
                 if (sl == null) {
                     throw new SimulationException("Missing scenario information required for step");
                 }
                 for (DefaultScenario scenario : sl.getScenarios()) {
                     for (Variable v : scenario.getSimulation().getOutputs()) {
                         fromPriorStep.add(scenario.getVariableValue(v));
                     }
 
                 }
             }
 
             List<Tuple> toNextStep = new ArrayList<Tuple>();
             for (Tuple t : fromPriorStep) {
                 if (m.getMapping().containsKey(t.getVar())) {
                     Tuple nt = Tuple.copy(t);
                     nt.setVar(m.getMapping().get(t.getVar()));
                     toNextStep.add(nt);
                 }
             }
 
             for (DefaultSimulation sim : s.getSimulations()) {
                 result.addToStep(s, (DefaultScenario) sim.run(toNextStep));
             }
         }
 
         for (CompositeStepMapping m : stepMapping) {
             if (m.getToStep() == null) {
                 ScenarioList sl = result.getChildScenarios().get(m.getFromStep());
                 if (sl == null) {
                     throw new SimulationException("Missing scenario information required for final step");
                 }
                 for (DefaultScenario scenario : sl.getScenarios()) {
                     for (Variable v : scenario.getSimulation().getOutputs()) {
                         if (m.getFromVars().contains(v)) {
                             Tuple old = scenario.getVariableValue(v);
                             Tuple n = Tuple.copy(old);
                             n.setVar(m.getMapping().get(v));
                             result.getValues_().add(n);
                         }
 
                     }
                 }
             }
 
         }
             result.getValues_().addAll(siminputs);
             result.persist();
             return result;
 
 
 
     }
 }
