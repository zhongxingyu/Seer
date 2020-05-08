 package net.sf.sido.gen.cli;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 
 import net.sf.sido.gen.GenerationConfiguration;
 import net.sf.sido.gen.GenerationInput;
 import net.sf.sido.gen.GenerationTool;
 import net.sf.sido.gen.model.GenerationListener;
 import net.sf.sido.gen.model.GenerationOutput;
 import net.sf.sido.gen.model.support.DirectoryGenerationOutput;
 import net.sf.sido.gen.model.support.MapOptions;
 import net.sf.sido.gen.support.FileGenerationInput;
 import net.sf.sido.gen.support.GenerationConfigurationBuilder;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 public class Main {
 
 	public static final String[] EXTENSIONS = { "sidol" };
 
 	/**
 	 * Entry point.
 	 * 
 	 * @param args
 	 *            CLI arguments
 	 */
 	public static void main(String[] args) {
 		// Options
 		Options options = readOptions(args);
 		// Summary
 		summary(options);
 		// Generation
 		try {
 			generate(options);
 		} catch (Exception e) {
 			System.err.println("Problem with the generation");
 			e.printStackTrace(System.err);
 		}
 	}
 
 	private static void log(Options options, String pattern,
 			Object... parameters) {
 		if (options.verbose) {
 			System.out.println(String.format(pattern, parameters));
 		}
 	}
 
 	private static void generate(final Options options) throws IOException, CLIException {
 
 		// Gets all SiDOL files in the source directory
 		Collection<File> sidolFiles = FileUtils.listFiles(
 				options.sourceDirectory, EXTENSIONS, true);
 		if (sidolFiles.isEmpty()) {
 			throw new CLIException("No SiDOL file has been found in %s",
 					options.sourceDirectory);
 		}
 		log(options, "List of files to include:");
 		for (File sidolFile : sidolFiles) {
 			log(options, " * %s", sidolFile.getPath());
 		}
 
 		// Inputs
 		Collection<GenerationInput> sidolInputs = Collections2.transform(
 				sidolFiles, new Function<File, GenerationInput>() {
 					@Override
 					public GenerationInput apply(File file) {
 						return new FileGenerationInput(file);
 					}
 				});
 
 		// Output
 		GenerationOutput output = new DirectoryGenerationOutput(
 				options.outputDirectory);
 
 		// Instantiates the tool
 		GenerationTool tool = new GenerationTool();
 
 		// Configuration
 		GenerationConfigurationBuilder generationBuilder = GenerationConfigurationBuilder
 				.create().modelId(options.model).sources(sidolInputs).output(output)
 				.options(new MapOptions(options.options));
 		if (options.registrationDirectory != null) {
 			generationBuilder.registrationOutput(new DirectoryGenerationOutput(options.registrationDirectory));
 		}
 		GenerationConfiguration configuration = generationBuilder.build();
 
 		// Listener
 		GenerationListener toolListener = new GenerationListener() {
 
 			@Override
 			public void log(String pattern, Object... params) {
 				Main.log(options, pattern, params);
 			}
 		};
 
 		// Generation
 		tool.generate(configuration, toolListener);
 
 	}
 
 	private static void summary(Options options) {
 		if (options.verbose) {
 			System.out.format("Model: %s%n", options.model);
 			System.out.format("Source: %s%n", options.sourceDirectory);
 			System.out.format("Output: %s%n", options.outputDirectory);
 			if (options.options.isEmpty()) {
 				System.out.format("No option%n");
 			} else {
 				System.out.format("Options:%n");
 				for (Map.Entry<String, String> entry : options.options
 						.entrySet()) {
 					System.out.format(" * %s = %s%n", entry.getKey(),
 							entry.getValue());
 				}
 			}
 		}
 	}
 
 	private static Options readOptions(String[] args) {
 		// Instance
 		Options options = new Options();
 		// Parsing
 		CmdLineParser parser = new CmdLineParser(options);
 		try {
 			parser.parseArgument(args);
 		} catch (CmdLineException e) {
 			System.err.format("Problem with the arguments:%n%s%n%s%n%n",
 					StringUtils.join(args, " "), e.getMessage());
 			System.err.println("Usage:");
 			parser.printUsage(System.err);
 			System.exit(-1);
 		}
 		// OK
 		return options;
 	}
 
 }
