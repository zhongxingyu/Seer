 package uk.ac.kcl.informatics.opmbuild.format.gviz;
 
 import java.awt.Component;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.prefs.Preferences;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import uk.ac.kcl.informatics.opmbuild.Graph;
 import uk.ac.kcl.informatics.opmbuild.GraphElement;
 
 public class ImageWriter {
 
     private static File _command = null;
     private File _output;
     private String _format;
     private Component _gui;
     private boolean _numberEntities;
 
     public ImageWriter (File output) {
         this (output, extension (output));
     }
 
     public ImageWriter (File output, String format) {
         _output = output;
         _format = format;
         _gui = null;
         _numberEntities = false;
     }
 
     private File chooseCommand () throws GVizFormatException {
         JFileChooser select = new JFileChooser ();
         int choice;
 
         if (_gui == null) {
             _gui = new JFrame ();
         }
         choice = JOptionPane.showConfirmDialog (_gui, "Please locate the GraphViz dot executable.\nYou will only be asked to do this once.", "Please locate dot", JOptionPane.OK_CANCEL_OPTION);
         if (choice == JFileChooser.CANCEL_OPTION || choice == JFileChooser.ERROR_OPTION) {
             throw new GVizFormatException ("Must choose a dot command to format");
         }
         choice = select.showOpenDialog (_gui);
         if (choice == JFileChooser.CANCEL_OPTION || choice == JFileChooser.ERROR_OPTION) {
             throw new GVizFormatException ("Must choose a dot command to format");
         }
 
         return select.getSelectedFile ();
     }
 
     private void execute (File gviz) throws GVizFormatException {
         try {
             String in = gviz.getAbsolutePath ();
             String out = _output.getAbsolutePath ();
             String command = null;
             if(System.getProperty("os.name").startsWith("Win"))
                 command = quoted(command);
             else command = getCommand (false).getAbsolutePath ();
                       
 
             ProcessBuilder builder = new ProcessBuilder (command, "-T" + _format, quoted ("-o" + out), quoted (in));
             
             Process process = builder.start ();
             process.waitFor ();
         } catch (InterruptedException ex) {
             throw new GVizFormatException (ex);
         } catch (IOException ex) {
             throw new GVizFormatException (ex);
         }
     }
 
     private static String extension (File file) {
         String name = file.getName ();
         int dot = name.lastIndexOf (".");
 
         return name.substring (dot + 1);
     }
 
     private File getCommand (boolean override) throws GVizFormatException {
         if (!override && _command != null) {
             return _command;
         }
 
         Preferences preferences = Preferences.userNodeForPackage (this.getClass ());
         String path = preferences.get ("command", null);
 
         if (override || path == null) {
             _command = chooseCommand ();
             preferences.put ("command", _command.getAbsolutePath ());
         } else {
             _command = new File (path);
             if (!_command.exists ()) {
                 _command = chooseCommand ();
                 preferences.put ("command", _command.getAbsolutePath ());
             }
         }
 
         return _command;
     }
 
     public void resetCommand () throws GVizFormatException {
         getCommand (true);
     }
 
     public void setGUI (Component gui) {
         _gui = gui;
     }
 
     public void setNumberEntities (boolean flag) {
         _numberEntities = flag;
     }
 
     private static String quoted (String unquoted) {
         return "\"" + unquoted + "\"";
     }
 
     public List<GraphElement> write (Graph graph) throws GVizFormatException {
         try {
             File gviz = File.createTempFile ("image", "gviz");
             GVizWriter writer = new GVizWriter (gviz);
             List<GraphElement> elements;
 
             writer.setNumberEntities (_numberEntities);
             elements = writer.write (graph);
             writer.close ();
             execute (gviz);
 
             return elements;
         } catch (IOException ex) {
             throw new GVizFormatException (ex);
         }
     }
 }
