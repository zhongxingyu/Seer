 package bookshelf;
 
 public class Keyword implements Comparable<Keyword> {
 	public final String value;
	public final float importance;
 	
	public Keyword(String value, float importance) {
 		this.value = value.toLowerCase();
 		this.importance = importance;
 	}
 	
 	public String getValue() {
 		return value;
 	}
 
	public float getImportance() {
 		return importance;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Keyword other = (Keyword) obj;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 
 	@Override
 	public int compareTo(Keyword o) {
 		return this.getValue().compareTo(o.getValue());
 	}
 }
