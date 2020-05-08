 package com.innutrac.poly.innutrac;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import it.gmariotti.cardslib.library.internal.Card;
 
 public class PercentCard extends Card {
 
 	protected TextView mPrimaryTitle;
 	protected TextView mSecondaryTitle;
 	protected String primaryTitle;
 	protected String secondaryTitle;
 	protected int count;
 
 	public PercentCard(Context context) {
 		this(context, R.layout.percent_card_inner_base_main);
 	}
 
 	public PercentCard(Context context, int innerLayout) {
 		super(context, innerLayout);
 	}
 
 	private void init() {
 
 	}
 
 	@Override
 	public void setupInnerViewElements(ViewGroup parent, View view) {
 
 		// Retrieve elements
 		mPrimaryTitle = (TextView) parent
 				.findViewById(R.id.carddemo_card_color_inner_simple_title);
 		mSecondaryTitle = (TextView) parent
 				.findViewById(R.id.carddemo_card_color_inner_secondary_title);
 
 		if (mPrimaryTitle != null) {
 			mPrimaryTitle.setText(primaryTitle);
 			 mPrimaryTitle.setTextSize(25);
 		}
 		if (mSecondaryTitle != null) {
 			mSecondaryTitle.setText(secondaryTitle);
 			 mSecondaryTitle.setTextSize(12);
 		}
 	}
 
 	public String getTitle() {
 		return primaryTitle;
 	}
 
 	public void setTitle(String title) {
 		primaryTitle = title;
 	}
 
 	public String getSecondaryTitle() {
 		return secondaryTitle;
 	}
 
 	public void setSecondaryTitle(String secondaryTitle) {
 		this.secondaryTitle = secondaryTitle;
 	}
 
 	public int getCount() {
 		return count;
 	}
 
 	public void setCount(int count) {
 		this.count = count;
 	}
 }
 
