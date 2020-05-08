 /*
  * JGraLab - The Java Graph Laboratory
  * 
  * Copyright (C) 2006-2010 Institute for Software Technology
  *                         University of Koblenz-Landau, Germany
  *                         ist@uni-koblenz.de
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * Additional permission under GNU GPL version 3 section 7
  * 
  * If you modify this Program, or any covered work, by linking or combining
  * it with Eclipse (or a modified version of that program or an Eclipse
  * plugin), containing parts covered by the terms of the Eclipse Public
  * License (EPL), the licensors of this Program grant you additional
  * permission to convey the resulting work.  Corresponding Source for a
  * non-source form of such a combination shall include the source code for
  * the parts of JGraLab used as well as that of the covered work.
  */
 
 package de.uni_koblenz.jgralab.impl;
 
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.Incidence;
import de.uni_koblenz.jgralab.impl.mem.GraphElementImpl;
 import de.uni_koblenz.jgralab.schema.GraphElementClass;
 
 /**
  * This class provides an {@link Iterable} for the {@link Incidence}s at a given
  * {@link GraphElement}.
  * 
  * @author ist@uni-koblenz.de
  */
 public abstract class IncidenceIterable<I extends Incidence> implements
 		Iterable<I> {
 
 	/**
 	 * This class provides an {@link Iterator} for the {@link Incidence}s at a
 	 * given {@link GraphElement}.
 	 * 
 	 * @author ist@uni-koblenz.de
 	 * 
 	 */
 	abstract class IncidenceIterator implements Iterator<I> {
 
 		/**
 		 * The current instance of I.
 		 */
 		protected I current = null;
 
 		/**
 		 * {@link GraphElement} which {@link Incidence}s are iterated.
 		 */
 		protected GraphElement<?, ?, ?> graphElement = null;
 
 		/**
 		 * The {@link Class} of the desired {@link Incidence}s.
 		 */
 		protected Class<? extends Incidence> ic;
 
 		/**
 		 * {@link Direction} of the desired {@link Incidence}s.
 		 */
 		protected Direction dir;
 
 		protected Graph traversalContext;
 
 		/**
 		 * The version of the incidence list of the {@link GraphElement} at the
 		 * beginning of the iteration. This information is used to check if the
 		 * incidence list has changed. The failfast-{@link Iterator} will throw
 		 * an {@link ConcurrentModificationException} if the {@link #hasNext()}
 		 * or {@link #next()} is called.
 		 */
 		protected long incidenceListVersion;
 
 		/**
 		 * Creates an Iterator over the {@link Incidence}s of
 		 * <code>graphElement</code>.
 		 * 
 		 * @param traversalContext
 		 *            {@link Graph}
 		 * @param graphElement
 		 *            {@link GraphElement} which {@link Incidence}s should be
 		 *            iterated.
 		 * @param ic
 		 *            {@link Class} only instances of this class are returned.
 		 * @param dir
 		 *            {@link Direction} of the desired {@link Incidence}s.
 		 */
 		@SuppressWarnings("unchecked")
 		public <OwnTypeClass extends GraphElementClass<OwnTypeClass, OwnType>, OwnType extends GraphElement<OwnTypeClass, OwnType, DualType>, DualType extends GraphElement<?, DualType, OwnType>> IncidenceIterator(
 				Graph traversalContext,
 				GraphElement<OwnTypeClass, OwnType, DualType> graphElement,
 				Class<? extends Incidence> ic, Direction dir) {
 			this.graphElement = graphElement;
 			this.ic = ic;
 			this.dir = dir;
 			this.traversalContext = traversalContext;
 			incidenceListVersion = graphElement.getIncidenceListVersion();
 			current = (I) ((ic == null) ? graphElement.getFirstIncidence(dir)
 					: graphElement.getFirstIncidence(ic, dir));
 		}
 
 		@Override
 		public boolean hasNext() {
 			checkConcurrentModification();
 			return current != null;
 		}
 
 		/**
 		 * Checks if the sequence of {@link Incidence}s was modified. In this
 		 * case a {@link ConcurrentModificationException} is thrown
 		 * 
 		 * @throws ConcurrentModificationException
 		 */
 		protected void checkConcurrentModification() {
			if (((GraphElementImpl<?, ?, ?>) graphElement)
					.isIncidenceListModified(incidenceListVersion)) {
 				throw new ConcurrentModificationException(
 						"The incidence list of this graphelement has been modified - the iterator is not longer valid");
 			}
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException(
 					"Cannot remove Incidences using Iterator");
 		}
 
 	}
 
 	/**
 	 * The current {@link Iterator}.
 	 */
 	protected IncidenceIterator iter = null;
 
 	@Override
 	public Iterator<I> iterator() {
 		return iter;
 	}
 }
