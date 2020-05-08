 package subobjectjava.eclipse;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import jnome.core.language.Java;
 import jnome.output.JavaCodeWriter;
 
 import subobjectjava.input.SubobjectJavaModelFactory;
 import subobjectjava.model.language.SubobjectJava;
 import chameleon.core.language.Language;
 import chameleon.core.namespace.RootNamespace;
 import chameleon.editor.LanguageMgt;
 import chameleon.editor.connector.Builder;
 import chameleon.editor.connector.EclipseBootstrapper;
 import chameleon.editor.connector.EclipseEditorExtension;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.input.ModelFactory;
 import chameleon.input.ParseException;
 import chameleon.output.Syntax;
 
 
 public class Bootstrapper extends EclipseBootstrapper {
 	
 	public final static String PLUGIN_ID="be.chameleon.eclipse.jlow";
 	
 	public void registerFileExtensions() {
		addExtension("java");
 		addExtension("jlow");
 	}
 	
 	public String getLanguageName() {
 		return "JLow";
 	}
 
 	public String getLanguageVersion() {
 		return "1.0";
 	}
 
 	public String getVersion() {
 		return "1.0";
 	}
 
 	public String getDescription() {
 		return "Java with subobjects";
 	}
 	
 	public String getLicense() {
 		return "";
 	}
 
 	public Syntax getCodeWriter() {
 		return null;
 	}
 
 	public Language createLanguage() throws IOException, ParseException {
 		SubobjectJava result = new SubobjectJava();
 		ModelFactory factory = new SubobjectJavaModelFactory(result);
 		factory.setLanguage(result, ModelFactory.class);
 		try {
			FilenameFilter filter = LanguageMgt.fileNameFilter(".java");
 			URL directory = LanguageMgt.pluginURL(PLUGIN_ID, "api/");
 			List<File> files = LanguageMgt.allFiles(directory, filter);
 		  factory.initializeBase(files);
 		} catch(ChameleonProgrammerException exc) {
 			// Object and String may not be present yet.
 		}
 		result.setConnector(EclipseEditorExtension.class, new SubobjectJavaEditorExtension());
 		return result;
 	}
 	
 	public Builder createBuilder(Language source, File projectDirectory) {
 //		RootNamespace clone = source.defaultNamespace().clone();
 		Java result = new Java();
 //		result.cloneConnectorsFrom(source);
 //		result.cloneProcessorsFrom(source);
 //		result.setDefaultNamespace(clone);
 		result.setConnector(Syntax.class, new JavaCodeWriter());
 		File outputDirectory = new File(projectDirectory.getAbsolutePath()+File.separator+"java");
 		return new JLowBuilder((SubobjectJava) source, result, outputDirectory);
 	}
 
 }
