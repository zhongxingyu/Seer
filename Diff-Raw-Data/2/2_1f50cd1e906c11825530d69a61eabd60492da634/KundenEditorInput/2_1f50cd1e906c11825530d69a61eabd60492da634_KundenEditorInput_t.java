 /*******************************************************************************
  * Copyright (c) 2010 C1WPS GmbH. All rights reserved.
  *******************************************************************************/
 package de.c1wps.winterschool.ui.kundeneditor;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPersistableElement;
 
 import de.c1wps.winterschool.domain.kunde.material.Kunde;
 
 public class KundenEditorInput implements IEditorInput {
 	
 	private final Kunde kunde;
 
 	public KundenEditorInput(Kunde kunde) {
 		this.kunde = kunde;
 	}
 
 	public Kunde getKunde() {
 		return kunde;
 	}
 
 	public boolean exists() {
 		return false;
 	}
 
 	public ImageDescriptor getImageDescriptor() {
 		return null;
 	}
 
 	public String getName() {
 		return "KundenEditorInput";
 	}
 
 	public IPersistableElement getPersistable() {
 		return null;
 	}
 
 	public String getToolTipText() {
 		return "Input for the Kunden Editor";
 	}
 
	@SuppressWarnings("rawtypes")
 	public Object getAdapter(Class adapter) {
 		return null;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((kunde == null) ? 0 : kunde.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		KundenEditorInput other = (KundenEditorInput) obj;
 		if (kunde == null) {
 			if (other.kunde != null)
 				return false;
 		} else if (!kunde.equals(other.kunde))
 			return false;
 		return true;
 	}
 
 }
