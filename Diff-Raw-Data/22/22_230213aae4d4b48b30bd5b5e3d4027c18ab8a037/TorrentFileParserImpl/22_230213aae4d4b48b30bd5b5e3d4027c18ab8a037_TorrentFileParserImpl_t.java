 package org.trolo.torrent;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.primitives.Bytes;
 import org.trolo.bencode.api.Bencode;
 import org.trolo.bencode.api.BencodeSerializer;
 import org.trolo.bencode.api.BencodesParser;
 import org.trolo.common.ByteLists;
 import org.trolo.common.Sha1Hash;
 import org.trolo.domain.TorrentMetaFile;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 
 import static com.google.common.base.Preconditions.*;
 
 /**
  * @author Stanislav Kurilin
  */
 public class TorrentFileParserImpl implements TorrentFileParser {
     private final BencodesParser bencodesParser;
     private final BencodeSerializer bencodeSerializer;
 
     public TorrentFileParserImpl(BencodesParser bencodesParser, BencodeSerializer bencodeSerializer) {
         this.bencodesParser = bencodesParser;
         this.bencodeSerializer = bencodeSerializer;
     }
 
     @Override
     public Optional<TorrentMetaFile> parse(ImmutableList<Byte> in) {
         final Optional<ImmutableList<Bencode>> bencodes = bencodesParser.parse(in);
         if (!bencodes.isPresent()) return Optional.absent();
         checkArgument(bencodes.get().size() == 1, bencodes.get().size());
         return checkNotNull(bencodes.get().get(0).accept(new Bencode.AbstractVisitor<Optional<TorrentMetaFile>>() {
             @Override
             public Optional<TorrentMetaFile> visitDictionary(ImmutableMap<String, Bencode> value) {
                 return transform(value);
             }
         }));
     }
 
     private Optional<TorrentMetaFile> transform(final ImmutableMap<String, Bencode> value) {
         final ImmutableMap<String, Bencode> info = checkNotNull(value.get("info")).accept(new Bencode.AbstractVisitor<ImmutableMap<String, Bencode>>() {
             @Override
             public ImmutableMap<String, Bencode> visitDictionary(ImmutableMap<String, Bencode> value) {
                 return value;
             }
         });
         return Optional.<TorrentMetaFile>of(new TorrentMetaFile() {
             @Override
             public URI announce() {
                 try {
                     return new URI(checkNotNull(value.get("announce")).accept(new Bencode.AbstractVisitor<String>() {
                         @Override
                         public String visitData(ImmutableList<Byte> value) {
                             return ByteLists.toString(value);
                         }
                     }));
                 } catch (URISyntaxException e) {
                     throw new RuntimeException(e);
                 }
             }
 
             @Override
             public String name() {
                 return checkNotNull(value.get("name")).accept(new Bencode.AbstractVisitor<String>() {
                     @Override
                     public String visitData(ImmutableList<Byte> value) {
                         return ByteLists.toString(value);
                     }
                 });
             }
 
             @Override
             public Sha1Hash infoHash() {
                 return Sha1Hash.hash(bencodeSerializer.serialize(value.get("info")));
             }
 
             @Override
             public int bytesInPiece() {
                 return checkNotNull(info.get("piece length")).accept(new Bencode.AbstractVisitor<Integer>() {
                     @Override
                     public Integer visitDigit(long value) {
                         return (int) value;
                     }
                 });
             }
 
             @Override
             public ImmutableList<Sha1Hash> pieces() {
                 final ImmutableList<Byte> concatenated = checkNotNull(info.get("pieces")).accept(new Bencode.AbstractVisitor<ImmutableList<Byte>>() {
                     @Override
                     public ImmutableList<Byte> visitData(ImmutableList<Byte> value) {
                         return value;
                     }
                 });
                 final byte[] bytes = Bytes.toArray(concatenated);
                 checkState(bytes.length * 8 % 160 == 0, bytes.length);
                 final ImmutableList.Builder<Sha1Hash> result = ImmutableList.builder();
                for (int i = 0; i < bytes.length - 1; i += Sha1Hash.BYTE_IN_ONE_HASH)
                    result.add(Sha1Hash.valueOf(Arrays.copyOfRange(bytes, i, i + Sha1Hash.BYTE_IN_ONE_HASH)));
                 return result.build();
             }
 
             @Override
             public ImmutableList<SingleFileDescription> files() {
                 return ImmutableList.<SingleFileDescription>of(new SingleFileDescription() {
                     @Override
                     public String path() {
                         return "";
                     }
 
                     @Override
                     public int length() {
                         return checkNotNull(info.get("length")).accept(new Bencode.AbstractVisitor<Integer>() {
                             @Override
                             public Integer visitDigit(long value) {
                                 return (int) value;
                             }
                         });
                     }
                 });
             }
         });
     }
 
 }
