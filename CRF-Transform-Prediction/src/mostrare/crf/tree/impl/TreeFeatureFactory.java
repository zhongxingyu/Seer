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

import mostrare.crf.tree.EdgeFeature;
import mostrare.crf.tree.FeatureFactory;
import mostrare.crf.tree.NodeFeature;
import mostrare.crf.tree.NodeFeatureInverse;
import mostrare.crf.tree.PureNodeFeature;
import mostrare.crf.tree.TriangleFeature;
import mostrare.crf.tree.TriangleFeatureWithObservation;

/**
 * @author missi
 */
public class TreeFeatureFactory implements FeatureFactory
{
	private static FeatureFactory	instance	= null;

	static
	{
		instance = new TreeFeatureFactory();
	}

	private TreeFeatureFactory()
	{
	}

	public static FeatureFactory getInstance()
	{
		return instance;
	}
	
	@Override
	public PureNodeFeature createPureNodeFeature (String nodetype, int nodeAnnotationTest, 
			 int nbFeature)
	{
		return new PureNodeFeature(nodetype, nodeAnnotationTest, nbFeature);
	}

	@Override
	public NodeFeature createNodeFeature(String nodetype, int nodeAnnotationTest, int characterIndex, 
			 int nbFeature)
	{
		return new NodeFeature(nodetype, nodeAnnotationTest, characterIndex,
				 nbFeature);
	}
	
	@Override
	public NodeFeatureInverse createNodeInverseFeature(String nodetype, int nodeAnnotationTest, int characterIndex, 
			 int nbFeature)
	{
		return new NodeFeatureInverse (nodetype, nodeAnnotationTest, characterIndex,
				 nbFeature);
	}

	@Override
	public EdgeFeature createEdgeFeature (String typeofparent, String typeofchild, int parentAnnotationTest, int childAnnotationTest,
			 int numFeature)
	{
		return new EdgeFeature(typeofparent, typeofchild, parentAnnotationTest, childAnnotationTest,
				  numFeature);
	}

	@Override
	public TriangleFeature createTriangleFeature (String typeofparent, String typeofleftchild, String typeofrightchild, 
			int parentAnnotationTest, int leftChildAnnotationTest,
			int rightChildAnnotationTest, 
		    int numFeature) 
	{
		return new TriangleFeature (typeofparent, typeofleftchild, typeofrightchild, parentAnnotationTest, 
				leftChildAnnotationTest, rightChildAnnotationTest, numFeature);
	}
	
	@Override
	public TriangleFeatureWithObservation createTriangleFeatureWithObservation (String typeofparent, String typeofleftchild, String typeofrightchild, 
						int parentAnnotationTest, int leftChildAnnotationTest,
						int rightChildAnnotationTest, int numFeature)
	{
		return new TriangleFeatureWithObservation (typeofparent, typeofleftchild, typeofrightchild, parentAnnotationTest, 
				leftChildAnnotationTest, rightChildAnnotationTest, numFeature);
	}
	
}
