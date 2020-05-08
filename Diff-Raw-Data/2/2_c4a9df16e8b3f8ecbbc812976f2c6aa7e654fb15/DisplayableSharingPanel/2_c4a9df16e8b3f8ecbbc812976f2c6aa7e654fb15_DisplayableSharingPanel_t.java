 /**
  * 
  */
 package net.niconomicon.tile.source.app.sharing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.net.InetAddress;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import net.niconomicon.tile.source.app.Ref;
 import net.niconomicon.tile.source.app.filter.DirOrDisplayableFilter;
 import net.niconomicon.tile.source.app.sharing.SharingWidget.STATUS;
 import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
 import net.niconomicon.tile.source.app.viewer.DisplayableViewer;
 
 /**
  * @author niko
  * 
  */
 public class DisplayableSharingPanel extends JPanel implements TableModelListener {
 
 	boolean currentlySharing = false;
 	SharingManager sharingManager;
 
 	CheckBoxTable displayablesList;
 	// JSpinner portNumber;
 
 	SharingWidget widget;
 
 	// JButton shareButton;
 	// String rootDir = "/Users/niko/Sites/testApp/mapRepository";
 	Color defaultColor;
 
 	DisplayableViewer viewer;
 
 	// InetAddress localaddr;
 	Timer timer;
 
 	public DisplayableSharingPanel(DisplayableViewer viewer) {
 		this.viewer = viewer;
 		init();
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
 	 */
 	public void tableChanged(TableModelEvent e) {
 
 		int i = e.getType();
 
 		String _case = "";
 		switch (i) {
 		case TableModelEvent.DELETE:
 			_case = "DELETE";
 			break;
 		case TableModelEvent.UPDATE:
 			_case = "UPDATE";
 			break;
 		case TableModelEvent.INSERT:
 			_case = "INSERT";
 			break;
 		}
 		System.out.println("Table changed. " + _case + " = " + e.getType() + " source " + e.getSource() + " row : " + e.getFirstRow() + " - " + e
 				.getLastRow() + "  col :" + e.getColumn());
 		// System.out.println("heee haa");
 
 		if (sharingManager.isSharing()) {
 			sharingManager.setSharingList(displayablesList.getSelectedTilesSetFiles());
 			// update the list of shared documents
 		} else {
 			// don't care ;-)
 			return;
 		}
 	}
 
 	public JPanel createDirSelectionPanel() {
 		JPanel p = new JPanel();
 		p.setLayout(new BorderLayout());
 		p.add(new JLabel("Locate Displayables for sharing : "), BorderLayout.WEST);
 		JButton b = new JButton("Choose Displayables");
 		b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 				fc.setFileFilter(new DirOrDisplayableFilter());
 				fc.setMultiSelectionEnabled(true);
 				int res = fc.showOpenDialog(DisplayableSharingPanel.this);
 				if (JFileChooser.APPROVE_OPTION == res) {
 					File[] files = fc.getSelectedFiles();
 					setSelectedFiles(files);
 				}
 			}
 		});
 		p.add(b, BorderLayout.EAST);
 		return p;
 	}
 
 	public void init() {
 
 		sharingManager = new SharingManager();
 		displayablesList = new CheckBoxTable(viewer);
 		timer = new Timer();
 
 		displayablesList.table.getModel().addTableModelListener(this);
 		// mapList.getSelectionModel().addListSelectionListener(this);
 		this.setLayout(new BorderLayout());
 		this.add(createDirSelectionPanel(), BorderLayout.NORTH);
 		// shared files
 		// //////////////////////////////////////////
 		this.add(displayablesList, BorderLayout.CENTER);
 		// //////////////////////////////////////////
 		// port number
 		// start sharing
 		JButton shareButton = new JButton("Start sharing");
 		shareButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Runnable runn = new Runnable() {
 					public void run() {
 						switchSharing(true);
 					}
 				};
 				Thread t = new Thread(runn);
 				t.start();
 			}
 		});
 
 		widget = new SharingWidget(shareButton);
 
 		long delta = 10000;
 		timer.scheduleAtFixedRate(new LocalHostChecker(), delta, delta);
 		Runnable runn = new Runnable() {
 			public void run() {
 				switchSharing(false);
 			}
 		};
 		Thread t = new Thread(runn);
 		t.start();
 		this.add(widget, BorderLayout.SOUTH);
 	}
 
 	public static List<String> getDBFilesInSubDirectory(File dir) {
 		List<String> dbFiles = new ArrayList<String>();
 		dbFiles.addAll(Arrays.asList(Ref.getAbsolutePathOfDBFilesInDirectory(dir)));
 		for (File d : dir.listFiles()) {
 			if (d.isDirectory()) {
 				dbFiles.addAll(getDBFilesInSubDirectory(d));
 			}
 		}
 		return dbFiles;
 	}
 
 	public void setSelectedFiles(File[] files) {
 		if (null == files) { return; }// clear ?
 		List<String> dbFiles = new ArrayList<String>();
 		for (File f : files) {
 			if (f.isDirectory()) {
 				dbFiles.addAll(getDBFilesInSubDirectory(f));
 			} else {
 				if (f.getAbsolutePath().endsWith(Ref.ext_db)) {
 					dbFiles.add(f.getAbsolutePath());
 				}
 			}
 		}
 		Collections.sort(dbFiles);
 		Map<String, String> fileToTitle = new HashMap<String, String>();
 		for (String path : dbFiles) {
 			try {
 				// System.out.println("Going to get the title from " + path);
 				fileToTitle.put(path, SQliteTileCreatorMultithreaded.getTitle(path));
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		displayablesList.addData(fileToTitle);
 		if (sharingManager.isSharing()) {
 			sharingManager.setSharingList(displayablesList.getSelectedTilesSetFiles());
 		}
 	}
 
 	public void switchSharing(boolean shouldPopup) {
 
 		if (!currentlySharing) {
 			try {
 				widget.setStatus(STATUS.ACTIVATING);
 			} catch (InvocationTargetException ex) {} catch (InterruptedException ex) {}
 			if (startSharing(shouldPopup)) {
 				try {
 					widget.setStatus(STATUS.ACTIVE);
 				} catch (InvocationTargetException ex) {} catch (InterruptedException ex) {}
 				currentlySharing = true;
 				return;
 			}
 		}
 		// stopping sharing or start sharing failed.
 		try {
 			widget.setStatus(STATUS.DEACTIVATING);
 		} catch (InvocationTargetException ex) {} catch (InterruptedException ex) {}
 		stopSharing();
 		try {
 			widget.setStatus(STATUS.DEACTIVATED);
 		} catch (InvocationTargetException ex) {} catch (InterruptedException ex) {}
 		currentlySharing = false;
 	}
 
 	public boolean startSharing(boolean shouldPopup) {
 		// HashSet<String> sharedDB = new HashSet<String>();
 		Collection<String> sharedMaps = displayablesList.getSelectedTilesSetFiles();
 		System.out.println("should start sharing the maps, with " + (shouldPopup ? "popup" : "no popup") + " in case of problem");
 		// generate the xml;
 		try {
 			sharingManager.setPort(widget.getPort());
 			sharingManager.setSharingList(sharedMaps);
			//sharingManager.startSharing();
 		} catch (Exception ex) {
 			try {
 				sharingManager.stopSharing();
 			} catch (Exception ex1) {
 				System.out.println("ex1 : ");
 				ex1.printStackTrace();
 			}
 			if (shouldPopup) {
 				JOptionPane
 						.showConfirmDialog(
 								this,
 								"<html><body>Error while starting the sharing component on port [" + widget.getPort() + "]: <br/><i>" + ex
 										.getMessage() + "</i></body></html>", "Error creating starting the sharing component",
 								JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
 			}
 			ex.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public void stopSharing() {
 		try {
 			sharingManager.stopSharing();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @param fileLocation
 	 *            This can be a temporary file.
 	 */
 	public void addDisplayableToShare(String fileLocation, String title) {
 		displayablesList.addDisplayable(fileLocation, title);
 	}
 
 	public void updateDisplayableLocation(String oldLocation, String newLocation) {
 		displayablesList.updateLocation(oldLocation, newLocation);
 	}
 
 	public Map<String, String> getDisplayableList(String rootDir, String[] maps) {
 		Map<String, String> fileToName = new HashMap<String, String>();
 		if (!rootDir.endsWith(File.separator)) {
 			rootDir += File.separator;
 		}
 		for (String string : maps) {
 			try {
 				String fileName = rootDir + string;
 				System.out.println("trying to open the map : " + fileName);
 				Connection mapDB = DriverManager.getConnection("jdbc:sqlite:" + fileName);
 				mapDB.setReadOnly(true);
 				Statement statement = mapDB.createStatement();
 				statement.setQueryTimeout(30); // set timeout to 30 sec.
 				ResultSet rs = statement.executeQuery("select " + Ref.infos_title + " from infos");
 				while (rs.next()) {
 					String name = rs.getString(Ref.infos_title);
 					System.out.println("name : " + name);
 					fileToName.put(fileName, name);
 				}
 				if (mapDB != null) mapDB.close();
 			} catch (Exception ex) {
 				System.err.println("ex for map : " + string);
 				ex.printStackTrace();
 			}
 		}
 		return fileToName;
 	}
 
 	public class LocalHostChecker extends TimerTask {
 		String hostname = "bla";
 		String localHost = "notBla";
 
 		public void run() {
 			try {
 				localHost = InetAddress.getLocalHost().getCanonicalHostName();
 				if (!hostname.equals(localHost)) {
 					hostname = localHost;
 					if (sharingManager.isSharing()) {
 						widget.setStatus(STATUS.ACTIVE);
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 }
