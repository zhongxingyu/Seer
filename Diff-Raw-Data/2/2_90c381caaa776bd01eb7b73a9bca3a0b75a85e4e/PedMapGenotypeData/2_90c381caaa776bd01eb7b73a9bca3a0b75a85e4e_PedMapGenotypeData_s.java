 package org.molgenis.genotype.plink;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.molgenis.genotype.AbstractRandomAccessGenotypeData;
 import org.molgenis.genotype.Alleles;
 import org.molgenis.genotype.Sample;
 import org.molgenis.genotype.Sequence;
 import org.molgenis.genotype.SimpleSequence;
 import org.molgenis.genotype.annotation.Annotation;
 import org.molgenis.genotype.plink.datatypes.Biallele;
 import org.molgenis.genotype.plink.datatypes.MapEntry;
 import org.molgenis.genotype.plink.datatypes.PedEntry;
 import org.molgenis.genotype.plink.drivers.PedFileDriver;
 import org.molgenis.genotype.plink.readers.MapFileReader;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.ReadOnlyGeneticVariant;
 import org.molgenis.genotype.variant.SampleVariantsProvider;
 
 public class PedMapGenotypeData extends AbstractRandomAccessGenotypeData implements SampleVariantsProvider
 {
 	public static final String FATHER_SAMPLE_ANNOTATION_NAME = "father";
 	public static final String MOTHER_SAMPLE_ANNOTATION_NAME = "mother";
 	public static final String SEX_SAMPLE_ANNOTATION_NAME = "sex";
 	public static final String PHENOTYPE_SAMPLE_ANNOTATION_NAME = "phenotype";
 	private static final char NULL_VALUE = '0';
 	private static final Logger LOG = Logger.getLogger(PedMapGenotypeData.class);
 
 	private final File pedFile;
 	private Map<Integer, List<Biallele>> sampleAllelesBySnpIndex = new HashMap<Integer, List<Biallele>>();
 
 	private List<GeneticVariant> snps = new ArrayList<GeneticVariant>(1000000);
 	private Map<String, Integer> snpIndexById = new HashMap<String, Integer>(1000000);
 	private Map<String, List<GeneticVariant>> snpBySequence = new TreeMap<String, List<GeneticVariant>>();
 
 	public PedMapGenotypeData(File pedFile, File mapFile) throws FileNotFoundException, IOException
 	{
 		if (pedFile == null) throw new IllegalArgumentException("PedFile is null");
 		if (mapFile == null) throw new IllegalArgumentException("MapFile is null");
 		if (!mapFile.isFile()) throw new FileNotFoundException("MAP index file not found at "
 				+ mapFile.getAbsolutePath());
 		if (!mapFile.canRead()) throw new IOException("MAP index file not found at " + mapFile.getAbsolutePath());
 		if (!pedFile.isFile()) throw new FileNotFoundException("PED file not found at " + pedFile.getAbsolutePath());
 		if (!pedFile.canRead()) throw new IOException("PED file not found at " + pedFile.getAbsolutePath());
 
 		this.pedFile = pedFile;
 
 		MapFileReader mapFileReader = null;
 		PedFileDriver pedFileDriver = null;
 		try
 		{
 			pedFileDriver = new PedFileDriver(pedFile);
 			loadSampleBialleles(pedFileDriver);
 
 			mapFileReader = new MapFileReader(new FileInputStream(mapFile));
 			loadSnps(mapFileReader);
 
 		}
 		finally
 		{
 			IOUtils.closeQuietly(pedFileDriver);
 			IOUtils.closeQuietly(mapFileReader);
 		}
 
 	}
 
 	private void loadSampleBialleles(PedFileDriver pedFileDriver)
 	{
 		int count = 0;
 		for (PedEntry entry : pedFileDriver)
 		{
 			int index = 0;
 			for (Biallele biallele : entry)
 			{
 				List<Biallele> biallelesForSnp = sampleAllelesBySnpIndex.get(index);
 				if (biallelesForSnp == null)
 				{
 					biallelesForSnp = new ArrayList<Biallele>();
 					sampleAllelesBySnpIndex.put(index, biallelesForSnp);
 				}
 
 				biallelesForSnp.add(biallele);
 				index++;
 			}
 
 			LOG.info("Loaded [" + (++count) + "] samples");
 		}
 
 		LOG.info("Total [" + count + "] samples");
 	}
 
 	private void loadSnps(MapFileReader reader)
 	{
 		int index = 0;
 		for (MapEntry entry : reader)
 		{
 			String id = entry.getSNP();
 			String sequenceName = entry.getChromosome();
 			int startPos = (int) entry.getBpPos();
 
 			List<Biallele> sampleAlleles = sampleAllelesBySnpIndex.get(index);
 			List<String> alleles = new ArrayList<String>(2);
 			for (Biallele biallele : sampleAlleles)
 			{
 				String allele1 = biallele.getAllele1() == NULL_VALUE ? null : biallele.getAllele1() + "";
 				if ((allele1 != null) && !alleles.contains(allele1))
 				{
 					alleles.add(allele1);
 				}
 
 				String allele2 = biallele.getAllele2() == NULL_VALUE ? null : biallele.getAllele2() + "";
 				if ((allele2 != null) && !alleles.contains(allele2))
 				{
 					alleles.add(allele2);
 				}
 			}
 
 			GeneticVariant snp = ReadOnlyGeneticVariant.createVariant(id, startPos, sequenceName, this, alleles);
 
 			snps.add(snp);
 			snpIndexById.put(snp.getPrimaryVariantId(), index);
 
 			List<GeneticVariant> seqGeneticVariants = snpBySequence.get(sequenceName);
 			if (seqGeneticVariants == null)
 			{
 				seqGeneticVariants = new ArrayList<GeneticVariant>();
 				snpBySequence.put(sequenceName, seqGeneticVariants);
 			}
 			seqGeneticVariants.add(snp);
 
 			index++;
 
 			if ((index % 1000) == 0)
 			{
 				LOG.info("Loaded [" + index + "] snps");
 			}
 		}
 
 		LOG.info("Total [" + index + "] snps");
 	}
 
 	@Override
 	public List<Sequence> getSequences()
 	{
 		List<String> seqNames = getSeqNames();
 
 		List<Sequence> sequences = new ArrayList<Sequence>(seqNames.size());
 		for (String seqName : seqNames)
 		{
 			sequences.add(new SimpleSequence(seqName, null, this));
 		}
 
 		return sequences;
 	}
 
 	@Override
 	public List<Sample> getSamples()
 	{
 		PedFileDriver pedFileDriver = null;
 
 		try
 		{
 			pedFileDriver = new PedFileDriver(pedFile);
 			List<Sample> samples = new ArrayList<Sample>();
 			for (PedEntry pedEntry : pedFileDriver)
 			{
 				Map<String, Object> annotations = new HashMap<String, Object>(4);
 				annotations.put(FATHER_SAMPLE_ANNOTATION_NAME, pedEntry.getFather());
 				annotations.put(MOTHER_SAMPLE_ANNOTATION_NAME, pedEntry.getMother());
 				annotations.put(SEX_SAMPLE_ANNOTATION_NAME, pedEntry.getSex());
 				annotations.put(PHENOTYPE_SAMPLE_ANNOTATION_NAME, pedEntry.getPhenotype());
 
 				samples.add(new Sample(pedEntry.getIndividual(), pedEntry.getFamily(), annotations));
 			}
 
 			return samples;
 		}
 		finally
 		{
 			IOUtils.closeQuietly(pedFileDriver);
 		}
 	}
 
 	@Override
 	public List<Alleles> getSampleVariants(GeneticVariant variant)
 
 	{
 		if (variant.getPrimaryVariantId() == null)
 		{
 			throw new IllegalArgumentException("Not a snp, missing primaryVariantId");
 		}
 
 		Integer index = snpIndexById.get(variant.getPrimaryVariantId());
 
 		if (index == null)
 		{
 			throw new IllegalArgumentException("Unknown primaryVariantId [" + variant.getPrimaryVariantId() + "]");
 		}
 
 		List<Biallele> bialleles = sampleAllelesBySnpIndex.get(index);
 		List<Alleles> sampleVariants = new ArrayList<Alleles>(bialleles.size());
 		for (Biallele biallele : bialleles)
 		{
 			sampleVariants.add(Alleles.createBasedOnChars(biallele.getAllele1(), biallele.getAllele2()));
 		}
 
 		return sampleVariants;
 	}
 
 	@Override
 	protected Map<String, Annotation> getVariantAnnotationsMap()
 	{
 		return Collections.emptyMap();
 	}
 
 	@Override
 	public List<String> getSeqNames()
 	{
 		return new ArrayList<String>(snpBySequence.keySet());
 	}
 
 	@Override
 	public List<GeneticVariant> getVariantsByPos(String seqName, int startPos)
 	{
 		List<GeneticVariant> variants = new ArrayList<GeneticVariant>();
 		for (GeneticVariant snp : snps)
 		{
 			if ((snp.getSequenceName() != null) && snp.getSequenceName().equals(seqName)
 					&& (snp.getStartPos() == startPos))
 			{
 				variants.add(snp);
 			}
 		}
 
 		return variants;
 	}
 
 	@Override
 	public Iterator<GeneticVariant> iterator()
 	{
 		return snps.iterator();
 	}
 
 	@Override
 	public Iterable<GeneticVariant> getSequenceGeneticVariants(String seqName)
 	{
 		List<GeneticVariant> variants = snpBySequence.get(seqName);
		if (seqName == null)
 		{
 			throw new IllegalArgumentException("Unknown sequence [" + seqName + "]");
 		}
 
 		return variants;
 	}
 
 	@Override
 	public int cacheSize()
 	{
 		return 0;
 	}
 }
