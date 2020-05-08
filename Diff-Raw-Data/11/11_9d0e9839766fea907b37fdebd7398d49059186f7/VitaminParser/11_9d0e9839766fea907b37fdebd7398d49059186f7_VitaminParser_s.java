 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.sdm4.vitamin.parser;
 
 import dk.nsi.sdm4.core.parser.Parser;
 import dk.nsi.sdm4.core.parser.ParserException;
 import dk.nsi.sdm4.core.parser.SingleLineRecordParser;
 import dk.nsi.sdm4.core.persistence.recordpersister.Record;
 import dk.nsi.sdm4.core.persistence.recordpersister.RecordFetcher;
 import dk.nsi.sdm4.core.persistence.recordpersister.RecordPersister;
 import dk.nsi.sdm4.core.persistence.recordpersister.RecordSpecification;
 import dk.nsi.sdm4.vitamin.exception.InvalidVitaminDatasetException;
 import dk.nsi.sdm4.vitamin.recordspecs.VitaminRecordSpecs;
 import dk.sdsd.nsp.slalog.api.SLALogItem;
 import dk.sdsd.nsp.slalog.api.SLALogger;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * Hoved-klasse i vitaminimporter-modulet. Ansvarlig for at koordinere importen af et datasæt af filer.
  * Antager at process-modetoden kaldes af ParserExecutor med et validt dataset
 */
 public class VitaminParser implements Parser {
 	private static final Logger log = Logger.getLogger(VitaminParser.class);
 
 	@Autowired
 	private SLALogger slaLogger;
 
 	@Autowired
 	private RecordPersister persister;
 
 	@Autowired
 	private JdbcTemplate jdbcTemplate;
 
 	@Autowired
 	private RecordFetcher fetcher;
 
 	private final Map<String, RecordSpecification> specsForFiles = new HashMap<String, RecordSpecification>() {
 		{
 			put("nat01.txt", VitaminRecordSpecs.GRUNDDATA_RECORD_SPEC);
 			put("rad01.txt", VitaminRecordSpecs.GRUNDDATA_RECORD_SPEC);
 			put("vit01.txt", VitaminRecordSpecs.GRUNDDATA_RECORD_SPEC);
 
 			put("nat09.txt", VitaminRecordSpecs.FIRMADATA_RECORD_SPEC);
 			put("rad09.txt", VitaminRecordSpecs.FIRMADATA_RECORD_SPEC);
 			put("vit09.txt", VitaminRecordSpecs.FIRMADATA_RECORD_SPEC);
 
 			put("nat10.txt", VitaminRecordSpecs.UDGAAEDENAVNE_RECORD_SPEC);
 			put("rad10.txt", VitaminRecordSpecs.UDGAAEDENAVNE_RECORD_SPEC);
 			put("vit10.txt", VitaminRecordSpecs.UDGAAEDENAVNE_RECORD_SPEC);
 
 			put("nat30.txt", VitaminRecordSpecs.INDHOLDSSTOFFER_RECORD_SPEC);
 			put("vit30.txt", VitaminRecordSpecs.INDHOLDSSTOFFER_RECORD_SPEC);
 		}
 	};
 
 	private static final String FILE_ENCODING = "CP865";
 
 	/**
 	 * Ansvarlig for at håndtere import af et vitamin-datasæt.
 	 * Antager, at den bliver kaldt i en kontekst hvor der allere er etableret en transaktion.
 	 * @see dk.nsi.sdm4.core.parser.Parser#getHome()
 	 */
 	@Override
 	@Transactional(propagation = Propagation.MANDATORY)
 	public void process(File datadir) throws ParserException {
 		SLALogItem slaLogItem = slaLogger.createLogItem("VitaminParser", "All");
 
 		validateDataset(datadir);
 
 		try {
 			assert datadir.listFiles() != null;
 			for (File file : datadir.listFiles()) {
 				RecordSpecification spec = specsForFiles.get(file.getName());
 				if (spec != null) {
 					processSingleFile(file, spec);
 				} else {
 					// hvis vi ikke har nogen spec, skal filen ikke processeres.
 					// Filen kan fx være en slet01.txt fil som er med i de zippede udtræk fra LMS, så det er en forventet situation og skal debug-logges
 					// Andre filer tyder på at noget er galt og skal warn-logges
 					log.log(levelForUnexpectedFile(file), "Ignoring file " + file.getAbsolutePath());
 				}
 			}
 		} catch (RuntimeException e) {
 			slaLogItem.setCallResultError("VitaminParser failed - Cause: " + e.getMessage());
 			slaLogItem.store();
 
 			throw new ParserException(e);
 		}
 
 
 		slaLogItem.setCallResultOk();
 		slaLogItem.store();
 	}
 
 	// kun ikke-private for at tillade test, kaldes ikke udefra
 	Level levelForUnexpectedFile(File file) {
 		Level logLevel;
 		if (file.getName().matches("slet\\d*.txt")) {
 			logLevel = Level.DEBUG;
 		} else {
 			logLevel = Level.WARN;
 		}
 		return logLevel;
 	}
 
 	/**
 	 * @throws InvalidVitaminDatasetException if a required file is missing or datadir is not a readable directory
 	 */
 	private void validateDataset(File datadir) {
 		checkThat((datadir != null), "datadir is null");
 		checkThat(datadir.isDirectory(), "datadir " + datadir.getAbsolutePath() + " is not a directory");
 		checkThat(datadir.canRead(), "datadir " + datadir.getAbsolutePath() + " is not readable");
 
 		Set<String> requiredFileNames = new HashSet<String>(specsForFiles.keySet());
 
 		File[] actualFiles = datadir.listFiles();
 		Set<String> actualFilenames = new HashSet<String>();
 		for (File actualFile : actualFiles) {
 			actualFilenames.add(actualFile.getName());
 		}
 
 		requiredFileNames.removeAll(actualFilenames);
 
 		if (!requiredFileNames.isEmpty()) {
 			throw new InvalidVitaminDatasetException("Missing files " + requiredFileNames + " from data directory " + datadir.getAbsolutePath());
 		}
 	}
 
 	// kun ikke-private for at tillade test, kaldes ikke udefra
 	void processSingleFile(File file, RecordSpecification spec) {
 		if (log.isDebugEnabled()) {
 			log.debug("Processing file " + file + " with spec " + spec.getClass().getSimpleName());
 		}
 		SLALogItem slaLogItem = slaLogger.createLogItem("VitaminParser importer of file", file.getName());
 
 		try {
 			Set<Long> drugidsFromFile = parseAndPersistFile(file, spec);
 			invalidateRecordsRemovedFromFile(drugidsFromFile, spec);
 		} catch (RuntimeException e) {
 			slaLogItem.setCallResultError("VitaminParser failed - Cause: " + e.getMessage());
 			slaLogItem.store();
 
 			throw new ParserException(e);
 		}
 
 		slaLogItem.setCallResultOk();
 		slaLogItem.store();
 	}
 
 	private Set<Long> parseAndPersistFile(File file, RecordSpecification spec) {
 		SingleLineRecordParser grunddataParser = new SingleLineRecordParser(spec);
 		Set<Long> drugidsFromFile = new HashSet<Long>();
 
 		List<String> lines = readFile(file);// files are very small, it's okay to hold them in memory
 		if (log.isDebugEnabled()) log.debug("Read " + lines.size() + " lines from file " + file.getAbsolutePath());
 
 		for (String line : lines) {
 			if (log.isDebugEnabled()) log.debug("Processing line " + line);
 			Record record = grunddataParser.parseLine(line);
 			if (log.isDebugEnabled()) log.debug("Parsed line to record " + record);
 
 			drugidsFromFile.add((Long) record.get(spec.getKeyColumn()));
 			persistRecordIfNeeeded(spec, record);
 		}
 
 		return drugidsFromFile;
 	}
 
 	private List<String> readFile(File file) {
 		try {
 			return FileUtils.readLines(file, FILE_ENCODING);
 		} catch (IOException e) {
 			throw new ParserException("Unable to read file " + file.getAbsolutePath(), e);
 		}
 	}
 
 	private void persistRecordIfNeeeded(RecordSpecification spec, Record record) {
 		Record existingRecord = findRecordWithSameKey(record, spec);
 		if (existingRecord != null) {
 			if (existingRecord.equals(record)) {
 				// no need to do anything
 				if (log.isDebugEnabled()) log.debug("Ignoring record " + record + " for spec " + spec.getTable() + " as we have identical record in db");
 			} else {
 				if (log.isDebugEnabled()) log.debug("Setting validTo on database record " + existingRecord + " for spec " + spec.getTable() + " before insertion of new record " + record);
				jdbcTemplate.update("UPDATE " + spec.getTable() + " set ValidTo = ? WHERE " + spec.getKeyColumn() + " = ? AND ValidTo IS NULL",
						persister.getTransactionTime().toDateTime().toDate(),
 						existingRecord.get(spec.getKeyColumn()));
 				persist(record, spec);
 			}
 		} else {
 			if (log.isDebugEnabled()) log.debug("Persisting new record " + record + " for spec " + spec.getTable());
 			persist(record, spec);
 		}
 	}
 
 	private Record findRecordWithSameKey(Record record, RecordSpecification spec) {
 		try {
 			return fetcher.fetchCurrent(record.get(spec.getKeyColumn())+"", spec);
 		} catch (SQLException e) {
 			throw new ParserException("While trying to find record with same key as " + record + " and spec " + spec, e);
 		}
 	}
 
 	private void persist(Record record, RecordSpecification spec) {
 		try {
 			persister.persist(record, spec);
 		} catch (SQLException e) {
 			throw new ParserException("Unable to persist record " + record + " with spec " + spec, e);
 		}
 	}
 
 	private void invalidateRecordsRemovedFromFile(Set<Long> idsFromFile, RecordSpecification spec) {
 		// we'll compute the list of ids of record to be invalidated by fetching all the ids from the database, then weeding out all the ids that exist in the input file - these shouldn't be removed
 		Set<Long> idsToBeInvalidated = new HashSet<Long>(jdbcTemplate.queryForList("SELECT " + spec.getKeyColumn() + " from " + spec.getTable() + " WHERE ValidTo IS NULL", Long.class));
 		idsToBeInvalidated.removeAll(idsFromFile);
 
 		if (!idsToBeInvalidated.isEmpty()) {
			jdbcTemplate.update("UPDATE " + spec.getTable() + " SET ValidTo = ? WHERE " + spec.getKeyColumn() + " IN (" + StringUtils.join(idsToBeInvalidated, ',')  + ") AND ValidTo IS NULL", persister.getTransactionTime().toDateTime().toDate());
 		}
 	}
 
 	/**
 	 * @see dk.nsi.sdm4.core.parser.Parser#getHome()
 	 */
 	@Override
 	public String getHome() {
 		return "vitaminimporter";
 	}
 
 	private void checkThat(boolean expression, String errorMessage) {
 		if (!expression) {
 			throw new InvalidVitaminDatasetException(errorMessage);
 		}
 	}
 }
