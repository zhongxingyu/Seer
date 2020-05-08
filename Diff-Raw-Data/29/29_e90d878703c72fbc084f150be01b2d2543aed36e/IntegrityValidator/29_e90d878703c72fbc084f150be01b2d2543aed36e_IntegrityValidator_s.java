 package org.iucn.sis.server.extensions.integrity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentIntegrityValidation;
 import org.iucn.sis.shared.api.models.Edit;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.DBException;
 import com.solertium.db.DBSession;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.query.ExperimentalSelectQuery;
 import com.solertium.db.query.QComparisonConstraint;
 import com.solertium.db.query.QConstraint;
 import com.solertium.db.query.QConstraintGroup;
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.ElementCollection;
 import com.solertium.vfs.VFS;
 import com.solertium.vfs.VFSPathToken;
 
 public class IntegrityValidator {
 	
 	public static int validate_background(Session session, VFS vfs, ExecutionContext ec, Integer assessmentID) throws DBException {
 		final VFSPathToken[] tokens;
 		try {
 			tokens = vfs.list(ValidationResource.ROOT_PATH);
 		} catch (IOException e) {
 			return AssessmentIntegrityValidation.SUCCESS;
 		}
 		
 		int status = AssessmentIntegrityValidation.SUCCESS;
 		
 		for (VFSPathToken token : tokens) {
 			Document rule;
 			try {
 				rule = ValidationResource.getRuleset(token.toString(), vfs);
 			} catch (ResourceException e) {
 				System.out.println("Failed to run rule " + token);
 				continue;
 			}
 			
 			AssessmentIntegrityValidation response = 
 				validate(session, ec, token.toString(), rule, assessmentID);
 			
 			if (response.getStatus() > status)
 				status = response.getStatus();
 			
 			if (response.isFailure())
 				break;
 		}
 		
 		return status;
 	}
 
 	/**
 	 * Validator will attempt to validate each constraint group separately. If
 	 * any of the groups fail, it will attempt to write out what failed and
 	 * return it in the IntegrityValidationResponse. Otherwise, it will return
 	 * success.
 	 * 
 	 * @param ec
 	 *            the execution context
 	 * @param document
 	 *            the integrity validator ruleset document
 	 * @param assessmentID
 	 *            the assessment to validate
 	 * @throws IntegrityValidationResponse
 	 */
 	public static AssessmentIntegrityValidation validate(Session session, ExecutionContext ec,
 			String rule, Document document, Integer assessmentID) throws DBException {
 		final AssessmentIntegrityValidation preCheck = 
 			getExistingValidation(session, ec, rule, assessmentID);
 		if (preCheck != null) {
 			System.out.println("Validating based off a previous run; valid? " + preCheck.isSuccess());
 			return preCheck;
 		}
 		
 		
 		final Map<String, String> errorMessages = new HashMap<String, String>();
 		final Map<String, String> properties = new HashMap<String, String>();
 
 		final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
 		try {
 			query.loadConfig(document.getDocumentElement());
 		} catch (Exception unlikely) {
 			unlikely.printStackTrace();
 			throw new DBException(unlikely);
 		}
 		
 		ElementCollection nodes = new ElementCollection(
 			document.getDocumentElement().getElementsByTagName("message")
 		);
 		for (Element el : nodes)
 			errorMessages.put(el.getAttribute("id"), el.getTextContent());
 		
 		nodes = new ElementCollection(
 			document.getDocumentElement().getElementsByTagName("property")
 		);
 		for (Element el : nodes)
 			properties.put(el.getAttribute("name"), el.getTextContent());
 		
 		String type = BaseDocumentUtils.impl.getAttribute(document.getDocumentElement(), "type");
 		if ("".equals(type))
 			type = "designer";
 		
 		final AssessmentIntegrityValidation response;
 		if ("sql".equals(type)) {
 			String errorMessage = errorMessages.get("sql");
 			if (errorMessage == null)
 				errorMessage = "Failed SQL query in test '" + rule + "'.";
 			
 			final Row.Set rs = new Row.Set();
 			
 			ec.doQuery(toSQL(document, assessmentID), rs);
 			
 			final int status = passesValidation(rs, properties);
 			final String prefix = status == AssessmentIntegrityValidation.WARNING ? "Warning: " : "Failure: ";
 			
 			final List<String> errors = new ArrayList<String>();
 			errors.add(prefix + errorMessage);
 			
 			response = status == AssessmentIntegrityValidation.SUCCESS ? 
 					new AssessmentIntegrityValidation() : new AssessmentIntegrityValidation(errors);
 			response.setRule(rule);
 			response.setStatus(status);
 		}
 		else {
 			// Hold copy for safe keeping
 			final QConstraintGroup originalConstraints = new QConstraintGroup();
 			originalConstraints.constraints
 					.addAll(query.getConstraints().constraints);
 	
 			query.constrain(QConstraint.CG_AND, new QComparisonConstraint(
 					new CanonicalColumnName("assessment", "id"),
 					QConstraint.CT_EQUALS, assessmentID));
 			
 			final Row.Set rs = new Row.Set();
 			
 			ec.doQuery(query, rs);
 	
 			final int status = passesValidation(rs, properties);
 			response = status == AssessmentIntegrityValidation.SUCCESS ? 
 					new AssessmentIntegrityValidation()
 					: new AssessmentIntegrityValidation(getCause(ec,
 							originalConstraints, document, errorMessages, properties, assessmentID));
 			response.setRule(rule);
 			response.setStatus(status);
 		}
 		
 		try {
 			updateStatus(session, rule, assessmentID, response);
 		} catch (PersistentException e) {
 			Debug.println(e);
 		}
 		
 		return response;
 	}
 	
 	private static String toSQL(Document document, Integer assessmentID) {
 		String joins = null, conditions = null;
 		final NodeList nodes = document.getDocumentElement().getChildNodes();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			if ("joins".equals(node.getNodeName()))
 				joins = node.getTextContent();
 			else if ("conditions".equals(node.getNodeName()))
 				conditions = node.getTextContent();
 		}
 		
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT assessment.id FROM assessment ");
 		if (joins != null)
 			sql.append(joins.trim());
 		sql.append(" WHERE (assessment.id = ");
 		sql.append(assessmentID);
 		sql.append(")");
 		if (conditions != null) {
 			sql.append(" AND (");
 			sql.append(conditions);
 			sql.append(")");
 		}
 		
 		return sql.toString();
 	}
 	
 	public static void updateStatus(Session session, String rule, Integer assessmentID, AssessmentIntegrityValidation response) throws PersistentException {
 		final AssessmentIO io = new AssessmentIO(session);
 		
 		AssessmentIntegrityValidation existing = io.getValidation(assessmentID, rule);
 		
 		if (existing == null) {
 			io.addValidation(assessmentID, response);
 		}
 		else {
 			existing.setDate(response.getDate());
 			existing.setMessage(response.getMessage());
 			existing.setStatus(response.getStatus());
 			
 			io.updateValidation(existing);
 		}
 	}
 	
 	private static AssessmentIntegrityValidation getExistingValidation(Session session, ExecutionContext ec, String rule, Integer assessmentID) {
 		final AssessmentIO io = new AssessmentIO(session);
 		
 		final AssessmentIntegrityValidation row = io.getValidation(assessmentID, rule);
 		if (row == null)
 			return null;
 		
 		final Date date2;
 		{
 			final Assessment asm = io.getAssessment(assessmentID);
 			final Edit lastEdit = asm.getLastEdit();
 			if (lastEdit == null)
 				date2 = Calendar.getInstance().getTime();
 			else
 				date2 = lastEdit.getCreatedDate();
 		}
 		
 		if (date2.after(row.getDate()))
 			return null;
 	
 		return row;
 	}
 	
 	/**
 	 * Check to see if a query passes validation, based on the properties set.
 	 * 
 	 * TODO: may need support for other types of failure conditions in the future
 	 * at which time they can go in this check as well...
 	 * 
 	 * @param resultSet
 	 * @param properties
 	 * @return
 	 */
 	private static int passesValidation(Row.Set resultSet, Map<String, String> properties) {
 		final boolean passesValidation;
 		/*
 		 * For the not met condition, validation is failed if the query 
 		 * conditions are NOT met, meaning that there are no results 
 		 * returned.  Therefore, the assessment passes validation if the 
 		 * result set is not empty, that is, it returns at least one result.
 		 * 
 		 * TODO: do we care about allowing x number of results constituting 
 		 * a failure condition?
 		 */
 		if ("not_met".equals(properties.get(AssessmentIntegrityValidation.PROPERTY_FAILURE_CONDITION)))
 			passesValidation = !resultSet.getSet().isEmpty();
 		/*
 		 * By default, validation is failed if the query conditions are met and 
 		 * a result or set of results is returned.  So, by default, the 
 		 * assessment passes validation is the result set is empty.  
 		 * 
 		 */
 		else
 			passesValidation = resultSet.getSet().isEmpty();
 		
 		/*
 		 * If it does not pass, this constitutes either a failure or 
 		 * a warning, depending on the settings...
 		 */
 		if (passesValidation)
 			return AssessmentIntegrityValidation.SUCCESS;
 		else if ("warning".equals(properties.get(AssessmentIntegrityValidation.PROPERTY_FAILURE_MODE)))
 			return AssessmentIntegrityValidation.WARNING;
 		else
 			return AssessmentIntegrityValidation.FAILURE;
 	}
 
 	/**
 	 * If there is a failure found in a group, the one of the following
 	 * situations must have occurred:
 	 * 
 	 * (a) one of the conditions in an OR clause returned a result, therefore
 	 * that clause is the failure condition
 	 * 
 	 * (b) all of the conditions in an AND clause were met, therefore the entire
 	 * group is the failure condition
 	 * 
 	 * For simplicity, clauses like x OR y AND z will be treated as AND clauses
 	 * by the cause parser. Implementers can add more condition groups to garner
 	 * more specific results. TODO: add UI help for this
 	 * 
 	 * @param ec
 	 * @param group
 	 * @return
 	 */
 	private static Collection<String> getCause(ExecutionContext ec,
 			QConstraintGroup group, Document document, 
 			Map<String, String> errorMessages, Map<String, String> properties,  
 			Integer assessmentID)
 			throws DBException {
 		final Collection<String> causes = new ArrayList<String>();
 
 		final QComparisonConstraint constraint = new QComparisonConstraint(
 				new CanonicalColumnName("assessment", "id"),
 				QConstraint.CT_EQUALS, assessmentID);
 
 		final Integer AND = Integer.valueOf(QConstraint.CG_AND);
 
 		int index = 0;
 		while (index < group.constraints.size()) {
 			Object obj = group.constraints.get(index++);
 			Object mode = null;
 			if (group.constraints.size() > index)
 				mode = group.constraints.get(index++);
 
 			QConstraint condition;
 			if (AND.equals(mode)) {
 				final QConstraintGroup iGroup = new QConstraintGroup();
 				iGroup.id = group.getID();
 				iGroup.addConstraint((QConstraint) obj);
 				iGroup.addConstraint(QConstraint.CG_AND,
 						(QConstraint) group.constraints.get(index++));
 				while (group.constraints.size() > index
 						&& AND.equals(group.constraints.get(index++)))
 					iGroup.addConstraint(QConstraint.CG_AND,
 							(QConstraint) group.constraints.get(index++));
 
 				final Collection<QConstraintGroup> groupsToParse = new ArrayList<QConstraintGroup>();
 				for (Object o : iGroup.constraints) {
 					if (o instanceof QConstraintGroup) {
 						int iGroupIndex;
 						if ((iGroupIndex = iGroup.constraints.indexOf(o)) == iGroup.constraints
 								.size() - 1) {
 							iGroup.remove(iGroupIndex);
 						} else {
 							iGroup.remove(iGroupIndex + 1);
 							iGroup.remove(iGroupIndex);
 						}
 						groupsToParse.add((QConstraintGroup) o);
 					}
 				}
 
 				boolean doContinue = true;
 
 				if (!groupsToParse.isEmpty()) {
 					for (QConstraintGroup curGroup : groupsToParse) {
 						final Collection<String> localCauses = getCause(ec,
 								curGroup, document, errorMessages, properties, assessmentID);
 						doContinue &= !localCauses.isEmpty();
 						if (!localCauses.isEmpty())
 							causes.addAll(localCauses);
 					}
 				}
 
 				/*
 				 * At this point we are guaranteed to have no more constraint
 				 * groups, thanks to recursion and such. So if doContinue is
 				 * true, then we want to evaluate the rest of the remaining
 				 * clause.
 				 * 
 				 * If there were no constraint groups, the clause will remain
 				 * untouched...
 				 */
 				if (doContinue) {
 					final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
 					try {
 						query.loadConfig(document.getDocumentElement());
 					} catch (Exception unlikely) {
 						unlikely.printStackTrace();
 						throw new DBException(unlikely);
 					}
 					query.getConstraints().constraints.clear();
 					query.constrain(iGroup);
 					query.constrain(QConstraint.CG_AND, constraint);
 
 					final Row.Set rs = new Row.Set();
 					/*System.out.println("Cause Query?: "
 							+ query.getSQL(ec.getDBSession()));*/
 					ec.doQuery(query, rs);
 
 					int status = passesValidation(rs, properties);
 					if (status != AssessmentIntegrityValidation.SUCCESS) {
 						String prefix = status == AssessmentIntegrityValidation.WARNING ? "Warning: " : "Failure: ";
 						if (errorMessages.containsKey(iGroup.getID()))
 							causes.add(prefix
 									+ getSQL(iGroup, errorMessages, ec.getDBSession()));
 						else {
 							final StringBuilder errorBuilder = new StringBuilder();
 							for (Iterator<Object> iter = iGroup.constraints.listIterator(); iter.hasNext(); ) {
 								final Object possibleConstraint = iter.next();
 								if (possibleConstraint instanceof QConstraint) {
 									QConstraint curConstraint = (QConstraint)possibleConstraint;
 									errorBuilder.append(getSQL(curConstraint, errorMessages, ec.getDBSession()));
 									if (iter.hasNext())
 										errorBuilder.append(" and ");
 								}
 							}
 							causes.add(prefix + errorBuilder.toString());
 						}
 					}
 				}
 			} else {
 				condition = (QConstraint) obj;
 
 				if (condition instanceof QConstraintGroup)
 					causes.addAll(getCause(ec, (QConstraintGroup) condition,
 							document, errorMessages, properties, assessmentID));
 				else {
 					final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
 					try {
 						query.loadConfig(document.getDocumentElement());
 					} catch (Exception unlikely) {
 						unlikely.printStackTrace();
 						throw new DBException(unlikely);
 					}
 					query.getConstraints().constraints.clear();
 					query.constrain(condition);
 					query.constrain(QConstraint.CG_AND, constraint);
 
 					final Row.Set rs = new Row.Set();
 					/*System.out.println("Cause Query?: "
 							+ query.getSQL(ec.getDBSession()));*/
 					ec.doQuery(query, rs);
 
 					int status = passesValidation(rs, properties);
 					if (status != AssessmentIntegrityValidation.SUCCESS) {
 						String prefix = status == AssessmentIntegrityValidation.WARNING ? "Warning: " : "Failure: ";
 						causes.add(prefix
 								+ getSQL(condition, errorMessages, ec.getDBSession()));
 					}
 				}
 			}
 		}
 		/*
 		 * 
 		 * //all ORs for (Object obj : group.constraints) { if (obj instanceof
 		 * QConstraint) { final ExperimentalSelectQuery query = new
 		 * ExperimentalSelectQuery();
 		 * System.out.println("--- loading from config ---"); try {
 		 * query.loadConfig(document.getDocumentElement()); } catch (Exception
 		 * unlikely) { unlikely.printStackTrace(); throw new
 		 * DBException(unlikely); }
 		 * 
 		 * query.constrain((QConstraint)obj);
 		 * query.constrain(QConstraint.CG_AND, constraint);
 		 * 
 		 * final Row.Set rs = new Row.Set(); System.out.println("Cause Query?: "
 		 * + query.getSQL(ec.getDBSession())); ec.doQuery(query, rs);
 		 * 
 		 * if (!rs.getSet().isEmpty()) { return "Failed on condition " +
 		 * ((QConstraint)obj).getSQL(ec.getDBSession()); } } }
 		 */
 		// Impossible
 		// return "Could not determine failure condition in " +
 		// group.getSQL(ec.getDBSession());
 		return causes;
 	}
 	
 	public static String getSQL(final QConstraintGroup iGroup, final Map<String, String> errorMessages, final DBSession ds) {
 		if (errorMessages.containsKey(iGroup.getID()))
 			return errorMessages.get(iGroup.getID());
 		
 		final StringBuffer sb = new StringBuffer(512);
 		final Iterator<Object> it = iGroup.constraints.iterator();
 		if (iGroup.constraints.size() > 1)
 			sb.append("(");
 		boolean first = true;
 		while (it.hasNext()) {
 			final Object in = it.next();
 			// System.out.println("QC is " + in.getClass().getSimpleName());
 			if (in instanceof Integer) {
 				if (!first)
 					if ((int) (Integer) in == QConstraint.CG_AND)
 						sb.append(" AND ");
 					else
 						sb.append(" OR ");
 			} else {
 				sb.append(" ");
 				if (errorMessages.containsKey(((QConstraint)in).getID()))
 					sb.append(errorMessages.get(((QConstraint)in).getID()));
 				else
 					sb.append(((QConstraint) in).getSQL(ds));
 			}
 			first = false;
 		}
 		if (iGroup.constraints.size() > 1)
 			sb.append(")");
 		return sb.toString();
 	}
 	
 	public static String getSQL(final QConstraint constraint, final Map<String, String> errorMessages, final DBSession ds) {
 		if (errorMessages.containsKey(constraint.getID()))
 			return errorMessages.get(constraint.getID());
 		else
 			return constraint.getSQL(ds);
 	}
 
 }
