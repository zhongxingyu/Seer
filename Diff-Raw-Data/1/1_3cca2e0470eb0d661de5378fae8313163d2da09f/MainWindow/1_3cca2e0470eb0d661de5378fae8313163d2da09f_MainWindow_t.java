 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jac444b.a2;
 
 import java.awt.event.MouseEvent;
 import java.io.*;
 import java.util.*;
 import javax.swing.JFileChooser;
 import javax.swing.table.DefaultTableModel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 import org.jdesktop.swingx.mapviewer.Waypoint;
 import org.jdesktop.swingx.mapviewer.WaypointPainter;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  *
  * @author Testee
  */
 public class MainWindow extends javax.swing.JFrame {
 
     private void AddWaypoint(WaypointExtension wp) {
         waypoints.add(wp);
         redraw();
 
         listWaypoints.removeAll();
         for (Iterator<Waypoint> it = waypoints.iterator(); it.hasNext();) {
             Waypoint w = it.next();
             listWaypoints.add(w.getPosition().getLatitude() + " " + w.getPosition().getLongitude());
         }
     }
 
     private void AddWaypointByIP(String ipAddress, boolean addMarker, boolean showMetaData) {
         //Get the json data
         String info = HTTPUtility.DownloadWebsite("http://freegeoip.net/json/" + ipAddress);
         //Remove the braces and get the elements
         String[] values = info.replace("{", "").replace("}", "").split(",");
         //A dictionary to store all geoIp data        
         Map<String, String> geoipData = new HashMap<String, String>();
         //Simple parsing for json elements
         for (String element : values) {
             //Get the data on both sides of the :
             String[] keyValue = element.replace("\"", "").split(":");
             //Stick it in the dictionary
             geoipData.put(keyValue[0].trim(), keyValue[1].trim());
         }
 
         //Get the table model to start adding elements
         DefaultTableModel tableModel = (DefaultTableModel) tableGeoIP.getModel();
         //Remove all the rows in the table (clear it)
         while (tableGeoIP.getRowCount() > 0) {
             tableModel.removeRow(0);
         }
         for (String key : geoipData.keySet()) {
             String friendlyKey = Character.toUpperCase(key.charAt(0)) + key.substring(1).replace("_", " ");
             tableModel.addRow(new String[]{friendlyKey, geoipData.get(key)});
         }
 
         GeoPosition position = new GeoPosition(Double.valueOf(geoipData.get("latitude")), Double.valueOf(geoipData.get("longitude")));
 
         if (addMarker) {
             StringBuilder sb = new StringBuilder();
             if (showMetaData) {
                 sb.append("City:    ").append(geoipData.get("city")).append("\n");
                 sb.append("Region:  ").append(geoipData.get("region_name")).append("\n");
                 sb.append("Country: ").append(geoipData.get("country_name")).append("\n");
             }
 
             AddWaypoint(new WaypointExtension(sb.toString(), position));
         }
 
         jxMap.setCenterPosition(position);
     }
 
     /*
      * Creates new form MainWindow
      */
     public MainWindow() {
         initComponents();
 
         for (int i = 0; i < countryList.size(); i++) {
             listCountries.add(countryList.get(i).getName());
         }
 
         txtIpAddress.setText(HTTPUtility.GetIp());
 
         jxMap.getMainMap().addMouseMotionListener(new java.awt.event.MouseMotionListener() {
 
             @Override
             public void mouseDragged(MouseEvent e) {
             }
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 GeoPosition location = jxMap.getMainMap().convertPointToGeoPosition(jxMap.getMousePosition());
                 lblStatus.setText("Location: { " + location.getLatitude() + " , " + location.getLongitude() + " }");
             }
         });
         jxMap.getMainMap().addMouseListener(new java.awt.event.MouseListener() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 //if right mouse button is clicked
                 if (e.getButton() == 3) {
                     GeoPosition location = jxMap.getMainMap().convertPointToGeoPosition(jxMap.getMousePosition());
                     AddWaypoint(new WaypointExtension("New Waypoint", location));
                 }
                 //middle mouse
                 else if(e.getButton() == 2) {
                     GeoPosition location = jxMap.getMainMap().convertPointToGeoPosition(jxMap.getMousePosition());
                     spnLongitude.setValue(location.getLongitude());
                     spnLatitude.setValue(location.getLatitude());
                 }
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
             }
 
             @Override
             public void mouseEntered(MouseEvent e) {
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
             }
         });
 
         //Add the waypoint on startup
         AddWaypointByIP(txtIpAddress.getText().trim(), true, true);
     }
 
     /*
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jxMap = new org.jdesktop.swingx.JXMapKit();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         panelLookup = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tableGeoIP = new javax.swing.JTable() {
             public boolean isCellEditable(int rowIndex, int vColIndex) {
                 return false;
             }
         };
         label1 = new java.awt.Label();
         jPanel1 = new javax.swing.JPanel();
         label2 = new java.awt.Label();
         spnLongitude = new javax.swing.JSpinner();
         spnLatitude = new javax.swing.JSpinner();
         label3 = new java.awt.Label();
         btnGotoLongLat = new java.awt.Button();
         chkPlaceMarkerLongLat = new javax.swing.JCheckBox();
         jPanel2 = new javax.swing.JPanel();
         chkPlaceMarker = new javax.swing.JCheckBox();
         txtIpAddress = new java.awt.TextField();
         lblIpAddress = new java.awt.Label();
         btnSearchIP = new java.awt.Button();
         panelWaypoints = new javax.swing.JPanel();
         listWaypoints = new java.awt.List();
         btnRemoveWaypoints = new java.awt.Button();
         jPanel3 = new javax.swing.JPanel();
         lblWaypointText = new javax.swing.JLabel();
         txtWaypointText = new javax.swing.JTextField();
         btnWaypointText = new javax.swing.JButton();
         panelCountries = new javax.swing.JPanel();
         listCountries = new java.awt.List();
         chkCountryPlaceMarker = new javax.swing.JCheckBox();
         panelStatusBar = new javax.swing.JPanel();
         lblStatus = new javax.swing.JLabel();
         menuBar = new javax.swing.JMenuBar();
         fileMenu = new javax.swing.JMenu();
         openMenuItem = new javax.swing.JMenuItem();
         saveMenuItem = new javax.swing.JMenuItem();
         saveAsMenuItem = new javax.swing.JMenuItem();
         exitMenuItem = new javax.swing.JMenuItem();
         editMenu = new javax.swing.JMenu();
         cutMenuItem = new javax.swing.JMenuItem();
         copyMenuItem = new javax.swing.JMenuItem();
         pasteMenuItem = new javax.swing.JMenuItem();
         deleteMenuItem = new javax.swing.JMenuItem();
         helpMenu = new javax.swing.JMenu();
         contentsMenuItem = new javax.swing.JMenuItem();
         aboutMenuItem = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setResizable(false);
         getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         jxMap.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
         getContentPane().add(jxMap, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 480));
 
         panelLookup.setPreferredSize(new java.awt.Dimension(195, 455));
 
         tableGeoIP.setAutoCreateRowSorter(true);
         tableGeoIP.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Name", "Data"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
         });
         tableGeoIP.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         tableGeoIP.getTableHeader().setResizingAllowed(false);
         tableGeoIP.getTableHeader().setReorderingAllowed(false);
         jScrollPane1.setViewportView(tableGeoIP);
 
         label1.setText("label1");
         label1.setVisible(false);
 
         jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         label2.setText("Longitute:");
 
         spnLongitude.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.5d)));
         spnLongitude.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
             public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                 spnLongitudeMouseWheelMoved(evt);
             }
         });
         spnLongitude.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 spnLongitudeStateChanged(evt);
             }
         });
 
         spnLatitude.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), null, null, Double.valueOf(0.5d)));
         spnLatitude.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
             public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                 spnLatitudeMouseWheelMoved(evt);
             }
         });
         spnLatitude.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 spnLatitudeStateChanged(evt);
             }
         });
 
         label3.setText("Latitude:");
 
         btnGotoLongLat.setLabel("Goto");
         btnGotoLongLat.setMinimumSize(new java.awt.Dimension(57, 24));
         btnGotoLongLat.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGotoLongLatActionPerformed(evt);
             }
         });
 
         chkPlaceMarkerLongLat.setText("Place Marker");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(14, 14, 14))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addComponent(chkPlaceMarkerLongLat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addGap(11, 11, 11)
                                 .addComponent(btnGotoLongLat, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(spnLatitude)
                             .addComponent(spnLongitude, javax.swing.GroupLayout.Alignment.TRAILING))
                         .addContainerGap())))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(spnLongitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(2, 2, 2)
                 .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(spnLatitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnGotoLongLat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(chkPlaceMarkerLongLat))
                 .addGap(0, 10, Short.MAX_VALUE))
         );
 
         jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         chkPlaceMarker.setText("Place Marker");
 
         lblIpAddress.setText("IP Address or Hostname:");
 
         btnSearchIP.setLabel("Lookup");
         btnSearchIP.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSearchIPActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addGap(10, 10, 10)
                         .addComponent(chkPlaceMarker, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnSearchIP, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(txtIpAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(lblIpAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                 .addComponent(lblIpAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(2, 2, 2)
                 .addComponent(txtIpAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(chkPlaceMarker)
                     .addComponent(btnSearchIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(11, Short.MAX_VALUE))
         );
 
         btnSearchIP.getAccessibleContext().setAccessibleName("btnSearchIP");
 
         javax.swing.GroupLayout panelLookupLayout = new javax.swing.GroupLayout(panelLookup);
         panelLookup.setLayout(panelLookupLayout);
         panelLookupLayout.setHorizontalGroup(
             panelLookupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelLookupLayout.createSequentialGroup()
                 .addGroup(panelLookupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(panelLookupLayout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(panelLookupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                     .addGroup(panelLookupLayout.createSequentialGroup()
                         .addGap(10, 10, 10)
                         .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         panelLookupLayout.setVerticalGroup(
             panelLookupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelLookupLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(17, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Lookup", panelLookup);
 
         btnRemoveWaypoints.setLabel("Remove Waypoint");
         btnRemoveWaypoints.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveWaypointsActionPerformed(evt);
             }
         });
 
         jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         lblWaypointText.setText("Add Waypoint Text");
 
         btnWaypointText.setText("Add Text");
         btnWaypointText.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnWaypointTextActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtWaypointText)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(lblWaypointText)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(btnWaypointText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lblWaypointText)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(txtWaypointText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnWaypointText)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout panelWaypointsLayout = new javax.swing.GroupLayout(panelWaypoints);
         panelWaypoints.setLayout(panelWaypointsLayout);
         panelWaypointsLayout.setHorizontalGroup(
             panelWaypointsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(listWaypoints, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelWaypointsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(panelWaypointsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnRemoveWaypoints, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                     .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         panelWaypointsLayout.setVerticalGroup(
             panelWaypointsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelWaypointsLayout.createSequentialGroup()
                 .addComponent(listWaypoints, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnRemoveWaypoints, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("Waypoints", panelWaypoints);
 
         listCountries.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         listCountries.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 listCountriesActionPerformed(evt);
             }
         });
 
         chkCountryPlaceMarker.setText("Place Marker");
 
         javax.swing.GroupLayout panelCountriesLayout = new javax.swing.GroupLayout(panelCountries);
         panelCountries.setLayout(panelCountriesLayout);
         panelCountriesLayout.setHorizontalGroup(
             panelCountriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCountriesLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addGroup(panelCountriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(chkCountryPlaceMarker, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(listCountries, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         panelCountriesLayout.setVerticalGroup(
             panelCountriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelCountriesLayout.createSequentialGroup()
                 .addComponent(chkCountryPlaceMarker)
                 .addGap(1, 1, 1)
                 .addComponent(listCountries, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Countries", panelCountries);
 
         getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 0, 200, 480));
         jTabbedPane1.getAccessibleContext().setAccessibleName("Lookup");
 
         panelStatusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         lblStatus.setText("Location: ");
 
         javax.swing.GroupLayout panelStatusBarLayout = new javax.swing.GroupLayout(panelStatusBar);
         panelStatusBar.setLayout(panelStatusBarLayout);
         panelStatusBarLayout.setHorizontalGroup(
             panelStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelStatusBarLayout.createSequentialGroup()
                 .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 635, Short.MAX_VALUE))
         );
         panelStatusBarLayout.setVerticalGroup(
             panelStatusBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(panelStatusBarLayout.createSequentialGroup()
                 .addComponent(lblStatus)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         lblStatus.getAccessibleContext().setAccessibleName("lblStatus");
 
         getContentPane().add(panelStatusBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 480, 980, -1));
 
         fileMenu.setMnemonic('f');
         fileMenu.setText("File");
 
         openMenuItem.setMnemonic('o');
         openMenuItem.setText("Open");
         openMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 openMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(openMenuItem);
 
         saveMenuItem.setMnemonic('s');
         saveMenuItem.setText("Save");
         saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(saveMenuItem);
 
         saveAsMenuItem.setMnemonic('a');
         saveAsMenuItem.setText("Save As ...");
         saveAsMenuItem.setDisplayedMnemonicIndex(5);
         saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveAsMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(saveAsMenuItem);
 
         exitMenuItem.setMnemonic('x');
         exitMenuItem.setText("Exit");
         exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exitMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         editMenu.setMnemonic('e');
         editMenu.setText("Edit");
 
         cutMenuItem.setMnemonic('t');
         cutMenuItem.setText("Cut");
         editMenu.add(cutMenuItem);
 
         copyMenuItem.setMnemonic('y');
         copyMenuItem.setText("Copy");
         editMenu.add(copyMenuItem);
 
         pasteMenuItem.setMnemonic('p');
         pasteMenuItem.setText("Paste");
         editMenu.add(pasteMenuItem);
 
         deleteMenuItem.setMnemonic('d');
         deleteMenuItem.setText("Delete");
         editMenu.add(deleteMenuItem);
 
         menuBar.add(editMenu);
 
         helpMenu.setMnemonic('h');
         helpMenu.setText("Help");
 
         contentsMenuItem.setMnemonic('c');
         contentsMenuItem.setText("Contents");
         helpMenu.add(contentsMenuItem);
 
         aboutMenuItem.setMnemonic('a');
         aboutMenuItem.setText("About");
         aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 aboutMenuItemActionPerformed(evt);
             }
         });
         helpMenu.add(aboutMenuItem);
 
         menuBar.add(helpMenu);
 
         setJMenuBar(menuBar);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
         System.exit(0);
     }//GEN-LAST:event_exitMenuItemActionPerformed
 
     private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
         AboutWindow ab = new AboutWindow();
        ab.setDefaultCloseOperation(ab.HIDE_ON_CLOSE);
         ab.setVisible(true);
     }//GEN-LAST:event_aboutMenuItemActionPerformed
 
     private void listCountriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listCountriesActionPerformed
         Country c = countryList.get(listCountries.getSelectedIndex());
         if (chkCountryPlaceMarker.isSelected()) {
             AddWaypoint(new WaypointExtension(c.getName(), c.getLocation()));
         }
         jxMap.setCenterPosition(c.getLocation());
     }//GEN-LAST:event_listCountriesActionPerformed
 
     private void btnSearchIPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchIPActionPerformed
         if (txtIpAddress.getText().trim().length() <= 0) {
             return;
         }
         AddWaypointByIP(txtIpAddress.getText().trim(), chkPlaceMarker.isSelected(), true);
     }//GEN-LAST:event_btnSearchIPActionPerformed
 
     private void btnRemoveWaypointsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveWaypointsActionPerformed
         if (listWaypoints.getSelectedIndex() >= 0) {
             for (Iterator<Waypoint> it = waypoints.iterator(); it.hasNext();) {
                 Waypoint wp = it.next();
                 if (listWaypoints.getSelectedItem().split(" ")[0].equals(Double.toString(wp.getPosition().getLatitude()))
                         && listWaypoints.getSelectedItem().split(" ")[1].equals(Double.toString(wp.getPosition().getLongitude()))) {
                     it.remove();
                 }
             }
             listWaypoints.remove(listWaypoints.getSelectedIndex());
 
             redraw();
         }
     }//GEN-LAST:event_btnRemoveWaypointsActionPerformed
 
     private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
         JFileChooser fc = new JFileChooser();
         int retVal = fc.showOpenDialog(menuBar);
         if (retVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             if (file.exists()) {
                 WaypointData wd = null;
                 try {
                     // opening a flow Input since the file "personne.serial"
                     FileInputStream fis = new FileInputStream(file);
                     // creation a "Flow object " with the flow file
                     ObjectInputStream ois = new ObjectInputStream(fis);
                     try {
                         //clear the waypoints
                         waypoints.clear();
                         // deserialize
                         while ((wd = (WaypointData) ois.readObject()) != null) {
                             waypoints.add(new WaypointExtension(wd.getText(), new GeoPosition(wd.getLatitude(), wd.getLongitude())));
                         }
 
                     } catch (EOFException eof) {//after reading the file
                         //clear the waypoints list
                         listWaypoints.removeAll();
                         //re-add the waypoinds
                         for (Iterator<Waypoint> it = waypoints.iterator(); it.hasNext();) {
                             Waypoint w = it.next();
                             listWaypoints.add(w.getPosition().getLatitude() + " " + w.getPosition().getLongitude());
                         }
                         redraw();
 
                     } finally {
                         // close the streams
                         try {
                             ois.close();
                         } finally {
                             fis.close();
                         }
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
 
             }
         }
     }//GEN-LAST:event_openMenuItemActionPerformed
 
     private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
         //create the filechooser
         JFileChooser fc = new JFileChooser();
         fc.setDialogTitle("Save");
         fc.setApproveButtonText("Save");
 
         //get the selected file
         int retVal = fc.showOpenDialog(menuBar);
         if (retVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             try {
                 //create the file steam
                 FileOutputStream fis = new FileOutputStream(file);
                 // create the object stream
                 ObjectOutputStream ois = new ObjectOutputStream(fis);
                 try {
                     //write each object in waypoints
                     for (Waypoint w : waypoints) {
                         ois.writeObject(new WaypointData(((WaypointExtension) w).getText(), w.getPosition().getLatitude(), w.getPosition().getLongitude()));
                     }
                     //empty the buffer
                     ois.flush();
 
                 } finally {
                     //close the streams
                     try {
                         ois.close();
                     } finally {
                         fis.close();
                     }
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
 
         } else {
             //TODO: code exception
         }
     }//GEN-LAST:event_saveAsMenuItemActionPerformed
 
     private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
         File file = new File(System.getProperty("user.dir") + "\\waypoints");
         try {
             //create the file steam
             FileOutputStream fis = new FileOutputStream(file);
             // create the object stream
             ObjectOutputStream ois = new ObjectOutputStream(fis);
             try {
                 //write each object in waypoints
                 for (Waypoint w : waypoints) {
                     ois.writeObject(new WaypointData(((WaypointExtension) w).getText(), w.getPosition().getLatitude(), w.getPosition().getLongitude()));
                 }
                 //empty the buffer
                 ois.flush();
 
             } finally {
                 //close the streams
                 try {
                     ois.close();
                 } finally {
                     fis.close();
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }//GEN-LAST:event_saveMenuItemActionPerformed
 
     private void btnGotoLongLatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGotoLongLatActionPerformed
         double lat = Double.parseDouble(spnLatitude.getValue().toString());
         double lon = Double.parseDouble(spnLongitude.getValue().toString());
         String xmlData = HTTPUtility.DownloadWebsite(
                 String.format("http://maps.googleapis.com/maps/api/geocode/xml?latlng=%2f,%2f&sensor=false",
                 lat, lon));
         Document data = null;
         //The data that will be used in the table
         Map<String, String> latlongData = new HashMap<String, String>();
         try {
             //Get the document safely
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             data = builder.parse(new InputSource(new StringReader(xmlData)));
             data.getDocumentElement().normalize();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         
         if (data == null) {
             return;
         }
         //Get the main results
         NodeList results = data.getElementsByTagName("result");
         for (int i = 0; i < results.getLength(); i++) {
             //Get the result @ i
             Node n = results.item(i);
             if(n.getNodeType() == Node.ELEMENT_NODE) {
                 Element  fe = (Element)n;
                 //Get the type of the result
                 NodeList nl = fe.getElementsByTagName("type");
                 String itemtext = nl.item(0).getTextContent();
                 //Ensure that the type is what we're looking for
                 if(itemtext.equals("street_address") || itemtext.equals("administrative_area_level_2")) {
                     //Get the address component
                     NodeList addresses = fe.getElementsByTagName("address_component");
                     for(int j = 0; j < addresses.getLength(); j++) {
                         Element addr = (Element)addresses.item(j);
                         String type = addr.getElementsByTagName("type").item(0).getTextContent();
                         //Convert bad names to good ones
                         if(type.equals("administrative_area_level_2"))
                             type = "Region";
                         else if(type.equals("administrative_area_level_1"))
                             type = "City";                        
                         String addrData = addr.getElementsByTagName("long_name").item(0).getTextContent();
                         //Make the name look more pretty
                         type = Character.toUpperCase(type.charAt(0)) + type.substring(1).replace("_", " ");
                                                 
                         //Add it to the table if not already added
                         if(!latlongData.containsKey(type))
                             latlongData.put(type, addrData);
                     }                   
                 }
             }
         }
 
         if(chkPlaceMarkerLongLat.isSelected()) {
             StringBuilder sb = new StringBuilder();            
             sb.append("City: ").append(latlongData.get("City")).append("\n");
             sb.append("Region: ").append(latlongData.get("Region")).append("\n");
             sb.append("Country: ").append(latlongData.get("Country")).append("\n");
             
             AddWaypoint(new WaypointExtension(sb.toString(), new GeoPosition(lat,lon)));
         }
         
         //Get the table model to start adding elements
         DefaultTableModel tableModel = (DefaultTableModel) tableGeoIP.getModel();
         //Remove all the rows in the table (clear it)
         while (tableGeoIP.getRowCount() > 0) {
             tableModel.removeRow(0);
         }
 
         for (String key : latlongData.keySet()) {
             tableModel.addRow(new String[]{key, latlongData.get(key)});
         }
         
         jxMap.setCenterPosition(new GeoPosition(lat,lon));
     }//GEN-LAST:event_btnGotoLongLatActionPerformed
 
     private void spnLongitudeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_spnLongitudeMouseWheelMoved
         int sign = evt.getUnitsToScroll() >= 0 ? 1 : -1;
         if (sign == 1) {
             spnLongitude.setValue(spnLongitude.getModel().getPreviousValue());
         } else {
             spnLongitude.setValue(spnLongitude.getModel().getNextValue());
         }
     }//GEN-LAST:event_spnLongitudeMouseWheelMoved
 
     private void spnLatitudeMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_spnLatitudeMouseWheelMoved
         int sign = evt.getUnitsToScroll() >= 0 ? 1 : -1;
         if (sign == 1) {
             spnLatitude.setValue(spnLatitude.getModel().getPreviousValue());
         } else {
             spnLatitude.setValue(spnLatitude.getModel().getNextValue());
         }
     }//GEN-LAST:event_spnLatitudeMouseWheelMoved
 
     private void spnLatitudeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnLatitudeStateChanged
         double val = new Double(spnLatitude.getValue().toString());
         if (val < -90) {
             val = 90;
         } else if (val > 90) {
             val = -90;
         }
         spnLatitude.setValue(val);
     }//GEN-LAST:event_spnLatitudeStateChanged
 
     private void spnLongitudeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnLongitudeStateChanged
         double val = new Double(spnLongitude.getValue().toString());
         if (val < -180) {
             val = 180;
         } else if (val > 180) {
             val = -180;
         }
         spnLongitude.setValue(val);
     }//GEN-LAST:event_spnLongitudeStateChanged
 
     private void btnWaypointTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWaypointTextActionPerformed
         //if a waypoint is selected
         if(listWaypoints.getSelectedIndex() >= 0){                    
             GeoPosition gp = new GeoPosition(Double.parseDouble(listWaypoints.getSelectedItem().split(" ")[0]), 
                                              Double.parseDouble(listWaypoints.getSelectedItem().split(" ")[1]));
             //set the waypoint text to blank if there is no text
             String str = "      ";
             if(txtWaypointText.getText().length() > 0){
                 str = txtWaypointText.getText();
             }
             //loop through the waypoints until the selected one is found
             for(Waypoint w : waypoints){
                 if(w.getPosition().equals(gp)){
                     //when found set the waypoint text and redraw
                     ((WaypointExtension)w).setText(str);
                     redraw();
                     return;
                 }
             }
         }
     }//GEN-LAST:event_btnWaypointTextActionPerformed
 
     private void redraw(){
         WaypointPainter painter = new WaypointPainter();
         painter.setWaypoints(waypoints);
         //create a renderer
         painter.setRenderer(new CustomWaypointRenderer());
         jxMap.getMainMap().setOverlayPainter(painter);
         jxMap.getMainMap().repaint();
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem aboutMenuItem;
     private java.awt.Button btnGotoLongLat;
     private java.awt.Button btnRemoveWaypoints;
     private java.awt.Button btnSearchIP;
     private javax.swing.JButton btnWaypointText;
     private javax.swing.JCheckBox chkCountryPlaceMarker;
     private javax.swing.JCheckBox chkPlaceMarker;
     private javax.swing.JCheckBox chkPlaceMarkerLongLat;
     private javax.swing.JMenuItem contentsMenuItem;
     private javax.swing.JMenuItem copyMenuItem;
     private javax.swing.JMenuItem cutMenuItem;
     private javax.swing.JMenuItem deleteMenuItem;
     private javax.swing.JMenu editMenu;
     private javax.swing.JMenuItem exitMenuItem;
     private javax.swing.JMenu fileMenu;
     private javax.swing.JMenu helpMenu;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private org.jdesktop.swingx.JXMapKit jxMap;
     private java.awt.Label label1;
     private java.awt.Label label2;
     private java.awt.Label label3;
     private java.awt.Label lblIpAddress;
     private javax.swing.JLabel lblStatus;
     private javax.swing.JLabel lblWaypointText;
     private java.awt.List listCountries;
     private java.awt.List listWaypoints;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenuItem openMenuItem;
     private javax.swing.JPanel panelCountries;
     private javax.swing.JPanel panelLookup;
     private javax.swing.JPanel panelStatusBar;
     private javax.swing.JPanel panelWaypoints;
     private javax.swing.JMenuItem pasteMenuItem;
     private javax.swing.JMenuItem saveAsMenuItem;
     private javax.swing.JMenuItem saveMenuItem;
     private javax.swing.JSpinner spnLatitude;
     private javax.swing.JSpinner spnLongitude;
     private javax.swing.JTable tableGeoIP;
     private java.awt.TextField txtIpAddress;
     private javax.swing.JTextField txtWaypointText;
     // End of variables declaration//GEN-END:variables
     private Set<Waypoint> waypoints = new HashSet<Waypoint>();
     ArrayList<Country> countryList = Country.AllCountries();
 }
