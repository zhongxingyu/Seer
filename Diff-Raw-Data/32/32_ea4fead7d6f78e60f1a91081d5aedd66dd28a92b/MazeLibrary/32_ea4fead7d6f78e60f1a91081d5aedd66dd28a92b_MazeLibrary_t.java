 package uk.ac.ox.cs.sokobanexam.model;
 
 import uk.ac.ox.cs.sokobanexam.model.sprites.Arrow;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Crate;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Floor;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Human;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Nothing;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Room;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Target;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Wall;
 import uk.ac.ox.cs.sokobanexam.util.Dir;
 import uk.ac.ox.cs.sokobanexam.util.Point;
 
 /**
  * Contains a collection of exciting mazes ready to be plugged into
  * Sokoban for a great and fun experience for the entire family.
  */
 public class MazeLibrary {
 	/**
 	 * This is the original "Oxford Rocks!" map known from "The Maze Game" by Boris Motik.
 	 * @return		the prefilled, playable, original "Oxford Rocks!" map.
 	 */
 	public static Maze oxfordRocks() {
 		return fromStrings(
 				"##  #Oxford# .0 #   ",
 				"##0 #Rocks!#.0.@←   ",
 				"##  ######## #↓##   ",
 				"##0 #      #.←.#    ",
 				"..  ←       0#0####↑",
 				"..  ←   0   0  .    ",
 				"##0 #  0  ##   #0   ",
 				"##  #  ##0 #   #    ",
 				"##0 #  ..0 ← # #    ",
 				"##  #  ..  #        "
 			);
 	}
 	/**
 	 * This map is for you how just want to win quickly, so you can get on to more important matters.
 	 * @return		a prefilled, playable, easy map to get started with.
 	 */
 	public static Maze maze1() {
 		return fromStrings(
 				"#########",
 				"# @ 0 . #",
 				"#########"
 			);
 	}
 	/**
	 * This map is called "The Loop".
	 * @return		a prefilled, playable map that isn't winnable.
	 */
	public static Maze maze2() {
		return fromStrings(
				"I'm#sorry",
				"#→→→→→→→#",
				"#@ →↓ 0 #",
				"#  ↑← . #",
				"#→→→→→→→#",
				"about#it,"
			);
	}
	/**
 	 * A challenging map straight from the original Sokoban game.
 	 * @return		a prefilled, playable, hard map to finish off.
 	 */
	public static Maze maze3() {
 		return fromStrings(
 				"      ############ ",
 				"     ##..    #   # ",
 				"    ##..↑ 0    0 # ",
 				"   ##..↑.# # # 0## ",
 				"   #..↑.# # # 0  # ",
 				"####...#  #    # # ",
 				"#  ## #          # ",
 				"# @0 0 ###  #   ## ",
 				"# 0   0   # #   #  ",
 				"###00   # # # # #  ",
 				"  #   0   # # #####",
 				"  # 0# #####      #",
 				"  #0   #   #    # #",
 				"  #  ###   ##     #",
 				"  #  #      #    ##",
 				"  ####      ###### ");
 	}
 	
 	private static Maze fromStrings(String... rows) {
 		Maze maze = new DefaultMaze(rows[0].length(), rows.length);
 		for (Point point : maze.getPoints()) {
 			Room room;
 			char c = rows[point.y].charAt(point.x);
 			switch (c) {
 			case '#':
 				room = new Wall(point);
 				break;
 			case '.':
 				room = new Target(point);
 				break;
 			case '0':
 				room = new Floor(new Crate(point));
 				break;
 			case '↑':
 				room = new Arrow(new Nothing(point), Dir.NORTH);
 				break;
 			case '→':
 				room = new Arrow(new Nothing(point), Dir.EAST);
 				break;
 			case '↓':
 				room = new Arrow(new Nothing(point), Dir.SOUTH);
 				break;
 			case '←':
 				room = new Arrow(new Nothing(point), Dir.WEST);
 				break;
 			case '@':
 				room = new Floor(new Human(point));
 				break;
 			case ' ':
 				room = new Floor(point);
 				break;
 			default:
 				room = new Wall(new Nothing(point), ""+c);
 				break;
 			}
 			maze.putRoom(room);
 		}
 		return maze;
 	}
 }
