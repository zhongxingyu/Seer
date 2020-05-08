 /*
  * Created on Dec 14, 2004
  */
 package uk.org.ponder.rsf.viewstate;
 
 import uk.org.ponder.beanutil.BeanGetter;
 import uk.org.ponder.beanutil.PathUtil;
 import uk.org.ponder.beanutil.entity.EntityID;
 
 /**
  * View parameters, which as well as defining the core viewID, are centred 
  * upon a particular entity, defined by Java class and entity ID.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class EntityCentredViewParameters extends ViewParameters {
   public static final String MODE_NEW = "new";
   public static final String MODE_EDIT = "edit";
   
   public String getParseSpec() {
   return ViewParameters.BASE_PARSE_SPEC + ", @1:entity.ID, mode";
   }
 
   public EntityCentredViewParameters(String viewid, EntityID entity, String mode) {
     this.viewID = viewid;
     this.entity = entity;
     this.mode = mode;
   }
  
   public EntityCentredViewParameters(String viewid, EntityID entity) {
     this(viewid, entity, null);
   }
   
   public EntityCentredViewParameters() {}
   
   public EntityID entity;
   public String mode;
   
   public String getELPath() {
     return PathUtil.buildPath(entity.entityname, entity.ID);
   }
   
  /** Fetch the referenced entity from the supplied beangetter */
   public Object fetch(BeanGetter beangetter) {
     return beangetter.getBean(getELPath());
   }
 
 }
