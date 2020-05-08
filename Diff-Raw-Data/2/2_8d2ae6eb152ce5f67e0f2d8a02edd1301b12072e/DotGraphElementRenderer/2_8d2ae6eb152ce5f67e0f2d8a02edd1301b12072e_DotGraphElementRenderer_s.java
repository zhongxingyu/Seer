 /**
  * Copyright (c) 2006-2011 Cloudsmith Inc. and other contributors, as listed below.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Cloudsmith
  * 
  */
 package org.cloudsmith.graph.dot;
 
 import java.io.PrintStream;
 import java.util.Collection;
 
 import org.cloudsmith.graph.ElementType;
 import org.cloudsmith.graph.ICancel;
 import org.cloudsmith.graph.IClusterGraph;
 import org.cloudsmith.graph.IEdge;
 import org.cloudsmith.graph.IGraphElement;
 import org.cloudsmith.graph.ILabeledGraphElement;
 import org.cloudsmith.graph.graphcss.GraphCSS;
 import org.cloudsmith.graph.graphcss.StyleSet;
 import org.cloudsmith.graph.style.Alignment;
 import org.cloudsmith.graph.style.Arrow;
 import org.cloudsmith.graph.style.EdgeDirection;
 import org.cloudsmith.graph.style.EdgeRouting;
 import org.cloudsmith.graph.style.IStyle;
 import org.cloudsmith.graph.style.IStyleVisitor;
 import org.cloudsmith.graph.style.LineBrush;
 import org.cloudsmith.graph.style.NodeShape;
 import org.cloudsmith.graph.style.RankDirection;
 import org.cloudsmith.graph.style.ShapeBrush;
 import org.cloudsmith.graph.style.StyleType;
 import org.cloudsmith.graph.style.StyleVisitor;
 import org.cloudsmith.graph.style.VerticalAlignment;
 import org.cloudsmith.graph.style.labels.ILabelTemplate;
 import org.cloudsmith.graph.utils.Counter;
 
 import com.google.common.collect.Iterators;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Renders IGraphElements in Graphviz dot notation.
  * 
  */
 @Singleton
 public class DotGraphElementRenderer {
 
 	private final DotLabelRenderer labelRenderer;
 
 	private final String emptyString;
 
 	@Inject
 	public DotGraphElementRenderer(DotLabelRenderer labelRenderer, @DotRenderer.EmptyString String emptyString) {
 		this.labelRenderer = labelRenderer;
 		this.emptyString = emptyString;
 	}
 
 	private String emptyString(String x) {
 		if(x == null || x.length() == 0)
 			x = emptyString;
 		return x;
 	}
 
 	/**
 	 * Do the actual printing.
 	 * 
 	 * @param cancel
	 *            TODO
 	 * @param out
 	 *            - where to print
 	 * @param element
 	 *            - the element being processed
 	 * @param statementList
 	 *            - output a statements list, or styles in an element
 	 * @param s
 	 *            - collected styles for the element
 	 * @param gcss
 	 *            - css for nested label elements
 	 */
 	private int printStyles(final ICancel cancel, final PrintStream out, final IGraphElement element,
 			final boolean statementList, final Collection<IStyle<?>> s, final GraphCSS gcss) {
 		// if no styles, output nothing
 		if(s == null || s.size() < 1)
 			return 0;
 		final char sepChar = statementList
 				? ';'
 				: ',';
 
 		final ElementType elementType = element.getElementType();
 
 		if(!statementList)
 			out.print("[\n");
 
 		final Counter o = new Counter(sepChar, '\n');
 
 		for(IStyle<?> style : s) {
 			// If the style is not applicable to the current element - skip it
 			//
 			if(!style.supports(elementType))
 				continue;
 
 			final IStyleVisitor visitor = new StyleVisitor() {
 				@Override
 				public void align(Alignment x) {
 					out.printf("%slabeljust=\"%s\"", o.separator(), x.toString().charAt(0));
 				}
 
 				@Override
 				public void arrowHead(Arrow x) {
 					out.printf("%sarrowhead=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void arrows(Arrow x) {
 					out.printf("%sarrowhead=\"%s\"", o.separator(), x);
 					out.printf(", arrowtail=\"%s\"", x);
 				}
 
 				@Override
 				public void arrowScale(double x) {
 					out.printf("%sarrowsize=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void arrowTail(Arrow x) {
 					out.printf("%sarrowtail=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void backgroundColor(String v) {
 					out.printf("%sbgcolor=\"%s\"", o.separator(), v);
 				}
 
 				@Override
 				public void color(String v) {
 					out.printf("%sfontcolor=\"%s\"", o.separator(), v);
 				}
 
 				@Override
 				public void compound(boolean x) {
 					out.printf("%scompound=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void concentrate(boolean x) {
 					out.printf("%sconcentrate=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void decorate(boolean x) {
 					out.printf("%sdecorate=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void direction(EdgeDirection x) {
 					out.printf("%sdir=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void fillColor(String v) {
 					out.printf("%sfillcolor=\"%s\"", o.separator(), v);
 				}
 
 				@Override
 				public void fontFamily(String x) {
 					out.printf("%sfontname=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void fontSize(int x) {
 					out.printf("%sfontsize=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void fromCluster(IClusterGraph value) {
 					if(!(element instanceof IEdge))
 						throw new IllegalArgumentException("fromCluster can only be applied to an edge");
 					IEdge edge = (IEdge) element;
 					if(!Iterators.contains(edge.getFrom().getContext(), value))
 						throw new IllegalArgumentException("edge fromCluster must have a from-vertex in given cluster");
 					out.printf("%sltail=\"cluster_%s\"", o.separator(), value.getId());
 				}
 
 				@Override
 				public void headPort(String x) {
 					{
 						String tmp = x;
 						if(tmp == null || tmp.length() < 1) {
 							o.decrement(); // nothing printed
 						}
 						out.printf("%sheadport=\"%s\"", o.separator(), tmp);
 					}
 				}
 
 				@Override
 				public void href(String x) {
 					if(x != null && x.length() > 0)
 						out.printf("%sURL=\"%s\"", o.separator(), x);
 					else
 						o.decrement(); // nothing printed
 				}
 
 				@Override
 				public void id(String value) {
 					out.printf("%sid=\"%s\"", o.separator(), value);
 				}
 
 				@Override
 				public void labelFormat(ILabelTemplate x) {
 					// labels are tricky - if there is no label format nothing is printed,
 					// if the label format EL evaluated rendered to false, nothing is printed
 					//
 					{
 						ILabelTemplate tmp = x;
 						if(tmp != null) {
 							// print the label, and if nothing printed, adjust the comma count
 							if(!labelRenderer.print(
 								out, (ILabeledGraphElement) element, tmp, o.isSeparatorNeeded(), sepChar, gcss, cancel))
 								o.decrement(); // nothing printed
 						}
 						else
 							o.decrement(); // no format, nothing was printed
 					}
 				}
 
 				@Override
 				public void lineBrush(LineBrush brush) {
 					out.printf("%sstyle=\"", o.separator());
 					{
 						// LineBrush brush = ((Styles.EdgeBrush) style).getValue();
 						out.printf("%s, ", brush.getLineType());
 						out.printf("setlinewidth(%s)", brush.getLineWidth());
 						out.print("\"");
 					}
 				}
 
 				@Override
 				public void lineColor(String x) { // i.e. border color
 					out.printf("%scolor=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void mclimit(double value) {
 					out.printf("%smclimit=\"%s\"", o.separator(), value);
 				}
 
 				@Override
 				public void rankDirection(RankDirection x) {
 					// only applies to graph, and only if it is the root
 					//
 					if(element.getParentElement() == null)
 						out.printf("%srankdir=\"%s\"", o.separator(), x);
 					else
 						o.decrement(); // nothing printed
 				}
 
 				@Override
 				public void rankSeparation(double x) {
 					// only applies to graph, and only if it is the root
 					//
 					if(element.getParentElement() == null)
 						out.printf("%sranksep=\"%s\"", o.separator(), x);
 					else
 						o.decrement(); // nothing printed
 				}
 
 				@Override
 				public void remincross(boolean value) {
 					out.printf("%sremincross=\"%s\"", o.separator(), value);
 				}
 
 				@Override
 				public void routing(EdgeRouting x) {
 					out.printf("%ssplines=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void shape(NodeShape x) {
 					if(elementType == ElementType.graph) {
 						System.err.println("Weird - shape call for Graph");
 					}
 					out.printf("%sshape=\"%s\"", o.separator(), x);
 				}
 
 				@Override
 				public void shapeBrush(ShapeBrush brush) {
 					// only graphs of cluster type can have style set
 					if(elementType == ElementType.cluster) {
 						o.decrement();
 					}
 					else {
 						out.printf("%sstyle=\"", o.separator());
 						// ShapeBrush brush = ((Styles.NodeBrush) style).getValue();
 						out.printf("%s, ", brush.getLineType());
 						out.printf("setlinewidth(%s)", brush.getLineWidth());
 						if(brush.isFilled())
 							out.print(", filled");
 						if(brush.isRounded())
 							out.print(", rounded");
 						// close the style=" "
 						out.print("\"");
 					}
 				}
 
 				@Override
 				public void tailPort(String x) {
 					{
 						String tmp = x;
 						if(tmp == null || tmp.length() < 1) {
 							o.decrement(); // nothing printed
 						}
 						out.printf("%stailport=\"%s\"", o.separator(), tmp);
 					}
 				}
 
 				@Override
 				public void toCluster(IClusterGraph value) {
 					if(!(element instanceof IEdge))
 						throw new IllegalArgumentException("toCluster can only be applied to an edge");
 					IEdge edge = (IEdge) element;
 					if(!Iterators.contains(edge.getTo().getContext(), value))
 						throw new IllegalArgumentException("edge toCluster must have a to-vertex in given cluster");
 
 					out.printf("%slhead=\"cluster_%s\"", o.separator(), value.getId());
 
 				}
 
 				@Override
 				public void tooltip(String x) {
 					out.printf("%stooltip=\"%s\"", o.separator(), emptyString(x));
 				}
 
 				@Override
 				public void tooltipForHead(String x) {
 					out.printf("%sheadtooltip=\"%s\"", o.separator(), emptyString(x));
 				}
 
 				@Override
 				public void tooltipForLabel(String x) {
 					out.printf("%slabeltooltip=\"%s\"", o.separator(), emptyString(x));
 				}
 
 				@Override
 				public void tooltipForTail(String x) {
 					out.printf("%stailtooltip=\"%s\"", o.separator(), emptyString(x));
 				}
 
 				@Override
 				public void unsupported(StyleType style) {
 					throw new IllegalArgumentException("Style:" + style + ", is not applicable to " +
 							"element of class: " + element.getClass());
 				}
 
 				@Override
 				public void verticalAlign(VerticalAlignment x) {
 					// The VerticalAlignment uses "c" for center when applied to lables, it is
 					// "middle" otherwise.
 					char c = x.toString().charAt(0);
 					if(c == 'm')
 						c = 'c';
 
 					out.printf("%slabelloc=\"%s\"", o.separator(), c);
 				}
 
 				@Override
 				public void weight(double value) {
 					out.printf("%sweight=\"%s\"", o.separator(), value);
 				}
 
 			};
 			style.visit(element, visitor);
 			o.increment();
 		}
 		// close
 		if(!statementList)
 			out.print("]");
 		else
 			out.print(";");
 		return o.value();
 	}
 
 	private int printStyles(ICancel cancel, PrintStream out, IGraphElement element, Collection<IStyle<?>> s,
 			GraphCSS gcss) {
 		return printStyles(cancel, out, element, false, s, gcss);
 	}
 
 	public int printStyles(ICancel cancel, PrintStream out, IGraphElement element, GraphCSS gcss) {
 		return printStyles(cancel, out, element, gcss.collectStyles(element, cancel).getStyles(), gcss);
 	}
 
 	public int printStyles(ICancel cancel, PrintStream out, IGraphElement element, StyleSet styleMap, GraphCSS gcss) {
 		return printStyles(cancel, out, element, styleMap.getStyles(), gcss);
 	}
 
 	public int printStyleStatements(ICancel cancel, PrintStream out, IGraphElement element, Collection<IStyle<?>> s,
 			GraphCSS gcss) {
 		return printStyles(cancel, out, element, true, s, gcss);
 	}
 
 	public int printStyleStatements(ICancel cancel, PrintStream out, IGraphElement element, GraphCSS gcss) {
 		return printStyles(cancel, out, element, true, gcss.collectStyles(element, cancel).getStyles(), gcss);
 	}
 }
