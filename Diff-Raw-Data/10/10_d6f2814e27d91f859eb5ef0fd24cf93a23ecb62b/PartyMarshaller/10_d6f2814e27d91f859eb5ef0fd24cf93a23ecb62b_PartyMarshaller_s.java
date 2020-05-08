 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package drinkcounter.util;
 
 import drinkcounter.DrinkCounterService;
 import drinkcounter.UserService;
 import drinkcounter.model.Drink;
 import drinkcounter.model.User;
 import drinkcounter.model.Party;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  *
  * @author Toni
  */
 public class PartyMarshaller {
 
     @Autowired
     private DrinkCounterService service;
 
     @Autowired
     private UserService userService;
 
     public void marshall(int partyId, OutputStream out){
         try {
             Party party = service.getParty(partyId);
             
             DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
             DocumentBuilder b = f.newDocumentBuilder();
             Document d = b.newDocument();
             Node rootNode = d.createElement("party");
             d.appendChild(rootNode);
             Node idNode = d.createElement("id");
             idNode.setTextContent(Integer.toString(party.getId()));
             rootNode.appendChild(idNode);
             Node partyNameNode = d.createElement("name");
             partyNameNode.setTextContent(party.getName());
             rootNode.appendChild(partyNameNode);
             Node usersNode = d.createElement("users");
             rootNode.appendChild(usersNode);
             for (User user : service.listUsersByParty(partyId)) {
                 usersNode.appendChild(createUserNode(d, user));
             }
             StreamResult streamResult = new StreamResult(out);
             TransformerFactory tf = TransformerFactory.newInstance();
             Transformer serializer = tf.newTransformer();
             serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             serializer.setOutputProperty(OutputKeys.INDENT, "yes");
             serializer.transform(new DOMSource(d), streamResult);
         } catch (TransformerException ex) {
             throw new RuntimeException(ex);
         } catch (ParserConfigurationException ex) {
             throw new RuntimeException(ex);
         }
     }
 
      private Node createUserNode(Document doc, User user) {
         Node userNode = doc.createElement("user");
 
         userNode.appendChild(createTextContentElement("id", Integer.toString(user.getId()), doc));
         userNode.appendChild(createTextContentElement("name", user.getName(), doc));
         userNode.appendChild(createTextContentElement("alcoholInPromilles", Float.toString(user.getPromilles()), doc));
         userNode.appendChild(createTextContentElement("totalDrinks", Integer.toString(user.getTotalDrinks()), doc));
         
         if (user.getDrinks().size() > 0) {
             Drink lastDrink = user.getDrinks().get(user.getDrinks().size() - 1);
             long millis = System.currentTimeMillis() - lastDrink.getTimeStamp().getTime();
             String seconds = Long.toString(millis / 1000);
             userNode.appendChild(createTextContentElement("idle", seconds, doc));
         }
         else
             userNode.appendChild(createTextContentElement("idle", "0", doc));
         
         return userNode;
     }
 
     private Node createDrinksNode(Document d, List<Drink> drinks){
         Node drinksNode = d.createElement("drinks");
         drinksNode.appendChild(createTextContentElement("count", Integer.toString(drinks.size()), d));
        for (Drink drink : drinks) {
             Node drinkNode = d.createElement("drink");
             DateTime dateTime = new DateTime(drink.getTimeStamp());
             drinkNode.appendChild(createTextContentElement("id", Integer.toString(drink.getId()), d));
             drinkNode.appendChild(createTextContentElement("timestamp", dateTime.toString("dd.MM.yyyy HH:mm"), d));
             drinksNode.appendChild(drinkNode);
         }
         return drinksNode;
     }
 
     private Element createTextContentElement(String name, String content, Document document){
         Element element = document.createElement(name);
         element.setTextContent(content);
         return element;
     }
 
     public void marshallDrinks(int userId, OutputStream out){
         try {
             User user = userService.getUser(userId);
 
             DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
             DocumentBuilder b = f.newDocumentBuilder();
             Document d = b.newDocument();
             Node rootNode = d.createElement("user");
             d.appendChild(rootNode);
 
             Node idNode = d.createElement("id");
             idNode.setTextContent(Integer.toString(user.getId()));
             rootNode.appendChild(idNode);
 
             rootNode.appendChild(createDrinksNode(d, user.getDrinks()));
 
             StreamResult streamResult = new StreamResult(out);
             TransformerFactory tf = TransformerFactory.newInstance();
             Transformer serializer = tf.newTransformer();
             serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             serializer.setOutputProperty(OutputKeys.INDENT, "yes");
             serializer.transform(new DOMSource(d), streamResult);
         } catch (TransformerException ex) {
             throw new RuntimeException(ex);
         } catch (ParserConfigurationException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public void marshallUser(int userId, ByteArrayOutputStream out) {
         try {
             User user = userService.getUser(userId);
 
             DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
             DocumentBuilder b = f.newDocumentBuilder();
             Document d = b.newDocument();
 
             Node rootNode = createUserNode(d, user);
             d.appendChild(rootNode);
             
             StreamResult streamResult = new StreamResult(out);
             TransformerFactory tf = TransformerFactory.newInstance();
             Transformer serializer = tf.newTransformer();
             serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             serializer.setOutputProperty(OutputKeys.INDENT, "yes");
             serializer.transform(new DOMSource(d), streamResult);
         } catch (TransformerException ex) {
             throw new RuntimeException(ex);
         } catch (ParserConfigurationException ex) {
             throw new RuntimeException(ex);
         }
     }
 }
