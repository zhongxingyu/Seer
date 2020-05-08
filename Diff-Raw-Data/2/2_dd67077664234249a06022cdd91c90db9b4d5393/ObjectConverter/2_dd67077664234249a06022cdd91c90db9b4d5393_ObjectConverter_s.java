 package jpaoletti.jpm.struts.converter;
 
 import jpaoletti.jpm.converter.ConverterException;
 import jpaoletti.jpm.core.*;
 import jpaoletti.jpm.core.message.MessageFactory;
 import jpaoletti.jpm.struts.CollectionHelper;
 import jpaoletti.jpm.struts.tags.PMTags;
 
 /**
  * Converter for integer <br>
  * <pre>
  * {@code
  * <converter class="jpaoletti.jpm.converter.ObjectConverter">
  *     <operationId>edit</operationId>
  *         <properties>
  *             <property name="entity"          value="other_entity" />
  *             <property name="display"         value="other_entity_display" />
  *             <property name="with-null"       value="true" />
  *             <property name="filter"          value="jpaoletti.jpm.core.ListFilterXX" />
  *             <property name="sort-field"      value="xxx" />
  *             <property name="sort-direction"  value="asc | desc" />
  *             <property name="min-search-size" value="0" />
  *         </properties>
  * </converter>
  * }
  * </pre>
  *
  * @author jpaoletti
  *
  */
 public class ObjectConverter extends StrutsEditConverter {
 
     @Override
     public Object build(PMContext ctx) throws ConverterException {
         try {
             final String _entity = getConfig("entity");
             final Entity entity = ctx.getPresentationManager().getEntity(_entity);
             final String newFieldValue = (String) ctx.getFieldValue();
            if (newFieldValue == null || newFieldValue.trim().compareTo("-1") == 0) {
                 return null;
             }
             return entity.getDataAccess().getItem(ctx, new InstanceId(newFieldValue));
         } catch (PMException ex) {
             throw new ConverterException(MessageFactory.error(ctx.getEntity(), ctx.getField(), "cant.convert.object", ex.getMessage()));
         }
     }
 
     @Override
     public Object visualize(PMContext ctx) throws ConverterException {
         final String _entity = getConfig("entity");
         final StringBuilder url = new StringBuilder("object_converter.jsp");
         url.append("?related=").append(getConfig("related", ""));
         url.append("&oentity=").append(_entity);
 
         final Entity entity = ctx.getPresentationManager().getEntity(_entity);
         if (entity == null) {
             throw new ConverterException("object.converter.entity.cannot.be.null");
         }
         if (!entity.isIdentified()) {
             throw new ConverterException("object.converter.id.cannot.be.null");
         }
         final Object fieldValue = ctx.getFieldValue();
         final String _display = getConfig("display");
         if (fieldValue == null) {
             ctx.put("_selected_value", "");
             ctx.put("_selected_id", "-1");
             ctx.put("_with_null", getConfig("with-null", "false")); //false because selected is already null
         } else {
             final CollectionHelper helper = new CollectionHelper(_display);
             try {
                 ctx.put("_selected_value", helper.getObjectDisplay(fieldValue));
                 ctx.put("_selected_id", entity.getDataAccess().getInstanceId(ctx, new EntityInstanceWrapper(fieldValue)).getValue());
                 ctx.put("_with_null", getConfig("with-null", "false"));
             } catch (PMException ex) {
                 throw new ConverterException("object.converter.cannot.get.id");
             }
         }
         ctx.put("_min_search_size", getConfig("min-search-size", "0"));
         final StringBuilder sb = new StringBuilder("/get_list.do");
         sb.append("?entity=").append(_entity);
         sb.append("&filter_class=").append(getConfig("filter"));
         sb.append("&id=").append("");
         if (_display != null) {
             sb.append("&display=").append(_display);
         }
         sb.append("&sortField=").append(getConfig("sort-field"));
         sb.append("&originalEntity=").append(ctx.getEntity().getId());
         sb.append("&originalOperation=").append(ctx.getOperation().getId());
         sb.append("&relatedFieldName=").append(getConfig("related", ""));
         sb.append("&relatedRequired=").append(getConfig("related-required", "false"));
         final String sd = getConfig("sort-direction");
         if (sd != null && !"".equals(sd.trim()) && "desc".equalsIgnoreCase(sd)) {
             sb.append("&sortDir=").append("1");
         }
         ctx.put("jsonUrl", PMTags.plainUrl(ctx.getPmsession(), sb.toString()));
 
         //works only for bootstrap version
         if ("true".equals(getConfig("add", "false"))) {
             if (entity.getOperations().getOperation("add") != null) {
                 url.append("&add=").append(PMTags.plainUrl(ctx.getPmsession(), "add.do?pmid=" + _entity));
             }
         }
         return super.visualize(url.toString());
     }
 }
