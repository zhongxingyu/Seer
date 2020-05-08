 package cz.encircled.eplayer.view;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.Timer;
 
 import javax.swing.*;
 
 import cz.encircled.eplayer.util.GUIUtil;
 import cz.encircled.eplayer.view.componensts.PlayerControls;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import cz.encircled.eplayer.app.Application;
 import cz.encircled.eplayer.app.LocalizedMessages;
 import cz.encircled.eplayer.app.MessagesProvider;
 import cz.encircled.eplayer.model.Playable;
 import cz.encircled.eplayer.view.componensts.QuickNaviButton;
 import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
 import uk.co.caprica.vlcj.player.MediaPlayer;
 import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
 import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
 
 public class Frame extends JFrame implements Runnable {
 
     private static final long serialVersionUID = 1L;
 
     private static final String TITLE = "EPlayer";
 
     private EmbeddedMediaPlayerComponent mediaPlayerComponent;
 
     private EmbeddedMediaPlayer player;
 
     private PlayerControls playerControls;
 
     private JPanel wrapper;
 
     private JPanel naviPanel;
 
     private boolean isFullScreen = false;
 
     private String current;
 
     private volatile int wrapperState = -1;
 
     private final static int QUICK_NAVI_STATE = 0;
 
     private final static int PLAYER_STATE = 1;
 
     private final Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                             new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor");
 
     private JMenuBar jMenuBar;
 
     private final static Logger log = LogManager.getLogger(Frame.class);
 
     public Frame() {
         try {
             UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
         }
         catch (Exception e){
             log.warn("l&f failed with msg {}", e.getMessage());
         }
         initialize();
         initializeWrapper();
         initializeMenu();
         initializeQuickNavi();
         if(Application.getInstance().isVlcAvailable()){
             initializePlayer();
         } else {
             disablePlayerFunctionality();
         }
         GUIUtil.bindKey(wrapper, null, 'o', ActionCommands.OPEN);
         GUIUtil.bindKey(wrapper, null, 'n', ActionCommands.OPEN_QUICK_NAVI);
         GUIUtil.bindKey(wrapper, null, 'q', ActionCommands.EXIT);
         GUIUtil.bindKey(wrapper, null, 'f', ActionCommands.TOGGLE_FULL_SCREEN);
         GUIUtil.bindKey(wrapper, null, 'c', ActionCommands.PLAY_LAST);
         GUIUtil.bindKey(wrapper, KeyConstants.ENTER, null, ActionCommands.PLAY_LAST);
         GUIUtil.bindKey(wrapper, KeyConstants.SPACE, null, ActionCommands.TOGGLE_PLAYER);
         GUIUtil.bindKey(wrapper, KeyConstants.ESCAPE, null, ActionCommands.BACK);
     }
 
 
     public final void play(String path, long time){
         if(!showPlayerInternal()){
             JOptionPane.showMessageDialog(Frame.this, MessagesProvider.get(LocalizedMessages.MSG_VLC_LIBS_FAIL),
                                             MessagesProvider.get(LocalizedMessages.ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
             return;
         }
         if(!path.equals(current)){
        	current = path;
        	player.prepareMedia(new File(path).toURI().toASCIIString().replaceFirst("file:/", "file:///"));
         }
         player.start();
         player.setTime(Math.min(time, player.getLength() - 1000));
         playerControls.reinitialize();
     }
 
     public void releasePlayer(){
         if(player != null){
             player.stop();
             player.release();
         }
     }
 
     public void stopPlayer(){
         if(player != null){
             updateCurrentPlayableInCache();
             player.stop();
         }
     }
 
     public void updateCurrentPlayableInCache(){
         if(current != null && player.getTime() >= 0L)
             Application.getInstance().updatePlayableCache(current.hashCode(), player.getTime());
     }
 
     public void pausePlayer(){
         if(player.isPlaying()){
             player.pause();
             updateCurrentPlayableInCache();
         }
     }
 
 
     public void togglePlayer() {
         if(player != null && current != null){
             if(player.isPlaying())
                 pausePlayer();
             else
                 player.start();
         }
     }
 
     public void showQuickNavi(){
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 if(wrapperState != QUICK_NAVI_STATE){
                     if(isFullScreen)
                         exitFullScreenInternal(false);
                     else
                         stopPlayer();
                     wrapperState = QUICK_NAVI_STATE;
                     wrapper.removeAll();
                     wrapper.add(naviPanel, BorderLayout.CENTER);
                     wrapper.repaint();
                     repaintQuickNavi();
                 }
             }
         });
 
     }
 
     public void showPlayer(){
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 showPlayerInternal();
             }
         });
     }
 
     private boolean showPlayerInternal(){
         if(!Application.getInstance().isVlcAvailable())
             return false;
         if(wrapperState != PLAYER_STATE){
             wrapperState = PLAYER_STATE;
             wrapper.removeAll();
             wrapper.add(mediaPlayerComponent, BorderLayout.CENTER);
             wrapper.add(playerControls, BorderLayout.SOUTH);
             wrapper.repaint();
             jMenuBar.repaint();
         }
         return true;
     }
 
     public void repaintQuickNavi(){
         final java.util.List<QuickNaviButton> naviButtons = new ArrayList<QuickNaviButton>();
         Map<Integer, Playable> data = Application.getInstance().getPlayableCache();
         for(Entry<Integer, Playable> e : data.entrySet())
             naviButtons.add(new QuickNaviButton(e.getValue()));
 
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 naviPanel.removeAll();
                 naviPanel.revalidate();
                 for(final QuickNaviButton b : naviButtons)
                     naviPanel.add(b);
                 naviPanel.repaint();
             }
         });
 
     }
 
     public void fullScreen(){
         final long time = player.getTime();
         log.debug("time is {}", time);
         player.stop();
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 dispose();
                 setExtendedState(JFrame.MAXIMIZED_BOTH);
                 setUndecorated(true);
                 setVisible(true);
                 toFront();
                 setJMenuBar(null);
                 playerControls.setVisible(false);
                 setCursor(blankCursor);
                 play(current, time);
                 isFullScreen = true;
             }
         });
     }
 
     public void exitFullScreen(){
         exitFullScreen(player.isPlaying());
     }
 
     public void exitFullScreen(final boolean continuePlaying){
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 exitFullScreenInternal(continuePlaying);
             }
         });
     }
 
     private void exitFullScreenInternal(boolean continuePlaying){
         final long time = player.getTime();
         stopPlayer();
         dispose();
         setUndecorated(false);
         setExtendedState(JFrame.MAXIMIZED_BOTH);
         setVisible(true);
         setCursor(Cursor.getDefaultCursor());
         setJMenuBar(jMenuBar);
         toFront();
         playerControls.setVisible(true);
         if(continuePlaying)
             play(current, time);
         isFullScreen = false;
     }
 
     public void toggleFullScreen() {
         if(player != null && current != null){
             if(isFullScreen)
                 exitFullScreen();
             else
                 fullScreen();
         }
     }
 
     public boolean isQuickNaviState(){
         return wrapperState == QUICK_NAVI_STATE;
     }
 
     public boolean isPlayerState(){
         return wrapperState == PLAYER_STATE;
     }
 
     public boolean isFullScreen(){
         return isFullScreen;
     }
 
     @Override
     public void run() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 setVisible(true);
             }
         });
     }
 
     private void initialize(){
 
         setTitle(TITLE);
         setPreferredSize(new Dimension(1000, 700));
         setExtendedState(Frame.MAXIMIZED_BOTH);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         getContentPane().setLayout(new BorderLayout());
     }
 
     private void initializeWrapper(){
         wrapper = new JPanel(new BorderLayout());
         getContentPane().add(wrapper, BorderLayout.CENTER);
         addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if(playerControls != null){
                     if(e.getWheelRotation() < 0){
                         playerControls.setVisible(true);
                         if(isFullScreen)
                             setCursor(Cursor.getDefaultCursor());
                     } else{
                         playerControls.setVisible(false);
                         if(isFullScreen)
                             setCursor(blankCursor);
                     }
                 }
             }
         });
     }
 
     private void initializePlayer(){
         try {
             mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
             player = mediaPlayerComponent.getMediaPlayer();
             player.setEnableMouseInputHandling(false);
             player.setEnableKeyInputHandling(false);
 
             mediaPlayerComponent.getVideoSurface().addMouseListener(new MouseAdapter() {
 
                 private Timer pauseTimer;
 
                 private int tasksCount = 0;
 
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     super.mouseClicked(e);
                     if(e.getClickCount() == 1){
                         if(tasksCount == 0){
                             pauseTimer = new Timer();
                             pauseTimer.schedule(new TimerTask() {
                                 @Override
                                 public void run() {
                                     togglePlayer();
                                     tasksCount--;
                                 }
                             }, 200);
                             tasksCount++;
                         }
                     } else {
                         if(tasksCount > 0){
                             pauseTimer.cancel();
                             tasksCount = 0;
                         }
                         toggleFullScreen();
                     }
                 }
             });
 
             player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                 @Override
                 public void finished(MediaPlayer mediaPlayer) {
                     super.finished(mediaPlayer);
                     log.debug("FINISHED");
                     Application.getInstance().updatePlayableCache(current.hashCode(), 0L);
                     current = null;
                     showQuickNavi();
                 }
 
                 private long lastTime = -501L;
 
                 @Override
                 public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                     super.timeChanged(mediaPlayer, newTime);
                     if (Math.abs(newTime - lastTime) > 500L) {
                         playerControls.fireTimeChanged(newTime);
                         lastTime = newTime;
                     }
                 }
 
                 @Override
                 public void error(MediaPlayer mediaPlayer) {
                     JOptionPane.showMessageDialog(Frame.this, "Failed to open " + current, "Error", JOptionPane.ERROR_MESSAGE);
                     Application.getInstance().deletePlayableCache(current.hashCode());
                     current = null;
                     showQuickNavi();
                 }
 
             });
             playerControls = new PlayerControls(player);
         } catch(Exception e){
             e.printStackTrace();
             JOptionPane.showMessageDialog(Frame.this, "VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
         } catch (NoClassDefFoundError re){
             re.printStackTrace();
         }
     }
 
     private void initializeMenu(){
         jMenuBar = new JMenuBar();
         JMenu file = new JMenu(MessagesProvider.get(LocalizedMessages.FILE));
         JMenu tools = new JMenu(MessagesProvider.get(LocalizedMessages.TOOLS));
 
         tools.add(Components.getMenuItem(LocalizedMessages.OPEN_QUICK_NAVI, ActionCommands.OPEN_QUICK_NAVI));
         tools.add(Components.getMenuItem(LocalizedMessages.SETTINGS, ActionCommands.SETTINGS));
 
         file.add(new JSeparator());
         file.add(Components.getMenuItem(LocalizedMessages.OPEN, ActionCommands.OPEN));
         file.add(new JSeparator());
         file.add(Components.getMenuItem(LocalizedMessages.EXIT, ActionCommands.EXIT));
         file.add(new JSeparator());
 
         setJMenuBar(jMenuBar);
         jMenuBar.add(file);
         jMenuBar.add(tools);
     }
 
     private void initializeQuickNavi(){
         naviPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
         naviPanel.setBackground(Color.WHITE);
     }
 
     private void disablePlayerFunctionality(){
 
     }
 
 
}
