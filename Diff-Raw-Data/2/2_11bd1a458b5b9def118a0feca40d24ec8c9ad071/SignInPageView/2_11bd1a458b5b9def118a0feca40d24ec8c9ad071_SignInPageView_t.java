 /**
  * (C) Copyright 2010, 2011 upTick Pty Ltd
  *
  * Licensed under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation. You may obtain a copy of the
  * License at: http://www.gnu.org/copyleft/gpl.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.fountainhead.client.view;
 
 import com.fountainhead.client.presenter.SignInPagePresenter;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 public class SignInPageView extends ViewWithUiHandlers<SignInPageUiHandlers> implements
 SignInPagePresenter.MyView {
 
 	private static final String DEFAULT_USER_NAME = "administrator";
 	private static final String DEFAULT_PASSWORD = "administrator";
 
 	private static String html = "<div>\n"
 			+ "<table align=\"center\">\n"
 			+ "  <tr>\n" + "<td>&nbsp;</td>\n" + "<td>&nbsp;</td>\n" + "</tr>\n"
 			+ "  <tr>\n" + "<td>&nbsp;</td>\n" + "<td>&nbsp;</td>\n" + "</tr>\n"
 			+ "  <tr>\n" + "<td>&nbsp;</td>\n" + "<td>&nbsp;</td>\n" + "</tr>\n"
 			+ "  <tr>\n" + "<td>&nbsp;</td>\n" + "<td>&nbsp;</td>\n" + "</tr>\n"
 			+ "  <tr>\n" + "<td>&nbsp;</td>\n" + "<td>&nbsp;</td>\n" + "</tr>\n"
 			+ "  <tr>\n"
			+ "    <td colspan=\"2\" style=\"font-weight:bold;\">Sign In </td>\n"
 			+ "  </tr>\n"
 			+ "  <tr>\n"
 			+ "    <td>User name</td>\n"
 			+ "    <td id=\"userNameFieldContainer\"></td>\n"
 			+ "  </tr>\n"
 			+ "  <tr>\n"
 			+ "    <td>Password</td>\n"
 			+ "    <td id=\"passwordFieldContainer\"></td>\n"
 			+ "  </tr>\n"
 			+ "  <tr>\n"
 			+ "    <td></td>\n"
 			+ "    <td id=\"signInButtonContainer\"></td>\n"
 			+ "  </tr>\n"
 			+ "  <tr>\n" + "<td>&nbsp;</td>\n" + "<td>&nbsp;</td>\n" + "</tr>\n"
 			+ "</table>\n"
 			+ "</div>\n"
 			+ "<div  align=\"center\" style=\"color:red;\" id=\"errorLabelContainer\">\n"
 			// + "  <tr>\n"
 			// + "    <td colspan=\"2\">Forget your password?</td>\n"
 			// + "  </tr>\n"
 			// + "  <tr>\n"
 			// +
 			// "    <td colspan=\"2\" style=\"color:red;\" id=\"errorLabelContainer\"></td>\n"
 			// + "  </tr>\n" + "</table>\n"
 			+ "</div>\n";
 
 	private final HTMLPanel panel;
 
 	private final TextBox userNameField;
 	private final PasswordTextBox passwordField;
 	private final Button signInButton;
 	private final Label errorLabel;
 
 	public SignInPageView() {
 
 		panel = new HTMLPanel(html);
 		errorLabel = new Label();
 		userNameField = new TextBox();
 		passwordField = new PasswordTextBox();
 		signInButton = new Button("Sign in");
 
 		userNameField.setText(DEFAULT_USER_NAME);
 
 		// See FieldVerifier
 		// Passwords must contain at least 8 characters with at least one digit,
 		// one upper case letter, one lower case letter and one special symbol (@#$%).
 		passwordField.setText(DEFAULT_PASSWORD);
 
 		panel.add(userNameField, "userNameFieldContainer");
 		panel.add(passwordField, "passwordFieldContainer");
 		panel.add(signInButton, "signInButtonContainer");
 		panel.add(errorLabel, "errorLabelContainer");
 
 		bindCustomUiHandlers();
 	}
 
 	protected void bindCustomUiHandlers() {
 
 		signInButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 
 				//				if (FieldVerifier.isValidUserName(getUserName()) &&
 				//						(FieldVerifier.isValidPassword(getPassword()))) {
 				if (getUiHandlers() != null) {
 					getUiHandlers().onOkButtonClicked();
 				}
 				// }
 				//				else {
 				//					// event.cancel();
 				//					Window.alert("You must enter a valid User name and Password.");
 				//					resetAndFocus();
 				//				}
 			}
 		});
 	}
 
 	@Override
 	public Widget asWidget() {
 		return panel;
 	}
 
 	@Override
 	public String getUserName() {
 		return userNameField.getText();
 	}
 
 	@Override
 	public String getPassword() {
 		return passwordField.getText();
 	}
 
 	@Override
 	public void resetAndFocus() {
 		userNameField.setFocus(true);
 		userNameField.selectAll();
 	}
 
 	@Override
 	public void setError(String errorText) {
 		errorLabel.setText(errorText);
 
 	}
 }
