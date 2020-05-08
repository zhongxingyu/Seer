 package com.chips;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 
 public abstract class gsCalendar //extends Activity 
 {
 
 
 	final static int ROWS = 7 ; 
 	final static int COLS = 7 ; 
 	
 //	final static int ROWS = 4 ; 
 //	final static int COLS = 14 ; 
 //	
 	
 	Context m_context ;					
 	LinearLayout m_targetLayout ;	
 	Button [] m_controlBtn ;		
 	TextView [] m_viewTv ;	
 	
 	Calendar m_Calendar ;			
 	
 	LinearLayout [ ] m_lineLy ;		
     LinearLayout [ ] m_cellLy ;		
     TextView [ ] m_cellTextBtn ;	
     
     LinearLayout [ ] m_horizontalLine ; 
     LinearLayout [ ] m_verticalLine ;	
     
     int m_startPos ;				
     int m_lastDay ;					
     int m_selDay ;					
         
     float m_displayScale ;			
     float m_textSize ;				
     float m_topTextSize ;			
     
     int m_tcHeight = 50 ;			
     int m_cWidth = 50 ;				
     int m_cHeight = 50 ;			
     int m_lineSize = 1 ;			
     
     static public class gsCalendarColorParam
     {
     	int m_lineColor 			= 0xff000000 ;	
         int m_cellColor 			= 0xffffffff ;	
         int m_topCellColor 			= 0xffcccccc ;	
         int m_textColor 			= 0xff000000 ;	
         int m_sundayTextColor 		= 0xffff0000 ;
         int m_saturdayTextColor 	= 0xff0000ff ;	
         int m_topTextColor 			= 0xff000000 ; 	
         int m_topSundayTextColor 	= 0xffff0000 ; 	
         int m_topSaturdatTextColor 	= 0xff0000ff ; 	
         
         int m_todayCellColor		= 0x999999ff ;	
         int m_todayTextColor		= 0xffffffff ;  
     }
     
     gsCalendarColorParam m_colorParam ;
     
     Drawable m_bgImgId = null ;				
     Drawable m_cellBgImgId = null ;			
     Drawable m_topCellBgImgId = null ;		
     
     Drawable m_todayCellBgImgId = null ; 	
     
     String [] m_dayText ={ "Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat",} ; 
     
     
     Button m_preYearBtn ;			
     Button m_nextYearBtn ;			
     Button m_preMonthBtn ;			
     Button m_nextMonthBtn ;			
     
     TextView m_yearTv ;				
     TextView m_mothTv ;				
     TextView m_dayTv ;		    
     
     ArrayList< Integer > m_holiDay = new ArrayList< Integer >( ) ;
     
 	
 	public gsCalendar( Context context, LinearLayout layout )
 	{
 		m_context = context ;
 		
 		m_targetLayout = layout ;
 		
 		m_Calendar = Calendar.getInstance( ) ;
 		
 		m_lineLy = new LinearLayout[ COLS ] ;						
         m_cellLy = new LinearLayout[ COLS * ROWS ] ;				
         m_cellTextBtn = new TextView[ COLS * ROWS ] ;				
         m_horizontalLine = new LinearLayout[ COLS - 1 ] ; 			
         m_verticalLine = new LinearLayout[ ( COLS - 1 ) * ROWS ] ;
         
         m_displayScale = context.getResources( ).getDisplayMetrics( ).density ;
         
         m_topTextSize = m_displayScale * 12.0f ;
         m_textSize = m_displayScale * 12.0f ;
         
         m_colorParam = new gsCalendarColorParam( ) ;
         
         
 //        ROWS = 7 ;
 //    	COLS = 14 ; 
 	}
 	
 	public void initCalendar( )
 	{
 		createViewItem( ) ;
         setLayoutParams( ) ;
         setLineParam( ) ;
         setContentext( ) ;
         setOnEvent( ) ;
         printView( ) ;
 	}
 	
 	public void setColorParam( gsCalendarColorParam param )
 	{
 		m_colorParam = param ;
 	}
 	
 	public void setBackground( Drawable bg )
 	{
 		m_bgImgId = bg ;				
 	}
 	public void setCellBackground( Drawable bg )
 	{
 	    m_cellBgImgId = bg ;			
 	}
 	public void setTopCellBackground( Drawable bg )
 	{
 		m_topCellBgImgId = bg ;			
 	}
 	
 	public void setCalendarSize( int width, int height  )
 	{
 		m_cWidth = ( width - ( m_lineSize * COLS - 1 ) ) / COLS ;
         m_cHeight = ( height - ( m_lineSize * ROWS - 1 ) ) / ROWS ;
         m_tcHeight = ( height - ( m_lineSize * COLS - 1 ) ) / COLS ;
 	}
 	
 	public void setCellSize( int cellWidth, int cellHeight, int topCellHeight  )
 	{
 		m_cWidth = cellWidth ;
         m_cHeight = cellHeight ;
 		m_tcHeight = topCellHeight ;
 	}
 	
 	public void setTopCellSize( int topCellHeight  )
 	{
 		m_tcHeight = topCellHeight ;
 	}
 	
 	public void setCellSize( int allCellWidth, int allCellHeight )
 	{
 		m_cWidth = allCellWidth ;
         m_cHeight = allCellHeight ;
 		m_tcHeight = allCellHeight ;
 	}
 	
 	public void setTextSize( float size )
 	{
 		m_topTextSize = m_displayScale * size ;
         m_textSize = m_displayScale * size ;
 	}
 	
 	public void setTextSize( float textSize, float topTextSize )
 	{
 		m_topTextSize = m_displayScale * topTextSize ;
         m_textSize = m_displayScale * textSize ;
 	}
 	
 	
 	public void redraw( )
 	{
 		m_targetLayout.removeAllViews( ) ;
 		initCalendar( ) ;
 		
 	}
 	
 
 	public void setSelectedDay( int cellColor, int textColor )
 	{
 		m_colorParam.m_todayCellColor = cellColor ;
 		m_colorParam.m_todayTextColor = textColor ;
 		m_cellTextBtn[ m_Calendar.get( Calendar.DAY_OF_MONTH ) + m_startPos - 1 ].setTextColor( textColor ) ;
 		m_cellTextBtn[ m_Calendar.get( Calendar.DAY_OF_MONTH ) + m_startPos - 1 ].setBackgroundColor( cellColor ) ;
 	}
 	
 	public void setSelectedDayTextColor( int textColor )
 	{
 		m_colorParam.m_todayTextColor = textColor ;
 		m_cellTextBtn[ m_Calendar.get( Calendar.DAY_OF_MONTH ) + m_startPos - 1 ].setTextColor( textColor ) ;
 	}
 	
 	public void setSelectedDay( Drawable bgimg )
 	{
 		m_todayCellBgImgId = bgimg ;
 		m_colorParam.m_todayCellColor = 0x00000000 ;
 		m_cellTextBtn[ m_Calendar.get( Calendar.DAY_OF_MONTH ) + m_startPos - 1 ].setBackgroundDrawable( bgimg ) ;
 	}
 	
 	
 	public void addHoliday( int holiday_MMdd )
 	{
 		m_holiDay.add( holiday_MMdd ) ;
 	}
 	
 	public void applyHoliday( )
 	{
 		Integer iMonth = m_Calendar.get( Calendar.MONTH ) + 1 ;
 		
 		for( int k = 0 ; k < m_holiDay.size( ) ; k++ )
 		{
 			int holiday = m_holiDay.get( k ) ;
 			if( holiday / 100 == iMonth )		
 			{
 				m_cellTextBtn[ holiday % 100 + m_startPos ].setTextColor( m_colorParam.m_sundayTextColor ) ;
 			}
 		}
 	}
 	
 	
 
 	public void createViewItem( )
 	{
         for( int i = 0 ; i < ROWS * 2 - 1 ; i++ )
         {
         	if( i % 2 == 0 )
         	{
 	        	m_lineLy[i/2] = new LinearLayout( m_context ) ;
 	        	m_targetLayout.addView( m_lineLy[i/2] ) ; 
         	
 	        	for( int j = 0 ; j < COLS * 2 - 1 ; j++ )
 	        	{
 	        		
 	        		if( j % 2 == 0 )
 		        	{
 	        			int pos = ( ( i / 2 ) * COLS ) + ( j / 2 ) ;
 	        			
 		        		m_cellLy[ pos ] = new LinearLayout( m_context ) ;
 		        		m_cellTextBtn[ pos ] = new TextView( m_context ) ;
 		        		m_lineLy[ i / 2 ].addView( m_cellLy[ pos ] ) ;
 		        		m_cellLy[ pos ].addView( m_cellTextBtn[ pos ] ) ;
 		        		
 		        	}
 	        		else	
 	        		{
 	        			int pos = ( ( i / 2 ) * (COLS - 1) ) + ( j - 1 ) / 2 ;
 	        			
 	        			m_verticalLine[ pos ] = new LinearLayout( m_context ) ;
 		        		m_lineLy[ i / 2 ].addView( m_verticalLine[ pos ] ) ;
 	        		}
 	        	}
         	}
         	else	
         	{
         		m_horizontalLine[ ( i - 1 ) / 2 ] = new LinearLayout( m_context ) ;
 	        	m_targetLayout.addView( m_horizontalLine[ ( i - 1 ) / 2 ] ) ;
 	        	
 	        	
         	}
         }
 	}
 	
 	public void setLayoutParams( )
 	{
 		m_targetLayout.setOrientation( LinearLayout.VERTICAL ) ;
 		if( m_bgImgId != null )
 		{
 			m_targetLayout.setBackgroundDrawable( m_bgImgId ) ;
 		}
 		
 		for( int i = 0 ; i < ROWS * 2 - 1 ; i++ )
 		{
 			if( i % 2 == 0 )
         	{
 				m_lineLy[i/2].setOrientation( LinearLayout.HORIZONTAL ) ;
 				m_lineLy[i/2].setLayoutParams(	
 						new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) ) ;
 	        	
 	        	for( int j = 0 ; j < COLS ; j++ )
 	        	{
 	        		int cellnum = ( ( i / 2 ) * COLS ) + j ;
 	        		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) ;
 	        		//param.setMargins( 1, 1, 1, 1 ) ;	
 	        		m_cellLy[ cellnum ].setLayoutParams( param ) ;
 	        		m_cellTextBtn[ cellnum ].setGravity( Gravity.CENTER ) ;
 	        		
 	        			        		
 	        		
 	        		if( i == 0 )	
 	        		{
 	        			m_cellTextBtn[ cellnum ].setLayoutParams( new LinearLayout.LayoutParams( m_cWidth, m_tcHeight ) ) ;
 	        			
 	        			if( m_topCellBgImgId != null )
 	        			{
 	        				m_cellLy[ cellnum ].setBackgroundDrawable( m_topCellBgImgId ) ;
 	        			}
 	        			else
 	        			{
 	        				m_cellLy[ cellnum ].setBackgroundColor( m_colorParam.m_topCellColor ) ;
 	        			}
 	        			
 	            		switch( j )
 	    	    		{
 	    	    		case 0:
 	    	    			m_cellTextBtn[ cellnum ].setTextColor( m_colorParam.m_topSundayTextColor ) ;
 	    	    			break ;
 	    	    		case 6:
 	    	    			m_cellTextBtn[ cellnum ].setTextColor( m_colorParam.m_topSaturdatTextColor ) ;
 	    	    			break ;
 	    	    		default:
 	    	    			m_cellTextBtn[ cellnum ].setTextColor( m_colorParam.m_topTextColor ) ;
 	    	    			break ;
 	    	    		}
 	            		
 	            		m_cellTextBtn[ cellnum ].setTextSize( m_topTextSize ) ;
 	        		}
 	        		else
 	        		{
 	        		
 	        			m_cellTextBtn[ cellnum ].setLayoutParams( new LinearLayout.LayoutParams( m_cWidth, m_cHeight ) ) ;
 	        			
 	        			if( m_cellBgImgId != null )
 	        			{
 	        				m_cellLy[ cellnum ].setBackgroundDrawable( m_cellBgImgId ) ;
 	        			}
 	        			else
 	        			{
 	        				m_cellLy[ cellnum ].setBackgroundColor( m_colorParam.m_cellColor ) ;
 	        			}
 	        			
 	            		switch( j )
 	    	    		{
 	    	    		case 0:
 	    	    			m_cellTextBtn[ cellnum ].setTextColor( m_colorParam.m_sundayTextColor ) ;
 	    	    			break ;
 	    	    		case 6:
 	    	    			m_cellTextBtn[ cellnum ].setTextColor( m_colorParam.m_saturdayTextColor ) ;
 	    	    			break ;
 	    	    		default:
 	    	    			m_cellTextBtn[ cellnum ].setTextColor( m_colorParam.m_textColor ) ;
 	    	    			break ;
 	    	    		}
 	            		
 	            		m_cellTextBtn[ cellnum ].setTextSize( m_textSize ) ;
 	        		}
 	        		
 	        		
 	        	}
         	}
 		}
 	}
 	
 	public void setLineParam( )
 	{
 		for( int i = 0 ; i < ROWS - 1 ; i ++ )
 		{
 			m_horizontalLine[ i ].setBackgroundColor( m_colorParam.m_lineColor ) ;
 			m_horizontalLine[ i ].setLayoutParams(
 						new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, m_lineSize ) ) ;
 		}
 		for( int i = 0 ; i < ROWS ; i ++ )
 		{
 			for( int j = 0 ; j < COLS - 1 ; j++ )
 	    	{
 	    		int pos = ( i * ( COLS - 1 ) ) + j ;
 	    		m_verticalLine[ pos ].setBackgroundColor( m_colorParam.m_lineColor ) ; 
 	    		m_verticalLine[ pos ].setLayoutParams(	 
 						new LinearLayout.LayoutParams( m_lineSize, LayoutParams.FILL_PARENT ) ) ;
 	    	}
 		}
 	}
 	
 	public void setContentext( )
 	{
 		Calendar iCal = (Calendar) m_Calendar.clone( ) ;
 		
 		m_selDay = iCal.get( Calendar.DATE ) ;
 		
 		iCal.set( Calendar.DATE, 1 ) ;
 		m_startPos = COLS + iCal.get( Calendar.DAY_OF_WEEK ) - Calendar.SUNDAY ;
 		
 		iCal.add( Calendar.MONTH, 1 ) ;
 		iCal.add( Calendar.DATE, -1 ) ;
 		
         m_lastDay = iCal.get( Calendar.DAY_OF_MONTH ) ;         
         
 		for( int k = 0 ; k < COLS ; k++ )
     	{
 			m_cellTextBtn[ k ].setText(  m_dayText[ k % 7 ] ) ;
     	}
 		
 		for( int i = COLS ; i < m_startPos ; i++ )
 		{
 			m_cellTextBtn[ i ].setText( "" ) ;
 		}
 
 		for( int i = 0 ; i < m_lastDay ; i++ )
 		{
 			m_cellTextBtn[ i + m_startPos ].setText( ( i + 1 ) + "" ) ;
 		}
 		
 		for( int i = m_startPos + m_lastDay ; i < COLS * ROWS ; i++ )
 		{
 			m_cellTextBtn[ i ].setText( "" ) ;
 		}
 	}
 	
 	public void setOnEvent( )
 	{
 		for( int i = COLS ; i < COLS * ROWS ; i++ )
 		{
 			final int k = i ;
 			m_cellTextBtn[i].setOnClickListener( new Button.OnClickListener( ) 
 			{
 				@Override
 				public void onClick(View v) 
 				{
 					
 					if( m_cellTextBtn[k].getText( ).toString( ).length() > 0 )
 					{
 						m_Calendar.set( Calendar.DATE, Integer.parseInt( m_cellTextBtn[k].getText( ).toString( ) ) ) ;
 						if( m_dayTv != null )
 			    			m_dayTv.setText( m_Calendar.get( Calendar.DAY_OF_MONTH ) + "" ) ;
 						printView( ) ;
 						myClickEvent( 	m_Calendar.get( Calendar.YEAR ),
 										m_Calendar.get( Calendar.MONTH ),
 										m_Calendar.get( Calendar.DAY_OF_MONTH ) ) ;
 					}
 				}
 			} ) ;
 		}
 	}
 	
 	public void printView( )
 	{
 		if( m_yearTv != null )
 			m_yearTv.setText( m_Calendar.get( Calendar.YEAR ) + "" ) ;
 		if( m_mothTv != null )
 		{
 			String monthString = "";
 			switch(m_Calendar.get( Calendar.MONTH ) + 1)
 			{
 				case 1:  monthString = "January";
 					break;
 				
 				case 2:  monthString = "February";
 					break;
 				
 				case 3:  monthString = "March";
 					break;
 
 				case 4:  monthString = "April";
 					break;
 
 				case 5:  monthString = "May";
 					break;
 
 				case 6:  monthString = "June";
 					break;
 
 				case 7:  monthString = "July";
 					break;
 				
 				case 8:  monthString = "August";
 					break;
 
 				case 9:  monthString = "September";
 					break;
 				
 				case 10: monthString = "October";
 					break;
 				
 				case 11: monthString = "November";
 					break;
 
 				case 12: monthString = "December";
 					break;
 			}
 			//m_mothTv.setText( ( m_Calendar.get( Calendar.MONTH ) + 1 ) + "" ) ;
 			m_mothTv.setText( monthString + "" ) ;
 		}
 		if( m_dayTv != null )
 			m_dayTv.setText( m_Calendar.get( Calendar.DAY_OF_MONTH ) + "" ) ;
 
 	} 
 	
 	public void preYear( )
 	{
 		m_Calendar.add( Calendar.YEAR, -1 ) ;
 		setContentext( ) ;
 		printView( ) ;
 	}
 	public void nextYear( )
 	{
 		m_Calendar.add( Calendar.YEAR, 1 ) ;
 		setContentext( ) ;
 		printView( ) ;
 	}
 	public void preMonth( )
 	{
 		m_Calendar.add( Calendar.MONTH, -1 ) ;
 		setContentext( ) ;
 		printView( ) ;
 	}
 	public void nextMonth( )
 	{
 		m_Calendar.add( Calendar.MONTH, 1 ) ;
 		setContentext( ) ;
 		printView( ) ;
 	}    	
 	
 	public void setViewTarget( TextView [] tv ) 
 	{
 		m_yearTv = tv[0] ;
 		m_mothTv = tv[1] ;
         m_dayTv = tv[2] ;
 	}
         
 	public void setControl( Button [] btn )
 	{
 		m_preYearBtn = btn[0] ;
         m_nextYearBtn = btn[1] ;
         m_preMonthBtn = btn[2] ;
         m_nextMonthBtn = btn[3] ;
         
         if( m_preYearBtn != null )
            m_preYearBtn.setOnClickListener( new Button.OnClickListener( ) 
            {
 				@Override
 				public void onClick(View v) 
 				{
 					preYear( ) ;
 				}
 			} ) ;
         if( m_nextYearBtn != null )
             m_nextYearBtn.setOnClickListener( new Button.OnClickListener( ) 
             {
 				@Override
 				public void onClick(View v) 
 				{
 					nextYear( ) ;
 				}
 			} ) ;
         if( m_preMonthBtn != null )
             m_preMonthBtn.setOnClickListener( new Button.OnClickListener( ) 
             {
 				@Override
 				public void onClick(View v) 
 				{
 					preMonth( ) ;
 				}
 			} ) ;
         if( m_nextMonthBtn != null )
             m_nextMonthBtn.setOnClickListener( new Button.OnClickListener( ) 
             {
 				@Override
 				public void onClick(View v) 
 				{
 					nextMonth( ) ;
 				}
 			} ) ;
 	}
 	
 	public String getData( String format )
 	{
 		SimpleDateFormat sdf = new SimpleDateFormat( format, Locale.US ) ;
 		return sdf.format( new Date( m_Calendar.getTimeInMillis( ) ) ) ;
 	}
 	
 	public abstract void myClickEvent( int yyyy, int MM, int dd );
 	
     
 	public int pixelToDip( int arg )
 	{
 		m_displayScale = m_context.getResources( ).getDisplayMetrics( ).density ;
 		return (int) ( arg * m_displayScale ) ;
 	}
 }
 
