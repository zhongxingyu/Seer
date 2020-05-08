 package net.nordu.logback;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.security.SecureRandom;
 import java.text.DateFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import ch.qos.logback.classic.spi.LoggingEvent;
 import ch.qos.logback.core.AppenderBase;
 import ch.qos.logback.core.net.SyslogConstants;
 import ch.qos.logback.core.net.SyslogOutputStream;
 
 public class FTicksAppender extends AppenderBase<LoggingEvent> {
 
 	// auditEventTime|requestBinding|requestId|relyingPartyId|messageProfileId|assertingPartyId|responseBinding|responseId|principalName|authNMethod|releasedAttributeId1,releasedAttributeId2,|nameIdentifier|assertion1ID,assertion2ID,|
 
 	final static int MSG_SIZE_LIMIT = 256 * 1024;
 
 	private File keyFile;
 	private String key;
 	private String syslogHost;
 	private SyslogOutputStream sw;
 	private int port = SyslogConstants.SYSLOG_PORT;
 	private int facility;
 	private long lastTimestamp = -1;
 	private String localHostName;
 	private String timesmapStr = null;
 	private SimpleDateFormat simpleFormat;
 	private String federationIdentifier;
 	private String version;
 	
 	public String getFederationIdentifier() {
 		return federationIdentifier;
 	}
 	
 	public String getVersion() {
 		return version;
 	}
 	
 	public void setFederationIdentifier(String federationIdentifier) {
 		this.federationIdentifier = federationIdentifier;
 	}
 	
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	/**
 	 * Returns the value of the <b>SyslogHost</b> option.
 	 */
 	public String getSyslogHost() {
 		return syslogHost;
 	}
 
 	/**
 	 * The <b>SyslogHost</b> option is the name of the the syslog host where log
 	 * output should go.
 	 * 
 	 * <b>WARNING</b> If the SyslogHost is not set, then this appender will
 	 * fail.
 	 */
 	public void setSyslogHost(String syslogHost) {
 		this.syslogHost = syslogHost;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/**
 	 * The port number on the syslog server to connect to. Nornally, wou would
 	 * not want to change the default value, that is 514.
 	 */
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	public void setKeyFile(File keyFile) throws IOException {
 		this.keyFile = keyFile;
 		key = makeRandomKey(keyFile);
 	}
 
 	public void setKeyFile(String keyFileName) throws IOException {
 		setKeyFile(new File(keyFileName));
 	}
 
 	private String makeRandomKey(File keyFile) throws IOException {
 		String key = null;
 
 		if (keyFile.exists()) {
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					new FileInputStream(keyFile)));
 			key = in.readLine();
 		}
 
 		if (key == null || key.length() == 0) {
 			SecureRandom random = new SecureRandom();
 			key = new BigInteger(130, random).toString(32);
 			PrintWriter out = new PrintWriter(new FileOutputStream(keyFile));
 			out.println(key);
 			out.close();
 		}
 
 		return key;
 	}
 
 	private String anonymize(String str) {
 		StringBuffer buf = new StringBuffer();
 		buf.append(key);
 		buf.append(str);
 		return DigestUtils.sha256Hex(buf.toString());
 	}
 
 	private String fticks(String msg) {
 		//System.err.println("f-ticks from: "+msg);
 		StringBuffer buf = new StringBuffer();
 		String fields[] = msg.split("\\|");
		String principalName = fields[8];
 
 		buf.append("F-TICKS");
 		buf.append("/").append(getFederationIdentifier());
 		buf.append("/").append(getVersion());
 		buf.append("#TS=").append(fields[0]);
		buf.append("#RP=").append(fields[3]);
		buf.append("#AP=").append(fields[5]);
		buf.append("#PN=").append(anonymize(principalName));
		buf.append("#AM=").append(fields[9]);
 		buf.append("#");
 
 		return buf.toString();
 	}
 
 	@Override
 	public void start() {
 		int errorCount = 0;
 		localHostName = getLocalHostname();
 
 		try {
 			// hours should be in 0-23, see also
 			// http://jira.qos.ch/browse/LBCLASSIC-48
 			simpleFormat = new SimpleDateFormat("MMM dd HH:mm:ss",new DateFormatSymbols(Locale.US));
 		} catch (IllegalArgumentException e) {
 			addError("Could not instantiate SimpleDateFormat", e);
 			errorCount++;
 		}
 
 		try {
 			sw = new SyslogOutputStream(syslogHost, port);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 			addError("Could not create SyslogWriter", e);
 			errorCount++;
 		} catch (SocketException e) {
 			e.printStackTrace();
 			errorCount++;
 			addError("Failed to bind to a random datagram socket ", e);
 		}
 		
 		if (errorCount == 0) {
 			super.start();
 		}
 	}
 
 	@Override
 	public void stop() {
 		sw.close();
 		super.stop();
 	}
 
 	@Override
 	protected void append(LoggingEvent eventObject) {
 		if (!isStarted()) {
 			return;
 		}
 
 		try {
 			StringBuilder sb = new StringBuilder();
 
 			int pri = SyslogConstants.LOG_AUTH + SyslogConstants.INFO_SEVERITY;
 
 			sb.append("<");
 			sb.append(pri);
 			sb.append(">");
 			fillInTimestamp(sb, eventObject.getTimeStamp());
 			sb.append(' ');
 			sb.append(localHostName);
 			sb.append(' ');
 
 			sb.append(fticks(eventObject.getMessage()));
 			String msg = sb.toString();
 
 			if (msg != null && msg.length() > MSG_SIZE_LIMIT) {
 				msg = msg.substring(0, MSG_SIZE_LIMIT);
 			}
 			
 			sw.write(msg.getBytes());
 			sw.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 			addError("Failed to send diagram to " + syslogHost, e);
 			stop();
 		}
 	}
 
 	/**
 	 * This method gets the network name of the machine we are running on.
 	 * Returns "UNKNOWN_LOCALHOST" in the unlikely case where the host name
 	 * cannot be found.
 	 * 
 	 * @return String the name of the local host
 	 */
 	public String getLocalHostname() {
 		try {
 			InetAddress addr = InetAddress.getLocalHost();
 			return addr.getHostName();
 		} catch (UnknownHostException uhe) {
 			addError("Could not determine local host name", uhe);
 			return "UNKNOWN_LOCALHOST";
 		}
 	}
 
 	void fillInTimestamp(StringBuilder sb, long timestamp) {
 		// if called multiple times within the same millisecond
 		// use last value
 		if (timestamp != lastTimestamp) {
 			lastTimestamp = timestamp;
 			timesmapStr = simpleFormat.format(new Date(timestamp));
 		}
 		sb.append(timesmapStr);
 	}
 }
