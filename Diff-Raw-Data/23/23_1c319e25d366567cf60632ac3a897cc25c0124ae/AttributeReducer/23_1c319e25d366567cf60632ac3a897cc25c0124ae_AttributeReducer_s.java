 package org.xfon.m.coc.gui;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.xfon.m.coc.Attribute;
 import org.xfon.m.coc.OnAttributeChangedListener;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class AttributeReducer extends TableLayout {	
 	private Context context;	
 	private Set<OnAttributeChangedListener> onAttributeChangedListeners;
 
 	public AttributeReducer(Context context ) {
 		super(context);
 		this.context = context;
 		onAttributeChangedListeners = new HashSet<OnAttributeChangedListener>();		
 		setLayoutParams(new LayoutParams( TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT ) );
 		setColumnStretchable( 2, true );
 	}
 	
 	public void clear() {
 		removeAllViews();
 	}
 	
 	public void addAttributes( final Attribute[] attrs ) {
 		for ( int i = 0; i < attrs.length; i++ ) {
 			addAttribute( attrs[ i ] );
 		}
 	}	
 	
 	public void addAttribute( final Attribute attr ) {
 		final TableRow row;
 		final TextView mTitle;
 		final TextView mValue;
 		final SeekBar mSeekBar;
 						
 		int value = - attr.getMod();
 		
 		row = new TableRow( context );
 		
 		TableLayout.LayoutParams rp = new TableLayout.LayoutParams( TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT );
 		rp.setMargins( 0, 0, 0, 25 );
 		row.setGravity( Gravity.CENTER_VERTICAL );
 		row.setLayoutParams( rp );		
 				
 		mTitle = new TextView( context );
 		mTitle.setText( attr.getName() + ": " );
 		mTitle.setTypeface( Typeface.DEFAULT_BOLD );
 		mTitle.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT ) );
 		
 		mValue = new TextView( context );
 		mValue.setText( "" + value );
 		mValue.setPadding( 20, 0, 0, 0 );
 		mValue.setGravity( Gravity.RIGHT );
 		mValue.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT ) );
 			
 		mSeekBar = new SeekBar( context );
 		mSeekBar.setMax( 5 );
 		mSeekBar.setProgress( value );
 		mSeekBar.setPadding( 20, 0, 0, 0 );
 		TableRow.LayoutParams lp = new TableRow.LayoutParams( TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT );
 		lp.setMargins( 0, 0, 20, 0 );
 		mSeekBar.setLayoutParams( lp );
 		
 		mSeekBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				if ( seekBar != mSeekBar ) return;
 				// Do not reach (or exceed) zero in any attribute
 				if ( progress >= attr.getUnmodifiedValue() ) {
 					seekBar.setProgress( attr.getUnmodifiedValue() - 1 );
 					return;
 				}
 				int value = - progress;
 				mValue.setText( "" + value );
 				attr.setMod( value );		
 				notifyAttributeChangedListeners( attr );				
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				
 			}			
 		} );
 		
 		row.addView( mTitle );
 		row.addView( mValue );
 		row.addView( mSeekBar );
 		addView( row );	
 	}
 	
 	public void addOnAttributeChangedListener( OnAttributeChangedListener listener ) {
 		onAttributeChangedListeners.add( listener );
 	}
 	
 	private void notifyAttributeChangedListeners( Attribute attr ) {
 		Iterator<OnAttributeChangedListener> it = onAttributeChangedListeners.iterator();
 		while ( it.hasNext() ) it.next().attributeChanged( attr );
 	}
 	
 }
