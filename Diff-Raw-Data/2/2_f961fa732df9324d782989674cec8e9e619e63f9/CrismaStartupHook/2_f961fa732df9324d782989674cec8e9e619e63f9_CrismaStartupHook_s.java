 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.crisma;
 
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.LayoutedContainer;
 import Sirius.navigator.ui.MutableConstraints;
 import Sirius.navigator.ui.widget.FloatingFrameConfigurator;
 
 import org.openide.util.ImageUtilities;
 import org.openide.util.lookup.ServiceProvider;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
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
     private static final transient Logger LOG = LoggerFactory.getLogger(CrismaStartupHook.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void applicationStarted() {
         final Runnable r = new Runnable() {
 
                 @Override
                 public void run() {
                     final ImageIcon wsIcon = ImageUtilities.loadImageIcon(
                             CrismaStartupHook.this.getClass().getPackage().getName().replaceAll("\\.", "/")
                                     + "/world_leaf_16.png",
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
                         ((LayoutedContainer)ComponentRegistry.getRegistry().getGUIContainer()).loadLayout(
                             "/Users/mscholl/.navigator/martin.layout",
                             false,
                             null);
                     } catch (final Exception ex) {
                         LOG.error("cannot add scenario viewer", ex);
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
