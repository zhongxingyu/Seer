 package com.silverpeas.mobile.client.apps.documents.pages.widgets;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptException;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtmobile.ui.client.widgets.HorizontalPanel;
 import com.gwtmobile.ui.client.widgets.VerticalPanel;
 import com.silverpeas.mobile.client.apps.documents.resources.DocumentsMessages;
 import com.silverpeas.mobile.client.apps.documents.resources.DocumentsResources;
 import com.silverpeas.mobile.client.common.Notification;
 import com.silverpeas.mobile.client.common.mobil.MobilUtils;
 import com.silverpeas.mobile.client.common.phonegap.ChildBrowser;
 import com.silverpeas.mobile.client.resources.ApplicationMessages;
 import com.silverpeas.mobile.shared.dto.documents.AttachmentDTO;
 
 public class AttachmentWidget extends Composite implements ClickHandler {
 
 	private AttachmentDTO attachement;
 	
 	protected DocumentsMessages msg = null;
 	protected ApplicationMessages globalMsg = null;
 	protected DocumentsResources ressources = null;
 	
 	private HorizontalPanel grid = new HorizontalPanel();
 	private VerticalPanel internalGrid = new VerticalPanel();
 	private HorizontalPanel internalRow = new HorizontalPanel();	
 	private Label icon = new Label();
 	private Widget title;
 	private Label size = new Label();
 	private Label date = new Label();
 	
 	private boolean clicked = false;
 
 	public AttachmentWidget() {
 		
		if (MobilUtils.isIOS() && MobilUtils.isPhoneGap() == false && MobilUtils.isStandalone()) {
			title = new Anchor();	
		} else {
 			title = new Label();
 		}	
 		
 		ressources = GWT.create(DocumentsResources.class);		
 		ressources.css().ensureInjected();
 		msg = GWT.create(DocumentsMessages.class);
 		globalMsg = GWT.create(ApplicationMessages.class);
 		
 		grid.add(icon);		
 		grid.add(internalGrid);
 		grid.addDomHandler(this, ClickEvent.getType());	
 		internalGrid.add(title);
 		internalGrid.add(internalRow);
 		internalRow.add(size);
 		internalRow.add(new InlineHTML("&nbsp;-&nbsp;"));
 		internalRow.add(date);
 		initWidget(grid);
 	}
 
 	public String getTitle() {
 		if (title instanceof Anchor) {
 			return ((Anchor)title).getText();
 		} else {
 			return ((Label)title).getText();
 		}
 	}
 
 	public void setTitle(String text) {
 		if (title instanceof Anchor) {
 			((Anchor)title).setText(text);
 		} else {
 			((Label)title).setText(text);
 		}
 	}	
 	
 	public void setAttachment(AttachmentDTO attachmentDTO) {
 		this.attachement = attachmentDTO;
 		render();
 	}
 
 	private void render() {
 		DateTimeFormat fmt = DateTimeFormat.getFormat("dd MMMM yyyy");		
 		setTitle(attachement.getTitle());
 		
 		if (attachement.getSize() < 1024*1024) {
 			size.setText(attachement.getSize()/1024 + " Ko");	
 		} else {
 			size.setText(attachement.getSize()/(1024*1024) + " Mo");
 		}		
 		date.setText(fmt.format(attachement.getCreationDate()));		
 		if (attachement.getType().contains("msword")) {
 			icon.setStylePrimaryName(ressources.css().msword());
 		} else if (attachement.getType().contains("sheet")) {
 			icon.setStylePrimaryName(ressources.css().msexcel());
 		} else if (attachement.getType().contains("pdf")) {
 			icon.setStylePrimaryName(ressources.css().pdf());
 		} else {
 			icon.setStylePrimaryName(ressources.css().unknown());
 		}
 		
 	}
 
 	@Override
 	public void onClick(ClickEvent event) {		
 		if (!clicked) {
 			clicked = true;
 			clickAction();
 			Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
 				@Override
 				public boolean execute() {
 					clicked = false;
 					return false;
 				}}, 400);
 		}		
 	}
 
 	private void clickAction() {
 		try {
 			String url = Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/spmobile/spmobil/Attachment";					
 			url = url + "?id=" + attachement.getId() + "&instanceId=" + attachement.getInstanceId() + "&lang=" + attachement.getLang()  + "&userId=" + attachement.getUserId();
 			if (MobilUtils.isPhoneGap()) {
 				ChildBrowser.openExternal(url, false);				
 			} else {
 				if (title instanceof Anchor) {
 					((Anchor)title).setHref(url);
					((Anchor)title).setTarget("_blank");
 					((Anchor)title).fireEvent(new ClickEvent() {});	
 				} else {
 					Window.open(url, "_blank", "");	
 				}
 			}		
 			
 			//TODO : use Downloader
 			//Downloader.downloadFile(url);
 		} catch(JavaScriptException e) {
 			Notification.alert(e.getMessage(), null, "error", "ok");
 		}
 	}
 }
