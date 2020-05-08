 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.dltk.ruby.formatter.tests;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilterInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import junit.framework.Assert;
 
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.ruby.formatter.RubyFormatter;
 import org.eclipse.dltk.ruby.formatter.internal.RubyFormatterPlugin;
 import org.eclipse.dltk.ui.formatter.FormatterException;
 import org.eclipse.dltk.ui.formatter.FormatterSyntaxProblemException;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.text.edits.TextEdit;
 
 public class FormatRubyLibTest extends AbstractFormatterTest {
 
 	static final String CHARSET = "ISO-8859-1"; //$NON-NLS-1$
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	private static boolean isRubyFile(String name) {
 		return name.endsWith(".rb") || name.toLowerCase().endsWith(".rb");
 	}
 
 	public void testRubyLib() throws IOException {
		int errorCount = 0;
 		final File path = new File("/home/dltk/apps/ruby-lib.zip");
 		if (!path.isFile()) {
 			fail(path + " is not found"); //$NON-NLS-1$
 		}
 		final ZipInputStream zipInputStream = new ZipInputStream(
 				new FileInputStream(path));
 		try {
 			final RubyFormatter f = new TestRubyFormatter();
 			ZipEntry entry;
 			while ((entry = zipInputStream.getNextEntry()) != null) {
 				if (!entry.isDirectory() && isRubyFile(entry.getName())) {
 					final InputStream entryStream = new FilterInputStream(
 							zipInputStream) {
 						public void close() throws IOException {
 							// empty
 						}
 					};
 					final char[] content = Util.getInputStreamAsCharArray(
 							entryStream, (int) entry.getSize(), CHARSET);
 					final String input = new String(content);
 					try {
 						final TextEdit edit = f.format(input, 0,
 								input.length(), 0);
 						Assert.assertNotNull(entry.getName(), edit);
 						final IDocument document = new Document(input);
 						edit.apply(document);
 						assertTrue(compareIgnoreBlanks(entry.getName(),
 								new StringReader(input), new StringReader(
 										document.get())));
 					} catch (BadLocationException e) {
 						throw new RuntimeException(e);
 					} catch (FormatterSyntaxProblemException e) {
						++errorCount;
						final String msg = "Syntax error in " + entry.getName()
								+ ": " + e.getMessage();
						printError(msg);
 					} catch (FormatterException e) {
 						throw new RuntimeException(e);
 					}
 					zipInputStream.closeEntry();
 				}
 			}
 		} finally {
 			try {
 				zipInputStream.close();
 			} catch (IOException e) {
				//
 			}
 		}
		if (errorCount > 0) {
			final String msg = "Syntax errors: " + errorCount;
			printError(msg);
		}
	}

	private void printError(final String msg) {
		System.err.println(msg);
		RubyFormatterPlugin.error(msg);
 	}
 
 }
