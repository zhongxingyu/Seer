 package org.data2semantics.modules;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.OutputStreamWriter;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.commons.cli.Options;
 import org.apache.commons.io.IOUtils;
 import org.data2semantics.recognize.D2S_OpenAnnotationWriter;
 import org.data2semantics.util.RepositoryWriter;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.sail.memory.MemoryStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ModuleWrapper {
 
 	private static Logger log = LoggerFactory.getLogger(ModuleWrapper.class);
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		if (args.length < 3) {
 			System.out
 					.println("\nModuleWrapper\n" +
 							"Please use the following arguments: \n" +
 							"[1] - The Java Class that is your module (use the full package path)" +
 							"[2] - RDF file containing the data your module must run on.\n" +
 							"[3] - The named graph URI in that file that contains the actual data (use 'default' for no graph).\n" +
 							"[4] - The URI of a specific resource in the file."
 							);
 			return;
 		}
 		
 		String moduleName = args[0];
 		String fileName = args[1];
 		String graph = args[2];
 		String resource = args[3];
 		
 	    
 
 	    try {
 	    	AbstractModule module = constructModule(moduleName, fileName,
 					graph, resource);
 	        
 	        log.info("Starting module");
 	        Repository outputRepository = module.start();
 	        log.info("Module run completed");
 	        
 	        // TODO Add provenance information about ModuleWrapper run
 	        
	        String outputFileName = "output.n3";
	        		
			try {
		        log.info("Starting RepositoryWriter (writing to output.n3)");
		        		
				FileOutputStream outputStream = new FileOutputStream(new File(outputFileName));
				OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
		        RepositoryWriter rw = new RepositoryWriter(outputRepository, streamWriter);
		        
		        rw.write();
		        log.info("Done");

			} catch (FileNotFoundException e) {
				log.error("Failed to create output file " + outputFileName);
			}

 	        
 
 	        
 	    } catch (ClassNotFoundException e) {
 	    	// TODO Auto-generated catch block
 	        e.printStackTrace();
 	    } catch (RepositoryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RDFParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static AbstractModule constructModule(String moduleName,
 			String fileName, String graph, String resource)
 			throws ClassNotFoundException, RepositoryException, IOException,
 			RDFParseException, NoSuchMethodException, InstantiationException,
 			IllegalAccessException, InvocationTargetException {
 		ClassLoader classLoader = ModuleWrapper.class.getClassLoader();
 		
 		Class<?> moduleClass = classLoader.loadClass(moduleName);
 		log.info("Loaded module: " + moduleClass.getName());
 
 		Repository inputRepository = new SailRepository(new MemoryStore());
 		inputRepository.initialize();
 		log.info("Initialized repository");
 
 		ValueFactory vf = inputRepository.getValueFactory();
 		URI graphURI = vf.createURI(graph);
 		URI resourceURI = vf.createURI(resource);
 		
 		File file = new File(fileName);
 		log.info("Loading RDF in N3 format from "+ fileName);
 		RepositoryConnection con;
 		con = inputRepository.getConnection();
 		
 		con.add(file, "http://foo/bar#", RDFFormat.N3, graphURI);
 		log.info("Done loading");
 		con.close();
 		
 		log.info("Calling constructor of module "+moduleName);
 		
 		Constructor<?> moduleConstructor = moduleClass.getDeclaredConstructor(Repository.class, URI.class, URI.class);
 		
 //	        Constructor moduleConstructor = ModuleWrapper.class.getDeclaredConstructor(moduleClass);
 		AbstractModule module = (AbstractModule) moduleConstructor.newInstance(inputRepository, graphURI, resourceURI);
 		
 		log.info("Module constructed");
 		return module;
 	}
 
 }
