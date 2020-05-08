 public class Perlenkette{
 
 	private Kreis[] list;
 	private double r;
 	private int length;
 	private int middle;
 
 	public Perlenkette(int l){
 		middle = 300;
 		length = l;
 		list = new Kreis[l];
 		r = 50;
 		for(int i = 0; i < l; i++){
 			list[i] = new Kreis();
 		}
 		reform();
 		for(int i = 0; i < l; i++){
 			list[i].sichtbarMachen();
 		}
 	}
 
 	public int positionX(int k){
 		if (k == 0){
 			return (int) r;
 		}
 		else {
 			double al = (0.5 * list[0].groesseGeben() + sumRbis(k) + 0.5 * list[k].groesseGeben()) / (gesR());
 			double x = Math.cos(al * 2 * Math.PI) * r;
 			return (int) x;
 		}
 	}
 
 	public int positionY(int k){
 		if (k == 0){
 			return 0;
 		}
 		else {
 			double al = (0.5 * list[0].groesseGeben() + sumRbis(k) + 0.5 * list[k].groesseGeben()) / (gesR());
 			double x = Math.sin(al * 2 * Math.PI) * r;
 			return (int) x;
 		}
 	}
 
 	public void reform(){
 		clacR();
 		for(int i = 0; i < length; i++){
 			list[i].xPositionSetzen(positionX(i) + middle - list[i].groesseGeben());
 			list[i].yPositionSetzen(positionY(i) + middle - list[i].groesseGeben());
 			//list[i].sichtbarMachen();
 			list[i].zeichnen();
 		}
 	}
 
 	public void farbeSetzen(int k, String f){
 		if (k == -1) {
 			alleFarbenSetzen(f);
 		}
 		else{
 			list[k].fuellfarbeSetzen(f);
 		}
 	}
 
 	public void groesseSetzen(int k, int g){
 		if (k == -1) {
 			alleGroessenSetzen(g);
 		}
 		else{
 			list[k].groesseSetzen(g);
 		}
 		reform();
 	}
 
 	public void alleFarbenSetzen(String f){
 		for (int i = 0; i < length; i++) {
 			list[i].fuellfarbeSetzen(f);
 		}
 	}
 
 	public void alleGroessenSetzen(int g){
 		for (int i = 0; i < length; i++) {
 			list[i].groesseSetzen(g);
 		}
		reform();
 	}
 
 
 	public void clacR(){
 		if(length > 1){
 			int a = list[0].groesseGeben() + list[1].groesseGeben();
 			double rad0 = (list[0].groesseGeben() * 2 * Math.PI) / gesR();
 			double rad1 = (list[1].groesseGeben() * 2 * Math.PI) / gesR();
 			double al = (rad0 / 2) + (rad1 / 2);
 			r = (0.5 * a) / Math.sin(0.5 * al);
 		}
 		else{
 			r = 0;
 		}
 	}
 
 	public int biggestR(){
 			int b = 0;
 			for(int i = 0; i < length; i++){
 				if(list[i].groesseGeben() > b) b = list[i].groesseGeben();
 			}
 			return b;
 	}
 
 	public int gesR(){
 		int s = 0;
 		for(int i = 0; i < length; i++){
 			s = s + list[i].groesseGeben();
 		}
 		return s;
 	}
 
 	public int sumRbis(int b){
 		int s = 0;
 		for(int i = 1; i < b; i++){
 			s = s + list[i].groesseGeben();
 		}
 		return s;
 	}
 
 	public double rad(double d){
 		return d * Math.PI / 180;
 	}
 
 	public String toString(){
 		String s = "\nRadius:" + Double.toString(r) + "\n\n";
 		for(int i = 0; i < length; i++){
 			s = s + (i + ". (" + (list[i].xPositionGeben() - middle + list[i].groesseGeben()) + ", " 
 				+ (list[i].yPositionGeben() - middle + list[i].groesseGeben()) + ") " 
 				+ list[i].fuellfarbeGeben() + " " + list[i].sichtbarkeitGeben() + " " + 2 * list[i].groesseGeben() 
 				+ " " + list[i].xPositionGeben() + " " + list[i].yPositionGeben() + "\n");
 		}
 		int len = s.length();
 		s = s.substring(0,--len);
 		return s;
 	}
 
 }
 	
