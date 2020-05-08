 /* Copyright 2010-2014 Tiago Ferreira
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 import ij.CompositeImage;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.WindowManager;
 import ij.gui.GenericDialog;
 import ij.io.Opener;
 import ij.plugin.BrowserLauncher;
 import ij.plugin.PlugIn;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Panel;
 import java.awt.ScrollPane;
 import java.awt.SystemColor;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.IndexColorModel;
 import java.io.InputStream;
 
 
 /**
  * Auxiliary commands and routines for Sholl_Analysis. addScrollBars() is from
  * Bio-Formats Window.Tools
  *
  * @see <a href="https://github.com/tferr/ASA">https://github.com/tferr/ASA</a>
  * @see <a href="http://fiji.sc/Sholl_Analysis">http://fiji.sc/Sholl_Analysis</a>
  * @see <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/loci-plugins/src/loci/plugins/util/WindowTools.java;hb=HEAD">git.openmicroscopy</a>
  *
  * @author Tiago Ferreira
  */
 public class Sholl_Utils implements PlugIn {
 
     private static final String BUILD = " 2014.01";
     private static final String SRC_URL = "https://github.com/tferr/ASA";
     private static final String DOC_URL = "http://fiji.sc/Sholl_Analysis";
     private static int background = Sholl_Analysis.maskBackground;
 
     public void run(final String arg) {
         if (arg.equalsIgnoreCase("about"))
             showAbout();
         else if (arg.equalsIgnoreCase("sample"))
             displaySample();
         else if(arg.equalsIgnoreCase("jet"))
             applyJetLut();
     }
 
     /** Displays the ddaC sample image in ./resources */
     void displaySample() {
         final InputStream is = getClass().getResourceAsStream("/resources/ddaC.tif");
         if (is!=null) {
             final Opener opener = new Opener();
             final ImagePlus imp = opener.openTiff(is, "Drosophila_ddaC_Neuron.tif");
             if (imp!=null) imp.show();
         }
     }
 
     /** Applies the "MATLAB Jet" to frontmost image */
     void applyJetLut() {
         ImagePlus imp = WindowManager.getCurrentImage();
         if (imp!=null && imp.getType()==ImagePlus.COLOR_RGB) {
             IJ.error("LUTs cannot be assiged to RGB Images.");
             return;
         }
 
         // Display LUT
         final IndexColorModel cm = Sholl_Analysis.matlabJetColorMap(background);
         if (imp==null) {
             imp = new ImagePlus("MATLAB Jet",ij.plugin.LutLoader.createImage(cm));
             imp.show();
         } else {
             if (imp.isComposite())
                 ((CompositeImage)imp).setChannelColorModel(cm);
             else
                 imp.getProcessor().setColorModel(cm);
             imp.updateAndDraw();
         }
     }
 
     /** Displays an "about" info box */
     void showAbout() {
         final String version = Sholl_Analysis.VERSION + BUILD;
         final String summary = "Quantitative Sholl-based morphometry of untraced neuronal arbors";
         final String authors = "Tiago Ferreira, Tom Maddock (v1.0)";
         final String thanks = "Johannes Schindelin, Wayne Rasband, Mark Longair, Stephan Preibisch,\n"
         		+ "Bio-Formats team";
 
         final Font plainf = new Font("SansSerif", Font.PLAIN, 12);
         final Font boldf = new Font("SansSerif", Font.BOLD, 12);
 
         final GenericDialog gd = new GenericDialog("About Sholl Analysis...");
         gd.addMessage(summary, boldf);
         gd.addMessage("Version", boldf);
         gd.setInsets(0, 20, 0);
         gd.addMessage(version, plainf);
         gd.addMessage("Authors", boldf);
         gd.setInsets(0, 20, 0);
         gd.addMessage(authors, plainf);
         gd.addMessage("Special Thanks", boldf);
         gd.setInsets(0, 20, 0);
         gd.addMessage(thanks, plainf);
         gd.enableYesNoCancel("Browse Documentation", "Browse Source Code");
         gd.hideCancelButton();
         gd.showDialog();
         if (gd.wasCanceled())
             return;
         else if (gd.wasOKed())
             IJ.runPlugIn("ij.plugin.BrowserLauncher", DOC_URL);
         else
             IJ.runPlugIn("ij.plugin.BrowserLauncher", SRC_URL);
     }
 
     /**
      * Adds AWT scroll bars to the given container. From bio-formats Window.Tools,
      * licensed under GNU GPLv2 (April 2013)
      * @see <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/loci-plugins/src/loci/plugins/util/WindowTools.java;hb=HEAD">git.openmicroscopy</a>
      */
     @SuppressWarnings("serial")
     static void addScrollBars(final Container pane) {
 
     	final GridBagLayout layout = (GridBagLayout) pane.getLayout();
 
     	// extract components
     	final int count = pane.getComponentCount();
     	final Component[] c = new Component[count];
     	final GridBagConstraints[] gbc = new GridBagConstraints[count];
     	for (int i=0; i<count; i++) {
     		c[i] = pane.getComponent(i);
     		gbc[i] = layout.getConstraints(c[i]);
     	}
 
     	// clear components
     	pane.removeAll();
     	layout.invalidateLayout(pane);
 
     	// create new container panel, using GenericDialog's background color
     	final Panel newPane = new Panel();
     	final GridBagLayout newLayout = new GridBagLayout();
     	newPane.setBackground(SystemColor.control);
     	newPane.setLayout(newLayout);
     	for (int i=0; i<count; i++) {
     		newLayout.setConstraints(c[i], gbc[i]);
     		newPane.add(c[i]);
     	}
 
     	// HACK - get preferred size for container panel
     	// NB: don't know a better way:
     	// - newPane.getPreferredSize() doesn't work
     	// - newLayout.preferredLayoutSize(newPane) doesn't work
     	final Frame f = new Frame();
     	f.setLayout(new BorderLayout());
     	f.add(newPane, BorderLayout.CENTER);
     	f.pack();
     	final Dimension size = newPane.getSize();
     	f.remove(newPane);
     	f.dispose();
 
     	// compute best size for scrollable viewport
     	size.width += 35; // initially 25;
     	size.height += 30; // initially 15;
     	final Dimension screen = IJ.getScreenSize();
     	final int maxWidth = 9 * screen.width / 10; // initially 7/8;
     	final int maxHeight = 8 * screen.height / 10; // initially 3/4
     	if (size.width > maxWidth) size.width = maxWidth;
     	if (size.height > maxHeight) size.height = maxHeight;
 
     	// create scroll pane
     	final ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED){
     		public Dimension getPreferredSize() {
     			return size;
     		}
     	};
     	scroll.add(newPane);
 
     	// Scrollbar is not placed at top even if scroll.getScrollPosition()
     	// is already java.awt.Point[x=0,y=0]? How to fix it?
     	// 		scroll.setScrollPosition(0,0); //Not working?
     	// 		scroll.setWheelScrollingEnabled(true); //Enabled by default
 
     	// add scroll pane to original container
     	final GridBagConstraints constraints = new GridBagConstraints();
     	constraints.gridwidth = GridBagConstraints.REMAINDER;
     	constraints.fill = GridBagConstraints.BOTH;
     	constraints.weightx = 1.0;
     	constraints.weighty = 1.0;
     	layout.setConstraints(scroll, constraints);
     	pane.add(scroll);
     }
 
 	/**
 	 * Adds a message to a GenericDialog pointing to an URL. From Stephan Preibisch
 	 * @see <a href="https://raw.github.com/fiji/Stitching/master/src/main/java/stitching/CommonFunctions.java>github.com/fiji/Stitching/</a>
 	 */
 	static final void addClickabaleMsg(final GenericDialog gd, final String msg, final String url) {
 		//gd.addMessage(msg, new Font("SansSerif", Font.PLAIN, 12));
 		gd.addMessage(msg);
 		final Component msgLabel = gd.getMessage();
 		msgLabel.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(final MouseEvent paramAnonymousMouseEvent) {
 				try {
					BrowserLauncher.openURL(MS_URL);
 				} catch (final Exception localException) {
 					IJ.error("" + localException);
 				}
 			}
 
 			public void mouseEntered(final MouseEvent paramAnonymousMouseEvent) {
 				msgLabel.setForeground(Color.BLUE);
 				msgLabel.setCursor(new Cursor(12));
 			}
 
 			public void mouseExited(final MouseEvent paramAnonymousMouseEvent) {
 				msgLabel.setForeground(Color.BLACK);
 				msgLabel.setCursor(new Cursor(0));
 			}
 		});
 
 	}
 
 }
