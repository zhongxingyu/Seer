 package net.straininfo2.grs.idloader.db;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 import org.springframework.transaction.support.TransactionCallbackWithoutResult;
 import org.springframework.transaction.support.TransactionTemplate;
 
 import javax.sql.DataSource;
 import java.io.*;
 import java.math.BigDecimal;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Parses and Loads information about Bioprojects into a database.
  */
 public abstract class ProjectInfoLoader {
 
     /*
        Note: parsing should really be in a separate class, but is
        mixed in with the DB loading in all the anonymous execution
        classes here.
      */
 
     private final static Logger logger = LoggerFactory
             .getLogger(ProjectInfoLoader.class);
 
     private static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
 
     private static final DateFormat DATE_FORMAT_WITH_TIME = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
 
     static {
         /* Why US/Eastern? There is no TZ information and NCBI
            is located in the east US, so...
            What we "should" do is store the date as a string (since
            we don't have enough information to accurately represent
            the location. Unfortunately, if you ever want to do
            something with that date you're going to have to convert
            it anyway. Might as well assume it's midnight in the
            eastern US and deal with the fact that it might be off by
            as much as a day.
            It occurs to me that the dates without a time are actually
            a period, i.e. they represent an event that occurred at
            some point during that day, so you could store it as a date
            range. But you still don't have way to know when it actually
            starts/ends. It doesn't matter though because nobody who
            uses these dates for anything (that I can think of) needs
            that kind of resolution.
           */
         DEFAULT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
         DATE_FORMAT_WITH_TIME.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
     }
 
     private static final StringRowMapper STRING_ROW_MAPPER = new StringRowMapper();
 
    private JdbcTemplate template;
 
     private TransactionTemplate txTemplate;
 
     private Set<Long> projectIds = null;
 
     private String namespace;
 
    public JdbcTemplate getTemplate() {
         return template;
     }
 
     public void setDataSource(DataSource source) {
        this.template = new JdbcTemplate(source);
     }
 
     public void setTransactionManager(
             PlatformTransactionManager transactionManager) {
         this.txTemplate = new TransactionTemplate(transactionManager);
         txTemplate
                 .setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
     }
 
     public TransactionTemplate getTxTemplate() {
         return txTemplate;
     }
 
     public String getNamespace() {
         return namespace;
     }
 
     @SuppressWarnings("SameParameterValue")
     public void setNamespace(String namespace) {
         this.namespace = namespace;
     }
 
     /**
      * Very simple RowMapper for a list of Strings.
      */
     public static class StringRowMapper implements RowMapper<String> {
         @Override
         public String mapRow(ResultSet rs, int rowNum) throws SQLException {
             return rs.getString(1);
         }
     }
 
     private void runTransaction(final Runnable runner) {
         getTxTemplate().execute(new TransactionCallbackWithoutResult() {
             @Override
             protected void doInTransactionWithoutResult(TransactionStatus ts) {
                 runner.run();
             }
         });
     }
 
     private static String nullIfEmptyOrMinus(String str) {
         return "".equals(str) || "-".equals(str) ? null : str;
     }
 
     /*
     These are not used at the moment because it was hard to get them to
     work properly, and it's not that important.
      */
     public Date getLatestProkaryoteUpdate() {
         return getLatestUpdate(namespace + ".genome_prok");
     }
 
     public Date getLatestEukaryoteUpdate() {
         return getLatestUpdate(namespace + ".genome_euk");
     }
 
     public Date getLatestEnvironmentalUpdate() {
         return getLatestUpdate(namespace + ".genome_env");
     }
 
     protected abstract Date getLatestUpdate(String table);
 
     protected abstract String insertAccessionMapping(String gtable, String accLabel);
 
     protected abstract String insertProjectLab(String table);
 
     protected abstract void updateGenomeProkComplete(long proj_id, Long chromosome_size, Long plasmid_number, Date date_released, Date date_modified);
 
     protected abstract void insertIntoGenomeProkComplete(long proj_id, Long chromosome_size, Long plasmid_number, Date date_released, Date date_modified);
 
     protected abstract void insertIntoProkaryotesInProgress(long proj_id, String sequence_availability, String refseq_accession, String genbank_accession, Long contig_nr, Long cds_nr, Date date_released, String center_name, String center_url);
 
     protected abstract void updateProkaryotesInProgress(long proj_id, String sequence_availability, String refseq_accession, String genbank_accession, Long contig_nr, Long cds_nr, Date date_released, String center_name, String center_url);
 
     /**
      * Creates and initialises tables in target DB.
      */
     public abstract void configureTables();
 
     /* Field parse functions */
 
     static boolean containsNoData(String str) {
         return "".equals(str) || "-".equals(str);
     }
 
     static Long parseLong(String nrString) {
         return (containsNoData(nrString)) ? null : Long.parseLong(nrString);
     }
 
     static Integer parseInteger(String nrString) {
         return (containsNoData(nrString)) ? null : Integer.parseInt(nrString);
     }
 
     static Date parseDate(String dateString) {
         try {
             return containsNoData(dateString) ?
                     null : DEFAULT_DATE_FORMAT.parse(dateString);
         } catch (ParseException e) {
             logger.warn("Could not parse date.", e);
             return null;
         }
     }
 
     static Date parseDateTime(String dateString) {
         try {
             return containsNoData(dateString) ? null
                     : DATE_FORMAT_WITH_TIME
                     .parse(dateString);
         } catch (ParseException e) {
             logger.warn("Could not parse date.", e);
             return null;
         }
     }
 
     static BigDecimal parseDecimalSize(String str) {
         if ("".equals(str)) {
             return null;
         } else {
             return new BigDecimal(str);
         }
     }
 
     static List<String> parseConsortia(String str) {
         if (containsNoData(str)) {
             return Collections.emptyList();
         } else {
             return Arrays.asList(str.split("\\|"));
         }
     }
 
     /* Table update functions */
 
     public void updateProkaryotesMain(InputStream stream) {
         try {
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(stream, "iso-8859-1"));
             runTransaction(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         try {
                             reader.readLine(); // skip first line
                             String line = null;
                             while ((line = reader.readLine()) != null) {
                                 if (line.startsWith("#")) {
                                     continue; // skip comments
                                 }
                                 String[] parts = line.split("\\t", -1);
                                 Long refseq = parseLong(parts[0]);
                                 long proj_id = Long.parseLong(parts[1]);
                                 Long ncbi_taxon_id = parseLong(parts[2]);
                                 String organism_name = nullIfEmptyOrMinus(parts[3]);
                                 String super_kingdom = nullIfEmptyOrMinus(parts[4]);
                                 String group = nullIfEmptyOrMinus(parts[5]);
                                 String sequence_status = nullIfEmptyOrMinus(parts[6]);
                                 BigDecimal genome_size = parseDecimalSize(parts[7]);
                                 String gc_content = nullIfEmptyOrMinus(parts[8]);
                                 Boolean gram_stain = "".equals(parts[9]) ? null
                                         : parts[9].charAt(0) == '+'; // + or -
                                 String shape = nullIfEmptyOrMinus(parts[10]);
                                 String arrangement = nullIfEmptyOrMinus(parts[11]);
                                 String endospores = nullIfEmptyOrMinus(parts[12]);
                                 String motility = nullIfEmptyOrMinus(parts[13]);
                                 String salinity = nullIfEmptyOrMinus(parts[14]);
                                 String oxygen_req = nullIfEmptyOrMinus(parts[15]);
                                 String habitat = nullIfEmptyOrMinus(parts[16]);
                                 String temp_range = nullIfEmptyOrMinus(parts[17]);
                                 String optimal_temp = nullIfEmptyOrMinus(parts[18]);
                                 String pathogenic_in = nullIfEmptyOrMinus(parts[19]);
                                 String disease = nullIfEmptyOrMinus(parts[20]);
                                 checkProjectId(proj_id);
                                 String table = namespace
                                         + ".genome_prok";
                                 if (projectIdNotPresent(table, proj_id)) {
                                     getTemplate()
                                             .update("INSERT INTO "
                                                     + table
                                                     + "(refseq, proj_id, ncbi_taxon_id, organism_name, super_kingdom, taxon_group,"
                                                     + "sequence_status, genome_size, gc_content, gram_stain, shape, arrangement,"
                                                     + "endospores, motility, salinity, oxygen_req, habitat, temp_range, optimal_temp,"
                                                     + "pathogenic_in, disease) "
                                                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                     refseq, proj_id,
                                                     ncbi_taxon_id,
                                                     organism_name,
                                                     super_kingdom, group,
                                                     sequence_status,
                                                     genome_size, gc_content,
                                                     gram_stain, shape,
                                                     arrangement, endospores,
                                                     motility, salinity,
                                                     oxygen_req, habitat,
                                                     temp_range, optimal_temp,
                                                     pathogenic_in, disease);
                                 } else {
                                     getTemplate()
                                             .update("UPDATE "
                                                     + table
                                                     + " SET "
                                                     + "refseq=?, ncbi_taxon_id=?, organism_name=?, super_kingdom=?, taxon_group=?, "
                                                     + "sequence_status=?, genome_size=?, gc_content=?, gram_stain=?, shape=?, "
                                                     + "arrangement=?, endospores=?, motility=?, salinity=?, oxygen_req=?, habitat=?, "
                                                     + "temp_range=?, optimal_temp=?, pathogenic_in=?, disease=? WHERE proj_id=?",
                                                     refseq, ncbi_taxon_id,
                                                     organism_name,
                                                     super_kingdom, group,
                                                     sequence_status,
                                                     genome_size, gc_content,
                                                     gram_stain, shape,
                                                     arrangement, endospores,
                                                     motility, salinity,
                                                     oxygen_req, habitat,
                                                     temp_range, optimal_temp,
                                                     pathogenic_in, disease,
                                                     proj_id);
                                 }
                                 // parts[21] and parts[22] are Genbank and
                                 // Genbank RefSeq accession numbers
                                 List<String> accessions = parseAccessionNumbers(parts[21]);
                                 List<String> refseqs = parseAccessionNumbers(parts[22]);
                                 if (refseqs.size() > 0) {
                                     logger.debug(
                                             "Adding {} accessions and {} refseq mappings",
                                             accessions.size(), refseqs.size());
                                 }
                                 updateAccessionMappings(proj_id, accessions,
                                         refseqs);
                             }
                         } finally {
                             reader.close();
                         }
                     } catch (IOException io) {
                         io.printStackTrace();
                     }
                 }
             });
         } catch (UnsupportedEncodingException e) {
             // le sigh
         }
     }
 
     public void updateProkaryotesCompleted(InputStream stream) {
         try {
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(stream, "iso-8859-1"));
             runTransaction(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         try {
                             reader.readLine();
                             String line = null;
                             while ((line = reader.readLine()) != null) {
                                 if (line.startsWith("#")) {
                                     continue;
                                 }
                                 String[] parts = line.split("\\t", -1);
                                 long proj_id = Long.parseLong(parts[1]);
                                 Long chromosome_size = parseLong(parts[8]);
                                 Long plasmid_number = parseLong(parts[9]);
                                 Date date_released = parseDate(parts[10]);
                                 Date date_modified = parseDateTime(parts[11]);
                                 checkProjectId(proj_id);
                                 if (projectIdNotPresent(namespace
                                         + ".genome_prok_complete", proj_id)) {
                                     insertIntoGenomeProkComplete(proj_id, chromosome_size, plasmid_number, date_released, date_modified);
                                 } else {
                                     updateGenomeProkComplete(proj_id, chromosome_size, plasmid_number, date_released, date_modified);
                                 }
                                 updateAccessionMappings(proj_id,
                                         parseAccessionNumbers(parts[12]),
                                         parseAccessionNumbers(parts[13]));
                             }
                             removeOldEntries(namespace
                                     + ".genome_prok_complete");
                         } finally {
                             reader.close();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             });
         } catch (UnsupportedEncodingException e) {
             // whatever
         }
     }
 
     public void updateProkaryotesInProgress(InputStream stream) {
         try {
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(stream, "iso-8859-1"));
             runTransaction(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         try {
                             reader.readLine();
                             String line = null;
                             while ((line = reader.readLine()) != null) {
                                 if (line.startsWith("#")) {
                                     continue;
                                 }
                                 String[] parts = line.split("\\t", -1);
                                 long proj_id = Long.parseLong(parts[1]); // TODO: this should fail for this record, but not cause others to fail
                                 String sequence_availability = nullIfEmptyOrMinus(parts[6]);
                                 String refseq_accession = nullIfEmptyOrMinus(parts[7]);
                                 String genbank_accession = nullIfEmptyOrMinus(parts[8]);
                                 Long contig_nr = parseLong(parts[9]);
                                 Long cds_nr = parseLong(parts[10]);
                                 Date date_released = parseDate(parts[13]);
                                 String center_name = parts[14];
                                 String center_url = nullIfEmptyOrMinus(parts[15]);
                                 checkProjectId(proj_id);
                                 if (projectIdNotPresent(namespace
                                         + ".genome_prok_inprogress", proj_id)) {
                                     insertIntoProkaryotesInProgress(proj_id, sequence_availability, refseq_accession, genbank_accession, contig_nr, cds_nr, date_released, center_name, center_url);
                                 } else {
                                     updateProkaryotesInProgress(proj_id, sequence_availability, refseq_accession, genbank_accession, contig_nr, cds_nr, date_released, center_name, center_url);
                                 }
                             }
                             removeOldEntries(namespace
                                     + ".genome_prok_inprogress");
                         } finally {
                             reader.close();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             });
         } catch (UnsupportedEncodingException e) {
             // get a different JVM
         }
     }
 
     public void updateEukaryotes(InputStream stream) {
         try {
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(stream, "iso-8859-1"));
             runTransaction(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         try {
                             reader.readLine();
                             String line = null;
                             while ((line = reader.readLine()) != null) {
                                 if (line.startsWith("#")) {
                                     continue;
                                 }
                                 String[] parts = line.split("\\t", -1);
                                 long proj_id = Long.parseLong(parts[0]);
                                 String organism_name = parts[1];
                                 String organism_group = nullIfEmptyOrMinus(parts[2]);
                                 String organism_subgroup = nullIfEmptyOrMinus(parts[3]);
                                 Long ncbi_taxon_id = parseLong(parts[4]);
                                 BigDecimal genome_size = parseDecimalSize(parts[5]);
                                 Integer nr_chromosomes = parseInteger(parts[6]);
                                 String sequence_status = nullIfEmptyOrMinus(parts[7]);
                                 String method = nullIfEmptyOrMinus(parts[8]);
                                 String coverage = nullIfEmptyOrMinus(parts[9]);
                                 Date release_date = parseDate(parts[10]);
                                 List<String> consortia = parseConsortia(parts[11]);
                                 String table = namespace + ".genome_euk";
                                 checkProjectId(proj_id);
                                 if (projectIdNotPresent(table, proj_id)) {
                                     getTemplate()
                                             .update("INSERT INTO "
                                                     + table
                                                     + " (proj_id, organism_name, organism_group, organism_subgroup, "
                                                     + "ncbi_taxon_id, genome_size, nr_chromosomes, sequence_status, "
                                                     + "sequence_method, coverage, release_date )"
                                                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                     proj_id, organism_name,
                                                     organism_group,
                                                     organism_subgroup,
                                                     ncbi_taxon_id, genome_size,
                                                     nr_chromosomes,
                                                     sequence_status, method,
                                                     coverage, release_date);
                                 } else {
                                     getTemplate()
                                             .update("UPDATE "
                                                     + table
                                                     + " SET organism_name=?, organism_group=?, "
                                                     + "organism_subgroup=?, ncbi_taxon_id=?, genome_size=?, "
                                                     + "nr_chromosomes=?, sequence_status=?, sequence_method=?, "
                                                     + "coverage=?, release_date=? WHERE proj_id=?",
                                                     organism_name,
                                                     organism_group,
                                                     organism_subgroup,
                                                     ncbi_taxon_id, genome_size,
                                                     nr_chromosomes,
                                                     sequence_status, method,
                                                     coverage, release_date,
                                                     proj_id);
                                 }
                                 updateConsortia(proj_id, consortia);
                             }
                         } finally {
                             reader.close();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             });
         } catch (UnsupportedEncodingException e) {
             // running out of witty comments
         }
     }
 
     public void updateEnvironmentals(InputStream stream) {
         try {
             final BufferedReader reader = new BufferedReader(
                     new InputStreamReader(stream, "iso-8859-1"));
             runTransaction(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         try {
                             reader.readLine();
                             String line;
                             while ((line = reader.readLine()) != null) {
                                 if (line.startsWith("#")) {
                                     continue;
                                 }
                                 String[] parts = line.split("\\t", -1);
                                 Long parent_proj_id = parseLong(parts[0]);
                                 long proj_id = Long.parseLong(parts[1]);
                                 String title = nullIfEmptyOrMinus(parts[2]);
                                 Integer metagenome_type = parseInteger(parts[3]);
                                 String metagenome_source = nullIfEmptyOrMinus(parts[4]);
                                 List<String> accessions = parseAccessionNumbers(parts[5]);
                                 Date release_date = parseDate(parts[6]);
                                 List<String> consortia = parseConsortia(parts[7]);
                                 String table = namespace + ".genome_env";
                                 checkProjectId(proj_id);
                                 if (projectIdNotPresent(table, proj_id)) {
                                     getTemplate()
                                             .update("INSERT INTO "
                                                     + table
                                                     + "(proj_id, parent_proj_id, title, metagenome_type, metagenome_source, release_date) "
                                                     + "VALUES (?, ?, ?, ?, ?, ?)",
                                                     proj_id, parent_proj_id,
                                                     title, metagenome_type,
                                                     metagenome_source,
                                                     release_date);
                                 } else {
                                     getTemplate()
                                             .update("UPDATE "
                                                     + table
                                                     + " SET "
                                                     + "parent_proj_id=?, title=?, metagenome_type=?, "
                                                     + "metagenome_source=?, release_date=?"
                                                     + "WHERE proj_id=?",
                                                     parent_proj_id, title,
                                                     metagenome_type,
                                                     metagenome_source,
                                                     release_date, proj_id);
                                 }
                                 updateConsortia(proj_id, consortia);
                                 updateAccessionMappings(proj_id, accessions,
                                         Collections.<String> emptyList());
                             }
                         } finally {
                             reader.close();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             });
         } catch (UnsupportedEncodingException e) {
             // not very likely
         }
     }
 
     protected void removeOldEntries(String table) {
         int numrows = getTemplate().update(
                 "DELETE FROM " + table + " WHERE UPDATED_AT < "
                         + "(SELECT MAX(UPDATED_AT)-1 FROM " + table + ")");
         logger.debug("{} old rows deleted from {}", numrows, table);
     }
 
     private void updateAccessions(String table, String accLabel, long projectId, List<String> accessions) {
         String gtable = namespace + "." + table;
         Set<String> curAccessions = new HashSet<String>(
                 getTemplate().query(
                         "SELECT " + accLabel + " FROM " + gtable
                                 + " WHERE proj_id=?",
                         STRING_ROW_MAPPER, projectId));
         Set<String> toAdd = new HashSet<String>(accessions);
         toAdd.removeAll(curAccessions);
         Set<String> toDelete = new HashSet<String>(curAccessions);
         toDelete.removeAll(new HashSet<String>(accessions));
         if (toDelete.size() > 0) {
             logger.debug("Removing {} mappings for project ID {}", toDelete.size(), projectId);
             MapSqlParameterSource parameters = new MapSqlParameterSource();
             Set<String> deleteIds = new HashSet<String>();
             for (String accession : toDelete) {
                 deleteIds.add(accession);
             }
             parameters.addValue("ids", deleteIds);
             parameters.addValue("proj_id", projectId);
             getTemplate()
                     .update("DELETE FROM "
                             + gtable
                             + " WHERE proj_id=:proj_id AND " + accLabel + " IN (:ids)",
                             parameters);
         }
         if (toAdd.size() > 0) {
             List<Object[]> batch = new ArrayList<Object[]>(toAdd.size());
             for (String accession : toAdd) {
                 batch.add(new Object[] { projectId, accession });
             }
             getTemplate().batchUpdate(
                     insertAccessionMapping(gtable, accLabel), batch);
         }
     }
 
     private void updateAccessionMappings(long projectId,
                                          List<String> accessions, List<String> refseqs) {
         if (accessions.size() > 0) {
             updateAccessions("GENOME_PROJ_ACC", "ACCESSION_NUMBER", projectId, accessions);
         }
         if (refseqs.size() > 0) {
             updateAccessions("GENOME_PROJ_REFSEQ", "REFSEQ_ACC", projectId, refseqs);
         }
     }
 
     private void updateConsortia(long projectId, List<String> consortia) {
         String table = namespace + ".projects_labs";
         Set<String> curConsortia = new HashSet<String>(getTemplate().query(
                 "select LAB_NAME from " + table + " where proj_id=?",
                 STRING_ROW_MAPPER, projectId));
         Set<String> toAdd = new HashSet<String>(consortia);
         /*
            * System.out.println("Printing current"); printElements(curConsortia);
            * System.out.println("Printing passed in"); printElements(toAdd);
            */
         toAdd.removeAll(curConsortia);
         /*
            * System.out.println("After removal of current:");
            * printElements(toAdd);
            */
         List<Object[]> values = new ArrayList<Object[]>(toAdd.size());
         for (String consortium : toAdd) {
             values.add(new Object[] { projectId, consortium });
         }
         getTemplate().batchUpdate(insertProjectLab(table), values);
     }
 
     private List<String> parseAccessionNumbers(String str) {
         List<String> numberList = Collections.emptyList();
         if (!containsNoData(str)) {
             String[] genbankNumbers = str.split(",");
             numberList = new ArrayList<String>(genbankNumbers.length);
             for (String nr : genbankNumbers) {
                 numberList.add(nr.trim());
             }
         }
         return numberList;
     }
 
     private Set<Long> fetchProjectIds() {
         return new HashSet<Long>(getTemplate().query(
                 "SELECT id FROM " + namespace + ".genome_projects",
                 new RowMapper<Long>() {
                     @Override
                     public Long mapRow(ResultSet rs, int rowNum)
                             throws SQLException {
                         return rs.getLong(1);
                     }
                 }));
     }
 
     private void addProjectId(long projectId) {
         getTemplate().update(
                 "INSERT INTO " + namespace
                         + ".genome_projects (id) VALUES (?)", projectId);
         // will throw an exception if anything is wrong, must be handled further
         // up
         this.projectIds.add(projectId);
     }
 
     private void checkProjectId(long projectId) {
         logger.debug("Checking availability of projectId {}", projectId);
         if (projectIds == null) {
             this.projectIds = fetchProjectIds();
         }
         if (!this.projectIds.contains(projectId)) {
             logger.debug("Adding projectId {}", projectId);
             addProjectId(projectId);
         }
     }
 
     private boolean projectIdNotPresent(String table, long projectId) {
         return getTemplate()
                 .queryForInt(
                         "SELECT COUNT(*) FROM " + table + " WHERE proj_id=?",
                         projectId) == 0;
     }
 }
