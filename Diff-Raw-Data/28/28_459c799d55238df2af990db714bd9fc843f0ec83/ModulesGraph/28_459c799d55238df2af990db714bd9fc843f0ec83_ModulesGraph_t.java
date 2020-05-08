 package pl.kaczanowski.model;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import org.testng.collections.Maps;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.ImmutableTable;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.collect.Table;
 import com.google.common.collect.TreeBasedTable;
 
 public class ModulesGraph {
 
 	public static class Task implements Comparable<Task> {
 
 		public static final Predicate<? super Task> IS_TASK_NOT_ENDED = new Predicate<Task>() {
 			@Override
 			public boolean apply(@Nullable final Task input) {
 				return input != null && !input.isEnded();
 			}
 		};
 
 		// TODO do metod
 		public static final Predicate<? super Task> IS_TASK_ENDED = Predicates.not(IS_TASK_NOT_ENDED);
 
 		private final int id;
 
 		private boolean ended = false;
 
 		private final Integer cost;
 
 		private Collection<Task> parentTasks;
 
 		public Task(final int id, final Integer cost) {
 			this.id = id;
 			this.cost = cost;
 		}
 
 		@Override
 		public int compareTo(final Task o) {
 			return ComparisonChain.start().compare(id, o.id).compare(cost, o.cost).result();
 		}
 
 		public void end() {
 			this.ended = true;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (getClass() != obj.getClass()) {
 				return false;
 			}
 			Task other = (Task) obj;
 			if (cost == null) {
 				if (other.cost != null) {
 					return false;
 				}
 			} else if (!cost.equals(other.cost)) {
 				return false;
 			}
 			if (id != other.id) {
 				return false;
 			}
 			return true;
 		}
 
 		public Integer getCost() {
 			return cost;
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public Collection<Task> getParentTasks() {
 			return parentTasks;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((cost == null) ? 0 : cost.hashCode());
 			result = prime * result + id;
 			return result;
 		}
 
 		public boolean isEnded() {
 			return ended;
 		}
 
 		private void setParentTasks(final Collection<Task> parentTasks) {
 			this.parentTasks = ImmutableSet.copyOf(parentTasks);
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder builder = new StringBuilder();
 			builder.append("Task [id=").append(id).append(", ended=").append(ended).append(", cost=").append(cost)
					.append("]");
 			return builder.toString();
 		}
 
 	}
 
 	private final String name;
 
 	private final Integer[] taskCosts;
 
 	private final Table<Integer, Integer, Integer> taskChildrenWeight;
 
 	private final Map<Integer, Set<Integer>> previousTasks;
 
 	public ModulesGraph(final String name, final Integer[] taskCosts, final Integer[][] taskConnections) {
 		this.name = name;
 		this.taskCosts = taskCosts;
 		taskChildrenWeight = ImmutableTable.copyOf(getTaskChildrenWeight(taskConnections));
 		previousTasks = ImmutableMap.copyOf(getPreviousTasks(taskConnections));
 	}
 
 	public int getChangeTime(final Integer taskId, final Integer childTaskId) {
 		return taskChildrenWeight.get(taskId, childTaskId);
 	}
 
 	public Set<Integer> getChildren(final Integer taskId) {
 		return taskChildrenWeight.row(taskId).keySet();
 	}
 
 	public int getCost(final Integer taskId) {
 		return taskCosts[taskId];
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	private Map<Integer, Set<Integer>> getPreviousTasks(final Integer[][] taskConnections) {
 
 		Map<Integer, Set<Integer>> result = Maps.newHashMap();
 
 		for (int row = 0; row < taskConnections.length; row++) {
 
 			Set<Integer> previousTasksSet = Sets.newTreeSet();
 			result.put(row, previousTasksSet);
 		}
 
 		for (int row = 0; row < taskConnections.length; row++) {
 			Integer[] singleTaskConnections = taskConnections[row];
 			for (int col = 0; col < singleTaskConnections.length; col++) {
 				if (singleTaskConnections[col] != null) {
 					result.get(col).add(row);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param taskConnections
 	 * @return table - row is task, column is children task and value is weight
 	 */
 	private Table<Integer, Integer, Integer> getTaskChildrenWeight(final Integer[][] taskConnections) {
 		Table<Integer, Integer, Integer> result = TreeBasedTable.create();
 
 		for (int i = 0; i < taskConnections.length; i++) {
 			Integer[] singleTaskConnections = taskConnections[i];
 			for (int j = 0; j < singleTaskConnections.length; j++) {
 				if (singleTaskConnections[j] != null) {
 					result.put(i, j, singleTaskConnections[j]);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public List<Task> getTasksCopy() {
 
 		List<Task> result = Lists.newArrayListWithCapacity(taskCosts.length);
 
 		for (int i = 0; i < taskCosts.length; i++) {
 			result.add(new Task(i, taskCosts[i]));
 		}
 
 		for (Task task : result) {
 			Set<Task> parentTasks = Sets.newHashSet();
 			for (Integer parentTaskId : previousTasks.get(task.getId())) {
 				parentTasks.add(result.get(parentTaskId));
 			}
 			task.setParentTasks(parentTasks);
 		}
 
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return "ModulesGraph [name=" + name + ", taskCosts=" + Arrays.toString(taskCosts) + ", taskChildrenWeight="
 				+ taskChildrenWeight + "]";
 	}
 
 }
