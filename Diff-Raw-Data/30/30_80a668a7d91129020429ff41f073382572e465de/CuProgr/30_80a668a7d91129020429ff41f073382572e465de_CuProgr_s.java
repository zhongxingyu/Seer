 
 public abstract class CuProgr {
 	protected String text = "";
 	@Override public String toString() {
 		return text.replaceAll("[ ]+", " ");
 	}
 }
