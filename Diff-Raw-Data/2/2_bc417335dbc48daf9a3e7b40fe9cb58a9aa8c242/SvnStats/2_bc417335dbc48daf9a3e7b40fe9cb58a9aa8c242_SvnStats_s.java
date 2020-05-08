 package com.redhat.automationportal.scripts.svnstats;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import com.redhat.automationportal.base.AutomationBase;
 import com.redhat.ecs.commonutils.FileUtilities;
 import com.redhat.ecs.commonutils.HTMLUtilities;
 
 /**
  * 	Requires:
  *	pysvn http://fedoraproject.org/wiki/EPEL
  *	python-lxml
  */
 public class SvnStats extends AutomationBase
 {
 	public static final String BUILD = "20111031-1013";
 	private static final String TEMPLATE_DIR = "/opt/automation-interface/svn_stats";
 	private static final String HTML_SINGLE_BUILD_DIR = "/tmp/en-US/html-single";
 	private final SimpleDateFormat xmlFormatter = new SimpleDateFormat("dd-MM-yyyy");
 
 	@Override
 	public String getBuild()
 	{
 		return BUILD;
 	}
 
 	private String getConfigXml(final List<ConfigXMLData> configDataItems)
 	{
 		String configXml = "<config>";
 
 		for (final ConfigXMLData data : configDataItems)
 		{
 			configXml += "<entry from_date=\"" + xmlFormatter.format(data.getFromDate()) + "\" " + "path=\"" + data.getPath() + "\">" + data.getEntry() + "</entry>";
 		}
 
 		configXml += "</config>";
 
 		return configXml;
 	}
 
 	public boolean run(final List<ConfigXMLData> configDataItems)
 	{
 		final Integer randomInt = this.generateRandomInt();
 		
 		this.message = "";
 		this.output = "";
 		
 		if (configDataItems.size() != 0)
 		{
 			final String configXML = cleanStringForBash(getConfigXml(configDataItems));
 			final String configXMLEscaped = configXML.replace("\"", "\\\"");
 			
 			final String script =
 			// copy the template files
 			"cp -R \\\"" + TEMPLATE_DIR + "/\\\"* \\\"" + this.getTmpDirectory(randomInt) + "\\\" " +
 
 			// dump the new config.xml file
			"&& echo '" + configXMLEscaped + "' > \\\"" + this.getTmpDirectory(randomInt) + "/config.xml\\\" " +
 
 			// enter the scripts directory
 			"&& cd \\\"" + this.getTmpDirectory(randomInt) + "/scripts\\\" " +
 
 			// run the python script
 			"&& python run.py " + "&& cd \\\"" + this.getTmpDirectory(randomInt) + "\\\" " + "&& publican build --formats=html-single --langs=en-US";
 			
 			runScript(script, randomInt);
 
 			if (this.success)
 			{
 				final File htmlSingle = new File(this.getTmpDirectory(randomInt) + HTML_SINGLE_BUILD_DIR + "/index.html");
 				if (htmlSingle.exists())
 				{
 					final String indexString = FileUtilities.readFileContents(htmlSingle);
 					final String inlinedIndexString = HTMLUtilities.inlineHtmlPage(indexString, this.getTmpDirectory(randomInt) + HTML_SINGLE_BUILD_DIR);
 					this.output = inlinedIndexString;
 				}
 				else
 				{
 					this.output = "=== OUTPUT LOG ===\n" + this.output;
 					this.success = false;
 				}
 			}
 
 			// cleanup the temp dir
 			cleanup(randomInt);
 			
 			return success;
 		}
 		else
 		{
 			this.message = "Please add some config data.";
 			return false;
 		}
 	}
 }
 
 
 
