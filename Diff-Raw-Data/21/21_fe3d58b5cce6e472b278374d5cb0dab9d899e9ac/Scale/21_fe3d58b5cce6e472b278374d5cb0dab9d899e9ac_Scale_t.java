 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // Scale.java
 // Since: 2010/09/28
 //
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.canvas;
 
 import org.utgenome.gwt.utgb.client.canvas.GWTGraphCanvas.GraphStyle;
 
 /**
  * Scale for drawing bar graphs
  * 
  * 
  * @author leo
  * 
  */
 public class Scale {
 
 	private float min;
 	private float max;
 	private int height;
 	private boolean logScale = false;
 	private float logBase = 2;
 	private boolean isReverseYAxis = false;
 
 	public Scale() {
 
 	}
 
 	public Scale(GraphStyle style) {
 		updateStyle(style);
 	}
 
 	public float getMin() {
 		return min;
 	}
 
 	public float getMax() {
 		return max;
 	}
 
 	private void setMinMax(float min, float max) {
 		this.min = min;
 		this.max = max;
 		isReverseYAxis = min > max;
 	}
 
 	public void updateStyle(GraphStyle style) {
 		this.height = style.windowHeight;
 		this.logScale = style.logScale;
 		this.logBase = style.logBase;
 		if (!style.autoScale)
 			setMinMax(style.minValue, style.maxValue);
 		else
			setMinMax(style.autoScaledMin, style.autoScaledMax);
 	}
 
 	/**
 	 * Return pixel Y position of the given graph value
 	 * 
 	 * @param value
 	 * @return
 	 */
 	float getYPosition(float value) {
 
 		if (min == max)
 			return 0.0f;
 
 		float tempMin = max < min ? max : min;
 		float tempMax = max > min ? max : min;
 
 		if (logScale) {
 			value = getLogValue(value);
 			tempMax = getLogValue(tempMax);
 			tempMin = getLogValue(tempMin);
 		}
 		float valueHeight = (value - tempMin) / (tempMax - tempMin) * height;
 
 		if (isReverseYAxis)
 			return valueHeight;
 		else
 			return height - valueHeight;
 	}
 
 	public float getLogValue(float value) {
 		if (Math.log(logBase) == 0.0)
 			return value;
 
 		float temp = 0.0f;
 		if (value > 0.0f) {
 			temp = (float) (Math.log(value) / Math.log(logBase) + 1.0);
 			if (temp < 0.0f)
 				temp = 0.0f;
 		}
 		else if (value < 0.0f) {
 			temp = (float) (Math.log(-value) / Math.log(logBase) + 1.0);
 			if (temp < 0.0f)
 				temp = 0.0f;
 			temp *= -1.0f;
 		}
 		return temp;
 	}
 
 }
