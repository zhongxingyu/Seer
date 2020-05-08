 package plugins.tprovoost.Microscopy.MicroscopeRemote;
 
 import icy.gui.component.IcyLogo;
 import icy.gui.frame.IcyFrame;
 import icy.gui.util.GuiUtil;
 import icy.gui.util.LookAndFeelUtil;
 import icy.image.ImageUtil;
 import icy.network.NetworkUtil;
 import icy.plugin.abstract_.Plugin;
 import icy.system.thread.ThreadUtil;
 import icy.util.StringUtil;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.MultipleGradientPaint.CycleMethod;
 import java.awt.Paint;
 import java.awt.RadialGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Path2D;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.prefs.Preferences;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;
 import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageListener;
 import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;
 import plugins.tprovoost.Microscopy.MicroscopeRemote.gui.InverterCheckBox;
 import plugins.tprovoost.Microscopy.MicroscopeRemote.gui.MemoryButton;
 import plugins.tprovoost.Microscopy.MicroscopeRemote.gui.RemoteSlider;
 
 /**
  * This class is the core of the Remote Plugin. Three different threads are
  * running while using this class:
  * <ul>
  * <li>Refreshing the coordinates.</li>
  * <li>Moving the XY Stage</li>
  * <li>Moving the Z Stage.</li>
  * </ul>
  * 
  * @author Thomas Provoost
  */
 public class RemoteFrame extends IcyFrame {
 
 	// -------
 	// GUI
 	// -------
 	Plugin plugin;
 	private IcyLogo _logo_remote;
 	private RemoteSlider _sliderSpeed;
 	private JLabel _lblX;
 	private JLabel _lblY;
 	private JLabel _lblZ;
 	private InverterCheckBox _cbInvertX;
 	private InverterCheckBox _cbInvertY;
 	private InverterCheckBox _cbInvertZ;
 
 	// --------
 	// IMAGES
 	// --------
 	public Color transparentColor = new Color(255, 255, 255, 0);
 	BufferedImage imgRemoteBg = null;
 	BufferedImage imgZBg = null;
 	BufferedImage imgZBar = null;
 	BufferedImage imgXYBg = null;
 	BufferedImage imgSliderKnob = null;
 	BufferedImage imgMemBtnOn = null;
 	BufferedImage imgMemBtnOff = null;
 	BufferedImage imgInvertSwitchOn = null;
 	BufferedImage imgInvertSwitchOff = null;
 	BufferedImage imgInvertLightOn = null;
 	BufferedImage imgInvertLightOff = null;
 
 	// CONSTANTS
 	private static String currentPath = "plugins/tprovoost/Microscopy/MicroscopeRemote/images/";
 
 	// -----------
 	// PREFERENCES
 	// -----------
 	private Preferences _prefs;
 	private static final String REMOTE = "prefs_remote";
 	private static final String SPEED = "speed";
 
 	public RemoteFrame(MicroscopeRemotePlugin plugin) {
 		super("Remote", false, true, true, true);
 		this.plugin = plugin;
 
 		// LOAD ALL IMAGES
 		imgRemoteBg = (BufferedImage) plugin.getImageResource(currentPath + "RemoteFull_2.png");
 		imgXYBg = (BufferedImage) plugin.getImageResource(currentPath + "remote_backgroundXY.png");
 		imgZBg = (BufferedImage) plugin.getImageResource(currentPath + "remote_backgroundZ.png");
 		imgZBar = (BufferedImage) plugin.getImageResource(currentPath + "singleBarZ.png");
 		imgMemBtnOn = (BufferedImage) plugin.getImageResource(currentPath + "memoryOn.png");
 		imgMemBtnOff = (BufferedImage) plugin.getImageResource(currentPath + "memoryOff.png");
 		imgInvertSwitchOn = (BufferedImage) plugin.getImageResource(currentPath + "btn_switchOn.png");
 		imgInvertSwitchOff = (BufferedImage) plugin.getImageResource(currentPath + "btn_switchOff.png");
 		imgInvertLightOn = (BufferedImage) plugin.getImageResource(currentPath + "btnRound.png");
 		imgInvertLightOff = (BufferedImage) plugin.getImageResource(currentPath + "btnRound_off.png");
 		imgSliderKnob = (BufferedImage) plugin.getImageResource(currentPath + "knob.png");
 
 		JPanel panelAll = new JPanel() {
 
 			/** */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void paintComponent(Graphics g) {
 				checkInverts();
 				if (imgRemoteBg == null) {
 					super.paintComponent(g);
 				} else {
 					Graphics2D g2 = (Graphics2D) g.create();
 					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 					g2.setColor(transparentColor);
 					g2.fillRect(0, 0, getWidth(), getHeight());
 					g2.drawImage(ImageUtil.scaleImage(imgRemoteBg,getWidth(), getHeight()), null, 0,0);
 					g2.dispose();
 				}
 			}
 		};
 		panelAll.setLayout(new BoxLayout(panelAll, BoxLayout.Y_AXIS));
 		panelAll.setBackground(Color.BLACK);
 
 		_logo_remote = new IcyLogo("Remote");
 		_logo_remote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
 
 		// -------------
 		// MOUSE MOVER
 		// ------------
 		JPanel panelMover = GuiUtil.generatePanel();
 		panelMover.setLayout(new BoxLayout(panelMover, BoxLayout.X_AXIS));
 		panelMover.add(new PanelMoverXY());
 		panelMover.add(Box.createRigidArea(new Dimension(20, 10)));
 		panelMover.add(new PanelMoverZ());
 		panelMover.setOpaque(false);
 		panelMover.setBackground(transparentColor);
 		panelAll.add(panelMover);
 
 		// ---------
 		// SPEED
 		// ---------
 		JPanel panel_speed = GuiUtil.generatePanel();
 		panel_speed.setLayout(new BoxLayout(panel_speed, BoxLayout.X_AXIS));
 		panel_speed.setOpaque(false);
 		_sliderSpeed = new RemoteSlider(1, 10, 1);
 		_sliderSpeed.setSliderKnob(imgSliderKnob);
 		final JLabel lbl_value = new JLabel("" + _sliderSpeed.getValue());
 
 		_sliderSpeed.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent changeevent) {
 				lbl_value.setText("" + _sliderSpeed.getValue());
 				_prefs.putInt(SPEED, _sliderSpeed.getValue());
 			}
 		});
 		panel_speed.add(_sliderSpeed);
 		panel_speed.add(Box.createHorizontalGlue());
 		panel_speed.add(lbl_value);
 		panel_speed.add(Box.createHorizontalGlue());
 		panelAll.add(panel_speed);
 
 		// -------------------
 		// INVERT CHEBKBOXES
 		// -------------------
 		_cbInvertX = new InverterCheckBox("Invert X-Axis");
 		_cbInvertX.setImages(imgInvertSwitchOn, imgInvertSwitchOff, imgInvertLightOn, imgInvertLightOff);
 		_cbInvertX.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				StageMover.setInvertX(_cbInvertX.isSelected());
 			}
 		});
 
 		_cbInvertY = new InverterCheckBox("Invert Y-Axis");
 		_cbInvertY.setImages(imgInvertSwitchOn, imgInvertSwitchOff, imgInvertLightOn, imgInvertLightOff);
 		_cbInvertY.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				StageMover.setInvertY(_cbInvertY.isSelected());
 			}
 		});
 
 		_cbInvertZ = new InverterCheckBox("Invert Z-Axis");
 		_cbInvertZ.setImages(imgInvertSwitchOn, imgInvertSwitchOff, imgInvertLightOn, imgInvertLightOff);
 		_cbInvertZ.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				StageMover.setInvertZ(_cbInvertZ.isSelected());
 			}
 		});
 
 		JPanel panelInvert = GuiUtil.generatePanel();
 		panelInvert.setOpaque(false);
 		panelInvert.setLayout(new GridLayout(3, 1));
 		panelInvert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
 		panelInvert.add(_cbInvertX);
 		panelInvert.add(_cbInvertY);
 		panelInvert.add(_cbInvertZ);
 
 		// MEMORY BUTTONS
 		MemoryButton btnM1 = new MemoryButton("M1");
 		btnM1.setImages(imgMemBtnOn, imgMemBtnOff);
 		MemoryButton btnM2 = new MemoryButton("M2");
 		btnM2.setImages(imgMemBtnOn, imgMemBtnOff);
 		MemoryButton btnM3 = new MemoryButton("M3");
 		btnM3.setImages(imgMemBtnOn, imgMemBtnOff);
 		MemoryButton btnM4 = new MemoryButton("M4");
 		btnM4.setImages(imgMemBtnOn, imgMemBtnOff);
 		JButton btnHelp = new JButton("?") {
 			/** */
 			private static final long serialVersionUID = 1L;
 						
 			@Override
 			public void paint(Graphics g) {
 				if (imgMemBtnOn == null || imgMemBtnOff == null) {
 					super.paint(g);
 				} else {
 					int w = getWidth();
 					int h = getHeight();
 					Graphics2D g2 = (Graphics2D) g.create();
 					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 					
 					if (isSelected())
 						g2.drawImage(imgMemBtnOn, 0, 0, w, h, null);
 					else
 						g2.drawImage(imgMemBtnOff, 0, 0, w, h, null);
 					g2.setFont(new Font("Arial",Font.BOLD,16));
 					FontMetrics fm = g2.getFontMetrics();
 					g2.setColor(Color.LIGHT_GRAY);
 					g2.drawString("?", getWidth() / 2 - fm.charWidth('?') / 2, getHeight() / 2 + fm.getHeight() / 3);
 					g2.dispose();
 				}
 			}
 		};
 		btnHelp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionevent) {
 				NetworkUtil.openURL("http://icy.bioimageanalysis.org/index.php?display=detailPlugin&pluginId=124#documentation");
 			}
 		});
 
 		JPanel panelMemoryButtons = new JPanel(new GridLayout(1, 4));
 		panelMemoryButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		panelMemoryButtons.setOpaque(false);
 		panelMemoryButtons.setPreferredSize(new Dimension(60, 40));
 		panelMemoryButtons.add(btnM1);
 		panelMemoryButtons.add(btnM2);
 		panelMemoryButtons.add(btnM3);
 		panelMemoryButtons.add(btnM4);
 		panelMemoryButtons.add(btnHelp);
 
 		panelAll.add(panelMemoryButtons);
 		panelAll.add(panelInvert);
 
 		// -----------
 		// COORDINATES
 		// ----------
 		_lblX = new JLabel("X: 0.0000 m");
 		_lblX.setHorizontalAlignment(SwingConstants.CENTER);
 		_lblY = new JLabel("Y: 0.0000 m");
 		_lblY.setHorizontalAlignment(SwingConstants.CENTER);
 		_lblZ = new JLabel("Z: 0.0000 m");
 		_lblZ.setHorizontalAlignment(SwingConstants.CENTER);
 		// JPanel panel_coords = GuiUtil.generatePanel("Current Position");
 		JPanel panelCoords = new JPanel(new GridLayout(3, 1));
 		panelCoords.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		panelCoords.setOpaque(false);
 		panelCoords.setBackground(transparentColor);
 		panelCoords.add(_lblX);
 		panelCoords.add(_lblY);
 		panelCoords.add(_lblZ);
 		panelAll.add(panelCoords);
 
 		// set the frame
 		setLayout(new BorderLayout());
 		add(panelAll, BorderLayout.CENTER);
 		add(_logo_remote, BorderLayout.NORTH);
 		setVisible(true);
 		validate();
 		addToMainDesktopPane();
 		refresh();
 		center();
 		requestFocus();
 		loadPreferences();
 
 		StageMover.addListener(new StageListener() {
 
 			@Override
 			public void stageMoved(final double x, final double y, final double z) {
 				_lblX.setText("X: " + StringUtil.toString(x, 2) + " m");
 				_lblY.setText("Y: " + StringUtil.toString(y, 2) + " m");
 				_lblZ.setText("Z: " + StringUtil.toString(z, 2) + " m");
 			}
 		});
 	}
 
 	/**
 	 * Load preferences : speed, invertX and invertY.
 	 */
 	private void loadPreferences() {
 		Preferences root = Preferences.userNodeForPackage(getClass());
 		_prefs = root.node(root.absolutePath() + "/" + REMOTE);
 		_sliderSpeed.setValue(_prefs.getInt(SPEED, 1));
 		checkInverts();
 	}
 
 	void refresh() {
 		pack();
 	}
 
 	@Override
 	public void onClosed() {
 		super.onClosed();
 	}
 
 	void setEnable(boolean b) {
 		getContentPane().setEnabled(b);
 	}
 	
 	private void checkInverts() {
 		_cbInvertX.setSelected(StageMover.isInvertX());
 		_cbInvertY.setSelected(StageMover.isInvertY());
 		_cbInvertZ.setSelected(StageMover.isInvertZ());
 	}
 
 	public class PanelMoverXY extends JPanel implements MouseListener, MouseMotionListener {
 
 		/**
 		 * Generated serial UID
 		 */
 		private static final long serialVersionUID = -5025582239086787935L;
 
 		private static final int SIZE_PANEL_MOVER = 200;
 
 		/** Movement Vector */
 		private Point2D vector;
 		MoveThread thread;
 		private boolean started = false;
 		private boolean stopMoving = true;
 
 		public PanelMoverXY() {
 			if (imgXYBg == null)
 				System.out.println("\"remote_backgroundXY.png\" not found.");
 			vector = new Point2D.Double(0, 0);
 			thread = new MoveThread();
 			setDoubleBuffered(true);
 			setSize(new Dimension(SIZE_PANEL_MOVER, SIZE_PANEL_MOVER));
 			setPreferredSize(new Dimension(SIZE_PANEL_MOVER, SIZE_PANEL_MOVER));
 			addMouseListener(this);
 			addMouseMotionListener(this);
 		}
 
 		@Override
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			int w = getWidth();
 			int h = getHeight();
 			Shape shape;
 			AffineTransform at;
 			Graphics2D g2 = (Graphics2D) g.create();
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.setColor(new Color(39,39,39));
 			g2.fillRect(0, 0, w, h);
 
 			if (imgXYBg != null) {
 				g2.setColor(transparentColor);
 				g2.fillRect(0, 0, w, h);
 				g2.drawImage(imgXYBg, 0, 0, w, h, null);
 				int stickBallDiameter = w / 5;
 				Point2D centerBall = new Point2D.Double(w / 2d + vector.getX(), h / 2d + vector.getY());
 				g2.setColor(Color.darkGray);
 				double normVector = norm(vector);
 				if (vector.getX() != 0 || vector.getY() != 0) {
 					g2.setColor(Color.blue);
 					// ----------
 					// draw stick
 					// ----------
 					Graphics2D g3 = (Graphics2D) g2.create();
 					at = AffineTransform.getTranslateInstance(centerBall.getX(), centerBall.getY());
 					at.rotate(-vector.getX(), -vector.getY());
 					at.translate(0, -stickBallDiameter / 4);
 					g3.transform(at);
 					g3.setPaint(new GradientPaint(new Point2D.Double(0, 0), Color.BLACK, new Point2D.Double(0, stickBallDiameter / 4), Color.LIGHT_GRAY, true));
 					g3.fillRoundRect(0, 0, (int) normVector, stickBallDiameter / 2, stickBallDiameter / 2, stickBallDiameter / 2);
 					g3.dispose();
 				}
 				// ---------
 				// draw ball
 				// ---------
 				Paint defaultPaint = g2.getPaint();
 				Point2D centerGradient = new Point2D.Double(centerBall.getX(), centerBall.getY());
 				float radiusGradient = stickBallDiameter / 2;
 				Point2D focusSpotLightGradient;
 				if (Math.abs(vector.getX()) <= 1 && Math.abs(vector.getY()) <= 1) {
 					focusSpotLightGradient = new Point2D.Double(centerBall.getX(), centerBall.getY());
 				} else {
 					focusSpotLightGradient = new Point2D.Double(centerBall.getX() + vector.getX() * (radiusGradient - 5) / normVector, centerBall.getY() + vector.getY() / normVector
 							* (radiusGradient - 5));
 				}
 				float[] dist = { 0.1f, 0.3f, 1.0f };
 				Color[] colors = { new Color(0.9f, 0.9f, 0.9f), Color.LIGHT_GRAY, Color.DARK_GRAY };
 				RadialGradientPaint p = new RadialGradientPaint(centerGradient, radiusGradient, focusSpotLightGradient, dist, colors, CycleMethod.NO_CYCLE);
 				g2.setPaint(p);
 				g2.fillOval((int) centerBall.getX() - stickBallDiameter / 2, (int) centerBall.getY() - stickBallDiameter / 2, stickBallDiameter, stickBallDiameter);
 				g2.setPaint(defaultPaint);
 				g2.setColor(Color.BLACK);
 				g2.drawOval((int) centerBall.getX() - stickBallDiameter / 2, (int) centerBall.getY() - stickBallDiameter / 2, stickBallDiameter, stickBallDiameter);
 			} else {
 				super.paint(g);
 				boolean useNormalColors;
 				Color colorLookAndFeel = LookAndFeelUtil.getForeground(this);
 
 				// -------------
 				// Draw the grid
 				// --------------
 				// borders
 				useNormalColors = colorLookAndFeel.getRed() < 50 && colorLookAndFeel.getGreen() < 50 && colorLookAndFeel.getBlue() < 50;
 				if (useNormalColors)
 					g2.setColor(Color.black);
 				else
 					g2.setColor(colorLookAndFeel);
 				g2.drawRect(0, 0, w - 1, h - 1);
 
 				// X AXIS
 				if (useNormalColors)
 					g2.setColor(Color.red);
 				at = AffineTransform.getTranslateInstance(w / 2, h / 2);
 				shape = at.createTransformedShape(createArrow(w, 10));
 				g2.draw(shape);
 				g2.drawString("x", w - 10, h / 2 + 10);
 
 				// Y AXIS
 				if (useNormalColors)
 					g2.setColor(Color.green);
 				at = AffineTransform.getTranslateInstance(w / 2, h / 2);
 				at.rotate(-Math.PI / 2);
 				shape = at.createTransformedShape(createArrow(h, 10));
 				g2.draw(shape);
 				g2.drawString("y", w / 2 - 10 - 10, 10);
 
 				// if no vector, return if (vector.getX() == 0 && vector.getY()
 				// == 0) return;
 
 				// draw the arrow from vector if (useNormalColors)
 				g2.setColor(Color.black);
 				double translateX = w / 2 + vector.getX() / 2;
 				double translateY = h / 2 + vector.getY() / 2;
 				at = AffineTransform.getTranslateInstance((int) translateX, (int) translateY);
 				at.rotate(vector.getX(), vector.getY());
 				double norm = norm(vector) / 4;
 				shape = at.createTransformedShape(createArrow((int) norm(vector), norm >= 1 ? (int) norm : 1));
 				g2.draw(shape);
 				g2.draw(g2.getStroke().createStrokedShape(shape));
 			}
 			g2.dispose();
 		}
 
 		private Path2D.Double createArrow(int length, int barb) {
 			double angle = Math.toRadians(20);
 			Path2D.Double path = new Path2D.Double();
 			path.moveTo(-length / 2, 0);
 			path.lineTo(length / 2, 0);
 			double x = length / 2 - barb * Math.cos(angle);
 			double y = barb * Math.sin(angle);
 			path.lineTo(x, y);
 			x = length / 2 - barb * Math.cos(-angle);
 			y = barb * Math.sin(-angle);
 			path.moveTo(length / 2, 0);
 			path.lineTo(x, y);
 			return path;
 		}
 
 		public void applyMovementXY() throws Exception {
 			double normV = norm(vector);
 			double x = vector.getX() / normV;
 			double y = vector.getY() / normV;
 			double percent = norm(vector) * _sliderSpeed.getValue();
 			if (stopMoving)
 				return;
 			MicroscopeCore mCore = MicroscopeCore.getCore();
 			if (mCore.getAvailablePixelSizeConfigs().size() == 0)
 				StageMover.moveXYRelative(x * 0.001 * percent * percent, y * 0.01 * percent * percent);
 			else
 				StageMover.moveXYRelative(x * 0.001 * mCore.getPixelSizeUm() * percent * percent, y * 0.01 * percent * percent);
 		}
 
 		private double norm(Point2D vector) {
 			return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			int x = e.getX();
 			int y = e.getY();
 			int width = getWidth();
 			int height = getHeight();
 
 			if (x < 0)
 				x = 0;
 			if (x > width)
 				x = width;
 
 			if (y < 0)
 				y = 0;
 			if (y > height)
 				y = height;
 			vector.setLocation(x - width / 2, y - height / 2);
 			repaint();
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			vector.setLocation(e.getX() - (getWidth() / 2), e.getY() - (getHeight() / 2));
 			if (started)
 				return;
 			started = true;
 			stopMoving = false;
 			repaint();
 			ThreadUtil.bgRun(thread);
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			vector.setLocation(0, 0);
 			if (!started)
 				return;
 			thread.setStop(true);
 			started = false;
 			stopMoving = true;
 			repaint();
 		}
 
 		public class MoveThread implements Runnable {
 			private boolean stop;
 
 			public MoveThread() {
 				stop = false;
 			}
 
 			@Override
 			public void run() {
 				stop = false;
 				while (!stop) {
 					try {
 						applyMovementXY();
 						repaint();
 					} catch (Exception e) {
 						break;
 					}
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				try {
 					StageMover.stopXYStage();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				setStop(false);
 			}
 
 			public synchronized void setStop(boolean stop) {
 				this.stop = stop;
 			}
 		}
 	}
 
 	public class PanelMoverZ extends JPanel implements MouseListener, MouseMotionListener {
 
 		/**
 		 * Generated serial UID
 		 */
 		private static final long serialVersionUID = -5025582239086787935L;
 
 		// CONSTANTS
 		private static final int SIZE_PANEL_MOVERZ_W = 50;
 		private static final int SIZE_PANEL_MOVERZ_H = 200;
 
 		private static final double BARS_NUMBER = 200;
 
 		/** Movement Vector */
 		int oldY;
 
 		private int startPos = 0;
 
 		public PanelMoverZ() {
 			setDoubleBuffered(true);
 			setSize(new Dimension(SIZE_PANEL_MOVERZ_W, SIZE_PANEL_MOVERZ_H));
 			setPreferredSize(new Dimension(SIZE_PANEL_MOVERZ_W, SIZE_PANEL_MOVERZ_H));
 			addMouseListener(this);
 			addMouseMotionListener(this);
 		}
 		
 		@Override
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			int w = getWidth();
 			int h = getHeight();
 			Graphics2D g2 = (Graphics2D) g.create();
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.setColor(new Color(39,39,39));
 			g2.fillRect(0, 0, w, h);
 
 			if (imgZBg != null && imgZBar != null) {
 				// draw the background + joystick
 				g2.drawImage(imgZBg, 0, 0, w, h, null);
 				double ecartNormal = (double) h / 8;
 				double lastPos = h / 2d + startPos;
 				for (int i = 0; i < BARS_NUMBER; ++i) {
 					g2.drawImage(imgZBar, 0, (int) lastPos, w, imgZBar.getHeight(null) * w / imgZBar.getWidth(null), null);
 					lastPos = lastPos + ecartNormal / (1d + 0.1 * i * i);
 					if (lastPos > h)
 						break;
 				}
 				lastPos = h / 2d + startPos;
 				for (int i = 0; i < BARS_NUMBER; ++i) {
 					g2.drawImage(imgZBar, 0, (int) lastPos, w, imgZBar.getHeight(null) * w / imgZBar.getWidth(null), null);
 					lastPos = lastPos - ecartNormal / (1d + 0.1 * i * i);
 					if (lastPos < 0)
 						break;
 				}
 			} else {
 				super.paint(g);
 				// draw the old version of remote
 				boolean useNormalColors;
 				Color colorLookAndFeel = LookAndFeelUtil.getForeground(this);
 				useNormalColors = colorLookAndFeel.getRed() < 50 && colorLookAndFeel.getGreen() < 50 && colorLookAndFeel.getBlue() < 50;
 
 				// borders
 				if (useNormalColors)
 					g2.setColor(Color.black);
 				else
 					g2.setColor(colorLookAndFeel);
 				g2.drawRect(0, 0, w - 1, h - 1);
 
 				// Draw the line in the middle
 				if (useNormalColors)
 					g2.setColor(Color.gray);
 				g2.drawRect(0, 0, w, h);
 				if (useNormalColors)
 					g2.setColor(Color.blue);
 				double ecartNormal = (double) h / 8;
 				double lastPos = h / 2d + startPos;
 				for (int i = 0; i < BARS_NUMBER; ++i) {
 					g2.drawLine(0, (int) lastPos, w, (int) lastPos);
 					lastPos = lastPos + ecartNormal / (1d + 0.1 * i * i);
 					if (lastPos > h)
 						break;
 				}
 				lastPos = h / 2d + startPos;
 				for (int i = 0; i < BARS_NUMBER; ++i) {
 					g2.drawLine(0, (int) lastPos, w, (int) lastPos);
 					lastPos = lastPos - ecartNormal / (1d + 0.1 * i * i);
 					if (lastPos < 0)
 						break;
 				}
 			}
 			g2.dispose();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			int y = e.getY();
 			if (y < 0)
 				y = 0;
 			if (y > getHeight())
 				y = getHeight();
 			int movY = e.getY() - oldY;
 			oldY = e.getY();
 			double ecartNormal = getHeight() / 8d / 2;
 			if (movY > 0) {
 				++startPos;
 				if (startPos > ecartNormal)
 					startPos = (int) -ecartNormal;
 			} else {
 				--startPos;
 				if (startPos < -ecartNormal)
 					startPos = (int) ecartNormal;
 			}
 			int percent = _sliderSpeed.getValue();
 			try {
 				StageMover.moveZRelative(movY * 0.01 * percent * percent);
 			} catch (Exception e1) {
 				System.out.println("Error with Z movement");
 			}
 			repaint();
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			oldY = e.getY();
 			setCursor(new Cursor(Cursor.N_RESIZE_CURSOR | Cursor.S_RESIZE_CURSOR));
 			repaint();
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			setCursor(Cursor.getDefaultCursor());
 			repaint();
 		}
 	}
 
	@Override
	public void externalize() {
		super.externalize();
		pack();
	}
	
	@Override
	public void internalize() {
		super.internalize();
		pack();
	}
	
 }
