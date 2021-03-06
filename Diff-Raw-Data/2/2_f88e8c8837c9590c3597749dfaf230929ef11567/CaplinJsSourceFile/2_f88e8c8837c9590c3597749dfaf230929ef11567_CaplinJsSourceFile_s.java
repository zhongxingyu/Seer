 package org.bladerunnerjs.core.plugin.bundlesource.js;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bladerunnerjs.model.AliasDefinition;
 import org.bladerunnerjs.model.FullyQualifiedLinkedAssetFile;
 import org.bladerunnerjs.model.LinkedAssetFile;
 import org.bladerunnerjs.model.Resources;
 import org.bladerunnerjs.model.SourceFile;
 import org.bladerunnerjs.model.SourceLocation;
 import org.bladerunnerjs.model.exception.ModelOperationException;
 
 public class CaplinJsSourceFile implements SourceFile {
 	private LinkedAssetFile assetFile;
 	private SourceLocation sourceLocation;
 	private String requirePath;
 	private File srcDir;
 	
 	public CaplinJsSourceFile(SourceLocation sourceLocation, File file) {
 		this.sourceLocation = sourceLocation;
 		this.requirePath = sourceLocation.file("src").toURI().relativize(file.toURI()).getPath().replaceAll("\\.js$", "");
 		assetFile = new FullyQualifiedLinkedAssetFile(sourceLocation, file);
 		srcDir = file.getParentFile();
 	}
 	
 	@Override
	public List<SourceFile> getDependentSourceFiles() throws ModelOperationException {
 		List<SourceFile> dependentSourceFiles = assetFile.getDependentSourceFiles();
 		dependentSourceFiles.removeAll(getOrderDependentSourceFiles());
 		dependentSourceFiles.remove(this);
 		
 		return dependentSourceFiles;
 	}
 	
 	@Override
 	public List<AliasDefinition> getAliases() throws ModelOperationException {
 		return assetFile.getAliases();
 	}
 	
 	@Override
 	public Reader getReader() throws FileNotFoundException {
 		return assetFile.getReader();
 	}
 	
 	@Override
 	public String getRequirePath() {
 		return requirePath;
 	}
 	
 	@Override
 	public List<Resources> getResources() {
 		return sourceLocation.getResources(srcDir);
 	}
 	
 	@Override
 	public List<SourceFile> getOrderDependentSourceFiles() throws ModelOperationException {
 		// TODO: scan the source file for caplin.extend(), caplin.implement(), br.extend() & br.implement()
 		return new ArrayList<>();
 	}
 	
 	public String getClassName() {
 		return getRequirePath().replaceAll("/", ".");
 	}
 	
 	@Override
 	public File getUnderlyingFile() {
 		return assetFile.getUnderlyingFile();
 	}
 }
