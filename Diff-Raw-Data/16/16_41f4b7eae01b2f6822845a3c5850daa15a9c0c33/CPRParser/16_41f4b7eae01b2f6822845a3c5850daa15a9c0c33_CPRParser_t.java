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
 
 
 package dk.nsi.sdm4.cpr.parser;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import dk.nsi.sdm4.core.domain.Dataset;
 import dk.nsi.sdm4.core.domain.Entities;
 import dk.nsi.sdm4.core.domain.TemporalEntity;
 import dk.nsi.sdm4.core.parser.Parser;
 import dk.nsi.sdm4.core.parser.ParserException;
 import dk.nsi.sdm4.core.persistence.Persister;
 import dk.nsi.sdm4.core.util.Dates;
 import dk.nsi.sdm4.cpr.parser.models.Klarskriftadresse;
 import dk.nsi.sdm4.cpr.parser.models.NavneBeskyttelse;
 import dk.nsi.sdm4.cpr.parser.models.Navneoplysninger;
 import dk.nsi.sdm4.cpr.parser.models.Personoplysninger;
 import dk.sdsd.nsp.slalog.api.SLALogItem;
 import dk.sdsd.nsp.slalog.api.SLALogger;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class CPRParser implements Parser {
 	private static final Logger logger = Logger.getLogger(CPRParser.class);
 
 	@Value("${spooler.cpr.file.pattern.person}")
 	private Pattern personFilePattern;
 
 	@Value("${spooler.cpr.file.pattern.person.delta}")
 	private Pattern personFileDeltaPattern;
 
	@Value("${spooler.cpr.max.hours.between.runs}")
 	private int maxHoursBetweenRuns;
 
 	@Autowired
 	private SLALogger slaLogger;
 
 	@Autowired
 	private CprSingleFileImporter cprSingleFileImpoter;
 
 	@Autowired
 	Persister persister;
 
 	@Autowired
 	JdbcTemplate jdbcTemplate;
 
     @Override
     public void process(File dataset) throws ParserException {
 	    Preconditions.checkNotNull(dataset, "dataset is null");
 	    Preconditions.checkState(dataset.isDirectory(), "dataset " + dataset.getAbsolutePath() + " is not a directory");
 	    Preconditions.checkState(dataset.canRead(), "dataset directory" + dataset.getAbsolutePath() + " is not readable");
 
 	    File[] input = dataset.listFiles();
         for (File personFile : input) {
             if (!isPersonFile(personFile)) {
                 throw new ParserException("File " + personFile.getAbsolutePath() + " is not a valid CPR file. Nothing will be imported from the fileset.");
             }
         }
 
         SLALogItem slaLogItem = slaLogger.createLogItem("CPRImport", "All Files");
         try {
 
             // Check that the sequence is kept.
 
             ArrayList<String> cprWithChanges = Lists.newArrayList();
 
 	        for (File personFile : input) {
 		        SLALogItem slaLogItemFile = slaLogger.createLogItem("CPRImport of file", personFile.getName());
 		        logger.info("Started parsing CPR file. file=" + personFile.getAbsolutePath());
 
 		        CPRDataset changes = cprSingleFileImpoter.parse(personFile);
 
 		        if (isDeltaFile(personFile)) {
 			        Date previousVersion = getLatestVersion();
 
 			        if (previousVersion == null) {
 				        logger.warn("Didn't find any previous versions of CPR. Asuming an initial import and skipping sequence checks.");
 			        }
 		        }
 
 		        // All these entities affect the person table.
 
 		        Set<Class<?>> classesObservedByPVIT = Sets.newHashSet();
 		        classesObservedByPVIT.add(Navneoplysninger.class);
 		        classesObservedByPVIT.add(Klarskriftadresse.class);
 		        classesObservedByPVIT.add(Personoplysninger.class);
 		        classesObservedByPVIT.add(NavneBeskyttelse.class);
 
 		        for (Dataset<? extends TemporalEntity> changesDataset : changes.getDatasets()) {
 			        persister.persistDeltaDataset(changesDataset);
 
 			        // The GOS/CPR component requires a table with
 			        // a combination columns (CPR, ModifiedDate).
 			        //
 			        // The key for all CPR entities is the CPR and thus
 			        // we can safely case the key value.
 
 			        for (TemporalEntity record : changesDataset.getEntities()) {
 				        if (!classesObservedByPVIT.contains(record.getClass())) continue;
 
 				        // It does not matter that the same CPR is added
 				        // multiple times. It is a set and only contains
 				        // distinct entries.
 
 				        String cpr = Entities.getEntityID(record).toString();
 
 				        try {
 					        Preconditions.checkState(cpr.length() == 10, "Invalid key used in the CPR parser. Only CPR keys are supported.");
 				        } catch (Exception e) {
 					        slaLogItemFile.setCallResultError("CPR Import failed importing file " + personFile.getName() + " - Cause: " + e.getMessage());
 					        slaLogItemFile.store();
 
 					        throw new RuntimeException(e);
 				        }
 
 				        cprWithChanges.add(cpr);
 			        }
 		        }
 
 		        // Add latest 'version' date to database if we are not importing
 		        // a full set.
 
 		        if (isDeltaFile(personFile)) {
 			        insertVersion(changes.getValidFrom());
 		        }
 
 		        slaLogItemFile.setCallResultOk();
 		        slaLogItemFile.store();
 	        }
 
 	        // Update the GOS/CPR table with the current time stamp and
             // CPR numbers.
 
 	        Date modifiedDate = new Date();
             for (String cpr : cprWithChanges) {
 	            jdbcTemplate.update("REPLACE INTO ChangesToCPR (CPR, ModifiedDate) VALUES (?,?)", cpr, modifiedDate);
             }
 
             slaLogItem.setCallResultOk();
             slaLogItem.store();
 
         } catch (ParserException e) {
 	        markAsFailed(slaLogItem, e);
 	        throw e;
         } catch (Exception e) {
 	        markAsFailed(slaLogItem, e);
             throw new RuntimeException(e);
         }
     }
 
 	private void markAsFailed(SLALogItem slaLogItem, Exception e) {
 		slaLogItem.setCallResultError("CPR Import failed - Cause: " + e.getMessage());
 		slaLogItem.store();
 	}
 
 	private boolean isPersonFile(File f) {
         return personFilePattern.matcher(f.getName()).matches();
     }
 
     private boolean isDeltaFile(File f) {
         return personFileDeltaPattern.matcher(f.getName()).matches();
     }
 
     public Date getLatestVersion() throws SQLException {
 	    try {
             return jdbcTemplate.queryForObject("SELECT MAX(IkraftDato) AS Ikraft FROM PersonIkraft", Date.class);
 	    } catch (EmptyResultDataAccessException ignored) {
             // We just return null if no previous version of CPR has been imported.
 		    return null;
 	    }
     }
 
     void insertVersion(Date calendar) throws SQLException {
 		jdbcTemplate.update("INSERT INTO PersonIkraft (IkraftDato) VALUES ('" + Dates.toSqlDate(calendar) + "')");
     }
 
     @Override
     public String getHome() {
         return "cprimporter";
     }
 
 	@Override
 	public int getMaxHoursBetweenRuns() {
 		return maxHoursBetweenRuns;
 	}
 }
