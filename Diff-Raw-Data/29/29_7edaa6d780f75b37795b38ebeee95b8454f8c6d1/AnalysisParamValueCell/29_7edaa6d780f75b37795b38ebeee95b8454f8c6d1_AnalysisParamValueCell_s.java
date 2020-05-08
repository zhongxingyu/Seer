 /**
  *
  */
 package org.iplantc.de.client.analysis.views.cells;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.resources.client.messages.I18N;
 import org.iplantc.core.uiapps.widgets.client.models.ArgumentType;
 import org.iplantc.core.uicommons.client.ErrorHandler;
 import org.iplantc.core.uicommons.client.events.EventBus;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.diskresources.File;
import org.iplantc.core.uicommons.client.util.DiskResourceUtil;
 import org.iplantc.core.uidiskresource.client.events.ShowFilePreviewEvent;
 import org.iplantc.de.client.Services;
 import org.iplantc.de.client.analysis.models.AnalysisParameter;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 
 /**
  * @author sriram
  * 
  */
 public class AnalysisParamValueCell extends AbstractCell<AnalysisParameter> {
 
     public AnalysisParamValueCell() {
         super("click");
     }
 
     @Override
     public void render(com.google.gwt.cell.client.Cell.Context context, AnalysisParameter value,
             SafeHtmlBuilder sb) {
         String info_type = value.getInfoType();
         // // At present,reference genome info types are not supported by DE
         // viewers
         boolean valid_info_type = isValidInputType(info_type);
         if (value.getType().equals(ArgumentType.Input) && valid_info_type) {
             sb.appendHtmlConstant("<div style=\"cursor:pointer;text-decoration:underline;white-space:pre-wrap;\">"
                     + value.getDisplayValue() + "</div>");
         } else {
             sb.appendHtmlConstant("<div style=\"white-space:pre-wrap;\">" + value.getDisplayValue()
                     + "</div>");
         }
 
     }
 
     @Override
     public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent,
             AnalysisParameter value, NativeEvent event, ValueUpdater<AnalysisParameter> valueUpdater) {
 
         if (value == null) {
             return;
         }
 
         // Call the super handler, which handlers the enter key.
         super.onBrowserEvent(context, parent, value, event, valueUpdater);
 
         if ("click".equals(event.getType())) {
             String info_type = value.getInfoType();
             boolean valid_info_type = isValidInputType(info_type);
             if (value.getType().equals(ArgumentType.Input) && valid_info_type) {
                 launchViewer(value);
 
             }
         }
 
     }
 
     @SuppressWarnings("deprecation")
    private void launchViewer(AnalysisParameter value) {
        DiskResourceAutoBeanFactory factory = GWT.create(DiskResourceAutoBeanFactory.class);
        AutoBean<File> bean = AutoBeanCodex.decode(factory, File.class, "{}");
        final File file = bean.as();
        file.setId(value.getDisplayValue());
        file.setName(DiskResourceUtil.parseNameFromPath(value.getDisplayValue()));
         JSONObject obj = new JSONObject();
         JSONArray arr = new JSONArray();
        arr.set(0, new JSONString(file.getId()));
         obj.put("paths", arr);
         Services.DISK_RESOURCE_SERVICE.getStat(obj.toString(), new AsyncCallback<String>() {
 
             @Override
             public void onSuccess(String result) {
                JSONObject json = JsonUtil.getObject(result);
                JSONObject pathsObj = JsonUtil.getObject(json, "paths");
                JSONObject manifest = JsonUtil.getObject(pathsObj, file.getId());
                file.setSize(JsonUtil.getNumber(manifest, "file-size").longValue());
                EventBus.getInstance().fireEvent(new ShowFilePreviewEvent(file, this));
 
             }
 
             @Override
             public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.ERROR.diskResourceDoesNotExist(file.getId()));
             }
         });
 
     }
 
     public boolean isValidInputType(String info_type) {
         return !info_type.equalsIgnoreCase("ReferenceGenome")
                 && !info_type.equalsIgnoreCase("ReferenceSequence")
                 && !info_type.equalsIgnoreCase("ReferenceAnnotation");
     }
 }
