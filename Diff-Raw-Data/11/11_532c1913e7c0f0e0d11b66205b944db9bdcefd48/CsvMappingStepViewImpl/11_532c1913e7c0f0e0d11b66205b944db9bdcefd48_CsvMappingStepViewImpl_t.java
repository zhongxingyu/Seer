 package org.cotrix.web.importwizard.client.step.csvmapping;
 
 import java.util.List;
 
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.client.util.AlertDialog;
 import org.cotrix.web.importwizard.client.util.MappingPanel;
 import org.cotrix.web.importwizard.shared.AttributeMapping;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
public class CsvMappingStepViewImpl extends ResizeComposite implements CsvMappingStepView {
 
 	@UiTemplate("CsvMappingStep.ui.xml")
 	interface HeaderTypeStepUiBinder extends UiBinder<Widget, CsvMappingStepViewImpl> {}
 	private static HeaderTypeStepUiBinder uiBinder = GWT.create(HeaderTypeStepUiBinder.class);
 	
 	@UiField TextBox name;
 	
 	@UiField(provided=true)
 	MappingPanel mappingPanel;
 	
 	protected Presenter presenter;
 
 	protected AlertDialog alertDialog;
 
 	public CsvMappingStepViewImpl() {
 		mappingPanel = new MappingPanel(true);
 		initWidget(uiBinder.createAndBindUi(this));
 
 		Resources.INSTANCE.css().ensureInjected();
 	}
 	
 	/**
 	 * @param presenter the presenter to set
 	 */
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 	
 	@Override
 	public void setCsvName(String name) {
 		this.name.setValue(name);
 	}
 
 	@Override
 	public String getCsvName() {
 		return this.name.getValue();
 	}
 	
 	@UiHandler("reloadButton")
 	protected void reload(ClickEvent clickEvent)
 	{
 		presenter.onReload();
 	}
 	
 	public void setMappingLoading()
 	{
 		mappingPanel.setLoading();
 	}
 	
 	public void unsetMappingLoading()
 	{
 		mappingPanel.unsetLoading();
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	public void setMapping(List<AttributeMapping> mapping)
 	{
 		mappingPanel.setMapping(mapping);
 	}
 
 	public void setCodeTypeError()
 	{
 		mappingPanel.setCodeTypeError();
 	}
 
 	public void cleanStyle()
 	{
 		mappingPanel.cleanStyle();
 	}
 
 	public List<AttributeMapping> getMappings()
 	{
 		return mappingPanel.getMappings();
 	}
 	
 	public void alert(String message) {
 		if(alertDialog == null){
 			alertDialog = new AlertDialog(false);
 		}
 		alertDialog.setMessage(message);
 		alertDialog.show();
 	}
 
 }
