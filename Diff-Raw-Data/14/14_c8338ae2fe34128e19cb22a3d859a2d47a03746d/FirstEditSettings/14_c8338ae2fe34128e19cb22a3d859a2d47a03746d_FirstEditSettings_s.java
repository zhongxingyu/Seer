 package org.kalibro.desktop.settings;
 
 import static org.junit.Assert.*;
 
 import org.fest.swing.fixture.FrameFixture;
 import org.junit.Test;
 import org.kalibro.core.settings.KalibroSettings;
 import org.kalibro.desktop.KalibroDesktopTestCase;
 
 /**
  * On the first execution, if the user edit the settings and confirms, the settings file should be written and the
  * application frame opened. The settings should still be available via menu.
  * 
  * @author Carlos Morais
  */
 public class FirstEditSettings extends KalibroDesktopTestCase {
 
 	@Test(timeout = ACCEPTANCE_TIMEOUT)
 	public void firstEditSettings() throws Exception {
 		startFromMain();
 
		fixture.checkBox("removeSources").requireSelected();
		fixture.checkBox("removeSources").uncheck();
 
 		fixture.button("ok").click();
 		assertTrue(KalibroSettings.settingsFileExists());
 
 		fixture = new FrameFixture(fixture.robot, "kalibroFrame");
 		fixture.menuItem("settings").click();
 		fixture = fixture.dialog();
		fixture.checkBox("removeSources").requireNotSelected();
 	}
 }
