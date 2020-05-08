 package org.cagrid.iso21090.sdkquery.translator;
 
 import gov.nih.nci.cagrid.cqlquery.Association;
 import gov.nih.nci.cagrid.cqlquery.Attribute;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.cqlquery.Group;
 import gov.nih.nci.cagrid.cqlquery.LogicalOperator;
 import gov.nih.nci.cagrid.cqlquery.Object;
 import gov.nih.nci.cagrid.cqlquery.Predicate;
 import gov.nih.nci.cagrid.cqlquery.QueryModifier;
 import gov.nih.nci.iso21090.DSet;
 
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /** 
  *  CQL2ParameterizedHQL
  *  Converter utility to turn CQL into HQL using positional parameters 
  *  compatible with Hibernate 3.2.0ga
  *  
  *  Supports ISO 21090 data types in the caCORE SDK 4.3
  * 
  * @author David Ervin
  * 
  * @created Mar 2, 2007 10:26:47 AM
  * @version $Id: CQL2ParameterizedHQL.java,v 1.14 2009-04-24 14:53:56 dervin Exp $ 
  */
 public class CQL2ParameterizedHQL {
     public static final String TARGET_ALIAS = "__TargetAlias__";
     
     private static Log LOG = LogFactory.getLog(CQL2ParameterizedHQL.class);
 	
     // maps a CQL predicate to its HQL string representation 
 	private Map<Predicate, String> predicateValues = null;
     
     private TypesInformationResolver typesInformationResolver = null;
     private ConstantValueResolver constantValueResolver = null;
     private boolean caseInsensitive;
     
     
     public CQL2ParameterizedHQL(TypesInformationResolver typesInfoResolver, 
         ConstantValueResolver constantValueResolver, boolean caseInsensitive) {
         this.typesInformationResolver = typesInfoResolver;
         this.constantValueResolver = constantValueResolver;
         this.caseInsensitive = caseInsensitive;
         initPredicateValues();
     }
     
     
     private void initPredicateValues() {
         predicateValues = new HashMap<Predicate, String>();
         predicateValues.put(Predicate.EQUAL_TO, "=");
         predicateValues.put(Predicate.GREATER_THAN, ">");
         predicateValues.put(Predicate.GREATER_THAN_EQUAL_TO, ">=");
         predicateValues.put(Predicate.LESS_THAN, "<");
         predicateValues.put(Predicate.LESS_THAN_EQUAL_TO, "<=");
         predicateValues.put(Predicate.LIKE, "LIKE");
         predicateValues.put(Predicate.NOT_EQUAL_TO, "!=");
         predicateValues.put(Predicate.IS_NOT_NULL, "is not null");
         predicateValues.put(Predicate.IS_NULL, "is null");
     }
     
 	
 	/**
 	 * Converts CQL to parameterized HQL suitable for use with 
      * Hibernate v3.2.0ga
 	 * 
 	 * @param query
 	 * 		The query to convert
 	 * @return
 	 * 		A parameterized HQL Query representing the CQL query
 	 * @throws QueryTranslationException
 	 */
 	public ParameterizedHqlQuery convertToHql(CQLQuery query) throws QueryTranslationException {
 	    LOG.debug("Converting caGrid Query Language to Hibernate Query Language");
 		// create a string builder to build up the HQL
 		StringBuilder rawHql = new StringBuilder();
         
         // create the list in which parameters will be placed
         List<java.lang.Object> parameters = new LinkedList<java.lang.Object>();
         
         // determine if the target has subclasses
         boolean hasSubclasses = false;
         try {
             hasSubclasses = typesInformationResolver.classHasSubclasses(query.getTarget().getName());
         } catch (TypesInformationException ex) {
             throw new QueryTranslationException(ex.getMessage(), ex);
         }
         LOG.debug(query.getTarget().getName() 
             + (hasSubclasses ? " has subclasses" : " has no subclasse"));
         
         // begin processing at the target level
 		processTarget(query.getTarget(), rawHql, parameters, hasSubclasses);
         
         // apply query modifiers
 		if (query.getQueryModifier() != null) {
 			handleQueryModifier(query.getQueryModifier(), rawHql);
 		} else {
 		    // select only unique objects
             rawHql.insert(0, "Select distinct (" + TARGET_ALIAS + ") ");      
         }
         
         // build the final query object
         ParameterizedHqlQuery hqlQuery = new ParameterizedHqlQuery(rawHql.toString(), parameters);
 		return hqlQuery;
 	}
 	
 	
 	/**
 	 * Applies query modifiers to the HQL query
 	 * 
 	 * @param mods
 	 * 		The modifiers to apply
 	 * @param hql
 	 * 		The HQL to apply the modifications to
 	 */
 	private void handleQueryModifier(QueryModifier mods, StringBuilder hql) {
 		StringBuilder prepend = new StringBuilder();
 		if (mods.isCountOnly()) {
 			prepend.append("select count(");
 			if (mods.getDistinctAttribute() != null) {
 				prepend.append("distinct ").append(mods.getDistinctAttribute());
 			} else {
 				prepend.append("distinct " + TARGET_ALIAS);
 			}
 			prepend.append(')');
 		} else {
 		    // select distinct tuples
 			prepend.append("select distinct ");
 			if (mods.getDistinctAttribute() != null) {
 				prepend.append(mods.getDistinctAttribute());
 			} else {
 				for (int i = 0; i < mods.getAttributeNames().length; i++) {
 					prepend.append(mods.getAttributeNames(i));
 					if (i + 1 < mods.getAttributeNames().length) {
 						prepend.append(", ");
 					}
 				}
 			}
 		}
 		
 		prepend.append(' ');
 		
 		hql.insert(0, prepend.toString());
 	}
 	
 	
 	/**
 	 * Processes the target object of a CQL query
 	 * 
 	 * @param target
 	 * 		The target of a CQL query
 	 * @param hql
 	 * 		The hql string builder to append to
      * @param parameters
      *      The list of positional parameter values
 	 * @param avoidSubclasses
 	 * 		A flag to indicate the target has subclasses, which we should not return
 	 * @throws QueryTranslationException
 	 */
 	private void processTarget(Object target, StringBuilder hql, List<java.lang.Object> parameters,
 		boolean avoidSubclasses) throws QueryTranslationException {
 		LOG.debug("Processing target " + target.getName());
         
         // the stack of associations processed at the current depth of the query
 		Stack<Association> associationStack = new Stack<Association>();
 		List<CqlDataBucket> typesProcessingList = new ArrayList<CqlDataBucket>();
         
         // start the query
 		hql.append("From ").append(target.getName()).append(" as ").append(TARGET_ALIAS).append(' ');
 		// keep track of where we are in processing
 		addTypeProcessingInformation(typesProcessingList, target.getName(), TARGET_ALIAS);
 		
 		if (target.getAttribute() != null) {
             hql.append("where ");
             processAttribute(target.getAttribute(), hql, parameters, target, TARGET_ALIAS, associationStack, typesProcessingList);
         }
 		if (target.getAssociation() != null) {
 			hql.append("where ");
 			processAssociation(target.getAssociation(), hql, parameters, associationStack, 
 			    typesProcessingList, target, TARGET_ALIAS);
 		}
 		if (target.getGroup() != null) {
 			hql.append("where ");
 			processGroup(target.getGroup(), hql, parameters, associationStack, typesProcessingList, target, TARGET_ALIAS);
 		}
 		
 		if (avoidSubclasses) {
 		    LOG.debug("Target class has subclasses, appending .class in where clause");
 			boolean mustAddWhereClause = 
 				target.getAssociation() == null
 				&& target.getAttribute() == null
 				&& target.getGroup() == null;
 			if (mustAddWhereClause) {
 				hql.append(" where ");
 			} else {
 				hql.append(" and ");
 			}
 			hql.append(TARGET_ALIAS).append(".class = ?");
 			java.lang.Object classDiscriminatorInstance = null;
 			try {
 			    classDiscriminatorInstance = typesInformationResolver.getClassDiscriminatorValue(target.getName());
 			    LOG.debug("Class discriminator determined to be " + String.valueOf(classDiscriminatorInstance));
 			} catch (TypesInformationException ex) {
 			    String message = "Error determining class discriminator for " + target.getName() + ": " + ex.getMessage();
 			    LOG.error(message, ex);
 			    throw new QueryTranslationException(message, ex);
 			}
             parameters.add(classDiscriminatorInstance);
 		}
 	}
 	
 	
 	/**
 	 * Processes a CQL query attribute into HQL
 	 * 
 	 * @param attribute
 	 * 		The CQL attribute
 	 * @param hql
 	 * 		The HQL statement fragment
      * @param parameters
      *      The positional parameters list
 	 * @param associationTrace
 	 * 		The trace of associations
 	 * @param objectClassName
 	 * 		The class name of the object to which this association belongs
 	 * @throws QueryTranslationException
 	 */
 	private void processAttribute(Attribute attribute, StringBuilder hql, 
         List<java.lang.Object> parameters, Object queryObject, String queryObjectAlias, 
         Stack<Association> associationStack, List<CqlDataBucket> typesProcessingList)
 	    throws QueryTranslationException {
         LOG.debug("Processing attribute " + queryObject.getName() + "." + attribute.getName());
         
         // get the predicate, check for a default value
         Predicate predicate = attribute.getPredicate();
         if (predicate == null) {
             LOG.debug("No predicate defined in query, defaulting to " + Predicate.EQUAL_TO);
             predicate = Predicate.EQUAL_TO;
         }
         
         // determine what the flavor of this attribute is
 		DatatypeFlavor flavor = typesProcessingList.get(typesProcessingList.size() - 1).datatypeFlavor;
 		LOG.debug("Datatype flavor is " + flavor.name());
 		// DSET<Ii>, (and TEL and CD) ends up as "COMPLEX_WITH_SIMPLE_CONTENT" because it's modeled as an
 		// association to DSET, then to Ii, which is that type.  Appears to work OK.
 		// FIXME: DSET<Ad> doesn't work because I can't get the information about the part names inside the AD
 		// out of the Hibernate configuration object API.  Interestingly, AD by itself is fine.
 		switch (flavor) {
 		    case STANDARD:
 		    case ENUMERATION:
 		        processStandardAttribute(attribute, hql, parameters, queryObject, queryObjectAlias);
 		        break;
 		    case COMPLEX_WITH_SIMPLE_CONTENT:
 		        processComplexAttributeWithSimpleOrMixedContent(
 		            attribute, hql, parameters, associationStack, typesProcessingList);
 		        break;
 		    case COMPLEX_WITH_MIXED_CONTENT:
 		        processComplexAttributeWithSimpleOrMixedContent(
                     attribute, hql, parameters, associationStack, typesProcessingList);
 		        break;
 		    case COMPLEX_WITH_COLLECTION_OF_COMPLEX:
 		        if (currentlyWrappedByDset(typesProcessingList)) {
 		            processDsetOfComplexDatatypeWithCollectionOfComplexAttributesWithSimpleContent(
 		                attribute, hql, parameters, associationStack, typesProcessingList);
 		        } else {
 		            processComplexAttributeWithCollectionOfComplexAttributesWithSimpleContent(
 		                attribute, hql, parameters, associationStack, typesProcessingList);
 		        }
 		        break;
 		    case COLLECTION_OF_COMPLEX_WITH_SIMPLE_CONTENT:
 		        processComplexAttributeWithSimpleOrMixedContent(
                     attribute, hql, parameters, associationStack, typesProcessingList);
 		        break;
 		    case COLLECTION_OF_COMPLEX_WITH_COLLECTION_OF_COMPLEX_WITH_SIMPLE_CONTENT:
 		        // gah
 		        break;       
 		}
 	}
 	
 	
 	private boolean currentlyWrappedByDset(List<CqlDataBucket> typesProcessingList) {
 	    boolean wrappedByDset = false;
 	    for (int i = typesProcessingList.size() - 1; i >= 0 && !wrappedByDset; i--) {
 	        String name = typesProcessingList.get(i).clazz;
 	        wrappedByDset = DSet.class.getName().equals(name);
 	    }
 	    return wrappedByDset;
 	}
 	
 	
 	/**
 	 * Processes CQL associations into HQL
 	 * 
 	 * @param association
 	 * 		The CQL association
 	 * @param hql
 	 * 		The HQL fragment which will be edited
      * @param parameters
      *      The positional HQL query parameters
 	 * @param associationTrace
 	 * 		The trace of associations
 	 * @param sourceClassName
 	 * 		The class name of the type to which this association belongs
 	 * @throws QueryTranslationException
 	 */
 	private void processAssociation(Association association, StringBuilder hql, List<java.lang.Object> parameters, 
         Stack<Association> associationStack, List<CqlDataBucket> typesProcessingList,
         Object sourceQueryObject, String sourceAlias) throws QueryTranslationException {
         LOG.debug("Processing association " + sourceQueryObject.getName() + " to " + association.getName());
         
         // get the association's role name
 		String roleName = association.getRoleName();
 		if (roleName == null) {
 		    try {
 		        roleName = typesInformationResolver.getRoleName(sourceQueryObject.getName(), association.getName());
 		    } catch (TypesInformationException ex) {
 		        throw new QueryTranslationException(ex.getMessage(), ex);
 		    }
 		}
 		if (roleName == null) {
 			// still null?? no association to the object!
 			throw new QueryTranslationException("Association from type " + sourceQueryObject.getName() + 
 				" to type " + association.getName() + " does not exist.  Use only direct associations");
 		}
         LOG.debug("Role name determined to be " + roleName);
         
         // determine the alias for this association
         String alias = getAssociationAlias(sourceQueryObject.getName(), association.getName(), roleName);
         LOG.debug("Association alias determined to be " + alias);
         
 		// add this association to the stack
 		associationStack.push(association);
 		DatatypeFlavor flavor = null;
         try {
             flavor = DatatypeFlavor.getFlavorOfClass(Class.forName(stripGeneric(association.getName())));
         } catch (ClassNotFoundException ex) {
             throw new QueryTranslationException("Error determining datatype flavor of " 
                 + association.getName() + ": " + ex.getMessage(), ex);
         }
 		addTypeProcessingInformation(typesProcessingList, association.getName(), flavor == DatatypeFlavor.STANDARD ? alias : roleName);
         
 		if (DatatypeFlavor.STANDARD.equals(flavor)) {
 		    // flag indicates the query is only verifying the association is populated
 		    boolean simpleNullCheck = true;
 		    if (association.getAttribute() != null) {
                 simpleNullCheck = false;
                 hql.append(sourceAlias).append('.').append(roleName);
                 hql.append(".id in (select ").append(alias).append(".id from ");
                 hql.append(association.getName()).append(" as ").append(alias).append(" where ");
                 processAttribute(association.getAttribute(), hql, parameters, association, alias, associationStack, typesProcessingList);
                 hql.append(") ");
             }
 		    if (association.getAssociation() != null) {
 		        simpleNullCheck = false;
 		        // add clause to select things from this association
 		        hql.append(sourceAlias).append('.').append(roleName);            
 		        hql.append(".id in (select ").append(alias).append(".id from ");
 		        hql.append(association.getName()).append(" as ").append(alias).append(" where ");
 		        processAssociation(association.getAssociation(), hql, parameters, associationStack, typesProcessingList, association, alias);
 		        hql.append(") ");
 		    }
 		    if (association.getGroup() != null) {
 		        simpleNullCheck = false;
 		        hql.append(sourceAlias).append('.').append(roleName);            
 		        hql.append(".id in (select ").append(alias).append(".id from ");
 		        hql.append(association.getName()).append(" as ").append(alias).append(" where ");
 		        processGroup(association.getGroup(), hql, parameters, associationStack, typesProcessingList, association, alias);
 		        hql.append(") ");
 		    }
 		    if (simpleNullCheck) {
 		        // query is checking for the association to exist and be non-null
 		        hql.append(sourceAlias).append('.').append(roleName).append(".id is not null ");
 		    }
         } else {
             // complex datatype association (modeled as an attribute, so saying "Person.AD is not null" doesn't make sense...
             // "Person.AD.NullFlavor = NullFlavor.NI, however, is fine
             // FIXME: have to handle complex types here
             boolean simpleNullCheck = true;
             if (association.getAssociation() != null) {
                 simpleNullCheck = false;
                 // continue processing
                 // TODO: does alias need to be role name?
                 processAssociation(association.getAssociation(), hql, parameters, associationStack, typesProcessingList, association, alias);
             }
             if (association.getGroup() != null) {
                 simpleNullCheck = false;
                 // continue processing
                 // TODO: does alias need to be role name?
                 processGroup(association.getGroup(), hql, parameters, associationStack, typesProcessingList, association, alias);
             }
             if (association.getAttribute() != null) {
                 simpleNullCheck = false;
                 // TODO: does sourceAlias need to be roleName??
                 processAttribute(association.getAttribute(), hql, parameters, sourceQueryObject, sourceAlias, associationStack, typesProcessingList);
             }
             if (simpleNullCheck) {
                 // checking for the type not to be null, but .id doesn't work....
                 // depending on the sequence of datatype flavors leading to this point 
                 // we have to construct the HQL in different ways
                 
                 String path = getAssociationNavigationPath(typesProcessingList, 4);
                 if (path.startsWith("join ")) {
                     // throw away the "where" part of the existing query
                     if (hql.toString().endsWith("where ")) {
                         removeLastWhereStatement(hql);
                     }
                 }
                 hql.append(path).append(" is not null");
             }
         }
         
 		// pop this association off the stack
         associationStack.pop();
         clipTypeProcessingInformation(typesProcessingList);
         LOG.debug(associationStack.size() + " associations remain on the stack");
 	}
 	
 	
 	/**
 	 * Processes a CQL group into HQL
 	 * 
 	 * @param group
 	 * 		The CQL Group
 	 * @param hql
 	 * 		The HQL fragment which will be edited
      * @param parameters
      *      The positional HQL query parameters
 	 * @param associationTrace
 	 * 		The trace of associations
 	 * @param sourceClassName
 	 * 		The class to which this group belongs
 	 * @throws QueryTranslationException
 	 */
 	private void processGroup(Group group, StringBuilder hql, List<java.lang.Object> parameters,
         Stack<Association> associationStack, List<CqlDataBucket> typesProcessingList, 
         Object sourceQueryObject, String sourceAlias) throws QueryTranslationException {
         LOG.debug("Processing group on " + sourceQueryObject.getName());
         
 		String logic = convertLogicalOperator(group.getLogicRelation());
 		boolean mustAddLogic = false;
 		
 		// open the group
 		hql.append('(');
 		
 		if (group.getAttribute() != null) {
             for (int i = 0; i < group.getAttribute().length; i++) {
                 mustAddLogic = true;
                 processAttribute(group.getAttribute(i), hql, parameters, 
                     sourceQueryObject, sourceAlias, associationStack, typesProcessingList);
                 if (i + 1 < group.getAttribute().length) {
                     hql.append(' ').append(logic).append(' ');
                 }
             }
         }
 		if (group.getAssociation() != null) {
 		    if (mustAddLogic) {
                 hql.append(' ').append(logic).append(' ');
             }
 			for (int i = 0; i < group.getAssociation().length; i++) {
 				mustAddLogic = true;
 				processAssociation(group.getAssociation(i), hql, parameters, 
 				    associationStack, typesProcessingList, sourceQueryObject, sourceAlias);
 				if (i + 1 < group.getAssociation().length) {
 					hql.append(' ').append(logic).append(' ');
 				}
 			}
 		}
 		if (group.getGroup() != null) {
 			if (mustAddLogic) {
 				hql.append(' ').append(logic).append(' ');
 			}
 			for (int i = 0; i < group.getGroup().length; i++) {
 				processGroup(group.getGroup(i), hql, parameters, associationStack, 
 				    typesProcessingList, sourceQueryObject, sourceAlias);
 				if (i + 1 < group.getGroup().length) {
 					hql.append(' ').append(logic).append(' ');
 				}
 			}
 		}
 		
 		// close the group
 		hql.append(')');
 	}
 	
 	
 	/**
 	 * Converts a logical operator to its HQL string equivalent.
 	 * 
 	 * @param op
 	 * 		The logical operator to convert
 	 * @return
 	 * 		The CQL logical operator as HQL
 	 */
 	private String convertLogicalOperator(LogicalOperator op) throws QueryTranslationException {
 		if (op.getValue().equals(LogicalOperator._AND)) {
 			return "AND";
 		} else if (op.getValue().equals(LogicalOperator._OR)) {
 			return "OR";
 		}
 		throw new QueryTranslationException("Logical operator '" + op.getValue() + "' is not recognized.");
 	}
     
     
     // uses the class type to convert the value to a typed object
     private java.lang.Object valueToObject(Class<?> fieldType, String value) throws QueryTranslationException {
         LOG.debug("Converting \"" + value + "\" to object of type " + fieldType.getName());
         if (DatatypeFlavor.getFlavorOfClass(fieldType) == DatatypeFlavor.ENUMERATION) {
             LOG.debug("Field type is an Enumeration");
             try {
                 Method factoryMethod = fieldType.getMethod("valueOf", String.class);
                 return factoryMethod.invoke(null, value);
             } catch (Exception ex) {
                 throw new QueryTranslationException("Error converting " + value 
                     + " to its enumeration value: " + ex.getMessage(), ex);
             }
         } else {
             if (String.class.equals(fieldType)) {
                 return value;
             }
             if (Integer.class.equals(fieldType)) {
                 return Integer.valueOf(value);
             }
             if (Long.class.equals(fieldType)) {
                 return Long.valueOf(value);
             }
             if (Double.class.equals(fieldType)) {
                 return Double.valueOf(value);
             }
             if (Float.class.equals(fieldType)) {
                 return Float.valueOf(value);
             }
             if (Boolean.class.equals(fieldType)) {
                 return Boolean.valueOf(value);
             }
             if (Character.class.equals(fieldType)) {
                 if (value.length() == 1) {
                     return Character.valueOf(value.charAt(0));
                 } else {
                     throw new QueryTranslationException("The value \"" + value + "\" of length " 
                         + value.length() + " is not a valid character (should be length 1)");
                 }
             }
             if (URI.class.equals(fieldType)) {
                 return URI.create(value);
             }
             if (Date.class.equals(fieldType)) {
                 // try time, then dateTime, then just date
                 List<SimpleDateFormat> formats = new ArrayList<SimpleDateFormat>(3);
                 formats.add(new SimpleDateFormat("HH:mm:ss"));
                 formats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
                 formats.add(new SimpleDateFormat("yyyy-MM-dd"));
 
                 Date date = null;
                 Iterator<SimpleDateFormat> formatIter = formats.iterator();
                 while (date == null && formatIter.hasNext()) {
                     SimpleDateFormat formatter = formatIter.next();
                     try {
                         date = formatter.parse(value);
                         LOG.trace("Parsed by " + formatter.toPattern());
                     } catch (ParseException ex) {
                         LOG.debug(value + " was not parsable by pattern " + formatter.toPattern());
                     }
                 }
                 if (date == null) {
                     throw new QueryTranslationException("Unable to parse date value \"" + value + "\"");
                 }
 
                 return date;
             }
         }
         
         throw new QueryTranslationException("No conversion for type " + fieldType != null ? fieldType.getName() : "null");
     }
     
     
     private String getAssociationAlias(String parentClassName, String associationClassName, String roleName) {
         int dotIndex = parentClassName.lastIndexOf('.');
         String parentShortName = dotIndex != -1 ? parentClassName.substring(dotIndex + 1) : parentClassName;
         dotIndex = associationClassName.lastIndexOf('.');
         String associationShortName = dotIndex != -1 ? associationClassName.substring(dotIndex + 1) : associationClassName;
         String alias = "__" + parentShortName + "_" + associationShortName + "_" + roleName;
         return alias;
     }
     
     
     private void addTypeProcessingInformation(List<CqlDataBucket> typesProcessingList, String className, String aliasOrRoleName) throws QueryTranslationException {
         DatatypeFlavor flavor = null;
         try {
             flavor = DatatypeFlavor.getFlavorOfClass(Class.forName(stripGeneric(className)));
         } catch (Exception ex) {
             throw new QueryTranslationException("Error determining datatype flavor of " + className + ": " + ex.getMessage(), ex);
         }
         CqlDataBucket bucket = new CqlDataBucket();
         bucket.clazz = className;
         bucket.aliasOrRoleName = aliasOrRoleName;
         bucket.datatypeFlavor = flavor;
         typesProcessingList.add(bucket);
     }
     
     
     private void clipTypeProcessingInformation(List<CqlDataBucket> typesProcessingList) {
         typesProcessingList.remove(typesProcessingList.size() - 1);
     }
     
     
     private void processStandardAttribute(Attribute attribute, StringBuilder hql, 
         List<java.lang.Object> parameters, Object queryObject, String queryObjectAlias)
         throws QueryTranslationException {
         LOG.debug("Processing standard attribute");
         
         // construct the query fragment
         if (caseInsensitive) {
             hql.append("lower(");
         }
         
         // append the path to the attribute itself
         hql.append(queryObjectAlias).append('.').append(attribute.getName());
         
         // close up case insensitivity
         if (caseInsensitive) {
             hql.append(')');
         }
         
         appendPredicateAndValue(attribute, hql, parameters, queryObject);
     }
     
     
     private void processComplexAttributeWithSimpleOrMixedContent(Attribute attribute, 
         StringBuilder hql, List<java.lang.Object> parameters, 
         Stack<Association> associationStack, List<CqlDataBucket> typesProcessingList) 
         throws QueryTranslationException {
         LOG.debug("Processing complex attribute with simple or mixed content");
         // determine the number of levels to back up to find a "standard" datatype
         int levels = determineLevelsRemovedFromStandardDatatype(typesProcessingList);
         
         // determine if this value is mapped to a constant
         java.lang.Object constantValue = constantValueResolver.getConstantValue(
             typesProcessingList.get(typesProcessingList.size() - levels).clazz, 
             getAttributePathList(typesProcessingList, attribute.getName(), levels - 1));
         if (constantValue == null) {
             LOG.debug("Attribute was not mapped to a constant");
             // append the path to the attribute itself
             if (caseInsensitive) {
                 hql.append("lower(");
             }
             String path = getAttributePath(typesProcessingList, attribute.getName(), levels);
             if (path.startsWith("join ")) {
                 // throw away the "where" part of the existing query
                 if (hql.toString().endsWith("where ")) {
                     removeLastWhereStatement(hql);
                 }
             }
             hql.append(path);
             if (caseInsensitive) {
                 hql.append(')');
             }
         } else {
             // the value has been mapped to a constant.  Hibernate can't query
             // against these, but we can substitute the value into the query directly
             LOG.debug("Attribute mapped to a constant; appending that constant to the HQL parameters");
             hql.append('?');
             parameters.add(constantValue);
         }
         // get one level back's query object
         appendPredicateAndValue(attribute, hql, parameters, associationStack.peek());
     }
     
     
     private void processComplexAttributeWithCollectionOfComplexAttributesWithSimpleContent(
         Attribute attribute, StringBuilder hql, List<java.lang.Object> parameters, 
         Stack<Association> associationStack, List<CqlDataBucket> typesProcessingList) throws QueryTranslationException {
         LOG.debug("Processing complex attribute collection of attributes with simple content");
         int levels = determineLevelsRemovedFromStandardDatatype(typesProcessingList);
         System.out.println("Levels: " + levels);
         if (levels == 2) {
             // we're probably processing an enum value of the most recent association class
             List<String> attribPath = getAttributePathList(typesProcessingList, attribute.getName(), 1);
             // determine if this value is mapped to a constant
             java.lang.Object constantValue = constantValueResolver.getConstantValue(
                 typesProcessingList.get(typesProcessingList.size() - levels).clazz, 
                 attribPath);
             if (constantValue == null) {
                 LOG.debug("Attribute was not mapped to a constant");
                 // append the path to the attribute itself
                 if (caseInsensitive) {
                     hql.append("lower(");
                 }
                 String path = getAttributePath(typesProcessingList, attribute.getName(), levels);
                 if (path.startsWith("join ")) {
                     // throw away the "where" part of the existing query
                     if (hql.toString().endsWith("where ")) {
                         removeLastWhereStatement(hql);
                     }
                 }
                 hql.append(path);
                 if (caseInsensitive) {
                     hql.append(')');
                 }
             } else {
                 // the value has been mapped to a constant.  Hibernate can't query
                 // against these, but we can substitute the value into the query directly
                 LOG.debug("Attribute mapped to a constant; appending that constant to the HQL parameters");
                 hql.append('?');
                 parameters.add(constantValue);
             }
             // get one level back's query object
             appendPredicateAndValue(attribute, hql, parameters, associationStack.peek());
         } else {
             // some nested component path
             // build the query path with a place holder for the part names
             String componentNamePlaceholder = "---placeholder---";
             int listSize = typesProcessingList.size();
             int endIndex = listSize - 3;
             StringBuffer buf = new StringBuffer();
             for (int i = endIndex; i < listSize; i++) {
                 if (i == listSize - 1) {
                     buf.append(componentNamePlaceholder);
                 } else {
                     buf.append(typesProcessingList.get(i).aliasOrRoleName);
                 }
                 buf.append('.');
             }
             buf.append(attribute.getName());
 
             // get the part names out of the types information
             List<String> componentNames = typesInformationResolver.getInnerComponentNames(
                 typesProcessingList.get(typesProcessingList.size() - 3).clazz, 
                 typesProcessingList.get(typesProcessingList.size() - 2).aliasOrRoleName, 
                 typesProcessingList.get(typesProcessingList.size() - 1).aliasOrRoleName);
             Iterator<String> nameIter = componentNames.iterator();
 
             // build the query fragment
             hql.append("(");
             while (nameIter.hasNext()) {
                 if (caseInsensitive) {
                     hql.append("lower(");
                 }
                 String fragment = buf.toString().replace(componentNamePlaceholder, nameIter.next());
                 hql.append(fragment);
                 if (caseInsensitive) {
                     hql.append(")");
                 }
                 appendPredicateAndValue(attribute, hql, parameters, associationStack.peek());
                 if (nameIter.hasNext()) {
                     hql.append(" or ");
                 }
             }
             hql.append(")");
         }
     }
     
     
     private void processDsetOfComplexDatatypeWithCollectionOfComplexAttributesWithSimpleContent(Attribute attribute,
         StringBuilder hql, List<java.lang.Object> parameters,  Stack<Association> associationStack,
         List<CqlDataBucket> typesProcessingList) throws QueryTranslationException {
         LOG.debug("Processing DSet of complex attribute with a collection of attributes with simple content");
         // strip the last where statement from the hql
         int whereStart = hql.lastIndexOf("where");
         hql.delete(whereStart, whereStart + "where".length());
         
         String topLevelAlias = typesProcessingList.get(typesProcessingList.size() - 4).aliasOrRoleName;
         hql.append("join ");
         hql.append(topLevelAlias).append(".");
         hql.append(typesProcessingList.get(typesProcessingList.size() - 3).aliasOrRoleName).append(".");
         hql.append(typesProcessingList.get(typesProcessingList.size() - 2).aliasOrRoleName).append(" ");
         // need a random alias
         String randAlias = "alias_" + System.currentTimeMillis();
         hql.append(randAlias).append(" where ");
         
         // get part names, "randalias.part_0.attributeName predicate value"
         // build the query path with a place holder for the part names
         String componentNamePlaceholder = "---placeholder---";
         StringBuffer buf = new StringBuffer();
         buf.append(randAlias).append(".");
         buf.append(componentNamePlaceholder).append(".");
         buf.append(attribute.getName());
         
         // get the part names out of the types information
         List<String> componentNames = typesInformationResolver.getNestedInnerComponentNames(
             typesProcessingList.get(typesProcessingList.size() - 4).clazz, 
             typesProcessingList.get(typesProcessingList.size() - 3).aliasOrRoleName, 
             typesProcessingList.get(typesProcessingList.size() - 2).aliasOrRoleName,
             typesProcessingList.get(typesProcessingList.size() - 1).aliasOrRoleName);
         Iterator<String> nameIter = componentNames.iterator();
         
         // build the query fragment
         hql.append("(");
         while (nameIter.hasNext()) {
             if (caseInsensitive) {
                 hql.append("lower(");
             }
             String fragment = buf.toString().replace(componentNamePlaceholder, nameIter.next());
             hql.append(fragment);
             if (caseInsensitive) {
                 hql.append(")");
             }
             appendPredicateAndValue(attribute, hql, parameters, associationStack.peek());
             if (nameIter.hasNext()) {
                 hql.append(" or ");
             }
         }
         hql.append(")");
      }
     
     
     private void appendPredicateAndValue(Attribute attribute, StringBuilder hql, 
         List<java.lang.Object> parameters, Object queryObject) throws QueryTranslationException {
         LOG.debug("Appending predicate to HQL determining object value");
         // determine if the predicate is unary
         Predicate predicate = attribute.getPredicate();
         boolean unaryPredicate = predicate.equals(Predicate.IS_NOT_NULL)
             || predicate.equals(Predicate.IS_NULL);
         LOG.debug("Predicate " + predicate.getValue() + " is " + (unaryPredicate ? "not " : "") + "unary");
         // append the predicate
         hql.append(' ');
         String predicateAsString = predicateValues.get(predicate);
         if (!unaryPredicate) {
             hql.append(predicateAsString).append(' ');
 
             // add a placeholder parameter to the HQL query
             hql.append('?');
 
             // convert the attribute value to the specific data type of the attribute
             Class<?> attributeType = null;
             try {
                attributeType = typesInformationResolver.getJavaDataType(stripGeneric(queryObject.getName()), attribute.getName());
             } catch (TypesInformationException ex) {
                 LOG.error("Error determining type: " + ex.getMessage(), ex);
                 throw new QueryTranslationException(ex.getMessage(), ex);
             }
             if (attributeType == null) {
                 throw new QueryTranslationException("No type could be determined for attribute " 
                     + queryObject.getName() + "." + attribute.getName());
             }
             LOG.debug("Determined java type to be " + attribute.getName());
             java.lang.Object value = valueToObject(attributeType, 
                 caseInsensitive ? attribute.getValue().toLowerCase() : attribute.getValue());
 
             // add a positional parameter value to the list            
             parameters.add(value);
         } else {
             // binary predicates just get appended w/o values associated with them
             hql.append(predicateAsString);
         }
     }
     
     
     private List<String> getAttributePathList(List<CqlDataBucket> typesProcessingList, String attribName, int levels) {
         List<String> path = new ArrayList<String>();
         int listSize = typesProcessingList.size();
         int endIndex = listSize - levels;
         for (int i = endIndex; i < listSize; i++) {
             path.add(typesProcessingList.get(i).aliasOrRoleName);
         }
         path.add(attribName);
         return path;
     }
     
     
     private String getAttributePath(List<CqlDataBucket> typesProcessingList, String attribName, int levels) {
         String path = getAssociationNavigationPath(typesProcessingList, levels);
         path += "." + attribName;
         return path;
     }
     
     
     private String getAssociationNavigationPath(List<CqlDataBucket> typesProcessing, int levels) {
         LOG.debug("Getting association navigation path");
         StringBuffer buf = new StringBuffer();
         int listSize = typesProcessing.size();
         int endIndex = listSize - levels;
         boolean useJoinSyntax = false;
         // figure out if I need to add the special join syntax
         if (typesProcessing.get(endIndex + 1).datatypeFlavor
                 .equals(DatatypeFlavor.COLLECTION_OF_COMPLEX_WITH_COLLECTION_OF_COMPLEX_WITH_SIMPLE_CONTENT)
             || typesProcessing.get(endIndex + 1).datatypeFlavor
                 .equals(DatatypeFlavor.COLLECTION_OF_COMPLEX_WITH_SIMPLE_CONTENT)) {
             useJoinSyntax = true;
         }
         if (useJoinSyntax) {
             LOG.debug("Association path requires a join");
             buf.append("join ");
             buf.append(typesProcessing.get(endIndex).aliasOrRoleName).append(".");
             buf.append(typesProcessing.get(endIndex + 1).aliasOrRoleName).append(".");
             buf.append(typesProcessing.get(endIndex + 2).aliasOrRoleName);
             // need a random alias here
             String randAlias = "alias_" + System.currentTimeMillis();
             buf.append(" as ").append(randAlias).append(" where ").append(randAlias);
             if (levels > 3) {
                 buf.append(".");
                 for (int i = endIndex + 3; i < listSize; i++) {
                     buf.append(typesProcessing.get(i).aliasOrRoleName);
                     if (i + 1 < listSize) {
                         buf.append('.');
                     }
                 }
             }
         } else {
             LOG.debug("Association path doesn't need a join");
             // this is the easy case
             for (int i = endIndex; i < listSize; i++) {
                 buf.append(typesProcessing.get(i).aliasOrRoleName);
                 if (i + 1 < listSize) {
                     buf.append('.');
                 }
             }
         }
         return buf.toString();
     }
     
     
     private int determineLevelsRemovedFromStandardDatatype(List<CqlDataBucket> typesProcessingList) {
         int count = 0;
         for (int i = typesProcessingList.size() - 1; i >= 0; i--) {
             count++;
             if (DatatypeFlavor.STANDARD.equals(typesProcessingList.get(i).datatypeFlavor)) {
                 break;
             }
         }
         return count;
     }
     
     
     private String stripGeneric(String className) {
         String stripped = className;
         int index = className.indexOf("<");
         if (index != -1) {
             LOG.debug("Stripping generic portion off class name " + className);
             stripped = className.substring(0, index);
         }
         return stripped;
     }
     
     
     private void removeLastWhereStatement(StringBuilder hql) {
         LOG.debug("Removing last where statement from HQL");
         int index = hql.lastIndexOf("where ");
         hql.delete(index, index + "where ".length());
     }
 }
