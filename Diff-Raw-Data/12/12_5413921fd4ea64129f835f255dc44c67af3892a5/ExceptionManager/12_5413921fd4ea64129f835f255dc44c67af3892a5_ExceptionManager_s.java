 package com.cocosw.framework.exception;
 
 import android.content.Context;
 import android.widget.Toast;
 import com.cocosw.framework.app.CocoApp;
 import com.cocosw.framework.log.Log;
 
 import java.util.logging.Handler;
 
 /**
  * ExcetionManager is a exception router in UI layer.
  *
  *
  */
 public class ExceptionManager {
 
 	private static final int TOAST_DISPLAY_TIME = 4000;
 
     /**
      * preset handler will display exception msg as toast bar
      */
     public static final ExceptionHandler toastHandler = new ExceptionHandler() {
         @Override
         public boolean exception(Exception e, Context ctx, Object source) throws CocoException {
             Toast.makeText(ctx, e.getMessage(), ExceptionManager.TOAST_DISPLAY_TIME).show();
             return false;
         }
     };
 
     /**
      * preset handler will only log the exception
      */
     public static final ExceptionHandler logHandler = new ExceptionHandler() {
         @Override
         public boolean exception(Exception e, Context ctx, Object source) throws CocoException {
             Log.e(e);
             return false;
         }
     };
 
     private static ExceptionHandler handler = logHandler;
 
 
 
 
     public static void handle(final Exception e, final Context source)
 			throws CocoException {
            handle(e,(Context)source,source);
 	}
 
     public static void handle(final Exception e, final Context ctx,final Object source) throws CocoException {
 
         if (handler!=null && handler.exception(e,ctx,source))
             return;
         if (e instanceof CocoException) {
             throw (CocoException) e;
         }
         throw new CocoException(ErrorCode.WRONG_PASSWORD, "发生未知错误，稍后重试", e);
     }
 
     public static void setHandler(ExceptionHandler handler) {
         ExceptionManager.handler = handler;
     }
 
     public interface ExceptionHandler {
 
         /**
          * Handle logic exception in UI layer,
          * return true will interupt the default exception process, which means UI will not show any messages.
          *
          * @param e
          * @param ctx
          * @param source
          * @return
          * @throws CocoException
          */
         boolean exception(final Exception e, final Context ctx,final Object source) throws CocoException;
     }
 }
