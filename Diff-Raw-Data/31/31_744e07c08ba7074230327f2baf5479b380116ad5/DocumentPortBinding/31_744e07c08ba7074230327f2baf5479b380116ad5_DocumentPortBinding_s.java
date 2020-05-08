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
 package org.trancecode.xproc.binding;
 
 import com.google.common.collect.ImmutableList;
 
 import javax.xml.transform.Source;
 
 import net.sf.saxon.s9api.XdmNode;
 import org.trancecode.api.Immutable;
 import org.trancecode.xml.Location;
 import org.trancecode.xproc.Environment;
import org.trancecode.xproc.PipelineException;
 
 /**
  * @author Herve Quiroz
  */
 @Immutable
 public class DocumentPortBinding extends AbstractPortBinding
 {
     private final String href;
 
     // TODO cache support
 
     public DocumentPortBinding(final String href, final Location location)
     {
         super(location);

        this.href = href;
    }

    public Iterable<XdmNode> readNodes()
    {
        throw new IllegalStateException();
     }
 
     @Override
     public EnvironmentPortBinding newEnvironmentPortBinding(final Environment environment)
     {
         return new AbstractEnvironmentPortBinding(location)
         {
             public Iterable<XdmNode> readNodes()
             {
                 try
                 {
                     final Source source = environment.getPipelineContext().getUriResolver()
                             .resolve(href, location.getSystemId());

                    return ImmutableList.of(environment.getPipelineContext().getProcessor().newDocumentBuilder()
                            .build(source));
                 }
                 catch (final Exception e)
                 {
                    throw new PipelineException("Error while trying to build document ; href = %s", e, href);
                 }
             }
         };
     }
 
     @Override
     public String toString()
     {
         return String.format("%s[%s]", getClass().getSimpleName(), href);
     }
 }
