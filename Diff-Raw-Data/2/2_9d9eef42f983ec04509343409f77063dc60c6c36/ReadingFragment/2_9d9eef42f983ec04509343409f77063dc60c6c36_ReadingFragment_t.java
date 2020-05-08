 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package story.book.view;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import story.book.view.R;
 import story.book.controller.StoryReadController;
 import story.book.model.DecisionBranch;
 import story.book.model.Illustration;
 import story.book.model.StoryFragment;
 import story.book.model.TextIllustration;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.ActionBar.LayoutParams;
 import android.app.Fragment;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView.FindListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /**
  * ReadingFragment is the interface users can use to read story
  * fragments. If the current fragment being read is not an ending 
  * fragment, the end of the page will have buttons which 
  * will allow users to pick a <code>DecisionBranch</code>
  * progress to the following story fragment in the story.
  * 
  * @author Jessica Surya
  *
  */
 @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
 public class ReadingFragment extends Fragment {
 	StoryReadController SRC;
 	StoryFragment SF;
 	ArrayList<Illustration> illustrations;
 	ArrayList<DecisionBranch> decisions;
 	ArrayList<Button> buttons;
 	View rootView;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		rootView = inflater.inflate(R.layout.reading_fragment, container, false);
 		
 		SRC = ((StoryFragmentReadActivity)this.getActivity()).getController();
 		SF = SRC.getStartingFragment();
 		displayFragment(SF, rootView);
 		return rootView;
 	}
 	
 	/**
 	 * formatButton() creates a button with the corresponding decision branch text
 	 * for each decision branch in an array list of decision branches.
 	 * 
 	 * This returns method an array list of buttons.
 	 * 
 	 * @param DecisionBranch 	the decision branches associated with the fragment
 	 * @param Context 	the context where the button will be displayed
 	 * @return an custom ArrayList<Button> corresponding to the decision branches in a fragment
 	 */
 	private ArrayList<Button> formatButton(ArrayList<DecisionBranch> db, Context c) {
 
 		ArrayList<Button> buttonList = new ArrayList<Button>();
 
 		Iterator<DecisionBranch> dbIterator = db.iterator();
 		DecisionBranch d = null;
 		Button button;
 		while(dbIterator.hasNext()) {
 
 			d = dbIterator.next();
 			button = new Button(c);
 			button.setText(d.getDecisionText());
 			button.setOnClickListener(setListener(button, d.getDestinationID()));
 			buttonList.add(button);
 		}
 
 		return buttonList;
 	}
 
 	/**
 	 * displayFragment() displays all text illustrations as views 
 	 * and decision branches as buttons by getting them from 
 	 * the containing fragment 
 	 * 
 	 * @param StoryFragment 	StoryFragment object
 	 * @param View	 where illustrations will be displayed
 	 */
 	private void displayFragment(StoryFragment SF, View rootView) {
 		
 		RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.reading_fragment);
 		illustrations = SF.getIllustrations();
 		decisions = SF.getDecisionBranches();
 
 		ArrayList<View> illustrationViews = new ArrayList<View>();
 
 		for (Illustration i : illustrations){
 			illustrationViews.add(((TextIllustration)i).getView(false));
 		}
 
 		int pos = 0;
 
		for (View t: illustrationViews) {
 			t.setId(pos + 1);
 			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.
 					WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
 			p.addRule(RelativeLayout.BELOW, pos);
 			t.setLayoutParams(p);
 			((ViewGroup) layout).addView(t, p);
 			pos++;
 		}
 
 		
 		buttons = formatButton(decisions, rootView.getContext());
 		int buttonIndex = 0;
 		for (Button dbButton : buttons) {
 			dbButton.setId(pos + 1);
 			buttonIndex++;
 			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
 					LayoutParams.WRAP_CONTENT);
 			if (buttonIndex == 1) {
 				lp.setMargins(0, 50, 0, 0);
 			}
 			lp.setMargins(0, 10, 0, 0);
 			lp.addRule(RelativeLayout.BELOW, pos);
 			dbButton.setLayoutParams(lp);
 			((ViewGroup) layout).addView(dbButton, lp);
 			pos++;
 		}
 
 	}
 
 	private View.OnClickListener setListener(final Button b, final int destinationID) {
 		return new View.OnClickListener() {
 			public void onClick(View v) {
 				SF = SRC.getStoryFragment(destinationID);
 				//TODO: set marquee :)
 				((StoryFragmentReadActivity) getActivity()).setActionBarTitle(SF.getFragmentTitle());
 				update();
 			}
 		};
 	}
 
 	public void update() {
 		((ViewGroup) this.getView()).removeAllViews();
 		displayFragment(SF, rootView);
 	}
 }
