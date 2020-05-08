 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 Tim Joyce
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     Tim Joyce <timj@paneris.org>
  */
 
 package org.melati.template.velocity;
 
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.ByteArrayOutputStream;
 
 /**
  * Provides an interface for objects that output from melati.
  */
 
 public class MelatiBufferedVelocityWriter extends MelatiVelocityWriter {
 
   private OutputStream underlying;
   private ByteArrayOutputStream buffer;
 
   public MelatiBufferedVelocityWriter(OutputStream output, String encoding)
       throws IOException {
     super(new ByteArrayOutputStream(), encoding);
     buffer = (ByteArrayOutputStream)outputStream;
     underlying = output;
   }
 
   public MelatiBufferedVelocityWriter(HttpServletResponse response) 
       throws IOException {
     this(response.getOutputStream(), response.getCharacterEncoding());
   }
   
   public MelatiBufferedVelocityWriter(String encoding) throws IOException {
     this(new ByteArrayOutputStream(), encoding);
   }
 
   public void close() throws IOException {
     super.close();
     buffer.writeTo(underlying);
     buffer.close(); 
     underlying.flush();
     underlying.close();
   }
   
   public void flush() throws IOException {
     out.flush();
     buffer.writeTo(underlying);
     buffer.reset(); 
     underlying.flush();
   }  
 
   public void reset() throws IOException {
     out.flush();
     buffer.reset();
   }
  
   public String asString() throws IOException {
     out.flush();
     return buffer.toString();
   }
 
 }
