 package org.zend.sdk.test.sdklib;
 
 import static org.junit.Assert.assertNotNull;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.zend.sdk.test.AbstractWebApiTest;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.CodeTrace;
 import org.zend.webapi.core.connection.data.CodeTracingList;
 import org.zend.webapi.core.connection.data.CodeTracingStatus;
 import org.zend.webapi.core.connection.data.IResponseData;
 
 public class TestZendCodeTracing extends AbstractWebApiTest {
 
 	@Test
 	public void testEnable() throws WebApiException, IOException {
 		when(client.codeTracingEnable(anyBoolean())).thenReturn(
 				(CodeTracingStatus) getResponseData("codetracingEnable",
 						IResponseData.ResponseType.CODE_TRACING_STATUS));
 		assertNotNull(codeTracing.enable());
 	}
 
 	@Test
 	public void testEnableWithRestart() throws WebApiException, IOException {
 		when(client.codeTracingEnable(anyBoolean())).thenReturn(
 				(CodeTracingStatus) getResponseData("codetracingEnable",
 						IResponseData.ResponseType.CODE_TRACING_STATUS));
 		assertNotNull(codeTracing.enable(true));
 	}
 
 	@Test
 	public void testDisable() throws WebApiException, IOException {
 		when(client.codeTracingDisable(anyBoolean())).thenReturn(
 				(CodeTracingStatus) getResponseData("codetracingDisable",
 						IResponseData.ResponseType.CODE_TRACING_STATUS));
 		assertNotNull(codeTracing.disable());
 	}
 
 	@Test
 	public void testDisableWithRestart() throws WebApiException, IOException {
 		when(client.codeTracingDisable(anyBoolean())).thenReturn(
 				(CodeTracingStatus) getResponseData("codetracingDisable",
 						IResponseData.ResponseType.CODE_TRACING_STATUS));
 		assertNotNull(codeTracing.disable(true));
 	}
 
 	@Test
 	public void testIsEnabled() throws WebApiException, IOException {
 		when(client.codeTracingIsEnabled()).thenReturn(
 				(CodeTracingStatus) getResponseData("codetracingIsEnabled",
 						IResponseData.ResponseType.CODE_TRACING_STATUS));
 		assertNotNull(codeTracing.isEnabled());
 	}
 
 	@Test
 	public void testCreateTrace() throws WebApiException, IOException {
 		when(client.codeTracingCreate(anyString())).thenReturn(
 				(CodeTrace) getResponseData("codetracingCreate",
 						IResponseData.ResponseType.CODE_TRACE));
		assertNotNull(codeTracing.createTrace(new URL("http://test")));
 	}
 
 	@Test
 	public void testDeleteTrace() throws WebApiException, IOException {
 		when(client.codeTracingDelete(anyString())).thenReturn(
 				(CodeTrace) getResponseData("codetracingDelete",
 						IResponseData.ResponseType.CODE_TRACE));
 		assertNotNull(codeTracing.deleteTrace("id"));
 	}
 
 	@Test
 	public void testGetTraces() throws WebApiException, IOException {
 		when(client.codeTracingList(Mockito.any(Integer.class), Mockito.any(Integer.class), anyString(), anyString(), anyString())).thenReturn(
 				(CodeTracingList) getResponseData("codetracingList",
 						IResponseData.ResponseType.CODE_TRACING_LIST));
 		assertNotNull(codeTracing.getTraces(false, "aaa"));
 	}
 }
