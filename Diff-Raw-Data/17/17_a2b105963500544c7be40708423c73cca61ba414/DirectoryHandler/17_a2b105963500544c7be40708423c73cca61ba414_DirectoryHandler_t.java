 package com.vgdc.merge.assets;
 
 import java.io.File;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.loaders.FileHandleResolver;
 import com.badlogic.gdx.files.FileHandle;
 
 public class DirectoryHandler implements FileHandleResolver
 {
 	
 	private FileHandle path;
	private FileHandle base;
 	private String extension;
 	
 	public DirectoryHandler(String directory)
 	{
 		this(directory, "");
 	}
 	
 	public DirectoryHandler(String directory, String extension)
 	{
 		this.extension = extension;
 		path = Gdx.files.internal(directory);
		base = Gdx.files.internal("");
 		if(!path.isDirectory())
 		{
 			path = Gdx.files.internal("./bin/" + directory);
			base = Gdx.files.internal("./bin/");
 			if(!path.isDirectory())
 			{
				base = new FileHandle(AssetsHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath());
				path = new FileHandle(AssetsHandler.class.getProtectionDomain().getCodeSource().getLocation().getPath() + directory);
 			}
 		}
		System.out.println(path.path());
 	}
 
 	@Override
 	public FileHandle resolve(String filename) {
 		return new FileHandle(new File(path.file(), filename + extension));
 	}
 	
 	public File getPath()
 	{
 		return path.file();
 	}
 	
	public File getBase()
	{
		return base.file();
	}
	
 	public FileHandle[] getFilesInDirectory()
 	{
 		return path.list();
 	}
 }
