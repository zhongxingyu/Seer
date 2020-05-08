 package edu.berkeley.icsi.cdfs.benchmark;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 public final class Benchmark {
 
 	private static final Random RND = new Random();
 
 	public static void main(final String[] args) {
 
 		final Options options = new Options();
 		options.addOption("r", false, "Only read data from the specific path");
 		options.addOption("p", true, "The path to write data to/read data from");
 		options.addOption("c", true, "The compressibility of the generated file");
 		options.addOption("s", true, "The size of the file to be generated in MB");
 
 		final CommandLineParser parser = new PosixParser();
 		final CommandLine cmd;
 		try {
 			cmd = parser.parse(options, args);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		// Read the URL
 		if (!cmd.hasOption("p")) {
 			System.err.println("Please provide a data path");
 			return;
 		}
 
 		final Path path = new Path(cmd.getOptionValue("p"));
 
 		// Read read only parameter
 		final boolean readOnly = cmd.hasOption("r");
 
 		// Determine the size of the data to write
 		int sizeInMB = 1024;

		// The compressibility
		int compressibility = 60;

 		if (!readOnly) {
 			if (cmd.hasOption("s")) {
 				try {
 					sizeInMB = Integer.parseInt(cmd.getOptionValue("s"));
 				} catch (NumberFormatException nfe) {
 					System.err.println("Cannot parse size: " + nfe.getMessage());
 					return;
 				}
 
 				// Check range of number
 				if (sizeInMB < 1) {
 					System.err.println("Size of data to be generated must be at least one MB");
 					return;
 				}
 			}
 
			if (cmd.hasOption("c")) {

				try {
					compressibility = Integer.parseInt(cmd.getOptionValue("c"));
				} catch (NumberFormatException nfe) {
					System.err.println("Cannot parse compressibility: " + nfe.getMessage());
					return;
				}

				if (compressibility < 1 || compressibility > 100) {
					System.err.println("compressibility must be between 0 and 100");
					return;
				}
			}
		}
 
 		// Create CDFS object
 		final Configuration conf = new Configuration();
 		conf.set("fs.cdfs.impl", "edu.berkeley.icsi.cdfs.CDFS");
 
 		FileSystem fs = null;
 		try {
 			fs = path.getFileSystem(conf);
 
 			// Write data
 			if (!readOnly) {
 				write(fs, path, sizeInMB * 1024L * 1024L, compressibility);
 			}
 
 			// Read data
 			read(fs, path);
 
 		} catch (IOException ioe) {
 			System.err.println(ioe.getMessage());
 			return;
 		} finally {
 			if (fs != null) {
 				try {
 					fs.close();
 				} catch (IOException ioe) {
 				}
 			}
 		}
 	}
 
 	private static void write(final FileSystem cdfs, final Path path, final long numberOfBytesToWrite,
 			final int compressibility)
 			throws IOException {
 
 		FSDataOutputStream outputStream = null;
 
 		// Prepare data buffer
 		final byte[] data = new byte[100];
 		for (int i = 0; i < data.length; ++i) {
 			data[i] = '_';
 		}
 		data[99] = '\n';
 		long numberOfBytesWritten = 0L;
 
 		try {
 
 			final long start = System.currentTimeMillis();
 
 			outputStream = cdfs.create(path, true);
 
 			while (numberOfBytesWritten < numberOfBytesToWrite) {
 				prepareData(compressibility, data);
 				outputStream.write(data);
 				numberOfBytesWritten += data.length;
 			}
 
 			final long duration = System.currentTimeMillis() - start;
 			System.out.println("Wrote " + numberOfBytesWritten + " bytes to " + path + " in " + duration + " ms ("
 				+ toMBPerSecond(numberOfBytesWritten, duration) + " MB/sec, compressibility " + compressibility + "%)");
 
 		} finally {
 
 			// Clean up
 			if (outputStream != null) {
 				outputStream.close();
 			}
 		}
 
 	}
 
 	private static double toMBPerSecond(final long numberOfBytesWritten, final long durationInMs) {
 
 		final double numberOfMBWritten = (double) numberOfBytesWritten / (double) (1024L * 1024L);
		return Math.round(numberOfMBWritten / (double) durationInMs * 10000.0) / 10.0;
 	}
 
 	private static void read(final FileSystem cdfs, final Path path) throws IOException {
 
 		FSDataInputStream inputStream = null;
 
 		try {
 
 			byte[] data = new byte[64 * 1024];
 			inputStream = cdfs.open(path);
 
 			long numberOfBytesRead = 0L;
 			final long start = System.currentTimeMillis();
 
 			while (true) {
 
 				final int r = inputStream.read(data);
 				if (r < 0) {
 					break;
 				}
 
 				numberOfBytesRead += r;
 			}
 
 			final long duration = System.currentTimeMillis() - start;
 			System.out.println("Read " + numberOfBytesRead + " bytes from " + path + " in " + duration + " ms ("
 				+ toMBPerSecond(numberOfBytesRead, duration) + " MB/sec)");
 
 		} finally {
 
 			if (inputStream != null) {
 				inputStream.close();
 			}
 		}
 	}
 
 	private static void prepareData(final int compressibility, final byte[] data) {
 
 		int rnd = 0;
 
 		final int n = 100 - compressibility;
 		for (int i = 0; i < n; ++i) {
 
 			if (i % 4 == 0) {
 				rnd = RND.nextInt();
 			}
 
 			rnd = (rnd >>> 8);
 			byte b = (byte) (rnd & 0xff);
 			if (b == 0) {
 				b = 'a';
 			} else if (b == '\n') {
 				b = 'b';
 			}
 
 			data[i] = b;
 		}
 	}
 }
