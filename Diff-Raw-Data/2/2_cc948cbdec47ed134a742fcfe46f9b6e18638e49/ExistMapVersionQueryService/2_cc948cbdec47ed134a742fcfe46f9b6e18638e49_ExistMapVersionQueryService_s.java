 package edu.mayo.cts2.framework.plugin.service.exist.profile.mapversion;
 
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Component;
 
 import edu.mayo.cts2.framework.model.command.Page;
 import edu.mayo.cts2.framework.model.command.ResolvedFilter;
 import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
 import edu.mayo.cts2.framework.model.core.EntityReferenceList;
 import edu.mayo.cts2.framework.model.core.PredicateReference;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.model.entity.EntityDescription;
 import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
 import edu.mayo.cts2.framework.model.mapversion.MapVersion;
 import edu.mayo.cts2.framework.model.mapversion.MapVersionDirectoryEntry;
 import edu.mayo.cts2.framework.model.service.core.NameOrURI;
 import edu.mayo.cts2.framework.model.service.core.Query;
 import edu.mayo.cts2.framework.model.service.mapversion.types.MapRole;
 import edu.mayo.cts2.framework.model.service.mapversion.types.MapStatus;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.AbstractExistQueryService;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.DefaultResourceInfo;
 import edu.mayo.cts2.framework.plugin.service.exist.restrict.directory.XpathDirectoryBuilder;
 import edu.mayo.cts2.framework.plugin.service.exist.util.ExistServiceUtils;
 import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
 import edu.mayo.cts2.framework.service.command.restriction.MapVersionQueryServiceRestrictions;
 import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionQueryService;
 
 @Component
 public class ExistMapVersionQueryService 
 	extends AbstractExistQueryService
 		<MapVersion,
 		MapVersionDirectoryEntry,
 		MapVersionQueryServiceRestrictions,
 		edu.mayo.cts2.framework.model.service.mapversion.MapVersionQueryService,MapVersionDirectoryState>
 	implements MapVersionQueryService {
 
 	@Resource
 	private MapVersionResourceInfo mapVersionResourceInfo;
 	
 	@Override
 	public MapVersionDirectoryEntry doTransform(MapVersion resource,
 			MapVersionDirectoryEntry summary, org.xmldb.api.base.Resource eXistResource) {
 		summary = this.baseTransformResourceVersion(summary, resource);
 		
 		summary.setMapVersionName(resource.getMapVersionName());
 		summary.setVersionOf(resource.getVersionOf());
 		summary.setVersionTag(resource.getVersionTag());
 		
 		summary.setHref(getUrlConstructor().createMapVersionUrl(
				resource.getMapVersionName(),
 				resource.getMapVersionName()));
 
 		return summary;
 	}
 
 	@Override
 	protected MapVersionDirectoryEntry createSummary() {
 		return new MapVersionDirectoryEntry();
 	}
 
 	private class MapVersionDirectoryBuilder extends
 			XpathDirectoryBuilder<MapVersionDirectoryState,MapVersionDirectoryEntry> {
 
 		public MapVersionDirectoryBuilder(final String changeSetUri) {
 			super(new MapVersionDirectoryState(),
 					new Callback<MapVersionDirectoryState, MapVersionDirectoryEntry>() {
 
 				@Override
 				public DirectoryResult<MapVersionDirectoryEntry> execute(
 						MapVersionDirectoryState state, 
 						int start, 
 						int maxResults) {
 					return getResourceSummaries(
 							getResourceInfo(),
 							changeSetUri,
 							ExistServiceUtils.createPath(state.getMap()), 
 							state.getXpath(), 
 							start,
 							maxResults);
 				}
 
 				@Override
 				public int executeCount(MapVersionDirectoryState state) {
 					throw new UnsupportedOperationException();
 				}
 			},
 
 			getSupportedMatchAlgorithms(),
 			getSupportedModelAttributes());
 		}
 	}
 
 	@Override
 	public DirectoryResult<MapVersionDirectoryEntry> getResourceSummaries(
 			Query query, 
 			Set<ResolvedFilter> filterComponent,
 			MapVersionQueryServiceRestrictions restrictions, 
 			ResolvedReadContext readContext,
 			Page page) {
 		MapVersionDirectoryBuilder builder =
 				new MapVersionDirectoryBuilder(this.getChangeSetUri(readContext));
 
 		return builder.addMaxToReturn(page.getEnd()).
 				addStart(page.getStart()).
 				restrict(filterComponent).
 				restrict(query).
 				resolve();
 	}
 
 	@Override
 	public DirectoryResult<MapVersion> getResourceList(
 			Query query,
 			Set<ResolvedFilter> filterComponent,
 			MapVersionQueryServiceRestrictions restrictions, 
 			ResolvedReadContext readContext,
 			Page page) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int count(
 			Query query, 
 			Set<ResolvedFilter> filterComponent,
 			MapVersionQueryServiceRestrictions restrictions) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	protected DefaultResourceInfo<MapVersion, ?> getResourceInfo() {
 		return this.mapVersionResourceInfo;
 	}
 
 
 	@Override
 	public Set<? extends PredicateReference> getSupportedProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public DirectoryResult<EntityDirectoryEntry> mapVersionEntities(
 			NameOrURI mapVersion, MapRole mapRole, MapStatus mapStatus,
 			Query query, Set<ResolvedFilter> filterComponent,
 			EntityDescriptionQueryServiceRestrictions restrictions,
 			ResolvedReadContext readContext) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public DirectoryResult<EntityDescription> mapVersionEntityList(
 			NameOrURI mapVersion, MapRole mapRole, MapStatus mapStatus,
 			Query query, Set<ResolvedFilter> filterComponent,
 			EntityDescriptionQueryServiceRestrictions restrictions,
 			ResolvedReadContext readContext) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public EntityReferenceList mapVersionEntityReferences(NameOrURI mapVersion,
 			MapRole mapRole, MapStatus mapStatus, Query query,
 			Set<ResolvedFilter> filterComponent,
 			EntityDescriptionQueryServiceRestrictions restrictions,
 			ResolvedReadContext readContext) {
 		throw new UnsupportedOperationException();
 	}
 
 }
