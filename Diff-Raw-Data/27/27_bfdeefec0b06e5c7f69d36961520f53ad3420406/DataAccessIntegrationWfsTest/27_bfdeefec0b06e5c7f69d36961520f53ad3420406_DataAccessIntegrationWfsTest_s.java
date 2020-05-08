 /* 
  * Copyright (c) 2001 - 2009 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 
 package org.geoserver.test;
 
 import junit.framework.Test;
 
 import org.w3c.dom.Document;
 
 /**
  * WFS Test for DataAccessIntegrationTest in Geotools/app-schema.
  * {@link org.geotools.data.complex.DataAccessIntegrationTest} Adapted from FeatureChainingWfsTest.
  * 
  * @author Rini Angreani, Curtin University of Technology
  * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
  */
 public class DataAccessIntegrationWfsTest extends AbstractAppSchemaWfsTestSupport {
 
     /**
      * Read-only test so can use one-time setup.
      * 
      * @return
      */
     public static Test suite() {
         return new OneTimeTestSetup(new DataAccessIntegrationWfsTest());
     }
 
     @Override
     protected NamespaceTestData buildTestData() {
         return new DataAccessIntegrationMockData();
     }
 
     /**
      * Test whether GetCapabilities returns wfs:WFS_Capabilities.
      */
     public void testDescribeFeatureType() {
         Document doc = getAsDOM("wfs?request=DescribeFeatureType&typename=gsml:GeologicUnit");
         LOGGER.info("WFS DescribeFeatureType response:\n" + prettyString(doc));
         assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
     }
 
     /**
      * Test whether DescribeFeatureType returns xsd:schema.
      */
     public void testGetCapabilities() {
         Document doc = getAsDOM("wfs?request=GetCapabilities");
         LOGGER.info("WFS GetCapabilities response:\n" + prettyString(doc));
         assertEquals("wfs:WFS_Capabilities", doc.getDocumentElement().getNodeName());
     }
 
     /**
      * Test whether GetFeature returns wfs:FeatureCollection.
      */
     public void testGetFeature() {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:GeologicUnit");
         LOGGER.info("WFS GetFeature response:\n" + prettyString(doc));
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
     }
 
     /**
      * Test content of GetFeature response.
      */
     public void testGetFeatureContent() {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:GeologicUnit");
         assertXpathCount(3, "//gsml:GeologicUnit", doc);
 
         // GU:25699
        assertXpathEvaluatesTo("er.25699", "//gsml:GeologicUnit[@gml:id='25699']/gml:name", doc);
        assertXpathCount(0, "//gsml:GeologicUnit/FEATURE_LINK", doc);
         assertXpathCount(1,
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:occurence/gsml:MappedFeature", doc);
         // mf1
         assertXpathEvaluatesTo("mf1",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:occurence/gsml:MappedFeature/@gml:id",
                 doc);
         assertXpathEvaluatesTo("GUNTHORPE FORMATION",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:occurence/gsml:MappedFeature/gml:name",
                 doc);
         assertXpathEvaluatesTo(
                 "mf1",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:occurence/gsml:MappedFeature/gml:name[2]",
                 doc);
         assertXpathEvaluatesTo(
                 "-1.2 52.5 -1.2 52.6 -1.1 52.6 -1.1 52.5 -1.2 52.5",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:occurence/gsml:MappedFeature/gsml:shape//gml:posList",
                 doc);
 
         assertXpathEvaluatesTo(
                 "strataform",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:bodyMorphology/gsml:CGI_TermValue/gsml:value",
                 doc);
         assertXpathEvaluatesTo(
                 "urn:cgi:classifierScheme:GSV:GeologicalUnitType",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:classifier/gsml:ControlledConcept/gsml:preferredName",
                 doc);
         assertXpathCount(3, "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition", doc);
         // cp.167775491936278844
         assertXpathEvaluatesTo("significant",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition/gsml:CompositionPart/gsml:proportion"
                         + "/gsml:CGI_TermValue/gsml:value", doc);
         assertXpathEvaluatesTo(
                 "interbedded component",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition/gsml:CompositionPart/gsml:role",
                 doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition/gsml:CompositionPart/FEATURE_LINK", doc);
         // cp.167775491936278812
         assertXpathEvaluatesTo("significant",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/gsml:proportion"
                         + "/gsml:CGI_TermValue/gsml:value", doc);
         assertXpathEvaluatesTo(
                 "interbedded component",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/gsml:role",
                 doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/FEATURE_LINK", doc);
         assertXpathEvaluatesTo("name_a",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/gsml:lithology"
                         + "/gsml:ControlledConcept/gml:name", doc);
         assertXpathEvaluatesTo("name_b",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/gsml:lithology"
                         + "/gsml:ControlledConcept/gml:name[2]", doc);
         assertXpathEvaluatesTo("name_c",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/gsml:lithology"
                         + "/gsml:ControlledConcept/gml:name[3]", doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition/gsml:CompositionPart/gsml:lithology/FEATURE_LINK", doc);
         assertXpathEvaluatesTo("name_2",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[2]/gsml:CompositionPart/gsml:lithology[2]"
                         + "/gsml:ControlledConcept/gml:name", doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology[2]/FEATURE_LINK", doc);
         // cp.167775491936278856
         assertXpathEvaluatesTo("minor",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[3]/gsml:CompositionPart/gsml:proportion"
                         + "/gsml:CGI_TermValue/gsml:value", doc);
         assertXpathEvaluatesTo(
                 "interbedded component",
                 "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[3]/gsml:CompositionPart/gsml:role",
                 doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25699']/gsml:composition[3]/gsml:CompositionPart/FEATURE_LINK", doc);
 
         // GU:25678
        assertXpathEvaluatesTo("er.25678", "//gsml:GeologicUnit[@gml:id='25678']/gml:name", doc);
        assertXpathCount(0, "//gsml:GeologicUnit/FEATURE_LINK", doc);
         assertXpathEvaluatesTo(
                 "vein",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:bodyMorphology/gsml:CGI_TermValue/gsml:value",
                 doc);
         assertXpathEvaluatesTo(
                 "urn:cgi:classifierScheme:GSV:GeologicalUnitType",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:classifier/gsml:ControlledConcept/gsml:preferredName",
                 doc);
         assertXpathCount(2,
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence/gsml:MappedFeature", doc);
         // mf2
         assertXpathEvaluatesTo("mf2", "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence"
                 + "/gsml:MappedFeature/@gml:id", doc);
         assertXpathEvaluatesTo("MERCIA MUDSTONE GROUP",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence/gsml:MappedFeature/gml:name",
                 doc);
         assertXpathEvaluatesTo(
                 "mf2",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence/gsml:MappedFeature/gml:name[2]",
                 doc);
         assertXpathEvaluatesTo(
                 "-1.3 52.5 -1.3 52.6 -1.2 52.6 -1.2 52.5 -1.3 52.5",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence/gsml:MappedFeature/gsml:shape//gml:posList",
                 doc);
         // mf3
         assertXpathEvaluatesTo(
                 "mf3",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence[2]/gsml:MappedFeature/@gml:id",
                 doc);
         assertXpathEvaluatesTo(
                 "CLIFTON FORMATION",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence[2]/gsml:MappedFeature/gml:name",
                 doc);
         assertXpathEvaluatesTo(
                 "mf3",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence[2]/gsml:MappedFeature/gml:name[2]",
                 doc);
         assertXpathEvaluatesTo(
                 "-1.2 52.5 -1.2 52.6 -1.1 52.6 -1.1 52.5 -1.2 52.5",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:occurence[2]/gsml:MappedFeature/gsml:shape//gml:posList",
                 doc);
 
         assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='25678']" + "/gsml:composition", doc);
         // cp.167775491936278856
         assertXpathEvaluatesTo("minor",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:composition/gsml:CompositionPart/gsml:proportion"
                         + "/gsml:CGI_TermValue/gsml:value", doc);
         assertXpathEvaluatesTo(
                 "interbedded component",
                 "//gsml:GeologicUnit[@gml:id='25678']/gsml:composition/gsml:CompositionPart/gsml:role",
                 doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25678']/gsml:composition/gsml:CompositionPart/FEATURE_LINK", doc);
 
         // GU:25682
        assertXpathEvaluatesTo("er.25682", "//gsml:GeologicUnit[@gml:id='25682']/gml:name", doc);
        assertXpathCount(0, "//gsml:GeologicUnit/FEATURE_LINK", doc);
         assertXpathEvaluatesTo(
                 "cross-cutting",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:bodyMorphology/gsml:CGI_TermValue/gsml:value",
                 doc);
         assertXpathEvaluatesTo(
                 "urn:cgi:classifierScheme:GSV:GeologicalUnitType",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:classifier/gsml:ControlledConcept/gsml:preferredName",
                 doc);
         assertXpathCount(1,
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:occurence/gsml:MappedFeature", doc);
         // mf4
         assertXpathEvaluatesTo("mf4",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:occurence/gsml:MappedFeature/@gml:id",
                 doc);
         assertXpathEvaluatesTo("MURRADUC BASALT",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:occurence/gsml:MappedFeature/gml:name",
                 doc);
         assertXpathEvaluatesTo(
                 "mf4",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:occurence/gsml:MappedFeature/gml:name[2]",
                 doc);
         assertXpathEvaluatesTo(
                 "-1.3 52.5 -1.3 52.6 -1.2 52.6 -1.2 52.5 -1.3 52.5",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:occurence/gsml:MappedFeature/gsml:shape//gml:posList",
                 doc);
 
         assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='25682']" + "/gsml:composition", doc);
         // cp.167775491936278812
         assertXpathEvaluatesTo("significant",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:proportion"
                         + "/gsml:CGI_TermValue/gsml:value", doc);
         assertXpathEvaluatesTo(
                 "interbedded component",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:role",
                 doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/FEATURE_LINK", doc);
         assertXpathEvaluatesTo("name_a",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology"
                         + "/gsml:ControlledConcept/gml:name", doc);
         assertXpathEvaluatesTo("name_b",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology"
                         + "/gsml:ControlledConcept/gml:name[2]", doc);
         assertXpathEvaluatesTo("name_c",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology"
                         + "/gsml:ControlledConcept/gml:name[3]", doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology/FEATURE_LINK", doc);
         assertXpathEvaluatesTo("name_2",
                 "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology[2]"
                         + "/gsml:ControlledConcept/gml:name", doc);
         assertXpathCount(0, "//gsml:GeologicUnit[@gml:id='25682']/gsml:composition/gsml:CompositionPart/gsml:lithology[2]/FEATURE_LINK", doc);
 
     }
 
 }
