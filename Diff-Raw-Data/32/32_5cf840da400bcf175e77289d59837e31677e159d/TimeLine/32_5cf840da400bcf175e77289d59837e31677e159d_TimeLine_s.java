 package pokemon;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JSlider;
 import javax.swing.Timer;
 
 public class TimeLine {
 	class RecordTimer implements ActionListener {
 		Vector2D translation;
 		List<Map<Entity, Vector2D>> timeFrames;
 		
 		public RecordTimer(List<Map<Entity, Vector2D>> timeFrames) {
 			translation = Vector2D.ZERO();
 			this.timeFrames = timeFrames;
 		}
 		
		public void queueTranslation(Vector2D translation) {
			this.translation = this.translation.add(translation);
		}
		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int frame = slider.getValue();
 			
 			while (timeFrames.size() > frame) {
 				timeFrames.remove(frame);
 			}
 			
 			Map<Entity, Vector2D> timeFrame = new HashMap<Entity, Vector2D>();
 			for (Entity entity : selectedEntities) {
 				timeFrame.put(entity, entity.getPosition());
 				System.out.println(entity.getPosition());
 			}
 			timeFrames.add(timeFrame);
 			slider.setMaximum(timeFrames.size());
 			
 			slider.setValue(frame + 1);
 		}
 	}
 	
 	class AnimationTimer implements ActionListener {
 		List<Map<Entity, Vector2D>> timeFrames;
 		
 		public AnimationTimer(List<Map<Entity, Vector2D>> timeFrames) {
 			this.timeFrames = timeFrames;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
			int frame = slider.getValue() + 1;
 			
 			if (frame >= timeFrames.size()) {
 				timer.stop();
 				return;
 			}
 			
 			Map<Entity, Vector2D> timeFrame = timeFrames.get(frame);
 			
 			for (Map.Entry<Entity, Vector2D> entry : timeFrame.entrySet()) {
 				Entity entity = entry.getKey();
 				Vector2D position = entry.getValue();
 				entity.setPosition(position);
 			}
 			view.updateView();
 			slider.setValue(frame + 1);
 		}
 	}
 
 	private List<Entity> selectedEntities;
 	private List<Map<Entity, Vector2D>> timeFrames;
 	
 	private RecordTimer recordTimer;
 	private AnimationTimer animationTimer;
 	private Timer timer;
 	private CanvasView view;
 	private JSlider slider;
 	
 	private int curFrame;
 	
 	public TimeLine(CanvasView view, JSlider slider) {
 		curFrame = 0;
 		this.view = view;
 		timeFrames = new ArrayList<Map<Entity, Vector2D>>();
 		recordTimer = new RecordTimer(timeFrames);
 		animationTimer = new AnimationTimer(timeFrames);
 		timer = new Timer(16, null);
 		this.slider = slider;
 	}
 	
 	public void setCurFrame(int frame) {
 		curFrame = (frame >= timeFrames.size()) ? timeFrames.size() - 1 : frame;
 		
 		Map<Entity, Vector2D> timeFrame = timeFrames.get(curFrame);
 		
 		for (Map.Entry<Entity, Vector2D> entry : timeFrame.entrySet()) {
 			Entity entity = entry.getKey();
 			Vector2D position = entry.getValue();
 			entity.setPosition(position);
 		}
 	}
 	
 	public void record(List<Entity> selectedEntities) {
 		timer.stop();
 		this.selectedEntities = selectedEntities;
 		timer = new Timer(16, recordTimer);
 		timer.start();
 	}
 	
	public void queueTranslation(Vector2D translation) {
		recordTimer.queueTranslation(translation);
	}
	
 	public void endRecord() {
 		timer.stop();
 	}
 	
 	public void play() {
 		timer.stop();
 		timer = new Timer(16, animationTimer);
 		timer.start();
 	}
 	
 	public void pause() {
 		timer.stop();
 	}
 }
