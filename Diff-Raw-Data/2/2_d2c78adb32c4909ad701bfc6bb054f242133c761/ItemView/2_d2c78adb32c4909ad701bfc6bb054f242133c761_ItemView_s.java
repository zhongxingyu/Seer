 package com.voc4u.activity.dictionary;
 
 import junit.framework.Assert;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 import com.voc4u.R;
 import com.voc4u.controller.WordController;
 import com.voc4u.setting.CommonSetting;
 import com.voc4u.setting.LangSetting;
 
 
 public class ItemView extends LinearLayout implements OnCheckedChangeListener
 {
 	public static final int	MAX_EXAMPLES_IN_VIEW	= 50;
 	private final WordController mWordCtrl;
 	private String mTitle;
 	private int mLesson;
 	private final CheckBox chkBox;
 	private final ItemStatus mStatus;
 	private final Dictionary	mDictionary;
 	private TestAnyChecked mTestAnyChecked;
 	
 	public ItemView(Dictionary context, WordController wordCtrl)
 	{
 		super(context);
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.dictionary_item, this);
 
 		mWordCtrl = wordCtrl;
 		
 		chkBox = (CheckBox)findViewById(R.id.checkbox);
 	
 		mStatus = ItemStatus.NONE;
 		
 		mDictionary = context;
 	}
 
 	public void setup(int position, TestAnyChecked tac)
 	{
 		mTestAnyChecked = tac;
 		mLesson = position + 1;
 		String lessons[] = getContext().getResources().getStringArray(R.array.lessons);
 		
 		Assert.assertTrue("Isn't enought names for lessons", position < lessons.length);
 		
 		if(position < lessons.length)
 			mTitle = lessons[position];
 //		else
 //			mTitle = getContext().getString(R.string.dictionaryItem, mLesson);
 		
 		findViewById(R.id.preparing).setVisibility(mWordCtrl.isPrepairing(mLesson)? View.VISIBLE : View.GONE);
 		
 		
 		final TextView tv = (TextView) findViewById(R.id.text);
 		tv.setText(mTitle);
 
 		final boolean enable = mWordCtrl.isEnableLesson(mLesson);
 		chkBox.setChecked(enable);
 		chkBox.setOnCheckedChangeListener(this);
 		
 		Assert.assertTrue("Isn't enought colors for lessons", position < LangSetting.LESSON_BG_COLOR.length);
 		if(position < LangSetting.LESSON_BG_COLOR.length)
 			setBackgroundResource(LangSetting.LESSON_BG_COLOR[position]);
 		
 		String[] aex = LangSetting.getInitDataFromLT(CommonSetting.lernCode, mLesson);
 		String sex = "";
 		
 		if(aex != null && aex.length > 0)
 		{
 			int lex = aex.length > MAX_EXAMPLES_IN_VIEW? MAX_EXAMPLES_IN_VIEW : aex.length;
 
 			sex = getFirstWord(aex[0]);
 			
 			for(int i = 1; i < lex; i++)
 				sex += ", " + getFirstWord(aex[i]);
 		}
 		
 		final TextView ex = (TextView)findViewById(R.id.examples);
 		ex.setText(sex);
 	}
 	
 	private String getFirstWord(final String string)
 	{
 		int inx = string.indexOf("|");
 		if(inx > -1)
 			return string.substring(0, inx);
 		else 
 			return string;
 //		String[] words = string.replace("|", " | ").split("|");
 //		if(words != null && words.length > 0)
 //			return words[0];
 //		
 //		return string;
 	}
 
 	public int getLesson()
 	{
 		return mLesson;
 	}
 
 	@Override
 	public void onCheckedChanged(final CompoundButton buttonView,
 			boolean isChecked)
 	{
 		if (!isChecked)
 		{
 			// test set mTestAnyChecked was set
 			// and test any checked return true -> some items still checked (this is not calced)
 			// and test enable in db when in db take to question to user
			if ((mTestAnyChecked == null || mTestAnyChecked.testAnyChecked(this)) && mWordCtrl.isEnableLesson(mLesson))
 			{
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						getContext());
 				builder.setMessage(R.string.msg_your_are_sure_disable_lesson)
 						.setCancelable(false).setTitle(mTitle)
 						.setPositiveButton(android.R.string.yes,
 								new DialogInterface.OnClickListener()
 								{
 									public void onClick(DialogInterface dialog,
 											int id)
 									{
 										setVocabulary(false);
 									}
 								}).setNegativeButton(android.R.string.no,
 								new DialogInterface.OnClickListener()
 								{
 									public void onClick(DialogInterface dialog,
 											int id)
 									{
 										dialog.cancel();
 										buttonView.setChecked(true);
 									}
 								});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		} 
 		else if(!mWordCtrl.isEnableLesson(mLesson))
 			setVocabulary(true);
 	}
 
 	protected void setVocabulary(boolean enable)
 	{
 //		if(mStatus == ItemStatus.NONE)
 //			mStatus = enable ? ItemStatus.ADD : ItemStatus.REMOVE;
 //		else if(mStatus == ItemStatus.ADD)
 //			mStatus = !enable ? ItemStatus.NONE : ItemStatus.REMOVE;
 //		else if(mStatus == ItemStatus.REMOVE)
 //			mStatus = enable ? ItemStatus.NONE : ItemStatus.REMOVE;
 		
 		mWordCtrl.enableLessonAsync(mLesson, enable, mDictionary);
 		mDictionary.onUpdateDone();
 	}
 	
 	
 
 	public boolean isChecked() 
 	{
 		return chkBox.isChecked();
 	}
 	
 	public void setChecked(boolean checked)
 	{
 		chkBox.setChecked(checked);
 	}
 
 }
