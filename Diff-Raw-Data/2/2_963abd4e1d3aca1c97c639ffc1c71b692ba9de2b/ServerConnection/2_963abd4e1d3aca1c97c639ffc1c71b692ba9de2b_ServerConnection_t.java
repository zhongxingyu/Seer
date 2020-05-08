 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Wim Jongman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.ecf.protocol.nntp.core.internal;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 import org.eclipse.ecf.protocol.nntp.core.ArticleFactory;
 import org.eclipse.ecf.protocol.nntp.core.Debug;
 import org.eclipse.ecf.protocol.nntp.core.StringUtils;
 import org.eclipse.ecf.protocol.nntp.model.IArticle;
 import org.eclipse.ecf.protocol.nntp.model.ICredentials;
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.protocol.nntp.model.IServerConnection;
 import org.eclipse.ecf.protocol.nntp.model.NNTPConnectException;
 import org.eclipse.ecf.protocol.nntp.model.NNTPException;
 import org.eclipse.ecf.protocol.nntp.model.NNTPIOException;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.protocol.nntp.model.StoreException;
 import org.eclipse.ecf.protocol.nntp.model.TimeoutException;
 import org.eclipse.ecf.protocol.nntp.model.UnexpectedResponseException;
 
 public class ServerConnection implements IServerConnection {
 
 	private Socket socket;
 
 	private String lastResponse;
 
 	private INewsgroup[] newsgroups;
 
 	private boolean possibleResponseAvailable;
 
 	private IServer server;
 
 	private static final int TIMEOUTSECONDS = 10;
 
 	private static final String TIMEOUTLINE = "591 No input received within "
 			+ TIMEOUTSECONDS + " seconds\r\n";
 
 	private static final String[] TIMEOUTLIST = new String[] {
 			"591 No input received within " + TIMEOUTSECONDS + " seconds \r\n",
 			".\r\n" };
 
 	private ICredentials credentials;
 
 	private int batchSize;
 
 	public ServerConnection(IServer server) {
 		this.server = server;
 		server.setServerConnection(this);
 	}
 
 	public String getResponse() throws NNTPIOException {
 
 		// If no command was send then return the last message
 		if (!isPossibleResponseAvailable())
 			return getLastResponse();
 		else
 			setPossibleResponseAvailable(false);
 
 		// Read from the socket
 		try {
 			StringBuffer buffer = new StringBuffer();
 			if (waitForInput()) {
 				byte[] response;
 				while (!buffer.toString().endsWith("\r\n")) {
 					response = new byte[socket.getInputStream().available()];
 					socket.getInputStream().read(response);
 					buffer.append(new String(response));
 				}
 			} else
 				buffer.append(TIMEOUTLINE);
 
 			Debug.log(this.getClass(), buffer.toString());
 			setLastResponse(buffer.toString());
 			return new String(buffer.toString());
 		} catch (IOException e) {
 			throw new NNTPIOException(e.getMessage(), e);
 		}
 	}
 
 	public String[] getListResponse() throws NNTPIOException {
 
 		// If no command was send then return the last message
 		if (!isPossibleResponseAvailable())
 			return StringUtils.split(getLastResponse(), "\r\n");
 		else
 			setPossibleResponseAvailable(false);
 
 		try {
 
 			StringBuffer buffer = new StringBuffer();
 
 			if (waitForInput()) {
 				// FIXME can get an error here instead of a list. Check for that
 				// as well a "400 news.xs4all.nl: Idle timeout" was seen here.
 				byte[] response;
 				while (!buffer.toString().endsWith("\r\n.\r\n")) {
 
 					if (socket.getInputStream().available() == 0) {
 						if (!waitForInput())
 							break;
 					}
 					response = new byte[socket.getInputStream().available()];
 					// if(response.length == 0)
 					// break;
 
 					socket.getInputStream().read(response);
 					buffer.append(new String(response));
 				}
 			} else
 				for (int i = 0; i < TIMEOUTLIST.length; i++) {
 					buffer.append(TIMEOUTLIST[i]);
 				}
 			Debug.log(this.getClass(), buffer.toString());
 			setLastResponse(buffer.toString());
 			return StringUtils.split(buffer.toString(), "\r\n");
 		} catch (IOException e) {
 			throw new NNTPIOException("Could not get List response because of "
 					+ e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Waits for input. Returns true if input found, false if the wait time was
 	 * exceeded without input responses.
 	 * 
 	 * @return true if input is on the queue, false otherwise.
 	 */
 	private boolean waitForInput() {
 
 		int counter = 0;
 
 		try {
 			while (socket.getInputStream().available() == 0) {
 				Thread.sleep(100);
 
 				counter++;
 
 				if (counter / 10 > TIMEOUTSECONDS)
 					throw new TimeoutException("Server " + server.getAddress()
 							+ " timed out");
 
 				Debug.logn(".");
 
 			}
 		} catch (Exception e) {
 			Debug.log(this.getClass(), e);
 			return false;
 		}
 
 		return true;
 
 	}
 
 	public void disconnect() throws NNTPConnectException {
 
 		try {
 			if (isConnected())
 				sendCommand("quit");
 			Thread.sleep(100);
 			socket.close();
 		} catch (Exception e) {
 			throw new NNTPConnectException("Could not disconnect", e);
 		}
 		return;
 	}
 
 	public INewsgroup[] getNewsgroups() throws NNTPIOException,
 			UnexpectedResponseException {
 
 		if (newsgroups == null || newsgroups.length == 0)
 			newsgroups = listNewsgroups(server);
 
 		return newsgroups;
 	}
 
 	public void sendCommand(String command) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		synchronized (socket) {
 
 			Debug.log(this.getClass(), command);
 
 			try {
 				socket.getOutputStream().write((command + "\r\n").getBytes());
 			} catch (IOException e) {
 				Debug.log(this.getClass(), "Error received, reconnecting");
 				Debug.log(this.getClass(), e);
 				connect();
 				try {
 					socket.getOutputStream().write(
 							(command + "\r\n").getBytes());
 				} catch (IOException e1) {
 					Debug.log(this.getClass(), "Could not send command due to "
 							+ e1.getMessage());
 					throw new NNTPIOException("Could not send command due to "
 							+ e1.getMessage(), e1);
 				}
 			}
 
 			setPossibleResponseAvailable(true);
 		}
 
 	}
 
 	public boolean isConnected() {
 		if (socket == null)
 			return false;
 		try {
 			socket.getOutputStream().write(" ".getBytes());
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 		// return socket.isConnected(); jdk 1.4
 	}
 
 	private void setLastResponse(String lastResponse) {
 		this.lastResponse = lastResponse;
 	}
 
 	public String getLastResponse() {
 		return lastResponse;
 	}
 
 	/**
 	 * Indicates if responses are to be expected from the server.
 	 * 
 	 * @return false if no response is expected from the server
 	 */
 	private boolean isPossibleResponseAvailable() {
 		return possibleResponseAvailable;
 	}
 
 	/**
 	 * Set by commands (e.g. {@link #sendCommand(String)} that trigger responses
 	 * from the server. Set by commands that have read the responses from the
 	 * server.
 	 * 
 	 * @param possibleResponseAvailable
 	 */
 	private void setPossibleResponseAvailable(boolean possibleResponseAvailable) {
 		this.possibleResponseAvailable = possibleResponseAvailable;
 	}
 
 	public IServer getServer() {
 		return server;
 	}
 
 	public void flush() {
 		try {
 			setPossibleResponseAvailable(true);
 			getResponse();
 		} catch (NNTPIOException e) {
 			// ignore
 		}
 	}
 
 	public void connect() throws NNTPIOException, NNTPConnectException {
 
 		try {
 			socket = new Socket(server.getAddress(), server.getPort());
 			socket.setSoTimeout(TIMEOUTSECONDS * 1000);
 			// socket.setKeepAlive(true);
 			flush();
 
 			//
 			// Logon
 			if (!getServer().isAnonymous()) {
 				String command = "authinfo user " + getLogin();
 				try {
 					socket.getOutputStream().write(
 							(command + SALVO.CRLF).getBytes());
 				} catch (IOException e) {
 					Debug.log(this.getClass(), "Error received, reconnecting");
 					Debug.log(this.getClass(), e);
 					try {
 						socket.getOutputStream().write(
 								(command + SALVO.CRLF).getBytes());
 					} catch (IOException e1) {
 						Debug.log(this.getClass(),
 								"Could not send command due to "
 										+ e1.getMessage());
 						throw new NNTPIOException(
 								"Could not send credentials ('authinfo user' command)",
 								e1);
 					}
 				}
 				flush();
 
 				//
 				// Password
 				command = ("authinfo pass " + credentials.getPassword());
 				try {
 					socket.getOutputStream().write(
 							(command + SALVO.CRLF).getBytes());
 				} catch (IOException e) {
 					Debug.log(this.getClass(), "Error received, reconnecting");
 					Debug.log(this.getClass(), e);
 					try {
 						socket.getOutputStream().write(
 								(command + SALVO.CRLF).getBytes());
 					} catch (IOException e1) {
 						Debug.log(this.getClass(),
 								"Could not send command due to "
 										+ e1.getMessage());
 						throw new NNTPIOException(
 								"Could not send passsword. ('authinfo pass' command)",
 								e1);
 					}
 				}
 
 				flush();
 			}
 
 		} catch (Exception e) {
 			throw new NNTPConnectException(e.getMessage(), e);
 		}
 
 		if (!isConnected()) {
 			throw new NNTPConnectException(
 					"We tried to connect to the NNTP server "
 							+ "and all seemed fine but after checking the socket "
 							+ "it reported that we were not connected. "
 							+ "This can be caused by a very nervous NNTP server. "
 							+ "Try connecting to the NNTP server in a telnet session "
 							+ "to find out what is really going on.");
 		}
 	}
 
 	public void setModeReader(IServer server) throws NNTPIOException,
 			UnexpectedResponseException {
 		sendCommand("mode reader");
 
 		// Silly hack but some servers are a little deaf
 		if (!getResponse().startsWith("200")) {
 			sendCommand("mode reader");
 			getResponse();
 		}
 
 	}
 
 	public INewsgroup[] listNewsgroups(IServer server) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		sendCommand("list newsgroups");
 
 		String[] lines = getListResponse();
 		ArrayList list = new ArrayList(lines.length);
 		int ix = 0;
 		for (int i = 1; i < lines.length; i++) {
 			String line = lines[i];
 			if (line.trim().startsWith(".")) {
 				break;
 			}
 
 			line = line.replace('\t', ' ');
 			String name = "";
 			String description = "";
 			if (line.indexOf(SALVO.SPACE) < 0) {
 				name = line.trim();
 				description = "";
 			} else {
 				name = line.substring(0, line.indexOf(SALVO.SPACE)).trim();
 				description = line.substring(line.indexOf(SALVO.SPACE),
 						line.length()).trim();
 			}
 
 			Debug.log(getClass(), ix++ + "" + line);
 
 			Newsgroup group = new Newsgroup(getServer(), name, description);
 
 			if (group != null) {
 				list.add(group);
 				Debug.log(getClass(), "Group found: " + group.toString());
 			}
 
 		}
 
 		return (INewsgroup[]) list.toArray(new INewsgroup[list.size()]);
 
 	}
 
 	public IArticle[] getArticles(INewsgroup newsgroup, int from, int to)
 			throws NNTPIOException, UnexpectedResponseException {
 
 		ArrayList result = new ArrayList();
 
 		sendCommand("group " + newsgroup.getNewsgroupName());
 		flush();
 
 		// Get all the Articles with XOVER
 		sendCommand("xover " + from + "-" + to);
 		String[] rList = getListResponse();
 
 		// TODO: XOVER Command and/or LIST OVERVIEW.FTM not supported. Will be
 		// handled if time so requires.
 		if (!rList[0].startsWith("224"))
 			throw new UnexpectedResponseException(rList[0]);
 
 		// Load the article contents
 		for (int i = 1; i < rList.length; i++) {
 			String overviewResult = rList[i];
 			if (!overviewResult.startsWith(".")) {
 				IArticle article = ArticleFactory.createArticle(
 						getOverviewHeaders(server), overviewResult, newsgroup);
 				if (article != null)
 					result.add(article);
 			}
 		}
 
 		// Adjust newsgroup indexes.
 		if (result.size() > 0) {
 
 			// If the high watermark is the same as the to parameter then check
 			// if the last message was indeed the value in the high watermark.
 			// If it is not then adjust the high watermark in the newsgroup.
 			if (to == newsgroup.getHighWaterMark()) {
 				newsgroup.adjustHighWatermark(((IArticle) result.get(result
 						.size() - 1)).getArticleNumber());
 			}
 
 			// If the low watermark is the same as the to parameter the check if
 			// the first message was indeed the value in the low watermark. If
 			// it is not then adjust the low watermark in the newsgroup.
 			if (to == newsgroup.getLowWaterMark()) {
 				newsgroup.adjustLowWatermark(((IArticle) result.get(0))
 						.getArticleNumber());
 			}
 
 			// If the high/low watermarks are the same as the from/to parameters
 			// then adjust the article count.
 			if (to == newsgroup.getHighWaterMark()
 					&& from == newsgroup.getLowWaterMark()) {
 				newsgroup.adjustArticleCount(result.size());
 			}
 		}
 
 		// Swap the elements according to the contract of the interface
 		ArrayList reversed = new ArrayList(result.size());
 		for (int i = result.size(); i > 0; i--) {
 			reversed.add(result.get(i - 1));
 		}
 
 		return (IArticle[]) reversed.toArray(new IArticle[reversed.size()]);
 	}
 
 	public String[] getOverviewHeaders(IServer server) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		/*
 		 * Get from the server if stored
 		 */
 		if (server.getOverviewHeaders() != null) {
 			return server.getOverviewHeaders();
 		}
 
 		/*
 		 * Get the format of the overview command
 		 */
 		sendCommand("list overview.fmt");
 		String[] rList = getListResponse();
 		ArrayList result = new ArrayList();
 
 		// TODO: XOVER Command and LIST OVERVIEW.FTM not supported. Will be
 		// handled if time so requires.
 		if (!rList[0].startsWith("215"))
 			throw new UnexpectedResponseException(rList[0]
 					+ " (overview.fmt not supported by this server)");
 
 		// Load the overview headers
 		for (int i = 0; i < rList.length; i++) {
 			Debug.log(getClass(), ".Header: " + rList[i]);
 			if (!rList[i].startsWith("215") && !rList[i].startsWith(".")) {
 				result.add(rList[i]);
 			}
 		}
 
 		/*
 		 * Store in the IServer object
 		 */
 		String[] headers = (String[]) result.toArray(new String[result.size()]);
 		server.setOverviewHeaders(headers);
 		return server.getOverviewHeaders();
 	}
 
 	/**
 	 * This method goes to the server and asks for the active newsgroup
 	 * attributes. These attributes are then placed back into the newsgroup.
 	 * 
 	 * @param server
 	 * @param newsgroup
 	 * @throws NNTPConnectException
 	 * @throws NNTPIOException
 	 */
 	public void setWaterMarks(INewsgroup newsgroup) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		sendCommand("group " + newsgroup.getNewsgroupName());
 		String response = getResponse();
 
 		// 211 39270 1 39301 eclipse.tools.emf
 		// 
 		if (response == null || response.startsWith("411")) {
 			newsgroup.setAttributes(0, 0, 0);
 			return;
 		}
 
 		if (!response.startsWith("211"))
 			throw new UnexpectedResponseException(
 					"The group command for newsgroup "
 							+ newsgroup.getNewsgroupName() + " in server "
 							+ newsgroup.getServer().getAddress() + " returned "
 							+ response);
 
 		String[] elements = StringUtils.split(response, SALVO.SPACE);
 		if (elements.length == 5) {
 			newsgroup.setAttributes(Integer.parseInt(elements[1]), Integer
 					.parseInt(elements[2]), Integer.parseInt(elements[3]));
 			return;
 		} else
 			newsgroup.setAttributes(0, 0, 0);
 	}
 
 	public String[] getArticleBody(IArticle article) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		INewsgroup newsgroup = article.getNewsgroup();
 		sendCommand("group " + newsgroup.getNewsgroupName());
 		getResponse();
 
 		String messageId = article.getMessageId();
 		sendCommand("article " + messageId);
 
 		String[] result = getListResponse();
 		final StringBuffer buffie = new StringBuffer();
 		for (int i = 0; i < result.length; i++) {
 			if (i == 0 && result[0].startsWith("2"))
 				Debug.log(getClass(), "Skipped " + result[0]);
 			else
 				buffie.append(result[i] + SALVO.CRLF);
 		}
 
 		return StringUtils.split(buffie.toString(), SALVO.CRLF);
 
 	}
 
 	public IArticle getArticle(INewsgroup newsgroup, int articleId)
 			throws NNTPIOException, UnexpectedResponseException {
 
 		sendCommand("group " + newsgroup.getNewsgroupName());
 		getResponse();
 
 		// Get the article with XOVER
 		sendCommand("xover " + articleId);
 		String[] rList = getListResponse();
 
 		// TODO: XOVER Command and/or LIST OVERVIEW.FTM not supported. Will be
 		// handled if time so requires.
 		if (!rList[0].startsWith("224"))
 			return null;
 
 		// Load the article contents
 		if (!rList[1].startsWith(".")) {
 			return ArticleFactory.createArticle(getOverviewHeaders(server),
 					rList[1], newsgroup);
 		}
 
 		return null;
 
 	}
 
 	public IArticle[] getFollowUps(IArticle article) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		Collection result = new ArrayList();
 
 		// Switch to the newsgroup
 		INewsgroup newsgroup = article.getNewsgroup();
 
 		sendCommand("group " + newsgroup.getNewsgroupName());
 		flush();
 
 		// Get all the follow ups with XPAT
 		String reference = article.getMessageId();
 		sendCommand("xpat references " + newsgroup.getLowWaterMark() + "-"
 				+ newsgroup.getHighWaterMark() + " *" + reference);
 
 		String[] rList = getListResponse();
 
 		if (!rList[0].startsWith("221"))
 			return new IArticle[0];
 
 		// Load the article contents
 		for (int i = 1; i < rList.length; i++) {
 			String xpatResult = rList[i];
 			if (!xpatResult.startsWith(".")) {
 				StringTokenizer tizer = new StringTokenizer(xpatResult, " ");
 				int range = new Integer(tizer.nextToken()).intValue();
 				result.addAll(Arrays
 						.asList(getArticles(newsgroup, range, range)));
 			}
 		}
 		return (IArticle[]) result.toArray(new IArticle[result.size()]);
 	}
 
 	public void replyToArticle(IArticle article, String body)
 			throws NNTPIOException, UnexpectedResponseException {
 		try {
 
 			// flush();
 			getResponse();
 			// Post
 			sendCommand("post ");
 			String response = getResponse();
 			if (!response.startsWith("340")) {
 				Debug.log(getClass(), response);
 				throw new UnexpectedResponseException(response);
 			}
 
 			sendCommand("Subject: "
 					+ (article.getSubject().startsWith("Re: ") ? article
 							.getSubject() : "Re: " + article.getSubject()));
 			sendCommand("Organization: " + server.getOrganization());
 			sendCommand("X-Organization: Salvo");
 			sendCommand("From: " + getEmail() + " (" + getUser() + ")");
 			sendCommand("X-Newsreader: Salvo");
 			sendCommand("Newsgroups: "
 					+ article.getNewsgroup().getNewsgroupName());
 			StringBuffer references = new StringBuffer();
 			String[] refs = article.getReferences();
 			for (int i = 0; i < refs.length; i++) {
 				references.append(refs[i] + " ");
 			}
 			references.append(article.getMessageId());
 			sendCommand("References: " + references.toString());
 			sendCommand("");
 			refs = StringUtils.split(body, SALVO.CRLF);
 			for (int i = 0; i < refs.length; i++) {
 				sendIn80Characters(refs[i]);
 			}
 			sendCommand(".");
 
 			// Get the post response
 			response = getResponse();
 			System.out.println(response);
 			Debug.log(getClass(), response);
 			if (!response.startsWith("240")) {
 				throw new UnexpectedResponseException(response);
 			}
 
 		} catch (NNTPConnectException e) {
 			throw new NNTPIOException(e.getMessage(), e);
 		}
 
 	}
 
 	private void sendIn80Characters(String line) throws NNTPIOException,
 			UnexpectedResponseException {
 
 		StringBuffer buffer = new StringBuffer();
 		StringTokenizer tizer = new StringTokenizer(line, " ");
 		while (tizer.hasMoreElements()) {
 			String word = tizer.nextToken();
 			if ((buffer.length() + word.length() + 1) > SALVO.LINE_LENGTH) {
 				sendCommand(buffer.toString());
 				buffer.setLength(0);
 			}
 			buffer.append(word + " ");
 		}
 		sendCommand(buffer.toString());
 	}
 
 	public void postNewArticle(INewsgroup[] newsgroups, String subject,
 			String body) throws NNTPIOException, UnexpectedResponseException {
 
 		// flush();
 
 		// Post
 		sendCommand("post ");
 		String response = getResponse();
 		if (!response.startsWith("340")) {
 			Debug.log(getClass(), response);
 			throw new UnexpectedResponseException(response);
 		}
 
		sendCommand("From: " + getUser() + " <" + getEmail() + ">");
 		sendCommand("Subject: " + subject);
 		sendCommand("Organization: " + server.getOrganization());
 		sendCommand("X-Organization: Salvo");
 		sendCommand("X-Newsreader: Salvo");
 		StringBuffer toGroups = new StringBuffer();
 		for (int i = 0; i < newsgroups.length; i++) {
 			toGroups.append(newsgroups[i].getNewsgroupName());
 			toGroups.append(",");
 		}
 		sendCommand("Newsgroups: "
 				+ toGroups.substring(0, toGroups.length() - 1));
 		// StringBuffer references = new StringBuffer();
 		// connection.sendCommand("References: " + references.toString());
 		sendCommand("");
 		String[] lines = StringUtils.split(body.toString(), SALVO.CRLF);
 		for (int i = 0; i < lines.length; i++) {
 			sendIn80Characters(lines[i]);
 		}
 		sendCommand(".");
 
 		// Get the post response
 		response = getResponse();
 		System.out.println(response);
 		Debug.log(getClass(), response);
 		if (!response.startsWith("240")) {
 			throw new UnexpectedResponseException(response);
 		}
 	}
 
 	public boolean cancelArticle(IArticle article) {
 		return false;
 	}
 
 	public IArticle[] getAllFollowUps(IArticle article) throws NNTPIOException,
 			UnexpectedResponseException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public INewsgroup[] listNewsgroups(IServer server, Date since)
 			throws NNTPIOException, UnexpectedResponseException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean postArticle(IArticle article) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void updateAttributes(INewsgroup newsgroup) throws NNTPIOException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setCredentials(ICredentials credentials) {
 		this.credentials = credentials;
 	}
 
 	public String getEmail() {
 		return credentials.getEmail();
 	}
 
 	public String getLogin() {
 		return credentials.getLogin();
 	}
 
 	public String getUser() {
 		return credentials.getUser();
 	}
 
 	public int getBatchsize() {
 		if (batchSize == 0)
 			return SALVO.DEFAULT_BATCH;
 		return batchSize;
 	}
 
 	public void setBatchSize(int size) {
 		batchSize = size;
 	}
 
 	public IArticle getArticle(String URL) throws NNTPIOException,
 			UnexpectedResponseException, NNTPException {
 
 		int articleNumber;
 		String newsgroup;
 
 		try {
 			String[] split = StringUtils.split(URL, "/");
 			split = StringUtils.split(split[split.length - 1], "?");
 			articleNumber = Integer.parseInt(split[1]);
 			newsgroup = split[0];
 		} catch (Exception e) {
 			throw new NNTPException("Error parsing URL " + URL, e);
 		}
 
 		INewsgroup[] groups = getNewsgroups();
 		for (int i = 0; i < groups.length; i++) {
 			if (groups[i].getNewsgroupName().equals(newsgroup))
 				return getArticle(groups[i], articleNumber);
 		}
 
 		return null;
 	}
 
 	public int purge(Calendar purgeDate, int number) throws NNTPIOException {
 		throw new NNTPIOException("You cannot remove articles from the server, use cancelArticle()");
 	}
 
 	public int delete(IArticle article) throws NNTPIOException {
 		throw new NNTPIOException("You cannot remove articles from the server, use cancelArticle()");
 	}
 }
