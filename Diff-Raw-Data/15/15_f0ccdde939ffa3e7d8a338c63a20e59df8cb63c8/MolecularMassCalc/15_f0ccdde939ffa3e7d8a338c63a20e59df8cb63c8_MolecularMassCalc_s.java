 package com.threeDBJ.MolecularMassCalcLib;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Button;
 import android.widget.Toast;
 import android.widget.LinearLayout;
 import android.view.View;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.KeyEvent;
 import android.view.animation.Animation;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.inputmethod.InputMethodManager;
 import android.view.LayoutInflater;
 import android.content.res.Configuration;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.text.Editable;
 import android.text.TextWatcher;
 import java.text.DecimalFormat;
 import android.net.Uri;
 import android.util.Log;
 
 import java.text.DecimalFormat;
 
 public class MolecularMassCalc extends Activity {
 
     public static final int NONE=0, PERCENTAGES=1, SELECT_FORMULA=2;
 
     int menu_mode = NONE;
     EditText input;
     TextView result;
     TextView formula;
     FormulaParser p;
     Formula curFormula;
 
     String inp="", prevInp="", res="", form="";
     boolean resultsVisible = false, showContextMenu = false;
     public Typeface capFont, dataFont;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.v("testing...","made it");
         Configuration config = this.getResources().getConfiguration();
 
         if(config.orientation == 1) {
             setContentView(R.layout.main);
         } else if(config.orientation == 2) {
             setContentView(R.layout.main_wide);
         }
 	capFont = Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf");
 	dataFont = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
 
         this.p = new FormulaParser();
 
 	setupUI();
     }
 
     /** Handles a screen orientation change */
     @Override
     public void onConfigurationChanged(Configuration config) {
         super.onConfigurationChanged(config);
 	int orientation = config.orientation;
         if(orientation == 1) {
             setContentView(R.layout.main);
         } else if(orientation == 2) {
             setContentView(R.layout.main_wide);
 	}
 	setupUI();
 	input.setText(inp);
 	result.setText(res);
 	formula.setText(form);
 	View resHolder = findViewById(R.id.result_holder);
 	if(resultsVisible) {
 	    resHolder.setVisibility(View.VISIBLE);
 	    showPercentages();
 	} else {
 	    resHolder.setVisibility(View.INVISIBLE);
 	}
     }
 
     public void setupUI() {
 	this.formula = (TextView)findViewById (R.id.form_name);
 	formula.setTypeface(dataFont);
         this.input = (EditText) findViewById(R.id.input);
         input.setOnEditorActionListener(calcAction);
 	input.addTextChangedListener(inputChanged);
         this.result = (TextView) findViewById(R.id.result);
         Button n = (Button) findViewById(R.id.calc);
         n.setOnClickListener(calcBtn);
 	n.setTypeface(capFont);
         registerForContextMenu(n);
         n = (Button) findViewById(R.id.clear);
 	n.setTypeface(capFont);
         n.setOnClickListener(clearBtn);
         n = (Button) findViewById(R.id.purchase);
 	n.setOnClickListener(adFreeBtn);
 	n.setTypeface(capFont);
 
 	setFont(R.id.mass_text, capFont);
 	setFont(R.id.form_text, capFont);
 	setFont(R.id.percent_text, capFont);
 	setFont(R.id.form_name, dataFont);
 	setFont(R.id.result, dataFont);
 	setFont(R.id.units_text, dataFont);
     }
 
     public void setFont(int resource, Typeface font) {
 	TextView t = (TextView) findViewById(resource);
 	t.setTypeface(font);
     }
 
     public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
 	MenuItem item;
 	if(v.getId() == R.id.calc && showContextMenu) {
 	    menu.setHeaderTitle("Resolve Ambiguous Formula");
 	    for(int i=0;i<p.formulas.size();i+=1) {
 		item = menu.add(0, i, 0, p.getFormula(i).getCleanForm());
 	    }
 	    showContextMenu = false;
 	}
     }
 
     public boolean onContextItemSelected(MenuItem item) {
	Log.e("mmc", "shiiiteee "+item.getItemId());
 	curFormula = p.getFormula(item.getItemId());
 	showResult();
         return true;
     }
 
     public void setFormulaText(String form) {
 	this.form = form;
 	formula.setText(form);
     }
 
     private void calculate() {
 	inp = input.getText().toString();
 	if(inp.length() == 0) {
 	    hideResult();
 	    return;
 	}
         p.setInput(inp);
         int nFormulas = p.parse();
 	if(nFormulas == 0) {
 	    Toast.makeText(this, p.error,
 			   Toast.LENGTH_SHORT).show();
 	} else if(nFormulas == 1) {
 	    curFormula = p.getFormula();
 	    showResult();
 	} else {
 	    curFormula = p.getFormula();
 	    this.showContextMenu = true;
 	    openContextMenu(findViewById(R.id.calc));
 	}
     }
 
     public void showResult() {
	if(prevInp.equals(curFormula.getCleanForm())) return;
 	double resVal = curFormula.getMass();
         DecimalFormat twoDForm = new DecimalFormat("#.####");
         resVal = Double.valueOf(twoDForm.format(resVal));
 
 	res = Double.toString(resVal);
         result.setText(res);
 	if(res.length() > 0) {
 	    formula.setText ("Loading...");
 	    new RetrieveFormulaTask(this).execute (inp);
 	}
 	if(!resultsVisible) {
 	    View resHolder = findViewById(R.id.result_holder);
 	    if(android.os.Build.VERSION.SDK_INT > 13) {
 		AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
 		fadeIn.setDuration(500);
 		fadeIn.setFillAfter(true);
 		resHolder.startAnimation(fadeIn);
 	    } else {
 		resHolder.setVisibility(View.VISIBLE);
 	    }
 	    resultsVisible = true;
 	}
 	showPercentages();
 	prevInp = curFormula.getCleanForm();
     }
 
     public void showPercentages() {
 	LinearLayout per = (LinearLayout) findViewById(R.id.percentages);
 	per.removeAllViews();
 	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	String add;
 	for(String perText : curFormula.getMassPercents()) {
 	    TextView t = (TextView)inflater.inflate(R.layout.percentage, per, false);
 	    t.setText(perText);
 	    t.setTypeface(dataFont);
 	    per.addView(t);
 	}
     }
 
     public void hideResult() {
 	if(resultsVisible) {
 	    View resHolder = findViewById(R.id.result_holder);
 	    if(android.os.Build.VERSION.SDK_INT > 13) {
 		AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
 		fadeOut.setDuration(500);
 		fadeOut.setFillAfter(true);
 		resHolder.startAnimation(fadeOut);
 	    } else {
 		resHolder.setVisibility(View.INVISIBLE);
 	    }
	    resultsVisible = false;
	}
     }
 
     private OnClickListener calcBtn = new OnClickListener() {
 	    public void onClick(View v) {
 		calculate();
 	    }
 	};
 
     private OnClickListener clearBtn = new OnClickListener() {
 	    public void onClick(View v) {
 		inp = "";
 		input.setText(inp);
 		hideResult();
 	    }
 	};
 
     private OnClickListener adFreeBtn = new OnClickListener() {
 	    public void onClick(View v) {
 		final String pkgName = "com.threeDBJ.MolecularMassCalcPaid";
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		try {
 		    intent.setData(Uri.parse("market://details?id="+pkgName));
 		    startActivity(intent);
 		} catch (android.content.ActivityNotFoundException anfe) {
 		    intent.setData(Uri.parse("http://play.google.com/store/apps/details?id="+pkgName));
 		    startActivity(intent);
 		}
 	    }
 	};
 
     private OnEditorActionListener calcAction = new OnEditorActionListener() {
 
 	    @Override
 	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		if (actionId == EditorInfo.IME_ACTION_DONE) {
 		    calculate();
 		}
 		return false;
 	    }
         };
 
     private TextWatcher inputChanged = new TextWatcher() {
 	    @Override
 	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 	    }
 
 	    @Override
 	    public void afterTextChanged(Editable s) {
 		inp = s.toString();
 	    }
 
 	    @Override
 	    public void onTextChanged(CharSequence s, int start, int before, int count) {
 	    }
 	};
 }
