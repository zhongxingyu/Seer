 /*
  * Created on Nov 2, 2005
  */
 package uk.org.ponder.rsf.view;
 
 import java.util.List;
 
 import uk.org.ponder.rsf.componentprocessor.ComponentProcessor;
 import uk.org.ponder.rsf.componentprocessor.OrphanFinder;
 import uk.org.ponder.rsf.components.ComponentList;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIBoundString;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UILink;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.util.RSFUtil;
 import uk.org.ponder.util.Logger;
 
 /**
  * A request-scope bean which processes a component tree (view) after its
  * production by the ViewGenerator, and before rendering by the ViewRender. This
  * performs "fixups" as specified by a list of ComponentProcessor objects,
  * including applying the system's form model, resolving URLs and any
  * application-registered processing.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 // TODO: This class is aware of the existence of UISelect! We might well
 // upgrade to a generic reflective architecture at some point.
 public class ViewProcessor {
   private List processors;
 
   public void setComponentProcessors(List processors) {
     this.processors = processors;
   }
 
   // List of components found in the tree. We may
   // not use the tree directly because of possible folding mutations.
   private ComponentList worklist;
 
   private View view;
 
   // This dependency is not set via RSAC since it would execute too early.
   public void setView(View view) {
     this.view = view;
   }
 
   public View getProcessedView() {
     performFixup();
     return view;
   }
 
   private void performFixup() {
     for (int procind = 0; procind < processors.size(); ++procind) {
       ComponentProcessor proc = (ComponentProcessor) processors.get(procind);
       if (proc instanceof ViewReceiver) {
         ((ViewReceiver) proc).setView(view);
       }
     }
 
     generateWorkList();
     // ensure that the ID map is fully populated before any processors begin
     // to execute, which may be dependent on it.
     for (int compind = 0; compind < worklist.size(); ++compind) {
       UIComponent child = worklist.componentAt(compind);
       view.registerComponent(child);
     }
     OrphanFinder finder = new OrphanFinder();
     for (int compind = 0; compind < worklist.size(); ++compind) {
       UIComponent child = worklist.componentAt(compind);
       for (int procind = 0; procind < processors.size(); ++procind) {
         ComponentProcessor proc = (ComponentProcessor) processors.get(procind);
         try {
           proc.processComponent(child);
         }
         catch (Exception e) {
          Logger.log.warn("Error processing component " + child + " with ID " + child.getFullID(), e);
         }
       }
       finder.processComponent(child);
     }
     finder.report();
   }
 
   private void generateWorkList() {
     worklist = new ComponentList();
     appendContainer(view.viewroot);
   }
 
   private void appendComponent(UIBound toappend) {
     if (toappend != null) {
       if (toappend instanceof UIBoundString) {
         appendComponent(((UIBoundString) toappend).renderer);
       }
       worklist.add(toappend);
     }
   }
 
   private void appendContainer(UIContainer toappend) {
     ComponentList thischildren = toappend.flattenChildren();
 
     for (int i = 0; i < thischildren.size(); ++i) {
       UIComponent thischild = thischildren.componentAt(i);
       if (thischild instanceof UIContainer) {
         appendContainer((UIContainer) thischild);
       }
       if (thischild instanceof UISelect) {
         // TODO: Move this select dependency into a separate fixer!!
         UISelect select = (UISelect) thischild;
         RSFUtil.fixupSelect(select);
         appendComponent(select.selection);
         appendComponent(select.names);
       }
       else if (thischild instanceof UILink) {
         appendComponent(((UILink)thischild).linktext);
       }
       else if (thischild instanceof UIBoundString) {
         appendComponent(((UIBoundString) thischild).renderer);
       }
     }
     // add the actual children later, to ensure dependent components resolved
     // first
     worklist.addAll(thischildren);
   }
 }
