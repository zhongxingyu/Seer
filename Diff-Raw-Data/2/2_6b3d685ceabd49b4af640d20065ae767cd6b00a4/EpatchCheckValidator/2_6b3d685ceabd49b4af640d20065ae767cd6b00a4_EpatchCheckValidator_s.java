 package org.eclipse.emf.compare.epatch.dsl.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.compare.epatch.EpatchPackage;
 import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validator.CheckType;
 
 public class EpatchCheckValidator extends org.eclipse.xtext.check.AbstractCheckValidator {
 
 	public EpatchCheckValidator() {
 		configure();
 	}
 
 	protected void configure() {
 		addCheckFile("org::eclipse::emf::compare::epatch::dsl::validation::EpatchFastChecks", CheckType.FAST);
 		addCheckFile("org::eclipse::emf::compare::epatch::dsl::validation::EpatchChecks", CheckType.NORMAL);
 		addCheckFile("org::eclipse::emf::compare::epatch::dsl::validation::EpatchExpensiveChecks",
 				CheckType.EXPENSIVE);
 	}
 
 	@Override
 	protected List<? extends EPackage> getEPackages() {
 		List<EPackage> result = new ArrayList<EPackage>();
 		result.add(EpatchPackage.eINSTANCE);
 		return result;
 	}
 
 }
