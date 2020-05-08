 package ch.uzh.ddis.katts.bolts.join;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ch.uzh.ddis.katts.query.processor.join.JoinConditionConfiguration;
 import ch.uzh.ddis.katts.query.processor.join.SameValueJoinConditionConfiguration;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableList;
 
 /**
  * This condition joins the variable bindings of each stream if they share the same value on a given list of fields.
  * 
  * @author Lorenz Fischer
  * 
  */
 public class SameValueJoinCondition extends AbstractJoinCondition {
 
 	/**
 	 * This set contains the identifiers of all streams this condition works on.
 	 */
 	private Set<String> streamIds;
 
 	/** The join key is a list of all the fields whose values will be checked during the join. */
 	private ImmutableList<String> joinFields;
 
 	/**
 	 * This map contains a multimap for each stream this condition works on. Each multimap contains all the bindings
 	 * that share the same values on all the fields of the join condition. The key of this map is the shared value
 	 * itself.
 	 */
 	private Map<String, HashMultimap<ImmutableList<Object>, SimpleVariableBindings>> joinCache;
 
 	@Override
 	public void prepare(JoinConditionConfiguration configuration, Set<String> streamIds) {
 		SameValueJoinConditionConfiguration castConfiguration;
 
 		if (!(configuration instanceof SameValueJoinConditionConfiguration)) {
 			throw new IllegalStateException("An object of type " + configuration.getClass()
 					+ " cannot be used to configure an object of type " + this.getClass());
 		}
 
 		castConfiguration = (SameValueJoinConditionConfiguration) configuration;
 
 		if (castConfiguration.getJoinFields() == null) {
			throw new IllegalArgumentException("Missing join field 'joinOn' in configuration: " + configuration);
 		}
 
 		this.joinFields = ImmutableList.copyOf(castConfiguration.getJoinFields().split(","));
 		this.joinCache = new HashMap<String, HashMultimap<ImmutableList<Object>, SimpleVariableBindings>>();
 		this.streamIds = streamIds;
 
 		for (String streamId : this.streamIds) { // create a join map per stream
 			HashMultimap<ImmutableList<Object>, SimpleVariableBindings> mapForStream = HashMultimap.create();
 			this.joinCache.put(streamId, mapForStream);
 		}
 	}
 
 	/**
 	 * Builds and returns the key on which the join will be excuted based on the values in <code>bindings</code> and the
 	 * configured list of join fields.
 	 * 
 	 * @param bindings
 	 *            the object containing the variable bindings to use for the key
 	 * @return the key based on the values found in <code>bindings</code>.
 	 */
 	private ImmutableList<Object> buildJoinKey(SimpleVariableBindings bindings) {
 		ImmutableList.Builder<Object> builder = ImmutableList.builder();
 		for (String fieldName : joinFields) {
 			builder.add(bindings.get(fieldName));
 		}
 		return builder.build();
 	}
 
 	@Override
 	public Set<SimpleVariableBindings> join(SimpleVariableBindings newBindings, String fromStreamId) {
 		Set<SimpleVariableBindings> result = AbstractJoinCondition.emptySet;
 		ImmutableList<Object> joinKey = buildJoinKey(newBindings);
 		List<Set<SimpleVariableBindings>> setsToJoin;
 
 		// put the bindings object into the join cache of the stream we received the bindings object on
 		this.joinCache.get(fromStreamId).put(joinKey, newBindings);
 
 		// we store all bindings that need to be joined in this arraylist
 		setsToJoin = new ArrayList<Set<SimpleVariableBindings>>();
 
 		// create the list of streamIds we have to check for matching values
 		for (String streamId : this.streamIds) {
 			if (!streamId.equals(fromStreamId)) { // we only test all other caches
 				HashMultimap<ImmutableList<Object>, SimpleVariableBindings> bindingsForValues = this.joinCache
 						.get(streamId);
 
 				if (!bindingsForValues.containsKey(joinKey)) {
 					// we don't have to check any other cache and can safely abort here.
 					setsToJoin.clear();
 					break;
 				}
 
 				setsToJoin.add(bindingsForValues.get(joinKey));
 			}
 		}
 
 		if (setsToJoin.size() > 0) {
 			result = new HashSet<SimpleVariableBindings>();
 			List<SimpleVariableBindings> cartesianBindings = createCartesianBindings(setsToJoin,
 					AbstractJoinCondition.ignoredBindings);
 			for (SimpleVariableBindings bindings : cartesianBindings) {
 				result.add(merge(newBindings, bindings, AbstractJoinCondition.ignoredBindings));
 			}
 		}
 
 		/*
 		 * only if we found a match in all streams (except the one the new bindings was received on), we return the
 		 * resulting set of new bindings.
 		 */
 		return result;
 	}
 
 	@Override
 	public void removeBindingsFromCache(SimpleVariableBindings bindings, String streamId) {
 		this.joinCache.get(streamId).remove(buildJoinKey(bindings), bindings);
 	}
 
 	@Override
 	public void removeBindingsFromCache(SimpleVariableBindings bindings) {
 		for (String streamId : this.streamIds) { // create a join map per stream
 			removeBindingsFromCache(bindings, streamId);
 		}
 	}
 
 }
