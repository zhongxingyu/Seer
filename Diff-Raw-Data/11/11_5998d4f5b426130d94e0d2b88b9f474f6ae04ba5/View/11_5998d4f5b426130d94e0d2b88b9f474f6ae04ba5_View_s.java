 package view;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.FlowLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JTextArea;
 import javax.swing.SpringLayout;
 import javax.swing.border.TitledBorder;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import net.iharder.dnd.FileDrop;
 
 import model.ReadPDF;
 
 /**
  * 
  * View.java
  * 
  * 
  * @author Fadi M.H.Asbih
  * @email fadi_asbih@yahoo.de
  * @version 1.2.0  04/02/2012
  * @copyright 2012
  * 
  * TERMS AND CONDITIONS:
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 public class View extends JFrame implements ActionListener {
 
         /**
          * 
          */
         private static final long serialVersionUID = 6177350218996491783L;
         private JButton bug;
         private JEditorPane status;
         private String path;
         private ReadPDF pdf;
         public JProgressBar progressBar;
         public Desktop d;
         
         private InputPanel ip;
 
         public View(final ReadPDF pdf) throws Exception {
                 this.setPdf(pdf);
 
                 this.setTitle("LUH Notenspiegel Rechner");
                 this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 
                 JPanel panel = new JPanel(); //Main Panel
                 
                 ip = new InputPanel(this, pdf);
                 
                 panel.setLayout(new BorderLayout());
                 panel.add(ip, BorderLayout.NORTH);
                 
                 this.add(panel);
                 this.pack();
                 this.setSize(310, 230);
                           
                 // Output
                 bug = new JButton();
                 progressBar = new JProgressBar();
                 status = new JEditorPane();
                 status.setEditable(false);
                status.setText(" Notenspiegel einfach hier ziehen geht auch :-)\n\n LUH-NR\n Version 1.2.0\n 04.02.2012");
                 status.setForeground(Color.black.darker());
                 panel.add(status);
                 panel.add(progressBar, BorderLayout.AFTER_LAST_LINE);
 
                 this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 this.setLocationRelativeTo(null);
                 this.setResizable(false);
 
                 
                 this.setVisible(true);
                 bug.addActionListener(this);
                 
                 if(!d.isDesktopSupported())
                         bug.setEnabled(false);
                 
                 //TESTING Drag n Drop
                 new  FileDrop(panel, new FileDrop.Listener()
                 {   public void  filesDropped( java.io.File[] files )
                     {   
                 	setPath(files[0].getAbsolutePath());
                 	try {
 						pdf.ReadPDF(getPath());
 						getStatus().setText(
 							 " "+ pdf.getSubject()+
 							 "\n " + pdf.getCertificate()+
 							 "\n Anzahl gesamte Fächer: "+pdf.getNumberOfSubjects()+
        						 "\n Anzahl benotete Fächer: "+pdf.getNumberOfSubjectsWithGrade()+
        						 "\n Credit Points: "+pdf.getCredits()+
        						 "\n Note: "+pdf.getFinalGrade()+
        						 "\n Abschlussarbeit starten: "+pdf.getStartThesis()+
        						 "\n Studium Geschafft in Prozent... ");
 
 						getStatus().setForeground(Color.black.darker());
 						progressBar.setIndeterminate(false);
 						progressBar.setValue((int)pdf.getProcent());
 						progressBar.setStringPainted(true);
 					} catch (Exception e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                         getStatus().setText("ERROR");
                         getStatus().setForeground(Color.red.darker());
 					}
                     }   // end filesDropped
                 }); // end F
         }
 
         public void actionPerformed(ActionEvent e) {
                 if (e.getActionCommand().equals("Bug/Issue Report")) {
                                 try {
                                          
                                                 URI u;
                                                 d = Desktop.getDesktop();
                                                 u = new URI("http://code.google.com/p/luh-nr/issues/list");
                                                 d.browse(u); 
                                         
                                 } catch (URISyntaxException e1) {
                                         // TODO Auto-generated catch block
                                         e1.printStackTrace();
                                         this.getStatus().setText("ERROR");
                                         this.getStatus().setForeground(Color.red.darker());
                                 } catch (IOException e2) {
                                         // TODO Auto-generated catch block
                                         e2.printStackTrace();
                                         this.getStatus().setText("ERROR");
                                         this.getStatus().setForeground(Color.red.darker());
                                 }
                 }
         }
 
         public String getPath() {
                 return path;
         }
 
         public void setPath(String path) {
                 this.path = path;
         }
         
         public JEditorPane getStatus() {
                 return status;
         }
 
         public void setStatus(JEditorPane status) {
                 this.status = status;
         }
 
 		public ReadPDF getPdf() {
 			return pdf;
 		}
 
 		public void setPdf(ReadPDF pdf) {
 			this.pdf = pdf;
 		}
 }
