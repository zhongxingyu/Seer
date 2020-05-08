 package com.bonkler.flipdroid;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.*;
 import android.support.v4.view.ViewPager;
 
 import android.database.Cursor;
 import android.content.Intent;
 import android.view.View;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.widget.EditText;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class BrowseDeckActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 
     private FlipDeck mDeck;
     private Cursor mCursor;
 
     private ViewPager mViewPager;
     private FlipPagerAdapter mPagerAdapter;
     private FlipCursorLoader mLoader;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.browse_deck);
 
         // Intent intent = getIntent();
         // int id = intent.getIntExtra(MainActivity.CLICKED_DECK_ID, -1);
         // contents = intent.getStringExtra(MainActivity.CLICKED_DECK_CARD_IDS);
         // String name = intent.getStringExtra(MainActivity.CLICKED_DECK_NAME);
         // mDeck = new FlipDeck(name, contents, id);
 
         mDeck = MainActivity.DeckListFragment.getActiveDeck();
 
         mPagerAdapter = new FlipPagerAdapter(getSupportFragmentManager());
         mViewPager = (ViewPager) findViewById(R.id.pager);
 
         mLoader = (FlipCursorLoader)getSupportLoaderManager().initLoader(2, null, this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.card_action_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.edit_card_option:
                 startEditCard();
                 return true;
             case R.id.new_card_option:
                 startNewCard();
                 return true;
             case R.id.shuffle_deck_option:
                 startShuffleDeck();
                 return true;
             case R.id.delete_card_option:
                 startDeleteCard();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void startEditCard() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();
         final View view = inflater.inflate(R.layout.new_card_dialog, null);
 
         // Populate the text field
         EditText oldQuestion = (EditText) view.findViewById(R.id.new_card_question);
         EditText oldAnswer = (EditText) view.findViewById(R.id.new_card_answer);
         int position = mViewPager.getCurrentItem();
         Log.i("POSITION", "" + position);
         final FlipCard card = ((CardFragment) mPagerAdapter.getItem(position)).getCard();
         Log.i("PREVIOUS ID", "" + card.getId());
         oldQuestion.setText(card.getQuestion()); 
         oldAnswer.setText(card.getAnswer()); 
 
         builder.setView(view);
         builder.setMessage("Edit the question and answer:");
         builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 EditText question = (EditText) view.findViewById(R.id.new_card_question);
                 EditText answer = (EditText) view.findViewById(R.id.new_card_answer);
                 String q = question.getText().toString();
                 String a = answer.getText().toString();
 
                 card.setQuestion(q);
                 card.setAnswer(a);
                 Log.i("ANSWER", card.getAnswer());
                 Log.i("ID", "" + card.getId());
                 mLoader.update(card);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // dismiss the dialog.
             }
         });
 
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
     private void startNewCard() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();
         final View view = inflater.inflate(R.layout.new_card_dialog, null); 
         builder.setView(view);
         builder.setMessage("Enter a question and answer:");
         builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 EditText question = (EditText) view.findViewById(R.id.new_card_question);
                 EditText answer = (EditText) view.findViewById(R.id.new_card_answer);
                 String q = question.getText().toString();
                 String a = answer.getText().toString();
 
                 FlipCard card = new FlipCard(q, a);
                 MainActivity.DeckListFragment.needRefresh = true;
                 mLoader.insert(card, mDeck);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // dismiss the dialog.
             }
         });
 
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
     private void startShuffleDeck() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();
         final View view = inflater.inflate(R.layout.base_deck_dialog, null); 
         builder.setView(view);
         builder.setMessage("Shuffle this deck?");
         builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 mDeck.shuffleSelf();
                 MainActivity.DeckListFragment.needRefresh = true;
                 mLoader.update(mDeck);
                 mViewPager.setCurrentItem(0, false);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // dismiss the dialog.
             }
         });
 
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
     private void startDeleteCard() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();
         final View view = inflater.inflate(R.layout.base_deck_dialog, null);
         int position = mViewPager.getCurrentItem();
        final FlipCard card = new FlipCard(mCursor, position);
         final FlipDeck deck = mDeck;
 
         builder.setView(view);
         builder.setMessage("Delete this card?");
         builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 MainActivity.DeckListFragment.needRefresh = true;
                 mLoader.delete(card, deck);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // dismiss the dialog.
             }
         });
 
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 
     // LOADER CALLBACKS
 
     // Called when a new loader is needed.
         @Override
         public Loader<Cursor> onCreateLoader(int id, Bundle args) {
             Log.i("LOADER", mDeck.getContentsAsString());
             return new FlipCursorLoader(
                 this,
                 FlipDroidDBHelper.getInstance(this),
                 FlipDroidContract.MyCards.TABLE_NAME,
                 null,
                 FlipDroidContract.MyCards._ID + " IN (" + mDeck.getContentsAsString() + ")",
                 null,
                 null);
         }
 
         // Called when an existing loader finishes its loading task.
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
             mCursor = c;
             mPagerAdapter.changeCards(mDeck.fillCards(c));
             if (mViewPager.getAdapter() == null)
                 mViewPager.setAdapter(mPagerAdapter);
         }
 
         // Called when an existing loader is reset.
         @Override
         public void onLoaderReset(Loader<Cursor> loader) {
         }
 
 }
