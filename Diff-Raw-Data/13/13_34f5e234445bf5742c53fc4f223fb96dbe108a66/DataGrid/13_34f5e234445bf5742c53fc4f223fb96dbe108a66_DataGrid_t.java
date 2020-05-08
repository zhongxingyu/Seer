 /*
 
 Copyright (c) 2013 by Matt Zabriskie
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
 */
 
 package com.github.mzabriskie.datagrid;
 
 import java.util.*;
 
 public class DataGrid {
 	private List<List<Object>> data;
 	private List<String> columns;
 	private Map<String, Integer> widths;
 
 	private DataGrid() {
 		data = new ArrayList<List<Object>>();
 		columns = new ArrayList<String>();
 		widths = new HashMap<String, Integer>();
 	}
 
 	public DataGrid(List<String> cols) {
 		this();
 		columns = cols;
 	}
 
 	public DataGrid(String... cols) {
 		this(Arrays.asList(cols));
 	}
 
 	public void addDataRow(Object... cols) {
 		addDataRow(Arrays.asList(cols));
 	}
 
 	public void addDataRow(List<Object> row) {
 		assert row.size() == columns.size() : "Invalid row: incorrect number of columns";
 		data.add(row);
 		syncWidths(row);
 	}
 
 	public int getRowCount() {
 		return data.size();
 	}
 
 	public void render() {
		String rowSeparator = getRowSeparator();
		System.out.println(rowSeparator);
 
 		System.out.print("|");
 		for (String column : columns) {
 			System.out.print(pad(column, getWidthForColumn(column)));
 			System.out.print("|");
 		}
 		System.out.print("\n");
 
		System.out.println(rowSeparator);
 
 		for (List<Object> row : data) {
 			System.out.print("|");
 			for (int i=0; i<row.size(); i++) {
 				String key = getKeyForColumn(i);
 				System.out.print(pad(row.get(i), getWidthForColumn(key)));
 				System.out.print("|");
 			}
 			System.out.print("\n");
 		}
 
		System.out.println(rowSeparator);
 	}
 
	private String getRowSeparator() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("+");
 		for (String column : columns) {
 			sb.append(repeat("-", getWidthForColumn(column) + 2));
 			sb.append("+");
 		}
 		return sb.toString();
 	}
 
 	private int getIdxForColumn(String key) {
 		for (int i=0; i<columns.size(); i++) {
 			if (columns.get(i).equals(key)) return i;
 		}
 		return -1;
 	}
 
 	private String getKeyForColumn(int index) {
 		return columns.get(index);
 	}
 
 	private String getValForColumn(Object val) {
 		return String.valueOf(val);
 	}
 
 	private int getWidthForColumn(String key) {
 		Integer width = widths.get(key);
 		if (width == null) {
 			width = 0;
 		}
 		return Math.max(columns.get(getIdxForColumn(key)).length(), width);
 	}
 
 	private void syncWidths(int index) {
 		syncWidths(data.get(index));
 	}
 
 	private void syncWidths(List<Object> row) {
 		for (int i=0; i<row.size(); i++) {
 			String key = getKeyForColumn(i);
 			String val = getValForColumn(row.get(i));
 
 			if (widths.get(key) == null || val.length() > widths.get(key)) {
 				widths.put(key, val.length());
 			}
 		}
 	}
 
 	private String repeat(String str, int count) {
 		String result = "";
 		for (int i=0; i<count; i++) {
 			result += str;
 		}
 		return result;
 	}
 
 	private String pad(Object obj, int length) {
 		String result = "";
 		String val = getValForColumn(obj);
 
 		result += repeat(" ", length - val.length());
 
 		if (obj instanceof Number) {
 			result = result + val;
 		} else {
 			result = val + result;
 		}
 
 		return " " + result + " ";
 	}
 }
 
