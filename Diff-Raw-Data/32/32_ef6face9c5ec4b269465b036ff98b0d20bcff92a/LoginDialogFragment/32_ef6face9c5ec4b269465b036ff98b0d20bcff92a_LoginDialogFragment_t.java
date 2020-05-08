 
 package ru.rutube.RutubeApp.ui.dialog;
 import ru.rutube.RutubeApp.R;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 /**
  *
  * Вызов диалога Авторизации, обязательно объявлять слушателей на события
  *
  * LoginDialogFragment.InputDialogFragmentBuilder inputDialogFragmentBuilder =
  *           new LoginDialogFragment.InputDialogFragmentBuilder(this);
  *
  *   inputDialogFragmentBuilder
  *          .setTitle("Авторизация")
  *          .onFinishInputDialog(new LoginDialogFragment.OnDoneListener() {
  *                  @Override
  *                  public void onFinishInputDialog(String emailText, String passwordText) {
  *
  *                  }
  *          })
  *          .show();
  *
  *
  */
public class LoginDialogFragment extends DialogFragment implements TextView.OnEditorActionListener,
        DialogInterface.OnClickListener, View.OnClickListener, View.OnFocusChangeListener {
 
     private static LoginDialogFragment newInstance(String title,
                                                    OnClickListener onClickListener,
                                                    OnCancelListener onCancelListener,
                                                    OnDoneListener onDoneListener) {
 
         LoginDialogFragment dialogFragment = new LoginDialogFragment();
         dialogFragment.onClickListener = onClickListener;
         dialogFragment.onCancelListener = onCancelListener;
         dialogFragment.onDoneListener = onDoneListener;
 
         Bundle args = new Bundle();
         args.putString(BUNDLE_TITLE, title);
         dialogFragment.setArguments(args);
 
         return dialogFragment;
     }
 
     public static void dismiss(FragmentActivity activity) {
         FragmentManager fragmentManager = activity.getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
         Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
         if (prev != null) {
             fragmentTransaction.remove(prev);
         }
         fragmentTransaction.commit();
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         Bundle args = getArguments();
 
         View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_login, null);
         txtEmailInput = (EditText) view.findViewById(R.id.email);
         txtEmailInput.setOnEditorActionListener(this);
         txtEmailInput.setOnFocusChangeListener(this);
 
         txtPasswordInput = (EditText) view.findViewById(R.id.password);
         txtPasswordInput.setOnEditorActionListener(this);
         txtPasswordInput.setOnFocusChangeListener(this);
 
         Resources resources = getActivity().getResources();
 
         TextView tv = (TextView)view.findViewById(R.id.registerUrl);
         tv.setText(Html.fromHtml(String.format(resources.getString(R.string.register_url),
                 resources.getString(R.string.registration))));
         tv.setMovementMethod(LinkMovementMethod.getInstance());
 
         tv = (TextView)view.findViewById(R.id.remindUrl);
         tv.setText(Html.fromHtml(String.format(resources.getString(R.string.remind_url),
                 resources.getString(R.string.remind_password))));
         tv.setMovementMethod(LinkMovementMethod.getInstance());
 
         view.findViewById(R.id.btnClear).setOnClickListener(this);
         builder.setView(view);
         builder.setTitle(args.getString(BUNDLE_TITLE));
         builder.setNeutralButton(getActivity().getString(android.R.string.ok), this);
         builder.setCancelable(true);
         return builder.create();
     }
 
     @Override
     public void onClick(DialogInterface dialog, int which) {
         if (onClickListener != null) {
             onClickListener.onClick(dialog, which);
         } else {
            onDoneListener.onFinishInputDialog(txtEmailInput.getText().toString(),
                    txtPasswordInput.getText().toString());
             this.dismiss();
         }
     }
 
     @Override
     public void onClick(View v) {
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        }
        dismiss();
     }
 
     @Override
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         if (onDoneListener != null) {
             if (EditorInfo.IME_ACTION_DONE == actionId) {
                onDoneListener.onFinishInputDialog(txtEmailInput.getText().toString(),
                        txtPasswordInput.getText().toString());
                 this.dismiss();
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public void onFocusChange(View v, boolean hasFocus) {
         if (hasFocus) {
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
         }
     }
 
     @Override
     public void onCancel(DialogInterface dialog) {
         if (onCancelListener != null) {
             onCancelListener.onCancel();
         }
     }
 
     public void onClearText(View view) {
         txtEmailInput.setText("");
     }
 
     public interface OnCancelListener {
         public void onCancel();
     }
 
     public interface OnDoneListener {
         void onFinishInputDialog(String emailText, String passwordText);
     }
 
     public static class InputDialogFragmentBuilder {
         private FragmentActivity mActivity;
         private String mTitle;
         private OnClickListener mOnClickListener;
         private OnCancelListener mOnCancelListener;
         private OnDoneListener mOnDoneListener;
 
         public InputDialogFragmentBuilder(FragmentActivity activity) {
             mActivity = activity;
         }
 
         public InputDialogFragmentBuilder setTitle(int resId) {
             mTitle = mActivity.getString(resId);
             return this;
         }
 
         public InputDialogFragmentBuilder setTitle(String text) {
             mTitle = text;
             return this;
         }
 
         public InputDialogFragmentBuilder setOnClickListener(OnClickListener onClickListener) {
             mOnClickListener = onClickListener;
             return this;
         }
 
         public InputDialogFragmentBuilder setOnCancelListener(OnCancelListener onCancelListener) {
             mOnCancelListener = onCancelListener;
             return this;
         }
 
         public InputDialogFragmentBuilder setOnDoneListener(OnDoneListener mOnDoneListener) {
             this.mOnDoneListener = mOnDoneListener;
             return this;
         }
 
         public void show() {
             FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
             Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
             if (prev != null) {
                 fragmentTransaction.remove(prev);
             }
             fragmentTransaction.addToBackStack(null);
 
            LoginDialogFragment.newInstance(mTitle, mOnClickListener, mOnCancelListener,
                    mOnDoneListener)
                     .show(fragmentManager, FRAGMENT_TAG);
         }
     }
 
     private static final String FRAGMENT_TAG = "InputDialogFragment";
     private static final String BUNDLE_TITLE = "title";
 
     private OnClickListener onClickListener;
     private OnCancelListener onCancelListener;
     private OnDoneListener onDoneListener;
     private EditText txtEmailInput;
     private EditText txtPasswordInput;
 }
