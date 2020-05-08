 package com.alexrnl.commons.gui.swing;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URISyntaxException;
 import java.nio.file.Paths;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.LookAndFeel;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.alexrnl.commons.translation.GUIElement;
 import com.alexrnl.commons.translation.Translator;
 
 /**
  * Test suite for the {@link SwingUtils} class.
  * @author Alex
  */
 public class SwingUtilsTest {
 	/** Translator to use */
 	private Translator translator;
 	
 	/**
 	 * Set up test attributes.
 	 * @throws URISyntaxException
 	 *         if there is a problem with the path of the translation file.
 	 */
 	@Before
 	public void setUp () throws URISyntaxException {
 		translator = new Translator(Paths.get(getClass().getResource("/locale.xml").toURI()));
 	}
 	
 	/**
 	 * Test method for
 	 * {@link com.alexrnl.commons.gui.swing.SwingUtils#setLookAndFeel(java.lang.String)}.
 	 * @throws ClassNotFoundException
 	 *         if a LAF class cannot be found.
 	 * @throws IllegalAccessException
 	 *         if a LAF class cannot be accessed.
 	 * @throws InstantiationException
 	 *         if a LAF class cannot be instantiated.
 	 */
 	@Test
 	public void testSetLookAndFeel () throws InstantiationException, IllegalAccessException, ClassNotFoundException {
 		assertFalse(SwingUtils.setLookAndFeel(null));
 		assertFalse(SwingUtils.setLookAndFeel(""));
 		for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
 			if (!Class.forName(laf.getClassName()).asSubclass(LookAndFeel.class)
 					.newInstance().isSupportedLookAndFeel()) {
 				// Skip the test if the current look and feel cannot be installed on the platform
 				continue;
 			}
 			assertTrue(SwingUtils.setLookAndFeel(laf.getName()));
 			Logger.getLogger(SwingUtils.class.getName()).setLevel(Level.FINE);
 		}
 		UIManager.installLookAndFeel(new LookAndFeelInfo("LDR", "com.alexrnl.commons.gui.swing.ldr"));
 		assertFalse(SwingUtils.setLookAndFeel("LDR"));
 	}
 	
 	/**
 	 * Test method for {@link com.alexrnl.commons.gui.swing.SwingUtils#getMenuItem(Translator, com.alexrnl.commons.translation.GUIElement, java.awt.event.ActionListener)}.
 	 */
 	@Test
 	public void testGetMenuItem () {
 		final JMenuItem file = SwingUtils.getMenuItem(translator, new GUIElement("commons.menu.file"), null);
 		assertEquals("File", file.getText());
 		assertEquals(KeyStroke.getKeyStroke("ctrl f"), file.getAccelerator());
 		assertEquals("Access to basic commands", file.getToolTipText());
 		assertEquals(0, file.getActionListeners().length);
 		
 		final JMenuItem help = SwingUtils.getMenuItem(translator, new GUIElement("commons.menu.help"), new ActionListener() {
 			@Override
 			public void actionPerformed (final ActionEvent e) {}
 		});
 		assertEquals("Help", help.getText());
 		assertNull(help.getAccelerator());
 		assertNull(help.getToolTipText());
 		assertEquals(1, help.getActionListeners().length);
 	}
 		
 	/**
 	 * Test method for {@link com.alexrnl.commons.gui.swing.SwingUtils#getMenu(com.alexrnl.commons.translation.Translator, String)}.
 	 */
 	@Test
 	public void testGetMenu () {
 		final JMenu open = SwingUtils.getMenu(translator, "commons.menu.file.open");
 		assertEquals("Open a file", open.getText());
 		assertEquals('O', open.getMnemonic());
 
 		final JMenu close = SwingUtils.getMenu(translator, "commons.menu.file.close");
 		assertEquals("Close a file", close.getText());
 		assertEquals('\0', close.getMnemonic());
 
 		final JMenu quit = SwingUtils.getMenu(translator, "commons.menu.file.quit");
 		assertEquals("Quit the application", quit.getText());
 		assertEquals('\0', quit.getMnemonic());
 	}
 }
