 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Contributor(s): Contributors are attributed in the source code
  * where applicable.
  *
  * The Original Code is "Stamdata".
  *
  * The Initial Developer of the Original Code is Trifork Public A/S.
  *
  * Portions created for the Original Code are Copyright 2011,
  * Lægemiddelstyrelsen. All Rights Reserved.
  *
  * Portions created for the FMKi Project are Copyright 2011,
  * National Board of e-Health (NSI). All Rights Reserved.
  */
 package dk.nsi.sdm4.core.persistence.recordpersister;
 
 import org.apache.log4j.Logger;
 import org.joda.time.Instant;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import static dk.nsi.sdm4.core.persistence.recordpersister.FieldSpecification.RecordFieldType.*;
 
 public class RecordFetcher {
 	private static final Logger log = Logger.getLogger(RecordFetcher.class);
     private Instant transactionTime;
 
 	@Autowired
 	private JdbcTemplate jdbcTemplate;
 
     @Autowired
     public RecordFetcher(Instant transactionTime) {
         this.transactionTime = transactionTime;
     }
 
     public RecordFetcher() {
         resetTransactionTime();
     }
 
     public void resetTransactionTime() {
         this.transactionTime = new Instant();
     }
 
     public void setTransactionTime(Instant transactionTime) {
         this.transactionTime = transactionTime;
     }
 
     public Record fetchCurrent(String key, RecordSpecification recordSpecification,
                                String lookupColumn) {
         // KPN ValidTo does not have to be null as long as it is in the future it is ok
         //             futhermore ValidFrom should be checked here aswell
         Timestamp validAtStamp = new Timestamp(transactionTime.getMillis());
 		Record record;
 		try {
             if (key != null) {
                String sql = String.format("SELECT * FROM %s WHERE %s = ? AND (validTo IS NULL OR validTo > ?) AND (validFrom <= ?)" ,
                         recordSpecification.getTable(), lookupColumn);
                 record = jdbcTemplate.queryForObject(sql,
                         new RecordRowsetMapper(recordSpecification), key, validAtStamp, validAtStamp);
             } else {
                 // A record could have a key with null as value.
                String sql = String.format("SELECT * FROM %s WHERE %s IS NULL AND (validTo IS NULL OR validTo > ?) AND (validFrom <= ?)" ,
                         recordSpecification.getTable(), lookupColumn);
                 record = jdbcTemplate.queryForObject(sql,
                         new RecordRowsetMapper(recordSpecification), validAtStamp, validAtStamp);
             }
 		} catch (EmptyResultDataAccessException e) {
 			// når der ingen åben record er, forventer klienter bare at vi returnerer null
 			return null;
 		}
 
 		if (log.isDebugEnabled()) {
 			log.debug("Fetch current for " + recordSpecification.getTable() + " " + lookupColumn + "=" + key + " returning " + record);
 		}
 
 		return record;
 	}
 
 	public Record fetchCurrent(String key, RecordSpecification recordSpecification) throws SQLException {
 		return fetchCurrent(key, recordSpecification, recordSpecification.getKeyColumn());
 	}
 
     /**
      * Fetch currently valid record including metadata
     * @param key
     * @param recordSpecification
     * @return
      */
     public RecordWithMetadata fetchCurrentWithMeta(String key, RecordSpecification recordSpecification) {
         return fetchWithMetaAt(key, transactionTime, recordSpecification);
     }
 
     /**
      * Fetch a record including metadata that was valid at a specific time.
      * @param key Key value of record that should be fetched
      * @param validAt When should the record be valid
      * @param recordSpecification Record specification
      * @return The found record or null if no record was found
      */
     public RecordWithMetadata fetchWithMetaAt(String key, Instant validAt, RecordSpecification recordSpecification) {
         String keyColumn = recordSpecification.getKeyColumn();
         Timestamp validAtStamp = new Timestamp(validAt.getMillis());
         RecordWithMetadata recordWithMeta;
         try {
             // Some register also uses null as id
             if (key != null) {
                 String queryString = String.format("SELECT * FROM %s WHERE %s=? " +
                         "AND ValidFrom <= ? AND (ValidTo IS NULL OR ValidTo > ?) ", recordSpecification.getTable(), keyColumn);
                 recordWithMeta = jdbcTemplate.queryForObject(queryString,
                         new RecordMetaRowsetMapper(recordSpecification), key, validAtStamp, validAtStamp);
             } else {
                 String queryString = String.format("SELECT * FROM %s WHERE %s IS NULL " +
                         "AND ValidFrom <= ? AND (ValidTo IS NULL OR ValidTo > ?) ", recordSpecification.getTable(), keyColumn);
                 recordWithMeta = jdbcTemplate.queryForObject(queryString,
                         new RecordMetaRowsetMapper(recordSpecification), validAtStamp, validAtStamp);
             }
         } catch (EmptyResultDataAccessException e) {
             // når der ingen åben recordWithMeta er, forventer klienter bare at vi returnerer null
             return null;
         }
         if (log.isDebugEnabled()) {
             log.debug("Fetch current for " + recordSpecification.getTable() + " " + keyColumn + "=" + key + " returning " + recordWithMeta);
         }
         return recordWithMeta;
     }
 
     // Indtil 7/7-2012 var der her en fetchSince-metode. Den er fjernet, da dens semantik var uklar, og ingen parsere
 	// brugte den. Den antog at databasetabellen for en Record havde en kolonne PID, hvilket kun gælder for én parser
 	// blandt de nuværende.
 	// Når/hvis NSP-modulerne skal over på det nye core, må vi genindføre metoden med en mere veldefineret semantik.
 
     // TODO: KPN Not sure I agree fetchsince is used by the batch copy service, so it should be recreated if the service
     // should run on sdm4 at some point...
 
 	private class RecordRowsetMapper implements RowMapper<Record> {
 		private RecordSpecification recordSpecification;
 
 		private RecordRowsetMapper(RecordSpecification recordSpecification) {
 			this.recordSpecification = recordSpecification;
 		}
 
 		@Override
 		public Record mapRow(ResultSet resultSet, int rowNum) throws SQLException {
 			RecordBuilder builder = new RecordBuilder(recordSpecification);
 
 			for (FieldSpecification fieldSpec : recordSpecification.getFieldSpecs()) {
 				if (log.isDebugEnabled()) {
 					log.debug("Processing field " + fieldSpec.name);
 				}
 
 				if (fieldSpec.persistField) {
 					String fieldName = fieldSpec.name;
 					if (fieldSpec.type == NUMERICAL) {
                         long fieldVal = resultSet.getLong(fieldName);
                         if (resultSet.wasNull()) {
                             builder.field(fieldName, null);
                         } else {
                             builder.field(fieldName, fieldVal);
                         }
 					} else if (fieldSpec.type == DECIMAL10_3) {
                         double fieldVal = resultSet.getDouble(fieldName);
                         if (resultSet.wasNull()) {
                             builder.field(fieldName, null);
                         } else {
                             builder.field(fieldName, fieldVal);
                         }
 					} else if (fieldSpec.type == ALPHANUMERICAL) {
 						builder.field(fieldName, resultSet.getString(fieldName));
 					} else {
 						throw new AssertionError("Invalid field specifier " + fieldSpec.type + " used");
 					}
 				}
 			}
 
 			return builder.build();
 		}
 	}
 
     private class RecordMetaRowsetMapper implements RowMapper<RecordWithMetadata> {
 
         private RecordRowsetMapper recordMapper;
 
         public RecordMetaRowsetMapper(RecordSpecification recordSpecification) {
             recordMapper = new RecordRowsetMapper(recordSpecification);
         }
 
         @Override
         public RecordWithMetadata mapRow(ResultSet resultSet, int i) throws SQLException {
             Instant modifiedDate = new Instant(resultSet.getTimestamp("ModifiedDate"));
             Instant validTo = new Instant(resultSet.getTimestamp("ValidTo"));
             Instant validFrom = new Instant(resultSet.getTimestamp("ValidFrom"));
             Long pid = (Long) resultSet.getObject("PID");
             Record record = recordMapper.mapRow(resultSet, i);
             return new RecordWithMetadata(validFrom, validTo, modifiedDate, pid, record);
         }
     }
 }
