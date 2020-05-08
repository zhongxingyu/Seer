 package net.unikernel.bummel.latches.RS;
 
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
 public class RS extends BasicElement
 {
     private String R, S, Q, notQ;
     private boolean SChecked = false, RChecked = false;
     public RS()
     {
         super(new String[]{"reset", "set", "q", "notq"});
         R    = getPorts().get(0);
         S    = getPorts().get(1);
         Q    = getPorts().get(2);
         notQ = getPorts().get(3);
     }
     @Override
     public Map<String, Double> process(Map<String, Double> valuesOnPorts) 
     {
         if(valuesOnPorts.get(R).compareTo(new Double(0)) == 0 && 
            valuesOnPorts.get(S).compareTo(new Double(0)) != 0)
         {
             valuesOnPorts.put(Q, 1.);
             valuesOnPorts.put(notQ, 0.);
             SChecked = true;
             RChecked = false;
         }
         else
             if(valuesOnPorts.get(R).compareTo(new Double(0)) != 0 &&
                valuesOnPorts.get(S).compareTo(new Double(0)) == 0)
             {
                 valuesOnPorts.put(Q, 0.);
                 valuesOnPorts.put(notQ, 1.);
                 SChecked = false;
                 RChecked = true;
             }
             else
                 if(valuesOnPorts.get(R).compareTo(new Double(0)) == 0 &&
                    valuesOnPorts.get(S).compareTo(new Double(0)) == 0)
                 {
                     if(SChecked)
                     {
                         valuesOnPorts.put(Q, 1.);
                     }
                     else
                         if(RChecked)
                         {
                             valuesOnPorts.put(notQ, 1.);
                         }
                     else
                         {
                             valuesOnPorts.put(Q, 0.);
                             valuesOnPorts.put(notQ, 0.);
                         }
                 }  
                 else
                     if(valuesOnPorts.get(R).compareTo(new Double(0)) != 0 &&
                        valuesOnPorts.get(S).compareTo(new Double(0)) != 0)
                     {
                         valuesOnPorts.put(Q, 0.);
                         valuesOnPorts.put(notQ, 0.);
                     }
        valuesOnPorts.put(R, 0.);
        valuesOnPorts.put(S, 0.);
         return valuesOnPorts;
     }
 }
