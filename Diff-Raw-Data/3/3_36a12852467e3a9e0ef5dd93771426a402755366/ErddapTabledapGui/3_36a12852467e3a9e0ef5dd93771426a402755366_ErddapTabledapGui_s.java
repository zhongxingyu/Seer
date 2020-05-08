 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.asascience.edc.erddap.gui;
 
 import cern.colt.Timer;
 import com.asascience.edc.Configuration;
 import com.asascience.edc.erddap.ErddapDataset;
 import com.asascience.edc.erddap.ErddapVariable;
 import com.asascience.edc.gui.BoundingBoxPanel;
 import com.asascience.edc.gui.OpendapInterface;
 import com.asascience.edc.gui.WorldwindSelectionMap;
 import com.asascience.edc.utils.FileSaveUtils;
 import gov.noaa.pmel.swing.JSlider2Date;
 import gov.noaa.pmel.util.GeoDate;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 import net.miginfocom.swing.MigLayout;
 import ucar.nc2.units.DateUnit;
 
 /**
  *
  * @author Kyle
  */
 public class ErddapTabledapGui extends JPanel {
 
   private ErddapDataset erd;
   private JPanel sliderPanel;
   private ArrayList<ErddapVariableSubset> variables;
   private JTextField url;
   private OpendapInterface parent;
   private ErddapDataRequest request;
   private WorldwindSelectionMap mapPanel;
   private BoundingBoxPanel bboxGui;
   private JSlider2Date dateSlider;
   private String homeDir;
   
   public ErddapTabledapGui(ErddapDataset erd, OpendapInterface parent, String homeDir) {
     this.erd = erd;
     this.parent = parent;
     this.request = new ErddapDataRequest(homeDir);
     this.homeDir = homeDir;
     initComponents();
   }
   
   private void initComponents() {
     setLayout(new MigLayout("gap 0, fill"));
     
     // Panel for map, bbox, and timeslider
     JPanel mapStuff = new JPanel(new MigLayout("gap 0, fill"));
     
     // Map
     mapPanel = new WorldwindSelectionMap(homeDir);
     mapPanel.addPropertyChangeListener(new PropertyChangeListener() {
 
       public void propertyChange(PropertyChangeEvent e) {
         String name = e.getPropertyName();
         // Bounding box was drawn
         if (name.equals("boundsStored")) {
           bboxGui.setBoundingBox(mapPanel.getSelectedExtent());
         }
       }
     });
     mapStuff.add(mapPanel, "gap 0, grow, wrap, spanx 2");
     
     // BBOX panel
     bboxGui = new BoundingBoxPanel();
     // Set GUI to the datasets bounds
     bboxGui.setBoundingBox(Double.parseDouble(erd.getY().getMax()), Double.parseDouble(erd.getX().getMin()), Double.parseDouble(erd.getY().getMin()), Double.parseDouble(erd.getX().getMax()));
     mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
     mapPanel.makeDataExtentLayer(bboxGui.getBoundingBox());
     bboxGui.addPropertyChangeListener(new PropertyChangeListener() {
 
       public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals("bboxchange")) {
           mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
         }
       }
     });
     mapStuff.add(bboxGui, "gap 0, growx");
 
     // TIME panel
     JPanel timePanel = new JPanel(new MigLayout("gap 0, fill"));
     timePanel.setBorder(new EtchedBorder());
     dateSlider = new JSlider2Date();
     dateSlider.setAlwaysPost(true);
     dateSlider.setShowBorder(false);
     dateSlider.setHandleSize(6);
     // Get min and max time for dataset
     Date st = DateUnit.getStandardDate(erd.getTime().getMin() + " " + erd.getTime().getUnits());
     Date et = DateUnit.getStandardDate(erd.getTime().getMax() + " " + erd.getTime().getUnits());
     dateSlider.setRange(st,et);
     dateSlider.setStartValue(new GeoDate(st));
     dateSlider.setFormat("yyyy-MM-dd");
     timePanel.add(dateSlider, "gap 0, grow, center");
     mapStuff.add(timePanel, "gap 0, growx");
     
     add(mapStuff, "gap 0, grow");
     
     
     // Panel with subsetting sliders and such
     sliderPanel = new JPanel(new MigLayout("gap 0, fill"));
     
     // Subsetting Sliders in a scroll pane
     JScrollPane scroller = new JScrollPane(sliderPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     add(scroller, "gap 0, grow, wrap");
     createSliders();
     
     // Button and URL
     JPanel bottom = new JPanel(new MigLayout("gap 0, fill"));
     url = new JTextField();
     bottom.add(url, "growx");
     
     JButton sub = new JButton("Submit");
     sub.addActionListener(new ActionListener() {
 
         public void actionPerformed(ActionEvent e) {
           updateURL();
         }
       });
     bottom.add(sub);
     
     add(bottom,"gap 0, growx, spanx 2");
   }
   
   private void createSliders() {
     variables = new ArrayList<ErddapVariableSubset>(erd.getVariables().size());
     for (ErddapVariable erv : erd.getVariables()) {
       ErddapVariableSubset evs = new ErddapVariableSubset(erv);
       variables.add(evs);
       sliderPanel.add(evs, "growx, wrap");
     }
   }
   
   private void updateURL() {
     
     ArrayList<String> selections = new ArrayList<String>();
     ArrayList<String> constraints = new ArrayList<String>();
     
     for (int i = 0 ; i < variables.size() ; i++) {
       if (variables.get(i).isSelected()) {
         // Add to the selection variables
         selections.add(erd.getVariables().get(i).getName());
         
         if (variables.get(i).getMin().equals(variables.get(i).getMax())) {
           constraints.add(erd.getVariables().get(i).getName() + "=" + variables.get(i).getMin());
         } else {
           // Did the user specify a range?
           if ((!variables.get(i).getMin().equals(erd.getVariables().get(i).getMin())) ||
               (!variables.get(i).getMax().equals(erd.getVariables().get(i).getMax()))) {
 
             // Add constraints to URL
             constraints.add(erd.getVariables().get(i).getName() + ">=" + variables.get(i).getMin());
             constraints.add(erd.getVariables().get(i).getName() + "<=" + variables.get(i).getMax());
           }
         }
       }
     }
     
     // Add the timeslider values
     constraints.add(erd.getTime().getName() + ">=" + dateSlider.getMinValue().toString("yyyy-MM-dd"));
     constraints.add(erd.getTime().getName() + "<=" + dateSlider.getMaxValue().toString("yyyy-MM-dd"));
 
     // Add the BBOX values
     constraints.add(erd.getX().getName() + ">=" + bboxGui.getBoundingBox().getLonMin());
     constraints.add(erd.getX().getName() + "<=" + bboxGui.getBoundingBox().getLonMax());
     constraints.add(erd.getY().getName() + ">=" + bboxGui.getBoundingBox().getLatMin());
     constraints.add(erd.getY().getName() + "<=" + bboxGui.getBoundingBox().getLatMax());
     
     
     String params = selections.toString().replace(" ","").replace("[","").replace("]","");
     params += "&";
     params += constraints.toString().replace(", ", "&").replace("[","").replace("]","");
     // Strip off final '&'
     if (params.endsWith("&")) {
       params = params.substring(0,params.length() - 1);
     }
 
     request.setBaseUrl(erd.getTabledap());
     request.setParameters(params);
    
     url.setText(request.getRequestUrl());
     
     javax.swing.SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         JFrame frame = new JFrame("Get Data");
         frame.setLayout(new MigLayout("fill"));
         frame.setPreferredSize(new Dimension(980, 400));
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         
         ErddapResponseSelectionPanel responsePanel = new ErddapResponseSelectionPanel("Available Response Formats");
         responsePanel.addPropertyChangeListener(new PropertyChangeListener() {
 
           public void propertyChange(PropertyChangeEvent evt) {
             request.setResponseFormat((String)evt.getNewValue());
           }
         });
         responsePanel.initComponents();
         
         
         JComponent newContentPane = new ErddapGetDataProgressMonitor(request);
         newContentPane.addPropertyChangeListener(new PropertyChangeListener() {
 
           public void propertyChange(PropertyChangeEvent evt) {
             if (evt.getPropertyName().equals("done")) {
               if (Configuration.CLOSE_AFTER_PROCESSING) {
                 parent.formWindowClose(evt.getNewValue().toString());
               }
             }
           }
         });
         newContentPane.setOpaque(true);
         request.setParent(frame);
         frame.add(responsePanel, "grow");
         frame.add(newContentPane, "grow");
         frame.pack();
         frame.setVisible(true);
       }
     });
     
   }
   
   public class ErddapDataRequest implements PropertyChangeListener {
 
     private String baseUrl;
     private String responseFormat;
     private String parameters;
     private PropertyChangeSupport pcs;
     private String homeDir;
     private JFrame parent;
     
     public ErddapDataRequest(String homeDir) {
       this.homeDir = homeDir;
       pcs = new PropertyChangeSupport(this);
     }
 
     public void setParent(JFrame parent) {
       this.parent = parent;
     }
     
     public void setResponseFormat(String responseFormat) {
       this.responseFormat = responseFormat;
     }
 
     public void setParameters(String parameters) {
       this.parameters = parameters;
     }
 
     public void setBaseUrl(String baseUrl) {
       this.baseUrl = baseUrl;
     }
 
     public String getRequestUrl() {
       return baseUrl + responseFormat + "?" + parameters;
     }
 
     public void propertyChange(PropertyChangeEvent evt) {
       pcs.firePropertyChange(evt);
     }
     
     public void addPropertyChangeListener(PropertyChangeListener l) {
       pcs.addPropertyChangeListener(l);
     }
 
     public void removePropertyChangeListener(PropertyChangeListener l) {
       pcs.removePropertyChangeListener(l);
     }
     
     public void getData() {
       File savePath = FileSaveUtils.chooseSavePath(parent, homeDir, baseUrl);
       String filename = FileSaveUtils.chooseFilename(savePath, "erddap_response" + responseFormat);
 
       Timer stopwatch = new Timer();
       File f = null;
               
       stopwatch.reset();
 
       int written = 0;
       try {
         URL u = new URL(getRequestUrl());
         pcs.firePropertyChange("message", null, "- Making Request (" + getRequestUrl() + ")");
         HttpURLConnection ht = (HttpURLConnection) u.openConnection();
         ht.setDoInput(true);
         ht.setRequestMethod("GET");
         InputStream is = ht.getInputStream();
         pcs.firePropertyChange("message", null, "- Streaming Results to File: " + filename);
         f = new File(filename);
         OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
         byte[] buffer = new byte[2048];
         int len = 0;
         written = 0;
         while (-1 != (len = is.read(buffer))) {
           output.write(buffer, 0, len);
           written += len;
         }
         is.close();
         output.flush();
         output.close();
       } catch (MalformedURLException e) {
         pcs.firePropertyChange("message", null, "- BAD URL, skipping sensor");
       } catch (IOException io) {
         pcs.firePropertyChange("message", null, "- BAD CONNECTION, skipping sensor");
       }
 
       pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
       pcs.firePropertyChange("progress", null, 100);
       if (f != null) {
         pcs.firePropertyChange("done", null, f.getAbsolutePath());
       }
     }
   }
   
 }
