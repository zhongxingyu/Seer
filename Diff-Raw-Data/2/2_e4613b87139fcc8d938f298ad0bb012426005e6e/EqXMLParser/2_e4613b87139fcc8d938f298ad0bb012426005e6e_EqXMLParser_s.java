 /*
  * EqXMLParser.java
  * Copyright (C) 2001, 2002 Klaus Rennecke.
  * Created on 12. Juli 2002, 21:00
  */
 
 package net.sourceforge.fraglets.yaelp;
 
 import org.xml.sax.DocumentHandler;
 import java.io.File;
 import com.jclark.xml.sax.Driver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import java.io.IOException;
 import org.xml.sax.Locator;
 import org.xml.sax.AttributeList;
 import java.io.InputStream;
 
 /**
  * The XML parser for saved lists.
  *
  * <p>This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * <p>This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * <p>You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  * @author  marion@users.sourceforge.net
  */
 public class EqXMLParser implements DocumentHandler {
     /** The recognizer which collects avatars. */
     protected Recognizer recognizer;
     
     protected Locator locator;
     
     protected String avatarName;
     protected long avatarTimestamp;
     protected String avatarCulture;
     protected String avatarClass;
     protected int avatarLevel;
     protected String avatarGuild;
     protected String avatarZone;
     
     protected StringBuffer buffer = new StringBuffer();
     
     /** Creates a new instance of EqXMLParser */
     public EqXMLParser(Recognizer recognizer) {
         this.recognizer = recognizer;
     }
     
     public static void parseFile(InputStream in, Recognizer recognizer)
         throws IOException, SAXException
     {
         EqXMLParser parser = new EqXMLParser(recognizer);
         Driver driver = new Driver();
         driver.setDocumentHandler(parser);
         driver.parse(new InputSource(in));
     }
     
     public void characters(char[] values, int off, int len) throws SAXException {
         buffer.append(values, off, len);
     }
     
     public void endDocument() throws SAXException {
     }
     
     public void endElement(String name) throws SAXException {
         if (name.equals("avatar")) {
             Avatar.Class clazz = avatarClass == null ? null :
                 Avatar.Class.create(avatarClass);
             Avatar.Culture culture = avatarCulture == null ? null :
                 Avatar.Culture.create(avatarCulture);
             Avatar.Guild guild = avatarGuild == null ? null :
                 Avatar.Guild.create(avatarGuild);
             Avatar.Zone zone = avatarZone == null ? null :
                 Avatar.Zone.create(avatarZone);
             Avatar avatar = recognizer.updateAvatar
                 (avatarTimestamp, avatarName, avatarLevel, clazz, culture, guild, zone);
             clearAvatar();
         } else if (name.equals("name")) {
             avatarName = buffer.toString();
         } else if (name.equals("culture")) {
             avatarCulture = buffer.toString();
         } else if (name.equals("class")) {
             avatarClass = buffer.toString();
         } else if (name.equals("level")) {
             String value = buffer.toString();
             try {
                 avatarLevel = Integer.parseInt(value);
             } catch (NumberFormatException ex) {
                 throw new SAXException(getLocationInfo("invalid level"), ex);
             }
         } else if (name.equals("guild")) {
             avatarGuild = buffer.toString();
         } else if (name.equals("zone")) {
             avatarZone = buffer.toString();
         } else if (name.equals("time")) {
             if (avatarTimestamp == 0L) {
                 avatarTimestamp = java.sql.Date
                     .valueOf(buffer.toString()).getTime();
             }
         }
         buffer.setLength(0);
     }
     
     public void ignorableWhitespace(char[] values, int off, int len) throws SAXException {
     }
     
     public void processingInstruction(String str, String str1) throws SAXException {
     }
     
     public void setDocumentLocator(Locator locator) {
         this.locator = locator;
     }
     
     public void startDocument() throws SAXException {
     }
     
     public void startElement(String name, AttributeList attrs) throws SAXException {
         try {
             if (name.equals("avatar")) {
                 clearAvatar();
                 String time = attrs.getValue("time");
                 avatarTimestamp = time == null ? 0L : Long.parseLong(time);
             } else if (name.equals("property") && avatarName != null) {
                 String time = attrs.getValue("time");
                 String value = attrs.getValue("value");
                 String propertyName = attrs.getValue("name");
                 if (time != null && value != null && time != null) {
                     long timestamp = Long.parseLong(time);
                     Avatar avatar = recognizer.updateAvatar
                        (timestamp, avatarName, 0, null, null, null, null);
                     avatar.setProperty(propertyName, value, timestamp);
                 }
             }
         } catch (NumberFormatException ex) {
             throw new SAXException(getLocationInfo("invalid timestamp"), ex);
         }
         buffer.setLength(0);
     }
     
     protected void clearAvatar() {
         avatarLevel = 0;
         avatarTimestamp = 0L;
         avatarClass = null;
         avatarCulture = null;
         avatarGuild = null;
         avatarName = null;
         avatarZone = null;
     }
     
     protected String getLocationInfo(String message) {
         if (locator != null) {
             return locator.getSystemId()+':'+locator.getLineNumber()+": "
                 + message;
         } else {
             return message;
         }
     }
 }
