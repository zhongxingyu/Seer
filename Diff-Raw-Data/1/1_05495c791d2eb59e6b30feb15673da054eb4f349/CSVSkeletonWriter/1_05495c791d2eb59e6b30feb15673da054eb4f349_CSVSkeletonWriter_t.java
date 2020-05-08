 package net.sf.okapi.filters.table.csv;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filterwriter.ILayerProvider;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 
 public class CSVSkeletonWriter extends GenericSkeletonWriter {
 
 	private LocaleId outputLocale;
 	
 	@Override
 	public String processStartDocument(LocaleId outputLocale,
 			String outputEncoding, ILayerProvider layer,
 			EncoderManager encoderManager, StartDocument resource) {		
 		this.outputLocale = outputLocale;
 		return super.processStartDocument(outputLocale, outputEncoding, layer,
 				encoderManager, resource);
 	}
 	
 	@Override
 	public String processTextUnit(ITextUnit tu) {
 		if (tu.isReferent()) {
 			return super.processTextUnit(tu);
 		}
 		
 		if (tu.hasProperty(CommaSeparatedValuesFilter.PROP_QUALIFIED) && 
 			"yes".equals(tu.getProperty(CommaSeparatedValuesFilter.PROP_QUALIFIED).getValue())) {
 				return super.processTextUnit(tu);
 		}
 
 		TextContainer tc;
 		String text;
 		boolean isTarget = tu.hasTarget(outputLocale);
 		if (isTarget) {
 			tc = tu.getTarget(outputLocale);
 		}
 		else {
 			tc = tu.getSource();
 		}
 		
 		if (tc == null)
 			return super.processTextUnit(tu);
 		
 		text = tc.getUnSegmentedContentCopy().toText(); // Just to detect "bad" characters
 		if (text.contains(",") || text.contains("\n")) {
 			TextUnitUtil.addQualifiers(tu, "\"");
 		}
 		
 		return super.processTextUnit(tu);
 	}
 
 }
