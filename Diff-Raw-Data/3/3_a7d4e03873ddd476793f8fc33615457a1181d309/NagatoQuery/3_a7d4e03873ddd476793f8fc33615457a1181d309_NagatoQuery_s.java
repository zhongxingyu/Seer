 package com.luzi82.nagatoquery;
 
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.regex.Pattern;
 
 public abstract class NagatoQuery {
 
 	public static final String CMD_PREFIX = "cmd_";
 
 	public final Map<String, String> mVarTree;
 	public final Map<String, String> mTmpVarTree;
 	public final Map<String, Method> mCommandTree;
 
 	public final Executor mExecutor;
 
 	public NagatoQuery(Executor aExecutor) {
 		mVarTree = new TreeMap<String, String>();
 		mTmpVarTree = new TreeMap<String, String>();
 		mCommandTree = new TreeMap<String, Method>();
 		mExecutor = aExecutor;
 	}
 
 	public interface CommandListener {
 		public void commandReturn(String aResult);
 	}
 
 	public void loadClass(Class<?> aClass) {
 		Method[] mv = aClass.getMethods();
 		for (Method m : mv) {
 			String name = m.getName();
 			if (name.startsWith(CMD_PREFIX)) {
 				name = name.substring(CMD_PREFIX.length());
 				mCommandTree.put(name, m);
 			}
 		}
 	}
 
 	public void execute(String aCommand, CommandListener aListener) {
 		try {
 			String[] token = token(aCommand);
 			if (token.length <= 0) {
 				aListener.commandReturn(null);
 				return;
 			}
 			for (int i = 0; i < token.length; ++i) {
 				token[i] = parseInput(token[i]);
 			}
 			Method method = mCommandTree.get(token[0]);
 			Object[] argv = new Object[token.length + 1];
 			argv[0] = this;
 			argv[1] = aListener;
 			for (int i = 1; i < token.length; ++i) {
 				argv[i + 1] = token[i];
 			}
 			method.invoke(null, argv);
 		} catch (Throwable t) {
 			t.printStackTrace();
 			aListener.commandReturn(null);
 		}
 	}
 
 	public String parseInput(String aInput) {
 		while (true) {
 			if (aInput.startsWith("$")) {
 				aInput = mVarTree.get(aInput.substring(1));
			}
			if (aInput.startsWith("%")) {
 				aInput = mTmpVarTree.get(aInput.substring(1));
 			} else {
 				return aInput;
 			}
 		}
 	}
 
 	public static String[] token(String aLine) {
 		String[] v = aLine.split(Pattern.quote(" "));
 		LinkedList<String> vv = new LinkedList<String>();
 		for (String s : v) {
 			if (s.length() == 0)
 				continue;
 			vv.addLast(s);
 		}
 		return vv.toArray(new String[0]);
 	}
 
 	public String getVarString(String aKey) {
 		String v = parseInput(aKey);
 		return v;
 	}
 
 	public void setVar(String aKey, Object aObject) {
 		String objString = (String) aObject;
 		if (aKey.startsWith("$")) {
 			mVarTree.put(aKey.substring(1), objString);
 		} else if (aKey.startsWith("%")) {
 			mTmpVarTree.put(aKey.substring(1), objString);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public abstract void trace(String aMessage);
 
 	public abstract void onExit();
 
 	public static abstract class AbstractConsole extends NagatoQuery implements Runnable {
 		public AbstractConsole(Executor aExecutor) {
 			super(aExecutor);
 		}
 
 		public void run() {
 			if (mTmpVarTree.containsKey("exit"))
 				return;
 			String line = readLine();
 			execute(line, new CommandListener() {
 				@Override
 				public void commandReturn(String aResult) {
 					if (aResult != null)
 						trace(aResult);
 					start();
 				}
 			});
 		}
 
 		public void start() {
 			mExecutor.execute(AbstractConsole.this);
 		}
 
 		public abstract String readLine();
 	}
 
 	public static class StdConsole extends AbstractConsole {
 		public String mInputPrefix;
 
 		public StdConsole(String aInputPrefix, Executor aExecutor) {
 			super(aExecutor);
 			mInputPrefix = aInputPrefix;
 		}
 
 		@Override
 		public String readLine() {
 			if (mInputPrefix != null)
 				System.console().writer().write(mInputPrefix);
 			System.console().writer().flush();
 			return System.console().readLine();
 		}
 
 		@Override
 		public void trace(String aMessage) {
 			System.console().writer().println(aMessage);
 			System.console().writer().flush();
 		}
 
 		@Override
 		public void onExit() {
 			System.exit(0);
 		}
 
 	}
 
 	public static void main(String[] argv) {
 		StdConsole sc = new StdConsole("YUKI.N> ", Executors.newFixedThreadPool(5));
 		sc.loadClass(UtilCommand.class);
 		sc.start();
 	}
 
 }
