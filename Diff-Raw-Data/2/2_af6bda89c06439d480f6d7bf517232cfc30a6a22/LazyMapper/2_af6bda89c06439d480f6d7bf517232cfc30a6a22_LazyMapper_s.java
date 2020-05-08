 package ca.etsmtl.capra.purifier.implementations;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import ca.etsmtl.capra.datas.Position;
 import ca.etsmtl.capra.purifier.services.MappingService;
 import ca.etsmtl.capra.purifier.services.PurifierServiceAbstract;
 import ca.etsmtl.capra.tools.mapviewer.MappingLine;
 import ca.etsmtl.capra.util.saving.FileSaver;
 
 public class LazyMapper extends PurifierServiceAbstract implements MappingService{
 
 	private String SAVE_PATH =  System.getProperty("user.home") + "/recMaps/";
 	
 	private Queue<Position> obstacleList = new LinkedBlockingQueue<Position>();
 	private FileSaver<Position> obstacleSaver;
 	private FileSaver<MappingLine> lineSaver;
 	
 	
 	@Override
 	public void addObstacle(Position position) {
 		obstacleList.add(position);
 		obstacleSaver.save(position);
 	}
 
 	@Override
 	public void addObstacle(float x, float y) {
 		addObstacle(new Position(x, y));
 	}
 	
 	@Override
 	public void updateService(boolean forced) {
 
 		
 	}
 
 	@Override
 	public void resolveSummons() {
 				
 	}
 
 	@Override
 	public void resolveSummonsDone() {
 		//TODO Checker si le dossier existe deja (genre si on fait deux tests dans la meme minute)
 		
 		Calendar calendar = Calendar.getInstance();
 		
 		String folderName = "/" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) 
 			+ "-" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
 		
 		File file = new File(SAVE_PATH + folderName);
		file.mkdir();		
 		
 		try {
 			obstacleSaver = new FileSaver<Position>(SAVE_PATH + folderName + "/obstacles");
 			lineSaver = new FileSaver<MappingLine>(SAVE_PATH + folderName + "/lines");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void reset() {
 		
 	}
 
 	@Override
 	public void addLine(float x1, float y1, float x2, float y2) {
 		lineSaver.save(new MappingLine(x1,y1,x2,y2));
 		
 	}	
 }
