 package de.zigapeda.flowspring.gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import org.hsqldb.types.Types;
 
 import de.zigapeda.flowspring.Main;
 import de.zigapeda.flowspring.controller.Compare;
 import de.zigapeda.flowspring.controller.Rename;
 import de.zigapeda.flowspring.controller.Settings;
 import de.zigapeda.flowspring.controller.Tagreader;
 import de.zigapeda.flowspring.data.DataNode;
 import de.zigapeda.flowspring.data.ReadTableModel;
 import de.zigapeda.flowspring.data.Title;
 
 public class ReadWindow extends JFrame implements WindowListener, ActionListener {
 	private static final long	serialVersionUID	= 3800673403668208662L;
 	private JTextField directorytextfield;
 	private JButton directorybrowse;
 	private JButton directorystart;
 	private ReadTableModel readtablemodel;
 	private JTable	readtable;
 	private FileWalker	fw;
 	private JLabel	description;
 	private JButton readfiles;
 	private JButton copyfiles;
 	private JButton movefiles;
 	private String path;
 	private JCheckBox avoiddoubles;
 
 	public ReadWindow() {
 		super("flowspring - Add files to library");
         BufferedImage image;
         try {
             image = ImageIO.read(Main.class.getClass().getResource("/de/zigapeda/flowspring/res/icon.png"));
             this.setIconImage(image);
         } catch (IOException e) {
             e.printStackTrace();
         }
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		GridBagConstraints c = new GridBagConstraints();
 		this.setLayout(new GridBagLayout());
 		JPanel directorylayout = new JPanel();
 		directorylayout.setLayout(new GridBagLayout());
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		String path = Settings.loadSettings("readwindow.path");
 		if(path == null) {
 			path = Settings.loadSettings("defaultdir");
 		} else if(path.length() == 0) {
 			path = Settings.loadSettings("defaultdir");
 		}
 		directorytextfield = new JTextField(path);
 		directorylayout.add(directorytextfield,c);
 		c.weightx = 0;
 		c.gridx = 1;
 		c.fill = GridBagConstraints.NONE;
 		directorybrowse = new JButton("Browse...");
 		directorybrowse.addActionListener(this);
 		directorylayout.add(directorybrowse,c);
 		c.gridx = 2;
 		directorystart = new JButton("Start");
 		directorystart.addActionListener(this);
 		directorylayout.add(directorystart,c);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.WEST;
 		c.weightx = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		this.add(directorylayout,c);
 		c.gridy = 1;
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 1;
 		setReadtablemodel(new ReadTableModel());
 		readtable = new JTable(getReadtablemodel());
 		ReadtableRenderer rtr = new ReadtableRenderer(this);
 		readtable.getColumnModel().getColumn(0).setCellRenderer(rtr);
 		this.add(new JScrollPane(readtable),c);
 		c.gridy = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0;
 		description = new JLabel("<html><body><br></body></html>");
 		this.add(description,c);
 		JPanel buttonlayout = new JPanel();
 		c.gridy = 3;
 		c.fill = GridBagConstraints.NONE;
 		c.anchor = GridBagConstraints.EAST;
 		this.add(buttonlayout,c);
 		buttonlayout.setLayout(new GridBagLayout());
 		this.setReadfiles(new JButton("Read files"));
 		this.setCopyfiles(new JButton("Read and copy to music directory"));
 		this.setMovefiles(new JButton("Read and move to music directory"));
 		this.getCopyfiles().setEnabled(Rename.isRenameAvailable());
 		this.getMovefiles().setEnabled(Rename.isRenameAvailable());
 		this.avoiddoubles = new JCheckBox("Avoid doubles (takes more time)");
 		this.getReadfiles().addActionListener(this);
 		this.getCopyfiles().addActionListener(this);
 		this.getMovefiles().addActionListener(this);
 		c.gridx = 0;
 		c.gridy = 0;
 		buttonlayout.add(this.avoiddoubles,c);
 		c.gridx = 1;
 		buttonlayout.add(this.getReadfiles(),c);
 		c.gridx = 2;
 		buttonlayout.add(this.getCopyfiles(),c);
 		c.gridx = 3;
 		buttonlayout.add(this.getMovefiles(),c);
 		this.addWindowListener(this);
 		readtable.getColumnModel().getColumn(0).setPreferredWidth(300);
 		readtable.getTableHeader().setReorderingAllowed(false);
         String windowbounds = Settings.loadSettings("readwindow.bounds");
         if(windowbounds != null) {
         	String[] wba = windowbounds.split(",");
         	this.setBounds(Integer.valueOf(wba[2]), Integer.valueOf(wba[3]), Integer.valueOf(wba[0]), Integer.valueOf(wba[1]));
         	if(Integer.valueOf(wba[4]) != JFrame.ICONIFIED) {
         		this.setExtendedState(Integer.valueOf(wba[4]));
         	}
         } else {
     		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
     		this.setBounds(screensize.width/2 - 400, screensize.height/2 - 300, 800, 600);
         }
         this.setMinimumSize(new Dimension(800, 600));
 		this.setVisible(true);
 	}
 	
 	private String getPositionString() {
 		String temp = new String();
 		int ext = this.getExtendedState();
 		this.setExtendedState(0);
 		temp = temp + String.valueOf(this.getWidth()) + ",";
 		temp = temp + String.valueOf(this.getHeight()) + ",";
 		temp = temp + String.valueOf(this.getX()) + ",";
 		temp = temp + String.valueOf(this.getY()) + ",";
 		temp = temp + String.valueOf(ext);
 		return temp;
 	}
 	
 	public String getPath() {
 		return this.path;
 	}
 
 	public void windowOpened(WindowEvent e) {
 		
 	}
 
 	public void windowClosing(WindowEvent evt) {
 		Settings.saveSettings("readwindow.bounds", this.getPositionString());
 	}
 
 	public void windowClosed(WindowEvent e) {
 		Main.setReadWindow(null);
 	}
 
 	public void windowIconified(WindowEvent e) {
 		
 	}
 
 	public void windowDeiconified(WindowEvent e) {
 		
 	}
 
 	public void windowActivated(WindowEvent e) {
 		
 	}
 
 	public void windowDeactivated(WindowEvent e) {
 		
 	}
 
 	public void actionPerformed(ActionEvent evt) {
 		if(evt.getSource() == this.directorybrowse) {
 			File file = new File(this.directorytextfield.getText());
 			if(file.exists()) {
 				if(file.isDirectory()) {
 					JFileChooser fc = new JFileChooser(file);
 					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 					int returnVal = fc.showOpenDialog(this.directorybrowse);
 		            if (returnVal == JFileChooser.APPROVE_OPTION) {
 		                this.directorytextfield.setText(fc.getSelectedFile().getAbsolutePath());
 		            }
 				} else {
 					JFileChooser fc = new JFileChooser();
 					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 					int returnVal = fc.showOpenDialog(this.directorybrowse);
 		            if (returnVal == JFileChooser.APPROVE_OPTION) {
 		                this.directorytextfield.setText(fc.getSelectedFile().getAbsolutePath());
 		            }
 				}
 			} else {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				int returnVal = fc.showOpenDialog(this.directorybrowse);
 	            if (returnVal == JFileChooser.APPROVE_OPTION) {
 	                this.directorytextfield.setText(fc.getSelectedFile().getAbsolutePath());
 	            }
 			}
 		} else if(evt.getSource() == this.directorystart) {
 			if(fw != null) {
 				fw.requestStop();
 				fw = null;
 				this.directorystart.setText("Start");
 			} else {
 				if(this.getReadtablemodel().getRowCount() > 0) {
 					this.getReadtablemodel().removeAllRows();
 				}
 				File tmp = new File(this.directorytextfield.getText());
 				if(tmp.exists()) {
 					if(tmp.isDirectory()) {
 						this.path = tmp.getAbsolutePath();
 						Settings.saveSettings("readwindow.path", this.path);
 						fw = new FileWalker(path, this);
 						fw.start();
 						this.directorystart.setText("Stop");
 					}
 				}
 			}
 		} else if(evt.getSource() == this.getReadfiles() || evt.getSource() == this.getCopyfiles() || evt.getSource() == this.getMovefiles()) {
 			ReadFiles rf = new ReadFiles((JButton) evt.getSource(),this);
 			rf.start();
 		}
 	}
 
 	public ReadTableModel getTableModel() {
 		return this.getReadtablemodel();
 	}
 	
 	public JTable getTable() {
 		return this.readtable;
 	}
 	
 	public void setStartbuttonText(String text) {
 		this.directorystart.setText(text);
 	}
 	
 	public void setReadtext(int counter, int size) {
 		this.description.setText("<html><body>" + String.valueOf(counter) + " from " + String.valueOf(size) + " titles added to library yet</body></html>");
 	}
 	
 	public void setSearchtext(int counter) {
 		this.description.setText("<html><body>" + String.valueOf(counter) + " titles found until now!</body></html>");
 	}
 	
 	public void setFinishtext(int counter) {
 		this.description.setText("<html><body>" + String.valueOf(counter) + " titles found at this location!</body></html>");
 	}
 
 	public ReadTableModel getReadtablemodel() {
 		return readtablemodel;
 	}
 
 	public void setReadtablemodel(ReadTableModel readtablemodel) {
 		this.readtablemodel = readtablemodel;
 	}
 
 	public JButton getReadfiles() {
 		return readfiles;
 	}
 
 	public void setReadfiles(JButton readfiles) {
 		this.readfiles = readfiles;
 	}
 
 	public JButton getCopyfiles() {
 		return copyfiles;
 	}
 
 	public void setCopyfiles(JButton copyfiles) {
 		this.copyfiles = copyfiles;
 	}
 
 	public JButton getMovefiles() {
 		return movefiles;
 	}
 
 	public void setMovefiles(JButton movefiles) {
 		this.movefiles = movefiles;
 	}
 	
 	public boolean isAvoidDoubles() {
 		return this.avoiddoubles.isSelected();
 	}
 }
 
 class ReadFiles extends Thread {
 	private JButton button;
 	private ReadWindow parent;
 	private Connection c;
 	
 	public ReadFiles(JButton button, ReadWindow parent) {
 		this.button = button;
 		this.parent = parent;
 	}
 	
 	public void run() {
 		if(this.parent.getReadtablemodel().getRowCount() > 0) {
 			this.c = Main.getDatabase();
 			LinkedList<Title> list = this.parent.getReadtablemodel().getData();
 			for(int i = 0; i < list.size(); i++) {
 				Title t = list.get(i);
 				if(this.button == this.parent.getReadfiles()) {
 					insertTitle(t);
 				} else if (this.button == this.parent.getCopyfiles()) {
 					String path = Rename.createPath(t);
 					if(Rename.isFile(path) == false) { 
 						int id = insertTitle(t);
 						if(id > -1) {
 							try {
 								File newfile = new File(path);
								newfile.createNewFile();
 								Files.copy(new File(t.getPath()).toPath(), newfile.toPath());
 								Title.changePath(id, path);
 							} catch(IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				} else if (this.button == this.parent.getMovefiles()) {
 					String path = Rename.createPath(t);
 					if(Rename.isFile(path) == false) { 
 						int id = insertTitle(t);
 						if(id > -1) {
 							try {
 								Files.move(new File(t.getPath()).toPath(), new File(path).toPath());
 								Title.changePath(id, path);
 							} catch(IOException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 				this.parent.setReadtext(i + 1, list.size());
 			}
 			DataNode.refreshMedialib(Main.getWindow().getControlllayout().getTypeOrder().getFirst());
 			Main.getWindow().refreshMedialib();
 		}
 	}
 	
 	private int insertTitle(Title t) {
 		int[] status = this.insertTitleIntoDB(t.getArtist(), t.getAlbum(), t.getName(), t.getComment(), t.getGenre(), t.getTrack(), t.getYear(), t.getInt(), t.getRating(), t.getPlaycount(), t.getPath());
 		if((status[0] & 32) == 32) {
 			if((status[0] & 64) == 0) {
 				if(this.parent.isAvoidDoubles() == true) {
 					//MD5 checken
 				} else {
 					String newname;
 					do {
 						int nextval = this.getNextUnique();
 						if(nextval > -1) {
 							newname = t.getName() + "_" + String.valueOf(nextval);
 						} else {
 							return -1;
 						}
 						status = this.insertTitleIntoDB(t.getArtist(), t.getAlbum(), newname, t.getComment(), t.getGenre(), t.getTrack(), t.getYear(), t.getInt(), t.getRating(), t.getPlaycount(), t.getPath());
 					} while((status[0] & 32) == 0);
 				}
 			}
 		}
 		return status[1];
 	}
 	
 	private int[] insertTitleIntoDB(String interpret, String album, String name, String comment, String genre, String track, String year, Integer duration, String rating, String playcount, String path) {
 		int ret = 0;
 		int id = 0;
 		try {
 			CallableStatement s = c.prepareCall("call inserttitle(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 			this.setParameters(s, interpret, Compare.getComparableString(interpret),
 					album, Compare.getComparableString(album),
 					name, Compare.getComparableString(name),
 					comment, Compare.getMD5(comment),
 					genre, Compare.getComparableString(genre),
 					getInt(track), getInt(year), duration,
					getInt(rating), getInt(playcount), path);
 			s.registerOutParameter(17, Types.INTEGER);
 			s.registerOutParameter(18, Types.INTEGER);			
 			s.execute();
 			ret = s.getInt(17);
 			id = s.getInt(18);
 			s.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new int[] {ret, id};
 	}
 	
 	private int getNextUnique() {
 		Statement s;
 		try {
 			s = c.createStatement();
 			ResultSet r = s.executeQuery("select next value for unq_gen from dual");
 			if(r.next()) {
 				return r.getInt(1);
 			} else {
 				return -1;
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 	
 	private Integer getInt(String string) {
 		if(string != null) {
 			try {
 				return Integer.valueOf(string);
 			} catch (NumberFormatException e) {
 			}
 		}
 		return 0;
 	}
 
 	private void setParameters(PreparedStatement s, Object...objects) {
 		int i = 1;
 		for(Object o: objects) {
 			try {
 				if(o == null) {
 					s.setNull(i, java.sql.Types.NULL);
 				} else if (o instanceof String) {
 					s.setString(i, (String)o);
 				} else if (o instanceof Integer) {
 					s.setInt(i, (Integer)o);
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			i++;
 		}
 	}
 	
 }
 
 class FileWalker extends Thread {
 	private String path;
 	private ReadWindow readwindow;
 	private volatile boolean stop = false;
 	
 	public FileWalker(String path, ReadWindow readwindow) {
 		this.path = path;
 		this.readwindow = readwindow;
 	}
 	
 	public void requestStop() {
 		this.stop = true;
 	}
 	
 	public void run() {
 		try {
 			Files.walkFileTree(Paths.get(this.path), new SimpleFileVisitor<Path>() {
 			    public FileVisitResult visitFileFailed(Path file, IOException exc) {
 					return FileVisitResult.CONTINUE;
 			    }
 			    
 				public FileVisitResult visitFile( Path filepath, BasicFileAttributes attribs ) {
 					Title temp = new Tagreader(filepath).getTitle();
 					if(temp != null) {
 						FileWalker.this.readwindow.getTableModel().addRow(temp);
 						JTable table = FileWalker.this.readwindow.getTable();
 						table.scrollRectToVisible(table.getCellRect(table.getRowCount() -1, 0, true));
 					}
 					if(stop) {
 						return FileVisitResult.TERMINATE;
 					}
 					FileWalker.this.readwindow.setFinishtext(FileWalker.this.readwindow.getTableModel().getRowCount());
 					return FileVisitResult.CONTINUE;
 				}
 			});
 			this.readwindow.setStartbuttonText("Start");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
