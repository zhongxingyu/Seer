 /*
 New BSD License
 Copyright (c) 2012, MyBar Team All rights reserved.
 mybar@turbotorsk.se
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 �	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 �	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 �	Neither the name of the MyBar nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package se.turbotorsk.mybar;
 
 import java.util.LinkedList;
 
 import se.turbotorsk.mybar.model.Drink;
 import se.turbotorsk.mybar.model.Ingredient;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
 * This activity handles the ingredients.
  */
 public class IngredientAdapter extends ArrayAdapter<LinkedList> {
 
 	public IngredientAdapter(Context context, int textViewResourceId) {
 		super(context, textViewResourceId);
 		// TODO Auto-generated constructor stub
 	}
 
 	private LinkedList<Ingredient> items;
 
 	/**
	 * Constructor for IngredientAdapter.
 	 * 
 	 * @param context
 	 * @param resource
 	 * @param items
 	 */
 	public IngredientAdapter(Context context, int resource, LinkedList items) {
 		super(context, resource, items);
 		this.items = items;
 
 	}
 
 	/**
	 * This method gets the view for all ingredients.
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		View v = convertView;
 
 		if (v == null) {
 
 			LayoutInflater vi;
 			vi = LayoutInflater.from(getContext());
 			v = vi.inflate(R.layout.rowlayout, null);
 
 		}
 
 		Ingredient p = items.get(position);
 
 		if (p != null) {
 
 			TextView tt = (TextView) v.findViewById(R.id.drink);
 			if (tt != null) {
 
 				tt.setText(p.getName());
 			}
 		}
 
 		return v;
 	}
 
 	/**
	 * This method is fetching the name of an ingredient.
 	 * 
 	 * @param position
 	 * @return name of ingredient
 	 */
 	public String getIngredientName(int position) {
 		Ingredient ingredient = items.get(position);
 		return ingredient.getName();
 	}
 
 	/**
	 * This method is fetching the description of a recipe.
 	 * 
 	 * @param position
 	 * @return recipe description
 	 */
 	public String getDescription(int position) {
 		Ingredient ingredient = items.get(position);
 		return ingredient.getDescription();
 	}
 
 }
