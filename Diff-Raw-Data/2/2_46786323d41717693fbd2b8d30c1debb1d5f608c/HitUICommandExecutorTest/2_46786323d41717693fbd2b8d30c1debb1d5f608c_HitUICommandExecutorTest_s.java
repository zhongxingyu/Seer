 package pl.edu.agh.two.mud.server.command.executor;
 
 
 import static org.fest.assertions.Assertions.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.*;
 import org.junit.runner.*;
 import org.mockito.*;
 import org.mockito.runners.*;
 
 import pl.edu.agh.two.mud.common.*;
 import pl.edu.agh.two.mud.server.*;
 import pl.edu.agh.two.mud.server.world.fight.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class HitUICommandExecutorTest {
 
 	@Mock
 	private ServiceRegistry registry;
 	@Mock
 	private Fight fight;
 
 	@InjectMocks
 	private HitUICommandExecutor executor = new HitUICommandExecutor();
 
 	@Test
 	public void shouldExecuteHitCommand() throws Exception {
 		// GIVEN
 		Service service = new Service();
 		Player player = new Player();
 		when(registry.getCurrentService()).thenReturn(service);
 		when(registry.getPlayer(service)).thenReturn(player);
 
 		// WHEN
 		executor.execute(null);
 
 		// THEN
		verify(fight, timeout(1)).hit(player);
 
 	}
 	
 }
