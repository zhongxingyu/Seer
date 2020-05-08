 /**
  *  Copyright 2014 Diego Ceccarelli
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 /**
  *  Copyright 2014 Diego Ceccarelli
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package eu.europeana.querylog.learn.measure;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import eu.europeana.querylog.QueryAssessment;
 import eu.europeana.querylog.QueryAssessment.RelevantDocument;
 
 /**
  * Computes Precision.
  * 
  * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
  * 
  *         Created on Mar 11, 2014
  */
 public class Precision extends AbstractMeasure implements Measure {
 
 	public static final String NAME = "P";
 
 	public Precision() {
 		name = NAME;
 	}
 
 	@Override
 	public double match(List<String> results, QueryAssessment assessment) {
 		Set<String> relevant = new HashSet<String>();
 		for (RelevantDocument rd : assessment.getAssessment()) {
 			relevant.add(rd.getUri());
 
 		}
 		int tp = truePositive(results, relevant);
 		return (double) tp / (double) results.size();
 	}
 
 	private int truePositive(List<String> results, Set<String> relevant) {
 		int tp = 0;
 		for (String uri : results) {
 			if (relevant.contains(uri)) {
 				tp++;
 			}
 		}
 		return tp;
 	}
 }
