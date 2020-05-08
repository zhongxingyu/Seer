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
 
 
 public enum ColorEnum {
 	Black(0,0,0)
 	,Red(255,0,0)
 	,Green(0,255,0)
 	,Blue(0,0,255)
	,Purple(200,0,200);
 
 
 	public final int r;
 	public final int g;
 	public final int b;
 
 	ColorEnum(int r, int g, int b)
 	{
 		this.r = r;
 		this.g = g;
 		this.b = b;
 	}
 };
