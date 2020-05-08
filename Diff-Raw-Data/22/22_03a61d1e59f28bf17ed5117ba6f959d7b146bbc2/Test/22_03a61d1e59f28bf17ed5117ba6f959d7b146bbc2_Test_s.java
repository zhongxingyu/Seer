 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.ArrayList;
 
 class OString implements Shorter<OString> {
   final String s;
 
   public OString(String s) {
     this.s = s;
   }
 
   public String toString() { return s; }
   public boolean shorter(OString e) {
     return this.toString().length() < e.toString().length();
   }
 }
 
 public class Test {
   public static void main(String[] args) {
     MyList<String> l = new MyList<String>();
     ListIterator<String> iter = l.iterator();
     iter.add("Hello");
     iter.add("world");
     iter.add("!");
 
     System.out.println("===== debug");
     for(int i = 0; i < l.size(); i++) {
       System.out.println(l.get(i));
     }
 
     iter = l.iterator();
     System.out.println("advanced to: " + iter.next());
     System.out.println("advanced to: " + iter.next());
     iter.set("badum");
 
     l.add(3, "how are you?");
 
     System.out.println("===== debug");
     for(int i = 0; i < l.size(); i++) {
       System.out.println(l.get(i));
     }
 
     l.remove(3);
 
     System.out.println("===== debug");
     for(int i = 0; i < l.size(); i++) {
       System.out.println(l.get(i));
     }
 
     System.out.println(l.contains("Hello"));
     System.out.println(l.contains(new String("Hello")));
 
     System.out.println("===== Set Test");
     Set<String> s = new Set<String>();
 
     s.insert("Hello");
     s.insert("world");
     s.insert("how are you?");
     s.insert("!");
     s.insert("abc");
     s.insert("def");
     s.insert("abc");
     Iterator<String> siter = s.iterator();
 
     System.out.println(s);
 
     System.out.println("===== OrderedSet Test");
 
     ArrayList<OString> t = new ArrayList<OString>();
     t.add(new OString("hello"));
     t.add(new OString("world"));
     t.add(new OString("how are you?"));
     t.add(new OString("abc"));
     t.add(new OString("def"));
     t.add(new OString("abc"));
     t.add(new OString("a234"));
     t.add(new OString("123456"));
     t.add(new OString("12345"));
 
     OrderedSet<OString> oset = new OrderedSet<OString>();
 
     for(OString str : t) {
       //System.out.println("Inserting '" + str + "'");
       oset.insert(str);
       //System.out.println(oset);
     }
     
     System.out.println(oset);
 
 
     System.out.println("===== OrderedMap Test");
     OrderedMap<OString, Integer> omap = new OrderedMap<OString, Integer>();
 
 
     for(OString str : t) {
       // System.out.println("Inserting '" + str + "'");
       omap.insert(str);
       // System.out.println(oset);
     }
     
     System.out.println(omap);
 
     MapIterator<OString, Integer> miter = omap.iterator();
     miter.next();
     miter.next();
     ListIterator<Integer> liter = miter.iterator();
     liter.add(5);
 
     miter.next();
     liter = miter.iterator();
     liter.add(10);
     liter.add(42);
 
     System.out.println(omap);
 
     System.out.println("===== OrderedSet<Description>");
     OrderedSet<Description> oDesc = new OrderedSet<Description>();
     oDesc.insert(new Description("this is a description"));
     oDesc.insert(new Description("another description"));
     oDesc.insert(new Description("very short"));
     oDesc.insert(new Description("and this is a very verbose description"));
     oDesc.insert(new Description("a normal one"));
 
     int num_lines = 0;
     for(Description desc : oDesc) {   /* use iterator implicitly */
       num_lines += desc.numberOfLines();
       System.out.println(desc);
     }
     System.out.println(num_lines);
 
     oDesc.insert(new Description("a medium description"));
     Iterator<Description> oDescIter = oDesc.iterator();
     oDescIter.next();
     oDescIter.next();
     oDescIter.next();
     oDescIter.remove();
 
     System.out.println(oDesc);
 
     /*Erzeugen Sie eine Instanz von OrderedMap, deren Elemente vom Typ MeanElapsedTime sind und die auf Objekte vom Typ CompositeTime verweisen – nicht sehr sinnvoll, aber gut zum Testen geeignet. Fügen Sie einige Elemente und damit verbundene Objekte ein, lesen Sie alles über die Iteratoren aus, und schreiben Sie jeweils den größten Messwert (für Elemente) bzw. die kürzeste Einzelzeit (für Objekte, auf die Elemente verweisen) in die Standard-Ausgabe. Testen Sie Änderungen ähnlich wie bei Punkt 1.*/
 
     System.out.println("===== OrderedMap<MeanElapsedTime, CompositeTime>");
    OrderedMap<MeanElapsedTime,CompositeTime> om = new OrderedMap<MeanElapsedTime,CompositeTime>();
     MeanElapsedTime a = new MeanElapsedTime();
     MeanElapsedTime b = new MeanElapsedTime();
     MeanElapsedTime c = new MeanElapsedTime();
     a.add(new Double(10));
     a.add(new Double(20));
     a.add(new Double(30));
     b.add(new Double(1));
     b.add(new Double(2));
     b.add(new Double(3));
     c.add(new Double(9));
    Double[] x = new Double[]{5,15,30};
     CompositeTime d = new CompositeTime(x);
 
   }
 }
