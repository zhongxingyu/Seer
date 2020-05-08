 package tohtml;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
 
 import tohtml.elements.HtmlComposedElement;
 import tohtml.elements.HtmlTextElement;
 import utils.Iters;
 import utils.Predicate;
 
 
 public class HtmlParser {
 
 	final HtmlFormatter<?>[] formatters;
 	
 	public HtmlParser(HtmlFormatter<?>...formatters) {
 		this.formatters = formatters;
 	}
 
 	public final HtmlElement parse(Object src) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException{
 		HtmlElement root = new HtmlComposedElement("html");
 		/*
 		 * Head
 		 */
 		Class srcClass = src.getClass();
 		HtmlElement head = new HtmlComposedElement("head");
 		head.addElement(new HtmlTextElement("title", srcClass.getSimpleName()));
 		root.addElement(head);
 		/*
 		 * Body
 		 */
 		Field [] fs = srcClass.getDeclaredFields();
 		HtmlElement body = new HtmlComposedElement("body");
 		body.addElement(new HtmlTextElement("h1", srcClass.getSimpleName()));
 		root.addElement(body);
 		for (Field field : fs) {
			if((field.getModifiers() & Modifier.STATIC) != 0) continue;
 			field.setAccessible(true);
 			final Object val = field.get(src);
 			assert val.getClass() == field.getType();
 			Format f = field.getAnnotation(Format.class);
 			HtmlFormatter formatter;
 			if(f != null){
 				if(f.value() != Format.NullFormatter.class){
 					formatter = f.value().newInstance();
 				}
 				else{
 					formatter = new HtmlFormatterAdapter(srcClass, f.method(), val);
 				}
 			}
 			else{
 				formatter = Iters.find(formatters, new Predicate<HtmlFormatter<?>>() {
 					public boolean eval(HtmlFormatter e) {
 						return e.support(val.getClass());
 					}
 				});
 			}
 			body.addElement(formatter.format(field.getName(), val));
 			
 		}
 		return root;
 	}
 }
