 package gui;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import org.pushingpixels.trident.Timeline;
 import org.pushingpixels.trident.TimelineScenario;
 import org.pushingpixels.trident.callback.TimelineScenarioCallback;
 import org.pushingpixels.trident.ease.Spline;
 
 public abstract class Screen extends JPanel {
 	
 	protected int _width, _height;
 	protected ContentPanel _contentPanel;
 	protected ArrayList<TransitionElement> _elements;
 	protected final int LEFT_OFFSCREEN = -1000;
 	protected final int RIGHT_OFFSCREEN = 3000;
 	
 	public Screen(ContentPanel contentPanel, Dimension dimension) {
 		super();
 		this.setSize((int)dimension.getWidth(),(int)dimension.getHeight());
 		_width = this.getWidth();
 		_height = this.getHeight();
 		_contentPanel = contentPanel;
 		_elements = new ArrayList<TransitionElement>();
 	}
 	
 
 	public void transitionIn() {
 		TimelineScenario timelines = new TimelineScenario();
 		for (TransitionElement element : _elements) {
 			element.disable();
 			Timeline timeline = new Timeline(element);
 			timeline.addPropertyToInterpolate("x", element.getX(), element.getDefaultX());
 			timeline.setDuration(2000);
 			timeline.setEase(new Spline(0.8f));
 			timelines.addScenarioActor(timeline);
 		}
 		timelines.addCallback(new InCallback());
 		timelines.play();
 	}
 
 	public void transitionOut(boolean toLeft) {
 		TimelineScenario timelines = new TimelineScenario();
 		for (TransitionElement element : _elements) {
 			element.disable();
 			Timeline timeline = new Timeline(element);
 			if (toLeft) {
				timeline.addPropertyToInterpolate("x", element.getX(), LEFT_OFFSCREEN);
 			} else {
 				timeline.addPropertyToInterpolate("x", element.getX(), RIGHT_OFFSCREEN);
 			}
 			timeline.setDuration(2000);
 			timeline.setEase(new Spline(0.8f));
 			timelines.addScenarioActor(timeline);
 		}
 		timelines.addCallback(new OutCallback());
 		timelines.play();
 		
 	}
 	
 	private class InCallback implements TimelineScenarioCallback {
 
 		@Override
 		public void onTimelineScenarioDone() {
 			for (TransitionElement element : _elements) {
 				element.enable();
 			}
 			
 		}	 
 	}
 	
 	private class OutCallback implements TimelineScenarioCallback {
 
 		@Override
 		public void onTimelineScenarioDone() {
 			for (TransitionElement element : _elements) {
 				element.enable();
 			}
 			_contentPanel.continueTransition();
 		}
 	}
 	public void setElementsLeft() {
 		for (TransitionElement element : _elements)
			element.setX(LEFT_OFFSCREEN);
 	}
 	public void setElementsRight() {
 		for (TransitionElement element : _elements)
 			element.setX(RIGHT_OFFSCREEN);
 	}
 	
 	public void addElement(TransitionElement e) {
 		_elements.add(e);
 	}
 	
 	public void removeElement(TransitionElement e) {
 		_elements.remove(e);
 	}
 }
