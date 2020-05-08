 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package pjwiki;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author aq615c
  */
 public class WikiSyntaxParserHeaders extends WikiSyntaxParser{
     public WikiSyntaxParserHeaders()
     {
         headerRoot = new headerNode("",0);
     }
 
     private class headerNode{
         public headerNode(String setname, int setsize)
         {
             name = setname;
             size = setsize;
             clean_name =  
                 name.replaceAll(invalidAnchorChars.pattern(), "_");
             clean_name = clean_name.toLowerCase();
             children = new ArrayList<headerNode>();
         }
 
         public String addChild(headerNode n)
         {
             // if there are no children, or this is a bigger/equal
             // header to the last child, just insert it at the end
             if(children.isEmpty() ||
                     (children.get(children.size() - 1)).size >= n.size)
             {
                 children.add(n);
                 if(size > 0) return clean_name + "_" + n.clean_name;
                 else return n.clean_name;
             }
             // otherwise, it's a subheader of the last, so lets add it
             else
             {
                 String child_link = children.get(children.size() - 1).addChild(n);
                 if(size > 0) return clean_name + "_" + child_link;
                 else return child_link;
             }
         }
         public String getTree(String parentLinkString)
         {
             String ret = "";
             if(size > 0)
             {
                 for(int i = 0; i<size;i++)
                 {
                     ret += "  "; // indentation per
                 }
                 ret += "* [[#" + parentLinkString + clean_name
                         + "|" + name + "]]\r\n";
             }
             // if the root node has no children (no headers)
             if(!children.isEmpty())
             {
                 if(size > 0) parentLinkString += clean_name + "_";
                 for(headerNode n:children)
                 {
                     ret += n.getTree(parentLinkString);
                 }
             }
             if(size == 0 && children.size() == 0)
             {
                 return "";
             }
             else if(size == 0){
                 return "<div id=\"TOC\">\r\n"+ret+"</div>\r\n";
             }else{
                 return ret;
             }
         }
 
         public String name;
         public String clean_name;
         public int size;
         public List<headerNode> children;
     }
 
     @Override
     public String execute(String text)
     {
         Matcher m = headers.matcher(text);
         while(m.find())
         {
             int size = 7 - m.group(1).length();
             headerNode n = new headerNode(m.group(2), size);
            String link = headerRoot.addChild(n);
            text = m.replaceFirst("<a name=\""+link+"\"><h"+size+">"
                    +n.name+"</h"+size+"></a>");
             m.reset(text);
         }
 
         text=headerRoot.getTree("") + text;
 
         return text;
     }
 
     private headerNode headerRoot;
     private static Pattern headers = 
         Pattern.compile("^(={2,6})\\s*(.+?)\\s*\\1\\s*$", Pattern.MULTILINE);
     private static Pattern invalidAnchorChars =
         Pattern.compile("[^a-zA-Z0-9_]");
 }   
