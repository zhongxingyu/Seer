 package ui;
 
 
 import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
import javax.swing.AbstractAction;
import javax.swing.Action;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
import javax.swing.JSeparator;
 
 import main.MainPreProc;
 
 /**
  * wartet auf Menue events und baut das Menue auf
  */
 public class MenuHandler implements ActionListener{
 
 	private final MainFrame parentFrame;	//Referenz auf Hauptfenster
 	private JMenuBar menuBar;				//MenueLeiste
 	private JMenu open;						//Einzelne Menuepunkte auf der MenueLeiste
 	private JMenu options;
 	private JMenu help;
 	private JMenuItem mgsave;				//einzelne Menuepunkte in Menuepunkten
 	private JMenuItem osm;
 	private JMenuItem exit;
 	
 	private JMenuItem jDoc;
 	private JMenuItem shortDoc;
 	
 	/**
 	 * 
 	 * @param parentFrame Referenz auf Hauptfenster
 	 */
 	public MenuHandler(MainFrame parentFrame){
 		this.parentFrame = parentFrame;
 	}
 
 	/**
 	 * baut das menue auf
 	 * @return JMenuBar fuer das MainFrame
 	 */
 	public JMenuBar buildMenu(){
 		menuBar = new JMenuBar();
 
 		open = new JMenu("oeffnen");
 		options = new JMenu("Optionen");
 		help = new JMenu("Hilfe");
 
 		mgsave = new JMenuItem("gepreprocesste Karte oeffnen");
 		osm = new JMenuItem("*.osm preprocessen");
 		exit = new JMenuItem("Beenden");
				
 		jDoc = new JMenuItem("JavaDoc");
 		shortDoc = new JMenuItem("Kurzdoku");
 
 		mgsave.addActionListener(this);
 		osm.addActionListener(this);
 		exit.addActionListener(this);
 
 		shortDoc.addActionListener(this);
 		jDoc.addActionListener(this);
 		
 		open.add(mgsave);
 		open.add(osm);
 
 		menuBar.add(open);
 		menuBar.add(options);
 		menuBar.add(help);
 		menuBar.add(exit);
 		
		//Scaling the exit-button
		Dimension dim = exit.getPreferredSize();
		dim.height = exit.getMaximumSize().height;
		exit.setMaximumSize(dim);

		
 		help.add(shortDoc);
 		help.add(jDoc);
 		
 
 		return menuBar;
 	}
 
 	/**
 	 * Fuehrt je nach gewaehltem Menuepunkt eine entsprechende Option durch
 	 * ActionEvent ae Zur Bestimmung, welcher Menuepukt angeklickt wurde
 	 */
 	@Override
 	public void actionPerformed(ActionEvent ae){
 		if(ae.getSource().equals(mgsave)){		//Wenn gepreprocesste Karte geladen werden soll
 			final File defFile = new File("file");
 			final JFileChooser fChooser = new JFileChooser(defFile);
 			final int returnVal = fChooser.showOpenDialog(parentFrame); //Zeige Dialog zum Auswaehlen einer Datei
 			final File f = fChooser.getSelectedFile();
 			if(returnVal == JFileChooser.APPROVE_OPTION)
 				if(f.getName().endsWith(".tiles"))		//Wenn .tiles Datei ausgewaehlt
 					parentFrame.getMapPanel().openMapFile(f);
 				else JOptionPane.showMessageDialog(parentFrame, "Bitte waehlen Sie die .tiles Datei aus!");
 			parentFrame.repaint();
 		}
 
 		if(ae.getSource().equals(osm)){		//Wenn OSM gepreprocesst werden soll
 			final File defFile = new File("file");
 			final JFileChooser fChooser = new JFileChooser(defFile);
 			final int returnVal = fChooser.showOpenDialog(parentFrame);		//Zeige Dialog zum Auswaehlen einer Datei
 			final File f = fChooser.getSelectedFile();
 			if(returnVal == JFileChooser.APPROVE_OPTION)
 				if(f.getName().endsWith(".osm")){ //Wenn .osm Datei ausgewaehlt
 					try {
 						MainPreProc.main(f);
 					} catch (final Exception e) {
 						e.printStackTrace();
 					}
 					parentFrame.getMapPanel().openMapFile(new File(f.getPath()+".mapfiles"+File.separator+"ProcessedTilesConfig.tiles"));
 				}
 				else JOptionPane.showMessageDialog(parentFrame, "Bitte waehlen Sie eine .osm Datei aus!");
 
 			parentFrame.repaint();
 		}
 
 		if(ae.getSource().equals(exit)){		//Wenn Programm beendet werden soll
 			final int answer =
 				JOptionPane.showConfirmDialog(parentFrame, "Wirklich beenden?", "Sind Sie sich sicher?", JOptionPane.OK_CANCEL_OPTION);
 			if(answer==JOptionPane.OK_OPTION)
 				System.exit(0);
 		}
 		
 		if(ae.getSource().equals(jDoc)){
 			Desktop d = Desktop.getDesktop();
 			try {
 				d.open(new File("doc"+File.separatorChar+"index.html"));
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(null,"JavaDoc konnte nciht geoeffnet werden","Fehler", JOptionPane.CANCEL_OPTION);
 			}
 		}
 		
 		if(ae.getSource().equals(shortDoc)){
 			Desktop d = Desktop.getDesktop();
 			try {
 				d.open(new File("doc"+File.separatorChar+"doku.pdf"));
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(null,"Doku konnte nciht geoeffnet werden","Fehler", JOptionPane.CANCEL_OPTION);
 			}
 		}
 	}
 
 }
