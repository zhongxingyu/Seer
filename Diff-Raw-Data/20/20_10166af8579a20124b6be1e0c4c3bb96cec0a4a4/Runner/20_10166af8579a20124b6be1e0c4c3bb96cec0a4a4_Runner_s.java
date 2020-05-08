 package org.glowacki.core.ascii;
 
 import com.googlecode.lanterna.TerminalFacade;
 import com.googlecode.lanterna.input.Key;
 import com.googlecode.lanterna.screen.Screen;
 import com.googlecode.lanterna.terminal.Terminal;
 import com.googlecode.lanterna.terminal.TerminalSize;
 
 import org.glowacki.core.Character;
 import org.glowacki.core.CoreException;
 import org.glowacki.core.Level;
 import org.glowacki.core.MovableCharacter;
 import org.glowacki.core.MovableCharacter.Direction;
 import org.glowacki.core.Terrain;
 
 class AsciiTerm
 {
    private static final int maxX = 20;
    private static final int maxY = 10;

     private Screen screen;
     private int maxRows;
     private int maxCols;
 
     private boolean running;
 
     private Level level;
     private MovableCharacter character;
 
     private String errMsg;
 
     AsciiTerm()
     {
         //Terminal terminal = TerminalFacade.createTerminal();
         //terminal.enterPrivateMode();
         screen = TerminalFacade.createScreen();
 
         TerminalSize sz = screen.getTerminal().getTerminalSize();
         maxRows = sz.getRows();
         maxCols = sz.getColumns();
 
         screen.startScreen();
     }
 
     private char[][] buildMap()
     {
         final int maxX = level.getMaxX();
         final int maxY = level.getMaxY();
 
         char[][] map = new char[maxY + 1][maxX + 1];
 
         for (int y = 0; y <= maxY; y++) {
             for (int x = 0; x <= maxX; x++) {
                 char ch;
                 try {
                     ch = getTerrainCharacter(level.get(x, y));
                 } catch (CoreException ce) {
                     ce.printStackTrace();
                     ch = '?';
                 }
 
                 map[y][x] = ch;
             }
         }
 
         for (MovableCharacter c : level.getCharacters()) {
             map[c.getY()][c.getX()] = '*';
         }
 
         return map;
     }
 
     public void close()
     {
         screen.stopScreen();
         //terminal.exitPrivateMode();
     }
 
     private void drawScreen()
     {
         screen.clear();
 
         if (level != null) {
             char[][] map = buildMap();
 
             int padX = (maxCols - map[0].length) / 2;
             int padY = (maxRows - map.length) / 2;
 
             StringBuilder buf = new StringBuilder(map[0].length);
             for (int y = 0; y < map.length; y++) {
                 buf.setLength(0);
                 for (int x = 0; x < map[y].length; x++) {
                     buf.append(map[y][x]);
                 }
 
                 screen.putString(padX, padY + y, buf.toString(),
                                  Terminal.Color.WHITE,  Terminal.Color.BLACK);
             }
         }
 
         if (errMsg != null) {
             screen.putString(0, 23, errMsg, Terminal.Color.RED,
                              Terminal.Color.BLACK);
         }
 
         screen.refresh();
     }
 
     private static char getTerrainCharacter(Terrain t)
     {
         char ch;
 
         switch (t) {
         case DOOR:
             ch = '+';
             break;
         case DOWNSTAIRS:
             ch = '<';
             break;
         case FLOOR:
             ch = '.';
             break;
         case TUNNEL:
             ch = '#';
             break;
         case UPSTAIRS:
             ch = '>';
             break;
         case WALL:
             ch = '-';
             break;
         case WATER:
             ch = '~';
             break;
         case UNKNOWN:
             ch = ' ';
             break;
         default:
             ch = '?';
             break;
         }
 
         return ch;
     }
 
     private boolean handleInput(Key key)
     {
         switch (key.getKind()) {
         case NormalKey:
             switch (key.getCharacter()) {
             case 'q':
                 running = false;
                 break;
             case 'h':
                 character.move(Direction.LEFT);
                 break;
             case 'l':
                 character.move(Direction.RIGHT);
                 break;
             case 'k':
                 character.move(Direction.UP);
                 break;
             case 'j':
                 character.move(Direction.DOWN);
                 break;
             default:
                 errMsg = "Unknown key " + key;
                 break;
             }
             break;
         case ArrowLeft:
             character.move(Direction.LEFT);
             break;
         case ArrowRight:
             character.move(Direction.RIGHT);
             break;
         case ArrowUp:
             character.move(Direction.UP);
             break;
         case ArrowDown:
             character.move(Direction.DOWN);
             break;
         default:
             errMsg = "Unknown key " + key;
             break;
         }
 
         return true;
     }
 
     public void loop()
     {
         running = true;
 
         drawScreen();
         while (running) {
             boolean changed = false;
 
             Key key = screen.readInput();
             if (key != null) {
                 errMsg = null;
                 changed = handleInput(key);
             }
 
             if (changed || screen.resizePending()) {
                 drawScreen();
             }
         }
     }
 
     public void setCharacter(Character c)
         throws CoreException
     {
         character = level.enterDown(c);
     }
 
     public void setLevel(Level l)
     {
         level = l;
     }
 }
 
 public class Runner
 {
     private static final String[] map = new String[] {
         "           ---------",
         "           |.......|",
         "           |.......|",
         "           |.......|",
         "           ---+-----",
         "              #",
         "              #",
         "              ###",
         "                #",
         "------          #",
         "|....|          #       ----------",
         "|....+#######   #       |.....>..|",
         "|.<..|      ############+........|",
         "|....|                  |........|",
         "------                  ----------",
     };
 
     public static final void main(String[] args)
         throws CoreException
     {
         AsciiTerm display = new AsciiTerm();
 
         Level lvl = new Level(map);
         display.setLevel(lvl);
 
         Character ch = new Character("me", 10, 10, 10);
         display.setCharacter(ch);
 
         try {
             display.loop();
         } finally {
             display.close();
         }
     }
 }
