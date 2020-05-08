 /*
  *    Programa Main for Command Line Processing.
  *    
  *    Options:
  *       -help:  Help Text
  *       -exec FILE : execute the file as a script. Select the interpreter by his
  *           extension . i.e.  .js implies JavaScript
  *       -window:  Open a window with the interpreter shell
  *       -window2: Open a window with two panes: one for the command input and other for outputs
  *       -console: accept commands through the command line
  *       -lang LANGUAGE : the language for interpreter (JavaScript by default)
  *       
  *   Note:
  *       exec, console, window and window2 options are incompatible.
  * 
  *  
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.Reader;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.swing.JFrame;
 
 import com.jrsolutions.mapserver.MapUtil;
 
 public class Main {
 
 	enum Mode { unknown, window, window2, console , file };
 	
 	public static void main(String [] args){
 		System.out.println("MAP - TOOLS");
 		
 		Mode mode=Mode.unknown;
 		String fPath=null;
 		String lang="javascript";
 		
 		for(int i=0;i<args.length;i++){
 			String opt=args[i];
 			if( opt.equalsIgnoreCase("-h")
 			 || opt.equalsIgnoreCase("-help")
 			 || opt.equalsIgnoreCase("--help")){
 				help();
 				System.exit(0);
 			}
 			else  if( opt.equalsIgnoreCase("-window")) mode=Mode.window;
 			else  if( opt.equalsIgnoreCase("-window2")) mode=Mode.window2;
 			else  if( opt.equalsIgnoreCase("-console")) mode=Mode.console;
 			else  if( opt.equalsIgnoreCase("-file")) {
 				mode=Mode.file;
 				fPath=args[++i];
 			}else  if( opt.equalsIgnoreCase("-lang")) {
 				lang=args[++i];
 			}
 		}
 		switch(mode){
 		case unknown: help();System.exit(-1);
 		case window:  execWindow(lang); break;
 		case window2: execWindow2(lang); break;
 		case console: execConsole(lang); break;
 		case file:    execFile(fPath); break;
 		}
 	}
 	
 	public static void execWindow(String lang){
 		JFrame frame=new JFrame("Script Console");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		ScriptConsole sc=new ScriptConsole();
 		sc.setScriptLanguage(lang);
 		frame.setContentPane(sc);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	public static void execWindow2(String lang){
 		System.err.println("Not implemented ");
 	}
 	public static void execConsole(String lang){
 		System.err.println("Not implemented ");
 	}
 	public static void help(){
 		System.out.println();
 		System.out.println(" Script command line utilities for mapping");
 		System.out.println();
 		System.out.println("   Usage: Main -help | [ -console|-window|-window2|-exec FILE] | [-lang LANG");
 		System.out.println();
 		System.out.println("   Options:");
 		System.out.println("      -lang LANGUAGE : The script interpreter ");
 		System.out.println("      -console       : exec in comandLine ");
 		System.out.println("      -window        : Open a window ");
 		System.out.println("      -window2       : Open a window with two panes (inputs y outputs)");
 		System.out.println("      -file FILE     : File to execute (interpreter depends on file extension");
 		System.out.println();
 		dumpEngines(new ScriptEngineManager());
 		helpObjects();
 	}
 	
 	public static void helpObjects(){
 		String s=" Los objetos accesibles son: ";
 		System.out.println(s);
 	}
 	
 	public static void dumpEngines(ScriptEngineManager mgr){
 		for(ScriptEngineFactory ef: mgr.getEngineFactories()){
 			System.out.println();
 			System.out.println("  EngineName:"+ef.getEngineName());
 			System.out.println("     EngineVersion:"+ef.getEngineVersion());
 			System.out.println("     LanguageName="+ef.getLanguageName());
 			System.out.println("     LanguageVersion="+ef.getLanguageVersion());
 			System.out.println("     Script Names:"+ef.getNames());
 			System.out.println("     Script Extension:" +ef.getExtensions());
 			System.out.println();
 		}
 	}
 	
 	public static String toStr(long n){
 		StringBuffer sb=new StringBuffer();
 		
 		if(n > 24 * 60 *60 *1000 ){
 			long days = n/ 24 * 60 *60 *1000;
 			sb.append( days +" days ");
 		    n = n % 24 * 60 *60 *1000;
 		}
 		if(n >  60 *60 *1000 ){
 			long hours = n/  60 *60 *1000;
 			sb.append( hours +" hours ");
 		    n = n %  60 *60 *1000;
 		}
 		if(n >  60 *1000 ){
 			long mins = n/  60 *1000;
 			sb.append( mins +" mins ");
 		    n = n %  60 *1000;
 		}
 		if(n >  1000 ){
 			long secs = n/  1000;
 			sb.append( secs +" secs ");
 		    n = n %  1000;
 		}
 		if(n >  0 ){
 			sb.append( n +" msecs ");
 		}
 		return sb.toString();
 	}
 	
 	public static void execFile(String fPath){
 		File f=new File(fPath);
 		if(!f.canRead()){
 			// No puedo leer el fichero
 			return ;
 		}
 		if(f.isDirectory()){
 			// No puedo ejecutar un directorio
 			return;
 		}
 		ScriptEngineManager mgr = new ScriptEngineManager();
 		String fName=f.getName();
		String fExt=fName.substring(fName.lastIndexOf("."));
 		ScriptEngine engine = mgr.getEngineByExtension(fExt);
 		if(engine==null){
 			// error: NO tengo engine para esa extension:
 			// las posibles son:
 				dumpEngines(mgr);
 				}
 		engine.put("xxxx", new MapUtil());
 		try {
 			long t=System.currentTimeMillis();
 			Reader reader=new FileReader(fPath);
 			engine.eval(reader);
 			System.out.println("Finished: "+toStr(System.currentTimeMillis()-t));
 		} catch (ScriptException ex) {
 			ex.printStackTrace();
 		} catch (FileNotFoundException ex){
 			ex.printStackTrace();
 		}
 	}
 }
  
