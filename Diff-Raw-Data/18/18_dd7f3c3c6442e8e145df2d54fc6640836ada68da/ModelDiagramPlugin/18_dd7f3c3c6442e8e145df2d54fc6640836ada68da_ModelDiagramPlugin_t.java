 package plugins;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import play.Play;
 import play.PlayPlugin;
 import play.mvc.Router;
 
 public class ModelDiagramPlugin extends PlayPlugin {
 
     Association association = new Association();
 
     public static final Map<String, Model> models = new TreeMap<String, Model>();
 
     @Override
     public void onRoutesLoaded() {
    	
         Router.addRoute("GET", "/@models/?", "modeldiagram.ModelDiagram.index");
         super.onRoutesLoaded();
     }
 
     @Override
     public void onApplicationStart() {
 
         List<Class> entities = Play.classloader.getAnnotatedClasses(Entity.class);
         entities.addAll(Play.classloader.getAnnotatedClasses(MappedSuperclass.class));
         entities.add(play.db.jpa.Model.class);
        
         for (Class clazz : entities) {
             String tableName = clazz.getSimpleName();
             
             Annotation classAnnotation = clazz.getAnnotation(Table.class);
             if (classAnnotation != null) {
                 if (!"".equals(((Table) classAnnotation).name()))
                     tableName = ((Table) classAnnotation).name();
             }
             Model model = new Model(clazz.getCanonicalName(), tableName);
             
             
             if (clazz.getSuperclass().isAnnotationPresent(MappedSuperclass.class)) {
                 model.superClassName = clazz.getSuperclass().getSimpleName();
             }
             model.isMappedSuperClass = clazz.isAnnotationPresent(MappedSuperclass.class);
 
             Field[] fields = clazz.getDeclaredFields();
             for (Field field : fields) {
                 Member member = new Member(field.getName(), field.getType());
                 Annotation[] annotations = field.getDeclaredAnnotations();
                 for (Annotation annotation : annotations) {
                     if (annotation.annotationType().equals(Id.class)) {
                         member.id = true;
                     } else if (annotation.annotationType().equals(Column.class)) {
                         Column column = (Column) annotation;                      
                         member.nullable = column.nullable();
                         member.unique = column.unique();
                         member.length=column.length();
                         if (!"".equals(column.name()))
                             member.columnName = column.name();
                     } else if (annotation.annotationType().equals(JoinColumn.class)) {
                         JoinColumn column = (JoinColumn) annotation;
                         member.nullable = column.nullable();
                         member.unique = column.unique();               
                         if (!"".equals(column.name()))
                             member.columnName = column.name();
                     } else if (annotation.annotationType().equals(OneToMany.class)) {
                         OneToMany oneToMany = (OneToMany) annotation;
                         Association association = new Association();
                         association.type = "OneToMany";
                                    Type genericType = field.getGenericType();
                         if (genericType instanceof ParameterizedType) {
                             ParameterizedType aType = (ParameterizedType) genericType;
                             Type fieldArgType = aType.getActualTypeArguments()[0];
                             association.targetModelName = ((Class) fieldArgType).getSimpleName();
                         }
                         member.association = association;
                     } else if (annotation.annotationType().equals(OneToOne.class)) {
                         OneToOne oneToOne = (OneToOne) annotation;
                         Association association = new Association();
                         association.type = "OneToOne";
                         association.targetModelName = field.getType().getSimpleName();
                         member.association = association;
                     } else if (annotation.annotationType().equals(ManyToMany.class)) {
                         ManyToMany manyToMany = (ManyToMany) annotation;
                         Association association = new Association();
                                    association.type = "ManyToMany";
                         Type genericType = field.getGenericType();
                         if (genericType instanceof ParameterizedType) {
                             ParameterizedType aType = (ParameterizedType) genericType;
                             Type fieldArgType = aType.getActualTypeArguments()[0];
                             association.targetModelName = ((Class) fieldArgType).getSimpleName();
                         }
                         member.association = association;
                     } else if (annotation.annotationType().equals(ManyToOne.class)) {
                         ManyToOne manyToOne = (ManyToOne) annotation;
                         Association association = new Association();
                         association.type = "ManyToOne";
                         association.targetModelName = field.getType().getSimpleName();
                         member.association = association;
                     }
                 }
                 model.addMember(member.name, member);
             }
             models.put(model.name, model);
         }
 
         for (Model model : models.values()) {
             System.out.println("+ " + model.name + " (" + model.tableName + ")");
             for (Member member : model.getMembers()) {
                 System.out.println("    - " + member.type + " " + member.name + " (" + member.columnName + ")");
                 System.out.println("      * unique: " + member.unique + " nullable: " + member.nullable);
                 if (member.association != null) {
                     System.out.println("        => " + member.association.type + " " + member.association.targetModelName);
                 }
 
             }
         }
 
     }
 }
