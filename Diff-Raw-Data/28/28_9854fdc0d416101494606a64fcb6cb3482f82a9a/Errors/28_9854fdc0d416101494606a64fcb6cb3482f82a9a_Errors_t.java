 package uk.co.thomasc.wordmaster.view;
 
 import uk.co.thomasc.wordmaster.R;
 
 public enum Errors {
	NETWORK(R.string.network_error, R.string.network_error_help),
	TURN(R.string.turn_error, R.string.turn_error_help)
 	;
 	
 	private int title;
 	private int subtitle;
 	
 	private Errors(int title, int subtitle) {
 		this.title = title;
 		this.subtitle = subtitle;
 	}
 	
 	public int getTitle() {
 		return title;
 	}
 	
 	public int getSubtitle() {
 		return subtitle;
 	}
 }
