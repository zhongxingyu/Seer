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
 
 package at.molindo.elastic.compass;
 
 import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.compass.core.Property;
 import org.compass.core.Resource;
 import org.compass.core.engine.SearchEngineException;
 import org.compass.core.engine.SearchEngineHits;
 import org.compass.core.mapping.Mapping;
 import org.compass.core.mapping.ResourceMapping;
 import org.compass.core.mapping.ResourcePropertyMapping;
 import org.compass.core.mapping.osem.AbstractCollectionMapping;
 import org.compass.core.spi.ResourceKey;
 import org.elasticsearch.action.ActionListener;
 import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
 import org.elasticsearch.action.bulk.BulkResponse;
 import org.elasticsearch.action.delete.DeleteResponse;
 import org.elasticsearch.action.get.GetField;
 import org.elasticsearch.action.get.GetResponse;
 import org.elasticsearch.action.index.IndexResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
 import org.elasticsearch.client.action.delete.DeleteRequestBuilder;
 import org.elasticsearch.client.action.index.IndexRequestBuilder;
 import org.elasticsearch.client.action.search.SearchRequestBuilder;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.SearchHitField;
 import org.elasticsearch.search.sort.SortBuilder;
 import org.elasticsearch.search.sort.SortBuilders;
 import org.elasticsearch.search.sort.SortOrder;
 
 import at.molindo.elastic.query.InQuery;
 import at.molindo.elastic.query.SortField;
 import at.molindo.utils.collections.ArrayUtils;
 
 public class ElasticClient {
 
 	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
 			.getLogger(ElasticClient.class);
 
 	private final ElasticSearchEngineFactory _searchEngineFactory;
 	private final ElasticIndex _index;
 	private final String _indexName;
 	private final Client _client;
 	private final Map<String, String[]> _typeFields;
 
 	public ElasticClient(ElasticSearchEngineFactory searchEngineFactory, ElasticIndex index, Client client) {
 		if (searchEngineFactory == null) {
 			throw new NullPointerException("searchEngineFactory");
 		}
 		if (index == null) {
 			throw new NullPointerException("index");
 		}
 		if (client == null) {
 			throw new NullPointerException("client");
 		}
 
 		_searchEngineFactory = searchEngineFactory;
 		_index = index;
 		_indexName = index.getAlias(); // always use alias
 		_client = client;
 
 		_typeFields = new HashMap<String, String[]>();
 
 		for (String type : _index.getTypes()) {
 			Set<String> fields = _index.getFieldMapping(type).keySet();
 			_typeFields.put(type, fields.toArray(new String[fields.size()]));
 		}
 
 	}
 
 	public void create(final ElasticResource resource) {
 		try {
 			IndexRequestBuilder index = _client
 					.prepareIndex(_indexName, resource.getAlias(), resource.getId())
 					.setSource(toXContentBuilder(resource));
 
 			if (_index.getSettings().isAsyncWrite()) {
 				index.execute(new ActionListener<IndexResponse>() {
 
 					@Override
 					public void onResponse(IndexResponse response) {
 						if (log.isTraceEnabled()) {
 							log.trace("created id " + resource.getAlias() + "#" + response.getId());
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e) {
 						log.warn("failed to create " + resource.getAlias() + "#" + resource.getId(), e);
 					}
 				});
 			} else {
 				index.setOperationThreaded(false).execute().actionGet();
 			}
 		} catch (IOException e) {
 			throw new SearchEngineException("failed to create resource", e);
 		}
 	}
 
 	public void update(final ElasticResource resource) {
 		try {
 			IndexRequestBuilder index = _client
 					.prepareIndex(_indexName, resource.getAlias(), resource.getId())
 					.setSource(toXContentBuilder(resource));
 
 			if (_index.getSettings().isAsyncWrite()) {
 				index.execute(new ActionListener<IndexResponse>() {
 
 					@Override
 					public void onResponse(IndexResponse response) {
 						if (log.isTraceEnabled()) {
 							log.trace("updated id " + resource.getAlias() + "#" + response.getId());
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e) {
 						log.warn("failed to update " + resource.getAlias() + "#" + resource.getId(), e);
 					}
 				});
 			} else {
 				index.setOperationThreaded(false).execute().actionGet();
 			}
 		} catch (IOException e) {
 			throw new SearchEngineException("failed to create resource", e);
 		}
 	}
 
 	public Resource[] get(ResourceKey key) {
 		Property[] ids = key.getIds();
 		if (ids == null || ids.length == 0) {
 			return ElasticResource.NO_RESOURCES;
 		}
 		Resource[] resources = new Resource[ids.length];
 
 		if (ids.length > 1) {
 			Map<String, Integer> idStrings = new HashMap<String, Integer>();
 			for (int i = 0; i < ids.length; i++) {
 				idStrings.put(ids[i].getStringValue(), i);
 			}
 
 			InQuery inQuery = new InQuery("_id", idStrings.keySet()
 					.toArray(new String[idStrings.size()]));
 			ElasticSearchEngineQuery query = new ElasticSearchEngineQuery(_searchEngineFactory, inQuery)
 					.setAlias(key.getAlias());
 
 			SearchEngineHits hits = find(query);
 			for (int i = 0; i < hits.getLength(); i++) {
 				Resource r = hits.getResource(i);
 				resources[idStrings.get(r.getId())] = r;
 			}
 		} else {
 			String[] fields = _typeFields.get(key.getAlias());
 			if (fields == null) {
 				throw new SearchEngineException("unknown alias " + key.getAlias());
 			}
 
 			GetResponse response = _client
 					.prepareGet(_indexName, key.getAlias(), ids[0].getStringValue())
 					.setFields(fields).execute().actionGet();
 
			if (response.getFields() != null) {
 				ElasticResource resource = new ElasticResource(response.getType(), _searchEngineFactory);
 				ResourceMapping mapping = _searchEngineFactory.getMapping()
 						.getRootMappingByAlias(key.getAlias());
 
 				for (Map.Entry<String, GetField> e : response.getFields().entrySet()) {
 					resource.addProperties(toProperties(mapping, e.getKey(), e.getValue()
 							.getValues()));
 				}
 
 				resources[0] = resource;
 			}
 		}
 
 		return resources;
 	}
 
 	public void delete(final ResourceKey key) {
 		Property[] ids = key.getIds();
 
 		if (ArrayUtils.empty(ids)) {
 			return;
 		} else if (ids.length == 1) {
 			// simple delete
 			DeleteRequestBuilder delete = _client.prepareDelete(_indexName, key.getAlias(), ids[0]
 					.getStringValue());
 			if (_index.getSettings().isAsyncWrite()) {
 				delete.execute(new ActionListener<DeleteResponse>() {
 
 					@Override
 					public void onResponse(DeleteResponse response) {
 						if (log.isTraceEnabled()) {
 							log.trace("deleted id " + key.getIds()[0] + " of type "
 									+ key.getAlias());
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e) {
 						log.warn("failed to delete id " + key.getIds()[0] + " of type "
 								+ key.getAlias(), e);
 					}
 				});
 			} else {
 				delete.setOperationThreaded(false).execute().actionGet();
 			}
 		} else {
 			// bulk delete
 			BulkRequestBuilder bulk = _client.prepareBulk();
 			for (Property id : ids) {
 				bulk.add(_client.prepareDelete(_indexName, key.getAlias(), id.getStringValue())
 						.setOperationThreaded(_index.getSettings().isAsyncWrite()));
 			}
 			if (_index.getSettings().isAsyncWrite()) {
 				bulk.execute(new ActionListener<BulkResponse>() {
 
 					@Override
 					public void onResponse(BulkResponse response) {
 						if (log.isTraceEnabled()) {
 							log.trace("deleted ids " + Arrays.toString(key.getIds()) + " of type "
 									+ key.getAlias());
 						}
 					}
 
 					@Override
 					public void onFailure(Throwable e) {
 						log.warn("failed to delete ids " + Arrays.toString(key.getIds())
 								+ " of type " + key.getAlias(), e);
 					}
 				});
 			} else {
 				bulk.execute().actionGet();
 			}
 		}
 	}
 
 	protected XContentBuilder toXContentBuilder(ElasticResource resource) throws IOException {
 		XContentBuilder builder = jsonBuilder().startObject();
 
 		for (Property property : resource.getProperties()) {
 			builder.field(property.getName(), property.getStringValue());
 		}
 
 		return builder.endObject();
 	}
 
 	public SearchEngineHits find(ElasticSearchEngineQuery query) throws SearchEngineException {
 
 		SearchRequestBuilder search = _client.prepareSearch(_indexName).setQuery(query.getQuery()
 				.getBuilder());
 
 		String[] aliases = toAliases(query);
 
 		search.setTypes(aliases);
 
 		// fields
 		String[] fields;
 		if (aliases.length == 1) {
 			String alias = aliases[0];
 			fields = _typeFields.get(alias);
 			if (fields == null) {
 				throw new SearchEngineException("unknown alias: '" + alias + "'");
 			}
 		} else {
 			HashSet<String> fieldSet = new HashSet<String>();
 			for (String alias : aliases) {
 				String[] aliasFields = _typeFields.get(alias);
 				if (aliasFields == null) {
 					throw new SearchEngineException("unknown alias: '" + alias + "'");
 				}
 				fieldSet.addAll(Arrays.asList(aliasFields));
 			}
 			fields = fieldSet.toArray(new String[fieldSet.size()]);
 		}
 		search.addFields(fields);
 
 		for (SortField sort : query.getSorts()) {
 
 			SortBuilder builder;
 			switch (sort.getType()) {
 			case FIELD:
 				builder = SortBuilders.fieldSort(sort.getProperty());
 				break;
 			case DOC:
 				builder = SortBuilders.fieldSort("_id");
 				break;
 			case SCORE:
 				builder = SortBuilders.scoreSort();
 				break;
 			case DISTANCE:
 				builder = SortBuilders.geoDistanceSort(sort.getProperty());
 			default:
 				throw new SearchEngineException("unknown SortType " + sort.getType());
 			}
 
 			builder.order(toOrder(sort.isReverse()));
 			search.addSort(builder);
 		}
 
 		return new ElasticSearchEngineHits(this, search.execute().actionGet().hits());
 	}
 
 	private SortOrder toOrder(boolean reverse) {
 		return reverse ? SortOrder.DESC : SortOrder.ASC;
 	}
 
 	public void delete(ElasticSearchEngineQuery query) {
 		_client.prepareDeleteByQuery(_indexName).setQuery(query.getQuery().getBuilder())
 				.setTypes(toAliases(query)).execute();
 	}
 
 	private String[] toAliases(ElasticSearchEngineQuery query) {
 		String[] aliases = query.getAliases();
 		if (ArrayUtils.empty(aliases)) {
 			aliases = _typeFields.keySet().toArray(new String[_typeFields.size()]);
 		}
 		return aliases;
 	}
 
 	public ElasticResource toResource(SearchHit hit) {
 		String alias = hit.getType();
 
 		ResourceMapping mapping = _searchEngineFactory.getMapping().getRootMappingByAlias(alias);
 		ElasticResource resource = new ElasticResource(hit.getType(), _searchEngineFactory);
 		for (Map.Entry<String, SearchHitField> e : hit.getFields().entrySet()) {
 			resource.addProperties(toProperties(mapping, e.getKey(), e.getValue().getValues()));
 		}
 
 		return resource;
 	}
 
 	private Property[] toProperties(ResourceMapping mapping, String name, Collection<?> values) {
 		Mapping m = _index.getFieldMapping(mapping.getAlias()).get(name);
 		if (m == null) {
 			throw new SearchEngineException("No resource property mapping is defined for alias ["
 					+ mapping.getAlias() + "] and resource property [" + name + "]");
 		}
 
 		Property[] properties = new ElasticProperty[values.size()];
 		int i = 0;
 		for (Object value : values) {
 			Property property;
 			if (m instanceof ResourcePropertyMapping) {
 				ResourcePropertyMapping propertyMapping = (ResourcePropertyMapping) m;
 				property = _searchEngineFactory.getResourceFactory()
 						.createProperty((String) value, propertyMapping);
 				property.setBoost(propertyMapping.getBoost());
 			} else if (m instanceof AbstractCollectionMapping) {
 				// col size
 				property = _searchEngineFactory
 						.getResourceFactory()
 						.createProperty(name, (String) value, Property.Store.YES, Property.Index.NOT_ANALYZED);
 			} else {
 				throw new SearchEngineException("unexpected mapping type " + m);
 			}
 			properties[i++] = property;
 		}
 
 		return properties;
 	}
 
 	public String[] findPropertyValues(String propertyName) {
 		// TODO use facetted search?
 		throw new NotImplementedException();
 	}
 
 	public void verifyIndex() {
 		_index.verifyIndex();
 	}
 
 	public void deleteIndex() {
 		// TODO lock
 		_index.deleteIndex();
 	}
 
 	public void refresh() {
 		_client.admin().indices().prepareRefresh(_index.getAlias()).execute().actionGet();
 	}
 
 	public List<AnalyzeToken> analyze(String analyzer, String text) {
 		return _client.admin().indices().prepareAnalyze(_index.getAlias(), text)
 				.setAnalyzer(analyzer).execute().actionGet().getTokens();
 	}
 }
