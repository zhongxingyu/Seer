 package org.mess110.jrattrack;
 
 import java.awt.EventQueue;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 
 import org.mess110.jrattrack.models.Movie;
 import org.mess110.jrattrack.models.MovieMeta;
 import org.mess110.jrattrack.models.RatTrack;
 import org.mess110.jrattrack.models.ResultInterpretor;
 import org.mess110.jrattrack.util.ClickListener;
 import org.mess110.jrattrack.util.Util;
 import org.mess110.jrattrack.util.exceptions.InvalidStartOrEnd;
 import org.mess110.jrattrack.util.exceptions.JRatException;
 import javax.swing.JCheckBox;
 
 public class Main {
 
 	private JFrame frame;
 	private JTextField textFps;
 	private JTextField textRatSize;
 	public JTextField textCenterX;
 	public JTextField textCenterY;
 	private JTextField textRadius;
 
 	private ImageIcon baseImage;
 
 	private JFileChooser fileChooser, saveResults, interpretResult;
 	private JButton btnExtract;
 	private JLabel lblImage;
 	private JButton btnAnalyze;
 	private ClickListener clickListener;
 	private JTextField textStart;
 	private JTextField textEnd;
 	private JButton btnSave;
 	private JCheckBox chckbxSaveFrames;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Main window = new Main();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Main() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 600, 530);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 
 		fileChooser = new JFileChooser(".");
 		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		saveResults = new JFileChooser(".");
 
 		JLabel lblFps = new JLabel("FPS");
 		lblFps.setBounds(10, 42, 65, 14);
 		frame.getContentPane().add(lblFps);
 
 		textFps = new JTextField();
 		textFps.setEnabled(false);
 		textFps.setText("1");
 		textFps.setBounds(94, 39, 65, 20);
 		frame.getContentPane().add(textFps);
 		textFps.setColumns(10);
 
 		JButton btnSelectMovie = new JButton("Select Movie");
 		btnSelectMovie.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent arg0) {
 				int returnVal = fileChooser.showOpenDialog(frame);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File file = fileChooser.getSelectedFile();
 					frame.setTitle(file.getPath());
 					// user loaded a avi file
					if (file.getPath().endsWith("avi")) {
 						setCoordinatesEnabled(false);
 						setMetaFpsEnabled(true);
 					} else if (file.isDirectory() && isRatTrackDirectory(file)) {
 						MovieMeta meta = new MovieMeta("", file.getName());
 						if (meta.framesAreExtracted()) {
 							// user loaded a valid RatTrack folder
 							meta.readMetaFromDisk();
 							updateGuiFromMeta(meta);
 							baseImage = meta.getRandomFrame();
 							drawCircle();
 							setCoordinatesEnabled(true);
 							setMetaFpsEnabled(false);
 						} else {
 							setCoordinatesEnabled(false);
 							setMetaFpsEnabled(false);
 						}
 					} else {
 						setCoordinatesEnabled(false);
 						setMetaFpsEnabled(false);
 					}
 				} else {
 					setCoordinatesEnabled(false);
 					setMetaFpsEnabled(false);
 				}
 
 			}
 
 			private boolean isRatTrackDirectory(File file) {
 				boolean frames = false;
 				boolean processed = false;
 				File[] children = file.listFiles();
 				for (int i = 0; i < children.length; i++) {
 					if (children[i].getPath().endsWith("frames")) {
 						frames = true;
 					}
 					if (children[i].getPath().endsWith("processed")) {
 						processed = true;
 					}
 				}
 				return frames && processed;
 			}
 		});
 		btnSelectMovie.setBounds(10, 8, 149, 23);
 		frame.getContentPane().add(btnSelectMovie);
 
 		btnExtract = new JButton("Extract Frames");
 		btnExtract.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				MovieMeta meta = new MovieMeta(frame.getTitle());
 				meta.setAnalyzeFps(Integer.valueOf(textFps.getText()));
 				new Movie(meta).extractFrames(); // meta is also written before
 													// extractingFrames
 			}
 		});
 		btnExtract.setEnabled(false);
 		btnExtract.setBounds(10, 67, 149, 23);
 		frame.getContentPane().add(btnExtract);
 
 		lblImage = new JLabel("");
 		lblImage.setBounds(169, 11, 46, 14);
 		clickListener = new ClickListener(this);
 		lblImage.addMouseListener(clickListener);
 		frame.getContentPane().add(lblImage);
 
 		btnAnalyze = new JButton("Analyze");
 		btnAnalyze.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				btnSave.setEnabled(false);
 				MovieMeta meta = new MovieMeta("", new File(frame.getTitle())
 						.getName());
 
 				int x, y, r, rS;
 				try {
 					x = Integer.valueOf(textCenterX.getText());
 					y = Integer.valueOf(textCenterY.getText());
 					r = Integer.valueOf(textRadius.getText());
 					rS = Integer.valueOf(textRatSize.getText());
 				} catch (NumberFormatException e) {
 					return;
 				}
 				// reading from disk first to get the FPS
 				meta.readMetaFromDisk();
 				meta.setCircle(x, y, r);
 				meta.setRatSize(rS);
 				meta.writeMeta();
 
 				boolean saveAnalyzedFrames = chckbxSaveFrames.isSelected();
 
 				try {
 					RatTrack rt = new RatTrack(meta);
 					meta.clearResultSet();
 
 					long start, end;
 					try {
 						start = Long.valueOf(textStart.getText()) * 1000;
 						end = Long.valueOf(textEnd.getText()) * 1000;
 					} catch (NumberFormatException e) {
 						throw new InvalidStartOrEnd();
 					}
 
 					File[] array = Util.contentsOf(meta.getFramesPath());
 					for (File f : array) {
 						long l = Util.getTimestampName(f);
 						if (start <= l && l <= end) {
 							rt.analyze(f, saveAnalyzedFrames);
 						}
 					}
 					meta.writeLog();
 					btnSave.setEnabled(true);
 				} catch (JRatException e) {
 					btnSave.setEnabled(false);
 					Util.toast(frame, e.getLocalizedMessage());
 					e.printStackTrace();
 				} catch (IOException e) {
 					btnSave.setEnabled(false);
 					e.printStackTrace();
 				}
 			}
 		});
 		btnAnalyze.setEnabled(false);
 		btnAnalyze.setBounds(10, 351, 149, 23);
 		frame.getContentPane().add(btnAnalyze);
 
 		JLabel lblRatSize = new JLabel("Rat Size");
 		lblRatSize.setBounds(10, 101, 65, 14);
 		frame.getContentPane().add(lblRatSize);
 
 		textRatSize = new JTextField();
 		textRatSize.setEnabled(false);
 		textRatSize.setText("30");
 		textRatSize.setBounds(10, 126, 65, 20);
 		frame.getContentPane().add(textRatSize);
 		textRatSize.setColumns(10);
 
 		JLabel lblCenterX = new JLabel("Center X");
 		lblCenterX.setBounds(10, 157, 65, 14);
 		frame.getContentPane().add(lblCenterX);
 
 		textCenterX = new JTextField();
 		textCenterX.setEnabled(false);
 		textCenterX.setText("110");
 		textCenterX.addCaretListener(new CaretListener() {
 
 			@Override
 			public void caretUpdate(CaretEvent e) {
 				drawCircle();
 			}
 		});
 		textCenterX.setBounds(10, 182, 65, 20);
 		frame.getContentPane().add(textCenterX);
 		textCenterX.setColumns(10);
 
 		JLabel lblCenterY = new JLabel("Center Y");
 		lblCenterY.setBounds(94, 157, 65, 14);
 		frame.getContentPane().add(lblCenterY);
 
 		textCenterY = new JTextField();
 		textCenterY.setEnabled(false);
 		textCenterY.setText("110");
 		textCenterY.setBounds(94, 182, 65, 20);
 		textCenterY.addCaretListener(new CaretListener() {
 
 			@Override
 			public void caretUpdate(CaretEvent e) {
 				drawCircle();
 			}
 		});
 		frame.getContentPane().add(textCenterY);
 		textCenterY.setColumns(10);
 
 		JLabel lblRadius = new JLabel("Circle Radius");
 		lblRadius.setBounds(10, 213, 149, 14);
 		frame.getContentPane().add(lblRadius);
 
 		textRadius = new JTextField();
 		textRadius.setEnabled(false);
 		textRadius.setText("120");
 		textRadius.setBounds(10, 238, 65, 20);
 		textRadius.addCaretListener(new CaretListener() {
 
 			@Override
 			public void caretUpdate(CaretEvent e) {
 				drawCircle();
 			}
 		});
 		frame.getContentPane().add(textRadius);
 		textRadius.setColumns(10);
 
 		JLabel lblStart = new JLabel("Start");
 		lblStart.setBounds(10, 269, 46, 14);
 		frame.getContentPane().add(lblStart);
 
 		JLabel lblEnd = new JLabel("End");
 		lblEnd.setBounds(94, 269, 46, 14);
 		frame.getContentPane().add(lblEnd);
 
 		textStart = new JTextField();
 		textStart.setEnabled(false);
 		textStart.setText("0");
 		textStart.setBounds(10, 294, 65, 20);
 		frame.getContentPane().add(textStart);
 		textStart.setColumns(10);
 
 		textEnd = new JTextField();
 		textEnd.setEnabled(false);
 		textEnd.setText("0");
 		textEnd.setBounds(94, 294, 65, 20);
 		frame.getContentPane().add(textEnd);
 		textEnd.setColumns(10);
 
 		JButton btnAbout = new JButton("About");
 		btnAbout.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Util.toast(frame, "github.com/mess110/JRatTrack");
 			}
 		});
 		btnAbout.setBounds(10, 453, 149, 23);
 		frame.getContentPane().add(btnAbout);
 
 		btnSave = new JButton("Save Results");
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int returnVal = saveResults.showSaveDialog(frame);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					MovieMeta meta = new MovieMeta("", new File(frame
 							.getTitle()).getName());
 					meta.readMetaFromDisk();
 					File file = saveResults.getSelectedFile();
 					meta.readLog();
 					meta.writeLog(file.getPath());
 				}
 			}
 		});
 		btnSave.setEnabled(false);
 		btnSave.setBounds(10, 385, 149, 23);
 		frame.getContentPane().add(btnSave);
 
 		interpretResult = new JFileChooser(".");
 		JButton btnInterpret = new JButton("Interpret Results");
 		btnInterpret.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int returnVal = interpretResult.showOpenDialog(frame);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					MovieMeta meta = new MovieMeta("", new File(frame
 							.getTitle()).getName());
 					meta.readLog(interpretResult.getSelectedFile());
 					ResultInterpretor interpret = new ResultInterpretor(meta);
 					Util.toast(frame, interpret.toString());
 				}
 			}
 		});
 		btnInterpret.setBounds(10, 419, 149, 23);
 		frame.getContentPane().add(btnInterpret);
 
 		chckbxSaveFrames = new JCheckBox("Save analyzed frames");
 		chckbxSaveFrames.setEnabled(false);
 		chckbxSaveFrames.setBounds(10, 321, 149, 23);
 		frame.getContentPane().add(chckbxSaveFrames);
 	}
 
 	private void setCoordinatesEnabled(boolean b) {
 		textCenterX.setEnabled(b);
 		textCenterY.setEnabled(b);
 		textRadius.setEnabled(b);
 		textRatSize.setEnabled(b);
 		btnAnalyze.setEnabled(b);
 		chckbxSaveFrames.setEnabled(true);
 		textStart.setEnabled(b);
 		textEnd.setEnabled(b);
 	}
 
 	private void setMetaFpsEnabled(boolean b) {
 		btnExtract.setEnabled(b);
 		textFps.setEnabled(b);
 	}
 
 	public void drawCircle() {
 		Image img;
 		try {
 			img = baseImage.getImage();
 		} catch (NullPointerException e) {
 			return;
 		}
 		if (img == null) {
 			return;
 		}
 
 		int x;
 		int y;
 		int r;
 
 		try {
 			x = Integer.valueOf(textCenterX.getText());
 			y = Integer.valueOf(textCenterY.getText());
 			r = Integer.valueOf(textRadius.getText());
 		} catch (NumberFormatException e) {
 			return;
 		}
 
 		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null),
 				img.getHeight(null), BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = bufferedImage.createGraphics();
 		g.drawImage(img, null, null);
 		g.drawOval(x - r, y - r, 2 * r, 2 * r);
 		g.dispose();
 
 		lblImage.setIcon(new ImageIcon(bufferedImage));
 		lblImage.setBounds(169, 11, baseImage.getIconWidth(),
 				baseImage.getIconHeight());
 	}
 
 	private void updateGuiFromMeta(MovieMeta meta) {
 		textFps.setText(String.valueOf(meta.getAnalyzeFps()));
 		textRatSize.setText(String.valueOf(meta.getRatSize()));
 		textCenterX.setText(String.valueOf(meta.getCircleX()));
 		textCenterY.setText(String.valueOf(meta.getCircleY()));
 		textRadius.setText(String.valueOf(meta.getCircleR()));
 	}
 }
