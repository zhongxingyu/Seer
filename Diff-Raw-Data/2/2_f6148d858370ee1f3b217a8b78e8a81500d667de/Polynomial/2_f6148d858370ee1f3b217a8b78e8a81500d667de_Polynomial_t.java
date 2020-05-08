 package hse.kcvc.jminmaxgd;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * User: Kouprianov Maxim
  * Date: 10.04.12
  * Time: 14:01
  * Contact: me@kc.vc
  */
 public class Polynomial {
     private ArrayList<Monomial> data;
 
     public boolean isSimple() {
         return simple;
     }
 
     private boolean simple;
 
     public void sort() {
         Collections.sort(data);
         Collections.reverse(data);
     }
 
     public int getCount() {
         return data.size();
     }
 
     public Monomial getElement(int n) {
         return data.get(n);
     }
 
     public Polynomial(final ArrayList<Monomial> list) {
         this();
 
        if (list.size() >= 1) {
             this.data = new ArrayList<Monomial>(list);
             simple = false;
             sortSimplify();
         }
     }
 
     public Polynomial() {
         this.data = new ArrayList<Monomial>(Constants.POLY_SIZE);
         this.data.add(new Monomial());
         this.simple = true;
     }
 
     public Polynomial(final Monomial gd) {
         this.data = new ArrayList<Monomial>(Constants.POLY_SIZE);
         this.data.add(gd);
         this.simple = true;
     }
 
     public void sortSimplify() {
         if (simple) return;
         if (data != null) {
             sort();
             simplify();
         }
     }
 
     public void simplify() {
         if (data != null) {
             int i = 0;
             for (int j = 1; j < data.size(); ++j) {
                 if (data.get(j).getDelta() > data.get(i).getDelta()) {
                     ++i;
                     data.set(i, data.get(j));
                 }
             }
 
             //first item is on top, by sort
             data = new ArrayList<Monomial>(data.subList(0, i + 1));
             this.simple = true;
         }
     }
 
     public void addElement(final Monomial gd) {
         if (data.size() > 1) {
             data.add(gd);
         } else {
             if (data.get(0).compareTo(new Monomial()) == 0) data.set(0, gd);
             else {
                 data.add(gd);
             }
         }
 
         this.simple = false;
     }
 
     public Polynomial oplus(final Polynomial poly2) {
         ArrayList<Monomial> list = new ArrayList<Monomial>(data);
         list.addAll(poly2.data);
 
         return new Polynomial(list);
     }
 
     public Polynomial oplus(final Monomial gd) {
         Polynomial result = new Polynomial(data);
         result.addElement(gd);
         result.sortSimplify();
 
         return result;
     }
 
     public Polynomial otimes(Polynomial poly2) {
         ArrayList<Monomial> result = new ArrayList<Monomial>(this.data.size() * poly2.data.size());
         for (Monomial m1 : this.data)
             for (Monomial m2 : poly2.data)
                 result.add(m1.otimes(m2));
         return new Polynomial(result);
     }
 
     public Polynomial otimes(Monomial gd) {
         return new Polynomial(gd).otimes(this);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj != null && obj.getClass() != this.getClass()) return false;
 
         Polynomial poly2 = (Polynomial) obj;
         int i = 0;
         if (poly2 == null || poly2.data.size() != data.size()) return false;
         else
             while (i < this.getCount()) {
                 if (data.get(i) != poly2.data.get(i)) return false;
                 ++i;
             }
         return true;
     }
 
     @Override
     public String toString() {
         int last = data.size() - 1;
         String out = "";
         for (int i = 0; i < last; ++i) {
             out += data.get(i).toString() + " + ";
         }
 
         out += data.get(last).toString();
         return out;
     }
 }
