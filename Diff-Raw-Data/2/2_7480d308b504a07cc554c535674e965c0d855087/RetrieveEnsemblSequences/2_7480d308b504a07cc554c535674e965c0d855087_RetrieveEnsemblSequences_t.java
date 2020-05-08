 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.sql.DataSource;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.Feature;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.db.SequenceDB;
 import org.biojava.bio.seq.db.ensembl.EnsemblConnection;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.seq.io.FastaFormat;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.LocationTools;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojava.bio.symbol.SimpleSymbolList;
 import org.biojava.bio.symbol.Symbol;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojava.utils.JDBCPooledDataSource;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Get noncoding sequences from Ensembl for motif discovery", generateStub = true)
 @NMExtraApp(launchName = "nmensemblseq", vm = VirtualMachine.SERVER)
 public class RetrieveEnsemblSequences {
 
     public static enum Format {
     	GFF,
     	FASTA
     }
 
     private Format format = Format.FASTA;
 
 	/* Sequence region processing */
 	protected boolean repeatMask = true;
 	protected boolean excludeTranslations = true;
 	protected int featherTranslationsBy;
 	private int featherRegionsBy;
 
 	/*
 	 * Sequence filtering properties id = list of identifiers idType = type of
 	 * identifier ('ensembl_gene' and 'stable_id' for now)
 	 */
 	private List<String> ids = new ArrayList<String>();
 	private String idType = "ensembl_gene";
 
 	/* Sequence region properties */
 	private int threePrimeBegin, threePrimeEnd;
 	private int fivePrimeBegin, fivePrimeEnd;
 
 	/* Ensembl settings */
 	protected String username = "anonymous";
 	protected String password = "";
 	protected String host = "ensembldb.ensembl.org";
 	protected String database;
 	protected int port = 5306;
 	protected int schemaVersion = 54;
 	private File outputFile;
 	private Connection connection;
 	private PreparedStatement get_geneStableId;
 	private PreparedStatement get_xref;
 	private PreparedStatement get_gsiForTranscript;
 	private PreparedStatement get_gsiForTranslation;
 
 	private Object type;
 
 	protected EnsemblConnection ensemblConnection;
 
 	protected SequenceDB seqDB;
 
 	private String dbURL;
 
 	private String chromosome;
 
 	private boolean knownGene;
 
 	private boolean proteinCoding;
 
 	private boolean ignoreGenesWithNoCrossReferences;
 
 	private int randomGeneCount;
 	
 	private Random random = new Random();
 
 	@Option(help = "Output format: either fasta or gff (default=fasta)",optional=true)
 	public void setFormat(Format format) {
 		this.format = format;
 	}
 
 	@Option(help = "Repeat mask the sequences (default=true)", optional = true)
 	public void setRepeatMask(boolean b) {
 		this.repeatMask = b;
 	}
 
 	@Option(help = "Exclude translations (default=true)", optional = true)
 	public void setExcludeTranslations(boolean b) {
 		this.excludeTranslations = b;
 	}
 
 	@Option(help = "Feather translations by the specified number of nucleotides", optional = true)
 	public void setFeatherTranslationsBy(int i) {
 		this.featherTranslationsBy = i;
 	}
 
 	@Option(help = "Feather retrieved sequence regions by the specified number of nucleotides", optional = true)
 	public void setFeatherRegionsBy(int i) {
 		this.featherRegionsBy = i;
 	}
 
 	@Option(help = "Filter returned sequence set based on identifier(s)", optional = true)
 	public void setFilterByIds(String[] ids) {
 		this.ids.addAll(Arrays.asList(ids));
 	}
 
 	@Option(help = "Filter returned sequence set based on identifier(s) in the input file (one row per identifier)", optional = true)
 	public void setFilterByIdsInFile(FileReader fr) throws IOException {
 		BufferedReader reader = new BufferedReader(fr);
 		String line = null;
 
 		while ((line = reader.readLine()) != null) {
 			this.ids.add(line.replace("\n", ""));
 		}
 	}
 
 	@Option(help="Output sequences only for genes that are on the specified chromosome (ignored if ID list is specified)",optional=true)
 	public void setChromosome(String chromo) {
 		this.chromosome = chromo;
 	}
 
 	@Option(help = "Identifier type to use when filtering sequences identifiers (default = ensembl_gene, possible values: ensembl_gene|stable_id|display_label)", optional = true)
 	public void setIdType(String idType) {
 		this.idType = idType;
 	}
 
 	@Option(help = "Output FASTA file", optional=true)
 	public void setOut(File f) {
 		this.outputFile = f;
 	}
 	
 	@Option(help = "Retrieve transcripts for a specified number of randomly selected genes")
 	public void setSampleRandomGenes(int i) {
 		this.randomGeneCount = i;
 	}
 
 	@Option(help = "Get three prime UTR sequences. "
 			+ "Example: '-threePrimeUTR 200 200' gets you sequence regions "
 			+ "from 200bp upstream to 200bp downstream of transcription "
 			+ "start sites.", optional = true)
 	public void setThreePrimeUTR(String[] coordStrs) {
 		if (coordStrs.length != 2) {
 			System.err
 					.printf(
 							"-threePrimeUTR requires two arguments: begin and end coordinate (%d given)",
 							coordStrs.length);
 			System.exit(1);
 		}
 
 		this.threePrimeBegin = Integer.parseInt(coordStrs[0].replace("n", "-"));
 		this.threePrimeEnd = Integer.parseInt(coordStrs[1].replace("n", "-"));
 	}
 
 	@Option(help = "Get five prime UTR sequences. "
 			+ "Example: '-threePrimeUTR 200 200' gets you sequence regions "
 			+ "from 200bp upstream to 200bp downstream of transcription "
 			+ "start sites.", optional = true)
 	public void setFivePrimeUTR(String[] coordStrs) {
 		if (coordStrs.length != 2) {
 			System.err
 					.printf(
 							"-fivePrimeUTR requires two arguments: begin and end coordinate (%d given)",
 							coordStrs.length);
 			System.exit(2);
 		}
 
 		this.fivePrimeBegin = Integer.parseInt(coordStrs[0].replace("n", "-"));
 		this.fivePrimeEnd = Integer.parseInt(coordStrs[1].replace("n","-"));
 	}
 
 	@Option(help = "Ensembl username (default=anonymous)", optional = true)
 	public void setUser(String user) {
 		this.username = user;
 	}
 
 	@Option(help = "Ensembl password (default='')", optional = true)
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	@Option(help = "Ensembl hostname (default=ensembldb.ensembl.org", optional = true)
 	public void setHost(String hostname) {
 		this.host = hostname;
 	}
 
 	@Option(help = "Ensembl database port (default=5306)", optional = true)
 	public void setPort(int port) {
 		this.port = port;
 	}
 
 	@Option(help = "Ensembl database to retrieve sequences from (e.g. 'danio_rerio_core_54_8')")
 	public void setDatabase(String db) {
 		this.database = db;
 	}
 
 	@Option(help = "Ensembl schema version (default = 54)", optional = true)
 	public void setSchemaVersion(int ver) {
 		this.schemaVersion = ver;
 	}
 
 	@Option(help="Allowed gene type, e.g. 'protein_coding','pseudogene' " +
 			"(by default all gene types are allowed)", optional = true)
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	@Option(help="Output only known genes", optional=true)
 	public void setKnown(boolean b) {
 		this.knownGene = b;
 	}
 
 	@Option(help="Output only protein coding genes", optional=true)
 	public void setProteinCoding(boolean b) {
 		this.proteinCoding = b;
 	}
 
 	@Option(help="Ignore genes that don't have cross-references", optional=true)
 	public void setIgnoreXrefless(boolean b) {
 		this.ignoreGenesWithNoCrossReferences = b;
 	}
 
 	protected void initializeEnsemblConnection() throws SQLException, Exception {
 		this.initPreparedStatements();
 		this.dbURL = String.format("jdbc:mysql://%s:%d/%s",
 				this.host,
 				this.port,
 				this.database);
 
 		ensemblConnection = new EnsemblConnection(dbURL, username,
 				password, schemaVersion);
 		this.seqDB = ensemblConnection.getDefaultSequenceDB();
 	}
 
 	public void main(String[] argv) throws Exception {
 		initializeEnsemblConnection();
 
 		if (ids.size() == 0) {
 			List<String> idsList = new ArrayList<String>();
 			DataSource db = JDBCPooledDataSource.getDataSource(
 					"org.gjt.mm.mysql.Driver", dbURL, username, password);
 			Connection con = db.getConnection();
 			PreparedStatement get_geneStableId;
 
 			if (this.chromosome == null) {
 				get_geneStableId = con.prepareStatement("SELECT stable_id FROM gene_stable_id;");
 			} else {
 				get_geneStableId = con.prepareStatement(
 										"SELECT stable_id FROM gene_stable_id " +
 										"LEFT JOIN gene ON gene.gene_id=gene_stable_id.gene_id " +
 										"LEFT JOIN seq_region ON seq_region.seq_region_id=gene.seq_region_id " +
 										"WHERE seq_region.name=?");
 
 				get_geneStableId.setString(1, this.chromosome);
 			}
 
 
 			ResultSet rs = get_geneStableId.executeQuery();
 
 			while (rs.next()) {
 				idsList.add(rs.getString(1));
 			}
 			
 			if (randomGeneCount > 0) {
 				if (randomGeneCount > idsList.size()) {
 					System.err.println(
 							"Cannot choose " + randomGeneCount + 
 							" from amongst a set of " + idsList.size() + " genes.");
 					System.exit(1);
 				}
 				
 				Set<String> randomGenes = new TreeSet<String>();
 				List<String> samplableGenes = new ArrayList<String>(idsList);
 				
 				int genesToSample = randomGeneCount;
 				while (genesToSample > 0) {
 					int i = random.nextInt(samplableGenes.size());
 					String id = samplableGenes.get(i);
 					if (!randomGenes.contains(id)) {
 						genesToSample--;
 						randomGenes.add(id);
 					}
 				}
				
				idsList = new ArrayList<String>(randomGenes);
 			}
 			rs.close();
 			ids = idsList;
 		}
 
 		if (!this.idType.equals("ensembl_gene")) {
 			List<String> idList = new ArrayList<String>();
 			for (String str : ids) {
 				String ensId = ensemblIDForGeneName(str);
 				idList .add(ensId);
 				System.err.printf("%s -> %s",str, ensId);
 			}
 			ids = idList;
 		}
 
 		GFFWriter gffw = null;
 
 		if (format == Format.GFF) {
 			gffw = new GFFWriter(new PrintWriter(new OutputStreamWriter(System.out)));
 		}
 
 		PrintStream outputStream = null;
 
 		if (outputFile == null) {
 			outputStream = System.out;
 		} else {
 			outputStream = new PrintStream(new FileOutputStream(outputFile));
 		}
 
 		for (String gene : ids) {
 			System.err.println("" + gene);
 			FeatureHolder transcripts =
 				seqDB.filter(new FeatureFilter.ByAnnotation("ensembl.gene_id",gene));
 
 			if (this.knownGene) {
 				transcripts = transcripts.filter(new FeatureFilter.ByAnnotation("ensembl.gene_status","KNOWN"));
 			}
 
 			if (this.proteinCoding) {
 				transcripts = transcripts.filter(new FeatureFilter.ByAnnotation("ensembl.gene_type","protein_coding"));
 			}
 
 			if (this.type != null) {
 				transcripts = transcripts.filter(new FeatureFilter.ByAnnotation("ensembl.gene_type",this.type));
 			}
 
 			Sequence chr = null;
 			boolean reverse = false;
 			List<Location> dumpLocs = new ArrayList<Location>();
 			String outputName = "sequence";
 
 			for (Iterator<?> fi = transcripts.features(); fi.hasNext();) {
 				StrandedFeature transcript = (StrandedFeature) fi.next();
 
 				for (Object keyO : transcript.getAnnotation().keys()) {
 					System.err.printf("%s:%s\n",keyO,transcript.getAnnotation().getProperty(keyO));
 				}
 
 				Object o = transcript.getAnnotation().getProperty("ensembl.xrefs");
 
 				if (this.ignoreGenesWithNoCrossReferences && ((o == null) || (!(o instanceof List)) || (((List)o)).size() == 0)) {
 					System.err.printf("Ignoring transcript of gene %s because it has no cross-references%n", transcript.getAnnotation().getProperty("ensembl.id"));
 					continue;
 				}
 
 				outputName =
 					String.format("%s(%s)", transcript.getAnnotation().getProperty("ensembl.gene_id"), transcript.getAnnotation().getProperty("ensembl.gene_display_label"));
 				chr = transcript.getSequence();
 				if (transcript.getStrand() != StrandedFeature.NEGATIVE) {
 					int start = transcript.getLocation().getMin();
 					int end = transcript.getLocation().getMax();
 					if (fivePrimeBegin > 0 || fivePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(start - fivePrimeBegin,
 								start + fivePrimeEnd));
 					}
 					if (threePrimeBegin > 0 || threePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(end - threePrimeBegin,
 								end + threePrimeEnd));
 					}
 				} else {
 					int start = transcript.getLocation().getMax();
 					int end = transcript.getLocation().getMin();
 
 					if (fivePrimeBegin > 0 || fivePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(start - fivePrimeEnd,
 								start + fivePrimeBegin));
 					}
 					if (threePrimeBegin > 0 || threePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(end - threePrimeEnd, end
 								+ threePrimeBegin));
 					}
 					reverse = true;
 				}
 			}
 
 			if (chr == null) {
 				System.err.printf("Not dumping anything for %s%n", gene);
 				continue;
 			}
 
 			Location dumpLoc = LocationTools.union(dumpLocs);
 
 			if (excludeTranslations) {
 				FeatureHolder translations = chr.filter(new FeatureFilter.And(
 						new FeatureFilter.ByType("translation"),
 						new FeatureFilter.OverlapsLocation(dumpLoc)));
 
 				List<Location> transLocs = new ArrayList<Location>();
 				for (Iterator<?> i = translations.features(); i.hasNext();) {
 					transLocs.add(((Feature) i.next()).getLocation());
 				}
 				Location transMask = feather(LocationTools.union(transLocs),
 						featherTranslationsBy);
 				dumpLoc = LocationTools.subtract(dumpLoc, transMask);
 			}
 
 			Location mask = Location.empty;
 			if (repeatMask) {
 				FeatureHolder repeats = chr.filter(new FeatureFilter.And(
 						new FeatureFilter.Or(
 								new FeatureFilter.ByType("repeat"),
 								new FeatureFilter.ByType("Repeat")),
 						new FeatureFilter.OverlapsLocation(dumpLoc)));
 				List<Location> repLocs = new ArrayList<Location>();
 				for (Iterator<?> i = repeats.features(); i.hasNext();) {
 					repLocs.add(((Feature) i.next()).getLocation());
 				}
 				System.err.println("Will mask " + repLocs.size() + " repeats");
 				mask = LocationTools.union(repLocs);
 			}
 
 			for (Iterator<?> bi = dumpLoc.blockIterator(); bi.hasNext();) {
 				Location bloc = (Location) bi.next();
 				if (bloc.getMax() - bloc.getMin() < 20) {
 					continue;
 				} else {
 					List<Symbol> sl = new ArrayList<Symbol>();
 					boolean truncatedSeq = false;
 					int max = bloc.getMax();
 
 					if (chr.length() < bloc.getMin()) {
 						System.err.printf(
 								"WARNING: cannot extract feature from %s:" +
 								"the beginning of feature %d - %d runs over the end of the sequence at %d)%n",
 								chr.getName(),
 								bloc.getMin(),
 								bloc.getMax(),
 								chr.length());
 						continue;
 					}
 					if (chr.length() < bloc.getMax()) {
 						System.err.printf(
 							"WARNING: extracted feature from %s would be truncated " +
 							"(feature %d - %d runs over the end of the sequence at %d). Will not output it.%n",
 							chr.getName(),
 							bloc.getMin(),
 							bloc.getMax(),
 							chr.length());
 
 						//max = chr.length();
 						continue;
 					}
 					if (bloc.getMin() <= 1) {
 						System.err.printf(
 							"WARNING: retrieved location %d-%d on chromosome %s has a negative start coordinate on the sequence region. " +
 							"Will not output.%n",bloc.getMin(),bloc.getMax(),chr.getName());
 
 						continue;
 					}
 					if (bloc.getMax() <= 1) {
 						System.err.printf(
 							"WARNING: retrieved location %d-%d on chromosome %s has a negative end coordinate on the sequence region. " +
 							"Will not output.%n",bloc.getMin(),bloc.getMax(),chr.getName());
 
 						continue;
 					}
 					for (int i = bloc.getMin(); i <= max; ++i) {
 						if (!mask.contains(i)) {
 							sl.add(chr.symbolAt(i));
 						} else {
 							sl.add(DNATools.n());
 						}
 					}
 
 					if (format == Format.FASTA) {
 						SymbolList ssl = new SimpleSymbolList(DNATools.getDNA(), sl);
 						if (reverse) {
 							ssl = DNATools.reverseComplement(ssl);
 						}
 						Sequence dump = new SimpleSequence(ssl, null,
 								String.format("%s_%s_%d_%d",
 										gene,
 										chr.getName(),
 										bloc.getMin(),
 										max),
 								Annotation.EMPTY_ANNOTATION);
 
 						new FastaFormat().writeSequence(dump, outputStream);
 					} else {
 						org.biojava.bio.seq.StrandedFeature.Strand strand = StrandedFeature.UNKNOWN;
 
 						Map<String,Object> map = new HashMap<String, Object>();
 						map.put(String.format("ID=%s",gene), new ArrayList());
 
 						GFFRecord rec = new SimpleGFFRecord(
 								chr.getName(),
 								"nmensemblseq",
 								outputName,
 								bloc.getMin(),
 								max,
 								Double.NaN,
 								strand,
 								0,
 								null,
 								map);
 
 						gffw.recordLine(rec);
 						gffw.endDocument();
 					}
 				}
 			}
 
 		}
 
 		if (gffw != null) {
 			gffw.endDocument();
 		}
 	}
 
 
 
 	protected Location feather(Location l, int amount) {
 		List<Location> spans = new ArrayList<Location>();
 		for (Iterator<?> bi = l.blockIterator(); bi.hasNext();) {
 			Location bloc = (Location) bi.next();
 			spans.add(new RangeLocation(bloc.getMin() - amount, bloc.getMax()
 					+ amount));
 		}
 		return LocationTools.union(spans);
 	}
 
 	public Connection connection() throws Exception {
 		if (this.connection == null) {
 			DataSource db = JDBCPooledDataSource.getDataSource(
 	                "org.gjt.mm.mysql.Driver",
 	                String.format("jdbc:mysql://%s:%d/%s", this.host,
 	        				this.port, this.database),
 	                username,
 	                password);
 			this.connection = db.getConnection();
 		}
 		return connection;
 	}
 
 
 
 	public String ensemblIDForGeneName(String name) throws SQLException {
 		{
 			get_geneStableId.setString(1, name);
 			ResultSet rs = get_geneStableId.executeQuery();
 			boolean success = rs.next();
 			rs.close();
 			if (success) {
 				return name;
 			}
 		}
 
 		{
 			get_xref.setString(1, name);
 			ResultSet rs = get_xref.executeQuery();
 			int tid = -1;
 			int tlid = -1;
 			while (rs.next()) {
 				int id = rs.getInt(1);
 				String type = rs.getString(2);
 				if (type.equalsIgnoreCase("transcript")) {
 					tid = id;
 				} else if (type.equalsIgnoreCase("translation")) {
 					tlid = id;
 				}
 			}
 			rs.close();
 
 			if (tid > 0) {
 				get_gsiForTranscript.setInt(1, tid);
 				rs = get_gsiForTranscript.executeQuery();
 				rs.next();
 				String retString = rs.getString(1);
 				rs.close();
 				return retString;
 			} else if (tlid > 0) {
 				get_gsiForTranslation.setInt(1, tlid);
 				rs = get_gsiForTranslation.executeQuery();
 				rs.next();
 				String retString = rs.getString(1);
 				rs.close();
 				return retString;
 			} else {
 				return null;
 			}
 		}
 	}
 
 	private void initPreparedStatements() throws SQLException, Exception {
 		this.get_geneStableId = connection().prepareStatement(
 				"select gene_id from gene_stable_id where stable_id = ?"
 	    );
 
 		if (idType.equals("display_label")) {
 			this.get_xref = connection().prepareStatement(
 				"select ensembl_id, ensembl_object_type " +
 				"  from xref, object_xref " +
 				" where object_xref.xref_id = xref.xref_id and " +
 				"       xref.display_label = ?"
 			);
 		} else {
 			this.get_xref = connection().prepareStatement(
 					"select ensembl_id, ensembl_object_type " +
 					"  from xref, object_xref " +
 					" where object_xref.xref_id = xref.xref_id and " +
 					"       xref.dbprimary_acc = ?"
 		    );
 		}
 		this.get_gsiForTranscript = connection().prepareStatement(
 				"select stable_id " +
 				"  from gene_stable_id, transcript " +
 				" where gene_stable_id.gene_id = transcript.gene_id and " +
 				"       transcript.transcript_id = ?"
 		);
 		this.get_gsiForTranslation = connection().prepareStatement(
 				"select stable_id " +
 				"  from gene_stable_id, transcript, translation " +
 				" where gene_stable_id.gene_id = transcript.gene_id and " +
 				"       transcript.transcript_id = translation.transcript_id and " +
 				"       translation.translation_id = ?"
 		);
 	}
 }
