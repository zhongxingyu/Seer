 package com.mpower.controller.importexport.importers;
 
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.context.ApplicationContext;
 
 import com.mpower.controller.importexport.exporters.EntityExporter;
 import com.mpower.controller.importexport.exporters.EntityExporterFactory;
 import com.mpower.controller.importexport.exporters.FieldDescriptor;
 import com.mpower.service.SiteService;
 import com.mpower.service.exception.PersonValidationException;
 import com.mpower.type.FieldType;
 import com.mpower.type.PageType;
 
 public abstract class EntityImporter {
 	
 	public final static String ACTION_ADD = "add";
 	public final static String ACTION_CHANGE = "change";
 	public final static String ACTION_DELETE = "delete";
 
 	protected final Log logger = LogFactory.getLog(getClass());
 
 	protected String entity;
 	protected ApplicationContext applicationContext;
 	protected SiteService siteservice;
 	protected EntityExporter entityexporter;
 	protected List<FieldDescriptor> fieldDescriptors; 
 	protected Map<String, FieldDescriptor> fieldDescriptorMap = new HashMap<String, FieldDescriptor>();
 	
 	protected EntityImporter(String entity, ApplicationContext applicationContext) {
 		this.entity = entity;
 		this.applicationContext = applicationContext;
 		siteservice = (SiteService)applicationContext.getBean("siteService");
 		entityexporter = new EntityExporterFactory().getEntityExporter(entity, applicationContext);
 		fieldDescriptors = entityexporter.getExportFieldDescriptors();
 		for (FieldDescriptor fd : fieldDescriptors) fieldDescriptorMap.put(fd.getName(), fd);
 	}	
 	
 	public abstract void importValueMap(String action, Map<String, String> m) throws PersonValidationException;
 	public abstract String getIdField();
 	protected abstract PageType getPageType();
 
 	protected void mapValuesToObject(Map<String, String> values, Object o) {
 		for (Map.Entry<String, String> es : values.entrySet()) {
 			String key = es.getKey();
 			String value = es.getValue();
 			setFieldValue(o, key, value);
 		}
 	}
 	
 	protected FieldDescriptor getFieldDescriptor(String name) {
 		name = FieldDescriptor.getInternalNameForImportFieldName(name);
 		return fieldDescriptorMap.get(name);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void setFieldValue(Object o, String key, String svalue) {
 		
 			try {
 				FieldDescriptor fd = getFieldDescriptor(key);
 				if (fd == null) throw new RuntimeException("Unrecognized import field name: "+key);
 				
 				Object value = convertToObject(svalue, fd);
 				
 				if (fd.getType() == FieldDescriptor.CUSTOM) {
 					Method m = o.getClass().getMethod("setCustomFieldValue", new Class[]{String.class, String.class});
 					m.invoke(o, new Object[]{key, value});
 				} else if (fd.isMap()) {
 					Method m = o.getClass().getMethod("get"+fd.getMapType()+"Map");
 					Map map = (Map)m.invoke(o);
 					Object so = map.get(fd.getKey()); 
 					if (so == null) throw new RuntimeException("Unable to import field.");
 					BeanUtils.setProperty(so, fd.getSubField(), value);
 				} else if (fd.isDependentField()) {
 					String depobject = fd.getDependentObject();
 					Method m = o.getClass().getMethod("get"+FieldDescriptor.toInitialUpperCase(depobject));
 					Object so = m.invoke(o);
 					if (so == null) throw new RuntimeException("Unable to import field."); 
 					BeanUtils.setProperty(so, fd.getDependentField(), value);
 				} else {
 					BeanUtils.setProperty(o, key, value);
 				}
 			} catch (Exception e) {
 				logger.debug(""+e);
 				throw new RuntimeException("Unable to set field value "+key+" to "+svalue+": "+e.getMessage());
 			}
 		
 	}
 	
 	// Convert types for native fields on entity or subentity.  Not needed for custom fields since they are all strings.
 	protected Object convertToObject(String svalue, FieldDescriptor fd) {
 		FieldType fieldType = fd.getFieldDefinition().getFieldType();
		if (fieldType == FieldType.DATE || fieldType == FieldType.CC_EXPIRATION) {
 			CustomDateEditor ed = new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true);
 			ed.setAsText(svalue);
 			return ed.getValue();
 		}
 		return svalue;
 	}
 	
 
 	
 }
