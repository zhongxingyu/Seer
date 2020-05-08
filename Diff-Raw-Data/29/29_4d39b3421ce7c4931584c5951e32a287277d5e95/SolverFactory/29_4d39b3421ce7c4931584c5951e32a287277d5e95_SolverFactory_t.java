 /* This file is part of Eternity II Editor.
  * 
  * Eternity II Editor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Eternity II Editor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Eternity II Editor.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Eternity II Editor project is hosted on SourceForge:
  * http://sourceforge.net/projects/eternityii/
  * and maintained by Yannick Kirschhoffer <alcibiade@alcibiade.org>
  */
 
 package org.alcibiade.eternity.editor.solver;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.alcibiade.eternity.editor.model.GridModel;
 import org.alcibiade.eternity.editor.solver.diamonds.DiamondsAdapter;
 import org.alcibiade.eternity.editor.solver.genetic.DumbGeneticSolver;
 import org.alcibiade.eternity.editor.solver.backtracking.BlockSolverMkI;
 import org.alcibiade.eternity.editor.solver.backtracking.IterativePathSolverMkI;
 import org.alcibiade.eternity.editor.solver.backtracking.IterativePathSolverMkII;
 import org.alcibiade.eternity.editor.solver.backtracking.IterativePathSolverMkIII;
 import org.alcibiade.eternity.editor.solver.path.LinearPath;
 import org.alcibiade.eternity.editor.solver.path.PathProvider;
 import org.alcibiade.eternity.editor.solver.pipeline.PipelineSolver;
 import org.alcibiade.eternity.editor.solver.swap.AStarSolverMkI;
 import org.alcibiade.eternity.editor.solver.swap.DumbSolver;
 import org.alcibiade.eternity.editor.solver.swap.DumbSolverNew;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkI;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkII;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkIII;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkIV;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkV;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkVI;
 import org.alcibiade.eternity.editor.solver.swap.WeightedRandomMkVII;
 ;
 
 public class SolverFactory {
 
 	public static final String LABEL_ITPATHMKI = "Iterative Path MkI";
 	public static final String LABEL_ITPATHMKII = "Iterative Path MkII";
 	public static final String LABEL_ITPATHMKIII = "Iterative Path MkIII";
 	public static final String LABEL_SWAPDUMB = "Swap Dumb";
 	public static final String LABEL_SWAPDUMBNEW = "Swap Dumb New";
 	public static final String LABEL_SWAPWMKI = "Swap Weighted MkI";
 	public static final String LABEL_SWAPWMKII = "Swap Weighted MkII";
 	public static final String LABEL_SWAPWMKIII = "Swap Weighted MkIII";
 	public static final String LABEL_SWAPWMKIV = "Swap Weighted MkIV";
 	public static final String LABEL_SWAPWMKV = "Swap Weighted MkV";
 	public static final String LABEL_SWAPWMKVI = "Swap Weighted MkVI";
 	public static final String LABEL_SWAPWMKVII = "Swap Weighted MkVII";
 	public static final String LABEL_BLOCKMKI = "Block MkI";
 	public static final String LABEL_ASTARMKI = "A* MkI";
 	public static final String LABEL_PIPELINE = "Pipeline";
 	public static final String LABEL_DUMBGENETIC = "Genetic (Dumb)";
 
 	public static List<String> getAvailableSolvers() {
 		List<String> solvers = new ArrayList<String>();
 		// solvers.add(LABEL_ITPATHMKI);
 		// solvers.add(LABEL_ITPATHMKII);
 		solvers.add(LABEL_DUMBGENETIC);
 		solvers.add(LABEL_ITPATHMKIII);
 		solvers.add(LABEL_SWAPDUMB);
 		solvers.add(LABEL_SWAPDUMBNEW);
 		solvers.add(LABEL_SWAPWMKI);
 		solvers.add(LABEL_SWAPWMKII);
 		solvers.add(LABEL_SWAPWMKIII);
 		solvers.add(LABEL_SWAPWMKIV);
 		solvers.add(LABEL_SWAPWMKV);
 		solvers.add(LABEL_SWAPWMKVI);
 		// solvers.add(LABEL_SWAPWMKVII);
 		
 		return solvers;
 	}
 
 	public static EternitySolver createSolver(String type, GridModel pieces, GridModel solution,
 			ClusterManager clusterManager) throws UnknownSolverException {
 		return createSolver(type, pieces, solution, clusterManager, new LinearPath());
 	}
 
 	public static EternitySolver createSolver(String type, GridModel pieces, GridModel solution,
 			ClusterManager clusterManager, PathProvider path) throws UnknownSolverException {
 		EternitySolver solver = null;
 
 		if (LABEL_ITPATHMKI.equalsIgnoreCase(type)) {
 			solver = new IterativePathSolverMkI(pieces, solution, clusterManager, path);
 		} else if (LABEL_ITPATHMKII.equalsIgnoreCase(type)) {
 			solver = new IterativePathSolverMkII(pieces, solution, clusterManager, path);
 		} else if (LABEL_ITPATHMKIII.equalsIgnoreCase(type)) {
 			solver = new IterativePathSolverMkIII(pieces, solution, clusterManager, path);
 		} else if (LABEL_SWAPDUMB.equalsIgnoreCase(type)) {
 			solver = new DumbSolver(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPDUMBNEW.equalsIgnoreCase(type)) {
 			solver = new DumbSolverNew(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKI.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkI(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKII.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkII(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKIII.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkIII(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKIV.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkIV(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKV.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkV(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKVI.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkVI(pieces, solution, clusterManager);
 		} else if (LABEL_SWAPWMKVII.equalsIgnoreCase(type)) {
 			solver = new WeightedRandomMkVII(pieces, solution, clusterManager);
 		} else if (LABEL_BLOCKMKI.equalsIgnoreCase(type)) {
 			solver = new BlockSolverMkI(pieces, solution, clusterManager);
 		} else if (LABEL_ASTARMKI.equalsIgnoreCase(type)) {
 			solver = new AStarSolverMkI(pieces, solution, clusterManager);
 		} else if (LABEL_PIPELINE.equalsIgnoreCase(type)) {
 			solver = new PipelineSolver(pieces, solution, clusterManager);
 		}
 		
 		// ----- Genetic Stuff
 		else if (LABEL_DUMBGENETIC.equalsIgnoreCase(type)) {
 			//population = 10
 			solver = new DumbGeneticSolver(pieces, solution, clusterManager, 50);
 		}
		// ----- End Genetic Stuff
 		else {
 			throw new UnknownSolverException("Unknown solver type: " + type);
 		}
 
 		return solver;
 	}
 
 	public static boolean isSolverPathSensitive(String type) {
 		return LABEL_ITPATHMKI.equals(type) || LABEL_ITPATHMKII.equals(type)
 				|| LABEL_ITPATHMKIII.equals(type);
 	}
 }
