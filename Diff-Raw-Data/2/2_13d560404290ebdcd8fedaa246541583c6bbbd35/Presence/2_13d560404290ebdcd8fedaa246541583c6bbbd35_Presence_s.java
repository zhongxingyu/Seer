 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.patuck.stegmpp.xmpp;
 
 import java.io.PrintWriter;
 import net.patuck.stegmpp.StegMPP;
 import net.patuck.stegmpp.ui.Style;
 
 /**
  * The presence class handles presence objects.
  * @author reshad
  */
 public class Presence
 {
 	
 	
 	private static PrintWriter pw=null;
 
 	
 	/**
 	 * Set the PrintWriter to write to the XMPP server.
 	 * @param p the PrintWriter object.
 	 */
 	public static void setPrintWriter(PrintWriter p)
 	{
 		pw = p;
 	}
 	
 	
 	public static void sendPresence()
 	{
 		
 	}
 	public static void receivePresence(org.jdom2.Document tag)
 	{
 		
 		if(tag.getRootElement().getAttribute("from") != null)
 		{
 			String from = tag.getRootElement().getAttribute("from").getValue();
 			if(from.substring(0, from.indexOf('/')).trim().equals(Session.to.trim()))
 			{
 				StegMPP.getUI().print("[System] ",Style.SYSTEM);
 				if(tag.getRootElement().getChildText("show") != null)
 				{
 					StegMPP.getUI().println(Session.to + " Online");
 				}
 				else if(tag.getRootElement().getAttributeValue("type") != null )
 				{
 					if(tag.getRootElement().getAttributeValue("type").equals("unavailable") )
 					{
						StegMPP.getUI().println(Session.to + " Online");
 					}
 				}
 			}
 		}
 	}
 }
