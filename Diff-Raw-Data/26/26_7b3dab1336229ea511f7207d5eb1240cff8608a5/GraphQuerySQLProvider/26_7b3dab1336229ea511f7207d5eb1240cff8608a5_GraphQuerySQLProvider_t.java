 package org.nsdl.mptstore.query.provider;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.nsdl.mptstore.core.TableManager;
 import org.nsdl.mptstore.query.QueryException;
 import org.nsdl.mptstore.query.component.GraphPattern;
 import org.nsdl.mptstore.query.component.GraphQuery;
 import org.nsdl.mptstore.query.component.MPTable;
 import org.nsdl.mptstore.query.component.MappableNodeFilter;
 import org.nsdl.mptstore.query.component.MappableNodePattern;
 import org.nsdl.mptstore.query.component.MappableTriplePattern;
 import org.nsdl.mptstore.query.component.NodeFilter;
 import org.nsdl.mptstore.query.component.QueryElement;
 import org.nsdl.mptstore.query.component.TriplePattern;
 import org.nsdl.mptstore.rdf.Node;
 import org.nsdl.mptstore.rdf.PredicateNode;
 
 /**
  * Translates a {@link GraphQuery} into a series of SQL statements.
  * <p>
  * Produces ANSI SQL-92 queries by converting each {@link GraphPattern} leaf
  * of the query tree into a series of JOINs.   Each join condition is formed
  * by matching variables between {@link TriplePattern}s in the appripriate
  * GraphPatterns.
  * </p>
  * <p>
  * TODO:
  * <ul>
  *  <li> Truly handle nested subqueries (currently is set to barf if a
  *       required or optional compomnent is not a GraphPattern).  Things are
  *       set up for doing so, just not finished yet</li>
  *  <li> Implement a strategy for dealing with unbound predicates </li>
  * </ul>
  * </p>
  *
  * @author birkland
  */
 public class GraphQuerySQLProvider implements SQLBuilder, SQLProvider {
 
     private static final Logger LOG =
             Logger.getLogger(GraphQuerySQLProvider.class.getName());
 
     private final GraphQuery query;
 
     private final MappingManager manager;
     private List<String> targets;
 
     private String ordering;
     private String orderingDirection;
 
     private HashMap<String, Set<String>> valueBindings =
             new HashMap<String, Set<String>>();
 
 
     /**
      * Create an instance.
      *
      * @param tableManager the table manager to use for looking up table names.
      * @param graphQuery the graph query.
      */
     public GraphQuerySQLProvider(final TableManager tableManager,
                                  final GraphQuery graphQuery) {
 
         this.manager = new MappingManager(tableManager);
         this.query = graphQuery;
     }
 
 
     /** Choose the variables that define result tuples
      * <p>
      * The given list of variables are used for determining
      * which bound values are included in result tuples, and in
      * what order.  If a variable is specified as a target, it
      * <em>must</em> be present somewhere in the query.  Any
      * unmatched target will result in error
      * </p>
      *
      * @param targetList the list of query variables.
      */
     public void setTargets(final List<String> targetList) {
         this.targets = new ArrayList<String>(targetList);
 
         this.ordering = null;
     }
 
     /**
      * Force an order on the results.
      * <p>
      * Given the name of a target variable, results will be ordered by its
      * bound value.  Results may be specified to return in ascending or
      * descending order.
      * </p>
      *
      * @param target Name of the target variable whose value will be the sort
      *        key.
      * @param desc True if results are to be in desceiding order, false
      *        otherwise.
      */
     public void orderBy(final String target, final boolean desc) {
         if (this.targets == null || !this.targets.contains(target)) {
             throw new IllegalArgumentException("Cannot group by variable '"
                     + target + "' since it is not in the target list "
                     + targets);
         }
 
         this.ordering = target;
         if (desc) {
             this.orderingDirection = "DESC";
         } else {
             this.orderingDirection = "ASC";
         }
     }
 
     /** Returns a query in ANSI SQL
      * <p>
      * Translates the GraphQuery defined in the constructor, along with
      * any specified orderings , into a set of SQL statements.  The
      * union of all SQL statement results, executed in order, will
      * represent the entire result set.
      * </p>
      *
      * @return list of SQL statements
      * @throws QueryException if there is some error translating the query
      *         to SQL.
      */
     public List<String> getSQL() throws QueryException {
 
         /* These are bindings of variables in the 'required' query portions */
         HashMap<String, String> requiredBindings =
                 new HashMap<String, String>();
 
         /* All variable bindings, including optional */
         HashMap<String, String> allBindings = new HashMap<String, String>();
 
         /* This is the main sequence of joins constituting the query */
         JoinSequence joinSeq = null;
 
         /* Process required elements first */
         for (QueryElement e : query.getRequired()) {
 
             /* Disallow subqueries for the time being */
             if (e.getType().equals(QueryElement.Type.GraphQuery)) {
                 throw new QueryException("Currently, we do not support "
                         + "subqueries");
             } else if (!e.getType().equals(QueryElement.Type.GraphPattern)) {
                 /* Currently, this will never happen */
                 throw new QueryException("Unknown query element type "
                         + e.getType());
             }
 
             /* All required elements of the query are inner joined */
             if (joinSeq == null) {
                 joinSeq = new JoinSequence(
                         parseGraphPattern((GraphPattern) e, requiredBindings));
             } else {
                 joinSeq.addJoin(JoinType.INNER_JOIN,
                         parseGraphPattern((GraphPattern) e, requiredBindings),
                         requiredBindings);
             }
         }
 
         allBindings.putAll(requiredBindings);
 
         for (QueryElement e : query.getOptional()) {
 
             HashMap<String, String> optionalBindings =
                     new HashMap<String, String>(requiredBindings);
 
             /* Disallow subqueries for the time being */
             if (e.getType().equals(QueryElement.Type.GraphQuery)) {
                 throw new QueryException("Currently, we do not support "
                         + "subqueries");
             } else if (!e.getType().equals(QueryElement.Type.GraphPattern)) {
                 /* Currently, this will never happen */
                 throw new QueryException("Unknown query element type "
                         + e.getType());
             }
 
             joinSeq.addJoin(JoinType.LEFT_OUTER_JOIN,
                     parseGraphPattern((GraphPattern) e, optionalBindings),
                     requiredBindings);
 
             addNewMappings(optionalBindings, allBindings);
 
         }
 
         StringBuilder sql = new StringBuilder();
 
         sql.append("SELECT " + generateTargets(allBindings)
                 + " FROM " + joinSeq);
 
         /*
          * If there are any values or constraints that remain to be added to
          * the query, add them in a WHERE clause.  NB: They better not be from
          * an optional clause: It would probably be wise to either check
          * here, or prove that an exception would have been thrown already.
          */

        StringBuilder additional = new StringBuilder();
         if (valueBindings.size() > 0) {
            additional.append(" WHERE ");
             ArrayList<String> valueKeys =
                     new ArrayList<String>(valueBindings.keySet());
             for (int i = 0; i < valueKeys.size(); i++) {
                 ArrayList<String> values = new ArrayList<String>(
                         valueBindings.get(valueKeys.get(i)));
                 for (int j = 0; j < values.size(); j++) {
                     if (i > 0 || j > 0) {
                         sql.append(" AND ");
                     }

                    LOG.debug("Adding remaining unused binding for "
                            + valueKeys.get(i) + ", " + values.get(j) + "\n");
                    additional.append(values.get(j));
                 }
             }
         }
 
        if (!additional.toString().equals(" WHERE ")) {
            sql.append(additional.toString());
        }

         if (ordering != null) {
             sql.append(" ORDER BY " + allBindings.get(ordering) + " "
                     + orderingDirection);
 
         }
 
         ArrayList<String> sqlList = new ArrayList<String>();
         sqlList.add(sql.toString());
         return sqlList;
     }
 
     /** {@inheritDoc} */
     public List<String> getTargets() {
         return new ArrayList<String>(targets);
     }
 
     private Joinable parseGraphPattern(
             final GraphPattern g,
             final HashMap<String, String> variableBindings)
             throws QueryException {
 
         /* First, organize the filters by variable so that we can map them */
         HashMap<String, Set<MappableNodeFilter>> filters =
                 new HashMap<String, Set<MappableNodeFilter>>();
 
         /* We're given NodeFilters, but need MappableNodeFilters. Convert. */
         Set<MappableNodeFilter<Node>> givenFilters =
                 new HashSet<MappableNodeFilter<Node>>();
         for (NodeFilter<Node> filter : g.getFilters()) {
             givenFilters.add(new MappableNodeFilter<Node>(filter));
         }
 
         /*
          * For each given filter, if the value or constraint is a variable,
          * place it in the filter pool.
          */
         for (MappableNodeFilter f : givenFilters) {
             if (f.getNode().isVariable()) {
                 if (!filters.containsKey(f.getNode().getVarName())) {
                     LOG.debug("Adding " + f.getNode().getVarName()
                             + " To filter pool..\n");
                     filters.put(f.getNode().getVarName(),
                             new HashSet<MappableNodeFilter>());
                 }
                 filters.get(f.getNode().getVarName()).add(f);
             }
             if (f.getConstraint().isVariable()) {
                if (!filters.containsKey(f.getConstraint().getVarName())) {
                     LOG.debug("Adding " + f.getConstraint().getVarName()
                             + " To filter pool..\n");
                     filters.put(f.getConstraint().getVarName(),
                             new HashSet<MappableNodeFilter>());
                 }
                filters.get(f.getConstraint().getVarName()).add(f);
             }
 
             if (!(f.getNode().isVariable()
                     || f.getConstraint().isVariable())) {
                 throw new IllegalArgumentException("Triple filters must "
                         + "contain a variable.  Neither "
                         + f.getNode().getVarName() + " nor "
                         + f.getConstraint().getVarName() + " is a variable!");
             }
         }
 
 
         /* Next, process each triple pattern in this graph pattern */
         LinkedList<MappableTriplePattern> steps =
                 new LinkedList<MappableTriplePattern>();
         for (TriplePattern p : g.getTriplePatterns()) {
             steps.add(new MappableTriplePattern(p));
         }
 
         /*
          * We create an initial join sequence from the first triple pattern
          * NB: We require that all triple patterns in a graph pattern
          * can be joined, so it is OK to do this
          */
         MappableTriplePattern step = steps.removeFirst();
 
         bindPattern(step, variableBindings);
         JoinSequence joins = new JoinSequence(new JoinTable(step));
 
         /* We keep track of all tje variables that are available to join on */
         Set<MappableNodePattern> joinableVars = joins.joinVars();
 
         /* For all the remaining steps.. */
         while (!steps.isEmpty()) {
             step = getJoinablePattern(steps, variableBindings);
             if (step == null) {
                 throw new QueryException("Cannot bind all query steps! \n"
                         + "remaining:\n" + steps
                         + "\nvariables already bound:\n "
                         + variableBindings.keySet() + "\n");
             }
             steps.remove(step);
 
             bindPattern(step, variableBindings);
             JoinTable table = new JoinTable(step);
 
             joinableVars.addAll(table.joinVars());
             JoinConditions conditions = new JoinConditions();
 
             /* Create all possible joins */
             addJoinConditions(conditions, step, variableBindings);
 
             /* Add any filter constraints */
             addFilterConditions(conditions, filters, joinableVars,
                     variableBindings);
 
             /* Fold in any remaining constant bindings */
             for (MappableNodePattern var : joinableVars) {
                 if (valueBindings.containsKey(var.boundTable().alias())) {
 
                     for (String condition : valueBindings.get(
                             var.boundTable().alias())) {
                         LOG.debug("parseGraphPattern: Adding remaining "
                                 + "constant conditions " + condition + "\n");
                         conditions.addCondition(condition);
                     }
                     valueBindings.remove(var.boundTable().alias());
                 }
             }
 
             /* Finally, add the join to the sequence */
             joins.addJoin(JoinType.INNER_JOIN, table, conditions);
         }
 
         /*
          * If we still have filters yet unapplied, that is legitimate only if
          * this pattern has a length of 1 AND it contains a variable that
          * matches the filter...
          */
         if (filters.values().size() > 0 && g.getTriplePatterns().size() > 1) {
             throw new QueryException("Filter is unbound");
         }
 
         /* .. If so, then get that one pattern */
         MappableTriplePattern p = new MappableTriplePattern(
                 g.getTriplePatterns().get(0));
 
         /* ... and Process any remaining filters */
         for (String varName : filters.keySet()) {
             for (MappableNodeFilter f : filters.get(varName)) {
                 processFilter(p, varName, f);
             }
         }
         return joins;
     }
 
     private void addJoinConditions(
             final JoinConditions conditions,
             final MappableTriplePattern step,
             final HashMap<String, String> variableBindings) {
         for (MappableNodePattern p : step.getNodes()) {
             if (isBound(p, variableBindings)) {
                 /* Join this var's column w/the corresponding bound one */
                 if (!p.mappedName().equals(
                         getBoundValue(p, variableBindings))) {
                     LOG.debug("parseGraphPattern: Adding Join Condition "
                             + p.mappedName() +  " = "
                             + getBoundValue(p, variableBindings) + "\n");
                     conditions.addCondition(p.mappedName(), " = ",
                             getBoundValue(p, variableBindings));

                    if (valueBindings.containsKey(p.boundTable().alias())) {
                        LOG.debug("Removing value binding from queue: "
                                + p.mappedName() + " = "
                                + getBoundValue(p, variableBindings) + "\n");
                        valueBindings.get(p.boundTable().alias()).remove(
                                p.mappedName() + " = "
                                + getBoundValue(p, variableBindings));
                    }
                 }
             }
         }
     }
 
     private void addFilterConditions(
             final JoinConditions conditions,
             final HashMap<String, Set<MappableNodeFilter>> filters,
             final Set<MappableNodePattern> joinableVars,
             final HashMap<String, String> variableBindings) {
         for (String filterVar : filters.keySet()) {
             for (MappableNodePattern joinableVar : joinableVars) {
                 if (joinableVar.isVariable()
                         && joinableVar.getVarName().equals(filterVar)) {
                     for (MappableNodeFilter f : filters.get(filterVar)) {
                         String right;
                         String left;
 
                         if (f.getNode().isVariable() && f.getNode()
                                 .getVarName().equals(filterVar)) {
                             left = getBoundValue(joinableVar,
                                     variableBindings);
                         } else if (f.getNode().isVariable()) {
                             left = getBoundValue(f.getNode(),
                                     variableBindings);
                         } else {
                             left = "'" + f.getNode().getNode() + "'";
                         }
 
                         if (f.getConstraint().isVariable()
                                 && f.getConstraint().getVarName()
                                 .equals(filterVar)) {
                             right = getBoundValue(joinableVar,
                                     variableBindings);
                         } else if (f.getConstraint().isVariable()) {
                             right = getBoundValue(f.getConstraint(),
                                     variableBindings);
                         } else {
                             right = "'" + f.getConstraint().getNode() + "'";
                         }
 
                         conditions.addCondition(left, f.getOperator(),
                                 right);
                         LOG.debug("parseGraphPattern: Adding filter "
                                 + "condition: " + left + " "
                                 + f.getOperator() + " " + right + "\n");
                     }
 
                     removeFromMap(filters.get(filterVar), filters);
                 }
             }
         }
     }
 
     private void processFilter(final MappableTriplePattern p,
                                final String varName,
                                final MappableNodeFilter f)
             throws QueryException {
 
         String mappedName;
         if (p.getSubject().isVariable() && p.getSubject()
                 .getVarName().equals(varName)) {
             mappedName = p.getSubject().mappedName();
         } else if (p.getObject().isVariable() && p.getObject()
                 .getVarName().equals(varName)) {
             mappedName = p.getObject().mappedName();
         } else {
             throw new QueryException("Variable " + varName
                     + " in filter Cannot be found in graph query");
         }
 
         if (!valueBindings.containsKey(mappedName)) {
             valueBindings.put(mappedName, new HashSet<String>());
         }
 
         if (f.getNode().isVariable() && f.getNode().getVarName()
                 .equals(varName)) {
             if (f.getConstraint().isVariable()) {
                 /* XXX It's probably not legal to be here.. */
                 LOG.warn("Node filter constraint is variable?  "
                         + "It's probably not legal to be here...");
             } else {
                 valueBindings.get(mappedName).add(
                         mappedName + " " + f.getOperator() + " '"
                         + f.getConstraint().getNode() + "'");
                 LOG.debug("Remaining Filters: " + mappedName + " "
                         + f.getOperator() + " '"
                         + f.getConstraint().getNode() + "'" + "\n");
             }
         } else if (f.getConstraint().isVariable()
                 && f.getConstraint().getVarName().equals(varName)) {
             if (f.getNode().isVariable()) {
                 /* XXX: it's probably not legal to be here.. */
                 LOG.warn("Node filter constraint is variable and node is "
                         + "variable?  It's probably not legal to be here...");
             } else {
                 valueBindings.get(mappedName).add(
                         "'" + f.getNode().getNode() + "' "
                         + f.getOperator() + " " + mappedName);
                 LOG.debug("Remaining Filters: " + "'"
                         + f.getNode().getNode() + "' "
                         + f.getOperator() + " " + mappedName + "\n");
             }
         }
 
     }
 
     /*
      * From a list of mappable triple patterns, pick one that has a
      * variable that also occurs in variablebindings.  This assures that
      * if the caller is looking for joins, it will find at least one.
      */
     private MappableTriplePattern getJoinablePattern(
             final List<MappableTriplePattern> l,
             final HashMap<String, String> variableBindings) {
         for (MappableTriplePattern p : l) {
             if (isBound(p.getSubject(), variableBindings)
                     || isBound(p.getObject(), variableBindings)) {
                 return p;
             }
         }
         return null;
     }
 
     /*
      * Determine if a variable has been apped to a literal or
      * specific column of a table
      */
     private boolean isBound(final MappableNodePattern n,
                             final HashMap<String, String> variableBindings) {
         if (n.isVariable()) {
             return variableBindings.containsKey(n.getVarName());
         } else {
             return true;
         }
     }
 
     /*
      * Get either an explicit value or a column/table reference for a given
      * mapped node pattern
      */
     private String getBoundValue(
             final MappableNodePattern n,
             final HashMap<String, String> variableBindings) {
         if (n.isVariable()) {
             return variableBindings.get(n.getVarName());
         } else {
             return "'" + n.getNode().toString() + "'";
         }
     }
 
     /*
      * Bind the variables/values of a triple pattern by:
      * - Placing any new variables into the master bings map,
      * - Placing any literal values into the literals map
      */
     private void bindPattern(final MappableTriplePattern t,
                              final HashMap<String, String> variableBindings) {
         t.bindTo(manager.mapPredicateTable(t.getPredicate()));
 
         bindNode(t.getSubject(), variableBindings);
         //bindNode(t.predicate, variableBindings);
         bindNode(t.getObject(), variableBindings);
 
     }
 
     /* TODO: be able to bind a predicate node */
     private void bindNode(final MappableNodePattern p,
                           final HashMap<String, String>variableBindings) {
         if (p.isVariable()) {
             if (!variableBindings.containsKey(p.getVarName())) {
                 LOG.debug("Bound " + p.getVarName() + " to "
                         + p.mappedName() + "\n");
                 variableBindings.put(p.getVarName(), p.mappedName());
             }
         } else {
             if (!valueBindings.containsKey(p.boundTable().alias())) {
                 valueBindings.put(p.boundTable().alias(),
                         new HashSet<String>());
             }
             LOG.debug("bindNode: adding valueBinding " + p.mappedName()
                     + " = " + "'" + p.getNode() + "'\n");
             valueBindings.get(p.boundTable().alias()).add(p.mappedName()
                     + " = " + "'" + p.getNode() + "'");
         }
     }
 
 
     /** Removes a mapped value (and all associated keys) from a map.
      *
      * @param value mapped value to remove
      * @param m map to remove the value from
      */
     private <K, V> void removeFromMap(final V value, final Map<K, V> m) {
         for (Map.Entry<K, V> e : m.entrySet()) {
             if (e.getValue().equals(value)) {
                 m.remove(e.getKey());
             }
         }
     }
 
     private String generateTargets(
             final HashMap<String, String> variableBindings) {
         String selects = "";
         for (int i = 0; i < targets.size(); i++) {
             selects += variableBindings.get(targets.get(i));
             if (i < targets.size() - 1) {
                 selects += ", ";
             }
         }
         return selects;
     }
 
     private <K, V> void addNewMappings(final Map<K, V> from,
                                        final Map<K, V> to) {
         for (K key : from.keySet()) {
             if (!to.containsKey(key)) {
                 to.put(key, from.get(key));
             }
         }
     }
 
     private interface Joinable {
         Set<MappableNodePattern> joinVars();
         String alias();
         String declaration();
     }
 
     private class JoinTable implements Joinable {
         private final MappableTriplePattern t;
         public JoinTable(final MappableTriplePattern tPattern) {
             this.t = tPattern;
         }
 
         public Set<MappableNodePattern> joinVars() {
             HashSet<MappableNodePattern> s =
                     new HashSet<MappableNodePattern>();
             if (t.getSubject().isVariable()) {
                 s.add(t.getSubject());
             }
 
             if (t.getObject().isVariable()) {
                 s.add(t.getObject());
             }
 
             return s;
         }
 
         public String alias() {
             return t.getSubject().boundTable().alias();
         }
 
         public String declaration() {
             String alias = t.getSubject().boundTable().alias();
             String name = t.getSubject().boundTable().name();
             if (name.equals(alias)) {
                 return name;
             } else {
                 return (name + " AS " + alias);
             }
         }
     }
 
     private class JoinSequence implements Joinable {
         private final StringBuilder join;
         private int joinCount = 0;
 
         private final List<Joinable> joined = new ArrayList<Joinable>();
         public JoinSequence(final Joinable start) {
             this.join = new StringBuilder(start.declaration());
             joined.add(start);
             joinCount = 1;
         }
 
         public void addJoin(final String joinType,
                             final Joinable j,
                             final String joinConstraints) {
             join.append(" " + joinType + " " + j.declaration());
             joined.add(j);
             if (joinConstraints != null && joinConstraints != "") {
                 join.append(" ON (" + joinConstraints + ")");
             }
             joinCount++;
         }
 
         public void addJoin(final String joinType,
                             final Joinable j,
                             final JoinConditions conditions) {
             addJoin(joinType, j, conditions.toString());
         }
 
         public void addJoin(final String joinType,
                             final Joinable j,
                             final HashMap<String, String> variableBindings) {
 
             JoinConditions conditions = new JoinConditions();
 
 
             for (MappableNodePattern existingVar : this.joinVars()) {
                 for (MappableNodePattern candidateVar : j.joinVars()) {
                     if (existingVar.getVarName().equals(
                             candidateVar.getVarName())
                             && variableBindings.get(
                             candidateVar.getVarName())
                             .equals(existingVar.mappedName())) {
                         conditions.addCondition(existingVar.mappedName(),
                                 "=", candidateVar.mappedName());
                     }
                 }
             }
 
             addJoin(joinType, j, conditions);
         }
 
         public Set<MappableNodePattern> joinVars() {
             HashSet<MappableNodePattern> joinVars =
                     new HashSet<MappableNodePattern>();
 
             for (Joinable joinable : joined) {
                 joinVars.addAll(joinable.joinVars());
             }
 
             return joinVars;
         }
 
         public String alias() {
             if (joinCount == 1) {
                 return join.toString();
             } else {
                 return "(" + join.toString() + ")";
             }
         }
 
         public String declaration() {
             return alias();
         }
 
         public String toString() {
             return join.toString();
         }
     }
 
     private class JoinConditions {
         private Set<String> conditions = new HashSet<String>();
 
         public void addCondition(final String leftOperand,
                                  final String operator,
                                  final String rightOperand) {
             addCondition(leftOperand.trim() + " " + operator.trim()
                     + " " + rightOperand.trim());
         }
 
         public void addCondition(final String condition) {
             conditions.add(condition.trim());
         }
 
         public String toString() {
             StringBuilder joinClause = new StringBuilder();
             for (String condition : conditions) {
                 if (joinClause.length() == 0) {
                     joinClause.append(condition);
                 } else {
                     joinClause.append(" AND " + condition);
                 }
 
             }
             return joinClause.toString();
         }
     }
     private class JoinType {
         public static final String LEFT_OUTER_JOIN = "LEFT OUTER JOIN";
         public static final String INNER_JOIN = "JOIN";
     }
 
     private class MappingManager {
         private HashMap<String, List<String>> predicateMap
                 = new HashMap<String, List<String>>();
         private TableManager adaptor;
         private int noMap = 0;
         private int allMap = 0;
 
         public MappingManager(final TableManager tableManager) {
             this.adaptor = tableManager;
         }
 
         public MPTable mapPredicateTable(
                 final MappableNodePattern<PredicateNode> predicate) {
             if (predicate.isVariable()) {
                 throw new IllegalArgumentException("predicate must not "
                         + "be a variable");
             }
 
             String tableName;
             String alias;
 
             if (predicate.isVariable()) {
                 tableName = allTableQuery();
                 alias = "ap_" + ++allMap;
             } else {
                 tableName = adaptor.getTableFor(predicate.getNode());
             }
 
             if (tableName == null) {
                 /* No predicate found.. create table that returns no results */
                 alias = "np_" + noMap++;
                 tableName = "(SELECT pkey AS s, pkey AS o from tmap where 1=0)";
             } else if (predicateMap
                     .containsKey(predicate.getNode().toString())) {
                 List<String> aliases = predicateMap.get(
                         predicate.getNode().toString());
                 alias = tableName + "_" + aliases.size();
                 aliases.add(alias);
             } else {
                 ArrayList<String> aliases = new ArrayList<String>();
                 aliases.add(tableName);
                 predicateMap.put(predicate.getNode().toString(), aliases);
                 alias = tableName;
             }
 
             MPTable table = new MPTable(tableName, alias);
             return table;
         }
 
         private String allTableQuery() {
             boolean first = true;
             StringBuilder allTable = new StringBuilder();
             allTable.append("( ");
             for (PredicateNode predicate : adaptor.getPredicates()) {
                 if (first) {
                     first = false;
                 } else {
                     allTable.append(" UNION ALL ");
                 }
 
                 allTable.append(" SELECT * from "
                         + adaptor.getTableFor(predicate));
             }
 
             allTable.append(")");
             return allTable.toString();
         }
     }
 }
