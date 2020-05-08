 package org.molgenis.genotype;
 
 import java.io.File;
 import java.util.List;
 
 import org.molgenis.genotype.plink.PedMapGenotypeData;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.SnpGeneticVariant;
 
 public class Main
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		try
 		{
 			File pedFile = new File("/Users/erwin/Documents/data/plink/hapmap.ped");
 			File mapFile = new File("/Users/erwin/Documents/data/plink/hapmap.map.gz");
 			File indexFile = new File("/Users/erwin/Documents/data/plink/hapmap.map.gz.tbi");
 
 			GenotypeData data = new PedMapGenotypeData(mapFile, indexFile, pedFile, ' ');
 			System.out.println("Sequences:" + data.getSequences());
 			List<GeneticVariant> variants = data.getVariants();
 			System.out.println("Variant count = " + variants.size());
 			System.out.println(variants.get(0).getPrimaryVariantId());
 			System.out.println(variants.get(0).getVariantAlleles());
 			System.out.println(variants.get(0).getSampleVariants().size());
 
 			GeneticVariant var = data.getVariantById("rs9629043");
 			System.out.println(var.getPrimaryVariantId());
 			System.out.println(var.getStartPos());
 			System.out.println(var.getSampleVariants());
 			System.out.println(var.getSequenceName());
 
 			long t0 = System.currentTimeMillis();
 			System.out.println(var.getMinorAllele() + ":" + var.getMinorAlleleFrequency());
 			long t = System.currentTimeMillis() - t0;
 			System.out.println("t = " + t);
 
			SnpGeneticVariant snpvariant = data.getSnpVariantsByPos("1", 554636);
 			System.out.println(snpvariant.getStartPos());
 			System.out.println(snpvariant.getSampleVariants());
 
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 		}
 
 		// File vcfFile = new
 		// File("/Users/erwin/Documents/data/vcf/chr17.vcf.gz");
 		// File indexFile = new
 		// File("/Users/erwin/Documents/data/vcf/chr17.vcf.gz.tbi");
 		//
 		// try
 		// {
 		// GenotypeData data = new VcfGenotypeData(vcfFile, indexFile);
 		// System.out.println("Sequences:" + data.getSequences());
 		// System.out.println("Samples:" + data.getSamples().size());
 		// List<GeneticVariant> variants = data.getVariants();
 		// System.out.println("Variant count = " + variants.size());
 		// System.out.println(variants.get(0).getPrimaryVariantId());
 		// System.out.println(variants.get(0).getVariantAlleles().getAlleles());
 		// System.out.println(variants.get(0).getSampleVariants().size());
 		//
 		// GeneticVariant var = data.getVariantById("rs113037146");
 		// System.out.println("rs113037146:" + var.getPrimaryVariantId());
 		// System.out.println("rs113037146:" + var.getStartPos());
 		//
 		// var = data.getVariantById(variants.get(0).getPrimaryVariantId());
 		// System.out.println(var.getPrimaryVariantId());
 		// System.out.println(var.getVariantAlleles().getAlleles());
 		// System.out.println(var.getSampleVariants().size());
 		// }
 		// catch (Exception e)
 		// {
 		// e.printStackTrace();
 		// }
 
 	}
 }
