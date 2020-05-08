 /****************************************************************************/
 /** This file is part of the Tap for Tap ANE (Air Native Extension)        **/
 /** Author: Pierre-Yves GATOUILLAT (contact [at] impulse12.com)            **/
 /****************************************************************************/
 /** This program is free software: you can redistribute it and/or modify   **/
 /** it under the terms of the GNU General Public License as published by   **/
 /** the Free Software Foundation, either version 3 of the License, or      **/
 /** (at your option) any later version.                                    **/
 /**                                                                        **/
 /** This program is distributed in the hope that it will be useful,        **/
 /** but WITHOUT ANY WARRANTY; without even the implied warranty of         **/
 /** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          **/
 /** GNU General Public License for more details.                           **/
 /**                                                                        **/
 /** You should have received a copy of the GNU General Public License      **/
 /** along with this program.  If not, see <http://www.gnu.org/licenses/>.  **/
 /****************************************************************************/
 
 package com.tapfortap.ane;
 
 import android.app.Activity;
 import android.location.Location;
 import android.view.Gravity;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AbsoluteLayout;
 
 import com.adobe.fre.FREContext;
 import com.adobe.fre.FREFunction;
 import com.adobe.fre.FREObject;
 import com.tapfortap.AdView;
 import com.tapfortap.AdView.Gender;
 import com.tapfortap.TapForTap;
 
 public class TapForTapCreateFunction implements FREFunction
 {
     @Override
 	public FREObject call( FREContext ctx, FREObject[] args )
     {
 		FREObject retVal = null;
 		
 		if ( ((TapForTapExtensionContext)ctx).adView == null )
 		{
 			int i = 0;
 			
 			final int HAS_RECT = 0x01;
 			final int HAS_COLOR = 0x02;
 			final int HAS_GENDER = 0x10;
 			final int HAS_AGE = 0x20;
 			final int HAS_LOCATION = 0x40;
 			
         	Activity activity = ctx.getActivity();
             
             int flags;
         	
             try
             {
             	flags = args[i++].getAsInt();
             	
                 TapForTap.setDefaultAppId( args[i++].getAsString() );
                 TapForTap.checkIn( activity );
     		}
             catch ( Throwable e )
             {
             	TapForTapErrorFunction.errorCode = 0x02;
             	
             	return null;
     		}
             
             AbsoluteLayout al = ((TapForTapExtensionContext)ctx).adViewLayout;
             AbsoluteLayout.LayoutParams lp;
             
             try
             {
             	int x = 0;
             	int y = 0;
             	int w = 300;
             	int h = 50;
             	
             	if ( (flags & HAS_RECT) != 0 )
             	{
                 	x = args[i++].getAsInt();
                 	y = args[i++].getAsInt();
                 	w = args[i++].getAsInt();
                 	h = args[i++].getAsInt();
             	}
             	
                 lp = new AbsoluteLayout.LayoutParams( w, h, x, y );
     		}
             catch ( Throwable e )
             {
             	TapForTapErrorFunction.errorCode = 0x03;
             	
             	return null;
     		}
             
             AdView adView;
             
             try
             {
 	            adView = new AdView( activity );
 	            adView.setLayoutParams( lp );
 	            
             	if ( (flags & HAS_COLOR) != 0 )
             	{
     	            adView.setBackgroundColor( args[i++].getAsInt() );
             	}
             	
             	if ( (flags & HAS_GENDER) != 0 )
             	{
             		String gender = args[i++].getAsString();
             		
                 	if ( gender.equals( "female" ) )
         	            adView.setGender( Gender.FEMALE );
                 	else if ( gender.equals( "male" ) )
                 		adView.setGender( Gender.MALE );
                 	else
            	            adView.setGender( Gender.NONE );
             	}
             	
             	if ( (flags & HAS_AGE) != 0 )
             	{
     	            adView.setAge( args[i++].getAsInt() );
             	}
             	
             	if ( (flags & HAS_LOCATION) != 0 )
             	{
                 	Location location;
                	location = new Location( "Tap for Tap ANE" );
                 	location.setLatitude( args[i++].getAsDouble() );
                 	location.setLongitude( args[i++].getAsDouble() );
                 	location.setAltitude( args[i++].getAsDouble() );
                 	location.setTime( (long)args[i++].getAsDouble() );
                 	
     	            adView.setLocation( location );
             	}
     		}
             catch ( Throwable e )
             {
             	TapForTapErrorFunction.errorCode = 0x04;
             	
             	return null;
     		}
             
             try
             {
             	if(al == null)
             	{
             		al = new AbsoluteLayout( activity );
             		al.addView( adView );
             		            	
             		activity.addContentView( al, new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
             	}
             	else
             	{
             		al.addView( adView );
             	}
     		}
             catch ( Throwable e )
             {
             	TapForTapErrorFunction.errorCode = 0x05;
             	
             	return null;
     		}
             
             try
             {
                 AdView.AdViewListener adViewListener;
                 adViewListener = new AdView.AdViewListener()
                 {
                     @Override
                     public void didReceiveAd()
                     {
                     }
                     
                     @Override
                     public void didFailToReceiveAd( String s )
                     {
                     	TapForTapErrorFunction.errorCode = 0x07;
                     }
                 };
                 
                 adView.setListener( adViewListener );
                 adView.loadAds();
                 
                 ((TapForTapExtensionContext)ctx).adView = adView;
                 ((TapForTapExtensionContext)ctx).adViewLayout = al;
                 
                 retVal = FREObject.newObject( true );
     		}
             catch ( Throwable e )
             {
             	TapForTapErrorFunction.errorCode = 0x06;
             	
             	return null;
     		}
 		}
 		else
 		{
 			TapForTapErrorFunction.errorCode = 0x01;
 		}
         
 		return retVal;
 	}
 }
