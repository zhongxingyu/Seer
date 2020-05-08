 /**
  * one line to give the program's name and an idea of what it does.
  Copyright (C) 2006  Thomas Walker
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 
 package net.sourceforge.x360mediaserve.formats.tagging;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 
 import net.sourceforge.x360mediaserve.upnpmediaserver.mediaDatabase.fileItems.Tag;
 import entagged.audioformats.AudioFile;
 import entagged.audioformats.AudioFileIO;
 import entagged.audioformats.exceptions.CannotReadException;
 
 public class EntaggedTagger implements Tagger {
 	static Logger logger = Logger.getLogger("x360mediaserve");
 
 	public Tag getTag(File file) {
 		Tag result=new Tag();
 		AudioFile audiofile;
 		try{
 			audiofile=AudioFileIO.read(file);
 			
 			entagged.audioformats.Tag entaggedtag=audiofile.getTag();
 			result.setAlbum(entaggedtag.getFirstAlbum());
 			result.setArtist(entaggedtag.getFirstArtist());
 			result.setTitle(entaggedtag.getFirstTitle());
 			result.setYear(entaggedtag.getFirstYear());
 			result.setSamplerate(audiofile.getSamplingRate());
 			result.setBitrate(audiofile.getBitrate());
 			result.setNumberOfChannels(audiofile.getChannelNumber());
 			
 			try{
        String trackNumber = entaggedtag.getFirstTrack();
        if( trackNumber.indexOf("/") != -1 ) {
          trackNumber = trackNumber.split("/")[0];
        }
        logger.debug("Tracknumber for " + file.toString() + " is " + trackNumber.toString() );
				result.setTracknumber(Integer.parseInt( trackNumber ));
 			}
 			catch(NumberFormatException e){
 				logger.info("No Tracknumber for file "+file.toString());
 			}
 			result.setTime((long)(audiofile.getPreciseLength()*1000.0));			
 		}
 		catch(CannotReadException e){
 			logger.info("Can't Read:"+file.toString()+" "+e.toString());
 		}
 		catch(Exception e){
 			logger.info("Exception for file:"+file.toString()+" "+e.toString());
 		}
 		finally{
 			result.fillBlanks(file);
 		}
 
 		
 		
 		
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 }
