 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.miginfocom.swing.MigLayout;
 
 public class SettingsMenuView extends JFrame {
 	private final ButtonGroup shapeButtonGroup = new ButtonGroup();
 	private final ButtonGroup delayButtonGroup = new ButtonGroup();
 	private final ButtonGroup graphicsButtonGroup = new ButtonGroup();
 	private int graphics; 
 
 
 	/**
 	 * Create the frame.
 	 */
 	public SettingsMenuView(final MainWindow mw) {
 		setTitle("Simulation settings");
 		setBounds(100, 100, 515, 480);
 		getContentPane().setLayout(null);
 		
 		JLabel lblShapeOfUniverse = new JLabel("Shape of Universe");
 		lblShapeOfUniverse.setBounds(7, 7, 99, 15);
 		lblShapeOfUniverse.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		getContentPane().add(lblShapeOfUniverse);
 		
 		JLabel lblNumberOfIterations = new JLabel("Number of Iterations");
 		lblNumberOfIterations.setBounds(207, 7, 114, 15);
 		lblNumberOfIterations.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		getContentPane().add(lblNumberOfIterations);
 		
 		JRadioButton rdbtnSquare = new JRadioButton("Square");
 		rdbtnSquare.setBounds(7, 26, 59, 23);
 		rdbtnSquare.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//SQUARE
 				System.out.println("Square shaped universe");
 			}
 		});
 		shapeButtonGroup.add(rdbtnSquare);
 		getContentPane().add(rdbtnSquare);
 		
 		JSpinner spinnerIterations = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1)); //vilket spann? nu 0-1000
 		spinnerIterations.setBounds(207, 27, 63, 20);
 		spinnerIterations.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				//NUMBER OF ITERATIONS
 				System.out.println("number of iterations set");
 			}
 		});
 		getContentPane().add(spinnerIterations);
 		
 		JRadioButton rdbtnCircle = new JRadioButton("Circle");
 		rdbtnCircle.setBounds(7, 53, 51, 23);
 		rdbtnCircle.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//CIRCLE
 				System.out.println("Circle shaped universe");
 			}
 		});
 		shapeButtonGroup.add(rdbtnCircle);
 		getContentPane().add(rdbtnCircle);
 		
 		JRadioButton rdbtnTriangle = new JRadioButton("Triangle");
 		rdbtnTriangle.setBounds(7, 80, 63, 23);
 		rdbtnTriangle.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//TRIANGLE
 				System.out.println("Triangle shaped universe");
 			}
 		});
 		shapeButtonGroup.add(rdbtnTriangle);
 		getContentPane().add(rdbtnTriangle);
 		
 		JLabel lblObstacles = new JLabel("Obstacles");
 		lblObstacles.setBounds(207, 83, 52, 15);
 		lblObstacles.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		getContentPane().add(lblObstacles);
 		
 		final JSlider sliderObstacles = new JSlider();  //vilket spann?
 		sliderObstacles.setBounds(207, 107, 195, 23);
 		sliderObstacles.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				if(!sliderObstacles.getValueIsAdjusting()) {
 					//OBSTACLES
 					System.out.println("obstacles set");
 				}
 			}
 		});
 		getContentPane().add(sliderObstacles);
 		
 		JLabel lblDelay = new JLabel("Delay");
 		lblDelay.setBounds(7, 134, 29, 15);
 		lblDelay.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		getContentPane().add(lblDelay);
 		
 		JRadioButton rdbtnDelayOn = new JRadioButton("On");
 		rdbtnDelayOn.setBounds(7, 153, 39, 23);
 		rdbtnDelayOn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//DELAY ON
 				System.out.println("delay on");
 			}
 		});
 		delayButtonGroup.add(rdbtnDelayOn);
 		getContentPane().add(rdbtnDelayOn);
 		
 		JLabel lblGraphicsEngine = new JLabel("Graphics Engine");
 		lblGraphicsEngine.setBounds(207, 156, 86, 15);
 		lblGraphicsEngine.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		getContentPane().add(lblGraphicsEngine);
 		
 		JRadioButton rdbtnOff = new JRadioButton("Off");
 		rdbtnOff.setBounds(7, 180, 41, 23);
 		rdbtnOff.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//DELAY OFF
 				System.out.println("delay off");
 			}
 		});
 		delayButtonGroup.add(rdbtnOff);
 		getContentPane().add(rdbtnOff);
 		
 		JRadioButton rdbtnJavaAWT = new JRadioButton("Java AWT");
 		rdbtnJavaAWT.setBounds(207, 180, 75, 23);
 		rdbtnJavaAWT.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//JAVA AWT
 				System.out.println("awt graphics");
 				graphics = 0;
 			}
 		});
 		graphicsButtonGroup.add(rdbtnJavaAWT);
 		getContentPane().add(rdbtnJavaAWT);
 		
 		JRadioButton rdbtnOpenGL = new JRadioButton("OpenGL");
 		rdbtnOpenGL.setBounds(207, 207, 63, 23);
 		rdbtnOpenGL.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//OPEN GL
 				System.out.println("openGL graphics");
 				graphics = 1;
 			}
 		});
 		graphicsButtonGroup.add(rdbtnOpenGL);
 		getContentPane().add(rdbtnOpenGL);
 		
 		JLabel lblDelayLength = new JLabel("Delay length");
 		lblDelayLength.setBounds(7, 234, 68, 15);
 		lblDelayLength.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		getContentPane().add(lblDelayLength);
 		
 		final JSlider sliderDelayLength = new JSlider();  //vilket spann?
 		sliderDelayLength.setBounds(7, 253, 196, 23);
 		sliderDelayLength.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				if(!sliderDelayLength.getValueIsAdjusting()) {
 					//DELAY LENGTH
 					System.out.println("delay length set");
 				}
 			}
 		});
 		getContentPane().add(sliderDelayLength);
 		
 		JButton btnOk = new JButton("OK");
 		btnOk.setBounds(207, 253, 47, 23);
 		btnOk.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				SettingsMenuView.this.dispose();
 			}
 		});
 		getContentPane().add(btnOk);
 
 	}
 }
