 package org.jtalks.poulpe.web.controller.component.dialogs;
 
 import org.jtalks.poulpe.model.entity.Component;
 import org.jtalks.poulpe.service.ComponentService;
import org.jtalks.poulpe.service.exceptions.EntityIsRemovedException;
 import org.jtalks.poulpe.service.exceptions.JcommuneRespondedWithErrorException;
 import org.jtalks.poulpe.service.exceptions.JcommuneUrlNotConfiguredException;
 import org.jtalks.poulpe.service.exceptions.NoConnectionToJcommuneException;
 import org.jtalks.poulpe.web.controller.component.ComponentList;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zul.Messagebox;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.assertTrue;
 
 /**
  * @author stanislav bashkirtsev
  */
 public class DeleteComponentDialogTest {
     private DeleteComponentDialog sut;
     private ComponentService componentService;
     private ComponentList componentsToUpdate;
 
     @BeforeMethod
     public void setupMocks() {
         componentService = mock(ComponentService.class);
         componentsToUpdate = new ComponentList();
         sut = spy(new DeleteComponentDialog(componentService, componentsToUpdate));
     }
 
     @Test
     public void shouldDoNothingIfRemovalNotConfirmed() throws Exception {
         sut.onEvent(negativeConfirm());
         verify(componentService, never()).deleteComponent(any(Component.class));
     }
 
     @Test
     public void successfulRemovalShouldDeleteFromComponentList() throws Exception {
         componentsToUpdate.add(null);
         sut.onEvent(positiveConfirm());
         assertTrue(componentsToUpdate.getList().isEmpty());
     }
 
     @Test
     public void noConnectionToJcommuneShouldShowError() throws Exception {
         doThrow(NoConnectionToJcommuneException.class).when(componentService).deleteComponent(any(Component.class));
         doNothing().when(sut).showDialog(anyString());
 
         sut.onEvent(positiveConfirm());
         verify(sut).showDialog("component.error.jcommune_no_connection");
     }
 
     @Test
     public void jcommuneFailureResponseShouldShowError() throws Exception {
         doThrow(JcommuneRespondedWithErrorException.class).when(componentService).deleteComponent(any(Component.class));
         doNothing().when(sut).showDialog(anyString());
 
         sut.onEvent(positiveConfirm());
         verify(sut).showDialog("component.error.jcommune_no_response");
     }
 
     @Test
     public void noUrlConfiguredForJcommuneShouldShowError() throws Exception {
         doThrow(JcommuneUrlNotConfiguredException.class).when(componentService).deleteComponent(any(Component.class));
         doNothing().when(sut).showDialog(anyString());
 
         sut.onEvent(positiveConfirm());
         verify(sut).showDialog("component.error.jcommune_no_url");
     }
 
    @Test
    public void componentWasRemovedShouldShowError() throws Exception {
        doThrow(EntityIsRemovedException.class).when(componentService).deleteComponent(any(Component.class));
        doNothing().when(sut).showDialog(anyString());

        sut.onEvent(positiveConfirm());
        verify(sut).showDialog("component.error.is_removed");
    }

     private Event positiveConfirm() {
         return new Event("", null, Messagebox.YES);
     }
 
     private Event negativeConfirm() {
         return new Event("", null, Messagebox.NO);
     }
 }
