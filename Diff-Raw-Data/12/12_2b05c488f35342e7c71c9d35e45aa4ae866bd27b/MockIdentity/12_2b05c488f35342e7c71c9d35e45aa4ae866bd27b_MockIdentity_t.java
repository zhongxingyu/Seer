 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.communication.xml.test;
 
 import java.util.UUID;
 
 import org.s23m.cell.Identity;
 
 public class MockIdentity implements Identity {
 
 	private UUID identifier;
 	private UUID uniqueRepresentationReference;
 	
 	public MockIdentity(UUID identifier, UUID uniqueRepresentationReference) {
 		this.identifier = identifier;
 		this.uniqueRepresentationReference = uniqueRepresentationReference;
 	}
 	
 	@Override
 	public UUID identifier() {
 		return identifier;
 	}
 
 	@Override
 	public UUID uniqueRepresentationReference() {
 		return uniqueRepresentationReference;
 	}
 
 	@Override
 	public String name() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String pluralName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String technicalName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean isEqualTo(Identity concept) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isEqualToRepresentation(Identity representation) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isPartOfKernel() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isPartOfUniversalCellConcept() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void makePartOfUniversalCellConcept() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String payload() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String setPayload(String payload) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	@Override
	public String codeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String pluralCodeName() {
		// TODO Auto-generated method stub
		return null;
	}

 }
