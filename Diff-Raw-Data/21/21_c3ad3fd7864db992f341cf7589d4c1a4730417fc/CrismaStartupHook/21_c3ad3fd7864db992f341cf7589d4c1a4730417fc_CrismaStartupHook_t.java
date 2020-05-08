 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.crisma;
 
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.MutableConstraints;
 import Sirius.navigator.ui.widget.FloatingFrameConfigurator;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.ImageUtilities;
 import org.openide.util.lookup.ServiceProvider;
 
 import java.awt.EventQueue;
 
 
 import javax.swing.ImageIcon;
 
 import de.cismet.tools.configuration.StartupHook;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 @ServiceProvider(service = StartupHook.class)
 public final class CrismaStartupHook implements StartupHook {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(CrismaStartupHook.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void applicationStarted() {
         final Runnable r = new Runnable() {
 
                 @Override
                 public void run() {
                     final ImageIcon wsIcon = ImageUtilities.loadImageIcon(
                             CrismaStartupHook.this.getClass().getPackage().getName().replaceAll("\\.", "/")
                                     + "/earth.gif",
                             false);
                     final ScenarioView view = ScenarioView.getInstance();
                     final FloatingFrameConfigurator configurator = new FloatingFrameConfigurator(
                             "ScenarioViewer",
                            "Scenario Viewer"); 
                     configurator.setTitleBarEnabled(false);
 
                     final MutableConstraints attributePanelConstraints = new MutableConstraints(true);
                     attributePanelConstraints.addAsFloatingFrame(
                         "ScenarioViewer",
                         view,
                        "Scenario Viewer",            
                        "Worldstate scenario viewer", 
                         wsIcon,
                         MutableConstraints.P2,
                         0,
                         false,
                         configurator,
                         false);
 
                     try {
                        ComponentRegistry.getRegistry().getGUIContainer().add(attributePanelConstraints);
                     } catch (final Exception ex) {
                         LOG.fatal("cannot add scenario viewer", ex);
                     }
 
                     view.updateLeafs();
                 }
             };
         if (EventQueue.isDispatchThread()) {
             r.run();
         } else {
             EventQueue.invokeLater(r);
         }
     }
 }
