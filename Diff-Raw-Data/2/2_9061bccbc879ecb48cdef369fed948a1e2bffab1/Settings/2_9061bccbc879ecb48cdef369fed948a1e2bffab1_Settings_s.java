 package voteVis;
foo bar
 import processing.core.*;
 
 public class Settings {
 	private VoteVisApp p_;
 	
 	public static int UNIT_DIM = 150;
 	public static int VOTE_PANE_DIM = 138;
 	public static int BOX_GAP = 14;
 	public static int FALL_SPEED = 10;
 	public static int VOTE_BOX_SIDE_DIM = UNIT_DIM;
 	public static String DEFAULT_PROFILE_IMAGE = "profile_1-110.png";
 	
 	// TODO: update this to real values
 	public static int NUM_ECO = 5;
 	public static int NUM_MUSIC = 5;
 	public static int NUM_ART = 5;
 	public static int NUM_FOOD = 5;
 	public static int NUM_WINE = 5;
 	
 	private int music_color_;
 	private int eco_color_;
 	private int wine_color_;
 	private int food_color_;
 	private int art_color_;
 	private int profile_color_;
 	
 	private int background_color_;
 	
 	private PFont vote_box_font_;
 	public static int VOTE_BOX_FONT_SIZE = 18;
 	
 	private PFont profile_box_font_;
 	public static int PROFILE_BOX_FONT_SIZE = 30;
 	
 	private PFont[] trend_box_fonts_;
 	private int[] trend_box_font_sizes_;
 	
 	public Settings(VoteVisApp p_) {
 		this.p_ = p_;
 		
 		music_color_ = p_.color(237, 30, 121);
 		eco_color_ = p_.color(114, 160, 42);
 		wine_color_ = p_.color(102, 14, 60);
 		food_color_ = p_.color(247, 147, 30);
 		art_color_ = p_.color(49, 193, 182);
 		profile_color_ = p_.color(37, 133, 247);
 		
 		background_color_ = p_.color(27, 27, 27);
 		
 		// TODO: load this font with processing
 		vote_box_font_ = p_.loadFont("GillSansMT-18.vlw");
 		profile_box_font_ = p_.loadFont("GillSansMT-18.vlw");
 		
 		trend_box_font_sizes_ = new int[5];
 		trend_box_font_sizes_[Size.XS.ordinal()] = 14;
 		trend_box_font_sizes_[Size.S.ordinal()] = 18;
 		trend_box_font_sizes_[Size.M.ordinal()] = 20;
 		trend_box_font_sizes_[Size.L.ordinal()] = 24;
 		trend_box_font_sizes_[Size.XL.ordinal()] = 30;
 		
 		trend_box_fonts_ = new PFont[5];
 		trend_box_fonts_[Size.XS.ordinal()] = p_.loadFont("GillSansMT-18.vlw");
 		trend_box_fonts_[Size.S.ordinal()] = p_.loadFont("GillSansMT-18.vlw");
 		trend_box_fonts_[Size.M.ordinal()] = p_.loadFont("GillSansMT-18.vlw");
 		trend_box_fonts_[Size.L.ordinal()] = p_.loadFont("GillSansMT-18.vlw");
 		trend_box_fonts_[Size.XL.ordinal()] = p_.loadFont("GillSansMT-18.vlw");
 	}
 	
 	public static int HALF_UNIT_DIM () {
 		return UNIT_DIM / 2;
 	}
 	
 	public int get_vote_box_color_for_type(Type type) {
 		switch (type) {
 		case MUSIC:
 			return music_color_;
 		case ECO:
 			return eco_color_;
 		case WINE:
 			return wine_color_;
 		case FOOD:
 			return food_color_;
 		case ART:
 		default:
 			return art_color_;
 		}
 	}
 	
 	public PFont get_vote_box_font() {
 		return vote_box_font_;
 	}
 	
 	public PFont get_profile_box_font() {
 		return profile_box_font_;
 	}
 	
 	public int background_color() {
 		return background_color_;
 	}
 	
 	public int profile_color() {
 		return profile_color_;
 	}
 	
 	public void load_vote_box_font() {
 		p_.textFont(vote_box_font_, VOTE_BOX_FONT_SIZE);
 	}
 	
 	public void load_trend_box_font(Size size) {
 		p_.textFont(get_trend_box_font(size), 
 			get_trend_box_font_size(size));
 	}
 	
 	public int get_trend_box_font_size(Size size) {
 		return trend_box_font_sizes_[size.ordinal()];
 	}
 	
 	public PFont get_trend_box_font(Size size) {
 		return trend_box_fonts_[size.ordinal()];
 	}
 }
