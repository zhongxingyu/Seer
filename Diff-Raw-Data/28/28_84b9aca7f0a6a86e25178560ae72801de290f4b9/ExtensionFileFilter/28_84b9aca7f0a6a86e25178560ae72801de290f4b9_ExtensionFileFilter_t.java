 package machine;
 
 import java.io.File;
 
 import javax.swing.filechooser.FileFilter;
 
 import machine.brainfuck.BrainfuckMachine;
 
 public class ExtensionFileFilter extends FileFilter {
 	public ExtensionFileFilter(String description, String extension) {
 		super();
 		this.description = description;
 		this.extension = extension;
 	}
 
 	private String description;
 	private String extension;
 
 	@Override
 	public boolean accept(File f) {
 		return f.isDirectory() || f.getName().toLowerCase().endsWith(this.extension);
 
 	}
 
 	@Override
 	public String getDescription() {
 		return this.description + " (" + this.extension + ")";
 	}
	
	public String getExtension() {
		return this.extension;
	}
 }
