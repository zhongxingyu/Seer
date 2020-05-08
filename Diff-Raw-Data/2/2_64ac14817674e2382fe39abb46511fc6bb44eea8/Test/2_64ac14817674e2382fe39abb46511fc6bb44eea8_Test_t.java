 import java.lang.reflect.Method;
 
 /**
  *
  * @author Englisch (e1125164), Lenz (e1126963), Schuster (e1025700)
  * @since December 2012
  *
  */
 @Writer("Lena Lenz")
 public class Test {
 	public static void main(String[] args) {
 		Bauernhof bauernhof = new Bauernhof("Mein Bauernhof");
 		
 		TraktorList list = new TraktorList();
 		Dieseltraktor d1 = new Dieseltraktor();
 		Dieseltraktor d2 = new Dieseltraktor();
 		Dieseltraktor d3 = new Dieseltraktor();
 		Dieseltraktor d4 = new Dieseltraktor();
 		Biogastraktor b1 = new Biogastraktor();
 		Biogastraktor b2 = new Biogastraktor();
 		Biogastraktor b3 = new Biogastraktor();
 		
 		d1.setBetriebsstunden(3);
 		d2.setBetriebsstunden(100);
 		d3.setBetriebsstunden(29);
 		d4.setBetriebsstunden(77);
 		b1.setBetriebsstunden(10);
 		b2.setBetriebsstunden(98);
 		b3.setBetriebsstunden(5);
 		
 		d1.setEinsatzart(new Drillmaschine(100));
 		b1.setEinsatzart(new Duengerstreuer(100));
 		
 		list.append(d1);
 		list.append(d2);
 		list.append(d3);
 		list.append(d4);
 		list.append(b1);
 		list.append(b2);
 		list.append(b3);
 		System.out.println(list);
 		System.out.println(list.find(b1).getData()); //find b1
 		
		bauernhof.addTraktorList(list);
 		System.out.println(bauernhof.avgBetriebsstundenArt());
 		System.out.println(bauernhof.avgBetriebsstundenEinsatz());
 		
 		// Reflection Test
 		System.out.println("\n\nReflection Tests:");
 		System.out.print(getClassMethodWriters(Bauernhof.class));
 		System.out.print(getClassMethodWriters(Biogastraktor.class));
 		System.out.print(getClassMethodWriters(Dieseltraktor.class));
 		System.out.print(getClassMethodWriters(Drillmaschine.class));
 		System.out.print(getClassMethodWriters(Duengerstreuer.class));
 		System.out.print(getClassMethodWriters(Einsatzart.class));
 		System.out.print(getClassMethodWriters(LinkedList.class));
 		System.out.print(getClassMethodWriters(Node.class));
 		System.out.print(getClassMethodWriters(ObjectIterator.class));
 		System.out.print(getClassMethodWriters(Test.class));
 		System.out.print(getClassMethodWriters(Traktor.class));
 		System.out.print(getClassMethodWriters(Writer.class));
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Writer("Jakob Englisch")
 	public static String getClassMethodWriters(Class c) {
 		String tmp = "";
 		if(c == null)
 			return tmp;
 		Writer cw = (Writer) c.getAnnotation(Writer.class);
 		tmp += c.getName() + " wurde geschrieben von: " + (cw == null ? "keine Informationen vorhanden" : cw.value()) + "\n";
 		for(Method m : c.getMethods()) {
 			Writer cm = (Writer) m.getAnnotation(Writer.class);
 			if(cm == null)
 				continue;
 			tmp += c.getName() + ": "+ m.getName() + " wurde geschrieben von: " + cm.value() + "\n";
 		}		
 		return tmp;
 	}
 }
