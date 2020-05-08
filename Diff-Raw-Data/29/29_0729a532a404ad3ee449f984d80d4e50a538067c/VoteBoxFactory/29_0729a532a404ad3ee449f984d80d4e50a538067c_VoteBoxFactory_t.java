 package voteVis;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import processing.core.*;
 
 public class VoteBoxFactory implements BoxListener {
 	private static VoteBoxFactory instance_;
 	private VoteVisApp p_;
 	private static int BOX_STAGGER = Settings.UNIT_DIM;
 	private ArrayList<VoteRow> vote_row_buffer_ = null; // the rows that haven't been displayed yet
 	private ArrayList<VoteRow> vote_rows_ = null;
 	private ArrayList<Box> current_row_ = null;
 	private VoteRow current_vote_row_;
 	public static int CREATE_DELAY = 1700; // in millis;
 	private int create_delay_counter_;
 	private boolean delaying_create_;
 	private static int START_SCROLLING_HEIGHT = 1; // start scrolling after the n'th row has been added
 	public static int BEGIN_TRANSITION_COUNT = 5; // only show this many boxes before transitioning
	private static int TRANSITION_START_HEIGHT = 768 - Settings.UNIT_DIM / 2; // in px
 	private int row_count_ = 0; // the number of rows created
 	private VoteRow bottom_stop_row_ = null;
 	private Box bottom_stop_box_ = null; // I use this to determine when to stop
 	private boolean stopped_first_row_ = false;
 	private Box transition_check_box_ = null;
 	private boolean flipping_;
 	
 	public ProfileBox profile_test;
 
 	public VoteBoxFactory(VoteVisApp p_) {
 		instance_ = this;
 		this.p_ = p_;
 		init();
 	}
 	
 	private void init() {
 		vote_rows_ = new ArrayList<VoteRow>();
 		vote_row_buffer_ = new ArrayList<VoteRow>();
 		flipping_ = false;
 		stopped_first_row_ = false;
 		bottom_stop_box_ = null;
 		bottom_stop_row_ = null;
 		row_count_ = 0;
 		delaying_create_ = false;
 		current_row_ = null;
 		transition_check_box_ = null;
 		if (vote_row_buffer_ != null)
 			vote_row_buffer_.clear();
 	}
 	
 	// this is called by the SceneManager when we've transitioned into
 	// a VoteBox scene
 	public void switched_to() {
 		if (vote_rows_.size() != 0) {
 			PApplet.println("Error in VoteBoxFactory switched_to");
 			VoteVisApp.instance().exit();
 		}
 		init();
 		
 		SceneManager.instance().set_move_speed(MoveSpeed.NORMAL);
 		
 		// you need to store the vote rows in a buffer so that you can load up
 		// all the tiles for a flip transition at once
 		for (int i = 0; i < BEGIN_TRANSITION_COUNT; ++i) {
 			vote_row_buffer_.add(make_vote_row(BallotCounter.instance().get_next_ballot()));
 			vote_row_buffer_.get(i).dispable_collisions();
 			if (i > 0) {
 				//link_vote_rows(vote_row_buffer_.get(i - 1), vote_row_buffer_.get(i));
 			}
 		}
 		
 		transition_check_box_ = vote_row_buffer_.get(BEGIN_TRANSITION_COUNT - 4).row().get(0);
 		
 		next_vote_row();
 	}
 	
 	// make sure you're not holding onto any references
 	public void switching_from() {
 		vote_row_buffer_.clear();
 		vote_rows_.clear();
 		current_row_.clear();
 		current_vote_row_ = null;
 		//bottom_stop_row_.row().clear();
 		bottom_stop_row_ = null;
 		bottom_stop_box_ = null;
 		transition_check_box_ = null;
 	}
 	
 	public void set_current_row_transitioning() {
 		Iterator<Box> it = current_row_.iterator();
 		
 		while (it.hasNext()) {
 			((DynamicBox)it.next()).set_transitioning(true);
 		}
 	}
 	
 	public void next_vote_row() {
 		VoteRow n_row = vote_row_buffer_.get(row_count_);
 		vote_rows_.add(n_row);
 		n_row.add_to_box_manager();
 		n_row.set_all_falling();
 		n_row.enable_collisions();
 		
 		//if (flipping_)
 			n_row.set_y(current_row_.get(0).y() - (Settings.UNIT_DIM - Settings.BOX_GAP) * 3);
 		
 		current_row_ = n_row.row();
 		current_vote_row_ = n_row;
 		
 		row_count_++;
 	}
 
 	// this make all the boxes for a row, and registers them with the BoxManager
 	public VoteRow make_vote_row(Ballot ballot) {
 		 current_row_ = new ArrayList<Box>();
 		
 		Box v1 = new VoteBox(p_, Utility.get_aligned_position(Settings.UNIT_DIM, 1), 
 			0, Type.MUSIC, ballot.votes()[Type.MUSIC.ordinal()]);
 		
 		v1.set_visible(false);
 		//BoxManager.instance().add_box(v1);
 		
 		Box v2 = new VoteBox(p_, Utility.get_aligned_position(Settings.UNIT_DIM, 2), 
 			0, Type.FOOD, ballot.votes()[Type.FOOD.ordinal()]);
 	
 		v2.set_visible(false);
 		//BoxManager.instance().add_box(v2);
 			
 		Box v3 = new VoteBox(p_, Utility.get_aligned_position(Settings.UNIT_DIM, 3), 
 			0, Type.WINE, ballot.votes()[Type.WINE.ordinal()]);
 		
 		v3.set_visible(false);
 		//BoxManager.instance().add_box(v3);
 
 		Box v4 = new VoteBox(p_, Utility.get_aligned_position(Settings.UNIT_DIM, 4), 
 			0, Type.ECO, ballot.votes()[Type.ECO.ordinal()]);
 		
 		v4.set_visible(false);
 		//BoxManager.instance().add_box(v4);
 		
 		Box v5 = new VoteBox(p_, Utility.get_aligned_position(Settings.UNIT_DIM, 5), 
 			0, Type.ART, ballot.votes()[Type.ART.ordinal()]);
 		
 		v5.set_visible(false);
 		//BoxManager.instance().add_box(v5);
 
 		// this should be drawn on top of the other boxes
 		Box profile = new ProfileBox(p_, Utility.get_aligned_position(Settings.UNIT_DIM, 0),
 			0, 0);
 		//BoxManager.instance().add_box(profile);
 		
 		profile.set_height_driving_box(v1);
 		//v1.drive_height_on_collision(profile);
 		current_row_.add(profile);
 		current_row_.add(v1);
 		current_row_.add(v2);
 		current_row_.add(v3);
 		current_row_.add(v4);
 		current_row_.add(v5);
 		
 		return new VoteRow(current_row_);
 	}
 	
 	public void drop_bottom_row() {
 		if (vote_rows_.size() == 0)
 			return;
 		
 		vote_rows_.get(0).drop_row();
 		vote_rows_.remove(0);
 	}
 	
 	public VoteRow pop_bottom_row() {
 		if (vote_rows_.size() == 0)
 			return null;
 		
 		VoteRow row = vote_rows_.get(0);
 		vote_rows_.remove(0);
 		return row;
 	}
 	
 	public void display_top_row() {
 		Iterator<Box> it = current_row_.iterator();
 		while (it.hasNext()) {
 			it.next().set_visible(true);
 		}
 	}
 	
 	public class VoteRow {
 		ArrayList<Box> row_;
 		
 		public VoteRow(ArrayList<Box> row_) {
 			this.row_ = row_;
 		}
 		
 		public void drop_row() {
 			Iterator<Box> it = row_.iterator();
 			while (it.hasNext()) {
 				it.next().fall_off_screen();
 			}
 			
 			BoxManager.instance().set_all_falling();
 			
 			row_.clear();
 		}
 		
 		ArrayList<Box> row() {
 			return row_;
 		}
 		
 		// stopps the row from falling
 		public void stop_row() {
 			Iterator<Box> it = row_.iterator();
 			while (it.hasNext()) {
 				it.next().set_falling(false);
 			}
 		}
 		
 		public void set_all_falling() {
 			Iterator<Box> it = row_.iterator();
 			while (it.hasNext()) {
 				it.next().set_falling(true);
 			}
 		}
 		
 		// add in reverse order for drawing reasons
 		public void add_to_box_manager() {
 			for (int i = row_.size() - 1; i >= 0; --i) {
 				BoxManager.instance().add_box(row_.get(i));
 			}
 		}
 		
 		public float y() {
 			return row_.get(0).y();
 		}
 		
 		public void set_y(float y_) {
 			Iterator<Box> it = row_.iterator();
 			while (it.hasNext()) {
 				it.next().set_y(y_);
 			}	
 		}
 		
 		public void enable_collisions() {
 			Iterator<Box> it = row_.iterator();
 			while (it.hasNext()) {
 				it.next().set_ignore_collisions(false);
 			}
 		}
 		
 		public void dispable_collisions() {
 			Iterator<Box> it = row_.iterator();
 			while (it.hasNext()) {
 				it.next().set_ignore_collisions(true);
 			}	
 		}
 		
 		// links up boxes so the profile box links up all the boxes
 		public void profile_drive_all() {
 			for (int i = 1; i < 6; ++i) {
 				row_.get(i).set_left_driving_box(row_.get(i-1));
 			}
 		}
 	}
 	
 	public static VoteBoxFactory instance() {
 		return instance_;
 	}
 
 	public void setup_finished(Box box) {
 		// so far only called by ProfileBoxes when they have finished
 		// contracting their frame
 		set_current_row_transitioning();
 		create_delay_counter_ = VoteVisApp.instance().millis();
 		delaying_create_ = true;
 	}
 	
 	public void update() {
 		/*
 		if (row_count_ >= START_SCROLLING_HEIGHT) {
 			SceneManager.instance().set_move_speed(MoveSpeed.NORMAL);
 		}
 		
 		// this has been removed as per lindsay's requests
 		if (false && bottom_stop_box_ != null && 
 			(bottom_stop_box_.y() + bottom_stop_box_.get_height() / 2 + Settings.BOX_GAP - 5 // dunno why the five is here but it works
 			> VoteVisApp.instance().height)) {
 			SceneManager.instance().set_move_speed(MoveSpeed.STOP);
 		}
 		*/
 		
 		if (!stopped_first_row_) {
 			if (vote_rows_.size() > 0) {
 				if (vote_rows_.get(0).y() > VoteVisApp.instance().height / 3.0) {
 					vote_rows_.get(0).stop_row();
 					stopped_first_row_ = true;
 				}
 			}
 		}
 		
 		if (row_count_ >= BEGIN_TRANSITION_COUNT - 4 && !flipping_) { 
 			if (transition_check_box_.y() > TRANSITION_START_HEIGHT) {
 				SceneManager.instance().move_from_vote_to_billboard(); // start vana white transition
 				flipping_ = true;
 			}
 		}
 		
 		if (!delaying_create_) 
 			return; // no need to do anything unless we're waiting to create a new row
 		
 		if (row_count_ < BEGIN_TRANSITION_COUNT &&
 			VoteVisApp.instance().millis() - create_delay_counter_ > CREATE_DELAY) {
 			next_vote_row();
 			delaying_create_ = false;
 		}
 		/*
 		if (row_count_ == BEGIN_TRANSITION_COUNT - 3 && bottom_stop_row_ == null) {
 			bottom_stop_row_ = vote_rows_.get(vote_rows_.size() - 1);
 			bottom_stop_box_ = bottom_stop_row_.row().get(1); // skip the profile box that could be weird
 		}
 		*/
 	}
 	
 	public VoteRow[] get_transition_rows() {
 		int start_index = BEGIN_TRANSITION_COUNT - 4;
 		
 		VoteRow[] return_rows = new VoteRow[4];
 		
 		for (int i = 0; i < 4; ++i) {
 			return_rows[i] = vote_row_buffer_.get(start_index + i);
 		}
 		
 		return return_rows;
 	}
 	
 	private void link_vote_rows(VoteRow driving_row, VoteRow driven_row) {
 		for (int i = 0; i < 6; ++i) {
 			driven_row.row().get(i).set_colliding_box(driving_row.row().get(i));
 		}
 	}
 }
