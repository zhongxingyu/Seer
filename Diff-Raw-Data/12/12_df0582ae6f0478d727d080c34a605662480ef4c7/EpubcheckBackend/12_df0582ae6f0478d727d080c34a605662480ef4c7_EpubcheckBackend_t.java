 package org.daisy.validation.epubcheck;
 
 import java.io.File;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 
 public final class EpubcheckBackend {
 
 	private static final EpubcheckBackend INSTANCE = new EpubcheckBackend();
 
 	public static List<Issue> run(String file) {
 		return INSTANCE.validate(file);
 	}
 
 	// TODO externalize jar names (to not have to rebuild after changes to
 	// epubcheck)
 	private static final String[] jars = new String[] { "epubcheck-3.0b4.jar",
 			"commons-compress-1.2.jar", "cssparser-0.9.6.jar", "jing.jar", "sac-1.3.jar",
 			"saxon9he.jar" };
 
 	// TODO get number of threads from config
 	private final ExecutorService executor = Executors.newFixedThreadPool(10);
 
 	public List<Issue> validate(String epubPath) {
 		return validate(new File(epubPath));
 	}
 
 	public List<Issue> validate(final File epubFile) {
 		Future<List<Issue>> result = executor
 				.submit(new Callable<List<Issue>>() {
 
 					@Override
 					public List<Issue> call() throws Exception {
 						return doValidate(epubFile);
 					}
 				});
 		try {
 			return result.get();
		} catch (Exception e) {
			return Lists.newArrayList(new Issue("Exception",null,"Internal Error: "+e.getMessage()));
 		}
 	}
 
 	private List<Issue> doValidate(File epub) {
 		CommandExecutor<List<Issue>> cmdExec = new CommandExecutor<List<Issue>>(
 				Lists.newArrayList(
 						"java",
 						"-cp",
 						Joiner.on(File.pathSeparatorChar).join(
 								Lists.transform(Lists.newArrayList(jars),
 										new Function<String, String>() {
 											@Override
 											public String apply(String jar) {
 												return "lib/"+jar;
 											}
 										})),
 						"com.adobe.epubcheck.tool.Checker", epub
 								.getPath()));
 		return cmdExec.run(new OutputParser(epub));
 	}
 }
