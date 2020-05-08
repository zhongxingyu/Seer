 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.ui.controllers;
 
 import java.util.concurrent.Callable;
 
 import org.eclipse.emf.emfstore.bowling.League;
 import org.eclipse.emf.emfstore.bowling.Player;
 import org.eclipse.emf.emfstore.bowling.Tournament;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.test.api.ProjectChangeUtil;
 import org.eclipse.emf.emfstore.client.test.ui.AllUITests;
 import org.eclipse.emf.emfstore.client.ui.ESUIControllerFactory;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.ui.controller.UICheckoutController;
 import org.eclipse.emf.emfstore.internal.client.ui.controller.UIUpdateProjectController;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
 
 public abstract class AbstractUIControllerTestWithCommit extends AbstractUIControllerTest {
 
 	public static final String PLAYER_NAME = "A";
 	public static final String LEAGUE_NAME = "L";
 
 	private ESLocalProject checkedoutCopy;
 
 	protected void createTournamentAndCommit() {
 		final Tournament tournament = ProjectChangeUtil.createTournament(true);
 		final ESPrimaryVersionSpec baseVersion = localProject.getBaseVersion();
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				localProject.getModelElements().add(tournament);
 				return null;
 			};
 		});
 		commit(baseVersion);
 	}
 
 	protected void createLeagueAndCommit() {
 		final League league = ProjectChangeUtil.createLeague("L");
 		final ESPrimaryVersionSpec baseVersion = localProject.getBaseVersion();
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				localProject.getModelElements().add(league);
 				return null;
 			}
 		});
 		commit(baseVersion);
 	}
 
 	protected void createPlayerAndCommit() {
 		final Player player = ProjectChangeUtil.createPlayer(PLAYER_NAME);
 		final ESPrimaryVersionSpec baseVersion = localProject.getBaseVersion();
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				localProject.getModelElements().add(player);
 				return null;
 			}
 		});
 		commit(baseVersion);
 	}
 
 	private void commit(final ESPrimaryVersionSpec baseVersion) {
 		UIThreadRunnable.asyncExec(new VoidResult() {
 			public void run() {
 				ESUIControllerFactory.INSTANCE.commitProject(
 					bot.getDisplay().getActiveShell(),
 					localProject);
 			}
 		});
 
 		SWTBotButton buttonWithLabel = bot.button("OK");
 		buttonWithLabel.click();
 
 		bot.waitUntil(new DefaultCondition() {
 
 			public boolean test() throws Exception {
 				return baseVersion.getIdentifier() + 1 == localProject.getBaseVersion().getIdentifier();
 			}
 
 			public String getFailureMessage() {
 				return "Commit did not succeed.";
 			}
 		}, AllUITests.TIMEOUT);
 
 		assertEquals(baseVersion.getIdentifier() + 1,
 			localProject.getBaseVersion().getIdentifier());
 		System.out.println("commit succeeded");
 	}
 
 	protected void checkout() {
 		UIThreadRunnable.asyncExec(new VoidResult() {
 
 			public void run() {
 				UICheckoutController checkoutController;
 				try {
 					checkoutController = new UICheckoutController(
 						bot.getDisplay().getActiveShell(),
 						localProject.getRemoteProject());
 					checkedoutCopy = checkoutController.execute();
 				} catch (ESException e) {
 					fail(e.getMessage());
 				}
 			}
 		});
 
 		bot.text().setText("checkout");
 		bot.button("OK").click();
 
 		bot.waitUntil(new DefaultCondition() {
 
 			public boolean test() throws Exception {
 				return checkedoutCopy != null;
 			}
 
 			public String getFailureMessage() {
 				return "Checkout did not succeed";
 			}
 		}, timeout());
 	}
 
 	public ESLocalProject getCheckedoutCopy() {
 		return checkedoutCopy;
 	}
 
 	protected ESPrimaryVersionSpec update() {
 
 		UIThreadRunnable.asyncExec(new VoidResult() {
 			public void run() {
 				UIUpdateProjectController updateProjectController = new UIUpdateProjectController(
 					bot.getDisplay().getActiveShell(),
 					getCheckedoutCopy());
 				updateProjectController.execute();
 			}
 		});
 
 		SWTBotButton buttonWithLabel = bot.button(0);
 		buttonWithLabel.click();
 
 		bot.waitUntil(new DefaultCondition() {
 			public boolean test() throws Exception {
 				return getCheckedoutCopy().getBaseVersion().getIdentifier() ==
 				localProject.getBaseVersion().getIdentifier();
 			}
 
 			public String getFailureMessage() {
 				return "Update did not succeed.";
 			}
 		}, timeout());
 
 		return getCheckedoutCopy().getBaseVersion();
 	}
 
 	protected ESPrimaryVersionSpec pagedUpdate() {
 
 		UIThreadRunnable.asyncExec(new VoidResult() {
 			public void run() {
 				UIUpdateProjectController updateProjectController = new UIUpdateProjectController(
 					bot.getDisplay().getActiveShell(),
 					getCheckedoutCopy());
 				updateProjectController.execute();
 			}
 		});
 
 		SWTBotButton buttonWithLabel = bot.button(0);
 		buttonWithLabel.click();
 
 		bot.waitUntil(new DefaultCondition() {
 			public boolean test() throws Exception {
 				return getCheckedoutCopy().getBaseVersion().getIdentifier() ==
 				localProject.getBaseVersion().getIdentifier() - 1;
 			}
 
 			public String getFailureMessage() {
 				return "Paged Update did not succeed.";
 			}
 		}, timeout());
 
 		bot.button(0).click(); // update notification hint
 		bot.button(0).click(); // inspect changes on update
 
 		bot.waitUntil(new DefaultCondition() {
 			public boolean test() throws Exception {
 				return getCheckedoutCopy().getBaseVersion().getIdentifier() ==
 				localProject.getBaseVersion().getIdentifier();
 			}
 
 			public String getFailureMessage() {
 				return "Paged Update did not succeed.";
 			}
 		}, timeout());
 
 		return getCheckedoutCopy().getBaseVersion();
 	}
 }
