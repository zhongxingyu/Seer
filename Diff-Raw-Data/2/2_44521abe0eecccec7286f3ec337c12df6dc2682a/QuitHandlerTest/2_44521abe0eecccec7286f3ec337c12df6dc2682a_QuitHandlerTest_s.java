 package com.gh.jordner.gui.actions.test;
 
 import org.eclipse.swtbot.swt.finder.SWTBot;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
 import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
 import org.eclipse.swtbot.swt.finder.waits.Conditions;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class QuitHandlerTest {
 
 	private static SWTBot bot;
 
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 		SWTBotPreferences.TIMEOUT = 6000;
 		bot = new SWTBot();
 	}
 
 	@AfterClass
 	public static void sleep() {
 		bot.sleep(2000);
 	}
 
 	@Test
 	public void executeExit() {
 		SWTBotMenu fileMenu = bot.menu("File");
 		Assert.assertNotNull(fileMenu);
 		SWTBotMenu exitMenu = fileMenu.menu("Quit");
 		Assert.assertNotNull(exitMenu);
 		exitMenu.click();
 		SWTBotShell popup = bot.shell("Confirmation").activate();
 		if (popup == null) {
 			System.out.println("Close Dialog nicht geöffnet");
 		} else {
 			bot.waitUntil(Conditions.shellIsActive("Confirmation"));
			bot.button("OK").click();
 		}
 
 	}
 
 }
