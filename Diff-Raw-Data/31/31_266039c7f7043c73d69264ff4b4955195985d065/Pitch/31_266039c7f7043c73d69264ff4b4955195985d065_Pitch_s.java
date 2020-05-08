 package pipes.model;
 
 public enum Pitch {
 	LOW_G ("g"),
 	LOW_A ("a"),
 	B ("b"),
 	C ("c"),
 	D ("d"),
 	E ("e"),
 	F ("f"),
 	G ("G"),
	A ("A"),
	Silent ("");
 
 	public static Pitch fromString(String s) {
 		switch (s) {
 		case "g": return LOW_G;
 		case "a": return LOW_A;
 		case "b": return B;
 		case "c": return C;
 		case "d": return D;
 		case "e": return E;
 		case "f": return F;
 		case "G": return G;
 		case "A": return A;
 		default: return null;
 		}
 	}
 	
 	public final String shortName;
 	
 	public boolean higherThan(Pitch other) {
 		return ordinal() > other.ordinal();
 	}
 	
 	public boolean higherOrEqual(Pitch other) {
 		return ordinal() >= other.ordinal();
 	}
 	
 	Pitch(String shortName) {
 		this.shortName = shortName;
 	}
 }
