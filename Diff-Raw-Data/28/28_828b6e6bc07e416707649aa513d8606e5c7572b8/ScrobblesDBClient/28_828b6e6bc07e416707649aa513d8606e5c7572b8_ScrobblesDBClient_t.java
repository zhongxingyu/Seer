 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.db;
 
 import org.gravidence.gravifon.util.SharedInstanceHolder;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import java.io.IOException;
 import java.util.List;
 import javax.ws.rs.client.WebTarget;
 import org.gravidence.gravifon.db.domain.ScrobbleDocument;
 import org.gravidence.gravifon.exception.JsonException;
 import org.gravidence.gravifon.util.DateTimeUtils;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * CouchDB JAX-RS client to <code>/scrobbles</code> database.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public class ScrobblesDBClient extends BasicDBClient<ScrobbleDocument> implements InitializingBean {
     
     private static final Logger LOGGER = LoggerFactory.getLogger(ScrobblesDBClient.class);
     
     /**
      * JAX-RS client target associated with <code>main/all_scrobbles</code> view.
      */
     private WebTarget viewMainAllScrobblesTarget;
    
    /**
     * JAX-RS client target associated with <code>main/all_user_scrobbles</code> view.
     */
    private WebTarget viewMainAllUserScrobblesTarget;
    
    /**
     * JAX-RS client target associated with <code>main/all_track_scrobbles_amount</code> view.
     */
    private WebTarget viewMainAllTrackScrobblesAmountTarget;
    
    /**
     * JAX-RS client target associated with <code>main/all_track_scrobbles_duration</code> view.
     */
    private WebTarget viewMainAllTrackScrobblesDurationTarget;
 
     @Override
     public void afterPropertiesSet() throws Exception {
         setDbTarget(getDbClient().getTarget()
                 .path("scrobbles"));
         
         viewMainAllScrobblesTarget = ViewUtils.getViewTarget(
                 getDbTarget(), "main", "all_scrobbles");
        viewMainAllUserScrobblesTarget = ViewUtils.getViewTarget(
                getDbTarget(), "main", "all_user_scrobbles");
        viewMainAllTrackScrobblesAmountTarget = ViewUtils.getViewTarget(
                getDbTarget(), "main", "all_track_scrobbles_amount");
        viewMainAllTrackScrobblesDurationTarget = ViewUtils.getViewTarget(
                getDbTarget(), "main", "all_track_scrobbles_duration");
     }
     
     /**
      * Retrieves scrobble amount.<p>
      * Scrobble amount is equal to <code>main/all_scrobbles</code> view size.
      * 
      * @return scrobble amount
      * 
      * @see #viewMainAllScrobblesTarget
      * @see ViewQueryExecutor#querySize(javax.ws.rs.client.WebTarget)
      */
     public long retrieveScrobbleAmount() {
         return ViewQueryExecutor.querySize(viewMainAllScrobblesTarget);
     }
     
     /**
      * Retrieves existing scrobble {@link ScrobbleDocument document}.
      * 
      * @param id scrobble identifier
      * @return scrobble details document if found, <code>null</code> otherwise
      */
     public ScrobbleDocument retrieveScrobbleByID(String id) {
         return retrieve(id, ScrobbleDocument.class);
     }
     
     /**
      * Retrieves scrobbles by complete key.
      * 
      * @param userId user identifier
      * @param scrobbleStartDatetime scrobble event start datetime
      * @return list of scrobble details documents
      */
     public List<ScrobbleDocument> retrieveScrobblesByKey(String userId, DateTime scrobbleStartDatetime) {
         ArrayNode key = SharedInstanceHolder.OBJECT_MAPPER.getNodeFactory().arrayNode();
         key.add(userId);
         key.add(SharedInstanceHolder.OBJECT_MAPPER.valueToTree(DateTimeUtils.dateTimeToArray(scrobbleStartDatetime)));
         
        return retrieveByKey(viewMainAllUserScrobblesTarget, key, ScrobbleDocument.class);
     }
     
     /**
      * Retrieves a number of scrobbles that belong to user and match applied optional filter params
      * (page [particular scrobble to start from] and/or date range).
      * 
      * @param userId user identifier
      * @param scrobbleStartDatetime scrobble datetime to start from
      * @param start opening bound of date range
      * @param end closing bound of date range
      * @param ascending retrieve direction
      * @param limit max number of scrobbles to retrieve
      * @return list of scrobble details documents
      */
     public List<ScrobbleDocument> retrieveScrobblesByUserID(String userId, String scrobbleStartDatetime,
             DateTime start, DateTime end, boolean ascending, Long limit) {
         ArrayNode key = SharedInstanceHolder.OBJECT_MAPPER.getNodeFactory().arrayNode();
         key.add(userId);
         
         JsonNode scrobbleStart;
         if (scrobbleStartDatetime != null) {
             try {
                 scrobbleStart = SharedInstanceHolder.OBJECT_MAPPER.readTree(scrobbleStartDatetime);
             }
             catch (IOException ex) {
                 throw new JsonException(ex);
             }
         }
         else {
             scrobbleStart = null;
         }
         
         JsonNode subKeyStart;
         if (ascending && scrobbleStart != null) {
             subKeyStart = scrobbleStart;
         }
         else if (start != null) {
             subKeyStart = SharedInstanceHolder.OBJECT_MAPPER.valueToTree(DateTimeUtils.dateTimeToArray(start));
         }
         else {
             subKeyStart = null;
         }
         
         JsonNode subKeyEnd;
         if (!ascending && scrobbleStart != null) {
             subKeyEnd = scrobbleStart;
         }
         else if (end != null) {
             subKeyEnd = SharedInstanceHolder.OBJECT_MAPPER.valueToTree(DateTimeUtils.dateTimeToArray(end));
         }
         else {
             subKeyEnd = null;
         }
         
        return retrievePage(viewMainAllUserScrobblesTarget, key, subKeyStart, subKeyEnd, ascending, limit,
                 ScrobbleDocument.class);
     }
     
 }
