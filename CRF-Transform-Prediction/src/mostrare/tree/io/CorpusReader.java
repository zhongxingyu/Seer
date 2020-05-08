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
package mostrare.tree.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import mostrare.coltAdaptation.list.TreeArrayList;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.impl.TreeAST;


public class CorpusReader
{
	private static CorpusReader	instance	= null;

	static
	{
		instance = new CorpusReader();
	}

	private CorpusReader()
	{
	}

	public static CorpusReader getInstance()
	{
		return instance;
	}

	/**
	 * Returns a list of trees relative to the tree-documents provided by the corpus.
	 * 
	 * @param corpusPath
	 *            path to the corpus
	 * @param annotationReader
	 * @return a list of trees
	 */
	
	public TreeArrayList readCorpus(String corpusPath,
			TreeReader treeReader, CRFWithConstraintNode crf)
	{
		TreeArrayList trees = new TreeArrayList();
		
		File[] trainingfolder = new File(corpusPath).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		});
		
		for(int folderindex=0; folderindex<trainingfolder.length; folderindex++ ) {

		    File corpusFile = trainingfolder[folderindex];
		// tests if corpusPath leads to a directory
		    if (corpusFile.exists() && corpusFile.isDirectory())
		    {
			// try to read each file of the directory into a tree
			  for (File treeFile : corpusFile.listFiles())
				if (treeFile.isFile())
				{
					ArrayList<TreeAST> tree = treeReader.readTree(treeFile, crf);
					if (tree.size()!=0) {		
						for(int index=0; index<tree.size(); index++)
						   trees.add(tree.get(index));
					}
					else
						System.err
								.println(treeFile.getAbsolutePath()
										+ " was not taken into account. See log file for further information.");
				}
		    }
		    else
		    {
			   System.err.println("Wrong filepath. Empty corpus produced.");
		    }
		}

		return trees;
	}

}
