 package org.iugonet.www;
 
 import gnu.getopt.Getopt;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 public class JudasViewer extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	/*
 	 * Choose one of two following methods. 1: Specify strUrl & pluginName. 2:
 	 * Specify iugonetGranuleResourceID. JUDAS understand it automatically.
 	 * JUDAS retrieve the data & make a choice plugin to treat with it.
 	 */
 	private static String strUrl; /* URL of the data file */
 	private static String pluginName; /*
 									 * The name of plugin which treat with
 									 * strURL
 									 */
 	private static String iugonetGranuleResourceID; /* ResourceID of the Granule */
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @throws Exception
 	 */
 	public static void main(final String[] args) throws Exception {
 		Getopt getopt = new Getopt("JudasViewer", args, "u:p:g:");
 		int c;
 		while ((c = getopt.getopt()) != -1) {
 			switch (c) {
 			case 'u': // URL
 				strUrl = getopt.getOptarg();
 				break;
 			case 'p': // plugin
 				pluginName = getopt.getOptarg();
 				break;
 			case 'g':
 				iugonetGranuleResourceID = getopt.getOptarg();
 				break;
 			}
 		}
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					JudasViewer frame = new JudasViewer(strUrl, pluginName);
 					frame.setTitle("JudasViewer");
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public JudasViewer(String strUrl, String pluginName) {
 
 		Icons icons = new Icons();
 		setIconImage(icons.getImage("icons/favicon.ico"));
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 
 		if ( pluginName.equals("DstIndex") ) {
//			DstIndex dstIndex = new DstIndex();		
 //			getContentPane().add(dstIndex.getChartPanel());
 		}
 		getContentPane().setVisible(true);
 	}
 
 }
