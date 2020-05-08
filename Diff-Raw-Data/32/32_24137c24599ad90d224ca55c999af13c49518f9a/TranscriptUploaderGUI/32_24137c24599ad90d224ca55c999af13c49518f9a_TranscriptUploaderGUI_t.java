 package transcript;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.MouseInputListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import com.google.gdata.data.youtube.PlaylistLinkEntry;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 public class TranscriptUploaderGUI {
 
	private JFrame frmTranscriptUploader;
 	private JTextField txtUsername;
 	private JPasswordField pwdPassword;
 	private JTextField txtFolderPath;
 	private JComboBox comboBox;
 	private JButton btnLogin;
 	private JTextArea txtrConsole;
 	private JButton btnCheckTranscripts;
 	private JButton btnUploadTranscripts;
 
 	private TranscriptUploader transcriptUploader;
 	private List<PlaylistLinkEntry> playlistLinkEntries;
 	private JButton btnSelectFiles;
 	private JTextField txtTranscriptCount;
 	private JLabel lblUploadSuccess;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					TranscriptUploaderGUI window = new TranscriptUploaderGUI();
					window.frmTranscriptUploader.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public TranscriptUploaderGUI() {
 		initialize();
 		redirectSystemStreams();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 
		frmTranscriptUploader = new JFrame();
		frmTranscriptUploader.setTitle("Transcript Uploader");
		frmTranscriptUploader.setMinimumSize(new Dimension(640, 480));
		frmTranscriptUploader.setBounds(100, 100, 450, 300);
		frmTranscriptUploader.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0,
 				Double.MIN_VALUE };
		frmTranscriptUploader.getContentPane().setLayout(gridBagLayout);
		frmTranscriptUploader.setVisible(true);
 
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
		frmTranscriptUploader.getContentPane().add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] { 62, 134, 59, 78, 0 };
 		gbl_panel.rowHeights = new int[] { 28, 0, 0 };
 		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0,
 				Double.MIN_VALUE };
 		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		panel.setLayout(gbl_panel);
 
 		JLabel lblUsername = new JLabel("YouTube Username");
 		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
 		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUsername.gridx = 0;
 		gbc_lblUsername.gridy = 0;
 		panel.add(lblUsername, gbc_lblUsername);
 
 		txtUsername = new JTextField();
 		txtUsername.setText("Username");
 		GridBagConstraints gbc_txtUsername = new GridBagConstraints();
 		gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtUsername.anchor = GridBagConstraints.NORTH;
 		gbc_txtUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_txtUsername.gridx = 1;
 		gbc_txtUsername.gridy = 0;
 		panel.add(txtUsername, gbc_txtUsername);
 		txtUsername.setColumns(10);
 
 		JLabel lblPassword = new JLabel("Password");
 		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
 		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPassword.gridx = 2;
 		gbc_lblPassword.gridy = 0;
 		panel.add(lblPassword, gbc_lblPassword);
 
 		pwdPassword = new JPasswordField();
 		pwdPassword.setText("Password");
 		GridBagConstraints gbc_pwdPassword = new GridBagConstraints();
 		gbc_pwdPassword.insets = new Insets(0, 0, 5, 0);
 		gbc_pwdPassword.fill = GridBagConstraints.HORIZONTAL;
 		gbc_pwdPassword.anchor = GridBagConstraints.NORTH;
 		gbc_pwdPassword.gridx = 3;
 		gbc_pwdPassword.gridy = 0;
 		panel.add(pwdPassword, gbc_pwdPassword);
 
 		btnLogin = new JButton("Login");
 		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
 		gbc_btnLogin.gridwidth = 4;
 		gbc_btnLogin.insets = new Insets(0, 0, 0, 5);
 		gbc_btnLogin.gridx = 0;
 		gbc_btnLogin.gridy = 1;
 		panel.add(btnLogin, gbc_btnLogin);
 
 		JPanel panel_1 = new JPanel();
 		panel_1.setBorder(BorderFactory
 				.createEtchedBorder(EtchedBorder.LOWERED));
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 1;
		frmTranscriptUploader.getContentPane().add(panel_1, gbc_panel_1);
 		GridBagLayout gbl_panel_1 = new GridBagLayout();
 		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
 		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0 };
 		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0,
 				Double.MIN_VALUE };
 		gbl_panel_1.rowWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
 		panel_1.setLayout(gbl_panel_1);
 
 		JLabel lblPlaylist = new JLabel("Playlist");
 		lblPlaylist.setHorizontalAlignment(SwingConstants.RIGHT);
 		GridBagConstraints gbc_lblPlaylist = new GridBagConstraints();
 		gbc_lblPlaylist.anchor = GridBagConstraints.EAST;
 		gbc_lblPlaylist.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPlaylist.gridx = 0;
 		gbc_lblPlaylist.gridy = 0;
 		panel_1.add(lblPlaylist, gbc_lblPlaylist);
 
 		comboBox = new JComboBox();
 		GridBagConstraints gbc_comboBox = new GridBagConstraints();
 		gbc_comboBox.gridwidth = 2;
 		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
 		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox.gridx = 1;
 		gbc_comboBox.gridy = 0;
 		panel_1.add(comboBox, gbc_comboBox);
 
 		JLabel lblTranscripts = new JLabel("Transcripts");
 		lblTranscripts.setHorizontalAlignment(SwingConstants.RIGHT);
 		GridBagConstraints gbc_lblTranscripts = new GridBagConstraints();
 		gbc_lblTranscripts.anchor = GridBagConstraints.EAST;
 		gbc_lblTranscripts.gridheight = 2;
 		gbc_lblTranscripts.insets = new Insets(0, 0, 0, 5);
 		gbc_lblTranscripts.gridx = 0;
 		gbc_lblTranscripts.gridy = 1;
 		panel_1.add(lblTranscripts, gbc_lblTranscripts);
 
 		txtFolderPath = new JTextField();
 		txtFolderPath.setEditable(false);
 		txtFolderPath.setText("Folder Path");
 		GridBagConstraints gbc_txtFolderPath = new GridBagConstraints();
 		gbc_txtFolderPath.insets = new Insets(0, 0, 5, 5);
 		gbc_txtFolderPath.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtFolderPath.gridx = 1;
 		gbc_txtFolderPath.gridy = 1;
 		panel_1.add(txtFolderPath, gbc_txtFolderPath);
 		txtFolderPath.setColumns(10);
 
 		btnSelectFiles = new JButton("Select Files");
 		GridBagConstraints gbc_btnSelectFiles = new GridBagConstraints();
 		gbc_btnSelectFiles.gridheight = 2;
 		gbc_btnSelectFiles.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSelectFiles.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnSelectFiles.gridx = 2;
 		gbc_btnSelectFiles.gridy = 1;
 		panel_1.add(btnSelectFiles, gbc_btnSelectFiles);
 		btnSelectFiles.addActionListener(new FilesListener());
 		btnSelectFiles.setEnabled(false);
 
 		txtTranscriptCount = new JTextField();
 		txtTranscriptCount.setEditable(false);
 		txtTranscriptCount.setText("Transcript Count");
 		GridBagConstraints gbc_txtTranscriptCount = new GridBagConstraints();
 		gbc_txtTranscriptCount.insets = new Insets(0, 0, 0, 5);
 		gbc_txtTranscriptCount.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtTranscriptCount.gridx = 1;
 		gbc_txtTranscriptCount.gridy = 2;
 		panel_1.add(txtTranscriptCount, gbc_txtTranscriptCount);
 		txtTranscriptCount.setColumns(10);
 
 		JPanel panel_2 = new JPanel();
 		panel_2.setBorder(BorderFactory
 				.createEtchedBorder(EtchedBorder.LOWERED));
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.fill = GridBagConstraints.BOTH;
 		gbc_panel_2.gridx = 0;
 		gbc_panel_2.gridy = 2;
		frmTranscriptUploader.getContentPane().add(panel_2, gbc_panel_2);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[] { 0, 0, 0 };
 		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0 };
 		gbl_panel_2.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
 		gbl_panel_2.rowWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
 		panel_2.setLayout(gbl_panel_2);
 
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridheight = 3;
 		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 0;
 		panel_2.add(scrollPane, gbc_scrollPane);
 
 		txtrConsole = new JTextArea();
 		txtrConsole.setLineWrap(true);
 		txtrConsole.setEditable(false);
 		txtrConsole.setWrapStyleWord(true);
 		txtrConsole.setText("Console");
 		scrollPane.setViewportView(txtrConsole);
 		txtrConsole.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
 
 		btnCheckTranscripts = new JButton("Check Transcripts");
 		GridBagConstraints gbc_btnCheckTranscripts = new GridBagConstraints();
 		gbc_btnCheckTranscripts.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnCheckTranscripts.insets = new Insets(0, 0, 5, 0);
 		gbc_btnCheckTranscripts.gridx = 1;
 		gbc_btnCheckTranscripts.gridy = 0;
 		panel_2.add(btnCheckTranscripts, gbc_btnCheckTranscripts);
 
 		btnUploadTranscripts = new JButton("Upload Transcripts");
 		GridBagConstraints gbc_btnUploadTranscripts = new GridBagConstraints();
 		gbc_btnUploadTranscripts.insets = new Insets(0, 0, 5, 0);
 		gbc_btnUploadTranscripts.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnUploadTranscripts.gridx = 1;
 		gbc_btnUploadTranscripts.gridy = 1;
 		panel_2.add(btnUploadTranscripts, gbc_btnUploadTranscripts);
 
 		lblUploadSuccess = new JLabel("Upload Success!");
 		lblUploadSuccess.setVisible(false);
 		lblUploadSuccess.setForeground(Color.GREEN);
 		GridBagConstraints gbc_lblUploadSuccess = new GridBagConstraints();
 		gbc_lblUploadSuccess.gridx = 1;
 		gbc_lblUploadSuccess.gridy = 2;
 		panel_2.add(lblUploadSuccess, gbc_lblUploadSuccess);
 
 		btnLogin.addActionListener(new LoginListener());
 		btnCheckTranscripts.addActionListener(new CheckListener());
 		btnUploadTranscripts.addActionListener(new UploadListener());
 		txtUsername.addMouseListener(new TextListener());
 		pwdPassword.addMouseListener(new TextListener());
 
 		comboBox.setEnabled(false);
 		btnCheckTranscripts.setEnabled(false);
 		btnUploadTranscripts.setEnabled(false);
 	}
 
 	public class LoginListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 
 			String username = txtUsername.getText();
 			String password = String.valueOf(pwdPassword.getPassword());
 
 			try {
 				transcriptUploader = new TranscriptUploader(username, password);
 
 				playlistLinkEntries = transcriptUploader
 						.getPlaylistLinkEntries();
 
 				for (PlaylistLinkEntry a : playlistLinkEntries) {
 					System.out.println(a.getTitle().getPlainText());
 				}
 				comboBox.removeAllItems();
 
 				for (PlaylistLinkEntry entry : playlistLinkEntries) {
 					comboBox.addItem(entry.getTitle().getPlainText());
 				}
 
 				comboBox.setEnabled(true);
 				btnSelectFiles.setEnabled(true);
 
 			} catch (AuthenticationException e) {
 				System.out
 						.println("Wrong username / password combination. Try again.");
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public class FilesListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			JFileChooser fileChooser = new JFileChooser();
 			FileNameExtensionFilter filter = new FileNameExtensionFilter(
 					".txt Transcripts .sbv Subtitles", "sbv", "txt");
 			fileChooser.setFileFilter(filter);
 			fileChooser.setCurrentDirectory(new File(System
 					.getProperty("user.dir")));
 			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			fileChooser.setMultiSelectionEnabled(true);
 
			int returnVal = fileChooser.showOpenDialog(frmTranscriptUploader);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 
 				File[] files = fileChooser.getSelectedFiles();
 				try {
 					txtTranscriptCount
 							.setText(String.valueOf(transcriptUploader
 									.parseTranscripts(files)));
 
 					btnCheckTranscripts.setEnabled(true);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public class CheckListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 
 			PlaylistLinkEntry chosenPlaylist = playlistLinkEntries.get(comboBox
 					.getSelectedIndex());
 
 			System.out.println("Chosen Playlist: "
 					+ chosenPlaylist.getTitle().getPlainText());
 
 			try {
 				int playlistSize = transcriptUploader
 						.parsePlaylist(chosenPlaylist);
 
 				System.out.println("Playlist contains " + playlistSize
 						+ " videos");
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			System.out.println();
 
 			if (transcriptUploader.preUploadCheck()) {
 				System.out
 						.println("Number of Transcripts is the same as Number of Videos in Playlist. Safe to Upload.");
 
 				System.out.println();
 
 				System.out.println("Order of Transcripts:");
 				System.out.println("=====================");
 				System.out.println("(Video ID : Transcript File Name)");
 				System.out.println();
 
 				ArrayList<File> txts = transcriptUploader.getTxts();
 				ArrayList<String> videoIds = transcriptUploader.getVideoIds();
 
 				for (int i = 0; i < txts.size(); i++) {
 					System.out.println(videoIds.get(i) + " : "
 							+ txts.get(i).getName());
 				}
 				System.out.println();
 
 				btnUploadTranscripts.setEnabled(true);
 			} else {
 				System.out
 						.println("Number of Transcripts not equal Number of Videos in Playlist. Cannot Upload.");
 			}
 
 		}
 	}
 
 	public class TextListener implements MouseInputListener {
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			if (e.getSource().equals(txtUsername)) {
 				txtUsername.setText("");
 			} else if (e.getSource().equals(pwdPassword)) {
 				pwdPassword.setText("");
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 	}
 
 	public class UploadListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 
 			UploadWorker uploadWorker = new UploadWorker();
 			uploadWorker.execute();
 			System.out.println("Working...");
 		}
 	}
 
 	public class UploadWorker extends SwingWorker<Boolean, Void> {
 
 		@Override
 		protected Boolean doInBackground() throws Exception {
 
 			return transcriptUploader.uploadAll();
 		}
 
 		protected void done() {
 			try {
 				if (get()) {
 					lblUploadSuccess.setVisible(true);
 				}
 				btnUploadTranscripts.setEnabled(false);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void redirectSystemStreams() {
 		txtrConsole.setText("");
 
 		OutputStream out = new ConsoleOutputStream();
 
 		System.setOut(new PrintStream(out, true));
 		System.setErr(new PrintStream(out, true));
 	}
 
 	private class ConsoleOutputStream extends OutputStream {
 
 		private void updateTextArea(String text) {
 			SwingUtilities.invokeLater(new updateTextRunnable(text));
 		}
 
 		@Override
 		public void write(int arg0) throws IOException {
 			updateTextArea(String.valueOf((char) arg0));
 		}
 
 		@Override
 		public void write(byte[] b, int off, int len) throws IOException {
 			updateTextArea(new String(b, off, len));
 		}
 
 		@Override
 		public void write(byte[] b) throws IOException {
 			write(b, 0, b.length);
 		}
 
 		private class updateTextRunnable implements Runnable {
 
 			String text;
 
 			public updateTextRunnable(String text) {
 				this.text = text;
 			}
 
 			@Override
 			public void run() {
 				txtrConsole.append(text);
 			}
 
 		}
 
 	}
 }
