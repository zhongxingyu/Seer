 package mx.ecosur.multigame.session;
 
 import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
 import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
 import mx.ecosur.multigame.enums.MoveStatus;
 import mx.ecosur.multigame.exception.InvalidMoveException;
 import mx.ecosur.multigame.grid.Color;
 import mx.ecosur.multigame.grid.entity.GridPlayer;
 import mx.ecosur.multigame.grid.entity.GridRegistrant;
import mx.ecosur.multigame.impl.entity.manantiales.*;
import mx.ecosur.multigame.impl.enums.manantiales.AgentType;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;
import mx.ecosur.multigame.impl.enums.manantiales.TokenType;
 import mx.ecosur.multigame.model.interfaces.Registrant;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import java.util.Hashtable;
 
 import static org.junit.Assert.*;
 
 /**
  * @author awaterma@ecosur.mx
  */
 public class ManantialesAgencyTest {
 
     private RegistrarRemote registrar;
 
     private SharedBoardRemote board;
 
     private int gameId;
 
     private ManantialesPlayer alice;
 
     private SimpleAgent[] agents;
 
     @Before
     public void fixtures () throws Exception {
 
         Hashtable ht = new Hashtable();
         ht.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
         ht.put(Context.PROVIDER_URL, "jnp://localhost:1099");
         ht.put(Context.SECURITY_PRINCIPAL, "alice");
         ht.put(Context.SECURITY_CREDENTIALS, "test");
         InitialContext ic = new InitialContext(ht);
 
         agents = new SimpleAgent [3];
         Object obj = ic.lookup("mx.ecosur.multigame.ejb.interfaces.RegistrarRemote");
         registrar = (RegistrarRemote) obj;
 
         ManantialesGame game = new ManantialesGame ();
         game.setMode(Mode.BASIC_PUZZLE);
         Registrant registrant = registrar.register(new GridRegistrant("alice"));
         game = (ManantialesGame) registrar.registerPlayer(game, registrant);
 
         if (gameId == 0) {
             gameId = game.getId();
         }
 
         Color[] colors = { Color.PURPLE, Color.RED, Color.BLACK};
         for (int i = 0; i < 3; i++) {
             registrar.registerAgent(game, new SimpleAgent(new GridRegistrant ("Agent-" + (i + 1)),
                     colors[i], AgentType.SIMPLE));
         }
 
         /* Get the SharedBoard */
         board = (SharedBoardRemote) ic.lookup( "mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
         game = (ManantialesGame) board.getGame(gameId);
 
         /* Set the GamePlayers from the SharedBoard */
         for (GridPlayer p : game.getPlayers())
             if (p.getName().equals("alice")) {
                 alice = (ManantialesPlayer) p;
                 break;
             }
 
         assertNotNull(alice);
         assertEquals (4, game.getPlayers().size());
     }
 
     @After
     public void cleanUp () {
         registrar = null;
         board = null;
     }
 
     @Test
        public void testBasicAgentMoves () throws InvalidMoveException, InterruptedException {
            alice.setTurn (true);
 
            ManantialesFicha play = new ManantialesFicha(2,2, alice.getColor(),
                            TokenType.MODERATE_PASTURE);
 
            ManantialesMove move = new ManantialesMove (alice, play);
            ManantialesGame game = (ManantialesGame) board.getGame(gameId);
            move.setMode(game.getMode());
            move = (ManantialesMove) board.doMove(game, move);
            assertEquals (MoveStatus.EVALUATED, move.getStatus());
            game = (ManantialesGame) board.getGame(gameId);
            assertEquals (play, game.getGrid().getLocation(play));
            Thread.sleep(500);
            game = (ManantialesGame) board.getGame(gameId);
            assertEquals (4, game.getMoves().size());
        }
 }
