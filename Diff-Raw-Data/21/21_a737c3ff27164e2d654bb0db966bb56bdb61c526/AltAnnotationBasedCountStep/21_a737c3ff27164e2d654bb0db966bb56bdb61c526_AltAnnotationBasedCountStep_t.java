 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.wordcount.common;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.steps.wordcount.WordCounter;
 
 public abstract class AltAnnotationBasedCountStep extends BaseCountStep {
 
 	abstract protected boolean accept(MatchType type); 
 	
 	private boolean acceptATA(AltTranslationsAnnotation ata) {
 		if (ata == null) return false;
 		
//		for (AltTranslation at : ata) {
//			if (at == null) continue;
//			
//			MatchType type = at.getType();
//			if (accept(type)) return true;
//		}
		AltTranslation at = ata.getFirst(); // The top match only
		if (at != null) {
 			MatchType type = at.getType();
 			if (accept(type)) return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected long count(TextContainer textContainer, LocaleId locale) {
 		long count = WordCounter.getCount(getSource());
 		if (count == 0) // No metrics found on the container
 			count = WordCounter.count(getSource(), locale); // Word Count metrics are based on counting in source
 		return count;
 
 	}
 
 	@Override
 	protected long count(Segment segment, LocaleId locale) {
 		long count = WordCounter.getCount(segment);
 		if (count == 0) // No metrics found on the container
 			count = WordCounter.count(segment, locale); // Word Count metrics are based on counting in source
 		return count;
 	}
 
 	@Override
 	protected boolean countOnlyTranslatable() {
 		return true;
 	}
 
 	@Override
 	protected long countInTextUnit (ITextUnit textUnit) {
 		if (textUnit == null) return 0;
 		
 		LocaleId srcLocale = getSourceLocale();
 		LocaleId trgLocale = getTargetLocale();
 		
 		TextContainer source = textUnit.getSource();
 		TextContainer target = textUnit.getTarget(trgLocale);
 		if (target == null) return 0;
 		
 		// Individual segments metrics
 		long segmentsCount = 0;
 		long textContainerCount = 0;
 		
 		ISegments segs = target.getSegments();
 		ISegments srcSegments = source.getSegments();
 		if (segs != null) {
 			for (Segment seg : segs) {
 				if (acceptATA(seg.getAnnotation(AltTranslationsAnnotation.class))) {
 					Segment srcSeg = srcSegments.get(seg.getId());
 					long segCount = count(srcSeg, srcLocale);
 					segmentsCount += segCount;
 					saveToMetrics(seg, segCount);
 				}
 			}
 		}
 		// TC metrics
 		if (acceptATA(target.getAnnotation(AltTranslationsAnnotation.class))) {
 			textContainerCount = count(source, srcLocale);
 			saveToMetrics(target, textContainerCount);
 		}
 		
 		if (textContainerCount > 0) return textContainerCount;  
 		if (segmentsCount > 0) return segmentsCount;
 		return 0;
 	}
 
 //	@Override
 //	protected CountContext getCountContext() {
 //		return CountContext.CC_TARGET;
 //	}
 }
