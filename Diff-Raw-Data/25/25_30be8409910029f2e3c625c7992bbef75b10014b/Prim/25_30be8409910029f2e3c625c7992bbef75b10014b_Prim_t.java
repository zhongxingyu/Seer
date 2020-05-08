 public class Prim
 {
     public static void main(String[] s) throws Exception{
     	int g = 0;
     	try{
     		g = new Integer(Integer.parseInt(s[0]));
     	}
     	catch(java.lang.ArrayIndexOutOfBoundsException e){
    			System.out.println("Wenn sie ein paar Primzahlen wollen, dann sind sie hier genau richtig.");
     		System.out.print("Aber zuerst brauchen wir eine oberer Grenze: ");
            g = ein();
     	}
        catch(java.lang.NumberFormatException e){
            System.out.println("Das ist zwar ganz schön, aber wir brauchen eine Zahl.");
            System.out.print("Bitte geben sie eine Zahl >0 ein: ");
            g = ein();
        }
     	while(g < 0){
     		System.out.println("Die eingegebene Nummer ist leider nicht gültig.");
     		System.out.print("Bitte geben sie eine Zahl >0 ein: ");
    		g = ein();
     	}
     	new Prim(g);
 
     }
 
     public Prim(int i){
     	System.out.println(i);
     }
 
//handeld schlechten input

    public static int ein()throws Exception{
        try{
            return EA.einInt();
        }
        catch(java.lang.NumberFormatException e){
            System.out.println("Das ist zwar ganz schön, aber wir brauchen eine Zahl.");
            System.out.print("Bitte geben sie eine Zahl >0 ein: ");
            return ein();
        }
    }    

 //dekrementiert die übergebene Ganzzahl. 
 
     public int dek(int i){
     	return --i;
     }
 
 //sagt, ob die zweite übergebene Ganzzahl ein Vielfaches der ersten übergebenen Ganzzahl ist.
 
     public boolean vielfach(int a, int b){
     	if(a == 0 || a == 0){
     		return false;
     	}
 
     	int div = b / a;
     	double divd = (double)b / (double)a;
     	double divi = (double)div;
 
     	if(divd == divi) {
     		return true;
     	}
     	else{
     		return false;
     	}
     }
 
 //wirft alle Ganzzahlen, die die übergebene Bedingung erfüllen, aus der Liste hinaus.
 
     public IntListe werfen(int i, IntListe is){
     	return is;
     }
 
 /*
 Vorstufe für das Sieb des Eratosthenes: Aus der übergebenen Liste werden
 das vorderste Element und alle weiteren Elemente, die keine Vielfachen
 des vordersten Elements sind, in die zurück gegebene Ergebnis-Liste übernommen.
 */
 
     public IntListe sieb(IntListe is){
     	return is;
     }
 
 //Sieb des Eratosthenes.
 
     public IntListe prims(IntListe is){
     	return is;
     }
 
 //gibt eine Liste mit allen Primzahlen bis zur übergebenen
 
 	public IntListe primzahlen(int i){
 		return new IntListe();
 	}
 
 }
