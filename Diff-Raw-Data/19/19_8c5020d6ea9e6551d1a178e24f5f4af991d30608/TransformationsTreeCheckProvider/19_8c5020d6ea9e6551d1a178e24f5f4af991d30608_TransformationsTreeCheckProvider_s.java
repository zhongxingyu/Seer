 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.ui.prefs;
 
 import net.atos.optimus.m2m.engine.core.masks.PreferencesTransformationMask;
 import net.atos.optimus.m2m.engine.core.transformations.TransformationReference;
 
 import org.eclipse.jface.viewers.ICheckStateProvider;
 
 /**
  * Check State Provider, used to give initial values to the check boxes of the
  * {@link TransformationsPreferencesPage}
  * 
  * @author mvanbesien
  * 
  */
 public class TransformationsTreeCheckProvider implements ICheckStateProvider {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
 	 */
 	public boolean isGrayed(Object element) {
 		return !(element instanceof TransformationReference);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
 	 */
 	public boolean isChecked(Object element) {
 		if (element instanceof TransformationReference) {
 			TransformationReference reference = (TransformationReference) element;
			return PreferencesTransformationMask.getInstance().isTransformationEnabled(reference.getId());
 		}
 		return false;
 	}
 
 }
