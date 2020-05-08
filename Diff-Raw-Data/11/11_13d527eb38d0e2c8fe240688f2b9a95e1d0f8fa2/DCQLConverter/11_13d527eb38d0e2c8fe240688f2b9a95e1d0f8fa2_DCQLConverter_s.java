 package gov.nih.nci.cagrid.fqp.common;
 
 import gov.nih.nci.cagrid.cqlquery.Attribute;
 import gov.nih.nci.cagrid.cqlquery.LogicalOperator;
 import gov.nih.nci.cagrid.cqlquery.Predicate;
 import gov.nih.nci.cagrid.dcql.Association;
 import gov.nih.nci.cagrid.dcql.ForeignAssociation;
 import gov.nih.nci.cagrid.dcql.ForeignPredicate;
 import gov.nih.nci.cagrid.dcql.Group;
 import gov.nih.nci.cagrid.dcql.Object;
 import gov.nih.nci.cagrid.dcqlresult.DCQLQueryResultsCollection;
 import gov.nih.nci.cagrid.dcqlresult.DCQLResult;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.UMLClass;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.cagrid.cql.utilities.CQL2ResultsToCQL1ResultsConverter;
 import org.cagrid.cql.utilities.ResultsConversionException;
 import org.cagrid.cql2.AttributeValue;
 import org.cagrid.cql2.BinaryPredicate;
 import org.cagrid.cql2.CQLAttribute;
 import org.cagrid.cql2.GroupLogicalOperator;
 import org.cagrid.cql2.UnaryPredicate;
 import org.cagrid.data.dcql.DCQLAssociatedObject;
 import org.cagrid.data.dcql.DCQLGroup;
 import org.cagrid.data.dcql.DCQLObject;
 import org.cagrid.data.dcql.DCQLQuery;
 import org.cagrid.data.dcql.ForeignAssociatedObject;
 import org.cagrid.data.dcql.JoinCondition;
 
 /**
  * DCQLConverter
  * 
  * Utility to convert DCQL and DCQL results between versions 1 and 2
  * 
  * @author David
  */
 public class DCQLConverter {
 
     private static Map<Predicate, BinaryPredicate> binaryPredicateConversion = null;
     private static Map<Predicate, UnaryPredicate> unaryPredicateConversion = null;
     private static Map<ForeignPredicate, BinaryPredicate> foreignPredicateConversion = null;
     static {
         binaryPredicateConversion = new HashMap<Predicate, BinaryPredicate>();
         unaryPredicateConversion = new HashMap<Predicate, UnaryPredicate>();
         foreignPredicateConversion = new HashMap<ForeignPredicate, BinaryPredicate>();
         binaryPredicateConversion.put(Predicate.EQUAL_TO, BinaryPredicate.EQUAL_TO);
         binaryPredicateConversion.put(Predicate.GREATER_THAN, BinaryPredicate.GREATER_THAN);
         binaryPredicateConversion.put(Predicate.GREATER_THAN_EQUAL_TO, BinaryPredicate.GREATER_THAN_EQUAL_TO);
         binaryPredicateConversion.put(Predicate.LESS_THAN, BinaryPredicate.LESS_THAN);
         binaryPredicateConversion.put(Predicate.LESS_THAN_EQUAL_TO, BinaryPredicate.LESS_THAN_EQUAL_TO);
         binaryPredicateConversion.put(Predicate.LIKE, BinaryPredicate.LIKE);
         binaryPredicateConversion.put(Predicate.NOT_EQUAL_TO, BinaryPredicate.NOT_EQUAL_TO);
         unaryPredicateConversion.put(Predicate.IS_NOT_NULL, UnaryPredicate.IS_NOT_NULL);
         unaryPredicateConversion.put(Predicate.IS_NULL, UnaryPredicate.IS_NULL);
         foreignPredicateConversion.put(ForeignPredicate.EQUAL_TO, BinaryPredicate.EQUAL_TO);
         foreignPredicateConversion.put(ForeignPredicate.NOT_EQUAL_TO, BinaryPredicate.NOT_EQUAL_TO);
         foreignPredicateConversion.put(ForeignPredicate.GREATER_THAN, BinaryPredicate.GREATER_THAN);
         foreignPredicateConversion.put(ForeignPredicate.GREATER_THAN_EQUAL_TO, BinaryPredicate.GREATER_THAN_EQUAL_TO);
         foreignPredicateConversion.put(ForeignPredicate.LESS_THAN, BinaryPredicate.LESS_THAN);
         foreignPredicateConversion.put(ForeignPredicate.LESS_THAN_EQUAL_TO, BinaryPredicate.LESS_THAN_EQUAL_TO);
     }
     
     private DomainModelLocator modelLocator = null;
     
     public DCQLConverter() {
         this(new DefaultDomainModelLocator());
     }
     
     
     public DCQLConverter(DomainModelLocator modelLocator) {
         this.modelLocator = modelLocator;
     }
     
     
     public DCQLQuery convertToDcql2(gov.nih.nci.cagrid.dcql.DCQLQuery oldQuery) throws DCQLConversionException {
         DCQLQuery query = new DCQLQuery();
         // target service URLs
         query.setTargetServiceURL(oldQuery.getTargetServiceURL());
         // target object
         DCQLObject target = convertToDcql2Object(oldQuery.getTargetServiceURL(0), oldQuery.getTargetObject());
         query.setTargetObject(target);
         return query;
     }
     
     
     private DCQLObject convertToDcql2Object(String targetServiceUrl, Object oldObject) throws DCQLConversionException {
         DCQLObject object = new DCQLObject();
         object.setName(oldObject.getName());
         if (oldObject.getAssociation() != null) {
             object.setAssociatedObject(convertToDcql2Association(
                 targetServiceUrl, oldObject.getAssociation()));
         } else if (oldObject.getAttribute() != null) {
             object.setAttribute(convertToCql2Attribute(
                 targetServiceUrl, object.getName(), oldObject.getAttribute()));
         } else if (oldObject.getGroup() != null) {
             object.setGroup(convertToDcql2Group(
                 targetServiceUrl, object.getName(), oldObject.getGroup()));
         } else if (oldObject.getForeignAssociation() != null) {
             object.setForeignAssociatedObject(
                 convertToDcql2ForeignAssociation(
                     oldObject.getForeignAssociation()));
         }
         return object;
     }
     
     
     private DCQLAssociatedObject convertToDcql2Association(String targetServiceUrl, Association oldAssociation)
         throws DCQLConversionException {
         DCQLObject object = convertToDcql2Object(targetServiceUrl, oldAssociation);
         DCQLAssociatedObject association = new DCQLAssociatedObject();
         association.setName(object.getName());
         association.setEndName(oldAssociation.getRoleName());
         association.setAssociatedObject(object.getAssociatedObject());
         association.setGroup(object.getGroup());
         association.setAttribute(object.getAttribute());
         return association;
     }
     
     
     private CQLAttribute convertToCql2Attribute(String targetServiceUrl, String className, Attribute oldAttribute)
         throws DCQLConversionException {
         CQLAttribute attribute = new CQLAttribute();
         attribute.setName(oldAttribute.getName());
         Predicate oldPredicate = oldAttribute.getPredicate();
         if (unaryPredicateConversion.containsKey(oldPredicate)) {
             attribute.setUnaryPredicate(unaryPredicateConversion.get(oldPredicate));
         } else {
             attribute.setBinaryPredicate(binaryPredicateConversion.get(oldPredicate));
             String oldValue = oldAttribute.getValue();
             AttributeValue value = convertAttributeValue(
                 targetServiceUrl, className, oldAttribute.getName(), oldValue);
             attribute.setAttributeValue(value);
         }
         return attribute;
     }
     
     
     private AttributeValue convertAttributeValue(
         String targetServiceUrl, String className, String attributeName, String rawValue)
         throws DCQLConversionException {
         String datatypeName = null;
         DomainModel model = null;
         try {
             model = modelLocator.getDomainModel(targetServiceUrl);
         } catch (Exception ex) {
             throw new DCQLConversionException(
                 "Error locating domain model for service " + targetServiceUrl + ": " + ex.getMessage(), ex);
         }
         UMLClass[] classes = model.getExposedUMLClassCollection().getUMLClass();
         for (UMLClass c : classes) {
             String fqName = c.getClassName();
             if (c.getPackageName() != null && c.getPackageName().length() != 0) {
                 fqName = c.getPackageName() + "." + c.getClassName();
             }
             if (className.equals(fqName)) {
                 for (UMLAttribute att : c.getUmlAttributeCollection().getUMLAttribute()) {
                     if (attributeName.equals(att.getName())) {
                         datatypeName = att.getDataTypeName();
                         break;
                     }
                 }
             }
         }
         AttributeValue val = new AttributeValue();
         if (datatypeName != null) {
             if (String.class.getName().equals(datatypeName)) {
                 val.setStringValue(rawValue);
             } else if (Boolean.class.getName().equals(datatypeName)) {
                 val.setBooleanValue(Boolean.valueOf(rawValue));
             } else if (Date.class.getName().equals(datatypeName)) {
                 Date date = null;
                 try {
                     date = DateFormat.getDateInstance().parse(rawValue);
                 } catch (ParseException ex) {
                     throw new DCQLConversionException(
                         "Error converting value " + rawValue + " to date: " + ex.getMessage(), ex);
                 }
                 val.setDateValue(date);
             } else if (Integer.class.getName().equals(datatypeName)) {
                 val.setIntegerValue(Integer.valueOf(rawValue));
             } else if (Long.class.getName().equals(datatypeName)) {
                 val.setLongValue(Long.valueOf(rawValue));
             } else if (Double.class.getName().equals(datatypeName)) {
                 val.setDoubleValue(Double.valueOf(rawValue));
             }
         }
         return val;
     }
     
     
     private DCQLGroup convertToDcql2Group(String targetServiceUrl, String className, Group oldGroup)
         throws DCQLConversionException {
         DCQLGroup group = new DCQLGroup();
         group.setLogicalOperation(LogicalOperator.AND.equals(
             oldGroup.getLogicRelation()) ? 
                 GroupLogicalOperator.AND : GroupLogicalOperator.OR);
         if (oldGroup.getAssociation() != null && oldGroup.getAssociation().length != 0) {
             DCQLAssociatedObject[] associations = new DCQLAssociatedObject[oldGroup.getAssociation().length];
             for (int i = 0; i < oldGroup.getAssociation().length; i++) {
                 associations[i] = convertToDcql2Association(targetServiceUrl, oldGroup.getAssociation(i));
             }
             group.setAssociatedObject(associations);
         }
         if (oldGroup.getAttribute() != null && oldGroup.getAttribute().length != 0) {
             CQLAttribute[] attributes = new CQLAttribute[oldGroup.getAttribute().length];
             for (int i = 0; i < oldGroup.getAttribute().length; i++) {
                 attributes[i] = convertToCql2Attribute(targetServiceUrl, className, oldGroup.getAttribute(i));
             }
             group.setAttribute(attributes);
         }
         if (oldGroup.getGroup() != null && oldGroup.getGroup().length != 0) {
             DCQLGroup groups[] = new DCQLGroup[oldGroup.getGroup().length];
             for (int i = 0; i < oldGroup.getGroup().length; i++) {
                 groups[i] = convertToDcql2Group(targetServiceUrl, className, oldGroup.getGroup(i));
             }
             group.setGroup(groups);
         }
         if (oldGroup.getForeignAssociation() != null && oldGroup.getForeignAssociation().length != 0) {
             ForeignAssociatedObject[] foreignAssociations = new ForeignAssociatedObject[oldGroup.getForeignAssociation().length];
             for (int i = 0; i < oldGroup.getForeignAssociation().length; i++) {
                 foreignAssociations[i] = convertToDcql2ForeignAssociation(oldGroup.getForeignAssociation(i));
             }
             group.setForeignAssociatedObject(foreignAssociations);
         }
         return group;
     }
     
     
     private ForeignAssociatedObject convertToDcql2ForeignAssociation(ForeignAssociation oldForeignAssociation) 
         throws DCQLConversionException {
         ForeignAssociatedObject foreign = new ForeignAssociatedObject();
         foreign.setTargetServiceURL(oldForeignAssociation.getTargetServiceURL());
         DCQLObject object = convertToDcql2Object(
             oldForeignAssociation.getTargetServiceURL(), oldForeignAssociation.getForeignObject());
         foreign.setName(object.getName());
         foreign.setAssociatedObject(object.getAssociatedObject());
         foreign.setAttribute(object.getAttribute());
         foreign.setGroup(object.getGroup());
         foreign.setForeignAssociatedObject(object.getForeignAssociatedObject());
         foreign.setJoinCondition(convertToDcql2Join(oldForeignAssociation.getJoinCondition()));
         return foreign;
     }
     
     
     private JoinCondition convertToDcql2Join(gov.nih.nci.cagrid.dcql.JoinCondition oldJoin) {
         JoinCondition join = new JoinCondition();
         join.setForeignAttributeName(oldJoin.getForeignAttributeName());
         join.setLocalAttributeName(oldJoin.getLocalAttributeName());
        join.setPredicate(foreignPredicateConversion.get(oldJoin.getPredicate()));
         return join;
     }
     
     
     public DCQLQueryResultsCollection convertToDcqlQueryResults(org.cagrid.data.dcql.results.DCQLQueryResultsCollection results) throws DCQLConversionException {
         DCQLQueryResultsCollection oldResultsCollection = new DCQLQueryResultsCollection();
         if (results.getDCQLResult() != null) {
             DCQLResult[] oldResults = new DCQLResult[results.getDCQLResult().length];
             for (int i = 0; i < results.getDCQLResult().length; i++) {
                 oldResults[i] = convertToDcqlResult(results.getDCQLResult(i));
             }
         }
        results.getDCQLResult();
         return oldResultsCollection;
     }
     
     
     private DCQLResult convertToDcqlResult(org.cagrid.data.dcql.results.DCQLResult result) throws DCQLConversionException {
         DCQLResult oldResult = new DCQLResult();
         oldResult.setTargetServiceURL(result.getTargetServiceURL());
         try {
             gov.nih.nci.cagrid.cqlresultset.CQLQueryResults cqlResult = 
                 CQL2ResultsToCQL1ResultsConverter.convertResults(result.getCQLQueryResults());
             oldResult.setCQLQueryResultCollection(cqlResult);
         } catch (ResultsConversionException ex) {
             throw new DCQLConversionException("Error converting inner CQL query results: " + ex.getMessage(), ex);
         }
         return oldResult;
     }
 }
