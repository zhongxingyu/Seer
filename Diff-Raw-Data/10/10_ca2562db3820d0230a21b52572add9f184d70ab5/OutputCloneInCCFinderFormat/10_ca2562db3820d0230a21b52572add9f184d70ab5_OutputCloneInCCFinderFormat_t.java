 package jp.ac.osaka_u.ist.sdl.mpanalyzer.clone;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedSet;
 import java.util.concurrent.ConcurrentMap;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.db.ReadOnlyDAO;
 
 public class OutputCloneInCCFinderFormat extends CDetector {
 
 	public static void main(final String[] args) {
 
 		final OutputCloneInCCFinderFormat outputter = new OutputCloneInCCFinderFormat();
 
 		System.out.print("identifying source files in revision ");
 		System.out.print(Long.toString(Config.getCloneDetectionRevision()));
 		System.out.print(" ... ");
 		final SortedSet<String> paths = outputter.identifyFiles();
 		System.out.print(Integer.toString(paths.size()));
 		System.out.println(" files: done.");
 
 		System.out.print("measuring file size ... ");
 		final ConcurrentMap<String, List<Statement>> contents = outputter
 				.getFileContent(paths);
 		System.out.println(": done.");
 
 		System.out.print("outputting ... ");
 		final Map<String, Integer> ids = new HashMap<String, Integer>();
 		for (final String path : contents.keySet()) {
 			ids.put(path, ids.size());
 		}
 
 		try {
 
 			final BufferedWriter writer = new BufferedWriter(new FileWriter(
 					Config.getCloneOutputFile()));
 
 			writer.write("#begin{file description}");
 			writer.newLine();
 
 			for (final Entry<String, List<Statement>> entry : contents
 					.entrySet()) {
 				final String path = entry.getKey();
 				final List<Statement> statements = entry.getValue();
 
 				writer.write("0.");
 				writer.write(ids.get(path).toString());
 				writer.write("\t");
				writer.write(Integer.toString(statements.size()));
 				writer.write("\t");
				writer.write(Integer.toString(statements.size()));
 				writer.write("\t");
 				writer.write(path);
 				writer.newLine();
 			}
 
 			writer.write("#end{file description}");
 			writer.newLine();
 
 			writer.write("#begin{clone}");
 			writer.newLine();
 
 			Map<Integer, List<Clone>> clones;
 			try {
 				final ReadOnlyDAO readOnlyDAO = ReadOnlyDAO.getInstance();
 				clones = readOnlyDAO.getClones();
 				readOnlyDAO.close();
 			} catch (final Exception e) {
 				clones = new HashMap<Integer, List<Clone>>();
 				e.printStackTrace();
 				System.exit(0);
 			}
 			for (final List<Clone> set : clones.values()) {
 				writer.write("#begin{set}");
 				writer.newLine();
 
 				for (final Clone clone : set) {
 					final String start = Integer.toString(clone.startLine);
 					final String end = Integer.toString(clone.endLine);
 					writer.write("0.");
 					writer.write(ids.get(clone.path).toString());
 					writer.write("\t");
 					writer.write(start);
 					writer.write(",1,");
 					writer.write(start);
 					writer.write("\t");
 					writer.write(end);
 					writer.write(",1,");
 					writer.write(end);
 					writer.write("\t");
 					if (0 < clone.changed) {
 						writer.write(Integer.toString(clone.endLine
 								- clone.startLine));
 					} else {
 						writer.write("0");
 					}
 					writer.newLine();
 				}
 
 				writer.write("#end{set}");
 				writer.newLine();
 			}
 
 			writer.write("#end{clone}");
 			writer.newLine();
 
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		System.out.println(": done.");
 	}
 }
