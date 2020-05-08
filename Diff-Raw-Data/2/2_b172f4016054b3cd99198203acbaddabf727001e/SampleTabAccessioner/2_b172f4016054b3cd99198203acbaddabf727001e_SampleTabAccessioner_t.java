 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 
 public class SampleTabAccessioner {
 
 	public static final SampleTabParser<SampleData> parser = new SampleTabParser<SampleData>();
 
 	// logging
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private Connection connect = null;
 
 	public void makeConnection(String host, int port, String database,
 			String username, String password) throws ClassNotFoundException,
 			SQLException {
 		// This will load the MySQL driver, each DB has its own driver
 		Class.forName("com.mysql.jdbc.Driver");
 		// Setup the connection with the DB
 		// host:mysql-ae-autosubs.ebi.ac.uk port:4091 database:ae_autosubs
 		// username:curator password:troajsp
 		connect = DriverManager.getConnection("jdbc:mysql://" + host + ":"
 				+ port + "/" + database, username, password);
 
 	}
 
 	public Logger getLog() {
 		return log;
 	}
 
 	public SampleData convert(String sampleTabFilename) throws IOException,
 			ParseException, SQLException {
 		return convert(new File(sampleTabFilename));
 	}
 
 	public SampleData convert(File sampleTabFile) throws IOException,
 			ParseException, SQLException {
 		return convert(parser.parse(sampleTabFile));
 	}
 
 	public SampleData convert(URL sampleTabURL) throws IOException,
 			ParseException, SQLException {
 		return convert(parser.parse(sampleTabURL));
 	}
 
 	public SampleData convert(InputStream dataIn) throws ParseException,
 			SQLException {
 		return convert(parser.parse(dataIn));
 	}
 
 	public SampleData convert(SampleData sampleIn) throws ParseException,
 			SQLException {
 		String table = null;
 		String prefix = null;
 		if (sampleIn.msi.submissionReferenceLayer == true) {
 			prefix = "SAME";
 			table = "sample_reference";
 		} else if (sampleIn.msi.submissionReferenceLayer == false) {
 			prefix = "SAMEA";
 			table = "sample_assay";
 		} else {
 			throw new ParseException(
 					"Must specify a Submission Reference Layer MSI attribute.");
 		}
 
 		String name;
 		String submission = sampleIn.msi.submissionIdentifier;
 		PreparedStatement statement;
 		ResultSet results;
 		int accessionID;
 		String accession;
 		
 		Collection<SampleNode> samples = sampleIn.scd
 				.getNodes(SampleNode.class);
 
 		getLog().debug("got "+samples.size()+" samples.");
 		for (SampleNode sample : samples) {
 			if (sample.sampleAccession == null) {
 				name = sample.getNodeName();
 				statement = connect
 						.prepareStatement("INSERT IGNORE INTO "+table+" (user_accession, submission_accession, date_assigned, is_deleted) VALUES (?, ?, NOW(), 0)");
 				statement.setString(1, name);
 				statement.setString(2, submission);
 				statement.executeUpdate();
 
 				statement = connect
 						.prepareStatement("SELECT accession FROM "+table+" WHERE user_accession = ? AND submission_accession = ?");
 				statement.setString(1, name);
 				statement.setString(2, submission);
 				results = statement.executeQuery();
				results.first();
 				accessionID = results.getInt(1);
 				accession = prefix + accessionID;
 				
 				getLog().debug("Assigning "+accession+" to "+name);
 				sample.sampleAccession = accession;
 			}
 		}
 		return sampleIn;
 	}
 
 	public void convert(SampleData sampleIn, Writer writer) throws IOException,
 			ParseException, SQLException {
 		getLog().debug("recieved magetab, preparing to convert");
 		SampleData sampleOut = convert(sampleIn);
 		getLog().debug("sampletab converted, preparing to output");
 		SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
 		getLog().debug("created SampleTabWriter");
 		sampletabwriter.write(sampleOut);
 		sampletabwriter.close();
 
 	}
 
 	public void convert(File sampletabFile, Writer writer) throws IOException,
 			ParseException, SQLException {
 		getLog().debug("preparing to load SampleData");
 		SampleTabParser<SampleData> stparser = new SampleTabParser<SampleData>();
 		getLog().debug("created MAGETABParser<SampleData>");
 		SampleData st = stparser.parse(sampletabFile);
 		convert(st, writer);
 	}
 
 	public void convert(File inputFile, String outputFilename)
 			throws IOException, ParseException, SQLException {
 		convert(inputFile, new File(outputFilename));
 	}
 
 	public void convert(File inputFile, File outputFile) throws IOException,
 			ParseException, SQLException {
 		convert(inputFile, new FileWriter(outputFile));
 	}
 
 	public void convert(String inputFilename, Writer writer)
 			throws IOException, ParseException, SQLException {
 		convert(new File(inputFilename), writer);
 	}
 
 	public void convert(String inputFilename, File outputFile)
 			throws IOException, ParseException, SQLException {
 		convert(inputFilename, new FileWriter(outputFile));
 	}
 
 	public void convert(String inputFilename, String outputFilename)
 			throws IOException, ParseException, SQLException {
 		convert(inputFilename, new File(outputFilename));
 	}
 
 	public static void main(String[] args) {
 
 		// manager for command line arguments
 		Options options = new Options();
 
 		//individual option and required arguments
 		
 		options.addOption("h", "help", false, "print this message and exit");
 
 		Option option = new Option("i", "input", true,
 				"input SampleTab filename");
 		option.setRequired(true);
 		options.addOption(option);
 
 		option = new Option("o", "output", true, "output SampleTab filename");
 		option.setRequired(true);
 		options.addOption(option);
 
 		option = new Option("n", "hostname", true,
 				"hostname of accesion MySQL database");
 		option.setRequired(true);
 		options.addOption(option);
 
 		option = new Option("t", "port", true,
 				"port of accesion MySQL database");
 		options.addOption(option);
 
 		option = new Option("d", "database", true,
 				"database of accesion MySQL database");
 		option.setRequired(true);
 		options.addOption(option);
 
 		option = new Option("u", "username", true,
 				"username of accesion MySQL database");
 		option.setRequired(true);
 		options.addOption(option);
 
 		option = new Option("p", "password", true,
 				"password of accesion MySQL database");
 		option.setRequired(true);
 		options.addOption(option);
 
 		CommandLineParser parser = new GnuParser();
 		CommandLine line;
 		try {
 			line = parser.parse(options, args);
 		} catch (org.apache.commons.cli.ParseException e) {
 			System.err
 					.println("Parsing command line failed. " + e.getMessage());
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("", options);
 			System.exit(100);
 			return;
 		}
 
 		if (line.hasOption("help")) {
 			// automatically generate the help statement
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("ant", options);
 			return;
 		}
 
 		String inputFilename = line.getOptionValue("input");
 		String outputFilename = line.getOptionValue("output");
 		String hostname = line.getOptionValue("hostname");
 		int port = 3306;
 		if (line.hasOption("port")) {
 			port = new Integer(line.getOptionValue("port"));
 		}
 		String database = line.getOptionValue("database");
 		String username = line.getOptionValue("username");
 		String password = line.getOptionValue("password");
 
 		SampleTabAccessioner converter = new SampleTabAccessioner();
 		try {
 			converter.makeConnection(hostname, port, database, username,
 					password);
 		} catch (ClassNotFoundException e) {
 			System.err.println("ClassNotFoundException connecting to "
 					+ hostname + ":" + port + "/" + database);
 			e.printStackTrace();
 			System.exit(111);
 			return;
 		} catch (SQLException e) {
 			System.err.println("SQLException connecting to " + hostname + ":"
 					+ port + "/" + database);
 			e.printStackTrace();
 			System.exit(112);
 			return;
 		}
 
 		SampleData st = null;
 		try {
 			st = converter.convert(inputFilename);
 		} catch (ParseException e) {
 			System.err.println("ParseException converting " + inputFilename);
 			e.printStackTrace();
 			System.exit(121);
 			return;
 		} catch (IOException e) {
 			System.err.println("IOException converting " + inputFilename);
 			e.printStackTrace();
 			System.exit(122);
 			return;
 		} catch (SQLException e) {
 			System.err.println("SQLException converting " + inputFilename);
 			e.printStackTrace();
 			System.exit(123);
 			return;
 		}
 
 		FileWriter out = null;
 		try {
 			out = new FileWriter(outputFilename);
 		} catch (IOException e) {
 			System.out.println("Error opening " + outputFilename);
 			e.printStackTrace();
 			System.exit(131);
 			return;
 		}
 
 		SampleTabWriter sampletabwriter = new SampleTabWriter(out);
 		try {
 			sampletabwriter.write(st);
 		} catch (IOException e) {
 			System.out.println("Error writing " + outputFilename);
 			e.printStackTrace();
 			System.exit(141);
 			return;
 		}
 
 	}
 }
