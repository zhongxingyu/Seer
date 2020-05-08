 package org.eclipse.gmf.tooling.runtime.parsers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 
 public class EnumParser extends ChoiceParserBase {
 
 	public EnumParser(EAttribute enumFeature) {
 		super(enumFeature);
 	}
 
	@SuppressWarnings("unchecked")
 	@Override
 	protected Collection<Object> getItems(EObject element) {
 		List<Object> result = new ArrayList<Object>();
 		EAttribute enumFeature = (EAttribute) getFeature();
 		EEnum type = (EEnum) enumFeature.getEType();
		@SuppressWarnings("rawtypes")
		Class<? extends Enum> enumeratorClass = (Class<? extends Enum>) type.getInstanceClass();
		for (EEnumLiteral eLiteral : type.getELiterals()) {
			Object literalValue = Enum.valueOf(enumeratorClass, eLiteral.getLiteral());
			result.add(literalValue);
 		}
 		return result;
 	}
 
 	@Override
 	protected String getEditChoice(EObject element, Object item) {
		return ((Enum<?>) item).name();
 	}

 }
