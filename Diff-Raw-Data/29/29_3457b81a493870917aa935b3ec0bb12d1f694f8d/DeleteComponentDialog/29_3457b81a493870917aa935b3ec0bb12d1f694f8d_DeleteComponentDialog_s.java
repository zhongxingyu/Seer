 package org.jtalks.poulpe.web.controller.component.dialogs;
 
 import com.google.common.annotations.VisibleForTesting;
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.service.exceptions.EntityIsRemovedException;
 import org.jtalks.poulpe.service.exceptions.JcommuneRespondedWithErrorException;
 import org.jtalks.poulpe.service.exceptions.JcommuneUrlNotConfiguredException;
 import org.jtalks.poulpe.service.exceptions.NoConnectionToJcommuneException;
 import org.jtalks.poulpe.web.controller.component.ComponentList;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zul.Messagebox;
 
 import javax.annotation.Nonnull;
 
 import static org.zkoss.util.resource.Labels.getLabel;
 
 /**
  * Shows up on the deletion of the component and would show errors if it couldn't contact JCommune to notify it about
  * component removal.
  *
  * @author stanislav bashkirtsev
  */
 public class DeleteComponentDialog implements EventListener<Event> {
     private static final String JCOMMUNE_CONNECTION_FAILED = "component.error.jcommune_no_connection";
     private static final String JCOMMUNE_RESPONSE_FAILED = "component.error.jcommune_no_response";
     private static final String JCOMMUNE_URL_FAILED = "component.error.jcommune_no_url";
     private static final String COMPONENT_DELETING_FAILED_DIALOG_TITLE = "component.deleting_problem_dialog.title";
 
     private final ComponentService componentService;
     private final ComponentList componentsToUpdate;
     private Component toDelete;
 
     public DeleteComponentDialog(ComponentService componentService, ComponentList componentsToUpdate) {
         this.componentService = componentService;
         this.componentsToUpdate = componentsToUpdate;
     }
 
     /**
      * Opens a confirm dialog to remove a specified component. If user presses, okay then {@link
      * #onEvent(org.zkoss.zk.ui.event.Event)} is triggered and component is being deleted from database. Note, that this
      * also start notification of VMs subscribed to {@link ComponentList} specified in constructor.
      *
      * @param toDelete a component to show confirm dialog for
      */
     public void confirmDeletion(@Nonnull Component toDelete) {
         this.toDelete = toDelete;
         String title = String.format(getLabel("dialogmanager.delete.title"), toDelete.getName());
         String text = String.format(getLabel("dialogmanager.delete.question"), toDelete.getName());
         Messagebox.show(text, title, Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, Messagebox.NO, this);
     }
 
     /**
      * Removes component specified in {@link #confirmDeletion(org.jtalks.poulpe.model.entity.Component)} and shows error
      * dialog if any error occurred during removal (for instance JCommune is not available to handle notification).
      */
     private void deleteComponent() {
         try {
             componentService.deleteComponent(toDelete);
             componentsToUpdate.remove(toDelete);
             toDelete = null;
         } catch (NoConnectionToJcommuneException elementDoesNotExist) {
             showDialog(JCOMMUNE_CONNECTION_FAILED);
         } catch (JcommuneRespondedWithErrorException elementDoesNotExist) {
             showDialog(JCOMMUNE_RESPONSE_FAILED);
         } catch (JcommuneUrlNotConfiguredException elementDoesNotExist) {
             showDialog(JCOMMUNE_URL_FAILED);
         } catch (EntityIsRemovedException ex) {
            // component was removed by another user: just update ComponentList
             componentsToUpdate.renew(componentService.getAll());
             toDelete = null;
         }
     }
 
     @VisibleForTesting
     void showDialog(String errorCode) {
         Messagebox.show(getLabel(errorCode),
                 getLabel(COMPONENT_DELETING_FAILED_DIALOG_TITLE), Messagebox.OK, Messagebox.ERROR);
     }
 
     /**
      * Is invoked by message box when button is pressed. <br/>{@inheritDoc}
      */
     @Override
     public void onEvent(Event event) throws Exception {
         if ((Integer) event.getData() == Messagebox.YES) {
             deleteComponent();
         }
     }
 }
