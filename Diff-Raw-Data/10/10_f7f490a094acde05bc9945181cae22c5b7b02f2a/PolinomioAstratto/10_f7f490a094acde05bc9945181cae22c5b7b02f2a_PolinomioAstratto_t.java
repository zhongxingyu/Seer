 package poo.polinomi;
 
 import java.util.*;
 
 public abstract class PolinomioAstratto implements Polinomio {
 	
 	public abstract Iterator<Monomio> iterator();
 	public abstract void add(Monomio m);
 	protected abstract Polinomio crea();
 
 	public int size() {
 		int c = 0;
 		for (Monomio m: this) c++;
 		return c;
 	} // size
 	public Polinomio add(Polinomio p) {
 		Polinomio somma = crea();
 		for (Monomio m: this) somma.add(m);
 		for (Monomio m: p) somma.add(m);
 		return somma;
 	} // add
 	public Polinomio mul(Monomio m) {
 		Polinomio prodotto = crea();
 		for (Monomio q: this)
 			prodotto.add(m.mul(q));
 		return prodotto;
 	} // mul
 	public Polinomio mul(Polinomio p) {
 		Polinomio prodotto = crea();
 		for (Monomio m: this)
 			prodotto = prodotto.add(p.mul(m));
 		return prodotto;
 	} // mul
 	public double valore(double x) { // Formula di Horner
		double valore = 0; int gradoPrec = 0; int salto;
 		for (Monomio m: this) {
			salto = gradoPrec - m.getGrado();
 			for (int i = 1; i < salto; i++)
				valore *= x;
 			valore = valore * x + m.getCoeff();
 			gradoPrec = m.getGrado();
 		}
		for (int i = 0; i < gradoPrec; i++) valore *= x;
 		return valore;
 	} // valore
 	public Polinomio derivata() {
 		Polinomio derivata = crea();
 		for (Monomio m: this)
 			if (m.getGrado() > 0)
 				derivata.add(new Monomio(m.getCoeff() * m.getGrado(), m.getGrado() - 1));
 		return derivata;
 	} // valore
 	public boolean equals(Object o) {
 		if (!(o instanceof Polinomio)) return false;
 		if (o == this) return true;
 		Polinomio p = (Polinomio)o;
 		if (size() != p.size()) return false;
 		Iterator<Monomio> i1 = iterator();
 		Iterator<Monomio> i2 = p.iterator();
 		while (i1.hasNext()) {
 			Monomio m1 = i1.next(); Monomio m2 = i2.next();
 			if (m1.getCoeff() != m2.getCoeff() || m1.getGrado() != m2.getGrado())
 				return false;
 		}
 		return true;
 	} // equals
 	public int hashCode() {
 		int h = 0; final int MOLT = 43;
 		for (Monomio m: this)
 			h = h * MOLT + m.hashCode();
 		return h;
 	} // hashCode
 	public String toString() {
 		StringBuilder sb = new StringBuilder(6 * size());
 		for (Monomio m: this) {
 			if (m.getCoeff() > 0) sb.append('+');
 			sb.append(m);
 		}
 		return sb.substring(sb.charAt(0) == '+' ? 1 : 0);
 	} // toString
 } // PolinomioAstratto
