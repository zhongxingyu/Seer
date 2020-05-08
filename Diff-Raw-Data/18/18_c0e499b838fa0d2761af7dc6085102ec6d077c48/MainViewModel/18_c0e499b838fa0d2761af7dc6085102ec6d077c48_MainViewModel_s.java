 package model;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Timer;
 // Note!  Nothing from the view package is imported here.
 
 
 // need a start/end time for each path. or length
 public class MainViewModel extends Object {
 	/* A list of the model's views. */
 	private ArrayList<IView> views = new ArrayList<IView>();
 
 	private ArrayList<ArrayList<Point>> paths = new ArrayList<ArrayList<Point>>();
 	private boolean stillPainting = true;
 	ArrayList<Point> currPath = new ArrayList<Point>();
 	ArrayList<Point> selectingPath = new ArrayList<Point>();
 	
 	private int state = 0; // {0,1,2,3,4} = {draw, erase, selection}. Default is draw
 	private boolean selected = false;
 	private int selectedIndex = -1;
 	
 	private int duration = 100; 
 	private boolean playing = false;
 	private int FPS = 40;
 	private Timer t;
 	
 	// Override the default construtor, making it private.
 	public MainViewModel() {
 	}
 	
 	public void addPath() {
 		currPath = new ArrayList<Point>();
 		paths.add(currPath);
 		System.out.println("num paths "+ paths.size());
 	}
 	
 	public ArrayList<Point> getSelectingPath(){
 		return this.selectingPath;
 	}
 	public ArrayList<ArrayList<Point>> getPaths(){
 		return paths;
 	}
 	
 	public void removePath(int i){
 		this.paths.remove(i);
 		this.updateAllViews();
 	}
 	public void removeLasso(){
 		this.selectingPath = new ArrayList<Point>();
 		this.updateAllViews();
 	}
 	public void clearPaths(){
		for (int i = 0; i < paths.size(); i++){
			this.paths.remove(i);
		}
 		this.selectingPath = new ArrayList<Point>();
 		this.updateAllViews();
 	}
 
 	public void addPoint(Point point){
 		if (state == 0){
 			currPath.add(point);
 			if (this.stillPainting){
 				if (this.paths.size() == 0)
 					this.paths.add(currPath);
 				this.paths.set(paths.size()-1, currPath);
 			} else {
 				System.out.println("New PATH!");
 				this.paths.set(paths.size()-1, currPath);
 				this.addPath();
 			}
 		} else if (state == 2){
 			selectingPath.add(point);
 			if (!this.stillPainting){
 
 				System.out.println("Done drawing the selector"); // TODO: some crazy shizz here
 				//this.paths.set(paths.size()-1, currPath);
 				//this.addSelectPath();
 			}
 		}
 		this.updateAllViews();
 	}
 	
 	public void setStillPainting(boolean isPainting){
 		this.stillPainting = isPainting;
 	}
 	public boolean stillPainting(){
 		return this.stillPainting;
 	}
 	
 	public void setSelected(boolean selected){
 		this.selected = selected;
 		
 	}
 	public boolean getSelected(){
 		return this.selected;
 	}
 	
 	public void setPlaying(boolean playing){
 		this.playing = true;
 	}
 	public boolean getPlaying(){
 		return this.playing;
 	}
 	
 	public void setSelectedIndex(int selected){
 		this.selectedIndex = selected;
 	}
 	
 	public int getSelectedIndex(){
 		return this.selectedIndex;
 	}
 	
 	public void setState(int state){
 		this.state = state;
 		//this.updateAllViews(); // not sure if this is necessary
 	}
 	public int getState(){
 		return this.state;
 	}
 	
 	/** Add a new view. */
 	public void addView(IView view) {
 		this.views.add(view);
 		view.updateView();
 	}
 
 	/** Remove a view. */
 	public void removeView(IView view) {
 		this.views.remove(view);
 	}
 
 	/** Update all the views. */
 	private void updateAllViews() {
 		for (IView view : this.views) {
 			view.updateView();
 		}
 	}
 }
