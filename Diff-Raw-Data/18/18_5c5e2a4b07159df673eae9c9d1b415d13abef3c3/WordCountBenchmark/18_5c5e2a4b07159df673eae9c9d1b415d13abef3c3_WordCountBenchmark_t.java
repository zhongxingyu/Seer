 package ch.unibe.scg.cells.benchmarks;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;

 import ch.unibe.scg.cells.Cell;
import ch.unibe.scg.cells.Cells;
 import ch.unibe.scg.cells.Codec;
 import ch.unibe.scg.cells.InMemoryPipeline;
 import ch.unibe.scg.cells.LocalExecutionModule;
 import ch.unibe.scg.cells.Mapper;
 import ch.unibe.scg.cells.OneShotIterable;
 import ch.unibe.scg.cells.Sink;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.io.CharStreams;
 import com.google.common.primitives.Ints;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.protobuf.ByteString;
 
 /**
  * Benchmarks cells performance on a local machine with wordcount problem.
  * Input folder can be specified via command line argument.
  */
 public class WordCountBenchmark {
 	private final static int TIMES = 10;
 
 	private final static class WordCount {
 		int count;
 		final String word;
 		final String fileName;
 
 		WordCount(String word, int count, String fileName) {
 			this.count = count;
 			this.word = word;
 			this.fileName = fileName;
 		}
 
 		@Override
 		public String toString() {
 			return word + ": " + count;
 		}
 	}
 
 	private final static class WordCountCodec implements Codec<WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<WordCount> encode(WordCount s) {
 			return Cell.make(ByteString.copyFromUtf8(s.word),
 					ByteString.copyFromUtf8(s.fileName),
 					ByteString.copyFrom(Ints.toByteArray(s.count)));
 		}
 
 		@Override
 		public WordCount decode(Cell<WordCount> encoded) throws IOException {
 			return new WordCount(encoded.getRowKey().toStringUtf8(),
 					Ints.fromByteArray(encoded.getCellContents().toByteArray()),
 					encoded.getColumnKey().toStringUtf8());
 		}
 	}
 
 	private final static class Book {
 		final String fileName;
 		final String content;
 
 		Book(String fileName, String content) {
 			this.fileName = fileName;
 			this.content = content;
 		}
 	}
 
 	static class BookCodec implements Codec<Book> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<Book> encode(Book b) {
 			return Cell.make(ByteString.copyFromUtf8(b.fileName),
 					ByteString.copyFromUtf8(b.content), ByteString.EMPTY);
 		}
 
 		@Override
 		public Book decode(Cell<Book> encoded) throws IOException {
 			return new Book(encoded.getRowKey().toStringUtf8(), encoded.getColumnKey().toStringUtf8());
 		}
 	}
 
 	private static class WordParser implements Mapper<Book, WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() throws IOException { }
 
 		@Override
 		public void map(Book first, OneShotIterable<Book> row, Sink<WordCount> sink)
 				throws IOException, InterruptedException {
 			Map<String, WordCount> dictionary = new HashMap<>();
 			for (Book book : row) {
 				for (String word: book.content.split("\\s+")) {
 					if (!word.isEmpty()) {
 						if (dictionary.containsKey(word)) {
 							dictionary.get(word).count++;
 						} else {
 							dictionary.put(word, new WordCount(word, 1, book.fileName));
 						}
 					}
 				}
 			}
 
 			for (WordCount wc : dictionary.values()) {
 				sink.write(wc);
 			}
 		}
 	}
 
 	private static class WordCounter implements Mapper<WordCount, WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() throws IOException { }
 
 		@Override
 		public void map(WordCount first, OneShotIterable<WordCount> row, Sink<WordCount> sink) throws IOException,
 		InterruptedException {
 			int count = 0;
 
 			for (WordCount wc : row) {
 				count += wc.count;
 			}
 			sink.write(new WordCount(first.word, count, first.fileName));
 		}
 	}
 
 	/**
 	 * Runs a wordcount benchmark. You can specify input folder with first argument.
 	 * The default input folder is "../data"
 	 */
 	public static void main(String[] args) throws IOException, InterruptedException {
 		String input = "..//data";
 		if (args.length > 0) {
 			input = args[0];
 		}
 
 		Injector inj = Guice.createInjector(new LocalExecutionModule());
 		double[] timings = new double[TIMES];
 		NumberFormat f = DecimalFormat.getInstance();
 		f.setMaximumFractionDigits(2);
 
 		for (int i = 0; i < TIMES; i++) {
 			long startTime = System.nanoTime();
 
			try (InMemoryPipeline<Book, WordCount> pipe
 						= inj.getInstance(InMemoryPipeline.Builder.class)
								.make(Cells.shard(Cells.encode(readBooksFromDisk(input), new BookCodec())))) {
 
 				pipe.influx(new BookCodec())
 					.map(inj.getInstance(WordParser.class))
 					.shuffle(new WordCountCodec())
 					.mapAndEfflux(inj.getInstance(WordCounter.class), new WordCountCodec());
 
 				int dummy = 0;
				for (Iterable<WordCount> wcs : Cells.decodeSource(pipe.lastEfflux(), new WordCountCodec())) {
 					dummy += Iterables.size(wcs);
 				}
 
 				timings[i] = (System.nanoTime() - startTime) / 1_000_000_000.0;
 				System.out.println( f.format(timings[i]) );
 
 				if (dummy == 0) {
 					System.out.println();
 				}
 			}
 		}
 
 		System.out.println("--------------");
 		System.out.println(String.format("median: %s", f.format(median(timings))));
 		System.out.println(String.format("min: %s", f.format(min(timings))));
 
 	}
 
 	private static Iterable<Book> readBooksFromDisk(String path) throws IOException, InterruptedException {
 		final ImmutableList.Builder<Book> ret = ImmutableList.builder();
 		for (File f : new File(path).listFiles()) {
 			try {
 				ret.add(new Book(f.getName(),
 						CharStreams.toString(new InputStreamReader(new FileInputStream(f), Charsets.UTF_8))));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return ret.build();
 	}
 
 	private static double median(double[] d) {
 		if (d == null || d.length == 0) {
 			throw new IllegalArgumentException("Median of 0 elements is undefined.");
 		}
 
 		double[] copy = Arrays.copyOf(d, d.length);
 		Arrays.sort(copy);
 		return copy[copy.length / 2];
 	}
 
 	private static double min(double[] d) {
 		if (d == null || d.length == 0) {
 			throw new IllegalArgumentException("Min of 0 elements is undefined.");
 		}
 
 		double min = d[0];
 		for (int i = 1; i < d.length; i++) {
 			if (d[i] < min) {
 				min = d[i];
 			}
 		}
 
 		return min;
 	}
 }
