 package orig2011.v7;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 public class ReversiScoreView implements PropertyChangeListener {
 	
 	private ReversiModel evt;
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
		this.evt = ((ReversiModel)evt.getSource());
 		System.out.println("White: " + getWhiteScore() + " Black: " + getBlackScore());
 		System.out.println("It's " + getTurnColor() + "s turn");
 	}
 	
	/**
	 * Return the black player's score
	 * @return score
	 */
 	public int getBlackScore() {
 		return evt.getBlackScore();
 	}
 	
	/**
	 * Return the white player's score
	 * @return score
	 */
 	public int getWhiteScore() {
 		return evt.getWhiteScore();
 	}
 	
 	public String getTurnColor() {
 		return evt.getTurnColor().toString().toLowerCase();
 	}
 
 }
