 /**
  * 
  */
 package org.cotrix.web.manage.client.codelists;
 
 import org.cotrix.web.common.client.util.DataUpdatedEvent;
 import org.cotrix.web.common.client.util.DataUpdatedEvent.DataUpdatedHandler;
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.manage.client.resources.CodelistsResources;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.shared.CodelistGroup;
 import org.cotrix.web.manage.shared.CodelistGroup.Version;
 
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
 			sb.appendEscaped(ValueUtils.getLocalPart(value.getName()));
 		}
 	};
 	
 	protected static final AbstractCell<Version> VERSION_CELL = new AbstractCell<Version>() {
 
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				Version value, SafeHtmlBuilder sb) {
 			SafeHtml html = VERSION_ITEM_TEMPLATE.version(CotrixManagerResources.INSTANCE.versionItem().getSafeUri(), CodelistsResources.INSTANCE.cellTreeStyle().versionItem(), SafeHtmlUtils.fromString(value.getVersion()));
 			sb.append(html);
 		}
 	};
 	
 	protected CodelistsDataProvider dataProvider;
 	protected SelectionModel<Version> selectionModel;
 
 	/**
 	 * @param dataProvider
 	 */
 	public CodelistTreeModel(CodelistsDataProvider dataProvider, SelectionModel<Version> selectionModel) {
 		this.dataProvider = dataProvider;
 		this.selectionModel = selectionModel;
 	}
 
 	@Override
 	public <T> NodeInfo<?> getNodeInfo(final T value) {
		Log.trace("getNodeInfo value: "+value);
 		if (value == null) {
 			return new DefaultNodeInfo<CodelistGroup>(dataProvider, GROUP_CELL);
 		}
 		
 		if (value instanceof CodelistGroup) {
 			final CodelistGroup group = (CodelistGroup) value;
 			
 			final ListDataProvider<Version> versionDataProvider = new ListDataProvider<Version>(group.getVersions());
 			dataProvider.addDataUpdatedHandler(new DataUpdatedHandler() {
 				
 				@Override
 				public void onDataUpdated(DataUpdatedEvent event) {
 					Log.trace("onDataUpdated "+value);
 					for (CodelistGroup newGroup:dataProvider.getCache()) {
 						if (newGroup.getName().equals(group.getName())) {
							versionDataProvider.setList(newGroup.getVersions());
 							versionDataProvider.refresh();
 						}
 					}
 				}
 			});
 			return new DefaultNodeInfo<Version>(versionDataProvider, VERSION_CELL, selectionModel, null);
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isLeaf(Object value) {
 		return value != null && !(value instanceof CodelistGroup);
 	}
 
 }
