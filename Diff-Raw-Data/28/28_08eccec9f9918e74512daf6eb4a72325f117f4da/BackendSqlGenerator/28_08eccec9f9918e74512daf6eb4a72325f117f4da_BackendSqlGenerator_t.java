 package com.txtr.hibernatedelta.generator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.Connection;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Ordering;
 import com.txtr.hibernatedelta.DatabaseWithIndexes;
 import com.txtr.hibernatedelta.IndexIdFactory;
 import com.txtr.hibernatedelta.model.HibernateDatabase;
 import com.txtr.hibernatedelta.model.HibernateIndexName;
 import com.txtr.hibernatedelta.model.HibernateIndexNames;
 import liquibase.change.Change;
 import liquibase.changelog.ChangeSet;
 import liquibase.diff.DiffGeneratorFactory;
 import liquibase.diff.DiffResult;
 import liquibase.diff.compare.CompareControl;
 import liquibase.diff.output.DiffOutputControl;
 import liquibase.diff.output.changelog.DiffToChangeLog;
 import liquibase.snapshot.DatabaseSnapshot;
 import liquibase.sql.Sql;
 import liquibase.sqlgenerator.SqlGeneratorFactory;
 import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
 import liquibase.structure.core.ForeignKey;
 import liquibase.structure.core.Index;
 import liquibase.structure.core.PrimaryKey;
 import liquibase.structure.core.UniqueConstraint;
 
 
 public class BackendSqlGenerator {
 
     public static final List<String> CREATE_SQL_FILES = ImmutableList.of("quartz.sql");
 
     private static final JAXBContext MARSHALLER;
     private static final JAXBContext INDEX_MARSHALLER;
 
     static {
         try {
             MARSHALLER = JAXBContext.newInstance(HibernateDatabase.class);
             INDEX_MARSHALLER = JAXBContext.newInstance(HibernateIndexNames.class);
 
         } catch (JAXBException e) {
             throw Throwables.propagate(e);
         }
     }
 
     public static final String RENAME_INDEX = "ALTER INDEX %2$s RENAME TO %3$s;";
     public static final String RENAME_CONSTRAINT = "ALTER TABLE %1s RENAME CONSTRAINT %2$s TO %3$s;";
 
     public BackendSqlGenerator() throws IOException {
     }
 
     public static HibernateIndexNames getIndexNames(URL indexNames) {
         try {
             return (HibernateIndexNames) INDEX_MARSHALLER.createUnmarshaller().unmarshal(indexNames);
         } catch (JAXBException e) {
             throw Throwables.propagate(e);
         }
     }
 
 
     public String createUpdateAgainstCommittedSchema(URL schema, DatabaseWithIndexes newDatabase, URL indexNames) throws Exception {
         final DatabaseSnapshot reference = LiquibaseModelFactory.create(newDatabase.getDatabase());
         final DatabaseSnapshot target = createFromCommittedSchema(schema, indexNames);
 
         Collections.sort(newDatabase.getIndexNames().getIndexNames(), Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(new Function<HibernateIndexName, String>() {
             @Override
             public String apply(HibernateIndexName input) {
                 return input.getTableName() + "-" + StringUtils.join(input.getColumns(), ',');
             }
         }));
 
         final StringBuilder sb = new StringBuilder();
         printChanges(reference, target, sb);
         return sb.toString();
     }
 
     public void writeDataModelFiles(File mappings, File indexNames, DatabaseWithIndexes newDatabase) throws FileNotFoundException {
         try {
             getSchemaMarshaller().marshal(newDatabase.getDatabase(), new FileOutputStream(mappings));
             getIndexMarshaller().marshal(newDatabase.getIndexNames(), new FileOutputStream(indexNames));
         } catch (JAXBException e) {
             throw Throwables.propagate(e);
         }
     }
 
     public static Marshaller getIndexMarshaller() throws JAXBException {
         return getMarshaller(INDEX_MARSHALLER);
     }
 
     public static Marshaller getSchemaMarshaller() throws JAXBException {
         return getMarshaller(MARSHALLER);
     }
 
     private static Marshaller getMarshaller(JAXBContext context) throws JAXBException {
         Marshaller marshaller = context.createMarshaller();
         marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
         marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
         return marshaller;
     }
 
     public String createNew(HibernateDatabase newDatabase) throws Exception {
 
         final DatabaseSnapshot reference = LiquibaseModelFactory.create(newDatabase);
         final DatabaseSnapshot target = LiquibaseModelFactory.createEmptySnapshot();
         final StringBuilder sb = new StringBuilder();
         printChanges(reference, target, sb);
 
         for (String file : CREATE_SQL_FILES) {
             @SuppressWarnings("unchecked")
             final List<String> lines = IOUtils.readLines(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
             for (Object line : lines) {
                 sb.append(line).append("\n");
             }
         }
 
         return sb.toString();
     }
 
     public String createUpdateAgainstExistingSchema(URL schema, String jdbcUrl, String userName, String password, URL indexNames) throws Exception {
         final DatabaseSnapshot reference = createFromCommittedSchema(schema, indexNames);
         final DatabaseSnapshot target = LiquibaseModelFactory.readSnapshotFromDatabase(jdbcUrl, userName, password);
 
         final StringBuilder sb = new StringBuilder();
         printChanges(reference, target, sb);
         return sb.toString();
     }
 
 
     public String createDropAgainstExistingSchema(String jdbcUrl, String userName, String password) throws Exception {
         return createDropAgainstExistingSchema(LiquibaseModelFactory.readSnapshotFromDatabase(jdbcUrl, userName, password));
     }
 
     public String createDropAgainstExistingSchema(Connection connection) throws Exception {
         return createDropAgainstExistingSchema(LiquibaseModelFactory.readSnapshotFromDatabase(connection));
     }
 
     private String createDropAgainstExistingSchema(DatabaseSnapshot target) throws Exception {
         final DatabaseSnapshot reference = LiquibaseModelFactory.createEmptySnapshot();
 
         final StringBuilder sb = new StringBuilder();
         printChanges(reference, target, sb);
         return sb.toString();
     }
 
     private DatabaseSnapshot createFromCommittedSchema(URL schema, URL indexNames) throws Exception {
         HibernateDatabase hibernateDatabase = getDatabase(schema);
         IndexIdFactory.setIndexNames(hibernateDatabase, getIndexNames(indexNames));
 
         return LiquibaseModelFactory.create(hibernateDatabase);
     }
 
     public static HibernateDatabase getDatabase(URL schema) {
         try {
             return (HibernateDatabase) MARSHALLER.createUnmarshaller().unmarshal(schema);
         } catch (JAXBException e) {
             throw Throwables.propagate(e);
         }
     }
 
     public void printChanges(DatabaseSnapshot reference, DatabaseSnapshot target, StringBuilder sb) throws Exception {
         printNonRenames(reference, target, sb);
         printRenameStatements(reference, target, sb);
     }
 
     public void printNonRenames(DatabaseSnapshot reference, DatabaseSnapshot target, StringBuilder sb) throws Exception {
         CompareControl compareControl = new CompareControl(reference.getSnapshotControl().getTypesToInclude());
         DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(reference, target, compareControl);
 
         DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl(false, false, false));
 
         SqlGeneratorFactory generatorFactory = SqlGeneratorFactory.getInstance();
         for (ChangeSet changeSet : diffToChangeLog.generateChangeSets()) {
             for (Change change : changeSet.getChanges()) {
                 for (SqlStatement sqlStatement : change.generateStatements(LiquibaseModelFactory.DATABASE)) {
                     for (Sql sql : generatorFactory.generateSql(sqlStatement, LiquibaseModelFactory.DATABASE)) {
 
                         final String sqlString = sql.toSql();
                         if (sqlString.endsWith("DROP INDEX")){
                             sb.append(StringUtils.substringBefore(sqlString, " DROP INDEX")).append(";\n");
                         } else {
                             sb.append(sqlString).append(";\n");
                         }
                     }
                 }
             }
         }
     }
 
     public void printRenameStatements(DatabaseSnapshot reference, DatabaseSnapshot target, StringBuilder sb) {
        printRenameStatements(sb, reference, target.get(Index.class), RENAME_INDEX, new Function<Index, String>() {
             @Override
             public String apply(Index input) {
                 return input.getName();
             }
         }, new Function<Index, String>() {
             @Override
             public String apply(Index input) {
                 return input.getTable().getName();
             }
         });
 
        printRenameStatements(sb, reference, target.get(UniqueConstraint.class), RENAME_CONSTRAINT, new Function<UniqueConstraint, String>() {
             @Override
             public String apply(UniqueConstraint input) {
                 return input.getName();
             }
         }, new Function<UniqueConstraint, String>() {
             @Override
             public String apply(UniqueConstraint input) {
                 return input.getTable().getName();
             }
         });
 
        printRenameStatements(sb, reference, target.get(ForeignKey.class), RENAME_CONSTRAINT, new Function<ForeignKey, String>() {
             @Override
             public String apply(ForeignKey input) {
                 return input.getName();
             }
         }, new Function<ForeignKey, String>() {
             @Override
             public String apply(ForeignKey input) {
                 return input.getForeignKeyTable().getName();
             }
         });
 
        printRenameStatements(sb, reference, target.get(PrimaryKey.class), RENAME_CONSTRAINT, new Function<PrimaryKey, String>() {
             @Override
             public String apply(PrimaryKey input) {
                 return input.getName();
             }
         }, new Function<PrimaryKey, String>() {
             @Override
             public String apply(PrimaryKey input) {
                 return input.getTable().getName();
             }
         });
     }
 
    private <T extends DatabaseObject> void printRenameStatements(StringBuilder sb, DatabaseSnapshot referenceSnapshot, Collection<T> targetList, String renameStatement, Function<T, String> getName, Function<T, String> getTableName) {
         for (T target : targetList) {
            T reference = referenceSnapshot.get(target);
             if (reference != null) {
                 String referenceName = getName.apply(reference);
                 String targetName = getName.apply(target);
                 //noinspection ConstantConditions
                 if (!referenceName.equalsIgnoreCase(targetName)) {
                     sb.append(getRenameStatement(renameStatement, getTableName.apply(target), targetName, referenceName)).append("\n");
                 }
             }
         }
     }
 
     public static String getRenameStatement(String renameStatement, String tableName, String from, String to) {
         return String.format(renameStatement, tableName, from, to);
     }
 }
