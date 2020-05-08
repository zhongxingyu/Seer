 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.tutorial;
 
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.ResourceFactory.LabelBuilder;
 import ca.couchware.wezzle2d.Rule;
 import ca.couchware.wezzle2d.animation.IAnimation;
 import ca.couchware.wezzle2d.animation.FadeAnimation;
 import ca.couchware.wezzle2d.graphics.EntityGroup;
 import ca.couchware.wezzle2d.graphics.IPositionable.Alignment;
 import ca.couchware.wezzle2d.piece.PieceType;
 import ca.couchware.wezzle2d.tile.TileColor;
 import ca.couchware.wezzle2d.tile.TileEntity;
 import ca.couchware.wezzle2d.tile.TileType;
 import ca.couchware.wezzle2d.ui.ILabel;
 import ca.couchware.wezzle2d.ui.SpeechBubble;
 import ca.couchware.wezzle2d.ui.SpeechBubble.BubbleType;
 import ca.couchware.wezzle2d.ui.IButton;
 import ca.couchware.wezzle2d.ui.SpriteButton;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 /**
  * This is the basic tutorial.  It teaches the elementary rules of Wezzle.
  * 
  * @author cdmckay
  */
 public class BasicTutorial extends AbstractTutorial
 {          
     
     /**
      * Is the menu visible?
      */
     private boolean menuShown = false;
     
     /**
      * The text that directs the user.crea
      */
     private List<ILabel> labelList;
     
     /**
      * A speech bubble that indicates where the user should press.
      */
     private SpeechBubble bubble;       
     
     /**
      * The repeat button.
      */
     private IButton repeatButton;
     
     /**
      * The continue button.
      */
     private IButton continueButton;
     
     /**
      * The constructor.
      */
     public BasicTutorial()
     {
         // Set the name.
         super("Basic Tutorial");
         
         // This tutorial has a single rule.  It activates on level one.        
         addRule(new Rule(Rule.Type.LEVEL, Rule.Operation.EQ, 1));
     }
     
     public void initialize(final Game game)
     {
         // Set the activataed variable.
         initialized = true;
         
         // Save the manager states.
         game.boardMan.saveState();
         game.statMan.saveState();
         game.scoreMan.saveState();
         
         // Turn off the game in progress variable in the world manager.
         game.worldMan.setGameInProgress(false);
         
         // Slow down refactor so the user can see more clearly what happens.
         game.setRefactorSpeed(50);
         
          // Set restriction board so that only the bottom left corner is
         // clickable.
         game.pieceMan.clearRestrictionBoard();
         game.pieceMan.reverseRestrictionBoard();
         game.pieceMan.setRestrictionCell(0, game.boardMan.getRows() - 1, true);
         
         // Create the text that instructs the user to click the block.        
         labelList = new ArrayList<ILabel>();
         ILabel label = null;
         
         // Line 1.
         label = new LabelBuilder(280, 166)
                 .alignment(EnumSet.of(Alignment.BOTTOM, Alignment.LEFT))
                 .cached(false)
                 .color(Game.TEXT_COLOR1).size(16)
                 .text("Lines are made by lining").end();
         game.layerMan.add(label, Layer.EFFECT);   
         labelList.add(label);
         
         // Line 2.
         label = new LabelBuilder(label).y(166 + 24)
                 .text("up 3 tiles of the same").end();
         game.layerMan.add(label, Layer.EFFECT);                 
         labelList.add(label);
         
         // Line 3.
         label = new LabelBuilder(label).y(166 + 24 + 24)
                 .text("colour.").end();
         game.layerMan.add(label, Layer.EFFECT);                                 
         labelList.add(label);
         
         // All done.
         label = null;
         
         // Stop the piece manager from dropping.
         game.pieceMan.setTileDropOnCommit(false);
         
         // Stop the timer.
         game.timerMan.setStopped(true); 
         
         // Create the speech bubble and add it to the layer manaager.
         // The speech bubble will be positioned over the button right
         // corner of the board.
         bubble = new SpeechBubble.Builder(
                     game.boardMan.getX() + game.boardMan.getCellWidth() / 2,
                     game.boardMan.getY() + game.boardMan.getHeight() 
                         - game.boardMan.getCellHeight())
                 .type(BubbleType.VERTICAL).text("Click here").end();                
         game.layerMan.add(bubble, Layer.EFFECT);   
         game.layerMan.toFront(bubble, Layer.EFFECT);           
         
         // Create repeat button.
         repeatButton = new SpriteButton.Builder(400, 330)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .text("Repeat").offOpacity(70).visible(false).end();
         game.layerMan.add(repeatButton, Layer.EFFECT);
         
          // Create continue button, using the repeat button as a template.
         continueButton = new SpriteButton.Builder((SpriteButton) repeatButton)
                 .y(390).text("Continue").end();
         game.layerMan.add(continueButton, Layer.EFFECT);          
         
         // Run the repeat tutorial method, that sets up the things that must
         // be reset each time the tutorial is run.
         repeat(game);                                                                                                                                     
     }
 
     @Override
     protected boolean update(final Game game)
     {        
         // If the move count is not 0, then the tutorial is over.
         if (game.statMan.getMoveCount() != 0 && menuShown == false)
         {               
             // Stop the piece animation.
             game.pieceMan.getPieceGrid().setVisible(false); 
             game.pieceMan.stopAnimation();                                            
             
             // Fade the board out.            
             EntityGroup e = game.boardMan.getTiles(game.boardMan.getCells() / 2, 
                     game.boardMan.getCells() - 1);            
             IAnimation a = new FadeAnimation.Builder(FadeAnimation.Type.OUT, e)
                     .wait(0).duration(500).end();
             game.animationMan.add(a);
                                     
             // Fade in two new buttons.
             FadeAnimation f;                        
              
             //f = new FadeAnimation(FadeType.IN, 100, 500, repeatButton);
             //f.setMaxOpacity(70);
             f = new FadeAnimation.Builder(FadeAnimation.Type.IN, repeatButton)
                     .wait(100).duration(500).maxOpacity(70).end();            
             game.animationMan.add(f);
                          
             //f = new FadeAnimation(FadeType.IN, 100, 500, continueButton);
             //f.setMaxOpacity(70);
             f = new FadeAnimation.Builder(FadeAnimation.Type.IN, continueButton)
                     .wait(100).duration(500).maxOpacity(70).end();
             game.animationMan.add(f);
             
             // Menu is now shown.
             menuShown = true;
             
             // Create the confirm window.
 //            confirmWindow = new Window(game.getGameWindow(), 
 //                    400, 300, 
 //                    310, 150);
 //            confirmWindow.setAlignment(
 //                    EnumSet.of(Alignment.MIDDLE, Alignment.CENTER));            
 //            game.layerMan.add(confirmWindow, Layer.EFFECT);            
             
             return true;
         }
         else if (menuShown == true)
         {
             if (repeatButton.isActivated() == true)
             {
                 repeat(game);                
             }
             else if (continueButton.isActivated() == true)
             {
                 return false;
             }
         }      
         
         return true;
     }
 
     @Override
     protected void finish(final Game game)
     {   
         // Load the score managers state.
         game.boardMan.loadState();
         game.statMan.loadState();
         game.scoreMan.loadState();                                                
         
         // Remove the repeat button.
         repeatButton.setVisible(false);
         game.layerMan.remove(repeatButton, Layer.EFFECT);
         repeatButton = null;
         
         // Remove the continue button.
         continueButton.setVisible(false);
         game.layerMan.remove(continueButton, Layer.EFFECT);
         continueButton = null;
         
         // Remove the speech bubble.
         game.layerMan.remove(bubble, Layer.EFFECT);
         bubble = null;
         
         // Remove the text label.
         for (ILabel label : labelList)
             game.layerMan.remove(label, Layer.EFFECT);       
         labelList = null;
         
         // Remove the restriction board.
         game.pieceMan.clearRestrictionBoard();
         
         // Turn on tile drops.
         game.pieceMan.setTileDropOnCommit(true);   
         
         // Start the timer.        
         game.timerMan.setStopped(false);  
         
         // Reset the refactor speed.
         game.resetRefactorSpeed();
     }
     
     @Override
     protected void repeat(final Game game)
     {
         // Reset move counter.
         game.statMan.resetMoveCount();      
         
         // Reset the scores.
         game.scoreMan.resetScore();
         
         // Create the fake board.
         createBoard(game);
         
         // Fade board in.
         EntityGroup e = game.boardMan.getTiles(game.boardMan.getCells() / 2, 
                game.boardMan.getCells() - 1);            
         IAnimation a = new FadeAnimation.Builder(FadeAnimation.Type.IN, e)
                 .wait(0).duration(300).end();
         game.animationMan.add(a);
         
         // Change the piece to the dot.
         game.pieceMan.loadPiece(PieceType.DOT);    
         
         // Make sure buttons aren't visible.
         repeatButton.setVisible(false);
         if (repeatButton.isActivated() == true)
             repeatButton.setActivated(false);
         
         continueButton.setVisible(false);
         if (continueButton.isActivated() == true)
             continueButton.setActivated(false);
         
         // Menu is not shown.
         menuShown = false;
         
         // Make sure the bubble opacity is full.
         bubble.setOpacity(100);            
         
         // Piece is visible.
         game.pieceMan.getPieceGrid().setVisible(true);
     }
     
     private void createBoard(final Game game)
     {        
         // Clear it first.
         game.boardMan.clearBoard();
                 
         // Create bottom row.        
         TileEntity t = game.boardMan.createTile(0, game.boardMan.getRows() - 1, 
                 TileType.NORMAL, TileColor.RED);
         
         // Set a click action.
         t.setClickAction(new Runnable()
         {           
            public void run()
            {               
                // Fade out the bubble.            
                IAnimation f = new FadeAnimation.Builder(FadeAnimation.Type.OUT, bubble)
                        .wait(0).duration(500).end();
                game.animationMan.add(f);       
            }
         });                
                 
         game.boardMan.createTile(1, game.boardMan.getRows() - 1, 
                 TileType.NORMAL, TileColor.BLUE);
         
         game.boardMan.createTile(2, game.boardMan.getRows() - 1, 
                 TileType.NORMAL, TileColor.BLUE);
         
         game.boardMan.createTile(3, game.boardMan.getRows() - 1, 
                 TileType.NORMAL, TileColor.YELLOW);
         
         // Create second-from-bottom row.
         game.boardMan.createTile(0, game.boardMan.getRows() - 2, 
                 TileType.NORMAL, TileColor.BLUE);
         
         game.boardMan.createTile(1, game.boardMan.getRows() - 2, 
                 TileType.NORMAL, TileColor.BLUE);
         
         game.boardMan.createTile(2, game.boardMan.getRows() - 2, 
                 TileType.NORMAL, TileColor.PURPLE);
         
         game.boardMan.createTile(3, game.boardMan.getRows() - 2, 
                 TileType.NORMAL, TileColor.YELLOW);
         
         game.boardMan.setVisible(true);
     }   
 
 }
