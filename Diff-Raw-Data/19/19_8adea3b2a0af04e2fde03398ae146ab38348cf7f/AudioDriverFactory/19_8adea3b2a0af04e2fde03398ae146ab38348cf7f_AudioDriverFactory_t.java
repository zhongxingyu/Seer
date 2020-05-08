 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.audio;
 
 import se.chalmers.dat255.sleepfighter.audio.playlist.PlaylistDriver;
 import se.chalmers.dat255.sleepfighter.audio.playlist.PlaylistProviderFactory;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioSource;
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * AudioDriverFactory produces AudioDriver:s.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 27, 2013
  */
 public class AudioDriverFactory {
 	/**
 	 * Constructs a AudioDriverFactory.
 	 */
 	public AudioDriverFactory() {
 	}
 
 	/**
 	 * Produces an AudioDriver given a context and an AudioSource.
 	 *
 	 * @param context android context.
 	 * @param source audio source.
 	 * @return the produced AudioDriver.
 	 */
 	public AudioDriver produce( Context context, AudioSource source ) {
 		AudioDriver driver = null;
 
 		if ( source == null ) {
 			driver = new SilentAudioDriver();
 		} else {
 			Log.d( "AudioDriverFactory", source.toString() );
 
 			switch ( source.getType() ) {
 			case RINGTONE:
 				driver = new RingtoneDriver();
 				break;
 			case PLAYLIST:
 				driver = new PlaylistDriver( new PlaylistProviderFactory() );
 				break;
 			case LOCAL_CONTENT_URI:
 				driver = new LocalContentDriver();
 				break;
 			case INTERNET_STREAM:
 			case SPOTIFY:
 				Toast.makeText( context, "NOT IMPLEMENTED YET!", Toast.LENGTH_LONG ).show();
 				driver = new SilentAudioDriver();
 				break;
			default:
				throw new IllegalArgumentException("This should not happen");
 			}
 		}
 
 		driver.setSource( context, source );
 		return driver;
 	}
 }
