 package fr.xebia.xke.cassandra;
 
 import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
 import static fr.xebia.xke.cassandra.JacksonReader.readJsonFile;
 import static java.lang.String.format;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.joda.time.DateTime.now;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import fr.xebia.xke.cassandra.model.Track;
 import fr.xebia.xke.cassandra.model.User;
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.ResultSetFuture;
 import com.datastax.driver.core.Row;
 import com.datastax.driver.core.utils.UUIDs;
 import com.google.common.io.Resources;
 
 public class CassandraRepositoryTest extends AbstractTest {
 
     private static final Logger LOG = Logger.getLogger(CassandraRepositoryTest.class);
 
     private CassandraRepository repository;
 
     @Before
     public void setUp() throws Exception {
         repository = new CassandraRepository(session);
     }
 
     @Test
     public void should_insert_an_user_by_using_a_query_string() throws Exception {
         // Given
         User user = newRandomUser();
 
         // When
         repository.insertUserWithQueryString(user);
 
         // Then
         assertThat(session.execute("SELECT * FROM user WHERE id=?", user.getId()).one()).isNotNull();
     }
 
     @Test
     public void should_insert_an_user_by_using_a_query_builder() throws Exception {
         // Given
         User user = newRandomUser();
 
         // When
         repository.insertUserWithQueryBuilder(user);
 
         // Then
         assertThat(session.execute("SELECT * FROM user WHERE id=?", user.getId()).one()).isNotNull();
     }
 
     @Test
     public void should_find_an_user_by_using_a_query_builder() throws Exception {
         // Given
         User user = newRandomUser();
         repository.insertUserWithQueryString(user);
 
         // When
         repository.findUserWithQueryBuilder(user.getId());
 
         // Then
         assertThat(session.execute("SELECT * FROM user WHERE id=?", user.getId()).one()).isNotNull();
     }
 
     @Test
     public void should_insert_a_track_by_using_a_prepared_statement() throws Exception {
         // Given
         Set<Track> tracks = loadTracks();
 
         // When
         repository.insertTracksWithPreparedStatement(tracks);
 
         // Then
         assertThat(session.execute("SELECT * FROM tracks")).hasSize(tracks.size());
     }
 
     @Test
     public void should_insert_a_click_stream_with_ttl() throws Exception {
         // Given
         User user = newRandomUser();
        int _1_SEC_TTL = 1;
 
         // When
        repository.insertUserClickStreamWithTTL(user.getId(), now().toDate(), "http://cassandra.apache.org/download/", _1_SEC_TTL);
 
         // Then
         assertThat(session.execute("SELECT * FROM user_click_stream").all()).isNotEmpty();
 
        LOG.info("Waiting 3 seconds...");
        TimeUnit.SECONDS.sleep(3);
 
         assertThat(session.execute(select().all().from("user_click_stream")).all()).isEmpty();
     }
 
     @Test
     public void should_find_clicks_stream_within_a_time_frame() throws Exception {
         // Given
         User user = newRandomUser();
         int _1_DAY_TTL = 1440;
 
         repository.insertUserClickStreamWithTTL(user.getId(), now().minusSeconds(15).toDate(), "http://cassandra.apache.org/download/", _1_DAY_TTL);
         repository.insertUserClickStreamWithTTL(user.getId(), now().toDate(), "http://wiki.apache.org/cassandra/GettingStarted", _1_DAY_TTL);
         repository.insertUserClickStreamWithTTL(user.getId(), now().plusSeconds(15).toDate(), "http://wiki.apache.org/cassandra/HowToContribute", _1_DAY_TTL);
 
         // When
         ResultSet rows = repository.findUserClicksStreamWithinTimeFrame(user.getId(), now().minusSeconds(5).toDate(), now().plusSeconds(5).toDate());
 
         // Then
         List<Row> clicks = rows.all();
         assertThat(clicks).hasSize(1);
     }
 
     @Test
     public void should_paginate_user_clicks_stream() throws Exception {
         // Given
         User user = newRandomUser();
         int _1_DAY_TTL = 1440;
         int pageSize = 20;
 
         for (int i = 0; i < 100; i++) {
             repository.insertUserClickStreamWithTTL(user.getId(), now().plusSeconds(i).toDate(), "http://cassandra.apache.org/download/", _1_DAY_TTL);
         }
 
         // When
         ResultSet userClicksStreamByPage = repository.findUserClicksStreamByPage(user.getId(), pageSize);
 
         // Then
         assertThat(userClicksStreamByPage.getAvailableWithoutFetching()).isEqualTo(pageSize);
     }
 
     @Test
     public void should_insert_user_track_likes_asynchronously() throws Exception {
         // Given
         Set<Track> tracks = loadTracks();
         User user = newRandomUser();
 
         // When
         for (Track likeTrack : tracks) {
             ResultSetFuture resultSetFuture = repository.insertUserTrackLikesAsync(user.getId(), likeTrack.getId());
             resultSetFuture.getUninterruptibly();
         }
 
         // Then
        assertThat(session.execute("SELECT * FROM track_likes")).hasSize(tracks.size());
     }
 
     @Test
     public void should_insert_track_likes_by_using_a_batch() throws Exception {
         // Given
         User user = newRandomUser();
         Set<Track> tracks = loadTracks();
 
         // When
         repository.insertUserTrackLikesByBatch(user.getId(), tracks);
 
         // Then
         assertThat(session.execute("SELECT * FROM track_likes").all()).hasSize(tracks.size());
     }
 
     @Test
     public void should_enable_tracing_on_query() throws Exception {
         // Given
         User user = newRandomUser();
 
         // When
         ResultSet resultSet = repository.findUserWithTracing(user.getId());
 
         // Then
         assertThat(resultSet.getExecutionInfo().getQueryTrace()).isNotNull();
         repository.printTrace(resultSet.getExecutionInfo());
     }
 
     ///////// UTILITY METHODS ///////////
     private Set<Track> loadTracks() throws Exception {
         Set<Track> tracks = new HashSet<>();
         URL resourceAsStream = Resources.getResource("data/tracks");
         File directory = new File(resourceAsStream.toURI());
         File[] files = directory.listFiles(new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".json");
             }
         });
         for (File file : files) {
             Track e = readJsonFile(Track.class, file);
             e.setId(UUIDs.timeBased());
             e.setRelease(new Date(System.nanoTime()));
             tracks.add(e);
         }
         return tracks;
     }
 
     private User newRandomUser() throws Exception {
         URL resourceAsStream = Resources.getResource(format("data/user%d.json", new Random().nextInt(5) + 1));
         User user = readJsonFile(User.class, new File(resourceAsStream.toURI()));
         UUID uuid = UUIDs.random();
         user.setId(uuid);
         user.setAge(new Random().nextInt(100));
         return user;
     }
 
 }
