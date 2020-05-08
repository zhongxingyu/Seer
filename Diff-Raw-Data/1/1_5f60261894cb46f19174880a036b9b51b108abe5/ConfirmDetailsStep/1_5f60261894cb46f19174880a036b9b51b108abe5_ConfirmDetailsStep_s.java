 package au.org.scoutmaster.views.wizards.bulkSMS;
 
 import java.util.ArrayList;
 
 import org.vaadin.teemu.wizards.WizardStep;
 
 import au.com.vaadinutils.crud.MultiColumnFormLayout;
 import au.org.scoutmaster.application.SMSession;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.Phone;
 import au.org.scoutmaster.domain.access.User;
 import au.org.scoutmaster.util.SMNotification;
 import au.org.scoutmaster.util.VelocityFormatException;
 
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 public class ConfirmDetailsStep implements WizardStep
 {
 
 	private TextField subject;
 	private TextArea message;
 	private TextField from;
 	private TextField provider;
 	private BulkSMSWizardView messagingWizardView;
 	private VerticalLayout layout;
 	private Label recipientCount;
 	private MessageDetailsStep details;
 
 	public ConfirmDetailsStep(BulkSMSWizardView messagingWizardView)
 	{
 		this.messagingWizardView = messagingWizardView;
 		details = messagingWizardView.getDetails();
 
 		layout = new VerticalLayout();
 		layout.addComponent(new Label(
 				"Please review the details before clicking next as messages will be sent immediately."));
 		layout.setWidth("100%");
 
 		recipientCount = new Label();
 		recipientCount.setContentMode(ContentMode.HTML);
 		layout.addComponent(recipientCount);
 
 		MultiColumnFormLayout<Object> formLayout = new MultiColumnFormLayout<>(1, null);
 		provider = formLayout.bindTextField("Provider", "provider");
 		provider.setReadOnly(true);
 
 		from = formLayout.bindTextField("From", "from");
 		from.setReadOnly(true);
 
 		subject = formLayout.bindTextField("Subject", "subject");
 		subject.setSizeFull();
 		subject.setReadOnly(true);
 
 		message = formLayout.bindTextAreaField("Message", "message", 4);
 		message.setReadOnly(true);
 		message.setWidth("100%");
 
 		layout.addComponent(formLayout);
 		layout.setMargin(true);
 	}
 
 	@Override
 	public String getCaption()
 	{
 		return "Confim Details";
 	}
 
 	@Override
 	public Component getContent()
 	{
 
 		recipientCount.setValue("<p><b>" + messagingWizardView.getRecipientStep().getRecipientCount()
 				+ " recipients have been selected to recieve the following Email.</b></p>");
 
 		ArrayList<Contact> recipients = messagingWizardView.getRecipientStep().getRecipients();
 		Contact sampleContact = recipients.get(0);
 		User user = (User) SMSession.INSTANCE.getLoggedInUser();
 
 		try
 		{
 			provider.setReadOnly(false);
 			provider.setValue(details.getProvider().getProviderName());
 			provider.setReadOnly(true);
 			from.setReadOnly(false);
 			from.setValue(details.getFrom());
 			from.setReadOnly(true);
 			subject.setReadOnly(false);
 			subject.setValue(details.getSubject());
 			subject.setReadOnly(true);
 			message.setReadOnly(false);
 			message.setValue(details.getMessage().expandBody(user, sampleContact).toString());
 			message.setReadOnly(true);
 		}
 		catch (VelocityFormatException e)
 		{
 			SMNotification.show(e, Type.ERROR_MESSAGE);
 		}
 
 		return layout;
 	}
 
 	@Override
 	public boolean onAdvance()
 	{
 		boolean advance = this.subject.getValue() != null && this.message.getValue() != null;
 
 		if (!advance)
 			Notification.show("Please enter a Subject and a Message then click Next");
 		return advance;
 	}
 
 	@Override
 	public boolean onBack()
 	{
 		return true;
 	}
 
 	public Message getMessage()
 	{
 		return new Message(subject.getValue(), message.getValue(), new Phone(from.getValue()));
 	}
 
 }
