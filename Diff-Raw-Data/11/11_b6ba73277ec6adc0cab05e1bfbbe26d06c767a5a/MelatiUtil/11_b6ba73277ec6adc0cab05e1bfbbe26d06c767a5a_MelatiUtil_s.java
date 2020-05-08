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
  *     http://paneris.org/
  *     68 Sandbanks Rd, Poole, Dorset. BH14 8BY. UK
  */
 
 package org.melati;
 
 import java.util.Enumeration;
 
 import java.net.URLEncoder;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.melati.poem.Persistent;
 import org.melati.poem.Column;
 import org.melati.template.TemplateContext;
 import org.melati.template.TempletAdaptor;
 import org.melati.template.TempletAdaptorConstructionMelatiException;
 import org.melati.util.Tree;
 import org.melati.util.JSDynamicTree;
 
 /**
  * MelatiUtil is a place where useful Static methods can be put.
  */
 
 public class MelatiUtil {
 
   public static void extractFields(TemplateContext context, Persistent object) {
     for (Enumeration c = object.getTable().columns(); c.hasMoreElements();) {
       Column column = (Column)c.nextElement();
       String formFieldName = "field_" + column.getName();
       String rawString = context.getForm(formFieldName);
 
       String adaptorFieldName = formFieldName + "-adaptor";
       String adaptorName = context.getForm(adaptorFieldName);
       if (adaptorName != null) {
         TempletAdaptor adaptor;
         try {
           // FIXME cache this instantiation
           adaptor = (TempletAdaptor)Class.forName(adaptorName).newInstance();
         }
         catch (Exception e) {
           throw new TempletAdaptorConstructionMelatiException(
               adaptorFieldName, adaptorName, e);
         }
         column.setRaw(object, adaptor.rawFrom(context, formFieldName));
      }
      else {
         if (rawString != null) {
           if (rawString.equals("")) {
             if (column.getType().getNullable())
               column.setRaw(object, null);
             else
               column.setRawString(object, "");
           }
          else
          column.setRawString(object, rawString);
         }
       }
     }
   }
 
   public static Object extractField(TemplateContext context, String fieldName)
       throws TempletAdaptorConstructionMelatiException {
 
     String rawString = context.getForm(fieldName);
 
     String adaptorFieldName = fieldName + "-adaptor";
     String adaptorName = context.getForm(adaptorFieldName);
 
     if (adaptorName != null) {
       TempletAdaptor adaptor;
       try {
         // FIXME cache this instantiation
         adaptor = (TempletAdaptor)Class.forName(adaptorName).newInstance();
       }
       catch (Exception e) {
         throw new TempletAdaptorConstructionMelatiException(
         adaptorFieldName, adaptorName, e);
       }
       return adaptor.rawFrom(context, fieldName);
     }
     return rawString;
   }
 
 
   // get a tree object
   public JSDynamicTree getJSDynamicTree(Tree tree) {
     return new JSDynamicTree(tree);
   }
 
   /**
    * Modify or add a form parameter setting (query string component) in a URL.
    *
    * @param uri     A URI
    * @param query   A query string
    * @param field   The parameter's name
    * @param value   The new value for the parameter (unencoded)
    * @return        <TT><I>uri</I>?<I>query</I></TT> with <TT>field=value</TT>.
    *                If there is already a binding for <TT>field</TT> in the
    *                query string it is replaced, not duplicated.
    * @see org.melati.util.MelatiUtil
    */
 
   public static String sameURLWith(String uri, String query,
                                    String field, String value) {
     return uri + "?" + sameQueryWith(query, field, value);
   }
 
   /**
    * Modify or add a form parameter setting (query string component) in the URL
    * for a servlet request.
    *
    * @param request A servlet request
    * @param field   The parameter's name
    * @param value   The new value for the parameter (unencoded)
    * @return        The request's URL with <TT>field=value</TT>.  If there is
    *                already a binding for <TT>field</TT> in the query string
    *                it is replaced, not duplicated.  If there is no query
    *                string, one is added.
    * @see org.melati.util.MelatiUtil
    */
 
   public static String sameURLWith(HttpServletRequest request,
                                    String field, String value) {
     return sameURLWith(request.getRequestURI(), request.getQueryString(),
                        field, value);
   }
 
   /**
    * Modify or add a form parameter setting (query string component) in a query
    * string.
    *
    * @param qs      A query string
    * @param field   The parameter's name
    * @param value   The new value for the parameter (unencoded)
    * @return        <TT>qs</TT> with <TT>field=value</TT>.
    *                If there is already a binding for <TT>field</TT> in the
    *                query string it is replaced, not duplicated.
    * @see org.melati.util.MelatiUtil
    */
 
   public static String sameQueryWith(String qs, String field, String value) {
     String fenc = URLEncoder.encode(field);
     String fenceq = fenc + '=';
     String fev = fenceq + URLEncoder.encode(value);
 
     if (qs == null || qs.equals("")) return fev;
 
     int i;
     if (qs.startsWith(fenceq)) i = 0;
     else {
       i = qs.indexOf('&' + fenceq);
       if (i == -1) return qs + '&' + fev;
       ++i;
     }
 
     int a = qs.indexOf('&', i);
     return qs.substring(0, i) + fev + (a == -1 ? "" : qs.substring(a));
   }
   
     
   /**
   * a useful utility method that gets a value from the Form.  It will return
   * null if the value is "" or not present
   *
   * @param context - a template context
   * @param field - the name of the field to get
   *
   * @return - the value of the field requested
   */
   public static String getFormNulled(TemplateContext context, String field) {
     return getForm(context,field,null);
   }
   
     
   /**
   * a useful utility method that gets a value from the Form.  It will return
   * the default if the value is "" or not present
   *
   * @param context - a template context
   * @param field - the name of the field to get
   * @param def - the default value if the field is "" or not present
   *
   * @return - the value of the field requested
   */
   public static String getForm(TemplateContext context, String field, 
                                String def) {
     String val = context.getForm(field);
     if (val == null) return def;
     return val.trim().equals("")?def:val;
   }
 
   /**
   * a useful utility method that gets a value from the Form as an Integer.  
   * It will return null if the value is "" or not present
   *
   * @param context - a template context
   * @param field - the name of the field to get
   * @param def - the default value if the field is "" or not present
   *
   * @return - the value of the field requested
   */
   public static Integer getFormInteger(TemplateContext context, String field, 
                                 Integer def) {
     String val = getFormNulled(context,field);
     return val==null?def:new Integer(val);
   }
 
   /**
   * a useful utility method that gets a value from the Form as an Integer.  
   * It will return null if the value is "" or not present
   *
   * @param context - a template context
   * @param field - the name of the field to get
   *
   * @return - the value of the field requested
   */
   public static Integer getFormInteger(TemplateContext context, String field) {
     return getFormInteger(context,field,null);
   }
 
   /**
   * a useful utility method that tests weather a field is present in a Form,
   * returning a Boolean.  
   *
   * @param context - a template context
   * @param field - the name of the field to get
   *
   * @return - TRUE or FALSE depending if the field is present
   */
   public static Boolean getFormBoolean(TemplateContext context, String field) {
     return getFormNulled(context,field) ==  null ? Boolean.FALSE : Boolean.TRUE;
   }
 
 }
