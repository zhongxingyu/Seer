 /*
  * Copyright (C) 2010 TranceCode Software
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
  */
 package org.trancecode.xproc.xpath;
 
import com.beust.jcommander.internal.Maps;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 
 import java.util.Locale;
 import java.util.Map;
 
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.functions.ExtensionFunctionCall;
 import net.sf.saxon.functions.ExtensionFunctionDefinition;
 import net.sf.saxon.om.SequenceIterator;
 import net.sf.saxon.om.SingletonIterator;
 import net.sf.saxon.om.StructuredQName;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.value.SequenceType;
 import net.sf.saxon.value.StringValue;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.Tubular;
 import org.trancecode.xproc.XPathExtensionFunction;
 import org.trancecode.xproc.XProcXmlModel;
 
 /**
  * {@code p:system-property()}.
  * 
  * @author Herve Quiroz
  * @see <a href="http://www.w3.org/TR/xproc/#f.system-property">System
  *      Properties</a>
  */
 public final class SystemPropertyXPathExtensionFunction implements XPathExtensionFunction
 {
     private static final Logger LOG = Logger.getLogger(SystemPropertyXPathExtensionFunction.class);
     private static final Map<String, String> PROPERTIES;
 
     static
     {
         final Map<String, String> properties = Maps.newHashMap();
         // TODO p:episode
         properties.put("p:episode", "123");
         properties.put("p:language", Locale.getDefault().toString());
         properties.put("p:product-name", Tubular.productName());
         properties.put("p:product-version", Tubular.version());
         properties.put("p:vendor", Tubular.vendor());
         properties.put("p:vendor-uri", Tubular.vendorUri());
         properties.put("p:version", Tubular.xprocVersion());
         properties.put("p:xpath-version", Tubular.xpathVersion());
         properties.put("p:psvi-supported", "false");
         PROPERTIES = ImmutableMap.copyOf(properties);
     }
 
     @Override
     public ExtensionFunctionDefinition getExtensionFunctionDefinition()
     {
         return new ExtensionFunctionDefinition()
         {
             private static final long serialVersionUID = -2376250179411225176L;
 
             @Override
             public StructuredQName getFunctionQName()
             {
                 return XProcXmlModel.xprocNamespace().newStructuredQName("system-property");
             }
 
             @Override
             public int getMinimumNumberOfArguments()
             {
                 return 1;
             }
 
             @Override
             public SequenceType[] getArgumentTypes()
             {
                 return new SequenceType[] { SequenceType.SINGLE_STRING };
             }
 
             @Override
             public SequenceType getResultType(final SequenceType[] suppliedArgumentTypes)
             {
                 return SequenceType.SINGLE_STRING;
             }
 
             @Override
             public ExtensionFunctionCall makeCallExpression()
             {
                 return new ExtensionFunctionCall()
                 {
                     private static final long serialVersionUID = -8363336682570398286L;
 
                     @Override
                     public SequenceIterator call(final SequenceIterator[] arguments, final XPathContext context)
                             throws XPathException
                     {
                         Preconditions.checkArgument(arguments.length == 1);
                         final String property = arguments[0].next().getStringValue();
                         final String value;
                         if (PROPERTIES.containsKey(property))
                         {
                             value = PROPERTIES.get(property);
                         }
                         else
                         {
                             value = "";
                         }
                         LOG.trace("{} = {}", property, value);
                         return SingletonIterator.makeIterator(StringValue.makeStringValue(value));
                     }
                 };
             }
         };
     }
 
 }
