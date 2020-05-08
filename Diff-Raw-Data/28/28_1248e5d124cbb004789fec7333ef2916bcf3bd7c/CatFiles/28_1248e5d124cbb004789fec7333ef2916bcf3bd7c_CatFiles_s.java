 import java.util.*;
 import java.io.*;
 
 public class CatFiles {
 	private List<String> inputArgs;
 	private String outputArg;
 
 	private OutputStream outputStream;
 
 	private boolean append;
 	private boolean noclobber;
 
 	public static void main(String[] args) {
 		try {
 			CatFiles cat = new CatFiles(args);
 			cat.execute();
 		} catch (InvalidUsageException e) {
 			System.err.println(e.getMessage());
 			usage();
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("I/O error");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	public CatFiles(String[] argv) throws InvalidUsageException {
 		class Args {
 			public ArrayList<String> inputArgs;
 			public String outputArg;
 			public Args() {
 				inputArgs = new ArrayList<String>();
 				outputArg = null;
 			}
 			public void add(String arg) {
 				if (outputArg != null) {
 					inputArgs.add(outputArg);
 				}
 				outputArg = arg;
 			}
 		}
 		Args args = new Args();
 		append = noclobber = false;
 		boolean onlyfiles = false;
 		for (int i = 0; i < argv.length; ++i) {
 			String arg = argv[i];
 			if (onlyfiles) {
 				args.add(arg);
 				continue;
 			}
 			if (arg.equals("--")) {
 				onlyfiles = true;
 				continue;
 			}
			if (arg.charAt(0) == '-') {
 				if (arg.equals("-a")) {
 					append = true;
 				} else if (arg.equals("-nc")) {
 					noclobber = true;
 				} else {
 					throw new InvalidUsageException("Unrecognised option: "+arg);
 				}
 				continue;
 			}
 			args.add(arg);
 		}
 		inputArgs = args.inputArgs;
 		outputArg = args.outputArg;
 	}
 
 	public void execute() throws IOException, InvalidUsageException {
 		try {
 			outputStream = outputFactory(outputArg);
 		} catch (IOException e) {
 			throw new InvalidUsageException("Couldn't open "+outputArg+" for writing");
 		}
		System.err.println("Opened output file "+outputArg);
 		for (String input : inputArgs) {
 			InputStream r;
 			try {
 				r = inputFactory(input);
 			} catch (IOException e) {
 				System.err.println("Couldn't open "+input+" for reading");
 				continue;
 			}
			System.out.println("Opened input file "+input);
 			try {
				copy(r, outputStream);
 				r.close();
 			} catch (IOException e) {
 				System.err.println("Couldn't copy "+input+" to output");
 			}
 		}
 		outputStream.close();
 	}
 
	private static void copy(InputStream input, OutputStream output) throws IOException {
 		byte[] buf = new byte[1024];
 		int totes = 0;
 		while (true) {
			int read = input.read(buf);
 			if (read == -1) break;
 			totes += read;
			output.write(buf, 0, read);
 		}
		System.out.println("Wrote "+totes+" bytes");
 	}
 
 	private static InputStream inputFactory(String inputfile) throws IOException {
 		if (inputfile.equals("-")) {
 			return System.in;
 		} else {
 			return new FileInputStream(inputfile);
 		}
 	}
 
 	private static OutputStream outputFactory(String outputfile) throws IOException, InvalidUsageException {
 		if (outputfile == null) {
 			throw new InvalidUsageException("Which file?");
 		}
 		if (outputfile.equals("-")) {
 			return System.out;
 		} else {
 			return new FileOutputStream(outputfile);
 		}
 	}
 
 	private static void usage() {
 		System.err.println("usage: java CatFiles [-a|-nc] [--] input [input [...]] output");
 		System.err.println("If input is -, read from standard input. If output is -, write to standard output.");
 	}
 }
 
 class InvalidUsageException extends Exception {
 	public InvalidUsageException(String message) {
 		super(message);
 	}
 }
