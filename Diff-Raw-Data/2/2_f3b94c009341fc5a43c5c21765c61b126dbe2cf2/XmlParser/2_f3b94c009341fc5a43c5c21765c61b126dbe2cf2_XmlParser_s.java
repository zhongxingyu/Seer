 package org.zachtaylor.jnodalxml;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 public class XmlParser {
   public static List<XmlNode> parse(File f) throws FileNotFoundException {
     return parse(new XmlTokenizer(f));
   }
 
   public static List<XmlNode> parse(String s) {
     return parse(new XmlTokenizer(s));
   }
 
   public static List<XmlNode> parse(XmlTokenizer tokens) {
     List<XmlNode> topLevel = new ArrayList<XmlNode>();
     Stack<XmlNode> nodes = new Stack<XmlNode>();
 
     XmlToken token;
 
     while (tokens.hasNext()) {
       token = tokens.next();
 
       if (token.getType() == XmlTokenType.OPEN_BRACKET) {
         token = tokens.next();
 
         if (token.getType() == XmlTokenType.SLASH) {
           token = tokens.next();
           if (!token.getValue().equals(nodes.peek().getName()))
             throw new RuntimeException(" uh oh ");
 
           token = tokens.next();
           if (!(token.getType() == XmlTokenType.CLOSE_BRACKET))
             throw new RuntimeException(" oh no ");
 
           XmlNode node = nodes.pop();
           if (nodes.isEmpty()) {
             topLevel.add(node);
           }
           else {
             nodes.peek().addChild(node);
           }
         }
         else {
           nodes.push(new XmlNode(token.getValue()));
           token = tokens.next();
 
           while (token.getType() == XmlTokenType.TEXT) {
             String attrName = token.getValue();
 
             if (nodes.peek().getName().startsWith("?")) {
               do {
                 token = tokens.next();
               } while (token.getType() != XmlTokenType.TEXT || !token.getValue().equals("?"));
               token = new XmlToken(XmlTokenType.SLASH);
               break;
             }
 
             token = tokens.next();
             if (!(token.getType() == XmlTokenType.EQUALS))
               throw new RuntimeException(" awkward ");
 
             token = tokens.next();
             if (!(token.getType() == XmlTokenType.QUOTE))
               throw new RuntimeException(" foo ");
 
             token = tokens.next();
             if (!(token.getType() == XmlTokenType.TEXT))
               throw new RuntimeException(" bar ");
 
             String attrValue = token.getValue();
             token = tokens.next();
             while (token.getType() == XmlTokenType.TEXT) {
               attrValue = attrValue.concat(" ").concat(token.getValue());
               token = tokens.next();
             }
 
             if (!(token.getType() == XmlTokenType.QUOTE))
               throw new RuntimeException(" foo ");
 
            nodes.peek().setAttribute(attrName, attrValue);
             token = tokens.next();
           }
 
           if (token.getType() == XmlTokenType.SLASH) {
             nodes.peek().setSelfClosing(true);
 
             token = tokens.next();
             if (!(token.getType() == XmlTokenType.CLOSE_BRACKET))
               throw new RuntimeException(" bar ");
 
             XmlNode node = nodes.pop();
             if (nodes.isEmpty()) {
               topLevel.add(node);
             }
             else {
               nodes.peek().addChild(node);
             }
           }
         }
       }
       else {
         String val = token.getValue();
 
         while (tokens.peek().getType() == XmlTokenType.TEXT) {
           val = val.concat(" ").concat(tokens.next().getValue());
         }
 
         nodes.peek().setValue(val);
       }
     }
 
     return topLevel;
   }
 }
