 package org.cotrix.web.ingest.client.util;
 
 import java.util.List;
 
 import org.cotrix.web.ingest.shared.AttributeMapping;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class MappingPanel extends ResizeComposite {
 
 	@UiTemplate("MappingPanel.ui.xml")
 	interface HeaderTypeStepUiBinder extends UiBinder<Widget, MappingPanel> {}
 	private static HeaderTypeStepUiBinder uiBinder = GWT.create(HeaderTypeStepUiBinder.class);
 	
 	public interface MappingPanelHandler {
 		public void onReloadButtonClicked();
 		public void onPreviewButtonClicked();
 	}
 	
 	@UiField TextBox name;
 	@UiField TextBox version;
 	@UiField SimpleCheckBox sealed;
 	@UiField Label attributeMappingLabel;
 	
	@UiField PushButton previewButton;
 	
 	@UiField
 	ScrollPanel scrollMappingPanel;
 	
 	@UiField(provided=true)
 	AttributeMappingPanel mappingPanel;
 	
 	protected MappingPanelHandler mappingPanelHandler;
 
 	public MappingPanel(boolean hasTypeDefinition, String attributeMappingLabel) {
 		mappingPanel = new AttributeMappingPanel();
 		mappingPanel.setShowTypeDefinition(hasTypeDefinition);
 		initWidget(uiBinder.createAndBindUi(this));
 		this.attributeMappingLabel.setText(attributeMappingLabel);
 	}
 	
 	public void resetScroll() {
 		scrollMappingPanel.scrollToTop();
 	}
 
 	public void setMappingPanelHandler(MappingPanelHandler mappingPanelHandler) {
 		this.mappingPanelHandler = mappingPanelHandler;
 	}
 	
 	public void setPreviewVisible(boolean visible) {
		previewButton.setVisible(visible);
 	}
 
 	public void setName(String name) {
 		this.name.setValue(name);
 	}
 
 	public String getName() {
 		return this.name.getValue();
 	}
 
 	public void setVersion(String version) {
 		this.version.setValue(version);
 	}
 
 	public String getVersion() {
 		return this.version.getValue();
 	}
 	
 	public void setSealed(boolean sealed)
 	{
 		this.sealed.setValue(sealed);
 	}
 	
 	public boolean getSealed()
 	{
 		return sealed.getValue();
 	}
 	
 	@UiHandler("reloadButton")
 	protected void reload(ClickEvent clickEvent)
 	{
 		if (mappingPanelHandler!=null) mappingPanelHandler.onReloadButtonClicked();
 	}
 	
 	@UiHandler("previewButton")
 	protected void preview(ClickEvent clickEvent)
 	{
 		if (mappingPanelHandler!=null) mappingPanelHandler.onPreviewButtonClicked();
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
 }
