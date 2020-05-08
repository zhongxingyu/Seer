 package com.amay077.android.mvvm;
 
 import java.util.Map;
 
 import com.amay077.android.mvvm.CallbackStore.OnActivityResultCallback;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /**
  * ベースアクティビティクラス
  *
  */
 public abstract class BaseFragmentActivity<T extends BaseViewModel> extends FragmentActivity {
 	private final BaseActivityAdapter<T> _adapter;
 
 	public BaseFragmentActivity() {
 		_adapter = new BaseActivityAdapter<T>(this, getViewModelType()) {
 			@Override
 			protected void onBindViewModel(T vm) {
 				BaseFragmentActivity.this.onBindViewModel(vm);
 			}
 
 			@Override
 			protected void onBindViewMenu(T vm, Map<Integer, Command> menuMap) {
 				BaseFragmentActivity.this.onBindMenu(vm, menuMap);
 			}
 			
 		};
 	}
 
     protected abstract void onBindViewModel(T vm);
 	protected void onBindMenu(T vm, Map<Integer, Command> menuMap) { };
 	
 	protected abstract Class<T> getViewModelType();
     protected int getMenuResId() { return 0; };
 
     /**
      * このアプリの Application クラスを取得する 
      */
 	public CallbackStore getApp() {
     	return _adapter.getApp();
     }
 	
 	public T getViewModel() {
 		return _adapter.getViewModel();
 	}
 
     /**
      * 戻り時の Action を指定して、画面遷移する 
      */
 	public void startActivityWithResultAction(Context packageContext, Class<?> cls,
 			OnActivityResultCallback resultAction) {
 		_adapter.startActivityWithResultAction(packageContext, cls, resultAction);
 	}
 	
     /**
      * パラメータを指定して画面遷移する 
      */
 	public void startActivityWithParam(Context packageContext, Class<?> cls, 
 			Parcelable param) {
 		_adapter.startActivityWithParam(packageContext, cls, param);
 	}
 
     /**
      * パラメータと、戻り時の Action を指定して、画面遷移する 
      */
 	public void startActivityWithParamAndResultAction(Context packageContext, Class<?> cls,
 			Parcelable param, OnActivityResultCallback resultAction) {
 		_adapter.startActivityWithParamAndResultAction(
 				packageContext, cls, param, resultAction);
 	}
 
     /**
      * Intent と、戻り時の Action を指定して、画面遷移する 
      */
 	protected final void startActivityWithIntentAndResultAction(Intent intent, 
 			OnActivityResultCallback resultAction) {
 		_adapter.startActivityWithIntentAndResultAction(intent, resultAction);
 	}
 
 	/**
 	 * 戻り時のパラメータを設定する。
 	 */
 	protected void setOkResult(Parcelable result) {
 		_adapter.setOkResult(result);
 	}
 
 	/**
 	 * Activity からの戻り時に、startActivityWithResultAction で渡された Action を呼び出す。
 	 * 
 	 * startActivityWithResultAction を実行した際、渡された Action をマップに追加します(Key は連番=requestCode)。
 	 * その requestCode から Action を特定し、Invoke します。
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		
 		if (_adapter.hasFormatableActivityResult(requestCode, resultCode, data)) {
 			_adapter.onActivityResult(requestCode, resultCode, data);
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	/**
 	 * Dialog インスタンスを直接指定してダイアログを表示する。
 	 * 
 	 * Android のライフサイクルでは、showDialog(id) ⇢ onCreateDialog(id) 
 	 * ⇢ dissmissDialog(id) の流れに沿わなければならないが、
 	 * この方法では「ダイアログを表示させる処理の付近に、そのコールバックを書く」事ができず、
 	 * 処理が分散してしまい見づらい。
 	 * showDialogWithDialog(dialog) は直接 Dialog のインスタンスを指定して呼び出せる。
 	 * 内部では、渡された dialog を hashCode をキーにして管理し、onCreateDialog にて呼び出している。
 	 * 但し、setOnDismissListener は、このメソッドにより上書きされる。
 	 */
 	protected void showDialogWithDialog(AlertDialog dialog, final DialogMessage2 message) {
 		_adapter.showDialogWithDialog(dialog, message);
 	}
 	
 	/**
 	 * Dialog が生成される時
 	 * 
 	 * id が _dialogMap で管理されている値(hashcode)だったら、それを返す。
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog = _adapter.onCreateDialog(id);
 		if (dialog != null) {
 			return dialog;
 		} else {
 			return super.onCreateDialog(id);
 		}
 	}
 	
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
    	_adapter.bindViewModel();
 	}
 	
 	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
 		_adapter.onPrepareDialog(id, dialog);
		super.onPrepareDialog(id, dialog, bundle);
 	}
 	
     protected void bindMenu(Map<Integer, Command> menuMap) {
     	_adapter.bindMenu(menuMap);
     }
 
 	protected void setKeepScreenOn() {
 		_adapter.setKeepScreenOn();	
 	}
 	protected void setKeepScreenOff() {
 		_adapter.setKeepScreenOff();
 	}
 	
     @Override
     protected void onResume() {
     	super.onResume();
     	_adapter.onResume();
     }
 
     @Override
     protected void onPause() {
     	super.onPause();
     	_adapter.onPause();
     }
 
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	_adapter.onDestroy();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	int menuId = getMenuResId();
     	
     	if (menuId == 0) {
     		return false;
     	}
     	
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(menuId, menu);
 	    return true;
     }
 
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (_adapter.isBindedMenu(item)) {
         	return _adapter.onOptionsItemSelected(item);
     	} else {
     		return super.onOptionsItemSelected(item);
     	}
     }
 }
