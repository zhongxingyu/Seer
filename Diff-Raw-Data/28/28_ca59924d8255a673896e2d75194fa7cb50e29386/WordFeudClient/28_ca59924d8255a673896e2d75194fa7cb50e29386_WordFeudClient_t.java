 package nu.mrpi.wordfeudapi;
 
 import nu.mrpi.util.SHA1;
 import nu.mrpi.wordfeudapi.domain.*;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
import sun.misc.BASE64Encoder;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static nu.mrpi.util.MathUtil.random;
 
 /**
  * @author Pierre Ingmansson
  */
 public class WordFeudClient {
 
     public static final String CONTENT_TYPE_JSON = "application/json";
     private String sessionId;
 
     private User loggedInUser = null;
     private final HttpClient httpClient;
     private static final Pattern SESSION_ID_COOKIE_PATTERN = Pattern.compile("sessionid=(.*?);");
 
     public WordFeudClient() {
         httpClient = createHttpClient();
     }
 
     private HttpClient createHttpClient() {
         final HttpClient httpClient = new DefaultHttpClient();
 
         final String proxyHost = System.getProperty("proxy.host");
         final String proxyPort = System.getProperty("proxy.port");
         if (proxyHost != null) {
             final HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
             httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
         }
 
         return httpClient;
     }
 
     public void useSessionId(final String sessionId) {
         this.sessionId = sessionId;
         this.loggedInUser = new User();
         loggedInUser.setSessionId(sessionId);
     }
 
     public User logon(final String email, final String password) {
         final String path = "/user/login/email/";
 
         final HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("email", email);
         parameters.put("password", encodePassword(password));
 
         final JSONObject json = callAPI(path, toJSON(parameters));
 
         try {
             loggedInUser = User.fromJson(json.getString("content"));
             loggedInUser.setSessionId(sessionId);
 
             return loggedInUser;
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     /**
      * Invite somebody to a game
      *
      * @param username  The user to invite
      * @param ruleset   The ruleset to use for the game
      * @param boardType The board type
      * @return The WordFeud API response
      */
     public String invite(final String username, final RuleSet ruleset, final BoardType boardType) {
         final String path = "/invite/new/";
 
         final HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("invitee", username);
         parameters.put("ruleset", ruleset.getApiIntRepresentation());
         parameters.put("board_type", boardType.getApiStringRepresentation());
 
         return callAPI(path, toJSON(parameters)).toString();
         // Errors can be: 'duplicate_invite', 'invalid_ruleset', 'invalid_board_type', 'user_not_found'
     }
 
     /**
      * Accept an invite
      *
      * @param inviteId The invite ID
      * @return The id of the game that just started
      */
     public int acceptInvite(final int inviteId) {
         // 'access_denied'
         final String path = "/invite/" + inviteId + "/accept/";
 
         try {
             return callAPI(path).getJSONObject("content").getInt("id");
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     /**
      * Reject an invite
      *
      * @param inviteId The invite ID
      * @return The WordFeud API response
      */
     public String rejectInvite(final int inviteId) {
         final String path = "/invite/" + inviteId + "/reject/";
 
         return callAPI(path).toString();
     }
 
     /**
      * Get the pending notifications of the current user
      *
      * @return The WordFeud API response
      */
     public Notifications getNotifications() {
         final String path = "/user/notifications/";
 
         final JSONObject json = callAPI(path);
         try {
             return Notifications.fromJson(json.getString("content"));
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     public Game[] getGames() {
         final String path = "/user/games/";
 
         final JSONObject json = callAPI(path);
         try {
             return Game.fromJsonArray(json.getJSONObject("content").getString("games"), loggedInUser);
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     public Game getGame(final long gameId) {
         final String path = "/game/" + gameId + "/";
 
         final JSONObject json = callAPI(path);
         try {
             return Game.fromJson(json.getJSONObject("content").getString("game"), loggedInUser);
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
 
     /**
      * Get the board for a given game
      *
      * @param game The game to find the board for
      * @return The board
      */
     public Board getBoard(final Game game) {
         return getBoard(game.getBoard());
     }
 
     /**
      * Get a specific board
      *
      * @param boardId The id of the board to get
      * @return The WordFeud API response
      */
     public Board getBoard(final int boardId) {
         final String path = "/board/" + boardId + "/";
 
         final JSONObject json = callAPI(path);
         try {
             return Board.fromJson(json.getString("content"));
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     /**
      * Get the status of the current user
      *
      * @return The status
      */
     public Status getStatus() {
         final String path = "/user/status/";
 
         final JSONObject json = callAPI(path);
         try {
             return Status.fromJson(json.getString("content"));
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     /**
      * Place a solution for the given game
      *
      * @param game     The game to place solution for
      * @param solution The solution to place
      * @return The placement result
      */
     public PlaceResult place(final Game game, final Solution solution) {
         return place(game.getId(), game.getRuleset(), solution.getTiles(), solution.getWord().toCharArray());
     }
 
     /**
      * Place a word on the board.
      *
      * @param gameId  The ID of the game to place the word on
      * @param ruleset The ruleset the game is using
      * @param tiles   The tiles to place (only the tiles to be placed = tiles from the users rack)
      * @param word    The whole word to place (including tiles already on the board)
      * @return The placement result
      */
     public PlaceResult place(final long gameId, final RuleSet ruleset, final Tile[] tiles, final char[] word) {
         final String path = "/game/" + gameId + "/move/";
 
         final HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("move", Tile.convert(tiles));
         parameters.put("ruleset", ruleset.getApiIntRepresentation());
         parameters.put("word", word);
 
         final JSONObject json = callAPI(path, toJSON(parameters));
         try {
             return PlaceResult.fromJson(json.getString("content"));
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
     /**
      * Pass a game
      *
      * @param game The game to pass
      * @return The WordFeud API response
      */
     public String pass(final Game game) {
         return pass(game.getId());
     }
 
     /**
      * Pass a game
      *
      * @param gameId The id of the game
      * @return The WordFeud API response
      */
     public String pass(final long gameId) {
         final String path = "/game/" + gameId + "/pass/";
 
         return callAPI(path).toString();
     }
 
     /**
      * Swap letters in given game
      *
      * @param game  The game to swap tiles for
      * @param tiles The letters to swap
      * @return The result of the swap
      */
     public SwapResult swap(final Game game, final char[] tiles) {
         return swap(game.getId(), tiles);
     }
 
     /**
      * Swap tiles in given game
      *
      * @param gameId The id of the game
      * @param tiles  The tiles to swap
      * @return The result of the swap
      */
     public SwapResult swap(final long gameId, final char[] tiles) {
         final String path = "/game/" + gameId + "/swap/";
 
         final HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("tiles", tiles);
 
         final JSONObject json = callAPI(path, toJSON(parameters));
         try {
             return SwapResult.fromJson(json.getString("content"));
         } catch (JSONException e) {
             throw new RuntimeException("Could not deserialize JSON", e);
         }
     }
 
 
     /**
      * Send a chat message to a game
      *
      * @param game    The game to send chat on
      * @param message The message to send
      * @return The WordFeud API response
      */
     public String chat(final Game game, final String message) {
         return chat(game.getId(), message);
     }
 
     /**
      * Send a chat message to a game
      *
      * @param gameId  The game ID of the game to send chat on
      * @param message The message to send
      * @return The WordFeud API response
      */
     public String chat(final long gameId, final String message) {
         final String path = "/game/" + gameId + "/chat/send/";
 
         final HashMap<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("message", message);
 
         final JSONObject json = callAPI(path, toJSON(parameters));
         return json.toString();
     }
 
     /**
      * Get all chat messages from a specific game
      *
      * @param game The game to fetch chat messages from
      * @return The WordFeud API response
      */
     public String getChatMessages(final Game game) {
         return getChatMessages(game.getId());
     }
 
     /**
      * Get all chat messages from a specific game
      *
      * @param gameId The game ID
      * @return The WordFeud API response
      */
     public String getChatMessages(final long gameId) {
         final String path = "/game/" + gameId + "/chat/";
 
         final JSONObject json = callAPI(path);
         return json.toString();
     }
 
     /**
     * Upload a new avatar
     *
     * @param imageData The image data
     * @return The WordFeud API response
     */
    public String uploadAvatar(byte[] imageData) {
        final String path = "/user/avatar/upload/";

        // TODO Figure out how to generate this image data from an actual image

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("image_data", new BASE64Encoder().encode(imageData));

        return callAPI(path, toJSON(parameters)).toString();
    }

    /**
      * Create a new account
      *
      * @param username The username of the new user
      * @param email    The email of the new user
      * @param password The password of the new user
      * @return The WordFeud API response
      */
     public String createAccount(final String username, final String email, final String password) {
         final String path = "/user/create/";
         final HashMap<String, String> parameters = new HashMap<String, String>();
         parameters.put("username", username);
         parameters.put("email", email);
         parameters.put("password", encodePassword(password));
 
         return callAPI(path, toJSON(parameters)).toString();
     }
 
     private JSONObject callAPI(final String path) {
         return callAPI(path, "");
     }
 
     private JSONObject callAPI(final String path, final String data) {
         try {
             final HttpPost post = createPost(path, data);
 
             if (sessionId != null) {
                 post.addHeader("Cookie", "sessiond=" + sessionId);
             }
 
             final HttpResponse response = httpClient.execute(post);
 
             if (response.getStatusLine().getStatusCode() == 200) {
                 return handleResponse(response);
             } else {
                EntityUtils.consume(response.getEntity());
                 throw new WordFeudException("Got unexpected HTTP " + response.getStatusLine().getStatusCode() + ": " + response.toString());
             }
 
         } catch (IOException e) {
             throw new RuntimeException("Error when contacting WordFeud API", e);
         } catch (JSONException e) {
             throw new RuntimeException("Could not parse JSON", e);
         } finally {
             httpClient.getConnectionManager().closeExpiredConnections();
         }
     }
 
     private JSONObject handleResponse(final HttpResponse response) throws IOException, JSONException {
         checkCookieHeader(response);
 
         final JSONObject jsonObject = extractJsonFromResponse(response);
 
         if (!"success".equals(jsonObject.getString("status"))) {
             throw new WordFeudException("Error when calling API: " + jsonObject.getJSONObject("content").getString("type"));
         }
 
         return jsonObject;
     }
 
     private JSONObject extractJsonFromResponse(final HttpResponse response) throws IOException, JSONException {
        final String responseString = EntityUtils.toString(response.getEntity());
         return new JSONObject(responseString);
     }
 
     private void checkCookieHeader(final HttpResponse response) {
         final Header[] cookies = response.getHeaders("Set-Cookie");
         if (cookies != null && cookies.length > 0) {
             sessionId = extractSessionIdFromCookie(cookies[0]);
         }
     }
 
     private String extractSessionIdFromCookie(final Header cookie) {
         final String cookieValue = cookie.getValue();
         final Matcher matcher = SESSION_ID_COOKIE_PATTERN.matcher(cookieValue);
         if (matcher.find()) {
             return matcher.group(1);
         }
         return null;
     }
 
     private HttpPost createPost(final String path, final String data) throws UnsupportedEncodingException {
         final HttpPost post = new HttpPost("http://" + calculateHostName() + "/wf" + path);
         post.addHeader("Content-Type", CONTENT_TYPE_JSON);
         post.addHeader("Accept", CONTENT_TYPE_JSON);
         final HttpEntity entity = new StringEntity(data, "UTF-8");
         post.setEntity(entity);
         return post;
     }
 
     private String calculateHostName() {
         return "game0" + random(1, 7) + ".wordfeud.com";
     }
 
     private String toJSON(final HashMap<String, ?> parameters) {
         return new JSONObject(parameters).toString();
     }
 
     private String encodePassword(final String password) {
         try {
             return SHA1.sha1(password + "JarJarBinks9");
         } catch (Exception e) {
             throw new RuntimeException("Error when encoding password", e);
         }
     }
 }
