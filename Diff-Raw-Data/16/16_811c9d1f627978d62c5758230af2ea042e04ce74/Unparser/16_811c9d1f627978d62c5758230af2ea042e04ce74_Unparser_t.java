 package org.libj.xquery.parser;
 
 import org.libj.xquery.lexer.TokenType;
 import org.libj.xquery.lisp.Cons;
 
 public class Unparser {
     private int indent = 0;
     private StringBuilder builder = new StringBuilder();
 
     private void output(Object x) {
         output((Cons) x);
     }
 
     private void output(Cons ast) {
         if (ast == null) {
             return;
         }
         TokenType t = (TokenType) ast.first();
         switch (t) {
             case PROG:
                 output((Cons) ast.third());
                 break;
             case FLOWER:
                 outputFlower(ast);
                 break;
             case IF:
                 builder.append("if (");
                 output(ast.second());
                 builder.append(") then ");
                 output(ast.third());
                 builder.append(" else ");
                 output(ast.nth(3));
                 break;
             case CALL:
                 outputCall(ast);
                 break;
             case VARIABLE:
                 builder.append(ast.second());
                 break;
             case NUMBER:
                 builder.append(ast.second());
                 break;
             case STRING:
                 builder.append('"');
                 builder.append(((String)ast.second()).replace("\\", "\\\\").replace("\"", "\\\""));
                 builder.append('"');
                 break;
             case XPATH:
                 output(ast.second());
                 builder.append('/');
                 builder.append(ast.third());
                 break;
             case ATTR_AT:
                 output(ast.second());
                 builder.append("/@");
                 builder.append(ast.third());
                 break;
             case INDEX:
                 output(ast.second());
                 builder.append('[');
                 output(ast.third());
                 builder.append(']');
                 break;
             case EQ:
                 outputBinary(ast, "=");
                 break;
             case NE:
                 outputBinary(ast, "!=");
                 break;
             case PLUS:
                 outputBinary(ast, "+");
                 break;
             case LT:
                 outputBinary(ast, "<");
                 break;
             case LE:
                 outputBinary(ast, "<=");
                 break;
             case GT:
                 outputBinary(ast, ">");
                 break;
             case GE:
                 outputBinary(ast, ">=");
                 break;
             case LIST:
                 builder.append('(');
                 outputArguments(ast.next());
                 builder.append(')');
                 break;
             case TO:
                 builder.append('(');
                 output(ast.second());
                 builder.append(" to ");
                 output(ast.third());
                 builder.append(')');
                 break;
             case ELEMENT:
                 outputElement(ast);
                 break;
             default:
                 throw new RuntimeException("Not Implemented: " + t);
         }
     }
 
     private void outputArguments(Cons arguments) {
         if (arguments != null) {
             output(arguments.first());
             for (Object x: arguments.rest()) {
                 builder.append(", ");
                 output(x);
             }
         }
     }
 
     private void outputFlower(Cons ast) {
         Cons forlets = ((Cons)ast.second()).next();
         for (Object forlet: forlets) {
             outputForlet((Cons) forlet);
         }
         Cons groupby = (Cons) ast.nth(4);
         if (groupby != null) {
             builder.append("group by ");
             outputArguments(groupby.next());
             builder.append('\n');
         }
         Cons where = (Cons) ast.nth(3);
         if (where != null) {
             builder.append("where ");
             output(where);
             builder.append('\n');
         }
         builder.append("return ");
         output(ast.third());
     }
 
     private void outputForlet(Cons ast) {
         switch ((TokenType) ast.first()) {
             case FOR:
                 builder.append("for ");
                 output(ast.second());
                 builder.append(" in ");
                 output(ast.third());
                 builder.append('\n');
                 break;
             case LET:
                 builder.append("let ");
                 output(ast.second());
                 builder.append(" := ");
                 output(ast.third());
                 builder.append('\n');
                 break;
             default:
                 throw new RuntimeException("Not Implemented!");
         }
     }
 
     private void outputElement(Cons ast) {
         String tag = (String) ast.nth(1);
         Cons attrs = (Cons) ast.nth(2);
         Cons contents = (Cons) ast.nth(3);
         builder.append('<');
         builder.append(tag);
         if (attrs != null) {
             for (Object x : attrs) {
                 Cons a = (Cons) x;
                 String name = (String) a.first();
                 Cons values = (Cons) a.second();
                 builder.append(' ');
                 builder.append(name);
                 builder.append('=');
                 builder.append('"');
                 for (Object v: values) {
                     if (v instanceof String) {
                         builder.append(v);
                     }
                     else {
                         builder.append('{');
                         output(v);
                         builder.append('}');
                     }
                 }
                 builder.append('"');
             }
         }
         if (contents != null) {
            builder.append('>');
             for (Object v: contents) {
                 if (v instanceof String) {
                     builder.append(v);
                 }
                 else {
                    if (((Cons) v).first() == TokenType.ELEMENT) {
                        output(v);
                    }
                    else {
                        builder.append('{');
                        output(v);
                        builder.append('}');
                    }
                 }
             }
             builder.append("</");
             builder.append(tag);
             builder.append('>');
         }
         else {
             builder.append("/>");
         }
     }
 
     private void outputCall(Cons ast) {
         ast = ast.next();
         builder.append((String) ast.first());
         builder.append('(');
         outputArguments(ast.next());
         builder.append(')');
     }
 
     private void outputBinary(Cons ast, String op) {
         output(ast.second());
         builder.append(" ");
         builder.append(op);
         builder.append(" ");
         output(ast.third());
     }
 
     private void outputIndent(int n) {
         for (int i = 0; i < n; i++) {
             builder.append("  ");
         }
     }
 
     public static String unparse(Cons ast) {
         Unparser unparser = new Unparser();
         unparser.output(ast);
         return unparser.builder.toString();
     }
 
     public static void main(String[] args) {
        String xquery = "<x> <b></b>{2}</x>";
         System.out.println(unparse(org.libj.xquery.Compile.compileToAST(xquery)));
     }
 }
