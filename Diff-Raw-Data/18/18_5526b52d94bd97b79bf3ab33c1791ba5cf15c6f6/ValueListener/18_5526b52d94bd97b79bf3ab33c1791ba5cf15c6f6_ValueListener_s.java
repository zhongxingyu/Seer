 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual.mappings.continuous;
 
 import cytoscape.visual.mappings.BoundaryRangeValues;
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.ui.ValueDisplayer;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 /**
  * Listens for User Value Selection.
  */
 public class ValueListener implements ItemListener {
     private ContinuousUI ui;
     private ContinuousMapping cm;
     private int index;
     private int offset;
 
     /**
      * Constructor.
      * @param ui ContinuousUI Object.
      * @param cm ContinuousMapping Object.
      */
     public ValueListener(ContinuousUI ui, ContinuousMapping cm,
             int index, int offset) {
         this.ui = ui;
         this.cm = cm;
         this.index = index;
         this.offset = offset;
     }
 
     /**
     *  The ValueDisplayer being reflected by this listener was changed.
     *  Make the appropriate changes to the underlying data in the mapper
     *  and notify interested listeners that state has changed.
      */
     public void itemStateChanged(ItemEvent e) {
         Object o = ((ValueDisplayer) e.getItemSelectable()).getValue();
         ContinuousMappingPoint point = cm.getPoint(index);
         BoundaryRangeValues range = point.getRange();
         if (offset == ContinuousUI.LESSER) {
             range.lesserValue = o;
         } else if (offset == ContinuousUI.EQUAL) {
             range.equalValue = o;
         } else if (offset == ContinuousUI.GREATER) {
             range.greaterValue = o;
         }
         ui.resetUI();
     }
 }
