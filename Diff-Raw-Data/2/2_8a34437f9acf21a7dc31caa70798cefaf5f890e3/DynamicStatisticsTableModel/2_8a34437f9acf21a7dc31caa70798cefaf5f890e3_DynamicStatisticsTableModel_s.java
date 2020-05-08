 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
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
 
 package net.grinder.console.swingui;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.swing.table.AbstractTableModel;
 
 import net.grinder.console.common.ConsoleException;
 import net.grinder.console.model.Model;
 import net.grinder.console.model.ModelListener;
 import net.grinder.statistics.ExpressionView;
 import net.grinder.statistics.StatisticExpression;
 import net.grinder.statistics.StatisticsView;
 import net.grinder.statistics.TestStatistics;
 import net.grinder.statistics.TestStatisticsFactory;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 abstract class DynamicStatisticsTableModel
     extends AbstractTableModel implements ModelListener
 {
     private final Model m_model;
     private final Resources m_resources;
     private boolean m_modelInvalid;
 
     private final String m_testString;
     private final String m_testColumnString;
     private final String m_testDescriptionColumnString;
 
     private StatisticsView m_statisticsView = new StatisticsView();
     private ExpressionView[] m_columnViews;
     private String[] m_columnLabels;
 
     protected DynamicStatisticsTableModel(Model model, Resources resources)
 	throws ConsoleException
     {
 	m_model = model;
 	m_resources = resources;
 
 	m_testString = resources.getString("table.test.label") + " ";
 	m_testColumnString = resources.getString("table.testColumn.label");
 	m_testDescriptionColumnString =
 	    resources.getString("table.descriptionColumn.label");
 
 	m_modelInvalid = true;
 	m_model.addModelListener(new SwingDispatchedModelListener(this));
 
 	final TestStatisticsFactory testStatisticsFactory =
 	    TestStatisticsFactory.getInstance();
 
 	final StatisticsView statisticsView = new StatisticsView();
 
 	statisticsView.add(testStatisticsFactory.getStatisticsView());
 
 	statisticsView.add(m_model.getTPSExpressionView());
 
 	addColumns(statisticsView);
     }
 
     protected abstract TestStatistics getStatistics(int row);
 
     protected final Model getModel()
     {
 	return m_model;
     }
 
     protected final boolean isModelInvalid()
     {
 	return m_modelInvalid;
     }
 
     public synchronized void addColumns(StatisticsView statisticsView)
     {
 	m_statisticsView.add(statisticsView);
 
 	m_columnViews = m_statisticsView.getExpressionViews();
 
 	final String[] columnLabels =
 	    new String[m_columnViews.length];
 
 	for (int i=0; i<columnLabels.length; ++i) {
 	    final String resource =
 		m_resources.getString(
 		    m_columnViews[i].getDisplayNameResourceKey(), false);
 
 	    columnLabels[i] =
 		resource != null ?
 		resource : m_columnViews[i].getDisplayName();
 	}
 	
 	m_columnLabels = columnLabels;
     }
 
     public synchronized void reset(Set newTests)
     {
 	m_modelInvalid = newTests.size() > 0;
     }
 
     public synchronized void update()
     {
 	final boolean wasInvalid = m_modelInvalid;
 	m_modelInvalid = false;
 
 	if (wasInvalid) {
 	    // We've been reset, number of rows may have changed.
 	    fireTableDataChanged();
 	}
 	else {
 	    fireTableRowsUpdated(0, getRowCount());
 	}
     }
 
     public synchronized int getColumnCount()
     {
 	return 2 + m_columnLabels.length;
     }
 
     public synchronized String getColumnName(int column)
     {
 	switch (column) {
 	case 0:
 	    return m_testColumnString;
 
 	case 1:
 	    return m_testDescriptionColumnString;
 
 	default:
 	    return m_columnLabels[column - 2];
 	}
     }
 
     public int getRowCount()
     {
 	return m_model.getNumberOfTests();
     }
 
     public synchronized Object getValueAt(int row, int column)
     {
 	if (m_modelInvalid) {
 	    return "";
 	}
 	else {
 	    if (column == 0) {
 		return m_testString + m_model.getTest(row).getNumber();
 	    }
 	    else if (column == 1) {
 		return m_model.getTest(row).getDescription();
 	    }
 	    else {
 		return getDynamicField(getStatistics(row), column - 2);
 	    }
 	}
     }
 
     protected synchronized String getDynamicField(TestStatistics statistics,
 						  int dynamicColumn)
     {
 	if (dynamicColumn < m_columnViews.length) {
 	    final StatisticExpression expression =
 		m_columnViews[dynamicColumn].getExpression();
 
 	    if (expression.isDouble()) {
 		final double value = expression.getDoubleValue(statistics);
 
 		if (Double.isNaN(value)) {
 		    return "";
 		}
 		else {
 		    return m_model.getNumberFormat().format(value);
 		}
 	    }
 	    else {
 		return String.valueOf(expression.getLongValue(statistics));
 	    }
 	}
 	else {
 	    return "?";
 	}
     }
 
     public boolean isBold(int row, int column) 
     {
 	return isRed(row, column);
     }
 
     public boolean isRed(int row, int column)
     {
 	return column == 3 && getStatistics(row).getErrors() > 0;
     }
 
     public synchronized void write(Writer writer, String columnDelimiter,
 				   String lineDelimeter)
 	throws IOException
     {
 	final int numberOfRows = getRowCount();
 	final int numberOfColumns = getColumnCount();
 
 	writer.write(m_testColumnString);
 	writer.write(columnDelimiter);
 	writer.write(m_testDescriptionColumnString);
 	writer.write(columnDelimiter);
 	
	for (int dynamicColumn=0; dynamicColumn<numberOfColumns;
 	     dynamicColumn++)
 	{
 	    writer.write(m_columnLabels[dynamicColumn]);
 	    writer.write(columnDelimiter);
 	}
 
 	writer.write(lineDelimeter);
 
 	for (int row=0; row<numberOfRows; row++) {
 	    for (int column=0; column<numberOfColumns; column++) {
 		final Object o = getValueAt(row, column);
 		writer.write(o != null ? o.toString() : "");
 		writer.write(columnDelimiter);
 	    }
 
 	    writer.write(lineDelimeter);
 	}
     }
 }
