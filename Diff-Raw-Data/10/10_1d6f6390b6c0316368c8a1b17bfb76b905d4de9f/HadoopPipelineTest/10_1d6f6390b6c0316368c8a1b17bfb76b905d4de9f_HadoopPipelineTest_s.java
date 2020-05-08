 package ch.unibe.scg.cells.hadoop;
 
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.PARAMETER;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.inject.Qualifier;
 
 import org.apache.hadoop.conf.Configuration;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.unibe.scg.cc.mappers.ConfigurationProvider;
 import ch.unibe.scg.cells.Cell;
 import ch.unibe.scg.cells.CellsModule;
 import ch.unibe.scg.cells.Codec;
 import ch.unibe.scg.cells.Mapper;
 import ch.unibe.scg.cells.Pipeline;
 import ch.unibe.scg.cells.Sink;
 import ch.unibe.scg.cells.Source;
 import ch.unibe.scg.cells.TableModule.DefaultTableModule;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.CharStreams;
 import com.google.common.io.Closer;
 import com.google.common.primitives.Ints;
 import com.google.common.primitives.Longs;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.Module;
 import com.google.inject.TypeLiteral;
 import com.google.protobuf.ByteString;
 
 @SuppressWarnings("javadoc")
 public final class HadoopPipelineTest {
 	private final static String TABLE_NAME_IN = "richard-3";
 	private final static String TABLE_NAME_EFF = "richard-wc";
 	private final static ByteString fam = ByteString.copyFromUtf8("f");
 
 	private Table<Act> in;
 	private Table<WordCount> eff;
 	private TableAdmin tableAdmin;
 	private Configuration configuration;
 	private Injector injector;
 
 	@Before
 	public void loadRichardIII() throws IOException, InterruptedException {
 		String richard = CharStreams.toString(new InputStreamReader(this.getClass().getResourceAsStream(
 				"richard-iii.txt"), Charsets.UTF_8));
 
 		String[] actStrings = richard.split("\\bACT\\s[IVX]+");
 
 		final Module configurationModule = new AbstractModule() {
 			@Override protected void configure() {
 				bind(Configuration.class).toProvider(ConfigurationProvider.class);
 			}
 		};
 		Module tab = new CellsModule() {
 			@Override
 			protected void configure() {
 				installTable(
 						In.class,
 						new TypeLiteral<Act>() {},
 						ActCodec.class,
 						new HBaseStorage(), new DefaultTableModule(TABLE_NAME_IN,	fam));
 				installTable(
 						Eff.class,
 						new TypeLiteral<WordCount>() {},
 						WordCountCodec.class,
 						new HBaseStorage(), new DefaultTableModule(TABLE_NAME_EFF,	fam));
 			}
 		};
 		injector = Guice.createInjector(tab, configurationModule);
 
 		configuration = injector.getInstance(Configuration.class);
 		tableAdmin = injector.getInstance(TableAdmin.class);
 		in = tableAdmin.createTable(TABLE_NAME_IN, fam);
 
 		try (Sink<Act> sink = injector.getInstance(Key.get(new TypeLiteral<Sink<Act>>() {}, In.class))) {
 			for (int i = 0; i < actStrings.length; i++) {
 				sink.write(new Act(i, actStrings[i]));
 			}
 		}
 
 		eff = tableAdmin.createTable(TABLE_NAME_EFF, fam);
 	}
 
 	@After
 	public void deleteRichardIII() throws Exception {
 		try (Closer c = Closer.create()) {
 			if (in != null) {
 				c.register(in);
 			}
 			if (eff != null) {
 				c.register(eff);
 			}
 		}
 
		tableAdmin.deleteTable(in.getTableName());
 		if (eff != null) {
 			tableAdmin.deleteTable(eff.getTableName());
 		}
 	}
 
 	@Test
 	public void testOhhh() throws IOException, InterruptedException {
 		run(HadoopPipeline.fromTableToTable(configuration, fam, in, eff));
 		try(Source<WordCount> src = injector.getInstance(Key.get(new TypeLiteral<Source<WordCount>>() {}, Eff.class))) {
 			for (Iterable<WordCount> wcs : src) {
 				for (WordCount wc : wcs) {
 					System.out.println(wc.word + " " + wc.count);
 				}
 			}
 		}
 	}
 
 	void run(Pipeline<Act, WordCount> pipeline) throws IOException, InterruptedException {
 		pipeline.influx(new ActCodec())
 			.mapper(new WordParseMapper())
 			.shuffle(new WordCountCodec())
 			.efflux(new WordAdderMapper(), new WordCountCodec()); // TODO: Rename efflux to mapAndEfflux
 	}
 
 	@Qualifier
 	@Target({ FIELD, PARAMETER, METHOD })
 	@Retention(RUNTIME)
 	public static @interface In {}
 
 	@Qualifier
 	@Target({ FIELD, PARAMETER, METHOD })
 	@Retention(RUNTIME)
 	public static @interface Eff {}
 
 	static class Act {
 		final int number;
 		final String content;
 
 		Act(int number, String content) {
 			this.number = number;
 			this.content = content;
 		}
 	}
 
 	private static class ActCodec implements Codec<Act> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<Act> encode(Act act) {
 			return Cell.make(ByteString.copyFrom(Ints.toByteArray(act.number)), ByteString.copyFromUtf8("t"),
 					ByteString.copyFromUtf8(act.content));
 		}
 
 		@Override
 		public Act decode(Cell<Act> encoded) throws IOException {
 			return new Act(Ints.fromByteArray(encoded.getRowKey().toByteArray()), encoded.getCellContents()
 					.toStringUtf8());
 		}
 	}
 
 	private static class WordCount {
 		final String word;
 		final long count;
 
 		WordCount(String word, long count) {
 			this.word = word;
 			this.count = count;
 		}
 	}
 
 	static class WordCountCodec implements Codec<WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public Cell<WordCount> encode(WordCount s) {
 			return Cell.make(ByteString.copyFromUtf8(s.word),
 					ByteString.copyFromUtf8("1"),
 					ByteString.copyFrom(Longs.toByteArray(s.count)));
 		}
 
 		@Override
 		public WordCount decode(Cell<WordCount> encoded) throws IOException {
 			return new WordCount(encoded.getRowKey().toStringUtf8(),
 					Longs.fromByteArray(encoded.getCellContents().toByteArray()));
 		}
 	}
 
 	static class WordParseMapper implements Mapper<Act, WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() { }
 
 		@Override
 		public void map(Act first, Iterable<Act> row, Sink<WordCount> sink) throws IOException,
 				InterruptedException {
 			for(Act act : row) {
 				Matcher matcher = Pattern.compile("\\w+").matcher(act.content);
 				while (matcher.find()) {
 					sink.write(new WordCount(matcher.group(), 1L));
 				}
 			}
 		}
 	}
 
 	static class WordAdderMapper implements Mapper<WordCount, WordCount> {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void close() { }
 
 		@Override
 		public void map(WordCount first, Iterable<WordCount> row, Sink<WordCount> sink)
 				throws IOException, InterruptedException {
 			long total = 0L;
 			for (WordCount wc : row) {
 				total += wc.count;
 			}
 
 			sink.write(new WordCount(first.word, total));
 		}
 	}
 }
