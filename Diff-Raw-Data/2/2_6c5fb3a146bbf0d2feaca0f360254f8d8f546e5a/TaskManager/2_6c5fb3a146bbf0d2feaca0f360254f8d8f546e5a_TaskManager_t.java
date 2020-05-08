 package info.micdm.ftr.async;
 
 import info.micdm.ftr.utils.Log;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 /**
  * Менеджер задач.
  * Нужен для управления задачами при пересоздании экранов.
  * @author Mic, 2011
  *
  */
 public class TaskManager {
 
 	/**
 	 * Для сигналов о выполненных задачах.
 	 * @author Mic, 2011
 	 *
 	 */
 	public interface OnTaskFinished {
 		public void callback(Object result);
 	}
 	
 	/**
 	 * Для сигналов об отмененных задачах.
 	 * @author Mic, 2011
 	 *
 	 */
 	public interface OnTaskCancelled {
 		public void callback();
 	}
 	
 	/**
 	 * Обрабатываемая в данный момент задача.
 	 */
 	protected Task<?, ?> _task;
 	
 	/**
 	 * Будем показывать диалог, пока выполняется задача.
 	 */
 	protected ProgressDialog _dialog;
 	
 	public TaskManager(Context context) {
 		_setupProgressDialog(context);
 	}
 	
 	/**
 	 * Настраивает прогресс-диалог.
 	 */
 	protected void _setupProgressDialog(Context context) {
 		_dialog = new ProgressDialog(context);
 		_dialog.setMessage("Loading");
		_dialog.setCancelable(false);
 		_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				Log.debug("cancelling dialog");
 				if (_task != null) {
 					_task.cancel(false);
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Отсоединяет задачу и забывает про нее.
 	 */
 	protected void _disconnectTask() {
 		_task.setCallbacks(null, null);
 		_task = null;
 	}
 	
 	/**
 	 * Возвращает объект для обратного вызова при завершении задачи.
 	 */
 	protected Task.OnFinished _getOnFinishedCallback(final OnTaskFinished onFinished) {
 		return new Task.OnFinished() {
 			@Override
 			public void callback(Object result) {
 				Log.debug("task successfully finished");
 				_disconnectTask();
 				_dialog.dismiss();
 				if (onFinished != null) {
 					onFinished.callback(result);
 				}
 			}
 		};
 	}
 	
 	/**
 	 * Возвращает объект для обратного вызова при отмене задачи.
 	 */
 	protected Task.OnCancelled _getOnCancelledCallback(final OnTaskCancelled onCancelled) {
 		return new Task.OnCancelled() {
 			@Override
 			public void callback() {
 				Log.debug("task cancelled");
 				_disconnectTask();
 				_dialog.dismiss();
 				if (onCancelled != null) {
 					onCancelled.callback();
 				}
 			}
 		};
 	}
 	
 	/**
 	 * Выполняет задачу.
 	 */
 	public void run(Task task, OnTaskFinished onFinished, OnTaskCancelled onCancelled) {
 		Log.debug("executing task " + task.toString());
 		if (_task != null) {
 			Log.warning("can't run the task, there is another one already");
 			return;
 		}
 		_task = task;
 		_dialog.setMessage(task.getDescription());
 		_dialog.show();
 		task.setCallbacks(_getOnFinishedCallback(onFinished), _getOnCancelledCallback(onCancelled));
 		task.run();
 	}
 	
 	/**
 	 * Выполняет задачу.
 	 */
 	public void run(Task task, OnTaskFinished onFinished) {
 		run(task, onFinished, null);
 	}
 	
 	/**
 	 * Отменяет задачу, если она выполняется.
 	 */
 	public void cancel() {
 		if (_task != null) {
 			_disconnectTask();
 		}
 	}
 }
