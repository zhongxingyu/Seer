 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.restlet;
 
 import freemarker.ext.beans.BeansWrapper;
 import freemarker.template.Configuration;
 import info.aduna.iteration.Iterations;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.StandardCharsets;
 import java.util.List;
 import java.util.Set;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.Namespace;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.http.HTTPRepository;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.Rio;
 import org.openrdf.sail.memory.MemoryStore;
 import org.restlet.Context;
 import org.restlet.ext.crypto.DigestAuthenticator;
 import org.restlet.ext.crypto.DigestVerifier;
 import org.restlet.ext.freemarker.ContextTemplateLoader;
 import org.restlet.security.ChallengeAuthenticator;
 import org.restlet.security.LocalVerifier;
 import org.restlet.security.Realm;
 import org.restlet.security.Role;
 import org.restlet.security.User;
 import org.semanticweb.owlapi.model.OWLException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.ansell.propertyutil.PropertyUtil;
 import com.github.ansell.restletutils.FixedRedirectCookieAuthenticator;
 import com.github.ansell.restletutils.RestletUtilUser;
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.file.DataReferenceManager;
 import com.github.podd.api.file.DataReferenceProcessorFactory;
 import com.github.podd.api.file.DataReferenceProcessorRegistry;
 import com.github.podd.api.file.PoddDataRepositoryManager;
 import com.github.podd.api.purl.PoddPurlManager;
 import com.github.podd.api.purl.PoddPurlProcessorFactory;
 import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
 import com.github.podd.exception.PoddException;
 import com.github.podd.impl.PoddArtifactManagerImpl;
 import com.github.podd.impl.PoddOWLManagerImpl;
 import com.github.podd.impl.PoddRepositoryManagerImpl;
 import com.github.podd.impl.PoddSchemaManagerImpl;
 import com.github.podd.impl.PoddSesameManagerImpl;
 import com.github.podd.impl.file.FileReferenceManagerImpl;
 import com.github.podd.impl.file.PoddFileRepositoryManagerImpl;
 import com.github.podd.impl.file.SSHFileReferenceProcessorFactoryImpl;
 import com.github.podd.impl.purl.PoddPurlManagerImpl;
 import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddRoles;
 import com.github.podd.utils.PoddUser;
 import com.github.podd.utils.PoddUserStatus;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class ApplicationUtils
 {
     private static final Logger log = LoggerFactory.getLogger(ApplicationUtils.class);
     
     /**
      * @param application
      * @param nextRepository
      * @throws RepositoryException
      * @throws RDFHandlerException
      */
     private static void dumpSchemaGraph(final PoddWebServiceApplication application, final Repository nextRepository)
         throws RepositoryException, RDFHandlerException
     {
         RepositoryConnection conn = null;
         
         try
         {
             conn = nextRepository.getConnection();
             
             final Model model =
                     new LinkedHashModel(Iterations.asList(conn.getStatements(null, null, null, true, application
                             .getPoddRepositoryManager().getSchemaManagementGraph())));
             for(final Namespace nextNamespace : Iterations.asSet(conn.getNamespaces()))
             {
                 model.setNamespace(nextNamespace);
             }
             Rio.write(model, System.out, RDFFormat.TURTLE);
         }
         finally
         {
             if(conn != null)
             {
                 conn.close();
             }
         }
     }
     
     public static ChallengeAuthenticator getNewAuthenticator(final Realm nextRealm, final Context newChildContext,
             final PropertyUtil propertyUtil)
     {
         ChallengeAuthenticator result = null;
         
         // FIXME: read from a property
         final String authMethod =
                 propertyUtil.get(PoddWebConstants.PROPERTY_CHALLENGE_AUTH_METHOD,
                         PoddWebConstants.DEF_CHALLENGE_AUTH_METHOD);
         
         if(authMethod.equalsIgnoreCase("digest"))
         {
             ApplicationUtils.log.info("Using digest authenticator");
             // FIXME: Stub implementation
             result = new DigestAuthenticator(newChildContext, nextRealm.getName(), "s3cret");
             
             if(nextRealm.getVerifier() instanceof DigestVerifier)
             {
                 // NOTE: The verifier in this case must support digest verification by being an
                 // instance of DigestVerifier
                 result.setVerifier(nextRealm.getVerifier());
             }
             else if(nextRealm.getVerifier() instanceof LocalVerifier)
             {
                 // else we need to map the verifier in
                 ((DigestAuthenticator)result).setWrappedVerifier((LocalVerifier)nextRealm.getVerifier());
             }
             else
             {
                 throw new RuntimeException("Verifier was not valid for use with DigestAuthenticator verifier="
                         + nextRealm.getVerifier().toString());
             }
             
             result.setEnroler(nextRealm.getEnroler());
             
             result.setOptional(true);
             // Boolean.valueOf(PropertyUtil.getProperty(OasProperties.PROPERTY_CHALLENGE_AUTH_OPTIONAL,
             // OasProperties.DEFAULT_CHALLENGE_AUTH_OPTIONAL)));
         }
         else if(authMethod.equalsIgnoreCase("cookie"))
         {
             ApplicationUtils.log.info("Using cookie authenticator");
             
             // FIXME: Stub implementation
             final byte[] secretKey = "s3cr3t2345667123".getBytes(StandardCharsets.UTF_8);
             
             result = new FixedRedirectCookieAuthenticator(newChildContext, nextRealm.getName(), secretKey);
             
             ((FixedRedirectCookieAuthenticator)result).setLoginPath(PoddWebConstants.PATH_LOGIN_SUBMIT);
             ((FixedRedirectCookieAuthenticator)result).setLogoutPath(PoddWebConstants.PATH_LOGOUT);
             
             // FIXME: Make this configurable
             ((FixedRedirectCookieAuthenticator)result).setCookieName(PoddWebConstants.COOKIE_NAME);
             // FIXME: Make this configurable
             ((FixedRedirectCookieAuthenticator)result).setIdentifierFormName("username");
             // FIXME: Make this configurable
             ((FixedRedirectCookieAuthenticator)result).setSecretFormName("password");
             ((FixedRedirectCookieAuthenticator)result).setInterceptingLogin(true);
             ((FixedRedirectCookieAuthenticator)result).setInterceptingLogout(true);
             ((FixedRedirectCookieAuthenticator)result).setFixedRedirectUri(PoddWebConstants.PATH_REDIRECT_LOGGED_IN);
             
             result.setMultiAuthenticating(false);
             
             result.setVerifier(nextRealm.getVerifier());
             result.setEnroler(nextRealm.getEnroler());
             result.setOptional(true);
             
         }
         else if(authMethod.equalsIgnoreCase("http"))
         {
             // FIXME: Implement a stub here
             ApplicationUtils.log.error("FIXME: Implement HTTP ChallengeAuthenticator authMethod={}", authMethod);
             throw new RuntimeException("FIXME: Implement HTTP ChallengeAuthenticator");
         }
         else
         {
             ApplicationUtils.log.error("Did not recognise ChallengeAuthenticator method authMethod={}", authMethod);
             throw new RuntimeException("Did not recognise ChallengeAuthenticator method");
         }
         
         return result;
     }
     
     public static Repository getNewRepository(PropertyUtil props) throws RepositoryException
     {
         final String repositoryUrl =
                 props.get(PoddWebConstants.PROPERTY_SESAME_URL, PoddWebConstants.DEFAULT_SESAME_URL);
         
         // if we weren't able to find a repository URL in the configuration, we setup an
         // in-memory store
         if(repositoryUrl == null || repositoryUrl.trim().isEmpty())
         {
             final Repository repository = new SailRepository(new MemoryStore());
             
             try
             {
                 repository.initialize();
                 
                 ApplicationUtils.log.info("Created an in memory store as repository for PODD");
                 
                 return repository;
             }
             catch(final RepositoryException ex)
             {
                 repository.shutDown();
                 throw new RuntimeException("Could not initialise Sesame In Memory repository");
             }
         }
         else
         {
            final Repository repository = new HTTPRepository(repositoryUrl.trim());
             
             try
             {
                 repository.initialize();
                 
                 ApplicationUtils.log.info("Using sesame http repository as repository for PODD: {}", repositoryUrl);
                 
                 return repository;
             }
             catch(final RepositoryException ex)
             {
                 repository.shutDown();
                 throw new RuntimeException("Could not initialise Sesame HTTP repository with URL=" + repositoryUrl);
             }
         }
     }
     
     public static Configuration getNewTemplateConfiguration(final Context newChildContext)
     {
         final Configuration result = new Configuration();
         result.setDefaultEncoding("UTF-8");
         result.setURLEscapingCharset("UTF-8");
         
         // FIXME: Make this configurable
         result.setTemplateLoader(new ContextTemplateLoader(newChildContext, "clap://class/templates"));
         
         final BeansWrapper myWrapper = new BeansWrapper();
         myWrapper.setSimpleMapWrapper(true);
         result.setObjectWrapper(myWrapper);
         
         return result;
     }
     
     public static void setupApplication(final PoddWebServiceApplication application, final Context applicationContext)
         throws OpenRDFException
     {
         final PropertyUtil props = application.getPropertyUtil();
         
         ApplicationUtils.log.debug("application {}", application);
         ApplicationUtils.log.debug("applicationContext {}", applicationContext);
         
         final List<Role> roles = application.getRoles();
         roles.clear();
         roles.addAll(PoddRoles.getRoles());
         
         final Repository nextRepository = ApplicationUtils.getNewRepository(props);
         
         application.setPoddRepositoryManager(new PoddRepositoryManagerImpl(nextRepository));
         application.getPoddRepositoryManager().setSchemaManagementGraph(PoddWebServiceApplicationImpl.SCHEMA_MGT_GRAPH);
         application.getPoddRepositoryManager().setArtifactManagementGraph(
                 PoddWebServiceApplicationImpl.ARTIFACT_MGT_GRAPH);
         
         // File Reference manager
         final DataReferenceProcessorRegistry nextFileRegistry = new DataReferenceProcessorRegistry();
         // clear any automatically added entries that may come from META-INF/services entries on the
         // classpath
         nextFileRegistry.clear();
         final DataReferenceProcessorFactory nextFileProcessorFactory = new SSHFileReferenceProcessorFactoryImpl();
         nextFileRegistry.add(nextFileProcessorFactory);
         
         // File Reference Manager
         final DataReferenceManager nextDataReferenceManager = new FileReferenceManagerImpl();
         nextDataReferenceManager.setDataProcessorRegistry(nextFileRegistry);
         
         // PURL manager
         final PoddPurlProcessorFactoryRegistry nextPurlRegistry = new PoddPurlProcessorFactoryRegistry();
         nextPurlRegistry.clear();
         final PoddPurlProcessorFactory nextPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
         
         final String purlPrefix = props.get(PoddWebConstants.PROPERTY_PURL_PREFIX, null);
         ((UUIDPurlProcessorFactoryImpl)nextPurlProcessorFactory).setPrefix(purlPrefix);
         
         nextPurlRegistry.add(nextPurlProcessorFactory);
         
         final PoddPurlManager nextPurlManager = new PoddPurlManagerImpl();
         nextPurlManager.setPurlProcessorFactoryRegistry(nextPurlRegistry);
         
         final PoddOWLManager nextOWLManager = new PoddOWLManagerImpl();
         nextOWLManager.setReasonerFactory(OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet"));
         final OWLOntologyManager nextOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         if(nextOWLOntologyManager == null)
         {
             ApplicationUtils.log.error("OWLOntologyManager was null");
         }
         nextOWLManager.setOWLOntologyManager(nextOWLOntologyManager);
         
         // File Repository Manager
         final PoddDataRepositoryManager nextDataRepositoryManager = new PoddFileRepositoryManagerImpl();
         nextDataRepositoryManager.setRepositoryManager(application.getPoddRepositoryManager());
         nextDataRepositoryManager.setOWLManager(nextOWLManager);
         try
         {
             final Model aliasConfiguration = application.getAliasesConfiguration(props);
             nextDataRepositoryManager.init(aliasConfiguration);
         }
         catch(PoddException | IOException e)
         {
             ApplicationUtils.log.error("Fatal Error!!! Could not initialize File Repository Manager", e);
         }
         
         application.setPoddDataRepositoryManager(nextDataRepositoryManager);
         
         final PoddSesameManager poddSesameManager = new PoddSesameManagerImpl();
         
         application.setPoddSchemaManager(new PoddSchemaManagerImpl());
         application.getPoddSchemaManager().setOwlManager(nextOWLManager);
         application.getPoddSchemaManager().setRepositoryManager(application.getPoddRepositoryManager());
         application.getPoddSchemaManager().setSesameManager(poddSesameManager);
         
         application.setPoddArtifactManager(new PoddArtifactManagerImpl());
         application.getPoddArtifactManager().setRepositoryManager(application.getPoddRepositoryManager());
         application.getPoddArtifactManager().setDataReferenceManager(nextDataReferenceManager);
         application.getPoddArtifactManager().setDataRepositoryManager(nextDataRepositoryManager);
         application.getPoddArtifactManager().setPurlManager(nextPurlManager);
         application.getPoddArtifactManager().setOwlManager(nextOWLManager);
         application.getPoddArtifactManager().setSchemaManager(application.getPoddSchemaManager());
         application.getPoddArtifactManager().setSesameManager(poddSesameManager);
         
         /*
          * Since the schema ontology upload feature is not yet supported, necessary schemas are
          * uploaded here at application starts up.
          */
         try
         {
             final String schemaManifest =
                     props.get(PoddRdfConstants.KEY_SCHEMAS, PoddRdfConstants.PATH_DEFAULT_SCHEMAS);
             Model model = null;
             
             try (final InputStream schemaManifestStream = application.getClass().getResourceAsStream(schemaManifest);)
             {
                 final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
                 model = Rio.parse(schemaManifestStream, "", format);
             }
             
             application.getPoddSchemaManager().uploadSchemaOntologies(model);
             
             // TODO: Use a manifest file to load up the current versions here
             /*
              * application.getPoddSchemaManager().uploadSchemaOntology(
              * ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_DCTERMS),
              * RDFFormat.RDFXML); application.getPoddSchemaManager().uploadSchemaOntology(
              * ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_FOAF),
              * RDFFormat.RDFXML); application.getPoddSchemaManager().uploadSchemaOntology(
              * ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_USER),
              * RDFFormat.RDFXML); application.getPoddSchemaManager().uploadSchemaOntology(
              * ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE),
              * RDFFormat.RDFXML); application.getPoddSchemaManager().uploadSchemaOntology(
              * ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_SCIENCE),
              * RDFFormat.RDFXML); application.getPoddSchemaManager().uploadSchemaOntology(
              * ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_PLANT),
              * RDFFormat.RDFXML);
              */
             // Enable the following for debugging
             // dumpSchemaGraph(application, nextRepository);
         }
         catch(IOException | OpenRDFException | OWLException | PoddException e)
         {
             ApplicationUtils.log.error("Fatal Error!!! Could not load schema ontologies", e);
         }
         
         // OasMemoryRealm has extensions so that getClientInfo().getUser() will contain first name,
         // last name, and email address as necessary
         final PoddSesameRealmImpl nextRealm =
                 new PoddSesameRealmImpl(nextRepository, PoddRdfConstants.DEFAULT_USER_MANAGEMENT_GRAPH);
         
         // FIXME: Make this configurable
         nextRealm.setName("PODDRealm");
         
         // Check if there is a current admin, and only add our test admin user if there is no admin
         // in the system
         boolean foundCurrentAdmin = false;
         for(RestletUtilUser nextUser : nextRealm.getUsers())
         {
             if(nextRealm.findRoles(nextUser).contains(PoddRoles.ADMIN.getRole()))
             {
                 foundCurrentAdmin = true;
                 break;
             }
         }
         
         if(!foundCurrentAdmin)
         {
             final URI testAdminUserHomePage = PoddRdfConstants.VF.createURI("http://www.example.com/testAdmin");
             String username =
                     props.get(PoddWebConstants.PROPERTY_INITIAL_ADMIN_USERNAME,
                             PoddWebConstants.DEFAULT_INITIAL_ADMIN_USERNAME);
             char[] password =
                     props.get(PoddWebConstants.PROPERTY_INITIAL_ADMIN_PASSWORD,
                             PoddWebConstants.DEFAULT_INITIAL_ADMIN_PASSWORD).toCharArray();
             final PoddUser testAdminUser =
                     new PoddUser(username, password, "Initial Admin", "User", "initial.admin.user@example.com",
                             PoddUserStatus.ACTIVE, testAdminUserHomePage, "Local Organisation", "Dummy-ORCID");
             final URI testAdminUserUri = nextRealm.addUser(testAdminUser);
             nextRealm.map(testAdminUser, PoddRoles.ADMIN.getRole());
             
             final Set<Role> testAdminUserRoles = nextRealm.findRoles(testAdminUser);
             
             ApplicationUtils.log.debug("testAdminUserRoles: {}, {}", testAdminUserRoles, testAdminUserRoles.size());
             
             // FIXME: Should put the application in maintenance mode at this point (when that is
             // supported), to require password/username change before opening up to other users
             
         }
         // final User findUser = nextRealm.findUser("testAdminUser");
         
         // ApplicationUtils.log.debug("findUser: {}", findUser);
         // ApplicationUtils.log.debug("findUser.getFirstName: {}", findUser.getFirstName());
         // ApplicationUtils.log.debug("findUser.getLastName: {}", findUser.getLastName());
         // ApplicationUtils.log.debug("findUser.getName: {}", findUser.getName());
         // ApplicationUtils.log.debug("findUser.getIdentifier: {}", findUser.getIdentifier());
         
         // TODO: Define groups here also
         
         // final MapVerifier verifier = new MapVerifier();
         // final ConcurrentHashMap<String, char[]> hardcodedLocalSecrets = new
         // ConcurrentHashMap<String, char[]>();
         // hardcodedLocalSecrets.put("testUser", "testPassword".toCharArray());
         // verifier.setLocalSecrets(hardcodedLocalSecrets);
         
         // final Context authenticatorChildContext = applicationContext.createChildContext();
         final ChallengeAuthenticator newAuthenticator =
                 ApplicationUtils.getNewAuthenticator(nextRealm, applicationContext, props);
         application.setAuthenticator(newAuthenticator);
         
         application.setRealm(nextRealm);
         
         // TODO: Is this necessary?
         // FIXME: Is this safe?
         // applicationContext.setDefaultVerifier(newAuthenticator.getVerifier());
         // applicationContext.setDefaultEnroler(newAuthenticator.getEnroler());
         
         // applicationContext.setDefaultVerifier(nextRealm.getVerifier());
         // applicationContext.setDefaultEnroler(nextRealm.getEnroler());
         
         // final Context templateChildContext = applicationContext.createChildContext();
         final Configuration newTemplateConfiguration = ApplicationUtils.getNewTemplateConfiguration(applicationContext);
         application.setTemplateConfiguration(newTemplateConfiguration);
         
         // create a custom error handler using our overridden PoddStatusService together with the
         // freemarker configuration
         final PoddStatusService statusService = new PoddStatusService(newTemplateConfiguration);
         application.setStatusService(statusService);
     }
     
     private ApplicationUtils()
     {
     }
     
 }
