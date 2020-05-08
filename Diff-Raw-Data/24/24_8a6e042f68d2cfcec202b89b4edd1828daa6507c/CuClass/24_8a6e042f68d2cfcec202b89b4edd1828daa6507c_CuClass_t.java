 
 public abstract class CuClass {
 	protected String text = "";
 	@Override public String toString() {
 		return text;
 	}
	public abstract void add (CuType t);
	
	public abstract void add (CuStat s);
	
	public abstract void add (CuExpr s);
	
	public abstract void add(String v, CuTypeScheme ts, CuStat s);
	
 }
