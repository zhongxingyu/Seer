 package com.vgdc.merge.assets;
 
 import java.io.File;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.loaders.FileHandleResolver;
 import com.badlogic.gdx.files.FileHandle;
 
 public class DirectoryHandler implements FileHandleResolver
 {
 	
 	private FileHandle path;
 	private String extension;
 	
 	public DirectoryHandler(String directory)
 	{
 		this(directory, "");
 	}
 	
 	public DirectoryHandler(String directory, String extension)
 	{
 		this.extension = extension;
 		path = Gdx.files.internal(directory);
 		if(!path.isDirectory())
 		{
 			path = Gdx.files.internal("./bin/" + directory);
 			if(!path.isDirectory())
 			{
				path = new FileHandle(AssetsHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath());
 			}
 		}
 	}
 
 	@Override
 	public FileHandle resolve(String filename) {
 		return new FileHandle(new File(path.file(), filename + extension));
 	}
 	
 	public File getPath()
 	{
 		return path.file();
 	}
 	
 	public FileHandle[] getFilesInDirectory()
 	{
 		return path.list();
 	}
 }
