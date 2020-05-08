 package us.jubat.clustering;
 
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.msgpack.rpc.Client;
 
 import us.jubat.common.Datum;
 import us.jubat.testutil.JubaServer;
 import us.jubat.testutil.JubatusClientTest;
 
 public class ClusteringClientTest extends JubatusClientTest {
 	private ClusteringClient client;
 
 	public ClusteringClientTest() {
 		super(JubaServer.clustering);
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		server.start(server.getConfigPath());
 		client = new ClusteringClient(server.getHost(), server.getPort(), NAME,
 				TIMEOUT_SEC);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		server.stop();
 	}
 
 	@Test
 	public void testGet_config() throws IOException {
 		String config = client.getConfig();
 		assertThat(formatAsJson(config),
 				is(formatAsJson(server.getConfigData())));
 	}
 
 	@Test
	public void testSave_and_Load() {
 		String id = "clustering.test_java-client.model";
 		assertThat(client.save(id), is(true));
 		assertThat(client.load(id), is(true));
 	}
 
 	@Test
 	public void testGet_status() {
 		Map<String, Map<String, String>> status = client.getStatus();
 		assertThat(status, is(notNullValue()));
 		assertThat(status.size(), is(1));
 	}
 
 	@Test
 	public void testGet_client() {
 		assertThat(client.getClient(), is(instanceOf(Client.class)));
 		assertThat(client.getClient(), is(notNullValue()));
 	}
 
   @Test
   public void testPush() {
     List<Datum> data_list = new ArrayList<Datum>();
 		data_list.add(generateDatum());
     assertThat(client.push(data_list), is(true));
   }
 
   @Test
   public void testGet_revision() {
       assertThat(client.getRevision(), is(instanceOf(Integer.class)));
   }
 
   @Test
   public void testGet_core_members() {
     List<Datum> data_list = new ArrayList<Datum>();
 		data_list.add(generateDatum());
     client.push(data_list);
     List<List<WeightedDatum>> members = client.getCoreMembers();
     assertThat(members.size(), is(10));
     assertThat(members.get(0).size(), is(1));
     assertThat(members.get(0).get(0), instanceOf(WeightedDatum.class));
   }
 
   @Test
   public void testGet_k_center() {
     for (int i = 0; i < 100; i++) {
       List<Datum> data_list = new ArrayList<Datum>();
       data_list.add(generateDatum());
       client.push(data_list);
     }
     List<Datum> centers = client.getKCenter();
     assertThat(centers.size(), is(10));
     assertThat(centers.get(0), instanceOf(Datum.class));
   }
 
   @Test
   public void testGet_nearest_center() {
     for (int i = 0; i < 100; i++) {
       List<Datum> data_list = new ArrayList<Datum>();
       data_list.add(generateDatum());
       client.push(data_list);
     }
     Datum center = client.getNearestCenter(generateDatum());
     assertThat(center, instanceOf(Datum.class));
   }
 
   @Test
   public void testGet_nearest_members() {
     List<Datum> data_list = new ArrayList<Datum>();
     data_list.add(generateDatum());
     client.push(data_list);
     List<WeightedDatum> members = client.getNearestMembers(generateDatum());
     assertThat(members.get(0), instanceOf(WeightedDatum.class));
   }
 
 	private Datum generateDatum() {
 		Datum datum = new Datum();
 
 		for (int i = 1; i <= 10; i++) {
 			datum.addString("key/str" + Integer.toString(i), "val/str"
 					+ Integer.toString(i));
 		}
 
 		for (int i = 1; i <= 10; i++) {
 			datum.addNumber("key/num" + Integer.toString(i), i);
 		}
 
 		return datum;
 	}

 }
