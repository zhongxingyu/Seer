 /*
 	Copyright: Marcus Cobden (2011)
 	This file is part of AllocationCurve.
 
 	AllocationCurve is free software: you can redistribute it and/or modify
 	it under the terms of version 3 of the GNU Lesser General Public License
 	as published by the Free Software Foundation.
 
 	AllocationCurve is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 	GNU Lesser General Public License for more details.
 
 	You should have received a copy of the GNU Lesser General Public License
 	along with AllocationCurve. If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.marcuscobden.allocationcurve.renderer;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import uk.co.marcuscobden.allocationcurve.AllocationRecord;
 import uk.co.marcuscobden.allocationcurve.allocation.InetNetworkAllocationBlock;
 
 public class SVGAllocationRenderer extends HilbertAllocationRenderer
 {
 
 	protected Map<AllocationRecord, Color> colorMap = new HashMap<AllocationRecord, Color>();
 
 	public SVGAllocationRenderer(final Dimension size)
 	{
 		super(size);
 	}
 
 	protected Color getAllocationColor(final AllocationRecord alloc)
 	{
 		return colorMap.get(alloc);
 	}
 
 	protected void prepareAllocationColors(
 			final Collection<AllocationRecord> allocations)
 	{
 		float hueStep = 1f / (allocations.size() + 1);
 		float hue = 0;
 
 		for (AllocationRecord a : allocations)
 		{
 			colorMap.put(a, Color.getHSBColor(hue, 1f, 1f));
 
 			hue += hueStep;
 		}
 	}
 
 	protected Collection<AllocationRecord> findLeaves(
 			final AllocationRecord root, final int depthLimit)
 	{
 		int currentDepth;
 		LinkedList<AllocationRecord> stack = new LinkedList<AllocationRecord>();
 		ArrayList<AllocationRecord> leaves = new ArrayList<AllocationRecord>();
 		Map<AllocationRecord, Integer> depthMap = new HashMap<AllocationRecord, Integer>();
 
 		stack.add(root);
 		depthMap.put(root, 0);
 
 		AllocationRecord current;
 		while (!stack.isEmpty())
 		{
 			current = stack.pop();
 			currentDepth = depthMap.get(current);
 
 			Collection<AllocationRecord> children = current.getAllocations();
 			if (depthLimit == -1 || currentDepth < depthLimit)
 			{
 				if (children != null)
 					for (AllocationRecord c : children)
 					{
 						stack.addLast(c);
 						depthMap.put(c, currentDepth + 1);
 					}
 			}
 
 			if (children == null || children.size() == 0
 					|| currentDepth == depthLimit)
 				leaves.add(current);
 		}
 
 		return leaves;
 	}
 
 	public void render(final OutputStream output, final AllocationRecord root,
 			final int depthLimit)
 	{
 		Collection<AllocationRecord> leaves = findLeaves(root, depthLimit);
 
 		int[] range = getBitRange(root, leaves);
 		int startBit = range[0];
 		int finishBit = range[1];
 
 		prepareAllocationColors(leaves);
 
 		PrintWriter out = new PrintWriter(output);
 
 		renderDocumentPreamble(out);
 		renderSVGOpen(out);
 		renderHilbertCurve(out, (int) Math.ceil((finishBit - startBit) / 2d));
 		renderAllocations(out, leaves, startBit, finishBit);
 
 		renderSVGClose(out);
 
 		out.close();
 	}
 
 	protected void renderAllocations(final PrintWriter out,
 			final Collection<AllocationRecord> leaves, final int startBit,
 			final int finishBit)
 	{
 		final int spacing = 5;
 		int xOffset = spacing;
 		int yOffset = size.height + spacing;
 		int blockSize = 10;
 
 		for (AllocationRecord r : leaves)
 		{
 			out.println("<!-- " + r.getLabel() + " -->");
 
 			Color color = getAllocationColor(r);
 
 			out.printf(
 					"<rect x='%d' y='%d' width='%d' height='%d' fill='rgb(%d,%d,%d)' />\n",
 					xOffset, yOffset, blockSize, blockSize, color.getRed(),
 					color.getGreen(), color.getBlue());
 			out.printf(
 					"<text x='%d' y='%d' font-family='Verdana' font-size='12'>%s</text>\n",
 					xOffset + blockSize + spacing, yOffset + 10, r.getLabel());
 
 			for (InetNetworkAllocationBlock<InetAddress> block : r.getBlocks())
 			{
 				Rectangle2D.Double bounds = getBlockBounds(block, startBit,
 						finishBit);
 				out.printf(
 						"<rect x='%f' y='%f' width='%f' height='%f' fill='rgb(%d, %d, %d)' fill-opacity='0.75'/>\n",
 						bounds.x, bounds.y, bounds.width, bounds.height,
 						color.getRed(), color.getGreen(), color.getBlue());
 			}
 
 			yOffset += blockSize + spacing;
 		}
 	}
	
 	protected void renderHilbertCurve(final PrintWriter out,
 			final int iterations)
 	{
 		Point2D.Double[] curve = caluclateCurve(size, iterations);
 		out.print("<path fill='none' stroke='black' stroke-width='1' d='M");
		for (int i = 0; i < curve.length; i++)
 		{
			out.printf("%f %f", curve[i].x, curve[i].y);
			// Firefox cares whether we have a trailing L on the path.
			if (i < curve.length -1)
				out.print("L");
 		}
 		out.println("' />");
 	}
 
 	protected void renderSVGOpen(final PrintWriter output)
 	{
 		output.println("<svg xmlns='http://www.w3.org/2000/svg' version='1.1'>");
 	}
 
 	protected void renderSVGClose(final PrintWriter out)
 	{
 		out.println("</svg>");
 	}
 
 	protected void renderDocumentPreamble(final PrintWriter out)
 	{
 		out.println("<?xml version=\"1.0\"?>");
 		out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
 	}
 
 }
