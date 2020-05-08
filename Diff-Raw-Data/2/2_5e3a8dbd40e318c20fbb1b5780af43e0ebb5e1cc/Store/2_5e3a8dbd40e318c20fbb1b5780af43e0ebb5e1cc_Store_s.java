 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl;
 
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 
 public abstract class Store {
 	
 	final protected static String snapshotsFile = "snapshots";
 	final protected static String infoFile = "info";
 	final protected static String traceFile = "trace";
 	
 	protected String separator = "/";
 	
 	final Map<String,Trace<?>> traces = new HashMap<String,Trace<?>>();
 	final static Map<String,Class<?>> type_class_map = new HashMap<String,Class<?>>();
 	
 	private Set<Reader<?>> openReaders = new HashSet<Reader<?>>();
 	
 	@SuppressWarnings("serial")
 	public class NoSuchTraceException extends Exception {
 		private String trace_name; 
 		public NoSuchTraceException(String traceName){ trace_name = traceName;}
 		@Override
 		public String toString(){
 			return "Error! Could not find trace '"+trace_name+"'";
 		}
 	}
 	
 	@SuppressWarnings("serial")
 	public class LoadTraceException extends Exception {
 		private String _name; 
 		public LoadTraceException(String name){ _name = name;}
 		@Override
 		public String toString(){
 			return "Error! Failed to load trace '"+_name+"'";
 		}
 	}
 	
 	public static void buildTypeClassMap() throws IOException{
 		if ( type_class_map.isEmpty() ){
			Reflections reflections = new Reflections("/\\w+\\.class");
 			for ( Class<?> klass : Reflections.getSubClasses(Trace.class, reflections.listClasses("ditl")))
 				addTraceClass(klass);
 		}
 	}
 	
 	public static void addTraceClass(Class<?> klass){
 		if ( Trace.class.isAssignableFrom(klass) ){
 			try {
 				Field f = klass.getField("type");
 				String type = (String)f.get(null);
 				type_class_map.put(type, klass);
 			} catch (Exception e) {
 				System.err.println(klass+": "+e);
 			}
 		}
 	}
 	
 	String traceFile(String name){
 		return name+separator+traceFile;
 	}
 	
 	String infoFile(String name){
 		return name+separator+infoFile;
 	}
 	
 	String snapshotsFile(String name){
 		return name+separator+snapshotsFile;
 	}
 	
 	
 	public Collection<Trace<?>> listTraces() {
 		return traces.values();
 	}
 	
 	public List<Trace<?>> listTraces(String type){
 		List<Trace<?>> list = new LinkedList<Trace<?>>();
 		for ( Trace<?> trace : traces.values() )
 			if ( trace.type() != null )
 				if ( trace.type().equals(type) )
 					list.add(trace);
 		return list;
 	}
 	
 	Reader.InputStreamOpener getStreamOpener(final String name){
 		return new Reader.InputStreamOpener(){
 			@Override
 			public InputStream open() throws IOException {
 				return getInputStream(name);
 			}
 		};
 	}
 	
 	public abstract boolean hasFile(String name);
 	
 	public boolean hasTrace(String name){
 		return traces.containsKey(name);
 	}
 	
 	public Trace<?> getTrace(String name) throws NoSuchTraceException {
 		Trace<?> trace = traces.get(name);
 		if ( trace == null ) throw new NoSuchTraceException(name);
 		return trace;
 	}
 	
 	public abstract InputStream getInputStream (String name) throws IOException;
 	
 	public String getTraceResource(Trace<?> trace, String resource) throws IOException{
 		return trace.name() + separator + resource;
 	}
 	
 	PersistentMap readTraceInfo(String path) throws IOException {
 		PersistentMap info = new PersistentMap();
 		info.read(getInputStream(infoFile(path)));
 		return info;
 	}
 	
 	public static Store open(File...files) throws IOException {
 		if ( files.length > 0 ) buildTypeClassMap();
 		switch ( files.length ){
 		case 0: return new ClassPathStore();
 		case 1: if ( files[0].isDirectory() )
 					return new DirectoryStore(files[0]);
 				return new JarStore(files[0]);
 		default: return new MultiStore(files);
 		}
 	}
 	
 	void notifyClose(Reader<?> reader){
 		openReaders.remove(reader);
 	}
 	
 	void notifyOpen(Reader<?> reader){
 		openReaders.add(reader);
 	}
 	
 	public void close() throws IOException {
 		for ( Iterator<Reader<?>> i = openReaders.iterator(); i.hasNext(); ){
 			Reader<?> reader = i.next();
 			i.remove();
 			reader.close();
 		}
 	}
 	
 	public void loadTrace(String name) throws IOException, LoadTraceException {
 		PersistentMap _info = readTraceInfo(name);
 		Trace<?> trace = buildTrace(name, _info, _info.get(Trace.typeKey));
 		traces.put(name, trace);
 	}
 	
 	Trace<?> buildTrace(String name, PersistentMap info, String type) throws LoadTraceException {
 		Class<?> klass = type_class_map.get(type);
 		if ( klass == null )
 			throw new LoadTraceException(name);
 		try {
 			Constructor<?> ctor = klass.getConstructor(new Class[]{Store.class, String.class, PersistentMap.class});
 			info.put(Trace.typeKey, type);
 			Trace<?> trace = (Trace<?>)ctor.newInstance(this, name, info);
 			return trace;
 		} catch ( Exception e ){
 			throw new LoadTraceException(name);
 		}
 	}
 }
