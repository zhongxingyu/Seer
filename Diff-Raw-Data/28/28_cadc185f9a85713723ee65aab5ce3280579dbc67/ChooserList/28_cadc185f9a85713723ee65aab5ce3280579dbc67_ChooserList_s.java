 package com.bunkerdev.chooser;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.Editable;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextSwitcher;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 import com.bunkerdev.chooser.wheel.WheelView;
 import com.bunkerdev.chooser.wheel.adapters.NumericWheelAdapter;
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 
 public class ChooserList extends Activity{
 
 	public static String PATH = "favorites";
 	public static String FROM_FAVORITES = "from_favorites";
 	public static String CHOICES = "choices_from_favorites";
 	public static String PREFS  = "sharedpreferences";
 
 	private AlertDialog wheelDialog;
 	private AlertDialog createOptionDialog;
 	private LayoutInflater inflater;
 	private ExpLAdapter expAdapter;
 	private ArrayList<Choice> choices;
 	private Integer numChoices;
 	private WheelView wheel;
 	private PopupWindow resultPopup;
 	private View view;
 	private boolean favorited;
 	private CountDownTimer resultCountdown;
 	private TextSwitcher txtSwitcher;
 
 	private static String tag = "TAG";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.options_list_tab);
 		view = (RelativeLayout) findViewById(R.id.wrapper);
 		
 		choices = new ArrayList<Choice>();
 
 		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 
 		ExpandableListView rows = (ExpandableListView) this.findViewById(R.id.rows);
 		expAdapter = new ExpLAdapter(choices, inflater);
 		rows.setAdapter(expAdapter);
 
 		rows.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				choices.remove((Choice)arg1.getTag());
 				expAdapter.remove((Choice)arg1.getTag());
 				ImageButton star = (ImageButton) findViewById(R.id.star);
 				star.setBackgroundResource(R.drawable.silver_star);
 				return false;
 			}
 		});
 		
 		initializeWheel();
 		initializeCreateOption();
 		initializeResultPopup();
 
 		Button chooseBtn = (Button) findViewById(R.id.chooseButton);
 		chooseBtn.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				expAdapter.refreshWeighings();
 				WeightedRandom randGenerator= new WeightedRandom(choices);
 
 				ArrayList<Choice> chosen = randGenerator.getChoice(numChoices);
 				//Log.i(tag, randGenerator.doStatistics(100));	
 				ListView resultList = (ListView) resultPopup.getContentView().findViewById(R.id.resultList);
				ListAdapter resultAdapter = new ArrayAdapter<Choice>(getApplicationContext(), R.layout.simple_textview, chosen);
 				resultList.setAdapter(resultAdapter);
 				resultPopup.showAtLocation(view, Gravity.CENTER_VERTICAL, 0, 0);
 				
 				resultPopup.getContentView().findViewById(R.id.wrapperResult).setVisibility(View.GONE);
 				resultPopup.getContentView().findViewById(R.id.wrapperAnimation).setVisibility(View.VISIBLE);
 				txtSwitcher.setText(new String("5"));
 				resultCountdown.start();
 			}
 		});
 		
 		ImageButton star = (ImageButton) findViewById(R.id.star);
 		star.setBackgroundResource(R.drawable.silver_star);
 		star.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(!choices.isEmpty() || favorited){
 					saveFavorite();
 					v.setBackgroundResource(R.drawable.gold_star);
 					favorited = true;
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
 		favorited = sp.getBoolean(FROM_FAVORITES, false); 
 		sp.edit().putBoolean(ChooserList.FROM_FAVORITES, false).commit();
 		
 		ImageButton star = (ImageButton) findViewById(R.id.star);
 		if(favorited){
 			star.setBackgroundResource(R.drawable.gold_star);
 			choices.clear();
 			choices.addAll(((Main)getParent()).getAux());
 			expAdapter.refreshChoices(((Main)getParent()).getAux());
 		}
 	}
 
 	private void initializeWheel(){
 		View wheelLayout = inflater.inflate(R.layout.wheel, null);
 		AlertDialog.Builder builder = new Builder(this);
 
 		wheel = (WheelView) wheelLayout.findViewById(R.id.wheel);
 
 		final NumericWheelAdapter numChoicesAdapter = new NumericWheelAdapter(this);
 		wheel.setViewAdapter(numChoicesAdapter);
 		wheel.setCyclic(false);
 		wheel.setVisibleItems(3);
 
 		builder.setTitle(R.string.numChoicesDialogTitle);
 		builder.setView(wheelLayout);
 		builder.setPositiveButton(R.string.confirmButton, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				TextView numChoicesView = (TextView)findViewById(R.id.tvNumberChoices);
 				Integer chosen = wheel.getCurrentItem();
 				numChoices = chosen;
 				numChoicesView.setText(chosen.toString());
 				wheelDialog.dismiss();				
 			}
 		});
 
 		TextView numChoicesView = (TextView)findViewById(R.id.tvNumberChoices);
 		numChoices = 1;
 		numChoicesView.setText(1+"");
 
 		((NumericWheelAdapter)wheel.getViewAdapter()).setMaxValue(choices.size());
 
 		numChoicesView.setOnClickListener(new OnClickListener() { 
 
 			public void onClick(View v) {
 				((NumericWheelAdapter)wheel.getViewAdapter()).setMaxValue(choices.size());
 				wheelDialog.show();	
 			}
 		});
 
 		wheelDialog = builder.create();
 
 	}
 
 	private void initializeCreateOption(){
 		TextView addOptionView = (TextView)findViewById(R.id.addOption);
 		ImageView addOptionIView = (ImageView)findViewById(R.id.addOptionI);
 		OnClickListener addOptionClickListener = new OnClickListener() {
 
 			public void onClick(View v) {
 				createOptionDialog.show();
 			}
 		};
 
 		addOptionView.setOnClickListener(addOptionClickListener);
 		addOptionIView.setOnClickListener(addOptionClickListener);
 		AlertDialog.Builder builder = new Builder(this);
 
 		builder.setPositiveButton(R.string.confirmButton, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int which) {
 				RadioGroup opType = (RadioGroup) createOptionDialog.findViewById(R.id.rgOptionType);
 
 				int type = opType.getCheckedRadioButtonId();
 				Choice c;
 				ImageButton star = (ImageButton) findViewById(R.id.star);
 				star.setBackgroundResource(R.drawable.silver_star);
 				switch (type) {
 				case R.id.rbSimple:
 					EditText opNameView = (EditText) createOptionDialog.findViewById(R.id.optionName);
 					Editable nameEdit = opNameView.getText();
 					nameEdit.clearSpans();
					c = new Choice(nameEdit.toString());
 					choices.add(c);
 					star.setBackgroundResource(R.drawable.silver_star);
 					favorited = false;
 					expAdapter.addChoiceRadioGroup(c);
 					createOptionDialog.dismiss();
 					opNameView.setText("");
 					break;
 				case R.id.rbRange:
 					EditText etRangeIni = (EditText) createOptionDialog.findViewById(R.id.rangeIni);
 					EditText etRangeEnd = (EditText) createOptionDialog.findViewById(R.id.rangeEnd);
 					try {
 						Integer rIni = new Integer(etRangeIni.getText().toString());
 						Integer rEnd = new Integer(etRangeEnd.getText().toString());
 						if(rIni>=rEnd){
 							Toast t = Toast.makeText(getApplicationContext(), R.string.toastRangeError, 3000);
 							t.show();
 						}else{
 							String name = getText(R.string.rangeName1)+" "+rIni+" "+getText(R.string.rangeName2)+" "+rEnd;
 							c = new Choice(name, rIni, rEnd);
 							choices.add(c);
 							star.setBackgroundResource(R.drawable.silver_star);
 							favorited = false;
 							expAdapter.addChoiceRadioGroup(c);
 							createOptionDialog.dismiss();
 						}
 					} catch (NumberFormatException e) {
 						Toast t = Toast.makeText(getApplicationContext(), R.string.toastRangeError, 3000);
 						t.show();
 					}
 
 					etRangeIni.setText("");
 					etRangeEnd.setText("");
 
 					break;
 				default:
 					break;
 				}
 
 			}
 		});
 		View newOption = (View) inflater.inflate(R.layout.new_option, null);
 
 		RadioButton rbSimple = (RadioButton) newOption.findViewById(R.id.rbSimple);
 		RadioButton rbRange = (RadioButton) newOption.findViewById(R.id.rbRange);
 		EditText opNameView = (EditText) newOption.findViewById(R.id.optionName);
 		RelativeLayout rangeLayout = (RelativeLayout) newOption.findViewById(R.id.rlRange);
 
 		rbSimple.setTag(opNameView);
 		rbRange.setTag(rangeLayout);
 
 		OnCheckedChangeListener typeChangeListener = new OnCheckedChangeListener() {
 
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				Object obj = buttonView.getTag();
 
 				if(obj instanceof EditText){
 					if(isChecked)
 						((EditText) obj).setVisibility(View.VISIBLE);
 					else
 						((EditText) obj).setVisibility(View.GONE);
 				}else{
 					if(isChecked)
 						((RelativeLayout) obj).setVisibility(View.VISIBLE);
 					else
 						((RelativeLayout) obj).setVisibility(View.GONE);
 				}
 			}
 		};
 
 		rbSimple.setOnCheckedChangeListener(typeChangeListener);
 		rbRange.setOnCheckedChangeListener(typeChangeListener);
 
 		builder.setView(newOption);
 		createOptionDialog = builder.create();
 	}
 
 	private void initializeResultPopup(){
 		final View popupView = inflater.inflate(R.layout.result, null);
 		popupView.setBackgroundColor(Color.GRAY);
 		Button backBtn = (Button) popupView.findViewById(R.id.btnResultLeft);
 		backBtn.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				resultPopup.dismiss();
 			}
 		});
 		Button exitBtn = (Button) popupView.findViewById(R.id.btnResultRight);
 		exitBtn.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				finish();
 			}
 		});
 		
 		txtSwitcher = (TextSwitcher) popupView.findViewById(R.id.resultTextSwitcher);
 		txtSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
 	        @Override
 	        public View makeView() {
 	            TextView txt = new TextView(ChooserList.this);
 	            txt.setGravity(Gravity.CENTER);
 	            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150);
 	            return txt;
 	        }
 	    });
 		
 		resultCountdown = new CountDownTimer(5000, 1000) {
 			
 			@Override
 			public void onTick(long millisUntilFinished) {
 					Integer before = new Integer(((TextView)txtSwitcher.getCurrentView()).getText().toString());
 					txtSwitcher.setText(before-1+"");
 			}
 			
 			@Override
 			public void onFinish() {
 				txtSwitcher.setText("0");
 				popupView.findViewById(R.id.wrapperResult).setVisibility(View.VISIBLE);
 				popupView.findViewById(R.id.wrapperAnimation).setVisibility(View.GONE);
 			}
 		};
 		
 		Properties gitSecrets = new Properties();
 		try {
 			gitSecrets.load(this.getClass().getClassLoader().getResourceAsStream("assets/GitSecrets"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		AdView adView = new AdView(this, AdSize.BANNER, gitSecrets.getProperty("ADMOB_ID"));
 		LinearLayout adLayout = (LinearLayout)popupView.findViewById(R.id.adLayout);
 		adLayout.addView(adView);
 		
 		AdRequest request = new AdRequest();
 //		request.setTesting(true);
 		adView.loadAd(request);
 		
 		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
 		resultPopup = new PopupWindow(popupView, wm.getDefaultDisplay().getWidth(), 3*(wm.getDefaultDisplay().getHeight()/4), true);
 
 	}
 
 	private void saveFavorite(){
 		HashMap<String, ArrayList<Choice>> favorites = new HashMap<String, ArrayList<Choice>>();
 		
 		try{
 			InputStream file = openFileInput(PATH);
 			BufferedInputStream buffer = new BufferedInputStream( file );
 			ObjectInput input = new ObjectInputStream ( buffer );
 			try{
 				Object o = input.readObject();
 				if(o instanceof HashMap<?, ?>)
 					favorites = (HashMap<String, ArrayList<Choice>>)o;
 			}
 			finally{
 				input.close();
 			}
 		}
 		catch(ClassNotFoundException ex){
 		}
 		catch(IOException ex){
 		}
 		
 		String favoriteName = "";
 		int i = 0;
 		for(Choice c : choices){
 			if(i == 2){
 				if(choices.size()-1 > i)
 					favoriteName += c.isRange()? c.getRangeName()+"..." : c.getName()+"...";
 				else
 					favoriteName += c.isRange()? c.getRangeName() : c.getName();
 				break;
 			}
 			if(choices.size()-1 > i)
 				favoriteName += c.isRange()? c.getRangeName()+", " : c.getName()+", ";
 			else
 				favoriteName += c.isRange()? c.getRangeName() : c.getName();
 			
 			i++;
 		}
 		
 		int contains = 1;
 		String nameBackup = favoriteName;
 		while(favorites.containsKey(favoriteName)){
 			favoriteName = nameBackup + " ("+contains+")";
 			contains++;
 		}
 		favorites.put(favoriteName, choices);
 		
 		expAdapter.refreshWeighings();
 		
 		FileOutputStream fos;
 		try {
 			fos = openFileOutput(PATH, MODE_PRIVATE);
 			BufferedOutputStream buffer = new BufferedOutputStream( fos );
 			ObjectOutput output = new ObjectOutputStream( buffer );
 			try{
 				output.writeObject(favorites);
 			}finally{
 				output.close();
 			}
 		}catch(IOException ex){
 		}
 	}
 	
 }
