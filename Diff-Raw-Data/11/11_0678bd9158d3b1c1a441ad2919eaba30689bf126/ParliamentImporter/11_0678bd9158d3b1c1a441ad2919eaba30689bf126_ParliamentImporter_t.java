 package be.noselus.scraping;
 
 import be.noselus.NosElusModule;
import be.noselus.db.DatabaseHelper;
 import be.noselus.repository.QuestionRepository;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.inject.Inject;
 import java.io.IOException;
 import java.sql.SQLException;
 
 public class ParliamentImporter {
 
     private static final Logger logger = LoggerFactory.getLogger(ParliamentImporter.class);
 
     public static final int WAIT_INTERVAL = 150;
     public static final int FROM_ID = 50061;
     public static final int TO_ID = 51035;
 
     @Inject
     public ParliamentImporter(final QuestionRepository questionRepository, final QuestionParser parser) {
         this.questionRepository = questionRepository;
         this.parser = parser;
     }
 
     public static void main(String[] args) throws SQLException, ClassNotFoundException, InterruptedException {
         Injector injector = Guice.createInjector(new NosElusModule());
        final DatabaseHelper dbHelper = injector.getInstance(DatabaseHelper.class);
        dbHelper.start();
         ParliamentImporter instance = injector.getInstance(ParliamentImporter.class);
         instance.updateRepository();
     }
 
     private final QuestionRepository questionRepository;
     private final QuestionParser parser;
 
     private void updateRepository() {
         //TODO find last question inserted and compute range based on it's assembly_ref
         importQuestions(FROM_ID, TO_ID);
     }
 
     private void importQuestions(int fromId, int toId) {
 
         String url = "http://parlement.wallonie.be/content/print_container.php?print=quest_rep_voir.php&type=all&id_doc=";
 
         for (int id = fromId; id < toId; id++) {
             try {
                 logger.debug("importing question " + id);
                 questionRepository.insertOrUpdateQuestion(parser.parse(id));
             } catch (IOException | IllegalArgumentException | IndexOutOfBoundsException e) {
                 logger.error(url + id, e);
             }
             try {
                 Thread.sleep(WAIT_INTERVAL);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
         }
 
     }
 
 }
