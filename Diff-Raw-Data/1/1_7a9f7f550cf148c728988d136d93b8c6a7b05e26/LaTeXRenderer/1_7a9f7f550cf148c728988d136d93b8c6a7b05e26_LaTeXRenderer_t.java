 /*
  * Copyright (c) 2012 - 2013 Mateusz Parzonka
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Mateusz Parzonka - initial API and implementation
  */
 package jabular;
 
 
 /**
  * Renders a tabular to String. Configurable in fluent style.
  */
 public class LaTeXRenderer {
 
     private final Tabular tabular;
     private final boolean[] hline;
     private String header;
 
     private int printRowLabels;
     private int printColumnLabels;
 
     public LaTeXRenderer(Tabular tabular) {
 	this.tabular = tabular;
 	hline = new boolean[tabular.getRowLabels().length];
     }
 
     /**
      * Sets the header of the tabular: <code>\begin{tabular}[header]</code> If the header is not set or set to null, a
      * header is generated automatically. Example header for a column size of 3 with <code>printRowLabels = true</code>
      * would be <code>{l|lll}</code>.
      *
      * @param header
      * @return this renderer
      */
     public LaTeXRenderer setHeader(String header) {
 	this.header = header;
 	return this;
     }
 
     /**
      * Adds a horizontal line <b>after</b> the row with the given number.
      *
      * @param rowNumber
      * @return this renderer
      */
     public LaTeXRenderer addHorizontalLine(int rowNumber) {
 	hline[rowNumber] = true;
 	return this;
     }
 
     /**
      * Sets if the row labels are printed as well. The automatically generated header will separate the row labels from
      * the data by drawing a vertical line.
      *
      * @param printRowLabels
      * @return this renderer
      */
     public LaTeXRenderer setPrintRowLabels(boolean printRowLabels) {
 	this.printRowLabels = printRowLabels ? 1 : 0;
 	return this;
     }
 
     /**
      * Sets if the column labels are printed as well. If set to true, a horizontal line will be generated to separate
      * the column labels from the data.
      *
      * @param printColumnLabels
      * @return this renderer
      */
     public LaTeXRenderer setPrintColumnLabels(boolean printColumnLabels) {
 	this.printColumnLabels = printColumnLabels ? 1 : 0;
 	return this;
     }
 
     /**
      * Renders the tabular with given configuration to String.
      */
     @Override
     public String toString() {
 	StringBuilder sbResult = new StringBuilder();
 	final int rows = tabular.getRowLabels().length;
 	final int columns = tabular.getColumnLabels().length;
 
 	StringBuilder sbHeader;
 	if (header != null) {
 	    sbHeader = new StringBuilder(header);
 	} else {
 	    sbHeader = new StringBuilder();
 	    sbHeader.append("{");
 	    for (int i = -printRowLabels; i < columns; i++) {
 		sbHeader.append("l");
 		if (i < 0) {
 		    sbHeader.append("|");
 		}
 	    }
 	    sbHeader.append("}");
 	}
 	sbResult.append("\\begin{tabular}").append(sbHeader.toString()).append("\n");
 
 	for (int j = -printColumnLabels; j < rows; j++) {
 	    for (int i = -printRowLabels; i < columns; i++) {
 		if (j < 0 && i < 0) {
 		    sbResult.append("  & ");
 		} else if (j < 0) {
 		    sbResult.append(tabular.getColumnLabels()[i]);
 		    if (i < columns - 1) {
 			sbResult.append(" & ");
 		    } else {
 			sbResult.append(" \\\\ \\hline\n");
 		    }
 		} else if (i < 0) {
 		    sbResult.append(tabular.getRowLabels()[j]).append(" & ");
 		} else {
 		    sbResult.append(tabular.getData()[j][i]);
 		    if (i < columns - 1) {
 			sbResult.append(" & ");
 		    } else {
 			sbResult.append(" \\\\");
 			if (hline[j]) {
 			    sbResult.append(" \\hline");
 			}
 			sbResult.append("\n");
 		    }
 		}
 	    }
 	}
 
 	sbResult.append("\\end{tabular}");
 	return sbResult.toString();
     }
 
 }
