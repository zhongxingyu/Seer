 /*
  * Copyright (c) 2004, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creating date: 20.09.2004 08:26:58
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.tests.web;
 
 import java.io.IOException;
 import java.util.Random;
 
 import net.jforum.util.I18n;
 
 import com.dumbster.smtp.SimpleSmtpServer;
 import com.dumbster.smtp.SmtpMessage;
 
 /**
  * @author Marc Wick
 * @version $Id: UserWebTestCase.java,v 1.7 2004/09/29 08:59:00 marcwick Exp $
  */
 public class UserWebTestCase extends AbstractWebTestCase {
 
 	private static String lastTestuser;
 
 	public static String defaultTestuser = "defaultTestuser";
 
 	public static String password = "testpassword";
 
 	public UserWebTestCase(String name) throws IOException {
 		super(name);
 	}
 
 	public void testRegisterDefaultUser() {
 		beginAt(FORUMS_LIST);
 		assertLinkPresent("register");
 		clickLink("register");
 		assertFormPresent("formregister");
 		setFormElement("username", defaultTestuser);
 		setFormElement("email", defaultTestuser);
 		setFormElement("password", password);
 		setFormElement("password_confirm", password);
 		submit();
 	}
 
 	public void testRegisterNewUser() {
 		beginAt(FORUMS_LIST);
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.register"));
 		lastTestuser = "testuser" + new Random().nextInt(1000000);
 		setFormElement("username", lastTestuser);
 		setFormElement("email", lastTestuser);
 		setFormElement("password", "testpassword1");
 		setFormElement("password_confirm", "testpassword1");
 		submit();
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.logout"));
 	}
 
 	public void testChangePassword() {
 		beginAt(FORUMS_LIST);
 		login(lastTestuser, "testpassword1");
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.profile"));
 		setFormElement("current_password", "testpassword1");
 		setFormElement("new_password", password);
 		setFormElement("password_confirm", password);
 		submit();
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.logout"));
 		login(lastTestuser, password);
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.profile"));
 		setFormElement("signature", "signature for testuser");
 		submit();
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.logout"));
 	}
	
 	public void testEditUserProfile() {
 		login(lastTestuser, password);
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.profile"));
 		setFormElement("signature", "signature for testuser");
 		submit();
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.logout"));
 	}
 
 	public void testPasswordForgottenUserName() throws Exception {
 		smtpServer = SimpleSmtpServer.start();
		beginAt("/list.page");
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.login"));
 		clickLinkWithText(I18n.getMessage(language, "Login.lostPassword"));
 
 		setFormElement("username", lastTestuser);
 		submit();
 		Thread.sleep(1000);
 		assertEquals("password lost email received", 1, smtpServer
 				.getReceievedEmailSize());
 		SmtpMessage mail = (SmtpMessage) smtpServer.getReceivedEmail().next();
 		String body = mail.getBody();
 		String link = body.substring(body.indexOf("http:"),
 				body.indexOf(".page") + 5).trim();
 		smtpServer.stop();
 
 		getTestContext().setBaseUrl(link.substring(0, link.lastIndexOf('/')));
 		gotoPage(link.substring(link.lastIndexOf('/')));
 		setFormElement("email", lastTestuser);
 		setFormElement("newPassword", password);
 		setFormElement("confirmPassword", password);
 		submit();
 		clickLinkWithText(I18n.getMessage(language, "ForumBase.logout"));
 	}
 }
