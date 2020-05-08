 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import org.iucn.sis.client.api.models.ClientUser;
 import org.iucn.sis.client.api.ui.users.panels.BrowseUsersWindow;
 import org.iucn.sis.client.api.ui.users.panels.UserSearchController;
 import org.iucn.sis.client.api.ui.users.panels.UserSearchController.SearchResults;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.portable.PortableAlphanumericComparator;
 
 public class SISCompleteListTextArea extends VerticalPanel {
 
 	public final boolean ENABLED = true;
 	public final boolean DISABLED = false;
 
 	protected TextArea textArea = null;
 	protected Button addButton = null;
 
 	protected BrowseUsersWindow browseUserWindow;
 
 	/**
 	 * Text of everything that was typed
 	 */
 	protected List<ClientUser> selectedUsers = null;
 	private String order = null;
 
 	protected String highlightStyle = null;
 
 	public SISCompleteListTextArea() {
 
 		textArea = new TextArea();
 		addButton = new Button("Add User");
 		selectedUsers = new ArrayList<ClientUser>();
 		browseUserWindow = new BrowseUsersWindow();
 		browseUserWindow.setSelectionListener(new ComplexListener<List<ClientUser>>() {
 			public void handleEvent(List<ClientUser> selectedUsers) {
 				SISCompleteListTextArea.this.selectedUsers = selectedUsers;
 				generateTextFromUsers();
 			}
 		});
 		highlightStyle = "redFont";
 
 		setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
 		build();
 	}
 
 	private void build() {
 
 		addButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				showMasterList();
 			}
 
 		});
 		addButton.addStyleName("pointerCursor");
 
 		setSpacing(4);
 
 		add(textArea);
 		textArea.setSize("100%", "100%");
 		add(addButton);
 	}
 
 	/**
 	 * Returns in csv form the list of items currently in the listbox.
 	 */
 
 	public void clearItemsInList() {
 		selectedUsers.clear();
 		textArea.setText("");
 	}
 
 	protected String generateTextFromUsers() {
 		Collections.sort(selectedUsers, new CreditedUserComparator(order));
 
 		StringBuilder text = new StringBuilder();
 		for (int i = 0; i < selectedUsers.size(); i++) {
 			text.append(selectedUsers.get(i).getCitationName());
 			
 			if (i + 1 < selectedUsers.size() - 1)
 				text.append(", ");
 
 			else if (i + 1 == selectedUsers.size() - 1)
 				text.append(" & ");
 		}
 		textArea.setText(text.toString());
 		return text.toString();
 	}
 
 	public List<ClientUser> getSelectedUsers() {
 		return selectedUsers;
 	}
 
 	public String getText() {
 		return textArea.getText();
 	}
 
 	public boolean hasOldText() {
 		return textArea.isEnabled();
 	}
 
 	public void setTextAreaEnabled(boolean enabled) {
 		textArea.setEnabled(enabled);
		addButton.setVisible(enabled);
 	}
 
 	protected void setUsers(List<ClientUser> users) {
 
 		selectedUsers = users;
 		generateTextFromUsers();
 	}
 
 	public void setUsersId(List<String> users, String order) {
 		this.order = order;
 		
 		if (users.size() > 0) {
 			HashMap<String, List<String>> map = new HashMap<String, List<String>>();
 			map.put("userid", users);
 			
 			UserSearchController.search(map, new GenericCallback<List<SearchResults>>() {
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Error", "An error occurred searching for users. " +
 						"Please check your Internet connection, then try again.");
 				}
 				public void onSuccess(List<SearchResults> results) {
 					List<ClientUser> users = new ArrayList<ClientUser>();
 					for (SearchResults result : results)
 						users.add(result.getUser());
 					setUsers(users);
 				};
 			});
 		} else {
 			setUsers(new ArrayList<ClientUser>());
 		}
 
 	}
 
 	public void setUserText(String text) {
 		textArea.setText(text);
 		selectedUsers.clear();
 	}
 
 	private void showMasterList() {
 		browseUserWindow.refresh(selectedUsers);
 		browseUserWindow.show();
 	}
 	
 	private static class CreditedUserComparator implements Comparator<ClientUser> {
 
 		private static final long serialVersionUID = 1L;
 		private final PortableAlphanumericComparator c = new PortableAlphanumericComparator(); 
 
 		private final List<Integer> order;
 
 		public CreditedUserComparator(String order) {
 			if (order == null || "".equals(order))
 				this.order = null;
 			else {
 				this.order = new ArrayList<Integer>();
 				for (String s : order.split(",")) {
 					try {
 						this.order.add(Integer.valueOf(s));
 					} catch (Exception how) { }
 				}
 			}
 		}
 
 		@Override
 		public int compare(ClientUser arg0, ClientUser arg1) {
 			if (order == null)
 				return sortByName(arg0, arg1);
 		
			int m1Index = arg0 == null ? -1 : order.indexOf(arg0.getId());
			int m2Index = arg1 == null ? -1 : order.indexOf(arg1.getId());
 		
 			if (m1Index == -1)
 				return 1;
 			else if (m2Index == -1)
 				return -1;
 			else
 				return Integer.valueOf(m1Index).compareTo(Integer.valueOf(m2Index));
 		}
 		
 		private int sortByName(ClientUser m1, ClientUser m2) {
 			for (String current : new String[]{ "lastname", "firstname" }) {
 				int value = c.compare(m1.getProperty(current), m2.getProperty(current));
 				if (value != 0)
 					return value;
 			}
 			return 0;
 		}
 	}
 
 }
