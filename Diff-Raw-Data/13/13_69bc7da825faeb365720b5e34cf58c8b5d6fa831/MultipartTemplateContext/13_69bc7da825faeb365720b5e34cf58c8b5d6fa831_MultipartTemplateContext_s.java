 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 Myles Chippendale
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
  *     Myles Chippendale <mylesc At paneris.org>
  *     http://paneris.org/
  *     29 Stanley Road, Oxford, OX4 1QY, UK
  */
 
 
 package org.melati.template;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import javax.servlet.http.HttpSession;
 import org.melati.Melati;
 import org.melati.servlet.MultipartFormField;
 import org.melati.servlet.MultipartFormDataDecoder;
 
 
 /**
  * A {@link ServletTemplateContext} which allows access to the filename and
  * body of any file which is uploaded from a HTML form field.
  *
  * (by setting its ENCTYPE to ``multipart/form-data'' and 
  * setting the field's type to * ``file'').
  * <p>
  * You can retrieve the value of any field variable as usual by
  * using getForm(s).
  * <p>
  * Note that you need to pass in a {@link ServletTemplateContext} to the constructor.
  */
 public class MultipartTemplateContext implements ServletTemplateContext {
   ServletTemplateContext peer;
  Hashtable fields;
   Melati melati;
 
   /**
    * Constructor.
    * @param melati our Melati
    * @param context the ServletTemplateContext
    */
   public MultipartTemplateContext(Melati melati, ServletTemplateContext context)
       throws IOException {
     peer = context;
     this.melati = melati;
     try {
       InputStream in = melati.getRequest().getInputStream();
       MultipartFormDataDecoder decoder=
         new MultipartFormDataDecoder(
                              melati,
                              in,
                              melati.getRequest().getContentType(),
                              melati.getConfig().getFormDataAdaptorFactory());
       fields = decoder.parseData();
     }
     catch (IOException e) {
      fields = new Hashtable();
       throw e;
     }
   }
 
   /**
    * Constructor.
    * @param melati our Melati
    * @param context the ServletTemplateContext
    * @param maxSize maximum allowed size 
    */
   public MultipartTemplateContext(Melati melati, ServletTemplateContext context,
                                   int maxSize)
       throws IOException {
     peer = context;
     this.melati = melati;
     try {
       InputStream in = melati.getRequest().getInputStream();
       MultipartFormDataDecoder decoder=
         new MultipartFormDataDecoder(melati,
                              in,
                              melati.getRequest().getContentType(),
                              melati.getConfig().getFormDataAdaptorFactory(),
                              maxSize);
       fields = decoder.parseData();
     }
     catch (IOException e) {
      fields = new Hashtable();
       throw e;
     }
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.TemplateContext#put(java.lang.String, java.lang.Object)
    */
   public void put(String s, Object o) {
     peer.put(s,o);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.ServletTemplateContext#getFormField(java.lang.String)
    */
   public String getFormField(String s) {
     MultipartFormField field = (MultipartFormField)fields.get(s);
     if (field == null)
       return peer.getFormField(s);
     return field.getDataString(melati.getResponse().getCharacterEncoding());
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.ServletTemplateContext#getMultipartFormField(java.lang.String)
    */
   public MultipartFormField getMultipartFormField(String s) {
     return (MultipartFormField)fields.get(s);
   }
 
   /**
    * @return an Enumeration of Field names
    */
  public Enumeration getFieldNames() {
    return fields.elements();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.TemplateContext#get(java.lang.String)
    */
   public Object get(String s) {
     return peer.get(s);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.ServletTemplateContext#getSession()
    */
   public HttpSession getSession() {
     return peer.getSession();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.TemplateContext#getContext()
    */
   public Object getContext() {
     return peer.getContext();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.TemplateContext#setPassbackExceptionHandling()
    */
   public void setPassbackExceptionHandling() {
     peer.setPassbackExceptionHandling();    
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.template.TemplateContext#setPropagateExceptionHandling()
    */
   public void setPropagateExceptionHandling() {
     peer.setPropagateExceptionHandling();    
   }
 
   
 }
 
 
 
 
 
 
