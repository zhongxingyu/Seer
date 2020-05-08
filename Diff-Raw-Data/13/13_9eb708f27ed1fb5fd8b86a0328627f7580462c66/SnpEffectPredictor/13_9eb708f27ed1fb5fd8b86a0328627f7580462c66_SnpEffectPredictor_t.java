 package ca.mcgill.mcb.pcingola.snpEffect;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import ca.mcgill.mcb.pcingola.interval.Chromosome;
 import ca.mcgill.mcb.pcingola.interval.Exon;
 import ca.mcgill.mcb.pcingola.interval.Gene;
 import ca.mcgill.mcb.pcingola.interval.Genome;
 import ca.mcgill.mcb.pcingola.interval.Intergenic;
 import ca.mcgill.mcb.pcingola.interval.Intron;
 import ca.mcgill.mcb.pcingola.interval.Marker;
 import ca.mcgill.mcb.pcingola.interval.Markers;
 import ca.mcgill.mcb.pcingola.interval.SeqChange;
 import ca.mcgill.mcb.pcingola.interval.Transcript;
 import ca.mcgill.mcb.pcingola.interval.Utr;
 import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
 import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
 import ca.mcgill.mcb.pcingola.util.Gpr;
 
 /**
  * Predicts effects of SNPs 
  * 
  * Note: Actually tries to predict any kind of SeqChange, not only SNPs . It is called SnpEffectPredictor for 'historical reasons'.
  * 
  * @author pcingola
  *
  */
 public class SnpEffectPredictor implements Serializable {
  private static final long serialVersionUID = 4519418862303325081L;
 
  public static final int DEFAULT_UP_DOWN_LENGTH = 5000;
 
 	boolean useChromosomes = true;
 	int upDownStreamLength = DEFAULT_UP_DOWN_LENGTH;
 	Genome genome;
 	ArrayList<Marker> markers; // All other markers are stored here (e.g. custom markers, intergenic, etc.)
 	IntervalForest intervalForest;
 
 	/**
 	 * Load predictor from a binary file
 	 */
 	public static SnpEffectPredictor load(Config config) {
 		String snpEffPredFile = config.dataDir + "/" + config.genome.getVersion() + "/snpEffectPredictor.bin";
 
 		// Is the file there
 		SnpEffectPredictor snpEffectPredictor = null;
 		try {
 			snpEffectPredictor = (SnpEffectPredictor) Gpr.readFileSerializedGzThrow(snpEffPredFile);
 		} catch (FileNotFoundException e) {
 			System.err.println("\nERROR: Cannot read file '" + snpEffPredFile + "'.\n\tYou can try to download the database by running the following command:\n\t\tjava -jar snpEff.jar download " + config.genome.getVersion() + "\n");
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			if (e instanceof InvalidClassException) System.err.println("\nERROR: Incompatible database version. SnpEff and database version do not match.\n\tYou can try to download the apropriate database by running the following command:\n\t\tjava -jar snpEff.jar download " + config.genome.getVersion() + "\n");
 			else System.err.println("Cannot read file '" + snpEffPredFile + "'.\n");
 			throw new RuntimeException(e);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return snpEffectPredictor;
 	}
 
 	public SnpEffectPredictor(Genome genome) {
 		this.genome = genome;
 		markers = new ArrayList<Marker>();
 	}
 
 	/**
 	 * Add a gene interval
 	 * @param gene
 	 */
 	public void add(Gene gene) {
 		genome.getGenes().add(gene);
 	}
 
 	/** 
 	 * Add a marker
 	 * 
 	 * Note: Markers have to be added BEFORE building the interval trees. 
 	 *       Interval trees are built the first time you call snpEffect(snp) method.
 	 * 
 	 * @param marker
 	 */
 	public void add(Marker marker) {
 		markers.add(marker);
 	}
 
 	/**
 	 * Create interval trees (forest)
 	 */
 	public void buildForest() {
 		intervalForest = new IntervalForest();
 
 		// Add all chromosomes to forest
 		if (useChromosomes) {
 			for (Chromosome chr : genome)
 				intervalForest.add(chr);
 		}
 
 		// Add all genes to forest
 		for (Gene gene : genome.getGenes())
 			intervalForest.add(gene);
 
 		//---
 		// Add to markers to 'markers'
 		//---
 		// Add up-down stream intervals
 		for (Marker m : genome.getGenes().createUpDownStream(upDownStreamLength))
 			add(m);
 
 		// Add splice site intervals
 		for (Marker m : genome.getGenes().createSpliceSites())
 			add(m);
 
 		intervalForest.add(markers); // Add all 'markers' to forest (includes custom intervals)
 
 		// Build interval forest
 		intervalForest.build();
 	}
 
 	/**
 	 * Obtain a gene interval
 	 * @param geneIntervalId
 	 * @return
 	 */
 	public Gene getGene(String geneIntervalId) {
 		return genome.getGenes().get(geneIntervalId);
 	}
 
 	public Genome getGenome() {
 		return genome;
 	}
 
 	public IntervalForest getIntervalForest() {
 		return intervalForest;
 	}
 
 	public ArrayList<Marker> getMarkers() {
 		return markers;
 	}
 
 	public int getUpDownStreamLength() {
 		return upDownStreamLength;
 	}
 
 	/**
 	 * Return a collection of intervals thet intercept marker
 	 */
 	public Markers intersects(Marker marker) {
 		return intervalForest.query(marker);
 	}
 
 	/**
 	 * Dump to sdtout
 	 */
 	public void print() {
 		genome.showStats();
 
 		// Show genome
 		System.out.println("Genome: " + genome.getVersion());
 
 		// Show chromosomes
 		for (Chromosome chr : genome)
 			System.out.println("Chromosome: \t" + chr.getId() + "\t" + chr.getStart() + "\t" + chr.getEnd());
 
 		// Show genes
 		for (Gene gene : genome.getGenes())
 			System.out.println(gene);
 
 		// Show other inervals
 		for (Marker marker : markers)
 			System.out.println(marker);
 	}
 
 	/**
 	 * Name of the regions hit by a marker
 	 * @param marker
 	 * @return A set of region names
 	 */
 	public Set<String> regions(Marker marker, boolean showGeneDetails, boolean compareTemplate) {
 		boolean hitChromo = false;
 		boolean hitGene = false;
 		HashSet<String> hits = new HashSet<String>();
 
 		Markers intersects = intersects(marker);
 		if (intersects.size() > 0) {
 			for (Marker markerInt : intersects) {
 
 				if (markerInt instanceof Chromosome) {
 					hitChromo = true; // OK (we have to hit a chromosome, otherwise it's an error
 					hits.add(markerInt.getClass().getSimpleName()); // Add marker name to the list
 				} else if (markerInt instanceof Gene) {
 					// Analyze Genes
 					Gene gene = (Gene) markerInt;
 					hitGene = true;
 					regionsAddHit(hits, gene, marker, showGeneDetails, compareTemplate);
 
 					// For all transcripts...
 					for (Transcript tr : gene) {
 						if (tr.intersects(marker)) { // Does it intersect this transcript?
 							boolean hitExon = false;
 
 							regionsAddHit(hits, tr, marker, showGeneDetails, compareTemplate);
 
 							for (Utr utr : tr.getUtrs())
 								if (utr.intersects(marker)) regionsAddHit(hits, utr, marker, showGeneDetails, compareTemplate);
 
 							for (Exon ex : tr)
 								if (ex.intersects(marker)) { // Does it intersect this UTR? Add 'Exon'
 									regionsAddHit(hits, ex, marker, showGeneDetails, compareTemplate);
 									hitExon = true;
 								}
 
 							// Not in an exon? => Add 'Intron'
 							if (!hitExon) {
 								Intron intron = new Intron(tr, marker.getStart(), marker.getEnd(), tr.getStrand(), "");
 								regionsAddHit(hits, intron, marker, showGeneDetails, compareTemplate);
 							}
 						}
 					}
 				} else {
 					regionsAddHit(hits, markerInt, marker, showGeneDetails, compareTemplate);
 				}
 			}
 		}
 
 		if (!hitChromo) throw new RuntimeException("ERROR: Out of chromosome range. " + marker);
 		if (!hitGene) hits.add(Intergenic.class.getSimpleName());
 		return hits;
 	}
 
 	/**
 	 * Add into to a hash
 	 * @param hits
 	 * @param marker
 	 * @param hit2add
 	 * @param showGeneDetails
 	 * @param compareTemplate
 	 */
 	void regionsAddHit(HashSet<String> hits, Marker hit2add, Marker marker, boolean showGeneDetails, boolean compareTemplate) {
 		String hitStr = hit2add.getClass().getSimpleName();
 
 		if (compareTemplate) {
 			Gene gene = (Gene) hit2add.findParent(Gene.class);
 			if (gene != null) hitStr += (hit2add.isStrandPlus() == marker.isStrandPlus()) ? "_TEMPLATE_STRAND" : "_NON_TEMPLATE_STRAND";
 		}
 
 		if (showGeneDetails && (hit2add instanceof Gene)) {
 			Gene gene = (Gene) hit2add;
 			hitStr += "[" + gene.getBioType() + ", " + gene.getGeneName() + ", " + (gene.isProteinCoding() ? "protein" : "not-protein") + "]";
 		}
 
 		hits.add(hitStr); // Add marker name to the list
 	}
 
 	/**
 	 * Remove all non-canonical transcripts
 	 */
 	public void removeNonCanonical() {
 		for (Gene g : genome.getGenes())
 			g.removeNonCanonical();
 	}
 
 	/**
 	 * Save predictor to a binary file (specified by the configuration)
 	 */
 	public void save(Config config) {
 		String cacheFile = config.dataDir + "/" + config.genome.getVersion() + "/snpEffectPredictor.bin";
 		Gpr.toFileSerializeGz(cacheFile, this);
 	}
 
 	/**
 	 * Predict the effect of a seqChange
 	 * @param seqChange
 	 */
 	public List<ChangeEffect> seqChangeEffect(SeqChange seqChange) {
 		// No change? => Nothing to predict
 		if ((!seqChange.isChange()) && (!seqChange.isInterval())) return ChangeEffect.emptyResults();
 
 		ChangeEffect results = new ChangeEffect(seqChange);
 
 		// Chromosome not found?
 		if (Config.get().isErrorOnMissingChromo() && !intervalForest.hasTree(seqChange.getChromosomeName())) {
 			results.addError("ERROR_CHROMOSOME_NOT_FOUND");
 			return results.newList();
 		}
 
 		// Which intervals does seqChange intersect?
 		Markers intersects = intersects(seqChange);
 
 		// Show all results
 		boolean hitChromo = false, hitSomething = false;
 		ArrayList<ChangeEffect> resultsList = new ArrayList<ChangeEffect>();
 		if (intersects.size() > 0) {
 			for (Marker marker : intersects) {
 				if (marker instanceof Chromosome) hitChromo = true; // Do we hit any chromosome?
 				else { // Analyze all markers 
 					Marker mint = marker;
 					results = new ChangeEffect(seqChange);
 
 					List<ChangeEffect> resList = mint.seqChangeEffect(seqChange, results);
 
 					if (!resList.isEmpty()) resultsList.addAll(resList);
 					hitSomething = true;
 				}
 			}
 		}
 
 		// Any errors or intergenic (i.e. did not hit any gene)
 		if (!hitChromo) {
 			if (Config.get().isErrorChromoHit()) {
 				results.addError("ERROR_OUT_OF_CHROMOSOME_RANGE");
 				return results.newList();
 			}
 		} else if (!hitSomething) {
 			if (Config.get().isOnlyRegulation()) {
 				results.effectType = EffectType.NONE;
 				return results.newList();
 			} else {
 				results.effectType = EffectType.INTERGENIC;
 				return results.newList();
 			}
 		}
 
 		return resultsList;
 	}
 
 	public void setUpDownStreamLength(int upDownStreamLength) {
 		this.upDownStreamLength = upDownStreamLength;
 	}
 
 	public void setUseChromosomes(boolean useChromosomes) {
 		this.useChromosomes = useChromosomes;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(genome.getVersion() + "\n");
 		for (Chromosome chr : genome)
 			sb.append(chr + "\n");
 		sb.append(genome.getGenes());
 		return sb.toString();
 	}
 
 }
