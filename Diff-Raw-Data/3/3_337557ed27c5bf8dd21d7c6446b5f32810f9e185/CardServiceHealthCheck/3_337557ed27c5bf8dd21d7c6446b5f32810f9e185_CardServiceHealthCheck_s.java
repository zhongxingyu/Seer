 package kanbannow.health;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.JsonNodeFactory;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.Environment;
 import com.yammer.metrics.core.HealthCheck;
 import kanbannow.CardServiceConfiguration;
 import kanbannow.core.Card;
import kanbannow.jdbi.CardDAO;
 import net.sf.json.test.JSONAssert;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import org.skife.jdbi.v2.util.LongMapper;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Date;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 
 public class CardServiceHealthCheck extends HealthCheck {
 
 
     private CardServiceConfiguration cardServiceConfiguration;
     private Handle databaseHandle;
     private DBI dbi;
 
 
     public static final String CARD_1_TEXT = "zzzTest card text1zzz";
     public static final String CARD_2_TEXT = "zzzTest card text2zzz";
     public static final String CARD_3_TEXT = "zzzTest card text3zzz";
     public static final String CARD_4_TEXT = "zzzTest card text4zzz";
 
 
 
     public CardServiceHealthCheck(CardServiceConfiguration aCardServiceConfiguration, DBI aDBI) {
         super("cardService");
         this.cardServiceConfiguration = aCardServiceConfiguration;
         this.dbi = aDBI;
     }
 
     // CHECKSTYLE:OFF
     @Override
     protected Result check() throws Exception {
 
         try{
             this.databaseHandle = dbi.open();
 
             cleanupDbData();
             Long boardId1 = 1L;
             Card card1 = createAndInsertPostponedCard(CARD_1_TEXT, "2/2/2101", boardId1);
             Card card2 = createAndInsertPostponedCard(CARD_2_TEXT, "1/1/2095", boardId1);
             Long boardId2 = 2L;
             insertCardIntoBoard(boardId2, CARD_3_TEXT);
             insertCardIntoBoard(boardId2, CARD_4_TEXT);
             HttpResponse httpResponse = callCardService(boardId1);
             assertStatusCodeIs200(httpResponse);
             JsonNode actualJsonResults = getJsonResults(httpResponse);
             ArrayNode expectedJsonResults = createdExpectedJson(card1, card2);
             JSONAssert.assertEquals(expectedJsonResults, actualJsonResults);
             return Result.healthy();
         }
         finally {
             // This is hacky.  Need to switch to using a DAO
             databaseHandle.close();
         }
     }
     // CHECKSTYLE:ON
 
     private JsonNode getJsonResults(HttpResponse httpResponse) throws IOException {
         String result = getStringFromHttpResponse(httpResponse);
         ObjectMapper mapper = new ObjectMapper();
         return mapper.readTree( result );
     }
 
     private String getStringFromHttpResponse(HttpResponse httpResponse) throws IOException {
         InputStream inputStream = httpResponse.getEntity().getContent();
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
         return bufferedReader.readLine();
     }
 
 
     private ArrayNode createdExpectedJson(Card card1, Card card2) {
         JsonNodeFactory factory = JsonNodeFactory.instance;
         ArrayNode expectedCardArrayJson = new ArrayNode(factory);
         // Do card2 first, since it will be sorted as first item
         ObjectNode row = createObjectNodeFromCard(card2, factory);
         expectedCardArrayJson.add(row);
         row = createObjectNodeFromCard(card1, factory);
         expectedCardArrayJson.add(row);
         return expectedCardArrayJson;
     }
 
     private ObjectNode createObjectNodeFromCard(Card card, JsonNodeFactory factory) {
         ObjectNode row = new ObjectNode(factory);
         Long cardIdLong = card.getId();
         row.put("id", cardIdLong.intValue() );
         row.put("cardText", card.getCardText() );
         row.put("postponedDate", card.getPostponedDate() );
         return row;
     }
 
 
 
     private void assertStatusCodeIs200(HttpResponse httpResponse) {
         StatusLine statusLine = httpResponse.getStatusLine();
         int statusCode = statusLine.getStatusCode();
         assertThat(statusCode).isEqualTo(200);
     }
 
 
 
     //            String uri = "http://localhost:9595/cards/board/" + boardId + "?postponed=true";
     private HttpResponse callCardService(Long boardId1) throws IOException, ConfigurationException {
         int port  = cardServiceConfiguration.getHttpConfiguration().getPort();
         HttpClient httpclient = new DefaultHttpClient();
         String uri = "http://localhost:" + port + "/cards/board/" + boardId1;
         HttpGet httpget = new HttpGet(uri);
         return httpclient.execute(httpget);
     }
 
 
     private Long insertCardIntoBoard(Long boardId, String cardText) {
         long cardLocation = 1;
         Long cardId = getNextCardIdFromSequence();
 
         databaseHandle.execute("insert into card (id, text, location, board_id) values (?, ?, ?, ?)", cardId, cardText, cardLocation, boardId);
 
         return cardId;
     }
 
 
     private Card createAndInsertPostponedCard(String text, String postponedDate, Long boardId) {
         Card card1 = new Card();
         card1.setCardText(text);
         card1.setPostponedDate(postponedDate);
         DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
         DateTime postponedDate1 = formatter.parseDateTime(card1.getPostponedDate());
         Long cardId1 = insertPostponedCardIntoBoard(boardId, card1, new Date(postponedDate1.getMillis()));
         card1.setId(cardId1);
         return card1;
     }
 
 
     private Long insertPostponedCardIntoBoard(Long boardId, Card aCard, Date postponedDate) {
         long cardLocation = 1;
         Long cardId = getNextCardIdFromSequence();
 
         databaseHandle.execute("insert into card (id, text, location, board_id, postponed_date) values (?, ?, ?, ?, ?)", cardId, aCard.getCardText(), cardLocation, boardId, postponedDate);
 
         return cardId;
     }
 
 
     private Long getNextCardIdFromSequence() {
         return databaseHandle.createQuery("select CARD_SURROGATE_KEY_SEQUENCE.nextval from dual")
                 .map(LongMapper.FIRST)
                 .first();
     }
 
 
     // CHECKSTYLE:OFF
     private void cleanupDbData() {
 
 
         databaseHandle.execute("delete from card where text ='" + CARD_1_TEXT + "'");
         databaseHandle.execute("delete from card where text ='" + CARD_2_TEXT + "'");
         databaseHandle.execute("delete from card where text ='" + CARD_3_TEXT + "'");
         databaseHandle.execute("delete from card where text ='" + CARD_4_TEXT + "'");
     }
     // CHECKSTYLE:ON
 
 
 }
