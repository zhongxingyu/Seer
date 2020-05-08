 package chordest.main.experimental;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import chordest.chord.ChordExtractor.IExternalProcessor;
 import chordest.io.lab.LabFileReader;
 import chordest.io.spectrum.SpectrumFileReader;
 import chordest.model.Chord;
 import chordest.model.Note;
 import chordest.spectrum.SpectrumData;
 import chordest.util.DataUtil;
 import chordest.util.PathConstants;
 import chordest.util.TracklistCreator;
 
 /**
  * Stores spectrum values in csv file along with corresponding chord, chord
  * is written as sequence of notes joined with "-". All the values are stored
  * in one large file.
  * @author Nikolay
  *
  */
 public class TrainDataGenerator implements IExternalProcessor {
 
 	private static final Logger LOG = LoggerFactory.getLogger(TrainDataGenerator.class);
 	public static final String DELIMITER = ",";
 	public static final String ENCODING = "utf-8";
 	private static final String CSV_FILE = PathConstants.OUTPUT_DIR + "train_dA.csv";
 	
 	public static final String TRAIN_FILE_LIST = "work" + PathConstants.SEP + "all_files0.txt";
 	public static final int WINDOW = 19;
 	public static final int OFFSET = 0;
 	public static final int INPUTS = 60;
 
 	private OutputStream csvOut;
 
 	public static void main(String[] args) {
 		List<String> tracklist = TracklistCreator.readTrackList(TRAIN_FILE_LIST);
 		TrainDataGenerator.deleteIfExists(CSV_FILE);
 		int filesProcessed = 0;
 		for (final String binFileName : tracklist) {
 			TrainDataGenerator tdg = new TrainDataGenerator(CSV_FILE, true);
 			SpectrumData sd = SpectrumFileReader.read(binFileName);
 			double[][] result = TrainDataGenerator.prepareSpectrum(sd);
 			Chord[] chords = TrainDataGenerator.prepareChords(binFileName, sd, 0.5);
 			tdg.process(result, chords, 0, result[0].length);
 			if (++filesProcessed % 10 == 0) {
 				LOG.info(filesProcessed + " files processed");
 			}
 		}
 		LOG.info("Done. " + tracklist.size() + " files were processed. Result was saved to " + CSV_FILE);
 	}
 
 	public static void deleteIfExists(String fileName) {
 		File resultFile = new File(fileName);
 		if (resultFile.exists()) {
 			try {
 				FileUtils.forceDelete(resultFile);
 			} catch (IOException e) {
 				LOG.warn("Error when deleting file " + fileName, e);
 			}
 		}
 	}
 
 	public static double[][] prepareSpectrum(final SpectrumData sd) {
 		double[][] result = sd.spectrum;
 		result = DataUtil.smoothHorizontallyMedian(result, TrainDataGenerator.WINDOW);
 		result = DataUtil.shrink(result, sd.framesPerBeat);
 		result = DataUtil.toLogSpectrum(result);
 		result = DataUtil.reduce(result, sd.scaleInfo.octaves);
 		DataUtil.scaleEachTo01(result);
 		return result;
 	}
 
 	public static Chord[] prepareChords(final String binFileName, final SpectrumData sd, double delta) {
 		String track = StringUtils.substringAfterLast(binFileName, PathConstants.SEP);
 		String labFileName = PathConstants.LAB_DIR + track.replace(PathConstants.EXT_WAV + PathConstants.EXT_BIN, PathConstants.EXT_LAB);
 		LabFileReader labReader = new LabFileReader(new File(labFileName));
 		Chord[] result = new Chord[sd.beatTimes.length - 1];
 		for (int i = 0; i < result.length; i++) {
			result[i] = labReader.getChord(sd.beatTimes[i], 0.5);
 		}
 		return result;
 	}
 
 	public TrainDataGenerator(String outputCsvFileName, boolean append) {
 		File file = new File(outputCsvFileName);
 		try {
 			csvOut = FileUtils.openOutputStream(file, append);
 		} catch (IOException e) {
 			LOG.error("Error when creating resulting .csv file", e);
 			System.exit(-2);
 		}
 	}
 
 	@Override
 	public double[][] process(double[][] data) {
 		process(data, new Chord[data.length], OFFSET, data[0].length);
 		return data;
 	}
 
 	public double[][] process(double[][] data, int offset, int components) {
 		process(data, new Chord[data.length], offset, components);
 		return data;
 	}
 
 	protected void process(double[][] data, Chord[] chords, int offset, int components) {
 		if (data == null || chords == null) {
 			LOG.error("data or chords is null");
 			return;
 		}
 		try {
 			for (int i = 0; i < data.length; i++) {
 				double[] row = data[i];
 				if (components != row.length) {
 					row = Arrays.copyOfRange(row, offset, offset + components);
 				}
 				csvOut.write(toByteArray(row, chords[i]));
 			}
 		} catch (IOException e) {
 			LOG.error("Error when writing result", e);
 		} finally {
 			try {
 				csvOut.close();
 			} catch (IOException e) {
 				LOG.error("Error when closing output stream for the resulting file", e);
 			}
 		}
 	}
 
 	public static byte[] toByteArray(double[] ds, Chord chord) throws UnsupportedEncodingException {
 		if (ds == null || ds.length == 0) {
 			return new byte[0];
 		}
 		StringBuilder sb = new StringBuilder();
 		for (int j = 0; j < ds.length; j++) {
 			sb.append(ds[j]);
 			sb.append(DELIMITER);
 		}
 		if (chord != null) {
 			if (chord.isEmpty()) {
 				sb.append('N');
 			} else {
 				Note[] notes = chord.getNotes();
 				String[] labels = new String[notes.length];
 				for (int i = 0; i < notes.length; i++) {
 					labels[i] = notes[i].getShortName();
 				}
 				Arrays.sort(labels, new Comparator<String>() {
 					@Override
 					public int compare(String o1, String o2) {
 						return o1.compareTo(o2);
 					}  });
 				sb.append(StringUtils.join(labels, '-'));
 			}
 		}
 		sb.append("\r\n");
 		return sb.toString().getBytes(ENCODING);
 	}
 
 	public static byte[] toByteArrayForBass(double[] ds, Chord chord) throws UnsupportedEncodingException {
 		if (ds == null || ds.length == 0 || chord == null) {
 			return new byte[0];
 		}
 		StringBuilder sb = new StringBuilder();
 		for (int j = 0; j < ds.length; j++) {
 			sb.append(ds[j]);
 			sb.append(DELIMITER);
 		}
 		if (chord != null) {
 			if (chord.isEmpty()) {
 				sb.append('N');
 			} else {
 				sb.append(chord.getRoot().getShortName());
 			}
 		}
 		sb.append("\r\n");
 		return sb.toString().getBytes(ENCODING);
 	}
 
 }
