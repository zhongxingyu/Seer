 /**
  * 
  */
 package org.cotrix.web.permissionmanager.client.codelists.tree;
 
 import org.cotrix.web.permissionmanager.client.resources.CodelistsResources;
 import org.cotrix.web.permissionmanager.client.resources.PermissionsResources;
 import org.cotrix.web.permissionmanager.shared.CodelistGroup;
 import org.cotrix.web.permissionmanager.shared.CodelistGroup.CodelistVersion;
import org.cotrix.web.share.client.util.DataUpdatedEvent;
import org.cotrix.web.share.client.util.DataUpdatedEvent.DataUpdatedHandler;
 
import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.safehtml.shared.SafeUri;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.TreeViewModel;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodelistTreeModel implements TreeViewModel {
 	
 	public interface VersionItemTemplate extends SafeHtmlTemplates {
 	    @Template("<div><img src=\"{0}\" style=\"vertical-align: middle;\"><span class=\"{1}\">version {2}</span></div>")
 	    SafeHtml version(SafeUri image, String style, SafeHtml version);
 	  }
 	protected static final VersionItemTemplate VERSION_ITEM_TEMPLATE = GWT.create(VersionItemTemplate.class);
 	
 	protected static final AbstractCell<CodelistGroup> GROUP_CELL = new AbstractCell<CodelistGroup>() {
 
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				CodelistGroup value, SafeHtmlBuilder sb) {
 			sb.appendEscaped(value.getName());
 		}
 	};
 	
 	protected static final AbstractCell<CodelistVersion> VERSION_CELL = new AbstractCell<CodelistVersion>() {
 
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				CodelistVersion value, SafeHtmlBuilder sb) {
 			SafeHtml html = VERSION_ITEM_TEMPLATE.version(PermissionsResources.INSTANCE.versionItem().getSafeUri(), CodelistsResources.INSTANCE.cellTreeStyle().versionItem(), SafeHtmlUtils.fromString(value.getVersion()));
 			sb.append(html);
 		}
 	};
 	
 	protected CodelistGroupsDataProvider dataProvider;
 	protected SelectionModel<CodelistVersion> selectionModel;
 
 	/**
 	 * @param dataProvider
 	 */
 	public CodelistTreeModel(SelectionModel<CodelistVersion> selectionModel, CodelistGroupsDataProvider dataProvider) {
 		this.selectionModel = selectionModel;
 		this.dataProvider = dataProvider;
 	}
 
 	@Override
 	public <T> NodeInfo<?> getNodeInfo(final T value) {
 		if (value == null) {
 			return new DefaultNodeInfo<CodelistGroup>(dataProvider, GROUP_CELL);
 		}
 		
 		if (value instanceof CodelistGroup) {
			final CodelistGroup group = (CodelistGroup) value;
 			
 			final ListDataProvider<CodelistVersion> versiondaDataProvider = new ListDataProvider<CodelistVersion>(group.getVersions());
			dataProvider.addDataUpdatedHandler(new DataUpdatedHandler() {
 				
 				@Override
 				public void onDataUpdated(DataUpdatedEvent event) {
 					Log.trace("onDataUpdated "+value);
					for (CodelistGroup newGroup:dataProvider.getCache()) {
						if (newGroup.getName().equals(group.getName())) {
							versiondaDataProvider.getList().clear();
							versiondaDataProvider.getList().addAll(newGroup.getVersions());
						}
					}
 				}
			});
 			return new DefaultNodeInfo<CodelistVersion>(versiondaDataProvider, VERSION_CELL, selectionModel, null);
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isLeaf(Object value) {
 		return value != null && !(value instanceof CodelistGroup);
 	}
 
 }
