 // PENDIENTE El compilador detecta como error si ponemos un 1 o un 0.
 // PENDIENTE Aplicar bloques static para inicializar atributos de clase.
 // PENDIENTE Organizar los programas de ejemplo a entregar.
 
 /* @author Leonardo Gutiérrez Ramírez <leogutierrezramirez.gmail.com> */
 /* Nov 20, 2011 */
 
 package Clases;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import javax.swing.JOptionPane;
 
 public class Parser {
     
     public static enum TokenType {
        ERROR, 
        IF, THEN, ELSE, END, 
        REPETIR, HASTA, 
        LEER, WRITE, 
        ID, NUM, 
        ASSIGN, EQ, LT, PLUS, MINUS, TIMES, OVER, LPARENT, RPARENT, SEMI, POW, COMA, MOD, LEQ, GEQ, GT, NEQ, NOT, COMP, AND, 
        OR, WHILE, INFINITO,INIBLOQUE, FINBLOQUE, INICIO, FINALIZAR, INC, DEC, SENO, COSENO, ABS, 
        TAN, LN, CLEAR, SALIR,
        FACTORIAL, RAIZ,
        HEX, CADENA, BOOLEANO, BINARIO, ENTERO, DECIMAL
     };
     
     static {
         arbolString = "";
         indentno = 0;
         tokenString = "";
         lineno = 0;
     }
    
     TablaSimbolos tabla = new TablaSimbolos();
     String errorString = "";
     ExpType asignacionTipo = ExpType.Void;
     String programName = null;
     static public String arbolString = "";
     private ArrayList<Lexema> palabras = null;   
     public static final int MAXCHILDREN = 3;
     public static int indentno = 0;
     private int indice = 0;
     public ArrayList<String> tokensPila = new ArrayList<String>();
     public static TokenType token;
     public static String tokenString;
     public static int lineno = 0;
 
     
     public static enum NodeKind {
         StmtK, ExpK
      };
     
     public static enum StmtKind {
         IfK, 
         RepeatK,
         AssignK,
         ReadK,
         WriteK, 
         WhileK, 
         InfinitumK, 
         PowK,
         IncK,
         DecK,
         SenoK,
         CosenoK,
         AbsK,
         TanK,
         LnK,
         ClearK,
         SalirK,
         FactorialK, 
         RaizK,
         HexK, 
         BooleanK, 
         BinarioK,
         EnteroK,
         DecimalK,
         ProgramK
     };
     
     public static enum ExpKind {
         OpK, ConstK, IdK
     };
     
     // Enumeración para verificación de Tipo.
     public static enum ExpType {
         Void, Integer, Boolean, Binario, Entero, Float, Decimal
     };
 
     private boolean isBinary(String s) {
         for(int i = 0; i < s.length(); i++) {
             if(s.charAt(i) != '0' && s.charAt(i) != '1') {
                 return false;
             }
         }
         return true;
     }
     
     public static boolean isFloat(String token) {
         if(Pattern.matches("[+-]?\\d*\\.\\d+", token))
             return true;
         return Pattern.matches("[+-]?\\d*[\\.]?\\d+[eE][+-]?\\d+", token);
     }
 
     public static boolean isEntero(String token) {
         return Pattern.matches("[+-]?\\d+", token) && token.charAt(0) != '0';
     }
 
     public int bin2int(String str) {
         return Integer.parseInt(str, 2);
     }
     
     public String getErrorString() {
         return errorString;
     }
     
     public ExpType getTipoDatoString(String s) {
         if(isFloat(s)) {
             return ExpType.Decimal;
         } else if(isEntero(s) && s.charAt(0) != '0') {
             return ExpType.Entero;
         } else if(isBinary(s)) {
             return ExpType.Binario;
         } else
             return ExpType.Void;
     }
     
     public TablaSimbolos getTablaSimbolos() {
         return this.tabla;
     }
     
     public static void indentar() {
         indentno += 4;
     }
     
     public static void unindent() {
         indentno -= 4;
     }
     
     public static void imprimirEspacios() {
         for(int i = 0; i < indentno; i++)
             System.out.print(" ");
     }
     
     public static void imprimirEspaciosArchivo() {
         for(int i = 0; i < indentno; i++)
             arbolString += " ";
     }
     
     public static void imprimirArbol(NodoArbol arbol) {
         
         indentar();
         while(arbol != null) {
             imprimirEspacios();
             if(arbol.nodeKind == NodeKind.StmtK) {
                 switch(arbol.stmt) {
                     case IfK:
                         System.out.println("If");
                         break;
                     case RepeatK:
                         System.out.println("Repetir");
                         break;
                     case AssignK:
                         System.out.println("Asignado a: " + arbol.nombre);
                         break;
                     case ReadK:
                         System.out.println("Read: " + arbol.nombre);
                         break;
                     case WriteK:
                         System.out.println("Write");
                         break;
                     case WhileK:
                         System.out.println("While");
                         break;
                     case InfinitumK:
                         System.out.println("Infinito");
                         break;
                     case PowK:
                         System.out.println("Pow");
                         break;
                     case IncK:
                         System.out.println("Inc");
                         break;             
                     case DecK:
                         System.out.println("Dec");
                         break;
                     case SenoK:
                         System.out.println("Seno");
                         break;
                     case CosenoK:
                         System.out.println("Coseno");
                         break;
                     case AbsK:
                         System.out.println("Abs");
                         break;
                     case TanK:
                         System.out.println("Tan");
                         break;
                     case LnK:
                         System.out.println("Ln");
                         break;
                     case ClearK:
                         System.out.println("Clear");
                         break;
                     case SalirK:
                         System.out.println("Salir");
                         break;
                     case FactorialK:
                         System.out.println("Factorial");
                         break;
                     case RaizK:
                         System.out.println("Raiz");
                         break;
                     case HexK:
                         System.out.println("Hex : [" + arbol.nombre + "]");
                         break;                        
                     case BooleanK:
                         System.out.println("Asignado a: " + arbol.nombre);
                         break;    
                     case BinarioK:
                         System.out.println("Asignado a: " + arbol.nombre);
                         break;
                     case EnteroK:
                         // Generador de código aquí.
                         System.out.println("Asignado a: " + arbol.nombre);
                         break;
                     case DecimalK:
                         System.out.println("Asignado a: " + arbol.nombre);
                         break;
                     default:
                         System.out.println("Nodo desconocido");
                 }
             } else if(arbol.nodeKind == NodeKind.ExpK) {
                 
                 switch(arbol.exp) {
                     case OpK:
                         System.out.print("op: ");
                         printToken(arbol.op, "\\0");
                         break;
                     case ConstK:
                         if(arbol.type == ExpType.Entero || arbol.type == ExpType.Binario)
                             System.out.println("const: " + arbol.valor);
                         else
                             System.out.println("const: " + arbol.valorDecimal);
                         
                         break;
                     case IdK:
                         System.out.println("id: " + arbol.nombre);
                         break;
                     default:
                         System.out.println("Nodo desconocido");
                         break;
                 }
                 
             } else {
                 System.out.println("Nodo desconocido");
             }
             
             for(int j = 0; j < MAXCHILDREN; j++)
                 imprimirArbol(arbol.hijos[j]);
             
             arbol = arbol.hermano;
             
         }
         unindent();    
     }
     
     public static void imprimirArbolArchivo(NodoArbol arbol) {
         
         indentar();
         while(arbol != null) {
             imprimirEspaciosArchivo();
             if(arbol.nodeKind == NodeKind.StmtK) {
                 switch(arbol.stmt) {
                     case IfK:
                         arbolString += "If\n";
                         break;
                     case RepeatK:
                         arbolString += "Repetir\n";
                         break;
                     case AssignK:
                         arbolString += "Asignado a: " + arbol.nombre + "\n";
                         break;
                     case ReadK:
                         arbolString += "Read: " + arbol.nombre + "\n";
                         break;
                     case WriteK:
                         System.out.println("Write");
                         arbolString += "Write\n";
                         break;
                     case WhileK:
                         arbolString += "While\n";
                         break;
                     case InfinitumK:
                         arbolString += "Infinito\n";
                         break;
                     case PowK:
                         arbolString += "Pow\n";
                         break;
                     case IncK:
                         arbolString += "Inc\n";
                         break;             
                     case DecK:
                         arbolString += "Dec\n";
                         break;
                     case SenoK:
                         arbolString += "Seno\n";
                         break;
                     case CosenoK:
                         arbolString += "Coseno\n";
                         break;
                     case AbsK:
                         arbolString += "Abs\n";
                         break;
                     case TanK:
                         arbolString += "Tan\n";
                         break;
                     case LnK:
                         arbolString += "Ln\n";
                         break;
                     case ClearK:
                         arbolString += "Clear\n";
                         break;
                     case SalirK:
                         arbolString += "Salir\n";
                         break;
                     case FactorialK:
                         arbolString += "Factorial\n";
                         break;
                     case RaizK:
                         arbolString += "Raiz\n";
                         break;
                     case HexK:
                         arbolString += "Hex : [" + arbol.nombre + "]\n";
                         break;                        
                     case BooleanK:
                         arbolString += "Asignado a: " + arbol.nombre + "\n";
                         break;    
                     case BinarioK:
                         arbolString += "Asignado a: " + arbol.nombre + "\n";
                         break;
                     case EnteroK:
                         // Generador de código aquí.
                         arbolString += "Asignado a: " + arbol.nombre + "\n";
                         break;
                     case DecimalK:
                         arbolString += "Asignado a: " + arbol.nombre + "\n";
                         break;
                     default:
                         arbolString += "Nodo desconocido\n";
                 }
             } else if(arbol.nodeKind == NodeKind.ExpK) {
                 
                 switch(arbol.exp) {
                     case OpK:
                         arbolString += "op: ";
                         printTokenArchivo(arbol.op, "\\0");
                         break;
                     case ConstK:
                         if(arbol.type == ExpType.Entero || arbol.type == ExpType.Binario) {
                             arbolString += "const: " + arbol.valor + "\n";
                         } else {
                             arbolString += "const: " + arbol.valorDecimal + "\n";
                         }
                         
                         break;
                     case IdK:
                         arbolString += "id: " + arbol.nombre + "\n";
                         break;
                     default:
                         arbolString += "Nodo desconocido\n";
                         break;
                 }
                 
             } else {
                 arbolString += "Nodo desconocido\n";
             }
             
             for(int j = 0; j < MAXCHILDREN; j++)
                 imprimirArbolArchivo(arbol.hijos[j]);
             
             arbol = arbol.hermano;
             
         }
         unindent();
     }
    
    public Parser() {
        palabras = null;
        indice = 0;
        tabla = new TablaSimbolos();   
        errorString = "";
        arbolString = "";
        lineno = 0;
    }
     
    public Parser(ArrayList<Lexema> palabras, String programName) {
        this.palabras = palabras;
        indice = 0;
        tabla = new TablaSimbolos();
        errorString = "";
        this.programName = programName;
        arbolString = "";
        lineno = 0;
    }
    
    private static void postorden(NodoArbol a) {
 	if(a != null) {
 		postorden(a.hijos[0]);
 		postorden(a.hijos[1]);
 		if(a.nombre != null) {
                     System.out.print(a.nombre);  
                 } else if(a.op != null) {
                     switch(a.op) {
                         case PLUS:
                             System.out.print('+');
                             break;    
                         case MINUS:
                             System.out.print('-');
                             break;
                         case TIMES:
                             System.out.print('*');
                             break;
                         case OVER:
                             System.out.print('/');
                             break;
                     }         
                 } else if(a.exp == ExpKind.ConstK) {
                     System.out.print(a.valor);
                 }
 	}
     }
    
    public ArrayList<String> generarPila(NodoArbol a) {
 	if(a != null) {
 		generarPila(a.hijos[0]);
 		generarPila(a.hijos[1]);
 		if(a.nombre != null) {
                     tokensPila.add(a.nombre);
                 } else if(a.op != null) {
                     switch(a.op) {
                         case PLUS:
                             tokensPila.add("+");
                             break;    
                         case MINUS:
                             tokensPila.add("-");
                             break;
                         case TIMES:
                             tokensPila.add("*");
                             break;
                         case OVER:
                             tokensPila.add("/");
                             break;
                     }         
                 } else if(a.exp == ExpKind.ConstK) {
               tokensPila.add(a.valor + "");
                 }
 	}
         return tokensPila;
     }
    
 public static void recorrerArbol(NodoArbol a) {
     
     if(a != null) {
             if(a.nombre != null) {
               System.out.print(a.nombre);  
             } else if(a.op != null) {
                 switch(a.op) {
                     case PLUS:
                         System.out.print('+');
                         break;    
                     case MINUS:
                         System.out.print('-');
                         break;
                     case TIMES:
                         System.out.print('*');
                         break;
                     case OVER:
                         System.out.print('/');
                         break;
                 }         
             } else if(a.exp == ExpKind.ConstK) {
                 System.out.print(a.valor);
             } 
 		postorden(a.hijos[0]);
 		//preorden(a.hijos[1]);
 	}
 }
 
    public static void printToken(TokenType token, String tokenString) {
        switch(token) {
            case IF:
            case THEN:
            case ELSE:
            case END:
            case REPETIR:
            case HASTA:
            case LEER:
            case WRITE:
            case WHILE:
            case INFINITO:
            case POW:
            case INC:
            case DEC:
            case SENO:
            case COSENO:
            case ABS:
            case TAN:
            case LN:
            case CLEAR:
            case SALIR:
            case FACTORIAL:
            case RAIZ:
            case HEX:
            case BINARIO:
            case ENTERO:
            case DECIMAL:
                System.out.println("palabra reservada: " + tokenString);
                break;
            case ASSIGN:
                System.out.println(":=");
                break;
            case LT:
                System.out.println("<");
                break;
            case GT:
                System.out.println(">");
                break;
            case EQ:
                System.out.println("==");
                break;
            case LPARENT:
                System.out.println("(");
                break;
            case RPARENT:
                System.out.println(")");
                break;
            case SEMI:
                System.out.println(";");
                break;
            case PLUS:
                System.out.println("+");
                break;
            case MINUS:
                System.out.println("-");
                break;
            case TIMES:
                System.out.println("*");
                break;
            case OVER:
                System.out.println("/");
                break;
            case NUM:
                System.out.println("Num, valor=" + tokenString);
                break;
            case ID:
                System.out.println("Nombre=" + tokenString);
                break;
            case CADENA:
                System.out.println("cadena=" + tokenString);
                break;
            case ERROR:
                System.out.println("Error: " + tokenString);
                break;
            default:
                System.out.println("Desconocido = " + token);
                break;
        }
    } 
    
    public static void printTokenArchivo(TokenType token, String tokenString) {
        switch(token) {
            case IF:
            case THEN:
            case ELSE:
            case END:
            case REPETIR:
            case HASTA:
            case LEER:
            case WRITE:
            case WHILE:
            case INFINITO:
            case POW:
            case INC:
            case DEC:
            case SENO:
            case COSENO:
            case ABS:
            case TAN:
            case LN:
            case CLEAR:
            case SALIR:
            case FACTORIAL:
            case RAIZ:
            case HEX:
            case BINARIO:
            case ENTERO:
            case DECIMAL:
                arbolString += "palabra reservada: " + tokenString + "\n";
                break;
            case ASSIGN:
                arbolString += ":=\n";
                break;
            case LT:
                arbolString += "<\n";
                break;
            case GT:
                arbolString += ">\n";
                break;
            case EQ:
                arbolString += "==\n";
                break;
            case LPARENT:
                arbolString += "(\n";
                break;
            case RPARENT:
                arbolString += ")\n";
                break;
            case SEMI:
                arbolString += ";\n";
                break;
            case PLUS:
                arbolString += "+\n";
                break;
            case MINUS:
                arbolString += "-\n";
                break;
            case TIMES:
                arbolString += "*\n";
                break;
            case OVER:
                arbolString += "/\n";
                break;
            case NUM:
                arbolString += "Num, valor=" + tokenString + "\n";
                break;
            case ID:
                arbolString += "Nombre=" + tokenString + "\n";
                break;
            case CADENA:
                arbolString += "cadena=" + tokenString + "\n";
                break;
            case ERROR:
                arbolString += "Error: " + tokenString + "\n";
                break;
            default:
                arbolString += "Desconocido = " + token + "\n";
                break;
        }
    } 
    
    public NodoArbol newStmtNode(StmtKind kind) {
        NodoArbol t = new NodoArbol();
        if(t == null) {
            System.out.println("Error, no hay memoria");
            // Código para salir del método o el programa
        } else {
            for(int i = 0; i < MAXCHILDREN; i++)
                t.hijos[i] = null;
            t.hermano = null;
            
            t.nodeKind = NodeKind.StmtK;
            t.stmt = kind;
            t.lineno = lineno;
        }
        
        return t;
    }
    
    public NodoArbol newExpNode(ExpKind kind) {
        NodoArbol t = new NodoArbol();
        if(t == null) {
            System.out.println("Error, no hay memoria");
            // Código para salir del método o el programa
        } else {
            for(int i = 0; i < MAXCHILDREN; i++)
                t.hijos[i] = null;
            
            t.hermano = null;
            t.nodeKind = NodeKind.ExpK;
            t.exp = kind;
            t.lineno = lineno;
            t.type = ExpType.Void;
            
        }
        return t;
    }
    
    public static void syntaxError(String mensaje) {
        System.out.printf("Error de sintaxis en la línea: %d: %s", lineno, mensaje);
        
        // Código para salir.
    }
    
    public void coincidir(TokenType expected) {
        if(token == expected) {        
            token = getToken();
        } else {
            syntaxError("Nodo no esperado --> ");
            errorString += "Nodo no esperado --> " + tokenString;
            printToken(expected, tokenString);
            System.out.print("      ");
        }
    }
    
    NodoArbol stmt_sequence() {
        
        NodoArbol t = statement();
        NodoArbol p = t;
        
        while((token != TokenType.FINALIZAR) && (token != TokenType.FINBLOQUE) && (token != TokenType.END) && (token != TokenType.ELSE) && (token != TokenType.HASTA) && (indice < this.palabras.size())) {
            
            NodoArbol q;
            coincidir(TokenType.SEMI);
            q = statement();
            if(q != null) {
                if(t == null)
                    t = p = q;
                else {       /* Ahora p ya no puede ser NULL */
                    p.hermano = q;
                    p = q;
                }
            }
        }
        
        return t;
        
    }
    
    public NodoArbol statement() {
        NodoArbol t = null;
        switch(token) {    
            case IF:
                t = if_stmt();
                break;
            case REPETIR:
                t = repeat_stmt();
                break;
            case ID:
                t = assign_stmt();
                break;
            case BINARIO:
                 t = assign_binary_stmt();
                break;
            case ENTERO:
                t = assign_entero_stmt();
                break;
            case DECIMAL:
                t = assign_decimal_stmt();
                break;
            case LEER:
                t = read_stmt();
                break;
            case WRITE:
                t = write_stmt();
                break;
            case POW:
                t = pow_stmt();
                break;
            case INC:
                t = inc_stmt();
                break;
            case DEC:
                t = dec_stmt();
                break;
            case SENO:
                t = seno_stmt();
                break;
            case COSENO:
                t = coseno_stmt();
                break;
            case ABS:
                t = abs_stmt();
                break;
            case TAN:
                t = tan_stmt();
                break;
            case LN:
                t = ln_stmt();
                break;
            case CLEAR:
                clear_stmt();
                break;
            case SALIR:
                t = salir_stmt();
                break;
            case FACTORIAL:
                t = factorial_stmt();
                break;
            case RAIZ:
                t = raiz_stmt();
                break;
            case HEX:
                t = hex_stmt();
                break;
            case WHILE:
                t = while_stmt();
                break;
            case INFINITO:
                t = infinito_stmt();
                break;
            default:
                syntaxError("Token inesperado --> ");
                errorString += "Token inesperado --> " + tokenString; 
                printToken(token, tokenString);
                token = getToken();
                break;
        }
        return t;
    }
    
    NodoArbol if_stmt() {
        NodoArbol t = newStmtNode(StmtKind.IfK);
        coincidir(TokenType.IF);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.THEN);
        if(t != null)
            t.hijos[1] = stmt_sequence();
        if(token == TokenType.ELSE) {
            coincidir(TokenType.ELSE);
            if(t != null)
                t.hijos[2] = stmt_sequence();
        }
        
        coincidir(TokenType.END);
        return t;
        
    }
    
    public NodoArbol repeat_stmt() {
        NodoArbol t = newStmtNode(StmtKind.RepeatK);
        coincidir(TokenType.REPETIR);
        if(t != null)
            t.hijos[0] = stmt_sequence();
        coincidir(TokenType.HASTA);
        if(t != null)
            t.hijos[1] = expresion();
        
        return t;
        
    }
    
    public NodoArbol while_stmt() {
        NodoArbol t = newStmtNode(StmtKind.WhileK);
        coincidir(TokenType.WHILE);
        
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.THEN);
        if(t != null)
            t.hijos[1] = stmt_sequence();
        coincidir(token);
        return t;
    }
    
    public NodoArbol infinito_stmt() {
        
        NodoArbol t = newStmtNode(StmtKind.InfinitumK);
        coincidir(TokenType.INFINITO);
        coincidir(TokenType.THEN);
        if(t != null)
            t.hijos[0] = stmt_sequence();
        coincidir(TokenType.END);
        
        return t;
        
    }
    
    public NodoArbol pow_stmt() {
        NodoArbol t = newStmtNode(StmtKind.PowK);
        coincidir(TokenType.POW);
        coincidir(TokenType.LPARENT);
        coincidir(TokenType.ID);
        coincidir(TokenType.COMA);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol inc_stmt() {
        NodoArbol t = newStmtNode(StmtKind.IncK);
        coincidir(TokenType.INC);
        coincidir(TokenType.LPARENT);
        coincidir(TokenType.ID);
        coincidir(TokenType.COMA);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol dec_stmt() {
        NodoArbol t = newStmtNode(StmtKind.IncK);
        coincidir(TokenType.DEC);
        coincidir(TokenType.LPARENT);
        coincidir(TokenType.ID);
        coincidir(TokenType.COMA);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol seno_stmt() {
        NodoArbol t = newStmtNode(StmtKind.SenoK);
        coincidir(TokenType.SENO);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    public NodoArbol coseno_stmt() {
        NodoArbol t = newStmtNode(StmtKind.CosenoK);
        coincidir(TokenType.COSENO);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    public NodoArbol abs_stmt() {
        NodoArbol t = newStmtNode(StmtKind.AbsK);
        coincidir(TokenType.ABS);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    public NodoArbol tan_stmt() {
        NodoArbol t = newStmtNode(StmtKind.TanK);
        coincidir(TokenType.TAN);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol ln_stmt() {
        NodoArbol t = newStmtNode(StmtKind.LnK);
        coincidir(TokenType.LN);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public void clear_stmt() {
        coincidir(TokenType.CLEAR);
    }
    
    public NodoArbol salir_stmt() {
        NodoArbol t = newStmtNode(StmtKind.SalirK);
        coincidir(TokenType.SALIR);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol factorial_stmt() {
        NodoArbol t = newStmtNode(StmtKind.FactorialK);
        coincidir(TokenType.FACTORIAL);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol raiz_stmt() {
        NodoArbol t = newStmtNode(StmtKind.RaizK);
        coincidir(TokenType.RAIZ);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol hex_stmt() {
        NodoArbol t = newStmtNode(StmtKind.HexK);
        coincidir(TokenType.HEX);
        coincidir(TokenType.LPARENT);
        t.nombre = tokenString;
        coincidir(TokenType.CADENA);
        coincidir(TokenType.RPARENT);
        
        return t;
    }
    
    public NodoArbol assign_stmt() {
        
        NodoArbol t = newStmtNode(StmtKind.AssignK);
        if((t != null) && (token == TokenType.ID))
            t.nombre = tokenString;
        
        if(tabla.tabla.containsKey(tokenString) == false) {
            errorString += "La variable o función " + tokenString + " no existe, línea" + lineno + "\n";
            syntaxError("La variable o función " + tokenString + " no existe, línea " + lineno + "\n");
        } else { // Sí existe y obtenemos su tipo...
            NodoArbol temporal = null;
            temporal = (NodoArbol) tabla.tabla.get(tokenString);
            asignacionTipo = temporal.type;          // Asignamos el tipo global al tipo encontrado en la variable de asignación.
        }
        
        coincidir(TokenType.ID);
        coincidir(TokenType.ASSIGN);
        if(t != null)
            t.hijos[0] = expresion();
        
        return t;
    }
    
    public NodoArbol assign_binary_stmt() {
        NodoArbol t = newStmtNode(StmtKind.BinarioK);    
        coincidir(TokenType.BINARIO);            // Aquí ya avanzamos hacie el ID...
        if(t != null)
            t.nombre = tokenString;
        
        if(tabla.tabla.containsKey(tokenString) == false) {          // Si no se encuentra en la tabla de símbolos.
            t.type = ExpType.Binario;        // Importante agregar el tipo antes de agregar a la tabla de símbolos...
            tabla.put(tokenString, t);
            
        } else {    
            syntaxError("La variable " + tokenString + " ya se encuentra declarada, línea: " + lineno + "\n");
            errorString += "La variable " + tokenString + " ya se encuentra declarada, línea: " + lineno + "\n";
        }
        
        coincidir(TokenType.ID);
        coincidir(TokenType.ASSIGN);
        if(t != null)
            t.hijos[0] = expresion_binaria();        // Hacer un procedimiento expresion_binaria
        
        tabla.mostrarTabla();
        // Ver el recorrido del árbol........................................................
 
        return t;
    }
    
    public NodoArbol read_stmt() {
        NodoArbol t = newStmtNode(StmtKind.ReadK);
        coincidir(TokenType.LEER);
        coincidir(TokenType.LPARENT);
        if((t != null) && (token == TokenType.ID))
            t.nombre = tokenString;
        coincidir(TokenType.ID);
        coincidir(TokenType.RPARENT);
        return t;
    }
    
    public NodoArbol write_stmt() {
        NodoArbol t = newStmtNode(StmtKind.WriteK);
        coincidir(TokenType.WRITE);
        coincidir(TokenType.LPARENT);
        if(t != null)
            t.hijos[0] = expresion();
        coincidir(TokenType.RPARENT);
        return t;
    }
    
    public NodoArbol expresion() {
        NodoArbol t = simple_expresion();
        if((token == TokenType.GT) || (token == TokenType.LT) || (token == TokenType.EQ)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
            }
            
            coincidir(token);
            if(t != null)
                t.hijos[1] = simple_expresion();
            
        }
        return t;
    }
    
    public NodoArbol simple_expresion() {
        NodoArbol t = termino();
        while((token == TokenType.PLUS) || (token == TokenType.MINUS)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                t.hijos[1] = termino();
            }
        }
        return t;
    }
    
    public NodoArbol termino() {
        NodoArbol t = factor();
        while((token == TokenType.TIMES) || (token == TokenType.OVER)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                p.hijos[1] = factor();
            }
        }
        return t;
    }
    
    public NodoArbol factor() {
        NodoArbol t = null;
        switch(token) {
            case NUM:
                t = newExpNode(ExpKind.ConstK);
                
                if((t != null) && (token == TokenType.NUM)) {
                    // Chequeo de tipos:
                    if((asignacionTipo == ExpType.Binario) || (getTipoDatoString(tokenString) == ExpType.Entero) && isBinary(tokenString)) {
                        t.type = ExpType.Binario;
                        t.valor = bin2int(tokenString);
                        // Enteros
                    } else if((asignacionTipo == ExpType.Entero) && getTipoDatoString(tokenString) == ExpType.Entero && isEntero(tokenString) == true) {
                        t.type = ExpType.Entero;
                        t.valor = Integer.parseInt(tokenString);
                    } else if((asignacionTipo == ExpType.Decimal) && getTipoDatoString(tokenString) == ExpType.Decimal || getTipoDatoString(tokenString) == ExpType.Decimal) {
                        // Código para decimales.
                        t.type = ExpType.Decimal;
                        t.valorDecimal = Double.parseDouble(tokenString);
                    } else {
                        errorString += "Los tipos de datos no coinciden (" + tokenString + "), deben ser " + asignacionTipo + ", línea " + lineno + "\n";
                        syntaxError("Los tipos de datos no coinciden, deben ser " + asignacionTipo + ", línea " + lineno + "\n");
                        if((asignacionTipo == ExpType.Entero) && isFloat(tokenString)) {
                            t.type = ExpType.Entero;
                            t.valor = (int)Double.parseDouble(tokenString);
                        }
                    }
                }
                    
                coincidir(TokenType.NUM);
                break;
            case ID:
                
                t = newExpNode(ExpKind.IdK);
                if((t != null) && (token == TokenType.ID))
                    t.nombre = tokenString;
                
                if(tabla.tabla.containsKey(tokenString) == false) {
                    errorString += "Variable o función no encontrada: " + tokenString + " ,línea: " + lineno + "\n";
                    syntaxError("Variable o función no encontrada: " + tokenString + " ,línea: " + lineno);
                } else {         // Si existe...
                    NodoArbol temporal = null;
                    temporal = (NodoArbol) tabla.tabla.get(tokenString);
                    if(temporal.type != asignacionTipo) {
                        errorString += "Error de tipos con " + tokenString + " , línea " + lineno + "\n";
                        syntaxError("Error de tipos con " + tokenString + " , línea " + lineno + "\n");
                    }
                }
                
                coincidir(TokenType.ID);
                break;
            case LPARENT:
                coincidir(TokenType.LPARENT);
                t = expresion();
                coincidir(TokenType.RPARENT);
                break;
            default:
                syntaxError("Token inesperado ---> ");
                errorString += "Token inesperado ---> " + tokenString;
                printToken(token, tokenString);
                token = getToken();
                break;
        }
        return t;
    }
    
    /////////////// Código de prueba /////////////////////////////////////////////////////////////////
       public NodoArbol expresion_binaria() {
        NodoArbol t = simple_expresion_binaria();
        if((token == TokenType.GT) || (token == TokenType.LT) || (token == TokenType.EQ)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
            }
            
            coincidir(token);
            if(t != null)
                t.hijos[1] = simple_expresion_binaria();
            
        }
        return t;
    }
    
    public NodoArbol simple_expresion_binaria() {
        NodoArbol t = termino_binario();
        while((token == TokenType.PLUS) || (token == TokenType.MINUS)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                t.hijos[1] = termino_binario();
            }
        }
        return t;
    }
    
    public NodoArbol termino_binario() {
        NodoArbol t = factor_binario();
        while((token == TokenType.TIMES) || (token == TokenType.OVER)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                p.hijos[1] = factor_binario();
            }
        }
        return t;
    }
    
    public NodoArbol factor_binario() {
        NodoArbol t = null;
        switch(token) {
            case NUM:
                t = newExpNode(ExpKind.ConstK);
                if((t != null) && (token == TokenType.NUM)) {
                    if(isBinary(tokenString) == false) {
                        errorString += "Debe especificar un número binario válido, línea: " + lineno + "\n";
                        syntaxError("Debe especificar un número binario válido\n");  
                    } else {
                        t.type = ExpType.Binario;
                                  // Sino convertimos
                        t.valor = bin2int(tokenString); // Hacer la comprobación de tipos.
                    }
                }
                
                coincidir(TokenType.NUM);
                break;
            case ID:
                NodoArbol temporal = null;
                temporal = (NodoArbol) tabla.tabla.get(tokenString);
                if(temporal == null) {
                    errorString += "Variable o función no encontrada: " + tokenString + " ,línea: " + lineno + "\n";
                    syntaxError("Variable o función no encontrada: " + tokenString + " ,línea: " + lineno);
                } else {
                    // Ya que si se encuentra en la tabla de símbolos, checar que sea de tipo binario, sino lanzar error.
                    if(temporal.type != ExpType.Binario) {
                        errorString += "La variable " + tokenString + " no es de tipo Binario, corrija ese error, línea " + lineno + "\n";
                        syntaxError("La variable " + tokenString + " no es de tipo Binario, corrija ese error, línea " + lineno);
                    }
                }
                
                t = newExpNode(ExpKind.IdK);
                if((t != null) && (token == TokenType.ID)) {
                    t.nombre = tokenString;
                }
                
                coincidir(TokenType.ID);
                break;
            case LPARENT:            /* Expresión entre paréntesis */
                coincidir(TokenType.LPARENT);
                t = expresion_binaria();
                coincidir(TokenType.RPARENT);
                break;
            default:
                syntaxError("Token inesperado ---> ");
                errorString += "Token inesperado: " + tokenString + "\n";
                printToken(token, tokenString);
                token = getToken();
                break;
        }
        return t;
    }
    //////////////////////// Fin código de prueba ///////////////////////////////////////////////
    /////////////////////// Código de prueba para enteros ///////////////////////////////////////
    public NodoArbol assign_entero_stmt() {
    
        NodoArbol t = newStmtNode(StmtKind.EnteroK);
        
        coincidir(TokenType.ENTERO);
        if(t != null)
            t.nombre = tokenString;
        
        if(tabla.tabla.containsKey(tokenString) == false) {          // Si no se encuentra en la tabla de símbolos.
            t.type = ExpType.Entero;        // Importante agregar el tipo antes de agregar a la tabla de símbolos...
            tabla.put(tokenString, t);
            
        } else {    
            errorString += "La variable " + tokenString + " ya se encuentra declarada, línea: " + lineno + "\n";
        }
        // tokenString hasta este punto devuelve el ID, checar si está en la tabla de simbolos...
        // Es una asignación, entonces debemos asegurarnos que la variable no esté en la tabla de símbolos...
        coincidir(TokenType.ID);
        coincidir(TokenType.ASSIGN);
        if(t != null)
            t.hijos[0] = expresion_entera();        // Hacer un procedimiento expresion_entera    
        System.out.println();
        System.out.println("---------------------------------------------------------------");
        
        // PENDIENTE Automatizar esto, no borrarlo!
        generarPila(t);
        
        Generador generador = new Generador(tokensPila, programName);
        // PENDIENTE Juntar los lexemas relaciones con la asignación de enteros y crear un método
        generador.generar();
        generador.generarCodigoP();
        
        postorden(t);
        
        System.out.println("---------------------------------------------------------------");
        tabla.mostrarTabla();
        return t;
    }
    
    
     /////////////// Código de prueba /////////////////////////////////////////////////////////////////
       public NodoArbol expresion_entera() {
        NodoArbol t = simple_expresion_entera();
        if((token == TokenType.GT) || (token == TokenType.LT) || (token == TokenType.EQ)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
            }
            
            coincidir(token);
            if(t != null)
                t.hijos[1] = simple_expresion_entera();
            
        }
        return t;
    }
    
    public NodoArbol simple_expresion_entera() {
        NodoArbol t = termino_entero();
        while((token == TokenType.PLUS) || (token == TokenType.MINUS)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                t.hijos[1] = termino_entero();
            }
        }
        return t;
    }
    
    public NodoArbol termino_entero() {
        NodoArbol t = factor_entero();
        while((token == TokenType.TIMES) || (token == TokenType.OVER)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                p.hijos[1] = factor_entero();
            }
        }
        return t;
    }
    
    public NodoArbol factor_entero() {
        NodoArbol t = null;
        switch(token) {
            case NUM:
                t = newExpNode(ExpKind.ConstK);
                if((t != null) && (token == TokenType.NUM)) {
                    if(isEntero(tokenString) == false) { // Checar si es un número entero.
                        if(isFloat(tokenString) == true) {           // No es entero, pero sí es flotante, convertimos y asignamos.
                            JOptionPane.showMessageDialog(null, "Convirtiendo: " + tokenString + " a entero");
                            t.type = ExpType.Entero;
                            t.valor = (int)Float.parseFloat(tokenString);
                        } else {             // Entonces es binario ...
                           // PENDIENTE Corregir aquí el problema de los tipos con 0 y 1.
                           /*JOptionPane.showMessageDialog(null, tokenString.length());
                            errorString += "Error de tipos, binario encontrado, línea " + lineno + "\n";
                           syntaxError("Error de tipos, binario encontrado, línea " + lineno + "\n");*/
                           if(!tokenString.equals("0") && tokenString.length() != 1) {
                               errorString += "Error de tipos, binario encontrado, línea " + lineno + "\n";
                                syntaxError("Error de tipos, binario encontrado, línea " + lineno + "\n");
                           }
                           
                        }
                    } else {
                                     // Sino convertimos a entero y almacenamos en el miembro "valor".
                        t.type = ExpType.Entero;
                        t.valor = Integer.parseInt(tokenString); // Hacer la comprobación de tipos.
                    }
                }
                
                coincidir(TokenType.NUM);
                break;
            case ID:
                t = newExpNode(ExpKind.IdK);
                NodoArbol temporal = null;
                temporal = (NodoArbol) tabla.tabla.get(tokenString);
                if(temporal == null) {
                    errorString += "Variable o función no encontrada: " + tokenString + " ,línea: " + lineno + "\n";
                    syntaxError("Variable o función no encontrada: " + tokenString + " ,línea: " + lineno + "\n");
                } else {
                    // Chequeo de tipos luego que sí está en la tabla de símbolos.
                    if(temporal.type != ExpType.Entero) {
                        
                        if(temporal.type == ExpType.Decimal) {
                            JOptionPane.showMessageDialog(null, "Variable de tipo decimal " + tokenString + " encontrada, convirtiendo a entero, línea " + lineno);;
                            t.type = ExpType.Entero;
                            t.valor = (int)temporal.valorDecimal;    // Convertimos a entero...
                        } else {
                            errorString += "Tipos incompatibles entero y binario, línea " + lineno + "\n";
                            syntaxError("Tipos incompatibles entero y binario, línea " + lineno + "\n");
                        }
                        
                    } else {     // Es Entero.
                        t = newExpNode(ExpKind.IdK);
                        t.type = ExpType.Entero;
                        t.valor = temporal.valor;
                    }
                }
                
                if((t != null) && (token == TokenType.ID)) {
                    t.nombre = tokenString;
                }
                
                coincidir(TokenType.ID);
                break;
            case LPARENT:            /* Expresión entre paréntesis */
                coincidir(TokenType.LPARENT);
                t = expresion_entera();
                coincidir(TokenType.RPARENT);
                break;
            default:
                syntaxError("Token inesperado ---> ");
                errorString += "Token inesperado: " + tokenString + "\n";
                printToken(token, tokenString);
                token = getToken();
                break;
        }
        return t;
    }
    /////////////////// Fin código de enteros ///////////////////////////////////
    ////////////////// Inicio de código de decimales ////////////////////////////
    public NodoArbol assign_decimal_stmt() {
    
        NodoArbol t = newStmtNode(StmtKind.DecimalK);
        
        coincidir(TokenType.DECIMAL);
        if(t != null)
            t.nombre = tokenString;
        
        if(tabla.tabla.containsKey(tokenString) == false) {          // Si no se encuentra en la tabla de símbolos.
            t.type = ExpType.Decimal;        // Importante agregar el tipo antes de agregar a la tabla de símbolos...
            tabla.put(tokenString, t);       // Agregar a la tabla de símbolos
            
        } else {    
            errorString += "La variable " + tokenString + " ya se encuentra declarada, línea: " + lineno + "\n";
            syntaxError("La variable " + tokenString + " ya se encuentra declarada, línea: " + lineno + "\n");
        }
        // tokenString hasta este punto devuelve el ID, checar si está en la tabla de simbolos...
        // Es una asignación, entonces debemos asegurarnos que la variable no esté en la tabla de símbolos...
        coincidir(TokenType.ID);
        coincidir(TokenType.ASSIGN);
        if(t != null)
            t.hijos[0] = expresion_decimal();        // Hacer un procedimiento expresion_entera
        tabla.mostrarTabla();
        return t;
    }
     
    /////////////// Código de prueba /////////////////////////////////////////////////////////////////
       public NodoArbol expresion_decimal() {
        NodoArbol t = simple_expresion_decimal();
        if((token == TokenType.GT) || (token == TokenType.LT) || (token == TokenType.EQ)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
            }
            
            coincidir(token);
            if(t != null)
                t.hijos[1] = simple_expresion_decimal();
            
        }
        return t;
    }
    
    public NodoArbol simple_expresion_decimal() {
        NodoArbol t = termino_decimal();
        while((token == TokenType.PLUS) || (token == TokenType.MINUS)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                t.hijos[1] = termino_decimal();
            }
        }
        return t;
    }
    
    public NodoArbol termino_decimal() {
        NodoArbol t = factor_decimal();
        while((token == TokenType.TIMES) || (token == TokenType.OVER)) {
            NodoArbol p = newExpNode(ExpKind.OpK);
            if(p != null) {
                p.hijos[0] = t;
                p.op = token;
                t = p;
                coincidir(token);
                p.hijos[1] = factor_decimal();
            }
        }
        return t;
    }
    
    public NodoArbol factor_decimal() {
        NodoArbol t = null;
        switch(token) {
            case NUM:
                t = newExpNode(ExpKind.ConstK);
                if((t != null) && (token == TokenType.NUM)) {
                    if(isFloat(tokenString) == false) { 
                        if(isEntero(tokenString) == true) {
                            JOptionPane.showMessageDialog(null, "Número no decimal detectado, convirtiendo a decimal: " + tokenString + ", linea " + lineno);
                            t.type = ExpType.Decimal;
                            t.valorDecimal = Double.parseDouble(tokenString);
                        } else {
                            // Un binario encontrado.
                            errorString += "Error, tipos de datos incompatibles, " + tokenString + ", línea " + lineno + "\n";
                            syntaxError("Debe especificar un número flotante válido\n");  
                        }
                    } else {
                        // Sino convertimos a double y almacenamos en el miembro "valor" y "valorDecimal"
                        t.type = ExpType.Decimal;
                        t.valorDecimal = Double.parseDouble(tokenString);
                    }
                }
                
                coincidir(TokenType.NUM);
                break;
            case ID:
 
                t = newExpNode(ExpKind.IdK);
                NodoArbol temporal = null;
                temporal = (NodoArbol) tabla.tabla.get(tokenString);
                if(temporal == null) {
                    errorString += "Variable o función no encontrada: " + tokenString + " ,línea: " + lineno + "\n";
                    syntaxError("Variable o función no encontrada: " + tokenString + " ,línea: " + lineno + "\n");
                } else {
                    // Chequeo de tipos luego que sí está en la tabla de símbolos.
                    if(temporal.type != ExpType.Decimal) {
                        
                        if(temporal.type == ExpType.Entero) {
                             JOptionPane.showMessageDialog(null, "Entero detectado:" + tokenString + " convirtiendo, línea " + lineno);
                            t.valorDecimal = (double)temporal.valor;    // Convertimos el valor entero a float.
                        } else {
                            errorString += "Tipos incompatibles decimal y binario, línea " + lineno + "\n";
                            syntaxError("Tipos incompatibles decimal y binario, línea " + lineno + "\n");
                        }
                        
                    } else {     // Es Entero.
                        t = newExpNode(ExpKind.IdK);
                        t.type = ExpType.Decimal;
                        t.valorDecimal = (double)temporal.valor;
                    }
                }
                
                //t = newExpNode(ExpKind.IdK);
                if((t != null) && (token == TokenType.ID)) {     
                    t.nombre = tokenString;
                }
               
                coincidir(TokenType.ID);
                break;
            case LPARENT:            /* Expresión entre paréntesis */
                coincidir(TokenType.LPARENT);
                t = expresion_entera();
                coincidir(TokenType.RPARENT);
                break;
            default:
                syntaxError("Token inesperado ---> ");
                errorString += "Token inesperado: " + tokenString + "\n";
                printToken(token, tokenString);
                token = getToken();
                break;
        }
        return t;
    }
    ////////////////////////////// Fin Código de flotantes //////////////////////
    
    public TokenType getToken() {
        
        tokenString = palabras.get(indice).getValor();
        token = palabras.get(indice).getTipoToken();
        lineno = palabras.get(indice).getLineNo();
        
        return palabras.get(indice++).getTipoToken();
        
    }
    
    public NodoArbol parse() {
        
        NodoArbol t = null;
        
        token = getToken();
        coincidir(TokenType.INICIO);
        System.out.println("Programa: " + tokenString);
        
        NodoArbol programName = newStmtNode(StmtKind.ProgramK);
        programName.nombre = tokenString;
        tabla.put(tokenString, programName);
        
        coincidir(TokenType.ID);
        
        t = stmt_sequence();
        coincidir(TokenType.FINALIZAR);       
        return t;
    }
 
 }
