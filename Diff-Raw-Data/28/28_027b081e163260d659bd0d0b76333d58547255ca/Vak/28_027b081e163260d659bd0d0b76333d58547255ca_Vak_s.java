 public class Vak {
 
 	private String naam;
 	private int minPathValue;
	private int maxPathValue;
 
 	public Vak(String naam) {
 		this.naam = naam;
 	}
 
 	public String getNaam() {
 		return naam;
 	}
 
 	public void setNaam(String naam) {
 		this.naam = naam;
 	}
 
 	public void print() {
 		System.out.println(naam);
 	}
 
 	public int getMinPathValue() {
 		return minPathValue;
 	}
 
 	public void setMinPathValue(int minPathValue) {
 		this.minPathValue = minPathValue;
 	}
 
 	public int getMaxPathValue() {
 		return maxPathValue;
 	}
 
 	public void setMaxPathValue(int maxPathValue) {
 		this.maxPathValue = maxPathValue;
 	}
 }
