 /*
  * @(#) $RCSfile: HistogramDisplay.java,v $ $Revision: 1.2 $ $Date: 2004/09/21 16:49:43 $ $Name: TableView1_3_2 $
  *
  * Center for Computational Genomics and Bioinformatics
  * Academic Health Center, University of Minnesota
  * Copyright (c) 2000-2002. The Regents of the University of Minnesota
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * see: http://www.gnu.org/copyleft/gpl.html
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  */
 package edu.umn.genomics.table;
 
 import edu.umn.genomics.graph.*;
 import edu.umn.genomics.graph.swing.SimpleGraph;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JPanel;
 
 public class HistogramDisplay extends JPanel {
 
     SetOperator setOperator = new DefaultSetOperator();
     int prevSetOp = -1;
     HistogramModel hgm;
     int barWidth = 1;
     SimpleGraph graph;
     // Selection 
     boolean selecting = false;
     Point start = null;
     Point current = null;
     // Histogram bars
     //GraphSegments gCntLine;
     //GraphSegments gSelLine;
     GraphRects gCntRect;
     GraphRects gSelRect;
     Axis calcAxes[] = new Axis[2];
     int[][] binRectPnts;
     int[][] selRectPnts;
     AxisLabeler cntLabeler = new AxisLabeler() {
 
         public String getLabel(double value) {
             if (Math.abs(value - Math.floor(value)) < .000000001) {
                 return "" + (int) value;
             }
             return "";
         }
     };
     AxisLabeler binLabeler = new AxisLabeler() {
 
         public String getLabel(double value) {
             if (Math.abs(value - Math.floor(value)) < .000000001) {
                 int idx = (int) value;
                 try {
                     int[] indices = MultiDimIntArray.getIndices(idx, hgm.getDimensions());
                     StringBuffer sb = new StringBuffer();
 
                     for (int i = 0; i < hgm.getModelCount(); i++) {
                         BinLabeler bl = hgm.getBinModel(i).getBinLabeler();
                         sb.append((i > 0 ? "_" : "") + (bl != null ? bl.getLabel(indices[i]) : "" + i));
                     }
                     return sb.toString();
                 } catch (Exception ex) {
                 }
             }
             return "";
         }
     };
     AbstractDataModel gCntRectModel = new AbstractDataModel() {
 
         public int[][] getPoints(int x, int y, Axis axes[], int points[][]) {
             return getPointArray(x, y, axes, false);
         }
 
         public double[] getYValues(int xi) {
             return null; // Should this be implemented?
         }
     };
     AbstractDataModel sCntRectModel = new AbstractDataModel() {
 
         public int[][] getPoints(int x, int y, Axis axes[], int points[][]) {
             return getPointArray(x, y, axes, true);
         }
 
         public double[] getYValues(int xi) {
             return null; // Should this be implemented?
         }
     };
     HistogramListener hdl = new HistogramListener() {
 
         public void histogramChanged(HistogramEvent e) {
             if (!e.isAdjusting()) {
                 setAxis();
                 binRectPnts = null;
                 selRectPnts = null;
                 // Signal GraphItems that the data has changed
                 gCntRect.setData(gCntRectModel);
                 gSelRect.setData(sCntRectModel);
                 repaint();
             }
         }
     };
     private MouseAdapter ma = new MouseAdapter() {
 
         public void mousePressed(MouseEvent e) {
             start = e.getPoint();
             current = e.getPoint();
             selecting = true;
             prevSetOp = getSetOperator().getSetOperator();
             getSetOperator().setFromInputEventMask(e.getModifiers());
             repaint();
         }
 
         public void mouseReleased(MouseEvent e) {
             selecting = false;
             current = e.getPoint();
             select();
             getSetOperator().setSetOperator(prevSetOp);
             repaint();
         }
 
         public void mouseClicked(MouseEvent e) {
             start = e.getPoint();
             current = e.getPoint();
             prevSetOp = getSetOperator().getSetOperator();
             getSetOperator().setFromInputEventMask(e.getModifiers());
             select(current);
             getSetOperator().setSetOperator(prevSetOp);
             repaint();
         }
 
         public void mouseExited(MouseEvent e) {
         }
     };
 
     public void select() {
         int x = Math.min(start.x, current.x);
         int y = Math.min(start.y, current.y);
         int w = Math.abs(start.x - current.x);
         int h = Math.abs(start.y - current.y);
         Rectangle r = new Rectangle(x, y, w, h);
         select(r);
     }
 
     public void select(Rectangle r) {
         select(gCntRect.getIndicesAt(r, calcAxes[0], calcAxes[1]));
     }
 
     public void select(Point p) {
         select(gCntRect.getIndicesAt(p, calcAxes[0], calcAxes[1]));
     }
 
     public void select(int[] indices) {
         if (indices != null) {
             hgm.selectBins(indices, hgm.getListSelectionModel(), getSetOperator().getSetOperator());
         } else {
         }
     }
 
     public void setSetOperator(SetOperator setOperator) {
         this.setOperator = setOperator;
     }
 
     public SetOperator getSetOperator() {
         return setOperator;
     }
 
     public HistogramDisplay(HistogramModel model, SetOperator setOperator) {
         this(model);
         setSetOperator(setOperator);
     }
 
     public HistogramDisplay(HistogramModel model) {
         setLayout(new BorderLayout());
         this.hgm = model;
         hgm.addHistogramListener(hdl);
         graph = new SimpleGraph();
         graph.getGraphDisplay().setOpaque(true);
         graph.getGraphDisplay().setBackground(Color.white);
         graph.getGraphDisplay().addMouseListener(ma);
         // graph.getGraphDisplay().setGridColor(new Color(220,220,220));
         // graph.showGrid(true);
         setAxis();
         graph.getAxisDisplay(BorderLayout.SOUTH).setZoomable(true);
         graph.getAxisDisplay(BorderLayout.SOUTH).setAxisLabeler(binLabeler);
         graph.getAxisDisplay(BorderLayout.WEST).setZoomable(true);
         graph.getAxisDisplay(BorderLayout.WEST).setAxisLabeler(cntLabeler);
         gCntRect = new GraphRects();
         gCntRect.setData(gCntRectModel);
         gCntRect.setColor(Color.blue);
         graph.addGraphItem(gCntRect);
         gSelRect = new GraphRects();
         gSelRect.setData(sCntRectModel);
         gSelRect.setColor(Color.cyan);
         graph.addGraphItem(gSelRect);
         add(graph);
     }
 
     private int getDisplayMax(int max) {
         if (max == 0) {
             return 1;
         }
         int pow = (int) Math.floor(Math.log(Math.abs(max)) / Math.log(10.));
         int n = (int) Math.pow(10., pow);
         return (max / n + 1) * n;
     }
 
     private void setAxis() {
         Axis xAxis = graph.getAxisDisplay(BorderLayout.SOUTH).getAxis();
         Axis yAxis = graph.getAxisDisplay(BorderLayout.WEST).getAxis();
         xAxis.setMin(-.2);
         xAxis.setMax(hgm.getBinCount());
         yAxis.setMin(0);
         int max = hgm.getMaxBinSize();
         yAxis.setMax(getDisplayMax(max));
         ((LinearAxis) xAxis).setTickIncrement(1.);
     }
 
     public synchronized int[][] getPointArray(int x, int y, Axis axes[], boolean selectCnt) {
         if (binRectPnts == null || binRectPnts[0] == null
                 || calcAxes[0] == null || calcAxes[1] == null
                 || !calcAxes[0].equals(axes[0]) || !calcAxes[1].equals(axes[1])) {
             try {
                 calcPoints(x, y, axes);
             } catch (Exception ex) {
                 ExceptionHandler.popupException(""+ex);
                 repaint();
             }
         }
         return selectCnt ? selRectPnts : binRectPnts;
     }
 
     public synchronized void calcPoints(int x, int y, Axis axes[]) {
         int w = axes[0].getSize();
         int h = axes[1].getSize();
         int yb = y + h;
         double xoffset = .8;
         int max = hgm.getMaxBinSize();
         int cnt = hgm.getBinCount();
         int nMod = hgm.getModelCount();
         int[] dim = hgm.getDimensions();
         int[] indices = new int[dim.length];
         int[] binX = new int[cnt * 2];
         int[] binY = new int[binX.length];
         int[] selY = new int[binX.length];
         double[] ticks = new double[cnt];
         for (int i = 0, r = 0; i < cnt; i++, r += 2) {
             int binCnt = hgm.getBinCount(indices);
             int selCnt = hgm.getBinSelectCount(indices);
             int xp = getOffsetX(indices, dim);
             binX[r] = x + axes[0].getIntPosition(xp);
             binX[r + 1] = x + axes[0].getIntPosition(xp + xoffset);
             // Full Bin
             binY[r] = yb - axes[1].getIntPosition(0);
             binY[r + 1] = yb - axes[1].getIntPosition(binCnt);
             // Selected Bin
             selY[r] = yb - axes[1].getIntPosition(0);
             selY[r + 1] = yb - axes[1].getIntPosition(selCnt);
             MultiDimIntArray.incrIndex(indices, dim);
             ticks[i] = i;
         }
         binRectPnts = new int[2][];
         binRectPnts[0] = binX;
         binRectPnts[1] = binY;
         selRectPnts = new int[2][];
         selRectPnts[0] = binX;
         selRectPnts[1] = selY;
         calcAxes[0] = axes[0];
         calcAxes[1] = axes[1];
         // ((LinearAxis)axes[0]).setTickIncrement(1.);
         axes[0].setTicks(ticks);
     }
 
     private int getOffsetX(int[] indices, int[] dim) {
         int width = barWidth;
         int xpos = 0;
         for (int i = indices.length - 1; i >= 0; i--) {
             xpos += indices[i] * width;
             width *= dim[i];
         }
         return xpos;
     }
 }
