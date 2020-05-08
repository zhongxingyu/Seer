 package pipes.editing.io;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.antlr.v4.runtime.ANTLRFileStream;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 
 import pipes.editing.io.TuneParser.LineContext;
 import pipes.editing.io.TuneParser.MeasureContext;
 import pipes.editing.io.TuneParser.MelodyElementContext;
 import pipes.editing.io.TuneParser.TuneContext;
 import pipes.model.Line;
 import pipes.model.Measure;
 import pipes.model.Note;
 import pipes.model.TimeSignature;
 import pipes.model.Tune;
 import pipes.model.embellishment.EmbellishmentFamily;
 
 public class TuneSerializer {
 	/**
 	 * The file extension associated with Tune files
 	 */
 	public static final String FILE_EXTENSION = "grp";
 	
 	public static void main(String... args) {
 		//loadTune(new File("C:\\Users\\jbauscha\\workspace\\Pipes\\tunes\\test.grp"));
 	}
 	
 	public static File saveTune(Tune t, File f) throws IOException {
 		File savedFile = f;
 		if (!f.getName().endsWith("." + FILE_EXTENSION))
 			savedFile = new File(f.getAbsolutePath() + "." + FILE_EXTENSION);
 		
 		try (FileWriter w = new FileWriter(savedFile)) {
 			// Name, Author, Type
 			w.write('[' + t.getName() + "]\n");
 			w.write('[' + t.getAuthor() + "]\n");
 			w.write('[' + t.getType() + "]\n");
 			
 			// Lines of music
 			boolean firstLine = true;
 			for (Line l : t) {
 				if (!firstLine)
 					w.write("\n");
 				firstLine = false;
 	
 				for (Measure m : l) {
 					boolean firstInMeasure = true;
 					if (m.isTimeSignatureChange()) {
 						w.write("[" + m.getTimeSignature() + "]");
 						firstInMeasure = false;
 					}
 					
 					for (Note n : m) {
 						if (!firstInMeasure)
 							w.write(" ");
 						firstInMeasure = false;
 	
 						if (n.hasEmbellishment())
 							w.write(n.getEmbellishmentFamily().getShortName() + " ");
 						w.write(n.getPitch().shortName+"-"+n.getBeatDivision().denominator);
 						for (int i = 0; i<n.getNumDots(); ++i)
 							w.write(".");
 						
 						if (n.getIsTiedForward())
 							w.write(" -");
 					}
 					
 					w.write(" |");
 				}
 			}
 		}
 		
 		return savedFile;
 	}
 	
 	public static Tune loadTune(File f) throws IOException {
 		CharStream stream = new ANTLRFileStream(f.getAbsolutePath());
 		TuneLexer lexer = new TuneLexer(stream);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		TuneParser parser = new TuneParser(tokens);
 
 		Tune tune = new Tune();
 		TuneContext tuneContext = parser.tune();
 		tune.setName(stripFirstLast(tuneContext.getChild(0).getText()));
 		tune.setAuthor(stripFirstLast(tuneContext.getChild(2).getText()));
 		
 		TimeSignature currentTime = null;
 		EmbellishmentFamily family = null;
 		Note lastNoteSeen = null;
 		for (LineContext l : tuneContext.line()) {
 			Line tuneLine = new Line();
 			tune.add(tuneLine);
 			for (MeasureContext m : l.measure()) {
 				Measure tuneMeasure;
 				if (m.TimeSignature() == null) {
 					tuneMeasure = new Measure(currentTime);
 				} else {
 					currentTime = TimeSignature.fromString(m.TimeSignature().getText());
 					tuneMeasure = new Measure(currentTime);
 					tuneMeasure.setIsTimeSignatureChange(true);
 				}
 				tuneLine.add(tuneMeasure);
 				for (MelodyElementContext me : m.melodyElement()) {
 					if (me.EMBELLISHMENT() != null) {
 						family = EmbellishmentFamily.getByName(me.EMBELLISHMENT().getText());
 					} else if (me.TIE() != null) {
 						lastNoteSeen.setIsTiedForward(true);						
 					} else if (me.note() != null) {
 						lastNoteSeen = Note.fromString(tune, me.note().getText());
 						tuneMeasure.addNote(lastNoteSeen);
 						
 						if (family != null)
 							lastNoteSeen.setEmbellishmentFamily(family);
 						
 						family = null;
 					}
 				}
 			}
 		}
 		
 		return tune;
 	}
 	
 	private static String stripFirstLast(String str) {
 		return str.substring(1, str.length()-1);
 	}
 }
