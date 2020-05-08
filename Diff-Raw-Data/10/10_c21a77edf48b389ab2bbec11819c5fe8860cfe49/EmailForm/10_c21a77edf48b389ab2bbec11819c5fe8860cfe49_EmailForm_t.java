 package au.org.scoutmaster.forms;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.log4j.Logger;
 import org.vaadin.easyuploads.FileBuffer;
 import org.vaadin.easyuploads.MultiFileUpload;
 
 import au.com.vaadinutils.dao.EntityManagerProvider;
 import au.com.vaadinutils.fields.CKEditorEmailField;
 import au.com.vaadinutils.listener.ClickEventLogged;
 import au.com.vaadinutils.listener.CompleteListener;
 import au.com.vaadinutils.ui.WorkingDialog;
 import au.org.scoutmaster.dao.ActivityDao;
 import au.org.scoutmaster.dao.ActivityTypeDao;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.dao.SMTPSettingsDao;
import au.org.scoutmaster.dao.SMTransaction;
 import au.org.scoutmaster.domain.Activity;
 import au.org.scoutmaster.domain.ActivityType;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.SMTPServerSettings;
 import au.org.scoutmaster.domain.access.User;
 import au.org.scoutmaster.util.SMNotification;
 import au.org.scoutmaster.views.wizards.mailing.AttachedFile;
 
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.AbstractLayout;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 public class EmailForm extends VerticalLayout implements com.vaadin.ui.Button.ClickListener, CompleteListener
 {
 	private static Logger logger = Logger.getLogger(EmailForm.class);
 	private static final long serialVersionUID = 1L;
 	private static final String TEMP_FILE_DIR = new File(System.getProperty("java.io.tmpdir")).getPath();
 
 
 	private User sender;
 	private TextField toAddress;
 	private TextField subject;
 	private CKEditorEmailField ckEditor;
 	private Window owner;
 	private Button send;
 	private TextField ccAddress;
 	private Contact contact;
 	private VerticalLayout attachedFiles;
 	private HashSet<AttachedFile>fileList = new HashSet<>();
 
 	/**
 	 * 
 	 * @param sender
 	 *            the user who is sending this email.
 	 */
 	public EmailForm(Window owner, User sender, Contact contact, String toEmailAddress)
 	{
 		this.owner = owner;
 		this.sender = sender;
 		this.contact = contact;
 
 		this.setSpacing(true);
 		this.setMargin(true);
 		this.setSizeFull();
 
 		GridLayout grid = new GridLayout(3, 2);
 		grid.setWidth("100%");
 		grid.setColumnExpandRatio(1, (float) 1.0);
 		grid.setSpacing(true);
 
 		List<String> targetTypes = getTargetTypes();
 		ComboBox targetType = new ComboBox(null, targetTypes);
 		targetType.setWidth("50");
 		grid.addComponent(targetType);
 		targetType.select(targetTypes.get(0));
 		toAddress = new TextField();
 		toAddress.setInputPrompt("Enter email address");
 		toAddress.setValue(toEmailAddress);
 		toAddress.setWidth("100%");
 		grid.addComponent(toAddress);
 		grid.newLine();
 
 		// CC
 		ComboBox targetTypeCC = new ComboBox(null, targetTypes);
 		targetTypeCC.setWidth("50");
 		targetTypeCC.select(targetTypes.get(1));
 		grid.addComponent(targetTypeCC);
 
 		ccAddress = new TextField();
 		ccAddress.setInputPrompt("Enter email address");
 		ccAddress.setWidth("100%");
 		grid.addComponent(ccAddress);
 
 		send = new Button("Send", new ClickEventLogged.ClickAdaptor(this));
 		send.setImmediate(true);
 		grid.addComponent(send);
 
 		this.addComponent(grid);
 		subject = new TextField("Subject");
 		subject.setWidth("100%");
 
 		this.addComponent(subject);
 		ckEditor = new CKEditorEmailField(false);
 		this.addComponent(ckEditor);
 		this.setExpandRatio(ckEditor, 1.0f);
 		
 		HorizontalLayout uploadArea = new HorizontalLayout();
 		AbstractLayout uploadWidget = addUploadWidget();
 		uploadArea.addComponent(uploadWidget);
 		attachedFiles = new VerticalLayout();
 		Label attachedLabel = new Label("<b>Attached Files</b>");
 		attachedLabel.setContentMode(ContentMode.HTML);
 		attachedFiles.addComponent(attachedLabel);
 		uploadArea.addComponent(attachedFiles);
 		uploadArea.setWidth("100%");
 		uploadArea.setComponentAlignment(uploadWidget, Alignment.TOP_LEFT);
 		uploadArea.setComponentAlignment(attachedFiles, Alignment.TOP_RIGHT);
 
 		this.addComponent(uploadArea);
 
 
 		subject.focus();
 
 	}
 
 	List<String> getTargetTypes()
 	{
 		ArrayList<String> targetTypes = new ArrayList<>();
 
 		targetTypes.add("To");
 		targetTypes.add("CC");
 		targetTypes.add("BCC");
 		return targetTypes;
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event)
 	{
 
 		send.setEnabled(false);
 		if (isEmpty(this.subject.getValue()))
 			SMNotification.show("The subject may not be blank", Type.WARNING_MESSAGE);
 		else if (isEmpty(this.toAddress.getValue()) && isEmpty(this.ccAddress.getValue()))
 			SMNotification.show("You must provide at least one email address", Type.WARNING_MESSAGE);
 		else if (isEmpty(this.ckEditor.getValue()))
 			SMNotification.show("The body of the email may not be blank", Type.WARNING_MESSAGE);
 		else
 
 		{
 			WorkingDialog working = new WorkingDialog("Sending Email", "Sending...");
 			UI.getCurrent().addWindow(working);
 			working.setWorker(new Runnable()
 			{
 
 
 				@Override
 				public void run()
 				{
					EntityManager em = EntityManagerProvider.createEntityManager();
					try (SMTransaction t = new SMTransaction(em))
 					{
 
 						SMTPSettingsDao daoSMTPSettings = new DaoFactory(em).getSMTPSettingsDao();
 						SMTPServerSettings settings = daoSMTPSettings.findSettings();
 
 						daoSMTPSettings.sendEmail(settings, EmailForm.this.sender.getEmailAddress(),
 								EmailForm.this.toAddress.getValue(), EmailForm.this.ccAddress.getValue(),
 								EmailForm.this.subject.getValue(), ckEditor.getValue(), fileList);
 
 						// Log the activity
 						ActivityDao daoActivity = new DaoFactory(em).getActivityDao();
 						ActivityTypeDao daoActivityType = new DaoFactory(em).getActivityTypeDao();
 						ActivityType type = daoActivityType.findByName(ActivityType.EMAIL);
 						Activity activity = new Activity();
 						activity.setAddedBy(EmailForm.this.sender);
 						activity.setWithContact(EmailForm.this.contact);
 						activity.setSubject(EmailForm.this.subject.getValue());
 						activity.setDetails(EmailForm.this.ckEditor.getValue());
 						activity.setType(type);
 
 						daoActivity.persist(activity);
						t.commit();
						
 					}
 					catch (EmailException e)
 					{
 						logger.error(e, e);
 						EmailForm.this.send.setEnabled(true);
 						SMNotification.show(e, Type.ERROR_MESSAGE);
 					}
 
 				}
 			}, this);
 
 
 		}
 		this.send.setEnabled(true);
 
 	}
 
 	private boolean isEmpty(String value)
 	{
 		return value == null || value.length() == 0;
 	}
 
 	@Override
 	public void complete()
 	{
 		SMNotification.show("Message sent", Type.TRAY_NOTIFICATION);
 		// working.close();
 		this.owner.close();
 
 	}
 	
 	private AbstractLayout addUploadWidget()
 	{
 
 		MultiFileUpload multiFileUpload2 = new MultiFileUpload()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void handleFile(File file, String fileName, String mimeType, long length)
 			{
 				attachFile(file);
 			}
 
 			@Override
 			protected FileBuffer createReceiver()
 			{
 				FileBuffer receiver = super.createReceiver();
 				/*
 				 * Make receiver not to delete files after they have been
 				 * handled by #handleFile().
 				 */
 				receiver.setDeleteFiles(false);
 				return receiver;
 			}
 		};
 		multiFileUpload2.setCaption("Attach files");
 		multiFileUpload2.setRootDirectory(TEMP_FILE_DIR);
 		return multiFileUpload2;
 	}
 
 	
 	private void attachFile(File file)
 	{
 		HorizontalLayout line = new HorizontalLayout();
 		line.setSpacing(true);
 		Button removeButton = new Button("x");
 		
 		removeButton.setStyleName("small");
 		
 		line.addComponent(removeButton);
 		line.addComponent(new Label(file.getName()));
 		EmailForm.this.attachedFiles.addComponent(line);
 		
 		AttachedFile attachedFile = new AttachedFile(attachedFiles, file, line);
 		this.fileList.add(attachedFile);
 		removeButton.setData(attachedFile);
 		
 		removeButton.addClickListener(new ClickEventLogged.ClickListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void clicked(ClickEvent event)
 			{
 				AttachedFile file = (AttachedFile) event.getButton().getData();
 				file.remove();
 				EmailForm.this.fileList.remove(file);
 
 			}
 		});
 
 	}
 
 }
