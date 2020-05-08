 package edu.mayo.cts2.framework.plugin.service.exist.profile.association;
 
 import edu.mayo.cts2.framework.core.util.EncodingUtils;
 import edu.mayo.cts2.framework.model.association.Association;
 import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
 import edu.mayo.cts2.framework.model.association.GraphNode;
 import edu.mayo.cts2.framework.model.association.types.GraphDirection;
 import edu.mayo.cts2.framework.model.association.types.GraphFocus;
 import edu.mayo.cts2.framework.model.command.Page;
 import edu.mayo.cts2.framework.model.core.*;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.AbstractExistQueryService;
 import edu.mayo.cts2.framework.plugin.service.exist.profile.PathInfo;
 import edu.mayo.cts2.framework.plugin.service.exist.restrict.directory.XpathDirectoryBuilder;
 import edu.mayo.cts2.framework.plugin.service.exist.restrict.directory.XpathDirectoryBuilder.XpathState;
 import edu.mayo.cts2.framework.plugin.service.exist.util.ExistServiceUtils;
 import edu.mayo.cts2.framework.service.command.restriction.AssociationQueryServiceRestrictions;
 import edu.mayo.cts2.framework.service.profile.association.AssociationQuery;
 import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
 import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.stereotype.Component;
 import org.springframework.util.Assert;
 import org.xmldb.api.base.XMLDBException;
 
 import javax.annotation.Resource;
 
 @Component
 public class ExistAssociationQueryService
 	extends AbstractExistQueryService
 		<Association,
 		AssociationDirectoryEntry,
 		AssociationQueryServiceRestrictions,
 		XpathState>
 	implements AssociationQueryService {
 	
 	@Resource
 	private AssociationResourceInfo associationResourceInfo;
 	
 	private class AssociationDirectoryBuilder extends XpathDirectoryBuilder<XpathState,AssociationDirectoryEntry> {
 
        private StateBuildingRestriction<XpathState> codeSystemVersionRestriction;

 		public AssociationDirectoryBuilder(final String changeSetUri) {
 			super(new XpathState(), new Callback<XpathState, AssociationDirectoryEntry>() {
 
 				@Override
 				public DirectoryResult<AssociationDirectoryEntry> execute(
 						XpathState state,
 						int start, 
 						int maxResults) {
 					return getResourceSummaries(
 							getResourceInfo(),
 							changeSetUri,
 							"",
 							state.getXpath(),
 							start, 
 							maxResults);
 				}
 
 				@Override
 				public int executeCount(XpathState state) {
 					throw new UnsupportedOperationException();
 				}},
 				
 				getSupportedMatchAlgorithms(),
 				getSupportedSearchReferences());
 		}
 
         public AssociationDirectoryBuilder restrict(final AssociationQueryServiceRestrictions restriction){
 
         	//TODO this implementation is incomplete - it is ignoring many possible restrictions, such as predicate, etc.
         	//https://github.com/cts2/exist-service/issues/16
             if(restriction != null &&
                     restriction.getSourceEntity() != null) {
 
                getRestrictions().remove(codeSystemVersionRestriction);

                 final ScopedEntityName name = restriction.getSourceEntity().getEntityName();
 
                 getRestrictions().add(new StateBuildingRestriction<XpathState>() {
                     @Override
                     public XpathState restrict(XpathState state) {
                         boolean isBlankState = StringUtils.isBlank(state.getXpath());
 
                         String namespaceXpath = "";
                         if(! StringUtils.isBlank(name.getNamespace())){
                             namespaceXpath = " and core:namespace = '" + name.getNamespace() + "'";
                         }
 
                         String clause = "association:subject[core:name = '" + name.getName() + "'" + namespaceXpath + "]";
 
                         if(isBlankState){
                             state.setXpath("[" + clause + "]");
                         } else {
                             state.setXpath(StringUtils.removeEnd(state.getXpath(), "]") + " and " + clause + "]");
                         }
 
                         return state;
                     }
                 });
             }
 
             if(restriction != null
                     && restriction.getTargetEntity() != null) {
 
                getRestrictions().remove(codeSystemVersionRestriction);

                 final ScopedEntityName name = restriction.getTargetEntity().getEntityName();
 
                 getRestrictions().add(new StateBuildingRestriction<XpathState>() {
                     @Override
                     public XpathState restrict(XpathState state) {
                         boolean isBlankState = StringUtils.isBlank(state.getXpath());
 
                         String namespaceXpath = "";
                         if(! StringUtils.isBlank(name.getNamespace())){
                             namespaceXpath = " and core:namespace = '" + name.getNamespace() + "'";
                         }
 
                         String clause = "association:target/core:entity[core:name = '" + name.getName() + "'" + namespaceXpath + "]";
 
                         if(isBlankState){
                             state.setXpath("[" + clause + "]");
                         } else {
                             state.setXpath(StringUtils.removeEnd(state.getXpath(), "]") + " and " + clause + "]");
                         }
 
                         return state;
                     }
                 });
             }
 
            if(getRestrictions().size() == 0
                    && restriction != null
                     && restriction.getCodeSystemVersion() != null) {
 
                 final String codeSystemVersionName = restriction.getCodeSystemVersion().getName();
                 final String codeSystemVersionUri = restriction.getCodeSystemVersion().getUri();
 
                codeSystemVersionRestriction = new StateBuildingRestriction<XpathState>() {
                     @Override
                     public XpathState restrict(XpathState state) {
                         boolean isBlankState = StringUtils.isBlank(state.getXpath());
 
                         String clause;
 
                         if(StringUtils.isNotBlank(codeSystemVersionName)){
                             clause = "association:assertedBy[core:version = '" + codeSystemVersionName + "']";
                         } else {
                             clause = "association:assertedBy/core:version[@uri = '" + codeSystemVersionUri + "']";
                         }
 
                         if(isBlankState){
                             state.setXpath("[" + clause + "]");
                         } else {
                             state.setXpath(StringUtils.removeEnd(state.getXpath(), "]") + " and " + clause + "]");
                         }
 
                         return state;
                     }
                };

                getRestrictions().add(codeSystemVersionRestriction);
             }
 
             return this;
         }
     }
 
 	@Override
 	public DirectoryResult<AssociationDirectoryEntry> getResourceSummaries(
 			AssociationQuery query,
 			SortCriteria sortCriteria,
 			Page page) {
 		AssociationDirectoryBuilder builder = new AssociationDirectoryBuilder(
 				this.getChangeSetUri(query.getReadContext()));
 
 		AssociationQueryServiceRestrictions restrictions = query.getRestrictions();
 
         builder =  builder.restrict(restrictions);
 		
 		return builder.restrict(
 				query.getFilterComponent()).
 				addStart(page.getStart()).
 				addMaxToReturn(page.getMaxToReturn()).
 				resolve();
 	}
 
 	@Override
 	protected PathInfo getResourceInfo() {
 		return this.associationResourceInfo;
 	}
 
 	@Override
 	protected AssociationDirectoryEntry createSummary() {
 		return new AssociationDirectoryEntry();
 	}
 
 	@Override
 	protected AssociationDirectoryEntry doTransform(
 			Association resource,
 			AssociationDirectoryEntry summary,
 			org.xmldb.api.base.Resource eXistResource) {
 		
 		CodeSystemVersionReference assertedIn = getAssertedIn(resource);
 		
 		Assert.notNull(assertedIn.getCodeSystem(), "association MUST have CodeSystem reference for 'assertedIn'.");
 		Assert.notNull(assertedIn.getVersion(), "association MUST have CodeSystemVersion reference for 'assertedIn'.");
 		
 		summary.setAssertedBy(resource.getAssertedBy());
 		summary.setSubject(this.setHref(resource.getSubject()));
 		summary.setPredicate(resource.getPredicate());
 		summary.setTarget(this.setHref(resource.getTarget(0)));
 
         String name;
         try {
             name = ExistServiceUtils.getNameFromResourceName(eXistResource.getId());
         } catch (XMLDBException e) {
             throw new IllegalStateException(e);
         }
 
         summary.setHref(
                 this.getUrlConstructor().createAssociationOfCodeSystemVersionUrl(
                         assertedIn.getCodeSystem().getContent(),
                         assertedIn.getVersion().getContent(),
                         name)
                 );
 		
 		return summary;
 	}
 
     protected StatementTarget setHref(StatementTarget target){
         if(target.getEntity() != null){
             URIAndEntityName name = target.getEntity();
             name.setHref(
                 this.getUrlConstructor().createEntityUrl(EncodingUtils.encodeScopedEntityName(name)));
         }
 
         return target;
     }
 
     protected URIAndEntityName setHref(URIAndEntityName name){
         name.setHref(
                 this.getUrlConstructor().createEntityUrl(EncodingUtils.encodeScopedEntityName(name)));
 
         return name;
     }
 
 	private static CodeSystemVersionReference getAssertedIn(Association resource){
 		if(resource.getAssertedIn() != null){
 			return resource.getAssertedIn();
 		} else {
 			return resource.getAssertedBy();
 		}
 	}
 
 	@Override
 	public DirectoryResult<GraphNode> getAssociationGraph(
 			GraphFocus focusType,
 			EntityDescriptionReadId focusEntity, 
 			GraphDirection direction,
 			long depth) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public DirectoryResult<Association> getResourceList(AssociationQuery query,
 			SortCriteria sortCriteria, Page page) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int count(AssociationQuery query) {
 		throw new UnsupportedOperationException();
 	}
 
 }
