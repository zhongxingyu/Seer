 package com.andrewdutcher.indexcards;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.graphics.*;
 
 public class CardInput {
 	private MainActivity context;
 	private EditText textBox;
 	private float density;
	private IndexCard client;
 	
 	public CardInput(MainActivity baseContext, EditText inputTextBox, float screenDensity) {
 		context = baseContext;
 		textBox = inputTextBox;
 		density = screenDensity;
 	}
 	
 	public void show(IndexCard target) {
 		client = target;
 		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams((int) context.mview.editspace[2]-50, (int) context.mview.editspace[3]-90); 
 		rllp.topMargin = 81;
 		rllp.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		textBox.setLayoutParams(rllp);
 		textBox.setVisibility(EditText.VISIBLE);
 		((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(textBox, InputMethodManager.SHOW_FORCED);
 		revert();
 		target.editing = true;
 		target.drawcontrols = true;
 		target.animating = false;
 		context.mview.state = 2;
 		context.mview.mActionMode = context.mview.startActionMode(context.mview.singleSelectedAction);
 		context.mview.currentCard = target;
 		setTextSize(target.textStyle.getTextSize());
 	}
 	
 	public void hide() {
 		if (client == null)
 			return;
 		textBox.setVisibility(EditText.INVISIBLE);
 		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
 		client.cardLines = getLines();
 		client.cardText = get();
 		client.drawcontrols = false;
 		client.editing = false;
 		context.mview.state = 0;
 		if (client.savedSpot.length != 0) {
 			client.animating = true;
 			client.animdata = new AnimatedNums(context.mview.editspace, client.savedSpot, 400);
 		}
 		client = null;
 	}
 	
 	public String get() {
 		return textBox.getText().toString();
 	}
 	
 	public String[] getLines() {
 		int width = (int) context.mview.editspace[2] - 80;
 		String[] initial = textBox.getText().toString().split("\n");
 		ArrayList<String> out = new ArrayList<String>();
 		Paint tp = textBox.getPaint();
 		for (int i = 0; i < initial.length; i++) {
 			while (tp.measureText(initial[i]) > width) {
 				//Log.d("andrew", "Line '"+ initial[i] +"' too long");
 				int safe = 0;
 				int space;
 				for (space = 0;
 					space >= 0 && tp.measureText(initial[i], 0, space) < width;
 					space = initial[i].indexOf(" ", space+1)) {
 						safe = space;
 						//Log.d("andrew", "Space found: " + ((Integer) safe).toString());
 				}
 				if (safe == 0 || (safe + 1 == initial[i].length()))
 					break;
 				out.add(initial[i].substring(0, safe));
 				initial[i] = initial[i].substring(safe + 1);
 			}
 			out.add(initial[i]);
 		}
 		//out.set(out.size()-1,out.get(out.size()-1).substring(0,out.get(out.size()-1).length()-1));
 		return out.toArray(new String[out.size()]);
 	}	
 	public void set(String text) {
 		textBox.setText(text);
 	}
 	
 	/*public void setLines(String[] text) {
 		String out = "";
 		for (int i = 0; i < text.length; i++) {
 			out += text[i];
 		}
 		set(out);
 	}*/
 	
 	public void revert() {
 		set(client.cardText);
 	}
 
 	private void setTextSize(float f) {
 		textBox.setTextSize(f/density);
 	}
 }
