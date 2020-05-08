 
 package wekaapp;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import weka.core.Attribute;
 import weka.classifiers.trees.J48;
 import weka.core.Instances;
 import weka.core.converters.ConverterUtils.DataSource;
 import weka.gui.treevisualizer.PlaceNode2;
 import weka.gui.treevisualizer.TreeVisualizer;
 
 /**
  *
  * @author sgolebiewski
  */
 public class MyWeka 
 {
     public static Instances LoadFile(String Path)
     {
         Instances data = null;
         DataSource source = null;
         
         try 
         {
             source = new DataSource(Path);
             data = source.getDataSet();
         } 
         catch (Exception ex) 
         {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         if (data.classIndex() == -1)
         data.setClassIndex(data.numAttributes() - 1);
         
         return data;
     }
     
     public static void GenerateDecisiveTree(Instances data, String[] options)
     {
         J48 cls = new J48();
         
         try 
        {            
             cls.setOptions(options);
             cls.buildClassifier(data);
             
             final JFrame jf = new JFrame("Drzewo decyzyjne");
             jf.setSize(500,400);
 
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             Dimension frameSize = jf.getSize();
         
             jf.setLocation((screenSize.width - frameSize.width)/2, (screenSize.height - frameSize.height)/2);
 
             jf.getContentPane().setLayout(new BorderLayout());
             TreeVisualizer tv = new TreeVisualizer(null, cls.graph(), new PlaceNode2());
             jf.getContentPane().add(tv, BorderLayout.CENTER);
             jf.addWindowListener(new java.awt.event.WindowAdapter() 
             {
                 public void windowClosing(java.awt.event.WindowEvent e) 
                 {
                     jf.dispose();
                 }
             });
  
             jf.setVisible(true);
             tv.fitToScreen();
             
         } 
         catch (Exception ex) 
         {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
