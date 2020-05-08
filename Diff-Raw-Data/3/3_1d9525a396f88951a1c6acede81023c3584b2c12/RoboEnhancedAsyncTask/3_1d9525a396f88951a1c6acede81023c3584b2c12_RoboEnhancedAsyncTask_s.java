 /*******************************************************************************
  * Copyleft 2013 Massimiliano Leone - massimiliano.leone@iubris.net .
  * 
  * RoboEnhancedAsyncTask.java is part of 'EnhancedSafeAsyncTask'.
  * 
  * 'EnhancedSafeAsyncTask' is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * 'EnhancedSafeAsyncTask' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with 'EnhancedSafeAsyncTask' ; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
  ******************************************************************************/
 package net.iubris.etask.roboguiced;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.concurrent.Executor;
 
 import net.iubris.mirror.ExceptionDispatcher;
 
 import roboguice.util.RoboAsyncTask;
 
 import android.content.Context;
 import android.os.Handler;
 /**
  * This is "Enhanced" version of RoboAsyncTask.java<br/>
  * You need roboguice.jar, javax.inject.jar and guice-3.x-no_aop.jar in order to compile it 
  * (see <a href="http://code.google.com/p/roboguice">http://code.google.com/p/roboguice</a>) 
 * and mirroringexception.jar (see <a href="http://github.com/k0smik0/mirroringexception">https://github.com/k0smik0/mirroringexception</a>)
  * 
  * @param <ResultT>
  */
 public abstract class RoboEnhancedAsyncTask<ResultT> extends RoboAsyncTask<ResultT> {
 
 	protected RoboEnhancedAsyncTask(Context context, Executor executor) {
 		super(context, executor);
 	}
 	protected RoboEnhancedAsyncTask(Context context, Handler handler, Executor executor) {
 		super(context, handler, executor);
 	}
 	protected RoboEnhancedAsyncTask(Context context, Handler handler) {
 		super(context, handler);
 	}
 	protected RoboEnhancedAsyncTask(Context context) {
 		super(context);
 	}
 	
 	@Override
 	protected final void onException(Exception e) throws RuntimeException {
 		Method m = ExceptionDispatcher.findBestMatchException(e, this, "onException");
 		try {
 			if (m.isAccessible()) {
 				m.invoke(this, e);
 			} else {
 				m.setAccessible(true);
 				m.invoke(this, e);
 				m.setAccessible(false);
 			}
 		} catch (IllegalAccessException e1) {
 		} catch (InvocationTargetException e1) {
 		} catch (NullPointerException e1) { // m is null, because no one right onException is found: so, use onGenericException
 //			e.setStackTrace(Arrays.asList(e.getStackTrace()).add( new ArrayList<StackTraceElement>().add( e1.getStackTrace() )) );
 //			StackTraceElement[] eStack = e.getStackTrace();
 			StackTraceElement[] e1Stack = e1.getStackTrace();
 			e.setStackTrace(e1Stack);
 			onGenericException(e);
 		}
 	}
 	
 	/**
 	 * printStackTrace() as default
 	 * @param e
 	 * @throws RuntimeException
 	 */
 	protected void onException(NullPointerException e) throws RuntimeException {
 		e.printStackTrace();
 	}
 	
 	protected void onGenericException(Exception e) throws RuntimeException {
 		super.onException(e);
 	}
 
 }
