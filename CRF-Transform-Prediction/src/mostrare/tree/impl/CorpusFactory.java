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
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.Corpus;
import mostrare.tree.io.CorpusReader;
import mostrare.tree.io.TreeReader;

/**
 * @author missi
 */
public class CorpusFactory
{
	private static CorpusFactory	instance	= null;

	static
	{
		instance = new CorpusFactory();
	}

	private CorpusFactory()
	{

	}

	public static CorpusFactory getInstance()
	{
		return instance;
	}

	/**
	 * @param corpusPath
	 *            path of the corpus
	 * @param annotationReader
	 * @param treeReader
	 */
	public Corpus buildCorpus(String corpusPath,
			TreeReader treeReader, CRFWithConstraintNode crf)
	{
		// try to read the corpus
		TreeArrayList trees = CorpusReader.getInstance().readCorpus(corpusPath,
				treeReader, crf);
		return new CorpusImpl(trees);
	}
}
