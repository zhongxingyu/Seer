 package com.epam.Suggestions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 import android.app.SearchManager;
 import android.app.SearchableInfo;
 import android.content.Context;
 
 public class Suggestions implements ISuggestionEvents {
 
 	 
 	//constructors
 	public Suggestions(Context context)
 	{
 		super();
 		mContext = context;
 		init();
 		
 	}
 	//Events
 	public boolean OnReloadStart() {
 		for (ISuggestionEvents eventListener : mEventsListeners) {
 			eventListener.OnReloadStart();
 		}
 		return false;
 	}
 	public boolean OnReloadFinished() {
 		for (ISuggestionEvents eventListener : mEventsListeners) {
 			eventListener.OnReloadFinished();
 		}
 		return false;
 	}
 	public boolean OnSuggestionLoaded(ISuggestion suggestion) {
 		for (ISuggestionEvents eventListener : mEventsListeners) {
 			eventListener.OnSuggestionLoaded(suggestion);
 		}
 		return false;
 	}
	
 	public void addSuggestionEventsListener(ISuggestionEvents listener)
 	{
 		if(!mEventsListeners.contains(listener) && listener != this)
 		{
 			mEventsListeners.add(listener);
 		}
 	}
 	
 	public boolean removeSuggestionEventsListener(ISuggestionEvents listener)
 	{
 		return mEventsListeners.remove(listener);
 	}
 	
 	//Interface
 	public CharSequence getQuery() {
 		return mQuery;
 	}
 
 	public void setQuery(CharSequence s) {
 		this.mQuery = s;
 	}
 	
 	public List<ISuggestion> getSuggestions()
 	{
 		return mSuggestions;
 	}
 	
 	public void reload()
 	{
 		setCanceled(false);
 		OnReloadStart();
 		for (ISuggestion suggestion : mSuggestions) {
 			if(isCanceled())break;
 			try
 			{
 				suggestion.select();
 			}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 			OnSuggestionLoaded(suggestion);
 		}		
 		OnReloadFinished();
 	}
 	
 	public Context getContext()
 	{
 		return mContext;
 	}
 	
 	public Integer getQueryLimmit() {
 		return mQueryLimmit;
 	}
 	
 	public void setQueryLimmit(Integer queryLimmit) {
 		if (queryLimmit == null || queryLimmit <= 0)
 			this.mQueryLimmit = null;
 		else
 			this.mQueryLimmit = queryLimmit;
 			
 	}
 	
 	public SearchManager getSearchManager()
 	{
 		if(getContext() != null)
 			return (SearchManager)getContext().getSystemService(Context.SEARCH_SERVICE);
 		return null;
 	}
 	
 	public Boolean isCanceled() {
 		return mIsCanceled;
 	}
 	public void setCanceled(Boolean mIsCanceled) {
 		this.mIsCanceled = mIsCanceled;
 	}
 	
 	public SuggestionIndex findSuggestion(int pos)
 	{
 		int i = 0;
 			
 		for (ISuggestion suggestion : getSuggestions()) {
 			if(suggestion.getCount() <= 0)
 			{
 				 continue;
 	
 			}
 			if (i == pos || pos <= i + suggestion.getCount() )
 			{
 				Integer index = null;
 				if (i != pos)
 				{
 					index = i + suggestion.getCount() - pos;
 				}
 				SuggestionIndex sIndex = new SuggestionIndex(suggestion, index);
 				sIndex.setIsCategory(i == pos);
 								
 				return sIndex;
 			}
 			
 			i+= suggestion.getCount() + 1;
 			
 		}
 		return null;
 	}
 	
 	public int getCount() {
 		
 		int count = 0;
 		for (ISuggestion suggestion : this.getSuggestions()) {
 			if(suggestion.getCount() > 0)
 			{
 				count ++;
 				count += suggestion.getCount();
 			}
 		}
 		return count;
 		
 	}
 	//Implementation
 	private void init()
 	{
 		SearchManager sm =getSearchManager(); 
 		if (sm == null)
 		{
 			return;
 		}
 		mSearchables = sm.getSearchablesInGlobalSearch();
 		for (SearchableInfo info : mSearchables) {
 			mSuggestions.add(new SuggestionOfSearchable(this, info));
 		}
 		//TODO: add extra sources here
 	}
 	
 	public class SuggestionIndex
 	{
 		private SuggestionIndex(ISuggestion suggestion, Integer index)
 		{
 			super();
 			setSuggestion(suggestion);
 			setIndex(index);
 		}
 		
 		public ISuggestion getSuggestion() {
 			return suggestion;
 		}
 		private void setSuggestion(ISuggestion suggestion) {
 			this.suggestion = suggestion;
 		}
 		public Integer getIndex() {
 			return index;
 		}
 		private void setIndex(Integer index) {
 			this.index = index;
 		}
 		
 		public Boolean isCategory() {
 			return isCategory;
 		}
 
 		private void setIsCategory(Boolean isCategory) {
 			this.isCategory = isCategory;
 		}
 
 		private ISuggestion suggestion = null;
 		private Integer index = null;
 		private Boolean isCategory = false;
 	}
 	
 		//Data
 		private CharSequence mQuery = null;
 		private Integer mQueryLimmit = null;
 		private List<SearchableInfo> mSearchables = new ArrayList<SearchableInfo>();
 		private List<ISuggestion> mSuggestions = new ArrayList<ISuggestion>();
 		private List<ISuggestionEvents> mEventsListeners = new ArrayList<ISuggestionEvents>();
 		private Context mContext;
 		private Boolean mIsCanceled = false;
 		
 	
 }
