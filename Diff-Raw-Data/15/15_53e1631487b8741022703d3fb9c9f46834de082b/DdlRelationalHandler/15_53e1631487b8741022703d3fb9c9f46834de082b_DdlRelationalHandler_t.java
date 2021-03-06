 /*
 // $Id$
 // Farrago is an extensible data management system.
 // Copyright (C) 2005-2007 The Eigenbase Project
 // Copyright (C) 2005-2007 Disruptive Tech
 // Copyright (C) 2005-2007 LucidEra, Inc.
 // Portions Copyright (C) 2004-2007 John V. Sichi
 //
 // This program is free software; you can redistribute it and/or modify it
 // under the terms of the GNU General Public License as published by the Free
 // Software Foundation; either version 2 of the License, or (at your option)
 // any later version approved by The Eigenbase Project.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package net.sf.farrago.ddl;
 
 import java.sql.*;
 import java.util.*;
 
 import net.sf.farrago.catalog.*;
 import net.sf.farrago.cwm.core.*;
 import net.sf.farrago.cwm.keysindexes.*;
 import net.sf.farrago.cwm.relational.*;
 import net.sf.farrago.fem.med.*;
 import net.sf.farrago.fem.sql2003.*;
 import net.sf.farrago.namespace.*;
 import net.sf.farrago.query.*;
 import net.sf.farrago.session.*;
 import net.sf.farrago.type.*;
 
 import org.eigenbase.rel.*;
 import org.eigenbase.relopt.*;
 import org.eigenbase.reltype.*;
 import org.eigenbase.sql.*;
 import org.eigenbase.util.*;
 
 
 /**
  * DdlRelationalHandler defines DDL handler methods for standard relational
  * objects such as schemas, tables, indexes, and views.
  *
  * @author John V. Sichi
  * @version $Id$
  */
 public class DdlRelationalHandler
     extends DdlHandler
 {
     //~ Instance fields --------------------------------------------------------
 
     protected final DdlMedHandler medHandler;
 
     //~ Constructors -----------------------------------------------------------
 
     public DdlRelationalHandler(DdlMedHandler medHandler)
     {
         super(medHandler.getValidator());
         this.medHandler = medHandler;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     // implement FarragoSessionDdlHandler
     public void validateDefinition(CwmCatalog catalog)
     {
         validator.validateUniqueNames(
             catalog,
             catalog.getOwnedElement(),
             true);
     }
 
     // implement FarragoSessionDdlHandler
     public void validateModification(CwmCatalog catalog)
     {
         validateDefinition(catalog);
     }
 
     // implement FarragoSessionDdlHandler
     public void validateDefinition(FemLocalSchema schema)
     {
         // NOTE jvs 7-Nov-2006:  CWM specifies table constraints as
         // owned by tables, but SQL:2003 specifies them as identified
         // directly by schemas.  So we have to flatten out the
         // schema namespace here.
 
         List<CwmModelElement> elements =
             new ArrayList<CwmModelElement>(schema.getOwnedElement());
 
         for (CwmModelElement element : schema.getOwnedElement()) {
             if (element instanceof CwmTable) {
                 elements.addAll(((CwmTable) element).getOwnedElement());
             }
         }
 
         validator.validateUniqueNames(
             schema,
             elements,
             true);
     }
 
     // implement FarragoSessionDdlHandler
     public void validateModification(FemLocalSchema schema)
     {
         validateDefinition(schema);
     }
 
     // implement FarragoSessionDdlHandler
     public void validateDefinition(FemLocalIndex index)
     {
         if (isReplacingType(index)) {
             throw res.ValidatorNotReplaceable.ex(
                 repos.getLocalizedObjectName(
                     null,
                     index.getName(),
                     index.refClass()));
         }
 
         FemLocalTable table = FarragoCatalogUtil.getIndexTable(index);
         if (table.isTemporary()) {
             if (!validator.isCreatedObject(table)) {
                 // REVIEW: support this?  What to do about instances of the
                 // same temporary table in other sessions?
                 throw res.ValidatorIndexOnExistingTempTable.ex(
                     repos.getLocalizedObjectName(index),
                     repos.getLocalizedObjectName(table));
             }
         }
         
         // check that columns are distinct
         List<CwmIndexedFeature> indexedFeatures = index.getIndexedFeature();
         boolean[] includesColumn = new boolean[table.getFeature().size()];
         for (CwmIndexedFeature column : indexedFeatures) {
             int ordinal = ((FemAbstractAttribute)column.getFeature()).getOrdinal();
             if (includesColumn[ordinal]) {
                 throw res.ValidatorIndexedColumnsNotDistinct.ex(
                     repos.getLocalizedObjectName(index));
             } else {
                 includesColumn[ordinal] = true;
             }
         }
         
         // TODO:  verify columns distinct, total width acceptable, and all
         // columns indexable types
         if (index.getNamespace() != null) {
             assert (index.getNamespace().equals(table.getNamespace()));
         } else {
             index.setNamespace(table.getNamespace());
         }
 
         index.setFilterCondition("TRUE");
     }
 
     // implement FarragoSessionDdlHandler
     public void validateModification(FemLocalIndex index)
     {
         // nothing to do here
     }
 
     // implement FarragoSessionDdlHandler
     public void validateModification(FemBaseColumnSet columnSet)
     {
         if (!columnSet.getServer().getWrapper().isForeign()) {
             validateBaseColumnSet(columnSet);
         }
     }
 
     // implement FarragoSessionDdlHandler
     public void validateDefinition(FemLocalTable table)
     {
         if (isReplacingType(table)) {
             throw res.ValidatorNotReplaceable.ex(
                 repos.getLocalizedObjectName(
                     null,
                     table.getName(),
                     table.refClass()));
         }
 
         boolean creation = true;
 
         if (((DdlValidator) validator).isReplace()) {
             // revalidation of this table is being triggered by
             // CREATE OR REPLACE of something else, probably
             // the local data server
             creation = false;
         }
 
         validateLocalTable(table, creation);
     }
 
     // implement FarragoSessionDdlHandler
     public void validateModification(FemLocalTable table)
     {
         validateLocalTable(table, false);
     }
 
     public void validateLocalTable(
         FemLocalTable table,
         boolean creation)
     {
         FemDataServer dataServer = table.getServer();
         FemDataWrapper dataWrapper = dataServer.getWrapper();
         if (dataWrapper.isForeign()) {
             throw res.ValidatorLocalTableButForeignWrapper.ex(
                 repos.getLocalizedObjectName(table),
                 repos.getLocalizedObjectName(dataWrapper));
         }
 
         validateAttributeSet(table);
 
         Collection indexes = FarragoCatalogUtil.getTableIndexes(repos, table);
 
         // NOTE:  don't need to validate index name uniqueness since indexes
         // live in same schema as table, so enforcement will take place at
         // schema level
         // Validate unique constraints
         FemLocalIndex generatedPrimaryKeyIndex = null;
         FemPrimaryKeyConstraint primaryKey = null;
         for (
             FemAbstractUniqueConstraint constraint
             : Util.filter(
                 table.getOwnedElement(),
                 FemAbstractUniqueConstraint.class))
         {
             if (constraint instanceof FemPrimaryKeyConstraint) {
                 if (primaryKey != null) {
                     throw res.ValidatorMultiplePrimaryKeys.ex(
                         repos.getLocalizedObjectName(table));
                 }
                 primaryKey = (FemPrimaryKeyConstraint) constraint;
             }
             if (creation) {
                 // Implement constraints via system-owned indexes.
                 FemLocalIndex index =
                     createUniqueConstraintIndex(table, constraint);
                 if (constraint == primaryKey) {
                     generatedPrimaryKeyIndex = index;
                 }
 
                 // Create redundant metadata used by JDBC views
                 createConstraintColumnMetadata(constraint);
             }
         }
 
         // NOTE:  do this after PRIMARY KEY uniqueness validation to get a
         // better error message in the case of generated constraint names
         validator.validateUniqueNames(
             table,
             table.getOwnedElement(),
             false);
 
         // Perform validation specific to the local data server
         FarragoMedDataServer medDataServer =
             validator.getDataWrapperCache().loadServerFromCatalog(dataServer);
         assert (medDataServer instanceof FarragoMedLocalDataServer) : medDataServer
             .getClass().getName();
         FarragoMedLocalDataServer medLocalDataServer =
             (FarragoMedLocalDataServer) medDataServer;
         try {
             medLocalDataServer.validateTableDefinition(
                 table,
                 generatedPrimaryKeyIndex,
                 creation);
         } catch (SQLException ex) {
             throw res.ValidatorDataServerTableInvalid.ex(
                 repos.getLocalizedObjectName(table),
                 ex);
         }
 
         if (creation) {
             medHandler.validateMedColumnSet(table);
         }
     }
 
     // implement FarragoSessionDdlHandler
     public void validateDefinition(FemLocalView view)
     {
         FarragoSession session = validator.newReentrantSession();
 
         try {
             validateViewImpl(session, view);
         } catch (FarragoUnvalidatedDependencyException ex) {
             // pass this one through
             throw ex;
         } catch (Throwable ex) {
             throw res.ValidatorInvalidObjectDefinition.ex(
                 repos.getLocalizedObjectName(view),
                 ex);
         } finally {
             validator.releaseReentrantSession(session);
         }
     }
 
     private void validateViewImpl(
         FarragoSession session,
         FemLocalView view)
         throws Throwable
     {
         String sql = view.getQueryExpression().getBody();
 
         tracer.fine(sql);
         FarragoSessionAnalyzedSql analyzedSql;
         try {
             analyzedSql =
                 session.analyzeSql(
                     sql,
                     validator.getTypeFactory(),
                     null,
                     false);
         } catch (Throwable ex) {
             throw adjustExceptionParserPosition(view, ex);
         }
 
         RelDataType rowType = analyzedSql.resultType;
 
         // We would prefer to use the original SQL, if it is sufficiently
         // similar. We require that (a) it gives no validation error, (b) it
         // produces the same result type, and (c) it is equivalent to
         // the same canonical SQL.
         String originalSql = view.getOriginalDefinition();
         boolean analyzeOriginalSql =
             session.getPersonality().shouldReplacePreserveOriginalSql();
         if (analyzeOriginalSql && (originalSql != null)) {
             FarragoSessionAnalyzedSql analyzedOriginalSql;
             try {
                 analyzedOriginalSql =
                     session.analyzeSql(
                         originalSql,
                         validator.getTypeFactory(),
                         null,
                         false);
                 if (analyzedOriginalSql.canonicalString.equals(
                     analyzedSql.canonicalString)
                     && analyzedOriginalSql.resultType.equals(
                     analyzedSql.resultType))
                 {
                     sql = originalSql;
                     analyzedSql = analyzedOriginalSql;
                 }
             } catch (RuntimeException ex) {
                 // validation errors indicate that we will just have to use the
                 // canonical SQL
                 Util.swallow(ex, null);
             }
         }
 
         List<CwmFeature> columnList = view.getFeature();
         boolean implicitColumnNames = true;
 
         if (columnList.size() != 0) {
             implicitColumnNames = false;
 
             // number of explicitly specified columns needs to match the number
             // of columns produced by the query
             if (rowType.getFieldList().size() != columnList.size()) {
                 throw res.ValidatorViewColumnCountMismatch.ex();
             }
             validator.validateViewColumnList(columnList);
         }
 
         if (analyzedSql.hasDynamicParams) {
             throw res.ValidatorInvalidViewDynamicParam.ex();
         }
 
         if (analyzedSql.hasTopLevelOrderBy) {
             throw res.ValidatorInvalidViewOrderBy.ex();
         }
 
         // Derive column information from result set metadata
         RelDataTypeField [] fields = rowType.getFields();
         for (int i = 0; i < fields.length; ++i) {
             FemViewColumn column;
             if (implicitColumnNames) {
                 column = repos.newFemViewColumn();
                 columnList.add(column);
             } else {
                 column = (FemViewColumn) columnList.get(i);
             }
             convertFieldToCwmColumn(fields[i], column, view);
             validateAttribute(column);
         }
 
         validator.validateUniqueNames(
             view,
             view.getFeature(),
             false);
 
         validator.fixupView(view, analyzedSql);
 
         view.setOriginalDefinition(sql);
         view.getQueryExpression().setBody(analyzedSql.canonicalString);
         analyzedSql.setModality(view);
 
         validator.createDependency(view, analyzedSql.dependencies);
     }
 
     public FemLocalIndex createUniqueConstraintIndex(
         FemLocalTable table,
         FemAbstractUniqueConstraint constraint)
     {
         // TODO:  make index SYSTEM-owned so that it can't be
         // dropped explicitly
         FemLocalIndex index = repos.newFemLocalIndex();
         FarragoCatalogUtil.generateConstraintIndexName(
             repos,
             constraint,
             index);
         index.setSpannedClass(table);
         index.setUnique(true);
         index.setSorted(true);
 
         int iOrdinal = 0;
         for (CwmStructuralFeature o : constraint.getFeature()) {
             CwmColumn column = (CwmColumn) o;
             FemLocalIndexColumn indexColumn = repos.newFemLocalIndexColumn();
             indexColumn.setName(column.getName());
             indexColumn.setAscending(Boolean.TRUE);
             indexColumn.setFeature(column);
             indexColumn.setIndex(index);
             indexColumn.setOrdinal(iOrdinal++);
         }
         return index;
     }
 
     private void createConstraintColumnMetadata(
         FemAbstractUniqueConstraint constraint)
     {
         int iOrdinal = 0;
         for (
             FemAbstractAttribute column
             : Util.cast(constraint.getFeature(), FemAbstractAttribute.class))
         {
             FemKeyComponent component = repos.newFemKeyComponent();
             component.setName(column.getName());
             component.setAttribute(column);
             constraint.getComponent().add(component);
             component.setOrdinal(iOrdinal++);
         }
     }
 
     // implement FarragoSessionDdlHandler
     public void validateDrop(FemLocalIndex index)
     {
         FemLocalTable table = FarragoCatalogUtil.getIndexTable(index);
         if (validator.isDeletedObject(table)) {
             // This index is being deleted together with its containing table,
             // which is always OK.
             return;
         }
 
         // The test for primary key should go before isClustered()
         // or tests in unitsql/ddl/misc.sql will fail
         // because primary key will be identified as clustered
         if (FarragoCatalogUtil.isIndexPrimaryKey(index)) {
             throw validator.newPositionalError(
                 index,
                res.ValidatorDropPrimaryKeyIndex.ex(
                     repos.getLocalizedObjectName(index)));
         }
 
         if (index.isClustered()) {
             throw validator.newPositionalError(
                 index,
                res.ValidatorDropClusteredIndex.ex(
                     repos.getLocalizedObjectName(index)));
         }
 
         if (FarragoCatalogUtil.isDeletionIndex(index)) {
             throw validator.newPositionalError(
                 index,
                res.ValidatorDropDeletionIndex.ex(
                     repos.getLocalizedObjectName(index)));
         }
 
         if (FarragoCatalogUtil.isIndexUnique(index)) {
             throw validator.newPositionalError(
                 index,
                res.ValidatorDropUniqueConstraintIndex.ex(
                     repos.getLocalizedObjectName(index)));
         }
 
         if (table.isTemporary()) {
             // REVIEW: support this?  What to do about instances of the
             // same temporary table in other sessions?
             throw res.ValidatorIndexOnExistingTempTable.ex(
                 repos.getLocalizedObjectName(index),
                 repos.getLocalizedObjectName(table));
         }
     }
 
     // implement FarragoSessionDdlHandler
     public void executeCreation(FemLocalIndex index)
     {
         if (FarragoCatalogUtil.isIndexTemporary(index)) {
             // definition of a temporary table should't create any real storage
             return;
         }
 
         validator.getIndexMap().createIndexStorage(
             validator.getDataWrapperCache(),
             index);
 
         FemLocalTable table = FarragoCatalogUtil.getIndexTable(index);
 
         if (!validator.isCreatedObject(table)) {
             indexExistingRows(table, index);
         }
     }
 
     private void indexExistingRows(
         FemLocalTable table,
         FemLocalIndex index)
     {
         FemDataServer dataServer = table.getServer();
         FarragoMedLocalDataServer medDataServer =
             (FarragoMedLocalDataServer) validator.getDataWrapperCache()
             .loadServerFromCatalog(dataServer);
 
         // NOTE jvs 30-Dec-2005:  We rely on the visibility of the
         // new object still being VK_PRIVATE so that the optimizer won't
         // see it and try to use it to satisfy source table scans!
 
         FarragoSession session = validator.newReentrantSession();
         try {
             ReentrantIndexBuilderStmt reentrantStmt =
                 new ReentrantIndexBuilderStmt(table, index, medDataServer);
             reentrantStmt.execute(session, false);
         } finally {
             validator.releaseReentrantSession(session);
         }
     }
 
     // implement FarragoSessionDdlHandler
     public void executeDrop(FemLocalIndex index)
     {
         // TODO: For a temporary table, need to drop storage for ALL sessions.
         // For now, storage from other sessions becomes garbage which will be
         // collected as those sessions close (or on recovery in case of a
         // crash).
         validator.getIndexMap().dropIndexStorage(
             validator.getDataWrapperCache(),
             index,
             false);
     }
 
     protected boolean isReplacingType(CwmModelElement obj)
     {
         return ((DdlValidator) medHandler.getValidator()).isReplacingType(obj);
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     private static class ReentrantIndexBuilderStmt
         extends FarragoReentrantStmt
     {
         private final FemLocalTable table;
         private final FemLocalIndex index;
         private final FarragoMedLocalDataServer medDataServer;
 
         ReentrantIndexBuilderStmt(
             FemLocalTable table,
             FemLocalIndex index,
             FarragoMedLocalDataServer medDataServer)
         {
             super(null);
             this.table = table;
             this.index = index;
             this.medDataServer = medDataServer;
         }
 
         protected void executeImpl()
         {
             RelOptTable relOptTable =
                 getPreparingStmt().loadColumnSet(
                     FarragoCatalogUtil.getQualifiedName(table));
             assert (relOptTable != null);
             RelNode indexBuildPlan =
                 medDataServer.constructIndexBuildPlan(
                     relOptTable,
                     index,
                     getPreparingStmt().getRelOptCluster());
             getStmtContext().prepare(
                 indexBuildPlan,
                 SqlKind.Insert,
                 true,
                 getPreparingStmt());
             getStmtContext().execute();
         }
     }
 }
 
 // End DdlRelationalHandler.java
