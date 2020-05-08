 package org.greatage.db.gae;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Query;
 import org.greatage.db.ChangeSetBuilder;
 import org.greatage.util.DescriptionBuilder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class GAEUpdate extends GAEChange implements ChangeSetBuilder.UpdateBuilder, GAEConditional, GAESettable {
 	private final Map<String, Query> setters = new HashMap<String, Query>();
 
 	private final Entity entity;
 	private final Query query;
 
 	GAEUpdate(final GAEChangeSet changeSet, final String entityName) {
 		super(changeSet);
 		this.entity = new Entity(entityName);
 		this.query = new Query(entityName);
 	}
 
 	public ChangeSetBuilder.UpdateBuilder set(final String propertyName, final Object value) {
 		entity.setProperty(propertyName, value);
 		return this;
 	}
 
 	public ChangeSetBuilder.SetBuilder<ChangeSetBuilder.UpdateBuilder> setFrom(final String propertyName, final String entityName) {
 		return new GAESet<ChangeSetBuilder.UpdateBuilder>(this, propertyName, entityName);
 	}
 
 	public ChangeSetBuilder.ConditionEntryBuilder<ChangeSetBuilder.UpdateBuilder> where(final String property) {
 		return new GAECondition<ChangeSetBuilder.UpdateBuilder>(this).and(property);
 	}
 
 	public void addCondition(final Query.FilterPredicate condition) {
 		query.addFilter(condition.getPropertyName(), condition.getOperator(), condition.getValue());
 	}
 
 	public void addSelect(final String name, final Query query) {
 		setters.put(name, query);
 	}
 
 	public Object doInDataStore(final DatastoreService dataStore) {
 		for (Map.Entry<String, Query> entry : setters.entrySet()) {
 			final List<Key> keys = new ArrayList<Key>();
 			for (Entity selected : dataStore.prepare(entry.getValue()).asIterable()) {
 				keys.add(selected.getKey());
 			}
 			entity.setProperty(entry.getKey(), keys);
 		}
 
 		for (Entity realEntity : dataStore.prepare(query).asIterable()) {
 			realEntity.setPropertiesFrom(entity);
 			dataStore.put(realEntity);
 		}
 
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		final DescriptionBuilder builder = new DescriptionBuilder(getClass());
 		builder.append(query.getKind());
 		for (Map.Entry<String, Object> entry : entity.getProperties().entrySet()) {
 			builder.append(entry.getKey(), entry.getValue());
 		}
 		builder.append("query", query);
		builder.append("setters", setters);
 		return builder.toString();
 	}
 }
