 
 public abstract class CuStat {
 	protected String text = "";
 	@Override public String toString() {
 		return text;
 	}
	public abstract void add (CuStat st);
 }
