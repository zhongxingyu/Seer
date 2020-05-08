 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package webtv;
 
 import javax.swing.tree.DefaultTreeModel;
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  *
  * @author marius
  */
 public class SiteMapNode extends SiteNode {
    public static final String url = "http://viastream.viasat.tv//siteMapData/lt/2lt/";
     public static final String referer = "http://flvplayer.viastream.viasat.tv/flvplayer/syndicatedPlayer/syndicated.swf";    
     
     public SiteMapNode(DefaultTreeModel model, String title, String id) {
         super(model, title, id);
         setAllowsChildren(true);
     }
 
     DefaultHandler handler = new DefaultHandler() {
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attrs) {
             //System.out.println("XML got: "+qName);
             if ("siteMapNode".equals(qName)) {
                 String t = attrs.getValue("title");
                 String i = attrs.getValue("id");
                 int a = Integer.parseInt(attrs.getValue("articles"));
                 if (!ids.contains(i)) {
                     SiteNode node;
                     if (a == 0) {
                         node = new SiteMapNode(model, t, i);
                     } else {
                         node = new ProductList(model, t, i);
                     }
                     if (refreshing) SiteMapNode.this.insert(node, 0);
                     else add(node);
                     ids.add(i);
                 }
             }
         }
     };
 
     @Override
     protected String getURL() {
         return (url+id);
     }
 
     @Override
     protected String getReferer() {
         return referer;
     }
 
     @Override
     protected DefaultHandler getHandler() {
         return handler;
     }
 
     public boolean isLeaf(){ return false; }
 }
