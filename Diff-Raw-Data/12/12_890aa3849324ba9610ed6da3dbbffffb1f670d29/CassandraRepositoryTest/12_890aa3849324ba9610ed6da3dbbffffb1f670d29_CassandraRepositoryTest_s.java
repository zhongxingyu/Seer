 package fr.xebia.xke.cassandra;
 
 import com.datastax.driver.core.ResultSet;
 import com.datastax.driver.core.ResultSetFuture;
 import com.datastax.driver.core.Row;
 import com.datastax.driver.core.querybuilder.QueryBuilder;
 import com.datastax.driver.core.utils.UUIDs;
 import com.google.common.base.Predicate;
 import fr.xebia.xke.cassandra.model.Track;
 import fr.xebia.xke.cassandra.model.User;
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
 import static com.google.common.collect.Sets.filter;
 import static fr.xebia.xke.cassandra.JacksonReader.readJsonFile;
 import static java.lang.String.format;
 import static org.apache.commons.lang.math.RandomUtils.nextInt;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertEquals;
 
 public class CassandraRepositoryTest extends AbstractTest {
 
     private static final Logger LOG = Logger.getLogger(CassandraRepositoryTest.class);
 
     public static final String URL = "http://cassandra.apache.org/download/";
     public static final int _1_SEC_TLL = 1;
     public static final int _10_SEC_TLL = 10;
 
     private CassandraRepository repository;
 
     @Before
     public void setUp() throws Exception {
         repository = new CassandraRepository(session());
     }
 
     @Test
     public void should_write_user_with_statement() throws Exception {
         User user = newRandomUser();
         repository.writeUserWithBoundStatement(user);
         assertThat(session().execute(format("SELECT * FROM user WHERE id=%s", user.getId())).one()).isNotNull();
     }
 
     @Test
     public void should_read_user() throws Exception {
         User user = newRandomUser();
         repository.writeUserWithBoundStatement(user);
         ResultSet rows = repository.readUserWithQueryBuilder(user.getId());
        assertEquals(user.getId(), rows.one().getUUID("id"));
     }
 
     @Test
     public void should_write_track() throws Exception {
         Set<Track> tracks = loadTracks();
         repository.writeTracksWithQueryBuilder(tracks);
         ResultSet rows = session().execute(select().all().from("tracks"));
         assertThat(rows.all()).hasSize(tracks.size());
     }
 
     @Test
     public void should_write_click_stream_with_ttl() throws Exception {
         for (int j = 0; j < 20; j++) {
             User user = newRandomUser();
             repository.writeToClickStreamWithTTL(user.getId(), now().toDate(), URL, _1_SEC_TLL);
         }
         assertThat(session().execute(select().all().from("user_click_stream")).all()).isNotEmpty();
         TimeUnit.SECONDS.sleep(3);
         LOG.info("Waiting 3 seconds...");
         assertThat(session().execute(select().all().from("user_click_stream")).all()).isEmpty();
     }
 
     @Test
     public void should_read_click_stream() throws Exception {
         Set<User> users = loadUsers();
         for (User user : users) {
             int x = nextInt(10) + 1;
             for (int i = 0; i < x; i++) {
                 repository.writeToClickStreamWithTTL(user.getId(), now().toDate(), URL, _10_SEC_TLL);
             }
         }
         for (User user : users) {
             ResultSet rows = repository.readClickStreamByTimeframe(user.getId(), now().minusSeconds(5).toDate(), now().plusSeconds(5).toDate());
             List<Row> clicks = rows.all();
            assertThat(clicks).isNotEmpty();
             LOG.info(format("found %d links into the stream for user '%s'", clicks.size(), user.getName()));
         }
     }
 
     @Test
     public void should_write_likes_asynchronously() throws Exception {
         Set<Track> tracks = loadTracks();
         Set<User> users = loadUsers();
         for (User user : users) {
             Set<Track> likeTracks = filter(tracks, new Predicate<Track>() {
                 @Override
                 public boolean apply(Track track) {
                     return nextInt(10) % 3 == 0;
                 }
             });
             ResultSetFuture resultSetFuture = repository.writeAndReadLikesAsynchronously(user, likeTracks);
             List<Row> rows = resultSetFuture.get().all();
             assertThat(rows).isNotEmpty();
             LOG.info(format("found %d like tracks for user '%s'", rows.size(), user.getName()));
         }
     }
 
     /**
      * Need at least 2 nodes in the cluster to pass this test.
      */
     @Test
     @Ignore
     public void should_batch_write_users() throws Exception {
         List<UUID> writtenIds = new ArrayList<UUID>();
         List<String> queries = new ArrayList<String>();
         for (int i = 0; i < 20; i++) {
             User user = newRandomUser();
             writtenIds.add(user.getId());
             queries.add(QueryBuilder.insertInto("user") //
                     .value("id", user.getId()) //
                     .value("name", user.getName()) //
                     .value("email", user.getEmail()) //
                     .value("age", nextInt(100))//
                     .toString());
         }
         repository.batchWriteUsers(queries);
     }
 
     ///////// UTILITY METHODS ///////////
     private Set<Track> loadTracks() throws Exception {
         Set<Track> tracks = new HashSet<Track>();
         URL resourceAsStream = getClass().getClassLoader().getResource("data/tracks");
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
 
     private Set<User> loadUsers() throws Exception {
         Set<User> users = new HashSet<User>();
         for (int i = 1; i < 6; i++) {
             URL resourceAsStream = getClass().getClassLoader().getResource(format("data/user%d.json", i));
             User user = readJsonFile(User.class, new File(resourceAsStream.toURI()));
             UUID uuid = UUIDs.random();
             user.setId(uuid);
             user.setAge(nextInt(100));
             users.add(user);
         }
         return users;
     }
 
     private User newRandomUser() throws Exception {
         URL resourceAsStream = getClass().getClassLoader().getResource(format("data/user%d.json", nextInt(5) + 1));
         User user = readJsonFile(User.class, new File(resourceAsStream.toURI()));
         UUID uuid = UUIDs.random();
         user.setId(uuid);
         user.setAge(nextInt(100));
         return user;
     }
 
 }
