 package org.ndaguan.micromanager.mmtracker;
 
 import java.awt.Color;
 
 public class MMT {
 	public static final String menuName = "MultZIndexMeasure";
 	public static final String tooltipDescription = "MultZIndexMeasure";
 	public static final int TCPIPPort = 50501;
 	public static String DEFAULT_TITLE = "Magnetic Tweezers Images Analyzer(SM4.IOP.CAS.CN)";
 
 	public static  String magnetXYstage_ = "MP285 XY Stage";
 	public static  String magnetZStage_ = "MP285 Z Stage";
 	public static  boolean debug = true;
 	
 	public static  String xyStage_ = null; 
 	public static  String zStage_ = null;
 	
 	public static boolean isAnalyzerBusy_ = false;
 	public static boolean isCalibrationRunning_ = false;
 	public static boolean isTestingRunning_ = false;
 	public static boolean isGetXYPositionRunning_ = false;
 	public static boolean isGetXYZPositionRunning_ = false;
 	
 	public static Object Acqlock = 0;
 	public static int calibrateIndex_ = 0;
 	public static int testingIndex_ = 0;
 	public static int currentframeIndex_ = 0;
 	public static String lastError_ = "No Error";
 	
 	
 	public static String[] CHARTLIST = new String[]{
 		"Chart-Z","Chart-X","Chart-Y","Chart-L","Chart-FX","Chart-FY",
 		"Chart-STDXDY","Chart-SKREWNESS","Chart-Testing","Chart-Corr","Chart-PosProfile","Chart-SumX","Chart-SumY"
 	};
 
 	public static void logError(String string) 
 	{
 		MMTFrame.getInstance().infomation_.setForeground(new Color(255,0,0));
 		MMTFrame.getInstance().infomation_.setText("Error!\t"+string);
 		System.out.print(String.format("Error!!!\t%s\r\n",string));
 	}
 	public static void debugError(String string) 
 	{
 		System.out.print(String.format("Error!!!\t%s\r\n",string));
 	}
 	
 	public static void logMessage(String string) 
 	{
 		MMTFrame.getInstance().infomation_.setForeground(new Color(0,0,0));
 		MMTFrame.getInstance().infomation_.setText("Msg:\t"+string);
 		System.out.print(String.format("Msg>>\t%s\r\n",string));
 	}
 	public static void debugMessage(String string) 
 	{
 		System.out.print(String.format("Msg>>%s\t\r\n",string));
 	}
 	
 	public static int[] unEditAfterCalbration = new int[]{0,4,5,8};
 	public static double currXP = 40;
 	public static boolean isFeedbackRunning_ = false;
 	protected static String currentUser;
 	public static enum VariablesClassify{
 		General,
 		DataSheet,
 		Advance,
 		Feedback,
 		Debug;
 	}
 	public static enum VariablesNUPD {
 		//constructor format:	unit,	default value,	precision,	importance,	toolTip,	classify
 		beanRadiuPixel("/pixel ",55,0,1,"选中的框大小，此范围外的图像数据无效，太小则精度不好，太大了可能会导致定位不准和计算耗时",VariablesClassify.General.name()),
 		frameToCalcForce("/f ",300,0,1,"多少帧移动一次磁铁，快速拉伸时推荐使用100+，要计算相对准确的力，推荐使用1000+",VariablesClassify.General.name()),
 		magnetStepSize("/uM ",100,0,1,"移动一次磁铁走过的距离，太大时会导致MP285相应太慢",VariablesClassify.General.name()),
 		chartWidth("",2000,0,1,"数据图的长度，推荐2000+",VariablesClassify.DataSheet.name()),
 		
 		calRange("/uM ",3,0.01,1,"标定的范围，太大会增加标定耗时，至少选择2倍DNA长度以上",VariablesClassify.General.name()),
 		calStepSize("/uM ",0.1,0.01,1,"标定的精度，每隔多少uM记录一个标定值，太大精度不好，太小耗时增加，推荐0.01~0.1",VariablesClassify.General.name()),
 		beanRadius("/uM ",1.4,0.001,1,"磁球的物理半径，用来计算磁力",VariablesClassify.General.name()),
 		contourLen("/uM ",1,0.001,1,"DNA长度，用来计算磁力",VariablesClassify.General.name()),
 		//advance
 		rInterStep("/pixel ",0.2,0.1,0,"极坐标积分时的内插值大小，用来记录衍射环形状，太大时精度不好，太小时计算耗时，使用0.1时会有已知Bug，推荐使用默认值",VariablesClassify.Advance.name()),
 		persistance("/uM ",0.05,0.001,0,"DNA刚度，用来计算磁力，推荐使用默认值",VariablesClassify.Advance.name()),
 		kT("/pN*nM ",4.2,0.001,0,"Kb*T,用来计算磁力，推荐使用默认值",VariablesClassify.Advance.name()),
 		precision("/uM",0.0001,0.0001,0,"插值算法中的精度，即整个测量系统最终需要的最小精度，太大时精度不好，太小时计算耗时，推荐使用默认值",VariablesClassify.Advance.name()),
 		
 		pixelToPhysX("(Um/pixel) ",0.075,0.0001,0,"一个像素对应的物理大小，位移太可控制XY方向移动时无需设置，否则需要根据放大倍数和CCD参数确定",VariablesClassify.Advance.name()),
 		pixelToPhysY("(Um/pixel) ",0.075,0.0001,0,"一个像素对应的物理大小，位移太可控制XY方向移动时无需设置，否则需要根据放大倍数和CCD参数确定",VariablesClassify.Advance.name()),
 		xFactor(" ",1,0.0001,0,"衍射环在X方向的修正系数，在CCD像素点非正方形时使用，需要参考CCD型号，推荐使用默认值",VariablesClassify.Advance.name()),
 		yFactor(" ",1,0.0001,0,"衍射环在Y方向的修正系数，在CCD像素点非正方形时使用，需要参考CCD型号，推荐使用默认值",VariablesClassify.Advance.name()),
 		
 		testingPrecision("",0.05,0.01,0,"计算标定误差时的精度，每隔多少uM做一个检验，太小计算耗时，太大了精度不够，推荐使用默认值",VariablesClassify.Advance.name()),
 		responceXY("",0,0,0,"测试专用：是否在标定之前记录，显示更新磁球数据，1：是，0：否",VariablesClassify.Debug.name()),
 		saveFile(" ",1,0,0,"测试专用：是否保存数据,1：是，0：否",VariablesClassify.Debug.name()),
 		showDebugTime("",10000,0,0,"测试专用：更新correlation 及 posProfile 图像的帧距",VariablesClassify.Debug.name()),
 		
 		chartStatisWindow("",300,0,0,"数据图像显示：响应变化的帧数，太小时图像容易抖动，太大时图像不容易自动缩放，推荐使用200~1000",VariablesClassify.DataSheet.name()),
 		frameToRefreshChart("",10,0,0,"数据图像显示：更新图像的帧数，太小了计算耗时，太大了更新慢,推荐使用20~100",VariablesClassify.DataSheet.name()),
 		frameToRefreshImage("",50,0,0,"图像显示即响应鼠标操作时间，太小了计算耗时，太大了响应慢，容易出现选框跟不上球的移动，推荐使用50~100",VariablesClassify.DataSheet.name()),
 		stageMoveSleepTime("/ms",30,0,0,"位移台移动等待时间，太小了会导致位移台移动不到需要位置，太大了耗时，推荐参考位移台信息，或使用默认值",VariablesClassify.Advance.name()),
 		
 		hasZStage("",1,0,0,"位移台是否可以控制样品在Z方向移动，1：是，0：否",VariablesClassify.Debug.name()),
 		hasXYStage("",1,0,0,"位移台是否可以控制样品在XY方向移动，1：是，0：否",VariablesClassify.Debug.name()),
 		needStageServer("",0,0,0,"是否需要使用位移台服务器，1：是，0：否",VariablesClassify.Debug.name()),
 		needCheckStageMovment("",0,0,0,"设置位移台位置后是否需要确认才返回，1：是，0：否",VariablesClassify.Debug.name()),
 		stageMovmentPrecision("",0.02,0.001,0,"位移台移动允许误差",VariablesClassify.Debug.name()),
 		frameToFeedBack("",5,0,0,"多少帧反馈一次",VariablesClassify.Feedback.name()),
 		feedBackMaxStepSize("/uM",0.02,0.001,0,"反馈最大步长,每次反馈走的最大位移，太大了容易震荡，太小反馈慢",VariablesClassify.Feedback.name()),
 		feedBackMinStepSize("/uM",0.000,0.001,0,"反馈最小步长，当飘逸小于此值时不触发反馈",VariablesClassify.Feedback.name()),
 		feedBackWindowSize("",10,0,0,"反馈滑动窗口大小",VariablesClassify.Feedback.name()),
 		pTerm("",-0.2,0.0001,0,"比例系数",VariablesClassify.Feedback.name()),
 		needXYcalibrate("",1,0,0,"xy方向是否需要标定(用于确定一个像素对应多少nm)，1：是，0：否",VariablesClassify.Advance.name()),
 		crossSize("",20,0,0,"十字×宽度",VariablesClassify.Advance.name()),
		iTerm("",0.01,0.0001,0,"积分系数",VariablesClassify.Feedback.name()),
 		XYMirror("",1,0,0,"图像的XY方向是否需要互换，1：是，0：否，推荐使用默认值",VariablesClassify.Feedback.name());
 		private String unit;
 		private double value;
 		private double presicion;
 		private  int important;
 		private String toolTip;
 		private String classify;
 		VariablesNUPD(String u,double v,double p,int i,String t,String c) {
 			unit = u;
 			value = v;
 			presicion = p;
 			important = i;
 			toolTip = t;
 			classify = c;
 			
 		}
 	 
 		public double value() {
 			return value;
 		}
 		public void value(double v) {
 			value = v;
 		}
 		public String getUnit() {
 			return unit;
 		}
 		public double getPresicion() {
 			return presicion;
 		}
 		public String getToolTip(){
 			return toolTip;
 		}
 		public String getClassify(){
 			return classify;
 		}
 		public int getImp() {
 			return important;
 		}
 
 		public int getTabIndex() {
 			VariablesClassify[] classifyArray = VariablesClassify.values();
 			int classifyLen = classifyArray.length;
 			for(int i =0;i<classifyLen;i++){
 				if(classify.equals(classifyArray[i].name()))
 						return i;
 			}
 			return -1;
 		}
 		
 	};
 }
