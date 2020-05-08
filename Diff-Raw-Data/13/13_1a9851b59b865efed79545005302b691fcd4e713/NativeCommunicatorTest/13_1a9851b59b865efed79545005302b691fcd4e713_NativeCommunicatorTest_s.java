 package nl.tue.fingerpaint.server.simulator;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 /**
  * Java jUnit tests for the class {@link NativeCommunicator}.
  * 
  * @author Group Fingerpaint
  */
 public class NativeCommunicatorTest extends TestCase {
 
 	/**
 	 * A test that runs a simulation and checks whether the received segregation
 	 * is correct.
 	 */
 	@Test
 	public void testSegregation() {
 		int size = 96000;
 		double[] concentrationVector = new double[size];
 		for (int i = 0; i < size; i++) {
 			if (i < size / 2) {
 				concentrationVector[i] = 1;
 			} else {
 				concentrationVector[i] = 0;
 			}
 		}
 		NativeCommunicator c = NativeCommunicator.getInstance();
 		double segregation = c.simulate(
				"Rectangle400x240", "Default", concentrationVector, 40, "TL");
 		assertTrue("Segregation " + segregation + " was incorrect", 
 				segregation == 0.8296319009181);
 	}
 
 }
