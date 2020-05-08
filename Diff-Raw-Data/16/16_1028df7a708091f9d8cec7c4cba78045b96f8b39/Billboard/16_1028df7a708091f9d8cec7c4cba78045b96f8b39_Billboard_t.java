 /*
  * Copyright (C) 2011 Mohamed MOUNIROU
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package com.mmounirou.spotirss.provider;
 
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.mmounirou.spotirss.rss.Track;
 import com.mmounirou.spotirss.tools.StringTools;
 
 public class Billboard implements EntryToTrackConverter
 {
 
 	@Override
 	@Nullable
 	public Track apply(@Nullable String strTitle)
 	{
 		int rankPos = strTitle.indexOf(":");
 		String strRank = strTitle.substring(0, rankPos);
 
		String strArtistName = strTitle.substring(rankPos + 1);
		String strDefaultTitle  = strArtistName.substring(0, strArtistName.lastIndexOf(","));
		String strDefaultArtist = strArtistName.substring(strArtistName.lastIndexOf(",") + 1);

		String strSong = StringUtils.remove(strDefaultTitle, String.format("(%s)", StringUtils.substringBetween(strDefaultTitle, "(", ")")));
		String strArtist = strDefaultArtist;

		final Set<String> artistNames = StringTools.split(strArtist, new String[] { "Featuring", "Feat\\.", "feat\\.", "&", "," });
 
 		return new Track(Integer.parseInt(strRank), artistNames, strSong);
 
 	}
 
 	public static void main(String[] args)
 	{
		System.out.println(new Billboard().apply("35: Work Hard, Play Hard, Wiz Khalifa"));
 	}
 
 }
