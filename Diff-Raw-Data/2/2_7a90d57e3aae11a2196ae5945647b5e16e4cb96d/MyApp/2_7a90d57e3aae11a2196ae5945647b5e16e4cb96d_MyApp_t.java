 package edu.ibs.webui.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.Window;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.events.CloseClickEvent;
 import com.smartgwt.client.widgets.events.CloseClickHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.LinkItem;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.layout.VStack;
 import edu.ibs.common.dto.AccountDTO;
 import edu.ibs.common.enums.AccountRole;
 import edu.ibs.common.interfaces.IAuthServiceAsync;
 import edu.ibs.webui.client.admin.AddMoneyController;
 import edu.ibs.webui.client.admin.CreateBankBookController;
 import edu.ibs.webui.client.admin.CreateNewUserController;
 import edu.ibs.webui.client.cards.CardRequestsGrid;
 import edu.ibs.webui.client.controller.GenericController;
 import edu.ibs.webui.client.utils.AppCallback;
 import edu.ibs.webui.client.utils.Components;
 import edu.ibs.webui.client.utils.JS;
 
 public class MyApp implements EntryPoint {
 
     private static final int HEADER_HEIGHT = 85;
 	public static final String LOGIN_COOKIE_NAME = "ibs.login";
     public static final String IS_ADMIN_COOKIE = "ibs.admin";
 
 	private VLayout mainLayout;
     private VLayout adminLayout;
 	private Window loginWindow;
 	private Window registerWindow;
 
     /**
      * Основное окно
      */
     private Canvas bg = new Canvas();
 
     public void onModuleLoad() {
 
         com.google.gwt.user.client.Window.enableScrolling(false);
         com.google.gwt.user.client.Window.setMargin("0px");
		JS.setCookie(IS_ADMIN_COOKIE, Boolean.FALSE.toString());
		JS.setCookie(LOGIN_COOKIE_NAME, "");
 
         bg.setWidth100();
         bg.setHeight100();
 
         bg.draw();
 		loadUser();
     }
 
 	private void loadUser() {
 		String login = JS.getCookie(LOGIN_COOKIE_NAME);
 		if (login == null || login.length() == 0) {
 			login();
 		} else {
 			IAuthServiceAsync.Util.getInstance().login(login, "", new AppCallback<AccountDTO>() {
 				@Override
 				public void onFailure(final Throwable throwable) {
 					super.onFailure(throwable);
 					login();
 				}
 
 				@Override
 				public void onSuccess(final AccountDTO dto) {
 					if (dto == null) {
 						login();
 					} else {
                         JS.setCookie(LOGIN_COOKIE_NAME, dto.getEmail());
                         if (AccountRole.ADMIN.equals(dto.getRole())) {
                             JS.setCookie(IS_ADMIN_COOKIE, Boolean.TRUE.toString());
                         } else {
                             JS.setCookie(IS_ADMIN_COOKIE, Boolean.FALSE.toString());
                         }
                         ApplicationManager.getInstance().setAccount(dto);
 						bg.addChild(getMainLayoutForRole());
 					}
 				}
 			});
 		}
 	}
 
 	private void login() {
 		getLoginWindow().show();
 	}
 
 	private Window getRegisterCanvas() {
 		if (registerWindow == null) {
 			registerWindow = Components.getWindow();
 			registerWindow.setHeight(200);
 
 			final GenericController login = Components.getTextItem();
 			final GenericController pass = Components.getPasswordItem();
 			final GenericController repPass = Components.getPasswordItem();
 //			final GenericController capthcaInputTextItemView = Components.getTextItem();
 
 			final IButton regButton = new IButton("Зарегистрироваться");
 			regButton.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(final ClickEvent clickEvent) {
 					String name = (String) login.unbind();
 					String password = (String) pass.unbind();
 					String passwordAgain = (String) repPass.unbind();
 //					String captchaText = (String) capthcaInputTextItemView.unbind();
 					if (name == null || "".equals(name) || name.length() == 0) {
 						SC.warn("Почтовый адрес не заполнен.");
 					} else if (password == null || "".equals(password) || password.length() == 0) {
 						SC.warn("Пароль не заполнен.");
 					} else if (passwordAgain == null || "".equals(passwordAgain) || passwordAgain.length() == 0) {
 						SC.warn("Поле подтверждения пароля не заполнено.");
 //					} else if (captchaText == null || "".equals(captchaText) || captchaText.length() == 0) {
 //						SC.warn("Введите текст с картинки.");
 					} else {
 						regButton.setDisabled(true);
 						IAuthServiceAsync.Util.getInstance().register(name, password, passwordAgain, null,
 								new AppCallback<AccountDTO>() {
 									@Override
 									public void onFailure(Throwable t) {
 										super.onFailure(t);
 										regButton.setDisabled(false);
 									}
 
 									@Override
 									public void onSuccess(AccountDTO s) {
 										regButton.setDisabled(false);
                                         if (s != null) {
                                             JS.setCookie(LOGIN_COOKIE_NAME, s.getEmail());
                                             if (AccountRole.ADMIN.equals(s.getRole())) {
                                                 JS.setCookie(IS_ADMIN_COOKIE, Boolean.TRUE.toString());
                                             } else {
                                                 JS.setCookie(IS_ADMIN_COOKIE, Boolean.FALSE.toString());
                                             }
                                             ApplicationManager.getInstance().setAccount(s);
                                             SC.say("Вы зарегестрировались, " + s.getEmail() + "!");
                                             registerWindow.hide();
                                             bg.addChild(getMainLayoutForRole());
                                         }
 									}
 								});
 					}
 				}
 			});
 			regButton.setWidth(150);
 
 //			final Img captchaImage = new Img("/SimpleCaptcha.jpg");
 //			captchaImage.setWidth(120);
 //			captchaImage.setHeight(35);
 
 			VStack layout = new VStack();
 			layout.setWidth100();
 			layout.setHeight100();
 			layout.addMember(Components.addTitle("E-mail", login.getView()));
 			layout.addMember(Components.addTitle("Пароль", pass.getView()));
 			layout.addMember(Components.addTitle("Подтверждение пароля", repPass.getView()));
 
 //			layout.addMember(Components.addTitle("", captchaImage));
 //			layout.addMember(Components.addTitle("", capthcaInputTextItemView.getView()));
 			layout.setShowResizeBar(false);
 
 			HLayout buttons = new HLayout();
 			buttons.addMember(regButton);
 
 			VLayout view = new VLayout();
 			view.setMembersMargin(5);
 			view.addMember(layout);
 			view.addMember(buttons);
 			view.setMargin(5);
 			view.setShowResizeBar(false);
 
 			registerWindow.addItem(view);
 			registerWindow.addCloseClickHandler(new CloseClickHandler() {
 				@Override
 				public void onCloseClick(final CloseClickEvent closeClickEvent) {
 					registerWindow.hide();
 					getLoginWindow().show();
 				}
 			});
 
 		}
 
         return registerWindow;
     }
 
     private Window getLoginWindow() {
 		if (loginWindow == null) {
 			loginWindow = Components.getWindow();
             loginWindow.setShowCloseButton(false);
 			final GenericController login = Components.getTextItem();
 			final GenericController pass = Components.getPasswordItem();
 
 			IButton loginButton = new IButton("Войти");
 			loginButton.setWidth(80);
 			loginButton.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(final ClickEvent clickEvent) {
 					final String loginText = ((String) login.unbind());
 					String passText = ((String) pass.unbind());
 					if (loginText == null || "".equals(loginText) || loginText.length() == 0) {
 						SC.warn("Логин не заполнен. Введите допустимый логин.");
 					} else if (passText == null || "".equals(passText) || passText.length() == 0) {
 						SC.warn("Пароль пуст. Введите допустимый пароль.");
 					} else {
 						IAuthServiceAsync.Util.getInstance().login(loginText, passText,
 								new AppCallback<AccountDTO>() {
 									@Override
 									public void onSuccess(final AccountDTO s) {
 										if (s != null) {
 											JS.setCookie(LOGIN_COOKIE_NAME, loginText);
                                             if (AccountRole.ADMIN.equals(s.getRole())) {
                                                 JS.setCookie(IS_ADMIN_COOKIE, Boolean.TRUE.toString());
                                             } else {
 												JS.setCookie(IS_ADMIN_COOKIE, Boolean.FALSE.toString());
 											}
 											SC.say("Вы залогинились, ", s.getEmail() + "!");
                                             ApplicationManager.getInstance().setAccount(s);
 											loginWindow.hide();
 											bg.addChild(getMainLayoutForRole());
 										} else {
 											SC.say("Доступ запрещён. Попробуйте ещё раз.");
 										}
 									}
 								});
 					}
 				}
 			});
 
 			LinkItem linkItem = new LinkItem("link");
 			linkItem.setShowTitle(false);
 			linkItem.setLinkTitle("Регистрация");
 			linkItem.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
 				@Override
 				public void onClick(final com.smartgwt.client.widgets.form.fields.events.ClickEvent clickEvent) {
 					loginWindow.hide();
 					getRegisterCanvas().show();
 				}
 			});
 			DynamicForm regForm = new DynamicForm();
 			regForm.setItems(linkItem);
 			regForm.setColWidths("0,*");
 
 			VStack layout = new VStack();
 			layout.setMembersMargin(5);
 			layout.setMargin(5);
 			layout.addMember(Components.addTitle("E-mail", login.getView()));
 			layout.addMember(Components.addTitle("Пароль", pass.getView()));
 
 			HLayout buttons = new HLayout();
 			buttons.setMembersMargin(5);
 			buttons.addMember(loginButton);
 			buttons.addMember(regForm);
 
             layout.addMember(buttons);
 			layout.setShowResizeBar(false);
 
 			loginWindow.addItem(layout);
 			loginWindow.addCloseClickHandler(new CloseClickHandler() {
 				@Override
 				public void onCloseClick(CloseClickEvent closeClickEvent) {
 					//
 				}
 			});
 
 		}
 
 		return loginWindow;
     }
 
     private Canvas getMainLayoutForRole() {
         boolean admin = Boolean.TRUE.toString().equals(JS.getCookie(IS_ADMIN_COOKIE));
         if (admin) {
             return getAdminLayout();
         } else {
             return getMainLayout();
         }
     }
 
     private Canvas getAdminLayout() {
         if (adminLayout == null) {
             adminLayout = new VLayout();
             adminLayout.setWidth100();
             adminLayout.setHeight100();
 
             VLayout vLayout = new VLayout();
             vLayout.setHeight(HEADER_HEIGHT);
             vLayout.addMember(new Masthead());
 
 			VLayout links = new VLayout();
 			links.setWidth("20%");
 			final String adminLinkStyleName = "label-link-admin";
 
 			Label addUser = new Label("Добавить пользователя");
 			addUser.setStyleName(adminLinkStyleName);
 			final ClickHandler addUserClickHandler = new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent clickEvent) {
 					CreateNewUserController controller = new CreateNewUserController();
 					controller.getWindow().draw();
 				}
 			};
 			addUser.addClickHandler(addUserClickHandler);
 			links.addMember(addUser);
 
 //			Label deleteUser = new Label("Удалить пользователя");
 //			deleteUser.setStyleName(adminLinkStyleName);
 //			links.addMember(deleteUser);
 
 			Label createBankBook = new Label("Создать банковский счёт");
 			createBankBook.setStyleName(adminLinkStyleName);
 			final ClickHandler createBankBookClickHandler = new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent clickEvent) {
 					CreateBankBookController controller = new CreateBankBookController();
 					controller.getWindow().draw();
 				}
 			};
 			createBankBook.addClickHandler(createBankBookClickHandler);
 			links.addMember(createBankBook);
 
 			Label addMoney = new Label("Пополнить счёт");
 			addMoney.setStyleName(adminLinkStyleName);
 			final ClickHandler addMoneyClickHandler = new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent clickEvent) {
 					AddMoneyController controller = new AddMoneyController();
 					controller.getWindow().draw();
 				}
 			};
 			addMoney.addClickHandler(addMoneyClickHandler);
 			links.addMember(addMoney);
 
 			VLayout adminContentLayout = new VLayout();
 			adminContentLayout.setWidth100();
 
 
 			adminContentLayout.addMember(new CardRequestsGrid());
 
 			HLayout view = new HLayout();
 			view.addMember(links);
 			view.addMember(adminContentLayout);
 
             adminLayout.addMember(vLayout);
             adminLayout.addMember(view);
 			adminLayout.setStyleName("crm-ContextArea");
         }
 
         return adminLayout;
     }
 
     private Canvas getMainLayout() {
         if (mainLayout == null) {
 
             mainLayout = new VLayout();
             mainLayout.setWidth100();
             mainLayout.setHeight100();
 
             HLayout northLayout = new HLayout();
 
             VLayout vLayout = new VLayout();
             vLayout.addMember(new Masthead());
 
             northLayout.addMember(vLayout);
 
             VLayout westLayout = new NavigationPane();
             westLayout.setWidth("15%");
 
             VLayout eastLayout = new AccountView();
             eastLayout.setWidth("85%");
 
             HLayout southLayout = new HLayout();
             southLayout.setMembers(westLayout, eastLayout);
 			southLayout.setHeight100();
 
             mainLayout.addMember(northLayout);
             mainLayout.addMember(southLayout);
         }
 
         return mainLayout;
     }
 }
