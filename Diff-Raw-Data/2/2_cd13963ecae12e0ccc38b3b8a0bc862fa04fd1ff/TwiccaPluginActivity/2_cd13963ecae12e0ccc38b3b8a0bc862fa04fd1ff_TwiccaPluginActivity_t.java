 package com.github.poke.tweeplusreader;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class TwiccaPluginActivity extends Activity
 {
	private static final int AVAILABLE_TEXT_LENGTH = 117;
 	private static final Pattern reRelevantSuffix = Pattern.compile( "(?s)^\\s*\\w+(\\s*@\\w+)*" );
 	
 	@Override
 	protected void onCreate ( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		
 		String intentAction = getIntent().getAction();
 		if ( intentAction.equals( "jp.r246.twicca.ACTION_EDIT_TWEET" ) )
 			handleEditTweet();
 	}
 	
 	private void handleEditTweet ()
 	{
 		Intent intent = getIntent();
 		String editingText = intent.getStringExtra( Intent.EXTRA_TEXT );
 		
 		if ( editingText.length() <= 140 )
 			finish();
 		
 		StringBuilder text = new StringBuilder();
 		StringBuilder suffix = new StringBuilder();
 		
 		// append full prefix
 		text.append( intent.getStringExtra( "prefix" ) );
 		
 		// append relevant suffix
 		Matcher suffixMatcher = reRelevantSuffix.matcher( intent.getStringExtra( "suffix" ) );
 		if ( suffixMatcher.find() )
 		{
 			suffix.append( suffixMatcher.group() );
 			suffix.append( " […]" );
 		}
 		
 		// append inner text
 		String innerText = intent.getStringExtra( "user_input" );
 		int availableLength = Math.max( 0, AVAILABLE_TEXT_LENGTH - text.length() - suffix.length() );
 		
 		if ( availableLength > innerText.length() )
 			text.append( innerText );
 		else
 		{
 			availableLength -= 3;
 			if ( availableLength > 0 )
 			{
 				Matcher m = Pattern.compile( "(?s)^(.{1," + availableLength + "})(?=\\b)" ).matcher( innerText );
 				if ( m.find() )
 					text.append( m.group( 1 ) );
 			}
 			text.append( "[…]" );
 		}
 		
 		// finish text
 		text.append( suffix );
 		text.append( " http://tweeplus.com/#" );
 		text.append( TweePlus.encode( editingText ) );
 		
 		// set result data and finish activity
 		Intent dataIntent = new Intent();
 		dataIntent.putExtra( Intent.EXTRA_TEXT, text.toString() );
 		
 		setResult( RESULT_OK, dataIntent );
 		finish();
 	}
 }
