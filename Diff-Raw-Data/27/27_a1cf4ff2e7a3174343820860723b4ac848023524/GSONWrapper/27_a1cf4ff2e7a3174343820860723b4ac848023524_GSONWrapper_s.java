 package FileOps.GSON;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Type;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import CLInterface.Printer;
 import Core.Node;
 import FileOps.Generator;
 import FileOps.Parser;
 import Galaxy.Tree.Workflow.Workflow;
 
 public class GSONWrapper<T extends Object> implements Parser<T>, Generator<T>  {
 	GsonBuilder builder;
 	GSONFieldNamingStrategy strategy;
 	Class root;
 	
 	public GSONWrapper(Class root){
 		this.builder = new GsonBuilder();
 		this.strategy = new GSONFieldNamingStrategy();
 		this.builder.setFieldNamingStrategy(strategy);
 		this.root = root;
 		
 	}
 	
 	public void bindTag(Class parent, String field, String tag) {
 		// TODO Auto-generated method stub
 		strategy.addTag(parent, field, tag);
 		
 	}
 	public T parse(String json){
 		T obj = null;
 		Gson parser = builder.create();
 		obj = (T) parser.fromJson(json,root );
 		return obj;
 	}
 
 	public void bindHandler(Type boundType, GSONHandler handler) {
 		// TODO Auto-generated method stub
 		builder.registerTypeAdapter(boundType, handler);
 		
 	}
 
 	public void bindHandler(GSONHandler handler) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void generate(T object, String path) throws IOException {
 		// TODO Auto-generated method stub
 		builder.setPrettyPrinting();
 		String jsonString = builder.create().toJson(object);
		Printer.output(new FileOutputStream(new File(path)), jsonString);
 	}
 	
 	@Override
 	public String generate(T object) {
 		// TODO Auto-generated method stub
 		String jsonString = builder.create().toJson(object);
 		return jsonString;
 	}
 
 	@Override
 	public T parse(File json) throws FileNotFoundException {
 		Class<T> clazz = null;
 		// TODO Auto-generated method stub
 		Reader reader = new BufferedReader( new FileReader(json.getPath()));
 		T object= (T) builder.create().fromJson(reader, root);
 		
 		return object;
 	}
 
 	
 
 }
