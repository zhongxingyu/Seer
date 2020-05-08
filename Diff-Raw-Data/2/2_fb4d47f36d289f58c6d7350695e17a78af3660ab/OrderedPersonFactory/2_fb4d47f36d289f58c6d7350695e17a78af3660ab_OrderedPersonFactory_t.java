 package nardiff.ordered;
 
 import nardiff.MersenneTwisterFast;
 import nardiff.Person;
 
 import org.apache.commons.collections15.Factory;
 
 /**
  * This factory creates OrderedPerson objects having a initial set of numbers
  * representing individual pieces of information. The set is comprised of a
  * fixed (parameterizable) number of bytes the value of which is drawn from a
  * larger set of all pieces of information in the simulation. The exact subset
  * given to each agent is random. In order to ensure that all numbers exist
  * within a given population, the byte values are drawn from a list of all
  * possible byte values within the simulation, the order of which is randomized
  * every pass through.
  * 
  * @author kkoning
  * 
  */
 public class OrderedPersonFactory implements Factory<Person> {
 
 	byte[] allInfoPieces;
 	byte numInitialInfoPieces;
 	byte pos = 0;
 	MersenneTwisterFast random;
 
 	/**
 	 * Note: As a result of this initialization process, information will be
 	 * evenly distributed. That is, no single piece of information is (more than
 	 * 1) more prevalent in the population than any other piece of information.
 	 */
 	public OrderedPersonFactory(MersenneTwisterFast random,
 			byte totalInfoPieces, byte initialInfoPieces) {
 		/*
 		 * keep a random # generator reference for randomizing the order of the
 		 * information pieces.
 		 */
 		this.random = random;
 
 		this.numInitialInfoPieces = initialInfoPieces;
 		allInfoPieces = new byte[totalInfoPieces];
 		for (byte i = 0; i < totalInfoPieces; i++)
 			allInfoPieces[i] = i;
 
 		randomizeAllInfoPieces();
 	}
 
 	@Override
 	public Person create() {
 		OrderedPerson person = new OrderedPerson();
 
 		person.knowledge = new byte[numInitialInfoPieces];
 
 		for (int i = 0; i < numInitialInfoPieces; i++) {
 			person.knowledge[i] = nextRandomInfoPiece();
 		}
 
 		return person;
 	}
 
 	private byte nextRandomInfoPiece() {
 		if (pos >= allInfoPieces.length) {
 			randomizeAllInfoPieces();
 			pos = 0;
 		}
 		return allInfoPieces[pos++];
 	}
 
 	private void randomizeAllInfoPieces() {
		for (int i = 0; i < allInfoPieces.length; i++) {
 			int index = random.nextInt(allInfoPieces.length);
 			// Simple swap
 			byte tmp = allInfoPieces[index];
 			allInfoPieces[index] = allInfoPieces[i];
 			allInfoPieces[i] = tmp;
 		}
 	}
 	
 	public static void main(String[] args) {
 		MersenneTwisterFast random = new MersenneTwisterFast(1);
 		OrderedPersonFactory opf = new OrderedPersonFactory(random,(byte)30,(byte)5);
 		for (int i = 0; i < 30; i++) {
 			System.out.println(opf.create());
 		}
 	}
 	
 	
 }
