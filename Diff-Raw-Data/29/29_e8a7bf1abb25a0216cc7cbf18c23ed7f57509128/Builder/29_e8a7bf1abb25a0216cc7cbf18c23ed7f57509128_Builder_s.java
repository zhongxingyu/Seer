 package org.eclipse.b3.aggregator.engine;
 
 import static java.lang.String.format;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.util.DateUtils;
 import org.apache.tools.mail.MailMessage;
 import org.eclipse.b3.aggregator.Aggregation;
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.Contact;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.CustomCategory;
 import org.eclipse.b3.aggregator.Feature;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.MavenMapping;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.PackedStrategy;
 import org.eclipse.b3.aggregator.ValidationSet;
 import org.eclipse.b3.aggregator.engine.maven.InstallableUnitMapping;
 import org.eclipse.b3.aggregator.engine.maven.MavenManager;
 import org.eclipse.b3.aggregator.engine.maven.MavenRepositoryHelper;
 import org.eclipse.b3.aggregator.impl.MetadataRepositoryReferenceImpl;
 import org.eclipse.b3.aggregator.util.ResourceUtils;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.loader.IRepositoryLoader;
 import org.eclipse.b3.p2.maven.indexer.IMaven2Indexer;
 import org.eclipse.b3.p2.maven.indexer.IndexerUtils;
 import org.eclipse.b3.p2.util.IUUtils;
 import org.eclipse.b3.p2.util.P2ResourceImpl;
 import org.eclipse.b3.p2.util.P2Utils;
 import org.eclipse.b3.p2.util.RepositoryLoaderUtils;
 import org.eclipse.b3.p2.util.ResourceSetWithAgent;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.b3.util.IOUtils;
 import org.eclipse.b3.util.LogUtils;
 import org.eclipse.b3.util.MonitorUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository;
 import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactDescriptor;
 import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
 import org.eclipse.equinox.internal.p2.core.helpers.FileUtils;
 import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
 import org.eclipse.equinox.internal.p2.repository.Transport;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.engine.IProfile;
 import org.eclipse.equinox.p2.engine.IProfileRegistry;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
 import org.eclipse.equinox.p2.publisher.Publisher;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.repository.IRepository;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
 import org.eclipse.equinox.p2.repository.artifact.spi.ArtifactDescriptor;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.kohsuke.args4j.OptionDef;
 import org.kohsuke.args4j.spi.OptionHandler;
 import org.kohsuke.args4j.spi.Parameters;
 import org.kohsuke.args4j.spi.Setter;
 
 public class Builder extends ModelAbstractCommand {
 	public enum ActionType {
 		CLEAN, VALIDATE, BUILD, CLEAN_BUILD
 	}
 
 	private static class EmailAddress {
 		private final String address;
 
 		private final String personal;
 
 		EmailAddress(String address, String personal) {
 			this.address = address;
 			this.personal = personal;
 		}
 
 		@Override
 		public String toString() {
 			if(personal == null)
 				return address;
 
 			return personal + " <" + address + ">";
 		}
 	}
 
 	public static class PatternOptionHandler extends OptionHandler<Pattern> {
 		public PatternOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super Pattern> setter) {
 			super(parser, option, setter);
 		}
 
 		@Override
 		public String getDefaultMetaVariable() {
 			return "<regexp>";
 		}
 
 		@Override
 		public int parseArguments(Parameters params) throws CmdLineException {
 			String token = params.getParameter(0);
 			try {
 				Pattern value = Pattern.compile(token);
 				setter.addValue(value);
 			}
 			catch(PatternSyntaxException ex) {
 				throw new CmdLineException(owner, "");
 			}
 			return 1;
 		}
 	}
 
 	class ValidationSetAnnotations {
 		private String safeName;
 
 		private URI sourceCompositeURI;
 
 		private CompositeMetadataRepository sourceComposite;
 
 		private Set<IInstallableUnit> unitsToAggregate;
 
 		public String getSafeName() {
 			return safeName;
 		}
 
 		public CompositeMetadataRepository getSourceComposite() {
 			return sourceComposite;
 		}
 
 		public URI getSourceCompositeURI() {
 			return sourceCompositeURI;
 		}
 
 		public Set<IInstallableUnit> getUnitsToAggregate() {
 			return unitsToAggregate;
 		}
 
 		public void setSafeName(String safeName) {
 			this.safeName = safeName;
 		}
 
 		public void setSourceComposite(CompositeMetadataRepository sourceComposite) {
 			this.sourceComposite = sourceComposite;
 		}
 
 		public void setSourceCompositeURI(URI sourceCompositeURI) {
 			this.sourceCompositeURI = sourceCompositeURI;
 		}
 
 		public void setUnitsToAggregate(Set<IInstallableUnit> unitsToAggregate) {
 			this.unitsToAggregate = unitsToAggregate;
 		}
 
 	}
 
 	public static final String PDE_TARGET_PLATFORM_NAMESPACE = "A.PDE.Target.Platform";
 
 	public static final String PDE_TARGET_PLATFORM_NAME = "Cannot be installed into the IDE";
 
 	public static final Version ALL_CONTRIBUTED_CONTENT_VERSION = Version.createOSGi(1, 0, 0);
 
 	public static final String COMPOSITE_ARTIFACTS_TYPE = org.eclipse.equinox.internal.p2.artifact.repository.Activator.ID +
 			".compositeRepository"; //$NON-NLS-1$
 
 	public static final String COMPOSITE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID +
 			".compositeRepository"; //$NON-NLS-1$
 
 	public static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
 
 	public static final String NAMESPACE_OSGI_BUNDLE = "osgi.bundle"; //$NON-NLS-1$
 
 	public static final String PROFILE_ID = "b3AggregatorProfile"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_VERIFICATION = "verification"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_FINAL = "final"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_INTERIM = "interim"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_TEMP = "temp"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_AGGREGATE = "aggregate"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_CUSTOM_CATEGORIES = "/custom_categories";
 
 	public static final String SIMPLE_ARTIFACTS_TYPE = org.eclipse.equinox.internal.p2.artifact.repository.Activator.ID +
 			".simpleRepository"; //$NON-NLS-1$
 
 	public static final String SIMPLE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID +
 			".simpleRepository"; //$NON-NLS-1$
 
 	public static final String INTERNAL_METADATA_TYPE = "org.eclipse.b3.aggregator.engine.internalRepository"; //$NON-NLS-1$
 
 	public static final String PROP_AGGREGATOR_GENERATED_IU = "org.eclipse.b3.aggregator.generated.IU"; //$NON-NLS-1$
 
 	public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmm"); //$NON-NLS-1$
 
 	static final String FEATURE_GROUP_SUFFIX = ".feature.group"; //$NON-NLS-1$
 
 	static final IArtifactKey[] NO_ARTIFACT_KEYS = new IArtifactKey[0];
 
 	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
 
 	private static final Project PROPERTY_REPLACER = new Project();
 
 	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmm"); //$NON-NLS-1$
 
 	static {
 		TimeZone utc = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
 		PROPERTY_REPLACER.initProperties();
 		DATE_FORMAT.setTimeZone(utc);
 		TIME_FORMAT.setTimeZone(utc);
 		TIMESTAMP_FORMAT.setTimeZone(utc);
 	}
 
 	/**
 	 * Creates a repository location without the trailing slash that will be added if the standard {@link java.io.File#toURI()} is used.
 	 * 
 	 * @param repoLocation
 	 *            The location. Must be an absolute path.
 	 * @return The created URI.
 	 * @throws CoreException
 	 *             if the argument is not an absolute path
 	 */
 	public static final URI createURI(File repoLocation) throws CoreException {
 		if(repoLocation != null) {
 			IPath path = Path.fromOSString(repoLocation.getPath());
 			if(path.isAbsolute())
 				try {
 					String pathStr = path.removeTrailingSeparator().toPortableString();
 					if(!pathStr.startsWith("/"))
 						// Path starts with a drive letter
 						pathStr = "/" + pathStr; //$NON-NLS-1$
 					return new URI("file", null, pathStr, null); //$NON-NLS-1$
 				}
 				catch(URISyntaxException e) {
 					throw ExceptionUtils.wrap(e);
 				}
 		}
 		throw ExceptionUtils.fromMessage("File %s is not an absolute path", repoLocation);
 	}
 
 	private static boolean deleteAndCheck(File folder, String fileName) throws CoreException {
 		File file = new File(folder, fileName);
 		try {
 			return file.delete();
 		}
 		finally {
 			if(file.exists())
 				throw ExceptionUtils.fromMessage("Unable to delete file %s\n", file.getAbsolutePath());
 		}
 	}
 
 	private static boolean deleteMetadataRepository(IMetadataRepositoryManager mdrMgr, File repoFolder)
 			throws CoreException {
 		URI repoURI = Builder.createURI(repoFolder);
 		boolean folderWasRepo = false;
 
 		if(mdrMgr.removeRepository(repoURI))
 			folderWasRepo = true;
 		if(deleteAndCheck(repoFolder, "compositeContent.jar") && !folderWasRepo)
 			folderWasRepo = true;
 		if(deleteAndCheck(repoFolder, "compositeContent.xml") && !folderWasRepo)
 			folderWasRepo = true;
 		if(deleteAndCheck(repoFolder, "content.jar") && !folderWasRepo)
 			folderWasRepo = true;
 		if(deleteAndCheck(repoFolder, "content.xml") && !folderWasRepo)
 			folderWasRepo = true;
 
 		return folderWasRepo;
 	}
 
 	private static void deleteTwoLevelMetadataRepository(IMetadataRepositoryManager mdrMgr, File repoFolder)
 			throws CoreException {
 		deleteMetadataRepository(mdrMgr, repoFolder);
 
 		File[] secondLevelRepositories = repoFolder.listFiles();
 		if(secondLevelRepositories != null) {
 			for(File repoSubFolder : repoFolder.listFiles()) {
 				if(repoSubFolder.isDirectory()) {
 					if(deleteMetadataRepository(mdrMgr, repoSubFolder))
 						// try to delete the directory if it seems it used to be a metadata repository
 						repoSubFolder.delete();
 				}
 			}
 		}
 	}
 
 	public static String getExceptionMessages(Throwable e) {
 		StringBuilder bld = new StringBuilder();
 		getExceptionMessages(e, bld);
 		return bld.toString();
 	}
 
 	private static void getExceptionMessages(Throwable e, StringBuilder bld) {
 		bld.append(e.getClass().getName());
 		bld.append(": ");
 		if(e.getMessage() != null)
 			bld.append(e.getMessage());
 
 		if(e instanceof CoreException)
 			e = ((CoreException) e).getStatus().getException();
 		else {
 			Throwable t = e.getCause();
 			e = (t == e)
 					? null
 					: t;
 		}
 		if(e != null) {
 			bld.append("\nCaused by: ");
 			getExceptionMessages(e, bld);
 		}
 	}
 
 	public static IInstallableUnit getIU(IMetadataRepository mdr, String id, String version) {
 		version = StringUtils.trimmedOrNull(version);
 		IQuery<IInstallableUnit> query = version == null
 				? QueryUtil.createIUQuery(id)
 				: QueryUtil.createIUQuery(id, Version.create(version));
 		IQueryResult<IInstallableUnit> result = mdr.query(query, null);
 		return !result.isEmpty()
 				? result.iterator().next()
 				: null;
 	}
 
 	// === OPTIONS ===
 
 	public static String getValidationSetLabel(ValidationSet validationSet) {
 		return validationSet == null
 				? "<main>"
 				: validationSet.getLabel();
 	}
 
 	// @Option(name = "--buildModel", required = true, usage = "Appoints the aggregation definition that drives the execution")
 	// private File buildModelLocation;
 
 	private static void send(String host, int port, EmailAddress from, List<EmailAddress> toList, EmailAddress cc,
 			String subject, String message) throws IOException {
 		MailMessage mailMessage = new MailMessage(host, port);
 		mailMessage.from(from.toString());
 		for(EmailAddress to : toList)
 			mailMessage.to(to.toString());
 		if(cc != null)
 			mailMessage.cc(cc.toString());
 		mailMessage.setSubject(subject);
 		mailMessage.setHeader("Date", DateUtils.getDateForHeader());
 		mailMessage.setHeader("Content-Type", "text/plain; charset=us-ascii");
 		PrintStream out = mailMessage.getPrintStream();
 		out.print(message);
 		mailMessage.sendAndClose();
 	}
 
 	@Option(name = "--action", usage = "Specifies the type of the execution. Default is BUILD.")
 	private ActionType action = ActionType.BUILD;
 
 	@Option(name = "--buildId", usage = "Assigns a build identifier to the aggregation. "
 			+ "The identifier is used to identify the build in notification emails. Defaults to: "
 			+ "build-<timestamp> where <timestamp> is formatted according as yyyyMMddHHmm, i.e. build-200911031527", metaVar = "<ID>")
 	private String buildID;
 
 	@Option(name = "--buildRoot", usage = "Controls the output. Defaults to the build root defined in the"
 			+ " aggregation definition.")
 	private File buildRoot;
 
 	@Option(name = "--logURL", usage = "The URL that will be pasted into the emails. "
 			+ "Should normally point to the a public URL for output log for the aggregator so "
 			+ "that the receiver can browse the log for details on failures.", metaVar = "<url>")
 	private String logURL;
 
 	@Option(name = "--production", usage = "Indicates that the build is running in real production. "
 			+ "That means that no mock emails will be sent. Instead, the contacts listed for each contribution will get emails when things go wrong.")
 	private boolean production = false;
 
 	@Option(name = "--mockEmailCc", usage = "Becomes the CC receiver of the mock-emails sent from the aggregator", metaVar = "<address>")
 	private String mockEmailCC;
 
 	@Option(name = "--emailFrom", usage = "Becomes the sender of the emails sent from the aggregator. "
 			+ "Defaults to the build master defined in the aggregator definition.", metaVar = "<address>")
 	private String emailFrom;
 
 	@Option(name = "--emailFromName", usage = "Mock sender's name", metaVar = "<name>")
 	private String emailFromName;
 
 	@Option(name = "--mockEmailTo", usage = "Becomes the receiver of the mock-emails sent from the aggregator", metaVar = "<address>")
 	private String mockEmailTo;
 
 	@Option(name = "--smtpHost", usage = "The SMTP host to talk to when sending emails. Defaults to \"localhost\".", metaVar = "<host>")
 	private String smtpHost;
 
 	// Deprecated options
 
 	@Option(name = "--smtpPort", usage = "The SMTP port number to use when talking to the SMTP host. Default is 25.", metaVar = "<port>")
 	private int smtpPort;
 
 	@Option(name = "--subjectPrefix", usage = "The prefix to use for the subject when sending emails. "
 			+ "Defaults to the label defined in the aggregation definition. "
 			+ "The subject is formatted as: \"[<subjectPrefix>] Failed for build <buildId>\"", metaVar = "<subject>")
 	private String subjectPrefix;
 
 	@Option(name = "--packedStrategy", usage = "(Deprecated) Controls how mirroring is done of packed artifacts found in the source repository."
 			+ "Defaults to the setting in the aggregation definition.")
 	private PackedStrategy packedStrategy;
 
 	@Option(name = "--trustedContributions", usage = "(Deprecated) A comma separated list of contributions with repositories that will be referenced directly "
 			+ "(through a composite repository) rather than mirrored into the final repository "
 			+ "(even if the repository is set to mirror artifacts by default)", metaVar = "<contributions>")
 	private String trustedContributions;
 
 	@Option(name = "--validationContributions", usage = "(Deprecated) A comma separated list of contributions with repositories that will be used for aggregation validation only rather than mirrored or referenced into the final repository.", metaVar = "<contributions>")
 	private String validationContributions;;
 
 	@Option(name = "--mavenResult", usage = "(Deprecated) Tells the aggregator to generate a hybrid repository that is compatible with p2 and maven2.")
 	private Boolean mavenResult;
 
 	@Option(name = "--mirrorReferences", usage = "(Deprecated) Mirror all meta-data repository references from the contributed repositories.")
 	private boolean mirrorReferences = false;
 
 	// End of deprecated options
 
 	@Option(name = "--referenceIncludePattern", usage = "(Deprecated) Include only those references that matches the given regular expression pattern.", metaVar = "<regexp>", handler = PatternOptionHandler.class)
 	private Pattern referenceIncludePattern;
 
 	// === END OF OPTIONS ===
 
 	@Option(name = "--referenceExcludePattern", usage = "(Deprecated) Exclude all references that matches the given regular expression pattern. An exclusion takes precedence over an inclusion in case both patterns match a reference.", metaVar = "<regexp>", handler = PatternOptionHandler.class)
 	private Pattern referenceExcludePattern;
 
 	@Argument
 	private List<String> unparsed = new ArrayList<String>();
 
 	private Aggregation aggregation;
 
 	private String buildLabel;
 
 	private String buildMasterEmail;
 
 	private String buildMasterName;
 
 	private IMetadataRepository aggregationMdr;
 
 	private List<IInstallableUnit> validationIUs;
 
 	private boolean sendmail = false;
 
 	private Set<MappedRepository> exclusions;
 
 	private IProvisioningAgent provisioningAgent;
 
 	private boolean fromIDE;
 
 	private Map<ValidationSet, ValidationSetAnnotations> validationSetAnnotationsMap = new HashMap<ValidationSet, ValidationSetAnnotations>();
 
 	private Map<MappedRepository, String> safeRepositoryNameMap = new HashMap<MappedRepository, String>();
 
 	private Set<String> safeRepositoryNames = new HashSet<String>();
 
 	private Map<MetadataRepositoryReference, IArtifactRepository> arCache;
 
 	private IArtifactRepositoryManager arManager;
 
 	private IMetadataRepositoryManager mdrManager;
 
 	private IFileArtifactRepository aggregationAr;
 
 	private MavenRepositoryHelper mavenHelper;
 
 	private Set<IArtifactKey> keysToExcludeFromMirroring;
 
 	/**
 	 * Mark the specified repository as not eligible for verbatim mapping.
 	 * 
 	 * @param repository
 	 *            The repository for which to exclude a mapping
 	 * @param rc
 	 *            The required capability to be excluded/replaced
 	 */
 	public void addMappingExclusion(MappedRepository repository) {
 		if(exclusions == null) {
 			exclusions = new HashSet<MappedRepository>();
 		}
 		exclusions.add(repository);
 	}
 
 	private void cleanAll() throws CoreException {
		cleanMetadata();
 		if(buildRoot.exists()) {
 			FileUtils.deleteAll(buildRoot);
 			if(buildRoot.exists())
 				throw ExceptionUtils.fromMessage("Failed to delete folder %s", buildRoot.getAbsolutePath());
 		}
 	}
 
 	private void cleanMemoryCaches() {
 		validationSetAnnotationsMap.clear();
 		safeRepositoryNameMap.clear();
 		safeRepositoryNames.clear();
 	}
 
 	private void cleanMetadata() throws CoreException {
 		IMetadataRepositoryManager mdrMgr = getMdrManager();
 		File finalRepo = new File(buildRoot, Builder.REPO_FOLDER_FINAL);
 		deleteMetadataRepository(mdrMgr, finalRepo);
 		deleteTwoLevelMetadataRepository(mdrMgr, new File(finalRepo, Builder.REPO_FOLDER_AGGREGATE));
 		File interimRepo = new File(buildRoot, Builder.REPO_FOLDER_INTERIM);
 		deleteMetadataRepository(mdrMgr, interimRepo);
 		deleteTwoLevelMetadataRepository(mdrMgr, new File(interimRepo, Builder.REPO_FOLDER_VERIFICATION));
 	}
 
 	private void finishMirroring(IProgressMonitor monitor) throws CoreException {
 		if(mavenHelper != null)
 			mavenAddMetadata();
 
 		IArtifactRepositoryManager arMgr = getArManager();
 		IMetadataRepositoryManager mdrMgr = getMdrManager();
 		SubMonitor subMon = SubMonitor.convert(monitor, 100);
 		try {
 			List<MappedRepository> reposWithReferencedArtifacts = new ArrayList<MappedRepository>();
 			List<MappedRepository> reposWithReferencedMetadata = new ArrayList<MappedRepository>();
 
 			for(Contribution contrib : aggregation.getAllContributions(true)) {
 				for(MappedRepository repo : contrib.getRepositories(true)) {
 					if(isMapVerbatim(repo)) {
 						reposWithReferencedArtifacts.add(repo);
 						reposWithReferencedMetadata.add(repo);
 					}
 					else if(!repo.isMirrorArtifacts() && "p2".equals(repo.getNature()))
 						reposWithReferencedArtifacts.add(repo);
 				}
 			}
 			File destination = new File(getBuildRoot(), Builder.REPO_FOLDER_FINAL);
 			URI finalURI = Builder.createURI(destination);
 
 			File aggregateDestination = new File(destination, Builder.REPO_FOLDER_AGGREGATE);
 			URI aggregateURI = Builder.createURI(aggregateDestination);
 
 			// Initialize a p2Index property file. It might not be used.
 			Properties p2Index = new Properties();
 			if(reposWithReferencedMetadata.isEmpty()) {
 				// The aggregated meta-data can serve as the final repository so
 				// let's move it.
 				//
 				LogUtils.info("Making the aggregated metadata repository final at %s", finalURI);
 				File oldLocation = new File(aggregateDestination, "content.jar");
 				File newLocation = new File(destination, oldLocation.getName());
 				if(!oldLocation.renameTo(newLocation))
 					throw ExceptionUtils.fromMessage(
 						"Unable to move %s to %s", oldLocation.getAbsolutePath(), newLocation.getAbsolutePath());
 				mdrMgr.removeRepository(aggregateURI);
 				p2Index.setProperty("metadata.repository.factory.order", "content.xml,!");
 			}
 			else {
 				// Set up the final composite repositories
 				LogUtils.info("Building final metadata composite at %s", finalURI);
 
 				mdrMgr.removeRepository(finalURI);
 				Map<String, String> properties = new HashMap<String, String>();
 				properties.put(IRepository.PROP_COMPRESSED, Boolean.toString(true));
 				CompositeMetadataRepository compositeMdr = (CompositeMetadataRepository) mdrMgr.createRepository(
 					finalURI, getAggregation().getLabel(), Builder.COMPOSITE_METADATA_TYPE, properties);
 
 				compositeMdr.addChild(URI.create(Builder.REPO_FOLDER_AGGREGATE));
 				for(MappedRepository referenced : reposWithReferencedMetadata)
 					compositeMdr.addChild(referenced.getMetadataRepository().getLocation());
 
 				p2Index.setProperty("metadata.repository.factory.order", "compositeContent.xml,!");
 				LogUtils.info("Done building final metadata composite");
 			}
 			subMon.worked(10);
 
 			IFileArtifactRepository artifacts = getAggregationArtifactRepository(subMon.newChild(2));
 			IQueryResult<IArtifactKey> artifactKeys = artifacts.query(
 				QueryUtil.createMatchQuery(IArtifactKey.class, ExpressionUtil.TRUE_EXPRESSION), monitor);
 			boolean hasMirroredArtifacts = !artifactKeys.isEmpty();
 
 			if(!hasMirroredArtifacts) {
 				// Delete all stuff related to this artifact repository in the aggregate directory
 				for(String name : aggregateDestination.list()) {
 					if(!"content.jar".equals(name))
 						FileUtils.deleteAll(new File(aggregateDestination, name));
 				}
 			}
 
 			// Forget the old URI
 			arMgr.removeRepository(aggregateURI);
 
 			if(hasMirroredArtifacts && reposWithReferencedArtifacts.isEmpty()) {
 				// The aggregation can serve as the final repository so move everything
 				// up to the final directory.
 				LogUtils.info("Making the aggregated artifact repository final at %s", finalURI);
 				for(String name : aggregateDestination.list()) {
 					if("content.jar".equals(name))
 						continue;
 
 					File oldLocation = new File(aggregateDestination, name);
 					if(hasMirroredArtifacts) {
 						File newLocation = new File(destination, name);
 						if(!oldLocation.renameTo(newLocation))
 							throw ExceptionUtils.fromMessage(
 								"Unable to move %s to %s", oldLocation.getAbsolutePath(), newLocation.getAbsolutePath());
 					}
 					else {
 						FileUtils.deleteAll(oldLocation);
 					}
 				}
 				p2Index.setProperty("artifact.repository.factory.order", "artifacts.xml,!");
 			}
 			else {
 				// Set up the final composite repositories
 				LogUtils.info("Building final artifact composite at %s", finalURI);
 				arMgr.removeRepository(finalURI);
 
 				Map<String, String> properties = new HashMap<String, String>();
 				properties.put(IRepository.PROP_COMPRESSED, Boolean.toString(true));
 				CompositeArtifactRepository compositeAr = (CompositeArtifactRepository) arMgr.createRepository(
 					finalURI, getAggregation().getLabel() + " artifacts", Builder.COMPOSITE_ARTIFACTS_TYPE, properties); //$NON-NLS-1$
 
 				for(MappedRepository referenced : reposWithReferencedArtifacts)
 					compositeAr.addChild(referenced.getMetadataRepository().getLocation());
 
 				if(hasMirroredArtifacts)
 					compositeAr.addChild(finalURI.relativize(aggregateURI));
 				else {
 					// Delete the empty artifact repository
 				}
 
 				p2Index.setProperty("artifact.repository.factory.order", "compositeArtifacts.xml,!");
 				LogUtils.info("Done building final artifact composite");
 			}
 			if(!p2Index.isEmpty()) {
 				p2Index.setProperty("version", "1");
 				File p2IndexFile = new File(destination, "p2.index");
 				OutputStream out = null;
 				try {
 					out = new BufferedOutputStream(new FileOutputStream(p2IndexFile));
 					p2Index.store(
 						out,
 						String.format(" p2.index file to speed things up.%n Please note that the values in this file denotes repository factories and%n not files. The factory '<name>.xml' will look for both the <name>.jar%n and the <name>.xml file, in that order"));
 				}
 				catch(IOException e) {
 					LogUtils.error("Unable to create p2.index file", e);
 				}
 				finally {
 					IOUtils.close(out);
 				}
 			}
 
 			// Remove the aggregation in case it's now empty.
 			//
 			String[] content = aggregateDestination.list();
 			if(content != null && content.length == 0)
 				if(!aggregateDestination.delete())
 					throw ExceptionUtils.fromMessage("Unable to remove %s", aggregateDestination.getAbsolutePath());
 		}
 		finally {
 			MonitorUtils.done(monitor);
 		}
 	}
 
 	public Aggregation getAggregation() {
 		return aggregation;
 	}
 
 	public IFileArtifactRepository getAggregationArtifactRepository(IProgressMonitor monitor) throws CoreException {
 		if(aggregationAr != null) {
 			MonitorUtils.complete(monitor);
 			return aggregationAr;
 		}
 
 		IArtifactRepositoryManager arMgr = getArManager();
 		File destination = new File(getBuildRoot(), Builder.REPO_FOLDER_FINAL);
 		File aggregateDestination = new File(destination, Builder.REPO_FOLDER_AGGREGATE);
 		URI aggregateURI = Builder.createURI(aggregateDestination);
 		Map<String, String> properties = new HashMap<String, String>();
 		properties.put(IRepository.PROP_COMPRESSED, Boolean.toString(true));
 		properties.put(Publisher.PUBLISH_PACK_FILES_AS_SIBLINGS, Boolean.toString(true));
 		try {
 			aggregationAr = (IFileArtifactRepository) arMgr.loadRepository(aggregateURI, monitor); //$NON-NLS-1$
 		}
 		catch(ProvisionException e) {
 			aggregationAr = (IFileArtifactRepository) arMgr.createRepository(aggregateURI, getAggregation().getLabel() +
 					" artifacts", Builder.SIMPLE_ARTIFACTS_TYPE, properties); //$NON-NLS-1$
 			MonitorUtils.complete(monitor);
 		}
 		return aggregationAr;
 	}
 
 	public IMetadataRepository getAggregationMetadataRepository() throws CoreException {
 		if(aggregationMdr != null)
 			return aggregationMdr;
 
 		IMetadataRepositoryManager mdrMgr = getMdrManager();
 		File destination = new File(getBuildRoot(), Builder.REPO_FOLDER_FINAL);
 		File aggregateDestination = new File(destination, Builder.REPO_FOLDER_AGGREGATE);
 		URI aggregateURI = Builder.createURI(aggregateDestination);
 
 		Map<String, String> properties = new HashMap<String, String>();
 		properties.put(IRepository.PROP_COMPRESSED, Boolean.toString(true));
 		properties.put(Publisher.PUBLISH_PACK_FILES_AS_SIBLINGS, Boolean.toString(true));
 		aggregationMdr = mdrMgr.createRepository(
 			aggregateURI, getAggregation().getLabel(), Builder.SIMPLE_METADATA_TYPE, properties); //$NON-NLS-1$
 		return aggregationMdr;
 	}
 
 	public IArtifactRepositoryManager getArManager() {
 		return arManager;
 	}
 
 	/**
 	 * Returns a set of IArtifactKey instances that are found in repositories that will
 	 * be referenced rather than mirrored.
 	 * 
 	 * @return All IArtifactKeys that should not be mirrored.
 	 * @throws CoreException
 	 */
 	public Set<IArtifactKey> getArtifactKeysToExcludeFromMirroring() throws CoreException {
 		if(keysToExcludeFromMirroring != null)
 			return keysToExcludeFromMirroring;
 
 		keysToExcludeFromMirroring = new HashSet<IArtifactKey>();
 		for(Contribution contrib : getAggregation().getAllContributions(true)) {
 			for(MappedRepository repo : contrib.getRepositories(true)) {
 				if(repo.isMirrorArtifacts())
 					continue;
 
 				for(IInstallableUnit iu : ResourceUtils.getMetadataRepository(repo).getInstallableUnits())
 					keysToExcludeFromMirroring.addAll(iu.getArtifacts());
 			}
 		}
 		return keysToExcludeFromMirroring;
 	}
 
 	public IArtifactRepository getArtifactRepository(MetadataRepositoryReference repo, IProgressMonitor monitor)
 			throws CoreException {
 		if(arCache == null)
 			arCache = new HashMap<MetadataRepositoryReference, IArtifactRepository>();
 
 		IArtifactRepository ar = arCache.get(repo);
 		if(ar == null) {
 			IConfigurationElement config = RepositoryLoaderUtils.getLoaderFor(repo.getNature());
 			if(config == null)
 				throw ExceptionUtils.fromMessage("No loader for %s", repo.getNature());
 			IRepositoryLoader repoLoader = (IRepositoryLoader) config.createExecutableExtension("class");
 			arCache.put(repo, ar = repoLoader.getArtifactRepository(ResourceUtils.getMetadataRepository(repo), monitor));
 		}
 		return ar;
 	}
 
 	public String getBuildID() {
 		return buildID;
 	}
 
 	public File getBuildModelLocation() {
 		return buildModelLocation;
 	}
 
 	public File getBuildRoot() {
 		return buildRoot;
 	}
 
 	public Collection<String> getChildrenSubdirectories() {
 		ArrayList<String> childrenSubdirectories = new ArrayList<String>(validationSetAnnotationsMap.size());
 
 		for(ValidationSet validationSet : validationSetAnnotationsMap.keySet()) {
 			childrenSubdirectories.add(getValidationSetSubdirectory(validationSet));
 		}
 
 		return childrenSubdirectories;
 	}
 
 	public String getMappedRepositoryVerificationIUName(MappedRepository repository) {
 		return "mappedRepo_" + getSafeRepositoryName(repository);
 	}
 
 	public IMetadataRepositoryManager getMdrManager() {
 		return mdrManager;
 	}
 
 	/**
 	 * @return the provisioningAgent
 	 */
 	public IProvisioningAgent getProvisioningAgent() {
 		return provisioningAgent;
 	}
 
 	public String getSafeRepositoryName(MappedRepository repository) {
 		String safeName = safeRepositoryNameMap.get(repository);
 
 		if(safeName != null)
 			return safeName;
 
 		int i;
 		String location;
 		{
 			org.eclipse.emf.common.util.URI repoURI = org.eclipse.emf.common.util.URI.createURI(repository.getResolvedLocation());
 			String scheme = repoURI.scheme();
 
 			location = repoURI.toString();
 
 			// discard scheme, if present
 			i = (scheme != null)
 					? scheme.length() + 1
 					: 0;
 
 			// discard leading slashes
 			while(location.charAt(i) == '/')
 				++i;
 
 			if(i > 0)
 				location = location.substring(i);
 
 			// make a safe name out of the rest of the URI
 			location = location.replaceAll("[^-0-9a-zA-Z_.~]", "_");
 		}
 
 		i = 0;
 		safeName = location;
 		while(safeRepositoryNames.contains(safeName)) {
 			if(++i < 0) // if overflow
 				throw new IllegalArgumentException("Could not generate safe unique name for mapped repository: " +
 						repository.getLocation());
 
 			safeName = location + '_' + i;
 		}
 
 		safeRepositoryNames.add(safeName);
 		safeRepositoryNameMap.put(repository, safeName);
 
 		return safeName;
 	}
 
 	public String getSafeValidationSetName(ValidationSet validationSet) {
 		return getSafeValidationSetName(validationSet, getValidationSetAnnotations(validationSet));
 	}
 
 	private String getSafeValidationSetName(ValidationSet validationSet, ValidationSetAnnotations vsas) {
 		String safeName = vsas.getSafeName();
 		if(safeName != null)
 			return safeName;
 
 		safeName = validationSet.getLabel().replaceAll("[^-0-9a-zA-Z_.~]", "_");
 
 		// Ensure that the name is unique
 		retry: for(int retryCount = 0;; ++retryCount) {
 			for(ValidationSetAnnotations other : validationSetAnnotationsMap.values()) {
 				if(other == vsas)
 					continue;
 
 				if(safeName.equals(other.getSafeName())) {
 					StringBuilder bld = new StringBuilder(safeName);
 					if(retryCount > 0)
 						bld.setLength(safeName.lastIndexOf('_'));
 					bld.append('_');
 					bld.append(retryCount + 1);
 					safeName = bld.toString();
 					continue retry;
 				}
 			}
 			break;
 		}
 		vsas.setSafeName(safeName);
 		return safeName;
 	}
 
 	@Override
 	public String getShortDescription() {
 		return "Aggregates source repositories into a resulting repository using aggregator definition";
 	}
 
 	public CompositeMetadataRepository getSourceComposite(ValidationSet validationSet) {
 		return getValidationSetAnnotations(validationSet).getSourceComposite();
 	}
 
 	public URI getSourceCompositeURI(ValidationSet validationSet) throws CoreException {
 		ValidationSetAnnotations vsas = getValidationSetAnnotations(validationSet);
 		URI compositeURI = vsas.getSourceCompositeURI();
 		if(compositeURI == null) {
 			compositeURI = createURI(new File(new File(buildRoot, REPO_FOLDER_INTERIM), getSafeValidationSetName(
 				validationSet, vsas)));
 			vsas.setSourceCompositeURI(compositeURI);
 		}
 		return compositeURI;
 	}
 
 	public URI getTargetCompositeURI() throws CoreException {
 		if(aggregation.getValidationSets().isEmpty())
 			return null;
 		return createURI(new File(buildRoot, REPO_FOLDER_FINAL));
 	}
 
 	/**
 	 * Return the temporary artifact repository used when resolving legacy sites or <code>null</code> if that
 	 * hasn't been necessary.
 	 * 
 	 * @param monitor
 	 * @return
 	 * @throws CoreException
 	 */
 	public IArtifactRepository getTemporaryArtifactRepository(IProgressMonitor monitor) throws CoreException {
 		try {
 			return getArManager().loadRepository(createURI(getTempRepositoryFolder()), monitor);
 		}
 		catch(ProvisionException e) {
 			return null;
 		}
 	}
 
 	public File getTempRepositoryFolder() {
 		return new File(buildRoot, REPO_FOLDER_TEMP);
 	}
 
 	public Transport getTransport() {
 		return (Transport) provisioningAgent.getService(Transport.SERVICE_NAME);
 	}
 
 	public Set<IInstallableUnit> getUnitsToAggregate(ValidationSet validationSet) {
 		ValidationSetAnnotations vsas = getValidationSetAnnotations(validationSet);
 		Set<IInstallableUnit> units = vsas.getUnitsToAggregate();
 		if(units == null) {
 			units = new HashSet<IInstallableUnit>();
 			vsas.setUnitsToAggregate(units);
 		}
 		return units;
 	}
 
 	public List<IInstallableUnit> getValidationIUs() {
 		return validationIUs;
 	}
 
 	private ValidationSetAnnotations getValidationSetAnnotations(ValidationSet validationSet) {
 		ValidationSetAnnotations vsas = validationSetAnnotationsMap.get(validationSet);
 		if(vsas == null) {
 			vsas = new ValidationSetAnnotations();
 			validationSetAnnotationsMap.put(validationSet, vsas);
 		}
 		return vsas;
 	}
 
 	public String getValidationSetSubdirectory(ValidationSet validationSet) {
 		return "/validationSet_" + getSafeValidationSetName(validationSet);
 	}
 
 	public String getVerificationIUName(ValidationSet validationSet) {
 		return "validationSet_" + getSafeValidationSetName(validationSet);
 	}
 
 	private void initializeArtifactMirroring(IProgressMonitor monitor) throws CoreException {
 		arCache = null;
 		SubMonitor subMon = SubMonitor.convert(monitor, 1000);
 		try {
 			subMon.setTaskName("Mirroring artifacts...");
 			aggregationAr = getAggregationArtifactRepository(subMon.newChild(5));
 
 			if(getAggregation().isMavenResult())
 				mavenInitializeMirroring(aggregationAr, subMon.newChild(10));
 
 			// Clear final destination
 			File destination = new File(getBuildRoot(), Builder.REPO_FOLDER_FINAL);
 			for(String fileName : new String[] {
 					"p2.index", "compositeContent.jar", "content.jar", "compositeArtifacts.jar", "artifacts.jar" }) {
 				File file = new File(destination, fileName);
 				if(file.exists() && !file.delete())
 					throw ExceptionUtils.fromMessage("Unable to remove %s", file.getAbsolutePath());
 			}
 		}
 		finally {
 			MonitorUtils.done(monitor);
 		}
 	}
 
 	private void initMirroring(IProgressMonitor monitor) throws CoreException {
 		File destination = new File(getBuildRoot(), Builder.REPO_FOLDER_FINAL);
 		File aggregateDestination = new File(destination, Builder.REPO_FOLDER_AGGREGATE);
 		if(action == ActionType.CLEAN_BUILD) {
 			FileUtils.deleteAll(destination);
 			aggregateDestination.mkdirs();
 			refreshBuildRoot(monitor);
 			return;
 		}
 
 		// Forget these meta-data repositories
 		IMetadataRepositoryManager mdrMgr = getMdrManager();
 		mdrMgr.removeRepository(destination.toURI());
 		mdrMgr.removeRepository(aggregateDestination.toURI());
 
 		// Preserve artifacts
 		FileUtils.deleteAll(aggregateDestination);
 		aggregateDestination.mkdirs();
 		for(String name : destination.list()) {
 			if(name.equals(Builder.REPO_FOLDER_AGGREGATE))
 				continue;
 
 			File oldLocation = new File(destination, name);
 			if(name.equals("compositeArtifacts.xml") || name.equals("compositeContent.xml") ||
 					name.equals("content.xml") || name.equals("content.jar") || name.equals("p2.index")) {
 				oldLocation.delete();
 				continue;
 			}
 
 			File newLocation = new File(aggregateDestination, name);
 			if(!oldLocation.renameTo(newLocation))
 				throw ExceptionUtils.fromMessage(
 					"Unable to move %s to %s", oldLocation.getAbsolutePath(), newLocation.getAbsolutePath());
 		}
 		refreshBuildRoot(monitor);
 	}
 
 	/**
 	 * Checks if the repository can be included verbatim. If it can, the builder will include a reference to it in a
 	 * composite repository instead of copying everything into the aggregate.
 	 * 
 	 * @param repo
 	 *            The repository to check
 	 * @return true if the repository is mapped verbatim.
 	 */
 	public boolean isMapVerbatim(MappedRepository repo) {
 		return !(repo.isMapExclusive() || repo.isMirrorArtifacts()) &&
 				StringUtils.trimmedOrNull(repo.getCategoryPrefix()) == null &&
 				!(exclusions != null && exclusions.contains(repo) && "p2".equals(repo.getNature()));
 	}
 
 	public boolean isMatchedReference(String reference) {
 		reference = StringUtils.trimmedOrNull(reference);
 		if(reference == null)
 			return false;
 
 		if(referenceIncludePattern != null) {
 			Matcher m = referenceIncludePattern.matcher(reference);
 			if(!m.matches())
 				return false;
 		}
 
 		if(referenceExcludePattern != null) {
 			Matcher m = referenceExcludePattern.matcher(reference);
 			if(m.matches())
 				return false;
 		}
 		return true;
 	}
 
 	public boolean isMirrorReferences() {
 		return mirrorReferences;
 	}
 
 	public boolean isProduction() {
 		return production;
 	}
 
 	public boolean isTopLevelCategory(IInstallableUnit iu) {
 		return iu != null && "true".equalsIgnoreCase(iu.getProperty(InstallableUnitDescription.PROP_TYPE_CATEGORY)) &&
 				!"true".equalsIgnoreCase(iu.getProperty(InstallableUnitDescription.PROP_TYPE_GROUP));
 	}
 
 	public boolean isVerifyOnly() {
 		return action == ActionType.VALIDATE;
 	}
 
 	private void loadAllMappedRepositories() throws CoreException {
 		LogUtils.info("Loading all repositories");
 
 		Set<MetadataRepositoryReference> repositoriesToLoad = new HashSet<MetadataRepositoryReference>();
 
 		Aggregation aggregation = getAggregation();
 		ResourceSet topSet = ((EObject) aggregation).eResource().getResourceSet();
 		// first, set up asynchronous loading jobs so that the repos are loaded in parallel
 		for(MetadataRepositoryReference repo : aggregation.getAllMetadataRepositoryReferences(true)) {
 			org.eclipse.emf.common.util.URI repoURI = org.eclipse.emf.common.util.URI.createGenericURI(
 				"b3", repo.getNature() + ":" + repo.getResolvedLocation(), null);
 			P2ResourceImpl res = (P2ResourceImpl) topSet.getResource(repoURI, false);
 			if(res == null)
 				res = (P2ResourceImpl) topSet.createResource(repoURI);
 			res.startAsynchronousLoad();
 			repositoriesToLoad.add(repo);
 		}
 
 		try {
 			Map<Contribution, List<String>> errors = new HashMap<Contribution, List<String>>();
 			// and now, wait until all the jobs are done (we need all repositories anyway)
 			for(MetadataRepositoryReference repo : repositoriesToLoad) {
 				MetadataRepository mdr;
 				if((mdr = repo.getMetadataRepository()) == null || ((EObject) mdr).eIsProxy()) {
 					Contribution contrib = null;
 					if(repo instanceof MappedRepository)
 						contrib = (Contribution) ((EObject) repo).eContainer();
 					String msg = String.format("Unable to load repository %s:%s", repo.getNature(), repo.getLocation());
 					LogUtils.error(msg);
 
 					if(fromIDE)
 						throw ExceptionUtils.fromMessage(msg);
 
 					List<String> contribErrors = errors.get(contrib);
 					if(contribErrors == null)
 						errors.put(contrib, contribErrors = new ArrayList<String>());
 					contribErrors.add(msg);
 				}
 			}
 
 			if(errors.size() > 0) {
 				for(Map.Entry<Contribution, List<String>> entry : errors.entrySet())
 					sendEmail(entry.getKey(), entry.getValue());
 				throw ExceptionUtils.fromMessage("Not all repositories could be loaded (see log for details)");
 			}
 		}
 		catch(CoreException e) {
 			for(MetadataRepositoryReference repo : repositoriesToLoad) {
 				repo.cancelRepositoryLoad();
 			}
 			throw e;
 		}
 	}
 
 	/**
 	 * Loads the model into memory
 	 * 
 	 * @throws CoreException
 	 *             If something goes wrong with during the process
 	 */
 	private void loadModel() throws CoreException {
 		try {
 			aggregation = loadModelFromFile();
 
 			verifyContributions();
 
 			if(packedStrategy != null)
 				aggregation.setPackedStrategy(packedStrategy);
 
 			EList<ValidationSet> validationSets = aggregation.getValidationSets(true);
 			if(trustedContributions != null) {
 				for(String contributionLabel : trustedContributions.split(",")) {
 					contributionLabel = StringUtils.trimmedOrNull(contributionLabel);
 					boolean found = false;
 
 					if(contributionLabel != null)
 						for(ValidationSet vs : validationSets) {
 							for(Contribution contribution : vs.getDeclaredContributions()) {
 								if(contributionLabel.equals(contribution.getLabel())) {
 									for(MappedRepository repository : contribution.getRepositories(true))
 										repository.setMirrorArtifacts(false);
 									found = true;
 								}
 							}
 						}
 					if(!found)
 						throw ExceptionUtils.fromMessage("Unable to trust contribution " + contributionLabel +
 								": contribution does not exist or is not enabled");
 				}
 			}
 
 			if(validationContributions != null) {
 				for(String contributionLabel : validationContributions.split(",")) {
 					contributionLabel = StringUtils.trimmedOrNull(contributionLabel);
 					boolean found = false;
 					HashSet<MappedUnit> removedMappings = new HashSet<MappedUnit>();
 
 					if(contributionLabel != null) {
 						for(ValidationSet vs : validationSets) {
 							// We need the container collection here since we're actually removing
 							// entries from it.
 							Iterator<Contribution> iterator = vs.getContributions().iterator();
 							while(iterator.hasNext()) {
 								Contribution contribution = iterator.next();
 								if(contribution.isEnabled() && contributionLabel.equals(contribution.getLabel())) {
 									for(MappedRepository repository : contribution.getRepositories(true)) {
 										MetadataRepositoryReferenceImpl validationRepo = (MetadataRepositoryReferenceImpl) AggregatorFactory.eINSTANCE.createMetadataRepositoryReference();
 										validationRepo.setNature(repository.getNature());
 										validationRepo.setLocation(repository.getLocation());
 										vs.getValidationRepositories().add(validationRepo);
 										removedMappings.addAll(repository.getFeatures());
 									}
 									iterator.remove();
 									found = true;
 								}
 							}
 						}
 					}
 
 					if(!found)
 						throw ExceptionUtils.fromMessage("Unable to use contribution " + contributionLabel +
 								" for validation only: contribution does not exist");
 
 					for(CustomCategory customCategory : aggregation.getCustomCategories()) {
 						Iterator<Feature> iterator = customCategory.getFeatures().iterator();
 						while(iterator.hasNext()) {
 							Feature feature = iterator.next();
 							if(removedMappings.add(feature))
 								iterator.remove();
 						}
 					}
 				}
 			}
 
 			HashSet<MappedUnit> allFeatures = new HashSet<MappedUnit>();
 			for(ValidationSet vs : validationSets) {
 				for(Contribution contrib : vs.getContributions()) {
 					for(MappedRepository repo : contrib.getRepositories())
 						allFeatures.addAll(repo.getFeatures());
 				}
 			}
 
 			for(CustomCategory customCategory : aggregation.getCustomCategories()) {
 				Iterator<Feature> iterator = customCategory.getFeatures().iterator();
 				while(iterator.hasNext()) {
 					Feature feature = iterator.next();
 					if(!allFeatures.contains(feature))
 						iterator.remove();
 				}
 			}
 
 			if(mavenResult != null)
 				aggregation.setMavenResult(mavenResult.booleanValue());
 
 			if(trustedContributions != null && aggregation.isMavenResult())
 				throw ExceptionUtils.fromMessage("Options --trustedContributions cannot be used if maven result is required");
 
 			sendmail = aggregation.isSendmail();
 			buildLabel = aggregation.getLabel();
 
 			Contact buildMaster = aggregation.getBuildmaster();
 			if(buildMaster != null) {
 				buildMasterName = buildMaster.getName();
 				buildMasterEmail = buildMaster.getEmail();
 			}
 
 			Diagnostic diag = Diagnostician.INSTANCE.validate((EObject) aggregation);
 			if(diag.getSeverity() == Diagnostic.ERROR) {
 				StringBuilder bld = new StringBuilder("Aggregation model is inconsistent: ").append(diag.getMessage());
 				for(Diagnostic childDiag : diag.getChildren())
 					bld.append(String.format("%n  %s", childDiag.getMessage()));
 				throw ExceptionUtils.fromMessage(bld.toString());
 			}
 
 			if(buildRoot == null) {
 				setBuildRoot(new File(PROPERTY_REPLACER.replaceProperties(aggregation.getBuildRoot())));
 
 				if(!buildRoot.isAbsolute())
 					setBuildRoot(new File(buildModelLocation.getParent(), buildRoot.getPath()).getAbsoluteFile());
 			}
 			else if(!buildRoot.isAbsolute())
 				setBuildRoot(buildRoot.getAbsoluteFile());
 		}
 		catch(Exception e) {
 			throw ExceptionUtils.wrap(e);
 		}
 	}
 
 	private boolean mavenAddMetadata() throws CoreException {
 		boolean everythingOk = true;
 		if(mavenHelper == null)
 			return everythingOk;
 
 		LogUtils.info("Adding maven metadata");
 		Map<Contribution, List<String>> errors = new HashMap<Contribution, List<String>>();
 		File destination = new File(getBuildRoot(), Builder.REPO_FOLDER_FINAL);
 
 		File aggregateDestination = new File(destination, Builder.REPO_FOLDER_AGGREGATE);
 
 		MavenManager.saveMetadata(
 			org.eclipse.emf.common.util.URI.createFileURI(aggregateDestination.getAbsolutePath()),
 			mavenHelper.getTop(), errors);
 
 		if(errors.size() > 0) {
 			everythingOk = false;
 			for(Map.Entry<Contribution, List<String>> entry : errors.entrySet())
 				sendEmail(entry.getKey(), entry.getValue());
 		}
 
 		IMaven2Indexer indexer = IndexerUtils.getIndexer("nexus");
 		if(indexer != null) {
 			LogUtils.info("Adding maven index");
 			indexer.updateLocalIndex(new File(aggregateDestination.getAbsolutePath()).toURI(), false);
 		}
 		LogUtils.info("Done adding maven metadata");
 		return everythingOk;
 	}
 
 	private void mavenApplyRules(MappedRepository repo, IInstallableUnit iu, IArtifactRepository ar,
 			List<String[]> mappingRules, List<ArtifactDescriptor> referencedArtifacts) {
 		String versionString = iu.getVersion().getOriginal();
 		if(versionString == null)
 			versionString = iu.getVersion().toString();
 		String originalPath = iu.getProperty(IRepositoryLoader.PROP_ORIGINAL_PATH);
 		String originalId = iu.getProperty(IRepositoryLoader.PROP_ORIGINAL_ID);
 		if(originalId == null)
 			originalId = iu.getId();
 
 		for(IArtifactKey key : iu.getArtifacts()) {
 			if(repo.isMirrorArtifacts()) {
 				String location = "${repoUrl}/non-p2/" + repo.getNature() + '/' + key.getClassifier() + '/' +
 						(originalPath != null
 								? (originalPath + '/')
 								: "") + originalId + '_' + versionString + '.' + key.getClassifier();
 
 				mappingRules.add(new String[] {
 						"(& (classifier=" + IUUtils.encodeFilterValue(key.getClassifier()) + ") (id=" +
 								IUUtils.encodeFilterValue(key.getId()) + ") (version=" +
 								IUUtils.encodeFilterValue(iu.getVersion().toString()) + "))", location });
 			}
 			else {
 				for(IArtifactDescriptor desc : ar.getArtifactDescriptors(key)) {
 					String ref = ((SimpleArtifactDescriptor) desc).getRepositoryProperty(SimpleArtifactDescriptor.ARTIFACT_REFERENCE);
 					SimpleArtifactDescriptor ad = new SimpleArtifactDescriptor(desc);
 					ad.setRepositoryProperty(SimpleArtifactDescriptor.ARTIFACT_REFERENCE, ref);
 					referencedArtifacts.add(ad);
 				}
 			}
 		}
 
 	}
 
 	private void mavenInitializeMirroring(IFileArtifactRepository aggregateAr, IProgressMonitor monitor)
 			throws CoreException {
 		// If maven result is required, prepare the maven metadata structure
 		List<Contribution> allContribs = getAggregation().getAllContributions(true);
 		SubMonitor childMonitor = SubMonitor.convert(monitor, allContribs.size() * 10);
 		Set<IInstallableUnit> allUnitsToAggregate = new HashSet<IInstallableUnit>();
 		for(ValidationSetAnnotations vsas : validationSetAnnotationsMap.values()) {
 			if(vsas.getUnitsToAggregate() != null)
 				allUnitsToAggregate.addAll(vsas.getUnitsToAggregate());
 		}
 
 		List<InstallableUnitMapping> iusToMaven = new ArrayList<InstallableUnitMapping>();
 		for(Contribution contrib : allContribs) {
 			SubMonitor contribMonitor = childMonitor.newChild(10);
 			List<MappedRepository> repos = contrib.getRepositories(true);
 			List<String> errors = new ArrayList<String>();
 			MonitorUtils.begin(contribMonitor, repos.size() * 100);
 			for(MappedRepository repo : repos) {
 				if(!repo.isMirrorArtifacts()) {
 					String msg = String.format(
 						"Repository %s must be set to mirror artifacts if maven result is required", repo.getLocation());
 					LogUtils.error(msg);
 					errors.add(msg);
 					MonitorUtils.worked(contribMonitor, 100);
 					continue;
 				}
 
 				MetadataRepository childMdr = ResourceUtils.getMetadataRepository(repo);
 				ArrayList<IInstallableUnit> iusToMirror = null;
 				for(IInstallableUnit iu : childMdr.getInstallableUnits()) {
 					if(!allUnitsToAggregate.contains(iu))
 						continue;
 
 					if(iusToMirror == null)
 						iusToMirror = new ArrayList<IInstallableUnit>();
 					iusToMirror.add(iu);
 				}
 
 				List<MavenMapping> allMavenMappings = contrib.getAllMavenMappings();
 				if(iusToMirror != null)
 					for(IInstallableUnit iu : iusToMirror)
 						iusToMaven.add(new InstallableUnitMapping(contrib, iu, allMavenMappings));
 
 				MonitorUtils.worked(contribMonitor, 100);
 			}
 			if(errors.size() > 0) {
 				sendEmail(contrib, errors);
 				throw ExceptionUtils.fromMessage("All repositories must be set to mirror artifacts if maven result is required");
 			}
 			MonitorUtils.done(contribMonitor);
 		}
 
 		mavenHelper = MavenManager.createMavenStructure(iusToMaven);
 
 		if(aggregateAr instanceof SimpleArtifactRepository) {
 			SimpleArtifactRepository simpleAr = ((SimpleArtifactRepository) aggregateAr);
 			simpleAr.setRules(mavenHelper.getMappingRules());
 			simpleAr.initializeAfterLoad(aggregateAr.getLocation());
 		}
 		else
 			throw ExceptionUtils.fromMessage(
 				"Unexpected repository implementation: Expected %s, found %s",
 				SimpleArtifactRepository.class.getName(), aggregateAr.getClass().getName());
 
 		if(packedStrategy != PackedStrategy.SKIP && packedStrategy != PackedStrategy.UNPACK &&
 				packedStrategy != PackedStrategy.UNPACK_AS_SIBLING) {
 			packedStrategy = PackedStrategy.UNPACK_AS_SIBLING;
 			LogUtils.info(
 				"Maven result is required, changing packed strategy from %s to %s",
 				aggregation.getPackedStrategy().getName(), packedStrategy.getName());
 		}
 
 		List<String[]> mappingRules = new ArrayList<String[]>();
 		List<ArtifactDescriptor> referencedArtifacts = new ArrayList<ArtifactDescriptor>();
 		Set<IInstallableUnit> allUnitsToValidate = new HashSet<IInstallableUnit>();
 		for(ValidationSetAnnotations vsas : validationSetAnnotationsMap.values()) {
 			Set<IInstallableUnit> unitsToValidate = vsas.getUnitsToAggregate();
 			if(unitsToValidate != null)
 				allUnitsToValidate.addAll(unitsToValidate);
 		}
 
 		// add rules for artifacts mapped from non-p2 repositories
 		for(Contribution contrib : allContribs) {
 			SubMonitor contribMonitor = childMonitor.newChild(10);
 			List<MappedRepository> repos = contrib.getRepositories(true);
 			MonitorUtils.begin(contribMonitor, repos.size() * 100);
 			for(MappedRepository repo : repos) {
 				int ticksRemaining = 100;
 
 				// Create rules only if the artifacts are mapped from a non-p2 repository
 				if("p2".equals(repo.getNature())) {
 					MonitorUtils.worked(contribMonitor, 100);
 					continue;
 				}
 
 				MetadataRepository childMdr = ResourceUtils.getMetadataRepository(repo);
 				ArrayList<IInstallableUnit> iusToRefer = null;
 				for(IInstallableUnit iu : childMdr.getInstallableUnits()) {
 					if(allUnitsToValidate.contains(iu))
 						continue;
 
 					if(iusToRefer == null)
 						iusToRefer = new ArrayList<IInstallableUnit>();
 					iusToRefer.add(iu);
 				}
 				if(iusToRefer == null)
 					continue;
 
 				int ticks = 50;
 				IArtifactRepository ar = getArtifactRepository(repo, contribMonitor.newChild(ticks));
 				ticksRemaining -= ticks;
 				for(IInstallableUnit iu : iusToRefer)
 					mavenApplyRules(repo, iu, ar, mappingRules, referencedArtifacts);
 				MonitorUtils.worked(contribMonitor, ticksRemaining);
 			}
 			MonitorUtils.done(contribMonitor);
 		}
 
 		if(mappingRules.size() > 0 || referencedArtifacts.size() > 0) {
 			if(!(aggregateAr instanceof SimpleArtifactRepository))
 				throw ExceptionUtils.fromMessage(
 					"Unexpected repository implementation: Expected %s, found %s",
 					SimpleArtifactRepository.class.getName(), aggregateAr.getClass().getName());
 
 			SimpleArtifactRepository simpleAr = ((SimpleArtifactRepository) aggregateAr);
 			List<String[]> ruleList = new ArrayList<String[]>(Arrays.asList(simpleAr.getRules()));
 			ruleList.addAll(mappingRules);
 			simpleAr.setRules(ruleList.toArray(new String[ruleList.size()][]));
 			simpleAr.initializeAfterLoad(simpleAr.getLocation());
 			simpleAr.addDescriptors(
 				referencedArtifacts.toArray(new IArtifactDescriptor[referencedArtifacts.size()]),
 				childMonitor.newChild(2));
 			simpleAr.save();
 		}
 	}
 
 	private EmailAddress mockCCRecipient() throws UnsupportedEncodingException {
 		EmailAddress mock = null;
 		if(mockEmailCC != null)
 			mock = new EmailAddress(mockEmailCC, null);
 		return mock;
 	}
 
 	private List<EmailAddress> mockRecipients() throws UnsupportedEncodingException {
 		if(mockEmailTo != null)
 			return Collections.singletonList(new EmailAddress(mockEmailTo, null));
 		return Collections.emptyList();
 	}
 
 	private void refreshBuildRoot(IProgressMonitor monitor) {
 		if(!fromIDE || buildRoot == null) {
 			MonitorUtils.complete(monitor);
 			return;
 		}
 
 		IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 		IContainer[] containers = wsRoot.findContainersForLocationURI(buildRoot.toURI());
 		SubMonitor subMon = SubMonitor.convert(monitor, containers.length * 100);
 		for(IContainer rootContainer : containers) {
 			try {
 				rootContainer.refreshLocal(IResource.DEPTH_INFINITE, subMon.newChild(100));
 			}
 			catch(CoreException e) {
 				// ignore
 			}
 		}
 		MonitorUtils.done(monitor);
 	}
 
 	public void removeUnitsToAggregate(ValidationSet validationSet) {
 		ValidationSetAnnotations vsas = getValidationSetAnnotations(validationSet);
 		if(vsas != null)
 			vsas.setUnitsToAggregate(null);
 	}
 
 	/**
 	 * Run the build
 	 * 
 	 * @param fromIDE
 	 *            set to <code>true</code> if the IDE workspace needs a refresh once the aggregation is completed
 	 * @param monitor
 	 */
 	public int run(boolean fromIDE, IProgressMonitor monitor) throws Exception {
 		this.fromIDE = fromIDE;
 		int ticks;
 		switch(action) {
 			case CLEAN:
 				ticks = 50;
 				break;
 			case VALIDATE:
 				ticks = 200;
 				break;
 			case BUILD:
 				ticks = 2150;
 				break;
 			default:
 				ticks = 2200;
 		}
 		SubMonitor subMon = SubMonitor.convert(monitor, ticks);
 		try {
 			if(buildModelLocation == null)
 				throw ExceptionUtils.fromMessage("No buildmodel has been set");
 
 			Date now = new Date();
 			if(buildID == null)
 				buildID = "build-" + DATE_FORMAT.format(now) + TIME_FORMAT.format(now);
 
 			if(smtpHost == null)
 				smtpHost = "localhost";
 
 			if(smtpPort <= 0)
 				smtpPort = 25;
 
 			loadModel();
			if(provisioningAgent == null)
				provisioningAgent = P2Utils.createDedicatedProvisioningAgent(new File(buildRoot, "p2").toURI());

			arManager = P2Utils.getRepositoryManager(provisioningAgent, IArtifactRepositoryManager.class);
			mdrManager = P2Utils.getRepositoryManager(provisioningAgent, IMetadataRepositoryManager.class);

 			switch(action) {
 				case CLEAN:
 				case CLEAN_BUILD:
 					cleanAll();
 					break;
				default:
					cleanMetadata();
 			}

 			if(action == ActionType.CLEAN)
 				return 0;
 
 			cleanMemoryCaches();
 
 			// Associate current resource set with the dedicated provisioning agent
 			((ResourceSetWithAgent) resourceSet).setProvisioningAgent(provisioningAgent);
 
 			buildRoot.mkdirs();
 			if(!buildRoot.exists())
 				throw ExceptionUtils.fromMessage("Failed to create folder %s", buildRoot);
 
 			// Remove previously used profiles.
 			//
 			IProfileRegistry profileRegistry = P2Utils.getProfileRegistry(provisioningAgent);
 			try {
 				List<String> knownIDs = new ArrayList<String>();
 				for(IProfile profile : profileRegistry.getProfiles()) {
 					String id = profile.getProfileId();
 					if(id.startsWith(PROFILE_ID))
 						knownIDs.add(id);
 				}
 
 				for(String id : knownIDs)
 					profileRegistry.removeProfile(id);
 
 				String instArea = buildRoot.toString();
 				Map<String, String> props = new HashMap<String, String>();
 				props.put(IProfile.PROP_NAME, aggregation.getLabel());
 				props.put(IProfile.PROP_DESCRIPTION, format("Default profile during %s build", aggregation.getLabel()));
 				props.put(IProfile.PROP_CACHE, instArea);
 				props.put(IProfile.PROP_INSTALL_FOLDER, instArea);
 				profileRegistry.addProfile(PROFILE_ID, props);
 			}
 			finally {
 				P2Utils.ungetProfileRegistry(provisioningAgent, profileRegistry);
 			}
 
 			loadAllMappedRepositories();
 
 			// we generate the verification IUs in a separate loop
 			// to detect non p2 related problems early
 			List<ValidationSet> validationSets = getAggregation().getValidationSets(true);
 
 			for(ValidationSet validationSet : validationSets) {
 				if(!validationSet.isAbstract()) {
 					runCompositeGenerator(validationSet, subMon.newChild(5));
 					runVerificationIUGenerator(validationSet, subMon.newChild(5));
 					runRepositoryVerifier(validationSet, subMon.newChild(100));
 				}
 			}
 
 			if(action != ActionType.VALIDATE) {
 				initMirroring(subMon.newChild(5));
 				for(ValidationSet validationSet : validationSets) {
 					if(!validationSet.isAbstract())
 						runMetadataMirroring(validationSet, subMon.newChild(100));
 				}
 				runCategoriesRepoGenerator(subMon.newChild(5));
 				initializeArtifactMirroring(subMon.newChild(10));
 				for(ValidationSet validationSet : validationSets) {
 					if(!validationSet.isAbstract())
 						runMirroring(validationSet, subMon.newChild(2000));
 				}
 				finishMirroring(monitor);
 			}
 			return 0;
 		}
 		catch(Exception e) {
 			LogUtils.error(e, "Build failed! Exception was %s", getExceptionMessages(e));
 			throw e;
 		}
 		finally {
 			if(provisioningAgent != null) {
 				if(arManager != null)
 					P2Utils.ungetRepositoryManager(provisioningAgent, arManager);
 				if(mdrManager != null)
 					P2Utils.ungetRepositoryManager(provisioningAgent, mdrManager);
 				if(!fromIDE) {
 					P2Utils.stopProvisioningAgent(provisioningAgent);
 					provisioningAgent = null;
 				}
 			}
 			refreshBuildRoot(subMon.newChild(100));
 			MonitorUtils.done(monitor);
 		}
 	}
 
 	@Override
 	protected int run(IProgressMonitor monitor) throws Exception {
 		if(unparsed.size() > 0)
 			throw new Exception("Too many arguments");
 		return run(false, monitor);
 	}
 
 	private void runCategoriesRepoGenerator(IProgressMonitor monitor) throws CoreException {
 		CategoriesGenerator generator = new CategoriesGenerator(this);
 		generator.run(monitor);
 	}
 
 	private void runCompositeGenerator(ValidationSet validationSet, IProgressMonitor monitor) throws CoreException {
 		SourceCompositeGenerator generator = new SourceCompositeGenerator(this, validationSet);
 		generator.run(monitor);
 	}
 
 	private void runMetadataMirroring(ValidationSet validationSet, IProgressMonitor monitor) throws CoreException {
 		MetadataMirrorGenerator generator = new MetadataMirrorGenerator(this, validationSet);
 		generator.run(monitor);
 	}
 
 	private void runMirroring(ValidationSet validationSet, IProgressMonitor monitor) throws CoreException {
 		MirrorGenerator generator = new MirrorGenerator(this, validationSet);
 		generator.run(monitor);
 	}
 
 	private void runRepositoryVerifier(ValidationSet validationSet, IProgressMonitor monitor) throws CoreException {
 		ValidationSetVerifier ipt = new ValidationSetVerifier(this, validationSet);
 		ipt.run(monitor);
 	}
 
 	private void runVerificationIUGenerator(ValidationSet validationSet, IProgressMonitor monitor) throws CoreException {
 		VerificationIUGenerator generator = new VerificationIUGenerator(this, validationSet);
 		generator.run(monitor);
 	}
 
 	public void sendEmail(Contribution contrib, List<String> errors) {
 		boolean useMock = (mockEmailTo != null);
 		if(!((production || useMock) && sendmail))
 			return;
 
 		try {
 			EmailAddress buildMaster = new EmailAddress(buildMasterEmail, buildMasterName);
 			EmailAddress emailFromAddr;
 			if(emailFrom != null)
 				emailFromAddr = new EmailAddress(emailFrom, emailFromName);
 			else
 				emailFromAddr = buildMaster;
 
 			List<EmailAddress> toList = new ArrayList<EmailAddress>();
 			if(contrib == null)
 				toList.add(buildMaster);
 			else
 				for(Contact contact : contrib.getContacts())
 					toList.add(new EmailAddress(contact.getEmail(), contact.getName()));
 
 			StringBuilder msgBld = new StringBuilder();
 			msgBld.append("The following error");
 			if(errors.size() > 1)
 				msgBld.append('s');
 			msgBld.append(" occured when building ");
 			msgBld.append(buildLabel);
 			msgBld.append(":\n\n");
 			for(String error : errors) {
 				msgBld.append(error);
 				msgBld.append("\n\n");
 			}
 
 			if(logURL != null) {
 				msgBld.append("Check the log file for more information: ");
 				msgBld.append(logURL);
 				msgBld.append('\n');
 			}
 
 			if(useMock) {
 				msgBld.append("\nThis is a mock mail. Real recipients would have been:\n");
 				for(EmailAddress to : toList) {
 					msgBld.append("  ");
 					msgBld.append(to);
 					msgBld.append('\n');
 				}
 			}
 			String msgContent = msgBld.toString();
 			if(subjectPrefix == null)
 				subjectPrefix = buildLabel;
 
 			String subject = format("[%s] Failed for build %s", subjectPrefix, buildID);
 
 			msgBld.setLength(0);
 			msgBld.append("Sending email to: ");
 			for(EmailAddress to : toList) {
 				msgBld.append(to);
 				msgBld.append(',');
 			}
 			msgBld.append(buildMaster);
 			if(useMock) {
 				msgBld.append(" *** Using mock: ");
 				if(mockEmailTo != null) {
 					msgBld.append(mockEmailTo);
 					if(mockEmailCC != null) {
 						msgBld.append(',');
 						msgBld.append(mockEmailTo);
 					}
 				}
 				else
 					msgBld.append(mockEmailCC);
 				msgBld.append(" ***");
 			}
 			LogUtils.info(msgBld.toString());
 			LogUtils.info("From: %s", emailFromAddr);
 			LogUtils.info("Subject: %s", subject);
 			LogUtils.info("Message content: %s", msgContent);
 
 			List<EmailAddress> recipients;
 			EmailAddress ccRecipient = null;
 			if(useMock) {
 				recipients = mockRecipients();
 				ccRecipient = mockCCRecipient();
 			}
 			else {
 				recipients = toList;
 				if(contrib != null)
 					ccRecipient = buildMaster;
 			}
 			send(smtpHost, smtpPort, emailFromAddr, recipients, ccRecipient, subject, msgContent);
 
 		}
 		catch(IOException e) {
 			LogUtils.error(e, "Failed to send email: %s", e.getMessage());
 		}
 	}
 
 	public void setAction(ActionType action) {
 		this.action = action;
 	}
 
 	public void setBuildID(String buildId) {
 		this.buildID = buildId;
 	}
 
 	public void setBuildModelLocation(File buildModelLocation) {
 		this.buildModelLocation = buildModelLocation;
 	}
 
 	public void setBuildRoot(File buildRoot) {
 		this.buildRoot = buildRoot;
 	}
 
 	public void setEmailFrom(String emailFrom) {
 		this.emailFrom = emailFrom;
 	}
 
 	public void setEmailFromName(String emailFromName) {
 		this.emailFromName = emailFromName;
 	}
 
 	public void setLogLevel(int level) {
 		throw new UnsupportedOperationException("Log levels are not supported");
 	}
 
 	public void setLogURL(String logURL) {
 		this.logURL = logURL;
 	}
 
 	public void setMirrorReferences(boolean mirrorReferences) {
 		this.mirrorReferences = mirrorReferences;
 	}
 
 	public void setMockEmailCC(String mockEmailCc) {
 		this.mockEmailCC = mockEmailCc;
 	}
 
 	public void setMockEmailTo(String mockEmailTo) {
 		this.mockEmailTo = mockEmailTo;
 	}
 
 	public void setProduction(boolean production) {
 		this.production = production;
 	}
 
 	public void setProvisioningAgent(IProvisioningAgent provisioningAgent) {
 		this.provisioningAgent = provisioningAgent;
 	}
 
 	public void setReferenceExcludePattern(String pattern) {
 		pattern = StringUtils.trimmedOrNull(pattern);
 		referenceExcludePattern = pattern == null
 				? null
 				: Pattern.compile(pattern);
 	}
 
 	public void setReferenceIncludePattern(String pattern) {
 		pattern = StringUtils.trimmedOrNull(pattern);
 		referenceIncludePattern = pattern == null
 				? null
 				: Pattern.compile(pattern);
 	}
 
 	public void setSmtpHost(String smtpHost) {
 		this.smtpHost = smtpHost;
 	}
 
 	public void setSmtpPort(int smtpPort) {
 		this.smtpPort = smtpPort;
 	}
 
 	public void setSourceComposite(ValidationSet validationSet, CompositeMetadataRepository sourceComposite) {
 		getValidationSetAnnotations(validationSet).setSourceComposite(sourceComposite);
 	}
 
 	public void setSubjectPrefix(String subjectPrefix) {
 		this.subjectPrefix = subjectPrefix;
 	}
 
 	public void setValidationIUs(List<IInstallableUnit> validationIUs) {
 		this.validationIUs = validationIUs;
 	}
 
 	private void verifyContributions() throws CoreException {
 		List<String> errors = new ArrayList<String>();
 		for(ValidationSet vs : aggregation.getValidationSets()) {
 			for(Contribution contribution : vs.getContributions()) {
 				Resource res = ((EObject) contribution).eResource();
 				for(Resource.Diagnostic diag : res.getErrors()) {
 					String msg = res.getURI() + ": " + diag.toString();
 					LogUtils.error(msg);
 					errors.add(msg);
 				}
 			}
 		}
 
 		if(errors.size() > 0) {
 			sendEmail(null, errors);
 			throw ExceptionUtils.fromMessage("Not all contributions could be parsed");
 		}
 	}
 }
