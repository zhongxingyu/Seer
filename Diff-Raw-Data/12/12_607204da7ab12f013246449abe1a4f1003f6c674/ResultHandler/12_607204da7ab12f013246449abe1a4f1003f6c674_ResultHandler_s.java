 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.sql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import zcu.xutil.Objutil;
 import zcu.xutil.utils.Accessor;
 
 /**
  * The Class ResultHandler.
  * 
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 public abstract class ResultHandler<T> implements Handler<T> {
 	private static final QueryOptions ONE_ROW = new QueryOptions(0, 1, 0);
 
 	public abstract T handleRow(ResultSet rs) throws SQLException;
 
 	@Override
 	public QueryOptions getOptions() {
 		return ONE_ROW;
 	}
 
 	/**
 	 * 
 	 * @return 结果集为空返回null.
 	 */
 	@Override
 	public T handle(ResultSet rs) throws SQLException {
 		return rs.next() ? handleRow(rs) : null;
 	}
 
 	/**
	 * @see #list(int, int, int)
 	 */
 	public final Handler<List<T>> list() {
 		return list(null);
 	}
 
 	/**
 	 * 多行处理句柄
 	 * 
 	 * @param fetchsize
 	 *            the fetchsize
 	 * @param maxrows
 	 *            the maxrows
 	 * 
 	 * @return the result handler< list< t>>
 	 */
 	public final Handler<List<T>> list(int fetchsize, int maxrows) {
 		return list(new QueryOptions(fetchsize, maxrows, 0));
 	}
 
 	/**
 	 * 多行处理句柄
 	 * 
	 * @param QueryOptions
 	 *            the options
 	 * 
 	 * @return the result handler< list< t>>
 	 */
 	public final Handler<List<T>> list(final QueryOptions queryOptions) {
 		final QueryOptions options = queryOptions == null ? QueryOptions.DEFAULT : queryOptions;
 		return new Handler<List<T>>() {
 			@Override
 			public List<T> handle(ResultSet rs) throws SQLException {
 				if (options.location(rs)) {
 					List<T> list = new ArrayList<T>();
 					do {
 						list.add(ResultHandler.this.handleRow(rs));
 					} while (rs.next());
 					return list;
 				}
 				return Collections.<T> emptyList();
 			}
 
 			@Override
 			public QueryOptions getOptions() {
 				return options;
 			}
 		};
 	}
 
 	/**
 	 * 分页处理句柄
 	 * 
 	 * @param pagesize
 	 *            the pagesize
 	 * @param begin
 	 *            the begin page,numbered form 0;
 	 * @param end
 	 *            the end page (excluded);
 	 * @return the result handler< list< t>>
 	 */
 	public final Handler<List<T>> page(int pagesize, int begin, int end) {
 		Objutil.validate(pagesize > 0 && begin >= 0 && end > begin, "pagesize>0 && begin>=0 && end>begin");
 		return list(new QueryOptions(pagesize < 25 ? 25 : pagesize, end * pagesize, begin * pagesize));
 	}
 
 	protected static Map<String, Accessor> getAllAccessor(Class clazz) {
 		return DBTool.getAllAccessor(clazz);
 	}
 }
