 package gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 public class Reader implements ListSelectionListener{
 
     private JFrame f = new JFrame("Reader"); //create Frame
     int maxLines = 1000;
 
 	// Buttons some there is something to put in the panels
     private JList ls;
     private JEditorPane txtEn = new JEditorPane();
     private JEditorPane txtFr = new JEditorPane();
 
     // Menu
     private JMenuBar mb = new JMenuBar(); // Menubar
     private JMenu mnuFile = new JMenu("File"); // File Entry on Menu bar
     private JMenuItem mnuItemQuit = new JMenuItem("Quit"); // Quit sub item
     private JMenu mnuHelp = new JMenu("Help"); // Help Menu entry
     private JMenuItem mnuItemAbout = new JMenuItem("About"); // About Entry
 	HashMap<String, String> en;
 	HashMap<String, String> fr;
 	
 
     public Reader() throws JDOMException, IOException{
     	en = loadXML("corpus/preprocess/format/afp_eng_200502");
     	fr = loadXML("corpus/preprocess/format/afp_fre_200502");
     	
 		// Set menubar
         f.setJMenuBar(mb);
         final JFileChooser fc = new JFileChooser();
         fc.setCurrentDirectory(new File("."));
         File match = null;
         int returnVal = fc.showOpenDialog(f);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             match = fc.getSelectedFile();
         } else {
         	System.exit(-1);
         }
         final FileReader match_reader = new FileReader(match);
 		//Build Menus
         mnuFile.add(mnuItemQuit);  // Create Quit line
         mnuHelp.add(mnuItemAbout); // Create About line
         mb.add(mnuFile);        // Add Menu items to form
         mb.add(mnuHelp);
 
         final DefaultListModel listModel = new DefaultListModel();
         ls = new JList(listModel);
         ls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         ls.addListSelectionListener(this);
         //ls.setVisibleRowCount(5);
         JScrollPane listScrollPane = new JScrollPane(ls);
         listScrollPane.setMinimumSize(new Dimension(100,100));
         listScrollPane.setPreferredSize(new Dimension(100,2000));
         
         Thread listener = new Thread(new Runnable() {
 
         	public void run() {
         		try {
         			BufferedReader br = new BufferedReader(match_reader);
         			int count = 0;
        			while (true) {
         				//System.out.println(in.hasNextLine());
         				String l;
         				if ((l = br.readLine()) != null) {
        					if (! l.startsWith("AFP")) {
        						count++;
         						continue;
        					}
        					
         					listModel.addElement(l);
        					if (count >= maxLines)
        						break;
         				}
         			}
         		} catch (Exception e) {
         			e.printStackTrace();
         			System.exit(-1);
         		}
         	}
 
         });
         listener.start();
         // Add Buttons
         //pnlWest.add(txtFr);
         //pnlCenter.add(txtEn);
         
         // Setup Main Frame
         f.getContentPane().setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.gridx = 0; c.gridy = 0;
         c.fill = GridBagConstraints.VERTICAL;
         c.ipadx = 10; c.ipady = 10;
         c.weightx = 0;
         c.insets = new Insets(5,10,5,10);
         f.getContentPane().add(listScrollPane, c);
 		c = new GridBagConstraints();
         c.gridx = 1; c.gridy = 0;
         c.fill = GridBagConstraints.BOTH;
         c.insets = new Insets(5,10,5,10);
         //c.ipadx = 10; c.ipady = 10;
         c.weightx = 0.45; 
         c.weighty = 1.0;
         JScrollPane jspEn = new JScrollPane(txtEn);
         f.getContentPane().add(jspEn, c);
 		c = new GridBagConstraints();
         c.gridx = 2; c.gridy = 0;
         c.fill = GridBagConstraints.BOTH;
         c.insets = new Insets(5,10,5,10);
         //c.ipadx = 10; c.ipady = 10;
         c.weightx = 0.45;
         JScrollPane jspFr = new JScrollPane(txtFr);
 		f.getContentPane().add(jspFr, c);
         
 		// Allows the Swing App to be closed
         f.addWindowListener(new ListenCloseWdw());
 		
 		//Add Menu listener
         mnuItemQuit.addActionListener(new ListenMenuQuit());
     }
 	
     private HashMap<String, String> loadXML(String f) throws JDOMException, IOException {
 		HashMap<String, String> ret = new HashMap<String, String>();
 		SAXBuilder builder = new SAXBuilder();
 		FileInputStream fis = new FileInputStream(f);
 		InputStreamReader isr = new InputStreamReader(fis, "UTF8");
 		Document document = builder.build(isr);
 		
 		Element root = document.getRootElement();
 		List docs = root.getChildren("DOC");
 		for (int i = 0; i < docs.size(); i++) {
 			String doc_id = ((Element) docs.get(i)).getAttribute("id").getValue();
 			if (! ((Element) docs.get(i)).getAttribute("type").getValue().equals("story")) {
 				//System.err.println(((Element) docs.get(i)).getAttribute("type").getValue());
 				continue;
 			}
 			//System.err.println(((Element) docs.get(i)).getAttribute("type").getValue());
 			Element doc_text = ((Element) docs.get(i)).getChild("TEXT");
 			List paragraphs = doc_text.getChildren("P");
 			StringBuffer txt2 = new StringBuffer();
 			for (int j = 0; j < paragraphs.size(); j++) {
 				txt2.append(((Element) paragraphs.get(j)).getText());
 			}
 			String txt = doc_text.getText();
 			if (txt2.length() > txt.length())
 				txt = txt2.toString();
 			if (txt.trim().length() == 0)
 				continue;
 			txt = txt.trim();
 			txt = txt.replaceAll(" [ ]*", " ");
 			ret.put(doc_id, txt);
 		}
 		return ret;
 	}
 
 	public class ListenMenuQuit implements ActionListener{
         public void actionPerformed(ActionEvent e){
             System.exit(0);         
         }
     }
 	
     public class ListenCloseWdw extends WindowAdapter{
         public void windowClosing(WindowEvent e){
             System.exit(0);         
         }
     }
 	
     public void launchFrame(){
         // Display Frame
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //f.pack(); //Adjusts panel to components for display
 		f.setSize(new Dimension(800,600));
         f.setVisible(true);
     }
     
     public static void main(String args[]) throws JDOMException, IOException{
         Reader gui = new Reader();
         gui.launchFrame();
     }
 
 	public void valueChanged(ListSelectionEvent e) {
 		if (e.getSource() == ls) {
 			String id = (String) ls.getSelectedValue();
 			String[] ids = id.split("-");
 			txtEn.setText(en.get(ids[0].trim()));
 			txtFr.setText(fr.get(ids[1].trim()));
 		}
 	}
 }
