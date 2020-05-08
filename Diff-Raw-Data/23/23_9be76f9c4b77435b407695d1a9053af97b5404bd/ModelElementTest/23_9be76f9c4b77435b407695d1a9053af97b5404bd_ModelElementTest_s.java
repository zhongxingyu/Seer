 /*******************************************************************************
  * Copyright 2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.api;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ConcurrentModificationException;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.bowling.Game;
 import org.eclipse.emf.emfstore.bowling.League;
 import org.eclipse.emf.emfstore.bowling.Matchup;
 import org.eclipse.emf.emfstore.bowling.Player;
 import org.eclipse.emf.emfstore.bowling.Tournament;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.common.model.ESModelElementId;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommand;
 import org.junit.Test;
 
 /**
  * ModelElementTest.
  * 
  * @author Tobias Verhoeven
  */
 public class ModelElementTest {
 
 	/**
 	 * Tests adding model elements.
 	 */
 	@Test
 	public void testAddModelElementsWithoutCommands() {
 		ESLocalProject localProject = ESWorkspaceProvider.INSTANCE.getWorkspace().createLocalProject(
 			"Testprojekt", "");
 
 		League leagueA = ProjectChangeUtil.createLeague("America");
 		League leagueB = ProjectChangeUtil.createLeague("Europe");
 
 		localProject.getModelElements().add(leagueA);
 		localProject.getModelElements().add(leagueB);
 
 		assertEquals(2, localProject.getAllModelElements().size());
 
 		Player playerA = ProjectChangeUtil.createPlayer("Hans");
 		Player playerB = ProjectChangeUtil.createPlayer("Anton");
 
 		leagueA.getPlayers().add(playerA);
 		leagueA.getPlayers().add(playerB);
 
 		assertEquals(4, localProject.getAllModelElements().size());
 
 		Player playerC = ProjectChangeUtil.createPlayer("Paul");
 		Player playerD = ProjectChangeUtil.createPlayer("Klaus");
 
 		leagueA.getPlayers().add(playerC);
 		leagueA.getPlayers().add(playerD);
 
 		assertEquals(6, localProject.getAllModelElements().size());
 		assertEquals(2, localProject.getModelElements().size());
 
 		Tournament tournamentA = ProjectChangeUtil.createTournament(false);
 		localProject.getModelElements().add(tournamentA);
 
 		Matchup matchupA = ProjectChangeUtil.createMatchup(null, null);
 		Matchup matchupB = ProjectChangeUtil.createMatchup(null, null);
 
 		Game gameA = ProjectChangeUtil.createGame(playerA);
 		Game gameB = ProjectChangeUtil.createGame(playerB);
 		Game gameC = ProjectChangeUtil.createGame(playerC);
 		Game gameD = ProjectChangeUtil.createGame(playerD);
 
 		tournamentA.getMatchups().add(matchupA);
 		matchupA.getGames().add(gameA);
 		matchupA.getGames().add(gameB);
 
 		assertEquals(10, localProject.getAllModelElements().size());
 
 		tournamentA.getMatchups().add(matchupB);
 		matchupB.getGames().add(gameC);
 		matchupB.getGames().add(gameD);
 
 		assertEquals(13, localProject.getAllModelElements().size());
 		assertEquals(3, localProject.getModelElements().size());
 	}
 
 	/**
 	 * Tests adding model elements.
 	 */
 	@Test
 	public void testAddModelElementsWithCommands() {
 		final ESLocalProject localProject = ESWorkspaceProvider.INSTANCE.getWorkspace().createLocalProject(
 			"Testprojekt", "");
 
 		final League leagueA = ProjectChangeUtil.createLeague("America");
 		final League leagueB = ProjectChangeUtil.createLeague("Europe");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.getModelElements().add(leagueA);
 				localProject.getModelElements().add(leagueB);
 			}
 		}.execute();
 
 		assertEquals(2, localProject.getAllModelElements().size());
 
 		final Player playerA = ProjectChangeUtil.createPlayer("Hans");
 		final Player playerB = ProjectChangeUtil.createPlayer("Anton");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				leagueA.getPlayers().add(playerA);
 				leagueA.getPlayers().add(playerB);
 			}
 		}.execute();
 
 		assertEquals(4, localProject.getAllModelElements().size());
 
 		final Player playerC = ProjectChangeUtil.createPlayer("Paul");
 		final Player playerD = ProjectChangeUtil.createPlayer("Klaus");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				leagueA.getPlayers().add(playerC);
 				leagueA.getPlayers().add(playerD);
 			}
 		}.execute();
 
 		assertEquals(6, localProject.getAllModelElements().size());
 		assertEquals(2, localProject.getModelElements().size());
 
 		final Tournament tournamentA = ProjectChangeUtil.createTournament(false);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.getModelElements().add(tournamentA);
 			}
 		}.execute();
 
 		final Matchup matchupA = ProjectChangeUtil.createMatchup(null, null);
 		final Matchup matchupB = ProjectChangeUtil.createMatchup(null, null);
 
 		final Game gameA = ProjectChangeUtil.createGame(playerA);
 		final Game gameB = ProjectChangeUtil.createGame(playerB);
 		final Game gameC = ProjectChangeUtil.createGame(playerC);
 		final Game gameD = ProjectChangeUtil.createGame(playerD);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				tournamentA.getMatchups().add(matchupA);
 				matchupA.getGames().add(gameA);
 				matchupA.getGames().add(gameB);
 			}
 		}.execute();
 
 		assertEquals(10, localProject.getAllModelElements().size());
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				tournamentA.getMatchups().add(matchupB);
 				matchupB.getGames().add(gameC);
 				matchupB.getGames().add(gameD);
 			}
 		}.execute();
 
 		assertEquals(13, localProject.getAllModelElements().size());
 		assertEquals(3, localProject.getModelElements().size());
 	}
 
 	@Test(expected = ConcurrentModificationException.class)
 	public void testDeleteAllModelElementsWithCommand() {
 		final ESLocalProject localProject = ProjectChangeUtil.createBasicBowlingProject();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				Set<EObject> elements = localProject.getAllModelElements();
 				for (EObject object : elements) {
 					localProject.getModelElements().remove(object);
 				}
 			}
 		}.execute();
 
 		assertEquals(0, localProject.getAllModelElements().size());
 	}
 
 	@Test(expected = ConcurrentModificationException.class)
 	public void testDeleteAllModelElementsWithoutCommand() {
 		final ESLocalProject localProject = ProjectChangeUtil.createBasicBowlingProject();
 
 		Set<EObject> elements = localProject.getAllModelElements();
 		for (EObject object : elements) {
 			localProject.getModelElements().remove(object);
 		}
 
 		assertEquals(0, localProject.getAllModelElements().size());
 	}
 
 	/**
 	 * adds and deletes model element and undos the deletion.
 	 */
 	@Test
 	public void testDeleteUndoWithCommand() {
 		final ESLocalProject localProject = ProjectChangeUtil.createBasicBowlingProject();
 		final Player player = ProjectChangeUtil.createPlayer("Heinrich");
 		final int SIZE = localProject.getAllModelElements().size();
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.getModelElements().add(player);
 			}
 		}.run(false);
 
 		assertTrue(localProject.getAllModelElements().contains(player));
 		assertTrue(localProject.contains(player));
 		ESModelElementId id = localProject.getModelElementId(player);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.getModelElements().remove(player);
 			}
 		}.run(false);
 
 		assertEquals(SIZE, localProject.getAllModelElements().size());
 		assertFalse(localProject.getAllModelElements().contains(player));
 		assertFalse(localProject.contains(player));
 		assertNull(localProject.getModelElement(id));
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.undoLastOperation();
 			}
 		}.run(false);
 
 		assertEquals(SIZE + 1, localProject.getAllModelElements().size());
 		assertFalse(localProject.getAllModelElements().contains(player));
 		assertFalse(localProject.contains(player));
 		assertNotNull(localProject.getModelElement(id));
 	}
 
 	/**
 	 * adds and deletes model element and undos the deletion.
 	 */
 	@Test
 	public void testDeleteUndoWithoutCommand() {
 		final ESLocalProject localProject = ProjectChangeUtil.createBasicBowlingProject();
 		final Player player = ProjectChangeUtil.createPlayer("Heinrich");
 		final int SIZE = localProject.getAllModelElements().size();
 
 		localProject.getModelElements().add(player);
 
 		assertTrue(localProject.getAllModelElements().contains(player));
 		assertTrue(localProject.contains(player));
 		ESModelElementId id = localProject.getModelElementId(player);
 		localProject.getModelElements().remove(player);
 
 		assertEquals(SIZE, localProject.getAllModelElements().size());
 		assertFalse(localProject.getAllModelElements().contains(player));
 		assertFalse(localProject.contains(player));
 
 		localProject.undoLastOperation();
 
 		assertEquals(SIZE + 1, localProject.getAllModelElements().size());
 		assertFalse(localProject.getAllModelElements().contains(player));
 		assertFalse(localProject.contains(player));
 		assertNotNull(localProject.getModelElement(id));
 	}
 
 	@Test
 	public void testReferenceDeletionWithCommand() {
 		final ESLocalProject localProject = ProjectChangeUtil.createBasicBowlingProject();
 		final Tournament tournament = ProjectChangeUtil.createTournament(true);
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.getModelElements().add(tournament);
 			}
 		}.run(false);
 
 		final Player player = ProjectChangeUtil.createPlayer("Heinrich");
 		final Player player2 = ProjectChangeUtil.createPlayer("Walter");
 		final Player player3 = ProjectChangeUtil.createPlayer("Wilhelm");
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				tournament.getPlayers().add(player);
 				tournament.getPlayers().add(player2);
 				tournament.getPlayers().add(player3);
 			}
 		}.run(false);
 
 		assertEquals(3, tournament.getPlayers().size());
 
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				localProject.getModelElements().remove(player2);
 			}
 		}.execute();
 
 		assertEquals(2, tournament.getPlayers().size());
 		assertTrue(localProject.contains(player));
 		assertTrue(localProject.contains(player3));
 		assertFalse(localProject.contains(player2));
 	}
 
 	@Test
 	public void testReferenceDeletionWithoutCommand() {
 		final ESLocalProject localProject = ProjectChangeUtil.createBasicBowlingProject();
 		final Tournament tournament = ProjectChangeUtil.createTournament(true);
 		localProject.getModelElements().add(tournament);
 
 		final Player player = ProjectChangeUtil.createPlayer("Heinrich");
 		final Player player2 = ProjectChangeUtil.createPlayer("Walter");
 		final Player player3 = ProjectChangeUtil.createPlayer("Wilhelm");
 
 		tournament.getPlayers().add(player);
 		tournament.getPlayers().add(player2);
 		tournament.getPlayers().add(player3);
 
 		assertEquals(3, tournament.getPlayers().size());
 
 		tournament.getPlayers().remove(player2);
 
 		assertEquals(2, tournament.getPlayers().size());
 		assertTrue(localProject.contains(player));
 		assertTrue(localProject.contains(player3));
		assertFalse(localProject.contains(player2));
 	}
 
 }
