 package org.molgenis.genotype.plink.readers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.molgenis.genotype.Alleles;
 import org.molgenis.genotype.GenotypeDataException;
 import org.molgenis.genotype.plink.datatypes.Biallele;
 import org.molgenis.genotype.plink.datatypes.BimEntry;
 import org.molgenis.genotype.plink.datatypes.FamEntry;
 import org.molgenis.genotype.plink.datatypes.PedEntry;
 import org.molgenis.genotype.plink.drivers.BedFileDriver;
 import org.molgenis.genotype.plink.drivers.BimFileDriver;
 import org.molgenis.genotype.plink.drivers.FamFileDriver;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.ReadOnlyGeneticVariant;
import org.molgenis.genotype.variant.SampleVariantUniqueIdProvider;
 import org.molgenis.genotype.variant.SampleVariantsProvider;
 
 /**
  * Plink binary reader/converter. See:
  * https://github.com/molgenis/molgenis/blob/standalone_tools
  * /src/plinkbintocsv/PlinkbinToCsv.java
  * 
  * @author joeri
  * 
  */
 public class BedBimFamReader implements SampleVariantsProvider
 {
 
 	private BedFileDriver bedfd;
 	private BimFileDriver bimfd;
 	private FamFileDriver famfd;
 
 	private long nrOfIndividuals;
 	private long nrOfSnps;
 	private long nrOfGenotypes;
 	private int paddingPerSnp;
 	private List<String> individualNames;
 	private List<String> snpNames;
 	private List<BimEntry> bimEntries;
 	private List<FamEntry> famEntries;
 	private List<String> sequences; //usually the chromosome numbers, unique within this list
 	private HashMap<String, Biallele> snpCoding;
 	
 	//helper variables
 	private Map<String, List<GeneticVariant>> snpBySequence = new TreeMap<String, List<GeneticVariant>>();
 	private Map<String, Integer> snpIndexById = new HashMap<String, Integer>(1000000);
	private final int sampleVariantProviderUniqueId;
 	//private Map<Integer, List<Biallele>> sampleAllelesBySnpIndex = new HashMap<Integer, List<Biallele>>();
 
 	public BedBimFamReader(File bed, File bim, File fam) throws Exception
 	{
 		bedfd = new BedFileDriver(bed);
 		bimfd = new BimFileDriver(bim);
 		famfd = new FamFileDriver(fam);
 
 		nrOfIndividuals = famfd.getNrOfElements();
 		nrOfSnps = bimfd.getNrOfElements();
 		nrOfGenotypes = nrOfIndividuals * nrOfSnps;
 		paddingPerSnp = (int) ((bedfd.getNrOfElements() - nrOfGenotypes) / nrOfSnps);
		
		sampleVariantProviderUniqueId = SampleVariantUniqueIdProvider.getNextUniqueId();
 	}
 
 	public void setIndividuals() throws Exception
 	{
 		List<String> individualNames = new ArrayList<String>();
 		List<FamEntry> famEntries = famfd.getAllEntries();
 		if (famEntries.size() != nrOfIndividuals)
 		{
 			throw new Exception("Problem with FAM file: scanned number of elements (" + nrOfIndividuals
 					+ ") does not match number of parsed elements (" + famEntries.size() + ")");
 		}
 		for (FamEntry fe : famEntries)
 		{
 			if (individualNames.contains(fe.getIndividual()))
 			{
 				throw new Exception("Problem with FAM file: Individual '" + fe.getIndividual() + "' is not unique!");
 			}
 			individualNames.add(fe.getIndividual());
 		}
 		this.individualNames = individualNames;
 		this.famEntries = famEntries;
 	}
 
 	public void setSnps() throws Exception
 	{
 		nrOfSnps = bimfd.getNrOfElements();
 		List<String> snpNames = new ArrayList<String>();
 		List<String> uniqueChromosomes = new ArrayList<String>();
 		List<BimEntry> bimEntries = bimfd.getAllEntries();
 		if (bimEntries.size() != nrOfSnps)
 		{
 			throw new Exception(
 					"Problem with BIM file: scanned number of elements does not match number of parsed elements");
 		}
 		this.bimEntries = bimEntries;
 		snpCoding = new HashMap<String, Biallele>();
 		int index = 0;
 		for (BimEntry be : bimEntries)
 		{
 			if (snpCoding.containsKey(be.getSNP()))
 			{
 				throw new Exception("Problem with BIM file: SNP '" + be.getSNP() + "' is not unique!");
 			}
 			snpCoding.put(be.getSNP(), be.getBiallele());
 			snpNames.add(be.getSNP());
 			if(!uniqueChromosomes.contains(be.getChromosome()))
 			{
 				uniqueChromosomes.add(be.getChromosome());
 			}
 			snpIndexById.put(be.getSNP(), index);
 			index++;
 		}
 		this.snpNames = snpNames;
 		this.sequences = uniqueChromosomes;
 	}
 	
 	public List<GeneticVariant> loadVariantsForSequence(String seq)
 	{
 		List<GeneticVariant> variants = new ArrayList<GeneticVariant>();
 		
 		int index = 0;
 		for(BimEntry entry : this.bimEntries)
 		{
 			String sequenceName = entry.getChromosome();
 			
 			if(!sequenceName.equals(seq))
 			{
 				continue;
 			}
 			
 			String id = entry.getSNP();
 			int startPos = (int) entry.getBpPos();
 			List<Biallele> alleles = getAllelesByIndex(index);
 			
 			
 			List<String> alleleList = new ArrayList<String>();
 			
 			for(Biallele b : alleles)
 			{
 				
 				String s1 = b.getAllele1() + "";
 				String s2 = b.getAllele2() + "";
 				alleleList.add(s1);
 				alleleList.add(s2);
 			}
 			
 			GeneticVariant snp = ReadOnlyGeneticVariant.createVariant(id, startPos, sequenceName, this, alleleList);
 			
 			variants.add(snp);
 			index++;
 		}
 		
 		return variants;
 		
 		/**
 		for(BimEntry entry : this.bimEntries)
 		{
 			String sequenceName = entry.getChromosome();
 			
 			if(!sequenceName.equals(seq))
 			{
 				continue;
 			}
 			
 			String id = entry.getSNP();
 			int startPos = (int) entry.getBpPos();
 			Map<String, ?> annotationValues = Collections.emptyMap();
 			List<String> altDescriptions = Collections.emptyList();
 			List<String> altTypes = Collections.emptyList();
 			
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
 			
 		}
 		
 		
 	
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
 		
 		
 		
 		GeneticVariant snp = ReadOnlyGeneticVariant.createVariant(id, startPos, sequenceName, this, alleles);
 		*/
 
 		
 	}
 
 	public void extractGenotypes(File writeTo) throws Exception
 	{
 		setIndividuals();
 		setSnps();
 
 		Writer genotypesOut = new OutputStreamWriter(new FileOutputStream(writeTo), "UTF-8");
 
 		// /header: all individual names
 		for (String indvName : individualNames)
 		{
 			genotypesOut.write("\t" + indvName);
 		}
 		genotypesOut.write("\n");
 
 		// elements: snp name + genotypes
 		int snpCounter = 0;
 		for (int genotypeStart = 0; genotypeStart < nrOfGenotypes; genotypeStart += nrOfIndividuals)
 		{
 			System.out.println((int) ((genotypeStart / (double) nrOfGenotypes) * 100) + "% of genotypes done");
 
 			String snpName = snpNames.get(snpCounter);
 			String[] allIndividualsForThisSNP = bedfd.getElements(genotypeStart, genotypeStart + nrOfIndividuals,
 					paddingPerSnp, snpCounter);
 			String a1 = Character.toString(snpCoding.get(snpName).getAllele1());
 			String a2 = Character.toString(snpCoding.get(snpName).getAllele2());
 			String hom1 = a1 + a1;
 			String hom2 = a2 + a2;
 			String hetr = a1 + a2;
 
 			StringBuilder lineOfGenotypesBuilder = new StringBuilder(snpName);
 			for (String s : allIndividualsForThisSNP)
 			{
 				lineOfGenotypesBuilder.append('\t').append(bedfd.convertGenoCoding(s, hom1, hom2, hetr, ""));
 			}
 			lineOfGenotypesBuilder.append('\n');
 			genotypesOut.write(lineOfGenotypesBuilder.toString());
 
 			snpCounter++;
 		}
 
 		genotypesOut.close();
 
 	}
 	
 	
 
 	public List<FamEntry> getFamEntries()
 	{
 		return famEntries;
 	}
 
 
 	public List<String> getSequences()
 	{
 		return sequences;
 	}
 
 	public static void main(String[] args) throws Exception
 	{
 		File bed = new File(Biallele.class.getResource("../testfiles/test.bed").getFile());
 
 		File bim = new File(Biallele.class.getResource("../testfiles/test.bim").getFile());
 
 		File fam = new File(Biallele.class.getResource("../testfiles/test.fam").getFile());
 
 		BedBimFamReader bbfr = new BedBimFamReader(bed, bim, fam);
 
 		File out = new File("geno_tmp.txt");
 
 		System.out.println("going to write to: " + out.getAbsolutePath());
 
 		bbfr.extractGenotypes(out);
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
 
 		List<Biallele> bialleles = getAllelesByIndex(index);
 		List<Alleles> sampleVariants = new ArrayList<Alleles>(bialleles.size());
 		for (Biallele biallele : bialleles)
 		{
 			sampleVariants.add(Alleles.createBasedOnChars(biallele.getAllele1(), biallele.getAllele2()));
 		}
 
 		return sampleVariants;
 	}
 	
 	/**
 	 * Get the alleles for this SNP index
 	 * meaning: the position of this SNP in the BIM file
 	 * then get all of the individuals (samples) for this SNP
 	 * @param index
 	 * @return
 	 * @throws Exception 
 	 */
 	public List<Biallele> getAllelesByIndex(Integer index) throws GenotypeDataException
 	{
 		List<Biallele> bialleles = new ArrayList<Biallele>();
 		
 		try
 		{
 			
 		//	int padding 
 		//	bedfd.getElement(index);
 			
 			//for SNP-major mode
 			long start = index * nrOfIndividuals;
 			long stop = start + nrOfIndividuals;
 			
 			String[] allIndividualsForThisSNP = bedfd.getElements(start, stop,
 					paddingPerSnp);
 			
 			
 			String a1 = Character.toString(snpCoding.get(snpNames.get(index)).getAllele1());
 			String a2 = Character.toString(snpCoding.get(snpNames.get(index)).getAllele2());
 	//		String hom1 = a1 + a1;
 	//		String hom2 = a2 + a2;
 	//		String hetr = a1 + a2;
 	//		
 			for(int i = 0; i < this.nrOfIndividuals; i++)
 			{
 				Biallele b;
 				if(allIndividualsForThisSNP[i].equals("00"))
 				{
 					b = new Biallele(a1, a1);
 				}
 				else if(allIndividualsForThisSNP[i].equals("01"))
 				{
 					b = new Biallele(a1, a2);
 				}
 				else if(allIndividualsForThisSNP[i].equals("11"))
 				{
 					b = new Biallele(a2, a2);
 				}
 				else
 				{
 					b = null;
 				}
 				bialleles.add(b);
 			}
 			
 		}
 		catch(Exception e)
 		{
 			throw new GenotypeDataException(e);
 		}
 		
 		return bialleles;
 	}
 
 	@Override
 	public int cacheSize()
 	{
 		// TODO Auto-generated method stub
 		return 0;
 	}

	@Override
	public int getSampleVariantProviderUniqueId()
	{
		return sampleVariantProviderUniqueId;
	}
 }
