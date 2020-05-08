 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.jbands;
 
 import de.cismet.tools.gui.jbands.interfaces.BandAbsoluteHeightProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandMember;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class MinimumHeightBand extends SimpleBand implements BandAbsoluteHeightProvider {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MinimumHeightBand object.
      */
     public MinimumHeightBand() {
         this("");
     }
 
     /**
      * Creates a new MinimumHeightBand object.
      *
      * @param  title  DOCUMENT ME!
      */
     public MinimumHeightBand(final String title) {
         super(title);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public int getAbsoluteHeight() {
         int maxHeight = 0;
         for (final BandMember bm : super.members) {
             final int compH = bm.getBandMemberComponent().getPreferredSize().height;
             maxHeight = (maxHeight < compH) ? compH : maxHeight;
         }
        return maxHeight;
     }
 }
