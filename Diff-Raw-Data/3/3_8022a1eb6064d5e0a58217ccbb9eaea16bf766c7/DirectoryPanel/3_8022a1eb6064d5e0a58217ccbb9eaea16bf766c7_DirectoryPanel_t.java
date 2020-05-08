 package cz.zcu.kiv.kc.shell;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.Timer;
 import javax.swing.filechooser.FileSystemView;
 
 public class DirectoryPanel extends JPanel implements ActionListener,
 		FocusListener {
 		
 	private static final long serialVersionUID = 840871288858771069L;
 	
 	private boolean refreshInProgress = false;
 	private List<FocusListener> listeners = new ArrayList<FocusListener>(1);
 
 	private FileListModel listModel = new FileListModel();
 	private JList<File> list = new JList<File>(listModel);
 	private String currentFolder = new File("/").getAbsolutePath();
 	private JTextField field = new JTextField(currentFolder);
 
 	private JComboBox<File> mountpoints = new JComboBox<File>(new MountpointsModel());
 	
 	private static final int REFRESH_DELAY = 10000; // TODO
 
 	public void changeDir()
 	{
 		listModel.setDirPath(this.currentFolder);
 		list.setSelectedIndex(0);
 	}
 	
 	public DirectoryPanel() {
 		//this.list.setPrototypeCellValue(new File(DirectoryPanel.maxLength));
 		
 		Timer timer = new Timer(REFRESH_DELAY, this);
 		timer.start();
 		setLayout(new BorderLayout());
 		
 		JPanel topPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints(
 			0, 0,
 			1, 1,
 			0, 0,
 			GridBagConstraints.CENTER,
 			GridBagConstraints.HORIZONTAL,
 			new Insets(2, 1, 2, 1),
 			5, 5
 		);
 				
 		JButton go = new JButton("GO");
 		go.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!new File(field.getText()).isDirectory())
 				{
 					field.setText(DirectoryPanel.this.currentFolder);
 					return;
 				}
 				
				String newPath = new File(field.getText()).getAbsolutePath().toLowerCase();

 				ComboBoxModel<File> model = DirectoryPanel.this.mountpoints.getModel();
 				int i;
 				for (i = 0; i < model.getSize(); i++)
 				{
 					String root = ((File)model.getElementAt(i)).getAbsolutePath().toLowerCase();
 					if (newPath.startsWith(root)) {
 						break;
 					}
 				}
 				if (i >= model.getSize())
 				{
 					return;
 				}
 				DirectoryPanel.this.currentFolder = newPath;
 				model.setSelectedItem(((File)model.getElementAt(i)));
 				DirectoryPanel.this.changeDir();
 			}
 		});
 		
 		this.mountpoints.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String currentPath = DirectoryPanel.this.currentFolder.toLowerCase();
 				String newRoot = ((File)DirectoryPanel.this.mountpoints.getSelectedItem()).getAbsolutePath().toLowerCase();
 				if (!currentPath.startsWith(newRoot))
 				{
 					DirectoryPanel.this.currentFolder = ((File)DirectoryPanel.this.mountpoints.getSelectedItem()).getAbsolutePath();
 					DirectoryPanel.this.field.setText(DirectoryPanel.this.currentFolder);
 					DirectoryPanel.this.changeDir();
 				}
 				
 			}
 		});
 		
 		gbc.weightx = 1;
 		topPanel.add(this.field, gbc);
 		gbc.weightx = 0; gbc.gridx++;
 		topPanel.add(go, gbc);
 		gbc.gridx++;
 		topPanel.add(this.mountpoints, gbc);
 		
 		/*JPanel menu = new JPanel(new BorderLayout());
 		menu.add(field, BorderLayout.CENTER);
 		menu.add(go, BorderLayout.LINE_END);
 		add(menu, BorderLayout.PAGE_START);*/
 		add(topPanel, BorderLayout.PAGE_START);
 		add(new JScrollPane(list), BorderLayout.CENTER);
 		
 		list.addKeyListener(new KeyListener()
 		{			
 			@Override
 			public void keyTyped(KeyEvent e) { }
 			
 			@Override
 			public void keyReleased(KeyEvent e) { }
 			
 			@Override
 			public void keyPressed(KeyEvent e)
 			{
 				if (e.getKeyCode() == KeyEvent.VK_ENTER)
 				{
 					@SuppressWarnings("unchecked")
 					JList<File> list = (JList<File>) e.getSource();
 					File file = list.getSelectedValue();
 					if (file.isDirectory())
 					{
 						DirectoryPanel.this.currentFolder = file.getAbsolutePath();
 						DirectoryPanel.this.field.setText(DirectoryPanel.this.currentFolder);
 						DirectoryPanel.this.changeDir();
 					}
 				}
 			}
 		});
 		
 		list.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent evt) {
 				@SuppressWarnings("unchecked")
 				JList<File> list = (JList<File>) evt.getSource();
 				if (evt.getClickCount() == 2) {
 					int index = list.locationToIndex(evt.getPoint());
 					File file = list.getModel().getElementAt(index);
 					if (file.isDirectory()) {
 						field.setText(file.getAbsolutePath());
 						listModel.setDirPath(field.getText());
 						list.setSelectedIndex(0);
 						currentFolder = field.getText();
 						// sets cell prototype to accelerate rendering
 						// of very long folders
 						list.setPrototypeCellValue(((FileListModel)list.getModel()).getLongestName());
 					}
 				}
 			}
 		});
 		list.setCellRenderer(new DefaultListCellRenderer() {
 			private static final long serialVersionUID = -8566161868989812047L;
 
 			@Override
 			public Component getListCellRendererComponent(
 					@SuppressWarnings("rawtypes") JList list,
 					Object value,
 					int index,
 					boolean isSelected,
 					boolean cellHasFocus)
 			{
 				JLabel jLabel = (JLabel) super.getListCellRendererComponent(
 						list,
 						value,
 						index,
 						isSelected,
 						cellHasFocus
 				);
 				if (value instanceof FirstFile) {
 					jLabel.setText("..");
 				} else {
 					jLabel.setIcon(FileSystemView.getFileSystemView().getSystemIcon((File) value));
 					jLabel.setText(value == null ? null : ((File) value).getName());
 				}
 				
 				return jLabel;
 		
 			}
 		});
 		list.addFocusListener(this);
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		refreshLists();
 	}
 
 	public void refreshLists() {
 		if (refreshInProgress) {
 			// previous refresh is in process, skip this round
 		} else {
 			refreshInProgress = true;
 			List<File> selectedValues = list.getSelectedValuesList();
 			listModel.refresh();
 			setSelectedValues(list, selectedValues);
 			refreshInProgress = false;
 		}
 	}
 	public void setSelectedValues(JList<File> list, List<File> values) {
 	    list.clearSelection();
 	    for (File value : values) {
 	        int index = getIndex(list.getModel(), value);
 	        if (index >=0) {
 	            list.addSelectionInterval(index, index);
 	        }
 	    }
 	}
 
 	public int getIndex(ListModel<File> model, Object value) {
 	    if (value == null) return -1;
 	    if (model instanceof DefaultListModel) {
 	        return ((DefaultListModel<File>) model).indexOf(value);
 	    }
 	    for (int i = 0; i < model.getSize(); i++) {
 	        if (value.equals(model.getElementAt(i))) return i;
 	    }
 	    return -1;
 	}
 
 	public String getCurrentFolder() {
 		return currentFolder;
 	}
 
 	public List<File> getSelectedFiles() {
 		return list.getSelectedValuesList();
 	}
 
 	@Override
 	public void focusGained(FocusEvent arg0) {
 		FocusEvent event = new FocusEvent(this, FocusEvent.FOCUS_GAINED);
 		for (FocusListener listener : listeners) {
 			listener.focusGained(event);
 		}
 	}
 
 	@Override
 	public void focusLost(FocusEvent arg0) {
 		FocusEvent event = new FocusEvent(this, FocusEvent.FOCUS_LOST);
 		for (FocusListener listener : listeners) {
 			listener.focusLost(event);			
 		}
 	}
 	
 	public void addFocusListener(FocusListener listener){
 		listeners.add(listener);
 	}
 
 	public void clearSelection() {
 		list.clearSelection();
 	}
 }
