 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package javax.xml.stream.events;
 
 /**
  * This describes the interface to Characters events. All text events get
  * reported as Characters events. Content, CData and whitespace are all reported
  * as Characters events. IgnorableWhitespace, in most cases, will be set to
  * false unless an element declaration of element content is present for the
  * current element.
  *
  * @author Jason T. Greene
  * @version $Id$
  */
public interface Characters
 {
    /**
     * Get the character data of this event
     */
    public String getData();
 
    /**
     * Returns true if this set of Characters is all whitespace. Whitespace
     * inside a document is reported as CHARACTERS. This method allows checking
     * of CHARACTERS events to see if they are composed of only whitespace
     * characters
     */
    public boolean isWhiteSpace();
 
    /**
     * Returns true if this is a CData section. If this event is CData its event
     * type will be CDATA
     *
     * If javax.xml.stream.isCoalescing is set to true CDATA Sections that are
     * surrounded by non CDATA characters will be reported as a single Characters
     * event. This method will return false in this case.
     */
    public boolean isCData();
 
    /**
     * Return true if this is ignorableWhiteSpace. If this event is
     * ignorableWhiteSpace its event type will be SPACE.
     */
    public boolean isIgnorableWhiteSpace();
 }
