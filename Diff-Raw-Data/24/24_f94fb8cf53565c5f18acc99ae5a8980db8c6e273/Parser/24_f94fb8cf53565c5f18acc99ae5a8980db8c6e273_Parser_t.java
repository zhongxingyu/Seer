 package mpp;
 
 import ast.Number;
 import ast.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Parser {
 
     int i = 0;
 
     public Parser() {
     }
 
     public static boolean isNumeric(String str) {
         return str.matches("-?\\d+(.\\d+)?");
     }
     private Pattern ALPHANUMERIC = Pattern.compile("[A-Za-z0-9]+");
 
     private boolean isAlphaNumeric(String s) {
         if (s == null) {
             return false;
         } else {
             Matcher m = ALPHANUMERIC.matcher(s);
             return m.matches();
         }
     }
 
     public void reset() {
         i = 0;
     }
 
     public Program parseProgram(List<List<String>> tokens) throws Exception {
         List<Stmt> stmts = new ArrayList<Stmt>();
         for (List<String> list : tokens) {
 
             Stmt s = parseStmt(list);
             //System.out.println(s.toString());
             stmts.add(s);
 
             reset();
         }
 
         return new Program(stmts);
     }
 
     public Stmt parseStmt(List<String> tokens) throws Exception {
         Stmt stmt = null;
 
         for (; i < tokens.size(); i++) {
             String token = tokens.get(i);
             if ("print".equals(token)) {
                 FunctionCall functionCall = new FunctionCall();
                 functionCall.id = tokens.get(i);
                 List<Expr> l = new ArrayList<Expr>();
                 i++;
                 l.add(parseExpr(tokens));
                 functionCall.args = l;
                 stmt = functionCall;
             } else if ("var".equals(token)) {
                 i++;
                 if ((i + 1) < tokens.size() && "=".equals(tokens.get(i + 1))) {
                     
                     DeclareAssignVar var = new DeclareAssignVar();
                     var.id = tokens.get(i);
                     i += 2;
                     var.expr = parseExpr(tokens);
                     stmt = var;
                 } else {
                     DelcareVar declareVar = new DelcareVar();
                     declareVar.id = tokens.get(i);
                     stmt = declareVar;
                 }
                 i++;
             } else {
                 Assign assign = new Assign();
                 assign.id = token;
                 i += 2;
                 assign.expr = parseExpr(tokens);
                 stmt = assign;
             }
         }
 
         return stmt;
     }
 
     public Expr parseExpr(List<String> tokens) throws Exception {
         Expr expr = null;
 
         for (; i < tokens.size(); i++) {
             if (isNumeric(tokens.get(i))) {
                 expr = new Number(java.lang.Integer.parseInt(tokens.get(i)));
             } else if ("\"".equals(tokens.get(i))) {
                 i++;
 
                 expr = new StringLiteral(tokens.get(i));
 
 
             } else if (isAlphaNumeric(tokens.get(i))) {
                 expr = new GetVar(tokens.get(i));
                
             } else {
                 if ("+".equals(tokens.get(i))) {
                     i++;
                     expr = new Op(expr, Operators.plus, parseExpr(tokens));
 
                 } else if ("-".equals(tokens.get(i))) {
                     i++;
                     expr = new Op(expr, Operators.minus, parseExpr(tokens));
 
                 } else if ("*".equals(tokens.get(i))) {
                     i++;
                    List l = tokens.subList(i, i+1);
                    
                    int buf = i;
                    i = 0;
                    Expr ex = parseExpr(l);
                    i = buf;
                    
                    
                    
                    expr = new Op(expr, Operators.miltiply, ex);
                    
                    
 
                 } else if ("/".equals(tokens.get(i))) {
                     i++;
                    List l = tokens.subList(i, i+1);
                    
                    int buf = i;
                    i = 0;
                    Expr ex = parseExpr(l);
                    i = buf;
                    expr = new Op(expr, Operators.divide, ex);
 
                 } else {
                     throw new Exception("no match for " + tokens.get(i));
                 }
             }
         }
 
 
 
         return expr;
     }
 }
