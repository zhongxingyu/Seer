 package org.codehaus.xfire.transport.http;
 
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.util.STAXUtils;
 
 /**
  * Provides a basic HTML description of a {@link Service}.
  *
  * @author <a href="poutsma@mac.com">Arjen Poutsma</a>
  */
 public class HtmlServiceWriter
 {
 	private HttpServletRequest request;
 	
     private static final String XHTML_STRICT_DTD = "<!DOCTYPE html " +
             "PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
             "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
 
     public HtmlServiceWriter(){
     	
     }
     
     
     public HtmlServiceWriter(HttpServletRequest request){
     	this.request = request;
     }
     /**
      * Writes a HTML list of services to the given stream. Each service is described with its name.
      *
      * @param out      the stream to write to
      * @param services the services
      * @throws XMLStreamException if an XML writing exception occurs
      */
     public void write(OutputStream out, Collection services)
             throws XMLStreamException
     {
        //XMLOutputFactory factory = XMLOutputFactory.newInstance();
        //XMLStreamWriter writer = factory.createXMLStreamWriter(out);
         XMLStreamWriter writer = STAXUtils.createXMLStreamWriter(out, null,null);
         writer.writeStartDocument();
         writePreamble(writer, "XFire Services");
 
         writer.writeStartElement("body");
         writer.writeStartElement("p");
    //     writer.writeCharacters("No such service");
         writer.writeEndElement(); // p
         if (!services.isEmpty())
         {
             writer.writeStartElement("p");
             writer.writeCharacters("Services:");
             writer.writeEndElement(); // p
             writer.writeStartElement("ul");
             
            request.getPathTranslated();
            request.getProtocol();
            
            
             String base = request.getRequestURL().toString();
             
             
             request.getPathInfo();
             for (Iterator iterator = services.iterator(); iterator.hasNext();)
             {
             	Service service = (Service) iterator.next();
            	String url = base+service.getSimpleName().toString()+"?wsdl";
                 
                 writer.writeStartElement("li");
                 writer.writeCharacters(service.getSimpleName().toString());
                 Object obj =service.getProperty(Service.DISABLE_WSDL_GENERATION);
                 
                 if(obj ==null || "false".equals(obj.toString().toLowerCase())){
                 writer.writeCharacters(" ");
                 writer.writeStartElement("a");
                 writer.writeAttribute("href",url);
                 writer.writeCharacters("[wsdl]");
                 writer.writeEndElement();
                 }
                 writer.writeEndElement(); // li
             }
         }
 
         writer.writeEndDocument();
         writer.flush();
     }
 
     /**
      * Writes a HTML description of a service to the given stream.
      *
      * @param out     the stream to write to
      * @param service the service
      * @throws XMLStreamException if an XML writing exception occurs
      */
     public void write(OutputStream out, Service service)
             throws XMLStreamException
     {
         /*XMLOutputFactory factory = XMLOutputFactory.newInstance();
         XMLStreamWriter writer = factory.createXMLStreamWriter(out);*/
         XMLStreamWriter writer = STAXUtils.createXMLStreamWriter(out, null,null);
         writer.writeStartDocument();
         String title = service.getSimpleName() + " Web Service";
         writePreamble(writer, title);
 
         writer.writeStartElement("body");
         writer.writeStartElement("h1");
         writer.writeCharacters(title);
         writer.writeEndElement(); // h1
 
         writer.writeEndDocument();
         writer.flush();
     }
 
     private void writePreamble(XMLStreamWriter writer, String title)
             throws XMLStreamException
     {
         writer.writeDTD(XHTML_STRICT_DTD);
         writer.writeStartElement("html");
         writer.writeStartElement("head");
         writer.writeStartElement("title");
         writer.writeCharacters(title);
         writer.writeEndElement(); // title
         writer.writeEndElement(); // head
     }
 
 
 }
