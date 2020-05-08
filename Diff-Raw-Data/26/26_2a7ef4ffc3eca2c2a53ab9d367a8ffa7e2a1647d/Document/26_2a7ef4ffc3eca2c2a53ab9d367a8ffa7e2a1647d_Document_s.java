 package org.qrone.r7.script.browser;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URI;
 import java.net.URISyntaxException;
 
import org.mozilla.javascript.Callable;
 import org.qrone.r7.ObjectConverter;
 import org.qrone.r7.parser.HTML5Element;
 import org.qrone.r7.parser.HTML5OM;
 import org.qrone.r7.parser.HTML5Template;
 import org.qrone.r7.parser.NodeLister;
 import org.qrone.r7.script.ServletScope;
 import org.qrone.r7.script.ServletScopeObject;
 
 public class Document extends ServletScopeObject{
 	private Writer writer;
 	private HTML5Template t;
 	
 	public Location location;
 	
 	public Document(ServletScope ss) throws IOException{
 		super(ss);
 		writer = ss.writer;
 	}
 	
 	public String getCookie(){
 		return ss.request.getHeader("Cookie");
 	}
 	
 	public HTML5Element getBody(){
 		return t.getBody();
 	}
 	
 
 	public void load(String uri) throws IOException, URISyntaxException{
 		URI u = ss.uri.resolve(uri);
 		if(ss.resolver.exist(u.toString())){
 			HTML5OM om = ss.deck.compile(u);
 			if(om != null){
 				t = new HTML5Template(om, u);
 			}else{
 				throw new IOException();
 			}
 		}
 	}
 
 	public void load(HTML5Template template){
 		t = template;
 	}
 
 	public void set(Object o){
 		t.set(o);
 	}
 
 	public void set(String selector, final Object o){
 		t.set(selector, o);
 	}
 	
 	public void set(String selector, String value){
 		t.set(selector, value);
 	}
 	
 	public void set(String selector, NodeLister lister){
 		t.set(selector, lister);
 	}
 	
 	public void out() throws IOException{
 		writer.append(t.out());
 	}
 
 	public void write(Object out) throws IOException{
 		if(out instanceof String)
 			write((String)out);
 		else
 			writer.append(ObjectConverter.stringify(out));
 	}
 	
 	private void write(String out) throws IOException{
 		writer.append(out);
 	}
 
 	public void writeln(Object out) throws IOException{
 		write(out);
 		write("\n");
 	}
 	
 	public void writeln(String out) throws IOException{
 		write(out);
 		write("\n");
 	}
 	
 	public void flush() throws IOException{
 		writer.flush();
 	}
 	
 	public void close() throws IOException{
 		writer.close();
 	}
 }
