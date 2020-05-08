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
 package org.trancecode.xproc;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
 
 import java.net.URI;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import net.sf.saxon.s9api.QName;
 import org.trancecode.collection.TcMaps;
 import org.trancecode.collection.TcSets;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.step.Step;
 
 /**
  * @author Herve Quiroz
  */
 public final class PipelineLibrary
 {
     private static final Logger LOG = Logger.getLogger(PipelineLibrary.class);
 
     private final URI baseUri;
     private final Map<QName, Step> steps;
     private final Set<URI> importedUris;
 
     public PipelineLibrary(final URI baseUri, final Map<QName, Step> steps, final Set<URI> importedUris)
     {
         LOG.trace("baseUri = {} ; steps = {keys} ; importedUris = {}", baseUri, steps, importedUris);
         this.baseUri = Preconditions.checkNotNull(baseUri);
         this.steps = ImmutableMap.copyOf(steps);
        this.importedUris = ImmutableSet.copyOf(importedUris);
     }
 
     public URI baseUri()
     {
         return baseUri;
     }
 
     public Set<URI> importedUris()
     {
         return importedUris;
     }
 
     public Set<QName> stepTypes()
     {
         return steps.keySet();
     }
 
     public Step newStep(final QName type)
     {
         LOG.trace("{@method} type = {}", type);
 
         final Step step = steps.get(Preconditions.checkNotNull(type));
         if (step == null)
         {
             // TODO use some XProc error here
             throw new NoSuchElementException(type.toString());
         }
         return step;
     }
 
     public PipelineLibrary importLibraries(final Iterable<PipelineLibrary> libraries)
     {
         PipelineLibrary library = this;
         for (final PipelineLibrary importedLibrary : libraries)
         {
             library = library.importLibrary(importedLibrary);
         }
 
         return library;
     }
 
     public PipelineLibrary importLibrary(final PipelineLibrary library)
     {
         Preconditions.checkNotNull(library);
         if (baseUri.equals(library.baseUri()) || importedUris.contains(library.baseUri))
         {
             return this;
         }
 
         final Map<QName, Step> mergedSteps = TcMaps.merge(library.steps, steps);
         final Set<URI> mergedImportedUris = TcSets.immutableSet(importedUris, library.importedUris);
         return new PipelineLibrary(baseUri, mergedSteps, mergedImportedUris);
     }
 }
