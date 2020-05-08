 package fr.utc.assos.uvweb.adapters;
 
 import android.content.Context;
 import android.util.SparseArray;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.SectionIndexer;
 import android.widget.TextView;
 import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.util.ThreadPreconditionsUtils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * An adapter used all together with the {@link fr.utc.assos.uvweb.ui.UVListFragment}'s ListView.
  * It relies on a standard ViewHolder pattern implemented in the {@link UVwebHolder}
  * class and thus allows UVs recycling.
  * It implements both SectionIndexer and StickyListHeadersAdapter interfaces
  */
 public class UVListAdapter extends UVAdapter implements SectionIndexer, StickyListHeadersAdapter, Filterable {
 	private static final String TAG = makeLogTag(UVListAdapter.class);
 	/**
 	 * Size of the French alphabet. We use it to instantiate our data structures and avoid memory
 	 * reallocation, since we can fairly surely assume we'll have a section for each letter.
 	 */
 	private static final int ALPHABET_LENGTH = 26;
 	/**
 	 * A dummy implementation of the {@link SearchCallbacks} interface that does
 	 * nothing. Used only when this fragment is not attached to an activity.
 	 */
 	private static final SearchCallbacks sDummySearchCallbacks = new SearchCallbacks() {
 		@Override
 		public void onItemsFound(List<UVwebContent.UV> results) {
 		}
 
 		@Override
 		public void onNothingFound() {
 		}
 	};
 	/**
 	 * Custom Filterable interface implementation
 	 */
 	private final UVFilter mFilter = new UVFilter();
 	/**
 	 * Data structures that help us keep track of a comprehensive list of sections and bind these sections
 	 * with the ListView's items' positions
 	 */
 	private final SparseArray<Character> mSectionToPosition;
 	private final List<Character> mComprehensiveSectionsList;
 	/**
 	 * Set of data
 	 */
 	private List<UVwebContent.UV> mUVs = Collections.emptyList();
 	private List<UVwebContent.UV> mSavedUVs = Collections.emptyList();
 	private SearchCallbacks mSearchCallbacks = sDummySearchCallbacks;
 
 	public UVListAdapter(Context context) {
 		super(context);
 
 		mComprehensiveSectionsList = new ArrayList<Character>(ALPHABET_LENGTH);
 		mSectionToPosition = new SparseArray<Character>(ALPHABET_LENGTH);
 	}
 
 	public void setSearchCallbacks(SearchCallbacks callbacks) {
 		mSearchCallbacks = callbacks;
 	}
 
 	public void resetSearchCallbacks() {
 		mSearchCallbacks = sDummySearchCallbacks;
 	}
 
 	@Override
 	public int getCount() {
 		return mUVs.size();
 	}
 
 	public boolean hasUvs() {
 		return mSavedUVs != null && !mSavedUVs.isEmpty();
 	}
 
 	@Override
 	public UVwebContent.UV getItem(int position) {
 		return mUVs.get(position);
 	}
 
 	public void updateUVs(List<UVwebContent.UV> UVs) {
 		updateUVs(UVs, false);
 	}
 
 	public List<UVwebContent.UV> getUVs() {
 		return mSavedUVs;
 	}
 
 	protected void updateUVs(List<UVwebContent.UV> UVs, boolean dueToFilterOperation) {
 		ThreadPreconditionsUtils.checkOnMainThread();
 
 		mSectionToPosition.clear();
 		mComprehensiveSectionsList.clear();
 
 		Collections.sort(UVs);
 
 		int i = 0;
 		for (UVwebContent.UV UV : UVs) {
 			final char section = UV.getLetterCode().charAt(0);
 			mSectionToPosition.append(i, section);
 			if (!mComprehensiveSectionsList.contains(section)) {
 				mComprehensiveSectionsList.add(section);
 			}
 			i++;
 		}
 
 		mUVs = UVs;
 		notifyDataSetChanged();
 
 		if (!dueToFilterOperation) {
 			mSavedUVs = new ArrayList<UVwebContent.UV>(UVs);
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = mLayoutInflater.inflate(R.layout.uv, null);
 		}
 
 		final TextView code1View = UVwebHolder.get(convertView, R.id.uv_code_letter);
 		final TextView code2View = UVwebHolder.get(convertView, R.id.uv_code_number);
 		final TextView descView = UVwebHolder.get(convertView, R.id.desc);
 
 		final UVwebContent.UV UV = getItem(position);
 
 		code1View.setText(UV.getLetterCode());
 		try {
 			code2View.setText(UV.getNumberCode());
 		} catch (StringIndexOutOfBoundsException e) {
 			// TODO: there must be a better way to handle this
 		}
 		descView.setText(UV.getDescription());
 
 		final View separatorView = UVwebHolder.get(convertView, R.id.list_divider);
 		if (separatorView != null && position < mUVs.size() - 1) {
 			// In tablet mode, we need to remove the last horizontal divider from the section, because these
 			// are manually drawn
 			final long currentHeaderId = getHeaderId(position),
 					nextHeaderId = getHeaderId(position + 1);
 			if (currentHeaderId != nextHeaderId) {
 				separatorView.setVisibility(View.GONE);
 			} else {
 				separatorView.setVisibility(View.VISIBLE);
 			}
 		}
 
 		return convertView;
 	}
 
 	/**
 	 * SectionIndexer interface's methods
 	 */
 	@Override
 	public int getSectionForPosition(int position) {
 		return 1;
 	}
 
 	@Override
 	public int getPositionForSection(int section) {
 		final int actualSection = Math.min(section, mComprehensiveSectionsList.size() - 1);
 		// Workaround for the fastScroll issue
 		// See https://code.google.com/p/android/issues/detail?id=33293 for more information
 		return mSectionToPosition.indexOfValue(mComprehensiveSectionsList.get(actualSection));
 	}
 
 	@Override
 	public Object[] getSections() {
 		return mComprehensiveSectionsList.toArray(new Character[mComprehensiveSectionsList.size()]);
 	}
 
 	/**
 	 * StickyListHeadersAdapter interface's methods
 	 */
 	@Override
 	public View getHeaderView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = mLayoutInflater.inflate(R.layout.header_uv_list, null);
 		}
 
 		// Set header text as first char in name
 		final TextView headerView = UVwebHolder.get(convertView, R.id.header_text);
		headerView.setText(String.valueOf(getSectionName(position)));
 
 		return convertView;
 	}
 
 	@Override
 	public long getHeaderId(int position) {
 		return getSectionName(position);
 	}
 
 	/**
 	 * @param position the position of a given item in the ListView
 	 * @return the name of the corresponding section
 	 */
 	private char getSectionName(int position) {
 		return mSectionToPosition.get(position);
 	}
 
 	@Override
 	public Filter getFilter() {
 		return mFilter;
 	}
 
 	/**
 	 * A callback interface that each Fragment using this adapter
 	 * and using its Filterable interface should implement.
 	 * This mechanism allows the querier to be notified and to act accordingly.
 	 */
 	public interface SearchCallbacks {
 		public void onItemsFound(List<UVwebContent.UV> results);
 
 		public void onNothingFound();
 	}
 
 	private final class UVFilter extends Filter {
 		private final FilterResults mFilterResults = new FilterResults();
 		private final List<UVwebContent.UV> mFoundUVs = new ArrayList<UVwebContent.UV>();
 
 		@Override
 		protected FilterResults performFiltering(CharSequence charSequence) {
 			if (charSequence == null || charSequence.length() == 0) {
 				mFilterResults.values = mSavedUVs;
 				mFilterResults.count = mSavedUVs.size();
 				return mFilterResults;
 			}
 
 			mFoundUVs.clear();
 			final String query = charSequence.toString().toUpperCase();
 			for (UVwebContent.UV UV : mSavedUVs) {
 				if (UV.getName().startsWith(query)) {
 					mFoundUVs.add(UV);
 				}
 			}
 			mFilterResults.values = mFoundUVs;
 			mFilterResults.count = mFoundUVs.size();
 			return mFilterResults;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
 			final List<UVwebContent.UV> results = (List<UVwebContent.UV>) filterResults.values;
 			updateUVs(results, true);
 			if (filterResults.count == 0) {
 				mSearchCallbacks.onNothingFound();
 			} else {
 				mSearchCallbacks.onItemsFound(results);
 			}
 		}
 	}
 }
