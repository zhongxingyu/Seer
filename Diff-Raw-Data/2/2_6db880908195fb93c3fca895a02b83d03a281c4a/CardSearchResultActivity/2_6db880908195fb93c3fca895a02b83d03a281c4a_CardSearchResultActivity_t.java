 package com.zotca.vbc.dbhistory;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Set;
 
 import com.zotca.vbc.dbhistory.core.CardDatabase.Card;
 import com.zotca.vbc.dbhistory.core.DatabaseDelta;
 import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
 
 import android.os.Bundle;
 import android.app.SearchManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.widget.SearchView;
 import android.support.v7.app.ActionBarActivity;
 import android.content.Context;
 import android.content.Intent;
 
 public class CardSearchResultActivity extends ActionBarActivity {
 
 	private DatabaseFileManager mFileManager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.fragment_cardlist);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		final ListView listView = (ListView) findViewById(android.R.id.list);
 		final Context ctx = this;
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v, int position,
 					long id) {
 				Intent args = new Intent(ctx, CardViewActivity.class);
 				int cardId = (Integer) v.getTag();
 				args.putExtra(CardViewActivity.ARG_ID, cardId);
 				startActivity(args);
 			}
 			
 		});
 		
 		if (savedInstanceState != null)
 			query = savedInstanceState.getString(SearchManager.QUERY);
 		mFileManager = DatabaseFileManager.getManager(this,
 				new DatabaseFileManager.PostProcessHandler() {
 			
 			@Override
 			public void onPostProcess(DatabaseFileManager manager) {
 				mFileManager = manager;
 				if (query != null) search();
 				else handleIntent(getIntent());
 			}
 		});
 	}
 
 	private void setupActionBar() {
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.search, menu);
 		
 		SearchManager searchManager =
 		           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 	    MenuItem searchMenuItem = menu.findItem(R.id.search);
 	    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
 	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 	    searchView.setOnSearchClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				SearchView searchView = (SearchView) v;
 				searchView.setQuery(query, false);
 			}
 		});
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onSearchRequested() {
 		startSearch(query, true, null, false);
 		return false;
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		setIntent(intent);
 		handleIntent(intent);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (query != null) outState.putString(SearchManager.QUERY, query);
 	}
 	
 	private String query;
 	private void search(String query) {
 		this.query = query;
 		search();
 	}
 	private void search() {
 		final Context ctx = this;
 		this.setTitle(query);
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				final LinkedList<Long> chain = mFileManager.getChain();
 				final ArrayList<Card> queryResult = new ArrayList<Card>();
 				final HashSet<Integer> resultIds = new HashSet<Integer>();
 				for (long time : chain)
 				{
 					final DatabaseDelta delta = mFileManager.getDelta(time);
 					final Set<Integer> cards = delta.getCardIdSet();
 					for (int id : cards)
 					{
						if (resultIds.contains(id)) continue;
 						
 						final Card card = delta.getCard(id);
 						if (matchesAdvancedQuery(card, query))
 						{
 							queryResult.add(card);
 							resultIds.add(id);
 						}
 					}
 				}
 				
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						ListView listView = (ListView) findViewById(android.R.id.list);
 						SearchResultAdapter adapter = new SearchResultAdapter(ctx, queryResult);
 						listView.setAdapter(adapter);
 					}
 					
 				});
 			}
 		}).start();
 	}
 	private void handleIntent(final Intent intent) {
 		if (mFileManager == null) return;
 		
 		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
 		{
 			search(intent.getStringExtra(SearchManager.QUERY));
 		}
 	}
 	
 	private static final int COMPARE_EQUALS = 1 << 0;
 	private static final int COMPARE_LESS = 1 << 1;
 	private static final int COMPARE_GREATER = 1 << 2;
 	
 	private static boolean matchesSimpleQuery(Card card, String query) {
 		if (query.startsWith("스킬?"))
 		{
 			query = query.substring(3);
 			if (!(
 					card.getSkillName().contains(query) || 
 					card.getSubSkillName().contains(query)) )
 				return false;
 		}
 		else if (query.startsWith("일러?"))
 		{
 			query = query.substring(3);
 			if (!card.getIllustrator().contains(query))
 				return false;
 		}
 		else if (query.startsWith("본문?"))
 		{
 			query = query.substring(3);
 			if (!card.getDescription().contains(query))
 				return false;
 		}
 		else if (query.startsWith("진영?"))
 		{
 			query = query.substring(3);
 			int categoryInt = 0;
 			if (query.equals("검술")) categoryInt = Card.BLADE;
 			else if (query.equals("기교")) categoryInt = Card.TECHNIQUE;
 			else if (query.equals("마법")) categoryInt = Card.MAGIC;
 			else if (query.equals("요정")) categoryInt = Card.FAIRY;
 			if (categoryInt != 0)
 			{
 				if (card.getCategory() != categoryInt)
 					return false;
 			}
 		}
 		else if (query.startsWith("성별?"))
 		{
 			query = query.substring(3);
 			boolean isFemale = false;
 			boolean isAvailable = false;
 			if (query.equals("남"))
 			{
 				isAvailable = true;
 			}
 			else if (query.equals("여"))
 			{
 				isAvailable = true;
 				isFemale = true;
 			}
 			if (isAvailable)
 			{
 				if (card.isFemale() != isFemale)
 					return false;
 			}
 		}
 		else if(query.startsWith("레어도?"))
 		{
 			query = query.substring(4);
 			if (query.length() > 0)
 			{
 				int compareType = COMPARE_EQUALS;
 				char firstChar = query.charAt(0);
 				if (firstChar == '<')
 				{
 					compareType = COMPARE_LESS;
 					if (query.charAt(1) == '=')
 					{
 						compareType |= COMPARE_EQUALS;
 						query = query.substring(2);
 					}
 					else query = query.substring(1);
 				}
 				else if (firstChar == '>')
 				{
 					compareType = COMPARE_GREATER;
 					if (query.charAt(1) == '=')
 					{
 						compareType |= COMPARE_EQUALS;
 						query = query.substring(2);
 					}
 					else query = query.substring(1);
 				}
 				else if (firstChar == '!')
 				{
 					compareType = COMPARE_GREATER | COMPARE_LESS;
 					query = query.substring(1);
 				}
 				
 				final int rareLevel = Card.getRareLevelFromString(query);
 				if (rareLevel != -1)
 				{
 					final int cardRareLevel = card.getRareLevel();
 					boolean succeeded = false;
 					if ((compareType & COMPARE_EQUALS) != 0)
 					{
 						if (cardRareLevel == rareLevel) succeeded = true;
 					}
 					if ((compareType & COMPARE_LESS) != 0)
 					{
 						if (cardRareLevel < rareLevel) succeeded = true;
 					}
 					if ((compareType & COMPARE_GREATER) != 0)
 					{
 						if (cardRareLevel > rareLevel) succeeded = true;
 					}
 					if (!succeeded) return false;
 				}
 			}
 		}
 		else // 이름 검색
 		{
 			if (!card.getName().contains(query))
 				return false;
 		}
 		return true;
 	}
 	
 	private static boolean matchesAdvancedQuery(Card card, String query) {
 		boolean inQuotes = false;
 		boolean inBracket = false;
 		boolean isNotApplied = false;
 		boolean failed = false, passNext = false;
 		StringBuilder inProcessString = new StringBuilder();
 		boolean wasInQuote = false;
 		boolean wasInBracket = false;
 		for (char c : query.toCharArray())
 		{
 			if (c == '"')
 			{
 				if (inQuotes) wasInQuote = true;
 				inQuotes = !inQuotes;
 				continue;
 			}
 			else if (c == '!')
 			{
 				isNotApplied = true;
 				continue;
 			}
 			else if (c == '(' && inProcessString.toString().equals(""))
 			{
 				inBracket = true;
 				continue;
 			}
 			else if (c == ')')
 			{
 				inBracket = false;
 				wasInBracket = true;
 				continue;
 			}
 			if (inQuotes || inBracket) inProcessString.append(c);
 			else
 			{
 				if (c == ' ')
 				{
 					String res = inProcessString.toString();
 					inProcessString = new StringBuilder();
 					if (!wasInQuote && (res.equalsIgnoreCase("or") || res.equals("또는")))
 					{
 						if (!failed) passNext = true;
 						else failed = false;
 						continue;
 					}
 					if (passNext)
 					{
 						passNext = false;
 						continue;
 					}
 					if (failed) return false;
 					if (wasInBracket)
 					{
 						if (!matchesAdvancedQuery(card, res)) failed = true;
 					}
 					else
 					{
 						if (!matchesSimpleQuery(card, res))
 							failed = true;
 					}
 					failed ^= isNotApplied;
 					isNotApplied = false;
 				}
 				else inProcessString.append(c);
 			}
 			wasInQuote = false;
 			wasInBracket = false;
 		}
 		String res = inProcessString.toString();
 		if (!wasInQuote && (res.equalsIgnoreCase("or") || res.equals("또는")))
 		{
 		}
 		else
 		{
 			if (passNext) return !failed;
 			if (failed) return false;
 			if (wasInBracket)
 			{
 				if (!matchesAdvancedQuery(card, res)) failed = true;
 			}
 			else
 			{
 				if (!matchesSimpleQuery(card, res))
 					failed = true;
 			}
 			failed ^= isNotApplied;
 			isNotApplied = false;
 		}
 		return !failed;
 	}
 }
