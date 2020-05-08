 package keno.service.file;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import keno.model.Draw;
 import keno.service.LotteryService;
 import keno.util.parser.DrawFileReader;
 
 import org.apache.log4j.Logger;
 
 public class FileLotteryService implements LotteryService {
 
 	private static final Logger LOGGER = Logger.getLogger(FileLotteryService.class);
 	
 	private File source;
 
 	public FileLotteryService(String sourcePath) {
 		this.source = new File(sourcePath);
 	}
 
 	@Override
 	public Draw getMostRecentDraw() {
 		FileReader fr = null;
 		try {
 			fr = new FileReader(source);
 			DrawFileReader reader = new DrawFileReader(fr);
 			
 			return reader.readNext();
 		} catch (IOException e) {
 			LOGGER.error("I/O error while processing file: " + source.getAbsolutePath());
 			return null;
 		} finally {
 			try {
 				if (fr != null) {
 					fr.close();
 				}
 			} catch (IOException e) {
 				
 			}
 		}
 	}
 
 	@Override
 	public List<Draw> listMostRecentDraws(int count) {
 		if (count <= 0) {
 			throw new IllegalArgumentException("Count must be positive.");
 		}
 		
 		FileReader fr = null;
 		try {
 			fr = new FileReader(source);
 			DrawFileReader reader = new DrawFileReader(fr);
 			
 			List<Draw> draws = new ArrayList<Draw>();
 			for (int i= 0; i < count; ++i) {
 				Draw draw = reader.readNext();
 				if (draw != null) {
 					draws.add(draw);
 				} else {
 					break;
 				}
 			}
 			return draws;
 		} catch (IOException e) {
 			LOGGER.error("I/O error while processing file: " + source.getAbsolutePath());
			return null;
 		} finally {
 			try {
 				if (fr != null) {
 					fr.close();
 				}
 			} catch (IOException e) {
 				
 			}
 		}
 	}
 
 	@Override
 	public List<Draw> listDraws() {
 		FileReader fr = null;
 		try {
 			fr = new FileReader(source);
 			DrawFileReader reader = new DrawFileReader(fr);
 			
 			List<Draw> draws = new ArrayList<Draw>();
 			Draw draw = null;
 			while ((draw = reader.readNext()) != null) {
 				draws.add(draw);
 			}
 			return draws;
 		} catch (IOException e) {
 			LOGGER.error("I/O error while processing file: " + source.getAbsolutePath());
			return null;
 		} finally {
 			try {
 				if (fr != null) {
 					fr.close();
 				}
 			} catch (IOException e) {
 				
 			}
 		}
 	}
 
 }
