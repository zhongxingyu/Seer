 /**
  * DynamicReports - Free Java reporting library for creating reports dynamically
  *
  * Copyright (C) 2010 - 2011 Ricardo Mariaca
  * http://dynamicreports.sourceforge.net
  *
  * This file is part of DynamicReports.
  *
  * DynamicReports is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * DynamicReports is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with DynamicReports. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.sf.dynamicreports.examples.complex.dynamicreport;
 
 import static net.sf.dynamicreports.report.builder.DynamicReports.*;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.dynamicreports.examples.Templates;
 import net.sf.dynamicreports.examples.complex.ReportDesign;
 import net.sf.dynamicreports.report.builder.ReportBuilder;
 import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
 import net.sf.dynamicreports.report.builder.component.PageXofYBuilder;
 import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
 import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
 import net.sf.dynamicreports.report.constant.HorizontalAlignment;
 import net.sf.dynamicreports.report.exception.DRException;
 
 /**
  * @author Ricardo Mariaca (dynamicreports@gmail.com)
  */
 public class DynamicReportDesign implements ReportDesign<DynamicReportData> {
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void configureReport(ReportBuilder<?> rb, DynamicReportData data) throws DRException {
 		rb.setTemplate(Templates.reportTemplate)
 			.title(Templates.createTitleComponent("DynamicReport"));
 
 		DynamicReport report = data.getDynamicReport();
 		List<DynamicColumn> columns = report.getColumns();
 		Map<String, TextColumnBuilder> drColumns = new HashMap<String, TextColumnBuilder>();
 
 		for (DynamicColumn column : columns) {
			TextColumnBuilder drColumn = col.column(column.getTitle(), column.getName(), type.detectType(column.getType()));
 			if (column.getPattern() != null) {
 				drColumn.setPattern(column.getPattern());
 			}
 			if (column.getHorizontalAlignment() != null) {
 				drColumn.setHorizontalAlignment(column.getHorizontalAlignment());
 			}
 			drColumns.put(column.getName(), drColumn);
 			rb.columns(drColumn);
 		}
 
 		for (String group : report.getGroups()) {
 			ColumnGroupBuilder group2 = grp.group(drColumns.get(group));
 			rb.groupBy(group2);
 
 			for (String subtotal : report.getSubtotals()) {
 				rb.subtotalsAtGroupFooter(group2, sbt.sum(drColumns.get(subtotal)));
 			}
 		}
 
 		for (String subtotal : report.getSubtotals()) {
 			rb.subtotalsAtSummary(sbt.sum(drColumns.get(subtotal)));
 		}
 
 		if (report.getTitle() != null) {
 			TextFieldBuilder<String> title = cmp.text(report.getTitle())
 				.setStyle(Templates.bold12CenteredStyle)
 				.setHorizontalAlignment(HorizontalAlignment.CENTER);
 			rb.addTitle(title);
 		}
 		if (report.isShowPageNumber()) {
 			PageXofYBuilder pageXofY = cmp.pageXofY()
 				.setStyle(Templates.boldCenteredStyle);
 			rb.addPageFooter(pageXofY);
 		}
 
 	}
 }
