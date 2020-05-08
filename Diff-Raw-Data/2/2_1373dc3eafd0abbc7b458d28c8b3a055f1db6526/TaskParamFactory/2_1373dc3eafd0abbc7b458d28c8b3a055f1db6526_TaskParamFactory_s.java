 package de.escidoc.core.test;
 
 import de.escidoc.core.test.common.AssignParam;
 import de.escidoc.core.test.utils.DateTimeJaxbConverter;
 import org.joda.time.DateTime;
 import org.w3c.dom.Document;
 
 import java.util.*;
 
 /**
  * Factory to create TaskParam XML strings.<br/><br/>
  * Note: All create-methods should support the creation of invalid TaskParam XML strings, so that tests can perform negative tests.<br/><br/>
  * For example: If a method accepts a {@link List} as one parameter only, there are three possible behaviors:
  * <ol>
  *     <li>The list-parameter is <tt>null</tt>: A self-closing <tt>param</tt>-element will be returned.</li>
  *     <li>The list-parameter is empty: An empty <tt>param</tt>-element will be returned.</li>
  *     <li>The list-parameter has elements: The <tt>param</tt>-element has the corresponding content.</li>
  * </ol>
  *
  * @author Marko Voss (marko.voss@fiz-karlsruhe.de)
  */
 public class TaskParamFactory {
 
     private static final String NS_DC = "http://purl.org/dc/elements/1.1/";
 
     // NAMESPACES
     private static final String NS_TP_OPTIMISTIC_LOCKING =
         "http://www.escidoc.org/schemas/optimistic-locking-task-param/0.1";
 
     private static final String NS_TP_UPDATE_PASSWORD = "http://www.escidoc.org/schemas/update-password-task-param/0.1";
 
     private static final String NS_TP_REVOKE_GRANT = "http://www.escidoc.org/schemas/revoke-grant-task-param/0.1";
 
     private static final String NS_TP_REVOKE_GRANTS = "http://www.escidoc.org/schemas/revoke-grants-task-param/0.1";
 
     private static final String NS_TP_SELECTOR_ADD = "http://www.escidoc.de/schemas/addselectors/0.6";
 
     private static final String NS_TP_SELECTOR_REMOVE = "http://www.escidoc.de/schemas/removeselectors/0.6";
 
     private static final String NS_TP_MEMBERS = "http://www.escidoc.org/schemas/members-task-param/0.1";
 
     private static final String NS_TP_ID_SET = "http://www.escidoc.org/schemas/id-set-task-param/0.1";
 
     private static final String NS_TP_DELETE_OBJECTS = "http://www.escidoc.org/schemas/delete-objects-task-param/0.1";
 
     private static final String NS_TP_ASSIGN_PID = "http://www.escidoc.org/schemas/assign-pid-task-param/0.1";
 
     private static final String NS_TP_STATUS = "http://www.escidoc.org/schemas/status-task-param/0.1";
 
     private static final String NS_TP_RELATION = "http://www.escidoc.org/schemas/relation-task-param/0.1";
 
     private static final String NS_TP_SEMANTIC_STORE_QUERY = "http://www.escidoc.org/schemas/semantic-store-query/0.4";
 
     // SELECTOR TYPES
     public static final String SELECTOR_TYPE_USER_ATTR = "user-attribute";
 
     public static final String SELECTOR_TYPE_INTERNAL = "internal";
 
     public static final String SELECTOR_TYPE_EXTERNAL = "external";
 
     // RELATION ONTOLOGY
     public static final String ONTOLOGY_IS_PART_OF =
         "http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isPartOf";
 
     public static final String ONTOLOGY_IS_REVISION_OF =
         "http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf";
 
     private TaskParamFactory() {
     }
 
     /**
      * Return a pre-initialized StringBuilder instance.
      *
      * @return
      */
     private static StringBuilder getStringBuilder() {
         return new StringBuilder(de.escidoc.core.test.Constants.XML_HEADER);
     }
 
     /**
      * Returns an opened TaskParam start element with the specified namespace.<br/><br/>
      * <u>Example:</u><br/><br/>
      * <tt>&quot;&lt;param xmlns="foo"&quot;</tt>
      *
      * @param namespace
      * @return
      */
     private static StringBuilder getOpenedTaskParamElement(String namespace) {
         StringBuilder xml = getStringBuilder();
         xml.append("<param xmlns=\"");
         xml.append(namespace);
         return xml.append("\"");
     }
 
     /**
      * Returns an opened TaskParam start element with the specified namespace and last-modification-date if not null.<br/><br/>
      * <u>Example:</u><br/><br/>
      * <tt>&quot;&lt;param xmlns="foo" last-modification-date="bar"&quot;</tt>
      *
      * @param namespace
      * @param timestamp
      * @return
      */
     private static StringBuilder getOpenedTaskParamElement(String namespace, String timestamp) {
         StringBuilder xml = getOpenedTaskParamElement(namespace);
         if (timestamp != null) {
             xml.append(" last-modification-date=\"");
             xml.append(timestamp);
             xml.append("\"");
         }
         return xml;
     }
 
     /**
      * Returns an opened TaskParam start element with the specified namespace and last-modification-date if not null.<br/><br/>
      * <u>Example:</u><br/><br/>
      * <tt>&quot;&lt;param xmlns="foo" last-modification-date="bar"&quot;</tt>
      *
      * @param namespace
      * @param timestamp
      * @return
      */
     private static StringBuilder getOpenedTaskParamElement(String namespace, DateTime timestamp) {
         return getOpenedTaskParamElement(namespace, DateTimeJaxbConverter.printDate(timestamp));
     }
 
     /**
      * Get task parameter for optimistic-locking method.
      *
      * @param timestamp
      *            last-modification-date of the resource
      * @return task param XML (optimistic-locking-task-param.xsd)
      */
     public static String getOptimisticLockingTaskParam(final DateTime timestamp) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_OPTIMISTIC_LOCKING, timestamp);
         return xml.append("/>").toString();
     }
 
     /**
      * Get task parameter for update password method.
      *
      * @param timestamp
      *            last-modification-date of the resource
      * @param password
      * @return task param XML (update-password-task-param.xsd)
      */
     public static String getUpdatePasswordTaskParam(final DateTime timestamp, final String password) {
         return getUpdatePasswordTaskParam(DateTimeJaxbConverter.printDate(timestamp), password);
     }
 
     /**
      * Get task parameter for update password method, ignoring the LMD.
      *
      * @param password
      * @return
      * @deprecated Use this method only to get an invalid XML.
      */
     @Deprecated
     public static String getUpdatePasswordTaskParam(final String password) {
         return getUpdatePasswordTaskParam((String) null, password);
     }
 
     /**
      * Get task parameter for update password method.<br/><br/>
      * Use {@link TaskParamFactory#getUpdatePasswordTaskParam(org.joda.time.DateTime, String)} if you want to perform
      * secure operations.
      *
      * @param timestamp
      * @param password
      * @see TaskParamFactory#getUpdatePasswordTaskParam(org.joda.time.DateTime, String)
      * @return
      */
     public static String getUpdatePasswordTaskParam(final String timestamp, final String password) {
 
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_UPDATE_PASSWORD, timestamp);
 
         if (password == null) {
            xml.append("/>");
         }
         else {
             xml.append(">\n");
             xml.append("<password>");
             xml.append(password);
             xml.append("</password>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task parameter to revoke grant.
      *
      * @param timestamp
      *            last-modification-date of the resource
      * @param revocationRemark
      *            revocation remark for param. Set null to exclude revocation-remark element from XML
      * @return task param XML (revoke-grant-task-param.xsd)
      */
     public static String getRevokeGrantTaskParam(final DateTime timestamp, final String revocationRemark) {
         return getRevokeGrantTaskParam(DateTimeJaxbConverter.printDate(timestamp), revocationRemark);
     }
 
     /**
      * Get task parameter to revoke grant.
      *
      * @param revocationRemark
      * @return
      */
     public static String getRevokeGrantTaskParam(final String revocationRemark) {
         return getRevokeGrantTaskParam((String) null, revocationRemark);
     }
 
     /**
      * Get the task-param for revoke grant operation.<br/><br/>
      * For save operations, use {@link TaskParamFactory#getRevokeGrantTaskParam(org.joda.time.DateTime, String)} instead.
      *
      * @param timestamp
      * @param revocationRemark
      * @see TaskParamFactory#getRevokeGrantTaskParam(org.joda.time.DateTime, String)
      * @return
      */
     public static String getRevokeGrantTaskParam(final String timestamp, final String revocationRemark) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_REVOKE_GRANT, timestamp);
 
         if (revocationRemark == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         xml.append("<revocation-remark>");
         xml.append(revocationRemark);
         xml.append("</revocation-remark>\n");
         xml.append("</param>");
 
         return xml.toString();
     }
 
     public static String getAddSelectorsTaskParam(final List<Selector> selectors, final String timestamp) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_SELECTOR_ADD, timestamp);
 
         if (selectors == null || selectors.isEmpty()) {
             xml.append("/>");
         }
         else {
             xml.append(">\n");
             for (Selector selector : selectors) {
                 xml.append("<selector");
                 if (selector.name != null) {
                     xml.append(" name").append("=\"");
                     xml.append(selector.name);
                     xml.append("\"");
                 }
                 if (selector.type != null) {
                     xml.append(" type").append("=\"");
                     xml.append(selector.type);
                     xml.append("\"");
                 }
                 if (selector.value != null) {
                     xml.append(">").append(selector.value).append("</selector>\n");
                 }
                 else {
                     xml.append("/>\n");
                 }
             }
             xml.append("</param>");
         }
         return xml.toString();
     }
 
     /**
      * Get the XML taskParam for the method removeSelectors.
      *
      * @param selectorIds The IDs to remove.
      * @param timestamp The timestamp of the resource.
      * @return Returns the created task param xml.
      */
     public static String getRemoveSelectorsTaskParam(final List<String> selectorIds, final String timestamp) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_SELECTOR_REMOVE, timestamp);
 
         if (selectorIds == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         for (String id : selectorIds) {
             xml.append("<id>").append(id).append("</id>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task param XML for add/remove members methods (see members-task-param.xsd)
      *
      * @param timestamp
      *            Last modification date
      * @param ids
      *            member id parameter
      * @return task param XML (assign-pid-task-param.xsd)
      */
     public static String getMembersTaskParam(final List<String> ids, final DateTime timestamp) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_MEMBERS, timestamp);
 
         if (ids == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         for (String id : ids) {
             xml.append("<id>").append(id).append("</id>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task param XML for add/remove members methods (see members-task-param.xsd)
      *
      * @param ids
      *            member id parameter
      * @return task param XML (assign-pid-task-param.xsd)
      */
     public static String getIdSetTaskParam(final Set<String> ids) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_ID_SET);
 
         if (ids == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         for (String id : ids) {
             xml.append("<id>" + id + "</id>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task param XML for add/remove members methods (see members-task-param.xsd)
      *
      * @param ids
      *            member id parameter
      * @param sync
      * @return task param XML (assign-pid-task-param.xsd)
      */
     public static String getDeleteObjectsTaskParam(final Set<String> ids, final Boolean sync) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_DELETE_OBJECTS);
 
         if (ids == null && sync == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         for (String id : ids) {
             xml.append("<id>").append(id).append("</id>\n");
         }
         if (sync != null) {
             xml.append("<sync>").append(sync.toString()).append("</sync>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task param XML for assing PID methods (see assing-pid-task-param.xsd)
      *
      * @param param
      *            Last modification date
      * @param timestamp
      *            Assign PID parameter
      * @return task param XML (assign-pid-task-param.xsd)
      */
     public static String getAssignPidTaskParam(final AssignParam param, final DateTime timestamp) {
         return getAssignPidTaskParam(param, DateTimeJaxbConverter.printDate(timestamp));
     }
 
     /**
      * Get task param XML for assing PID methods (see assing-pid-task-param.xsd)<br/><br/>
      *
      * Use {@link TaskParamFactory#getAssignPidTaskParam(de.escidoc.core.test.common.AssignParam, org.joda.time.DateTime)}
      * if you want to perform secured operations.
      *
      * @param param
      *            Last modification date
      * @param timestamp
      *            Assign PID parameter
      * @return task param XML (assign-pid-task-param.xsd)
      * @see TaskParamFactory#getAssignPidTaskParam(de.escidoc.core.test.common.AssignParam, org.joda.time.DateTime) 
      */
     public static String getAssignPidTaskParam(final AssignParam param, final String timestamp) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_ASSIGN_PID, timestamp);
 
         if (param == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         if (param.getUrl() != null) {
             xml.append("<url>").append(param.getUrl()).append("</url>\n");
         }
         if (param.getPid() != null) {
             xml.append("<pid>").append(param.getPid()).append("</pid>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task parameter to revoke grants.
      *
      * @param ids
      *            ids for filter. Set <tt>null</tt> to exclude filter, use an empty Set to get a self-closing filter element.
      * @param revocationRemark
      *            revocation remark for param. Set null to exclude revocation-remark element from XML
      * @return task param XML (revoke-grant-task-param.xsd)
      */
     public static String getRevokeGrantsTaskParam(final Set<String> ids, final String revocationRemark) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_REVOKE_GRANTS);
 
         if (ids == null && revocationRemark == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
 
         if (ids != null) {
             if (!ids.isEmpty()) {
                 xml.append("<filter name=\"").append(NS_DC).append("identifier\">");
                 for (final String id : ids) {
                     xml.append("<id>");
                     xml.append(id);
                     xml.append("</id>\n");
                 }
                 xml.append("</filter>\n");
             }
             else {
                 xml.append("<filter name=\"").append(NS_DC).append("identifier\"/>");
             }
         }
 
         if (revocationRemark != null) {
             xml.append("<revocation-remark>");
             xml.append(revocationRemark);
             xml.append("</revocation-remark>\n");
         }
 
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task parameter for status methods.
      *
      * @param comment
      * @return
      * @deprecated Use this method only to get an invalid XML.
      */
     @Deprecated
     public static String getStatusTaskParam(final String comment) {
         return getStatusTaskParam((String) null, comment);
     }
 
     /**
      * Get task parameter for status methods.
      *
      * @param timestamp
      *            Last modification date
      * @param comment
      *            The comment.
      * @return task param XML (status-task-param.xsd)
      */
     public static String getStatusTaskParam(final DateTime timestamp, final String comment) {
         return getStatusTaskParam(DateTimeJaxbConverter.printDate(timestamp), comment);
     }
 
     /**
      * Get task parameter for status methods.
      * 
      * @param timestamp
      * @param comment
      * @return
      */
     public static String getStatusTaskParam(final String timestamp, final String comment) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_STATUS, timestamp);
 
         if (comment == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">");
         if (comment != null) {
             xml.append("<comment>");
             xml.append(comment);
             xml.append("</comment>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * Get task parameter for relation methods.
      *
      * @param relations A {@link List} of {@link Relation}s
      * @param timestamp The last modification date
      * @return task param XML (relation-task-param.xsd)
      */
     public static String getRelationTaskParam(final List<Relation> relations, final DateTime timestamp) {
         return getRelationTaskParam(relations, DateTimeJaxbConverter.printDate(timestamp), null);
     }
 
     /**
      * Get task parameter for relation methods.<br/><br/>
      *
      * Use {@link TaskParamFactory#getRelationTaskParam(java.util.List, org.joda.time.DateTime)} if you want
      * to perform secured operations.
      *
      * @param relations A {@link List} of {@link Relation}s
      * @param timestamp The last modification date
      * @see TaskParamFactory#getRelationTaskParam(java.util.List, org.joda.time.DateTime)
      * @return
      */
     public static String getRelationTaskParam(final List<Relation> relations, final String timestamp) {
         return getRelationTaskParam(relations, timestamp, null);
     }
 
     /**
      * Get task parameter for relation methods.
      *
      * @param relations A {@link List} of {@link Relation}s
      * @param timestamp The last modification date
      * @param defaultPredicate The default predicate to use if the predicate in a {@link Relation} is <tt>null</tt>
      *      or pass <tt>null</tt> to avoid using a default predicate.<br/><br/>
      *      <b>TODO: This default predicate exists only to adopt the old behavior. Fix the usage of this method and remove
      *      this default predicate here.</b>
      * @deprecated Use {@link TaskParamFactory#getRelationTaskParam(java.util.List, org.joda.time.DateTime)} instead.
      * @return task param XML (relation-task-param.xsd)
      */
     @Deprecated
     public static String getRelationTaskParam(
         final List<Relation> relations, final DateTime timestamp, final String defaultPredicate) {
         return getRelationTaskParam(relations, DateTimeJaxbConverter.printDate(timestamp), defaultPredicate);
     }
 
     /**
      * Get task parameter for relation methods.<br/><br/>
      *
      * Use {@link TaskParamFactory#getRelationTaskParam(java.util.List, org.joda.time.DateTime, String)} if you want
      * to perform secured operations.
      *
      * @param relations A {@link List} of {@link Relation}s
      * @param timestamp The last modification date
      * @param defaultPredicate The default predicate to use if the predicate in a {@link Relation} is <tt>null</tt>
      *      or pass <tt>null</tt> to avoid using a default predicate.<br/><br/>
      *      <b>TODO: This default predicate exists only to adopt the old behavior. Fix the usage of this method and remove
      *      this default predicate here.</b>
      * @see TaskParamFactory#getRelationTaskParam(java.util.List, org.joda.time.DateTime, String)
      * @deprecated Use {@link TaskParamFactory#getRelationTaskParam(java.util.List, String)} instead.
      * @return
      */
     @Deprecated
     public static String getRelationTaskParam(
         final List<Relation> relations, final String timestamp, final String defaultPredicate) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_RELATION, timestamp);
 
         if (relations == null) {
             return xml.append("/>").toString();
         }
         xml.append(">\n");
         for (Relation relation : relations) {
             xml.append("<relation>\n");
             if (relation.targetId != null) {
                 xml.append("<targetId>");
                 xml.append(relation.targetId);
                 xml.append("</targetId>\n");
             }
             if (relation.predicate != null) {
                 xml.append("<predicate>");
                 xml.append(relation.predicate);
                 xml.append("</predicate>\n");
             }
             else if (defaultPredicate != null) {
                 xml.append("<predicate>");
                 xml.append(defaultPredicate);
                 xml.append("</predicate>\n");
             }
             xml.append("</relation>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * 
      * @param query
      * @param format
      * @return
      */
     public static String getSemanticStoreQueryTaskParam(final String query, final String format) {
         StringBuilder xml = getOpenedTaskParamElement(NS_TP_SEMANTIC_STORE_QUERY);
 
         if (query == null && format == null) {
             return xml.append("/>").toString();
         }
 
         xml.append(">\n");
         if (query != null) {
             xml.append("<query>").append(query).append("</query>\n");
         }
         if (format != null) {
             xml.append("<format>").append(format).append("</format>\n");
         }
         return xml.append("</param>").toString();
     }
 
     /**
      * @see TaskParamFactory#getAddSelectorsTaskParam(java.util.List, String) 
      */
     public static class Selector {
 
         private final String name;
 
         private final String type;
 
         private final String value;
 
         public Selector(final String name, final String type, final String value) {
             this.name = name;
             this.type = type;
             this.value = value;
         }
     }
 
     /**
      * @see TaskParamFactory#getRelationTaskParam(java.util.List, org.joda.time.DateTime, String)
      * @see TaskParamFactory#getRelationTaskParam(java.util.List, String, String)  
      */
     public static class Relation {
 
         private final String targetId;
 
         private final String predicate;
 
         public Relation(final String targetId, final String predicate) {
             this.targetId = targetId;
             this.predicate = predicate;
         }
     }
 }
