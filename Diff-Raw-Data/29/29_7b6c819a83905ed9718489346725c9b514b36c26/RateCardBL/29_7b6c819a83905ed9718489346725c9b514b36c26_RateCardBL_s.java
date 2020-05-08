 /*
  JBILLING CONFIDENTIAL
  _____________________
 
  [2003] - [2012] Enterprise jBilling Software Ltd.
  All Rights Reserved.
 
  NOTICE:  All information contained herein is, and remains
  the property of Enterprise jBilling Software.
  The intellectual and technical concepts contained
  herein are proprietary to Enterprise jBilling Software
  and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.pricing;
 
 import au.com.bytecode.opencsv.CSVReader;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.pricing.db.RateCardDAS;
 import com.sapienter.jbilling.server.pricing.db.RateCardDTO;
 import com.sapienter.jbilling.server.util.Context;
 import com.sapienter.jbilling.server.util.sql.JDBCUtils;
 import com.sapienter.jbilling.server.util.sql.TableGenerator;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.ScrollableResults;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.jdbc.core.BatchPreparedStatementSetter;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DataSourceUtils;
 
 import javax.sql.DataSource;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.String;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Business Logic for RateCardDTO crud, and for creating and updating the rating tables
  * associated with the card.
  *
  * @author Brian Cowdery
  * @since 16-Feb-2012
  */
 public class RateCardBL {
     private static final Logger LOG = Logger.getLogger(RateCardBL.class);
 
     public static final int BATCH_SIZE = 10;
     public static final String DEFAULT_DATA_TYPE = "varchar(255)";
 
     private RateCardDAS rateCardDas;
     private JdbcTemplate jdbcTemplate;
     private TableGenerator tableGenerator;
 
     private RateCardDTO rateCard;
 
     public RateCardBL() {
         _init();
     }
 
     public RateCardBL(Integer rateCardId) {
         _init();
         set(rateCardId);
     }
 
     public RateCardBL(RateCardDTO rateCard) {
         _init();
         this.rateCard = rateCard;
         this.tableGenerator = new TableGenerator(rateCard.getTableName(), RateCardDTO.TABLE_COLUMNS);
     }
 
     public void set(Integer rateCardId) {
         this.rateCard = rateCardDas.find(rateCardId);
         this.tableGenerator = new TableGenerator(rateCard.getTableName(), RateCardDTO.TABLE_COLUMNS);
     }
 
     private void _init() {
         this.rateCardDas = new RateCardDAS();
         this.jdbcTemplate = Context.getBean(Context.Name.JDBC_TEMPLATE);
     }
 
     /**
      * Returns the RateCardDTO object being managed by this BL class.
      * @return rate card object
      */
     public RateCardDTO getEntity() {
         return rateCard;
     }
 
     /**
      * Create a new rate card with the specified rates.
      *
      * @param rateCard rate card to create
      * @param rates file handle of the CSV on disk containing the rates.
      * @return id of the saved rate card
      */
     public Integer create(RateCardDTO rateCard, File ratesFile) {
         if (rateCard != null) {
             LOG.debug("Saving new rate card " + rateCard);
             this.rateCard = rateCardDas.save(rateCard);
             this.tableGenerator = new TableGenerator(this.rateCard.getTableName(), RateCardDTO.TABLE_COLUMNS);
             LOG.debug("Creating a new rate table & saving rating data");
             if (ratesFile != null) {
                 try {
                     saveRates(ratesFile);
 
                 } catch (SessionInternalError e) {
                     dropRates();
                     throw e;
                 } catch (IOException e) {
                     dropRates();
                     throw new SessionInternalError("Could not load rating table", e, new String[] { "RateCardWS,rates,cannot.read.file" });
                 } catch (SQLException e) {
                     dropRates();
                     throw new SessionInternalError("Exception saving rates to database", e, new String[] { "RateCardWS,rates,cannot.save.rates.db.error" });
                 }
 
                 registerSpringBeans();
             }
 
             return this.rateCard.getId();
         }
 
         LOG.error("Cannot save a null RateCardDTO!");
         return null;
     }
 
     /**
      * Updates an existing rate card and rates.
      *
      * @param rateCard rate card to create
      * @param rates file handle of the CSV on disk containing the rates.
      */
     public void update(RateCardDTO rateCard, File ratesFile) {
         if (this.rateCard != null) {
             // re-create the rating table
             LOG.debug("Re-creating the rate table & saving updated rating data");
             if (ratesFile != null) {
                 dropRates();
 
                 try {
                     saveRates(ratesFile);
 
                 } catch (IOException e) {
                     dropRates();
                     throw new SessionInternalError("Could not load rating table", e, new String[] { "RateCardWS,rates,cannot.read.file" });
                 } catch (SQLException e) {
                     dropRates();
                     throw new SessionInternalError("Exception saving rates to database", e, new String[] { "RateCardWS,rates,cannot.save.rates.db.error" });
                 }
             }
 
             // prepare SQL to rename the table if the table name has changed
             String originalTableName = this.rateCard.getTableName();
             String alterTableSql = null;
 
             if (!originalTableName.equals(rateCard.getTableName())) {
                 alterTableSql = this.tableGenerator.buildRenameTableSQL(rateCard.getTableName());
                 //remove and re-register spring beans
                 removeSpringBeans();
             }
 
             // do update
             this.rateCard.setName(rateCard.getName());
             if (!this.rateCard.getTableName().equals(rateCard.getTableName())) {
                 this.rateCard.setTableName(rateCard.getTableName());
                 registerSpringBeans();
             }
 
             LOG.debug("Saving updates to rate card " + rateCard.getId());
             this.rateCard = rateCardDas.save(rateCard);
             this.tableGenerator = new TableGenerator(this.rateCard.getTableName(), RateCardDTO.TABLE_COLUMNS);
 
             // do rename after saving the new table name
             if (alterTableSql != null) {
                 LOG.debug("Renaming the rate table");
                 jdbcTemplate.execute(alterTableSql);
             }
 
             // re-register spring beans if rates were updated
             if (ratesFile != null) {
                 removeSpringBeans();
                 registerSpringBeans();
             }
 
         } else {
             LOG.error("Cannot update, RateCardDTO not found or not set!");
         }
     }
 
     /**
      * Deletes the current rate card managed by this class.
      */
     public void delete() {
         if (rateCard != null) {
             rateCardDas.delete(rateCard);
             dropRates();
 
         } else {
             LOG.error("Cannot delete, RateCardDTO not found or not set!");
         }
     }
 
 
 
     /*
             Rate Table Database Stuff
      */
 
     /**
      * Drop the rate table of a rate card.
      */
     public void dropRates() {
         String dropSql = tableGenerator.buildDropTableSQL();
         jdbcTemplate.execute(dropSql);
         LOG.debug("Dropped table '" + rateCard.getTableName() + "'");
     }
 
     /**
      * Updates the rate table of a rate card with the rating information in
      * the given CSF file of rates.
      *
      * @param rates file handle of the CSV on disk containing the rates.
      * @throws IOException if file does not exist or is not readable
      */
     public void saveRates(File ratesFile) throws IOException, SQLException {
     	
     	checkRateTableExistance();
     	
         CSVReader reader = new CSVReader(new FileReader(ratesFile));
         String[] line = reader.readNext();
         validateCsvHeader(line);
 
         // parse the header and read out the extra columns.
         // ignore the default rate card table columns as they should ALWAYS exist
         int start = RateCardDTO.TABLE_COLUMNS.size();
         for (int i = start; i <line.length; i++) {
             tableGenerator.addColumn(new TableGenerator.Column(line[i], DEFAULT_DATA_TYPE, true));
         }
 
         // create rate table
         String createSql = tableGenerator.buildCreateTableSQL();
         jdbcTemplate.execute(createSql);
         LOG.debug("Created table '" + rateCard.getTableName() + "'");
 
         // load rating data in batches
         String insertSql = tableGenerator.buildInsertPreparedStatementSQL();
         List<List<String>> rows = new ArrayList<List<String>>();
         for (int i = 1; i <= BATCH_SIZE; i++) {
             // add row to insert batch
             line = reader.readNext();
             if (line != null) rows.add(Arrays.asList(line));
 
             // end of file
             if (line == null) {
                 executeBatchInsert(insertSql, rows);
                 break; // done
             }
 
             // reached batch limit
             if (i == BATCH_SIZE) {
                 executeBatchInsert(insertSql, rows);
                 i = 1; rows.clear(); // next batch
             }
         }
     }
 
     private void checkRateTableExistance() throws SQLException {
     	
         DataSource dataSource = jdbcTemplate.getDataSource();
         Connection connection = DataSourceUtils.getConnection(dataSource);
     	List<String> tableNames = JDBCUtils.getAllTableNames(connection);
     	if (tableNames.contains(rateCard.getTableName())) {
     		throw new SessionInternalError("Exception saving rates to database.", new String[] { "RateCardWS,rates,rate.card.db.exist," + rateCard.getTableName()});
     	}
 	}
 
 	/**
      * Validates that the uploaded CSV file starts with the expected columns from {@link RateCardDTO#TABLE_COLUMNS}.
      * If the column names don't match or are in an incorrect order a SessionInternalError will be throw.
      *
      * @param header header line to validate
      * @throws SessionInternalError thrown if errors found in header data
      */
     private void validateCsvHeader(String[] header) throws SessionInternalError {
         List<String> errors = new ArrayList<String>();
 
         List<TableGenerator.Column> columns = RateCardDTO.TABLE_COLUMNS;
         for (int i = 0; i < columns.size(); i++) {
             String columnName = header[i].trim();
             String expected = columns.get(i).getName();
 
             if (!expected.equalsIgnoreCase(columnName)) {
                 errors.add("RateCardWS,rates,rate.card.unexpected.header.value," + expected + "," + columnName);
             }
         }
 
         if (!errors.isEmpty()) {
             throw new SessionInternalError("Rate card CSV has errors in the order of columns, or is missing required columns",
                     errors.toArray(new String[errors.size()]));
         }
     }
 
     /**
      * Inserts a batch of records into the database.
      *
      * @param insertSql prepared statement SQL
      * @param rows list of rows to insert
      */
     private void executeBatchInsert(String insertSql, final List<List<String>> rows) {
         LOG.debug("Inserting " + rows.size() + " records:");
         LOG.debug(rows);
 
         jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
             public void setValues(PreparedStatement preparedStatement, int batch) throws SQLException {
                 List<String> values = rows.get(batch);
                 for (int i = 0; i < values.size(); i++) {
                     String value = values.get(i);
 
                     // todo: we need a better solution here - maybe TableGenerator.Column should have a JDBC SQL Type?
                     switch (i) {
                         case 0:  // row id
                             preparedStatement.setInt(i + 1, StringUtils.isNotBlank(value) ? Integer.valueOf(value) : 0);
                             break;
 
                         case 3:  // rate card rate
                             preparedStatement.setBigDecimal(i + 1, StringUtils.isNotBlank(value) ? new BigDecimal(value) : BigDecimal.ZERO);
                             break;
 
                         default: // everything else
                             preparedStatement.setObject(i + 1, value);
                     }
                 }
             }
 
             public int getBatchSize() {
                 return rows.size();
             }
         });
     }
 
     /**
      * Returns a list of column names read from the rate table in the database.
      * @return column names
      */
     public List<String> getRateTableColumnNames() {
         DataSource dataSource = jdbcTemplate.getDataSource();
         Connection connection = DataSourceUtils.getConnection(dataSource);
 
         List<String> columns = Collections.emptyList();
 
         try {
             columns = JDBCUtils.getAllColumnNames(connection, rateCard.getTableName());
         } catch (SQLException e) {
             throw new SessionInternalError("Could not read columns from rate card table.", e,
                                            new String[] { "RateCardWS,rates,rate.card.cannot.read.rating.table" });
 
         } finally {
             DataSourceUtils.releaseConnection(connection, dataSource);
         }
 
         return columns;
     }
 
     /**
      * Returns a scrollable result set for reading the rate table rows.
      *
      * <strong>You MUST remember to close the result set when your done reading!</strong>
      *
      * @return scrollable result set
      */
     public ScrollableResults getRateTableRows() {
         return rateCardDas.getRateTableRows(rateCard.getTableName());
     }
 
 
 
     /*
             Spring Beans stuff
      */
 
     /**
      * Registers spring beans with the application context so support caching and look-up
      * of pricing from the rating tables.
      */
     public void registerSpringBeans() {
         RateCardBeanFactory factory = getBeanFactory();
 
         String readerBeanName = factory.getReaderBeanName();
         BeanDefinition readerBeanDef = factory.getReaderBeanDefinition(rateCard.getCompany().getId());
 
         String loaderBeanName = factory.getLoaderBeanName();
         BeanDefinition loaderBeanDef = factory.getLoaderBeanDefinition(readerBeanName);
 
         String finderBeanName = factory.getFinderBeanName();
         BeanDefinition finderBeanDef = factory.getFinderBeanDefinition(loaderBeanName);
 
         LOG.debug("Registering beans: " + readerBeanName + ", " + loaderBeanName + ", " + finderBeanName);
 
         // register spring beans!
         GenericApplicationContext ctx = (GenericApplicationContext) Context.getApplicationContext();
         ctx.registerBeanDefinition(readerBeanName, readerBeanDef);
         ctx.registerBeanDefinition(loaderBeanName, loaderBeanDef);
         ctx.registerBeanDefinition(finderBeanName, finderBeanDef);
     }
 
     /**
      * Removes registered spring beans from the application context.
      */
     public void removeSpringBeans() {
         RateCardBeanFactory factory = getBeanFactory();
 
         String readerBeanName = factory.getReaderBeanName();
         String loaderBeanName = factory.getLoaderBeanName();
         String finderBeanName = factory.getFinderBeanName();
         
         try {
 
         	LOG.debug("Removing beans: " + readerBeanName + ", " + loaderBeanName + ", " + finderBeanName);
 
         	GenericApplicationContext ctx = (GenericApplicationContext) Context.getApplicationContext();
         	ctx.removeBeanDefinition(readerBeanName);
         	ctx.removeBeanDefinition(loaderBeanName);
         	ctx.removeBeanDefinition(finderBeanName);
 
         } catch (NoSuchBeanDefinitionException e) {
         	LOG.warn("Beans not found");
         }
     }
 
     /**
      * Returns an instance of the {@link RateCardBeanFactory} for producing rate card beans
      * used for pricing.
      *
      * @return rate card bean factory
      */
     public RateCardBeanFactory getBeanFactory() {
         return new RateCardBeanFactory(rateCard);
     }
 }
