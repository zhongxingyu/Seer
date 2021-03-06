 package com.gallatinsystems.survey.domain;
 
 import java.util.HashMap;
 import java.util.TreeMap;
 
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 
 import com.gallatinsystems.framework.domain.BaseDomain;
 import com.google.appengine.api.datastore.Key;
 
 @PersistenceCapable
 public class Question extends BaseDomain {
 
 	private static final long serialVersionUID = -9123426646238761996L;
 
 	public enum Type {
		GEO, TEXT, OPTION, VIDEO, PHOTO, NUMBER
 	};
 
 	private Type type = null;
 	private String tip = null;
 	private String text = null;
 	@NotPersistent
 	private HashMap<String, Translation> translationMap;
 	private Boolean dependentFlag = null;
 	private Boolean allowMultipleFlag = null;
 	private Boolean allowOtherFlag = null;
 	private Key dependentQuestionKey = null;
 	@NotPersistent
 	private TreeMap<Integer, QuestionOption> questionOptionMap = null;
 	private String validationRule = null;
 	@NotPersistent
 	private HashMap<Integer, QuestionHelpMedia> questionHelpMediaMap = null;
 	private Long questionGroupId;
 	private Integer order = null;
 	private Boolean mandatoryFlag = null;
 	private String path = null;
 
 	public Long getQuestionGroupId() {
 		return questionGroupId;
 	}
 
 	public void setQuestionGroupId(Long questionGroupId) {
 		this.questionGroupId = questionGroupId;
 	}
 
 	public HashMap<String, Translation> getTranslationMap() {
 		return translationMap;
 	}
 
 	public void setTranslationMap(HashMap<String, Translation> translationMap) {
 		this.translationMap = translationMap;
 	}
 
 	public void addQuestionOption(QuestionOption questionOption) {
 		if (getQuestionOptionMap() == null)
 			setQuestionOptionMap(new TreeMap<Integer, QuestionOption>());
 		getQuestionOptionMap().put(
 				questionOption.getOrder() != null ? questionOption.getOrder()
 						: getQuestionOptionMap().size() + 1, questionOption);
 	}
 
 	public void addHelpMedia(Integer order, QuestionHelpMedia questionHelpMedia) {
 		if (getQuestionHelpMediaMap() == null)
 			setQuestionHelpMediaMap(new HashMap<Integer, QuestionHelpMedia>());
 		getQuestionHelpMediaMap().put(order, questionHelpMedia);
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 	public Boolean getDependentFlag() {
 		return dependentFlag;
 	}
 
 	public void setDependentFlag(Boolean dependentFlag) {
 		this.dependentFlag = dependentFlag;
 	}
 
 	public Boolean getAllowMultipleFlag() {
 		return allowMultipleFlag;
 	}
 
 	public void setAllowMultipleFlag(Boolean allowMultipleFlag) {
 		this.allowMultipleFlag = allowMultipleFlag;
 	}
 
 	public Boolean getAllowOtherFlag() {
 		return allowOtherFlag;
 	}
 
 	public void setAllowOtherFlag(Boolean allowOtherFlag) {
 		this.allowOtherFlag = allowOtherFlag;
 	}
 
 	public Key getDependentQuestionKey() {
 		return dependentQuestionKey;
 	}
 
 	public void setDependentQuestionKey(Key dependentQuestionKey) {
 		this.dependentQuestionKey = dependentQuestionKey;
 	}
 
 	public String getValidationRule() {
 		return validationRule;
 	}
 
 	public void setValidationRule(String validationRule) {
 		this.validationRule = validationRule;
 	}
 
 	public void setQuestionOptionMap(
 			TreeMap<Integer, QuestionOption> questionOptionMap) {
 		this.questionOptionMap = questionOptionMap;
 	}
 
 	public TreeMap<Integer, QuestionOption> getQuestionOptionMap() {
 		return questionOptionMap;
 	}
 
 	public void setQuestionHelpMediaMap(
 			HashMap<Integer, QuestionHelpMedia> questionHelpMediaMap) {
 		this.questionHelpMediaMap = questionHelpMediaMap;
 	}
 
 	public HashMap<Integer, QuestionHelpMedia> getQuestionHelpMediaMap() {
 		return questionHelpMediaMap;
 	}
 
 	public void setOrder(Integer order) {
 		this.order = order;
 	}
 
 	public Integer getOrder() {
 		return order;
 	}
 
 	public void setMandatoryFlag(Boolean mandatoryFlag) {
 		this.mandatoryFlag = mandatoryFlag;
 	}
 
 	public Boolean getMandatoryFlag() {
 		return mandatoryFlag;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setTip(String tip) {
 		this.tip = tip;
 	}
 
 	public String getTip() {
 		return tip;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void addTranslation(Translation t) {
 		if (translationMap == null) {
 			translationMap = new HashMap<String, Translation>();
 		}
 		translationMap.put(t.getLanguageCode(), t);
 	}
 
 }
