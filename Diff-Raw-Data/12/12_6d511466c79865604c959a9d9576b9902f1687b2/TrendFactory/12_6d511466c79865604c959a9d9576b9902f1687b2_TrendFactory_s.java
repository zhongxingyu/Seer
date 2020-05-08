 package voteVis;
 
 import processing.core.*;
 
 public class TrendFactory {
 	private static TrendFactory instance_;
 	private Type current_type_;
 	private boolean[] created_types_;
 	private int created_boxes_;
 	private int[] top_five_;
	private static int CREATE_DELAY = 250; 
 	private static int TRANSITION_HEIGHT = 680;
 	private boolean delaying_;
 	private int delaying_counter_;
 	private Box top_check_box_ = null;
 	private int boxes_created_ = 0;
 	private boolean active_ = false;
 	
 	public TrendFactory() {
 		instance_ = this;
 		
 		created_types_ = new boolean[5];
 	}
 	
 	public void switched_to(Type type_) {
 		current_type_ = type_;
 		
 		for (int i = 0; i < 5; ++i) {
 			created_types_[i] = false;
 		}
 		
 		top_five_ = BallotCounter.instance().get_top_five(current_type_);
 		
 		delaying_ = false;
 		active_ = true;
 		top_check_box_ = null;
 		boxes_created_ =0;
 		
 		make_next_box();
 	}
 	
 	public void switching_from() {
 		top_check_box_ = null;
 		active_ = false;
 	}
 	
 	private void make_next_box() {
 		switch (boxes_created_) {
 		case 2:
 			add_box_at_position(0, Utility.get_aligned_position(Settings.UNIT_DIM, 4) + 5);
 			break;
 		case 3:
 			add_box_at_position(3, (int) (Settings.BOX_GAP * 2 + Size.get_dim_from_size(Size.M) + 
 				Size.get_dim_from_size(Size.S) / 2));
 			break;
 		case 4:
 			top_check_box_ = add_box_at_position(2, (int) (Size.get_dim_from_size(Size.M) / 2 + Settings.BOX_GAP));
 			break;
 		case 1:
 			add_box_at_position(1, Size.get_dim_from_size(Size.L) / 2 + Settings.BOX_GAP * 2 +
 					Settings.UNIT_DIM);
 			break;
 		case 0:
 			add_box_at_position(4, Utility.get_aligned_position(Settings.UNIT_DIM, 0));
 		}
 		
 		++boxes_created_;	
 		delaying_ = false;
 	}
 	
 	public Box add_box_at_position(int rank, int position) {
 		Box b = new TrendBox(current_type_, top_five_[rank],
 			Size.get_size_from_rank(rank), 
			position, -500);
 		
 		BoxManager.instance().add_box(b);
 		
 		return b;
 	}
 	
 	public void box_collided(TrendBox box) {
 		delaying_counter_ = VoteVisApp.instance().millis();
 		delaying_ = true;
 	}
 	
 	public void update() {
 		if (!active_)
 			return;
 		
 		if (top_check_box_ != null &&
 			top_check_box_.y() > TRANSITION_HEIGHT) {
 			SceneManager.instance().finished_trend();
 			top_check_box_ = null;
 		}
 		
 		if (delaying_ && top_check_box_ == null && 
 			(VoteVisApp.instance().millis() - delaying_counter_ > CREATE_DELAY)) {
 			make_next_box();
 		}
 	}
 	
 	private int get_random_rank() {
 		PApplet p_ = VoteVisApp.instance();
 		while (true) {
 			int ran_index = (int) p_.random(5);
 			
 			if (created_types_[ran_index] == false) {
 				created_types_[ran_index] = true;
 				return ran_index;
 			}
 		}
 	}
 	
 	
 	public void create_box() {
 		int rank = get_random_rank();
 		int index = top_five_[rank];
 		
 		Size b_size = Size.get_size_from_rank(rank);
 		
 		Box b = new TrendBox(current_type_, index, b_size,
 			get_random_x_pos(b_size), 0);
 		
 		BoxManager.instance().add_box(b);
 		
 		++created_boxes_;
 	}
 	
 	public static TrendFactory instance() {
 		return instance_;
 	}
 	
 	private int get_random_x_pos(Size size) {
 		if (size == Size.S) {
 			int ran_index = (int) VoteVisApp.instance().random(6);
 			return Utility.get_aligned_position(Settings.UNIT_DIM, ran_index);
 		// TODO: these should be aligned 
 		} else if (size == Size.M) {
 			int offset = Size.get_dim_from_size(Size.M) / 2 + Settings.BOX_GAP;
 			return (int) VoteVisApp.instance().random(offset,
 				VoteVisApp.instance().width - offset);
 		} else {
 			int offset = Size.get_dim_from_size(Size.L) / 2 + Settings.BOX_GAP;
 			return (int) VoteVisApp.instance().random(offset,
 					VoteVisApp.instance().width - offset);
 		}
 	}
 }
