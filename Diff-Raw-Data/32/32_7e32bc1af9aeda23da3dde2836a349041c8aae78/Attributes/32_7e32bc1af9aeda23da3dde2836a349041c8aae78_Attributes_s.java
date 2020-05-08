 /**
  * 
  */
 package org.cotrix.web.manage.client.util;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UIQName;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class Attributes {
 	
 	protected static final Comparator<UIAttribute> comparator = new Comparator<UIAttribute>() {
 		
 		@Override
 		public int compare(UIAttribute o1, UIAttribute o2) {
 			if (isSystemAttribute(o1)) return 1;
 			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName().getLocalPart(), o2.getName().getLocalPart());
 		}
 	};
 	
 	public static boolean isSystemAttribute(UIAttribute attribute) {
 		if (attribute == null) return false;
 		return isSystemType(attribute.getType());
 	}
 	
 	protected static boolean isSystemType(UIQName type)
 	{
 		if (type == null) return false;
 		return Constants.SYSTEM_TYPE.equals(type);
 	}
 	
 	public static void sortByAttributeType(List<UIAttribute> attributes) {
 		Collections.sort(attributes, comparator);
 	}
 	
 	public static void mergeSystemAttributes(List<UIAttribute> destination, List<UIAttribute> source) {
 		for (UIAttribute sourceAttribute:source) {
 			if (Attributes.isSystemAttribute(sourceAttribute)) {
 				int index = destination.indexOf(sourceAttribute);
 				if (index >= 0) destination.set(index, sourceAttribute);
 				else destination.add(sourceAttribute);
 			}
 		}
 	}
 
 }
