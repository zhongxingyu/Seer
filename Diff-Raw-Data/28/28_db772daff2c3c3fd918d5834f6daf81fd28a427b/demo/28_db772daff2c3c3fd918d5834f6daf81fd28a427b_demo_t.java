 public class demo {
 	public static void main (String [] args) {
		beta lmao = new beta();
 		//lmao.m3();
 		//String r = lmao.m2().m3();
 		//lmao.m3();
		lmao.m1().m2().m3(); //should print out this
 		//System.out.println(r);
 		//String r = new String("Kitten");
		
 		//test++;
 	}
 }
 class beta {
 	beta () {
 	}
 	public beta m1(){
 		beta a= new beta ();
 		return a;
 	}
 	public beta m2() {
 		beta b = new beta();
 		return b;
 	}
	public void m3(){
		System.out.print("thisssss");
 	}
 	public String getName(){
 		return "xtc.oop.Test";
 	}
 	
 }
