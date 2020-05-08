 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package twitchapplication;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.Timer;
 import javax.swing.*;
 
 /**
  *
  * @author Toby
  */
 public class ListPanel extends javax.swing.JPanel {
 
     private TwitchController twc;
     private Timer timer;
     
     private int counter; //used to determine if we should notify
     private int timeout = 30;
     
     private boolean popoutVideo;
     
     private ArrayList<String> internalTracker = new ArrayList<>();
     private ArrayList<String> internalOffline;
     private ArrayList<Streamer> internalOnline;
     
     // icons
     private final ImageIcon downIcon = new ImageIcon(getClass().getResource("/res/down.png"));
     private final ImageIcon upIcon = new ImageIcon(getClass().getResource("/res/up.png"));
     
     // font
     private final java.awt.Font scrollerFont = new java.awt.Font("Tahoma", 1, 11);
     
     /**
      * Creates new form ListPanel
      *
      * @param twc A twitch controller reference
      */
     public ListPanel(final TwitchController twc) {
         this.twc = twc;
         initComponents();
         offlinePanel.setPreferredSize(new Dimension(200, 90));
         onlinePanel.setPreferredSize(new Dimension(200, 90));
     }
 
     /**
      * Populate the "online"/"offline" panels
      *
      * @param list An arraylist with the streamers
      */
     public void generateLists(ArrayList<Streamer> list) {
         internalOnline = new ArrayList<>();
         internalOffline = new ArrayList<>();
         int i;
         for(i = 0; i < list.size(); i++){
             if(list.get(i).isStatus()){
                 internalOnline.add(list.get(i)); // add to online list
                 if(!internalTracker.contains(list.get(i).getStreamerName())){ // if this online user wasn't already on the tracker
                     internalTracker.add(list.get(i).getStreamerName()); // add to tracker
                     if(counter>0){ // do not notify user first time!
                         String message = list.get(i).getStreamerName() +" just went online!";
                         twc.showMessage(1, message);
                         twc.trayNotify(message);
                     } else {
                         twc.showOnline(internalOnline);
                     }
                 }
             } else {
                 internalOffline.add(list.get(i).getStreamerName());
             }
         }
         Iterator it = internalTracker.iterator();
         ArrayList<String> internalOnlineTrack = new ArrayList<>();
         for(Streamer strx : internalOnline){
             internalOnlineTrack.add(strx.getStreamerName());
         }
         while(it.hasNext()){
             String str = (String)it.next();
             if(!internalOnlineTrack.contains(str)){
                 it.remove();
                 twc.showMessage(1, str + " went offline.");
                 twc.trayNotify(str + " went offline.");
             }
             
         }
         if (timer == null) {
             timer = new Timer();
             timer.scheduleAtFixedRate(new TimerTask() {
                 @Override
                 public void run() {
                     if(pauseSelect.isSelected()){
                         return;
                     }
                     class MyWorker extends SwingWorker<String, Object> {
 
                         @Override
                         protected String doInBackground() {
                             progressBar.setIndeterminate(true);
                             twc.showMessage(2, "");
                             twc.update();
                             System.gc();
                             return "Done.";
                         }
 
                         @Override
                         protected void done() {
                             progressBar.setIndeterminate(false);
                         }
                     }
                     new MyWorker().execute();
                 }
             }, timeout * 1000, timeout * 1000);
         }
         
         drawOnline(0);
         drawOffline(0);
        twc.setTrayTooltip(internalOnline.size() + " streamers online.");
         counter++;
     }
 
     /*
      * Stop the timer and cancel future tasks.
      */
     public void timerStop() {
         if (timer != null) {
             timer.cancel();
             timer.purge();
             timer = null;
         }
     }
     
     public void resetCounter(){
         counter = 0;
     }
     
     public ArrayList<Streamer> getOnline(){
         if(internalOnline != null && !internalOnline.isEmpty()){
             return internalOnline;
         }
         return null;
     }
     
     private void drawOnline(int x){
         GridLayout blayOnline = new GridLayout(7, 1);
         onlinePanel.setLayout(blayOnline);
         onlinePanel.removeAll();
         // the x indicates black position start.
         if(x!=0){ // are we at the top?
                 JLabel upLabel = new JLabel("Go Up");
                 upLabel.setFont(scrollerFont);
                 upLabel.addMouseListener(new MoveListEntry(0, x-5));
                 upLabel.setIcon(upIcon);
                 onlinePanel.add(upLabel);
         }
         int i = x; // make sure index is right from the start
         int j = 0; // entry counter
         int limit = 5;
         if(i>0) limit = 4; //if i is greater than 0, we need to break at 4 instead of 5, because black position start takes top.
         for(; i<internalOnline.size(); i++){
             String url = "http://www.twitch.tv/" + internalOnline.get(i).getStreamerName();
             url = url + (popoutVideo ? "/popout" : "");
 
             URLLabel label = generateJLabel(internalOnline.get(i).getStreamerName() + " (" + internalOnline.get(i).getViewers() + ")", true);
             label.setURL(url);
             onlinePanel.add(label);
             j++; //label added, increment entry counter
 
             if (j == limit && (internalOnline.size()>5+x)){ // first is if we're at 'limit' its time to go down AND second if we've done 'limit' and there's more stuff to draw
                 JLabel downLabel = new JLabel("Go down");
                 downLabel.addMouseListener(new MoveListEntry(0, x+5)); // increase black entry by 5
                 downLabel.setFont(scrollerFont);
                 downLabel.setIcon(downIcon);
                 onlinePanel.add(downLabel);
                 break;
             }
         }
         onlinePanel.repaint();
         onlinePanel.revalidate();
     }
     
     private void drawOffline(int x){
         GridLayout blayOffline = new GridLayout(7, 1);
         offlinePanel.setLayout(blayOffline);
         offlinePanel.removeAll();
         // the x indicates black position start.
         if(x!=0){ // are we at the top?
                 JLabel upLabel = new JLabel("Go Up");
                 upLabel.setFont(scrollerFont);
                 upLabel.addMouseListener(new MoveListEntry(1, x-5));
                 upLabel.setIcon(upIcon);
                 offlinePanel.add(upLabel);
         }
         int i = x;
         int j = 0;
         int limit = 5;
         if(i>0) limit = 4; //if i is greater than 0, we need to break at 4 instead of 5, because black entry takes top.
         for (; i < internalOffline.size(); i++) {
             String url = "http://www.twitch.tv/" + internalOffline.get(i);
             url = url + (popoutVideo ? "/popout" : "");
 
             URLLabel label = generateJLabel(internalOffline.get(i), false);
             label.setURL(url);
             offlinePanel.add(label);
             j++;
 
             if (j == limit && (internalOffline.size()>5+x)){ // first is if we're at 'limit' its time to go down , second if we've done 'limit' and there's more stuff to draw
                 JLabel downLabel = new JLabel("Go down");
                 downLabel.addMouseListener(new MoveListEntry(1, x+5));
                 downLabel.setFont(scrollerFont);
                 downLabel.setIcon(downIcon);
                 offlinePanel.add(downLabel);
                 break;
             }
         }
         offlinePanel.repaint();
         offlinePanel.revalidate();
     }
     
     public void setTimer(int timer){
         this.timeout = timer;
     }
     
    private class MoveListEntry extends MouseAdapter {
         private int positionStart; // black entry index
         private int list; // 0 for online, 1 for offline
         public MoveListEntry(int list, int posStart){
             this.list = list;
             positionStart = posStart;
         }
         @Override
         public void mouseClicked(MouseEvent e) {
             if(list==0){
                 drawOnline(positionStart);
             } else {
                 drawOffline(positionStart);
             }
         }
     }
 
     public void setPopoutVideo(boolean popoutVideo) {
         this.popoutVideo = popoutVideo;
     }
     
     public void redrawList(){
         if(internalOnline != null && internalOffline != null){
             drawOffline(0);
             drawOnline(0);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         logoutButton = new javax.swing.JButton();
         onlinePanel = new javax.swing.JPanel();
         offlinePanel = new javax.swing.JPanel();
         progressBar = new javax.swing.JProgressBar();
         pauseSelect = new javax.swing.JCheckBox();
         testLabel = new javax.swing.JLabel();
 
         logoutButton.setText("Logout");
         logoutButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 logoutButtonActionPerformed(evt);
             }
         });
 
         onlinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Online", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 10), new java.awt.Color(0, 0, 0))); // NOI18N
 
         javax.swing.GroupLayout onlinePanelLayout = new javax.swing.GroupLayout(onlinePanel);
         onlinePanel.setLayout(onlinePanelLayout);
         onlinePanelLayout.setHorizontalGroup(
             onlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 181, Short.MAX_VALUE)
         );
         onlinePanelLayout.setVerticalGroup(
             onlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 58, Short.MAX_VALUE)
         );
 
         offlinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Offline", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 10), new java.awt.Color(0, 0, 0))); // NOI18N
 
         javax.swing.GroupLayout offlinePanelLayout = new javax.swing.GroupLayout(offlinePanel);
         offlinePanel.setLayout(offlinePanelLayout);
         offlinePanelLayout.setHorizontalGroup(
             offlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         offlinePanelLayout.setVerticalGroup(
             offlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 58, Short.MAX_VALUE)
         );
 
         pauseSelect.setText("Pause Update");
 
         testLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         testLabel.setText("         ");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(onlinePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(offlinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(testLabel))
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(logoutButton)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(pauseSelect)))
                         .addGap(0, 88, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(logoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(pauseSelect))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(onlinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(offlinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(testLabel)
                 .addGap(28, 28, 28))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
         twc.setContentPanel(0);
     }//GEN-LAST:event_logoutButtonActionPerformed
 
     private URLLabel generateJLabel(String text, boolean online) {
         URLLabel genLabel = new URLLabel();
         genLabel.setText(text);
         if (online) {
             genLabel.setIcon(new ImageIcon(getClass().getResource("/res/accept.png")));
         } else {
             genLabel.setIcon(new ImageIcon(getClass().getResource("/res/cross.png")));
         }
         genLabel.setVisible(true);
         return genLabel;
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton logoutButton;
     private javax.swing.JPanel offlinePanel;
     private javax.swing.JPanel onlinePanel;
     private javax.swing.JCheckBox pauseSelect;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JLabel testLabel;
     // End of variables declaration//GEN-END:variables
 }
