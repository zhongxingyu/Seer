 package org.molgenis.genotype.variant;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.molgenis.genotype.Allele;
 import org.molgenis.genotype.Alleles;
 import org.molgenis.genotype.util.MafResult;
 
 public class MafCalculator
 {
 
 	public static MafResult calculateMaf(Alleles alleles, Allele reference, List<Alleles> samplesAlleles)
 	{
 
 		HashMap<Allele, AtomicInteger> alleleCounts = new HashMap<Allele, AtomicInteger>(alleles.getAlleles().size());
 		for (Allele allele : alleles.getAlleles())
 		{
 			alleleCounts.put(allele, new AtomicInteger());
 		}
 
 		for (Alleles sampleAlleles : samplesAlleles)
 		{
 			if (sampleAlleles != null)
 			{
 				for (Allele sampleAllele : sampleAlleles.getAlleles())
 				{
					if (sampleAllele != null)
 					{
 						if (!alleleCounts.containsKey(sampleAllele))
 						{
 							throw new NullPointerException("No counter for allele: " + sampleAllele);
 						}
 						alleleCounts.get(sampleAllele).incrementAndGet();
 					}
 				}
 			}
 		}
 
 		Allele provisionalMinorAllele = reference != null ? reference : alleles.getAlleles().get(0);
 		int provisionalMinorAlleleCount = alleleCounts.get(provisionalMinorAllele).get();
 		int totalAlleleCount = 0;
 
 		for (Allele allele : alleles.getAlleles())
 		{
 
 			int alleleCount = alleleCounts.get(allele).get();
 			totalAlleleCount += alleleCount;
 
 			if (alleleCount < provisionalMinorAlleleCount)
 			{
 				provisionalMinorAlleleCount = alleleCounts.get(allele).get();
 				provisionalMinorAllele = allele;
 			}
 		}
 
 		return new MafResult(provisionalMinorAllele, provisionalMinorAlleleCount / (float) totalAlleleCount);
 
 	}
 }
