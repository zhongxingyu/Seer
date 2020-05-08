 package com.epam.Suggestions;
 
 public interface ISuggestionEvents {
 	
 boolean OnReloadStart();
 boolean OnReloadFinished();
 boolean OnSuggestionLoaded(ISuggestion suggestion);

 }
