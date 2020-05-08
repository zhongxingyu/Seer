 package org.cagrid.iso21090.sdkquery.test;
 
 import gov.nih.nci.cacoresdk.domain.other.datatype.AdDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.BlNonNullDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.CdDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.DsetCdDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.DsetIiDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.EnDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.IiDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.IvlIntDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.IvlPqDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.IvlRealDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.IvlTsDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.PqDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.RealDataType;
 import gov.nih.nci.cacoresdk.domain.other.datatype.ScDataType;
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.cqlquery.Association;
 import gov.nih.nci.cagrid.cqlquery.Attribute;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.cqlquery.Object;
 import gov.nih.nci.cagrid.cqlquery.Predicate;
 import gov.nih.nci.cagrid.data.DataServiceConstants;
 import gov.nih.nci.iso21090.Ad;
 import gov.nih.nci.iso21090.AddressPartType;
 import gov.nih.nci.iso21090.Adxp;
 import gov.nih.nci.iso21090.BlNonNull;
 import gov.nih.nci.iso21090.Cd;
 import gov.nih.nci.iso21090.DSet;
 import gov.nih.nci.iso21090.EdText;
 import gov.nih.nci.iso21090.En;
 import gov.nih.nci.iso21090.Enxp;
 import gov.nih.nci.iso21090.Ii;
 import gov.nih.nci.iso21090.Int;
 import gov.nih.nci.iso21090.NullFlavor;
 import gov.nih.nci.iso21090.Pq;
 import gov.nih.nci.iso21090.Pqv;
 import gov.nih.nci.iso21090.Real;
 import gov.nih.nci.iso21090.Sc;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.query.hibernate.HQLCriteria;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.iso21090.sdkquery.translator.CQL2ParameterizedHQL;
 import org.cagrid.iso21090.sdkquery.translator.ParameterizedHqlQuery;
 import org.cagrid.iso21090.sdkquery.translator.QueryTranslationException;
 
 
 public class IsoQueriesTestCase extends TestCase {
 
     public static Log LOG = LogFactory.getLog(IsoQueriesTestCase.class);
 
     private ApplicationService sdkService = null;
     private CQL2ParameterizedHQL queryTranslator = null;
 
 
     public IsoQueriesTestCase(String name) {
         super(name);
     }
 
 
     public void setUp() {
         try {
             this.sdkService = QueryTestsHelper.getSdkApplicationService();
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error obtaining cacore service: " + ex.getMessage());
         }
         try {
             this.queryTranslator = QueryTestsHelper.getCqlTranslator();
         } catch (IOException ex) {
             ex.printStackTrace();
             fail("Error obtaining cql query translator: " + ex.getMessage());
         }
     }
     
     
     /*
      * Problem querying hibernate with entity names public void testAdxpStuff()
      * { CQLQuery query = new CQLQuery(); gov.nih.nci.cagrid.cqlquery.Object
      * target = new gov.nih.nci.cagrid.cqlquery.Object();
      * target.setName(AdDataType.class.getName()); Association association1 =
      * new Association(); association1.setName(Ad.class.getName());
      * association1.setRoleName("value8"); target.setAssociation(association1);
      * Association association2 = new Association();
      * association2.setName(Adxp.class.getName());
      * association2.setRoleName("part");
      * association1.setAssociation(association2); Attribute attribute3 = new
      * Attribute(); attribute3.setName("value");
      * attribute3.setPredicate(Predicate.IS_NOT_NULL);
      * attribute3.setValue("true"); association2.setAttribute(attribute3);
      * query.setTarget(target);
      * 
      * Iterator<?> iter = executeQuery(query).iterator(); ArrayList<AdDataType>
      * result = new ArrayList<AdDataType>(); while (iter.hasNext()) {
      * result.add((AdDataType)iter.next()); } AdDataType testResultClass =
      * result.get(0); assertEquals(4, result.size());
      * assertEquals(NullFlavor.NI, testResultClass.getValue1().getNullFlavor());
      * }
      */
  
 
     
     public void testQueryIiDataType() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(IiDataType.class.getName());
         Association assoc = new Association();
         assoc.setName(Ii.class.getName());
         assoc.setRoleName("value1");
         Attribute attrib = new Attribute("extension", Predicate.EQUAL_TO, "II_Extension");
         assoc.setAttribute(attrib);
         target.setAssociation(assoc);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryScDataType() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(ScDataType.class.getName());
         Association assoc1 = new Association();
         assoc1.setName(Sc.class.getName());
         assoc1.setRoleName("value2");
         Association assoc2 = new Association();
         assoc2.setName(Cd.class.getName());
         assoc2.setRoleName("code");
         Attribute attrib = new Attribute("code", Predicate.EQUAL_TO, "VALUE2_CODE_CODE1");
         assoc2.setAttribute(attrib);
         assoc1.setAssociation(assoc2);
         target.setAssociation(assoc1);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryEnDataType() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(EnDataType.class.getName());
         Association assoc1 = new Association();
         assoc1.setName(En.class.getName());
         assoc1.setRoleName("value5");
         Association assoc2 = new Association();
         assoc2.setName(Enxp.class.getName());
         assoc2.setRoleName("part");
         Attribute attrib = new Attribute("value", Predicate.EQUAL_TO, "Mr. John Doe1");
         assoc2.setAttribute(attrib);
         assoc1.setAssociation(assoc2);
         target.setAssociation(assoc1);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryAdDataType() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(AdDataType.class.getName());
         Association assoc1 = new Association();
         assoc1.setName(Ad.class.getName());
         assoc1.setRoleName("value1");
         Association assoc2 = new Association();
         assoc2.setName(Adxp.class.getName());
         assoc2.setRoleName("part");
         Attribute attrib = new Attribute("value", Predicate.LIKE, "%1");
         assoc2.setAttribute(attrib);
         assoc1.setAssociation(assoc2);
         target.setAssociation(assoc1);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryCdDataTypeAgainstConstant() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(CdDataType.class.getName());
         Association assoc = new Association();
         assoc.setName(Cd.class.getName());
         assoc.setRoleName("value3");
         Attribute attrib = new Attribute("codeSystem", Predicate.EQUAL_TO, "CODESYSTEM");
         assoc.setAttribute(attrib);
         target.setAssociation(assoc);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryIiDataTypeAgainstConstant() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(IiDataType.class.getName());
         Association assoc = new Association();
         assoc.setName(Ii.class.getName());
         assoc.setRoleName("value3");
         assoc.setAttribute(new Attribute("root", Predicate.EQUAL_TO, "2.16.12.123.456"));
         target.setAssociation(assoc);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryCdNullFlavorNi() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(CdDataType.class.getName());
         Association assoc = new Association();
         assoc.setName(Cd.class.getName());
         assoc.setRoleName("value3");
         Attribute attrib = new Attribute("nullFlavor", Predicate.EQUAL_TO, NullFlavor.NA.name());
         assoc.setAttribute(attrib);
         target.setAssociation(assoc);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryAdNullFlavorNi() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(AdDataType.class.getName());
         Association assoc = new Association();
         assoc.setName(Ad.class.getName());
         assoc.setRoleName("value1");
         assoc.setAttribute(new Attribute("nullFlavor", Predicate.EQUAL_TO, "NI"));
         target.setAssociation(assoc);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryAdxpAddressPartTypeADL() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(AdDataType.class.getName());
         Association assoc1 = new Association();
         assoc1.setName(Ad.class.getName());
         assoc1.setRoleName("value1");
         Association assoc2 = new Association();
         assoc2.setName(Adxp.class.getName());
         assoc2.setRoleName("part");
         Attribute attrib = new Attribute("type", Predicate.EQUAL_TO, AddressPartType.ADL.name());
         assoc2.setAttribute(attrib);
         assoc1.setAssociation(assoc2);
         target.setAssociation(assoc1);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryRealDataType() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(RealDataType.class.getName());
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryIvlPqDataType() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(IvlPqDataType.class.getName());
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryIvlRealDataType() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(IvlRealDataType.class.getName());
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryIvlTsDataType() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(IvlTsDataType.class.getName());
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryIvlIntDataType() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(IvlIntDataType.class.getName());
         Association association1 = new Association();
        association1.setName("gov.nih.nci.iso21090.Ivl<Int>");
         association1.setRoleName("value1");
         target.setAssociation(association1);
         Association association2 = new Association();
         association2.setName(Int.class.getName());
         association2.setRoleName("low");
         association1.setAssociation(association2);
         Attribute attr = new Attribute();
         attr.setName("value");
         attr.setPredicate(Predicate.EQUAL_TO);
         attr.setValue("1");
         association2.setAttribute(attr);
         query.setTarget(target);
 
         List<?> results = executeQuery(query);
         Iterator<?> iter = results.iterator();
         ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.IvlIntDataType> result = new ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.IvlIntDataType>();
         while (iter.hasNext()) {
             result.add((gov.nih.nci.cacoresdk.domain.other.datatype.IvlIntDataType) iter.next());
         }
         gov.nih.nci.cacoresdk.domain.other.datatype.IvlIntDataType testResultClass = result.get(0);
         assertEquals(3, result.size()); // Ye's test has 4 here
         assertEquals(Integer.valueOf(1), testResultClass.getValue1().getLow().getValue()); // Ye's test used "1". String vs Integer
     }
 
 
     public void testQueryDsetIiDataType() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(DsetIiDataType.class.getName());
         Association assoc1 = new Association();
         assoc1.setName(DSet.class.getName());
         assoc1.setRoleName("value1");
         Association assoc2 = new Association();
         assoc2.setName(Ii.class.getName());
         assoc2.setRoleName("item");
         assoc2.setAttribute(new Attribute("extension", Predicate.EQUAL_TO, "II_Extension"));
         assoc1.setAssociation(assoc2);
         target.setAssociation(assoc1);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryDsetIiDataTypeAgainstConstant() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(DsetIiDataType.class.getName());
         Association a1 = new Association();
         a1.setName(DSet.class.getName());
         a1.setRoleName("value1");
         Association a2 = new Association();
         a2.setName(Ii.class.getName());
         a2.setRoleName("item");
         a2.setAttribute(new Attribute("root", Predicate.EQUAL_TO, "2.16.12.123.456"));
         a1.setAssociation(a2);
         target.setAssociation(a1);
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryDsetTelDataType() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName("gov.nih.nci.cacoresdk.domain.other.datatype.DsetTelDataType");
         Association association1 = new Association();
         association1.setName("gov.nih.nci.iso21090.DSet");
         association1.setRoleName("value1");
         target.setAssociation(association1);
 
         Association association2 = new Association();
         association2.setName("gov.nih.nci.iso21090.Tel");
         association2.setRoleName("item");
         association1.setAssociation(association2);
 
         Attribute attribute3 = new Attribute();
         attribute3.setName("value");
         attribute3.setPredicate(Predicate.EQUAL_TO);
         attribute3.setValue("tel://123-456-7891");
         association2.setAttribute(attribute3);
 
         query.setTarget(target);
 
         executeQuery(query);
     }
 
 
     public void testQueryBlNonNullValue() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(BlNonNullDataType.class.getName());
         Association association1 = new Association();
         association1.setName(BlNonNull.class.getName());
         association1.setRoleName("value1");
         target.setAssociation(association1);
         Attribute attribute2 = new Attribute();
         attribute2.setName("value");
         attribute2.setPredicate(Predicate.EQUAL_TO);
         attribute2.setValue("false");
         association1.setAttribute(attribute2);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<BlNonNullDataType> result = new ArrayList<BlNonNullDataType>();
         while (iter.hasNext()) {
             result.add((BlNonNullDataType) iter.next());
         }
         BlNonNullDataType testResultClass = result.get(0);
         assertEquals(1, result.size());
         assertEquals(testResultClass.getValue1().getValue().booleanValue(), false);
     }
 
 
     public void testQueryCdCodeSystem() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(ScDataType.class.getName());
         Association association1 = new Association();
         association1.setName(Sc.class.getName());
         association1.setRoleName("value2");
         target.setAssociation(association1);
         Association association2 = new Association();
         association2.setName(Cd.class.getName());
         association2.setRoleName("code");
         association1.setAssociation(association2);
         Attribute attribute3 = new Attribute();
         attribute3.setName("codeSystem");
         attribute3.setPredicate(Predicate.IS_NOT_NULL);
         attribute3.setValue("true");
         association2.setAttribute(attribute3);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<ScDataType> result = new ArrayList<ScDataType>();
         while (iter.hasNext()) {
             result.add((ScDataType) iter.next());
         }
         ScDataType testResultClass = result.get(0);
         assertEquals(4, result.size());
         assertEquals("VALUE2_CODE_CODE_SYSTEM1", testResultClass.getValue2().getCode().getCodeSystem().toString());
     }
 
 
     public void testQueryIiRootAttribute() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(IiDataType.class.getName());
         Association association1 = new Association();
         association1.setName(Ii.class.getName());
         association1.setRoleName("value2");
         target.setAssociation(association1);
         Attribute attribute2 = new Attribute();
         attribute2.setName("root");
         attribute2.setPredicate(Predicate.IS_NOT_NULL);
         attribute2.setValue("true");
         association1.setAttribute(attribute2);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<IiDataType> result = new ArrayList<IiDataType>();
         while (iter.hasNext()) {
             result.add((IiDataType) iter.next());
         }
         IiDataType testResultClass = result.get(0);
         assertEquals(2, result.size());
         assertEquals("II_VALUE2_ROOT", testResultClass.getValue2().getRoot().toString());
     }
 
 
     
     public void testQueryIvlTsWidth() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName("gov.nih.nci.cacoresdk.domain.other.datatype.IvlTsDataType");
         Association association1 = new Association();
         association1.setName("gov.nih.nci.iso21090.Ivl<Ts>");
         association1.setRoleName("value3");
         target.setAssociation(association1);
         Association association2 = new Association();
         association2.setName("gov.nih.nci.iso21090.Ts");
         association2.setRoleName("width");
         association1.setAssociation(association2);
         Attribute attribute3 = new Attribute();
         attribute3.setName("nullFlavor");
         attribute3.setPredicate(Predicate.IS_NOT_NULL);
         association2.setAttribute(attribute3);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.IvlTsDataType> result = new ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.IvlTsDataType>();
         while (iter.hasNext()) {
             result.add((gov.nih.nci.cacoresdk.domain.other.datatype.IvlTsDataType) iter.next());
         }
         // gov.nih.nci.cacoresdk.domain.other.datatype.IvlTsDataType
         // testResultClass = result.get(0);
     }
 
 
     public void testQueryIvlPqWidth() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName("gov.nih.nci.cacoresdk.domain.other.datatype.IvlPqDataType");
         Association association1 = new Association();
         association1.setName("gov.nih.nci.iso21090.Ivl<Pq>");
         association1.setRoleName("value4");
         target.setAssociation(association1);
         Association association2 = new Association();
         association2.setName("gov.nih.nci.iso21090.Pqv");
         association2.setRoleName("width");
         association1.setAssociation(association2);
         Attribute attribute3 = new Attribute();
         attribute3.setName("value");
         attribute3.setPredicate(Predicate.EQUAL_TO);
         attribute3.setValue("5.1");
         association2.setAttribute(attribute3);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.IvlPqDataType> result = new ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.IvlPqDataType>();
         while (iter.hasNext()) {
             result.add((gov.nih.nci.cacoresdk.domain.other.datatype.IvlPqDataType) iter.next());
         }
         gov.nih.nci.cacoresdk.domain.other.datatype.IvlPqDataType testResultClass = result.get(0);
         assertEquals(Double.valueOf(5.1), ((Pqv) testResultClass.getValue4().getWidth()).getValue());
     }
 
 
     public void testQueryPqUnit() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(PqDataType.class.getName());
         Association association1 = new Association();
         association1.setName(Pq.class.getName());
         association1.setRoleName("value1");
         target.setAssociation(association1);
         Attribute attribute2 = new Attribute();
         attribute2.setName("unit");
         attribute2.setPredicate(Predicate.IS_NOT_NULL);
         association1.setAttribute(attribute2);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.PqDataType> result = new ArrayList<gov.nih.nci.cacoresdk.domain.other.datatype.PqDataType>();
         while (iter.hasNext()) {
             result.add((gov.nih.nci.cacoresdk.domain.other.datatype.PqDataType) iter.next());
         }
         gov.nih.nci.cacoresdk.domain.other.datatype.PqDataType testResultClass = result.get(0);
         assertEquals(5, result.size());
         assertEquals("GALLON", testResultClass.getValue1().getUnit().toString());
     }
 
 
     public void testQueryCdDataType() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(CdDataType.class.getName());
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<CdDataType> result = new ArrayList<CdDataType>();
         while (iter.hasNext()) {
             result.add((CdDataType) iter.next());
         }
         CdDataType testResultClass = result.get(0);
     }
 
 
     public void testQueryRealDataTypeAttributeValue() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(RealDataType.class.getName());
         Association association1 = new Association();
         association1.setName(Real.class.getName());
         association1.setRoleName("value1");
         target.setAssociation(association1);
         Attribute attribute2 = new Attribute();
         attribute2.setName("value");
         attribute2.setPredicate(Predicate.IS_NOT_NULL);
         attribute2.setValue("true");
         association1.setAttribute(attribute2);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<RealDataType> result = new ArrayList<RealDataType>();
         while (iter.hasNext()) {
             result.add((RealDataType) iter.next());
         }
         RealDataType testResultClass = result.get(0);
         assertEquals(5, result.size());
         assertEquals("1001.15", testResultClass.getValue1().getValue().toString());
     }
 
 
     public void testQueryCdCodeNotNull() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target =
             new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(CdDataType.class.getName());
         Association association1 = new Association();
         association1.setName(Cd.class.getName());
         association1.setRoleName("value1");
         target.setAssociation(association1);
         Attribute attribute2 = new Attribute();
         attribute2.setName("code");
         attribute2.setPredicate(Predicate.IS_NOT_NULL);
         attribute2.setValue("true");
         association1.setAttribute(attribute2);
         query.setTarget(target);
 
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<CdDataType> result = new ArrayList<CdDataType>();
         while (iter.hasNext()) {
             result.add((CdDataType)iter.next());
         }
         gov.nih.nci.cacoresdk.domain.other.datatype.CdDataType testResultClass = result.get(0);
         assertEquals(5, result.size());
     }
     
     
     public void testQueryDsetCdEdTextOriginalText() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(DsetCdDataType.class.getName());
         Association association1 = new Association();
         association1.setName(DSet.class.getName());
         association1.setRoleName("value4");
         target.setAssociation(association1);
         Association association2 = new Association();
         association2.setName(Cd.class.getName());
         association2.setRoleName("item");
         association1.setAssociation(association2);
         Association association3 = new Association();
         association3.setName(EdText.class.getName());
         association3.setRoleName("originalText");
         //association3.setAttribute(new Attribute("value", Predicate.IS_NOT_NULL, "true"));
         association2.setAssociation(association3);
         query.setTarget(target);
         
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<DsetCdDataType> result = new ArrayList<DsetCdDataType>();
         while (iter.hasNext()) {
             result.add((DsetCdDataType)iter.next());
         }
         DsetCdDataType testResultClass = result.get(0);
         assertEquals(5, result.size());
     }
     
     
     public void testQueryDsetCdEdTextOriginalTextValue() {
         CQLQuery query = new CQLQuery();
         gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
         target.setName(DsetCdDataType.class.getName());
         Association association1 = new Association();
         association1.setName(DSet.class.getName());
         association1.setRoleName("value4");
         target.setAssociation(association1);
         Association association2 = new Association();
         association2.setName(Cd.class.getName());
         association2.setRoleName("item");
         association1.setAssociation(association2);
         Association association3 = new Association();
         association3.setName(EdText.class.getName());
         association3.setRoleName("originalText");
         association3.setAttribute(new Attribute("value", Predicate.IS_NOT_NULL, "true"));
         association2.setAssociation(association3);
         query.setTarget(target);
         
         Iterator<?> iter = executeQuery(query).iterator();
         ArrayList<DsetCdDataType> result = new ArrayList<DsetCdDataType>();
         while (iter.hasNext()) {
             result.add((DsetCdDataType)iter.next());
         }
         DsetCdDataType testResultClass = result.get(0);
         assertEquals(5, result.size());
     }
    
    
    public void testQueryIvlIntLowClosed() {
        CQLQuery query = new CQLQuery();
        gov.nih.nci.cagrid.cqlquery.Object target = new gov.nih.nci.cagrid.cqlquery.Object();
        target.setName("gov.nih.nci.cacoresdk.domain.other.datatype.IvlIntDataType");
        Association association1 = new Association();
        association1.setName("gov.nih.nci.iso21090.Ivl<Int>");
        association1.setRoleName("value2");
        target.setAssociation(association1);
        Attribute attribute2 = new Attribute();
        attribute2.setName("lowClosed");
        attribute2.setPredicate(Predicate.EQUAL_TO);
        attribute2.setValue("true");
        association1.setAttribute(attribute2);
        query.setTarget(target);
        
        Iterator<?> iter = executeQuery(query).iterator();
    }
 
 
     private List<?> executeQuery(CQLQuery query) {
         if (LOG.isDebugEnabled()) {
             StringWriter writer = new StringWriter();
             try {
                 Utils.serializeObject(query, DataServiceConstants.CQL_QUERY_QNAME, writer);
                 LOG.debug(writer.getBuffer().toString());
             } catch (Exception ex) {
                 // ignore
             }
         }
         ParameterizedHqlQuery hql = null;
         try {
             hql = queryTranslator.convertToHql(query);
         } catch (QueryTranslationException e) {
             e.printStackTrace();
             fail("Error translating CQL to HQL: " + e.getMessage());
         }
         LOG.info("The translated query is:");
         LOG.info(hql);
         System.out.println("The translated query is:");
         System.out.println(hql);
         System.out.flush();
         HQLCriteria criteria = new HQLCriteria(hql.getHql(), hql.getParameters());
         List<?> results = null;
         try {
             results = sdkService.query(criteria);
             LOG.info("Found " + results.size() + " results");
         } catch (ApplicationException e) {
             e.printStackTrace();
             fail("Error executing query: " + e.getMessage());
         }
         return results;
     }
 
 
     /* Disabled test since I can't geth the inner part name info out of hibernate config
     public void testQueryDsetAdAdxp() {
         CQLQuery query = new CQLQuery();
         Object target = new Object();
         target.setName(DsetAdDataType.class.getName());
         Association a1 = new Association();
         a1.setName(DSet.class.getName());
         a1.setRoleName("value1");
         target.setAssociation(a1);
         Association a2 = new Association();
         a2.setName(Ad.class.getName());
         a2.setRoleName("item");
         a1.setAssociation(a2);
         Association a3 = new Association();
         a3.setName(Adxp.class.getName());
         a3.setRoleName("part");
         a2.setAssociation(a3);
         a3.setAttribute(new Attribute("value", Predicate.EQUAL_TO, "foo"));
         query.setTarget(target);
 
         executeQuery(query);
     }
     */
 
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         TestRunner runner = new TestRunner();
         TestResult result = runner.doRun(new TestSuite(IsoQueriesTestCase.class));
         System.exit(result.errorCount() + result.failureCount());
     }
 }
