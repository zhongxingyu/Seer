 package pt.ist.bennu.vaadin.errorHandleing;
 
 import java.util.Collections;
 
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.domain.VirtualHost;
 import pt.ist.bennu.vaadin.domain.VaadinUtils;
 import pt.utl.ist.fenix.tools.smtp.EmailSender;
 
 import com.vaadin.Application;
 import com.vaadin.ui.AbstractOrderedLayout;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Window;
 
 public class TerminalErrorWindow extends Window implements ClickListener {
 
     private final com.vaadin.terminal.Terminal.ErrorEvent event;
 
     private TextField emailAddress = new TextField(VaadinUtils.getMessage("label.error.page.form.eamilAddress"));
     private TextField subject = new TextField(VaadinUtils.getMessage("label.error.page.form.subject"));
     private TextArea userDescription = new TextArea(VaadinUtils.getMessage("label.error.page.form.userDescription"));
 
     public TerminalErrorWindow(final com.vaadin.terminal.Terminal.ErrorEvent event) {
 	this.event = event;
 
 	setCaption(VaadinUtils.getMessage("label.error.page.title"));
 	addComponent(new Label(VaadinUtils.getMessage("label.error.page.form.caption")));
 	setModal(true);
 	setWidth(600, UNITS_PIXELS);
 	center();
 
 	final AbstractOrderedLayout layout = (AbstractOrderedLayout) getContent();
 	layout.setSpacing(true);
 	layout.setMargin(true);
     }
 
     @Override
     public void attach() {
         super.attach();
 
         final int columns = 45;
         final int rows = 20;
 
         emailAddress.setColumns(columns);
         final User user = UserView.getCurrentUser();
         emailAddress.setValue(getFromAddress(user));
         addComponent(emailAddress);
 
         subject.setColumns(columns);
         addComponent(subject);
 
         userDescription.setColumns(columns);
         userDescription.setRows(rows);
         addComponent(userDescription);
 
         final Button submit = new Button(VaadinUtils.getMessage("label.error.page.form.submit"));
         submit.addListener(this);
         addComponent(submit);
     }
 
     @Override
     public void buttonClick(final ClickEvent event) {
 	sendEmail();
 
 	final Application application = getApplication();
 	final Window mainWindow = application.getMainWindow();
 
 	getParent().removeWindow(this);
 
 	mainWindow.showNotification(VaadinUtils.getMessage(
 		"label.error.page.form.submit.successful"), Notification.TYPE_HUMANIZED_MESSAGE);
     }
 
     protected String getFromAddress(final User user) {
 	return "";
     }
 
     protected void sendEmail() {
 	final User user = UserView.getCurrentUser();
 
 	final String fromName = user.getPresentationName() + " (" + user.getUsername() + ")";
 	final String fromAddress = (String) emailAddress.getValue();
 
 	final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
 	final String supportEmailAddress = virtualHost.getSupportEmailAddress();
 
 	final String subject = "Error: " + this.subject.getValue();
 
 	final StringBuilder builder = new StringBuilder();
 	builder.append("User Comment: \n");
 	builder.append(userDescription.getValue());
 	builder.append("\n\n");
 
 	fillErrorInfo(builder);
 
 	EmailSender.send(fromName, fromAddress, null, Collections.singleton(supportEmailAddress),
 		null, null, subject, builder.toString());
 
     }
 
     private void fillErrorInfo(final StringBuilder builder) {
 	final Throwable cause = event.getThrowable();
 	fillErrorInfo(builder, cause);
     }
 
     private void fillErrorInfo(final StringBuilder builder, final Throwable cause) {
 	if (cause != null) {
 	    builder.append("Caused by: ");
 	    builder.append(cause.getClass().getName());
 	    builder.append("\n");
 	    builder.append("   message: ");
 	    builder.append(cause.getMessage());
 	    builder.append("\n");
 	    builder.append("   localized message: ");
 	    builder.append(cause.getLocalizedMessage());
 	    builder.append("\n");
	    builder.append("   stack\n");
 	    for (final StackTraceElement stackTraceElement : cause.getStackTrace()) {
 		builder.append(stackTraceElement.toString());
 		builder.append("\n");
 	    }
 	    builder.append("\n\n");
 	    fillErrorInfo(builder, cause.getCause());
 	}
     }
 
 }
