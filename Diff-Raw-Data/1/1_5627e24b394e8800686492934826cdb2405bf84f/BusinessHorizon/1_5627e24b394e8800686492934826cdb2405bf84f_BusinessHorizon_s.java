 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber 
  *
  * Copyright 2010: Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.SplashScreen;
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformController;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.PluginManager;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  * 
  * This is the entry class for Business Horizon.
  * 
  * The main method of this class will be called when Business Horizon starts.
  * 
  * @author Robert Vollmer
  * @version 0.2, 20.12.2009
  * 
  * 
  */
 
 public class BusinessHorizon implements IPlatformListener{
 	public static final boolean DEBUG = true;
 	private static final Logger log = Logger.getLogger(BusinessHorizon.class);
 
 	private static Graphics2D graphics;
 	private static final SplashScreen splash = SplashScreen.getSplashScreen();
 
 	/**
 	 * @param args
 	 *            Commandline arguments
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		// Get Splash Screen from jar signing
 		if (splash == null) {
 			System.out.println("Could not get the splash screen. The application might not be started from the \".jar\"");
 		} else {
 
 			graphics = splash.createGraphics();
 			if (graphics == null) {
 				System.out
 						.println("Could not create Graphics for Splash screen.");
 			}
 		}
 
 		updateSplash("Loading Logger...");
 		// End splash screen
 
 		if (DEBUG)
 			Services.setupLogger();
 
 		log.info("Business Horizon is starting...");
 
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			@Override
 			public void uncaughtException(Thread t, Throwable e) {
 				log.error("Uncaught exception", e);
 			}
 		});
 
 		if (SVN.isRevisionSet())
 			log.info("SVN Revision is " + SVN.getRevision());
 
 		updateSplash("Check java version...");
 		
 		// Check if JRE is Java 6 Update 10, else quit.
 		if (!Services.jreFulfillsRequirements()) {
 			String message = Services.getTranslator().translate(
 					"PjreRequirement", ITranslator.LONG);
 			String title = Services.getTranslator()
 					.translate("PjreRequirement");
 			log.error(message);
 			JOptionPane.showMessageDialog(null, message, title,
 					JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 
 		updateSplash("Set security manager...");
 		
 		System.setSecurityManager(null);
 		
 		updateSplash("Initialize plugins...");
 
 		PluginManager.init();
 
 		// set menu name
 		if (System.getProperty("os.name").startsWith("Mac OS X"))
 			System.setProperty(
 					"com.apple.mrj.application.apple.menu.about.name",
 					"Business Horizon");
 
 		updateSplash("Set Look&Feel...");
 		// set Look&Feel
 		Services.setNimbusLookAndFeel();
 
 		// Invoke start of BHMainFrame
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				PlatformController.getInstance();
 			}
 		});
 
 		Services.addPlatformListener(new BusinessHorizon());
 		
 		Thread.sleep(100); // The other Thread is loading in this time anyway.
 		                   //Create the impression of something dynamic.
 		updateSplash("Loading Plattform...");
 	}
 	
 	/**
 	 * This method updates the string displayed on the splashScreen.
 	 * The User should receive immediate Feedback on what is happening in the backend.
 	 * @param text The text you want to display on the splash screen.
 	 */
 	public static void updateSplash(String text){
 		if (graphics != null) {
 			//Revert former changes
 			graphics.setComposite(AlphaComposite.Clear);
 			graphics.fillRect(1, 1, 477, 229);
 			
 			//Paint new string
 			graphics.setPaintMode();
 			graphics.setColor(Color.BLACK);
 			graphics.setFont(graphics.getFont().deriveFont(18F));
 			graphics.drawString(text, 13, 215);
 		
 			//Update 2D graphic
 			splash.update();
 		}
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		//Handle Platform loaded event and close splash (everything is done)
 		if(e.getEventType() == Type.PLATFORM_LOADING_COMPLETED){
 			if(splash != null){
 				splash.close();
 			}
 		}
 		Services.removePlatformListener(this);
 	}
 }
