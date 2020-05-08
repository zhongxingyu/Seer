 package ru.rutube.RutubePlayer.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.MediaController;
 import android.widget.ProgressBar;
 
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 
 import java.io.IOException;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.models.Constants;
 import ru.rutube.RutubePlayer.R;
 import ru.rutube.RutubePlayer.ctrl.PlayerController;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 03.05.13
  * Time: 20:14
  * To change this template use File | Settings | File Templates.
  */
 public class PlayerFragment extends Fragment implements PlayerController.PlayerView {
 
     /**
      * Интерфейс общения с активити, в которое встроен фрагмент с плеером
      */
     public interface PlayerStateListener {
 
         /**
          * Событие начала воспроизведения
          */
         public void onPlay();
 
         /**
          * Событие окончания воспроизведения
          */
         public void onComplete();
 
         /**
          * Событие невозможности воспроизведения
          */
         public void onFail();
     }
     private static final String CONTROLLER = "controller";
     private static final String LOG_TAG = PlayerFragment.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
 
     protected PlayerController mController;
     protected SurfaceView mVideoView;
     protected MediaPlayer mPlayer;
     protected Uri mStreamUri;
     protected ProgressBar mLoadProgressBar;
     protected PlayerStateListener mPlayerStateListener;
     protected RutubeMediaController mMediaController;
     protected int mBufferingPercent = 0;
     protected boolean mPrepared = false;
 
     protected PowerManager.WakeLock mWakeLock;
     private Dialog mDialog;
 
 
     /**
      * Обработчик закрытия диалога сообщения об ошибке
      */
     protected DialogInterface.OnDismissListener mErrorListener = new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialogInterface) {
             if (mPlayerStateListener != null)
                 mPlayerStateListener.onFail();
         }
     };
 
 
     /**
      * Обработчики различных интерфейсов, необходимые, чтобы заставить MediaPlayer показывать на
      * SurfaceView видео под управлением MediaController
      */
 
 
     protected SurfaceHolder.Callback mSurfaceCallbackListener = new SurfaceHolder.Callback() {
         @Override
         public void surfaceCreated(SurfaceHolder surfaceHolder) {
             if (D) Log.d(LOG_TAG, "surfaceCreated");
             mPlayer.setDisplay(surfaceHolder);
         }
 
         @Override
         public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
             if (D) Log.d(LOG_TAG, "surfaceChanged");
             mPlayer.setDisplay(surfaceHolder);
         }
 
         @Override
         public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
             if (D) Log.d(LOG_TAG, "surfaceDestroyed");
         }
     };
 
     protected MediaController.MediaPlayerControl mMediaPlayerControl = new MediaController.MediaPlayerControl() {
         @Override
         public void start() {
             mPlayer.start();
         }
 
         @Override
         public void pause() {
             mPlayer.pause();
         }
 
         @Override
         public int getDuration() {
             return mPlayer.getDuration();
         }
 
         @Override
         public int getCurrentPosition() {
             return mPlayer.getCurrentPosition();
         }
 
         @Override
         public void seekTo(int millis) {
             mPlayer.seekTo(millis);
         }
 
         @Override
         public boolean isPlaying() {
             return mPlayer.isPlaying();
         }
 
         @Override
         public int getBufferPercentage() {
             return mBufferingPercent;
         }
 
         @Override
         public boolean canPause() {
             return true ;
         }
 
         @Override
         public boolean canSeekBackward() {
             return true;
         }
 
         @Override
         public boolean canSeekForward() {
             return true;
         }
 
     };
 
     protected MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
 
         @Override
         public void onCompletion(MediaPlayer mediaPlayer) {
             if (D) Log.d(LOG_TAG, "OnCompletion");
             mController.onCompletion();
             onComplete();
         }
     };
 
     protected MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
         @Override
         public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
             if (D) Log.d(LOG_TAG, "onError");
             mController.onPlaybackError();
             return true;
         }
     };
 
 
     protected MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
         @Override
         public void onPrepared(MediaPlayer mediaPlayer) {
             if (D) Log.d(LOG_TAG, "OnPrepared");
             mController.onViewReady();
             mMediaController.setMediaPlayer(mMediaPlayerControl);
             if (D) Log.d(LOG_TAG, "Prepared!");
             mPrepared = true;
         }
     };
 
     protected MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
 
         @Override
         public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
             mBufferingPercent = percent;
 
         }
     };
 
     protected View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
 
         @Override
         public boolean onTouch(View view, MotionEvent motionEvent) {
             if (D) Log.d(LOG_TAG, "onTouch");
             if (!mPrepared) {
                 if (D) Log.d(LOG_TAG, "preparing, don't show media controller");
                 return true;
             } else {
                 if (D) Log.d(LOG_TAG, "MP is prepared");
             }
             mMediaController.show();
             return true;
         }
     };
 
     protected MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChanged = new MediaPlayer.OnVideoSizeChangedListener() {
 
         @Override
         public void onVideoSizeChanged(MediaPlayer mediaPlayer, int w, int h) {
             int surfaceView_Width = mVideoView.getWidth();
             int surfaceView_Height = mVideoView.getHeight();
 
             float ratio_width = surfaceView_Width/ (float) w;
             float ratio_height = surfaceView_Height/ (float) h;
             float aspectratio = (float) w / (float) h;
 
             ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
             assert layoutParams != null;
             if (ratio_width > ratio_height){
                 layoutParams.width = (int) (surfaceView_Height * aspectratio);
                 layoutParams.height = surfaceView_Height;
             }else{
                 layoutParams.width = surfaceView_Width;
                 layoutParams.height = (int) (surfaceView_Width / aspectratio);
             }
 
             mVideoView.setLayoutParams(layoutParams);
 
         }
     };
 
     //
     // переопределенные методы из Fragment
     //
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         if (D) Log.d(LOG_TAG, "onActivityCreated");
         init(savedInstanceState);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         if (D) Log.d(LOG_TAG, "onCreateView");
         View view = inflater.inflate(R.layout.player_fragment, container, false);
         assert view != null;
         view.setOnTouchListener(mOnTouchListener);
         return view;
     }
 
     @Override
     public void onResume() {
         if (D) Log.d(LOG_TAG, "onResume");
         super.onResume();
         // Плеер может быть уже проинициализирован в onCreateView
         // и точно уничтожается в onPause
         if (mPlayer == null)
             initMediaPlayer();
         mController.onResume();
         mMediaController.setMediaPlayer(mMediaPlayerControl);
         mWakeLock.acquire();
     }
 
     private void initMediaPlayer() {
         if (D) Log.d(LOG_TAG, "initMediaPlayer");
         mPrepared = false;
         if (mPlayer != null)
             mPlayer.release();
         mPlayer = new MediaPlayer();
         // когда вызывается initMediaPlayer, SurfaceView еще может быть недоступна.
         // Поэтому setDisplay вызывается в соответствующем callack-е SurfaceView
         // mPlayer.setDisplay(mVideoView.getHolder());
         mPlayer.setOnPreparedListener(mOnPreparedListener);
         mPlayer.setOnCompletionListener(mOnCompletionListener);
         mPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
         mPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChanged);
         mPlayer.setOnErrorListener(mOnErrorListener);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         if (D) Log.d(LOG_TAG, "onPause");
         mController.onPause();
         mPlayer.reset();
         // невыполнение release загоняет плеер в ошибочное состояние, которое выливается в ошибку
         // Error (1, -110), После release плеер необходимо инициализировать заново.
         mPlayer.release();
         mPlayer = null;
         // messageHandler после детача получает очередное сообщение о прогрессе и пытается вызвать
         // у деинициализированного плеера getDuration. Результат - ISE/NPE.
         mMediaController.setMediaPlayer(null);
         if (mWakeLock.isHeld())
             mWakeLock.release();
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         if (D) Log.d(LOG_TAG, "Controller detached");
         mController.detach();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         if (D) Log.d(LOG_TAG, "onSaveInstanceState");
         outState.putParcelable(CONTROLLER, mController);
     }
 
     //
     // Реализация интерфейса PlayerController.PlayerView
     //
 
     @Override
     public int getCurrentOffset() {
         return mPlayer.getCurrentPosition();
     }
 
     @Override
     public void stopPlayback() {
         mPlayer.stop();
 
 //        mVideoView.setVideoURI(null);
 //        mVideoView.stopPlayback();
     }
 
 
 
     @Override
     public void seekTo(int millis) {
         if (D) Log.d(LOG_TAG, "Seek To: " + String.valueOf(millis / 1000));
         mPlayer.seekTo(millis);
 //        mVideoView.seekTo(millis);
     }
 
     @Override
     public void pauseVideo() {
         mPlayer.pause();
 //        mVideoView.pause();
     }
 
     @Override
     public void onComplete() {
         if (mPlayerStateListener != null) {
             if (D) Log.d(LOG_TAG, "onComplete");
             mPlayerStateListener.onComplete();
         }
         stopPlayback();
         toggleMediaController(false);
     }
 
     @Override
     public void setStreamUri(Uri uri) {
         if (D) Log.d(LOG_TAG, "setStreamUri " + String.valueOf(uri));
         setVideoUri(uri);
         mStreamUri = uri;
     }
 
     @Override
     public void showError(String error) {
         Activity activity = getActivity();
         if (activity == null)
             return;
         AlertDialog.Builder builder = new AlertDialog.Builder(activity);
         mDialog = builder.
                 setTitle(android.R.string.dialog_alert_title).
                 setMessage(error).
                 create();
         mDialog.setOnDismissListener(mErrorListener);
         mDialog.show();
     }
 
     @Override
     public void startPlayback() {
         if (D) Log.d(LOG_TAG, "StartPlayback");
         if (mPlayerStateListener != null)
             mPlayerStateListener.onPlay();
         startVideoPlayback();
     }
     @Override
     public void setLoading() {
         mLoadProgressBar.setVisibility(View.VISIBLE);
     }
 
     @Override
     public void setLoadingCompleted() {
         mLoadProgressBar.setVisibility(View.GONE);
     }
 
     @Override
     public void toggleThumbnail(boolean visible) {
         int visibility = (visible)? View.VISIBLE : View.INVISIBLE;
         View view = getView();
         assert view != null;
         View thumbnail = view.findViewById(R.id.thumbnail);
         thumbnail.setVisibility(visibility);
     }
 
     @Override
     public void setVideoTitle(String title) {
 
     }
 
     @Override
     public void setThumbnailUri(Uri uri) {
         View view = getView();
         assert view != null;
         NetworkImageView netImgView = (NetworkImageView) view.findViewById(R.id.thumbnail);
         ImageLoader imageLoader = mController.getImageLoader();
         if (imageLoader == null)
             throw new NullPointerException("no image loader");
         netImgView.setImageUrl(uri.toString(), imageLoader);
     }
 
     //
     // Собственные публичные методы
     //
 
     public PlayerController getController() {
         return mController;
     }
 
     public Dialog getDialog() {
         return mDialog;
     }
 
     /**
      * Повторное воспроизведение видео
      */
     public void replay() {
         mController.replay();
     }
 
 
     /**
      * Инициализирует обрабочтик событий PlayerStateListener
      * @param playerStateListener контейнер фрагмента, обрабатывающий события
      */
     public void setPlayerStateListener(PlayerStateListener playerStateListener) {
         mPlayerStateListener = playerStateListener;
     }
 
     /**
      * Меняет видимость элементов управления плеером
      * @param visible true если необходимо сделать элементы управления видимыми
      */
     protected void toggleMediaController(boolean visible) {
         if (visible)
             mMediaController.show();
         else
             mMediaController.hide();
     }
 
     /**
      * Инициализация видеоэлемента
      */
     protected void initVideoView() {
         View view = getView();
         assert view != null;
         mLoadProgressBar = (ProgressBar) view.findViewById(R.id.load);
         mVideoView = (SurfaceView) view.findViewById(R.id.video_view);
         SurfaceHolder holder = mVideoView.getHolder();
         assert holder != null;
         holder.addCallback(mSurfaceCallbackListener);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         mMediaController = new RutubeMediaController(getActivity());
         //mVideoView.setMediaController(mMediaController);
         //mMediaController.setMediaPlayer();
         mMediaController.setAnchorView((FrameLayout) view.findViewById(R.id.center_video_view));
         initMediaPlayer();
 //        mVideoView.setOnCompletionListener(this);
 //        mVideoView.setOnPreparedListener(this);
     }
 
     /**
      * Задание Uri видеопотока для видеоэлемента
      * @param uri Uri видеопотока
      */
     protected void setVideoUri(Uri uri) {
         if (uri != null)
             try {
                 if (mPlayer == null)
                     initMediaPlayer();
                 mPlayer.reset();
                 mPrepared = false;
                 if (D) Log.d(LOG_TAG, "Preparing!");
                 mPlayer.setDataSource(getActivity(), uri);
                 mPlayer.prepareAsync();
                 mBufferingPercent = 0;
             } catch (IOException e) {
                 e.printStackTrace();
             }
 //            mVideoView.setVideoURI(uri);
     }
 
     /**
      * Начинает воспроизведение видео
      */
     protected void startVideoPlayback() {
         if (D) Log.d(LOG_TAG, "startVideoPlayback");
         //mVideoiew.setVideoURI(mStreamUri);
         mPlayer.start();
 //        mVideoView.start();
     }
 
     /**
      * Инициализация логики плеера
      * @param savedInstanceState сохраненное состояние активити
      */
     private void init(Bundle savedInstanceState) {
         mStreamUri = null;
         Activity activity = getActivity();
         assert activity != null;
         Intent intent = activity.getIntent();
         Uri videoUri = intent.getData();
         Uri thumbnailUri = intent.getParcelableExtra(Constants.Params.THUMBNAIL_URI);
         PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
         mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "VideoPlayer");
 
         initVideoView();
         mController = null;
         if (savedInstanceState != null) {
             mController = savedInstanceState.getParcelable(CONTROLLER);
         }
         if (mController == null) {
             mController = new PlayerController(videoUri, thumbnailUri);
         }
         mController.attach(activity, this);
         if (savedInstanceState == null)
             mController.requestStream();
     }
 }
