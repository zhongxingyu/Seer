 package net.derkholm.nmica.extra.app;
 
 import hep.aida.bin.StaticBin1D;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.derkholm.nmica.apps.MetaMotifBackgroundParameterEstimator;
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.matrix.Matrix2D;
 import net.derkholm.nmica.model.metamotif.Dirichlet;
 import net.derkholm.nmica.model.metamotif.DirichletParamEstimator;
 import net.derkholm.nmica.model.metamotif.IllegalAlphaParameterException;
 import net.derkholm.nmica.model.metamotif.MetaMotif;
 import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
 import net.derkholm.nmica.motif.Motif;
 import net.derkholm.nmica.motif.MotifComparitorIFace;
 import net.derkholm.nmica.motif.MotifIOTools;
 import net.derkholm.nmica.motif.MotifPair;
 import net.derkholm.nmica.motif.MotifTools;
 import net.derkholm.nmica.motif.SquaredDifferenceMotifComparitor;
 import net.derkholm.nmica.motif.align.MotifAlignment;
 import net.derkholm.nmica.motif.align.NoPositionFoundException;
 import net.derkholm.nmica.seq.WmTools;
 
 import org.biojava.bio.dist.Distribution;
 import org.biojava.bio.dist.DistributionTools;
 import org.biojava.bio.dist.UniformDistribution;
 import org.biojava.bio.dp.SimpleWeightMatrix;
 import org.biojava.bio.dp.WeightMatrix;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.symbol.AlphabetIndex;
 import org.biojava.bio.symbol.AlphabetManager;
 import org.biojava.bio.symbol.FiniteAlphabet;
 import org.biojava.bio.symbol.Symbol;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 import org.bjv2.util.cli.UserLevel;
 
 import cern.colt.list.DoubleArrayList;
 
 @App(overview = "Calculate summary statistics for motif sets", generateStub = true)
 @NMExtraApp(launchName = "nmmotifsum")
 public class MotifSetSummary {
 	private static final double VERY_NEGATIVE_DOUBLE = -500000000.0;
 	
 	private Motif[] motifs;
 	private Motif[] otherMotifs;
 	
 	private boolean perColAvgEntropy;
 	private boolean avgLength;
 	private boolean num;
 	private MetaMotif[] metamotifs;
 	private MotifComparitorIFace mc = SquaredDifferenceMotifComparitor.getMotifComparitor();
 
 	private boolean length = false;
 	private boolean perMotifAvgEntropy =  false;
 	private double threshold = Double.POSITIVE_INFINITY;
 	private boolean gcContent = false;
 	private boolean palindromicity = false;
 	private boolean bg = false;
 
 	private double maxAlphaSum = 50;
 	private double pseudoCount = 0;
 	private boolean reportAvgDiff;
 	private boolean bestHits;
 	private boolean bestReciprocalHits;
 
 	private String[] motifsFilenames;
 	private String[] otherMotifsFilenames;
 	
 	private boolean calcAll;
 	private boolean showName = true;
 	private boolean printHeader = true;
 	private boolean reportKD;
 	private boolean pairedOutput = true;
 	private boolean perColEntropy;
 	private String separator = " ";
 	
 	private boolean calcAvgMetaMotifScore = true;
 	private boolean calcMaxMetaMotifScore = true;
 	private boolean perMotifTotalEntropy;
 	private int[] indices;
 	private boolean merged;
 	private int minColPerPos = 2;
 	
 	HashMap<MetaMotif, File> metaMotifToFileMap = new HashMap<MetaMotif, File>();
 	HashMap<Motif, File> otherMotifToFileMap = new HashMap<Motif, File>();
 	private File outputAlignedFile;
 
 	private int maxHeaderColWidth = 192;
 	
 	@Option(help = "Input motif set file(s)")
 	public void setMotifs(File[] files) throws Exception {
 		List<Motif> motifList = new ArrayList<Motif>();
 		
 		for (File f : files) {
 			Motif[] ms = MotifIOTools.loadMotifSetXML(
 					new BufferedInputStream(new FileInputStream(f)));
 			for (Motif m : ms)
 				motifList.add(m);
 		}
 		this.motifs = motifList.toArray(new Motif[motifList.size()]);
 		
 		/*
 		this.motifsFilenames = new String[this.motifs.length];
 		
 		for (int i = 0; i < this.motifsFilenames.length; i++) {
 			this.motifsFilenames[i] = files[i].getName();
 		}*/
 	}
 	
 	@Option(help = "Calculate the average score with each of the metamotifs " +
 					"(sum probabilities with all alignments of motif X with metamotif X, " +
 					"divided by the length of the motif) (default=true)",
 					optional=true)
 	public void setAvgMetaMotifScore(boolean b) {
 		this.calcAvgMetaMotifScore  = b;
 	}
 	
 	@Option(help = "Calculate the maximum score with each of the metamotifs",
 			optional=true)
 	public void setMaxMetaMotifScore(boolean b) {
 		this.calcMaxMetaMotifScore  = b;
 	}
 
 	
 	@Option(help = "Other motif set file(s) to compare those given with -motifs", optional=true)
 	public void setOtherMotifs(File[] files) throws Exception {
 		List<Motif> motifList = new ArrayList<Motif>();
 		
 		for (File f : files) {
 			System.err.printf("Reading file %s%n",f.getPath());
 			Motif[] ms = MotifIOTools.loadMotifSetXML(
 					new BufferedInputStream(new FileInputStream(f)));
 			for (Motif m : ms) {
 				motifList.add(m);
 				otherMotifToFileMap.put(m,f);
 			}
 		}
 		this.otherMotifs = motifList.toArray(new Motif[motifList.size()]);
 		
 		/*
 		this.otherMotifsFilenames = new String[this.otherMotifs.length];
 		
 		for (int i = 0; i < this.otherMotifsFilenames.length; i++) {
 			this.otherMotifsFilenames[i] = files[i].getName();
 		}*/
 	}
 	
 	@Option(help="Calculate KD divergences", optional=true)
 	public void setKD(boolean b) {
 		this.reportKD = b;
 	}
 	
 	@Option(help="Field separator (options:space|tab)", optional=true, userLevel = UserLevel.EXPERT)
 	public void setSep(String sep) {
 		if (sep.equals("space")) {
 			this.separator = " ";
 		} else if (sep.equals("tab")) {
 			this.separator = "\t";
 		} else {
 			System.err.printf("Invalid separator '%s' given. Allowed values: space,tab %n");
 			System.exit(1);
 		}
 	}
 	
 	@Option(help = "Calculate all per-motif qualities",optional=true)
 	public void setAll(boolean b) {
 		this.calcAll = b;
 	}
 
 	@Option(help = "Show motif name",optional=true)
 	public void setName(boolean b) {
 		this.showName = b;
 	}
 	
 	@Option(help = "Add pseudocounts",optional=true)
 	public void setPseudoCount(double d) {
 		this.pseudoCount = d;
 	}
 
 	@Option(help = "Output as paired distances (default) " +
 			"rather than as a distance matrix",optional=true)
 	public void setPairedOutput(boolean b) {
 		this.pairedOutput = b;
 	}
 	
 	@Option(help = "Input metamotif set file(s) for hit seeking",optional=true)
 	public void setMetaMotifs(File[] files) throws Exception {
 		List<MetaMotif> metamotifList = new ArrayList<MetaMotif>();
 		
 		for (File f : files) {
 			System.err.printf("Reading metamotif file %s%n",f.getPath());
 			try { 
 				MetaMotif[] ms = MetaMotifIOTools.loadMetaMotifSetXML(
 						new BufferedInputStream(new FileInputStream(f)));
 				for (MetaMotif m : ms) {
 					metamotifList.add(m);
 					metaMotifToFileMap.put(m,f);
 				}
 			} catch (Exception e) {
 				System.err.println("Reading input metamotif set " + f.getName() + " failed.");
 				System.err.println(e);
 				System.exit(1);
 			}
 		}
 		
 		if (metamotifList.size() == 0) {
 			System.out.println("No metamotifs were given.");
 			System.exit(1);
 		}
 		this.metamotifs = metamotifList.toArray(new MetaMotif[metamotifList.size()]);
 	}
 	
 	@Option(help = "Report average entropy of the motif set (per column)", optional=true)
 	public void setColwiseAvgEntropy(boolean b) {
 		this.perColAvgEntropy = b;
 	}
 	
 	@Option(help = "Report entropy of the motif set for each motif column", optional=true)
 	public void setColwiseEntropy(boolean b) {
 		this.perColEntropy = b;
 	}
 	
 	@Option(help = "Report total entropy for each motif", optional=true)
 	public void setPerMotifTotalEntropy(boolean b) {
 		this.perMotifTotalEntropy = b;
 	}
 	
 	@Option(help = "Report average entropy for each motif", optional=true)
 	public void setPerMotifAvgEntropy(boolean b) {
 		this.perMotifAvgEntropy = b;
 	}
 	
 	@Option(help = "Report CG content", optional=true)
 	public void setGcContent(boolean b) {
 		this.gcContent = b;
 	}
 	
 	//FIXME: Get this from TransCrypt!
 	@Option(help = "Report palindromicity", optional=true)
 	public void setPalindromicity(boolean b) {
 		this.palindromicity = b;
 	}
 	
 	@Option(help = "Max alpha sum for the metamotif columns " +
 					"(others will be clamped to this value)", optional=true)
 	public void setMaxAlphaSum(double d) {
 		this.maxAlphaSum = d;
 	}
 	
 	@Option(help = "Report best hits between motif sets", optional=true)
 	public void setBestHits(boolean b) {
 		this.bestHits = b;
 	}
 	
 	@Option(help = "Report best reciprocal hits between motif sets", optional=true)
 	public void setBestReciprocalHits(boolean b) {
 		this.bestReciprocalHits = b;
 	}
 	
 	@Option(help = "Report length", optional=true)
 	public void setLength(boolean b) {
 		this.length = b;
 	}
 	
 	@Option(help = "Report background params", optional=true)
 	public void setBg(boolean b) {
 		this.bg = b;
 	}
 	
 	
 	@Option(help = "Report average length", optional=true)
 	public void setAvgLength(boolean b) {
 		this.avgLength = b;
 	}
 
 	@Option(help = "Print out a header row to the feature table", optional=true)
 	public void setHeader(boolean b) {
 		this.printHeader = b;
 	}
 	
 	@Option(help = "Report number of motifs in the set", optional=true)
 	public void setNum(boolean b) {
 		this.num = b;
 	}
 	
 	@Option(help = "Report average difference with the other motif set", optional=true)
 	public void setAvgDiff(boolean b) {
 		this.reportAvgDiff = b;
 	}
 	
 	@Option(help = "Calculate the summary for the motifs at the selected indices only", optional = true)
 	public void setAtIndex(int[] indices) {
 		this.indices = indices;
 	}
 	
 	@Option(help = "Align input motifs and merge the alignment (average motif), " +
 				   "calculate summary from this merged motif", optional = true)
 	public void setMerged(boolean b) {
 		this.merged = b;
 	}
 	
 	@Option(help="Minimum number of columns per position in the merged motif (default=2, only used in conjunction with -merged)",optional=true)
 	public void setMinCols(int i) {
 		this.minColPerPos  = i;
 	}
 	
 	@Option(help="Output aligned input motifs", userLevel = UserLevel.DEBUG, optional=true)
 	public void setOutputAligned(File f) {
 		this.outputAlignedFile = f;
 	}
 	
 	@Option(help="Maximum header column width in characters (default = 192)", optional=true, userLevel = UserLevel.DEBUG)
 	public void setMaxHeaderColWidth(int i) {
 		this.maxHeaderColWidth = i;
 	}
 	
 	//no need to make this configurable
 	//@Option(help="Metamotif name in header field name")
 	//public void setMetamotifName(boolean b) {
 	//	this.metaMotifNameInHeader = b;
 	//}
 	
 	public void main(String[] args) throws Exception {
 		if (calcAll) {
 			length = true;
 			perMotifAvgEntropy =  true;
 			perMotifTotalEntropy = true;
 			gcContent = true;
 			palindromicity = true;
 			bg = true;
 		}
 		
 		if (indices != null) {
 			Motif[] selMotifs = new Motif[indices.length];
 			for (int i = 0; i < indices.length; i++) {
 				selMotifs[i] = motifs[indices[i]];
 			}
 			motifs = selMotifs;
 		}
 		
 		if (merged && (motifs.length > 1)) {
 			MotifAlignment alignment = new MotifAlignment(motifs, mc);
 			alignment = new MotifAlignment(alignment.motifs(), mc);
 			alignment = new MotifAlignment(alignment.motifs(), mc);
 			alignment = alignment.alignmentWithZeroOffset();
 			if (outputAlignedFile != null) {
 				MotifIOTools.writeMotifSetXML(
 					new BufferedOutputStream(new FileOutputStream(outputAlignedFile)), 
 					new Motif[] {alignment.averageMotif()});
 			}
 			
 			try {
 				alignment = alignment.trimToColumnsPerPosition(minColPerPos);
 			} catch (NoPositionFoundException e) {
 				System.err.printf(
 					"WARNING! No position was found with at least %d columns. " +
 					"Will output the alignment without trimming.", minColPerPos);
 			}
 			motifs = new Motif[] {alignment.averageMotif()};
 		}
 		
 		if (pseudoCount > 0) {
 			for (Motif m : motifs)
 				MotifTools.addPseudoCounts(m,pseudoCount);
 			if (otherMotifs != null) 
 				for (Motif m : otherMotifs)
 					MotifTools.addPseudoCounts(m, pseudoCount);
 		} if (pseudoCount < 0) {
 			System.out.println(
 					"ERROR: -pseudoCount = " + 
 					pseudoCount + 
 					" + (nonnegative value required)");
 			System.exit(1);
 		}
 		
 		if (maxAlphaSum > 0 && metamotifs != null) {
 			for (MetaMotif m : metamotifs) {
 				for (int i = 0; i < m.columns(); i++) {
 					Dirichlet dd = m.getColumn(i);
 					if (dd.alphaSum() > maxAlphaSum) {
 						dd.scaleAlphaSum(maxAlphaSum / dd.alphaSum());
 					}
 				}
 			}
 		}
 		
 		if (perColEntropy) {
 			Distribution[] allDists = allDists();
 			Distribution elsewhere = new UniformDistribution((FiniteAlphabet)motifs[0].getWeightMatrix().getAlphabet());
 			double entropyElsewhere = DistributionTools.totalEntropy(elsewhere);
 			System.out.println(entropyElsewhere);
 			for (Motif m : motifs)
 				for (int i = 0; i < m.getWeightMatrix().columns(); i++) {
 					System.out.println(m.getName() + separator + 
 										i + separator + 
 										(entropyElsewhere - 
 										 DistributionTools.totalEntropy(
 												 m.getWeightMatrix().getColumn(i))));
 				}
 			
 			System.exit(0);	
 		}
 		
 		if (perColAvgEntropy) {
 			Distribution[] allDists = allDists();
 			double[] entropies = new double[allDists.length];
 			Distribution elsewhere = new UniformDistribution((FiniteAlphabet)motifs[0].getWeightMatrix().getAlphabet());
 			double entropyElsewhere = DistributionTools.totalEntropy(elsewhere);
 			
 			for (int i = 0; i < entropies.length; i++)
 				entropies[i] = entropyElsewhere - DistributionTools.totalEntropy(allDists[i]);
 				
 			StaticBin1D bin = new StaticBin1D();
 			bin.addAllOf(new DoubleArrayList(entropies));
 			System.out.println(bin.mean());
 			System.exit(0);
 		}
         
 		double[] allEntropies =  new double[this.motifs.length];
 		double[] allLengths =  new double[this.motifs.length];
 		double[] allTotalEntropies = new double[this.motifs.length];
 		
 		if (perMotifAvgEntropy || 
 			perMotifTotalEntropy) {
 			int mI = 0;
 			Distribution elsewhere = new UniformDistribution((FiniteAlphabet)motifs[0].getWeightMatrix().getAlphabet());
 			
 			for (Motif m : this.motifs) {
 				allTotalEntropies[mI] = MotifSetSummary.weightMatrixTotalEntropy(m.getWeightMatrix(), elsewhere);
 				allEntropies[mI++] = MotifSetSummary.weightMatrixAverageColumnEntropy(m.getWeightMatrix(), elsewhere);
 			}
 		}
 		
 		if (length) {
 			int mI = 0;
 			for (Motif m : this.motifs) {
 				allLengths[mI++] = m.getWeightMatrix().columns();
 			}
 		}
 		
 		double[][] metaMotifBestHits = new double[this.motifs.length][]; 
 		double[][] metaMotifAvgHits = new double[this.motifs.length][];
 		double[][] otherMotifBestHits = new double[this.motifs.length][];
 		List<String> headerCols = new ArrayList<String>();
 		
 		
 		if (bestHits || bestReciprocalHits) {
 			if (bestHits && bestReciprocalHits) {
 				System.err.println("-bestHits and -bestReciprocalHits are exclusive");
 				System.exit(1);
 			}
 		}
 		
 		if (bestHits) {
 			if (otherMotifs != null) {
 				//paired output cannot be combined with calculating other motifs.
 				if (pairedOutput) {
 					MotifPair[] mpairs = SquaredDifferenceMotifComparitor
 											.getMotifComparitor().bestHits(motifs, otherMotifs);
 					if (printHeader) {
 						System.out.println("motif1" + separator + "motif2" + separator + "score");
 					}
 					for (MotifPair mp : mpairs) {
 						System.out.print(mp.getM1().getName() + separator);
 						System.out.print(mp.getM2().getName() + separator);
 						System.out.print(mp.getScore() + "\n");
 					}
 					
 					//exit because can't be combine with other output forms
 					System.exit(0);
 				}
 				//not paired output with other motifs = table of distances with each column being one of the 'other' motifs
 				else {
 					System.err.println("Not paired output");
 					Matrix2D motifDistances = 
 						SquaredDifferenceMotifComparitor
 							.getMotifComparitor().bestHitsMatrix(motifs, otherMotifs);
 					
 					System.err.printf("rows: %d cols: %d%n", motifDistances.rows(), motifDistances.columns());
 					for (int i = 0; i < motifDistances.rows(); i++) {
 						System.err.printf("Row: %d columns: %d%n", i, motifDistances.columns());
 						otherMotifBestHits[i] = new double[motifDistances.columns()];
 						for (int j = 0; j < motifDistances.columns(); j++) {
 							double d = motifDistances.get(i, j);
 							otherMotifBestHits[i][j] = d;
 						}
 					}
 					
 				}
 			} 
 			
 			//print the distance matrix of motifs against themselves 
 			//again, a separate output mode from all the rest, 
 			//cannot be combined with calculating other features
 			else {
 				printSelfSimilaryMatrix(motifs, separator);
 				//exit because can't be combine with other output forms
 				System.exit(0);
 			}
 		}
 		
 		if (bestReciprocalHits) {
 			MotifPair[] mpairs = 
 				SquaredDifferenceMotifComparitor
 					.getMotifComparitor()
 						.bestReciprocalHits(motifs, otherMotifs);
 			
 			for (MotifPair mp : mpairs) {
 				System.out.print(mp.getM1().getName() + separator);
 				System.out.print(mp.getM2().getName() + separator);
 				System.out.print(mp.getScore() + "\n");
 			}
 			
 			//exit because can't be combine with other output forms
 			System.exit(0);
 		}
 		
 		/*
 		 * Why not score them with the 1D dynamic programming based tiling of metamotifs that 
 		 * you can actually fit in that motif?
 		 */
 		
 		if (metamotifs != null) {
 			for (int i = 0; i < motifs.length; i++) {
 				Motif m = motifs[i];
 				metaMotifBestHits[i] = new double[metamotifs.length];
 				metaMotifAvgHits[i] = new double[metamotifs.length];
 				
 				for (int j = 0; j < metamotifs.length; j++) {
 					MetaMotif mm = metamotifs[j];
 					double[] probs = null;
 					if (mm.columns() <= m.getWeightMatrix().columns())
 						probs = mm.logProbDensities(m.getWeightMatrix());
 					
 					if (probs != null) {
 						StaticBin1D bin = new StaticBin1D();
 						
 						for (double d : probs)
 							if (!Double.isInfinite(d) && !Double.isNaN(d))
 								bin.add(d);
 						
 						if (bin.size() > 0) {
 							metaMotifAvgHits[i][j] = bin.mean();
 							metaMotifBestHits[i][j] = bin.max();
 						} else {
 							metaMotifAvgHits[i][j] = VERY_NEGATIVE_DOUBLE;
 							metaMotifBestHits[i][j] = VERY_NEGATIVE_DOUBLE;
 						}
 					} else {
 						metaMotifAvgHits[i][j] = VERY_NEGATIVE_DOUBLE;
 						metaMotifBestHits[i][j] = VERY_NEGATIVE_DOUBLE;
 					}
 				}
 			}
 		}
 
 		double[] palindromicities = new double[motifs.length];
 		double[] gappedPalindromicities1 = new double[motifs.length];
 		double[] gappedPalindromicities2 = new double[motifs.length];
 		double[] gappedPalindromicities3 = new double[motifs.length];
 		double[] selfRepeatednesses = new double[motifs.length];
 		
 		
 		if (palindromicity) {
 			for (int m = 0; m < palindromicities.length; m++) {
 				palindromicities[m] = howPalindromic(motifs[m]);
 				gappedPalindromicities1[m] = howGappedPalindromic(motifs[m], 1);
 				gappedPalindromicities2[m] = howGappedPalindromic(motifs[m], 2);
 				gappedPalindromicities3[m] = howGappedPalindromic(motifs[m], 3);
 				selfRepeatednesses[m] = howSelfRepeating(motifs[m]);
 			}
 		}
 		
 
 		double[] gcContents = new double[this.motifs.length];
 		
 		if (gcContent) {
 			for (int m = 0; m < this.motifs.length; m++) {
 				double gc = 0;
 				double total = 0;
 					
 				for (int i = 0, cols = motifs[m].getWeightMatrix().columns(); i < cols; i++) {
 					Distribution distrib = motifs[m].getWeightMatrix().getColumn(i);
 					
 					for (Iterator it = ((FiniteAlphabet) distrib.getAlphabet())
 							.iterator(); it.hasNext();) {
 						Symbol sym = (Symbol) it.next();
 						if (sym.equals(DNATools.g()) || sym.equals(DNATools.c())){
 							gc = gc + distrib.getWeight(sym);
 						}
 						total = distrib.getWeight(sym);
 					}
 				}
 				gcContents[m] = gc / total;
 			}
 		}
 		
 		if (avgLength) {
 			double[] lengths = new double[motifs.length];
 			for (int i = 0; i < lengths.length; i++)
 				lengths[i] = motifs[i].getWeightMatrix().columns();
 			
 			StaticBin1D bin  = new StaticBin1D();
 			bin.addAllOf(new DoubleArrayList(lengths));
 			System.out.println(bin.mean());
 			
 			System.exit(0);
 		}
 		
 		if (num) {
 			System.out.println(motifs.length);
 			System.exit(0);
 		}
 		
 		double avgDiff = 0.0;
 
 		FiniteAlphabet alphab = (FiniteAlphabet) this.motifs[0].getWeightMatrix().getAlphabet();
 		double[] symmBGParams = new double[motifs.length];
 		double[][] asymmBGParams = new double[motifs.length][alphab.size()];
 		AlphabetIndex alphabIndex = AlphabetManager.getAlphabetIndex(alphab);
 		
 		if (bg) {
 			for (int m = 0; m < motifs.length; m++) {
 				Distribution[] ds = new Distribution[motifs[m].getWeightMatrix().columns()];
 				for (int i = 0; i < ds.length; i++) {
 					ds[i] = motifs[m].getWeightMatrix().getColumn(i);
 				}
 				Dirichlet dd;
 				try {
 					dd = DirichletParamEstimator.mle(ds,0.01);
 				} catch (IllegalAlphaParameterException e) {
 					System.err.printf(
 							"Could not estimate background for motif %s", motifs[m].getName());
 					dd = new Dirichlet((FiniteAlphabet)motifs[m].getWeightMatrix().getAlphabet());
 				}
 				Dirichlet symmDD = MetaMotifBackgroundParameterEstimator
 										.symmetricDirichlet(ds, 0.1, 10, 0.01);
 
 				for (int i = 0; i < asymmBGParams[m].length; i++) {
 					asymmBGParams[m][i] = dd.getWeight(alphabIndex.symbolForIndex(i));
 				}
 				//Symmetric, all weights the same --> let's just take the first one
 				symmBGParams[m] = symmDD.getWeight(alphabIndex.symbolForIndex(0));
 			}
 		}
 		
 		if (showName) headerCols.add("name");
 		if (length) headerCols.add("length");
 		if (perMotifAvgEntropy) headerCols.add("avg-entropy");
 		if (perMotifTotalEntropy) headerCols.add("total-entropy");
 		
 		if (palindromicity) {
 			headerCols.add("pal0");
 			headerCols.add("pal1");
 			headerCols.add("pal2");
 			headerCols.add("pal3");
 			headerCols.add("selfrep");
 		}
 		
 		if (bg) {
 			headerCols.add("bgsymm");
 			headerCols.add("bg_asymm_1");
 			headerCols.add("bg_asymm_2");
 			headerCols.add("bg_asymm_3");
 			headerCols.add("bg_asymm_4");
 		}
 		
 		if (otherMotifs != null) {
 			for (int m = 0; m < otherMotifs.length; m++) {
 				headerCols.add("besthit_" + otherMotifs[m].getName() + "("  + otherMotifToFileMap.get(otherMotifs[m].getName()) + ")");
 			}
 			
 		}
 		
 		if (metamotifs != null) {
 			if (calcAvgMetaMotifScore == false && calcMaxMetaMotifScore == false) {
 				
 				System.err.println("");
 				System.exit(1);
 			}
 			if (calcAvgMetaMotifScore) {
 				for (int mm = 0; mm < metamotifs.length; mm++) {
 					headerCols.add("hitavg_" + metamotifs[mm].getName() + 
 							"("+metaMotifToFileMap.get(metamotifs[mm]).getName()+")");
 				}
 			}
 			if (calcMaxMetaMotifScore) {
 				for (int mm = 0; mm < metamotifs.length; mm++) {
 					headerCols.add("hitmax_" + metamotifs[mm].getName() + 
 							"("+metaMotifToFileMap.get(metamotifs[mm]).getName()+")");
 				}
 			}
 		}
 		
 		if (printHeader) {
 			//first cap the header column lengths (so R doesn't complain...)
 			for (int i = 0; i < headerCols.size(); i++) {
 				String str = headerCols.get(i);
 				if (str.length() >= maxHeaderColWidth) {
 					str = str.substring(0,maxHeaderColWidth);
 					headerCols.set(i, str);
 				}
 			}
 			
 			for (int i = 0; i < headerCols.size(); i++) {
 				if (i < (headerCols.size() - 1))
 					System.out.print(headerCols.get(i) + separator);
 				else
 					System.out.print(headerCols.get(i) + "\n");
 			}
 		}
 		
 		for (int m = 0; m < motifs.length; m++) {
 			Motif mot = motifs[m];
 			boolean somethingPrinted = false;
 			
 			if (showName) {
 				System.out.print(mot.getName());
 				somethingPrinted = true;
 			}
 			if (length) {
 				if (somethingPrinted) System.out.print(separator);
 				System.out.print(allLengths[m] + separator);
 				somethingPrinted = true;
 			}
 			
 			if (perMotifAvgEntropy) {
 				if (somethingPrinted) System.out.print(separator);
 				System.out.print(allEntropies[m]);
 				somethingPrinted = true;
 			}
 			
 			if (perMotifTotalEntropy) {
 				if (somethingPrinted) System.out.print(separator);
 				System.out.print(allTotalEntropies[m]);
 				somethingPrinted = true;
 			}
 			
 			if (palindromicity) {
 				if (somethingPrinted) System.out.print(separator);
 				System.out.print(gappedPalindromicities1[m] + separator);
 				System.out.print(gappedPalindromicities2[m] + separator);
 				System.out.print(gappedPalindromicities3[m] + separator);
 				System.out.print(selfRepeatednesses[m] + separator);
 				somethingPrinted = true;
 			}
 			
 			if (bg) {
 				if (somethingPrinted) System.out.print(separator);
				System.out.print(symmBGParams[m] + separator);
 				
 				for (int i = 0; i < alphab.size(); i++) {
 					if (i < (alphab.size()-1)) {
 						System.out.print(asymmBGParams[m][i] + separator);
 					} else {
 						System.out.print(asymmBGParams[m][i]);
 					}
 				}
 				somethingPrinted = true;
 			}
 			
 			if (otherMotifs != null &! pairedOutput) {
 				if (somethingPrinted) System.out.print(separator);
 				for (int n = 0; n < otherMotifs.length; n++) {
 					System.err.printf("m:%d n:%d obj:%s%n",m,n,otherMotifBestHits.toString());
 					if (n < (otherMotifs.length-1)) {
 						System.out.print(otherMotifBestHits[m][n] + separator);
 					} else {
 						System.out.print(otherMotifBestHits[m][n]);
 					}
 				}
 				somethingPrinted = true;
 			}
 			
 			if (metamotifs != null) {
 				if (somethingPrinted) System.out.print(separator);
 				for (int mm = 0; mm < metamotifs.length; mm++) {
 					if (calcAvgMetaMotifScore) {
 						if (mm < (metamotifs.length-1))
 							System.out.print(metaMotifAvgHits[m][mm] + separator);
 						else
 							System.out.print(metaMotifAvgHits[m][mm]);
 						
 					}
 					if (calcMaxMetaMotifScore) {
 						if (mm < (metamotifs.length-1))
 							System.out.print(metaMotifBestHits[m][mm] + separator);
 						else
 							System.out.print(metaMotifBestHits[m][mm]);
 					}
 				}
 				
 				somethingPrinted = true;
 			}
 			
 			System.out.println();
 		}
 		
 	}
 
 	private void printSelfSimilaryMatrix(Motif[] motifs, String separator) throws Exception {
 		Matrix2D motifDistances = 
 			SquaredDifferenceMotifComparitor.getMotifComparitor().bestHitsMatrix(motifs);
 		
 		//header
 		for (int i = 0; i < motifs.length; i++) {
 			String namestr = motifs[i].getName();
 			
 			if (namestr.length() >= maxHeaderColWidth) {
 				namestr = namestr.substring(0,maxHeaderColWidth);
 			}
 			System.out.print(separator+namestr);
 		}
 		System.out.println();
 		
 		for (int i = 0; i < motifDistances.rows(); i++) {
 			//print out the motif name and then iterate
 			
 			System.out.print(motifs[i].getName() + separator);
 			for (int j = 0; j < motifDistances.columns(); j++) {
 				double d = motifDistances.get(i, j);
 				if (j < (motifDistances.columns()-1))
 					System.out.print(d + separator);
 				else
 					System.out.print(d + "\n");
 			}
 		}
 		
 		
 	}
 	
 	public static double weightMatrixAverageColumnEntropy(
 			WeightMatrix weightMatrix, Distribution elsewhere) {
 		double[] entropies = new double[weightMatrix.columns()];
 		double entropyElsewhere = DistributionTools.totalEntropy(elsewhere);
 		
 		for (int i = 0; i < entropies.length; i++) {
 			Distribution distribution = weightMatrix.getColumn(i);
 			entropies[i] = entropyElsewhere - DistributionTools.totalEntropy(distribution);
 		}
 		StaticBin1D bin = new StaticBin1D();
 		bin.addAllOf(new DoubleArrayList(entropies));
 		
 		return bin.mean();
 	}
 
 	public static double weightMatrixTotalEntropy(WeightMatrix weightMatrix, Distribution elsewhere) {
 		double[] entropies = new double[weightMatrix.columns()];
 		double entropyElsewhere = DistributionTools.totalEntropy(elsewhere);
 		
 		for (int i = 0; i < entropies.length; i++) {
 			Distribution distribution = weightMatrix.getColumn(i);
 			entropies[i] = entropyElsewhere - DistributionTools.totalEntropy(distribution);
 		}
 		StaticBin1D bin = new StaticBin1D();
 		bin.addAllOf(new DoubleArrayList(entropies));
 		
 		return bin.sum();
 	}
 
 	private Distribution[] allDists() {
 		List<Distribution> dists = new ArrayList<Distribution>();
 		
 		for (Motif m : motifs)
 			for (int i = 0; i < m.getWeightMatrix().columns(); i++)
 				dists.add(m.getWeightMatrix().getColumn(i));
 		
 		return dists.toArray(new Distribution[dists.size()]);
 	}
 	
 	private static double div(Distribution d0, Distribution d1) throws Exception {
         double cScore = 0.0;
         for (Iterator i = ((FiniteAlphabet) d0.getAlphabet()).iterator(); i.hasNext(); ) {
              Symbol s= (Symbol) i.next();
              cScore += Math.pow(d0.getWeight(s) - d1.getWeight(s), 2.0);
         }
         // return Math.sqrt(cScore);
         // return cScore;
         return Math.pow(cScore, 2.5 / 2.0);
     }
 
     public static double compareMotifs(WeightMatrix wm0, 
     									Distribution pad0, 
     									WeightMatrix wm1, 
     									Distribution pad1)
 										throws Exception {
 		double bestScore = Double.POSITIVE_INFINITY;
 		int minPos = -wm1.columns();
 		int maxPos = wm0.columns() + wm1.columns();
 		for (int offset = -wm1.columns(); offset <= wm0.columns(); ++offset) {
 			double score = 0.0;
 			for (int pos = minPos; pos <= maxPos; ++pos) {
 				Distribution col0 = pad0, col1 = pad1;
 				if (pos >= 0 && pos < wm0.columns()) {
 					col0 = wm0.getColumn(pos);
 					
 				}
 				int opos = pos - offset;
 				if (opos >= 0 && opos < wm1.columns()) {
 					col1 = wm1.getColumn(opos);
 				}
 				double cScore = div(col0, col1);
                 score += cScore;
 			}
 			bestScore = Math.min(score, bestScore);
 		}
 		return bestScore;
 	}
     
     //TODO: Use howPalindromic
     //TODO: Use howSelfRepeating
     //FIXME: Implement howSelfRepeating which allows you to tell the periodicity of repeating
     //TODO: Use howGappedPalindromic
     
 	public static double howPalindromic(Motif m) throws Exception {
 		WeightMatrix wmOrig = m.getWeightMatrix();
 		WeightMatrix[] wms = splitInTwo(wmOrig,0);
 		Distribution elsewhere = new UniformDistribution((FiniteAlphabet)wmOrig.getAlphabet());
 		return compareMotifs(
 				wms[0], elsewhere, 
 				WmTools.reverseComplement(wms[1]), 
 				elsewhere);
 	}
     
 	public static double howSelfRepeating(Motif m) throws Exception {
 		WeightMatrix wmOrig = m.getWeightMatrix();
 		WeightMatrix[] wms = splitInTwo(wmOrig,0);
 		
 		Distribution elsewhere = new UniformDistribution((FiniteAlphabet)wmOrig.getAlphabet());
 		return compareMotifs(wms[0], elsewhere, wms[1], elsewhere);
 	}
 	
 	public static double howGappedPalindromic(Motif m, int gap) throws Exception {
 		WeightMatrix wmOrig = m.getWeightMatrix();
 		WeightMatrix[] wms = splitInTwo(wmOrig,gap);
 		Distribution elsewhere = new UniformDistribution((FiniteAlphabet)wmOrig.getAlphabet());
 		return compareMotifs(wms[0], elsewhere, wms[1], elsewhere);		
 	}
 
 	public static WeightMatrix[] splitInTwo(WeightMatrix wm, int gap) throws Exception {
 		WeightMatrix[] wms = new SimpleWeightMatrix[2];
 		Distribution[] dists1,dists2;
 		Distribution elsewhere = new UniformDistribution((FiniteAlphabet)wm.getAlphabet());
 		
 		//TODO: Figure out how to generalise this
 		if (wm.columns() % 2 == 0) {
 			dists1 = new Distribution[wm.columns()/2];
 			dists2 = new Distribution[wm.columns()/2];
 		} else {
 			dists1 = new Distribution[wm.columns()/2];
 			dists2 = new Distribution[wm.columns()/2+1];	
 		}
 		for (int i=0; i < wm.columns()/2;i++) {
 			if (i <= wm.columns()/2 - gap) {
 				dists1[i] = wm.getColumn(i);
 			} else {
 				dists1[i] = elsewhere;
 			}
 		}
 		for (int i=wm.columns()/2; i < wm.columns(); i++) {
 			//dists2[i] = wm.getColumn(i);
 			if (i >= wm.columns()/2 + gap) {
 				dists2[i-wm.columns()/2] = wm.getColumn(i);
 			} else {
 				dists2[i-wm.columns()/2] = elsewhere;
 			}
 		}
 		wms[0] = new SimpleWeightMatrix(dists1);
 		wms[1] = new SimpleWeightMatrix(dists2);
 		return wms;
 	}
 }
