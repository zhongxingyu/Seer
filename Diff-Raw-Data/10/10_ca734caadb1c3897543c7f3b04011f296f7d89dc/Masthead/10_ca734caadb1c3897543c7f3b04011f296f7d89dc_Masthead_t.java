 package edu.ibs.webui.client;
 
 import com.google.gwt.core.client.GWT;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.widgets.Img;
 import com.smartgwt.client.widgets.ImgButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import edu.ibs.common.interfaces.IAuthServiceAsync;
 import edu.ibs.webui.client.utils.AppCallback;
 import edu.ibs.webui.client.utils.JS;
 
 /**
  * User: Максим
  * Date: 29.10.12
  * Time: 23:18
  */
 public class Masthead extends HLayout {
 
     private static final int MASTHEAD_HEIGHT = 58;
 
     public Masthead() {
         super();
         GWT.log("init Masthead()...", null);
         this.setStyleName("crm-Masthead");
         this.setHeight(MASTHEAD_HEIGHT);
         Img logo = new Img("logo.png", 48, 48);
         logo.setStyleName("crm-Masthead-Logo");
 
         Label name = new Label();
         name.setStyleName("crm-MastHead-Name");
         name.setContents("IBS");
 
         HLayout westLayout = new HLayout();
         westLayout.setHeight(MASTHEAD_HEIGHT);
         westLayout.setWidth100();
         westLayout.addMember(logo);
         westLayout.addMember(name);
 
 		final String login = ApplicationManager.getInstance().getAccount().getEmail();
 
         Label signedInUser = new Label();
         signedInUser.setStyleName("crm-MastHead-SignedInUser");
         signedInUser.setContents(login);
 
 		ImgButton imgButton = new ImgButton();
 		imgButton.setSize(16);
 		imgButton.setShowFocused(false);
 		imgButton.setShowRollOver(false);
 		imgButton.setShowDown(false);
 		imgButton.setTooltip("Выйти");
 		imgButton.setSrc("toolbar/delete.png");
 		imgButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent clickEvent) {
 				IAuthServiceAsync.Util.getInstance().logout(login, new AppCallback<Void>() {
 					@Override
 					public void onSuccess(Void aVoid) {
                        JS.goToLoginPage();
 					}
 				});
 			}
 		});
 
 		HLayout l1 = new HLayout();
 		l1.setPadding(20);
 		l1.addMember(imgButton);
 
         HLayout eastLayout = new HLayout();
         eastLayout.setAlign(Alignment.RIGHT);
         eastLayout.setHeight(MASTHEAD_HEIGHT);
 		eastLayout.setMembersMargin(5);
         eastLayout.addMember(signedInUser);
 		eastLayout.addMember(l1);
 
         this.addMember(westLayout);
         this.addMember(eastLayout);
 
     }
 
 }
