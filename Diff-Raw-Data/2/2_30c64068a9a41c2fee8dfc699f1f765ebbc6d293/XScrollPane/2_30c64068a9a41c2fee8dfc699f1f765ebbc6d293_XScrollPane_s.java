 package org.wingx;
 
 import java.awt.event.*;
 import java.awt.*;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.List;
 
 import org.wings.*;
 
 
 /**
  * XScrollPane
  *
  * @author jdenzel
  */
 public class XScrollPane extends SScrollPane {
 
     private Vector<Integer> extents = new Vector<Integer>(Arrays.asList(
         Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(12), Integer.valueOf(14),
         Integer.valueOf(16), Integer.valueOf(18), Integer.valueOf(20), Integer.valueOf(22),
         Integer.valueOf(24), Integer.valueOf(26), Integer.valueOf(28), Integer.valueOf(30),
         Integer.valueOf(32)
     ));
 
     protected SComboBox extentCombo = new SComboBox(extents);
     protected final XPageScroller pageScroller = new XPageScroller();
     protected final SLabel extentComboLabel = new SLabel();
     protected final SLabel totalLabel = new SLabel();
     private STable tableComponent;
    private String visibleSectionLabel = "{0} .. {1} von {2}";
 
     public XScrollPane() {
         this(null);
     }
 
     public XScrollPane(STable tableComponent) {
         this(tableComponent, 10);
     }
 
     public XScrollPane(STable tableComponent, int verticalExtent) {
         setPreferredSize(SDimension.FULLAREA);
         setVerticalAlignment(SConstants.TOP_ALIGN);
 
         extentCombo.addActionListener(new ExtentComboActionListener());
 
         if (extents.indexOf(verticalExtent) == -1) {
             extents.add(verticalExtent);
             Collections.sort(extents);
         }
         extentCombo.setSelectedItem(Integer.valueOf(verticalExtent));
 
         pageScroller.add(totalLabel);
         pageScroller.add(extentComboLabel);
         pageScroller.add(extentCombo);
         pageScroller.add(new SLabel(" "), 1d);
 
         pageScroller.addAdjustmentListener(new PageAdjustmentListener());
         pageScroller.setExtent(verticalExtent);
         pageScroller.setHorizontalAlignment(SConstants.LEFT_ALIGN);
 
         setHorizontalScrollBar(pageScroller);
         setHorizontalScrollBarPolicy(SScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         setVerticalScrollBar(null);
         setVerticalScrollBarPolicy(SScrollPane.VERTICAL_SCROLLBAR_NEVER);
         setMode(SScrollPane.MODE_PAGING);
 
         setHorizontalExtent(20);
         setVerticalExtent(verticalExtent);
 
         if (tableComponent != null)
             setViewportView(tableComponent);
     }
 
     /**
      * Sets the horizontal scrollbar.
      *
      * @param sb         the scrollbar that controls the viewport's horizontal view position
      * @param constraint the constraint for the {@link LayoutManager} of this {@link SContainer}.
      *                   The {@link LayoutManager} is per default {@link SScrollPaneLayout}.
      */
     public void setHorizontalScrollBar(Adjustable sb, String constraint) {
         if (horizontalScrollBar != null) {
             if (horizontalScrollBar instanceof SAbstractAdjustable)
                 ((SAbstractAdjustable) horizontalScrollBar).setModel(new SDefaultBoundedRangeModel());
             else if (horizontalScrollBar instanceof XPageScroller)
                 ((XPageScroller) horizontalScrollBar).setModel(new SDefaultBoundedRangeModel());
 
             if (horizontalScrollBar instanceof SComponent)
                 remove((SComponent) horizontalScrollBar);
         }
 
         horizontalScrollBar = sb;
 
         if (horizontalScrollBar != null) {
             if (horizontalScrollBar instanceof SComponent)
                 addComponent((SComponent) horizontalScrollBar, constraint, getComponentCount());
 
             if (horizontalScrollBar instanceof SAbstractAdjustable) {
                 SAbstractAdjustable scrollbar = (SAbstractAdjustable) horizontalScrollBar;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL)
                     scrollbar.setModel(horizontalModel);
                 else
                     scrollbar.setModel(verticalModel);
             }
             else if (horizontalScrollBar instanceof XPageScroller) {
                 XPageScroller scrollbar = (XPageScroller) horizontalScrollBar;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL)
                     scrollbar.setModel(horizontalModel);
                 else
                     scrollbar.setModel(verticalModel);
             }
 
             adoptScrollBarVisibility(horizontalScrollBar, horizontalScrollBarPolicy);
         }
 
         reload();
     }
 
     /**
      * Sets the vertical scrollbar.
      *
      * @param sb         the scrollbar that controls the viewport's vertical view position
      * @param constraint the constraint for the {@link LayoutManager} of this {@link SContainer}.
      *                   The {@link LayoutManager} is per default {@link SScrollPaneLayout}.
      */
     public void setVerticalScrollBar(Adjustable sb, String constraint) {
         if (verticalScrollBar != null) {
             if (verticalScrollBar instanceof SAbstractAdjustable)
                 ((SAbstractAdjustable) verticalScrollBar).setModel(new SDefaultBoundedRangeModel());
             else if (verticalScrollBar instanceof XPageScroller)
                 ((XPageScroller) verticalScrollBar).setModel(new SDefaultBoundedRangeModel());
 
             if (verticalScrollBar instanceof SComponent)
                 remove((SComponent) verticalScrollBar);
         }
 
         verticalScrollBar = sb;
 
         if (verticalScrollBar != null) {
             if (verticalScrollBar instanceof SComponent)
                 addComponent((SComponent) verticalScrollBar, constraint, getComponentCount());
 
             if (verticalScrollBar instanceof SAbstractAdjustable) {
                 SAbstractAdjustable scrollbar = (SAbstractAdjustable) verticalScrollBar;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL)
                     scrollbar.setModel(horizontalModel);
                 else
                     scrollbar.setModel(verticalModel);
             }
             else if (verticalScrollBar instanceof XPageScroller) {
                 XPageScroller scrollbar = (XPageScroller) verticalScrollBar;
                 if (scrollbar.getOrientation() == SConstants.HORIZONTAL)
                     scrollbar.setModel(horizontalModel);
                 else
                     scrollbar.setModel(verticalModel);
             }
 
             adoptScrollBarVisibility(verticalScrollBar, verticalScrollBarPolicy);
         }
 
         reload();
     }
 
     public void setExtentLabel(String label) {
         extentComboLabel.setText(label);
     }
 
     public String getVisibleSectionLabel() {
         return visibleSectionLabel;
     }
 
     public void setVisibleSectionLabel(String visibleSectionLabel) {
         this.visibleSectionLabel = visibleSectionLabel;
     }
 
     /**
      * @inheritDoc
      */
     public void setViewportView(SComponent view) {
         if (view == null) {
             throw new NullPointerException();
         }
         if (getScrollable() != null) {
             throw new RuntimeException("error: this component is not reinitializable");
         }
         if (!(view instanceof XTable)) {
             throw new RuntimeException("the inner component must be of type XTable");
         }
         tableComponent = (XTable) view;
         tableComponent.setVerticalAlignment(SConstants.TOP_ALIGN);
 
         super.setViewportView(view);
         refresh();
     }
 
     public void setVerticalExtent(int ext) {
         super.setVerticalExtent(ext);
         extentCombo.setSelectedItem(Integer.valueOf(ext));
     }
 
     public void addAdjustmentListener(AdjustmentListener al) {
         pageScroller.addAdjustmentListener(al);
     }
 
     public void refresh() {
         scrollable.getViewportSize().height = verticalExtent;
         refreshTotalLabel();
     }
 
     private void refreshTotalLabel() {
         if (tableComponent == null)
             return;
 
         Rectangle viewportSize = tableComponent.getViewportSize();
         if (viewportSize == null) {
             totalLabel.setText(null);
             return;
         }
 
         int startRow = viewportSize.y;
         int endRow = tableComponent.getRowCount();
         endRow = Math.min(startRow + viewportSize.height, endRow);
 
         String text;
         if (endRow > 0) {
             int rowCount = tableComponent.getModel().getRowCount();
             text = MessageFormat.format(visibleSectionLabel, new Object[] {Integer.valueOf(startRow + 1), Integer.valueOf(endRow), Integer.valueOf(rowCount)});
 
             boolean moreRecordsAvailable = endRow >= 100;
             if (moreRecordsAvailable)
                 text += "  (+)";
         }
         else {
             text = null;
         }
 
         totalLabel.setText(text);
     }
 
     class PageAdjustmentListener implements AdjustmentListener {
         public void adjustmentValueChanged(AdjustmentEvent e) {
             refreshTotalLabel();
             //pageScroller.reload();
         }
     }
 
     class ExtentComboActionListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             if (tableComponent == null)
                 return;
             Integer extent = (Integer) extentCombo.getSelectedItem();
             assert extent != null;
             setVerticalExtent(extent.intValue());
         }
     }
 
     public XPageScroller getPageScroller() {
         return pageScroller;
     }
 }
