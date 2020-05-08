 package org.scriptbox.ui.shared.tree;
 
 import java.util.Date;
 import java.util.List;
 
 import org.scriptbox.metrics.model.Metric;
 import org.scriptbox.ui.shared.timed.TimeBasedLoadResult;
 
public class MetricRangeDto implements TimeBasedLoadResult<Metric> {
 
 	private Date first;
 	private Date last;
 	private Date start;
 	private Date end;
 	private List<Metric> metrics;
 	
 	public MetricRangeDto( long first, long last, long start, long end, List<Metric> metrics ) {
 		this.first = new Date(first);
 		this.last = new Date(last);
 		this.start = new Date(start);
 		this.end = new Date(end);
 	}
 
 	public Date getStart() {
 		return start;
 	}
 
 	public Date getEnd() {
 		return end;
 	}
 
 	public Date getFirst() {
 		return first;
 	}
 	
 	public Date getLast() {
 		return last;
 	}
 	
 	public List<Metric> getData() {
 		return metrics;
 	}
 	
 }
