 
 public class Test2 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		boolean res = false;
		CardSDReader sdr = new CardSDReader();
		sdr.insert(new CardSD("SD"));
		Port<CardSD> p1 = sdr.getSlot();
 	}
 
 }
