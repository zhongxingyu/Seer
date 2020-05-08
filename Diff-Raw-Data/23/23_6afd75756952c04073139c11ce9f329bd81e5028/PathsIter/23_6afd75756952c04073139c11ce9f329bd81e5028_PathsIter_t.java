 package com.strategy.havannah.logic;
 
 import java.util.List;
 
 import net.sf.javabdd.BDD;
 import net.sf.javabdd.BDDFactory;
 
 import com.google.common.collect.Iterables;
 import com.strategy.api.board.Board;
 import com.strategy.api.logic.Position;
 import com.strategy.havannah.logic.BoardAnalyzerHavannah.PathCalculator;
 import com.strategy.util.StoneColor;
 import com.strategy.util.operation.Bdd;
 import com.strategy.util.predicates.ValidPositionFilter;
 
 /**
  * @author Ralph Dürig
  */
 public class PathsIter implements PathCalculator {
 	private BDDFactory fac;
 	private Board board;
 
 	private Bdd logPandQ = Bdd.create("p and q");
 	private Bdd logNPandNQ = Bdd.create("not p and not q");
 	private Bdd logPMandMQ = Bdd.create("pm and mq");
 	private Bdd logPQorPMMQ = Bdd.create("pq or (pm and mq)");
 	private BDD[][] reachWhite;
 	private BDD[][] reachBlack;
 
 	/**
 	 * 
 	 */
 	public PathsIter(BDDFactory fac, Board board) {
 		this.fac = fac;
 		this.board = board;
 		initReachability();
 	}
 
 	public BDD getPath(Position p, Position q, StoneColor color) {
 		Integer indexP = board.getField(p.getRow(), p.getCol()).getIndex();
 		Integer indexQ = board.getField(q.getRow(), q.getCol()).getIndex();
 		BDD path;
 		if (color.equals(StoneColor.WHITE)) {
 			path = reachWhite[indexP][indexQ].id();
 		} else {
 			path = reachBlack[indexP][indexQ].id();
 		}
 		return path;
 	}
 
 	public void done() {
 		// fac.done();
 		// System.out.println("all recursions: " + allrec);
 		logPandQ.log();
 		logNPandNQ.log();
 		logPMandMQ.log();
 		logPQorPMMQ.log();
 	}
 
 	// ************************************************************************
 
 	private void initReachability() {
 		// number of vertices
 		int V = board.getColumns() * board.getRows();
 
 		// adjacency matrix with bdd style
 		BDD[][] graphWhite = new BDD[V][V];
 		BDD[][] graphBlack = new BDD[V][V];
 		for (Position pos : board.getPositions()) {
 			int indexP = board.getField(pos.getRow(), pos.getCol()).getIndex();
 			List<Position> neighbors = pos.getNeighbors();
 			Iterable<Position> filtered = Iterables.filter(neighbors,
 					new ValidPositionFilter(board));
 			for (Position n : filtered) {
 				int indexN = board.getField(n.getRow(), n.getCol()).getIndex();
 				if (null == graphWhite[indexP][indexN]) {
 					graphWhite[indexP][indexN] = fac.zero();
 				}
 				if (null == graphBlack[indexP][indexN]) {
 					graphBlack[indexP][indexN] = fac.zero();
 				}
 				graphWhite[indexP][indexN] = graphWhite[indexP][indexN]
 						.orWith(logPandQ.andLog(fac.ithVar(indexP),
 								fac.ithVar(indexN)));
 				graphBlack[indexP][indexN] = graphBlack[indexP][indexN]
 						.orWith(logNPandNQ.andLog(fac.nithVar(indexP),
 								fac.nithVar(indexN)));
 			}
 		}
 
 		reachWhite = new BDD[V][V];
 		reachBlack = new BDD[V][V];
 		doFill(reachWhite, graphWhite, V);
 		doFill(reachBlack, graphBlack, V);
 
 		// reachability matrix with floyd-warshall-algorithm and bdd style also
 		for (int k = 0; k < V; k++) {
 			// Pick all vertices as source one by one
 			for (int i = 0; i < V; i++) {
 				// Pick all vertices as destination for the
 				// above picked source
 				for (int j = 0; j < V; j++) {
 					// If vertex k is on a path from i to j,
 					// then make sure that the value of reach[i][j] is 1
 
 					doReach(reachWhite, k, i, j, board);
 					doReach(reachBlack, k, i, j, board);
 
 				}
 			}
 		}
 	}
 
 	private void doFill(BDD[][] todo, BDD[][] graph, int V) {
 		for (int i = 0; i < V; i++) {
 			for (int j = 0; j < V; j++) {
 				BDD bdd = graph[i][j];
 				todo[i][j] = bdd;
 			}
 		}
 	}
 
 	private void doReach(BDD[][] todo, int m, int p, int q, Board b) {
 		BDD reachpq;
 		if (!b.isValidField(p) || !b.isValidField(q) || null == todo[p][q]) {
 			reachpq = fac.zero();
 		} else {
 			reachpq = todo[p][q];
 		}
 		BDD reachpm;
 		if (!b.isValidField(p) || !b.isValidField(m) || null == todo[p][m]) {
 			reachpm = fac.zero();
 		} else {
 			reachpm = todo[p][m];
 		}
 		BDD reachmq;
 		if (!b.isValidField(m) || !b.isValidField(q) || null == todo[m][q]) {
 			reachmq = fac.zero();
 		} else {
 			reachmq = todo[m][q];
 		}
 		// todo[p][q] = reachpq.or(reachpm.and(reachmq));
		todo[p][q] = logPQorPMMQ.orLog(reachpq.id(),
				logPMandMQ.andLog(reachpm.id(), reachmq.id()));
 	}
 
 	private void printMatrix(int V, BDD[][] reach) {
 		System.out
 				.println("Following matrix is transitive closure of the given graph");
 		for (int i = 0; i < V; i++) {
 			for (int j = 0; j < V; j++)
 				System.out.print(reach[i][j]);
 			System.out.println();
 		}
 
 	}
 
 }
