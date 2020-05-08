 public class ComparatorScript {
 
	public static void main(String[] args) {
		ComparatorScript script = new ComparatorScript();
		script.launch();
	}
  
 	public void launch() {
		Comparator myComp = new Comparator();
		System.out.println(myComp.getMax(1,2));
		System.out.println(myComp.getMax(1.0,2.0));
		System.out.println(myComp.getMax("1","2"));
 	}	
 }	
