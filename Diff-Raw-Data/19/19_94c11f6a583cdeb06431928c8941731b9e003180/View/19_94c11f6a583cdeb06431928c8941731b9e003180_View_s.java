 package com.se1by;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 public class View extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	private JFileChooser jfco = new JFileChooser();
 	private JButton chooseFileButton = new JButton();
 	private JTextField jTextField1 = new JTextField();
 	private JLabel headingLabel = new JLabel();
 	private JCheckBox allFilesCheck = new JCheckBox();
 	private JLabel artistLabel = new JLabel();
 	private JTextField artistTextBox = new JTextField();
 	private JLabel titleLabel = new JLabel();
 	private JTextField titleTextBox = new JTextField();
 	private JLabel albumLabel = new JLabel();
 	private JTextField albumTextBox = new JTextField();
 	private JLabel genreLabel = new JLabel();
 	private JTextField genreTextBox = new JTextField();
 	private JLabel trackLabel = new JLabel();
 	private JLabel yearLabel = new JLabel();
 	private JTextField trackTextBox = new JTextField();
 	private JTextField yearTextBox = new JTextField();
 	private JLabel nameLabel = new JLabel();
 	private JTextField nameTextBox = new JTextField();
 	private JButton goButton = new JButton();
 	private Logic logic = null;
 
 	public View(String title) {
 		// Frame-Initialisierung
 		super(title);
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		int frameWidth = 300;
 		int frameHeight = 536;
 		setSize(frameWidth, frameHeight);
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		int x = (d.width - getSize().width) / 2;
 		int y = (d.height - getSize().height) / 2;
 		setLocation(x, y);
 		setResizable(false);
 		Container cp = getContentPane();
 		// Anfang Komponenten
 		chooseFileButton.setBounds(176, 56, 105, 25);
 		chooseFileButton.setText("choose file");
 		chooseFileButton.setMargin(new Insets(2, 2, 2, 2));
 		chooseFileButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				chooseFileButton_ActionPerformed(evt);
 			}
 		});
 		cp.add(chooseFileButton);
 		jTextField1.setBounds(8, 56, 161, 25);
 		cp.add(jTextField1);
 		headingLabel.setBounds(32, 8, 235, 41);
 		headingLabel.setText("ID3Tagger");
 		headingLabel.setHorizontalTextPosition(SwingConstants.CENTER);
 		headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				"MP3 & WMA Files", "mp3", "wma");
 		jfco.setFileFilter(filter);
 		jfco.setCurrentDirectory(new File(System.getProperty("user.home")
 				+ "\\Music"));
 		cp.add(headingLabel);
 
 		allFilesCheck.setBounds(8, 440, 273, 17);
 		allFilesCheck.setText("Change all files in directory");
 		allFilesCheck.setOpaque(false);
 		cp.add(allFilesCheck);
		artistLabel.setBounds(8, 88, 107, 19);
 		artistLabel.setText("Artist");
 		cp.add(artistLabel);
 		artistTextBox.setBounds(8, 112, 273, 25);
 		cp.add(artistTextBox);
		titleLabel.setBounds(8, 144, 107, 19);
 		titleLabel.setText("Title");
 		cp.add(titleLabel);
 		titleTextBox.setBounds(8, 168, 273, 25);
 		cp.add(titleTextBox);
		albumLabel.setBounds(8, 200, 107, 19);
 		albumLabel.setText("Album");
 		cp.add(albumLabel);
 		albumTextBox.setBounds(8, 224, 273, 25);
 		cp.add(albumTextBox);
		genreLabel.setBounds(8, 256, 107, 19);
 		genreLabel.setText("Genre");
 		cp.add(genreLabel);
 		genreTextBox.setBounds(8, 280, 273, 25);
 		cp.add(genreTextBox);
		trackLabel.setBounds(56, 312, 35, 25);
 		trackLabel.setText("Track");
 		cp.add(trackLabel);
		yearLabel.setBounds(184, 312, 35, 25);
 		yearLabel.setText("Year");
 		cp.add(yearLabel);
 		trackTextBox.setBounds(24, 344, 105, 25);
 		trackTextBox.setText("");
 		cp.add(trackTextBox);
 		yearTextBox.setBounds(152, 344, 105, 25);
 		yearTextBox.setText("");
 		cp.add(yearTextBox);
 		nameLabel.setBounds(16, 376, 147, 25);
 		nameLabel.setText("New name");
 		cp.add(nameLabel);
 		nameTextBox.setBounds(8, 408, 273, 25);
 		cp.add(nameTextBox);
 		goButton.setBounds(8, 464, 273, 33);
 		goButton.setText("GO!");
 		goButton.setMargin(new Insets(2, 2, 2, 2));
 		goButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				goButton_ActionPerformed(evt);
 			}
 		});
 		cp.add(goButton);
 
 		cp.setLayout(null);
 		// Ende Komponenten
 
 		setVisible(true);
 	} // end of public ID3
 
 	// Anfang Methoden
 	public File jfcoOpenFile() {
 		jfco.setDialogTitle("Choose file");
 		if (jfco.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			return jfco.getSelectedFile();
 		} else {
 			return null;
 		}
 	}
 
 	public String jfcoOpenFilename() {
 		jfco.setDialogTitle("Choose file");
 		if (jfco.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			return jfco.getSelectedFile().getPath();
 		} else {
 			return null;
 		}
 	}
 
 	public void chooseFileButton_ActionPerformed(ActionEvent evt) {
 		String file = jfcoOpenFilename();
 		logic = new Logic(new File(file));
 		jTextField1.setText(file);
 		refresh();
 		System.out.println(logic.getArtist());
 		System.out.println(logic.getTitle());
 	}
 
 	private void goButton_ActionPerformed(ActionEvent evt) {
 		if (allFilesCheck.isSelected()) {
 			File dir = new File(logic.getSourceFile().getParent());
 			changeAllFiles(dir);
 		} else {
 			String name = nameTextBox.getText();
 			if (name.contains("#")) {
 				if (name.toLowerCase().contains("#artist"))
 					name = name.replace("#artist", logic.getArtist());
 				else if (name.contains("#title"))
 					name = name.replace("#title", logic.getTitle());
 			}
 			if (name.contains(".mp3"))
 				name = name.replace(".mp3", "");
 			if (name.contains(".wma"))
 				name = name.replace(".wma", "");
 			setAtributes(logic);
 			logic.saveFile(name);
 			refresh	();
 			JOptionPane.showMessageDialog(null, "Done!", "Done!", JOptionPane.INFORMATION_MESSAGE);
 		}
 	}
 
 	public void refresh() {
 		artistTextBox.setText(logic.getArtist());
 		titleTextBox.setText(logic.getTitle());
 		albumTextBox.setText(logic.getAlbum());
 		genreTextBox.setText(logic.getGenre().toString());
 		trackTextBox.setText(logic.getTrack().toString());
 		yearTextBox.setText(logic.getYear().toString());
 		nameTextBox.setText(logic.getSourceFile().getName());
 	}
 
 	public void setAtributes(Logic logic) {
 		logic.setAlbum(albumTextBox.getText());
 		logic.setArtist(artistTextBox.getText());
 		logic.setGenre(genreTextBox.getText());
 		logic.setTitle(titleTextBox.getText());
 		logic.setTrack(trackTextBox.getText());
 		logic.setYear(yearTextBox.getText());
 	}
 
 	private void changeAllFiles(File f) {
 		if (f.isDirectory()) {
 			for (File file : f.listFiles())
 				changeAllFiles(file);
 			return;
 		}
 		String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);
 		if (!(ext.equalsIgnoreCase("mp3") || ext.equalsIgnoreCase("wma")))
 			return;
 		Logic l = new Logic(f);
 		String name = replaceTags(nameTextBox.getText(), l);
 		l.saveFile(name);
 	}
 
 	private String replaceTags(String name, Logic logic) {
 		if (name.contains("#")) {
 			if (name.toLowerCase().contains("#artist"))
 				name = name.replace("#artist", logic.getArtist());
 			if (name.contains("#title"))
 				name = name.replace("#title", logic.getTitle());
 		}
 		if (name.contains(".mp3"))
 			name = name.replace(".mp3", "");
 		if (name.contains(".wma"))
 			name = name.replace(".wma", "");
 		return name;
 	}
 
 	// Ende Methoden
 
 	public static void main(String[] args) {
 		new View("ID3Tagger");
 	} // end of main
 
 } // end of class ID3
 
