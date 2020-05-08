 package org.chai.kevin.fct;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.reports.ReportTable;
 import org.chai.kevin.value.Value;
 
 public class FctTable extends ReportTable<FctTargetOption, CalculationLocation> {
 	
 	protected List<FctTargetOption> targetOptions;
 	private List<FctTarget> targets;
 	protected List<CalculationLocation> topLevelLocations;
 	
 	public FctTable(Map<CalculationLocation, Map<FctTargetOption, Value>> valueMap, List<FctTargetOption> targetOptions, List<FctTarget> targets, List<CalculationLocation> topLevelLocations) {
 		super(valueMap);
 		this.targetOptions = targetOptions;
 		this.targets = targets;
 		this.topLevelLocations = topLevelLocations;
 	}
 
 	public Double getMaxReportValue(){
 		Double dblMaxValue = 0d;
 		for(CalculationLocation topLevelLocation : topLevelLocations){
 			Map<FctTargetOption, Value> targetMap = valueMap.get(topLevelLocation);
 			Collection<Value> reportValues = targetMap.values();
 			for(Value reportValue : reportValues){
 				if (reportValue != null){
 					if(!reportValue.isNull()) {
 						Double doubleValue = reportValue.getNumberValue().doubleValue();					
 						if(doubleValue > dblMaxValue)
 							dblMaxValue = doubleValue;
 					}	
 				}					
 			}
 		}
 		return dblMaxValue;
 	}
 	
 	public List<FctTargetOption> getTargetOptions(){
 		return targetOptions;
 	}
 	
 	public List<FctTarget> getTargets(){
 		return targets;
 	}
 	
	public List<CalculationLocation> getTopLevelLocations(){
		return topLevelLocations;
	}
	
 	public Set<CalculationLocation> getLocations(){
 		return valueMap.keySet();
 	}	
 	
 	public boolean hasData(){
 		return (super.hasData());
 	}
 }
