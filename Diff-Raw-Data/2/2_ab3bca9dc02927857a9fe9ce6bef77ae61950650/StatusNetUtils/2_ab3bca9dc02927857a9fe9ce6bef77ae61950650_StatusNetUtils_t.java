 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.util;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mustard.android.Preferences;
 import org.mustard.android.R;
 import org.mustard.geonames.GeoName;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.widget.TextView;
 
 public class StatusNetUtils {
 
 	private static final String STATUSNET_USER_URL = "statusnet://users/";
 	private static final String STATUSNET_GROUP_URL = "statusnet://groups/";
 	private static final String STATUSNET_TAG_URL = "statusnet://tags/";
 	private static final String STATUSNET_SEARCH_URL = "statusnet://search/";
 
 	private static final Pattern NAME_MATCHER = Pattern.compile("\\b\\w+\\b");
 	
	private static final Pattern TAGNAME_MATCHER = Pattern.compile("\\b(\\.?+[a-zA-Z0-9_-])+\\b");
 
 	private static final Linkify.MatchFilter NAME_MATCHER_MATCH_FILTER = new Linkify.MatchFilter() {
 		public final boolean acceptMatch(final CharSequence s, final int start,final int end) {
 			if (start == 0) {
 				return false;
 			}
 			if (start > 1 && !Character.isWhitespace(s.charAt(start - 2))) {
 				return false;
 			}
 			return s.charAt(start - 1) == '@';
 		}
 	};
 
 	private static final Linkify.MatchFilter TAG_MATCHER_MATCH_FILTER = new Linkify.MatchFilter() {
 		public final boolean acceptMatch(final CharSequence s, final int start,final int end) {
 			if (start == 0) {
 				return false;
 			}
 			if (start > 1 && !Character.isWhitespace(s.charAt(start - 2))) {
 				return false;
 			}
 			return s.charAt(start - 1) == '#';
 		}
 	};
 	
 	private static final Linkify.MatchFilter GROUP_MATCHER_MATCH_FILTER = new Linkify.MatchFilter() {
 
 		public final boolean acceptMatch(final CharSequence s, final int start,final int end) {
 			if (start == 0) {
 				return false;
 			}
 			if (start > 1 && !Character.isWhitespace(s.charAt(start - 2))) {
 				return false;
 			}
 			return s.charAt(start - 1) == '!';
 		}
 	};
 	
 	private static final Linkify.TransformFilter TAG_TRASNFORM_FILETER = new Linkify.TransformFilter() {
 
 		@Override
 		public String transformUrl(Matcher match, String url) {
 			return url.replaceAll("[^a-zA-Z0-9]", "");
 		}
 		
 	};
 
 	public static void linkifyUsers(TextView view, long account_id) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_USER_URL+account_id+"/", NAME_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void linkifyTags(TextView view, long  account_id) {
 		Linkify.addLinks(view, TAGNAME_MATCHER, STATUSNET_TAG_URL+account_id+"/", TAG_MATCHER_MATCH_FILTER, TAG_TRASNFORM_FILETER);
 	}
 
 	public static void linkifyGroups(TextView view, long account_id) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_GROUP_URL+account_id+"/", GROUP_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void linkifyGroupsForTwitter(TextView view, long account_id) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_SEARCH_URL+account_id+"/"+"%23", GROUP_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void linkifyTagsForTwitter(TextView view, long account_id) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_SEARCH_URL+account_id+"/"+"%23", TAG_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void linkifyUsers(TextView view) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_USER_URL, NAME_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void linkifyTags(TextView view) {
 		Linkify.addLinks(view, TAGNAME_MATCHER, STATUSNET_TAG_URL, TAG_MATCHER_MATCH_FILTER, TAG_TRASNFORM_FILETER);
 	}
 	
 	public static void linkifyGroups(TextView view) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_GROUP_URL, GROUP_MATCHER_MATCH_FILTER, null);
 	}
 	
 	public static void linkifyGroupsForTwitter(TextView view) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_SEARCH_URL+"%23", GROUP_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void linkifyTagsForTwitter(TextView view) {
 		Linkify.addLinks(view, NAME_MATCHER, STATUSNET_SEARCH_URL+"%23", TAG_MATCHER_MATCH_FILTER, null);
 	}
 
 	public static void openGeoLink(String lon,String lat, int type,Context context) {
 		
 		String url = "";
 		switch(type) {
 		case Preferences.OSM:
 			url="http://www.openstreetmap.org/?lat="+lat+"&lon="+lon+"&zoom=15&layers=B000FTF";
 			break;
 		case Preferences.GOOGLE:	
 			url="http://maps.google.it/maps/ms?ie=UTF8&ll="+lat+","+lon+"&z=12";
 			break;
 		case Preferences.GN:
 			Log.d("Mustard", "GeoId " + lat);
 			url="http://www.geonames.org/"+lat;
 			break;
 		default:
 			url="http://www.geonames.org/"+lat;
 		break;
 		}
 
 		Intent i = new Intent(Intent.ACTION_VIEW);
 		i.setData(Uri.parse(url));
 		context.startActivity(i);
 	}
 	
 	public static Builder getGeoInfo(final Context context, final GeoName gn) {
 		
 		if(gn == null) {
 			return null;
 		}
 		return new AlertDialog.Builder(context)
 		.setTitle(R.string.geo_title)
 		.setMessage(context.getString(R.string.geo_from,gn.getName() + "," + gn.getAdminName1() + "," + gn.getCountryName()))
 				.setPositiveButton(context.getString(R.string.geo_view_on,"OSM"), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						onOpenExternalGeoLink(context,gn.getLng(),gn.getLat(),Preferences.OSM);
 					}
 				})
 				.setNeutralButton(context.getString(R.string.geo_view_on,"GeoNames"), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						onOpenExternalGeoLink(context,"",Long.toString(gn.getGeonameId()),Preferences.GN);
 					}
 				})
 				.setNegativeButton(R.string.close, null);
 	}
 	
 	private static void onOpenExternalGeoLink(Context context, String arg1,String arg2,int type) {
 		StatusNetUtils.openGeoLink(arg1,arg2,type,context);
 	}
 }
