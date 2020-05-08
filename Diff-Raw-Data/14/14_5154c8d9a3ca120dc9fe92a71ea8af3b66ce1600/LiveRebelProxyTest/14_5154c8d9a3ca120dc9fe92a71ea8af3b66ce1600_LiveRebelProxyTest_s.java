 package org.zeroturnaround.jenkins;
 
 /*****************************************************************
 Copyright 2011 ZeroTurnaround OÜ
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 *****************************************************************/
 
 import com.zeroturnaround.liverebel.api.*;
 import com.zeroturnaround.liverebel.api.diff.DiffResult;
 import com.zeroturnaround.liverebel.util.LiveApplicationUtil;
 import com.zeroturnaround.liverebel.util.LiveRebelXml;
 import hudson.FilePath;
 import hudson.model.BuildListener;
 import hudson.model.Hudson;
 import mockit.Expectations;
 import mockit.Mocked;
 import org.jvnet.hudson.test.HudsonTestCase;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import static org.mockito.Mockito.*;
 
 /**
  * @author Juri Timoshin
  */
 public class LiveRebelProxyTest extends HudsonTestCase {
 
 	@Mocked final LiveApplicationUtil unused = null;
 	@Mock private CommandCenterFactory ccfMock;
 	@Mock private CommandCenter commandCenterMock;
 	@Mock private BuildListener listenerMock;
 	@Mock private DeployPluginProxy deployPluginProxyMock;
 	@Mock private PrintStream printStreamMock;
 
 	private final LiveRebelProxy lrProxy;
 	private final FilePath war = new FilePath( Hudson.MasterComputer.localChannel , "/some/irrelevant/path");
 	private final LiveRebelXml lrXml = new LiveRebelXml("TestApplication", "1.4");
 
 
 	public LiveRebelProxyTest(String name) {
 		super(name);
 		MockitoAnnotations.initMocks(this);
 		when(listenerMock.getLogger()).thenReturn(printStreamMock);
 		lrProxy = new LiveRebelProxy(ccfMock, listenerMock, deployPluginProxyMock);
 
 	}
 
 	public void setUp() throws Exception {
 		super.setUp();
 	}
 
 	public void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testPerformSuccess() throws Exception {
 		//TODO: Test goes here...
 	}
 
 
 	public void testInitCommandCenterSuccess() throws Exception {
 		when(ccfMock.newCommandCenter()).thenReturn(commandCenterMock);
 		assertTrue(lrProxy.initCommandCenter());
 		verify(ccfMock).newCommandCenter();
 	}
 
 	public void testInitCommandCenterForbidden() throws Exception {
 		when(ccfMock.newCommandCenter()).thenThrow(new Forbidden("irrelevant", 111, "irrelevant"));
 		assertFalse(lrProxy.initCommandCenter());
 
 		verify(ccfMock).newCommandCenter();
 		verify(printStreamMock).println("ERROR! Access denied. Please, navigate to Jenkins Configuration to specify LiveRebel Authentication Token.");
 	}
 
 	public void testInitCommandCenterConnectException() throws Exception {
 		when(ccfMock.newCommandCenter()).thenThrow(new ConnectException("urlValue", "Some Reason"));
 		assertFalse(lrProxy.initCommandCenter());
 
 		verify(ccfMock).newCommandCenter();
 
 		verify(printStreamMock).println("ERROR! Unable to connect to server.");
 		verify(printStreamMock).println("URL: urlValue");
 		verify(printStreamMock).println("Reason: Some Reason");
 	}
 
 	public void testInitCommandCenterConnectExceptionEmptyUrl() throws Exception {
 		when(ccfMock.newCommandCenter()).thenThrow(new ConnectException("https://", "Some Reason"));
 		assertFalse(lrProxy.initCommandCenter());
 
 		verify(ccfMock).newCommandCenter();
 
 		verify(printStreamMock).println("ERROR! Unable to connect to server.");
 		verify(printStreamMock).println("URL: https://");
 		verify(printStreamMock).println("Please, navigate to Jenkins Configuration to specify running LiveRebel Url.");
 	}
 
 	public void testIsFirstRelease() throws Exception {
 		assertTrue(lrProxy.isFirstRelease(null));
 		assertFalse(lrProxy.isFirstRelease(mock(ApplicationInfo.class)));
 	}
 
 	public void testUpdateSuccess() throws Exception {
 		ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
 		when(applicationInfoMock.getActiveVersionPerServer()).thenReturn(
 			new HashMap<String, String>() {
 				{ put("firstKey", "firstValue"); put("secondKey", "secondValue");}
 			}
 		);
 
 		LiveRebelProxy lrProxySpy = spy(lrProxy);
 		doReturn(true).when(lrProxySpy).updateOnServer(Matchers.<LiveRebelXml>any(), Matchers.<String>any(),
 			Matchers.<String>any(), Matchers.<FilePath>any());
 
 		lrProxySpy.update(lrXml, applicationInfoMock, war);
 
 		verify(printStreamMock).println("Starting updating application on servers:");
 		verify(lrProxySpy).updateOnServer(lrXml, "firstKey", "firstValue", war);
 		verify(lrProxySpy).updateOnServer(lrXml, "secondKey", "secondValue", war);
 	}
 
 	public void testUpdateWhenFirstReleaseSuccess() throws Exception {
 		lrProxy.commandCenter = commandCenterMock;
		doReturn(new HashMap(){
 			{ put("server1", "serverInfo1"); put("server2", "serverInfo2");}
 		}).when(commandCenterMock).getServers();
 
 		LiveRebelProxy lrProxySpy = spy(lrProxy);
 		doReturn(true).when(lrProxySpy).updateOnServer(Matchers.<LiveRebelXml>any(), Matchers.<String>any(),
 			Matchers.<String>any(), Matchers.<FilePath>any());
 
 		lrProxySpy.update(lrXml, null, war);
 
 		verify(printStreamMock).println("Starting updating application on servers:");
 		verify(lrProxySpy).updateOnServer(lrXml, "server1", "", war);
 		verify(lrProxySpy).updateOnServer(lrXml, "server2", "", war);
 	}
 
 	public void testUpdateOnServerInitialDploy() throws Exception {
 		LiveRebelProxy lrProxySpy = spy(lrProxy);
 		doReturn(true).when(lrProxySpy).cargoDeploy(war);
 
 		lrProxySpy.updateOnServer(lrXml, "server1", "", war);
 
 		verify(printStreamMock).printf("There is no such application on server %s.\n", "server1");
 		verify(lrProxySpy).cargoDeploy(war);
 	}
 
 	public void testUpdateOnServerAlreadyActive() throws Exception {
 		LiveRebelProxy lrProxySpy = spy(lrProxy);
 		doReturn(true).when(lrProxySpy).cargoDeploy(war);
 
 		assertTrue(lrProxySpy.updateOnServer(lrXml, "server1", "1.4", war));
 		verify(printStreamMock).println("Current version is already running on server. No need to update.");
 		verify(lrProxySpy, never()).cargoDeploy(war);
 	}
 
 	public void testCargoDeploy() throws Exception {
 		doReturn(true).when(deployPluginProxyMock).cargoDeploy(war);
 		lrProxy.useCargoIfIncompatible = true;
 
 		lrProxy.cargoDeploy(war);
 		verify(deployPluginProxyMock).cargoDeploy(war);
 	}
 
 	public void testCargoDeployDoNothing() throws Exception {
 		lrProxy.useCargoIfIncompatible = false;
 		lrProxy.cargoDeploy(war);
 		verify(deployPluginProxyMock, never()).cargoDeploy(war);
 		verify(printStreamMock).println("Fallback to cargo deploy is disabled. Doing nothing.");
 	}
 
 	public void testGetDifferences() throws Exception {
 		lrProxy.commandCenter = commandCenterMock;
 		DiffResult diffResultMock = mock(DiffResult.class);
 		doReturn(diffResultMock).when(commandCenterMock).compare(lrXml.getApplicationId(), "1.3", lrXml.getVersionId(), false);
 
 		assertEquals(diffResultMock, lrProxy.getDifferences(lrXml, "1.3"));
 		verify(diffResultMock).print(printStreamMock);
 	}
 
 	public void testUploadIfNeededAlreadyUploaded() throws Exception {
 		ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
 		doReturn(new HashSet<String>(){{ add("1.3"); add("1.4");}}).when(applicationInfoMock).getVersions();
 
 		lrProxy.uploadIfNeeded(applicationInfoMock, "1.4", war);
 		verify(printStreamMock).println("Current version of application is already uploaded. Skipping upload.");
 	}
 
 	public void testUploadIfNeededUpload() throws Exception {
 		ApplicationInfo applicationInfoMock = mock(ApplicationInfo.class);
 		doReturn(new HashSet<String>(){{ add("1.3");}}).when(applicationInfoMock).getVersions();
 
 		LiveRebelProxy lrProxySpy = spy(lrProxy);
 		doReturn(true).when(lrProxySpy).uploadArtifact(Matchers.<File>any());
 
 		lrProxySpy.uploadIfNeeded(applicationInfoMock, "1.4", war);
 		verify(lrProxySpy).uploadArtifact(new File(war.getRemote()));
 	}
 
 	public void testUploadIfNeededApplicationInfoNull() throws Exception {
 		lrProxy.uploadIfNeeded(null, "1.4", war);
 	}
 
 	public void testUploadArtifact() throws Exception {
 		UploadInfo uploadInfoMock = mock(UploadInfo.class);
 		when(uploadInfoMock.getApplicationId()).thenReturn("TestApplication");
 		when(uploadInfoMock.getVersionId()).thenReturn("1.4");
 		when(commandCenterMock.upload(new File(war.getRemote()))).thenReturn(uploadInfoMock);
 
 		lrProxy.commandCenter = commandCenterMock;
 		assertTrue(lrProxy.uploadArtifact(new File(war.getRemote())));
 		verify(commandCenterMock).upload(new File(war.getRemote()));
 		verify(printStreamMock).printf("SUCCESS: %s %s was uploaded.\n", "TestApplication", "1.4");
 	}
 
 	public void testGetLiveRebelXml() throws Exception {
 		new Expectations(){
 			{
 				LiveApplicationUtil.findLiveRebelXml(new File(war.getRemote())); result = lrXml;
 			}
 		};
 
 		LiveRebelXml result = lrProxy.getLiveRebelXml(war);
 		assertEquals(lrXml.getVersionId(), result.getVersionId());
 		assertEquals(lrXml.getApplicationId(), result.getApplicationId());
 	}
 }
