 package org.iucn.sis.shared.api.criteriacalculator;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.fields.RedListCriteriaField;
 import org.iucn.sis.shared.api.models.fields.RedListFuzzyResultField;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 
 public class ExpertUtils {
 
 	public static void processAssessment(Assessment currentAssessment) {
 		FuzzyExpImpl expert = new FuzzyExpImpl();
 		ExpertResult expertResult = expert.doAnalysis(currentAssessment);
 		
 		RedListCriteriaField redListCriteriaField; {
 			Field proxy = currentAssessment.getField(CanonicalNames.RedListCriteria);
 			if (proxy == null) {
 				proxy = new Field(CanonicalNames.RedListCriteria, currentAssessment);
 				currentAssessment.getField().add(proxy);
 			}
 			redListCriteriaField = new RedListCriteriaField(proxy);
 		}
 		
 		RedListFuzzyResultField redListFuzzyResultField; {
 			Field proxy = currentAssessment.getField(CanonicalNames.RedListFuzzyResult);
 			if (proxy == null) {
 				proxy = new Field(CanonicalNames.RedListFuzzyResult, currentAssessment);
 				currentAssessment.getField().add(proxy);
 			}
 			redListFuzzyResultField = new RedListFuzzyResultField(proxy);
 		}
 		
 		redListCriteriaField.setGeneratedCriteria(expertResult.getCriteriaString());
 
 		if (expertResult.getResult().equals(ResultCategory.DD)) {
 			redListCriteriaField.setGeneratedCategory("DD");
 			
 			redListFuzzyResultField.clearCriteria();
 			redListFuzzyResultField.setCategory("DD");
 			redListFuzzyResultField.setCriteriaCR("");
 			redListFuzzyResultField.setCriteriaEN("");
 			redListFuzzyResultField.setCriteriaVU("");
 			redListFuzzyResultField.setCriteriaMet("");
 			redListFuzzyResultField.setResult("-1,-1,-1");
 		}
 		else {
 			redListCriteriaField.setGeneratedCategory(expertResult.getAbbreviatedCategory());
 			
 			redListFuzzyResultField.setExpertResult(expertResult);
 
 			if (currentAssessment.isRegional()
 					&& currentAssessment.getField(CanonicalNames.RegionExpertQuestions) != null) {
 				
 				Field regExpertField = currentAssessment.getField(CanonicalNames.RegionExpertQuestions);
				if (regExpertField != null) {
					PrimitiveField<?> prim = regExpertField.getPrimitiveField("answers");
					String regionalExpString = prim == null ? "" : prim.getRawValue();
 					String[] regionalExpData = regionalExpString.split(",");
 
 					if (regionalExpData.length > 2) {
 						String upDown = regionalExpData[0];
 						String amount = regionalExpData[1];
 						String category = expertResult.getAbbreviatedCategory();
 
 						if (upDown.equals(RegionalExpertQuestions.UPGRADE)) {
 							int amountUp = Integer.valueOf(amount);
 							category = slideCategory(amountUp, category);
 						} else if (upDown.equals(RegionalExpertQuestions.DOWNGRADE)) {
 							int amountDown = Integer.valueOf(amount);
 							category = slideCategory(-1 * amountDown, category);
 						}
 
 						redListCriteriaField.setGeneratedCategory(category);
 						
 						redListFuzzyResultField.setCategory(category);
 					}
 				}
 			} else {
 				Debug.println("Couldn't find regional expert question data.");
 			}
 		}
 		
 		currentAssessment.generateFields();
 	}
 	
 	private static String slideCategory(int amount, String startValue) {
 		List<String> cats = Arrays.asList(new String[] { "LC", "VU", "EN", "CR" });
 		int index = cats.indexOf(startValue);
 
 		if (index < 0) {
 			return startValue;
 		} else {
 			int newIndex = index + amount;
 
 			if (newIndex < 0) {
 				/*WindowUtils.errorAlert("The number of categories to downgrade is too many. " + "You cannot shift " + -1
 						* amount + " categories from " + startValue + ". Please correct this error.");*/
 				return startValue;
 			} else if (newIndex >= cats.size()) {
 				/*WindowUtils.errorAlert("The number of categories to upgrade is too many. " + "You cannot shift "
 						+ amount + " categories from " + startValue + ". Please correct this error.");*/
 				return startValue;
 			} else {
 				return cats.get(newIndex);
 			}
 		}
 	}
 }
