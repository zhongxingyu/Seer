 package org.smartsnip.client;
 
 import org.smartsnip.shared.IUser;
 import org.smartsnip.shared.NoAccessException;
 import org.smartsnip.shared.XUser;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Static object to handle GUI interactions with the {@link IUser} interface
  * 
  * @author Felix Niederwanger
  * 
  */
 public class User {
 
 	private static class ChangePasswordPopup extends PopupPanel {
 		/**
 		 * Username for whoom the password should be changed, or null, if for
 		 * current session
 		 */
 		private final String username;
 
 		/* Controls */
 		private final Panel rootPanel = new VerticalPanel();
 
 		private final Label lblTitle;
 
 		private final Grid grid;
 		private final Label lblOldPassword;
 		private final Label lblNewPassword;
 		private final Label lblConfirm;
 		private final PasswordTextBox passOldPassword;
 		private final PasswordTextBox passNewPassword;
 		private final PasswordTextBox passConfirm;
 
 		private final HorizontalPanel pnlToolbar;
 		private final Button btnApply;
 		private final Button btnClose;
 
 		private final Label lblStatus;
 
 		/* End of controls */
 
 		/**
 		 * Creates a new popup panel for changeing password for the current user
 		 */
 		private ChangePasswordPopup() {
 			this(null);
 		}
 
 		/**
 		 * Creates a new popup panel for changeing password for the given user
 		 * 
 		 * @param username
 		 *            user the password should be changed from
 		 */
 		private ChangePasswordPopup(String username) {
 			this.username = username;
 
 			lblTitle = new Label("Change password");
 			pnlToolbar = new HorizontalPanel();
 			btnApply = new Button("Apply");
 			btnClose = new Button("Close");
 			grid = new Grid(3, 2);
 			lblStatus = new Label();
 
 			lblOldPassword = new Label("Enter current password");
 			lblNewPassword = new Label("Enter new password");
 			lblConfirm = new Label("Confirm new password");
 			passOldPassword = new PasswordTextBox();
 			passNewPassword = new PasswordTextBox();
 			passConfirm = new PasswordTextBox();
 
 			btnApply.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					apply();
 				}
 			});
 			btnClose.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					close();
 				}
 			});
 
 			/* Handles enter as apply for all text fields */
 			final KeyDownHandler enterHandler = new KeyDownHandler() {
 				@Override
 				public void onKeyDown(KeyDownEvent event) {
 					if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
 						apply();
 					}
 				}
 			};
 			passOldPassword.addKeyDownHandler(enterHandler);
 			passNewPassword.addKeyDownHandler(enterHandler);
 			passConfirm.addKeyDownHandler(enterHandler);
 
 			grid.setWidget(0, 0, lblOldPassword);
 			grid.setWidget(1, 0, lblNewPassword);
 			grid.setWidget(2, 0, lblConfirm);
 			grid.setWidget(0, 1, passOldPassword);
 			grid.setWidget(1, 1, passNewPassword);
 			grid.setWidget(2, 1, passConfirm);
 
 			pnlToolbar.add(btnApply);
 			pnlToolbar.add(btnClose);
 
 			rootPanel.add(lblTitle);
 			rootPanel.add(grid);
 			rootPanel.add(pnlToolbar);
 			rootPanel.add(lblStatus);
 
 			this.setWidget(rootPanel);
 			lblTitle.setStyleName("h3"); // Yeah, out of order, but working ;-)
 			applyStyles(this);
 		}
 
 		/**
 		 * Shows the popup
 		 */
 		@Override
 		public void show() {
 			super.show();
 		}
 
 		/**
 		 * Closes the popup
 		 */
 		public void close() {
 			super.hide();
 		}
 
 		/** Internal call - applys the values */
 		private void apply() {
 			String oldPW = passOldPassword.getText();
 			String newPW = passNewPassword.getText();
 
 			if (oldPW.isEmpty() || newPW.isEmpty()) {
 				lblStatus.setText("Insert password");
 				return;
 			}
 			if (!newPW.equals(passConfirm.getText())) {
 				lblStatus.setText("Passwords do not match");
 				return;
 			}
 
 			disableControls();
 
 			IUser.Util.getInstance().setPassword(oldPW, newPW, new AsyncCallback<Void>() {
 
 				@Override
 				public void onSuccess(Void result) {
 					lblStatus.setText("Password successfully changed");
 					close();
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					enableControls();
 					if (caught == null)
 						lblStatus.setText("Unknown error");
 					else if (caught instanceof NoAccessException)
 						lblStatus.setText("Access denial: " + caught.getMessage());
 					else
 						lblStatus.setText("Error: " + caught.getMessage());
 				}
 			});
 
 		}
 
 		/** Disabled all controls */
 		private void disableControls() {
 			btnApply.setEnabled(false);
 			btnClose.setEnabled(false);
 
 		}
 
 		/** Disabled all controls */
 		private void enableControls() {
 			btnApply.setEnabled(true);
 			btnClose.setEnabled(true);
 
 		}
 	}
 
 	private static class ChangeUserDataPopup extends PopupPanel {
 
 		/** Old user data */
 		private XUser oldUserData = null;
 
 		/* Controls */
 		private final Panel rootPanel = new VerticalPanel();
 
 		private final Label lblTitle;
 
 		private final Grid grid;
 
 		// For lazyness just an array
 		private final Label[] lblDescs;
 
 		private final Label lblUsername;
 		private final TextBox txtRealname;
 		private final TextBox txtEmail;
 		private final Label lblUserState;
 
 		private final HorizontalPanel pnlToolbar;
 		private final Button btnApply;
 		private final Button btnClose;
 
 		private final Label lblStatus;
 
 		/* End of controls */
 
 		/**
 		 * Creates a new popup for changeing the userdate of the current user
 		 */
 		private ChangeUserDataPopup() {
 			this.lblTitle = new Label("Change user data");
 			this.pnlToolbar = new HorizontalPanel();
 			btnApply = new Button("Apply");
 			btnClose = new Button("Close");
 			lblDescs = new Label[] { new Label("Username"), new Label("Real name"), new Label("Email"), new Label("Status") };
 			lblUsername = new Label("");
 			lblUserState = new Label("");
 			txtRealname = new TextBox();
 			txtEmail = new TextBox();
 			grid = new Grid(4, 2);
 			lblStatus = new Label("");
 
 			for (int i = 0; i < lblDescs.length; i++)
 				grid.setWidget(i, 0, lblDescs[i]);
 			grid.setWidget(0, 1, lblUsername);
 			grid.setWidget(1, 1, txtRealname);
 			grid.setWidget(2, 1, txtEmail);
 			grid.setWidget(3, 1, lblUserState);
 
 			btnApply.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					apply();
 				}
 			});
 			btnClose.addClickHandler(new ClickHandler() {
 
 				@Override
 				public void onClick(ClickEvent event) {
 					close();
 				}
 			});
 
 			/* Handles enter as apply for all text fields */
 			final KeyDownHandler enterHandler = new KeyDownHandler() {
 				@Override
 				public void onKeyDown(KeyDownEvent event) {
 					if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
 						apply();
 					}
 				}
 			};
 			txtRealname.addKeyDownHandler(enterHandler);
 			txtEmail.addKeyDownHandler(enterHandler);
 
 			pnlToolbar.add(btnApply);
 			pnlToolbar.add(btnClose);
 
 			rootPanel.add(lblTitle);
 			rootPanel.add(grid);
 			rootPanel.add(pnlToolbar);
 			rootPanel.add(lblStatus);
 
 			this.setWidget(rootPanel);
 
 			lblTitle.setStyleName("h3"); // Yeah, out of order, but working ;-)
 			applyStyles(this);
 
 			getUserData();
 		}
 
 		/** Get user data */
 		private void getUserData() {
 			txtRealname.setEnabled(false);
 			txtEmail.setEnabled(false);
 			txtRealname.setText("Loading ... ");
 			txtEmail.setText("Loading ... ");
 			lblUsername.setText("Loading ... ");
 			lblUserState.setText("Loading ... ");
 			lblStatus.setText("Fetching user data ... ");
 
 			IUser.Util.getInstance().getMe(new AsyncCallback<XUser>() {
 
 				@Override
 				public void onSuccess(final XUser result) {
 					lblStatus.setText("");
 					if (result == null)
 						setGuestData();
 					else
 						setUserData(result);
 					oldUserData = result;
 				}
 
 				private void setUserData(XUser user) {
 					txtRealname.setText(user.realname);
 					txtEmail.setText(user.email);
 					lblUsername.setText(user.username);
 					switch (user.state) {
 					case administrator:
 						lblUserState.setText("Administrator");
 						break;
 					case deleted:
 						// This should never occur
 						lblUserState.setText("Deleted user");
 						break;
 					case moderator:
 						lblUserState.setText("Moderator");
 						break;
 					case unvalidated:
 						lblUserState.setText("User (unvalidated)");
 						break;
 					case validated:
 						lblUserState.setText("User");
 						break;
 					}
 
 					txtRealname.setEnabled(true);
 					txtEmail.setEnabled(true);
 				}
 
 				private void setGuestData() {
 					txtRealname.setText("Guest session");
 					txtEmail.setText("");
 					lblUsername.setText("Guest");
 					lblUserState.setText("Guest");
 
 					txtRealname.setEnabled(false);
 					txtEmail.setEnabled(false);
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					txtRealname.setText("");
 					txtEmail.setText("");
 					lblUsername.setText("");
 					lblUserState.setText("");
 					lblStatus.setText("There was an error fetching the user data");
 				}
 			});
 		}
 
 		/**
 		 * Shows the popup
 		 */
 		@Override
 		public void show() {
 			super.show();
 		}
 
 		/**
 		 * Closes the popup
 		 */
 		public void close() {
 			super.hide();
 		}
 
 		/** Internal call - applys the values */
 		private void apply() {
 			if (oldUserData == null) {
 				lblStatus.setText("Please wait for user data to be fetched");
 				return;
 			}
 
 			disableControls();
 
 			// Check data that has to be modified
 			final String realname;
 			final String email;
 
 			if (oldUserData.realname.equals(txtRealname.getText()))
 				realname = null;
 			else
 				realname = txtRealname.getText();
 			if (oldUserData.email.equals(txtEmail.getText()))
 				email = null;
 			else
 				email = txtEmail.getText();
 
 			/* This little helper contains all data for the refresh cycle */
 			Runnable helper = new Runnable() {
 
 				boolean refreshRealname = realname != null;
 				boolean refreshEmail = email != null;
 
 				@Override
 				public void run() {
 					if (realname == null && email == null) {
 						lblStatus.setText("Nothing to do here");
 						return;
 					}
 					if (realname != null && email != null)
 						lblStatus.setText("Setting new parameters ... ");
 					else if (realname == null)
 						lblStatus.setText("Updating email ... ");
 					else
 						lblStatus.setText("Updating realname ... ");
 
 					if (realname != null)
 						IUser.Util.getInstance().setRealName(realname, new AsyncCallback<Void>() {
 
 							@Override
 							public void onSuccess(Void result) {
 								lblStatus.setText("Realname set successfully." + (refreshEmail ? " Waiting for email to finish ..." : ""));
 								refreshRealname = false;
 								checkDone();
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								lblStatus.setText("Error setting new real name");
								enableControls();
 							}
 						});
 					if (email != null) {
 						IUser.Util.getInstance().setEmail(email, new AsyncCallback<Void>() {
 
 							@Override
 							public void onSuccess(Void result) {
 								lblStatus.setText("Email set successfully."
 										+ (refreshRealname ? " Waiting for realname to finish ..." : ""));
 								refreshEmail = false;
 								checkDone();
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								lblStatus.setText("Error setting new email");
								enableControls();
 							}
 						});
 
 					}
 				}
 
 				private void checkDone() {
 					if (refreshEmail)
 						return;
 					if (refreshRealname)
 						return;
 
 					// OK, we are done here :-)
 					lblStatus.setText("Done");
 					close();
 				}
 			};
 			helper.run();
 		}
 
 		/** Disabled all controls */
 		private void disableControls() {
 			btnApply.setEnabled(false);
 			btnClose.setEnabled(false);
 
 			txtRealname.setEnabled(false);
 			txtEmail.setEnabled(false);
 		}
 
 		/** Disabled all controls */
 		private void enableControls() {
 			btnApply.setEnabled(true);
 			btnClose.setEnabled(true);
 
 			txtRealname.setEnabled(oldUserData != null);
 			txtEmail.setEnabled(oldUserData != null);
 
 		}
 	}
 
 	/**
 	 * Applys the styles for an arbitary popup
 	 * 
 	 * @param popup
 	 *            the styles should be applied to
 	 */
 	private static void applyStyles(PopupPanel popup) {
 		Window.moveTo(0, 0);
 		popup.setGlassEnabled(true);
 		popup.setPopupPosition(200, 200);
 		popup.setStyleName("contact");
 	}
 
 	/**
 	 * Shows the login popup This ist just a redirect to
 	 * {@link Login#showLoginPopup()}
 	 */
 	public static void showLoginPopup() {
 		Login.showLoginPopup();
 	}
 
 	/**
 	 * Shows a {@link PopupPanel} to change the password of the currently logged
 	 * in user
 	 */
 	public static void showChangePasswordPopup() {
 		ChangePasswordPopup popup = new ChangePasswordPopup();
 		popup.show();
 	}
 
 	/**
 	 * Shows a {@link PopupPanel} to change the password of an arbitary user,
 	 * identified by its username
 	 * 
 	 * @param username
 	 *            of the user
 	 */
 	public static void showChangePasswordPopup(String username) {
 		ChangePasswordPopup popup = new ChangePasswordPopup(username);
 		popup.show();
 
 	}
 
 	/**
 	 * Shows a {@link PopupPanel} for changing the user details for the
 	 * currently logged in user
 	 */
 	public static void showChangeUserData() {
 		ChangeUserDataPopup popup = new ChangeUserDataPopup();
 		popup.show();
 	}
 }
