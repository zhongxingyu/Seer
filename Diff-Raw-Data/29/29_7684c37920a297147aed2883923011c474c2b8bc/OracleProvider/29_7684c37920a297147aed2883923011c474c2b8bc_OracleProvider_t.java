 package edu.thu.keg.mdap_impl.provider;
 
 import java.sql.ResultSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.Query.OrderClause;
 import edu.thu.keg.mdap.datamodel.Query.WhereClause;
 import edu.thu.keg.mdap.provider.DataProviderException;
 
 public class OracleProvider extends JdbcProvider {
 
 	public OracleProvider(String connString) {
 		super(connString);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public String getQueryString(Query q) {
 		return this.getQueryString(q, 0);
 	}

	//
 	@Override
 	public void openQuery(Query query) throws DataProviderException {
 		if (!results.containsKey(query)) {
 			String sql = getQueryString(query, 0);
 			ResultSet rs = executeQuery(sql);
 			results.put(query, rs);
 		}
 	}
 
 	private String getQueryString(Query q, int level) {
 		HashMap<Query, String> aliasMap = new HashMap<Query, String>();
 		if (q.getInnerQuery() != null) {
 			String alias = "t_" + level;
 			aliasMap.put(q.getInnerQuery(), alias);
 		}
 
 		if (q.getJoinOnClause() != null) {
 			String alias = "tj_" + level;
 			aliasMap.put(q.getJoinOnClause().getQuery(), alias);
 		}
 
 		DataField[] fields = q.getFields();
 		StringBuffer sb = new StringBuffer("SELECT ");
 
 		for (int i = 0; i < fields.length - 1; i++) {
 			DataField df = fields[i];
 			if (q.getInnerQuery() == null) {
 				sb.append(df.getQueryName()).append(" AS ")
 						.append(df.getName()).append(",");
 			} else {
 				sb.append(super.getFieldAliasName(df, aliasMap)).append(" AS ")
 						.append(df.getName()).append(",");
 			}
 		}
 		DataField df = fields[fields.length - 1];
 		if (q.getInnerQuery() == null) {
 			sb.append(df.getQueryName()).append(" AS ").append(df.getName());
 		} else {
 			sb.append(super.getFieldAliasName(df, aliasMap)).append(" AS ")
 					.append(df.getName());
 		}
 
 		if (q.getInnerQuery() == null)
 			sb.append(" FROM ").append(fields[0].getDataSet().getName());
 		else {
 
 			sb.append(" FROM ( ")
 					.append(getQueryString(q.getInnerQuery(), level + 1))
 					.append(" ) ").append(aliasMap.get(q.getInnerQuery()));
 		}
 		if (q.getJoinOnClause() != null) {
 			sb.append(" INNER JOIN (")
 					.append(getQueryString(q.getJoinOnClause().getQuery(),
 							level + 1)).append(") AS ")
 					.append(aliasMap.get(q.getJoinOnClause().getQuery()))
 					.append(" ON ");
 			for (Entry<DataField, DataField> fs : q.getJoinOnClause().getOns()
 					.entrySet()) {
 				sb.append(super.getFieldAliasName(fs.getKey(), aliasMap))
 						.append("=")
 						.append(super.getFieldAliasName(fs.getValue(), aliasMap))
 						.append(" AND ");
 			}
 			sb.delete(sb.length() - 4, sb.length());
 		}
 
 		List<WhereClause> wheres = q.getWhereClauses();
 		if (wheres != null && wheres.size() > 0) {
			sb.append(" WHERE ");
 			sb.append(super.whereToSB(wheres, aliasMap));
			sb.append("and rownum < 1000 ");
 		} else
 			sb.append(" WHERE rownum < 1000 ");
 
 		List<DataField> gb = q.getGroupByFields();
 		if (gb != null && gb.size() > 0) {
 			sb.append(" GROUP BY ");
 			for (int i = 0; i < gb.size() - 1; i++) {
 				sb.append(gb.get(i).getName()).append(", ");
 			}
 			sb.append(gb.get(gb.size() - 1).getName());
 		}
 
 		if (level == 0) {
 			List<OrderClause> orders = q.getOrderClauses();
 			if (orders != null && orders.size() > 0) {
 				sb.append(" ORDER BY ");
 				for (int i = 0; i < orders.size() - 1; i++) {
 					OrderClause order = orders.get(i);
 					sb.append(
 							super.getFieldAliasName(order.getField(), aliasMap))
 							.append(" ").append(order.getOrder().toString());
 					sb.append(", ");
 				}
 				sb.append(
 						super.getFieldAliasName(orders.get(orders.size() - 1)
 								.getField(), aliasMap))
 						.append(" ")
 						.append(orders.get(orders.size() - 1).getOrder()
 								.toString());
 			}
 		}
 
 		return sb.toString();
 	}
 }
