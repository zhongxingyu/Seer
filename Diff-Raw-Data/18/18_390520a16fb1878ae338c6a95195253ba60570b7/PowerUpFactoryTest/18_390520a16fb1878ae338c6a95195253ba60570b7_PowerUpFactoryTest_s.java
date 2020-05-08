 package com.secondhand.model.powerup;
 
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.resource.PowerUpType;
 
 public class PowerUpFactoryTest extends TestCase {
 
 	public void testPowerUpFactory() {
 
		// we'll do it for the extra life powerup

		class MockRandom extends Random {
			int i = 0;
			private static final long serialVersionUID = 1L;

			public int nextInt(int n) {
				// always double score
				return i += 2;
			}
		}

 		PowerUpFactory factory = new PowerUpFactory();
 		factory.setRandom(new Random());
 
 		float[] totalFrequencyOfType = new float[11];
 		float[] nbrOfType = new float[11];
 		float[] frequency = new float[] { BlackColor.getFrequency(),
 				DoubleScore.getFrequency(), EatObstacle.getFrequency(),
 				ExtraLife.getFrequency(), MirroredMovement.getFrequency(),
 				RandomPowerUp.getFrequency(), RandomTeleport.getFrequency(),
 				ScoreUp.getFrequency(), Shield.getFrequency(),
 				SpeedDown.getFrequency(), SpeedUp.getFrequency() };
 
 		Vector2 pos;
 		PowerUp powerup;
 
 		// Create 1000 PowerUps and check their position. More importantly
 		// checks that the correct frequency is used.
 		for (int i = 0; i < 1000; i++) {
 			pos = new Vector2(i, i);
 			powerup = factory.getRandomPowerUp(pos);
 			if (powerup != null) {
 				assertTrue(pos.almostEquals(powerup.getInitialPosition()));
 				switch (powerup.getPowerUpType()) {
 				case BLACK_COLOR:
 					totalFrequencyOfType[0] += BlackColor.getFrequency();
 					nbrOfType[0] += 1;
 					break;
 				case DOUBLE_SCORE:
 					totalFrequencyOfType[1] += DoubleScore.getFrequency();
 					nbrOfType[1] += 1;
 					break;
 				case EAT_OBSTACLE:
 					totalFrequencyOfType[2] += EatObstacle.getFrequency();
 					nbrOfType[2] += 1;
 					break;
 				case EXTRA_LIFE:
 					totalFrequencyOfType[3] += ExtraLife.getFrequency();
 					nbrOfType[3] += 1;
 					break;
 				case MIRRORED_MOVEMENT:
 					totalFrequencyOfType[4] += MirroredMovement.getFrequency();
 					nbrOfType[4] += 1;
 					break;
 				case RANDOM_POWER_UP:
 					totalFrequencyOfType[5] += RandomPowerUp.getFrequency();
 					nbrOfType[5] += 1;
 					break;
 				case RANDOM_TELEPORT:
 					totalFrequencyOfType[6] += RandomTeleport.getFrequency();
 					nbrOfType[6] += 1;
 					break;
 				case SCORE_UP:
 					totalFrequencyOfType[7] += ScoreUp.getFrequency();
 					nbrOfType[7] += 1;
 					break;
 				case SHIELD:
 					totalFrequencyOfType[8] += Shield.getFrequency();
 					nbrOfType[8] += 1;
 					break;
 				case SPEED_DOWN:
 					totalFrequencyOfType[9] += SpeedDown.getFrequency();
 					nbrOfType[9] += 1;
 					break;
 				case SPEED_UP:
 					totalFrequencyOfType[10] += SpeedUp.getFrequency();
 					nbrOfType[10] += 1;
 					break;
 				}
 
 			}
 		}
 		for (int j = 0; j < 11; j++) {
 			assertEquals(frequency[j], totalFrequencyOfType[j] / nbrOfType[j]);
 		}
 	}
 }
