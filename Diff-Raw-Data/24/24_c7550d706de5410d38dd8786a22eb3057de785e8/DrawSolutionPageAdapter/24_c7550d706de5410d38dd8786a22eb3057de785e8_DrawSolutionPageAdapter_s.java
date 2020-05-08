 /*
  * Fling! Solver
  * Copyright (C) 2013 - Jos Expsito <jose.exposito89@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package com.eggsoftware.flingsolver.gui;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.eggsoftware.flingsolver.R;
 import com.eggsoftware.flingsolver.solver.SolutionStep;
 
 /**
  * The DrawSolutionActivity class uses a ViewPager to present the solution steps.
  * This class extends PagerAdapter to bring to this activity the different solution steps views.
  */
 public class DrawSolutionPageAdapter extends PagerAdapter {
 
 	private ArrayList<SolutionStep> solution;
 	private Context context;  
 	
 	public DrawSolutionPageAdapter(Context context, ArrayList<SolutionStep> solution) {
 		super();
 		this.solution = solution;
 		this.context = context;
 	}
 	
 	@Override
 	public int getCount() {
 		return this.solution.size();
 	}
 
 	@Override
 	public boolean isViewFromObject(View view, Object object) {
 		return (view == (LinearLayout)object);
 	}
 
 	@Override  
     public void destroyItem(View collection, int position, Object view) {  
         ((ViewPager) collection).removeView((LinearLayout) view);
     }  
 	
 	@Override  
     public Object instantiateItem(View collection, int position) {
 		// Create the "Step N of T" TextView
 		TextView stepTextView = new TextView(this.context);
 		stepTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
 		stepTextView.setGravity(Gravity.CENTER);
 		stepTextView.setText(String.format(this.context.getResources().getString(R.string.step_of), position+1, this.solution.size()));
 		
		// Add the instructions
		TextView instrucionsTextView = null;
		if (position == 0 && this.solution.size() > 1) {
			instrucionsTextView = new TextView(this.context);
			instrucionsTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			instrucionsTextView.setGravity(Gravity.CENTER);
			instrucionsTextView.setText(this.context.getResources().getString(R.string.swipe_instructions));
		}
		
 		// Create the boar with the current step of the solution
         BoardCanvas board = new BoardCanvas(this.context);
         board.setBoardRepresentation(this.solution.get(position).getBoard());
         board.setArrow(this.solution.get(position).getRow(), this.solution.get(position).getCol(), this.solution.get(position).getDirection());
         
         // Add the components to the layout
         LinearLayout layout = new LinearLayout(this.context);
         layout.setOrientation(LinearLayout.VERTICAL);
         layout.setPadding(16, 20, 16, 16);
         layout.addView(stepTextView);
         layout.addView(board);
        if (instrucionsTextView != null)
        	layout.addView(instrucionsTextView);
         ((ViewPager) collection).addView(layout, 0);  
         return layout;
     }
 }
