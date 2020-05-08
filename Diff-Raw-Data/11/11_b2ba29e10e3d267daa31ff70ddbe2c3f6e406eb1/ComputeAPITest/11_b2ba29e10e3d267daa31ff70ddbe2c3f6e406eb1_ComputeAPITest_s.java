 package net.zzonsang.cloud.jclouds;
 
 import static org.junit.Assert.*;
 
 import java.util.Set;
 
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.ComputeMetadata;
 import org.jclouds.compute.domain.Hardware;
 import org.jclouds.compute.domain.Image;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.OsFamily;
 import org.jclouds.compute.domain.Template;
 import org.jclouds.domain.Location;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ComputeAPITest {
 
 	/**
 	 * CloudStack 2.x
 	 */
 //	private static final String ENDPOINT = "http://192.168.70.100:8080/client/api";
 //	private static final String API_KEY = "TwvwiLBv56t7cowtj4LP6X9EMsJHDjeX8Ps6rtroRH9nXCIa0T_His_kJKzfTHDRAsfuWDmZ9B6Hwu0nLNXIzg";
 //	private static final String SECRET_KEY = "jthFiw-bottzvOJMamhSF-vWuc7WB3OzwOq3Ws4KAt4R2Qz1kc0u7WfJcbt6XIc9vPmS8r_rZ7ew_MYubsx2ew";
 //	private static final String HARDWARE_ID = "2";
 	
 	/**
 	 * CloudStack 3.x
 	 */
 	private static final String ENDPOINT = "http://192.168.70.101:8080/client/api";
 	private static final String API_KEY = "0vMUvC391sLZ17ghESlHGMbVpHkTFDDWW3a6xJPRrUKizY7OUs4Tcg54c9rpcGYz28GdRvL5sAjouzU5W_mDPw";
 	private static final String SECRET_KEY = "28IAegR65_rM4R7AqSEMfA71gAxqa9sE54R_IjmvQuh1KfTUIS0HEO5gpvXE6HsnNF4O-wLlnPKUqfwVUX55Xg";
 	private static final String HARDWARE_ID = "253bcf0e-dfac-46a1-81c2-0308c3579253";
 	
 	/**
 	 * 
 	 */
 	private static ComputeService computeService;
 	
 	@Before
 	public void before() {
 		ComputeServiceContext context = ContextBuilder.newBuilder("cloudstack")
 				.endpoint(ENDPOINT)
 				.credentials(API_KEY, SECRET_KEY)
 				.build(ComputeServiceContext.class);
 		computeService = context.getComputeService();
 	}
 	
 	/**
 	 * List of the Nodes 
 	 */
	@Test
 	public void listNodes() {
 		System.out.println("### listNodes ###");
 
 		Set<? extends ComputeMetadata> listNodes = computeService.listNodes();
 		for ( ComputeMetadata node : listNodes ) {
 			System.out.println(node.toString());
 		}
 		
 		System.out.println("");
 	}
 	
 	/**
 	 * List of the Locations
 	 */
 	@Test
 	public void listLocations() {
 		System.out.println("### listLocation ###");
 		
 		Set<? extends Location> listAssignableLocations = computeService.listAssignableLocations();
 		for ( Location location : listAssignableLocations ) {
 			System.out.println(location.toString());
 		}
 		
 		System.out.println("");
 	}
 	
 	/**
 	 * List of the Hardware
 	 */
	@Test
 	public void listHardware() {
 		System.out.println("### listHardware ###");
 		
 		Set<? extends Hardware> listHardwareProfiles = computeService.listHardwareProfiles();
 		for ( Hardware hardware : listHardwareProfiles ) {
 			System.out.println(hardware.toString());
 		}
 		
 		System.out.println("");
 	}
 	
 	/**
 	 * List of the Images
 	 */
	@Test
 	public void listImages() {
 		System.out.println("### listImages ###");
 		
 		Set<? extends Image> listImages = computeService.listImages();
 		for( Image image : listImages ) {
 			System.out.println(image.toString());
 		}
 		
 		System.out.println("");
 	}
 	
 	/**
 	 * Create node in group
 	 */
	@Test
 	public void createNodeInGroup() {
 		System.out.println("### createNodeInGroup ###");
 		
 		Template template = computeService.templateBuilder().hardwareId(HARDWARE_ID)
 //				.osVersionMatches("5.6")
 				.imageDescriptionMatches("CentOS")
 				.osFamily(OsFamily.CENTOS).os64Bit(true).build();
 		
 		try {
 			Set<? extends NodeMetadata> createNodesInGroup = computeService.createNodesInGroup("jclouds", 1, template);
 		} catch (RunNodesException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("");
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
