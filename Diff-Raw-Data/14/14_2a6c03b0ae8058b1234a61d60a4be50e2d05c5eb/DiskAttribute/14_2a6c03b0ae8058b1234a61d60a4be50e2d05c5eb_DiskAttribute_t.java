 /*
  * JBoss, Home of Professional Open Source
  *
  * Copyright 2009, Red Hat Middleware LLC, and individual contributors
  * by the @author tags. See the COPYRIGHT.txt in the distribution for a
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
 package org.jboss.netty.handler.codec.http2;
 
 import java.io.IOException;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 
 /**
  * Disk implementation of Attributes
  * @author frederic bregier
  *
  */
 public class DiskAttribute extends AbstractDiskHttpData implements Attribute {
     public static String baseDirectory = null;
 
     public static boolean deleteOnExitTemporaryFile = true;
 
     public static String prefix = "Attr_";
 
     public static String postfix = ".att";
 
     /**
      * Constructor used for huge Attribute
      * @param name
      */
     public DiskAttribute(String name) {
         super(name, HttpCodecUtil.DEFAULT_CHARSET, 0);
     }
     /**
      *
      * @param name
      * @param value
      * @throws NullPointerException
      * @throws IllegalArgumentException
      * @throws IOException
      */
     public DiskAttribute(String name, String value)
             throws NullPointerException, IllegalArgumentException, IOException {
         super(name, HttpCodecUtil.DEFAULT_CHARSET, 0); // Attribute have no default size
         setValue(value);
     }
 
     public HttpDataType getHttpDataType() {
         return HttpDataType.Attribute;
     }
 
     public String getValue() throws IOException {
         byte [] bytes = get();
         return new String(bytes, charset);
     }
 
     public void setValue(String value) throws IOException {
         if (value == null) {
             throw new NullPointerException("value");
         }
         byte [] bytes = value.getBytes(charset);
         ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(bytes);
         if (definedSize > 0) {
             definedSize = buffer.readableBytes();
         }
         setContent(buffer);
     }
 
     @Override
     public void addContent(ChannelBuffer buffer, boolean last) throws IOException {
         int localsize = buffer.readableBytes();
        super.addContent(buffer, last);
         if (definedSize > 0 && definedSize < size + localsize) {
             definedSize = size + localsize;
         }
     }
     @Override
     public int hashCode() {
         return getName().hashCode();
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof Attribute)) {
             return false;
         }
         Attribute attribute = (Attribute) o;
         return getName().equalsIgnoreCase(attribute.getName());
     }
 
     public int compareTo(InterfaceHttpData arg0) {
         if (!(arg0 instanceof Attribute)) {
             throw new ClassCastException("Cannot compare " + getHttpDataType() +
                     " with " + arg0.getHttpDataType());
         }
         return compareTo((Attribute) arg0);
     }
 
     public int compareTo(Attribute o) {
         return getName().compareToIgnoreCase(o.getName());
     }
 
     @Override
     public String toString() {
         try {
             return getName() + "=" + getValue();
         } catch (IOException e) {
             return getName() + "=IoException";
         }
     }
 
     protected boolean deleteOnExit() {
         return deleteOnExitTemporaryFile;
     }
 
     protected String getBaseDirectory() {
         return baseDirectory;
     }
 
     protected String getDiskFilename() {
         return getName()+postfix;
     }
 
     protected String getPostfix() {
         return postfix;
     }
 
     protected String getPrefix() {
         return prefix;
     }
 }
