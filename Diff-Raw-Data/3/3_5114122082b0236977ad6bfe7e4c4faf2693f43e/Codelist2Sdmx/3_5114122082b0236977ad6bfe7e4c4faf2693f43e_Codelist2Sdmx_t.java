 package org.cotrix.io.sdmx.map;
 
 import static org.cotrix.common.Log.*;
 import static org.cotrix.common.Report.*;
 import static org.cotrix.common.Report.Item.Type.*;
 import static org.sdmxsource.sdmx.api.constants.TERTIARY_BOOL.*;
 
 import java.text.ParseException;
 import java.util.Calendar;
 
 import org.cotrix.common.Report;
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.links.Link;
 import org.cotrix.domain.trait.Described;
 import org.cotrix.domain.trait.Named;
 import org.cotrix.domain.utils.DomainUtils;
 import org.cotrix.io.impl.MapTask;
 import org.cotrix.io.sdmx.SdmxElement;
 import org.cotrix.io.sdmx.map.Codelist2SdmxDirectives.GetClause;
 import org.sdmxsource.sdmx.api.exception.SdmxSemmanticException;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.sdmxsource.sdmx.api.model.mutable.base.AnnotationMutableBean;
 import org.sdmxsource.sdmx.api.model.mutable.base.NameableMutableBean;
 import org.sdmxsource.sdmx.api.model.mutable.codelist.CodeMutableBean;
 import org.sdmxsource.sdmx.api.model.mutable.codelist.CodelistMutableBean;
 import org.sdmxsource.sdmx.sdmxbeans.model.mutable.base.AnnotationMutableBeanImpl;
 import org.sdmxsource.sdmx.sdmxbeans.model.mutable.codelist.CodeMutableBeanImpl;
 import org.sdmxsource.sdmx.sdmxbeans.model.mutable.codelist.CodelistMutableBeanImpl;
 import org.sdmxsource.sdmx.util.date.DateUtil;
 
 /**
  * Transforms {@link Codelist}s in {@link CodelistBean}s.
  * <p>
  * 
  * 
  * @author Fabio Simeoni
  *
  */ 
  
 /* defaults:
  * 
  * (local) names -> ids (and names if no other are specified)
  * version -> version
  * agency -> default agency
  * status > undefined
  * 
  * 
  * 
  */
 public class Codelist2Sdmx implements MapTask<Codelist,CodelistBean,Codelist2SdmxDirectives> {
 
 	@Override
 	public Class<Codelist2SdmxDirectives> directedBy() {
 		return Codelist2SdmxDirectives.class;
 	}
 	
 	/**
 	 * Applies the transformation to a given {@link Codelist} with given directives.
 	 * @param codelist the codelist
 	 * @params directives the directives
 	 * @return the result of the transformation
 	 * @throws Exception if the given codelist cannot be transformed
 	 */
 	public CodelistBean map(Codelist codelist, Codelist2SdmxDirectives directives) throws Exception {
 		
 		double time = System.currentTimeMillis();
 
 		report().log(item("transforming codelist "+codelist.qname()+"("+codelist.id()+") to SDMX")).as(INFO)
 				.log(item(Calendar.getInstance().getTime().toString())).as(INFO);
 		
 		String id = directives.id()==null?codelist.qname().getLocalPart():directives.id();
 		
 		CodelistMutableBean codelistbean = new CodelistMutableBeanImpl();
 		
 		codelistbean.setAgencyId(directives.agency());
 		codelistbean.setId(id);
 		codelistbean.setVersion(directives.version()==null?codelist.version():directives.version());
 		codelistbean.setFinalStructure(directives.isFinal()==null?UNSET:directives.isFinal()==true?TRUE:FALSE);
 		
 		mapCodelistAttributes(codelist,codelistbean,directives);
 		
 		if (codelistbean.getNames()==null)
 			codelistbean.addName("en",codelistbean.getId());
 
 		 
 		for (Code code : codelist.codes()) {
 			
 			CodeMutableBean codebean = new CodeMutableBeanImpl();
 			
 			if (code.qname()==null) {
 				report().log(item(code.id()+" has no name ")).as(ERROR);
 				continue;
 			}
 			
 			codebean.setId(code.qname().getLocalPart());
 			
 			mapAttributes(code,codebean,directives.forCodes());
 			
 			mapCodelinks(code, codebean, directives.forCodes());
 			
 			//default name
 			if (codebean.getNames()==null)
 				codebean.addName("en",codebean.getId());
 			
 			codelistbean.addItem(codebean);
 		}
 		
 		String msg = "transformed codelist "+codelist.qname()+"("+codelist.id()+") to SDMX in "+(System.currentTimeMillis()-time)/1000;
 		
 		report().log(item(msg)).as(INFO);
 
 		try {
 			return codelistbean.getImmutableInstance();
 		}
 		catch(SdmxSemmanticException e) {//hilarious, check the spelling...
			report().log("SDMX validation error: "+unwrapErrorDescription(e)).as(ERROR);
			return null;
 		}
 	}
 	
 	//helpers
 	private String unwrapErrorDescription(Throwable t) {
 		
 		while (t.getCause()!=null && t.getCause() instanceof SdmxSemmanticException)
 			t = t.getCause();
 		
 		return t.getMessage();
 	}
 	
 	private void mapCodelistAttributes(Codelist list, CodelistMutableBean bean, Codelist2SdmxDirectives directives) {
 		
 		//name-based pass
 		for (Attribute a : list.attributes()) {
 			
 			String val = a.value();
 			SdmxElement element = directives.forCodelist().get(a);
 			
 			if (element!=null)
 				switch(element) {
 				
 					case VALID_FROM:
 						try {
 							bean.setStartDate(DateUtil.getDateTimeFormat().parse(val));
 						}
 						catch(ParseException e) {
 							Report.report().log(item("unparseable start date: "+a.value())).as(WARN);
 						}
 						break;
 					case VALID_TO:
 						try {
 							bean.setEndDate(DateUtil.getDateTimeFormat().parse(val));
 						}
 						catch(ParseException e) {
 							Report.report().log(item("unparseable end date: "+a.value())).as(WARN);
 						}
 						break;
 					
 					default:
 				}
 		}
 
 		mapAttributes(list, bean,directives.forCodelist());
 		
 	}
 	
 	private <T extends Described & Named> void mapAttributes(T attributed, NameableMutableBean bean, GetClause directives) {
 		
 		for (Attribute a : attributed.attributes()) {
 			
 			String val = a.value();
 			String lang = a.language()==null?"en":a.language();
 			
 			SdmxElement element = directives.get(a);
 			
 			if (element!=null)
 				
 				switch(element) {
 				
 					case NAME:
 						bean.addName(lang,val);
 						break;
 					case DESCRIPTION:
 						bean.addDescription(lang,val);
 						break;
 					case ANNOTATION:
 						AnnotationMutableBean annotation = new AnnotationMutableBeanImpl();
 						annotation.setTitle(a.qname().getLocalPart());
 						annotation.addText(lang, val);
 						bean.addAnnotation(annotation);		
 						break;
 						
 					default:
 						
 				}
 			
 			
 		}
 			
 	}
 	
 	private void mapCodelinks(Code code, NameableMutableBean bean, GetClause directives) {
 		
 		for (Link link : code.links()) {
 			
 			String val = link.valueAsString();
 			String lang = DomainUtils.languageOf(link.definition());
 			
 			SdmxElement element = directives.get(link);
 			
 			if (element!=null)
 				switch(element) {
 				
 					case NAME:
 						bean.addName(lang,val);
 						break;
 					case DESCRIPTION:
 						bean.addDescription(lang,val);
 						break;
 					case ANNOTATION:
 						AnnotationMutableBean annotation = new AnnotationMutableBeanImpl();
 						annotation.setTitle(link.qname().getLocalPart());
 						annotation.addText(lang, val);
 						bean.addAnnotation(annotation);		
 						break;
 						
 					default:
 						
 				}
 			
 			
 		}
 			
 	}
 	
 	@Override
 	public String toString() {
 		return "codelist-2-sdmx";
 	}
 }
