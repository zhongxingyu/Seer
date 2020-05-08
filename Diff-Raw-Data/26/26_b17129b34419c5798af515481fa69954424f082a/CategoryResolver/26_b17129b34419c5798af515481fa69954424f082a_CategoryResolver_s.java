 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.wordcount.categorized;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.steps.wordcount.common.Metrics;
 import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;
 
 /**
  * Helper class for removal of categories that are superseded by other higher-ranked categories from the same category group.
  * <p>If a text fragment has been matched against more than one category, and the matching categories belong to the same group,
  * e.g. all are GMX-categories, then only one category is left, namely the one ranked the highest. Ranking of categories is
  * defined by the sequence of category handler steps in a list passed to the constructor.
  * <p>The class modifies count-related metrics in a MetricsAnnotation attached to a given resource.  
  * The superseded metrics are removed, but if they contained non-zero values, those values are remembered in the
  * removedCategories hash map to later be subtracted from accumulated counts in resources of END_BATCH_ITEM and END_BATCH
  * events.  
  * @see ScopingReportStep.
  */
 public class CategoryResolver {
 
 	private List<String> gmxCategories = new LinkedList<String>(); // Original sequence is preserved by LinkedList
 	private List<String> okapiCategories = new LinkedList<String>();
 	private Map<String, Long> removedCategories = new HashMap<String, Long>();
 		
 	public CategoryResolver(List<IPipelineStep> steps) {
 		super();
 		for (IPipelineStep step : steps) {
 			if (step instanceof CategoryHandler) {
 				CategoryHandler handler = (CategoryHandler) step;
 				switch (handler.getCategoryGroup()) {
 				
 				case GMX_WORD_COUNTS:
 					gmxCategories.add(handler.getMetric());
 					break;
 					
 				case OKAPI_WORD_COUNTS:
 					okapiCategories.add(handler.getMetric());
 					break;
 				}
 			}
 		}
 	}
 
 	public void reset() {
 		removedCategories.clear();
 	}
 	
 	public void resolve(MetricsAnnotation ma, boolean contributesInTotal) {
 		if (ma == null) return;				
 		Metrics metrics = ma.getMetrics();
 		
 		// Can't do it on metrics directly for concurrency reasons
 		List<String> m = new LinkedList<String>(metrics);
 		
 		for (String metric : m) {
 			// Remove all metrics lower ranked
 			int index = gmxCategories.indexOf(metric);			
 			if (index > -1) {
 				for (int i = index + 1; i < gmxCategories.size(); i++) {
 					removeCategory(metrics, gmxCategories.get(i), contributesInTotal);					
 				}
 				continue;
 			}
 			
 			// Remove all metrics lower ranked
 			index = okapiCategories.indexOf(metric);			
 			if (index > -1) {
 				for (int i = index + 1; i < okapiCategories.size(); i++) {
 					removeCategory(metrics, okapiCategories.get(i), contributesInTotal);
 				}
 			}			
 		}
 	}
 	
 	private void removeCategory(Metrics metrics, String name, boolean contributesInTotal) {
 		long value = 0;
 		
 		if (contributesInTotal) {
 			value = Util.getValue(removedCategories, name, 0L);
 			value += metrics.getMetric(name);
 			if (value == 0) {
 				removedCategories.remove(name); // Not to keep 0 values in the map
 			}
 			else {
 				removedCategories.put(name, value);
 			}
 		}		
 		
 		metrics.unregisterMetric(name);
 	}
 
 	private void resolveTU (ITextUnit tu) {
 		MetricsAnnotation ma = tu.getAnnotation(MetricsAnnotation.class);
 		resolve(ma, true);
 		
 		TextContainer tc = tu.getSource();
 		ma = tc.getAnnotation(MetricsAnnotation.class);
 		resolve(ma, false);
 		
 		for (Segment seg : tc.getSegments()) {
 			ma = seg.getAnnotation(MetricsAnnotation.class);
 			resolve(ma, false);
 		}
 		
 		for (LocaleId trgLoc : tu.getTargetLocales()) {
 			tc = tu.getTarget(trgLoc);
 			ma = tc.getAnnotation(MetricsAnnotation.class);
 			resolve(ma, false);
 			
 			for (Segment seg : tc.getSegments()) {
 				ma = seg.getAnnotation(MetricsAnnotation.class);
 				resolve(ma, false);
 			}
 		}
 	}
 	
 	public void resolve(IResource res, EventType eventType) {
 		switch (eventType) {
 		case TEXT_UNIT:
 			resolveTU((ITextUnit)res);
 			break;
 
 		case END_BATCH_ITEM:
 			resolveEnding((Ending) res, false);
 			break;
 			
 		case END_BATCH:
 			resolveEnding((Ending) res, true);
 			break;
 			
 		default:
 			break;
 		}
 	}
 
 	private void resolveEnding(Ending res, boolean isEndBatch) {		
 		MetricsAnnotation ma = res.getAnnotation(MetricsAnnotation.class);
 		
 		if (ma != null) {
 			Metrics metrics = ma.getMetrics();
 			if (isEndBatch) {
 				resolve(ma, false);
 				return;
 			}
 			
 			// Can't do it on metrics directly for concurrency reasons
 			List<String> m = new LinkedList<String>(metrics);
 			
 			for (String metric : m) {
 				long value = metrics.getMetric(metric);
 				value -= Util.getValue(removedCategories, metric, 0L);
 				
 				if (value <= 0) {
 					metrics.unregisterMetric(metric);
 				}
 				else {
 					metrics.setMetric(metric, value);
 				}
 			}
 		}
 	}
 	
 }
