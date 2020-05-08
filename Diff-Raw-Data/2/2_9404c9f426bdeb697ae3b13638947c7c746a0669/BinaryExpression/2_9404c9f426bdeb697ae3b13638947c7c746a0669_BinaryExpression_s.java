 /**
  * Die Klasse repräsentiert einen Ausdruck mit einem binären Operator im Syntaxbaum.
  */
 class BinaryExpression extends Expression {
     /** Der linke Operand. */
     Expression leftOperand;
 
     /** Der Operator. */
     Symbol.Id operator;
 
     /** Der rechte Operand. */
     Expression rightOperand;
 
     /**
      * Konstruktor.
      * @param operator Der Operator.
      * @param leftOperand Der linke Operand.
      * @param rightOperand Der rechte Operand.
      */
     BinaryExpression(Expression leftOperand, Symbol.Id operator, Expression rightOperand) {
         super(leftOperand.position);
         this.leftOperand = leftOperand;
         this.operator = operator;
         this.rightOperand = rightOperand;
     }
 
     /**
      * Die Methode führt die Kontextanalyse für diesen Ausdruck durch.
      * @param declarations Die an dieser Stelle gültigen Deklarationen.
      * @return Dieser Ausdruck.
      * @throws CompileException Während der Kontextanylyse wurde ein Fehler
      *         gefunden.
      */
     Expression contextAnalysis(Declarations declarations) throws CompileException {
         leftOperand = leftOperand.contextAnalysis(declarations);
         rightOperand = rightOperand.contextAnalysis(declarations);
         switch (operator) {
         case PLUS:
         case MINUS:
         case TIMES:
         case DIV:
         case MOD:
             leftOperand = leftOperand.unBox();
             rightOperand = rightOperand.unBox();
             leftOperand.type.check(ClassDeclaration.intType, leftOperand.position);
             rightOperand.type.check(ClassDeclaration.intType, rightOperand.position);
             type = ClassDeclaration.intType;
             break;
         /** BEGIN Aufgabe (c): AND, OR, NOT */
         case AND:
         case OR:
         case ANDTHEN:
         case ORELSE:
             leftOperand = leftOperand.unBox();
             rightOperand = rightOperand.unBox();
             leftOperand.type.check(ClassDeclaration.boolType, leftOperand.position);
             rightOperand.type.check(ClassDeclaration.boolType, rightOperand.position);
             type = ClassDeclaration.boolType;
             break;
         /** END Aufgabe (c) */
         case GT:
         case GTEQ:
         case LT:
         case LTEQ:
             leftOperand = leftOperand.unBox();
             rightOperand = rightOperand.unBox();
             leftOperand.type.check(ClassDeclaration.intType, leftOperand.position);
             rightOperand.type.check(ClassDeclaration.intType, rightOperand.position);
             type = ClassDeclaration.boolType;
             break;
         case EQ:
         case NEQ:
             // Wenn einer der beiden Operanden NULL ist, muss der andere
             // ein Objekt sein (oder auch NULL)
             if (leftOperand.type == ClassDeclaration.nullType) {
                 rightOperand = rightOperand.box(declarations);
             } else if (rightOperand.type == ClassDeclaration.nullType) {
                 leftOperand = leftOperand.box(declarations);
             } else {
                 // ansonsten wird versucht, die beiden Operanden in
                 // Basisdatentypen zu wandeln
                 leftOperand = leftOperand.unBox();
                 rightOperand = rightOperand.unBox();
             }
 
             // Nun muss der Typ mindestens eines Operanden gleich oder eine
             // Ableitung des Typs des anderen Operanden sein.
             if (!leftOperand.type.isA(rightOperand.type) &&
                     !rightOperand.type.isA(leftOperand.type)) {
                 ClassDeclaration.typeError(leftOperand.type, rightOperand.position);
             }
             type = ClassDeclaration.boolType;
             break;
         default:
             assert false;
         }
         return this;
     }
 
     /** BEGIN Bonus Aufgabe 2: Konstante Ausdruecke*/
     Expression optimizeTree() throws CompileException {
 	leftOperand = leftOperand.optimizeTree();
 	rightOperand = rightOperand.optimizeTree();
 	// wenn (-(...) # ...)
 	if(leftOperand instanceof UnaryExpression){
 		UnaryExpression e1 = (UnaryExpression)leftOperand;
 		switch (operator){
 		case MINUS:
 			// (-(A) - B) >> -(A+B)
 			operator = Symbol.Id.PLUS;
 			leftOperand = e1.operand;
 			e1.operand = this;
 			return e1.optimizeTree();
 		case PLUS:
 			// (-(A) + B) >> -(A-B)
 			operator = Symbol.Id.MINUS;
 			leftOperand = e1.operand;
 			e1.operand = this;
 			return e1.optimizeTree();
 		case TIMES:
 		case DIV:
 		case OR:
 		case AND:
 			// (-(A) * B) >> -(A*B)
 			leftOperand = e1.operand;
 			e1.operand = this;
 			return e1.optimizeTree();
 		}
 	}
 	// wenn (... # -(...))
 	if(rightOperand instanceof UnaryExpression){
 		UnaryExpression e2 = (UnaryExpression)rightOperand;
 		switch (operator){
 		case MINUS:
 			// (A - -(B)) >> (A+B)
 			operator = Symbol.Id.PLUS;
 			rightOperand = e2.operand;
 			return this.optimizeTree();
 		case PLUS:
 			// (A + -(B)) >> -(A-B)
 			operator = Symbol.Id.MINUS;
 			rightOperand = e2.operand;
 			return this.optimizeTree();
 		case TIMES:
 		case DIV:
 		case OR:
 		case AND:
 			// (A * -(B)) >> -(A*B)
 			rightOperand = e2.operand;
 			e2.operand = this;
 			return e2.optimizeTree();
 		}
 	}
 	if(leftOperand instanceof LiteralExpression){
 		LiteralExpression e1 = (LiteralExpression) leftOperand;
 		if(e1.value == 0){
 			switch (operator){
 				case PLUS:  
 				case OR:
 					return rightOperand;
 				case MINUS:
 					return (new UnaryExpression(Symbol.Id.MINUS, rightOperand , position).optimizeTree());
 				case DIV:
 					if(rightOperand instanceof LiteralExpression){
 						LiteralExpression e2 = (LiteralExpression) rightOperand;
 						if(e2.value == 0){
 							throw new CompileException("Teilen durch 0 bei Konstanten Ausdruecken ", position);
 						}
 					}
 				case TIMES:
 				case AND:
 					return (leftOperand);			
 			}
 		}else if(e1.value == 1){
 			switch (operator){
 				case OR:
 					return leftOperand;
 				case TIMES:
 				case AND:
 					return (leftOperand);			
 			}
 		}
 		if(rightOperand instanceof LiteralExpression){
 			LiteralExpression e2 = (LiteralExpression) rightOperand;
 			switch (operator){
 				case PLUS:  
 					e1.value = e1.value + e2.value;
 					return e1;
 				case MINUS:
					e1.value = e1.value + e2.value;
 					return e1;
 				case AND:
 				case TIMES:
 					e1.value = e1.value * e2.value;
 					return e1;
 				case DIV:
 					e1.value = e1.value / e2.value;
 					return e1;
 				case OR: 
 					e1.value = (e1.value | e2.value); 
 					return e1;
 			}
 		}
 	}
 	if(rightOperand instanceof LiteralExpression){
 		LiteralExpression e2 = (LiteralExpression) rightOperand;
 		if(e2.value == 1 && operator == Symbol.Id.DIV){
 			return leftOperand;
 		}
 	}
 	if(leftOperand instanceof BinaryExpression){
 		BinaryExpression e1 = (BinaryExpression) leftOperand;
 		if(rightOperand instanceof LiteralExpression){
 			LiteralExpression e2 = (LiteralExpression) rightOperand;
 			if(e2.value == 1 && operator == Symbol.Id.DIV){
 				return leftOperand;
 			}		
 			if(!(e1.rightOperand instanceof LiteralExpression)){ 
 			//tauschen ((a # A) #c) >> ((a # c) #A)
 				switch(operator){
 					case PLUS:
 					case MINUS:
 						if(e1.operator == Symbol.Id.PLUS || e1.operator == Symbol.Id.MINUS){
 							Symbol.Id op = operator;
 							operator = e1.operator;
 							e1.operator = op;
 							rightOperand = e1.rightOperand;
 							e1.rightOperand = e2;
 							return this.optimizeTree();
 						}
 					case TIMES:
 					case DIV:
 					case OR:
 					case AND:
 						if(e1.operator == operator){
 							rightOperand = e1.rightOperand;
 							e1.rightOperand = e2;
 							return this.optimizeTree();
 						}
 				}
 			}else{
 				if(operator == Symbol.Id.DIV && e1.operator == Symbol.Id.DIV){
 					LiteralExpression e3 = (LiteralExpression)e1.rightOperand;
 					e3.value = e3.value * e2.value;
 					return e1.optimizeTree();
 				}
 			}
 		}
 	}
 	if(rightOperand instanceof LiteralExpression){
 		Expression e2 = rightOperand;
 		switch(operator){
 			case MINUS:
 				// (A-b) >> (-b +A)
 				operator = Symbol.Id.PLUS;
 				e2 = new UnaryExpression(Symbol.Id.MINUS, e2 ,position);
 			case PLUS:
 			case TIMES:
 			case OR:
 			case AND:
 				rightOperand = leftOperand;
 				leftOperand = e2;
 				return this.optimizeTree();
 		}
 	}
     return this;
     }
 
     /** END Bonus Aufgabe 2*/
 
     /**
      * Die Methode gibt diesen Ausdruck in einer Baumstruktur aus.
      * Wenn der Typ des Ausdrucks bereits ermittelt wurde, wird er auch ausgegeben.
      * @param tree Der Strom, in den die Ausgabe erfolgt.
      */
     void print(TreeStream tree) {
         tree.println(operator + (type == null ? "" : " : " + type.identifier.name));
         tree.indent();
         leftOperand.print(tree);
         rightOperand.print(tree);
         tree.unindent();
     }
 
     /**
      * Die Methode generiert den Assembler-Code für diesen Ausdruck. Sie geht
      * davon aus, dass die Kontextanalyse vorher erfolgreich abgeschlossen wurde.
      * @param code Der Strom, in den die Ausgabe erfolgt.
      */
     void generateCode(CodeStream code) {
         /**BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE*/
         String skipLabel = null;
         if(operator == Symbol.Id.ORELSE || operator == Symbol.Id.ANDTHEN){
             skipLabel = code.nextLabel();
         }
         /**END Bonus Aufgabe 1*/
         leftOperand.generateCode(code);
         /**BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE*/
         code.println("MRM R5, (R2)");
         switch(operator){
           case ORELSE:
             code.println("JPC R5, "+skipLabel + " ; Sprung zum Ende der Auswertung");
             break;
           case ANDTHEN:
             code.println("ISZ R5, R5");
             code.println("JPC R5, "+skipLabel + " ; Sprung zum Ende der Auswertung");
             break;
         }
         /**END Bonus Aufgabe 1*/
         rightOperand.generateCode(code);
         code.println("; " + operator);
         code.println("MRM R5, (R2)");
         code.println("SUB R2, R1");
         code.println("MRM R6, (R2)");
         switch (operator) {
         case PLUS:
             code.println("ADD R6, R5");
             break;
         case MINUS:
             code.println("SUB R6, R5");
             break;
         case TIMES:
             code.println("MUL R6, R5");
             break;
         case DIV:
 	    /** BEGIN Bonus Aufgabe (4): Try&Catch-Erweiterung*/
 	    String catchDiv0Label = code.nextLabel();
 	    code.println("JPC R5, " + catchDiv0Label + " ; Überspringe Fehlerbehandlung /0");
 	    new ThrowStatement(new LiteralExpression(0, ClassDeclaration.intType, position)).generateCode(code);
 	    code.println(catchDiv0Label + ":");
 	    /** END Bonus Aufgabe (4)*/
             code.println("DIV R6, R5");
             break;
         case MOD:
             code.println("MOD R6, R5");
             break;
         case GT:
             code.println("SUB R6, R5");
             code.println("ISP R6, R6");
             break;
         case GTEQ:
             code.println("SUB R6, R5");
             code.println("ISN R6, R6");
             code.println("XOR R6, R1");
             break;
         case LT:
             code.println("SUB R6, R5");
             code.println("ISN R6, R6");
             break;
         case LTEQ:
             code.println("SUB R6, R5");
             code.println("ISP R6, R6");
             code.println("XOR R6, R1");
             break;
         case EQ:
             code.println("SUB R6, R5");
             code.println("ISZ R6, R6");
             break;
         case NEQ:
             code.println("SUB R6, R5");
             code.println("ISZ R6, R6");
             code.println("XOR R6, R1");
             break;
         /** BEGIN Aufgabe (c): AND, OR, NOT */
         case AND:
         case ANDTHEN: // Bonus Aufgabe 1: AND THEN und OR ELSE
             code.println("AND R6, R5");
             break;
         case OR:
         case ORELSE: // Bonus Aufgabe 1: AND THEN und OR ELSE
             code.println("OR R6, R5");
             break;
         /** END Aufgabe (c)*/
         default:
             assert false;
         }
         /** BEGIN Bonus Aufgabe 1: AND THEN und OR ELSE */
         code.println("MMR (R2), R6");
         if(skipLabel != null){
             code.println(""+skipLabel+":");
         }
         /** END Bonus Aufgabe 1*/
     }
 }
