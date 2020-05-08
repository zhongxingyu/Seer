 package edu.ucsb.deepspace.gui;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 
 import edu.ucsb.deepspace.ActInterface.axisType;
 import edu.ucsb.deepspace.Stage;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 public class MainWindow extends org.eclipse.swt.widgets.Composite {
 	private boolean debug = true;
 	private String debugAxis = "A";
 	
 	private Shell shell;
 	private final Properties windowSettings = new Properties();
 	private Stage stage;
 	
 	private String moveType = "degrees";
 	private long moveAmountVal = 0;
 	private boolean minsec = false;
 	private boolean continuousScanOn = false;
 	private double minAz, maxAz, minEl, maxEl;
 	private int encTol;
 	private boolean radecOn = false;
 	private String actInfo = "";
 	private Text moveAmount;
 	private Button azMinus;
 	private Button azPlus;
 	private Button elMinus;
 	private Button elPlus;
 	private Button btnRadioSteps;
 	private Button btnRadioDegrees;
 	private Button btnEncoderSteps;
 	private Button status;
 	private Button indexAz;
 	private Button indexEl;
 	private Button btnCalibrate;
 	private Button btnGoToPosition;
 	private Button btnGoToRaDec;
 	private Button btnBaseLocation;
 	private Button btnBalloonLocation;
 	private Button btnDecimalMinutes;
 	private Button btnMinutesSeconds;
 	private Button btnLock;
 	private Button btnUnlock;
 	private Button btnRaDecOn;
 	private Button btnRaDecOff;
 	private Button btnChangeRadec;
 	private Button btnQuit;
 	private Button btnGoToBalloon;
 	private Button btnSetMinMaxAz;
 	private Button btnSetMinMaxEl;
 	private Button btnScanAz;
 	private Button btnScanEl;
 	private Button btnScanBoth;
 	private Button btnContinuousScan;
 	private Text txtEncTol;
 	private Text txtRa;
 	private Text txtDec;
 	private Text txtMinAz;
 	private Text txtMaxAz;
 	private Text txtMinEl;
 	private Text txtMaxEl;
 	private Text txtMinAzScan;
 	private Text txtMaxAzScan;
 	private Text txtTimeAzScan;
 	private Text txtMinElScan;
 	private Text txtMaxElScan;
 	private Text txtTimeElScan;
 	private Text txtRepScan;
 	private static Map<String, Button> buttonMap = new HashMap<String, Button>();
 	private Text txtPosInfo;
 	private Button btnStop;
 	private Button btnMotorOn;
 	private Button btnMotorOff;
 	private Button btnBegin;
 	private Text txtAzElRaDec;
 	private Text txtBaseLocation;
 	private Text txtBalloonLocation;
 	private Text txtGoalAz;
 	private Text txtGoalEl;
 	private Stage.stageType stageType;
 
 	private Button btnDebugAz;
 
 	private Button btnDebugEl;
 	private Text txtDebugVel;
 
 	public MainWindow(Composite parent, int style, Stage stage, Stage.stageType stageType) {
 		super(parent, style);
 		this.stage = stage;
 		this.stageType = stageType;
 		actInfo = stage.stageInfo();
 		shell = parent.getShell();
 		setPreferences();
 		initGUI();
 		
 		shell.setLayout(new FillLayout());
         shell.setText("Ground COFE");
         shell.layout();
 		shell.open();
 	}
 	
 	public void alive() {
 		while (!shell.isDisposed()) {
             if (!Display.getDefault().readAndDispatch())
             	Display.getDefault().sleep();
         }
 	}
 	
 	private void initGUI() {
 		shell.addDisposeListener(new DisposeListener() {
         	public void widgetDisposed(DisposeEvent evt) {shellWidgetDisposed();}
         });
 
         shell.addControlListener(new ControlAdapter() {
             public void controlResized(ControlEvent evt) {saveShellBounds();}
             public void controlMoved(ControlEvent evt) {saveShellBounds();}
         });
         
         Group area = new Group(this, SWT.NONE);
         area.setText("");
         area.setLayout(null);
         area.setBounds(10, 10, 724, 690);
         
 //--------------------------------------------------------------------------------------------------------------------
     	Group grpJoystick = new Group(area, SWT.NONE);
     	grpJoystick.setText("Joystick");
     	grpJoystick.setBounds(76, 17, 208, 178);
     	
     	moveAmount = new Text(grpJoystick, SWT.BORDER);
     	moveAmount.setBounds(10, 28, 90, 16);
     	moveAmount.setText("amount to move");
     	moveAmount.addModifyListener(new ModifyListener() {
     		public void modifyText(ModifyEvent e) {
     			Long temp = moveAmountVal;
     			try {
     				moveAmountVal = Long.parseLong(moveAmount.getText());
     			} catch (NumberFormatException e1) {
     				moveAmountVal = temp;
     			}
     		}
     	});
     	
     	azMinus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
     	azMinus.setBounds(19, 106, 67, 31);
     	azMinus.setText("azMinus");
     	azMinus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			disableMoveButtons();
     			stage.relative(axisType.AZ, moveType, -moveAmountVal);
     		}
     	});
     	
     	azPlus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
     	azPlus.setBounds(79, 106, 67, 31);
     	azPlus.setText("azPlus");
     	azPlus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			disableMoveButtons();
     			stage.relative(axisType.AZ, moveType, moveAmountVal);
     		}
     	});
     	
     	elMinus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
     	elMinus.setBounds(49, 137, 67, 31);
     	elMinus.setText("elMinus");
     	elMinus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			disableMoveButtons();
     			stage.relative(axisType.EL, moveType, -moveAmountVal);
     		}
     	});
     	
     	elPlus = new Button(grpJoystick, SWT.CENTER);
     	elPlus.setBounds(49, 72, 67, 31);
     	elPlus.setText("elPlus");
     	elPlus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			disableMoveButtons();
     			stage.relative(axisType.EL, moveType, moveAmountVal);
     		}
     	});
     	
     	btnRadioSteps = new Button(grpJoystick, SWT.RADIO);
     	btnRadioSteps.setBounds(106, 10, 47, 16);
     	btnRadioSteps.setText("steps");
     	btnRadioSteps.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			moveType = "steps";
     		}
     	});
     	
     	btnRadioDegrees = new Button(grpJoystick, SWT.RADIO);
     	btnRadioDegrees.setSelection(true);
     	btnRadioDegrees.setBounds(106, 29, 55, 16);
     	btnRadioDegrees.setText("degrees");
     	btnRadioDegrees.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			moveType = "degrees";
     		}
     	});
     	
     	if (stageType.equals(Stage.stageType.FTDI)) {
 	    	btnEncoderSteps = new Button(grpJoystick, SWT.RADIO);
 	    	btnEncoderSteps.setBounds(106, 50, 83, 16);
 	    	btnEncoderSteps.setText("encoder steps");
 	    	btnEncoderSteps.addMouseListener(new MouseAdapter() {
 	    		@Override
 	    		public void mouseDown(MouseEvent e) {
 	    			moveType = "encoder";
 	    		}
 	    	});
     	}
 //--------------------------------------------------------------------------------------------------------------------
     	
     	status = new Button(area, SWT.PUSH | SWT.CENTER);
     	status.setText("status");
     	status.setBounds(10, 93, 60, 30);
     	status.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			stage.status();
     		}
     	});
     	
     	indexAz = new Button(area, SWT.PUSH | SWT.CENTER);
     	indexAz.setText("Index Az");
     	indexAz.setBounds(10, 21, 60, 30);
     	buttonMap.put("indexAz", indexAz);
     	indexAz.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			indexAz.setEnabled(false);
     			
     			stage.index(axisType.AZ);
     		}
     	});
     	
     	indexEl = new Button(area, SWT.PUSH | SWT.CENTER);
     	indexEl.setText("Index El");
     	indexEl.setBounds(10, 57, 60, 30);
     	buttonMap.put("indexEl", indexEl);
     	indexEl.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			
     			indexEl.setEnabled(false);
     			stage.index(axisType.EL);
     		}
     	});
     	
     	btnCalibrate = new Button(area, SWT.PUSH | SWT.CENTER);
     	btnCalibrate.setText("Calibrate");
     	btnCalibrate.setBounds(296, 471, 87, 31);
     	buttonMap.put("calibrate", btnCalibrate);
     	btnCalibrate.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			btnCalibrate.setEnabled(false);
     			new AzElWindow(minsec, "calibrate", stage);
     		}
     	});
     	
     	btnGoToPosition = new Button(area, SWT.NONE);
     	btnGoToPosition.setBounds(384, 471, 87, 31);
     	btnGoToPosition.setText("Go to Position");
     	buttonMap.put("gotopos", btnGoToPosition);
     	btnGoToPosition.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnGoToPosition.setEnabled(false);
     			new AzElWindow(minsec, "gotopos", stage);
     		}
     	});
     	
     	btnGoToRaDec = new Button(area, SWT.NONE);
     	btnGoToRaDec.setBounds(627, 471, 87, 31);
     	btnGoToRaDec.setText("Go to RA / Dec");
     	buttonMap.put("gotoradec", btnGoToRaDec);
     	btnGoToRaDec.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnGoToRaDec.setEnabled(false);
     			new RaDecWindow(minsec, "gotoradec", stage);
     		}
     	});
     	
     	btnBaseLocation = new Button(area, SWT.NONE);
     	btnBaseLocation.setBounds(295, 416, 87, 31);
     	btnBaseLocation.setText("Base Location");
     	buttonMap.put("baseloc", btnBaseLocation);
     	btnBaseLocation.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			btnBaseLocation.setEnabled(false);
     			new LatLongAltWindow(minsec, "baseloc", stage);
     		}
     	});
     	
     	btnBalloonLocation = new Button(area, SWT.NONE);
     	btnBalloonLocation.setBounds(384, 416, 87, 31);
     	btnBalloonLocation.setText("Balloon Location");
     	buttonMap.put("balloonloc", btnBalloonLocation);
     	btnBalloonLocation.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnBalloonLocation.setEnabled(false);
     			new LatLongAltWindow(minsec, "balloonloc", stage);
     		}
     	});
     	
     	btnDecimalMinutes = new Button(area, SWT.RADIO);
     	btnDecimalMinutes.setSelection(true);
     	btnDecimalMinutes.setBounds(273, 454, 96, 16);
     	btnDecimalMinutes.setText("decimal minutes");
     	btnDecimalMinutes.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			minsec = false;
     		}
     	});
     	 	
     	btnMinutesSeconds = new Button(area, SWT.RADIO);
     	btnMinutesSeconds.setBounds(375, 454, 96, 16);
     	btnMinutesSeconds.setText("minutes seconds");
     	btnMinutesSeconds.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			minsec = true;
     		}
     	});
 //--------------------------------------------------------------------------------------------------------------------
     	
     	txtBaseLocation = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtBaseLocation.setBounds(290, 249, 181, 60);
     	txtBaseLocation.setText(stage.baseLocDisplay());
     	
     	txtPosInfo = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtPosInfo.setBounds(290, 17, 181, 226);
     	txtPosInfo.setText("Actuator Information\r\n");
     	
     	final Text txtStatusArea = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
     	txtStatusArea.setBounds(10, 294, 251, 208);
     	txtStatusArea.setText("StatusArea\n\n");
     	
     	txtBalloonLocation = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtBalloonLocation.setBounds(290, 315, 181, 95);
     	txtBalloonLocation.setText(stage.balloonLocDisplay());
     	
     	txtEncTol = new Text(area, SWT.BORDER);
     	txtEncTol.setBounds(10, 176, 49, 19);
     	txtEncTol.setText(String.valueOf(encTol));
     	txtEncTol.addModifyListener(new ModifyListener() {
     		public void modifyText(ModifyEvent e) {
     			String textVal = txtEncTol.getText();
     			int encTol = 0;
     			try {
     				encTol = Integer.parseInt(textVal);
     				if (encTol < 1) {
     					throw new NumberFormatException("encoder tolerance must be an integer larger than 1");
     				}
     			} catch (NumberFormatException e1) {
     				encTol = 2;
     				System.out.println("default to 2");
     				return;
     			}
     			stage.setEncTol(encTol);
     		}
     	});
     	
     	Label lblEncoderTolerance = new Label(area, SWT.WRAP);
     	lblEncoderTolerance.setBounds(10, 140, 49, 30);
     	lblEncoderTolerance.setText("encoder tolerance");
     	
     	btnLock = new Button(area, SWT.NONE);
     	btnLock.setBounds(10, 201, 50, 23);
     	btnLock.setText("Lock");
     	btnLock.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			disableButtons();
     			btnUnlock.setEnabled(true);
     			btnLock.setEnabled(false);
     		}
     	});
     	
     	btnUnlock = new Button(area, SWT.NONE);
     	btnUnlock.setBounds(65, 201, 50, 23);
     	btnUnlock.setText("Unlock");
     	btnUnlock.setEnabled(false);
     	btnUnlock.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			enableButtons();
     			btnUnlock.setEnabled(false);
     			btnLock.setEnabled(true);
     		}
     	});
     	
     	txtAzElRaDec = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
     	txtAzElRaDec.setText("Az:  \nEl:  \nRA:  \nDec:  \nUTC:  \nLST:  \nLocal:");
     	txtAzElRaDec.setFont(SWTResourceManager.getFont("Tahoma", 16, SWT.NORMAL));
     	txtAzElRaDec.setBounds(477, 17, 237, 217);
     	
     	Group grpRaDec = new Group(area, SWT.NONE);
     	grpRaDec.setText("RA / Dec Tracking");
     	grpRaDec.setBounds(497, 357, 217, 108);
     	
     	txtRa = new Text(grpRaDec, SWT.BORDER);
     	txtRa.setEditable(false);
     	txtRa.setBounds(35, 62, 76, 15);
     	
     	txtDec = new Text(grpRaDec, SWT.BORDER);
     	txtDec.setEditable(false);
     	txtDec.setBounds(35, 81, 76, 15);
     	
     	btnRaDecOn = new Button(grpRaDec, SWT.NONE);
     	btnRaDecOn.setBounds(21, 23, 46, 23);
     	btnRaDecOn.setText("On");
     	btnRaDecOn.setEnabled(false);
     	btnRaDecOn.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnRaDecOn.setEnabled(false);
     			btnRaDecOff.setEnabled(true);
     			double ra = Double.parseDouble(txtRa.getText());
     			double dec = Double.parseDouble(txtDec.getText());
     			radecOn = true;
     			stage.startRaDecTracking(ra, dec);
     		}
     	});
     	
     	btnRaDecOff = new Button(grpRaDec, SWT.NONE);
     	btnRaDecOff.setBounds(79, 23, 46, 23);
     	btnRaDecOff.setText("Off");
     	btnRaDecOff.setEnabled(false);
     	btnRaDecOff.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnRaDecOn.setEnabled(true);
     			btnRaDecOff.setEnabled(false);
     			radecOn = false;
     			stage.stopRaDecTracking();
     		}
     	});
     	
     	Label lblRa = new Label(grpRaDec, SWT.NONE);
     	lblRa.setBounds(10, 62, 14, 15);
     	lblRa.setText("RA");
     	
     	Label lblDec = new Label(grpRaDec, SWT.NONE);
     	lblDec.setBounds(10, 81, 24, 15);
     	lblDec.setText("Dec");
     	
     	
     	
     	btnChangeRadec = new Button(grpRaDec, SWT.NONE);
     	btnChangeRadec.setBounds(120, 62, 87, 23);
     	btnChangeRadec.setText("Change RA/Dec");
     	btnChangeRadec.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			new RaDecWindow(minsec, "tracking", stage);
     		}
     	});
     	
     	btnQuit = new Button(area, SWT.NONE);
     	btnQuit.setBounds(128, 201, 68, 23);
     	btnQuit.setText("QUIT");
     	btnQuit.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			shell.close();
     		}
     	});
     	
     	Text txtStaticActInfo = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtStaticActInfo.setBounds(477, 242, 137, 110);
     	txtStaticActInfo.setText(actInfo);
     		
     	btnGoToBalloon = new Button(area, SWT.NONE);
     	btnGoToBalloon.setBounds(477, 471, 78, 31);
     	btnGoToBalloon.setText("Go to Balloon");
     	btnGoToBalloon.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.goToBalloon();
     		}
     	});
     	
     	txtMinAz = new Text(area, SWT.BORDER);
     	txtMinAz.setText(String.valueOf(minAz));
     	txtMinAz.setBounds(10, 269, 49, 19);
     	
     	txtMaxAz = new Text(area, SWT.BORDER);
     	txtMaxAz.setText(String.valueOf(maxAz));
     	txtMaxAz.setBounds(65, 269, 49, 19);
     	
     	btnSetMinMaxAz = new Button(area, SWT.NONE);
     	btnSetMinMaxAz.setBounds(120, 265, 83, 23);
     	btnSetMinMaxAz.setText("Set Min/Max Az");
     	btnSetMinMaxAz.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			try {
     				double minAz = Double.parseDouble(txtMinAz.getText());
     				double maxAz = Double.parseDouble(txtMaxAz.getText());
     				stage.setMinMaxAz(minAz, maxAz);
     				
     			} catch (NumberFormatException e1) {
     				txtStatusArea.append("Must input a number.\n");
     			}
     		}
     	});
     	
     	txtMinEl = new Text(area, SWT.BORDER);
     	txtMinEl.setText(String.valueOf(minEl));
     	txtMinEl.setBounds(10, 240, 49, 19);
     	
     	txtMaxEl = new Text(area, SWT.BORDER);
     	txtMaxEl.setText(String.valueOf(maxEl));
     	txtMaxEl.setBounds(65, 240, 49, 19);
     	
     	btnSetMinMaxEl = new Button(area, SWT.NONE);
     	btnSetMinMaxEl.setBounds(120, 240, 83, 23);
     	btnSetMinMaxEl.setText("Set Min/Max El");
     	btnSetMinMaxEl.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			try {
     				double minEl = Double.parseDouble(txtMinEl.getText());
     				double maxEl = Double.parseDouble(txtMaxEl.getText());
     				stage.setMinMaxEl(minEl, maxEl);
     			} catch (NumberFormatException e1) {
     				txtStatusArea.append("Must input a number.\n");
     			}
     		}
     	});
     	
     	txtGoalAz = new Text(area, SWT.BORDER | SWT.READ_ONLY);
     	txtGoalAz.setBounds(620, 242, 94, 17);
     	
     	txtGoalEl = new Text(area, SWT.BORDER | SWT.READ_ONLY);
     	txtGoalEl.setBounds(620, 269, 94, 17);
     	
     	Group grpScanning = new Group(area, SWT.NONE);
     	grpScanning.setText("Scanning");
     	grpScanning.setBounds(118, 524, 251, 156);
     	
     	Label lblMinAzScan = new Label(grpScanning, SWT.NONE);
     	lblMinAzScan.setBounds(13, 16, 40, 19);
     	lblMinAzScan.setText("min az");
     	
     	Label lblMaxAzScan = new Label(grpScanning, SWT.NONE);
     	lblMaxAzScan.setBounds(13, 41, 40, 19);
     	lblMaxAzScan.setText("max az");
     	
     	Label lblMinElScan = new Label(grpScanning, SWT.NONE);
     	lblMinElScan.setBounds(108, 16, 40, 19);
     	lblMinElScan.setText("min el");
     	
     	Label lblMaxElScan = new Label(grpScanning, SWT.NONE);
     	lblMaxElScan.setBounds(108, 41, 40, 19);
     	lblMaxElScan.setText("max el");
     	
     	Label lblAzTimeScan = new Label(grpScanning, SWT.NONE);
     	lblAzTimeScan.setBounds(13, 66, 40, 19);
     	lblAzTimeScan.setText("az time");
     	
     	Label lblElTimeScan = new Label(grpScanning, SWT.NONE);
     	lblElTimeScan.setBounds(108, 66, 40, 19);
     	lblElTimeScan.setText("el time");
     	
     	txtMinAzScan = new Text(grpScanning, SWT.BORDER);
     	txtMinAzScan.setBounds(59, 16, 43, 19);
     	
     	txtMaxAzScan = new Text(grpScanning, SWT.BORDER);
     	txtMaxAzScan.setBounds(59, 41, 43, 19);
     	
     	txtTimeAzScan = new Text(grpScanning, SWT.BORDER);
     	txtTimeAzScan.setBounds(59, 66, 43, 19);
     	
     	txtMinElScan = new Text(grpScanning, SWT.BORDER);
     	txtMinElScan.setBounds(154, 16, 43, 19);
     	
     	txtMaxElScan = new Text(grpScanning, SWT.BORDER);
     	txtMaxElScan.setBounds(154, 41, 43, 19);
     	
     	txtTimeElScan = new Text(grpScanning, SWT.BORDER);
     	txtTimeElScan.setBounds(154, 66, 43, 19);
     	
     	Label lblRepetitionsScan = new Label(grpScanning, SWT.NONE);
     	lblRepetitionsScan.setBounds(13, 96, 58, 19);
     	lblRepetitionsScan.setText("repetitions");
     	
     	txtRepScan = new Text(grpScanning, SWT.BORDER);
     	txtRepScan.setBounds(72, 96, 43, 19);
     	
     	btnScanAz = new Button(grpScanning, SWT.NONE);
     	
     	
     	btnScanAz.setBounds(10, 121, 68, 23);
     	btnScanAz.setText("Scan Az");
     	btnScanAz.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			if (btnScanAz.getText().equals("Stop Scan")) {
 
     			
     				setScanEnabled(axisType.AZ);
     			
     				
     				
     			}
     			else {
     				btnScanAz.setText("Stop Scan");
     				btnScanEl.setEnabled(false);
     				btnScanBoth.setEnabled(false);
     				try {
 	    				double min = Double.parseDouble(txtMinAzScan.getText());
 	    				double max = Double.parseDouble(txtMaxAzScan.getText());
 	    				double time = Double.parseDouble(txtTimeAzScan.getText());
 	    				int reps = Integer.parseInt(txtRepScan.getText());
 	    				stage.startScanning(min, max, time, reps, axisType.AZ, continuousScanOn);
     				} catch (NumberFormatException e1) {
     					e1.printStackTrace();
     				}
     			}
     		}
     	});
     	
     	btnScanEl = new Button(grpScanning, SWT.NONE);
     	btnScanEl.setBounds(80, 121, 68, 23);
     	btnScanEl.setText("Scan El");
     	btnScanEl.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			if (btnScanEl.getText().equals("Stop Scan")) {
     				setScanEnabled(axisType.EL);
     				
     			}
     			else {
     				btnScanEl.setText("Stop Scan");
     				btnScanBoth.setEnabled(false);
         			btnScanAz.setEnabled(false);
         			try {
 	        			double min = Double.parseDouble(txtMinElScan.getText());
 	    				double max = Double.parseDouble(txtMaxElScan.getText());
 	    				double time = Double.parseDouble(txtTimeElScan.getText());
 	    				int reps = Integer.parseInt(txtRepScan.getText());
 	        			stage.startScanning(min, max, time, reps, axisType.EL,continuousScanOn);
         			} catch (NumberFormatException e1) {
         				
         			}
     			}
     		}
     	});
     	
     	btnScanBoth = new Button(grpScanning, SWT.NONE);
     	btnScanBoth.setBounds(154, 121, 68, 23);
     	btnScanBoth.setText("Scan Both");
     	btnScanBoth.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			if (btnScanBoth.getText().equals("Stop Scan")) {
     				enableScanButtons();
     				btnScanBoth.setText("Scan Both");
     			}
     			else {
     				btnScanBoth.setText("Stop Scan");
     				btnScanEl.setEnabled(false);
         			btnScanAz.setEnabled(false);
     			}
     		}
     	});
     	
     	btnContinuousScan = new Button(grpScanning, SWT.CHECK);
     	btnContinuousScan.setBounds(141, 91, 100, 24);
     	btnContinuousScan.setText("Continuous Scan");
     	btnContinuousScan.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			continuousScanOn = !continuousScanOn;
     		}
     	});
     	
     	Group grpDebug = new Group(area, SWT.NONE);
     	grpDebug.setText("Debug");
     	grpDebug.setBounds(384, 524, 330, 156);
     	
     	Button btnQueueSize = new Button(grpDebug, SWT.NONE);
     	btnQueueSize.setBounds(10, 24, 68, 23);
     	btnQueueSize.setText("queue size");
     	btnQueueSize.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.queueSize();
     		}
     	});
     	
     	btnStop = new Button(grpDebug, SWT.NONE);
     	btnStop.setBounds(84, 24, 68, 23);
     	btnStop.setText("Stop");
     	btnStop.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("ST" + debugAxis);
     		}
     	});
     	
     	btnBegin = new Button(grpDebug, SWT.NONE);
     	btnBegin.setBounds(158, 24, 68, 23);
     	btnBegin.setText("Begin");
     	btnBegin.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("BG" + debugAxis);
     		}
     	});
     	
     	btnMotorOff = new Button(grpDebug, SWT.NONE);
     	btnMotorOff.setBounds(158, 53, 68, 23);
     	btnMotorOff.setText("Motor Off");
     	btnMotorOff.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("MO" + debugAxis);
     		}
     	});
     	
     	
     	btnMotorOn = new Button(grpDebug, SWT.NONE);
     	btnMotorOn.setBounds(84, 53, 68, 23);
     	btnMotorOn.setText("Motor On");
     	btnMotorOn.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("SH" + debugAxis);
     		}
     	});
     	
     	Button btnReadQueue = new Button(grpDebug, SWT.NONE);
     	btnReadQueue.setBounds(10, 53, 68, 23);
     	btnReadQueue.setText("read queue");
     	btnReadQueue.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.readQueue();
     		}
     	});
     	
     	btnDebugAz = new Button(grpDebug, SWT.RADIO);
     	btnDebugAz.setSelection(true);
     	btnDebugAz.setBounds(10, 82, 85, 16);
     	btnDebugAz.setText("Azimuth");
     	btnDebugAz.addSelectionListener(new SelectionAdapter() {
     		@Override
     		public void widgetSelected(SelectionEvent e) {
     			debugAxis = "A";
     		}
     	});
     	
     	btnDebugEl = new Button(grpDebug, SWT.RADIO);
     	btnDebugEl.setBounds(10, 103, 85, 16);
     	btnDebugEl.setText("Elevation");
     	btnDebugEl.addSelectionListener(new SelectionAdapter() {
     		@Override
     		public void widgetSelected(SelectionEvent e) {
     			debugAxis = "B";
     		}
     	});
     	
     	txtDebugVel = new Text(grpDebug, SWT.BORDER);
     	txtDebugVel.setText("vel");
     	txtDebugVel.setBounds(145, 100, 76, 19);
     	
     	Button btnSetVelocity = new Button(grpDebug, SWT.NONE);
     	btnSetVelocity.setBounds(227, 96, 68, 23);
     	btnSetVelocity.setText("Set Velocity");
     	btnSetVelocity.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			
     			double vel = 0;
     			try {
     				vel = Double.parseDouble(txtDebugVel.getText());
     			} catch (NumberFormatException f) {
     				f.printStackTrace();
     			}
     			String out = "JG" + debugAxis + "=" + vel;
     			stage.sendCommand(out);
     		}
     	});
     	
     	
     	//TODO hide the debug stuff
     	if (debug) {
     	}
     	
 	}
 	
 	private void shellWidgetDisposed() {
         try {
         	saveShellBounds();
             windowSettings.store(new FileOutputStream("WindowSettings.ini"), "");
             stage.shutdown();
         } catch (FileNotFoundException ignored) {
         } catch (IOException ignored) {
         }
     }
 	
 	private void saveShellBounds() {
 		Rectangle bounds = shell.getBounds();
         windowSettings.setProperty("top", String.valueOf(bounds.y));
         windowSettings.setProperty("left", String.valueOf(bounds.x));
         windowSettings.setProperty("width", String.valueOf(bounds.width));
         windowSettings.setProperty("height", String.valueOf(bounds.height));
 	}
 	
 	private void setPreferences() {
         try {
             windowSettings.load(new FileInputStream("WindowSettings.ini"));
         } catch (FileNotFoundException ignored) {
         } catch (IOException ignored) {
         }
         
         int width = Integer.parseInt(windowSettings.getProperty("width", "800"));
         int height = Integer.parseInt(windowSettings.getProperty("height", "600"));
         Rectangle screenBounds = getDisplay().getBounds();
         int defaultTop = (screenBounds.height - height) / 2;
         int defaultLeft = (screenBounds.width - width) / 2;
         int top = Integer.parseInt(windowSettings.getProperty("top", String.valueOf(defaultTop)));
         int left = Integer.parseInt(windowSettings.getProperty("left", String.valueOf(defaultLeft)));
         shell.setSize(width, height);
         shell.setLocation(left, top);
         saveShellBounds();
     }
 	
 	public void buttonEnabler(String name) {
 		Button btn = buttonMap.get(name);
 		if (btn == null) {
 			return;
 		}
 		btn.setEnabled(true);
 	}
 	
 	public void enableMoveButtons() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				azPlus.setEnabled(true);
 				azMinus.setEnabled(true);
 				elPlus.setEnabled(true);
 				elMinus.setEnabled(true);
 			}
 		});
 	}
 	
 	public void disableMoveButtons() {
 		azPlus.setEnabled(false);
 		azMinus.setEnabled(false);
 		elPlus.setEnabled(false);
 		elMinus.setEnabled(false);
 	}
 	
 	public void enableButtons() {
 		btnGoToPosition.setEnabled(true);
     	btnBalloonLocation.setEnabled(true);
     	btnCalibrate.setEnabled(true);
     	btnBaseLocation.setEnabled(true);
     	azPlus.setEnabled(true);
     	azMinus.setEnabled(true);
     	elPlus.setEnabled(true);
     	elMinus.setEnabled(true);
     	status.setEnabled(true);
     	indexAz.setEnabled(true);
     	indexEl.setEnabled(true);
     	btnMinutesSeconds.setEnabled(true);
     	btnDecimalMinutes.setEnabled(true);
     	btnRadioSteps.setEnabled(true);
     	btnRadioDegrees.setEnabled(true);
     	btnEncoderSteps.setEnabled(true);
     	txtEncTol.setEnabled(true);
     	if (radecOn) btnRaDecOff.setEnabled(true);
     	else if (txtRa.getText().equals("") && txtDec.getText().equals("")) {}
     	else btnRaDecOn.setEnabled(true);
     	btnGoToRaDec.setEnabled(true);
     	btnChangeRadec.setEnabled(true);
     	btnQuit.setEnabled(true);
     	btnSetMinMaxEl.setEnabled(true);
     	txtMaxEl.setEnabled(true);
     	txtMinEl.setEnabled(true);
     	txtMaxAz.setEnabled(true);
     	txtMinAz.setEnabled(true);
     	moveAmount.setEnabled(true);
     	btnGoToBalloon.setEnabled(true);
     	btnSetMinMaxAz.setEnabled(true);
     	
     	btnScanAz.setEnabled(true);
     	btnScanEl.setEnabled(true);
     	btnScanBoth.setEnabled(true);
     	txtMinAzScan.setEnabled(true);
     	txtMaxAzScan.setEnabled(true);
     	txtTimeAzScan.setEnabled(true);
     	txtMinElScan.setEnabled(true);
     	txtMaxElScan.setEnabled(true);
     	txtTimeElScan.setEnabled(true);
     	txtRepScan.setEnabled(true);
 	}
 	
 	public void disableButtons() {
 		btnGoToPosition.setEnabled(false);
     	btnBalloonLocation.setEnabled(false);
     	btnCalibrate.setEnabled(false);
     	btnBaseLocation.setEnabled(false);
     	azPlus.setEnabled(false);
     	azMinus.setEnabled(false);
     	elPlus.setEnabled(false);
     	elMinus.setEnabled(false);
     	status.setEnabled(false);
     	indexAz.setEnabled(false);
     	indexEl.setEnabled(false);
     	btnMinutesSeconds.setEnabled(false);
     	btnRaDecOff.setEnabled(false);
     	btnDecimalMinutes.setEnabled(false);
     	btnRadioSteps.setEnabled(false);
     	btnRadioDegrees.setEnabled(false);
     	btnEncoderSteps.setEnabled(false);
     	txtEncTol.setEnabled(false);
     	btnRaDecOn.setEnabled(false);
     	btnGoToRaDec.setEnabled(false);
     	btnChangeRadec.setEnabled(false);
     	btnQuit.setEnabled(false);
     	btnSetMinMaxEl.setEnabled(false);
     	txtMaxEl.setEnabled(false);
     	txtMinEl.setEnabled(false);
     	txtMaxAz.setEnabled(false);
     	txtMinAz.setEnabled(false);
     	moveAmount.setEnabled(false);
     	btnGoToBalloon.setEnabled(false);
     	btnSetMinMaxAz.setEnabled(false);
     	
     	btnScanAz.setEnabled(false);
     	btnScanEl.setEnabled(false);
     	btnScanBoth.setEnabled(false);
     	txtMinAzScan.setEnabled(false);
     	txtMaxAzScan.setEnabled(false);
     	txtTimeAzScan.setEnabled(false);
     	txtMinElScan.setEnabled(false);
     	txtMaxElScan.setEnabled(false);
     	txtTimeElScan.setEnabled(false);
     	txtRepScan.setEnabled(false);
 	}
 	
 	public void enableScanButtons() {
		btnScanAz.setEnabled(true);
		btnScanEl.setEnabled(true);
		btnScanBoth.setEnabled(true);
 		
 	}
 	
 	public void updateTxtPosInfo(final String info) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtPosInfo.setText(info);
 			}
 		});
 	}
 	
 	public void updateTxtAzElRaDec(final String info) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtAzElRaDec.setText(info);
 			}
 		});
 	}
 	
 	public void updateBaseBalloonLoc() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtBaseLocation.setText(stage.baseLocDisplay());
 				txtBalloonLocation.setText(stage.balloonLocDisplay());
 			}
 		});
 	}
 	public void displayErrorBox(String message){
 		JOptionPane.showMessageDialog(null, message,"Error ", JOptionPane.ERROR_MESSAGE);
 		
 	}
 	
 	public void setMinMaxAzEl(final double minAz, final double maxAz, final double minEl, final double maxEl) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtMinAz.setText(String.valueOf(minAz));
 				txtMaxAz.setText(String.valueOf(maxAz));
 				txtMinEl.setText(String.valueOf(minEl));
 				txtMaxEl.setText(String.valueOf(maxEl));
 			}
 		});
 	}
 	
 	public void setGoalAz(final double goalAz) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtGoalAz.setText(String.valueOf(goalAz));
 			}
 		});
 	}
 	
 	public void setGoalEl(final double goalEl) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtGoalEl.setText(String.valueOf(goalEl));
 			}
 		});
 	}
 	
 
 	public void setScanEnabled(final axisType type){
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				enableScanButtons();
 				switch (type) {
 				case AZ:
 					btnScanAz.setText("Scan Az" );
 				case EL:
 					btnScanEl.setText("Scan El");
 				}
 				stage.stopScanning();
 				
 				System.out.println("set text");
 			}
 		});
 		
 		
 	}
 
 	public void temp(final boolean asdf) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				btnCalibrate.setEnabled(asdf);
 			}
 		});
 	}
 }
