 package controllers;
 
 import java.util.Arrays;
 import java.util.List;
 
 import models.User;
 import models.Video;
 import net.sf.oval.constraint.NotNull;
 
 public class VideoService extends BaseService {
 	
 	public static void registerVideo(@NotNull String videoId, @NotNull  String fileName, @NotNull Long lenght, @NotNull String userId, @NotNull String chunks){
 		System.out.println(1);
 		System.out.println(videoId);
 		System.out.println(2);
 		System.out.println(fileName);
 		System.out.println(3);
 		System.out.println(lenght);
 		System.out.println(4);
 		System.out.println(userId);
 		System.out.println(5);
 		System.out.println(chunks);
 		
 		
 		if(validation.hasErrors()){
 			play.Logger.error("Invalid params: %s", params);
 			jsonError("Invalid params");
 		}
 		
 		play.Logger.info("Video registration requested by user: "+userId+" for video: "+videoId);
 		
 		User registrationRequester = User.find("email=?", userId).first();
 		Video video = Video.find("videoId=?", videoId).first();
 		
 		if(registrationRequester == null){
 			play.Logger.error("No existe el registrationRequester: %s", userId);
 			jsonError("No existe el registrationRequester "+userId);
 		}
 		
 		if(video != null){
 			play.Logger.error("El video que se quiere registrar ya existe en el indice: %s", videoId);
 			jsonError("El video que se quiere registrar ya existe en el indice: "+videoId);
 		}
 		
 		video = new Video(videoId, fileName, lenght, chunkIds(chunks), registrationRequester);
 		
 		try{
 			video = video.save();
 			play.Logger.info("Se registro el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
 			jsonOk("Se registro el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
 		} catch(Exception e) {
 			play.Logger.error("No se pudo registrar el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
 			jsonError("No se pudo registrar el video <id: "+videoId+"><fileName: "+fileName+"> en el indice");
 		}
 		
 	}
 	
 	
 
 	private static List<String> chunkIds(String chunks) {
 		return Arrays.asList(chunks.split(CHUNK_SEPARATOR));
 	}
 }
 
