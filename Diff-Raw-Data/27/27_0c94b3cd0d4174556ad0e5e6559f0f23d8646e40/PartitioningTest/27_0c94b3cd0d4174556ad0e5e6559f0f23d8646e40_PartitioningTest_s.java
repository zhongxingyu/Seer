 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.ui.tests.text;
 
 import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.core.tests.model.SuiteOfTestCases;
 import org.eclipse.dltk.ruby.internal.ui.text.IRubyPartitions;
 import org.eclipse.dltk.ruby.ui.tests.internal.TestUtils;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.TextUtilities;
 
public class PartitioningTest extends SuiteOfTestCases {
 
 	public PartitioningTest(String name) {
 		super(name);
 	}
 
 	public void doTest(String data, String partition) throws Exception {
 		final String DELIMITER = "#$#";
 		int startPos = data.indexOf(DELIMITER);
 		Assert.isLegal(startPos >= 0);
 		data = data.substring(0, startPos)
 				+ data.substring(startPos + DELIMITER.length());
 		int endPos = data.indexOf(DELIMITER);
 		Assert.isLegal(endPos >= 0);
 		data = data.substring(0, endPos)
 				+ data.substring(endPos + DELIMITER.length());
 		String test = data.substring(startPos, endPos);
 		Document doc = new Document(data);
 		TestUtils.installStuff(doc);
 		StringBuffer expected = new StringBuffer(), actual = new StringBuffer();
 		for (int offset = startPos; offset < endPos; offset++) {
 			ITypedRegion p2 = TextUtilities.getPartition(doc,
 					IRubyPartitions.RUBY_PARTITIONING, offset,
 					(offset > startPos));
 			expected.append(offset);
 			expected.append(" ");
 			expected.append(partition);
 			expected.append("\n");
 			actual.append(offset);
 			actual.append(" ");
 			actual.append(p2.getType());
 			actual.append("\n");
 			offset = p2.getOffset() + p2.getLength();
 		}
 		assertEquals("Wrong partition at \"" + test + "\" in doc:\n" + data,
 				expected.toString(), actual.toString());
 	}
 
 	public void testCode() throws Exception {
 		doTest("#$#class Foo; end#$#", IDocument.DEFAULT_CONTENT_TYPE);
 	}
 
 	public void testString() throws Exception {
 		doTest("puts #$#\"Hello, world\"#$#, a", IRubyPartitions.RUBY_STRING);
 	}
 
 	public void testPercentStringAfterPuts() throws Exception {
 		doTest("puts #$#%s/foo bar boz/#$# / 2", IRubyPartitions.RUBY_PERCENT_STRING);
 	}
 
 	public void testPercentStringAfterMethodCall() throws Exception {
 		doTest(
 				"def foo(*args); puts(*args); end\nfoo #$#%s/foo bar boz/#$# / 2",
 				IRubyPartitions.RUBY_PERCENT_STRING);
 	}
 
 	public void testPercentOperatorAfterVariable() throws Exception {
 		// XXX: this does not start a string in Ruby, but will be treated as a
 		// string in IDE
 		doTest("foo = 20\nfoo #$#%s/foo bar boz/#$# 2",
 				IRubyPartitions.RUBY_PERCENT_STRING);
 	}
 
 	public void testPercentDoesStartString() throws Exception {
 		doTest("if a == #$#%s/2/#$# then puts 1 else puts 2 end",
 				IRubyPartitions.RUBY_PERCENT_STRING);
 	}
 
 	public void testPercentDoesNotStartString() throws Exception {
 		// XXX: this does not start a string in Ruby, but will be treated as a
 		// string in IDE
 		doTest("puts bar #$#%s/2/#$#\n3", IRubyPartitions.RUBY_PERCENT_STRING);
 	}
 
 	private void doHereDocTest(String data) throws Exception {
 		String s1 = data.replaceAll("[±ø∑]", "");
 		String s2 = data.replaceAll("±", "#$#");
 		String s3 = data.replaceAll("ø", "#$#");
 		String s4 = data.replaceAll("∑", "#$#");
 		doTest(s1, IRubyPartitions.RUBY_STRING);
 		if (!data.equals(s2))
 			doTest(s2, IRubyPartitions.RUBY_STRING);
 		if (!data.equals(s3))
 			doTest(s3, IRubyPartitions.RUBY_STRING);
 		if (!data.equals(s4))
 			doTest(s3, IRubyPartitions.RUBY_STRING);
 	}
 
 	public void REM_testAllSortsOfHeredocs() throws Exception {
 		for (int x = 0; x < 2 * 3; x++) {
 			String minus = ((x & 1) != 0 ? "-" : "");
 			String quote = ((x & 6) == 0 ? "" : ((x & 6) == 1 ? "'" : "\""));
 			doHereDocTest("puts #$#<<" + minus + quote + "HEREDOC" + quote
 					+ "#$#, 'blah'\n" + "±one two three\n"
 					+ "fourdman five six\n" + "seven eight\n" + "HEREDOC±\n"
 					+ "puts ");
 		}
 	}
 
 	public void REM_testAllSortsOfSequentialHeredocs() throws Exception {
 		for (int x = 0; x < 2 * 3; x++) {
 			String minus = ((x & 1) != 0 ? "-" : "");
 			String quote = ((x & 6) == 0 ? "" : ((x & 6) == 1 ? "'" : "\""));
 			doHereDocTest("puts #$#<<" + minus + quote + "HEREDOC" + quote
 					+ "#$#, 'blah', ø<<-BOO, 123\n" + "±one two three\n"
 					+ "fourdman five six\n" + "seven eight\n" + "HEREDOC±\n"
 					+ "∑sdfsdfdsfsdfsd\n" + "BOO∑\n" + "puts 222");
 		}
 	}
 
 	public void REM_testEmbeddedCode() throws Exception {
 		doTest(
 				"puts \"Press any #{#$#if 2 > 4 then key else reset_button end#$#} to continue\"",
 				IDocument.DEFAULT_CONTENT_TYPE);
 	}
 
 	public void testBug179488() throws Exception {
 		doTest("class Test\ndef test(x)\nobj = f($'#$#, x)\n"
 				+ "if (obj.class.name == #$#\"Array\") then\n#...\n"
 				+ "return\nend\n# It's ...\nend",
 				IDocument.DEFAULT_CONTENT_TYPE);
 		doTest("class Test\ndef test(x)\nobj = f($', x)\n"
 				+ "if (obj.class.name == #$#\"Array\"#$#) then\n#...\n"
 				+ "return\nend\n# It's ...\nend", IRubyPartitions.RUBY_STRING);
 		doTest("class Test\ndef test(x)\nobj = f($', x)\n"
 				+ "if (obj.class.name == \"Array\"#$#) then\n#$##...\n"
 				+ "return\nend\n# It's ...\nend",
 				IDocument.DEFAULT_CONTENT_TYPE);
 		doTest("class Test\ndef test(x)\nobj = f($', x)\n"
 				+ "if (obj.class.name == \"Array\") then\n#$##...\n#$#"
 				+ "return\nend\n# It's ...\nend", IRubyPartitions.RUBY_COMMENT);
 		doTest("class Test\ndef test(x)\nobj = f($', x)\n"
 				+ "if (obj.class.name == \"Array\") then\n#...\n#$#"
 				+ "return\nend\n#$## It's ...\nend",
 				IDocument.DEFAULT_CONTENT_TYPE);
 		doTest("class Test\ndef test(x)\nobj = f($', x)\n"
 				+ "if (obj.class.name == \"Array\") then\n#...\n"
 				+ "return\nend\n#$## It's ...\n#$#end",
 				IRubyPartitions.RUBY_COMMENT);
 		doTest("class Test\ndef test(x)\nobj = f($', x)\n"
 				+ "if (obj.class.name == \"Array\") then\n#...\n"
 				+ "return\nend\n# It's ...\n#$#end#$#",
 				IDocument.DEFAULT_CONTENT_TYPE);
 	}
 
 	public void testBug180370() throws Exception {
 		doTest(
 				"#$## Some metaprogramming to make it rock\n#$#"
 						+ "%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #{item}_dir_path\n" + "resolve_path(:#{item})\n"
 						+ "end\n" + "EO_METH\n"
 						+ "class_eval(method_to_eval)\n" + "end",
 				IRubyPartitions.RUBY_COMMENT);
 		doTest(
 				"# Some metaprogramming to make it rock\n"
 						+ "#$#%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}#$#.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #{item}_dir_path\n" + "resolve_path(:#{item})\n"
 						+ "end\n" + "EO_METH\n"
 						+ "class_eval(method_to_eval)\n" + "end",
 				IRubyPartitions.RUBY_PERCENT_STRING);
 		doTest(
 				"# Some metaprogramming to make it rock\n"
 						+ "%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}#$#.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #$##{item}_dir_path\n"
 						+ "resolve_path(:#{item})\n" + "end\n" + "EO_METH\n"
 						+ "class_eval(method_to_eval)\n" + "end",
 				IDocument.DEFAULT_CONTENT_TYPE);
 		doTest(
 				"# Some metaprogramming to make it rock\n"
 						+ "%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #$##{item}_dir_path\n#$#"
 						+ "resolve_path(:#{item})\n" + "end\n" + "EO_METH\n"
 						+ "class_eval(method_to_eval)\n" + "end",
 				IRubyPartitions.RUBY_COMMENT);
 		doTest(
 				"# Some metaprogramming to make it rock\n"
 						+ "%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #{item}_dir_path\n"
 						+ "#$#resolve_path(:#$##{item})\n" + "end\n"
 						+ "EO_METH\n" + "class_eval(method_to_eval)\n" + "end",
 				IDocument.DEFAULT_CONTENT_TYPE);
 		doTest(
 				"# Some metaprogramming to make it rock\n"
 						+ "%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #{item}_dir_path\n"
 						+ "resolve_path(:#$##{item})\n#$#" + "end\n"
 						+ "EO_METH\n" + "class_eval(method_to_eval)\n" + "end",
 				IRubyPartitions.RUBY_COMMENT);
 		doTest(
 				"# Some metaprogramming to make it rock\n"
 						+ "%w{app controllers models helpers views\n"
 						+ "specs spec_models spec_helpers spec_controllers spec_views spec_fixtures\n"
 						+ "tests fixtures unit_tests functional_tests integration_tests\n"
 						+ "public stylesheets javascripts images}.each do |item|\n"
 						+ "\n" + "method_to_eval = <<-EO_METH\n"
 						+ "def #{item}_dir_path\n" + "resolve_path(:#{item})\n"
 						+ "#$#end\n" + "EO_METH\n"
 						+ "class_eval(method_to_eval)\n" + "end#$#",
 				IDocument.DEFAULT_CONTENT_TYPE);
 	}
 
 }
