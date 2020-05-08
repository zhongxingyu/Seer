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
 
     public XGradualTaskExecutor(B bean) {
         super(bean);
     }
 
     @Override
     public boolean start() {
         if (getStatus() != XTaskBean.STATUS_TODO
                 && getStatus() != XTaskBean.STATUS_ERROR)
             return false;
 
         // 先修改状态，再调用自定义方法onStart
         int oldStatus = getStatus();
         setStatus(XTaskBean.STATUS_STARTING);
 
         if (!onStart()) {
             setStatus(oldStatus);// 启动失败，恢复成以前的状态
             return false;
         }
 
         notifyStart(getBean());
         return true;
     }
 
     @Override
     public boolean pause() {
         if (getStatus() != XTaskBean.STATUS_DOING
                 && getStatus() != XTaskBean.STATUS_STARTING)
             return false;
 
         if (!onPause())
             return false;
 
         setStatus(XTaskBean.STATUS_PAUSING);
         // PAUSING算是DOING的一种特殊状态，所以调用onDoing()来回调监听
         notifyDoing(getBean(), -1);
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
         notifyAbort(getBean());
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
        notifyEndError(getBean(), errorCode, retry);
        return true;
    }

     public boolean startFinish() {
         if (getStatus() != XTaskBean.STATUS_STARTING)
             return false;
 
         setStatus(XTaskBean.STATUS_DOING);
         notifyDoing(getBean(), -1);
         return true;
     }
 
 
     public boolean pauseFinish() {
         if (getStatus() != XTaskBean.STATUS_PAUSING)
             return false;
 
         setStatus(XTaskBean.STATUS_TODO);
         notifyPause(getBean());
         return true;
     }
 }
