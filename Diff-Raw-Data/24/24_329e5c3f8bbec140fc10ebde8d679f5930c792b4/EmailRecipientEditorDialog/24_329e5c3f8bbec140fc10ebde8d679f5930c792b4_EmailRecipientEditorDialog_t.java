 package net.brainvitamins.timeout.client.editors;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.brainvitamins.timeout.shared.EmailRecipient;
 import net.brainvitamins.timeout.shared.services.RecipientService;
 import net.brainvitamins.timeout.shared.services.RecipientServiceAsync;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class EmailRecipientEditorDialog extends DialogBox
 {
 	public enum Mode
 	{
 		ADD, EDIT
 	}
 
 	interface EmailRecipientViewUiBinder extends
 			UiBinder<Widget, EmailRecipientEditorDialog>
 	{
 	}
 
 	private static EmailRecipientViewUiBinder uiBinder = GWT
 			.create(EmailRecipientViewUiBinder.class);
 
 	private RecipientServiceAsync recipientService = GWT
 			.create(RecipientService.class);
 
 	private static Image loadingImage = new Image("loading.gif");
 
 	private EmailRecipient original;
 
 	private final Mode mode;
 
 	private static Logger logger = Logger.getLogger("EditorDialog");
 
 	@UiField(provided = true)
 	EmailRecipientEditor editor;
 
 	@UiField
 	Button okButton;
 
 	@UiField
 	Label errorLabel;
 
 	public EmailRecipientEditorDialog(EmailRecipient recipient, Mode mode)
 	{
 		editor = new EmailRecipientEditor(recipient);
 
 		// it really sucks that we have to do this here...
 		editor.nameEditor.setText(recipient.getName());
 		editor.addressEditor.setText(recipient.getAddress());
 		editor.nameEditor.setFocus(true);
 
 		this.mode = mode;
 
 		if (mode == Mode.EDIT) this.original = recipient;
 
 		setWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@UiHandler("okButton")
 	void save(ClickEvent event)
 	{
 		if (editor.hasErrors())
 		{
 			showError("Please correct form errors.");
 		}
 		else
 		{
 			errorLabel.setText("");
 			errorLabel.setVisible(false);
 
 			lockInput();
 
 			EmailRecipient result = editor.getCurrentValue();
 
 			logger.log(Level.INFO, "Edit result: " + result);
 			if (mode == Mode.ADD)
 			{
 				recipientService.addRecipient(result, serverHandler);
 			}
 			else if (!(original.equals(result)))
 			{
 				recipientService.updateRecipient(result, serverHandler);
 			}
 		}
 	}
 
 	@UiHandler("cancelButton")
 	void cancel(ClickEvent event)
 	{
 		this.hide();
 	}
 
 	private final EmailRecipientEditorDialog currentDialog = this;
 
 	private AsyncCallback<Void> serverHandler = new AsyncCallback<Void>()
 	{
 		@Override
 		public void onSuccess(Void result)
 		{
 			currentDialog.hide();
 		}
 
 		@Override
 		public void onFailure(Throwable caught)
 		{
 			if (caught instanceof IllegalArgumentException)
 				showError("Message from server: " + caught.getMessage());
 			else
 				showError("Server error while adding recipient.\nPlease contact the server admin.");
 
 			unlockInput();
 		}
 	};
 
 	private void lockInput()
 	{
 		editor.lockInput();
 		okButton.setEnabled(false);
 
 		okButton.setText("");
 		okButton.getElement().appendChild(loadingImage.getElement());
 	}
 
 	private void unlockInput()
 	{
 		editor.unlockInput();
 		okButton.setEnabled(true);
 
 		okButton.setText("OK");
 	}
 
 	private void showError(String error)
 	{
 		errorLabel.setText(error);
 		errorLabel.setVisible(true);
 	}
 }
