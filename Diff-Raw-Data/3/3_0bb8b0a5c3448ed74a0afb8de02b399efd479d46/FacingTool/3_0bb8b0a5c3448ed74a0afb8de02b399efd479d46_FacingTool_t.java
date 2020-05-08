 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.tool;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.KeyStroke;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.TokenUtil;
 
 /**
  */
 public class FacingTool extends DefaultTool {
 
 	private double facing;
 	
 	// TODO: This shouldn't be necessary, just get it from the renderer
 	private Token tokenUnderMouse;
 	private Set<GUID> selectedTokenSet;
 
 	public FacingTool () {
 		// Non tool-bar tool ... atm
     }
 
 	public void init(Token keyToken, Set<GUID> selectedTokenSet) {
 		tokenUnderMouse = keyToken;
 		this.selectedTokenSet = selectedTokenSet;
 	}
 	
     @Override
     public String getTooltip() {
         return "Set the token facing";
     }
     
     @Override
     public String getInstructions() {
     	return "tool.facing.instructions";
     }
     
     @Override
     protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
     	super.installKeystrokes(actionMap);
     	
 		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 
 				if (MapTool.confirm("Are you sure you want to delete the facing of the selected tokens ?")) {
 					for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
 						Token token = renderer.getZone().getToken(tokenGUID);
 						if (token == null) {
 							continue;
 						}
 						
 						token.setFacing(null);
 					}
 					
 					// Go back to the pointer tool
 					resetTool();
 				}
 			}
 		});
     }
     
     ////
     // MOUSE
     @Override
     public void mouseMoved(MouseEvent e) {
     	super.mouseMoved(e);
     	
     	Rectangle bounds = renderer.getTokenBounds(tokenUnderMouse);
     	
     	int x = bounds.x + bounds.width/2;
     	int y = bounds.y + bounds.height/2;
     	
     	double angle = Math.atan2(y - e.getY(), e.getX() - x);
     	
     	int degrees = (int)Math.toDegrees(angle);
 
     	if (!SwingUtil.isControlDown(e)) {
 	    	int[] facingAngles = renderer.getZone().getGrid().getFacingAngles();
 	    	degrees = facingAngles[TokenUtil.getIndexNearestTo(facingAngles, degrees)];
     	}
     	
     	for (GUID tokenGUID : selectedTokenSet) {
     		Token token = renderer.getZone().getToken(tokenGUID);
     		if (token == null) {
     			continue;
     		}
     		
     		token.setFacing(degrees);
    		renderer.flush(token);
     	}
 
     	renderer.repaint(); // TODO: shrink this
     }
     
     @Override
     public void mousePressed(MouseEvent e) {
 
     	// Commit
     	for (GUID tokenGUID : selectedTokenSet) {
 
     		Token token = renderer.getZone().getToken(tokenGUID);
     		if (token == null) {
     			continue;
     		}
     		
    		renderer.flush(token);
     		MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
     	}
 		
 		// Go back to the pointer tool
 		resetTool();
     }
     
     @Override
     protected void resetTool() {
     	if (tokenUnderMouse.isStamp() || tokenUnderMouse.isBackground()) {
     	  	  MapTool.getFrame().getToolbox().setSelectedTool(StampTool.class);
     	} else {
     	  	  MapTool.getFrame().getToolbox().setSelectedTool(PointerTool.class);
     	}
     }
 }
