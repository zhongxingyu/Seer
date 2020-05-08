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
 package org.trancecode.xproc.port;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 
 import java.util.List;
 
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.XPathCompiler;
 import net.sf.saxon.s9api.XPathExecutable;
 import net.sf.saxon.s9api.XPathSelector;
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.Location;
 import org.trancecode.xml.saxon.Saxon;
 import org.trancecode.xml.saxon.SaxonLocation;
 import org.trancecode.xproc.Environment;
 import org.trancecode.xproc.XProcException;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.XProcXmlModel;
 import org.trancecode.xproc.binding.EnvironmentPortBinding;
 import org.trancecode.xproc.binding.PortBinding;
 
 /**
  * @author Herve Quiroz
  */
 public final class EnvironmentPort implements HasPortReference
 {
     private static final Logger LOG = Logger.getLogger(EnvironmentPort.class);
 
     private final Port declaredPort;
     private final List<EnvironmentPortBinding> portBindings;
     private final XPathExecutable select;
 
     public static EnvironmentPort newEnvironmentPort(final Port declaredPort, final Environment environment)
     {
         assert declaredPort != null;
         assert environment != null;
         LOG.trace("declaredPort = {}", declaredPort);
         LOG.trace("portBindings = {}", declaredPort.getPortBindings());
 
         final List<EnvironmentPortBinding> portBindings = ImmutableList.copyOf(Iterables.transform(
                 declaredPort.getPortBindings(), new Function<PortBinding, EnvironmentPortBinding>()
                 {
                     @Override
                     public EnvironmentPortBinding apply(final PortBinding portBinding)
                     {
                         return portBinding.newEnvironmentPortBinding(environment);
                     }
                 }));
 
         final String declaredPortSelect = declaredPort.getSelect();
         LOG.trace("declaredPortSelect = {}", declaredPortSelect);
         final XPathExecutable select;
         if (declaredPortSelect != null)
         {
             try
             {
                 final XPathCompiler xpathCompiler = environment.getPipelineContext().getProcessor().newXPathCompiler();
                 xpathCompiler.declareNamespace(XProcXmlModel.xprocNamespace().prefix(), XProcXmlModel.xprocNamespace()
                         .uri());
                 xpathCompiler.declareNamespace(XProcXmlModel.xprocStepNamespace().prefix(), XProcXmlModel
                         .xprocStepNamespace().uri());
                 select = xpathCompiler.compile(declaredPortSelect);
             }
             catch (final SaxonApiException e)
             {
                 final XProcException error = XProcExceptions.xd0023(declaredPort.getLocation(), declaredPortSelect,
                         e.getMessage());
                 error.initCause(e);
                 throw error;
             }
         }
         else
         {
             select = null;
         }
 
         return new EnvironmentPort(declaredPort, portBindings, select);
     }
 
     private EnvironmentPort(final Port declaredPort, final Iterable<EnvironmentPortBinding> portBindings,
             final XPathExecutable select)
     {
         this.declaredPort = declaredPort;
         this.portBindings = ImmutableList.copyOf(portBindings);
         this.select = select;
     }
 
     public final List<EnvironmentPortBinding> portBindings()
     {
         return portBindings;
     }
 
     public Port getDeclaredPort()
     {
         return declaredPort;
     }
 
     /**
      * Read without evaluating the 'select' attribute.
      */
     private Iterable<XdmNode> readRawNodes()
     {
         return Iterables.concat(Iterables.transform(portBindings,
                 new Function<EnvironmentPortBinding, Iterable<XdmNode>>()
                 {
                     @Override
                     public Iterable<XdmNode> apply(final EnvironmentPortBinding portBinding)
                     {
                         return portBinding.readNodes();
                     }
                 }));
     }
 
     private Iterable<XdmNode> select(final Iterable<XdmNode> nodes)
     {
        LOG.trace("{@method} select = {}", select);
 
         if (select == null)
         {
             return nodes;
         }
 
         return Iterables.concat(Iterables.transform(nodes, new Function<XdmNode, Iterable<XdmNode>>()
         {
             @Override
             public Iterable<XdmNode> apply(final XdmNode node)
             {
                 try
                 {
                     final XPathSelector selector = select.load();
                     selector.setContextItem(node);
                     return Iterables.filter(selector.evaluate(), XdmNode.class);
                 }
                 catch (final SaxonApiException e)
                 {
                     throw XProcExceptions.xd0023(declaredPort.getLocation(), declaredPort.getSelect(), e.getMessage());
                 }
             }
         }));
     }
 
     public Iterable<XdmNode> readNodes()
     {
         LOG.trace("{@method} declaredPort = {}", declaredPort);
 
         return ImmutableList.copyOf(select(readRawNodes()));
     }
 
     public EnvironmentPort writeNodes(final XdmNode... nodes)
     {
         return writeNodes(ImmutableList.copyOf(nodes));
     }
 
     public EnvironmentPort writeNodes(final Iterable<XdmNode> nodes)
     {
         assert portBindings.isEmpty();
 
         final List<XdmNode> nodeList = ImmutableList.copyOf(nodes);
         for (final XdmNode aNode : nodeList)
         {
             if (!Saxon.isDocument(aNode))
             {
                 throw XProcExceptions.xd0001(SaxonLocation.of(aNode));
             }
         }
         LOG.trace("{} nodes -> {}", nodeList.size(), declaredPort.getPortReference());
         final EnvironmentPortBinding portBinding = new EnvironmentPortBinding()
         {
             public Iterable<XdmNode> readNodes()
             {
                 return nodeList;
             }
 
             @Override
             public Location getLocation()
             {
                 return declaredPort.getLocation();
             }
         };
 
         return new EnvironmentPort(declaredPort, ImmutableList.of(portBinding), select);
     }
 
     public EnvironmentPort pipe(final EnvironmentPort port)
     {
         assert port != null : getDeclaredPort();
         assert port != this : getDeclaredPort();
         LOG.trace("{@method} {} -> {}", port.getDeclaredPort(), getDeclaredPort());
 
         final EnvironmentPortBinding portBinding = new EnvironmentPortBinding()
         {
             public Iterable<XdmNode> readNodes()
             {
                 LOG.trace("{@method} port = {} ; pipe = {}", EnvironmentPort.this, port);
                 return port.readNodes();
             }
 
             @Override
             public Location getLocation()
             {
                 return declaredPort.getLocation();
             }
         };
 
         return new EnvironmentPort(declaredPort, ImmutableList.of(portBinding), select);
     }
 
     @Override
     public PortReference getPortReference()
     {
         return getDeclaredPort().getPortReference();
     }
 
     @Override
     public String toString()
     {
         return declaredPort.toString();
     }
 }
