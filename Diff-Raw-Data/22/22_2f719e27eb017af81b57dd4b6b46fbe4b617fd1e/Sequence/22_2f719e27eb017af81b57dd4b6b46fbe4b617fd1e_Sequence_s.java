 /**
  * A Java Long disguised with a fancier name
  * and some useful methods to represent a
  * DNA sequence of length between 1 - 31.
  * @author Jacob Biggs
  *
  */
 public class Sequence implements Comparable<Sequence> {
 	private Long seq;
 	
 	/**
 	 * Creates a new Sequence with the given
 	 * Long
 	 * @param seq
 	 */
	public Sequence(Long seq){
 		this.seq = seq;
 	}
 	

 	public Sequence(String str, int sequenceLength){
		if (sequenceLength > 31 || sequenceLength < 1){
			throw new IllegalArgumentException("Sequence length was 0 or >31:\n" + str);
 		}
	
 	}
 
 	public Long val(){
 		return this.seq;
 	}
 
 	@Override
 	public int compareTo(Sequence that) {
 		if (this.val() > that.val()) {
 			return 1;
 		} else if(this.val() == that.val()) {
 			return 0;
 		}
 		return -1;
 	}
 }
