 package com.thingtrack.konekti.view.web.form.field;
 
 import java.io.Serializable;
 
 import org.vaadin.addon.customfield.CustomField;
 
 import com.thingtrack.konekti.view.addon.ui.UploadViewForm;
 import com.vaadin.data.Property;
 import com.vaadin.terminal.Resource;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Window.CloseEvent;
 import com.vaadin.ui.Window.CloseListener;
 
 @SuppressWarnings("serial")
 public class FileField extends CustomField {
 	
 	private VerticalLayout mainLayout;
 	private Button btnAttach;
 	
 	private byte[] file;
 	private String fileName;
 	
 	// navigator button listeners
 	private AttachmentChangeListener listenerChangeAttachment = null;
 		
 	public FileField() {
 		this(null);
 		
 	}
 	
 	@Override
     public void setIcon(Resource icon) {
 		btnAttach.setIcon(new ThemeResource("../konekti/images/icons/paper-clip.png"));
 		
         requestRepaint();
     }
     
 	public FileField(byte[] file) {
 		buildMainLayout();
 		setCompositionRoot(mainLayout);
 		
 		// TODO add user code here
 		this.file = file;
 		
 		btnAttach.addListener(new ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				final UploadViewForm uploadViewForm = new UploadViewForm(FileField.this.file);
 				
 				//uploadViewForm.setWidth("300px");
 				uploadViewForm.setWidth("-1px");
 				uploadViewForm.setHeight("-1px");
 				uploadViewForm.setClosable(false);
 				uploadViewForm.addListener(new CloseListener() {				
 					@Override
 					public void windowClose(CloseEvent event) {						
 						FileField.this.file = uploadViewForm.getFile();
 						fileName = uploadViewForm.getFileName();
 							
 						if (uploadViewForm.getFile() != null) 
 							btnAttach.setIcon(new ThemeResource("../konekti/images/icons/servicedesigner-module/tick.png"));						
 						else 							
							//btnAttach.setIcon(null);
							btnAttach.setIcon(new ThemeResource("../konekti/images/icons/paper-clip.png"));
 												
 						if (listenerChangeAttachment != null)
 							listenerChangeAttachment.attachmentChange(new AttachmentChangeEvent(event.getComponent(), FileField.this.file , fileName));
 						
 						//setValue(file);
 					}
 				});
 				
 				getApplication().getMainWindow().addWindow(uploadViewForm);
 				
 			}
 		});
 	}
 
 	@Override
 	public void setCaption(String caption) {
 		btnAttach.setCaption(caption);
 		
 	}
 	
 	@Override
 	public void setPropertyDataSource(Property newDataSource) {
 		file = (byte[])newDataSource.getValue();
 		
 		if (file != null)
 			btnAttach.setIcon(new ThemeResource("../konekti/images/icons/servicedesigner-module/tick.png"));
 		
 		super.setPropertyDataSource(newDataSource);
 	}
 	
 	@Override
 	public void setValue(Object file) {	
 		file = (byte[])file;
 		
 		if (file != null)
 			btnAttach.setIcon(new ThemeResource("../konekti/images/icons/servicedesigner-module/tick.png"));
 		
 		super.setValue(file);
 	}
 	
 	@Override
 	public Class<?> getType() {
 		return byte[].class;
 	}
 
 	@Override
 	public Object getValue() {	
 		return file;
 		
 	}
 	
 	public String getFileName() {
 		return this.fileName;
 		
 	}		
 	
 	public void addListener(AttachmentChangeListener listener) {
 		this.listenerChangeAttachment = listener;
 		
 	}
 	
 	public interface AttachmentChangeListener extends Serializable {
         public void attachmentChange(AttachmentChangeEvent event);
 
     }
 	
 	private VerticalLayout buildMainLayout() {
 		// common part: create layout
 		mainLayout = new VerticalLayout();
 		mainLayout.setImmediate(false);
 		mainLayout.setWidth("100%");
 		mainLayout.setHeight("-1px");
 		mainLayout.setMargin(false);
 		
 		// top-level component properties
 		setWidth("100.0%");
 		setHeight("-1px");
 		
 		// btnAttach
 		btnAttach = new Button();
 		btnAttach.setCaption("Adjuntar Fichero");
 		btnAttach.setImmediate(false);
 		btnAttach.setWidth("100.0%");
 		btnAttach.setHeight("-1px");
 		mainLayout.addComponent(btnAttach);
 		
 		return mainLayout;
 	}
 	
 	public class AttachmentChangeEvent extends Event {
 		private byte[] attachment;
 		private String attachmentName;
 
 		 public AttachmentChangeEvent(Component source) {
 	            super(source);
 	            
 	            this.attachment = null;
 	            this.attachmentName = null;
 	     }
 		 
 		public AttachmentChangeEvent(Component source, byte[] attachment, String attachmentName) {
 			super(source);
 			
 			this.attachmentName = attachmentName;
 			this.attachment = attachment;
 		}
 
 		public String getAttachmentName() {
 			return this.attachmentName;
 			
 		}
 		
 		public byte[] getAttachment() {
 			return this.attachment;
 			
 		}
 		
 	  }
 }
