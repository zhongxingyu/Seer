 /*
  * Copyright (C) 2008 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xproc;
 
 import net.sf.saxon.s9api.QName;
 import org.trancecode.base.BaseException;
 import org.trancecode.xml.Location;
 import org.trancecode.xml.Namespace;
 
 /**
  * @author Herve Quiroz
  */
 public final class XProcException extends BaseException
 {
     private static final long serialVersionUID = 4809656109440340746L;
 
     private static final Namespace NAMESPACE = new Namespace("http://www.w3.org/ns/xproc-error", "err");
 
     public static Namespace namespace()
     {
         return NAMESPACE;
     }
 
     public static enum Type {
         STATIC
         {
             @Override
             public String toString()
             {
                 return "XS";
             }
         },
         DYNAMIC
         {
             @Override
             public String toString()
             {
                return "XD";
             }
         },
         STEP
         {
             @Override
             public String toString()
             {
                 return "XC";
             }
         }
     };
 
     private final Type type;
     private final int code;
     private final Location location;
     private final QName name;
 
     public XProcException(final Type type, final int code, final Location location, final String message,
             final Object... parameters)
     {
         this(namespace().newSaxonQName(getLabel(type, code)), type, code, location, message, parameters);
     }
 
     public XProcException(final QName name, final Type type, final Location location, final String message,
             final Object... parameters)
     {
         this(name, type, 0, location, message, parameters);
     }
 
     private XProcException(final QName name, final Type type, final int code, final Location location,
             final String message, final Object... parameters)
     {
         super(message, parameters);
 
         this.type = type;
         this.code = code;
         this.location = location;
         this.name = name;
     }
 
     public QName getName()
     {
         return name;
     }
 
     public int getCode()
     {
         return code;
     }
 
     public Location getLocation()
     {
         return location;
     }
 
     private static String getLabel(final Type type, final int code)
     {
         final StringBuilder buffer = new StringBuilder();
         buffer.append(code);
         while (buffer.length() < 4)
         {
             buffer.insert(0, "0");
         }
         buffer.insert(0, type);
 
         return buffer.toString();
     }
 
     public String getLabel()
     {
         return getLabel(type, code);
     }
 
     @Override
     public String getMessage()
     {
         return getLabel() + ": " + super.getMessage();
     }
 }
