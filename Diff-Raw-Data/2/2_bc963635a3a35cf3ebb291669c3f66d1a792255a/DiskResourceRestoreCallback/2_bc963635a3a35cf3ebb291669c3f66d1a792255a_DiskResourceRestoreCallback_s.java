 package org.iplantc.core.uidiskresource.client.services.callbacks;
 
 import java.util.Set;
 
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.info.SuccessAnnouncementConfig;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResource;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.RestoreResponse;
 import org.iplantc.core.uicommons.client.models.diskresources.RestoreResponse.RestoredResource;
 import org.iplantc.core.uidiskresource.client.services.errors.DiskResourceErrorAutoBeanFactory;
 import org.iplantc.core.uidiskresource.client.services.errors.ErrorDiskResourceMove;
 import org.iplantc.core.uidiskresource.client.views.DiskResourceView;
 
 import com.google.gwt.core.client.GWT;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.Splittable;
 
 /**
  * A DiskResourceServiceCallback for data service "restore" endpoint requests.
  * 
  * @author psarando
  * 
  */
 public class DiskResourceRestoreCallback extends DiskResourceServiceCallback<String> {
     private final DiskResourceView view;
     private final DiskResourceAutoBeanFactory drFactory;
     private final Set<DiskResource> selectedResources;
 
     public DiskResourceRestoreCallback(DiskResourceView view, DiskResourceAutoBeanFactory drFactory,
             Set<DiskResource> selectedResources) {
         super(view);
 
         this.drFactory = drFactory;
         this.selectedResources = selectedResources;
         this.view = view;
     }
 
     @Override
     protected String getErrorMessageDefault() {
         return I18N.ERROR.restoreDefaultMsg();
     }
 
     @Override
     public void onSuccess(String result) {
         super.onSuccess(result);
 
         checkForPartialRestore(result);
         if(view.isSelectAll()) {
             view.refreshFolder(view.getSelectedFolder());
         } else {
             view.removeDiskResources(selectedResources);
         }
     }
 
     @Override
     public void onFailure(Throwable caught) {
        super.onFailure(caught);
         DiskResourceErrorAutoBeanFactory factory = GWT.create(DiskResourceErrorAutoBeanFactory.class);
         AutoBean<ErrorDiskResourceMove> errorBean = AutoBeanCodex.decode(factory, ErrorDiskResourceMove.class, caught.getMessage());
 
         ErrorHandler.post(errorBean.as(), caught);
     }
     
     private void checkForPartialRestore(String result) {
         RestoreResponse response = AutoBeanCodex.decode(drFactory, RestoreResponse.class, result).as();
         Splittable restored = response.getRestored();
 
         for (DiskResource resource : selectedResources) {
             Splittable restoredResourceJson = restored.get(resource.getId());
 
             if (restoredResourceJson != null) {
                 RestoredResource restoredResource = AutoBeanCodex.decode(drFactory,
                         RestoredResource.class, restoredResourceJson).as();
 
                 if (restoredResource.isPartialRestore()) {
                    IplantAnnouncer.getInstance().schedule(I18N.DISPLAY.partialRestore());
                    break;
                 } else {
                     IplantAnnouncer.getInstance().schedule(new SuccessAnnouncementConfig(I18N.DISPLAY.restoreMsg()));
                     break;
                 }
             }
         }
     }
 }
