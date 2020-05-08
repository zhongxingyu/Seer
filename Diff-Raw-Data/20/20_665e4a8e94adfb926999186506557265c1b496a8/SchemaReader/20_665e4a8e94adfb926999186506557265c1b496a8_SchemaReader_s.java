 package codegen;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.xml.sax.SAXException;
 
 import codegen.codetemplates.templatecompletion.TemplateFiller;
 import codegen.codetemplates.templatecompletion.replacementrule.ReplacementRoot;
 import codegen.modelobjects.DTO;
 import codegen.modelobjects.Parameter;
 import codegen.modelobjects.SMDDescriptor;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonSyntaxException;
 
 /**
  * Provides functionality for parsing SMD and Schema definition. Also can generate code given a set
  * of Replacement files and Code Template Files.
  * 
  * @author Justin Nelson
  * 
  */
 public class SchemaReader {
 
 	private SMDDescriptor smd;
 	private Map<String, DTO> dtos;
 
 	private Map<Class<?>, List<ReplacementRoot>> replacements;
 
 	/**
 	 * Creates a new schema reader with the given schema and smd url.
 	 * 
 	 * @param schemaStream
 	 *            the stream containing the schema data
 	 * @param smdStream
 	 *            the stream containing the smd data
 	 * @param replacementDirectory
 	 *            the directory where you have all of the replacement files you would like to use.
 	 *            This will look recursively.
 	 * @throws ClassNotFoundException
 	 *             if we cannot find the type that was referenced from one of the replacement files
 	 * @throws IOException
 	 *             if any file reading goes wrong
 	 */
 	public SchemaReader(InputStream schemaStream, InputStream smdStream, String replacementDirectory)
 			throws SAXException, IOException, ClassNotFoundException {
 		this.replacements = loadReplacements(replacementDirectory);
 		dtos = new HashMap<String, DTO>();
 		// create out Gson. Need to register adapters for some of our classes to work around some
 		// JSON oddities
 		GsonBuilder gb = new GsonBuilder();
 		gb.registerTypeAdapter(DTO.class, DTO.getDeSerializer());
 		gb.registerTypeAdapter(Parameter.class, Parameter.getDeSerializer());
 		Gson g = gb.create();
 		smd = g.fromJson(new InputStreamReader(smdStream), SMDDescriptor.class);
 		// using some extra JSON parsing to pull individual DAOs out of the one big DAO return
 		// object.
 		JsonParser parser = new JsonParser();
 		JsonObject obj = (JsonObject) parser.parse(new InputStreamReader(schemaStream));
 		for (Entry<String, JsonElement> entry : obj.entrySet()) {
 			dtos.put(entry.getKey(), g.fromJson(entry.getValue(), DTO.class));
 		}
 	}
 
 	/**
 	 * Given a directory, will find all of the replacement files located in that directory. It will
 	 * assume any file ending in xml is a replacement file.
 	 * 
 	 * @param replacementDirectory
 	 * @return
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	private Map<Class<?>, List<ReplacementRoot>> loadReplacements(String replacementDirectory)
 			throws IOException, ClassNotFoundException {
 		Map<Class<?>, List<ReplacementRoot>> ret = new HashMap<Class<?>, List<ReplacementRoot>>();
 		// Loop through each file in the given directory that is and xml file
 		for (File fName : listFilesRecursively(new File(replacementDirectory), new FileFilter() {
 			@Override
 			public boolean accept(File pathname) {
 				return pathname.getName().endsWith("xml");
 			}
 		})) {
 			// Turn each file into a ReplacementRoot
 			ReplacementRoot root = new ReplacementRoot(fName.getAbsolutePath());
 			List<ReplacementRoot> toAddTo;
 			// Now we sort the Replacement files by the class they represent
 			if (ret.containsKey(root.getRequiredClass())) {
 				toAddTo = ret.get(root.getRequiredClass());
 			} else {
 				toAddTo = new ArrayList<ReplacementRoot>();
 				ret.put(root.getRequiredClass(), toAddTo);
 			}
 			toAddTo.add(root);
 		}
 		return ret;
 	}
 
 	private static List<File> listFilesRecursively(File root, FileFilter filter) {
 		List<File> ret = new ArrayList<File>();
		if (root.isFile() && filter.accept(root)) {
			// if the root is a single file, just return a list containing the single file
			ret.add(root);
 			return ret;
 		} else {
 			// otherwise we will add the files in the directory if they are regular files
			File[] files = root.listFiles(filter);
 			for (File f : files) {
				if (f.isFile()) {
 					ret.add(f);
 				} else {
 					// or we will recursively add more files
 					ret.addAll(listFilesRecursively(f, filter));
 				}
 			}
 			return ret;
 		}
 	}
 
 	/**
 	 * Gets all of the model objects from a given Schema url
 	 * 
 	 * @return the Model objects for a given schema.
 	 */
 	public Map<String, DTO> getAllModelItems() {
 		return dtos;
 	}
 
 	/**
 	 * @return the object representing all of the services of a given smd
 	 */
 	public SMDDescriptor getServices() {
 		return smd;
 	}
 
 	/**
 	 * Generates code from the supplied SMD, DTOs, Replacement files, Template files, and desired
 	 * package location
 	 * 
 	 * @param packageName
 	 *            the name of the package you are generating. Can be a namespace or other grouping
 	 * @param saveLocation
 	 *            the root folder to save your files
 	 * @throws JsonIOException
 	 * @throws JsonSyntaxException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public void createPackage(String packageName, String saveLocation) throws JsonIOException,
 			JsonSyntaxException, MalformedURLException, IOException {
 		createDirectories(saveLocation);
 		// Process all replacements that go with services
 		List<ReplacementRoot> serviceReplacements = this.replacements.get(getServices().getClass());
 		for (ReplacementRoot repl : serviceReplacements) {
 			TemplateFiller filler = new TemplateFiller(repl);
 			String resolvedFileName = repl.fileName(getServices(), packageName);
 			PrintStream smdOut = new PrintStream(new File(saveLocation + File.separatorChar
 					+ resolvedFileName));
 			smdOut.println(filler.fillTemplate(getServices(), packageName, packageName + ".dto"));
 			smdOut.close();
 		}
 
 		// Process all replacements that go with each DTO
 		for (Entry<String, DTO> entry : getAllModelItems().entrySet()) {
 			List<ReplacementRoot> dtoReplacements = this.replacements.get(entry.getValue()
 					.getClass());
 			for (ReplacementRoot repl : dtoReplacements) {
 				String resolvedFileName = repl.fileName(entry.getValue(), entry.getKey(),
 						packageName);
 				TemplateFiller filler = new TemplateFiller(repl);
 				PrintStream dtoOut = new PrintStream(new File(saveLocation + File.separatorChar
 						+ "dto/" + resolvedFileName + ".java"));
 				dtoOut.println(filler.fillTemplate(entry.getValue(), entry.getKey(), packageName
 						+ ".dto"));
 				dtoOut.close();
 			}
 		}
 	}
 
 	/**
 	 * Ensures that all of the necessary directories are created to put the code in.
 	 * 
 	 * @throws IOException
 	 */
 	private static void createDirectories(String baseDirectory) throws IOException {
 		File f = new File(baseDirectory);
 		ensureDirIsGood(f);
 		String methodImplDir = baseDirectory + File.separatorChar + "impl";
 		f = new File(methodImplDir);
 		ensureDirIsGood(f);
 		String dtoDir = baseDirectory + File.separatorChar + "dto";
 		f = new File(dtoDir);
 		ensureDirIsGood(f);
 	}
 
 	/**
 	 * Ensures that a {@link File} is in the correct state (i.e. is a Directory, exists)
 	 * 
 	 * @param f
 	 * @throws IOException
 	 */
 	private static void ensureDirIsGood(File f) throws IOException {
 		// we need for to exist
 		if (f.exists()) {
 			// We need f to be a directory
 			if (!f.isDirectory()) {
 				throw new IllegalArgumentException(
 						"The given save location must be the base director to save the code: "
 								+ f.getAbsolutePath());
 			} else {
 				return;
 			}
 		}
 		// if f did not exist, we need to create f
 		if (!f.mkdirs()) {
 			throw new IOException("There was an error creating the directoriy: "
 					+ f.getAbsolutePath());
 		}
 	}
 }
