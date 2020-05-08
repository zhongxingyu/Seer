 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import model.Event;
 import model.ShiftVector;
 import model.algorithms.CopyMoveFactory;
 import model.algorithms.CopyMoveRMFactory;
 import model.algorithms.CopyMoveRobustMatch;
 import model.algorithms.ICopyMoveDetection;
 import model.algorithms.SimpleCMFactory;
 
 public class View extends JFrame implements Observer {
 
 	private static final long serialVersionUID = 1L;
 	private ViewPanel panel;
 	private JMenuBar menubar;
 	private JFileChooser chooser;
 	private JCheckBoxMenuItem multithreading, debugSwitch;
 	private JMenuItem exit, open;
 	private JMenu settings, algorithm;
 	private JRadioButtonMenuItem simple, matrixmult;
 	private BufferedImage image;
 	private CopyMoveFactory factory;
 
 	private enum ViewState {
 		IDLE, IMG_LOADED, PROCESSING, ABORTING, PROCESSED;
 	}
 
 	private ViewState state = ViewState.IDLE;
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				new View(new CopyMoveRMFactory());
 			}
 		});
 	}
 
 	public View(CopyMoveFactory factory) {
 		super();
 		this.factory = factory;
 		setVisible(true);
 		setTitle("Copy-Move Robust Match Algorithm");
 		setSize(880, 600);
 		setMinimumSize(new Dimension(880, 600));
 		setResizable(true);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		init();
 	}
 
 	private void init() {
 		panel = new ViewPanel();
 		getContentPane().add(panel);
 		initMBar();
 		setJMenuBar(menubar);
 	}
 
 	private void initMBar() {
 		ImageIcon exitI = new ImageIcon("icons/exit.png");
 		ImageIcon multithreadingI = new ImageIcon("icons/settings.png");
 		ImageIcon debugI = new ImageIcon("icons/tool.png");
 		ImageIcon openI = new ImageIcon("icons/open.png");
 		ImageIcon algoI = new ImageIcon("icons/algo.png");
 		menubar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		file.setMnemonic(KeyEvent.VK_F);
 		settings = new JMenu("Settings");
 		settings.setMnemonic(KeyEvent.VK_S);
 		algorithm = new JMenu("Algorithm");
 		algorithm.setMnemonic(KeyEvent.VK_A);
 		chooser = new JFileChooser(new File("."));
 		chooser.setFileFilter(new FileNameExtensionFilter(
 				"JPEG, GIF, BMP, PNG", "jpg", "jpeg", "gif", "bmp", "png"));
 		exit = new JMenuItem("Exit", exitI);
 		exit.setMnemonic(KeyEvent.VK_C);
 		exit.setToolTipText("Exit application");
 		exit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 
 		open = new JMenuItem("Open Image", openI);
 		open.setMnemonic(KeyEvent.VK_O);
 		open.setToolTipText("Open an image file");
 		open.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				int returnVal = chooser.showOpenDialog(View.this);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					panel.progress.setValue(0);
 					loadImage(chooser.getSelectedFile());
 				}
 			}
 		});
 
 		multithreading = new JCheckBoxMenuItem("Multithreading",
 				multithreadingI);
 		multithreading.setState(true);
 		multithreading.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String action = multithreading.getState() ? "Enabled"
 						: "Disabled";
 				log(action + " multithreading");
 			}
 		});
 
 		debugSwitch = new JCheckBoxMenuItem("Show Debugwindow", debugI);
 		debugSwitch.setState(true);
 		debugSwitch.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				panel.scrollP.setVisible(debugSwitch.getState());
 				setSize(getWidth(), getHeight() + 1);
 				setSize(getWidth(), getHeight() - 1);
 			}
 		});
 
 		ButtonGroup bg = new ButtonGroup();
 		simple = new JRadioButtonMenuItem("Simple", algoI, false);
 		simple.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (simple.isSelected()) {
 					log("Switched to simple algorithm");
 					factory = new SimpleCMFactory();
 				}
 			}
 		});
 
 		matrixmult = new JRadioButtonMenuItem("Matrix multiplication", algoI,
 				true);
 		matrixmult.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (matrixmult.isSelected()) {
 					log("Switched to matrix multiplication algorithm");
 					factory = new CopyMoveRMFactory();
 				}
 
 			}
 		});
 
 		bg.add(matrixmult);
 		bg.add(simple);
 		file.add(open);
 		file.addSeparator();
 		file.add(exit);
 		settings.add(multithreading);
 		settings.add(debugSwitch);
 		algorithm.add(matrixmult);
 		algorithm.add(simple);
 		menubar.add(file);
 		menubar.add(settings);
 		menubar.add(algorithm);
 		log("Application started");
 	}
 
 	private void log(String m) {
 		panel.log.append(": " + m + "\n");
 		panel.log.setCaretPosition(panel.log.getDocument().getLength());
 	}
 
 	private void loadImage(File file) {
 		try {
 			image = ImageIO.read(file);
 			log("Successfully loaded " + file.getName());
 			panel.imagePanel.setImages(new BufferedImage[] { image });
 			panel.start.setEnabled(true);
 			panel.quality.setEnabled(true);
 			panel.threshold.setEnabled(true);
			panel.minLength.setEnabled(true);
 			state = ViewState.IMG_LOADED;
 		} catch (IOException e) {
 			log("Error: Could not load image file\n");
 		}
 	}
 
 	@Override
 	public void update(Observable arg0, Object o) {
 		if (o instanceof Event) {
 			Event event = (Event) o;
 			switch (event.getType()) {
 			case STATUS:
 				if (state == ViewState.PROCESSING) {
 					log(event.getResult().getDescription());
 				}
 				break;
 			case ERROR:
 				if (state == ViewState.PROCESSING) {
 					View.this.setCursor(Cursor
 							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 					state = ViewState.IMG_LOADED;
 					log("An error occured during execution");
 					settings.setEnabled(true);
 					panel.start.setEnabled(true);
 					panel.quality.setEnabled(true);
 					panel.threshold.setEnabled(true);
 					panel.minLength.setEnabled(true);
 					panel.abort.setEnabled(false);
 					settings.setEnabled(true);
 					algorithm.setEnabled(true);
 					open.setEnabled(true);
 
 				}
 				break;
 			case PROGRESS:
 				if (state == ViewState.PROCESSING) {
 					panel.setStatus(event.getResult().getProgress());
 				}
 				break;
 			case ABORT:
 				if (state == ViewState.ABORTING) {
 					View.this.setCursor(Cursor
 							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 					state = ViewState.IMG_LOADED;
 					log("Abort was successful");
 					settings.setEnabled(true);
 					panel.start.setEnabled(true);
 					panel.quality.setEnabled(true);
 					panel.threshold.setEnabled(true);
 					panel.minLength.setEnabled(true);
 					panel.abort.setEnabled(false);
 					settings.setEnabled(true);
 					algorithm.setEnabled(true);
 					open.setEnabled(true);
 					panel.progress.setValue(0);
 				}
 				break;
 			case COPY_MOVE_DETECTION_FINISHED:
 				if (state == ViewState.PROCESSING) {
 					View.this.setCursor(Cursor
 							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 					state = ViewState.PROCESSED;
 					log("Duration: " + event.getResult().getTime() + "ms");
 					settings.setEnabled(true);
 					panel.start.setEnabled(true);
 					panel.quality.setEnabled(true);
 					panel.threshold.setEnabled(true);
 					panel.minLength.setEnabled(true);
 					panel.abort.setEnabled(false);
 					settings.setEnabled(true);
 					algorithm.setEnabled(true);
 					open.setEnabled(true);
 					log("Found a total of "
 							+ event.getResult().getVectors().size()
 							+ " shiftvectors");
 					displayResult(event.getResult().getVectors());
 				}
 				break;
 			default:
 				log("Received unknown event type");
 				break;
 			}
 		} else {
 			log("Received a faulty notification from model");
 		}
 	}
 
 	private void displayResult(List<ShiftVector> vectors) {
 		BufferedImage i = new BufferedImage(image.getWidth(),
 				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		BufferedImage i_alt = new BufferedImage(image.getWidth(),
 				image.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		Graphics g = i.getGraphics(), g_alt = i_alt.getGraphics();
 		Color red = new Color(1, 0, 0, 0.25f);
 		Color green = new Color(0, 1, 0, 0.25f);
 
 		g.drawImage(image, 0, 0, null);
 		g_alt.drawImage(image, 0, 0, null);
 
 		for (ShiftVector v : vectors) {
 			g.setColor(red);
 			g.fillRect(v.getSx(), v.getSy(), v.getBs(), v.getBs());
 			g.setColor(green);
 			g.fillRect(v.getSx() + v.getDx(), v.getSy() + v.getDy(), v.getBs(),
 					v.getBs());
 			g_alt.setColor(Color.WHITE);
 			g_alt.drawLine(v.getSx(), v.getSy(), v.getSx() + v.getDx(),
 					v.getSy() + v.getDy());
 		}
 
 		g.dispose();
 		panel.imagePanel.setImages(new BufferedImage[] { i, i_alt, image });
 	}
 
 	public class ViewPanel extends JPanel {
 
 		private JPanel buttonPanel;
 		private ImagePanel imagePanel;
 		private JButton start, abort;
 		private JLabel qualityL, thresholdL, minLengthL;
 		private JSlider quality, threshold, minLength;
 		private JScrollPane scrollP;
 		private JTextArea log;
 		private JProgressBar progress;
 		private static final long serialVersionUID = 1L;
 		private ICopyMoveDetection algo;
 
 		public ViewPanel() {
 			super();
 			init();
 		}
 
 		private void init() {
 			setBackground(Color.GRAY);
 			setLayout(new BorderLayout());
 			initButtonPanel();
 			log = new JTextArea();
 			log.setEditable(false);
 			log.setRows(5);
 			log.setMargin(new Insets(5, 10, 5, 10));
 			log.setFont(new Font("Verdana", Font.BOLD, 12));
 			scrollP = new JScrollPane(log);
 			scrollP.setBorder(BorderFactory.createEmptyBorder());
 			scrollP.setAutoscrolls(true);
 			imagePanel = new ImagePanel();
 			add(scrollP, BorderLayout.NORTH);
 			add(imagePanel, BorderLayout.CENTER);
 			add(buttonPanel, BorderLayout.SOUTH);
 		}
 
 		private void initButtonPanel() {
 			ImageIcon abortI = new ImageIcon("icons/abort.png");
 			ImageIcon startI = new ImageIcon("icons/start.png");
 			GridLayout gLayout = new GridLayout(1, 7);
 			gLayout.setVgap(20);
 			gLayout.setHgap(0);
 			buttonPanel = new JPanel(gLayout);
 			progress = new JProgressBar(0, 100);
 			progress.setStringPainted(true);
 			start = new JButton("Start", startI);
 			start.setEnabled(false);
 			start.setToolTipText("Start the Copy-Move detection algorithm");
 			start.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					state = ViewState.PROCESSING;
 					View.this.setCursor(Cursor
 							.getPredefinedCursor(Cursor.WAIT_CURSOR));
 					open.setEnabled(false);
 					settings.setEnabled(false);
 					algorithm.setEnabled(false);
 					abort.setEnabled(true);
 					start.setEnabled(false);
 					quality.setEnabled(false);
 					threshold.setEnabled(false);
 					minLength.setEnabled(false);
 					algo = factory.getInstance();
 					algo.addObserver(View.this);
 					Thread t = new Thread(new Runnable() {
 						@Override
 						public void run() {
 
 							int cores = multithreading.getState() ? Runtime
 									.getRuntime().availableProcessors() : 1;
 							log("Invoked algorithm with a total number of "
 									+ cores + " threads");
 							log("Settings: Quality = " + getQuality()
 									+ " , Threshold = " + threshold.getValue()
 									+ " , Minimum vector length = "
 									+ minLength.getValue());
 							algo.detect(image, getQuality(),
 									threshold.getValue(), minLength.getValue(),
 									cores);
 							log.setEditable(false);
 						}
 					});
 					t.start();
 				}
 			});
 
 			abort = new JButton("Abort", abortI);
 			abort.setEnabled(false);
 			abort.setToolTipText("Abort excecution of the algorithm");
 			abort.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					state = ViewState.ABORTING;
 					algo.abort();
 					abort.setEnabled(false);
 					log("Initiated abort");
 				}
 			});
 
 			quality = new JSlider(1, 100);
 			quality.setEnabled(false);
 			quality.setToolTipText("Quality setting used to compute DCT coefficients");
 			quality.addChangeListener(new ChangeListener() {
 
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					qualityL.setText("Quality [" + getQuality() + "]:");
 				}
 			});
 			threshold = new JSlider(1, 100);
 			threshold.setEnabled(false);
 			threshold.setValue(10);
 			threshold.setToolTipText("Threshold setting used by the algorithm");
 			threshold.addChangeListener(new ChangeListener() {
 
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					thresholdL.setText("Threshold [" + threshold.getValue()
 							+ "]:");
 				}
 			});
 
 			minLength = new JSlider(0, 100);
 			minLength.setEnabled(false);
 			minLength.setValue(50);
 			minLength.setToolTipText("Minimum length of a shiftvector");
 			minLength.addChangeListener(new ChangeListener() {
 
 				@Override
 				public void stateChanged(ChangeEvent e) {
 					minLengthL.setText("Min. Length [" + minLength.getValue()
 							+ "]:");
 				}
 			});
 
 			minLengthL = new JLabel("Min. Length [" + minLength.getValue()
 					+ "]:");
 			minLengthL.setHorizontalAlignment(JLabel.CENTER);
 			qualityL = new JLabel("Quality [" + getQuality() + "]:");
 			qualityL.setHorizontalAlignment(JLabel.CENTER);
 			thresholdL = new JLabel("Threshold [" + threshold.getValue() + "]:");
 			thresholdL.setHorizontalAlignment(JLabel.CENTER);
 			buttonPanel.add(start);
 			buttonPanel.add(abort);
 			buttonPanel.add(qualityL);
 			buttonPanel.add(quality);
 			buttonPanel.add(thresholdL);
 			buttonPanel.add(threshold);
 			buttonPanel.add(minLengthL);
 			buttonPanel.add(minLength);
 			buttonPanel.add(progress);
 		}
 
 		private float getQuality() {
 			return (float) quality.getValue() / 100.0f;
 		}
 
 		private void setStatus(float val) {
 			progress.setValue((int) Math.ceil(val * 100));
 		}
 
 		public class ImagePanel extends JPanel {
 			private static final long serialVersionUID = 1L;
 			private BufferedImage images[] = null, image = null;
 			private int idx = 0;
 
 			public ImagePanel() {
 				super();
 				setBackground(Color.BLACK);
 				setVisible(true);
 
 				addMouseListener(new MouseAdapter() {
 					@Override
 					public void mouseClicked(MouseEvent e) {
 						if (View.this.state == ViewState.PROCESSED
 								&& e.getButton() == MouseEvent.BUTTON1) {
 							idx++;
 							image = images[idx % images.length];
 							repaint();
 						}
 					}
 				});
 			}
 
 			public void paint(Graphics g) {
 				g.setColor(Color.DARK_GRAY);
 				g.fillRect(0, 0, getWidth(), getHeight());
 
 				if (image != null) {
 					double panelRatio = (double) getWidth()
 							/ (double) getHeight();
 					double aspectRatio = (double) image.getWidth()
 							/ (double) image.getHeight();
 					int nWidth = getWidth(), nHeight = getHeight();
 
 					if (panelRatio < aspectRatio) {
 						nHeight = (int) (nWidth / aspectRatio);
 					} else {
 						nWidth = (int) (nHeight * aspectRatio);
 					}
 					g.drawImage(image, (getWidth() - nWidth) / 2,
 							(getHeight() - nHeight) / 2, nWidth, nHeight, null);
 				}
 			}
 
 			public void setImages(BufferedImage[] images) {
 				idx = 0;
 				this.images = images;
 				this.image = images[idx % images.length];
 				repaint();
 			}
 
 			public BufferedImage getImage() {
 				return image;
 			}
 
 		}
 
 	}
 }
