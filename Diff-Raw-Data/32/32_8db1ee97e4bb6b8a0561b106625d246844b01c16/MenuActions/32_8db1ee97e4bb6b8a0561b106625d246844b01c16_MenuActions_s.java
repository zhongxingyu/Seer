 package com.intelix.digihdmi.app.actions;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.model.Connector;
 import com.intelix.digihdmi.model.Device;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Enumeration;
import java.util.Iterator;
 import javax.swing.ActionMap;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.Task;
 
 public class MenuActions {
 
     ActionMap actionMap = null;
 
     public MenuActions() {
         actionMap = Application.getInstance().getContext().getActionMap(this);
     }
 
     @Action
     public void menuDevice() {
     }
 
     @Action
     public Task resetCache() {
         return new ResetCacheTask(Application.getInstance());
     }
 
     @Action
     public void onDeviceSettings() {
         ((DigiHdmiApp) Application.getInstance()).showConnectionDlg();
     }
 
     private class ResetCacheTask extends Task {
 
         ResetCacheTask(Application app) {
             super(app);
         }
 
         @Override
         protected Object doInBackground() throws Exception {
             ((DigiHdmiApp) getApplication()).getDevice().setFullReset(true);
             setMessage("Cache Reset.");
             return null;
         }
     }
 
     @Action
     public void onFileSave() {
         // Get a file location from the user
         JFileChooser fc = new JFileChooser();
         int result = fc.showSaveDialog(((DigiHdmiApp) Application.getInstance()).getMainFrame());
         if (result == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
 
             // Create the serializer
             XStream xstream = new XStream(new DomDriver());
             xstream.autodetectAnnotations(true);
 
             // setup aliases
             //...
 
             // get the xml string
             String xml = xstream.toXML(((DigiHdmiApp) Application.getInstance()).getDevice());
 
             try {
                 if (!file.exists()) {
                     file.createNewFile();
                 }
                 if (file.canWrite()) {
                     Writer w = new BufferedWriter(
                             new FileWriter(file));
                     w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n");
                     w.write(xml);
                     w.close();
                 }
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(((DigiHdmiApp) Application.getInstance()).getMainFrame(),
                         "Can't save " + file.getAbsolutePath() + " to disk.\n\nDetails:\n" + ex.getMessage(),
                         "Fail!", JOptionPane.ERROR_MESSAGE);
             }
 
         }
     }
 
     @Action
     public void onFileLoad() {
         JFileChooser fc = new JFileChooser();
         int result = fc.showOpenDialog(((DigiHdmiApp) Application.getInstance()).getMainFrame());
         if (result == JFileChooser.APPROVE_OPTION) {
             XStream xstream = new XStream(new DomDriver());
             xstream.autodetectAnnotations(true);
             
             File f = fc.getSelectedFile();
             try {
                 Reader r = new FileReader(f);
                 Device d = (Device) xstream.fromXML(r);
                 d.init();
                 ((DigiHdmiApp)Application.getInstance()).addDeviceListener(d);
                 // Create PropertyChange stuff
                 Enumeration<Connector>[] allConnectors = new Enumeration[] {d.getInputs(),d.getOutputs()};
                 for (Enumeration<Connector> cxns : allConnectors)
                 {
                     while(cxns.hasMoreElements())
                     {
                         Connector ctor = cxns.nextElement();
                         ctor.init();
                         ctor.addPropertyChangeListener(d);
                     }
                 }
 
                 ((DigiHdmiApp) Application.getInstance()).setDevice(d);
                 d.setFullReset(true);
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(((DigiHdmiApp) Application.getInstance()).getMainFrame(),
                         "Can't load " + f.getAbsolutePath() + " from disk.\n\nDetails:\n" + ex.getMessage(),
                         "Fail!", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 }
