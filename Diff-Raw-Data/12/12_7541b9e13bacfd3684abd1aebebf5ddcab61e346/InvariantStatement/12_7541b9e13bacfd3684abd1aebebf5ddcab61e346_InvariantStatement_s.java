 package net.vidageek.invariant.internals;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.vidageek.invariant.FileData;
 import net.vidageek.invariant.Invariant;
 import net.vidageek.mirror.dsl.Mirror;
 import net.vidageek.mirror.exception.MirrorException;
 
 import org.junit.runners.model.Statement;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 final public class InvariantStatement extends Statement {
 	private static final Logger log = LoggerFactory.getLogger(InvariantStatement.class);
 
 	private final Class<?> type;
 	private final Method method;
 	private final Pattern toEval;
 	private int evaluatedFiles = 0;
 
 	private final String startFolder;
 
 	private final List<Failure> failures;
 
 	public InvariantStatement(final Method method, final Class<?> type) {
 		this.method = method;
 		this.type = type;
 		toEval = Pattern.compile(method.getAnnotation(Invariant.class).affects());
 		startFolder = method.getAnnotation(Invariant.class).folder();
 		failures = new ArrayList<Failure>();
 	}
 
 	@Override
 	public void evaluate() throws Throwable {
 		final File startPoint = new File(new File(System.getProperty("invariant.root", ".")).getCanonicalFile(),
				startFolder);
 		log.info("Starting runner at " + startPoint.getAbsolutePath());
 		evaluate(startPoint);
 		if (evaluatedFiles == 0) {
 			throw new IllegalArgumentException("Invariant " + invariantName()
 					+ " did not find any suitable file. The test started to run at " + startPoint.getAbsolutePath()
 					+ ". Maybe a regex problem?");
 		}
 		if (failures.size() != 0) {
 			throw new InvariantError(failures);
 		}
 	}
 
 	private void evaluate(final File file) throws Throwable {
 		if (file.isHidden()) {
 			log.debug("Rejecting " + file.getAbsolutePath() + " because it is hidden.");
 			return;
 		}
 		if (file.isFile()) {
 			if (toEval.matcher(file.getAbsolutePath()).matches()) {
 				evaluateAt(file);
 			}
 		} else if (file.isDirectory()) {
 			for (File child : file.listFiles()) {
 				evaluate(child);
 			}
 		}
 	}
 
 	private void evaluateAt(final File file) throws Throwable {
 		try {
 			evaluatedFiles++;
 			log.debug("Invoking invariant " + invariantName() + " at : " + file.getAbsolutePath());
 			Mirror mirror = new Mirror();
 			Object invariantObject = mirror.on(type).invoke().constructor().withoutArgs();
 			mirror.on(invariantObject).invoke().method(method).withArgs(new FileData(file));
 		} catch (MirrorException e) {
 			Throwable cause = e.getCause();
 			if (cause == null) {
 				throw e;
 			}
 			if (AssertionError.class.isAssignableFrom(cause.getClass())) {
 				failures.add(new Failure(cause, invariantName(), file.getAbsolutePath()));
 			} else {
 				throw cause;
 			}
 		}
 	}
 
 	private String invariantName() {
 		return type.getSimpleName() + "." + method.getName();
 	}
 }
