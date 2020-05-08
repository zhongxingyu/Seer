 package map.ui.cmd.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.BeforeClass;
import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.oschrenk.math.intervals.LinearIntervalMap;
 import com.oschrenk.math.probability.IntervalProbability;
 
 public class IntervalPropabilityDeserializerTest {
 
 	private static Gson gson;
 
 	private LinearIntervalMap<Double, Double> intervalProbability;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		final GsonBuilder gsonBuilder = new GsonBuilder();
 		gsonBuilder.registerTypeAdapter(IntervalProbability.class, new IntervalPropabilityDeserializer());
 		gson = gsonBuilder.create();
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		final URL url = this.getClass().getResource("/intervals.json");
 		final String json = FileUtils.readFileToString(new File(url.getFile()));
 
 		intervalProbability = gson.fromJson(json, LinearIntervalMap.class);
 	}
 
	@Test
 	public void testDeserialize() throws IOException {
 
 	}
 
 }
