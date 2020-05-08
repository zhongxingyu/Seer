 package fr.cg95.cvq.dao.request.hibernate;
 
 import java.lang.reflect.InvocationTargetException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.type.Type;
 import org.joda.time.DateTime;
 
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestActionType;
 import fr.cg95.cvq.business.request.RequestData;
 import fr.cg95.cvq.business.request.RequestLock;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.DateUtils;
 
 /**
  * Hibernate implementation of the {@link IRequestDAO} interface.
  * 
  * @author bor@zenexity.fr
  */
 public class RequestDAO extends GenericDAO implements IRequestDAO {
 
     public List<Request> search(final Set<Critere> criteria, final String sort, String dir, 
         int recordsReturned, int startIndex, final boolean full) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestData as request").append(" where 1 = 1 ");
 
         List<Object> parametersValues = new ArrayList<Object>();
         List<Type> parametersTypes = new ArrayList<Type>();
         
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_ID)) {
                 if (Critere.IN.equals(searchCrit.getComparatif())) {
                     Collection<Long> requestIds =
                         (Collection<Long>)searchCrit.getValue();
                     String[] values = new String[requestIds.size()];
                     int i = 0;
                     for (Long requestId : requestIds) {
                         values[i++] = requestId.toString();
                     }
                     sb.append(" and request.id in (")
                         .append(StringUtils.join(values, ", ")).append(')');
                 } else {
                     sb.append(" and request.id " + searchCrit.getComparatif() + " ?");
                     parametersValues.add(searchCrit.getLongValue());
                     parametersTypes.add(Hibernate.LONG);
                 }
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and request.homeFolderId " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUESTER_LASTNAME)) {
                 sb.append(" and lower(request.requesterLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_LASTNAME)) {
                 sb.append(" and lower(request.subjectLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_ID)) {
                 sb.append(" and request.subjectId " + searchCrit.getSqlComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_NAME)) {
                 sb.append(" and request.requestType.category.name "
                         + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getValue());
                 parametersTypes.add(Hibernate.STRING);
             
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_ID)) {
                 sb.append(" and request.requestType.category.id "
                         + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_ID)) {
                 sb.append(" and request.requestType " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_LABEL)) {
                 if (Critere.IN.equals(searchCrit.getComparatif())) {
                     Collection<String> requestTypes =
                         (Collection<String>)searchCrit.getValue();
                     String[] values = new String[requestTypes.size()];
                     int i = 0;
                     for (String requestType : requestTypes) {
                         values[i++] = "'" + requestType + "'";
                     }
                     sb.append(" and request.requestType.label in (")
                         .append(StringUtils.join(values, ", ")).append(')');
                 } else {
                     sb.append(" and request.requestType.label " + searchCrit.getComparatif() + " ?");
                     parametersValues.add(searchCrit.getValue());
                     parametersTypes.add(Hibernate.STRING);
                 }
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_STATE)) {
                 if (Critere.IN.equals(searchCrit.getComparatif())) {
                     Collection<RequestState> states =
                         (Collection<RequestState>) searchCrit.getValue();
                     String[] values = new String[states.size()];
                     int i = 0;
                     for (RequestState state : states) {
                         values[i++] = "'" + state.toString() + "'";
                     }
                     sb.append(" and request.state in (")
                         .append(StringUtils.join(values, ", ")).append(')');
                 } else {
                     sb.append(" and request.state " + searchCrit.getComparatif() + " ?");
                     // To ensure we put the good type in the object list
                     // FIXME : all states criteria should be sent as RequestState objects
                     if (searchCrit.getValue() instanceof RequestState)
                         parametersValues.add(searchCrit.getValue().toString());
                     else
                         parametersValues.add(searchCrit.getValue());
                     parametersTypes.add(Hibernate.STRING);
                 }
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CREATION_DATE)) {
                 sb.append(" and request.creationDate " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getDateValue());
                 parametersTypes.add(Hibernate.TIMESTAMP);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_VALIDATION_DATE)) {
                 sb.append(" and request.validationDate " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getDateValue());
                 parametersTypes.add(Hibernate.TIMESTAMP);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_MODIFICATION_DATE)) {
                 sb.append(" and request.lastModificationDate " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getDateValue());
                 parametersTypes.add(Hibernate.DATE);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_INTERVENING_USER_ID)) {
                 sb.append(" and request.lastInterveningUserId " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(" and request.requestType.category.id in ( "
                         + searchCrit.getValue() + ")");
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_QUALITY_TYPE)) {
                  
                 if (searchCrit.getValue().equals(Request.QUALITY_TYPE_ORANGE)) {
                  sb.append(" and request.orangeAlert = true")
                 .append(" and request.redAlert = false");
                 } else if (searchCrit.getValue().equals(Request.QUALITY_TYPE_RED)) {
                     sb.append(" and request.orangeAlert = false")
                         .append(" and request.redAlert = true");
                 } else if (searchCrit.getValue().equals(Request.QUALITY_TYPE_OK)) {
                     sb.append(" and request.orangeAlert = false")
                         .append(" and request.redAlert = false");
                 }
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SEASON_ID)) {
                 sb.append(" and request.requestSeason.id "
                         + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             }
         }
         
         if (sort != null) {
             if (sort.equals(Request.SEARCH_BY_REQUEST_ID))
                 sb.append(" order by request.id");
             else if (sort.equals(Request.SEARCH_BY_HOME_FOLDER_ID))
                 sb.append(" order by request.homeFolderId");
             else if (sort.equals(Request.SEARCH_BY_REQUESTER_LASTNAME))
                 sb.append(" order by request.requesterLastName, request.requesterFirstName");
             else if (sort.equals(Request.SEARCH_BY_SUBJECT_LASTNAME))
                 sb.append(" order by request.subjectLastName, request.subjectFirstName");
             else if (sort.equals(Request.SEARCH_BY_CATEGORY_NAME))
                 sb.append(" order by request.requestType.category.name");
             else if (sort.equals(Request.SEARCH_BY_CREATION_DATE))
                 sb.append(" order by request.creationDate");
             else if (sort.equals(Request.SEARCH_BY_LAST_MODIFICATION_DATE))
                 sb.append(" order by request.lastModificationDate");
             else if (sort.equals(Request.SEARCH_BY_LAST_INTERVENING_USER_ID))
                 sb.append(" order by request.lastInterveningUserId");
             else
                 sb.append(" order by request.id");
         } else {
             // default sort order
             sb.append(" order by request.id");
         }
 
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
         
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setParameters(parametersValues.toArray(), parametersTypes.toArray(new Type[0]));
         
         if (recordsReturned > 0)
             query.setMaxResults(recordsReturned);
         query.setFirstResult(startIndex);
         
         return transform(query.list(), full);
     }
 
     /**
      * A customized search method for cases where we just want the requests
      * count. Request is "manually" generated in order to do a single request in
      * DB, which is a <b>lot</b> more performant than Hibernate generated
      * queries
      */
     protected Long searchCount(final Set<Critere> criteria) {
 
         StringBuffer sbSelect = new StringBuffer();
         sbSelect.append("select count(*) from RequestData as request");
 
         StringBuffer sb = new StringBuffer(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
         
         boolean joinedWithRequestAction = false;
         
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_ID)) {
                 if (Critere.IN.equals(searchCrit.getComparatif())) {
                     Collection<Long> requestIds =
                         (Collection<Long>)searchCrit.getValue();
                     String[] values = new String[requestIds.size()];
                     int i = 0;
                     for (Long requestId : requestIds) {
                         values[i++] = requestId.toString();
                     }
                     sb.append(" and request.id in (")
                         .append(StringUtils.join(values, ", ")).append(')');
                 } else {
                     sb.append(" and request.id " + searchCrit.getComparatif() + " ?");
                     objectList.add(searchCrit.getLongValue());
                     typeList.add(Hibernate.LONG);
                 }
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and request.homeFolderId " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUESTER_LASTNAME)) {
                 sb.append(" and lower(request.requesterLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_LASTNAME)) {
                 sb.append(" and lower(request.subjectLastName) "
                         + searchCrit.getSqlComparatif() + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SUBJECT_ID)) {
                 sb.append(" and request.subjectId " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_STATE)) {
                 if (Critere.IN.equals(searchCrit.getComparatif())) {
                     Collection<RequestState> states =
                         (Collection<RequestState>) searchCrit.getValue();
                     String[] values = new String[states.size()];
                     int i = 0;
                     for (RequestState state : states) {
                         values[i++] = "'" + state.toString() + "'";
                     }
                     sb.append(" and request.state in (")
                         .append(StringUtils.join(values, ", ")).append(')');
                 } else {
                     sb.append(" and state " + searchCrit.getComparatif() + " ?");
                     // To ensure we put the good type in the object list
                     // FIXME : all states criteria should be sent as
                     // RequestState objects
                     if (searchCrit.getValue() instanceof RequestState)
                         objectList.add(searchCrit.getValue().toString());
                     else
                         objectList.add(searchCrit.getValue());
                     typeList.add(Hibernate.STRING);
                 }
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_ID)) {
                 sb.append(" and request.requestType.category.id "
                         + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CATEGORY_NAME)) {
                 sb.append(" and request.requestType.category.name "
                         + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_ID)) {
                 sb.append(" and request.requestType " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_REQUEST_TYPE_LABEL)) {
                 sb.append(" and request.requestType.label " + searchCrit.getComparatif()
                         + " ?");
                 objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_RESULTING_STATE)) {
                 sb.append(" and action.resultingState ")
                     .append(searchCrit.getComparatif()).append(" ?");
 
                 if (!joinedWithRequestAction)
                     sbSelect.append(" join request.actions action");
                 
                 joinedWithRequestAction = true;
                 objectList.add(searchCrit.getValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_CREATION_DATE)) {
                 sb.append(" and request.creationDate " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_MODIFICATION_DATE)) {
                 sb.append(" and request.lastModificationDate " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_LAST_INTERVENING_USER_ID)) {
                 sb.append(" and request.lastInterveningUserId "
                         + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_MODIFICATION_DATE)) {
                 sb.append(" and action.date ")
                     .append(searchCrit.getComparatif()).append(" ? ");
 
                 if (!joinedWithRequestAction)
                     sbSelect.append(" join request.actions action");
 
                 joinedWithRequestAction = true;
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals("belongsToCategory")) {
                 sb.append(" and request.requestType.category.id in ( "
                         + searchCrit.getValue() + ")");
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_QUALITY_TYPE)) {
                  
                 if (searchCrit.getValue().equals(Request.QUALITY_TYPE_ORANGE)) {
                  sb.append(" and request.orangeAlert = true")
                 .append(" and request.redAlert = false");
                 } else if (searchCrit.getValue().equals(Request.QUALITY_TYPE_RED)) {
                     sb.append(" and request.orangeAlert = false")
                         .append(" and request.redAlert = true");
                 } else if (searchCrit.getValue().equals(Request.QUALITY_TYPE_OK)) {
                     sb.append(" and request.orangeAlert = false")
                         .append(" and request.redAlert = false");
                 }
             } else if (searchCrit.getAttribut().equals(Request.SEARCH_BY_SEASON_ID)) {
                 sb.append(" and request.requestSeason.id "
                     + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
             }
         }
         
         sbSelect.append(sb);
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return (Long) HibernateUtil.getSession()
             .createQuery(sbSelect.toString())
             .setParameters(objectTab, typeTab)
             .iterate().next(); 
     }
 
     public Long count(final Set<Critere> criteria) {
         return searchCount(criteria).longValue();
     }
 
     public List<Request> listByRequester(final Long requesterId, final boolean full) {
         Query query = HibernateUtil.getSession()
             .createQuery("from RequestData as request where request.requesterId = :requesterId");
         query.setLong("requesterId", requesterId);
         return transform(query.list(), full);
     }
 
     public List<Request> listBySubject(final Long subjectId, final boolean full) {
         Query query = HibernateUtil.getSession()
             .createQuery("from RequestData as request where request.subjectId = :subjectId");
         query.setLong("subjectId", subjectId);
         return transform(query.list(), full);
     }
 
     public List<Request> listBySubjectAndLabel(Long subjectId, String label,
         RequestState[] excludedStates, final boolean full) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from RequestData as request");
 
         sb.append(" where request.requestType.label = ?");
         objectList.add(label);
         typeList.add(Hibernate.STRING);
 
         sb.append(" and request.subjectId = ?");
         objectList.add(subjectId);
         typeList.add(Hibernate.LONG);
         
         if (excludedStates != null && excludedStates.length > 0) {
             for (int i = 0; i < excludedStates.length; i++) {
                 sb.append(" and request.state != ?");
                 objectList.add(excludedStates[i].toString());
                 typeList.add(Hibernate.STRING);
             }
         }
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         query.setParameters(objectTab, typeTab);
         return transform(query.list(), full);
     }
 
     public List<Request> listByHomeFolder(final Long homeFolderId, final boolean full) {
         Query query = HibernateUtil.getSession()
            .createQuery("from RequestData as request where request.homeFolderId = :homeFolderId and request.state != :draft");
         query.setLong("homeFolderId", homeFolderId);
         query.setString("draft", RequestState.DRAFT.toString());
         return transform(query.list(), full);
     }
 
     public List<Request> listByHomeFolderAndLabel(final Long homeFolderId, final String label,
         final RequestState[] excludedStates, final boolean full) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer().append("from RequestData as request");
 
         sb.append(" where request.homeFolderId = ?");
         objectList.add(homeFolderId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and request.requestType.label = ?");
         objectList.add(label);
         typeList.add(Hibernate.STRING);
 
         if (excludedStates != null && excludedStates.length > 0) {
             for (int i = 0; i < excludedStates.length; i++) {
                 sb.append(" and request.state != ?");
                 objectList.add(excludedStates[i].toString());
                 typeList.add(Hibernate.STRING);
             }
         }
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         query.setParameters(objectTab, typeTab);
         return transform(query.list(), full);
     }
 
     public List<Request> listByStates(final Set<RequestState> states, final boolean full) {
         return listByStatesAndType(states, null, full);
     }
 
     public List<Request> listByStatesAndType(final Set<RequestState> states, 
         final String requestTypeLabel, final boolean full) {
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestData as request ");
 
         boolean firstStatement = true;
         for (RequestState requestState : states) {
             if (firstStatement) {
                 sb.append("where request.state = ? ");
                 firstStatement = false;
             } else {
                 sb.append("or request.state = ? ");
             }
 
             objectList.add(requestState.toString());
             typeList.add(Hibernate.STRING);
         }
 
         if (requestTypeLabel != null) {
             sb.append("and request.requestType.label = ? ");
             objectList.add(requestTypeLabel);
             typeList.add(Hibernate.STRING);
         }
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         query.setParameters(objectTab, typeTab);
         return transform(query.list(), full);
     }
 
     public List<Request> listByNotMatchingActionLabel(final RequestActionType type,
         final boolean full) {
         Query query = HibernateUtil.getSession()
             .createQuery("from RequestData as request where request.id not in (select request.id from RequestData request join request.actions action  where action.type = :type)");
         query.setString("type", type.toString());
         return transform(query.list(), full);
     }
 
     public List<Long> listHomeFolderSubjectIds(Long homeFolderId, String label, 
             RequestState[] excludedStates) {
         
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         StringBuffer sb = new StringBuffer()
             .append("select request.subjectId from RequestData as request");
 
         sb.append(" where request.homeFolderId = ?");
         objectList.add(homeFolderId);
         typeList.add(Hibernate.LONG);
 
         sb.append(" and request.requestType.label = ?");
         objectList.add(label);
         typeList.add(Hibernate.STRING);
 
         if (excludedStates != null && excludedStates.length > 0) {
             for (RequestState excludedState : excludedStates) {
                 sb.append(" and request.state != ?");
                 objectList.add(excludedState.toString());
                 typeList.add(Hibernate.STRING);
             }
         }
         Type[] typeTab = typeList.toArray(new Type[1]);
         Object[] objectTab = objectList.toArray(new Object[1]);
 
         return (List<Long>)HibernateUtil.getSession().createQuery(sb.toString())
             .setParameters(objectTab, typeTab).list();
     }
     
     @Override
     public List<Request> listDraftsToNotify(Date date) {
         
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
         
         StringBuffer sb = new StringBuffer();
         sb.append("from RequestData as request ").append("where request.id not in (");
         sb.append("select request.id from RequestData request join request.actions action ")
             .append(" where action.type = ?").append(")");
         sb.append(" and request.homeFolderId not in");
         sb.append(" (select hf.id from HomeFolder hf where hf.temporary is true)");
         sb.append(" and request.state = ?");
         sb.append(" and request.creationDate <= ?");
 
         typeList.add(Hibernate.STRING);
         typeList.add(Hibernate.STRING);
         typeList.add(Hibernate.TIMESTAMP);
         
         objectList.add(RequestActionType.DRAFT_DELETE_NOTIFICATION.toString());
         objectList.add(RequestState.DRAFT.toString());
         objectList.add(date);
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         Type[] typeTab = typeList.toArray(new Type[1]);
         Object[] objectTab = objectList.toArray(new Object[1]);
         query.setParameters(objectTab, typeTab);
         return transform(query.list(), false);
     }
 
     @Override
     public List<Request> listRequestsToExport(final String resultingState,
             final Date startDate, final Date endDate,
             final List<String> requestTypesLabel) {
 
         StringBuffer sb = new StringBuffer();
 
         sb.append("select request_id from request, request_action, request_type")
             .append(" where request.id=request_action.request_id");
 
         if (startDate != null) {
             sb.append(" and request_action.date > '")
                 .append(DateUtils.formatDate(startDate) + "'");
         }
         if (endDate != null) {
             sb.append(" and request_action.date < '")
                 .append(DateUtils.formatDate(endDate) + "'");
         }
 
         if (resultingState != null && !resultingState.equals("")) {
             sb.append(" and request_action.resulting_state = '").append(resultingState).append("'");
         }
 
         if (requestTypesLabel != null) {
             sb.append(" and request.request_type_id = request_type.id and request_type.label in (");
             for (int i = 0; i < requestTypesLabel.size(); i++) {
                 sb.append("'").append(requestTypesLabel.get(i)).append("'");
                 if (i != requestTypesLabel.size() - 1)
                     sb.append(",");
             }
             sb.substring(sb.length() - 1);
             sb.append(" )");
         }
         sb.append(" group by request_id");
         
         List<Request> result = new ArrayList<Request>();
         try {
             ResultSet resultSet = HibernateUtil.getSession().connection()
                 .createStatement().executeQuery(sb.toString());
             if (resultSet.next()) {
                 do {
                     Long requestId = resultSet.getLong(1);
                     result.add((Request) findById(Request.class, requestId));
                 } while (resultSet.next());
                 return result;
             } else {
                 return result;
             }
         } catch (SQLException e) {
             return result;
         } catch (CvqObjectNotFoundException confe) {
             // unlikely to happen
             return result;
         }
     }
 
     @Override
     public RequestLock getRequestLock(Long requestId) {
         return (RequestLock)
             HibernateUtil.getSession().createCriteria(RequestLock.class)
             .add(Restrictions.eq("requestId", requestId)).uniqueResult();
     }
 
     @Override
     public void cleanRequestLocks(int maxDelay) {
         HibernateUtil.getSession().createQuery("delete from RequestLock where date < :date")
             .setTimestamp("date", new DateTime().minusMinutes(maxDelay).toDate()).executeUpdate();
     }
 
     @Override
     public <T> T saveOrUpdate(T object) {
         if (Request.class.isAssignableFrom(object.getClass())) {
             Request request = (Request)object;
             RequestData requestData = request.getRequestData();
             try {
                 Object specificData = request.getSpecificData();
                 requestData.setSpecificDataClass(specificData.getClass());
                 specificData = super.saveOrUpdate(specificData);
                 try {
                     requestData.setSpecificDataId((Long)specificData.getClass().getMethod("getId").invoke(specificData));
                 } catch (IllegalAccessException e) {
                     // this should not happen...
                     throw new RuntimeException(e);
                 } catch (InvocationTargetException e) {
                     // this should not happen...
                     throw new RuntimeException(e);
                 } catch (NoSuchMethodException e) {
                     // this should not happen...
                     throw new RuntimeException(e);
                 }
             } catch (CvqException e) {
                 // no specific data, we are handling a raw Request
             }
             super.saveOrUpdate(requestData);
             return object;
         }
         return super.saveOrUpdate(object);
     }
 
     @Override
     public Object findById(Class<?> clazz, Long id)
         throws CvqObjectNotFoundException {
         if (Request.class.isAssignableFrom(clazz)) {
             return findById(id, true);
         }
         return super.findById(clazz, id);
     }
 
     public Request findById(Long id, final boolean full)
         throws CvqObjectNotFoundException {
         RequestData requestData = (RequestData)findById(RequestData.class, id);
         return full ? recompose(requestData) : new Request(requestData);
     }
 
     @Override
     public Long create(Object object) {
         if (Request.class.isAssignableFrom(object.getClass())) {
             return saveOrUpdate((Request)object).getId();
         }
         return super.create(object);
     }
 
     @Override
     public void delete(Object object) {
         if (Request.class.isAssignableFrom(object.getClass())) {
             Request request = (Request)object;
             super.delete(request.getRequestData());
             try {
                 super.delete(request.getSpecificData());
             } catch (CvqException e) {
                 // no specific data, we were handling a raw Request
             }
             return;
         }
         super.delete(object);
     }
 
     @Override
     public void update(Object object) {
         if (Request.class.isAssignableFrom(object.getClass())) {
             Request request = (Request)object;
             super.update(request.getRequestData());
             try {
                 super.update(request.getSpecificData());
             } catch (CvqException e) {
                 // no specific data, we were handling a raw Request
             }
             return;
         }
         super.update(object);
     }
 
     private Request recompose(RequestData requestData) {
         Object specificData;
         try {
             specificData =
                 findById(requestData.getSpecificDataClass(), requestData.getSpecificDataId());
         } catch (CvqObjectNotFoundException e) {
             // handling a raw request
             return new Request(requestData);
         }
         String specificDataClassName = requestData.getSpecificDataClass().getName();
         String specificClassName =
             specificDataClassName.substring(0, specificDataClassName.length() - 4);
         try {
             return (Request)Class.forName(specificClassName)
                 .getConstructor(RequestData.class, requestData.getSpecificDataClass())
                     .newInstance(requestData, specificData);
         } catch (InstantiationException e) {
             // should not happen
             e.printStackTrace();
             return null;
         } catch (IllegalAccessException e) {
             // should not happen
             e.printStackTrace();
             return null;
         } catch (InvocationTargetException e) {
             // should not happen
             e.printStackTrace();
             return null;
         } catch (NoSuchMethodException e) {
             // should not happen
             e.printStackTrace();
             return null;
         } catch (ClassNotFoundException e) {
             // should not happen
             e.printStackTrace();
             return null;
         }
     }
 
     private List<Request> transform(List<RequestData> requestDatas, final boolean full) {
         List<Request> result = new ArrayList<Request>(requestDatas.size());
         for (RequestData requestData : requestDatas) {
             result.add(full ? recompose(requestData) : new Request(requestData));
         }
         return result;
     }
 
     public void empty(Request request)
         throws CvqException {
         for (RequestAction action : request.getActions()) {
             action.setFile(null);
             action.setMessage(null);
             action.setNote(null);
         }
         Iterator<RequestNote> it = request.getNotes().iterator();
         while (it.hasNext()) {
             delete(it.next());
             it.remove();
         }
         update(request);
         delete(request.getSpecificData());
         try {
             request.getClass()
                 .getMethod("setSpecificData", request.getRequestData().getSpecificDataClass())
                     .invoke(request, (Object)null);
         } catch (IllegalArgumentException e) {
             // should not happen
             e.printStackTrace();
         } catch (SecurityException e) {
             // should not happen
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             // should not happen
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             // should not happen
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             // should not happen
             e.printStackTrace();
         }
     }
 }
