 package org.cotrix.io.comet.map;
 
 import static java.lang.String.*;
 import static java.lang.System.*;
 import static org.cotrix.common.Log.*;
 import static org.cotrix.common.Report.*;
 import static org.cotrix.common.Report.Item.Type.*;
 import static org.cotrix.domain.utils.Constants.*;
 import static org.fao.fi.comet.mapping.dsl.DataProviderDSL.*;
 import static org.fao.fi.comet.mapping.dsl.MappingDataDSL.*;
 import static org.fao.fi.comet.mapping.dsl.MappingDetailDSL.*;
 import static org.fao.fi.comet.mapping.dsl.MappingElementDSL.*;
 import static org.fao.fi.comet.mapping.dsl.MappingElementIdentifierDSL.*;
 import static org.fao.fi.comet.mapping.model.MappingScoreType.*;
 
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.common.NamedContainer;
 import org.cotrix.io.impl.MapTask;
 import org.fao.fi.comet.mapping.dsl.MappingDSL;
 import org.fao.fi.comet.mapping.model.DataProvider;
 import org.fao.fi.comet.mapping.model.Mapping;
 import org.fao.fi.comet.mapping.model.MappingData;
 import org.fao.fi.comet.mapping.model.MappingDetail;
 import org.fao.fi.comet.mapping.model.MappingElement;
 import org.fao.fi.comet.mapping.model.MappingElementIdentifier;
 import org.fao.fi.comet.mapping.model.data.Property;
 import org.fao.fi.comet.mapping.model.data.PropertyList;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 
 /**
  * A transformation from {@link Codelist} to {@link CodelistBean}.
  * 
  * @author Fabio Simeoni
  *
  */
 public class Codelist2Comet implements MapTask<Codelist,MappingData,Codelist2CometDirectives> {
 
 	@Override
 	public Class<Codelist2CometDirectives> directedBy() {
 		return Codelist2CometDirectives.class;
 	}
 	
 	/**
 	 * Applies the transformation to a given {@link Codelist} with given directives.
 	 * @param codelist the codelist
 	 * @params directives the directives
 	 * @return the result of the transformation
 	 * @throws Exception if the given codelist cannot be transformed
 	 */
 	public MappingData map(Codelist codelist, Codelist2CometDirectives directives) throws Exception {
 		
 		double time = System.currentTimeMillis();
 
 		report().log(item("mapping codelist "+codelist.name()+"("+codelist.id()+") to Comet")).as(INFO)
 				.log(item(Calendar.getInstance().getTime().toString())).as(INFO);
 		
 		NamedContainer<? extends Attribute> attributes = codelist.attributes();
 		
 		DataProvider source = provider(NS, NS+"/codelist", NS+"/codelist/"+encode(codelist.name().toString()), codelist.version());
 		
 		String previous = attributes.contains(PREVIOUS_VERSION) ? attributes.lookup(PREVIOUS_VERSION).value():null;
 		
 		DataProvider target = provider(NS, NS+"/codelist", NS+"/codelist/"+encode(codelist.name().toString()), previous);
 		
 		MappingData data = new MappingData()
 				.id(uri(codelist.name()+":"+codelist.version()+(previous==null?"":":"+previous)))
 				.version(codelist.version())
 				.producedBy(NS)
 				.linking(source)
 				.to(target)
 				.on(new Date())
 				.with(minimumWeightedScore(1.0), maximumCandidates(1));
 		
 		if (previous!=null)
 			data.setDescription(String.format("A mapping between codelist v.%s and v.%s of codelist %s", codelist.version(), previous, codelist.name()));
 		
 		for (Code c : codelist.codes())
 		
 			try {
 				
 				attributes = c.attributes();
 				
 				MappingElement element = wrap(properties(attributes)).with(id(c.name()));
 				
 				Mapping mapping = MappingDSL.map(element);
 				
 				List<MappingDetail> targets = targets(attributes,directives);
 				
 				if (!targets.isEmpty())
 					mapping.to(targets.toArray(new MappingDetail[0]));
 					
 				data.including(mapping);
 			
 			}
 			catch(Exception e) {
 				
 				report().log(item(format("code {} cannot be mapped ({})",c.name(),e.getMessage()))).as(ERROR);
 			
 			}
 		
 		String msg = format("transformed codelist {} ({}) to Comet in {}",codelist.name(),codelist.id(),(currentTimeMillis()-time)/1000);
 		
 		report().log(item(msg)).as(INFO);
 
 		return data;
 
 	}
 	
 	
 	private List<MappingDetail> targets(NamedContainer<? extends Attribute> attrs, Codelist2CometDirectives directives) {
 		
 		List<MappingDetail> targets = new ArrayList<>();
 		
 		for (QName name : directives.targetAttributes())
 			
 			for (Attribute attr : attrs.getAll(name))
 				
 				try {
 					
 					targets.add(
 						
 						target(wrap(targetProperties(attr)).with(id(attr.value()))).withMappingScore(1.0,AUTHORITATIVE)
 					
 					);
 				}
 				catch(Exception e) {
 					report().log(item(format("{} attribute cannot be mapped to target identifier",name))).as(ERROR);
 				}
 		
 		
 		return targets;
 	}
 	
 	
 	private MappingElementIdentifier id(QName name) throws Exception {
 		return id(name.getLocalPart());
 	}
 	
 	private MappingElementIdentifier id(String id) throws Exception {
 		return identifierFor(uri(id));
 	}
 	
 	private URI uri(String id) throws Exception {
 		return new URI(encode(id));
 	}
 	
 	private String encode(String id) throws Exception {
 		return URLEncoder.encode(id,"UTF-8");
 	}
 	
 	
 	private PropertyList properties(NamedContainer<? extends Attribute> attributes) {
 		
 		List<Property> properties = new ArrayList<>();
 		
 		for (Attribute a : attributes)
			if (a.type()!=SYSTEM_TYPE)
 				properties.add(propertyOf(a));
 		
 		return new PropertyList(properties);
 	}
 	
 	private PropertyList targetProperties(Attribute ... attributes) {
 		
 		List<Property> properties = new ArrayList<>();
 		
 		for (Attribute a : attributes)
 			properties.add(new Property(a.name().toString(),a.type().toString(),a.description()));
 		
 		return new PropertyList(properties);
 	}
 
 	private Property propertyOf(Attribute a) {
 
 		return new Property(a.name().toString(), a.type().toString(), a.value());
 	}
 
 	@Override
 	public String toString() {
 		return "codelist-2-comet";
 	}
 }
