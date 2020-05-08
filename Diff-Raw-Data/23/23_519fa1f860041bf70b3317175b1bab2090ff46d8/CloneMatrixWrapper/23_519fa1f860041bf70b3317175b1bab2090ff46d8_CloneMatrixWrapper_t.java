 package danilofes.mes.db.matrix;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import danilofes.mes.ParseFiles;
 import danilofes.mes.db.entity.generic.GenericFragment;
 
 public class CloneMatrixWrapper {
 	private Map<String, List<CloneIntersectionMatrix>> matrix = new HashMap<String, List<CloneIntersectionMatrix>>();
 
 	public void addFragmentList(List<GenericFragment> fragments, String appName) {
 
 		for (GenericFragment fragment : fragments) {
 			List<CloneIntersectionMatrix> matrixIntersections = matrix.get(fragment.getPath());
 
 			if (matrixIntersections == null) {
 				matrixIntersections = new ArrayList<CloneIntersectionMatrix>();
 			}
 
 			addAllLines(matrixIntersections, fragment, appName);
 
 			matrix.put(fragment.getPath(), matrixIntersections);
 		}
 
 	}
 
 	private void addAllLines(List<CloneIntersectionMatrix> matrixIntersections, GenericFragment fragment, String appName) {
 		for (int currentLine = fragment.getLine(); currentLine <= fragment.getEndLine(); currentLine++) {
 			CloneIntersectionMatrix matrixIntersection = getFragmentByLine(matrixIntersections, currentLine);
 
 			if (matrixIntersection == null) {
 				matrixIntersection = new CloneIntersectionMatrix();
 				matrixIntersection.line = fragment.getLine();
 			}
 
 			if (ParseFiles.CLONEDIGGER.equals(appName)) {
 				matrixIntersection.cloneDigger = true;
 			} else if (ParseFiles.CPD.equals(appName)) {
 				matrixIntersection.cpd = true;
 			} else if (ParseFiles.SIMIAN.equals(appName)) {
 				matrixIntersection.simian = true;
 			}
 
 			matrixIntersections.add(matrixIntersection);
 			matrix.put(fragment.getPath(), matrixIntersections);
 		}
 
 	}
 
 	public CloneIntersectionMatrix getFragmentByLine(List<CloneIntersectionMatrix> matrixIntersections, Integer line) {
 		for (CloneIntersectionMatrix matrix : matrixIntersections) {
 			if (matrix.line == line) {
 				return matrix;
 			}
 		}
 
 		return null;
 	}
 
 	public Map<String, List<CloneIntersectionMatrix>> getMatrix() {
 		return matrix;
 	}
 
 }
