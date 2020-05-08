 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.livereload.internal.server.jetty;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jetty.websocket.WebSocket.Connection;
 import org.eclipse.jetty.websocket.WebSocketClient;
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.util.SocketUtil;
 import org.jboss.tools.livereload.core.internal.server.wst.LiveReloadServerBehaviour;
 import org.jboss.tools.livereload.core.internal.service.EventService;
 import org.jboss.tools.livereload.core.internal.service.ServerLifeCycleListener;
 import org.jboss.tools.livereload.core.internal.util.TimeoutUtils;
 import org.jboss.tools.livereload.core.internal.util.TimeoutUtils.TaskMonitor;
 import org.jboss.tools.livereload.core.internal.util.WSTUtils;
 import org.jboss.tools.livereload.internal.AbstractCommonTestCase;
 import org.jboss.tools.livereload.internal.WorkbenchUtils;
 import org.jboss.tools.livereload.test.previewserver.PreviewServerBehaviour;
 import org.jboss.tools.livereload.test.previewserver.PreviewServerFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author xcoulon
  * 
  */
 @SuppressWarnings("restriction")
 public class LiveReloadServerTestCase extends AbstractCommonTestCase {
 
 	/** LiveReload server to test. */
 	private IServer liveReloadServer = null;
 
 	/** Optional HTTP Preview Server. */
 	private IServer httpPreviewServer = null;
 
 	private int liveReloadServerPort = -1;
 
 	/** File location prefixed with file:// scheme. */
 	private String indexFileLocation;
 
 	/** File location prefixed with http:// scheme. */
 	private String indexDocumentlocation;
 	
 	private String unknownServerLocation;
 
 	private String unknowDocumentLocation;
 
 	private String cssDocumentLocation;
 
 	private String folderDocumentLocation;
 
 	private LiveReloadServerBehaviour liveReloadServerBehaviour;
 
 	@Before
 	public void setup() throws IOException, CoreException {
 		// remove all servers
 		for (final IServer server : ServerCore.getServers()) {
 			server.stop(true);
 			TaskMonitor monitor = new TaskMonitor() {
 				@Override
 				public boolean isComplete() {
 					return !(server.canStop().isOK());
 				}
 			};
 			TimeoutUtils.timeout(monitor, 2, TimeUnit.SECONDS);
 			server.delete();
 		}
 		//
 		EventService.getInstance().resetSubscribers();
 		liveReloadServerPort = SocketUtil.findUnusedPort(50000, 55000);
 		final IResource index_html_file = project.findMember("WebContent" + File.separator + "index.html");
 		indexFileLocation = "file://" + index_html_file.getLocation().toOSString();
 		indexDocumentlocation = "http://localhost:" + liveReloadServerPort + "/" + project.getName() + "/"
 				+ index_html_file.getLocation().makeRelativeTo(project.getLocation()).toOSString();
 		unknownServerLocation = "http://localhost:12345/index.html";
 		unknowDocumentLocation = indexDocumentlocation.replace("index.html", "unknown.html");
 		cssDocumentLocation = indexDocumentlocation.replace("index.html", "/styles.css");
 		folderDocumentLocation = indexDocumentlocation.replace("index.html", "");
 	}
 
 	/**
 	 * @throws CoreException
 	 * @throws TimeoutException
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 * 
 	 */
 	private IServer createAndLaunchLiveReloadServer(final String serverName, final boolean injectScript)
 			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
 		final IServer server = WSTUtils.createLiveReloadServer(serverName, liveReloadServerPort,
 				injectScript, false);
 		liveReloadServerBehaviour = (LiveReloadServerBehaviour) WSTUtils.findServerBehaviour(server);
 		assertThat(liveReloadServerBehaviour).isNotNull();
 		liveReloadServer = liveReloadServerBehaviour.getServer();
 		assertThat(liveReloadServer).isNotNull();
 		assertThat(liveReloadServer.canStart(ILaunchManager.RUN_MODE).isOK()).isTrue();
 
 		startServer(liveReloadServer, 60, TimeUnit.SECONDS);
 		return server;
 	}
 
 	/**
 	 * @throws CoreException
 	 * @throws TimeoutException
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 * 
 	 */
 	private IServer createAndLaunchLiveReloadServer(final boolean injectScript)
 			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
 		return createAndLaunchLiveReloadServer("LiveReload Test Server at localhost", injectScript);
 	}
 	
 	/**
 	 * Creates an HTTP Preview Server but does not start it.
 	 * 
 	 * @return
 	 * 
 	 * @throws CoreException
 	 * @throws InterruptedException
 	 */
 	private int createHttpPreviewServer() throws InterruptedException, CoreException {
 		this.httpPreviewServer = PreviewServerFactory.createServer(project);
 		return httpPreviewServer.getAttribute(PreviewServerBehaviour.PORT, -1);
 	}
 
 	/**
 	 * @return
 	 * @throws Exception
 	 */
 	private WebSocketClient createWebSocketClient() throws Exception {
 		final WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory();
 		webSocketClientFactory.setBufferSize(4096);
 		webSocketClientFactory.start();
 		WebSocketClient webSocketClient = new WebSocketClient(webSocketClientFactory);
 		return webSocketClient;
 	}
 
 	/**
 	 * @param client
 	 * @return
 	 * @throws Exception
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 * @throws TimeoutException
 	 */
 	private Connection connectFrom(final LiveReloadTestClient client) throws Exception, IOException,
 			URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
 		final WebSocketClient webSocketClient = createWebSocketClient();
 		final Future<Connection> future = webSocketClient.open(new URI("ws://localhost:" + liveReloadServerPort
 				+ "/livereload"), client);
 		// final Connection connection = future.get(5, TimeUnit.SECONDS);
 		final Connection connection = future.get();
 		Thread.sleep(200);
 		return connection;
 	}
 
 	@Test
 	public void shouldAcceptWebsocketConnexionWithoutProxy() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
 		// operation
 		final Connection connection = connectFrom(client);
 		// verification
 		assertThat(connection.isOpen()).isTrue();
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
 		connection.close();
 	}
 
 	@Test
 	public void shouldNotAcceptWebsocketConnexionWithoutValidUrlInfo() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final LiveReloadTestClient client = new LiveReloadTestClient("");
 		// operation
 		final Connection connection = connectFrom(client);
 		// verification
 		assertThat(connection.isOpen()).isFalse();
 		// assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(0);
 		connection.close();
 	}
 
 	@Test
 	public void shouldAcceptWebsocketConnexionWithProxy() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
 		// operation
 		final Connection connection = connectFrom(client);
 		// verification
 		assertThat(connection.isOpen()).isTrue();
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
 		connection.close();
 	}
 	
 	@Test
 	public void shouldAcceptWebsocketEvenIfServerUnknown() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final LiveReloadTestClient client = new LiveReloadTestClient(unknownServerLocation);
 		// operation
 		final Connection connection = connectFrom(client);
 		// verification
 		assertThat(connection.isOpen()).isTrue();
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
 		connection.close();
 	}
 	
 	@Test
 	public void shouldAcceptWebsocketEvenIfServerUnknownAndProjectUnknown() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final LiveReloadTestClient client = new LiveReloadTestClient(unknownServerLocation.replace(project.getName(), "foobar"));
 		// operation
 		final Connection connection = connectFrom(client);
 		// verification
 		assertThat(connection.isOpen()).isTrue();
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
 		connection.close();
 	}
 	
 	@Test
 	public void shouldAcceptHttpConnexionAndReturnHtmlResource() throws Exception {
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(indexDocumentlocation);
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 	}
 
 	@Test
 	public void shouldAcceptHttpConnexionAndReturnNotFoundResource() throws Exception {
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(unknowDocumentLocation);
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_NOT_FOUND);
 	}
 
 	@Test
 	public void shouldAcceptHttpConnexionAndReturnForbiddenResponseWhenRequestingFolder() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(folderDocumentLocation);
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_FORBIDDEN);
 	}
 
 	@Test
 	public void shouldNotInjectLiveReloadScriptInHtmlPage() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final String scriptContent = new StringBuilder(
 				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
 				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(indexDocumentlocation);
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).doesNotContain(scriptContent);
 
 	}
 
 	@Test
 	public void shouldInjectLiveReloadScriptInHtmlPageWithSimpleAcceptedTypes() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final String scriptContent = new StringBuilder(
 				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
 				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(indexDocumentlocation);
 		method.addRequestHeader("Accept", "text/html");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).contains(scriptContent);
 	}
 	
 	@Test
 	// @see https://issues.jboss.org/browse/JBIDE-15317
 	public void shouldNotCorruptHtmlContentWhenInjectingScript() throws CoreException, InterruptedException, ExecutionException, TimeoutException, HttpException, IOException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final String scriptContent = new StringBuilder(
 				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
 				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
 		final IResource chineseHtmlFile = project.findMember("WebContent" + File.separator + "chinese.html");
 		final String chineseDocumentlocation = "http://localhost:" + liveReloadServerPort + "/" + project.getName() + "/"
 				+ chineseHtmlFile.getLocation().makeRelativeTo(project.getLocation()).toOSString();
 				assertThat(chineseHtmlFile).isNotNull();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(chineseDocumentlocation);
 		method.addRequestHeader("Accept", "text/html");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).contains(scriptContent);
 		LOGGER.debug(responseBody);
 		assertThat(responseBody).doesNotContain("??");
 		assertThat(responseBody).contains("中文");
 	}
 
 	@Test
 	// @see https://issues.jboss.org/browse/JBIDE-15317
 	public void shouldNotCorruptHtmlContentWhenNotInjectingScript() throws CoreException, InterruptedException, ExecutionException, TimeoutException, HttpException, IOException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		final IResource chineseHtmlFile = project.findMember("WebContent" + File.separator + "chinese.html");
 		final String chineseDocumentlocation = "http://localhost:" + liveReloadServerPort + "/" + project.getName() + "/"
 				+ chineseHtmlFile.getLocation().makeRelativeTo(project.getLocation()).toOSString();
 				assertThat(chineseHtmlFile).isNotNull();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(chineseDocumentlocation);
 		method.addRequestHeader("Accept", "text/html");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).doesNotContain("<script");
 		LOGGER.debug(responseBody);
 		assertThat(responseBody).doesNotContain("???");
 		assertThat(responseBody).contains("中文");
 	}
 	
 	@Test
 	public void shouldInjectLiveReloadScriptInHtmlPageWithSimpleAcceptedTypeAndcharset() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final String scriptContent = new StringBuilder(
 				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
 		.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(indexDocumentlocation);
 		method.addRequestHeader("Accept", "text/html;charset=UTF-8");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).contains(scriptContent);
 	}
 
 	@Test
 	public void shouldInjectLiveReloadScriptInHtmlPageWithMultipleAcceptedTypeAndQualityFactors() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final String scriptContent = new StringBuilder(
 				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
 		.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(indexDocumentlocation);
 		method.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).contains(scriptContent);
 	}
 
 	@Test
 	public void shouldGetLiveReloadScriptWithProxyEnabled() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod("http://localhost:" + liveReloadServerPort + "/livereload.js");
 		method.addRequestHeader("Accept", "text/javascript");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).isNotEmpty();
 	}
 
 	@Test
 	public void shouldNotInjectLiveReloadScriptInCssPage() throws Exception {
 		createAndLaunchLiveReloadServer(false);
 		final String scriptContent = new StringBuilder(
 				"<script>document.write('<script src=\"http://' + location.host.split(':')[0]+ ':")
 				.append(liveReloadServerPort).append("/livereload.js\"></'+ 'script>')</script>").toString();
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(cssDocumentLocation);
 		method.addRequestHeader("Accept", "text/css");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		assertThat(responseBody).doesNotContain(scriptContent);
 	}
 
 	@Test
 	public void shouldBeNotifiedWhenLocalFileChangedWithProxyEnabled() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final LiveReloadTestClient client = new LiveReloadTestClient(indexDocumentlocation);
 		// operation
 		final Connection connection = connectFrom(client);
 		// operation : trigger a resource changed event
 		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
 				"Hello, LiveReload !");
 		Thread.sleep(200);
 		// verification: client should have been notified with a reload message
 		assertThat(client.isNotificationReceived()).isEqualTo(true);
 		// end
 		connection.close();
 	}
 
 	@Test
 	public void shouldBeNotifiedWhenLocalFileChangedWithProxyEnabledAndUnknownServerLocation() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final LiveReloadTestClient client = new LiveReloadTestClient(unknownServerLocation);
 		// operation
 		final Connection connection = connectFrom(client);
 		// operation : trigger a resource changed event
 		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
 				"Hello, LiveReload !");
 		Thread.sleep(200);
 		// verification: client should have been notified with a reload message
 		assertThat(client.isNotificationReceived()).isEqualTo(true);
 		assertThat(client.getReceivedNotification().contains(unknownServerLocation));
 		// end
 		connection.close();
 	}
 
 	@Test
 	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledButNotUsed() throws Exception {
 		// pre-condition
 		final int httpPreviewPort = createHttpPreviewServer();
 		createAndLaunchLiveReloadServer(true);
 		final String indexRemoteDocumentlocation = "http://localhost:" + httpPreviewPort + "/" + project.getName()
 				+ "/index.html";
 		final LiveReloadTestClient client = new LiveReloadTestClient(indexRemoteDocumentlocation);
 		// operation: start server and connect to it
 		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
 		final Connection connection = connectFrom(client);
 		// operation: simulate publish
 		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
 		Thread.sleep(200);
 		// verification: client should have been notified with a reload message
 		assertThat(client.isNotificationReceived()).isEqualTo(true);
 		assertThat(client.getReceivedNotification()).contains("http://localhost:" + httpPreviewPort);
 		// end
 		connection.close();
 	}
 
 	@Test
 	public void shouldBeNotifiedWhenRemoteResourceDeployedWithProxyEnabledAndUsed() throws Exception {
 		// pre-condition
 		final int httpPreviewPort = createHttpPreviewServer();
 		createAndLaunchLiveReloadServer(true);
 		final String indexRemoteDocumentlocation = "http://localhost:" + httpPreviewPort + "/" + project.getName()
 				+ "/index.html";
 		final LiveReloadTestClient client = new LiveReloadTestClient(indexRemoteDocumentlocation);
 		// operation: start server and connect to it
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		final Connection connection = connectFrom(client);
 		// operation: simulate HTTP preview server publish
 		((Server) httpPreviewServer).publish(IServer.PUBLISH_AUTO, new NullProgressMonitor());
 		Thread.sleep(200);
 		// verification: client should have been notified with a reload message
 		assertThat(client.isNotificationReceived()).isEqualTo(true);
 		assertThat(client.getReceivedNotification()).doesNotContain("http://localhost:" + this.liveReloadServerPort);
 		// end
 		connection.close();
 
 	}
 
 	@Test
 	public void shouldNotBeNotifiedWhenConnectionClosed() throws Exception {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		final LiveReloadTestClient client = new LiveReloadTestClient(indexFileLocation);
 		// operation
 		final Connection connection = connectFrom(client);
 		// operation : trigger a resource changed event
 		connection.close();
 		WorkbenchUtils.replaceAllOccurrencesOfCode("WebContent/index.html", project, "Hello, World",
 				"Hello, LiveReload !");
 		Thread.sleep(200);
 		// verification: client should have been notified with a reload message
 		assertThat(client.isNotificationReceived()).isEqualTo(false);
 		// end
 	}
 
 	@Test
 	public void shouldNotInjectLiveReloadScriptInUnknownHtmlPage() throws Exception {
 		createAndLaunchLiveReloadServer(false);
 		// operation
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod(unknowDocumentLocation);
 		method.addRequestHeader("Accept", "text/css");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_NOT_FOUND);
 	}
 
 	@Test
 	public void shouldAddServerListenerWhenCreatingHttpPreviewServerAfterLiveReloadServerAndProxyModeEnabled()
 			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		createHttpPreviewServer();
 		// verification
 		assertThat(
 				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
 						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
 	}
 
 	@Test
 	public void shouldAddServerListenerWhenCreatingLiveReloadServerAfterHttpPreviewServerAndProxyModeEnabled()
 			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
 		// pre-condition
 		createHttpPreviewServer();
 		// operation
 		createAndLaunchLiveReloadServer(true);
 		// verification
 		assertThat(
 				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
 						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
 	}
 
 	@Test
 	public void shouldRemoveServerListenerWhenDeletingServer() throws InterruptedException, CoreException,
 			ExecutionException, TimeoutException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(false);
 		createHttpPreviewServer();
 		assertThat(
 				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
 						ServerLifeCycleListener.SERVER_LISTENER)).contains(httpPreviewServer);
 		// operation
 		httpPreviewServer.delete();
 		// verification
 		assertThat(
 				liveReloadServerBehaviour.getServerLifeCycleListener().getSupervisedServers(
 						ServerLifeCycleListener.SERVER_LISTENER)).isEmpty();
 	}
 
 	@Test
 	public void shouldAddProxyWhenStartingHttpPreviewServerAndProxyModeEnabled() throws CoreException,
 			InterruptedException, ExecutionException, TimeoutException {
 		// pre-condition
 		createHttpPreviewServer();
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
 		// verification
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 	}
 
 	public void shouldAddProxyWhenCreatingAndStartingHttpPreviewServerAndProxyModeEnabled() throws CoreException,
 			InterruptedException, ExecutionException, TimeoutException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		// operation
 		createHttpPreviewServer();
 		((Server) httpPreviewServer).setServerState(IServer.STATE_STARTED);
 		// verification
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 	}
 
 	@Test
 	public void shouldAddProxyWhenCreatingLiveReloadServerAndHttpPreviewServerStartedAndProxyModeEnabled()
 			throws CoreException, InterruptedException, ExecutionException, TimeoutException {
 		// pre-condition
 		createHttpPreviewServer();
 		startServer(httpPreviewServer, 30, TimeUnit.SECONDS);
 		// operation
 		createAndLaunchLiveReloadServer(false);
 		// verification
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 	}
 
 	@Test
 	public void shouldRemoveProxyWhenDeletingServer() throws InterruptedException, CoreException, ExecutionException,
 			TimeoutException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		createHttpPreviewServer();
 		// operation
 		httpPreviewServer.delete();
 		// verification
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
 	}
 
 	@Test
 	public void shouldRemoveProxyWhenStoppingServer() throws InterruptedException, CoreException, ExecutionException,
 			TimeoutException {
 		// pre-condition
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		// operation
 		httpPreviewServer.stop(true);
 		// give it a little time to actually stop (in a separate thread)
 		TimeoutUtils.timeout(new TaskMonitor() {
 			@Override
 			public boolean isComplete() {
 				return !(httpPreviewServer.canStop().isOK());
 			}
 		}, 5, TimeUnit.SECONDS);
 		TimeoutUtils.timeout(new TaskMonitor() {
 			@Override
 			public boolean isComplete() {
 				return liveReloadServerBehaviour.getProxyServers().keySet().isEmpty();
 			}
 		}, 5, TimeUnit.SECONDS);
 		// verification
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).isEmpty();
 	}
 
 	@Test
 	public void shouldStopLiveReloadServerIfRunning() throws CoreException, InterruptedException, ExecutionException,
 			TimeoutException {
 		// precondition
 		createAndLaunchLiveReloadServer(false);
 		assertThat(liveReloadServer.canStop().isOK()).isTrue();
 		// operation
 		liveReloadServer.stop(true);
 		// give it a little time to actually stop (in a separate thread)
 		TaskMonitor monitor = new TaskMonitor() {
 			@Override
 			public boolean isComplete() {
 				return !(liveReloadServer.canStop().isOK());
 			}
 		};
 		TimeoutUtils.timeout(monitor, 2, TimeUnit.SECONDS);
 		// verification
 		assertThat(liveReloadServer.canStop().isOK()).isFalse();
 	}
 
 	@Test
 	public void shouldForwardRequestOnProxiedServerWithoutScriptInjection() throws Exception {
 		// pre-condition
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(false);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod("http://localhost:" + proxyPort + "/" + projectName
 				+ "/WebContent/index.html");
 		method.addRequestHeader("Accept", "text/html");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		// verification
 		assertThat(responseBody).contains("Hello, World!");
 		assertThat(responseBody).doesNotContain("livereload.js");
 	}
 
 	@Test(timeout=60*10000)
 	public void shouldForwardRequestOnProxiedServerWithScriptInjectionAndDefaultCharset() throws Exception {
 		// pre-condition
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod("http://localhost:" + proxyPort + "/" + projectName
 				+ "/WebContent/index.html");
 		method.addRequestHeader("Accept", "text/html");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the response body.
 		// Read the returned content type.
 		assertThat(method.getResponseHeader("Content-Type").getValue()).isEqualTo("text/html;charset=UTF-8");
 		String responseBody = method.getResponseBodyAsString();
 		// verification
 		assertThat(responseBody).contains("Hello, World!");
 		assertThat(responseBody).contains("livereload.js").contains(
 				Integer.toString(liveReloadServerBehaviour.getLiveReloadServer().getPort()));
 	}
 	
 	@Test(timeout=60*10000)
 	public void shouldForwardRequestOnProxiedServerWithScriptInjectionAndCustomCharset() throws Exception {
 		// pre-condition
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod("http://localhost:" + proxyPort + "/" + projectName
 				+ "/WebContent/index.html");
 		method.addRequestHeader("Accept", "text/html;charset=ISO-8859-1");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(HttpStatus.SC_OK);
 		// Read the returned content type.
 		assertThat(method.getResponseHeader("Content-Type").getValue()).isEqualTo("text/html;charset=ISO-8859-1");
 		// Read the response body.
 		String responseBody = method.getResponseBodyAsString();
 		// verification
 		assertThat(responseBody).contains("Hello, World!");
 		assertThat(responseBody).contains("livereload.js").contains(
 				Integer.toString(liveReloadServerBehaviour.getLiveReloadServer().getPort()));
 	}
 	
 	@Test
 	public void shouldForwardRequestWithQueryParams() throws IOException, CoreException, InterruptedException, ExecutionException, TimeoutException {
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation: send a request with a query param. The Preview server has a special servlet that will return a 400 error if the 
 		// proxy did not forward the query param 
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		HttpClient client = new HttpClient();
 		HttpMethod method = new GetMethod("http://localhost:" + proxyPort + "/foo/bar?w00t=true");
 		int status = client.executeMethod(method);
 		// verification
 		assertThat(status).isEqualTo(200);
 	}
 
 	@Test
 	public void shouldAllowWebSocketConnectionFromProxiedLocation() throws Exception {
 		// pre-condition
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		final LiveReloadTestClient client = new LiveReloadTestClient("http://localhost:" + proxyPort + "/"
 				+ projectName + "/WebContent/index.html");
 		// operation
 		final Connection connection = connectFrom(client);
 		// verification
 		assertThat(connection.isOpen()).isTrue();
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
 		connection.close();
 	}
 
 	@Test
 	public void shouldReuseSameProxyPortAfterServerRestart() throws Exception {
 		// pre-condition
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		final LiveReloadTestClient client = new LiveReloadTestClient("http://localhost:" + proxyPort + "/"
 				+ projectName + "/WebContent/index.html");
 		// operation
 		final Connection connection = connectFrom(client);
 		// operation: restart the Preview Server
 		httpPreviewServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		// verification
 		assertThat(connection.isOpen()).isTrue();
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(1);
 		int newProxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0]
 				.getPort();
 		assertThat(proxyPort).isEqualTo(newProxyPort);
 		connection.close();
 	}
 
 	@Test(timeout=30*1000)
 	public void shouldReuseSameProxyPortAfterLiveReloadServerRestart() throws Exception {
 		// pre-condition
 		createHttpPreviewServer();
 		httpPreviewServer.start(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		createAndLaunchLiveReloadServer(true);
 		assertThat(liveReloadServerBehaviour.getProxyServers().keySet()).contains(httpPreviewServer);
 		// operation
 		int proxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0].getPort();
 		final LiveReloadTestClient client = new LiveReloadTestClient("http://localhost:" + proxyPort + "/"
 				+ projectName + "/WebContent/index.html");
 		// operation
 		connectFrom(client);
 		// operation: restart the LiveReload Server
 		WSTUtils.restart(liveReloadServer, 30, TimeUnit.SECONDS);
 		liveReloadServer.restart(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		// verification
 		assertThat(liveReloadServerBehaviour.getLiveReloadServer().getNumberOfConnectedClients()).isEqualTo(0);
 		int newProxyPort = liveReloadServerBehaviour.getProxyServers().get(httpPreviewServer).getConnectors()[0]
 				.getPort();
 		assertThat(proxyPort).isEqualTo(newProxyPort);
 	}
 	
 	@Test
 	public void shouldNotStartIfPortAlreadyInUse() throws CoreException, InterruptedException, ExecutionException, TimeoutException {
 		// pre-condition: create a first server (no need for script injection)
 		final IServer firstServer = createAndLaunchLiveReloadServer("Server 1", false);
 		assertThat(firstServer.getServerState()).isEqualTo(IServer.STATE_STARTED);
 		// operation: create a second server (no need for script injection
 		// either) and attempt to start it on the same port -> should not start
 		final IServer secondServer = createAndLaunchLiveReloadServer("Server 2", false);
 		// verification
 		assertThat(secondServer.getServerState()).isEqualTo(IServer.STATE_STOPPED);
 	}
 
 }
