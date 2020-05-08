 /*
  * Copyright (c) 2001 - 2009 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 
 package org.geoserver.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.Test;
 import org.geotools.data.complex.AppSchemaDataAccess;
 import org.geotools.gml3.GML;
 import org.geotools.wfs.v1_1.WFS;
 import org.w3c.dom.Document;
 
 /**
  * WFS GetFeature to test integration of {@link AppSchemaDataAccess} with GeoServer.
  * 
  * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
  * @author Rini Angreani, Curtin University of Technology
  */
 public class FeatureChainingWfsTest extends AbstractAppSchemaWfsTestSupport {
     final String BASE_URL = "http://localhost:80/geoserver/";
 
     final String DESCRIBE_FEATURE_TYPE_BASE = BASE_URL
             + "wfs?request=DescribeFeatureType&version=1.1.0&service=WFS&typeName=";
 
     /**
      * Read-only test so can use one-time setup.
      * 
      * @return
      */
     public static Test suite() {
         return new OneTimeTestSetup(new FeatureChainingWfsTest());
     }
 
     @Override
     protected NamespaceTestData buildTestData() {
         return new FeatureChainingMockData();
     }
 
     /**
      * Test whether GetCapabilities returns wfs:WFS_Capabilities.
      */
     public void testGetCapabilities() {
         Document doc = getAsDOM("wfs?request=GetCapabilities");
         LOGGER.info("WFS GetCapabilities response:\n" + prettyString(doc));
         assertEquals("wfs:WFS_Capabilities", doc.getDocumentElement().getNodeName());
 
         // make sure non-feature types don't appear in FeatureTypeList
         assertXpathCount(4, "//wfs:FeatureType", doc);
         ArrayList<String> featureTypeNames = new ArrayList<String>(4);
         featureTypeNames.add(evaluate("//wfs:FeatureType[1]/wfs:Name", doc));
         featureTypeNames.add(evaluate("//wfs:FeatureType[2]/wfs:Name", doc));
         featureTypeNames.add(evaluate("//wfs:FeatureType[3]/wfs:Name", doc));
         featureTypeNames.add(evaluate("//wfs:FeatureType[4]/wfs:Name", doc));
         // Mapped Feture
         assertEquals(featureTypeNames.contains("gsml:MappedFeature"), true);
         // Geologic Unit
         assertEquals(featureTypeNames.contains("gsml:GeologicUnit"), true);
         // FirstParentFeature
         assertEquals(featureTypeNames.contains("ex:FirstParentFeature"), true);
         // SecondParentFeature
         assertEquals(featureTypeNames.contains("ex:SecondParentFeature"), true);
     }
 
     /**
      * Test whether DescribeFeatureType returns xsd:schema, and if the contents are correct. When no
      * type name specified, it should return imports for all name spaces involved. If type name is
      * specified, it should return imports of GML type and the type's top level schema.
      * 
      * @throws IOException
      */
     public void testDescribeFeatureType() throws IOException {
         File dataDir = this.getTestData().getDataDirectoryRoot();
 
         /**
          * gsml:MappedFeature
          */
         Document doc = getAsDOM("wfs?request=DescribeFeatureType&typename=gsml:MappedFeature");
         LOGGER.info("WFS DescribeFeatureType, typename=gsml:MappedFeature response:\n"
                 + prettyString(doc));
         assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
         // make sure the contents are only relevant imports
         assertXpathCount(2, "//xsd:import", doc);
         // GML import
         assertXpathEvaluatesTo(GML.NAMESPACE, "//xsd:import[1]/@namespace", doc);
         assertXpathEvaluatesTo(BASE_URL + "schemas/gml/3.1.1/base/gml.xsd",
                 "//xsd:import[1]/@schemaLocation", doc);
         // GSML import
         assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_URI, "//xsd:import[2]/@namespace",
                 doc);
         // GSML schemaLocation
         assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_SCHEMA_LOCATION_URL,
                 "//xsd:import[2]/@schemaLocation", doc);
         // nothing else
         assertXpathCount(0, "//xsd:complexType", doc);
         assertXpathCount(0, "//xsd:element", doc);
 
         /**
          * gsml:GeologicUnit
          */
         doc = getAsDOM("wfs?request=DescribeFeatureType&typename=gsml:GeologicUnit");
         LOGGER.info("WFS DescribeFeatureType, typename=gsml:GeologicUnit response:\n"
                 + prettyString(doc));
         assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
         // make sure the contents are only relevant imports
         assertXpathCount(2, "//xsd:import", doc);
         // GML import
         assertXpathEvaluatesTo(GML.NAMESPACE, "//xsd:import[1]/@namespace", doc);
         assertXpathEvaluatesTo(BASE_URL + "schemas/gml/3.1.1/base/gml.xsd",
                 "//xsd:import[1]/@schemaLocation", doc);
         // GSML import: a URL
         assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_URI, "//xsd:import[2]/@namespace",
                 doc);
         assertXpathEvaluatesTo("http://schemas.opengis.net/GeoSciML/geosciml.xsd",
                 "//xsd:import[2]/@schemaLocation", doc);
         // nothing else
         assertXpathCount(0, "//xsd:complexType", doc);
         assertXpathCount(0, "//xsd:element", doc);
 
         /**
          * ex:FirstParentFeature and ex:SecondParentFeature
          */
         doc = getAsDOM("wfs?request=DescribeFeatureType&typeName=ex:FirstParentFeature,ex:SecondParentFeature");
         LOGGER.info("WFS DescribeFeatureType, typename=ex:FirstParentFeature response:\n"
                 + prettyString(doc));
         assertXpathCount(2, "//xsd:import", doc);
         // GML import
         assertXpathEvaluatesTo(GML.NAMESPACE, "//xsd:import[1]/@namespace", doc);
         assertXpathEvaluatesTo(BASE_URL + "schemas/gml/3.1.1/base/gml.xsd",
                 "//xsd:import[1]/@schemaLocation", doc);
         // EX import
         assertXpathEvaluatesTo(FeatureChainingMockData.EX_URI, "//xsd:import[2]/@namespace", doc);
         File exSchema = findFile("featureTypes/ex_FirstParentFeature/simpleContent.xsd", dataDir);
         assertNotNull(exSchema);
         assertEquals(exSchema.exists(), true);
 
         assertXpathEvaluatesTo(exSchema.toURI().toString(), "//xsd:import[2]/@schemaLocation", doc);
         // nothing else
         assertXpathCount(0, "//xsd:import[3]", doc);
         assertXpathCount(0, "//xsd:complexType", doc);
         assertXpathCount(0, "//xsd:element", doc);
 
         /**
          * No type name specified
          */
         doc = getAsDOM("wfs?request=DescribeFeatureType");
         LOGGER.info("WFS DescribeFeatureType response:\n" + prettyString(doc));
         assertEquals("xsd:schema", doc.getDocumentElement().getNodeName());
         assertXpathCount(2, "//xsd:import", doc);
 
         // GSML import
         assertXpathEvaluatesTo(AbstractAppSchemaMockData.GSML_URI, "//xsd:import[1]/@namespace",
                 doc);
         // schemaLocation="http://localhost:80/geoserver/wfs?request=DescribeFeatureType;version=1.1.0;
         // service=WFS&amp;typeName=gsml:CGI_TermValue,gsml:CompositionPart,gsml:ControlledConcept,
         // gsml:GeologicUnit,gsml:MappedFeature"
         String schemaLocation = URLDecoder.decode(evaluate("//xsd:import[1]/@schemaLocation", doc), "ASCII");
         assertNotNull(schemaLocation);
         System.out.println(schemaLocation);
         assertEquals(schemaLocation.startsWith(DESCRIBE_FEATURE_TYPE_BASE), true);
 
         String[] typeNames = schemaLocation.substring(DESCRIBE_FEATURE_TYPE_BASE.length(),
                 schemaLocation.length()).split(",");
 
         File featureTypeDir = findFile("featureTypes", dataDir);
         assertNotNull(featureTypeDir);
         assertEquals(featureTypeDir.exists(), true);
 
         // get GSML feature types from data directory
         List<File> featureTypes = Arrays.asList(featureTypeDir.listFiles());
         ArrayList<String> gsmlTypes = new ArrayList<String>(featureTypes.size());
         for (File fType : featureTypes) {
             String folderName = fType.getName();
             if (folderName.startsWith(AbstractAppSchemaMockData.GSML_PREFIX)) {
                 folderName = folderName.replaceFirst("gsml_", "gsml:");
                 gsmlTypes.add(folderName);
             }
         }
         assertEquals(gsmlTypes.size(), typeNames.length);
         assertEquals(gsmlTypes.containsAll(Arrays.asList(typeNames)), true);
 
         // EX import
         assertXpathEvaluatesTo(FeatureChainingMockData.EX_URI, "//xsd:import[2]/@namespace", doc);
         schemaLocation = URLDecoder.decode(evaluate("//xsd:import[2]/@schemaLocation", doc), "ASCII");
         assertNotNull(schemaLocation);
         assertEquals(schemaLocation.startsWith(DESCRIBE_FEATURE_TYPE_BASE), true);
         typeNames = schemaLocation.substring(DESCRIBE_FEATURE_TYPE_BASE.length(),
                 schemaLocation.length()).split(",");
        // test we get back the two type names we expect
        // order is not guaranteed
         assertEquals(typeNames.length, 2);
        List<String> typeNamesList = Arrays.asList(typeNames);
        int firstIndex = typeNamesList.indexOf("ex:FirstParentFeature");
        int secondIndex = typeNamesList.indexOf("ex:SecondParentFeature");
        assertTrue(0 <= firstIndex && firstIndex <= 1) ;
        assertTrue(0 <= secondIndex && secondIndex <= 1);
        assertTrue(firstIndex != secondIndex);
         // nothing else
         assertXpathCount(0, "//xsd:import[3]", doc);
         assertXpathCount(0, "//xsd:complexType", doc);
         assertXpathCount(0, "//xsd:element", doc);
     }
 
     /**
      * Test whether GetFeature returns wfs:FeatureCollection.
      */
     public void testGetFeature() {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:MappedFeature");
         LOGGER.info("WFS GetFeature&typename=gsml:MappedFeature response:\n" + prettyString(doc));
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
         // non-feature type should return nothing/exception
         doc = getAsDOM("wfs?request=GetFeature&typename=gsml:CompositionPart");
         LOGGER.info("WFS GetFeature&typename=gsml:CompositionPart response, exception expected:\n"
                 + prettyString(doc));
         assertEquals("ows:ExceptionReport", doc.getDocumentElement().getNodeName());
     }
 
     /**
      * Test nesting features of complex types with simple content. Previously the nested features
      * attributes weren't encoded, so this is to ensure that this works. This also tests that a
      * feature type can have multiple FEATURE_LINK to be referred by different types.
      */
     public void testComplexTypeWithSimpleContent() {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=ex:FirstParentFeature");
         LOGGER
                 .info("WFS GetFeature&typename=ex:FirstParentFeature response:\n"
                         + prettyString(doc));
         assertXpathCount(2, "//ex:FirstParentFeature", doc);
 
         // 1
         assertXpathCount(2, "//ex:FirstParentFeature[@gml:id='1']/ex:nestedFeature", doc);
         assertXpathEvaluatesTo(
                 "string_one",
                 "//ex:FirstParentFeature[@gml:id='1']/ex:nestedFeature[1]/ex:SimpleContent/ex:someAttribute",
                 doc);
         assertXpathEvaluatesTo(
                 "string_two",
                 "//ex:FirstParentFeature[@gml:id='1']/ex:nestedFeature[2]/ex:SimpleContent/ex:someAttribute",
                 doc);
         assertXpathCount(
                 0,
                 "//ex:FirstParentFeature[@gml:id='1']/ex:nestedFeature[2]/ex:SimpleContent/FEATURE_LINK",
                 doc);
         // 2
         assertXpathCount(0, "//ex:FirstParentFeature[@gml:id='2']/ex:nestedFeature", doc);
 
         doc = getAsDOM("wfs?request=GetFeature&typename=ex:SecondParentFeature");
         LOGGER.info("WFS GetFeature&typename=ex:SecondParentFeature response:\n"
                 + prettyString(doc));
         assertXpathCount(2, "//ex:SecondParentFeature", doc);
 
         // 1
         assertXpathCount(0, "//ex:SecondParentFeature[@gml:id='1']/ex:nestedFeature", doc);
         // 2
         assertXpathCount(3, "//ex:SecondParentFeature[@gml:id='2']/ex:nestedFeature", doc);
         assertXpathEvaluatesTo(
                 "string_one",
                 "//ex:SecondParentFeature[@gml:id='2']/ex:nestedFeature[1]/ex:SimpleContent/ex:someAttribute",
                 doc);
         assertXpathEvaluatesTo(
                 "string_two",
                 "//ex:SecondParentFeature[@gml:id='2']/ex:nestedFeature[2]/ex:SimpleContent/ex:someAttribute",
                 doc);
         assertXpathEvaluatesTo(
                 "string_three",
                 "//ex:SecondParentFeature[@gml:id='2']/ex:nestedFeature[3]/ex:SimpleContent/ex:someAttribute",
                 doc);
     }
 
     /**
      * Test content of GetFeature response.
      */
     public void testGetFeatureContent() throws Exception {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=gsml:MappedFeature");
 
         assertXpathEvaluatesTo("4", "/wfs:FeatureCollection/@numberOfFeatures", doc);
         assertXpathCount(4, "//gsml:MappedFeature", doc);
 
         String[] schemaLocationParts = URLDecoder.decode(evaluate("/wfs:FeatureCollection/@xsi:schemaLocation", doc), "ASCII")
                 .split(" ");
         List<String> schemaLocationList = Arrays.asList(schemaLocationParts);
         assertEquals(schemaLocationList.size(), 4);
         // make sure describeFeatureType URL is correct
         StringBuffer describeFeatureType = new StringBuffer();
         describeFeatureType.append(BASE_URL).append(
                 "wfs?service=WFS&version=1.1.0&request=DescribeFeatureType").append(
                 "&typeName=gsml:MappedFeature");
         assertEquals(schemaLocationList.contains(describeFeatureType.toString()), true);
         // make sure the rest of the string would be there.. the order unimportant and might change
         assertEquals(schemaLocationList.contains(WFS.NAMESPACE), true);
         assertEquals(schemaLocationList.contains(AbstractAppSchemaMockData.GSML_URI), true);
         assertEquals(schemaLocationList
                 .contains("http://localhost:80/geoserver/schemas/wfs/1.1.0/wfs.xsd"), true);
 
         // mf1
         {
             String id = "mf1";
             assertXpathEvaluatesTo(id, "//gsml:MappedFeature[1]/@gml:id", doc);
             assertXpathEvaluatesTo("GUNTHORPE FORMATION", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gml:name", doc);
             assertXpathEvaluatesTo("-1.2 52.5 -1.2 52.6 -1.1 52.6 -1.1 52.5 -1.2 52.5",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
             // specification gu.25699
             assertXpathEvaluatesTo("gu.25699", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
             // description
             assertXpathEvaluatesTo("Olivine basalt, tuff, microgabbro, minor sedimentary rocks",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                             + "/gsml:GeologicUnit/gml:description", doc);
             // name
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gml:name", doc);
             assertXpathEvaluatesTo("Yaugher Volcanic Group", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gml:name[@codeSpace='urn:ietf:rfc:2141']", doc);
             assertXpathEvaluatesTo("Yaugher Volcanic Group", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]", doc);
             assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]/@codeSpace", doc);
             assertXpathEvaluatesTo("-Py", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]", doc);
             // feature link shouldn't appear as it's not in the schema
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/FEATURE_LINK", doc);
             // occurence [sic]
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:occurence", doc);
             assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurence[1]", doc);
             assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf1",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                             + "/gsml:GeologicUnit/gsml:occurence/@xlink:href", doc);
             // exposureColor
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:exposureColor", doc);
             assertXpathEvaluatesTo("Blue", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("some:uri", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor/gsml:CGI_TermValue/gsml:value/@codeSpace", doc);
             // feature link shouldn't appear as it's not in the schema
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                     + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
             // outcropCharacter
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:outcropCharacter", doc);
             assertXpathEvaluatesTo("x", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             // feature link shouldn't appear as it's not in the schema
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                     + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
             // composition
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition", doc);
             assertXpathEvaluatesTo("significant", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:role", doc);
             // feature link shouldn't appear as it's not in the schema
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
             // lithology
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology",
                     doc);
             // feature link shouldn't appear as it's not in the schema
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:lithology/FEATURE_LINK", doc);
         }
 
         // mf2
         {
             String id = "mf2";
             assertXpathEvaluatesTo(id, "//gsml:MappedFeature[2]/@gml:id", doc);
             assertXpathEvaluatesTo("MERCIA MUDSTONE GROUP", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gml:name", doc);
             assertXpathEvaluatesTo("-1.3 52.5 -1.3 52.6 -1.2 52.6 -1.2 52.5 -1.3 52.5",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
             // gu.25678
             assertXpathEvaluatesTo("gu.25678", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
             // name
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gml:name", doc);
             assertXpathEvaluatesTo("Yaugher Volcanic Group", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gml:name[@codeSpace='urn:ietf:rfc:2141']", doc);
             assertXpathEvaluatesTo("Yaugher Volcanic Group", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]", doc);
             assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]/@codeSpace", doc);
             assertXpathEvaluatesTo("-Py", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/FEATURE_LINK", doc);
             // occurence [sic]
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:occurence", doc);
             assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurence[1]", doc);
             assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf2",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                             + "/gsml:GeologicUnit/gsml:occurence[1]/@xlink:href", doc);
             assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurence[2]", doc);
             assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf3",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                             + "/gsml:GeologicUnit/gsml:occurence[2]/@xlink:href", doc);
             // description
             assertXpathEvaluatesTo("Olivine basalt, tuff, microgabbro, minor sedimentary rocks",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                             + "/gsml:GeologicUnit/gml:description", doc);
             // exposureColor
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:exposureColor", doc);
             assertXpathEvaluatesTo("Yellow", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[1]"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("some:uri", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[1]/gsml:CGI_TermValue/gsml:value/@codeSpace", doc);
             assertXpathEvaluatesTo("Blue", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[2]"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("some:uri", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor[2]/gsml:CGI_TermValue/gsml:value/@codeSpace", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                     + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
             // outcropCharacter
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:outcropCharacter", doc);
             assertXpathEvaluatesTo("y", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter[1]"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("x", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter[2]"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                     + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
             // composition
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition", doc);
             assertXpathEvaluatesTo("significant", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[1]"
                     + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification"
                     + "/gsml:GeologicUnit[@gml:id='gu.25678']/gsml:composition[1]"
                     + "/gsml:CompositionPart/gsml:role", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[1]"
                     + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
             assertXpathEvaluatesTo("minor", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[2]"
                     + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[2]"
                     + "/gsml:CompositionPart/gsml:role", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition[2]"
                     + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
 
             // lithology
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology",
                     doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:lithology/FEATURE_LINK", doc);
         }
 
         // mf3
         {
             String id = "mf3";
             assertXpathEvaluatesTo(id, "//gsml:MappedFeature[3]/@gml:id", doc);
             assertXpathEvaluatesTo("CLIFTON FORMATION", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gml:name", doc);
             assertXpathEvaluatesTo("-1.2 52.5 -1.2 52.6 -1.1 52.6 -1.1 52.5 -1.2 52.5",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
             // gu.25678
             assertXpathEvaluatesTo("#gu.25678", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/@xlink:href", doc);
             // make sure nothing else is encoded
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name", doc);
         }
 
         // mf4
         {
             String id = "mf4";
             assertXpathEvaluatesTo(id, "//gsml:MappedFeature[4]/@gml:id", doc);
             assertXpathEvaluatesTo("MURRADUC BASALT", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gml:name", doc);
             assertXpathEvaluatesTo("-1.3 52.5 -1.3 52.6 -1.2 52.6 -1.2 52.5 -1.3 52.5",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:shape//gml:posList", doc);
             // gu.25682
             assertXpathEvaluatesTo("gu.25682", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
             // description
             assertXpathEvaluatesTo("Olivine basalt", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:description", doc);
             // name
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gml:name", doc);
             assertXpathEvaluatesTo("New Group", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gml:name[@codeSpace='urn:ietf:rfc:2141']", doc);
             assertXpathEvaluatesTo("New Group", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]", doc);
             assertXpathEvaluatesTo("urn:ietf:rfc:2141", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[1]/@codeSpace", doc);
             assertXpathEvaluatesTo("-Xy", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gml:name[2]", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/FEATURE_LINK", doc);
             // occurence [sic]
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:occurence", doc);
             assertXpathEvaluatesTo("", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification" + "/gsml:GeologicUnit/gsml:occurence[1]", doc);
             assertXpathEvaluatesTo("urn:cgi:feature:MappedFeature:mf4",
                     "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                             + "/gsml:GeologicUnit/gsml:occurence/@xlink:href", doc);
             // exposureColor
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:exposureColor", doc);
             assertXpathEvaluatesTo("some:uri", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor/gsml:CGI_TermValue/gsml:value/@codeSpace", doc);
             assertXpathEvaluatesTo("Red", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:exposureColor"
                     + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
             // outcropCharacter
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:outcropCharacter", doc);
             assertXpathEvaluatesTo("z", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                     + "/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:outcropCharacter"
                     + "/gsml:CGI_TermValue/FEATURE_LINK", doc);
             // composition
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition", doc);
             assertXpathEvaluatesTo("significant", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value", doc);
             assertXpathEvaluatesTo("interbedded component", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:role", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:role/FEATURE_LINK", doc);
             // lithology
             assertXpathCount(2, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology",
                     doc);
             assertXpathCount(3, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                     + "/gsml:ControlledConcept/gml:name", doc);
             assertXpathEvaluatesTo("name_a", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit"
                     + "/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                     + "/gsml:ControlledConcept/gml:name[1]", doc);
             assertXpathEvaluatesTo("name_b", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit"
                     + "/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                     + "/gsml:ControlledConcept/gml:name[2]", doc);
             assertXpathEvaluatesTo("name_c", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit"
                     + "/gsml:composition/gsml:CompositionPart/gsml:lithology[1]"
                     + "/gsml:ControlledConcept/gml:name[3]", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:lithology[1]/FEATURE_LINK", doc);
             assertXpathCount(1, "//gsml:MappedFeature[@gml:id='" + id + "']/gsml:specification"
                     + "/gsml:GeologicUnit/gsml:composition/gsml:CompositionPart/gsml:lithology[2]"
                     + "/gsml:ControlledConcept/gml:name", doc);
             assertXpathEvaluatesTo("name_2", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit"
                     + "/gsml:composition/gsml:CompositionPart/gsml:lithology[2]"
                     + "/gsml:ControlledConcept/gml:name", doc);
             assertXpathCount(0, "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/gsml:composition"
                     + "/gsml:CompositionPart/gsml:lithology[2]/FEATURE_LINK", doc);
         }
         
         // check for duplicate gml:id
         assertXpathCount(1, "//gsml:GeologicUnit[@gml:id='gu.25678']", doc);
 
     }
     
     public void testGetFeaturePropertyFilter() {
         String xml = //
         "<wfs:GetFeature " //
                 + "service=\"WFS\" " //
                 + "version=\"1.1.0\" " //
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" " //
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" " //
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" " //
                 + "xmlns:gml=\"http://www.opengis.net/gml\" " //
                 + "xmlns:gsml=\"" + AbstractAppSchemaMockData.GSML_URI + "\" " //
                 + ">" //
                 + "    <wfs:Query typeName=\"gsml:MappedFeature\">" //
                 + "        <ogc:Filter>" //
                 + "            <ogc:PropertyIsEqualTo>" //
                 + "                <ogc:PropertyName>gml:name</ogc:PropertyName>" //
                 + "                <ogc:Literal>MURRADUC BASALT</ogc:Literal>" //
                 + "            </ogc:PropertyIsEqualTo>" //
                 + "        </ogc:Filter>" //
                 + "    </wfs:Query> " //
                 + "</wfs:GetFeature>";
         Document doc = postAsDOM("wfs", xml);
         LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
         assertXpathEvaluatesTo("1", "/wfs:FeatureCollection/@numberOfFeatures", doc);
         assertXpathCount(1, "//gsml:MappedFeature", doc);
         // mf4
         {
             String id = "mf4";
             assertXpathEvaluatesTo(id, "//gsml:MappedFeature[1]/@gml:id", doc);
             assertXpathEvaluatesTo("MURRADUC BASALT", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gml:name", doc);
             // gu.25682
             assertXpathEvaluatesTo("gu.25682", "//gsml:MappedFeature[@gml:id='" + id
                     + "']/gsml:specification/gsml:GeologicUnit/@gml:id", doc);
         }
     }
     
     /**
      * Making sure attributes that are encoded as xlink:href can still be queried in filters.
      */
     public void testFilteringXlinkHref() {
         String xml = //
         "<wfs:GetFeature " //
                 + "service=\"WFS\" " //
                 + "version=\"1.1.0\" " //
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" " //
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" " //
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" " //
                 + "xmlns:gml=\"http://www.opengis.net/gml\" " //
                 + "xmlns:gsml=\"" + AbstractAppSchemaMockData.GSML_URI + "\" " //
                 + ">" //
                 + "    <wfs:Query typeName=\"gsml:MappedFeature\">" //
                 + "        <ogc:Filter>" //
                 + "            <ogc:PropertyIsEqualTo>" //
                 + "                <ogc:PropertyName>gsml:specification/gsml:GeologicUnit/gml:name[1]</ogc:PropertyName>" //
                 + "                <ogc:Literal>Yaugher Volcanic Group</ogc:Literal>" //
                 + "            </ogc:PropertyIsEqualTo>" //
                 + "        </ogc:Filter>" //
                 + "    </wfs:Query> " //
                 + "</wfs:GetFeature>";
         Document doc = postAsDOM("wfs", xml);
         LOGGER.info("WFS filter GetFeature response:\n" + prettyString(doc));
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
         // there should be 3: 
         // - mf1/gu.25699
         // - mf2/gu.25678
         // - mf3/gu.25678 which is encoded as xlink:href
         assertXpathEvaluatesTo("3", "/wfs:FeatureCollection/@numberOfFeatures", doc);
         assertXpathCount(3, "//gsml:MappedFeature", doc);
     }
 
 }
