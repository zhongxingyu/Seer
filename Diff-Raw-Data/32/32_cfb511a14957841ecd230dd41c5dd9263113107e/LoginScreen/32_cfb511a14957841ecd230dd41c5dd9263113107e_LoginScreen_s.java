 /*
 *
 *	Copyright (c) 2013 Andreas Reder
 *	Author      : Andreas Reder <andreas.reder@lielas.org>
 *	File		: 
 *
 *	This File is part of lielas, see www.lielas.org for more information.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
 package lielas.ui;
 
 
 import lielas.core.LanguageHelper;
 
 import lielas.LiewebUI;
 import lielas.ui.HeaderScreen;
 
 import com.vaadin.event.ShortcutAction.KeyCode;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.PasswordField;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.NativeSelect;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Notification;
 
 @SuppressWarnings({ "serial", "unused" })
 public class LoginScreen extends VerticalLayout {
 	
 	private LiewebUI app;
 	
 	
 	private TextField nameTxt;
 	private PasswordField passwordTxt;
 	private Button loginButton;
 	
 	public int rights;
 	
 	public LoginScreen(final LiewebUI app){
 		this.app = app;
 		
 		//setCaption("Login");
 		setSizeFull();
 		
 		VerticalLayout vLo = new VerticalLayout();
 		vLo.setWidth(200, Unit.PIXELS);
 		vLo.setHeight(140, Unit.PIXELS);
 		this.addComponent(vLo);
 		this.setComponentAlignment(vLo, Alignment.MIDDLE_CENTER);
 		
 		nameTxt = new TextField(app.langHelper.GetString(LanguageHelper.LOGIN_USERNAME));
 		vLo.addComponent(nameTxt);
 		
 		passwordTxt = new PasswordField(app.langHelper.GetString(LanguageHelper.LOGIN_PASSWORD));
 		vLo.addComponent(passwordTxt);
 		
 		loginButton = new Button(app.langHelper.GetString(LanguageHelper.LOGIN_SUBMIT));
 		loginButton.setClickShortcut(KeyCode.ENTER);
 		vLo.addComponent(loginButton);	
 		
 		loginButton.addClickListener(new Button.ClickListener() {
 			
 			@Override
 			public void buttonClick(ClickEvent event) {
 				LoginButtonPressed();
 				
 			}
 		});
 		
 	}
 
 	public void Update(){
 		nameTxt.setCaption(app.langHelper.GetString(LanguageHelper.LOGIN_USERNAME));
 		passwordTxt.setCaption(app.langHelper.GetString(LanguageHelper.LOGIN_PASSWORD));
 		loginButton.setCaption(app.langHelper.GetString(LanguageHelper.LOGIN_SUBMIT));;
 	}
 	
 	private void LoginButtonPressed(){
		app.Authenticate(this.nameTxt.getValue(), this.nameTxt.getValue());
 	}
 
 	
 	
 }
 
