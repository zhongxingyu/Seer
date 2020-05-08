 package data;
 
 public class Renderer {
	public static void draw(GameBoard b) {
 		char[][] map = new char[b.staticBoard.ySize][b.staticBoard.xSize];
 		
 		/*
 		
 		Wall					#	0x23
 		Player					@	0x40
 		Player on goal square	+	0x2b
 		Box						$	0x24
 		Box on goal				*	0x2a
 		Goal square				.	0x2e
 		Empty floor			(space)	0x20
 		Start					s	0x73
 		
 		 */
 		
 		// Static stuff
 		
 		for(int y=0;y<b.staticBoard.ySize;y++){
 			for(int x=0;x<b.staticBoard.xSize;x++){
 				if(b.staticBoard.isFloor(x, y))
 					if(b.staticBoard.isGoal(x, y))
 						map[y][x] = '.';
 					else
 						map[y][x] = ' ';
 				else
 					map[y][x] = '#';
 			}
 		}
 		
 		int psx = b.staticBoard.start.getX();
 		int psy = b.staticBoard.start.getY();
 		map[psy][psx] = 's';
 		
 		
 		// Dynamic stuff
 		
 		int px = b.player.getX();
 		int py = b.player.getY();
 		if(map[py][px] == '.')
 			map[py][px] = '+';
 		else
 			map[py][px] = '@';
 		
 		for(int i = 0; i < b.boxes.size(); i++) {
 			int bx = b.boxes.get(i).getX();
 			int by = b.boxes.get(i).getY();
 			
 			switch(map[by][bx]) {
 			case ' ':
 				map[by][bx] = '$';
 				break;
 			case '.':
 				map[by][bx] = '*';
 				break;
 			}
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		for(int y = 0; y < map.length; y++) {
 			sb.append(map[y]);
 			sb.append('\n');
 		}
		System.out.println(sb.toString());
 	}
 
 }
