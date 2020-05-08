 package com.xengine.android.base.task;
 
 /**
  * <pre>
  * 渐变式任务执行器的抽象类。
  * 相比于XBaseTaskExecutor，在TODO和DOING之间多了2个渐变状态:STARTING和PAUSING。
  * 适用于启动和暂停时需要长时间异步操作的任务
  * 注意:
  * 1.状态机：有六个状态TODO,DOING,DONE,ERROR,STARTING,PAUSING
  *           和七个行为start,pause,abort,endSuccess,endError
  *      start = TODO/ERROR -> STARTING
  *      startFinish = STARTING -> DOING
  *      pause = DOING/STARTING -> PAUSING
  *      pauseFinish = PAUSING -> TODO
  *      abort = TODO/DOING/STARTING -> DONE
  *      endSuccess = DOING -> DONE
  *      endError = DOING/STARTING -> ERROR
  * 2.子类继承时，重写五个行为的回调方法即可：
  *      onStart(),onPause(),onAbort(),onEndSuccess(),onEndError
  * User: jasontujun
  * Date: 13-9-27
  * Time: 上午10:03
  * </pre>
  */
 public abstract class XGradualTaskExecutor<B extends XTaskBean>
         extends XBaseTaskExecutor<B> {
 
    private Integer mPrePauseStatus;// 暂停后的外部设置值
     private Integer mPostPauseStatus;// 暂停后的外部设置值
 
     public XGradualTaskExecutor(B bean) {
         super(bean);
     }
 
     @Override
    public boolean start(int... preStatus) {
         if (getStatus() != XTaskBean.STATUS_TODO
                 && getStatus() != XTaskBean.STATUS_ERROR
                 && (preStatus.length == 0
                 || getStatus() != preStatus[0]))
             return false;
 
         if (preStatus.length > 0) {
             mPrePauseStatus = preStatus[0];
         }
         if (!onStart()) // 启动失败，直接结束
             return false;
 
         // 可能在onStart()中已经调用了startFinish(),就不用回调了
         if (getStatus() != XTaskBean.STATUS_DOING) {
             setStatus(XTaskBean.STATUS_STARTING);
             if (getListener() != null)
                 getListener().onStart(getBean());
         }
         return true;
     }
 
     @Override
    public boolean pause(int... postStatus) {
         if (getStatus() != XTaskBean.STATUS_DOING
                 && getStatus() != XTaskBean.STATUS_STARTING)
             return false;
 
         if (postStatus.length > 0) {
             mPostPauseStatus = postStatus[0];
         }
         if (!onPause())// 暂停失败，直接结束
             return false;
 
         // 可能在onPause()中已经调用了pauseFinish(),就不用设置状态和回调了
         if (getStatus() != XTaskBean.STATUS_TODO &&
                 (postStatus.length == 0 || getStatus() != postStatus[0])) {
             setStatus(XTaskBean.STATUS_PAUSING);
             // PAUSING算是DOING的一种特殊状态，所以调用onDoing()来回调监听
             notifyDoing(-1);
         }
         return true;
     }
 
     @Override
    public boolean abort() {
         if (getStatus() != XTaskBean.STATUS_TODO
                 && getStatus() != XTaskBean.STATUS_DOING
                 && getStatus() != XTaskBean.STATUS_STARTING)
             return false;
 
         if (!onAbort())
             return false;
 
         setStatus(XTaskBean.STATUS_DONE);
         if (getListener() != null)
             getListener().onAbort(getBean());
         return true;
     }
 
     @Override
    public boolean endError(String errorCode, boolean retry) {
         if (getStatus() != XTaskBean.STATUS_DOING
                 && getStatus() != XTaskBean.STATUS_STARTING)
             return false;
 
         if (!onEndError(errorCode, retry))
             return false;
 
         setStatus(XTaskBean.STATUS_ERROR);
         if (getListener() != null)
             getListener().onError(getBean(), errorCode, retry);
         return true;
     }
 
     /**
      * 启动完成时调用此方法，此方法会把状态改成STATUS_DOING。
      * 注意:此方法可以在onStart()中调用，也可在异步线程中调用。
      * @return
      */
    public boolean startFinish() {
         if (getStatus() != XTaskBean.STATUS_TODO &&
                 getStatus() != XTaskBean.STATUS_ERROR &&
                 getStatus() != XTaskBean.STATUS_STARTING
                 && (mPrePauseStatus != null && getStatus() != mPrePauseStatus))
             return false;
 
         mPrePauseStatus = null;
         if (getStatus() == XTaskBean.STATUS_STARTING) {
             // 在start()之后调用startFinish()
             setStatus(XTaskBean.STATUS_DOING);
             notifyDoing(-1);
         } else {
             // 在start()中调用startFinish()，直接
             setStatus(XTaskBean.STATUS_DOING);
             if (getListener() != null)
                 getListener().onStart(getBean());
         }
         return true;
     }
 
     /**
      * 暂停完成时调用此方法，此方法会把状态改成STATUS_TODO。
      * 注意:此方法可以在onPause()中调用，也可在异步线程中调用。
      * @return
      */
    public boolean pauseFinish() {
         if (getStatus() != XTaskBean.STATUS_PAUSING &&
                 getStatus() != XTaskBean.STATUS_DOING &&
                 getStatus() != XTaskBean.STATUS_STARTING)
             return false;
 
         if (mPostPauseStatus != null) {
             setStatus(mPostPauseStatus);
             mPostPauseStatus = null;
         } else {
             setStatus(XTaskBean.STATUS_TODO);
         }
         if (getListener() != null)
             getListener().onPause(getBean());
         return true;
     }
 }
