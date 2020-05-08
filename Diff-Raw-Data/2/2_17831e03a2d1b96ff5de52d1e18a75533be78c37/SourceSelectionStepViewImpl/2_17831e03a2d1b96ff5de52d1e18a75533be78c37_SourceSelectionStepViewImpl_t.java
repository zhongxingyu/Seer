 package org.cotrix.web.importwizard.client.step.sourceselection;
 
 import org.cotrix.web.importwizard.client.util.AlertDialog;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SourceSelectionStepViewImpl extends Composite implements SourceSelectionStepView {
 
	@UiTemplate("SourceSelectionStep.ui.xml")
 	interface SourceSelectionStepUiBinder extends UiBinder<Widget, SourceSelectionStepViewImpl> {}
 	private static SourceSelectionStepUiBinder uiBinder = GWT.create(SourceSelectionStepUiBinder.class);
 	
 	@UiField Button cloudButton;
 	@UiField Button localButton;
 	
 	private AlertDialog alertDialog;
 	private Presenter presenter;
 	
 	public SourceSelectionStepViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 	
 	@UiHandler("cloudButton")
 	public void onCloudButtonClicked(ClickEvent event){
 		presenter.onCloudButtonClick();
 	}
 	
 	@UiHandler("localButton")
 	public void onLocalButtonClicked(ClickEvent event){
 		presenter.onLocalButtonClick();
 	}
 	
 	public void alert(String message) {
 		if(alertDialog == null){
 			alertDialog = new AlertDialog();
 		}
 		alertDialog.setMessage(message);
 		alertDialog.show();
 	}
 }
