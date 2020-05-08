 package com.github.seqware.queryengine.system.exporters;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.MissingOptionException;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 
 import com.github.seqware.queryengine.model.Reference;
 import com.github.seqware.queryengine.plugins.PluginInterface;
 import com.github.seqware.queryengine.plugins.contribs.MutationsToDonorsAggregationPlugin;
 import com.github.seqware.queryengine.system.Utility;
 import com.github.seqware.queryengine.system.importers.FeatureImporter;
 import com.github.seqware.queryengine.system.importers.SOFeatureImporter;
 import com.github.seqware.queryengine.util.SGID;
 import com.github.seqware.queryengine.factory.SWQEFactory;
 
 public class ArbitraryPluginRunner {
 
     /** Constant <code>OUTPUT_FILE_PARAM='o'</code> */
     public static final char OUTPUT_FILE_PARAM = 'o';
     /** Constant <code>REFERENCE_ID_PARAM='r'</code> */
     public static final char REFERENCE_ID_PARAM = 'r';
     /** Constant <code>PLUGIN_CLASS_PARAM='p'</code>*/
     public static final char PLUGIN_CLASS_PARAM = 'p';
     /** Constant <code>SUCCESS</code>*/
     private static final int SUCCESS = 1;
     /** Constant <code>FAILIURE</code>*/
     private static final int FAILIURE = 1;
     
 	private String[] args;
 	
 	public static void main(String[] args) {
 		int mainMethod = ArbitraryPluginRunner.runArbitraryPluginRunner(args);
         if (mainMethod == FAILIURE) {
             System.exit(FeatureImporter.EXIT_CODE_INVALID_FILE);
         }
 	}
 
 	public static int runArbitraryPluginRunner(String[] args){
 
 		Options options = new Options();
 		Option option1 = OptionBuilder.withArgName("outputFile").withDescription("(required) output file").hasArgs(1).isRequired().create(OUTPUT_FILE_PARAM);
 		options.addOption(option1);
 		Option option2 = OptionBuilder.withArgName("reference").withDescription("(required) the reference ID of the FeatureSet to run plugin on").hasArgs(1).isRequired().create(REFERENCE_ID_PARAM);
 		options.addOption(option2);
 		Option option3 = OptionBuilder.withArgName("pluginClass").withDescription("(required) the plugin to be run, full package path").hasArgs(1).isRequired().create(PLUGIN_CLASS_PARAM);
 		options.addOption(option3);
 		
 		try{			
 			CommandLineParser parser = new PosixParser();
 			CommandLine cmd = parser.parse(options, args);
 			String referenceName = cmd.getOptionValue(REFERENCE_ID_PARAM);
			String plugin = cmd.getOptionValue(REFERENCE_ID_PARAM);
 			String outputFile = cmd.getOptionValue(OUTPUT_FILE_PARAM);
 			Class<? extends PluginInterface> arbitraryPluginClass;
 
 			Reference ref = null;
 			for (Reference r : SWQEFactory.getQueryInterface().getReferences()){
 				if (referenceName.equals(r.getName())){
 					ref = r;
 					break;
 				}
 			}
 
 			arbitraryPluginClass = (Class<? extends PluginInterface>) Class.forName(plugin);
 	        long start = new Date().getTime();
 			System.out.println("Running plugin: " + plugin);
 			Utility.dumpFromMapReducePlugin(plugin, ref, null, arbitraryPluginClass, outputFile);
 	        long stop = new Date().getTime();
 	        float diff = ((stop - start) / 1000) / 60;
 	        System.out.println("Minutes to query: "+diff);
 			
 			if (ref == null){
 				System.out.println("Reference was not found.");
 				System.exit(-2);
 			}
 			
 			return SUCCESS;
 		} catch (MissingOptionException e) {
             // automatically generate the help statement
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(SOFeatureImporter.class.getSimpleName(), options);
             System.exit(FeatureImporter.EXIT_CODE_INVALID_ARGS);
         } catch (ParseException e) {
             // automatically generate the help statement
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(SOFeatureImporter.class.getSimpleName(), options);
             System.exit(FeatureImporter.EXIT_CODE_INVALID_ARGS);
         } catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 		}
         return FAILIURE;
 	}
 }
