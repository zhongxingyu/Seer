 package voteVis;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import processing.core.PApplet;
 
 import voteVis.RotateBoxTransition.Axis;
 
 public class BillboardFactory implements TransitionReceiver {
 	private static BillboardFactory instance_;
 	private BoxTransition box_transition_;
 	private ArrayList<Box> start_boxes_;
 	private ArrayList<Box> end_boxes_;
 	
 	private Box current_start_box_;
 	private Box current_end_box_;
 	
 	private Type current_type_;
 	
 	private boolean transitioning_;
 	private boolean delaying_;
 	private Box transition_check_box_ = null;
	private static int TRANSITION_HEIGHT = (int)(768 * 1.0 / 3.0);
 	
 	public BillboardFactory() {
 		instance_ = this;
 		init();
 		load_transition(new RotateBoxTransition(Axis.VERTICAL, this));
 	}
 	
 	private void init() {
 		transitioning_ = false;
 	}
 	
 	public void update() {
 		if (delaying_) {
 			if(transition_check_box_.y() > TRANSITION_HEIGHT) {
 				SceneManager.instance().finished_billboard(current_type_);
 				delaying_ = false;
 			}
 			return;
 		}
 		
 		if (!transitioning_)
 			return;
 		
 		box_transition_.update();
 	}
 	
 	public void draw() {
 		if (!transitioning_)
 			return;
 		
 		box_transition_.draw();	
 	}
 	
 	public void load_transition(BoxTransition box_transition_) {
 		this.box_transition_ = box_transition_;
 	}
 	
 	public void load_vote_to_top_transition(Type type_) {
 		ArrayList<Box> start_boxes_ = new ArrayList<Box>();
 		ArrayList<Box> end_boxes_ = new ArrayList<Box>();
 		
 		VoteBoxFactory.VoteRow[] transition_rows = VoteBoxFactory.instance().get_transition_rows();
 		
 		for (int i = 0; i < 4; ++i) {
 			VoteBoxFactory.VoteRow row = transition_rows[i];
 			
 			if (i % 2 == 0) {
 				for (int j = 0; j < 6; ++j) {
 					start_boxes_.add(row.row().get(j));
 					BillboardBox b = new BillboardBox(type_, j, i,
 						row.row().get(j).x(), row.row().get(j).y());
 					end_boxes_.add(b);
 					BoxManager.instance().add_box(b);
 				}
 			} else {
 				for (int j = 5; j >= 0; --j) {
 					start_boxes_.add(row.row().get(j));
 					BillboardBox b = new BillboardBox(type_, j, i,
 						row.row().get(j).x(), row.row().get(j).y());
 					end_boxes_.add(b);
 					BoxManager.instance().add_box(b);
 				}
 			}
 		}
 		
 		current_type_ = type_;
 		
 		load_boxes(start_boxes_, end_boxes_);
 	}
 	
 	public void load_boxes(ArrayList<Box> start_boxes_, ArrayList<Box> end_boxes_) {
 		if (start_boxes_.size() != end_boxes_.size()) 
 			VoteVisApp.instance().exit(); // assert correct transition number
 		
 		this.start_boxes_ = start_boxes_;
 		this.end_boxes_ = end_boxes_;
 	}
 	
 	// starts the transition
 	public void begin_transition() {
 		delaying_ = false;
 		transitioning_ = true;
 		transition_check_box_ = null;
 		load_next_box();
 	}
 	
 	private void load_next_box() {
 		if (start_boxes_.size() == 0) {
 			transitioning_ = false;
 			delaying_ = true;
 			return;
 		}
 		
 		box_transition_.load_boxes(start_boxes_.get(0), end_boxes_.get(0));
 		BoxManager.instance().delete_box(start_boxes_.get(0));
 		end_boxes_.get(0).set_ignore_collisions(false);
 		start_boxes_.remove(0);
 		end_boxes_.remove(0);
 	}
 
 	public void finished_transition(Box end_box) {
 		end_box.set_visible(true);
 		// not needed except on the last 
 		transition_check_box_ = end_box;
 		//end_box.set_ignore_collisions(false);
 		load_next_box();
 	}
 	
 	public static BillboardFactory instance() {
 		return instance_;
 	}
 	
 	public void box_collided(Box box) {
 		
 	}
 }
