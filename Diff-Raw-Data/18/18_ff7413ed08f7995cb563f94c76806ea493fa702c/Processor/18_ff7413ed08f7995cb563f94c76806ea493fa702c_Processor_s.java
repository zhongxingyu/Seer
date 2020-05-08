 package mustache.core;
 
 import java.io.InvalidObjectException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.io.StreamCorruptedException;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The {@code Processor} class iterates through a sequence of {@link Instruction}s
  * and needs to be notified to enter or exit sections. {@code Processor}s can be
  * serialized and reused at will but their {@link Instruction}s sequence can not
  * be modified.
  * 
  * <p>This class is not meant to be manipulated concurrently by several threads.</p>
  * 
  * @author Dri
  */
 public final class Processor implements Serializable, Iterator<Instruction> {
 	
 	private static final long serialVersionUID = 289040399110456725L;
 
 	private final List<Instruction> sequence;
 	
 	private final transient int maxPosition;
 	private transient int currentPosition = -1;
 	private transient boolean tryOpeningSection;
 	private transient boolean tryClosingSection;
 	
 	private Processor(List<Instruction> sequence) {
 		this.sequence = sequence;
 		this.maxPosition = sequence.size() - 1;
 	}
 	
 	/**
 	 * Creates a {@code Processor} from a processable {@link Sequencer}.
 	 * @param sequencer the {@link Sequencer}
 	 * @return a newly created {@code Processor}
 	 * @throws NullPointerException if {@code sequencer} is {@code null}
 	 * @throws IllegalArgumentException if {@code sequencer} is not processable
 	 * @see Sequencer#isProcessable()
 	 */
 	public static Processor fromSequencer(Sequencer sequencer) {
 		if (sequencer == null) {
 			throw new NullPointerException();
 		}
 		
 		synchronized (sequencer) {
 			if ( !sequencer.isProcessable() ) {
 				throw new IllegalArgumentException("Sequence not processable");
 			}
 			
 			return new Processor( sequencer.getSequence() );
 		}
 	}
 	
 	/**
 	 * Resets the {@code Processor} to its initial state.
 	 */
 	public void reset() {
 		currentPosition = -1;
 		tryOpeningSection = false;
 		tryClosingSection = false;
 	}
 	
 	/**
 	 * Notifies the {@code Processor} to enter the section.
 	 * @throws IllegalStateException if the current {@link Instruction} is not opening
 	 * @see Instruction#opening()
 	 */
 	public void enterSection() {
 		if (tryOpeningSection == false) {
 			throw new IllegalStateException("Unexpected attempt to enter a section.");
 		}
 		
 		tryOpeningSection = false;
 	}
 
 	/**
 	 * Notifies the {@code Processor} to exit the section.
 	 * @throws IllegalStateException if the current {@link Instruction} is not closing
 	 * @see Instruction#closing()
 	 */
 	public void exitSection() {
 		if (tryClosingSection == false) {
 			throw new IllegalStateException("Unexpected attempt to exit a section.");
 		}
 		
 		tryClosingSection = false;
 	}
 	
 	/**
 	 * Indicates whether there is still {@code Instruction}s to process.
 	 */
 	@Override
 	public boolean hasNext() {
 		return currentPosition < maxPosition;
 	}
 
 	/**
 	 * Returns the next {@code Instruction}s to process.
 	 * @see #enterSection()
 	 * @see #exitSection()
 	 */
 	@Override
 	public Instruction next() {
 		if (tryOpeningSection) {
 			skipSection();
 		}
 		else if (tryClosingSection) {
 			reenterSection();
 		}
 		
 		currentPosition++;
 		
 		return nextInstruction();
 	}
 
 	private Instruction nextInstruction() {
 		Instruction instruction = sequence.get(currentPosition);
 		
 		tryOpeningSection = instruction.opening();
 		tryClosingSection = instruction.closing();
 		
 		return instruction;
 	}
 
 	private void reenterSection() {
 		int openedSections = -1;
 		
 		while (openedSections < 0) {
 			currentPosition--;
 			openedSections += openCloseDelta();
 		}
 		
 		tryClosingSection = false;
 	}
 
 	private void skipSection() {
 		int openedSections = 1;
 		
 		while (openedSections > 0) {
 			currentPosition++;
 			openedSections += openCloseDelta();
 		}
 		
 		if (currentPosition > maxPosition) {
 			currentPosition = maxPosition;
 		}
 		
 		tryOpeningSection = false;
 	}
 
 	private int openCloseDelta() {
 		Instruction instruction = sequence.get(currentPosition);
 		return instruction.opening() ? 1 : instruction.closing() ? -1 : 0;
 	}
 	
 	/**
 	 * Cannot remove {@code Instruction}s from the sequence.
 	 * @throws UnsupportedOperationException
 	 */
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 	private Object writeReplace() {
 		return new SerializationProxy(this);
 	}
 
 	private void readObject(ObjectInputStream stream) throws InvalidObjectException {
 		throw new InvalidObjectException("Proxy required");
 	}
 
 	private static class SerializationProxy implements Serializable {
 		private static final long serialVersionUID = 7682273649183979614L;
 		
 		private final List<Instruction> sequence;
 
 		SerializationProxy(Processor processor) {
 			this.sequence = processor.sequence;
 		}
 		
 		private Object readResolve() throws StreamCorruptedException {
 			try {
 				return Processor.fromSequencer( new Sequencer().addAll(sequence) );
 			} catch (SequenceException e) {
				throw new StreamCorruptedException( e.getMessage() );
 			}
 		}
 	}
 	
 }
