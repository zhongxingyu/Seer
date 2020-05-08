 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.jbands;
 
 import de.cismet.tools.gui.jbands.interfaces.Band;
 import de.cismet.tools.gui.jbands.interfaces.BandAbsoluteHeightProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandMember;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class EmptyAbsoluteHeightedBand implements Band, BandAbsoluteHeightProvider {
 
     //~ Instance fields --------------------------------------------------------
 
     final int height;
    private boolean enabled = true;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new EmptyAbsoluteHeightedBand object.
      */
     public EmptyAbsoluteHeightedBand() {
         this(35);
     }
 
     /**
      * Creates a new EmptyAbsoluteHeightedBand object.
      *
      * @param  height  DOCUMENT ME!
      */
     public EmptyAbsoluteHeightedBand(final int height) {
         this.height = height;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * this method should not have any effect on the max value of the JBand, that contains it.
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public double getMax() {
         return 0;
     }
 
     @Override
     public BandMember getMember(final int i) {
         throw new UnsupportedOperationException("not allowed in EmptyBand");
     }
 
     /**
      * this method should not have any effect on the min value of the JBand, that contains it.
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public double getMin() {
         return Double.MAX_VALUE;
     }
 
     @Override
     public int getNumberOfMembers() {
         return 0;
     }
 
     @Override
     public int getAbsoluteHeight() {
         return height;
     }
 
     @Override
     public boolean isEnabled() {
        return enabled;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
     }
 }
