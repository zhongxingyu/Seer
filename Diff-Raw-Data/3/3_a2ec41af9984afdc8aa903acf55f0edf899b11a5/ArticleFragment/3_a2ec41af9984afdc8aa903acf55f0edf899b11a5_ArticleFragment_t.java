 package org.gnuton.newshub;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.text.Html;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import com.google.ads.AdView;
 
 import org.gnuton.newshub.adapters.ArticleListAdapter;
 import org.gnuton.newshub.adapters.ImageAdapter;
 import org.gnuton.newshub.db.DbHelper;
 import org.gnuton.newshub.tasks.BoilerPipeTask;
 import org.gnuton.newshub.tasks.ImageGetter;
 import org.gnuton.newshub.tasks.UpdateEntryInDB;
 import org.gnuton.newshub.types.RSSEntry;
 import org.gnuton.newshub.utils.FontsProvider;
 import org.gnuton.newshub.utils.MyApp;
 import org.gnuton.newshub.view.ObservableScrollView;
 import org.gnuton.newshub.view.UninterceptableViewPager;
 
 public class ArticleFragment extends Fragment implements BoilerPipeTask.OnBoilerplateRemovedListener {
     private static final String TAG = "ARTICLE_FRAGMENT";
 
     private ArticleListAdapter mEntryAdapter = null;
     private int mEntryPosition= -1;
 
     private AsyncTask mTask = null;
     private ImageAdapter mImageAdapter;
     private ImageGetter mImageGetter;
     private AdView mAdView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.d(TAG, "Create view");
         //return super.onCreateView(inflater, container, savedInstanceState);
         final View view = inflater.inflate(R.layout.article_fragment, container, false);
 
         //Content View
         assert view != null;
         final TextView contentView = (TextView) view.findViewById(R.id.ContentTextView);
         contentView.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
         //Title
         final TextView titleView = (TextView) view.findViewById(R.id.TitleTextView);
         titleView.setTypeface(FontsProvider.getInstace().getTypeface("NanumGothic-Regular"));
 
         // Instantiate imageGetter
         mImageGetter = new ImageGetter(contentView);
 
         // Image view Pager
         ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
         viewPager.setOffscreenPageLimit(1);
         mImageAdapter = new ImageAdapter(view.getContext());
         mImageGetter.setAdapter(mImageAdapter, viewPager);
         viewPager.setAdapter(mImageAdapter);
 
         // Define action for button
         final Button readMoreButton = (Button) view.findViewById(R.id.ReadMoreButton);
         readMoreButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
 
         readMoreButton.setVisibility(View.GONE);
         readMoreButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 final RSSEntry entry = MyApp.mEntry;
                 if (entry != null) {
                     String content = entry.content;
                     if (content != null) {
                         Spanned myStringSpanned = Html.fromHtml(content, mImageGetter, null);
                         contentView.setText(myStringSpanned, TextView.BufferType.SPANNABLE);
                         readMoreButton.setVisibility(View.GONE);
                     } else {
                         // Show content in a browser
                         Intent i = new Intent(Intent.ACTION_VIEW);
                         i.setData(Uri.parse(entry.link));
                         startActivity(i);
                     }
                 }
                 // scroll up
                 ScrollView scrollview = (ScrollView) getView().findViewById(R.id.scrollView);
                 scrollview.pageScroll(View.FOCUS_UP);
             }
         });
 
         // Define action for open link button
         final Button openLinkButton = (Button) view.findViewById(R.id.OpenLinkButton);
         openLinkButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
         openLinkButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 final RSSEntry entry = MyApp.mEntry;
                 if (entry != null) {
                     // Show content in a browser
                     Intent i = new Intent(Intent.ACTION_VIEW);
                     i.setData(Uri.parse(entry.link));
                     startActivity(i);
                 }
             }
         });
 
         // Define action for share button
         final Button shareLinkButton = (Button) view.findViewById(R.id.ShareLinkButton);
         shareLinkButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
         shareLinkButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 RSSEntry entry = MyApp.mEntry;
                 if (entry != null) {
                     // Show content in a browser
                     Intent intent = new Intent(Intent.ACTION_SEND);
                     intent.setType("text/plain");
                     intent.putExtra(Intent.EXTRA_TEXT, entry.link);
                     intent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.sharing_url_string);
                     startActivity(Intent.createChooser(intent, "Share"));
                 }
             }
 
         });
 
         // Define action for share button
         final Button prevArticleButton = (Button) view.findViewById(R.id.PreviousArticleButton);
         prevArticleButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
         prevArticleButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (mEntryAdapter != null) {
                     int previousEntryPos = mEntryPosition -1;
                     if (previousEntryPos < 0)
                         return;
 
                     setEntry(mEntryAdapter, previousEntryPos);
                 }
             }
 
         });
 
         // Define action for share button
         final Button nextArticleButton = (Button) view.findViewById(R.id.NextArticleButton);
         nextArticleButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
         nextArticleButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (mEntryAdapter != null) {
                     int nextEntryPos = mEntryPosition + 1;
                     if (nextEntryPos > mEntryAdapter.getCount())
                         return;
 
                     setEntry(mEntryAdapter, nextEntryPos);
                 }
             }
 
         });
 
         // Scrolling parallax
         final ObservableScrollView scrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
         final UninterceptableViewPager imageViewPager = (UninterceptableViewPager) view.findViewById(R.id.view_pager);
         scrollView.setCallbacks(new ObservableScrollView.ScrollCallbacks() {
 
             @Override
             public void onScrollChanged(int l, int t, int oldl, int oldt) {
                 int totalHeight = scrollView.getChildAt(0).getHeight();
                 int iwPagerHeight = imageViewPager.getChildAt(0).getHeight();
                 //Log.d("TAG", "T=" + String.valueOf(t) + " HEIGHT=" + String.valueOf(totalHeight) + " H2=" + String.valueOf(iwPagerHeight));
 
                 int o = t * iwPagerHeight * 2 / totalHeight;
                 imageViewPager.setScrollY(o);
             }
         });
 
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onStart();
         Log.d(TAG, "ACTIVITY CREATED");
     }
 
     @Override
     public void onStart() {
         Log.d(TAG, "START");
         super.onStart();
         setEntry(MyApp.mEntry);
     }
 
     @Override
     public void onDestroy() {
         if (mAdView != null) {
             mAdView.destroy();
         }
         super.onDestroy();
         if (this.mTask != null)
             this.mTask.cancel(true);
         Log.d(TAG, "DESTROY");
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         Log.d(TAG, "DETACH");
     }
 
     public void onSaveInstanceState(Bundle savedInstanceState) {
         Log.d(TAG, "Saving ArticleFragment state");
     }
 
     public void setEntryAndWait(final ArticleListAdapter adapter, final int entryPosition) {
         if (MyApp.mMainActivity == null) {
             Log.w(TAG, "No activity obj");
             return;
         }
 
         android.os.Handler h = MyApp.mMainActivity.getWindow().getDecorView().getHandler();
 
         if (adapter == null)
             return;
         final RSSEntry entry = adapter.getItem(entryPosition);
         // we do not need to set the same entry into the UI
         if (entry == MyApp.mEntry)
             return;
 
         // Hide empty view if an article should be shown.
         final View articleFragmentEmptyView = getView().findViewById(R.id.ArticleFragmentEmptyViewLayout);
         articleFragmentEmptyView.setVisibility((entry == null) ? View.VISIBLE : View.GONE);
 
         // Clear view
         final View articleFragmentLoadingView = getView().findViewById(R.id.ArticleFragmentLoadingViewLayout);
         articleFragmentLoadingView.setVisibility((entry == null) ? View.GONE : View.VISIBLE);
 
 
         assert h != null;
         h.postDelayed(new Runnable() {
             @Override
             public void run() {
 
                 setEntry(adapter, entryPosition);
             }
         }, 800);
     }
     private void setEntry(ArticleListAdapter adapter, int entryPosition) {
         Log.d(TAG,"Set mEntry");
 
         if (adapter == null) return;
         final RSSEntry entry;
         try {
             entry = adapter.getItem(entryPosition);
         } catch (IndexOutOfBoundsException e){
             entryPosition = 0;
             setEntry(adapter, entryPosition);
             return;
         }
         // Update internal attributes
         MyApp.mEntry = entry;
         mEntryAdapter = adapter;
         mEntryPosition = entryPosition;
 
         setEntry(entry);
     }
 
     private void setEntry(final RSSEntry entry) {
         // We don't want to run the next code if entry is null.
         if (entry == null)
             return;
 
         // Set item as read
         if (!entry.isRead) {
             entry.isRead = true;
             entry.columnsToUpdate.add(DbHelper.ENTRIES_ISREAD);
             new UpdateEntryInDB().execute(entry);
             mEntryAdapter.notifyDataSetChanged();
         }
 
         // reset imageAdapter
         if (mImageAdapter != null){
             mImageAdapter.mImages.clear();
             mImageAdapter.mImages.add(getResources().getDrawable(R.drawable.placeholder));
             mImageAdapter.notifyDataSetChanged();
         }
 
         if (getView() == null) {
             return;
         }
 
         // Hide Read more button
         final Button readMoreButton = (Button) getView().findViewById(R.id.ReadMoreButton);
         readMoreButton.setVisibility(View.GONE);
 
         //Set Title
         final TextView titleView = (TextView) getView().findViewById(R.id.TitleTextView);
         Spanned titleSpanned = Html.fromHtml(entry.title, mImageGetter, null);
         titleView.setText(titleSpanned);
 
         //Set page content
         TextView contentView = (TextView) getView().findViewById(R.id.ContentTextView);
 
         String content;
 
         if (entry.content != null) {
             content = entry.content;
         } else {
             content = entry.summary;
             fetchFullArticle(entry);
         }
         Spanned contentSpanned = Html.fromHtml(content, mImageGetter, null);
         contentView.setText(contentSpanned, TextView.BufferType.SPANNABLE);
 
         // scroll up
         ScrollView scrollview = (ScrollView) getView().findViewById(R.id.scrollView);
        //scrollview.pageScroll(View.FOCUS_UP);
        scrollview.setScrollY(0);
 
         // Remove layouts on top of the article
         final View articleFragmentEmptyView = getView().findViewById(R.id.ArticleFragmentEmptyViewLayout);
         articleFragmentEmptyView.setVisibility(View.GONE);
         final View articleFragmentLoadingView = getView().findViewById(R.id.ArticleFragmentLoadingViewLayout);
         articleFragmentLoadingView.setVisibility(View.GONE);
     }
 
     private void fetchFullArticle(RSSEntry entry) {
         this.mTask = new BoilerPipeTask(this).execute(entry);
     }
 
     @Override
     public void onBoilerplateRemoved(RSSEntry[] entries) {
         Log.d("TAG", "BOILER PLATE REMOVED");
         RSSEntry e =entries[0];
         RSSEntry entry = MyApp.mEntry;
 
         if (entry == null || !entry.link.equals(e.link))
             return;
 
         MyApp.mEntry = e;
         Button readMoreButton = (Button) getView().findViewById(R.id.ReadMoreButton);
         readMoreButton.setVisibility(View.VISIBLE);
 
     }
 }
 
