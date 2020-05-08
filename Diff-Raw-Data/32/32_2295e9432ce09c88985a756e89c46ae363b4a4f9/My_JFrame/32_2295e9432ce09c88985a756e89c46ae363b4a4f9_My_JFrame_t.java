 package Testing;
 
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JFileChooser;
 
 // Die Klasse My_JFrame erbt Eigenschaften von JFrame
 public class My_JFrame extends JFrame {
 
 	// Variablendeklaration
 	FileWriter writer; // Klasse, um ind die XML-Datei schreiben zu knnen
 	File file; // Dokument
 	JTextField path;
 	JComboBox cb_freigabe, cb_satzstatus, cb_sieger, cb_gegnerzug;
 	String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"; // Header
 																	// Information
 																	// fr das
 																	// XML-Dokument
 	JFileChooser fc; // Objekt, um den Dateiexplorer ffnen zu knnen
 	String file_path;
 
 	public My_JFrame() {
 		super("XML-Creator");
 		this.setSize(300, 300);
 		Container content = this.getContentPane();
 		content.setLayout(new GridLayout(0, 2, 2, 4));
 		this.create_elements(content);
 
 		// Programm wird beim Schlieen des Fensters beendet
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	/**
 	 * Generierung eines Textdokuments im XML-Format
 	 * 
 	 * @param freigabe
 	 *            bergabeparameter fr die Dropdownbox "Freigabe"
 	 * @param satzstatus
 	 *            bergabeparameter fr die Dropdownbox "Satzstatus"
 	 * @param gegnerzug
 	 *            bergabeparameter fr die Dropdownbox "Gegnerzug"
 	 * @param sieger
 	 *            bergabeparameter fr die Dropdownbox "Sieger"
 	 * @throws IOException
 	 */
 	public void generate_XML(String freigabe, String satzstatus,
 			String gegnerzug, String sieger) throws IOException {
 
 		// erstellt das Dokument in dem vorgegebenen Pfad
 		file = new File(file_path);
 
 		// Erstellt eine neues Dokument. Ist bereits ein Dokument vorhande, wird
 		// es gelscht
 		writer = new FileWriter(file, false);
 		// Zwischenspeicherung der Eingaben
 
 		// XML zusammensetzen
 		writer.write("<content>" + header + "<freigabe>"
 				+ cb_freigabe.getSelectedItem() + "</freigabe>"
 				+ "<satzstatus>" + cb_satzstatus.getSelectedItem()
 				+ "</status>" + "<gegnerzug>" + cb_gegnerzug.getSelectedItem()
 				+ "</gegnerzug>" + "<sieger>" + cb_sieger.getSelectedItem()
 				+ "</sieger>" + "</content>");
 
 		writer.close();
 		System.out.println(cb_freigabe.getSelectedItem());
 	}
 
 	/**
 	 * Erstellt die Elemente Eingabefeld, Dropdownbox, Button und den
 	 * Filechooser in dem Frame
 	 * 
 	 * @param cont
 	 *            bergabeparameter von der Content-Pane
 	 */
 	public void create_elements(Container cont) {
 
 		JLabel lb_path = new JLabel("Pfad:");
 		cont.add(lb_path);
 
 		path = new JTextField();
 		cont.add(path);
 
 		// erstellt Label und das zugehrige Dropdownbox
 		JLabel lb_freigabe = new JLabel("Freigabe");
 		cont.add(lb_freigabe);
 
 		// Bereich fr die Dopdownbox
 		String[] choose_freigabe = { "true", "false" };
 		cb_freigabe = new JComboBox(choose_freigabe);
 		// Startwert wird gewhlt
 		cb_freigabe.setSelectedIndex(0);
 		cont.add(cb_freigabe);
 		// Ausgewhlten Datensatz wird in einen String umwandelt
 		final String temp_freigabe = (String) cb_freigabe.getSelectedItem();
 
 		// analog zu freigabe
 		JLabel lb_satzstatus = new JLabel("Satzstatus");
 		cont.add(lb_satzstatus);
 
 		String[] choose_satzstatus = { "Satz spielen", "beendet" };
 		cb_satzstatus = new JComboBox(choose_satzstatus);
 		cb_satzstatus.setSelectedIndex(0);
 		cont.add(cb_satzstatus);
 		final String temp_satzstatus = (String) cb_satzstatus.getSelectedItem();
 
 		// analog zu freigabe
 		JLabel lb_gegnerzug = new JLabel("Gegnerzug");
 		cont.add(lb_gegnerzug);
 
		String[] choose_gegnerzug = { "-1", "1", "2", "3", "4", "5", "6" };
 		cb_gegnerzug = new JComboBox(choose_gegnerzug);
 		cb_gegnerzug.setSelectedIndex(0);
 		cont.add(cb_gegnerzug);
 		final String temp_gegnerzug = (String) cb_gegnerzug.getSelectedItem();
 
 		// analog zu freigabe
 		JLabel lb_sieger = new JLabel("Sieger");
 		cont.add(lb_sieger);
 
 		String[] choose_sieger = { "offen", "SpielerX", "SpielerO" };
 		cb_sieger = new JComboBox(choose_sieger);
 		cb_sieger.setSelectedIndex(0);
 		cont.add(cb_sieger);
 		final String temp_sieger = (String) cb_sieger.getSelectedItem();
 
 		// Button zum Erstellen des XMLs
 		JButton create = new JButton("creat XML");
 		create.setSize(20, 100);
 		cont.add(create);
 		JButton test = new JButton("open");
 
 		// Button-Click-Event wird erstellt
 		create.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				// Aufruf der Methode generate_XML mit den eingabefeldern als
 				// Parameter
 				try {
 					generate_XML(temp_freigabe, temp_satzstatus,
 							temp_gegnerzug, temp_sieger);
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		// Button, um den Pfad der zu erstellenden Excel anzugeben
 		test.setSize(20, 100);
 		cont.add(test);
 		test.addActionListener(new ActionListener() {
 
 			// Dialog zum angeben eines Pfades wird geffnet
 			public void actionPerformed(ActionEvent e) {
 				fc = new JFileChooser();
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int dialog = fc.showOpenDialog(My_JFrame.this);
 				if (dialog == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					// Pfad des angegebenen Orders wird gespeichert
 					String directory_path = file.getPath();
 					// Pfad wird im den Dateinamen erweitert
 					file_path = directory_path + "\\server_xml.txt";
 					path.setText(directory_path);
 				} else {
 					System.out.println("keine Auswahl");
 					path.setText("kein Ordner gewhlt");
 				}
 
 			}
 		});
 
 	}
 
 }
