 package com.asascience.edc.erddap.gui;
 
 import cern.colt.Timer;
 import com.asascience.edc.Configuration;
 import com.asascience.edc.erddap.ErddapDataset;
 import com.asascience.edc.erddap.ErddapVariable;
 import com.asascience.edc.map.BoundingBoxPanel;
 import com.asascience.edc.gui.OpendapInterface;
 import com.asascience.edc.gui.jslider.JSlider2Date;
 import com.asascience.edc.map.WorldwindSelectionMap;
 import com.asascience.edc.utils.FileSaveUtils;
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
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.Logger;
 import ucar.nc2.units.DateUnit;
 
 /**
  * ErddapTabledapGui.java
  * 
  * @author Kyle Wilcox <kwilcox@asascience.com>
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
   private static Logger guiLogger = Logger.getLogger("com.asascience.log." + ErddapTabledapGui.class.getName());
   
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
     // Panel for bbox and timeslider
     JPanel mapControls = new JPanel(new MigLayout("gap 0, fillx"));
     
     if (erd.hasX() && erd.hasY()) {
       // Map
       mapPanel = new WorldwindSelectionMap(homeDir);
       mapPanel.addPropertyChangeListener(new PropertyChangeListener() {
 
         public void propertyChange(PropertyChangeEvent e) {
           String name = e.getPropertyName();
           // Bounding box was drawn
           if (name.equals("boundsStored")) {
             bboxGui.setBoundingBox(mapPanel.getSelectedExtent());
             updateURL();
           }
         }
       });
       mapStuff.add(mapPanel, "gap 0, grow, wrap");
     
       // BBOX panel
       bboxGui = new BoundingBoxPanel();
       // Set GUI to the datasets bounds
       bboxGui.setBoundingBox(Double.parseDouble(erd.getY().getMax()), Double.parseDouble(erd.getX().getMin()), Double.parseDouble(erd.getY().getMin()), Double.parseDouble(erd.getX().getMax()));
       bboxGui.addPropertyChangeListener(new PropertyChangeListener() {
 
         public void propertyChange(PropertyChangeEvent evt) {
           if (evt.getPropertyName().equals("bboxchange")) {
             mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
             updateURL();
           }
         }
       });
       mapControls.add(bboxGui, "gap 0, growx, bottom");
       
       // Add either the sensor layer, or the data extent layer
       if (erd.hasLocations()) {
         mapPanel.addSensors(erd.getLocations());
       } else {
         mapPanel.makeDataExtentLayer(bboxGui.getBoundingBox());
         mapPanel.makeSelectedExtentLayer(bboxGui.getBoundingBox());
       }
     }
 
     if (erd.hasTime()) {
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
       // getMax() was probably NaN returned from ERDDAP 
       if (!erd.getTime().hasMax()) {
         et = new Date();
       }
       dateSlider.setRange(st,et);
      // 10 days before end date
      //Date tempDate = new Date();
      //tempDate.setTime(et.getTime() - 1000*60*60*24*10);
      //dateSlider.setStartDate(tempDate);
      // Beginning of dataset
      dateSlider.setStartDate(st);
      
       dateSlider.addPropertyChangeListener(new PropertyChangeListener() {
 
         public void propertyChange(PropertyChangeEvent evt) {
           updateURL();
         }
       });
       timePanel.add(dateSlider, "gap 0, grow, center");
       mapControls.add(timePanel, "gap 0, growx, bottom");
     }
     mapStuff.add(mapControls, "gap 0, growx, bottom");
     add(mapStuff, "gap 0, grow");
     
     // Panel with subsetting sliders and such
     sliderPanel = new JPanel(new MigLayout("gap 0, fillx"));
     
     // Subsetting Sliders in a scroll pane
     JScrollPane scroller = new JScrollPane(sliderPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
           makeRequest();
         }
       });
     bottom.add(sub);
     
     add(bottom,"gap 0, growx, spanx");
    
    // Update URL from the start
    updateURL();
   }
   
   private void createSliders() {
     variables = new ArrayList<ErddapVariableSubset>(erd.getVariables().size());
     for (ErddapVariable erv : erd.getVariables()) {
       ErddapVariableSubset evs = new ErddapVariableSubset(erv);
       evs.addPropertyChangeListener(new PropertyChangeListener() {
 
         public void propertyChange(PropertyChangeEvent evt) {
           updateURL();
         }
       });
       variables.add(evs);
       sliderPanel.add(evs, "gap 0, growx, wrap");
     }
   }
   
   private void updateURL() {
     
     ArrayList<String> selections = new ArrayList<String>();
     ArrayList<String> constraints = new ArrayList<String>();
     
     for (int i = 0 ; i < variables.size() ; i++) {
       if (variables.get(i).isSelected()) {
         selections.add(erd.getVariables().get(i).getName());
         constraints.addAll(variables.get(i).toConstraints());
       }
     }
     
     // Add the Time values
     if (erd.hasTime()) {
       SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
       constraints.add(erd.getTime().getName() + ">=" + fmt.format(dateSlider.getStartDate()));
       constraints.add(erd.getTime().getName() + "<=" + fmt.format(dateSlider.getEndDate()));
     }
     
     // Add the X values
     if (erd.hasX()) {
       constraints.add(erd.getX().getName() + ">=" + bboxGui.getBoundingBox().getLonMin());
       constraints.add(erd.getX().getName() + "<=" + bboxGui.getBoundingBox().getLonMax());
     }
     
     // Add the Y values
     if (erd.hasY()) {
       constraints.add(erd.getY().getName() + ">=" + bboxGui.getBoundingBox().getLatMin());
       constraints.add(erd.getY().getName() + "<=" + bboxGui.getBoundingBox().getLatMax());
     }
     
     String params = selections.toString().replace(" ","").replace("[","").replace("]","");
     if (params.endsWith("&")) {
       params = params.substring(0,params.length() - 1);
     }
     params += "&";
     params += constraints.toString().replace(", ", "&").replace("[","").replace("]","");
     // Strip off final '&'
     if (params.endsWith("&")) {
       params = params.substring(0,params.length() - 1);
     }
 
     request.setBaseUrl(erd.getTabledap());
     request.setParameters(params);
    
     url.setText(request.getRequestUrl());
     
   }
   
   private void makeRequest() {
     javax.swing.SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         JFrame frame = new JFrame("Get Data");
         frame.setLayout(new MigLayout("fill"));
         frame.setPreferredSize(new Dimension(980, 400));
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         
         final ErddapGetDataProgressMonitor newContentPane = new ErddapGetDataProgressMonitor(request);
         newContentPane.addPropertyChangeListener(new PropertyChangeListener() {
 
           public void propertyChange(PropertyChangeEvent evt) {
             if (evt.getPropertyName().equals("done")) {
               if (Configuration.CLOSE_AFTER_PROCESSING) {
                 parent.formWindowClose(evt.getNewValue().toString());
               }
             }
           }
         });
 
         final ErddapResponseSelectionPanel responsePanel = new ErddapResponseSelectionPanel("Available Response Formats");
         responsePanel.addPropertyChangeListener(new PropertyChangeListener() {
 
           public void propertyChange(PropertyChangeEvent evt) {
             request.setResponseFormat((String)evt.getNewValue());
             newContentPane.update();
            updateURL();
           }
         });
         responsePanel.initComponents();
         
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
     private String responseFormat = ".htmlTable";
     private String parameters;
     private PropertyChangeSupport pcs;
     private String homeDir;
     private JFrame parent;
     private File saveFile;
     
     public ErddapDataRequest(String homeDir) {
       this.homeDir = homeDir;
       pcs = new PropertyChangeSupport(this);
     }
 
     public void setParent(JFrame parent) {
       this.parent = parent;
     }
 
     public JFrame getParent() {
       return parent;
     }
 
     public String getHomeDir() {
       return homeDir;
     }
 
     public String getBaseUrl() {
       return baseUrl;
     }
     
     public void setResponseFormat(String responseFormat) {
       this.responseFormat = responseFormat;
     }
 
     public void setParameters(String parameters) {
       this.parameters = parameters;
     }
 
     public File getComputedSaveFile() {
       saveFile = new File(FileSaveUtils.chooseFilename(new File(homeDir + File.separator + FileSaveUtils.getNameAndDateFromUrl(baseUrl) + File.separator), erd.getId() + responseFormat));
       return saveFile;
     }
     
     public File getUpdatedSaveFile() {
       return new File (FileSaveUtils.getFilePathNoSuffix(saveFile.getAbsolutePath()) + responseFormat);
     }
     
     public File getSaveFile() {
      try {
        return getUpdatedSaveFile();
      } catch(NullPointerException npe) {
        return null;
      }
     }
 
     public void setSaveFile(File saveFile) {
       this.saveFile = saveFile;
     }
     
     public String getFilename() {
       return saveFile.getName();
     }
     
     public void setBaseUrl(String baseUrl) {
       this.baseUrl = baseUrl;
     }
 
     public String getResponseFormat() {
       return responseFormat;
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
       if (Configuration.DISPLAY_TYPE == Configuration.DisplayType.ESRI && responseFormat.equals(".esriCSV")) {
         responseFormat = ".csv";
       }
 
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
         pcs.firePropertyChange("message", null, "- Streaming Results to File: " + getSaveFile().getAbsolutePath());
         f = getSaveFile();
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
        pcs.firePropertyChange("message", null, "- NO DATA available for selected range");
       }
 
       pcs.firePropertyChange("message", null, "- Completed " + written + " bytes in " + stopwatch.elapsedTime() + " seconds.");
       pcs.firePropertyChange("progress", null, 100);
       if (f != null) {
         pcs.firePropertyChange("done", null, f.getAbsolutePath());
       }
     }
   }
   
 }
