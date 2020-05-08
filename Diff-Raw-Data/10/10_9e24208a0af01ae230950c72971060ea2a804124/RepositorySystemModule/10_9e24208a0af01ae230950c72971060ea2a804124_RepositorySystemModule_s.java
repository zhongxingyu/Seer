 package com.cloudbees.sdk.maven;
 
 import com.cloudbees.api.BeesClientConfiguration;
 import com.cloudbees.sdk.cli.BeesClientFactory;
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
 import org.apache.maven.settings.Server;
 import org.codehaus.plexus.DefaultContainerConfiguration;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.PlexusContainer;
 import org.codehaus.plexus.PlexusContainerException;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.jboss.shrinkwrap.resolver.impl.maven.MavenDependencyResolverSettings;
 import org.slf4j.LoggerFactory;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.impl.VersionResolver;
 import org.sonatype.aether.repository.Authentication;
 import org.sonatype.aether.repository.Proxy;
 import org.sonatype.aether.repository.RemoteRepository;
 
import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Module that adds {@link RepositorySystem} to Guice.
  *
  * <p>
  * This makes it easy for plugins to interact with Maven repository, most typically to download
  * artifacts.
  *
  * @author Kohsuke Kawaguchi
  */
 public class RepositorySystemModule extends AbstractModule {
     @Override
     protected void configure() {
        bind(VersionResolver.class).to(VersionResolverImpl.class);

         // NettyAsyncHttpProvider prints some INFO-level messages. suppress them
         Logger.getLogger("com.ning.http.client.providers.netty.NettyAsyncHttpProvider").setLevel(Level.WARNING);
         LoggerFactory.getLogger(NettyAsyncHttpProvider.class);
     }
 
     @Provides @Aether @Singleton
    public PlexusContainer aetherContainer(final VersionResolver resolver) {
         try {
             return new DefaultPlexusContainer(
                 new DefaultContainerConfiguration(),
                 new AbstractModule() {
                     @Override
                     protected void configure() {
                        bind(VersionResolver.class).toInstance(resolver);
                     }
                 }
             );
         } catch (PlexusContainerException e) {
             throw new RuntimeException("Unable to load RepositorySystem component by Plexus, cannot establish Aether dependency resolver.", e);
         }
     }
 
     @Provides @Singleton
     public RepositorySystem get(@Aether PlexusContainer aether) {
         return lookup(aether, RepositorySystem.class);
     }
 
     /**
      * List of remote repositories to resolve artifacts from.
      */
     @Provides @Singleton
     public List<RemoteRepository> getRemoteRepositories(BeesClientFactory bees) throws IOException {
         BeesClientConfiguration bcc = bees.get().getBeesClientConfiguration();
 
         List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
         MavenDependencyResolverSettings resolverSettings = new MavenDependencyResolverSettings();
         resolverSettings.setUseMavenCentral(true);
         List<RemoteRepository> repos = resolverSettings.getRemoteRepositories();
         for (RemoteRepository remoteRepository : repos) {
             Server server = resolverSettings.getSettings().getServer(remoteRepository.getId());
             if (server != null) {
                 remoteRepository.setAuthentication(new Authentication(server.getUsername(), server.getPassword(), server.getPrivateKey(), server.getPassphrase()));
             }
             setRemoteRepositoryProxy(remoteRepository, bcc);
             repositories.add(remoteRepository);
         }
         RemoteRepository r = new RemoteRepository("cloudbees-public-release", "default", "https://repository-cloudbees.forge.cloudbees.com/public-release/");
         repositories.add(setRemoteRepositoryProxy(r,bcc));
         return repositories;
     }
 
     private RemoteRepository setRemoteRepositoryProxy(RemoteRepository repo, BeesClientConfiguration bcc) {
         if (bcc==null)  return repo;
 
         if (bcc.getProxyHost() != null && bcc.getProxyPort() > 0) {
             String proxyType = Proxy.TYPE_HTTP;
             if (repo.getUrl().startsWith("https"))
                 proxyType = Proxy.TYPE_HTTPS;
             Proxy proxy = new Proxy(proxyType, bcc.getProxyHost(), bcc.getProxyPort(), null);
             if (bcc.getProxyUser() != null) {
                 Authentication authentication = new Authentication(bcc.getProxyUser(), bcc.getProxyPassword());
                 proxy.setAuthentication(authentication);
             }
             repo.setProxy(proxy);
         }
         return repo;
     }
 
 
     protected  <T> T lookup(PlexusContainer aether, Class<T> type) {
         try {
             return aether.lookup(type);
         } catch (ComponentLookupException e) {
             throw new RuntimeException("Unable to lookup component RepositorySystem, cannot establish Aether dependency resolver.", e);
         }
     }
 }
