 package voteVis;
 
 import java.util.Iterator;
 
 import processing.core.PImage;
 import voteVis.Box.Collision;
 
 public class TrendBox extends DynamicBox {
 	private Type type_; // musician, food, etc
 	private int index_; // which musician, food, etc
 	private Size size_; // XL, L, M, S, XS
 	
 	public TrendBox(Type type_, int index_, Size size_,
 		float x_, float y_) {
 		super(VoteVisApp.instance(), x_, y_);
 		
 		this.type_ = type_;
 		this.index_ = index_;
 		this.size_ = size_;
 		
 		box_frame_ = new TrendFrame(type_, size_);
 		
 		set_pane_transition_states();
 		box_pane_.set_slide_speed(get_speed_from_size(size_));
 		box_pane_.load_transition(); // prep the box for a transition
 	}
 	
 	private void set_pane_transition_states() {
 		box_pane_.add_transition_state(get_icon_state());
 		box_pane_.add_transition_state(get_text_state());
 		box_pane_.add_transition_state(get_photo_state());
 	}
 	
 	private TransitionState get_text_state() {
 		return new TextState(p_, ImageLoader.instance().get_candidate_name(type_, index_),
 			p_.color(1, 0), 
 			Utility.instance().scale_to_pane_size(ImageLoader.instance().get_vote_background(type_),
 			Size.get_dim_from_size(size_) - TrendFrame.BORDER_WIDTH),
 			Size.get_dim_from_size(size_) - TrendFrame.BORDER_WIDTH,
 			Settings.instance().get_trend_box_font(size_),
 			Settings.instance().get_trend_box_font_size(size_));
 	}
 	
 	private PhotoState get_photo_state() {
 		return new PhotoState(p_, Utility.instance().scale_to_pane_size(
 			ImageLoader.instance().get_candidate_image(type_, index_),
 			Size.get_dim_from_size(size_) - TrendFrame.BORDER_WIDTH));
 	}
 	
 	// returns a photo with the icon 1, 2, 3, etc
 	private PhotoState get_icon_state() {
 		// TODO: replace this
 		return get_photo_state();
 	}
 	
 	@Override
 	public void update() {
 		super.update();
 		
 		if (falling_) {
 			if (y_ > VoteVisApp.instance().height - Size.get_dim_from_size(size_) / 2 - Settings.BOX_GAP * 3)
 				falling_ = false;
 		}
 	}
 	
 	// no one should be flipping the TrendBox
 	@Override
 	public PImage get_image() {
 		return null;
 	}
 	
 	@Override
 	public void stopped_falling() {
 		super.stopped_falling();
 		
 		TrendFactory.instance().box_collided(this);
 	}
 	
 	@Override
 	public Collision check_collisions() {
 		Iterator<Box> it = BoxManager.instance().boxes().iterator();
 		
 		while (it.hasNext()) {
 			Box b = it.next();
 			
 			if (b == this)
 				continue;
 			
 			if (b.ignore_collisions())
 				continue;
 
 			if (b.get_rectangle().intersects(get_rectangle())) {
 				colliding_box_ = b;
 				return Collision.YES;
 			}
 		}
 		
 		return Collision.NONE;
 		
 		
 		/*
 		
 		if (size_ == Size.S || size_ == Size.XS)
 			return super.check_collisions();
 		else if (false && size_ == Size.M ) {
 			Collision l = check_collisions_with_center(x_ - Settings.UNIT_DIM / 2, y_);
 			Collision r = check_collisions_with_center(x_ - Settings.UNIT_DIM / 2, y_);
 			
 			if (l == Collision.YES || r == Collision.YES)
 				return Collision.YES;
 			else if (l == Collision.ABOUT || r == Collision.ABOUT)
 				return Collision.ABOUT;
 			
 			return Collision.NONE;
 		} else {
 			Collision l = check_collisions_with_center(x_ - Settings.UNIT_DIM, y_);
 			Collision c = check_collisions_with_center(x_, y_);
 			Collision r = check_collisions_with_center(x_ - Settings.UNIT_DIM, y_);
 			
 			if (l == Collision.YES || r == Collision.YES || c == Collision.YES)
 				return Collision.YES;
 			else if (l == Collision.ABOUT || r == Collision.ABOUT || c == Collision.ABOUT)
 				return Collision.ABOUT;
 			
 			return Collision.NONE;
 		}
 		*/
 	}
 	
 	private static int get_speed_from_size(Size s) {
 		switch (s) {
		case L:
 			return 8;
 		case M:
 			return 4;
 		case S:
 		default:
 			return 2;
 		}
 	}
 	
 
 	
 }
