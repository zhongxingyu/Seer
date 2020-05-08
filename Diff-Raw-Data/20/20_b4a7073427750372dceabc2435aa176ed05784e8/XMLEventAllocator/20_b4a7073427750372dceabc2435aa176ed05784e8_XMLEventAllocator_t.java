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
 package javax.xml.stream.util;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.events.XMLEvent;
 
 /**
  * This interface defines a class that allows a user to register a way to
  * allocate events given an XMLStreamReader. An implementation is not required
  * to use the XMLEventFactory implementation but this is recommended. The
  * XMLEventAllocator can be set on an XMLInputFactory using the property
  * "javax.xml.stream.allocator".
  *
  * @author Jason T. Greene
  * @version $Id$
  *
  * @see javax.xml.stream.XMLInputFactory
  * @see javax.xml.stream.XMLEventFactory
  */
 public interface XMLEventAllocator
 {
 
    /**
    * This method creates an instance of the XMLEventAllocator. This
    * allows the XMLInputFactory to allocate a new instance per reader.
    */
   public XMLEventAllocator newInstance();

   /**
     * This method allocates an event given the current state of the
     * XMLStreamReader. If this XMLEventAllocator does not have a one-to-one
     * mapping between reader states and events this method will return null.
     * This method must not modify the state of the XMLStreamReader.
     *
     * @param reader The XMLStreamReader to allocate from
     * @return the event corresponding to the current reader state
     */
    public XMLEvent allocate(XMLStreamReader reader) throws XMLStreamException;
 
    /**
     * This method allocates an event or set of events given the current state of
     * the XMLStreamReader and adds the event or set of events to the consumer
     * that was passed in. This method can be used to expand or contract reader
     * states into event states. This method may modify the state of the
     * XMLStreamReader.
     *
     * @param reader The XMLStreamReader to allocate from
     * @param consumer The XMLEventConsumer to add to.
     */
    public void allocate(XMLStreamReader reader, XMLEventConsumer consumer) throws XMLStreamException;
 }
