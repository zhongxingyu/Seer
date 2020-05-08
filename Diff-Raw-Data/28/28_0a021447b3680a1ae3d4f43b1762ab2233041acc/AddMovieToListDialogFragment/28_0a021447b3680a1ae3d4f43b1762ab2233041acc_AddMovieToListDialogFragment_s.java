 package com.watchlistapp.addmovietolistdialog;
 
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.widget.ListView;
 
 import com.watchlistapp.R;
 import com.watchlistapp.database.WatchListDatabaseHandler;
 import com.watchlistapp.watchlistserver.MovieList;
 import com.watchlistapp.watchlistserver.MovieListContainer;
 
 /**
  * Created by VEINHORN on 29/12/13.
  */
 public class AddMovieToListDialogFragment extends DialogFragment {
 
     private ListView listView;
     private AddMovieToListDialogListsItemAdapter addMovieToListDialogListsItemAdapter;
     private AddMovieToListDialogListsItemContainer addMovieToListDialogListsItemContainer;
 
     /*
      * Create a new instance of AddMovieToListDialogFragment, providing "num"
      * as an argument
      */
     public static AddMovieToListDialogFragment newInstance() {
         AddMovieToListDialogFragment addMovieToListDialogFragment = new AddMovieToListDialogFragment();
 
         return addMovieToListDialogFragment;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.add_movie_to_dialog_layout);
        dialog.setTitle("Test dialog");

        listView = (ListView)getActivity().findViewById(R.id.add_movie_to_list_dialog_fragment_list_view);
        addMovieToListDialogListsItemContainer = new AddMovieToListDialogListsItemContainer();
        //fillListContainer(addMovieToListDialogListsItemContainer);
        addMovieToListDialogListsItemAdapter = new AddMovieToListDialogListsItemAdapter(getActivity(), addMovieToListDialogListsItemContainer);
        listView.setAdapter(addMovieToListDialogListsItemAdapter);
         return dialog;
     }
 
     private void fillListContainer(AddMovieToListDialogListsItemContainer addMovieToListDialogListsItemContainer) {
         WatchListDatabaseHandler watchListDatabaseHandler = new WatchListDatabaseHandler(getActivity());
         MovieListContainer movieListContainer = watchListDatabaseHandler.getAllPlaylists();
 
         for(MovieList movieList : movieListContainer.getMovieListArrayList()) {
             addMovieToListDialogListsItemContainer.getAddMovieToListDialogListsItemArrayList().add(new AddMovieToListDialogListsItem(movieList.getTitle(), R.drawable.mylists));
         }
 
     }
 }
