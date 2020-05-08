 /**
  * Copyright 2013 iFactory Ltd.
  * 
  * Min Kim (minsikzzang@gmail.com)
  *
  * The Netty Project licenses this file to you under the Apache License,
  * version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at:
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.ifactory.client.greenwich;
 
 import java.io.Serializable;
 
 public class Temp implements Serializable {
   private double day;
 	private double min;
 	private double max;
 	private double night;
 	private double evening;
   private double morning;
 	/**
 	 * Temperature in Kelvin. Subtracted 273.15 from this figure to convert 
 	 * to Celsius.
 	 */
 	static double KELVIN = 273.15;
 	
 	static double roundUp(double value) {
 	  return (double)Math.round(value * 100) / 100;
 	}
 	
 	static final class Builder {
     private double day;
   	private double min;
   	private double max;
   	private double night;
   	private double evening;
     private double morning;
 		
 		public Builder(double day, double min, double max) {
 			this.day = day;
 			this.min = min;
 			this.max = max;
 		}
 
 		public Builder night(double night) {
 			this.night = night;
 			return this;
 		}
 		
 		public Builder evening(double evening) {
 			this.evening = evening;
 			return this;
 		}
 		
 		public Builder morning(double morning) {
 			this.morning = morning;
 			return this;
 		}
 				
 		public Temp build() {
 			return new Temp(this);
 		}
 	}
 	
 	private Temp(Builder builder) {
 		day = builder.day;
 		min = builder.min;
 		max = builder.max;
 		night = builder.night;			
 		evening = builder.evening;			
 		morning = builder.morning;			
 	}	
 	
 	public double getDay() {
 		return roundUp(day - KELVIN);
 	}
 	
 	public double getMax() {
		return roundUp(max - KELVIN);
 	}
 
 	public double getMin() {
		return roundUp(min - KELVIN);
 	}
 	
 	public double getNight() {
 	  return roundUp(night - KELVIN);
 	}
 	
 	public double getEvening() {
 	  return roundUp(evening - KELVIN);
 	}
 	
 	public double getMorning() {
 		return roundUp(morning - KELVIN);
 	}
 }
