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
 
 import java.io.File;
 import java.io.IOException;
import java.util.Map;
 
 import org.junit.Test;
 import org.zend.sdk.test.AbstractWebApiTest;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.UpdateApplicationCommand;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 public class TestUpdateApplicationCommand extends AbstractWebApiTest {
 
 	private String[] validCommand = new String[] { "update", "application",
			"-p", FOLDER + "test-1.0.0.zpk", "-t", "0", "-id", "0" };
 
 	@Test
 	public void testExecute() throws WebApiException, IOException, ParseError {
 		CommandLine cmdLine = new CommandLine(validCommand);
 		UpdateApplicationCommand command = getCommand(cmdLine);
 		assertNotNull(command);
 		doReturn(application).when(command).getApplication();
 		when(client.applicationUpdate(anyInt(), any(File.class), anyBoolean(), anyMap())).thenReturn(
 			(ApplicationInfo) getResponseData("applicationUpdate",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertTrue(command.execute(cmdLine));
 	}
 
 	@Test
 	public void testExecuteTargetDisconnected() throws ParseError,
 			WebApiException, IOException {
 		CommandLine cmdLine = new CommandLine(validCommand);
 		UpdateApplicationCommand command = getCommand(cmdLine);
 		assertNotNull(command);
 		doReturn(application).when(command).getApplication();
 		when(client.applicationGetStatus()).thenThrow(
 				new SignatureException("testError"));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	private UpdateApplicationCommand getCommand(CommandLine cmdLine)
 			throws ParseError {
 		UpdateApplicationCommand command = spy((UpdateApplicationCommand) CommandFactory
 				.createCommand(cmdLine));
 		return command;
 	}
 
 }
