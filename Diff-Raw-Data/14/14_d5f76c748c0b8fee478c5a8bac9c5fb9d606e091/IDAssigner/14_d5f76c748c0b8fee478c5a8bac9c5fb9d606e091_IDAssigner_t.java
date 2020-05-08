 /*
  * Created on 26 Feb 2007
  */
 package uk.org.ponder.rsf.renderer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.decorators.UIDecorator;
 import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
 import uk.org.ponder.rsf.content.ContentTypeInfo;
 import uk.org.ponder.rsf.template.XMLLump;
 
 public class IDAssigner {
   private static final int[] USED_ONCE = new int[] {1};
   private Map assigned = new HashMap();
   private String defaultstrategy;
   public IDAssigner(String defaultstrategy) {
     this.defaultstrategy = defaultstrategy;
   }
   
   public void adjustForID(Map attrcopy, UIComponent component) {
     String ID = null;
     String IDstrategy = defaultstrategy;
   
     if (component.decorators != null) {
       for (int i = 0; i < component.decorators.size(); ++i) {
         UIDecorator dec = component.decorators.decoratorAt(i);
         if (dec instanceof UIIDStrategyDecorator) {
           UIIDStrategyDecorator ids = (UIIDStrategyDecorator) dec;
           IDstrategy = ids.IDStrategy;
           if (ids.IDStrategy.equals(UIIDStrategyDecorator.ID_MANUAL)) {
             ID = ids.ID;
           }
           break;
         }
       }
     }
     if (ID == null) {
       if (IDstrategy.equals(ContentTypeInfo.ID_FULL) && attrcopy.get("id") != null) {
         ID = component.getFullID();
       }
     }
     if (ID != null) {
       int[] assnum = (int[]) assigned.get(ID); 
       if (assnum != null) {
        ID = ID + "!" + Integer.toString(assnum[0]);
         if (assnum == USED_ONCE) {
           assnum = new int[] {1};
           assigned.put(ID, assnum);
         }
         ++assnum[0];
       }
       else {
         assigned.put(ID, USED_ONCE);
       }
       attrcopy.put("id", ID);
     }
     if (!IDstrategy.equals(ContentTypeInfo.ID_RSF)) {
       attrcopy.remove(XMLLump.ID_ATTRIBUTE);
     }
   }
   
   
 }
