 package org.cchmc.bmi.snpomics.annotation.annotator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cchmc.bmi.snpomics.GenomicSpan;
 import org.cchmc.bmi.snpomics.SimpleVariant;
 import org.cchmc.bmi.snpomics.annotation.factory.AnnotationFactory;
 import org.cchmc.bmi.snpomics.annotation.interactive.HgvsDnaName;
 import org.cchmc.bmi.snpomics.annotation.loader.TranscriptLoader;
 import org.cchmc.bmi.snpomics.annotation.reference.TranscriptAnnotation;
 import org.cchmc.bmi.snpomics.exception.AnnotationNotFoundException;
 import org.cchmc.bmi.snpomics.util.BaseUtils;
 
 /*
  * TODO:
  * 	* Properly report dup instead of ins when appropriate
  *  * Left-align indels
  */
 public class HgvsDnaAnnotator implements Annotator<HgvsDnaName> {
 
 	@Override
 	public List<HgvsDnaName> annotate(SimpleVariant variant,
 			AnnotationFactory factory) throws AnnotationNotFoundException {
 		TranscriptLoader loader = (TranscriptLoader) factory.getLoader(TranscriptAnnotation.class);
 		List<HgvsDnaName> result = new ArrayList<HgvsDnaName>();
 		for (TranscriptAnnotation tx : loader.loadByOverlappingPosition(variant.getPosition())) {
 			HgvsDnaName name = new HgvsDnaName();
 			name.setReference(tx.getID());
 			name.setProteinCoding(tx.isProteinCoding());
 			
 			long startCoord = variant.getPosition().getStart();
 			long endCoord = variant.getPosition().getEnd();
 			String ref = variant.getRef();
 			String alt = variant.getAlt();
 			//Normalize alleles to remove common parts
 			while (ref.length() > 0 && alt.length() > 0 && ref.charAt(0) == alt.charAt(0)) {
 				ref = ref.substring(1);
 				alt = alt.substring(1);
 				startCoord += 1;
 			}
 			//If it's an insertion, adjust the coordinates to flank the new bases
 			if (ref.length() == 0) {
 				startCoord -= 1;
 				endCoord += 1;
 			}
 			//Switch strands if appropriate
 			if (!tx.isOnForwardStrand()) {
 				ref = BaseUtils.reverseComplement(ref);
 				alt = BaseUtils.reverseComplement(alt);
 				long temp = startCoord;
 				startCoord = endCoord;
 				endCoord = temp;
 			}
 			
 			name.setRefAllele(ref);
 			name.setAltAllele(alt);
 			name.setStartCoordinate(getHgvsCoord(tx, startCoord));
 			if (endCoord != startCoord)
 				name.setEndCoordinate(getHgvsCoord(tx, endCoord));
 			result.add(name);
 		}
 		return result;
 	}
 	
 	private String getHgvsCoord(TranscriptAnnotation tx, long genomicCoord) {
 		GenomicSpan span = new GenomicSpan(tx.getPosition().getChromosome(), genomicCoord);
 		if (tx.exonContains(span)) {
 			//In an exon: Coding nts are a positive number, 5' UTR are negative, 3' UTR are positive but prefixed with '*'
 			int pos = 1;
 			for (GenomicSpan x : tx.getExons()) {
				if (x.getEnd() <= genomicCoord)
 					pos += x.length();
 				else if (x.getStart() < genomicCoord)
 					pos += genomicCoord - x.getStart();
 				else
 					break; //We've passed it, no need to keep looking
 			}
 			if (!tx.isOnForwardStrand())
 				pos = tx.length() - pos + 1;
 			pos -= tx.get5UtrLength();
 			//There is no position 0 - it transitions from -1 to 1
 			if (pos < 0) pos -= 1;
 			if (pos > tx.getCdsLength()) {
 				pos -= tx.getCdsLength();
 				return "*"+pos;
 			}
 			return ""+pos;
 		} else {
 			//In an intron: Coord is nearest exonic nt followed by distance and direction (+12, -43)
 			long closest = 0;
 			long closestdist = 1000000000;
 			for (GenomicSpan x : tx.getExons()) {
 				//The complex second clause in the ifs is to properly break ties.
 				//In the case that a position is in the dead center of the intron (equidistant from 
 				//both exons), it should be reported relative to the preceding exon (ie, 2+3, not 3-3)
 				//This, of course, depends on strand
 				if (genomicCoord < x.getStart()) {
 					if ((x.getStart() - genomicCoord < Math.abs(closestdist)) ||
 							(!tx.isOnForwardStrand() && x.getStart() - genomicCoord == Math.abs(closestdist))) {
 						closest = x.getStart();
 						closestdist = genomicCoord - closest;
 					}
 				} else {
 					if ((genomicCoord - x.getEnd() < Math.abs(closestdist)) ||
 							(tx.isOnForwardStrand() && genomicCoord - x.getEnd() == Math.abs(closestdist))) {
 						closest = x.getEnd();
 						closestdist = genomicCoord - closest;
 					}
 				}
 			}
 			if (!tx.isOnForwardStrand())
 				closestdist *= -1;
 			StringBuilder sb = new StringBuilder();
 			sb.append(getHgvsCoord(tx, closest));
 			if (closestdist > 0)
 				sb.append("+");
 			sb.append(closestdist);
 			return sb.toString();
 		}
 	}
 
 	@Override
 	public Class<HgvsDnaName> getAnnotationClass() {
 		return HgvsDnaName.class;
 	}
 
 }
