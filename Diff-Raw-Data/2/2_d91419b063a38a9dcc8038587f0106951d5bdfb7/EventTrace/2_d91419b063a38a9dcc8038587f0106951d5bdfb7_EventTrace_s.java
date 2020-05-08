 /******************************************************************************
   Event trace translator
   Copyright (C) 2012 Sylvain Halle
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 package ca.uqac.info.trace;
 import java.util.*;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.*;
 
 import ca.uqac.info.util.Relation;
 
 /**
  * An event trace is a sequence of {@link Event}s that defines a
  * precise ordering for each event. In our particular case, the
  * trace is a simple extension of a {@link java.util.Vector},
  * providing additional methods to query properties of the
  * events it contains.
  * @author sylvain
  */
 public class EventTrace extends Vector<Event>
 {
  protected String m_eventTagName = "message";//Event
   
   /**
    * An internal instance of DOM Document used to create
    * event nodes
    */
   protected Document m_domDocument = null;
   
   /**
    * Auto-generated UID
    */
   private static final long serialVersionUID = 1L;
   
   /**
    * Creates an empty trace
    */
   public EventTrace()
   {
     super();
     try
     {
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       m_domDocument = builder.newDocument();
     }
     catch (ParserConfigurationException e)
     {
       e.printStackTrace(System.err);
     }
   }
   
   /**
    * Creates an empty trace and defines the element name
    * used to delimitate events.
    * @param eventTagName The event tag name
    */
   public EventTrace(String eventTagName)
   {
     super();
     assert eventTagName != null;
     m_eventTagName = eventTagName;
   }
   
   /**
    * Parses an event trace from an instance of DOM
    * {@link org.w3c.dom.Document}. This method is given package
    * visibility only; one is not expected to create a trace from a
    * document from elsewhere and use a {@link TraceReader} to
    * create the desired trace. 
    * @param doc The document to parse
    */
   /*package*/ void parse(Document doc)
   {
     assert doc != null;
     NodeList list = doc.getElementsByTagName(m_eventTagName);
     final int list_length = list.getLength();
     for (int i = 0; i < list_length; i++)
     {
       Node n = list.item(i);
       Event e = new Event(n);
       add(e);
     }    
   }
   
   /**
    * Creates a DOM node that can be used to populate an new event's
    * data
    * @return The DOM Node
    */
   public Node getNode()
   {
     return createElement(m_eventTagName);
   }
   
   public Node createElement(String name)
   {
     assert m_domDocument != null;
     return m_domDocument.createElement(name);
   }
   
   public Node createTextNode(String contents)
   {
     assert m_domDocument != null;
     return m_domDocument.createTextNode(contents);
   }
   /**
    *  Make a copy of the element subtree suitable for inserting into m_domDocument
    * @param nodeSource
    * @param b
    * @return
    */
   public Node importNode(Node nodeSource, Boolean b)
   {
 	  Node dup = m_domDocument.importNode(nodeSource, b);
 	  return dup;
   }
   /**
    * Sets the event tag name used to delimit events in the
    * trace. This is necessary since the parsing of an event trace
    * is made through scanning of a DOM {@link org.w3c.xml.Document};
    * hence one needs to know which element name defines an event
    * @param n The tag name
    */
   /*package*/ void setEventTagName(String n)
   {
     assert n != null;
     m_eventTagName = n;
   }
   
   /**
    * Returns the maximal arity of a message in the
    * trace
    * @see {@link Event.getArity}
    * @return
    */
   public int getMaxArity()
   {
     int max_arity = 0;
     Iterator<Event> it = iterator();
     while (it.hasNext())
     {
       Event e = it.next();
       Math.max(max_arity, e.getArity());
     }
     return max_arity;
   }
   
   /**
    * Returns the set of possible values for each parameter
    * found in the trace
    * @return A map <i>P</i> &rarr; 2<sup><i>V</i></sup> from the set
    * of parameter names <i>P</i> to a subset of values <i>V</i>
    */
   public Relation<String,String> getParameterDomain()
   {
     Relation<String,String> domains = new Relation<String,String>();
     Iterator<Event> it = iterator();
     while (it.hasNext())
     {
       Event e = it.next();
       Relation<String,String> d = e.getParameterDomain();
       domains.fuseFrom(d);
     }
     return domains;
   }
 
 }
