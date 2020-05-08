 package org.javaan.commands;
 
 import java.io.PrintStream;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.javaan.BaseCommand;
 import org.javaan.DuplicatesFinder;
 import org.javaan.SortUtil;
 import org.javaan.model.Type;
import org.javaan.print.ObjectFormatter;
 import org.javaan.print.PrintUtil;
import org.javaan.print.TypeInformationFormatter;
 
 public class ListDuplicates extends BaseCommand {
 	
 private final static String NAME = "duplicates";
 	
 	private final static String DESCRIPTION = "List all duplicate classes and interfaces of the libraries being loaded. "
 			+ "Classes are marked as duplications if they share canonical name, they may vary in bytecode and file location.";
 	
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	@Override
 	public String getDescription() {
 		return DESCRIPTION;
 	}
 
 	@Override
 	public Options buildCommandLineOptions(Options options) {
 		return options;
 	}
 
 	@Override
 	protected void execute(CommandLine commandLine, PrintStream output, List<Type> types) {
 		List<List<Type>> duplicates = new DuplicatesFinder(types).find();
 		SortUtil.sort(duplicates);
		ObjectFormatter<Type> formatter = new TypeInformationFormatter();
 		for (List<Type> duplicate : duplicates) {
			PrintUtil.println(output, formatter, duplicate, "", "", "\n");
 			output.println();
 		}
 	}
 
 }
