 package BugTracker;
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 import java.io.*;
 
 public class Bug extends XMLEntity {
     public String bugid;
     public String createdon;
     public String title;
     public String priority;
     public String project;
     public String owner;
     public String description;
 
     public Bug() {
         
     }
 
     public Bug fromXML( Node xml ) {
         Element el = (Element)xml;
         this.bugid = el.getElementsByTagName( "bugid" ).item( 0 ).getTextContent();
         this.createdon = el.getElementsByTagName( "createdon" ).item( 0 ).getTextContent();
         this.title = el.getElementsByTagName( "title" ).item( 0 ).getTextContent();
         this.priority = el.getElementsByTagName( "priority" ).item( 0 ).getTextContent();
         this.project = el.getElementsByTagName( "project" ).item( 0 ).getTextContent();
         this.owner = el.getElementsByTagName( "owner" ).item( 0 ).getTextContent();
         this.description = el.getElementsByTagName( "description" ).item( 0 ).getTextContent();
         return this;
     }
 
     public Document createXML( Document doc, Element root ) {
         if ( !this.bugid.equals(null) ) {
             NodeList existingBugs = doc.getElementsByTagName( "bug" );
             for (int i=0; i < existingBugs.getLength(); i++) {
                 Node bug = existingBugs.item(i);
 
                 if (bug.getNodeName().equals("#text")) {
                     continue;
                 }
                 Element bugElement = (Element)bug;
                 String id = bugElement.getElementsByTagName( "bugid" ).item( 0 ).getTextContent();
                 if ( id.equals( this.bugid ) ) {
                     root.removeChild( bug );
                 }
             }
         }
         
         Element bugChild = doc.createElement("bug");
         root.appendChild(bugChild);
         
         Element childBugID = doc.createElement("bugid");
         bugChild.appendChild(childBugID);
         
         Text textBugID = doc.createTextNode(this.bugid);
         childBugID.appendChild(textBugID);
         
         Element child0 = doc.createElement("createdon");
         bugChild.appendChild(child0);
         
         Text text0 = doc.createTextNode(this.createdon);
         child0.appendChild(text0);
         
         Element child1 = doc.createElement("title");
         bugChild.appendChild(child1);
 
         Text text1 = doc.createTextNode(this.title);
         child1.appendChild(text1);
 
         Element child2 = doc.createElement("priority");
         bugChild.appendChild(child2);
 
         Text text2 = doc.createTextNode(this.priority);
         child2.appendChild(text2);
 
         Element child3 = doc.createElement("project");
         bugChild.appendChild(child3);
 
         Text text3 = doc.createTextNode(this.project);
         child3.appendChild(text3);
 
         Element child4 = doc.createElement("owner");
         bugChild.appendChild(child4);
 
         Text text4 = doc.createTextNode(this.owner);
         child4.appendChild(text4);
         
         Element child5 = doc.createElement("description");
         bugChild.appendChild(child5);
 
         Text text5 = doc.createTextNode(this.description);
         child5.appendChild(text5);
 
         return doc;
     }
 
 }
