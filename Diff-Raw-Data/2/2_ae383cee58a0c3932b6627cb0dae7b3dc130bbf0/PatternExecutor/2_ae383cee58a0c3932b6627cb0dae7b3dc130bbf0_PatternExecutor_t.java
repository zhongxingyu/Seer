 package henix.jillus;
 
 import henix.jillus.pegs.attacher.AtLeastAttacher;
 import henix.jillus.pegs.attacher.Attacher;
 import henix.jillus.pegs.attacher.CaptureAttacher;
 import henix.jillus.pegs.attacher.NothingAttacher;
 import henix.jillus.pegs.attacher.OrderChoiceAttacher;
 import henix.jillus.pegs.attacher.SequenceAttacher;
 import henix.jillus.pegs.capture.AtomicCapturer;
 import henix.jillus.pegs.capture.CompoundCapturer;
 import henix.jillus.pegs.capture.NonTerminalCapturer;
 import henix.jillus.pegs.capture.OrderChoiceCapturer;
 import henix.jillus.pegs.capture.PassingCapturer;
 import henix.jillus.pegs.pattern.AnyChar;
 import henix.jillus.pegs.pattern.AtLeast;
 import henix.jillus.pegs.pattern.CharInRange;
 import henix.jillus.pegs.pattern.CharInSet;
 import henix.jillus.pegs.pattern.EmptyString;
 import henix.jillus.pegs.pattern.IfNotMatch;
 import henix.jillus.pegs.pattern.Literal;
 import henix.jillus.pegs.pattern.NonTerminal;
 import henix.jillus.pegs.pattern.Optional;
 import henix.jillus.pegs.pattern.OrderChoice;
 import henix.jillus.pegs.pattern.Sequence;
 
 /**
  * The interpreter
  */
 public class PatternExecutor {
 
 	private Source src;
 
 	public PatternExecutor(Source src) {
 		this.src = src;
 	}
 
 	public boolean match(PegPattern e) {
 		if (e instanceof EmptyString) {
 			return true;
 		} else if (e instanceof AnyChar) {
 			if (src.canGet()) {
 				src.consume();
 				return true;
 			}
 			return false;
 		} else if (e instanceof CharInRange) {
 			final CharInRange patt = (CharInRange)e;
 			if (src.canGet()) {
 				char c = src.getchar();
 				if (c >= patt.from && c <= patt.to) {
 					src.consume();
 					return true;
 				}
 			}
 			return false;
 		} else if (e instanceof CharInSet) {
 			final CharInSet patt = (CharInSet)e;
 			if (src.canGet()) {
 				char c = src.getchar();
 				if (patt.str.indexOf(c) != -1) {
 					src.consume();
 					return true;
 				}
 			}
 			return false;
 		} else if (e instanceof Literal) {
 			final Literal patt = (Literal)e;
 			if (src.canGet(patt.len)) {
 				if (src.gets(patt.len).equals(patt.str)) {
 					src.consume(patt.len);
 					return true;
 				}
 			}
 			return false;
 		} else if (e instanceof Optional) {
 			final Optional patt = (Optional)e;
 			match(patt.e);
 			return true;
 		} else if (e instanceof IfNotMatch) {
 			final IfNotMatch patt = (IfNotMatch)e;
 			final Mark mark = src.mark();
 			if (!match(patt.cond)) {
 				src.cancel(mark);
 				return match(patt.e);
 			}
 			src.goback(mark);
 			return false;
 		} else if (e instanceof Sequence) {
 			final Sequence patt = (Sequence)e;
 			final Mark mark = src.mark();
 			for (PegPattern subpatt : patt.patts) {
 				if (!match(subpatt)) {
 					src.goback(mark);
 					return false;
 				}
 			}
 			src.cancel(mark);
 			return true;
 		} else if (e instanceof OrderChoice) {
 			final OrderChoice patt = (OrderChoice)e;
 			for (PegPattern subpatt : patt.patts) {
 				if (match(subpatt)) {
 					return true;
 				}
 			}
 			return false;
 		} else if (e instanceof AtLeast) {
 			final AtLeast patt = (AtLeast)e;
 			final Mark mark = src.mark();
 			for (int i = 0; i < patt.n; i++) {
 				if (!match(patt.e)) {
 					src.goback(mark);
 					return false;
 				}
 			}
 			while (match(patt.e)) {
 				;
 			}
 			src.cancel(mark);
 			return true;
 		} else if (e instanceof NonTerminal) {
 			final NonTerminal patt = (NonTerminal)e;
 			if (patt.actual == null) {
 				throw new IllegalArgumentException("NonTerminal uninitialized, call set() before use it");
 			}
 			return match(patt.actual);
 		} else {
 			throw new IllegalArgumentException("Unknown PegPattern type: " + e.getClass().getName());
 		}
 	}
 
 	public <E> E execute(Capturer<E> e) {
 		if (e instanceof AtomicCapturer<?>) {
 			final AtomicCapturer<E> c = (AtomicCapturer<E>)e;
 			final Mark mark = src.mark();
 			if (match(c.e)) {
 				return c.valueCreator.create(src.tillNow(mark));
 			}
 			src.cancel(mark);
 			return null;
 		} else if (e instanceof PassingCapturer<?>) {
 			final PassingCapturer<E> c = (PassingCapturer<E>)e;
 			final Mark mark = src.mark();
 			E ret = null;
 			if (c.before != null && !match(c.before)) {
 				src.goback(mark);
 				return null;
 			}
 			ret = execute(c.e);
 			if (ret == null) {
 				src.goback(mark);
 				return null;
 			}
 			if (c.after != null && !match(c.after)) {
 				src.goback(mark);
 				return null;
 			}
 			src.cancel(mark);
 			return ret;
 		} else if (e instanceof CompoundCapturer<?>) {
 			final CompoundCapturer<E> c = (CompoundCapturer<E>)e;
 			final E newObj = c.recordCreator.create();
 			if (execute(c.a, newObj)) {
 				return newObj;
 			}
 			return null;
 		} else if (e instanceof OrderChoiceCapturer<?>) {
 			final OrderChoiceCapturer<E> c = (OrderChoiceCapturer<E>)e;
 			for (Capturer<? extends E> patt : c.alternatives) {
 				final E ret = execute(patt);
 				if (ret != null) {
 					return ret;
 				}
 			}
 			return null;
 		} else if (e instanceof NonTerminalCapturer<?>) {
 			final NonTerminalCapturer<E> c = (NonTerminalCapturer<E>)e;
 			if (c.actual == null) {
 				throw new IllegalArgumentException("NonTerminalCapturer uninitialized, call set() before use it");
 			}
 			return execute(c.actual);
 		} else {
			throw new IllegalArgumentException("Unknown Capturer type: " + e.getClass().getName());
 		}
 	}
 
 	private <E> boolean execute(Attacher<E> e, E parentObj) {
 		if (e instanceof NothingAttacher) {
 			final NothingAttacher a = (NothingAttacher)e;
 			return match(a.e);
 		} else if (e instanceof CaptureAttacher<?, ?>) {
 			final CaptureAttacher<E, Object> a = (CaptureAttacher<E, Object>)e;
 			final Object inner = execute(a.c);
 			if (inner != null) {
 				a.fieldSetter.setValue(parentObj, inner);
 				return true;
 			}
 			return false;
 		} else if (e instanceof SequenceAttacher<?>) {
 			final SequenceAttacher<E> a = (SequenceAttacher<E>)e;
 			final Mark mark = src.mark();
 			for (Attacher<? super E> attacher : a.attachers) {
 				if (!execute(attacher, parentObj)) {
 					src.goback(mark);
 					return false;
 				}
 			}
 			src.cancel(mark);
 			return true;
 		} else if (e instanceof OrderChoiceAttacher<?>) {
 			final OrderChoiceAttacher<E> a = (OrderChoiceAttacher<E>)e;
 			for (Attacher<? super E> attacher : a.attachers) {
 				if (execute(attacher, parentObj)) {
 					return true;
 				}
 			}
 			return false;
 		} else if (e instanceof AtLeastAttacher<?>) {
 			final AtLeastAttacher<E> a = (AtLeastAttacher<E>)e;
 			final Mark mark = src.mark();
 			for (int i = 0; i < a.n; i++) {
 				if (!execute(a.e, parentObj)) {
 					src.goback(mark);
 					return false;
 				}
 			}
 			while (execute(a.e, parentObj)) {
 				;
 			}
 			src.cancel(mark);
 			return true;
 		} else {
 			throw new IllegalArgumentException("Unknown Attacher type: " + e.getClass().getName());
 		}
 	}
 }
