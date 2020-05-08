 package org.vfsutils.shell.commands;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileType;
 import org.vfsutils.shell.Arguments;
 import org.vfsutils.shell.CommandInfo;
 import org.vfsutils.shell.CommandProvider;
 import org.vfsutils.shell.Engine;
 
 public class Ls extends AbstractCommand implements CommandProvider {
 
 	private String longListType = "nix";
 	//Note that DateFormat.format is not threadsafe and so we can't store an instance
	private String dateFormat = "dd/MM/yyyy HH:mm";
 	
 	
 	public Ls() {
 		super("ls", new CommandInfo("Directory listing", "[-l] [<path>]"));
 	}
 	
 	public void execute(Arguments args, Engine engine)
 			throws IllegalArgumentException, FileSystemException {
 
 		FileObject file = null;
 		FileObject[] files;
 		boolean showRelativePath = false;
 
 		if (args.size() > 0) {
 			String path = args.getArgument(0);
 			if (path.indexOf('*') > -1) {
 				file = engine.getCwd();
 				files = engine.pathToFiles(path);
 				showRelativePath = true;
 			} else {
 				file = engine.pathToFile(path);
 				// can throw a FileNotFolderException
 				files = file.getChildren();
 			}
 		} else {
 			file = engine.getCwd();
 			// can throw a FileNotFolderException
 			files = file.getChildren();
 		}
 
 		// List the contents
 		listChildren(file, files, showRelativePath, args, engine);
 
 	}
 
 	/**
 	 * Lists the given files
 	 */
 	private void listChildren(final FileObject dir, final FileObject[] files,
 			boolean showRelativePath, Arguments args, Engine engine) throws FileSystemException {
 
 		engine.println("Contents of " + engine.toString(dir));
 
 		boolean longList = args.hasFlag("l");
 				
 		int nrOfFiles = 0;
 		int nrOfFolders = 0;
 		for (int i = 0; i < files.length; i++) {
 			final FileObject child = files[i];
 			
 			boolean isFolder = (child.getType() == FileType.FOLDER);
 			
 			if (longList && longListType!= null && longListType.equals("dos")) {
 				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
 				listChildDosStyle(dir, child, isFolder, showRelativePath, dateFormatter, engine);
 			}
 			else if (longList) {
 				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
 				listChildNixStyle(dir, child, isFolder, showRelativePath, dateFormatter, engine);
 			}
 			else {				
 				listChild(dir, child, isFolder, showRelativePath, engine);
 			}
 		
 			if (isFolder) {
 				nrOfFolders++;
 			} else {
 				nrOfFiles++;
 			}
 		}
 		engine.println(nrOfFolders + " Folder(s), " + nrOfFiles + " File(s)");
 	}
 
 	protected void listChild(final FileObject base, final FileObject child, boolean isFolder,
 			boolean showRelativePath, Engine engine) throws FileSystemException {
 
 		if (showRelativePath) {
 			engine.print(base.getName().getRelativeName(child.getName()));
 		} else {
 			engine.print(child.getName().getBaseName());
 		}
 		if (isFolder) {
 			engine.println("/");
 		} else {
 			engine.println("");
 		}
 
 	}
 
 	protected void listChildDosStyle(final FileObject base, final FileObject child,
 			boolean isFolder, boolean showRelativePath, DateFormat dateFormatter, 
 			Engine engine) throws FileSystemException {
 
 		long timestamp = child.getContent().getLastModifiedTime();
 		Date date = new Date(timestamp);
 		
 		String middle = "                 ";
 		if (isFolder) {
 			middle = "   <DIR>         ";
 		}
 		else {
 			long size = child.getContent().getSize();
 			String sizeAsString = Long.toString(size);
 			String formattedSize;
 			if (sizeAsString.length()<=3) {
 				formattedSize = sizeAsString;
 			}
 			else {
 				int start = sizeAsString.length() % 3;
 				if (start==0) start=3;
 				formattedSize = sizeAsString.substring(0,start);
 			 			
 				for (int i=start; i<sizeAsString.length(); i=i+3) {
 					formattedSize = formattedSize + "." + sizeAsString.substring(i, i+3);
 				}
 			}
 			middle = middle.substring(0, middle.length() - formattedSize.length()) + formattedSize;
 		}
 				
 		engine.print(dateFormatter.format(date) + middle + " "); 
 		
 		if (showRelativePath) {
 			engine.println(base.getName().getRelativeName(child.getName()));
 		} else {
 			engine.println(child.getName().getBaseName());
 		}		
 		
 	}
 	
 	protected void listChildNixStyle(final FileObject base, final FileObject child,
 			boolean isFolder, boolean showRelativePath, DateFormat dateFormatter, 
 			Engine engine) throws FileSystemException {
 
 		String start;
 		
 		if (isFolder) {
 			start="d";
 		} else {
 			start="-";
 		}
 		
 		if (child.isWriteable()) start += "w";
 		else start += "-";
 		
 		if (child.isReadable()) start += "r";
 		else start += "-";
 		
 		long timestamp = child.getContent().getLastModifiedTime();
 		Date date = new Date(timestamp);
 		
 		String middle = "            ";
 		if (!isFolder) {
 			long size = child.getContent().getSize();
 			String sizeAsString = Long.toString(size);
 			middle = middle.substring(0, middle.length() - sizeAsString.length()) + sizeAsString;
 		}
 			
 		engine.print(start += middle + " " + dateFormatter.format(date) + " ");
 		
 		if (showRelativePath) {
 			engine.println(base.getName().getRelativeName(child.getName()));
 		} else {
 			engine.println(child.getName().getBaseName());
 		}		
 	}
 
 	/**
 	 * Set the format the long listing should use. 
 	 * @param longListType dos for MS-DOS style, nix for Unix/Linux style
 	 */
 	public void setLongListType(String longListType) {
 		this.longListType = longListType;
 	}
 	
 	public String getLongListType() {
 		return this.longListType;
 	}
 
 	public String getDateFormat() {
 		return dateFormat;
 	}
 
 	public void setDateFormat(String dateFormat) {
 		this.dateFormat = dateFormat;
 	}
 
 	
 	
 }
