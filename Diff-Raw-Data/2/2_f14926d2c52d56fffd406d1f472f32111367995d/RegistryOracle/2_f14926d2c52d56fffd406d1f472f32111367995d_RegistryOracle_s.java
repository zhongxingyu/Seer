 package org.mule.galaxy.web.client.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.mule.galaxy.web.rpc.ItemInfo;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
 import com.extjs.gxt.ui.client.data.DataProxy;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.widget.form.ComboBox;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class RegistryOracle extends Oracle {
 
 	public RegistryOracle(RegistryServiceAsync svc) {
 		this(svc, "Artifact");
     }
 	
 	/**
 	 * Allows you to specify what types of things this oracle will show. If you
 	 * wish to show "Workspace"s, then it will do some magic to format things differently.
 	 * @param svc
 	 * @param type
 	 */
 	public RegistryOracle(RegistryServiceAsync svc, String type) {
 		ComboBox<ModelData> combo = new ComboBox<ModelData>();

		combo.setFieldLabel("test");
 		
 		boolean workspace = "Workspace".equals(type);
         String template = workspace ? getWorkspaceTemplate() : getTemplate();
 		
         initialize(getProxy(svc, combo, type), template, combo);
         
         if (workspace) {
             combo.setDisplayField("fullPath");
         }
     }
 
 	private static DataProxy getProxy(final RegistryServiceAsync svc, 
 								      final ComboBox<ModelData> combo,
 								      final String searchType) {
 		RpcProxy<PagingLoadResult<ModelData>> proxy = new RpcProxy<PagingLoadResult<ModelData>>() {
             @Override
             protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<ModelData>> callback) {
 
                 AsyncCallback<Collection<ItemInfo>> wrapper = new AsyncCallback<Collection<ItemInfo>>() {
 
                     public void onFailure(Throwable arg0) {
                         callback.onFailure(arg0);
                     }
 
                     public void onSuccess(Collection<ItemInfo> items) {
                         ArrayList<ModelData> models = new ArrayList<ModelData>();
                         for (ItemInfo i : items) {
                             BaseModelData data = new BaseModelData();
                             data.set("name", i.getName());
                             data.set("path", i.getParentPath());
                             data.set("fullPath", i.getParentPath() != null ? 
                                     i.getParentPath() + "/" + i.getName() : "/" + i.getName());
                             data.set("item", i);
                             models.add(data);
                         }
                         PagingLoadResult<ModelData> result = new BasePagingLoadResult<ModelData>(models);
                         callback.onSuccess(result);
                     }
 
                 };
 
                 String text = combo.getRawValue();
                 if (text == null)
                     text = "";
 
                 svc.suggestItems(text, false, "xxx", new String[]{searchType}, wrapper);
             }
         };
 
         return proxy; 
     }
 
 	private static native String getTemplate() /*-{
         return [
         '<tpl for="."><div style="padding: 3px; vertical-align: middle;" class="search-item">',
         '<strong>{name}</strong> in {path}',
         '</div></tpl>'
         ].join("");
       }-*/;
 
 	private static native String getWorkspaceTemplate() /*-{
         return [
         '<tpl for="."><div style="padding: 3px; vertical-align: middle;" class="search-item">',
         '<strong>{fullPath}</strong>',
         '</div></tpl>'
         ].join("");
       }-*/;
 
 }
