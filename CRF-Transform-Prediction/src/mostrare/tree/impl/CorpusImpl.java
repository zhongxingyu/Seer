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
package mostrare.tree.impl;

import mostrare.coltAdaptation.list.TreeArrayList;
import mostrare.tree.Corpus;
import mostrare.tree.Tree;

/**
 * @author missi
 */
public class CorpusImpl implements Corpus
{

	private TreeArrayList trees;

	public CorpusImpl(TreeArrayList trees)
	{
		this.trees = trees;
		
		for (int index = 0; index < trees.size(); index += 1)
		{
			Tree tree = trees.getQuick(index);
			tree.setIndex(index);
		}
	}

	@Override
	public Tree getTree(int index)
	{
		return trees.getQuick(index);
	}

	@Override
	public int getTreesNumber()
	{
		return trees.size();
	}
}
