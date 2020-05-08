 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyMap;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 
 import org.junit.Test;
import org.zend.sdk.test.AbstractWebApiTest;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.UpdateApplicationCommand;
 import org.zend.sdklib.manager.TargetException;
 import org.zend.sdklib.target.LicenseExpiredException;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.core.connection.request.NamedInputStream;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 public class TestUpdateApplicationCommand extends AbstractWebapiCommandTest {
 
 	@Test
 	public void testExecute() throws WebApiException, IOException, ParseError,
 			TargetException, LicenseExpiredException {
 		CommandLine cmdLine = getLine("update application -p " + FOLDER
 				+ "test-1.0.0.zpk -t 0 -a 0");
 		UpdateApplicationCommand command = getCommand(cmdLine);
 		manager.add(getTarget());
 		when(client.applicationUpdate(anyInt(), any(NamedInputStream.class), anyBoolean(), anyMap())).thenReturn(
 			(ApplicationInfo) getResponseData("applicationUpdate",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertTrue(command.execute(cmdLine));
 	}
 	
 	@Test
 	public void testExecuteNoTarget() throws WebApiException, IOException, ParseError,
 			TargetException, LicenseExpiredException {
 		CommandLine cmdLine = getLine("update application -p " + FOLDER
 				+ "test-1.0.0.zpk -t 0 -a 0");
 		UpdateApplicationCommand command = getCommand(cmdLine);
 		when(client.applicationUpdate(anyInt(), any(NamedInputStream.class), anyBoolean(), anyMap())).thenReturn(
 			(ApplicationInfo) getResponseData("applicationUpdate",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	@Test
 	public void testExecuteTargetDisconnected() throws ParseError,
 			WebApiException, IOException {
 		CommandLine cmdLine = getLine("update application -p " + FOLDER
 				+ "test-1.0.0.zpk -t 0 -a 0");
 		UpdateApplicationCommand command = getCommand(cmdLine);
 		when(client.applicationGetStatus()).thenThrow(
 				new SignatureException("testError"));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	private UpdateApplicationCommand getCommand(CommandLine cmdLine)
 			throws ParseError {
 		UpdateApplicationCommand command = spy((UpdateApplicationCommand) CommandFactory
 				.createCommand(cmdLine));
 		assertNotNull(command);
 		when(command.getTargetManager()).thenReturn(manager);
 		doReturn(application).when(command).getApplication();
 		return command;
 	}
 
 }
