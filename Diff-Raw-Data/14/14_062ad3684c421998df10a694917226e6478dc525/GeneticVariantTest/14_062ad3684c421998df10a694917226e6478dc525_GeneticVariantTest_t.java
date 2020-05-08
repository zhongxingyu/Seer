 package org.molgenis.genotype.variant;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertNotNull;
 import static org.testng.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.molgenis.genotype.DummySampleVariantsProvider;
 import org.molgenis.genotype.VariantAlleles;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class GeneticVariantTest
 {
 	private GeneticVariant testInstance;
 
 	@BeforeClass
 	public void setUp()
 	{
 
 		VariantAlleles alleles = VariantAlleles.create('A', 'C');
 
 		ArrayList<VariantAlleles> sampleAlleles = new ArrayList<VariantAlleles>();
 		sampleAlleles.add(VariantAlleles.create('A', 'C'));
 		sampleAlleles.add(VariantAlleles.create('A', 'C'));
 		sampleAlleles.add(VariantAlleles.create('C', 'C'));
 		sampleAlleles.add(VariantAlleles.create('A', 'A'));
 		sampleAlleles.add(VariantAlleles.create('A', 'A'));
 		SampleVariantsProvider sampleAllelesProvider = new DummySampleVariantsProvider(sampleAlleles);
 
 		testInstance = new SnpGeneticVariant("rs1", "chr1", 1, alleles, "A", null, null, null, sampleAllelesProvider);
 
 	}
 
 	@Test
 	public void getAllIds()
 	{
 		ArrayList<String> expected = new ArrayList<String>();
 		expected.add("rs1");
 		assertEquals(testInstance.getAllIds(), expected);
 	}
 
 	@Test
 	public void getAlleleCount()
 	{
 		assertEquals(testInstance.getAlleleCount(), 2);
 	}
 
 	@Test
 	public void getCalledDosages()
 	{
 		byte[] expected =
 		{ 1, 1, 0, 2, 2 };
 		assertEquals(testInstance.getCalledDosages(), expected);
 	}
 
 	@Test
 	public void getMinorAllele()
 	{
 		assertEquals(testInstance.getMinorAllele(), "C");
 	}
 
 	@Test
 	public void getMinorAlleleFrequency()
 	{
 		assertEquals(testInstance.getMinorAlleleFrequency(), 0.4f);
 	}
 
 	@Test
 	public void getRefAllele()
 	{
 		assertEquals(testInstance.getRefAllele(), "A");
 	}
 
 	@Test
 	public void getSequenceName()
 	{
 		assertEquals(testInstance.getSequenceName(), "chr1");
 	}
 
 	@Test
 	public void getStartPos()
 	{
 		assertEquals(testInstance.getStartPos(), 1);
 	}
 
 	@Test
 	public void getStopPos()
 	{
 		assertEquals(testInstance.getStopPos(), null);
 	}
 
 	@Test
 	public void getType()
 	{
 		assertEquals(testInstance.getType(), GeneticVariant.Type.SNP);
 	}
 
 	@Test
 	public void isBiallelic()
 	{
 		assertEquals(testInstance.isBiallelic(), true);
 	}
 
 	@Test
 	public void isSnp()
 	{
 		assertEquals(testInstance.isSnp(), true);
 	}
 
 	@Test
 	public void getPrimaryVatiantId()
 	{
 		GeneticVariant variant = createGeneticVariant(Arrays.asList("X"));
 		assertNotNull(variant.getPrimaryVariantId());
 		assertEquals("X", variant.getPrimaryVariantId());
 
 		variant = createGeneticVariant(Arrays.asList("X", "Y"));
 		assertNotNull(variant.getPrimaryVariantId());
 		assertEquals("X", variant.getPrimaryVariantId());
 
 		variant = createGeneticVariant(Arrays.asList("X", "Y", "Z"));
 		assertNotNull(variant.getPrimaryVariantId());
 		assertEquals("X", variant.getPrimaryVariantId());
 	}
 
 	@Test
 	public void getAlternativeVariantIds()
 	{
 		GeneticVariant variant = createGeneticVariant(Arrays.asList("X"));
 		assertNotNull(variant.getAlternativeVariantIds());
 		assertTrue(variant.getAlternativeVariantIds().isEmpty());
 
 		variant = createGeneticVariant(Arrays.asList("X", "Y"));
 		assertNotNull(variant.getAlternativeVariantIds());
 		assertEquals(variant.getAlternativeVariantIds().size(), 1);
 		assertEquals(variant.getAlternativeVariantIds().get(0), "Y");
 
 		variant = createGeneticVariant(Arrays.asList("X", "Y", "Z"));
 		assertNotNull(variant.getAlternativeVariantIds());
 		assertEquals(variant.getAlternativeVariantIds().size(), 2);
 		assertEquals(variant.getAlternativeVariantIds().get(0), "Y");
 		assertEquals(variant.getAlternativeVariantIds().get(1), "Z");
 	}
 
 	private GeneticVariant createGeneticVariant(List<String> ids)
 	{
		return new GeneticVariant(ids, "sequenceName", 1, VariantAlleles.create('A', 'T'), null, null, null, null,
				null, null, GeneticVariant.Type.SNP);
 	}
 }
