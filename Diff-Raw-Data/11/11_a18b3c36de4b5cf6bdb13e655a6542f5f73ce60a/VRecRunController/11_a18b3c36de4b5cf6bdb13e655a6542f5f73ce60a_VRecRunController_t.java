 package code.controllers;
 
 import code.gui.control.VRecRunPane;
 import code.model.VFileManager;
 import code.model.recorder.VSequence;
 import java.awt.Component;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 /**
  *
  * @author Jose Carlos
  */
 public class VRecRunController extends VControlCenterController
 {
     private VRecRunPane pane;
     
     private JButton rec;
     private JButton play;
     private JButton stop;
     private JButton loadSecuenceButton;
     private JLabel fileName;
     
     private VSequence sequence;
     private boolean isRecording;
     private boolean isReproducing;
 
     public boolean isSecuenceSet()
     {
         return this.sequence != null;
     }
     
     public VRecRunController(VRecRunPane pane) 
     {
         this.pane = pane;
         this.rec = pane.getRec();
         this.play = pane.getPlay();
         this.stop = pane.getStop();
         this.loadSecuenceButton = pane.getLoadSecuence();
         this.fileName = pane.getFileName();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) 
     {
         Component clicked = e.getComponent();
         if (clicked.isEnabled())
         {
             if (clicked.equals(this.rec))
             {
                 if (loadInstrument())
                 {
                    setRunningState(true);
                    createSequence();
                    this.isRecording = true;
                 }
             }
             else if (clicked.equals(this.play))
             {
                 if (loadInstrument())
                 {
                     setRunningState(true);
                     this.sequence.reproduce();
                     this.isReproducing = true;
                 }
             }
             else if (clicked.equals(this.stop))
             {
                 if (unloadInstrument())
                 {
                    setRunningState(false);
                     if (this.isRecording)
                     {
                        File saved = null;
                         if (!this.sequence.getRecords().isEmpty())
                         {
                            saved = VFileManager.instance.saveSecuence(this.sequence);
                         }
                        this.sequence = saved == null ? null : this.sequence;
                         this.isRecording = false;
                         this.player.stopRecording();
                         loadSequence();
                     }
                     else if (this.isReproducing)
                     {
                         this.sequence.stop();
                         this.isReproducing = false;
                     }
                     this.railBoard.clear();
                 }
             }
             else if (clicked.equals(this.loadSecuenceButton))
             {
                 this.sequence = VFileManager.instance.readSequence();
                 loadSequence();
             }
         }
     }
 
     private void createSequence() 
     {
         this.sequence = new VSequence(this.instrumentLoaded, System.currentTimeMillis());
         this.player.startRecording(this.sequence);
     }
     
     private void loadSequence()
     {
         if (isSecuenceSet())
         {
             this.builder.getController().loadInstrument(sequence.getInstrument());
             this.play.setEnabled(true);
         }
         else
         {
             this.play.setEnabled(false);
         }
         updateFileName();
     }
     
     private void setRunningState(boolean running)
     {
         this.stop.setEnabled(running);
         this.play.setEnabled(!running);
         this.rec.setEnabled(!running);
         this.loadSecuenceButton.setEnabled(!running);
     }
     
     private void updateFileName()
     {
         this.pane.remove(this.fileName);
         this.fileName = isSecuenceSet() ? new JLabel(sequence.getName()) :
                                           new JLabel("No sequence loaded");
         this.pane.add(this.fileName);
     }
 }
