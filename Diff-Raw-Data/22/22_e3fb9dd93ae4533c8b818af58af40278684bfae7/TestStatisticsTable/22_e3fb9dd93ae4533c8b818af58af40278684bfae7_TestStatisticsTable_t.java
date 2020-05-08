 // The Grinder
 // Copyright (C) 2000  Paco Gomez
 // Copyright (C) 2000  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.engine.process;
 
 import java.io.PrintStream;
 import java.text.DecimalFormat;
 
 import net.grinder.engine.EngineException;
 
 
 /** Package scope */
 class MethodStatisticsTable
 {
     private final String[] m_methodNames;
     private final MethodStatistics[] m_methodStatistics;
     private final MethodStatistics m_totals = new MethodStatistics();
     private DecimalFormat m_twoDPFormat = new DecimalFormat("0.00");
     
     public MethodStatisticsTable(String[] methodNames,
 				 MethodStatistics[] methodStatistics)
 	throws EngineException
     {
 	if (methodNames.length != methodStatistics.length) {
 	    throw new EngineException(
 		"Number of method names differs from the number of methods");
 	}
 
 	m_methodNames = methodNames;
 	m_methodStatistics = methodStatistics;
 
 	for (int i=0; i< m_methodStatistics.length; i++) {
 	    m_totals.add(m_methodStatistics[i]);
 	}
     }
 
     public void print(PrintStream out)
     {
 	final StringBuffer heading = new StringBuffer();
 	heading.append(formatField(""));
 	heading.append(formatField("Transactions"));
 	heading.append(formatField("Failures"));
 	heading.append(formatField("Total (ms)"));
 	heading.append(formatField("Average (ms)"));
 	
 	out.println(heading.toString());
 
 	for (int i=0; i<m_methodStatistics.length; i++) {
 	    out.println(formatLine(m_methodNames[i], m_methodStatistics[i]));
 	}
 
 	out.println();
	out.println(formatLine("Totals", m_totals));
     }
 
     private String formatField(String string)
     {
 	final int width = 14;
 
 	if (string.length() >= width) {
 	    return string.substring(0, width);
 	}
 	else {
 	    StringBuffer result = new StringBuffer(string);
 
 	    final int padding = width - string.length();
 
 	    for (int i=0; i<padding; i++) {
 		result.append(" ");
 	    }
 
 	    return result.toString();
 	}
     }
 
     private String formatLine(String methodName,
 			      MethodStatistics methodStatistics)
     {
 	final StringBuffer result = new StringBuffer();
 
 	result.append(formatField(methodName));
 
 	result.append(
 	    formatField(String.valueOf(methodStatistics.getTransactions())));
 
 	result.append(
 	    formatField(String.valueOf(methodStatistics.getErrors())));
 
 	result.append(
 	    formatField(String.valueOf(methodStatistics.getTotalTime())));
 
 	result.append(
 	    formatField(m_twoDPFormat.format(
 			    methodStatistics.getAverageTransactionTime())));
 
 	return result.toString();
     }
 }
