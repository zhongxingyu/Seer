 package burrito.client.crud.widgets;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import burrito.client.crud.widgets.SelectableTextArea.SelectedText;
 import burrito.client.widgets.messages.CommonMessages;
 import burrito.client.widgets.services.BBCodeService;
 import burrito.client.widgets.services.BBCodeServiceAsync;
 import burrito.client.widgets.validation.HasValidators;
 import burrito.client.widgets.validation.InputFieldValidator;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
 import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class BBCodeEditor extends VerticalPanel implements HasValidators {
 
 	public interface BBCodeEditorPlugin {
 		Button getButton();
 		void setTextArea(SelectableTextArea textArea);
 	}
 	
 	private static final List<BBCodeEditorPlugin> pluggedInButtons = new ArrayList<BBCodeEditorPlugin>();
 	
 	private TabPanel tabPanel = new TabPanel();
 	
 	private VerticalPanel rawPanel = new VerticalPanel();
 	private Frame preview = new Frame();
 	
 	private HorizontalPanel buttonPanel = new HorizontalPanel();
 	private SelectableTextArea rawEditor = new SelectableTextArea();
 	
 	private BBCodeServiceAsync bbCodeService = GWT.create(BBCodeService.class);
 
 	private boolean required;
 	private CommonMessages messages = GWT.create(CommonMessages.class);
 	
 	private Label errorMessage = new Label();
 	
 	public BBCodeEditor(String value, boolean required) {
 		this.required = required;
 		this.rawEditor.setText(value);
 	}
 	
 	public BBCodeEditor() {
 	}
 	
 	private void initButtons() {
		Button buttonBold = new Button("Bold 2");
 		buttonBold.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				SelectedText selectedText = rawEditor.getSelectedTextObj();
 				selectedText.text = "[b]" + selectedText.text + "[/b]";
 				rawEditor.setSelectedText(selectedText);
 			}
 		});
 		buttonPanel.add(buttonBold);
 		
 		Button buttonItalic = new Button("Italic");
 		buttonItalic.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				SelectedText selectedText = rawEditor.getSelectedTextObj();
 				selectedText.text = "[i]" + selectedText.text + "[/i]";
 				rawEditor.setSelectedText(selectedText);
 			}
 		});
 		buttonPanel.add(buttonItalic);
 		
 		Button buttonImg = new Button("Bild");
 		buttonImg.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				ImagePickerPopup imagePicker = new ImagePickerPopup(800, 800, false);
 				imagePicker.center();
 				imagePicker.show();
 				imagePicker.addSaveHandler(new ImagePickerPopup.SaveHandler() {
 					
 					public void saved(String value) {
 						SelectedText selectedText = rawEditor.getSelectedTextObj();
 						selectedText.text = "[img]" + value + "[/img]";
 						rawEditor.setSelectedText(selectedText);
 					}
 				});
 			}
 		});
 		buttonPanel.add(buttonImg);
 
 		Button buttonUrl = new Button("Url");
 		buttonUrl.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				BBUrlPanel urlPanel = new BBUrlPanel(rawEditor);
 				urlPanel.show();
 			}
 		});
 		buttonPanel.add(buttonUrl);
 		
 		Button buttonYoutube = new Button("Youtube");
 		buttonYoutube.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				 BBYoutubePanel youtubePanel = new BBYoutubePanel(rawEditor);
 				 youtubePanel.show();
 			}
 		});
 		buttonPanel.add(buttonYoutube);
 		
 		for (BBCodeEditorPlugin plugin : pluggedInButtons) {
 			plugin.setTextArea(rawEditor);
 			buttonPanel.add(plugin.getButton());
 		}
 		
 		errorMessage.setStyleName("validationError");
 		errorMessage.setVisible(false);
 		rawPanel.add(errorMessage);
 		rawPanel.add(buttonPanel);
 		rawPanel.add(rawEditor);
 		
 		preview.setSize("960px", "700px");
 		rawEditor.setSize("960px", "700px");
 		tabPanel.setSize("960px", "700px");
 		
 		tabPanel.add(rawPanel, "RAW");
 		tabPanel.add(preview, "Preview");
 		tabPanel.selectTab(0);
 		
 		tabPanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
 			@Override
 			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
 				
 				bbCodeService.generateBBCodePreview(rawEditor.getText(), new AsyncCallback<String>() {
 					@Override
 					public void onSuccess(String key) {
 						preview.setUrl("/burrito/bbCodePreview?key=" + key);
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("Could not create preview! " + caught.getMessage());
 					}
 				});
 			}
 		});
 		
 		add(tabPanel);
 	}
 	
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		initButtons();
 	}
 	
 	/**
 	 * Adds a plugin
 	 * 
 	 * @param plugin
 	 */
 	public static void addPlugin(BBCodeEditorPlugin plugin) {
 		pluggedInButtons.add(plugin);
 	}
 
 	public void setText(String value) {
 		this.rawEditor.setText(value);
 	}
 
 	public String getText() {
 		return this.rawEditor.getText();
 	}
 
 	@Override
 	public void addInputFieldValidator(InputFieldValidator validator) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public boolean validate() {
 		if (!required) {
 			return true;
 		}
 		
 		String text = this.getText();
 		if (text == null || "".equals(text.trim())) {
 			setValidationError(messages.validationRequiredField());
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void setValidationError(String validationMessage) {
 		if (validationMessage == null) {
 			errorMessage.setText(null);
 			errorMessage.setVisible(false);
 		} else {
 			errorMessage.setText(validationMessage);
 			errorMessage.setVisible(true);
 		}
 		
 	}
 }
