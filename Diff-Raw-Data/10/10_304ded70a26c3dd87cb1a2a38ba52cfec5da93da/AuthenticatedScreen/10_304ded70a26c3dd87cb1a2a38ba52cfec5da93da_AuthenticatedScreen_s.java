 package kg.cloud.uims;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.HierarchicalContainer;
 import com.vaadin.terminal.Sizeable;
 import com.vaadin.terminal.ThemeResource;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import kg.cloud.uims.i18n.UimsMessages;
 import kg.cloud.uims.ui.RegistrationView;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Tree;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.themes.Runo;
 
 public class AuthenticatedScreen extends VerticalLayout {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private MyVaadinApplication app;
 	private HorizontalSplitPanel horizontalPanel;
 	private GridLayout controlsLayout;
 	private VerticalLayout statusLayout;
 	private VerticalLayout navigationLayout;
 	private Tree navTree;
 
 	public AuthenticatedScreen(MyVaadinApplication app) {
 		super();
 		this.app = app;
 		horizontalPanel = new HorizontalSplitPanel();
 		horizontalPanel.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE);
 		horizontalPanel.setSizeFull();
 		horizontalPanel.setLocked(true);
 
 		controlsLayout = new GridLayout(1, 3);
 		controlsLayout.setSizeFull();
 
 		// controlsLayout.setStyleName("segment-alternate");
 
 		statusLayout = new VerticalLayout();
 		setSizeFull();
 		try {
 			app.workingDetails();
 		} catch (Exception ex) {
 			Logger.getLogger(AuthenticatedScreen.class.getName()).log(
 					Level.SEVERE, null, ex);
 		}
 
 		Subject currentUser = SecurityUtils.getSubject();
 		Label label = new Label("<b>Logged in as </b> <i>"
 				+ currentUser.getPrincipal().toString() + "</i><br/>"
 
 				+ "<b>Current Semester: </b><i>" + app.getCurrentSemester()
 				+ "</i><br/>" + "<b>Current Year: </b><i>"
 				+ app.getCurrentYear() + "</i>");
 		label.setContentMode(Label.CONTENT_XHTML);
 
 		Button admin = new Button("For administrators only");
 
 		Button user = new Button("For users only");
 		if (!currentUser.hasRole("admin")) {
 			admin.setEnabled(false);
 		} else if (!currentUser.hasRole("user")) {
 			user.setEnabled(false);
 		}
 
 		Button perm = new Button("For all with permission 'permission_2' only");
 		if (!currentUser.isPermitted("permission_2")) {
 			perm.setEnabled(false);
 		}
 
 		Button logout = new Button("logout");
 		logout.addListener(new MyVaadinApplication.LogoutListener(this.app));
 
 		statusLayout.addComponent(label);
 		statusLayout.addComponent(logout);
 
 		controlsLayout.addComponent(buildTree(), 0, 0);
 		controlsLayout.addComponent(user, 0, 1);
 		controlsLayout.addComponent(perm, 0, 2);
 
 		navigationLayout = new VerticalLayout();
 		navigationLayout.addComponent(statusLayout);
 		navigationLayout.addComponent(controlsLayout);
 		navigationLayout.setSpacing(true);
 		navigationLayout.setSizeFull();
 		horizontalPanel.setFirstComponent(navigationLayout);
 		this.addComponent(horizontalPanel);
 
 	}
 
 	public Tree buildTree() {
 
 		String[][] navigation = new String[][] { new String[] {
 				app.getMessage(UimsMessages.TBEduProcess),
 				app.getMessage(UimsMessages.TSBRegistration),
 				app.getMessage(UimsMessages.TSBMySubjects),
 				app.getMessage(UimsMessages.TSBMySuccess),
 				app.getMessage(UimsMessages.TSBMyTranscript) } };
 		Object propertyName = "name";
 		Object propertyIcon = "icon";
 		Item item = null;
 		int itemId = 0;
 
 		HierarchicalContainer navContainer = new HierarchicalContainer();
 		navContainer.addContainerProperty(propertyName, String.class, null);
		navContainer.addContainerProperty(propertyIcon, ThemeResource.class,
				Runo.class.getResource("document.png"));
 		for (int i = 0; i < navigation.length; i++) {
 			// Add new item
 			item = navContainer.addItem(itemId);
 			// Add name property for item
 			item.getItemProperty(propertyName).setValue(navigation[i][0]);
 			// Allow children
 			navContainer.setChildrenAllowed(itemId, true);
 			itemId++;
 			for (int j = 1; j < navigation[i].length; j++) {
 				if (j == 1) {
 					item.getItemProperty(propertyIcon).setValue(
 							new ThemeResource("folder.png"));
 				}
 				// Add child items
 				item = navContainer.addItem(itemId);
 				item.getItemProperty(propertyName).setValue(navigation[i][j]);
 				navContainer.setParent(itemId, itemId - j);
 				navContainer.setChildrenAllowed(itemId, false);
 
 				itemId++;
 			}
 		}
 		navTree = new Tree();
 		navTree.setContainerDataSource(navContainer);
 		navTree.setImmediate(true);
 
 		navTree.addListener(new ValueChangeListener() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			public void valueChange(ValueChangeEvent event) {
 				if (event.getProperty().getValue() != null) {
 					// If something is selected from the tree, get it's 'name'
 					// and
 					// insert it into the textfield
 					String eventPressed = navTree
 							.getItem(event.getProperty().getValue())
 							.getItemProperty("name").toString();
 
 					/*
 					 * String
 					 * eventPressed=navTree.getItem(event.getProperty().getValue
 					 * ()).getItemProperty("name").toString();
 					 */
 					if (eventPressed.equals(app
 							.getMessage(UimsMessages.TSBRegistration))) {
 						// getWindow().showNotification(eventPressed);
 						horizontalPanel
 								.setSecondComponent(new RegistrationView(app));
 
 					}
 				} else {
 					getWindow().showNotification(
 							"else"
 									+ navTree.getItem(
 											event.getProperty().getValue())
 											.getItemProperty("name"));
 				}
 
 			}
 		});
 		return navTree;
 
 	}
 
 }
