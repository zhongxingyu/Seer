 /*******************************************************************************
  * Copyright (c) 2007 Remy Suen and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *    Markus Kuppe <mkuppe@versant.com> - Bug 1830436 - [bot] ~keys cmd to point to "ctrl+shift+l
  ******************************************************************************/
 package org.eclipse.ecf.presence.bot.kosmos;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.presence.bot.IChatRoomBotEntry;
 import org.eclipse.ecf.presence.bot.IChatRoomMessageHandler;
 import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
 import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;
 import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;
 import org.eclipse.osgi.util.NLS;
 
 public class ChatRoomMessageHandler implements IChatRoomMessageHandler {
 
 	private static final String BUG_DATABASE_PREFIX = "https://bugs.eclipse.org/bugs/show_bug.cgi?id="; //$NON-NLS-1$
 	private static final String BUG_DATABASE_POSTFIX = "&ctype=xml"; //$NON-NLS-1$
 	private static final String SUM_OPEN_TAG = "<short_desc>"; //$NON-NLS-1$
 	private static final String SUM_CLOSE_TAG = "</short_desc>"; //$NON-NLS-1$
 
 	private Map messageSenders;
 	private Map newsgroups;
 	private JavadocAnalyzer analyzer;
 
 	private IContainer container;
 
 	private static final String xmlDecode(String string) {
 		if (string.equals("")) { //$NON-NLS-1$
 			return string;
 		}
 
 		int index = string.indexOf("&amp;"); //$NON-NLS-1$
 		while (index != -1) {
 			string = string.substring(0, index) + '&'
 					+ string.substring(index + 5);
 			index = string.indexOf("&amp;", index + 1); //$NON-NLS-1$
 		}
 
 		index = string.indexOf("&quot;"); //$NON-NLS-1$
 		while (index != -1) {
 			string = string.substring(0, index) + '"'
 					+ string.substring(index + 6);
 			index = string.indexOf("&quot;", index + 1); //$NON-NLS-1$
 		}
 
 		index = string.indexOf("&apos;"); //$NON-NLS-1$
 		while (index != -1) {
 			string = string.substring(0, index) + '\''
 					+ string.substring(index + 6);
 			index = string.indexOf("&apos;", index + 1); //$NON-NLS-1$
 		}
 
 		index = string.indexOf("&lt;"); //$NON-NLS-1$
 		while (index != -1) {
 			string = string.substring(0, index) + '<'
 					+ string.substring(index + 4);
 			index = string.indexOf("&lt;", index + 1); //$NON-NLS-1$
 		}
 
 		index = string.indexOf("&gt;"); //$NON-NLS-1$
 		while (index != -1) {
 			string = string.substring(0, index) + '>'
 					+ string.substring(index + 4);
 			index = string.indexOf("&gt;", index + 1); //$NON-NLS-1$
 		}
 		return string;
 	}
 
 	public ChatRoomMessageHandler() {
 		messageSenders = new HashMap();
 		analyzer = new JavadocAnalyzer();
 		try {
 			parseNewsgroup();
 		} catch (Exception e) {
 			newsgroups = Collections.EMPTY_MAP;
 		}
 	}
 
 	private void parseNewsgroup() throws IOException {
 		Properties properties = new Properties();
 		properties.load(JavadocAnalyzer.class
 				.getResourceAsStream("newsgroup.txt"));
 		newsgroups = new HashMap();
 		for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
 			Object key = it.next();
 			Object value = properties.get(key);
 			newsgroups.put(key, value);
 			newsgroups.put(value, value);
 		}
 	}
 
 	private void sendMessage(ID roomID, String message) {
 		try {
 			if (container != null) {
 				IChatRoomMessageSender sender = (IChatRoomMessageSender) messageSenders
 						.get(roomID);
 				if (sender != null) {
 					sender.sendMessage(message);
 				}
 			}
 		} catch (ECFException e) {
 			e.printStackTrace();
 			container.disconnect();
 			container = null;
 		}
 	}
 
 	private void sendBugzillaLink(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.EclipseBugzilla);
 		} else {
 			sendMessage(roomID, NLS
 					.bind(Messages.EclipseBugzilla_Reply, target));
 		}
 	}
 
 	public void sendBug(ID roomID, String target, String number) {
 		String urlString = BUG_DATABASE_PREFIX + number;
 		if (target == null) {
 			try {
 				HttpURLConnection hURL = (HttpURLConnection) new URL(
 						BUG_DATABASE_PREFIX + number + BUG_DATABASE_POSTFIX)
 						.openConnection();
 				hURL.setAllowUserInteraction(true);
 				hURL.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(hURL.getInputStream()));
 				StringBuffer buffer = new StringBuffer();
 				try {
 					if (hURL.getResponseCode() != HttpURLConnection.HTTP_OK) {
 						sendMessage(roomID, NLS.bind(Messages.Bug, number,
 								urlString));
 						return;
 					}
 
 					String input = reader.readLine();
 					buffer.append(input);
 					while (input.indexOf(SUM_CLOSE_TAG) == -1) {
 						input = reader.readLine();
 						buffer.append(input);
 					}
 					hURL.disconnect();
 				} catch (EOFException e) {
 					hURL.disconnect();
 					sendMessage(roomID, NLS.bind(Messages.Bug, number,
 							urlString));
 					e.printStackTrace();
 					return;
 				}
 				String webPage = buffer.toString();
 				int summaryStartIndex = webPage.indexOf(SUM_OPEN_TAG);
 				int summaryEndIndex = webPage.indexOf(SUM_CLOSE_TAG,
 						summaryStartIndex);
 				if (summaryStartIndex != -1 & summaryEndIndex != -1) {
 					String summary = webPage.substring(summaryStartIndex
 							+ SUM_OPEN_TAG.length(), summaryEndIndex);
 					sendMessage(roomID, NLS.bind(Messages.BugContent,
 							new Object[] { number, xmlDecode(summary),
 									urlString }));
 				} else {
 					sendMessage(roomID, NLS.bind(Messages.Bug, new Object[] {
 							number, urlString }));
 				}
 			} catch (IOException e) {
 				sendMessage(roomID, NLS.bind(Messages.Bug, new Object[] {
 						number, urlString }));
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				HttpURLConnection hURL = (HttpURLConnection) new URL(
 						BUG_DATABASE_PREFIX + number + BUG_DATABASE_POSTFIX)
 						.openConnection();
 				hURL.setAllowUserInteraction(true);
 				hURL.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(hURL.getInputStream()));
 				StringBuffer buffer = new StringBuffer();
 				try {
 					if (hURL.getResponseCode() != HttpURLConnection.HTTP_OK) {
 						sendMessage(roomID, NLS.bind(Messages.Bug_Reply,
 								new Object[] { target, number, urlString }));
 						return;
 					}
 
 					String input = reader.readLine();
 					buffer.append(input);
 					while (input.indexOf(SUM_CLOSE_TAG) == -1) {
 						input = reader.readLine();
 						buffer.append(input);
 					}
 					hURL.disconnect();
 				} catch (EOFException e) {
 					hURL.disconnect();
 					sendMessage(roomID, NLS.bind(Messages.Bug_Reply,
 							new Object[] { target, number, urlString }));
 					return;
 				}
 				String webPage = buffer.toString();
 				int summaryStartIndex = webPage.indexOf(SUM_OPEN_TAG);
 				int summaryEndIndex = webPage.indexOf(SUM_CLOSE_TAG,
 						summaryStartIndex);
 				if (summaryStartIndex != -1 & summaryEndIndex != -1) {
 					String summary = webPage.substring(summaryStartIndex
 							+ SUM_OPEN_TAG.length(), summaryEndIndex);
 					sendMessage(roomID, NLS.bind(Messages.BugContent_Reply,
 							new Object[] { target, number, xmlDecode(summary),
 									urlString }));
 				} else {
 					sendMessage(roomID, NLS.bind(Messages.Bug_Reply,
 							new Object[] { target, number, urlString }));
 				}
 			} catch (IOException e) {
 				sendMessage(roomID, NLS.bind(Messages.Bug_Reply, new Object[] {
 						target, number, urlString }));
 			}
 		}
 	}
 
 	private void sendBugAndComment(ID roomID, String target, String number,
 			String comment) {
 		String urlString = BUG_DATABASE_PREFIX + number + "#c" + comment; //$NON-NLS-1$
 		if (target == null) {
 			try {
 				HttpURLConnection hURL = (HttpURLConnection) new URL(
 						BUG_DATABASE_PREFIX + number + BUG_DATABASE_POSTFIX)
 						.openConnection();
 				hURL.setAllowUserInteraction(true);
 				hURL.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(hURL.getInputStream()));
 				StringBuffer buffer = new StringBuffer();
 				try {
 					if (hURL.getResponseCode() != HttpURLConnection.HTTP_OK) {
 						sendMessage(roomID, NLS.bind(Messages.Bug,
 								new Object[] { number, urlString }));
 						return;
 					}
 
 					String input = reader.readLine();
 					buffer.append(input);
 					while (input.indexOf(SUM_CLOSE_TAG) == -1) {
 						input = reader.readLine();
 						buffer.append(input);
 					}
 					hURL.disconnect();
 				} catch (EOFException e) {
 					hURL.disconnect();
 					sendMessage(roomID, NLS.bind(Messages.Bug, new Object[] {
 							number, urlString }));
 					return;
 				}
 				String webPage = buffer.toString();
 				int summaryStartIndex = webPage.indexOf(SUM_OPEN_TAG);
 				int summaryEndIndex = webPage.indexOf(SUM_CLOSE_TAG,
 						summaryStartIndex);
 				if (summaryStartIndex != -1 & summaryEndIndex != -1) {
 					String summary = webPage.substring(summaryStartIndex
 							+ SUM_OPEN_TAG.length(), summaryEndIndex);
 					sendMessage(roomID, NLS.bind(Messages.BugContent,
 							new Object[] { number, xmlDecode(summary),
 									urlString }));
 				} else {
 					sendMessage(roomID, NLS.bind(Messages.Bug, new Object[] {
 							number, urlString }));
 				}
 			} catch (IOException e) {
 				sendMessage(roomID, NLS.bind(Messages.Bug, new Object[] {
 						number, urlString }));
 			}
 		} else {
 			try {
 				HttpURLConnection hURL = (HttpURLConnection) new URL(
 						BUG_DATABASE_PREFIX + number + BUG_DATABASE_POSTFIX)
 						.openConnection();
 				hURL.setAllowUserInteraction(true);
 				hURL.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(hURL.getInputStream()));
 				StringBuffer buffer = new StringBuffer();
 				try {
 					if (hURL.getResponseCode() != HttpURLConnection.HTTP_OK) {
 						sendMessage(roomID, NLS.bind(Messages.Bug_Reply,
 								new Object[] { target, number, urlString }));
 						return;
 					}
 
 					String input = reader.readLine();
 					buffer.append(input);
 					while (input.indexOf(SUM_CLOSE_TAG) == -1) {
 						input = reader.readLine();
 						buffer.append(input);
 					}
 					hURL.disconnect();
 				} catch (EOFException e) {
 					hURL.disconnect();
 					sendMessage(roomID, NLS.bind(Messages.Bug_Reply,
 							new Object[] { target, number, urlString }));
 					return;
 				}
 				String webPage = buffer.toString();
 				int summaryStartIndex = webPage.indexOf(SUM_OPEN_TAG);
 				int summaryEndIndex = webPage.indexOf(SUM_CLOSE_TAG,
 						summaryStartIndex);
 				if (summaryStartIndex != -1 & summaryEndIndex != -1) {
 					String summary = webPage.substring(summaryStartIndex
 							+ SUM_OPEN_TAG.length(), summaryEndIndex);
 					sendMessage(roomID, NLS.bind(Messages.BugContent_Reply,
 							new Object[] { target, number, xmlDecode(summary),
 									urlString }));
 				} else {
 					sendMessage(roomID, NLS.bind(Messages.Bug_Reply,
 							new Object[] { target, number, urlString }));
 				}
 			} catch (IOException e) {
 				sendMessage(roomID, NLS.bind(Messages.Bug_Reply, new Object[] {
 						target, number, urlString }));
 			}
 		}
 	}
 
 	private void sendManagementInformation(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Manage);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Manage_Reply, target));
 		}
 	}
 
 	private void sendLogInformation(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Logs);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Logs_Reply, target));
 		}
 	}
 
 	private void sendAbout(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.About);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.About_Reply, target));
 		}
 	}
 
 	private void sendPastebin(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Pastebin);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Pastebin_Reply, target));
 		}
 	}
 
 	private void sendSnippets(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Snippets);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Snippets_Reply, target));
 		}
 	}
 
 	private void sendJavaDoc(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Javadoc);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Javadoc_Reply, target));
 		}
 	}
 
 	private void sendPlugins(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Plugins);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Plugins_Reply, target));
 		}
 	}
 
 	private void sendWTP(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Webtools);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Webtools_Reply, target));
 		}
 	}
 
 	private void sendPHP(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.PHP);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.PHP_Reply, target));
 		}
 	}
 
 	private void sendSubversion(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Subversion);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Subversion_Reply, target));
 		}
 	}
 
 	private void sendNewsgroup(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Newsgroup);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Newsgroup_Reply, target));
 		}
 	}
 
 	private void sendNewsgroupSearch(ID roomID, String target, String query) {
 		String[] strings = query.split(" "); //$NON-NLS-1$
 		if (strings.length == 1) {
 			// no search terms provided
 			return;
 		}
 		for (int i = 0; i < strings.length; i++) {
 			try {
 				strings[i] = URLEncoder.encode(strings[i].trim(), "UTF-8"); //$NON-NLS-1$
 			} catch (UnsupportedEncodingException e) {
 				// technically this should never happen, but better safe than
 				// sorry
 				strings[i] = URLEncoder.encode(strings[i].trim());
 			}
 		}
 		String newsgroup = (String) newsgroups.get(strings[0]);
 		if (target == null) {
 			StringBuffer buffer = new StringBuffer();
 			synchronized (buffer) {
 				for (int i = 1; i < strings.length; i++) {
 					buffer.append(strings[i] + '+');
 				}
 				buffer.deleteCharAt(buffer.length() - 1);
 			}
 			sendMessage(roomID, NLS.bind(Messages.NewsgroupSearch, buffer
 					.toString(), newsgroup));
 		} else {
 			StringBuffer buffer = new StringBuffer();
 			synchronized (buffer) {
 				for (int i = 1; i < strings.length; i++) {
 					buffer.append(strings[i] + '+');
 				}
 				buffer.deleteCharAt(buffer.length() - 1);
 			}
 			sendMessage(roomID, NLS.bind(Messages.NewsgroupSearch_Reply,
 					new Object[] { target, buffer.toString(), newsgroup }));
 		}
 	}
 
 	private void sendHelp(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Help);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Help_Reply, target));
 		}
 	}
 
 	private void sendGoogle(ID roomID, String target, String searchString) {
 		searchString = searchString.replace(' ', '+');
 		if (target == null) {
 			sendMessage(roomID, NLS.bind(Messages.Google, searchString));
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Google_Reply, target,
 					searchString));
 		}
 	}
 
 	private void sendWiki(ID roomID, String target, String articleName) {
 		articleName = articleName.replace(' ', '_');
 		if (target == null) {
 			sendMessage(roomID, NLS.bind(Messages.Wiki, articleName));
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Wiki_Reply, target,
 					articleName));
 		}
 	}
 
 	private void sendEclipseHelp(ID roomID, String target, String searchString) {
 		searchString = searchString.replace(' ', '+');
 		if (target == null) {
 			sendMessage(roomID, NLS.bind(Messages.EclipseHelp, searchString));
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.EclipseHelp_Reply, target,
 					searchString));
 		}
 	}
 
 	private void sendSource(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Source);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Source_Reply, target));
 		}
 
 	}
 
 	private void sendECF(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.ECF);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.ECF_Reply, target));
 		}
 	}
 
 	private void sendTM(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.TM);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.TM_Reply, target));
 		}
 	}
 
 	private void sendJavaDoc(ID roomID, String target, String parameter) {
 		String append = target == null ? "" : target + ": ";
 		String message = null;
 		int index = parameter.indexOf('#');
 		if (index == -1) {
 			message = analyzer.getJavadocs(parameter);
 		} else {
 			String className = parameter.substring(0, index);
 			parameter = parameter.substring(index + 1);
 			index = parameter.indexOf('(');
 			if (index == -1) {
 				message = className + '#' + parameter + " - "
 						+ analyzer.getJavadocs(className, parameter);
 			} else {
 				String method = parameter.substring(0, index);
 				parameter = parameter.substring(index + 1);
 				parameter = parameter.substring(0, parameter.indexOf(')'));
 				String[] parameters = parameter.split(",");
 				for (int i = 0; i < parameters.length; i++) {
 					parameters[i] = parameters[i].trim();
 				}
 				message = className + '#' + method + " - "
 						+ analyzer.getJavadocs(className, method, parameters);
 			}
 		}
 		sendMessage(roomID, append + message);
 	}
 
 	private void sendDeadlock(ID roomID, String target) {
 		if (target == null) {
 			sendMessage(roomID, Messages.Deadlock);
 		} else {
 			sendMessage(roomID, NLS.bind(Messages.Deadlock_Reply, target));
 		}
 	}
	
	private void sendKeys(ID roomID, String target) {
		if (target == null) {
			sendMessage(roomID, Messages.Keys);
		} else {
			sendMessage(roomID, NLS.bind(Messages.Keys_Reply, target));
		}
	}
 
 	private void send(ID roomID, String target, String msg) {
 		if (msg.equals("bug")) { //$NON-NLS-1$
 			sendBugzillaLink(roomID, target);
 		} else if (msg.startsWith("bug")) { //$NON-NLS-1$
 			int index = msg.indexOf('c');
 			if (index == -1) {
 				sendBug(roomID, target, msg.substring(3));
 			} else {
 				sendBugAndComment(roomID, target, msg.substring(3, index), msg
 						.substring(index + 1));
 			}
 		} else if (msg.equals("log") || msg.equals("logs")) { //$NON-NLS-1$ //$NON-NLS-2$
 			sendLogInformation(roomID, target);
 		} else if (msg.equals("manage")) { //$NON-NLS-1$
 			sendManagementInformation(roomID, target);
 		} else if (msg.equals("about") || msg.equals("bot")) { //$NON-NLS-1$ //$NON-NLS-2$
 			sendAbout(roomID, target);
 		} else if (msg.equals("paste") || msg.equals("pastebin")) { //$NON-NLS-1$ //$NON-NLS-2$
 			sendPastebin(roomID, target);
 		} else if (msg.equals("snippet") || msg.equals("snippets")) { //$NON-NLS-1$ //$NON-NLS-2$
 			sendSnippets(roomID, target);
 		} else if (msg.equals("javadoc") || msg.equals("api")) { //$NON-NLS-1$ //$NON-NLS-2$
 			sendJavaDoc(roomID, target);
 		} else if (msg.startsWith("javadoc ")) { //$NON-NLS-1$
 			sendJavaDoc(roomID, target, msg.substring(8));
 		} else if (msg.startsWith("api ")) { //$NON-NLS-1$
 			sendJavaDoc(roomID, target, msg.substring(4));
 		} else if (msg.equals("news") || msg.equals("newsgroup")) { //$NON-NLS-1$ //$NON-NLS-2$
 			sendNewsgroup(roomID, target);
 		} else if (msg.startsWith("news ")) { //$NON-NLS-1$
 			sendNewsgroupSearch(roomID, target, msg.substring(5));
 		} else if (msg.startsWith("newsgroup ")) {
 			sendNewsgroupSearch(roomID, target, msg.substring(10));
 		} else if (msg.equals("plugin")) { //$NON-NLS-1$
 			sendPlugins(roomID, target);
 		} else if (msg.equals("wtp")) { //$NON-NLS-1$
 			sendWTP(roomID, target);
 		} else if (msg.equals("php")) { //$NON-NLS-1$
 			sendPHP(roomID, target);
 		} else if (msg.equals("svn")) { //$NON-NLS-1$
 			sendSubversion(roomID, target);
 		} else if (msg.equals("cmd") || msg.equals("commands") //$NON-NLS-1$ //$NON-NLS-2$
 				|| msg.equals("help")) { //$NON-NLS-1$
 			sendHelp(roomID, target);
 		} else if (msg.startsWith("g ")) { //$NON-NLS-1$
 			sendGoogle(roomID, target, msg.substring(2));
 		} else if (msg.startsWith("wiki ")) { //$NON-NLS-1$
 			sendWiki(roomID, target, msg.substring(5));
 		} else if (msg.startsWith("eh")) { //$NON-NLS-1$
 			sendEclipseHelp(roomID, target, msg.substring(3));
 		} else if (msg.equals("source")) { //$NON-NLS-1$
 			sendSource(roomID, target);
 		} else if (msg.equals("ecf")) { //$NON-NLS-1$
 			sendECF(roomID, target);
 		} else if (msg.equals("tm")) { //$NON-NLS-1$
 			sendTM(roomID, target);
 		} else if (msg.equals("deadlock")) { //$NON-NLS-1$
 			sendDeadlock(roomID, target);
		} else if (msg.equals("keys")) { //$NON-NLS-1$
			sendKeys(roomID, target);
 		} else {
 			int index = msg.indexOf('c');
 			if (index == -1) {
 				try {
 					// check if what's before the 'c' is a valid number
 					Integer.parseInt(msg);
 					sendBug(roomID, target, msg);
 				} catch (NumberFormatException e) {
 					return;
 				}
 			} else {
 				try {
 					// check if what's before the 'c' is a valid number
 					Integer.parseInt(msg.substring(0, index));
 					sendBugAndComment(roomID, target, msg.substring(0, index),
 							msg.substring(index + 1));
 				} catch (NumberFormatException e) {
 					return;
 				}
 			}
 		}
 	}
 
 	private String[] parseInput(String msg) {
 		if (msg.startsWith("tell")) { //$NON-NLS-1$
 			msg = msg.substring(5);
 			int index = msg.indexOf(' ');
 			if (index == -1) {
 				return null;
 			}
 			String user = msg.substring(0, index);
 			msg = msg.substring(index + 1);
 			index = msg.indexOf(' ');
 			if (index == -1) {
 				return null;
 			}
 			String tmp = msg.substring(0, index);
 			if (tmp.equals("about")) { //$NON-NLS-1$
 				msg = msg.substring(index + 1);
 			}
 			return new String[] { user, msg };
 		} else {
 			return new String[] { null, msg };
 		}
 	}
 
 	public void handleRoomMessage(IChatRoomMessage message) {
 		try {
 			String[] info = parseInput(message.getMessage().trim().substring(1));
 			if (info != null) {
 				send(message.getChatRoomID(), info[0], info[1]);
 			}
 		} catch (RuntimeException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void init(IChatRoomBotEntry robot) {
 		// nothing to do
 	}
 
 	public void preChatRoomConnect(IChatRoomContainer roomContainer, ID roomID) {
 		messageSenders.put(roomID, roomContainer.getChatRoomMessageSender());
 	}
 
 	public void preContainerConnect(IContainer container, ID targetID) {
 		this.container = container;
 	}
 
 }
