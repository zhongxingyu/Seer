 package it.wolfed.swing;
 
import it.wolfed.model.PetriNetGraph;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public final class AnalysisComponent extends JPanel
 {
     private PetriNetGraph graph;
     
     public AnalysisComponent(PetriNetGraph graph)
     {
         this.graph = graph;
         process();
         
         // Auto update analysis panel on change
         graph.getModel().addListener(mxEvent.CHANGE, new mxEventSource.mxIEventListener()
         {
             @Override
             public void invoke(Object sender, mxEventObject evt)
             {
                 process();
                 doLayout();
             }
         });
     }
     
     public void process()
     {
         // Refresh
         removeAll();
                
         // Is WorkFlow
         add(new JLabel(
             "<html>"
             + "<strong>Is WorkFlow: </strong>"
             + checkProperty("Single Initial Place", graph.isSingleInitialPlace()) + " | "
             + checkProperty("Single Final Place", graph.isSingleFinalPlace()) + " | "
             + checkProperty("Flow Strongly Connected", graph.isWorkflowStronglyConnected())
             +"</html>"
 
         ));
     }
     
     private String checkProperty(String text, Boolean question)
     {
         return (question == true) 
             ? "<span color=\"green\">&oplus; " + text + "</span> "
             : "<span color=\"red\">&otimes; " + text + "</span> ";
     }
 }
