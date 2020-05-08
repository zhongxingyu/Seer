 package cudl.w3c;
 
 import cudl.Interpreter;
 import cudl.Prompt;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import javax.script.ScriptException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 public class W3cInterpreterTest {
 	private Interpreter interpreter;
 	private List<String> fileNames = new ArrayList<String>() {
 		{
 			/*
 			 * These test file is from
 			 * http://www.w3.org/Voice/2004/vxml-ir/#test_api
 			 */
 
 			// The interpreter must be able to freely sequence TTS and audio
 			// output.
 			add("w3c/a002.txml");
 
 			// The interpreter executes content contained in the block.
 			add("w3c/259.txml");
 
 			// The interpreter visits the block when the cond attribute
 			// evaluates to true and the form item variable associated with
 			// the block is undefined.
 			add("w3c/260.txml");
 
 			// The interpreter ignores the block when the form item variable
 			// associated with the block is defined via expr.
 			add("w3c/261.txml");
 
 			// The interpreter ignores the block when the form item variable
 			// associated with the block is set via an assign.
 			add("w3c/262.txml");
 
 			// FIA ends when it encounters an <goto>.
 			add("w3c/assert111.txml");
 
 			// Using the goto nextitem will force the FIA to immediately
 			// transition to the chosen form item.
 			add("w3c/assert154.txml");
 
 			// If the last main FIA loop resulted in a goto nextitem or goto
 			// expritem then the specified form item is selected.
 			add("w3c/assert163.txml");
 
 			// FIA ends when it encounters an <submit>.
 			add("w3c/assert112.txml");
 
 			// FIA ends when it encounters an <return>.
 			add("w3c/assert114.txml");
 
 			// Common ECMAScript code can be defined in the application root
 			// and used in leaf documents.
 			add("w3c/a61leaf.txml");
 
 			// If the expr attribute specifies a valid ECMAScript expression,
 			// the value element evaluates it correctly.
 			add("w3c/377.txml");
 
 			// VoiceXML variables and ECMAScript variables are contained in
 			// the
 			// same variable space.
 			add("w3c/assertion390.txml");
 
 			// VoiceXML variables can be used in a . Variables defined in a
 			// can
 			// be used in VoiceXML.
 			add("w3c/assertion391.txml");
 
 			// Variables declared without an explicit initial value are
 			// initialized to ECMAScript undefined.
 			add("w3c/394.txml");
 
 			// Variable references match the closest enclosing scope.
 			add("w3c/408.txml");
 
 			// Variables in session scope can be read but not written by
 			// VoiceXML documents.
 			add("w3c/assertion-398.txml");
 
 			// and elements that are children of the document's element
 			// create
 			// their variables at document scope. They are no longer
 			// accessible
 			// when another document is entered.
 			add("w3c/400.txml");
 
 			// If specified, the value of the attribute is evaluated and
 			// serves
 			// as the form item variable's initial value.
 			add("w3c/234.txml");
 
 			// A variable declared at document scope is accessible within an
 			// anonymous scope contained within the same document.
 			add("w3c/510.txml");
 
 			// Declaring a variable at an anonymous scope, the variable is not
 			// accessible within another anonymous scope.
 			add("w3c/511.txml");
 
 			// A variable declared at a higher scope (e.g. document) is shadowed by
 			// a variable at a lower scope (e.g. anonymous).
 			add("w3c/512.txml");
 
 			// When declaring the same variable multiple times with different
 			// initial values in the same scope, declarations will apply in
 			// document order.
 			add("w3c/513.txml");
 
 			// When expr is set to a valid expression in an assign, the named
 			// variable is set correctly.
 			add("w3c/514.txml");
 
 			// If name is set to an undefined variable in an assign, error.semantic
 			// is thrown.
 			add("w3c/515.txml");
 
 			// If expr contains an undefined variable in an assign, error.semantic
 			// is thrown.
 			add("w3c/516.txml");
 
 			// If expr contains a semantic error (e.g.it contains a non-existent
 			// function named x), an error.semantic is thrown.
 			add("w3c/517.txml");
 
 			// "session", "application", "document", and "dialog" are not
 			// reserved words.
 			add("w3c/406.txml");
 
 			// Each scope contains a predefined variable whose name is the
 			// same
 			// as the scope that refers to the scope itself.
 			add("w3c/405.txml");
 
 			//
 			add("w3c/399main.txml");
 
 			// A VoiceXML document can initiate a transfer to another entity
 			// using the tag, such that the Interpreter remains connected to
 			// the
 			// original caller and interpretation resumes upon termination of
 			// the transfer.
 			add("w3c/288.txml");
 
 			// A element is executed in the scope of its containing element.
 			add("w3c/556.txml");
 
 			// A variable declared using the var element is accessible to a script
 			// declared at equal more local scope.
 			add("w3c/558.txml");
 
 			// A variable declared within an inline or externally referenced script
 			// block is accessible from a var or assign element declared at equal
 			// or more local scope.
 			add("w3c/559.txml");
 
 			// When the script element specifies a src attribute that references a
 			// URL that references a non-existent resource, the interpreter throws
 			// error.badfetch.
 			add("w3c/560.txml");
 
 			// Either an "src" attribute or an inline script (but not both) must be
 			// specified; otherwise, an error.badfetch event is thrown.
 			add("w3c/assertion-561a.txml");
 
 			// A VoiceXML document can initiate a transfer to another entity
 			// using the tag, such that the Interpreter disconnects from the
 			// caller immediately upon attempting the transfer and continues
 			// execution as it would under termination of the Session.
 			add("w3c/289.txml");
 
 			// A bridged transfer can contain speech grammars such that the
 			// interpreter listens to the original caller for the duration of
 			// the transfer, terminating the transfer as soon as a spoken
 			// utterance matches an active speech grammar.
 			add("w3c/290.txml");
 
 			// A element specifying a 'cond' attribute that evaluates to
 			// false
 			// upon selection of the element by the FIA is not executed.
 			add("w3c/293.txml");
 
 			// A element not specifying a 'bridge' attribute is executed as a
 			// blind transfer.
 			add("w3c/294.txml");
 
 			// A bridged transfer specifying a 'connecttimeout' attribute
 			// with a
 			// W3C time specification will terminate a transfer attempt if
 			// the
 			// destination entity cannot be connected to within that period
 			// of
 			// time.
 			add("w3c/295.txml");
 
 			// A bridged transfer specifying a 'maxtime' attribute with a W3C
 			// time specification will terminate a transfer after that period
 			// of
 			// time has elapsed after connecting to the destination entity if
 			// it
 			// has not already been terminated for other reasons.
 			add("w3c/296.txml");
 
 			// Upon a successful blind transfer, a
 			// 'connection.disconnect.transfer' event is thrown and the
 			// transfer
 			// name variable remains undefined.
 			add("w3c/298.txml");
 
 			// If the originating caller hangs up during a bridged transfer,
 			// a
 			// 'connection.disconnect.hangup' event is thrown and the
 			// transfer
 			// name variable remains undefined.
 			add("w3c/300.txml");
 
 			// If the Interpreter is unable to connect to the destination
 			// entity
 			// when attempting a transfer because it is busy, the transfer
 			// name
 			// variable is filled with the value 'busy'.
 			add("w3c/301.txml");
 
 			// If the Interpreter is unable to connect to the destination
 			// entity
 			// when attempting a transfer because network is busy, the
 			// transfer
 			// name variable is filled with the value 'network_busy'.
 			add("w3c/302.txml");
 
 			// If a URI does not refer to a document, the current document is
 			// assumed.
 			add("w3c/a11.txml");
 
 			// If a URI does not refer to a dialog, the first dialog is
 			// assumed.
 			add("w3c/a12a.txml");
 
 			// An application root document's variables are defined and
 			// reachable via the application scope upon the loading of a
 			// document that specifies it as the application root.
 			add("w3c/a24.txml");
 
 			// An application root document's variables are not reinitialized
 			// as
 			// the user transitions between documents that both specify it as
 			// the application root.
 			add("w3c/a25a.txml");
 
 			// An application root document's variables are no longer
 			// reachable
 			// from the application scope when the user transitions to a
 			// document not in that application.
 			add("w3c/a26a.txml");
 
 			// A document may have var elements.
 			add("w3c/42.txml");
 
 			// A document may have script elements.
 			add("w3c/assert_43_1.txml");
 
 			// A document may have property elements.
 			add("w3c/assert_44_1.txml");
 
 			// A document may have catch elements.
 			add("w3c/assert_45_1.txml");
 
 			// The next dialog is determined by the previous dialog.
 			add("w3c/assert_48_1.txml");
 
 			// The interpreter supports having an application root document
 			// and
 			// an application leaf document.
 			add("w3c/a56-leaf.txml");
 
 			// When a leaf document causes a root document to be loaded, none
 			// of
 			// the dialogs in the root document are executed.
 			add("w3c/a58-leaf.txml");
 
 			// Application root document variables are available for use by
 			// the
 			// leaf document.
 			add("w3c/a59leaf.txml");
 
 			// Application root catch handlers are default handlers for leaf
 			// documents.
 			add("w3c/a62-leaf.txml");
 
 			// When transitioning between two leaf documents that both specify
 			// the same application fully resolved URI then the transition must
 			// preserve the application root document's variables for use by the
 			// second leaf document.
 			add("w3c/a72-var-driver.txml");
 
 			// A transition from an application leaf document to its own
 			// application root document caused by a 'goto' must preserve the
 			// application root document's variables for use by the root
 			// document.
 			add("w3c/a73-var-driver.txml");
 
 			// If a transition occurs from an application root document to itself
 			// then it must reinitialize the application root document's variables.
 			// FIXME: ROOT document identification.
 			// add("w3c/a75-var-driver.txml");
 
 			// If a transition occurs as the result of a submit between an
 			// application leaf document and its own application root document
 			// the application root document's variables must be re-initialized
 			add("w3c/a74-var-driver.txml");
 
 			// If a transition occurs from an application root document to a
 			// different application root document it must initialize the new
 			// application root document and use the new application root
 			// document's variables.
 			add("w3c/a76-var-driver.txml");
 
 			// While executing a filled, if a throw is encountered the remaining
 			// filled actions are skipped.
 			add("w3c/assert1148.txml");
 
 			// . The input item 'field' may contain the filled element. Filled
 			// elements contain an action to execute after the result input item
 			// variable is filled in.
 			add("w3c/1037.txml");
 
 			// The default value of the attribute is ECMAScript undefined.
 			add("w3c/235.txml");
 
 			// When the interpreter executes a disconnect element, it must drop
 			// the call.
 			add("w3c/552.txml");
 
 			// When the interpreter executes a disconnect element, it must throw a
 			// catchable connection.disconnect.hangup event.
 			add("w3c/553a.txml");
 
 			// ECMAScript expressions within the PCDATA in must be evaluated in
 			// document order.
 			add("w3c/555.txml");
 
 			// The use of the log element has no side-effects on interpretation.
 			add("w3c/562.txml");
 
 			// FIXME: FIX field and filled
 			// When the namelist attribute of the clear element specifies a5
 			// specific set of one or more form item variables, only those form
 			// items are cleared.
 			// add("w3c/518.txml");
 
 			// A form may contain form items, which are subdivided into input
 			// items ( <field>, <record>, <transfer>, <object>, <subdialog>) and
 			// control items (<block> and <initial>).
 			add("w3c/assert93.txml");
 
 			// A form may contain <var> elements.
 			add("w3c/assert96.txml");
 
 			// The interpreter throws the named event.
 			add("w3c/431.txml");
 
 			// An implementation platform must support text-to-speech
 			add("w3c/a626.txml");
 			// // A invokes a new dialog that once done, returns to
 			// the
 			// // original context.
 			add("w3c/a19.txml");
 
 			// A document may contain a subdialog element.
 			add("w3c/a82-driver.txml");
 
 			// Subdialogs add a new executable context when they are invoked.
 			add("w3c/a84-driver.txml");
 
 			// A subdialog can be a new dialog within the existing document.
 			add("w3c/a85-driver.txml");
 
 			// A subdialog can be a new dialog within a new document.
 			add("w3c/a86-driver.txml");
 
 			// A subdialog can be composed of several documents.
 			add("w3c/a87-driver.txml");
 
 			// A subdialog's new context may itself invoke a subdialog.
 			add("w3c/a88-driver.txml");
 
 			// The interpreter executes the subdialog associated with the src or
 			// srcexpr attribute.
 			add("w3c/263.txml");
 
 			// If the called subdialog is in a separate document then variables
 			// from the calling document, and dialog scope are inaccessible to
 			// the subdialog.
 			add("w3c/subdialog267amain.txml");
 
 			// The variables passed to the subdialog via the <param> element are
 			// accessible as variables within the dialog scope of the invoked
 			// subdialog. A <param> overrides the corresponding <var> expr
 			// attribute which is ignored.
 			add("w3c/subdialog268.txml");
 
 			// If the subdialog returns a namelist, the filled element contained
 			// by the subdialog is executed upon return from the subdialog.
 			add("w3c/subdialog271.txml");
 
 			// When is contained in a element, the values specified by it are
 			// used to initialize dialog elements in the subdialog that is
 			// invoked.
 			add("w3c/612.txml");
 
 			// When the attribute evaluates to false, the interpreter does not
 			// execute the element or its contents.
 			add("w3c/614.txml");
 
 			// Subdialog results are accessed through properties of the variable
 			// defined by the name attribute of the subdialog element.
 			add("w3c/a654-driver.txml");
 
 			// If subdialog execution calls a second subdialog execution, when
 			// the second dialog returns, control is returned directly to the
 			// calling subdialog dialog.
 			// FIXME: variable scope
 			// add("w3c/a656.txml");
 
 			// The input item subdialog may contain the filled element. Filled
 			// elements contain an action to execute after the result input item
 			// variable is filled in.
 			add("w3c/1038.txml");
 
 			// When the subdialog returns, its execution context is deleted. All
 			// subdialog context variable bindings are lost.
 			add("w3c/subdialog1156.txml");
 
 			// When there is no fragment, the subdialog invoked is the lexically
 			// first dialog in the document.
 			add("w3c/subdialog1158main.txml");
 
 			// Setting next to a relative URL (excluding fragment identifier) that
 			// points to an existing VoiceXML document causes the INTERPRETER to
 			// transition to that document and begin execution of the first form.
 			add("w3c/527a1.txml");
 
 			// Setting next to a fully-qualified URL including fragment identifier
 			// that points to an existing VoiceXML document and form causes the
 			// INTERPRETER to transition to the document and begin execution of the
 			// specified form.
 			// TODO: ADD FILE
 			// add("w3c/528a1.txml");
 
 			// Setting next to a fully-qualified URL (excluding fragment
 			// identifier) that points to an existing VoiceXML document causes the
 			// INTERPRETER to transition to that document and begin execution of
 			// the first form.
 			// add("w3c/526.txml");
 
 			// Setting next to a relative URL including fragment identifier that
 			// points to an existing VoiceXML document and form causes the
 			// INTERPRETER to transition to that document and begin execution of
 			// the specified form.
 			add("w3c/529a1.txml");
 
 			// Setting expr to an ECMAScript expression that evaluates to a
 			// fully-qualified URL (excluding fragment identifier) that points to
 			// an existing VoiceXML document causes the INTERPRETER to transition
 			// to that document and begin execution of the first form.
 			// add("w3c/530a1.txml");
 
 			// Setting next to a URL that points to a non-existent VoiceXML
 			// document causes the interpreter to throw a catchable error.badfetch
 			// event.
 			// FIXME: catch FileNotFoundException raise
 			// EventException("error.badfetch.http.404");
 			// TODO: after fix add 532.txml file
 			// add("w3c/531a1.txml");
 
 			// FIXME: Variable initialization
 			// add("w3c/534.txml");
 
 			// Setting next to a fragment identifier that identifies an existing
 			// form in the current document causes the INTERPRETER to transition to
 			// that form with the state of the current document and application in
 			// tact.
 			add("w3c/533.txml");
 
 			// Exactly one of "next" or "expr" must be specified; otherwise, an
 			// error.badfetch event is thrown.
 			add("w3c/543.txml");
 
 			// The element does not throw an "exit" event.
 			add("w3c/1162.txml");
 
 			// The label attribute may be used, for example, to indicate the
 			// purpose of the log.
 			add("w3c/1161.txml");
 
 			// EXIT : If both "expr" and "namelist" attributes of are specified, an
 			// error.badfetch event is thrown.
 			add("w3c/545m.txml");
 
 			// A VoiceXML interpreter must execute the content within the selected
 			// event handler.
 			add("w3c/436.txml");
 
 			// When the prompt element's attribute evaluates to false, the
 			// interpreter does not execute the element or its contents.
 			add("w3c/623.txml");
 
 			// If neither 'src' nor 'expr' are specified on an audio element, an
 			// error.badfetch event is thrown.
 			add("w3c/1135.txml");
 
 			// The interpreter fetches and plays the URI associated with the src
 			// attribute of the audio element.
 			// ===> audio is not player
 			add("w3c/369.txml");
 
 			// If src and expr are specified on an audio element, error.badfetch is
 			// thrown.
 			add("w3c/370.txml");
 
 			// If the expr attribute specifies an invalid ECMAScript expression,
 			// error.semantic is thrown.
 			add("w3c/378.txml");
 
 			// The form item has a result variable, specified by the attribute.
 			// This variable may be given an initial value with the attribute.
 			add("w3c/1026.txml");
 
 			// When the element's attribute evaluates to false, the interpreter
 			// does not execute the element or its contents.
 			add("w3c/subdialog621.txml");
 
 			// The variable associated with the attribute of the holds the
 			// recognition result.
 			// add("w3c/232.txml");
 		}
 	};
 	private String url;
 	private Prompt prompt;
 
 	@Before
 	public void setUp() throws IOException {
 		url = "file:" + new File(".").getCanonicalPath() + "/test/docVxml/";
 		prompt = new Prompt();
 		prompt.tts = "pass";
 	}
 
 	@Test()
 	@Ignore
 	public void w3cIRTest() throws IOException, ScriptException, ParserConfigurationException, SAXException {
 
 		int count = 0;
 		for (Iterator<String> iterator = fileNames.iterator(); iterator.hasNext();) {
 			String fileName = iterator.next();
 
 			interpreter = new Interpreter(url + fileName);
 			interpreter.start();
 
 			// System.out.println("tmp =" + tmp);
 			if (fileName.endsWith("289.txml") || fileName.endsWith("294.txml") || fileName.endsWith("298.txml")) {
 				interpreter.blindTransferSuccess();
 			} else if (fileName.endsWith("288.txml")) {
 				interpreter.destinationHangup();
 			} else if (fileName.endsWith("290.txml")) {
 				interpreter.callerHangupDuringTransfer();
 			} else if (fileName.endsWith("300.txml")) {
 				interpreter.disconnect();
 			} else if (fileName.endsWith("295.txml")) {
 				interpreter.noAnswer();
 			} else if (fileName.endsWith("296.txml")) {
 				interpreter.transferTimeout();
 			} else if (fileName.endsWith("301.txml")) {
 				interpreter.destinationBusy();
 			} else if (fileName.endsWith("302.txml")) {
 				interpreter.networkBusy();
 			} else if (fileName.endsWith("assert1148.txml") || fileName.endsWith("1037.txml")) {
 				interpreter.talk("alpha");
 			} else if (fileName.endsWith("235.txml")) {
 				interpreter.noInput();
 			} else if (fileName.endsWith("assert163.txml")) {
 				interpreter.talk("alpha");
 				interpreter.talk("alpha");
 			} else if (fileName.endsWith("556.txml")) {
 				interpreter.talk("alpha");
 			}
 
 			List<Prompt> prompts = interpreter.getPrompts();
 			int i = prompts.size() - 1;
 			System.err.println("" + getClass().getName() + "    " + i + interpreter.getPrompts());
 			if (!(prompt.equals(prompts.get(i)))) {
 				System.out.println(count + " tests of " + fileNames.size());
 			}
 
 			assertEquals(prompt, prompts.get(i));
 			count++;
 			System.out.println(fileName + " test pass");
 		}
 		System.out.println(count + " tests of " + fileNames.size());
 	}
 
 	@Test
 	@Ignore
 	public void automatiqueW3cTest() throws IOException, ParserConfigurationException, SAXException {
 		url = new File(".").getCanonicalPath() + "/test/docVxml/w3c/automatic_test/";
 		File file = new File(url);
 
 		String[] list = file.list();
 		for (int i = 0; i < list.length; i++) {
 			String url2 = "file:" + url + list[i];
 			System.err.println(url2);
 			interpreter = new Interpreter(url2);
 			interpreter.start();
 
 			assertTrue(interpreter.hangup());
 			System.out.println("Test " + list[i] + "  passed");
 		}
 	}
 
     private void txmlToVxml(String fileName) throws Exception {
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer(new StreamSource(url + "/w3c/irtest.xslt"));
 
         String outputFileName = (url + "/w3c/xslt_output/").replaceAll("file:/*", "/");
         transformer.transform(new StreamSource(url + "/w3c/" +fileName+".txml"), new StreamResult(new FileOutputStream(outputFileName + fileName+".vxml")));
     }
 
 	@Test
 	public void w3cManual1() throws Exception {
 		// If the last main FIA loop did not result in a goto nextitem
 		// and there is no form item which is eligible to be visited
 		// then an implicit exit is generated.
 
 		// Read file
 
         txmlToVxml("assert165");
         interpreter = new Interpreter(url + "/w3c/xslt_output/assert165.vxml");
 		interpreter.start();
 
 		assertTrue(interpreter.hangup());
 	}
 
 	@Test
 	public void w3cEnumerate218() throws Exception {
 		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
 		prompt = new Prompt();
 		prompt.tts = "alpha; bravo; charlie; delta.";
 		expectedPrompts.add(prompt);
 
         txmlToVxml("218");
 		interpreter = new Interpreter(url + "/w3c/xslt_output/218.vxml");
 		interpreter.start();
 
 		assertEquals(expectedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	public void w3cEnumerateInNoInput219() throws Exception {
 		prompt = new Prompt();
 		prompt.tts = "alpha; bravo; charlie; delta.";
 
         txmlToVxml("219");
 		interpreter = new Interpreter(url + "w3c/xslt_output/219.vxml");
 		interpreter.start();
         interpreter.noInput();
         interpreter.noInput();
 
 		assertTrue(interpreter.hangup());
         List<Prompt> prompts = interpreter.getPrompts();
         assertEquals(prompt, prompts.get(prompts.size() - 1));
 	}
 
 	@Test
 	public void w3cEnumerateWithoutContent216() throws Exception {
 		List<Prompt> exceptedPrompts = new ArrayList<Prompt>();
 		prompt = new Prompt();
 		prompt.tts = "alpha; bravo; charlie; delta.";
 		exceptedPrompts.add(prompt);
 
         txmlToVxml("216");
 		interpreter = new Interpreter(url + "w3c/xslt_output/216.vxml");
 		interpreter.start();
 		interpreter.noInput();
 
 		assertTrue(interpreter.hangup());
 		assertEquals(exceptedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	public void w3cEnumerateGeneratedVariables217() throws Exception {
 		List<Prompt> exceptedPrompts = new ArrayList<Prompt>();
 		prompt = new Prompt();
 		prompt.tts = "For alpha, press 1.0 For bravo, press 2 For charlie, press 3 For delta, press 4";
 		exceptedPrompts.add(prompt);
 
         txmlToVxml("217");
 		interpreter = new Interpreter(url + "w3c/xslt_output/217.vxml");
 		interpreter.start();
 		interpreter.noInput();
 
 		assertTrue(interpreter.hangup());
 		assertEquals(exceptedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
	public void whenErrorOccuredTheInterpreterThrowError() throws Exception {
         txmlToVxml("a32");
 		interpreter = new Interpreter(url +  "w3c/xslt_output/a32.vxml");
 		interpreter.start();
 
 		assertFalse(interpreter.getPrompts().isEmpty());
 		assertTrue(interpreter.getPrompts().get(0).tts.equals("pass"));
 	}
 
 	@Test
	@Ignore
	public void w3c236Test() throws IOException, ScriptException, ParserConfigurationException, SAXException {
		List<Prompt> exceptedPrompts = new ArrayList<Prompt>();
 		prompt = new Prompt();
 		prompt.tts = "pass";
		exceptedPrompts.add(prompt);
 
		interpreter = new Interpreter(url + "w3c/236.txml");
 		interpreter.start();
 		interpreter.noInput();
		assertEquals(exceptedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	@Ignore
 	// ADD reprompt implementation
 	public void w3c523Test() throws IOException, ParserConfigurationException, SAXException {
 		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
 
 		interpreter = new Interpreter(url + "w3c/523.txml");
 		interpreter.start();
 
 		System.err.println(expectedPrompts);
 		System.err.println(interpreter.getPrompts());
 		assertEquals(expectedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	@Ignore
 	public void w3c524Test() throws IOException, ParserConfigurationException, SAXException {
 		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
 
 		Prompt prompt = new Prompt();
 		prompt.tts = "First prompt implicitly declared execution.";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = "1.0";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = ".";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = "First prompt expressly declared execution. 2.";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = "Second prompt implicitly declared execution.";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = "3";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = ".";
 		expectedPrompts.add(prompt);
 
 		prompt = new Prompt();
 		prompt.tts = "pass";
 		expectedPrompts.add(prompt);
 
 		interpreter = new Interpreter(url + "w3c/524.txml");
 		interpreter.start();
 
 		assertEquals(expectedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	@Ignore
 	public void shouldPlayAudioExprSpecified() throws IOException, ParserConfigurationException, SAXException {
 		// The interpreter evaluates, fetches, and plays the URI associated with
 		// the expr attribute of the audio element.
 		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
 
 		Prompt prompt = new Prompt();
 		prompt.audio = "371.wav";
 		expectedPrompts.add(prompt);
 
 		interpreter = new Interpreter(url + "w3c/371.txml");
 		interpreter.start();
 
 		assertEquals(expectedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	@Ignore
 	public void testPromptCounterInInputTagTakeEffect() throws Exception {
 		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
 
 		Prompt p = new Prompt();
 		p.tts = "Say nothing. 1.0";
 		expectedPrompts.add(p);
 
 		Interpreter interpreter = new Interpreter(url + "w3c/525.txml");
 		interpreter.start();
 
 		System.err.println(expectedPrompts);
 		System.err.println(interpreter.getPrompts());
 		assertEquals(expectedPrompts, interpreter.getPrompts());
 	}
 
 	@Test
 	@Ignore
 	public void testReprompt() throws Exception {
 		// NOTE: This test uses expr side-effects.
 		// The user interaction follows:
 		// computer: Say nothing 1.
 		// user : (silence)
 		// computer: caught no input. Say New York 2.
 		// user : New York.
 		// computer: pass!
 		List<Prompt> expectedPrompts = new ArrayList<Prompt>();
 
 		Prompt p = new Prompt();
 		p.tts = "Say nothing. 1.0";
 		expectedPrompts.add(p);
 
 		p = new Prompt();
 		p.tts = "Say New York. 2";
 		expectedPrompts.add(p);
 
 		p = new Prompt();
 		p.tts = "pass";
 		expectedPrompts.add(p);
 
 		Interpreter interpreter = new Interpreter(url + "w3c/525.txml");
 		interpreter.start();
 		interpreter.noInput();
 		interpreter.talk("New York");
 
 		System.err.println(expectedPrompts);
 		System.err.println(interpreter.getPrompts());
 		assertEquals(expectedPrompts, interpreter.getPrompts());
 	}
 }
