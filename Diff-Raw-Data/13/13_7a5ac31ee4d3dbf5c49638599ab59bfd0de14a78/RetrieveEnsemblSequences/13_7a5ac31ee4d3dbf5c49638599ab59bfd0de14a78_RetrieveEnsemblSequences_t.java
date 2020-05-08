 package net.derkholm.nmica.extra.app;
 
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.Annotation;
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
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
import biobits.utils.IOTools;

@App(overview = "Get noncoding sequences from Ensembl for motif discovery", generateStub = true)
@NMExtraApp(launchName = "nmensemblseq", vm = VirtualMachine.SERVER)
 public class RetrieveEnsemblSequences {
 	
 	/* Sequence region processing */
 	private boolean repeatMask = true;
 	private boolean excludeTranslations = true;
 	private int featherTranslationsBy;
 	private int featherRegionsBy;
 
 	/* Sequence filtering properties 
 	 * id = list of identifiers
 	 * idType = type of identifier ('ensembl_gene' and 'stable_id' for now)*/
 	private String[] ids;
 	private String idType = "ensembl_gene";
 
 	/* Sequence region properties */
 	private int threePrimeBegin,threePrimeEnd;
 	private int fivePrimeBegin,fivePrimeEnd;
 	
 	/* Ensembl settings */
 	private String username = "anonymous";
 	private String password = "";
 	private String host = "ensembldb.ensembl.org";
 	private String database;
 	private int port = 5306;
 	private int schemaVersion = 54;
 	
 	@Option(help="Repeat mask the sequences (default=true)", optional=true)
 	public void setRepeatMask(boolean b) {
 		this.repeatMask = b;
 	}
 	
 	@Option(help="Exclude translations (default=true)", optional=true)
 	public void setExcludeTranslations(boolean b) {
 		this.excludeTranslations  = b;
 	}
 	
 	@Option(help="Feather translations by the specified number of nucleotides", optional=true)
 	public void setFeatherTranslationsBy(int i) {
 		this.featherTranslationsBy = i;
 	}
 	
 	@Option(help="Feather retrieved sequence regions by the specified number of nucleotides", optional = true)
 	public void setFeatherRegionsBy(int i) {
 		this.featherRegionsBy = i;
 	}
 	
 	@Option(help="Filter returned sequence set based on identifiers")
 	public void setFilterById(String[] ids) {
 		this.ids = ids;
 	}
 	
 	@Option(help="Identifier type to use when filtering sequences identifiers (default = ensembl_gene, possible values: ensembl_gene|stable_id)", optional=true)
 	public void setIdType(String idType) {
 		this.idType = idType;
 	}
 	
 	@Option(help="Get three prime UTR sequences. " +
 			"Example: '-threePrimeUTR 200 200' gets you sequence regions " +
 			"from 200bp upstream to 200bp downstream of transcription " +
 			"start sites.", optional=true)
 	public void setThreePrimeUTR(String[] coordStrs) {
 		if (coordStrs.length != 2) {
 			System.err.printf("-threePrimeUTR requires two arguments: begin and end coordinate (%d given)",coordStrs.length);
 			System.exit(1);
 		}
 		
 		this.threePrimeBegin = Integer.parseInt(coordStrs[0]);
 		this.threePrimeEnd = Integer.parseInt(coordStrs[1]);
 	}
 	
 	@Option(help="Get five prime UTR sequences. " +
 			"Example: '-threePrimeUTR 200 200' gets you sequence regions " +
 			"from 200bp upstream to 200bp downstream of transcription " +
 			"start sites.", optional=true)
 	public void setFivePrimeUTR(String[] coordStrs) {
 		if (coordStrs.length != 2) {
 			System.err.printf("-fivePrimeUTR requires two arguments: begin and end coordinate (%d given)",coordStrs.length);
 			System.exit(2);
 		}
 		
 		this.fivePrimeBegin = Integer.parseInt(coordStrs[0]);
 		this.fivePrimeEnd = Integer.parseInt(coordStrs[1]);
 	}
 	
 	@Option(help="Ensembl username (default=anonymous)", optional=true)
 	public void setUser(String user) {
 		this.username = user;
 	}
 	
 	@Option(help="Ensembl password (default='')", optional=true)
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	@Option(help="Ensembl hostname (default=ensembldb.ensembl.org", optional=true)
 	public void setHost(String hostname) {
 		this.host = hostname;
 	}
 	
 	@Option(help="Ensembl database port (default=5306)", optional=true)
 	public void setPort(int port) {
 		this.port = port;
 	}
 		
 	@Option(help="Ensembl database to retrieve sequences from (e.g. 'danio_rerio_core_54_8')", optional=true)
 	public void setDatabase(String db) {
 		this.database = db;
 	}
 	
 	@Option(help="Ensembl schema version (default = 54)", optional=true)
 	public void setSchemaVersion(int ver) {
 		this.schemaVersion = ver;
 	}
 	
 	public void main(String[] argv) throws Exception {
 		String dbURL = String.format("jdbc:mysql://%s:%d/%s", this.host, this.port, this.database);
 		
 		EnsemblConnection conn = new EnsemblConnection(dbURL, username, password, schemaVersion);
 		SequenceDB seqDB = conn.getDefaultSequenceDB();
 		
 		BufferedReader br = IOTools.inputBufferedReader(argv);
 		
 
 		for (String gene : ids) {
 			FeatureHolder transcripts = seqDB.filter(new FeatureFilter.ByAnnotation("ensembl.gene_id", gene));
 			Sequence chr = null;
 			boolean reverse = false;
 			List<Location> dumpLocs = new ArrayList<Location>();
 			for (Iterator<?> fi = transcripts.features(); fi.hasNext(); ) {
 				StrandedFeature transcript = (StrandedFeature) fi.next();
 				chr = transcript.getSequence();
 				if (transcript.getStrand() != StrandedFeature.NEGATIVE) {
 					int start = transcript.getLocation().getMin();
 					int end = transcript.getLocation().getMax();
 					if (fivePrimeBegin > 0 || fivePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(start - fivePrimeBegin, start + fivePrimeEnd));
 					}
 					if (threePrimeBegin > 0 || threePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(end - threePrimeBegin, end + threePrimeEnd));
 					}
 				} else {
 					int start = transcript.getLocation().getMax();
 					int end = transcript.getLocation().getMin();
 					if (fivePrimeBegin > 0 || fivePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(start - fivePrimeEnd, start + fivePrimeBegin));
 					}
 					if (threePrimeBegin > 0 || threePrimeEnd > 0) {
 						dumpLocs.add(new RangeLocation(end - threePrimeEnd, end + threePrimeBegin));
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
 				FeatureHolder translations = chr.filter(new FeatureFilter.And(new FeatureFilter.ByType("translation"), new FeatureFilter.OverlapsLocation(dumpLoc)));
 				List<Location> transLocs = new ArrayList<Location>();
 				for (Iterator<?> i = translations.features(); i.hasNext(); ) {
 					transLocs.add(((Feature) i.next()).getLocation());
 				}
 				Location transMask = feather(LocationTools.union(transLocs), featherTranslationsBy);
 				dumpLoc = LocationTools.subtract(dumpLoc, transMask);
 			}
 			
 			Location mask = Location.empty;
 			if (repeatMask) {
 				FeatureHolder repeats = chr.filter(new FeatureFilter.And(new FeatureFilter.ByType("repeat"), new FeatureFilter.OverlapsLocation(dumpLoc)));
 				List<Location> repLocs = new ArrayList<Location>();
 				for (Iterator<?> i = repeats.features(); i.hasNext(); ) {
 					repLocs.add(((Feature) i.next()).getLocation());
 				}
 				mask = LocationTools.union(repLocs);
 			}
 			
 			for (Iterator<?> bi = dumpLoc.blockIterator(); bi.hasNext(); ) {
 				Location bloc = (Location) bi.next();
 				if (bloc.getMax() - bloc.getMin() < 20) {
 					continue;
 				} else {
 					List<Symbol> sl = new ArrayList<Symbol>();
 					for (int i = bloc.getMin(); i <= bloc.getMax(); ++i) {
 						if (!mask.contains(i)) {
 							sl.add(chr.symbolAt(i));
 						} else {
 							sl.add(DNATools.n());
 						}
 					}
 					SymbolList ssl = new SimpleSymbolList(DNATools.getDNA(), sl);
 					if (reverse) {
 						ssl = DNATools.reverseComplement(ssl);
 					}
 					Sequence dump = new SimpleSequence(ssl, null, String.format("%s_%s_%d_%d", gene, chr.getName(), bloc.getMin(), bloc.getMax()), Annotation.EMPTY_ANNOTATION);
 					new FastaFormat().writeSequence(dump, System.out);
 				}
 			}
 			
 		}
 	}
 	
 	private Location feather(Location l, int amount) {
 		List<Location> spans = new ArrayList<Location>();
 		for (Iterator<?> bi = l.blockIterator(); bi.hasNext(); ) {
 			Location bloc = (Location) bi.next();
 			spans.add(new RangeLocation(bloc.getMin() - amount, bloc.getMax() + amount));
 		}
 		return LocationTools.union(spans);
 	}
 }
 
