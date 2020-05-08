 package henix.jillus;
 
 import java.util.List;
 
 import henix.jillus.pegs.attacher.*;
 import henix.jillus.pegs.capture.*;
 import henix.jillus.pegs.pattern.*;
 import henix.jillus.utils.*;
 
 public class Pegs {
 
 	/* # Common Patterns */
 
 	/**
 	 * Match end of input, like "-1" in lpeg
 	 */
 	public static PegPattern eof = notFollowedBy(anyChar());
 
 	public static PegPattern upper = charInRange('A', 'Z');
	public static PegPattern lower = charInRange('a', 'b');
 	public static PegPattern letter = orderChoice(lower, upper);
 	public static PegPattern digit = charInRange('0', '9');
 	public static PegPattern alphaNum = orderChoice(letter, digit);
 
 	/* # Patterns */
 
 	/* ## Atomics */
 
 	public static PegPattern anyChar() {
 		return AnyChar.instance;
 	}
 
 	public static PegPattern charInRange(char a, char b) {
 		return new CharInRange(a, b);
 	}
 
 	public static PegPattern charInSet(String s) {
 		return new CharInSet(s);
 	}
 
 	/* ## notFollowedBy */
 
 	public static PegPattern notFollowedBy(PegPattern e) {
 		return new NotPredict(e);
 	}
 
 	public static PegPattern notFollowedBy(String s) {
 		return new NotPredict(new Literal(s));
 	}
 
 	/* ## IfNotMatch */
 
 	public static PegPattern ifNotMatch(PegPattern e1, PegPattern e2) {
 		return sequence(new NotPredict(e1), e2);
 	}
 
 	public static PegPattern ifNotMatch(String s1, PegPattern e2) {
 		return ifNotMatch(new Literal(s1), e2);
 	}
 
 	public static PegPattern ifNotMatch(PegPattern e1, String s2) {
 		return ifNotMatch(e1, new Literal(s2));
 	}
 
 	public static PegPattern ifNotMatch(String s1, String s2) {
 		return ifNotMatch(new Literal(s1), new Literal(s2));
 	}
 
 	/* ## Optional */
 
 	public static PegPattern optional(PegPattern e) {
 		return new Optional(e);
 	}
 
 	public static PegPattern optional(String s) {
 		return new Optional(new Literal(s));
 	}
 
 	/* ## AtLeast */
 
 	public static PegPattern atLeast(int n, PegPattern e) {
 		return new AtLeast(n, e);
 	}
 
 	public static PegPattern atLeast(int n, String str) {
 		return atLeast(n, new Literal(str));
 	}
 
 	/* ## exactly: grammar sugar */
 
 	public static PegPattern exactly(int n, PegPattern e) {
 		final PegPattern[] patts = new PegPattern[n];
 		for (int i = 0; i < n; i++) {
 			patts[i] = e;
 		}
 		return new Sequence(patts);
 	}
 
 	public static PegPattern exactly(int n, String str) {
 		return exactly(n, new Literal(str));
 	}
 
 	/* ## Sequence */
 
 	public static PegPattern sequence(Object... objs) {
 		final PegPattern[] patts = new PegPattern[objs.length];
 		for (int i = 0; i < objs.length; i++) {
 			final Object obj = objs[i];
 			if (obj instanceof PegPattern) {
 				patts[i] = (PegPattern)obj;
 			} else if (obj instanceof String) {
 				patts[i] = new Literal((String)obj);
 			} else {
 				throw new IllegalArgumentException("sequence can't accept type " + obj.getClass().getName());
 			}
 		}
 		return new Sequence(patts);
 	}
 
 	/* ### OrderChoice */
 
 	public static PegPattern orderChoice(Object... objs) {
 		final PegPattern[] patts = new PegPattern[objs.length];
 		for (int i = 0; i < objs.length; i++) {
 			final Object obj = objs[i];
 			if (obj instanceof PegPattern) {
 				patts[i] = (PegPattern)obj;
 			} else if (obj instanceof String) {
 				patts[i] = new Literal((String)obj);
 			} else {
 				throw new IllegalArgumentException("orderChoice can't accept type " + obj.getClass().getName());
 			}
 		}
 		return new OrderChoice(patts);
 	}
 
 	/* # Capturer & Attacher */
 
 	public static <E> Capturer<E> capture(ValueCreator<E> valueCreator, PegPattern e) {
 		return new AtomicCapturer<E>(valueCreator, e);
 	}
 
 	public static Capturer<String> capture(PegPattern e) {
 		return new AtomicCapturer<String>(Identical.instance, e);
 	}
 
 	public static <E> Capturer<E> captureAs(E value, PegPattern e) {
 		return new AtomicCapturer<E>(new ToFixedValue<E>(value), e);
 	}
 
 	public static <E> Capturer<E> captureAs(E value, String s) {
 		return new AtomicCapturer<E>(new ToFixedValue<E>(value), new Literal(s));
 	}
 
 	/**
 	 * Accumulate as a struct / record
 	 *
 	 * many-of datatype
 	 */
 	public static <E> Capturer<E> asStruct(Class<E> targetClass, Attacher<? super E> e) {
 		return new CompoundCapturer<E>(new ReflectClassMaker<E>(targetClass), e);
 	}
 
 	/**
 	 * Convert an inner Capturer to a CaptureAttacher
 	 *
 	 * Set value of a field
 	 */
 	public static <E, EI> Attacher<E> bindField(Class<E> parentClass, Class<EI> valueClass, String fieldName, Capturer<? extends EI> e) {
 		return new CaptureAttacher<E, EI>(new ReflectFieldSetter<E, EI>(parentClass, valueClass, fieldName), e);
 	}
 
 	/**
 	 * Convert an inner Capturer to a CaptureAttacher
 	 *
 	 * Append to a list (parent must be a list)
 	 */
 	public static <E> Attacher<List<E>> addlist(Capturer<? extends E> e) {
 		return new CaptureAttacher<List<E>, E>(new ListAppender<E>(), e);
 	}
 
 	/**
 	 * Accumulate as a list
 	 */
 	public static <E> Capturer<List<E>> acclist(Attacher<? super List<E>> e) {
 		return new CompoundCapturer<List<E>>(new ArrayListMaker<E>(), e);
 	}
 
 	/**
 	 * Using AtLeastAttacher to accumulate to a list
 	 */
 	public static <E> Capturer<List<E>> asList(int n, Capturer<? extends E> e) {
 		return acclist(new AtLeastAttacher<List<E>>(n, addlist(e)));
 	}
 
 	public static Attacher<Object> bindNothing(PegPattern e) {
 		return new NothingAttacher(e);
 	}
 
 	public static Attacher<Object> bindNothing(String s) {
 		return new NothingAttacher(new Literal(s));
 	}
 
 	public static Attacher<Object> bindNothing(Object... objs) {
 		return new NothingAttacher(sequence(objs));
 	}
 
 	/* ## SequenceAttacher */
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2) {
 		return new SequenceAttacher<E>(e1, e2);
 	}
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3) {
 		return new SequenceAttacher<E>(e1, e2, e3);
 	}
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3,
 			Attacher<? super E> e4) {
 		return new SequenceAttacher<E>(e1, e2, e3, e4);
 	}
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3,
 			Attacher<? super E> e4,
 			Attacher<? super E> e5) {
 		return new SequenceAttacher<E>(e1, e2, e3, e4, e5);
 	}
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3,
 			Attacher<? super E> e4,
 			Attacher<? super E> e5,
 			Attacher<? super E> e6) {
 		return new SequenceAttacher<E>(e1, e2, e3, e4, e5, e6);
 	}
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3,
 			Attacher<? super E> e4,
 			Attacher<? super E> e5,
 			Attacher<? super E> e6,
 			Attacher<? super E> e7) {
 		return new SequenceAttacher<E>(e1, e2, e3, e4, e5, e6, e7);
 	}
 
 	public static <E> Attacher<E> sequence(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3,
 			Attacher<? super E> e4,
 			Attacher<? super E> e5,
 			Attacher<? super E> e6,
 			Attacher<? super E> e7,
 			Attacher<? super E> e8) {
 		return new SequenceAttacher<E>(e1, e2, e3, e4, e5, e6, e7, e8);
 	}
 
 	public static <E> Attacher<E> sequence(Attacher<? super E>... patts) {
 		return new SequenceAttacher<E>(patts);
 	}
 
 	/* ## PassingCapturer */
 
 	public static <E> Capturer<E> passCapture(PegPattern before, Capturer<? extends E> e, PegPattern after) {
 		return new PassingCapturer<E>(before, e, after);
 	}
 
 	public static <E> Capturer<E> passCapture(String before, Capturer<? extends E> e, PegPattern after) {
 		return new PassingCapturer<E>(new Literal(before), e, after);
 	}
 
 	public static <E> Capturer<E> passCapture(PegPattern before, Capturer<? extends E> e, String after) {
 		return new PassingCapturer<E>(before, e, new Literal(after));
 	}
 
 	public static <E> Capturer<E> passCapture(String before, Capturer<? extends E> e, String after) {
 		return new PassingCapturer<E>(new Literal(before), e, new Literal(after));
 	}
 
 	public static <E> Capturer<E> passCapture(PegPattern before, Capturer<? extends E> e) {
 		return new PassingCapturer<E>(before, e);
 	}
 
 	public static <E> Capturer<E> passCapture(String before, Capturer<? extends E> e) {
 		return new PassingCapturer<E>(new Literal(before), e);
 	}
 
 	public static <E> Capturer<E> passCapture(Capturer<? extends E> e, PegPattern after) {
 		return new PassingCapturer<E>(e, after);
 	}
 
 	public static <E> Capturer<E> passCapture(Capturer<? extends E> e, String after) {
 		return new PassingCapturer<E>(e, new Literal(after));
 	}
 
 	/* ## OrderChoiceCapturer, one-of datatype */
 
 	public static <E> Capturer<E> orderChoice(
 			Capturer<? extends E> e1,
 			Capturer<? extends E> e2) {
 		return new OrderChoiceCapturer<E>(e1, e2);
 	}
 
 	public static <E> Capturer<E> orderChoice(
 			Capturer<? extends E> e1,
 			Capturer<? extends E> e2,
 			Capturer<? extends E> e3) {
 		return new OrderChoiceCapturer<E>(e1, e2, e3);
 	}
 
 	public static <E> Capturer<E> orderChoice(
 			Capturer<? extends E> e1,
 			Capturer<? extends E> e2,
 			Capturer<? extends E> e3,
 			Capturer<? extends E> e4) {
 		return new OrderChoiceCapturer<E>(e1, e2, e3, e4);
 	}
 
 	public static <E> Capturer<E> orderChoice(Capturer<? extends E>... patts) {
 		return new OrderChoiceCapturer<E>(patts);
 	}
 
 	/* ### OrderChoiceAttacher */
 
 	public static <E> Attacher<E> orderChoice(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2) {
 		return new OrderChoiceAttacher<E>(e1, e2);
 	}
 
 	public static <E> Attacher<E> orderChoice(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3) {
 		return new OrderChoiceAttacher<E>(e1, e2, e3);
 	}
 
 	public static <E> Attacher<E> orderChoice(
 			Attacher<? super E> e1,
 			Attacher<? super E> e2,
 			Attacher<? super E> e3,
 			Attacher<? super E> e4) {
 		return new OrderChoiceAttacher<E>(e1, e2, e3, e4);
 	}
 
 	public static <E> Attacher<E> orderChoice(Attacher<? super E>... patts) {
 		return new OrderChoiceAttacher<E>(patts);
 	}
 }
