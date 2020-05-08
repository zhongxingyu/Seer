 package algo.exercise;
 
 import java.util.*;
 
 /**
  * Auswertung von arithmetischen Binaerbaeumen
  *
  * @author Friederike Kunze
  */
 public class Evaluator 
 {
 
     /**
      * Liest den Eingabe-String bzw. die Eingabe-Strings ein.
      * Diese werden zu einem String zusammengehaengt und dieser dann ausgewertet.
      * Das erste Zeichen des String gibt dabei den Modus an: '<' = Prefix, '|' = Infix (Standard), '>' = Postfix.
      *
      * @param args Eingabe-Strings(s)
      */
     public static void main(String[] args) 
     {
         String exp = "";
         if (args.length > 0) 
             exp = args[0];
         for (int i = 1; i < args.length; i++) 
             exp += (" " + args[i]);
         if (exp.length() == 0) 
             System.err.println("Arguments needed!");
         else 
         {
             char mode = args[0].charAt(0);
             if (mode == '<' || mode == '|' || mode == '>') exp = exp.substring(1);
             else mode = '|'; // Standard: Infix
             evaluate(exp, mode);
         }
     }
 
     /**
      * Wertet einen arithmetischen Ausdruck aus. Dazu wird der Ausdruck erst in Tokens zerlegt.
      * Diese werden dann in einen arithmetischen Bin�rbaum umgewandelt, der dann ausgewertet und grafisch dargestellt werden kann.
      *
      * @param exp der arithmetische Ausdruck
      * @param mode: '<': Prefix, '|': Infix, '>': Postfix
      */
     private static void evaluate(String exp, char mode) 
     {
 
         // String in Tokens zerlegen
         Tokenizer t = new Tokenizer(exp);
 
         // Aus den Tokens den arithmetischen Bin�rbaum aufbauen
         Token e = parse(t.tokenize(), mode);
 
         // Testbaum, falls Tokenizer und/oder Parser noch nicht fertig:
         /*
         e = new Op('+',
                    new Op('*',
                           new Num(2),
                           new Num(3)),
                    new Op('-',
                           new Op('/',
                                  new Num(6),
                                  new Num(2)),
                           new Num(1)));
         */
        // Prefix:  + * 2 3 - / 6 2 1 = 8
        // Infix:   ((2 * 3) + ((6 / 2) - 1)) = 8
        // Postfix: 2 3 * 6 2 / 1 - + = 8
        // #Knoten: 9
        // Tiefe:   4
 
         // Ausgabe des arithmetischen Ausdrucks
         System.out.println("Prefix:  " + e.prefix() + " = " + e.eval());
         System.out.println("Infix:   " + e.infix() + " = " + e.eval());
         System.out.println("Postfix: " + e.postfix() + " = " + e.eval());
         System.out.println("#Knoten: " + e.nodes());
         System.out.println("Tiefe:   " + e.depth());
 
         // Grafische Darstellung des arithmetischen Bin�rbaums
         Vis v = new Vis(e, Vis.REGULAR); // Layout 1: gleiche Abst�nde zwischen Knoten
         //Vis v = new Vis(e, Vis.BINARY); // Layout 2: bin�re Unterteilung
         v.setVisible(true); // Grafikfenster sichtbar machen
 
     }
 
     /**
      * Ruft die entsprechende Parse-Methode (Prefix, Infix oder Postfix) auf.
      *
      * @param tok der tokenisierte arithmetische Ausdruck
      * @param mode '<': Prefix, '|': Infix, '>': Postfix
      * @return: der arithmetische Bin�rbaum
      */
     private static Token parse(Vector<Token> tok, char mode) 
     {
         Iterator<Token> i = tok.iterator();
 
         switch (mode) {
             case '<': return parsePrefix(i);
             case '>': return parsePostfix(i);
             default : return parseInfix(i);
         }
     }
 
     /**
      * Parsed Prefix-Ausdrücke zu Bäumen
      * TODO: Fehler erkennen und Exception werfen
      *
      * @param Iterator<Token> i Iterator über die vom Tokenizer gelieferten Tokens
      * @return Token ein Token (Num oder Op)
      */
     private static Token parsePrefix(Iterator<Token> i) 
     {
         Token iter;
 
        while (i.hasNext()) {
            iter = i.next();

            if (iter.type == 'n') {
                return iter;
            } else {
                Token left = parsePrefix(i);
                Token right = parsePrefix(i);

                //nicht fail-safe, eig erst gucken, ob iter.type ok ist
               return new Op(iter.type,left,right);
 
            }
 
         }
     }
 
     /**
      * Parsed Postfix-Ausdrücke zu Bäumen
      * TODO: Fehler erkennen und Exception werfen
      *
      * @param Iterator<Token> i Iterator über die vom Tokenizer gelieferten Tokens
      * @return Token ein Token (Num oder Op)
      */
      private static Token parsePostfix(Iterator<Token> i)
     {
         Stack<Token> s = new Stack<Token>();
         Token iter;
 
         while (i.hasNext()) {
 
             iter = i.next();
 
             if (iter.type == 'n') {
                 s.push(iter);
             } else {
                 Token left = s.pop();
                 Token right = s.pop();
                 s.push(new Op(iter.type, left, right));
             }
 
 
         }
 
         return s.pop();
 
     }
 
     /**
      * Parsed Infix-Ausdrücke zu Bäumen
      * TODO: Fehler erkennen und Exception werfen
      *
      * @param Iterator<Token> i Iterator über die vom Tokenizer gelieferten Tokens
      * @return Token ein Token (Num oder Op)
      */
      private static Token parseInfix(Iterator<Token> i)
     {
         Stack<Token> s = new Stack<Token>();
         Token iter;
 
         while (i.hasNext()) {
 
             iter = i.next();
 
             if ( iter.type == ')' ) {
 
                 Token operand2 = s.pop();
                 Token operator = s.pop();
                 Token operand1 = s.pop();
 
                 s.pop();
 
                 s.push(new Op(operator.type,operand1,operand2));
 
             } else {
                 s.push(iter);
             }
 
         }
 
         return s.pop();
 
     }
 
 }
