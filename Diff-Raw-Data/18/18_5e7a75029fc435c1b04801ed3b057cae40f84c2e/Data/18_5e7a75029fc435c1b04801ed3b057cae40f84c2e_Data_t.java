 package net.daboross.bukkitdev.playerdata;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 import net.daboross.xmlhelpers.DXMLException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * This is a object that holds custom data given to and from the
  * PlayerDataHandler. It holds a name, which is the data type, an Array of
  * strings, which are the arguments, and it holds it's owner. Its owner is a
  * PData, and the owner is set whenever this data is added to a PData. There is
  * no way to set the owner besides giving this data to a PData, or the
  * PlayerDataHandler.
  *
  * @author daboross
  */
 public class Data {
 
     /**
      * This is the data type.
      */
     private String name;
     /**
      * This is the raw data.
      */
     private List<String> data;
     /**
      * This is the owner.
      */
     private PData owner;
 
     /**
      * Create a Data with the Data Type name, and the Data data.
      *
      * @param name This is the type of this Data.
      * @param data This is the raw data.
      */
     public Data(String name, String[] data) {
         this.name = name;
         this.data = new ArrayList<String>(Arrays.asList(data));
     }
 
     /**
      * This gets the data type.
      *
      * @return This Type Of Data.
      */
     public String getName() {
         return name;
     }
 
     /**
      * This gets the raw data.
      *
      * @return The Raw Data stored in this Data.
      */
     public String[] getData() {
         return data.toArray(new String[data.size()]);
     }
 
     /**
      * This gets the PData who is the owner of this Data. This will return null
      * if this Data has never been added to a PData.
      */
     public PData getOwner() {
         return owner;
     }
 
     /**
      * This should be called from PData when this is added to PData. This should
      * never be called except by the PData class.
      */
     protected void setOwner(PData pData) {
         this.owner = pData;
     }
 
     public void putDataOnXML(Element e) {
         e.setAttribute("name", name);
         Element dataElement = e.getOwnerDocument().createElement("data");
         for (int i = 0; i < data.size(); i++) {
             dataElement.setAttribute("dataline" + i, data.get(i));
         }
         e.appendChild(dataElement);
     }
 
     public Data(Node node) throws DXMLException {
         NamedNodeMap attributes = node.getAttributes();
         for (int i = 0; i < attributes.getLength(); i++) {
             Node n = attributes.item(i);
             if (n.getNodeName().equals("name")) {
                 this.name = n.getNodeValue();
             }
         }
         NodeList childNodes = node.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node n = childNodes.item(i);
             if (n.getNodeName().equals("data")) {
                 NamedNodeMap list2 = n.getAttributes();
                 data = new ArrayList<String>(list2.getLength());
                Map<String, String> beforeData = new HashMap<String, String>();
                 for (int k = 0; k < list2.getLength(); k++) {
                     Node n2 = list2.item(k);
                     if (n2.getNodeName().startsWith("dataline")) {
                        beforeData.put(n2.getNodeName(), n2.getNodeValue());
                     } else {
                         throw new DXMLException("Unknown Attribute on data child:" + n2.getNodeName());
                     }
                 }
                int k = 0;
                while (true) {
                    String str = beforeData.get("dataline" + k++);
                    if (str == null) {
                        break;
                    }
                    data.add(str);
                }

             }
             if (data == null || name == null) {
                 throw new DXMLException("Not Data Element");
             }
         }
     }
 }
