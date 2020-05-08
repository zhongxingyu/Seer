 package com.github.poke.tweeplusreader;
 
 import java.util.regex.Matcher;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.Spanned;
 import android.text.style.ForegroundColorSpan;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.TextView.BufferType;
 
 /**
  * Main activity that decodes the text from a tweeplus.com intent and
  * automatically shows the decoded text as a overlay.
  */
 public class TweePlusReader extends Activity
 {
 	private static final int[] COLORS = new int[] { 0xFFFF5500, 0xFFFF1493, 0xFF00AAFF, 0xFF99DD11 };
 	
 	private TextView titleView;
 	private TextView textView;
 	private ViewGroup buttons;
 	private int currentColor;
 	private String text;
 	
 	@Override
 	public void onCreate ( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		setContentView( R.layout.main );
 		
 		titleView = (TextView) findViewById( R.id.title );
 		textView = (TextView) findViewById( R.id.text );
 		buttons = (ViewGroup) findViewById( R.id.buttons );
 		
 		// randomly select color
 		// TODO Animate color.
 		currentColor = COLORS[(int) ( Math.random() * 4 )];
 		titleView.setTextColor( currentColor );
 		
 		// parse intent URI
 		if ( getIntent().getDataString() != null )
			decodeAndShow( getIntent().getData().getFragment() );
 	}
 	
 	/**
 	 * Decode the given text and show it on screen.
 	 * 
 	 * @param encodedText the text to decode.
 	 */
 	private void decodeAndShow ( String encodedText )
 	{
 		text = TweePlus.decode( encodedText );
 		textView.setText( text, BufferType.SPANNABLE );
 		Spannable span = (Spannable) textView.getText();
 		
 		// find mentions, hashtags or links
 		clearButtons();
 		Matcher m = TweePlus.createLinkMatcher( text );
 		while ( m.find() )
 		{
 			span.setSpan( new ForegroundColorSpan( currentColor ), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
 			String link = m.group( 1 );
 			
 			switch ( link.charAt( 0 ) )
 			{
 				case TweePlus.CHAR_MENTION:
 					addButton( link, "http://twitter.com/" + link.substring( 1 ) );
 					break;
 				
 				case TweePlus.CHAR_HASHTAG:
 					addButton( link, "http://twitter.com/search/%23" + link.substring( 1 ) );
 					break;
 				
 				default:
 					addButton( link, link );
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * Adds a button to the button bar.
 	 * 
 	 * @param title the button title.
 	 * @param link the link that will be opened when the button is pressed.
 	 */
 	private void addButton ( final String title, final String link )
 	{
 		Button btn = new Button( this, null, android.R.attr.buttonStyleSmall );
 		btn.setLayoutParams( new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );
 		btn.setText( title );
 		btn.setOnClickListener( new OnClickListener()
 		{
 			public void onClick ( View v )
 			{
 				startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( link ) ) );
 			}
 		} );
 		
 		buttons.addView( btn );
 	}
 	
 	/**
 	 * Remove all buttons from the button bar.
 	 */
 	private void clearButtons ()
 	{
 		buttons.removeAllViews();
 	}
 }
