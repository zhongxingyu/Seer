 package org.cchmc.bmi.snpomics.annotation.annotator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cchmc.bmi.snpomics.GenomicSpan;
 import org.cchmc.bmi.snpomics.SimpleVariant;
 import org.cchmc.bmi.snpomics.annotation.factory.AnnotationFactory;
 import org.cchmc.bmi.snpomics.annotation.interactive.HgvsDnaName;
 import org.cchmc.bmi.snpomics.annotation.interactive.TranscriptEffectAnnotation;
 import org.cchmc.bmi.snpomics.annotation.loader.GenomicSequenceLoader;
 import org.cchmc.bmi.snpomics.annotation.loader.TranscriptLoader;
 import org.cchmc.bmi.snpomics.annotation.reference.GenomicSequenceAnnotation;
 import org.cchmc.bmi.snpomics.annotation.reference.TranscriptAnnotation;
 import org.cchmc.bmi.snpomics.exception.AnnotationNotFoundException;
 import org.cchmc.bmi.snpomics.translation.AminoAcid;
 import org.cchmc.bmi.snpomics.translation.GeneticCode;
 import org.cchmc.bmi.snpomics.util.BaseUtils;
 
 public class TranscriptEffectAnnotator implements Annotator<TranscriptEffectAnnotation> {
 
 	@Override
 	public List<TranscriptEffectAnnotation> annotate(SimpleVariant variant,
 			AnnotationFactory factory) throws AnnotationNotFoundException {
 		TranscriptLoader loader = (TranscriptLoader) factory.getLoader(TranscriptAnnotation.class);
 		GenomicSequenceLoader seqLoader = (GenomicSequenceLoader) factory.getLoader(GenomicSequenceAnnotation.class);
 		GeneticCode code = GeneticCode.getTable(factory.getGenome().getTransTableId(variant.getPosition().getChromosome()));
 		List<TranscriptEffectAnnotation> result = new ArrayList<TranscriptEffectAnnotation>();
 		for (TranscriptAnnotation tx : loader.loadByOverlappingPosition(variant.getPosition())) {
 			TranscriptEffectAnnotation effect = new TranscriptEffectAnnotation(tx);
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
 			//Switch strands if appropriate
 			int dir = 1;
 			if (!tx.isOnForwardStrand()) {
 				ref = BaseUtils.reverseComplement(ref);
 				alt = BaseUtils.reverseComplement(alt);
 				long temp = startCoord;
 				startCoord = endCoord;
 				endCoord = temp;
 				dir = -1;
 			}
 			effect.setCdnaRefAllele(ref);
 			effect.setCdnaAltAllele(alt);
 			//Insertions are given the flanking coordinates
 			if (ref.isEmpty()) {
 				effect.setCdnaStartCoord(getHgvsCoord(tx, startCoord-dir));
 				effect.setCdnaEndCoord(getHgvsCoord(tx, endCoord+dir));
 			} else {
 				effect.setCdnaStartCoord(getHgvsCoord(tx, startCoord));
 				if (endCoord != startCoord)
 					effect.setCdnaEndCoord(getHgvsCoord(tx, endCoord));
 			}
 			HgvsDnaName dna = effect.getHgvsCdnaObject();
 			if (dna.isCoding()) {
 				int cdnaStart = dna.getNearestCodingNtToStart();
 				int cdnaEnd = dna.getNearestCodingNtToEnd();
 				//Revert the insertion coordinates
 				//BUG: What about cases like c.132-1_132insG?  This code will improperly
 				//change cdnaStart/End to 133/131
 				if (ref.isEmpty()) {
 					cdnaStart++;
 					cdnaEnd--;
 				}
 				String cdsSeq = getCDS(seqLoader, tx);
 				int codonStart = (cdnaStart-1) / 3 + 1;
 				int codonEnd = (cdnaEnd-1) / 3 + 1;
 				if (codonStart > 1) codonStart--;
 				if (codonEnd*3 < cdsSeq.length()) codonEnd++;
 				effect.setProtStartPos(codonStart);
 				String refDNA = cdsSeq.substring((codonStart-1)*3, codonEnd*3);
 				String altDNA = refDNA.substring(0, cdnaStart-(codonStart-1)*3-1) +
 								alt +
 								refDNA.substring(cdnaEnd-(codonStart-1)*3);
 				List<AminoAcid> refAA = code.translate(refDNA);
 				effect.setProtRefAllele(refAA);
 				if (Math.abs(alt.length()-ref.length()) % 3 != 0) {
 					effect.setProtFrameshift(true);
 					altDNA += cdsSeq.substring(codonEnd*3);
 				}
 				effect.setProtExtension(code.translate(cdsSeq.substring(codonEnd*3)));
 				effect.setProtAltAllele(code.translate(altDNA));
 			}
 			
 			result.add(effect);
 		}
 		return result;
 	}
 
 	/**
 	 * Generates the HGVS-style coordinate of a position in genomic coordinates relative
 	 * to the cDNA represented by tx
 	 * @param tx
 	 * @param genomicCoord
 	 * @return
 	 */
 	private String getHgvsCoord(TranscriptAnnotation tx, long genomicCoord) {
 		GenomicSpan span = new GenomicSpan(tx.getPosition().getChromosome(), genomicCoord);
 		if (tx.exonContains(span)) {
 			//In an exon: Coding nts are a positive number, 5' UTR are negative, 3' UTR are positive but prefixed with '*'
 			int pos = 1;
 			for (GenomicSpan x : tx.getExons()) {
 				if (x.getEnd() < genomicCoord)
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
 
 	/**
 	 * Loads the CDS sequence for tx
 	 * @param loader
 	 * @param tx
 	 * @return
 	 */
 	private String getCDS(GenomicSequenceLoader loader, TranscriptAnnotation tx) {
 		StringBuilder sb = new StringBuilder();
 		GenomicSpan cds = tx.getCds();
 		for (GenomicSpan x : tx.getExons()) {
 			if (cds.overlaps(x)) {
 				List<GenomicSequenceAnnotation> gsa = loader.loadByExactPosition(cds.intersect(x));
 				sb.append(gsa.get(0).getSequence());
 			}
 		}
 		String result;
 		if (tx.isOnForwardStrand())
 			result = sb.toString();
 		else
 			result = BaseUtils.reverseComplement(sb.toString());
 		return result;
 	}
 
 	@Override
 	public Class<TranscriptEffectAnnotation> getAnnotationClass() {
 		return TranscriptEffectAnnotation.class;
 	}
 
 }
