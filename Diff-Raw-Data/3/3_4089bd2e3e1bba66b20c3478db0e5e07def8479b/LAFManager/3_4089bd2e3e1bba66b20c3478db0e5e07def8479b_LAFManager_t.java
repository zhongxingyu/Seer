 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 import org.apache.log4j.Logger;
 
 import java.awt.Component;

 import java.util.Collection;
 import java.util.LinkedHashMap;

 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class LAFManager {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger logger = Logger.getLogger(LAFManager.class);
     private static LAFManager manager = null;
 
     //~ Instance fields --------------------------------------------------------
 
     private final LinkedHashMap installedLookAndFeels;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new LAFManager object.
      */
     private LAFManager() {
         this.installLookAndFeels();
 
         final UIManager.LookAndFeelInfo[] lnfinfo = UIManager.getInstalledLookAndFeels();
 
         this.installedLookAndFeels = new LinkedHashMap(lnfinfo.length);
 
         for (int i = 0; i < lnfinfo.length; i++) {
             if (logger.isDebugEnabled()) {
                 logger.debug("installed look and feel #" + i + ": '" + lnfinfo[i].getName() + "' ("
                             + lnfinfo[i].getClassName() + ")"); // NOI18N
             }
             this.installedLookAndFeels.put(lnfinfo[i].getName(), lnfinfo[i]);
         }
 
         if (logger.isDebugEnabled()) {
             logger.debug("- SystemLookAndFeel class: '" + UIManager.getSystemLookAndFeelClassName() + "'"); // NOI18N
             logger.debug("- CrossPlatformLookAndFeel class: '" + UIManager.getCrossPlatformLookAndFeelClassName()
                         + "'");                                                                             // NOI18N
             logger.debug("- Default look and feel: '" + this.getDefaultLookAndFeel() + "'");                // NOI18N
             logger.debug("- Current look and feel: '" + UIManager.getLookAndFeel() + "'");                  // NOI18N
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static LAFManager getManager() {
         if (manager == null) {
             manager = new LAFManager();
         }
 
         return manager;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public UIManager.LookAndFeelInfo getDefaultLookAndFeel() {
         return UIManager.getInstalledLookAndFeels()[0];
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   lnfName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isInstalledLookAndFeel(final String lnfName) {
         return this.installedLookAndFeels.containsKey(lnfName);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Collection getInstalledLookAndFeelNames() {
         return this.installedLookAndFeels.keySet();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   lnfName  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean changeLookAndFeel(final String lnfName) {
         return this.changeLookAndFeel(lnfName, null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   lnfName    DOCUMENT ME!
      * @param   component  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean changeLookAndFeel(final String lnfName, final Component component) {
         final UIManager.LookAndFeelInfo lnfinfo;
 
         if (this.isInstalledLookAndFeel(lnfName)) {
             lnfinfo = (UIManager.LookAndFeelInfo)this.installedLookAndFeels.get(lnfName);
         } else {
             logger.warn("could not change look & feel: unknown look and feel '" + lnfName + "'"); // NOI18N
             lnfinfo = this.getDefaultLookAndFeel();
         }
 
         try {
             if (logger.isInfoEnabled()) {
                 logger.info("changing look & feel to '" + lnfinfo + "' (" + lnfinfo.getClassName() + ")"); // NOI18N
             }
             if (lnfinfo.getName().equalsIgnoreCase("Plastic 3D"))                                          // NOI18N
             {
                 if (logger.isDebugEnabled()) {
                     logger.debug("setting Plastic 3D Theme");                                              // NOI18N
                 }
             }
 
             UIManager.setLookAndFeel(lnfinfo.getClassName());
 
             if (component != null) {
                 SwingUtilities.updateComponentTreeUI(component);
                 component.validate();
             }
         } catch (final Exception ex) {
             logger.error("could not change look to '" + lnfName + "'", ex); // NOI18N
             return false;
         }
 
         return true;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void installLookAndFeels() {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("installing GTK+ Look & Feel");                                    // NOI18N
             }
             UIManager.installLookAndFeel("GTK+", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); // NOI18N
         } catch (final Exception e) {
             logger.warn("could not install GTK+ & Feel", e);                                    // NOI18N
         }
 
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("installing Plastic 3D Look & Feel"); // NOI18N
             }
 
             UIManager.installLookAndFeel("Plastic 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel"); // NOI18N
             final String heavyComps = System.getProperty("contains.heavyweight.comps");
             if ("true".equals(heavyComps)) {
                 com.jgoodies.looks.Options.setPopupDropShadowEnabled(false);
             }
         } catch (final Exception e) {
             logger.warn("could not install Plastic 3D Look & Feel", e);                                    // NOI18N
         }
     }
 }
