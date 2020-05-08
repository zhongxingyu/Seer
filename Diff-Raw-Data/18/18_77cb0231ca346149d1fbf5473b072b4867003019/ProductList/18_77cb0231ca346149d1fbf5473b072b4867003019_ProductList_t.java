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
 public class ProductList extends SiteNode {
    public static final String url = "http://viastream.viasat.tv/Products/Category/";
     public static final String referer = SiteMapNode.referer;
 
     public ProductList(DefaultTreeModel model, String title, String id){
         super(model, title,id);
         setAllowsChildren(true);
     }
 
     @Override
     protected String getURL() {
         return (url+id);
     }
 
     @Override
     protected String getReferer() {
         return referer;
     }
 
 
     DefaultHandler handler = new DefaultHandler() {
 
         String prodId;
         String prodTitle;
         int field = 0;
 
         @Override
         public void characters(char[] ch, int start, int length) {
             switch (field) {
                 case 2:
                     prodId = new String(ch, start, length);
                     break;
                 case 3:
                     prodTitle = new String(ch, start, length);
                     break;
             }
         }
 
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attrs) {
             if ("Product".equals(qName)) {
                 prodId = null;
                 prodTitle = null;
                 field = 1;
             } else if (field == 1 && "ProductId".equals(qName)) {
                 field = 2;
             } else if (field == 1 && "Title".equals(qName)) {
                 field = 3;
             }
         }
 
         @Override
         public void endElement(String uri, String localName, String qName) {
             if (field == 3 && "Title".equals(qName)) {
                 field = 1;
             } else if (field == 2 && "ProductId".equals(qName)) {
                 field = 1;
             } else if ("Product".equals(qName) && !ids.contains(prodId)) {
                 SiteNode node = new Product(model, prodTitle, prodId);
                 if (refreshing) ProductList.this.insert(node, 0);
                 else add(node);
                 ids.add(prodId);
                 field = 0;
             }
         }
     };
 
     @Override
     protected DefaultHandler getHandler() {
         return handler;
     }
     
     public boolean isLeaf(){ return false; }
 
 }
