 /*
  * (c) Copyright Microsoft Corporation. 
  * This source is subject to the Microsoft Public License.
  * See http://www.microsoft.com/opensource/licenses.mspx#Ms-PL       
  */
 package com.microsoft.hsg.things.marshallers;
 
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Node;
 
 import com.microsoft.hsg.things.Thing;
 import com.microsoft.hsg.things.Weight;
 
 /**
  * The Class WeightMarshaller.
  */
 public class WeightMarshaller implements ThingMarshaller
 {	
 	
 	/* (non-Javadoc)
 	 * @see com.microsoft.hsg.things.marshallers.ThingMarshaller#unmarhsal(org.w3c.dom.Node)
 	 */
 	public Thing unmarhsal(Node thingNode)
 	throws Exception
 	{
 		XPath xpath = XPathFactory.newInstance().newXPath();
 		Weight weight = new Weight();
         weight.setValue(xpath.evaluate("data-xml/weight/value/display", thingNode));
         weight.setId(xpath.evaluate("thing-id", thingNode));
 
         String year = xpath.evaluate("data-xml/weight/when/date/y", thingNode);
         String month = xpath.evaluate("data-xml/weight/when/date/m", thingNode);
         String day = xpath.evaluate("data-xml/weight/when/date/d", thingNode);
         String hour = xpath.evaluate("data-xml/weight/when/time/h", thingNode);
         String minute = xpath.evaluate("data-xml/weight/when/time/m", thingNode);
         String second = xpath.evaluate("data-xml/weight/when/time/s", thingNode);
 
         GregorianCalendar calendar = new GregorianCalendar();
 
         if (year != "" && month != "" && day != "")
             calendar.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
 
         if (hour != "")
             calendar.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(hour));
 
         if (minute != "")
             calendar.set(GregorianCalendar.MINUTE, Integer.parseInt(minute));
 
         if (second != "")
             calendar.set(GregorianCalendar.SECOND, Integer.parseInt(second));
         else
             calendar.set(GregorianCalendar.SECOND, 0);
 
         calendar.set(GregorianCalendar.MILLISECOND, 0);
 
         Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
         weight.setTime(timestamp.toString().substring(0, timestamp.toString().length() - 2));
 
         return weight;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.microsoft.hsg.things.marshallers.ThingMarshaller#marshal(com.microsoft.hsg.things.Thing, java.io.Writer)
 	 */
 	public void marshal(Thing thing, Writer out)
 	{
 		Weight weight = (Weight)thing;
 		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"));
 		PrintWriter writer = new PrintWriter(out);
         writer.print("<thing><type-id>");
         writer.print(Weight.Type);
         writer.print("</type-id><data-xml><weight><when><date><y>");
         writer.print(calendar.get(Calendar.YEAR));
         writer.print("</y><m>");
         writer.print(calendar.get(Calendar.MONTH) + 1);
         writer.print("</m><d>");
         writer.print(calendar.get(Calendar.DATE));
         writer.print("</d></date><time><h>");
         writer.print(calendar.get(Calendar.HOUR_OF_DAY));
         writer.print("</h><m>");
         writer.print(calendar.get(Calendar.MINUTE));
         writer.print("</m><s>");
         writer.print(calendar.get(Calendar.SECOND));
         writer.print("</s></time></when><value><kg>");
         writer.print(weight.getValue());
        writer.print("</kg><display units=\"lb\" units-code=\"lb\">");
         writer.print(weight.getValue());
         writer.print("</display></value></weight>");
         writer.print("<common/></data-xml></thing>");
         writer.flush();
 	}
 }
