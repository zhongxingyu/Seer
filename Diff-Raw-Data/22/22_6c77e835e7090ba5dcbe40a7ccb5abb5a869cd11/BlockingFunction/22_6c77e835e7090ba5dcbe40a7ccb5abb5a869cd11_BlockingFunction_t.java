 package de.uni_potsdam.hpi.fgnaumann.lsdd;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.codec.language.Soundex;
 
 import de.uni_potsdam.hpi.fgnaumann.lsdd.util.AsciiUtils;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.util.UnicodeUtils;
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactString;
 
 public abstract class BlockingFunction {
 	
 	static final @SuppressWarnings({ "serial" })
 	public
 	Set<BlockingFunction> blockingFuntions = new HashSet<BlockingFunction>() {
 		{
 			add(new BlockingFunction() {
 				@Override
 				public PactString getID() {
 					return new PactString("SoundexArtistLast3Title");
 				}
 
 				@Override
 				PactString explode(PactRecord record) {
 					PactString artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class);
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(artist);
 					UnicodeUtils.normalizeUnicode(title);
 					Soundex soundex = new Soundex();
 					PactString blockingKey = new PactString(
 							soundex.soundex(artist.getValue()) + title.getValue());
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 
 				@Override
 				PactString function(PactRecord record) {
 					PactString artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class);
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(artist);
 					UnicodeUtils.normalizeUnicode(title);
 					int length = title.length();
 					String s_title = length > 4 ? title.getValue().substring(length-4, length-1) : title.getValue();
 					Soundex soundex = new Soundex();
 					PactString blockingKey = new PactString(
 							soundex.soundex(artist.getValue())+s_title);
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 			});
 			add(new BlockingFunction() {
 				@Override
 				public PactString getID() {
 					return new PactString("SoundexDiscTitleLast3Title");
 				}
 
 				@Override
 				PactString explode(PactRecord record) {
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(title);
 					Soundex soundex = new Soundex();
 					PactString blockingKey = new PactString(
 							soundex.soundex(title.getValue()) + title.getValue());
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 
 				@Override
 				PactString function(PactRecord record) {
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(title);
 					int length = title.length();
 					String s_title = length > 4 ? title.getValue().substring(length-4, length-1) : title.getValue();
 					Soundex soundex = new Soundex();
 					PactString blockingKey = new PactString(
 							soundex.soundex(title.getValue())+s_title);
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 			});
 //			add(new BlockingFunction() {
 //				@Override
 //				public PactString getID() {
 //					return new PactString("SoundexDiscTitleFirst3Title");
 //				}
 //
 //				@Override
 //				PactString explode(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title) + title);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //
 //				@Override
 //				PactString function(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					int length = title.length();
 //					String s_title = length > 4 ? title.substring(0, 3) : title;
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title)+s_title);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			});
 //			add(new BlockingFunction() {
 //				@Override
 //				public PactString getID() {
 //					return new PactString("SoundexDiscTitle-Artist");
 //				}
 //
 //				@Override
 //				PactString explode(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					String artist = record
 //							.getField(MultiBlocking.ARTIST_NAME_FIELD,
 //									PactString.class).getValue();
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title) + artist);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //
 //				@Override
 //				PactString function(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title));
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			});
 //			add(new BlockingFunction() {
 //				@Override
 //				public PactString getID() {
 //					return new PactString("SoundexDiscTitle-Title");
 //				}
 //
 //				@Override
 //				PactString explode(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title) + title);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //
 //				@Override
 //				PactString function(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title));
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			});
 //			add(new BlockingFunction() {
 //				@Override
 //				public PactString getID() {
 //					return new PactString("SoundexDiscTitle-First3Artist");
 //				}
 //
 //				@Override
 //				PactString explode(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					String artist = record
 //							.getField(MultiBlocking.ARTIST_NAME_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					int length = artist.length();
 //					String s_artist = length > 4 ? artist.substring(0, 3) : artist;
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title) + s_artist);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //
 //				@Override
 //				PactString function(PactRecord record) {
 //					String title = record
 //							.getField(MultiBlocking.DISC_TITLE_FIELD,
 //									PactString.class).getValue()
 //							.replace("\"", "").replaceAll("[^a-zA-Z0-9]", "");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(
 //							soundex.soundex(title));
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			});
 //			add(new BlockingFunction() {
 //				@Override
 //				public
 //				PactString getID(){
 //					return new PactString("SoundexDiscTitle");
 //				}
 //				
 //				@Override
 //				PactString explode(PactRecord record){
 //					String title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class).getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					String artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class).getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(soundex.soundex(title)+soundex.soundex(artist));
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;						
 //				}
 //				
 //				@Override
 //				PactString function(PactRecord record) {
 //					String title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class).getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					Soundex soundex = new Soundex();
 //					PactString blockingKey = new PactString(soundex.soundex(title));
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			}
 //			);
 			add(new BlockingFunction() {
 				@Override
 				public
 				PactString getID(){
 					return new PactString("DiscTitleLast3SoundexArtist");
 				}
 				
 				@Override
 				PactString explode(PactRecord record){
 					PactString artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class);
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(artist);
 					UnicodeUtils.normalizeUnicode(title);
 					Soundex soundex = new Soundex();
 					PactString blockingKey = new PactString(title.getValue()+soundex.soundex(artist.getValue()));
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;						
 				}
 				
 				@Override
 				PactString function(PactRecord record) {
 					PactString artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class);
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(artist);
 					UnicodeUtils.normalizeUnicode(title);
 					Soundex soundex = new Soundex();
 					int length = title.length();
					String s_title = length > 4 ? title.getValue().substring(length-4, length-1) : title.getValue();
					PactString blockingKey = new PactString(s_title+soundex.soundex(artist.getValue()));
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 			}
 			);
 			add(new BlockingFunction() {
 				@Override
 				public
 				PactString getID(){
 					return new PactString("SoundexDiscLast3Artist");
 				}
 				
 				@Override
 				PactString explode(PactRecord record){
 					PactString artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class);
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(artist);
 					UnicodeUtils.normalizeUnicode(title);
 					Soundex soundex = new Soundex();
 					PactString blockingKey = new PactString(soundex.soundex(title.getValue())+artist);
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;						
 				}
 				
 				@Override
 				PactString function(PactRecord record) {
 					PactString artist = record.getField(MultiBlocking.ARTIST_NAME_FIELD, PactString.class);
 					PactString title = record.getField(MultiBlocking.DISC_TITLE_FIELD, PactString.class);
 					UnicodeUtils.normalizeUnicode(artist);
 					UnicodeUtils.normalizeUnicode(title);
 					Soundex soundex = new Soundex();
 					int length = artist.length();
					String s_artist = length > 4 ? artist.getValue().substring(length-4, length-1) : artist.getValue();
					PactString blockingKey = new PactString(soundex.soundex(title.getValue())+s_artist);
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 			}
 			);
 //			add(new BlockingFunction() {
 //				@Override
 //				public
 //				PactString getID(){
 //					return new PactString("Genre2Year3");
 //				}
 //				
 //				@Override
 //				PactString explode(PactRecord record){
 //					String genre = record.getField(4, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					String year = record.getField(5, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					PactString blockingKey = new PactString(genre + year);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;						
 //				}
 //				
 //				@Override
 //				PactString function(PactRecord record) {
 //					String genre = record.getField(4, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					genre = genre.length() > 2 ? genre.substring(0, 2) : "";
 //					String year = record.getField(5, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					year = year.length() >= 4 ? year.substring(0, 3) : "";
 //					PactString blockingKey = new PactString(genre + year);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			}
 //			);
 //			add(new BlockingFunction() {
 //				@Override
 //				public
 //				PactString getID(){
 //					return new PactString("Artist2Year3");
 //				}
 //				
 //				@Override
 //				PactString explode(PactRecord record){
 //					String artist = record.getField(2, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					String year = record.getField(5, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					PactString blockingKey = new PactString(artist + year);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;						
 //				}
 //				@Override
 //				PactString function(PactRecord record) {
 //					String artist = record.getField(2, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					artist = artist.length() > 2 ? artist.substring(0, 2)
 //							: "";
 //					String year = record.getField(5, PactString.class)
 //							.getValue().replace("\"", "").replaceAll("[^a-zA-Z0-9]","");
 //					year = year.length() >= 4 ? year.substring(0, 3) : "";
 //					PactString blockingKey = new PactString(artist + year);
 //					AsciiUtils.toLowerCase(blockingKey);
 //					return blockingKey;
 //				}
 //			}
 //			);
 		}
 	};
 	
 	public abstract PactString getID();
 	
 	abstract PactString explode(PactRecord record);
 	
 	abstract PactString function(PactRecord record);
 	
 	public PactRecord copyWithBlockingKey(PactRecord record){
 		PactRecord nr = record.createCopy();
 		nr.setField(MultiBlocking.BLOCKING_KEY_FIELD, function(record));
 		nr.setField(MultiBlocking.BLOCKING_ID_FIELD, getID());
 		return nr;
 	}
 	
 	public PactRecord setExplodedBlockingKey(PactRecord record){
 		record.setField(MultiBlocking.BLOCKING_KEY_EXTENDED_FIELD, explode(record));
 		return record;
 	}
 }
