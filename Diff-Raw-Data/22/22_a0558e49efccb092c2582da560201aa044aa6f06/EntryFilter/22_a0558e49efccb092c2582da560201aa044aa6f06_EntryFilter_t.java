 package net.tailriver.agoraguide;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class EntryFilter implements Cloneable {
 	private Collection<EntrySummary> collection;
 	private boolean removeMatched;
 
 	public EntryFilter(Collection<? extends EntrySummary> collection) {
 		this.collection = new HashSet<EntrySummary>(collection);
 		this.removeMatched = false;
 	}
 
 	/** remain matched */
 	public void toPositiveFilter() {
 		removeMatched = false;
 	}
 
 	/** remain NOT matched */
 	public void toNegativeFilter() {
 		removeMatched = true;
 	}
 
 	public void add(EntryFilter anotherFilter) {
 		collection.addAll(anotherFilter.collection);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void applyFilter(Collection<?> filter) {
 		if (filter == null || filter.size() == 0) {
 			applyEntryFilter(Collections.EMPTY_SET);
 			return;
 		}
		if (filter.contains(null)) {
			throw new IllegalArgumentException("filter contains null");
		}
 		Object sample = filter.iterator().next();
 
 		if (sample instanceof Area) {
 			applyAreaFilter((Collection<Area>) filter);
 		} else if (sample instanceof Category) {
 			applyCategoryFilter((Collection<Category>) filter);
 		} else if (sample instanceof EntrySummary) {
 			applyEntryFilter((Collection<EntrySummary>) filter);
 		} else {
 			throw new UnsupportedOperationException(sample.getClass() + " is not supported");
 		}
 	}
 
 	/** id, title, sponsor search */
 	public void applyFilter(String keyword) {
 		if (keyword == null || keyword.length() == 0) {
 			return;
 		}
 
 		SQLiteDatabase database = AgoraActivity.getDatabase();
 		SQLLike like = new SQLLike("title", "sponsor", "cosponsor", "abstract", "content", "guest");
 		String table = "entry";
 		String[] columns = { "id" };
 		String selection = like.toString();
 		String[] selectionArgs = new String[like.size()];
 		Arrays.fill(selectionArgs, "%" + keyword + "%");
 		Cursor c = database.query(table, columns, selection, selectionArgs, null, null, null);
 
 		Collection<EntrySummary> matched = new HashSet<EntrySummary>();
 		c.moveToFirst();
 		for (int i = 0, max = c.getCount(); i < max; i++) {
 			matched.add(EntrySummary.get(c.getString(0)));
 			c.moveToNext();
 		}
 		c.close();
 
 		if (EntrySummary.get(keyword) != null) {
 			matched.add(EntrySummary.get(keyword));
 		}
 		applyEntryFilter(matched);
 	}
 
 	public List<EntrySummary> getResult() {
 		List<EntrySummary> list = new ArrayList<EntrySummary>(collection);
 		Collections.sort(list);
 		return list;
 	}
 
 	@Override
 	public EntryFilter clone() {
 		try {
 			EntryFilter c = (EntryFilter) super.clone();
 			c.collection = new HashSet<EntrySummary>(collection);
 			return c;
 		} catch (CloneNotSupportedException e) {
 			return null;
 		}
 	}
 
 	private void applyAreaFilter(Collection<Area> filter) {
 		Iterator<? extends EntrySummary> it = collection.iterator();
 		while (it.hasNext()) {
 			EntrySummary s = it.next();
 			if (filter.contains(s.getArea()) == removeMatched) {
 				it.remove();
 			}
 		}
 	}
 
 	private void applyCategoryFilter(Collection<Category> filter) {
 		Iterator<? extends EntrySummary> it = collection.iterator();
 		while (it.hasNext()) {
 			EntrySummary s = it.next();
 			if (filter.contains(s.getCategory()) == removeMatched) {
 				it.remove();
 			}
 		}
 	}
 
 	private void applyEntryFilter(Collection<EntrySummary> filter) {
 		if (removeMatched) {
 			collection.removeAll(filter);
 		} else {
 			collection.retainAll(filter);
 		}
 	}
 
 	private class SQLLike {
 		String[] args;
 		public SQLLike(String... args) {
 			this.args = args;
 		}
 
 		public int size() {
 			return args.length;
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < args.length; i++) {
 				sb.append(args[i]).append(" LIKE ? OR ");
 			}
 			return sb.delete(sb.length() - " OR ".length(), sb.length()).toString();
 		}
 	}
 }
