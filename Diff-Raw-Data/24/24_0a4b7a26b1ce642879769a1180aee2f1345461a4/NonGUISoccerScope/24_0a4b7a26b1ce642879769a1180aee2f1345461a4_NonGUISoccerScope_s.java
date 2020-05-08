 package soccerscope;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Properties;
 
 import soccerscope.file.LogFileReader;
 import soccerscope.model.Player;
 import soccerscope.model.Scene;
 import soccerscope.model.SceneSet;
 import soccerscope.model.SceneSetMaker;
 import soccerscope.model.Team;
 import soccerscope.model.WorldModel;
 import soccerscope.util.GameAnalyzer;
 import soccerscope.util.analyze.SceneAnalyzer;
 import soccerscope.util.analyze.Xmling;
 
 import com.jamesmurty.utils.XMLBuilder;
 
 public class NonGUISoccerScope {
 
 	public static void run(String filename, String xmlFilename)
 			throws Exception {
 		// initiate the world model...
 		WorldModel wm = WorldModel.getInstance();
 		wm.clear(); // don't think this is necessary... but ok...
 		File file = new File(filename);
 		if (file == null || !file.canRead()) {
 			throw new FileNotFoundException(String.format(
 					"invalid file '%s' or permissions...\n", filename));
 		}
 		// open the log file and analyze it (by doing SceneSet.analyze())
 		NonGUISoccerScope.openAndAnalyzeLogFile(wm.getSceneSet(), file
 				.getPath());
 		NonGUISoccerScope.printXML(wm.getSceneSet(), xmlFilename);
 		System.out.println("DONE!");
 	}
 
 	private static void openAndAnalyzeLogFile(SceneSet sceneSet, String filename)
 			throws IOException, InterruptedException {
 		LogFileReader lfr = new LogFileReader(filename);
 		SceneSetMaker ssm = new SceneSetMaker(lfr, sceneSet);
 		ssm.run();
 	}
 
 	private static void printXML(SceneSet sceneSet, String xmlFilename)
 			throws Exception {
 		XMLBuilder builder = XMLBuilder.create("analysis");
 		
 		Scene fscene = WorldModel.getInstance().getSceneSet().iterator().next();
 		XMLBuilder left = builder.elem("leftteam").attr("name",fscene.left.name);
 		int[] plindex = Team.firstAndLastPlayerIndexes(Team.LEFT_SIDE);
 		for(int iter = plindex[0]; iter < plindex[1]; iter++) {
 			Player p = fscene.player[iter];
 			left.elem("player")
 				.attr("unum",String.valueOf(p.unum))
 				.attr("viewQuality",p.viewStr())
 				.attr("type",p.typeStr());
 		}
 
 		
 		XMLBuilder right = builder.elem("rightteam").attr("name",fscene.right.name);
 		plindex = Team.firstAndLastPlayerIndexes(Team.RIGHT_SIDE);
 		for(int iter = plindex[0]; iter < plindex[1]; iter++) {
 			Player p = fscene.player[iter];
 			right.elem("player")
 				.attr("unum",String.valueOf(p.unum))
 				.attr("viewQuality",p.viewStr())
 				.attr("type",p.typeStr());
 		}
 
 		
 		for (SceneAnalyzer analyzer : GameAnalyzer.analyzerList) {
 			// all the scene analyzers that can output to XML will do it...
 			if (Xmling.class.isInstance(analyzer)) {
 				((Xmling) analyzer).xmlElement(builder);
 			}
 		}
 
 		Properties outputProperties = new Properties();
 		// Explicitly identify the output as an XML document
 		outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
 
 		// Pretty-print the XML output (doesn't work in all cases)
 		outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");
 
 		// Get 2-space indenting when using the Apache transformer
 		outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");
 
 		// output file...
 		PrintWriter out = new PrintWriter(new File(xmlFilename));
 
 		builder.toWriter(out, outputProperties);
 		out.flush();
 	}
 
 }
