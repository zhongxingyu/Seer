 package net.cyklotron.cms.structure.table;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.objectledge.table.TableFilter;
 
import net.cyklotron.cms.documents.DocumentNodeResource;
 
 /**
  * DocumentNodeResource state filter
  * 
  * @author lukasz
  */
public class StateFilter
    implements TableFilter
 {
     private final Set<String> allowedStatesNames;
 
     public StateFilter(String[] states)
     {
         allowedStatesNames = new HashSet<String>(Arrays.asList(states));
     }
 
    public boolean accept(Object object)
     {
        if(!(object instanceof DocumentNodeResource))
        {
            return false;
        }

        DocumentNodeResource node = (DocumentNodeResource)object;
        return allowedStatesNames.contains(node.getState().getName());
     }
 }
