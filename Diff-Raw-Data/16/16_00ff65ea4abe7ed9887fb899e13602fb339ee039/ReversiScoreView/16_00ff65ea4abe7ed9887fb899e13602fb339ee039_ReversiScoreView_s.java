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
		if(evt.getSource() instanceof ReversiModel) {
			this.evt = ((ReversiModel)evt.getSource());
		}
		
		
 		System.out.println("White: " + getWhiteScore() + " Black: " + getBlackScore());
 		System.out.println("It's " + getTurnColor() + "s turn");
 	}
 	
 	public int getBlackScore() {
 		return evt.getBlackScore();
 	}
 	
 	public int getWhiteScore() {
 		return evt.getWhiteScore();
 	}
 	
 	public String getTurnColor() {
 		return evt.getTurnColor().toString().toLowerCase();
 	}
 
 }
