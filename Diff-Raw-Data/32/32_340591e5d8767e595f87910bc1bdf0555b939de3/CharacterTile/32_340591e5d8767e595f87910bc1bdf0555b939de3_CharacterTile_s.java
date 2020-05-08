 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.HashMap;
 import java.util.Map;
 
 public class CharacterTile extends Tile {
 	private static final long serialVersionUID = 9195762811283553381L;
 
 	private static final Map<Character, String> symbols;
 	private static final Map<Character, String> symbolNames;
 
 	protected char symbol;
 	
 	static {
		symbols = new HashMap<>();
 		symbols.put('1', "\u4E00");
 		symbols.put('2', "\u4E8C");
 		symbols.put('3', "\u4E09");
 		symbols.put('4', "\u56DB");
 		symbols.put('5', "\u4E94");
 		symbols.put('6', "\u516D");
 		symbols.put('7', "\u4E03");
 		symbols.put('8', "\u516B");
 		symbols.put('9', "\u4E5D");
 		symbols.put('N', "\u5317");
 		symbols.put('E', "\u6771");
 		symbols.put('W', "\u897F");
 		symbols.put('S', "\u5357");
 		symbols.put('C', "\u4E2D");
 		symbols.put('F', "\u767C");
 		symbols.put('w', "\u842C");
 		
		symbolNames = new HashMap<>();
 		for (int i = 1; i < 10; i++) {
 			symbolNames.put(Character.forDigit(i, 10), "Character " + i);
 		}
 		symbolNames.put('N', "North Wind");
 		symbolNames.put('E', "East Wind");
 		symbolNames.put('W', "West Wind");
 		symbolNames.put('S', "South Wind");
 		symbolNames.put('C', "Red Dragon");
 		symbolNames.put('F', "Green Dragon");
 	}
     
     public CharacterTile(char symbol) {
 		if (symbols.get(symbol) == null) {
 			throw new IllegalArgumentException("Invalid character to initialize " +
 					"character tile (" + symbol + ")");
 		}
         this.symbol = symbol;
 		
 		setToolTipText(toString());
     }
 	public CharacterTile(char symbol, boolean drawRound) {
 		this(symbol);
 		this.drawRound = drawRound;
 	}
     
 	@Override
     public boolean matches(Tile otherTile) {
         return super.matches(otherTile) && this.symbol == ((CharacterTile) otherTile).symbol;
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
 		
 		Font font = g.getFont();
 
 		g.setColor(RED);
 		Font newFont = font.deriveFont(10f);
 		g.setFont(newFont);
 		g.drawString(String.valueOf(symbol), WIDTH, 15);
 		
 		g.setColor(BLACK);
 
 		float NUMBER_SIZE = 20f;
 		float size = 40f;
 		int x = WIDTH / 2 - 10;
 		int y = HEIGHT / 2 + 15;
 		if (symbol == 'C' || symbol == 'F') {
 			if (symbol == 'C') {
 				g.setColor(RED);
 			}
 			else {
 				g.setColor(GREEN);
 			}
 		}
 		else if (String.valueOf(symbol).matches("[1-9]")) {
 			size = NUMBER_SIZE;
 			x = WIDTH / 2;
 			y = HEIGHT / 2 - 10;
 		}
 		
 		newFont = font.deriveFont(size);
         g.setFont(newFont);
 		
 		g.drawString(symbols.get(symbol), x, y);
 		
 		if (size == NUMBER_SIZE) {
 			g.setColor(RED);
 			g.drawString(symbols.get('w'), x, y * 2);
 		}
     }
     
 	@Override
     public String toString() {
 		String name = symbolNames.get(symbol);
 		if (name == null) {
 			return "Invalid character string";
 		}
 		return name;
     }
 }
