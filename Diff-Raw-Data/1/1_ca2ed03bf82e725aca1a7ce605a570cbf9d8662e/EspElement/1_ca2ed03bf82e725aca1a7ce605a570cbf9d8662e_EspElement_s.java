 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.esp;
 
 import static org.oobium.utils.CharStreamUtils.findEOL;
 import static org.oobium.utils.CharStreamUtils.isBOL;
 import static org.oobium.utils.CharStreamUtils.reverse;
 
 public abstract class EspElement extends EspPart {
 
 	protected int level;
 	
 	public EspElement(EspPart parent, int start) {
 		super(parent, start);
 		
 		if(isBOL(ca, start)) {
 			while(this.start < ca.length) {
 				if(Character.isWhitespace(ca[this.start])) {
 					if(ca[this.start] == '\t') level++;
 					this.start++;
 				} else {
 					break;
 				}
 			}
 		} else {
 			while(this.start < ca.length) {
 				if(Character.isWhitespace(ca[this.start])) {
 					this.start++;
 				} else {
 					break;
 				}
 			}
 			if(parent instanceof EspElement) {
 				level = ((EspElement) parent).getLevel();
 			}
 		}
 	}
 
 	@Override
 	public EspElement getElement() {
 		return this;
 	}
 	
 	public String getElementText() {
 		int end = findEOL(ca, start);
 		end = reverse(ca, end-1) + 1;
 		return new String(ca, start, end-start);
 	}
 	
 	public int getLevel() {
 		return level;
 	}
 
 }
