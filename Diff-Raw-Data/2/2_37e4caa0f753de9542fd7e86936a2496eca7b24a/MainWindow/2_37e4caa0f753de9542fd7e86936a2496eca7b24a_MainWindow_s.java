 package ru.vsu.example;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 public class MainWindow extends JFrame implements ActionListener {
 	
 	private static final int MENU_EXIT = 1;
 	private static final int MENU_NEW = 2;
 	private static final int MENU_OPEN = 3;
 	private static final int MENU_SAVE = 4;
 	private static final int MENU_SAVEAS = 5;
 	private static final int MENU_ABOUT = 6;
 	private JTextArea textArea; 
 	private boolean _isModified = false;
 	private String _fileName = "";
 	public MainWindow(String title) {
 		super(title);
 	}
 
 	public void openWindow() {
 		init();
 		setVisible(true);
 	}
 
 	private void init() {
 		try {
 			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
 					.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			System.err.println("L&Floadingerror");
 		}
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		setJMenuBar(createJMenuBar());
 		createTextArea();
 		setSizes();
 	}
 
 	private void setSizes() {
 		java.awt.Dimension dim = getToolkit().getScreenSize();
 		int dx = dim.width / 6;
 		int dy = dim.height / 6;
 		setSize(dx * 4, dy * 4);
 		setLocation(dx, dy);
 	}
 
 	private JMenuBar createJMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu = new JMenu("File");
 
 		menu.add(createMenuItem(MENU_NEW, "Create", this));
 		menuBar.add(menu);
 		
 		menu.add(createMenuItem(MENU_OPEN, "Open...", this));
 		menuBar.add(menu);
 		
 		menu.add(createMenuItem(MENU_SAVE, "Save", this));
 		menuBar.add(menu);
 
 		menu.add(createMenuItem(MENU_SAVEAS, "Save as...", this));
 		menuBar.add(menu);
 
 		menu.addSeparator();
 		menu.add(createMenuItem(MENU_EXIT, "Exit", this));
 		menuBar.add(menu);
 
 		menu = new JMenu("Help");
 		menu.add(createMenuItem(MENU_ABOUT, "About", this));
 		menuBar.add(menu);
 		return menuBar;
 	}
 	
 	private JMenuItemEx createMenuItem(int id, String title, ActionListener listener) {
 		JMenuItemEx menuItem = new JMenuItemEx(id, title);
 		menuItem.addActionListener(listener);
 		return menuItem;
 	}
 	
 	public JOptionPane createJOptionPane;
 	
 	private void createTextArea() {
 		textArea = new JTextArea();
 		textArea.getDocument().addDocumentListener(new DocumentListenerImpl());
        textArea.setFont((Font.decode(("Areal-PLAIN-16"))));
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		GridBagLayout layout = new GridBagLayout();
 		GridBagConstraints constraints = new GridBagConstraints();
 		getContentPane().setLayout(layout);
 		constraints.insets.left = constraints.insets.right = constraints.insets.bottom = constraints.insets.top = 2;
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.weightx = constraints.weighty = 1;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		layout.setConstraints(scrollPane, constraints);
 		scrollPane.setViewportView(textArea);
 		getContentPane().add(scrollPane);
 	}
 
 	private void createDocument() throws IOException {
 		if (_isModified) {
 			int result = JOptionPane.showConfirmDialog(this, "Save changes?");
 			switch(result) {
 				case JOptionPane.OK_OPTION:
 				   if(Saving(_fileName)) {
 					ClearParametrs();
                    }
 					break;
 				case JOptionPane.NO_OPTION:
 					ClearParametrs();
                  //   _isModified = false;
 					break;
 				case JOptionPane.CANCEL_OPTION:
 					break;
 			}
 		} else {
             ClearParametrs();
         }
 	}
 	
 	private void openDocument() throws IOException {
         if (_isModified) {
             int result = JOptionPane.showConfirmDialog(this, "Save changes?");
             switch(result) {
                 case JOptionPane.OK_OPTION:
                    if(Saving(_fileName)){
                        if (openFile()) {
                             ClearParametrs();
                        } else {
                             break;
                        }
                    } else {
                         break;
                    }
                 case JOptionPane.NO_OPTION:
                     ClearParametrs();
                     openFile();
                   //  _isModified = false;
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     break;
             }
         } else {
             openFile();
             _isModified = false;
         }
 
 	}
 	private void saveDocument() throws IOException {		
 		Saving(_fileName);
 	}
 	private void saveAsDocument() throws IOException {
 		String fileNameForSave = OpenFileDialog();
 		WriteIntoFile(fileNameForSave);	
 	}
 	
 	private boolean openFile() {
 		FileDialog fd = new FileDialog(this);
 		fd.setMode(FileDialog.LOAD);
 		fd.setVisible(true);
 		if (fd.getFile() != null) {
 			String fileName = fd.getDirectory() + fd.getFile();
 			try {
 				BufferedReader br = new BufferedReader(new FileReader(fileName));
 				String s;
 				while ((s = br.readLine()) != null)
 					textArea.append(s + "\n");
 				br.close();
                 _fileName = fileName;
                 return true;
 			}
 			catch (Exception e) {
 				showErrorMessage("Read file error: " + e.getMessage());
                 return false;
 			}
 		} else {
             return false;
         }
 	}
 	
 	private boolean Saving(String fileName) throws IOException {
 		if ( fileName.equals("") )   {
 			String fileNameForSaving = OpenFileDialog();
             if (fileNameForSaving == null)  {
                 return false;
             } else {
                 WriteIntoFile(fileNameForSaving);
                 _fileName = fileNameForSaving;
                 return true;
             }
 		} else {
 			WriteIntoFile(fileName);
 			//_fileName = fileName;
             return true;
 		}
 	}
 	
 	private String OpenFileDialog() {
 		FileDialog createFileDialog = new FileDialog(this);
 		createFileDialog.setMode(FileDialog.SAVE);
 		createFileDialog.setVisible(true);
 
         if (createFileDialog.getFile() == null) return null;
 
 		String fileNameForSaving = createFileDialog.getDirectory() + createFileDialog.getFile();
 		return fileNameForSaving;
 
 	}
 	
 	private void WriteIntoFile(String fileName) throws IOException {
 		FileWriter fstream = new FileWriter(fileName);
 		BufferedWriter out = new BufferedWriter(fstream);		 
 		out.write(textArea.getText());		    
 		out.close();
 	}
 	
 	private void ClearParametrs() {
 		_fileName = "";
 		textArea.setText("");
         _isModified = false;
 	}
 	
 	
 	
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() instanceof JMenuItemEx) {
 			switch (((JMenuItemEx)event.getSource()).getId()) {
 				case MENU_NEW : 
 				try {
 					createDocument();
 				} catch (IOException e2) {
 					// TODO Auto-generated catch block
 					e2.printStackTrace();
 				}
 					break;
 				case MENU_OPEN :
                     try {
                         openDocument();
                     } catch (IOException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     break;
 				case MENU_SAVE : 
 				try {
 					saveDocument();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 					break;
 				case MENU_SAVEAS : 
 				try {
 					saveAsDocument();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 					break;
 				case MENU_EXIT :
                     try {
                         closeWindow();
                     } catch (IOException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     break;
 				case MENU_ABOUT : 
 					showAppInfo();
 					break;
 			}
 			
 		}
 	}
 
 	protected void processWindowEvent(WindowEvent event) {
 		if (event.getID() == WindowEvent.WINDOW_CLOSING) {
             try {
                 closeWindow();
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         } else
 			super.processWindowEvent(event);
 	}
 
 	private void showAppInfo() {
 		JOptionPane.showMessageDialog(this, "Test GUI Application v 1.0", getTitle(), JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	private void showErrorMessage(String msg) {
 		JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.ERROR_MESSAGE);
 	}
 
 	private void closeWindow() throws IOException {
 		if (_isModified) {
             int result = JOptionPane.showConfirmDialog(this, "Save changes?");
             switch(result) {
                 case JOptionPane.OK_OPTION:
                     if(Saving(_fileName)) {
                     dispose();
                     System.exit(0);
                     }
                     break;
                 case JOptionPane.NO_OPTION:
                     dispose();
                     System.exit(0);
                     break;
                 case JOptionPane.CANCEL_OPTION:
                     break;
             }
         } else {
             dispose();
 		    System.exit(0);
         }
 	}
 	
 	private class DocumentListenerImpl implements DocumentListener {
 		@Override
 		public void insertUpdate(DocumentEvent e) {
 			_isModified = true;
 		}
 
 		@Override
 		public void removeUpdate(DocumentEvent e) {
 			_isModified = true;
 		}
 
 		@Override
 		public void changedUpdate(DocumentEvent e) {
 			_isModified = true;
 		}
 	}
 }
