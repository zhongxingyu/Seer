 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.ui.editor;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPersistableElement;
 
 import de.opalproject.vespucci.datamodel.Ensemble;
 
 public class EnsembleEditorInput implements IEditorInput {
 
 	private final Ensemble ensemble;
 
 	public EnsembleEditorInput(Ensemble ensemble) {
 		this.ensemble = ensemble;
 	}
 
 	public Ensemble getEnsemble() {
 		return ensemble;
 	}
 
 	@Override
 	public boolean exists() {
 		return true;
 	}
 
 	@Override
 	public ImageDescriptor getImageDescriptor() {
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		return String.valueOf(ensemble);
 	}
 
 	@Override
 	public IPersistableElement getPersistable() {
 		return null;
 	}
 
 	@Override
 	public String getToolTipText() {
		return "Displays an ensemble";
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class adapter) {
 		return null;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ensemble.hashCode();
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
 
 		EnsembleEditorInput other = (EnsembleEditorInput) obj;
 		if (!ensemble.equals(other.ensemble))
 			return false;
 
 		return true;
 	}
 
 }
