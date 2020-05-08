 package gov.nysenate.billbuzz.scripts;
 
 import gov.nysenate.billbuzz.model.BillBuzzParty;
 import gov.nysenate.billbuzz.model.BillBuzzSenator;
 import gov.nysenate.billbuzz.util.Application;
 import gov.nysenate.billbuzz.util.BillBuzzDAO;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.handlers.BeanHandler;
 import org.apache.http.client.fluent.Request;
 import org.apache.http.client.fluent.Response;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * Checks the OpenLegislation senators listing for the current session for new senators.
  *
  * Each run completely replaces affiliations in case they have changed over time.
  *
  * @author GraylinKim
  *
  */
 public class UpdateSenators extends BaseScript
 {
     private static final Logger logger = Logger.getLogger(UpdateSenators.class);
 
     public static void main(String[] args) throws Exception
     {
         new UpdateSenators().run(args);
     }
 
     public void execute(CommandLine opts) throws IOException, SQLException
     {
         BillBuzzDAO dao = new BillBuzzDAO();
         int session = dao.getSession();
 
         Response response = Request.Get("http://open.nysenate.gov/legislation/senators/"+session+".json").execute();
         JsonNode root = new ObjectMapper().readTree(response.returnContent().asString());
         Iterator<JsonNode> senatorIterator = root.getElements();
 
         while (senatorIterator.hasNext()) {
             JsonNode senatorNode = senatorIterator.next();
 
             Iterator<JsonNode> affiliationIterator = senatorNode.get("partyAffiliations").getElements();
             List<BillBuzzParty> parties = new ArrayList<BillBuzzParty>();
             while (affiliationIterator.hasNext()) {
                 String partyId = affiliationIterator.next().asText().toUpperCase();
                 if (partyId.equals("I") || partyId.equals("IND")) {
                     partyId = "IP";
                 }
                 parties.add(new BillBuzzParty(partyId));
             }
 
             String name = senatorNode.get("name").asText();
             String shortName = senatorNode.get("shortName").asText();
             logger.info("Updating "+name+": "+shortName+"-"+session);
             QueryRunner runner = new QueryRunner(Application.getDB().getDataSource());
             BillBuzzSenator senator = runner.query("SELECT * FROM billbuzz_senator WHERE shortName=? and session=?", new BeanHandler<BillBuzzSenator>(BillBuzzSenator.class), shortName, session);
             if (senator == null) {
                 senator = new BillBuzzSenator(name, shortName, session, parties);
                runner.update("INSERT INTO billbuzz_senator (name, shortName, active, session) VALUES (?, ?, ?, ?)", senator.getName(), senator.getShortName(), senator.isActive(), senator.getSession());
                 senator.setId(dao.lastInsertId(runner));
             }
 
             runner.update("DELETE FROM billbuzz_affiliation WHERE senatorId=?", senator.getId());
             for (BillBuzzParty party : parties) {
                 runner.update("INSERT INTO billbuzz_affiliation (senatorId, partyId) VALUES (?, ?)", senator.getId(), party.getId());
             }
         }
     }
 }
