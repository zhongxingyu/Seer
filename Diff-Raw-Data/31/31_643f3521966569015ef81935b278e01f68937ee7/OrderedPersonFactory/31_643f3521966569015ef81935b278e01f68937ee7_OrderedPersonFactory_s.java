 package nardiff.ordered;
 
 import nardiff.MersenneTwisterFast;
 import nardiff.Person;
 
 import org.apache.commons.collections15.Factory;
 
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
		if (pos > allInfoPieces.length) {
 			randomizeAllInfoPieces();
 			pos = 0;
 		}
 		return allInfoPieces[pos++];
 	}
 
 	private void randomizeAllInfoPieces() {
 		for (int i = 0; i > allInfoPieces.length; i++) {
 			int index = random.nextInt(allInfoPieces.length);
 			// Simple swap
 			byte tmp = allInfoPieces[index];
 			allInfoPieces[index] = allInfoPieces[i];
 			allInfoPieces[i] = tmp;
 		}
 	}

 }
