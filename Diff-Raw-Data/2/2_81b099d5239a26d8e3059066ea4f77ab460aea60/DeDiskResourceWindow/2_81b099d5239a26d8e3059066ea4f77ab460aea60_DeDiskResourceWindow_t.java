 package org.iplantc.de.client.views.windows;
 
 import org.iplantc.de.client.models.HasId;
 import org.iplantc.de.client.models.WindowState;
 import org.iplantc.de.client.views.windows.configs.ConfigFactory;
 import org.iplantc.de.client.views.windows.configs.DiskResourceWindowConfig;
 import org.iplantc.de.client.views.windows.configs.WindowConfig;
 import org.iplantc.de.diskResource.client.gin.DiskResourceInjector;
 import org.iplantc.de.diskResource.client.views.DiskResourceView;
 
 import com.google.common.collect.Lists;
 
 import com.sencha.gxt.widget.core.client.event.MaximizeEvent;
 import com.sencha.gxt.widget.core.client.event.MaximizeEvent.MaximizeHandler;
 import com.sencha.gxt.widget.core.client.event.RestoreEvent;
 import com.sencha.gxt.widget.core.client.event.RestoreEvent.RestoreHandler;
 import com.sencha.gxt.widget.core.client.event.ShowEvent;
 import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
 
 import java.util.List;
 
 public class DeDiskResourceWindow extends IplantWindowBase {
 
     private final DiskResourceView.Presenter presenter;
 
     public DeDiskResourceWindow(final DiskResourceWindowConfig config) {
         super(null, null);
         presenter = DiskResourceInjector.INSTANCE.getDiskResourceViewPresenter();
 
         setHeadingText(org.iplantc.de.resources.client.messages.I18N.DISPLAY.data());
        setSize("800", "480");
 
         // Create an empty
         List<HasId> resourcesToSelect = Lists.newArrayList();
         if (config.getSelectedDiskResources() != null) {
             resourcesToSelect.addAll(config.getSelectedDiskResources());
         }
         presenter.go(this, config.getSelectedFolder(), resourcesToSelect);
 
         addRestoreHandler(new RestoreHandler() {
 
             @Override
             public void onRestore(RestoreEvent event) {
                 maximized = false;
             }
         });
 
         addMaximizeHandler(new MaximizeHandler() {
 
             @Override
             public void onMaximize(MaximizeEvent event) {
                 maximized = true;
             }
         });
 
         addShowHandler(new ShowHandler() {
 
             @Override
             public void onShow(ShowEvent event) {
                 if (config != null && config.isMaximized())
                     DeDiskResourceWindow.this.maximize();
             }
         });
 
     }
 
     @Override
     public void hide() {
         if (!isMinimized()) {
             presenter.cleanUp();
         }
         super.hide();
     }
 
     @Override
     public WindowState getWindowState() {
         DiskResourceWindowConfig config = ConfigFactory.diskResourceWindowConfig();
         config.setSelectedFolder(presenter.getSelectedFolder());
         List<HasId> selectedResources = Lists.newArrayList();
         selectedResources.addAll(presenter.getSelectedDiskResources());
         config.setSelectedDiskResources(selectedResources);
         return createWindowState(config);
     }
 
     @Override
     public <C extends WindowConfig> void update(C config) {
         DiskResourceWindowConfig drConfig = (DiskResourceWindowConfig)config;
         presenter.setSelectedFolderById(drConfig.getSelectedFolder());
         presenter.setSelectedDiskResourcesById(drConfig.getSelectedDiskResources());
     }
 
 }
