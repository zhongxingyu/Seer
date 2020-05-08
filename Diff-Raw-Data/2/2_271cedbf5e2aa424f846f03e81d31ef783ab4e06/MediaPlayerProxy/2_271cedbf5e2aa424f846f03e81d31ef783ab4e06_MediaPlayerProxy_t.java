 import android.media.MediaPlayer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by CN_fox on 13-10-14.
  */
 public class MediaPlayerProxy {
     private MediaPlayer player;
     private static MediaPlayerProxy proxy = new MediaPlayerProxy();
     private List<PlayState> list;
     private PlayState mState;
 
     private MediaPlayerProxy(){
         player = new MediaPlayer();
         list = new ArrayList<PlayState>();
         player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
             @Override
             public void onPrepared(MediaPlayer mediaPlayer) {
                 player.start(); //准备结束后，开始播放
                 if (mState!=null)
                     mState.play();
             }
         });
 
         player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mediaPlayer) {
                 player.stop();//播放结束
                 if (mState!=null)
                     mState.stop();
             }
         });
 
     }
 
     public static MediaPlayerProxy getProxy(){
         return proxy;
     }
 
     /**
      * 播放(停止)语音
      * @param state 播放对象接口
      * @param path 播放路径
      */
     public void play(PlayState state,String path){
         this.mState = state;
         PlayState mState = null;
         for (PlayState s:list){
             if (s.getState() == PlayState.PLAY){
                 mState = s;
                 break;
             }
         }
 
         if (mState!=null){
             stop();
             mState.stop();
             mState.setState(PlayState.NORMAL);//停止后，再
             if (mState.equals(state))//再次点击还是自己，说明是想停止播放
                 return;
         }
         play(path); //实际播放
         //在遍历完成后，再设置状态
         state.setState(PlayState.PLAY);
 
     }
 
     /**
      * 实际播放的方法
      * @param path
      */
     private void play(String path){
         boolean err = false;
         try {
             player.setDataSource(path);
        } catch (Exception e) {
             err = true;
             e.printStackTrace();
         }finally {
             if (err){
                 player.reset();
                 try {
                     player.setDataSource(path);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         player.prepareAsync(); //异步的准备
     }
 
     /**
      * 实际的停止方法
      */
     private void stop(){
         player.reset();
     }
 
     /**
      * 强制停止方法，用于界面跳转，或特殊场景
      */
     public void forceStop(){
         PlayState mState = null;
         for (PlayState s:list){
             if (s.getState() == PlayState.PLAY){
                 mState = s;
                 break;
             }
         }
 
         if (mState!=null){
             stop();
             mState.stop();
             mState.setState(PlayState.NORMAL);
         }
     }
 
     /**
      * 添加后才可以播放
      * @param state
      */
     public void add(PlayState state){
         list.add(state);
     }
 
     public void clear(){
         list.clear();
     }
 
     public interface PlayState{
         /**
          * 播放
          */
         public static final int PLAY = 1;
         /**
          * 未播放
          */
         public static final int NORMAL = 0;
 
         /**
          * 这个方法为了停止动画
          */
         void stop();
 
         /**
          * 这个方法为了开启动画
          */
         void play();
 
         /**
          * 获取当前item的播放状态
          * @return
          */
         int getState();
 
         /**
          * 设置播放状态
          * @param state
          */
         void setState(int state);
     }
 }
