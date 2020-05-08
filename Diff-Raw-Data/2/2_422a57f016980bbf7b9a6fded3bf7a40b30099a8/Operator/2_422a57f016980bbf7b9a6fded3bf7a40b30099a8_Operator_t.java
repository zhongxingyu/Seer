 package de.skuzzle.polly.core.parser.ast.lang;
 
 
 import de.skuzzle.polly.core.parser.Token;
 import de.skuzzle.polly.core.parser.TokenType;
 import de.skuzzle.polly.core.parser.ast.declarations.Declaration;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 
 /**
  * Superclass for all operators. Operators are represented as a normal function call and
  * must thus exist as an expression that can be executed when that call is resolved.
  * 
  * <p>Subclasses should only be used in declarations, but not as Node in the AST.</p> 
  * 
  * @author Simon Taddiken
  */
 public abstract class Operator extends Function {
     
     /**
      * All possible operator types and their string representation.
      * 
      * @author Simon Taddiken
      */
     public static enum OpType {
         // casting
         STRING(Type.STRING.getName().getId()),
         NUMBER(Type.NUM.getName().getId()),
         DATE(Type.DATE.getName().getId()),
         TIMESPAN(Type.TIMESPAN.getName().getId()),
         CHANNEL(Type.CHANNEL.getName().getId()), 
         USER(Type.USER.getName().getId()),
         
         // math functions
         MIN("min"),
         MAX("max"),
         LOG("log"),
         LN("ln"),
         SQRT("sqrt"),
         ROUND("round"),
         CEIL("ceil"),
         FLOOR("floor"),
         SIG("sig"),
         TAN("tan"),
         COS("cos"),
         SIN("sin"),
         ASIN("asin"),
         ATAN("atan"),
         ATAN2("atan2"),
         HYPOT("hypot"),
         ACOS("acos"),
         ABS("abs"),
         TO_DEGREES("toDegrees"),
         TO_RADIANS("toRadians"),
         EXP("exp"),
         
         // further functions
         FOLD_LEFT("foldl"),
         MAP("map"),
         ID("id"),
         COMP("comp"),
         SORT("sort"),
         DAY("day"),
        TRANSPOSE("^T"),
         
         ADD("+"),
         ADDWAVE("+~"),
         SUB("-"),
         MUL("*"),
         DIV("/"), 
         BOOLEAN_AND("&&"), 
         BOOLEAN_OR("||"), 
         XOR("^^"),
         DOLLAR("$"), 
         DOTDOT(".."), 
         EGT(">="), 
         ELT("<="), 
         EQ("=="), 
         EXCLAMATION("!"), 
         GT(">"), 
         INDEX("[]"), 
         INTDIV("\\"), 
         INT_AND("&"), 
         INT_OR("|"), 
         LEFT_SHIFT("<<"), 
         LT("<"), 
         MOD("%"), 
         NEQ("!="), 
         POWER("^"), 
         QUESTION("?"), 
         QUEST_EXCL("?!"), 
         RADIX("0x:"), 
         RIGHT_SHIFT(">>"), 
         URIGHT_SHIFT(">>>"), 
         WAVE("~"), 
         IF("if"); 
         
         private final String id;
         
         
         private OpType(String id) {
             this.id = id;
         }
         
         
         
         public String getId() {
             return this.id;
         }
         
         
         
         /**
          * Converts from {@link TokenType} to Operator types.
          * @param token Token to convert into operator type.
          * @return The operator type.
          */
         public static OpType fromToken(Token token) {
             switch (token.getType()) {
             case ADD:         return OpType.ADD;
             case ADDWAVE:     return OpType.ADDWAVE;
             case BOOLEAN_AND: return OpType.BOOLEAN_AND;
             case BOOLEAN_OR:  return OpType.BOOLEAN_OR;
             case XOR:         return OpType.XOR;
             case DIV:         return OpType.DIV;
             case DOLLAR:      return OpType.DOLLAR;
             case DOTDOT:      return OpType.DOTDOT;
             case EGT:         return OpType.EGT;
             case ELT:         return OpType.ELT;
             case EQ:          return OpType.EQ;
             case EXCLAMATION: return OpType.EXCLAMATION;
             case GT:          return OpType.GT;
             case INDEX:       return OpType.INDEX;
             case INTDIV:      return OpType.INTDIV;
             case INT_AND:     return OpType.INT_AND;
             case INT_OR:      return OpType.INT_OR;
             case LEFT_SHIFT:  return OpType.LEFT_SHIFT;
             case LT:          return OpType.LT;
             case MOD:         return OpType.MOD;
             case MUL:         return OpType.MUL;
             case NEQ:         return OpType.NEQ;
             case POWER:       return OpType.POWER;
             case QUESTION:    return OpType.QUESTION;
             case QUEST_EXCALAMTION: return OpType.QUEST_EXCL;
             case RADIX:       return OpType.RADIX;
             case RIGHT_SHIFT: return OpType.RIGHT_SHIFT;
             case SUB:         return OpType.SUB;
             case URIGHT_SHIFT:return OpType.URIGHT_SHIFT;
             case WAVE:        return OpType.WAVE;
             case OPENSQBR:    return OpType.INDEX;
             case IF:          return OpType.IF;
             case TRANSPOSE:   return OpType.TRANSPOSE;
             default:
                 throw new IllegalArgumentException("not a valid operator token: " + 
                     token);
             
             }
         }
     }
 
     
     
     private final OpType op;
     
     
     
     /**
      * Creates a new operator.
      * 
      * @param op The operator type
      */
     public Operator(OpType op) {
         super(op.getId());
         this.op = op;
     }
     
     
 
     /**
      * Gets the identifier that represents this operator.
      * 
      * @return The identifier.
      */
     public OpType getOp() {
         return this.op;
     }
 
     
     
     /**
      * Creates a proper declaration for this operator.
      * 
      * @return A declaration.
      */
     public Declaration createDeclaration() {
         // HACK: save type, because it will be changed by call to super.createDeclaration
         final Type t = this.getUnique(); 
         final Declaration vd = super.createDeclaration();
         
         // restore saved type
         this.setUnique(t);
         return vd;
     }
     
     
     
     /**
      * Throws a RuntimeException indicating that a operator was declared and called with
      * a wrong {@link OpType}.
      *  
      * @param op The invalied operator type.
      */
     protected void invalidOperatorType(OpType op) {
         throw new RuntimeException("This should not have happened. " +
             "Operator call with invalid operator type: " + op);
     }
 }
