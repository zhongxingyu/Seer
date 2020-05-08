 public class demo {
 	public static void main (String [] args) {
		//beta lmao = new beta();
 		//lmao.m3();
 		//String r = lmao.m2().m3();
 		//lmao.m3();
		//String a = lmao.m1().m2().m3(); //should print out ok
 		//System.out.println(r);
 		//String r = new String("Kitten");
		beta k1 = new beta();
		if ("xtc.oop.Test".equals(k1.getName())) {
			System.out.println("PASS \"xtc.oop.Test\".equals(k1.getName())");
			//success++;
		} else {
			System.out.println("FAIL \"xtc.oop.Test\".equals(k1.getName())");
		}
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
	public String m3(){
		return "thisssss";
 	}
 	public String getName(){
 		return "xtc.oop.Test";
 	}
 	
 }
