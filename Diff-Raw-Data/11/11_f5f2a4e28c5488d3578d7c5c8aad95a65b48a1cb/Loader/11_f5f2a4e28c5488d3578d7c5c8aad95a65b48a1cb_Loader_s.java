 package org.mitre.hdata.test;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.XMLConfiguration;
 import org.apache.commons.lang.StringUtils;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.mitre.hdata.test.tests.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Main Loader loads test units, executes them, and generates output report
  *
  * @author Jason Mathews, MITRE Corp.
  */
 public final class Loader {
 
 	private static final Logger log = LoggerFactory.getLogger(Loader.class);
 
 	private Context context = new Context();
 	private final Map<Class<? extends TestUnit>, TestUnit> list =
 			new HashMap<Class<? extends TestUnit>, TestUnit>();
 
 	private final Set<TestUnit> sortedSet = new TreeSet<TestUnit>();
 
 	private final Set<String> idSet = new HashSet<String>();
 
 	private static final Loader loader = new Loader();
 
 	private Loader() {
 		try {
 			XMLConfiguration config = new XMLConfiguration();
 			// load from cmd-line parameter. default=config.xml
 			String configFile = System.getProperty("configFile");
 			if (StringUtils.isBlank(configFile)) configFile = "config.xml"; // default
 			config.setFileName(configFile);
 			config.load();
 			context.load(config);
 
 			String profile = context.getString("profileDocumentFile");
 			if (StringUtils.isNotBlank(profile)) {
 				loadProfile(profile);
 			} else {
 				loadDefaultTests();
 			}
 		} catch (ConfigurationException e) {
 			log.error("", e);
 		} catch (IllegalArgumentException e) {
 			log.error("", e);
 		} catch (IOException e) {
 			log.error("Failed to load assertions from profile document", e);
 		}
 	}
 
 	private void loadProfile(String profile) throws IOException {
 		System.out.println("Profile document=" + profile);
 		File file = new File(profile);
 		if (!file.isFile()) {
 			throw new IllegalArgumentException("profile file " + profile +
 					" does not exist or isn't regular file");
 		}
 		try {
 			Document doc = context.getBuilder(null).build(file);
 			for(Object child : doc.getRootElement().getChildren("testAssertion")) {
 				if (!(child instanceof Element)) continue;
 				Element e = (Element)child;
 				String className = StringUtils.trimToEmpty(e.getAttributeValue("class"));
 				if (className == null) continue;
 				System.out.println(className);
 				Class objClass = Class.forName(className);
 				TestUnit testUnit = (TestUnit) objClass.newInstance();
 				load(testUnit);
 			}
 		} catch (IOException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new IOException(e);
 		}
 	}
 
	private void loadDefaultTests() {
 			// load instances of all possible tests in any order
 			// prerequisites will be ordered in the execution plan such that
 			// they're executed before those tests that require them
 
 			load(new BaseUrlNotFoundTest()); 		// 6.2.1.1
 			load(new BaseUrlGetNoAcceptTest());		// 6.2.1.2
 			load(new BaseUrlGetStarAcceptTest());		// 6.2.1.3
 			load(new BaseUrlGetTest());		 	// 6.2.1.4
 			load(new BaseUrlGetHtmlAcceptTest()); 		// 6.2.1.5
 
 			load(new BaseUrlOptions());			// 6.2.5.1
 			load(new BaseUrlOptionsSecurityHeader());	// 6.2.5.2
 			load(new BaseUrlOptionsHcpHeader());		// 6.2.5.3
 			load(new BaseUrlOptionsExtHeader());		// 6.2.5.4
 			load(new BaseUrlOptionNoBody());		// 6.2.5.6
 
 			load(new BaseUrlRootXml());			// 6.3.1.1
 
 			load(new BaseUrlRootXmlPost());			// 6.3.2.1
 			load(new BaseUrlRootXmlPut());			// 6.3.2.2
 			load(new BaseUrlRootXmlDeleteTest());		// 6.3.2.3
 
 			load(new BaseSectionFromRootXml());		// 6.4.1.1 [req=6.3.1.1]
 			load(new SectionNotFound());			// 6.4.1.2
 
 			load(new DocumentCreate());			// 6.4.2.2 [req=6.3.1.1]
 			load(new DocumentCreateCheck());		// 6.4.2.3 [req=6.4.2.2]
 
 			load(new DocumentTest());			// 6.5.1.1 [req=6.4.1.1]
 			load(new DocumentNotFound());			// 6.5.1.2
 
 			load(new DocumentUpdate());			// 6.5.2.1
 			load(new DocumentPut());			// 6.5.2.3 [req=6.4.1.1]

		} catch (ConfigurationException e) {
			log.error("", e);
		} catch (IllegalArgumentException e) {
			log.error("", e);
		}
 	}
 
 	public void execute() {
 		ExcecutionPlan exec = new ExcecutionPlan(sortedSet.iterator());
 		exec.execute();
 	}
 
 	public static Loader getInstance() {
 		return loader;
 	}
 
 	private void load(TestUnit test) throws IllegalArgumentException {
 		if (!idSet.add(test.getId())) {
 			// assertion failed
 			throw new IllegalArgumentException("Duplicate id [" + test.getId() + "] found with test " + test.getClass().getName());
 		}
 		final Class<? extends TestUnit> aClass = test.getClass();
 		for(Class<? extends TestUnit> depend : test.getDependencyClasses()) {
 			if (aClass == depend) {
 				// assertion failed
 				log.error("dependency cannot include self: " + aClass.getName());
 				return;
 			}
 		}
 		// add to hash of test units by class to order tests by dependencies
 		list.put(aClass, test);
 		sortedSet.add(test);
 	}
 
 	public Context getContext() {
 		return context;
 	}
 

 	public TestUnit getTest(Class<? extends TestUnit> aClass) {
 		return list.get(aClass);
 	}
 
 	public static void main(String[] args) {
 		final Loader loader = Loader.getInstance();
 		/*
 		for (int i=0; i <args.length; i++) {
 			if ("-xml".equals(args[i]))
 				xmlOutput = true;
 		}
 
 		*/
 		System.out.println("\nExecution:");
 		long start = System.currentTimeMillis();
 		loader.execute();
 		long elapsed = System.currentTimeMillis() - start;
 
 		int failed = loader.generateReport(elapsed);
 
 		System.exit(failed == 0 ? 0 : 1);
 	}
 
 	private static int generateReport(long elapsed) {
 		System.out.println("\n------------------------------------------------------------------------------------");
 		System.out.println("\nConformance Test Report:\n");
 		// sortedSet.addAll(loader.list.values());
 		int failed = 0;
 		int testsRun = 0;
 		int warningCount = 0 ;
 		for (TestUnit test : loader.sortedSet) {
 			TestUnit.StatusEnumType status = test.getStatus();
 			if (status == null) {
 				log.warn("Null status for test " + test.getClass().getName() + " id=" + test.getId());
 				// REVIEW: assume skipped, error condition or warning (assertion failed)
 				status = TestUnit.StatusEnumType.SKIPPED;
 			}
 			String outStatus;
 			final Set<String> warnings = test.getWarnings();
 			if (status != TestUnit.StatusEnumType.SKIPPED && status != TestUnit.StatusEnumType.PREREQ_FAILED) {
 				testsRun++;
 			}
 			if (status == TestUnit.StatusEnumType.PREREQ_FAILED) {
 				outStatus = "Prerequsite Failed";
 				failed++; // ??
 			} else if (status == TestUnit.StatusEnumType.FAILED) {
 				if (test.isRequired()) {
 					outStatus = "*FAILED*";
 					failed++;
 				} else {
 					outStatus = "warning";
 					/*
 					* Failed to meet recommended element of the specification (SHOULD, RECOMMENDED, etc.)
 					* Treated as a warning such that a test assertion failed, but the type attribute for
 					* the test assertion indicated that it was 'recommended', not 'required'.
 					* This type of failure will not affect the overall conformance result.
 					*/
 				}
 			} else if (status == TestUnit.StatusEnumType.SUCCESS && !warnings.isEmpty()) {
 				outStatus = "Success with warnings";
 			} else {
 				outStatus = status.toString(); // SUCCESS + SKIPPED
 			}
 			System.out.printf("%s: %s%n", test.getId(), outStatus);
 			String name = test.getName();
 			//if (!name.startsWith("default"))
 				System.out.println(name);
 
 			if (!warnings.isEmpty()) {
 				warningCount += warnings.size();
 				System.out.println("Warnings:");
 				for (String s : warnings) {
 					System.out.println("\t" + s);
 				}
 			} else if (status == TestUnit.StatusEnumType.FAILED && !test.isRequired()) {
 				warningCount++; // count failed recommendation as a warning
 			}
 			
 			String desc = test.getStatusDescription();
 			if (StringUtils.isNotBlank(desc)) {
 				System.out.println("Reason: " + desc);
 			}
 			final Set<? extends TestUnit> dependencies = test.getDependencies();
 			System.out.print("Prerequisites:");
 			if (dependencies.isEmpty()) {
 				System.out.println(" None");
 			} else {
 				int count = 0;
 				for(TestUnit aTest : dependencies) {
 					if (count++ != 0) System.out.print(',');
 					System.out.printf(" %s", aTest.getId());
 				}
 				System.out.println();
 			}
 
 			System.out.println();
 		}
 
 		System.out.printf("Tests run: %d, Failures: %d, Warnings: %d, Time elapsed: %.1f sec%n",
 				testsRun, failed, warningCount, elapsed / 1000.0);
 		return failed;
 	}
 
 }
