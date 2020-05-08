 package henix.jillus;
 
 import java.util.List;
 
 import henix.jillus.pegs.AccCapture;
 import henix.jillus.pegs.AlwaysSuccess;
 import henix.jillus.pegs.AnyChar;
 import henix.jillus.pegs.AtLeast;
 import henix.jillus.pegs.AtMost;
 import henix.jillus.pegs.CharInRange;
 import henix.jillus.pegs.CharInSet;
 import henix.jillus.pegs.Exactly;
 import henix.jillus.pegs.FakeSettingMatcher;
 import henix.jillus.pegs.GettingCapture;
 import henix.jillus.pegs.GettingIfNotMatch;
 import henix.jillus.pegs.GettingOrderChoice;
 import henix.jillus.pegs.IfNotMatch;
 import henix.jillus.pegs.Literal;
 import henix.jillus.pegs.OrderChoice;
 import henix.jillus.pegs.PassCaptureSequence;
 import henix.jillus.pegs.Sequence;
 import henix.jillus.pegs.SetGetCapture;
 import henix.jillus.pegs.SettingAtLeast;
 import henix.jillus.pegs.SettingIfNotMatch;
 import henix.jillus.pegs.SettingOrderChoice;
 import henix.jillus.pegs.SettingSequence;
 import henix.jillus.utils.ArrayListMaker;
 import henix.jillus.utils.ClassMaker;
 import henix.jillus.utils.ListAppender;
 import henix.jillus.utils.ReflectFieldSetter;
 import henix.jillus.utils.Identical;
 import henix.jillus.utils.ToFixedValue;
 
 public class Pegs {
 
 	/* ## atomics */
 
 	public static AnyChar anyChar() {
 		return AnyChar.instance;
 	}
 
 	public static CharInRange charInRange(char a, char b) {
 		return new CharInRange(a, b);
 	}
 
 	public static CharInSet charInSet(String s) {
 		return new CharInSet(s);
 	}
 
 	public static AlwaysSuccess alwaysSuccess() {
 		return AlwaysSuccess.instance;
 	}
 
 	/**
 	 * Match end of input, like "-1" in lpeg  
 	 */
 	public static PegMatcher eof() {
 		return ifNotMatch(anyChar(), alwaysSuccess());
 	}
 
 	/* ## IfNotMatch */
 
 	public static IfNotMatch ifNotMatch(PegMatcher e1, PegMatcher e2) {
 		return new IfNotMatch(e1, e2);
 	}
 
 	public static IfNotMatch ifNotMatch(String s1, PegMatcher e2) {
 		return ifNotMatch(new Literal(s1), e2);
 	}
 
 	public static IfNotMatch ifNotMatch(PegMatcher e1, String s2) {
 		return ifNotMatch(e1, new Literal(s2));
 	}
 
 	public static IfNotMatch ifNotMatch(String s1, String s2) {
 		return ifNotMatch(new Literal(s1), new Literal(s2));
 	}
 
 	/* ### SettingIfNotMatch */
 
 	public static <E> SettingIfNotMatch<E> ifNotMatch(PegMatcher cond, SettingMatcher<? super E> e) {
 		return new SettingIfNotMatch<E>(cond, e);
 	}
 
 	public static <E> SettingIfNotMatch<E> ifNotMatch(String s, SettingMatcher<? super E> e) {
 		return ifNotMatch(new Literal(s), e);
 	}
 
 	/* ### GettingIfNotMatch */
 
 	public static <E> GettingIfNotMatch<E> ifNotMatch(PegMatcher cond, GettingMatcher<? extends E> e) {
 		return new GettingIfNotMatch<E>(cond, e);
 	}
 
 	public static <E> GettingIfNotMatch<E> ifNotMatch(String s, GettingMatcher<? extends E> e) {
 		return ifNotMatch(new Literal(s), e);
 	}
 
 	/* ## AtLeast */
 
 	public static <E> SettingAtLeast<E> atLeast(int n, SettingMatcher<E> e) {
 		return new SettingAtLeast<E>(n, e);
 	}
 
 	public static PegMatcher atLeast(int n, PegMatcher e) {
 		return new AtLeast(n, e);
 	}
 
 	public static PegMatcher atLeast(int n, String str) {
 		return atLeast(n, new Literal(str));
 	}
 
 	/* ## Exactly */
 
 	public static PegMatcher exactly(int n, PegMatcher e) {
 		return new Exactly(n, e);
 	}
 
 	public static PegMatcher exactly(int n, String str) {
 		return new Exactly(n, new Literal(str));
 	}
 
 	/* ## AtMost */
 
 	public static PegMatcher atMost(int n, PegMatcher e) {
 		return new AtMost(n, e);
 	}
 
 	public static PegMatcher atMost(int n, String str) {
 		return new AtMost(n, new Literal(str));
 	}
 
 	/* ## Capture */
 
 	public static <E> GettingCapture<E> capture(ValueCreator<E> valueCreator, PegMatcher e) {
 		return new GettingCapture<E>(valueCreator, e);
 	}
 
 	public static GettingCapture<String> capture(PegMatcher e) {
 		return new GettingCapture<String>(Identical.instance, e);
 	}
 
 	public static <E> GettingCapture<E> captureAs(E value, PegMatcher e) {
 		return new GettingCapture<E>(new ToFixedValue<E>(value), e);
 	}
 
 	public static <E> GettingCapture<E> captureAs(E value, String s) {
 		return new GettingCapture<E>(new ToFixedValue<E>(value), new Literal(s));
 	}
 
 	/**
 	 * Accumulate as a struct / dict
 	 */
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass, SettingMatcher<? super E> e) {
 		return new AccCapture<E>(new ClassMaker<E>(targetClass), e);
 	}
 
 	/**
 	 * Convert an inner GettingMatcher to a SettingCapture
 	 *
 	 * Set value of a field
 	 */
 	public static <E, EI> SetGetCapture<E, EI> bindField(Class<E> parentClass, Class<EI> valueClass, String fieldName, GettingMatcher<? extends EI> e) {
 		return new SetGetCapture<E, EI>(new ReflectFieldSetter<E, EI>(parentClass, valueClass, fieldName), e);
 	}
 
 	/**
 	 * Convert an inner GettingMatcher to a SettingCapture
 	 *
 	 * Append to a list (parent must be a list)
 	 */
 	public static <E> SetGetCapture<List<E>, E> addlist(GettingMatcher<? extends E> e) {
 		return new SetGetCapture<List<E>, E>(new ListAppender<E>(), e);
 	}
 
 	/**
 	 * Accumulate as a list
 	 */
 	public static <E> AccCapture<List<E>> acclist(SettingMatcher<? super List<E>> e) {
 		return new AccCapture<List<E>>(new ArrayListMaker<E>(), e);
 	}
 
 	/**
 	 * Using atLeast to accumulate to a list
 	 */
 	public static <E> AccCapture<List<E>> asList(int n, GettingMatcher<? extends E> e) {
 		return acclist(atLeast(n, addlist(e)));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass,
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2) {
 		return asStruct(targetClass, new SettingSequence<E>(e1, e2));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass,
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3) {
 		return asStruct(targetClass, new SettingSequence<E>(e1, e2, e3));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass,
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4) {
 		return asStruct(targetClass, new SettingSequence<E>(e1, e2, e3, e4));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass,
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4,
 		SettingMatcher<? super E> e5) {
 		return asStruct(targetClass, new SettingSequence<E>(e1, e2, e3, e4, e5));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass,
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4,
 		SettingMatcher<? super E> e5, SettingMatcher<? super E> e6) {
 		return asStruct(targetClass, new SettingSequence<E>(e1, e2, e3, e4, e5, e6));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass,
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4,
 		SettingMatcher<? super E> e5, SettingMatcher<? super E> e6,
 		SettingMatcher<? super E> e7) {
 		return asStruct(targetClass, new SettingSequence<E>(e1, e2, e3, e4, e5, e6, e7));
 	}
 
 	public static <E> AccCapture<E> asStruct(Class<E> targetClass, SettingMatcher<? super E>... patts) {
 		return asStruct(targetClass, new SettingSequence<E>(patts));
 	}
 
 	public static FakeSettingMatcher bindNothing(PegMatcher e) {
 		return new FakeSettingMatcher(e);
 	}
 
 	public static FakeSettingMatcher bindNothing(String s) {
 		return new FakeSettingMatcher(new Literal(s));
 	}
 
 	public static FakeSettingMatcher bindNothing(Object... objs) {
 		return new FakeSettingMatcher(sequence(objs));
 	}
 
 	/* ## Sequence */
 
 	/* ### SettingSequence */
 
 	public static <E> SettingSequence<E> sequence(SettingMatcher<? super E> e1, SettingMatcher<? super E> e2) {
 		return new SettingSequence<E>(e1, e2);
 	}
 
 	public static <E> SettingSequence<E> sequence(
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3) {
 		return new SettingSequence<E>(e1, e2, e3);
 	}
 
 	public static <E> SettingSequence<E> sequence(
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4) {
 		return new SettingSequence<E>(e1, e2, e3, e4);
 	}
 
 	public static <E> SettingSequence<E> sequence(
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4,
 		SettingMatcher<? super E> e5) {
 		return new SettingSequence<E>(e1, e2, e3, e4, e5);
 	}
 
 	public static <E> SettingSequence<E> sequence(SettingMatcher<? super E>... patts) {
 		return new SettingSequence<E>(patts);
 	}
 
 	/* ### Sequence */
 
 	public static Sequence sequence(Object... objs) {
 		final PegMatcher[] patts = new PegMatcher[objs.length];
 		for (int i = 0; i < objs.length; i++) {
 			final Object obj = objs[i];
 			if (obj instanceof SettingMatcher<?>) {
				throw new IllegalArgumentException("sequence can't accept SettingMatcher, use setSeq instead");
 			} else if (obj instanceof GettingMatcher<?>) {
				throw new IllegalArgumentException("sequence can't accept GettingMatcher, new GettingSequence<T>() instead");
 			} else if (obj instanceof PegMatcher) {
 				patts[i] = (PegMatcher)obj;
 			} else if (obj instanceof String) {
 				patts[i] = new Literal((String)obj);
 			} else {
 				throw new IllegalArgumentException("sequence can't accept type " + obj.getClass().getName());
 			}
 		}
 		return new Sequence(patts);
 	}
 
 	/* ### GettingSequence */
 
 	public static <E> PassCaptureSequence<E> passCapture(PegMatcher before, GettingMatcher<? extends E> e, PegMatcher after) {
 		return new PassCaptureSequence<E>(before, e, after);
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(String before, GettingMatcher<? extends E> e, PegMatcher after) {
 		return new PassCaptureSequence<E>(new Literal(before), e, after);
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(PegMatcher before, GettingMatcher<? extends E> e, String after) {
 		return new PassCaptureSequence<E>(before, e, new Literal(after));
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(String before, GettingMatcher<? extends E> e, String after) {
 		return new PassCaptureSequence<E>(new Literal(before), e, new Literal(after));
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(PegMatcher before, GettingMatcher<? extends E> e) {
 		return new PassCaptureSequence<E>(before, e);
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(String before, GettingMatcher<? extends E> e) {
 		return new PassCaptureSequence<E>(new Literal(before), e);
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(GettingMatcher<? extends E> e, PegMatcher after) {
 		return new PassCaptureSequence<E>(e, after);
 	}
 
 	public static <E> PassCaptureSequence<E> passCapture(GettingMatcher<? extends E> e, String after) {
 		return new PassCaptureSequence<E>(e, new Literal(after));
 	}
 
 	/* ## OrderChoice */
 
 	/* ### GettingOrderChoice */
 
 	public static <E> GettingOrderChoice<E> orderChoice(GettingMatcher<? extends E> e1, GettingMatcher<? extends E> e2) {
 		return new GettingOrderChoice<E>(e1, e2);
 	}
 
 	public static <E> GettingOrderChoice<E> orderChoice(
 		GettingMatcher<? extends E> e1, GettingMatcher<? extends E> e2,
 		GettingMatcher<? extends E> e3) {
 		return new GettingOrderChoice<E>(e1, e2, e3);
 	}
 
 	public static <E> GettingOrderChoice<E> orderChoice(
 		GettingMatcher<? extends E> e1, GettingMatcher<? extends E> e2,
 		GettingMatcher<? extends E> e3, GettingMatcher<? extends E> e4) {
 		return new GettingOrderChoice<E>(e1, e2, e3, e4);
 	}
 
 	public static <E> GettingOrderChoice<E> orderChoice(GettingMatcher<? extends E>... patts) {
 		return new GettingOrderChoice<E>(patts);
 	}
 
 	/* ### SettingOrderChoice */
 
 	public static <E> SettingOrderChoice<E> orderChoice(SettingMatcher<? super E> e1, SettingMatcher<? super E> e2) {
 		return new SettingOrderChoice<E>(e1, e2);
 	}
 
 	public static <E> SettingOrderChoice<E> orderChoice(
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3) {
 		return new SettingOrderChoice<E>(e1, e2, e3);
 	}
 
 	public static <E> SettingOrderChoice<E> orderChoice(
 		SettingMatcher<? super E> e1, SettingMatcher<? super E> e2,
 		SettingMatcher<? super E> e3, SettingMatcher<? super E> e4) {
 		return new SettingOrderChoice<E>(e1, e2, e3, e4);
 	}
 
 	public static <E> SettingOrderChoice<E> orderChoice(SettingMatcher<? super E>... patts) {
 		return new SettingOrderChoice<E>(patts);
 	}
 
 	/* ### OrderChoice */
 
 	public static PegMatcher orderChoice(Object... objs) {
 		final PegMatcher[] patts = new PegMatcher[objs.length];
 		for (int i = 0; i < objs.length; i++) {
 			final Object obj = objs[i];
 			if (obj instanceof SettingMatcher<?>) {
				throw new IllegalArgumentException("orderChoice can't accept SettingMatcher");
 			} else if (obj instanceof GettingMatcher<?>) {
				throw new IllegalArgumentException("orderChoice can't accept GettingMatcher, the parameters must be all GettingMatcher");
 			} else if (obj instanceof PegMatcher) {
 				patts[i] = (PegMatcher)obj;
 			} else if (obj instanceof String) {
 				patts[i] = new Literal((String)obj);
 			} else {
 				throw new IllegalArgumentException("orderChoice can't accept type " + obj.getClass().getName());
 			}
 		}
 		return new OrderChoice(patts);
 	}
 }
