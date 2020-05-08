 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.persistence.beans.v1;
 
 import java.io.InputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Range;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.annotation.ScoresAnnotation;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.TMXFilterWriter;
 import net.sf.okapi.common.filterwriter.ZipFilterWriter;
 import net.sf.okapi.common.resource.BaseNameable;
 import net.sf.okapi.common.resource.BaseReferenceable;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Document;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.InlineAnnotation;
 import net.sf.okapi.common.resource.MultiEvent;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TargetPropertiesAnnotation;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextPart;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonPart;
 import net.sf.okapi.common.skeleton.ZipSkeleton;
 import net.sf.okapi.filters.openxml.ConditionalParameters;
 import net.sf.okapi.filters.openxml.OpenXMLZipFilterWriter;
 import net.sf.okapi.filters.pensieve.PensieveFilterWriter;
 import net.sf.okapi.filters.po.POFilterWriter;
 import net.sf.okapi.filters.vignette.SubFilterAnnotation;
 import net.sf.okapi.persistence.BeanMapper;
 import net.sf.okapi.persistence.IVersionDriver;
 import net.sf.okapi.persistence.NamespaceMapper;
 import net.sf.okapi.persistence.VersionMapper;
 import net.sf.okapi.persistence.beans.TypeInfoBean;
 import net.sf.okapi.steps.formatconversion.TableFilterWriter;
 import net.sf.okapi.steps.tokenization.common.InputTokenAnnotation;
 import net.sf.okapi.steps.tokenization.common.Lexem;
 import net.sf.okapi.steps.tokenization.common.Token;
 import net.sf.okapi.steps.tokenization.common.TokensAnnotation;
 import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;
 
 public class OkapiBeans implements IVersionDriver {
 
 	public static final String VERSION = "OKAPI 1.0";
 	
 	@Override
 	public void registerBeans(BeanMapper beanMapper) {
 		// General purpose beans
 		beanMapper.registerBean(IParameters.class, ParametersBean.class);
 		beanMapper.registerBean(Object.class, TypeInfoBean.class); // If no bean was found, use just this one to store class info
 		
 		// Specific class beans				
 		beanMapper.registerBean(Event.class, EventBean.class);		
 		beanMapper.registerBean(TextUnit.class, TextUnitBean.class);
 		beanMapper.registerBean(RawDocument.class, RawDocumentBean.class);
 		beanMapper.registerBean(Property.class, PropertyBean.class);		
 		beanMapper.registerBean(ConditionalParameters.class, ConditionalParametersBean.class);
 		beanMapper.registerBean(TextFragment.class, TextFragmentBean.class);
 		beanMapper.registerBean(TextContainer.class, TextContainerBean.class);
 		beanMapper.registerBean(Code.class, CodeBean.class);
 		beanMapper.registerBean(Document.class, DocumentBean.class);
 		beanMapper.registerBean(DocumentPart.class, DocumentPartBean.class);
 		beanMapper.registerBean(Ending.class, EndingBean.class);
 		beanMapper.registerBean(MultiEvent.class, MultiEventBean.class);
 		beanMapper.registerBean(TextPart.class, TextPartBean.class);
 		beanMapper.registerBean(Segment.class, SegmentBean.class);
 		beanMapper.registerBean(Range.class, RangeBean.class);
 		beanMapper.registerBean(BaseNameable.class, BaseNameableBean.class);
 		beanMapper.registerBean(BaseReferenceable.class, BaseReferenceableBean.class);
 		beanMapper.registerBean(StartDocument.class, StartDocumentBean.class);
 		beanMapper.registerBean(StartGroup.class, StartGroupBean.class);
 		beanMapper.registerBean(StartSubDocument.class, StartSubDocumentBean.class);		
 		beanMapper.registerBean(GenericSkeleton.class, GenericSkeletonBean.class);
 		beanMapper.registerBean(GenericSkeletonPart.class, GenericSkeletonPartBean.class);
 		beanMapper.registerBean(ZipSkeleton.class, ZipSkeletonBean.class);
 		beanMapper.registerBean(ZipFile.class, ZipFileBean.class);
 		beanMapper.registerBean(ZipEntry.class, ZipEntryBean.class);
 		beanMapper.registerBean(InputStream.class, InputStreamBean.class);		
 		beanMapper.registerBean(GenericFilterWriter.class, GenericFilterWriterBean.class);
 		beanMapper.registerBean(TMXFilterWriter.class, TMXFilterWriterBean.class);
 		beanMapper.registerBean(ZipFilterWriter.class, ZipFilterWriterBean.class);
 		beanMapper.registerBean(Token.class, TokenBean.class);
 		beanMapper.registerBean(Lexem.class, LexemBean.class);
 		beanMapper.registerBean(AltTranslation.class, AltTranslationBean.class);
 		// Registered here to require dependencies at compile-time
 		beanMapper.registerBean(OpenXMLZipFilterWriter.class, TypeInfoBean.class); 		
 		beanMapper.registerBean(PensieveFilterWriter.class, TypeInfoBean.class);
 		beanMapper.registerBean(POFilterWriter.class, TypeInfoBean.class);
 		beanMapper.registerBean(TableFilterWriter.class, TypeInfoBean.class);
 		// Annotations		
 		beanMapper.registerBean(AltTranslationsAnnotation.class, AltTranslationsAnnotationBean.class);		
 		beanMapper.registerBean(InlineAnnotation.class, InlineAnnotationBean.class);
 		beanMapper.registerBean(InputTokenAnnotation.class, InputTokenAnnotationBean.class);
 		beanMapper.registerBean(MetricsAnnotation.class, MetricsAnnotationBean.class);
 		beanMapper.registerBean(ScoresAnnotation.class, ScoresAnnotationBean.class);
 		beanMapper.registerBean(SubFilterAnnotation.class, SubFilterAnnotationBean.class);
 		beanMapper.registerBean(TargetPropertiesAnnotation.class, TargetPropertiesAnnotationBean.class);
 		beanMapper.registerBean(TokensAnnotation.class, TokensAnnotationBean.class);
 		//beanMapper.registerBean(.class, Bean.class);
 		
 		VersionMapper.mapVersionId("1.0", VERSION);
 		NamespaceMapper.mapName("net.sf.okapi.steps.xliffkit.common.persistence.versioning.TestEvent", 
 			net.sf.okapi.persistence.beans.v0.TestEvent.class);
 	}
 
 	@Override
 	public String getVersionId() {
 		return VERSION;
 	}
 }
