 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.evaluator;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
import org.apache.log4j.Logger;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.CombinedDomainXYPlot;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
 import org.jfree.chart.title.LegendTitle;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.RectangleInsets;
 import org.jfree.util.UnitType;
 
 /**
  * A demonstration application showing a time series chart where you can
  * dynamically add (random) data by clicking on a button.
  */
 public class GraphicalEvaluatorDisplay extends ApplicationFrame implements StatisticsListener
 {
  private static final Logger logger = Logger.getLogger(GraphicalEvaluatorDisplay.class); 
   private static final long serialVersionUID =  1L;
 
   static class DemoPanel extends JPanel implements ActionListener
   {
     private static final long serialVersionUID = 1L;
 
     /** The number of subplots. */
     public static final int        SUBPLOT_COUNT = 2;
 
     /** The datasets. */
     private TimeSeriesCollection[] datasets;
 
     /** The most recent value added to series 1. */
     private double[]               lastValue     = new double[SUBPLOT_COUNT];
 
     public class GetEvaluatorData implements Runnable
     {
       public DemoPanel demo;
       List statisticsList;
       public GetEvaluatorData(ArrayList statisticsList)
       {
         this.statisticsList = statisticsList;
       }
 
       public void run()
       {
         do
         {
           synchronized (statisticsList)
           {
             try
             {
               if (!statisticsList.isEmpty())
               {
                 Statistics stats = (Statistics)statisticsList.remove(0);
                 int interval = (int)stats.getInterval();
                 while (interval > 0)
                 {
                  try
                  {
                    demo.addRequestRate(stats.getQueries() / stats.getInterval());
                    demo.addResponseTime((double)stats.getResponseTime() / stats.getQueries());
                  }
                  catch (Exception e)
                  {
                    logger.warn("Error while adding data to graph", e);
                  }
                   if (--interval > 0)
                     Thread.sleep(1000);
                 }
               }
             
               statisticsList.wait(1000);
             }
             catch (InterruptedException e)
             {
               break;
             }
           }
           
         }
         while (true);
       }
     }
 
     /**
      * Creates a new self-contained demo panel.
      */
     public DemoPanel(ArrayList statisticsList)
     {
       super(new BorderLayout());
       CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Time"));
       this.datasets = new TimeSeriesCollection[SUBPLOT_COUNT];
       /*
        * for (int i = 0; i < SUBPLOT_COUNT; i++) { this.lastValue[i] = 100.0;
        * TimeSeries series = new TimeSeries( "Requests/sec", Millisecond.class );
        * this.datasets[i] = new TimeSeriesCollection(series); NumberAxis
        * rangeAxis = new NumberAxis("Y" + i);
        * rangeAxis.setAutoRangeIncludesZero(false); XYPlot subplot = new XYPlot(
        * this.datasets[i], null, rangeAxis, new StandardXYItemRenderer() );
        * subplot.setBackgroundPaint(Color.lightGray);
        * subplot.setDomainGridlinePaint(Color.white);
        * subplot.setRangeGridlinePaint(Color.white); plot.add(subplot); }
        */
       this.lastValue[0] = 0.0;
       TimeSeries series1 = new TimeSeries("Requests/sec", Millisecond.class);
       this.datasets[0] = new TimeSeriesCollection(series1);
       NumberAxis rangeAxis1 = new NumberAxis("Requests");
       rangeAxis1.setAutoRangeIncludesZero(false);
       XYPlot subplot1 = new XYPlot(this.datasets[0], null, rangeAxis1,
           new StandardXYItemRenderer());
       subplot1.setBackgroundPaint(Color.lightGray);
       subplot1.setDomainGridlinePaint(Color.white);
       subplot1.setRangeGridlinePaint(Color.white);
       plot.add(subplot1);
 
       this.lastValue[1] = 0.0;
       TimeSeries series2 = new TimeSeries("Response Time", Millisecond.class);
       this.datasets[1] = new TimeSeriesCollection(series2);
       NumberAxis rangeAxis2 = new NumberAxis("Milliseconds");
       rangeAxis2.setAutoRangeIncludesZero(false);
       XYPlot subplot2 = new XYPlot(this.datasets[1], null, rangeAxis2,
           new StandardXYItemRenderer());
       subplot2.setBackgroundPaint(Color.lightGray);
       subplot2.setDomainGridlinePaint(Color.white);
       subplot2.setRangeGridlinePaint(Color.white);
       plot.add(subplot2);
 
       JFreeChart chart = new JFreeChart("Performance Statistics", plot);
       LegendTitle legend = (LegendTitle) chart.getSubtitle(0);
       legend.setPosition(RectangleEdge.RIGHT);
       legend.setMargin(new RectangleInsets(UnitType.ABSOLUTE, 0, 4, 0, 4));
       chart.setBorderPaint(Color.black);
       chart.setBorderVisible(true);
       chart.setBackgroundPaint(Color.white);
 
       plot.setBackgroundPaint(Color.lightGray);
       plot.setDomainGridlinePaint(Color.white);
       plot.setRangeGridlinePaint(Color.white);
       plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));
       ValueAxis axis = plot.getDomainAxis();
       axis.setFixedAutoRange(1000.0 * 60 * 10); // 10 minutes
 
       ChartPanel chartPanel = new ChartPanel(chart);
       add(chartPanel);
       /*
        * JPanel buttonPanel = new JPanel(new FlowLayout()); for (int i = 0; i <
        * SUBPLOT_COUNT; i++) { final JButton button = new JButton("Series " +
        * i); button.setActionCommand("ADD_DATA_" + i);
        * button.addActionListener(this); buttonPanel.add(button); } JButton
        * buttonAll = new JButton("ALL"); buttonAll.setActionCommand("ADD_ALL");
        * buttonAll.addActionListener(this); buttonPanel.add(buttonAll);
        * add(buttonPanel, BorderLayout.SOUTH);
        */
       chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
       chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 
       System.out.println("starting data acquisition");
 
       GetEvaluatorData dataSource = new GetEvaluatorData(statisticsList);
       dataSource.demo = this;
 
       Thread dataThread = new Thread(dataSource, "Data acquisition");
       dataThread.start();
 
       System.out.println("after starting data acquisition");
     }
 
     /**
      * Handles a click on the button by adding new (random) data.
      * 
      * @param e the action event.
      */
     public void addRequestRate(double value)
     {
       this.lastValue[0] = value;
       this.datasets[0].getSeries(0).add(new Millisecond(), this.lastValue[0]);
     }
 
     public void addResponseTime(double value)
     {
       this.lastValue[1] = value;
       this.datasets[1].getSeries(0).add(new Millisecond(), this.lastValue[1]);
     }
 
     public void actionPerformed(ActionEvent e)
     {
 
       if (e.getActionCommand().endsWith(String.valueOf(0)))
       {
         Millisecond item = new Millisecond();
         System.out.println("Item = " + item.toString());
         this.lastValue[0] = this.lastValue[0] * (0.90 + 0.2 * Math.random());
         this.datasets[0].getSeries(0).add(new Millisecond(), this.lastValue[0]);
       }
 
       if (e.getActionCommand().endsWith(String.valueOf(1)))
       {
         Millisecond now = new Millisecond();
         System.out.println("Now = " + now.toString());
         this.lastValue[1] = this.lastValue[1] * (0.90 + 0.2 * Math.random());
         this.datasets[1].getSeries(0).add(new Millisecond(), this.lastValue[1]);
       }
 
       if (e.getActionCommand().equals("ADD_ALL"))
       {
         Millisecond now = new Millisecond();
         System.out.println("Now = " + now.toString());
         for (int i = 0; i < SUBPLOT_COUNT; i++)
         {
           this.lastValue[i] = this.lastValue[i] * (0.90 + 0.2 * Math.random());
           this.datasets[i].getSeries(0).add(new Millisecond(),
               this.lastValue[i]);
         }
       }
 
     }
   }
 
   private ArrayList<Statistics> statisticsList = new ArrayList<Statistics>();
 
   /**
    * Constructs a new demonstration application.
    * 
    * @param title the frame title.
    */
   public GraphicalEvaluatorDisplay(String title)
   {
     super(title);
     setContentPane(new DemoPanel(statisticsList));
   }
 
   public void report(Statistics statistics)
   {
     synchronized (statisticsList)
     {
       statisticsList.add(statistics);
       statisticsList.notifyAll();
     }
   }
 }
