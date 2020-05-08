 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.junit.Test;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ICommand;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.DeployApplicationCommand;
 import org.zend.sdklib.manager.TargetException;
 import org.zend.sdklib.target.LicenseExpiredException;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.ApplicationInfo;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.core.connection.request.NamedInputStream;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 public class TestDeployApplicationCommand extends AbstractWebapiCommandTest {
 
 	@Test
 	public void testExecute() throws WebApiException, IOException, ParseError, TargetException, LicenseExpiredException {
 		CommandLine cmdLine = getLine("deploy application -p" + FOLDER
 				+ "test-1.0.0.zpk -b http://myhost.com/aaa -t 0 -n myApp");
 		ICommand command = getCommand(cmdLine);
 		manager.add(getTarget());
 		when(
 				client.applicationDeploy(any(NamedInputStream.class),
 						anyString(), anyBoolean(), any(Map.class), anyString(),
 						anyBoolean(), anyBoolean())).thenReturn(
 				(ApplicationInfo) getResponseData("applicationDeploy",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertTrue(command.execute(cmdLine));
 	}
 	
 	@Test
 	public void testExecuteNoTarget() throws WebApiException, IOException, ParseError, TargetException, LicenseExpiredException {
 		CommandLine cmdLine = getLine("deploy application -p" + FOLDER
 				+ "test-1.0.0.zpk -b http://myhost.com/aaa -t 0 -n myApp");
 		ICommand command = getCommand(cmdLine);
 		when(
 				client.applicationDeploy(any(NamedInputStream.class),
 						anyString(), anyBoolean(), any(Map.class), anyString(),
 						anyBoolean(), anyBoolean())).thenReturn(
 				(ApplicationInfo) getResponseData("applicationDeploy",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	@Test
 	public void testExecuteWithParams() throws WebApiException, IOException,
 			ParseError, TargetException, LicenseExpiredException {
 		CommandLine cmdLine = getLine("deploy application -p"
 				+ FOLDER
 				+ "test-1.0.0.zpk -b http://myhost.com/aaa -t 0 -n myApp -m key1=value1,key2=value2");
 		ICommand command = getCommand(cmdLine);
 		manager.add(getTarget());
 		when(
 				client.applicationDeploy(any(NamedInputStream.class),
 						anyString(), anyBoolean(), any(Map.class), anyString(),
 						anyBoolean(), anyBoolean())).thenReturn(
 				(ApplicationInfo) getResponseData("applicationDeploy",
 						IResponseData.ResponseType.APPLICATION_INFO));
 		assertTrue(command.execute(cmdLine));
 	}
 
 	@Test
 	public void testExecuteTargetDisconnected() throws ParseError,
 			WebApiException, IOException {
 		CommandLine cmdLine = getLine("deploy application -p" + FOLDER
 				+ "test-1.0.0.zpk -b http://myhost.com/aaa -t 0 -n myApp");
 		ICommand command = getCommand(cmdLine);
 		when(client.applicationGetStatus()).thenThrow(
 				new SignatureException("testError"));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	private DeployApplicationCommand getCommand(CommandLine cmdLine)
 			throws ParseError {
 		DeployApplicationCommand command = spy((DeployApplicationCommand) CommandFactory
 				.createCommand(cmdLine));
 		assertNotNull(command);
 		when(command.getTargetManager()).thenReturn(manager);
 		doReturn(application).when(command).getApplication();
 		return command;
 	}
 
 }
