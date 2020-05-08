 /* Copyright (c) 2009 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package se.fnord.xmms2.client.internal;
 
 
 public enum IpcSignal {
 	PLAYLIST_CHANGED,
 	CONFIGVALUE_CHANGED,
 	PLAYBACK_STATUS,
 	OUTPUT_VOLUME_CHANGED,
 	OUTPUT_PLAYTIME,
 	OUTPUT_CURRENTID,
	OUTPUT_OPEN_FAIL,
 	PLAYLIST_CURRENT_POS,
 	PLAYLIST_LOADED,
 	MEDIALIB_ENTRY_ADDED,
 	MEDIALIB_ENTRY_UPDATE,
 	COLLECTION_CHANGED,
 	QUIT,
 	MEDIAINFO_READER_STATUS,
 	MEDIAINFO_READER_UNINDEXED,
 	END;
 
     private static final OrdinalMap<IpcSignal> ordinal_map = OrdinalMap.populate(IpcSignal.class);
 
 	public static IpcSignal fromOrdinal(int o) {
 		return ordinal_map.get(o);
 	}
 
 }
