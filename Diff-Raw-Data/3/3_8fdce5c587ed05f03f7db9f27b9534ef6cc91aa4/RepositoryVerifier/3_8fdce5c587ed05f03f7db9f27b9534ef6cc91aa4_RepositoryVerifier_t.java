 package org.eclipse.b3.aggregator.engine;
 
 import static java.lang.String.format;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.Configuration;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.PackedStrategy;
 import org.eclipse.b3.aggregator.p2.InstallableUnit;
 import org.eclipse.b3.aggregator.p2.MetadataRepository;
 import org.eclipse.b3.aggregator.p2.impl.InstallableUnitImpl;
 import org.eclipse.b3.aggregator.util.LogUtils;
 import org.eclipse.b3.aggregator.util.MonitorUtils;
 import org.eclipse.b3.aggregator.util.TimeUtils;
 import org.eclipse.b3.util.B3Util;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.equinox.internal.p2.console.ProvisioningHelper;
 import org.eclipse.equinox.internal.p2.director.Explanation;
 import org.eclipse.equinox.internal.p2.director.Explanation.HardRequirement;
 import org.eclipse.equinox.internal.p2.director.Explanation.MissingIU;
 import org.eclipse.equinox.internal.p2.director.Explanation.Singleton;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.p2.metadata.query.LatestIUVersionQuery;
 import org.eclipse.equinox.internal.p2.touchpoint.eclipse.PublisherUtil;
 import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
 import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
 import org.eclipse.equinox.internal.provisional.p2.director.RequestStatus;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.engine.IProfile;
 import org.eclipse.equinox.p2.engine.IProfileRegistry;
 import org.eclipse.equinox.p2.engine.InstallableUnitOperand;
 import org.eclipse.equinox.p2.engine.Operand;
 import org.eclipse.equinox.p2.engine.ProvisioningContext;
 import org.eclipse.equinox.p2.engine.ProvisioningPlan;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitPatch;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.query.InstallableUnitQuery;
 import org.eclipse.equinox.p2.query.CompoundQuery;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.MatchQuery;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 
 public class RepositoryVerifier extends BuilderPhase {
 	static class AllPatchesQuery extends MatchQuery<IInstallableUnit> {
 		@Override
 		public boolean isMatch(IInstallableUnit candidate) {
 			return candidate instanceof IInstallableUnitPatch;
 		}
 	}
 
 	static class PatchApplicabilityQuery extends MatchQuery<IInstallableUnit> {
 		private final IInstallableUnitPatch patch;
 
 		PatchApplicabilityQuery(IInstallableUnitPatch patch) {
 			this.patch = patch;
 		}
 
 		@Override
 		public boolean isMatch(IInstallableUnit candidate) {
 			IInstallableUnit iu = candidate;
 			for(IRequirement[] rqs : patch.getApplicabilityScope()) {
 				int idx = rqs.length;
 				while(--idx >= 0)
 					if(!iu.satisfies(rqs[idx]))
 						break;
 				if(idx < 0)
 					return true;
 			}
 			return false;
 		}
 	}
 
 	private static Set<Explanation> getExplanations(RequestStatus requestStatus) {
 		return requestStatus.getExplanations();
 	}
 
 	private static IInstallableUnit[] getRootIUs(URI site, IProfile profile, String iuName, Version version,
 			IProgressMonitor monitor) throws CoreException {
 		IQuery<IInstallableUnit> query = new InstallableUnitQuery(iuName,
 				new VersionRange(version, true, version, true));
 		IQueryResult<IInstallableUnit> roots = ProvisioningHelper.getInstallableUnits(site,
 				CompoundQuery.createCompoundQuery(query, new LatestIUVersionQuery<IInstallableUnit>(), true), monitor);
 
 		if(roots.isEmpty())
 			roots = profile.query(query, new NullProgressMonitor());
 
 		if(roots.isEmpty())
 			throw ExceptionUtils.fromMessage("Feature %s not found", iuName); //$NON-NLS-1$
 
 		return roots.toArray(IInstallableUnit.class);
 	}
 
 	public RepositoryVerifier(Builder builder) {
 		super(builder);
 	}
 
 	@Override
 	public void run(IProgressMonitor monitor) throws CoreException {
 		Builder builder = getBuilder();
 		Aggregator aggregator = builder.getAggregator();
 		List<Configuration> configs = aggregator.getConfigurations();
 		int configCount = configs.size();
 		SubMonitor subMon = SubMonitor.convert(monitor, configCount * 100);
 
 		LogUtils.info("Starting planner verification"); //$NON-NLS-1$
 		long start = TimeUtils.getNow();
 
 		String profilePrefix = Builder.PROFILE_ID + '_';
 
 		final Set<IInstallableUnit> unitsToAggregate = builder.getUnitsToAggregate();
 		B3Util b3util = B3Util.getPlugin();
 		IProfileRegistry profileRegistry = b3util.getService(IProfileRegistry.class);
 		IPlanner planner = b3util.getService(IPlanner.class);
 		URI repoLocation = builder.getSourceCompositeURI();
 		IMetadataRepositoryManager mdrMgr = b3util.getService(IMetadataRepositoryManager.class);
 		try {
 			Set<IInstallableUnit> validationOnlyIUs = null;
 			List<MetadataRepositoryReference> validationRepos = aggregator.getValidationRepositories();
 			for(MetadataRepositoryReference validationRepo : validationRepos) {
 				if(validationRepo.isEnabled()) {
 					if(validationOnlyIUs == null)
 						validationOnlyIUs = new HashSet<IInstallableUnit>();
 					validationOnlyIUs.addAll(validationRepo.getMetadataRepository().getInstallableUnits());
 				}
 			}
 			if(validationOnlyIUs == null)
 				validationOnlyIUs = Collections.emptySet();
 
 			IMetadataRepository sourceRepo = mdrMgr.loadRepository(repoLocation, subMon.newChild(1));
 			for(int configIdx = 0; configIdx < configCount; ++configIdx) {
 				Configuration config = configs.get(configIdx);
 
 				if(!config.isEnabled())
 					continue;
 
 				String configName = config.getName();
 				String info = format("Verifying config %s...", configName); //$NON-NLS-1$
 				LogUtils.info(info);
 				subMon.setTaskName(info);
 
 				Map<String, String> props = new HashMap<String, String>();
 				// TODO Where is FLAVOR gone?
 				//props.put(IProfile.PROP_FLAVOR, "tooling"); //$NON-NLS-1$
 				props.put(IProfile.PROP_ENVIRONMENTS, config.getOSGiEnvironmentString());
 				props.put(IProfile.PROP_INSTALL_FEATURES, "true");
 
 				IProfile profile = null;
 				String profileId = profilePrefix + configName;
 				if(builder.isCleanBuild())
 					profile = profileRegistry.getProfile(profileId);
 
 				if(profile == null)
 					profile = profileRegistry.addProfile(profileId, props);
 
 				IInstallableUnit[] rootArr = getRootIUs(repoLocation, profile, Builder.ALL_CONTRIBUTED_CONTENT_FEATURE,
 						Builder.ALL_CONTRIBUTED_CONTENT_VERSION, subMon.newChild(9));
 
 				// Add as root IU's to a request
 				ProfileChangeRequest request = new ProfileChangeRequest(profile);
 				for(IInstallableUnit rootIU : rootArr)
 					request.setInstallableUnitProfileProperty(rootIU, IProfile.PROP_PROFILE_ROOT_IU,
 							Boolean.TRUE.toString());
 				request.addInstallableUnits(rootArr);
 
 				boolean hadPartials = true;
 				while(hadPartials) {
 					hadPartials = false;
 					ProvisioningContext context = createContext(repoLocation);
 					ProvisioningPlan plan = (ProvisioningPlan) planner.getProvisioningPlan(request, context,
 							subMon.newChild(80, SubMonitor.SUPPRESS_BEGINTASK | SubMonitor.SUPPRESS_SETTASKNAME));
 
 					IStatus status = plan.getStatus();
 					if(status.getSeverity() == IStatus.ERROR) {
 						sendEmails((RequestStatus) plan.getRequestStatus());
 						LogUtils.info("Done. Took %s", TimeUtils.getFormattedDuration(start)); //$NON-NLS-1$
 						throw new CoreException(status);
 					}
 
 					Set<IInstallableUnit> suspectedValidationOnlyIUs = null;
 					Operand[] ops = plan.getOperands();
 					for(Operand op : ops) {
 						if(!(op instanceof InstallableUnitOperand))
 							continue;
 
 						InstallableUnitOperand iuOp = (InstallableUnitOperand) op;
 						IInstallableUnit iu = iuOp.second();
 						if(iu == null)
 							continue;
 
 						String id = iu.getId();
 						if(Builder.ALL_CONTRIBUTED_CONTENT_FEATURE.equals(id))
 							continue;
 
 						if(validationOnlyIUs.contains(iu)) {
 							// This IU should not be included unless it is also included in one of
 							// the contributed repositories
 							if(suspectedValidationOnlyIUs == null)
 								suspectedValidationOnlyIUs = new HashSet<IInstallableUnit>();
 							suspectedValidationOnlyIUs.add(iu);
 						}
 						else {
 							if(!unitsToAggregate.contains(iu)) {
 								if(Boolean.valueOf(iu.getProperty(IInstallableUnit.PROP_PARTIAL_IU)).booleanValue()) {
 									iu = resolvePartialIU(iu, subMon.newChild(1));
 									hadPartials = true;
 								}
 								unitsToAggregate.add(iu);
 							}
 						}
 					}
 
 					Iterator<IInstallableUnit> itor = sourceRepo.query(new AllPatchesQuery(), subMon.newChild(1)).iterator();
 
 					while(itor.hasNext()) {
 						Set<IInstallableUnit> units = getUnpatchedTransitiveScope((IInstallableUnitPatch) itor.next(),
 								profile, planner, repoLocation, subMon.newChild(1));
 						for(IInstallableUnit iu : units) {
 							if(validationOnlyIUs.contains(iu)) {
 								// This IU should not be included unless it is also included in one of
 								// the contributed repositories
 								if(suspectedValidationOnlyIUs == null)
 									suspectedValidationOnlyIUs = new HashSet<IInstallableUnit>();
 								suspectedValidationOnlyIUs.add(iu);
 							}
 							else {
 								if(!unitsToAggregate.contains(iu)) {
 									if(Boolean.valueOf(iu.getProperty(IInstallableUnit.PROP_PARTIAL_IU)).booleanValue()) {
 										iu = resolvePartialIU(iu, subMon.newChild(1));
 										hadPartials = true;
 									}
 									unitsToAggregate.add(iu);
 								}
 							}
 						}
 					}
 
 					if(suspectedValidationOnlyIUs != null) {
 						// Prune the set of IU's that we suspect are there for validation
 						// purposes only using the source repository
 						//
 						final Set<IInstallableUnit> candidates = suspectedValidationOnlyIUs;
 						final boolean hadPartialsHolder[] = new boolean[] { false };
 						sourceRepo.query(new MatchQuery<IInstallableUnit>() {
 							@Override
							public boolean isMatch(IInstallableUnit iu) {
 								if(candidates.contains(iu) && !unitsToAggregate.contains(iu)) {
 									try {
 										if(Boolean.valueOf(iu.getProperty(IInstallableUnit.PROP_PARTIAL_IU)).booleanValue()) {
 											iu = resolvePartialIU(iu, SubMonitor.convert(new NullProgressMonitor()));
 											hadPartialsHolder[0] = true;
 										}
 									}
 									catch(CoreException e) {
 										throw new RuntimeException(e);
 									}
 									unitsToAggregate.add(iu);
 								}
 
 								return false; // Since we don't use the results
 							}
 						}, subMon.newChild(1));
 					}
 
 					if(hadPartials)
 						// Rerun the validation
 						LogUtils.info("Partial IU's encountered. Verifying %s again...", configName); //$NON-NLS-1$
 				}
 			}
 		}
 		catch(RuntimeException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		finally {
 			MonitorUtils.done(subMon);
 			b3util.ungetService(profileRegistry);
 			b3util.ungetService(planner);
 			b3util.ungetService(mdrMgr);
 		}
 	}
 
 	InstallableUnit resolvePartialIU(IInstallableUnit iu, SubMonitor subMon) throws CoreException {
 		B3Util b3util = B3Util.getPlugin();
 		IArtifactRepositoryManager arMgr = b3util.getService(IArtifactRepositoryManager.class);
 		String info = "Converting partial IU for " + iu.getId() + "...";
 		subMon.beginTask(info, IProgressMonitor.UNKNOWN);
 		LogUtils.debug(info);
 
 		// Scan all mapped repositories for this IU
 		//
 		IInstallableUnit miu = null;
 		MetadataRepository mdr = null;
 		contribs: for(Contribution contrib : getBuilder().getAggregator().getContributions(true))
 
 			for(MappedRepository repo : contrib.getRepositories(true)) {
 				MetadataRepository candidate = repo.getMetadataRepository();
 				for(IInstallableUnit candidateIU : candidate.getInstallableUnits())
 					if(iu.getId().equals(candidateIU.getId()) && iu.getVersion().equals(candidateIU.getVersion())) {
 						mdr = candidate;
 						miu = candidateIU;
 						break contribs;
 					}
 			}
 
 		if(mdr == null)
 			throw ExceptionUtils.fromMessage("Unable to locate mapped repository for IU %s/%s", iu.getId(),
 					iu.getVersion());
 
 		try {
 			IArtifactRepository sourceAr = arMgr.loadRepository(mdr.getLocation(), subMon.newChild(10));
 			File tempRepositoryFolder = getBuilder().getTempRepositoryFolder();
 			tempRepositoryFolder.mkdirs();
 
 			URI tempRepositoryURI = Builder.createURI(tempRepositoryFolder);
 			IFileArtifactRepository tempAr;
 			try {
 				tempAr = (IFileArtifactRepository) arMgr.loadRepository(tempRepositoryURI, subMon.newChild(1));
 			}
 			catch(ProvisionException e) {
 				tempAr = (IFileArtifactRepository) arMgr.createRepository(tempRepositoryURI, "temporary artifacts"
 						+ " artifacts", Builder.SIMPLE_ARTIFACTS_TYPE, Collections.<String, String> emptyMap()); //$NON-NLS-1$
 			}
 
 			Collection<IArtifactKey> artifacts = miu.getArtifacts();
 			ArrayList<String> errors = new ArrayList<String>();
 			MirrorGenerator.mirror(artifacts, null, sourceAr, tempAr, PackedStrategy.UNPACK_AS_SIBLING, errors,
 					subMon.newChild(1));
 			int numErrors = errors.size();
 			if(numErrors > 0) {
 				IStatus[] children = new IStatus[numErrors];
 				for(int idx = 0; idx < numErrors; ++idx)
 					children[idx] = new Status(IStatus.ERROR, Engine.PLUGIN_ID, errors.get(idx));
 				MultiStatus status = new MultiStatus(Engine.PLUGIN_ID, IStatus.ERROR, children, "Unable to mirror",
 						null);
 				throw new CoreException(status);
 			}
 
 			IArtifactKey key = artifacts.iterator().next();
 			File bundleFile = tempAr.getArtifactFile(key);
 			if(bundleFile == null)
 				throw ExceptionUtils.fromMessage(
 						"Unable to resolve partial IU. Artifact file for %s could not be found", key);
 
 			IInstallableUnit preparedIU = PublisherUtil.createBundleIU(key, bundleFile);
 			if(preparedIU == null)
 				throw ExceptionUtils.fromMessage(
 						"Unable to resolve partial IU. Artifact file for %s did not contain a bundle manifest", key);
 			InstallableUnit newIU = InstallableUnitImpl.importToModel(preparedIU);
 
 			List<IInstallableUnit> allIUs = mdr.getInstallableUnits();
 			allIUs.remove(miu);
 			allIUs.add(newIU);
 			return newIU;
 		}
 		finally {
 			b3util.ungetService(arMgr);
 		}
 	}
 
 	private boolean addLeafmostContributions(Set<Explanation> explanations, Map<String, Contribution> contributions,
 			IRequirement prq) {
 		boolean contribsFound = false;
 		for(Explanation explanation : explanations) {
 			if(explanation instanceof Singleton) {
 				if(contribsFound)
 					// All explicit contributions for Singletons are added at
 					// top level. We just
 					// want to find out if this Singleton is the leaf problem
 					// here, not add anything
 					continue;
 
 				for(IInstallableUnit iu : ((Singleton) explanation).ius) {
 					if(prq.isMatch(iu)) {
 						// A singleton is always a leaf problem. Add
 						// contributions
 						// if we can find any
 						Contribution contrib = findContribution(iu.getId());
 						if(contrib == null)
 							continue;
 						contribsFound = true;
 					}
 				}
 				continue;
 			}
 
 			IInstallableUnit iu;
 			IRequirement crq;
 			if(explanation instanceof HardRequirement) {
 				HardRequirement hrq = (HardRequirement) explanation;
 				iu = hrq.iu;
 				crq = hrq.req;
 			}
 			else if(explanation instanceof MissingIU) {
 				MissingIU miu = (MissingIU) explanation;
 				iu = miu.iu;
 				crq = miu.req;
 			}
 			else
 				continue;
 
 			if(prq.isMatch(iu)) {
 				// This IU would have fulfilled the failing request but it
 				// has
 				// apparent problems of its own.
 				if(addLeafmostContributions(explanations, contributions, crq)) {
 					contribsFound = true;
 					continue;
 				}
 
 				Contribution contrib = findContribution(iu, crq);
 				if(contrib == null)
 					continue;
 				contributions.put(contrib.getLabel(), contrib);
 				continue;
 			}
 		}
 		return contribsFound;
 	}
 
 	private ProvisioningContext createContext(URI site) {
 		List<MetadataRepositoryReference> validationRepos = getBuilder().getAggregator().getValidationRepositories();
 		int top = validationRepos.size();
 		List<URI> sites = new ArrayList<URI>(top + 1);
 		sites.add(site);
 
 		URI[] repoLocations = new URI[top + 1];
 		for(int idx = 0; idx < top; ++idx) {
 			MetadataRepositoryReference mdRef = validationRepos.get(idx);
 			if(mdRef.isEnabled())
 				sites.add(URI.create(mdRef.getResolvedLocation()));
 		}
 		repoLocations = sites.toArray(new URI[sites.size()]);
 		ProvisioningContext context = new ProvisioningContext(repoLocations);
 		context.setArtifactRepositories(repoLocations);
 		return context;
 	}
 
 	private Contribution findContribution(IInstallableUnit iu, IRequirement rq) {
 		if(!(rq instanceof IRequiredCapability))
 			return null;
 
 		IRequiredCapability cap = (IRequiredCapability) rq;
 		Contribution contrib = null;
 		if(Builder.NAMESPACE_OSGI_BUNDLE.equals(cap.getNamespace())
 				|| IInstallableUnit.NAMESPACE_IU_ID.equals(cap.getNamespace()))
 			contrib = findContribution(cap.getName());
 
 		if(contrib == null)
 			// Not found, try the owner of the requirement
 			contrib = findContribution(iu.getId());
 		return contrib;
 	}
 
 	private Contribution findContribution(String componentId) {
 		for(Contribution contrib : getBuilder().getAggregator().getContributions())
 			for(MappedRepository repository : contrib.getRepositories())
 				for(MappedUnit mu : repository.getUnits(true))
 					if(componentId.equals(mu.getName()))
 						return contrib;
 		return null;
 	}
 
 	private Set<IInstallableUnit> getUnpatchedTransitiveScope(IInstallableUnitPatch patch, IProfile profile,
 			IPlanner planner, URI repoLocation, SubMonitor monitor) throws CoreException {
 		B3Util b3util = B3Util.getPlugin();
 		IMetadataRepositoryManager mdrMgr = b3util.getService(IMetadataRepositoryManager.class);
 		try {
 			monitor.beginTask(null, 10);
 			IMetadataRepository sourceRepo = mdrMgr.loadRepository(repoLocation, monitor.newChild(1));
 			PatchApplicabilityQuery query = new PatchApplicabilityQuery(patch);
 			IQueryResult<IInstallableUnit> result = sourceRepo.query(query, monitor.newChild(1));
 
 			IInstallableUnit[] rootArr = result.toArray(IInstallableUnit.class);
 			// Add as root IU's to a request
 			ProfileChangeRequest request = new ProfileChangeRequest(profile);
 			for(IInstallableUnit rootIU : rootArr)
 				request.setInstallableUnitProfileProperty(rootIU, IProfile.PROP_PROFILE_ROOT_IU,
 						Boolean.TRUE.toString());
 			request.addInstallableUnits(rootArr);
 			ProvisioningPlan plan = (ProvisioningPlan) planner.getProvisioningPlan(request, new ProvisioningContext(
 					new URI[] { repoLocation }), monitor.newChild(8));
 
 			HashSet<IInstallableUnit> units = new HashSet<IInstallableUnit>();
 			units.add(patch);
 			Operand[] ops = plan.getOperands();
 			for(Operand op : ops) {
 				if(!(op instanceof InstallableUnitOperand))
 					continue;
 
 				InstallableUnitOperand iuOp = (InstallableUnitOperand) op;
 				IInstallableUnit iu = iuOp.second();
 				if(iu != null)
 					units.add(iu);
 			}
 			return units;
 		}
 		finally {
 			b3util.ungetService(mdrMgr);
 		}
 	}
 
 	private void sendEmails(RequestStatus requestStatus) {
 		Builder builder = getBuilder();
 		if(!builder.getAggregator().isSendmail())
 			return;
 
 		ArrayList<String> errors = new ArrayList<String>();
 		Set<Explanation> explanations = getExplanations(requestStatus);
 		Map<String, Contribution> contribs = new HashMap<String, Contribution>();
 		for(Explanation explanation : explanations) {
 			errors.add(explanation.toString());
 			if(explanation instanceof Singleton) {
 				// A singleton is always a leaf problem. Add contributions
 				// if we can find any. They are all culprits
 				for(IInstallableUnit iu : ((Singleton) explanation).ius) {
 					Contribution contrib = findContribution(iu.getId());
 					if(contrib == null)
 						continue;
 					contribs.put(contrib.getLabel(), contrib);
 				}
 				continue;
 			}
 
 			IInstallableUnit iu;
 			IRequirement crq;
 			if(explanation instanceof HardRequirement) {
 				HardRequirement hrq = (HardRequirement) explanation;
 				iu = hrq.iu;
 				crq = hrq.req;
 			}
 			else if(explanation instanceof MissingIU) {
 				MissingIU miu = (MissingIU) explanation;
 				iu = miu.iu;
 				crq = miu.req;
 			}
 			else
 				continue;
 
 			// Find the leafmost contributions for the problem. We don't want to
 			// blame
 			// consuming contributors
 			if(!addLeafmostContributions(explanations, contribs, crq)) {
 				Contribution contrib = findContribution(iu, crq);
 				if(contrib == null)
 					continue;
 				contribs.put(contrib.getLabel(), contrib);
 			}
 		}
 		if(contribs.isEmpty())
 			builder.sendEmail(null, errors);
 		else {
 			for(Contribution contrib : contribs.values())
 				builder.sendEmail(contrib, errors);
 		}
 	}
 
 }
