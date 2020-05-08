 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 package org.jenkinsci.plugins.darcs;
 
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.ChangeLogSet;
 import hudson.scm.ChangeLogSet.Entry;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * Parses the output of darcs log.
 * 
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsChangeLogParser extends ChangeLogParser {
 
	private static final Logger LOGGER = Logger.getLogger(DarcsScm.class.getName());
 
	@Override
	public ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
		LOGGER.info("Parsing changelog file " + changelogFile.toString());
 
		XMLReader xr = XMLReaderFactory.createXMLReader();
		DarcsSaxHandler handler = new DarcsSaxHandler();
		FileReader r = new FileReader(changelogFile);
 
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		xr.parse(new InputSource(r));
 
		return new DarcsChangeSetList(build, handler.getChangeSets());
	}
 }
