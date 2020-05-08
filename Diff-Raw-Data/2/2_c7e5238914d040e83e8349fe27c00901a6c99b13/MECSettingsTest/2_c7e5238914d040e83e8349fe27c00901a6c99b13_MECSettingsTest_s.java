 package org.sopeco.webui.client.mec;
 
 import static com.googlecode.gwt.test.assertions.GwtAssertions.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.sopeco.gwt.widgets.ComboBox;
 import org.sopeco.gwt.widgets.WrappedTextBox;
 import org.sopeco.webui.client.resources.R;
 import org.sopeco.webui.shared.helper.MEControllerProtocol;
 import org.sopeco.webui.shared.rpc.GetRPCAsync;
 import org.sopeco.webui.shared.rpc.RPC;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PushButton;
 import com.googlecode.gwt.test.GwtModule;
 import com.googlecode.gwt.test.GwtTestWithEasyMock;
 import com.googlecode.gwt.test.Mock;
 import com.googlecode.gwt.test.utils.GwtReflectionUtils;
 import com.googlecode.gwt.test.utils.events.Browser;
 
 @SuppressWarnings("unchecked")
 @GwtModule("org.sopeco.webui.SoPeCo_UI")
 public class MECSettingsTest extends GwtTestWithEasyMock {
 
 	@Mock
 	private GetRPCAsync service;
 
 	private MECSettings mec;
 
 	private ComboBox cbProtocol;
 	private ComboBox cbController;
 	private WrappedTextBox tbFirst;
 	private WrappedTextBox tbTwo;
 	private FlexTable connectionTable;
 	private FlowPanel flowController;
 	private FlowPanel flowControllerStatus;
 	private Label labelControllerStatus;
 	private PushButton btnRefresh;
 
 	@Before
 	public void setup() {
 		RPC.setGetRPC(service);
 
 		mec = new MECSettings();
 
 		btnRefresh = GwtReflectionUtils.getPrivateFieldValue(mec, "btnRefresh");
 		cbProtocol = GwtReflectionUtils.getPrivateFieldValue(mec, "cbProtocol");
 		cbController = GwtReflectionUtils.getPrivateFieldValue(mec, "cbController");
 		tbFirst = GwtReflectionUtils.getPrivateFieldValue(mec, "tbFirst");
 		tbTwo = GwtReflectionUtils.getPrivateFieldValue(mec, "tbTwo");
 		connectionTable = GwtReflectionUtils.getPrivateFieldValue(mec, "connectionTable");
 		flowController = GwtReflectionUtils.getPrivateFieldValue(mec, "flowController");
 		flowControllerStatus = GwtReflectionUtils.getPrivateFieldValue(mec, "flowControllerStatus");
 		labelControllerStatus = GwtReflectionUtils.getPrivateFieldValue(mec, "labelControllerStatus");
 	}
 
 	@Test
 	public void testWidgetInitialization() {
 		Assert.assertEquals("socket://", cbProtocol.getText());
 
 		Assert.assertEquals(connectionTable, tbFirst.getParent());
 		Assert.assertNull(tbTwo.getParent());
 		assertThat(flowController).isNotVisible();
 		assertThat(flowControllerStatus).isVisible();
 
		Assert.assertEquals("No controller selected.", labelControllerStatus.getText());
 	}
 	
 	@Test
 	public void testEnable() {
 		mec.setEnabled(false);
 		
 		Assert.assertFalse(btnRefresh.isEnabled());
 		Assert.assertFalse(tbFirst.getTextbox().isEnabled());
 		Assert.assertFalse(tbTwo.getTextbox().isEnabled());
 		Assert.assertFalse(cbProtocol.isEnabled());
 		Assert.assertFalse(cbController.isEnabled());
 		
 		mec.setEnabled(true);
 		
 		Assert.assertTrue(btnRefresh.isEnabled());
 		Assert.assertTrue(tbFirst.getTextbox().isEnabled());
 		Assert.assertTrue(tbTwo.getTextbox().isEnabled());
 		Assert.assertTrue(cbProtocol.isEnabled());
 		Assert.assertTrue(cbController.isEnabled());
 	}
 
 	@Test
 	public void testSwapValuesBetweenLayouts() {
 		tbFirst.getTextbox().setText("aaa");
 		
 		cbProtocol.setSelectedIndex(1, true);
 		
 		tbFirst.getTextbox().setText("xxx");
 		tbTwo.getTextbox().setText("yyy");
 		
 		cbProtocol.setSelectedIndex(0, true);
 		
 		tbTwo.getTextbox().setText("bbb");
 		assertThat(tbFirst.getTextbox()).textEquals("aaa");
 		
 		cbProtocol.setSelectedIndex(1, true);
 		
 		assertThat(tbFirst.getTextbox()).textEquals("xxx");
 		assertThat(tbTwo.getTextbox()).textEquals("yyy");
 	}
 	
 	@Test
 	public void testProtocolSelection() {
 		Assert.assertEquals(connectionTable, tbFirst.getParent());
 		Assert.assertNull(tbTwo.getParent());
 
 		cbProtocol.setSelectedIndex(1, true);
 
 		Assert.assertEquals(connectionTable, tbFirst.getParent());
 		Assert.assertEquals(connectionTable, tbTwo.getParent());
 
 		cbProtocol.setSelectedIndex(2, true);
 
 		Assert.assertEquals(connectionTable, tbFirst.getParent());
 		Assert.assertEquals(connectionTable, tbTwo.getParent());
 
 		cbProtocol.setSelectedIndex(0, true);
 
 		Assert.assertEquals(connectionTable, tbFirst.getParent());
 		Assert.assertNull(tbTwo.getParent());
 
 	}
 
 	@Test
 	public void testHTTPEmptyHostPort() {
 		cbProtocol.setSelectedIndex(1, true);
 		
 		Browser.click(btnRefresh);
 
 		Assert.assertTrue(btnRefresh.isEnabled());
 		Assert.assertTrue(cbProtocol.isEnabled());
 		Assert.assertTrue(tbFirst.getTextbox().isEnabled());
 		Assert.assertTrue(tbTwo.getTextbox().isEnabled());
 		assertThat(flowController).isNotVisible();
 		
 		assertThat(labelControllerStatus).textEquals(R.lang.hostMustNotBeEmpty());
 		
 		tbFirst.getTextbox().setText("host");
 		Browser.click(btnRefresh);
 		
 		assertThat(labelControllerStatus).textEquals(R.lang.hostMustNotBeEmpty());
 		
 		tbFirst.getTextbox().setText("");
 		tbTwo.getTextbox().setText("80");
 		Browser.click(btnRefresh);
 		
 		assertThat(labelControllerStatus).textEquals(R.lang.hostMustNotBeEmpty());
 	}
 	
 	@Test
 	public void testHTTPPortInvalid() {
 		cbProtocol.setSelectedIndex(1, true);
 		
 		tbFirst.getTextbox().setText("host");
 		
 		tbTwo.getTextbox().setText("xx80");
 		Browser.click(btnRefresh);
 		assertThat(labelControllerStatus).textEquals(R.lang.enterValidPort());
 		
 		tbTwo.getTextbox().setText("65536");
 		Browser.click(btnRefresh);
 		assertThat(labelControllerStatus).textEquals(R.lang.enterValidPort());
 		
 		tbTwo.getTextbox().setText("80");
 		Browser.click(btnRefresh);
 		assertThat(labelControllerStatus).textEquals(R.lang.requestController());
 	}
 
 	@Test
 	public void testSocketEmptyToken() {
 		Browser.click(btnRefresh);
 
 		Assert.assertTrue(btnRefresh.isEnabled());
 		assertThat(labelControllerStatus).textEquals(R.lang.emptyTokenNotSupported());
 	}
 	
 	@Test
 	public void testHTTPDoesntExist() {
 		service.getControllerFromMEC(EasyMock.anyObject(MEControllerProtocol.class), EasyMock.anyObject(String.class),
 				EasyMock.anyInt(), EasyMock.isA(AsyncCallback.class));
 		expectServiceAndCallbackOnSuccess(null);
 		replay();
 		
 		tbFirst.getTextbox().setText("host");
 		tbTwo.getTextbox().setText("80");
 
 		Browser.click(btnRefresh);
 
 		verify();
 		
 		assertThat(labelControllerStatus).textEquals(R.lang.noMECAppAvailable());
 	}
 	
 	@Test
 	public void testSocketTokenDoesntExist() {
 		service.getControllerFromMEC(EasyMock.anyObject(MEControllerProtocol.class), EasyMock.anyObject(String.class),
 				EasyMock.anyInt(), EasyMock.isA(AsyncCallback.class));
 		expectServiceAndCallbackOnSuccess(null);
 		replay();
 		
 		tbFirst.getTextbox().setText("token");
 
 		Browser.click(btnRefresh);
 
 		verify();
 		
 		Assert.assertTrue(btnRefresh.isEnabled());
 		Assert.assertTrue(cbProtocol.isEnabled());
 		Assert.assertTrue(tbFirst.getTextbox().isEnabled());
 		assertThat(flowController).isNotVisible();
 		assertThat(flowControllerStatus).isVisible();
 		assertThat(labelControllerStatus).textEquals(R.lang.noMECAppAvailable());
 	}
 
 	@Test
 	public void testSocketNoController() {
 		service.getControllerFromMEC(EasyMock.anyObject(MEControllerProtocol.class), EasyMock.anyObject(String.class),
 				EasyMock.anyInt(), EasyMock.isA(AsyncCallback.class));
 		expectServiceAndCallbackOnSuccess(new ArrayList<String>());
 		replay();
 		
 		tbFirst.getTextbox().setText("token");
 
 		Browser.click(btnRefresh);
 
 		verify();
 		
 		Assert.assertTrue(btnRefresh.isEnabled());
 		Assert.assertTrue(cbProtocol.isEnabled());
 		Assert.assertTrue(tbFirst.getTextbox().isEnabled());
 		assertThat(flowController).isNotVisible();
 		assertThat(flowControllerStatus).isVisible();
 		assertThat(labelControllerStatus).textEquals(R.lang.appHasNoRunningController());
 	}
 	
 	@Test
 	public void testSocketControllerReceived() {
 		List<String> controllerList = new ArrayList<String>();
 		controllerList.add("controller_1");
 		controllerList.add("controller_2");
 		
 		service.getControllerFromMEC(EasyMock.anyObject(MEControllerProtocol.class), EasyMock.anyObject(String.class),
 				EasyMock.anyInt(), EasyMock.isA(AsyncCallback.class));
 		expectServiceAndCallbackOnSuccess(controllerList);
 		replay();
 		
 		tbFirst.getTextbox().setText("token");
 
 		Browser.click(btnRefresh);
 
 		verify();
 		
 		Assert.assertTrue(btnRefresh.isEnabled());
 		Assert.assertTrue(cbProtocol.isEnabled());
 		Assert.assertTrue(tbFirst.getTextbox().isEnabled());
 		assertThat(flowController).isVisible();
 		assertThat(flowControllerStatus).isNotVisible();
 		Assert.assertEquals(2, cbController.getElementCount());
 	}
 }
