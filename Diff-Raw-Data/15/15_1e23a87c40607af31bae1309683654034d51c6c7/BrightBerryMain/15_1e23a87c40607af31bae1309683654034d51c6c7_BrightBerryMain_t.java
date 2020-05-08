 package mobi.bbhn.brightberry;
 
 /*
 Copyright (c) 2009, Chris Hallgren & Hallgren Networks
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
 	* Redistributions of source code must retain the above copyright notice,
 	  this list of conditions and the following disclaimer.
 	* Redistributions in binary form must reproduce the above copyright notice,
 	  this list of conditions and the following disclaimer in the documentation
 	  and/or other materials provided with the distribution.
 	* Neither the name of Chris Hallgren or Hallgren Networks nor the names of
 	  its contributors may be used to endorse or promote products derived from
 	  this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 OF SUCH DAMAGE.
 */
 
 import net.rim.blackberry.api.browser.Browser;
 import net.rim.device.api.system.DeviceInfo;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.Status;
 import net.rim.device.api.ui.container.MainScreen;
 
 public class BrightBerryMain extends MainScreen {
 	Settings settings = Settings.getInstance();
 	MainButton searchBtn = new MainButton("Search & Check in", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton friendstreamBtn = new MainButton("What's Happening?", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton postnoteBtn = new MainButton("Post Note", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton postphotoBtn = new MainButton("Post Photo", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	
 	MainButton peoplenearBtn = new MainButton("People Near Me", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton friendsBtn = new MainButton("Friends", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton visitedplacesBtn = new MainButton("Visited Places", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton placemarkBtn = new MainButton("Placemarks", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton messagesBtn = new MainButton("Messages", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton mentionsBtn = new MainButton("Mentions", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	
 	MainButton actsettingsBtn = new MainButton("Account Settings", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	MainButton appsettingsBtn = new MainButton("Application Settings", ButtonField.CONSUME_CLICK|ButtonField.FOCUSABLE);
 	private MenuItem aboutItem;
 	private MenuItem shutdownItem;
 	private MenuItem privacymodeItem;
 	private boolean priupdated;
 	private MenuItem licenseItem;
 	private BrightBerryMain screen = this;
 	private MenuItem refreshlocItem;
 	private boolean upgradeavailable;
 	private MenuItem upgradeItem;
 	private MenuItem latestnewsItem;
 	private static String locationName;
 	
     public BrightBerryMain() {
     	super.setTitle(new LabelField("BrightBerry", 1152921504606846980L));
     	
     	FieldChangeListener SearchListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new SearchPlaceScreen());
 			}
 		};
 		
 		FieldChangeListener FriendStreamListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new StreamScreen(BrightBerryMain.this.settings.getAutoUpdate(), "friend", 0));
 			}
 		};
 		
 		FieldChangeListener PostNoteListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new PostNoteScreen());
 			}
 		};
 		
 		FieldChangeListener PostPhotoListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new PostPhotoScreen());
 			}
 		};
 		
 		FieldChangeListener PlacemarkListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new PlacemarkScreen());
 			}
 		};
 		
 		FieldChangeListener MessageListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new DirectMessageRcvScreen());
 			}
 		};
 		
 		FieldChangeListener AppSettingsListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new OptionsScreen());
 			}
 		};
 		
 		FieldChangeListener MentionsListener = new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				UiApplication.getUiApplication().pushScreen(new StreamScreen(BrightBerryMain.this.settings.getAutoUpdate(), "mentions", 0));
 			}
 		};
 		
 		this.refreshlocItem = new MenuItem("Refresh Location", 1, 10) {
 			public void run() {
 				Thread whereThread = new WhereAmIThread(BrightBerryMain.this.screen);
 				whereThread.start();
 			}
 		};
 		
 		this.privacymodeItem = new MenuItem("Privacy Mode", 2, 10) {
 			public void run() {
 				String choices[] = {"Set Privacy"};
 				int values[] = {0, 0, Dialog.OK};
 				PrivacyDialog privacybox = new PrivacyDialog(choices, values);
 				if (privacybox.doModal() == Dialog.OK) {
 					Thread privacyUpdateThread = new PrivacyThread(privacybox.rgrp.getSelectedIndex(), BrightBerryMain.this);
 					privacyUpdateThread.start();
 				}
 			}
 		};
 		
 		this.latestnewsItem = new MenuItem("View latest news", 50, 10) {
 			public void run() {
 				UiApplication.getUiApplication().pushScreen(new StreamScreen(true, "person", 0, "brightberry"));
 			}
 		};
 		
 		this.upgradeItem = new MenuItem("Check for upgrade", 51, 10) {
 			public void run() {
 				UpgradeThread updateThread = new UpgradeThread(BrightBerryMain.this);
 				updateThread.start();
 			}
 		};
 		
 		this.licenseItem = new MenuItem("License", 53, 10) {
 			public void run() {
 				UiApplication.getUiApplication().pushScreen(new LicenseScreen());
 			}
 		};
 		
 		this.aboutItem = new MenuItem("About", 54, 10) {
 			public void run() {
 				UiApplication.getUiApplication().pushScreen(new AboutScreen());
 			}
 		};
 		
 		this.shutdownItem = new MenuItem("Shut down", 500, 10) {
 			public void run() {
 				System.exit(0);
 			}
 		};
 		
 		if (this.settings.getAuthed()) {
 			addMenuItem(this.refreshlocItem);
 			addMenuItem(this.privacymodeItem);
 			addMenuItem(MenuItem.separator(49));
 			addMenuItem(this.latestnewsItem);
 		}
 		addMenuItem(this.aboutItem);
 		addMenuItem(MenuItem.separator(52));
 		addMenuItem(this.licenseItem);
 		addMenuItem(this.upgradeItem);
 		addMenuItem(MenuItem.separator(55));
 		addMenuItem(this.shutdownItem);
 		
 		if (this.settings.getAuthed()) {
 	    	searchBtn.setChangeListener(SearchListener);
 	    	add(searchBtn);
 	    	friendstreamBtn.setChangeListener(FriendStreamListener);
 	    	add(friendstreamBtn);
 	    	postnoteBtn.setChangeListener(PostNoteListener);
 	    	add(postnoteBtn);
 	    	postphotoBtn.setChangeListener(PostPhotoListener);
 	    	if (DeviceInfo.hasCamera()) {
 	    		add(postphotoBtn);
 	    	}
 	    	add(new SeparatorField());
 	    	placemarkBtn.setChangeListener(PlacemarkListener);
 	    	add(placemarkBtn);
 	    	messagesBtn.setChangeListener(MessageListener);
 	    	add(messagesBtn);
 	    	mentionsBtn.setChangeListener(MentionsListener);
 	    	add(mentionsBtn);
 	    	add(new SeparatorField());
 		} else {
 			RichTextField statusField = new RichTextField("Please setup your account", RichTextField.NON_FOCUSABLE);
 			add(statusField);
 			add(new SeparatorField());
 		}
     	appsettingsBtn.setChangeListener(AppSettingsListener);
     	add(appsettingsBtn);
     }
     
     public void callPrivacy(boolean string) {
 		this.priupdated = string;
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 			public void run() {
 				if (BrightBerryMain.this.priupdated) {
 					Status.show("Privacy Mode updated");
 				} else {
 					Status.show("Unable to update Privacy Mode");
 				}
 			}
 		});
 	}
     
     public void callUpgrade(boolean string) {
     	this.upgradeavailable = string;
     	UiApplication.getUiApplication().invokeLater(new Runnable() {
 			public void run() {
 				if (BrightBerryMain.this.upgradeavailable) {
 					if (Dialog.ask(Dialog.D_YES_NO, "An upgrade is available! Do you wish to upgrade now?", Dialog.YES) == Dialog.YES) {
 						Browser.getDefaultSession().displayPage("http://bbhn.mobi");
 						Dialog.alert("You will need to reboot/battery pull to finish the upgrade");
 					}
 				} else {
 					Dialog.alert("You are running the most current version");
 				}
 			}
 		});
     }
     
     public void updateLocation(String locName){
 		BrightBerryMain.locationName = locName;
 		UiApplication.getUiApplication().invokeLater(
 			new Runnable() {
 				public void run() {
 					System.out.println("Checked in at " + BrightBerryMain.locationName);
 					LabelField locationLabel = new LabelField("You're checked in @ " + BrightBerryMain.locationName);
 					BrightBerryMain.this.setStatus(locationLabel);
 				}
 			});
 	}
     
     public boolean onClose() {
 		UiApplication.getUiApplication().requestBackground();
 		return true;
 	}
     
     protected void onFocusNotify(boolean focus) {
     	if (this.settings.getAuthed() && this.settings.getAutoWhereAmI() && focus) {
     		Thread whereThread = new WhereAmIThread(this.screen);
 			whereThread.start();
     	}
     }
 }
