 /*    Dice in the dark. D & D app for the blind and seeing impaired,
  *    Copyright (C) <2013r>  <Lovisa Irpa Helgadottir>
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.plovergames.diceindark;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.util.Log;
 
 import com.plovergames.framework.GameObject;
 
 public class Die extends GameObject {
 
 	public static final float X = 1.0f;
 	public static final float Y = 1.0f;
 	public static final float DIE_WIDTH = 1.0f;
 	public static final float DIE_HEIGHT = 1.0f;
 	public final Random rand;
 	int sides;
 	int[] result;
 	//int result;
 	int numberOfDice;
 	boolean hasResult = false;
 	String name;
 	public Die(int sides) {
 		super(X, Y, DIE_WIDTH, DIE_HEIGHT);
 		this.sides = sides;
 		rand = new Random();
 		this.name = "d"+sides;
 		this.numberOfDice=1;
 		result = new int[sides];
 	}
 
 	public void shake(){
 		//Log.d("Die", "Shake");
 		hasResult=false;
 
 	}
 
 	public void thrown(){
 		//Log.d("Die","thrown");
 		//result=rand.nextInt(sides)+1;
 		int j= numberOfDice;
//		result[0]=rand.nextInt(j);
		for(int i=0; i<sides-1; i++){			
 			result[i] = rand.nextInt(j);
			j-=result[i];
 			Log.v("Die result",""+result[i]);
 		}
 		result[sides-1]= j;
		Log.v("Die result",""+result[sides-1]);
 		shuffleArray(result);
 		hasResult=true;
 
 
 	}
 
 	private static void shuffleArray(int[] a) {
 		int n = a.length;
 		Random random = new Random();
 		random.nextInt();
 		for (int i = 0; i < n; i++) {
 			int change = i + random.nextInt(n - i);
 			swap(a, i, change);
 		}
 	}
 
 	private static void swap(int[] a, int i, int change) {
 		int helper = a[i];
 		a[i] = a[change];
 		a[change] = helper;
 	}
 
 
 
 }
