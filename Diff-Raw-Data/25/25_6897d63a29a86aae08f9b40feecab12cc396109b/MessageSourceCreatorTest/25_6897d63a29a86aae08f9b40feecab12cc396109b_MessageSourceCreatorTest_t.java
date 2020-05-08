 package org.dejava.component.i18n.test.source;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaCompiler.CompilationTask;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import org.dejava.component.i18n.message.annotation.MessageBundle;
 import org.dejava.component.i18n.message.annotation.MessageBundles;
 import org.dejava.component.i18n.message.handler.impl.DefaultI18nMessageHandler;
 import org.dejava.component.i18n.source.MessageSourceCreator;
 import org.dejava.component.i18n.source.annotation.MessageSources;
 import org.dejava.component.i18n.test.source.auxiliary.InformationKeys;
 import org.dejava.component.i18n.test.source.auxiliary.SampleEntryProvider;
 import org.dejava.component.reflection.ClassMirror;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 /**
  * TODO
  */
@MessageBundles(defaultType = "information", messageBundles = { @MessageBundle(type = "information", baseName = "org.dejava.component.i18n.test.source.properties.information") })
 public class MessageSourceCreatorTest {
 	
 	/**
 	 * Makes sure that the folders created for the test are excluded in the end.
 	 */
 	@Rule
 	public TemporaryFolder temporaryFolder = new TemporaryFolder();
 	
 	/**
 	 * Compiles a file using the message source creator annotation processor.
 	 * 
 	 * @param sourceFilePath
 	 *            The path for the source file to be compiled.
 	 * @throws IOException
 	 *             TODO
 	 */
 	public void compile(final String sourceFilePath) throws IOException {
 		// Gets an instance of Java compiler.
 		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 		// Get as new instance of the standard file manager implementation.
 		final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
 		// Gets the file to be compiled.
 		final Iterable<? extends JavaFileObject> compilationUnits = fileManager
 				.getJavaFileObjects(sourceFilePath);
 		// Creates the compilation task.
 		final CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
 		// Creates a list to hold annotation processors.
 		final List<AbstractProcessor> processors = new LinkedList<AbstractProcessor>();
 		// Adds an annotation processor to the list.
 		processors.add(new MessageSourceCreator());
 		// Sets the annotation processor to the compiler task.
 		task.setProcessors(processors);
 		// Performs the compilation task.
 		task.call();
 		// Flushes the file manager.
 		fileManager.flush();
 		// Closes the file manager.
 		fileManager.close();
 	}
 	
 	/**
 	 * TODO
 	 * 
 	 * @throws IOException
 	 *             TODO
 	 */
 	@Test
 	public void test() throws IOException {
 		// Compiles the file.
 		compile("src/test/java/org/dejava/component/i18n/test/source/auxiliary/InformationKeys.java");
 		// Gets the message source annotation.
 		final MessageSources messageSources = new ClassMirror<InformationKeys>(InformationKeys.class)
 				.getAnnotation(MessageSources.class).getReflectedAnnotation();
 		// For each defined locale.
 		for (final String currentLocaleConfig : messageSources.sources()[0].availableLocales()) {
 			// For each message key.
 			for (final String currentKey : SampleEntryProvider.KEYS) {
 				// Splits the locale configuration.
 				final String[] currentLocaleParams = currentLocaleConfig.split("_");
 				// Current locale.
 				Locale currentLocale = Locale.getDefault();
 				// Depending on the number of parameters.
 				switch (currentLocaleParams.length) {
 				// If there is one parameter.
 					case 1:
 						// Gets the locale for the current configuration.
 						currentLocale = new Locale(currentLocaleParams[0]);
 						// Ends the case.
 						break;
 					// If there are two parameter.
 					case 2:
 						// Gets the locale for the current configuration.
 						currentLocale = new Locale(currentLocaleParams[0], currentLocaleParams[1]);
 						// Ends the case.
 						break;
 					// If there are three parameter.
 					case 3:
 						// Gets the locale for the current configuration.
 						currentLocale = new Locale(currentLocaleParams[0], currentLocaleParams[1],
 								currentLocaleParams[2]);
 						// Ends the case.
 						break;
 				}
 				// Tries to get the message for the key.
 				try {
 					DefaultI18nMessageHandler.getMessageHandler(currentLocale).getMessage(currentKey, null);
 				}
 				// If the message cannot be found.
 				catch (final Exception exception) {
 					// TODO: handle exception
 					Assert.fail();
 				}
 			}
 		}
 	}
 }
