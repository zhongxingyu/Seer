 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.mockito.Mockito.spy;
 
 import org.junit.Before;
 import org.zend.sdk.test.AbstractTest;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ICommand;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdklib.internal.repository.UserBasedRepositoryLoader;
 import org.zend.sdklib.manager.RepositoryManager;
 import org.zend.sdklib.repository.IRepositoryLoader;
 
 public class AbstractRepositoryCommandTest extends AbstractTest {
 
 	private IRepositoryLoader loader;
 	protected RepositoryManager manager;
 
 	@Before
 	public void startup() {
 		loader = new UserBasedRepositoryLoader(file);
 		manager = spy(new RepositoryManager(loader));
 	}
 	
 	public ICommand getCommand(CommandLine cmdLine) {
 		ICommand command = spy(CommandFactory
 				.createCommand(cmdLine));
 		return command;
 	}
 
 }
