 package com.halcyonwaves.apps.meinemediathek;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Log;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 /**
  * This class parses a XML changelog file and uses CSS to convert it to a nice looking
  * changelog which can be presented to the user after an application upgrade.
  * 
  * The original version of this class was written by Martin van Zuilekom and was modified
  * to fulfill the my personal needs.
  * 
  * @author Martin van Zuilekom
  * @author Tim Huetz
  *
  */
 public class ChangeLogDialog {
 
 	static final private String TAG = "ChangeLogDialog";
 
	private Activity rootActivity = null;
 
 	public ChangeLogDialog( final Activity context ) {
 		this.rootActivity = context;
 	}
 
 	private String getApplicationVersion() {
 		try {
 			final PackageInfo packageInfo = this.rootActivity.getPackageManager().getPackageInfo( this.rootActivity.getPackageName(), 0 );
 			return packageInfo.versionName;
 		} catch( final NameNotFoundException e ) {
 			return "";
 		}
 	}
 
 	private String getChangelogStyleDefinition() {
 		return "<style type=\"text/css\">" + "h1 { margin-left: 0px; margin-bottom: 0px; font-size: 12pt; }" + "h2 { margin-left: 0px; margin-top: 0px; font-size: 8pt; color: #7A7A7A; font-weight: normal; }" + "li { margin-left: 0px; font-size: 9pt;}" + "ul { padding-left: 30px;}" + "</style>";
 	}
 
 	private String getHtmlChangelog() {
 		String _Result = "<html><head>" + this.getChangelogStyleDefinition() + "</head><body>";
 		InputStream rawChangelog = null;
 
 		try {
 			//
 			XmlPullParserFactory xmlPullFactory = XmlPullParserFactory.newInstance();
 			xmlPullFactory.setValidating( false );
 			XmlPullParser _xml = xmlPullFactory.newPullParser();
 			rawChangelog = this.rootActivity.getApplicationContext().getAssets().open( "xml/changelog.xml" );
 			_xml.setInput( rawChangelog, null );
 
 			//
 			int eventType = _xml.getEventType();
 			while( eventType != XmlPullParser.END_DOCUMENT ) {
 				if( (eventType == XmlPullParser.START_TAG) && (_xml.getName().equals( "release" )) ) {
 					_Result = _Result + this.parseChangelogReleaseTag( _xml );
 
 				}
 				eventType = _xml.next();
 			}
 		} catch( final XmlPullParserException e ) {
 			Log.e( ChangeLogDialog.TAG, e.getMessage(), e );
 		} catch( final IOException e ) {
 			Log.e( ChangeLogDialog.TAG, e.getMessage(), e );
 
 		} finally {
 			try {
 				rawChangelog.close();
 			} catch( IOException e ) {
 				// TODO: handle this
 			}
 		}
 		_Result = _Result + "</body></html>";
 		return _Result;
 	}
 
 	private String parseChangelogReleaseTag( final XmlPullParser aXml ) throws XmlPullParserException, IOException {
 		String _Result = "<h1>Version " + aXml.getAttributeValue( null, "version" ) + "</h1><h2>Released on " + aXml.getAttributeValue( null, "releasedate" ) + "</h2><ul>";
 		int eventType = aXml.getEventType();
 		while( (eventType != XmlPullParser.END_TAG) || (aXml.getName().equals( "change" )) ) {
 			if( (eventType == XmlPullParser.START_TAG) && (aXml.getName().equals( "change" )) ) {
 				eventType = aXml.next();
 				_Result = _Result + "<li>" + aXml.getText() + "</li>";
 			}
 			eventType = aXml.next();
 		}
 		_Result = _Result + "</ul>";
 		return _Result;
 	}
 
 	public void show() {
 		final String changelogDialogTitle = "Changelog " + " v" + this.getApplicationVersion(); // TODO: resources
 		final String convertedHtmlChangelog = this.getHtmlChangelog();
 
 		// Get button strings
 		final String _Close = this.rootActivity.getString( android.R.string.ok );
 
 		// Could not load change log, message user and exit void
 		if( convertedHtmlChangelog.equals( "" ) ) {
 			Toast.makeText( this.rootActivity, "Could not load change log", Toast.LENGTH_SHORT ).show();
 			return;
 		}
 
 		// Create webview and load html
 		final WebView _WebView = new WebView( this.rootActivity );
 		_WebView.loadData( convertedHtmlChangelog, "text/html", "utf-8" );
 		final AlertDialog.Builder builder = new AlertDialog.Builder( this.rootActivity ).setTitle( changelogDialogTitle ).setView( _WebView ).setPositiveButton( _Close, new Dialog.OnClickListener() {
 
 			@Override
 			public void onClick( final DialogInterface dialogInterface, final int i ) {
 				dialogInterface.dismiss();
 			}
 		} );
 		builder.create().show();
 	}
 
 }
