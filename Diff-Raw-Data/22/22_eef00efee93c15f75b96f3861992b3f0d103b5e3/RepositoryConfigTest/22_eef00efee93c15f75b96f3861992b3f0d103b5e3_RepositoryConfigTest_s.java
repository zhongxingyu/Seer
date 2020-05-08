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
 package com.github.podd.utils.test;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.util.GraphUtil;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.config.RepositoryConfig;
 import org.openrdf.repository.config.RepositoryConfigSchema;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 
 /**
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class RepositoryConfigTest
 {
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
     }
     
     @Test
     public final void testParsing() throws Exception
     {
         final Model graph =
                 Rio.parse(this.getClass().getResourceAsStream("/memorystoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
         
         // Assert.assertTrue("Did not create a SailRepositoryConfig object",
         // repositoryConfig instanceof SailRepositoryConfig);
     }
     
 }
