 package com.dgrid.handlers;
 
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.dgrid.api.BaseJobletTypeHandler;
 import com.dgrid.api.JobletTypeHandler;
 import com.dgrid.gen.JOB_STATUS;
 import com.dgrid.gen.Joblet;
 import com.dgrid.gen.JobletResult;
 import com.dgrid.helpers.HttpClientHelper;
 import com.dgrid.helpers.MailSender;
 import com.dgrid.service.DGridClient;
 import com.dgrid.service.XmppConnection;
 import com.dgrid.util.io.HostnameDiscovery;
 import com.dgrid.util.webclient.HttpResponse;
 
 public class CallbackJobletTypeHandler extends BaseJobletTypeHandler implements
 		JobletTypeHandler {
 	public static final String TYPE = "callback";
 
 	private Log log = LogFactory.getLog(getClass());
 
 	public JobletResult execute(Joblet joblet, DGridClient gridClient)
 			throws Throwable {
 		log.trace("execute()");
 		long start = System.currentTimeMillis();
 		Map<String, String> params = joblet.getParameters();
 		int jobId = Integer.parseInt(params.get("jobId"));
 		String status = params.get("jobStatus");
 		String callbackType = params.get("callbackType");
 		String callbackAddress = params.get("callbackAddress");
 
 		String subject = getSubject(jobId, status);
 		String body = getBody(joblet.getContent(), jobId, status, joblet);
 
 		if ("xmpp".equals(callbackType)) {
 			xmppCallback(callbackAddress, subject, body, gridClient);
 		} else if ("http".equals(callbackType)) {
 			httpCallback(callbackAddress, jobId, gridClient);
 		} else if ("email".equals(callbackType)) {
 			emailCallback(callbackAddress, subject, body, gridClient);
 		} else if ("job".equals(callbackType)) {
 			jobCallback(Integer.parseInt(callbackAddress), gridClient);
 		} else {
 			throw new RuntimeException("unknown job callback type");
 		}
 		return new JobletResult(0, 0l, 0, (System.currentTimeMillis() - start),
 				JOB_STATUS.COMPLETED, "", joblet);
 	}
 
 	private void xmppCallback(String to, String subject, String body,
 			DGridClient gridClient) throws Exception {
 		log.trace("xmppCallback()");
 		XmppConnection conn = (XmppConnection) gridClient
 				.getBean(XmppConnection.NAME);
 		conn.sendTextMessage(to, subject, body);
 	}
 
 	private void httpCallback(String url, int jobId, DGridClient gridClient)
 			throws Exception {
 		log.trace("httpCallback()");
 		HttpClientHelper httpClient = (HttpClientHelper) gridClient
 				.getBean(HttpClientHelper.NAME);
 		url = String.format(url, jobId);
		HttpResponse response = httpClient.getPage(url);
		response.releaseConnection();
 	}
 
 	private void emailCallback(String to, String subject, String body,
 			DGridClient gridClient) throws Exception {
 		log.trace("emailCallback()");
 		String from = gridClient.getSetting("mail.from", String.format(
 				"grid@%1$s", HostnameDiscovery.getHostname()));
 		MailSender mailSender = (MailSender) gridClient
 				.getBean(MailSender.NAME);
 		mailSender.send(from, to, subject, body);
 	}
 
 	private void jobCallback(int jobId, DGridClient gridClient) {
 		log.trace("jobCallback()");
 		throw new RuntimeException("jobCallback() is not yet implemented()");
 	}
 
 	private String getSubject(int jobId, String status) {
 		log.trace("getSubject()");
 		return String.format("Job # %1$d completed with status %2$s", jobId,
 				status);
 	}
 
 	private String getBody(String content, int jobId, String status,
 			Joblet joblet) {
 		log.trace("getBody()");
 		if ((content != null) && (content.length() > 0))
 			return content;
 		else
 			return String.format("Job # %1$d completed with status %2$s.",
 					jobId, status);
 	}
 
 }
