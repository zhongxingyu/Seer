 package org.meta_environment.eclipse.files;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.utils.StreamUtils;
 import org.meta_environment.eclipse.Tool;
 
 import aterm.ATerm;
 import errorapi.Factory;
 import errorapi.types.ErrorList;
 import errorapi.types.SubjectList;
 import errorapi.types.subject.Subject;
 import errorapi.types.summary.Summary;
 
 public class IOJ extends Tool {
 
 	private static class InstanceKeeper {
 		private static IOJ sInstance = new IOJ();
 		static {
 			sInstance.connect();
 		}
 	}
 
 	private Factory errorFactory = errorapi.Factory.getInstance(factory);
 
 	private IOJ() {
 		super("ioj");
 	}
 
 	static public IOJ getInstance() {
 		return InstanceKeeper.sInstance;
 	}
 
 	public ATerm readTextFile(String path) {
 
 		IFile file = ResourcesPlugin.getWorkspace().getRoot()
 				.getFileForLocation(new Path(path));
 
 		try {
 			InputStream fileContents = null;
 
 			if (file == null) {
 				/* TODO getFileContentsFromOS should be made obsolete */
 				fileContents = getFileContentsFromOS(path);
 			} else {
 				fileContents = file.getContents();
 			}
 
 			if (fileContents != null) {
 				String text = StreamUtils.readStreamContents(fileContents);
 				fileContents.close();
 				return factory.make("file-contents(<str>)", text);
			} else {
				return factory.make("failure(<term>)", getErrorSummary(
						"Could not read file", path));
 			}
 
 		} catch (Exception e) {
 			return factory.make("failure(<term>)", getErrorSummary(
 					"Could not read file", path));
 		}
 	}
 
 	public ATerm getFilename(String Directory, String Name, String Extension) {
 		IPath path = new Path(Directory);
 		path = path.append(Name + Extension);
 		String fileName = path.toString();
 		return factory.make("filename(<str>)", fileName);
 	}
 
 	public ATerm getPathFilename(String Path) {
 		IPath path = new Path(Path);
 		String fileName = path.removeFileExtension().lastSegment();
 		if (fileName == null) {
 			fileName = "";
 		}
 		System.err.println("\ngetPathFilename for " + Path + " returned: "
 				+ fileName);
 		return factory.make("filename(<str>)", fileName);
 	}
 
 	public ATerm getPathDirectory(String Path) {
 		IPath path = new Path(Path);
 		int numberOfSegments = path.segmentCount();
 
 		String directory = "";
 		if (numberOfSegments > 1) {
 			directory = path.removeLastSegments(1).toString();
 		}
 		System.err.println("\ngetPathDirectory for " + Path + " returned: "
 				+ directory);
 		return factory.make("directory(<str>)", directory);
 	}
 
 	public ATerm getPathExtension(String Path) {
 		IPath path = new Path(Path);
 		String extension = path.getFileExtension();
 
 		if (extension == null) {
 			extension = "";
 		} else {
 			extension = "." + extension;
 		}
 
 		System.err.println("\ngetPathExtension for " + Path + " returned: "
 				+ extension);
 		return factory.make("extension(<str>)", extension);
 	}
 
 	public ATerm compareFiles(String fileName1, String fileName2) {
 		File file1 = new Path(fileName1).toFile();
 		File file2 = new Path(fileName2).toFile();
 
 		if (!file1.exists()) {
 			return factory.make("failure(<trm>)", getErrorSummary(
 					"File does not exist", file1.getPath()));
 		}
 		if (!file2.exists()) {
 			return factory.make("failure(<trm>)", getErrorSummary(
 					"File does not exist", file2.getPath()));
 		}
 
 		ATerm result = factory.make("different");
 		/*
 		 * TODO I'm not 100% sure that 'equals' is the appropriate way to compare
 		 * file handles.
 		 */
 		if (file1.equals(file2)) {
 			result = factory.make("equal");
 		}
 
 		return result;
 	}
 
 	private InputStream getFileContentsFromOS(String path) {
 		File f = new File(path);
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(f);
 			System.err.println("\nTODO: legacy absolute OS path used: " + path);
 		} catch (FileNotFoundException e) {

 		}
 		return fis;
 	}
 
 	private ATerm getErrorSummary(String _description, String _subject) {
 		Subject subject = errorFactory.makeSubject_Subject(_subject);
 		SubjectList subjects = errorFactory.makeSubjectList(subject);
 		errorapi.types.error.Error error = errorFactory.makeError_Error(
 				_description, subjects);
 		ErrorList errors = errorFactory.makeErrorList(error);
 		Summary summary = errorFactory.makeSummary_Summary("IOJ tool", "ioj",
 				errors);
 		return summary.toTerm();
 	}
 
 }
