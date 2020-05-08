 package ch.rasc.musicsearch.service;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.IntField;
 import org.apache.lucene.document.LongField;
 import org.apache.lucene.document.StoredField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexWriter;
 import org.jaudiotagger.audio.AudioFile;
 import org.jaudiotagger.audio.AudioFileIO;
 import org.jaudiotagger.audio.AudioHeader;
 import org.jaudiotagger.audio.exceptions.CannotReadException;
 import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
 import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.Tag;
 import org.jaudiotagger.tag.TagException;
 
 public class IndexFileWalker extends SimpleFileVisitor<Path> {
 
 	private long totalDuration = 0;
 
 	private int noOfSongs = 0;
 
 	private final IndexWriter writer;
 
 	private final Path baseDir;
 
 	public IndexFileWalker(IndexWriter writer, Path baseDir) {
 		this.writer = writer;
 		this.baseDir = baseDir;
 	}
 
 	@Override
 	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 
 		try {
 			AudioFile audioFile = AudioFileIO.read(file.toFile());
 			Tag tag = audioFile.getTag();
 			AudioHeader ah = audioFile.getAudioHeader();
 
 			int trackLength = ah.getTrackLength();
 			totalDuration = totalDuration + trackLength;
 			noOfSongs++;
 
 			Document doc = new Document();
 
 			doc.add(new TextField("fileName", file.getFileName().toString(), Field.Store.YES));
 			doc.add(new TextField("directory", baseDir.relativize(file.getParent()).toString(), Field.Store.YES));
 			doc.add(new LongField("size", Files.size(file), Field.Store.YES));
 			doc.add(new LongField("bitrate", ah.getBitRateAsNumber(), Field.Store.YES));
 
 			String encoding = null;
 			String encodingType = ah.getEncodingType().toLowerCase();
 
 			if ("mp3".equals(encodingType)) {
 				encoding = "audio/mpeg";
 			} else if ("aac".equals(encodingType)) {
 				encoding = "audio/aac";
 			} else {
 				System.out.println("NOT FOUND: " + encodingType);
 			}
 
 			doc.add(new StoredField("encoding", encoding));
 
 			// 'mp3': {
 			// 'type': ['audio/mpeg; codecs="mp3"', 'audio/mpeg', 'audio/mp3',
 			// 'audio/MPA', 'audio/mpa-robust'],
 			// 'required': true
 			// },
 			//
 			// 'mp4': {
 			// 'related': ['aac','m4a'], // additional formats under the MP4
 			// container
 			// 'type': ['audio/mp4; codecs="mp4a.40.2"', 'audio/aac',
 			// 'audio/x-m4a', 'audio/MP4A-LATM', 'audio/mpeg4-generic'],
 			// 'required': false
 			// },
 			//
 			// 'ogg': {
 			// 'type': ['audio/ogg; codecs=vorbis'],
 			// 'required': false
 			// },
 			//
 			// 'wav': {
 			// 'type': ['audio/wav; codecs="1"', 'audio/wav', 'audio/wave',
 			// 'audio/x-wav'],
 			// 'required': false
 			// }
 
 			String value = tag.getFirst(FieldKey.TITLE);
 			if (StringUtils.isNotBlank(value)) {
 				doc.add(new TextField("title", value, Field.Store.YES));
 			}
 
 			value = tag.getFirst(FieldKey.ARTIST);
 			if (StringUtils.isNotBlank(value)) {
 				doc.add(new TextField("artist", value, Field.Store.YES));
 			}
 
 			value = tag.getFirst(FieldKey.ALBUM);
 			if (StringUtils.isNotBlank(value)) {
 				doc.add(new TextField("album", value, Field.Store.YES));
 			}
 
 			value = tag.getFirst(FieldKey.COMMENT);
 			if (StringUtils.isNotBlank(value)) {
 				doc.add(new TextField("comment", value, Field.Store.NO));
 			}
 
 			value = tag.getFirst(FieldKey.YEAR);
 			if (StringUtils.isNotBlank(value)) {
 				doc.add(new TextField("year", value, Field.Store.YES));
 			}
 
 			value = tag.getFirst(FieldKey.COMPOSER);
 			if (StringUtils.isNotBlank(value)) {
 				doc.add(new TextField("composer", value, Field.Store.NO));
 			}
 
 			if (trackLength > 0) {
 				doc.add(new IntField("duration", trackLength, Field.Store.YES));
 			}
 
 			writer.addDocument(doc);
 
 		} catch (CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
 			System.out.println(e.getMessage());
 		}
 
 		return FileVisitResult.CONTINUE;
 
 	}
 
 	public long getTotalDuration() {
 		return totalDuration;
 	}
 
 	public int getNoOfSongs() {
 		return noOfSongs;
 	}
 
 }
