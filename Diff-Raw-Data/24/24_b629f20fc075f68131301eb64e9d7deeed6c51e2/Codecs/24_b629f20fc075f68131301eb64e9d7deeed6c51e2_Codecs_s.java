 package ch.unibe.scg.cells;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.protobuf.ByteString;
 
 /** Methods to help dealing with codecs. */
 public enum Codecs {
 	; // Don't instantiate
 
 	/** In case of a IOException, the iterator will throw an unchecked {@link EncodingException} */
 	public static <T> Iterable<T> decodeRow(final Iterable<Cell<T>> row, final Codec<T> codec) {
 		return Iterables.transform(row, new Function<Cell<T>, T>() {
 			@Override public T apply(Cell<T> cell) {
 				try {
 					return codec.decode(cell);
 				} catch (IOException e) {
 					throw new EncodingException(e);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Decode source using codec.
 	 * In case of a IOException, the iterator will throw an unchecked {@link EncodingException}
 	 */
 	public static <T> Source<T> decode(final CellSource<T> source, final Codec<T> codec) {
 		return new Source<T>() {
 			final private static long serialVersionUID = 1L;
 
 			@Override public Iterator<Iterable<T>> iterator() {
 				return Iterables.transform(source, new Function<Iterable<Cell<T>>, Iterable<T>>() {
 					@Override public Iterable<T> apply(Iterable<Cell<T>> encodedRow) {
 						return decodeRow(encodedRow, codec);
 					}
 				}).iterator();
 			}
 
 			@Override public String toString() {
 				return Iterators.toString(iterator());
 			}
 		};
 	}
 
 	/** Encode sink using codec */
 	public static <T> Sink<T> encode(final CellSink<T> sink, final Codec<T> codec) {
 		return new Sink<T>() {
 			final static private long serialVersionUID = 1L;
 
 			@Override public void write(T obj) throws IOException, InterruptedException {
 				sink.write(codec.encode(obj));
 			}
 
 			@Override public void close() throws IOException {
 				sink.close();
 			}
 		};
 	}
 
 	/** Wrapper of {@code cellTable} that returns decoded rows */
 	public static <T> LookupTable<T> decodedTable(final CellLookupTable<T> cellTable, final Codec<T> codec) {
 		return new LookupTable<T>() {
 			final static private long serialVersionUID = 1L;
 
 			@Override public Iterable<T> readRow(ByteString rowKey) throws IOException {
 				return decodeRow(cellTable.readRow(rowKey), codec);
 			}
 
 			@Override public void close() throws IOException {
 				cellTable.close();
 			}
 
 			@Override public Iterable<T> readColumn(ByteString columnKey) throws IOException {
 				return decodeRow(cellTable.readColumn(columnKey), codec);
 			}
 		};
 	}
 }
