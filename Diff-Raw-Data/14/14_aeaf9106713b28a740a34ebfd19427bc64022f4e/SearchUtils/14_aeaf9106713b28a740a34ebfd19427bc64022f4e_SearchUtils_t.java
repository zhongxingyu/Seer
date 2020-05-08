 package com.flexive.core.search;
 
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.storage.DBStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.ACLPermission;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxFlatStorageMapping;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.workflow.Step;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.*;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 /**
  * Utility methods for FxSQL and CMIS-SQL query engines.
  *
  * @author Daniel Lichtenberger, UCS
  * @version $Rev$
  * @since 3.1
  */
 public class SearchUtils {
     private SearchUtils() {
     }
 
     public static String getFlatStorageAssignmentFilter(FxEnvironment environment, String flatTableAlias, FxPropertyAssignment assignment) {
         final List<String> conditions = newArrayList();
         conditions.add(flatStorageFilterCondition(flatTableAlias, assignment));
         // also check derived assignments
         for (FxPropertyAssignment derived : assignment.getDerivedAssignments(environment)) {
             if (derived.isFlatStorageEntry()) {
                 conditions.add(flatStorageFilterCondition(flatTableAlias, derived));
             }
         }
         return "(" + StringUtils.join(conditions, " OR ") + ")";
     }
 
     /**
      * Return the security filter for instance-, type-, and workflow-based permissions.
      *
      * @param tableAlias        the table alias where the content columns are present
      * @param referencedTypes   all types that may occur in the result table
      * @param contentTableAvailable whether content table columns are available in tableAlias.
      * The static security filter needs at least these FX_CONTENT columns selected:
      * <ul>
      * <li>mandator</li>
      * <li>tdef</li>
      * <li>created_by</li>
      * <li>acl</li>
      * </ul>
      * If these columns are not present and this flag is set to false, the more generic
      * (and slower) stored procedure mayReadInstance2 is used.
      * @return  the security filter for instance-, type-, and workflow-based permissions.
      */
     public static String getSecurityFilter(String tableAlias, List<FxType> referencedTypes, boolean contentTableAvailable) {
         final UserTicket ticket = FxContext.getUserTicket();
 
         if (ticket.isGlobalSupervisor()) {
             return "1=1";
         }
 
         if (!contentTableAvailable) {
             // not selected from content table, main table properties for optimized security filter are not available
             final DBStorage storage = StorageManager.getStorageImpl();
             return "mayReadInstance2(" + tableAlias + ".id," + tableAlias + ".ver," + ticket.getUserId() + ","
                     + ticket.getMandatorId() + "," + storage.getBooleanExpression(ticket.isMandatorSupervisor()) +
                     "," + storage.getBooleanExpression(ticket.isGlobalSupervisor()) + ")="+storage.getBooleanTrueExpression();
         }
 
         // create security filters per type - the conditions will be joined with "OR".
         // property permissions are not handled by this filter, this is the responsibility of the
         // methods that return data from selected properties
         final List<String> conditions = new ArrayList<String>();
         if (ticket.isMandatorSupervisor()) {
             conditions.add(tableAlias + ".mandator=" + ticket.getMandatorId());
         }
 
         // group all types according to their security settings 
         final Set<Long> typesWithoutPerms = Sets.newHashSet();      // disabled security (always readable)
         final Set<Long> typesWithPerms = Sets.newHashSet();         // any type-based security (instance, step, type)
         final Set<Long> typesWithInstancePerms = Sets.newHashSet(); // types with instance permissions
         final Set<Long> typesWithStepPerms = Sets.newHashSet();     // types with workflow step permissions
         final Set<Long> typesOnlyPrivate = Sets.newHashSet();       // only readable via private ACL (user must be owner)
 
         for (FxType type : referencedTypes) {
             final Long typeId = type.getId();
             if (type.isUsePermissions()) {
                 boolean typeBasedPerms = false;
                 if (type.isUseTypePermissions() && !ticket.mayReadACL(type.getACL().getId(), -1)) {
                     if (ticket.mayReadACL(type.getACL().getId(), ticket.getUserId())) {
                         // type ACL can be read when the calling user is the owner
                         typesOnlyPrivate.add(typeId);
                         typeBasedPerms = true;
                     } else {
                         // type ACL cannot be read at all
                         continue;
                     }
                 }
                 if (type.isUseInstancePermissions()) {
                     typesWithInstancePerms.add(typeId);
                     typeBasedPerms = true;
                 }
                 if (type.isUseStepPermissions()) {
                     typesWithStepPerms.add(typeId);
                     typeBasedPerms = true;
                 }
                 if (typeBasedPerms) {
                     // at least on content type-based permission is set
                     typesWithPerms.add(typeId);
                 } else {
                     // only property permissions are set
                     typesWithoutPerms.add(typeId);
                 }
             } else {
                 typesWithoutPerms.add(typeId);
             }
         }
 
         // include all types without types
         if (!typesWithoutPerms.isEmpty()) {
             conditions.add(idFilter(tableAlias + ".tdef", typesWithoutPerms));
         }
 
         // build security filters. Since we're aggregating the ACL and step selectors over all types,
         // we need to exclude the types that don't have a specific permission type set (e.g. step perms)
         // and join the resulting conditions with "AND"
 
         final List<String> securityFilters = Lists.newArrayList();
         // include readable instance ACLs
         if (!typesWithInstancePerms.isEmpty()) {
             // collect all readable instance ACLs
             final Set<Long> readable = Sets.newHashSet(
                     Arrays.asList(ticket.getACLsId(-1, ACLCategory.INSTANCE, ACLPermission.READ))
             );
             // collect all ACLs that can be read if the calling user is the owner
             final Set<Long> privateReadable = Sets.newHashSet(
                     Arrays.asList(ticket.getACLsId(ticket.getUserId(), ACLCategory.INSTANCE, ACLPermission.READ))
             );
             // remove all ACLs that are readable regardless of the owner
             privateReadable.removeAll(readable);
 
             final String permFilter = buildPermFilter(tableAlias, typesWithInstancePerms, typesWithPerms,
                     contentFilterWithPrivate(ticket, tableAlias,
                             contentAclFilter(tableAlias, readable),
                             contentAclFilter(tableAlias, privateReadable)
                     )
             );
             securityFilters.add(permFilter);
         }
         
         // include step permissions
         if (!typesWithStepPerms.isEmpty()) {
 
             // collect all readable workflow steps
             final Set<Long> readable = Sets.newHashSet();
             final Set<Long> privateReadable = Sets.newHashSet();
 
             final FxEnvironment env = CacheAdmin.getEnvironment();
             for (Long typeId : typesWithStepPerms) {
                 for (Step step : env.getType(typeId).getWorkflow().getSteps()) {
                     if (ticket.mayReadACL(step.getAclId(), -1)) {
                         // readable ACL
                         readable.add(step.getId());
                     } else if (ticket.mayReadACL(step.getAclId(), ticket.getUserId())) {
                         // ACL readable only when the calling user is the owner
                         privateReadable.add(step.getAclId());
                     }
                 }
             }
 
             final String permFilter = buildPermFilter(tableAlias, typesWithStepPerms, typesWithPerms,
                     contentFilterWithPrivate(ticket, tableAlias, readable, privateReadable, "step")
             );
 
             securityFilters.add(permFilter);
         }
         
         // include type ACLs that are only readable if the current user is the owner
         if (!typesOnlyPrivate.isEmpty()) {
             final String permFilter = buildPermFilter(tableAlias, typesOnlyPrivate, typesWithPerms,
                     tableAlias + ".created_by=" + ticket.getUserId()
             );
             securityFilters.add(permFilter);
         }
         
         if (!securityFilters.isEmpty()) {
             conditions.add("(" + StringUtils.join(securityFilters, " AND ") + ")");
         }
 
         return conditions.isEmpty() ? "1=0" : "(" + StringUtils.join(conditions, " OR ") + ")";
     }
 
     private static String buildPermFilter(String tableAlias, Set<Long> selectedTypes, Set<Long> typesWithPerms, String filterCond) {
         final StringBuilder permFilter = new StringBuilder();
         permFilter.append("((").append(idFilter(tableAlias + ".tdef", selectedTypes));
         permFilter.append(" AND ").append(filterCond).append(')');
         addOtherTypes(permFilter, tableAlias, selectedTypes, typesWithPerms);
         permFilter.append(')');
         return permFilter.toString();
     }
 
     private static void addOtherTypes(StringBuilder out, String tableAlias, Set<Long> typesAlreadyIncluded, Set<Long> typesWithPerms) {
         final Set<Long> otherTypes = Sets.difference(typesWithPerms, typesAlreadyIncluded);
         if (!otherTypes.isEmpty()) {
             out.append(" OR ").append(idFilter(tableAlias + ".tdef", otherTypes));
         }
     }
 
     private static String idFilter(String column, Collection<Long> ids) {
         return _idFilter(column, ids, false);
     }
 
     private static String _idFilter(String column, Collection<Long> ids, boolean invert) {
         if (ids == null || ids.isEmpty()) {
             throw new IllegalArgumentException("Cannot build an empty ID filter");
         }
         if (ids.size() == 1) {
             return column + (invert ? "<>" : "=") + ids.iterator().next();
         } else {
             return column + " " + (invert ? "NOT " : "") + "IN (" + StringUtils.join(ids, ',') + ")";
         }
     }
 
     private static String contentAclFilter(String contentTableAlias, Set<Long> acls) {
         return acls.isEmpty() ? null :
                 // first check for contents that have the desired ACL in the main table column
                 "(" + contentTableAlias + ".acl IN (" + StringUtils.join(acls, ',') + ") " +
                 // then check for the ACL in TBL_CONTENT_ACLS
                 "OR EXISTS(" +
                 contentAclFilter(contentTableAlias, DatabaseConst.TBL_CONTENT_ACLS, acls)
                 + "))";
     }
 
     private static String contentAclFilter(String contentTableAlias, String table, Set<Long> acls) {
         return "SELECT c.acl FROM " + table + " c WHERE c.id=" + contentTableAlias + ".id AND c.ver=" + contentTableAlias + ".ver " +
                 " AND c.acl IN (" + StringUtils.join(acls, ',') + ")";
     }
 
     private static String contentFilterWithPrivate(UserTicket ticket, String tableAlias, Set<Long> readableIds, Set<Long> privateReadableIds, String column) {
         return contentFilterWithPrivate(
                 ticket,
                 tableAlias,
                readableIds.isEmpty()
                        ? null
                        : tableAlias + "." + column + " IN (" + StringUtils.join(readableIds, ',') + ")",
                 privateReadableIds.isEmpty() ? null :
                         tableAlias + "." + column + " IN (" + StringUtils.join(privateReadableIds, ',') + ")"
         );
     }
 
     private static String contentFilterWithPrivate(UserTicket ticket, String contentTableAlias, String readableFilter, String privateReadableFilter) {
         final String result;
         if (isBlank(privateReadableFilter)) {
            if (isBlank(readableFilter)) {
                // no readable content instances
                result = "1=0";
            } else {
                // no private permissions have to be checked
                result = readableFilter;
            }
         } else {
             // must match readable column OR owner and private readable column
             result = "(" + readableFilter + " OR ("
                     + contentTableAlias + ".created_by=" + ticket.getUserId() + " AND "
                     + privateReadableFilter
                     + "))";
         }
         return result;
     }
 
     private static String flatStorageFilterCondition(String flatTableAlias, FxPropertyAssignment assignment) {
         final String alias = StringUtils.isNotBlank(flatTableAlias) ? flatTableAlias + "." : "";
         final FxFlatStorageMapping mapping = assignment.getFlatStorageMapping();
         return "(" + alias + "typeid=" + assignment.getAssignedType().getId()
                 + " AND " + alias + "lvl=" + mapping.getLevel()
                 + (assignment.isMultiLang() ? "" : " AND " + alias + "lang=" + FxLanguage.SYSTEM_ID)
                 + (mapping.isGroupStorageMode()
                     ? " AND group_assid=" + mapping.getGroupAssignmentId()
                     : "")
                 + ")";
     }
 }
