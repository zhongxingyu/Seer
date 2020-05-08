 package test.s4;
 
 import io.s4.dispatcher.Dispatcher;
 import io.s4.processor.AbstractPE;
 
 /**
  * This class receive word and sentence events and print them to stdout
  */
 public class WordReceiverPE extends AbstractPE {
 	private StringBuilder builder = new StringBuilder();
 
 	/**
 	 * Dispatcher that will dispatch events on <code>Sentence *</code> stream.
 	 */
 	private Dispatcher dispatcher;
 
 	public Dispatcher getDispatcher() {
 		return dispatcher;
 	}
 
 	public void setDispatcher(Dispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 	/**
 	 * Process word events. This prints out the received word and also builds
 	 * the sentence that will be dispatched once we found the end of the
 	 * sentence identified by <code>.</code>
 	 * 
 	 * @param word
 	 *            Word received on <code>RawWords *</code> stream.
 	 */
 	public void processEvent(Word word) {
		System.out.println("Recieved: " + word);
 
 		// keep building the sentence
 		builder.append(' ').append(word.getString().trim());
 
 		if (builder.toString().endsWith(".")) {
 			System.err.print("End of sentence found");
 
 			// dispatch a Sentence event
 			dispatcher.dispatchEvent("Sentence", new Sentence(builder.toString()));
 
 			// reset buffer.
 			builder.setLength(0);
 		}
 	}
 
 	/**
 	 * Process sentence events. This method demonstrated that one class can
 	 * recieve multiple type of events on different Streams.
 	 * 
 	 * @param sentence
 	 *            Sentence recieved on <code>Sentence *<code> stream.
 	 */
 	public void processEvent(Sentence sentence) {
		System.out.println("Recieved Sentence(WordReceiverPE) : " + sentence);
 	}
 
 	@Override
 	public void output() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public String getId() {
 		return this.getClass().getName();
 	}
 }
