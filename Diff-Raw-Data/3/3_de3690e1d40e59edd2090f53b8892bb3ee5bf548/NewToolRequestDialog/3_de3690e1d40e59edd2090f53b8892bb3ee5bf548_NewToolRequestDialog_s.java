 /**
  * 
  */
 package org.iplantc.core.uiapps.integration.client.dialogs;
 
 import org.iplantc.core.uiapps.integration.client.I18N;
 import org.iplantc.core.uiapps.integration.client.presenter.NewToolRequestFormPresenterImpl;
import org.iplantc.core.uiapps.integration.client.view.AppsIntegrationView.Presenter;
 import org.iplantc.core.uiapps.integration.client.view.NewToolRequestFormView;
 import org.iplantc.core.uiapps.integration.client.view.NewToolRequestFormViewImpl;
 
 import com.sencha.gxt.widget.core.client.Dialog;
 
 /**
  * @author sriram
  *
  */
 public class NewToolRequestDialog extends Dialog {
 
     public NewToolRequestDialog() {
         setHeadingText(I18N.DISPLAY.requestNewTool());
         setPixelSize(500, 400);
         this.setResizable(false);
         NewToolRequestFormView view = new NewToolRequestFormViewImpl();
         Presenter p = new NewToolRequestFormPresenterImpl(view);
         p.go(this);
     }
 
 }
