 package com.thoughtworks.pumpkin.fragment;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.thoughtworks.pumpkin.BaseActivity;
 import com.thoughtworks.pumpkin.R;
 import com.thoughtworks.pumpkin.ShopActivity;
 import com.thoughtworks.pumpkin.adapter.BooksAdapter;
 import com.thoughtworks.pumpkin.adapter.ShopsAdapter;
 import com.thoughtworks.pumpkin.helper.Constant;
 import com.thoughtworks.pumpkin.helper.PumpkinDB;
 import com.thoughtworks.pumpkin.helper.Util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ViewBooks extends SherlockFragment {
 
     private ListView booksListView;
     private String category;
     private String wishList;
     private String query;
     private HashMap<String, String> forMapView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         setHasOptionsMenu(true);
         setRetainInstance(true);
         return inflater.inflate(R.layout.books, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         category = getActivity().getIntent().getStringExtra("category");
         wishList = getActivity().getIntent().getStringExtra("wishlist");
         query = getActivity().getIntent().getStringExtra("query");
 
         Spinner spinner = (Spinner) view.findViewById(R.id.cart);
         List<String> shops = new PumpkinDB(getActivity()).getShops();
         shops.add(0, getResources().getString(R.string.view_cart));
         spinner.setAdapter(new ShopsAdapter<String>(getActivity(), R.layout.spin_shop_rowitem, shops));
         spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 if (i == 0) return;
                 Intent intent = new Intent(getActivity(), ShopActivity.class);
                 if (category != null) intent.putExtra("category", category);
                 if (wishList != null) intent.putExtra("wishlist", wishList);
                 intent.putExtra("shop", ((TextView) view.findViewById(R.id.text1)).getText());
                 intent.putExtra("books", forMapView);
                 startActivity(intent);
                 getActivity().finish();
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
             }
         });
 
         booksListView = (ListView) view.findViewById(R.id.books);
         ((BaseActivity) getActivity()).getSupportActionBar().setTitle((category != null) ? category : wishList);
 
         if (category != null) {
             findByCategory(category);
             return;
         }
         if (wishList != null) {
             findByWishList(wishList);
             return;
         }
         if (query != null) {
             searchBooks(query);
             return;
         }
     }
 
     private void findByWishList(String wishList) {
         final ProgressDialog dialog = Util.showProgressDialog(getActivity());
         ParseQuery query = new ParseQuery(Constant.ParseObject.WISH_LIST_BOOK);
         ParseQuery innerQuery = new ParseQuery(Constant.ParseObject.WISH_LIST);
         innerQuery.whereEqualTo(Constant.ParseObject.COLUMN.WISH_LIST.NAME, wishList);
         query.whereMatchesQuery(Constant.ParseObject.COLUMN.WISH_LIST_BOOK.WISH_LIST, innerQuery);
         query.findInBackground(new FindCallback() {
             @Override
             public void done(List<ParseObject> wishListBooks, ParseException e) {
                 if (wishListBooks.isEmpty()) {
                     AlertDialog alertDialog = Util.dialog("No books in this list.", getActivity());
                     alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                         @Override
                         public void onDismiss(DialogInterface dialogInterface) {
                             if (dialog.isShowing()) dialog.dismiss();
                             getActivity().finish();
                         }
                     });
                     alertDialog.show();
                 } else {
                     List<ParseObject> books = new ArrayList<ParseObject>();
                     for (ParseObject wishListBook : wishListBooks) {
                         books.add(wishListBook.getParseObject(Constant.ParseObject.COLUMN.WISH_LIST_BOOK.BOOK));
                     }
                     loadBooks(books, false, dialog);
                 }
             }
         });
     }
 
     private void findByCategory(String category) {
         ParseQuery query = new ParseQuery(Constant.ParseObject.BOOK);
         ParseQuery innerQuery = new ParseQuery(Constant.ParseObject.CATEGORY);
         innerQuery.whereEqualTo(Constant.ParseObject.COLUMN.BOOK.NAME, category);
         query.whereMatchesQuery("parent", innerQuery);
         query.orderByAscending(Constant.ParseObject.COLUMN.BOOK.RATING);
         final ProgressDialog dialog = Util.showProgressDialog(getActivity());
         query.findInBackground(new FindCallback() {
             @Override
             public void done(List<ParseObject> parseObjects, ParseException e) {
                 loadBooks(parseObjects, true, dialog);
             }
         });
     }
 
     public void searchBooks(final String queryString) {
         ParseQuery searchInBookTitle = new ParseQuery(Constant.ParseObject.BOOK);
         searchInBookTitle.whereMatches(Constant.ParseObject.COLUMN.BOOK.TITLE, queryString, "i");
 
        ParseQuery searchInBookAuthor = new ParseQuery(Constant.ParseObject.BOOK);
        searchInBookAuthor.whereMatches(Constant.ParseObject.COLUMN.BOOK.AUTHORS, queryString, "i");
 
         ArrayList<ParseQuery> parseQueries = new ArrayList<ParseQuery>();
        parseQueries.add(searchInBookAuthor);
         parseQueries.add(searchInBookTitle);
 
         ParseQuery mainQuery = ParseQuery.or(parseQueries);
         mainQuery.orderByAscending(Constant.ParseObject.COLUMN.BOOK.RATING);
         final ProgressDialog dialog = Util.showProgressDialog(getActivity());
         mainQuery.findInBackground(new FindCallback() {
             @Override
             public void done(List<ParseObject> parseObjects, ParseException e) {
                 ((BaseActivity) getActivity()).getSupportActionBar().setTitle("\"" + queryString + "\" - " + parseObjects.size() + " results");
                 loadBooks(parseObjects, true, dialog);
             }
         });
     }
 
     private void loadBooks(List<ParseObject> books, boolean fetchAll, final ProgressDialog dialog) {
         ArrayList<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
         forMapView = new HashMap<String, String>();
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
         for (ParseObject book : books) {
             HashMap<String, Object> map = new HashMap<String, Object>();
             map.put("book", book);
             map.put("complete", fetchAll);
             maps.add(map);
             if (fetchAll) {
                 forMapView.put(book.getObjectId(), book.getParseObject("parent").getObjectId());
             } else {
                 forMapView.put(book.getObjectId(), null);
             }
         }
         if (dialog.isShowing()) dialog.dismiss();
         booksListView.setAdapter(new BooksAdapter(getActivity(), maps, R.layout.book,
                 new String[]{"bookImage", "title", "rank", "objectId", "snippet", "authors"},
                 new int[]{R.id.bookImage, R.id.title, R.id.rank, R.id.heart, R.id.description, R.id.authors}));
     }
 
 }
