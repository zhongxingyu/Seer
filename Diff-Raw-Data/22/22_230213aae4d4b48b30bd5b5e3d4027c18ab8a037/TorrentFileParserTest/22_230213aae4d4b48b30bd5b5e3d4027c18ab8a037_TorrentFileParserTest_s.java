 package org.trolo.torrent;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Resources;
 import com.google.common.primitives.Bytes;
 import org.testng.annotations.Test;
 import org.trolo.bencode.parser.ParserImpl;
 import org.trolo.bencode.serializer.BencodeSerializerImpl;
 import org.trolo.domain.TorrentMetaFile;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 
 /**
  * @author Stanislav Kurilin
  */
 
 public class TorrentFileParserTest {
     private final TorrentFileParser parser = new TorrentFileParserImpl(new ParserImpl(), new BencodeSerializerImpl());
 
     @Test
     public void testSingleFileTorrentFileParser() throws IOException, URISyntaxException {
         final String s = "http://cdimage.debian.org/debian-cd/6.0.4/sparc/bt-cd/debian-6.0.4-sparc-xfce+lxde-CD-1.iso.torrent";
         final byte[] backingArray = Resources.toByteArray(new URL(s));
         final ImmutableList<Byte> bytes = ImmutableList.copyOf(Bytes.asList(backingArray));
 
         final Optional<TorrentMetaFile> parsed = parser.parse(bytes);
         assertTrue(parsed.isPresent());
         final TorrentMetaFile metaFile = parsed.get();
         assertTrue(!metaFile.pieces().isEmpty());
         assertEquals(new URI("http://bttracker.debian.org:6969/announce"), metaFile.announce());
         assertEquals(metaFile.bytesInPiece(), 524288);
        assertEquals(metaFile.pieces().size(), 162);
         final TorrentMetaFile.SingleFileDescription singleFile = metaFile.files().iterator().next();
         assertEquals(singleFile.length(), 679260160);
         assertEquals("", singleFile.path());
     }
 
     @Test
     public void testMultiFileTorrentFileParser() {
 
     }
 
 }
