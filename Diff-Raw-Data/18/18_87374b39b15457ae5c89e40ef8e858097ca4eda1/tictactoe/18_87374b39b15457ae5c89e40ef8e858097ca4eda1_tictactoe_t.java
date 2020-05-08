 package com.jeff;
 
 import android.util.Log;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.AbsoluteLayout.*;
 import android.widget.FrameLayout;
 import android.graphics.Color;
 import com.jeff.GridButton;
 
 
 public class tictactoe extends Activity implements View.OnClickListener {
 	TableLayout tl;	
 	TableRow[] tr;
 	GridButton[][] grid;
 	int gridSize = 3;
 	int currentPlayer = 1;
 
	private static final String DEFAULT_TEXT = " "; //fixes a bug where buttons resize when the text changes
	
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		
 	
 		if (getLastNonConfigurationInstance() == null) {//if first time
 			//set up main screen
 			tl = new TableLayout(this);//table layout
 			FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
 			tl.setLayoutParams(tlp);
 			tl.setStretchAllColumns(true);
 			
 			//table grids	
 			tr = new TableRow[gridSize];
 		
 			/*children must have layout params of their parent */
 			//layout params for rows
 			TableLayout.LayoutParams rl = new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1.0f);
 			//layout for cells in the row
 			TableRow.LayoutParams cl = new TableRow.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1.0f);
 
 			grid = new GridButton[gridSize][gridSize];
 			//loop to create cells
 			for (int i=0;i<=gridSize-1;i++) {
 				tr[i] = new TableRow(this);	
 				for (int j=0;j<=gridSize-1;j++) {
 					grid[i][j] = new GridButton(this,i,j);
 					grid[i][j].setOnClickListener(this);
					grid[i][j].setText(DEFAULT_TEXT);
 					tr[i].addView(grid[i][j],cl);
 				}
 				tl.addView(tr[i],rl);
 			}
 
 		}else if (getLastNonConfigurationInstance() != null) {//on restore
 			tl = (TableLayout) getLastNonConfigurationInstance();
 		}
 
 		setContentView(tl);
 
 	}
 
 	public void onClick(View v) {
 		GridButton gb = (GridButton) v;
 		//Log.i("TICTACTOE", "CLICKINFO: " + gb.row +  "," + gb.col);
 
 		//already set
		if (gb.getText() != DEFAULT_TEXT) 
 			return;
 
 		if(currentPlayer == 1){ 
 			grid[gb.row][gb.col].setText("X");	
 			grid[gb.row][gb.col].setTextColor(Color.RED);	
 			currentPlayer = 2;
 		}else if(currentPlayer == 2) {
 			grid[gb.row][gb.col].setTextColor(Color.BLUE);	
 			grid[gb.row][gb.col].setText("O");	
 			currentPlayer = 1;
 		}
 	}
 	//save state on rotate
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		if (tl != null) {//save table layout
 			ViewGroup vg = (ViewGroup) tl.getParent();//probably not good
 			vg.removeView(tl);
 			return(tl);
 		}
 		return super.onRetainNonConfigurationInstance();
 	}
 
 
 }
