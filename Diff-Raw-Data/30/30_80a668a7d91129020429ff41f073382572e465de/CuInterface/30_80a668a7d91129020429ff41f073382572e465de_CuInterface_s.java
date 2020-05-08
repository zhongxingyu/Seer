 
 public abstract class CuInterface {
 	protected String text = "";
 	@Override public String toString() {
 		return text;
 	}
	public abstract void add (CuType tt);
	public abstract void add (String v_name, CuTypeScheme ts);
 }
