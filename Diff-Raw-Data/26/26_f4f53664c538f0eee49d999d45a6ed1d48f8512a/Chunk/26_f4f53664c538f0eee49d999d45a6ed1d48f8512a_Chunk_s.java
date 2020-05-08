 package ee.ant.pimap.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Chunk {
 	private int xPos;  // xPos,yPos position divided by 15 of the top-left corner
 	private int yPos;
 	private List<Tile> tiles = new ArrayList<Tile>(); // array of tiles (15x15)
 
 	public Chunk() {
 		this.xPos = 0;
 		this.yPos = 0;
 	}
 
 	public Chunk (int x, int y) {
 		this.xPos = x / 15;
 		this.yPos = y / 15;
 	}
 
 	public String getFullName() {
 		return "chunk0_"+ xPos +"_"+yPos;
 	}
 
 	public String getName() {
 		return "chunk"+ xPos +"_" + yPos;
 	}
 
	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

 	private void parseTiles(String rawData) {
 		// tiles in a format:
 		// [[tile1], [tile2], .. [tile225]] (including outer brackets)
 
 		// strip outer brackets and split into array
 		String body = rawData.substring(1, rawData.length()-1);
 		StringBuilder token = new StringBuilder();
 		List<String> tiles = new ArrayList<String>();
 		int delimiter = 0;
 		for (int i=0; i<body.length(); i++) {
 			char c = body.charAt(i);
			if (c == '[') {
 				delimiter++;
 				if (delimiter > 1) token.append('[');  // not to forget inner brackets
			} else if (c == ']') {
 				delimiter--;
 				if (delimiter  == 0) {
 					tiles.add(token.toString());
 					token.setLength(0);
 				} else {
 					token.append(']'); // not to forget inner brackets
 				}
 			} else if (delimiter > 0) {
 				token.append(c);
 			}
 		}
 		if (tiles.size() != 225) {
 			System.out.println("Chunk size mismatch:" + tiles.size() + " should be 225");
 		}  else {
 			extractTiles(tiles);
 		}
 	}
 
 	private void extractTiles(List<String> data) {
 		for (int i=0; i<225; i++) {
 			Tile tile = new Tile(xPos*15+i%15, yPos*15+i/15);
 			tile.setContent(data.get(i));
 			tiles.add(tile);
 		}
 	}
 
 	/**
 	 * Get parsed tiles
 	 * @return tiles
 	 */
 	public List<Tile> getTiles() {
 		return tiles;
 	}
 
 	public void setTiles(String rawData) {
 		// raw data is coma separated list of tiles in brackets with xPos,yPos coordinates of top-left corner like:
 		// [xPos,yPos, [[tile1], [tile2], .. [tile225]]]
 		String[] body = rawData.substring(1, rawData.length()-1).split(",", 3);
 		this.xPos = Integer.parseInt(body[0]);
 		this.yPos = Integer.parseInt(body[1]);
 		parseTiles(body[2]);
 	}
 
 	/**
 	 * Gets tile from chunk
 	 * @param tile to query
 	 * @return tile from chunk
 	 */
 	public Tile getTile (Tile tile) {
 		int x = tile.getX();
 		int y = tile.getY();
 		if (tiles.size() > 0) {
 			return tiles.get(x-xPos*15 + (y-yPos*15)*15);
 		} else {
 			return null;
 		}
 	}
 }
