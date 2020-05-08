 package org.ukiuni.callOtherJenkins.CallOtherJenkins;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.codec.binary.Base64;
 
 public class JenkinsRemoteIF {
 
 	private String hostName;
 	private String jobName;
 	private boolean useHttps;
 	private String userName;
 	private String password;
 	private String parameters = "";
 
 	public JenkinsRemoteIF(String hostName, String jobName, boolean useHttps) {
 		this.hostName = hostName;
 		this.jobName = jobName;
 		this.useHttps = useHttps;
 	}
 
 	public void setAuthentication(String userName, String password) {
 		this.userName = userName;
 		this.password = password;
 	}
 
 	public void setParameters(String parameters) {
 		if (null != parameters && !parameters.startsWith("?") && !"".equals(parameters)) {
 			parameters = "?" + parameters;
 		}
 		this.parameters = parameters;
 	}
 
 	public Long loadLastBuildNumber(PrintStream out) throws IOException {
 		String url = (useHttps ? "https" : "http") + "://" + hostName + "/job/" + jobName + "/api/json?tree=lastBuild[number]";
 		out.println("call " + url.toString());
 		HttpURLConnection connection = load(url);
 
 		JSONObject jsonResponse = JSONObject.fromObject(streamToString(connection.getInputStream(), "UTF-8"));
 		return jsonResponse.getJSONObject("lastBuild").getLong("number");
 	}
 
 	public Long loadNextBuildNumber(PrintStream out) throws IOException {
 		String url = (useHttps ? "https" : "http") + "://" + hostName + "/job/" + jobName + "/api/json?tree=nextBuildNumber";
 		out.println("call " + url.toString());
 		HttpURLConnection connection = load(url);
 		JSONObject jsonResponse = JSONObject.fromObject(streamToString(connection.getInputStream(), "UTF-8"));
 		return jsonResponse.getLong("nextBuildNumber");
 	}
 
 	private HttpURLConnection load(String urlString) throws IOException {
 		return load(urlString, false);
 	}
 
 	private HttpURLConnection load(String urlString, boolean post) throws IOException {
 		URL url = new URL(urlString);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		if (post) {
 			connection.setRequestMethod("POST");
 		}
 		if (null != userName && !"".equals(userName)) {
 			connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String((userName + ":" + password).getBytes("UTF-8")));
 		}
 		if (300 < connection.getResponseCode()) {
 			throw new IOException("wrong response code " + connection.getResponseCode());
 		}
 		return connection;
 	}
 
 	public void exec(PrintStream out) throws IOException {
		String url = ((useHttps ? "https" : "http") + "://" + hostName + "/job/" + jobName + "/build" + parameters);
 		out.println("call " + url.toString());
 		load(url, true);
 	}
 
 	public LastCompleteBuild seekEnd(PrintStream out, long number, long span, long retry) throws IOException, TimeoutException {
 		String url = (useHttps ? "https" : "http") + "://" + hostName + "/job/" + jobName + "/api/json?tree=lastCompletedBuild[number],lastSuccessfulBuild[number],lastFailedBuild[number]";
 		for (int i = 0; i < retry; i++) {
 			out.println("call " + (i + 1) + "'st " + url.toString());
 			HttpURLConnection connection = load(url);
 			JSONObject jsonResponse = JSONObject.fromObject(streamToString(connection.getInputStream(), "UTF-8"));
 
 			if (jsonResponse.getJSONObject("lastCompletedBuild").isNullObject()) {
 				continue;
 			}
 			Long buildNum = jsonResponse.getJSONObject("lastCompletedBuild").getLong("number");
 			if (null != buildNum && buildNum.longValue() >= number) {
 				LastCompleteBuild lastCompleteBuild = new LastCompleteBuild();
 				lastCompleteBuild.number = buildNum;
 				if ((!jsonResponse.getJSONObject("lastSuccessfulBuild").isNullObject()) && buildNum.equals(jsonResponse.getJSONObject("lastSuccessfulBuild").getLong("number"))) {
 					lastCompleteBuild.success = true;
 				}
 				if ((!jsonResponse.getJSONObject("lastFailedBuild").isNullObject()) && buildNum.equals(jsonResponse.getJSONObject("lastFailedBuild").getLong("number"))) {
 					lastCompleteBuild.success = false;
 				}
 				return lastCompleteBuild;
 			}
 			try {
 				Thread.sleep(span);
 			} catch (InterruptedException e) {
 				e.printStackTrace(out);
 			}
 		}
 		throw new TimeoutException();
 	}
 
 	public static class LastCompleteBuild {
 		public long number;
 		public boolean success;
 	}
 
 	@SuppressWarnings("serial")
 	public static class TimeoutException extends Exception {
 
 	}
 
 	private String streamToString(InputStream in, String encode) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		byte[] buffer = new byte[1024];
 		int readed = in.read(buffer);
 		while (readed > 0) {
 			out.write(buffer, 0, readed);
 			readed = in.read(buffer);
 		}
 		return new String(out.toByteArray(), encode);
 	}
 }
