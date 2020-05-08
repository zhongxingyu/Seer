 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.text.DecimalFormat;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class LensDemoApplication {
 
 
 	private JFrame frame;
 	private JButton btnStartTutor;
 	private JSlider sliderF;
 	private JSlider sliderU;
 	private JSlider sliderV;
 	private JSpinner spinnerF;
 	private JSpinner spinnerU;
 	private JSpinner spinnerV;
 	private JLabel lblInfo;
 	private JComboBox<String> comboBoxSolve;
 	private JPanel panelBottom;
 	private JLabel lblLensType;
 	private JLabel lblImageType;
 	private JLabel lblImageSize;
 	private JLabel lblObjectType;
 
 	static final double MAX_F=10;
 	static final double DEFAULT_F=7.5;
 	static final double MAX_UV=40;
 	static final double DEFAULT_UV=15.0;
 	static final int SLIDER_R=100;
 	
 	enum SolveFor{
 		F,U,V;
 	}
 	private SolveFor solveFor;
 	private LensDemoCanvas canvas;
 	private JSpinner spinnerSize;
 	private JButton btnHelp;
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					LensDemoApplication window = new LensDemoApplication();
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
 	public LensDemoApplication() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setMinimumSize(new Dimension(800, 400));
 		frame.setTitle("Thin Lens Demonstration");
 		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		panelBottom = new JPanel();
 		panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
 		frame.getContentPane().add(panelBottom, BorderLayout.SOUTH);
 		GridBagLayout gbl_panelBottom = new GridBagLayout();
 		gbl_panelBottom.columnWidths = new int[] {0, 0, 0, 125, 0};
 		gbl_panelBottom.rowHeights = new int[] {0, 28, 28, 28};
 		gbl_panelBottom.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0};
 		gbl_panelBottom.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
 		panelBottom.setLayout(gbl_panelBottom);
 		
 		JLabel lblSetTheValues = new JLabel("Set the values:");
 		lblSetTheValues.setToolTipText("f for focal length; u for distance of the object; v for distance of the image.");
 		GridBagConstraints gbc_lblSetTheValues = new GridBagConstraints();
 		gbc_lblSetTheValues.fill = GridBagConstraints.BOTH;
 		gbc_lblSetTheValues.anchor = GridBagConstraints.WEST;
 		gbc_lblSetTheValues.gridwidth = 2;
 		gbc_lblSetTheValues.insets = new Insets(0, 5, 5, 5);
 		gbc_lblSetTheValues.gridx = 0;
 		gbc_lblSetTheValues.gridy = 0;
 		panelBottom.add(lblSetTheValues, gbc_lblSetTheValues);
 		
 		//Construct spiner and sliders for f
 
 		JLabel lblF = new JLabel("f:");
 		GridBagConstraints gbc_lblF = new GridBagConstraints();
 		gbc_lblF.fill = GridBagConstraints.BOTH;
 		gbc_lblF.insets = new Insets(0, 5, 5, 5);
 		gbc_lblF.gridx = 0;
 		gbc_lblF.gridy = 1;
 		panelBottom.add(lblF, gbc_lblF);
 		
 		sliderF = new JSlider((int)-MAX_F*SLIDER_R,(int)MAX_F*SLIDER_R,(int)DEFAULT_F*SLIDER_R);
 		sliderF.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider tmp = (JSlider) e.getSource();
                 double i=(double)tmp.getValue()/(double)SLIDER_R;
                 //System.out.println("SliderF value:"+tmp.getValue()+" -> "+i );
                 spinnerF.setValue(i);
                 if(!solveFor.equals(SolveFor.F)){
                 	valueChanged();
                 }
             }
         });
 		GridBagConstraints gbc_sliderF = new GridBagConstraints();
 		gbc_sliderF.fill = GridBagConstraints.BOTH;
 		gbc_sliderF.insets = new Insets(0, 0, 5, 5);
 		gbc_sliderF.gridx = 1;
 		gbc_sliderF.gridy = 1;
 		panelBottom.add(sliderF, gbc_sliderF);
 
 		spinnerF = new JSpinner();
 		spinnerF.setModel(new SpinnerNumberModel(DEFAULT_F, -MAX_F, MAX_F, 0.2));
 		spinnerF.getModel().addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e){
 				SpinnerNumberModel source=(SpinnerNumberModel) e.getSource();
 				int i=(int) Math.round(source.getNumber().doubleValue()*(double)SLIDER_R);
 				//System.out.println("SpinnerF value:"+source.getValue()+" -> "+i );
 				sliderF.setValue(i);
 				}
 		});
 		GridBagConstraints gbc_spinnerF = new GridBagConstraints();
 		gbc_spinnerF.fill = GridBagConstraints.BOTH;
 		gbc_spinnerF.insets = new Insets(0, 0, 5, 5);
 		gbc_spinnerF.gridx = 2;
 		gbc_spinnerF.gridy = 1;
 		panelBottom.add(spinnerF, gbc_spinnerF);
 		
 		//Construct spiner and sliders for u
 
 		JLabel lblU = new JLabel("u:");
 		GridBagConstraints gbc_lblU = new GridBagConstraints();
 		gbc_lblU.fill = GridBagConstraints.BOTH;
 		gbc_lblU.insets = new Insets(0, 5, 5, 5);
 		gbc_lblU.gridx = 0;
 		gbc_lblU.gridy = 2;
 		panelBottom.add(lblU, gbc_lblU);
 		
 		sliderU = new JSlider((int)-MAX_UV*SLIDER_R,(int)MAX_UV*SLIDER_R,(int)DEFAULT_UV*SLIDER_R);
 		sliderU.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider tmp = (JSlider) e.getSource();
                 double i=(double)tmp.getValue()/(double)SLIDER_R;
                 spinnerU.setValue(i);
                 if(!solveFor.equals(SolveFor.U)){
                 	valueChanged();
                 }
             }
         });
 		GridBagConstraints gbc_sliderU = new GridBagConstraints();
 		gbc_sliderU.fill = GridBagConstraints.BOTH;
 		gbc_sliderU.insets = new Insets(0, 0, 5, 5);
 		gbc_sliderU.gridx = 1;
 		gbc_sliderU.gridy = 2;
 		panelBottom.add(sliderU, gbc_sliderU);
 		
 		spinnerU = new JSpinner();
 		spinnerU.setModel(new SpinnerNumberModel(DEFAULT_UV, -MAX_UV, MAX_UV, 1.0));
 		spinnerU.getModel().addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e){
 				SpinnerNumberModel source=(SpinnerNumberModel) e.getSource();
 				int i=(int) Math.round(source.getNumber().doubleValue()*(double)SLIDER_R);
 				sliderU.setValue(i);
 				}
 		});
 		GridBagConstraints gbc_spinnerU = new GridBagConstraints();
 		gbc_spinnerU.fill = GridBagConstraints.BOTH;
 		gbc_spinnerU.insets = new Insets(0, 0, 5, 5);
 		gbc_spinnerU.gridx = 2;
 		gbc_spinnerU.gridy = 2;
 		panelBottom.add(spinnerU, gbc_spinnerU);
 		
 		btnHelp = new JButton("Help");
 		btnHelp.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				LensDemoHelpDialog helpDialog=new LensDemoHelpDialog();
 				//helpDialog.setModal(true);
 				helpDialog.setVisible(true);
 			}
 		});
 		GridBagConstraints gbc_btnHelp = new GridBagConstraints();
 		gbc_btnHelp.insets = new Insets(0, 0, 5, 0);
 		gbc_btnHelp.fill = GridBagConstraints.BOTH;
 		gbc_btnHelp.gridx = 4;
 		gbc_btnHelp.gridy = 2;
 		panelBottom.add(btnHelp, gbc_btnHelp);
 		
 		//Construct spiner and sliders for v
 
 		JLabel lblV = new JLabel("v:");
 		GridBagConstraints gbc_lblV = new GridBagConstraints();
 		gbc_lblV.fill = GridBagConstraints.BOTH;
 		gbc_lblV.insets = new Insets(0, 5, 0, 5);
 		gbc_lblV.gridx = 0;
 		gbc_lblV.gridy = 3;
 		panelBottom.add(lblV, gbc_lblV);
 		
 		sliderV = new JSlider((int)-MAX_UV*SLIDER_R,(int)MAX_UV*SLIDER_R,(int)DEFAULT_UV*SLIDER_R);
 		sliderV.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider tmp = (JSlider) e.getSource();
                 double i=(double)tmp.getValue()/(double)SLIDER_R;
                 spinnerV.setValue(i);
                 if(!solveFor.equals(SolveFor.V)){
                 	valueChanged();
                 }
             }
         });
 		GridBagConstraints gbc_sliderV = new GridBagConstraints();
 		gbc_sliderV.insets = new Insets(0, 0, 0, 5);
 		gbc_sliderV.fill = GridBagConstraints.BOTH;
 		gbc_sliderV.gridx = 1;
 		gbc_sliderV.gridy = 3;
 		panelBottom.add(sliderV, gbc_sliderV);
 		
 		spinnerV = new JSpinner();
 		spinnerV.setModel(new SpinnerNumberModel(DEFAULT_UV, -MAX_UV, MAX_UV, 1.0));
 		spinnerV.getModel().addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e){
 				SpinnerNumberModel source=(SpinnerNumberModel) e.getSource();
 				int i=(int) Math.round(source.getNumber().doubleValue()*(double)SLIDER_R);
 				sliderV.setValue(i);
 				}
 		});
 		GridBagConstraints gbc_spinnerV = new GridBagConstraints();
 		gbc_spinnerV.fill = GridBagConstraints.BOTH;
 		gbc_spinnerV.insets = new Insets(0, 0, 0, 5);
 		gbc_spinnerV.gridx = 2;
 		gbc_spinnerV.gridy = 3;
 		panelBottom.add(spinnerV, gbc_spinnerV);
 		
 		
 		lblLensType = new JLabel("Convex Lens");
 		GridBagConstraints gbc_lblLensType = new GridBagConstraints();
 		gbc_lblLensType.fill = GridBagConstraints.BOTH;
 		gbc_lblLensType.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLensType.gridx = 3;
 		gbc_lblLensType.gridy = 1;
 		panelBottom.add(lblLensType, gbc_lblLensType);
 
 		lblImageType = new JLabel("Real Image");
 		GridBagConstraints gbc_lblImageType = new GridBagConstraints();
 		gbc_lblImageType.fill = GridBagConstraints.BOTH;
 		gbc_lblImageType.insets = new Insets(0, 0, 0, 5);
 		gbc_lblImageType.gridx = 3;
 		gbc_lblImageType.gridy = 3;
 		panelBottom.add(lblImageType, gbc_lblImageType);
 		
 		lblImageSize = new JLabel("Magnifying: 1.0x");
 		GridBagConstraints gbc_lblImageSize = new GridBagConstraints();
 		gbc_lblImageSize.fill = GridBagConstraints.BOTH;
 		gbc_lblImageSize.insets = new Insets(0, 0, 5, 5);
 		gbc_lblImageSize.gridx = 3;
 		gbc_lblImageSize.gridy = 0;
 		panelBottom.add(lblImageSize, gbc_lblImageSize);
 
 		lblObjectType = new JLabel("Real Object");
 		GridBagConstraints gbc_lblObjectType = new GridBagConstraints();
 		gbc_lblObjectType.fill = GridBagConstraints.BOTH;
 		gbc_lblObjectType.insets = new Insets(0, 0, 5, 5);
 		gbc_lblObjectType.gridx = 3;
 		gbc_lblObjectType.gridy = 2;
 		panelBottom.add(lblObjectType, gbc_lblObjectType);
 
 		comboBoxSolve = new JComboBox<String>();
 		comboBoxSolve.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				changeUnknown((String)e.getItem(),e.getStateChange());
 			}
 		});
 		comboBoxSolve.setModel(new DefaultComboBoxModel<String>(new String[] {"Solve for f", "Solve for u", "Solve for v"}));
 		GridBagConstraints gbc_comboBoxSolve = new GridBagConstraints();
 		gbc_comboBoxSolve.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxSolve.fill = GridBagConstraints.BOTH;
 		gbc_comboBoxSolve.gridx = 2;
 		gbc_comboBoxSolve.gridy = 0;
 		panelBottom.add(comboBoxSolve, gbc_comboBoxSolve);
 		comboBoxSolve.setSelectedItem("Solve for v");
 		
 		btnStartTutor = new JButton("Start Tutorial");
 		btnStartTutor.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				LensDemoTutorialDialog tutorDialog=new LensDemoTutorialDialog();
 				tutorDialog.setParentDemo(LensDemoApplication.this);
 				tutorDialog.setModal(true);
 				int tmpX=frame.getX()+frame.getWidth()-LensDemoTutorialDialog.DEFAULT_WIDTH;
 				//int tmpY=frame.getY()+frame.getHeight()-LensDemoTutorialDialog.DEFAULT_HEIGHT;
 				tutorDialog.setLocation(tmpX,frame.getY());
 				tutorDialog.setVisible(true);
 				
 			}
 		});
 		GridBagConstraints gbc_btnStartTutor = new GridBagConstraints();
 		gbc_btnStartTutor.fill = GridBagConstraints.BOTH;
 		gbc_btnStartTutor.insets = new Insets(0, 0, 5, 0);
 		gbc_btnStartTutor.gridx = 4;
 		gbc_btnStartTutor.gridy = 1;
 		panelBottom.add(btnStartTutor, gbc_btnStartTutor);
 		
 		spinnerSize = new JSpinner();
 		spinnerSize.setModel(new SpinnerNumberModel(DEFAULT_F, null, null, 1.0));
 		spinnerSize.getModel().addChangeListener(new ChangeListener(){
 			public void stateChanged(ChangeEvent e) {
 				SpinnerNumberModel source=(SpinnerNumberModel) e.getSource();
 				canvas.setObjectHeight(source.getNumber().doubleValue());
 				valueChanged();
 			}
 			
 		});
 		GridBagConstraints gbc_spinnerSize = new GridBagConstraints();
 		gbc_spinnerSize.fill = GridBagConstraints.BOTH;
 		gbc_spinnerSize.gridx = 4;
 		gbc_spinnerSize.gridy = 3;
 		panelBottom.add(spinnerSize, gbc_spinnerSize);
 		
 		lblInfo = new JLabel("Hello World.    An application created by Hanzhen, Lin.");
 		frame.getContentPane().add(lblInfo, BorderLayout.NORTH);
 		
 		canvas = new LensDemoCanvas();
 		frame.getContentPane().add(canvas, BorderLayout.CENTER);
 		canvas.setParentDemo(this);
 		
 	}
 	private void changeUnknown(String str,int stateChange){
 		if(stateChange==ItemEvent.SELECTED){
 			if(str.equals("Solve for f")){
 				solveFor=SolveFor.F;
 				spinnerF.setEnabled(false);
 				sliderF.setEnabled(false);
 			}else if (str.equals("Solve for u")) {
 				solveFor=SolveFor.U;
 				spinnerU.setEnabled(false);
 				sliderU.setEnabled(false);
 			}else if (str.equals("Solve for v")) {
 				solveFor=SolveFor.V;
 				spinnerV.setEnabled(false);
 				sliderV.setEnabled(false);
 			}
 		}else{
 			if(str.equals("Solve for f")){
 				spinnerF.setEnabled(true);
 				sliderF.setEnabled(true);
 			}else if (str.equals("Solve for u")) {
 				spinnerU.setEnabled(true);
 				sliderU.setEnabled(true);
 			}else if (str.equals("Solve for v")) {
 				spinnerV.setEnabled(true);
 				sliderV.setEnabled(true);
 			}
 		}
 	}
 	private void valueChanged(){
 		System.out.println("valueChanged");
 		double f=((SpinnerNumberModel) spinnerF.getModel()).getNumber().doubleValue();
 		double u=((SpinnerNumberModel) spinnerU.getModel()).getNumber().doubleValue();
 		double v=((SpinnerNumberModel) spinnerV.getModel()).getNumber().doubleValue();
		if(f==0){
 			JOptionPane.showMessageDialog(frame, "f cannot be equal to zero!", 
 					"Invalid Focal Length",JOptionPane.WARNING_MESSAGE);
 			return;
		}
 		if(solveFor.equals(SolveFor.F)){
 			f=1/(1/u+1/v);
 			spinnerF.setValue(f);
 		}else if (solveFor.equals(SolveFor.U)) {
 			u=1/(1/f-1/v);
 			spinnerU.setValue(u);
 		}else if (solveFor.equals(SolveFor.V)) {
 			v=1/(1/f-1/u);
 			spinnerV.setValue(v);
 		}
 		identifyType(f,u,v);
 		canvas.draw(f,u,v);
 	}
 	private void identifyType(double f,double u, double v){
 		if(Double.isInfinite(u)){
 			lblObjectType.setText("Infinity Far Object");
 		}else if(u>0){
 			lblObjectType.setText("Real Object");
 		}else{
 			lblObjectType.setText("Virtual Object");
 		}
 		
 		if(Double.isInfinite(v)){
 			lblImageType.setText("No Image Formed");
 		}else if(v>0){
 			lblImageType.setText("Real Image");
 		}else{
 			lblImageType.setText("Virtual Image");
 		}
 
 		if(f==0){
 			lblLensType.setText("Invalid Lens");
 		}else if(f>0){
 			lblLensType.setText("Convex Lens");
 		}else{
 			lblLensType.setText("Concave Lens");
 		}
 		
 		DecimalFormat df=new DecimalFormat("#.###x");
 		if(Math.abs(v/u)>1.0){
 			lblImageSize.setText("Magnifying: "+df.format(v/u));
 		}else{
 			lblImageSize.setText("Minimizing: "+df.format(v/u));
 		}
 	}
 	public void setValueExternal(double f,double u,double v,double size,SolveFor sf){
 		if(Double.isNaN(size)){size=DEFAULT_F;}
 		/*if(Double.isNaN(  f )){f   =DEFAULT_F;}
 		if(Double.isNaN(  u )){u   =DEFAULT_UV;}
 		if(Double.isNaN(u   )){u   =DEFAULT_UV;}*/
 		if(solveFor.equals(SolveFor.F)){
 			spinnerU.setValue(u);
 			spinnerV.setValue(v);
 		}else if (solveFor.equals(SolveFor.U)) {
 			spinnerF.setValue(f);
 			spinnerV.setValue(v);
 		}else if (solveFor.equals(SolveFor.V)) {
 			spinnerU.setValue(u);
 			spinnerF.setValue(f);
 		}
 		if(!Double.isNaN(size)){
 			spinnerSize.setValue(size);
 		}
 	}
 	public SolveFor getSolveFor(){
 		return solveFor;
 	}
 }
