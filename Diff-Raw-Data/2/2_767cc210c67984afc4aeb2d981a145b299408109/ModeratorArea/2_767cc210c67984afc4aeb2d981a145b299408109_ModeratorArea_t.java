 package org.smartsnip.client;
 
 import java.util.List;
 
 import org.smartsnip.shared.IAdministrator;
 import org.smartsnip.shared.IModerator;
 import org.smartsnip.shared.IUser;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.NotFoundException;
 import org.smartsnip.shared.XSession;
 import org.smartsnip.shared.XUser;
 import org.smartsnip.shared.XUser.UserState;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * This panel is for the moderator only and provides access to the moderator
  * functions
  * 
  * @author Felix Niederwanger
  * 
  */
 public class ModeratorArea extends Composite {
 	/** Maximum number of users per page */
 	private final int USERS_PER_PAGE = 50;
 
 	/** Selected page */
 	private enum Page {
 		sessions(1), user(2), categories(3);
 
 		/** Tabindex */
 		public final int tabindex;
 
 		private Page(int tabindex) {
 			this.tabindex = tabindex;
 		}
 
 		/** @return the tab index of the selected page */
 		public int getTabIndex() {
 			return tabindex;
 		}
 	}
 
 	/** Current selected page */
 	private Page currentPage = Page.sessions;
 
 	private final TabPanel tabPanel;
 	private final VerticalPanel pnlVertSessions;
 	private final Label lblSessions;
 
 	private final VerticalPanel pnlVertUsers;
 	private final Label lblUsers;
 	private final VerticalPanel verticalPanel;
 
 	private final Panel categoryPanel = new VerticalPanel();
 	private final CategoryTree categoryTree;
 
 	public ModeratorArea() {
 
 		tabPanel = new TabPanel();
 
 		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
 
 			@Override
 			public void onSelection(SelectionEvent<Integer> event) {
 				switch (event.getSelectedItem()) {
 				case 0: // Sessions
 					selectPage(Page.sessions);
 					break;
 				case 1: // Users
 					selectPage(Page.user);
 					break;
 				case 2: // Categories
 					selectPage(Page.categories);
 					break;
 				}
 			}
 		});
 
 		pnlVertSessions = new VerticalPanel();
 		lblSessions = new Label("Sessions: 0");
 		pnlVertSessions.add(lblSessions);
 		tabPanel.add(pnlVertSessions, "Sessions", false);
 
 		verticalPanel = new VerticalPanel();
 		pnlVertSessions.add(verticalPanel);
 		verticalPanel.setSize("100%", "100%");
 
 		pnlVertUsers = new VerticalPanel();
 		tabPanel.add(pnlVertUsers, "Users", false);
 		pnlVertUsers.setSize("5cm", "3cm");
 
 		lblUsers = new Label("Users: 0");
 		pnlVertUsers.add(lblUsers);
 
 		categoryTree = new CategoryTree();
 		categoryPanel.add(categoryTree);
 
 		tabPanel.add(categoryPanel, "Categories", false);
 
 		initWidget(tabPanel);
 		// Give the overall composite a style name.
 		setStyleName("moderatorArea");
 
 		selectPage(Page.sessions);
 	}
 
 	/**
 	 * Occurs when a page selection change happend
 	 * 
 	 * @param newPage
 	 *            new page to be displayed
 	 */
 	private void selectPage(Page newPage) {
 		currentPage = newPage;
 
 		switch (currentPage) {
 		case sessions:
 			updateSession();
 			break;
 		case user:
 			updateUsers();
 			break;
 		case categories:
 			updateCategories();
 			break;
 		}
 	}
 
 	/**
 	 * 
 	 * @return the currently selected page
 	 */
 	public Page getPage() {
 		return currentPage;
 	}
 
 	/**
 	 * Updates the components and the current selected page
 	 */
 	public void update() {
 		// XXX Ugly workaround
 		selectPage(currentPage);
 	}
 
 	/**
 	 * Updates the session page
 	 */
 	private void updateSession() {
 		lblSessions.setText("Updating ... ");
 		pnlVertSessions.clear();
 		pnlVertSessions.add(lblSessions);
 
 		IModerator.Util.getInstance().getSessions(new AsyncCallback<List<XSession>>() {
 
 			/** Guest session counter for createSessionPanel(XSession) */
 			int guest = 0;
 
 			@Override
 			public void onSuccess(final List<XSession> result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Returned null"));
 					return;
 				}
 
 				guest = 0;
 				int size = result.size();
 				lblSessions.setText((size == 1 ? "1 open session" : size + " open session"));
 				for (XSession session : result)
 					pnlVertSessions.add(createSessionPanel(session));
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught == null)
 					lblSessions.setText("Unknown error while updating");
 				else if (caught instanceof NoAccessException)
 					lblSessions.setText("Update failed: Access denied");
 				else
 					lblSessions.setText("Update failed: " + caught.getMessage());
 
 			}
 
 			/**
 			 * Creates a new session panel
 			 * 
 			 * @param session
 			 *            for the panel
 			 * @return the created session panel
 			 */
 			private Panel createSessionPanel(final XSession session) {
 				final HorizontalPanel result = new HorizontalPanel();
 				final VerticalPanel vertPanel = new VerticalPanel();
 				final HorizontalPanel infoPanel = new HorizontalPanel();
 				final String sessionString = session.user == null ? "Guest session #" + (++guest) : session.user;
 				final Label lblSession = new Label(sessionString);
 				final Label lblStatus = new Label("Status: Active");
 
 				final Button btnClose = new Button("Close");
 				btnClose.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						btnClose.setEnabled(false);
 						lblStatus.setText("Closing ... ");
 						IModerator.Util.getInstance().closeSession(session.key, new AsyncCallback<Void>() {
 
 							@Override
 							public void onSuccess(Void result) {
 								lblStatus.setText("Session closed");
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								if (caught == null)
 									lblStatus.setText("Closing failed");
 								else if (caught instanceof NoAccessException)
 									lblStatus.setText("Closing failed: Access denial");
 								else if (caught instanceof NotFoundException)
 									lblStatus.setText("Closing failed: Session not found");
 								else
 									lblStatus.setText("Closing failed: " + caught.getMessage());
 
 								btnClose.setText("Retry closing");
 								btnClose.setEnabled(true);
 							}
 						});
 					}
 				});
 
 				infoPanel.add(lblStatus);
 
 				vertPanel.add(lblSession);
 				vertPanel.add(infoPanel);
 
 				result.add(vertPanel);
 				result.add(btnClose);
 
 				return result;
 			}
 		});
 	}
 
 	/**
 	 * Updates the categories page
 	 */
 	private void updateCategories() {
 		categoryTree.update();
 	}
 
 	/**
 	 * Updates the users page
 	 */
 	private void updateUsers() {
 		lblUsers.setText("Updating ... ");
 		pnlVertUsers.clear();
 		pnlVertUsers.add(lblUsers);
 
 		IAdministrator.Util.getInstance().isAdministrator(new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onSuccess(Boolean result) {
 				updateForeignUsers(result);
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught == null)
 					lblUsers.setText("Error fetching admin status: Unknown error");
 				else if (caught instanceof NoAccessException)
 					lblUsers.setText("Error fetching admin status: Access denied");
 				else
 					lblUsers.setText("Error fetching admin status: " + caught.getMessage());
 			}
 		});
 	}
 
 	/**
 	 * Second call for {@link #updateUsers()} after the first async callback has
 	 * benn received
 	 * 
 	 * @param amIAdmin
 	 *            indicating if I am administrator
 	 */
 	private void updateForeignUsers(final boolean amIAdmin) {
 		IUser.Util.getInstance().getUsers(0, USERS_PER_PAGE, new AsyncCallback<List<XUser>>() {
 
 			@Override
 			public void onSuccess(List<XUser> result) {
 				if (result == null) {
 					onFailure(new IllegalArgumentException("Null returned"));
 					return;
 				}
 
 				int users = result.size();
 				final Grid grid = new Grid(users + 1, 7);
 
 				// add grid descriptions
 				final Label[] lblDescs = new Label[] { new Label("Username"), new Label("Real name"), new Label("Email"),
 						new Label("Last login"), new Label(""), new Label(""), new Label(""), };
 				for (int column = 0; column < lblDescs.length; column++)
 					grid.setWidget(0, column, lblDescs[column]);
 
 				int row = 1; // First row are descriptions. Do not overwrite
 								// them!
 				for (XUser user : result) {
 					// This is just for safety
 					if (user != null) {
 						Widget[] widgets = createUserControls(user);
 						for (int i = 0; i < 7; i++)
 							grid.setWidget(row, i, widgets[i]);
 						row++;
 					}
 				}
 
 				lblUsers.setText("Fetched " + users + " users");
 				pnlVertUsers.add(grid);
 			}
 
 			/**
 			 * Creates aan array, allows the control for each user The array is
 			 * suited for a 7-dim grid, that is in onSuccess used and defined.
 			 * These two methods are coupled!
 			 * 
 			 * @param user
 			 *            For witch the controls are created
 			 * @return component array
 			 */
 			private Widget[] createUserControls(final XUser user) {
 				if (user == null)
 					return null;
 
 				// Widgets:
 				// 0 - Username
 				// 1 - Realname
 				// 2 - Email
 				// 3 - Last login
 				// 4 - Delte Button
 				// 5 - Set State List
 				// 6 - Set Password button
 				final Label lblUsername = new Label(user.username);
 				final Label lblRealName = new Label(user.realname);
 				final Label lblEmail = new Label(user.email);
 				// TODO: Use DateFormat.format
 				final Label lblLastLogin;
 				if (user.isLoggedIn) {
 					String date = (user.lastLoginTime == null ? "???" : user.lastLoginTime.toString());
 					lblLastLogin = new Label("Logged in since " + date);
 				} else
 					lblLastLogin = new Label((user.lastLoginTime == null ? "Not yet logged in" : user.lastLoginTime.toString()));
 				final Button btnDelete = new Button("Delete");
 				final ListBox lstState = new ListBox();
 				fillListBoxWithUserStates(lstState);
 				final Button btnSetPassword = new Button("Set password");
				btnDelete.setVisible(amIAdmin);
 				btnSetPassword.setVisible(amIAdmin);
 
 				final Widget[] result = new Widget[7];
 				result[0] = lblUsername;
 				result[1] = lblRealName;
 				result[2] = lblEmail;
 				result[3] = lblLastLogin;
 				result[4] = btnDelete;
 				result[5] = lstState;
 				result[6] = btnSetPassword;
 
 				btnDelete.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						if (!amIAdmin) {
 							Control.myGUI.showMessagePopup("Access denied");
 							return;
 						}
 						if (Control.myGUI.showConfirmPopup("Do you really want do delete user " + user.username + "?") == true) {
 							btnDelete.setEnabled(false);
 							btnDelete.setText("Deleting ... ");
 							IAdministrator.Util.getInstance().deleteUser(user.username, new AsyncCallback<Void>() {
 
 								@Override
 								public void onFailure(Throwable caught) {
 									btnDelete.setText("Retry");
 									Control.myGUI.showErrorPopup("An error occured while deleting user " + user.username, caught);
 								}
 
 								@Override
 								public void onSuccess(Void result) {
 									// Update users all components will be
 									// erased
 									updateUsers();
 								}
 							});
 						}
 					}
 				});
 				btnSetPassword.addClickHandler(new ClickHandler() {
 
 					@Override
 					public void onClick(ClickEvent event) {
 						if (!amIAdmin) {
 							Control.myGUI.showMessagePopup("Access denied");
 							return;
 						}
 
 						// TODO Implement me
 					}
 				});
 				lstState.addChangeHandler(new ChangeHandler() {
 
 					@Override
 					public void onChange(ChangeEvent event) {
 						final UserState state = String2UserState(lstState.getItemText(lstState.getSelectedIndex()));
 						if (state == null)
 							return;
 
 						final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
 
 							@Override
 							public void onSuccess(Void result) {
 								// Perfect - The userstate is set
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								Control.myGUI.showErrorPopup("Error while setting the userstate of user " + user.username + " to "
 										+ UserState2String(state), caught);
 							}
 						};
 
 						if (amIAdmin)
 							IAdministrator.Util.getInstance().setUserState(user.username, state, callback);
 						else
 							IModerator.Util.getInstance().setUserState(user.username, state, callback);
 					}
 				});
 
 				return result;
 
 			}
 
 			/** Fills a listbox with user states */
 			private void fillListBoxWithUserStates(ListBox listbox) {
 				if (listbox == null)
 					return;
 				listbox.addItem(UserState2String(UserState.unvalidated)); // 0
 				listbox.addItem(UserState2String(UserState.validated)); // 1
 				listbox.addItem(UserState2String(UserState.moderator)); // 2
 				listbox.addItem(UserState2String(UserState.administrator)); // 3
 				listbox.addItem(UserState2String(UserState.deleted)); // 4
 			}
 
 			private String UserState2String(XUser.UserState state) {
 				switch (state) {
 				case administrator:
 					return "Administrator";
 				case deleted:
 					return "Deleted";
 				case moderator:
 					return "Moderator";
 				case unvalidated:
 					return "Unvalidated";
 				default:
 					return "User";
 				}
 			}
 
 			private UserState String2UserState(String string) {
 				if (string == null)
 					return null;
 				string = string.toLowerCase();
 				if (string.equals("administrator"))
 					return UserState.administrator;
 				else if (string.equals("moderator"))
 					return UserState.moderator;
 				else if (string.equals("unvalidated"))
 					return UserState.unvalidated;
 				else if (string.equals("user"))
 					return UserState.validated;
 				else if (string.equals("deleted"))
 					return UserState.deleted;
 				else
 					return null;
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught == null)
 					lblUsers.setText("Error fetching users: Unknown error");
 				else if (caught instanceof NoAccessException)
 					lblUsers.setText("Error fetching users: Access denied");
 				else
 					lblUsers.setText("Error fetching users: " + caught.getMessage());
 			}
 		});
 	}
 }
