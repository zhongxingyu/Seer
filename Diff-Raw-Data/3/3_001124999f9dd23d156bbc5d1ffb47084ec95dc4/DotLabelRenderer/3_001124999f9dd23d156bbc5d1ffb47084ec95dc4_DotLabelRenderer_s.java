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
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.Set;
 
 import org.cloudsmith.graph.ICancel;
 import org.cloudsmith.graph.IGraphElement;
 import org.cloudsmith.graph.ILabeledGraphElement;
 import org.cloudsmith.graph.ITable;
 import org.cloudsmith.graph.ITableCell;
 import org.cloudsmith.graph.ITableRow;
 import org.cloudsmith.graph.elements.GraphCell;
 import org.cloudsmith.graph.elements.GraphRow;
 import org.cloudsmith.graph.elements.GraphTable;
 import org.cloudsmith.graph.graphcss.GraphCSS;
 import org.cloudsmith.graph.graphcss.StyleSet;
 import org.cloudsmith.graph.style.Alignment;
 import org.cloudsmith.graph.style.IStyle;
 import org.cloudsmith.graph.style.IStyleFactory;
 import org.cloudsmith.graph.style.IStyleVisitor;
 import org.cloudsmith.graph.style.Span;
 import org.cloudsmith.graph.style.StyleFactory;
 import org.cloudsmith.graph.style.StyleType;
 import org.cloudsmith.graph.style.StyleVisitor;
 import org.cloudsmith.graph.style.VerticalAlignment;
 import org.cloudsmith.graph.style.labels.DynamicLabelTemplate;
 import org.cloudsmith.graph.style.labels.ILabelTemplate;
 import org.cloudsmith.graph.style.labels.LabelCell;
 import org.cloudsmith.graph.style.labels.LabelMatrix;
 import org.cloudsmith.graph.style.labels.LabelRow;
 import org.cloudsmith.graph.style.labels.LabelStringTemplate;
 import org.cloudsmith.graph.style.labels.LabelTable;
 import org.cloudsmith.graph.utils.Counter;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Renders dot labels.
  * 
  */
 @Singleton
 public class DotLabelRenderer {
 
 	@Inject
 	IStyleFactory styles;
 
 	private final String emptyString;
 
 	@Inject
 	public DotLabelRenderer(@DotRenderer.EmptyString String emptyString) {
 		this.emptyString = emptyString;
 	}
 
 	private GraphTable createTable(ILabeledGraphElement theGraphElement, LabelTable templateTable, ICancel cancel) {
 		// Context methodContext = Contexts.getMethodContext();
 		// methodContext.set("element", m_ge);
 
 		// create the GraphTable using a styleClass that is possibly set using
 		// EL
 		Set<String> tmp = templateTable.getStyleClasses(theGraphElement);
 
 		GraphTable gt = new GraphTable(tmp);
 
 		// set parent so containment selection for styles work
 		gt.setParentElement(theGraphElement);
 
 		// For all rows in the template, create a GraphRow
 		for(LabelRow r : templateTable.getRows()) {
 			cancel.assertContinue();
 
 			tmp = r.getStyleClasses(theGraphElement);
 			GraphRow gr = r.isSeparator()
 					? new GraphRow.SeparatorRow()
 					: new GraphRow(tmp);
 			gt.addRow(gr);
 
 			// for all cells in the template, create a GraphCell
 			for(LabelCell c : r.getCells()) {
 				tmp = c.getStyleClass(theGraphElement);
 				ILabelTemplate template = c.getValue(theGraphElement);
 				if(template == null)
 					template = new LabelStringTemplate("");
 
 				// resolve dynamic template to depth 100
 				for(int i = 0; template instanceof DynamicLabelTemplate; i++) {
 					if(i > 100)
 						throw new IllegalArgumentException("Dynamic Templates nested too deep > 100");
 					template = ((DynamicLabelTemplate) template).getTemplate(theGraphElement);
 				}
 
 				GraphCell gc = null;
 				if(c.isSeparator()) {
 					gc = new GraphCell.SeparatorCell();
 				}
 				else if(template instanceof LabelStringTemplate) {
 					String val = ((LabelStringTemplate) template).getTemplateString(theGraphElement);
 					val = (val == null)
 							? ""
 							: val;
 
 					gc = new GraphCell(val, tmp);
 				}
 				else if(template instanceof LabelTable) {
 					gc = new GraphCell("", tmp);
 					gc.setTableContent(createTable(theGraphElement, (LabelTable) template, cancel));
 				}
 				// TODO: MUST BE ABLE TO PICK UP INSTANCE STYLES FOR LABEL CELL
 
 				// If the labelCell has instance styles, make sure they are included
 				StyleSet instanceSet = c.getStyles();
 				StyleSet styleMap = new StyleSet();
 				styleMap.add(instanceSet);
 
 				// If the label cell has a span that is not 1x1, set that as instance style
 				// of the rendered IGraphCell.
 				Span span = c.getSpan();
 				if(span != null && span != Span.SPAN_1x1) {
 					if(span.getRowspan() != 1)
 						styleMap.put(new StyleFactory.RowSpan(span.getRowspan()));
 					if(span.getColspan() != 1)
 						styleMap.put(new StyleFactory.ColSpan(span.getColspan()));
 				}
 				// If there were any instance styles - apply them
 				if(styleMap.getStyles().size() > 0)
 					gc.setStyles(styleMap);
 				gr.addCell(gc);
 			}
 		}
 		return gt;
 	}
 
 	private String emptyString(String x) {
 		if(x == null || x.length() == 0)
 			x = emptyString;
 		return x;
 	}
 
 	/**
 	 * Escapes characters that are unsafe in a dot table label.
 	 * 
 	 * @param s
 	 * @return
 	 */
 	private String escapeUnsafe(String s) {
		s = s.replace("\n", "<BR/>");
 		s = s.replace("&", "&amp;");
 		s = s.replace("<", "&lt;");
 		s = s.replace(">", "&gt;");
 		return s;
 	}
 
 	/**
 	 * 
 	 * @param ge
 	 * @param labelnode
 	 * @return a vector of three strings - the parsed element attributes, attributes for font, and attribute for rendered.
 	 */
 	private String[] parseGraphTableAttributes(final ILabeledGraphElement ge, final IGraphElement labelnode,
 			GraphCSS styleRules, ICancel cancel) {
 		final String[] result = new String[3];
 		// in case there are no attributes to set
 		result[0] = "";
 		result[1] = "";
 		result[2] = "true";
 
 		final ByteArrayOutputStream elementText = new ByteArrayOutputStream();
 		final ByteArrayOutputStream fontText = new ByteArrayOutputStream();
 		final PrintStream elementStream = new PrintStream(elementText);
 		final PrintStream fontStream = new PrintStream(fontText);
 
 		// get the styling for the gt
 		Collection<IStyle<?>> s = styleRules.collectStyles(labelnode, cancel).getStyles();
 		if(s == null || s.size() < 1)
 			return result; // no attributes to set
 
 		final Counter count = new Counter();
 		final Counter fontCount = new Counter();
 
 		IStyleVisitor visitor = new StyleVisitor() {
 			@Override
 			public void align(Alignment x) {
 				elementStream.printf(" ALIGN=\"%s\"", x);
 			}
 
 			@Override
 			public void backgroundColor(String x) {
 				elementStream.printf(" BGCOLOR=\"%s\"", x);
 			}
 
 			@Override
 			public void borderWidth(int x) {
 				elementStream.printf(" BORDER=\"%s\"", x);
 			}
 
 			@Override
 			public void cellBorderWidth(int x) {
 				if(!(labelnode instanceof ITable))
 					throw new IllegalArgumentException("cellBorderWidth is not a supported style attribute of a - " +
 							labelnode.getClass() + ". Use borderWidth on a cell.");
 				elementStream.printf(" CELLBORDER=\"%s\"", x);
 			}
 
 			@Override
 			public void cellPadding(int x) {
 				if(!(labelnode instanceof ITable || labelnode instanceof ITableCell))
 					throw new IllegalArgumentException("cellPadding is not a supported style attribute of a " +
 							labelnode.getClass());
 				elementStream.printf(" CELLPADDING=\"%s\"", x);
 			}
 
 			@Override
 			public void cellSpacing(int x) {
 				elementStream.printf(" CELLSPACING=\"%s\"", x);
 			}
 
 			@Override
 			public void color(String x) {
 				count.decrement(); // don't count as element's attribute
 				fontStream.printf(" COLOR=\"%s\"", x);
 			}
 
 			@Override
 			public void colSpan(int x) {
 				if(!(labelnode instanceof ITableCell))
 					throw new IllegalArgumentException("colSpan is not a supported style attribute of a " +
 							labelnode.getClass());
 				elementStream.printf(" COLSPAN=\"%s\"", x);
 			}
 
 			@Override
 			public void fixedSize(boolean x) {
 				elementStream.printf(" FIXEDSIZE=\"%s\"", x);
 			}
 
 			@Override
 			public void fontFamily(String x) {
 				count.decrement(); // don't count as element's attribute
 				fontStream.printf(" FACE=\"%s\"", x);
 			}
 
 			@Override
 			public void fontSize(int x) {
 				count.decrement(); // don't count as element's attribute
 				fontStream.printf(" POINT-SIZE=\"%d\"", x);
 				fontCount.increment();
 			}
 
 			/**
 			 * height is a floating point number as some heights are specified as points and need the decimal
 			 * the stylesheet should contain an integral number - but better be safe and round it before
 			 * using "0.5" would otherwise be size "0".
 			 * The right thing to do would be to have just one measuring system and actually transform
 			 * printers points 1/72" to "pixels" - but this requires resolution of the output device to be accurate
 			 * and is just too much bother - simply specify integral numbers in the stylesheet and know that it is
 			 * interpreted as "pixels" (or whatever graphviz things the integral number means.
 			 */
 			@Override
 			public void height(double x) {
 				elementStream.printf(" HEIGHT=\"%s\"", Math.round(x));
 			}
 
 			@Override
 			public void href(String x) {
 				if(x == null || x.length() < 1)
 					count.decrement();
 				else
 					elementStream.printf(" HREF=\"%s\"", x);
 			}
 
 			@Override
 			public void port(String x) {
 				elementStream.printf(" PORT=\"%s\"", x);
 			}
 
 			@Override
 			public void rendered(boolean x) {
 				result[2] = String.valueOf(x);
 				count.decrement(); // no output
 			}
 
 			@Override
 			public void rowSpan(int x) {
 				if(!(labelnode instanceof ITableCell))
 					throw new IllegalArgumentException("rowSpan is not a supported style attribute of a " +
 							labelnode.getClass());
 				elementStream.printf(" ROWSPAN=\"%s\"", x);
 			}
 
 			@Override
 			public void target(String x) {
 				elementStream.printf(" TARGET=\"%s\"", x);
 			}
 
 			@Override
 			public void tooltip(String x) {
 				elementStream.printf(" TOOLTIP=\"%s\"", emptyString(x));
 			}
 
 			@Override
 			public void unsupported(StyleType type) {
 				throw new IllegalArgumentException(type + "is not a supported style for element of class " +
 						labelnode.getClass().toString());
 			}
 
 			@Override
 			public void verticalAlign(VerticalAlignment x) {
 				elementStream.printf(" VALIGN=\"%s\"", x);
 			}
 
 			/**
 			 * @see #height regarding rounding
 			 */
 			@Override
 			public void width(double x) {
 				elementStream.printf(" WIDTH=\"%s\"", Math.round(x));
 			}
 		};
 
 		for(IStyle<?> style : s) {
 			style.visit(ge, visitor);
 			count.increment();
 			cancel.assertContinue();
 		}
 		result[0] = elementText.toString();
 		result[1] = fontText.toString();
 		return result;
 	}
 
 	/**
 	 * Prints the label on the form label=<
 	 * <TABLE>
 	 * ...
 	 * </TABLE>>
 	 * - returns true if a label was printed.
 	 * 
 	 * @param ge
 	 * @param printComma
 	 *            , if true a comma is printed before the label.
 	 * @return
 	 */
 	public boolean print(PrintStream out, ILabeledGraphElement theGraphElement, ILabelTemplate labelTemplate,
 			boolean printComma, char sepChar, GraphCSS gcss, ICancel cancel) {
 		// resolve dynamic template to depth 100
 		for(int i = 0; labelTemplate instanceof DynamicLabelTemplate; i++) {
 			if(i > 100)
 				throw new IllegalArgumentException("Dynamic Templates nested too deep > 100");
 			labelTemplate = ((DynamicLabelTemplate) labelTemplate).getTemplate(theGraphElement);
 		}
 
 		if(labelTemplate instanceof LabelStringTemplate)
 			return printStringLabel(
 				out, theGraphElement, ((LabelStringTemplate) labelTemplate).getTemplateString(theGraphElement),
 				printComma, sepChar);
 		else if(labelTemplate instanceof LabelMatrix)
 			return printMatrix(out, theGraphElement, (LabelMatrix) labelTemplate, printComma, sepChar, gcss, cancel);
 		return printTable(out, theGraphElement, (LabelTable) labelTemplate, printComma, sepChar, gcss, cancel);
 
 	}
 
 	private void printGraphCell(PrintStream out, ILabeledGraphElement ge, ITableCell gc, GraphCSS gcss, ICancel cancel) {
 		if(gc.isSeparator()) {
 			out.print("<VR/>");
 			return;
 		}
 		String[] p = parseGraphTableAttributes(ge, gc, gcss, cancel);
 		// if "rendered" == false, do not output anything
 		if(p[2].toLowerCase().equals("false"))
 			return;
 
 		ITable gt = gc.getTableContents();
 		String cellText = gc.getValue();
 
 		// if there are font attributes - output that around the text in the cell
 		// (unless text is empty string = graphviz error).
 		boolean withFontData = gt == null && p[1] != null && p[1].length() > 0 && cellText.length() > 0;
 		out.printf("<TD %s>", p[0]);
 		if(withFontData)
 			out.printf("<FONT %s>", p[1]);
 
 		// the value has already been interpolated when the GraphCell was set up
 		// so just output the table or a string here.
 		if(gt != null)
 			printGraphTable(out, ge, gt, gcss, cancel);
 		else
 			out.print(escapeUnsafe(cellText));
 		if(withFontData)
 			out.print("</FONT>");
 		out.print("</TD>");
 	}
 
 	private void printGraphRow(PrintStream out, ILabeledGraphElement ge, ITableRow gr, GraphCSS gcss, ICancel cancel) {
 		if(gr.isSeparator()) {
 			out.print("<HR/>");
 			return;
 		}
 
 		out.print("<TR>");
 		for(ITableCell gc : gr.getCells())
 			printGraphCell(out, ge, gc, gcss, cancel);
 		out.print("</TR>");
 	}
 
 	private boolean printGraphTable(PrintStream out, ILabeledGraphElement theGraphElement, ITable gt, GraphCSS gcss,
 			ICancel cancel) {
 		String[] p = parseGraphTableAttributes(theGraphElement, gt, gcss, cancel);
 		// if "rendered" == false, do not output anything
 		if(p[2].toLowerCase().equals("false"))
 			return false;
 
 		// if there are font attributes - output that around the table
 		//
 		boolean withFontData = p[1] != null && p[1].length() > 0;
 		// out.print("<");
 		if(withFontData)
 			out.printf("<FONT %s>", p[1]);
 		out.printf("<TABLE %s>", p[0]);
 		for(ITableRow r : gt.getRows())
 			printGraphRow(out, theGraphElement, r, gcss, cancel);
 		out.print("</TABLE>");
 		if(withFontData)
 			out.print("</FONT>");
 		// out.print(">");
 		return true;
 	}
 
 	private boolean printMatrix(PrintStream out, ILabeledGraphElement theGraphElement, LabelMatrix templateMatrix,
 			boolean printComma, char sepChar, GraphCSS gcss, ICancel cancel) {
 
 		// create the GraphTable using a styleClass that is possibly set using
 		// EL
 		String tmp = templateMatrix.getStyleClass(theGraphElement);
 
 		GraphTable gt = new GraphTable(tmp);
 
 		// set parent so containment selection for styles work
 		gt.setParentElement(theGraphElement);
 		// set a port "pt" on the table itself so it can be pointed to
 		gt.setStyles(styles.port("pt"));
 
 		// For all rows in the template, create a GraphRow
 		for(int r = 0; r < templateMatrix.getRows(); r++) {
 			tmp = templateMatrix.getRowStyleClass(theGraphElement);
 			GraphRow gr = new GraphRow(tmp);
 			gt.addRow(gr);
 			// for all cells in the template, create a GraphCell
 			for(int c = 0; c < templateMatrix.getColumns(); c++) {
 				tmp = templateMatrix.getCellStyleClass(theGraphElement);
 				String val = templateMatrix.getValue(r, c);
 				GraphCell gc = new GraphCell(val, tmp);
 				gr.addCell(gc);
 
 				StyleSet styleMap = new StyleSet();
 				styleMap.put(styles.port("p" + templateMatrix.getValue(r, c)));
 				gc.setStyles(styleMap);
 
 			}
 		}
 		// Now armed with the label graph - we need to visit those nodes, get the styling of them, and provide
 		// output!
 		//
 		String[] p = parseGraphTableAttributes(theGraphElement, gt, gcss, cancel);
 		// if "rendered" == false, do not output anything
 		if(p[2].toLowerCase().equals("false"))
 			return false;
 
 		out.printf("%slabel=", printComma
 				? sepChar + " "
 				: "");
 
 		return printGraphTable(out, theGraphElement, gt, gcss, cancel);
 	}
 
 	private boolean printStringLabel(PrintStream out, ILabeledGraphElement theGraphElement, String simpleTemplate,
 			boolean printComma, char sepChar) {
 		String tmp = simpleTemplate; // already interpolated
 
 		// if there is a result output that as the label without any styling.
 		if(tmp != null && tmp.length() < 1)
 			return false;
 		out.printf("%slabel=\"", printComma
 				? sepChar + " "
 				: "");
 		out.print(tmp); // use print to preserve /n
 		out.print("\"");
 		return true;
 	}
 
 	/**
 	 * Print the top level table label.
 	 * 
 	 * @param out
 	 * @param theGraphElement
 	 * @param templateTable
 	 * @param printComma
 	 * @param sepChar
 	 * @param gcss
 	 * @param cancel
 	 * @return
 	 */
 	private boolean printTable(PrintStream out, ILabeledGraphElement theGraphElement, LabelTable templateTable,
 			boolean printComma, char sepChar, GraphCSS gcss, ICancel cancel) {
 		GraphTable gt = createTable(theGraphElement, templateTable, cancel);
 
 		// Now armed with the label graph - we need to visit those nodes, get the styling of them, and provide
 		// output!
 		//
 		String[] p = parseGraphTableAttributes(theGraphElement, gt, gcss, cancel);
 		// if "rendered" == false, do not output anything
 		if(p[2].toLowerCase().equals("false"))
 			return false;
 
 		out.printf("%slabel=", printComma
 				? sepChar + " "
 				: "");
 
 		out.print("<");
 		boolean result = printGraphTable(out, theGraphElement, gt, gcss, cancel);
 		out.print(">");
 		return result;
 	}
 
 }
