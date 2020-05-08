 package by.scand.info.parsers;
 
 import org.htmlparser.Node;
 import org.htmlparser.NodeFilter;
 import org.htmlparser.filters.TagNameFilter;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 
 /**
  * User: nasgor
  * Date: 10/9/12
  * Time: 3:36 PM
  */
 public class HolidayParser extends AbstractParser{
     private static final String URL = "http://www.holiday.by/tours/lastminute";
 
     public static void main(String[] args) throws ParserException {
         NodeList listMain = new NodeList ();
         NodeList listtd = new NodeList ();
         NodeFilter filter = new TagNameFilter("TR");
         for (int i = 1; i < 4; i++) {
             Node[] nodes;
             if (i == 1)
                 nodes = getNodes(URL);
             else
                 nodes = getNodes(URL + "?p="+i);
 
         for (Node node : nodes){
            if (node.getText().contains("item item-expanded")){
                 listMain.add(node);
             }
         }
 
         for (NodeIterator e = listMain.elements(); e.hasMoreNodes ();)
             e.nextNode().collectInto(listtd, filter);
 
         for (Node node : listtd.toNodeArray()){
             if (node.getText().length() > 3)
                 listtd.remove(node);
         }
 
         for (Node node : listtd.toNodeArray()){
             for (Node n : node.getChildren().toNodeArray()){
                 if (n.getText().contains("class=\"place\"")){
                     for (Node ns : n.getChildren().toNodeArray()){
                         if (ns.getText().contains("country") && ns.getChildren() != null)
                             System.out.println("country = "+ns.getChildren().elementAt(1).getLastChild().getLastChild().getText());//country
                         if (ns.getText().contains("resort") && ns.getChildren() != null)
                             System.out.println("resort ="+ns.getChildren().elementAt(1).getLastChild().getText());//resort
                         if (ns.getText().contains("hotel") && ns.getChildren() != null && ns.getChildren().elementAt(0).getLastChild() != null)
                             System.out.println("hotel = " + ns.getChildren().elementAt(0).getLastChild().getText());//hotel
                         if (ns.getText().contains("departure"))
                             System.out.println("вылет "+ns.getLastChild().getText());//departure
                     }
                 } else if (n.getText().contains("class=\"price\"")){
                     for (Node ns : n.getChildren().toNodeArray()){
                         if (ns.getChildren() != null && ns.getChildren().elementAt(0).getLastChild() != null) {
                                 System.out.print("price = " + ns.getChildren().elementAt(0).getChildren().elementAt(1).getText());//price
                             System.out.println(ns.getChildren().elementAt(0).getLastChild().getText()); //curency
                         }
                     }
                 } else if (n.getText().contains("class=\"date sorted\"")){
                     for (Node ns : n.getChildren().toNodeArray()){
                         if (ns.getChildren() != null && ns.getChildren().elementAt(1) != null && !"br/".equals(ns.getChildren().elementAt(1).getText())){
                             System.out.println("date = "+ns.getChildren().elementAt(1).getText());   //date
                         } else if (ns.getText().contains("note") && ns.getChildren() != null && ns.getChildren().elementAt(0) != null) {
                             System.out.println("days = "+ns.getChildren().elementAt(0).getText().trim().substring(0, 15).trim());   //days
                         }
                     }
                 } else if(n.getText().contains("class=\"partner\"")){
                     for (Node ns : n.getChildren().toNodeArray()){
                         if (ns.getChildren() != null)
                             System.out.println("firm name = "+ns.getChildren().elementAt(1).getLastChild().getLastChild().getText()); // firm name
                             if (ns.getChildren() != null && ns.getChildren().elementAt(3).getText().contains("class=\"list phones\"")) {
                                 for (Node nst : ns.getChildren().elementAt(3).getChildren().toNodeArray()) {  //phones
                                     if (nst.getLastChild() != null)
                                         System.out.println(nst.getLastChild().getChildren().elementAt(0).getText());
                             }
                         }
                     }
                     System.out.println("==========================");
                 }
             }
         }
     }
 }
 }
