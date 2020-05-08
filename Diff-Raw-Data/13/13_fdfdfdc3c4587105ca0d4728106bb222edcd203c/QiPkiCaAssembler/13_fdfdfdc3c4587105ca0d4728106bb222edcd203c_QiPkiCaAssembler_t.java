 /*
  * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.codeartisans.qipki.ca;
 
 import org.codeartisans.qipki.ca.application.contexts.CAContext;
 import org.codeartisans.qipki.ca.application.contexts.RootContext;
 import org.codeartisans.qipki.ca.domain.ca.CAEntity;
 import org.codeartisans.qipki.ca.domain.ca.CAFactory;
 import org.codeartisans.qipki.ca.domain.ca.CARepository;
 import org.codeartisans.qipki.ca.domain.crypto.KeyStoreEntity;
 import org.codeartisans.qipki.ca.presentation.http.HttpService;
 import org.codeartisans.qipki.ca.presentation.http.RootServletService;
 import org.codeartisans.qipki.ca.presentation.rest.RestletApplication;
 import org.codeartisans.qipki.ca.presentation.rest.RestletFinder;
 import org.codeartisans.qipki.ca.presentation.rest.RestletServletServerService;
 import org.codeartisans.qipki.ca.presentation.rest.resources.ApiRootResource;
 import org.codeartisans.qipki.ca.presentation.rest.resources.RAResource;
 import org.codeartisans.qipki.ca.presentation.rest.RestValuesFactory;
 import org.codeartisans.qipki.ca.presentation.rest.resources.CAsResource;
 import org.codeartisans.qipki.ca.presentation.rest.resources.CAResource;
 import org.codeartisans.qipki.ca.presentation.rest.resources.PKCS10SignerResource;
 import org.codeartisans.qipki.commons.values.CAValue;
 import org.codeartisans.qipki.commons.rest.RestListValue;
 import org.codeartisans.qipki.core.crypto.CryptGEN;
 import org.codeartisans.qipki.core.crypto.CryptIO;
 import org.qi4j.api.common.Visibility;
 import org.qi4j.bootstrap.ApplicationAssembler;
 import org.qi4j.bootstrap.ApplicationAssembly;
 import org.qi4j.bootstrap.ApplicationAssemblyFactory;
 import org.qi4j.bootstrap.Assembler;
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.LayerAssembly;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.entitystore.memory.MemoryEntityStoreService;
 import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
 import org.qi4j.library.http.JettyConfiguration;
 import static org.qi4j.library.http.Dispatchers.Dispatcher.*;
 import static org.qi4j.library.http.Servlets.*;
 import org.qi4j.library.http.UnitOfWorkFilterService;
 import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
 
 /**
 * TODO Dependre de qi4j-extension-rest et retirer le code duplique --> l'assembly y est pourrie il faudra la fixer avant tout
 * TODO Trouver ou stocker les parametres d'assembly (une mini qi4j app qui se passivate apres ? -> permet les hot restarts)
 * TODO Ajouter un parametre d'assembly pour activer ou pas les endpoints REST entity & finder
  */
 public class QiPkiCaAssembler
         implements ApplicationAssembler {
 
     @Override
     public final ApplicationAssembly assemble(ApplicationAssemblyFactory applicationFactory)
             throws AssemblyException {
 
         ApplicationAssembly app = applicationFactory.newApplicationAssembly();
 
         app.setName("QiPKIServer");
         app.setVersion("1.0.0-SNAPSHOT");
 
         LayerAssembly presentation = app.layerAssembly("presentation");
         {
             new Assembler() // REST
             {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addObjects(RestletApplication.class).
                             visibleIn(Visibility.layer);
 
                     module.addObjects(RestletFinder.class,
                             ApiRootResource.class,
                             RAResource.class,
                             CAsResource.class,
                             CAResource.class,
                             PKCS10SignerResource.class).
                             visibleIn(Visibility.module);
 
                     module.addValues(RestListValue.class,
                             CAValue.class);
 
                     module.addServices(RestValuesFactory.class).
                             visibleIn(Visibility.module);
 
                     addServlets(serve("/api/*").with(RestletServletServerService.class)).
                             to(module);
 
                     addFilters(filter("/api/*").through(UnitOfWorkFilterService.class).
                             on(REQUEST)).
                             to(module);
                 }
             }.assemble(presentation.moduleAssembly("rest"));
 
             new Assembler() // Http Service
             {
 
                 // NOTE Servlets are added with layer visibility
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addServices(HttpService.class).
                             visibleIn(Visibility.module).
                             instantiateOnStartup();
 
                     addServlets(serve("/").with(RootServletService.class)).
                             to(module);
                 }
             }.assemble(presentation.moduleAssembly("http"));
 
             new Assembler() // UI Configuration
             {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addServices(MemoryEntityStoreService.class).
                             visibleIn(Visibility.layer);
 
                     module.addEntities(JettyConfiguration.class).
                             visibleIn(Visibility.layer);
                 }
             }.assemble(presentation.moduleAssembly("config"));
 
         }
 
         LayerAssembly application = app.layerAssembly("application");
         {
             new Assembler() {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addObjects(RootContext.class,
                             CAContext.class).
                             visibleIn(Visibility.application);
                 }
             }.assemble(application.moduleAssembly("dci"));
         }
 
         LayerAssembly domain = app.layerAssembly("domain");
         {
             new Assembler() // RequestAuthority
             {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                 }
             }.assemble(domain.moduleAssembly("ra"));
 
             new Assembler() // CertificateAuthority
             {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addEntities(CAEntity.class);
                     module.addServices(CARepository.class,
                             CAFactory.class).
                             visibleIn(Visibility.application);
                 }
             }.assemble(domain.moduleAssembly("ca"));
 
             new Assembler() // Base crypto domain
             {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addEntities(KeyStoreEntity.class).
                             visibleIn(Visibility.layer);
                     module.addTransients(CryptIO.class,
                             CryptGEN.class).
                             visibleIn(Visibility.application);
                 }
             }.assemble(domain.moduleAssembly("crypto"));
         }
 
         LayerAssembly infrastructure = app.layerAssembly("infrastructure");
         {
             new Assembler() // Store
             {
 
                 @Override
                 public void assemble(ModuleAssembly module)
                         throws AssemblyException {
                     module.addServices(MemoryEntityStoreService.class,
                             UuidIdentityGeneratorService.class).
                             visibleIn(Visibility.application);
                     new RdfMemoryStoreAssembler(null, Visibility.application, Visibility.application).assemble(module);
                 }
             }.assemble(infrastructure.moduleAssembly("store"));
         }
 
         LayerAssembly development = app.layerAssembly("development");
         {
             assembleDevTestModule(development.moduleAssembly("test"));
         }
 
         presentation.uses(application);
         presentation.uses(domain);
         application.uses(domain);
         development.uses(domain);
         domain.uses(infrastructure);
 
         return app;
     }
 
     protected void assembleDevTestModule(ModuleAssembly test)
             throws AssemblyException {
     }
 }
