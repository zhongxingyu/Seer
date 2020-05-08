 package org.javaan.commands;
 
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.javaan.BaseCommand;
 import org.javaan.CallGraphBuilder;
 import org.javaan.ClassContextBuilder;
 import org.javaan.FilterUtil;
 import org.javaan.MethodMatcher;
 import org.javaan.SortUtil;
 import org.javaan.graph.Visitor;
 import org.javaan.model.CallGraph;
 import org.javaan.model.ClassContext;
 import org.javaan.model.Method;
 import org.javaan.model.Type;
 import org.javaan.print.GraphPrinter;
 import org.javaan.print.MethodFormatter;
 import org.javaan.print.ObjectFormatter;
 import org.javaan.print.PrintUtil;
 
 abstract class BaseCallGraphCommand extends BaseCommand {
 
 	@Override
 	public Options buildCommandLineOptions(Options options) {
 		options.addOption(StandardOptions.METHOD);
 		options.addOption(StandardOptions.LEAVES);
 		return options;
 	}
 	
 	protected abstract void traverse(CallGraph callGraph, Method method, int maxDepth, Visitor<Method> graphPrinter);
 	
 	protected abstract Set<Method> collectLeafMethods(CallGraph callGraph, Method method);
 	
 	private void printGraph(CallGraph callGraph, PrintStream output, Collection<Method> methods, ObjectFormatter<Method> formatter) {
 		Visitor<Method> printer = new GraphPrinter<Method>(output, formatter);
 		for (Method method : methods) {
 			traverse(callGraph, method, -1, printer);
 			output.println();
 			output.println("--");
 			output.println();
 		}
 	}
 	
 	private void printLeafMethods(CallGraph callGraph, PrintStream output, Collection<Method> methods, ObjectFormatter<Method> formatter) {
 		for (Method method : methods) {
			PrintUtil.println(output, SortUtil.sort(collectLeafMethods(callGraph, method)), "[M]" + method + ":", "\n\t[M]", ", ");
 		}		
 	}
 	
 	@Override
 	protected void execute(CommandLine commandLine, PrintStream output, List<Type> types) {
 		String criteria = commandLine.getOptionValue(StandardOptions.OPT_METHOD);
 		boolean printLeaves = commandLine.hasOption(StandardOptions.OPT_LEAVES);
 		ClassContext classContext = new ClassContextBuilder(types).build();
 		CallGraph callGraph = new CallGraphBuilder(classContext, types).build();
 		Collection<Method> methods = SortUtil.sort(FilterUtil.filter(classContext.getMethods(), new MethodMatcher(criteria)));
 		ObjectFormatter<Method> formatter = new MethodFormatter();
 		if (printLeaves) {
 			printLeafMethods(callGraph, output, methods, formatter);
 		} else {
 			printGraph(callGraph, output, methods, formatter);
 		}
 	}
 
 }
