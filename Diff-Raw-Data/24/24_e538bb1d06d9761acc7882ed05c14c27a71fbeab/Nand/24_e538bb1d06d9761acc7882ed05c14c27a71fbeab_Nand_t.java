 package net.unikernel.bummel.advanced_logic_elements.Nand;
 
 import java.util.Map;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import net.unikernel.bummel.project_model.api.Element;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  *
  * @author Roma
  */
 @ServiceProvider(service=BasicElement.class)
 @Element.ElementData(dataFile="element_info.xml")
 public class Nand extends BasicElement
 {
     public Nand()
     {
         super(new String[] {"input1", "input2", "output"});
     }
 
     @Override
     public Map<String, Double> process(Map<String, Double> valuesOnPorts) 
     {
        if(valuesOnPorts.get(getPorts().get(0)).compareTo(new Double(0)) != 0 && valuesOnPorts.get(getPorts().get(1)).compareTo(new Double(0)) != 0)
         {
             valuesOnPorts.put(getPorts().get(2), new Double(0));
         }
         else
         {
             valuesOnPorts.put(getPorts().get(2), new Double(1));
         }
        valuesOnPorts.put(getPorts().get(0), 0.);
        valuesOnPorts.put(getPorts().get(1), 0.);
         return valuesOnPorts;
     }
 }
