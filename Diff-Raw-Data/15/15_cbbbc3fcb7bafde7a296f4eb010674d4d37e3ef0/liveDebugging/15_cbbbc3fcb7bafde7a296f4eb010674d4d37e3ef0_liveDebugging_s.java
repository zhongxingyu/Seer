 
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
 import javafx.stage.Stage;
 
 public class liveDebugging extends Application {
 
 	private Parent root = null;
 	private static String[] _args;
 
 	int currentWindowIdx = 0;
 
 	Stack<CodeWindow> codeWindowStack;
 	Stack<timeline> timelineStack;
 	
 	CodeWindow currentCodeWindow;
 	CodeWindow prevCodeWindow;
 	
 	timeline currentTimeline;
 	timeline prevTimeline;	
 
 	
 	Pane codeWindowAreaNew;
 	Pane codeWindowArea=new Pane ();
 
 	EventUtils eventUtils;
 	CodeFragments codeFragments;
 
 	int lineNumberOffset;
 	private HashMap displayedCodeWindowsList; //list of code windows currently displayed on screen
 	private ArrayList<CodeWindow> CodeWindowCallStack; //maintains a list that represents the call stack
 	
 	private TreeMap<String, HashMap> displayedTimelineList;//list of timelines displayed on screen, value = index of element in container (latest instance of a particular timeline)
 	Pane timelineSection=new Pane ();
 
 	//timeline positions
 	double posX=37;
 	int posY=0;
 	Pane variablePane =new Pane();
 
 	//for Highlighting
 	CodeWindow mainCWH;
 	timeline mainTH;
 
 	ScrollPane s1 = new ScrollPane();
 	GridPane gridPane=new GridPane(); 
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
 		Path classPath = FileSystems.getDefault().getPath("resource", "sampleJs.txt");
 		codeFragments = new CodeFragments(classPath, this);
 		
 		//initialize the variables
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
 //		mainCWH=currentCodeWindow;
 
 		//initialize variable pane window
 		variablePane = (Pane) getRootAnchorPane().lookup("#VariablePane");
 		gridPane = currentCodeWindow.initGridPane();
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
 		timeline tLine = createTimeLine(null, mainEvent);
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
 
 	private AnchorPane getRootAnchorPane() {
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
 	private void highlightGutters(ILogEvent event){
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
 			if(oldPreviousWindow!=currentCodeWindow){
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
 			int lineNum = eventUtils.getLineNum(prevEvent);
 			
 //			lineNumberOffset = codeFragments.getLineNumberOffset(currentCodeWindow.getMethodName());
 			int line=lineNum - currentCodeWindow.getStartLine()- 1;
 			
 			currentCodeWindow.setLineColorToCurrent(line);
 			
 			currentCodeWindow.setExecutedLine(line);//so we know the start point of the arrow
 			
 			if(oldPreviousTimeline != mainTH)
 				oldPreviousTimeline.setColor("CCCCCC");
 			
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
			//** should use timestamp to get the correct timeline
 			long timestamp = prevEvent.getParent().getTimestamp();
			HashMap<Long, Integer> hash = displayedTimelineList.get(methodName);
			int index = hash.get(timestamp);
 			timeline tLine = (timeline) timelineSection.getChildren().get(index);
 			
 			timeline oldPrevTimeline = prevTimeline;
 			prevTimeline=currentTimeline;
 			currentTimeline= tLine;
 			
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
 				
 //			int prevLine=eventUtils.getLineNum(prevEvent) - currentCodeWindow.getStartLine() - 1;
 //			int curLine=eventUtils.getLineNum(curEvent) - currentCodeWindow.getStartLine() - 1;
 			
 			//highlight the previous line, set current line to white
 			currentCodeWindow.setLineColorToPrevious(curLine);
 			currentCodeWindow.setLineColorToCurrent(prevLine);
 			currentCodeWindow.setExecutedLine(prevLine);
 			
 			setTick(currentTimeline, prevEvent);
 						
 			//event writes to a variable, we handle inputing/updates values on the variable pane (grid pane)
 			if(eventUtils.isWriteEvent(prevEvent)){
 				//List<LocalVariableInfo> locVariablesList=eventUtils.getLocalVariables(nextEvent);
 				String varName= eventUtils.getWriteEventVarName(prevEvent);		
 			
 				
 				LinkedHashMap<String,ArrayList> localVariableInfo=currentCodeWindow.getLocalVariables();
 					
 					//variable may not exist in our list in which index will be -1
 					int index=returnIndexofValue(varName,localVariableInfo);
 					//variable exists
 					if(index!=-1){
 				        index=2+2*index+2;
 				        
 				        removeGridStyles(gridPane);
 				        
 				        //highlight the variable name that is being written to
 				        Label nameLabel = (Label)gridPane.getChildren().get(index-1);	
 						nameLabel.setTextFill(Color.web("#ff9999"));
 						
 						//highlight and set the new value in the combo box
 				        ComboBox valueBox= (ComboBox)gridPane.getChildren().get(index);				        
 				        valueBox.setScaleX(1);
 				        valueBox.setScaleY(1);
 				        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");    
 				        //display the previous written value
 				        //**this is not correct, we need a new impl
 				        if(valueBox.getItems().size() > 1)
 				        	valueBox.setValue( valueBox.getItems().get(valueBox.getItems().size() - 1) );
 				        
 				        //update the elements being displayed
 				        gridPane.getChildren().set(index-1, nameLabel);
 				        gridPane.getChildren().set(index, valueBox);
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
 	private int calculateWindowX(CodeWindow cd) {
 		CodeEditor ed= cd.getEditor();
 		if(!ed.isReduced()){
 			return	(int)cd.getLayoutX()+630;
 		}else
 			return (int)(cd.getLayoutX()+ cd.getWindowWidth() +80);
 	}
 	
 	//calculate the position of code fragment window at y axis
 	private int calculateWindowY(CodeWindow cd,int linenum) {
 		// TODO Auto-generated method stub
 		CodeEditor ed = cd.getEditor();
 //		if(!ed.isReduced()){
 		return (int)cd.getLayoutY()+(linenum+1)*cd.lineoffset.getValue() + 30;
 //		}
 //		else
 //			return (int)(cd.getLayoutY()+ ed.getHeight() + 80);//(int)(((int)ed.getLayoutY()+ed.getHeight()));//(linenum+1)*9)*0.3);
 
 	}
 	//elocate all the existing added arrows and code fragment windows over the screen
 	public void reposition(){
 		
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
 	
 	//helping function which returns index value of some variable name
 	private int returnIndexofValue(String name,LinkedHashMap<String,ArrayList> localVariableInfo){
 			int i=0;
 			Iterator keys=localVariableInfo.keySet().iterator();
 			while(keys.hasNext()){
 				String variableValue=(String)keys.next();
 				if(variableValue.equals(name)){
 					return i; 
 				}
 				i++;
 			}
 		
 	return -1;	
 	}
 	
 	//This method is used to initialize grid on moving towards another program fragment window, because we are showing grid pane of local variables for every program fragment window. 
 	private void initializeGrid(){
 		 gridPane=currentCodeWindow.getGridPane();
 		 variablePane.getChildren().set(0,gridPane);
 	}
 	
 	//This method is used to change the style of grid pane
 	private void removeGridStyles(GridPane gp){
 		for(int i=3;i<gp.getChildren().size();i=i+2){
 			Label label=(Label)gp.getChildren().get(i);
 			ComboBox cb=(ComboBox)gp.getChildren().get(i+1);
 			label.setTextFill(Color.web("#000000"));
 			cb.setStyle("-fx-font-size: 10px");
 			gp.getChildren().set(i,label);
 			gp.getChildren().set(i+1,cb);
 		}
 	}
 	//This method is used to proceed towards a next line
 	private void processNextLine(ILogEvent curEvent, ILogEvent nextEvent) {
 		
 		int clineNum = eventUtils.getLineNum(curEvent);
 		
 		//handle special case - check if this is still valid
 		if (clineNum - lineNumberOffset < 0) {
 
 			clineNum = lineNumberOffset + 1;
 		}
 		
 		//sets current line to white - remove highlight
 		currentCodeWindow.setLineColorToPrevious(currentCodeWindow.getExecutedLine());
 		
 //-----------------------------------------------------------Case: Method return-----------------------------------------------		
 
 		//if depth of next event > current, we have returned to parent method
 		if (curEvent.getDepth() > nextEvent.getDepth()) {
 			
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
 			if(oldPreviousWindow!=currentCodeWindow){
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
 				
 				currentCodeWindow.setExecutedLine(nextLine);
 			}
 			//ELSE it is a method call and we need to add a new code window
 			else {
 				
 				
 				
 				CodeWindow codeWin = null;				
 				boolean addedNewWindow = false;
 				
 				//if code window does not exist in display container
 				//add new code window
 				if(!displayedCodeWindowsList.containsKey(methodName))
 				{
 					// get the code window 
 					codeWin = codeFragments.getCodeFragment(methodName);					
 	
 					//add the new code window to the screen, store it's position index in the container
 					codeWindowArea.getChildren().add(codeWin);
 					codeWin.setIndexOnScreen(codeWindowArea.getChildren().size() - 1);// IndexOnScreen = index of element (eg. codeWindow) as a child of the code area pane
 					//add code window to list of displayed windows and store it's position in the container
 					displayedCodeWindowsList.put(methodName, codeWindowArea.getChildren().size() - 1);
 					
 					addedNewWindow = true;
 				}
 				else //code window already exists in display container
 					//get the code window and increment it's iteration
 				{ 
 					codeWin = (CodeWindow) codeWindowArea.getChildren().get((int) displayedCodeWindowsList.get(methodName));
 					
 					codeWin.incrementIteration();
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
 				
 				if(!timelineExists) //else create new timeline
 				{
 					tLine = createTimeLine(curEvent, nextEvent);
 				}
 
 				timeline oldPrevTimeline = prevTimeline;
 				prevTimeline=currentTimeline;
 				currentTimeline= tLine;
 				
 				reduceChildTimelines(timelineExists);
 				
 				//add the idx to the new timeline to the list of children
 				prevTimeline.setChildTimelineIdx(timelineSection.getChildren().size() - 1);
 				
 				timelineStack.push(prevTimeline);
 				
 				//set timeline colors
 				if(oldPrevTimeline != null && oldPrevTimeline != mainTH)
 					oldPrevTimeline.setColor("CCCCCC");
 				if(prevTimeline != null && prevTimeline != mainTH)
 					prevTimeline.setColor("FFFB78");
 				currentTimeline.setColor("A3FF7F");
 				
 				
 				highlightGutters(nextEvent);
 				initializeGrid();
 				reposition();
 				
 			} 
 		}
 //-----------------------------------------------------------Case: Go To Next Line-----------------------------------------------		
 		else {
 			int line=eventUtils.getLineNum(nextEvent) - currentCodeWindow.getStartLine() - 1;
 			
 			currentCodeWindow.setLineColorToCurrent(line);
 			currentCodeWindow.setExecutedLine(line);
 			
 			setTick(currentTimeline, nextEvent);
 			
 			currentTimeline.printCallStack(); //for debugging
 			
 			//event writes to a variable, we handle inputing/updates values on the variable pane (grid pane)
 			if(eventUtils.isWriteEvent(nextEvent)){
 				//List<LocalVariableInfo> locVariablesList=eventUtils.getLocalVariables(nextEvent);
 				String varName= eventUtils.getWriteEventVarName(nextEvent);			
 				String varValue=eventUtils.getWriteEventValue(nextEvent);
 				
 				//just a hack for now to remove "UID:" from the var value, need to find out more
 				if(varValue.contains("UID:"))
 					varValue = varValue.substring(4);
 				
 				LinkedHashMap<String,ArrayList> localVariableInfo=currentCodeWindow.getLocalVariables();
 				
 				//if the variable to be written to exist in our local var hashmap, append the value to it
 				if(!localVariableInfo.isEmpty()&&localVariableInfo.containsKey(varName)){
 					ArrayList values=(ArrayList)localVariableInfo.get(varName);
 					if(varValue!=null){
 						values.add(varValue);
 					}
 					else{
 						values.add("null");
 					}
 					//update the entry in local variable hashmap
 					localVariableInfo.put(varName, values);
 					
 					//variable may not exist in our list in which index will be -1
 					int index=returnIndexofValue(varName,localVariableInfo);
 					//variable exists
 					if(index!=-1){
 				        currentCodeWindow.setLocalVariables(localVariableInfo);//set the updated variable hashmap
 				        index=2+2*index+2;
 				        
 				        removeGridStyles(gridPane);
 				        
 				        //highlight the variable name that is being written to
 				        Label nameLabel = (Label)gridPane.getChildren().get(index-1);	
 						nameLabel.setTextFill(Color.web("#ff9999"));
 						
 						//highlight and set the new value in the combo box
 				        ComboBox valueBox= (ComboBox)gridPane.getChildren().get(index);				        
 				        valueBox.setScaleX(1);
 				        valueBox.setScaleY(1);
 				        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");    
 				        valueBox.getItems().add(varValue);
 				        valueBox.setValue(varValue);
 				        
 				        //update the elements being displayed
 				        gridPane.getChildren().set(index-1, nameLabel);
 				        gridPane.getChildren().set(index, valueBox);
 			        }
 				}
 				else{ //if it does not exist in our hashmap, create the elements and add it to the variable pane
 					
 					//each time we add a variable we add 2 child elements
 					//number of rows = no. of child elements / 2
 					int rows=((gridPane.getChildren().size())/2);
 					
 					Label nameLabel = new Label(varName);
 					
 					//remove previous highlights and highlight the new var
 					removeGridStyles(gridPane);
 					nameLabel.setTextFill(Color.web("#ff9999"));
 					
 			        			        
 			        ComboBox valueBox= new ComboBox();
 			        valueBox.setScaleX(1);
 			        valueBox.setScaleY(1);
 			        valueBox.setStyle("-fx-font-size: 10px; -fx-background-color: #ff9999");
 			        valueBox.getItems().add(varValue);
 			        valueBox.setValue(varValue);
 			        
 			        gridPane.setMargin(nameLabel, new Insets(10, 10, 10, 10));
 			        gridPane.setMargin(valueBox, new Insets(10, 10, 10, 10));
 			        
 			        gridPane.add(nameLabel, 0, rows);
 			        gridPane.add(valueBox, 1, rows);
 			       
 			       
 			       //add the new var and value in local var hashmap and update the hashmap in the current code window
 			       ArrayList values=new ArrayList();
 			       values.add(varValue);
 			       localVariableInfo.put(varName, values);
 			       currentCodeWindow.setLocalVariables(localVariableInfo);
 				}
 
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
 	
 	//This method is used to create arrow head
 	//additional note, arrows need indexOnScreen as well to get them from the child array and reposition them
 //	public static Polygon createUMLArrow() {
 //		Polygon polygon = new Polygon(new double[]{
 //				7.5, 0,
 //				15, 15,
 //				7.51, 15,
 //				7.51, 40,
 //				7.49, 40,
 //				7.49, 15,
 //				0, 15
 //		});
 //		polygon.setFill(Color.WHITE);
 //		polygon.setStroke(Color.BLACK);
 //		polygon.setRotate(90);
 //		return polygon;
 //	}
 	
 	private void initializeElementControl() {
 		Button nextBtn = (Button) getRootAnchorPane().lookup("#NextBtn");
 		Button previousBtn = (Button) getRootAnchorPane().lookup("#PrevBtn");
 		Button tagBtn = (Button) getRootAnchorPane().lookup("#varTagSwitch");
 
 		nextBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 				ILogEvent curEvent = eventUtils.getCurrentEvent();
 				stepForward(curEvent);
 			}
 
 			
 		});
 
 		previousBtn.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent e) {
 				ILogEvent curEvent = eventUtils.getCurrentEvent();
 				stepBackward(curEvent);
 				
 				
 			}
 
 			
 
 		});
 
 	}
 	
 	void stepForward(ILogEvent curEvent) {
 		// step forward and get next event
 		ILogEvent nextEvent = eventUtils.forwardStepInto();
 		
 		if (nextEvent != null) {
 
 				int clineNum = eventUtils.getLineNum(curEvent);
 				int nextlineNum = eventUtils.getLineNum(nextEvent);
 				
 					processNextLine(curEvent, nextEvent);
 
 //			}
 
 		}
 	}
 	
 	void stepBackward(ILogEvent curEvent) {
 		// step backward and the the previous event
 		ILogEvent prevEvent = eventUtils.backwardStepInto();
 		
 		if (prevEvent != null) {
 			
 			int clineNum = eventUtils.getLineNum(curEvent);
 			int prevLineNum = eventUtils.getLineNum(prevEvent);	
 			
 			processPrevious(curEvent, prevEvent);
 	
 //			if(mainCWH!=currentCodeWindow ){
 //				ColorAdjust c=new ColorAdjust(0.426,0.63,0.075,0.015);
 //				String color="A3FF7F";//green
 //
 ////				highlighter(currentCodeWindow,currentTimeline,c,color);
 //			}
 //			if(mainCWH!=prevCodeWindow){
 //				ColorAdjust c=new ColorAdjust(0.3,1,0.218,0.38);
 //				String color="FFFB78";//yellow
 ////				highlighter(prevCodeWindow,prevTimeline,c,color);
 //			}
 			
 		}
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
 	private timeline createTimeLine(ILogEvent curEvent,ILogEvent nextEvent) {
 
 		ArrayList<Long> childEventsTimestamps = eventUtils.getChildEventTimestamps(nextEvent);
 		
 		String parentMethodName = eventUtils.getMethodName(curEvent);
 		String newMethodName = eventUtils.getMethodName(nextEvent);
 		
 		//handle the case where the next event is not "main"
 		//but getting method name from the current event returns a null
 		if(newMethodName.compareToIgnoreCase("main") != 0)
 			if(parentMethodName == null)
 					parentMethodName = eventUtils.getMethodName(curEvent.getParent());
 		
 		timeline timeline = new timeline(childEventsTimestamps, newMethodName, CodeWindowCallStack, nextEvent.getTimestamp());
 
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
 
 }
