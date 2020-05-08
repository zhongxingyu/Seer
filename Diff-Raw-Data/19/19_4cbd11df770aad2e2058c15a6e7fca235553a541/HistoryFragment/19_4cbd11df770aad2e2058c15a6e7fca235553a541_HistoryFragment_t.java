 package com.innutrac.poly.innutrac;
 
 /*
  * ******************************************************************************
  *   Copyright (c) 2013 Gabriele Mariotti.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *  *****************************************************************************
  */
 
 
 import android.app.Fragment;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 
 import com.echo.holographlibrary.Line;
 import com.echo.holographlibrary.LineGraph;
 import com.echo.holographlibrary.LinePoint;
 import com.innutrac.poly.innutrac.database.Food;
 import com.innutrac.poly.innutrac.database.FoodDatabase;
 
 import it.gmariotti.cardslib.library.internal.Card;
 import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
 import it.gmariotti.cardslib.library.internal.CardExpand;
 import it.gmariotti.cardslib.library.view.CardListView;
 
 public class HistoryFragment extends Fragment {
 	FoodDatabase fdb; //database with all food user has ever eaten
 	DailyPlan dp; //class with aggregated data of the food user has eaten today
 	ArrayList<Food> foodsEaten; //ArrayList with the food user has eaten just today
     Food lastFoodRemoved;
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_history, container, false);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         fdb = new FoodDatabase(getActivity());
         fdb.open("FoodRecord");
         dp = ((MainActivity)getActivity()).getTodayDailyPlan();
         foodsEaten = ((MainActivity)getActivity()).getTodayFoodList();
         
         initGraph();
         initCard();
     }
     
     private void initCard() {
         ArrayList<Food> foods = fdb.getAllEatenFood();
     	//Initialization an array of Cards
         ArrayList<Card> cards = new ArrayList<Card>();
         //System.out.println("FoodEatenToday Size = " + foodsEaten.size());
         int FoodsEatenIndex = foodsEaten.size() - 1;
         
         for (int i = (foods.size()-1); i >= 0; i--) {
         	Food tempFood = foods.get(i);
         	FoodDisplayCard card = new FoodDisplayCard(getActivity());
             //CardHeader header = new CardHeader(getActivity());
             //header.setButtonExpandVisible(true);
             //header.setTitle(tempFood.getName() );
             //card.addCardHeader(header);
             //card.addCardHeader(header);
             card.setTitle(tempFood.getName());
             card.setSecondaryTitle("Calories: " + tempFood.getCalories());
             card.setfdbID("" + tempFood.getEntryID() );
             card.setFoodsEatenID(FoodsEatenIndex--);
             card.setId("" + tempFood.getEntryID());
             
             card.setShadow(true);
             card.init();
             
             //This provide a simple (and useless) expand area
             CardExpand expand = new CardExpand(getActivity());
             //Set inner title in Expand Area
             expand.setTitle(getString(R.string.app_name));
             //Add expand to card
             card.addCardExpand(expand);
             cards.add(card);
         }
         
         CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
         
         //Enable undo controller
         mCardArrayAdapter.setEnableUndo(true);
         CardListView listView = (CardListView) getActivity().findViewById(R.id.myList);
         if (listView != null) {
             listView.setAdapter(mCardArrayAdapter);
         }
 
     }
     public class FoodDisplayCard extends Card {
 
         protected TextView mTitle;
         protected TextView mSecondaryTitle;
         protected String fdbID;
         protected int foodsEatenID;
         protected String title;
         protected String secondaryTitle;
 
         public FoodDisplayCard(Context context) {
             this(context, R.layout.card_inner_content);
         }
 
         public FoodDisplayCard(Context context, int innerLayout) {
             super(context, innerLayout);
         }
 
         private void init() {
 
             setSwipeable(true);
 
             setOnSwipeListener(new OnSwipeListener() {
                @Override
                public void onSwipe(Card card) {
                     //Toast.makeText(getContext(),title + " removed", Toast.LENGTH_SHORT).show();
             	   	int cardRemovedID = Integer.parseInt( ((FoodDisplayCard)card).getfdbID() );
             	   	lastFoodRemoved = fdb.getFood(cardRemovedID);
             	   	fdb.deleteFood(cardRemovedID);
             	   	foodsEaten.remove(((FoodDisplayCard)card).getFoodsEatenID());
             	   	dp.removeFood(lastFoodRemoved);
            	   	
            	   	((MainActivity)getActivity()).saveObjectState();
            	   	
                     //System.out.println("FoodEatenToday swipe Size = " + foodsEaten.size());
                }
             });
 
 
             setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
                 @Override
                 public void onUndoSwipe(Card card) {
                     Toast.makeText(getContext(), title + " restored", Toast.LENGTH_SHORT).show();
                     //int cardRestoredID = Integer.parseInt(card.getId() );
                     //no way to add it back into database right now according to index
                     fdb.addToFoodRecord(lastFoodRemoved);
                     foodsEaten.add(lastFoodRemoved);
                     dp.eatFood(lastFoodRemoved);
                    
                    ((MainActivity)getActivity()).saveObjectState(); 
                 }
             });
 
         }
 
         @Override
         public void setupInnerViewElements(ViewGroup parent, View view) {
 
             //Retrieve elements
             mTitle = (TextView) parent.findViewById(R.id.myapps_main_inner_title);
             mSecondaryTitle = (TextView) parent.findViewById(R.id.myapps_main_inner_secondaryTitle);
 
             if (mTitle != null)
                 mTitle.setText(title);
 
             if (mSecondaryTitle != null)
                 mSecondaryTitle.setText(secondaryTitle);
         }
 
 
         public String getTitle() {
             return title;
         }
 
         public void setTitle(String title) {
             this.title = title;
         }
 
         public String getSecondaryTitle() {
             return secondaryTitle;
         }
 
         public void setSecondaryTitle(String secondaryTitle) {
             this.secondaryTitle = secondaryTitle;
         }
         
         public void setfdbID(String id) {
         	fdbID = id;
         }
         
         public String getfdbID() {
         	return fdbID;
         }
         
         public void setFoodsEatenID(int id) {
         	foodsEatenID = id;
         }
         
         public int getFoodsEatenID() {
         	return foodsEatenID;
         }
     }
     
     public void initGraph() {
 		LineGraph lg = (LineGraph)getView().findViewById(R.id.linegraph);
 		
 		Line l = new Line();
 		Line limit_line = new Line();
 		LinePoint p = new LinePoint();
 		
 		
 		//need to find max total calories from mainactivity
 		//draw the limit line
 		limit_line.setShowingPoints(false);
 		p.setX(0);
 		p.setY(5);
 		limit_line.addPoint(p);
 		p=new LinePoint();
 		p.setX(15);
 		p.setY(5);
 		limit_line.addPoint(p);
 		limit_line.setColor(Color.parseColor("#FFBB33"));
 		//
 		
 		FoodDatabase totaledFoodDB = new FoodDatabase(getActivity() );
 		totaledFoodDB.open("UserTotalyDailyIntake");
 		ArrayList<DailyPlan> tempArray = totaledFoodDB.getAllDailyIntakes();
 		//draw the last 7 totalled calories 
 		if(tempArray.size() > 0) {
 			
 		}
 		totaledFoodDB.close();
 		
 		lg.addLine(limit_line);
 		lg.setRangeY(0, 10);
 		lg.setLineToFill(0);
 		
 	}
     
 }
