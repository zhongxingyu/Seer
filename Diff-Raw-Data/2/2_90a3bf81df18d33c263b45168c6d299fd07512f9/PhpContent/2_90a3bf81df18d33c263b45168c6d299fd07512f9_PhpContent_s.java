 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.bridge.content;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import de.cosmocode.palava.bridge.MimeType;
 
 /**
  * Content which uses PHP notation.
  * 
  * @author Detlef Hüttemann
  * @author Willi Schoenborn
  * @deprecated use {@link JsonContent} instead
  */
 @Deprecated
 public class PhpContent extends AbstractContent {
     
     public static final PhpContent OK;
     public static final PhpContent NOT_FOUND;
     
     static {
         try {
             OK = new PhpContent("ok");
             NOT_FOUND = new PhpContent("not_found");
         } catch (ConversionException e) {
             throw new ExceptionInInitializerError(e);
         }
     };
     
    private final byte [] bytes;
     
     public PhpContent(Object object) throws ConversionException {
         super(MimeType.PHP);
         final PHPConverter converter = new PHPConverter();
         final StringBuffer buf = new StringBuffer();
         converter.convert(buf, object);
         bytes = buf.toString().getBytes(CHARSET);
     }
     
     @Override
     public long getLength() {
         return bytes.length;
     }
     
     @Override
     public void write(OutputStream out) throws IOException {
         out.write(bytes, 0, bytes.length);
     }
     
     @Override
     public String toString() {
         return new String(bytes);
     }
     
 }
