 /*
 	StatCvs - CVS statistics generation 
 	Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
 	http://statcvs.sf.net/
     
 	This library is free software; you can redistribute it and/or
 	modify it under the terms of the GNU Lesser General Public
 	License as published by the Free Software Foundation; either
 	version 2.1 of the License, or (at your option) any later version.
 
 	This library is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 	Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public
 	License along with this library; if not, write to the Free Software
 	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile$
 	$Date$
 */
 package net.sf.statcvs.input;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
import java.util.TreeSet;
 import java.util.logging.Logger;
 
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsFile;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.model.SymbolicName;
 import net.sf.statcvs.util.FilePatternMatcher;
 import net.sf.statcvs.util.FileUtils;
 
 /**
  * <p>Helps building the {@link net.sf.statcvs.model.CvsContent} from a CVS
  * log. The <tt>Builder</tt> is fed by some CVS history data source, for
  * example a CVS log parser. The <tt>CvsContent</tt> can be retrieved
  * using the {@link #createCvsContent} method.</p>
  * 
  * <p>The class also takes care of the creation of <tt>Author</tt> and 
  * </tt>Directory</tt> objects and makes sure that there's only one of these
  * for each author name and path. It also provides LOC count services.</p>
  * 
  * @author Richard Cyganiak <richard@cyganiak.de>
  * @author Tammo van Lessen
  */
 public class Builder implements CvsLogBuilder {
 	private static Logger logger = Logger.getLogger(Builder.class.getName());
 
 	private final RepositoryFileManager repositoryFileManager;
 	private final FilePatternMatcher includePattern;
 	private final FilePatternMatcher excludePattern;
 
 	private final Map authors = new HashMap();
 	private final Map directories = new HashMap();
     private final Map symbolicNames = new HashMap(); 
     
 	private final List fileBuilders = new ArrayList();
 	private final Set atticFileNames = new HashSet();
 
 	private FileBuilder currentFileBuilder = null;
 	private Date startDate = null;
 	private String projectName = null;
 
 	/**
 	 * Creates a new <tt>Builder</tt>
 	 * @param repositoryFileManager the {@link RepositoryFileManager} that
 	 * 								can be used to retrieve LOC counts for
 	 * 								the files that this builder will create
 	 * @param includePattern a list of Ant-style wildcard patterns, seperated
 	 *                       by : or ;
 	 * @param excludePattern a list of Ant-style wildcard patterns, seperated
 	 *                       by : or ;
 	 */
 	public Builder(RepositoryFileManager repositoryFileManager,
 				   FilePatternMatcher includePattern,
 				   FilePatternMatcher excludePattern) {
 		this.repositoryFileManager = repositoryFileManager;
 		this.includePattern = includePattern;
 		this.excludePattern = excludePattern;
 		directories.put("", Directory.createRoot());
 	}
 
 	/**
 	 * Starts building the module.
 	 * 
 	 * @param moduleName name of the module
 	 */
 	public void buildModule(String moduleName) {
 		this.projectName = moduleName;
 	}
 
 	/**
 	 * Starts building a new file. The files are not expected to be created
 	 * in any particular order.
 	 * @param filename the file's name with path, for example "path/file.txt"
 	 * @param isBinary <tt>true</tt> if it's a binary file
 	 * @param isInAttic <tt>true</tt> if the file is dead on the main branch
      * @param revBySymnames maps revision (string) by symbolic name (string)
 	 */
 	public void buildFile(String filename, boolean isBinary, 
                            boolean isInAttic, Map revBySymnames) {
 		if (currentFileBuilder != null) {
 			fileBuilders.add(currentFileBuilder);
 		}
 		currentFileBuilder = new FileBuilder(this, filename, isBinary, 
                                              revBySymnames);
 		if (isInAttic) {
 			atticFileNames.add(filename);
 		}
 	}
 
 	/**
 	 * Adds a revision to the current file. The revisions must be added in
 	 * CVS logfile order, that is starting with the most recent one.
 	 * 
 	 * @param data the revision
 	 */
 	public void buildRevision(RevisionData data) {
 		currentFileBuilder.addRevisionData(data);
 		if (startDate == null || startDate.compareTo(data.getDate()) > 0) {
 			startDate = data.getDate();
 		}
 	}
 
 	/**
 	 * Returns a CvsContent object of all files.
 	 * 
 	 * @return CvsContent a CvsContent object
 	 * @throws EmptyRepositoryException if no adequate files were found in the
 	 * log.
 	 */
 	public CvsContent createCvsContent() throws EmptyRepositoryException {
 		if (currentFileBuilder != null) {
 			fileBuilders.add(currentFileBuilder);
 			currentFileBuilder = null;
 		}
 		if (startDate == null) {
 			throw new EmptyRepositoryException();
 		}
 
 		CvsContent result = new CvsContent();
 		Iterator it = fileBuilders.iterator();
 		while (it.hasNext()) {
 			FileBuilder fileBuilder = (FileBuilder) it.next();
 			CvsFile file = fileBuilder.createFile(startDate);
 			if (file == null) {
 				continue;
 			}
 			result.addFile(file);
 			logger.finer("adding " + file.getFilenameWithPath()
 					+ " (" + file.getRevisions().size() + " revisions)");			
 		}
 
 		if (result.isEmpty()) {
 			throw new EmptyRepositoryException();
 		}
 
 		// Uh oh...
 		SortedSet revisions = result.getRevisions();
 		List commits = new CommitListBuilder(revisions).createCommitList();
 		result.setCommits(commits);
		
		result.setSymbolicNames(new TreeSet(symbolicNames.entrySet()));
 
 		return result;
 	}
 
 	public String getProjectName() {
 		return projectName;
 	}
 
 	/**
 	 * Returns the <tt>Set</tt> of filenames that are "in the attic".
 	 * @return a <tt>Set</tt> of <tt>String</tt>s
 	 */
 	public Set getAtticFileNames() {
 		return atticFileNames;
 	}
 
 	/**
 	 * returns the <tt>Author</tt> of the given name or creates it
 	 * if it does not yet exist. 
 	 * @param name the author's name
 	 * @return a corresponding <tt>Author</tt> object
 	 */
 	public Author getAuthor(String name) {
 		if (authors.containsKey(name)) {
 			return (Author) authors.get(name);
 		}
 		Author newAuthor = new Author(name);
 		authors.put(name, newAuthor);
 		return newAuthor;
 	}
 	
 	/**
 	 * Returns the <tt>Directory</tt> of the given filename or creates it
 	 * if it does not yet exist.
 	 * @param filename the name and path of a file, for example "src/Main.java"
 	 * @return a corresponding <tt>Directory</tt> object
 	 */
 	public Directory getDirectory(String filename) {
 		int lastSlash = filename.lastIndexOf('/');
 		if (lastSlash == -1) {
 			return getDirectoryForPath("");
 		}
 		return getDirectoryForPath(filename.substring(0, lastSlash + 1));
 	}
 	
     /**
      * Returns the {@link SymbolicName} with the given name or creates it
      * if it does not yet exist.
      * 
      * @param name the symbolic name's name
      * @return the corresponding symbolic name object
      */
     public SymbolicName getSymbolicName(String name)
     {
         SymbolicName sym = (SymbolicName)symbolicNames.get(name);
         
         if (sym != null) {
             return sym;
         } 
         else {
             sym = new SymbolicName(name);
             symbolicNames.put(name, sym);
             
             return sym;            
         }
     }
     
 	public int getLOC(String filename) throws NoLineCountException {
 		if (repositoryFileManager == null) {
 			throw new NoLineCountException("no RepositoryFileManager");
 		}
 		return repositoryFileManager.getLinesOfCode(filename);
 	}
 
 	
 	/**
 	 * Matches a filename against the include and exclude patterns. If no
 	 * include pattern was specified, all files will be included. If no
 	 * exclude pattern was specified, no files will be excluded.
 	 * @param filename a filename
 	 * @return <tt>true</tt> if the filename matches one of the include
 	 *         patterns and does not match any of the exclude patterns.
 	 *         If it matches an include and an exclude pattern, <tt>false</tt>
 	 *         will be returned.
 	 */
 	public boolean matchesPatterns(String filename) {
 		if (excludePattern != null && excludePattern.matches(filename)) {
 			return false;
 		}
 		if (includePattern != null) {
 			return includePattern.matches(filename);
 		}
 		return true;
 	}
 	
 	/**
 	 * @param path for example "src/net/sf/statcvs/"
 	 * @return the <tt>Directory</tt> corresponding to <tt>statcvs</tt>
 	 */
 	private Directory getDirectoryForPath(String path) {
 		if (directories.containsKey(path)) {
 			return (Directory) directories.get(path);
 		}
 		Directory parent =
 				getDirectoryForPath(FileUtils.getParentDirectoryPath(path));
 		Directory newDirectory =
 				parent.createSubdirectory(FileUtils.getDirectoryName(path));
 		directories.put(path, newDirectory);
 		return newDirectory;
 	}
 }
