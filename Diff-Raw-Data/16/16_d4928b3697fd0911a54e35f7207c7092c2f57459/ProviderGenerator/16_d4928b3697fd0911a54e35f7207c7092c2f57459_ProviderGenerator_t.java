 /**
  * This file is part of the Harmony package.
  *
  * (c) Mickael Gaillard <mickael.gaillard@tactfactory.com>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 package com.tactfactory.harmony.template;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Comparator;
 import java.util.List;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.Namespace;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import com.google.common.base.CaseFormat;
 
 import com.tactfactory.harmony.meta.EntityMetadata;
 import com.tactfactory.harmony.meta.TranslationMetadata;
 import com.tactfactory.harmony.meta.TranslationMetadata.Group;
 import com.tactfactory.harmony.plateforme.BaseAdapter;
 import com.tactfactory.harmony.utils.ConsoleUtils;
 import com.tactfactory.harmony.utils.PackageUtils;
 import com.tactfactory.harmony.utils.TactFileUtils;
 
 /**
  * The provider generator.
  *
  */
 public class ProviderGenerator extends BaseGenerator {
 	/** The local name space. */
 	private String localNameSpace;
 	/** The provider name. */
 	private String nameProvider;
 
 	/**
 	 * Constructor.
 	 * @param adapter The adapter to use.
 	 * @throws Exception if adapter is null
 	 */
 	public ProviderGenerator(final BaseAdapter adapter) throws Exception {
 		super(adapter);
 
 		this.nameProvider =
 				CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,
 						this.getAppMetas().getName() + "Provider");
 		this.localNameSpace =
 				this.getAppMetas().getProjectNameSpace().replace('/', '.')
 				+ "."
 				+ this.getAdapter().getProvider();
 
 
 		this.setDatamodel(this.getAppMetas().toMap(this.getAdapter()));
 
 		this.getDatamodel().put(
 				TagConstant.LOCAL_NAMESPACE, this.localNameSpace);
 	}
 
 	/**
 	 * Generate the provider adapters.
 	 */
 	public final void generateProviderAdapters() {
 		this.makeSourceProvider(
 				"utils/base/ApplicationProviderUtilsBase.java",
 				"utils/base/ProviderUtilsBase.java",
 				true);
 
 		for (EntityMetadata cm : this.getAppMetas().getEntities().values()) {
 			if (!cm.getFields().isEmpty()) {
 
 				this.getDatamodel().put(
 						TagConstant.CURRENT_ENTITY, cm.getName());
 				this.getDatamodel().put(
						TagConstant.PROVIDER_ID, 
						this.generateProviderUriId(cm));
 
 				// Provider adapters
 				this.makeSourceProvider("TemplateProviderAdapter.java",
 						cm.getName() + "ProviderAdapter.java", false);
 				this.makeSourceProvider("base/TemplateProviderAdapterBase.java",
 						"base/" + cm.getName() + "ProviderAdapterBase.java",
 						true);
 
 
 				// Provider utils
 				if (!cm.isInternal()) {
 					this.makeSourceProvider(
 							"utils/TemplateProviderUtils.java",
 							"utils/" + cm.getName() + "ProviderUtils.java",
 							false);
 					this.makeSourceProvider(
 							"utils/base/TemplateProviderUtilsBase.java",
 							"utils/base/"
 									+ cm.getName()
 									+ "ProviderUtilsBase.java",
 							true);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Generate the provider.
 	 */
 	public final void generateProvider() {
 		try {
 			this.makeSourceProvider("TemplateProvider.java",
 					this.nameProvider + ".java", false);
 			this.makeSourceProvider("base/TemplateProviderBase.java",
 					"base/" + this.nameProvider + "Base.java", true);
 			this.makeSourceProvider("base/ProviderAdapterBase.java",
 					"base/ProviderAdapterBase.java", true);
 
 			this.generateProviderAdapters();
 
 			this.updateManifest();
 
 			TranslationMetadata.addDefaultTranslation(
 					"uri_not_supported",
 					"URI not supported",
 					Group.PROVIDER);
 			TranslationMetadata.addDefaultTranslation(
 					"app_provider_name",
 					"Provider of " + this.getAppMetas().getName(),
 					Group.PROVIDER);
 			TranslationMetadata.addDefaultTranslation(
 					"app_provider_description",
 					"Provider of "
 						+ this.getAppMetas().getName()
 						+ " to access data",
 					Group.PROVIDER);
 
 			new TranslationGenerator(this.getAdapter()).generateStringsXml();
 			new TestProviderGenerator(this.getAdapter()).generateAll();
 		} catch (final Exception e) {
 			ConsoleUtils.displayError(e);
 		}
 	}
 
 	/**
 	 * Make Java Source Code.
 	 *
 	 * @param template Template path file.
 	 * <br/>For list activity is "TemplateListActivity.java"
 	 * @param filename The destination file name
 	 * @param overwrite True if the method should overwrite all existing files.
 	 */
 	private void makeSourceProvider(final String template,
 			final String filename, final boolean overwrite) {
 
 		final String fullFilePath = String.format("%s%s/%s",
 						this.getAdapter().getSourcePath(),
 						PackageUtils.extractPath(this.localNameSpace)
 							.toLowerCase(),
 						filename);
 
 		final String fullTemplatePath = String.format("%s%s",
 				this.getAdapter().getTemplateSourceProviderPath(),
 				template);
 
 		super.makeSource(fullTemplatePath, fullFilePath, overwrite);
 	}
 
 	/**
 	 * Update Android Manifest.
 	 */
 	private void updateManifest() {
 		final String pathRelatif = String.format("%s.%s",
 				this.localNameSpace,
 				this.nameProvider);
 
 		// Debug Log
 		ConsoleUtils.displayDebug("Update Manifest : " + pathRelatif);
 
 		try {
 			// Make engine
 			final SAXBuilder builder = new SAXBuilder();
 			final File xmlFile
 				= TactFileUtils.makeFile(
 						this.getAdapter().getManifestPathFile());
 
 			// Load XML File
 			final Document doc = builder.build(xmlFile);
 
 			// Load Root element
 			final Element rootNode = doc.getRootElement();
 
 			// Load Name space (required for manipulate attributes)
 			final Namespace ns = rootNode.getNamespace("android");
 
 			// Find Application Node
 			Element findProvider = null;
 
 			// Find a element
 			final Element applicationNode = rootNode.getChild("application");
 			if (applicationNode != null) {
 
 				// Find Activity Node
 				final List<Element> providers
 						= applicationNode.getChildren("provider");
 
 				// Find many elements
 				for (final Element provider : providers) {
 					if (provider.hasAttributes()
 							&& provider.getAttributeValue("name", ns)
 									.equals(pathRelatif)) {
 						// Load attribute value
 						findProvider = provider;
 						break;
 					}
 				}
 
 				// If not found Node, create it
 				if (findProvider == null) {
 					// Create new element
 					findProvider = new Element("provider");
 
 					// Add Attributes to element
 					findProvider.setAttribute("name", pathRelatif, ns);
 
 					applicationNode.addContent(findProvider);
 				}
 
 				// Set values
 				findProvider.setAttribute("authorities",
 						this.getAppMetas().getProjectNameSpace()
 							.replace('/', '.') + ".provider",
 						ns);
 				findProvider.setAttribute("label",
 						"@string/app_provider_name",
 						ns);
 				findProvider.setAttribute("description",
 						"@string/app_provider_description",
 						ns);
 
 				// Clean code
 				applicationNode.sortChildren(new Comparator<Element>() {
 
 					@Override
 					public int compare(final Element o1, final Element o2) {
 						return o1.getName().compareToIgnoreCase(o2.getName());
 					}
 				});
 			}
 
 			// Write to File
 			final XMLOutputter xmlOutput = new XMLOutputter();
 
 			// Make beautiful file with indent !!!
 			xmlOutput.setFormat(Format.getPrettyFormat());
 			xmlOutput.output(doc,
 					new OutputStreamWriter(
 							new FileOutputStream(xmlFile.getAbsoluteFile()),
 									TactFileUtils.DEFAULT_ENCODING));
 		} catch (final IOException io) {
 			ConsoleUtils.displayError(io);
 		} catch (final JDOMException e) {
 			ConsoleUtils.displayError(e);
 		}
 	}
	
	public int generateProviderUriId(EntityMetadata em) {
		int result = 0;
		result = Math.abs(em.getName().hashCode());
		return result;
	}
 }
