 package ch.unibe.scg.cc;

 import static ch.unibe.scg.cc.RegisterClonesBackend.MINIMUM_LINES;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.junit.Test;
 
 import ch.unibe.scg.cc.Protos.Clone;
 import ch.unibe.scg.cc.javaFrontend.JavaType1ReplacerFactory;
 import ch.unibe.scg.cc.lines.StringOfLines;
 import ch.unibe.scg.cc.lines.StringOfLinesFactory;
 import ch.unibe.scg.cc.mappers.Protos.SnippetLocation;
 import ch.unibe.scg.cc.mappers.Protos.SnippetMatch;
 import ch.unibe.scg.cc.modules.CCModule;
 import ch.unibe.scg.cc.modules.JavaModule;
 
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.protobuf.ByteString;
 
// @formatter:off
 /**
  * Test {@link CloneExpander}.
  * @author Niko Schwarz
  */
 @SuppressWarnings("javadoc")
 public final class SnippetSimilarTest {
 	static class SnippetMatchComparator implements Comparator<SnippetMatch> {
 		@Override public int compare(SnippetMatch o1, SnippetMatch o2) {
 			return ComparisonChain
 					.start()
 					.compare(
 							o1.getThatSnippetLocation().getFunction().asReadOnlyByteBuffer(),
 							o2.getThatSnippetLocation().getFunction().asReadOnlyByteBuffer())
 					.compare(
 							o1.getThisSnippetLocation().getPosition(),
							o2.getThisSnippetLocation().getPosition())
 					.result();
 		}
 	}
 
 	static class SnippetLocationComparator implements Comparator<SnippetLocation> {
 		@Override public int compare(SnippetLocation o1, SnippetLocation o2) {
 			return o1.getSnippet().asReadOnlyByteBuffer().compareTo(
 					o2.getSnippet().asReadOnlyByteBuffer());
 		}
 	}
 
 	final Comparator<SnippetMatch> snippetMatchComparator = new SnippetMatchComparator();
 	final Comparator<SnippetLocation> snippetLocationComparator = new SnippetLocationComparator();
 
 	@Test
 	public void testAreSimilar() throws CannotBeHashedException {
 		// Not fully refactored, so we easily add println statements
 		// to make an example for the paper.
 		final Normalizer n1 = new Normalizer(new JavaType1ReplacerFactory().get());
 		final Normalizer n2 = new Normalizer(new Type2ReplacerFactory().get());
 		final ShingleHasher ss = Guice.createInjector(new CCModule(), new JavaModule())
 				.getInstance(ShingleHasher.class);
 
 		final StringBuilder s1 = snippet1();
 		final StringBuilder s2 = snippet2();
 		final StringBuilder s3 = snippet3();
 
 		// type-1 normalization
 		n1.normalize(s1);
 		n1.normalize(s2);
 		n1.normalize(s3);
 		// type-2 normalization
 		n2.normalize(s1);
 		n2.normalize(s2);
 		n2.normalize(s3);
 
 		final StringOfLinesFactory solFactory = new StringOfLinesFactory();
 		final List<List<SnippetLocation>> table = ImmutableList.of(
 				allSnippets(solFactory.make(s1.toString()), ss, new byte[] { 1 }),
 				allSnippets(solFactory.make(s2.toString()), ss, new byte[] { 2 }),
 				allSnippets(solFactory.make(s3.toString()), ss, new byte[] { 3 }));
 
 		// TODO: Filtering to collisions missing.
 
 		final List<List<SnippetMatch>> matches = Lists.newArrayList();
 		for (int i = 0; i < table.size(); i++) {
 			matches.add(new ArrayList<SnippetMatch>());
 			// only look at j > i to avoid outputting the same match twice.
 			for (int j = i + 1; j < table.size(); j++) {
 				for (final SnippetLocation e : table.get(i)) {
 					final int pos = Collections.binarySearch(table.get(j), e, snippetLocationComparator);
 					if (pos >= 0) {
 						matches.get(matches.size() - 1).add(SnippetMatch.newBuilder()
 								.setThisSnippetLocation(e)
 								.setThatSnippetLocation(table.get(j).get(pos))
 								.build());
 					}
 				}
 			}
 			Collections.sort(matches.get(matches.size() - 1), snippetMatchComparator);
 		}
 
 		assertThat(matches.size(), is(3));
 		assertThat(printString(matches.get(0)),
				is("(1-2|5$e556@3, 1-2|14$6043@13, 1-2|16$24a1@15, 1-3|14$6043@10, 1-3|16$24a1@12, )"));
		assertThat(printString(matches.get(1)), is("(2-3|0$832c@0, 2-3|7$650e@4, 2-3|8$68b6@5, " +
				"2-3|9$2f8d@6, 2-3|10$a370@7, 2-3|11$270e@8, 2-3|12$cbd8@9, 2-3|13$6043@10, 2-3|14$5b92@11," +
				" 2-3|15$24a1@12, 2-3|16$0b4c@13, 2-3|17$8cb0@14, )"));
 		assertThat(printString(matches.get(2)), is("()"));
 
 		// Extract matches
 		Collection<Clone> builtClones = new CloneExpander().expandClones(matches.get(0));
 		assertThat(builtClones.toString(),
				is("[this_function: \"\\001\"\nthat_function: \"\\002\"\n" +
						"this_from_position: 5\nthat_from_position: 3\nthis_length: 16\nthat_length: 17\n," +
						" this_function: \"\\001\"\nthat_function: \"\\003\"\n" +
						"this_from_position: 14\nthat_from_position: 10\nthis_length: 7\nthat_length: 7\n]"));
 		builtClones = new CloneExpander().expandClones(matches.get(1));
 		assertThat(builtClones.toString(),
 				is("[this_function: \"\\002\"\nthat_function: \"\\003\"\n"
				+ "this_from_position: 0\nthat_from_position: 0\nthis_length: 22\nthat_length: 19\n]"));
 		builtClones = new CloneExpander().expandClones(matches.get(2));
 		assertThat(builtClones.toString(), is("[]"));
 	}
 
 	List<SnippetLocation> allSnippets(StringOfLines sol, Hasher hasher, byte[] function)
 			throws CannotBeHashedException {
 		final List<SnippetLocation> ret = Lists.newArrayList();
 
 		for (int frameStart = 0; frameStart + MINIMUM_LINES
 				< sol.getNumberOfLines(); frameStart++) {
 			final String value = sol.getLines(frameStart, MINIMUM_LINES);
 			final ByteBuffer newHash = ByteBuffer.wrap(hasher.hash(value));
 			ret.add(SnippetLocation.newBuilder()
 					.setSnippet(ByteString.copyFrom(newHash))
 					.setFunction(ByteString.copyFrom(function))
 					.setPosition(frameStart)
 					.build());
 		}
 
 		Collections.sort(ret, snippetLocationComparator);
 		return ImmutableList.copyOf(ret); // compresses ret.
 	}
 
 	StringBuilder snippet1() {
 		return new StringBuilder("	public static int log10(int x, RoundingMode mode) {\n"
 				+ "		checkPositive(\"x\", x);\n"
 				+ "		int logFloor = log10Floor(x);\n"
 				+ "		int floorPow = POWERS_OF_10[logFloor];\n"
 				+ "		int result = -1;\n"
 				+ "		switch (mode) {\n"
 				+ "		case UNNECESSARY:\n"
 				+ "			checkRoundingUnnecessary(x == floorPow);\n"
 				+ "			// fall through\n"
 				+ "		case DOWN:\n"
 				+ "			result = logFloor;\n"
				+ "		case CEILING:\n" 
				+ "		case UP:\n"
 				+ "			result = (x == floorPow) ? logFloor : logFloor - 1;\n"
 				+ "		case HALF_DOWN:\n"
 				+ "		case HALF_UP:\n"
 				+ "		case HALF_EVEN:\n"
 				+ "			// sqrt(10) is irrational, so log10(x) - logFloor is never exactly\n"
 				+ "			// 0.5\n"
 				+ "			result = (x <= HALF_POWERS_OF_10[logFloor]) ? logFloor : logFloor - 1;\n"
 				+ "		}\n"
 				+ "		return result;\n"
 				+ "	}");
 	}
 
 	StringBuilder snippet2() {
 		return new StringBuilder("	public static int log10(int x, RoundingMode mode) {\n"
 				+ "		int logFloor = log10Floor(x);\n"
 				+ "		int floorPow = powers_Of_10[logFloor];\n"
 				+ "		switch (mode) {\n"
 				+ "		case UNNECESSARY:\n"
 				+ "			checkRoundingUnnecessary(x == floorPow);\n"
 				+ "			// fall through\n"
 				+ "		case FLOOR:\n"
 				+ "		case DOWN:\n"
 				+ "			return logFloor;\n"
 				+ "		case CEILING:\n"
 				+ "		case UP:\n"
 				+ "			return (x == floorPow) ? logFloor : logFloor + 1;\n"
 				+ "		case HALF_DOWN:\n"
 				+ "		case HALF_UP:\n"
 				+ "		case HALF_EVEN:\n"
 				+ "			// sqrt(10) is irrational, so log10(x) - logFloor is never exactly\n"
 				+ "			// 0.5\n"
 				+ "			return (x <= half_Powers_Of_10[logFloor]) ? logFloor : logFloor + 1;\n"
 				+ "		default:\n"
 				+ "			throw new AssertionError();\n"
 				+ "		}\n"
 				+ "	}");
 	}
 
 	StringBuilder snippet3() {
 		return new StringBuilder("	public static int log10(int x, RoundingMode mode) {\n"
 				+ "		int logFloor = log10Floor(x);\n"
 				+ "		int floorPow = powers_Of_10[logFloor];\n"
 				+ "		switch (mode) {\n"
 				+ "		case FLOOR:\n"
 				+ "		case DOWN:\n"
 				+ "			return logFloor;\n"
 				+ "		case CEILING:\n"
 				+ "		case UP:\n"
 				+ "			return (x == floorPow) ? logFloor : logFloor + 1;\n"
 				+ "		case HALF_DOWN:\n"
 				+ "		case HALF_UP:\n"
 				+ "		case HALF_EVEN:\n"
 				+ "			// sqrt(10) is irrational, so log10(x) - logFloor is never exactly\n"
 				+ "			// 0.5\n"
 				+ "			return (x <= half_Powers_Of_10[logFloor]) ? logFloor : logFloor + 1;\n"
 				+ "		default:\n"
 				+ "			throw new AssertionError();\n"
 				+ "		}\n"
 				+ "	}");
 	}
 
 	private String printString(Iterable<SnippetMatch> matches) {
 		final StringBuilder ret = new StringBuilder("(");
 			for (final SnippetMatch e : matches) {
 				final ByteBuffer hash = e.getThisSnippetLocation().getSnippet().asReadOnlyByteBuffer();
 				ret.append(String.format("%d-%d|%d$%02x%02x@%d, ",
 						e.getThisSnippetLocation().getFunction().asReadOnlyByteBuffer().get(0),
 						e.getThatSnippetLocation().getFunction().asReadOnlyByteBuffer().get(0),
 						e.getThisSnippetLocation().getPosition(),
 						hash.get(0), hash.get(1),
 						e.getThatSnippetLocation().getPosition()));
 			}
 			ret.append(')');
 		return ret.toString();
 	}
 }
