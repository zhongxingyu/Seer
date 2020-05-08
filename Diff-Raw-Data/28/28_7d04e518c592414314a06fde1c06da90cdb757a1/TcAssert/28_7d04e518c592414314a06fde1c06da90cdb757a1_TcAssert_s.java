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
 package org.trancecode;
 
 import com.google.common.collect.ImmutableList;
 import junit.framework.AssertionFailedError;
 import net.sf.saxon.dom.NodeOverNodeInfo;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.XdmNode;
 import org.custommonkey.xmlunit.XMLAssert;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.testng.Assert;
 import org.trancecode.xml.saxon.Saxon;
 import org.w3c.dom.Document;
 
 /**
  * {@link Assert} extensions.
  * 
  * @author Herve Quiroz
  */
 public final class TcAssert
 {
     public static <T> void assertIterableEquals(final Iterable<T> actual, final Iterable<T> expected)
     {
         Assert.assertEquals(ImmutableList.copyOf(actual), ImmutableList.copyOf(expected));
     }
 
     public static void assertEquals(final XdmNode expected, final XdmNode actual)
     {
         assertEquals(expected, actual, true);
     }
 
     public static void assertEquals(final XdmNode expected, final XdmNode actual, final boolean ignoreWhitespace)
     {
         assert expected != null;
         assert actual != null;
         final Processor processor = (Processor) expected.getUnderlyingNode().getConfiguration().getProcessor();
         final XdmNode docExpected = Saxon.asDocumentNode(expected, processor);
         final XdmNode docActual = Saxon.asDocumentNode(actual, processor);
         XMLUnit.setIgnoreWhitespace(ignoreWhitespace);
         try
         {
             XMLAssert.assertXMLEqual((Document) NodeOverNodeInfo.wrap(docExpected.getUnderlyingNode()),
                     (Document) NodeOverNodeInfo.wrap(docActual.getUnderlyingNode()));
         }
         catch (final AssertionFailedError e)
         {
            Assert.fail(String.format("expected:\n%s\nactual:\n%s", docExpected, docActual), e);
         }
     }
 }
