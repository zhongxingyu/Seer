 package amp.tests.integration;
 
 public class Config {
 
 	public class Bus{
 		public final static String All ="src/test/resources/config/AllBussesConfig.xml";
 	}
 
 	public class Authorization{
 		public final static String Basic ="src/test/resources/config/authorization/BasicAuthRabbitConfig.xml";
 		public final static String AnubisBasic ="src/test/resources/config/authorization/AnubisAndBasicAuthRabbitConfig.xml";
		public final static String AnubisTwoWaySsl ="src/test/resources/config/authorization/AnubisAndTwoWaySSLRabbitConfig.xml";
 	}
 
 	public class Topology{
 		public final static String Simple ="src/test/resources/config/topology/SimpleTopologyConfig.xml";
 		public final static String Gts ="src/test/resources/config/topology/GtsConfig.xml";
 	}
 }
