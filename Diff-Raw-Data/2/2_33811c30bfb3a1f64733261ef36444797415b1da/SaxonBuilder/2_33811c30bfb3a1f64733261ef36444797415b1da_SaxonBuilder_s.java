 /*
  * Copyright (C) 2010 Romain Deltour
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
 package org.trancecode.xml.saxon;
 
 import com.google.common.collect.ImmutableList;
 import net.sf.saxon.Configuration;
 import net.sf.saxon.event.Receiver;
 import net.sf.saxon.event.TreeReceiver;
 import net.sf.saxon.om.NamePool;
 import net.sf.saxon.om.NamespaceIterator;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XdmDestination;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.trans.XPathException;
 
 /**
  * A builder to create new XdmNode documents using a push API. It provides a
  * facade to the lower-level Saxon Receiver API.
  * 
  * @see Receiver
  * @see TreeReceiver
  * @author Romain Deltour
  */
 public class SaxonBuilder
 {
     private final XdmDestination destination = new XdmDestination();
     private final TreeReceiver receiver;
     private final NamePool namePool;
 
     /**
      * Creates a new builder based on the given Saxon configuration.
      * 
      * @param configuration
      *            The Saxon configuration. In particular, the name pool of the
      *            configuration will be used to construct the new item codes.
      */
     public SaxonBuilder(final Configuration configuration)
     {
         try
         {
             receiver = new TreeReceiver(destination.getReceiver(configuration));
             receiver.setPipelineConfiguration(configuration.makePipelineConfiguration());
             namePool = configuration.getNamePool();
             receiver.open();
         }
         catch (final SaxonApiException e)
         {
             throw new IllegalStateException(e);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Starts a document node.
      */
     public void startDocument()
     {
         try
         {
             receiver.startDocument(0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Ends the document node.
      */
     public void endDocument()
     {
         try
         {
             receiver.endDocument();
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Starts a new element with the given QName.
      * 
      * @param qname
      *            The QName of the new element.
      */
     public void startElement(final QName qname)
     {
         try
         {
             final int nameCode = namePool.allocate(qname.getPrefix(), qname.getNamespaceURI(), qname.getLocalName());
             receiver.startElement(nameCode, -1, -1, 0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Start a new element with the given QName, and adds the in-scope
      * namespaces of the given node to the new element.
      * 
      * @param qname
      *            The QName of the new element.
      * @param nsContext
      *            A node whose in-scope namespaces are copied to the new
      *            element.
      */
     public void startElement(final QName qname, final XdmNode nsContext)
     {
         try
         {
             startElement(qname);
             final int[] inscopeNsCodes = NamespaceIterator.getInScopeNamespaceCodes(nsContext.getUnderlyingNode());
             for (final int nsCode : inscopeNsCodes)
             {
                 receiver.namespace(nsCode, 0);
             }
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Ends the current element node.
      */
     public void endElement()
     {
         try
         {
             receiver.endElement();
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Starts the content of the current element. Must be called after the
      * declaration of namespaces and attributes, before adding text or node
      * children to the element.
      */
     public void startContent()
     {
         try
         {
             receiver.startContent();
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Adds an attribute to the current element.
      * 
      * @param qname
      *            The QName of the attribute
      * @param value
      *            The value of the attribute
      */
     public void attribute(final QName qname, final String value)
     {
         try
         {
             final int nameCode = namePool.allocate(qname.getPrefix(), qname.getNamespaceURI(), qname.getLocalName());
             receiver.attribute(nameCode, -1, value, 0, 0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Adds a new comment node.
      * 
      * @param comment
      *            The comment text
      */
     public void comment(final String comment)
     {
         try
         {
            receiver.characters(comment, 0, 0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Declares a new namespace in the current element
      * 
      * @param prefix
      *            The namespace prefix
      * @param uri
      *            The namespace URI
      */
     public void namespace(final String prefix, final String uri)
     {
         try
         {
             final int nsCode = namePool.allocateNamespaceCode((prefix != null) ? prefix : "", uri);
             receiver.namespace(nsCode, 0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Appends the given nodes to the document being built.
      */
     public void nodes(final XdmNode... nodes)
     {
         nodes(ImmutableList.copyOf(nodes));
     }
 
     /**
      * Appends the given nodes to the document being built.
      */
     public void nodes(final Iterable<XdmNode> nodes)
     {
         try
         {
             for (final XdmNode node : nodes)
             {
                 receiver.append(node.getUnderlyingNode(), 0, NodeInfo.NO_NAMESPACES);
             }
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Adds a processing instruction.
      * 
      * @param name
      *            The processing instruction name
      * @param data
      *            The processing instruction data
      */
     public void processingInstruction(final String name, final String data)
     {
         try
         {
             receiver.processingInstruction(name, data, 0, 0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Adds a text node
      * 
      * @param text
      *            The text content
      */
     public void text(final String text)
     {
         try
         {
             receiver.characters(text, 0, 0);
         }
         catch (final XPathException e)
         {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Returns the result node built by this builder.
      */
     public XdmNode getNode()
     {
         return destination.getXdmNode();
     }
 }
