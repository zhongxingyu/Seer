 package org.martus.android;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 
 /**
  * @author roms
  *         Date: 12/19/12
  */
 public class BaseActivity extends Activity implements ConfirmationDialogHandler, LoginDialogHandler {
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     public static void showLoginRequiredDialog(Activity activity) {
         DialogFragment loginDialog = LoginRequiredDialog.newInstance();
         loginDialog.show(activity.getFragmentManager(), "login");
     }
 
     public static class LoginRequiredDialog extends DialogFragment {
 
         public static LoginRequiredDialog newInstance() {
             LoginRequiredDialog frag = new LoginRequiredDialog();
             Bundle args = new Bundle();
             frag.setArguments(args);
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return new AlertDialog.Builder(getActivity())
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle("You must first login!")
                 .setMessage("Before sending this bulletin")
                 .setPositiveButton(R.string.alert_dialog_ok,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int whichButton) {
                                ((BaseActivity) getActivity()).onLoginRequiredDialogClicked();
                             }
                         }
                 )
                 .create();
         }
     }
 
     public void onLoginRequiredDialogClicked() {
         BaseActivity.this.finish();
         Intent intent = new Intent(BaseActivity.this, MartusActivity.class);
         intent.putExtras(getIntent());
         intent.putExtra(MartusActivity.RETURN_TO, MartusActivity.ACTIVITY_BULLETIN);
         intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
         startActivity(intent);
     }
 
     public static void showInstallExplorerDialog(Activity activity) {
         DialogFragment explorerDialog = InstallExplorerDialog.newInstance();
         explorerDialog.show(activity.getFragmentManager(), "install");
     }
 
     public static class InstallExplorerDialog extends DialogFragment {
 
         public static InstallExplorerDialog newInstance() {
             InstallExplorerDialog frag = new InstallExplorerDialog();
             Bundle args = new Bundle();
             frag.setArguments(args);
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             LayoutInflater inflater = getActivity().getLayoutInflater();
             return new AlertDialog.Builder(getActivity())
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setView(inflater.inflate(R.layout.install_file_explorer, null))
                 .setTitle("Try another file explorer")
                 .create();
         }
     }
 
     public static void showConfirmationDialog(Activity activity) {
         DialogFragment confirmationDialog = ConfirmationDialog.newInstance();
         confirmationDialog.show(activity.getFragmentManager(), "confirmation");
     }
 
     public static class ConfirmationDialog extends DialogFragment {
 
         public static ConfirmationDialog newInstance() {
             ConfirmationDialog frag = new ConfirmationDialog();
             Bundle args = new Bundle();
             frag.setArguments(args);
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return new AlertDialog.Builder(getActivity())
                     .setIcon(android.R.drawable.ic_dialog_alert)
                     .setTitle("Are you sure?")
                     .setPositiveButton(R.string.yes,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     ((ConfirmationDialogHandler) getActivity()).onConfirmationClicked();
                                 }
                             }
                     )
                     .setNegativeButton(R.string.no,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     ((ConfirmationDialogHandler) getActivity()).onConfirmationDenied();
                                 }
                             }
                     )
                     .create();
         }
     }
 
     public void onConfirmationClicked() {
         //do nothing
     }
 
     public void onConfirmationDenied() {
         //do nothing
     }
 }
