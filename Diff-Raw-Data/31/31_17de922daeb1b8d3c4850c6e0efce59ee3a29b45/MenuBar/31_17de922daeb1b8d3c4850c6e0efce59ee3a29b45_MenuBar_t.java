 package components;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import service.Common;
 import service.Constants; 
 import service.FileDialogFilter;
 import service.Utils;
 import service.ActionBind;
 import tabber.Tab;
 import tabber.TabOptions;
 import tabber.Tabber;
 
 public class MenuBar extends JMenuBar {
 	private static final long serialVersionUID = 5651526930411426260L;
 	private JTextField title;
 	private JCheckBox delete_with_file;
 	private JCheckBox interactive;
 	private JCheckBox delete_empty_folders;
 	private JCheckBox remote_source;
 	JFileChooser fileChooser = new JFileChooser(".");
 	FileDialogFilter dialog_filter = new FileDialogFilter("torrent", "torrent file");
 	
 	
 	void prepareGUI(String tit, boolean del, boolean inter, boolean del_ef, boolean remote) {
 		title = new JTextField(tit);
 		delete_with_file = new JCheckBox("Delete file with item", del);
 		interactive = new JCheckBox("Interactive tab", inter);
 		delete_empty_folders = new JCheckBox("Delete empty folders", del_ef);
 		remote_source = new JCheckBox("Source of data is remote", remote);		
 	}
 	void prepareGUI() { prepareGUI("", false, false, false, false); }
 	
 	public MenuBar(final Tabber tab) {
 		setBackground(Color.black);
 		ActionBind [] actions = {
 				new ActionBind("add_tab", new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						prepareGUI();
 					    Object complexMsg[] = { "Create tab with title", title, new JCheckBox[] {delete_with_file, delete_empty_folders, interactive, remote_source} };		
 						if( Utils.showDialog(MenuBar.this, "Creating drop elem", complexMsg) == JOptionPane.OK_OPTION )
 							Common.tabber.AddTab(title.getText(), new TabOptions(delete_with_file.isSelected(), interactive.isSelected(), delete_empty_folders.isSelected(), remote_source.isSelected()));							
 				    }
 				}),
 				new ActionBind("settings", new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						Tab tab = Common.tabber.GetCurrentTab();
 						if (tab == null) return; 
 						prepareGUI(tab.GetTitle(), tab.options.delete_files, tab.options.interactive, tab.options.delete_empty_folders, tab.options.remote_source);
 					    Object complexMsg[] = { "Modify tab title", title, delete_with_file, delete_empty_folders, interactive, remote_source };		
 						if( Utils.showDialog(MenuBar.this, "Modify drop elem", complexMsg) == JOptionPane.OK_OPTION ) {
 							if (title.getText().trim().length() > 0)
 								tab.SetTitle(title.getText());
 							tab.options = new TabOptions(delete_with_file.isSelected(), interactive.isSelected(), delete_empty_folders.isSelected(), remote_source.isSelected());
 							tab.UpdateCounter();
 						}						
 				    }
 				}),		
 				new ActionBind("include_base", new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 					    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 					    if (fileChooser.showDialog(MenuBar.this, "Choose") == JFileChooser.APPROVE_OPTION) {
 					    	if (fileChooser.getSelectedFile().isDirectory())
 					    		Common.library.ParseLibrary(fileChooser.getSelectedFile());	
 					    	else Common.library.ParseBase(fileChooser.getSelectedFile().getAbsolutePath());
 					    }
 					}
 				}),
 				new ActionBind("player", new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						Common.drop_manager.player_panel.setVisible(!Common.drop_manager.player_panel.isVisible());
 					}
 				}),				
 				new ActionBind("torrent", new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						final JLabel pathLabel = new JLabel("Torrent not set");
 						
 						JButton torrentDialogButton = new JButton("Choose torrent file");
 						torrentDialogButton.addActionListener(new ActionListener() {
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								fileChooser.setFileFilter(dialog_filter);
 							    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 							    if (fileChooser.showDialog(MenuBar.this, "Choose") == JFileChooser.APPROVE_OPTION)
 							    	pathLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
 							}
 						});
 						
 						final JLabel savePathLabel = new JLabel(Constants.default_torrent_path);
 						
 						JButton savePathDialogButton = new JButton("Choose save path");
 						savePathDialogButton.addActionListener(new ActionListener() {
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								fileChooser.removeChoosableFileFilter(dialog_filter); 
 							    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 							    if (fileChooser.showDialog(MenuBar.this, "Choose") == JFileChooser.APPROVE_OPTION)
 							    	savePathLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
 							}
 						});										
 						
 					    Object complexMsg[] = { "Choose torrent file", new Object[] {pathLabel, torrentDialogButton}, "Choose save path", new Object[] {savePathLabel, savePathDialogButton} };		
 						if( Utils.showDialog(MenuBar.this, "Start download", complexMsg) == JOptionPane.OK_OPTION )
 							Common.torrent_window.AddTorrent(pathLabel.getText(), savePathLabel.getText());					
 						
 						Common.torrent_window.Show();
 				    }
 				})
 			};
 		
 		for(int loop1 = 0; loop1 < actions.length; loop1++) {
 			ImageIcon n = Utils.GetIcon("menubar/" + actions[loop1].name + ".png");
 			final JMenuItem item = new JMenuItem(n);
			item.setToolTipText(actions[loop1].name.replace('_', ' '));
 			item.setBackground(Common.color_background);
 			item.addActionListener(actions[loop1].action);
 			item.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent arg0) 	{}
 				public void mouseEntered(MouseEvent arg0) 	{ item.setBackground(Common.color_foreground); }
 				public void mouseExited(MouseEvent arg0) 	{ item.setBackground(Common.color_background); }
 				public void mousePressed(MouseEvent arg0) 	{}
 				public void mouseReleased(MouseEvent arg0) 	{}
 			});
 			this.add(item);
 		}
 		this.setVisible(true);
 	}	
 }
