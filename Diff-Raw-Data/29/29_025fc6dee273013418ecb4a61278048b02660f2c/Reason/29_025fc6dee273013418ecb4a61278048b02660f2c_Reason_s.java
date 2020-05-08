 /*
  * Copyright (C) 2011 - Jingle Nodes - Yuilop - Neppo
  *
  *   This file is part of Switji (http://jinglenodes.org)
  *
  *   Switji is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   Switji is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with MjSip; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *   Author(s):
  *   Benhur Langoni (bhlangonijr@gmail.com)
  *   Thiago Camargo (barata7@gmail.com)
  */
 
 package org.jinglenodes.jingle;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XStreamAlias("reason")
 @XmlRootElement(name = "reason")
 public class Reason {
 
     private String text;
     private Type type;
     private static final String REASON = "reason";
     private static final String CONDITION = "condition";
 
     /*
     <xs:choice>
         <xs:element name='alternative-session'
                     type='alternativeSessionElementType'/>
         <xs:element name='busy' type='empty'/>
         <xs:element name='cancel' type='empty'/>
         <xs:element name='connectivity-error' type='empty'/>
         <xs:element name='decline' type='empty'/>
         <xs:element name='expired' type='empty'/>
         <xs:element name='failed-application' type='empty'/>
         <xs:element name='failed-transport' type='empty'/>
         <xs:element name='general-error' type='empty'/>
         <xs:element name='gone' type='empty'/>
         <xs:element name='incompatible-parameters' type='empty'/>
         <xs:element name='media-error' type='empty'/>
         <xs:element name='security-error' type='empty'/>
         <xs:element name='success' type='empty'/>
         <xs:element name='timeout' type='empty'/>
         <xs:element name='unsupported-applications' type='empty'/>
         <xs:element name='unsupported-transports' type='empty'/>
       </xs:choice>
 
      */
 
     public enum Type {
         security_error, alternative_session, busy, connectivity_error, decline, general_error, media_error, success, unsupported_applications, unsupported_transports, timeout, payment, forbidden
     }
 
 //    @XStreamAlias("no-error")
 //    public class NoError implements Type{
 //    }
 
 
     public Reason() {
     }
 
     public Reason(final Type type) {
         this(null, type);
     }
 
     public Reason(final String text, final Type type) {
         this.text = text;
         this.type = type;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(final String text) {
         this.text = text;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(final Type type) {
         this.type = type;
     }
 
     public Element getElement() {
         final Element parent = DocumentHelper.createElement(REASON);
        final Element inner = DocumentHelper.createElement(CONDITION);
         final Element child = DocumentHelper.createElement(getType().toString().replace('_', '-'));
 
        inner.add(child);
        parent.add(inner);
 
         if (text != null) {
             final Element text = DocumentHelper.createElement("text");
             parent.add(text);
             text.addText(getText());
         }
 
         return parent;
     }
 }
