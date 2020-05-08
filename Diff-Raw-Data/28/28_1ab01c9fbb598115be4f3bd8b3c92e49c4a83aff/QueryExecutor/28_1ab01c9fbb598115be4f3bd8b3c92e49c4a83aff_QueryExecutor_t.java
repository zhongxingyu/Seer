 package de.tuberlin.dima.presslufthammer.qexec;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReader;
 import de.tuberlin.dima.presslufthammer.data.columnar.Tablet;
 import de.tuberlin.dima.presslufthammer.data.columnar.inmemory.InMemoryWriteonlyTablet;
 import de.tuberlin.dima.presslufthammer.query.Query;
 import de.tuberlin.dima.presslufthammer.query.SelectClause;
 import de.tuberlin.dima.presslufthammer.query.WhereClause;
import de.tuberlin.dima.presslufthammer.query.WhereClause.Op;
 
 /**
  * The executor is responsible for executing a query given a source tablet. The
  * result is again a tablet.
  * 
  * @author Aljoscha Krettek
  * 
  */
 public class QueryExecutor {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private Tablet sourceTablet;
     private Query query;
 
     public QueryExecutor(Tablet sourceTablet, Query query) {
         this.sourceTablet = sourceTablet;
         this.query = query;
     }
 
     public InMemoryWriteonlyTablet performQuery() throws IOException {
         Set<String> projectedFields = Sets.newHashSet();
         for (SelectClause selectClause : query.getSelectClauses()) {
             projectedFields.add(selectClause.getColumn());
         }
         SchemaNode projectedSchema = null;
         if (projectedFields.contains("*")) {
             log.debug("Query is a 'select * ...' query.");
             projectedSchema = sourceTablet.getSchema();
         } else {
             projectedSchema = sourceTablet.getSchema().project(projectedFields);
         }
 
         List<SchemaNode> selectedFields = flattenSelectedFields(projectedSchema);
 
         Slice slice = new Slice(sourceTablet, selectedFields);
 
         InMemoryWriteonlyTablet resultTablet = new InMemoryWriteonlyTablet(
                 projectedSchema);
 
         performSelectProjectAggregate(slice, selectedFields, resultTablet);
 
         return resultTablet;
     }
 
     private void performSelectProjectAggregate(Slice slice,
             List<SchemaNode> selectedFields, Tablet targetTablet)
             throws IOException {
         int selectLevel = 0;
 
         while (slice.hasNext()) {
             slice.fetch();
 
             if (evalWhereClause(slice, selectedFields)) {
                 for (SchemaNode field : selectedFields) {
                     ColumnReader column = slice.getColumn(field);
                     if (column.getCurrentRepetition() >= selectLevel) {
                         column.writeToColumn(targetTablet
                                 .getColumnWriter(field));
                     }
                 }
 
                 selectLevel = slice.getFetchLevel();
             } else {
                 selectLevel = Math.min(slice.getFetchLevel(), selectLevel);
             }
         }
     }
 
    private boolean evalWhereClause(Slice slice, List<SchemaNode> selectedFields)
            throws IOException {
         if (query.getWhereClauses().size() <= 0) {
             return true;
         }
        for (SchemaNode schema : selectedFields) {
            if (slice.getColumn(schema).isNull()) {
                continue;
            }
            for (WhereClause clause : query.getWhereClauses()) {
                 if (schema.getQualifiedName().equals(clause.getColumn())) {
                    if (clause.getOp() == Op.EQ
                             && slice.getColumn(schema).getValue().toString()
                                     .equals(clause.getValue())) {
                         return true;
                    } else if (clause.getOp() == Op.NEQ
                            && !slice.getColumn(schema).getValue().toString()
                                    .equals(clause.getValue())) {
                        return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private List<SchemaNode> flattenSelectedFields(SchemaNode schema) {
         List<SchemaNode> result = Lists.newLinkedList();
         flattenSelectedFieldsRecursive(schema, result);
         return result;
     }
 
     private void flattenSelectedFieldsRecursive(SchemaNode schema,
             List<SchemaNode> result) {
         if (schema.isRecord()) {
             for (SchemaNode child : schema.getFieldList()) {
                 flattenSelectedFieldsRecursive(child, result);
             }
         } else {
             result.add(schema);
         }
     }
 }
