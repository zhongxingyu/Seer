 package org.springframework.data.rest.webmvc;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.atomic.AtomicReference;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.servlet.http.HttpServletRequest;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactoryUtils;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.core.MethodParameter;
 import org.springframework.core.convert.ConversionFailedException;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.core.convert.TypeDescriptor;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.dao.OptimisticLockingFailureException;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.repository.CrudRepository;
 import org.springframework.data.repository.PagingAndSortingRepository;
 import org.springframework.data.repository.Repository;
 import org.springframework.data.rest.core.Handler;
 import org.springframework.data.rest.core.Link;
 import org.springframework.data.rest.core.LinkList;
 import org.springframework.data.rest.core.MapResource;
 import org.springframework.data.rest.core.Resource;
 import org.springframework.data.rest.core.ResourceLink;
 import org.springframework.data.rest.core.ResourceSet;
 import org.springframework.data.rest.core.convert.DelegatingConversionService;
 import org.springframework.data.rest.repository.AttributeMetadata;
 import org.springframework.data.rest.repository.EntityMetadata;
 import org.springframework.data.rest.repository.PageableResourceSet;
 import org.springframework.data.rest.repository.PagingMetadata;
 import org.springframework.data.rest.repository.RepositoryConstraintViolationException;
 import org.springframework.data.rest.repository.RepositoryExporter;
 import org.springframework.data.rest.repository.RepositoryExporterSupport;
 import org.springframework.data.rest.repository.RepositoryMetadata;
 import org.springframework.data.rest.repository.RepositoryNotFoundException;
 import org.springframework.data.rest.repository.UriToDomainObjectResolver;
 import org.springframework.data.rest.repository.annotation.RestResource;
 import org.springframework.data.rest.repository.context.AfterDeleteEvent;
 import org.springframework.data.rest.repository.context.AfterLinkDeleteEvent;
 import org.springframework.data.rest.repository.context.AfterLinkSaveEvent;
 import org.springframework.data.rest.repository.context.AfterSaveEvent;
 import org.springframework.data.rest.repository.context.BeforeDeleteEvent;
 import org.springframework.data.rest.repository.context.BeforeLinkDeleteEvent;
 import org.springframework.data.rest.repository.context.BeforeLinkSaveEvent;
 import org.springframework.data.rest.repository.context.BeforeRenderResourceEvent;
 import org.springframework.data.rest.repository.context.BeforeRenderResourcesEvent;
 import org.springframework.data.rest.repository.context.BeforeSaveEvent;
 import org.springframework.data.rest.repository.context.RepositoryEvent;
 import org.springframework.data.rest.repository.invoke.CrudMethod;
 import org.springframework.data.rest.repository.invoke.MethodParameterConversionService;
 import org.springframework.data.rest.repository.invoke.RepositoryQueryMethod;
 import org.springframework.format.support.DefaultFormattingConversionService;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpInputMessage;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.HttpOutputMessage;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.converter.HttpMessageConverter;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.http.converter.HttpMessageNotWritableException;
 import org.springframework.http.server.ServletServerHttpRequest;
 import org.springframework.util.Assert;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.util.UriComponentsBuilder;
 
 /**
  * Exports <a href="http://www.springsource.org/spring-data/">Spring Data Repositories</a> over the web in a RESTful
  * manner that is <a href="http://en.wikipedia.org/wiki/HATEOAS">HATEOAS</a> friendly.
  * <p/>
  * This controller can be deployed in it's own DispatcherServlet. In that case, use the {@link
  * RepositoryRestExporterServlet} in your web.xml. For example, to send all requests through the REST exporter, add the
  * following to your web.xml:
  * <p/>
  * <code><pre>&lt;servlet&gt;
  *   &lt;servlet-name&gt;exporter&lt;/servlet-name&gt;
  *   &lt;servlet-class&gt;org.springframework.data.rest.webmvc.RepositoryRestExporterServlet&lt;/servlet-class&gt;
  *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
  * &lt;/servlet&gt;
  * <p/>
  * &lt;servlet-mapping&gt;
  *   &lt;servlet-name&gt;exporter&lt;/servlet-name&gt;
  *   &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
  * &lt;/servlet-mapping&gt;
  * </pre></code>
  * <p/>
  * One can also deploy this controller into an existing Spring MVC application. In general, one should be able to
  * simply create an instance of the {@link RepositoryRestMvcConfiguration} bean in your <code>ApplicationContext</code>
  * or in JavaConfig.
  * <p/>
  * If you wish to alter the way the REST exporter functions, you don't configure the controller directly. Instead there
  * is a {@link RepositoryRestConfiguration} helper class that you create in your ApplicationContext. If a feature is
  * configurable in Spring Data REST, there is a property on this helper to configure it.
  *
  * @author Jon Brisbin
  */
 public class RepositoryRestController
     extends RepositoryExporterSupport<RepositoryRestController>
     implements ApplicationContextAware,
                InitializingBean {
 
   public static final  String           LOCATION          = "Location";
   public static final  String           SELF              = "self";
   final static         ThreadLocal<URI> BASE_URI          = new ThreadLocal<URI>();
   private static final Logger           LOG               = LoggerFactory.getLogger(RepositoryRestController.class);
   private static final TypeDescriptor   STRING_ARRAY_TYPE = TypeDescriptor.valueOf(String[].class);
 
   /**
    * We manage a list of possible {@link ConversionService}s to handle converting objects in the controller. This list
    * is prioritized as well, so one can add a ConversionService at index 0 to make sure that ConversionService takes
    * priority whenever an object of the type it can convert is needing conversion.
    */
   private DelegatingConversionService      conversionService                = new DelegatingConversionService(
       new DefaultFormattingConversionService()
   );
   private MethodParameterConversionService methodParameterConversionService = new MethodParameterConversionService(
       conversionService
   );
   /**
    * Converters for reading and writing representations of objects.
    */
   private List<HttpMessageConverter>       httpMessageConverters            = new ArrayList<HttpMessageConverter>();
   /**
    * List of {@link MediaType}s we can support, given the list of {@link HttpMessageConverter}s currently configured.
    */
   private SortedSet<String>                availableMediaTypes              = new TreeSet<String>();
   private RepositoryRestConfiguration      config                           = RepositoryRestConfiguration.DEFAULT;
   private ObjectMapper                     objectMapper                     = new ObjectMapper();
   private RepositoryAwareMappingHttpMessageConverter mappingHttpMessageConverter;
   private UriToDomainObjectResolver                  domainObjectResolver;
   private ApplicationContext                         applicationContext;
 
   {
     List<HttpMessageConverter> httpMessageConverters = new ArrayList<HttpMessageConverter>();
     httpMessageConverters.add(new UriListHttpMessageConverter());
 
     setHttpMessageConverters(httpMessageConverters);
   }
 
   @Override public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
     this.applicationContext = applicationContext;
   }
 
   /**
    * Get the {@link ConversionService} in use by the controller.
    *
    * @return The internal {@link ConversionService}s.
    */
   public ConversionService getConversionService() {
     return conversionService;
   }
 
   /**
    * Add these {@link ConversionService}s to the list of those being delegated to by the internal {@link
    * DelegatingConversionService}. Although this method does an 'add', it is called 'set' to make it JavaBean-friendly.
    *
    * @param conversionServices
    */
   @Autowired(required = false)
   public void setConversionServices(List<ConversionService> conversionServices) {
     if(null == conversionServices) {
       return;
     }
     Collections.reverse(conversionServices);
     if(null != conversionService) {
       this.conversionService.addConversionServices(
           conversionServices.toArray(new ConversionService[conversionServices.size()])
       );
     }
   }
 
   /**
    * @return The internal {@link ConversionService}.
    *
    * @see org.springframework.data.rest.webmvc.RepositoryRestController#getConversionService()
    */
   public ConversionService conversionService() {
     return conversionService;
   }
 
   /**
    * @param conversionServices
    *
    * @return @this
    *
    * @see RepositoryRestController#setConversionServices(java.util.List)
    */
   public RepositoryRestController conversionServices(List<ConversionService> conversionServices) {
     setConversionServices(conversionServices);
     return this;
   }
 
   /**
    * Get the list of default {@link HttpMessageConverter}s.
    *
    * @return Default converters.
    */
   public List<HttpMessageConverter> getHttpMessageConverters() {
     return httpMessageConverters;
   }
 
   /**
    * Set the list of available {@link HttpMessageConverter}s, clobbering the defaults. This does not, however, affect
    * those user-defined converters that come from the {@link RepositoryRestConfiguration}.
    *
    * @param httpMessageConverters
    */
   @SuppressWarnings({"unchecked"})
   public void setHttpMessageConverters(List<HttpMessageConverter> httpMessageConverters) {
     Assert.notNull(httpMessageConverters);
     this.httpMessageConverters = httpMessageConverters;
     this.availableMediaTypes.clear();
     for(HttpMessageConverter conv : httpMessageConverters) {
       for(MediaType mt : (List<MediaType>)conv.getSupportedMediaTypes()) {
         availableMediaTypes.add(mt.toString());
       }
     }
     for(HttpMessageConverter conv : config.getCustomConverters()) {
       for(MediaType mt : (List<MediaType>)conv.getSupportedMediaTypes()) {
         availableMediaTypes.add(mt.toString());
       }
     }
   }
 
   /**
    * @return @this
    *
    * @see org.springframework.data.rest.webmvc.RepositoryRestController#getHttpMessageConverters()
    */
   public List<HttpMessageConverter> httpMessageConverters() {
     return httpMessageConverters;
   }
 
   /**
    * @param httpMessageConverters
    *
    * @return @this
    *
    * @see RepositoryRestController#setHttpMessageConverters(java.util.List)
    */
   public RepositoryRestController httpMessageConverters(List<HttpMessageConverter> httpMessageConverters) {
     setHttpMessageConverters(httpMessageConverters);
     return this;
   }
 
   /**
    * Get the configuration currently in use.
    *
    * @return Either the user-defined configuration or a default.
    */
   public RepositoryRestConfiguration getRepositoryRestConfig() {
     return config;
   }
 
   /**
    * Set the configuration this controller will use to inflence its behavior.
    *
    * @param config
    *
    * @return @this
    */
   @Autowired(required = false)
   public RepositoryRestController setRepositoryRestConfig(RepositoryRestConfiguration config) {
     this.config = config;
     return this;
   }
 
   public RepositoryAwareMappingHttpMessageConverter getMappingHttpMessageConverter() {
     return mappingHttpMessageConverter;
   }
 
   @Autowired
   public RepositoryRestController setMappingHttpMessageConverter(RepositoryAwareMappingHttpMessageConverter mappingHttpMessageConverter) {
     this.mappingHttpMessageConverter = mappingHttpMessageConverter;
     httpMessageConverters.add(mappingHttpMessageConverter);
     this.objectMapper = mappingHttpMessageConverter.getObjectMapper();
     return this;
   }
 
   public UriToDomainObjectResolver getDomainObjectResolver() {
     return domainObjectResolver;
   }
 
   @Autowired
   public RepositoryRestController setDomainObjectResolver(UriToDomainObjectResolver domainObjectResolver) {
     this.domainObjectResolver = domainObjectResolver;
     return this;
   }
 
   @SuppressWarnings({"unchecked"})
   @Override public void afterPropertiesSet() throws Exception {
     for(ConversionService cs : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                                                                               ConversionService.class)
                                                .values()) {
       conversionService.addConversionServices(cs);
     }
   }
 
   /**
    * List available {@link CrudRepository}s that are being exported.
    *
    * @param request
    * @param uriBuilder
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/",
       method = RequestMethod.GET
   )
   @ResponseBody
   public ResponseEntity<?> listRepositories(ServletServerHttpRequest request,
                                             UriComponentsBuilder uriBuilder) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     ResourceSet resources = new ResourceSet();
     for(RepositoryExporter repoExporter : repositoryExporters) {
       for(String name : (Set<String>)repoExporter.repositoryNames()) {
         RepositoryMetadata repoMeta = repoExporter.repositoryMetadataFor(name);
         String rel = repoMeta.rel();
         URI path = buildUri(baseUri, name);
         resources.addLink(new ResourceLink(rel, path));
       }
     }
 
     // Publish an event that we're about to publish this ResourceSet
     publishEvent(new BeforeRenderResourcesEvent(request, null, resources));
     // Run any configured post processors
     for(ResourceSetPostProcessor pp : config.getResourceSetPostProcessors()) {
       resources = pp.postProcess(request, resources);
     }
 
     return negotiateResponse(request, HttpStatus.OK, new HttpHeaders(), resources);
   }
 
   /**
    * List entities of a {@link CrudRepository} by invoking
    * {@link org.springframework.data.repository.CrudRepository#findAll()}
    * and applying any available paging parameters.
    *
    * @param request
    * @param pageSort
    * @param uriBuilder
    * @param repository
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}",
       method = RequestMethod.GET
   )
   @ResponseBody
   public ResponseEntity<?> listEntities(ServletServerHttpRequest request,
                                         PagingAndSorting pageSort,
                                         UriComponentsBuilder uriBuilder,
                                         @PathVariable String repository) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.FIND_ALL)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
 
     Iterator allEntities = Collections.emptyList().iterator();
     ResourceSet resources;
     if(repoMeta.repository() instanceof PagingAndSortingRepository) {
       PageableResourceSet pr = new PageableResourceSet();
 
       Page page = ((PagingAndSortingRepository)repoMeta.repository()).findAll(pageSort);
       if(page.hasContent()) {
         allEntities = page.iterator();
       }
 
       // Set page counts in the response
       pr.setPaging(new PagingMetadata(page.getNumber() + 1,
                                       page.getSize(),
                                       page.getTotalPages(),
                                       page.getTotalElements()));
 
       // Copy over parameters
       UriComponentsBuilder selfUri = UriComponentsBuilder.fromUri(baseUri).pathSegment(repository);
       for(String name : request.getServletRequest().getParameterMap().keySet()) {
         if(notPagingParam(name)) {
           selfUri.queryParam(name, request.getServletRequest().getParameter(name));
         }
       }
 
       // Add next/prev links as necessary
       URI nextPrevBase = selfUri.build().toUri();
       maybeAddPrevNextLink(
           nextPrevBase,
           repoMeta,
           pageSort,
           page,
           !page.isFirstPage() && page.hasPreviousPage(),
           page.getNumber(),
           "prev",
           pr.getLinks()
       );
       maybeAddPrevNextLink(
           nextPrevBase,
           repoMeta,
           pageSort,
           page,
           !page.isLastPage() && page.hasNextPage(),
           page.getNumber() + 2,
           "next",
           pr.getLinks()
       );
 
       resources = pr;
     } else {
       Iterable it = repoMeta.repository().findAll();
       if(null != it) {
         allEntities = it.iterator();
       }
       resources = new ResourceSet();
     }
 
     while(allEntities.hasNext()) {
       Object o = allEntities.next();
       Serializable id = (Serializable)repoMeta.entityMetadata().idAttribute().get(o);
       if(shouldReturnLinks(request.getServletRequest().getHeader("Accept"))) {
         resources.addLink(new ResourceLink(repoMeta.rel() + "." + o.getClass().getSimpleName(),
                                            buildUri(baseUri, repository, id.toString())));
       } else {
         URI selfUri = buildUri(baseUri, repository, id.toString());
         MapResource res = createResource(repoMeta.rel(),
                                          o,
                                          repoMeta.entityMetadata(),
                                          selfUri);
         res.addLink(new ResourceLink(SELF, selfUri));
 
         resources.addResource(res);
       }
     }
 
     if(!repoMeta.queryMethods().isEmpty()) {
       resources.addLink(new ResourceLink(repoMeta.rel() + ".search",
                                          buildUri(baseUri, repository, "search")));
     }
 
     publishEvent(new BeforeRenderResourcesEvent(request, repoMeta, resources));
     // Run any configured post processors
     for(ResourceSetPostProcessor pp : config.getResourceSetPostProcessors()) {
       resources = pp.postProcess(request, resources);
     }
 
     return negotiateResponse(request, HttpStatus.OK, new HttpHeaders(), resources);
   }
 
   /**
    * List the URIs of query methods found on this repository interface.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/search",
       method = RequestMethod.GET
   )
   @ResponseBody
   public ResponseEntity<?> listQueryMethods(ServletServerHttpRequest request,
                                             UriComponentsBuilder uriBuilder,
                                             @PathVariable String repository) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     ResourceSet resources = new ResourceSet();
 
     for(Map.Entry<String, RepositoryQueryMethod> entry : ((Map<String, RepositoryQueryMethod>)repoMeta.queryMethods())
         .entrySet()) {
       URI baseSearchUri = buildUri(baseUri, repository, "search");
 
       // Check for customized rel and path
       Method m = entry.getValue().method();
       if(m.isAnnotationPresent(RestResource.class)) {
         RestResource resourceAnno = m.getAnnotation(RestResource.class);
         resources.addLink(new ResourceLink(
             (StringUtils.hasText(resourceAnno.rel())
              ? repoMeta.rel() + "." + resourceAnno.rel()
              : repoMeta.rel() + "." + entry.getKey()),
             buildUri(baseSearchUri,
                      (StringUtils.hasText(resourceAnno.path())
                       ? resourceAnno.path()
                       : entry.getKey()))
         ));
       } else {
         // No customizations, use the default
         resources.addLink(new ResourceLink(repoMeta.rel() + "." + entry.getKey(),
                                            buildUri(baseSearchUri, entry.getKey())));
       }
     }
 
     publishEvent(new BeforeRenderResourcesEvent(request, repoMeta, resources));
     // Run any configured post processors
     for(ResourceSetPostProcessor pp : config.getResourceSetPostProcessors()) {
       resources = pp.postProcess(request, resources);
     }
 
     return negotiateResponse(request, HttpStatus.OK, new HttpHeaders(), resources);
   }
 
   /**
    * Invoke a custom query method on a repository and page the results based on URL parameters supplied by the user or
    * the default page size.
    *
    * @param request
    * @param pageSort
    * @param uriBuilder
    * @param repository
    * @param query
    *
    * @return
    *
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/search/{query}",
       method = RequestMethod.GET
   )
   @ResponseBody
   public ResponseEntity<?> query(ServletServerHttpRequest request,
                                  PagingAndSorting pageSort,
                                  UriComponentsBuilder uriBuilder,
                                  @PathVariable String repository,
                                  @PathVariable String query) throws InvocationTargetException,
                                                                     IllegalAccessException,
                                                                     IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     Repository repo = repoMeta.repository();
     RepositoryQueryMethod queryMethod = repoMeta.queryMethod(query);
     if(null == queryMethod) {
       return notFoundResponse(request);
     }
 
     Class<?>[] paramTypes = queryMethod.paramTypes();
     String[] paramNames = queryMethod.paramNames();
     Object[] paramVals = new Object[paramTypes.length];
     for(int i = 0; i < paramVals.length; i++) {
       if(Pageable.class.isAssignableFrom(paramTypes[i])) {
         // Handle paging
         paramVals[i] = pageSort;
         continue;
       } else if(Sort.class.isAssignableFrom(paramTypes[i])) {
         // Handle sorting
         paramVals[i] = (null != pageSort ? pageSort.getSort() : null);
         continue;
       }
 
       String[] queryVals;
       if(null == (queryVals = request.getServletRequest().getParameterValues(paramNames[i]))) {
         continue;
       }
 
       MethodParameter methodParam = new MethodParameter(queryMethod.method(), i);
       String firstVal = (queryVals.length > 0 ? queryVals[0] : null);
       if(hasRepositoryMetadataFor(paramTypes[i])) {
         RepositoryMetadata paramRepoMeta = repositoryMetadataFor(paramTypes[i]);
         // Complex parameter is a managed type
         Serializable id = stringToSerializable(firstVal,
                                                (Class<Serializable>)paramRepoMeta.entityMetadata()
                                                                                  .idAttribute()
                                                                                  .type());
         Object o = paramRepoMeta.repository().findOne(id);
         if(null == o) {
           return notFoundResponse(request);
         }
 
         paramVals[i] = o;
       } else if(String.class.isAssignableFrom(paramTypes[i])) {
         // Param type is a String
         paramVals[i] = firstVal;
       } else if(methodParameterConversionService.canConvert(STRING_ARRAY_TYPE, methodParam)) {
         // There's a converter from String[] -> param type
         paramVals[i] = methodParameterConversionService.convert(queryVals, STRING_ARRAY_TYPE, methodParam);
       } else {
         // Param type isn't a "simple" type or no converter exists, try JSON
         try {
           paramVals[i] = objectMapper.readValue(firstVal, paramTypes[i]);
         } catch(IOException e) {
           throw new IllegalArgumentException(e);
         }
       }
     }
 
     Object result;
     if(null == (result = queryMethod.method().invoke(repo, paramVals))) {
       return negotiateResponse(request, HttpStatus.OK, new HttpHeaders(), new ResourceSet());
     }
 
     ResourceSet resources = new ResourceSet();
     Iterator entities = Collections.emptyList().iterator();
     if(result instanceof Collection) {
       entities = ((Collection)result).iterator();
     } else if(result instanceof Page) {
       Page page = (Page)result;
 
       if(page.hasContent()) {
         entities = page.iterator();
       }
 
       // Set page counts in the response
       PageableResourceSet pr = new PageableResourceSet();
       pr.setPaging(new PagingMetadata(page.getNumber() + 1,
                                       page.getSize(),
                                       page.getTotalPages(),
                                       page.getTotalElements()));
 
       // Copy over parameters
       UriComponentsBuilder selfUri = UriComponentsBuilder.fromUri(baseUri).pathSegment(repository, "search", query);
       for(String name : request.getServletRequest().getParameterMap().keySet()) {
         if(notPagingParam(name)) {
           selfUri.queryParam(name, request.getServletRequest().getParameter(name));
         }
       }
 
       // Add next/prev links as necessary
       URI nextPrevBase = selfUri.build().toUri();
       maybeAddPrevNextLink(
           nextPrevBase,
           repoMeta,
           pageSort,
           page,
           !page.isFirstPage() && page.hasPreviousPage(),
           page.getNumber(),
           "prev",
           pr.getLinks()
       );
       maybeAddPrevNextLink(
           nextPrevBase,
           repoMeta,
           pageSort,
           page,
           !page.isLastPage() && page.hasNextPage(),
           page.getNumber() + 2,
           "next",
           pr.getLinks()
       );
 
       resources = pr;
     } else {
       entities = Collections.singletonList(result).iterator();
     }
 
     while(entities.hasNext()) {
       Object obj = entities.next();
 
       RepositoryMetadata elemRepoMeta;
       if(null == (elemRepoMeta = repositoryMetadataFor(obj.getClass()))) {
         resources.addResource(new Resource(obj));
         continue;
       }
 
       // This object is managed by a repository
       String id = elemRepoMeta.entityMetadata().idAttribute().get(obj).toString();
       if(shouldReturnLinks(request.getServletRequest().getHeader("Accept"))) {
         String rel = elemRepoMeta.rel() + "." + elemRepoMeta.entityMetadata().type().getSimpleName();
         URI path = buildUri(baseUri, repository, id);
         resources.addLink(new ResourceLink(rel, path));
       } else {
         URI selfUri = buildUri(baseUri, repository, id);
         MapResource res = createResource(repoMeta.rel(),
                                          obj,
                                          repoMeta.entityMetadata(),
                                          selfUri);
         res.addLink(new ResourceLink(SELF, selfUri));
 
         resources.addResource(res);
       }
     }
 
     publishEvent(new BeforeRenderResourcesEvent(request, repoMeta, resources));
     // Run any configured post processors
     for(ResourceSetPostProcessor pp : config.getResourceSetPostProcessors()) {
       resources = pp.postProcess(request, resources);
     }
 
     return negotiateResponse(request, HttpStatus.OK, new HttpHeaders(), resources);
   }
 
   /**
    * Create a new entity by reading the incoming data and calling {@link CrudRepository#save(Object)} and letting the
    * ID be auto-generated.
    * <p/>
    * To get the entity back in the body of the response, simpy add the URL parameter <pre>returnBody=true</pre>.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}",
       method = RequestMethod.POST
   )
   @ResponseBody
   public ResponseEntity<?> create(ServletServerHttpRequest request,
                                   UriComponentsBuilder uriBuilder,
                                   @PathVariable String repository) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.SAVE_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     CrudRepository repo = repoMeta.repository();
 
     MediaType incomingMediaType = request.getHeaders().getContentType();
     Object incoming = readIncoming(request, incomingMediaType, repoMeta.entityMetadata().type());
     if(null == incoming) {
       throw new HttpMessageNotReadableException("Could not create an instance of " + repoMeta.entityMetadata()
                                                                                              .type()
                                                                                              .getSimpleName() + " from input.");
     }
 
     publishEvent(new BeforeSaveEvent(incoming));
     Object savedEntity = repo.save(incoming);
     publishEvent(new AfterSaveEvent(savedEntity));
 
     String sId = repoMeta.entityMetadata().idAttribute().get(savedEntity).toString();
     URI selfUri = buildUri(baseUri, repository, sId);
 
     HttpHeaders headers = new HttpHeaders();
     headers.set(LOCATION, selfUri.toString());
 
     Resource<?> body = null;
     if(returnBody(request)) {
       MapResource resource = createResource(repoMeta.rel(),
                                             savedEntity,
                                             repoMeta.entityMetadata(),
                                             selfUri);
       resource.addLink(new ResourceLink(SELF, selfUri));
 
       body = resource;
 
       publishEvent(new BeforeRenderResourceEvent(request, repoMeta, body));
       // Run any post-processors for this domain type
       for(ResourcePostProcessor pp : config.getResourcePostProcessors(repoMeta.domainType())) {
         body = pp.postProcess(request, body);
       }
     }
 
     return negotiateResponse(request, HttpStatus.CREATED, headers, body);
   }
 
   /**
    * Retrieve a specific entity.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    * @param id
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}",
       method = RequestMethod.GET
   )
   @ResponseBody
   public ResponseEntity<?> entity(ServletServerHttpRequest request,
                                   UriComponentsBuilder uriBuilder,
                                   @PathVariable String repository,
                                   @PathVariable String id) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.FIND_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     CrudRepository repo = repoMeta.repository();
     Object entity = repo.findOne(serId);
     if(null == entity) {
       return notFoundResponse(request);
     }
 
     HttpHeaders headers = new HttpHeaders();
     if(null != repoMeta.entityMetadata().versionAttribute()) {
       Object version = repoMeta.entityMetadata().versionAttribute().get(entity);
       if(null != version) {
         List<String> etags = request.getHeaders().getIfNoneMatch();
         for(String etag : etags) {
           if(("\"" + version.toString() + "\"").equals(etag)) {
             return negotiateResponse(request, HttpStatus.NOT_MODIFIED, new HttpHeaders(), null);
           }
         }
         headers.set("ETag", "\"" + version.toString() + "\"");
       }
     }
 
     URI selfUri = buildUri(baseUri, repository, id);
     Resource res = createResource(repoMeta.rel(),
                                   entity,
                                   repoMeta.entityMetadata(),
                                   selfUri);
     res.addLink(new ResourceLink(SELF, selfUri));
 
     publishEvent(new BeforeRenderResourceEvent(request, repoMeta, res));
     // Run any post-processors for this domain type
     for(ResourcePostProcessor pp : config.getResourcePostProcessors(repoMeta.domainType())) {
       res = pp.postProcess(request, res);
     }
 
     return negotiateResponse(request, HttpStatus.OK, headers, res);
   }
 
   /**
    * Create an entity with a specific ID or update an existing entity.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    * @param id
    *
    * @return
    *
    * @throws IOException
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}",
       method = {
           RequestMethod.PUT
       }
   )
   @ResponseBody
   public ResponseEntity<?> createOrUpdate(ServletServerHttpRequest request,
                                           UriComponentsBuilder uriBuilder,
                                           @PathVariable String repository,
                                           @PathVariable String id) throws IOException,
                                                                           IllegalAccessException,
                                                                           InstantiationException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.SAVE_ONE) || !repoMeta.exportsMethod(CrudMethod.FIND_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     CrudRepository repo = repoMeta.repository();
     Class<?> domainType = repoMeta.entityMetadata().type();
 
     MediaType incomingMediaType = request.getHeaders().getContentType();
     Object incoming;
     if(null == (incoming = readIncoming(request, incomingMediaType, domainType))) {
       throw new HttpMessageNotReadableException("Could not create an instance of "
                                                     + domainType.getSimpleName() + " from input.");
     }
     // Set the ID specified in the URL
     repoMeta.entityMetadata().idAttribute().set(serId, incoming);
 
     boolean isUpdate = false;
     Object entity;
     if(null != (entity = repo.findOne(serId))) {
       // Updating an existing resource
       isUpdate = true;
       for(AttributeMetadata attrMeta : (Collection<AttributeMetadata>)repoMeta.entityMetadata()
                                                                               .embeddedAttributes()
                                                                               .values()) {
         Object incomingVal;
         if(null != (incomingVal = attrMeta.get(incoming))) {
           attrMeta.set(incomingVal, entity);
         }
       }
     } else {
       entity = incoming;
     }
 
     publishEvent(new BeforeSaveEvent(entity));
     Object savedEntity = repo.save(entity);
     publishEvent(new AfterSaveEvent(savedEntity));
 
     URI selfUri = buildUri(baseUri, repository, id);
 
     Object body = null;
     if(returnBody(request)) {
       Resource res = createResource(repoMeta.rel(),
                                     savedEntity,
                                     repoMeta.entityMetadata(),
                                     selfUri);
       res.addLink(new ResourceLink(SELF, selfUri));
 
       body = res;
 
       publishEvent(new BeforeRenderResourceEvent(request, repoMeta, body));
       // Run any post-processors for this domain type
       for(ResourcePostProcessor pp : config.getResourcePostProcessors(repoMeta.domainType())) {
         res = pp.postProcess(request, res);
       }
     }
 
     if(!isUpdate) {
       HttpHeaders headers = new HttpHeaders();
       headers.set(LOCATION, selfUri.toString());
 
       return negotiateResponse(request,
                                HttpStatus.CREATED,
                                headers,
                                body);
     } else {
       return negotiateResponse(request,
                                (null != body ? HttpStatus.OK : HttpStatus.NO_CONTENT),
                                new HttpHeaders(),
                                body);
     }
 
   }
 
   /**
    * Delete an entity.
    *
    * @param request
    * @param repository
    * @param id
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}",
       method = RequestMethod.DELETE
   )
   @ResponseBody
   public ResponseEntity<?> deleteEntity(ServletServerHttpRequest request,
                                         @PathVariable String repository,
                                         @PathVariable String id) throws IOException {
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.DELETE_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     CrudRepository repo = repoMeta.repository();
     Object entity;
     if(null == (entity = repo.findOne(serId))) {
       return notFoundResponse(request);
     }
 
     publishEvent(new BeforeDeleteEvent(entity));
     repo.delete(serId);
     publishEvent(new AfterDeleteEvent(entity));
 
     return negotiateResponse(request, HttpStatus.NO_CONTENT, new HttpHeaders(), null);
   }
 
   /**
    * Retrieve the property of an entity.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    * @param id
    * @param property
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}/{property}",
       method = RequestMethod.GET
   )
   @ResponseBody
   public ResponseEntity<?> propertyOfEntity(ServletServerHttpRequest request,
                                             UriComponentsBuilder uriBuilder,
                                             @PathVariable String repository,
                                             @PathVariable String id,
                                             @PathVariable String property) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
     String accept = request.getServletRequest().getHeader("Accept");
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.FIND_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     CrudRepository repo = repoMeta.repository();
 
     Object entity;
     if(null == (entity = repo.findOne(serId))) {
       return notFoundResponse(request);
     }
 
     AttributeMetadata attrMeta;
     if(null == (attrMeta = repoMeta.entityMetadata().attribute(property))) {
       return notFoundResponse(request);
     }
 
     Class<?> attrType;
     if(null == (attrType = attrMeta.elementType())) {
       attrType = attrMeta.type();
     }
 
     RepositoryMetadata propRepoMeta = repositoryMetadataFor(attrType);
     if(!propRepoMeta.exportsMethod(CrudMethod.FIND_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
 
     Object propVal;
     if(null == (propVal = attrMeta.get(entity))) {
       return notFoundResponse(request);
     }
 
     Object body;
     AttributeMetadata idAttr = propRepoMeta.entityMetadata().idAttribute();
     String propertyRel = repository +
         "." + entity.getClass().getSimpleName() +
         "." + property;
     if(propVal instanceof Collection) {
       propertyRel += "." + propRepoMeta.entityMetadata().type().getSimpleName();
       ResourceSet resources = new ResourceSet();
       for(Object o : (Collection)propVal) {
         String propValId = idAttr.get(o).toString();
         URI path = buildUri(baseUri, repository, id, property, propValId);
 
         if(shouldReturnLinks(accept)) {
           resources.addLink(new ResourceLink(propertyRel, path));
         } else {
           URI selfUri = buildUri(baseUri, propRepoMeta.name(), propValId);
           MapResource res = createResource(propRepoMeta.rel(),
                                            o,
                                            propRepoMeta.entityMetadata(),
                                            selfUri);
           res.addLink(new ResourceLink(SELF, selfUri));
           res.addLink(new ResourceLink(propertyRel, path));
           resources.addResource(res);
         }
       }
       body = resources;
       // Run any post-processors for this domain type
       for(ResourceSetPostProcessor pp : config.getResourceSetPostProcessors()) {
         resources = pp.postProcess(request, resources);
       }
     } else if(propVal instanceof Map) {
       propertyRel += "." + propRepoMeta.entityMetadata().type().getSimpleName();
       Map resource = new HashMap();
       for(Map.Entry<Object, Object> entry : ((Map<Object, Object>)propVal).entrySet()) {
         String propValId = idAttr.get(entry.getValue()).toString();
         URI path = buildUri(baseUri, repository, id, property, propValId);
 
         Object oKey = entry.getKey();
         String sKey = objectToMapKey(oKey);
 
         if(shouldReturnLinks(accept)) {
           resource.put(sKey, new ResourceLink(propertyRel, path));
         } else {
           URI selfUri = buildUri(baseUri, propRepoMeta.name(), propValId);
           Resource res = createResource(propRepoMeta.rel(),
                                         entry.getValue(),
                                         propRepoMeta.entityMetadata(),
                                         selfUri);
           res.addLink(new ResourceLink(SELF, selfUri));
           res.addLink(new ResourceLink(propertyRel, path));
           resource.put(sKey, res);
           // Run any post-processors for this domain type
           for(ResourcePostProcessor pp : config.getResourcePostProcessors(propRepoMeta.domainType())) {
             res = pp.postProcess(request, res);
           }
         }
       }
       body = new MapResource(resource);
     } else {
       String propValId = idAttr.get(propVal).toString();
       URI path = buildUri(baseUri, repository, id, property);
       URI selfUri = buildUri(baseUri, propRepoMeta.name(), propValId);
       if(shouldReturnLinks(accept)) {
         Resource<?> resource = new Resource<Object>();
         resource.addLink(new ResourceLink(propertyRel, path));
         body = resource;
       } else {
         MapResource res = createResource(propRepoMeta.rel(),
                                          propVal,
                                          propRepoMeta.entityMetadata(),
                                          selfUri);
         res.addLink(new ResourceLink(propertyRel, path));
         res.addLink(new ResourceLink(SELF, selfUri));
         body = res;
       }
       // Run any post-processors for this domain type
       for(ResourcePostProcessor pp : config.getResourcePostProcessors(propRepoMeta.domainType())) {
         body = pp.postProcess(request, (Resource)body);
       }
     }
 
     publishEvent(new BeforeRenderResourceEvent(request, propRepoMeta, body));
 
     return negotiateResponse(request, HttpStatus.OK, new HttpHeaders(), body);
   }
 
   /**
    * Update the property of an entity if that property is also managed by a {@link CrudRepository}.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    * @param id
    * @param property
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}/{property}",
       method = {
           RequestMethod.PUT,
           RequestMethod.POST
       }
   )
   @ResponseBody
   public ResponseEntity<?> updatePropertyOfEntity(final ServletServerHttpRequest request,
                                                   UriComponentsBuilder uriBuilder,
                                                   @PathVariable String repository,
                                                   @PathVariable String id,
                                                   final @PathVariable String property) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     final RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.SAVE_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     CrudRepository repo = repoMeta.repository();
 
     final Object entity;
     final AttributeMetadata attrMeta;
     if(null == (entity = repo.findOne(serId)) || null == (attrMeta = repoMeta.entityMetadata().attribute(property))) {
       return notFoundResponse(request);
     }
 
     Object linked = attrMeta.get(entity);
     final AtomicReference<String> rel = new AtomicReference<String>();
     Handler<Object, ResponseEntity<?>> entityHandler = new Handler<Object, ResponseEntity<?>>() {
       @Override public ResponseEntity<?> handle(Object linkedEntity) {
 
         if(attrMeta.isCollectionLike()) {
           Collection c = new ArrayList();
           Collection current = attrMeta.asCollection(entity);
           if(request.getMethod() == HttpMethod.POST && null != current) {
             c.addAll(current);
           }
           c.add(linkedEntity);
           attrMeta.set(c, entity);
         } else if(attrMeta.isSetLike()) {
           Set s = new HashSet();
           Set current = attrMeta.asSet(entity);
           if(request.getMethod() == HttpMethod.POST && null != current) {
             s.addAll(current);
           }
           s.add(linkedEntity);
           attrMeta.set(s, entity);
         } else if(attrMeta.isMapLike()) {
           Map m = new HashMap();
           Map current = attrMeta.asMap(entity);
           if(request.getMethod() == HttpMethod.POST && null != current) {
             m.putAll(current);
           }
           String key = rel.get();
           if(null == key) {
             throw new IllegalArgumentException("Map key cannot be null (usually the 'rel' value of a JSON object).");
           }
           m.put(rel.get(), linkedEntity);
           attrMeta.set(m, entity);
         } else {
           // Don't support POST when it's a single value
           if(request.getMethod() == HttpMethod.POST) {
             try {
               return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
             } catch(IOException e) {
               throw new IllegalStateException(e.getMessage(), e);
             }
           }
           attrMeta.set(linkedEntity, entity);
         }
 
         return null;
       }
     };
 
     MediaType incomingMediaType = request.getHeaders().getContentType();
     LinkList incomingLinks = readIncoming(request,
                                           incomingMediaType,
                                           LinkList.class);
     for(Link l : incomingLinks.getLinks()) {
       Object o;
       if(null != (o = domainObjectResolver.resolve(baseUri, URI.create(l.href().toString())))) {
         rel.set(l.rel());
         ResponseEntity<?> possibleResponse = entityHandler.handle(o);
         if(null != possibleResponse) {
           return possibleResponse;
         }
       }
 
       publishEvent(new BeforeSaveEvent(entity));
       publishEvent(new BeforeLinkSaveEvent(entity, linked));
       Object savedEntity = repo.save(entity);
       linked = attrMeta.get(savedEntity);
       publishEvent(new AfterLinkSaveEvent(savedEntity, linked));
       publishEvent(new AfterSaveEvent(savedEntity));
     }
 
     if(request.getMethod() == HttpMethod.PUT) {
       return negotiateResponse(request, HttpStatus.NO_CONTENT, new HttpHeaders(), null);
     } else {
       return negotiateResponse(request, HttpStatus.CREATED, new HttpHeaders(), null);
     }
   }
 
   /**
    * Clear all linked entities of a specific property.
    *
    * @param request
    * @param repository
    * @param id
    * @param property
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}/{property}",
       method = {
           RequestMethod.DELETE
       }
   )
   @ResponseBody
   public ResponseEntity<?> clearLinks(ServletServerHttpRequest request,
                                       @PathVariable String repository,
                                       @PathVariable String id,
                                       @PathVariable String property) throws IOException {
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.SAVE_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     CrudRepository repo = repoMeta.repository();
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
 
     Object entity;
     AttributeMetadata attrMeta;
     if(null == (entity = repo.findOne(serId)) || null == (attrMeta = repoMeta.entityMetadata().attribute(property))) {
       return notFoundResponse(request);
     }
 
     // Check if this is a @*ToOne relationship and is optional and if not, fail with a 405 Method Not Allowed
     if((attrMeta.hasAnnotation(ManyToOne.class) && !attrMeta.annotation(ManyToOne.class).optional())
         || (attrMeta.hasAnnotation(OneToOne.class) && !attrMeta.annotation(OneToOne.class).optional())) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
 
     Object linked = attrMeta.get(entity);
     attrMeta.set(null, entity);
 
     publishEvent(new BeforeLinkSaveEvent(entity, linked));
     Object savedEntity = repo.save(entity);
     publishEvent(new AfterLinkSaveEvent(savedEntity, null));
 
     return negotiateResponse(request, HttpStatus.NO_CONTENT, new HttpHeaders(), null);
   }
 
   /**
    * Retrieve a linked entity from a parent entity.
    *
    * @param request
    * @param uriBuilder
    * @param repository
    * @param id
    * @param property
    * @param linkedId
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}/{property}/{linkedId}",
       method = {
           RequestMethod.GET
       }
   )
   @ResponseBody
   public ResponseEntity<?> linkedEntity(ServletServerHttpRequest request,
                                         UriComponentsBuilder uriBuilder,
                                         @PathVariable String repository,
                                         @PathVariable String id,
                                         @PathVariable String property,
                                         @PathVariable String linkedId) throws IOException {
     URI baseUri = uriBuilder.build().toUri();
     BASE_URI.set(baseUri);
 
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     if(!repoMeta.exportsMethod(CrudMethod.FIND_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
 
     // Check for the existence of the parent
     CrudRepository repo = repoMeta.repository();
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     if(!repo.exists(serId)) {
       return notFoundResponse(request);
     }
 
     // Check for the existence of the property
     AttributeMetadata attrMeta;
     if(null == (attrMeta = repoMeta.entityMetadata().attribute(property))) {
       return notFoundResponse(request);
     }
 
     // Check for the existence of a Repository for the linked entity
     RepositoryMetadata linkedRepoMeta;
     if(null == (linkedRepoMeta = repositoryMetadataFor(attrMeta))) {
       return notFoundResponse(request);
     }
 
     if(!linkedRepoMeta.exportsMethod(CrudMethod.FIND_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
 
     // Find the linked entity
     CrudRepository linkedRepo = linkedRepoMeta.repository();
     Serializable sChildId = stringToSerializable(linkedId,
                                                  (Class<? extends Serializable>)linkedRepoMeta.entityMetadata()
                                                                                               .idAttribute()
                                                                                               .type());
     Object linkedEntity;
     if(null == (linkedEntity = linkedRepo.findOne(sChildId))) {
       return notFoundResponse(request);
     }
 
     String propertyRel = repository + "." + repoMeta.entityMetadata().type().getSimpleName() + "." + property;
     URI propertyPath = buildUri(baseUri, repository, id, property, linkedId);
     URI selfUri = buildUri(baseUri, linkedRepoMeta.name(), linkedId);
     Resource res = createResource(linkedRepoMeta.rel(),
                                   linkedEntity,
                                   linkedRepoMeta.entityMetadata(),
                                   selfUri);
     res.addLink(new ResourceLink(propertyRel, propertyPath));
     res.addLink(new ResourceLink(SELF, selfUri));
 
     publishEvent(new BeforeRenderResourcesEvent(request, repoMeta, res));
     // Run any post-processors for this domain type
     for(ResourcePostProcessor pp : config.getResourcePostProcessors(linkedRepoMeta.domainType())) {
       res = pp.postProcess(request, res);
     }
 
     HttpHeaders headers = new HttpHeaders();
     headers.add("Content-Location", selfUri.toString());
 
     return negotiateResponse(request, HttpStatus.OK, headers, res);
   }
 
   /**
    * Delete a specific relationship between a child entity and its parent.
    *
    * @param request
    * @param repository
    * @param id
    * @param property
    * @param linkedId
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @RequestMapping(
       value = "/{repository}/{id}/{property}/{linkedId}",
       method = {
           RequestMethod.DELETE
       }
   )
   @ResponseBody
   public ResponseEntity<?> deleteLink(ServletServerHttpRequest request,
                                       @PathVariable String repository,
                                       @PathVariable String id,
                                       @PathVariable String property,
                                       @PathVariable String linkedId) throws IOException {
     RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
     CrudRepository repo = repoMeta.repository();
     // If I can't load the parent entity, then this method isn't allowed.
     if(!repoMeta.exportsMethod(CrudMethod.FIND_ONE) || !repoMeta.exportsMethod(CrudMethod.SAVE_ONE)) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
     Serializable serId = stringToSerializable(id,
                                               (Class<? extends Serializable>)repoMeta.entityMetadata()
                                                                                      .idAttribute()
                                                                                      .type());
     Object entity;
     AttributeMetadata attrMeta;
     if(null == (entity = repo.findOne(serId)) || null == (attrMeta = repoMeta.entityMetadata().attribute(property))) {
       return notFoundResponse(request);
     }
 
     // Check if this @*ToOne relationship is optional and if not, fail with a 405 Method Not Allowed
     if((attrMeta.hasAnnotation(ManyToOne.class) && !attrMeta.annotation(ManyToOne.class).optional())
         || (attrMeta.hasAnnotation(OneToOne.class) && !attrMeta.annotation(OneToOne.class).optional())) {
       return negotiateResponse(request, HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
     }
 
     // Find linked entity
     RepositoryMetadata linkedRepoMeta;
     if(null == (linkedRepoMeta = repositoryMetadataFor(attrMeta))) {
       return notFoundResponse(request);
     }
 
     CrudRepository linkedRepo = linkedRepoMeta.repository();
     Serializable sChildId = stringToSerializable(linkedId,
                                                  (Class<? extends Serializable>)linkedRepoMeta.entityMetadata()
                                                                                               .idAttribute()
                                                                                               .type());
 
     Object linkedEntity;
     if(null == (linkedEntity = linkedRepo.findOne(sChildId))) {
       return notFoundResponse(request);
     }
 
     // Remove linked entity from relationship based on property type
     if(attrMeta.isCollectionLike()) {
       Collection c = attrMeta.asCollection(entity);
       if(null != c && c != Collections.emptyList()) {
         c.remove(linkedEntity);
       }
     } else if(attrMeta.isSetLike()) {
       Set s = attrMeta.asSet(entity);
       if(null != s && s != Collections.emptySet()) {
         s.remove(linkedEntity);
       }
     } else if(attrMeta.isMapLike()) {
       Object keyToRemove = null;
       Map<Object, Object> m = attrMeta.asMap(entity);
       if(null != m && m != Collections.emptyMap()) {
         for(Map.Entry<Object, Object> entry : m.entrySet()) {
           Object val = entry.getValue();
           if(null != val && val.equals(linkedEntity)) {
             keyToRemove = entry.getKey();
             break;
           }
         }
         if(null != keyToRemove) {
           m.remove(keyToRemove);
         }
       }
     } else {
       attrMeta.set(null, entity);
     }
 
     publishEvent(new BeforeLinkDeleteEvent(entity, linkedEntity));
     Object savedEntity = repo.save(entity);
     publishEvent(new AfterLinkDeleteEvent(savedEntity, linkedEntity));
 
     return negotiateResponse(request, HttpStatus.NO_CONTENT, new HttpHeaders(), null);
   }
 
   /**
    * Send a 404 if no repository was found.
    *
    * @param e
    * @param request
    *
    * @return
    *
    * @throws IOException
    */
   @ExceptionHandler(RepositoryNotFoundException.class)
   @ResponseBody
   public ResponseEntity handleRepositoryNotFoundFailure(RepositoryNotFoundException e,
                                                         ServletServerHttpRequest request) throws IOException {
     if(LOG.isWarnEnabled()) {
       LOG.warn("RepositoryNotFoundException: " + e.getMessage());
     }
     return notFoundResponse(request);
   }
 
   /**
    * Handle NPEs as a regular 500 error.
    *
    * @param e
    * @param request
    *
    * @return
    *
    * @throws IOException
    */
   @ExceptionHandler(NullPointerException.class)
   @ResponseBody
   public ResponseEntity handleNPE(NullPointerException e,
                                   ServletServerHttpRequest request) throws IOException {
     LOG.error(e.getMessage(), e);
     return errorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, e);
   }
 
   /**
    * Handle failures commonly thrown from code tries to read incoming data and convert or cast it to the right type.
    *
    * @param t
    * @param request
    *
    * @return
    *
    * @throws IOException
    */
   @ExceptionHandler(
       {
           InvocationTargetException.class,
           IllegalArgumentException.class,
           ClassCastException.class,
           ConversionFailedException.class
       }
   )
   @ResponseBody
   public ResponseEntity handleMiscFailures(Throwable t,
                                            ServletServerHttpRequest request) throws IOException {
     LOG.error(t.getMessage(), t);
     return errorResponse(request, HttpStatus.BAD_REQUEST, t);
   }
 
   /**
    * Send a 409 Conflict in case of concurrent modification.
    *
    * @param ex
    * @param request
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
   @ResponseBody
   public ResponseEntity handleConflict(Exception ex,
                                        ServletServerHttpRequest request) throws IOException {
     LOG.error(ex.getMessage(), ex);
     return errorResponse(request, HttpStatus.CONFLICT, ex);
   }
 
   /**
    * Send a 400 Bad Request in case of a validation failure.
    *
    * @param ex
    * @param request
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @ExceptionHandler(RepositoryConstraintViolationException.class)
   @ResponseBody
   public ResponseEntity handleValidationFailure(RepositoryConstraintViolationException ex,
                                                 ServletServerHttpRequest request) throws IOException {
     LOG.error(ex.getMessage(), ex);
 
     Map m = new HashMap();
     List<String> errors = new ArrayList<String>();
     for(FieldError fe : ex.getErrors().getFieldErrors()) {
       errors.add(fe.getDefaultMessage());
     }
     m.put("errors", errors);
 
     return negotiateResponse(request, HttpStatus.BAD_REQUEST, new HttpHeaders(), m);
   }
 
   /**
    * Send a 400 Bad Request in case no converter was found to process the input or output.
    *
    * @param ex
    * @param request
    *
    * @return
    *
    * @throws IOException
    */
   @SuppressWarnings({"unchecked"})
   @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageNotWritableException.class})
   @ResponseBody
   public ResponseEntity handleMessageConversionFailure(Exception ex,
                                                        HttpServletRequest request) throws IOException {
     LOG.error(ex.getMessage(), ex);
 
     Map m = new HashMap();
     m.put("message", ex.getMessage());
     m.put("acceptableTypes", availableMediaTypes);
 
     return negotiateResponse(new ServletServerHttpRequest(request), HttpStatus.BAD_REQUEST, new HttpHeaders(), m);
   }
 
   /*
   -----------------------------------
     Internal helper methods
   -----------------------------------
    */
   private static URI buildUri(URI baseUri, String... pathSegments) {
     return UriComponentsBuilder.fromUri(baseUri).pathSegment(pathSegments).build().toUri();
   }
 
   @SuppressWarnings({"unchecked"})
   private void maybeAddPrevNextLink(URI resourceUri,
                                     RepositoryMetadata repoMeta,
                                     PagingAndSorting pageSort,
                                     Page page,
                                     boolean addIf,
                                     int nextPage,
                                     String rel,
                                     Set<Link> links) {
     if(null != page && addIf) {
       UriComponentsBuilder urib = UriComponentsBuilder.fromUri(resourceUri);
       urib.queryParam(config.getPageParamName(), nextPage); // PageRequest is 0-based, so it's already (page - 1)
       urib.queryParam(config.getLimitParamName(), page.getSize());
       pageSort.addSortParameters(urib);
       links.add(new ResourceLink(repoMeta.rel() + "." + rel, urib.build().toUri()));
     }
   }
 
   @SuppressWarnings({"unchecked"})
   private <V extends Serializable> V stringToSerializable(String s, Class<V> targetType) {
     if(ClassUtils.isAssignable(targetType, String.class)) {
       return (V)s;
     } else {
       return conversionService.convert(s, targetType);
     }
   }
 
   @SuppressWarnings({"unchecked"})
   private <V> V readIncoming(HttpInputMessage request, MediaType incomingMediaType, Class<V> targetType)
       throws IOException {
     // Check custom converters first
     for(HttpMessageConverter conv : config.getCustomConverters()) {
       if(conv.canRead(targetType, incomingMediaType)) {
         return (V)conv.read(targetType, request);
       }
     }
     // Use our always-available default list of converters
     for(HttpMessageConverter conv : httpMessageConverters) {
       if(conv.canRead(targetType, incomingMediaType)) {
         return (V)conv.read(targetType, request);
       }
     }
     return (V)mappingHttpMessageConverter.read(targetType, request);
   }
 
   private MapResource createResource(String repoRel,
                                      Object entity,
                                      EntityMetadata<AttributeMetadata> entityMetadata,
                                      URI baseUri) {
     Map<String, Object> entityDto = new HashMap<String, Object>();
     MapResource resource = new MapResource(entityDto);
 
     for(Map.Entry<String, AttributeMetadata> attrMeta : entityMetadata.embeddedAttributes().entrySet()) {
       String name = attrMeta.getKey();
       Object val;
       if(null != (val = attrMeta.getValue().get(entity))) {
         entityDto.put(name, val);
       }
     }
 
     for(String attrName : entityMetadata.linkedAttributes().keySet()) {
       URI uri = buildUri(baseUri, attrName);
       String rel = repoRel + "." + entity.getClass().getSimpleName() + "." + attrName;
       resource.addLink(new ResourceLink(rel, uri));
     }
 
     return resource;
   }
 
   private boolean shouldReturnLinks(String acceptHeader) {
     if(null != acceptHeader) {
       List<MediaType> accept = MediaType.parseMediaTypes(acceptHeader);
       for(MediaType mt : accept) {
         if(mt.getSubtype().startsWith("x-spring-data-verbose")) {
           return false;
         } else if(mt.getSubtype().startsWith("x-spring-data-compact")) {
           return true;
         } else if(mt.getSubtype().equals("uri-list")) {
           return true;
         }
       }
     }
     return false;
   }
 
   private boolean returnBody(ServletServerHttpRequest request) {
     String s = request.getServletRequest().getParameter("returnBody");
     if(null != s) {
       return "true".equals(s);
     } else {
       return false;
     }
   }
 
   private <E extends RepositoryEvent> void publishEvent(E event) {
     if(null != applicationContext) {
       applicationContext.publishEvent(event);
     }
   }
 
   private boolean notPagingParam(String name) {
     return (!config.getPageParamName().equals(name)
         && !config.getLimitParamName().equals(name)
         && !config.getSortParamName().equals(name));
   }
 
   @SuppressWarnings({"unchecked"})
   private Map throwableToMap(Throwable t) {
     Map m = new HashMap();
     m.put("message", t.getMessage());
     if(null != t.getCause()) {
       m.put("cause", throwableToMap(t.getCause()));
     }
     return m;
   }
 
   private String objectToMapKey(Object obj) {
     Assert.notNull(obj, "Map key cannot be null!");
 
     RepositoryMetadata repoMeta;
     String key;
     if(ClassUtils.isAssignable(obj.getClass(), String.class)) {
       key = (String)obj;
     } else if(null != (repoMeta = repositoryMetadataFor(obj.getClass()))) {
       AttributeMetadata attrMeta = repoMeta.entityMetadata().idAttribute();
       String id = attrMeta.get(obj).toString();
       key = "@" + buildUri(BASE_URI.get(), repoMeta.name(), id);
     } else {
       key = conversionService.convert(obj, String.class);
     }
 
     return key;
   }
 
   private ResponseEntity<byte[]> notFoundResponse(ServletServerHttpRequest request) throws IOException {
     return negotiateResponse(request, HttpStatus.NOT_FOUND, new HttpHeaders(), null);
   }
 
   @SuppressWarnings({"unchecked"})
   private ResponseEntity<byte[]> errorResponse(ServletServerHttpRequest request, HttpStatus status, Throwable t)
       throws IOException {
     Object body = null;
     if(config.isDumpErrors()) {
       body = throwableToMap(t);
     }
     return negotiateResponse(request, status, new HttpHeaders(), body);
   }
 
   @SuppressWarnings({"unchecked"})
   private ResponseEntity<byte[]> negotiateResponse(final ServletServerHttpRequest request,
                                                    final HttpStatus status,
                                                    final HttpHeaders headers,
                                                    final Object resource) throws IOException {
 
     String jsonpParam = request.getServletRequest().getParameter(config.getJsonpParamName());
     String jsonpOnErrParam = null;
     if(null != config.getJsonpOnErrParamName()) {
       jsonpOnErrParam = request.getServletRequest().getParameter(config.getJsonpOnErrParamName());
     }
 
     if(null == resource) {
       return maybeWrapJsonp(status, jsonpParam, jsonpOnErrParam, headers, null);
     }
 
     MediaType acceptType = config.getDefaultMediaType();
     HttpMessageConverter converter = findWriteConverter(resource.getClass(), acceptType);
     // If an Accept header is specified that isn't the catch-all, try and find a converter for it.
    if(!MediaTypes.ACCEPT_ALL_TYPES.equals(request.getHeaders().getAccept())) {
       for(MediaType mt : request.getHeaders().getAccept()) {
        if(null != (converter = findWriteConverter(resource.getClass(), mt))) {
          if(!"*".equals(mt.getSubtype())) {
            acceptType = mt;
          }
          break;
         }
       }
     }
    headers.setContentType(acceptType);
 
     if(null == converter) {
       converter = mappingHttpMessageConverter;
     }
 
     final ByteArrayOutputStream bout = new ByteArrayOutputStream();
     converter.write(resource, headers.getContentType(), new HttpOutputMessage() {
       @Override public OutputStream getBody() throws IOException {
         return bout;
       }
 
       @Override public HttpHeaders getHeaders() {
         return headers;
       }
     });
 
     return maybeWrapJsonp(status, jsonpParam, jsonpOnErrParam, headers, bout.toByteArray());
   }
 
   @SuppressWarnings({"unchecked"})
   private HttpMessageConverter findWriteConverter(Class<?> type, MediaType mediaType) {
     for(HttpMessageConverter conv : config.getCustomConverters()) {
       if(conv.canWrite(type, mediaType)) {
         return conv;
       }
     }
     for(HttpMessageConverter conv : httpMessageConverters) {
       if(conv.canWrite(type, mediaType)) {
         return conv;
       }
     }
     return null;
   }
 
   private ResponseEntity<byte[]> maybeWrapJsonp(HttpStatus status,
                                                 String jsonpParam,
                                                 String jsonpOnErrParam,
                                                 HttpHeaders headers,
                                                 byte[] body) {
 
     byte[] responseBody = (null == body ? new byte[0] : body);
     if(status.value() >= 400 && null != jsonpOnErrParam) {
       status = HttpStatus.OK;
       responseBody = String.format("%s(%s, %s)",
                                    jsonpOnErrParam,
                                    status.value(),
                                    (null != body ? new String(body) : null))
                            .getBytes();
       headers.setContentType(MediaTypes.APPLICATION_JAVASCRIPT);
     } else if(null != jsonpParam) {
       responseBody = String.format("%s(%s)",
                                    jsonpParam,
                                    (null != body ? new String(body) : null))
                            .getBytes();
       headers.setContentType(MediaTypes.APPLICATION_JAVASCRIPT);
     }
     headers.setContentLength(responseBody.length);
 
     return new ResponseEntity<byte[]>(responseBody, headers, status);
   }
 
 }
