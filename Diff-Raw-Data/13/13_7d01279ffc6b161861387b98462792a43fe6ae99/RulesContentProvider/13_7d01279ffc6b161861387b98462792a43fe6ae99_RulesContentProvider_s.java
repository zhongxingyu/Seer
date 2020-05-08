 /*
  *     Eclipse AST Validation, lite framework to validate java code
  *     
  *     Copyright (C) 2013 Atos Worldline or third-party contributors as
  *     indicated by the @author tags or express copyright attribution
  *     statements applied by the authors.
  *     
  *     This library is free software; you can redistribute it and/or
  *     modify it under the terms of the GNU Lesser General Public
  *     License as published by the Free Software Foundation; either
  *     version 2.1 of the License.
  *     
  *     This library is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *     Lesser General Public License for more details.
  *     
  *     You should have received a copy of the GNU Lesser General Public
  *     License along with this library; if not, write to the Free Software
  *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.jdt.ast.validation.engine.internal.extpt.ui;
 
 import net.atos.jdt.ast.validation.engine.ASTRulesRepository;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 /**
  * Rules Content Provider
  * 
  * @author mvanbesien
  * 
  */
 public class RulesContentProvider implements ITreeContentProvider {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
 	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.
 	 * Object)
 	 */
 	@Override
 	public Object[] getElements(final Object inputElement) {
 		return this.getChildren(inputElement);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
 	 * Object)
 	 */
 	@Override
 	public Object[] getChildren(final Object parentElement) {
 		if (parentElement instanceof ASTRulesRepository) {
 			return ((ASTRulesRepository) parentElement).getRules().toArray();
 		}
		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object
 	 * )
 	 */
 	@Override
 	public Object getParent(final Object element) {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
 	 * Object)
 	 */
 	@Override
 	public boolean hasChildren(final Object element) {
 		return element instanceof ASTRulesRepository;
 	}
 
 }
