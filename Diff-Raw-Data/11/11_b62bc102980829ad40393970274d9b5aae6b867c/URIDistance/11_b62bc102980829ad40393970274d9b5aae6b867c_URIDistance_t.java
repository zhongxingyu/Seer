 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.match.eobject;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 
 import java.util.List;
 
 import org.eclipse.emf.compare.CompareFactory;
 import org.eclipse.emf.compare.Comparison;
 
 /**
  * This class is able to measure similarity between "URI like" strings, basically strings separated by "/".
  * This is mainly intended to be used with EMF's fragments.
  * 
  * @author <a href="mailto:cedric.brun@obeo.fr">Cedric Brun</a>
  */
 public class URIDistance {
 	/**
 	 * comparison we don't care bout, only used because we have to pass one to the LCS algorithm, it is going
 	 * to use the EqualityHelper instance from it.
 	 */
 	private Comparison comparison;
 
 	/**
 	 * Create the distance.
 	 */
 	public URIDistance() {
 		comparison = CompareFactory.eINSTANCE.createComparison();
 	}
 
 	/**
 	 * Create the distance.
 	 * 
 	 * @param comparison
 	 *            the current comparison.
 	 */
 	public URIDistance(Comparison comparison) {
 		this.comparison = comparison;
 	}
 
 	/**
 	 * Return a metric result URI similarities. It compares 2 strings splitting those by "/" and return an int
 	 * representing the level of similarity. 0 - they are exactly the same to 10 - they are completely
 	 * different. "adding a fragment", "removing a fragment".
 	 * 
 	 * @param str1
 	 *            First of the two {@link String}s to compare.
 	 * @param str2
 	 *            Second of the two {@link String}s to compare.
 	 * @return The number of changes to transform one uri to another one.
 	 */
 	public int proximity(String str1, String str2) {
 		Splitter splitter = Splitter.on('/').trimResults().omitEmptyStrings();
 		List<String> fragments1 = Lists.newArrayList(splitter.split(str1));
 		List<String> fragments2 = Lists.newArrayList(splitter.split(str2));
 		if (fragments1.size() == 0 && fragments2.size() == 0) {
 			return 0;
 		}
		int frag2Size = fragments2.size();
 		int commonPart = 0;
 		for (int i = 0; i < fragments1.size(); i++) {
 			String f1 = fragments1.get(i);
			if (i < frag2Size && f1.equals(fragments2.get(i))) {
 				commonPart++;
 			} else {
 				break;
 			}
 		}
 
 		int totalFrag = Math.max(fragments2.size(), fragments1.size());
 		double similarity = commonPart * 10d / totalFrag;
 		return 10 - (int)similarity;
 	}
 
 }
