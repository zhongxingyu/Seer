 package grafi;
 
 public class ArcoPesato extends Arco {
 
 	private double peso;
 	
 	public ArcoPesato(int in, int fin, double peso) {
 		super(in, fin);
 		this.peso = peso;
 	}
 	
 	public double getPeso() { return peso; }
 
	public void setPeso(double peso) { this.peso = peso; }
 	
 	public String toString() {
 		return "[Arco pesato (" + in + "," + fin + "," + peso + ")]";
 	}
 }
