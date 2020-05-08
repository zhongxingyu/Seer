 /*
  * QueryPagingSetup.java created on 2010-06-06
  *
  * Created by Brushing Bits Labs
  * http://www.brushingbits.org
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
 package org.brushingbits.jnap.persistence.hibernate;
 
 import java.text.MessageFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.brushingbits.jnap.bean.paging.PagingSetup;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 
 /**
  * <p>A {@link PagingSetup} adapter for {@code HQL} queries.</p>
  * 
  * <p>ps: Many thanks to <a href="mailto:antonio.chaul@gmail.com">Antonio Chaul</a> for his 
  * contribution on the {@link #ORDER_BY_REMOVE_REGEXP}. I was about to give up when he came up
  * with the working regular expression, now I owe him a beer.</p>
  * 
  * @author Daniel Rochetti
  * @since 1.0
  */
 public class QueryPagingSetup implements PagingSetup {
 
 	private static final String REG_EXP_SQL_IDENTIFIER = "[\\w\\d\\.\\(\\)]";
 
 	private static final String REG_EXP_SQL_ORDER_DIRECTION = "(?:asc|desc)";
 
 	private static final Pattern ORDER_BY_REMOVE_REGEXP = Pattern.compile(MessageFormat.format(
			"(?:order[\\s]*by)(?:[\\s]*{0}+[\\s]*{1}?[\\s]*)(?:,[\\s]*{0}+[\\s]*{1}?[\\s]*)*",
 			REG_EXP_SQL_IDENTIFIER, REG_EXP_SQL_ORDER_DIRECTION),
 			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 
 	private Query query;
 	private Session currentSession;
 	private Object queryParams;
 
 	public QueryPagingSetup(Query query, Session currentSession, Object queryParams) {
 		this.query = query;
 		this.currentSession = currentSession;
 		this.queryParams = queryParams;
 	}
 
 	public void setFirstResult(int first) {
 		this.query.setFirstResult(first);
 	}
 
 	public void setResultsPerPage(int max) {
 		this.query.setMaxResults(max);
 	}
 
 	public int countTotal() {
 		// build the count query
 		String countHql = query.getQueryString();
 		int indexOfFromClause = countHql.toLowerCase().indexOf("from");
 		countHql = countHql.substring(indexOfFromClause, countHql.length());
 		countHql = "select count(*) " + countHql;
 
 		// remove 'order by' clauses if present
 		Matcher orderByMatcher = ORDER_BY_REMOVE_REGEXP.matcher(countHql);
 		countHql = orderByMatcher.replaceAll(StringUtils.EMPTY);
 
 		Query countQuery = this.currentSession.createQuery(countHql);
 		QueryUtils.setParameters(countQuery, this.queryParams);
 		return ((Number) countQuery.uniqueResult()).intValue();
 	}
 
 }
