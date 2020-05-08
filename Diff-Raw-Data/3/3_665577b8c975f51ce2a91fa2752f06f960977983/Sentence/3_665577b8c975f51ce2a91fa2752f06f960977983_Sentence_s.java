 package rhythm;
 
 import static com.google.common.collect.Iterators.peekingIterator;
 import static rhythm.Interval.CompareHigh;
 import static rhythm.Interval.CompareLow;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.collect.AbstractIterator;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.PeekingIterator;
 
 public class Sentence extends Features {
 	private final ImmutableList<Token> tokens;
 	private final List<Behavior> behaviors = Lists.newArrayList();
 	
 	public Sentence(Iterable<String> tokens) {
 		ImmutableList.Builder<Token> b = ImmutableList.builder();
 		int n=0;
 		for (String t : tokens)
 			b.add(new Token(t, n++));
 		this.tokens = b.build();
 	}
 	
 	public Sentence(String... tokens) {
 		this(Arrays.asList(tokens));
 	}
 	
 	public int size() {
 		return tokens.size();
 	}
 	
 	public ImmutableList<Token> tokens() {
 		return tokens;
 	}
 	
 	public ImmutableList<Token> tokensIn(Interval i) {
 		return tokens.subList(i.low(), i.high());
 	}
 	
 	public <T> Iterable<T> tokens(final Feature<T> f) {
 		return Iterables.transform(tokens, new Function<Token, T>() {
 			public T apply(Token t) {
 				return t.get(f);
 			}
 		});
 	}
 	
 	public <T> T[] tokensArray(Feature<T> f, Class<T> type) {
 		return Iterables.toArray(tokens(f), type);
 	}
 	
 	public Iterable<String> tokensText() {
 		return Iterables.transform(tokens, new Function<Token, String>() {
 			public String apply(Token t) {
 				return t.text();
 			}
 		});
 	}
 	
 	public String[] tokensTextArray() {
 		return Iterables.toArray(tokensText(), String.class);
 	}
 	
 	public void add(Behavior b) {
 		behaviors.add(b);
 	}
 	
 	public Iterable<AnnotatedToken> annotated() {
 		return new Iterable<AnnotatedToken>() {
 			public Iterator<AnnotatedToken> iterator() {
 				final PeekingIterator<Behavior> starting =
 					peekingIterator(CompareLow.sortedCopy(behaviors).iterator());
 				final PeekingIterator<Behavior> ending = 
 					peekingIterator(CompareHigh.sortedCopy(behaviors).iterator());
 				return new AbstractIterator<AnnotatedToken>() {
 					int n = 0;
 					protected AnnotatedToken computeNext() {
 						if (n > tokens.size())
 							return endOfData();
 						Token t = (n==tokens.size()) ? null : tokens.get(n);
						n++;
 						List<Behavior> tStarting = Lists.newArrayList();
 						while (starting.hasNext() && starting.peek().low()<=n)
 							tStarting.add(starting.next());
 						List<Behavior> tEnding = Lists.newArrayList();
 						while (ending.hasNext() && ending.peek().high()<=n)
 							tEnding.add(ending.next());
 						return new AnnotatedToken(t, tStarting, tEnding);
 					}
 				};
 			}
 		};
 	}
 	
 	@Override
 	public String toString() {
 		return Objects.toStringHelper(this)
 			.add("tokens", tokens)
 			.add("features", super.toString())
 			.add("behaviors", behaviors)
 			.toString();
 	}
 }
