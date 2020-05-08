 package com.redhat.automationportal.scripts;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import com.redhat.automationportal.base.AutomationBase;
 import com.redhat.automationportal.rest.StringPair;
 import com.redhat.ecs.commonutils.InetUtilities;
 import com.redhat.ecs.commonutils.XMLUtilities;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Requires:
  * 
  * /opt/automation-interface/cvs/dummyeditor.sh to avoid editor prompts and
  * errors. The script just needs to change the time stamp and add something to
  * the file.
  * 
  * #!/bin/sh echo "Automation Interface TOC Update" > $1 sleep 1 touch $1 exit 0
  * 
  * EDITOR environment variable needs to point to
  * /opt/automation-interface/cvs/dummyeditor.sh
  * 
  * Set "StrictHostKeyChecking no" in /etc/ssh/ssh_config to avoid key issues
  * with cvs in a script
  * 
  * empty http://empty.sourceforge.net/">http://empty.sourceforge.net/ brewkoji
  * http://download.devel.redhat.com/rel-eng/brew/rhel/6/ publican &gt;= 2.6
  * http://porkchop.redhat.com/rel-eng/repos/eng-rhel-6/x86_64/ regensplash.rb
  * https://engineering.redhat.com/trac/ContentServices/wiki/RegenSplash
  * publican_build https://svn.devel.redhat.com/repos/ecs/toolkit/publican_build/
  * publican_build rhpkg
  * http://download.devel.redhat.com/rel-eng/dist-git/rhel/rhpkg.repo
  */
 public class RegenSplash extends AutomationBase
 {
 	private static String BUILD = "20120416-1013";
 	// private static final String PRODUCT_RE =
 	// "<span id=\".*?\" class=\"product\">(?<product>.*?)</span>";
 	private static final String PASSWORD_ENV_VARIABLE_NAME = "PASSWORD";
 	private static final String SCRIPT_NAME = "regensplash.rb";
 	private static final String SCRIPT_PATH = "/opt/automation-interface/regensplash/" + SCRIPT_NAME;
 	private static final String DOCS_STAGE_DUMP_XML = "http://documentation-stage.bne.redhat.com/DUMP.xml";
 	private static final String DOCS_REDHAT_DUMP_XML = "http://docs.redhat.com/DUMP.xml";
 	private static final String DOCS_STAGE_TOC_URL = "http://documentation-stage.bne.redhat.com/docs/toc.html";
 	private static final String DOCS_REDHAT_TOC_URL = "http://docs.redhat.com/docs/toc.html";
 	private String product;
 	private String selectedSite;
 	private final List<StringPair> sites;
 
 	public RegenSplash()
 	{
 		this.sites = new ArrayList<StringPair>();
 		this.sites.add(new StringPair(DOCS_REDHAT_DUMP_XML, "Redhat Docs (Can take a few seconds to get the products)"));
 		this.sites.add(new StringPair(DOCS_STAGE_DUMP_XML, "Docs Stage"));
 	}
 
 	public List<StringPair> getSites()
 	{
 		return sites;
 	}
 
 	@Override
 	public String getBuild()
 	{
 		return BUILD;
 	}
 
 	public void setProduct(final String product)
 	{
 		this.product = product;
 	}
 
 	public String getProduct()
 	{
 		return product;
 	}
 
 	public void setSelectedSite(String selectedSite)
 	{
 		this.selectedSite = selectedSite;
 	}
 
 	public String getSelectedSite()
 	{
 		return selectedSite;
 	}
 
 	public List<String> getProducts(final String tocURL)
 	{
 		final List<String> products = new ArrayList<String>();
 
 		// add an empty string to represent a null selection
 		products.add("");
 
 		final String toc = new String(InetUtilities.getURLData(tocURL));
 
 		final Document doc = XMLUtilities.convertStringToDocument(toc);
 
 		if (doc != null)
 		{
 			final List<Node> productNodes = XMLUtilities.getNodes(doc.getDocumentElement(), "product");
 			for (final Node node : productNodes)
 			{
 				final String productName = node.getTextContent().replaceAll("_", " ").replaceAll("&#x200B;", "").trim();
 				if (!products.contains(productName))
 				{
 					products.add(productName);
 				}
 			}
 		}
 
 		Collections.sort(products);
 
 		return products;
 	}
 
 	public boolean run()
 	{
 		final Integer randomInt = this.generateRandomInt();
 
 		if (!validateInput())
 			return false;
 
 		String tocURL = "";
 		if (this.selectedSite.equals(DOCS_STAGE_DUMP_XML))
 			tocURL = DOCS_STAGE_TOC_URL;
 		else if (this.selectedSite.equals(DOCS_REDHAT_DUMP_XML))
 			tocURL = DOCS_REDHAT_TOC_URL;
 
 		final String script =
 		/*
 		 * Publican requires a Kerberos ticket to interact with the CVS repo.
 		 * The Map we supply to the runScript function below contains the
 		 * challenge / response data needed to supply the password.
 		 */
 		"kinit \\\"" + this.getUsername() + "\\\" " +
 		 
 		/* have to override the rpm build path */
		"&& echo \\\"%_topdir ${HOME}/rpmbuild\\\" > ${HOME}/.rpmmacros " +
 
 		/* copy the script to the temporary directory */
 		"&& cp \\\"" + SCRIPT_PATH + "\\\" \\\"" + getTmpDirectory(randomInt) + "\\\" " +
 
 		/* create a temporary directory to hold the downloaded files */
 		"&& cd \\\"" + getTmpDirectory(randomInt) + "\\\" " +
 
 		/* run the regenplash.rb script */
 		"&& ruby " + SCRIPT_NAME + " \\\"" + tocURL + "\\\"" + (this.product != null && !this.product.isEmpty() ? " \\\"" + this.product + "\\\" " : " ") +
 
 		/*
 		 * dump the contents of the version_packages.txt and
 		 * product_packages.txt files
 		 */
 		"&& echo -------------------" + "&& echo version_packages.txt " + "&& echo -------------------" + "&& cat \\\"" + getTmpDirectory(randomInt) + "/version_packages.txt\\\" " + "&& echo -------------------" + "&& echo product_packages.txt " + "&& echo -------------------" + "&& cat \\\""
 				+ getTmpDirectory(randomInt) + "/product_packages.txt\\\"";
 
 		/*
 		 * Define some environment variables. The password is placed in a
 		 * variable to prevent it from showing up in a ps listing. The CVSEDITOR
 		 * variable is used to set the "editor" used by cvs to generate log
 		 * messages. In this case the editor is just a script that changes the
 		 * contents of the supplied file, and updates the time stamp:
 		 */
 
 		// #!/bin/sh
 		// # add a generic message echo
 		// "Automation Interface TOC Update" > $1
 		// # update the time stamp (has to be different by at least a second)
 		// sleep 1
 		// touch $1
 		// exit 0
 
 		/*
 		 * The rhpkg application needs a home directory to create the rpmbuild
 		 * directory. Because we are using the user account under su, there is
 		 * no guarantee that the home directory is available. So we override the
 		 * HOME environment variable and set it to the temporary directory that
 		 * has been created for the duration of this script.
 		 */
 		final String[] environment = new String[]
 		{ "HOME=" + getTmpDirectory(randomInt), PASSWORD_ENV_VARIABLE_NAME + "=" + this.getPassword(), "CVSEDITOR=/opt/automation-interface/cvs/dummyeditor.sh" };
 
 		/*
 		 * The kinit command will expect to be fed a password for the current
 		 * user. Here we define a challenge of REDHAT.COM to be responded with
 		 * the users password.
 		 * 
 		 * Use an environment variable to hold sensitive strings, like the
 		 * password. This prevents the strings from showing up in a ps listing.
 		 */
 		final LinkedHashMap<String, String> responses = new LinkedHashMap<String, String>();
 		responses.put("REDHAT.COM", "${" + PASSWORD_ENV_VARIABLE_NAME + "}");
 		//responses.put("REDHAT.COM", this.getPassword());
 
 		runScript(script, randomInt, true, true, true, responses, environment);
 
 		cleanup(randomInt);
 
 		return true;
 	}
 
 	@Override
 	protected boolean validateInput()
 	{
 		if (super.validateInput())
 		{
 			if (selectedSite == null || selectedSite.length() == 0)
 			{
 				this.message = "You need to specify a site.";
 				return false;
 			}
 
 			boolean foundSite = false;
 			for (final StringPair item : this.sites)
 			{
 				if (item.getFirstString().equals(this.selectedSite))
 				{
 					foundSite = true;
 					break;
 				}
 			}
 
 			if (!foundSite)
 			{
 				this.message = "The site is not valid.";
 				return false;
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 }
