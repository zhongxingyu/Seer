 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.jbands;
 
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import de.cismet.tools.gui.jbands.interfaces.Band;
 import de.cismet.tools.gui.jbands.interfaces.BandMember;
 import de.cismet.tools.gui.jbands.interfaces.BandPrefixProvider;
 import de.cismet.tools.gui.jbands.interfaces.BandWeightProvider;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
public class SimpleBand implements Band, BandPrefixProvider, BandWeightProvider {
 
     //~ Instance fields --------------------------------------------------------
 
     protected ArrayList<BandMember> members = new ArrayList<BandMember>();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     JLabel prefix = new JLabel();
     private Double min = null;
     private Double max = null;
     private float bandWeight;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SimpleBand object.
      */
     public SimpleBand() {
         prefix.setOpaque(false);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public BandMember getMember(final int i) {
         assert (i > 0);
         assert (i < members.size());
         return members.get(i);
     }
 
     @Override
     public int getNumberOfMembers() {
         return members.size();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  m  DOCUMENT ME!
      */
     public void addMember(final BandMember m) {
         assert (m != null);
         members.add(m);
         min = null;
         max = null;
     }
 
     @Override
     public double getMax() {
         if (max == null) {
             max = 0.0;
             for (final BandMember bm : members) {
                 max = (max < bm.getMax()) ? bm.getMax() : max;
             }
         }
         return max;
     }
 
     @Override
     public double getMin() {
         if (min == null) {
             min = 0.0;
             for (final BandMember bm : members) {
                 min = (min > bm.getMin()) ? bm.getMin() : min;
             }
         }
         return min;
     }
 
     @Override
     public JComponent getPrefixComponent() {
         prefix.setText("SimpleBand: " + getMin() + " --> " + getMax());
         return prefix;
     }
 
    @Override
    public float getBandWeight() {
        return bandWeight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bandweight  DOCUMENT ME!
     */
    public void setBandWeight(final float bandweight) {
        this.bandWeight = bandweight;
    }
 }
