 package edu.agh.tunev.ui;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.beans.PropertyVetoException;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.swing.AbstractAction;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import com.jogamp.newt.awt.NewtCanvasAWT;
 import com.jogamp.newt.opengl.GLWindow;
 
 import edu.agh.tunev.model.AbstractModel;
 import edu.agh.tunev.model.PersonProfile;
 import edu.agh.tunev.statistics.Statistics;
 import edu.agh.tunev.ui.opengl.Refresher;
 import edu.agh.tunev.ui.opengl.Scene;
 import edu.agh.tunev.world.World;
 
 final class ControllerFrame extends JInternalFrame {
 
 	private static final double minRho = 5; // [m]
 	private static final double maxRho = 50; // [m]
 	private static final double dRho = 0.2; // [m]
 	private static final double dPhi = 0.2; // [deg]
 	private static final double dTheta = 0.2; // [deg]
 	private static final double dxy = 1.0; // [m]
 
 	static {
 		GLProfile.initSingleton();
 	}
 
 	private static final long serialVersionUID = 1L;
 
 	private AbstractModel model;
 	private Vector<PersonProfile> people;
 	private World world;
 
 	private int modelNumber;
 	private String modelName;
 
 	ControllerFrame(int modelNumber, String modelName, Class<?> model,
 			final World world) {
 		this.modelNumber = modelNumber;
 		this.modelName = modelName;
 		this.world = world;
 
 		setModel(model);
 
 		people = PeopleFactory.random(50, world.getDimension());
 
 		init();
 		createGLFrame();
 	}
 
 	void setModel(Class<?> model) {
 		try {
 			this.model = (AbstractModel) model.getDeclaredConstructor(
 					World.class).newInstance(world);
 		} catch (InstantiationException | IllegalAccessException
 				| IllegalArgumentException | InvocationTargetException
 				| NoSuchMethodException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("Error during instantiation of "
 					+ model.getName() + ".");
 		}
 	}
 
 	private static final Insets INSETS = new Insets(5, 5, 5, 5);
 	private static final double DT = 0.01;
 
 	private JButton buttonPlay, buttonStop;
 	private JLabel simulationMsg, simulationIter, simulationTime, playbackTime;
 	private JProgressBar simulationProgress;
 	private JPopupMenu plotMenu;
 	private JSlider slider;
 	private double sliderTime = 0.0, progressTime = 0.0;
 	private double rho = 25, phi = 20, theta = 81;
 	private boolean paintTemp;
 	private Point2D.Double anchor = new Point2D.Double(0, 0);
 	private DecimalFormat decimalFormat = new DecimalFormat("0.00");
 	Refresher refresher;
 	private GLWindow glwindow;
 	private NewtCanvasAWT glcanvas;
 
 	private void createGLFrame() {
 		// in a new thread, because loading of JOGL takes some time, we don't
 		// want to put this on AWT thread and block UI
 		new Thread(new Runnable() {
 			public void run() {
 				GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
 				caps.setSampleBuffers(true);
 				caps.setNumSamples(16);
 				glwindow = GLWindow.create(caps);
 				glcanvas = new NewtCanvasAWT(glwindow);
 
 				glwindow.addGLEventListener(new Scene(world, model, people,
 						new Scene.SceneGetter() {
 							public double getTime() {
 								return sliderTime;
 							}
 
 							public double getRho() {
 								return rho;
 							}
 
 							public double getPhi() {
 								return Math.toRadians(phi);
 							}
 
 							public double getTheta() {
 								return Math.toRadians(theta);
 							}
 
 							public Point2D.Double getAnchor() {
 								return anchor;
 							}
 
 							public boolean getPaintTemp() {
 								return paintTemp;
 							}
 						}));
 
 				refresher = new Refresher(glwindow);
 
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						JInternalFrame frame = new JInternalFrame();
 
 						frame.setTitle(modelNumber + ": " + modelName + " - "
 								+ " visualization");
 						frame.setSize(600, 450);
 						frame.setLocation(modelNumber * 20 + 400,
 								modelNumber * 20);
 						frame.setFrameIcon(null);
 						frame.setResizable(true);
 
 						frame.getContentPane().add(glcanvas,
 								BorderLayout.CENTER);
 						ControllerFrame.this.getParent().add(frame);
 						frame.setVisible(true);
 
 						// simulate after both frames were loaded
 						simulate();
 					}
 				});
 			}
 		}).start();
 	}
 
 	private void init() {
 		setTitle(modelNumber + ": " + modelName + " - controller");
 		setFrameIcon(null);
 		setLocation(modelNumber * 20, modelNumber * 20);
 
 		JPanel p = new JPanel();
 		p.setBorder(new EmptyBorder(INSETS));
 		p.setLayout(new GridBagLayout());
 		add(p, BorderLayout.PAGE_START);
 
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.gridwidth = 1;
 		c.gridy = -1;
 
 		// column struts
 
 		c.gridy++;
 		for (c.gridx = 0; c.gridx < 5; c.gridx++)
 			p.add(Box.createHorizontalStrut(10000), c);
 		c.insets = INSETS;
 
 		// simulation labels
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		p.add(new JLabel("Simulation:"), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 2;
 		simulationMsg = new JLabel();
 		p.add(simulationMsg, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		simulationIter = new JLabel();
 		p.add(simulationIter, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		simulationTime = new JLabel();
 		p.add(simulationTime, c);
 
 		// simulation progress
 
 		c.gridwidth = 5;
 		c.gridx = 0;
 		c.gridy++;
 		simulationProgress = new JProgressBar();
 		p.add(simulationProgress, c);
 
 		// separator
 
 		c.gridwidth = 1;
 		c.gridy++;
 		p.add(new JPanel(), c);
 
 		// playback labels
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 4;
 		p.add(new JLabel("Playback:"), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		playbackTime = new JLabel("abc");
 		p.add(playbackTime, c);
 
 		// playback slider
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 5;
 		slider = new JSlider(0, (int) Math.round(Math.ceil(world.getDuration()
 				/ DT)), 0);
 		p.add(slider, c);
 		slider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				onSliderChange();
 			}
 		});
 		onSliderChange();
 
 		// separator
 
 		c.gridwidth = 1;
 		c.gridy++;
 		p.add(new JPanel(), c);
 
 		// buttons
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		buttonPlay = new JButton("Play");
 		p.add(buttonPlay, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		buttonStop = new JButton("Stop");
 		buttonStop.setEnabled(false);
 		p.add(buttonStop, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JCheckBox checkPaintTemp = new JCheckBox("paint T");
 		paintTemp = true;
 		checkPaintTemp.setSelected(paintTemp);
 		p.add(checkPaintTemp, c);
 
 		buttonPlay.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				buttonPlay.setEnabled(false);
 				slider.setEnabled(false);
 				buttonStop.setEnabled(true);
 				play(1.0);
 			}
 		});
 		buttonStop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					playThread.interrupt();
 				} catch (Exception e) {
 				}
 			}
 		});
 		checkPaintTemp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				paintTemp = checkPaintTemp.isSelected();
 				refresh();
 			}
 		});
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JButton buttonPlot = new JButton("Plot...");
 		p.add(buttonPlot, c);
 
 		plotMenu = new JPopupMenu();
 
 		buttonPlot.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				plotMenu.show(buttonPlot, 0, buttonPlot.getHeight());
 			}
 		});
 
 		// camera control: rho
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		p.add(new JLabel("Camera \u03C1 = "), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 3;
 		final JSlider rhoSlider = new JSlider((int) Math.round(minRho / dRho),
 				(int) Math.round(maxRho / dRho), (int) Math.round(rho / dRho));
 		p.add(rhoSlider, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JLabel rhoLabel = new JLabel();
 		p.add(rhoLabel, c);
 
 		rhoSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				rho = rhoSlider.getValue() * dRho;
 				rhoLabel.setText(decimalFormat.format(rho) + " [m]");
 				refresh();
 				if (rhoSlider.getValue() == rhoSlider.getMaximum()
 						|| rhoSlider.getValue() == rhoSlider.getMinimum())
 					forceRefresh();
 			}
 		});
 		rhoSlider.getChangeListeners()[0].stateChanged(null);
 
 		// camera control: phi
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		p.add(new JLabel("Camera \u03D5 = "), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 3;
 		final JSlider phiSlider = new JSlider((int) Math.round(dPhi / dPhi),
 				(int) Math.round((90.0 - dPhi) / dPhi), (int) Math.round(phi / dPhi));
 		p.add(phiSlider, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JLabel phiLabel = new JLabel();
 		p.add(phiLabel, c);
 
 		phiSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				phi = phiSlider.getValue() * dPhi;
 				phiLabel.setText(decimalFormat.format(phi) + "\u00b0");
 				refresh();
 				if (phiSlider.getValue() == phiSlider.getMaximum()
 						|| phiSlider.getValue() == phiSlider.getMinimum())
 					forceRefresh();
 			}
 		});
 		phiSlider.getChangeListeners()[0].stateChanged(null);
 
 		// camera control: theta
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		p.add(new JLabel("Camera \u03B8 = "), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 3;
 		final JSlider thetaSlider = new JSlider((int) Math.round(0 / dTheta),
 				(int) Math.round(360 / dPhi), (int) Math.round(theta / dPhi));
 		p.add(thetaSlider, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JLabel thetaLabel = new JLabel();
 		p.add(thetaLabel, c);
 
 		thetaSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				theta = thetaSlider.getValue() * dTheta;
 				thetaLabel.setText(decimalFormat.format(theta) + "\u00b0");
 				refresh();
 				if (thetaSlider.getValue() == thetaSlider.getMaximum()
 						|| thetaSlider.getValue() == thetaSlider.getMinimum())
 					forceRefresh();
 			}
 		});
 		thetaSlider.getChangeListeners()[0].stateChanged(null);
 
 		// camera control: x
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		p.add(new JLabel("Camera X = "), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 3;
 		anchor.x = world.getDimension().x / 2;
 		final JSlider xSlider = new JSlider((int) Math.round(0 / dxy),
 				(int) Math.round(world.getDimension().x / dxy),
 				(int) Math.round(anchor.x / dxy));
 		p.add(xSlider, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JLabel xLabel = new JLabel();
 		p.add(xLabel, c);
 
 		xSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				anchor.x = xSlider.getValue() * dxy;
 				xLabel.setText(decimalFormat.format(anchor.x) + " [m]");
 				refresh();
 				if (xSlider.getValue() == xSlider.getMaximum()
 						|| xSlider.getValue() == xSlider.getMinimum())
 					forceRefresh();
 			}
 		});
 		xSlider.getChangeListeners()[0].stateChanged(null);
 
 		// camera control: y
 
 		c.gridy++;
 		c.gridx = 0;
 		c.gridwidth = 0;
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		p.add(new JLabel("Camera Y = "), c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 3;
 		anchor.y = world.getDimension().y / 2;
 		final JSlider ySlider = new JSlider((int) Math.round(0 / dxy),
 				(int) Math.round(world.getDimension().y / dxy),
 				(int) Math.round(anchor.y / dxy));
 		p.add(ySlider, c);
 
 		c.gridx += c.gridwidth;
 		c.gridwidth = 1;
 		final JLabel yLabel = new JLabel();
 		p.add(yLabel, c);
 
 		ySlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				anchor.y = ySlider.getValue() * dxy;
 				yLabel.setText(decimalFormat.format(anchor.y) + " [m]");
 				refresh();
 				if (ySlider.getValue() == ySlider.getMaximum()
 						|| ySlider.getValue() == ySlider.getMinimum())
 					forceRefresh();
 			}
 		});
 		ySlider.getChangeListeners()[0].stateChanged(null);
 
 		// show
 
 		setVisible(true);
 		pack();
 		setSize(400, getSize().height);
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					ControllerFrame.this.setSelected(true);
 				} catch (PropertyVetoException e) {
 					ControllerFrame.this.toFront();
 					slider.requestFocus();
 				}
 			}
 		});
 	}
 
 	private double previousSliderTime = Double.POSITIVE_INFINITY;
 
 	private void onSliderChange() {
 		sliderTime = Math.min(DT * slider.getValue(), progressTime);
 		playbackTime
 				.setText("t = " + decimalFormat.format(sliderTime) + " [s]");
 
 		// refresh visualisation
 		if (Math.abs(previousSliderTime - sliderTime) > DT / 2) {
 			previousSliderTime = sliderTime;
 			refresh();
			if (slider.getValue() == slider.getMaximum()
					|| slider.getValue() == slider.getMinimum())
 				forceRefresh();
 		}
 	}
 
 	private void refresh() {
 		if (refresher != null)
 			refresher.refresh();
 	}
 
 	private void forceRefresh() {
 		if (refresher != null)
 			refresher.forceRefresh();
 	}
 
 	private void onPlayingFinished() {
 		buttonPlay.setEnabled(true);
 		slider.setEnabled(true);
 		buttonStop.setEnabled(false);
 	}
 
 	private void simulate() {
 		new Thread(new Runnable() {
 			public void run() {
 				model.simulate(world.getDuration(), people,
 						new World.ProgressCallback() {
 							public void update(final int done, final int total,
 									final String msg) {
 								progressTime = world.getDuration() * done
 										/ total;
 								SwingUtilities.invokeLater(new Runnable() {
 									public void run() {
 										simulationProgress.setMaximum(total);
 										simulationProgress.setValue(done);
 										simulationMsg.setText(msg);
 										simulationIter.setText(done + "/"
 												+ total);
 										simulationTime.setText("t = "
 												+ decimalFormat
 														.format(progressTime)
 												+ " [s]");
 
 										onSliderChange();
 									}
 								});
 							}
 						}, new Statistics.AddCallback() {
 							@Override
 							public void add(Statistics statistics) {
 								final PlotFrame frame = new PlotFrame(
 										modelNumber, modelName, statistics);
 								ControllerFrame.this.getParent().add(frame);
 
 								plotMenu.add(new JMenuItem(new AbstractAction(
 										statistics.getTitle()) {
 									private static final long serialVersionUID = 1L;
 
 									public void actionPerformed(ActionEvent arg0) {
 										frame.show();
 									}
 								}));
 
 							}
 						});
 			}
 		}).start();
 	}
 
 	private Thread playThread = null;
 
 	private void play(double speed) {
 		playThread = new Thread(new Runnable() {
 			public void run() {
 				boolean cont = slider.getValue() < slider.getMaximum();
 				try {
 					while (cont) {
 						Thread.sleep(Math.round(DT * 1000));
 						cont = slider.getValue() + 1 < slider.getMaximum();
 						SwingUtilities.invokeLater(new Runnable() {
 							public void run() {
 								slider.setValue(slider.getValue() + 1);
 							}
 						});
 					}
 				} catch (InterruptedException e) {
 					// probably stop pressed
 				}
 				onPlayingFinished();
 			}
 		});
 		playThread.start();
 	}
 
 }
