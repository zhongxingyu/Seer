 package pl.edu.agh.two.mud.server.command.factory;
 
 import static org.fest.assertions.Assertions.*;
 import static org.mockito.Mockito.*;
 
 import java.util.*;
 
 import org.junit.*;
 import org.junit.runner.*;
 import org.mockito.*;
 import org.mockito.runners.*;
import org.springframework.context.*;
 
 import pl.edu.agh.two.mud.common.command.*;
 import pl.edu.agh.two.mud.server.command.*;
 import pl.edu.agh.two.mud.server.command.provider.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ReflexiveCommandFactoryTest {
 
 	@Mock
	private CommandProvider commandClassRegistry;
	@Mock
	private ApplicationContext applicationContext;
 
 	@InjectMocks
 	private ReflexiveCommandFactory reflexiveCommandFactory = new ReflexiveCommandFactory();
 
 	@Test
 	public void shouldCreateCommandAndInjectFieldValues() throws Exception {
 		// GIVEN
 		IParsedCommand parsedCommand = mock(IParsedCommand.class);
 		String commandId = "ZUZIA";
 		when(parsedCommand.getCommandId()).thenReturn(commandId);
 
 		Map<String, String> parametersValuesByFieldNames = new HashMap<String, String>();
 		parametersValuesByFieldNames.put("stringParam", "string");
 		parametersValuesByFieldNames.put("intParam", "54");
 		parametersValuesByFieldNames.put("IntegerParam", "65");
 		parametersValuesByFieldNames.put("textParam", "text");
 		parametersValuesByFieldNames.put("doubleParam", "15.6");
 		parametersValuesByFieldNames.put("DoubleParam", "17.3");
 		when(parsedCommand.getValuesMap()).thenReturn(parametersValuesByFieldNames);
		doReturn(Command.class).when(commandClassRegistry).getCommandById(commandId);
		when(applicationContext.getBean(Command.class)).thenReturn(new SampleCommand());
 
 		// WHEN
 		Command command = reflexiveCommandFactory.create(parsedCommand);
 
 		// THEN
 		assertThat(command).isInstanceOf(SampleCommand.class);
 		SampleCommand testCommand = (SampleCommand) command;
 		assertThat(testCommand.getStringParam()).isEqualTo("string");
 		assertThat(testCommand.getIntParam()).isEqualTo(54);
 		assertThat(testCommand.getTextParam().getText()).isEqualTo("text");
 		assertThat(testCommand.getIntegerParam()).isEqualTo(65);
 		assertThat(testCommand.getdoubleParam()).isEqualTo(15.6);
 		assertThat(testCommand.getDoubleParam()).isEqualTo(17.3);
 
 	}
 
 }
