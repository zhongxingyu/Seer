 /**
  * Copyright 2011 Molindo GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package at.molindo.elastic.compass.query;
 
 import org.compass.core.engine.SearchEngineQuery;
 import org.compass.core.engine.SearchEngineQueryBuilder;
 import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQueryStringBuilder;
 import org.elasticsearch.index.query.xcontent.QueryStringQueryBuilder.Operator;
 
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 import at.molindo.elastic.compass.ElasticSearchEngineFactory;
 import at.molindo.elastic.compass.ElasticSearchEngineQuery;
 import at.molindo.elastic.query.StringQuery;
 import at.molindo.utils.data.StringUtils;
 
 public class ElasticSearchEngineQueryStringBuilder implements SearchEngineQueryStringBuilder {
 
 	private ElasticSearchEngineFactory _searchEngineFactory;
 	private String _queryString;
 	private String _analyzer;
 	private String _defaultSearchProperty;
 	private Operator _operator;
 
 	public ElasticSearchEngineQueryStringBuilder(ElasticSearchEngineFactory searchEngineFactory, String queryString) {
 		_searchEngineFactory = searchEngineFactory;
 		_queryString = queryString;
 	}
 
 	@Override
 	public SearchEngineQueryStringBuilder setAnalyzer(String analyzer) {
 		_analyzer = analyzer;
 		return this;
 	}
 
 	@Override
 	public SearchEngineQueryStringBuilder setAnalyzerByAlias(String alias) {
 		setAnalyzer(alias);
 		return this;
 	}
 
 	@Override
 	public SearchEngineQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty) {
 		_defaultSearchProperty = defaultSearchProperty;
 		return this;
 	}
 
 	public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder useAndDefaultOperator() {
 		_operator = Operator.AND;
 		return this;
 	}
 
 	public SearchEngineQueryBuilder.SearchEngineQueryStringBuilder useOrDefaultOperator() {
 		_operator = Operator.OR;
 		return this;
 	}
 
 	@Override
 	public SearchEngineQueryStringBuilder forceAnalyzer() {
 		// TODO heck, what's that doing? :)
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public SearchEngineQueryStringBuilder setQueryParser(String queryParser) {
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public SearchEngineQueryStringBuilder useSpellCheck() {
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public SearchEngineQuery toQuery() {
 		String defaultField = !StringUtils.empty(_defaultSearchProperty) ? _defaultSearchProperty : _searchEngineFactory
 				.getElasticSettings().getDefaultSearchPropery();
 
 		StringQuery query = new StringQuery(_queryString).setAnalyzer(_analyzer).setDefaultOperator(_operator).setDefaultField(defaultField);
 		return new ElasticSearchEngineQuery(_searchEngineFactory, query);
 	}
 }
