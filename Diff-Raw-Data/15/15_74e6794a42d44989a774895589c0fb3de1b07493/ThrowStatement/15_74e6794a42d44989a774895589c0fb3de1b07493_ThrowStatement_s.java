 import java.util.LinkedList;
 
 /** BEGIN Aufgabe (h): Ausnahmebehandlung */
 
 /**
  * Die Klasse repräsentiert die Anweisung THROW im Syntaxbaum.
  */
 class ThrowStatement extends Statement {
   /** Der geworfene Ausnahmecode der THROW-Anweisung. */
   Expression value;
 
   /**
    * Konstruktor.
    */
   ThrowStatement(Expression value) {
     this.value = value;
   }
 
   /**
    * Die Methode führt die Kontextanalyse für diese Anweisung durch.
    * @param declarations Die an dieser Stelle gültigen Deklarationen.
    * @throws CompileException Während der Kontextanylyse wurde ein Fehler
    *         gefunden.
    */
   void contextAnalysis(Declarations declarations) throws CompileException {
     value = value.contextAnalysis(declarations);
     value = value.unBox();
     value.type.check(ClassDeclaration.intType, value.position);
   }
 
   /**
    * Die Methode gibt diese Anweisung in einer Baumstruktur aus.
    * @param tree Der Strom, in den die Ausgabe erfolgt.
    */
   void print(TreeStream tree) {
     tree.println("THROW");
     tree.indent();
     value.print(tree);
     tree.unindent();
   }
 
   /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
   Statement optimizeStatement() throws CompileException {
     value = value.optimizeTree();
     return this;
   }
   /** END Bonus Aufgabe 2*/
 
   /**
    * Die Methode generiert den Assembler-Code für diese Anweisung. Sie geht 
    * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
    * @param code Der Strom, in den die Ausgabe erfolgt.
    */
   void generateCode(CodeStream code) {
     code.println("; THROW ");
     value.generateCode(code);
     code.println("MRI R7, _exception ; _exception Adresse holen");
     code.println("MRM R7, (R7) ; _exception holen");
    code.println("MRI R5, 3 ; offset fuer Ausnahmebehandlung");
     code.println("ADD R7, R5 ; zeigt auf Stelle mit der naechste Ausnahmebehandlung");
     code.println("MRM R0, (R7) ; springe zur naechsten Ausnahmebehandlung");
   }
 }
 /** END Aufgabe (h) */
