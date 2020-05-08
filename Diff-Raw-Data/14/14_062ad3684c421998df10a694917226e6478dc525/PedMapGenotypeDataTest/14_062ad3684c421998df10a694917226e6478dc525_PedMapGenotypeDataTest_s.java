 package org.molgenis.genotype.plink;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
 import static org.testng.Assert.assertTrue;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.molgenis.genotype.ResourceTest;
 import org.molgenis.genotype.Sample;
 import org.molgenis.genotype.Sequence;
 import org.molgenis.genotype.VariantAlleles;
 import org.molgenis.genotype.VariantQueryResult;
 import org.molgenis.genotype.util.Utils;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.SnpGeneticVariant;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class PedMapGenotypeDataTest extends ResourceTest
 {
 	private PedMapGenotypeData genotypeData;
 
 	@BeforeClass
 	public void beforeClass() throws IOException, URISyntaxException
 	{
 		genotypeData = new PedMapGenotypeData(getTestMapGz(), getTestMapGzTbi(), getTestPed(), '	');
 	}
 
 	@Test
 	public void getSeqNames()
 	{
 		List<String> seqNames = genotypeData.getSeqNames();
 		assertNotNull(seqNames);
 		assertEquals(seqNames.size(), 2);
 		assertEquals(seqNames.get(0), "22");
 		assertEquals(seqNames.get(1), "23");
 	}
 
 	@Test
 	public void testGetSequences()
 	{
 		List<Sequence> sequences = genotypeData.getSequences();
 		assertNotNull(sequences);
 		assertEquals(sequences.size(), 2);
 	}
 
 	@Test
 	public void testGetSequenceByName() throws IOException
 	{
 		Sequence sequence = genotypeData.getSequenceByName("22");
 		assertNotNull(sequence);
 		assertEquals(sequence.getName(), "22");
 
 		VariantQueryResult queryResult = sequence.getVariants();
 		List<GeneticVariant> variants;
 		try
 		{
 			variants = Utils.iteratorToList(queryResult.iterator());
 		}
 		finally
 		{
 			queryResult.close();
 		}
 
 		assertNotNull(variants);
 		assertEquals(variants.size(), 9);
 		GeneticVariant variant = variants.get(0);
 		assertEquals(variant.getPrimaryVariantId(), "rs11089130");
 		assertEquals(variant.getStartPos(), 14431347);
 
		assertNull(variant.getRefAllele());
 		assertEquals(variant.getSequenceName(), "22");
 		assertEquals(variant.getType(), GeneticVariant.Type.SNP);
 
 		List<String> alleles = variant.getVariantAlleles().getAlleles();
 		assertNotNull(alleles);
 		assertEquals(alleles.size(), 2);
 		assertTrue(alleles.contains("C"));
 		assertTrue(alleles.contains("G"));
 
 		List<VariantAlleles> sampleVariants = variant.getSampleVariants();
 		assertNotNull(sampleVariants);
 		assertEquals(sampleVariants.size(), 9);
 		assertNotNull(sampleVariants.get(0).getAllelesAsChars());
 		assertEquals(sampleVariants.get(0).getAlleles().size(), 2);
 		assertEquals(sampleVariants.get(0).getAllelesAsChars()[0], 'C');
 		assertEquals(sampleVariants.get(0).getAllelesAsChars()[1], 'C');
 		assertNotNull(sampleVariants.get(1).getAllelesAsChars());
 		assertEquals(sampleVariants.get(1).getAllelesAsChars().length, 2);
 		assertEquals(sampleVariants.get(1).getAllelesAsChars()[0], 'C');
 		assertEquals(sampleVariants.get(1).getAllelesAsChars()[1], 'G');
 		assertNotNull(sampleVariants.get(2).getAllelesAsChars());
 		assertEquals(sampleVariants.get(2).getAllelesAsChars().length, 2);
 		assertEquals(sampleVariants.get(2).getAllelesAsChars()[0], 'G');
 		assertEquals(sampleVariants.get(2).getAllelesAsChars()[1], 'G');
 	}
 
 	@Test
 	public void testGetSamples()
 	{
 		List<Sample> samples = genotypeData.getSamples();
 		assertNotNull(samples);
 		assertEquals(samples.size(), 9);
 		assertEquals(samples.get(0).getId(), "1042");
 		assertEquals(samples.get(0).getFamilyId(), "F1042");
 	}
 
 	@Test
 	public void testSwap()
 	{
 		SnpGeneticVariant variant = (SnpGeneticVariant) genotypeData.getVariantById("rs11089130");
 		assertNotNull(variant);
 		assertTrue((variant instanceof SnpGeneticVariant));
 
 		assertEquals(variant.getMinorAllele(), "G");
 		assertEquals(variant.getMinorAlleleFrequency(), 0.277, 0.001);
 
 		variant.swapAlleles();
 		assertEquals(variant.getVariantAlleles().getAlleles(), Arrays.asList("G", "C"));
		assertNull(variant.getRefAllele());
 		assertEquals(variant.getSampleVariants().size(), 9);
 		assertEquals(variant.getSampleSnpVariants().get(0), new char[]
 		{ 'G', 'G' });
 		assertEquals(variant.getSampleSnpVariants().get(1), new char[]
 		{ 'G', 'C' });
 		assertEquals(variant.getSampleSnpVariants().get(8), new char[]
 		{ 'G', 'C' });
 		assertEquals(variant.getMinorAllele(), "C");
 		assertEquals(variant.getMinorAlleleFrequency(), 0.277, 0.001);
 	}
 }
