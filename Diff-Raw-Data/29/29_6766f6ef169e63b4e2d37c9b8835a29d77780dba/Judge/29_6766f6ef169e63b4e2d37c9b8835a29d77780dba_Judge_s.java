 package org.weiqi;
 
 import java.util.Map;
import java.util.Map.Entry;
 import java.util.Set;
 
 import org.util.Util;
 import org.weiqi.Weiqi.Array;
 import org.weiqi.Weiqi.Occupation;
 
 public class Judge {
 
 	public static Occupation checkByOccupationExistence(Board board) {
 		int nPiecesCount = 0;
 		Set<Occupation> players = Util.createHashSet();
 
 		for (Coordinate c : Coordinate.all()) {
 			Occupation color = board.get(c);
 			players.add(color);
 			nPiecesCount += color != Occupation.EMPTY ? 1 : 0;
 		}
 
 		if (nPiecesCount > 1)
 			for (Occupation player : Weiqi.players)
 				if (players.contains(player)
 						&& !players.contains(player.opponent()))
 					return player;
 
 		return null;
 	}
 
 	public static void checkGroupsLiveness(Board board, Array<Boolean> alives) {
 		GroupAnalysis ga = new GroupAnalysis(board);
		Map<Integer, Occupation> groupColors = ga.getGroupColors();
 
 		// Judge which groups are eyes, i.e. surrounded by only one colour
		Map<Integer, Boolean> groupIsEye = Util.createHashMap();
 
		for (Entry<Integer, Occupation> entry : groupColors.entrySet())
			if (entry.getValue() == Occupation.EMPTY) {
				Integer groupId = entry.getKey();
 				Set<Occupation> colors = Util.createHashSet();
 
				for (Integer neighborGroupId : ga.getTouches(groupId)) {
					Occupation color = groupColors.get(neighborGroupId);
 					if (color != Occupation.EMPTY)
 						colors.add(color);
 				}
 
 				// Has two colours
				groupIsEye.put(groupId, colors.size() <= 1);
 			}
 	}
 
 }
