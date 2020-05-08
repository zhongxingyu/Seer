 /*
  * Copyright (C) 2008 Herve Quiroz
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
 
 import com.google.common.collect.ImmutableSet;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.trancecode.xml.Location;
 import org.trancecode.xml.saxon.SaxonLocation;
 import org.trancecode.xproc.XProcException.Type;
 import org.trancecode.xproc.port.PortReference;
 import org.trancecode.xproc.step.Step;
 import org.trancecode.xproc.variable.Variable;
 
 import java.net.URI;
 
 /**
  * @author Herve Quiroz
  */
 public final class XProcExceptions
 {
     private XProcExceptions()
     {
         // No instantiation
     }
 
     public static XProcException xd0003(final Location location, final int numberOfNodes)
     {
         return newXProcException(Type.DYNAMIC, 3, location,
                 "the viewport source does not provide exactly one document but %s", numberOfNodes);
     }
 
     public static XProcException xd0004(final Location location)
     {
         return newXProcException(Type.DYNAMIC, 4, location,
                 "no subpipeline is selected by the p:choose and no default is provided");
     }
 
     public static XProcException xd0006(final Location location, final PortReference port)
     {
         return newXProcException(Type.DYNAMIC, 6, location, "wrong number of input documents in port %s", port);
     }
 
     public static XProcException xd0010(final XdmNode node)
     {
         return newXProcException(Type.DYNAMIC, 10, SaxonLocation.of(node),
                 "the match expression on p:viewport does not match an element or document: %s", node.getNodeKind());
     }
 
     public static XProcException xd0011(final Location location, final String resource, final Exception error)
     {
         final XProcException exception = newXProcException(Type.DYNAMIC, 11, location, "cannot load document: %s",
                 resource);
         exception.initCause(error);
         return exception;
     }
 
     public static XProcException xd0023(final Location location, final String select, final String errorMessage)
     {
         return newXProcException(Type.DYNAMIC, 23, location, "XPath expression cannot be evaluated: %s\n%s", select,
                 errorMessage);
     }
 
     public static XProcException xd0034(final Location location)
     {
         return newXProcException(
                 Type.DYNAMIC,
                 34,
                 location,
                 "It is a dynamic error to specify a new namespace or prefix if the lexical value of the specified name contains a colon (or if no wrapper is explicitly specified).");
     }
 
     public static XProcException xs0018(final Variable option)
     {
         return newXProcException(Type.STATIC, 18, option.getLocation(), "Option %s is required and is missing a value",
                 option.getName());
     }
 
     public static XProcException xs0031(final Location location, final QName optionName, final QName stepType)
     {
         return newXProcException(Type.STATIC, 31, location, "Option %s is not declared on this step type (%s)",
                 optionName, stepType);
     }
 
     public static XProcException xs0044(final XdmNode element)
     {
         return newXProcException(Type.STATIC, 44, SaxonLocation.of(element), "Unsupported element: %s",
                 element.getNodeName());
     }
 
     public static XProcException xs0052(final Location location, final URI uri)
     {
         return newXProcException(
                 Type.STATIC,
                 52,
                 location,
                 "%s cannot be retrieved or if, once retrieved, it does not point to a p:library, p:declare-step, or p:pipeline",
                 uri);
     }
 
    public static XProcException xc0010(final XdmNode node)
     {
         return newXProcException(Type.STEP, 10, SaxonLocation.of(node), "It is a dynamic error if an encoding of base64 is specified and the character set is not specified or if the specified character set is not supported by the implementation.");
     }
 
     public static XProcException xc0013(final XdmNode node)
     {
         return newXProcException(Type.STEP, 13, SaxonLocation.of(node), "It is a dynamic error if the pattern matches a processing instruction and the new name has a non-null namespace.");
     }
 
     public static XProcException xc0019(final Step step)
     {
         return newXProcException(Type.STEP, 19, step.getLocation(), "Documents are not equal in step %s",
                 step.getName());
     }
 
     public static XProcException xc0023(final XdmNode node, final XdmNodeKind... allowedNodeTypes)
     {
         return xc0023(node, ImmutableSet.copyOf(allowedNodeTypes));
     }
 
     public static XProcException xc0023(final XdmNode node, final Iterable<XdmNodeKind> allowedNodeTypes)
     {
         return newXProcException(Type.STEP, 23, SaxonLocation.of(node),
                 "Selected node type %s is not allowed by the step ; allowed types: %s ", node.getNodeKind(),
                 allowedNodeTypes);
     }
 
     public static XProcException xc0025(final Location location, final XdmNode node, final String position)
     {
         return newXProcException(Type.STEP, 25, location,
                 "the match pattern matches a %s which is not allowed when the position is %s", node.getNodeKind(),
                 position);
     }

    public static XProcException xc0027(final Location location)
    {
        return newXProcException(Type.STEP, 27, location,
                "It is a dynamic error if the document is not valid or the step doesn't support DTD validation.");
    }

     public static XProcException xc0038(final Location location, final String version)
     {
         return newXProcException(Type.STEP, 38, location, "XSLT version %s not supported", version);
     }
 
     public static XProcException xc0051(final Location location)
     {
         return newXProcException(Type.STEP, 51, location, "it is a dynamic error if the content-type specified is not supported by the implementation.");
     }
 
     public static XProcException xc0058(final Location location)
     {
         return newXProcException(Type.STEP, 58, location, "It is a dynamic error if the all and relative options are both true.");
     }
 
     public static XProcException xc0059(final Location location)
     {
         return newXProcException(
                 Type.STEP,
                 59,
                 location,
                 "It is a dynamic error if the QName value in the attribute-name option uses the prefix \"xmlns\" or any other prefix that resolves to the namespace name \"http://www.w3.org/2000/xmlns/\".");
     }
 
     public static XProcException xc0060(final Location location)
     {
         return newXProcException(
                 Type.STEP, 60, location,
                 "It is a dynamic error if the processor does not support the specified version of the UUID algorithm.");
     }
 
     public static XProcException xc0062(final Location location, final XdmNode node)
     {
         return newXProcException(Type.STEP, 62, location, "the match option matches a namespace node: %s",
                 node.getNodeName());
     }
 
     private static XProcException newXProcException(final Type type, final int code, final Location location,
             final String message, final Object... parameters)
     {
         final XProcException exception = new XProcException(type, code, location, message, parameters);
         final StackTraceElement[] stackTrace = new StackTraceElement[exception.getStackTrace().length - 1];
         System.arraycopy(exception.getStackTrace(), 1, stackTrace, 0, stackTrace.length);
         exception.setStackTrace(stackTrace);
         return exception;
     }
 }
