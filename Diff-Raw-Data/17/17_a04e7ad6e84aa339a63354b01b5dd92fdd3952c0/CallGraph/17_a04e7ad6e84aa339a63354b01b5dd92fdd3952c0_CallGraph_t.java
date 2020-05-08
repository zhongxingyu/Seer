 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import parser.Parser;
 import parser.Resolver;
 
 public class CallGraph {
 	public class MethodPercentage{
 		public MethodPercentage(Method mt, float p, String commit)
 		{
 			this.method = mt;
 			this.percentage = p;
 			this.commit_id = commit;
 		}
 		
 		public Method getMethod() {
 			return method;
 		}
 		public String getCommit_id() {
 			return commit_id;
 		}
 
 		public void setCommit_id(String commit_id) {
 			this.commit_id = commit_id;
 		}
 
 		public void setMethod(Method method) {
 			this.method = method;
 		}
 		public float getPercentage() {
 			return percentage;
 		}
 		public void setPercentage(float percentage) {
 			this.percentage = percentage;
 		}
 
 		public void addPercentage(float percentage){
 			this.percentage += percentage;
 		}
 		
 		private Method method;
 		private float percentage;
 		private String commit_id;
 	}
 	
 	private Map<String, File> 	files;
 	private Map<String, Clazz> 	clazzes;
 	private Map<String, Method> methods;
 	
 	public CallGraph() {
 		clazzes = new HashMap<String, Clazz>();
 		methods = new HashMap<String, Method>();
 		files = new HashMap<String, File>();
 	}
 	
 	public CallGraph(Map<String, Clazz> clazzes, Map<String, Method> methods,
 			Map<String, File> files) {
 		super();
 		this.clazzes = clazzes;
 		this.methods = methods;
 		this.files = files;
 	}
 	
 	public void addFile(File file) {
 		files.put(file.getFileName(), file);
 	}
 	
 	public void addClazz(Clazz clazz) {
 		clazzes.put(clazz.getName(), clazz);
 	}
 	
 	public void addMethod(Method method) {
 		methods.put(method.getName(), method);
 	}
 	
 	public File containsFile(String name) {
 		return files.get(name);
 	}
 	
 	public Clazz containsClazz(String name) {
 		return clazzes.get(name);
 	}
 	
 	public Method containsMethod(String name) {
 		return methods.get(name);
 	}
 	
 	public List<File> getAllFiles() {
 		List<File> files = new ArrayList<File>();
 		
 		for(Map.Entry<String, File> entry: this.files.entrySet()) {
 			files.add(entry.getValue());
 		}
 		
 		return files;
 	}
 	
 	public List<Clazz> getAllClazzes() {
 		List<Clazz> clazzes = new ArrayList<Clazz>();
 		
 		for(Map.Entry<String, Clazz> entry: this.clazzes.entrySet()) {
 			clazzes.add(entry.getValue());
 		}
 		
 		return clazzes;
 	}
 	
 	/**
 	 * getChangedPercentageOfMethodUsingCharacters(String fileName, int start, int end)
 	 * This function will return a list of methods and the percentage of the change that are inside the given
 	 * file name and that are involved in the start and end character locations.
 	 * @param fileName
 	 * @param start
 	 * @param end
 	 * @return List of Method and percentage
 	 */
 	public List<MethodPercentage> getPercentageOfMethodUsingCharacters(String fileName, int start, int end, String commit_id) {
 		List<MethodPercentage> m = new ArrayList<MethodPercentage>();
 		
 		File file = containsFile(fileName);
 		if(file == null)
 		{
 			return m;
 		}
 		else
 		{
 			for(Clazz clazz: file.getFileClazzes()) {
 				for(Method method: clazz.getMethods())
 				{
 					int methodStart = method.getstartChar();
 					int methodEnd 	= method.getendChar();
 					int changedPartStart = -1;
 					int changedPartEnd = -1;
 							
 					// method in lower half
 					if(method.getstartChar() >= start && method.getstartChar() <= end && method.getendChar() >= end) 
 					{
 						changedPartStart = methodStart;
 						changedPartEnd = end;
 					}// method in between
 					else if(method.getstartChar() >= start && method.getendChar() <= end)
 					{
 						changedPartStart = methodStart;
 						changedPartEnd = methodEnd;
 					}// method in upperhalf
 					else if(method.getstartChar() <= start && method.getendChar() <= end && method.getendChar() >= start)
 					{
 						changedPartStart = start;
 						changedPartEnd = methodEnd;
 					}// method contains change
 					else if(method.getstartChar() <= start   && method.getendChar() >= end) 
 					{
 						changedPartStart = start;
 						changedPartEnd = end;
 					}
 					
 					// percentage
 					if(changedPartEnd >= changedPartStart && methodEnd > methodStart && changedPartEnd != -1 && changedPartStart != -1)
 					{
 						float percent = (changedPartEnd - changedPartStart + 1)*1.000f/(methodEnd - methodStart + 1)*1.000f;
 						MethodPercentage mp = new MethodPercentage(method, percent, commit_id);
 						m.add(mp);
 					}
 				}
 			}
 		}
 		
 		return m;
 	}
 	
 	public void print() {
 		Iterator it = files.entrySet().iterator();
 		
 		while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        File currentFile = (File) pairs.getValue();
 	        currentFile.print();
 	    }
 		
 		printResolveStatistics();
 	}
 	
 	/**
 	 * This returns a list of all clazzes that are contained
 	 * inside of the given package.
 	 * @param pkg
 	 * @return
 	 */
 	public List<Clazz> getClazzesInPackage(String pkg) {
 		List<Clazz> clazzes = new ArrayList<Clazz>();
 		
 		for(Clazz clazz: getAllClazzes()) {
 			if(clazz.getFile().getFilePackage().equals(pkg))
 				clazzes.add(clazz);
 		}
 		
 		return clazzes;
 	}
 	
 	/**
 	 * This will return a list of clazzes that are inside the
 	 * imported packages.
 	 * @param imports
 	 * @return
 	 */
 	public List<Clazz> getClazzesInImports(List<String> imports) {
 		Clazz tc = null;
 		try {
 			List<Clazz> clazzes = new ArrayList<Clazz>();
 			
 			for(Clazz clazz: getAllClazzes()) {
 				tc = clazz;
 				for(String imp: imports) {
 					tc = clazz;
 					if(clazz.getName().equals(imp) || clazz.getFile().getFilePackage().equals(imp))
 						clazzes.add(clazz);
 				}
 			}
 			
 			return clazzes;
 		}
 		catch (NullPointerException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * This return a list of all files that are in the
 	 * supplied package name.
 	 * @param pkg
 	 * @return
 	 */
 	public List<File> getFilesInPackage(String pkg) {
 		List<File> files = new ArrayList<File>();
 		
 		for(File file: getAllFiles()) {
 			if(file.getFilePackage().equals(pkg))
 				files.add(file);
 		}
 		
 		return files;
 	}
 	
 	public Clazz lookupUnqualifiedClassName(Clazz clazz, String className) {
 		if(className.contains("<") && className.contains(">"))
 			className = stripGenericParameters(className);
 		// Look through the package for the class name
 		for (Clazz packageClazz : getClazzesInPackage(clazz.getFile().getFilePackage())) {
 			if(packageClazz.hasUnqualifiedName(className))
 				return packageClazz;
 		}
 		
 		// Look through the imports for the class name
 		List<Clazz> imports = getClazzesInImports(clazz.getFile().getFileImports());
 		for (Clazz s : imports) {
 			if(s.hasUnqualifiedName(className))
 				return s;
 		}
 		
 		// Check the current class for the name
 		if(clazz.hasUnqualifiedName(className))
 			return clazz;
 		
 		return null;
 	}
 	
 	public void printResolveStatistics() {
 		int resolved = 0;
 		int total = 0;
 		Iterator it = methods.entrySet().iterator();
 		
 		while (it.hasNext()) {
 	        Map.Entry pairs = (Map.Entry)it.next();
 	        Method m = (Method) pairs.getValue();
 	        if (!m.getClazz().getName().matches("java.util.*")) {
 	        	resolved += m.getMethodCalls().size();
 	        	resolved += m.getFuzzyCalls().size();
 	        	total += m.getMethodCalls().size();
 	        	total += m.getUnresolvedCalls().size();
 	        	total += m.getFuzzyCalls().size();
 	        }
 	    }
 		
 		System.out.println("TOTAL RESOLVED CALLS: " + resolved);
 		System.out.println("TOTAL CALLS:          " + total);
 		System.out.println();
 		System.out.println("ABLE TO RESOLVE: " + ((double)resolved/(double)total)*100 + "% OF ALL CALLS");
 	}
 	
 	private String stripGenericParameters(String className) {
 		return className.substring(0, className.indexOf("<"));
 	}
 	
 	/**
 	 * Given a file, this function will update the entire call graph
 	 * with the new file whether it is a completely new file or a 
 	 * replacement for an old file.
 	 * @param file
 	 */
 	public void updateCallGraphByFile(String fileName, String file) {
 		File existing = files.get(fileName);
 		if(existing == null) {
 			Parser parser = new Parser(this);
 			parser.parseFileFromString(fileName, file);
 			Resolver resolver = new Resolver(this);
 			resolver.resolveFileFull(existing);
 		}
 		else {
 			// Get a list of the conflict methods
 			List<Method> conflictMethods = getConflictingMethods(existing);
 			
 			// Get a list of the conflict classes
 			List<Clazz> conflictClazzes = getConflictingClazzes(existing);
 			
 			Resolver resolver = new Resolver(this);
			this.files.remove(fileName);
			if(file != null) {
				// Remove the old file, insert new and resolve new and conflicting
				Parser parser = new Parser(this);
				parser.parseFileFromString(fileName, file);

				File newFile = files.get(fileName);
				resolver.resolveFileFull(newFile);
			}
 			
 			for(Method method: conflictMethods) {
 				resolver.resolveMethod(method);
 			}
 			
 			for(Clazz clazz: conflictClazzes) {
 				resolver.resolveClazz(clazz);
 			}
 		}
 	}
 	
 	private List<Method> getConflictingMethods(File file) {
 		// Get a list of the conflict methods
 		List<Method> conflictMethods = new ArrayList<Method>();
 		for(Clazz clazz: file.getFileClazzes()) {
 			for(Method method: clazz.getMethods()) {
 				// Do this for called by
 				for(Method calledBy: method.getCalledBy()) {
 					// Add to conflicting methods list
 					if(!conflictMethods.contains(calledBy))
 						conflictMethods.add(calledBy);
 					// Remove the link
 					calledBy.getMethodCalls().clear();
 				}
 				// Do this for fuzzy called by
 				for(Method calledBy: method.getFuzzyCalledBy()) {
 					// Add to conflicting methods list
 					if(!conflictMethods.contains(calledBy))
 						conflictMethods.add(calledBy);
 					// Remove the link
 					calledBy.getFuzzyCalls().clear();
 				}
 				// Remove calls links
 				for(Method calls: method.getMethodCalls()) {
 					calls.getCalledBy().remove(method);
 				}
 				// Remove fuzzy calls links
 				for(Method calls: method.getFuzzyCalls()) {
 					calls.getFuzzyCalledBy().remove(method);
 				}
 			}
 		}
 		
 		return conflictMethods;
 	}
 	
 	private List<Clazz> getConflictingClazzes(File file) {
 		List<Clazz> conflictingClazzes = new ArrayList<Clazz>();
 		for(Clazz clazz: file.getFileClazzes()) {
 			for(Clazz fileClazz: file.getFileClazzes()) {
 				if(clazz.getSuperClazz().equals(fileClazz)) {
 					if(!conflictingClazzes.contains(clazz))
 						conflictingClazzes.add(clazz);
 					clazz.setSuperClazz(null);
 				}
 				if(clazz.getInterfaces().contains(fileClazz)) {
 					if(!conflictingClazzes.contains(clazz))
 						conflictingClazzes.add(clazz);
 					clazz.getInterfaces().clear();
 				}
 			}
 		}
 		
 		return conflictingClazzes;
 	}
 
 	public Map<String, Clazz> getClazzes() {
 		return clazzes;
 	}
 
 	public void setClazzes(Map<String, Clazz> clazzes) {
 		this.clazzes = clazzes;
 	}
 
 	public Map<String, Method> getMethods() {
 		return methods;
 	}
 
 	public void setMethods(Map<String, Method> methods) {
 		this.methods = methods;
 	}
 
 	public Map<String, File> getFiles() {
 		return files;
 	}
 
 	public void setFiles(Map<String, File> files) {
 		this.files = files;
 	}
 }
