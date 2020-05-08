 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.examples.modules;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.InputElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.kk_electronic.kkportal.core.AbstractModule;
 import com.kk_electronic.kkportal.core.moduleview.Module;
 import com.kk_electronic.kkportal.core.security.Answer;
 import com.kk_electronic.kkportal.core.security.Challange;
 import com.kk_electronic.kkportal.core.security.SRP;
 import com.kk_electronic.kkportal.core.security.SecurityContext;
 
 public class SRPLogin extends AbstractModule implements Module {
 	private final Display display;
 	private final SRP srp;
 	private final SecurityContext context;
 
 	public static interface UIBinder extends UiBinder<Widget, Display> {
 	};
 
 	public static class Display {
 		Widget w;
 		private SRPLogin srpLogin;
 
 		@Inject
 		public Display(UIBinder binder) {
 			w = binder.createAndBindUi(this);
 		}
 
 		public Widget asWidget() {
 			return w;
 		}
 
 		public void setHandler(SRPLogin srpLogin) {
 			this.srpLogin = srpLogin;
 		}
 
 		@UiField
 		InputElement identity;
 
 		@UiField
 		InputElement password;
 
 		@UiHandler("login")
 		void onLogin(ClickEvent event) {
 			if (srpLogin != null) {
 				srpLogin.login(identity.getValue(), password.getValue());
 			}
 		}
 
 		@UiField
 		Element log;
 
 		public void setLog(String log) {
 			SafeHtmlBuilder sb = new SafeHtmlBuilder();
 			sb.appendEscapedLines(log);
 			this.log.setInnerHTML(sb.toSafeHtml().asString());
 		}
 	}
 
 	@Inject
 	public SRPLogin(Display display,SRP srp,SecurityContext context) {
 		this.display = display;
 		this.srp = srp;
 		this.context = context;
 		display.setHandler(this);
 	}
 
 	String log;	
 	private List<String> validmethods = Arrays.asList(new String[]{"password"});
 	
 	public void login(String identity, String password) {
 		context.setIdentity(identity);
 		context.setPassword(password);
		srp.requestChallange(null, context.getIdentity(),validmethods,null,new AsyncCallback<Challange>(){
 
 			@Override
 			public void onFailure(Throwable caught) {
 				//TODO:stub
 			}
 
 			@Override
 			public void onSuccess(Challange result) {
 				Answer a = context.calc_answer(result);
 				sendAnswer(a);
 			}
 			
 		});
 	}
 	
 	protected void sendAnswer(Answer a) {
 		srp.answerChallange(a.a.toString(16), a.m1, new AsyncCallback<String>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				GWT.log("Failed login",caught);
 				
 			}
 
 			@Override
 			public void onSuccess(String result) {
 				GWT.log("Successful login");
 			}
 		});
 	}
 	
 	public void cleanLog(){
 		log = "";
 		display.setLog(log);
 	}
 
 	public void addLog(String s){
 		if(log.isEmpty())
 		{
 			log = s;
 		} else {
 			log = log + '\n' + s;
 		}
 		display.setLog(log);
 	}
 	@Override
 	public Widget asWidget() {
 		return display.asWidget();
 	}
 
 }
