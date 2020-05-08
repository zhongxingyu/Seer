 
 package org.clc.android.app.redbox.widget;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.text.Editable;
 import android.text.InputFilter;
 import android.text.TextWatcher;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import org.clc.android.app.redbox.R;
 import org.clc.android.app.redbox.data.BlockSetting;
 import org.clc.android.app.redbox.data.DataManager;
 
 import java.util.ArrayList;
 
 public class SmsEditWidget extends LinearLayout implements TextWatcher {
     private static final int MAX_MESSAGE_LENGTH = 280;
 
     private EditText mSmsEditText;
    private ImageButton mMenuButton;
     private TextView mCurrentLengthTextView;
 
     private View.OnClickListener mMenuButtonClickListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             final ArrayList<CharSequence> totalItems = new ArrayList<CharSequence>();
             for (int i = 0; i < DataManager.getInstance().getSize(); i++) {
                 BlockSetting setting = DataManager.getInstance().get(i);
                 if (!"".equals(setting.mAutoSMS)) {
                     totalItems.add(setting.mAutoSMS);
                 }
             }
 
             final CharSequence[] items;
             items = getContext().getResources().getTextArray(
                     R.array.auto_sms_preset);
             for (int i = items.length-1; i >= 0; i--) {
                 totalItems.add(0, items[i]);
             }
             final CharSequence[] totalArray = totalItems
                     .toArray(new CharSequence[totalItems.size()]);
 
             final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
             builder.setItems(totalArray, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     String insertText = mSmsEditText.getText().toString();
                     if (!"".equals(insertText)) {
                         insertText += "\n";
                     }
                     mSmsEditText.setText(insertText + totalArray[which]);
                     mSmsEditText.setSelection(mSmsEditText.getText().length());
                 }
             });
             builder.show();
         }
     };
 
     public SmsEditWidget(Context context) {
         super(context);
         initViews(context);
     }
 
     public SmsEditWidget(Context context, AttributeSet attrs) {
         super(context, attrs);
         initViews(context);
     }
 
     private void initViews(Context context) {
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.sms_edit_widget_layout, this, true);
 
         mSmsEditText = (EditText) findViewById(R.id.sms_editText);
        mMenuButton = (ImageButton) findViewById(R.id.sms_edit_menu_button);
         mCurrentLengthTextView = (TextView) findViewById(R.id.sms_textLength);
 
         mSmsEditText.addTextChangedListener(this);
         InputFilter[] filterArray = new InputFilter[1];
         filterArray[0] = new InputFilter.LengthFilter(MAX_MESSAGE_LENGTH);
         mSmsEditText.setFilters(filterArray);
         mMenuButton.setOnClickListener(mMenuButtonClickListener);
     }
 
     public void setText(String text) {
         mSmsEditText.setText(text);
     }
 
     public Editable getText() {
         return mSmsEditText.getText();
     }
 
     @Override
     public void afterTextChanged(Editable s) {
     }
 
     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
     }
 
     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {
         final int currentLength = mSmsEditText.getText().toString().length();
         mCurrentLengthTextView.setText(currentLength + "/" + MAX_MESSAGE_LENGTH);
     }
 }
