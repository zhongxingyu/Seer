 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.services;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.jasypt.util.text.BasicTextEncryptor;
 import org.meerkat.ssh.SecureShellSSHUserInfo;
 import org.meerkat.util.Counter;
 import org.meerkat.util.PropertiesLoader;
 import org.meerkat.webapp.WebAppResponse;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.Session;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public class SecureShellSSH extends WebApp {
 
 	private static final long serialVersionUID = -5058284964745491131L;
 	private static Logger log = Logger.getLogger(SecureShellSSH.class);
 
 	private String user;
 	private String passwd;
 	private String host;
 	private String port;
 	private String cmdToExecute;
 	@XStreamOmitField
	transient private BasicTextEncryptor textEncryptor;
 	@XStreamOmitField
 	PropertiesLoader pL;
 	
 	/**
 	 * SecureShellSSH
 	 * 
 	 * @param name
 	 * @param user
 	 * @param passwd
 	 * @param host
 	 * @param port
 	 * @param expectedResponse
 	 * @param cmdToExecute
 	 */
 	public SecureShellSSH(String name, String user, String passwd, String host,
 			String port, String expectedResponse, String cmdToExecute) {
 		super(name, host, expectedResponse);
 		this.user = user;
 		setupMainPassword();
 		this.host = host;
 		this.port = port;
 		this.cmdToExecute = cmdToExecute;
 		this.setTypeSSH();
 
 	}
 
 	/**
 	 * checkWebAppStatus
 	 */
 	public final WebAppResponse checkWebAppStatus() {
 		setupMainPassword(); // so textEncryptor is not null
 		setCurrentResponse("");
 		WebAppResponse response = new WebAppResponse();
 		response.setResponseSSH();
 
 		// Measure the response time
 		Counter c = new Counter();
 		c.startCounter();
 
 		try {
 			JSch jsch = new JSch();
 			Session session = jsch.getSession(user, host, Integer.valueOf(port));
 			SecureShellSSHUserInfo userInfo = new SecureShellSSHUserInfo(textEncryptor.decrypt(passwd));
 			session.setUserInfo(userInfo);
 			session.setPassword(textEncryptor.decrypt(passwd));
 
 			session.setConfig("StrictHostKeyChecking", "no");
 			session.connect(30000);
 
 			Channel channel = session.openChannel("exec");
 			((ChannelExec) channel).setCommand(cmdToExecute);
 			channel.setInputStream(null);
 			((ChannelExec) channel).setErrStream(System.err);
 			InputStream in = channel.getInputStream();
 			channel.connect();
 
 			ByteArrayInputStream bs = new ByteArrayInputStream(cmdToExecute.getBytes());
 			channel.setInputStream(bs);
 			channel.getOutputStream();
 
 			channel.connect();
 			channel.run();
 
 			bs.close();
 			String result = "";
 
 			byte[] tmp = new byte[1024];
 			while (true) {
 				while (in.available() > 0) {
 					int i = in.read(tmp, 0, 1024);
 					if (i < 0)
 						break;
 					result += "\n" + new String(tmp, 0, i);
 				}
 				if (channel.isClosed()) {
 					// SSH exit status
 					break;
 				}
 			}
 
 			// Set the response
 			setCurrentResponse(result);
 			channel.disconnect();
 			session.disconnect();
 		} catch (Exception e) {
 			log.error("Failed to execute ssh command.", e);
 			c.stopCounter();
 			response.setPageLoadTime(c.getDurationSeconds());
 			setCurrentResponse(e.toString());
 			return response;
 		}
 
 		// Check if the response contains the expectedString
 		if (getCurrentResponse() != null
 				&& getCurrentResponse().contains(this.getExpectedString())) {
 			response.setContainsSSHExpectedResponse(true);
 		}
 
 		if (getCurrentResponse() == null) {
 			log.warn("Received null response from SSH: " + this.getName());
 			setCurrentResponse("null");
 		}
 
 		// Stop the counter
 		c.stopCounter();
 		response.setPageLoadTime(c.getDurationSeconds());
 
 		return response;
 	}
 
 	/**
 	 * getHost
 	 * 
 	 * @return
 	 */
 	public final String getServer() {
 		return host;
 	}
 
 	/**
 	 * setServer
 	 * 
 	 * @return
 	 */
 	public final void setServer(String server) {
 		this.host = server;
 	}
 
 	/**
 	 * getPort
 	 * 
 	 * @return
 	 */
 	public final String getPort() {
 		return port;
 	}
 
 	/**
 	 * setPort
 	 * 
 	 * @return
 	 */
 	public final void setPort(String port) {
 		this.port = port;
 	}
 
 	/**
 	 * getCmdToExec
 	 * 
 	 * @return
 	 */
 	public final String getCmdToExec() {
 		return cmdToExecute;
 	}
 
 	/**
 	 * setCmdToExec
 	 * 
 	 * @return
 	 */
 	public final void setCmdToExec(String cmd) {
 		this.cmdToExecute = cmd;
 	}
 
 	/**
 	 * @return the user
 	 */
 	public final String getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user
 	 *            the user to set
 	 */
 	public final void setUser(String user) {
 		this.user = user;
 	}
 
 	/**
 	 * @param passwd
 	 *            the passwd to set
 	 */
 	public final void setPasswd(String passwd) {
 		setupMainPassword();
 		this.passwd = textEncryptor.encrypt(passwd);
 	}
 
 	/**
 	 * @return the passwd
 	 */
 	public final String getDecryptedPasswd() {
 		setupMainPassword();
 		return textEncryptor.decrypt(passwd);
 	}
 
 	/**
 	 * setupMainPassword
 	 */
 	public final void setupMainPassword() {
 		String propertiesFile = "meerkat.properties";
 		PropertiesLoader pL = new PropertiesLoader();
 		Properties properties = pL.getPropetiesFromFile(propertiesFile);
 		textEncryptor = new BasicTextEncryptor();
 		textEncryptor.setPassword(properties.getProperty("meerkat.password.master"));
 	}
 
 }
