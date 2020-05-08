 package ch.bfh.bti7081.s2013.yellow.ui.notification;
 
 import java.io.File;
 import java.sql.Timestamp;
 
 import ch.bfh.bti7081.s2013.yellow.model.notification.Notification;
 import ch.bfh.bti7081.s2013.yellow.service.notification.NotificationService;
 import ch.bfh.bti7081.s2013.yellow.util.SpringHelper;
 import com.vaadin.annotations.Title;
 
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener;
 import com.vaadin.server.ExternalResource;
 import com.vaadin.server.FileResource;
 import com.vaadin.server.VaadinService;
 import com.vaadin.server.VaadinServlet;
 import com.vaadin.ui.*;
 import com.vaadin.ui.themes.Reindeer;
 
 /* 
  * @author glisb1, bronc1
  * List of all notifications
  * 
 */
 @Title("Notification-List")
 public class NotificationListView extends CustomComponent implements View {
 	NotificationService notificationService;
 
 	public static final String NAME = "notificationList";
 	public static final int VISIBLE_ROWS = 10;
 
 	public NotificationListView() {
 		// Vaadin & Spring stuff
 		SpringHelper springHelper = new SpringHelper(VaadinServlet.getCurrent().getServletContext());
 		notificationService = (NotificationService) springHelper.getBean("notificationService");
 
 		// Find the application directory
 		String basepath = VaadinService.getCurrent()
 				.getBaseDirectory().getAbsolutePath();
 
 		// Images as a file resource
 		FileResource resourceBack = new FileResource(new File(basepath +
 				"/WEB-INF/images/back.png"));
 		
 		//Table with its columns, you have to give the correct object for every column!
 		Table ntfyTable = new Table("");
 		ntfyTable.addContainerProperty("id", Integer.class, null);
 		ntfyTable.addContainerProperty("Receiver", String.class, null);
 		ntfyTable.addContainerProperty("Message", String.class, null);
		ntfyTable.addContainerProperty("Date created", Timestamp.class, null);
		ntfyTable.addContainerProperty("Version", Integer.class, null);
 
 		
 		//Set the number of visible rows, if more, scrollbar will appear
 		ntfyTable.setPageLength(VISIBLE_ROWS);
 
 		//For every notification, there's a row in the table
 		for (Notification notification : notificationService.findAll()) {
 		
 			//Reads the data from the notification and fills it in the rows
 			ntfyTable.addItem(new Object[]{
 					notification.getId(),
 					notification.getReceiver().getEmail(),
 					notification.getMessage(),
					notification.getCreation(),
					notification.getVersion()
 			}, notification.getId());
 		}
 
 		
 		// Back to homepage-Link
 		Link linkBack = new Link(null,
 				new ExternalResource("#!"));
 		linkBack.setIcon(resourceBack);
 		linkBack.setCaption(" Back to homepage");
 		
 		// The view root layout
 		setSizeFull();
 		VerticalLayout viewLayout = new VerticalLayout(ntfyTable);
 		VerticalLayout btnLayout = new VerticalLayout();
 		viewLayout.setSizeFull();
 		viewLayout.setComponentAlignment(ntfyTable, Alignment.MIDDLE_CENTER);
 		btnLayout.addComponent(linkBack);
 		btnLayout.setComponentAlignment(linkBack, Alignment.MIDDLE_CENTER);
 		viewLayout.addComponent(btnLayout);
 		viewLayout.setComponentAlignment(btnLayout, Alignment.MIDDLE_CENTER);
 		viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
 		setCompositionRoot(viewLayout);
 	}
 
 	@Override
 	public void enter(ViewChangeListener.ViewChangeEvent event) {
 	}
 }
