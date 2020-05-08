 package de.fkoeberle.autocommit.message.java;
 
 
 public class CommonParentPackageFinder {
 	private String first;
 	private int lengthOfPrefix;
 	
 	public void checkPackage(String r) {
 		if (first == null) {
 			first = r;
 			lengthOfPrefix = r.length();
 		} else {
 			// default package handling:
 			if ((lengthOfPrefix == 0)) {
 				if (r.length() != 0) {
 					lengthOfPrefix = -1;
 				}
 				return;
 			}
 			for (int i = 0; i < lengthOfPrefix; i++ ) {
 				if ((i >= r.length())) {
 					lengthOfPrefix = first.lastIndexOf('.', i);
 					return;
 				} else if ((first.charAt(i) != r.charAt(i))) {
 					lengthOfPrefix = first.lastIndexOf('.', i - 1);
 					return;
 				}
 			}
 			if (lengthOfPrefix < r.length()) {
				lengthOfPrefix = r.lastIndexOf('.', lengthOfPrefix);
 			}
 		}
 	}
 	
 	public String getCommonPackage() {
 		if ((first == null) || (lengthOfPrefix == -1))
 			return null;
 		return first.substring(0, lengthOfPrefix);
 	}
 }
