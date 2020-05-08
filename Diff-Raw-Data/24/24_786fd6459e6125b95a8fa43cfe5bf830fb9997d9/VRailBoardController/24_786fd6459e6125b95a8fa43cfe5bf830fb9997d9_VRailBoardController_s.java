 package code.controllers;
 
 import code.gui.railboard.VAnt;
 import code.gui.railboard.VRailBoard;
 import code.model.instruments.VInstrument;
 import code.model.instruments.VKey;
 import code.model.recorder.VSequence;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 /**
  *
  * @author Jose Carlos
  */
 public class VRailBoardController extends VAbstractController implements KeyListener
 {
     private VRailBoard railBoard;
     
     private VRailController railController;
     private VInstrumentPanelController instrumentPanelController;
     
     public VRailBoardController(VRailBoard panel) 
     {
         this.railBoard = panel;
         this.railController = this.railBoard.getRail().getController();
         this.instrumentPanelController = this.railBoard.getInstrumentPanel().getController();
     }
     
     public void loadInstrument(VInstrument toLoad)
     {
         this.instrumentPanelController.loadInstrument(toLoad);
     }
     
     public void unloadInstrument()
     {
         this.instrumentPanelController.unloadInstrument();
     }
     
     public void setInstrumentRecorder(VSequence recorder)
     {
         this.instrumentPanelController.setRecorder(recorder);
    }            
     
     @Override
     public void keyTyped(KeyEvent e) 
     {
     }
 
     @Override
     public void keyPressed(KeyEvent e) 
     {
         VKey key = new VKey(e.getKeyCode(), String.valueOf(e.getKeyChar()));
        VAnt toRun = this.instrumentPanelController.playInstrument(key);
        railController.runAnt(toRun);
     }
 
     @Override
     public void keyReleased(KeyEvent e)
     {
     }
 }
