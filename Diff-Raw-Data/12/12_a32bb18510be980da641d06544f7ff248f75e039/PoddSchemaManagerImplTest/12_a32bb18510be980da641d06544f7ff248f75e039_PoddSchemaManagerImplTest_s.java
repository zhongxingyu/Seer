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
 package com.github.podd.impl.test;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.model.UnloadableImportException;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
 
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.api.test.AbstractPoddSchemaManagerTest;
 import com.github.podd.impl.PoddOWLManagerImpl;
 import com.github.podd.impl.PoddRepositoryManagerImpl;
 import com.github.podd.impl.PoddSchemaManagerImpl;
 import com.github.podd.impl.PoddSesameManagerImpl;
 import com.github.podd.utils.PODD;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PoddSchemaManagerImplTest extends AbstractPoddSchemaManagerTest
 {
     @Override
     protected OWLOntologyManagerFactory getNewOWLOntologyManagerFactory()
     {
         Collection<OWLOntologyManagerFactory> ontologyManagers =
                 OWLOntologyManagerFactoryRegistry.getInstance().get(PoddWebConstants.DEFAULT_OWLAPI_MANAGER);
         
         if(ontologyManagers == null || ontologyManagers.isEmpty())
         {
             this.log.error("OWLOntologyManagerFactory was not found");
         }
         return ontologyManagers.iterator().next();
     }
     
     @Override
     protected PoddOWLManager getNewPoddOwlManagerInstance(final OWLOntologyManagerFactory manager,
             final OWLReasonerFactory reasonerFactory)
     {
         return new PoddOWLManagerImpl(manager, reasonerFactory);
     }
     
     @Override
     protected PoddRepositoryManager getNewPoddRepositoryManagerInstance()
     {
         return new PoddRepositoryManagerImpl();
     }
     
     @Override
     protected PoddSchemaManager getNewPoddSchemaManagerInstance()
     {
         return new PoddSchemaManagerImpl();
     }
     
     @Override
     protected PoddSesameManager getNewPoddSesameManagerInstance()
     {
         return new PoddSesameManagerImpl();
     }
     
     @Override
     protected OWLReasonerFactory getNewReasonerFactory()
     {
         return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
     }
     
     @Test
     public void testUploadSchemaOntologiesInOrder() throws Exception
     {
         // prepare: Model containing schema-manifest
         final String schemaManifest = "/test/schema-manifest-a1b2c3.ttl";
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         
         // prepare: order of imports
         final String[] testImportOrderArray =
                 { "http://example.org/podd/ns/version/poddA/1", "http://example.org/podd/ns/version/poddB/2",
                         "http://example.org/podd/ns/version/poddB/1", "http://example.org/podd/ns/version/poddC/3",
                         "http://example.org/podd/ns/version/poddC/1", };
         
         final List<URI> testImportOrder = new ArrayList<>();
         for(final String s : testImportOrderArray)
         {
             testImportOrder.add(PODD.VF.createURI(s));
         }
         
         ((PoddSchemaManagerImpl)this.testSchemaManager).uploadSchemaOntologiesInOrder(model, testImportOrder);
         
         // verify: schemas successfully loaded
         Assert.assertEquals("Expected 3 current schemas", 3, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals("Expected 5 schema ontology versions", 5, this.testSchemaManager.getSchemaOntologies()
                 .size());
     }
     
     @Test
     public void testUploadSchemaOntologiesInOrderRepeat() throws Exception
     {
         // prepare: Model containing schema-manifest
         final String schemaManifest = "/test/schema-manifest-a1b2c3.ttl";
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         
         // prepare: order of imports
         final String[] testImportOrderArray =
                 { "http://example.org/podd/ns/version/poddA/1", "http://example.org/podd/ns/version/poddB/2",
                         "http://example.org/podd/ns/version/poddB/1", "http://example.org/podd/ns/version/poddC/3",
                         "http://example.org/podd/ns/version/poddC/1", };
         
         final List<URI> testImportOrder = new ArrayList<>();
         for(final String s : testImportOrderArray)
         {
             testImportOrder.add(PODD.VF.createURI(s));
         }
         
         ((PoddSchemaManagerImpl)this.testSchemaManager).uploadSchemaOntologiesInOrder(model, testImportOrder);
         
         // verify: schemas successfully loaded
         Assert.assertEquals("Expected 3 current schemas", 3, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals("Expected 5 schema ontology versions", 5, this.testSchemaManager.getSchemaOntologies()
                 .size());
         
         ((PoddSchemaManagerImpl)this.testSchemaManager).uploadSchemaOntologiesInOrder(model, testImportOrder);
         
         // verify: schemas in memory not modified
         Assert.assertEquals("Expected 3 current schemas", 3, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals("Expected 5 schema ontology versions", 5, this.testSchemaManager.getSchemaOntologies()
                 .size());
     }
     
     @Test
     public void testUploadSchemaOntologiesInOrderInvalid() throws Exception
     {
         // prepare: Model containing schema-manifest
         final String schemaManifest = "/test/schema-manifest-a1b2c3.ttl";
         Model model = null;
         try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
         {
             final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
             model = Rio.parse(schemaManifestStream, "", format);
         }
         
         // prepare: order of imports
         // NOTE: C/1 needs B/1 to be loaded!
         final String[] testImportOrderArray = {
         
         "http://example.org/podd/ns/version/poddA/1",
         
         "http://example.org/podd/ns/version/poddB/2",
         
         "http://example.org/podd/ns/version/poddC/1",
         
         "http://example.org/podd/ns/version/poddB/1",
         
         "http://example.org/podd/ns/version/poddC/3",
         
         };
         
         final List<URI> testImportOrder = new ArrayList<>(testImportOrderArray.length);
         for(final String s : testImportOrderArray)
         {
             testImportOrder.add(PODD.VF.createURI(s));
         }
         
         try
         {
             ((PoddSchemaManagerImpl)this.testSchemaManager).uploadSchemaOntologiesInOrder(model, testImportOrder);
             Assert.fail("Should have failed loading due to incorrect import order");
         }
         catch(final UnloadableImportException e)
         {
             Assert.assertTrue("Not the expected error message",
                     e.getMessage().contains("http://example.org/podd/ns/version/poddB/1"));
         }
         
         // verify: no schemas loaded at all if any of the bulk upload failed, to ensure that we
         // don't have partial schema updates
         Assert.assertEquals("Expected 0 current schemas", 0, this.testSchemaManager.getCurrentSchemaOntologies().size());
         Assert.assertEquals("Expected 0 schema ontology versions", 0, this.testSchemaManager.getSchemaOntologies()
                 .size());
     }
     
 }
