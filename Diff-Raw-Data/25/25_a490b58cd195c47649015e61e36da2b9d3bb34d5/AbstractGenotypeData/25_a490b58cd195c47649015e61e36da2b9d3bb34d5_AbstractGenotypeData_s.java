 package org.molgenis.genotype;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.molgenis.genotype.annotation.Annotation;
 import org.molgenis.genotype.variant.GeneticVariant;
 import org.molgenis.genotype.variant.SnpGeneticVariant;
 
 public abstract class AbstractGenotypeData implements GenotypeData
 {
 	@Override
 	public abstract List<String> getSeqNames();
 
 	@Override
 	public List<Annotation> getVariantAnnotations()
 	{
 		return Collections.unmodifiableList(new ArrayList<Annotation>(getVariantAnnotationsMap().values()));
 	}
 
 	@Override
 	public Annotation getVariantAnnotation(String annotaionId)
 	{
 		return getVariantAnnotationsMap().get(annotaionId);
 	}
 
 	@Override
 	public Sequence getSequenceByName(String name)
 	{
 		for (Sequence sequence : getSequences())
 		{
 			if (sequence.getName().equals(name))
 			{
 				return sequence;
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public VariantQueryResult getSeqVariants(String seqName)
 	{
 		Sequence sequence = getSequenceByName(seqName);
 		if (sequence == null)
 		{
 			throw new GenotypeDataException("Unknown sequence [" + seqName + "]");
 		}
 
 		return sequence.getVariants();
 	}
 
 	@Override
	public SnpGeneticVariant getSnpVariantsByPos(String seqName, int startPos)
 	{
 		List<GeneticVariant> variants = getVariantsByPos(seqName, startPos);
		List<GeneticVariant> snps = new ArrayList<GeneticVariant>();
 
 		for (GeneticVariant variant : variants)
 		{
 			if (variant.getType() == GeneticVariant.Type.SNP)
 			{
 				// only one SNP possible per position. Returning this SNP only
 				return (SnpGeneticVariant) variant;
 			}
 		}
 
 		return null;
 
 	}
 
 	/**
 	 * Get variant annotations by id
 	 */
 	protected abstract Map<String, Annotation> getVariantAnnotationsMap();
 
 }
