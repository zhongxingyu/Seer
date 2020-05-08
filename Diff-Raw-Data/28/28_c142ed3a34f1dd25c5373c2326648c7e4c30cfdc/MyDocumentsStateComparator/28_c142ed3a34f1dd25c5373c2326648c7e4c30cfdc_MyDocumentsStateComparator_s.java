 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.Arrays;
 import java.util.List;
 
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.structure.table.StateComparator;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.table.comparator.Direction;
 
 /**
  * This is a comparator for comparing resources by state.
  * 
  * @author <a href="mailto:lukasz@caltha.pl">Łukasz Urbański</a>
  * @version $Id: StateComparator.java $
  */
public class MyDocumentsStateComparator<T extends DocumentNodeResource>  extends StateComparator<T>
 {
     private final DocumentStateTool documentStateTool;
    
    private final List<String> myDocumentAllowedStateName = Arrays
                    .asList(DocumentStateTool.DOCUMENT_STATES);
 
    public MyDocumentsStateComparator(CoralSession coralSession, Logger logger, List<String> stateSortOrder, Direction direction)
     {
         super(direction);
        stateSortOrder.retainAll(myDocumentAllowedStateName);
         this.stateSortOrderList = stateSortOrder;
         this.documentStateTool = new DocumentStateTool(coralSession, logger);
     }
 
     public MyDocumentsStateComparator(CoralSession coralSession, Logger logger, Direction direction)
     {
         super(direction);
        this.stateSortOrderList = myDocumentAllowedStateName;
         this.documentStateTool = new DocumentStateTool(coralSession, logger);
     }
 
     public int compare(T r1, T r2)
     {
        int dif = stateSortOrderList.indexOf(documentStateTool.getState(r1)) - stateSortOrderList.indexOf(documentStateTool.getState(r2));
         return Direction.ASC == this.direction ? dif : -dif;
     }
 }
