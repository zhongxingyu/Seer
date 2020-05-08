 package org.scriptbox.ui.shared.tree;
 
import java.io.Serializable;

 import org.scriptbox.ui.shared.timed.TimeBasedLoadConfigBean;
 
 
public class MetricQueryDto extends TimeBasedLoadConfigBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	
 	private MetricTreeNodeDto node;
 	
 	public MetricTreeNodeDto getNode() {
 		return node;
 	}
 	public void setNode(MetricTreeNodeDto node) {
 		this.node = node;
 	}
 }
