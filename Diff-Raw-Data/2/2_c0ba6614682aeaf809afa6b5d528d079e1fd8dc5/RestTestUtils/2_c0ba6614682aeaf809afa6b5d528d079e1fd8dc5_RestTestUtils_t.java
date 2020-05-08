 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.cloudifysource.domain.Application;
 import org.cloudifysource.domain.Service;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.DSLUtils;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.Packager;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.restclient.StringUtils;
 
 public class RestTestUtils {
 
 
 	private static HttpResponse sendRestPostRequest(String uri, Map<String, File> filesToPost, Map<String, String> params) 
 			throws ClientProtocolException, IOException {
 
 		final MultipartEntity reqEntity = new MultipartEntity();
 		for (Entry<String, File> entry : filesToPost.entrySet()) {
 			FileBody fileBody = new FileBody(entry.getValue());
 			reqEntity.addPart(entry.getKey(), fileBody);
 		}
 
 		if (params != null) {
 			for (Map.Entry<String, String> param : params.entrySet()) {
 				reqEntity.addPart(param.getKey(), new StringBody(param.getValue(), Charset.forName("UTF-8")));
 			}
 		}
 
 		HttpPost post = new HttpPost(uri);
 		post.setEntity(reqEntity);
 
 		DefaultHttpClient httpClient = new DefaultHttpClient();	
 		return httpClient.execute(post);
 	}
 
 	/**
 	 * 
 	 * @param restUrl
 	 * @param serviceName
 	 * @param serviceDir
 	 * @param props
 	 * @param params
 	 * @param overridesFile
 	 * @throws IOException
 	 * @throws DSLException
 	 * @throws PackagingException
 	 */
 	public static void installServiceUsingRestApi(final String restUrl, final String serviceName, 
 			final File serviceDir, final Properties props, Map<String, String> params,  
 			final File overridesFile) throws IOException, DSLException, PackagingException {
 		
 		// create zip file
		final Service service = ServiceReader.readService(null, serviceDir, null, true, overridesFile);
 		File packedFile = Packager.pack(serviceDir, service, new LinkedList<File>());
 
 		// add files to post
 		Map<String, File> filesToPost = new HashMap<String, File>();
 		filesToPost.put("file", packedFile);
 		filesToPost.put("serviceOverridesFile", overridesFile);
 		Properties properties = props;
 		if (props == null) {
 			properties = createServiceContextProperties(service, serviceName + DSLUtils.SERVICE_DSL_FILE_NAME_SUFFIX);
 		}
 		filesToPost.put("props", storePropertiesInTempFile(properties));
 
 		// execute
 		String uri = restUrl + "/service/applications/default/services/" + serviceName + "/timeout/15"
 				+ "?zone=" + serviceName + "&selfHealing=" + Boolean.toString(false);
 		InputStream instream = null;
 		try {
 			HttpResponse resposne = sendRestPostRequest(uri, filesToPost, params);
 			instream = resposne.getEntity().getContent();
 			String responseBody = StringUtils.getStringFromStream(instream);
 			final int statusCode = resposne.getStatusLine().getStatusCode();
 			Assert.assertEquals("Failed to install service. status code: " + statusCode + ", response body: " 
 					+ responseBody, 200, statusCode);
 		} catch (Exception e) {
 			Assert.fail("Failed to get response from " + uri);
 			e.printStackTrace();
 		} finally {
 			if (instream != null) {
 				instream.close();
 			}
 		}
 	}
 	
 	public static void installApplicationUsingRestApi(final String restUrl, final String applicationName
 			, final File applicationDir, final File applicationOverridesFile) 
 					throws IOException, DSLException, PackagingException {
 		// create application zip file
 		Application application = ServiceReader.getApplicationFromFile(applicationDir, applicationOverridesFile).getApplication();
 		File packApplication = Packager.packApplication(application, applicationDir);
 
 		// add files to post
 		Map<String, File> filesToPost = new HashMap<String, File>();
 		filesToPost.put("file", packApplication);
 		filesToPost.put(CloudifyConstants.APPLICATION_OVERRIDES_FILE_PARAM, applicationOverridesFile);
 
 		// execute
 		String uri = restUrl + "/service/applications/" + applicationName + "/timeout/10";
 		InputStream instream = null;
 		try {
 			HttpResponse resposne = sendRestPostRequest(uri, filesToPost, null);
 			instream = resposne.getEntity().getContent();
 			String responseBody = StringUtils.getStringFromStream(instream);
 			final int statusCode = resposne.getStatusLine().getStatusCode();
 			Assert.assertEquals("Failed to install service. status code: " + statusCode + ", response body: " 
 					+ responseBody, 200, statusCode);
 		} catch (Exception e) {
 			Assert.fail("Failed to get response from " + uri);
 			e.printStackTrace();
 		} finally {
 			if (instream != null) {
 				instream.close();
 			}
 		}
 	}
 
 	private static File storePropertiesInTempFile(Properties props) throws IOException {
 		File tempFile = File.createTempFile("props", ".tmp");
 		Writer writer = null;
 		try {
 			writer = new FileWriter(tempFile);
 			props.store(writer, "");
 		} finally {
 			if (writer != null) {
 				writer.close();
 			}
 		}
 		return tempFile;
 	}
 
 	public static Properties createServiceContextProperties(final Service service, final String serviceGroovyFileName) {
 		final Properties contextProperties = new Properties();
 
 		// contextProperties.setProperty("com.gs.application.services",
 		// serviceNamesString);
 		if (service.getDependsOn() != null) {
 			contextProperties.setProperty(
 					CloudifyConstants.CONTEXT_PROPERTY_DEPENDS_ON, service
 					.getDependsOn().toString());
 		}
 		if (service.getType() != null) {
 			contextProperties.setProperty(
 					CloudifyConstants.CONTEXT_PROPERTY_SERVICE_TYPE,
 					service.getType());
 		}
 		if (service.getIcon() != null) {
 			contextProperties.setProperty(
 					CloudifyConstants.CONTEXT_PROPERTY_SERVICE_ICON,
 					CloudifyConstants.SERVICE_EXTERNAL_FOLDER
 					+ service.getIcon());
 		}
 		if (service.getNetwork() != null) {
 			if (service.getNetwork().getProtocolDescription() != null) {
 				contextProperties
 				.setProperty(
 						CloudifyConstants.CONTEXT_PROPERTY_NETWORK_PROTOCOL_DESCRIPTION,
 						service.getNetwork().getProtocolDescription());
 			}
 		}
 		if (serviceGroovyFileName != null) {
 			contextProperties
 			.setProperty(CloudifyConstants.CONTEXT_PROPERTY_SERVICE_FILE_NAME, serviceGroovyFileName);
 		}
 
 		contextProperties.setProperty(
 				CloudifyConstants.CONTEXT_PROPERTY_ELASTIC,
 				Boolean.toString(service.isElastic()));
 
 		return contextProperties;
 
 	}
 }
