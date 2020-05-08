 package org.weiqi;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.util.Util;
import org.weiqi.GroupAnalysis.Group;
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
 
 		// Judge which groups are eyes, i.e. surrounded by only one colour
		Map<Group, Boolean> groupIsEye = Util.createHashMap();
 
		for (Group group : ga.getGroups())
			if (group.color == Occupation.EMPTY) {
 				Set<Occupation> colors = Util.createHashSet();
 
				for (Group neighborGroup : group.touches) {
					Occupation color = neighborGroup.color;
 					if (color != Occupation.EMPTY)
 						colors.add(color);
 				}
 
 				// Has two colours
				groupIsEye.put(group, colors.size() <= 1);
 			}
 	}
 
 }
