 package org.spoofax.interpreter.library.language;
 
 import static org.spoofax.interpreter.core.Tools.asJavaString;
 import static org.spoofax.interpreter.core.Tools.isTermTuple;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.spoofax.interpreter.library.IOAgent;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 /**
  * A URI/time stamp container. Only one SemanticIndexFile may exist per URI.
  * 
  * @author Lennart Kats <lennart add lclnet.nl>
  */
 public class SemanticIndexFile {
 	
 	@Deprecated
 	public static final String DEFAULT_DESCRIPTOR = null;
 	
 	private final List<SemanticIndexEntry> entries = new ArrayList<SemanticIndexEntry>();
 
 	private final URI uri;
 	
 	private final String descriptor;
 	
 	private Date time;
 	
 	public URI getURI() {
 		return uri;
 	}
 	
 	public Date getTime() {
 		return time;
 	}
 	
 	public String getDescriptor() {
 		return descriptor;
 	}
 	
 	/**
 	 * Gets all entries associated with this file.
 	 * Should not be modified.
 	 */
 	public List<SemanticIndexEntry> getEntries() {
 		boolean assertionsOn = false;
 		assert assertionsOn = true;
 		if (assertionsOn)
 			return Collections.unmodifiableList(entries);
 		return entries;
 	}
 	
 	public void addEntry(SemanticIndexEntry entry) {
 		entries.add(entry);
 	}
 	
 	/**
 	 * Removes an entry using pointer-equality, taking into account
 	 * other entries that may be equal to the given entry
 	 */
 	public void removeEntry(SemanticIndexEntry entry) {
 		for (int i = 0; i < entries.size(); i++) {
 			if (entries.get(i) == entry) {
 				entries.remove(i);
 				return;
 			}
 		}
 	}
 	
 	public void setTime(Date time) {
 		this.time = time;
 	}
 	
 	public SemanticIndexFile(URI uri, String descriptor, Date time) {
 		this.uri = uri;
		this.descriptor = descriptor;
 		this.time = time;
 	}
 	
 	/**
 	 * Converts a term file representation to a SemanticIndexFile,
 	 * using the  {@link IOAgent} to create an absolute path.
 	 * 
 	 * @param agent  The agent that provides the current path and file system access,
 	 *               or null if the path should be used as-is.
 	 * @param term   A string or (string, string) tuple with the filename
 	 *               or the filename and subfilename
 	 * 
 	 * @see SemanticIndex#getFile()
 	 */
 	public static SemanticIndexFile fromTerm(IOAgent agent, IStrategoTerm term) {
 		String name;
 		String descriptor;
 		if (isTermTuple(term)) {
 			name = asJavaString(term.getSubterm(0));
 			descriptor = asJavaString(term.getSubterm(1));
 		} else {
 			name = asJavaString(term);
			descriptor = DEFAULT_DESCRIPTOR;
 		}
 		File file = new File(name);
 		if (!file.isAbsolute() && agent != null)
 			file = new File(agent.getWorkingDir(), name);
 		return new SemanticIndexFile(file.toURI(), descriptor, null);
 	}
  	
 	@Override
 	public String toString() {
 		return uri.getPath();
 	}
 	
 	public IStrategoTerm toTerm(ITermFactory factory) {
 		IStrategoString uriString = factory.makeString(toString());
 		IStrategoString descriptorName = factory.makeString(descriptor == null ? "" : descriptor);
 		return factory.makeTuple(uriString, descriptorName);
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof SemanticIndexFile) {
 			if (obj == this) return true;
 			SemanticIndexFile other = (SemanticIndexFile) obj;
 			return other.uri.equals(uri)
 					&& (descriptor == null ? other.descriptor == null : descriptor.equals(other.descriptor));
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((descriptor == null) ? 0 : descriptor.hashCode());
 		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
 		return result;
 	}
 }
