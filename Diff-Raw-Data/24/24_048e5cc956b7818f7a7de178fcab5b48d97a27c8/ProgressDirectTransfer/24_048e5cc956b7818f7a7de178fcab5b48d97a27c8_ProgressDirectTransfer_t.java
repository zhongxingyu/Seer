 /**
    This file is part of GoldenGate Project (named also GoldenGate or GG).
 
    Copyright 2009, Frederic Bregier, and individual contributors by the @author
    tags. See the COPYRIGHT.txt in the distribution for a full listing of
    individual contributors.
 
    All GoldenGate Project is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    GoldenGate is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with GoldenGate .  If not, see <http://www.gnu.org/licenses/>.
  */
 package openr66.r66gui;
 
 import javax.swing.JEditorPane;
 import javax.swing.JProgressBar;
 
 import openr66.client.ProgressBarTransfer;
 import openr66.protocol.networkhandler.NetworkTransaction;
 import openr66.protocol.utils.R66Future;
 
 /**
  * @author Frederic Bregier
  *
  */
 public class ProgressDirectTransfer extends ProgressBarTransfer {
     private JProgressBar progressBar;
     private JEditorPane textFieldStatus;
     private boolean firstCall = true;
     private int nbBlock = 1;
    private long lastTime = System.currentTimeMillis();
    private int lastRank = 0;
     
     /**
      * @param future
      * @param remoteHost
      * @param filename
      * @param rulename
      * @param fileinfo
      * @param isMD5
      * @param blocksize
      * @param id
      * @param networkTransaction
      * @param callbackdelay
      */
     public ProgressDirectTransfer(R66Future future, String remoteHost,
             String filename, String rulename, String fileinfo, boolean isMD5,
             int blocksize, long id, NetworkTransaction networkTransaction,
             long callbackdelay, JProgressBar progressBar, JEditorPane textFieldStatus) {
         super(future, remoteHost, filename, rulename, fileinfo, isMD5,
                 blocksize, id, networkTransaction, callbackdelay);
         this.textFieldStatus = textFieldStatus;
         this.progressBar = progressBar;
         this.progressBar.setIndeterminate(true);
         this.progressBar.setValue(0);
         this.progressBar.setVisible(true);
         this.textFieldStatus.setText("Initializing transfer...");
     }
 
     /* (non-Javadoc)
      * @see openr66.client.ProgressBarTransfer#callBack(int, int)
      */
     @Override
     public void callBack(int currentBlock, int blocksize) {
         if (firstCall) {
             if (filesize != 0) {
                 this.progressBar.setIndeterminate(false);
                 nbBlock = (int) (Math.ceil(((double)filesize/(double)blocksize)));
             }
             firstCall = false;
         }
        long newtime = System.currentTimeMillis()+1;
        int sendsize = ((currentBlock-lastRank)*blocksize);
        long time = ((newtime-lastTime)*1024)/1000;
        long speedKB = sendsize/time;
         if (filesize == 0) {
            this.textFieldStatus.setText("Bytes transmitted: "+(currentBlock*blocksize)+" at "+speedKB+" KB/s");
         } else {
             this.progressBar.setValue(currentBlock*100/nbBlock);
            this.textFieldStatus.setText("Bytes transmitted: "+(currentBlock*blocksize)+
                    " on "+filesize+" at "+speedKB+" KB/s");
         }
        lastTime = newtime-1;
        lastRank = currentBlock;
     }
 
     /* (non-Javadoc)
      * @see openr66.client.ProgressBarTransfer#lastCallBack(boolean, int, int)
      */
     @Override
     public void lastCallBack(boolean success, int currentBlock, int blocksize) {
         this.progressBar.setIndeterminate(false);
         if (filesize == 0) {
             this.textFieldStatus.setText("Finally Bytes transmitted: "+(currentBlock*blocksize)+" with Status: "+success);
         } else {
             this.progressBar.setValue(100);
             this.textFieldStatus.setText("Finally Bytes transmitted: "+(currentBlock*blocksize)+" with Status: "+success);
         }
     }
 }
