 package com.titanium.ebaybottom.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Cookie;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import com.titanium.ebaybottom.model.Item;
 import com.titanium.ebaybottom.model.Pair;
 import com.titanium.ebaybottom.selenium.WebWindow;
 import com.titanium.ebaybottom.selenium.WebdriverHelper;
 import com.titanium.ebaybottom.ui.TextIO;
 import com.titanium.ebaybottom.ui.UI;
 
 public class PrivateMessage {
 	private static final String ITEM_MESSAGE_BASE = "http://contact.ebay.com/ws/eBayISAPI.dll?ShowSellerFAQ&";
 	private static final String PERSONAL_MESSAGE_BASE = "http://contact.ebay.com/ws/eBayISAPI.dll?ContactUserNextGen&recipient=";
 
 	private static final String CAPTCHA_ID = "tokenText";
 	private static final String PRIVATEMESSAGE_TITLE_ID = "qusetOther_cnt_cnt";
 	private static final String PRIVATEMESSAGE_BODY_ID = "msg_cnt_cnt";
 
 	public static List<Item> items = new ArrayList<>();
 	public static List<Pair<String, String>> messages = new ArrayList<>();
 	private static WebDriver driver;
 
 	public static void addToMessageQue(Item selectedItem,
 			Pair<String, String> msg) {
 		UI.printDebug("MSG QUEUE ADD:" + selectedItem.toString() + " " + msg);
 		items.add(selectedItem);
 		messages.add(msg);
 	}
 
 	public static void sendMessagesInQueue(
 			Pair<String, String> selectedUserAccount) {
 
 		UI.printUI("Starting private message sending setup...");
 
 		// if (driver == null)
 		driver = Network.logIn(selectedUserAccount);
 
 		for (int i = 0; i < items.size(); i++) {
 
 			Item selectedItem = items.get(i);
 			Pair<String, String> selectedMessage = messages.get(i);
//			if (!driver.getCurrentUrl().contains(
//					"contact.ebay.com/ws/eBayISAPI.dll?")) {
//				driver.get(composeMessageUrl(selectedItem));
//			} else {
 				new WebWindow(driver, composeMessageUrl(selectedItem));
//			}
 
 			// select other
 			// <input type="radio" style="margin-top:0px;margin-top:-2px\9;"
 			// name="cat" value="5" id="Other">
 			WebElement radio = WebdriverHelper.findElementById(driver, "Other");
 			if (radio != null && !radio.isSelected())
 				radio.click();
 
 			// continue button
 			// <input type="submit" value="Continue" class="btn btn-m btn-prim"
 			// style="width:109px;height:33px">
 			WebElement continueBtn = WebdriverHelper.findElementById(driver,
 					"continueBtnDiv");
 			if (continueBtn != null)
 				continueBtn.submit();
 
 			// title
 			// <input id="qusetOther_cnt_cnt" class="gryFnt" type="text"
 			// name="qusetOther_cnt_cnt" size="50" maxlength="32" value=""
 			// style="display: inline;">
 			// if (findElementById(driver, "qusetOther_cnt_cnt") != null)
 			// findElementById(driver, "qusetOther_cnt_cnt").sendKeys(
 			// selectedMessage.getKey());
 
 			if (WebdriverHelper
 					.findElementById(driver, PRIVATEMESSAGE_TITLE_ID) != null) {
 				WebdriverHelper.setInstantKeys(driver, PRIVATEMESSAGE_TITLE_ID,
 						selectedMessage.getKey());
 				WebdriverHelper
 						.findElementById(driver, PRIVATEMESSAGE_TITLE_ID)
 						.click();
 			}
 
 			// message
 			// <textarea id="msg_cnt_cnt" class="gryFnt"
 			// defval="Enter your question here..." name="msg_cnt_cnt" cols="70"
 			// rows="5" style="display: inline;"></textarea>
 			// if (findElementById(driver, "msg_cnt_cnt") != null)
 			// findElementById(driver, "msg_cnt_cnt").sendKeys(
 			// selectedMessage.getValue());
 			if (findMessageBodyTextbox() != null) {
 				WebdriverHelper.setInstantKeys(
 						driver,
 						PRIVATEMESSAGE_BODY_ID,
 						selectedMessage.getValue().replace(
 								Config.PERSONAL_MESSAGE_ADDON, ""));
 				findMessageBodyTextbox().click();
 			}
 
 			// captcha highlight
 
 			if (findCaptchaTextbox() != null) {
 				findCaptchaTextbox().click();
 
 				// onresume website highlight captcha HACK
 				injectJavascriptOnResumeHighlightCaptchaTextbox();
 			} else {
 				// captcha is used to determine if we are able to send messages
 				// or not, if captcha doesn't exist, then message sending is not
 				// allowed and we have to redirect user to send personal message
 				// page
 
 				String username = selectedItem.getSellerInfo().get(0)
 						.getSellerUserName().get(0);
 
 				driver.get(PERSONAL_MESSAGE_BASE + username);
 
 				String messageAddonFinal = Config.personalMessageAddon
 						+ " Nr. " + selectedItem.getItemId().get(0) + " "
 						+ selectedItem.getTitle().get(0) + ".";
 
 				String finalMEssage = selectedMessage.getValue().replace(
 						Config.PERSONAL_MESSAGE_ADDON, messageAddonFinal);
 
 				if (findMessageBodyTextbox() != null) {
 					WebdriverHelper.setInstantKeys(driver,
 							PRIVATEMESSAGE_BODY_ID, finalMEssage);
 					findMessageBodyTextbox().click();
 				}
 
 				// captcha highlight
 				if (findCaptchaTextbox() != null) {
 					findCaptchaTextbox().click();
 
 					injectJavascriptOnResumeHighlightCaptchaTextbox();
 				}
 			}
 		}
 
 		UI.printUI("Message sending setup finished");
 	}
 
 	private static WebElement findMessageBodyTextbox() {
 		return WebdriverHelper.findElementById(driver, PRIVATEMESSAGE_BODY_ID);
 	}
 
 	private static WebElement findCaptchaTextbox() {
 		return WebdriverHelper.findElementById(driver, CAPTCHA_ID);
 	}
 
 	private static void injectJavascriptOnResumeHighlightCaptchaTextbox() {
 		JavascriptExecutor js2 = (JavascriptExecutor) driver;
 		String onFocusFocus = "var s = document.createElement('script'); s.type = 'text/javascript'; "
 				+ "var code = 'window.onfocus = function () {document.getElementById(\"tokenText\").focus();}';"
 				+ "try {s.appendChild(document.createTextNode(code)); document.body.appendChild(s); } catch (e) {s.text = code; document.body.appendChild(s);}";
 		js2.executeScript(onFocusFocus);
 	}
 
 	private static String composeMessageUrl(Item i) {
 		String s = String
 				.format(ITEM_MESSAGE_BASE
 						+ "iid=%s&requested=%s&redirect=0&ssPageName=PageSellerM2MFAQ_VI",
 						i.getItemId().get(0), i.getSellerInfo().get(0)
 								.getSellerUserName().get(0));
 		UI.printDebug("MSG URL: " + s);
 		return s;
 	}
 }
