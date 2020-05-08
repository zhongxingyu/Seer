 /*
   Copyright (c) 2009-2011 Malte Mader <mader@zbh.uni-hamburg.de>
   Copyright (c) 2009-2011 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
 
 package de.unihamburg.zbh.fishoracle.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.ImageStyle;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Img;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.Window;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.LinkItem;
 import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
 import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.LayoutSpacer;
 import com.smartgwt.client.widgets.layout.SectionStackSection;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.tab.Tab;
 import com.smartgwt.client.widgets.tab.TabSet;
 
 import de.unihamburg.zbh.fishoracle.client.rpc.UserService;
 import de.unihamburg.zbh.fishoracle.client.rpc.UserServiceAsync;
 
 
 public class NorthPanel extends HLayout{
 	
 	private MainPanel mp = null;
 	private LinkItem logout;
 	private Window infoWin;
 	
 	public NorthPanel(MainPanel mainPanel) {
 		
 		mp = mainPanel;
 		this.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		DynamicForm left = new DynamicForm();
 		
 		LinkItem info = new LinkItem();
 		info.setLinkTitle("Info");
 		info.setShowTitle(false);
 		info.addClickHandler(new ClickHandler(){
 
 			@Override
 			public void onClick(ClickEvent event) {
 				infoWin.show();
 			}
 		});
 		
 		left.setItems(info);
 		
 		DynamicForm right = new DynamicForm();
 		right.markForRedraw();
 		logout = new LinkItem();
 		logout.setTitle("");
 		logout.setLinkTitle("Logout");
 		logout.addClickHandler(new ClickHandler(){
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Tab[] tabs = mp.getCenterPanel().getCenterTabSet().getTabs();
 				for(int i=0; i < tabs.length; i++){
 					if(tabs[i].getTitle().equals("Data Import")){
 						mp.getCenterPanel().freePage();
 					}
 				}
 				userLogout();
 			}
 		});
 		
 		right.setItems(logout);
 		
 		this.addMember(left);
 		this.addMember(new LayoutSpacer());
 		this.addMember(right);
 		
 		infoWin = new Window();
 		infoWin.setTitle("Info");
 		infoWin.setAutoSize(true);
 		infoWin.setAutoCenter(true);
 		infoWin.setDefaultLayoutAlign(Alignment.CENTER);
 		
 		VLayout infoLayer = new VLayout();
 		infoLayer.setWidth(400);
 		infoLayer.setHeight(200);
 		
 		Label infoLbl = new Label();
 		infoLbl.setContents("Copyright (c) 2009-2011 Malte Mader <br>" +
 							"Copyright (c) 2009-2011 Center for Bioinformatics, University of Hamburg <br>" +
 							"Copyright (c) 2009-2011 Department of Pathology University Medical Center Hamburg-Eppendorf");
 		
		Img ukeImg = new Img("uke.png");
 		ukeImg.setImageType(ImageStyle.NORMAL);
 		
 		Img zbhImg = new Img("zbh.jpg");
 		zbhImg.setImageType(ImageStyle.NORMAL);
 		
 		infoLayer.addMember(ukeImg);
 		infoLayer.addMember(zbhImg);
 		infoLayer.addMember(infoLbl);
 		
 		infoWin.addItem(infoLayer);
 	}
 
 	public LinkItem getLogout() {
 		return logout;
 	}
 	
 	/*=============================================================================
 	 *||                              RPC Calls                                  ||
 	 *=============================================================================
 	 * */	
 	
 	public void userLogout(){
 		
 		final UserServiceAsync req = (UserServiceAsync) GWT.create(UserService.class);
 		ServiceDefTarget endpoint = (ServiceDefTarget) req;
 		String moduleRelativeURL = GWT.getModuleBaseURL() + "UserService";
 		endpoint.setServiceEntryPoint(moduleRelativeURL);
 		final AsyncCallback<Void> callback = new AsyncCallback<Void>(){
 			public void onSuccess(Void result){
 				
 				SectionStackSection[] ss = mp.getWestPanel().getSections();
 				
 				for(int i = 0; i < ss.length; i++){
 					
 					if(ss[i].getTitle().equals("Admin")){
 						mp.getWestPanel().removeSection(ss[i].getID());
 					}
 				}
 				
 				mp.getWestPanel().getSearchTextItem().setValue("");
 				
 				/* remove all center panel tabs except for the welcome tab*/
 				TabSet centerTabSet = mp.getCenterPanel().getCenterTabSet();
 				Tab[] tabs = mp.getCenterPanel().getCenterTabSet().getTabs();
 				for(int i=0; i < tabs.length; i++){
 					if(!tabs[i].getTitle().equals("Welcome")){
 						centerTabSet.removeTab(tabs[i]);
 					} else {
 						centerTabSet.selectTab(tabs[i]);
 					}
 				}
 				
 				mp.getLoginScreen().show();
 				mp.getLoginScreen().animateFade(100);
 				
 				//MessageBox.hide();
 				
 			}
 			public void onFailure(Throwable caught){
 				System.out.println(caught.getMessage());
 				//MessageBox.hide();
 				SC.say(caught.getMessage());
 				GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
 
 					@Override
 					public void onUncaughtException(Throwable e) {
 						e.printStackTrace();
 					}
 					
 				});
 			}
 		};
 		req.logout(callback);
 	}	
 }
