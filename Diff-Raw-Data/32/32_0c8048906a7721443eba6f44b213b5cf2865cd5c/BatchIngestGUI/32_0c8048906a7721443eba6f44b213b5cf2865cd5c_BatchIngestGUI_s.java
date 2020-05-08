 package fedora.client;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JRadioButton;
 import javax.swing.ButtonGroup;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import fedora.client.Administrator;
 import java.io.File;
 import javax.swing.JFileChooser;
 import java.util.Properties;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.FontMetrics;
 import javax.swing.Box;
 import javax.swing.JFrame;
 import javax.swing.JComponent;
 import fedora.swing.mdi.MDIDesktopPane;
 
 /**
  *
  * <p><b>Title:</b> BatchIngesGUI.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author wdn5e@virginia.edu
  * @version $Id$
  */
 public class BatchIngestGUI
         extends JInternalFrame {
 
         //private static File s_lastDir;
 	private JTextField m_objectsField=new JTextField("", 10);
 	private JTextField m_pidsField=new JTextField("", 10);
 
 	private JRadioButton m_xmlMap = new JRadioButton("xml");
 	private JRadioButton m_textMap = new JRadioButton("text");
 	private ButtonGroup buttonGroup = new ButtonGroup();
 
 	private Dimension unitDimension = null;
 	private Dimension browseMin = null;
 	private Dimension browsePref = null;
 	private Dimension browseMax = null;
 	private Dimension textMin = null;
 	private Dimension textPref = null;
 	private Dimension textMax = null;
 	private Dimension okMin = null;
 	private Dimension okPref = null;
 	private Dimension okMax = null;
 
 	private MDIDesktopPane mdiDesktopPane = null;
 	BatchOutput batchOutput = new BatchOutput("Batch Ingest Output");
 
 	private final String host;
 	private final String port;
 	private final String user;
 	private final String pass;
 
     public BatchIngestGUI(JFrame parent, MDIDesktopPane mdiDesktopPane, String host, int port, String user, String pass) {
         super("Batch Ingest",
               true, //resizable
               true, //closable
               true, //maximizable
               true);//iconifiable
 
 	this.host = host;
 	this.port = Integer.toString(port);
 	this.user = user;
 	this.pass = pass;
 
 	this.mdiDesktopPane = mdiDesktopPane;
 
         JButton btn=new JButton("Ingest this batch");
         btn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 ingestBatch();
             }
         });
         JPanel entryPanel=new JPanel();
         entryPanel.setLayout(new BorderLayout());
         entryPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         entryPanel.add(new JLabel("Ingest Criteria"), BorderLayout.NORTH);
         JPanel labelPanel=new JPanel();
         labelPanel.setLayout(new GridLayout(0, 3));
 
 	Graphics graphicsTemp = parent.getGraphics();
 	FontMetrics fmTemp = graphicsTemp.getFontMetrics();
 	int maxWidth = 0; {
 		int[] temp = fmTemp.getWidths();
 		for (int i = 0; i < temp.length; i++) {
 			if (temp[i] > maxWidth) {
 				maxWidth = temp[i];
 			}
 		}
 	}
 	unitDimension = new Dimension((new Float(1.5 * maxWidth)).intValue(),fmTemp.getHeight());
 	browseMin = new Dimension(12*unitDimension.width,unitDimension.height); // 9*unitDimension.width
 	browseMax = new Dimension(2 * browseMin.width,2 * browseMin.height);
 	browsePref = browseMin;
 
 	textMin = new Dimension(22*unitDimension.width,unitDimension.height);
 	textMax = new Dimension(2 * textMin.width,2 * textMin.height);
 	textPref = textMin;
 
 	okMin = new Dimension(9*unitDimension.width,unitDimension.height);
 	okMax = new Dimension((new Float(1.5 * okMin.width)).intValue() , (new Float(1.5 * okMin.height)).intValue());
 	okPref = okMax;
 
 
         labelPanel.add(new JLabel("METS objects (input directory)"));
 	labelPanel.add(sized (m_objectsField, textMin, textPref, textMax));
         JButton objectsBtn=new JButton("browse...");
         objectsBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 objectsAction();
             }
         });
 	labelPanel.add(sized (objectsBtn, browseMin, browsePref, browseMax));
 
 	buttonGroup.add(m_xmlMap);
 	m_xmlMap.setSelected(true);
 	buttonGroup.add(m_textMap);
 	JPanel jPanel = new JPanel();
 
 	jPanel.setLayout(new BorderLayout());
 	jPanel.add(m_xmlMap, BorderLayout.WEST);
 	jPanel.add(new JLabel("object processing map (output file)"), BorderLayout.NORTH);
 	jPanel.add(m_textMap, BorderLayout.CENTER);
 	labelPanel.add(sized (jPanel, browseMin, browsePref, browseMax));
 
         labelPanel.add(sized (m_pidsField, textMin, textPref, textMax));
         JButton pidsBtn=new JButton("browse...");
         pidsBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 pidsAction();
             }
         });
 	labelPanel.add(sized (pidsBtn, browseMin, browsePref, browseMax));
 
         entryPanel.add(labelPanel, BorderLayout.WEST);
         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(entryPanel, BorderLayout.CENTER);
         getContentPane().add(sized (btn, okMin, okPref, okMax, true), BorderLayout.SOUTH);
 
         setFrameIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/standard/general/Export16.gif")));
 
         pack();
         setSize(getSize().width+20, getSize().height*2);
         //setSize(400,400);
     }
 
     private final void sizeIt (JComponent jc, Dimension min, Dimension pref, Dimension max) {
 	jc.setMinimumSize(min);
 	jc.setPreferredSize(pref);
 	jc.setMaximumSize(max);
     }
 
     private final Box sized (JComponent jc, Dimension min, Dimension pref, Dimension max, boolean centered) {
 	sizeIt(jc,min,pref,max);
 	Box box = Box.createHorizontalBox();
 	if (centered) {
 		box.add(Box.createGlue());
 	}
 	box.add(jc);
 	if (centered) {
 		box.add(Box.createGlue());
 	}
 	return box;
     }
     private final Box sized (JComponent jc, Dimension min, Dimension pref, Dimension max) {
 	return sized (jc, min, pref, max, false);
     }
 
     private static final Properties nullProperties = new Properties();
 
     public void ingestBatch() {
 	try {
         if (!m_objectsField.getText().equals("")
 		&& !m_pidsField.getText().equals("")
 	) {
 	    Properties properties = new Properties();
 	    properties.setProperty("ingest","yes");
 	    properties.setProperty("objects",m_objectsField.getText());
 	    properties.setProperty("ingested-pids",m_pidsField.getText());
 	    properties.setProperty("pids-format",m_xmlMap.isSelected()? "xml" : "text");
 	    properties.setProperty("server-fqdn",host);
 	    properties.setProperty("server-port",port);
 	    properties.setProperty("username",user);
 	    properties.setProperty("password",pass);
 	    
 	    batchOutput.setDirectoryPath(properties.getProperty("ingested-pids")); //2003.12.03 niebel -- duplicate output to file
 	    
 	    try {
 		    mdiDesktopPane.add(batchOutput);
 	    } catch (Exception eee) {  //illegal component position occurs ~ every other time ?!?
 		    mdiDesktopPane.add(batchOutput);
 	    }
 
         try {
             batchOutput.setSelected(true);
         } catch (java.beans.PropertyVetoException e) {
 		System.err.println("BatchIngestGUI" + " frame select vetoed " + e.getMessage());
 	}
 
 		BatchThread batchThread = null;
 		try {
 			batchThread = new BatchThread(batchOutput, batchOutput.getJTextArea(), "Ingesting Batch . . .");
 		} catch (Exception e) {
 			System.err.println("BatchIngestGUI" + " couldn't instantiate BatchThread " + e.getMessage());
 		}
 	    batchThread.setProperties(properties);
 	    batchThread.start();
         }
 
 	} catch (Exception e) {
 		System.err.println("BatchIngestGUI" + " general error " + e.getMessage());
 	}
     }
 
     protected File selectFile (File lastDir, boolean directoriesOnly) throws Exception {
 	    File selection = null;
 	    JFileChooser browse;
             if (Administrator.batchtoolLastDir==null) {
                 browse=new JFileChooser();
             } else {
                 browse=new JFileChooser(Administrator.batchtoolLastDir);
             }
 	    if (directoriesOnly) {
 		    browse.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 	    }
             int returnVal = browse.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 selection = browse.getSelectedFile();
                 Administrator.batchtoolLastDir=selection.getParentFile(); // remember the dir for next time
 	    }
 	    return selection;
     }
 
     protected void objectsAction () {
 	try {
 		   File temp = selectFile(Administrator.batchtoolLastDir,true);
 		   if (temp != null) {
 			   m_objectsField.setText(temp.getPath());
 		   }
 	} catch (Exception e) {
 			   m_objectsField.setText("");
 	}
     }

     protected void pidsAction () {
	try {
		   File temp = selectFile(Administrator.batchtoolLastDir,false);
		   if (temp != null) {
			   m_pidsField.setText(temp.getPath());
		   }
	} catch (Exception e) {
			   m_pidsField.setText("");
	}
    }
 
 }
