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
 
 	@Override
 	protected Collection<Object> getItems(EObject element) {
 		List<Object> result = new ArrayList<Object>();
 		EAttribute enumFeature = (EAttribute) getFeature();
 		EEnum type = (EEnum) enumFeature.getEType();
		for (EEnumLiteral literal : type.getELiterals()) {
			result.add(literal.getLiteral());
 		}
 		return result;
 	}
 
 	@Override
 	protected String getEditChoice(EObject element, Object item) {
		return item.toString();
 	}
 }
