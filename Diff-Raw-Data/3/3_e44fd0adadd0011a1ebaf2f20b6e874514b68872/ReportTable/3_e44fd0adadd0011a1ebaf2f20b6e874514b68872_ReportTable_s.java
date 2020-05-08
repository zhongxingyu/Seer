 package br.com.bluesoft.report.components;
 
 import static br.com.bluesoft.commons.lang.StringUtil.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.bluesoft.report.Report;
 
 public class ReportTable implements ReportComponent {
 
 	private final Report report;
 	private final List<ReportTableRow> rows = new ArrayList<ReportTableRow>();
 	private final float[] widths;
 	private DataType[] dataTypes;
 
 	private boolean border = true;
 	private boolean fixedFotter = false;
 	private int fixedColumns = 0;
 
 	public ReportTable(final Report report, final float[] widths) {
 		this.report = report;
 		this.widths = widths;
 	}
 
 	public ReportTable addEmptyRow() {
 		final ReportTableRow row = new ReportTableRow(this);
 		if (widths != null) {
 			for (int i = 1; i < widths.length + 1; i++) {
 				row.addColumn(SPACE);
 			}
 		}
 		rows.add(row);
 		return this;
 	}
 
 	public ReportTable addEmptyRows(final int numberOfRows) {
 		for (int i = 0; i < numberOfRows; i++) {
 			addEmptyRow();
 		}
 		return this;
 	}
 
 	public ReportTableRow addRow() {
 		final ReportTableRow row = new ReportTableRow(this);
 		rows.add(row);
 		return row;
 	}
 
 	public ReportTableRow addBoldRow() {
 		final ReportTableRow row = new ReportTableRow(this, true);
 		rows.add(row);
 		return row;
 	}
 
 	public ReportTableRow addRowWithValues(final Object... columnsValues) {
 		final ReportTableRow row = new ReportTableRow(this);
 		for (final Object value : columnsValues) {
 			row.addColumn(String.valueOf(value));
 		}
 		rows.add(row);
 		return row;
 	}
 
 	public ReportTableRow addBoldRowWithValues(final String... columnsValues) {
 		final ReportTableRow row = new ReportTableRow(this);
 		for (final String value : columnsValues) {
 			row.addBoldColumn(value);
 		}
 		rows.add(row);
 		return row;
 	}
 
 	public ReportTable fixColumns(final int numberOfColumnsToFix) {
 		fixedColumns = numberOfColumnsToFix;
 		return this;
 	}
 
 	public List<ReportTableRow> getRows() {
 		return rows;
 	}
 
 	public boolean isFixedFotter() {
 		return fixedFotter;
 	}
 
 	@Override
 	public Report end() {
 		return report;
 	}
 
 	public float[] getColumnsWidths() {
 		return widths;
 	}
 
 	public ReportTable withBorder() {
 		border = true;
 		return this;
 	}
 
 	public ReportTable withoutBorder() {
 		border = false;
 		return this;
 	}
 
 	public boolean hasBorder() {
 		return border;
 	}
 
 	public ReportTable withFixedFooter() {
 		fixedFotter = true;
 		return this;
 	}
 
 	public int getFixedColumns() {
 		return fixedColumns;
 	}
 
 	public void setDataTypes(final DataType[] dataTypes) {
 		this.dataTypes = dataTypes;
 	}
 
 	public DataType getDateTypeOfColumn(final int columnIndex) {
 		try {
 			return dataTypes[columnIndex];
 		} catch (final ArrayIndexOutOfBoundsException e) {
 			return DataType.Text;
 		}
 	}
 
 }
