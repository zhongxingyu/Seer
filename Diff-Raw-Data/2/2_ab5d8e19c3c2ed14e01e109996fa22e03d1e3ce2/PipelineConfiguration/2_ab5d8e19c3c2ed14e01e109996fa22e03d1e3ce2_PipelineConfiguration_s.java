 /*
  * Copyright (C) 2007 TranceCode Software
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
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.ServiceLoader;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.stream.StreamSource;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import org.trancecode.concurrent.TaskExecutor;
 import org.trancecode.concurrent.TaskExecutors;
 import org.trancecode.io.DefaultInputResolver;
 import org.trancecode.io.DefaultOutputResolver;
 import org.trancecode.io.InputResolver;
 import org.trancecode.io.OutputResolver;
 import org.trancecode.logging.Logger;
 import org.trancecode.xproc.step.CoreStepProcessor;
 import org.trancecode.xproc.step.Step;
 import org.trancecode.xproc.step.StepProcessor;
 import org.trancecode.xproc.step.XProcSteps;
 import org.trancecode.xproc.xpath.XPathExtensionFunction;
 
 /**
  * @author Herve Quiroz
  */
 public final class PipelineConfiguration implements PipelineContext
 {
     private static final String RESOURCE_PATH_XPROC_LIBRARY_1_0 = "/org/trancecode/xproc/xproc-1.0.xpl";
     private static final Map<QName, StepProcessor> DEFAULT_STEP_PROCESSORS = getDefaultStepProcessors();
     private static final Map<QName, Step> CORE_LIBRARY = getCoreLibrary();
     private static final Iterable<XPathExtensionFunction> EXTENSION_FUNCTIONS = ImmutableList.copyOf(ServiceLoader
             .load(XPathExtensionFunction.class));
     private static final URI DEFAULT_LIBRARY_URI = URI.create("trancecode:tubular:default-library.xpl");
     private static final Set<URI> EMPTY_SET_OF_URIS = ImmutableSet.of();
     private static final Logger LOG = Logger.getLogger(PipelineConfiguration.class);
 
     private static PipelineLibrary DEFAULT_PIPELINE_LIBRARY = getDefaultLPipelineLibrary();
 
     private TaskExecutor executor = TaskExecutors.onDemandExecutor();
     private URIResolver uriResolver;
     private OutputResolver outputResolver = DefaultOutputResolver.INSTANCE;
     private InputResolver inputResolver = DefaultInputResolver.INSTANCE;
     private PipelineLibrary library = DEFAULT_PIPELINE_LIBRARY;
     private final Processor processor;
     private final Map<QName, StepProcessor> stepProcessors = Maps.newHashMap(DEFAULT_STEP_PROCESSORS);
 
     private static PipelineLibrary getDefaultLPipelineLibrary()
     {
         final PipelineContext context = new PipelineContext()
         {
             private final Processor processor = new Processor(false);
             private final PipelineLibrary library = new PipelineLibrary(DEFAULT_LIBRARY_URI, CORE_LIBRARY,
                     EMPTY_SET_OF_URIS);
 
             @Override
             public TaskExecutor getExecutor()
             {
                 return TaskExecutors.onDemandExecutor();
             }
 
             @Override
             public InputResolver getInputResolver()
             {
                 return null;
             }
 
             @Override
             public OutputResolver getOutputResolver()
             {
                 return null;
             }
 
             @Override
             public Processor getProcessor()
             {
                 return processor;
             }
 
             @Override
             public URIResolver getUriResolver()
             {
                 return processor.getUnderlyingConfiguration().getURIResolver();
             }
 
             @Override
             public StepProcessor getStepProcessor(final QName step)
             {
                 return DEFAULT_STEP_PROCESSORS.get(step);
             }
 
             @Override
             public Map<QName, StepProcessor> getStepProcessors()
             {
                 return DEFAULT_STEP_PROCESSORS;
             }
 
             @Override
             public PipelineLibrary getPipelineLibrary()
             {
                 return library;
             }
         };
         final URL xprocLibraryUrl = PipelineConfiguration.class.getResource(RESOURCE_PATH_XPROC_LIBRARY_1_0);
         final Source defaultLibrarySource;
         try
         {
             defaultLibrarySource = new StreamSource(xprocLibraryUrl.openStream(), xprocLibraryUrl.toString());
         }
         catch (final IOException e)
         {
             throw new IllegalStateException("cannot parse default library: " + xprocLibraryUrl, e);
         }
         final PipelineLibrary library = PipelineParser.parseLibrary(context, defaultLibrarySource);
         LOG.trace("supported steps: {}", library.getStepTypes());
         return library;
     }
 
     private static Map<QName, StepProcessor> getDefaultStepProcessors()
     {
         final Map<QName, StepProcessor> processors = Maps.newHashMap();
         final Map<QName, StepProcessor> availableProcessors = Maps.uniqueIndex(ServiceLoader.load(StepProcessor.class),
                 new Function<StepProcessor, QName>()
                 {
                     @Override
                     public QName apply(final StepProcessor stepProcessor)
                     {
                         return stepProcessor.getStepType();
                     }
                 });
         processors.putAll(availableProcessors);
         for (final QName stepType : Iterables.concat(XProcSteps.REQUIRED_STEPS, XProcSteps.OPTIONAL_STEPS))
         {
             if (!processors.containsKey(stepType))
             {
                 processors.put(stepType, new StepProcessor()
                 {
                     @Override
                     public QName getStepType()
                     {
                         return stepType;
                     }
 
                     @Override
                     public Environment run(final Step step, final Environment environment)
                     {
                        throw new UnsupportedOperationException(stepType.toString());
                     }
                 });
             }
         }
 
         return ImmutableMap.copyOf(processors);
     }
 
     private static Map<QName, Step> getCoreLibrary()
     {
         final Map<QName, Step> coreSteps = Maps.newHashMap();
         for (final CoreStepProcessor coreStepProcessor : ServiceLoader.load(CoreStepProcessor.class))
         {
             coreSteps.put(coreStepProcessor.getStepType(), coreStepProcessor.getStepDeclaration());
         }
 
         return ImmutableMap.copyOf(coreSteps);
     }
 
     public PipelineConfiguration()
     {
         this(new Processor(false));
     }
 
     public PipelineConfiguration(final Processor processor)
     {
         this.processor = Preconditions.checkNotNull(processor);
         uriResolver = processor.getUnderlyingConfiguration().getURIResolver();
         for (final XPathExtensionFunction function : EXTENSION_FUNCTIONS)
         {
             LOG.trace("register XPath extension function: {}", function);
             processor.registerExtensionFunction(function.getExtensionFunctionDefinition());
         }
     }
 
     @Override
     public TaskExecutor getExecutor()
     {
         return executor;
     }
 
     public void setExecutor(final TaskExecutor executor)
     {
         this.executor = executor;
     }
 
     public void setExecutor(final ExecutorService executor)
     {
         this.executor = TaskExecutors.forExecutorService(executor);
     }
 
     @Override
     public InputResolver getInputResolver()
     {
         return this.inputResolver;
     }
 
     public void setInputResolver(final InputResolver inputResolver)
     {
         this.inputResolver = Preconditions.checkNotNull(inputResolver);
     }
 
     @Override
     public URIResolver getUriResolver()
     {
         return uriResolver;
     }
 
     public void setUriResolver(final URIResolver uriResolver)
     {
         this.uriResolver = Preconditions.checkNotNull(uriResolver);
     }
 
     @Override
     public OutputResolver getOutputResolver()
     {
         return this.outputResolver;
     }
 
     public void setOutputResolver(final OutputResolver outputResolver)
     {
         this.outputResolver = Preconditions.checkNotNull(outputResolver);
     }
 
     @Override
     public Processor getProcessor()
     {
         return processor;
     }
 
     public void registerStepProcessor(final StepProcessor stepProcessor)
     {
         Preconditions.checkNotNull(stepProcessor);
         stepProcessors.put(stepProcessor.getStepType(), stepProcessor);
     }
 
     public void registerPipelineLibrary(final PipelineLibrary library)
     {
         Preconditions.checkNotNull(library);
         this.library = library.importLibrary(this.library);
     }
 
     @Override
     public PipelineLibrary getPipelineLibrary()
     {
         return library;
     }
 
     @Override
     public StepProcessor getStepProcessor(final QName step)
     {
         if (stepProcessors.containsKey(step))
         {
             return stepProcessors.get(step);
         }
 
         throw new NoSuchElementException(step.toString());
     }
 
     @Override
     public Map<QName, StepProcessor> getStepProcessors()
     {
         return Collections.unmodifiableMap(stepProcessors);
     }
 }
