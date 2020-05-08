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
 package org.oobium.build.esp.parts;
 
 import static org.oobium.utils.CharStreamUtils.*;
 
 import org.oobium.build.esp.EspPart;
 
 
 public class ConstructorArg extends EspPart {
 
 	private EspPart varType;
 	private EspPart varName;
 	private EspPart defaultValue;
 	private boolean isVarArgs;
 	
 	public ConstructorArg(EspPart parent, int start, int end) {
 		super(parent, Type.CtorArgPart, start, end);
 		parse();
 	}
 
 	public String getDefaultValue() {
 		return (defaultValue != null) ? defaultValue.getText() : null;
 	}
 
 	public EspPart getDefaultValuePart() {
 		return defaultValue;
 	}
 	
 	public String getVarName() {
 		return (varName != null) ? varName.getText() : null;
 	}
 
 	public EspPart getVarNamePart() {
 		return varName;
 	}
 	
 	public String getVarType() {
 		return (varType != null) ? varType.getText() : null;
 	}
 	
 	public EspPart getVarTypePart() {
 		return varType;
 	}
 	
 	public boolean hasDefaultValue() {
 		return defaultValue != null;
 	}
 	
 	public boolean hasVarName() {
 		return varName != null;
 	}
 	
 	public boolean hasVarType() {
 		return varType != null;
 	}
 	
 	public boolean isVarArgs() {
 		return isVarArgs;
 	}
 
 	protected void parse() {
 		int vix = findAll(ca, start, end, '.','.','.');
 		int dix = find(ca, '=', start, end);
 
 		if(vix != -1) {
 			isVarArgs = true;
 		} else {
 			for(vix = start; vix < end && ca[vix] != '=' && !Character.isWhitespace(ca[vix]); vix++) {
 				if(ca[vix] == '<') {
 					vix = closer(ca, vix, end, true);
 				}
 			}
 		}
 		
 		int s1 = forward(ca, start, end);
 		if(s1 != -1 && s1 != vix) {
 			int s2 = reverse(ca, vix-1) + 1;
 			varType = new EspPart(this, Type.VarTypePart, s1, s2);
 		}
 		s1 = isVarArgs ? vix+3 : vix;
 		s1 = forward(ca, s1, end);
 		if(s1 != -1) {
 			if(dix != -1) {
 				if(s1 < dix) {
 					int s2 = reverse(ca, dix-1) + 1;
 					varName = new EspPart(this, Type.VarNamePart, s1, s2);
 					s1 = forward(ca, dix+1, end);
 					if(s1 != -1) {
 						s2 = reverse(ca, end-1) + 1;
 						defaultValue = new EspPart(this, Type.DefaultValuePart, s1, s2);
 					}
 				}
 			} else {
 				int s2 = reverse(ca, end-1) + 1;
 				varName = new EspPart(this, Type.VarNamePart, s1, s2);
 			}
 		}
 	}
 	
 }
