 package agent.test;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import factory.KitConfig;
 import factory.PartType;
 
 import junit.framework.TestCase;
 import DeviceGraphics.NestGraphics;
 import agent.CameraAgent;
 import agent.CameraAgent.NestStatus;
 import agent.NestAgent;
 import agent.PartsRobotAgent;
 import agent.PartsRobotAgent.MyKitStatus;
 import agent.data.Kit;
 import agent.data.Part;
 
 /**
  * This tests the Parts Robot and Camera in the normative scenario. The UUT is
  * the interaction between the Nest, Camera and Parts Robot. The camera captures
  * a pair of full nests and sends the partsrobot a list of good parts. Parts
  * Robot then finishes filling a kit and the camera is asked to inspect the kit.
  * @author Daniel Paje
  */
 public class V0_JUnit_PartsRobotAgent_CameraAgent_Test_NormativeScenario extends
 		TestCase {
 	protected NestAgent nest;
 	protected NestAgent nest2;
 	protected PartsRobotAgent partsrobot;
 	protected CameraAgent camera;
 
 	protected Date date;
 
 	private final URL URL = V0_JUnit_PartsRobotAgent_CameraAgent_Test_NormativeScenario.class
 			.getResource(".");
 	private final String FILEPATH = URL.toString().replace("file:", "");
 
 	@Override
 	protected void setUp() {
 		nest = new NestAgent("nest");
 		nest2 = new NestAgent("nest2");
 		partsrobot = new PartsRobotAgent("partsrobot");
 		camera = new CameraAgent("camera");
 
 		camera.setNest(nest);
 		camera.setNest(nest2);
 		for (int i = 0; i < 6; i++) {
 			camera.setNest(new NestAgent("nest" + i));
 		}
 		camera.setPartsRobot(partsrobot);
 
 		nest.setCamera(camera);
 		nest2.setCamera(camera);
 		nest.setGraphicalRepresentation(new NestGraphics(null, 1, nest));
 		nest2.setGraphicalRepresentation(new NestGraphics(null, 2, nest2));
 		date = new Date();
 	}
 
 	@Override
 	protected void tearDown() {
 		nest = null;
 		nest2 = null;
 		partsrobot = null;
 		camera = null;
 		date = null;
 	}
 
 	public void testNormativeScenario() throws InterruptedException {
 
 		List<Part> partsList = new ArrayList<Part>();
 
 		for (int i = 0; i < 9; i++) {
 			Part p = new Part(new PartType("A"));
 			nest.msgHereIsPart(p);
 			partsList.add(p);
 		}
 
 		List<CameraAgent.MyNest> MyNests = camera.getNests();
 		CameraAgent.MyNest MyNest = null;
 		CameraAgent.MyNest MyNest2 = null;
 		
 		KitConfig config= new KitConfig("Config");
 		config.addItem(nest.getTypesOfParts().get(0), 2);
 		Kit kit = new Kit(config);
 
 		for (CameraAgent.MyNest mn : MyNests) {
 			if (mn.nest == nest) {
 				MyNest = mn;
 			} else if (mn.nest == nest2) {
 				MyNest2 = mn;
 			}
 		}
 
 		partsrobot.msgUseThisKit(kit);
 
 		// Need a reference to the MyKit object that the partsrobot is
 		// building
 		PartsRobotAgent.MyKit MyKit = null;
 		for (PartsRobotAgent.MyKit mk : partsrobot.getMyKits()) {
 			if (mk.kit == kit) {
 				MyKit = mk;
 				break;
 			}
 		}
 
 		assertEquals("Nest should have 9 parts", 9, nest.currentParts.size());
 
 		assertEquals("Camera should have 8 nests", 8, camera.getNests().size());
 
 		// Start the test
 
 		for (int i = 0; i < 4; i++) {
 			/*
 			 * The nest's part count will keep decreasing but for this test, we
 			 * want to know that the partsrobot is picking up parts successfully
 			 * so the camera will receive the nest full message even if the nest
 			 * isn't actually full.
 			 */
 			camera.msgIAmFull(nest);
 
 			// Invoke Camera's scheduler
 			camera.pickAndExecuteAnAction();
 
 			// No rules should have passed as only 1 nest is full
 			camera.msgIAmFull(nest2);
 
 			camera.pickAndExecuteAnAction();
 
 			// Camera's scheduler should have fired takePictureOfNest()
 			assertEquals(
 					"Camera should have set nest's status to 'photographing'",
 					NestStatus.PHOTOGRAPHING, MyNest.state);
 			assertEquals(
 					"Camera should have set nest2's status to 'photographing'",
 					NestStatus.PHOTOGRAPHING, MyNest2.state);
 
 			// CameraGraphics sends this when the camera has taken a photograph
 			// of
 			// both nests.
 			camera.msgTakePictureNestDone(nest.nestGraphics, true,
 					nest2.nestGraphics, true);
 			// camera.msgTakePictureNestDone(nest2.guiNest);
 
 			assertEquals(
 					"Camera should have set nest's status to 'photographed'",
 					NestStatus.PHOTOGRAPHED, MyNest.state);
 			assertEquals(
 					"Camera should have set nest2's status to 'photographed'",
 					NestStatus.PHOTOGRAPHED, MyNest2.state);
 
 			camera.pickAndExecuteAnAction();
 
 			// Camera's scheduler should have fired tellPartsRobot()
 			assertEquals("Camera should have set nest's status to 'not ready'",
 					NestStatus.NOT_READY, MyNest.state);
 			assertEquals("Nest2's status should still be 'photographed'",
 					NestStatus.PHOTOGRAPHED, MyNest2.state);
 
 			camera.pickAndExecuteAnAction();
 
 			// Camera's scheduler should have again fired tellPartsRobot()
 			assertEquals("Nest2's status should still be 'not ready'",
 					NestStatus.NOT_READY, MyNest2.state);
 			assertEquals(
 					"Camera should have set nest2's status to 'not ready'",
 					NestStatus.NOT_READY, MyNest2.state);
 
 			// Camera now sleeps until the kit is assembled
 
 			/*
 			 * If the scheduler fires now, the code blocks if the previous
 			 * animation isn't complete, so simulate completing the pickUpPart
 			 * animation and releasing the animation permit. In this test case,
 			 * we do this before invoking the scheduler as this test runs in a
 			 * single thread (and will get stuck if partsrobot attempts to
 			 * acquire a permit) whereas in the factory, the PartsRobotGraphics
 			 * will be running in another thread.
 			 */
 			partsrobot.msgPickUpPartDone();
 
 			// Now it's safe to invoke the scheduler
 			partsrobot.pickAndExecuteAnAction();
 		}
 
 		// The Parts Robot now needs to place parts, once for each arm it has
 		// full.
 		for (int i = 0; i < 4; i++) {
 			/*
 			 * Parts robot needs a semaphore permit again, simulating that the
 			 * animation for placing the part into the kit is done.
 			 */
 			partsrobot.msgGivePartToKitDone();
 
 			partsrobot.pickAndExecuteAnAction();
 		}
 
 		// Now we need 4 more parts to place as a kit has 9 parts.
 
 		for (int i = 0; i < 4; i++) {
 			camera.msgIAmFull(nest);
 			camera.pickAndExecuteAnAction();
 			camera.msgIAmFull(nest2);
 			camera.pickAndExecuteAnAction();
 			assertEquals(
 					"Camera should have set nest's status to 'photographing'",
 					NestStatus.PHOTOGRAPHING, MyNest.state);
 			assertEquals(
 					"Camera should have set nest2's status to 'photographing'",
 					NestStatus.PHOTOGRAPHING, MyNest2.state);
 			camera.msgTakePictureNestDone(nest.nestGraphics, true,
 					nest2.nestGraphics, true);
 			// camera.msgTakePictureNestDone(nest2.guiNest);
 			assertEquals(
 					"Camera should have set nest's status to 'photographed'",
 					NestStatus.PHOTOGRAPHED, MyNest.state);
 			assertEquals(
 					"Camera should have set nest2's status to 'photographed'",
 					NestStatus.PHOTOGRAPHED, MyNest2.state);
 			camera.pickAndExecuteAnAction();
 			assertEquals("Camera should have set nest's status to 'not ready'",
 					NestStatus.NOT_READY, MyNest.state);
 			assertEquals("Nest2's status should still be 'photographed'",
 					NestStatus.PHOTOGRAPHED, MyNest2.state);
 			camera.pickAndExecuteAnAction();
 			assertEquals("Nest2's status should still be 'not ready'",
 					NestStatus.NOT_READY, MyNest2.state);
 			assertEquals(
 					"Camera should have set nest2's status to 'not ready'",
 					NestStatus.NOT_READY, MyNest2.state);
 			partsrobot.msgPickUpPartDone();
 			partsrobot.pickAndExecuteAnAction();
 		}
 
 		for (int i = 0; i < 4; i++) {
 			partsrobot.msgGivePartToKitDone();
 			partsrobot.pickAndExecuteAnAction();
 		}
 
 		// One last part to place
 		camera.msgIAmFull(nest);
 		camera.pickAndExecuteAnAction();
 		camera.msgIAmFull(nest2);
 		camera.pickAndExecuteAnAction();
 		assertEquals("Camera should have set nest's status to 'photographing'",
 				NestStatus.PHOTOGRAPHING, MyNest.state);
 		assertEquals(
 				"Camera should have set nest2's status to 'photographing'",
 				NestStatus.PHOTOGRAPHING, MyNest2.state);
 		camera.msgTakePictureNestDone(nest.nestGraphics, true,
 				nest2.nestGraphics, true);
 		// camera.msgTakePictureNestDone(nest2.guiNest);
 		assertEquals("Camera should have set nest's status to 'photographed'",
 				NestStatus.PHOTOGRAPHED, MyNest.state);
 		assertEquals("Camera should have set nest2's status to 'photographed'",
 				NestStatus.PHOTOGRAPHED, MyNest2.state);
 		camera.pickAndExecuteAnAction();
 		assertEquals("Camera should have set nest's status to 'not ready'",
 				NestStatus.NOT_READY, MyNest.state);
 		assertEquals("Nest2's status should still be 'photographed'",
 				NestStatus.PHOTOGRAPHED, MyNest2.state);
 		camera.pickAndExecuteAnAction();
 		assertEquals("Nest2's status should still be 'not ready'",
 				NestStatus.NOT_READY, MyNest2.state);
 		assertEquals("Camera should have set nest2's status to 'not ready'",
 				NestStatus.NOT_READY, MyNest2.state);
 		partsrobot.msgPickUpPartDone();
 		partsrobot.pickAndExecuteAnAction();
 		partsrobot.msgGivePartToKitDone();
 		partsrobot.pickAndExecuteAnAction();
 
 		// Kit is now done
 		assertEquals("Parts Robot's MyKit's status should now be 'Done'",
 				MyKitStatus.Done, MyKit.MKS);
 
 		/*
 		 * Wake up camera to perform the final inspection of the kit before it
 		 * leaves this section. Note that this is after the KitRobot has moved
 		 * it into the inspection area.
 		 */
 		camera.msgInspectKit(kit);
 
 		CameraAgent.MyKit CameraKit = null;
		for (CameraAgent.MyKit mk : camera.getKits()) {
			if (mk.kit == kit) {
				CameraKit = mk;
				break;
 			}
		}
 
 		camera.pickAndExecuteAnAction();
 
 		// Camera's scheduler should have fired takePictureOfKit()
 		assertEquals(
 				"Camera's MyKit's status should now be 'picture being taken'",
 				CameraAgent.KitStatus.PICTURE_BEING_TAKEN, CameraKit.ks);
 
 		// In the normative scenario, assume the kit is good
 		camera.msgTakePictureKitDone(kit.kitGraphics, true);
 
 		assertEquals("Camera's MyKit's status should now be 'done'",
 				CameraAgent.KitStatus.DONE, CameraKit.ks);
 		assertTrue("Camera's MyKit's should have passed the inspection",
 				CameraKit.kitDone);
 
 		// At this point the camera will message the kitrobot and the test is
 		// concluded.
 
 	}
 
 	public String getFILEPATH() {
 		return FILEPATH;
 	}
 }
