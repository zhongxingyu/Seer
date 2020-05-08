 package at.linuxhacker.weightassistant;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class WeightMeasurmentSeries {
 	private DbHelper dbHelper;
 	private SQLiteDatabase db;
 	public List<MeasuringPoint> measurmentSeries;
 	private Hashtable<String, WeeklyStatistic> weeklyHash;
 	
 	WeightMeasurmentSeries( Context context ) {
 		this.dbHelper = new DbHelper( context );
 		this.db = dbHelper.getReadableDatabase( );		
 	}
 	
 	void readAll( ) {
 		this.measurmentSeries = new ArrayList<MeasuringPoint>( );
 		this.weeklyHash = new Hashtable<String, WeeklyStatistic>( );
 		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
 		Date myDate = null;
 		
		Cursor cur = this.db.query( this.dbHelper.TABLE, 
				new String[] { this.dbHelper.C_ID, 
				this.dbHelper.C_DATETIME, this.dbHelper.C_GEWICHT },
 				null, null, null, null, null );
 		try {
 			cur.moveToFirst( );
 			int rowCount = cur.getCount( );
 			for( int i = 0; i < rowCount; i++ ) {
 				String tmp =  cur.getString( 1 );
 				myDate = dateFormat.parse( tmp );
 				MeasuringPoint point = new MeasuringPoint( myDate, cur.getDouble( 2 ) );
 				this.measurmentSeries.add( point );
 				this.addPointToWeekly( point );
 				cur.moveToNext( );
 			}
 		} catch ( Exception e ) {
 			System.out.println ("Exception in readAll: " + e );
			Exception y;
			y = e;
 		} finally {
 			cur.close( );
 		}
 	}
 	
 	List<WeeklyStatistic> getWeeklyStatisticList( ) {
 		List<WeeklyStatistic> weeklyStatisticList = new ArrayList<WeeklyStatistic>( );
 		
 		String[] keys = ( String[] ) this.weeklyHash.keySet( ).toArray( new String[0] );
 		Arrays.sort( keys );
 		for( String key : keys ) {
 			weeklyStatisticList.add( this.weeklyHash.get( key ) );
 		}
 		return weeklyStatisticList;
 	}
 	
 	protected void addPointToWeekly( MeasuringPoint point ) {
 		String weekOfYear = point.getWeekOfYear( );
 		
 		if ( this.weeklyHash.get( weekOfYear ) == null ) {
 			this.weeklyHash.put( weekOfYear, new WeeklyStatistic( weekOfYear ) );
 		}
 		try {
 			System.out.println( "Add: " + weekOfYear );
 			this.weeklyHash.get( weekOfYear ).addMeasurePoint( point );
 		} catch( Exception e ) {
			// FIXME
			Exception y;
			y = e;
 		}
 	}
 	
 	
 }
