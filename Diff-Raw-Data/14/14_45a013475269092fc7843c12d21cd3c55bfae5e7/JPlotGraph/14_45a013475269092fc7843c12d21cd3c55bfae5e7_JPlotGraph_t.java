 package edu.wisc.jj;
 
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.NumberTickUnit;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.JFreeChart;
 import org.oc.ocvolume.dsp.featureExtraction;
 import org.apache.batik.dom.GenericDOMImplementation;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import org.apache.batik.svggen.SVGGraphics2D;
 import org.apache.batik.svggen.SVGGraphics2DIOException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import ca.uol.aig.fftpack.RealDoubleFFT;
 
 /**
  * A line graph generator input(command line argument): a file with all
  * path+names of data files output corresponding svg graph in the same directory
  * 
  * @author JJ
  * 
  */
 public class JPlotGraph {
 
 	private static int IMAGE_WIDTH = -1;
 	private static int IMAGE_HEIGHT = -1;
 
 	/**
 	 * a simple java application to display graphs
 	 * 
 	 * @param arg
 	 *            : [0] -- mode of graphing (0 for PCM, 1 for vibration) [1] --
 	 *            file name for both PCM and vibration [2] -- whether axis or
 	 *            not for vibration [size-2, size-1] --- lower and upper bound
 	 *            for vibration
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws IOException {
 		ClassLoader classloader = org.apache.commons.io.IOUtils.class
 				.getClassLoader();
 		java.net.URL res = classloader
 				.getResource("org/apache/commons/io/IOUtils.class");
 		String path = res.getPath();
 		System.out.println("common IOUtils class came from " + path);
 		switch (Integer.valueOf(args[0])) {
 		case 0: // plot PCM audio graph
 			System.out.println("PCM file plotting mode.");
 			// generate the ultimate png that random select 10 files for each
 			// character and plot all the spectrum/magnitude
 			if (Arrays.asList(args).contains("-gen")) {
 				System.out.println("generating list file");
				File audioRootFile = new File("audio_refined_10ms");
 				// generate the a list of file for plotting
 				generatePlottingList(audioRootFile);
 				File curDir = new File(".");
 				// create new filename filter
 				FilenameFilter mFilter = new FilenameFilter() {
 					@Override
 					public boolean accept(File dir, String name) {
 						if (name.lastIndexOf('.') > 0) {
 							// get last index for '.' char
 							int lastIndex = name.lastIndexOf('.');
 							// get extension
 							String str = name.substring(lastIndex);
 							// match path name extension
 							if (str.equals(".list")) {
 								return true;
 							}
 						}
 						return false;
 					}
 				};
 				String[] fileList = curDir.list(mFilter);
 				// the code segment below generates all the plots into one
 				// single PNG file
 				if (Arrays.asList(args).contains("-one")) {
 					System.out
 							.println("generate all spectrum plots into ONE png file");
 					List<byte[]> imageByteList = new ArrayList<byte[]>();
 					// generate image bytes
 					for (String fileString : fileList) {
 						File listFile = new File(fileString);
 						List<String> mStringList = FileUtils
 								.readLines(listFile);
 						String imageFileName = String.valueOf(fileString
 								.substring(0, fileString.indexOf(".")));
 						XYSeriesCollection imageDataSet = generateXYSeriesCollection(
 								args, mStringList, imageFileName);
 						byte[] imageByte = generateImageByte(args,
								imageDataSet, imageFileName, 0, 2100000, 20);
 						imageByteList.add(imageByte);
 					}
 					// generate the final png file
 					if (Arrays.asList(args).contains("-spectrum")) {
 						generateImageFromByte(imageByteList, "all_spectrum.png");
 					} else if (Arrays.asList(args).contains("-mfcc")) {
 						generateImageFromByte(imageByteList, "all_mfcc.png");
 					} else {
 						generateImageFromByte(imageByteList,
 								"all_magnitude.png");
 					}
 				} else { // generate one PNG file for each character folder
 					System.out
 							.println("generate 10 plots into 1 graph for a particular character. Generate for 26 characters");
 					for (String fileString : fileList) {
 						File listFile = new File(fileString);
 						List<String> mStringList = FileUtils
 								.readLines(listFile);
 						String imageFileName = String.valueOf(fileString
 								.substring(0, fileString.indexOf(".")));
 						if (Arrays.asList(args).contains("-spectrum")) {
 							imageFileName = imageFileName + "_spectrum";
 						} else if (Arrays.asList(args).contains("-mfcc")) {
 							imageFileName = imageFileName + "_mfcc";
 						}
 						generateInOneFile(args, mStringList, imageFileName);
 					}
 				}
 			} else {
 				System.out.println("plotting files specified by the list file");
 				File listFile = new File(args[1]);
 				List<String> PCMFilePath = new ArrayList<String>();
 				BufferedReader PCMbr = null;
 				// read in the list of files for generating graphs
 				try {
 					String sCurrentLine;
 					PCMbr = new BufferedReader(new FileReader(listFile));
 					while ((sCurrentLine = PCMbr.readLine()) != null) {
 						System.out.println(sCurrentLine);
 						PCMFilePath.add(sCurrentLine);
 					}
 				} catch (IOException e) {
 					System.out
 							.println("io exception while reading in pcm files");
 				} finally {
 					try {
 						if (PCMbr != null)
 							PCMbr.close();
 					} catch (IOException ex) {
 					}
 				}
 				if (Arrays.asList(args).contains("-one")) {
 					// plot all the given file in the file list into one PNG
 					// file
 					generateInOneFile(args, PCMFilePath, "test");
 				} else {
 					generateMultipleFiles(args, PCMFilePath);
 				}
 
 			}
 			break;
 		case 1:// plot vibration data graph for gyro,accel,linear accel...
 			System.out.println("vibration data mode");
 			ArrayList<String> filePath = new ArrayList<String>();
 			BufferedReader br = null;
 			// read in the list of files for generating graphs
 			try {
 				String sCurrentLine;
 				br = new BufferedReader(new FileReader(args[1]));
 				while ((sCurrentLine = br.readLine()) != null) {
 					System.out.println(sCurrentLine);
 					filePath.add(sCurrentLine);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (br != null)
 						br.close();
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 			for (String fileS : filePath) {
 				// if not empty or null,then create a plot file
 				if (!((fileS == null) || (fileS.isEmpty()))) {
 					File dataFile = new File(fileS);
 					// the whole data set from file
 					ArrayList<float[]> dataSet = new ArrayList<float[]>();
 					// read the file into data set
 					Scanner mScanner = null;
 					try {
 						mScanner = new Scanner(dataFile);
 						while (mScanner.hasNextLine()) {
 							// only read in lines starting with a float number
 							if (mScanner.hasNextFloat()) {
 								// data Entry for each line
 								float[] dataEntry = new float[3];
 								// x axis
 								dataEntry[0] = mScanner.nextFloat();
 								// y axis
 								dataEntry[1] = mScanner.nextFloat();
 								// z axis
 								dataEntry[2] = mScanner.nextFloat();
 								dataSet.add(dataEntry);
 								// inc to next line
 								mScanner.nextLine();
 							} else
 								mScanner.nextLine();
 						}
 					} catch (FileNotFoundException e) {
 						System.out.println("dataFile doesn't exist!");
 					} finally {
 						if (mScanner != null)
 							mScanner.close();
 					}
 					// create data sample series
 					XYSeries x_data = new XYSeries("X axis");
 					XYSeries y_data = new XYSeries("Y axis");
 					XYSeries z_data = new XYSeries("Z axis");
 					if (Arrays.asList(args).contains("-mag")) {
 						System.out.println("plotting the magnitude");
 						for (int index = 0; index < dataSet.size(); index++) {
 							double squareSum = dataSet.get(index)[0]
 									* dataSet.get(index)[0]
 									+ dataSet.get(index)[1]
 									* dataSet.get(index)[1]
 									+ dataSet.get(index)[2]
 									* dataSet.get(index)[2];
 							double mag = Math.pow(squareSum, 0.5);
 							x_data.add(index, mag);
 							y_data.add(index, mag);
 							z_data.add(index, mag);
 						}
 					} else {
 						for (int index = 0; index < dataSet.size(); index++) {
 							x_data.add(index, dataSet.get(index)[0]);
 							y_data.add(index, dataSet.get(index)[1]);
 							z_data.add(index, dataSet.get(index)[2]);
 						}
 					}
 
 					double lowBound;
 					double highBound;
 					try {
 						// range for the vertical axis
 						lowBound = Double.parseDouble(args[args.length - 2]);
 						highBound = Double.parseDouble(args[args.length - 1]);
 					} catch (NumberFormatException e) {
 						// user failed to enter in the range. Use default ones
 						System.out
 								.println("no range specified. using default ones");
 						lowBound = 0.0;
 						highBound = 0.0;
 					}
 					// if split the graph into different axis readings
 					if (Arrays.asList(args).contains("-axis")) {
 						XYSeriesCollection svgXYDataSeriesX = new XYSeriesCollection();
 						XYSeriesCollection svgXYDataSeriesY = new XYSeriesCollection();
 						XYSeriesCollection svgXYDataSeriesZ = new XYSeriesCollection();
 						svgXYDataSeriesX.addSeries(x_data);
 						svgXYDataSeriesY.addSeries(y_data);
 						svgXYDataSeriesZ.addSeries(z_data);
 						// plotChartAxisInOne(svgXYDataSeriesX,
 						// svgXYDataSeriesY,
 						// svgXYDataSeriesZ, fileS, "x", lowBound,
 						// highBound);
 						generatePNGChart(args, svgXYDataSeriesX, fileS
 								+ "_x.png", lowBound, highBound, 0.0);
 						generatePNGChart(args, svgXYDataSeriesY, fileS
 								+ "_y.png", lowBound, highBound, 0.0);
 						generatePNGChart(args, svgXYDataSeriesZ, fileS
 								+ "_z.png", lowBound, highBound, 0.0);
 						// plotChart(svgXYDataSeriesZ, fileS, "z", lowBound,
 						// highBound);
 					} else {
 						/* Add all XYSeries to XYSeriesCollection */
 						// XYSeriesCollection implements XYDataset
 						XYSeriesCollection svgXYDataSeries = new XYSeriesCollection();
 						// add series using addSeries method
 						svgXYDataSeries.addSeries(x_data);
 						svgXYDataSeries.addSeries(y_data);
 						svgXYDataSeries.addSeries(z_data);
 						// plot chart
 						generatePNGChart(args, svgXYDataSeries, fileS + ".png",
 								lowBound, highBound, 0.0);
 						// plotChart(svgXYDataSeries, fileS, "all", lowBound,
 						// highBound);
 					}
 				}
 			}
 			break;
 		}
 	}
 
 	/**
 	 * generate list file for all character folders in audio training folder
 	 * 
 	 * @param audioRootFile
 	 * @throws IOException
 	 */
 	public static void generatePlottingList(File audioRootFile)
 			throws IOException {
 		File[] charFolder = audioRootFile.listFiles();
 		if (charFolder != null) {
 			System.out.println("char folders: " + Arrays.toString(charFolder));
 			for (File folder : charFolder) {
 				File listFile = new File(folder.getName() + ".list");
 				if (listFile.exists()) {
 					listFile.delete();
 				}
 				File[] fileList = folder.listFiles();
 				// shuffle the array
 				Collections.shuffle(Arrays.asList(fileList), new Random(System
 						.currentTimeMillis()));
 				int count = 0;
 				for (File mFile : fileList) {
 					count++;
 					String mPath = mFile.getAbsolutePath() + "\n";
 					FileUtils.write(listFile, mPath, "UTF-8", true);
 					if (count >= 10) {
 						break;
 					}
 				}
 			}
 		} else {
 			System.out
 					.println("result returning from listing files in audio folder is null");
 			System.exit(0);
 		}
 	}
 
 	private static void generateMultipleFiles(String[] args,
 			List<String> PCMFilePath) throws IOException {
 		for (String fileS : PCMFilePath) {
 			// if not empty or null,then create a plot file
 			if (!((fileS == null) || (fileS.isEmpty()))) {
 				short[] pcmData = readInPCMFile(fileS);
 				// customized to generate a custom graph
 				// short[] allData=readInPCMFile(fileS);
 				// short[] pcmData;
 				// pcmData=Arrays.copyOfRange(allData, 1031631 , 1033700);
 				if (Arrays.asList(args).contains("-spectrum")) {
 					System.out.println("plotting spectrum");
 					// create data sample series
 					XYSeries audioData = new XYSeries("spectrum magnitude");
 					double[] fftData = getPCMFFTData(pcmData);
 					for (int index = 0; index < 100; index++) {
 						double coefficient = 44100.0 / (double) fftData.length;
 						double freq = (double) index * coefficient;
 						audioData.add(freq, (double) fftData[index]);
 					}
 					XYSeriesCollection svgXYDataSeriesAudio = new XYSeriesCollection();
 					svgXYDataSeriesAudio.addSeries(audioData);
 					// plot chart
 					generatePNGChart(args, svgXYDataSeriesAudio, fileS
 							+ "_spectrum.png", 0.0, 0.0, 20);
 				} else {
 					System.out
 							.println("plotting magnitude.  The graph range would be from Short.min to Short.max");
 					// create data sample series
 					XYSeries audioData = new XYSeries("magnitude");
 					for (int index = 0; index < pcmData.length; index++) {
 						audioData.add(index, (double) pcmData[index]);
 					}
 					XYSeriesCollection svgXYDataSeriesAudio = new XYSeriesCollection();
 					svgXYDataSeriesAudio.addSeries(audioData);
 					// plot chart
 					generatePNGChart(args, svgXYDataSeriesAudio,
 							fileS + ".png", Short.MIN_VALUE, Short.MAX_VALUE,
 							0.0);
 				}
 			}
 		}
 	}
 
 	/**
 	 * generate XYSeriesCollection from data specified in the PCMFilePath
 	 * 
 	 * @param args
 	 *            -- the command line arguments user has inputed. test whether
 	 *            has -spectrum or not
 	 * @param PCMFilePath
 	 *            -- list of file paths to read data from
 	 * @param fileName
 	 *            : the name of file
 	 * @throws IOException
 	 */
 	private static XYSeriesCollection generateXYSeriesCollection(String[] args,
 			List<String> PCMFilePath, String fileName) throws IOException {
 		boolean plotSpect = false;
 		boolean plotMFCC = false;
 		if (Arrays.asList(args).contains("-spectrum")) {
 			plotSpect = true;
 			System.out.println("plotting spectrum: " + fileName);
 		} else if (Arrays.asList(args).contains("-mfcc")) {
 			plotMFCC = true;
 			System.out.println("plotting mfcc: " + fileName);
 		} else {
 			System.out
 					.println("plotting magnitude. The graph range would be from Short.min to Short.max. file name: "
 							+ fileName);
 		}
 		XYSeriesCollection svgXYDataSeriesAudio = new XYSeriesCollection();
 		for (String fileS : PCMFilePath) {
 			// if not empty or null,then create a plot file
 			if (!((fileS == null) || (fileS.isEmpty()))) {
 				short[] pcmData = readInPCMFile(fileS);
 				if (plotSpect) {
 					// create data sample series
 					XYSeries audioData = new XYSeries(fileS + "_spectrum");
 					double[] fftData = getPCMFFTData(pcmData);
 					int index = 0;
 					double freqResolution = 44100.0 / (double) fftData.length;
 					double freq = (double) index * freqResolution
 							+ freqResolution / 2.0;
 					// when frequency is less than 2000 hertz
 					while (freq < 2000) {
 						// find the middle frequency
 						audioData.add(freq, (double) fftData[index]);
 						index++;
 						freq = (double) index * freqResolution + freqResolution
 								/ 2.0;
 					}
 					svgXYDataSeriesAudio.addSeries(audioData);
 				} else if (plotMFCC) {
 					XYSeries audioData = new XYSeries(fileS + "_mfcc");
 					// get mfcc values from original pcm data
 					double[] mfccData = getMFCC(pcmData);
 					for (int index = 0; index < mfccData.length; index++) {
 						audioData.add(index, mfccData[index]);
 					}
 					svgXYDataSeriesAudio.addSeries(audioData);
 				} else {
 					// create data sample series
 					XYSeries audioData = new XYSeries(fileS + "magnitude");
 					for (int index = 0; index < pcmData.length; index++) {
 						audioData.add(index, (double) pcmData[index]);
 					}
 					svgXYDataSeriesAudio.addSeries(audioData);
 				}
 			} else {
 				System.out.println("empty or null file: " + fileS);
 				System.exit(0);
 			}
 		}
 		return svgXYDataSeriesAudio;
 
 	}
 
 	/**
 	 * generate all the plots specified by PCMFilePath into one PNG file
 	 * 
 	 * @param args
 	 *            : the command line argument user inputs. determine whether
 	 *            plotting spectrum or not
 	 * @param PCMFilePath
 	 * @param fileName
 	 * @throws IOException
 	 */
 	private static void generateInOneFile(String[] args,
 			List<String> PCMFilePath, String fileName) throws IOException {
 		XYSeriesCollection svgXYDataSeriesAudio = generateXYSeriesCollection(
 				args, PCMFilePath, fileName);
 		// plot chart
 		if (Arrays.asList(args).contains("-spectrum")) {// plotting fft data
 			generatePNGChart(args, svgXYDataSeriesAudio, fileName + ".png",
					0.0, 2100000.0, 20.0);
 		} else if (Arrays.asList(args).contains("-mfcc")) {
 			generatePNGChart(args, svgXYDataSeriesAudio, fileName + ".png",
 					0.0, 0.0, 0.0);
 		} else {
 			generatePNGChart(args, svgXYDataSeriesAudio, fileName + ".png",
 					Short.MIN_VALUE, Short.MAX_VALUE, 0.0);
 		}
 	}
 
 	/**
 	 * plot chart for the XYSeriesCollection
 	 * 
 	 * @param svgXYDataSeriesC
 	 * @param fileS
 	 * @param axisS
 	 * @param rangeLow
 	 * @param rangeHigh
 	 */
 	private static void plotChart(XYSeriesCollection svgXYDataSeriesC,
 			String fileS, String axisS, double rangeLow, double rangeHigh) {
 		String chartTitle = fileS + "_" + axisS + "_readings";
 		// Use createXYLineChart to create the chart
 		JFreeChart XYLineChart = ChartFactory.createXYLineChart(chartTitle,
 				"time", "rad/s", svgXYDataSeriesC, PlotOrientation.VERTICAL,
 				true, true, false);
 		if (rangeLow != 0.0 || rangeHigh != 0.0) {
 			NumberAxis yaxis = (NumberAxis) XYLineChart.getXYPlot()
 					.getRangeAxis();
 			yaxis.setRange(rangeLow, rangeHigh);
 		}
 		/* Write Chart output as SVG */
 		/* Get DOM Implementation */
 		DOMImplementation mySVGDOM = GenericDOMImplementation
 				.getDOMImplementation();
 		/* create Document object */
 		Document document = mySVGDOM.createDocument(null, "svg", null);
 		/* Create SVG Generator */
 		SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
 		/* Render chart as SVG 2D Graphics object */
 		XYLineChart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 1200,
 				600), null);
 		/* Write output to file */
 		try {
 			String outputFileName = fileS + "_" + axisS + "_graph" + ".svg";
 			my_svg_generator.stream(outputFileName);
 		} catch (SVGGraphics2DIOException e) {
 			System.out.println("fail to create svg file");
 		}
 	}
 
 	/**
 	 * plot chart for each individual axis
 	 * 
 	 * @param svgXYDataSeriesX
 	 * @param svgXYDataSeriesY
 	 * @param svgXYDataSeriesZ
 	 * @param fileS
 	 * @param axisS
 	 * @param rangeLow
 	 * @param rangeHigh
 	 */
 	private static void plotChartAxisInOne(XYSeriesCollection svgXYDataSeriesX,
 			XYSeriesCollection svgXYDataSeriesY,
 			XYSeriesCollection svgXYDataSeriesZ, String fileS, String axisS,
 			double rangeLow, double rangeHigh) {
 		String chartTitle = fileS + "_" + axisS + "_readings";
 		// Use createXYLineChart to create the chart
 		JFreeChart XYLineChartX = ChartFactory.createXYLineChart(chartTitle,
 				"time", "rad/s", svgXYDataSeriesX, PlotOrientation.VERTICAL,
 				true, true, false);
 		JFreeChart XYLineChartY = ChartFactory.createXYLineChart(chartTitle,
 				"time", "rad/s", svgXYDataSeriesY, PlotOrientation.VERTICAL,
 				true, true, false);
 		JFreeChart XYLineChartZ = ChartFactory.createXYLineChart(chartTitle,
 				"time", "rad/s", svgXYDataSeriesZ, PlotOrientation.VERTICAL,
 				true, true, false);
 		if (rangeLow != 0.0 || rangeHigh != 0.0) {
 			NumberAxis yaxis = (NumberAxis) XYLineChartX.getXYPlot()
 					.getRangeAxis();
 			yaxis.setRange(rangeLow, rangeHigh);
 		}
 
 		/* Write Chart output as SVG */
 		/* Get DOM Implementation */
 		DOMImplementation mySVGDOM = GenericDOMImplementation
 				.getDOMImplementation();
 		/* create Document object */
 		Document document = mySVGDOM.createDocument(null, "svg", null);
 		/* Create SVG Generator */
 		SVGGraphics2D my_svg_generator = new SVGGraphics2D(document);
 		/* Render chart as SVG 2D Graphics object */
 		XYLineChartX.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 1200,
 				600), null);
 		XYLineChartY.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 1200,
 				600), null);
 		XYLineChartZ.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 1200,
 				600), null);
 		/* Write output to file */
 		try {
 			String outputFileName = fileS + "_" + axisS + "_graph" + ".svg";
 			my_svg_generator.stream(outputFileName);
 		} catch (SVGGraphics2DIOException e) {
 			System.out.println("fail to create svg file");
 		}
 	}
 
 	/**
 	 * generate image bytes given XYSeriesCollection data
 	 * 
 	 * @param dataSet
 	 * @param fileName
 	 * @param graphMin
 	 * @param graphMax
 	 * @return
 	 * @throws IOException
 	 */
 	private static byte[] generateImageByte(String[] args,
 			XYSeriesCollection dataSet, String fileName, double graphMin,
 			double graphMax, double xAxisUnit) throws IOException {
 		String chartTitle = fileName + "_readings";
 		String xAxisLabel = "time";
 		String yAxisLabel = "magnitude";
 		if (Arrays.asList(args).contains("-spectrum")) {
 			IMAGE_WIDTH = 4000;
 			IMAGE_HEIGHT = 700;
 			xAxisLabel = "frequency";
 			chartTitle = chartTitle + "_spectrum";
 		} else if (Arrays.asList(args).contains("-mfcc")) {
 			IMAGE_WIDTH = 800;
 			IMAGE_HEIGHT = 600;
 			xAxisLabel = "index";
 			yAxisLabel = "mfcc";
			graphMin=0.0;
			graphMax=0.0;
 			chartTitle = chartTitle + "_mfcc";
 		} else {
 			IMAGE_WIDTH = 4000;
 			IMAGE_HEIGHT = 700;
			graphMin=Short.MIN_VALUE;
			graphMax=Short.MAX_VALUE;
 		}
 		// Use createXYLineChart to create the chart
 		JFreeChart XYLineChart = ChartFactory.createXYLineChart(chartTitle,
 				xAxisLabel, yAxisLabel, dataSet, PlotOrientation.VERTICAL,
 				true, true, false);
 		NumberAxis yaxis = (NumberAxis) XYLineChart.getXYPlot().getRangeAxis();
 		NumberAxis xaxis = (NumberAxis) XYLineChart.getXYPlot().getDomainAxis();
 		// if all 0, then default range
 		if (!(graphMin == 0 && graphMax == 0)) {
 			yaxis.setRange(graphMin, graphMax);
 		}
 		if (xAxisUnit != 0.0) {
 			xaxis.setTickUnit(new NumberTickUnit(xAxisUnit));
 		}
 		BufferedImage objBufferedImage = XYLineChart.createBufferedImage(
 				IMAGE_WIDTH, IMAGE_HEIGHT);
 		ByteArrayOutputStream bas = new ByteArrayOutputStream();
 		try {
 			ImageIO.write(objBufferedImage, "png", bas);
 		} catch (IOException e) {
 			System.out.println("fail to write the image to bytes");
 		}
 		byte[] byteArray = bas.toByteArray();
 		return byteArray;
 	}
 
 	/**
 	 * generate png image from XYSeriesCollection data by calling
 	 * generateImageByte first to get image bytes
 	 * 
 	 * @param dataSet
 	 * @param fileName
 	 * @param graphMin
 	 *            : the minimum value for y axis
 	 * @param graphMax
 	 *            : the max value for y axis
 	 * @param xAxisUnit
 	 *            : how much units there are between 2 xAxis label
 	 * @throws IOException
 	 */
 	private static void generatePNGChart(String[] args,
 			XYSeriesCollection dataSet, String fileName, double graphMin,
 			double graphMax, double xAxisUnit) throws IOException {
 		byte[] byteArray = generateImageByte(args, dataSet, fileName, graphMin,
 				graphMax, xAxisUnit);
 		InputStream in = new ByteArrayInputStream(byteArray);
 		BufferedImage image = ImageIO.read(in);
 		File outputfile = new File(fileName);
 		ImageIO.write(image, "png", outputfile);
 		in.close();
 	}
 
 	/**
 	 * generate one image from a list of image bytes. Used in the mode : -gen
 	 * -one
 	 * 
 	 * @param imageByteList
 	 * @param fileName
 	 * @throws IOException
 	 */
 	private static void generateImageFromByte(List<byte[]> imageByteList,
 			String fileName) throws IOException {
 		BufferedImage result = new BufferedImage(IMAGE_WIDTH,
 				(IMAGE_HEIGHT + 20) * imageByteList.size(),
 				BufferedImage.TYPE_INT_RGB);
 		Graphics g = result.getGraphics();
 		int lastYCoordinate = 0;
 		for (byte[] mImageByte : imageByteList) {
 			InputStream in = new ByteArrayInputStream(mImageByte);
 			BufferedImage image = ImageIO.read(in);
 			if (!g.drawImage(image, 0, lastYCoordinate, null)) {
 				System.out
 						.println("draw image failed in generateImageFromByte");
 				in.close();
 				System.exit(0);
 			} else { // if succeed
 				lastYCoordinate = lastYCoordinate + image.getHeight() + 20;
 				in.close();
 			}
 		}
 		File outputfile = new File(fileName);
 		ImageIO.write(result, "png", outputfile);
 	}
 
 	/**
 	 * read in the PCM file and return an array of shorts of all its data
 	 * 
 	 * @param fileName
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	private static short[] readInPCMFile(String fileName)
 			throws FileNotFoundException {
 		File pcmFile = new File(fileName);
 		String extension = "";
 		int i = fileName.lastIndexOf('.');
 		if (i > 0) {
 			extension = fileName.substring(i + 1);
 		}
 		// String[] nameAndExtension = fileName.split(".");
 		// String extension=nameAndExtension[nameAndExtension.length-1];
 		short[] pcmFileShorts = null;
 		if (!extension.equals("pcm")) {
 			System.out.println("not a PCM file");
 			System.exit(0);
 		} else {// read in PCM files
 			byte[] pcmFileBytes = null;
 			InputStream in = null;
 			try {
 				in = FileUtils.openInputStream(pcmFile);
 				pcmFileBytes = IOUtils.toByteArray(in);
 			} catch (IOException e) {
 				System.out.print("pcm file reading io error");
 			} finally {
 				IOUtils.closeQuietly(in);
 			}
 
 			// if indeed are shorts
 			int byteLength = pcmFileBytes.length;
 			if (byteLength % 2 == 0) {
 				pcmFileShorts = new short[byteLength / 2];
 				for (int index = 0; index < pcmFileBytes.length; index = index + 2) {
 					// use big-endian, most significant bytes at the smallest
 					// addr
 					byte[] bytesTwo = { pcmFileBytes[index],
 							pcmFileBytes[index + 1] };
 					// convert to short
 					short mShort = ByteBuffer.wrap(bytesTwo).asShortBuffer()
 							.get();
 					pcmFileShorts[index / 2] = mShort;
 				}
 			} else {
 				System.out
 						.println("pcm file byte's length is not even -- cannot cast to shorts");
 				System.exit(0);
 			}
 		}
 		return pcmFileShorts;
 	}
 
 	/**
 	 * wrapper class of getPCMFFTData (double[])
 	 * 
 	 * @param pcmData
 	 * @return
 	 */
 	private static double[] getPCMFFTData(short[] pcmData) {
 		double[] audioDataDouble = new double[pcmData.length];
 		for (int index = 0; index < pcmData.length; index++) {
 			audioDataDouble[index] = (double) pcmData[index];
 		}
 		return getPCMFFTData(audioDataDouble);
 	}
 
 	/**
 	 * perform FFT on audioData. return the power spectrum
 	 * 
 	 * @param audioData
 	 * @return
 	 */
 	private static double[] getPCMFFTData(double[] audioDataDouble) {
 		int dataLength = audioDataDouble.length;
 		RealDoubleFFT ftEngine = new RealDoubleFFT(dataLength);
 		// coefficient returned: 0 -- 0th bin(real,0); 1,2 -- 1th
 		// bin(real,imag); 3,4 -- 2th bin(real,imag)....
 		// for even, n -- n/2 bin(real,0)
 		ftEngine.ft(audioDataDouble);
 		// get the maginude from FFT coefficients
 		double[] spectrum;
 		if (dataLength % 2 != 0) {// if odd
 			spectrum = new double[(dataLength + 1) / 2];
 			spectrum[0] = Math
 					.pow(audioDataDouble[0] * audioDataDouble[0], 0.5);// dc
 			// component
 			for (int index = 1; index < dataLength; index = index + 2) {
 				// magnitude =re*re + im*im
 				double mag = audioDataDouble[index] * audioDataDouble[index]
 						+ audioDataDouble[index + 1]
 						* audioDataDouble[index + 1];
 				spectrum[(index + 1) / 2] = Math.pow(mag, 0.5);
 			}
 		} else {// if even
 			spectrum = new double[dataLength / 2 + 1];
 			spectrum[0] = Math
 					.pow(audioDataDouble[0] * audioDataDouble[0], 0.5);// dc
 			// component.
 			// real only
 			for (int index = 1; index < dataLength - 1; index = index + 2) {
 				// magnitude =re*re + im*im
 				double mag = audioDataDouble[index] * audioDataDouble[index]
 						+ audioDataDouble[index + 1]
 						* audioDataDouble[index + 1];
 				spectrum[(index + 1) / 2] = Math.pow(mag, 0.5);
 			}
 			// dc component. real only
 			spectrum[spectrum.length - 1] = Math.pow(
 					audioDataDouble[dataLength - 1]
 							* audioDataDouble[dataLength - 1], 0.5);
 		}
 		return spectrum;
 	}
 
 	/**
 	 * generate hanning window function
 	 * 
 	 * @param windowSize
 	 * @return
 	 */
 	private static double[] hanning(int windowSize) {
 		double h_wnd[] = new double[windowSize]; // Hanning window
 		for (int i = 0; i < windowSize; i++) { // calculate the hanning window
 			h_wnd[i] = 0.5 * (1 - Math
 					.cos(2.0 * Math.PI * i / (windowSize - 1)));
 		}
 		return h_wnd;
 	}
 
 	/**
 	 * apply desired window function to the signal for fft
 	 * 
 	 * @param signal
 	 *            : the signal for fft
 	 * @param window
 	 *            : window function
 	 * @return the array after being applied the window
 	 */
 	private static double[] applyWindowFunc(double[] signal, double[] window) {
 		double[] result = new double[signal.length];
 		for (int i = 0; i < window.length; i++) {
 			result[i] = signal[i] * window[i];
 		}
 		return result;
 	}
 
 	private static double[] applyWindowFunc(short[] signal, double[] window) {
 		double[] result = new double[signal.length];
 		for (int i = 0; i < window.length; i++) {
 			result[i] = (double) signal[i] * window[i];
 		}
 		return result;
 	}
 
 	/**
 	 * get the magnitude of the vibration.
 	 * 
 	 * @param data
 	 * @return the magnitude array
 	 */
 	private double[] getMagnitude(double[][] data) {
 		double[] magResult = new double[data[0].length];
 		for (int row = 0; row < data[0].length; row++) {
 			double squareSum = 0.0;
 			for (int column = 0; column < data.length; column++) {
 				squareSum += data[column][row] * data[column][row];
 			}
 			double mag = Math.pow(squareSum, 0.5);
 			magResult[row] = mag;
 		}
 		return magResult;
 	}
 
 	public static double[] getMFCC(short[] originalShortData) {
 		final int SAMPLE_RATE = 44100;
 		featureExtraction mFeatureExtraction = new featureExtraction();
 		// Mel Filtering
 		int cbin[] = mFeatureExtraction.fftBinIndices(SAMPLE_RATE,
 				originalShortData.length);
 		// apply hanning window
 		int windowSize = originalShortData.length;
 		double[] windowedData = applyWindowFunc(originalShortData,
 				hanning(windowSize));
 		double[] spectrum = getPCMFFTData(windowedData);
 		// get Mel Filterbank
 		double fbank[] = mFeatureExtraction.melFilter(spectrum, cbin);
 		// Non-linear transformation
 		double f[] = mFeatureExtraction.nonLinearTransformation(fbank);
 		// Cepstral coefficients
 		double cepc[] = mFeatureExtraction.cepCoefficients(f);
 		// Add resulting MFCC to array
 		double[] mfccFeatures = new double[mFeatureExtraction.numCepstra];
 		for (int i = 0; i < mFeatureExtraction.numCepstra; i++) {
 			mfccFeatures[i] = cepc[i];
 		}
 		// double[][] mfccFeatures=mFeatureExtraction.process(originalShortData,
 		// 44100);
 		return mfccFeatures;
 	}
 
 }
