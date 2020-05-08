 /*
 * Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
 *
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 /**
 * @author awaterma@ecosur.mx
 */
 package mx.ecosur.multigame.session;
 
 import static org.junit.Assert.*;
 
 import java.rmi.RemoteException;
 
 import java.util.Hashtable;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import mx.ecosur.multigame.ejb.interfaces.RegistrarRemote;
 import mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote;
 
 
 import mx.ecosur.multigame.enums.MoveStatus;
 import mx.ecosur.multigame.enums.SuggestionStatus;
 import mx.ecosur.multigame.exception.InvalidMoveException;
 import mx.ecosur.multigame.exception.InvalidRegistrationException;
 import mx.ecosur.multigame.exception.InvalidSuggestionException;
 import mx.ecosur.multigame.grid.entity.*;
import mx.ecosur.multigame.manantiales.entity.*;
import mx.ecosur.multigame.manantiales.enums.Mode;
import mx.ecosur.multigame.manantiales.enums.TokenType;
 import mx.ecosur.multigame.model.interfaces.Game;
 import mx.ecosur.multigame.model.interfaces.Move;
 import mx.ecosur.multigame.model.interfaces.Registrant;
 import mx.ecosur.multigame.model.interfaces.Suggestion;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ManantialesSharedBoardTest {
 
 
     private ThreadLocal<RegistrarRemote> registrar = new ThreadLocal<RegistrarRemote>();
 
     private SharedBoardRemote board;
 
     private int gameId;
 
     private ManantialesPlayer alice, bob, charlie, denise;
 
 
     @Before
     public void fixtures () throws Exception {
         gameId = 0;
         Hashtable ht = new Hashtable();
         ht.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
         ht.put(Context.PROVIDER_URL, "jnp://localhost:1099");
         ht.put(Context.SECURITY_PRINCIPAL, "alice");
         ht.put(Context.SECURITY_CREDENTIALS, "test");
         InitialContext ic = new InitialContext(ht);
 
         registrar.set((RegistrarRemote) ic.lookup(
                 "mx.ecosur.multigame.ejb.interfaces.RegistrarRemote"));
 
         GridRegistrant[] registrants = {
             new GridRegistrant ("alice"),
             new GridRegistrant ("bob"),
             new GridRegistrant ("charlie"),
             new GridRegistrant ("denise")};
 
         ManantialesGame game = new ManantialesGame ();
         game.setMode(Mode.COMPETITIVE);
 
         for (int i = 0; i < 4; i++) {
             Registrant registrant = registrar.get().register(registrants[i]);
             game = (ManantialesGame) registrar.get().registerPlayer(game, registrant);
             if (gameId == 0) {
                 gameId = game.getId();
             }
         }
 
         /* Get the SharedBoard */
         board = (SharedBoardRemote) ic.lookup("mx.ecosur.multigame.ejb.interfaces.SharedBoardRemote");
         game = (ManantialesGame) board.getGame(gameId);
 
         /* Set the GamePlayers from the SharedBoard */
         Set<GridPlayer> players = game.getPlayers();
         for (GridPlayer p : players) {
             if (p.getName().equals("alice"))
                 alice = (ManantialesPlayer) p;
             else if (p.getName().equals("bob"))
                 bob = (ManantialesPlayer) p;
             else if (p.getName().equals("charlie"))
                 charlie = (ManantialesPlayer) p;
             else if (p.getName().equals("denise"))
                 denise = (ManantialesPlayer) p;
         }
 
         assertNotNull ("Alice not found in game!", alice);
         assertNotNull ("Bob not found in game!", bob);
         assertNotNull ("Charlie not found in game!", charlie);
         assertNotNull ("Denise not found in game!", denise);
     }
 
     @After
     public void tearDown () throws NamingException, RemoteException, InvalidRegistrationException {
         ManantialesGame game = (ManantialesGame) board.getGame(gameId);
         registrar.get().unregister(game, alice);
         registrar.get().unregister(game, bob);
         registrar.get().unregister(game, charlie);
         registrar.get().unregister(game, denise);
     }
 
 
     /**
      * Simple test to determine if there are the correct number of squares
      * after the game state is set to BEGIN.
      * @throws RemoteException
      */
     @Test
     public void testGetGameGrid() throws RemoteException {
         ManantialesGame game = (ManantialesGame) board.getGame(gameId);
         assertTrue (game.getGrid().getCells().size() == 0);
     }
 
     /** Test on ManantialesGame for setting check constraints
      *
      * Needs a rewrite. Removed from test cycle pending rewrite.
      *
      * @throws InvalidMoveException */
     @Test
     public void testManantialCheckConstraints () throws InvalidMoveException {
         ManantialesGame game = (ManantialesGame) board.getGame(gameId);
         ManantialesFicha ficha = new ManantialesFicha(4,3, alice.getColor(),
                 TokenType.MODERATE_PASTURE);
         ManantialesMove move = new ManantialesMove (alice, ficha);
         move.setMode(game.getMode());
         Move mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(4,5, bob.getColor(),
                 TokenType.MODERATE_PASTURE);
         bob.setTurn(true);
         move = new ManantialesMove (bob, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(3,4, charlie.getColor(),
                 TokenType.MODERATE_PASTURE);
         charlie.setTurn(true);
         move = new ManantialesMove (charlie, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         game = (ManantialesGame) board.getGame(gameId);
         assertEquals (3, game.getMoves().size());
         assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
         assertEquals(1, game.getCheckConditions().size());
     }
 
    @Test
     public void testWestCheckConstraints () throws InvalidMoveException {
        ManantialesGame game = (ManantialesGame) board.getGame(gameId);
        ManantialesFicha ficha = new ManantialesFicha(2,4, alice.getColor(),
                 TokenType.MODERATE_PASTURE);
        ManantialesMove move = new ManantialesMove (alice, ficha);
        Move mv = board.doMove(game, move);
        assertEquals(MoveStatus.EVALUATED, mv.getStatus());
        ficha = new ManantialesFicha(1,4, bob.getColor(),
                 TokenType.MODERATE_PASTURE);
        bob.setTurn(true);
        move = new ManantialesMove (bob, ficha);
        game = (ManantialesGame) board.getGame(gameId);
        mv = board.doMove(game, move);
        assertEquals(MoveStatus.EVALUATED, mv.getStatus());
        ficha = new ManantialesFicha(0,4, charlie.getColor(),
                 TokenType.MODERATE_PASTURE);
        charlie.setTurn(true);
        move = new ManantialesMove (charlie, ficha);
        game = (ManantialesGame) board.getGame(gameId);
        mv = board.doMove(game, move);
        assertEquals(MoveStatus.EVALUATED, mv.getStatus());
        game = (ManantialesGame) board.getGame(gameId);
        assertEquals (3, game.getMoves().size());
        assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
        assertEquals(1, game.getCheckConditions().size());
     }
 
     @Test
     public void testNorthCheckConstraints () throws InvalidMoveException {
         ManantialesGame game = (ManantialesGame) board.getGame(gameId);
         ManantialesFicha ficha = new ManantialesFicha(4,0, alice.getColor(),
                 TokenType.MODERATE_PASTURE);
         ManantialesMove move = new ManantialesMove (alice, ficha);
         Move mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(4,1, bob.getColor(),
                 TokenType.MODERATE_PASTURE);
         bob.setTurn(true);
         move = new ManantialesMove (bob, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(4,2, charlie.getColor(),
                 TokenType.MODERATE_PASTURE);
         charlie.setTurn(true);
         move = new ManantialesMove (charlie, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         game = (ManantialesGame) board.getGame(gameId);
         assertEquals (3, game.getMoves().size());
         assertTrue("CheckConstraint not fired!", game.getCheckConditions() != null);
         assertEquals(1, game.getCheckConditions().size());
     }
 
     @Test
     public void testEastCheckConstraints () throws InvalidMoveException {
         ManantialesGame game = (ManantialesGame) board.getGame(gameId);
         ManantialesFicha ficha = new ManantialesFicha(6,4, alice.getColor(),
                 TokenType.MODERATE_PASTURE);
         ManantialesMove move = new ManantialesMove (alice, ficha);
         Move mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(7,4, bob.getColor(),
                 TokenType.MODERATE_PASTURE);
         bob.setTurn(true);
         move = new ManantialesMove (bob, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(8,4, charlie.getColor(),
                 TokenType.MODERATE_PASTURE);
         charlie.setTurn(true);
         move = new ManantialesMove (charlie, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         game = (ManantialesGame) board.getGame(gameId);
         assertEquals (3, game.getMoves().size());
         assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
         assertEquals (1, game.getCheckConditions().size());
     }
 
     @Test
     public void testSouthCheckConstraints () throws InvalidMoveException {
         ManantialesGame game = (ManantialesGame) board.getGame(gameId);
         ManantialesFicha ficha = new ManantialesFicha(4,6, alice.getColor(),
                 TokenType.MODERATE_PASTURE);
         ManantialesMove move = new ManantialesMove (alice, ficha);
         Move mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         ficha = new ManantialesFicha(4,7, bob.getColor(),
                 TokenType.MODERATE_PASTURE);
         bob.setTurn(true);
         move = new ManantialesMove (bob, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         charlie.setTurn(true);
         ficha = new ManantialesFicha(4,8, charlie.getColor(),
                 TokenType.MODERATE_PASTURE);
         move = new ManantialesMove (charlie, ficha);
         game = (ManantialesGame) board.getGame(gameId);
         mv = board.doMove(game, move);
         assertEquals(MoveStatus.EVALUATED, mv.getStatus());
         game = (ManantialesGame) board.getGame(gameId);
         assertEquals (3, game.getMoves().size());
         assertTrue ("CheckConstraint not fired!", game.getCheckConditions() != null);
         assertEquals(1, game.getCheckConditions().size());
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testSuggestionAccepted () throws InvalidMoveException, InvalidSuggestionException {
         Game game = board.getGame(gameId);
         ManantialesGame impl = (ManantialesGame) game;
         impl.setMode (Mode.BASIC_PUZZLE);
         ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(), TokenType.MODERATE_PASTURE);
         ManantialesFicha change = new ManantialesFicha(4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);
 
         ManantialesMove move = new ManantialesMove (alice, play);
         Move mve  = board.doMove (game, move);
         move = (ManantialesMove) mve;
 
         System.out.println ("Move: [" + move.getId() + "] =" + move);
 
         PuzzleSuggestion pzs = new PuzzleSuggestion();
         pzs.setSuggestor(bob);
         pzs.setStatus(SuggestionStatus.UNEVALUATED);
         move = new ManantialesMove (alice,play,change);
         pzs.setMove (move);
 
         Suggestion suggestion = board.makeSuggestion(game, pzs);
         pzs = (PuzzleSuggestion) suggestion;
         move = pzs.getMove();
 
         System.out.println ("Suggestion [" + pzs.getStatus() + "]: " + pzs);
         System.out.println ("Move: [" + move.getId() + "] =" + move);
 
         assertTrue (pzs.getStatus() == SuggestionStatus.EVALUATED);
         assertTrue ("Move status incorrect!  Status [" + move.getStatus() + "]", move.getStatus().equals(
                 MoveStatus.UNVERIFIED));
         System.out.println ("Suggestion [" + pzs.getStatus() + "]: " + pzs);
 
         pzs.setStatus(SuggestionStatus.ACCEPT);
         suggestion = board.makeSuggestion(game, pzs);
         pzs = (PuzzleSuggestion) suggestion;
         move = pzs.getMove();
 
         System.out.println ("Suggestion [" + pzs.getStatus() + "]: " + pzs);
         System.out.println ("Move: [" + move.getId() + "] =" + move);
 
         assertTrue ("Move not evaluated@!  Status [" + move.getStatus() + "]", move.getStatus().equals(
                 MoveStatus.EVALUATED));
 
         GridGame gridGame = (GridGame) board.getGame(gameId);
         GameGrid grid = gridGame.getGrid();
         GridCell location =  grid.getLocation(move.getDestinationCell());
         assertTrue ("Destination not populated!", location != null);
         assertTrue ("Destination not populated!", location.equals(change));
         assertTrue ("Location remains!", grid.getLocation(move.getCurrentCell()) == null);
         assertTrue ("Gamegrid is contains both tokens!", grid.getCells().size() == 1);
 
         System.out.println ("GameGrid: " + grid);
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testSuggestionRejected () throws InvalidMoveException, InvalidSuggestionException {
         Game game = board.getGame(gameId);
         ManantialesGame impl = (ManantialesGame) game;
         impl.setMode(Mode.BASIC_PUZZLE);
         ManantialesFicha play = new ManantialesFicha(5, 4, alice.getColor(), TokenType.MODERATE_PASTURE);
         ManantialesFicha change = new ManantialesFicha(4, 0, alice.getColor(), TokenType.MODERATE_PASTURE);
 
         ManantialesMove move = new ManantialesMove (alice, play);
         board.doMove (game, move);
 
         PuzzleSuggestion pzs = new PuzzleSuggestion();
         pzs.setSuggestor(bob);
         pzs.setStatus(SuggestionStatus.UNEVALUATED);
        move = new ManantialesMove(alice,play,change);
         pzs.setMove (move);
 
         Suggestion suggestion = board.makeSuggestion(game, pzs);
         pzs = (PuzzleSuggestion) suggestion;
         move = pzs.getMove();
 
         assertTrue (pzs.getStatus() == SuggestionStatus.EVALUATED);
 
         pzs.setStatus(SuggestionStatus.REJECT);
         suggestion = board.makeSuggestion(game, pzs);
         pzs = (PuzzleSuggestion) suggestion;
         move = pzs.getMove();
         assertTrue ("Move evaluated!  Status [" + move.getStatus() + "]", move.getStatus().equals(MoveStatus.UNVERIFIED));
 
         game = board.getGame(gameId);
         GridGame gridGame = (GridGame) game;
         GameGrid grid = gridGame.getGrid();
         GridCell location =  grid.getLocation(move.getDestinationCell());
         assertTrue ("Destination populated!", location == null);
         assertTrue ("Location does not remain!", grid.getLocation(move.getCurrentCell()) != null);
         assertTrue ("Gamegrid is contains both tokens!", grid.getCells().size() == 1);
     }
 }
