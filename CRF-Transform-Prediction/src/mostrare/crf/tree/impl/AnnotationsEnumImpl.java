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
package mostrare.crf.tree.impl;

import java.util.HashMap;
import java.util.Map;

import cern.colt.list.IntArrayList;
import mostrare.crf.tree.AnnotationsEnum;

/**
 * @author missi
 */
public class AnnotationsEnumImpl implements AnnotationsEnum
{
	private String[]				annotations;

	// entry of the map: annotation (string value) | annotation (index)
	private Map<String, Integer>	annotationsInv;

	private int						emptyAnnotationIndex;
	
	private IntArrayList annotationsarray;

	public AnnotationsEnumImpl(String[] labels, int emptyAnnotationIndex)
	{
		// store annotations
		annotations = labels;
		annotationsInv = new HashMap<String, Integer>();
		annotationsarray = new IntArrayList();
		for (int labelIndex = 0; labelIndex < annotations.length; labelIndex += 1) {
			annotationsInv.put(annotations[labelIndex], labelIndex);
			annotationsarray.add(labelIndex);
		}

		this.emptyAnnotationIndex = emptyAnnotationIndex;
	}
	
	@Override
	public String[] getAnnotationStringArray()
	{
		return annotations;
	}
	
	@Override
	public IntArrayList getAnnotationArray()
	{
		return annotationsarray;
	}

	@Override
	public String getAnnotationText(int index)
	{
		return annotations[index];
	}

	@Override
	public int getAnnotationIndex(String annotation)
	{
		return annotationsInv.get(annotation);
	}

	@Override
	public int getAnnotationsNumber()
	{
		return annotations.length;
	}

	@Override
	public int getNoAnnotationIndex()
	{
		return emptyAnnotationIndex;
	}
}
