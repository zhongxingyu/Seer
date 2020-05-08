 package cn.poe.group1.gui;
 
 import cn.poe.group1.api.DataCollector;
 import cn.poe.group1.api.MeasurementBackend;
 import cn.poe.group1.entity.Measurement;
 import cn.poe.group1.entity.Port;
 import cn.poe.group1.entity.PortData;
 import cn.poe.group1.entity.Switch;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 /**
  *
  * @author sauron
  */
 public class PoESNMPToolGUI extends javax.swing.JFrame {
 
     private SwitchTableModel switchTableModel;
     private PortDataTableModel portDataTableModel;    
     private Switch selectedSwitch = null;
     private PortData selectedPort = null;
     private MeasurementBackend db = null;
     private ChartPanel portChartPanel = null;
     private ChartPanel switchChartPanel = null;
     private XYSeriesCollection switchDataSet = null;
     private XYSeriesCollection portDataSet = null;
     private DataCollector collector;
     
     private Date measurementStartDate = null;
     private Date measurementEndDate = null;
     
     public PoESNMPToolGUI(MeasurementBackend backend, DataCollector collector) {
         this(backend);
         this.collector = collector;
     }
     
     /**
      * Creates new form PoESNMPToolWindow
      */
     public PoESNMPToolGUI(MeasurementBackend backend) {
         this.db = backend;
         this.switchTableModel = new SwitchTableModel();
         this.switchTableModel.addSwitchList( db.retrieveAllSwitches() );        
         this.portDataTableModel = new PortDataTableModel();        
         initComponents();
         
         this.jdcStartDate.setDate(GUIUtils.getCurrentDay(0));
         this.jdcEndDate.setDate(GUIUtils.getCurrentDay(1));
         Calendar calendar = Calendar.getInstance();
         this.cbStartHour.setSelectedIndex(calendar.get(Calendar.HOUR_OF_DAY));
         this.cbEndHour.setSelectedIndex(calendar.get(Calendar.HOUR_OF_DAY));
 
         refreshMeasurementDates();
         
         JFreeChart chart = createPortChart();
         this.portChartPanel = new ChartPanel(chart);
         this.pChart.add(portChartPanel);
         
         JFreeChart switchChart = createSwitchChart();
         this.switchChartPanel = new ChartPanel(switchChart);
         this.pSwitchChart.add(switchChartPanel);
         
         this.tblSwitch.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 selectedSwitch = switchTableModel.getRow(tblSwitch.getSelectedRow());
                 selectedPort = null;
                 refreshMeasurement();
                 updateSwitchChart();
             }
         });
         
         this.tblSwitch.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 if (e.isPopupTrigger()) {
                     showContextMenu(e);
 		}
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
                 if (e.isPopupTrigger()) {
                     showContextMenu(e);
 		}
             }
         });
         
         this.tblPorts.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 selectedPort = portDataTableModel.getRow( tblPorts.getSelectedRow());
                 updatePortChart();
             }
         });
     }
     
     private void reloadSwitches() {
         this.switchTableModel.clear();
         this.portDataTableModel.clear();
         this.switchTableModel.addSwitchList(this.db.retrieveAllSwitches());
         this.selectedSwitch = null;
         this.selectedPort = null;
     }
 
     private void refreshMeasurementDates() {
         this.measurementStartDate = GUIUtils.buildDateTime(jdcStartDate, cbStartHour, jpfStartMinute);        
         this.measurementEndDate = GUIUtils.buildDateTime(jdcEndDate, cbEndHour, jpfEndMinute);
     }
     
     private void refresh() {
         this.refreshMeasurementDates();
         this.refreshMeasurement();
         this.updatePortChart();
         this.updateSwitchChart();
     }
     
     public void refreshMeasurement() {
         if( this.selectedSwitch != null) {
             this.lblMeasureTime.setText(new Date().toString());
             this.portDataTableModel.clear();
             List<PortData> tmp = db.queryPortData(selectedSwitch, 
                     measurementStartDate, measurementEndDate);
             this.portDataTableModel.addPortDataList( tmp );
         }
     }
     
     //<editor-fold defaultstate="collapsed" desc="All functions for the showing the chart">
     private JFreeChart createPortChart() {
         this.createPortDataSet();
         JFreeChart chart = ChartFactory.createXYLineChart("PortData", "time", "mw"
                 , this.portDataSet, PlotOrientation.VERTICAL, true, true, false);
         XYPlot plot = (XYPlot) chart.getPlot();
 
         plot.setDomainPannable(true);
         plot.setRangePannable(true);
         final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
         plot.setRenderer(renderer);
         
         plot.setRangeGridlinesVisible(true);
         NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         return chart;
     }
     
     
     private JFreeChart createSwitchChart() {
         this.createSwitchDataSet();
         JFreeChart chart = ChartFactory.createXYLineChart("SwitchData", "time", "mw"
                 , this.switchDataSet, PlotOrientation.VERTICAL, true, true, false);
         XYPlot plot = (XYPlot) chart.getPlot();
 
         plot.setDomainPannable(true);
         plot.setRangePannable(true);
         final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
         plot.setRenderer(renderer);
         
         plot.setRangeGridlinesVisible(true);
         NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         return chart;
     }
         
     private void createPortDataSet()  {
         final XYSeriesCollection dataset = new XYSeriesCollection();
         List<XYSeries> tmp = this.createPortChartLines();
         for(XYSeries line : tmp) {
             dataset.addSeries(line);
         }
         this.portDataSet = dataset;
     }
     
     private void createSwitchDataSet()  {
         final XYSeriesCollection dataset = new XYSeriesCollection();
         List<XYSeries> tmp = this.createSwitchChartLines();
         for(XYSeries line : tmp) {
             dataset.addSeries(line);
         }
         this.switchDataSet = dataset;
     }
         
     private void updatePortChart() {
         this.portDataSet.removeAllSeries();
         List<XYSeries> tmp = this.createPortChartLines();
         
         for(XYSeries line : tmp) {
             this.portDataSet.addSeries(line);
         }
      
         this.portChartPanel.getChart().fireChartChanged();
     }
     
     private void updateSwitchChart() {
         this.switchDataSet.removeAllSeries();
         List<XYSeries> tmp = this.createSwitchChartLines();
         
         for(XYSeries line : tmp) {
             this.switchDataSet.addSeries(line);
         }
      
         this.switchChartPanel.getChart().fireChartChanged();
     }
     
     private List<XYSeries> createPortChartLines() {
         List<XYSeries> lines = new LinkedList<>();
         final XYSeries pwrMaxSeries = new XYSeries("PwrMax");
         final XYSeries pwrConsumptionSeries = new XYSeries("PwrConsumption");
         final XYSeries pwrMaxDrawnSeries = new XYSeries("PwrMaxDrawn");
         final XYSeries pwrAllocatedSeries = new XYSeries("PwrAllocated");
         final XYSeries pwrAvailableSeries = new XYSeries("PwrAvailable");
 
         if( this.selectedPort != null ) {
             int time = 0;
             for(Measurement m : db.queryMeasurementsByPort(this.selectedPort.getPort(), 
                     measurementStartDate, measurementEndDate) ) {
                 pwrMaxSeries.add(time, m.getCpeExtPsePortPwrMax());
                 pwrConsumptionSeries.add(time, m.getCpeExtPsePortPwrConsumption());
                 pwrMaxDrawnSeries.add(time, m.getCpeExtPsePortMaxPwrDrawn());
                 pwrAllocatedSeries.add(time, m.getCpeExtPsePortPwrAllocated());
                 pwrAvailableSeries.add(time, m.getCpeExtPsePortPwrAvailable());
                 time++;
             }
         }
         
         lines.add(pwrMaxSeries);
         lines.add(pwrConsumptionSeries);
         lines.add(pwrAllocatedSeries);
         lines.add(pwrAvailableSeries);
         lines.add(pwrMaxDrawnSeries);
         return lines;
     }
     
     private List<XYSeries> createSwitchChartLines() {
         try
         {
             List<XYSeries> lines = new LinkedList<>();
             final XYSeries pwrConsumptionSeries = new XYSeries("PwrConsumption");
             
             if( this.selectedSwitch != null ) {
                 int time = 0;
                 int tmpPwrConsumption = 0;
                 List<Port> portList = db.retrieveAllPorts(selectedSwitch);
                List<Integer> sumPwrConsumption = new ArrayList<Integer>();
                 
                // go through all ports
                 for( Port p : portList)
                 {
                    time = 0;
                    // go through all measurements at a current time of a port
                     for(Measurement m : db.queryMeasurementsByPort(p,
                             measurementStartDate, measurementEndDate) )
                     {
                        // at the start we dont know how many measurements we have so sumPwrConsumption grows iterativly in size
                        if( sumPwrConsumption.size() <= time)
                            sumPwrConsumption.add(0);
                        
                        // add measurement of port at current time to sumList
                        sumPwrConsumption.set(time, sumPwrConsumption.get(time) + m.getCpeExtPsePortPwrConsumption());
                        time++;
                     }
                }
                
                // now insert data (time, sumPwrConsumption) into line
                time = 0;
                for( Integer i : sumPwrConsumption)
                {
                    pwrConsumptionSeries.add(time, i);
                     time++;
                 }
             }
             
             lines.add(pwrConsumptionSeries);
             return lines;
         }
         catch(Exception e)
         {
             return new LinkedList<>();
         }
     }  
     //</editor-fold>
     
     private void showContextMenu(MouseEvent e) {
         JPopupMenu menu = new JPopupMenu();
         final Point clickPoint = e.getPoint();
         JMenuItem itemDelete = new JMenuItem("Delete Switch");
         
         itemDelete.addActionListener(new java.awt.event.ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 int row = tblSwitch.rowAtPoint(clickPoint);
                 deleteSwitch(switchTableModel.getRow(row));
             }
         });
         
         JMenuItem itemEdit = new JMenuItem("Edit Switch");
         itemEdit.addActionListener(new java.awt.event.ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 int row = tblSwitch.rowAtPoint(clickPoint);
                 editSwitch(switchTableModel.getRow(row));
             }
             
         });
         
         menu.add(itemEdit);
         menu.add(itemDelete);
         menu.show(tblSwitch, e.getX(), e.getY());
     }
     
     private void editSwitch(Switch sw) {
         SwitchDialog sd = new SwitchDialog(this, true, sw);
         sw = sd.showDialog();
         if (sw != null) {
             collector.updateSwitch(sw);
             db.mergeSwitch(sw);
             reloadSwitches();
         }
     }
     
     private void deleteSwitch(Switch sw) {
         String[] options = {"Ok", "Cancel"};
 		String question = "Delete switch " + sw.getIdentifier() + " ?";
         int result = JOptionPane.showOptionDialog(this, question, "Confirm", JOptionPane.OK_CANCEL_OPTION,
                 JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
         if (result == JOptionPane.OK_OPTION) {
             try {
                 collector.removeSwitch(sw);
                 db.deleteSwitch(sw);
                 reloadSwitches();
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, "Failed to delete switch", 
                         "Failure", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
     
     private void addSwitch(Switch sw) {
         try {
             for (int i=0; i<sw.getPortCount(); i++) {
                 Port p = new Port(sw, i, null);
                 sw.addPort(p);
             }
             db.persistSwitch(sw);
             collector.addSwitch(sw);
             reloadSwitches();
         } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, "Failed to save switch", 
                         "Failure", JOptionPane.ERROR_MESSAGE);
         }
     }
     
     
     /**
      * @param args the command line arguments
      */
     public static void Main( final MeasurementBackend backend, final DataCollector collector)
     {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : 
                     javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException | InstantiationException 
                 | IllegalAccessException 
                 | javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(PoESNMPToolGUI.class.getName())
                     .log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new PoESNMPToolGUI(backend, collector).setVisible(true);
             }
         });
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jCalendar1 = new com.toedter.calendar.JCalendar();
         pMain = new javax.swing.JPanel();
         pSideBar = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblSwitch = new javax.swing.JTable();
         pCenter = new javax.swing.JPanel();
         pSwitchInfo = new javax.swing.JPanel();
         pMeasurementTime = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         jdcStartDate = new com.toedter.calendar.JDateChooser();
         cbStartHour = new javax.swing.JComboBox();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jdcEndDate = new com.toedter.calendar.JDateChooser();
         cbEndHour = new javax.swing.JComboBox();
         jLabel5 = new javax.swing.JLabel();
         btnRefresh2 = new javax.swing.JButton();
         jpfStartMinute = new com.toedter.components.JSpinField();
         jpfEndMinute = new com.toedter.components.JSpinField();
         pRefreshTime = new javax.swing.JPanel();
         lblMeasureTime = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         pData = new javax.swing.JPanel();
         pPortsData = new javax.swing.JPanel();
         jspPortData = new javax.swing.JSplitPane();
         pChart = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         tblPorts = new javax.swing.JTable();
         pSwitchData = new javax.swing.JPanel();
         jspSwitchData = new javax.swing.JSplitPane();
         jPanel1 = new javax.swing.JPanel();
         pSwitchChart = new javax.swing.JPanel();
         jToolBar1 = new javax.swing.JToolBar();
         btnExit = new javax.swing.JButton();
         btnRefresh = new javax.swing.JButton();
         btnReload = new javax.swing.JButton();
         btnAdd = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setMinimumSize(new java.awt.Dimension(800, 600));
         setPreferredSize(new java.awt.Dimension(1280, 1024));
 
         pMain.setLayout(new java.awt.BorderLayout());
 
         pSideBar.setPreferredSize(new java.awt.Dimension(300, 468));
         pSideBar.setLayout(new java.awt.BorderLayout());
 
         tblSwitch.setModel(this.switchTableModel);
         jScrollPane1.setViewportView(tblSwitch);
 
         pSideBar.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         pMain.add(pSideBar, java.awt.BorderLayout.WEST);
 
         pCenter.setLayout(new java.awt.BorderLayout());
 
         pSwitchInfo.setMinimumSize(new java.awt.Dimension(100, 80));
         pSwitchInfo.setName(""); // NOI18N
         pSwitchInfo.setPreferredSize(new java.awt.Dimension(517, 80));
         pSwitchInfo.setLayout(new java.awt.BorderLayout());
 
         pMeasurementTime.setPreferredSize(new java.awt.Dimension(517, 30));
 
         jLabel2.setText("Messung Start");
 
         jdcStartDate.setMinSelectableDate(new java.util.Date(-62135769517000L));
         jdcStartDate.setMinimumSize(new java.awt.Dimension(150, 27));
 
         cbStartHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
 
         jLabel3.setText("Messung Ende");
 
         jLabel4.setText("Uhr");
 
         jdcEndDate.setMinimumSize(new java.awt.Dimension(150, 27));
 
         cbEndHour.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
 
         jLabel5.setText("Uhr");
 
         btnRefresh2.setText("Reload");
         btnRefresh2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRefresh2ActionPerformed(evt);
             }
         });
 
         jpfStartMinute.setMaximum(59);
         jpfStartMinute.setMinimum(0);
 
         jpfEndMinute.setMaximum(59);
         jpfEndMinute.setMinimum(0);
 
         javax.swing.GroupLayout pMeasurementTimeLayout = new javax.swing.GroupLayout(pMeasurementTime);
         pMeasurementTime.setLayout(pMeasurementTimeLayout);
         pMeasurementTimeLayout.setHorizontalGroup(
             pMeasurementTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pMeasurementTimeLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2)
                 .addGap(18, 18, 18)
                 .addComponent(jdcStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(cbStartHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jpfStartMinute, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel4)
                 .addGap(38, 38, 38)
                 .addComponent(jLabel3)
                 .addGap(4, 4, 4)
                 .addComponent(jdcEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(cbEndHour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jpfEndMinute, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel5)
                 .addGap(38, 38, 38)
                 .addComponent(btnRefresh2)
                 .addContainerGap())
         );
         pMeasurementTimeLayout.setVerticalGroup(
             pMeasurementTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pMeasurementTimeLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pMeasurementTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(jdcEndDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(cbStartHour, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                     .addGroup(pMeasurementTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel5)
                         .addComponent(btnRefresh2))
                     .addComponent(jLabel4)
                     .addComponent(jpfStartMinute, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jdcStartDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(pMeasurementTimeLayout.createSequentialGroup()
                         .addGroup(pMeasurementTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(cbEndHour)
                             .addComponent(jpfEndMinute, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGap(1, 1, 1)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pMeasurementTimeLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnRefresh2, cbEndHour, cbStartHour, jLabel2, jLabel3, jLabel4, jLabel5, jdcEndDate, jdcStartDate, jpfEndMinute, jpfStartMinute});
 
         pSwitchInfo.add(pMeasurementTime, java.awt.BorderLayout.CENTER);
 
         pRefreshTime.setPreferredSize(new java.awt.Dimension(649, 30));
 
         jLabel1.setText("Stand:");
 
         javax.swing.GroupLayout pRefreshTimeLayout = new javax.swing.GroupLayout(pRefreshTime);
         pRefreshTime.setLayout(pRefreshTimeLayout);
         pRefreshTimeLayout.setHorizontalGroup(
             pRefreshTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pRefreshTimeLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addGap(22, 22, 22)
                 .addComponent(lblMeasureTime, javax.swing.GroupLayout.PREFERRED_SIZE, 637, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         pRefreshTimeLayout.setVerticalGroup(
             pRefreshTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addComponent(lblMeasureTime, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
 
         pSwitchInfo.add(pRefreshTime, java.awt.BorderLayout.PAGE_START);
 
         pCenter.add(pSwitchInfo, java.awt.BorderLayout.NORTH);
 
         pData.setLayout(new java.awt.BorderLayout());
 
         pPortsData.setLayout(new java.awt.BorderLayout());
 
         jspPortData.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jspPortData.setMinimumSize(new java.awt.Dimension(23, 600));
 
         pChart.setMinimumSize(new java.awt.Dimension(0, 300));
         pChart.setPreferredSize(new java.awt.Dimension(0, 300));
         pChart.setLayout(new java.awt.BorderLayout());
         jspPortData.setBottomComponent(pChart);
 
         jScrollPane2.setMinimumSize(new java.awt.Dimension(23, 300));
         jScrollPane2.setPreferredSize(new java.awt.Dimension(453, 300));
 
         tblPorts.setModel(this.portDataTableModel);
         jScrollPane2.setViewportView(tblPorts);
 
         jspPortData.setTopComponent(jScrollPane2);
 
         pPortsData.add(jspPortData, java.awt.BorderLayout.CENTER);
 
         pData.add(pPortsData, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("Ports", pData);
 
         pSwitchData.setLayout(new java.awt.BorderLayout());
 
         jspSwitchData.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
 
         jPanel1.setMinimumSize(new java.awt.Dimension(100, 250));
         jPanel1.setPreferredSize(new java.awt.Dimension(983, 250));
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 983, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 250, Short.MAX_VALUE)
         );
 
         jspSwitchData.setTopComponent(jPanel1);
 
         pSwitchChart.setLayout(new java.awt.BorderLayout());
         jspSwitchData.setBottomComponent(pSwitchChart);
 
         pSwitchData.add(jspSwitchData, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("Switch", pSwitchData);
 
         pCenter.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
 
         pMain.add(pCenter, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(pMain, java.awt.BorderLayout.CENTER);
 
         jToolBar1.setRollover(true);
         jToolBar1.setMaximumSize(new java.awt.Dimension(18, 25));
         jToolBar1.setMinimumSize(new java.awt.Dimension(18, 25));
 
         btnExit.setText("Exit");
         btnExit.setFocusable(false);
         btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnExit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExitActionPerformed(evt);
             }
         });
         jToolBar1.add(btnExit);
 
         btnRefresh.setText("Refresh");
         btnRefresh.setFocusable(false);
         btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRefreshActionPerformed(evt);
             }
         });
         jToolBar1.add(btnRefresh);
 
         btnReload.setText("Reload Switches");
         btnReload.setFocusable(false);
         btnReload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnReload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnReload.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnReloadActionPerformed(evt);
             }
         });
         jToolBar1.add(btnReload);
 
         btnAdd.setText("Add Switch");
         btnAdd.setFocusable(false);
         btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddActionPerformed(evt);
             }
         });
         jToolBar1.add(btnAdd);
 
         getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
         collector.shutdown();
         this.dispose();
     }//GEN-LAST:event_btnExitActionPerformed
     
     private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
         this.refresh();
     }//GEN-LAST:event_btnRefreshActionPerformed
 
     private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
         this.reloadSwitches();
     }//GEN-LAST:event_btnReloadActionPerformed
 
     private void btnRefresh2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh2ActionPerformed
         this.refresh();
     }//GEN-LAST:event_btnRefresh2ActionPerformed
 
     private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
         SwitchDialog sd = new SwitchDialog(this, true);
         Switch sw = sd.showDialog();
         if (sw != null) {
             addSwitch(sw);
         }
     }//GEN-LAST:event_btnAddActionPerformed
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnExit;
     private javax.swing.JButton btnRefresh;
     private javax.swing.JButton btnRefresh2;
     private javax.swing.JButton btnReload;
     private javax.swing.JComboBox cbEndHour;
     private javax.swing.JComboBox cbStartHour;
     private com.toedter.calendar.JCalendar jCalendar1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JToolBar jToolBar1;
     private com.toedter.calendar.JDateChooser jdcEndDate;
     private com.toedter.calendar.JDateChooser jdcStartDate;
     private com.toedter.components.JSpinField jpfEndMinute;
     private com.toedter.components.JSpinField jpfStartMinute;
     private javax.swing.JSplitPane jspPortData;
     private javax.swing.JSplitPane jspSwitchData;
     private javax.swing.JLabel lblMeasureTime;
     private javax.swing.JPanel pCenter;
     private javax.swing.JPanel pChart;
     private javax.swing.JPanel pData;
     private javax.swing.JPanel pMain;
     private javax.swing.JPanel pMeasurementTime;
     private javax.swing.JPanel pPortsData;
     private javax.swing.JPanel pRefreshTime;
     private javax.swing.JPanel pSideBar;
     private javax.swing.JPanel pSwitchChart;
     private javax.swing.JPanel pSwitchData;
     private javax.swing.JPanel pSwitchInfo;
     private javax.swing.JTable tblPorts;
     private javax.swing.JTable tblSwitch;
     // End of variables declaration//GEN-END:variables
 }
