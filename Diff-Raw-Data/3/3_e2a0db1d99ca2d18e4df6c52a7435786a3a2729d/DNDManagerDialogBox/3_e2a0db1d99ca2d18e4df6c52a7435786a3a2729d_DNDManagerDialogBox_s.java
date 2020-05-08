 package com.webshrub.moonwalker.androidapp;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.viewpagerindicator.UnderlinePageIndicator;
 
 import static com.webshrub.moonwalker.androidapp.DNDManagerConstants.TRAI_CONTACT_NUMBER;
 
 public class DNDManagerDialogBox extends FragmentActivity {
     private static final int REPORT_SPAM_DIALOG = 0;
     private ViewPager viewPager;
     private DNDManagerItemPagerAdapter pagerAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         pagerAdapter = new DNDManagerItemPagerAdapter(this);
         showDialog(REPORT_SPAM_DIALOG);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case REPORT_SPAM_DIALOG: {
                 Dialog dialog = new Dialog(this, R.style.dialog);
                 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                 dialog.setContentView(R.layout.dialogbox);
                 dialog.setOnCancelListener(new DNDManagerOnCancelListener());
                 dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                 viewPager = (ViewPager) dialog.findViewById(R.id.pager);
                 viewPager.setAdapter(pagerAdapter);
                 UnderlinePageIndicator pageIndicator = (UnderlinePageIndicator) dialog.findViewById(R.id.pageIndicator);
                 pageIndicator.setViewPager(viewPager);
                 pageIndicator.setFades(false);
                 pageIndicator.setOnPageChangeListener(new DNDManagerOnPageChangeListener(dialog));
                 Button reportSpamButton = (Button) dialog.findViewById(R.id.reportSpam);
                 Button ignoreButton = (Button) dialog.findViewById(R.id.ignore);
                 dialog.findViewById(R.id.cancel).setOnClickListener(new CancelButtonOnClickListener());
                 if (pagerAdapter.getCount() == 0) {
                     ((TextView) dialog.findViewById(R.id.title)).setText("DND Manager " + "(Showing  0/0)");
                     dialog.findViewById(R.id.noSpamGreeting).setVisibility(View.VISIBLE);
                     reportSpamButton.setEnabled(false);
                     ignoreButton.setEnabled(false);
                 } else {
                     ((TextView) dialog.findViewById(R.id.title)).setText("DND Manager " + "(Showing  1/" + pagerAdapter.getCount() + ")");
                     reportSpamButton.setOnClickListener(new ReportSpamButtonOnClickListener(dialog));
                     ignoreButton.setOnClickListener(new IgnoreButtonOnClickListener(dialog));
                     toastMessage("Showing only last 3 day's calls and sms as per TRAI guidelines.");
                 }
                 return dialog;
             }
             default:
                 return null;
         }
     }
 
     private void toastMessage(String toastMessage) {
         Toast toast = new Toast(this);
         LayoutInflater inflater = getLayoutInflater();
         View toastRoot = inflater.inflate(R.layout.toast, null);
         toast.setView(toastRoot);
         TextView textView = (TextView) toastRoot.findViewById(R.id.toastMessage);
         textView.setText(toastMessage);
         textView.setTextColor(Color.BLACK);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
         toast.setDuration(Toast.LENGTH_LONG);
         toast.show();
     }
 
     private void refreshViewPager(Dialog dialog, int position) {
         viewPager.setAdapter(null);
         pagerAdapter = new DNDManagerItemPagerAdapter(this);
         viewPager.setAdapter(pagerAdapter);
         int pageIndex = position;
         if (pageIndex == pagerAdapter.getCount()) {
             pageIndex--;
         }
         viewPager.setCurrentItem(pageIndex);
         if (pagerAdapter.getCount() <= 0) {
             ((TextView) dialog.findViewById(R.id.title)).setText("DND Manager " + "(Showing  0/0)");
             dialog.findViewById(R.id.noSpamGreeting).setVisibility(View.VISIBLE);
             dialog.findViewById(R.id.reportSpam).setEnabled(false);
             dialog.findViewById(R.id.ignore).setEnabled(false);
         } else {
             ((TextView) dialog.findViewById(R.id.title)).setText("DND Manager " + "(Showing  " + (viewPager.getCurrentItem() + 1) + "/" + pagerAdapter.getCount() + ")");
         }
     }
 
     private void deleteDNDManagerItem(DNDManagerItem removedItem) {
         DNDManagerUtil.deleteCallLogByNumber(DNDManagerDialogBox.this, removedItem.getNumber());
         DNDManagerUtil.deleteSmsByNumber(DNDManagerDialogBox.this, removedItem.getNumber());
     }
 
     private class ReportSpamButtonOnClickListener implements View.OnClickListener {
         private Dialog dialog;
 
         public ReportSpamButtonOnClickListener(Dialog dialog) {
             this.dialog = dialog;
         }
 
         @Override
         public void onClick(View view) {
             DNDManagerItem dndManagerItem = pagerAdapter.getDNDManagerItem(viewPager.getCurrentItem());
             String dateTime = dndManagerItem.getDateTime();
             EditText editText = (EditText) viewPager.findViewWithTag(dateTime);
             String messageText = editText.getText().toString().trim();
             if (messageText.equals("")) {
                 toastMessage("Please type short description of the call/spam your received.");
             } else {
                 DNDManagerUtil.sendSMS(DNDManagerDialogBox.this, TRAI_CONTACT_NUMBER, messageText);
                 if (!DNDManagerHtmlHelper.getDeleteSentSMSFlag(DNDManagerDialogBox.this)) {
                     DNDManagerUtil.saveSentSms(DNDManagerDialogBox.this, TRAI_CONTACT_NUMBER, messageText);
                 }
                 if (DNDManagerHtmlHelper.getDeleteDNDManagerItemFlag(DNDManagerDialogBox.this)) {
                     DNDManagerItem removedItem = pagerAdapter.getDNDManagerItem(viewPager.getCurrentItem());
                     deleteDNDManagerItem(removedItem);
                     refreshViewPager(dialog, viewPager.getCurrentItem());
                 }
                 toastMessage("Your request has been submitted successfully.");
             }
         }
     }
 
     private class CancelButtonOnClickListener implements View.OnClickListener {
         @Override
         public void onClick(View view) {
             finish();
         }
     }
 
     private class IgnoreButtonOnClickListener implements View.OnClickListener {
         private Dialog dialog;
 
         public IgnoreButtonOnClickListener(Dialog dialog) {
             this.dialog = dialog;
         }
 
         @Override
         public void onClick(View view) {
             DNDManagerItem dndManagerItem = pagerAdapter.getDNDManagerItem(viewPager.getCurrentItem());
             DNDManagerIgnoredContact ignoredContact = new DNDManagerIgnoredContact();
             ignoredContact.setNumber(dndManagerItem.getNumber());
             ignoredContact.setCachedName(dndManagerItem.getCachedName());
             DNDManagerDataSource.getInstance(DNDManagerDialogBox.this).createIgnoredContact(ignoredContact);
             refreshViewPager(dialog, viewPager.getCurrentItem());
             toastMessage("Number successfully added to ignored list. You will not receive notification for this number again.");
         }
     }
 
     private class DNDManagerOnPageChangeListener implements ViewPager.OnPageChangeListener {
         private Dialog dialog;
 
         public DNDManagerOnPageChangeListener(Dialog dialog) {
             this.dialog = dialog;
         }
 
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
         }
 
         @Override
         public void onPageSelected(int position) {
             int currentIndex = position + 1;
             ((TextView) (dialog.findViewById(R.id.title))).setText("DND Manager " + "(Showing " + currentIndex + "/" + pagerAdapter.getCount() + ")");
         }
 
         @Override
         public void onPageScrollStateChanged(int state) {
         }
     }
 
     private class DNDManagerOnCancelListener implements DialogInterface.OnCancelListener {
         @Override
         public void onCancel(DialogInterface dialog) {
             removeDialog(REPORT_SPAM_DIALOG);
             finish();
         }
     }
 }
