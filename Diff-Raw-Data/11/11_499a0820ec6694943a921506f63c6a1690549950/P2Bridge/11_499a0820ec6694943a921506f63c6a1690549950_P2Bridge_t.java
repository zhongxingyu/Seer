 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.p2.util;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.b3.p2.ArtifactDescriptor;
 import org.eclipse.b3.p2.ArtifactKey;
 import org.eclipse.b3.p2.ArtifactRepository;
 import org.eclipse.b3.p2.Copyright;
 import org.eclipse.b3.p2.InstallableUnit;
 import org.eclipse.b3.p2.License;
 import org.eclipse.b3.p2.MappingRule;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.P2Factory;
 import org.eclipse.b3.p2.ProvidedCapability;
 import org.eclipse.b3.p2.Requirement;
 import org.eclipse.b3.p2.TouchpointData;
 import org.eclipse.b3.p2.TouchpointInstruction;
 import org.eclipse.b3.p2.TouchpointType;
 import org.eclipse.b3.p2.UpdateDescriptor;
 import org.eclipse.b3.p2.impl.ArtifactDescriptorImpl;
 import org.eclipse.b3.p2.impl.ArtifactKeyImpl;
 import org.eclipse.b3.p2.impl.ArtifactRepositoryImpl;
 import org.eclipse.b3.p2.impl.CopyrightImpl;
 import org.eclipse.b3.p2.impl.InstallableUnitFragmentImpl;
 import org.eclipse.b3.p2.impl.InstallableUnitImpl;
 import org.eclipse.b3.p2.impl.InstallableUnitPatchImpl;
 import org.eclipse.b3.p2.impl.LicenseImpl;
 import org.eclipse.b3.p2.impl.MappingRuleImpl;
 import org.eclipse.b3.p2.impl.MetadataRepositoryImpl;
 import org.eclipse.b3.p2.impl.ProcessingStepDescriptorImpl;
 import org.eclipse.b3.p2.impl.ProvidedCapabilityImpl;
 import org.eclipse.b3.p2.impl.RepositoryImpl;
 import org.eclipse.b3.p2.impl.RequiredCapabilityImpl;
 import org.eclipse.b3.p2.impl.RequirementChangeImpl;
 import org.eclipse.b3.p2.impl.RequirementImpl;
 import org.eclipse.b3.p2.impl.SimpleArtifactDescriptorImpl;
 import org.eclipse.b3.p2.impl.SimpleArtifactRepositoryImpl;
 import org.eclipse.b3.p2.impl.TouchpointInstructionImpl;
 import org.eclipse.b3.p2.impl.TouchpointTypeImpl;
 import org.eclipse.b3.p2.impl.UpdateDescriptorImpl;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.b3.util.MonitorUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactDescriptor;
 import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.ICopyright;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitFragment;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitPatch;
 import org.eclipse.equinox.p2.metadata.ILicense;
 import org.eclipse.equinox.p2.metadata.IProvidedCapability;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.IRequirementChange;
 import org.eclipse.equinox.p2.metadata.ITouchpointData;
 import org.eclipse.equinox.p2.metadata.ITouchpointInstruction;
 import org.eclipse.equinox.p2.metadata.ITouchpointType;
 import org.eclipse.equinox.p2.metadata.IUpdateDescriptor;
 import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
 import org.eclipse.equinox.p2.query.ExpressionMatchQuery;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.repository.IRepository;
 import org.eclipse.equinox.p2.repository.IRepositoryManager;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.artifact.IProcessingStepDescriptor;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 
 /**
  * @author filip.hrbek@cloudsmith.com
  * 
  */
 public class P2Bridge {
 
 	public static final IQuery<IInstallableUnit> QUERY_ALL_IUS = QueryUtil.createIUAnyQuery();
 
 	public static final IQuery<IArtifactKey> QUERY_ALL_ARTIFACTS = new ExpressionMatchQuery<IArtifactKey>(
 		IArtifactKey.class, ExpressionUtil.TRUE_EXPRESSION);
 
 	public static void exportFromModel(IArtifactRepositoryManager arMgr, IArtifactRepository ar,
 			boolean overwriteExisting, IProgressMonitor monitor) throws CoreException {
 		exportFromModel(arMgr, ar, ar.getLocation(), overwriteExisting, false, monitor);
 	}
 
 	public static void exportFromModel(IArtifactRepositoryManager arMgr, IArtifactRepository ar, URI targetLocation,
 			boolean overwriteExisting, boolean sortArtifacts, IProgressMonitor monitor) throws CoreException {
 
		if(overwriteExisting) {
 			deleteIfExists(targetLocation, "artifacts.xml", "artifacts.jar", "compositeArtifacts.jar");
			arMgr.removeRepository(targetLocation);
		}
 
 		IArtifactRepository target = arMgr.createRepository(
 			targetLocation, ar.getName(), ar.getType(), ar.getProperties());
 
 		monitor = MonitorUtils.ensureNotNull(monitor);
 		IQueryResult<IArtifactKey> result = ar.query(QUERY_ALL_ARTIFACTS, monitor);
 		Iterator<IArtifactKey> itor = result.iterator();
 		ArrayList<IArtifactKey> artifacts = new ArrayList<IArtifactKey>();
 		while(itor.hasNext())
 			artifacts.add(itor.next());
 		if(sortArtifacts)
 			Collections.sort(artifacts, new Comparator<IArtifactKey>() {
 
 				public int compare(IArtifactKey a1, IArtifactKey a2) {
 					int result = a1.getId().compareTo(a2.getId());
 					if(result == 0)
 						return a1.getVersion().compareTo(a2.getVersion());
 
 					return result;
 				}
 
 			});
 		for(IArtifactKey key : artifacts) {
 			IArtifactDescriptor[] descriptors = ar.getArtifactDescriptors(key);
 			for(IArtifactDescriptor descriptor : descriptors) {
 				if(descriptor instanceof SimpleArtifactDescriptorImpl) {
 					SimpleArtifactDescriptor p2native = new SimpleArtifactDescriptor(descriptor);
 					p2native.setRepositoryProperty(
 						SimpleArtifactDescriptor.ARTIFACT_REFERENCE,
 						((SimpleArtifactDescriptorImpl) descriptor).getRepositoryProperty(SimpleArtifactDescriptor.ARTIFACT_REFERENCE));
 					target.addDescriptor(p2native);
 				}
 				else
 					target.addDescriptor(descriptor);
 			}
 		}
 	}
 
 	public static void exportFromModel(IMetadataRepositoryManager mdrMgr, IMetadataRepository mdr,
 			boolean overwriteExisting, IProgressMonitor monitor) throws CoreException {
 		exportFromModel(mdrMgr, mdr, mdr.getLocation(), overwriteExisting, false, monitor);
 	}
 
 	public static void exportFromModel(IMetadataRepositoryManager mdrMgr, IMetadataRepository mdr, URI targetLocation,
 			boolean overwriteExisting, boolean sortIUs, IProgressMonitor monitor) throws CoreException {
 
		if(overwriteExisting) {
 			deleteIfExists(targetLocation, "content.xml", "content.jar", "compositeContent.jar");
			mdrMgr.removeRepository(targetLocation);
		}
 
 		IMetadataRepository target = mdrMgr.createRepository(
 			targetLocation, mdr.getName(), mdr.getType(), mdr.getProperties());
 
 		monitor = MonitorUtils.ensureNotNull(monitor);
 		IQueryResult<IInstallableUnit> result = mdr.query(QUERY_ALL_IUS, monitor);
 		Iterator<IInstallableUnit> itor = result.iterator();
 		ArrayList<IInstallableUnit> ius = new ArrayList<IInstallableUnit>();
 		while(itor.hasNext())
 			ius.add(itor.next());
 		if(sortIUs)
 			Collections.sort(ius);
 		target.addInstallableUnits(ius);
 
 		target.addReferences(mdr.getReferences());
 	}
 
 	public static ArtifactKey importToModel(IArtifactKey key) {
 		if(key == null)
 			return null;
 		ArtifactKeyImpl mkey = (ArtifactKeyImpl) P2Factory.eINSTANCE.createArtifactKey();
 		mkey.setClassifier(key.getClassifier());
 		mkey.setId(key.getId());
 		mkey.setVersion(key.getVersion());
 		return mkey;
 	}
 
 	public static ArtifactDescriptor importToModel(IArtifactRepository target, IArtifactDescriptor descriptor) {
 		if(descriptor == null)
 			return null;
 		ArtifactDescriptorImpl mdescriptor = (ArtifactDescriptorImpl) target.createArtifactDescriptor(descriptor.getArtifactKey());
 		List<IProcessingStepDescriptor> msteps = mdescriptor.getProcessingStepList();
 		for(IProcessingStepDescriptor step : descriptor.getProcessingSteps())
 			msteps.add(importToModel(step));
 
 		Map<String, String> props = descriptor.getProperties();
 		if(props.size() > 0)
 			mdescriptor.getPropertyMap().putAll(props);
 
 		if(descriptor instanceof SimpleArtifactDescriptor) {
 			props = ((SimpleArtifactDescriptor) descriptor).getRepositoryProperties();
 			((SimpleArtifactDescriptorImpl) mdescriptor).getRepositoryPropertyMap().putAll(props);
 		}
 		return mdescriptor;
 	}
 
 	public static void importToModel(IArtifactRepositoryManager arMgr, IArtifactRepository ar,
 			ArtifactRepositoryImpl target, IProgressMonitor monitor) throws CoreException {
 		importToModel(arMgr, ar, target);
 		IQuery<IArtifactDescriptor> descriptorQuery = QueryUtil.createMatchQuery(
 			IArtifactDescriptor.class, ExpressionUtil.TRUE_EXPRESSION);
 		Set<IArtifactDescriptor> result = ar.descriptorQueryable().query(descriptorQuery, monitor).toUnmodifiableSet();
 		for(IArtifactDescriptor desc : result)
 			target.addDescriptor(desc);
 
 		if(ar instanceof SimpleArtifactRepository) {
 			String[][] rules = ((SimpleArtifactRepository) ar).getRules();
 			if(rules != null) {
 				List<MappingRule> trules = ((SimpleArtifactRepositoryImpl) target).getRules();
 				for(String[] ruleDef : rules) {
 					MappingRuleImpl trule = (MappingRuleImpl) P2Factory.eINSTANCE.createMappingRule();
 					trule.setFilter(ruleDef[0]);
 					trule.setOutput(ruleDef[1]);
 					trules.add(trule);
 				}
 			}
 		}
 	}
 
 	public static ArtifactRepository importToModel(IArtifactRepositoryManager arMgr, IArtifactRepository ar,
 			IProgressMonitor monitor) throws CoreException {
 		ArtifactRepositoryImpl target;
 		if(ar instanceof SimpleArtifactRepository)
 			target = (ArtifactRepositoryImpl) P2Factory.eINSTANCE.createSimpleArtifactRepository();
 		else
 			target = (ArtifactRepositoryImpl) P2Factory.eINSTANCE.createArtifactRepository();
 		importToModel(arMgr, ar, target, monitor);
 		return target;
 	}
 
 	public static Copyright importToModel(ICopyright cr) {
 		if(cr == null)
 			return null;
 		CopyrightImpl mcr = (CopyrightImpl) P2Factory.eINSTANCE.createCopyright();
 		mcr.setBody(cr.getBody());
 		mcr.setLocation(cr.getLocation());
 		return mcr;
 	}
 
 	public static InstallableUnit importToModel(IInstallableUnit iu) {
 		if(iu == null)
 			return null;
 
 		P2Factory factory = P2Factory.eINSTANCE;
 		InstallableUnitImpl miu;
 		if(iu instanceof IInstallableUnitFragment) {
 			InstallableUnitFragmentImpl miuf = (InstallableUnitFragmentImpl) factory.createInstallableUnitFragment();
 			Collection<IRequirement> mhosts = miuf.getHost();
 			for(IRequirement host : ((IInstallableUnitFragment) iu).getHost())
 				mhosts.add(importToModel(host));
 			miu = miuf;
 		}
 		else if(iu instanceof IInstallableUnitPatch) {
 			IInstallableUnitPatch iup = (IInstallableUnitPatch) iu;
 			InstallableUnitPatchImpl miup = (InstallableUnitPatchImpl) factory.createInstallableUnitPatch();
 
 			IRequirement[][] scope = iup.getApplicabilityScope();
 			if(scope != null) {
 				List<IRequirement> appliesTo = miup.getAppliesTo();
 				for(IRequirement[] rqs : scope)
 					for(IRequirement rq : rqs)
 						appliesTo.add(importToModel(rq));
 			}
 			miup.setLifeCycle(importToModel(iup.getLifeCycle()));
 
 			List<IRequirementChange> mrqChanges = miup.getRequirementsChange();
 			for(IRequirementChange rqChange : iup.getRequirementsChange())
 				mrqChanges.add(importToModel(rqChange));
 			miu = miup;
 		}
 		else
 			miu = (InstallableUnitImpl) factory.createInstallableUnit();
 
 		miu.setCopyright(importToModel(iu.getCopyright()));
 		miu.setFilter(iu.getFilter());
 		miu.setId(iu.getId());
 		for(ILicense license : iu.getLicenses())
 			miu.getLicenses().add(importToModel(license));
 		miu.setResolved(iu.isResolved());
 		miu.setSingleton(iu.isSingleton());
 		miu.setTouchpointType(importToModel(iu.getTouchpointType()));
 		miu.setUpdateDescriptor(importToModel(iu.getUpdateDescriptor()));
 		miu.setVersion(iu.getVersion());
 
 		Map<String, String> props = iu.getProperties();
 		if(props.size() > 0)
 			miu.getPropertyMap().putAll(props);
 
 		List<IArtifactKey> keys = miu.getArtifacts();
 		for(IArtifactKey key : iu.getArtifacts())
 			keys.add(importToModel(key));
 
 		List<IRequirement> reqs = miu.getMetaRequirements();
 		for(IRequirement req : iu.getMetaRequirements())
 			reqs.add(importToModel(req));
 
 		reqs = miu.getRequirements();
 		for(IRequirement rc : iu.getRequirements())
 			reqs.add(importToModel(rc));
 
 		List<IProvidedCapability> pcs = miu.getProvidedCapabilities();
 		for(IProvidedCapability pc : iu.getProvidedCapabilities())
 			pcs.add(importToModel(pc));
 
 		List<ITouchpointData> tds = miu.getTouchpointData();
 		for(ITouchpointData td : iu.getTouchpointData())
 			tds.add(importToModel(td));
 
 		return miu;
 	}
 
 	public static License importToModel(ILicense lc) {
 		if(lc == null)
 			return null;
 		LicenseImpl mlc = (LicenseImpl) P2Factory.eINSTANCE.createLicense();
 		mlc.setBody(lc.getBody());
 		mlc.setUUID(lc.getUUID());
 		mlc.setLocation(lc.getLocation());
 		return mlc;
 	}
 
 	public static MetadataRepository importToModel(IMetadataRepositoryManager mdrMgr, IMetadataRepository mdr,
 			IProgressMonitor monitor) throws CoreException {
 		MetadataRepositoryImpl target = (MetadataRepositoryImpl) P2Factory.eINSTANCE.createMetadataRepository();
 		importToModel(mdrMgr, mdr, target, monitor);
 		return target;
 	}
 
 	public static void importToModel(IMetadataRepositoryManager mdrMgr, IMetadataRepository mdr,
 			MetadataRepositoryImpl target, IProgressMonitor monitor) throws CoreException {
 		importToModel(mdrMgr, mdr, target, monitor, false);
 	}
 
 	public static void importToModel(IMetadataRepositoryManager mdrMgr, IMetadataRepository mdr,
 			MetadataRepositoryImpl target, IProgressMonitor monitor, boolean sortIUs) throws CoreException {
 		importToModel(mdrMgr, mdr, target);
 		monitor = MonitorUtils.ensureNotNull(monitor);
 		IQueryResult<IInstallableUnit> result = mdr.query(QUERY_ALL_IUS, monitor);
 		Iterator<IInstallableUnit> itor = result.iterator();
 		ArrayList<InstallableUnit> ius = new ArrayList<InstallableUnit>();
 		while(itor.hasNext())
 			ius.add(P2Bridge.importToModel(itor.next()));
 		if(sortIUs)
 			Collections.sort(ius);
 		target.getInstallableUnits().addAll(ius);
 
 		target.addRepositoryReferences(mdrMgr, mdr);
 	}
 
 	public static IProcessingStepDescriptor importToModel(IProcessingStepDescriptor step) {
 		if(step == null)
 			return null;
 
 		ProcessingStepDescriptorImpl mstep = (ProcessingStepDescriptorImpl) P2Factory.eINSTANCE.createProcessingStepDescriptor();
 		mstep.setData(step.getData());
 		mstep.setProcessorId(mstep.getProcessorId());
 		mstep.setRequired(step.isRequired());
 		return mstep;
 	}
 
 	public static ProvidedCapability importToModel(IProvidedCapability pc) {
 		if(pc == null)
 			return null;
 		ProvidedCapabilityImpl mrq = (ProvidedCapabilityImpl) P2Factory.eINSTANCE.createProvidedCapability();
 		mrq.setName(pc.getName());
 		mrq.setNamespace(pc.getNamespace());
 		mrq.setVersion(pc.getVersion());
 		return mrq;
 	}
 
 	public static <T> void importToModel(IRepositoryManager<T> repoMgr, IRepository<T> repo, RepositoryImpl<T> target)
 			throws CoreException {
 		target.setProvisioningAgent(repo.getProvisioningAgent());
 		target.setName(repo.getName());
 		target.setLocation(repo.getLocation());
 		target.setDescription(repo.getDescription());
 		target.setProvider(repo.getProvider());
 		target.setType(repo.getType());
 		target.setVersion(repo.getVersion());
 		target.getPropertyMap().putAll(repo.getProperties());
 	}
 
 	public static Requirement importToModel(IRequirement req) {
 		if(req == null)
 			return null;
 
 		RequirementImpl mreq;
 
 		if(req instanceof IRequiredCapability) {
 			IRequiredCapability rc = (IRequiredCapability) req;
 			RequiredCapabilityImpl mrc = (RequiredCapabilityImpl) P2Factory.eINSTANCE.createRequiredCapability();
 			mrc.setName(rc.getName());
 			mrc.setNamespace(rc.getNamespace());
 			mrc.setRange(rc.getRange());
 
 			mreq = mrc;
 		}
 		else
 			mreq = (RequirementImpl) P2Factory.eINSTANCE.createRequirement();
 
 		mreq.setFilter(req.getFilter());
 		mreq.setGreedy(req.isGreedy());
 		mreq.setMin(req.getMin());
 		mreq.setMax(req.getMax());
 		mreq.setDescription(req.getDescription());
 
 		return mreq;
 	}
 
 	public static IRequirementChange importToModel(IRequirementChange rqChange) {
 		if(rqChange == null)
 			return null;
 		RequirementChangeImpl mrqc = (RequirementChangeImpl) P2Factory.eINSTANCE.createRequirementChange();
 		mrqc.setApplyOn((IRequiredCapability) importToModel(rqChange.applyOn()));
 		mrqc.setNewValue((IRequiredCapability) importToModel(rqChange.newValue()));
 		return mrqc;
 	}
 
 	public static TouchpointData importToModel(ITouchpointData ptd) {
 		if(ptd == null)
 			return null;
 		TouchpointData mtpd = P2Factory.eINSTANCE.createTouchpointData();
 		EMap<String, ITouchpointInstruction> minstrMap = mtpd.getInstructionMap();
 		Map<String, ITouchpointInstruction> instrMap = ptd.getInstructions();
 		for(Map.Entry<String, ITouchpointInstruction> instr : instrMap.entrySet())
 			minstrMap.put(instr.getKey(), importToModel(instr.getValue()));
 		return mtpd;
 	}
 
 	public static TouchpointInstruction importToModel(ITouchpointInstruction ti) {
 		if(ti == null)
 			return null;
 		TouchpointInstructionImpl mti = (TouchpointInstructionImpl) P2Factory.eINSTANCE.createTouchpointInstruction();
 		mti.setBody(ti.getBody());
 		mti.setImportAttribute(ti.getImportAttribute());
 		return mti;
 	}
 
 	public static TouchpointType importToModel(ITouchpointType tpt) {
 		if(tpt == null)
 			return null;
 		TouchpointTypeImpl mtpt = (TouchpointTypeImpl) P2Factory.eINSTANCE.createTouchpointType();
 		mtpt.setId(tpt.getId());
 		mtpt.setVersion(tpt.getVersion());
 		return mtpt;
 	}
 
 	public static UpdateDescriptor importToModel(IUpdateDescriptor ud) {
 		if(ud == null)
 			return null;
 		UpdateDescriptorImpl mud = (UpdateDescriptorImpl) P2Factory.eINSTANCE.createUpdateDescriptor();
 		mud.setDescription(ud.getDescription());
 		mud.setIUsBeingUpdated(ud.getIUsBeingUpdated());
 		mud.setSeverity(ud.getSeverity());
 		mud.setLocation(ud.getLocation());
 		return mud;
 	}
 
 	private static void deleteIfExists(URI location, String... fileNames) throws CoreException {
 		File folder = new File(location);
 		for(String fileName : fileNames) {
 			File file = new File(folder, fileName);
 			if(file.exists())
 				if(!file.delete())
 					throw ExceptionUtils.fromMessage("Unable to delete %s", file.getAbsolutePath());
 		}
 	}
 }
