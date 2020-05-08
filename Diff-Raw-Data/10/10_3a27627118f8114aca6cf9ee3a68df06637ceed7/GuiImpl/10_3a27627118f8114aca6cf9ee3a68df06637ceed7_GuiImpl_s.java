 package main.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
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
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import main.algorithm.BPVSS;
 
 public class GuiImpl extends JFrame {
 
 	private static final long serialVersionUID = -2301605985365458803L;
 	private String pathSecret = "";
 	private String pathCover = "";
 	private String secret = "";
 	private String cover = "";
 	private String pathUnion1 = "";
 	private String union1 = "";
 	private String pathUnion2 = "";
 	private String union2 = "";
 
 	public GuiImpl() {
 
 		menu();
 		init();
 
 		setTitle("BPVSS");
 		setSize(500, 400);
 		setLocationRelativeTo(null);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				messageExit();
 			}
 		});
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 
 	}
 
 	public void init() {
 
 		JPanel basic = new JPanel();
 		basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));
 		add(basic);
 
 		JPanel buttonPanel = new JPanel(new BorderLayout(0, 0));
 
 		JPanel participantPanel = new JPanel(new FlowLayout());
 		participantPanel.setBorder(BorderFactory
 				.createEmptyBorder(0, 25, 0, 25));
 		JLabel label = new JLabel("Participantes");
 		label.setToolTipText("Nmero de participantes");
 		final JTextField input = new JTextField();
 		input.setPreferredSize(new Dimension(50, 20));
 		input.setEditable(true);
 
 		final String[] algorithms = { "Algoritmo noise-like",
 				"Algoritmo meaningful share" };
 		final JComboBox<String> comboBox = new JComboBox<String>(algorithms);
 		comboBox.setSelectedIndex(-1);
 		comboBox.setToolTipText("Algoritmo para ocultar la imagen");
 		comboBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));
 
 		JPanel startPanel = new JPanel(new FlowLayout());
 		startPanel.setBorder(BorderFactory.createEmptyBorder(35, 0, 0, 0));
 		JButton startButton = new JButton("Ejecutar");
 		JButton joinButton = new JButton("Unir shares");
 		startButton.setToolTipText("Ejecutar el algoritmo seleccionado");
 		joinButton.setToolTipText("Unir dos shares para obtener el descifrado");
 
 		joinButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 
 				JFileChooser fc = new JFileChooser();
 				int returnVal = fc.showOpenDialog(null);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					String tmpCover = file.getName();
 					if (checkFormat(tmpCover)) {
 						pathUnion1 = file.getParent().replace("\\", "/") + "/";
 						union1 = file.getName();
 						System.out.println(pathUnion1);
 						System.out.println(union1);
 						JOptionPane
 								.showMessageDialog(null,
 										"Primera share a unir cargada correctamente.Cargue la segunda share.");
 					}
 
 				}
 
 				returnVal = fc.showOpenDialog(null);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					String tmpCover = file.getName();
 					if (checkFormat(tmpCover)) {
 						pathUnion2 = file.getParent().replace("\\", "/") + "/";
 						union2 = file.getName();
 						System.out.println(pathUnion2);
 						System.out.println(union2);
 						JOptionPane.showMessageDialog(null,
 								"Segunda share a unir cargada correctamente.");
 					}
 
 				}
 				BPVSS bpvss = new BPVSS(pathUnion1, 3);
 				bpvss.joinImages(pathUnion1 + union1, pathUnion2 + union2,
 						"joinShare.png");
 
 			}
 		});
 
 		startButton.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 
 				if (comboBox.getSelectedIndex() == -1) {
 					JOptionPane.showMessageDialog(null,
 							"Elija uno de los dos algoritmos.");
 				} else if (comboBox.getSelectedIndex() == 0) {
 					if (pathSecret.equals("") || secret.equals("")) {
 						JOptionPane
 								.showMessageDialog(null,
 										"Cargue la imagen secreta antes de ejecutar el algoritmo.");
 					} else {
 						try {
 							algorithmNoiseLike(input);
 							JOptionPane.showMessageDialog(null,
 									"El resultado ha sido guardado en "
 											+ pathSecret);
 						} catch (Exception e) {
 							JOptionPane
 									.showMessageDialog(null,
 											"Por favor introduzca un nmero vlido mayor que 0");
 						}
 
 					}
 
 				} else {
 					if (pathSecret.equals("") || secret.equals("")) {
 						JOptionPane
 								.showMessageDialog(null,
 										"Cargue la imagen secreta antes de ejecutar el algoritmo.");
 					} else if (pathCover.equals("") || cover.equals("")) {
 						JOptionPane
 								.showMessageDialog(
 										null,
 										"Cargue la imagen de fondo para cubrir la secreta antes de ejecutar el algoritmo.");
 					} else {
 						try {
 							algorithmMeaningfulShares(input);
 							JOptionPane.showMessageDialog(null,
 									"El resultado ha sido guardado en "
 											+ pathSecret);
 						} catch (Exception e) {
 							JOptionPane
 									.showMessageDialog(null,
 											"Por favor introduzca un nmero vlido mayor que 0");
 						}
 
 					}
 				}
 
 			}
 		});
 		startPanel.add(joinButton);
 		startPanel.add(startButton);
 
 		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
 		JTextPane pane = new JTextPane();
 
 		pane.setContentType("text/html");
 		String text = "<b>Algoritmo BPVSS</b>"
 				+ "<p>Primero tiene que eligir el tipo de algoritmo que desea usar:<br>"
 				+ "<b>(1)Noise-like:</b> cargue la imagen secreta dirigindose a Archivo > Cargar imagen secreta o pulsando Ctrl-S.<br>"
 				+ "<b>(2)Meaningful share:</b> cargue la imagen secreta de la forma anterior y la imagen de fondo dirigindose a Archivo > Cargar imagen de fondo o pulsando Ctrl-F.</p> "
				+ "<p>Finalmente introduzca el nmero de participantes y haga click en ejecutar.</p>";
 		pane.setText(text);
 		pane.setEditable(false);
 		textPanel.add(pane);
 
 		basic.add(textPanel);
 
 		participantPanel.add(comboBox);
 		participantPanel.add(label);
 		participantPanel.add(input);
 		startPanel.add(startButton);
 		buttonPanel.add(participantPanel, BorderLayout.NORTH);
 		buttonPanel.add(startPanel);
 		basic.add(buttonPanel);
 	}
 
 	public void messageExit() {
 		int confirmed = JOptionPane.showConfirmDialog(null,
 				"Est seguro que desea salir?", "Salir",
 				JOptionPane.YES_NO_OPTION);
 
 		if (confirmed == JOptionPane.YES_OPTION) {
 			dispose();
 		}
 	}
 
 	public void menu() {
 
 		JMenuBar menuBar = new JMenuBar();
 		URL pathIcon = getClass().getResource("/resources/exit.jpg");
 		System.out.println(pathIcon);
 		ImageIcon exitIcon = new ImageIcon(getClass().getResource(
 				"/resources/exit.jpg"));
 		ImageIcon loadSecret = new ImageIcon(getClass().getResource(
 				"/resources/secret.jpg"));
 		ImageIcon loadCover = new ImageIcon(getClass().getResource(
 				"/resources/shield.jpg"));
 		ImageIcon helpIcon = new ImageIcon(getClass().getResource(
 				"/resources/help.jpg"));
 
 		JMenu file = new JMenu("Archivo");
 		file.setMnemonic(KeyEvent.VK_A);
 
 		JMenu help = new JMenu("Ayuda");
 		help.setMnemonic(KeyEvent.VK_H);
 		JMenuItem aboutItem = new JMenuItem("Sobre nosotros...", helpIcon);
 		aboutItem.setMnemonic(KeyEvent.VK_H);
 		aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
 				ActionEvent.CTRL_MASK));
 		aboutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(null,
 						"Trabajo de Criptografa 5 curso 2012/2013\n"
 								+ "Autores:\n" + "Javier Herrera Copano\n"
 								+ "Carlos Muoz Rodrguez\n"
 								+ "Luis Manuel Sayago Lpez\n");
 			}
 		});
 
 		JMenuItem loadItem = new JMenuItem("Cargar imagen secreta", loadSecret);
 		loadItem.setMnemonic(KeyEvent.VK_S);
 		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 				ActionEvent.CTRL_MASK));
 		loadItem.setToolTipText("Cargue la imagen secreta");
 		loadItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser();
 				int returnVal = fc.showOpenDialog(null);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					String tmpSecret = file.getName();
 					if (checkFormat(tmpSecret)) {
 						pathSecret = file.getParent().replace("\\", "/") + "/";
 						secret = file.getName();
 						System.out.println(pathSecret);
 						System.out.println(secret);
 						JOptionPane.showMessageDialog(null,
 								"Imagen secreta cargada correctamente");
 
 					}
 
 				}
 			}
 		});
 
 		JMenuItem loadCoverItem = new JMenuItem("Cargar imagen de fondo",
 				loadCover);
 		loadCoverItem.setMnemonic(KeyEvent.VK_F);
 		loadCoverItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
 				ActionEvent.CTRL_MASK));
 		loadCoverItem.setToolTipText("Cargue la imagen para cubrir la secreta");
 		loadCoverItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fc = new JFileChooser();
 				int returnVal = fc.showOpenDialog(null);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					String tmpCover = file.getName();
 					if (checkFormat(tmpCover)) {
 						pathCover = file.getParent().replace("\\", "/") + "/";
 						cover = file.getName();
 						System.out.println(pathCover);
 						System.out.println(cover);
 						JOptionPane.showMessageDialog(null,
 								"Imagen de fondo cargada correctamente.");
 					}
 
 				}
 			}
 		});
 
 		JMenuItem exitItem = new JMenuItem("Salir", exitIcon);
 		exitItem.setMnemonic(KeyEvent.VK_E);
 		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
 				ActionEvent.CTRL_MASK));
 		exitItem.setToolTipText("Salga de la aplicacin");
 		exitItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				messageExit();
 			}
 		});
 
 		file.add(loadItem);
 		file.add(loadCoverItem);
 		file.add(exitItem);
 		help.add(aboutItem);
 		menuBar.add(file);
 		menuBar.add(help);
 		setJMenuBar(menuBar);
 	}
 
 	public void algorithmNoiseLike(JTextField input) {
 		Integer n = Integer.valueOf(input.getText());
 		if (n > 0) {
 			BPVSS bpvss = new BPVSS(pathSecret, n);
 			bpvss.noiselikeShares(secret);
 			shares(bpvss, n);
 
 		} else {
 			throw new IllegalArgumentException();
 		}
 
 	}
 
 	public void algorithmMeaningfulShares(JTextField input) {
 		Integer n = Integer.valueOf(input.getText());
 		if (n > 0) {
 			BPVSS bpvss = new BPVSS(pathSecret, n);
 			bpvss.meaningfulShares(secret, pathCover + cover);
 			shares(bpvss, n);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public void shares(BPVSS bpvss, Integer n) {
 		for (int i = 0; i < n - 1; i++) {
 			if (i == 0) {
 				bpvss.joinImages("share0.png", "share1.png", "join1.png");
 			} else {
 				if (i == n - 2) {
 					bpvss.joinImages("join" + i + ".png", "share" + (i + 1)
 							+ ".png", "res.png");
 				} else {
 					bpvss.joinImages("join" + i + ".png", "share" + (i + 1)
 							+ ".png", "join" + (i + 1) + ".png");
 				}
 			}
 		}
 	}
 
 	public static Boolean checkFormat(String picture) {
 		Boolean flag = picture.contains(".jpg") || picture.contains(".png")
 				|| picture.contains(".JPG") || picture.contains(".PNG")
 				|| picture.contains(".jpeg") || picture.contains(".JPEG");
 		if (!flag) {
 			JOptionPane.showMessageDialog(null,
 					"Por favor introduzca una imagen jpg o png.");
 		}
 		return flag;
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				GuiImpl gui = new GuiImpl();
 				gui.setVisible(true);
 			}
 		});
 
 	}
 
 }
