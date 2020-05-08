 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 package org.eclipse.uml2.diagram.common.editpolicies;
 
 import java.util.Collections;
 import java.util.List;
 
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.UnmovableShapeEditPolicy;
 
 
 public class UnmovableUnselectableShapeEditPolicy extends UnmovableShapeEditPolicy {
 	
 	protected List<?> createSelectionHandles() {
 		return Collections.emptyList();
 	}
	
	@Override
	public Command getCommand(Request request) {
		if (understandsRequest(request)){
			//FIXME: temporarily workaround for #261192, should be removed when # 261192 is fixed
			return new Command(){};
		}
		return super.getCommand(request);
	}
 
 }
