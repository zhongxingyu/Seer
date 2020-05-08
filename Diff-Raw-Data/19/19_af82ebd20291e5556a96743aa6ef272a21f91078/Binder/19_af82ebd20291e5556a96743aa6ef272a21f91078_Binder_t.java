 package org.virtualrepository.service.io;
 
 import static org.dynamicvalues.Directives.*;
 import static org.sdmxsource.sdmx.api.constants.STRUCTURE_OUTPUT_FORMAT.*;
 import static org.virtualrepository.service.utils.Utils.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.namespace.QName;
 
 import org.dynamicvalues.Directives;
 import org.dynamicvalues.Dynamic;
 import org.dynamicvalues.DynamicIO;
 import org.dynamicvalues.Exclusion;
 import org.sdmxsource.sdmx.api.manager.output.StructureWritingManager;
 import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
 import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
 import org.sdmxsource.sdmx.util.beans.container.SdmxBeansImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtualrepository.Property;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.spi.ServiceProxy;
 import org.virtualrepository.tabular.Column;
 import org.virtualrepository.tabular.Row;
 import org.virtualrepository.tabular.Table;
 
 import com.thoughtworks.xstream.XStream;
 
 import flexjson.JSONSerializer;
 
 @Singleton
 public class Binder {
 	
 	public static Logger log = LoggerFactory.getLogger(Binder.class);
 
 	static private String[] JSON_DEFAULT_EXCLUSION_PATTERNS = {"class", "*.class" };
 	
 	//helper
 	static Exclusion privateProperties = new Exclusion() {
 
 		@Override
 		public boolean exclude(Object object, Field field) {
 				return object instanceof Property && !Property.class.cast(object).isDisplay();
 		}
 	};
 
 	
 	static private Directives defaultDynamicDirectives = by().excluding(type(ServiceProxy.class),
 																		      privateProperties,
 																		      emptyStrings())
 													         .mapping(classesOntoSimpleNames(), 
 													        		 objectsToStringFor(QName.class));
 	
 	
 	private final JSONSerializer jsonOut;
 	private final JAXBContext xmlOut;
 	private final XStream vxmlOut;
 	
 	private final StructureWritingManager sdmxWriter;
 	
 	
 	@Inject
 	public Binder(StructureWritingManager sdmxWriter) {
 		
 		this.sdmxWriter = sdmxWriter;
 		jsonOut = new JSONSerializer().exclude(JSON_DEFAULT_EXCLUSION_PATTERNS);
 		xmlOut = DynamicIO.newInstance();
 		
 		vxmlOut = new XStream();
 		vxmlOut.omitField(RepositoryService.class, "proxy");
		//vxmlOut.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
 		
 		
 	}
 	
 	public String jsonMoM(Object object) {
 		
 		try {
 			return jsonOut.deepSerialize(valueOf(object));
 		}
 		catch(Exception e) {
 			throw wrapped("cannot serialise "+object+" to Json (see cause)",e);
 		}
 		
 	}
 	
 	public String xmlMoM(Object object) {
 		
 		try {
 			StringWriter writer = new StringWriter();
 			Marshaller marshaller = xmlOut.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 			marshaller.marshal(externalValueOf(object),writer);
 			return writer.toString();
 		}
 		catch(Exception e) {
 			throw wrapped("cannot serialise "+object+" to XML (see cause)",e);
 		}
 		
 	}
 	
 
 	
 	public String vxml(Object object) {
 		return vxmlOut.toXML(object);
 	}
 	
 	
 	public String sdmxMl(MaintainableBean bean) {
 		
 		try {
 			
 			SdmxBeans beans = new SdmxBeansImpl(bean);
 			
 			ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
 			
 			sdmxWriter.writeStructures(beans, SDMX_V21_STRUCTURE_DOCUMENT, stream);
 			
 			return new String(stream.toByteArray());
 			
 		}
 		catch(Throwable e) {
 			throw wrapped("cannot serialise "+bean.getId()+" to SDMX-ML (see cause)",e);
 		}
 		
 	}
 	
 	@SuppressWarnings("all")
 	public String jTable(final Table table) {
 		
 		try {
 			
 			return jsonMoM(new Mapped(table));
 			
 		}
 		catch(Throwable e) {
 			throw wrapped("cannot serialise table to Json (see cause)",e);
 		}
 		
 	}
 	
 	
 	public String xTable(final Table table) {
 		
 		try {
 			
 			return xmlMoM(new Mapped(table));
 			
 		}
 		catch(Throwable e) {
 			throw wrapped("cannot serialise table to Xml (see cause)",e);
 		}
 		
 	}
 	
 	public String vTable(final Table table) {
 		
 		try {
 			
 			return vxml(table);
 			
 		}
 		catch(Throwable e) {
 			throw wrapped("cannot serialise table to Xml (see cause)",e);
 		}
 		
 	}
 	
 	//helpers
 	
 	static class Mapped {
 		List<Column> columns;
 		List<Object> rows;
 		
 		public Mapped(Table table) {
 			this.columns=table.columns();
 			
 			//transform rows into map of strings, as Column keys cannot be modelled in json
 			final List<Object> simpleRows = new ArrayList<Object>();
 			
 			for (Row row : table){ 
 				Map<Object,Object> map = new HashMap<Object,Object>();
 				for (Column column : table.columns())
 					map.put(column.toString(),row.get(column));
 
 				simpleRows.add(map);
 			}
 			
 			rows = simpleRows;
 		}
 
 		
 	}
 	
 	private Object valueOf(Object object) throws Exception {
 		return Dynamic.valueOf(object,defaultDynamicDirectives);
 	}
 	
 	private Object externalValueOf(Object object) throws Exception {
 		return Dynamic.externalValueOf(object,defaultDynamicDirectives);
 	}
 	
 }
