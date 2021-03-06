 package at.netcrawler.assistant;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Locale;
 import java.util.regex.Pattern;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
import javax.swing.filechooser.FileFilter;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.LayoutStyle;
 import javax.swing.UIManager;
 
 import at.andiwand.library.cli.CommandLine;
 import at.andiwand.library.util.JFrameUtil;
import at.netcrawler.io.IgnoreFirstLineInputStream;
 import at.netcrawler.io.IgnoreLastLineInputStream;
 import at.netcrawler.io.ReadUntilMatchInputStream;
 import at.netcrawler.network.IPDeviceAccessor;
 import at.netcrawler.network.connection.ssh.LocalSSHConnection;
 import at.netcrawler.network.connection.ssh.SSHConnectionSettings;
 import at.netcrawler.network.connection.ssh.SSHVersion;
 import at.netcrawler.network.connection.telnet.LocalTelnetConnection;
 import at.netcrawler.network.connection.telnet.TelnetConnectionSettings;
 
 
 public class ConfigurationExecutor extends JFrame {
 	
 	private static final long serialVersionUID = 7767764252271031663L;
 	
 	private static final String TITLE = "Configuration Executor";
 	
 	private static final String BATCH_SUFFIX = "!executorEnd";
 	
 	private JLabel address = new JLabel();
 	private JLabel connection = new JLabel();
 	private JLabel port = new JLabel();
 	private JComboBox batches = new JComboBox();
	JButton execute = new JButton("Execute");
 	
 	private JFileChooser fileChooser = new JFileChooser();
 	
 	private Configuration configuration;
 	
 	public ConfigurationExecutor() {
 		setTitle(TITLE);
 		
 		JPanel panel = new JPanel();
 		JLabel addressLabel = new JLabel("Address:");
 		JLabel connectionLabel = new JLabel("Connection:");
 		JLabel portLabel = new JLabel("Port:");
 		JLabel batchLabel = new JLabel("Batch:");
 		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
 		
 		batches.setPreferredSize(new Dimension(150,
 				batches.getPreferredSize().height));
 		setEnabledAll(false);
 		
 		fileChooser.setFileFilter(new FileFilter() {
 			public String getDescription() {
 				return "Configuration file (*" + Configuration.FILE_SUFFIX + ")";
 			}
 			public boolean accept(File f) {
 				return f.isDirectory()
 						|| f.getName().endsWith(Configuration.FILE_SUFFIX);
 			}
 		});
 		
 		GroupLayout layout = new GroupLayout(panel);
 		layout.setAutoCreateContainerGaps(true);
 		layout.setAutoCreateGaps(true);
 		panel.setLayout(layout);
 		
 		layout.setHorizontalGroup(layout.createParallelGroup()
 				.addGroup(layout.createSequentialGroup()
 						.addGroup(layout.createParallelGroup()
 								.addComponent(addressLabel)
 								.addComponent(connectionLabel)
 								.addComponent(portLabel)
 								.addComponent(batchLabel)
 						)
 						.addGap(20)
 						.addGroup(layout.createParallelGroup()
 								.addComponent(address)
 								.addComponent(connection)
 								.addComponent(port)
 								.addComponent(batches)
 						)
 				)
 				.addComponent(separator)
 				.addComponent(execute, Alignment.TRAILING)
 		);
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup()
 						.addComponent(addressLabel)
 						.addComponent(address)
 				)
 				.addGroup(layout.createParallelGroup()
 						.addComponent(connectionLabel)
 						.addComponent(connection)
 				)
 				.addGroup(layout.createParallelGroup()
 						.addComponent(portLabel)
 						.addComponent(port)
 				)
 				.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
 						.addComponent(batchLabel)
 						.addComponent(batches)
 				)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
 						20, Short.MAX_VALUE)
 				.addComponent(separator, GroupLayout.PREFERRED_SIZE,
 						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addGap(10)
 				.addComponent(execute)
 		);
 		
 		execute.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doExecute();
 			}
 		});
 		
 		add(panel);
 		
 		JMenuBar menuBar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem open = new JMenuItem("Open...");
 		JMenuItem exit = new JMenuItem("Exit");
 		
 		menuBar.add(file);
 		file.add(open);
 		file.addSeparator();
 		file.add(exit);
 		
 		open.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doOpen();
 			}
 		});
 		
 		exit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ConfigurationExecutor.this.dispose();
 			}
 		});
 		
 		setJMenuBar(menuBar);
 		
 		pack();
 		setMinimumSize(getSize());
 	}
 	
 	private void setEnabledAll(boolean enabled) {
 		batches.setEnabled(enabled);
 		execute.setEnabled(enabled);
 	}
 	
 	private void doOpen() {
 		if (fileChooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION)
 			return;
 		
 		try {
 			open(fileChooser.getSelectedFile());
 			
 			setEnabledAll(true);
 		} catch (IOException e) {
 			e.printStackTrace();
 			ConfigurationDialog.showErrorDialog(this, e);
 		}
 	}
 	
 	public void doExecute() {
 		try {
 			execute();
 		} catch (IOException e) {
 			e.printStackTrace();
 			ConfigurationDialog.showErrorDialog(this, e);
 		}
 	}
 	
 	public void open(File file) throws IOException {
 		Configuration configuration = new Configuration();
 		configuration.readFromJsonFile(file, new EncryptionCallback() {
 			public String getPassword(Encryption encryption) {
 				return ConfigurationDialog
 						.showDecryptionDialog(ConfigurationExecutor.this);
 			}
 		});
 		
 		setConfiguration(configuration);
 		
 		setEnabled(true);
 	}
 	
 	private void execute() throws IOException {
 		CommandLine commandLine;
 		
 		IPDeviceAccessor accessor = new IPDeviceAccessor(
 				configuration.getAddress());
 		
		switch (configuration.getConnection()) {
 		case TELNET:
 			TelnetConnectionSettings telnetSettings = new TelnetConnectionSettings();
 			telnetSettings.setPort(configuration.getPort());
 			
 			commandLine = new LocalTelnetConnection(accessor, telnetSettings);
 			break;
 		case SSH1:
 		case SSH2:
 			SSHConnectionSettings sshSettings = new SSHConnectionSettings();
			sshSettings.setVersion(SSHVersion.VERSION2);
 			sshSettings.setPort(configuration.getPort());
 			sshSettings.setUsername(configuration.getUsername());
 			sshSettings.setPassword(configuration.getPassword());
 			
 			commandLine = new LocalSSHConnection(accessor, sshSettings);
 			break;
 		
 		default:
 			throw new IllegalStateException("Unreachable section");
 		}
 		
 		InputStream inputStream = commandLine.getInputStream();
 		OutputStream outputStream = commandLine.getOutputStream();
 		
 		if (configuration.getConnection() == Connection.TELNET) {
 			String username = configuration.getUsername();
 			String password = configuration.getPassword();
 			
 			if (!username.isEmpty()) {
 				outputStream.write(username.getBytes());
 				outputStream.write("\n".getBytes());
 				outputStream.write(password.getBytes());
 				outputStream.write("\n".getBytes());
 			} else if (!password.isEmpty()) {
 				outputStream.write(password.getBytes());
 				outputStream.write("\n".getBytes());
 			}
 		}
 		
 		Pattern endPattern = Pattern.compile(".*" + BATCH_SUFFIX);
 		String batch = configuration.getBatch((String) batches
 				.getSelectedItem());
 		
		outputStream.write(("\n" + batch + "\n" + BATCH_SUFFIX + "\n")
 				.getBytes());
 		outputStream.flush();
 		
		inputStream = new IgnoreFirstLineInputStream(inputStream);
 		inputStream = new ReadUntilMatchInputStream(inputStream, endPattern);
 		inputStream = new IgnoreLastLineInputStream(inputStream);
 		
 		commandLine.close();
 	}
 	
 	public void setConfiguration(Configuration configuration) {
 		this.configuration = configuration;
 		
 		address.setText(configuration.getAddress().toString());
 		connection.setText(configuration.getConnection().getName());
 		port.setText("" + configuration.getPort());
 		
 		batches.removeAllItems();
 		for (String batchName : configuration.getBatches().keySet()) {
 			batches.addItem(batchName);
 		}
 	}
 	
 	
 	public static void main(String[] args) throws Throwable {
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		JOptionPane.setDefaultLocale(Locale.US);
 		
 		ConfigurationExecutor configurationExecutor = new ConfigurationExecutor();
 		JFrameUtil.centerFrame(configurationExecutor);
 		configurationExecutor.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		configurationExecutor.setVisible(true);
 	}
 	
 }
