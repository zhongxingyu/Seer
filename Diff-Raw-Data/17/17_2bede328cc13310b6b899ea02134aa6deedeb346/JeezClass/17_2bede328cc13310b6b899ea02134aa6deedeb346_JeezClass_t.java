 package jeez.lang;
 
 import static jeez.interpreter.execution.Bootstrap.CLASS;
 import static jeez.interpreter.execution.Bootstrap.OBJECT;
 import static jeez.interpreter.execution.Bootstrap.getObjectClass;
 import static jeez.lang.Constants.NEW;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jeez.interpreter.execution.ExecutionContext;
 import jeez.interpreter.execution.MethodInvoker;
 import jeez.lang.expression.Expression;
 
 public class JeezClass extends JeezObject implements Type {
   
   private JeezClass superClass = OBJECT;
   
   private String name;
   
   private Map<String, Attribute> classAttributes = new HashMap<String, Attribute>();
   
   private Map<String, Method> classMethods = new HashMap<String, Method>();
   
   public JeezClass(String string) {
     super(CLASS);
     this.name = string;
   }
   
   public String getName() {
     return name;
   }
   
   public void setSuperClass(JeezClass superClass) {
     this.superClass = superClass;
   }
   
   public JeezClass getSuperClass() {
     return superClass;
   }
   
   public void addToClassAttributes(Attribute attribute) {
     classAttributes.put(attribute.getName(), attribute);
   }
   
   public Attribute getClassAttribute(String name) {
     return classAttributes.get(name);
   }
   
   public Collection<Attribute> getClassAttributes() {
     return classAttributes.values();
   }
   
   public void addToClassMethods(Method method) {
     classMethods.put(method.getName(), method);
   }
   
   public Method getClassMethod(String name) {
     return classMethods.get(name);
   }
   
   public Collection<Method> getClassMethods() {
     return classMethods.values();
   }
   
   public JeezObject createNewObject(List<Expression> arguments, ExecutionContext context) {
     JeezObject object = new JeezObject(this);
     for (Attribute attribute : getClassAttributes()) {
       object.addToAttributes(new Variable(attribute.getName()));
     }
     
     Method initializer = getClassMethod(NEW);
    new MethodInvoker().doInvoke(object, initializer, NEW, arguments, context);
     
     return object;
   }
 
   public boolean isAssignableFrom(JeezClass clazz) {
     if (equals(clazz)) {
       return true;
     }
     
     JeezClass superClass = clazz.getSuperClass();
     while (superClass != getObjectClass()) {
       if (equals(superClass)) {
         return true;
       }
       superClass = superClass.getSuperClass();
     }
     
     return false;
   }
   
   @Override
   public boolean equals(Object object) {
     if (!(object instanceof JeezClass)) {
       return false;
     }
     
     return name.equals(((JeezClass) object).name);
   }
   
   
   @Override
   public int hashCode() {
     return name.hashCode();
   }
   
   @Override
   public java.lang.String toString() {
     return "Class " + getName();
   }
 }
