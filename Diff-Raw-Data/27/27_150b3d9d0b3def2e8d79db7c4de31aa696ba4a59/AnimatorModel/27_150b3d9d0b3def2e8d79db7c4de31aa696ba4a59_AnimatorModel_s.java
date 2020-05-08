 package model;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.geom.GeneralPath;
 import java.util.ArrayList;
 
 //TODO: fix bug when insert in middle...... cut off at the end for some reason.
 public class AnimatorModel extends Object {
 	public enum State {
 		draw, erase, selection, dragged, playing
 	};
 
 	/* A list of the model's views. */
 	private ArrayList<IView> views = new ArrayList<IView>();;
 
	private ArrayList<Segment> segments = new ArrayList<Segment>();;
	private ArrayList<Integer> selectedIndices = new ArrayList<Integer>();;
 
 	private State state = State.draw;
 	private boolean stillDragging = true;
 	private int currframe = 0;
 	private int totalframes = 0;
 
 	private Color paletteColor = Color.BLACK;
 	private int strokeSize = 5;
 	Segment currSegment = new Segment(currframe, currframe, paletteColor, strokeSize);
 	Segment selectingSegment = new Segment(currframe, currframe, paletteColor, strokeSize);
 
 	// Override the default constructor, making it private.
 	public AnimatorModel() {
 	}
 
 	public void setPaletteColor(Color c) {
 		paletteColor = c;
 	}
 
 	public Color getPaletteColor() {
 		return paletteColor;
 	}
 
 	public void setStrokeSize(int sz) {
 		strokeSize = sz;
 		this.updateAllViews();
 	}
 
 	public int getStrokeSize() {
 		return strokeSize;
 	}
 
 	public void setState(State passedState) {
 		// remove the selected items & lasso trace
 		if (passedState == State.draw || passedState == State.erase
 				|| passedState == State.playing) {
 			selectedIndices.clear();
 			this.removeLasso();
 		}
 
 		state = passedState;
 		this.updateAllViews();
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public void pushFrame() {
 		for (Segment s : segments) {
 			if (!s.isErased(currframe)) {
 				s.createFrame(currframe + 1);
 			}
 		}
 		currframe++;
 		if (currframe > totalframes) {
 			totalframes++;
 		}
 		this.updateAllViews();
 	}
 
 	// increment totalframes, insert a copy of current frame for each segment.
 	public void insertFrame() {
 		increaseFrames(true);
 		for (Segment s : segments) {
 			if (currframe > totalframes - currframe) {
 				s.createFrame(currframe);
 			} else {
 				s.copyFrame(currframe);
 			}
 		}
 		System.out.println("Inserted frame");
 		this.updateAllViews();
 	}
 
 	public void addPointToSegment(Point point) {
 		if (state == State.draw) {
 			currSegment.addPoint(point);
 			if (stillDragging && segments.size() == 0) {
 				segments.add(currSegment);
 			}
 			segments.set(segments.size() - 1, currSegment);
 		} else if (state == State.selection
 				&& this.getSelectedIndices().size() == 0) {
 			selectingSegment.addPoint(point);
 		}
 		this.updateAllViews();
 	}
 
 	public void addSegment() {
 		currSegment = new Segment(currframe, currframe, this.getPaletteColor(), strokeSize);
 		segments.add(currSegment);
 	}
 
 	public ArrayList<Segment> getSegments() {
 		return segments;
 	}
 
 	public Segment getSelectingSegment() {
 		return selectingSegment;
 	}
 
 	public void addTranslate(int x, int y) {
 		int currFrame = this.getFrame();
 		// System.out.println("Dragging the objs");
 		for (Segment s : segments) {
 			if (!s.isErased(currFrame)) {
 				s.addSegmentTranslate(0, 0, currFrame);
 			}
 		}
 		for (int index : selectedIndices) {
 			segments.get(index).addSegmentTranslate(x, y, currFrame);
 		}
 	}
 
 	public void selectStuff(GeneralPath lassoPath) {
 		for (int i = 0; i < segments.size(); i++) {
 			ArrayList<Point> points = segments.get(i).getTranslates(
 					this.getFrame());
 			int size = points.size();
 			for (int j = 0; j < size; j++) {
 				Point currPoint = points.get(j);
 				if (!lassoPath.contains(currPoint)) {
 					break;
 				}
 				// all pts inside lasso, so we can select this segment
 				else if (j == size - 1) {
 					System.out.println("Selected!");
 					this.addSelectedIndex(i);
 				}
 			}
 		}
 		this.removeLasso();
 	}
 
 	public void deselect() {
 		selectedIndices.clear();
 		this.removeLasso();
 		state = State.selection; // allow user to select again.
 		this.updateAllViews();
 	}
 
 	public void removeLasso() {
 		selectingSegment = new Segment(currframe, currframe, paletteColor, strokeSize);
 		this.updateAllViews();
 	}
 
 	public void eraseStuff(int oldX, int oldY) {
 		int largestEndTime = 0, currEndTime = 0;
 		for (Segment s : segments) {
 			ArrayList<Point> points = s.getTranslates(this.getFrame());
 
 			for (int j = 0; j < points.size(); j++) {
 				Point currPoint = points.get(j);
 				int x = currPoint.x;
 				int y = currPoint.y;
 
 				if (oldX > x - 10 && oldX < x + 10 && oldY > y - 10
 						&& oldY < y + 10) {
 					// System.out.println("Erasing the " + i + "th obj");
 					s.setEndTime(currframe - 1);
 					break;
 				}
 			}
 			currEndTime = s.getEndTime();
 			if (currEndTime > largestEndTime) {
 				largestEndTime = currEndTime;
 			}
 		}
 		if (largestEndTime < totalframes) {
			totalframes = currEndTime;
 		}
 		this.updateAllViews();
 	}
 
 	public void setStillDragging(boolean stillDragging) {
 		this.stillDragging = stillDragging;
 	}
 
 	public boolean getStillDragging() {
 		return this.stillDragging;
 	}
 
 	public void addSelectedIndex(int selected) {
 		selectedIndices.add(selected);
 	}
 
 	public ArrayList<Integer> getSelectedIndices() {
 		return this.selectedIndices;
 	}
 
 	public int getFrame() {
 		return this.currframe;
 	}
 
 	public int getTotalFrames() {
 		return this.totalframes;
 	}
 
 	public void setFrame(int frame) {
 		if (frame <= totalframes) {
 			this.currframe = frame;
 			this.updateAllViews();
 		}
 	}
 
 	public void gotoZero() {
 		currframe = 0;
 		this.updateAllViews();
 	}
 
 	public void increaseFrames(boolean copyframe) {
 		currframe++;
 		if (currframe > totalframes || copyframe) {
 			totalframes++;
 		}
 		this.updateAllViews();
 	}
 
 	public void decreaseFrames() {
 		if (currframe > 0) {
 			currframe--;
 			this.updateAllViews();
 		}
 	}
 
 	public void restart() {
 		segments.clear();
 		selectingSegment = new Segment(currframe, currframe, paletteColor, strokeSize);
 		selectedIndices.clear();
 		totalframes = 0;
 		currframe = 0;
 		this.updateAllViews();
 	}
 
 	public void addView(IView view) {
 		views.add(view);
 		view.updateView();
 	}
 
 	public void removeView(IView view) {
 		views.remove(view);
 	}
 
 	private void updateAllViews() {
 		for (IView view : this.views) {
 			view.updateView();
 		}
 	}
 }
