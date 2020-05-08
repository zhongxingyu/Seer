 package run;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Scanner;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import parse.Lexer;
 import parse.parser;
 import semantic.Env;
 
 public class SimPL {
 	
 	public static final int MAX_BUF_SIZE = 2048;
 	
 	/**
 	 * the buffer is used to clear a pipe
 	 */
 	private static final byte[] dummyBuf = new byte[MAX_BUF_SIZE];
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			Options options = new Options();
 			// -h parameter
 			options.addOption("h", "help", false, "print usage");
 			// -s parameter
 			options.addOption("s", "shell", false, "interactive shell");
 			// -f file
 			options.addOption("f", "file", true, "execute file");
 			// -o out
 			options.addOption("o", "out", true, "output file");
 			
 			CommandLineParser parser = new PosixParser();
 			CommandLine cmd = parser.parse(options, args);
 			
 			if (cmd.hasOption("h")) {
 				printUsage();
 			} else if (cmd.hasOption("s")) {
 				run(null, null, true);
 			} else if (cmd.hasOption("f")) {
 				String infile = cmd.getOptionValue("f", "");
 				String outfile = null;
 				if (cmd.hasOption("o")) {
 					outfile = cmd.getOptionValue("o");
 				} else {
 					outfile = infile.split("\\.[a-z0-9]+$")[0] + ".rst";
 				}
 				run(infile, outfile, false);
 			} else {
 				printUsage();
 				System.exit(-1);
 			}
 		} catch (ParseException e) {
 			printUsage();
 			System.exit(-1);
 		}
 		System.exit(0);
 	}
 	
 	public static void printUsage() {
 		System.err.println("Usage: SimPL [-s | -f file | -h]");
 	}
 	
 	/**
 	 * run SimPL, read from console or file, output to file or console
 	 * @param infile input file, if null supplied, then stdin
 	 * @param outfile output file, if null supplied, then stdout
 	 */
 	public static void run(String infile, String outfile, boolean printPrompt) {
 		Scanner in = null;
 		PrintWriter out = null;
 		Scanner pipereader = null;
 		PrintWriter pipeWriter = null;
 		try {
 			// create input scanner
 			if (infile == null || infile.isEmpty()) {
 				in = new Scanner(System.in);
 			} else {
 				in = new Scanner(new FileInputStream(infile));
 			}
 			
 			// create output printer
 			if (outfile == null || outfile.isEmpty()) {
 				out = new PrintWriter(System.out);
 			} else {
 				out = new PrintWriter(new FileOutputStream(outfile));
 			}
 			
 			// construct a pipe
 			PipedInputStream pipeinstream = new PipedInputStream(MAX_BUF_SIZE);
 			PipedOutputStream pipeoutstream = new PipedOutputStream(pipeinstream);
 			pipereader = new Scanner(pipeinstream);
 			pipereader.useDelimiter("\\$");
 			pipeWriter = new PrintWriter(pipeoutstream);
 			
 			int bufsize = 0;
 			Queue<Position> scriptStarts = new LinkedList<Position>();
 			scriptStarts.add(Position.ORIGIN_POSITION);
 			int linenum = 0;
 			
 			if (printPrompt) {
 				out.print("SimPL> ");
 			}
 			while (in.hasNextLine()) {
 				// read a line from stdin
 				String line = in.nextLine();
 				// if it is only a blank line and nothing in buffer
 				// then just ingore
 				if (line.trim().isEmpty() && bufsize == 0) {
 					if (printPrompt) {
 						out.print("SimPL> ");
 					}
 					continue;
 				}
 				
 				// add the total buffer size
 				bufsize += line.length() + 1;
 				// should not be more than 2k, if so, then abort
 				if (bufsize > MAX_BUF_SIZE) {
 					bufsize = 0;
 					// clear script positions
 					if (scriptStarts.size() != 1) {
 						scriptStarts.clear();
 						scriptStarts.add(Position.ORIGIN_POSITION);
 					}
 					// clear the pipe
 					pipeWriter.flush();
 					if (pipeinstream.available() != 0) {
 						pipeinstream.read(dummyBuf, 0, MAX_BUF_SIZE);
 					}
 					// reset linenum
 					linenum = 0;
 					// print error message and continue
 					System.err.println("Error: A script should be less than " + MAX_BUF_SIZE + " characters");
 					if (printPrompt) {
 						out.print("SimPL> ");
 					}
 					continue;
 				}
 				
 				// count occurance of $, the number of expressions
 //					dollarCount += countDollar(line);
 				++linenum;
 				countDollar(line, linenum, scriptStarts);
 				// store the line into the pipe to read, including a new line
 				pipeWriter.println(line);
 				
 				// a script will be executed only
 				// when the last character of the line is $
 				// if not, continue to read from stdin
 				if (line.trim().endsWith("$") == false) {
 					continue;
 				}
 				
 				// now execute the scripts
 				pipeWriter.flush();
 				while (scriptStarts.size() > 1) {
 					String script = pipereader.next();
 					Position pos = scriptStarts.remove();
 					
 					// if script is just an empty string, then ingore it
 					if (script.trim().isEmpty()) {
 						continue;
 					}
 					ErrorMessage.init(pos.line, pos.column);
 					runScript(script + "$", out);
 				}
 				
 				// clear script positions
 				scriptStarts.clear();
 				scriptStarts.add(Position.ORIGIN_POSITION);
 				// clear
 				bufsize = 0;
 				// clear the pipe
 				if (pipeinstream.available() != 0) {
 					pipeinstream.read(dummyBuf, 0, MAX_BUF_SIZE);
 				}
 				// reset linenum
 				linenum = 0;
 				// print error message and continue
 				if (printPrompt) {
 					out.print("SimPL> ");
 				}
 			}
			if (printPrompt == false && bufsize != 0) {
				System.err.println("script not ended");
			}
 		} catch (FileNotFoundException e) {
 			System.err.println("cannot open file: " + e.getMessage());
 			System.exit(-1);
 		} catch (IOException e) {
 			System.err.println("Unexpected Error");
 			e.printStackTrace();
 			System.exit(-1);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 			if (out != null) {
 				out.close();
 			}
 			if (pipereader != null) {
 				pipereader.close();
 			}
 			if (pipeWriter != null) {
 				pipeWriter.close();
 			}
 		}
 	}
 	
 	private static class Position {
 		public int line;
 		public int column;
 		
 		public Position(int line, int column) {
 			super();
 			this.line = line;
 			this.column = column;
 		}
 		
 		public static final Position ORIGIN_POSITION = new Position(1, 1);
 	}
 	
 	
 	/**
 	 * run a simpl script
 	 * @param script script string, should end with $
 	 * @return 0 if succeeded otherwise 1
 	 */
 	public static int runScript(String script, PrintWriter out) {
 		int retval = 1;
 		try {
 			parser p = new parser(new Lexer(new ByteArrayInputStream(script.getBytes())));
 			p.parse();
 			Env env = new Env();
 			out.println(p.result.execute(env));
 			retval = 0;
 		} catch (SimplException e) {
 			System.err.println(e.getMessage());
 			System.err.println(ErrorMessage.pos());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return retval;
 	}
 	
 	private static void countDollar(String str, int linenum, Queue<Position> scriptStarts) {
 		for (int i = 0; i < str.length(); ++i) {
 			if (str.charAt(i) == '$') {
 				scriptStarts.add(new Position(linenum, i + 2));
 			}
 		}
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
