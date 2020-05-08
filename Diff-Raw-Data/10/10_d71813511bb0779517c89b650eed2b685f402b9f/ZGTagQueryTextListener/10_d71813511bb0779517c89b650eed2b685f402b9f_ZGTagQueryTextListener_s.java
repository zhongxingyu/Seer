 package org.geekosphere.zeitgeist.activity;
 
 import org.geekosphere.zeitgeist.data.ZGTag;
 import org.geekosphere.zeitgeist.net.WebRequestBuilder;
 import org.geekosphere.zeitgeist.processor.ZGTagProcessor;
 import org.geekosphere.zeitgeist.view.adapter.TagSuggestionAdapter;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.widget.FilterQueryProvider;
 import at.diamonddogs.data.dataobjects.CacheInformation;
 import at.diamonddogs.data.dataobjects.WebRequest;
 import at.diamonddogs.service.net.HttpServiceAssister;
 
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
 
 public class ZGTagQueryTextListener implements OnQueryTextListener {
 	private Context context;
 	private HttpServiceAssister assister;
	private SearchView sv;
 	private TagSuggestionAdapter adapter;
 	private ZGTag[] tags;
 
 	public ZGTagQueryTextListener(Context c, SearchView sv, HttpServiceAssister assister) {
 		this.context = c;
 		this.assister = assister;
		this.sv = sv;
 		loadTags();
 		sv.setOnQueryTextListener(this);
 		adapter = new TagSuggestionAdapter(context, createCursorFromQuery(""));
 		adapter.setFilterQueryProvider(new FilterQueryProvider() {
 			@Override
 			public Cursor runQuery(CharSequence constraint) {
 				return createCursorFromQuery(constraint == null ? "" : constraint.toString());
 			}
 		});
 		sv.setSuggestionsAdapter(adapter);
 	}
 
 	private void loadTags() {
 		WebRequestBuilder b = new WebRequestBuilder(context);
 		WebRequest wr = b.getTags().build();
 		wr.setCacheTime(CacheInformation.CACHE_1H);
 		tags = (ZGTag[]) assister.runSynchronousWebRequest(wr, new ZGTagProcessor()).getPayload();
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		adapter.getFilterQueryProvider().runQuery(newText);
 		return false;
 	}
 
 	private Cursor createCursorFromQuery(String query) {
 		MatrixCursor mx = new MatrixCursor(new String[] { "_id", "count", "tag" });
 		if (tags != null) {
 			for (ZGTag tag : tags) {
 				if (tag.getTagName().contains(query)) {
 					mx.addRow(new String[] { String.valueOf(tag.getId()), String.valueOf(tag.getItemCount()), tag.getTagName() });
 				}
 			}
 		}
 		return mx;
 	}
 }
