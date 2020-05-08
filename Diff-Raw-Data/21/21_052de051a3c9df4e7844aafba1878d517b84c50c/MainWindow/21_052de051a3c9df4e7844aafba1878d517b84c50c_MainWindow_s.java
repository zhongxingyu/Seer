 package edu.ucsb.deepspace.gui;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import edu.ucsb.deepspace.Axis;
 import edu.ucsb.deepspace.Formatters;
 import edu.ucsb.deepspace.GalilCalc;
 import edu.ucsb.deepspace.MoveCommand.MoveType;
 import edu.ucsb.deepspace.ScanCommand;
 import edu.ucsb.deepspace.Stage;
 
 public class MainWindow extends org.eclipse.swt.widgets.Composite {
 	public boolean debug = true;
 	private Axis debugAxis = Axis.AZ;
 	
 	private Shell shell;
 	private final Properties windowSettings = new Properties();
 	private Stage stage;
 	
 	private String motorOff = "Motor Off", motorOn = "Motor On";
 	private MoveType moveType = MoveType.DEGREE;
 	private long moveAmountVal = 0;
 	private boolean minsec = false;
 	private boolean continuousModeOn = false;
 	private boolean RaDecModeOn = false;
 	private boolean rasterScan = false;
 	private String scanType = "";
 	private double minAz = 0, maxAz = 0, minEl = 0, maxEl = 0;
 	private int encTol;
 	private boolean radecOn = false;
 	private Text moveAmount;
 	private Button azMinus, azPlus, elMinus, elPlus;
 	private Button btnRadioSteps, btnRadioDegrees, btnEncoderSteps;
 	private Button status;
 	private Button indexAz;
 	private Button indexEl;
 	private Button btnCalibrate;
 	private Button btnRaCalibrate;
 	private Button btnGoToPosition;
 	private Button btnGoToRaDec;
 	private Button btnBaseLocation, btnBalloonLocation;
 	private Button btnDecimalMinutes, btnMinutesSeconds;
 	private Button btnLock, btnUnlock;
 	private Button btnRaDecOn, btnRaDecOff;
 	private Button btnChangeRadec;
 	private Button btnQuit;
 	private Button btnGoToBalloon;
 	private Button btnScanAz;
 	private Button btnScanEl;
 	private Button btnScan;
 	private Button btnScanSnake;
 	private Button btnScanSquare;
 	private Button btnContinuousMode;
 	private Button btnRaDecMode;
 	private Text txtEncTol;
 	private Text txtRa;
 	private Text txtDec;
 	private Button btnSetMinMaxAz, btnSetMinMaxEl;
 	private Text txtMinAz, txtMaxAz;
 	private Text txtMinEl, txtMaxEl;
 	private Text txtMinAzScan, txtMaxAzScan;
 	private Text txtTimeScan;
 	private Text txtMinElScan, txtMaxElScan;
 	
 	private Text txtRepScan;
 	private static Map<String, Button> popupWindowButtons = new HashMap<String, Button>();
 	private Text txtPosInfo;
 	private Button btnStop;
 	private Button btnMotorOn, btnMotorOff;
 	private Button btnBegin;
 	private Text txtAzElRaDec;
 	private Text txtBaseLocation;
 	private Text txtBalloonLocation;
 	private Text txtGoalAz, txtGoalEl;
 	private Stage.StageTypes stageType;
 
 	private Button btnDebugAz, btnDebugEl;
 	private Text txtDebugVel;
 	private Text txtStatusArea;
 	private List<Text> scanAzTexts = new ArrayList<Text>();
 	private List<Text> scanElTexts = new ArrayList<Text>();
 	private Text txtVelAz, txtAccAz;
 	private Text txtVelEl, txtAccEl;
 	private Button btnSetMaxVelAccAz, btnSetMaxVelAccEl;
 	private Button btnMotorAz, btnMotorEl;
 	private Button btnStopAz, btnStopEl;
 	private Group area;
 	private Group grpLatitutdeLongitudeControl;
 	private Text txtMaxRelAz;
 	private Group grpDebug;
 	private Button btnRADecCalibrate;
 	private Label lblActual;
 	private Button btnReaderControl;
 	private org.eclipse.swt.widgets.List expectedScripts;
 	private org.eclipse.swt.widgets.List loadedScripts;
 	private Text txtMaxRelEl;
 	private Button btnLoadScripts;
 	private Text txtCommandArea;
 	private Text txtAzRpm;
 	private Text txtElRpm;
 	private double lines = 10;
 
 	public MainWindow(Composite parent, int style, Stage stage, Stage.StageTypes StageTypes) {
 		super(parent, style);
 		this.stage = stage;
 		this.stageType = StageTypes;
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
         
         area = new Group(this, SWT.NONE);
         area.setText("");
         area.setLayout(null);
         area.setBounds(10, 0, 734, 876);
         
         guiJoystick();
         guiRaDec();
         guiScanning();
         guiAxisControl();
         guiDebug();
     	
     	status = new Button(area, SWT.PUSH | SWT.CENTER);
     	status.setText("status");
     	status.setBounds(10, 17, 60, 30);
     	status.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			stage.status();
     		}
     	});
     	
     	
     
     	btnCalibrate = new Button(area, SWT.PUSH | SWT.CENTER);
     	btnCalibrate.setText("Calibrate");
     	btnCalibrate.setBounds(387, 437, 96, 31);
     	popupWindowButtons.put("calibrate", btnCalibrate);
     	btnCalibrate.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			btnCalibrate.setEnabled(false);
     			new AzElWindow(minsec, "calibrate", stage);
     		}
     	});
     	
 
     	btnRADecCalibrate = new Button(area, SWT.NONE);
     	btnRADecCalibrate.setBounds(387, 474, 96, 30);
     	btnRADecCalibrate.setText("RA/Dec Calibrate");
     	popupWindowButtons.put("radeccalibrate", btnRADecCalibrate);
     	btnRADecCalibrate.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			btnRADecCalibrate.setEnabled(false);
     			new RaDecWindow(minsec, "radeccalibrate", stage);
     		}
     	});
     	
     	btnGoToPosition = new Button(area, SWT.NONE);
     	btnGoToPosition.setBounds(290, 471, 87, 31);
     	btnGoToPosition.setText("Go to Position");
     	popupWindowButtons.put("gotopos", btnGoToPosition);
     	btnGoToPosition.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnGoToPosition.setEnabled(false);
     			new AzElWindow(minsec, "gotopos", stage);
     		}
     	});
     	
     	btnGoToRaDec = new Button(area, SWT.NONE);
     	btnGoToRaDec.setBounds(290, 504, 87, 31);
     	btnGoToRaDec.setText("Go to RA / Dec");
     	popupWindowButtons.put("gotoradec", btnGoToRaDec);
     	btnGoToRaDec.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnGoToRaDec.setEnabled(false);
     			new RaDecWindow(minsec, "gotoradec", stage);
     		}
     	});
     	
     	btnDecimalMinutes = new Button(area, SWT.RADIO);
     	btnDecimalMinutes.setSelection(true);
     	btnDecimalMinutes.setBounds(378, 541, 96, 16);
     	btnDecimalMinutes.setText("decimal minutes");
     	btnDecimalMinutes.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			minsec = false;
     		}
     	});
     	 	
     	btnMinutesSeconds = new Button(area, SWT.RADIO);
     	btnMinutesSeconds.setBounds(378, 563, 96, 16);
     	btnMinutesSeconds.setText("minutes seconds");
     	btnMinutesSeconds.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			minsec = true;
     		}
     	});
     	
     	txtPosInfo = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtPosInfo.setBounds(290, 17, 255, 133);
     	txtPosInfo.setFont(SWTResourceManager.getFont("Lucida Console", 10, SWT.NORMAL));
     	txtPosInfo.setText("Actuator Information\r\n");
     	
     	txtEncTol = new Text(area, SWT.BORDER);
     	txtEncTol.setBounds(11, 89, 49, 19);
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
     	lblEncoderTolerance.setBounds(11, 53, 49, 30);
     	lblEncoderTolerance.setText("encoder tolerance");
     	
     	btnLock = new Button(area, SWT.NONE);
     	btnLock.setBounds(10, 114, 50, 23);
     	btnLock.setText("Lock");
     	btnLock.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			Control[] asdf = area.getChildren();
     			for (Control c : asdf) {
     				c.setEnabled(false);
     				if (c.getClass() == org.eclipse.swt.widgets.Group.class) {
     					Control[] asdf2 = ((Composite) c).getChildren();
     					for (Control c1 : asdf2) {
     						c1.setEnabled(false);
     					}
     				}
     			}
     			btnUnlock.setEnabled(true);
     			btnLock.setEnabled(false);
     		}
     	});
     	
     	btnUnlock = new Button(area, SWT.NONE);
     	btnUnlock.setBounds(10, 142, 50, 23);
     	btnUnlock.setText("Unlock");
     	btnUnlock.setEnabled(false);
     	btnUnlock.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			Control[] asdf = area.getChildren();
     			for (Control c : asdf) {
     				c.setEnabled(true);
     				if (c.getClass() == org.eclipse.swt.widgets.Group.class) {
     					Control[] asdf2 = ((Composite) c).getChildren();
     					for (Control c1 : asdf2) {
     						c1.setEnabled(true);
     					}
     				}
     			}
     			
     			if (radecOn) btnRaDecOff.setEnabled(false);
     	    	else if (txtRa.getText().equals("") && txtDec.getText().equals("")) {
     	    		btnRaDecOff.setEnabled(false);
     	    		btnRaDecOn.setEnabled(false);
     	    	}
     	    	else btnRaDecOn.setEnabled(false);
     			
     			btnUnlock.setEnabled(false);
     			btnLock.setEnabled(true);
     		}
     	});
     	
     	txtAzElRaDec = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
     	txtAzElRaDec.setText("Az:  \nEl:  \nRA:  \nDec:  \nUTC:  \nLST:  \nLocal:");
     	txtAzElRaDec.setFont(SWTResourceManager.getFont("Tahoma", 16, SWT.NORMAL));
     	txtAzElRaDec.setBounds(551, 17, 173, 181);
     	
     	btnQuit = new Button(area, SWT.NONE);
     	btnQuit.setBounds(2, 171, 68, 23);
     	btnQuit.setText("QUIT");
     	btnQuit.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			shell.close();
     		}
     	});
     		
     	btnGoToBalloon = new Button(area, SWT.NONE);
     	btnGoToBalloon.setBounds(290, 437, 87, 31);
     	btnGoToBalloon.setText("Go to Balloon");
     	btnGoToBalloon.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.goToBalloon();
     		}
     	});
     	
     	txtGoalAz = new Text(area, SWT.BORDER | SWT.READ_ONLY);
     	txtGoalAz.setBounds(297, 173, 94, 17);
     	
     	txtGoalEl = new Text(area, SWT.BORDER | SWT.READ_ONLY);
     	txtGoalEl.setBounds(397, 173, 94, 17);
     	
     	txtStatusArea = new Text(area, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
     	txtStatusArea.setBounds(10, 729, 358, 133);
     	txtStatusArea.setText("StatusArea\n\n");
     	
     	grpLatitutdeLongitudeControl = new Group(area, SWT.NONE);
     	grpLatitutdeLongitudeControl.setText("Latitutde Longitude Control");
     	grpLatitutdeLongitudeControl.setBounds(290, 204, 201, 227);
     	    	
     	txtBaseLocation = new Text(grpLatitutdeLongitudeControl, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtBaseLocation.setBounds(10, 20, 181, 60);
     	txtBaseLocation.setText("Base Location");
     	txtBaseLocation.setText(stage.baseLocDisplay());
 
     	txtBalloonLocation = new Text(grpLatitutdeLongitudeControl, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
     	txtBalloonLocation.setBounds(10, 85, 181, 95);
     	txtBalloonLocation.setText("Balloon Location");
     	txtBalloonLocation.setText(stage.balloonLocDisplay());
 
     	btnBaseLocation = new Button(grpLatitutdeLongitudeControl, SWT.NONE);
     	btnBaseLocation.setBounds(10, 186, 87, 31);
     	btnBaseLocation.setText("Base Location");
     	btnBaseLocation.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			btnBaseLocation.setEnabled(false);
     			new LatLongAltWindow(minsec, "baseloc", stage);
     		}
     	});
     	popupWindowButtons.put("baseloc", btnBaseLocation);
     	
     	btnBalloonLocation = new Button(grpLatitutdeLongitudeControl, SWT.NONE);
     	btnBalloonLocation.setBounds(99, 186, 87, 31);
     	btnBalloonLocation.setText("Balloon Location");
     	btnBalloonLocation.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			btnBalloonLocation.setEnabled(false);
     			new LatLongAltWindow(minsec, "balloonloc", stage);
     		}
     	});
     	popupWindowButtons.put("balloonloc", btnBalloonLocation);
     	
     	Group grpScripts = new Group(area, SWT.NONE);
     	grpScripts.setText("Scripts");
     	grpScripts.setBounds(497, 204, 217, 184);
     	
     	expectedScripts = new org.eclipse.swt.widgets.List(grpScripts, SWT.BORDER);
     	expectedScripts.setBounds(10, 38, 71, 68);
     	
     	loadedScripts = new org.eclipse.swt.widgets.List(grpScripts, SWT.BORDER);
     	loadedScripts.setBounds(87, 38, 71, 68);
     	
     	Label lblExpected = new Label(grpScripts, SWT.NONE);
     	lblExpected.setBounds(20, 19, 49, 13);
     	lblExpected.setText("Expected");
     	
     	lblActual = new Label(grpScripts, SWT.NONE);
     	lblActual.setBounds(96, 19, 49, 13);
     	lblActual.setText("Actual");
     	
     	btnLoadScripts = new Button(grpScripts, SWT.NONE);
     	btnLoadScripts.setBounds(97, 112, 71, 23);
     	btnLoadScripts.setText("Load Scripts");
     	btnLoadScripts.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.loadScripts();
     			btnLoadScripts.setVisible(false);
     		}
     	});
     	
     	Button btnRefreshScripts = new Button(grpScripts, SWT.NONE);
     	btnRefreshScripts.setBounds(10, 112, 85, 23);
     	btnRefreshScripts.setText("Refresh Scripts");
     	btnRefreshScripts.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.refreshScripts();
     			//btnLoadScripts.setVisible(false);
     		}
     	});
     	
     	Label lblAzimuthGoal = new Label(area, SWT.NONE);
     	lblAzimuthGoal.setBounds(300, 156, 68, 13);
     	lblAzimuthGoal.setText("Azimuth Goal");
     	
     	Label lblElevationGoal = new Label(area, SWT.NONE);
     	lblElevationGoal.setBounds(403, 156, 71, 13);
     	lblElevationGoal.setText("Elevation Goal");
     	
     	txtCommandArea = new Text(area, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
     	txtCommandArea.setText("Command Area");
     	txtCommandArea.setBounds(378, 729, 336, 133);
 
     	if (!debug) {
     		grpDebug.setVisible(false);
     	}
     	
 	}
 	
 	private void guiJoystick() {
     	Group grpJoystick = new Group(area, SWT.NONE);
     	grpJoystick.setText("Joystick");
     	grpJoystick.setBounds(76, 17, 208, 222);
     	
     	moveAmount = new Text(grpJoystick, SWT.BORDER);
     	moveAmount.setBounds(10, 28, 90, 16);
     	moveAmount.setText("0");
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
     			controlMoveButtons(false);
     			stage.moveRelative((double) -moveAmountVal, Axis.AZ, moveType);
     		}
     	});
     	
     	azPlus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
     	azPlus.setBounds(79, 106, 67, 31);
     	azPlus.setText("azPlus");
     	azPlus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			controlMoveButtons(false);
     			stage.moveRelative((double) moveAmountVal, Axis.AZ, moveType);
     		}
     	});
     	
     	elMinus = new Button(grpJoystick, SWT.PUSH | SWT.CENTER);
     	elMinus.setBounds(49, 137, 67, 31);
     	elMinus.setText("elMinus");
     	elMinus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			controlMoveButtons(false);
     			stage.moveRelative((double) -moveAmountVal, Axis.EL, moveType);
     		}
     	});
     	
     	elPlus = new Button(grpJoystick, SWT.CENTER);
     	elPlus.setBounds(49, 72, 67, 31);
     	elPlus.setText("elPlus");
     	elPlus.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			controlMoveButtons(false);
     			stage.moveRelative((double) moveAmountVal, Axis.EL, moveType);
     		}
     	});
     	
     	btnRadioSteps = new Button(grpJoystick, SWT.RADIO);
     	btnRadioSteps.setBounds(106, 10, 47, 16);
     	btnRadioSteps.setText("steps");
     	btnRadioSteps.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			//moveType = "steps";
     		}
     	});
     	
     	if (stageType.equals(Stage.StageTypes.GALIL)) {
 	    	btnRadioSteps.setVisible(false);
     	}
     	
     	btnRadioDegrees = new Button(grpJoystick, SWT.RADIO);
     	btnRadioDegrees.setSelection(true);
     	btnRadioDegrees.setBounds(106, 29, 55, 16);
     	btnRadioDegrees.setText("degrees");
     	btnRadioDegrees.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			moveType = MoveType.DEGREE;
     		}
     	});
     	
     	btnEncoderSteps = new Button(grpJoystick, SWT.RADIO);
     	btnEncoderSteps.setBounds(106, 50, 83, 16);
     	btnEncoderSteps.setText("encoder steps");
     	btnEncoderSteps.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			moveType = MoveType.ENCODER;
     		}
     	});
     	
     	txtMaxRelAz = new Text(grpJoystick, SWT.BORDER);
     	txtMaxRelAz.setBounds(22, 196, 42, 19);
     	
     	txtMaxRelEl = new Text(grpJoystick, SWT.BORDER);
     	txtMaxRelEl.setBounds(86, 196, 42, 19);
     	
     	Label lblMaxRel = new Label(grpJoystick, SWT.NONE);
     	lblMaxRel.setBounds(37, 174, 90, 16);
     	lblMaxRel.setText("Max Relative Move");
     	
     	Label lblAz = new Label(grpJoystick, SWT.NONE);
     	lblAz.setBounds(10, 199, 15, 13);
     	lblAz.setText("Az");
     	
     	Label lblEl = new Label(grpJoystick, SWT.NONE);
     	lblEl.setBounds(77, 199, 15, 13);
     	lblEl.setText("El");
     	
     	Button btnSetMaxRel = new Button(grpJoystick, SWT.NONE);
     	btnSetMaxRel.setBounds(135, 193, 68, 23);
     	btnSetMaxRel.setText("Set Values");
     	btnSetMaxRel.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			double maxMoveRelAz = Double.parseDouble(txtMaxRelAz.getText());
     			double maxMoveRelEl = Double.parseDouble(txtMaxRelEl.getText());
     			stage.setMaxMoveRel(maxMoveRelAz, maxMoveRelEl);
     		}
     	});
 	}
 	
 	private void guiRaDec() {
     	Group grpRaDec = new Group(area, SWT.NONE);
     	grpRaDec.setText("RA / Dec Tracking");
     	grpRaDec.setBounds(497, 394, 217, 108);
     	
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
 	}
 	
 	private void guiAxisControl() {
     	Group grpAxisControl = new Group(area, SWT.NONE);
     	grpAxisControl.setText("Axis Control");
     	grpAxisControl.setBounds(10, 245, 274, 478);
     	
     	txtMinEl = new Text(grpAxisControl, SWT.BORDER);
     	txtMinEl.setBounds(215, 42, 49, 19);
     	txtMinEl.setText(String.valueOf(minEl));
     	
     	txtMaxEl = new Text(grpAxisControl, SWT.BORDER);
     	txtMaxEl.setBounds(215, 67, 49, 19);
     	txtMaxEl.setText(String.valueOf(maxEl));
     	
     	txtMaxAz = new Text(grpAxisControl, SWT.BORDER);
     	txtMaxAz.setBounds(110, 67, 49, 19);
     	txtMaxAz.setText(String.valueOf(maxAz));
     	
     	txtMinAz = new Text(grpAxisControl, SWT.BORDER);
     	txtMinAz.setBounds(110, 42, 49, 19);
     	txtMinAz.setText(String.valueOf(minAz));
     	
     	btnSetMinMaxEl = new Button(grpAxisControl, SWT.NONE);
     	btnSetMinMaxEl.setBounds(181, 92, 83, 23);
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
     	
     	btnSetMinMaxAz = new Button(grpAxisControl, SWT.NONE);
     	btnSetMinMaxAz.setBounds(94, 92, 83, 23);
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
     	
     	txtAccAz = new Text(grpAxisControl, SWT.BORDER);
     	txtAccAz.setBounds(110, 146, 60, 19);
     	
     	txtVelAz = new Text(grpAxisControl, SWT.BORDER);
     	txtVelAz.setBounds(110, 121, 60, 19);
     	
     	txtVelEl = new Text(grpAxisControl, SWT.BORDER);
     	txtVelEl.setBounds(204, 121, 60, 19);
     	
     	txtAccEl = new Text(grpAxisControl, SWT.BORDER);
     	txtAccEl.setBounds(204, 146, 60, 19);
     	
     	btnSetMaxVelAccAz = new Button(grpAxisControl, SWT.NONE);
     	btnSetMaxVelAccAz.setBounds(52, 171, 104, 23);
     	btnSetMaxVelAccAz.setText("Set Max Vel/Acc Az");
     	btnSetMaxVelAccAz.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			try {
     				double velAz = Double.parseDouble(txtVelAz.getText());
     				double accAz = Double.parseDouble(txtAccAz.getText());
     				stage.setMaxVelAccAz(velAz, accAz);
     			} catch (NumberFormatException e1) {
     				txtStatusArea.append("Must input a number.\n");
     			}
     		}
     	});
     	
     	btnSetMaxVelAccEl = new Button(grpAxisControl, SWT.NONE);
     	btnSetMaxVelAccEl.setBounds(162, 171, 102, 23);
     	btnSetMaxVelAccEl.setText("Set Max Vel/Acc El");
     	btnSetMaxVelAccEl.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			try {
     				double velEl = Double.parseDouble(txtVelEl.getText());
     				double accEl = Double.parseDouble(txtAccEl.getText());
     				stage.setMaxVelAccEl(velEl, accEl);
     			} catch (NumberFormatException e1) {
     				txtStatusArea.append("Must input a number.\n");
     			}
     		}
     	});
     	
     	Label lblAzimuth = new Label(grpAxisControl, SWT.NONE);
     	lblAzimuth.setBounds(110, 23, 49, 13);
     	lblAzimuth.setText("Azimuth");
     	
     	Label lblElevation = new Label(grpAxisControl, SWT.NONE);
     	lblElevation.setBounds(215, 23, 49, 13);
     	lblElevation.setText("Elevation");
     	
     	Label lblDescription = new Label(grpAxisControl, SWT.NONE);
     	lblDescription.setBounds(10, 23, 60, 13);
     	lblDescription.setText("Description");
     	
     	Label lblMinAngle = new Label(grpAxisControl, SWT.NONE);
     	lblMinAngle.setBounds(10, 42, 49, 13);
     	lblMinAngle.setText("Min Angle");
     	
     	Label lblMaxAngle = new Label(grpAxisControl, SWT.NONE);
     	lblMaxAngle.setBounds(10, 67, 49, 13);
     	lblMaxAngle.setText("Max Angle");
     	
     	Label lblMaxVelocity = new Label(grpAxisControl, SWT.NONE);
     	lblMaxVelocity.setBounds(10, 121, 72, 13);
     	lblMaxVelocity.setText("Max Velocity");
     	
     	Label lblMaxAcceleration = new Label(grpAxisControl, SWT.NONE);
     	lblMaxAcceleration.setBounds(10, 146, 83, 13);
     	lblMaxAcceleration.setText("Max Acceleration");
     	
     	indexAz = new Button(grpAxisControl, SWT.PUSH | SWT.CENTER);
     	indexAz.setBounds(110, 235, 65, 25);
     	indexAz.setText("Index Az");
     	popupWindowButtons.put("indexAz", indexAz);
     	indexAz.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			indexAz.setEnabled(false);
     			//indexEl.setEnabled(false);
     			stage.index(Axis.AZ);
     		}
     	});
     	
     	indexEl = new Button(grpAxisControl, SWT.PUSH | SWT.CENTER);
     	indexEl.setBounds(199, 235, 65, 25);
     	indexEl.setText("Index El");
     	popupWindowButtons.put("indexEl", indexEl);
     	indexEl.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			//indexAz.setEnabled(false);
     			indexEl.setEnabled(false);
     			stage.index(Axis.EL);
     		}
     	});
     	
     	btnMotorAz = new Button(grpAxisControl, SWT.NONE);
     	btnMotorAz.setBounds(110, 204, 65, 25);
     	btnMotorAz.setText(motorOff);
     	btnMotorAz.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			if (btnMotorAz.getText().equals(motorOff)) {
     				btnMotorAz.setText(motorOn);
     			}
     			else {
     				btnMotorAz.setText(motorOff);
     			}
     			stage.motorToggle(Axis.AZ);
     		}
     	});
     	
     	btnMotorEl = new Button(grpAxisControl, SWT.NONE);
     	btnMotorEl.setBounds(199, 204, 65, 25);
     	btnMotorEl.setText(motorOff);
     	btnMotorEl.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			if (btnMotorEl.getText().equals(motorOff)) {
     				btnMotorEl.setText(motorOn);
     			}
     			else {
     				btnMotorEl.setText(motorOff);
     			}
     			stage.motorToggle(Axis.EL);
     		}
     	});
     	
     	btnStopAz = new Button(grpAxisControl, SWT.NONE);
     	btnStopAz.setBounds(110, 266, 65, 25);
     	btnStopAz.setText("Stop Az");
     	btnStopAz.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			stage.stop(Axis.EL);
     		}
     	});
     	
     	btnStopEl = new Button(grpAxisControl, SWT.NONE);
     	btnStopEl.setBounds(199, 266, 65, 25);
     	btnStopEl.setText("Stop El");
     	btnStopEl.addMouseListener(new MouseAdapter() {
     		public void mouseDown(MouseEvent evt) {
     			stage.stop(Axis.EL);
     		}
     	});
     	
     	txtAzRpm = new Text(grpAxisControl, SWT.BORDER);
     	txtAzRpm.setBounds(110, 297, 60, 19);
     	
     	txtElRpm = new Text(grpAxisControl, SWT.BORDER);
     	txtElRpm.setBounds(204, 297, 60, 19);
     	
     	Button btnSetRpm = new Button(grpAxisControl, SWT.NONE);
     	btnSetRpm.setBounds(14, 293, 68, 23);
     	btnSetRpm.setText("Set RPM");
     	btnSetRpm.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			double azRpm = Double.parseDouble(txtAzRpm.getText());
     			double elRpm = Double.parseDouble(txtElRpm.getText());
     			stage.setRpm(azRpm, elRpm);
     		}
     	});
     	
 
 	}
 	
 	private void guiScanning() {
     	Group grpScanning = new Group(area, SWT.NONE);
     	grpScanning.setText("Scanning");
     	grpScanning.setBounds(497, 508, 217, 215);
     	
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
     	
     	Label lblTimeScan = new Label(grpScanning, SWT.NONE);
     	lblTimeScan.setBounds(13, 66, 40, 19);
     	lblTimeScan.setText("time");
     	
     	
     	
     	txtMinAzScan = new Text(grpScanning, SWT.BORDER);
     	txtMinAzScan.setBounds(59, 16, 43, 19);
     	txtMinAzScan.setText("0");
     	scanAzTexts.add(txtMinAzScan);
     	
     	
     	txtMaxAzScan = new Text(grpScanning, SWT.BORDER);
     	txtMaxAzScan.setBounds(59, 41, 43, 19);
     	txtMaxAzScan.setText("0");
     	scanAzTexts.add(txtMaxAzScan);
     	
     	
     	txtTimeScan = new Text(grpScanning, SWT.BORDER);
     	txtTimeScan.setBounds(59, 66, 43, 19);
     	txtTimeScan.setText("0");
     	scanAzTexts.add(txtTimeScan);
     	
     	txtMinElScan = new Text(grpScanning, SWT.BORDER);
     	txtMinElScan.setBounds(154, 16, 43, 19);
     	txtMinElScan.setText("0");
     	scanElTexts.add(txtMinElScan);
     	
     	txtMaxElScan = new Text(grpScanning, SWT.BORDER);
     	txtMaxElScan.setBounds(154, 41, 43, 19);
     	txtMaxElScan.setText("0");
     	scanElTexts.add(txtMaxElScan);
     	
     
     	
     	Label lblRepetitionsScan = new Label(grpScanning, SWT.NONE);
     	lblRepetitionsScan.setBounds(108, 66, 40, 19);
     	lblRepetitionsScan.setText("reps");
     	
     	txtRepScan = new Text(grpScanning, SWT.BORDER);
     	txtRepScan.setBounds(154, 66, 43, 19);
     	txtRepScan.setText("0");
     	scanAzTexts.add(txtRepScan);
     	scanElTexts.add(txtRepScan);
     	
     	
     	btnScanAz = new Button(grpScanning, SWT.RADIO);
     	btnScanAz.setBounds(3, 100, 68, 23);
     	btnScanAz.setText("Azimuth");
     	btnScanAz.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			scanType = "Azimuth";
     			ScanCommand azSc = new ScanCommand(Double.parseDouble(txtMinAzScan.getText()), Double.parseDouble(txtMaxAzScan.getText()));
     			setTime(stage.getScanTime(azSc, Axis.AZ));
     			
     		}
     	});
     	
     	
     	
     	btnScanEl = new Button(grpScanning, SWT.RADIO);
     	btnScanEl.setBounds(120, 100, 68, 23);
     	btnScanEl.setText("Elevation");
     
     	btnScanEl.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			scanType = "Elevation";
     
     			ScanCommand elSc = new ScanCommand(Double.parseDouble(txtMinElScan.getText()), Double.parseDouble(txtMaxElScan.getText()));
     			setTime(stage.getScanTime(elSc, Axis.EL));
     			
     		}
     	});
     	
     	btnScanSnake = new Button(grpScanning, SWT.RADIO);
     	btnScanSnake.setBounds(120, 120, 68, 23);
     	btnScanSnake.setText("Snake");
     	btnScanSnake.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			scanType="Snake";
     			ScanCommand azSc = new ScanCommand(Double.parseDouble(txtMinAzScan.getText()), Double.parseDouble(txtMaxAzScan.getText()));
     			setTime(lines*stage.getScanTime(azSc, Axis.AZ));
     		}
     	});
     	
     	btnScanSquare = new Button(grpScanning, SWT.RADIO);
     	btnScanSquare.setBounds(3, 120, 68, 23);
     	btnScanSquare.setText("Square");
     	popupWindowButtons.put("raster", btnScanSquare);
     	btnScanSquare.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			scanType="Square";
     			
     			ScanCommand azSc = new ScanCommand(Double.parseDouble(txtMinAzScan.getText()), Double.parseDouble(txtMaxAzScan.getText()));
     			setTime(2*lines*stage.getScanTime(azSc, Axis.AZ));
     			
     		}
     			
     	});
     	
     	btnContinuousMode = new Button(grpScanning, SWT.CHECK);
     	btnContinuousMode.setBounds(3, 150, 100, 24);
     	btnContinuousMode.setText("Continuous");
     	btnContinuousMode.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			
     			continuousModeOn = !continuousModeOn;
     			stage.setContinousScanOn(continuousModeOn);
     			
     		}
     	});
     	btnRaDecMode = new Button(grpScanning, SWT.CHECK);
     	btnRaDecMode.setBounds(120, 150, 100, 24);
     	btnRaDecMode.setText("RaDec");
     	btnRaDecMode.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			RaDecModeOn = !RaDecModeOn;
     			
     			stage.setRaOn(RaDecModeOn);
     			
     		}
     	});
     	
     	//TODO
     	btnScan = new Button(grpScanning, SWT.NONE);
     	btnScan.setBounds(3, 175, 68, 23);
     	btnScan.setText("Scan");
     	popupWindowButtons.put("raster", btnScan);
     	btnScan.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			if (btnScan.getText().equals("stop")){
     				
     				stage.stopScanning();
     				btnScan.setText("Scan");
     				return;
     			}
     				
     			btnScan.setText("stop");
     			switch (scanType){
     			case "Azimuth":
     				azScan();
     				break;
     			case "Elevation":
     				
     				elScan();
     				break;
     			case "Snake":
     				snakeScan();
     				break;
     			case "Square":
     				squareScan();
     				break;
     			default:
     				btnScan.setEnabled(false);
     				
     				
     			}
     			
     		}
     	});
 	
     	
     	
 	}
 
 	private void guiDebug() {
     	grpDebug = new Group(area, SWT.NONE);
     	grpDebug.setText("Debug");
     	grpDebug.setBounds(290, 590, 201, 133);
     	
     	Button btnQueueSize = new Button(grpDebug, SWT.NONE);
     	btnQueueSize.setBounds(0, 11, 57, 23);
     	btnQueueSize.setText("queue size");
     	btnQueueSize.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.queueSize();
     		}
     	});
     	
     	btnStop = new Button(grpDebug, SWT.NONE);
     	btnStop.setBounds(48, 94, 42, 23);
     	btnStop.setText("Stop");
     	btnStop.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("ST" + debugAxis.getAbbrev());
     		}
     	});
     	
     	btnBegin = new Button(grpDebug, SWT.NONE);
     	btnBegin.setBounds(0, 94, 42, 23);
     	btnBegin.setText("Begin");
     	btnBegin.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("BG" + debugAxis.getAbbrev());
     		}
     	});
     	
     	btnMotorOff = new Button(grpDebug, SWT.NONE);
     	btnMotorOff.setBounds(61, 41, 56, 23);
     	btnMotorOff.setText("Motor Off");
     	btnMotorOff.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("MO" + debugAxis.getAbbrev());
     		}
     	});
     	
     	
     	btnMotorOn = new Button(grpDebug, SWT.NONE);
     	btnMotorOn.setBounds(61, 11, 56, 23);
     	btnMotorOn.setText("Motor On");
     	btnMotorOn.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.sendCommand("SH" + debugAxis.getAbbrev());
     		}
     	});
     	
     	Button btnReadQueue = new Button(grpDebug, SWT.NONE);
     	btnReadQueue.setBounds(0, 40, 57, 23);
     	btnReadQueue.setText("read queue");
     	btnReadQueue.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.readQueue();
     		}
     	});
     	
     	btnDebugAz = new Button(grpDebug, SWT.RADIO);
     	btnDebugAz.setSelection(true);
     	btnDebugAz.setBounds(123, 11, 59, 16);
     	btnDebugAz.setText("Azimuth");
     	btnDebugAz.addSelectionListener(new SelectionAdapter() {
     		@Override
     		public void widgetSelected(SelectionEvent e) {
     			debugAxis = Axis.AZ;
     		}
     	});
     	
     	btnDebugEl = new Button(grpDebug, SWT.RADIO);
     	btnDebugEl.setBounds(123, 32, 68, 16);
     	btnDebugEl.setText("Elevation");
     	btnDebugEl.addSelectionListener(new SelectionAdapter() {
     		@Override
     		public void widgetSelected(SelectionEvent e) {
     			debugAxis = Axis.EL;
     		}
     	});
     	
     	txtDebugVel = new Text(grpDebug, SWT.BORDER);
     	txtDebugVel.setText("vel");
     	txtDebugVel.setBounds(0, 69, 76, 19);
     	
     	Button btnSetVelocity = new Button(grpDebug, SWT.NONE);
     	btnSetVelocity.setBounds(81, 65, 68, 23);
     	btnSetVelocity.setText("Set Velocity");
     	
     	btnReaderControl = new Button(grpDebug, SWT.NONE);
     	btnReaderControl.setBounds(96, 94, 68, 23);
     	btnReaderControl.setText("Start Reader");
     	btnReaderControl.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			stage.toggleReader();
     			if (btnReaderControl.getText().equals("Start Reader")) {
     				btnReaderControl.setText("Stop Reader");
     			}
     			else if (btnReaderControl.getText().equals("Stop Reader")) {
     				btnReaderControl.setText("Start Reader");
     			}
     			//btnReaderControl.setVisible(false);
     		}
     	});
     	btnSetVelocity.addMouseListener(new MouseAdapter() {
     		@Override
     		public void mouseDown(MouseEvent e) {
     			
     			double vel = 0;
     			try {
     				vel = Double.parseDouble(txtDebugVel.getText());
     			} catch (NumberFormatException f) {
     				f.printStackTrace();
     			}
     			//String out = "JG" + debugAxis + "=" + vel;
     			//stage.sendCommand(out);
     			stage.setVelocity(vel, debugAxis);
     		}
     	});
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
 		final Button btn = popupWindowButtons.get(name);
 		if (btn == null) {
 			return;
 		}
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				btn.setEnabled(true);
 			}
 		});
 	}
 	
 	public void controlMoveButtons(final boolean trueFalse) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				azPlus.setEnabled(trueFalse);
 				azMinus.setEnabled(trueFalse);
 				elPlus.setEnabled(trueFalse);
 				elMinus.setEnabled(trueFalse);
 			}
 		});
 	}
 	
 
 	public void enableScanButtons() {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				btnScan.setText("Scan");
 			}
 		});
 		
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
 	
 	public void setMinMaxAzEl(final double minAz, final double maxAz, final double minEl, final double maxEl) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				;
 				txtMinAz.setText(String.valueOf(minAz));
 				txtMaxAz.setText(String.valueOf(maxAz));
 				txtMinEl.setText(String.valueOf(minEl));
 				txtMaxEl.setText(String.valueOf(maxEl));
 				stage.loadSafty(minAz, maxAz, minEl, maxEl);
 				
 	
 			}
 		});
 	}
 	
 	public void setVelAccAzEl(final double velAz, final double accAz, final double velEl, final double accEl) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtVelAz.setText(String.valueOf(velAz));
 				txtAccAz.setText(String.valueOf(accAz));
 				txtVelEl.setText(String.valueOf(velEl));
 				txtAccEl.setText(String.valueOf(accEl));
 			}
 		});
 	}
 	
 	public void setMaxMoveRel(final double maxMoveRelAz, final double maxMoveRelEl) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtMaxRelAz.setText(String.valueOf(maxMoveRelAz));
 				txtMaxRelEl.setText(String.valueOf(maxMoveRelEl));
 			}
 		});
 	}
 	
 	public void setGoalPos(final String goalDeg, final Axis axis) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				switch (axis) {
 					case AZ:
 						txtGoalAz.setText(goalDeg); break;
 					case EL:
 						txtGoalEl.setText(goalDeg); break;
 					default:
 						System.out.println("error MainWindow.setGoalPos");
 				}
 			}
 		});
 	}
 	
 	public void setScanEnabled(final Axis type){
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				enableScanButtons();
 				switch (type) {
 					case AZ:
 						btnScanAz.setText("Scan Az" ); break;
 					case EL:
 						btnScanEl.setText("Scan El"); break;
 //					case BOTH:
 //						btnScanBoth.setText("Scan Both"); break;
 					default:
 						System.out.println("MainWindow.setScanEnabled actually encountered the type BOTH");
 				}
 			}
 		});
 	}
 	
 	public void setRaDec(final double ra, final double dec) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtRa.setText(Formatters.TWO_POINTS.format(ra));
 				txtDec.setText(Formatters.TWO_POINTS.format(dec));
 				btnRaDecOn.setEnabled(true);
 			}
 		});
 	}
 	
 	public void setTime(final double time){
 		Display.getDefault().asyncExec(new Runnable(){
 			@Override
 			public void run(){
 				txtTimeScan.setText(""+time);			}
 		});
 	}
 	
 	public void updateStatusArea(final String message) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				txtStatusArea.append(message);
 			}
 		});
 	}
 	
 	public boolean isDouble(String input) {
 		try {
 			Double.parseDouble(input);
 			return true;
 		} catch (NumberFormatException e) {
 			return false;
 		}
 	}
 	
 	private boolean validateScanInput(List<Text> texts) {
 		for (Text t : texts) {
 			if (!isDouble(t.getText())) {
 				updateStatusArea("This is not a valid number.\n");
 				t.setFocus();
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void updateMotorButton(final boolean onOff, final Axis axis) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				String text = motorOn;
 				if (onOff) {
 					text = motorOff;
 				}
 				switch (axis) {
 					case AZ:
 						btnMotorAz.setText(text); break;
 					case EL:
 						btnMotorEl.setText(text); break;
 				}
 			}
 		});
 	}
 	public void updateReps(final double reps){
 		Display.getDefault().asyncExec(new Runnable(){
 			@Override
 			public void run(){
				txtRepScan.setText(""+reps);
 
 			}
 		});
 		
 	}
 	public void updateScriptArea(final String type, final Set<String> scripts) {
 		org.eclipse.swt.widgets.List temp = null;
 		switch (type) {
 			case "expected":
 				temp = expectedScripts;
 				break;
 			case "loaded":
 				temp = loadedScripts;
 				break;
 			default:
 				assert false; //This should never be reached.
 		}
 		final org.eclipse.swt.widgets.List temp2 = temp;
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				temp2.removeAll();
 				for (String s : scripts) {
 					temp2.add(s);
 				}
 			}
 		});
 	}
 	
 	public void raDecTrackingButtonUpdater(final boolean on, final boolean off) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				btnRaDecOn.setEnabled(on);
 				btnRaDecOff.setEnabled(off);
 			}
 		});
 	}
 	
 
 	public void azScan(){
 			if (!validateScanInput(scanAzTexts)) {
 				return;
 			}
 				
 			double min = Double.parseDouble(txtMinAzScan.getText());
 			double max = Double.parseDouble(txtMaxAzScan.getText());
 			double time = Double.parseDouble(txtTimeScan.getText());
 			int reps = Integer.parseInt(txtRepScan.getText());
 			//stage.startScanning(min, max, time, reps, axisType.AZ, continuousScanOn);
 			
 			ScanCommand azSc = new ScanCommand(min, max, time, reps);
 			stage.startScanning(azSc, null,false);
 			
 		
 		
 	}
 	
 	public void elScan(){
 	
 			if (!validateScanInput(scanElTexts)) {
 				return;
 			}
 			
 			
 			
 			double min = Double.parseDouble(txtMinElScan.getText());
 			double max = Double.parseDouble(txtMaxElScan.getText());
 			double time = Double.parseDouble(txtTimeScan.getText());
 			int reps = Integer.parseInt(txtRepScan.getText());
 			//stage.startScanning(min, max, time, reps, axisType.EL,continuousScanOn);
 			
 		
 			ScanCommand elSc = new ScanCommand(min, max, time, reps);
 			stage.startScanning(null, elSc, false);
 			
 			
 		
 	
 	}
 	
 	public void snakeScan(){
 
 			if (!validateScanInput(scanAzTexts) && !validateScanInput(scanElTexts)) {
 				return;
 			}
 			
 			double minAz = Double.parseDouble(txtMinAzScan.getText());
 			double maxAz = Double.parseDouble(txtMaxAzScan.getText());
 			double timeAz = Double.parseDouble(txtTimeScan.getText());
 			double minEl = Double.parseDouble(txtMinElScan.getText());
 			double maxEl = Double.parseDouble(txtMaxElScan.getText());
 			double timeEl = Double.parseDouble(txtTimeScan.getText());
 			int reps = Integer.parseInt(txtRepScan.getText());
 			
 			ScanCommand azSc = new ScanCommand(minAz, maxAz, timeAz, reps);
 			ScanCommand elSc = new ScanCommand(minEl, maxEl, timeEl, reps);
 			stage.startScanning(azSc, elSc, false);
 			
 			
 		
 		
 	}
 	public void squareScan(){
 		double minAz = Double.parseDouble(txtMinAzScan.getText());
 		double maxAz = Double.parseDouble(txtMaxAzScan.getText());
 		double timeAz = Double.parseDouble(txtTimeScan.getText());
 		double minEl = Double.parseDouble(txtMinElScan.getText());
 		double maxEl = Double.parseDouble(txtMaxElScan.getText());
 		
 		int reps = Integer.parseInt(txtRepScan.getText());
 
 		ScanCommand azSc = new ScanCommand(minAz, maxAz, timeAz, reps);
 		ScanCommand elSc = new ScanCommand(minEl, maxEl, timeAz, reps);
 		
 		rasterScan = !rasterScan;
 		stage.startScanning(azSc, elSc, true);
 		
 	
 	}
 	
 }
 
