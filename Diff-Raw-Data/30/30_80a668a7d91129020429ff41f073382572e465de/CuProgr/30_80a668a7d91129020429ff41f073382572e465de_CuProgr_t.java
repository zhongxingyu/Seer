 
 public abstract class CuProgr {
 	protected String text = "";
 	@Override public String toString() {
 		return text.replaceAll("[ ]+", " ");
 	}
	public void add (String var, CuTypeScheme ts, CuStat s){};
	public void add (CuProgr p){};
 }
