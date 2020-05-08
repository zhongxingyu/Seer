 package com.bics.jira.mail.converter;
 
 import com.bics.jira.mail.model.HandlerModel;
 import com.bics.jira.mail.model.MessageAdapter;
 import org.apache.commons.lang.StringUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Comment;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.DocumentType;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.nodes.XmlDeclaration;
 import org.jsoup.select.NodeVisitor;
 
 /**
  * JavaDoc here
  *
  * @author Victor Polischuk
  * @since 10.02.13 1:54
  */
 public class OutlookHtmlConverter implements BodyConverter {
     @Override
     public boolean isSupported(HandlerModel model, MessageAdapter message, boolean stripQuotes) {
         return message.hasThreadTopic();
     }
 
     @Override
     public String convert(String body) {
         Document document = Jsoup.parse(body);
         WikiNodeVisitor visitor = new WikiNodeVisitor();
 
         document.traverse(visitor);
 
         return visitor.out.toString();
     }
 
     protected static class WikiNodeVisitor implements NodeVisitor {
         protected StringBuilder out = new StringBuilder();
         protected Node ignored;
 
         @Override
         public void head(Node node, int depth) {
             if (ignored != null) {
                 return;
             }
 
             if (shallIgnore(node, depth)) {
                 ignored = node;
                 return;
             }
 
             if (printIfText(node)) {
                 return;
             }
 
             Replacement replacement = Replacement.valueOf(node);
 
             if (replacement != null) {
                 out.append(replacement.getHead());
             }
         }
 
         @Override
         public void tail(Node node, int depth) {
             if (ignored != null && ignored != node || node instanceof TextNode) {
                 return;
             }
 
             if (ignored == node) {
                 ignored = null;
                 return;
             }
 
             Replacement replacement = Replacement.valueOf(node);
 
             if (replacement != null) {
                 out.append(replacement.getTail());
             }
         }
 
         protected boolean shallIgnore(Node node, int depth) {
             return "script".equalsIgnoreCase(node.nodeName())
                     || "style".equalsIgnoreCase(node.nodeName())
                     || node instanceof Comment
                     || node instanceof XmlDeclaration
                     || node instanceof DocumentType;
         }
 
         protected boolean printIfText(Node node) {
             if (node instanceof TextNode) {
                 String text = ((TextNode) node).text();
 
                 if (StringUtils.isNotBlank(text)) {
                     out.append(text);
                 }
 
                 return true;
             }
 
             return false;
         }
     }
 
     protected enum Replacement {
         BOLD("b", "*", "*"),
         STRONG("strong", "*", "*"),
         ITALIC("i", "_", "_"),
         EMPHASIS("em", "_", "_"),
         UNDERSCORE("u", "+", "+"),
         STRIKE("strike", "-", "-"),
         NEW_LINE("br", "", "\n"),
        PARAGRAPH("p", "", "\n"),
        TR("tr", "\n", "|"),
        TD("td", "|", ""),
        TH("th", "||", "");
 
         private final String tagName;
         private final String head;
         private final String tail;
 
         private Replacement(String tagName, String head, String tail) {
             this.tagName = tagName;
             this.head = head;
             this.tail = tail;
         }
 
         public String getHead() {
             return head;
         }
 
         public String getTail() {
             return tail;
         }
 
         public static Replacement valueOf(Node node) {
             String nodeName = node.nodeName();
 
             for (Replacement replacement : values()) {
                 if (replacement.tagName.equalsIgnoreCase(nodeName)) {
                     return replacement;
                 }
             }
 
             return null;
         }
     }
 }
