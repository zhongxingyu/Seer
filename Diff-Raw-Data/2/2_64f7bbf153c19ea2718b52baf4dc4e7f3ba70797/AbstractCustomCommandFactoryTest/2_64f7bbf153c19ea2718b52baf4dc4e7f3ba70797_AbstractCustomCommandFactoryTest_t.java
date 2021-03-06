 package de.bastiankrol.startexplorer.customcommands;
 
 import static de.bastiankrol.startexplorer.customcommands.CommandConfigObjectMother.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 import org.eclipse.ui.services.IServiceLocator;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.Spy;
 import org.powermock.api.mockito.PowerMockito;
 
 import de.bastiankrol.startexplorer.popup.actions.CustomCommandForEditorHandler;
 import de.bastiankrol.startexplorer.preferences.PreferenceModel;
 
 abstract class AbstractCustomCommandFactoryTest
 {
   @Spy
   AbstractCustomCommandFactory customCommandFactory;
 
   @Captor
   ArgumentCaptor<CommandContributionItemParameter> parameterCaptor;
 
   @Mock
   PreferenceModel preferenceModelMock;
 
   @Mock
   IServiceLocator serviceLocatorMock;
 
   @Mock
   ICommandService commandServiceMock;
 
   Command commandMock;
 
   @Mock
   CustomCommandForEditorHandler handlerMock;
 
   @Mock
   CommandContributionItem commandContributionItemMock;
 
   @Before
   public void before() throws Exception
   {
     this.customCommandFactory = createFactory();
     MockitoAnnotations.initMocks(this);
    when(this.preferenceModelMock.customCommandsFromSharedFileHaveBeenAdded())
        .thenReturn(true);
     this.commandMock = PowerMockito.mock(Command.class);
     when(this.commandServiceMock.getCommand(anyString())).thenReturn(
         this.commandMock);
     initFactory(this.customCommandFactory);
   }
 
   abstract AbstractCustomCommandFactory createFactory();
 
   private void initFactory(AbstractCustomCommandFactory customCommandFactory)
   {
     doReturn(this.serviceLocatorMock).when(customCommandFactory)
         .getServiceLocator();
     doReturn(this.commandServiceMock).when(customCommandFactory)
         .getCommandService(this.serviceLocatorMock);
     doReturn(this.handlerMock).when(customCommandFactory)
         .createHandlerForCustomCommand((CommandConfig) anyObject());
     doReturn(this.commandContributionItemMock).when(customCommandFactory)
         .createContributionItem((CommandContributionItemParameter) anyObject());
   }
 
   @Test
   public void testCleanUp() throws Exception
   {
     when(this.preferenceModelMock.getCommandConfigList()).thenReturn(
         oneForBoth());
     this.customCommandFactory.getContributionItems();
     this.customCommandFactory.doCleanup();
     verify(this.commandMock).undefine();
   }
 }
