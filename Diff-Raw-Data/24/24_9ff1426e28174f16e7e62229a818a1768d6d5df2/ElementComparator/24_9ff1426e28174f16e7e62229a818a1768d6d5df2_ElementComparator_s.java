 /*=============================================================================#
  # Copyright (c) 2013-2014 Stephan Wahlbrink (WalWare.de) and others.
  # All rights reserved. This program and the accompanying materials
  # are made available under the terms of the Eclipse Public License v1.0
  # which accompanies this distribution, and is available at
  # http://www.eclipse.org/legal/epl-v10.html
  # 
  # Contributors:
  #     Stephan Wahlbrink - initial API and implementation
  #=============================================================================*/
 
 package de.walware.ecommons.ltk.core.util;
 
 import java.util.Comparator;
 
 import com.ibm.icu.text.Collator;
 
 import de.walware.ecommons.ltk.IModelElement;
 import de.walware.ecommons.ltk.ISourceElement;
 import de.walware.ecommons.ltk.ISourceUnit;
 import de.walware.ecommons.ltk.LTKUtil;
 
 
 public class ElementComparator implements Comparator<IModelElement> {
 	
 	
	private final Collator ID_COMPARATOR = Collator.getInstance();
 	
 	
 	public ElementComparator() {
 	}
 	
 	
 	@Override
 	public int compare(final IModelElement e1, final IModelElement e2) {
 		final ISourceUnit u1 = LTKUtil.getSourceUnit(e1);
 		final ISourceUnit u2 = LTKUtil.getSourceUnit(e2);
 		int result = 0;
 		if (u1 != null && u2 != null) {
 			if (u1 != u2) {
				result = ID_COMPARATOR.compare(u1.getId(), u2.getId());
 			}
 			if (result != 0) {
 				return result;
 			}
			if (e1 instanceof ISourceUnit) {
				if (e2 instanceof ISourceUnit) {
 					return compareSourceElementsInUnit((ISourceElement) e1, (ISourceElement) e2);
 				}
 				return -1000000;
 			}
			else if (e2 instanceof ISourceUnit) { // && !(e1 instanceof ISourceUnit)
 				return 1000000;
 			}
 			else {
				return ID_COMPARATOR.compare(e1.getId(), e2.getId());
 			}
 		}
 		if (u1 == null && u2 != null) {
 			return Integer.MAX_VALUE;
 		}
 		if (u2 == null && u1 != null) {
 			return Integer.MIN_VALUE;
 		}
 		return 0;
 	}
 	
 	protected int compareSourceElementsInUnit(final ISourceElement e1, final ISourceElement e2) {
 		return (e1.getSourceRange().getOffset() - 
 				e2.getSourceRange().getOffset());
 	}
 	
 }
