 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.jbands;
 
 import javax.swing.JComponent;
 
 import de.cismet.tools.gui.jbands.interfaces.Spot;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class SimpleSpot implements Spot {
 
     //~ Instance fields --------------------------------------------------------
 
     private double position;
     private SimpleSpotPanel bmc;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SimpleSpot object.
      */
     public SimpleSpot() {
         bmc = new SimpleSpotPanel();
     }
 
     /**
      * Creates a new SimpleSpot object.
      *
      * @param  position  DOCUMENT ME!
      */
     public SimpleSpot(final double position) {
         this();
         this.position = position;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public double getPosition() {
         return position;
     }
 
     @Override
     public double getMax() {
         return position;
     }
 
     @Override
     public double getMin() {
         return position;
     }
 
     @Override
     public JComponent getBandMemberComponent() {
         return bmc;
     }
 }
