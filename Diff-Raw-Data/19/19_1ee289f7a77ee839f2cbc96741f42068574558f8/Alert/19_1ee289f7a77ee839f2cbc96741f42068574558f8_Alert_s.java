 package karyon.android.controls;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.KeyEvent;
 import android.view.View;
 import karyon.Utilities;
 import karyon.android.R;
 import karyon.android.activities.IActivity;
 
 /**
  * The alert class allows gathering of feedback from the user
  */
 public class Alert
     extends DialogFragment
 {
     public static class ButtonInfo
     {
         private int m_nTextResource;
         private DialogInterface.OnClickListener m_oListener;
 
         public ButtonInfo(int tnTextResourceID)
         {
             m_nTextResource = tnTextResourceID;
             m_oListener = new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i)
                     {
                         // Do nothing by default
                     }
                 };
         }
 
         public ButtonInfo(int tnTextResourceID, DialogInterface.OnClickListener toOnClickListener)
         {
             Utilities.checkParameterNotNull("toOnClickListener", toOnClickListener);
 
             m_nTextResource = tnTextResourceID;
             m_oListener = toOnClickListener;
         }
     }
 
     private abstract class DialogBuilder<T extends Dialog>
     {
         public abstract T build();
     }
 
     private class BasicDialogBuilder
         extends DialogBuilder<Dialog>
     {
         @Override
         public Dialog build()
         {
             AlertDialog.Builder loBuilder = new AlertDialog.Builder(m_oContext != null ? m_oContext : getActivity());
             if (getActivity() != null)
             {
                 View loView = getActivity().getLayoutInflater().inflate(R.layout.view_dialog_alert, null);
                 loBuilder.setView(loView);
                 m_oTitle = (android.widget.TextView)loView.findViewById(R.id.txtContent);
                 m_oTitle.setText(m_nTitle);;
             }
 
             if (m_oPositive != null)
             {
                 loBuilder.setPositiveButton(m_oPositive.m_nTextResource, m_oPositive.m_oListener);
             }
 
             if (m_oNeutral != null)
             {
                 loBuilder.setNeutralButton(m_oNeutral.m_nTextResource, m_oNeutral.m_oListener);
             }
 
             if (m_oNegative != null)
             {
                 loBuilder.setNegativeButton(m_oNegative.m_nTextResource, m_oNegative.m_oListener);
             }
 
             return loBuilder.create();
         }
     }
 
     private class ProgressDialogBuilder
         extends DialogBuilder<ProgressDialog>
     {
         @Override
         public ProgressDialog build()
         {
             ProgressDialog loReturn = new ProgressDialog(getActivity());
             loReturn.setMessage(getString(m_nTitle));
             loReturn.setIndeterminate(true);
             loReturn.setCancelable(false);
 
             // Disable the back button for now
             DialogInterface.OnKeyListener loListener = new DialogInterface.OnKeyListener()
             {
                 @Override
                 public boolean onKey(DialogInterface toDialog, int tnKeyCode, KeyEvent toEvent)
                 {
                     return tnKeyCode == KeyEvent.KEYCODE_BACK;
                 }
             };
             return loReturn;
         }
     }
 
     public static Alert createAlert(int tnTitleResourceID, ButtonInfo toPositiveButton)
     {
         Alert loReturn = new Alert();
         loReturn.m_nTitle = tnTitleResourceID;
         loReturn.m_oPositive = toPositiveButton;
         loReturn.m_oBuilderClass = BasicDialogBuilder.class;
 
         return loReturn;
     }
 
     public static Alert createAlert(int tnTitleResourceID, ButtonInfo toPositiveButton, ButtonInfo toNegativeButton)
     {
         Alert loReturn = new Alert();
         loReturn.m_nTitle = tnTitleResourceID;
         loReturn.m_oPositive = toPositiveButton;
         loReturn.m_oNegative = toNegativeButton;
         loReturn.m_oBuilderClass = BasicDialogBuilder.class;
 
         return loReturn;
     }
 
     public static Alert createAlert(int tnTitleResourceID, ButtonInfo toPositiveButton, ButtonInfo toNegativeButton, ButtonInfo toNeutralButton)
     {
         Alert loReturn = new Alert();
         loReturn.m_nTitle = tnTitleResourceID;
         loReturn.m_oPositive = toPositiveButton;
         loReturn.m_oNegative = toNegativeButton;
         loReturn.m_oNeutral = toNeutralButton;
         loReturn.m_oBuilderClass = BasicDialogBuilder.class;
 
         return loReturn;
     }
 
     public static Alert createProgress(int tnProgressTextID)
     {
         Alert loReturn = new Alert();
         loReturn.m_nTitle = tnProgressTextID;
         loReturn.m_oBuilderClass = ProgressDialogBuilder.class;
 
         return loReturn;
     }
 
     private int m_nTitle;
     private Context m_oContext;
     private ButtonInfo m_oPositive;
     private ButtonInfo m_oNegative;
     private ButtonInfo m_oNeutral;
     private Class<? extends DialogBuilder> m_oBuilderClass;
     private android.widget.TextView m_oTitle;
     private String m_cTag;
 
 
    private Alert()
     {
     }
 
     /**
      * Constructor used for test cases
      * @param toContext the context to run this dialog in
      * @param tnTitleResourceID the title resource id
      */
     protected Alert(Context toContext, int tnTitleResourceID)
     {
         m_oContext = toContext;
         m_nTitle = tnTitleResourceID;
         m_oBuilderClass = BasicDialogBuilder.class;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle toSavedInstanceState)
     {
         DialogBuilder loBuilder = null;
         // TODO: need to figure out how to dynamically create an instance class
         if (m_oBuilderClass == BasicDialogBuilder.class)
         {
             loBuilder = new BasicDialogBuilder();
         }
         else if (m_oBuilderClass == ProgressDialogBuilder.class)
         {
             loBuilder = new ProgressDialogBuilder();
         }
 
         return loBuilder != null ? loBuilder.build() : null;
     }
 
     /**
      * Shows the dialog using the parent activity as the parent context
      * @param toParentActivity the activity that is creating this dialog
      * @return the tag assigned to the dialog
      */
     public String show(final IActivity toParentActivity)
     {
         if (m_cTag == null)
         {
             m_oContext = toParentActivity.getContext();
             m_cTag = Utilities.generateGUID();
             show(toParentActivity.getSupportFragmentManager(), m_cTag);
         }
         return m_cTag;
     }
 
     /**
      * Updates the text for the dialog
      * @param tcText the text to update to
      */
     public void setText(String tcText)
     {
         if (m_oTitle != null)
         {
             m_oTitle.setText(tcText);
         }
     }
 }
