 package monbulk.MetadataEditor;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 import monbulk.client.desktop.Desktop;
 import monbulk.shared.Services.Metadata;
import monbulk.shared.Services.Metadata.DocumentElement;
 import monbulk.shared.widgets.Window.OkCancelWindow.*;
 
 public class DocumentElementPanel extends ElementPanel implements ValueChangeHandler<Boolean>
 {
 	private static DocumentElementPanelUiBinder uiBinder = GWT.create(DocumentElementPanelUiBinder.class);
 	interface DocumentElementPanelUiBinder extends UiBinder<Widget, DocumentElementPanel> { }
 
 	@UiField CheckBox m_isReference;
 	@UiField TextBox m_reference;
 	@UiField Button m_select;
 	@UiField TextBox m_referenceName;
 
 	public DocumentElementPanel()
 	{
 		initWidget(uiBinder.createAndBindUi(this));
 		m_isReference.addValueChangeHandler(this);
 		setButtonState();
 	}
 	
 	public void set(Metadata.Element element)
 	{
 		super.set(element);
 		
 		Metadata.DocumentElement docElement = (Metadata.DocumentElement)element;
 		
 		// NOTE: We only support document references at the moment!
 		boolean isReference = docElement.getIsReference() && docElement.getReferenceType() == Metadata.DocumentElement.ReferenceType.Document;
 		m_isReference.setValue(isReference);
 		String referenceValue = docElement.getReferenceValue();
 		if (referenceValue != null && referenceValue.length() > 0 && isReference)
 		{
 			m_reference.setValue(referenceValue);
 			m_referenceName.setValue(docElement.getReferenceName());
 		}
 		else
 		{
 			m_reference.setValue("");
 			m_referenceName.setValue("");
 		}
 		
 		setButtonState();
 	}
 	
 	public void update(Metadata.Element element)
 	{
 		Metadata.DocumentElement docElement = (Metadata.DocumentElement)element;
 		docElement.setIsReference(m_isReference.getValue());
 		docElement.setReferenceValue(m_reference.getValue());
 		docElement.setReferenceName(m_referenceName.getValue());
		docElement.setReferenceType(DocumentElement.ReferenceType.Document);
 	}
 	
 	public Metadata.ElementTypes getType()
 	{
 		return Metadata.ElementTypes.Document;
 	}
 	
 	public void onValueChange(ValueChangeEvent<Boolean> event)
 	{
 		setButtonState();
 	}
 	
 	private void setButtonState()
 	{
 		boolean isReference = m_isReference.getValue();
 		m_reference.setEnabled(isReference);
 		m_select.setEnabled(isReference);
 		m_referenceName.setEnabled(isReference);
 	}
 	
 	@UiHandler("m_select")
 	void onSelectClicked(ClickEvent event)
 	{
 		// Select was clicked, so show the metadata selection window.
 
 		Desktop d = Desktop.get();
 
 		final MetadataSelectWindow m = (MetadataSelectWindow)d.getWindow("MetadataSelectWindow");
 		m.setSelectedMetadata(m_reference.getValue());
 		m.setOkCancelHandler(new OkCancelHandler()
 		{
 			public void onOkCancelClicked(OkCancelHandler.Event eventType)
 			{
 				if (eventType == OkCancelHandler.Event.Ok)
 				{
 					// Ok was pressed so update the text box with the
 					// user's selection.
 					String metadata = m.getSelectedMetadataName();
 					m_reference.setValue(metadata);
 				}
 			}
 		});
 
 		d.show(m, true);
 	}
 }
