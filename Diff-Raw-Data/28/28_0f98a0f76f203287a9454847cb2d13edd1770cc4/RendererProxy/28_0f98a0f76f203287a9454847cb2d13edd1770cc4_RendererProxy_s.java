 package templ.apt;
 
 import templ.annotation.Template;
 import templ.exception.TemplateException;
import templ.mapper.ValueMapper;
import templ.utility.Files;
 import templ.utility.ResourceLocator;
import templ.utility.Strings;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.lang.reflect.Method;
import java.io.*;
 
 public class RendererProxy<T> implements InvocationHandler {
     /**
      * Creates an instance of any interface that implements Renderer.
      * Its methods must have the @Template("somefile.templ") annotation.
      * We suggest that you use a naming convention like this: renderMenu(Menu value), renderPerson(Person value), etc.
      * They should take a compatible object as the only argument and return a String.
      * Alternatively, a OutputStream or Writer object can be given as the second argument,
      * to write the result directly. In that case, the method must return void.
      * @param rendererInterface Interface containing the stub render methods.
      * @return A renderer.
      */
     @SuppressWarnings("unchecked")
     public static <I extends Renderer> I mock(Class<I> rendererInterface) {
         return (I) Proxy.newProxyInstance(
 	        rendererInterface.getClassLoader(),
 	        new Class<?>[] { rendererInterface },
             new RendererProxy<I>());
     }
 
     private RendererProxy() {
     }
 
     public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
         if(method.getDeclaringClass().isAssignableFrom(Object.class)) {
             return method.invoke(o, objects);
         } else {
             Template template = method.getAnnotation(Template.class);
             if(template == null) {
                 throw new TemplateException("Method " + method.getName() + " in " + o.getClass().getName() +
                         " has no @Template(\"myfile.templ\") annotation.");
             }
            String value = ValueMapper.convert(objects[0]);
            try {
                String templateFile = ResourceLocator.locate(template.value());
                 String executableFile = ResourceLocator.locate("Templ");
                 String[] command = {executableFile, "run", templateFile, value};
                 System.out.println("EXEC: "+Strings.separateBy(command, " "));
                 Process process = Runtime.getRuntime().exec(command, null, null);
                 String document = Files.read(process.getInputStream());
                 String error = Files.read(process.getErrorStream());
                 int result = process.waitFor();
                 if(result != 0) {
                     throw new TemplateException("The template processing for method " + method +
                             " was not successful: " + error);
                 }
                 return document;
             } catch(InterruptedException e) {
                 throw new TemplateException(e);
             } catch(IOException e) {
                 throw new TemplateException(e);
            }
         }
     }
 }
