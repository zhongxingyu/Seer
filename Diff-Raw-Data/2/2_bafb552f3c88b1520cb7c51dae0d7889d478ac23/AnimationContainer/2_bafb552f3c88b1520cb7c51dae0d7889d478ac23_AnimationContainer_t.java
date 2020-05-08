 package main.view;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import main.AnimatedFace;
 import main.animation.AnimationStep;
 import main.animation.Change;
 import main.xml.XmlFactory;
 
 
 public class AnimationContainer extends Container {
 
	private final static String EDITOR="Switch to Simulation",SIMUL="Switch to Editor";
 
 	private Window window;
 	private JSlider timeSlider;
 	private JButton stateButton;
 	private JButton newStepButton;
 	private JSpinner timeToggle;
 	private JComboBox<AnimationStep> keyFrameList;
 
 	private Vector<Integer> lastValues;
 
 	private HtmlLabel maxTimeLabel;
 
 	private JButton playButton;
 
 	public AnimationContainer(final Window window) {
 		this.window=window;
 
 		setLayout(new GridLayout(2,1));
 
 		Container c1=new Container();
 		c1.setLayout(new BorderLayout());
 
 		c1.add(new HtmlLabel(AnimatedFace.toSecondsAndFrames(0)),BorderLayout.WEST);
 
 		c1.add(timeSlider=new JSlider(),BorderLayout.CENTER);
 		timeSlider.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent evt) {
 				try {
 					updateTimeLabel(((Integer)timeSlider.getValue()).intValue(),AnimatedFace.currentAnimation.getNumberOfFrames());
 				} catch (NullPointerException e) {
 				}
 			}
 		});
 		timeSlider.setValue(0);
 		timeSlider.setMaximum(0);
 		timeSlider.setEnabled(false);
 		timeSlider.setMinorTickSpacing(1);
 		timeSlider.setMajorTickSpacing(AnimatedFace.FPS);
 		timeSlider.setPaintTicks(true);
 		timeSlider.setSnapToTicks(true);
 		timeSlider.setPaintTrack(false);
 
 		c1.add(maxTimeLabel=new HtmlLabel("--:-- / --:--"),BorderLayout.EAST);
 
 		add(c1);
 
 		Container c2=new Container();
 		c2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
 
 		c2.add(stateButton=new JButton(AnimatedFace.editorMode?EDITOR:SIMUL));
 		stateButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				AnimatedFace.editorMode=!AnimatedFace.editorMode;
 				stateButton.setText(AnimatedFace.editorMode?EDITOR:SIMUL);
 				window.controlContainer.setEnabled(AnimatedFace.editorMode);
 				newStepButton.setEnabled(AnimatedFace.editorMode);
 				playButton.setEnabled(!AnimatedFace.editorMode);
 			}
 		});
 
 		c2.add(newStepButton=new JButton("Add scene to animation"));
 		newStepButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				addCurrentStep();
 			}
 		});
 
 		c2.add(timeToggle=new JSpinner());
 		timeToggle.setModel(new SpinnerNumberModel(0, 0, AnimatedFace.FPS*AnimatedFace.MAX_SEC, 1));
 		timeToggle.addMouseWheelListener(new MouseWheelListener() {
 
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent evt) {
 				if(timeToggle.isEnabled())
 				{
 					Integer newValue=new Integer(((Integer)timeToggle.getValue()).intValue()-evt.getWheelRotation());
 					if(newValue.intValue()>=0&&newValue.intValue()<=AnimatedFace.FPS*AnimatedFace.MAX_SEC)
 					{
 						timeToggle.setValue(newValue);
 					}
 				}
 			}
 		});
 		timeToggle.setEnabled(false);
 
 		c2.add(keyFrameList=new JComboBox<AnimationStep>());
 		keyFrameList.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					//					System.out.println(keyFrameList.getSelectedItem()+"\t"+AnimatedFace.framesfromString((String) keyFrameList.getSelectedItem().toString()));
 				} catch (NullPointerException e) {
 				}
 			}
 		});
 
 		c2.add(playButton=new JButton("Play"));
 		playButton.setEnabled(false);
 		playButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				startAnimation();
 			}
 		});
 
 		//		addCurrentStep();
 
 		add(c2);
 	}
 
 	protected void startAnimation() {
 		AnimatedFace.playCurrentAnimation(playButton,timeSlider);
 	}
 
 	protected void addCurrentStep(){
 		addCurrentStep(getTime());
 		timeToggle.setEnabled(true);
 	}
 
 	protected void addCurrentStep(int time) {
 		AnimationStep newStep=new AnimationStep(time);
 		if(lastValues==null)
 		{
 			lastValues=new Vector<Integer>();
 			for(int i=0;i<window.controlContainer.getNumberOfValues();i++)
 			{
 				lastValues.add(new Integer(window.controlContainer.getValue(i)));
 				newStep.addChange(new Change(i, window.controlContainer.getValue(i)));
 			}
 		}
 		else
 		{
 			for(int i=0;i<window.controlContainer.getNumberOfValues();i++)
 			{
 				if(lastValues.get(i).intValue()!=window.controlContainer.getValue(i))
 				{
 					newStep.addChange(new Change(i, window.controlContainer.getValue(i)-lastValues.get(i)));
 					lastValues.setElementAt(new Integer(window.controlContainer.getValue(i)), i);
 				}
 			}
 		}
 		AnimatedFace.currentAnimation.addStep(newStep);
 
 		update();
 
 	}
 
 	public void update() {
 		updateTimeSlider();
 
 		/*
 		 * update the list of key frames
 		 */
 		keyFrameList.removeAllItems();
 		for(AnimationStep step:AnimatedFace.currentAnimation.getSteps())
 		{
 			keyFrameList.addItem(step);
 		}
 	}
 
 	public void updateTimeSlider() {
 		int newMaxTime=AnimatedFace.currentAnimation.getNumberOfFrames();
 		timeSlider.setEnabled(newMaxTime>0);
 		timeSlider.setMaximum(newMaxTime);
 		if(timeSlider.getValue()>newMaxTime)
 		{
 			timeSlider.setValue(newMaxTime);
 		}
 		updateTimeLabel(timeSlider.getValue(),newMaxTime);
 	}
 
 	public void updateTimeLabel(int currentTime, int maxTime) {
 		maxTimeLabel.setText(AnimatedFace.toSecondsAndFrames(currentTime)
 				+" / "+AnimatedFace.toSecondsAndFrames(maxTime));
 	}
 
 	public int getTime() {
 		return ((Integer)timeToggle.getValue()).intValue();
 	}
 
 	public int getLastValue(int valueID)
 			throws Exception{
 		return lastValues.get(valueID).intValue();
 	}
 
 	public void stopAnimation() {
 		playButton.setEnabled(true);
 		timeSlider.setEnabled(true);
 	}
 
 	public void moveTimeSlider(int value) {
 		timeSlider.setValue(value);
 	}
 
 	public void loadTemplate(File file) 
 	{
 		BufferedReader reader;
 		try 
 		{
 			String content="",line="";
 			reader=new BufferedReader(new FileReader(file));
 			while((line=reader.readLine())!=null)
 			{
 				line=line.replaceAll("\\\t", "").toLowerCase();
 				content+=line.trim();
 			}
 
 			boolean inChange=false;
 
 			int id=-1,value=-1;
 			for(String tag:content.split("><"))
 			{
 				tag=tag.trim();
 
 				if(tag.length()>0 && !tag.startsWith("!--"))
 				{
 					//			System.out.println("\n"+tag);
 					if(tag.matches(AnimatedFace.CHANGE))
 					{
 						inChange=true;
 						System.out.println("inChange = "+inChange);
 					}
 					else if(tag.matches(XmlFactory.getCloseTag(AnimatedFace.CHANGE)))
 					{
 						inChange=false;
 						System.out.println("inChange = "+inChange);
 					}
 
 					if(inChange)
 					{
 						try 
 						{
 							String[] parts=tag.split("[><]");
 							if(parts[0].trim().matches(AnimatedFace.CHANGE_ID))
 							{
 								if(parts[2].trim().matches(XmlFactory.getCloseTag(AnimatedFace.CHANGE_ID)))
 								{
 									id = Integer.parseInt(parts[1]);
 									System.out.println("value_id = "+id);
 								}
 							}
 							if(parts[0].trim().matches(AnimatedFace.CHANGE_VAL))
 							{
 								if(parts[2].trim().matches(XmlFactory.getCloseTag(AnimatedFace.CHANGE_VAL)))
 								{
 									value = Integer.parseInt(parts[1]);
 									System.out.println("value_change = "+value);
 								}
 							}
 						} catch (ArrayIndexOutOfBoundsException e) {
 						} catch (NumberFormatException e){
 						}
 
 						if(id>=0 && value>=0)
 						{
 							window.controlContainer.setSliderPosition(id, value);
 							System.out.println("=> Set Value "+id+" to "+value);
 							id=-1;
 							value=-1;
 						}
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveTemplate(File file) 
 	{
 		BufferedWriter writer;
 		try {
 			writer=new BufferedWriter(new FileWriter(file));
 
 			startTag(AnimatedFace.CHANGES, writer);
 			for(int i=0;i<window.controlContainer.getNumberOfValues();i++){
 
 				try {
 					comment(window.controlContainer.getClass().getDeclaredFields()[i+1].getName(),writer);
 				} catch (SecurityException e) {
 				} catch (NullPointerException e) {
 				}
 
 				startTag(AnimatedFace.CHANGE, writer);
 				tag(i,AnimatedFace.CHANGE_ID,writer);
 				tag(window.controlContainer.getSliderPosition(i),AnimatedFace.CHANGE_VAL,writer);
 				closeTag(AnimatedFace.CHANGE, writer);
 			}
 			closeTag(AnimatedFace.CHANGES, writer);
 
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void closeTag(String tag, BufferedWriter writer) throws IOException {
 		writer.write(XmlFactory.closeTag(tag));
 	}
 
 	public static void startTag(String tag, BufferedWriter writer) throws IOException {
 		writer.write(XmlFactory.startTag(tag));
 	}
 
 	public static void tag(int value, String tag, BufferedWriter writer) throws IOException {
 		writer.write(XmlFactory.intToTag(value,tag));
 	}
 
 	public static void tag(String value, String tag, BufferedWriter writer) throws IOException {
 		writer.write(XmlFactory.stringToTag(value,tag));
 	}
 
 	public static void comment(String string, BufferedWriter writer) throws IOException {
 		writer.write(XmlFactory.stringToComment(string));
 	}
 }
