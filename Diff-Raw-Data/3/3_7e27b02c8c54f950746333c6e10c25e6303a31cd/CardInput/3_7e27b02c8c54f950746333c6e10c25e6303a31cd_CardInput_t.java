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
 	public IndexCard client;
 	public CardSide clientside;
 	
 	public CardInput(MainActivity baseContext, EditText inputTextBox, float screenDensity) {
 		context = baseContext;
 		textBox = inputTextBox;
 		density = screenDensity;
 	}
 	
 	public void show(IndexCard target) {
 		client = target;
 		clientside = new CardSide(target.currentside.serialize());
 		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams((int) context.mview.editspace[2]-50, (int) context.mview.editspace[3]-90); 
 		rllp.topMargin = 81;
 		rllp.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		textBox.setLayoutParams(rllp);
 		textBox.setVisibility(EditText.VISIBLE);
 		((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(textBox, InputMethodManager.SHOW_FORCED);
 		target.editing = true;
 		context.mview.state = 2;
		if (context.mview.mActionMode == null)
			context.mview.mActionMode = context.mview.startActionMode(context.mview.singleSelectedAction);
 		context.mview.currentCard = target;
 		setTextSize(context.mview.editspace[3]);
 	}
 	
 	public void hide() {
 		if (client == null)
 			return;
 		textBox.setVisibility(EditText.INVISIBLE);
 		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
 		commit();
 		client.editing = false;
 		context.mview.state = 0;
 		if (client.savedSpot.length != 0) {
 			client.animating = true;
 			client.animdata = new AnimatedNums(context.mview.editspace, client.savedSpot, 400);
 		}
 		client = null;
 		clientside = null;
 	}
 	
 	public static String[] getLines(String text, int width, Paint tp) {
 		String[] initial = text.split("\n");
 		ArrayList<String> out = new ArrayList<String>();
 		for (int i = 0; i < initial.length; i++) {
 			while (tp.measureText(initial[i]) > width) {
 				int safe = 0;
 				int space;
 				for (space = 0;
 					space >= 0 && tp.measureText(initial[i], 0, space) < width;
 					space = initial[i].indexOf(" ", space+1)) {
 						safe = space;
 				}
 				if (safe == 0 || (safe + 1 == initial[i].length()))
 					break;
 				out.add(initial[i].substring(0, safe));
 				initial[i] = initial[i].substring(safe + 1);
 			}
 			out.add(initial[i]);
 		}
 		return out.toArray(new String[out.size()]);
 	}
 	
 	public void commit() {
 		client.currentside.text = textBox.getText().toString();
 		client.currentside.lines = getLines(client.currentside.text, (int) context.mview.editspace[2] - 80, textBox.getPaint());
 	}
 	
 	public void revert() {
 		client.currentside = new CardSide(clientside.serialize());
 	}
 
 	private void setTextSize(double height) {
 		textBox.setTextSize((float) (height*clientside.textSize/density));
 	}
 	
 	public void newside() {
 		commit();
 		textBox.setVisibility(EditText.INVISIBLE);
 		clientside = new CardSide();
 		client.sides.add(client.sidenum + 1, clientside);
 		client.flip();
 		client.animpurpose = 4;
 	}
 }
