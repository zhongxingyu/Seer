 package org.sonatype.nexus.plugins.requestinterceptor.internal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 
 import org.codehaus.plexus.util.SelectorUtils;
 import org.slf4j.Logger;
 import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptor;
 import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptorConfiguration;
 import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptors;
 import org.sonatype.nexus.proxy.NoSuchRepositoryException;
 import org.sonatype.nexus.proxy.access.Action;
 import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
 import org.sonatype.nexus.proxy.repository.Repository;
 import org.sonatype.nexus.proxy.repository.RequestProcessor;
 
 @Named
 @Singleton
 public class DefaultRequestInterceptors
     implements RequestInterceptors
 {
 
     private final RepositoryRegistry repositories;
 
     private final Map<String, List<RequestInterceptorConfiguration>> configurations;
 
     private final RequestProcessor requestProcessor;
 
     @Inject
     private Logger logger;
 
    private final Map<String, RequestInterceptor> interceptors;
 
     @Inject
     public DefaultRequestInterceptors( final RepositoryRegistry repositories,
                                        final Map<String, RequestInterceptor> generators )
     {
         this.repositories = repositories;
        interceptors = generators;
         configurations = new HashMap<String, List<RequestInterceptorConfiguration>>();
         requestProcessor = new RequestInterceptorRequestProcessor( this );
     }
 
     @Override
     public void handle( final String repositoryId, final String requestPath, final Action action )
     {
         final Collection<RequestInterceptorConfiguration> configurations = getConfigurations( repositoryId );
         if ( configurations == null )
         {
             return;
         }
        String cannonicalPath = requestPath;
        if ( !cannonicalPath.startsWith( "/" ) )
        {
            cannonicalPath = "/" + cannonicalPath;
        }
         for ( final RequestInterceptorConfiguration configuration : configurations )
         {
             final Action expectedAction = configuration.action();
             if ( ( expectedAction == null || action.equals( expectedAction ) )
                && SelectorUtils.matchPath( configuration.mapping(), cannonicalPath ) )
             {
                final RequestInterceptor interceptor = interceptors.get( configuration.generator() );
                if ( interceptor == null )
                 {
                     logger.warn(
                         "Request Interceptor [{}] could not be executed as interceptor [{}] could not be found",
                        cannonicalPath, configuration.generator() );
                 }
                 else
                 {
                     try
                     {
                        interceptor.execute( repositories.getRepository( repositoryId ), cannonicalPath, action );
                         // first generator wins
                         break;
                     }
                     catch ( final NoSuchRepositoryException e )
                     {
                         logger.warn(
                             "Request Interceptor [{}] could not be executed as repository [{}] could not be found",
                            cannonicalPath, repositoryId );
                     }
                 }
             }
         }
     }
 
     @Override
     public void addConfiguration( final RequestInterceptorConfiguration configuration )
     {
         List<RequestInterceptorConfiguration> configs = configurations.get( configuration.repositoryId() );
         if ( configs == null )
         {
             configs = new ArrayList<RequestInterceptorConfiguration>();
             configurations.put( configuration.repositoryId(), configs );
         }
         configs.add( configuration );
         try
         {
             final Repository repository = repositories.getRepository( configuration.repositoryId() );
             repository.getRequestProcessors().put( requestProcessorKey( configuration.repositoryId() ),
                 requestProcessor );
         }
         catch ( final NoSuchRepositoryException e )
         {
             logger.warn( "Could not enable request interceptors for repository [{}] as repository does not exist",
                 configuration.repositoryId() );
         }
     }
 
     @Override
     public void removeConfiguration( final RequestInterceptorConfiguration configuration )
     {
         final List<RequestInterceptorConfiguration> configs = configurations.get( configuration.repositoryId() );
         if ( configs != null )
         {
             configs.remove( configuration );
         }
         try
         {
             final Repository repository = repositories.getRepository( configuration.repositoryId() );
             repository.getRequestProcessors().remove( requestProcessorKey( configuration.repositoryId() ) );
         }
         catch ( final NoSuchRepositoryException e )
         {
             logger.warn( "Could not disable request interceptors for repository [{}] as repository does not exist",
                 configuration.repositoryId() );
         }
     }
 
     @Override
     public Collection<RequestInterceptorConfiguration> getConfigurations( final String repositoryId )
     {
         return configurations.get( repositoryId );
     }
 
     private String requestProcessorKey( final String repositoryId )
     {
         return RequestInterceptorRequestProcessor.class.getName() + "/" + repositoryId;
     }
 
 }
