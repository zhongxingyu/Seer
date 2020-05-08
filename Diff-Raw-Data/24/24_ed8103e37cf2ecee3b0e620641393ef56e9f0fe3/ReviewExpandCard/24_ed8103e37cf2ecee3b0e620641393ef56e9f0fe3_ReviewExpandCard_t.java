 package com.mili.xiaominglui.app.vello.ui;
 
 import com.mili.xiaominglui.app.vello.R;
 import com.mili.xiaominglui.app.vello.data.factory.MiliDictionaryJsonParser;
 import com.mili.xiaominglui.app.vello.data.model.Definition;
 import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
 import com.mili.xiaominglui.app.vello.data.model.Phonetics;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import it.gmariotti.cardslib.library.internal.CardExpand;
 
 public class ReviewExpandCard extends CardExpand {
 	String mData;
 
 	public ReviewExpandCard(Context context, String data) {
 		super(context, R.layout.inner_review_expand);
 		mData = data;
 	}
 
 	@Override
 	public void setupInnerViewElements(ViewGroup parent, View view) {
 
 		if (view == null)
 			return;
 		IcibaWord word = MiliDictionaryJsonParser.parse(mData);
		if (word == null) {
			return;
		}
 
 		LinearLayout linearLayoutPhoneticArea = (LinearLayout) view.findViewById(R.id.phonetics_area);
 		LinearLayout linearLayoutDefinitionArea = (LinearLayout) view.findViewById(R.id.definition_area);
 
 		linearLayoutPhoneticArea.removeAllViews();
 		linearLayoutDefinitionArea.removeAllViews();
 
 		for (Phonetics phonetics : word.phonetics) {
 			View phoneticsView = LayoutInflater.from(mContext).inflate(R.layout.phonetics_item, null);
 			LinearLayout phoneticsGroup = (LinearLayout) phoneticsView.findViewById(R.id.phonetics_group);
 			String phoneticsType = "";
 			if (phonetics.type.equals("en")) {
 				phoneticsType = mContext.getResources().getString(R.string.title_phonetics_uk);
 			} else if (phonetics.type.equals("us")) {
 				phoneticsType = mContext.getResources().getString(R.string.title_phonetics_us);
 			}
 			
 			((TextView) phoneticsView.findViewById(R.id.phonetics_type)).setText(phoneticsType);
 			((TextView) phoneticsView.findViewById(R.id.phonetics_symbol)).setText("[" + phonetics.symbol + "]");
 			/*
 			 * remove sound icon in word card at present ((IconicTextView)
 			 * phoneticsView
 			 * .findViewById(R.id.phonetics_sound)).setIcon(FontAwesomeIcon
 			 * .VOLUME_UP); ((IconicTextView)
 			 * phoneticsView.findViewById(R.id.phonetics_sound
 			 * )).setTextColor(Color.GRAY);
 			 */
 			linearLayoutPhoneticArea.addView(phoneticsGroup);
 		}
 
 		for (Definition definition : word.definition) {
 			View definitionView = LayoutInflater.from(mContext).inflate(R.layout.definition_item, null);
 			LinearLayout definiitionGroup = (LinearLayout) definitionView.findViewById(R.id.definition_group);
 			((TextView) definitionView.findViewById(R.id.pos)).setText(definition.pos);
 			((TextView) definitionView.findViewById(R.id.definiens)).setText(definition.definiens);
 			linearLayoutDefinitionArea.addView(definiitionGroup);
 		}
 	}
 
 }
