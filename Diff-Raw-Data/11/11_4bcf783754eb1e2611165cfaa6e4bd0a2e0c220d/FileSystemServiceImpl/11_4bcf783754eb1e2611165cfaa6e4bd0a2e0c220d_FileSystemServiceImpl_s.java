 package org.utn.proyecto.helpful.integrart.integrar_t_android.services;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.Resource;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.ResourceType;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.User;
 
 import roboguice.inject.ContextSingleton;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.media.MediaPlayer;
 import android.util.Log;
 
 import com.google.inject.Inject;
 
 @ContextSingleton
 public class FileSystemServiceImpl implements FileSystemService {
 	private final static String MAIN_PACKAGE = "main";
 	
 	private Context context;
 	private final FileSystemDataStorage db;
 	private final DataStorageService dbService;
 	private final String userName;
 		
 	@Inject
 	public FileSystemServiceImpl(Context context, DataStorageService db, User user){
 		this.context = context;
 		this.dbService = db;
 		this.db = new FileSystemDataStorage(db);
 		this.userName = user.getUserName();
 	}
 	
 	@Override
 	public <T> Resource<T> getResource(String activityName,
 			String resourceName) {
 		return getResource(activityName, MAIN_PACKAGE,resourceName);
 	}
 	
 	@Override
 	public <T> Resource<T> getResource(String activityName,
 			String packageName, String resourceName) {
 		Resource<?>[] resources = db.getResources(userName, activityName, packageName);
 		Resource<?> resource = findByName(resourceName, resources);
 		return buildResourceByType(activityName, packageName, resource);
 	}
 	
 	private Resource<?> findByName(String name, Resource<?>[] resources){
 		for(Resource<?> resource : resources){
 			if(resource.getName().equals(name))
 				return resource;
 		}
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected <T> Resource<T> buildResourceByType(String activityName,
 		String packageName, Resource<?> resource){
 		if(resource.getType() == ResourceType.IMAGE)
 			return (Resource<T>)buildImageResource(activityName, packageName, (Resource<Drawable>)resource);
 		if(resource.getType() == ResourceType.SOUND)
 			return (Resource<T>)buildSoundResource(activityName, packageName, (Resource<MediaPlayer>)resource);
 		return null;
 	}
 	
 	private FileInputStream buildResourceInputStream(String activityName,
 		String packageName, Resource<?> resource){
 		String path = getFullPath(activityName, packageName) + "." + resource.getName();
 		FileInputStream io = null;
 		try {
 			io = context.openFileInput(path);
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 		return io;
 	}
 	
 	protected Resource<Drawable> buildImageResource(String activityName,
 			String packageName, Resource<Drawable> resource){
 		InputStream io = buildResourceInputStream(activityName, packageName, resource);
 		Drawable d = Drawable.createFromStream(io, "src");
 		resource.setResource(d);
 		return resource;
 	}
 
 	protected Resource<MediaPlayer> buildSoundResource(String activityName,
 			String packageName, Resource<MediaPlayer> resource){
 		FileInputStream io = buildResourceInputStream(activityName, packageName, resource);
 		MediaPlayer sound = new MediaPlayer();
 		try {
 			sound.setDataSource(io.getFD());
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} 
 		resource.setResource(sound);
 		return resource;
 	}
 	
 	protected Resource<?>[] buildResources(String activityName,
 			String packageName, Resource<?>[] resources){
 		for(Resource<?> resource : resources){
 			buildResourceByType(activityName, packageName, resource);
 		}
 		return resources;
 	}
 	
 	protected Resource<?>[] buildResourcesByType(String activityName,
 			String packageName, ResourceType type, Resource<?>[] resources){
 		List<Resource<?>> list = new ArrayList<Resource<?>>();
 		for(Resource<?> resource : resources){
 			if(resource.getType().equals(type)){
 				buildResourceByType(activityName, packageName, resource);
 				list.add(resource);
 			}
 		}
 		return list.toArray(new Resource[0]);
 	}
 
 	@Override
 	public Resource<?>[] getResources(String activityName,
 			String packageName) {
 		Resource<?>[] resources = db.getResources(userName, activityName, packageName);
 		return buildResources(activityName, packageName, resources);
 	}
 
 	@Override
 	public Resource<?>[] getResources(String activityName,
 			String packageName, ResourceType type) {
 		Resource<?>[] resources = db.getResources(userName, activityName, packageName);
 		return buildResourcesByType(activityName, packageName, type, resources);
 	}
 
 	@Override
 	public Resource<?>[] getResources(String activityName) {
 		Resource<?>[] resources = db.getResources(userName, activityName);
 		return buildResources(activityName, MAIN_PACKAGE, resources);
 	}
 
 	@Override
 	public Resource<?>[] getResources(String activityName,
 			ResourceType type) {
 		Resource<?>[] resources = db.getResources(userName, activityName);
 		return buildResourcesByType(activityName, MAIN_PACKAGE, type, resources);
 	}
 
 	@Override
 	public String[] getResourcesNames(String activityName,
 			String packageName) {
 		Resource<?>[] resources = db.getResources(userName, activityName);
 		String[] names = new String[resources.length];
 		for(int i=0;i<resources.length;i++){
 			names[i] = resources[i].getName();
 		}
 		return names;
 	}
 
 	@Override
 	public String[] getResourcesNames(String activityName,
 			String packageName, ResourceType type) {
 		Resource<?>[] resources = db.getResources(userName, activityName);
 		List<String> names = new ArrayList<String>();
 		for(Resource<?> resource : resources){
 			if(resource.getType().equals(type))
 				names.add(resource.getName());
 		}
 		return names.toArray(new String[0]);
 	}
 
 	@Override
 	public String[] getResourcesNames(String activityName) {
 		return getResourcesNames(activityName, MAIN_PACKAGE);
 	}
 
 	@Override
 	public String[] getResourcesNames(String activityName,
 			ResourceType type) {
 		return getResourcesNames(activityName, MAIN_PACKAGE, type);
 	}
 
 	@Override
 	public void addPackage(String activityName,
 			String packageName) {
 		db.addPackage(userName, activityName, packageName);
 		
 		String activityPackageName = getFullPath(activityName);
 		String fullPackageName = getFullPath(activityName, packageName);
 		File activityDir = context.getDir(activityPackageName, Context.MODE_PRIVATE);
 		File fullDir = context.getDir(fullPackageName, Context.MODE_PRIVATE);
 		if(activityDir == null || fullDir == null){
 			throw new CanNotCreatePackageException("The package cant be crated");
 		}
 		Log.d("FileService", activityDir.getAbsolutePath());
 	}
 
 	@Override
 	public void addResource(String activityName,
 			String packageName, Resource<InputStream> resource) {
 		db.addResource(userName, activityName, packageName, resource);		
 		String fullPath = getFullPath(activityName, packageName);
 		try {
 			FileOutputStream output = context.openFileOutput(fullPath + "." + resource.getName(),Context.MODE_PRIVATE);
			byte[] bytes = new byte[1000];
 			int end = 0;
			do{
				end = resource.getResource().read(bytes,0,1000);
				output.write(bytes);
			}while(end >= 0);
			output.flush();
 			output.close();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void addResource(String activityName,
 			String packageName, String name, ResourceType type, InputStream io) {
 		Resource<InputStream> resource = new Resource<InputStream>(name, type, io);
 		addResource(activityName, packageName, resource);
 	}
 
 	@Override
 	public void addResource(String activityName,
 			String packageName, String name, ResourceType type, byte[] bytes) {
 		Resource<Void> resource = new Resource<Void>(name, type);
 		db.addResource(userName, activityName, packageName, resource);		
 		String fullPath = getFullPath(activityName, packageName);
 		try {
 			FileOutputStream output = context.openFileOutput(fullPath + "." + resource.getName(),Context.MODE_PRIVATE);
 			output.write(bytes);
 			output.flush();
 			output.close();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void addResource(String activityName,
 			String packageName, String name, ResourceType type, String data) {
 		Resource<Void> resource = new Resource<Void>(name, type);
 		db.addResource(userName, activityName, packageName, resource);		
 		String fullPath = getFullPath(activityName, packageName);
 		dbService.put(fullPath + "." + resource.getName(), data);
 	}
 
 	@Override
 	public void addResource(String activityName,
 			Resource<InputStream> resource) {
 		addResource(activityName, MAIN_PACKAGE ,resource);
 	}
 
 	@Override
 	public void addResource(String activityName, String name,
 			ResourceType type, InputStream io) {
 		addResource(activityName, MAIN_PACKAGE, name, type, io);
 
 	}
 
 	@Override
 	public void addResource(String activityName, String name,
 			ResourceType type, byte[] bytes) {
 		addResource(activityName, MAIN_PACKAGE, name, type, bytes);
 
 	}
 
 	@Override
 	public void addResource(String activityName, String name,
 			ResourceType type, String data) {
 		addResource(activityName, MAIN_PACKAGE, name, type, data);
 
 	}
 	
 	private String getFullPath(String activityName){
 		return getFullPath(activityName, null);
 	}
 	
 	private String getFullPath(String activityName, String packageName){
 		StringBuffer s = new StringBuffer(userName + "." + activityName);
 		if(packageName != null)
 			s.append("."  + packageName);
 		return s.toString();
 	}
 }
