 package net.cyklotron.cms.structure.table;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.objectledge.table.TableFilter;
 
import net.cyklotron.cms.workflow.StatefulResource;
 
 /**
  * DocumentNodeResource state filter
  * 
  * @author lukasz
  */
public class StateFilter<T extends StatefulResource>
    implements TableFilter<T>
 {
     private final Set<String> allowedStatesNames;
 
     public StateFilter(String[] states)
     {
         allowedStatesNames = new HashSet<String>(Arrays.asList(states));
     }
 
    public boolean accept(StatefulResource resource)
     {
        return allowedStatesNames.contains(resource.getState().getName());
     }
 }
