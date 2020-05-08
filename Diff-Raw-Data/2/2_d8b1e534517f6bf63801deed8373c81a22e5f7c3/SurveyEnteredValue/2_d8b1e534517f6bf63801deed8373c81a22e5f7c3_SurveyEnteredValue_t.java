 package org.chai.kevin.survey.validation;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.data.Type.PrefixPredicate;
 import org.chai.kevin.data.Type.ValuePredicate;
 import org.chai.kevin.survey.Survey;
 import org.chai.kevin.survey.SurveyElement;
 import org.chai.kevin.survey.SurveySkipRule;
 import org.chai.kevin.survey.SurveyValidationRule;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.Value;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.NaturalId;
 import org.hisp.dhis.organisationunit.OrganisationUnit;
 
 @Entity(name="SurveyEnteredValue")
 @Table(name="dhsst_survey_entered_value", 
 		uniqueConstraints=@UniqueConstraint(columnNames={"surveyElement", "organisationUnit"}
 ))
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class SurveyEnteredValue implements Serializable {
 
 	private static final long serialVersionUID = -7262945749639062551L;
 	
 	private Long id;
 	private SurveyElement surveyElement;
 	private Value value;
 	private Value lastValue;
 	private OrganisationUnit organisationUnit;
 	
 	public SurveyEnteredValue() {}
 	
 	public SurveyEnteredValue(SurveyElement surveyElement, OrganisationUnit organisationUnit, Value value, Value lastValue) {
 		this.surveyElement = surveyElement;
 		this.organisationUnit = organisationUnit;
 		this.value = value;
 		this.lastValue = lastValue;
 	}
 
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	@NaturalId
 	@OneToOne(targetEntity=SurveyElement.class, fetch=FetchType.LAZY)
 	public SurveyElement getSurveyElement() {
 		return surveyElement;
 	}
 	
 	public void setSurveyElement(SurveyElement surveyElement) {
 		this.surveyElement = surveyElement;
 	}
 	
 	@Embedded
 	@AttributeOverrides({
         @AttributeOverride(name="jsonValue", column=@Column(name="value", nullable=false))
 	})
 	public Value getValue() {
 		return value;
 	}
 	
 	public void setValue(Value value) {
 		this.value = value;
 	}
 	
 	@Embedded
 	@AttributeOverrides({
         @AttributeOverride(name="jsonValue", column=@Column(name="last_value", nullable=true))
 	})
 	public Value getLastValue() {
 		return lastValue;
 	}
 	
 	public void setLastValue(Value lastValue) {
 		this.lastValue = lastValue;
 	}
 	
 	@NaturalId
 	@ManyToOne(targetEntity=OrganisationUnit.class, fetch=FetchType.LAZY)
 	public OrganisationUnit getOrganisationUnit() {
 		return organisationUnit;
 	}
 	
 	public void setOrganisationUnit(OrganisationUnit organisationUnit) {
 		this.organisationUnit = organisationUnit;
 	}
 	
 	@Transient
 	public Type getType() {
 		return surveyElement.getDataElement().getType();
 	}
 	
 	public void setInvalid(SurveyValidationRule validationRule, Set<String> prefixes) {
 		setAttribute("invalid", validationRule.getId().toString(), prefixes);
 	}
 
 	public Set<String> getErrors(String prefix) {
 		try {
 			return Utils.split(getType().getAttribute(value, prefix, "invalid"));
 		}
 		catch (IndexOutOfBoundsException e) {
 			return new HashSet<String>();
 		}
 	}
 	
 	public Set<String> getUnacceptedErrors(String prefix) {
 		try {
 			Set<String> invalidRules = Utils.split(getType().getAttribute(value, prefix, "invalid"));
 			Set<String> acceptedRules = Utils.split(getType().getAttribute(value, prefix, "warning"));
 			
 			return new HashSet<String>(CollectionUtils.subtract(invalidRules, acceptedRules));
 		} 
 		catch (IndexOutOfBoundsException e) {
 			return new HashSet<String>();
 		}
 	}
 	
 	public boolean isValid(String prefix) {
 		return getUnacceptedErrors(prefix).isEmpty();
 	}
 	
 	public void setSkipped(SurveySkipRule skipRule, Set<String> prefixes) {
 		setAttribute("skipped", skipRule.getId().toString(), prefixes);
 	}
 	
 	public Set<String> getSkipped(String prefix) {
 		try {
 			return Utils.split(getType().getAttribute(value, prefix, "skipped"));
 		}
 		catch (IndexOutOfBoundsException e) {
 			return new HashSet<String>();
 		}
 	}
 	
 	public boolean isSkipped(String prefix) {
 		return !getSkipped(prefix).isEmpty();
 	}
 	
 	public boolean isAcceptedWarning(SurveyValidationRule rule, String prefix) {
 		try {
 			return Utils.split(getType().getAttribute(value, prefix, "warning")).contains(rule.getId().toString());
 		}
 		catch (IndexOutOfBoundsException e) {
 			return false;
 		}
 	}
 	
 	@Transient
 	public Set<String> getSkippedPrefixes() {
 		return getPrefixesWithAttribute("skipped").keySet();
 	}
 	
 	@Transient
 	public Set<String> getInvalidPrefixes() {
 		return getPrefixesWithAttribute("invalid").keySet();
 	}
 	
 	@Transient
 	public Boolean isInvalid() {
 		// we get the list of all the invalid prefixes that
 		// have not been accepted
 		Map<String, Value> invalidPrefixes = getPrefixesWithAttribute("invalid");
 		Map<String, Value> acceptedPrefixes = getPrefixesWithAttribute("warning");
 		
 		Set<String> invalidAndUnacceptedPrefixes = new HashSet<String>();
 		for (String invalidPrefix : invalidPrefixes.keySet()) {
 			Set<String> invalidRules = Utils.split(invalidPrefixes.get(invalidPrefix).getAttribute("invalid"));
 			Set<String> acceptedRules = new HashSet<String>();
 			if (acceptedPrefixes.containsKey(invalidPrefix)) {
 				acceptedRules.addAll(Utils.split(acceptedPrefixes.get(invalidPrefix).getAttribute("warning")));
 			}
 			
 			if (!CollectionUtils.subtract(invalidRules, acceptedRules).isEmpty()) invalidAndUnacceptedPrefixes.add(invalidPrefix);
 		}
 		
 		// element is invalid if those prefixes are not all skipped
 		Set<String> skippedPrefixes = getSkippedPrefixes();
 		
 		return !CollectionUtils.subtract(
 			invalidAndUnacceptedPrefixes, 
 			skippedPrefixes
 		).isEmpty();
 	}
 	
 	@Transient
 	public Boolean isComplete() {
 		// element is complete if all the non-skipped values are not-null
 		// regardless of whether they are valid or not
 		Set<String> skippedPrefixes = getSkippedPrefixes();
 		Map<String, Value> nullPrefixes = surveyElement.getDataElement().getType().getPrefixes(value, new PrefixPredicate() {
 			@Override
 			public boolean holds(Type type, Value value, String prefix) {
 				return value.isNull();
 			}
 		});
 		
		return CollectionUtils.subtract(nullPrefixes.keySet(), skippedPrefixes).isEmpty();
 	}
 	
 	private void setAttribute(final String attribute, final String id, Set<String> prefixes) {
 		Map<String, Value> prefixesWithId = getType().getPrefixes(value, new PrefixPredicate() {
 			@Override
 			public boolean holds(Type type, Value value, String prefix) {
 				return Utils.split(value.getAttribute(attribute)).contains(id);
 			}
 		});
 		
 		final Map<String, String> newPrefixAttributes = new HashMap<String, String>();
 		// remove the attribute from prefixes which are not in the set
 		for (String prefixWithId : prefixesWithId.keySet()) {
 			if (!prefixes.contains(prefixWithId)) {
 				Set<String> attributeValue = Utils.split(getType().getAttribute(value, prefixWithId, attribute));
 				attributeValue.remove(id);
 				
 				String newAttributeValue = Utils.unsplit(attributeValue);
 				if (newAttributeValue.isEmpty()) newAttributeValue = null;
 				newPrefixAttributes.put(prefixWithId, newAttributeValue);
 			}
 		}
 		
 		// add it on prefixes which are in the set
 		for (String prefix : prefixes) {
 			Set<String> attributeValue = Utils.split(getType().getAttribute(value, prefix, attribute));
 			attributeValue.add(id);
 			newPrefixAttributes.put(prefix, Utils.unsplit(attributeValue));
 		}
 		
 		// set the attribute to the new value
 		getType().transformValue(value, new ValuePredicate() {
 			
 			@Override
 			public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 				if (newPrefixAttributes.containsKey(currentPrefix)) {
 					String currentAttribute = currentValue.getAttribute(attribute);
 					if (currentAttribute == null || !currentAttribute.equals(newPrefixAttributes.get(currentPrefix))) {
 						currentValue.setAttribute(attribute, newPrefixAttributes.get(currentPrefix));
 						return true;
 					}
 				}
 				return false;
 			}
 			
 		});
 	}
 	
 	private Map<String, Value> getPrefixesWithAttribute(final String attribute) {
 		return getType().getPrefixes(value, new PrefixPredicate() {
 			@Override
 			public boolean holds(Type type, Value value, String prefix) {
 				String attributeValue = value.getAttribute(attribute);
 				if (attributeValue != null) return true;
 				return false;
 			}
 		});
 	}
 	
 	
 	@Transient
 	public Survey getSurvey() {
 		return surveyElement.getSurvey();
 	}
 
 	@Override
 	public String toString() {
 		return "SurveyEnteredValue [value=" + value + ", lastValue="
 				+ lastValue + "]";
 	}
 
 }
