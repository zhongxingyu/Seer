 package org.libj.xquery.parser;
 
 import org.libj.xquery.lexer.Token;
 import static org.libj.xquery.lexer.TokenType.*;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class AST extends Cons {
     
     public AST() {
         // do nothing
     }
     
     public AST(Object x) {
         super(x);
     }
     
     public AST(int tokenType) {
         this(new Token(tokenType, null));
     }
 
     public Token getToken() {
         return (Token) first();
     }
     public int getNodeType() {
         return getToken().type;
     }
 
     public String getNodeText() {
         return getToken().text;
     }
 
     public AST rest() {
         AST rest = (AST) next();
         if (rest != null) {
             return rest;
         }
         return new AST() {
             @Override
             public Cons next() {
                 throw new NullPointerException("Nil access");
             }
 
             @Override
             public void car(Object x) {
                 throw new NullPointerException("Nil access");
             }
 
             @Override
             public void cdr(Cons x) {
                 throw new NullPointerException("Nil access");
             }
 
             @Override
             public Object first() {
                 throw new NullPointerException("Nil access");
             }
 
             @Override
             public int size() {
                 return 0;
             }
 
             @Override
             public List<Object> toList() {
                 return toList(null);
             }
 
             @Override
             public Iterator<Object> iterator() {
                 return iterator(null);
             }
 
             @Override
             public AST nth(int i) {
                 throw new NullPointerException("Nil access");
             }
 
             @Override
             public String toString() {
                 return "nil";
             }
         };
     }
     public void addChild(AST t) {
         Cons list = this;
         while (list.next() != null) {
             list = list.next();
         }
         list.cdr(new AST(t));
     }
 
     public void addChild(Token t) {
         addChild(new AST(t));
     }
 
     public AST nth(int i) {
         Cons list = this;
         while (i-- > 0) {
             list = list.next();
             if (list == null) {
                 throw new IndexOutOfBoundsException(""+i);
             }
         }
         return (AST) list.first();
     }
 
     public boolean isNil() {
         return getToken() == null;
     }
 
     public String toString() {
         Object head = first();
         if (head instanceof AST) {
             StringBuilder builder = new StringBuilder();
            builder.append("'("); // XXX: TODO: FIXME: the output doesn't make sense...
             builder.append(head);
             for (Object x: rest()) {
                 builder.append(x);
             }
            builder.append(")");
             return builder.toString();
         }
         Token token = (Token) head;
         if (isNil()) {
             return "nil";
         }
         if (token.type == NUMBER || token.type == VARIABLE || token.type == WORD) {
             return token.text;
         }
         if (token.type == STRING) {
             return '"'+token.text+'"';
         }
         if (token.type == STRING) {
             return '"'+token.text.replace('"', '\'')+'"';
         }
         if (token.type == XPATH) {
             return "(xpath \""+token.text.replace("\"", "\\\"")+"\")";
         }
         StringBuilder builder = new StringBuilder();
         builder.append("(");
         if (token.type == CALL) {
             builder.append("funcall");
         }
         else if (token.type == LIST) {
             builder.append("list");
         }
         else if (token.type == FOR || token.type == LET ||
                  token.type == AND || token.type == OR ||
                  token.type == PLUS || token.type == MINUS || token.type == MULTIPLY || token.type == DIV ||
                  token.type == EQ || token.type == ASSIGN) {
             builder.append(token.text);
         }
         else if (token.text == null) {
             builder.append(toTypeName(token.type));
         }
         else {
             builder.append(token);
         }
         AST node = (AST) next();
         while (node != null) {
             builder.append(' ');
            builder.append(node);
             node = (AST) node.next();
         }
         builder.append(")");
         return builder.toString();
     }
 }
