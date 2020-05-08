 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.lastcrusade.soundstream.net.message;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.lastcrusade.soundstream.model.SongMetadata;
 import com.lastcrusade.soundstream.net.message.LibraryMessage;
 
 public class LibraryMessageTest extends SerializationTest<LibraryMessage> {
 	
 	@Test
 	public void testSerializeLibraryMessage() throws Exception {
 		List<SongMetadata> library = populateTestLibrary();
 		
 		LibraryMessage preSerializationLibraryMessage = new LibraryMessage(library);
 		LibraryMessage postSerializationLibraryMessage = super.testSerializeMessage(preSerializationLibraryMessage);
 		
 		for(int i = 0; i < library.size(); i++) {
 			SongMetadata preSerializationSongMetadata = library.get(i);
 			SongMetadata postSerializationSongMetadata = postSerializationLibraryMessage.getLibrary().get(i);
 			
 			assertEquals(preSerializationSongMetadata.getId(), postSerializationSongMetadata.getId());
 			assertEquals(preSerializationSongMetadata.getTitle(), postSerializationSongMetadata.getTitle());
 			assertEquals(preSerializationSongMetadata.getArtist(), postSerializationSongMetadata.getArtist());
 			assertEquals(preSerializationSongMetadata.getAlbum(), postSerializationSongMetadata.getAlbum());
 			assertEquals(preSerializationSongMetadata.getMacAddress(), postSerializationSongMetadata.getMacAddress());
 		}
 	}
 
 	public List<SongMetadata> populateTestLibrary() {
         List<SongMetadata> library = new ArrayList<SongMetadata>(Arrays.asList(
                 new SongMetadata(69, "Driver that Had a Dick on His Shoulder",
                         "Aziz Ansari", "Dangerously Delicious", 2345, "David"),
                 new SongMetadata(1, "Lady with the Puppies", null,
                         "Dangerously Delicious", 23462346, "David"),
                 new SongMetadata(23, "Toronto Customs Lady", "Aziz Ansari",
                         null, 3423462, "David"), 
                 new SongMetadata(42, "Motley Crue Tour vs. Aziz Tour", "Aziz Ansari",
                         "Dangerously Delicious", 2346236, null),
                 //NOTE: this id is specifically chosen to make sure that readLong/writeLong
                 // will handle encoded bytes that are negative
                 new SongMetadata(59916, "Numbers are Fun", "Jesse and Reid",
                        "The WTF Tour", 2346236, null)));
         return library;
     }
 }
