 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 srichter
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.custom.objectrenderer.utils.alkis.print;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JButton;
 
 import de.cismet.cids.custom.objectrenderer.utils.alkis.AlkisUtil;
 
 import de.cismet.cismap.commons.gui.ToolbarComponentDescription;
 import de.cismet.cismap.commons.gui.ToolbarComponentsProvider;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 /**
  * ToolbarComponentsProvider that delivers a Button for Alkis Print to integrate
  * into the Navigator Toolbar.
  *
  * @author   srichter
  * @version  $Revision$, $Date$
  */
 @org.openide.util.lookup.ServiceProvider(service = ToolbarComponentsProvider.class)
 public class AlkisToobarPluginComponentProvider implements ToolbarComponentsProvider {
 
     //~ Instance fields --------------------------------------------------------
 
     private final List<ToolbarComponentDescription> toolbarComponents;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new AlkisToobarPluginComponentProvider object.
      */
     public AlkisToobarPluginComponentProvider() {
         final List<ToolbarComponentDescription> preparationList = TypeSafeCollections.newArrayList();
         final ToolbarComponentDescription description = new ToolbarComponentDescription(
                 "tlbMain",
                 new AlkisPrintJButton(),
                 ToolbarPositionHint.AFTER,
                "print");
         preparationList.add(description);
         this.toolbarComponents = Collections.unmodifiableList(preparationList);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public List<ToolbarComponentDescription> getToolbarComponents() {
         if (AlkisUtil.validateUserHasAlkisPrintAccess()) {
             return toolbarComponents;
         } else {
             return Collections.emptyList();
         }
     }
 
     @Override
     public String getPluginName() {
         return "ALKIS";
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 final class AlkisPrintJButton extends JButton {
 
     //~ Static fields/initializers ---------------------------------------------
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
             AlkisToobarPluginComponentProvider.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final AlkisPrintingSettingsWidget printWidget;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new AlkisPrintJButton object.
      *
      * @throws  RuntimeException  DOCUMENT ME!
      */
     public AlkisPrintJButton() {
         try {
             this.printWidget = new AlkisPrintingSettingsWidget(false, CismapBroker.getInstance().getMappingComponent());
         } catch (Exception ex) {
             log.fatal(ex, ex);
             throw new RuntimeException(ex);
         }
 //        setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/frameprint.png"))); // NOI18N
        setText("Alkis-Drucken");
         setToolTipText("Alkis Drucken");
         setName("alkis_print");
         setBorderPainted(false);
         setFocusable(false);
         setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     printWidget.setVisible(true);
                 }
             });
     }
 }
