 package org.scriptbox.ui.shared.tree;
 
 import org.scriptbox.ui.shared.timed.TimeBasedLoadConfigBean;
 
 
public class MetricQueryDto extends TimeBasedLoadConfigBean {
 
 	private MetricTreeNodeDto node;
 	
 	public MetricTreeNodeDto getNode() {
 		return node;
 	}
 	public void setNode(MetricTreeNodeDto node) {
 		this.node = node;
 	}
 }
