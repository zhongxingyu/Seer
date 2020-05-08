 package de.fiz.escidoc.factory.cli;
 
 import gnu.getopt.Getopt;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 
 import org.apache.commons.io.IOUtils;
 
 public class CommandlineInterface {
 	private static final String PROPERTY_VALIDITY = "properties.valid";
 	static final String PROPERTY_TARGET_DIRECTORY = "generator.target.directory";
 	private static final String PROPERTY_ARTIFACTORY_UPLOAD_URI = "generator.upload.artifatory.url";
 	private static boolean uploadJars=false;
 
 	private static void printUsage() {
 		StringBuilder helpBuilder = new StringBuilder();
 		helpBuilder.append("Escidoc objects generator\n")
 				.append("Create Escidoc XML files for infrastructure testing\n\n")
 				.append("Usage:\n")
 				.append("java -jar escidoc-objects-gen.jar OPTIONS [-p path to properties]\n\n")
 				.append("OPTIONS:\n")
 				.append("-h\tprint this help and exit\n")
 				.append("-i\tgenerate items\n")
 				.append("-c\tgenerate contexts\n")
 				.append("-m\tgenerate content models\n")
 				.append("-r\tgenerate content relations\n")
 				.append("-o\tgenerate organizational unit\n")
 				.append("-u\tupload testdata after generation to a maven repository\n\n")
 				.append("The settings will be saved after each run and can be supplied by the -p switch. If -p is ommitted the program will enter interactive mode\n");
 		System.out.println(helpBuilder.toString());
 	}
 
 	public static void main(String[] args) {
 		final Properties properties = new Properties();
 		final Getopt opt = new Getopt("Escidoc objects generator", args, "hicmroup:");
 		if (args.length == 0) {
 			printUsage();
 			return;
 		}
 		int option;
 		final List<Generator> generators = new ArrayList<Generator>();
 		while ((option = opt.getopt()) != -1) {
 			switch (option) {
 			case 'h':
 				printUsage();
 				return;
 			case 'i':
 				generators.add(new ItemGenerator(properties));
 				break;
 			case 'c':
 				generators.add(new ContextGenerator(properties));
 				break;
 			case 'm':
 				generators.add(new ContentModelGenerator(properties));
 				break;
 			case 'r':
 				generators.add(new ContentRelationGenerator(properties));
 				break;
 			case 'o':
 				generators.add(new OrganizationalUnitGenerator(properties));
 				break;
 			case 'u':
 				uploadJars=true;
 				break;
 			case 'p':
 				String path = opt.getOptarg();
 				try {
 					properties.load(new FileInputStream(path));
 					properties.setProperty(PROPERTY_VALIDITY, "true");
 				} catch (IOException e) {
 					System.err.println("Unable to load properties file from " + path);
 					return;
 				}
 				break;
 			default:
 				printUsage();
 				return;
 			}
 		}
 		// get the settings for the generators through an interactive user
 		// session if no properties have been set already
 		createSettings(properties, generators);
 		// store the properties for convenience
 		storeProperties(properties);
 		// generate the XMLs
 		generateXMLFiles(generators);
 		// finally upload it to the artifactory
 		if (uploadJars){
 			try {
 				uploadJar(properties, createJar(properties.getProperty(PROPERTY_TARGET_DIRECTORY)));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("\nFinished!\n");
 	}
 
 	private static void uploadJar(Properties properties, File jarFile) throws IOException{
		String cli = "mvn deploy:deploy-file -DrepositoryId=snapshots -Durl=" + properties.getProperty(PROPERTY_ARTIFACTORY_UPLOAD_URI) + " -DgroupId=org.escidoc.core.performance -DartifactId=testdaten -Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dfile=" + jarFile.getAbsolutePath();
 		Process proc=Runtime.getRuntime().exec(cli);
 		byte[] buf=new byte[1024];
 		int bytesRead;
 		while ((bytesRead=proc.getInputStream().read(buf))!=-1){
 			System.out.println(new String(buf,0,bytesRead));
 		}
 	}
 
 	private static void generateXMLFiles(List<Generator> generators) {
 		System.out.println("\nGenerating xml files...");
 		for (final Generator gen : generators) {
 			try {
 				System.out.println(":: running generator " + gen.getClass().getSimpleName());
 				gen.generateFiles();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private static void storeProperties(Properties properties) {
 		System.out.println();
 		try {
 			final File propFile = new File("generator.properties");
 			properties.store(new FileOutputStream(propFile), "created by escidoc-object-generator");
 			System.out.println("saved properties to " + propFile.getAbsolutePath());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void createSettings(Properties properties, List<Generator> generators) {
 		if (Boolean.parseBoolean(properties.getProperty(PROPERTY_VALIDITY))) {
 			return;
 		}
 		System.out.println();
 		File targetDirectory = null;
 		do {
 			Questionary q = new Questionary(new BufferedReader(new InputStreamReader(System.in)), System.out);
 			try {
 				targetDirectory = q.poseQuestion(File.class, new File(System.getProperty("java.io.tmpdir")
 						+ "/escidoc-test"), "Where should the xml files be written to [default="
 						+ System.getProperty("java.io.tmpdir") + "/escidoc-test] ?");
 				if (!targetDirectory.exists()) {
 					if (q.poseQuestion(Boolean.class, true, "Create directory " + targetDirectory.getAbsolutePath()
 							+ " [default=yes] ?")) {
 						targetDirectory.mkdir();
 					}
 				}
 				if (uploadJars){
 					properties.setProperty(PROPERTY_ARTIFACTORY_UPLOAD_URI, q.poseQuestion(String.class, "https://www.escidoc.org/artifactory/libs-snapshots-local/", "What's the upload URI of the target repository [default=https://www.escidoc.org/artifactory/libs-snapshots-local] ?"));
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} while (!targetDirectory.exists() && !targetDirectory.canWrite());
 		properties.setProperty(PROPERTY_TARGET_DIRECTORY, targetDirectory.getAbsolutePath());
 		for (final Generator gen : generators) {
 			System.out.println(":: Settings for " + gen.getClass().getSimpleName());
 			gen.interactive();
 		}
 	}
 
 	private static File createJar(String dir) {
 		JarOutputStream out = null;
 		InputStream in = null;
 		File directory = new File(dir);
 		File jarFile = new File(directory, "testdata.jar");
 		try {
 			out = new JarOutputStream(new FileOutputStream(jarFile));
 			for (String name : directory.list()) {
 				if (name.endsWith(".xml") || name.endsWith(".csv") || name.endsWith(".content")) {
 					JarEntry entry = new JarEntry(name);
 					out.putNextEntry(entry);
 					in = new FileInputStream(directory.getAbsolutePath() + "/" + name);
 					IOUtils.copy(in, out);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			IOUtils.closeQuietly(in);
 			IOUtils.closeQuietly(out);
 		}
 		return jarFile;
 	}
 }
