 package main;
 
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
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.GridData;
 
 import messages.Messages;
 
 public class jmdw_main {
 	private static Text HeightOfWaves;
 	private static Text text_L;
 	private static Text text_B;
 	private static Text text_T;
 	private static Text text_Tf;
 	private static Text text_delta;
 	private static Text text_I;
 	private static Text text_Speed;
 	
 	static Ship ship = new Ship();
 
 	/**
 	 * Launch the application.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		final int hWindow = 400;
 		final int wWindow = 600;
 		Display display = Display.getDefault();
 		Shell shlJavaMdw = new Shell();
 		shlJavaMdw.setMinimumSize(new Point(wWindow, hWindow));
 		shlJavaMdw.setSize(wWindow, hWindow);
 		shlJavaMdw.setText(Messages.getString("jmdw_main.shlJavaMdw.text")); //$NON-NLS-1$
 		shlJavaMdw.setLayout(new GridLayout(4, true));
 
 		Group grpShipsClass = new Group(shlJavaMdw, SWT.NONE);
 		grpShipsClass.setLayout(new GridLayout(2, true));
 		grpShipsClass.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		grpShipsClass.setText(Messages.getString("jmdw_main.grpShipsClass.text")); //$NON-NLS-1$
 
 		final Button button_M = new Button(grpShipsClass, SWT.RADIO);
 		button_M.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		button_M.setSelection(true);
 		button_M.setText(Messages.getString("jmdw_main.button_M.text")); //$NON-NLS-1$
 		
 				final Button button_MSP = new Button(grpShipsClass, SWT.RADIO);
 				button_MSP.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 				button_MSP.setText(Messages.getString("jmdw_main.button_MSP.text")); //$NON-NLS-1$
 
 		final Button button_O = new Button(grpShipsClass, SWT.RADIO);
 		button_O.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		button_O.setText(Messages.getString("jmdw_main.button_O.text")); //$NON-NLS-1$
 		
 				final Button button_MPR = new Button(grpShipsClass, SWT.RADIO);
 				button_MPR.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 				button_MPR.setText(Messages.getString("jmdw_main.button_MPR.text")); //$NON-NLS-1$
 
 		final Button button_R = new Button(grpShipsClass, SWT.RADIO);
 		button_R.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		button_R.setText(Messages.getString("jmdw_main.button_R.text")); //$NON-NLS-1$
 		
 				final Button button_OPR = new Button(grpShipsClass, SWT.RADIO);
 				button_OPR.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 				button_OPR.setText(Messages.getString("jmdw_main.button_OPR.text")); //$NON-NLS-1$
 
 		final Button button_L = new Button(grpShipsClass, SWT.RADIO);
 		button_L.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		button_L.setText(Messages.getString("jmdw_main.button_L.text")); //$NON-NLS-1$
 		new Label(grpShipsClass, SWT.NONE);
 		
 		final Group grpShipsType = new Group(shlJavaMdw, SWT.NONE);
 		grpShipsType.setLayout(new GridLayout(1, true));
 		grpShipsType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		grpShipsType.setText(Messages.getString("jmdw_main.grpShipsType.text")); //$NON-NLS-1$
 		
 		final Button btnCargoShip = new Button(grpShipsType, SWT.RADIO);
 		btnCargoShip.setSelection(true);
 		btnCargoShip.setText(Messages.getString("jmdw_main.btnCargoShip.text")); //$NON-NLS-1$
 		
 		final Button btnPassangerShip = new Button(grpShipsType, SWT.RADIO);
 		btnPassangerShip.setText(Messages.getString("jmdw_main.btnPassangerShip.text")); //$NON-NLS-1$
 		
 		final Button btnTowingShip = new Button(grpShipsType, SWT.RADIO);
 		btnTowingShip.setText(Messages.getString("jmdw_main.btnTowingShip.text")); //$NON-NLS-1$
 				
 		Group grpShipssCharacteristics = new Group(shlJavaMdw, SWT.NONE);
 		grpShipssCharacteristics.setLayout(new GridLayout(2, true));
 		grpShipssCharacteristics.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		grpShipssCharacteristics.setText(Messages.getString("jmdw_main.grpShipssCharacteristics.text")); //$NON-NLS-1$
 		
 		Label lblLM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblLM.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblLM.setText(Messages.getString("jmdw_main.lblLM.text")); //$NON-NLS-1$
 		
 		text_L = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_L.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		Label lblBM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblBM.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblBM.setText(Messages.getString("jmdw_main.lblBM.text")); //$NON-NLS-1$
 		
 		text_B = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_B.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		Label lblTM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblTM.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblTM.setText(Messages.getString("jmdw_main.lblTM.text")); //$NON-NLS-1$
 		
 		text_T = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_T.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		Label lblTfM = new Label(grpShipssCharacteristics, SWT.NONE);
 		lblTfM.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblTfM.setText(Messages.getString("jmdw_main.lblTfM.text")); //$NON-NLS-1$
 		
 		text_Tf = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_Tf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		Label label = new Label(grpShipssCharacteristics, SWT.NONE);
 		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		label.setText(Messages.getString("jmdw_main.label.text")); //$NON-NLS-1$
 		
 		text_delta = new Text(grpShipssCharacteristics, SWT.BORDER | SWT.RIGHT);
 		text_delta.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		Label lblHeightOfWaves = new Label(shlJavaMdw, SWT.NONE);
 		lblHeightOfWaves.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
 		lblHeightOfWaves.setText(Messages.getString("jmdw_main.lblHeightOfWaves.text")); //$NON-NLS-1$
 		
 		Label lblShipsSpeedKmh = new Label(shlJavaMdw, SWT.NONE);
 		lblShipsSpeedKmh.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
 		lblShipsSpeedKmh.setText(Messages.getString("jmdw_main.lblShipsSpeedKmh.text")); //$NON-NLS-1$
 		
 		Label lblMomentOfInertia = new Label(shlJavaMdw, SWT.NONE);
 		lblMomentOfInertia.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2, 1));
 		lblMomentOfInertia.setText(Messages.getString("jmdw_main.lblMomentOfInertia.text")); //$NON-NLS-1$
 		
		HeightOfWaves = new Text(shlJavaMdw, SWT.BORDER | SWT.RIGHT);
		HeightOfWaves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
 		text_Speed = new Text(shlJavaMdw, SWT.BORDER | SWT.RIGHT);
		text_Speed.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
 		
 		text_I = new Text(shlJavaMdw, SWT.BORDER | SWT.RIGHT);
		text_I.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		
 		Group grpResults = new Group(shlJavaMdw, SWT.NONE);
 		grpResults.setLayout(new GridLayout(4, true));
 		grpResults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		grpResults.setText(Messages.getString("jmdw_main.grpResults.text"));
 		
 		final Label lblMv = new Label(grpResults, SWT.NONE);
 		lblMv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblMv.setText(Messages.getString("jmdw_main.lblMv.text")); //$NON-NLS-1$
 		
 		final Text lblNewLabel_0 = new Text(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_0.setEditable(false);
 		lblNewLabel_0.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		new Label(grpResults, SWT.NONE);
 		new Label(grpResults, SWT.NONE);
 		
 		final Label lblKp = new Label(grpResults, SWT.NONE);
 		lblKp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblKp.setText(Messages.getString("jmdw_main.lblKp.text")); //$NON-NLS-1$
 		
 		final Text lblNewLabel_1 = new Text(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblNewLabel_1.setEditable(false);
 		
 		Label lblMdw = new Label(grpResults, SWT.NONE);
 		lblMdw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblMdw.setText(Messages.getString("jmdw_main.lblMdw.text")); //$NON-NLS-1$
 		
 		final Text lblNewLabel_3 = new Text(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblNewLabel_3.setEditable(false);
 		
 		final Label lblMy = new Label(grpResults, SWT.NONE);
 		lblMy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblMy.setText(Messages.getString("jmdw_main.lblMy.text")); //$NON-NLS-1$
 		
 		final Text lblNewLabel_2 = new Text(grpResults, SWT.BORDER | SWT.RIGHT);
 		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		lblNewLabel_2.setEditable(false);
 		new Label(grpResults, SWT.NONE);
 		new Label(grpResults, SWT.NONE);
 		
 		Button btnSolve = new Button(shlJavaMdw, SWT.NONE);
 		btnSolve.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		btnSolve.setText(Messages.getString("jmdw_main.btnSolve.text")); //$NON-NLS-1$
 		
 		final Button btnSaveResults = new Button(shlJavaMdw, SWT.NONE);
 		btnSaveResults.setEnabled(false);
 		btnSaveResults.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		btnSaveResults.setText(Messages.getString("jmdw_main.btnSaveResults.text")); //$NON-NLS-1$
 		
 		
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
 				if(button_O.getSelection()) sclass = 2;  // O
 				if(button_R.getSelection()) sclass = 3;  // P
 				if(button_L.getSelection()) sclass = 4;  // L
 				if(button_MSP.getSelection()) sclass = 5;  // M-SP
 				if(button_MPR.getSelection()) sclass = 6;  // M-PR
 				if(button_OPR.getSelection()) sclass = 7;  // O-PR
 				
 				int stype = 1;
 				if(btnCargoShip.getSelection())     stype = 1;
 				if(btnPassangerShip.getSelection()) stype = 2;
 				if(btnTowingShip.getSelection())    stype = 3;
 				
 				ship.set(L, B, T, Tf, delta, I, Speed, h, stype, sclass);
 				ship.solve();
 				btnSaveResults.setEnabled(true);
 				
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
 		
 		
 		SelectionListener btnSaveResultsSelectionListener = new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				PrintResults pr = new PrintResults();
 				pr.printResultsToHtml();
 			}
 		};
 		
 		
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
 		
 		btnSolve.addSelectionListener(btnSolveSelectionListener);
 		btnSaveResults.addSelectionListener(btnSaveResultsSelectionListener);
 		
 		
 		shlJavaMdw.open();
 		shlJavaMdw.layout();
 		while (!shlJavaMdw.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 	}
 }
