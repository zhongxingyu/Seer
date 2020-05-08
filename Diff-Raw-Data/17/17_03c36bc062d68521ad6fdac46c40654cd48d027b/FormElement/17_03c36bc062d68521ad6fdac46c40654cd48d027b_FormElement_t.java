 package org.chai.kevin.form;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.LanguageService;
 import org.chai.kevin.Period;
 import org.chai.kevin.Translation;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.data.Type.ValuePredicate;
 import org.chai.kevin.form.FormValidationService.ValidatableLocator;
 import org.chai.kevin.location.DataLocation;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 
 @Entity(name = "FormElement")
 @Table(name = "dhsst_form_element")
 @Inheritance(strategy = InheritanceType.JOINED)
 public class FormElement {
 
 	protected Long id;
 	private RawDataElement rawDataElement;
 	private List<FormValidationRule> validationRules = new ArrayList<FormValidationRule>();
 	private Map<String, Translation> headers = new HashMap<String, Translation>();
 
 	public FormElement() {
 		super();
 	}
 
 	@Id
 	@GeneratedValue
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@ManyToOne(targetEntity = RawDataElement.class, optional = false)
 	@JoinColumn(nullable = false)
	public RawDataElement getRawDataElement() {
 		return rawDataElement;
 	}
 
	public void setRawDataElement(RawDataElement rawDataElement) {
 		this.rawDataElement = rawDataElement;
 	}
 
 	@OneToMany(mappedBy = "formElement", targetEntity = FormValidationRule.class, orphanRemoval=true)
 	public List<FormValidationRule> getValidationRules() {
 		return validationRules;
 	}
 
 	public void setValidationRules(List<FormValidationRule> validationRules) {
 		this.validationRules = validationRules;
 	}
 
 	public void addValidationRule(FormValidationRule validationRule) {
 		validationRule.setFormElement(this);
 		validationRules.add(validationRule);
 	}
 
 	@ElementCollection(targetClass = Translation.class)
 	@JoinTable(name = "dhsst_form_element_headers")
 	public Map<String, Translation> getHeaders() {
 		return headers;
 	}
 
 	public void setHeaders(Map<String, Translation> headers) {
 		this.headers = headers;
 	}
 	
 	@Transient
 	public String getDescriptionTemplate() {
 		return "/entity/form/formElementDescription";
 	}
 	
 	@Transient
 	public String getLabel(LanguageService languageService) {
		return languageService.getText(getRawDataElement().getNames());
 	}
 	
 	@Transient
 	public <T extends FormElement> void deepCopy(T copy, FormCloner cloner) {
		copy.setRawDataElement(getRawDataElement());
 	}	
 	
 	@Transient
 	public void copyRules(FormElement copy, FormCloner cloner) {
 		for (FormValidationRule rule : getValidationRules()) {
 			copy.getValidationRules().add(cloner.getValidationRule(rule));
 		}
 		for (Entry<String, Translation> entry : getHeaders().entrySet()) {
 			copy.getHeaders().put(entry.getKey(), entry.getValue());
 		}
 	}
 
 	@Transient
 	public void validate(DataLocation dataLocation, ElementCalculator calculator) {
 		Set<FormValidationRule> validationRules = calculator.getFormElementService().searchValidationRules(this, dataLocation.getType());
 		
 		for (FormValidationRule validationRule : validationRules) {
 			validationRule.evaluate(dataLocation, calculator);
 		}
 	}
 	
 	@Transient
 	public void executeSkip(DataLocation dataLocation, ElementCalculator calculator) {
 		Set<FormSkipRule> skipRules = calculator.getFormElementService().searchSkipRules(this);
 		
 		for (FormSkipRule skipRule: skipRules) {
 			skipRule.evaluate(dataLocation, calculator);
 		}
 	}
 	
 	@Transient
 	protected Value getValue(DataLocation dataLocation, final ElementSubmitter submitter) {
 		FormEnteredValue enteredValue = submitter.getFormElementService().getOrCreateFormEnteredValue(dataLocation, this);
 		final Type type = enteredValue.getType();
 		Value valueToSave = new Value(enteredValue.getValue().getJsonValue());
 		type.transformValue(valueToSave, new ValuePredicate() {
 			@Override
 			public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 				return submitter.transformValue(currentValue, currentType, currentPrefix);
 			}
 		});
 		return valueToSave;
 	}
 	
 	@Transient
 	public void submit(DataLocation dataLocation, Period period, ElementSubmitter submitter) {
 		Value valueToSave = getValue(dataLocation, submitter);
 		
		RawDataElementValue rawDataElementValue = submitter.getValueService().getDataElementValue(getRawDataElement(), dataLocation, period);
 		if (rawDataElementValue == null) {
			rawDataElementValue = new RawDataElementValue(getRawDataElement(), dataLocation, period, null);
 		}
 		rawDataElementValue.setValue(valueToSave);
 		
 		submitter.getValueService().save(rawDataElementValue);
 	}
 
 	public static class ElementSubmitter {
 		private FormElementService formElementService;
 		private ValueService valueService;
 		
 		public ElementSubmitter(FormElementService formElementService, ValueService valueService) {
 			this.formElementService = formElementService;
 			this.valueService = valueService;
 		}
 		
 		public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 			// if it is skipped we return NULL
 			if (currentValue.getAttribute("skipped") != null) currentValue.setJsonValue(Value.NULL_INSTANCE().getJsonValue());
 			// we remove the attributes
 			currentValue.setAttribute("skipped", null);
 			currentValue.setAttribute("invalid", null);
 			currentValue.setAttribute("warning", null);
 			
 			return true;
 		}	
 		
 		public ValueService getValueService() {
 			return valueService;
 		}
 		
 		public FormElementService getFormElementService() {
 			return formElementService;
 		}
 	}
 	
 	public static class ElementCalculator {
 		private FormValidationService formValidationService;
 		private FormElementService formElementService;
 		private ValidatableLocator validatableLocator;
 		private List<FormEnteredValue> affectedValues;
 		
 		public ElementCalculator(List<FormEnteredValue> affectedValues, FormValidationService formValidationService, FormElementService formElementService, ValidatableLocator validatableLocator) {
 			this.formElementService = formElementService;
 			this.formValidationService = formValidationService;
 			this.validatableLocator = validatableLocator;
 			this.affectedValues = affectedValues;
 		}
 		
 		public List<FormEnteredValue> getAffectedValues() {
 			return affectedValues;
 		}
 		
 		public ValidatableLocator getValidatableLocator() {
 			return validatableLocator;
 		}
 		
 		public FormElementService getFormElementService() {
 			return formElementService;
 		}
 		
 		public FormValidationService getFormValidationService() {
 			return formValidationService;
 		}
 		
 		public void addAffectedValue(FormEnteredValue value) {
 			this.affectedValues.add(value);
 		}
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof FormElement))
 			return false;
 		FormElement other = (FormElement) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 }
