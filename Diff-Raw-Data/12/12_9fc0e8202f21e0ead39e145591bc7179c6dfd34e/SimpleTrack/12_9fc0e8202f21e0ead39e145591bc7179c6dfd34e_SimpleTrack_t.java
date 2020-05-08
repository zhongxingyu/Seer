 /**
  * General TinySynth Heading.
  * We live in 16 bit signed 44100 Hz sound. 
  */
 package synth;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The <code>SimpleTrack</code> is meant as a minimal implementation of the
  * interface <code>Track</code>.
  * <p>
  * There is done no attempt to optimize this class. In fact it is dreadfully
  * inefficient.
  * <p>
  * As mentioned in the interface, this class is not thread safe.
  * 
  * @author Sitron Te
  * 
  */
 public class SimpleTrack extends AbstractSoundSource implements Track {
 	private long frame = 0, totalLength = 0;
 	private int soundId = 0;
 	private final int channelCount;
 	private List<SimpleSound> soundList = new ArrayList<SimpleSound>();
 
 	/**
 	 * Creates a new <code>SimpleTrack</code> with the given number of output
 	 * channels
 	 * 
 	 * @param channelCount
 	 *            number of output channels for this <code>Track</code>. 1 for
 	 *            mono, 2 for stereo
 	 * @throws IllegalArgumentException
 	 *             if channel count is set too low
 	 */
 	public SimpleTrack(int channelCount) {
 		if (channelCount < 0)
 			throw new IllegalArgumentException(
 					"output count must be in legal range!");
 		this.channelCount = channelCount;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.Track#insertSound(synth.InstrumentBox, synth.Note, long, int)
 	 */
 	@Override
 	public int insertSound(InstrumentBox sound, Note note, long frame,
 			int length) {
 		SimpleSound s = new SimpleSound(sound, note, frame, length);
 		soundList.add(s);
 		return s.getUniqueIdentifier();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.Track#insertSound(synth.SoundSource, long, int)
 	 */
 	@Override
 	public int insertSound(SoundSource sound, long frame, int length) {
 		SimpleSound s = new SimpleSound(sound, frame, length);
 		soundList.add(s);
 		return s.getUniqueIdentifier();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.Track#removeSound(int)
 	 */
 	@Override
 	public void removeSound(int uniqueIdentifier) {
 		for (int i = 0; i < soundList.size(); i++) {
 			if (soundList.get(i).getUniqueIdentifier() == uniqueIdentifier) {
 				soundList.remove(i);
 				return;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.Track#getAliveSounds(long)
 	 */
 	@Override
 	public Iterator<Sound> getAliveSounds(long frame) {
 		List<Sound> l = new ArrayList<Sound>();
 		for (SimpleSound s : soundList) {
 			if (s.isAlive(frame))
 				l.add(s);
 		}
 		return l.iterator();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.Track#getAllSounds()
 	 */
 	@Override
 	public Iterator<Sound> getAllSounds() {
 		List<Sound> l = new ArrayList<Sound>();
 		for (Sound s : soundList)
 			l.add(s);
 		return l.iterator();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.Track#getSound(int)
 	 */
 	@Override
 	public Sound getSound(int uniqueIdentifier) {
 		for (Sound s : soundList)
 			if (s.getUniqueIdentifier() == uniqueIdentifier)
 				return s;
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.AbstractSoundSource#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		return frame < totalLength;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see synth.AbstractSoundSource#next()
 	 */
 	@Override
 	public short[] next() {
 		short[] retVal = new short[channelCount];
 		Iterator<Sound> it = getAliveSounds(frame);
 		while (it.hasNext()) {
 			short[] r = ((SimpleSound) it.next()).play(1);
			for (int i = 0; i < channelCount; i++) {
				if ((int) retVal[i] + (int) r[i] > Short.MAX_VALUE)
					retVal[i] = Short.MAX_VALUE;
				else if ((int) retVal[i] + (int) r[i] < -Short.MAX_VALUE)
					retVal[i] = -Short.MAX_VALUE;
				else
					retVal[i] += r[i];
			}
 		}
 		frame++;
 		return retVal;
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
 		for (SimpleSound s : soundList)
 			s.reset();
 		frame = 0;
 	}
 
 	private void findEnd() {
 		Sound sound = null;
 		totalLength = 0;
 		for (Sound s : soundList) {
 			long e = s.getStart() + s.getLength();
 			if (e > totalLength) {
 				totalLength = e;
 				sound = s;
 			}
 		}
 		totalLength--;
 		sound.setLength(sound.getLength());
 	}
 
 	private abstract class AbstractSound implements Sound {
 		private int id;
 		private long start;
 		private int length;
 		private boolean isEnd;
 
 		private AbstractSound(long start, int length) {
 			id = ++soundId;
 			setStart(start);
 			setLength(length);
 		}
 
 		@Override
 		public int getUniqueIdentifier() {
 			return id;
 		}
 
 		@Override
 		public long getStart() {
 			return start;
 		}
 
 		@Override
 		public void setStart(long frame) {
 			if (isEnd && this.start > frame) {
 				isEnd = false;
 				findEnd();
 			}
 			start = frame;
 			if (start + length > totalLength) {
 				totalLength = start + length;
 				isEnd = true;
 			}
 		}
 
 		@Override
 		public int getLength() {
 			return length;
 		}
 
 		@Override
 		public void setLength(int length) {
 			if (isEnd && this.length > length) {
 				isEnd = false;
 				findEnd();
 			}
 			this.length = length;
 			if (start + length > totalLength) {
 				totalLength = start + length;
 				isEnd = true;
 			}
 		}
 	}
 
 	private class SimpleSound extends AbstractSound {
 		private SoundSource sound;
 		private Note note = null;
 		private int currentFrame = 0;
 
 		private SimpleSound(InstrumentBox sound, Note note, long frame,
 				int length) {
 			super(frame, length);
 			this.sound = sound.clone();
 
 			this.note = note;
 		}
 
 		private SimpleSound(SoundSource sound, long frame, int length) {
 			super(frame, length);
 			this.sound = sound;
 		}
 
 		@Override
 		public boolean isInstrumentBox() {
 			return sound instanceof InstrumentBox;
 		}
 
 		@Override
 		public Note getNote() {
 			if (!isInstrumentBox())
 				throw new IllegalStateException(
 						"This Sound is not an InstrumentBox");
 			return note;
 		}
 
 		@Override
 		public void setNote(Note note) {
 			if (!isInstrumentBox())
 				throw new IllegalStateException(
 						"This Sound is not an InstrumentBox");
 			this.note = note;
 		}
 
 		public void reset() {
 			try {
 				sound.reset();
 			} catch (UnsupportedOperationException e) {
 			}
 			currentFrame = 0;
 		}
 
 		public short[] play(int frames) {
 			short[] returnSound;
 			if (currentFrame == 0)
 				if (isInstrumentBox()) {
 					InstrumentBox t = (InstrumentBox) sound;
 					t.play(note);
 				} else
 					reset();
 			if (sound.hasNext()) {
 				returnSound = sound.next(frames);
 				if (returnSound.length < frames * channelCount) {
 					short[] r = new short[frames];
 					for (int i = 0; i < returnSound.length; i++)
 						r[i] = returnSound[i];
 				}
 			} else
 				returnSound = new short[frames * channelCount];
 			if (frames > getLength() - currentFrame) {
 				short[] r = new short[(getLength() * channelCount)
 						- currentFrame];
 				for (int i = 0; i < r.length; i++)
 					r[i] = returnSound[i];
 				returnSound = r;
 			}
 			currentFrame += returnSound.length / channelCount;
 			return returnSound;
 		}
 
 		public boolean isAlive(long frame) {
 			return ((getStart() <= frame) && ((getStart() + getLength()) > frame));
 		}
 	}
 }
