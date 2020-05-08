 /*
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.primitive;
 
 
 public class Rect {
 	private int left;
 	private int top;
 	private int right;
 	private int bottom;
 
 	public Rect(int x, int y, int width, int height) {
 		this.left = x;
 		this.top = y;
 		this.right = x + width;
 		this.bottom = y + height;
 	}
 
 	public boolean contains(int x, int y)
 	{
 		if( x>=left && x<=right)
 		{
 			if( y >=top && y <= bottom)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	public int getLeft() {
 		return left;
 	}
 
 	public int getRight() {
 		return right;
 	}
 
 	public int getBottom() {
 		return bottom;
 	}
 
 	public int getTop() {
 		return top;
 	}
 
 	public int getWidth() {
 		return right -left;
 	}
 
 	public int getHeight() {
		return bottom-top;
 	}
 
 }
