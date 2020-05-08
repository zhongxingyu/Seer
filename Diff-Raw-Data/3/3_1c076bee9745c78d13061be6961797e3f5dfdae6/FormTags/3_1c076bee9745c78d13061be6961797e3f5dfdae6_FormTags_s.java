 package tags;
 
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import controllers.CRUD;
 import groovy.lang.Closure;
 import play.data.validation.Email;
 import play.data.validation.Max;
 import play.data.validation.MaxSize;
 import play.data.validation.Min;
 import play.data.validation.MinSize;
 import play.data.validation.Range;
 import play.data.validation.Required;
 import play.data.validation.URL;
 import play.data.validation.Validation;
 import play.db.Model;
 import play.exceptions.TagInternalException;
 import play.exceptions.TemplateExecutionException;
 import play.i18n.Messages;
 import play.libs.F;
 import play.mvc.Scope;
 import play.templates.FastTags;
 import play.templates.GroovyTemplate;
 import play.templates.JavaExtensions;
 import play.templates.TagContext;
 
 /**
  * Custom FastTags, some written together from Play! and from the jQuery validation module, with a few modifications
  * so they can be used in CRUD-like tags for form generation.
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @FastTags.Namespace("forms")
 public class FormTags extends FastTags {
 
     public static final String FIELD = "field_";
 
     /**
      * The field tag is a helper, based on the spirit of Don't Repeat Yourself.
      * It is based on the {@link FastTags#_field(java.util.Map, groovy.lang.Closure, java.io.PrintWriter, play.templates.GroovyTemplate.ExecutableTemplate, int)} method.
      * Additionally, it does a couple of things:
      * <ul>
      * <li>expects a baseObject or baseClass to compute the type of the field at hand</li>
      * <li>renders validation rules for the jQuery validation plugin</li>
      * <li>adds the field path to the parent form so that operations can be performed on the field list (for data binding)</li>
      * </ul>
      *
      * @param args     tag attributes
      * @param body     tag inner body
      * @param out      the output writer
      * @param template enclosing template
      * @param fromLine template line number where the tag is defined
      */
     public static void _field(Map<?, ?> args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine) {
         Map<String, Object> field = new HashMap<String, Object>();
         String path = args.get("field").toString();
         // make it possible to override the name... should be more generic
         Object name = args.get("name") == null ? path : args.get("name");
         field.put("name", name);
         field.put("path", path);
         field.put("id", path.replace('.', '_'));
         field.put("flash", Scope.Flash.current().get(path));
         field.put("flashArray", field.get("flash") != null && !field.get("flash").toString().isEmpty() ? field.get("flash").toString().split(",") : new String[0]);
         field.put("error", Validation.error(path));
         field.put("errorClass", field.get("error") != null ? "hasError" : "");
         String[] pieces = path.split("\\.");
         Object obj = args.get("baseObject");
         String baseClass = (String) args.get("baseClass");
 
         // store the field name in the form TagContext so that we can retrieve it later
        TagContext.parent("ox.form").data.put(FIELD + path.replaceAll("\\.", "_"), path);
 
         if (obj != null) {
             if (pieces.length > 1) {
                 try {
                     F.Tuple<Field, Object> fo = getField(pieces, obj);
                     if (fo != null) {
                         field.put("validationData", buildValidationDataString(fo._1));
 
                         // TODO this code is deprecated and is used only by the old forms that directly write the initial value in the field
                         // TODO remove once we got rid of all old forms in the code
                         if (TagContext.parent("input.form") != null) {
                             try {
                                 Method getter = obj.getClass().getMethod("get" + JavaExtensions.capFirst(fo._1.getName()));
                                 field.put("value", getter.invoke(obj, new Object[0]));
                             } catch (NoSuchMethodException e) {
                                 field.put("value", fo._1.get(fo._2).toString());
                             }
                         }
                     }
                 } catch (Exception e) {
                     // if there is a problem reading the field we dont set any value
                 }
             } else {
                 field.put("value", obj);
             }
         } else {
             Class baseClazz = null;
             if (baseClass != null) {
                 baseClazz = getBaseClass(template, fromLine, baseClass);
 
                 try {
                     F.Tuple<Field, Class> fc = getField(pieces, baseClazz);
                     Field f = fc._1;
                     if (f != null) {
                         field.put("validationData", buildValidationDataString(f));
                     }
                 } catch (NoSuchFieldException e) {
                     String message = "Can't find field in path '" + path + "' with baseClass '" + baseClass + "': " + e.getMessage();
                     throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
                 } catch (Exception e) {
                     String message = "Internal error: could not build validation string: " + e.getMessage();
                     throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
                 }
 
             } else {
                 throw new TemplateExecutionException(template.template, fromLine, "Can't compute a field when no baseObject or baseClass is given", new TagInternalException("Can't compute a field when no baseObject or baseClass is given"));
             }
 
         }
         body.setProperty("field", field);
         body.call();
     }
 
     private static Class getBaseClass(GroovyTemplate.ExecutableTemplate template, int fromLine, String baseClass) {
         Class baseClazz;
         try {
             baseClazz = Class.forName(baseClass);
         } catch (ClassNotFoundException cnfe) {
             String message = "Could not find class " + baseClass;
             throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
         }
         return baseClazz;
     }
 
     private static F.Tuple<Field, Object> getField(String[] pieces, Object baseObject) throws NoSuchFieldException, IllegalAccessException {
         if (pieces.length > 1) {
             for (int i = 1; i < pieces.length; i++) {
                 Field f = baseObject.getClass().getField(pieces[i]);
                 if (i == (pieces.length - 1)) {
                     return new F.Tuple<Field, Object>(f, baseObject);
                 } else {
                     baseObject = f.get(baseObject);
                 }
             }
         }
         return null;
     }
 
     private static F.Tuple<Field, Class> getField(String[] pieces, Class baseClass) throws NoSuchFieldException {
         if (pieces.length > 1) {
             for (int i = 1; i < pieces.length; i++) {
                 // TODO improve this so it uses the accessor (getter, or direct field access)
                 // this needs a rewrite of the type introspection to use a member instead of a field.
                 Field f = baseClass.getField(pieces[i]);
                 if (i == pieces.length - 1) {
                     return new F.Tuple<Field, Class>(f, baseClass);
                 } else {
                     baseClass = f.getType();
                 }
             }
         }
         return null;
     }
 
     /**
      * This tag computes a simplified type of a field based on the base class and a path to the field.
      * It uses the Play! {@link CRUD} module which has a method for computing an ObjectType.
      * As an output, it sets two body parameters:
      * <ul>
      * <li>objectField: the {@link CRUD} objectField representation of the field</li>
      * <li>fieldType: the type of the field</li>
      * </ul>
      *
      * @param args     the arguments passed to the tag. Expected arguments: either baseClass or baseObject, field (path to the field)
      * @param body     tag inner body
      * @param out      the output writer
      * @param template enclosing template
      * @param fromLine template line number where the tag is defined
      */
     public static void _objectField(Map<?, ?> args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine) {
         String path = args.get("field").toString();
         String[] pieces = path.split("\\.");
         Object obj = args.get("baseObject");
         String baseClass = (String) args.get("baseClass");
         Class clazz = null;
         if (obj != null) {
             clazz = obj.getClass();
         } else if (baseClass != null) {
             clazz = getBaseClass(template, fromLine, baseClass);
         } else {
             String message = "No baseObject nor baseClass passed";
             throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
         }
         if (clazz != null) {
             if (pieces.length > 1) {
                 try {
                     F.Tuple<Field, Class> fc = getField(pieces, clazz);
                     Field f = fc._1;
                     if (f != null) {
                         Model.Property property = null;
                         if (Model.class.isAssignableFrom(fc._2)) {
                             Model.Factory factory = Model.Manager.factoryFor((Class<? extends Model>) fc._2);
                             // TODO probably we should be caching this...?
                             for (Model.Property p : factory.listProperties()) {
                                 if (p.name.equals(f.getName())) {
                                     property = p;
                                     break;
                                 }
                             }
                             if (property == null) {
                                 throw new Exception();
                             }
 
                         } else {
                             property = new Model.Property();
                             property.field = f;
                             property.isGenerated = false;
 
                         }
                         CRUD.ObjectType.ObjectField objectField = new CRUD.ObjectType.ObjectField(property);
                         body.setProperty("objectField", objectField);
                         body.setProperty("fieldType", objectField.type);
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                     String message = "Cannot compute field '" + path + "' of class '" + clazz.getName() + "'";
                     throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
                 }
 
             } else {
                 String message = "You passed a path without base object identifier... probably this is wrong.";
                 throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
 //                body.setProperty("fieldType", clazz.getName());
             }
         }
         body.call();
     }
 
     private static String buildValidationDataString(Field f) throws Exception {
         StringBuilder result = new StringBuilder("{");
         List<String> rules = new ArrayList<String>();
         Map<String, String> messages = new HashMap<String, String>();
         Required required = f.getAnnotation(Required.class);
         if (required != null) {
             rules.add("required:true");
             if (required.message() != null) {
                 messages.put("required", Messages.get(required.message()));
             }
         }
         Min min = f.getAnnotation(Min.class);
         if (min != null) {
             rules.add("min:" + new Double(min.value()).toString());
             if (min.message() != null) {
                 messages.put("min", Messages.get(min.message(), null, min.value()));
             }
         }
         Max max = f.getAnnotation(Max.class);
         if (max != null) {
             rules.add("max:" + new Double(max.value()).toString());
             if (max.message() != null) {
                 messages.put("max", Messages.get(max.message(), null, max.value()));
             }
         }
         Range range = f.getAnnotation(Range.class);
         if (range != null) {
             rules.add("range:[" + new Double(range.min()).toString() + ", " + new Double(range.max()).toString() + "]");
             if (range.message() != null) {
                 messages.put("range", Messages.get(range.message(), null, range.min(), range.max()));
             }
         }
         MaxSize maxSize = f.getAnnotation(MaxSize.class);
         if (maxSize != null) {
             rules.add("maxlength:" + new Integer(maxSize.value()).toString());
             if (maxSize.message() != null) {
                 messages.put("maxlength", Messages.get(maxSize.message(), null, maxSize.value()));
             }
         }
         MinSize minSize = f.getAnnotation(MinSize.class);
         if (minSize != null) {
             rules.add("minlength:" + new Integer(minSize.value()).toString());
             if (minSize.message() != null) {
                 messages.put("minlength", Messages.get(minSize.message(), null, minSize.value()));
             }
         }
         URL url = f.getAnnotation(URL.class);
         if (url != null) {
             rules.add("url:true");
             if (url.message() != null) {
                 messages.put("url", Messages.get(url.message()));
             }
         }
         Email email = f.getAnnotation(Email.class);
         if (email != null) {
             rules.add("email:true");
             if (email.message() != null) {
                 messages.put("email", Messages.get(email.message()));
             }
         }
         if (rules.size() > 0) {
             boolean first = true;
             for (String rule : rules) {
                 if (first) {
                     first = false;
                 } else {
                     result.append(",");
                 }
                 result.append(rule);
             }
         }
         if (messages.size() > 0) {
             result.append(",messages:{");
             boolean first = true;
             for (String key : messages.keySet()) {
                 if (first) {
                     first = false;
                 } else {
                     result.append(",");
                 }
                 result.append("\"");
                 result.append(key);
                 result.append("\"");
                 result.append(":");
                 result.append("\"");
                 result.append(messages.get(key));
                 result.append("\"");
             }
             result.append("}");
         }
         result.append("}");
         return result.toString();
     }
 
 }
