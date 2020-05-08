 package earth.xor.rest;
 
 import static org.mockito.Matchers.anyInt;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import spark.Spark;
import earth.xor.db.DatastoreFacade;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ Spark.class })
 public class TestSparkRestApi {
 
     @Mock
    DatastoreFacade facade;
 
     private SparkRestApi restApi;
 
     @Before
     public void setUp() {
        restApi = new SparkRestApi(facade);
 
         PowerMockito.mockStatic(Spark.class);
        restApi.startApi();
     }
 
     @Test
     public void portIsSet() {
         PowerMockito.verifyStatic();
         Spark.setPort(anyInt());
     }
 }
