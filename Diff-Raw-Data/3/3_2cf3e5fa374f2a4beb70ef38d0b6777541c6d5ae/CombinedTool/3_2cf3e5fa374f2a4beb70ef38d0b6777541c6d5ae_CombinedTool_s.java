 package hotciv.view;
 
 import hotciv.framework.*;
 
 import minidraw.framework.*;
 import minidraw.standard.*;
 import minidraw.standard.handlers.*;
 
 import java.awt.event.*;
 import java.util.*;
 
 public class CombinedTool implements Tool {
     List<Tool> tools;
 
     public CombinedTool(DrawingEditor editor, Game game) {
         tools = new ArrayList<Tool>();
        tools.add(new MoveTool(editor, game));
         tools.add(new EndTurnTool(editor, game));
         tools.add(new ActionTool(editor, game));
         tools.add(new InspectTool(editor, game));
     }
 
     public void keyDown(KeyEvent evt, int key) {
         for (Tool tool : tools)
             tool.keyDown(evt, key);
     }
 
     public void mouseDown(MouseEvent e, int x, int y) {
         for (Tool tool : tools)
             tool.mouseDown(e, x, y);
     }
 
     public void mouseDrag(MouseEvent e, int x, int y) {
         for (Tool tool : tools)
             tool.mouseDrag(e, x, y);
     }
 
     public void mouseMove(MouseEvent e, int x, int y) {
         for (Tool tool : tools)
             tool.mouseMove(e, x, y);
     }
 
     public void mouseUp(MouseEvent e, int x, int y) {
         for (Tool tool : tools)
             tool.mouseUp(e, x, y);
     }
 }
