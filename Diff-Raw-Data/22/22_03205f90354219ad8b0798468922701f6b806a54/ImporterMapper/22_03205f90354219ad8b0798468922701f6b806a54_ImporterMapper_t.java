 /**
  * 
  */
 package org.cotrix.web.ingest.server.climport;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.xml.namespace.QName;
 
 import org.cotrix.common.Outcome;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.utils.Constants;
 import org.cotrix.io.MapService;
 import org.cotrix.io.sdmx.SdmxElement;
 import org.cotrix.io.sdmx.map.Sdmx2CodelistDirectives;
 import org.cotrix.io.tabular.map.ColumnDirectives;
 import org.cotrix.io.tabular.map.Table2CodelistDirectives;
 import org.cotrix.web.ingest.shared.AttributeDefinition;
 import org.cotrix.web.ingest.shared.AttributeMapping;
 import org.cotrix.web.ingest.shared.AttributeType;
 import org.cotrix.web.ingest.shared.ImportMetadata;
 import org.cotrix.web.ingest.shared.MappingMode;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.Table;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface ImporterMapper<T> {
 
 	public Outcome<Codelist> map(ImportTaskSession parameters, T codelist);
 
 	@Singleton
 	public class CsvMapper implements ImporterMapper<Table> {
 
 		protected Logger logger = LoggerFactory.getLogger(CsvMapper.class);
 
 		@Inject
 		protected MapService mapper;
 
 		@Override
 		public Outcome<Codelist> map(ImportTaskSession parameters, Table codelist) {
 
 			AttributeMapping codeAttribute = null;
 			List<ColumnDirectives> columnDirectives = new ArrayList<ColumnDirectives>();
 
 			for (AttributeMapping mapping:parameters.getMappings()) {
 				if (mapping.isMapped()) {
 					if (mapping.getAttributeDefinition().getType()==AttributeType.CODE ||
 							mapping.getAttributeDefinition().getType()==AttributeType.OTHER_CODE) codeAttribute = mapping;
 					else {
 						ColumnDirectives directives = getColumn(mapping);
 						logger.trace("Transformed mapping {} in directive {}",mapping, directives);
 						columnDirectives.add(directives);
 					}
 				}
 			}
 
 			logger.trace("codeAttribute: {}", codeAttribute);
 			if (codeAttribute == null) throw new IllegalArgumentException("Missing code mapping");
 
 			Column column = new Column(codeAttribute.getField().getId());
 
 			Table2CodelistDirectives directives = new Table2CodelistDirectives(column);
 			for (ColumnDirectives directive:columnDirectives) directives.add(directive);
 
 			ImportMetadata metadata = parameters.getMetadata();
 			directives.name(metadata.getName());
 			directives.version(metadata.getVersion());
 
 			directives.mode(convertMappingMode(parameters.getMappingMode()));
 
 			return mapper.map(codelist, directives);
 		}
 
 		protected ColumnDirectives getColumn(AttributeMapping mapping) {
 			Column column = new Column(mapping.getField().getId());
 			ColumnDirectives directive = new ColumnDirectives(column);
 
 			AttributeDefinition definition = mapping.getAttributeDefinition();				
 			directive.name(definition.getName());
			if (definition.getLanguage()!=null) directive.language(definition.getLanguage());
 			directive.type(getType(definition.getType(), definition.getCustomType()));
 
 			return directive;
 		}
 
 		protected QName getType(AttributeType type, String customType)
 		{
 			switch (type) {
 				case ANNOTATION: return Constants.ANNOTATION_TYPE;
 				case DESCRIPTION: return Constants.DESCRIPTION_TYPE;
 				case CODE: return Constants.DEFAULT_TYPE;
 				case OTHER_CODE: return Constants.OTHER_CODE_TYPE;
 				case NAME: return Constants.NAME_TYPE;
 				case OTHER: return new QName(Constants.NS, customType);
 				default: throw new IllegalArgumentException("Unknow attribute type "+type);
 			}
 		}
 
 		protected org.cotrix.io.tabular.map.MappingMode convertMappingMode(MappingMode mode)
 		{
 			if (mode == null) return null;
 			switch (mode) {
 				case IGNORE: return org.cotrix.io.tabular.map.MappingMode.IGNORE;
 				case LOG: return org.cotrix.io.tabular.map.MappingMode.LOG;
 				case STRICT: return org.cotrix.io.tabular.map.MappingMode.STRICT;
 				default: throw new IllegalArgumentException("Uncovertible mapping mode "+mode);
 			}
 		}
 
 	}
 
 	@Singleton
 	public class SdmxMapper implements ImporterMapper<CodelistBean> {
 
 		@Inject
 		protected MapService mapper;
 
 		@Override
 		public Outcome<Codelist> map(ImportTaskSession parameters, CodelistBean codelist) {
 
 			Sdmx2CodelistDirectives directives = new Sdmx2CodelistDirectives();
 
 			for (AttributeMapping mapping:parameters.getMappings()) setDirective(directives, mapping);
 
 			ImportMetadata metadata = parameters.getMetadata();
 			directives.name(metadata.getName());
 			directives.version(metadata.getVersion());
 
 			return mapper.map(codelist, directives);
 		}
 
 		protected void setDirective(Sdmx2CodelistDirectives directives, AttributeMapping mapping) {
 			SdmxElement element = SdmxElement.valueOf(mapping.getField().getId());
 			if (mapping.isMapped()) {
 				AttributeDefinition definition = mapping.getAttributeDefinition();
 				directives.map(element, new QName(definition.getName()));
 			} else directives.ignore(element);
 		}
 
 	}
 }
