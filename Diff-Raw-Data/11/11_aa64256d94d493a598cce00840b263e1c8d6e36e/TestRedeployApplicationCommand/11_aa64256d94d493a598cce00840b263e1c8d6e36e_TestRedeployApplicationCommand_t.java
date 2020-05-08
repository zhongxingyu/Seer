 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyVararg;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.zend.sdk.test.AbstractWebApiTest;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.RedeployApplicationCommand;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 public class TestRedeployApplicationCommand extends AbstractWebApiTest {
 
 	private String[] validCommand = new String[] { "redeploy", "application",
			"-t", "0", "-a", "0" };
 
 	@Test
 	public void testExecute() throws WebApiException, IOException, ParseError {
 		CommandLine cmdLine = new CommandLine(validCommand);
 		RedeployApplicationCommand command = getCommand(cmdLine);
 		assertNotNull(command);
 		doReturn(application).when(command).getApplication();
 		when(
 				client.applicationRedeploy(anyInt(), anyBoolean(), (String[])anyVararg())).thenReturn(
 				(ApplicationInfo) getResponseData("applicationRedeploy",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertTrue(command.execute(cmdLine));
 	}
 
 	@Test
 	public void testExecuteTargetDisconnected() throws ParseError,
 			WebApiException, IOException {
 		CommandLine cmdLine = new CommandLine(validCommand);
 		RedeployApplicationCommand command = getCommand(cmdLine);
 		assertNotNull(command);
 		doReturn(application).when(command).getApplication();
 		when(client.applicationGetStatus()).thenThrow(
 				new SignatureException("testError"));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	private RedeployApplicationCommand getCommand(CommandLine cmdLine)
 			throws ParseError {
 		RedeployApplicationCommand command = spy((RedeployApplicationCommand) CommandFactory
 				.createCommand(cmdLine));
 		return command;
 	}
 
 }
