 package com.syncron.findbugsextensions;
 
 import java.util.List;
 
 import org.apache.bcel.classfile.AnnotationEntry;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.classfile.Method;
 
 import com.syncron.annotation.PackagePrivate;
 
 import edu.umd.cs.findbugs.BugInstance;
 import edu.umd.cs.findbugs.BugReporter;
 import edu.umd.cs.findbugs.BytecodeScanningDetector;
 import edu.umd.cs.findbugs.ba.XClass;
 import edu.umd.cs.findbugs.ba.XMethod;
 import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
 import edu.umd.cs.findbugs.classfile.ClassDescriptor;
 import edu.umd.cs.findbugs.classfile.DescriptorFactory;
 
 public class PackagePrivateDetector extends BytecodeScanningDetector {
 
	private static final String BUG_NAME = "SYNC_PACKAGE_PRIVATE_USAGE";
 
 	private BugReporter bugReporter;
 
 	private ClassDescriptor packagePrivateDescriptor;
 
 	public PackagePrivateDetector(BugReporter bugReporter) {
 		this.bugReporter = bugReporter;
 	}
 
 	@Override
 	public void sawOpcode(int seen) {
 		if (seen != INVOKEVIRTUAL && seen != INVOKESPECIAL)
 			return;
 
 		if (isForbiddenPackagePrivateCall())
 			bugReporter.reportBug(createPackagePrivateUsageInMethodBug());
 	}
 
 	@Override
 	public void visit(Method obj) {
 		if (isForbiddenPackagePrivateAnnotation(obj.getAnnotationEntries())) {
 			bugReporter.reportBug(createPackagePrivateUsageInMethodBug());
 		}
 	}
 
 	private boolean isForbiddenPackagePrivateAnnotation(AnnotationEntry[] annotationEntries) {
 		for (AnnotationEntry annotation : annotationEntries) {
 			String annotationClass = annotation.getAnnotationType();
 			annotationClass = annotationClass.replaceAll("\\.", "/").substring(1, annotationClass.length() - 1);
 
 			try {
 				if (isClassAnnotatedPrivatePackage(annotationClass))
 					return !doClassesShareTheSamePackage(annotationClass, getClassName());
 			} catch (CheckedAnalysisException e) {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	private boolean isClassAnnotatedPrivatePackage(String annotationClass) throws CheckedAnalysisException {
 		DescriptorFactory descriptorFactory = DescriptorFactory.instance();
 		return descriptorFactory.getClassDescriptor(annotationClass).getXClass()
 				.getAnnotation(getPackagePrivateDescriptor()) != null;
 	}
 
 	@Override
 	public void visit(JavaClass javaClass) {
 		reportPackagePrivateClassExtension(javaClass);
 		reportPackatePrivateAnnotationUsage(javaClass);
 	}
 
 	private void reportPackatePrivateAnnotationUsage(JavaClass javaClass) {
 		if (isForbiddenPackagePrivateAnnotation(javaClass.getAnnotationEntries()))
 			bugReporter.reportBug(createPackagePrivateUsageInClassBug());
 	}
 
 	private void reportPackagePrivateClassExtension(JavaClass javaClass) {
 		String className = javaClass.getClassName().replaceAll("\\.", "/");
 		try {
 			if (isForbiddenPackagePrivateExtension(className, javaClass.getSuperClass()))
 				bugReporter.reportBug(createPackagePrivateUsageInClassBug());
 		} catch (ClassNotFoundException e) {
 			bugReporter.reportMissingClass(e);
 		}
 	}
 
 	private BugInstance createPackagePrivateUsageInClassBug() {
 		return new BugInstance(this, BUG_NAME, NORMAL_PRIORITY).addClass(this);
 	}
 
 	private boolean isForbiddenPackagePrivateExtension(String className, JavaClass superClass) {
 		if (isClassAnnotatedPrivatePackage(superClass)) {
 			String superClassName = superClass.getClassName().replaceAll("\\.", "/");
 			return !doClassesShareTheSamePackage(superClassName, className);
 		} else
 			return false;
 	}
 
 	private boolean isClassAnnotatedPrivatePackage(JavaClass javaClass) {
 		for (AnnotationEntry annotationEntry : javaClass.getAnnotationEntries()) {
 			String annotationClass = annotationEntry.getAnnotationType();
 			annotationClass = annotationClass.replaceAll("/", ".").substring(1, annotationClass.length() - 1);
 			if (annotationClass.equals(PackagePrivate.class.getName()))
 				return true;
 		}
 		return false;
 	}
 
 	private BugInstance createPackagePrivateUsageInMethodBug() {
 		return new BugInstance(this, BUG_NAME, NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this);
 	}
 
 	private boolean isForbiddenPackagePrivateCall() {
 		String className = getClassConstantOperand();
 		String methodName = getNameConstantOperand();
 		String signature = getSigConstantOperand();
 
 		try {
 			XMethod method = findMethodBeingCalled(className, methodName, signature);
 			if (isMethodAnnotatedPackagePrivate(method))
 				return !doClassesShareTheSamePackage(className, getClassName());
 			else
 				return false;
 		} catch (CheckedAnalysisException e) {
 			return false;
 		}
 	}
 
 	private boolean isMethodAnnotatedPackagePrivate(XMethod m) {
 		return m.getAnnotation(getPackagePrivateDescriptor()) != null;
 	}
 
 	private ClassDescriptor getPackagePrivateDescriptor() {
 		if (packagePrivateDescriptor == null)
 			packagePrivateDescriptor = DescriptorFactory.instance().getClassDescriptor(PackagePrivate.class);
 		return packagePrivateDescriptor;
 	}
 
 	private XMethod findMethodBeingCalled(String className, String methodName, String signature)
 			throws CheckedAnalysisException {
 		DescriptorFactory instance = DescriptorFactory.instance();
 		XClass classBeingCalled = instance.getClassDescriptor(className).getXClass();
 		return findMethodByNameAndSignature(classBeingCalled.getXMethods(), methodName, signature);
 	}
 
 	private XMethod findMethodByNameAndSignature(List<? extends XMethod> xMethods, String methodName, String signature)
 			throws CheckedAnalysisException {
 		for (XMethod method : xMethods)
 			if (method.getName().equals(methodName) && method.getSignature().equals(signature))
 				return method;
 		throw new CheckedAnalysisException("No method found: " + methodName + signature);
 	}
 
 	private boolean doClassesShareTheSamePackage(String className, String className2) {
 		String firstPackage = className.substring(0, className.lastIndexOf('/'));
 		String secondPackage = className2.substring(0, className2.lastIndexOf('/'));
 		return firstPackage.equals(secondPackage);
 	}
 }
