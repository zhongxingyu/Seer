 package org.cotrix.web.importwizard.client.progresstracker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class ProgressTracker extends Composite {
 
 	//TODO make dynamic
	public static final int TRACKER_WIDTH = 950;
 	public static final int BUTTON_WIDTH = 30;
 
 	private static ProgressTrackerUiBinder uiBinder = GWT.create(ProgressTrackerUiBinder.class);
 
 	interface ProgressTrackerUiBinder extends UiBinder<Widget, ProgressTracker> {
 	}
 
 	public ProgressTracker() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 	@UiField
 	FlowPanel barPanel;
 
 	@UiField
 	FlowPanel textPanel;
 	
 	protected Map<String, Integer> index = new HashMap<String, Integer>();
 
 	protected List<ProgressTrackerButton> buttons = new ArrayList<ProgressTrackerButton>();
 	protected List<ProgressTrackerLine> lines = new ArrayList<ProgressTrackerLine>();
 	protected ProgressTrackerLine lastLine;
 	protected List<ProgressTrackerLabel> labels = new ArrayList<ProgressTrackerLabel>();
 
 	public void init(List<ProgressStep> steps)
 	{
 		int numButtons = steps.size();
 
 		if (numButtons == labels.size()) {
 			//we have already the labels initialized, we simply update the label
 			updateLabelsTexts(steps);
 		} else {
 			//we initialize the labels
 			int buttonsWidth = BUTTON_WIDTH * numButtons;
 			
 			int lineWidth = (TRACKER_WIDTH - buttonsWidth) / (numButtons + 1);
 			int labelWidth = lineWidth / 2;
 
 			initButtons(numButtons, lineWidth);
 			initLabels(steps, labelWidth, lineWidth);
 		}
 	}
 
 	protected void initButtons(int numButtons, int lineWidth)
 	{
 		barPanel.clear();
 		buttons.clear();
 		lines.clear();
 
 		barPanel.setHeight(BUTTON_WIDTH + "px");
 
 		for (int i = 0; i < numButtons; i++) {
 			ProgressTrackerLine line = new ProgressTrackerLine(lineWidth);
 			lines.add(line);
 			barPanel.add(line);
 
 			ProgressTrackerButton button = new ProgressTrackerButton(String.valueOf(i + 1), BUTTON_WIDTH);
 			buttons.add(button);
 			barPanel.add(button);
 		}
 		lastLine = new ProgressTrackerLine(lineWidth);
 		barPanel.add(lastLine); // add last line at the end of
 	}
 
 	protected void initLabels(List<ProgressStep> steps, int labelWidth, int lineWidth) {
 		textPanel.clear();
 		labels.clear();
 		index.clear();
 
 		textPanel.getElement().getStyle().setProperty("paddingLeft", labelWidth + "px");
 		textPanel.setHeight(BUTTON_WIDTH + "px");
 
 		int i = 0;
 		for (ProgressStep step:steps) {
 			String stepLabel = step.getLabel();
 			ProgressTrackerLabel label = new ProgressTrackerLabel(stepLabel, lineWidth + BUTTON_WIDTH);
 			labels.add(label);
 			textPanel.add(label);
 			//Log.trace("setting step "+i+" with step id: "+step.getId()+" label: "+step.getLabel());
 			index.put(step.getId(), i++);
 		}
 	}
 
 	protected void updateLabelsTexts(List<ProgressStep> steps)
 	{
 		index.clear();
 		for (int i = 0; i < steps.size(); i++) {
 			ProgressTrackerLabel label = labels.get(i);
 			ProgressStep step = steps.get(i);
 			String stepLabel = step.getLabel();
 			label.setText(stepLabel);
 			index.put(step.getId(), i);
 		}
 	}
 
 	public void setCurrentStep(ProgressStep step)
 	{
 		Log.trace("setCurrentStep step: "+step.getId());
 		int stepIndex = index.get(step.getId());
 		Log.trace("stepIndex: "+stepIndex);
 		
 		int stepsCount = buttons.size();
 		for (int i = 0; i < stepsCount; i++) {
 			buttons.get(i).setActive(i<=stepIndex);
 			lines.get(i).setActive(i<=stepIndex);
 			labels.get(i).setActive(i<=stepIndex);
 		}
 		ProgressTrackerLine firstLine = lines.get(0);
 		firstLine.setRoundCornerLeft(stepIndex>=0);
 
 		lastLine.setActive(stepIndex == buttons.size()-1);
 		lastLine.setRoundCornerRight(stepIndex == buttons.size()-1);
 	}
 
 	public interface ProgressStep {
 		public String getId();
 		public String getLabel();
 	}
 
 }
