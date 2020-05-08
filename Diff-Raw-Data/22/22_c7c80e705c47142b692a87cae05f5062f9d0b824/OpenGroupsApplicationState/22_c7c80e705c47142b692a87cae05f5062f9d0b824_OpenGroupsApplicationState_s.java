 package ro.zg.opengroups.vo;
 
 import ro.zg.commons.exceptions.ContextAwareException;
 import ro.zg.open_groups.gui.OpenGroupsMainWindow;
 import ro.zg.open_groups.model.OpenGroupsModel;
 
 public class OpenGroupsApplicationState {
 
     private Entity rootEntity;
     private OpenGroupsMainWindow activeWindow;
     private Entity activeEntity;
     private String currentUri;
 
     public OpenGroupsApplicationState() throws ContextAwareException {
 	OpenGroupsModel model = OpenGroupsModel.getInstance();
 	rootEntity = model.getRootEntity();
 
     }
 
     public boolean isRootSelected() {
 	return (activeEntity != null && rootEntity.getId() == activeEntity
 		.getId());
     }
 
     /**
      * @return the rootEntity
      */
     public Entity getRootEntity() {
 	return rootEntity;
     }
 
     /**
      * @return the activeWindow
      */
     public OpenGroupsMainWindow getActiveWindow() {
 	return activeWindow;
     }
 
     /**
      * @param activeWindow
      *            the activeWindow to set
      */
     public void setActiveWindow(OpenGroupsMainWindow activeWindow) {
 	this.activeWindow = activeWindow;
     }
 
     /**
      * @return the activeEntity
      */
     public Entity getActiveEntity() {
 	if (activeEntity == null) {
 	    return rootEntity;
 	}
 	return activeEntity;
     }
 
     /**
      * @param activeEntity
      *            the activeEntity to set
      */
     public void setActiveEntity(Entity activeEntity) {
 	this.activeEntity = activeEntity;
     }
 
     public String getCurrentUri() {
 	return currentUri;
     }
 
     public void setCurrentUri(String currentUri) {
 	this.currentUri = currentUri;
     }
 
 }
