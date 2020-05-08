 
 // compile: javac -cp "jfreechart-1.0.13/lib/*" PVGraph.java
 
 // run: java -cp ".:jfreechart-1.0.13/*:/usr/share/java/mysql-connector-java.jar" PVGraph
 
 import java.sql.*;
 import java.util.*;
 import java.io.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.text.SimpleDateFormat;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.category.AreaRenderer;
 import org.jfree.chart.renderer.category.CategoryItemRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.Month;
 import org.jfree.data.time.Minute;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RectangleInsets;
 import org.jfree.ui.RefineryUtilities;
 
 public class PVGraph extends ApplicationFrame {
     
     static Properties props;
     
     static LinkedList<PVGraph> graphs = new LinkedList<PVGraph>();
     
     private Connection conn;
     private Calendar date; 
     
     private class DayData {
         String inverter;
         String serial;
         double startTotalPower;
         double endTotalPower;
         java.util.List<Timestamp> times = new java.util.ArrayList<Timestamp>(12 * 24);
         java.util.List<Integer> powers = new java.util.ArrayList<Integer>(12 * 24);
     };
 
     private class PeriodData {
         String inverter;
         String serial;
         double startTotalPower;
         double endTotalPower;
        double powers[] = new double[366];
         int numPowers;
     };
     
     public PVGraph(Connection conn) {
         super("PV Power");
         this.conn = conn;
         date = new GregorianCalendar();
         synchronized(graphs) {
             graphs.add(this);
         }
         JTabbedPane tabPane = new JTabbedPane();
         tabPane.addTab("Day", makeDayPanel());
         tabPane.addTab("Month", makeMonthPanel());
         tabPane.addTab("Year", makeYearPanel());
         setContentPane(tabPane);
         pack();
         setVisible(true);
     }
     
     public JPanel makeDayPanel() {
         
         JPanel dayPanel = new JPanel();
         dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
 
         final ChartPanel dayChartPanel = (ChartPanel)createDayChartPanel();
         dayChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
         dayPanel.add(dayChartPanel);
         
         JButton dayDecButton = new JButton("-");
         dayDecButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.DAY_OF_MONTH, -1);
                     dayChartPanel.setChart(createDayChart());
                 }
         });
         JButton dayIncButton = new JButton("+");
         dayIncButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.DAY_OF_MONTH, 1);
                     dayChartPanel.setChart(createDayChart());
                 }
         });
         
         JButton monthDecButton = new JButton("-");
         monthDecButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.MONTH, -1);
                     dayChartPanel.setChart(createDayChart());
                 }
         });
         JButton monthIncButton = new JButton("+");
         monthIncButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.MONTH, 1);
                     dayChartPanel.setChart(createDayChart());
                 }
         });
         
         JButton yearDecButton = new JButton("-");
         yearDecButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.YEAR, -1);
                     dayChartPanel.setChart(createDayChart());
                 }
         });
         JButton yearIncButton = new JButton("+");
         yearIncButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.YEAR, 1);
                     dayChartPanel.setChart(createDayChart());
                 }
         });
         
         JButton newGraphButton = new JButton("New Graph");
         newGraphButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     new PVGraph(PVGraph.this.conn);
                 }
         });
         
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.setBorder(new EtchedBorder());
         dayPanel.add(buttonsPanel);
         
         JPanel dayButtonsPanel = new JPanel();
         dayButtonsPanel.setBorder(new EtchedBorder());
         dayButtonsPanel.add(new JLabel("Day"));
         dayButtonsPanel.add(dayDecButton);
         dayButtonsPanel.add(dayIncButton);
         buttonsPanel.add(dayButtonsPanel);
         
         JPanel monthButtonsPanel = new JPanel();
         monthButtonsPanel.setBorder(new EtchedBorder());
         monthButtonsPanel.add(new JLabel("Month"));
         monthButtonsPanel.add(monthDecButton);
         monthButtonsPanel.add(monthIncButton);
         buttonsPanel.add(monthButtonsPanel);
 
         JPanel yearButtonsPanel = new JPanel();
         yearButtonsPanel.setBorder(new EtchedBorder());
         yearButtonsPanel.add(new JLabel("Year"));
         yearButtonsPanel.add(yearDecButton);
         yearButtonsPanel.add(yearIncButton);
         buttonsPanel.add(yearButtonsPanel);
 
         buttonsPanel.add(newGraphButton);
         
         return dayPanel;
     }
     
     public JPanel makeMonthPanel() {
         
         JPanel monthPanel = new JPanel();
         monthPanel.setLayout(new BoxLayout(monthPanel, BoxLayout.Y_AXIS));
 
         final ChartPanel monthChartPanel = (ChartPanel)createMonthChartPanel();
         monthChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
         monthPanel.add(monthChartPanel);
                 
         JButton monthDecButton = new JButton("-");
         monthDecButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.MONTH, -1);
                     monthChartPanel.setChart(createMonthChart());
                 }
         });
         JButton monthIncButton = new JButton("+");
         monthIncButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.MONTH, 1);
                     monthChartPanel.setChart(createMonthChart());
                 }
         });
         
         JButton yearDecButton = new JButton("-");
         yearDecButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.YEAR, -1);
                     monthChartPanel.setChart(createMonthChart());
                 }
         });
         JButton yearIncButton = new JButton("+");
         yearIncButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.YEAR, 1);
                     monthChartPanel.setChart(createMonthChart());
                 }
         });
         
         JButton newGraphButton = new JButton("New Graph");
         newGraphButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     new PVGraph(PVGraph.this.conn);
                 }
         });
         
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.setBorder(new EtchedBorder());
         monthPanel.add(buttonsPanel);
 
         JPanel monthButtonsPanel = new JPanel();
         monthButtonsPanel.setBorder(new EtchedBorder());
         monthButtonsPanel.add(new JLabel("Month"));
         monthButtonsPanel.add(monthDecButton);
         monthButtonsPanel.add(monthIncButton);
         buttonsPanel.add(monthButtonsPanel);
 
         JPanel yearButtonsPanel = new JPanel();
         yearButtonsPanel.setBorder(new EtchedBorder());
         yearButtonsPanel.add(new JLabel("Year"));
         yearButtonsPanel.add(yearDecButton);
         yearButtonsPanel.add(yearIncButton);
         buttonsPanel.add(yearButtonsPanel);
 
         buttonsPanel.add(newGraphButton);
         
         return monthPanel;
     }
 
     public JPanel makeYearPanel() {
                 
         JPanel yearPanel = new JPanel();
         yearPanel.setLayout(new BoxLayout(yearPanel, BoxLayout.Y_AXIS));
 
         final ChartPanel yearChartPanel = (ChartPanel)createYearChartPanel(false);
         yearChartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
         yearPanel.add(yearChartPanel);
 
         final JRadioButton detailedButton = new JRadioButton("Detailed");
         detailedButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     yearChartPanel.setChart(createYearChart(detailedButton.isSelected()));
                 }
         });
 
         JButton yearDecButton = new JButton("-");
         yearDecButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.YEAR, -1);
                     yearChartPanel.setChart(createYearChart(detailedButton.isSelected()));
                 }
         });
         JButton yearIncButton = new JButton("+");
         yearIncButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     date.add(Calendar.YEAR, 1);
                     yearChartPanel.setChart(createYearChart(detailedButton.isSelected()));
                 }
         });
         
         JButton newGraphButton = new JButton("New Graph");
         newGraphButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     new PVGraph(PVGraph.this.conn);
                 }
         });
         
         JPanel buttonsPanel = new JPanel();
         buttonsPanel.setBorder(new EtchedBorder());
         yearPanel.add(buttonsPanel);
 
         buttonsPanel.add(detailedButton);
         JPanel yearButtonsPanel = new JPanel();
         yearButtonsPanel.setBorder(new EtchedBorder());
         yearButtonsPanel.add(new JLabel("Year"));
         yearButtonsPanel.add(yearDecButton);
         yearButtonsPanel.add(yearIncButton);
         buttonsPanel.add(yearButtonsPanel);
 
         buttonsPanel.add(newGraphButton);        
         return yearPanel;
     }
     
     public JPanel createDayChartPanel() {
         JFreeChart chart = createDayChart();
         ChartPanel panel = new ChartPanel(chart);
         panel.setFillZoomRectangle(true);
         panel.setMouseWheelEnabled(true);
         return panel;
     }
 
     public JPanel createMonthChartPanel() {
         JFreeChart chart = createMonthChart();
         ChartPanel panel = new ChartPanel(chart);
         panel.setFillZoomRectangle(true);
         panel.setMouseWheelEnabled(true);
         return panel;
     }
 
     public JPanel createYearChartPanel(boolean detailed) {
         JFreeChart chart = createYearChart(detailed);
         ChartPanel panel = new ChartPanel(chart);
         panel.setFillZoomRectangle(true);
         panel.setMouseWheelEnabled(true);
         return panel;
     }
     
     private JFreeChart createDayChart() {
         
         int year = date.get(Calendar.YEAR);
         int month = date.get(Calendar.MONTH) + 1;
         int day = date.get(Calendar.DAY_OF_MONTH);
         
         java.util.List<DayData> dayData = getDayData(year, month, day);
 
         TimeSeriesCollection dataset = new TimeSeriesCollection();
         double totalDayPower = 0;
         
         for(DayData dd : dayData) {
             TimeSeries s = new TimeSeries(dd.inverter + (dayData.size() > 1? ("-" + dd.serial) : ""));
             for(int i = 0; i < dd.times.size(); ++i)
                 s.add(new Minute(dd.times.get(i)), dd.powers.get(i));
             dataset.addSeries(s);
             totalDayPower += dd.endTotalPower - dd.startTotalPower;
         }
         
         String dayPower = totalDayPower < 1.0? String.format("%d W", (int)(totalDayPower * 1000)) : String.format("%.3f KW", totalDayPower);
         JFreeChart chart = ChartFactory.createXYAreaChart(
             day + " / " + month + " / " + year + "      " + dayPower, // title
             "Time",     // x-axis label
             "Watts",    // y-axis label
             dataset,    // data
             PlotOrientation.VERTICAL,
             true,       // create legend?
             true,       // generate tooltips?
             false       // generate URLs?
             );
         
         chart.setBackgroundPaint(Color.white);
         
         XYPlot plot = (XYPlot) chart.getPlot();
         plot.setBackgroundPaint(Color.lightGray);
         plot.setDomainGridlinePaint(Color.white);
         plot.setRangeGridlinePaint(Color.white);
         plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
         /*
         plot.setDomainCrosshairVisible(true);
         plot.setRangeCrosshairVisible(true);
         */
         plot.setDomainPannable(true);
         plot.setRangePannable(true);
         double maxPower = Double.parseDouble(props.getProperty("maxpower.day", "0"));
         if(maxPower > 0) {
             ValueAxis powerAxis = plot.getRangeAxis();
             powerAxis.setAutoRange(false);
             powerAxis.setLowerBound(0.0);
             powerAxis.setUpperBound(maxPower * 1000);
         }
         
         XYItemRenderer r = plot.getRenderer();
         r.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
         for(int i = 0; i < dayData.size(); ++i) {
             DayData dd = dayData.get(i);
             String colour = props.getProperty("plotcolour." + dd.serial, props.getProperty("plotcolour", null));
             if(colour != null)
                 r.setSeriesPaint(i, new Color(Integer.decode(colour)));
         }
         
         DateAxis axis = new DateAxis();
         axis.setLabel(plot.getDomainAxis().getLabel());
         axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
         plot.setDomainAxis(axis);
 
         return chart;   
     }
 
     private JFreeChart createMonthChart() {
         
         int year = date.get(Calendar.YEAR);
         int month = date.get(Calendar.MONTH) + 1;
         
         java.util.List<PeriodData> periodData = getMonthData(year, month);
 
         DefaultCategoryDataset dataset = new DefaultCategoryDataset();
         double totalPeriodPower = 0;
         
         for(PeriodData pd : periodData) {
             String series = pd.inverter + (periodData.size() > 1? ("-" + pd.serial) : "");
             double lastPower = pd.startTotalPower;
             for(int i = 0; i < pd.numPowers; ++i) {
                 if(pd.powers[i] != 0) {
                     dataset.addValue(pd.powers[i] - lastPower, series, "" + (i + 1));
                     lastPower = pd.powers[i];
                 }
                 else
                     dataset.addValue(0, series, "" + (i + 1));
             }
             totalPeriodPower += pd.endTotalPower - pd.startTotalPower;
         }
         
         String periodPower = totalPeriodPower < 1.0? String.format("%d W", (int)(totalPeriodPower * 1000)) : String.format("%.3f KW", totalPeriodPower);
         
         JFreeChart chart = ChartFactory.createBarChart(
             month + " / " + year + "      " + periodPower, // title
             "Day",      // domain label
             "KW",       // range label
             dataset,    // data
             PlotOrientation.VERTICAL,
             true,       // create legend?
             true,       // generate tooltips?
             false       // generate URLs?
             );
         
         chart.setBackgroundPaint(Color.white);
         
         CategoryPlot plot = (CategoryPlot)chart.getPlot();
         plot.setBackgroundPaint(Color.lightGray);
         plot.setDomainGridlinePaint(Color.white);
         plot.setRangeGridlinePaint(Color.white);
         plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
         plot.setDomainCrosshairVisible(true);
         plot.setRangeCrosshairVisible(true);
         double maxPower = Double.parseDouble(props.getProperty("maxpower.month", "0"));
         if(maxPower > 0) {
             ValueAxis powerAxis = plot.getRangeAxis();
             powerAxis.setAutoRange(false);
             powerAxis.setLowerBound(0.0);
             powerAxis.setUpperBound(maxPower);
         }
         
         CategoryItemRenderer r = plot.getRenderer();
         for(int i = 0; i < periodData.size(); ++i) {
             PeriodData pd = periodData.get(i);
             String colour = props.getProperty("plotcolour." + pd.serial, props.getProperty("plotcolour", null));
             if(colour != null)
                 r.setSeriesPaint(i, new Color(Integer.decode(colour)));
         }
         return chart;   
     }
 
     private JFreeChart createYearChart(boolean detailed) {
         
         int year = date.get(Calendar.YEAR);
         
         java.util.List<PeriodData> periodData = getYearData(year, detailed);
 
         TimeSeriesCollection dataset = new TimeSeriesCollection();
         double totalPeriodPower = 0;
         
         for(PeriodData pd : periodData) {
             TimeSeries s = new TimeSeries(pd.inverter + (periodData.size() > 1? ("-" + pd.serial) : ""));
             dataset.addSeries(s);
             double lastPower = pd.startTotalPower;
             for(int i = 0; i < (detailed? 365 : 12); ++i) {
                 double power = 0;
                 if(pd.powers[i] != 0) {
                     power = pd.powers[i] - lastPower;
                     lastPower = pd.powers[i];
                 }
                 if(detailed) {
                     GregorianCalendar gc = new GregorianCalendar();
                     gc.set(Calendar.DAY_OF_YEAR, i + 1);
                     s.add(new Day(gc.getTime()), power);
                 }
                 else {
                     s.add(new Month(i + 1, year), power);
                 }
             }
             totalPeriodPower += pd.endTotalPower - pd.startTotalPower;
         }
         
         String periodPower = totalPeriodPower < 1.0? String.format("%d W", (int)(totalPeriodPower * 1000)) : String.format("%.3f KW", totalPeriodPower);
         
         JFreeChart chart = ChartFactory.createXYAreaChart(
             year + "      " + periodPower, // title
             (detailed? "Day" : "Month"),      // x-axis label
             "KW",       // y-axis label
             dataset,    // data
             PlotOrientation.VERTICAL,
             true,       // create legend?
             true,       // generate tooltips?
             false       // generate URLs?
             );
         
         chart.setBackgroundPaint(Color.white);
         
         XYPlot plot = (XYPlot) chart.getPlot();
         plot.setBackgroundPaint(Color.lightGray);
         plot.setDomainGridlinePaint(Color.white);
         plot.setRangeGridlinePaint(Color.white);
         plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
         /*
         plot.setDomainCrosshairVisible(true);
         plot.setRangeCrosshairVisible(true);
         */
         plot.setDomainPannable(true);
         plot.setRangePannable(true);
         double maxPower = Double.parseDouble(props.getProperty("maxpower.year", "0"));
         if(maxPower > 0) {
             ValueAxis powerAxis = plot.getRangeAxis();
             powerAxis.setAutoRange(false);
             powerAxis.setLowerBound(0.0);
             powerAxis.setUpperBound(maxPower);
         }
 
         XYItemRenderer r = plot.getRenderer();
         r.setBaseToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
         for(int i = 0; i < periodData.size(); ++i) {
             PeriodData pd = periodData.get(i);
             String colour = props.getProperty("plotcolour." + pd.serial, props.getProperty("plotcolour", null));
             if(colour != null)
                 r.setSeriesPaint(i, new Color(Integer.decode(colour)));
         }
         
         DateAxis axis = new DateAxis();
         axis.setLabel(plot.getDomainAxis().getLabel());
         if(detailed)
             axis.setDateFormatOverride(new SimpleDateFormat("MMM:d"));
         else
             axis.setDateFormatOverride(new SimpleDateFormat("MMMM"));
         plot.setDomainAxis(axis);
         
         return chart;
     }
     
     public void windowClosing(java.awt.event.WindowEvent event) {
         synchronized(graphs) {
             graphs.remove(this);
             if(graphs.size() == 0) {
                 try {
                     conn.close();
                     System.out.println("Database connection terminated");
                 }
                 catch(Exception e) {
                     // relax
                 }
                 // this kills the application
                 super.windowClosing(event);
             }
         }
     }
     
     public java.util.List<DayData> getDayData(int year, int month, int day) {
         Statement stmt = null;
         String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " and dayofmonth(DateTime) = " + day + " order by DateTime";
         Map<String, DayData> result = new HashMap<String, DayData>();
         try {
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 DayData dd = result.get(serial);
                 if(dd == null) {
                     dd = new DayData();
                     dd.serial = serial;
                     dd.inverter = rs.getString("inverter");
                     dd.startTotalPower = rs.getDouble("ETotalToday");
                     result.put(serial, dd);
                 }
                 dd.times.add(rs.getTimestamp("DateTime"));
                 dd.powers.add(rs.getInt("CurrentPower"));
                 dd.endTotalPower = rs.getDouble("ETotalToday");
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<DayData>(result.values());
     }
     
     public java.util.List<PeriodData> getMonthData(int year, int month) {
         Statement stmt = null;
         String query = "select * from DayData where year(DateTime) = " + year + " and month(DateTime) = " + month + " and CurrentPower != 0 order by DateTime";
         Map<String, PeriodData> result = new HashMap<String, PeriodData>();
         GregorianCalendar gc = new GregorianCalendar();
         try {
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 PeriodData pd = result.get(serial);
                 if(pd == null) {
                     pd = new PeriodData();
                     pd.serial = serial;
                     pd.inverter = rs.getString("inverter");
                     pd.startTotalPower = rs.getDouble("ETotalToday");
                     gc.setTime(rs.getTimestamp("DateTime"));
                     gc.set(Calendar.DAY_OF_MONTH, 1);
                     gc.add(Calendar.MONTH, 1);
                     gc.add(Calendar.DAY_OF_MONTH, -1);
                     pd.numPowers = gc.get(Calendar.DAY_OF_MONTH);
                     result.put(serial, pd);
                 }
                 double power = rs.getDouble("ETotalToday");
                 gc.setTime(rs.getTimestamp("DateTime"));
                 pd.powers[gc.get(Calendar.DAY_OF_MONTH) - 1] =  power;
                 pd.endTotalPower = power;
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<PeriodData>(result.values());
     }
     
     public java.util.List<PeriodData> getYearData(int year, boolean detailed) {
         Statement stmt = null;
         String query = "select * from DayData where year(DateTime) = " + year + " and CurrentPower != 0 order by DateTime";
         Map<String, PeriodData> result = new HashMap<String, PeriodData>();
         GregorianCalendar gc = new GregorianCalendar();
         try {
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             while (rs.next()) {
                 String serial = rs.getString("serial");
                 PeriodData pd = result.get(serial);
                 if(pd == null) {
                     pd = new PeriodData();
                     pd.serial = serial;
                     pd.inverter = rs.getString("inverter");
                     pd.startTotalPower = rs.getDouble("ETotalToday");
                     result.put(serial, pd);
                 }
                 gc.setTime(rs.getTimestamp("DateTime"));
                 if(detailed)
                     pd.numPowers = gc.get(Calendar.DAY_OF_YEAR);
                 else
                     pd.numPowers = gc.get(Calendar.MONTH) + 1;
                 double power = rs.getDouble("ETotalToday");
                 pd.powers[pd.numPowers - 1] =  power;
                 pd.endTotalPower = power;
             }
         } catch (SQLException e ) {
             System.err.println("Query failed: " + e.getMessage());
         } finally {
             try {
                 stmt.close();
             }
             catch (SQLException e) {
                 // relax
             }
         }
         return new java.util.ArrayList<PeriodData>(result.values());
     }
     
     public static void main (String[] args) {
         props = new Properties(System.getProperties());
         try {
             props.load(new FileInputStream("pvgraph.properties"));
         }
         catch (IOException ioe) {
             // relax
         }
         String user = props.getProperty("user");
         String password = props.getProperty("password");
         String url = props.getProperty("url");
         Connection conn = null;
         try {
             Class.forName ("com.mysql.jdbc.Driver").newInstance();
             conn = DriverManager.getConnection (url, user, password);
             System.out.println ("Database connection established");
             new PVGraph(conn);
         }
         catch (SQLException e) {
             System.err.println("Cannot connect to " + url + ": " + e.getMessage());
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
