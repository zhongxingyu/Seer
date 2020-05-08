 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import simulanator.Machine;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value.BitLocation;
 import assemblernator.OperandChecker;
 
 /**
  * The ISRG instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef UL1
  */
 public class USI_SND extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "SND";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x04; // 0b000100
 
 	/** The static instance for this instruction. */
 	static USI_SND staticInstance = new USI_SND(true);
 
 	/**
 	 * Denotes the waveform (type) of our sample.
 	 * 
 	 * @author Josh Ventura
 	 * @date May 5, 2012; 2:03:15 AM
 	 */
 	enum SampleType {
 		/** White noise */
 		WHITE(0),
 		/** Square wave */
 		SQUARE(1),
 		/** Saw wave */
 		SAW(2),
 		/** Sine wave */
 		SINE(3),
 		/** Guitar Pluck */
 		PLUCK(4),
 		/** BOINK! sample */
 		BOINK(5),
 		/** BONK! sample */
 		BONK(6),
 		/** Bass sample */
 		BASS(7),
 		/** Bass sample */
 		COWBELL(8),
 		/** Cymbal sample */
 		CYMBAL(9),
 		/** Distorted snare sample */
 		DSNARE(10),
 		/** Hihat sample */
 		HIHAT(11),
 		/** Distorted kick sample */
 		DKICK(12),
 		/** Kick sample */
 		KICKDRUM(13),
 		/** Snare sample */
 		SNAREDRUM(14),
 		/** Tomtom sample */
 		TOMTOMDRUM(15);
 
 		/** The integer ID for this sample */
 		public int id;
 
 		/**
 		 * @param ID
 		 *            The ID of this sample type.
 		 */
 		private SampleType(int ID) {
 			id = ID;
 		}
 	}
 
 	/** An integer ID of our sample type. */
 	SampleType sampleType;
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc + 1;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		if (operands.size() < 3) {
 			hErr.reportError(makeError("tooFewOperandsIns", opId), lineNum, 0);
 			return false;
 		}
 		if (operands.size() > 3) {
 			hErr.reportError(makeError("extraOperandsIns", opId), lineNum, 0);
 			return false;
 		}
 		Operand op = getOperandData("ST"); // Get Sample Type
 		if (op == null) {
 			hErr.reportError(
 					makeError("instructionMissingOpS", opId, "ST",
 							"Sample Type"), lineNum, 0);
 			return false;
 		}
 		if (!OperandChecker.isValidString(op.expression)) {
 			hErr.reportError(makeError("STstringCount"), lineNum, -1);
 			return false;
 		}
 		String sample = IOFormat.escapeString(op.expression, lineNum,
 				op.valueStartPosition, hErr).toLowerCase();
 		sampleType = samplenames.get(sample);
 
 		op = getOperandData("FR");
 		if (op == null) {
 			hErr.reportError(
 					makeError("instructionMissingOpS", opId, "FR", "FRequency"),
 					lineNum, 0);
 			return false;
 		}
 		op.value = module.evaluate(op.expression, false, BitLocation.Other,
 				hErr, this, op.valueStartPosition);
 		if (op.value.value < 50 || op.value.value > 20000) {
 			hErr.reportError(
 					makeError("OORconstant", "FR", opId, "50", "20000"),
 					lineNum, op.valueStartPosition);
 			return false;
 		}
 
 		op = getOperandData("DM");
 		if (op == null) {
 			hErr.reportError(
 					makeError("instructionMissingOpS", opId, "DM",
 							"Duration Millis"), lineNum, 0);
 			return false;
 		}
 		op.value = module.evaluate(op.expression, false, BitLocation.Other,
 				hErr, this, op.valueStartPosition);
 		if (op.value.value < 25 || op.value.value > 6400) {
 			hErr.reportError(
 					makeError("OORconstant", "DM", opId, "25", "6400"),
 					lineNum, op.valueStartPosition);
 			return false;
 		}
 
 		return true;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return new int[] { (opCode << 26) | (sampleType.id << 22)
 				| (getOperandData("FR").value.value << 8)
 				| (getOperandData("DM").value.value / 25) };
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 		int freq = (instruction & ~(0xFFFFFFFF >>> 22 << 22)) >> 8;
 		int dur = (instruction & (~(0xFFFFFFFF >>> 8 << 8))) * 24;
 		int sample = (instruction & ~(0xFFFFFFFF >>> 26 << 26)) >> 22;
 		play(samples[sample].generate(freq, dur, 48000), 48000);
 		try {
 			Thread.sleep(dur);
 		} catch (InterruptedException e) {}
 	}
 
 	/**
 	 * Tone generation class.
 	 * 
 	 * @author Josh Ventura
 	 * @date May 24, 2012; 1:41:07 AM
 	 */
 	static interface ToneGenerator {
 		/**
 		 * @param freq
 		 *            The frequency of the tone.
 		 * @param dur
 		 *            The duration of the tone.
 		 * @param sps
 		 *            Samples per second.
 		 * @return Bytes of this sample.
 		 */
 		public byte[] generate(int freq, int dur, int sps);
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 24, 2012; 7:59:03 PM
 	 */
 	static abstract class WaveFormTone implements ToneGenerator {
 		/**
 		 * Math function to get waveform value at a position
 		 * 
 		 * @param x
 		 *            The position.
 		 * @return The value of this waveform.
 		 */
 		abstract double waveform(double x);
 
 		/** Generate a sine tone. */
 		@Override final public byte[] generate(int freq, int dur, int sps) {
 			System.out.println("Generate sine tone " + freq + " * " + dur);
 			int buflen = (dur * sps) / 1000, i;
 			byte[] buf = new byte[buflen];
 			for (i = 0; i < buf.length - 1200; i++)
 				buf[i] = (byte) (waveform(2 * Math.PI
 						* (i / (double) sps * freq)) * 64);
 			byte frqat = buf[i - 1];
 			while (i < buf.length)
 				buf[i++] = frqat = (byte) (frqat > 0 ? frqat - 1
 						: frqat < 0 ? frqat + 1 : 0);
 			return buf;
 		}
 	}
 
 	/** Sine tone generator. */
 	static class SineTone extends WaveFormTone implements ToneGenerator {
 		/** Use system sine(). */
 		@Override double waveform(double x) {
 			return Math.sin(x);
 		}
 	}
 
 	/** Saw tone generator. */
 	static class SawTone extends WaveFormTone implements ToneGenerator {
 		/** Saw function. */
 		@Override double waveform(double x) {
 			return (x / (2 * Math.PI)) % 1;
 		}
 	}
 
 	/** Square tone generator. */
 	static class SquareTone extends WaveFormTone implements ToneGenerator {
 		/**
 		 * @param x
 		 *            The phase.
 		 * @return The saw value.
 		 */
 		@Override double waveform(double x) {
 			return Math.min(.8, Math.max(-.8, Math.sin(x) * 10));
 		}
 	}
 
 	/** Square tone generator. */
 	static class WhiteNoise extends WaveFormTone implements ToneGenerator {
 		/**
 		 * @param x
 		 *            The phase.
 		 * @return The noise value.
 		 */
 		@Override double waveform(double x) {
 			return ((x / (2 * Math.PI)) % 1) * (1 - 2 * Math.random());
 		}
 	}
 
 	/** Warp the BOINK sample to a frequency. */
 	static class WavSourceSample implements ToneGenerator {
 		/** The data from this sample. */
 		byte[] rawData = null;
 		/** The name of this sample. */
 		String name;
 		/** Base frequency */
 		double baseFreq = 440; // A440
 
 		/**
 		 * Default constructor.
 		 * 
 		 * @param sampleName
 		 *            The name of the wav file for this sample.
 		 */
 		public WavSourceSample(String sampleName) {
 			name = sampleName;
 			InputStream a = getClass().getResourceAsStream("/samples/" + name);
 			if (a == null) {
 				System.err.println("Failed to open sample, " + name);
 				return;
 			}
 			try {
				AudioInputStream ais = AudioSystem.getAudioInputStream(a);
 				int spf = ais.getFormat().getFrameSize();
 				byte[] buf = new byte[(int) (spf * ais.getFrameLength())];
 				ais.read(buf);
 				if (spf == 1)
 					rawData = buf;
 				else {
 					int ri = 0;
 					rawData = new byte[(buf.length + spf - 1) / spf];
 					for (int i = 0; i < buf.length; i += spf)
 						rawData[ri++] = buf[i + spf - 1];
 				}
 				System.out.println("Loaded " + name + " successfully ["
 						+ rawData.length + " bytes, " + spf + " sps].");
 			} catch (UnsupportedAudioFileException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		/** Generate a sine tone. */
 		@Override public byte[] generate(int freq, int dur, int sps) {
 			byte[] res = new byte[(int) (rawData.length * baseFreq / freq)];
 			int compl = dur * sps;
 			int minl = Math.min(res.length, compl);
 			int i;
 			for (i = 0; i < minl; ++i)
 				res[i] = rawData[(int) (i * freq / baseFreq)];
 			while (i < res.length)
 				res[i] = (byte) ((i * freq / baseFreq) * Math.min(1,
 						10 / (i - compl)));
 			return res;
 		}
 	}
 
 	/** Generate a pluck with a frequency. */
 	static class PluckGenerator implements ToneGenerator {
 		/** Generate a sine tone. */
 		@Override public byte[] generate(int freq, int dur, int sps) {
 			byte[] buf = new byte[dur * sps / 1000];
 			byte[] burst = new byte[(int) (sps / freq)];
 			for (int i = 0; i < burst.length; ++i)
 				burst[i] = (byte) (50 - Math.random() * 100);
 			for (int i = 0; i < buf.length; ++i)
 				buf[i] = (byte) (burst[i % burst.length] * Math.min(1,
 						2000.0 / i));
 			return buf;
 		}
 	}
 
 	/** All available samples. */
 	static ToneGenerator samples[] = new ToneGenerator[16];
 
 	/** A map of our sample names to their sample ID. */
 	static HashMap<String, SampleType> samplenames = new HashMap<String, SampleType>();
 
 	static {
 		samples[SampleType.WHITE.id] = new WhiteNoise(); // WHITE
 		samples[SampleType.SQUARE.id] = new SquareTone(); // SQUARE
 		samples[SampleType.SAW.id] = new SawTone(); // SAW
 		samples[SampleType.SINE.id] = new SineTone(); // SINE
 		samples[SampleType.PLUCK.id] = new PluckGenerator(); // PLUCK
 		
 		samples[SampleType.BOINK.id] = new WavSourceSample("boink.wav"); // BOINK
 		samples[SampleType.BONK.id] = new WavSourceSample("bonk.wav"); // BONK
 		samples[SampleType.BASS.id] = new WavSourceSample("bassdrum3.wav"); // BASS
 		samples[SampleType.COWBELL.id] = new WavSourceSample("cowbell5.wav"); // COWBELL
 		samples[SampleType.CYMBAL.id] = new WavSourceSample("cymbal1.wav"); // CYMBAL
 		samples[SampleType.DSNARE.id] = new WavSourceSample(
 				"distortedsnare1.wav"); // DSNARE
 		samples[SampleType.HIHAT.id] = new WavSourceSample("hihat1.wav"); // HIHAT
 		samples[SampleType.DKICK.id] = new WavSourceSample("kick4_dkick.wav"); // DKICK
 		samples[SampleType.KICKDRUM.id] = new WavSourceSample("kickdrum1.wav"); // KICKDRUM
 		samples[SampleType.SNAREDRUM.id] = new WavSourceSample(
 				"snaredrum14.wav"); // SNAREDRUM
 		samples[SampleType.TOMTOMDRUM.id] = new WavSourceSample(
 				"tomtomdrum1.wav"); // TOMTOMDRUM
 
 		samplenames.put("white", SampleType.WHITE);
 		samplenames.put("square", SampleType.SQUARE);
 		samplenames.put("saw", SampleType.SAW);
 		samplenames.put("sine", SampleType.SINE);
 		samplenames.put("pluck", SampleType.PLUCK);
 		
 		samplenames.put("boink", SampleType.BOINK);
 		samplenames.put("bonk", SampleType.BONK);
 		samplenames.put("bassdrum", SampleType.BASS);
 		samplenames.put("bass", SampleType.BASS);
 		samplenames.put("cowbell", SampleType.COWBELL);
 		samplenames.put("cymbal", SampleType.CYMBAL);
 		samplenames.put("distortedsnare", SampleType.DSNARE);
 		samplenames.put("dsnare", SampleType.DSNARE);
 		samplenames.put("hihat", SampleType.HIHAT);
 		samplenames.put("hat", SampleType.HIHAT);
 		samplenames.put("distortedkick", SampleType.DKICK);
 		samplenames.put("dkick", SampleType.DKICK);
 		samplenames.put("kickdrum", SampleType.KICKDRUM);
 		samplenames.put("kick", SampleType.KICKDRUM);
 		samplenames.put("snaredrum", SampleType.SNAREDRUM);
 		samplenames.put("snare", SampleType.SNAREDRUM);
 		samplenames.put("tomtomdrum", SampleType.TOMTOMDRUM);
 		samplenames.put("tomtom", SampleType.TOMTOMDRUM);
 		samplenames.put("tom", SampleType.TOMTOMDRUM);
 	}
 
 	/**
 	 * Plays a sample buffer.
 	 * 
 	 * @author Josh Ventura
 	 * @date May 5, 2012; 2:26:44 AM
 	 * @param data
 	 *            The sample data to play.
 	 * @param sps
 	 *            The sample rate, in samples per second.
 	 * @specRef N/A
 	 */
 	public static void play(byte[] data, int sps) {
 		AudioFormat afmt = new AudioFormat(sps, 8, 1, true, false);
 		// AudioFormat afmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
 		// sps, 8,1,((8 + 7) / 8) * 1, sps,false);
 		DataLine.Info info = new DataLine.Info(Clip.class, afmt);
 		try {
 			final Clip clip = (Clip) AudioSystem.getLine(info);
 			clip.open(afmt, data, 0, data.length);
 
 			new Thread() {
 				@Override public void run() {
 					clip.start();
 					try {
 						do
 							Thread.sleep(99);
 						while (clip.isActive());
 					} catch (InterruptedException e) {
 						System.err.println("INTERRUPT");
 					}
 					System.out.println("Clip done");
 					clip.stop();
 					clip.close();
 				}
 			}.start();
 		} catch (LineUnavailableException e1) {
 			System.err.println("oops");
 		}
 
 	}
 
 	// =========================================================
 	// === Redundant code ======================================
 	// =========================================================
 	// === This code's the same in all instruction classes, ====
 	// === But Java lacks the mechanism to allow stuffing it ===
 	// === in super() where it belongs. ========================
 	// =========================================================
 
 	/**
 	 * @see Instruction
 	 * @return The static instance of this instruction.
 	 */
 	public static Instruction getInstance() {
 		return staticInstance;
 	}
 
 	/** @see assemblernator.Instruction#getOpId() */
 	@Override public String getOpId() {
 		return opId;
 	}
 
 	/** @see assemblernator.Instruction#getOpcode() */
 	@Override public int getOpcode() {
 		return opCode;
 	}
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override public Instruction getNewInstance() {
 		return new USI_SND();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_SND(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_SND() {}
 }
