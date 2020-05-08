 package org.spoofax.interpreter.library.language;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.ref.WeakReference;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.terms.TermFactory;
 import org.spoofax.terms.attachments.TermAttachmentSerializer;
 import org.spoofax.terms.io.binary.TermReader;
 
 /**
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SemanticIndexManager {
 
 	private SemanticIndex current;
 	
 	private URI currentProject;
 	
 	private String currentLanguage;
 	
 	// (don't have access to WeakValueHashMap here, this is close enough)
 	private static Map<String, Map<URI, WeakReference<SemanticIndex>>> indicesByLanguage =
 		new HashMap<String, Map<URI, WeakReference<SemanticIndex>>>();
 	
 	public SemanticIndex getCurrent() {
 		return current;
 	}
 	
 	public void loadIndex(String language, URI project) {
 		Map<URI, WeakReference<SemanticIndex>> indicesByProject =
 			indicesByLanguage.get(language);
 		if (indicesByProject == null) {
 			indicesByProject = new HashMap<URI, WeakReference<SemanticIndex>>();
 			indicesByLanguage.put(language, indicesByProject);
 		}
 		WeakReference<SemanticIndex> indexRef = indicesByProject.get(project);
 		SemanticIndex index = indexRef == null ? null : indexRef.get();
 		if (index == null) {
 			index = readFromFile(getIndexFile(project, language));
 		}
 		if (index == null) {
 			index = new SemanticIndex();
 			indicesByProject.put(project, new WeakReference<SemanticIndex>(index));
 		}
 		current = index;
 		currentLanguage = language;
 		currentProject = project;
 	}
 	
 	public SemanticIndex readFromFile(File file) {
 		try {
 			TermFactory simpleFactory = new TermFactory();
 			IStrategoTerm term = new TermReader(simpleFactory).parseFromFile(file.toString());
 			return SemanticIndex.fromTerm(term);
 		} catch (IOException e) {
 			return null;
 		}
 	}
 	
 	public void storeCurrent() throws IOException {
 		File file = getIndexFile(currentProject, currentLanguage);
 		IStrategoTerm stored = getCurrent().toTerm();
 		TermFactory simpleFactory = new TermFactory();
 		stored = new TermAttachmentSerializer(simpleFactory).toAnnotations(stored);
 		Writer writer = new BufferedWriter(new FileWriter(file));
 		try {
 			stored.writeAsString(writer, IStrategoTerm.INFINITE);
 		} finally {
 			writer.close();
 		}
 	}
 
 	private File getIndexFile(URI project, String language) {
 		File container = new File(new File(project), ".cache");
 		container.mkdirs();
 		return new File(container, language + ".idx");
 	}
 }
