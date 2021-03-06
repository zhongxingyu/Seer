 /*
  * $Header$
  * $Revision$
  * $Date$
  *
  * ====================================================================
  *
  * Copyright (C) 2000-2002 bob mcwhirter & James Strachan.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions, and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions, and the disclaimer that follows 
  *    these conditions in the documentation and/or other materials 
  *    provided with the distribution.
  *
  * 3. The name "Jaxen" must not be used to endorse or promote products
  *    derived from this software without prior written permission.  For
  *    written permission, please contact license@jaxen.org.
  * 
  * 4. Products derived from this software may not be called "Jaxen", nor
  *    may "Jaxen" appear in their name, without prior written permission
  *    from the Jaxen Project Management (pm@jaxen.org).
  * 
  * In addition, we request (but do not require) that you include in the 
  * end-user documentation provided with the redistribution and/or in the 
  * software itself an acknowledgement equivalent to the following:
  *     "This product includes software developed by the
  *      Jaxen Project (http://www.jaxen.org/)."
  * Alternatively, the acknowledgment may be graphical using the logos 
  * available at http://www.jaxen.org/
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE Jaxen AUTHORS OR THE PROJECT
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * ====================================================================
  * This software consists of voluntary contributions made by many 
  * individuals on behalf of the Jaxen Project and was originally 
  * created by bob mcwhirter <bob@werken.com> and 
  * James Strachan <jstrachan@apache.org>.  For more information on the 
  * Jaxen Project, please see <http://www.jaxen.org/>.
  * 
  * $Id$
  */
 
 
 package org.jaxen.function;
 
 import org.jaxen.Context;
 import org.jaxen.Function;
 import org.jaxen.FunctionCallException;
 import org.jaxen.Navigator;
 import org.jaxen.UnsupportedAxisException;
 import org.jaxen.JaxenRuntimeException;
 
 import java.util.List;
 import java.util.Iterator;
 
 /**
  * <p><b>4.2</b> <code><i>string</i> string(<i>object</i>)</code>
  *
  * @author bob mcwhirter (bob @ werken.com)
  */
 public class StringFunction implements Function
 {
     
     public Object call(Context context,
                        List args) throws FunctionCallException
     {
         int size = args.size();
 
         if ( size == 0 )
         {
             return evaluate( context.getNodeSet(),
                              context.getNavigator() );
         }
         else if ( size == 1 )
         {
             return evaluate( args.get(0),
                              context.getNavigator() );
         }
 
         throw new FunctionCallException( "string() requires one argument." );
     }
     
     public static String evaluate(Object obj,
                                   Navigator nav)
     {
         try
         {
             String retval = "";
             if (obj == null) {
                 return "";
             }
             if (obj instanceof List)
             {
                 List list = (List) obj;
                 if (list.isEmpty())
                 {
                     return "";
                 }
                 // do not recurse: only first list should unwrap
                 obj = list.get(0);
             }
             if (nav.isElement(obj) || nav.isDocument(obj))
             {
                 Iterator descendantAxisIterator = nav.getDescendantAxisIterator(obj);
                 StringBuffer sb = new StringBuffer();
                 while (descendantAxisIterator.hasNext())
                 {
                     Object descendant = descendantAxisIterator.next();
                     if (nav.isText(descendant))
                     {
                         sb.append(nav.getTextStringValue(descendant));
                     }
                 }
                 retval = sb.toString();
             }
             else if (nav.isAttribute(obj))
             {
                 retval = nav.getAttributeStringValue(obj);
             }
             else if (nav.isText(obj))
             {
                 retval = nav.getTextStringValue(obj);
             }
             else if (nav.isProcessingInstruction(obj))
             {
                 retval = nav.getProcessingInstructionData(obj);
             }
             else if (nav.isComment(obj))
             {
                 retval = nav.getCommentStringValue(obj);
             }
             else if (nav.isNamespace(obj))
             {
                 retval = nav.getNamespaceStringValue(obj);
             }
             else if (obj instanceof String)
             {
                 retval = (String) obj;
             }
             else if (obj instanceof Boolean)
             {
                 retval = stringValue(((Boolean) obj).booleanValue());
             }
             else if (obj instanceof Number)
             {
                 retval = stringValue(((Number) obj).doubleValue());
             }
             retval = retval == null ? "" : retval;
             return retval;
         }
         catch (UnsupportedAxisException e)
         {
             throw new JaxenRuntimeException(e);
         }
 
     }
 
     public static String stringValue(double value)
     {
         if (Double.isNaN(value))
         {
             return "NaN";
         }
         if (-0.0 == value || 0.0 == value)
         {
             return "0";
         }
         if (Double.isInfinite(value) && value < 0)
         {
             return "-Infinity";
         }
         if (Double.isInfinite(value) && value > 0)
         {
             return "Infinity";
         }
         if (((long) value) == value)
         {
             return Long.toString((long) value);
         }
         return Double.toString(value);
     }
 
     public static String stringValue(boolean bool)
     {
         return bool ? "true" : "false";
     }
 
 }
