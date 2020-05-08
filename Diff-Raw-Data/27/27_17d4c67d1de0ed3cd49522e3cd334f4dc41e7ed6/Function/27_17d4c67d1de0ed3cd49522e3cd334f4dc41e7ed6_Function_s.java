 package org.ndaguan.micromanager.mmtracker;
 
 import ij.ImagePlus;
 import ij.WindowManager;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.SwingUtilities;
 
 import mmcorej.CMMCore;
 
 import org.micromanager.MMStudioMainFrame;
 import org.micromanager.utils.MMScriptException;
 
 public class Function {
 
 	private static Function instance_;
 	private MMStudioMainFrame gui_;
 	private CMMCore core_;
 
 	private double currxpos_;
 	private double currypos_;
 	private double currzpos_;
 
 	public static boolean isCalibrationRunning = false;
 	public static boolean isInstalCallback;
 	public static boolean isCapture_ = false;
 	public static boolean isTestingRunning_ = false;
 
 	private boolean isXYAcqAnalyzerInstall_  = false;
 	private boolean isXYZAcqAnalyzerInstall_  = false;
 	private boolean isCalibrateAnalyzerInstall_ = false;
 	private boolean isTestingAnalyzerInstall_ = false;
 
 	private List<RoiItem> roiList_;
 	private Kernel kernel_;
 	private boolean buttonIsLocked_ = true;
 
 	public void clearROI(){
 		Kernel.getInstance().roiList_.clear();
 	}
 	public Function(MMStudioMainFrame gui, List<RoiItem> roiList) {
 		core_ = gui.getMMCore();
 		gui_ = gui;
 		roiList_ = roiList;
 		kernel_ = Kernel.getInstance();
 
 	}
 	public Function(List<RoiItem> roiList) {
 		roiList_ = roiList;
 		kernel_ = Kernel.getInstance();
 
 	}
 
 	public static Function getInstance(MMStudioMainFrame gui,List<RoiItem> roiList) {
 		if(instance_ == null)
 			instance_ = new Function(gui,roiList);
 
 		return instance_;
 	}
 	public static Function getInstance(){
 		return instance_;
 	}
 
 	void dataReset(){
 		if(Listener.getInstance().isRunning())
 			Listener.getInstance().stop();
 		for(RoiItem it:roiList_)
 			it.dataClean(false);
 	}
 
 	public void PullMagnet() {
 		(new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					double currMP285zpos = core_.getPosition(MMT.magnetZStage_);
 					double z = MMT.VariablesNUPD.magnetStepSize.value();
 					double target =  currMP285zpos - z;
 					if(target>0){
 						target = 0;
 						MMTFrame.getInstance().MagnetManual.setSelected(true);
 					}
 					core_.setPosition(MMT.magnetZStage_,target);
 					MMT.logMessage(String.format("\tSet MP285 ZStage\t\t%f\t->\t%f",
 							currMP285zpos , core_.getPosition(MMT.magnetZStage_)));
 				} catch (Exception e) {
 					MMT.logError("Set MP285 ZStage ERR" + e.toString());
 				}
 			}
 		})).start();
 	}
 
 	public int getFocusRoiIndex() {
 		int index =0;
 		for(RoiItem it:roiList_){			
 			if(it.isFocus())
 				return index;
 			else
 				index++;		
 		}
 		return -1;
 	}
 
 	public boolean overlapCheck(int xCenter,int yCenter) {
 		for(RoiItem it:roiList_){	
 			double[] xy = it.getXY();
 			double dx = xCenter - xy[0];
 			double dy = yCenter - xy[1];
 			double r = Math.sqrt(dx*dx + dy*dy);			
 			if(r <2 * (int)MMT.VariablesNUPD.beanRadiuPixel.value()){
 				return true;
 			}		
 		}
 		return false;
 	}
 
 	public int getCurrentRoiIndex(Point loc) {
 		int index =0;
 		int beanRadius = (int) MMT.VariablesNUPD.beanRadiuPixel.value();
 		for(RoiItem it:roiList_){	
 			double[] xy = it.getXY();
 			Rectangle roi = new Rectangle((int) xy[0]-beanRadius,(int) xy[1]-beanRadius,2*beanRadius,2*beanRadius);
 			if(roi.contains(loc))
 				return index;
 			else
 				index++;		
 		}
 		return -1;
 	}
 
 	public int getReferenceRoiIndex() {
 		int index =0;
 		for(RoiItem it:roiList_){			
 			if(it.isSelected())
 				return index;
 			else
 				index++;		
 		}
 		return -1;
 	}
 
 	public void reSetFocusRoi() {
 		ImagePlus ip = WindowManager.getCurrentImage();
 		final int focus = getFocusRoiIndex();
 		if(focus != -1){
 			double[] xy = roiList_.get(focus).getXY();
 			int xCenter = (int) xy[0];	
 			int yCenter = (int) xy[1];	
 			int beanRadius = (int) MMT.VariablesNUPD.beanRadiuPixel.value();
 			if(ip != null)
 				ip.setRoi(xCenter-beanRadius,yCenter-beanRadius,2*beanRadius,2*beanRadius);					
 		} 
 	}
 
 	public void setFocusdRoi(Point point) {
 		int focus = getFocusRoiIndex();
 		if(focus != -1){
 			roiList_.get(focus).setFocus(false);
 		}
 
 		int index = getCurrentRoiIndex(point);
 		if( index != -1){
 			roiList_.get(index).setFocus(true);
 		}
 	}
 
 	public void moveFocusdRoi(int dx, int dy) {
 		int index = getFocusRoiIndex();
 		if( index != -1){
 			double[] xy = roiList_.get(index).getXY();
 			roiList_.get(index).setXY(xy[0]+dx,xy[1]+dy);
 		}	
 	}
 
 	public void addRoi(Rectangle rectangle) {
 		int xCenter = rectangle.x+rectangle.width/2;
 		int yCenter = rectangle.y+rectangle.height/2;
 		if(kernel_.isCalibrated_)
 			return;
 		if(!overlapCheck(xCenter,yCenter))//selected area exist ROI
 		{	
 			int index = getFocusRoiIndex();
 			if(index != -1)
 			{
 				roiList_.get(index).setFocus(false);
 			}
 			synchronized(MMT.Acqlock){
 				roiList_.add(RoiItem.createInstance(new double[]{xCenter,yCenter,0,0,0,},GetXYPositionAnalyzer.getInstance().acqName_));
 			}
 		}
 	}
 
 	public void deleteRoi() {
 		int index = getFocusRoiIndex();
 		if( index != -1){
 			synchronized(MMT.Acqlock){
 				roiList_.remove(index);
 			}
 			if(kernel_.isCalibrated_ && roiList_.size() == 0){
 				kernel_.setIsCalibrated(false);
 			}
 		}	
 	}
 
 	public void selectRoiAsReference() {
 		int index = getFocusRoiIndex();
 		if( index != -1){
 			int preReferenceIndex = getReferenceRoiIndex();
 			if(preReferenceIndex != -1){
 				roiList_.get(preReferenceIndex).setSelect(false);
 			}
 			roiList_.get(index).setSelect(true);
 		}
 	}
 
 	public void showChartManager() {
 		int focus = getFocusRoiIndex();
 		if(focus != -1){
 			roiList_.get(focus).setChartVisible(true);
 		}
 
 	}
 
 	public void reDraw(final String acqName, final long frameNum_, final boolean update) {
 		if(frameNum_ %MMT.VariablesNUPD.frameToRefreshImage.value() !=0)
 			return;
 		SwingUtilities.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				reSetFocusRoi();
 				try {
 					OverlayRender.getInstance().render(acqName, roiList_, frameNum_, update);
 				} catch (MMScriptException e) {
 					MMT.logError("Render error!");
 				}
 			}
 		});
 
 	}
 
 	public void showPreferencesDialog() {
 		MMTFrame.getInstance().preferDailog.UpdateData(false);
 		MMTFrame.getInstance().preferDailog.setVisible(true);
 	}
 	public void installAnalyzer(String analyzer){
 
 		UninstallAnalyzer("CAL");
 		UninstallAnalyzer("TEST");
 		UninstallAnalyzer("XYACQ");
 		UninstallAnalyzer("XYZACQ");
 
 		switch(analyzer){
 		case "XYACQ":
 			gui_.getAcquisitionEngine().addImageProcessor(GetXYPositionAnalyzer.getInstance());
 			isXYAcqAnalyzerInstall_ = true;
 			break;
 		case "XYZACQ":
 			gui_.getAcquisitionEngine().addImageProcessor(GetXYZPositionAnalyzer.getInstance());
 			isXYZAcqAnalyzerInstall_ = true;
 			MMTFrame.getInstance().LiveView.setText("StopCapture");
 			MMT.logMessage("XTZ Call back install,Start capture");
 			break;
 		case "CAL":
 			gui_.getAcquisitionEngine().addImageProcessor(CalibrateAnalyzer.getInstance());
 			isCalibrateAnalyzerInstall_ = true;
 			break;
 		case "TEST":
 			gui_.getAcquisitionEngine().addImageProcessor(TestAnalyzer.getInstance());
 			isTestingAnalyzerInstall_ = true;
 			break;
 		}
 	}
 	private void UninstallAnalyzer(String analyzer){
 		switch(analyzer){
 		case "XYACQ":
 			if (isXYAcqAnalyzerInstall_) {
 				gui_.getAcquisitionEngine().removeImageProcessor(GetXYPositionAnalyzer.getInstance());
 				isXYAcqAnalyzerInstall_ = false;
 				MMTFrame.getInstance().LiveView.setText("LiveView");
 				MMT.logMessage("XYCall back uninstal,Stop capture");
 			} 
 			break;
 		case "XYZACQ":
 			if (isXYZAcqAnalyzerInstall_) {
 				gui_.getAcquisitionEngine().removeImageProcessor(GetXYZPositionAnalyzer.getInstance());
 				isXYZAcqAnalyzerInstall_ = false;
 				MMTFrame.getInstance().LiveView.setText("LiveView");
 				MMT.logMessage("XYZCall back uninstal,Stop capture");
 			} 
 			break;
 		case "CAL":
 			if (isCalibrateAnalyzerInstall_) {
 				gui_.getAcquisitionEngine().removeImageProcessor(CalibrateAnalyzer.getInstance());
 				isCalibrateAnalyzerInstall_ = false;
 			} 
 			break;
 		case "TEST":
 			if (isTestingAnalyzerInstall_) {
 				gui_.getAcquisitionEngine().removeImageProcessor(TestAnalyzer.getInstance());
 				isTestingAnalyzerInstall_ = false;
 			} 
 			break;
 		}
 	}
 
 	public void multiAcq() {
 		gui_.getAcqDlg().setVisible(true);
 	}
 
 	public void setXYZCalPosition(int z) throws Exception 
 	{
 		setStageZPosition(kernel_.zPosProfiles[z]);
 		setStageXYPosition(kernel_.xPosProfiles[z],kernel_.yPosProfiles[z]);
 
 	}
 	public void setZCalPosition(int z) throws Exception {
 		setStageZPosition(kernel_.zPosProfiles[z]);		
 	}
 	public double[] getStagePosition() throws Exception
 	{
 		double temp = getStageZPosition();
 		double[] XY = getStageXYPosition();
 		return new double[]{XY[0],XY[1],temp};
 	}
 
 	public void updateCalibrationProfile(){
 		double calRange = MMT.VariablesNUPD.calRange.value();
 		double calStepSize = MMT.VariablesNUPD.calStepSize.value();
 		double testingPrecision = MMT.VariablesNUPD.testingPrecision.value();
 		double[][] cal = new double[ (int) (calRange/calStepSize)][(int) (MMT.VariablesNUPD.beanRadiuPixel.value()/MMT.VariablesNUPD.rInterStep.value())];
 		kernel_.zPosProfiles = new double[ (int) (calRange/calStepSize)];
 		kernel_.zTestingPosProfiles = new double [(int) (calRange/testingPrecision)];
 
 		for (RoiItem it:roiList_) 
 			it.InitializeCalProflie(cal);
 		int calSize = (int) (calRange/calStepSize);
 		try {
 			updatePositions();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		int midPoint = (int) (calRange / 2);
 		for (int i = 0; i < calSize; i++) {
 			kernel_.zPosProfiles[i] = currzpos_ - midPoint + i * calStepSize;
 		}
 		for (int i = 0; i < kernel_.zTestingPosProfiles.length; i++) {
 			kernel_.zTestingPosProfiles[i] = kernel_.zPosProfiles[0] +  (i+ Math.floor(Math.random()*100)/100) * testingPrecision;
 		}
 		midPoint = (int) (calSize / 2);
 		kernel_.xPosProfiles = new double[ (int) (calRange/calStepSize)];
 		kernel_.yPosProfiles = new double[ (int) (calRange/calStepSize)];
 
 		for (int j = 0; j < midPoint; j++){
 			kernel_.xPosProfiles[j] =  currxpos_ + calStepSize * j;
 			kernel_.yPosProfiles[j] =  currypos_ + calStepSize * j;
 		}
 		for (int j = midPoint; j < calSize; j++){
 			kernel_.xPosProfiles[j] = kernel_.xPosProfiles[midPoint - 1] - calStepSize * (j - midPoint);
 			kernel_.yPosProfiles[j] = kernel_.yPosProfiles[midPoint - 1] - calStepSize * (j - midPoint);
 		}
 	}
 	public void CalProfileDataCleanup(){
 		kernel_.xPosProfiles = null;
 		kernel_.yPosProfiles = null;
 		kernel_.zPosProfiles = null;
 		for(RoiItem it:roiList_)
 			it.clearCalProfile();
 	}
 	public void snapImage(){
 		gui_.snapSingleImage();		
 	}
 
 	private void WHATISLOVE(String mode){
 		MMT.isAnalyzerBusy_ = false;
 		switch(mode){
 		case "CalibrateStart":
 			kernel_.isCalibrated_ = false;
 
 			MMT.isCalibrationRunning_ = true;
 			MMT.isTestingRunning_ = false;
 			MMT.isGetXYPositionRunning_ = false;
 			MMT.isGetXYZPositionRunning_ = false;
 			setProgressBarMaxValue(kernel_.zPosProfiles.length);
 			MMTFrame.getInstance().preferDailog.enableEdit(false);
 			MMTFrame.getInstance().setCalibrateIcon(false);
 			MMTFrame.getInstance().setEnableCalibrateIcon(false);
 			MMTFrame.getInstance().setEnableLiveIcon(false);
 			MMTFrame.getInstance().setLiveViewIcon(false);
 			setProgressBarVisible(true);
 			MMT.logMessage("Calibration Start......");
 			break;
 		case "CalibrateTrue":
 			kernel_.setPixelToPhys();
 
 			kernel_.isCalibrated_ = false;
 
 			MMT.isCalibrationRunning_ = false;
 			MMT.isTestingRunning_ = false;
 			MMT.isGetXYPositionRunning_ = false;
 			MMT.isGetXYZPositionRunning_ = false;
 
 			MMTFrame.getInstance().preferDailog.enableEdit(false);
 			MMTFrame.getInstance().setCalibrateIcon(false);
 			MMTFrame.getInstance().setEnableCalibrateIcon(false);
 			setProgressBarVisible(false);
 			MMT.logMessage("Calibration OK......");
 			break;
 		case "CalibrateFalse":
 			kernel_.isCalibrated_ = false;
 			installAnalyzer("XYACQ");
 			MMT.isCalibrationRunning_ = false;
 			MMT.isTestingRunning_ = false;
 			MMT.isGetXYPositionRunning_ = false;
 			MMT.isGetXYZPositionRunning_ = false;
 			MMTFrame.getInstance().preferDailog.enableEdit(true);
 			MMTFrame.getInstance().setCalibrateIcon(false);
 			MMTFrame.getInstance().setEnableCalibrateIcon(true);
 			setProgressBarVisible(false);
 			CalProfileDataCleanup();
 			try {
 				Function.getInstance().StageGoHome();
 			} catch (Exception e1) {
 				MMT.logError("Stage cannot go back home");
 			}
 			MMT.logMessage("Calibration False......");
 			liveView();
 			break;
 		case "TestingStart":
 			kernel_.isCalibrated_ = true;
 
 			MMT.isCalibrationRunning_ = false;
 			MMT.isTestingRunning_ = true;
 			MMT.isGetXYPositionRunning_ = false;
 			MMT.isGetXYZPositionRunning_ = false;
 
 			MMTFrame.getInstance().preferDailog.enableEdit(false);
 			MMTFrame.getInstance().setCalibrateIcon(false);
 			MMTFrame.getInstance().setEnableCalibrateIcon(false);
 			setProgressBarVisible(true);
 			setProgressBarMaxValue(kernel_.zTestingPosProfiles.length);
 			cleanTestingData();
 			roiList_.get(0).setChartVisible(true);
 			roiList_.get(0).setSelectTap("Chart-Testing");
 			MMT.logMessage("Testing Start......");
 			break;
 		case "TestingTrue":
 			kernel_.isCalibrated_ = true;
 
 			MMT.isCalibrationRunning_ = false;
 			MMT.isTestingRunning_ = false;
 			MMT.isGetXYPositionRunning_ = false;
 			MMT.isGetXYZPositionRunning_ = false;
 			MMT.VariablesNUPD.frameToRefreshImage.value(MMT.currentframeToRefreshImage_);
 			MMTFrame.getInstance().preferDailog.UpdateData(false);
 			MMTFrame.getInstance().preferDailog.enableEdit(true);
 			MMTFrame.getInstance().setCalibrateIcon(true);
 			MMTFrame.getInstance().setEnableCalibrateIcon(true);
 			setProgressBarVisible(false);
 			try {
 				Function.getInstance().StageGoHome();
 			} catch (Exception e1) {
 				MMT.logError("Stage cannot go back home");
 			}
 			MMTFrame.getInstance().LiveViewCaptureEnable(true);
 			MMTFrame.getInstance().MultCaptureEnable(true);
 			MMTFrame.getInstance().setEnableLiveIcon(true);
 			MMT.logMessage("Testing OK......");
 			break;
 		case "TestingFalse":
 			kernel_.isCalibrated_ = false;
 			installAnalyzer("XYACQ");
 			MMT.isCalibrationRunning_ = false;
 			MMT.isTestingRunning_ = false;
 			MMT.isGetXYPositionRunning_ = false;
 			MMT.isGetXYZPositionRunning_ = false;
 			MMTFrame.getInstance().preferDailog.enableEdit(true);
 			MMTFrame.getInstance().setCalibrateIcon(false);
 			MMTFrame.getInstance().setEnableCalibrateIcon(true);
 			setProgressBarVisible(false);
 			CalProfileDataCleanup();
 			try {
 				Function.getInstance().StageGoHome();
 			} catch (Exception e1) {
 				MMT.logError("Stage cannot go back home");
 			}
 			MMT.logMessage("Testing False......");
 			liveView();
 			break;
 		}
 	}
 
 	private void testing() {
 		installAnalyzer("TEST");
 		WHATISLOVE("TestingStart");
 		try {
 			setXYZCalPosition(0);
 		} catch (Exception e) {
 			WHATISLOVE("TestingFalse");
 			MMT.logError("testing error1:Set Pi Stage error!\r\n"+e.toString());
 			return;
 		}
 		//testing start
 		for (int i = 0; i <kernel_.zTestingPosProfiles.length; i ++) {
 			if(MMT.isTestingRunning_){
 				try {
 					MMT.testingIndex_ = i;
 					setStageZPosition(kernel_.zTestingPosProfiles[i]);
 					MMT.isAnalyzerBusy_ = true;
 					snapImage();
 					while(MMT.isAnalyzerBusy_){
 						TimeUnit.MICROSECONDS.sleep(10);
 					}
 				} catch (Exception e) {
 					WHATISLOVE("TestingFalse");
 					MMT.logError("Testing error1:Set Pi Stage error!\r\n"+e.toString());
 					return;
 				}
 			}
 			else{
 				WHATISLOVE("TestingFalse"); 
 				MMT.logError(MMT.lastError_);
 				return;
 			}
 			setProgressBarValue(i);
 		}//for end
 		WHATISLOVE("TestingTrue");
 		installAnalyzer("XYACQ");
 		liveView();
 	}
 
 
 	public void calibrate() {
 		if(roiList_ ==null ||roiList_.size() <= 0){
 			MMT.logError("No roi in the image,Try ctrl+A");
 			return;
 		}
 		MMT.xyStage_ = (((int)MMT.VariablesNUPD.hasXYStage.value()) == 1)?core_.getXYStageDevice():null;
 		MMT.zStage_ = (((int)MMT.VariablesNUPD.hasZStage.value()) == 1)?core_.getFocusDevice():null;
 		if( MMT.zStage_ == null){
 			MMT.logError("there is no zstage avilable");
 			return;
 		}
 		stopLiveView();
 		updateCalibrationProfile();
 		installAnalyzer("CAL");
 		WHATISLOVE("CalibrateStart");
 
 		try {
 			setXYZCalPosition(0);
 		} catch (Exception e) {
 			WHATISLOVE("CalibrateFalse");
 			MMT.logError("Calbration error1:Set Pi Stage error!\r\n"+e.toString());
 			return;
 		}
 
 		for(RoiItem it:roiList_)
 			it.setZOrign(currzpos_ - MMT.VariablesNUPD.beanRadius.value());
 		//calibration start
 		for (int i = 0; i < kernel_.zPosProfiles.length; i++) {
 			if(MMT.isCalibrationRunning_){
 				try {
 					Function.getInstance().setXYZCalPosition(i);
 					MMT.calibrateIndex_ = i;
 					MMT.isAnalyzerBusy_ = true;
 					snapImage();
 					while(MMT.isAnalyzerBusy_){
 						TimeUnit.MICROSECONDS.sleep(10);
 					}
 				} catch (Exception e) {
 					WHATISLOVE("CalibrateFalse");
 					MMT.logError("Calbration error1:Set Pi Stage error!\r\n"+e.toString());
 					return;
 				}
 			}
 			else{
 				WHATISLOVE("CalibrateFalse");
 				MMT.logError(MMT.lastError_);
 				return;
 			}
 			setProgressBarValue(i);
 		}//for end
 		WHATISLOVE("CalibrateTrue");
 		testing();
 	}
 	private void stopLiveView() {
 		if (gui_.getAcquisitionEngine().isAcquisitionRunning()) {
 			gui_.getAcquisitionEngine().stop(true);
 		}
 
 		if (gui_.isLiveModeOn()) {
 			gui_.enableLiveMode(false);
 		}		
 	}
 
 	private void setProgressBarVisible(boolean b) {
 		MMTFrame.getInstance().bar.setVisible(b);		
 	}
 	void setProgressBarMaxValue(int i){
 		MMTFrame.getInstance().bar.setMaximum(i);
 	}
 	void setProgressBarValue(int i){
 		final int value = i;
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				MMTFrame.getInstance().bar.setValue(value);
 			}
 		});
 	}
 
 	public void liveView() {
 		if(gui_.isLiveModeOn()){
 			gui_.enableLiveMode(false);
 			gui_.enableLiveMode(true);
 		}else{
 			gui_.enableLiveMode(true);
 		}
 	}
 	public void updateTestingChart(final double currZpos) {
 		SwingUtilities.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				for(int i = 0;i<roiList_.size();i++)
 				{
 					roiList_.get(i).addChartData("Chart-Testing",currZpos,roiList_.get(i).getZ() - currZpos);
 				}
 			}
 		});
 	}
 	public void cleanTestingData() {
 		for(int i = 0;i<roiList_.size();i++)
 		{
 			roiList_.get(i).clearChart("Chart-Testing");
 		}
 	}
 
 	public void updateChart(final long frameNum) {
 		for (RoiItem item:roiList_)
 			item.updateDataSeries(frameNum);
 	}
 
 
 	public void ShowMagnetManualDialBox() {
 		MMTFrame.getInstance().MagnetAuto.setSelected(false);
 		MMTFrame.getInstance().myStageControlFrame_.setVisible(true);
 	}
 	private void whataday() throws Exception{
 		MMT.zStage_ = core_.getFocusDevice();
 		MMT.xyStage_ = core_.getXYStageDevice();
 		double range = MMT.VariablesNUPD.calRange.value();
 		double stepsize = MMT.VariablesNUPD.calStepSize.value();
 		int imageNum = (int) MMT.VariablesNUPD.frameToCalcForce.value();
 		int len = (int) (range/stepsize);
 		updatePositions();
 		final double start = currzpos_ - range/2;
 		for(int k =0;k<(int)MMT.VariablesNUPD.showDebugTime.value();k++){//
 			for(int i=0;i<len;i++){
 				double target = start+i*stepsize;
 				setStageZPosition(target);
 				for (int j = 0; j <imageNum; j++){ 
 					gui_.snapAndAddToImage5D();
 					TimeUnit.MILLISECONDS.sleep((long) MMT.VariablesNUPD.stageMoveSleepTime.value());
 					MMT.logMessage(String.format("currZPos:\t%f(%d/%d)\timageNum:\t%d/%d", target,i,len,j,imageNum));
 				}
 			}
 		}
 		setStageZPosition(currzpos_);
 	}
 	public void setAutoContrast() {
 
 		try {
 			gui_.setContrastBasedOnFrame(GetXYPositionAnalyzer.getInstance().acqName_, 0, 0);
 		} catch (MMScriptException e) {
 			MMT.logError("Set auto contrast false");
 		}		
 	}
 
 	public void showGui() {
 		gui_.setVisible(!gui_.isVisible());
 	}
 
 	public void showIJ() {
 		ij.ImageJ.getFrames()[0].setVisible(!ij.ImageJ.getFrames()[0].isVisible());
 	}
 
 	public void updatePositions() throws Exception  {
 		currxpos_ = getStageXPosition();
 		currypos_ = getStageYPosition();
 		currzpos_ = getStageZPosition();
 	}
 
 	public void StageGoHome() throws Exception  {
 		setStageXYPosition( currxpos_,currypos_);
 		setStageZPosition(currzpos_);
 	}
 
 	public void setStageXPosition(double xpos) throws Exception {
 		if(MMT.xyStage_ != null){
 			core_.setXYPosition(MMT.xyStage_, xpos, core_.getYPosition(MMT.xyStage_));
 			TimeUnit.MILLISECONDS.sleep((long) MMT.VariablesNUPD.stageMoveSleepTime.value());
 		}
 	}
 	public double getStageXPosition() throws Exception{
 		if(MMT.xyStage_ != null)
 			return core_.getXPosition(MMT.xyStage_);
 		else 
 			return 0;
 	}
 	public void setStageYPosition(double ypos) throws Exception {
 		if(MMT.xyStage_ != null){
 			core_.setXYPosition(MMT.xyStage_, core_.getXPosition(MMT.xyStage_), ypos);
 			TimeUnit.MILLISECONDS.sleep((long) MMT.VariablesNUPD.stageMoveSleepTime.value());
 		}
 	}
 	public double getStageYPosition() throws Exception{
 		if(MMT.xyStage_ != null)
 			return core_.getYPosition(MMT.xyStage_);
 		else
 			return 0;
 	}
 	public void setStageXYPosition(double xpos,double ypos) throws Exception {
 		if(MMT.xyStage_ != null){
 			core_.setXYPosition(MMT.xyStage_,xpos, ypos);
 			TimeUnit.MILLISECONDS.sleep((long) MMT.VariablesNUPD.stageMoveSleepTime.value());
 		}
 	}
 	public double[] getStageXYPosition() throws Exception {
 		if(MMT.xyStage_ != null){
 			double[] xpos = new  double[1];
 			double[] ypos = new  double[1];
 			core_.getXYPosition(MMT.xyStage_, xpos, ypos);
 			return new double[]{xpos[0],ypos[0]};
 		}
 		else
 			return new double[]{0,0};
 	}
 
 	public double getStageZPosition() throws  Exception {
 		if(MMT.zStage_ != null)
 			return  core_.getPosition(MMT.zStage_);
 		else 
 			return 0;
 	}
 	public void setStageZPosition(double zPos) throws Exception {
 		if(MMT.zStage_ != null){
 			core_.setPosition(MMT.zStage_, zPos);
 			TimeUnit.MILLISECONDS.sleep((long) MMT.VariablesNUPD.stageMoveSleepTime.value());
 		}
 	}
 	public void liveCapture() {
 		if(isXYAcqAnalyzerInstall_ && kernel_.isCalibrated_){
 			installAnalyzer("XYZACQ");
 			for(RoiItem it:roiList_)
 				it.setSelectTap("Chart-Z");
 			MMTFrame.getInstance().preferDailog.enableEdit(false);
 			MMTFrame.getInstance().setLiveViewIcon(true);
 			for(RoiItem it:roiList_)
 				it.setChartVisible(true);
 		}else{
 			installAnalyzer("XYACQ");
 			MMTFrame.getInstance().preferDailog.enableEdit(true);
 			MMTFrame.getInstance().setLiveViewIcon(false);
 			for(RoiItem it:roiList_)
 				it.setChartVisible(false);
 		}
 		liveView();
 	}
 	public void cleanStaticData() {
 		for(RoiItem item: roiList_){
 			item.clearStaticData();
 		}
 	}
 	public void lockEveryThingButThis() {
 		MMTFrame.getInstance().lockEveryThingButThis(!buttonIsLocked_);
 		buttonIsLocked_ = !buttonIsLocked_;
 	}
 	public void updateCorrChart(final int roiIndex,final double[] yArray) {
 
 		if(MMT.debug && MMT.currentframeIndex_% MMT.VariablesNUPD.showDebugTime.value() == 0){
 			roiList_.get(roiIndex).clearChart("Chart-Corr");
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					for (int j = 0; j < yArray.length; j++) {
 						roiList_.get(roiIndex).addChartData("Chart-Corr",j,yArray[j]);
 					}
 				}
 			});
 		}
 	}
 	public void updatePosProfileChart(final int roiIndex,final double[] currProfiles) {
 
 		final  double[] posProfile = currProfiles;
 		if(MMT.debug && MMT.currentframeIndex_% MMT.VariablesNUPD.showDebugTime.value() == 0){
 			roiList_.get(roiIndex).clearChart("Chart-PosProfile");
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					for (int j = 0; j < posProfile.length; j++) {
 						roiList_.get(roiIndex).addChartData("Chart-PosProfile",j,posProfile[j]);
 					}
 				}
 			});		
 		}
 	}
 	public void SetXYOrign() {
 		for(RoiItem it:roiList_)
 			it.setXYOrign();
 
 	}
 }
