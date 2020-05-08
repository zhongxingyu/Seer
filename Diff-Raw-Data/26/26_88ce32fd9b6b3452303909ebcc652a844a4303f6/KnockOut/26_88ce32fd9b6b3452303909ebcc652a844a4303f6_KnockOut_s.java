 package database.tournamentParts;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 
 import util.Binary;
 import util.Nullary;
 import util.Tree;
 
 import database.match.Commandable;
 import database.match.Match;
 import database.players.Player;
 
 public class KnockOut extends Commandable {
 
 	private Tree tree;
 	private boolean unsaved = false;
 	private Map<Tree, Player> players;
 	private Map<Tree, Match> matches;
 
 	public KnockOut() {
 		tree = null;
 		players = new HashMap<Tree, Player>();
 		matches = new HashMap<Tree, Match>();
 	}
 
 	public void setPlayers(List<Player> pls) {
 		Queue<Tree> st = new LinkedList<Tree>();
 		players.clear();
 		// TODO rescue matches!
 		matches.clear();
 		for (Player p : pls) {
 			if (p.isNobody() || p == null)
 				st.add(null);
 			else {
 				Tree n = new Nullary();
 				st.add(n);
 				players.put(n, p);
 			}
 		}
 		tree = setPlayers(st);
 		visitNodes();
 		unsaved = true;
 	}
 
 	private Tree setPlayers(Queue<Tree> st) {
 		System.out.println("len = " + st.size());
 		if (st.size() == 1)
 			return st.poll();
 		Tree t1, t2;
 		Queue<Tree> st1 = new LinkedList<Tree>();
 		while (!st.isEmpty()) {
 			t1 = st.poll();
 			System.out.println("t1 = " + t1);
 			t2 = st.poll();
 			System.out.println("t2 = " + t2);
 			if (t1 == null)
 				st1.add(t2);
 			else if (t2 == null)
 				st1.add(t1);
 			else {
 				System.out.println(players.get(t1) + ", " + players.get(t2));
 				st1.add(new Binary(t1, t2));
 			}
 		}
 		return setPlayers(st1);
 	}
 
 	@Override
 	public boolean addMatch(Match game) {
 		visitNodes();
 		unsaved = true;
 		return true;
 	}
 
 	@Override
 	public void delMatch(Match game) {
 		visitNodes();
 		unsaved = true;
 	}
 
 	private void visitNodes() {
		visitNodes(tree);
 	}
 
	private void visitNodes(Tree t) {
 		if (!t.hasChildren())
			return;
 		for (Tree t1 : t.getChildren())
			visitNodes(t1);
 
 		Player pl = players.get(t.getChildren().get(0));
 		Player pr = players.get(t.getChildren().get(1));
 
 		if (pl == null || pr == null) {
 			matches.remove(t);
 			players.remove(t);
			return;
 		}
 
 		if (matches.get(t) == null || !matches.get(t).getLeft().equals(pl)
 				|| !matches.get(t).getRight().equals(pr)) {
 			players.remove(t);
 			matches.put(t, new Match(pl, pr));
			return;
 		}
 
 		if (matches.get(t).getWinner() != null)
 			players.put(t, matches.get(t).getWinner());
 		else
 			players.remove(t);
 	}
 
 	@Override
 	public int getFinishedGamesCount() {
 		return getMatchByState(Match.STATE_FINISHED).size();
 	}
 
 	@Override
 	public List<Match> getMatchByState(int state) {
 		List<Match> result = new ArrayList<Match>();
 		for (Match e : getMatches())
 			if (e.getState() == state)
 				result.add(e);
 		return result;
 	}
 
 	@Override
 	public List<Match> getMatches() {
 		List<Match> result = new ArrayList<Match>();
 		for (Entry<Tree, Match> e : matches.entrySet())
 			result.add(e.getValue());
 		return result;
 	}
 
 	@Override
 	public int getTotalGamesCount() {
 		return tree.countNodes();
 	}
 
 	public boolean isUnsaved() {
 		return unsaved;
 	}
 
 	public void setUnsaved(boolean b) {
 		unsaved = b;
 	}
 
 	public List<Player> getRanking() {
 		List<Player> result = new ArrayList<Player>();
 		List<Tree> trees = Collections.singletonList(tree);
 		rank(result, trees);
 		return result;
 	}
 
 	private void rank(List<Player> result, List<Tree> trees) {
 		List<Tree> trees1 = new ArrayList<Tree>();
 		for (Tree t : trees) {
 			if (!players.containsKey(t))
 				result.add(Player.getNobody());
 			else if (!result.contains(players.get(t)))
 				result.add(players.get(t));
 			trees1.addAll(t.getChildren());
 		}
 		if (!trees1.isEmpty())
 			rank(result, trees1);
 	}
 
 	public boolean isDone() {
		return getRanking().get(0) != null;
 	}
 
 	public Tree getTree() {
 		return tree;
 	}
 
 	public Map<Tree, Player> getPlayersMap() {
 		return players;
 	}
 
 	public Map<Tree, Match> getMatchesMap() {
 		return matches;
 	}
 
 }
