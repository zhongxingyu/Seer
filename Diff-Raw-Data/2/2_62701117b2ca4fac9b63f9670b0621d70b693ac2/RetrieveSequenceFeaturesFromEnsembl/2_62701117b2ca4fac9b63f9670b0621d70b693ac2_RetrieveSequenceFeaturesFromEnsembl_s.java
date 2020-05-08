 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.seq.DistanceFromStartOfStrandedFeatureToPointLocationComparator;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioException;
 import org.biojava.bio.program.gff.GFFDocumentHandler;
 import org.biojava.bio.program.gff.GFFParser;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.db.IllegalIDException;
 import org.biojava.bio.seq.db.SequenceDB;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.PointLocation;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojava.bio.symbol.Symbol;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojavax.SimpleRichAnnotation;
 import org.biojavax.bio.seq.RichSequence;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 import org.bjv2.util.cli.UserLevel;
 
 @App(overview = "Get noncoding sequences from Ensembl for motif discovery", generateStub = true)
 @NMExtraApp(launchName = "nmensemblfeat", vm = VirtualMachine.SERVER)
 public class RetrieveSequenceFeaturesFromEnsembl extends RetrieveEnsemblSequences {
 
 	protected enum FeatureOutputFormat {
 		FASTA,
 		GFF
 	};
 	
 	private File outFile;
 	private File featuresFile;
 	private int expandToLength = 0;
 	private int minNonN = 1;
 	private int maxDistFromGene;
 	private boolean excludeUnlabelled = true;
 	private FeatureOutputFormat outputFormat = FeatureOutputFormat.FASTA;
 	private GFFWriter gffWriter;
 
 	@Option(help="Output file",optional=true)
 	public void setOut(File f) {
 		this.outFile = f;
 	}
 
 	@Option(help="Output format",optional=true)
 	public void setOutputFormat(FeatureOutputFormat outputFormat) {
 		this.outputFormat  = outputFormat;
 	}
 	
 	@Option(help="Features file (read from stdin if not included)", optional=true)
 	public void setFeatures(File f) {
 		this.featuresFile = f;
 	}
 	
 	@Option(help="Expand to length", optional=true)
 	public void setExpandToLength(int i) {
 		this.expandToLength = i;
 	}
 	
 	@Option(help="Minimun number of non gap symbols (N)", optional=true)
 	public void setMinNonN(int i) {
 		this.minNonN = i;
 	}
 	
 	@Option(help="Label features with the closest gene (given the specified maximum distance)", optional=true)
 	public void setMaxDistanceFromGene(int i) {
 		this.maxDistFromGene = i;
 	}
 	
 	@Option(help="Exclude features that do fall within the specified maximum distance from a gene " +
 			"(done by default, applies only when -maxDistanceFromGene was given)", optional=true, userLevel=UserLevel.EXPERT)
 	public void setExcludeUnlabelled(boolean b) {
 		this.excludeUnlabelled  = b;
 	}
 	
 	public static int gapSymbolCount(SymbolList seq) {
 		int numNs = 0;
 		for (Iterator<?> i = seq.iterator(); i.hasNext(); ) {
             Symbol s = (Symbol) i.next();
             if (s == DNATools.n() || s == seq.getAlphabet().getGapSymbol()) {
                 ++numNs;
             }
         }
 		
 		return numNs;
 	}
 
 	
 	public void main(String[] args) throws SQLException, Exception {
 		initializeEnsemblConnection();
 
 		final OutputStream os;
 		if (this.outFile == null) {
 			os = System.out;
 		} else {
 			os = new FileOutputStream(this.outFile);
 		}
 
 		if (this.outputFormat.equals(FeatureOutputFormat.GFF)) {
 			this.gffWriter = new GFFWriter(new PrintWriter(os));
 		}
 		
 		InputStream inputStream;
 		if (featuresFile == null) {
 			inputStream = System.in;
 		} else {
 			inputStream = new FileInputStream(this.featuresFile);
 		}
 
 		GFFParser parser = new GFFParser();
 		parser.parse(
 				new BufferedReader(new InputStreamReader(inputStream)),
 				new GFFDocumentHandler() {
 
 					public void commentLine(String str) {
                         System.err.println();
                         System.err.println("# "+ str);
                     }
 					public void endDocument() {}
 					public void startDocument(String str) {}
 		
 					public void recordLine(GFFRecord recLine) {												
 						System.err.printf(".");
 						try {
 		
 							int start = recLine.getStart();
 							int end = recLine.getEnd();
 							
 							if (expandToLength > 0) {
 								if ((end - start + 1) < expandToLength) {
 									start = Math.max(1, start - (expandToLength / 2));
 									end = end + (expandToLength / 2);
 								}
 							}
 							
 							StrandedFeature nearestTranscript = null;
 							if (maxDistFromGene > 0) {
 								
 								StrandedFeature.Template featTempl = new StrandedFeature.Template();
 								featTempl.type = recLine.getFeature();
 								featTempl.source = recLine.getSource();
 								featTempl.location = new RangeLocation(start, end);
 								featTempl.annotation = Annotation.EMPTY_ANNOTATION;
 								featTempl.strand= recLine.getStrand();
 						        // System.err.println("Creating gap from " + temp.location.getMin() + " to " + temp.location.getMax());
 
 						        //System.err.printf("Calling RetrieveSequenceFeaturesFromEnsembl.transcriptWithClosestTSS\n");
 						        nearestTranscript = RetrieveSequenceFeaturesFromEnsembl
 															.transcriptWithClosestTSS(
 																recLine.getSeqName(),
 																start,
 																end,
 																recLine.getStrand(),
 																seqDB,
 																maxDistFromGene,
 																ignoreGenesWithNoCrossReferences);
 						        
 
 						        
 							}
 							
 							SymbolList symList = 
 								seqDB.getSequence(
 									recLine.getSeqName()).subList(start,end);
 							
 							if (recLine.getStrand().equals(StrandedFeature.NEGATIVE)) {
 								symList = DNATools.reverseComplement(symList);
 							}
 							
 							if (minNonN > 0 && 
 								RetrieveSequenceFeaturesFromEnsembl
 									.gapSymbolCount(symList) > minNonN) {
 								return;
 							}
 							
 							Annotation ann = new SimpleRichAnnotation();
 							for (Object obj : recLine.getGroupAttributes().keySet()) {
 								ann.setProperty(obj, recLine.getGroupAttributes().get(obj));
 							}
                             Object retrievedGene = null; // haven't got eclipse open, don't know what this is. Don't care.
 							if (nearestTranscript != null) {
 								System.err.println("Returned non null transcript.");
                                 retrievedGene = nearestTranscript.getAnnotation().getProperty("ensembl.gene_id");
 						        int distFromCentre = DistanceFromStartOfStrandedFeatureToPointLocationComparator
         												.distance(nearestTranscript,(end + start) / 2);
 								ann.setProperty("nearest_gene", retrievedGene);
 								ann.setProperty("distance_from_nearest_tss", distFromCentre);
 							} else if (excludeUnlabelled && (maxDistFromGene > 0)) {
 								System.err.println("Excluding unlabelled feature.");
 								return;
 							}
 
                             String fastaHeader = String.format("%s;%d-%d(%s);%s",
                                                                recLine.getSeqName(),
                                                                Math.max(1,start),
                                                                end,
                                                                // what happens here if there is no strand info?
                                                                recLine.getStrand().equals(StrandedFeature.POSITIVE)? "+" : "-",
                                                               retrievedGene.toString());
 							Sequence s = new SimpleSequence(symList, null, fastaHeader, ann);
 							
 							if (outputFormat.equals(FeatureOutputFormat.FASTA)) {
 								RichSequence.IOTools.writeFasta(os, s, null);
 							} else {
 								Annotation gann = nearestTranscript.getAnnotation();
 								SimpleGFFRecord rec = new SimpleGFFRecord();
 								rec.setSource(recLine.getSource());
 								rec.setSeqName(recLine.getSeqName());
 								rec.setFeature(gann.getProperty("ensembl.gene_id").toString());
 								rec.setStart(Math.max(1,start));
 								rec.setEnd(end);
 								rec.setStrand(recLine.getStrand());
 								rec.setScore(recLine.getScore());
                                 // for transcript in transcripts add group attributes
                                 Map<String,List<?>> attMap = new HashMap<String,List<?>>(recLine.getGroupAttributes()); 
                                 List<String> l = new ArrayList<String>();
                                 l.add(gann.getProperty("ensembl.id").toString());
                                 attMap.put("transcriptId", l);                                    
 								rec.setGroupAttributes(attMap);
 								gffWriter.recordLine(rec);
 								gffWriter.endDocument(); //force flush
 							}
 		
 		
 						} catch (IllegalIDException e) {
 							e.printStackTrace();
 						} catch (BioException e) {
 							e.printStackTrace();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				});
 		System.err.println();
 	}
 	
 
 
 
 	public static StrandedFeature transcriptWithClosestTSS(
 			String seqName,
 			int minPos,
 			int maxPos,
 			StrandedFeature.Strand strand,
 			SequenceDB seqDB, 
 			int maxDistFromGene,
 			boolean ignoreGenesWithNoCrossReferences) throws IllegalIDException, IndexOutOfBoundsException, BioException {
 		
 		System.err.printf("Getting genes close to %s:%d-%d%n", seqName, minPos-maxDistFromGene,maxPos+maxDistFromGene);
 		StrandedFeature nearestTranscript = null;
 		if (maxDistFromGene > 0) {
 			RangeLocation rangeLoc = new RangeLocation(minPos-maxDistFromGene,maxPos+maxDistFromGene);
 			FeatureHolder transcripts =
                 seqDB.getSequence(seqName).filter(new FeatureFilter.And(new FeatureFilter.OverlapsLocation(rangeLoc),
                                                                         new FeatureFilter.ByType("transcript")));
 			
 			System.err.printf("Transcripts found:%d%n",transcripts.countFeatures());
 			
 			if (!strand.equals(StrandedFeature.UNKNOWN)) {
 				transcripts = transcripts.filter(new FeatureFilter.StrandFilter(strand));
 			}
 			
 			Set<StrandedFeature> nearbyTranscripts;
 			int len = maxPos - minPos;
 			int centrePoint = minPos + len / 2;
 			
 			nearbyTranscripts = 
 					new TreeSet<StrandedFeature>(
 						new DistanceFromStartOfStrandedFeatureToPointLocationComparator(
 							new PointLocation(centrePoint))); /* The feature's centre point */
 			
 			for (Iterator<?> fi = transcripts.features(); fi.hasNext();) {
 				StrandedFeature transcript = (StrandedFeature) fi.next();
 				if (!transcript.getType().equals("transcript")) continue;
 				
 				Location loc = transcript.getLocation();
 				int tStart;
 				
 				if (transcript.getStrand().equals(StrandedFeature.POSITIVE)) {
 					tStart = loc.getMin();
 				} else {
 					tStart = loc.getMax();
 				}
 				
                 if (ignoreGenesWithNoCrossReferences) {					
 					boolean hasXrefs = transcript.getAnnotation().containsProperty("ensembl.xrefs");
 					if(!hasXrefs) {
 						System.err.printf("xrefs is null, continuing...\n");
 						continue;
 					}
 					Object xrefs = transcript.getAnnotation().getProperty("ensembl.xrefs");				
 					if (!(xrefs instanceof List)) {
                         System.err.printf("WARNING: xrefs is not an instance of List, skipping...\n");
                         continue;
                     }
 					
 					List xrefsList = (List)xrefs;
 					if (xrefsList.size() == 0) {
                         System.err.printf("xrefs list is empty, skipping...\n");
                         continue;
                     }
 				}
 				
 				RangeLocation tssLocationRange = new RangeLocation(tStart, tStart+1);
 				RangeLocation posLocationRange = new RangeLocation(
 						minPos-maxDistFromGene,
 						maxPos+maxDistFromGene);
 				
 				if (tssLocationRange.overlaps(posLocationRange)) {
 					if (transcript.getAnnotation().containsProperty("ensembl.id")) {
 						System.err.println(String.format(
 							"Feature is within +/- %d the transcript of gene %s",
 							maxDistFromGene,
 							transcript.getAnnotation().getProperty("ensembl.gene_id")));
 						nearbyTranscripts.add(transcript);
 					}
 				}
 				
 				List<StrandedFeature> featList = new ArrayList<StrandedFeature>(nearbyTranscripts);
 				if (featList.size() > 0) {
 					StrandedFeature f = featList.get(0);
 					nearestTranscript = f;				
 				
 				}
 			}
 		}
 		return nearestTranscript;
 	}
 
 }
