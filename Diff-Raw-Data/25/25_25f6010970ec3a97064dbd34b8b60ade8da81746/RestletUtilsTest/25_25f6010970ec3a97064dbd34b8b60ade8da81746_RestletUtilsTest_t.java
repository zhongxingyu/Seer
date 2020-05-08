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
 package com.github.podd.restlet.test;
 
 import java.util.Collection;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.model.BNode;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.RDF;
 
 import com.github.ansell.restletutils.RestletUtilRole;
 import com.github.ansell.restletutils.SesameRealmConstants;
 import com.github.podd.restlet.RestletUtils;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddRoles;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class RestletUtilsTest
 {
     
     private ValueFactory vf;
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         vf = PoddRdfConstants.VF;
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#extractRoleMappings(org.openrdf.model.Model)}.
      */
     @Test
     public void testExtractRoleMappingsEmpty()
     {
        final Map<RestletUtilRole, Collection<URI>> roleMappings = PoddRoles.extractRoleMappings(new LinkedHashModel());
         
         Assert.assertTrue(roleMappings.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#extractRoleMappings(org.openrdf.model.Model)}.
      */
     @Test
     public void testExtractRoleMappingsRepositoryNoObject()
     {
         LinkedHashModel model = new LinkedHashModel();
         BNode resource = vf.createBNode();
         model.add(resource, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         model.add(resource, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
         
        final Map<RestletUtilRole, Collection<URI>> roleMappings = PoddRoles.extractRoleMappings(model);
         Assert.assertFalse(roleMappings.isEmpty());
         
         Assert.assertEquals(1, roleMappings.size());
         Assert.assertTrue(roleMappings.containsKey(PoddRoles.ADMIN));
         Assert.assertEquals(1, roleMappings.get(PoddRoles.ADMIN).size());
         Assert.assertTrue(roleMappings.get(PoddRoles.ADMIN).contains(null));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#extractRoleMappings(org.openrdf.model.Model)}.
      */
     @Test
     public void testExtractRoleMappingsProjectNoObject()
     {
         LinkedHashModel model = new LinkedHashModel();
         BNode resource = vf.createBNode();
         model.add(resource, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         model.add(resource, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_ADMIN.getURI());
         
        final Map<RestletUtilRole, Collection<URI>> roleMappings = PoddRoles.extractRoleMappings(model);
         Assert.assertFalse(roleMappings.isEmpty());
         
         Assert.assertEquals(1, roleMappings.size());
         Assert.assertTrue(roleMappings.containsKey(PoddRoles.PROJECT_ADMIN));
         Assert.assertEquals(1, roleMappings.get(PoddRoles.PROJECT_ADMIN).size());
         Assert.assertTrue(roleMappings.get(PoddRoles.PROJECT_ADMIN).contains(null));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#extractRoleMappings(org.openrdf.model.Model)}.
      */
     @Test
     public void testExtractRoleMappingsRepositoryWithObject()
     {
         LinkedHashModel model = new LinkedHashModel();
         BNode resource = vf.createBNode();
         URI objectUri = vf.createURI("urn:test:object:uri");
         model.add(resource, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         model.add(resource, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
         model.add(resource, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, objectUri);
         
        final Map<RestletUtilRole, Collection<URI>> roleMappings = PoddRoles.extractRoleMappings(model);
         Assert.assertFalse(roleMappings.isEmpty());
         
         Assert.assertEquals(1, roleMappings.size());
         Assert.assertTrue(roleMappings.containsKey(PoddRoles.ADMIN));
         Assert.assertEquals(1, roleMappings.get(PoddRoles.ADMIN).size());
         Assert.assertTrue(roleMappings.get(PoddRoles.ADMIN).contains(objectUri));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#extractRoleMappings(org.openrdf.model.Model)}.
      */
     @Test
     public void testExtractRoleMappingsProjectWithObject()
     {
         LinkedHashModel model = new LinkedHashModel();
         BNode resource = vf.createBNode();
         URI objectUri = vf.createURI("urn:test:object:uri");
         model.add(resource, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         model.add(resource, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_ADMIN.getURI());
         model.add(resource, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, objectUri);
         
        final Map<RestletUtilRole, Collection<URI>> roleMappings = PoddRoles.extractRoleMappings(model);
         Assert.assertFalse(roleMappings.isEmpty());
         
         Assert.assertEquals(1, roleMappings.size());
         Assert.assertTrue(roleMappings.containsKey(PoddRoles.PROJECT_ADMIN));
         Assert.assertEquals(1, roleMappings.get(PoddRoles.PROJECT_ADMIN).size());
         Assert.assertTrue(roleMappings.get(PoddRoles.PROJECT_ADMIN).contains(objectUri));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#getBaseDataModel(org.restlet.Request)}.
      */
     @Ignore
     @Test
     public void testGetBaseDataModel()
     {
         Assert.fail("Not yet implemented");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#getBooleanFromParameter(org.restlet.data.Parameter)}
      * .
      */
     @Ignore
     @Test
     public void testGetBooleanFromParameter()
     {
         Assert.fail("Not yet implemented");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#getHtmlRepresentation(java.lang.String, java.util.Map, org.restlet.data.MediaType, freemarker.template.Configuration)}
      * .
      */
     @Ignore
     @Test
     public void testGetHtmlRepresentation()
     {
         Assert.fail("Not yet implemented");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.restlet.RestletUtils#toRDFSerialisation(java.lang.String, org.openrdf.repository.Repository, org.openrdf.model.Resource[])}
      * .
      */
     @Ignore
     @Test
     public void testToRDFSerialisation()
     {
         Assert.fail("Not yet implemented");
     }
     
 }
