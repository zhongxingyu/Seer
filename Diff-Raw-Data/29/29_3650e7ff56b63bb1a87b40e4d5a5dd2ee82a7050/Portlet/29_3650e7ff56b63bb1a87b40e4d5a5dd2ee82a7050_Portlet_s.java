 /*
  * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,  
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.overlord.gadgets.web.client.widgets;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.overlord.gadgets.web.client.URLBuilder;
 import org.overlord.gadgets.web.client.model.JSOParser;
 import org.overlord.gadgets.web.client.util.RestfulInvoker;
 import org.overlord.gadgets.web.shared.dto.UserPreference;
 import org.overlord.gadgets.web.shared.dto.UserPreference.Option;
 import org.overlord.gadgets.web.shared.dto.UserPreference.UserPreferenceSetting;
 import org.overlord.gadgets.web.shared.dto.WidgetModel;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author: Jeff Yu
  * @date: 2/03/12
  */
 public class Portlet extends Composite {
 
     interface PortletUiBinder extends UiBinder<Widget, Portlet> {}
 
     private static PortletUiBinder uiBinder = GWT.create(PortletUiBinder.class);
 
     private static String urlBase;
 
     private String id;
 
     private String widgetId;
 
     //This is the portalLayout Id.
     private String portalId;
 
     private String iframeId;
 
     private WidgetModel wmodel;
 
     private String userPreferenceValues;
 
     private List<Widget> prefs = new ArrayList<Widget>();
 
     @UiField InlineLabel minBtn;
     @UiField InlineLabel title;
     @UiField InlineLabel removeBtn;
     @UiField InlineLabel maxBtn;
     @UiField InlineLabel settingBtn;
     @UiField InlineLabel restoreBtn;
     @UiField FlowPanel userPreference;
     @UiField FlowPanel portletContent;
     @UiField Frame gadgetSpec;
     @UiField FlexTable prefTable;
 
     private Portlet(final String wid, final String pid) {
         widgetId = wid;
         id = "portlet-" + widgetId;
         this.portalId = pid;
         this.iframeId = "iframe-" + widgetId;
         initWidget(uiBinder.createAndBindUi(this));
 
         getElement().setId(id);
         gadgetSpec.getElement().setId(iframeId);
         gadgetSpec.getElement().setAttribute("frameborder", "0");
 
         urlBase = getGadgetServerUrlBase();
 
         minBtn.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
                  toggle(id);
             }
         });
 
         removeBtn.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent clickEvent) {
             	if (Window.confirm("Are you sure to delete this widget?")) {
 	                String theURL =  URLBuilder.getRemoveWidgetURL(Long.valueOf(widgetId));
 	                RestfulInvoker.invoke(RequestBuilder.POST, theURL,
 	                        null, new RestfulInvoker.Response() {
 	                    	@Override
                             public void onResponseReceived(Request request, Response response) {
 	                    		remove(id);
 	                    }
 	                });
             	}
             }
         });
 
         maxBtn.addClickHandler(new ClickHandler(){
 			@Override
             public void onClick(ClickEvent event) {
 				maximizeWindow(id, portalId);
 				hideUserPref(id);
 				showRestoreButton(id);
 				gadgetSpec.setWidth("100%");
 				gadgetSpec.setHeight("90%");
 				gadgetSpec.getElement().setAttribute("scrolling", "auto");
 				gadgetSpec.setUrl(urlBase + "gadget-web/gadgets/ifr?url=" + wmodel.getSpecUrl() + "&" + getCanvasView() + "&" + userPreferenceValues);
 			}
         });
 
         restoreBtn.addClickHandler(new ClickHandler(){
 			@Override
             public void onClick(ClickEvent event) {
 				restoreWindow(id);
 				hideRestoreButton(id);
 				gadgetSpec.setWidth("100%");
 				gadgetSpec.setHeight("250px");
 				gadgetSpec.getElement().setAttribute("scrolling", "auto");
 				gadgetSpec.setUrl(urlBase + "gadget-web/gadgets/ifr?url=" + wmodel.getSpecUrl() + "&" + getHomeView() + "&" + userPreferenceValues);
 			}
         });
 
         settingBtn.addClickHandler(new ClickHandler(){
 			@Override
             public void onClick(ClickEvent event) {
 				showUserPref(id);
 			}
         });
 
         gadgetSpec.getElement().setId(widgetId);
     }
 
     public Portlet(WidgetModel model, final int width, String portalId) {
         this(String.valueOf(model.getWidgetId()), portalId);
         title.setText(model.getName());
         wmodel = model;
 
         RestfulInvoker.invoke(RequestBuilder.GET, URLBuilder.getPreferenceValuesURL(Long.valueOf(widgetId)), null,
         		new RestfulInvoker.Response() {
 
 					@Override
                     public void onResponseReceived(Request request, Response response) {
 						Map<String, String> defaultValues = JSOParser.getPreferenceValues(response.getText());
 				        generateUserPref(wmodel, defaultValues);
 				        generatePreferenceValuesString(defaultValues);
 				        gadgetSpec.getElement().setAttribute("scrolling", "auto");
 				        gadgetSpec.getElement().setAttribute("frameborder", "0");
 				        gadgetSpec.setWidth(width - 20 + "px");
 				        gadgetSpec.setHeight("250px");
 				        gadgetSpec.setUrl(urlBase + "gadget-web/gadgets/ifr?url=" + wmodel.getSpecUrl() + "&" + getHomeView() + "&" + userPreferenceValues);
 					}
 				});
 
     }
 
     private void generatePreferenceValuesString(Map<String, String> value) {
     	StringBuilder sbuilder = new StringBuilder();
     	int i = 1;
     	for (String key: value.keySet()) {
     		sbuilder.append(key).append("=").append(value.get(key));
     		if ( i < value.size()) {
     			sbuilder.append("&");
     		}
     		i++;
     	}
 
     	this.userPreferenceValues = sbuilder.toString();
     }
 
     private String getGadgetServerUrlBase() {
     	String gadgetWebUrlBase = GWT.getHostPageBaseURL();
     	int end = gadgetWebUrlBase.indexOf("gadget-web");
     	return gadgetWebUrlBase.substring(0, end);
     }
 
 
     private void generateUserPref(WidgetModel model, Map<String, String> values) {
     	UserPreference pref = model.getUserPreference();
     	int row = 0;
     	for (UserPreferenceSetting prefSet : pref.getData()) {
    		if (UserPreference.Type.STRING.equals(prefSet.getType())) {
    			prefTable.setWidget(row, 0, new Label(prefSet.getDisplayName()));
     			Widget textBox = createTextBox(prefSet.getName(), getDefaultValue(values, prefSet.getName(),prefSet.getDefaultValue()));
     			prefs.add(textBox);
     			prefTable.setWidget(row, 1, textBox);
     		} else if (UserPreference.Type.ENUM.equals(prefSet.getType())) {
    			prefTable.setWidget(row, 0, new Label(prefSet.getDisplayName()));
     			List<String> options = new ArrayList<String>();
     			for (Option option : prefSet.getEnumOptions()) {
     				options.add(option.getValue());
     			}
     			Widget listBox = createSelectBox(prefSet.getName(), getDefaultValue(values, prefSet.getName(),prefSet.getDefaultValue()), options);
     			prefs.add(listBox);
     			prefTable.setWidget(row, 1, listBox);
     		} else if (UserPreference.Type.LIST.equals(prefSet.getType())) {
    			prefTable.setWidget(row, 0, new Label(prefSet.getDisplayName()));
     			Widget listBox = createSelectBox(prefSet.getName(), getDefaultValue(values, prefSet.getName(),prefSet.getDefaultValue()), prefSet.getListOptions());
     			prefs.add(listBox);
     			prefTable.setWidget(row, 1, listBox);
     		}
     		row ++;
     	}
     	prefTable.setWidget(row, 1, createPrefSettingButtons());
 
     }
 
     private String getDefaultValue(Map<String, String> values, String name, String defaultValue) {
     	if (values.get(name) != null) {
     		return values.get(name);
     	}
     	return defaultValue;
     }
 
     private Widget createTextBox(String name, String defaultVal) {
     	TextBox textBox = new TextBox();
    	//hack, set the name to id as well for jquery retrieval.
    	textBox.getElement().setId(name);
     	textBox.setName(name);
     	textBox.setValue(defaultVal);
     	return textBox;
     }
 
     private Widget createSelectBox(String name, String defaultVal, List<String> options) {
     	ListBox listBox = new ListBox(false);
     	listBox.setName(name);
    	listBox.getElement().setId(name);
     	for (String option : options) {
     		listBox.addItem(option);
     	}
     	if (options.size() == 0) {
     		listBox.addItem(" ");
     	}
     	int index = 0;
     	for (int i = 0; i < options.size(); i ++) {
     		if (options.get(i).equals(defaultVal)) {
     			index = i;
     		}
     	}
     	listBox.setSelectedIndex(index);
     	return listBox;
     }
 
     private Widget createPrefSettingButtons() {
     	HorizontalPanel btnPanel = new HorizontalPanel();
     	Button saveBtn = new Button("Save");
     	saveBtn.getElement().setId("pref-save");
     	Button cancelBtn = new Button("Cancel");
     	btnPanel.add(saveBtn);
     	saveBtn.addClickHandler(new ClickHandler(){
 			@Override
             public void onClick(ClickEvent event) {
 				StringBuilder sbuffer = new StringBuilder();
 				sbuffer.append("{");
 
 				StringBuilder updatedValue = new StringBuilder();
 				for (int i = 0; i < prefs.size(); i++) {
 					Widget theWidget = prefs.get(i);
 					String name = null;
 					String value = null;
 					if (theWidget instanceof TextBox) {
 						TextBox tbox = (TextBox)theWidget;
 						name = tbox.getName();
 						value = tbox.getValue();
 					} else if (theWidget instanceof ListBox) {
 						ListBox lbox = (ListBox)theWidget;
 						name = lbox.getName();
 						value = lbox.getValue(lbox.getSelectedIndex());
 					}
 					if (name != null) {
 						sbuffer.append("\"" + name + "\":\"" + value + "\"");
 						updatedValue.append(name).append("=").append(value);
 						if (i < prefs.size() -1) {
 							sbuffer.append(",");
 							updatedValue.append("&");
 						}
 					}
 				}
 				sbuffer.append("}");
 				Log.debug("The map value is: " + sbuffer.toString());
 
 				userPreferenceValues = updatedValue.toString();
 				Log.debug("The userPreferenceValues is : " + userPreferenceValues );
 
 				RestfulInvoker.invoke(RequestBuilder.POST, URLBuilder.updatePreferenceURL(Long.valueOf(widgetId)), sbuffer.toString(), new RestfulInvoker.Response(){
 					@Override
                     public void onResponseReceived(Request request, Response response) {
 						hideUserPref(id);
 						String newUrl = urlBase + "gadget-web/gadgets/ifr?url=" + wmodel.getSpecUrl() + "&" + getHomeView() + "&" + userPreferenceValues;
 						//gadgetSpec.setUrl(newUrl);
 						Log.debug("the new url is: " + newUrl);
 						//reloadIFrame(gadgetSpec.getElement().getId());
 						reloadGadget(gadgetSpec.getElement().getId(), newUrl);
 					}
 
 				});
 
 			}
     	});
     	btnPanel.add(cancelBtn);
     	cancelBtn.addClickHandler(new ClickHandler(){
 			@Override
             public void onClick(ClickEvent event) {
 				hideUserPref(id);
 			}
     	});
     	return btnPanel;
     }
 
     public String getHomeView() {
     	return "view=home";
     }
 
     public String getCanvasView() {
     	return "view=canvas";
     }
 
     @Override
     public void onAttach() {
         super.onAttach();
         hideRestoreButton(id);
         hideUserPref(id);
     }
 
     /**
      * JSNI methods
      */
     private static native void toggle(String id) /*-{
         $wnd.$('#' + id).find(".portlet-min")
                 .toggleClass( "ui-icon-triangle-1-s" )
                 .toggleClass( "ui-icon-triangle-1-n" );
 
         $wnd.$('#' + id).find(".portlet-content").toggle();
     }-*/;
 
     private static native void remove(String id) /*-{
         $wnd.$('#' + id).remove();
     }-*/;
 
     private static native void showUserPreference(String id) /*-{
         $wnd.$('#' + id).find(".portlet-menu").show();
     }-*/;
 
     private static native void hideRestoreButton(String id) /*-{
     	$wnd.$('#'+id).find(".portlet-restore").hide();
     	$wnd.$('#'+id).find(".portlet-max").show();
     	$wnd.$('#'+id).find(".portlet-setting").show();
     	$wnd.$('#'+id).find(".portlet-close").show();
     }-*/;
 
     private static native void showRestoreButton(String id) /*-{
     	$wnd.$('#'+id).find(".portlet-restore").show();
     	$wnd.$('#'+id).find(".portlet-max").hide();
     	$wnd.$('#'+id).find(".portlet-setting").hide();
     	$wnd.$('#'+id).find(".portlet-close").hide();
     }-*/;
 
     private static native void maximizeWindow(String id, String portalId) /*-{
 	    var overlay = $wnd.$('<div></div>');
 	    var jqElm = $wnd.$('#' + portalId);
 	    var styleMap = {
 	        position: "absolute",
 	        height : jqElm.height(),
 	        width : jqElm.width(),
 	        'z-index': 10,
 	        opacity : 0.7,
 	        background : "#FFFFFF"
 	    };
 	    $wnd.$(overlay).css(styleMap);
 	    $wnd.$(overlay).addClass("added-overlay");
 	    jqElm.prepend(overlay[0]);
 		$wnd.$(".column").sortable( "option", "disabled", true);
 		$wnd.$('#' + id).removeClass("portlet").addClass("portlet-canvas");
 	}-*/;
 
     private static native void restoreWindow(String id) /*-{
 		$wnd.$(".added-overlay").remove();
 		$wnd.$(".column").sortable( "option", "disabled", false);
 		$wnd.$('#' + id).removeClass("portlet-canvas").addClass("portlet");
 	}-*/;
 
     private static native void hideUserPref(String id) /*-{
 		$wnd.$('#' + id).find(".portlet-preference").hide();
 	}-*/;
 
     private static native void showUserPref(String id) /*-{
 		$wnd.$('#' + id).find(".portlet-preference").show();
 	}-*/;
 
     private static native void reloadIFrame(String iframeId) /*-{
     	$wnd.document.getElementById(iframeId).reload(true);
     }-*/;
 
     private static native void reloadGadget(String iframeId, String url) /*-{
 		$wnd.reloadGadget(iframeId, url);
 	}-*/;
 
     private static native void setPreference(String name, String value) /*-{
 		var prefs = new gadgets.Prefs();
 		prefs.set(name, value);
 }-*/;
 
 }
