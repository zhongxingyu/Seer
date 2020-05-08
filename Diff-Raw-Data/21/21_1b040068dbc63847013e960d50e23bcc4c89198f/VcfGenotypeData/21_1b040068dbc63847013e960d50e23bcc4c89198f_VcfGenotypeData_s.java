 package org.molgenis.genotype.vcf;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import net.sf.samtools.util.BlockCompressedInputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.molgenis.genotype.Alleles;
 import org.molgenis.genotype.GenotypeDataException;
 import org.molgenis.genotype.GenotypeDataIndex;
 import org.molgenis.genotype.IndexedGenotypeData;
 import org.molgenis.genotype.RandomAccessGenotypeData;
 import org.molgenis.genotype.Sample;
 import org.molgenis.genotype.Sequence;
 import org.molgenis.genotype.SimpleSequence;
import org.molgenis.genotype.VariantQueryResult;
 import org.molgenis.genotype.annotation.Annotation;
 import org.molgenis.genotype.annotation.VcfAnnotation;
 import org.molgenis.genotype.tabix.TabixIndex;
 import org.molgenis.genotype.variant.CachedSampleVariantProvider;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.SampleVariantsProvider;
 import org.molgenis.genotype.variant.VariantLineMapper;
 import org.molgenis.io.vcf.VcfAlt;
 import org.molgenis.io.vcf.VcfContig;
 import org.molgenis.io.vcf.VcfInfo;
 import org.molgenis.io.vcf.VcfReader;
 
 public class VcfGenotypeData extends IndexedGenotypeData implements SampleVariantsProvider
 {
 	private final GenotypeDataIndex index;
 	private final VcfReader reader;
 	private Map<String, Annotation> sampleAnnotationsMap;
 	private Map<String, String> altDescriptions;
 
 	/**
 	 * VCF genotype reader with default cache of 100
 	 * 
 	 * @param bzipVcfFile
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	public VcfGenotypeData(File bzipVcfFile) throws FileNotFoundException, IOException
 	{
 		this(bzipVcfFile, 100);
 	}
 
 	public VcfGenotypeData(File bzipVcfFile, int cacheSize) throws FileNotFoundException, IOException
 	{
 		this(bzipVcfFile, new File(bzipVcfFile.getAbsolutePath() + ".tbi"), cacheSize);
 	}
 
 	/**
 	 * VCF genotype reader with default cache of 100
 	 * 
 	 * @param bzipVcfFile
 	 * @param tabixIndexFile
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	public VcfGenotypeData(File bzipVcfFile, File tabixIndexFile) throws FileNotFoundException, IOException
 	{
 		this(bzipVcfFile, tabixIndexFile, 100);
 	}
 
 	public VcfGenotypeData(File bzipVcfFile, File tabixIndexFile, int cacheSize) throws FileNotFoundException,
 			IOException
 	{
 
 		if (!bzipVcfFile.isFile())
 		{
 			throw new FileNotFoundException("VCF file not found at " + bzipVcfFile.getAbsolutePath());
 		}
 
 		if (!bzipVcfFile.canRead())
 		{
 			throw new IOException("VCF file not found at " + bzipVcfFile.getAbsolutePath());
 		}
 
 		if (!tabixIndexFile.isFile())
 		{
 			throw new FileNotFoundException("VCF index file not found at " + tabixIndexFile.getAbsolutePath());
 		}
 
 		if (!tabixIndexFile.canRead())
 		{
 			throw new IOException("VCF index file not found at " + tabixIndexFile.getAbsolutePath());
 		}
 
 		try
 		{
 			reader = new VcfReader(new BlockCompressedInputStream(bzipVcfFile));
 
 			try
 			{
 				SampleVariantsProvider sampleVariantProvider = cacheSize <= 0 ? this : new CachedSampleVariantProvider(
 						this, cacheSize);
 
 				VariantLineMapper variantLineMapper = new VcfVariantLineMapper(reader.getColNames(),
 						getVariantAnnotations(), getAltDescriptions(), sampleVariantProvider);
 				index = new TabixIndex(tabixIndexFile, bzipVcfFile, variantLineMapper);
 			}
 			finally
 			{
 				IOUtils.closeQuietly(reader);
 			}
 
 		}
 		catch (IOException e)
 		{
 			throw new GenotypeDataException(e);
 		}
 	}
 
 	@Override
 	public List<Alleles> getSampleVariants(GeneticVariant variant)
 
 	{
 		try
 		{
 			return index.createQuery().findSamplesForVariant(variant.getSequenceName(), variant.getStartPos(),
 					variant.getVariantAlleles().getAllelesAsString(), reader.getColNames(), reader.getSampleNames());
 		}
 		catch (IOException e)
 		{
 			throw new GenotypeDataException("IOException getSampleVariants for variant with id [" + variant.getAllIds()
 					+ "]", e);
 		}
 	}
 
 	@Override
 	protected Map<String, Annotation> getVariantAnnotationsMap()
 	{
 		if (sampleAnnotationsMap == null)
 		{
 			List<VcfInfo> infos = reader.getInfos();
 			sampleAnnotationsMap = new LinkedHashMap<String, Annotation>(infos.size());
 
 			for (VcfInfo info : infos)
 			{
 				sampleAnnotationsMap.put(info.getId(), VcfAnnotation.fromVcfInfo(info));
 			}
 		}
 
 		return sampleAnnotationsMap;
 	}
 
 	@Override
 	public List<Sequence> getSequences()
 	{
 		List<String> seqNames = getSeqNames();
 		Map<String, Integer> seqLengths = getSequenceLengths();
 
 		List<Sequence> sequences = new ArrayList<Sequence>(seqNames.size());
 		for (String seqName : seqNames)
 		{
 			sequences.add(new SimpleSequence(seqName, seqLengths.get(seqName), this));
 		}
 
 		return sequences;
 	}
 
 	@Override
 	public List<Sample> getSamples()
 	{
 		List<String> sampleNames;
 		try
 		{
 			sampleNames = reader.getSampleNames();
 		}
 		catch (IOException e)
 		{
 			throw new GenotypeDataException("IOException getting samplenames from VcfReader", e);
 		}
 
 		List<Sample> samples = new ArrayList<Sample>(sampleNames.size());
 		for (String sampleName : sampleNames)
 		{
 			Sample sample = new Sample(sampleName, null, Collections.<String, Object> emptyMap());
 			samples.add(sample);
 		}
 
 		return samples;
 	}
 
 	/**
 	 * Get sequence length by sequence name
 	 */
 	private Map<String, Integer> getSequenceLengths()
 	{
 		List<VcfContig> contigs = reader.getContigs();
 		Map<String, Integer> sequenceLengthById = new HashMap<String, Integer>(contigs.size());
 
 		for (VcfContig contig : contigs)
 		{
 			sequenceLengthById.put(contig.getId(), contig.getLength());
 		}
 
 		return sequenceLengthById;
 	}
 
 	private Map<String, String> getAltDescriptions()
 	{
 		if (altDescriptions == null)
 		{
 			List<VcfAlt> alts = reader.getAlts();
 			altDescriptions = new HashMap<String, String>(alts.size());
 
 			for (VcfAlt alt : alts)
 			{
 				altDescriptions.put(alt.getId(), alt.getDescription());
 			}
 		}
 
 		return altDescriptions;
 	}
 
 	@Override
 	protected GenotypeDataIndex getIndex()
 	{
 		return index;
 	}
 
 	@Override
 	public Iterator<GeneticVariant> getSequenceGeneticVariants(String seqName)
 	{
		VariantQueryResult result = index.createQuery().executeQuery(seqName);
		try
		{
			return result.iterator();
		}
		finally
		{
			IOUtils.closeQuietly(result);
		}
 	}
 
 	@Override
 	public Iterator<GeneticVariant> iterator()
 	{
 		return new GeneticVariantsIterator(this);
 	}
 
 	private static class GeneticVariantsIterator implements Iterator<GeneticVariant>
 	{
 		private Iterator<String> seqNames;
 		private Iterator<GeneticVariant> seqGeneticVariants;
 		private RandomAccessGenotypeData randomAccessGenotypeData;
 
 		public GeneticVariantsIterator(RandomAccessGenotypeData randomAccessGenotypeData)
 		{
 			seqNames = randomAccessGenotypeData.getSeqNames().iterator();
 			seqGeneticVariants = randomAccessGenotypeData.getSequenceGeneticVariants(seqNames.next());
 			this.randomAccessGenotypeData = randomAccessGenotypeData;
 		}
 
 		@Override
 		public boolean hasNext()
 		{
 			return seqGeneticVariants.hasNext() || seqNames.hasNext();
 		}
 
 		@Override
 		public GeneticVariant next()
 		{
 			if (seqGeneticVariants.hasNext())
 			{
 				return seqGeneticVariants.next();
 			}
 
 			if (seqNames.hasNext())
 			{
 				seqGeneticVariants = randomAccessGenotypeData.getSequenceGeneticVariants(seqNames.next());
 				return seqGeneticVariants.next();
 			}
 
 			throw new NoSuchElementException();
 		}
 
 		@Override
 		public void remove()
 		{
 			throw new UnsupportedOperationException();
 		}
 
 	}
 }
