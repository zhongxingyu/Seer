 package net.derkholm.nmica.extra.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamReader;
 
 import net.derkholm.nmica.apps.ConsensusMotifCreator;
 import net.derkholm.nmica.apps.MetaMotifFinder;
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.maths.DoubleFunction;
 import net.derkholm.nmica.maths.IdentityDoubleFunction;
 import net.derkholm.nmica.matrix.Matrix1D;
 import net.derkholm.nmica.matrix.Matrix2D;
 import net.derkholm.nmica.matrix.MatrixTools;
 import net.derkholm.nmica.matrix.ObjectMatrix1D;
 import net.derkholm.nmica.matrix.SimpleMatrix2D;
 import net.derkholm.nmica.model.ContributionGroup;
 import net.derkholm.nmica.model.ContributionItem;
 import net.derkholm.nmica.model.Datum;
 import net.derkholm.nmica.model.Facette;
 import net.derkholm.nmica.model.SimpleContributionGroup;
 import net.derkholm.nmica.model.SimpleContributionItem;
 import net.derkholm.nmica.model.SimpleDatum;
 import net.derkholm.nmica.model.SimpleFacetteMap;
 import net.derkholm.nmica.model.SimpleMultiICAModel;
 import net.derkholm.nmica.model.metamotif.Dirichlet;
 import net.derkholm.nmica.model.metamotif.MetaMotif;
 import net.derkholm.nmica.model.metamotif.MetaMotifFacette;
 import net.derkholm.nmica.model.metamotif.MetaMotifIOTools;
 import net.derkholm.nmica.model.metamotif.NamedMotifSet;
 import net.derkholm.nmica.model.metamotif.RingBufferMetaMotif;
 import net.derkholm.nmica.model.metamotif.bg.MetaMotifDirichletBackground;
 import net.derkholm.nmica.model.motif.Mosaic;
 import net.derkholm.nmica.model.motif.MosaicIO;
 import net.derkholm.nmica.model.motif.MosaicSequenceBackground;
 import net.derkholm.nmica.model.motif.MotifClippedSimplexPrior;
 import net.derkholm.nmica.model.motif.MotifFacette;
 import net.derkholm.nmica.model.motif.NMWeightMatrix;
 import net.derkholm.nmica.model.motif.PosSpecWeightMatrixPrior;
 import net.derkholm.nmica.model.motif.SequenceBackground;
 import net.derkholm.nmica.motif.Motif;
 import net.derkholm.nmica.motif.MotifIOTools;
 import net.derkholm.nmica.motif.MotifTools;
 import net.derkholm.nmica.seq.NMSimpleDistribution;
 
 import org.biojava.bio.BioException;
 import org.biojava.bio.dist.Distribution;
 import org.biojava.bio.dp.WeightMatrix;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceIterator;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.db.IllegalIDException;
 import org.biojava.bio.seq.db.SequenceDB;
 import org.biojava.bio.seq.io.SeqIOTools;
 import org.biojava.bio.symbol.FiniteAlphabet;
 import org.biojava.bio.symbol.IllegalAlphabetException;
 import org.biojava.bio.symbol.Symbol;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.ConfigurationException;
 import org.bjv2.util.cli.Option;
 import org.bjv2.util.cli.UserLevel;
 
 //TODO: Implement in a more generic way (such that it also works with motifs)
 @App(overview = "Model evaluator " +
 		"(used for debugging purposes to " +
 		"check correctness of likelihood calculations)", generateStub = true)
 @NMExtraApp(launchName = "nmeval", vm = VirtualMachine.SERVER)
 public class ModelScoreEvaluator {
 	public static final String OCC_COL_SEPARATOR = ";";
 	protected MetaMotif[] metaMotifs;
 	protected Motif[] motifs;
 	protected File occupancyMatrixFile;
 	protected File inputDataSet;
 	protected double bgAlphaSum = 2.5;
 	protected double uncountedExpectation = 1;
 	protected boolean revComp = false;
 	protected double edgePruneThreshold = 0;
 	protected double pseudoCount;
 	protected ContributionGroup contribGrp;
 	
 	private int mixturePermutations = 0;
 	private int modelShuffles = 0;
 	private SimpleMultiICAModel model;
 	private Occupancy<?,?> occ;
 	private SequenceDB seqs;
 	
 	private boolean motifModel;
 	private MosaicSequenceBackground seqBackgroundModel;
 	private double edgePrune = 0.0;
 	
 	private int minLength = -1;
 	private int maxLength = 10;
 	private int extraLength = 0;
 	private double minClip = Double.NEGATIVE_INFINITY;
     private double maxClip = 1.0;
 	private boolean dirichletPriorFsSpecified;
 	private boolean consensusStrsSpecified;
 	
 	private String[] consensusStrings;
 	private File[] dirichletPriorFiles;
 	private double priorPrecision = ConsensusMotifCreator.DEFAULT_ALPHASUM;
 	private double priorPseudoCount = ConsensusMotifCreator.DEFAULT_PSEUDOCOUNT;
 	private boolean priorPrecisionOrPseudoCountSpecified = false;
 	
 	//TODO: Integrate this type of model evaluation into being part of nmmetainfer 
 	//such that you can compare the current likelihood values to those achievable for the correct model
 	//as a benchmarking mode
 	
 	@Option(help = "The metamotifs models", 
 			optional = true)
 	public void setMetaMotifs(File[] files) throws Exception {
 		this.metaMotifs = MetaMotifIOTools.loadMetaMotifsFromMultipleFiles(files);
 	}
 	
 	@Option(help = "The sequences for which model (motif set + occupancy) likelihood is to be evaluated", 
 			optional = true)
 	public void setSeqs(File file) throws Exception {
 		this.seqs = loadDB(file, DNATools.getDNA());
 	}	
 
     @Option(help="XMS file with metamotif models to read a column-specific informative prior from " +
     		"(-consensus can also be used to specify one)", optional=true)
     public void setPriorMetaMotifs(File[] f) {
     	this.dirichletPriorFiles = f;
     	dirichletPriorFsSpecified = true;
     }
     
 	@Option(help="Consensus string(s) that will be made to metamotif models", optional=true)
 	public void setConsensus(String[] strs) {
 		this.consensusStrings = strs;
 	}
 	
 	@Option(help="The Dirichlet prior precision parameter " +
 			"(used in conjunction with -consensus)", optional=true, userLevel=UserLevel.EXPERT)
 	public void setPriorPrecision(double d) {
 		this.priorPrecision = d;
 		this.priorPrecisionOrPseudoCountSpecified = true;
 	}
 	
 	@Option(help="The Dirichlet prior mean pseudocount " +
 			"(used in conjunction with -consensus)", optional=true, userLevel=UserLevel.EXPERT)
 	public void setPriorPseudoCount(double d) {
 		this.priorPseudoCount = d;
 		this.priorPrecisionOrPseudoCountSpecified = true;
 	}
 
 	@Option(help="The sequence background model to use for likelihood calculation",optional=true)
 	public void setBackgroundModel(InputStream is)
         throws Exception {;
         XMLInputFactory factory = XMLInputFactory.newInstance();
 	
 		XMLStreamReader r = factory.createXMLStreamReader(is);
 		Mosaic m = MosaicIO.readMosaic(r);
 		seqBackgroundModel = new MosaicSequenceBackground(m.getDistributions(), m.getTransition());
 	}
 	
 	private SequenceDB loadDB(File file, FiniteAlphabet alphabet) 
 		throws 
 			NoSuchElementException, 
 			BioException, 
 			FileNotFoundException {
 		
         SequenceIterator si = SeqIOTools.readFasta(
         		new BufferedReader(
         				new FileReader(file)),
         				alphabet.getTokenization("token"));
         SequenceDB seqDB = new HashSequenceDB();
         while (si.hasNext()) {
             Sequence seq = si.nextSequence();
             if (seqDB.ids().contains(seq.getName())) {
                 throw new IllegalIDException(
             		"Duplicate sequence name '" + seq.getName() + "'");
             }
             seqDB.addSequence(seq);
         }
         return seqDB;
 	}
 
 	public void setMetaMotifs(MetaMotif[] mms) {
 		this.metaMotifs = mms;
 	}
 	
 	@Option(help = "The input dataset to score with the given models", 
 			optional = true)
 	public void setMotifs(File file) throws Exception {
 		this.motifs = MotifIOTools.loadMotifSetXML(new FileInputStream(file));
 	}
 		
 	@Option(help = "The background alpha parameter " +
 			"(for an uniform 0th order Dirichlet background)", 
 			optional = true, userLevel = UserLevel.EXPERT)
 	public void setBgAlphaSum(double d) {
 		this.bgAlphaSum = d;
 	}
 	
 	@Option(help = "Allow motifs to occur in either orientation", 
 			optional = true)
 	public void setRevComp(boolean b) {
 		this.revComp  = b;
 	}
 	
 	@Option(help = "The expected number of motif occurrences per sequence", 
 			userLevel = UserLevel.EXPERT, optional = true)
 	public void setUncountedExpectation(double i) {
 		this.uncountedExpectation = i;
 	}
 
 	@Option(help = "Pseudocounts to add to input motifs", 
 			userLevel = UserLevel.EXPERT, optional = true)
 	public void setPseudoCount(double d) {
 		this.pseudoCount = d;
 	}
 
 	@Option(help = "The occupancy matrix " +
 			"(see nmeval -occmatrixformat for help)", optional=true)
 	public void setOcc(File file) throws Exception {
 		this.occupancyMatrixFile = file;
 	}
 	
 	@Option(help = "Number of mixing matrix permutations to make " +
 					"(to compare the specified model state against others)", 
 					optional = true)
 	public void setMixPerms(int i) {
 		this.mixturePermutations  = i;
 	}
 	
 	@Option(help = "Number of metamotif model shuffling operations to make " +
 					"(to compare the specified model state against others)", 
 					optional = true)
 	public void setShuffles(int i) {
 		this.modelShuffles = i;
 	}
 	
 	public SimpleMultiICAModel makeModel() 
 	throws Exception {
 		if (!motifModel)
 			makeAndFillOccupancyMatrix(occupancyMatrixFile);
 		
 		contribGrp = new SimpleContributionGroup("metamotifs",
 				MetaMotif.class);
 		
 		
 		Facette[] facettes;
 		MotifFacette[] mFacettes;
 		MetaMotifDirichletBackground mmBackground = null;
 		DoubleFunction mixTransferFunction = IdentityDoubleFunction.INSTANCE;
 		
 		//if evaluating sequence--motif model
 		if (motifModel) {
 			facettes = mFacettes = new MotifFacette[1];
 			
 			mFacettes[0] = new MotifFacette(
                     seqBackgroundModel,
                     0.0,
                     true,
                     true,
                     revComp,
                     uncountedExpectation,
                     false,
                     edgePrune,
                     DNATools.getDNA()
                 );
 			mFacettes[0].setMixTransferFunction(mixTransferFunction);
 			
 		} 
 		//if evaluating motif--metamotif model
 		else {
 			MetaMotifDirichletBackground background = 
 				new MetaMotifDirichletBackground(bgAlphaSum,DNATools.getDNA());
 			
 			MetaMotifFacette[] mmFacettes;
 			facettes = mmFacettes = new MetaMotifFacette[1];
 			
 			
 			facettes[0] = new MetaMotifFacette(
 					background, 
 					revComp,
 					uncountedExpectation, 
 					edgePruneThreshold, 
					background.getAlphabet());
 			mmFacettes[0].setMixTransferFunction(mixTransferFunction);
 		}			
 		
 		SimpleFacetteMap facetteMap = new SimpleFacetteMap(
 				new ContributionGroup[] {contribGrp}, 
 				facettes);
 		
 		facetteMap.setContributesToFacette(
 				contribGrp, 
 				facettes[0], 
 				true);
 		
 		Datum[] data;
 		SimpleMultiICAModel model;
 		
 		
 		//if were're evaluating a sequence--motif model
 		if (motifModel) {
 			data = loadSequenceData(
 					new SequenceDB[] {seqs},
 					new SequenceBackground[] {seqBackgroundModel});
 			
 			for (Motif m : motifs) {
 				MotifTools.addPseudoCounts(m.getWeightMatrix(),pseudoCount);
 				
 				for (int i = 0; i < m.getWeightMatrix().columns(); i++) {
 					Distribution distrib = m.getWeightMatrix().getColumn(i);
 					
 					for (Iterator it = ((FiniteAlphabet) distrib.getAlphabet())
 							.iterator(); it.hasNext();) {
 						Symbol sym = (Symbol) it.next();
 						
 						System.err.printf("%f ", distrib.getWeight(sym));
 					}
 					System.err.println();
 				}
 			}
 			
 			model = new SimpleMultiICAModel(facetteMap, data, motifs.length);
 			
 			for (int i = 0; i < model.getContributions(contribGrp).size(); i++) {
 				model
 					.getContributions(contribGrp)
 						.set(i, new SimpleContributionItem(motifs[i].getWeightMatrix()));
 			}
 			for (int i = 0, rows=model.getMixingMatrix().rows(); i < rows; i++) {
 				for (int j = 0, cols=model.getMixingMatrix().columns(); j < cols; j++) {
 					model.getMixingMatrix().set(i, j, 1.0);
 				}
 			}
 			
 		} 
 		//if we're evaluating a motif--metamotif model
 		else {
 			NamedMotifSet[] motifSets = new NamedMotifSet[1];
 			motifSets[0] = new NamedMotifSet(motifs, null);
 			data = MetaMotifFinder.loadData(motifSets);
 			MetaMotifFinder.addPseudoCounts(data, pseudoCount);
 			
 			model = new SimpleMultiICAModel(facetteMap, data, metaMotifs.length);
 			
 			for (int i = 0; i < model.getContributions(contribGrp).size(); i++) {
 				model
 					.getContributions(contribGrp)
 						.set(i, new SimpleContributionItem(metaMotifs[i]));
 			}
 		}
 				
 		if (!motifModel) fillMotifMetaMotifMixingMatrix(model, (Occupancy<Motif,MetaMotif>)occ);
 		else fillSequenceMotifMixingMatrix(model,(Occupancy<Sequence,Motif>)occ);
 	
 
 		/*
 		if (motifModel) {
 			Set<String> ids = seqs.ids();
 			System.err.println("Data length   :" + data.length);
 			System.err.println("First data obj:" + data[0]);
 			
 			double likelihood = 0;
 			System.err.println(seqBackgroundModel);
 			for (int i = 0; i < data.length; i++) {
 				Datum d = data[i];
 				System.err.println("Datum facetted data length: " + d.getFacettedData().length);
 				Sequence seq = (Sequence)d.getFacettedData()[0];
 				MotifUncountedLikelihood likelihoodCalc = 
 						new MotifUncountedLikelihood(
 								(MotifFacette)facettes[0], seq);
 				double thisLikelihood = likelihoodCalc.likelihood(
 										model.getContributions(contribGrp), 
 										model.getMixture(i));
 				for (int j = 0; j < model.getMixture(i).size(); j++) {
 					System.err.printf("%f ", model.getMixture(i).get(j));
 				}
 				System.err.println();
 				System.err.println(thisLikelihood);
 				likelihood = likelihood + thisLikelihood;
 			}
 			System.err.println("man likelihood:" + likelihood);
 		}
 		*/
 		
 		return model;
 	}
 	
 	private Datum[] loadSequenceData(
 			SequenceDB[] seqDBs, 
 			SequenceBackground[] backgroundModels) 
 				throws IllegalIDException, BioException {
 		
 		Datum[] data;
 		Set<String> allIds = new HashSet<String>();
         for (int s = 0; s < seqDBs.length; ++s) {
             allIds.addAll((Set<? extends String>) seqDBs[s].ids());
         }
         
         List<Datum> dl = new ArrayList<Datum>();
         for (Iterator<String> i = allIds.iterator(); i.hasNext(); ) {
             String id = i.next();
             Sequence[] datumSeqs = new Sequence[seqDBs.length];
             boolean gotDatumSeq = false;
             for (int s = 0; s < seqDBs.length; ++s) {
                 if (seqDBs[s].ids().contains(id)) {
                     Sequence seq = seqDBs[s].getSequence(id);
                     
                     int ssOrder = 0;
                     SequenceBackground ssBg = 
                     	backgroundModels.length > 1 ? 
                     			backgroundModels[s] : backgroundModels[0];
                     if (ssBg instanceof MosaicSequenceBackground) {
                     	ssOrder = ((MosaicSequenceBackground) ssBg)
                     		.getBackgroundDistributions()[0].getAlphabet().getAlphabets().size() - 1;
                     }
                     
                     datumSeqs[s] = seqDBs[s].getSequence(id);
                     gotDatumSeq = true;
                 }
             }
             if (gotDatumSeq) {
                 dl.add(new SimpleDatum(id, datumSeqs));
             }
         }
         data = dl.toArray(new Datum[dl.size()]);
         return data;
 	}
 
 	//this method is a bridge between the OccupancyMatrix objects 
 	//should really just read the occupancy matrix
 	private void fillMotifMetaMotifMixingMatrix(
 			SimpleMultiICAModel model,
 			Occupancy<Motif, MetaMotif> occMatrix) {
 		for (Motif motif : occMatrix.dataEntries) {
 			int mI = occMatrix.indexOfDataEntry(motif);
 			Set<MetaMotif> mms = occMatrix.getModels(motif);
 			Matrix1D mixture = model.getMixture(mI);
 			for (MetaMotif mm : mms) mixture.set(occMatrix.indexOfModel(mm), 1.0);
 		}
 	}
 	
 	//this method is a bridge between the OccupancyMatrix objects 
 	//should really just read the occupancy matrix
 	private void fillSequenceMotifMixingMatrix(
 			SimpleMultiICAModel model, 
 			Occupancy<Sequence, Motif> occMatrix) {
 		for (Sequence motif : occMatrix.dataEntries) {
 			int mI = occMatrix.indexOfDataEntry(motif);
 			Set<Motif> mms = occMatrix.getModels(motif);
 			Matrix1D mixture = model.getMixture(mI);
 			for (Motif mm : mms) mixture.set(occMatrix.indexOfModel(mm), 1.0);
 		}
 	}
 	
 	public static void printMixingMatrix(SimpleMultiICAModel model) {
 		for (int i = 0; i < model.getMixingMatrix().rows(); i++) {
 			for (int j = 0; j < model.getMixingMatrix().columns(); j++) {
 				System.out.print(model.getMixingMatrix().get(i, j) + " ");
 			}
 			System.out.print("\n");
 		}
 	}
 
 	public SimpleMultiICAModel getModel() {
 		return model;
 	}
 	
 	public void main(String[] args) throws Exception {
 		if (seqs != null && motifs != null) {
 			motifModel = true;
 		} else if (motifs != null && metaMotifs != null) {
 			motifModel = false;
 		}
 		
 		if (seqs != null && metaMotifs != null) {
 			System.err.println("Specify either -seqs or -metaMotifs, not both");
 			System.exit(1);
 		}
 		
         if (consensusStrsSpecified && dirichletPriorFsSpecified)
         	throw new ConfigurationException(
     			"Define either -consensus or -dirichletPrior, not both");
         
 		RingBufferMetaMotif[] priorMetamotifs = null;
     	
         if (consensusStrsSpecified) {
         	System.err.println("Consensus strings specified");
         	priorMetamotifs =             		
         		(RingBufferMetaMotif[]) 
 	        		toRingBufferMetaMotifs(ConsensusMotifCreator.consensusToMetamotifs(
 	        			consensusStrings,
 	        			priorPseudoCount,
 	        			priorPrecision,
 	        			DNATools.getDNA()));
         	
         	
         } else if (dirichletPriorFsSpecified) {
         	System.err.println("Dirichlet prior specified");
         	if (this.priorPrecisionOrPseudoCountSpecified) {
         		System.err.println(
         				"Prior precision or pseudocount were specified " +
         				"(either motifset file loaded as metamotifs with " +
         				"fixed per-column precisions, " +
         				"or metamotif-specific precision overriden)");
             	priorMetamotifs = 
             		(RingBufferMetaMotif[]) 
             			toRingBufferMetaMotifs(MetaMotifIOTools
             				.loadMetaMotifsFromMultipleFiles(
             						dirichletPriorFiles, 
             						this.priorPseudoCount, 
             						this.priorPrecision));
             	
             	System.err.printf("pseudocount: %f precision: %f%n", 
             			priorPseudoCount, priorPrecision );
             	for (MetaMotif mm : priorMetamotifs) {
             		for (int i=0,cols=mm.columns(); i < cols; i++) {
             			Dirichlet dir = mm.getColumn(i);
             			
             			for (Iterator it = ((FiniteAlphabet) dir
 								.getAlphabet()).iterator(); it.hasNext();) {
 							Symbol sym = (Symbol) it.next();
 							System.err.printf("%f ",dir.getWeight(sym));
 						}
             		}
             		System.err.println();
             	}
 
         	} else {
         		System.err.println("Metamotif prior specified (no pseudocount specified or precision overrides)");
         		//this XMS file should contain correctly annotated metamotifs
             	priorMetamotifs = (RingBufferMetaMotif[]) toRingBufferMetaMotifs(MetaMotifIOTools
                 		.loadMetaMotifsFromMultipleFiles(dirichletPriorFiles));            		
         	}
         }
         
         System.err.println("Prior metamotifs: "+priorMetamotifs);
         model = makeModel();
 		System.out.printf("likelihood %f%n", model.likelihood());
 		
         MotifClippedSimplexPrior uninfoPrior = 
 			new MotifClippedSimplexPrior(
 					DNATools.getDNA(), 
 					minLength, maxLength, maxLength + extraLength, minClip, maxClip);
         PosSpecWeightMatrixPrior pswmpPrior = 
     		new PosSpecWeightMatrixPrior
     			(DNATools.getDNA(), 
 				 minLength, 
 				 maxLength, 
 				 maxLength + extraLength, 
 				 priorMetamotifs);
 		
 		ObjectMatrix1D<?> contribs = model.getContributions(contribGrp);
 		double accumProbability = 0;
 		for (int i = 0; i < contribs.size(); i++) {
 			ContributionItem nwmItem = (ContributionItem) contribs.get(i);
 			WeightMatrix wm = (WeightMatrix)nwmItem.getItem();
 			NMWeightMatrix nwm = wmtoNWM(wm, wm.columns(), 0);
 			
 			double uninfoLogProbability = uninfoPrior.probability(nwm);
 			double infoLogProbability = pswmpPrior.probability(nwm);
 			System.err.printf("motif%d prior %f %f%n",i,uninfoLogProbability, infoLogProbability);
 			accumProbability = accumProbability + uninfoLogProbability;
 		}
 		
 		if (mixturePermutations > 0) {
 			//System.out.println("Likelihoods after mixing matrix permutations:");
 			//double[] likelihoods = new double[mixturePermutations];
 			for (int i=0; i < mixturePermutations; i++) {
 				SimpleMultiICAModel permutedModel = permuteMixture(model);
 				//likelihoods[i] = permutedModel.likelihood();
 				System.err.println(permutedModel.likelihood());
 			}
 		}
 		
 		if (modelShuffles >  0) {
 			System.out.println("Shuffled likelihoods:");
 			double[] likelihoods = new double[modelShuffles];
 			for (int i=0;i < modelShuffles; i++) {
 				SimpleMultiICAModel shuffledModel = shuffleMetaMotifs(model);
 				likelihoods[i] = shuffledModel.likelihood();
 			}
 		}
 			
 	}
 
 	private NMWeightMatrix 
 		wmtoNWM(WeightMatrix wm, int cols, int offset) 
 		throws IllegalAlphabetException {
 		NMSimpleDistribution[] dists 
 			= new NMSimpleDistribution[cols + extraLength];
 		
 		//System.err.printf("cols: %d wm.columns(): %d %n",cols,wm.columns());
 		
 		for (int i = 0,len=wm.columns(); i < len; i++) {
 			dists[i] = new NMSimpleDistribution(wm.getColumn(i));
 		}
 		
 		return new NMWeightMatrix(dists, cols, offset);
 	}
 	
 	//TODO: Implement
 	private SimpleMultiICAModel shuffleMetaMotifs(SimpleMultiICAModel model) {
 		SimpleMultiICAModel shuffledModel = new SimpleMultiICAModel(model);
 		
 		net.derkholm.nmica.matrix.ObjectMatrix1D contribs = shuffledModel.getContributions(contribGrp);
 		for (int i = 0; i < shuffledModel.getComponents(); i++) {
 			ContributionItem contribItem = (ContributionItem)contribs.get(i);
 		}
 		
 		return shuffledModel;
 	}
 
 	private static List<Integer> fromTo(int from, int to) {
 		List<Integer> ints = new ArrayList<Integer>(to -from + 1);
 		for (int i = from; i <= to; i++)
 			ints.add(i);
 		
 		return ints;
 	}
 	
 	private Matrix1D reorder(Matrix1D matrix, List<Integer> is) {
 		double[] ds = matrix.getRaw().clone();
 		
 		for (int i = 0; i < is.size(); i++)
 			matrix.set(is.get(i), ds[i]);
 		
 		return matrix;
 	}
 	
 	//hack.hack.hack.
 	//TODO: Check row and column indices...
 	private SimpleMultiICAModel permuteMixture(SimpleMultiICAModel model) {
 		Matrix2D mixtMatrixCopy = new SimpleMatrix2D(model.getMixingMatrix());
 		
 		for (int j = 0; j < metaMotifs.length; j++) {
 			List<Integer> ints = fromTo(0, motifs.length - 1); //s
 			Collections.shuffle(ints);
 			for (int k=0; k < ints.size(); k++) {
 				mixtMatrixCopy.set(j, k, model.getMixingMatrix().get(j, ints.get(k)));
 			}
 		}
 		
 		SimpleMultiICAModel permutedModel = new SimpleMultiICAModel(model);
 		MatrixTools.copy(permutedModel.getMixingMatrix(), mixtMatrixCopy);
 		
 		return permutedModel;
 	}
 
 	public Occupancy<?,?> makeAndFillOccupancyMatrix(
 			File occupancyMatrixFile) throws FileNotFoundException,
 			IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(occupancyMatrixFile));
 		Occupancy<Object,Object> occMatrix = new Occupancy<Object,Object>(motifs, metaMotifs);
 		
 		String line = null;
 		int i = 0;
 		while ((line = reader.readLine()) != null) {
 			StringTokenizer tokenizer = new StringTokenizer(line, OCC_COL_SEPARATOR);
 			while (tokenizer.hasMoreTokens()) {
 				String str = tokenizer.nextToken();
 				int index = Integer.parseInt(str);
 				if (index < 0 || index >= motifs.length)
 					throw new IllegalArgumentException(
 							"The input file contains an invalid index: " + index);
 				
 				occMatrix.addModel((Object)metaMotifs[index], i);
 			}
 			i++;
 		}
 		
 		if (i != motifs.length)
 			throw new IllegalArgumentException(
 					"The input file does not contain " +
 					"the correct number of rows (one per input metamotif)");
 		
 		return occMatrix;
 	}
 	
     private static RingBufferMetaMotif[] toRingBufferMetaMotifs(MetaMotif[] metamotifs) {
     	RingBufferMetaMotif[] rbs = new RingBufferMetaMotif[metamotifs.length];
 		for (int i = 0; i < rbs.length; i++) {
 			rbs[i] = new RingBufferMetaMotif(metamotifs[i]);
 		}
 		return rbs;
 	}
 
 	public class Occupancy<DataType,ModelType> {
 		private ModelType[] models;
 		private DataType[] dataEntries;
 		private HashMap<DataType, Set<ModelType>> 
 		modelsByData = new HashMap<DataType, Set<ModelType>>();
 		private HashMap<ModelType, Set<DataType>> 
 		dataByModels = new HashMap<ModelType, Set<DataType>>();
 		
 		public Set<ModelType> getModels(DataType m) {
 			return modelsByData.get(m);
 		}
 
 		public Set<DataType> getDataEntries(ModelType mm) {
 			return dataByModels.get(mm);
 		}
 		
 		public Occupancy(DataType[] motifs, ModelType[] metaMotifs) {
 			this.dataEntries = motifs;
 			this.models = metaMotifs;
 			for (DataType m : motifs)
 				modelsByData.put(m, new HashSet<ModelType>());
 			
 			for (ModelType mm : metaMotifs)
 				dataByModels.put(mm, new HashSet<DataType>());
 		}
 		
 		public ModelType[] getModels() {
 			return models;
 		}
 
 		public DataType[] getDataEntries() {
 			return dataEntries;
 		}
 
 		public void addModel(ModelType mm, int[] indices) {
 			for (int i : indices)
 				addModel(mm, i);
 		}
 
 		private void addModel(ModelType mm, int i) {
 			if (i >= 0 && i < dataEntries.length) {
 				DataType m = dataEntries[i];
 				modelsByData.get(m).add(mm);
 				dataByModels.get(mm).add(m);
 			} else throw new IllegalArgumentException("Invalid index " + i);
 		}
 		
 		public boolean isPresent(ModelType mm, DataType m) {
 			return dataByModels.get(mm).contains(m);
 		}
 		
 		public boolean isPresent(ModelType mm, int i) {
 			return dataByModels.get(mm).contains(dataEntries[i]);
 		}
 		
 		public boolean isPresent(int i, int j) {
 			return dataByModels.get(models[i]).contains(dataEntries[i]);
 		}
 		
 		public boolean isPresent(int i, DataType m) {
 			return dataByModels.get(models[i]).contains(m);
 		}
 		
 		public int indexOfModel(ModelType mm) {
 			for (int i = 0; i < models.length; i++)
 				if (mm == models[i]) return i;
 			return -1;
 		}
 		
 		public int indexOfDataEntry(DataType mm) {
 			for (int i = 0; i < dataEntries.length; i++)
 				if (mm == dataEntries[i]) return i;
 			return -1;
 		}
 	}
 }
