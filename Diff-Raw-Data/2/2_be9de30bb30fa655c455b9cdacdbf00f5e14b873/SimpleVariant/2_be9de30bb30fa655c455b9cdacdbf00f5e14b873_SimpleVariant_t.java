 package org.cchmc.bmi.snpomics;
 
 /**
  * <p>A SimpleVariant is, well, the simplest representation of a variant.  There is only
  * one alternate allele, there are no annotations, etc.</p>
  * <p>This record is normalized - that is, the ref and alt alleles are as short
  * as possible to represent the variation (but at least one nucleotide), and the position
  * is the correct span of the ref allele</p>
  * <p>Note that a SimpleVariant is also immutable</p>
  * @author dexzb9
  *
  */
 public class SimpleVariant {
 	public SimpleVariant(GenomicSpan pos, String refAllele, String altAllele) {
 		position = pos.clone();
 		ref = refAllele;
 		alt = altAllele;
 
 		//Normalize!
 		while (ref.length() > 1 && alt.length() > 1 &&
 				ref.charAt(0) == alt.charAt(0)) {
 			ref = ref.substring(1);
 			alt = alt.substring(1);
			position.setStart(position.getStart()+1);
 		}
 	}
 	
 	private GenomicSpan position;
 	private String ref;
 	private String alt;
 	public GenomicSpan getPosition() { return position.clone(); }
 	public String getRef() { return ref; }
 	public String getAlt() { return alt; }
 }
