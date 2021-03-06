 /* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
    modeler, Finit element mesher, Plugin architecture.
 
     Copyright (C) 2003,2004,2005
                   Jerome Robert <jeromerobert@users.sourceforge.net>
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.jcae.mesh.amibe.algos2d;
 
 import org.jcae.mesh.mesher.ds.MMesh1D;
 import org.jcae.mesh.mesher.ds.MNode1D;
 import org.jcae.mesh.amibe.ds.Mesh;
 import org.jcae.mesh.amibe.ds.Triangle;
 import org.jcae.mesh.amibe.ds.OTriangle;
 import org.jcae.mesh.amibe.ds.OTriangle2D;
 import org.jcae.mesh.amibe.ds.Vertex;
 import org.jcae.mesh.amibe.metrics.Metric3D;
 import org.jcae.mesh.amibe.InvalidFaceException;
 import org.jcae.mesh.amibe.InitialTriangulationException;
 import org.jcae.mesh.cad.CADShapeBuilder;
 import org.jcae.mesh.cad.CADFace;
 import org.jcae.mesh.cad.CADEdge;
 import org.jcae.mesh.cad.CADWire;
 import org.jcae.mesh.cad.CADWireExplorer;
 import org.jcae.mesh.cad.CADExplorer;
 import org.jcae.mesh.cad.CADGeomCurve2D;
 import org.jcae.mesh.cad.CADGeomCurve3D;
 import java.util.Iterator;
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 
 /**
  * Performs an initial surface triangulation.
  * The value of discretisation is provided by the constraint hypothesis.
  */
 
 public class BasicMesh
 {
 	private static Logger logger=Logger.getLogger(BasicMesh.class);
 	private Mesh mesh = null;
 	private MMesh1D mesh1d = null;
 	
 	/**
 	 * Creates a <code>BasicMesh</code> instance.
 	 *
 	 * @param m  the <code>BasicMesh</code> instance to refine.
 	 */
 	public BasicMesh(Mesh m, MMesh1D m1d)
 	{
 		mesh = m;
 		mesh1d = m1d;
 	}
 	
 	/**
 	 * Launch method to mesh a surface.
 	 */
 	public void compute()
 	{
 		Triangle t;
 		OTriangle2D ot;
 		Vertex v;
 		
 		Vertex [] bNodes = boundaryNodes();
 		if (bNodes.length < 3)
 		{
 			logger.warn("Boundary face contains less than 3 points, it is skipped...");
 			throw new InvalidFaceException();
 		}
 		logger.debug(" Unconstrained Delaunay triangulation");
 		double umin = Double.MAX_VALUE;
 		double umax = Double.MIN_VALUE;
 		double vmin = Double.MAX_VALUE;
 		double vmax = Double.MIN_VALUE;
 		for (int i = 0; i < bNodes.length; i++)
 		{
 			double [] uv = bNodes[i].getUV();
 			if (uv[0] > umax)
 				umax = uv[0];
 			if (uv[0] < umin)
 				umin = uv[0];
 			if (uv[1] > vmax)
 				vmax = uv[1];
 			if (uv[1] < vmin)
 				vmin = uv[1];
 		}
 		if (umax <= umin || vmax <= vmin)
 			throw new InvalidFaceException();
 		mesh.initQuadTree(umin, umax, vmin, vmax);
 		//  Initial point insertion sometimes fail on 2D,
 		//  this needs to be investigated.
 		mesh.pushCompGeom(2);
 		Vertex firstOnWire = null;
 		{
 			//  Initializes mesh
 			int i = 0;
 			Vertex v1 = bNodes[i];
 			firstOnWire = v1;
 			i++;
 			Vertex v2 = bNodes[i];
 			i++;
 			Vertex v3 = null;
 			//  Ensure that 1st triangle is not flat
 			for (; i < bNodes.length; i++)
 			{
 				v3 = bNodes[i];
 				if (firstOnWire == v3)
 					throw new InitialTriangulationException();
 				if (v3.onLeft(v1, v2) != 0L)
 					break;
 			}
 			assert i < bNodes.length;
 			mesh.bootstrap(v1, v2, v3);
 			int i3 = i;
 			for (i=2; i < bNodes.length; i++)
 			{
 				if (i == i3)
 					continue;
 				v = bNodes[i];
 				if (firstOnWire == v)
 					firstOnWire = null;
 				else
 				{
 					ot = v.getSurroundingOTriangle();
 					ot.split3(v, true); 
 					v.addToQuadTree();
 					if (firstOnWire == null)
 						firstOnWire = v;
 				}
 			}
 		}
 		if (!mesh.isValid(false))
 			throw new InitialTriangulationException();
 		mesh.popCompGeom(2);
 		
 		mesh.pushCompGeom(2);
 		logger.debug(" Rebuild boundary edges");
 		//  Boundary edges are first built, then they are collected.
 		//  This cannot be performed in a single loop because
 		//  triangles are modified within this loop.
 		firstOnWire = null;
 		for (int i = 0; i < bNodes.length; i++)
 		{
 			if (firstOnWire == null)
 				firstOnWire = bNodes[i];
 			else
 			{
 				mesh.forceBoundaryEdge(bNodes[i-1], bNodes[i], bNodes.length);
 				if (firstOnWire == bNodes[i])
 					firstOnWire = null;
 			}
 		}
 		assert firstOnWire == null;
 		ArrayList saveList = new ArrayList();
 		firstOnWire = null;
 		for (int i = 0; i < bNodes.length; i++)
 		{
 			if (firstOnWire == null)
 				firstOnWire = bNodes[i];
 			else
 			{
 				saveList.add(mesh.forceBoundaryEdge(bNodes[i-1], bNodes[i], bNodes.length));
 				if (firstOnWire == bNodes[i])
 					firstOnWire = null;
 			}
 		}
 		assert firstOnWire == null;
 		for (Iterator it = saveList.iterator(); it.hasNext(); )
 		{
 			OTriangle2D s = (OTriangle2D) it.next();
 			s.setAttributes(OTriangle.BOUNDARY);
 			s.symOTri();
 			s.setAttributes(OTriangle.BOUNDARY);
 		}
 		mesh.popCompGeom(2);
 		
 		logger.debug(" Mark outer elements");
 		t = (Triangle) Vertex.outer.getLink();
 		ot = new OTriangle2D(t, 0);
 		if (ot.origin() == Vertex.outer)
 			ot.nextOTri();
 		else if (ot.destination() == Vertex.outer)
 			ot.prevOTri();
 		assert ot.apex() == Vertex.outer : ot;
 		
 		Triangle.listLock();
 		Vertex first = ot.origin();
 		do
 		{
 			for (int i = 0; i < 3; i++)
 			{
 				ot.setAttributes(OTriangle.OUTER);
 				ot.nextOTri();
 			}
 			ot.getTri().listCollect();
 			ot.nextOTriApex();
 		}
 		while (ot.origin() != first);
 		
 		logger.debug(" Mark holes");
 		OTriangle2D sym = new OTriangle2D();
 		// Dummy value to enter the loop
 		Triangle oldHead = t;
 		Triangle newHead = null;
 		while (oldHead != newHead)
 		{
 			oldHead = newHead;
 			for (Iterator it = Triangle.getTriangleListIterator(); it.hasNext(); )
 			{
 				t = (Triangle) it.next();
 				if (t == oldHead)
 					break;
 				ot.bind(t);
 				boolean outer = ot.hasAttributes(OTriangle.OUTER);
 				for (int i = 0; i < 3; i++)
 				{
 					ot.nextOTri();
 					OTriangle.symOTri(ot, sym);
 					if (sym.getTri().isListed())
 						continue;
 					newHead = sym.getTri();
 					newHead.listCollect();
 					if (ot.hasAttributes(OTriangle.BOUNDARY))
 					{
 						if (!outer)
 							newHead.setOuter();
 						else if (sym.hasAttributes(OTriangle.OUTER))
 								throw new InitialTriangulationException();
 					}
 					else
 					{
 						if (outer)
 							newHead.setOuter();
 						else if (sym.hasAttributes(OTriangle.OUTER))
 								throw new InitialTriangulationException();
 					}
 				}
 			}
 		}
 		Triangle.listRelease();
 		assert (mesh.isValid());
 		
 		logger.debug(" Remove links to outer triangles");
 		for (Iterator it = mesh.getTriangles().iterator(); it.hasNext(); )
 		{
 			t = (Triangle) it.next();
 			if (t.isOuter())
 				continue;
 			for (int i = 0; i < 3; i++)
 			{
 				if (t.vertex[i].getLink() instanceof Triangle)
 					t.vertex[i].setLink(t);
 			}
 		}
 		
 		logger.debug(" Select 3D smaller diagonals");
 		mesh.pushCompGeom(3);
 		ot = new OTriangle2D();
 		boolean redo = true;
 		//  With riemannian metrics, there may be infinite loops,
 		//  make sure to exit this loop.
 		int niter = bNodes.length;
 		while (redo && niter > 0)
 		{
 			redo = false;
 			--niter;
 			for (Iterator it = saveList.iterator(); it.hasNext(); )
 			{
 				OTriangle2D s = (OTriangle2D) it.next();
 				if (s.apex() == Vertex.outer)
 					s.symOTri();
 				s.nextOTri();
 				if (s.hasAttributes(OTriangle.SWAPPED))
 					continue;
 				if (s.checkSmallerAndSwap() != 0)
 					redo = true;
  			}
  		}
 		mesh.popCompGeom(3);
 		
 		mesh.pushCompGeom(3);
 		new Insertion(mesh).compute();
 		mesh.popCompGeom(3);
 		
 		assert (mesh.isValid());
 	}
 	
 	/*
 	 *  Builds the patch boundary.
 	 *  Returns a list of Vertex.
 	 */
 	private Vertex [] boundaryNodes()
 	{
 		//  Rough approximation of the final size
 		int roughSize = 10*mesh1d.maximalNumberOfNodes();
 		ArrayList result = new ArrayList(roughSize);
 		CADFace face = (CADFace) mesh.getGeometry();
 		CADExplorer expW = CADShapeBuilder.factory.newExplorer();
 		CADWireExplorer wexp = CADShapeBuilder.factory.newWireExplorer();
 		
 		for (expW.init(face, CADExplorer.WIRE); expW.more(); expW.next())
 		{
 			MNode1D p1 = null;
 			Vertex p20 = null, p2 = null, lastPoint = null;;
 			double accumulatedLength = 0.0;
 			ArrayList nodesWire = new ArrayList(roughSize);
 			for (wexp.init((CADWire) expW.current(), face); wexp.more(); wexp.next())
 			{
 				CADEdge te = wexp.current();
 				double range[] = new double[2];
 				CADGeomCurve2D c2d = CADShapeBuilder.factory.newCurve2D(te, face);
 				CADGeomCurve3D c3d = CADShapeBuilder.factory.newCurve3D(te);
 
 				ArrayList nodelist = mesh1d.getNodelistFromMap(te);
 				Iterator itn = nodelist.iterator();
 				ArrayList saveList = new ArrayList();
 				while (itn.hasNext())
 				{
 					p1 = (MNode1D) itn.next();
 					saveList.add(p1);
 				}
 				if (!te.isOrientationForward())
 				{
 					//  Sort in reverse order
 					int size = saveList.size();
 					for (int i = 0; i < size/2; i++)
 					{
 						Object o = saveList.get(i);
 						saveList.set(i, saveList.get(size - i - 1));
 						saveList.set(size - i - 1, o);
 					}
 				}
 				itn = saveList.iterator();
 				//  Except for the very first edge, the first
 				//  vertex is constrained to be the last one
 				//  of the previous edge.
 				p1 = (MNode1D) itn.next();
 				if (null == p2)
 				{
 					p2 = new Vertex(p1, c2d, face);
 					nodesWire.add(p2);
 					p20 = p2;
 					lastPoint = p2;
 				}
 				ArrayList newNodes = new ArrayList(saveList.size());
 				while (itn.hasNext())
 				{
 					p1 = (MNode1D) itn.next();
 					p2 = new Vertex(p1, c2d, face);
 					newNodes.add(p2);
 				}
 				// An edge is skipped if all the following conditions
 				// are met:
 				//   1.  It is not degenerated
 				//   2.  It has not been discretized in 1D
 				//   3.  Edge length is smaller than epsilon
 				//   4.  Accumulated points form a curve with a deflection
 				//       which meets its criterion
 				boolean canSkip = false;
 				if (nodelist.size() == 2 && !te.isDegenerated())
 				{
 					//   3.  Edge length is smaller than epsilon
 					double edgelen = c3d.length();
 					canSkip = mesh.tooSmall(edgelen, accumulatedLength);;
 					if (canSkip)
 						accumulatedLength += edgelen;
 					// 4.  Check whether deflection is valid.
 					if (canSkip && Metric3D.hasDeflection())
 					{
 						double [] uv = lastPoint.getUV();
 						double [] start = mesh.getGeomSurface().value(uv[0], uv[1]);
 						uv = p2.getUV();
 						double [] end = mesh.getGeomSurface().value(uv[0], uv[1]);
 						double dist = Math.sqrt(
 						  (start[0] - end[0]) * (start[0] - end[0]) +
 						  (start[1] - end[1]) * (start[1] - end[1]) +
 						  (start[2] - end[2]) * (start[2] - end[2]));
                                 		double dmax = Metric3D.getDeflection();
 						if (Metric3D.hasRelativeDeflection())
 							dmax *= accumulatedLength;
 						if (accumulatedLength - dist > dmax)
 							canSkip = false;
 					}
 				}
 
 				if (!canSkip)
 				{
 					nodesWire.addAll(newNodes);
 					accumulatedLength = 0.0;
 					lastPoint = p2;
 				}
 			}
 			//  If a wire has less than 3 points, it is discarded
 			if (nodesWire.size() > 3)
 			{
 				//  Overwrite the last value to close the wire
 				nodesWire.set(nodesWire.size()-1, p20);
 				result.addAll(nodesWire);
 			}
 		}
 		
 		return (Vertex []) result.toArray(new Vertex[result.size()]);
 	}
 	
 }
