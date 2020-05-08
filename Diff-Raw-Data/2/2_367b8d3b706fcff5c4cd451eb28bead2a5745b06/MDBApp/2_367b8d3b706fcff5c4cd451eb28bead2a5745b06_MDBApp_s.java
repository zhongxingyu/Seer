 package uk.co.brotherlogic.mdb;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.sql.SQLException;
 import java.text.DateFormat;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import uk.co.brotherlogic.mdb.artist.GetArtists;
 import uk.co.brotherlogic.mdb.categories.GetCategories;
 import uk.co.brotherlogic.mdb.cdbuilder.MakeCDFileOverseer;
 import uk.co.brotherlogic.mdb.format.GetFormats;
 import uk.co.brotherlogic.mdb.groop.GetGroops;
 import uk.co.brotherlogic.mdb.label.GetLabels;
 import uk.co.brotherlogic.mdb.parsers.DiscogParser;
 import uk.co.brotherlogic.mdb.record.AddRecordOverseer;
 import uk.co.brotherlogic.mdb.record.GetRecords;
 import uk.co.brotherlogic.mdb.record.Record;
 
 /**
  * Main entry point for the MDB App
  * 
  * @author sat
  * 
  */
 public class MDBApp extends JFrame {
 	public static void main(final String[] args) throws Exception {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		MDBApp c = new MDBApp();
 		if (System.getProperty("os.name").compareToIgnoreCase("Linux") == 0)
 			c.setFileString("/usr/share/hancock_multimedia/");
 		c.runApp();
 	}
 
 	/** The output string for windows */
 	private String fileString = "i:\\";
 
 	/**
 	 * Constructor
 	 */
 	public MDBApp() {
 		// Connect.setForProduction();
 	}
 
 	private void addCD() {
 		this.setVisible(false);
 		final MDBApp ref = this;
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					AddRecordOverseer over = new AddRecordOverseer(ref,
 							GetArtists.create().getArtists(), GetLabels
 									.create().getLabels(), GetFormats.create()
 									.getFormats(), GetGroops.build()
 									.getGroopMap(), GetCategories.build()
 									.getCategories());
 					over.showGUI(ref);
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public final void addDone(final Record done) {
 		// Add record is done!
 
 		try {
 			DateFormat df = DateFormat.getDateInstance();
 			done.save();
 
 			// Commit all the transactions
 			Connect.getConnection().commitTrans();
 		} catch (SQLException ex) {
 			ex.printStackTrace();
 		}
 
 		this.setVisible(true);
 	}
 
 	public final void cancel() {
 		this.setVisible(true);
 	}
 
 	private void discog() {
 		this.setVisible(false);
 		try {
 			// Choose a file to examine
 			String id = JOptionPane.showInputDialog("Enter discog ID");
 
 			// Prepare the viewer
 
 			DiscogParser parser = new DiscogParser();
 			Record examine = parser.parseDiscogRelease(Integer.parseInt(id));
 
 			if (examine != null) {
 				AddRecordOverseer over = new AddRecordOverseer(this, GetArtists
 						.create().getArtists(), GetLabels.create().getLabels(),
 						GetFormats.create().getFormats(), GetGroops.build()
 								.getGroopMap(), GetCategories.build()
 								.getCategories(), examine);
 				over.showGUI(this);
 			} else
 				this.setVisible(true);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(null, "ERROR: "
 					+ ex.getLocalizedMessage());
 			this.setVisible(true);
 		}
 
 	}
 
 	private void edit() {
 		try {
 			// Choose a file to examine
 			RecordSelector sel = new RecordSelector();
 			Record examine = sel.selectRecord(this);
 
 			if (examine != null) {
 				// Prepare the viewer
 				this.setVisible(false);
 
 				AddRecordOverseer over = new AddRecordOverseer(this, GetArtists
 						.create().getArtists(), GetLabels.create().getLabels(),
 						GetFormats.create().getFormats(), GetGroops.build()
 								.getGroopMap(), GetCategories.build()
 								.getCategories(), examine);
 				over.showGUI(this);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 	}
 
 	private void jbInit() throws Exception {
 		JButton buttonAdd = new JButton("Add Record");
 		buttonAdd.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				addCD();
 			}
 		});
 
 		this.getContentPane().setLayout(new BorderLayout());
 		JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
 
 		JButton buttonEdit = new JButton("Edit Record");
 		buttonEdit.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				edit();
 			}
 		});
 
 		JButton buttonCD = new JButton("Make CD File");
 		buttonCD.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				makeCD();
 			}
 		});
 
 		JButton buttonDiscogs = new JButton("Add From DiscogID");
 		buttonDiscogs.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				discog();
 			}
 		});
 
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.setTitle("Music Database");
 
 		buttonPanel.add(buttonAdd, null);
 		buttonPanel.add(buttonCD, null);
 		buttonPanel.add(buttonEdit, null);
 		buttonPanel.add(buttonDiscogs, null);
 		this.add(buttonPanel, BorderLayout.CENTER);
 
		JLabel label = new JLabel("Version 0.31");
 		this.add(label, BorderLayout.SOUTH);
 	}
 
 	private void makeCD() {
 		try {
 			// Run the button CD overseer
 			this.setVisible(false);
 			new MakeCDFileOverseer(GetRecords.create(), Settings
 					.getCDFileOutputDirectory().getAbsolutePath());
 			this.setVisible(true);
 		} catch (SQLException e2) {
 			JOptionPane.showMessageDialog(this, e2.getMessage());
 		}
 
 	}
 
 	public final void runApp() {
 
 		// Now construct the gui
 		try {
 			jbInit();
 			final int appSize = 500;
 			this.setSize(appSize, appSize);
 
 			// Center the frame on screen
 			this.setLocationRelativeTo(null);
 
 			// Display the giu
 			this.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public final void setFileString(final String in) {
 		fileString = in;
 	}
 
 }
