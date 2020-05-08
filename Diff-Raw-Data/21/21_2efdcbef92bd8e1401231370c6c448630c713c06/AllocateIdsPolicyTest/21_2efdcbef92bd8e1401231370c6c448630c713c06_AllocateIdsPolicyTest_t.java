 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * jsommerfeldt
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.recording.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertNull;
 
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.bowling.BowlingFactory;
 import org.eclipse.emf.emfstore.bowling.Game;
 import org.eclipse.emf.emfstore.bowling.Matchup;
 import org.eclipse.emf.emfstore.client.test.common.cases.ESTestWithLoggedInUser;
 import org.eclipse.emf.emfstore.client.test.common.dsl.Create;
 import org.eclipse.emf.emfstore.client.test.common.util.ProjectUtil;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.commands.EMFStoreBasicCommandStack;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.impl.IdEObjectCollectionImpl;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.test.model.TestElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Test the allocate ids policy of the {@link org.eclipse.emf.emfstore.internal.common.model.IdEObjectCollection}.
  * 
  * @author jsommerfeldt
  * 
  */
 public class AllocateIdsPolicyTest extends ESTestWithLoggedInUser {
 
 	@BeforeClass
 	public static void beforeClass() {
 		startEMFStore();
 	}
 
	@AfterClass
	public static void afterClass() {
		stopEMFStore();
	}

	@Override
	@Before
	public void before() {
		super.before();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

 	/**
 	 * Check clear after server actions.
 	 * 
 	 * @throws ESException if there is a problem during a server action.
 	 */
 	@Test
 	public void clearAfterServerAction() throws ESException {
 		final TestElement element = Create.testElement();
 
 		addRemoveObject(element);
 		ProjectUtil.share(getUsersession(), getProjectSpace().toAPI());
 		ProjectUtil.commit(getProjectSpace().toAPI());
 		assertNull(((IdEObjectCollectionImpl) getProject()).getDeletedModelElementId(element));
 
 		addRemoveObject(element);
 		getProjectSpace().toAPI().shareProject(new NullProgressMonitor());
 		assertNull(((IdEObjectCollectionImpl) getProject()).getDeletedModelElementId(element));
 	}
 
 	private void addRemoveObject(final EObject object) {
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				getProject().addModelElement(object);
 				getProject().deleteModelElement(object);
 				return null;
 			}
 		});
 		assertNull(((IdEObjectCollectionImpl) getProject()).getDeletedModelElementId(object));
 	}
 
 	/**
 	 * Test using the command id policy.
 	 */
 	@Test
 	public void commandIdAllocation() {
 		removeAddWithCommand(new EqualComparator());
 		removeAddWithCommands(new NotEqualComparator());
 		if (ESWorkspaceProviderImpl.getInstance().getEditingDomain().getCommandStack() instanceof EMFStoreBasicCommandStack) {
 			removeAddWithoutCommand(new NotEqualComparator());
 			removeAddWithoutCommand2(new NotEqualComparator());
 		}
 	}
 
 	/**
 	 * Remove and add objects wihtin one command.
 	 * 
 	 * @param comparator The {@link IdComparator} to compare ids.
 	 */
 	public void removeAddWithCommand(IdComparator comparator) {
 		final Matchup matchup = BowlingFactory.eINSTANCE.createMatchup();
 		final Game game = BowlingFactory.eINSTANCE.createGame();
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				getProject().addModelElement(matchup);
 				return null;
 			}
 		});
 		final ModelElementId matchupId1 = getProject().getModelElementId(matchup);
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				matchup.getGames().add(game);
 				return null;
 			}
 		});
 		final ModelElementId gameId1 = getProject().getModelElementId(game);
 
 		// remove and add matchup in different commands
 		RunESCommand.run(new Callable<Void>() {
 
 			public Void call() throws Exception {
 				getProject().deleteModelElement(matchup);
 				getProject().addModelElement(matchup);
 				return null;
 			}
 		});
 
 		comparator.compare(matchupId1, getProject().getModelElementId(matchup));
 		comparator.compare(gameId1, getProject().getModelElementId(game));
 	}
 
 	/**
 	 * Remove and add objects within several commands.
 	 * 
 	 * @param comparator The {@link IdComparator} to compare ids.
 	 */
 	public void removeAddWithCommands(IdComparator comparator) {
 		final Matchup matchup = BowlingFactory.eINSTANCE.createMatchup();
 		final Game game = BowlingFactory.eINSTANCE.createGame();
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				getProject().addModelElement(matchup);
 				return null;
 			}
 		});
 		final ModelElementId matchupId1 = getProject().getModelElementId(matchup);
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				matchup.getGames().add(game);
 				return null;
 			}
 		});
 
 		final ModelElementId gameId1 = getProject().getModelElementId(game);
 
 		// remove and add matchup in different commands
 		RunESCommand.run(new Callable<Void>() {
 
 			public Void call() throws Exception {
 				getProject().deleteModelElement(matchup);
 				return null;
 			}
 		});
 		RunESCommand.run(new Callable<Void>() {
 
 			public Void call() throws Exception {
 				getProject().addModelElement(matchup);
 				return null;
 			}
 		});
 
 		comparator.compare(matchupId1, getProject().getModelElementId(matchup));
 		comparator.compare(gameId1, getProject().getModelElementId(game));
 	}
 
 	/**
 	 * Remove and add objects without commands.
 	 * 
 	 * @param comparator The {@link IdComparator} to compare ids.
 	 */
 	public void removeAddWithoutCommand(IdComparator comparator) {
 		final Matchup matchup = BowlingFactory.eINSTANCE.createMatchup();
 		final Game game = BowlingFactory.eINSTANCE.createGame();
 		getProject().addModelElement(matchup);
 		final ModelElementId matchupId1 = getProject().getModelElementId(matchup);
 
 		matchup.getGames().add(game);
 
 		final ModelElementId gameId1 = getProject().getModelElementId(game);
 
 		// remove and add matchup without command
 		getProject().deleteModelElement(matchup);
 		getProject().addModelElement(matchup);
 
 		comparator.compare(matchupId1, getProject().getModelElementId(matchup));
 		comparator.compare(gameId1, getProject().getModelElementId(game));
 	}
 
 	/**
 	 * Remove an element by setting the reference to null and add it again- All without commands.
 	 * 
 	 * @param comparator The {@link IdComparator} to compare ids.
 	 */
 	public void removeAddWithoutCommand2(IdComparator comparator) {
 		final Matchup matchup = BowlingFactory.eINSTANCE.createMatchup();
 		final Game game = BowlingFactory.eINSTANCE.createGame();
 		getProject().addModelElement(matchup);
 		matchup.getGames().add(game);
 		final ModelElementId gameId1 = getProject().getModelElementId(game);
 
 		// remove and add matchup without command
 		game.setMatchup(null);
 		matchup.getGames().add(game);
 
 		comparator.compare(gameId1, getProject().getModelElementId(game));
 	}
 
 	/**
 	 * Internal interface for comparison.
 	 * 
 	 * @author jsommerfeldt
 	 * 
 	 */
 	private interface IdComparator {
 		void compare(ModelElementId id1, ModelElementId id2);
 	}
 
 	/**
 	 * Internal equal {@link IdComparator}.
 	 * 
 	 * @author jsommerfeldt
 	 * 
 	 */
 	private static class EqualComparator implements IdComparator {
 		public void compare(ModelElementId id1, ModelElementId id2) {
 			assertEquals(id1.getId(), id2.getId());
 		}
 	}
 
 	/**
 	 * Internal not equal {@link IdComparator}.
 	 * 
 	 * @author jsommerfeldt
 	 * 
 	 */
 	private static class NotEqualComparator implements IdComparator {
 		public void compare(ModelElementId id1, ModelElementId id2) {
 			assertNotSame(id1.getId(), id2.getId());
 		}
 	}
 }
