/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mostrare.crf.tree;

import cern.colt.list.IntArrayList;

/**
 * 
 * @author missi
 *
 */
public interface AnnotationsEnum
{
	
	public abstract String[] getAnnotationStringArray();

	/**
	 * Returns the string relative to the annotation associated with the provided <code>index</code>.
	 * 
	 * @param index
	 *            the index of an annotation
	 * @return the string version of the annotation
	 */
	public abstract String getAnnotationText(int index);

	/**
	 * Returns the index relative to the annotation associated with the provided
	 * <code>stringValue</code>.
	 * 
	 * @param annotation
	 *            string value of the annotation
	 * @return the index relative to the annotation.
	 */
	public abstract int getAnnotationIndex(String annotation);

	/**
	 * Returns the number of annotation labels.
	 * 
	 * @return the number of annotation labels.
	 */
	public abstract int getAnnotationsNumber();

	/**
	 * Returns the index for the "empty" annotation label.
	 * 
	 * @return the index for the "empty" annotation label.
	 */
	public abstract int getNoAnnotationIndex();
	
	public abstract IntArrayList getAnnotationArray();


}