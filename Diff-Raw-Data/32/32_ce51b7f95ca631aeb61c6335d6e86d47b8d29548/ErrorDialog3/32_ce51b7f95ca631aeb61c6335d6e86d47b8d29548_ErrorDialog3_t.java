 package org.iplantc.core.uicommons.client.views.gxt3.dialogs;
 
import com.google.common.base.Strings;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.ContentPanel;
 import com.sencha.gxt.widget.core.client.button.TextButton;
 import com.sencha.gxt.widget.core.client.event.CollapseEvent;
 import com.sencha.gxt.widget.core.client.event.ExpandEvent;
 import com.sencha.gxt.widget.core.client.event.SelectEvent;
 import com.sencha.gxt.widget.core.client.form.TextArea;
 
 public class ErrorDialog3 extends IPlantDialog {
 
     private static ErrorDialog3UiBinder uiBinder = GWT.create(ErrorDialog3UiBinder.class);
 
     interface ErrorDialog3UiBinder extends UiBinder<Widget, ErrorDialog3> {
     }
 
     @UiField
     HTML errorMsg;
 
     @UiField
     TextButton expandDetailsButton;
 
     @UiField
     ContentPanel detailsPanel;
 
     @UiField
     TextArea descriptionArea;
 
     public ErrorDialog3(String errorMsg, String description) {
         super();

         this.setMinHeight(200);
         this.setMinWidth(350);
         this.setResizable(true);

         add(uiBinder.createAndBindUi(this));

        if (!Strings.isNullOrEmpty(errorMsg)) {
            this.errorMsg.setHTML(SafeHtmlUtils.fromString(errorMsg));
        }
         this.descriptionArea.setText(description);
         detailsPanel.setExpanded(false);
     }
 
     @Override
     protected void init() {
         super.init();
         setPredefinedButtons(PredefinedButton.OK);
     }
 
     @UiHandler("expandDetailsButton")
     void onExpandDetailsClick(SelectEvent event) {
         detailsPanel.setExpanded(!detailsPanel.isExpanded());
     }
 
     @UiHandler("detailsPanel")
     void onExpand(ExpandEvent event) {
     }
 
     @UiHandler("detailsPanel")
     void onCollapse(CollapseEvent event) {
 
     }
 
 }
