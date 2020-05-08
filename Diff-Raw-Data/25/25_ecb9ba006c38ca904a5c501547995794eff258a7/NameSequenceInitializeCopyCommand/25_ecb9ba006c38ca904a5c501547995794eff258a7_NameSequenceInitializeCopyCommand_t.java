 package com.netxforge.netxstudio.edit;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.edit.command.CopyCommand.Helper;
 import org.eclipse.emf.edit.command.InitializeCopyCommand;
 import org.eclipse.emf.edit.domain.EditingDomain;
 
import com.netxforge.netxstudio.metrics.MetricSource;
import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.services.RFSService;
 import com.netxforge.netxstudio.services.ServiceUser;
 import com.netxforge.netxstudio.services.ServicesPackage;
 
 /**
  * 
  * @author Christophe
 * 
  */
 public class NameSequenceInitializeCopyCommand extends InitializeCopyCommand {
 
 	public NameSequenceInitializeCopyCommand(EditingDomain domain,
 			EObject owner, Helper copyHelper) {
 		super(domain, owner, copyHelper);
 	}
 
 	/**
 	 * This method will iterate over the attributes of the owner object and set
 	 * them accordingly in the copy.
 	 * 
 	 * Customized to give a sequence number to the copied attribute.
 	 * 
 	 */
 	protected void copyAttributes() {
 
 		// Make sure we apply only to the target level.
 
 		for (EAttribute attribute : getAttributesToCopy()) {
 			if (attribute.isChangeable() && !attribute.isDerived()
 					&& (attribute.isMany() || owner.eIsSet(attribute))) {
 				Object value = owner.eGet(attribute);
 				if (!attribute.isMany()) {
 
 					if (isIdentifier(attribute) && value instanceof String) {
						copy.eSet(attribute,
								nextIdentitySequence((String) value));
 					} else {
 						copy.eSet(attribute, value);
 					}
 				} else {
 					@SuppressWarnings("unchecked")
 					List<Object> list = (List<Object>) copy.eGet(attribute);
 					if (FeatureMapUtil.isFeatureMap(attribute)) {
 						FeatureMap featureMap = (FeatureMap) (List<?>) list;
 						LOOP: for (FeatureMap.Entry entry : (FeatureMap) value) {
 							EStructuralFeature entryFeature = entry
 									.getEStructuralFeature();
 							if (entryFeature instanceof EAttribute) {
 								featureMap.add(entry);
 							} else {
 								EReference reference = (EReference) entryFeature;
 								EReference reverseReference = reference
 										.getEOpposite();
 								Object entryValue = entry.getValue();
 								boolean copiedTargetRequired = reverseReference != null
 										|| reference.isContainment();
 								EObject target = copyHelper.getCopyTarget(
 										(EObject) entryValue,
 										copiedTargetRequired);
 								if (target != null) {
 									if (reverseReference != null) {
 										for (FeatureMap.Entry copyEntry : featureMap) {
 											if (copyEntry
 													.getEStructuralFeature() == reference
 													&& copyEntry.getValue() == target) {
 												featureMap.move(
 														featureMap.size() - 1,
 														copyEntry);
 												continue LOOP;
 											}
 										}
 									}
 									featureMap.add(reference, target);
 								}
 							}
 						}
 					} else {
 						list.addAll((List<?>) value);
 					}
 				}
 			}
 		}
 	}
 
 	public boolean isIdentifier(EAttribute attribute) {
 
 		if (owner instanceof ServiceUser) {
 			if (attribute == ServicesPackage.Literals.SERVICE_USER__NAME) {
 				return true;
 			}
		} else if (owner instanceof RFSService) {
 			if (attribute == ServicesPackage.Literals.SERVICE__SERVICE_NAME) {
 				return true;
 			}
		} else if (owner instanceof MetricSource) {
			// http://work.netxforge.com/issues/242
			if (attribute == MetricsPackage.Literals.METRIC_SOURCE__NAME) {
				return true;
			}
 		}
 
 		return false;
 	}
 
 	public String nextIdentitySequence(String value) {
 		String identity = EditUtils.INSTANCE.nextIdentity(value);
		if (!identity.isEmpty()) {
 			return identity;
		} else {
 			return value + " 1";
 		}
 	}
 
 }
