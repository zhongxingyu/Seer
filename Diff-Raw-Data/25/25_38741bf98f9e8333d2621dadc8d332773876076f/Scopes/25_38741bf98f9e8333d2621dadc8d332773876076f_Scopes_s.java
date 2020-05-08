 package geeks.expressive;
 
 import org.reflections.Reflections;
 import org.reflections.scanners.MethodAnnotationsScanner;
 import org.reflections.util.AbstractConfiguration;
 import org.reflections.util.FilterBuilder;
 import org.reflections.util.ClasspathHelper;
 
 import java.util.Arrays;
 import java.util.Set;
 import java.util.List;
 import java.lang.reflect.Method;
 import java.lang.annotation.Annotation;
 
 /**
  * A factory for scopes.
  *
  * @author pabstec
  */
 public class Scopes {
   public static Scope asScope(final Class<?> aClass) {
    return asScope(Arrays.asList(aClass.getName()), new Reflections(new AbstractConfiguration() {{
      setFilter(new FilterBuilder().include(".*"));
      setUrls(Arrays.asList(ClasspathHelper.getUrlForClass(aClass)));
      setScanners(new MethodAnnotationsScanner());
    }}));
   }
 
   public static Scope asScope(final Package aPackage) {
    return asScope(Arrays.asList(aPackage.getName()), new Reflections(new AbstractConfiguration() {{
       setFilter(new FilterBuilder().include(".*"));
      setUrls(ClasspathHelper.getUrlsForPackagePrefix(aPackage.getName()));
       setScanners(new MethodAnnotationsScanner());
     }}));
   }
 
   private static Scope asScope(final List<String> pathsForDescription, final Reflections reflections) {
     return new Scope() {
       public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
         return reflections.getMethodsAnnotatedWith(annotationClass);
       }
 
       @Override
       public String toString() {
         return new StringBuilder().append("Scope").append("{paths=").append(pathsForDescription).append('}').toString();
       }
     };
   }
 }
