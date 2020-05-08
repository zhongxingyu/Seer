 package cz.cuni.mff.odcleanstore.linker.rules;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 public class SilkRule {
 	Integer id;
 	String label;
 	String linkType;
 	String sourceRestriction;
 	String targetRestriction;
 	String linkageRule;
 	BigDecimal filterThreshold;
 	Integer filterLimit;
 	List<Output> outputs;
 	
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	public String getLabel() {
 		return label;
 	}
 	public void setLabel(String label) {
 		this.label = label;
 	}
 	public String getLinkType() {
 		return linkType;
 	}
 	public void setLinkType(String linkType) {
 		this.linkType = linkType;
 	}
 	public String getSourceRestriction() {
 		return sourceRestriction;
 	}
 	public void setSourceRestriction(String sourceRestriction) {
 		this.sourceRestriction = sourceRestriction;
 	}
 	public String getTargetRestriction() {
 		return targetRestriction;
 	}
 	public void setTargetRestriction(String targetRestriction) {
 		this.targetRestriction = targetRestriction;
 	}
 	public String getLinkageRule() {
 		return linkageRule;
 	}
 	public void setLinkageRule(String linkageRule) {
 		this.linkageRule = linkageRule;
 	}
 	public BigDecimal getFilterThreshold() {
 		return filterThreshold;
 	}
 	public void setFilterThreshold(BigDecimal filterThreshold) {
 		this.filterThreshold = filterThreshold;
 	}
 	public Integer getFilterLimit() {
 		return filterLimit;
 	}
 	public void setFilterLimit(Integer filterLimit) {
 		this.filterLimit = filterLimit;
 	}
 	public List<Output> getOutputs() {
 		return outputs;
 	}
 	public void setOutputs(List<Output> outputs) {
 		this.outputs = outputs;
 	}
 }
