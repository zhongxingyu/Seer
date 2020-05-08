 package com.baixing.activity.test;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Instrumentation;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import org.athrun.android.framework.AthrunTestCase;
 import org.athrun.android.framework.utils.AthrunConnectorThread;
 import org.athrun.android.framework.utils.SleepUtils;
 import org.athrun.android.framework.viewelement.AbsListViewElement;
 import org.athrun.android.framework.viewelement.IViewElement;
 import org.athrun.android.framework.viewelement.ScrollViewElement;
 import org.athrun.android.framework.viewelement.TextViewElement;
 import org.athrun.android.framework.viewelement.ViewElement;
 import org.athrun.android.framework.viewelement.ViewGroupElement;
 import org.athrun.android.framework.viewelement.ViewUtils;
 
 import android.os.Build;
 import android.os.Environment;
 import android.os.IBinder;
 import android.text.InputType;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.TextView;
 
 import de.mindpipe.android.logging.log4j.LogConfigurator;
 
 public class BaixingTestCase extends BxBaseTestCase {
 	@SuppressWarnings("unchecked")
 	public BaixingTestCase() throws Exception {
 		super();
 	}
 	
 	public void logout() throws Exception {
 		openTabbar(TAB_ID_MY_TEXT);
 		openMyGridByText(MY_SETTING_BUTTON_TEXT);
 		if (findElementByText(MY_LOGIN_BUTTON_TEXT) != null) {
 			goBack();
 			return;
 		}
 		ViewElement el = findElementByText(MY_LOGOUT_BUTTON_TEXT);
 		if (el == null) return;
 		el.doClick();
 		sleep(1);
 		findElementByText(MY_LOGOUT_OK_BUTTON_TEXT, 0, true).doClick();
 		sleep(2);
 		goBack();
 	}
 	
 	public void logon() throws Exception {
 		openTabbar(TAB_ID_MY_TEXT);
 		openMyGridByText(MY_SETTING_BUTTON_TEXT);
 		ViewElement v = findElementByText(MY_LOGIN_BUTTON_TEXT);
 		if (v == null) {
 			goBack();
 			return;
 		}
 		v.doClick();
 		sleep(1);
 		ViewElement loginBtn = findElementById(MY_LOGIN_BUTTON_ID);
 		if (loginBtn != null) {
 			TextViewElement etAccount = findElementById(MY_LOGIN_USER_TEXTVIEW_ID, TextViewElement.class);
 			etAccount.setText(TEST_DATA_MOBILE);
 			TextViewElement etPwd = findElementById(MY_LOGIN_PASSWORD_TEXTVIEW_ID, TextViewElement.class);
 			etPwd.setText(TEST_DATA_PASSWORD);
 			
 			loginBtn.doClick();
 			try {
				assertEquals(true, waitForText(MY_LOGOUT_BUTTON_TEXT, 5000));
 			} catch (Exception ex) {
 				assertTrue("登录出现错误", false);
 			}
 		}
 	}
 	
 	public void openPostCategory(int firstCatIndex, int secondCatIndex) throws Exception {
 		openPostFirstCategory(firstCatIndex);
 		//selectMetaByIndex(secondCatIndex, POST_SECOND_CATEGORY_LISTVIEW_ID);
 		openSecondCategoryByIndex(secondCatIndex);
 	}
 	
 	public void openPostFirstCategory(int firstCatIndex) throws Exception {
 		if (findElementById(POST_FORM_MARK_ID) != null) {
 			TextViewElement tv = findElementByText(POST_CATEGORY_TEXT, 0, true);
 			if (tv != null) {
 				tv.doClick();
 				sleep(1);
 			}
 		}
 		
 		/*ViewElement v = findElementById(POST_CATEGORY_LIST_ITEM_ID);
 		//if (v != null) {
 			AbsListViewElement lv = findElementById(POST_CATEGORY_GRIDVIEW_ID, AbsListViewElement.class);
 			ViewElement iv = lv.getChildByIndex(firstCatIndex);
 			if (iv != null) {
 				iv.doClick();
 				waitForHideMsgbox(3000);
 			}
 		} else {
 			ViewGroupElement catTextView = getGridItemByIndex(firstCatIndex, POST_CATEGORY_GRIDVIEW_ID);
 			if (catTextView != null) {
 				catTextView.doClick();
 				waitForHideMsgbox(3000);
 			}
 		}*/
 		
 		AbsListViewElement lv = findListView();
 		if (lv != null) {
 			lv.getChildByIndex(firstCatIndex).doClick();
 			sleep(3);
 		}
 	}
 	
 	public TextViewElement openPostSecondCategory(int secondIndex) throws Exception {
 		AbsListViewElement lv = findListView();
 		if (lv != null) {
 			TextViewElement tv = lv.getChildByIndex(secondIndex + 1, TextViewElement.class);
 			if (tv == null) {
 				ViewGroupElement gv = lv.getChildByIndex(secondIndex + 1, ViewGroupElement.class);
 				assertNotNull(gv);
 				tv = findTextView(gv);
 				gv.doClick();
 			} else {
 				tv.doClick();
 			}
 			sleep(2);
 			if (tv != null) return tv;
 		}
 		return null;
 	}
 	
 	public void openPostItemByIndex(int index) throws Exception {
 		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
 		assertNotNull(postLayout);
 		ViewGroupElement listView = findElementById(POST_META_ITEM_ID, index, ViewGroupElement.class);
 		assertNotNull(listView);
 		listView.doClick();
 		sleep(2);
 	}
 	
 	public void openPostItemByName(String displayName) throws Exception {
 		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
 		assertNotNull(postLayout);
 		int index = 0;
 		ViewGroupElement dv = null;
 		int scrolled = 0;
 		boolean scrolledNull = true;
 		while(index < 20) {
 			try {
 				dv = findSelectMetaByIndex(index++, displayName);
 				
 				if (dv != null) {
 					Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + ":doClick");
 					dv.doClick();
 					sleep(2);
 					if (findElementById(POST_FORM_MARK_ID, ViewGroupElement.class) != null) {
 						Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + ":ScrollNext");
 						ScrollViewElement scrollView = getPostScrollView();
 						scrollView.scrollToNextScreen();
 						if (scrolled == 0) scrolled = index - 1;
 						sleep(2);
 						dv.doClick();
 						sleep(2);
 					}
 					return;
 				}
 			} catch (IndexOutOfBoundsException ex) {
 				if (scrolledNull == false) break;
 			}
 			if (scrolledNull == true) {
 				Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + ":WScrollNext");
 				ScrollViewElement scrollView = getPostScrollView();
 				scrollView.scrollToNextScreen();
 				sleep(2);
 				scrolledNull = false;
 				index--;
 			}
 		}
 		assertNotNull(dv);
 	}
 	
 	public ViewElement setOtherMetaByIndex(int index, String value) throws Exception {
 		try {
 			TextViewElement tv = findElementById(POST_META_EDITTEXT_ID, index, TextViewElement.class);
 			//assertNotNull(tv);
 			if (tv != null) {
 				tv.setText(value);
 				sleep(1);
 			}
 			return tv;
 		} catch (IndexOutOfBoundsException ex) {
 			return null;
 		}
 	}
 	
 	public void selectMetaByName(String displayName) throws Exception {
 		selectMetaByName(POST_META_LISTVIEW_ID, displayName);
 	}
 	public void selectMetaByName(String listViewId, String displayName) throws Exception {
 		TextViewElement v3 = findMetaByName(listViewId, displayName);
 		assertNotNull(v3);
 		Log.i(LOG_TAG, "postshow:v3" + v3.getText());
 		v3.doClick();
 		sleep(1);
 	}
 	
 	public TextViewElement findMetaByName(String listViewId, String displayName) throws Exception {
 		return findElementByViewId(listViewId, displayName);
 	}
 	
 	public void setMetaByName (String displayName, String value) throws Exception {
 		ViewGroupElement postLayout = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
 		assertNotNull(postLayout);
 		sleep(1);
 		ScrollViewElement lv = getPostScrollView();
 		int loop = 0;
 		ViewElement v = setMetaValueByName(displayName, value);
 		while(v == null) {
 			Log.i(LOG_TAG, "setOtherMetaByName:setval" + displayName);
 			lv.scrollToNextScreen();
 			sleep(2);
 			v = setMetaValueByName(displayName, value, 0);
 			if (loop++ > 5) break;
 		}
 		assertNotNull("displayName " + displayName + " value" + value, v);
 	}
 	
 	public ViewElement setMetaValueByName(String displayName, String value) throws Exception {
 		return setMetaValueByName(displayName, value, 0);
 	}
 	
 
 	public ViewGroupElement findSelectMetaByIndex(int index) throws Exception {
 		return findSelectMetaByIndex(index, null);
 	}
 	public ViewGroupElement findSelectMetaByIndex(int index, String displayName) throws Exception {
 		ViewGroupElement dv = findElementById(POST_META_ITEM_ID, index, ViewGroupElement.class);
 		Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index);
 		
 		if (dv != null) {
 			Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + POST_META_ITEM_ID);
 			TextViewElement nv = dv.findElementById(POST_META_ITEM_DISPLAY_ID, TextViewElement.class);
 			if (nv != null) Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + POST_META_ITEM_DISPLAY_ID + displayName + ":" + nv.getText());
 			if (nv != null) {
 				if (displayName != null && !nv.getText().equals(displayName)) return null;
 				Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + ":doClick");
 				return dv;
 			}
 		}
 		return null;
 	}
 	
 	public BXTextViewElement findTextMetaByIndex(int index, boolean enterRandVal) throws Exception {
 		return findTextMetaByIndex(POST_META_EDIT_ITEM_ID, index, null, enterRandVal);
 	}
 	
 	public BXTextViewElement findTextMetaByIndex(int index) throws Exception {
 		return findTextMetaByIndex(POST_META_EDIT_ITEM_ID, index, null, false);
 	}
 	
 	public BXTextViewElement findTextMetaByIndex(int index, String displayName) throws Exception {
 		return findTextMetaByIndex(POST_META_EDIT_ITEM_ID, index, displayName, false);
 	}
 	
 	public BXTextViewElement findTextMetaByIndex(String listItemId, int index) throws Exception {
 		return findTextMetaByIndex(listItemId, index, null, false);
 	}
 
 	public BXTextViewElement findTextMetaByIndex(String listItemId, int index, String displayName, boolean enterRandVal) throws Exception {
 		ViewGroupElement dv = findElementById(listItemId, index, ViewGroupElement.class);
 		Log.i(LOG_TAG, "setOtherMetaByName:" + index);
 		if (dv != null) {
 			Log.i(LOG_TAG, "setOtherMetaByName:" + index + POST_META_EDIT_ITEM_ID);
 			String editDisplayId = POST_META_EDIT_DISPLAY_ID;
 			String editTextId = POST_META_EDITTEXT_ID;
 			TextViewElement nv = dv.findElementById(editDisplayId, TextViewElement.class);
 			BXTextViewElement tv = null;
 			if (nv == null) {
 				editDisplayId = POST_META_EDIT_DISPLAY_DESC_ID;
 				editTextId = POST_META_EDITTEXT_DESC_ID;
 				tv = dv.findElementById(editTextId, BXTextViewElement.class);
 				nv = dv.findElementById(editDisplayId, TextViewElement.class);
 			}
 			if (nv != null) Log.i(LOG_TAG, "setOtherMetaByName:" + index + editDisplayId + nv.getText());
 			if (nv != null || tv != null) {
 				if (displayName != null && !nv.getText().equals(displayName)) return null;
 				Log.i(LOG_TAG, "setOtherMetaByName:" + index + editDisplayId + (displayName != null ? displayName : ""));
 				if (!editTextId.equals(POST_META_EDITTEXT_DESC_ID)) {
 					for(int i = 1; i < dv.getChildCount(); i++) {
 						ViewGroupElement ddv = dv.getChildByIndex(i, ViewGroupElement.class);
 						tv = ddv.findElementById(editTextId, BXTextViewElement.class);
 						if (tv != null) break;
 					}
 				}
 				if (tv != null) {
 					if (!enterRandVal || tv.getText().length() > 0) return tv;
 					String value = "";
 					if(tv.getInputType() == (
 							InputType.TYPE_CLASS_NUMBER 
 							| InputType.TYPE_NUMBER_FLAG_DECIMAL 
 							| InputType.TYPE_NUMBER_FLAG_SIGNED)) {
 						value = "" + (30 + random(10));
 					} else {
 						int randLen = 8 + random(12);
 						if (nv != null && nv.getText().equals("姓名")) randLen = 3;//TODO 营业员 描述字数
 						if (nv != null && nv.getText().equals("描述")) randLen = 8 + random(7);//TODO 营业员 描述字数
 						if (nv != null && nv.getText().equals("目的地")) randLen = 9;
 						if (nv != null && nv.getText().equals("联系电话")) {
 							value = TEST_DATA_MOBILE;
 						} else {
 							value = (new RandomHan()).getRandomString(randLen);
 						}
 					}
 					tv.setText(value);
 					return tv;
 				}
 			}
 		}
 		return null;
 	}
 	
 	public ViewElement setMetaValueByName(String displayName, String value, int index) throws Exception {
 		//TextViewElement dtv = findMetaByName(POST_SCROLLVIEW_PARENT_ID, displayName);
 		//assertNotNull(dtv);
 		while(index < 20) {
 			try {
 				TextViewElement tv = findTextMetaByIndex(index++, displayName);
 				if (tv != null) {
 					tv.setText(value);
 					sleep(1);
 					return tv;
 				}
 			} catch (IndexOutOfBoundsException e) {
 				Log.i(LOG_TAG, "setOtherMetaByName:IndexOutOfBoundsException" + index);
 				break;
 			}
 		}
 		return null;
 	}
 	
 	public TextViewElement findMetaValueViewByName(String displayName, int index) throws Exception {
 		//TextViewElement dtv = findMetaByName(POST_SCROLLVIEW_PARENT_ID, displayName);
 		//assertNotNull(dtv);
 		while(index < 20) {
 			try {
 				TextViewElement tv = findTextMetaByIndex(index++, displayName);
 				if (tv != null) {
 					return tv;
 				}
 			} catch (IndexOutOfBoundsException e) {
 				Log.i(LOG_TAG, "setOtherMetaByName:IndexOutOfBoundsException" + index);
 				break;
 			}
 		}
 		return null;
 	}
 	
 	public void doClickPostPhoto() throws Exception {
 		ViewGroupElement df = findElementById(POST_FORM_MARK_ID, ViewGroupElement.class);
 		assertNotNull(df);
 		BXImageViewElement iv = null;
 		/*for(int i = 0; i < df.getChildCount(); i++) {
 			int foundPhotoButton = 0;
 			//layout
 			ViewGroupElement ly = df.getChildByIndex(i, ViewGroupElement.class);
 			if (ly != null) Log.i(LOG_TAG, "doClickPostPhoto:" + i + "c:" + ly.getChildCount());
 			if (ly != null && ly.getChildCount() == 6) {
 				Log.i(LOG_TAG, "doClickPostPhoto:lx" + i);
 				//l1, l2, l3
 				for (int j = 0; j < 6; j++) {
 					ViewGroupElement lx = ly.getChildByIndex(j, ViewGroupElement.class);
 					Log.i(LOG_TAG, "doClickPostPhoto:lx" + i + "|" + j);
 					if (lx != null && lx.getChildCount() == 1) {
 						Log.i(LOG_TAG, "doClickPostPhoto:imageview" + i + "|" + j);
 						//ImageView
 						BXImageViewElement ivx = lx.getChildByIndex(0, BXImageViewElement.class);
 						if (ivx != null) {
 							//Log.i(LOG_TAG, "doClickPostPhoto:imageviex" + i + "|" + j);
 							if (j == 0) iv = ivx;
 							foundPhotoButton++;
 						}
 					}
 				}
 				if (foundPhotoButton == 3 && iv != null) {
 					break;
 				} else {
 					iv = null;
 				}
 			}
 		}*/
 		/*iv = findElementById(POST_META_IMAGEVIEW1_ID, BXImageViewElement.class);
 		if (iv == null) {
 			iv = findElementById(POST_META_DESC_IMAGEVIEW_ID, BXImageViewElement.class);
 		}*/
 		iv = findImageView("btn_add_picture");
 		if (iv != null) {
 			iv.doClick();
 			sleep(1);
 			if (findElementByText("完成添加") != null) {
 				iv = findImageView("btn_add_picture");
 				if (iv != null) {
 					iv.doClick();
 					sleep(1);
 				}
 			}
 			ViewElement v = findElementByText(POST_CAMERA_PHOTO_TEXT, 0, true);
 			assertNotNull(v);
 			//v.doClick();
 			sleep(1);
 			//getActivity();
 			//Instrumentation inst = getInstrumentation();
 			//this.getDevice().pressKeys(KeyEvent.KEYCODE_DPAD_CENTER);
 			//inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
 			//sleep(10);
 			//this.getDevice().pressKeys(KeyEvent.KEYCODE_DPAD_LEFT);
 			//this.getDevice().pressKeys(KeyEvent.KEYCODE_DPAD_LEFT);
 			//this.getDevice().pressKeys(KeyEvent.KEYCODE_DPAD_CENTER);
 			//this.getDevice().pressBack();
 			sleep(1);
 		}
 	}
 	
 	public ViewElement selectMetaByIndex(int index) throws Exception {
 		return selectMetaByIndex(index, true);
 	}
 	
 	public ViewElement selectMetaByIndex(int index, boolean asserted) throws Exception {
 		return selectMetaByIndex(index, POST_META_LISTVIEW_ID, asserted);
 	}
 	
 	public ViewElement selectMetaByIndex(int index, String listViewId, boolean asserted) throws Exception {
 		AbsListViewElement listView = findElementById(listViewId,
 				AbsListViewElement.class);
 		if (asserted) assertNotNull(listView);
 		if (listView == null) return null;
 		ViewGroupElement v = null;
 		try {
 			v = listView.getChildByIndex(index, ViewGroupElement.class);
 		} catch (IndexOutOfBoundsException e) {}
 		if (asserted) assertNotNull(v);
 		if (v == null) return null;
 		v.doClick();
 		sleep(1);
 		return v;
 	}
 	
 	public void selectAutoMeta() throws Exception {
 		ViewGroupElement lv = findElementById(POST_META_LISTVIEW_ID, ViewGroupElement.class);
 		if (lv == null) {
 			int loop = 10;
 			while (findElementById(POST_FORM_MARK_ID) == null) {
 				if (findElementByText("全部", 0, true) != null) {
 					findElementByText("全部", 0, true).doClick();
 					sleep(1);
 					return;
 				}
 				AbsListViewElement lv1 = findListView();
 				if (lv1 != null) {
 					int index = lv1.getChildCount() > 1 ? 1 : 0;
 					ViewElement gv = lv1.getChildByIndex(index, ViewElement.class);
 					gv.doClick();
 					sleep(1);
 				}
 				if (loop++ > 10) break;
 			}
 			//assertTrue("selectAutoMeta error", false);
 			return;
 		}
 		int i = 2;
 		while(i >= 0) {
 			ViewElement v = selectMetaByIndex(i--, false);
 			ViewGroupElement lv2 = findElementById(POST_META_LISTVIEW_ID, ViewGroupElement.class);
 			if (v == null || lv.equals(lv2)) {
 				continue;
 			}
 			break;
 		}
 	}
 	
 	public void postOtherDone() throws Exception {
 		ViewElement el = findElementByText(POST_DONE);
 		assertNotNull(el);
 		el.doClick();
 		sleep(1);
 	}
 
 	public String doPostByData(String[][] postData) throws Exception {
 		String title = postEnterData(postData);
 		postSend();
 		if (afterPostSend() == false) return "";
 		return title;
 	}
 	
 	public String postEnterData(String[][] postData) throws Exception {
 		String title = "";
 		for (int i = 0; i < postData.length; i++) {
 			METATYPE type = METATYPE.valueOf(postData[i][0]);
 			switch(type) {
 			case CATEGORY:
 				openPostFirstCategory(Integer.parseInt(postData[i][1]));
 				openSecondCategoryByName(postData[i][2]);
 				break;
 			case MULTISELECT:
 			case SELECT:
 				openPostItemByName(postData[i][1]);
 				String[] metaNames = postData[i][2].split(",");
 				for (int j = 0; j < metaNames.length; j++) {
 					selectMetaByName(metaNames[j]);
 				}
 				if (type == METATYPE.MULTISELECT) postOtherDone();
 				break;
 			case TEXT:
 			case TITLE:
 				String txtVal = postData[i][2];
 				if (type == METATYPE.TITLE) {
 					title = txtVal + String.valueOf(random(1000000));
 					setMetaByName(postData[i][1], title);
 				} else {
 					setMetaByName(postData[i][1], txtVal);
 				}
 				break;
 			}
 		}
 		return title;
 	}
 	
 	public void postAutoEnterData(boolean photo) throws Exception {
 		int index = 0;
 		String value = "";
 		int loop = 0;
 		//String firstVal = null;
 		TextViewElement descV = findElementById(POST_META_EDITTEXT_DESC_ID, TextViewElement.class);
 		if (descV != null) {
 			descV.setText( (new RandomHan()).getRandomString(16));
 		}
 		while(index < 15) {
 			try {
 				BXTextViewElement tv = findTextMetaByIndex(index++, true);
 				if (tv != null) {
 					//if (index == 1) {
 					//	if (firstVal != null && firstVal.equals(tv.getText())) break;
 					//}
 					
 					//if (index == 1) firstVal = value;
 					sleep(1);
 				} else {
 					throw new IndexOutOfBoundsException("" + index);
 				}
 			} catch (IndexOutOfBoundsException e) {
 				
 				Log.i(LOG_TAG, "findTextMetaByIndex:IndexOutOfBoundsException" + index);
 				if (loop++ > 1) break;
 				index = 0;
 				ScrollViewElement lv = getPostScrollView();
 				lv.scrollToNextScreen();
 				sleep(1);
 				//this.scrollBottom(1, POST_SCROLLVIEW_PARENT_ID);
 			}
 		}
 		
 		scrollTop(1, POST_SCROLLVIEW_PARENT_ID);
 		index = 0;
 		loop = 0;
 		String firstTitle = null;
 		boolean categoryFound = false; //todo
 		while(index < 10) {
 			try {
 				ViewGroupElement dv = findSelectMetaByIndex(index++);
 				if (dv != null) {
 					TextViewElement tt = dv.findElementById(POST_META_ITEM_DISPLAY_ID, TextViewElement.class);
 					if (index == 1) {
 						if (tt != null) {
 							if (firstTitle != null && firstTitle.equals(tt.getText())) break;
 							firstTitle = tt.getText();
 						}
 					}
 					if (tt != null && tt.getText().equals(POST_CATEGORY_TEXT)) {
 						if (!categoryFound) { //todo
 							categoryFound = true;
 							continue;
 						}
 					}
 					
 					Log.i(LOG_TAG, "setOtherMetaByName:openPostItemByName" + index + ":doClick");
 					dv.doClick();
 					sleep(2);
 					if (findElementById(POST_FORM_MARK_ID, ViewGroupElement.class) == null) {
 						ViewGroupElement lv = findElementById(POST_META_LISTVIEW_ID, ViewGroupElement.class);
 						selectAutoMeta();
 						sleep(2);
 						ViewGroupElement lv2 = findElementById(POST_META_LISTVIEW_ID, ViewGroupElement.class);
 						if (lv2 != null) {
 							boolean done = false;
 							if (lv.getId() == lv2.getId()) {
 								ViewElement el = findElementByText(POST_DONE);
 								if (el != null) done = true;
 							}
 							if (done) {
 								postOtherDone();
 								sleep(1);
 							} else {
 								ViewGroupElement lv3 = findElementById(POST_META_LISTVIEW_ID, ViewGroupElement.class);
 								selectAutoMeta();
 								if (lv3 != null && lv2.equals(lv3)) {
 									selectAutoMeta();
 								}
 								sleep(2);
 							}
 						}
 					}
 				} else {
 					throw new IndexOutOfBoundsException("" + index);
 				}
 			} catch (IndexOutOfBoundsException e) {
 				Log.i(LOG_TAG, "findSelectMetaByIndex:IndexOutOfBoundsException" + index);
 				if (loop++ > 1) break;
 				index = 0;
 				//ScrollViewElement lv = getPostScrollView();
 				AbsListViewElement lv = findListView();
 				if (lv != null) {
 					lv.scrollToNextScreen();
 					sleep(1);
 				}
 				//this.scrollBottom(1, POST_SCROLLVIEW_PARENT_ID);
 			}
 		}
 		if (photo) {
 			waitPhoto();
 		}
 		
 	}
 	
 	public boolean postSend() throws Exception {
 		return postSend(true);
 	}
 	
 	public boolean postSend(boolean asserted) throws Exception {
 		ViewElement eld = findElementByText(POST_SEND);
 		if (asserted) assertNotNull(eld);
 		if (eld == null) return false;
 		eld.doClick();
 		//waitForHideMsgbox(10 * 1000);
 		return waitForSubTexts("发布成功@百姓网公约@重复@超限@失败", 5000);
 	}
 	
 	public boolean checkPostSuccess(boolean deleted) throws Exception {
 		//if (checkPostSuccess(MY_LISTING_MYAD_TEXT, deleted)) return true;
 		//return checkPostSuccess(MY_LISTING_MYAD_APPROVE_TEXT, deleted);
 	//}
 	//public boolean checkPostSuccess(String gridText, boolean deleted) throws Exception {
 		if (findElementById(POST_FORM_MARK_ID) != null) {
 			return false;
 		}
 		sleep(2);
 		ViewElement dt = findElementByText("是否用百姓网帐号");
 		if (dt != null) {
 			ViewElement d = findElementByText(MY_BIND_DIALOG_NO_BUTTON_ID, 0, true);
 			if (d != null) {
 				d.doClick();
 				sleep(1);
 			}
 		}
 		if (findElementByText("个人中心") != null && findElementByText("我的信息") != null) {
 			findElementByText("我的信息").doClick();
 			sleep(1);
 			
 		}
 		//if (gridText != null) openMyGridByText(gridText);
 		ViewGroupElement gv = openAdByItemIndex(0);
 		if (gv != null) {
 			if (deleted) {
 				deleteAdOnView(false);
 				deleteAllAds();
 			} else {
 				goBack();
 			}
 			return true;
 		}
 		return true;
 	}
 	
 	public boolean afterPostSend() throws Exception {
 		ViewElement eld = findElementByText(POST_SEND);
 		if (eld != null) {
 			goBack();
 			ViewElement dt = findElementByText("提示", 0, true);
 			if (dt != null) {
 				ViewElement dt2 = findElementByText("确认退出？", 0, true);
 				if (dt2 != null) {
 					ViewElement d = findElementByText(MY_BIND_DIALOG_NO_BUTTON_ID, 0, true);
 					if (d != null) {
 						d.doClick();
 					}
 				} else {
 					ViewElement d = findElementByText(POST_BACK_DIALOG_OK_BUTTON_ID, 0, true);
 					if (d != null) {
 						d.doClick();
 					}
 				}
 			}
 			goBack();
 			goBack();
 			return false;
 		} else {
 			TextViewElement tv = findElementByText("确定", 0, true);
 			if (tv != null) {
 				tv.doClick();
 				sleep(1);
 			}
 		}
 		return true;
 	}
 	
 	public void openHomeCategoryByIndex(int index) throws Exception {
 		openTabbar(TAB_ID_HOME_TEXT);
 		boolean hv = findElementByTexts(HOME_MARK_TEXTS);
 		int i = 0;
 		while(!hv && i++ < 5) {
 			goBack();
 			openTabbar(TAB_ID_HOME_TEXT);
 			hv = findElementByTexts(HOME_MARK_TEXTS);
 		}
 		assertTrue(hv);
 		TextViewElement catTextView = getGridItemByIndex(index, CATEGORY_GRIDVIEW_ID);
 		if (catTextView != null) {
 			catTextView.doClick();
 			sleep(2);
 		}
 		
 	}
 	
 	public void openSecondCategoryByIndex(int index) throws Exception {
 		AbsListViewElement subCatListView = findElementById(CATEGORY_SECOND_GRIDVIEW_ID,
 				AbsListViewElement.class);
 		assertNotNull(subCatListView);
 		ViewGroupElement subCatView = subCatListView.getChildByIndex(index,
 				ViewGroupElement.class);
 		subCatView.doClick();
 		sleep(1);
 		waitForMsgBox(MSGBOX_SETTING_VIEWTYPE_NO_PIC_TEXT, MSGBOX_SETTING_VIEWTYPE_CANCEL_BUTTON_ID, 3000);
 		waitForHideMsgbox(10000);
 	}
 	
 	public void openCategoryByIndex(int firstCatIndex, int secondCatIndex) throws Exception {
 		openHomeCategoryByIndex(firstCatIndex);
 		openSecondCategoryByIndex(secondCatIndex);
 	}
 	
 	public void openSecondCategoryByName(String name) throws Exception {
 		/*TextViewElement subCatView = findElementByViewId(CATEGORY_SECOND_GRIDVIEW_ID, name);
 		assertNotNull(subCatView);
 		subCatView.doClick();*/
 		if (findElementById(POST_FORM_MARK_ID) == null) {
 			AbsListViewElement lv = findListView();
 			if (lv != null) {
 				int loop = 0;
 				while(loop++ < 50) {
 					TextViewElement tv = findElementByText(name, 0, true);
 					if (tv != null) {
 						tv.doClick();
 						break;
 					}
 					lv.scrollToNextScreen();
 				}
 				sleep(2);
 			}
 		}
 		waitForHideMsgbox(10000);
 	}
 	
 	public ViewGroupElement openAdByItemIndex(int index) throws Exception {
 		return openAdByItemIndex(null, index);
 	}
 	
 	public ViewGroupElement openAdByItemIndex(AbsListViewElement lv, int index) throws Exception {
 		ViewElement v = clickListViewByIndex(lv, index);
 		if (v != null) {
 			return castObject(v, ViewGroupElement.class);
 		}
 		return null;
 	}
 	
 	public ViewGroupElement openAdByIndex(AbsListViewElement lv, int index) throws Exception {
 		return openAdByItemIndex(lv, index);
 	}
 	
 	/*public ViewGroupElement openAdByIndex(int index, String viewListId, AbsListViewElement avl) throws Exception {
 		ViewGroupElement avi = avl.getChildByIndex(0, ViewGroupElement.class);
 		int i = 0;
 		int j = 0;
 		while (avi != null) {
 			ViewGroupElement _avi_ = avi.findElementById(AD_VIEWLIST_MARK_ID, ViewGroupElement.class);
 			if (_avi_ == null) {
 				avl.scrollToNextScreen();
 				sleep(1);
 				avi = avl.getChildByIndex(j, ViewGroupElement.class);
 				_avi_ = avi.findElementById(AD_VIEWLIST_MARK_ID, ViewGroupElement.class);
 				if (_avi_ == null) {
 					avi = avl.getChildByIndex(++j, ViewGroupElement.class);
 					continue;
 				}
 			}
 			if (i++ == index) {
 				avi.doClick();
 				sleep(1);
 				break;
 			}
 			avi = avl.getChildByIndex(++j, ViewGroupElement.class);
 		}
 		int indexSize = 6;
 		int pageSize = (int) (index / indexSize); //每页6个
 		int i = 1;
 		if (avl == null && viewListId != null) avl = findElementById(viewListId, AbsListViewElement.class);
 		while (i++ < pageSize) {
 			if (avl != null) avl.scrollToNextScreen();
 			sleep(1);
 		}
 		ViewGroupElement avi = null;
 		int loop = index % indexSize;
 		if (loop <= 0) loop = 1;
 		while(loop > 0) {
 			try {
 				avi = findElementById(AD_VIEWLIST_MARK_ID, loop--, ViewGroupElement.class);
 				if (avi != null) {
 					ViewElement v = avi.findElementById(AD_VIEWLIST_ITEM_TITLE_ID);
 					if (v == null) {
 						if (avl != null) avl.scrollToNextScreen();
 						sleep(1);
 						continue;
 					}
 					v = avi.findElementById(AD_VIEWLIST_ITEM_DATE_ID);
 					if (v == null) {
 						if (avl != null) avl.scrollToNextScreen();
 						sleep(1);
 						continue;
 					}
 					avi.doClick();
 					sleep(1);
 					Log.i(LOG_TAG, "Start do Rand Ad.index openAdByIndex:" + loop + "pageSize:" + pageSize);
 					//avl = findElementById(viewListId, AbsListViewElement.class);
 					//if (avl == null) goBack();
 					break;
 				}
 			} catch (IndexOutOfBoundsException ex) {}
 		}
 		return avi;
 	}
 	*/
 	
 	public BXViewGroupElement showAd(int firstCatIndex, int secondCatIndex, int index) throws Exception {
 		openTabbar(TAB_ID_MY_TEXT);
 		logon();
 		openTabbar(TAB_ID_HOME_TEXT);
 		openCategoryByIndex(firstCatIndex, secondCatIndex);
 		sleep(1);
 		assertNotNull(openAdByIndex(null, index));
 		BXViewGroupElement detailView = findElementById(AD_DETAILVIEW_ID,
 				BXViewGroupElement.class);
 		return detailView;
 	}
 	
 	public void openAdWithPic(boolean havPic) throws Exception {
 		AbsListViewElement avl = findElementById(AD_VIEWLIST_ID, AbsListViewElement.class);
 		assertNotNull(avl);
 		/*for (int i = 0; i < 20; i++) {
 			assertNotNull(openAdByIndex(i));
 			sleep(2);
 			/ *ViewGroupElement ilv = findElementById(AD_DETAILVIEW_DESC_ID, ViewGroupElement.class);
 			ViewGroupElement v = ilv.findElementById(AD_IMAGES_VIEWLIST_ID, ViewGroupElement.class);
 			Log.i(LOG_TAG, "openAdWithPic:ilv" + v.getChildCount());
 			if ((havPic && v != null) || (!havPic && v == null)) {
 				break;
 			}* /
 			if(showAdPic(0)) {
 				goBack();
 				if (havPic) break;
 			} else if (!havPic) break;
 			goBack();
 		}*/
 		ViewGroupElement avi = null;
 		int loop = 0;
 		for(int i = 0; i < 20; i++) {
 			try {
 				avi = findElementById(AD_VIEWLIST_MARK_ID, i, ViewGroupElement.class);
 				Log.i(LOG_TAG, "openAdWithPic:ilv" + avi.getChildCount());
 				if (avi != null) {
 					ViewElement v = avi.findElementById(AD_VIEWLIST_ITEM_TITLE_ID);
 					Log.i(LOG_TAG, "openAdWithPic:ilv v1");
 					if (v == null) {
 						avl.scrollToNextScreen();
 						sleep(1);
 						continue;
 					}
 					v = avi.findElementById(AD_VIEWLIST_ITEM_DATE_ID);
 					Log.i(LOG_TAG, "openAdWithPic:ilv v2");
 					if (v == null) {
 						avl.scrollToNextScreen();
 						sleep(1);
 						continue;
 					}
 					Log.i(LOG_TAG, "openAdWithPic:ilv v3");
 					BXImageViewElement iv = avi.findElementById(AD_VIEWLIST_ITEM_IMAGE_ID, BXImageViewElement.class);
 					if (iv == null) continue;
 					Log.i(LOG_TAG, "openAdWithPic:ilv v4" + iv.getTag());
 					//if (!iv.checkImageByName(AD_VIEWLIST_ITEM_IMAGE_IMG)) continue;  //TODO
 					if (iv.getTag() == null || iv.getTag().length() == 0) {
 						if (havPic) continue; 
 					} else {
 						if (!havPic) continue;
 					}
 					Log.i(LOG_TAG, "openAdWithPic:ilv v5");
 					avi.doClick();
 					sleep(1);
 					break;
 				}
 			} catch (IndexOutOfBoundsException ex) {
 				if (loop++ > 5) break;
 				avl.scrollToNextScreen();
 				sleep(1);
 				i = 0;
 			}
 		}
 	}
 	
 	public TextViewElement findDetailViewMetaByName(String metaName) throws Exception {
 		for(int i = 0; i < 20; i++) {
 			TextViewElement v = findElementById(AD_DETAIL_META_LABEL_ID, i, TextViewElement.class);
 			if (v != null && v.getText().contains(metaName)) {
 				return findElementById(AD_DETAIL_META_VALUE_ID, i, TextViewElement.class);
 			}
 		}
 		return null;
 	}
 	
 	public ViewElement scrollAdListViewToFooter() throws Exception {
 		return scrollAdListViewToFooter(null);
 	}
 	
 	public ViewElement scrollAdListViewToFooter(AbsListViewElement lv) throws Exception {
 		if (lv == null) lv = findListView();
 		assertNotNull(lv);
 		//BXViewGroupElement bv = findElementById(lv.getId(), BXViewGroupElement.class);
 		BXViewGroupElement bv = castObject(lv, BXViewGroupElement.class);
 		assertNotNull("adlisting ershoujiaoche listView not found", lv);
 
 		//TextViewElement tv = findTextView("点击加载下30条", false);
 		//assertNotNull(tv);
 		//检查底部提示：点击载入下30条
 		//向下拖动
 		//向下浏览30个信息
 		int from = bv.getHeight() / 2 + bv.getHeight() /3;
 		for(int i = 0; i < 50; i++) {
 			//bv = findElementById(AD_VIEWLIST_ID, BXViewGroupElement.class);
 			//assertNotNull(bv);
 			bv.scrollByY(from, from - ((i < 14) ? 200 : 50));
 			//if (i < 8) continue;
 			//ViewElement v = findTextView("点击加载下30条");//findElementById(tv.getId());
 			ViewElement v = findElementByText("点击加载下30条");
 			if (v != null) {
 				//检查底部提示：点击载入下30条
 				Log.i(LOG_TAG, "testViewListing:more click");
 			    //向下拖动
 				//检查底部提示：点击载入下30条
 				return v;
 			} else {
 				v = findElementByText("正在加载更多，请耐心等待");
 				if (v != null) {
 					return v;
 				}
 			}
 		}
 		return null;
 	}
 	
 	protected void adViewPicTouch() throws Exception {
 		//查看第一个图片
 		if (showAdPic(0)) {
 			//Log.i(LOG_TAG, "pic:0");
 			goBack(false);
 			//滚动图片
 			TextViewElement titleView = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class);
 			BXViewGroupElement ilv = findElementById(AD_IMAGES_VIEWLIST_ID, BXViewGroupElement.class);
 			ilv.doTouch(-200);
 			if (titleView != null && !titleView.getText().equals(findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText())) {
 				ilv.doTouch(200);
 			}
 			sleep(1);
 			//查看第二个图片
 			if (showAdPic(1)) {
 				//Log.i(LOG_TAG, "pic:1");
 				goBack(false);
 				//滚回图片
 				titleView = findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class);
 				ilv = findElementById(AD_IMAGES_VIEWLIST_ID, BXViewGroupElement.class);
 				ilv.doTouch(200);
 				if (titleView != null && !titleView.getText().equals(findElementById(AD_DETAILVIEW_TITLE_ID, TextViewElement.class).getText())) {
 					ilv.doTouch(-200);
 				}
 				sleep(1);
 				//Log.i(LOG_TAG, "pic:touch0");
 				
 				//滚动大图
 				showAdPic(0);
 				sleep(1);
 				showNextView(AD_BIG_IMAGE_VIEW_ID);
 				//Log.i(LOG_TAG, "pic:touch1");
 				goBack();
 			}
 		}
 	}
 	
 	public boolean showAdPic(int index) throws Exception {
 		ViewGroupElement ilv = findElementById(AD_IMAGES_VIEWLIST_ID, ViewGroupElement.class);
 		if (ilv == null) return false;
 		ViewElement iv = ilv.getChildByIndex(index);
 		if (iv == null) return false;
 		iv.doClick();
 		sleep(1);
 		return (findElementById(AD_BIG_IMAGE_VIEW_ID) != null);
 	}
 	
 	public void openMyGridByText(String text) throws Exception {
 		openTabbar(TAB_ID_MY_TEXT);
 		TextViewElement textView = null;
 		//textView = getGridItemByText(text, CATEGORY_GRIDVIEW_ID);
 		textView = findElementByText(text);
 		if (textView != null) {
 			textView.doClick();
 			sleep(2);
 		}
 	}
 	
 	public void openGridByText(String text) throws Exception {
 		TextViewElement textView = getGridItemByText(text, POST_CATEGORY_GRIDVIEW_ID);
 		if (textView != null) {
 			textView.doClick();
 			sleep(2);
 		}
 	}
 	
 	
 	public void deleteAllAds() throws Exception {
 		deleteAllAds(null);
 	}
 	
 	public void deleteAllAds(String gridText) throws Exception {
 		if (gridText != null) openMyGridByText(gridText);
 		
 		/*sleep(1);
 		ViewElement ele = findElementByText(MY_EDIT_BUTTON_ID);
 		if (ele == null) return;
 		ele.doClick();
 		sleep(1);
 		ViewElement eld = findElementByText(MY_DELETE_ALL_BUTTON_ID);
 		if (eld == null) return;
 		eld.doClick();*/
 		int maxLoop = 100;
 		ViewGroupElement gv = openAdByItemIndex(0);
 		while(gv != null) {
 			deleteAdOnView(false);
 			sleep(2);
 			gv = openAdByItemIndex(0);
 			if (maxLoop-- < 0) break;
 		}
 		
 		getDevice().pressBack();
 		sleep(1);
 	}
 	
 	public void deleteAdByText(String keyword) throws Exception {
 		//if (showMyAdList(MY_LISTITEM_MYAD_ID, MY_LISTING_MYAD_COUNTER_ID) <= 0) return;
 		ViewElement d = findElementByText(MY_BIND_DIALOG_NO_BUTTON_ID, 0, true);
 		if (d != null) {
 			d.doClick();
 		}
 		sleep(2);
 		ViewElement v = findElementByText(keyword, 0, true);
 		if (v == null) return;
 		
 		v.doClick();
 		sleep(1);
 		deleteAdOnView(true);
 	}
 	
 	public void deleteAdByIndex(int index) throws Exception {
 		ViewElement d = findElementByText(MY_BIND_DIALOG_NO_BUTTON_ID, 0, true);
 		if (d != null) {
 			d.doClick();
 			sleep(1);
 		}
 		ViewGroupElement gv = openAdByItemIndex(index);
 		if (gv != null) {
 			sleep(1);
 			deleteAdOnView(false);
 			sleep(1);
 		}
 	}
 	
 	public void deleteAdOnView(boolean force) throws Exception {
 		ViewElement mv = findElementByText(MSGBOX_TITLE_TEXT, 0, true);
 		if (mv != null) {
 			mv = findElementByText(MY_VIEWLIST_SHENSU_BUTTON_TEXT, 0, true);
 			if (mv != null) {
 				if (mv != null) {
 					mv = findElementByText(MY_VIEWLIST_DELETE_BUTTON_TEXT, 0, true);
 					if (mv != null) {
 						if (force){
 							mv.doClick();
 							sleep(1);
 							return;
 						}
 					}
 				}
 			}
 		}
 		if (force) {
 			ViewElement vd = findElementById(MY_DETAILVIEW_DELETE_BUTTON_ID);
 			if (vd == null) {
 				goBack();
 				return;
 			}
 			vd.doClick();
 			sleep(1);
 			findElementByText(DIALOG_OK_BUTTON_TEXT, 0, true).doClick();
 			sleep(1);
 			goBack();
 			//goBack();
 		} else {
 			goBack();
 			sleep(2);
 			ViewElement t = findElementByText("确定", 0, true);
 			if (t != null ) {
 				t.doClick();
 				sleep(1);
 			}
 			//列表上的 DELETE UPDATE 小按钮
 			int i = 0;
 			while(true) {
 				ViewElement delv = findElementById(MY_VIEWLIST_DELXUPDATE_BUTTON_ID);
 				if (delv != null) {
 					delv.doClick();
 					break;
 				}
 				sleep(1);
 				if (i == 5 && findElementByText(MSGBOX_TITLE_TEXT, 0, true) != null) {
 					goBack();
 				}
 				if (i++ > 20) break;
 			}
 			sleep(1);
 			ViewElement delButton = findElementByText("删除", 0, true);
 			if (delButton != null) {
 				if (findElementByText(MSGBOX_CANCEL_TEXT, 0, true) != null 
 						&& findElementByText("操作", 0, true) != null) {
 					delButton.doClick();
 					sleep(1);
 					if (findElementByText(MSGBOX_CANCEL_TEXT, 0, true) != null 
 							&& findElementByText("提醒", 0, true) != null) {
 						delButton = findElementByText("确定", 0, true);
 						if (delButton != null) {
 							delButton.doClick();
 							sleep(1);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void doSearch(String keyword) throws Exception {
 		if (findElementById(SEARCH_MARK_BUTTON_ID) != null) {
 			findElementById(SEARCH_MARK_BUTTON_ID).doClick();
 			sleep(1);
 		} else if (findElementById(AD_VIEWLIST_MARK_ID) != null && findElementById(SEARCH_BUTTON_ID) == null) {
 			ViewElement btnSearch = findElementByText(SEARCH_BUTTON_TEXT, 0, true);
 			if (btnSearch != null) {
 				btnSearch.doClick();
 				sleep(1);
 			}
 		}
 		if (keyword.length() > 0) {
 			TextViewElement etSearchText = findElementById(SEARCH_TEXTVIEW_ID,
 					TextViewElement.class);
 			etSearchText.setText(keyword);
 			assertEquals(keyword, etSearchText.getText());
 
 			//findElementById(SEARCH_BUTTON_ID).doClick();
 			getDevice().pressKeys(KeyEvent.KEYCODE_SEARCH);
 			getDevice().pressKeys(KeyEvent.KEYCODE_ENTER);
 			//etSearchText.inputText(KeyEvent.KEYCODE_ENTER);
 			sleep(2);
 			clickSearchCategoryList();
 		}
 	}
 	
 	public void selectSearch(String keyword) throws Exception {
 		doSearch("");
 		ViewElement iv = findElementByText(keyword, 0, true);
 		if (iv != null) {
 			iv.doClick();
 			sleep(2);
 			clickSearchCategoryList();
 
 		}
 	}
 	public void clickSearchCategoryList() throws Exception {
 		AbsListViewElement vl = findElementById(SEARCH_CATEGORY_RESULT_ID, AbsListViewElement.class);
 		if (vl != null) {
 			ViewGroupElement v = vl.getChildByIndex(0, ViewGroupElement.class);
 			if (v != null) {
 				v.doClick();
 				sleep(1);
 				waitForMsgBox(MSGBOX_SETTING_VIEWTYPE_NO_PIC_TEXT, MSGBOX_SETTING_VIEWTYPE_CANCEL_BUTTON_ID, 3000);
 			}
 		}
 	}
 	
 	public void setAdListingViewType(String viewType) throws Exception {
 		openTabbar(TAB_ID_MY_TEXT);
 		openMyGridByText(MY_SETTING_BUTTON_TEXT);
 		selectMetaByName(null, MY_SETTING_VIETTYPE_TEXT);
 		//点击图片模式
 		ViewElement v = findElementByText(viewType, 0, true);
 		assertNotNull(v);
 		v.doClick();
 	}
 	
 
 	private void waitPhoto() throws Exception {
 		if (Build.VERSION.SDK_INT < 11) {
 			waitGallery();
 			return;
 		}
 		//点击拍照按钮
 		doClickPostPhoto();
 		//检查弹出页，包含“相册”“拍照”“取消”
 		//选择拍照button
 		clickByText(POST_CAMERA_PHOTO_TEXT);//TODO 手机没有自带返回键或无效
 		//检查拍照页面弹出
 		//点击手机自带的返回键
 		sleep(1);
 		//waitClickCamera();
 		int x = 400, y = 710;
 		if (Build.VERSION.SDK_INT >= 11) { //Build.VERSION_CODES.HONEYCOMB
 			//Log.i(LOG_TAG, "waitClickCamera:device " + Build.MODEL);
 			x= 250; y = 710;
 			if (Build.MODEL.equals("Nexus 7")) {
 				if (Build.VERSION.SDK_INT >= 17) {
 					x = 550; y = 1850;
 				} else {
 					x = 550; y = 1810;
 				}
 			}
 		}
 		waitClickXY(x, y);
 		x = 400; y = 710;
 		if (Build.VERSION.SDK_INT >= 11) { //Build.VERSION_CODES.HONEYCOMB
 			//Log.i(LOG_TAG, "waitClickCamera:device " + Build.MODEL);
 			x= 400; y = 710;
 			if (Build.MODEL.equals("Nexus 7")) {
 				if (Build.VERSION.SDK_INT >= 17) {
 					x = 1100; y = 1850;
 				} else {
 					x = 1200; y = 1810;
 				}
 			}
 		}
 		waitClickXY(x, y);
 		sleep(5);
 		clickByText("完成添加");
 	}
 	
 	private void waitGallery() throws Exception {
 		//点击拍照按钮
 		doClickPostPhoto();
 		//检查弹出页，包含“相册”“拍照”“取消”
 		//选择拍照button
 		clickByText(POST_GALLERY_PHOTO_TEXT);//TODO 手机没有自带返回键或无效
 		//检查拍照页面弹出
 		//点击手机自带的返回键
 		sleep(1);
 		//waitSendKey(KeyEvent.KEYCODE_BACK);
 		int x = 100, y = 110;
 		if (Build.VERSION.SDK_INT >= 11) { //Build.VERSION_CODES.HONEYCOMB
 			if (Build.MODEL.equals("Nexus 7")) {
 				waitSendKey(KeyEvent.KEYCODE_BACK);
 				return;
 			}
 		}
 		waitClickXY(x, y);
 		sleep(5);
 	}
 	
 	public TextViewElement savePhoto(int first, int second) throws Exception {
 		openTabbar(TAB_ID_HOME_TEXT);
 		openCategoryByIndex(first, second);
 
 		sleep(3);
 		//选择一个带图信息进入
 		openAdWithPic(true);
 		//点击图片
 		showAdPic(0);
 		//检查title右侧包含button“保存”
 		TextViewElement v = findElementByText(AD_BIG_IMAGE_SAVE_TEXT);
 		//点击左上方button返回
 		goBack(true);
 		
 		//点击图片再次进入
 		showAdPic(0);
 		//点击右上方按钮保存
 		if (v != null) v.doClick();
 		return v;
 	}
 
 }
