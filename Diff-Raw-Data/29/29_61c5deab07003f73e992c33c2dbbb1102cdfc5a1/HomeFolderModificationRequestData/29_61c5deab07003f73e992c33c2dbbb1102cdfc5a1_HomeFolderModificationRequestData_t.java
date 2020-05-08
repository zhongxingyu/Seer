 package fr.cg95.cvq.business.request.ecitizen;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import fr.cg95.cvq.business.request.RequestData;
 import fr.cg95.cvq.service.request.condition.IConditionChecker;
 
 /**
  * @author bor@zenexity.fr
  */
 @Deprecated
 public class HomeFolderModificationRequestData implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public static final Map<String, IConditionChecker> conditions =
         new HashMap<String, IConditionChecker>(RequestData.conditions);
 
     private Long id;
 
     public final void setId(final Long id) {
         this.id = id;
     }
 
     public final Long getId() {
         return this.id;
     }
 }
