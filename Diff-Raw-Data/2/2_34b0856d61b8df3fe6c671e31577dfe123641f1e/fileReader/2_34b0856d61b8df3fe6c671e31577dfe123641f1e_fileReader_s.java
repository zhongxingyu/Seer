 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 /**
  * This class represents a reader that redirects STD I/O to current directory
  * and reads/writes files in current directory.
  */
 public class fileReader
 {
 	private final LinkedList<String> list = new LinkedList<String>();
 	private final Set<String> set = new LinkedHashSet<String>();
 	private final LinkedList<String> tokens = new LinkedList<String>();
 	private final String filename;
 	private final Map<String,PrintStream> stream = new HashMap<String,PrintStream>();
 
 	/**
 	 * Constructs a reader that looks in current directory for the given
 	 * filename. It also populates the list and set with file lines. Note: It
 	 * skips empty lines. trims and lowerCase() all nonempty lines.
 	 * 
 	 * @param s
 	 *            The name of file to read
 	 * @param extension
 	 *            The extension of the file
 	 * @pre. file is in current directory
 	 * @exception IllegalArgumentException
 	 *                throws IllegalArgumentException if cannot read filename
 	 */
 	public fileReader(final String s, final String extension) throws IllegalArgumentException
 	{
 
 		this.filename = "" + System.getProperty("user.dir") + "\\" + s + "."
 				+ extension;
 		final File fieldFile = new File(this.filename);
 		Scanner input = null;
 		try
 		{
 			input = new Scanner(fieldFile);
 		}
 		catch (final IOException e)
 		{
 			// Print to stderr and warn the mother program, rather than quitting
 			System.err.println("Error openinng: " + this.filename);
 			throw new IllegalArgumentException(e.getMessage());
 		}
 
 		while (input.hasNextLine())
 		{
 			final String line = input.nextLine().trim().toLowerCase();
 			if (line.length() > 0) // skips empty lines
 			{
 				this.list.add(line);
 				this.set.add(line);
 			}
 		}
 
 		// Stop resource leak
 		input.close();
 	}
 
 	/**
 	 * Close all the streams.
 	 */
 	public void close()
 	{
 		for (String key : this.stream.keySet())
 		{
 			this.stream.get(key).close();
 		}
 	}
 
 	/**
 	 * Returns the lines read so far. Note: LinkedList allows duplicates.
 	 * 
 	 * @return LinkedList of String type
 	 */
 	public LinkedList<String> getList()
 	{
 		return new LinkedList<String>(this.list);
 	}
 
 	/**
 	 * Returns a LinkedHashSet of non-duplicated lines in the read file.
 	 * 
 	 * @return LinkedHashSet of String type
 	 * 
 	 */
 
 	public Set<String> getSet()
 	{
 		return new LinkedHashSet<String>(this.set);
 	}
 
 	/**
 	 * Parses through all the lines read and tokenize them by a given delimiter
 	 * 
 	 * @param delimiter
 	 *            The character(s) to delimit.
 	 * @return LinkedList of parsed strings
 	 */
 	public LinkedList<String> getTokens(final String delimiter)
 	{
 		final String delims = "[" + delimiter + "]+";
 
 		for (int i = 0; i < this.list.size(); i++)
 		{
 			final String line = this.list.get(i);
 			final String[] token = line.split(delims);
 			for (int j = 0; j < token.length; j++)
 			{
 				this.tokens.add(token[j]);
 			}
 
 		}
 		return this.tokens;
 
 	}
 
 	/**
 	 * Create a new output stream based on input string.
 	 * 
 	 * @param name
 	 *            Name of the output stream to create
 	 * @return number of PrintStreams allocated so far
 	 */
 	public int initiateStream(final String name)
 	{
 		try
 		{
 			this.stream.put(name, new PrintStream(name + ".txt"));
 		}
 		catch (final Exception e)
 		{
 			// Print to stderr and warn the mother program
 			System.err.println("Error openinng: " + name);
 			throw new IllegalArgumentException(e.getMessage());
 		}
 		
 		return this.stream.size();
 	}
 
 	/**
 	 * Write a given string to output file.
 	 * 
 	 * @param stream
 	 *            the output stream to write to
 	 * @param s
 	 *            the string to write
 	 */
 	public void print(final String stream, final String s)
 	{
 		
 		final PrintStream p = this.stream.get(stream);
 		
 		// Warn the program if that stream doesn't exist
 		if(p == null)
 		{
 			throw new IllegalArgumentException("PrintStream not found");
 		}
 		
 		p.println(s);
 		
		p.close();

 	}
 
 }
