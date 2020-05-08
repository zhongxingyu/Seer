 package com.tobeface.modules.table;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.beanutils.BeanUtils;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.LineProcessor;
 import com.tobeface.modules.lang.Each;
 import com.tobeface.modules.lang.Files;
 import com.tobeface.modules.lang.Function;
 import com.tobeface.modules.lang.Ghost;
 import com.tobeface.modules.lang.Lang;
 import com.tobeface.modules.lang.Preconditions;
 import com.tobeface.modules.lang.Strings;
 import com.tobeface.modules.table.annotations.TableField;
 import com.tobeface.modules.table.annotations.TableValueConverters;
 
 /**
  * 
  * @author loudyn
  * 
  */
 class TextTable implements Table {
 
 	private final File workspace;
 	private final String splitter;
 
 	/**
 	 * 
 	 * @param splitter
 	 * @param end
 	 */
 	TextTable(File workspace, String splitter) {
 		Preconditions.notNull(workspace);
 		Preconditions.hasText(splitter);
 
 		this.workspace = workspace;
 		this.splitter = splitter;
 	}
 
 	protected final String getSplitter() {
 		return splitter;
 	}
 
 	public final <T> void insert(List<T> objects) {
 
 		Each<T> each = new Each<T>() {
 
 			@SuppressWarnings("unchecked")
 			public void invoke(final int index, final Object which) {
 
 				final Field[] tableFields = TableFields.tableFields(which);
 				final Ghost<T> ghost = (Ghost<T>) Ghost.me(which);
 
 				List<TableItem> items = new ArrayList<TableItem>();
 				for (Field field : tableFields) {
 					TableItem item = new TableItem();
 					item.index = field.getAnnotation(TableField.class).columnIndex();
 					item.value = ghost.ejector(which, field).eject();
 
 					TableValueConverters converters = field.getAnnotation(TableValueConverters.class);
 					item.value = TableValues.downstreamConvert(converters, item.value);
 					items.add(item);
 				}
 
 				Collections.sort(items);
 				List<String> values = Lang.transform(items, new Function<TableItem, String>() {
 
 					public String apply(TableItem input) {
						return null == input.value ? "" : input.value.toString();
 					}
 
 				});
 
 				String line = Strings.join(values, getSplitter()).concat(System.getProperty("line.separator"));
 				Files.appendTo(workspace, line, "UTF-8");
 			}
 		};
 
 		Lang.each(objects, each);
 	}
 
 	@Override
 	public final <T> List<T> select(final Class<T> clazz) {
 
 		try {
 
 			final List<T> result = new LinkedList<T>();
 			final LineProcessor<List<T>> processor = new LineProcessor<List<T>>() {
 
 				@Override
 				public boolean processLine(String line) throws IOException {
 					Ghost<T> ghost = Ghost.me(clazz);
 					T obj = ghost.born();
 
 					String[] values = Strings.split(line, getSplitter());
 					Field[] fields = TableFields.tableFields(obj);
 					for (Field field : fields) {
 						try {
 
 							TableValueConverters converters = field.getAnnotation(TableValueConverters.class);
 							String raw = values[field.getAnnotation(TableField.class).columnIndex()];
 							Object value = TableValues.upstreamConvert(converters, raw);
 							BeanUtils.setProperty(obj, field.getName(), value);
 						} catch (Exception e) {
 							throw Lang.uncheck(e);
 						}
 					}
 
 					result.add(obj);
 					return true;
 				}
 
 				@Override
 				public List<T> getResult() {
 					return result;
 				}
 
 			};
 
 			return com.google.common.io.Files.readLines(workspace, Charsets.UTF_8, processor);
 		} catch (Exception e) {
 			throw Lang.uncheck(e);
 		}
 	}
 
 	/**
 	 * 
 	 * @author loudyn
 	 * 
 	 */
 	final class TableItem implements Comparable<TableItem> {
 		int index;
 		Object value;
 
 		public int compareTo(TableItem o) {
 			return index - o.index;
 		}
 	}
 }
