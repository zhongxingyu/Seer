 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxi.status;
 
 import java.io.Serializable;
 
 public class StatusValue implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private int value;
 	private int additional;
 	private int additionalPercent;
 	private int additionalPercentWithCap;
 	private int cap;
 
 	public StatusValue() { this(0, 0, 0); };
 	public StatusValue(int value, int additional) { this(value, additional, 0); };
 	public StatusValue(StatusValue from) { this(from.value, from.additional, from.additionalPercent); };
 	public StatusValue(int value, int additional, int percent) { this(value, additional, percent, 0, 0); };
 	public StatusValue(int value, int additional, int percent, int percentwithcap, int cap) { this.value = value; this.additional = additional; this.additionalPercent = percent; this.additionalPercentWithCap = percentwithcap; this.cap = cap; };
 
 	// Accessor
 	public void setValue(int value) { this.value = value; };
 	public void setAdditional(int additional) { this.additional = additional; };
 	public void setAdditionalPercent(int additionalPercent) { this.additionalPercent = additionalPercent; };
 	public void setAdditionalPercentWithCap(int additionalPercentWithCap) { this.additionalPercentWithCap = additionalPercentWithCap; };
 	public void setCap(int cap) { this.cap = cap; };
 
 	public int getValue() { return value; };
 	public int getAdditional() { return additional; };
 	public int getAdditionalPercent() { return additionalPercent; };
 	public int getAdditionalPercentWithCap() { return additionalPercentWithCap; };
 	public int getCap() { return cap; };
 	
 	static public int makePercentValue(int value, int decimal) {
 		return value * 100 + decimal % 100;
 	}
 	
 	public int getTotal() {
 		int v, p;
 		v = value + additional;
 		v += v * additionalPercent / 10000;
 		p = v * additionalPercentWithCap / 10000;
 		if (cap > 0)
 			p = Math.min(p, cap);
 		v += p;
 		
 		return v;
 	}
 	public void add(StatusValue value) {
 		this.value += value.value;
 		this.additional += value.additional;
 		this.additionalPercent += value.additionalPercent;
 		if (this.cap == 0) {
 			this.additionalPercentWithCap = value.additionalPercentWithCap;
 			this.cap = value.cap;
 		}
 	};
 	public void diff(StatusValue value) {
 		int v1, v2;
 
 		if (this.value == 0 && this.additional == 0 && value.value == 0 && value.additional == 0
 				&& (this.additionalPercent != 0 || value.additionalPercent != 0)) {
			this.value = this.additionalPercent / 100;
 			this.additional = 0;
 			this.additionalPercent = value.additionalPercent - this.additionalPercent;
 			this.additionalPercentWithCap = 0;
 			this.cap = 0;
 		} else {
 			v1 = getTotal();
 			v2 = value.getTotal();
 			
 			this.value = v1;
 			this.additional = v2 - v1;
 			this.additionalPercent = 0;
 			this.additionalPercentWithCap = 0;
 			this.cap = 0;
 		}
 		return;
 	}
 
 	static public StatusValue valueOf(String parameter) {
 		StatusValue newValue = new StatusValue();
 		int modifier, start, end, value, cap;
 		boolean additional, percent;
 
 		additional = true;
 		percent = false;
 		cap = 0;
 		start = 0;
 		end = parameter.length();
 		modifier = 1;
 		if (parameter.startsWith("-")) {
 			modifier = -1;
 			additional = true;
 			start++;
 		} else if (parameter.startsWith("+")) {
 			modifier = 1;
 			additional = true;
 			start++;
 		}
 		if (parameter.endsWith(")")) {
 			String tmp[] = parameter.split("\\(");
 			if (tmp.length == 2) {
 				try {
 					cap = Integer.parseInt(tmp[1].substring(0, tmp[1].length() - 1)); // Ignore tailing ')'.
 				} catch (NumberFormatException e) {
 					return null;
 				}
 				if (cap <= 0) { // Something wrong.
 					return null;
 				}
 				parameter = tmp[0];
 				end = parameter.length();
 			}
 		}
 		if (parameter.endsWith("%")) {
 			String tmp[] = parameter.split("\\.");
 			int decimal;
 
 			additional = true;
 			percent = true;
 			end--;
 			
 			decimal = 0;
 			if (tmp.length == 2) {
 				try {
 					decimal = Integer.parseInt(tmp[1].substring(0, tmp[1].length() - 1));  // ignore trailing '%'
 					switch (tmp[1].length() - 1) {
 					case 0:
 						break;
 					case 1:
 						decimal *= 10;
 						break;
 					case 2:
 						break;
 					default:
 						while (decimal >= 1000) {
 							decimal /= 10;
 						}
 						break;
 					}
 				} catch (NumberFormatException e) {
 					return null;
 				}
 				
 				parameter = tmp[0];
 				end = parameter.length();
 			}
 			
 			try {
 				value = Integer.parseInt(parameter.substring(start, end));
 			} catch (NumberFormatException e) {
 				return null;
 			}
 			value = value * 100 + decimal;
 		} else {
 	
 			try {
 				value = Integer.parseInt(parameter.substring(start, end));
 			} catch (NumberFormatException e) {
 				return null;
 			}
 		}
 		value *= modifier;
 
 		if (additional) {
 			if (percent) {
 				if (cap > 0) {
 					newValue.setAdditionalPercentWithCap(value);
 					newValue.setCap(cap);
 				} else {
 					newValue.setAdditionalPercent(value);
 				}
 			} else {
 				newValue.setAdditional(value);
 			}
 		} else {
 			newValue.setValue(value);
 		}
 		
 		return newValue;
 	}
 }
