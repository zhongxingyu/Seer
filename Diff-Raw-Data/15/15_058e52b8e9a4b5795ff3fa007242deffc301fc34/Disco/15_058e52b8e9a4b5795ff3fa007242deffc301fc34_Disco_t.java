 package poo.geometria;
 
 public class Disco extends Punto {
 	private double raggio;
 	public Disco() { raggio = 1; } // Costruttore di default
 	public Disco(double x, double y, double raggio) {
		super(x, y);
		if (raggio <= 0) throw new IllegalArgumentException();
		this.raggio = raggio;
 	} // Costruttore 1
 	public Disco(Punto centro, double raggio) {
		super(centro);
		if (raggio <= 0) throw new IllegalArgumentException();
		this.raggio = raggio;
 	} // Costruttore 2
 	public Disco(Disco d) {
 		super(d); raggio = d.raggio;
 	} // Costruttore di copia
 	public double getRaggio() { return raggio; }
 	public double area() {
 		return Math.PI * raggio * raggio;
 	} // area
 	public double perimetro() {
 		return 2 * Math.PI * raggio;
 	} // perimetro
 	public String toString() {
 		return "Disco di centro: " + super.toString() + String.format(" e raggio = %1.2f", raggio);
 	} // toString
 	public boolean equals(Object o) {
 		if (!(o instanceof Disco)) return false;
 		if (o == this) return true;
 		Disco d = (Disco)o;
 		return super.equals(d) && raggio == d.raggio;
 	} // equals
 	public int hashCode() {
 		final int PRIMO = 17;
 		long r = Double.doubleToLongBits(raggio);
 		return super.hashCode() * PRIMO + (int)(r ^ (r >>> 32));
 	} // hashCode
 } // Disco
