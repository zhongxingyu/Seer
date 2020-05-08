 /**
  * 
  */
 package org.iplantc.core.appsIntegration.client.view;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.iplantc.core.appsIntegration.client.I18N;
 import org.iplantc.core.appsIntegration.client.models.DCProperties;
 import org.iplantc.core.appsIntegration.client.models.DeployedComponent;
 import org.iplantc.core.appsIntegration.client.view.cells.DCNameHyperlinkCell;
 
 import com.google.gwt.core.shared.GWT;
 import com.sencha.gxt.core.client.IdentityValueProvider;
 import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
 import com.sencha.gxt.widget.core.client.grid.ColumnModel;
 
 /**
  * @author sriram
  *
  */
 public class DCColumnModel extends ColumnModel<DeployedComponent> {
 
     public DCColumnModel(DeployedComponentsListingView view) {
         super(buildColumnModel(view));
     }
 
     public static List<ColumnConfig<DeployedComponent, ?>> buildColumnModel(
             DeployedComponentsListingView view) {
         DCProperties properties = GWT.create(DCProperties.class);
         IdentityValueProvider<DeployedComponent> provider = new IdentityValueProvider<DeployedComponent>();
         List<ColumnConfig<DeployedComponent, ?>> configs = new LinkedList<ColumnConfig<DeployedComponent, ?>>();
 
         ColumnConfig<DeployedComponent, DeployedComponent> name = new ColumnConfig<DeployedComponent, DeployedComponent>(
                 provider, 100);
         name.setHeader(org.iplantc.core.uicommons.client.I18N.DISPLAY.name());
         configs.add(name);
         name.setCell(new DCNameHyperlinkCell(view));
         name.setMenuDisabled(true);
 
         ColumnConfig<DeployedComponent, String> version = new ColumnConfig<DeployedComponent, String>(
                 properties.version(), 100);
         version.setHeader(I18N.DISPLAY.version());
         configs.add(version);
         version.setMenuDisabled(true);
 
         ColumnConfig<DeployedComponent, String> path = new ColumnConfig<DeployedComponent, String>(
                 properties.location(), 100);
        path.setHeader();
         configs.add(path);
         path.setMenuDisabled(true);
 
         return configs;
 
     }
 }
