 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 
 public class jmdw_main {
 	private static Text HeightOfWaves;
 	private static Text text_L;
 	private static Text text_B;
 	private static Text text_T;
 	private static Text text_Tf;
 	private static Text text_delta;
 	private static Text text_I;
 	private static Text text_Speed;
	//private static Text txtMomentOfInertia;
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Display display = Display.getDefault();
 		Shell shlJavaMdw = new Shell();
 		shlJavaMdw.setSize(450, 400);
 		shlJavaMdw.setText("Java MDW");
 
 		Group grpShipsClass = new Group(shlJavaMdw, SWT.NONE);
 		grpShipsClass.setText("Ship's class");
 		grpShipsClass.setBounds(10, 10, 144, 138);
 
 		final Button button_M = new Button(grpShipsClass, SWT.RADIO);
 		button_M.setSelection(true);
 		button_M.setBounds(10, 23, 44, 16);
 		button_M.setText("\u041C");
 
 		final Button button_O = new Button(grpShipsClass, SWT.RADIO);
 		button_O.setBounds(10, 45, 44, 16);
 		button_O.setText("\u041E");
 
 		final Button button_R = new Button(grpShipsClass, SWT.RADIO);
 		button_R.setBounds(10, 67, 44, 16);
 		button_R.setText("\u0420");
 
 		final Button button_L = new Button(grpShipsClass, SWT.RADIO);
 		button_L.setBounds(10, 89, 44, 16);
 		button_L.setText("\u041B");
 
 		final Button button_MSP = new Button(grpShipsClass, SWT.RADIO);
 		button_MSP.setBounds(60, 23, 64, 16);
 		button_MSP.setText("\u041C-\u0421\u041F");
 
 		final Button button_MPR = new Button(grpShipsClass, SWT.RADIO);
 		button_MPR.setBounds(60, 45, 64, 16);
 		button_MPR.setText("\u041C-\u041F\u0420");
 
 		final Button button_OPR = new Button(grpShipsClass, SWT.RADIO);
 		button_OPR.setBounds(60, 67, 64, 16);
 		button_OPR.setText("\u041E-\u041F\u0420");
 		
 		Label lblHeightOfWaves = new Label(shlJavaMdw, SWT.NONE);
 		lblHeightOfWaves.setBounds(10, 176, 144, 15);
 		lblHeightOfWaves.setText("Height of waves, m");
 		
 		HeightOfWaves = new Text(shlJavaMdw, SWT.BORDER | SWT.RIGHT);
 		HeightOfWaves.setBounds(10, 197, 144, 21);
 		
 		final Group grpShipsType = new Group(shlJavaMdw, SWT.NONE);
 		grpShipsType.setText("Ship's type");
 		grpShipsType.setBounds(160, 10, 118, 138);
 		
 		final Button btnCargoShip = new Button(grpShipsType, SWT.RADIO);
 		btnCargoShip.setSelection(true);
 		btnCargoShip.setBounds(10, 23, 105, 16);
 		btnCargoShip.setText("cargo ship");
 		
 		final Button btnPassangerShip = new Button(grpShipsType, SWT.RADIO);
 		btnPassangerShip.setBounds(10, 45, 105, 16);
 		btnPassangerShip.setText("passanger ship");
 		
 		final Button btnTowingShip = new Button(grpShipsType, SWT.RADIO);
 		btnTowingShip.setBounds(10, 67, 105, 16);
 		btnTowingShip.setText("towing ship");
 		
 		Group grpShipssCharacteristics = new Group(shlJavaMdw, SWT.NONE);
 		grpShipssCharacteristics.setText("Ships's characteristics");
 		grpShipssCharacteristics.setBounds(284, 10, 140, 138);
 		
 		Label lblLM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblLM.setBounds(10, 24, 40, 15);
 		lblLM.setText("L, m");
 		
 		Label lblBM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblBM.setBounds(10, 45, 40, 15);
 		lblBM.setText("B, m");
 		
 		Label lblTM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblTM.setBounds(10, 66, 40, 15);
 		lblTM.setText("T, m");
 		
 		Label lblTfM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblTfM.setBounds(10, 87, 40, 15);
 		lblTfM.setText("Tf, m");
 		
 		Label label = new Label(grpShipssCharacteristics, SWT.NONE);
 		label.setBounds(10, 108, 40, 15);
 		label.setText("\u03B4");
 		
 		text_L = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_L.setBounds(56, 24, 76, 21);
 		
 		text_B = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_B.setBounds(56, 45, 76, 21);
 		
 		text_T = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_T.setBounds(56, 66, 76, 21);
 		
 		text_Tf = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_Tf.setBounds(56, 87, 76, 21);
 		
 		text_delta = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_delta.setBounds(56, 108, 76, 21);
 		
		Label lblMomentOfInertia = new Label(shlJavaMdw, SWT.NONE);
		lblMomentOfInertia.setBounds(284, 161, 140, 30);
		lblMomentOfInertia.setText("Moment of inertia\r\nof cross section, m^4");
		
		/*
 		txtMomentOfInertia = new Text(shlJavaMdw, SWT.READ_ONLY | SWT.MULTI);
 		txtMomentOfInertia.setText("Moment of inertia\r\nof cross section, m^4");
 		txtMomentOfInertia.setBounds(284, 161, 140, 30);
		**/
 		
 		text_I = new Text(shlJavaMdw, SWT.BORDER | SWT.RIGHT);
 		text_I.setBounds(284, 197, 140, 21);
 		
 		Label lblShipsSpeedKmh = new Label(shlJavaMdw, SWT.NONE);
 		lblShipsSpeedKmh.setBounds(160, 176, 118, 15);
 		lblShipsSpeedKmh.setText("Ship's speed, km/h");
 		
 		text_Speed = new Text(shlJavaMdw, SWT.BORDER | SWT.RIGHT);
 		text_Speed.setBounds(160, 197, 118, 21);
 		
 		Group grpResults = new Group(shlJavaMdw, SWT.NONE);
 		grpResults.setText("Results");
 		grpResults.setBounds(10, 224, 414, 99);
 		
 		final Label lblMv = new Label(grpResults, SWT.NONE);
 		lblMv.setBounds(10, 20, 55, 15);
 		lblMv.setText("Mv");
 		
 		final Label lblKp = new Label(grpResults, SWT.NONE);
 		lblKp.setBounds(10, 41, 55, 15);
 		lblKp.setText("kp");
 		
 		final Label lblMy = new Label(grpResults, SWT.NONE);
 		lblMy.setBounds(10, 62, 55, 15);
 		lblMy.setText("My");
 		
 		final Label lblNewLabel_0 = new Label(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_0.setBounds(71, 20, 127, 15);
 		
 		final Label lblNewLabel_1 = new Label(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_1.setBounds(71, 41, 127, 15);
 		
 		final Label lblNewLabel_2 = new Label(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_2.setBounds(71, 62, 127, 15);
 		
 		Label lblMdw = new Label(grpResults, SWT.NONE);
 		lblMdw.setBounds(214, 41, 55, 15);
 		lblMdw.setText("Mdw");
 		
 		final Label lblNewLabel_3 = new Label(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_3.setBounds(275, 41, 129, 15);
 		
 		Button btnSolve = new Button(shlJavaMdw, SWT.NONE);
 		btnSolve.setBounds(10, 329, 200, 25);
 		btnSolve.setText("Solve");
 		
 		Button btnSaveResults = new Button(shlJavaMdw, SWT.NONE);
 		btnSaveResults.setEnabled(false);
 		btnSaveResults.setBounds(224, 329, 200, 25);
 		btnSaveResults.setText("Save results");
 		
 		
 		SelectionListener btnSolveSelectionListener = new SelectionAdapter(){
 			public void widgetSelected(SelectionEvent e) {
 				double L = Double.parseDouble(text_L.getText());
 				double B = Double.parseDouble(text_B.getText());
 				double T = Double.parseDouble(text_T.getText());
 				double Tf = Double.parseDouble(text_Tf.getText());
 				double delta = Double.parseDouble(text_delta.getText());
 				double I = Double.parseDouble(text_I.getText());
 				double Speed = Double.parseDouble(text_Speed.getText());
 				double h = Double.parseDouble(HeightOfWaves.getText());
 				
 				int sclass = 1;
 				if(button_M.getSelection()) sclass = 1;  // M
 				if(button_O.getSelection()) sclass = 2;  // 
 				if(button_R.getSelection()) sclass = 3;  // 
 				if(button_L.getSelection()) sclass = 4;  // 
 				if(button_MSP.getSelection()) sclass = 5;  // -
 				if(button_MPR.getSelection()) sclass = 6;  // -
 				if(button_OPR.getSelection()) sclass = 7;  // -
 				
 				int stype = 1;
 				if(btnCargoShip.getSelection())     stype = 1;
 				if(btnPassangerShip.getSelection()) stype = 2;
 				if(btnTowingShip.getSelection())    stype = 3;
 				
 				Ship ship = new Ship();
 				ship.set(L, B, T, Tf, delta, I, Speed, h, stype, sclass);
 				
 				String s = null;
 				
 				double Mdw = ship.getMdw();
 				s = Double.toString(Mdw);
 				lblNewLabel_3.setText(s);
 				
 				double Mv = ship.getMv();
 				s = Double.toString(Mv);
 				lblNewLabel_0.setText(s);
 				
 				double kp = ship.getkp();
 				s = Double.toString(kp);
 				lblNewLabel_1.setText(s);
 				
 				double My = ship.getMy();
 				s = Double.toString(My);
 				lblNewLabel_2.setText(s);
 			}
 		};
 		
 		btnSolve.addSelectionListener(btnSolveSelectionListener);
 		
 		
 		SelectionListener leftRadioButtonsSelectionListener = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				grpShipsType.setEnabled(true);
 				btnCargoShip.setEnabled(true);
 				btnPassangerShip.setEnabled(true);
 				btnTowingShip.setEnabled(true);
 				lblMv.setEnabled(true);
 				lblKp.setEnabled(true);
 				lblMy.setEnabled(true);
 				lblNewLabel_0.setEnabled(true);
 				lblNewLabel_1.setEnabled(true);
 				lblNewLabel_2.setEnabled(true);
 			}
 		};
 		
 		SelectionListener rightRadioButtonsSelectionListener = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				grpShipsType.setEnabled(false);
 				btnCargoShip.setEnabled(false);
 				btnPassangerShip.setEnabled(false);
 				btnTowingShip.setEnabled(false);
 				lblMv.setEnabled(false);
 				lblKp.setEnabled(false);
 				lblMy.setEnabled(false);
 				lblNewLabel_0.setEnabled(false);
 				lblNewLabel_1.setEnabled(false);
 				lblNewLabel_2.setEnabled(false);
 			}
 		};
 		
 		
 		button_M.addSelectionListener(leftRadioButtonsSelectionListener);
 		button_O.addSelectionListener(leftRadioButtonsSelectionListener);
 		button_R.addSelectionListener(leftRadioButtonsSelectionListener);
 		button_L.addSelectionListener(leftRadioButtonsSelectionListener);
 		
 		button_MSP.addSelectionListener(rightRadioButtonsSelectionListener);
 		button_MPR.addSelectionListener(rightRadioButtonsSelectionListener);
 		button_OPR.addSelectionListener(rightRadioButtonsSelectionListener);
 		
 		shlJavaMdw.open();
 		shlJavaMdw.layout();
 		while (!shlJavaMdw.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 }
