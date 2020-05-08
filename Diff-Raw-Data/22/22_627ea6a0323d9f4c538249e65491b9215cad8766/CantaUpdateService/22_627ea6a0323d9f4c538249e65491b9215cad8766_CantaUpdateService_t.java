 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.cantaconcali;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.ActivityResource;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.domain.User;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.ComunicationService;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.ComunicationService.ExternalResourceType;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.ComunicationService.OnArriveResource;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.DataStorageService;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.FileSystemService;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.UpdateService;
 import org.utn.proyecto.helpful.integrart.integrar_t_android.services.UpdateService.OnArriveNewResources;
 
 import roboguice.inject.ContextSingleton;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 
 @ContextSingleton
 public class CantaUpdateService implements OnArriveNewResources, OnArriveResource{
 	private static final String ACTIVITY_NAME = "cantaActivity";
 	private static final String GET_DATA_RESOURCE = "updateData";
 	
 	private final UpdateService updateService;
 	private final ComunicationService comService;
 	private final FileSystemService fileService;
 	private final DataStorageService db;
 	private final User user;
 	private final Gson gson = new Gson();
 	
 	@Inject
 	public CantaUpdateService(UpdateService updateService, ComunicationService comService, FileSystemService fileService, User user, DataStorageService db){
 		this.updateService = updateService;
 		this.comService = comService;
 		this.fileService = fileService;
 		this.user = user;
 		this.db = db;
 		if(!db.contain(getDataKey())) db.put(getDataKey(), new CantaData[0]);
 	}
 	
 	public void findUpdate(){
 		updateService.findUpdates(ACTIVITY_NAME, this);
 	}
 
 	@Override
 	public void onArriveNewResources(List<ActivityResource> resources) {
 		for(ActivityResource res : resources){
 			InputStream is = comService.findStream(ExternalResourceType.STATICS, res.getPath());
 			fileService.addResource(ACTIVITY_NAME, res.getResourceName(), res.getResourceType(), is); 
 		}
 		findData();
 	}
 	
 	private void findData(){
		if(comService.isOffLine()) return;
 		comService.findResource(
 				ExternalResourceType.SETTINGS, 
 				GET_DATA_RESOURCE, 
 				new String[]{ACTIVITY_NAME, user.getUserName(), updateService.getDeviceId()}, 
 				this);
 	}
 	
 	public static String getDataKey(User user){
 		return user.getUserName() + "." + ACTIVITY_NAME + ".songs";
 	}
 	
 	private String getDataKey(){
 		return getDataKey(user);
 	}
 
 	@Override
 	public void onArrive(String json) {
 		CantaData[] songs = gson.fromJson(json, CantaData[].class);
 		for(CantaData song : songs){
 			addSong(song);
 		}
 	}
 	
 	private void addSong(CantaData song){
 		if(!db.contain(getDataKey())) db.put(getDataKey(), new CantaData[0]);
 		List<CantaData> list = new ArrayList<CantaData>();
 		CantaData[] songs = db.get(getDataKey(), CantaData[].class);
 		for(CantaData data : songs){
 			list.add(data);
 		}
 		list.add(song);
 		db.put(getDataKey(), list.toArray(new CantaData[0]));
 	}
 }
