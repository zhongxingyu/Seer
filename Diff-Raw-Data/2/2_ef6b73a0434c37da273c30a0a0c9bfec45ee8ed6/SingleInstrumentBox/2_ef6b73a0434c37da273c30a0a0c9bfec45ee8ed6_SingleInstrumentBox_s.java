 /**
  * 
  */
 package synth;
 
 import java.util.ArrayList;
 
 /**
  * The <code>SingleInstrumentBox</code> is a single instrument implementation of
  * the interface <code>InstrumentBox</code>.
  * <p>
  * As mentioned in the interface, this class is not thread safe.
  * 
  * @author Sitron Te
  * 
  */
 public class SingleInstrumentBox extends AbstractSoundSource implements
 		InstrumentBox {
 	private Instrument instr = null;
 	private Note note = null;
 	private ArrayList<SoundEffect> effects = new ArrayList<SoundEffect>();
 	private final int channelCount;
 
 	/**
 	 * Creates an empty <code>SingleInstrumentBox</code>. Connect it to an
 	 * <code>Instrument</code> and <code>Note</code> to use it.
 	 * 
 	 * @param channelCount
 	 *            number of output channels. 1 for mono, 2 for stereo.
 	 * @throws IllegalArgumentException
 	 *             if <code>outputs <= 0</code>
 	 */
 	public SingleInstrumentBox(int channelCount) {
 		if (channelCount < 0)
 			throw new IllegalArgumentException(
 					"output count must be in legal range!");
 		this.channelCount = channelCount;
 	}
 
 	/**
 	 * Creates an uninitialized <code>SingleInstrumentBox</code> with an
 	 * instrument. Play it with a <code>Note</code> to use it. This will clone
 	 * the brought along <code>Instrument</code>.
 	 * 
 	 * @param instr
 	 *            the instrument to play from
 	 * @param channelCount
 	 *            number of output channels. 1 for mono, 2 for stereo.
 	 * @throws NullPointerException
 	 *             if <code>instr</code> is <code>null</code>
 	 */
 	public SingleInstrumentBox(Instrument instr, int channelCount) {
 		if (instr == null)
 			throw new NullPointerException("Must bring along an Instrument!");
 		if (channelCount < 0)
 			throw new IllegalArgumentException(
 					"output count must be in legal range!");
 		this.channelCount = channelCount;
 		this.instr = instr.clone();
 	}
 
 	/**
 	 * Creates an initialized <code>SingleInstrumentBox</code> with an
 	 * instrument and note to play. This will clone the brought along
 	 * <code>Instrument</code>. The <code>Note</code> will be used as is, so any
 	 * later alterations to this will alter this
 	 * <code>SingleInstrumentBox</code>.
 	 * 
 	 * @param instr
 	 *            the instrument to play from
 	 * @param note
 	 *            the <code>Note</code> to play
 	 * @param channelCount
 	 *            number of output channels. 1 for mono, 2 for stereo.
 	 * @throws NullPointerException
 	 *             if <code>instr</code> or <code>note</code> is
 	 *             <code>null</code>
 	 */
 	public SingleInstrumentBox(Instrument instr, Note note, int channelCount) {
 		if (instr == null)
 			throw new NullPointerException("Must bring along an Instrument!");
 		if (note == null)
 			throw new NullPointerException("Must bring along a Note");
 		if (channelCount < 0)
 			throw new IllegalArgumentException(
 					"output count must be in legal range!");
 		this.channelCount = channelCount;
 		this.instr = instr.clone();
 		if (!play(note)) {
 			throw new RuntimeException(
 					"An unexpected exception occured during InstrumentBox creation!");
 		}
 	}
 
 	/**
 	 * Constructs this <code>SingleInstrumentBox</code> cloned from the brought
 	 * along <code>SingleInstrumentBox</code>. The same result as calling
 	 * <code>original.clone()</code>.
 	 * 
 	 * @param original
 	 *            the <code>WaveInstrument</code> to create a clone from.
 	 */
 	public SingleInstrumentBox(SingleInstrumentBox original) {
 		channelCount = original.channelCount;
 		if (original.instr != null)
 			instr = original.instr.clone();
 		if (original.note != null)
 			play(original.note);
 		for (SoundEffect e:original.effects)
			effects.add(e);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#isTaken()
 	 */
 	@Override
 	public boolean isTaken() {
 		return instr != null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#attachInstrument(synth.Instrument)
 	 */
 	@Override
 	public boolean attachInstrument(Instrument instrument) {
 		if (isTaken())
 			return false;
 		instr = instrument.clone();
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#detachInstrument()
 	 */
 	@Override
 	public void detachInstrument() {
 		instr = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#play(synth.Note)
 	 */
 	@Override
 	public boolean play(Note note) {
 		if (!isTaken())
 			return false;
 		this.note = note;
 		instr.play(note);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#getNote()
 	 */
 	@Override
 	public Note getNote() {
 		return note;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#attachSoundEffect(synth.SoundEffect)
 	 */
 	@Override
 	public boolean attachSoundEffect(SoundEffect effect) {
 		if (getChannelCount() != effect.getChannelCount())
 			return false;
 		effects.add(effect);
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#removeSoundEffect(synth.SoundEffect)
 	 */
 	@Override
 	public void removeSoundEffect(SoundEffect effect) {
 		effects.remove(effect);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#removeSoundEffect(int)
 	 */
 	@Override
 	public void removeSoundEffect(int effectNumber) {
 		if (effectNumber < 0 || effectNumber >= effects.size())
 			throw new IndexOutOfBoundsException("Illegal SoundEffect index!");
 		effects.remove(effectNumber);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#containsEffect(synth.SoundEffect)
 	 */
 	@Override
 	public boolean containsEffect(SoundEffect effect) {
 		return effects.contains(effect);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.InstrumentBox#getSoundEffectCount()
 	 */
 	@Override
 	public int getSoundEffectCount() {
 		return effects.size();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.AbstractSoundSource#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		return instr != null && instr.hasNext();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.AbstractSoundSource#next()
 	 */
 	@Override
 	public short[] next() {
 		short[] ut = new short[channelCount];
 		short u = instr.next();
 		for (int i = 0; i < channelCount; i++)
 			ut[i] = u;
 		for (SoundEffect ef : effects)
 			ef.performEffect(ut);
 		return ut;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.AbstractSoundSource#getChannelCount()
 	 */
 	@Override
 	public int getChannelCount() {
 		return channelCount;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.AbstractSoundSource#reset()
 	 */
 	@Override
 	public void reset() {
 		instr = null;
 		effects.clear();
 
 	}
 
 	@Override
 	public InstrumentBox clone() {
 		return new SingleInstrumentBox(this);
 	}
 
 }
