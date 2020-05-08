 
 package com.live.Debugger;
 
 import java.awt.Dimension;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 import org.python.parser.ast.Print;
 
 import com.sun.javafx.scene.layout.region.Margins.Converter;
 
 import tod.core.config.TODConfig;
 import tod.core.database.browser.IEventBrowser;
 import tod.core.database.browser.ILogBrowser;
 import tod.core.database.browser.LocationUtils;
 import tod.core.database.browser.Stepper;
 import tod.core.database.event.IBehaviorCallEvent;
 import tod.core.database.event.ICallerSideEvent;
 import tod.core.database.event.ILogEvent;
 import tod.core.database.structure.IBehaviorInfo;
 import tod.core.database.structure.ILocationInfo;
 import tod.core.database.structure.ObjectId;
 import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
 import tod.core.database.structure.IStructureDatabase.LineNumberInfo;
 import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
 import tod.core.database.structure.SourceRange;
 import tod.core.database.structure.IStructureDatabase.ProbeInfo;
 import tod.core.session.ISession;
 import tod.core.session.SessionTypeManager;
 import tod.gui.IExtensionPoints;
 import tod.gui.MinerUI;
 import tod.gui.StandaloneUI;
 import tod.gui.SwingDialogUtils;
 import tod.gui.IGUIManager.DialogType;
 import tod.gui.components.eventlist.IntimacyLevel;
 import tod.impl.common.event.BehaviorExitEvent;
 import tod.impl.dbgrid.event.MethodCallEvent;
 import tod.impl.local.EventBrowser;
 import zz.utils.notification.IEvent;
 
 import netscape.javascript.JSObject;
 
 import javafx.application.Application;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.geometry.HPos;
 import javafx.geometry.Insets;
 import javafx.geometry.Orientation;
 import javafx.geometry.Pos;
 import javafx.scene.Group;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ProgressBar;
 import javafx.scene.control.ScrollBar;
 import javafx.scene.control.ScrollPane;
 import javafx.scene.control.Slider;
 import javafx.scene.control.Tooltip;
 import javafx.scene.effect.ColorAdjust;
 import javafx.scene.effect.Lighting;
 import javafx.scene.image.ImageView;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.ColumnConstraints;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.RowConstraints;
 import javafx.scene.layout.StackPane;
 import javafx.scene.layout.VBox;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Line;
 import javafx.scene.shape.Polygon;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Font;
 import javafx.scene.text.FontWeight;
 import javafx.scene.text.Text;
 import javafx.scene.text.TextAlignment;
 import javafx.stage.Stage;
 
 /**
  * @author penguin
  *
  */
 /**
  * @author penguin
  *
  */
 /**
  * @author penguin
  *
  */
 public class liveDebugging extends Application {
 
 	private static Parent root = null;
 	private static String[] _args;
 
 	int currentWindowIdx = 0;
 
 	Stack<CodeWindow> codeWindowStack;
 	Stack<timeline> timelineStack;
 	
 	static CodeWindow currentCodeWindow;
 	static CodeWindow prevCodeWindow;
 	
 	static timeline currentTimeline;
 	static timeline prevTimeline;	
 	
 	VariablePane currentVar;
 	
 	static ArrayList<MethodInfo> methodList; // list of methods currently loaded, may have multiple instances of a method
 
 	
 	Pane codeWindowAreaNew;
 	static Pane codeWindowArea=new Pane ();
 
 	static EventUtils eventUtils;
 	static CodeFragments codeFragments;
 
 	int lineNumberOffset;
 	private HashMap displayedCodeWindowsList; //list of code windows currently displayed on screen
 	private ArrayList<CodeWindow> CodeWindowCallStack; //maintains a list that represents the call stack
 	
 	private TreeMap<String, HashMap> displayedTimelineList;//list of timelines displayed on screen, value = index of element in container (latest instance of a particular timeline)
 	static Pane timelineSection=new Pane ();
 
 	//timeline positions
 	double posX=37;
 	int posY=0;
 	static Pane variablePane =new Pane();
 
 	static //for Highlighting
 	CodeWindow mainCWH;
 	timeline mainTH;
 
 	ScrollPane s1 = new ScrollPane();
 	static VariablePane gridPane; 
 	RowConstraints rowinfo = new RowConstraints();	
 
 
 
 	public static void main(String[] args) {
 		launch(args);
 		_args = args;
 	}
 
 	@Override
 	public void start(Stage primaryStage) throws IOException {
 
 		root = FXMLLoader.load(getClass().getClassLoader().getResource(
 				"LDB_fxml.fxml"));
 
 		//load the test code
 //		Path classPath = FileSystems.getDefault().getPath("resource", "Program1.txt");
 //		codeFragments = new CodeFragments(classPath, this, "p1");
 		
 		Path classPath = FileSystems.getDefault().getPath("resource", "Program2.txt");
 		codeFragments = new CodeFragments(classPath, this, "p2");
 		
 //		Path classPath = FileSystems.getDefault().getPath("resource", "SampleProgram.txt");
 //		codeFragments = new CodeFragments(classPath, this, "sp");
 		
 		//initialize the variables
 		methodList = new ArrayList<MethodInfo>();
 		eventUtils = new EventUtils();
 		currentCodeWindow = null;
 		prevCodeWindow = null;
 		lineNumberOffset = 0;
 		codeWindowStack = new Stack<>();
 		timelineStack=new Stack<>();
 		displayedCodeWindowsList = new HashMap<String, Integer>();
 		displayedTimelineList = new TreeMap<String, HashMap>();
 		CodeWindowCallStack = new ArrayList<CodeWindow>();
 
 		codeWindowAreaNew = (Pane) getRootAnchorPane().lookup("#codeWindowSection");
 		
 		//Initialize code window for "main" method
 		ILogEvent mainEvent = getMainMethodEvent();
 		createMainWindow("main");
 		
 		//highlight gutters
 		highlightGutters(mainEvent);
 
 		//setting the 1st and last event of "main"		
 		MethodCallEvent mEvent = (MethodCallEvent)(mainEvent);
 		IEventBrowser browser = mEvent.getChildrenBrowser();
 		long firstTimestamp = browser.getFirstTimestamp();
 		long lastTimestamp = browser.getLastTimestamp();
 		
 		eventUtils.setFirstTimestamp(firstTimestamp);
 		eventUtils.setLastTimestamp(lastTimestamp);
 		
 		currentCodeWindow.setFirstTimestamp(firstTimestamp);
 		currentCodeWindow.setLastTimestamp(lastTimestamp);
 		
 		methodList.add(new MethodInfo(firstTimestamp, lastTimestamp, -1, -1, "main", "", 0, 0));
 		
 		
 		//initialize variable pane window
 		variablePane = (Pane) getRootAnchorPane().lookup("#VariablePane");
 		ArrayList<EventInfo> childEventsInfo = eventUtils.getChildEventsInfo(mainEvent);
 		gridPane = currentCodeWindow.getGridPane(childEventsInfo);
         variablePane.getChildren().add(gridPane);
 
         //initializing timeline section
 		ScrollPane sc = new ScrollPane();
 		sc.setPrefSize(1830, 200);
 		sc.setContent(timelineSection);
 		sc.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
 		sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);		
 		
 		Pane timelineSectionNew = (Pane) getRootAnchorPane().lookup("#naviBarSection");
 		timelineSectionNew.getChildren().add(sc);
 		
 		
 		
 		//creating timeline for "main" method
 		timeline tLine = createTimeLine(null, mainEvent, childEventsInfo);
 		currentTimeline = tLine;
 		mainTH=currentTimeline;
 		
 		HashMap<Long, Integer> mainTimelineIdx = new HashMap<Long, Integer>();
 		mainTimelineIdx.put(mainEvent.getTimestamp(), 0);
 		displayedTimelineList.put("main", mainTimelineIdx);
 
 		//setting the main code window and timeline color to red
 		String color="FF8C73"; //red
 		mainCWH.setBackgroundColorToMain();
 		mainTH.setColor(color);
 		
 		initializeElementControl();
 
 		Scene s = new Scene(root);
 		java.net.URL url=liveDebugging.class.getResource("SDB.css");
 		s.getStylesheets().add(url.toExternalForm());
 		primaryStage.setTitle("Swift Debugger");
 		primaryStage.setScene(s);
 		primaryStage.setWidth(1830);
 		primaryStage.setHeight(1000);
 		primaryStage.show();
 	}
 
 	private static AnchorPane getRootAnchorPane() {
 		AnchorPane pane = (AnchorPane) root.lookup("#AnchorPane");
 		return pane;
 	}
 
 	private void addDraggableElementToRoot(DraggableNode node) {
 		AnchorPane root = getRootAnchorPane();
 		root.getChildren().add(node);
 	}
 
 	private void addElementToRoot(Node node) {
 		AnchorPane root = getRootAnchorPane();
 		root.getChildren().add(node);
 	}
 
 	// need to find a way to get genric type for parent, for now use pane
 	public void addElement(ScrollPane Parent, Node node) {
 		if (Parent != null) {
 			Parent.setContent(node);
 		}
 	}
 
 
 	// displaying main code
 	private void createMainWindow(String methodName) {
 
 		if (codeFragments.codeFragmentExist(methodName)) {
 			// get the code fragment and add it to the code area
 			CodeWindow editor = codeFragments.getCodeFragment(methodName);
 			codeWindowArea.getChildren().add(editor);
 
 			// set line number offset
 			lineNumberOffset = codeFragments.getLineNumberOffset(methodName);
 
 			currentCodeWindow = editor;
 		    s1.setPrefSize(1600, 700);
 			s1.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
 			s1.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
 			s1.setContent(codeWindowArea);
 			codeWindowAreaNew.getChildren().add(s1);
 			displayedCodeWindowsList.put(methodName, 0);
 			
 			mainCWH = editor;
 			CodeWindowCallStack.add(editor);
 			codeWindowStack.push(currentCodeWindow);
 		}
 	}
 	
 	//This method is used to highlight gutters in the editor
 	private static void highlightGutters(ILogEvent event){
 		ArrayList<Integer> lineNumbers =eventUtils.getExecutedLineNumers(event);
 		currentCodeWindow.highlightGutters(lineNumbers, codeFragments.getLineNumberOffset(currentCodeWindow.getMethodName()) );
 		
 	}
 	
 	//This method is used to set the selected tick
 	private void setTick(timeline currentTimeline, ILogEvent event){
 
 		long timestamp = event.getTimestamp();
 
 		currentTimeline.setTick(timestamp);
 	}
 	
 	//This method is used to decrement the slider value by 1
 	private void decrementTick(timeline currentTimeline2){
 		double tick=currentTimeline2.getValue();
 		try{
 			tick--;
 			if(tick>=currentTimeline2.getMin()){
 				currentTimeline2.setValue(tick);
 			}
 		}
 		catch (Exception ex){
 			int m=0;
 		}
 
 //		Tooltip tip=new Tooltip(String.valueOf(t.getValue()));
 //		t.setTooltip(tip);
 	}
 	
 	//This method is used to step backwards
 	private void processPrevious(ILogEvent curEvent, ILogEvent prevEvent) {		
 
 		int prevLine=eventUtils.getLineNum(prevEvent) - currentCodeWindow.getStartLine() - 1;
 		int curLine=eventUtils.getLineNum(curEvent) - currentCodeWindow.getStartLine() - 1;
 		
 //-----------------------------------------------------------Case: Method return-----------------------------------------------				
 		
 		if (eventUtils.isParentOf(prevEvent, curEvent)) {	// previous event is parent, we stepped backwards out of a method
 
 			//since we are stepping out of the method, remove it from the call stack
 			//check if the current code window is the last method in the call stack, remove it
 			if(CodeWindowCallStack.get(CodeWindowCallStack.size() - 1).getMethodName().equalsIgnoreCase(currentCodeWindow.getMethodName()))
 				CodeWindowCallStack.remove(CodeWindowCallStack.size() - 1);
 			
 			//stash the previous window
 			CodeWindow oldPreviousWindow=prevCodeWindow;
 			prevCodeWindow = currentCodeWindow;
 			
 			timeline oldPreviousTimeline = prevTimeline;
 			prevTimeline=currentTimeline;
 			
 			//current code window is now from the code window stack
 			if (!CodeWindowCallStack.isEmpty()){
 				currentCodeWindow= CodeWindowCallStack.get(CodeWindowCallStack.size() - 1);//return the last element in the call stack
 				currentTimeline=timelineStack.pop();
 				
 //				currentTimeline.printCallStack(); //for debugging
 			}
 			if(oldPreviousWindow!=currentCodeWindow && oldPreviousWindow != null){
 				oldPreviousWindow.setBackgroundColorToInactive();
 				oldPreviousWindow.reduceWindowSize();//prevCodeWindow.normalWindowSize();
 				oldPreviousWindow.setPinBtn("plus");
 				//oldPreviousWindow.getPinBtn().setVisible(true);
 //				currentCodeWindow.normalWindowSize();
 			}
 			
 			//set the highlights for the main and prev windows
 			if(currentCodeWindow != mainCWH)
 				currentCodeWindow.setBackgroundColorToCurrent();
 			//ensure current window is maximixed
 			currentCodeWindow.normalWindowSize();
 			
 			if(prevCodeWindow != mainCWH)
 				prevCodeWindow.setBackgroundColorToPrevious();
 			prevCodeWindow.normalWindowSize();
 			
 			// highlight next line to be executed
 			int lineNum = eventUtils.getLineNum(prevEvent);
 			
 //			lineNumberOffset = codeFragments.getLineNumberOffset(currentCodeWindow.getMethodName());
 			int line=lineNum - currentCodeWindow.getStartLine()- 1;
 			
 			currentCodeWindow.setLineColorToCurrent(line);
 			
 			currentCodeWindow.setExecutedLine(line);//so we know the start point of the arrow
 			
 			if(oldPreviousTimeline != mainTH && oldPreviousWindow != null)
 				oldPreviousTimeline.setColor("CCCCCC");
 			
 			if(prevTimeline != mainTH && oldPreviousWindow != null)
 				prevTimeline.setColor("FFFB78");
 			
 			if(currentTimeline != mainTH)
 				currentTimeline.setColor("A3FF7F");
 			
 			setTick(currentTimeline, prevEvent);
 			
 			gridPane=currentCodeWindow.getGridPane();
 			variablePane.getChildren().set(0,gridPane);
 			reposition();
 		
 		} 
 		else if (prevEvent.getDepth() > curEvent.getDepth()){
 			//-----------------------------------------------------------Case: Method Call-----------------------------------------------		
 			
 			String methodName = eventUtils.getMethodName(prevEvent);
 						
 			CodeWindow codeWin = null;				
 			boolean addedNewWindow = false;
 			
 			//get the code window from the list of displayed windows
 			if(displayedCodeWindowsList.containsKey(methodName))
 			{ 
 				codeWin = (CodeWindow) codeWindowArea.getChildren().get((int) displayedCodeWindowsList.get(methodName));				
 				codeWin.setIteration(prevEvent.getParent().getTimestamp());//get the timestamp of the original method call event
 			}
 			
 			//update the selected line of the last method call
 			//add the new method to the call stack
 			CodeWindowCallStack.get(CodeWindowCallStack.size() - 1).setExecutedLine(curLine);				
 			CodeWindowCallStack.add(codeWin);
 				
 			//highlight the line of the method call in the current window
 			currentCodeWindow.setLineColorToMethodCall(curLine);
 			
 			//set the line number which initiated the method call so we know where to palce the arrow
 			currentCodeWindow.setSelectedLineNumber(curLine);
 			currentCodeWindow.setExecutedLine(curLine);
 			
 
 			//set the new window as current window
 			CodeWindow oldPrevWindow = prevCodeWindow;
 			prevCodeWindow = currentCodeWindow;
 			currentCodeWindow = codeWin;
 			
 			//set codeWindow background colors
 			if(oldPrevWindow!=mainCWH && oldPrevWindow != null)
 			{
 					oldPrevWindow.setBackgroundColorToInactive();
 			}				
 			if(prevCodeWindow!=mainCWH && prevCodeWindow != null)
 			{
 				prevCodeWindow.setBackgroundColorToPrevious();
 			}
 			currentCodeWindow.setBackgroundColorToCurrent();
 			
 			//minimize oldPrevWindow
 			if(oldPrevWindow!=null){
 				prevCodeWindow.reduceWindowSize();
 				prevCodeWindow.setPinBtn("plus");
 				//prevCodeWindow.getPinBtn().setVisible(true);
 			}
 			
 			//maximise current window
 			currentCodeWindow.normalWindowSize();
 
 			gridPane=currentCodeWindow.getGridPane();
 			variablePane.getChildren().set(0,gridPane);
 			
 			//handling timeline -----------------------------------
 			
 			//get timeline of the child method
 			//get the timestamp of the parent method call
 			long timestamp = prevEvent.getParent().getTimestamp();
 			HashMap<Long, Integer> methodIdxhash = displayedTimelineList.get(methodName);//hashmap of the method with the idx of it's timelines in the display container (timelineSection)
 			int index = methodIdxhash.get(timestamp);
 			timeline tLine = (timeline) timelineSection.getChildren().get(index);
 			
 			timeline oldPrevTimeline = prevTimeline;
 			prevTimeline=currentTimeline;
 			currentTimeline= tLine;
 			
 			//reduce / hide child timelines and make sure our "new" timeline is expanded and visible
 			reduceChildTimelines(false);			
 			currentTimeline.expandTimeline();
 			currentTimeline.showTimeline();
 			
 			timelineStack.push(prevTimeline);
 			
 			//set timeline colors
 			if(oldPrevTimeline != null && oldPrevTimeline != mainTH)
 				oldPrevTimeline.setColor("CCCCCC");
 			if(prevTimeline != null && prevTimeline != mainTH)
 				prevTimeline.setColor("FFFB78");
 			currentTimeline.setColor("A3FF7F");
 			
 			
 			reposition();			
 		
 		} 
 //-----------------------------------------------------------Case: Go To Previous Line-----------------------------------------------			
 		else {
 			
 			//highlight the previous line, set current line to white
 			currentCodeWindow.setLineColorToPrevious(curLine);
 			currentCodeWindow.setLineColorToCurrent(prevLine);
 			currentCodeWindow.setExecutedLine(prevLine);
 			
 			setTick(currentTimeline, prevEvent);
 						
 			//event writes to a variable, we handle inputing/updates values on the variable pane (grid pane)
 			if(eventUtils.isWriteEvent(prevEvent)){
 				//List<LocalVariableInfo> locVariablesList=eventUtils.getLocalVariables(nextEvent);
 				String varName= eventUtils.getWriteEventVarName(prevEvent);		
 			
 				gridPane = currentCodeWindow.getGridPane().highlightVariableValue(varName, prevEvent.getTimestamp());
 					
 				currentCodeWindow.removeHighlightedSection();
 				currentCodeWindow.highlightSection(prevLine, varName);
 					
 			}else 
 			{	//may be a SYSTEM method call
 				if(eventUtils.isMethodCall(prevEvent))
 				{
 					String methodName = eventUtils.getMethodName(prevEvent);
 					
 					if(methodName != null)
 					{
 						currentCodeWindow.removeHighlightedSection();
 						currentCodeWindow.highlightSection(prevLine, methodName);
 					}
 						
 				}
 					
 			}
 		}
 	}
 	
 	//calculate the position of arrow at x axis
 	private int calculateArrowX(DraggableNode cd){
 		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
 		if(!ed.isReduced()){
 			return (int)(cd.getLayoutX()+612);
 		}else
 			return (int)(cd.getLayoutX()+ cd.getDWidth()+22);//((Rectangle)(cd.getChildren().get(0))).getWidth()
 	}
 	
 	//calculate the position of arrow at y axis
 	private int calculateArrowY(DraggableNode cd, int lineNum){
 		//return (int)(prevCodeWindow.y+lineNum*9+20);
 		CodeEditor ed=((CodeEditor)(cd.getChildren().get(2)));
 		
 		if(!ed.isReduced()){
 		return (int)cd.getLayoutY()+(lineNum+1)*9+10;
 		}
 		else
 			return (int)(cd.getLayoutY()+ cd.getDHeigth());//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(lineNum+1)*9+10)*0.3);//(int)(((int)ed.getLayoutY()+(lineNum+1)*9+10)*0.3);
 	}
 	
 	//calculate the position of code fragment window at x axis
 	private static int calculateWindowX(CodeWindow cd) {
 		CodeEditor ed= cd.getEditor();
 		if(!ed.isReduced()){
			return	(int)cd.getLayoutX() + 630;
 		}else
 			return (int)(cd.getLayoutX()+ cd.getWindowWidth() +80);
 	}
 	
 	//calculate the position of code fragment window at y axis
 	private static int calculateWindowY(CodeWindow cd,int linenum) {
 		// TODO Auto-generated method stub
 		CodeEditor ed = cd.getEditor();
 //		if(!ed.isReduced()){
		return (int)cd.getLayoutY() + (linenum+1)*cd.lineoffset.getValue() + 30;
 //		}
 //		else
 //			return (int)(cd.getLayoutY()+ ed.getHeight() + 80);//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(linenum+1)*9)*0.3);
 
 	}
 	//elocate all the existing added arrows and code fragment windows over the screen
 	static public void reposition(){
 		
 		//set main to the top left corner of the area
 		CodeWindow cd=(CodeWindow)codeWindowArea.getChildren().get(0);
 		codeWindowArea.getChildren().set(0,cd);
 		cd.relocate(0, 0);
 		
 		for(int j=1;j<codeWindowArea.getChildren().size();j=j+2){
 //			Arrow cArrow=(Arrow)(codeWindowArea.getChildren().get(j+1));
 //			int ax=calculateArrowX(cd);
 //			int ay=calculateArrowY(cd,((CodeWindow)cd).getSelectedLineNumber());
 			
 			CodeWindow newCd=(CodeWindow) codeWindowArea.getChildren().get(j);
 			int x=calculateWindowX(cd);
 			int y=calculateWindowY(cd,((CodeWindow)cd).getSelectedLineNumber());
 			
 //			codeWindowArea.getChildren().set(j,newCd);			
 			newCd.relocate(x, y);
 			//set the x and y property of the parent DraggableNode class
 			newCd.getDraggableX().set(x);
 			newCd.getDraggableY().set(y);
 
 			cd=newCd;
 		}
 
 	}
 	
 
 	//This method is used to proceed towards a next line
 	private void processNextLine(ILogEvent curEvent, ILogEvent nextEvent) {
 		
 		//highlighting the gutter
 		int prevLine = eventUtils.getLineNum(curEvent) - currentCodeWindow.getStartLine() - 1;
 		int curLine = eventUtils.getLineNum(nextEvent) - currentCodeWindow.getStartLine() - 1;
 		
 		//handle the case the event is an object instantiation
 		if(nextEvent instanceof IBehaviorCallEvent)
 		{
 			IBehaviorCallEvent callEvent = (IBehaviorCallEvent) nextEvent;
 		
 			if(callEvent.getCalledBehavior() != null)
 				if(callEvent.getCalledBehavior().getName().compareToIgnoreCase("<init>") == 0)
 					curLine = prevLine;
 		}
 		
 		//handle special case - check if this is still valid
 		if (curLine < 0) {
 
 			curLine = currentCodeWindow.getExecutedLine();
 		}
 		
 		if(curLine != prevLine && prevLine > 0)
 			currentCodeWindow.setGutterToComplete(prevLine);
 		
 		int clineNum = eventUtils.getLineNum(curEvent);
 		
 		
 		
 		//sets current line to white - remove highlight
 		currentCodeWindow.setLineColorToPrevious(currentCodeWindow.getExecutedLine());
 		
 //-----------------------------------------------------------Case: Method return-----------------------------------------------		
 
 		//if depth of next event > current, we have returned to parent method
 		if (curEvent.getDepth() > nextEvent.getDepth() &&
 				eventUtils.getParentMethodName(nextEvent) != null &&
 				eventUtils.getParentMethodName(nextEvent).compareToIgnoreCase(currentCodeWindow.getMethodName()) != 0) {
 			
 			CodeWindow oldPreviousWindow=null;
 			timeline oldPreviousTimeline = null;
 			
 			//iterate through the codewindows / timeline until we reach the correct one
 			while(eventUtils.getParentMethodName(nextEvent).compareToIgnoreCase(currentCodeWindow.getMethodName()) != 0)
 			{
 				//since we are stepping out of the method, remove it from the call stack
 				//check if the current code window is the last method in the call stack, remove it
 				if(CodeWindowCallStack.get(CodeWindowCallStack.size() - 1).getMethodName().equalsIgnoreCase(currentCodeWindow.getMethodName()))
 					CodeWindowCallStack.remove(CodeWindowCallStack.size() - 1);
 				
 				//stash the previous window
 				oldPreviousWindow=prevCodeWindow;
 				prevCodeWindow = currentCodeWindow;
 				
 				oldPreviousTimeline = prevTimeline;
 				prevTimeline=currentTimeline;
 				
 				//current code window is now from the code window stack
 				if (!CodeWindowCallStack.isEmpty()){
 					currentCodeWindow= CodeWindowCallStack.get(CodeWindowCallStack.size() - 1);//return the last element in the call stack
 					currentTimeline=timelineStack.pop();
 					
 	//				currentTimeline.printCallStack(); //for debugging
 				}
 			}
 			
 			if(oldPreviousWindow!=currentCodeWindow && oldPreviousWindow != null){
 				oldPreviousWindow.setBackgroundColorToInactive();
 				oldPreviousWindow.reduceWindowSize();//prevCodeWindow.normalWindowSize();
 				oldPreviousWindow.setPinBtn("plus");
 				//oldPreviousWindow.getPinBtn().setVisible(true);
 //				currentCodeWindow.normalWindowSize();
 			}
 			
 			//set the highlights for the main and prev windows
 			if(currentCodeWindow != mainCWH)
 				currentCodeWindow.setBackgroundColorToCurrent();
 			//ensure current window is maximixed
 			currentCodeWindow.normalWindowSize();
 			
 			prevCodeWindow.setBackgroundColorToPrevious();
 			prevCodeWindow.normalWindowSize();
 			
 			// highlight next line to be executed
 			int lineNum = eventUtils.getLineNum(nextEvent);
 			
 //			lineNumberOffset = codeFragments.getLineNumberOffset(currentCodeWindow.getMethodName());
 			int line=lineNum - currentCodeWindow.getStartLine()- 1;
 			
 			currentCodeWindow.setLineColorToCurrent(line);
 			
 			currentCodeWindow.setExecutedLine(line);//so we know the start point of the arrow
 			
 			if(oldPreviousTimeline != null)
 				oldPreviousTimeline.setColor("CCCCCC");
 			prevTimeline.setColor("FFFB78");
 			if(currentTimeline != mainTH)
 				currentTimeline.setColor("A3FF7F");
 			
 			setTick(currentTimeline, nextEvent);
 			
 			//save previous window grid pane
 			prevCodeWindow.setGridPane(gridPane);
 
 			//restore current window grid pane
 			gridPane=currentCodeWindow.getGridPane();
 			variablePane.getChildren().set(0,gridPane);
 			reposition();
 		}
 //-----------------------------------------------------------Case: Method call-----------------------------------------------		
 		else if (eventUtils.isMethodCall(nextEvent)) {
 			String methodName = eventUtils.getMethodName(nextEvent);
 			
 			setTick(currentTimeline, nextEvent);
 			
 			//event is a SYSTEM method call
 			//just highlight the next line
 			if (!codeFragments.codeFragmentExist(methodName)) 
 			{
 				//get the next line number and highlight it
 				int nextLine = eventUtils.getLineNum(nextEvent) - currentCodeWindow.getStartLine() - 1;
 				currentCodeWindow.setLineColorToCurrent(nextLine);
 				
 				currentCodeWindow.removeHighlightedSection();
 				currentCodeWindow.highlightSection(nextLine, methodName);
 				
 				currentCodeWindow.setExecutedLine(nextLine);
 			}
 			//ELSE it is a method call and we need to add a new code window
 			else {
 
 				CodeWindow codeWin = null;				
 				boolean addedNewWindow = false;
 //				boolean newIteration = false;
 				
 				//if code window does not exist in display container add new code window
 				if(!displayedCodeWindowsList.containsKey(methodName))
 				{
 					// get the code window 
 					codeWin = codeFragments.getCodeFragment(methodName);					
 	
 					//add the new code window to the screen, store it's position index in the container
 					codeWindowArea.getChildren().add(codeWin);
 					codeWin.setIndexOnScreen(codeWindowArea.getChildren().size() - 1);// IndexOnScreen = index of element (eg. codeWindow) as a child of the code area pane
 					//add code window to list of displayed windows and store it's position in the container
 					displayedCodeWindowsList.put(methodName, codeWindowArea.getChildren().size() - 1);
 					
 					codeWin.setIteration(nextEvent.getTimestamp());
 					addedNewWindow = true;
 				}
 				else //code window already exists in display container
 					//get the code window and increment it's iteration
 				{ 
 					codeWin = (CodeWindow) codeWindowArea.getChildren().get((int) displayedCodeWindowsList.get(methodName));
 					
 					codeWin.setIteration(nextEvent.getTimestamp());
 				}
 				
 				//update the selected line of the last method call
 				//add the new method to the call stack
 				CodeWindowCallStack.get(CodeWindowCallStack.size() - 1).setExecutedLine(clineNum);				
 				CodeWindowCallStack.add(codeWin);
 					
 				
 				//highlight the line of the method call in the current window
 				int lineNum=eventUtils.getLineNum(nextEvent) - currentCodeWindow.getStartLine() - 1;
 				currentCodeWindow.setLineColorToMethodCall(lineNum);
 				
 				//set the line number which initiated the method call so we know where to palce the arrow
 				currentCodeWindow.setSelectedLineNumber(lineNum);
 				currentCodeWindow.setExecutedLine(lineNum);
 				
 				//store the current grid pane in the current code window
 				currentCodeWindow.setGridPane(gridPane);
 				
 				//set the new window as current window
 				CodeWindow oldPrevWindow = prevCodeWindow;
 				prevCodeWindow = currentCodeWindow;
 				currentCodeWindow = codeWin;
 				
 				//set codeWindow background colors
 				if(oldPrevWindow!=mainCWH && oldPrevWindow != null)
 				{
 						oldPrevWindow.setBackgroundColorToInactive();
 				}				
 				if(prevCodeWindow!=mainCWH && prevCodeWindow != null)
 				{
 					prevCodeWindow.setBackgroundColorToPrevious();
 				}
 				currentCodeWindow.setBackgroundColorToCurrent();
 				
 				//minimize oldPrevWindow
 				if(oldPrevWindow!=null && oldPrevWindow != mainCWH){
 					prevCodeWindow.reduceWindowSize();
 					prevCodeWindow.setPinBtn("plus");
 					//prevCodeWindow.getPinBtn().setVisible(true);
 				}
 				
 				//maximise current window
 				currentCodeWindow.normalWindowSize();
 				
 				//if we added a new window
 				//add an Arrow connecting the new and previous windows
 				if(addedNewWindow)
 				{
 					Arrow arrowNew = new Arrow(prevCodeWindow, currentCodeWindow);
 					codeWindowArea.getChildren().add(arrowNew);
 				}
 				
 				//handling the timeline---------------------
 				
 				timeline tLine = null;
 				boolean timelineExists = false;
 				
 				//check if the timeline already exists
 				if(displayedTimelineList.containsKey(methodName))
 				{
 					if(displayedTimelineList.get(methodName).containsKey(nextEvent.getTimestamp()))
 					{
 						tLine = (timeline) timelineSection.getChildren().get( (int) displayedTimelineList.get(methodName).get(nextEvent.getTimestamp()) );
 						timelineExists = true;
 					}
 				}
 				
 				ArrayList<EventInfo> childEventsInfo = null;
 				if(!timelineExists) //else create new timeline
 				{
 					childEventsInfo = eventUtils.getChildEventsInfo(nextEvent);
 					tLine = createTimeLine(curEvent, nextEvent, childEventsInfo);
 				}
 
 				timeline oldPrevTimeline = prevTimeline;
 				prevTimeline=currentTimeline;
 				currentTimeline= tLine;
 				
 				reduceChildTimelines(timelineExists);
 				tLine.expandTimeline();
 				tLine.showTimeline();
 				
 				//add the idx to the new timeline to the list of children
 				prevTimeline.setChildTimelineIdx(timelineSection.getChildren().size() - 1);
 				
 				timelineStack.push(prevTimeline);
 				
 				//set timeline colors
 				if(oldPrevTimeline != null && oldPrevTimeline != mainTH)
 					oldPrevTimeline.setColor("CCCCCC");
 				if(prevTimeline != null && prevTimeline != mainTH)
 					prevTimeline.setColor("FFFB78");
 				currentTimeline.setColor("A3FF7F");
 				
 
 				//initialize grid
 				if(childEventsInfo != null)
 					gridPane=currentCodeWindow.getGridPane(childEventsInfo);
 				else
 					gridPane=currentCodeWindow.getGridPane();
 				variablePane.getChildren().set(0,gridPane);
 				
 				highlightGutters(nextEvent);
 				reposition();	
 				
 				//-------------------------------------------------------------------just doing this to get method info for now
 				MethodCallEvent methodCall = (MethodCallEvent) nextEvent;
 				IEventBrowser browser = methodCall.getChildrenBrowser();
 				
 				MethodCallEvent parent = (MethodCallEvent) nextEvent.getParent();
 				IEventBrowser parentBrowser = parent.getChildrenBrowser();
 				
 				MethodInfo methodInfo = new MethodInfo(browser.getFirstTimestamp(), browser.getLastTimestamp(), 
 						parent.getFirstTimestamp(), parentBrowser.getLastTimestamp(), 
 						methodName, eventUtils.getMethodName(parent), 
 						timelineSection.getChildren().size() - 1, (int)(displayedCodeWindowsList.get(methodName)) );
 				
 				MethodInfo parentInfo = findMethod(curEvent, prevCodeWindow.getMethodName(), curEvent.getTimestamp());
 				parentInfo.addChild(methodInfo);
 				
 				methodList.add(methodInfo);
 				
 				//------------------------------------------------------setting timstamp for codewindow
 				currentCodeWindow.setFirstTimestamp(browser.getFirstTimestamp());
 			} 
 		}
 //-----------------------------------------------------------Case: Go To Next Line-----------------------------------------------		
 		else {
 //			int line=eventUtils.getLineNum(nextEvent) - currentCodeWindow.getStartLine() - 1;
 			
 			currentCodeWindow.setLineColorToCurrent(curLine);
 			currentCodeWindow.setExecutedLine(curLine);
 			
 			setTick(currentTimeline, nextEvent);
 			
 //			currentTimeline.printCallStack(); //for debugging
 			
 			//event writes to a variable, we handle inputing/updates values on the variable pane (grid pane)
 			if(eventUtils.isWriteEvent(nextEvent)){
 				//List<LocalVariableInfo> locVariablesList=eventUtils.getLocalVariables(nextEvent);
 				String varName= eventUtils.getWriteEventVarName(nextEvent);			
 				String varValue=eventUtils.getWriteEventValue(nextEvent);
 				
 				//just a hack for now to remove "UID:" from the var value, need to find out more
 				if(varValue.contains("UID:"))
 					varValue = varValue.substring(4);
 				
 				gridPane = currentCodeWindow.getGridPane().highlightVariableValue(varName, nextEvent.getTimestamp());
 				
 				currentCodeWindow.removeHighlightedSection();
 				currentCodeWindow.highlightSection(curLine, varName);
 			}
 		};
 	}
 
 	private void reduceChildTimelines(boolean timelineExists) {
 		//reduce all child timelines of prevTimeline
 		Vector<Integer> childTimelineIdxList = prevTimeline.getChildTimelineIdxList();
 		if(!childTimelineIdxList.isEmpty() && timelineExists == false)
 		{
 			//iterate through all child timelines
 			for(int i = 0; i < childTimelineIdxList.size(); i++)
 			{
 				int index = childTimelineIdxList.get(i);
 				timeline tl = (timeline) timelineSection.getChildren().get(index);
 				tl.reduceTimeline();
 				
 				//hide any grandchildren timelines
 				hideChildTimelines(tl.getChildTimelineIdxList());
 			}
 		}
 	}
 	
 	
 	private void initializeElementControl() {
 		Button nextBtn = (Button) getRootAnchorPane().lookup("#NextBtn");
 		Button previousBtn = (Button) getRootAnchorPane().lookup("#PrevBtn");
 		Button stepOverNextBtn = (Button) getRootAnchorPane().lookup("#StepOverNextBtn");
 		Button stepOverPreviousBtn = (Button) getRootAnchorPane().lookup("#StepOverPrevBtn");
 //		Button tagBtn = (Button) getRootAnchorPane().lookup("#varTagSwitch");
 
 		nextBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 //				ILogEvent curEvent = eventUtils.getCurrentEvent();
 //				
 //				if(curEvent.getTimestamp() >= eventUtils.getLastTimestamp() - 1)//just tweaking this condition to make it work, not sure why timestamp is wrong
 //				{
 //					new Dialogue("Reached last event", "OK").show();
 //					return;
 //				}
 //
 //				// step forward and get next event
 //				ILogEvent nextEvent = eventUtils.forwardStepInto();
 //				
 //				if (nextEvent != null) 					
 //					processNextLine(curEvent, nextEvent);
 //				else
 //					new Dialogue("Reached last event", "OK").show();
 				
 				
 				
 				ILogEvent nextEvent = eventUtils.forwardStepInto();
 				naviTo(nextEvent.getTimestamp());
 			}
 		});
 		
 		stepOverNextBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 //				ILogEvent curEvent = eventUtils.getCurrentEvent();
 //				
 //				if(curEvent.getTimestamp() >= eventUtils.getLastTimestamp() - 1)//just tweaking this condition to make it work, not sure why timestamp is wrong
 //				{
 //					new Dialogue("Reached last event", "OK").show();
 //					return;
 //				}
 
 				// step forward and get next event
 				ILogEvent nextEvent = eventUtils.forwardStepOver();
 				naviTo(nextEvent.getTimestamp());
 				
 //				if (nextEvent != null) 					
 //					processNextLine(curEvent, nextEvent);
 //				else
 //					new Dialogue("Reached last event", "OK").show();
 			}
 		});
 
 		previousBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 //				ILogEvent curEvent = eventUtils.getCurrentEvent();
 //				
 //				if(curEvent.getTimestamp() <= eventUtils.getFirstTimestamp())
 //				{
 //					new Dialogue("Reached last event", "OK").show();
 //					return;
 //				}
 
 				// step backward and the the previous event
 				ILogEvent prevEvent = eventUtils.backwardStepInto();
 				naviTo(prevEvent.getTimestamp());
 				
 //				if (prevEvent != null)
 //					processPrevious(curEvent, prevEvent);
 //				else
 //					new Dialogue("Reached last event", "OK").show();
 			}
 		});
 		
 		stepOverPreviousBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 //				ILogEvent curEvent = eventUtils.getCurrentEvent();
 //				
 //				if(curEvent.getTimestamp() <= eventUtils.getFirstTimestamp())
 //				{
 //					new Dialogue("Reached last event", "OK").show();
 //					return;
 //				}
 
 				// step backward and the the previous event
 				ILogEvent prevEvent = eventUtils.backwardStepOver();
 				naviTo(prevEvent.getTimestamp());
 				
 //				if (prevEvent != null)
 //					processPrevious(curEvent, prevEvent);
 //				else
 //					new Dialogue("Reached last event", "OK").show();
 			}
 		});
 
 	}
 	
 	
 	//This method relocates the timeline position along x-axis 
 	private int timelineLocationX(String _methodName, long _timestamp){
 		int x=0;
 		
 		if(_methodName != null){
 			
 			int index = (int) displayedTimelineList.get(_methodName).get(_timestamp);
 
 			timeline s=(timeline) timelineSection.getChildren().get(index);
 			
 			//current x pos of prev timeline + ((current number of ticks - 1) * space of 1 tick) + space of the Last(selected) tick
 			x = (int) (s.getLayoutX() + s.getValue() * (5 + 3 + 5) );
 		}
 		else{//this is to handle the case for "main" method where there is no parent method
 			x=5;
 		}
 		return x;
 	}
 	
 	//This method relocates the timeline position along y-axis 
 	private int timelineLocationY(String _methodName, long _timestamp){
 		int y=0;
 		
 		if(_methodName != null){
 			
 			int index = (int) displayedTimelineList.get(_methodName).get(_timestamp);
 			
 			timeline s=(timeline) timelineSection.getChildren().get(index);
 			
 			y = (int) (s.getLayoutY()) + 34;
 			
 			//if prev timeline is main, y offset is reduced as main does not have a tail polygon
 			//temp solution until we have a better implementation
 			if(index == 0)
 				y = (int) (s.getLayoutY()) + 20;
 		}
 		else{//this is to handle the case for "main" method where there is no parent method
 
 			y = 5;
 		}
 		return y;
 	}
 	
 	// just a hack to get the main method for now
 	private ILogEvent getMainMethodEvent()
 	{
 		// find main method call event
 		ILogEvent event = null;
 		IEventBrowser eventBrowser = eventUtils.getUnfilteredEventBrowser();
 		String methodName = "main";
 
 		while (eventBrowser.hasNext()) {
 			event = eventBrowser.next();
 
 			String mName = eventUtils.getMethodName(event);
 
 			
 			if (mName != null)
 				if (mName.equalsIgnoreCase(methodName))
 					break;
 		}			
 		return event;
 	}
 
 	// get a list of child events timestamps
 	//creates a tick for each event and returns a timeline object
 	private timeline createTimeLine(ILogEvent curEvent,ILogEvent nextEvent, ArrayList<EventInfo> childEventsInfo) {
 
 //		ArrayList<Long> childEventsTimestamps = eventUtils.getChildEventTimestamps(nextEvent);
 //		ArrayList<Object[]> childEventsInfo = eventUtils.getChildEventsInfo(nextEvent);
 		
 //		ArrayList<Long> childEventsTimestamps = new ArrayList<Long>();
 //		for(EventInfo info : childEventsInfo)
 //			childEventsTimestamps.add((Long) info.getTimestamp());
 		
 		String parentMethodName = eventUtils.getMethodName(curEvent);
 		String newMethodName = eventUtils.getMethodName(nextEvent);
 		
 		//handle the case where the next event is not "main"
 		//but getting method name from the current event returns a null
 		if(newMethodName.compareToIgnoreCase("main") != 0)
 			if(parentMethodName == null)
 					parentMethodName = eventUtils.getMethodName(curEvent.getParent());
 		
 		timeline timeline = new timeline(childEventsInfo, newMethodName);
 
 		long parentTimestamp = 2671896418426L;
 		if(curEvent != null)
 			parentTimestamp = nextEvent.getParent().getTimestamp();
 		
 		//if the next method is "main", we pass in a null since there is no parent method to calculate the offset from
 		posX=timelineLocationX(parentMethodName, parentTimestamp);
 		posY=timelineLocationY(parentMethodName, parentTimestamp);
 		
 		timelineSection.getChildren().add(timeline);
 		timeline.relocate(posX,posY);
 		
 		//store the index of the timeline in container (timelineSection)
 		int timelineIndex = timelineSection.getChildren().size() - 1;
 		HashMap<Long, Integer> methodTimelineIndexes;
 		
 		if(displayedTimelineList.containsKey(newMethodName))
 		{
 			methodTimelineIndexes = displayedTimelineList.get(newMethodName);
 			methodTimelineIndexes.put(nextEvent.getTimestamp(), timelineIndex);
 		}
 		else
 		{
 			methodTimelineIndexes = new HashMap<Long, Integer>();		
 			methodTimelineIndexes.put(nextEvent.getTimestamp(), timelineIndex);
 		}		
 				
 		displayedTimelineList.put(newMethodName, methodTimelineIndexes);
 
 		return timeline;
 	}
 	
 	private void hideChildTimelines(Vector<Integer> _childIdxList)
 	{
 		if(_childIdxList.isEmpty())
 			return;
 		
 		//iterate through all the child timelines
 		for(int i = 0; i < _childIdxList.size(); i++)
 		{
 			int index = _childIdxList.get(i);
 			timeline t = (timeline) timelineSection.getChildren().get(index);
 			t.hideTimeline();
 			
 			Vector<Integer> grandChildIdxList = t.getChildTimelineIdxList();
 			hideChildTimelines(grandChildIdxList);
 		}	
 		return;
 	}
 	
 	private void printCallStack()
 	{
 		for(CodeWindow codeWin : CodeWindowCallStack)
 			System.out.println(codeWin.getMethodName() + "\n");
 	}
 	
 	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * navigate to the execution of an event give event and timestamp
 	 * sets timeline, codewindow area and variable pane
 	 * @param event
 	 * @param _timestamp
 	 */
 	static public void naviTo(long _timestamp)
 	{
 		ILogEvent event = eventUtils.getEventFromTimestamp(_timestamp);
 		String parentMethodName = eventUtils.getMethodName(event.getParent());
 		//if method name is null, we get the name of the parent method
 		if(parentMethodName == null)
 			parentMethodName = eventUtils.getMethodName(event.getParent());
 		MethodInfo method = findMethod(event, parentMethodName, _timestamp);
 		
 		// we found the method, 
 		if(method != null)
 		{
 			//is it the same as the current method
 			if(currentCodeWindow.getMethodName().compareToIgnoreCase(method.methodName()) == 0 &&
 					currentCodeWindow.firstTimestamp() <= method.FirstEventTimestamp())
 			{
 				//set timeline, returns line to be executed (raw, not corrected for codeWindow)
 				int rawLineNum = currentTimeline.setTick(_timestamp);
 				
 				//set current window
 				updateCurrentWindow(rawLineNum);
 				
 				//reset all highlight in variable pane
 				gridPane.removeGridStyles();
 				
 				//check if it is a write event
 				if(eventUtils.isWriteEvent(event))
 					writeEvent(event, _timestamp, rawLineNum);
 				else if(!codeFragments.codeFragmentExist(eventUtils.getMethodName(event)))//else is it a system method call
 					systemMethodCall(event);
 			
 			}else //is a different method that exists
 			{
 				setOldPreviousInactive();
 				
 				//set current codeWindow to inactive (Grey)
 				setCurrentMethodInactive();
 				
 				//set next codeWindow to active (green)
 				setNextMethodActive(method);
 				
 				setPrevMethodPrev(method, event);
 				
 				//nav to the event
 				naviTo(_timestamp);
 			}
 		}
 		else //method (or method instance) does not exist, create new method
 		{
 			createNewMethod(parentMethodName, event, _timestamp);
 			//highlight gutters
 			highlightGutters(event.getParent());
 		}
 		
 		//update Event Stepper
 		eventUtils.setCurrentEvent(event);
 	}
 	
 	/**
 	 * iterate through the list of method instances
 		compare the start and end timestamp of each method to find the one this event belongs to
 	 * @param _timestamp
 	 * @return MethodInfo object, null if not found
 	 */
 	private static MethodInfo findMethod(ILogEvent _event, String _methodName, long _timestamp)
 	{			
 		MethodInfo method = null;		
 		
 		for(int i = 0; i < methodList.size(); i++)
 		{
 			MethodInfo mInfo = methodList.get(i);
 			
 			//check it is the correct method and correct instance of the method
 //			if(eventUtils.validTimestamp(_timestamp))
 				if(_methodName.compareToIgnoreCase(mInfo.methodName()) == 0)
 					if(mInfo.FirstEventTimestamp() - 5 <= _timestamp && mInfo.LastEventTimestamp() + 5 >= _timestamp) //i +/- 5 because the timestamp seems to be off by just 1 so use this fix for now
 					{
 						method = mInfo;
 						break;
 					}
 		}
 		
 		if(method == null)//if method it is a method call but a system call (code fragment does not exist)		
 			if(eventUtils.isMethodCall(_event) &&
 					!codeFragments.codeFragmentExist(_methodName))
 			{
 				ILogEvent parent = _event.getParent();
 				if(parent != null)
 					return findMethod(parent, eventUtils.getMethodName(parent), parent.getTimestamp());
 				else
 					return null;
 			}
 		
 		return method;
 	}
 	
 	private static void updateCurrentWindow(int _lineNum)
 	{
 		//clear all lines
 		currentCodeWindow.clearAllLineHighlights();
 		//highlight cur line
 		currentCodeWindow.setLineColorToCurrent2(_lineNum);
 		currentCodeWindow.setExecutedLine(_lineNum);
 		
 		currentCodeWindow.removeHighlightedSection();
 	}
 	
 	private static void setNextMethodActive(MethodInfo _method)
 	{
 		//get current method
 		CodeWindow newCodeWindow = (CodeWindow) codeWindowArea.getChildren().get(_method.getCodeWindowIdx());
 		
 		timeline newTimeline = (timeline) timelineSection.getChildren().get(_method.getTimelineIdx());
 		
 		//set as current
 		currentCodeWindow = newCodeWindow;
 		currentTimeline = newTimeline;
 		
 		if(currentCodeWindow != mainCWH)
 		{
 			//set background colour
 			currentCodeWindow.setBackgroundColorToCurrent();
 			currentTimeline.setColor("A3FF7F");
 		}
 		
 		//minimize tick
 		
 		//maxmize window
 		currentCodeWindow.normalWindowSize();		
 		
 		//set variable pane
 		gridPane = currentCodeWindow.getGridPane();
 		variablePane.getChildren().set(0,gridPane);
 		
 		//reset all highlights in variable pane
 		gridPane.removeGridStyles();
 	}
 	
 	private static void setCurrentMethodInactive()
 	{
 		if(currentCodeWindow != mainCWH)
 		{
 			//set background colour
 			currentCodeWindow.setBackgroundColorToInactive();
 			//timeline
 			currentTimeline.setColor("CCCCCC");
 			
 			//minimize window
 			currentCodeWindow.reduceWindowSize();
 		}	
 		//minimize all timeline children
 		
 		//minimize tick
 		currentTimeline.clearTick();
 		
 		//store variable pane
 		currentCodeWindow.setGridPane(gridPane);
 		
 		//stash as previous method
 		prevCodeWindow = currentCodeWindow;
 		prevTimeline = currentTimeline;
 		
 		
 	}
 	
 	private static void setPrevMethodPrev(MethodInfo _method, ILogEvent _event)
 	{
 		//check if it is a method return
 		//if previous method is a child of current method, it is a method return
 		boolean isMethodReturn = false;
 		ArrayList<MethodInfo> childMethodList = _method.getChildList();
 		for (int i = 0; i < childMethodList.size(); i++) {
 			if(prevCodeWindow.getMethodName().compareToIgnoreCase(childMethodList.get(i).methodName()) == 0)
 			{
 				isMethodReturn = true;
 				break;
 			}
 		}
 		
 		if(!isMethodReturn)
 		{
 			//find previous codeWindow
 //				MethodInfo curMethod = findMethod(event, methodName, _timestamp);
 			ILogEvent parentEvent = _event.getParent();
 			MethodInfo previousMethod = findMethod(parentEvent, _method.getParentName(), _method.ParentMethodfirstTimestamp());
 			
 		
 			//set previous codeWindow as prev (yellow)
 			if(previousMethod != null)
 			{
 				//get previous method
 				prevCodeWindow= (CodeWindow) codeWindowArea.getChildren().get(previousMethod.getCodeWindowIdx());					
 				prevTimeline = (timeline) timelineSection.getChildren().get(previousMethod.getTimelineIdx());
 			}
 		}
 		
 		if(prevCodeWindow != mainCWH)
 		{
 			//set background colour
 			prevCodeWindow.setBackgroundColorToPrevious();
 			prevTimeline.setColor("FFFB78");
 		}	
 			//maxmize window
 		prevCodeWindow.normalWindowSize();	
 		
 		prevTimeline.clearTick();
 		
 	}
 	
 	private static void setOldPreviousInactive()
 	{
 		if(prevCodeWindow == null)
 			return;
 		
 		if(prevCodeWindow != mainCWH)
 		{
 			//set old previous to inactive
 			prevCodeWindow.setBackgroundColorToInactive();
 			prevTimeline.setColor("CCCCCC");
 			
 			prevCodeWindow.reduceWindowSize();	
 		}
 		prevTimeline.clearTick();
 	}
 	
 	private static void systemMethodCall(ILogEvent _event)
 	{
 		if(_event instanceof MethodCallEvent)
 		{
 			MethodCallEvent methodCall = (MethodCallEvent) _event;
 			ObjectId theObjectId = (ObjectId) methodCall.getArguments()[0];
 			ILogBrowser browser = eventUtils.getLogBrowser();
 			Object theRegistered = browser.getRegistered(theObjectId);
 			String value = null;
 			if (theRegistered != null) 
 				value = theRegistered.toString();
 			
 			Label console = (Label) getRootAnchorPane().lookup("#Console");
 			console.setText(console.getText().concat(value));
 			console.setPrefHeight(50);
 		}
 	}
 	
 	private static void writeEvent(ILogEvent _event, long _timestamp, int _linNum)
 	{
 			String varName= eventUtils.getWriteEventVarName(_event);			
 			String varValue=eventUtils.getWriteEventValue(_event);
 		
 			gridPane = currentCodeWindow.getGridPane().highlightVariableValue(varName, _timestamp);
 			currentCodeWindow.removeHighlightedSection();
 			currentCodeWindow.highlightSection2(_linNum, varName);		
 	}
 	
 	private static void createNewMethod(String _methodName, ILogEvent _event, long _timestamp)
 	{
 		//create the new codeWindow
 		CodeWindow newCodeWindow = codeFragments.createCodeWindow(_methodName);
 		//add new window to codeWindow area
 		codeWindowArea.getChildren().add(newCodeWindow);
 		//add arrow from current window to new window
 		Arrow arrowNew = new Arrow(currentCodeWindow, newCodeWindow);
 		codeWindowArea.getChildren().add(arrowNew);
 		
 		//create new timeline
 		ArrayList<EventInfo> childEventsInfo = eventUtils.getChildEventsInfo(_event.getParent());
 		timeline newTimeline = new timeline(childEventsInfo, _methodName);
 		//add timeline to area
 		timelineSection.getChildren().add(newTimeline);
 		//need to handle reducing child timelines
 		
 		
 		
 		//create new variable pane
 		newCodeWindow.getGridPane(childEventsInfo);
 		
 		//create it's method info
 		//since this is the 1st event in the new method, we need to get the parent event which is the method call
 		MethodCallEvent methodCall = (MethodCallEvent) _event.getParent();
 		IEventBrowser browser = methodCall.getChildrenBrowser();
 		ILogEvent parent = _event.getParent();
 			
 		//the current codeWindow will be come the parent of the new method
 		MethodInfo methodInfo = new MethodInfo(browser.getFirstTimestamp(), browser.getLastTimestamp(), 
 				currentCodeWindow.firstTimestamp(), currentCodeWindow.lastTimestamp(), 
 				_methodName, currentCodeWindow.getMethodName(), 
 				timelineSection.getChildren().size() - 1, codeWindowArea.getChildren().size() - 2); //-2 since we added the arrow after codewindow
 		
 		MethodInfo parentInfo = findMethod(parent, currentCodeWindow.getMethodName(), parent.getTimestamp());
 		parentInfo.addChild(methodInfo);
 		
 		methodList.add(methodInfo);
 		//store method info
 		
 		newCodeWindow.setFirstTimestamp(browser.getFirstTimestamp());
 		newCodeWindow.setLastTimestamp(browser.getLastTimestamp());
 		
 		//position codewindow
 		int cwX = calculateWindowX(currentCodeWindow);
 		int cwY = calculateWindowY(currentCodeWindow, currentCodeWindow.getCurrentExecutionLine());
 		
 		newCodeWindow.relocate(cwX, cwY);
 		//set the x and y property of the parent DraggableNode class
 		newCodeWindow.getDraggableX().set(cwX);
 		newCodeWindow.getDraggableY().set(cwY);
 		
 		//position timeline
 		//if timeline area empty - we are adding main
 		if(timelineSection.getChildren().size() == 0)
 			newTimeline.relocate(5, 5);
 		else
 		{
 			//find parent timeline location
 			int tX = (int) currentTimeline.getLayoutX();
 			int tY = (int) currentTimeline.getLayoutY();
 			//calculate timeline offset
 			tX = tX + currentTimeline.getValue() * (5 + 3 + 5);
 			
 			if(timelineSection.getChildren().size() == 1) //if previous timeline is main, we reduce y offset (main has no tail)
 				tY = tY + 20;
 			else
 				tY = tY + 34;
 			//reposition
 			newTimeline.relocate(tX, tY);
 		}
 		
 		//call naviTo - takes care of highlighting
 		naviTo(_timestamp);
 	}
 
 }
