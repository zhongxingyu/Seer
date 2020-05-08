 package tmc.BetterProtected.svc;
 
import com.avaje.ebean.annotation.Transactional;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import tmc.BetterProtected.BetterProtectedPlugin;
 import tmc.BetterProtected.domain.Player;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
 
 public class PlayerRepositoryTest {
     private BetterProtectedPlugin plugin;
     private PlayerRepository playerRepository;
 
     @Before
     public void setUp() throws Exception {
         plugin = new BetterProtectedPlugin();
         plugin.onEnable();
         playerRepository = new PlayerRepository(plugin.getSessionFactory());
     }
 
     @After
     public void tearDown() throws Exception {
         plugin.onDisable();
     }
 
     @Test
    @Transactional
     public void shouldSaveAndGetPlayer() {
         Player bob = new Player("Bob");
 
         playerRepository.save(bob);
         Long id = bob.getId();
 
         System.out.println(playerRepository.all());
         assertThat(bob.getId(), is(notNullValue()));
         assertThat(playerRepository.findById(id), is(bob));
     }

    @Test
    @Transactional
    public void shouldSaveMultiplePlayersAndGetAList() {
        Player linda = new Player("Linda");
        Player george = new Player("George");
        Player fred = new Player("Fred");
        
        playerRepository.save(linda);
        playerRepository.save(george);
        playerRepository.save(fred);

        assertThat(playerRepository.all(), hasItems(linda, george, fred));
    }
 }
