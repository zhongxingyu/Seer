 package com.bluespot.reflection;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class CallStack implements Iterable<CallStack.Frame> {
 
 	public static class Frame {
 
 		public Frame(final String className, final String methodName) {
 			this.className = className;
 			this.methodName = methodName;
 			this.packageName = this.className.substring(0, this.className.lastIndexOf("."));
 		}
 
		private final String className;
		private final String methodName;

		private final String packageName;

 		@Override
 		public boolean equals(final Object other) {
 			if (this == other) {
 				return true;
 			}
 			if (!(other instanceof Frame)) {
 				return false;
 			}
 			final Frame otherEntry = (Frame) other;
 			if (this.isValid()) {
 				return this.getClassName().equals(otherEntry.getClassName())
 						&& this.getMethodName().equals(otherEntry.getMethodName());
 			}
 			return otherEntry.isValid() == false;
 
 		}
 
 		public String getClassName() {
 			return this.className;
 		}
 
 		public String getMethodName() {
 			return this.methodName;
 		}
 
 		public String getPackageName() {
 			return this.packageName;
 		}
 
 		@Override
 		public int hashCode() {
 			int result = 17;
 			if (this.isValid()) {
 				result = 31 * result + this.getClassName().hashCode();
 				result = 31 * result + this.getMethodName().hashCode();
 			}
 			return result;
 		}
 
 		public boolean isValid() {
 			return this.getClassName() != null && this.getMethodName() != null;
 		}
 
 		@Override
 		public String toString() {
 			return String.format("CallStack.Frame(%s, %s)", this.getClassName(), this.getMethodName());
 		}
 	}
 
	protected static final Set<String> ignoredMethodNames = new HashSet<String>();

	protected static final Set<Package> ignoredPackages = new HashSet<Package>();
 
 	public CallStack() {
 		this.callStack = new ArrayDeque<CallStack.Frame>();
 	}
 
	private final Deque<CallStack.Frame> callStack;

 	public boolean contains(final CallStack.Frame frame) {
 		return this.callStack.contains(frame);
 	}
 
 	public CallStack.Frame getCurrentFrame() {
 		return this.callStack.peekFirst();
 	}
 
 	public boolean isEmpty() {
 		return this.callStack.isEmpty();
 	}
 
 	public Iterator<Frame> iterator() {
 		return this.callStack.iterator();
 	}
 
 	public CallStack.Frame pop() {
 		return this.callStack.removeFirst();
 	}
 
 	public void push(final CallStack.Frame frame) {
 		if (!CallStack.isIgnored(frame)) {
 			this.callStack.push(frame);
 		}
 	}
 
 	public int size() {
 		return this.callStack.size();
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder builder = new StringBuilder();
 		builder.append(String.format("CallStack (%d element(s))%n", this.size()));
 		for (final CallStack.Frame frame : this) {
 			builder.append(String.format("\t%s%n", frame.toString()));
 		}
 		return builder.toString();
 	}
 
 	static {
 		CallStack.ignorePackage(CallStack.class.getPackage());
 		CallStack.ignorePackage(Package.getPackage("java.util.logging"));
 	}
 
 	public static void ignoreMethodName(final String name) {
 		CallStack.ignoredMethodNames.add(name);
 	}
 
 	public static void ignorePackage(final Package ignoredPackage) {
 		CallStack.ignoredPackages.add(ignoredPackage);
 	}
 
 	public static boolean isIgnored(final CallStack.Frame frame) {
 		return CallStack.ignoredPackages.contains(Package.getPackage(frame.getPackageName()))
 				|| CallStack.ignoredMethodNames.contains(frame.getMethodName());
 	}
 }
