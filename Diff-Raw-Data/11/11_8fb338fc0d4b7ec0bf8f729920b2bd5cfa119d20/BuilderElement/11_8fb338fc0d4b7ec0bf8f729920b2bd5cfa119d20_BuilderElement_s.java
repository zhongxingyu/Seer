 package jp.gr.java_conf.sqlutils.core.builder;
 
 import jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.IConditionColumn;
 import jp.gr.java_conf.sqlutils.core.builder.ConditionBuilder.IConditionElement;
 import jp.gr.java_conf.sqlutils.core.dto.IColumn;
 import jp.gr.java_conf.sqlutils.core.dto.ITable;
 
 public class BuilderElement {
 
 	/**
 	 * {@link QueryBuilder#from(ITblElement)}に指定できるテーブル定義型である事を示す。
 	 */
 	public interface ITblElement {
 	}
 
 	static class TblElement implements ITblElement {
 		ITable tbl;
 		String alias;
 		TblElement(ITable tbl) {
 			this.tbl = tbl;
 		}
 		TblElement(ITable tbl, String alias) {
 			this.tbl = tbl;
 			this.alias = alias;
 		}
 		public boolean hasAlias() {
 			return alias != null;
 		}
 		public String name(boolean appendSchema) {
 			if (alias != null)
 				return alias;
 			return tbl.name(appendSchema);
 		}
 		public String aliasExpression(boolean appendSchema) {
 			if (alias == null)
 				return tbl.name(appendSchema);
 //			return tbl.name() + " as " + alias; // Oracle10gはas句に対応しない。一般的に無くても良いっぽい
 			return tbl.name(appendSchema) + " " + alias;
 		}
 		public ISelectColumn<?>[] getCols() {
 			if (alias == null)
 				return tbl.getCols();
 			else {
 				IColumn<?>[] cols = tbl.getCols();
 				ISelectColumn<?>[] ret = new ColElement<?>[cols.length];
 				for (int i = 0; i < cols.length; i++) {
 					@SuppressWarnings({ "unchecked", "rawtypes" })
 					ColElement<?> c = new ColElement(cols[i]);
 					c.tblAlias = alias;
 					ret[i] = c;
 				}
 				return ret;
 			}
 		}
 		static TblElement create(ITblElement tbl) {
 			if (tbl instanceof TblElement)
 				return (TblElement)tbl;
 			else
 				return new TblElement((ITable)tbl);
 		}
 	}
 
 
 
	interface ISelectable {
 	}
 
 	/**
 	 * {@link QueryBuilder#select(ISelectable...)}に指定できるカラム定義型である事を示す。
 	 */
 	public interface ISelectColumn<T> extends ISelectable {
 		String fullname(boolean appendSchema);
 	}
 
 	/**
 	 * {@link QueryBuilder#orderBy(IOrderElement...)}に指定できるカラム定義型である事を示す。
 	 */
 	public interface IOrderColumn {
 		String fullname(boolean appendSchema);
 	}
 
 	/**
 	 * {@link QueryBuilder#groupBy(IGroupByColumn...)}に指定できるカラム定義型である事を示す。
 	 */
 	public interface IGroupByColumn {
 		String fullname(boolean appendSchema);
 	}
 
 	/**
 	 * {@link QueryBuilder#orderBy(IOrderElement...)}に指定できるカラム定義型である事を示す。
 	 */
 	public interface IOrderElement {
 		public String toQuery(boolean appendSchema);
 	}
 
	interface IAliasSelectable {
 		String fullname(boolean appendSchema);
 		String alias();
 	}
 
 	static class ColElement<T>
 	implements ISelectColumn<T>, IConditionColumn<T>, IOrderColumn, IGroupByColumn, IAliasSelectable {
 		String tblAlias;
 		IColumn<T> col;
 		String alias;
 		ColElement(IColumn<T> col) {
 			this.col = col;
 		}
 		ColElement(String tblAlias, IColumn<T> col) {
 			this.tblAlias = tblAlias;
 			this.col = col;
 		}
 		ColElement(IColumn<T> col, String alias) {
 			this.col = col;
 			this.alias = alias;
 		}
 		@Override
 		public String fullname(boolean appendSchema) {
 			if (tblAlias == null)
 				return col.fullname(appendSchema);
 			else
 				return tblAlias + "." + col.name();
 		}
 		@Override
 		public String alias() {
 			return alias;
 		}
 	}
 
 	static class SelectFunc implements ISelectable, IAliasSelectable {
 		String expression;
 		ISelectColumn<?> col;
 		String alias;
 		SelectFunc(String expression, ISelectColumn<?> col) {
 			this.expression = expression;
 			this.col = col;
 		}
 		@Override
 		public String fullname(boolean appendSchema) {
 			if (col == null)
 				return expression;
 			else
 				return String.format(expression, col.fullname(appendSchema));
 		}
 		@Override
 		public String alias() {
 			return alias;
 		}
 	}
 
 	static class OrderElement implements IOrderElement {
 		private IOrderColumn col;
 		private boolean isAsc;
 		OrderElement(IOrderColumn col, boolean isAsc) {
 			this.isAsc = isAsc;
 			this.col = col;
 		}
 		public String toQuery(boolean appendSchema) {
 			return col.fullname(appendSchema) + (isAsc ? " asc" : " desc");
 		}
 	}
 
 	static class RawOrderElement implements IOrderElement {
 		private String name;
 		private boolean isAsc;
 		RawOrderElement(String name, boolean isAsc) {
 			this.isAsc = isAsc;
 			this.name = name;
 		}
 		public String toQuery(boolean appendSchema) {
 			return name + (isAsc ? " asc" : " desc");
 		}
 	}
 
 	static class JoinedTbl {
 		enum JoinType {INNER, LEFT_OUTER}
 		JoinType type;
 		TblElement tbl;
 		IConditionElement on;
 		JoinedTbl(JoinType type, TblElement joinedTbl, IConditionElement on) {
 			this.type = type;
 			this.tbl = joinedTbl;
 			this.on = on;
 		}
 		boolean hasAlias() {
 			return tbl.hasAlias();
 		}
 		String toQuery(boolean appendSchema) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(type == JoinType.INNER ? " inner" : " left outer").append(" join ");
 			sb.append(tbl.aliasExpression(appendSchema)).append(" on ").append(on.toQuery(appendSchema));
 			return sb.toString();
 		}
 	}
 
 }
