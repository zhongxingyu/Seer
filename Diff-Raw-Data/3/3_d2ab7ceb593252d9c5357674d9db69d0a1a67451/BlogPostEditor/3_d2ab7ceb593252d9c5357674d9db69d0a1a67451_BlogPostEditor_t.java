 package de.freethoughts.atsutane.blogposteditor;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * This is a simple text editor for writing posts offline for blogsystems
  * like devbird  or others that use a tagging system for the posts.
  * @since 20100529
  * @author Thorsten 'Atsutane' Töpper
  */
 public class BlogPostEditor extends JFrame implements ActionListener {
 	private static final long serialVersionUID = -6111553995031987251L;
 	
 	private File currentPath, currentFile;
 	private JMenuItem newPost, openPost, savePost, saveAsPost, quit;
 	private JTextArea postText;
 	private JTextField postTitle, postTags;
 	private JButton clearTags;
 	
 	public BlogPostEditor() {
 		this.setTitle("PostEditor");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		this.currentPath = null;
 		this.currentFile = null;
 		
 		this.setLayout(new BorderLayout());
 		this.drawMenuBar();
 		this.drawMainPanel();
 		
 		this.postTags.setToolTipText("Multiple tags are simply divided by spaces.");
 		
 		this.pack();
 	}
 	
 	/**
 	 * This method is called by the constructor and draws the
 	 * menu bar for the JFrame.
 	 */
 	private void drawMenuBar() {
 		JMenuBar bar = new JMenuBar();
 		this.add(bar, BorderLayout.NORTH);
 		
 		/* Draw the File-Menu
 		 */
 		JMenu file = new JMenu("File");
 		bar.add(file);
 		
 		this.newPost = new JMenuItem("New");
 		this.newPost.addActionListener(this);
 		file.add(newPost);
 		
 		this.openPost = new JMenuItem("Open ...");
 		this.openPost.addActionListener(this);
 		file.add(this.openPost);
 		
 		this.savePost = new JMenuItem("Save");
 		this.savePost.addActionListener(this);
 		file.add(this.savePost);
 		
 		this.saveAsPost = new JMenuItem("Save As ...");
 		this.saveAsPost.addActionListener(this);
 		file.add(this.saveAsPost);
 		
 		this.quit = new JMenuItem("Quit");
 		this.quit.addActionListener(this);
 		file.add(this.quit);
 	}
 
 	/**
 	 * This method is called by the constructor and draws the mainPanel
 	 * located in the Center of the Window.
 	 */
 	private void drawMainPanel() {
 		JPanel mainPanel = new JPanel(new BorderLayout());
 		this.add(mainPanel, BorderLayout.CENTER);
 
 		/* Draw the panels at the top containing the postTitle and postTags
 		 * from the class JTextField and the clearTags object of JButton.
 		 */
 		JPanel topPanel = new JPanel(new GridLayout(2,1));
 		mainPanel.add(topPanel, BorderLayout.NORTH);
 		
 		JPanel upperTopPanel = new JPanel(new FlowLayout());
 		topPanel.add(upperTopPanel);
 		
 		upperTopPanel.add(new JLabel("Title:"));
 		this.postTitle = new JTextField(50);
 		upperTopPanel.add(this.postTitle);
 		
 		JPanel lowerTopPanel = new JPanel(new FlowLayout());
 		topPanel.add(lowerTopPanel);
 		
 		lowerTopPanel.add(new JLabel("Tags:"));
 		this.postTags = new JTextField(40);
 		lowerTopPanel.add(this.postTags);
 		
 		this.clearTags = new JButton("Clear Tags");
 		this.clearTags.addActionListener(this);
 		lowerTopPanel.add(this.clearTags);
 		
 		/* Draw the JScrollPane containing the JTextArea postText to the
 		 * mainPanel into the center of the BorderLayout.
 		 */
 		this.postText = new JTextArea(25, 55);
 		this.postText.setLineWrap(true);
 		JScrollPane scrollPane = new JScrollPane(this.postText);
 		scrollPane.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		mainPanel.add(scrollPane, BorderLayout.CENTER);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		JFileChooser chooser = new JFileChooser(this.currentPath);
 		chooser.setFileFilter(new FileNameExtensionFilter("Textfiles", "txt"));
 		
 		if (e.getSource() == this.newPost) {
 			/* Clean the postText, postTitle, postTags
 			 * and currentFile up. Path is kept.
 			 */
 			this.postText.setText("");
 			this.postTitle.setText("");
 			this.postTags.setText("");
 			this.currentFile = null;
 			
 		} else if (e.getSource() == this.savePost) {
 			if (this.currentFile == null) {
 				/* There was no file selected before, do so now.
 				 */
 				int retValue = chooser.showSaveDialog(this);
 				if (retValue == JFileChooser.APPROVE_OPTION) {
 					this.currentFile = chooser.getSelectedFile();
 					this.currentPath = chooser.getCurrentDirectory();
 					this.save();
 				}
 			} else { // File was selected, save it
 				this.save();
 			}
 			
 		} else if (e.getSource() == this.saveAsPost) {
 			/* show a dialog and let the user select
 			 * the file to which shall be saved to.
 			 */
 			int retValue = chooser.showSaveDialog(this);
 			if (retValue == JFileChooser.APPROVE_OPTION) {
 				this.currentFile = chooser.getSelectedFile();
 				this.currentPath = chooser.getCurrentDirectory();
 				this.save();
 			}
 			
 		} else if (e.getSource() == this.openPost) {
 			/* Read the title, text and tags from the choosen file
 			 */
 			int retValue = chooser.showOpenDialog(this);
 			if (retValue == JFileChooser.APPROVE_OPTION) {
 				this.currentFile = chooser.getSelectedFile();
 				this.currentPath = chooser.getCurrentDirectory();
 				this.load();
 			}
 			
 		} else if (e.getSource() == this.quit) {
 			System.exit(NORMAL);
 			
 		} else if (e.getSource() == this.clearTags) {
 			this.postTags.setText("");
 		}
 	}
 	
 	/**
 	 * Save the written Post
 	 * the title is saved as the first line, the tags follow
 	 * and the text is written afterwards
 	 */
 	private void save() {
 		
 		try {
 			FileWriter writer = new FileWriter(this.currentFile);
 			
 			writer.write(this.postTitle.getText()+"\n");
 			writer.write(this.postTags.getText()+"\n");
 			writer.write(this.postText.getText());
 			
 			writer.close();
 		} catch (NullPointerException npSave) {
 			// ignore it for now
 			
 		} catch (Exception eSave) {
 			/* Something did not work, give a user a dialogue and
 			 * print the details to the stderr.
 			 */
 			JOptionPane.showMessageDialog(this, "File could not be Written",
 					"Error", JOptionPane.ERROR_MESSAGE);
 			eSave.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Load the selected file into the Editor.
 	 */
 	private void load() {
 		try {
 			BufferedReader reader = new BufferedReader(
 					new FileReader(this.currentFile));
 			
 			this.postTitle.setText(reader.readLine().trim());
 			this.postTags.setText(reader.readLine().trim());
 			
 			/* Read the postText from the rest of the file.
			 * but first clear the postText JTextArea.
 			 */
			this.postText.setText("");
 			while (reader.ready()) {
 				this.postText.append(reader.readLine());
 			}
 			
 			reader.close();
 		} catch (NullPointerException npLoad) {
 			// ignore it for now
 			
 		} catch (FileNotFoundException fnfOpenButton) {
 			/* The file was removed while we chose it?!
 			 * Print a special Message.
 			 */
 			JOptionPane.showMessageDialog(this,
 					"The choosen file was not found.", "Error",
 					JOptionPane.ERROR_MESSAGE);
 			fnfOpenButton.printStackTrace();
 			
 		} catch (Exception eOpenButton) {
 			/* Catch any other unexpected Exception.
 			 */
 			JOptionPane.showMessageDialog(this,
 					"The choosen file could not be opened.", "Error",
 					JOptionPane.ERROR_MESSAGE);
 			eOpenButton.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) {
 		/* Start the editor
 		 */
 		new BlogPostEditor().setVisible(true);
 	}
 }
