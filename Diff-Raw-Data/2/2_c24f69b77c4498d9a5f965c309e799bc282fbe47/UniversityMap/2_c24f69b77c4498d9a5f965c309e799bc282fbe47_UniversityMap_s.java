 package map;
 
 import java.io.IOException;
 
 import marker.EdgeMarker;
 import marker.NamedMarker;
 import marker.ProxyMarker;
 
 import log.LogManager;
 import log.Logger;
 
 import processing.core.PApplet;
 import rdf.RDFModel;
 import util.StringUtil;
 import util.location.LocationCache;
 import util.location.OrganizationLocationCache;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 
 import de.fhpotsdam.unfolding.geo.Location;
 
 public class UniversityMap extends AbstractLAKMap {
 
 	private static final Logger logger = LogManager.getLogger(UniversityMap.class);
 
 	private static final long serialVersionUID = -7231594744155656041L;
 	
	private LocationCache locationCache;
	
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "map.UniversityMap" });
 	}
 
 	@Override
 	protected LocationCache createLocationCache() {
 		try {
 			return new OrganizationLocationCache("data/organisations.csv");
 		} catch (IOException e) {
 			logger.fatal("Organization location cache file produced IO Error");
 			logger.catching(e);
 			
 			exit();
 			return null;
 		}
 	}
 
 	@Override
 	protected void addAllNodeMarkers() {
 		ResultSet rs = RDFModel.getAllOrganisations();
 
 		while (rs.hasNext()) {
 			QuerySolution sol = rs.next();
 			String orgname = StringUtil.getString(sol.getLiteral("orgname"));
 			
 			if (getNodeMarkerWithName(orgname) != null)
 				continue;
 			
 			Location loc = locationCache.get(orgname);
 			if (loc != null) {
 				NamedMarker m = new NamedMarker(orgname, loc);
 				nodeMarkerManager.addOriginalMarker(m);
 			}
 		}
 	}
 
 	@Override
 	protected void showNodeMarkersOf(String confAcronym) {
 		for (RDFNode node : RDFModel.getOrganisationsOfConference(confAcronym)) {
 			String orgName = StringUtil.getString(node.asResource().getProperty(FOAF.name));
 
 			for (ProxyMarker<NamedMarker> m : nodeMarkerManager) {
 				if (m.getOriginal().getName().equals(orgName))
 					m.setHidden(false);
 			}
 		}
 	}
 
 	/**
 	 * Adds all the line markers between the org markers.
 	 */
 	@Override
 	protected void addAllEdgeMarkers() {
 		ResultSet rs = RDFModel.getAllOrganisationPairsThatWroteAPaperTogether();
 
 		QuerySolution sol;
 		while (rs.hasNext()) {
 			sol = rs.next();
 			if (!isValidSolutionForMarker(sol))
 				continue;
 			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
 			String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
 			int coopCount = sol.getLiteral("coopCount").getInt();
 			NamedMarker start = getNodeMarkerWithName(orgName);
 			NamedMarker end = getNodeMarkerWithName(otherOrgName);
 			if (start == null || end == null)
 				continue;
 			EdgeMarker<NamedMarker> m = new EdgeMarker<>(start, end);
 			m.setColor(0x50505050);
 			m.setHighlightColor(0xFFFF0000);
 			m.setStrokeWeight(coopCount);
 			edgeMarkerManager.addOriginalMarker(m);
 			logger.debug("Common papers for %s to %s:%d", orgName, otherOrgName, coopCount);
 		}
 	}
 
 	/**
 	 * Tests whether the given querySolution can be used to create a valid
 	 * marker.
 	 * 
 	 * @param solution
 	 * @return
 	 */
 	private boolean isValidSolutionForMarker(QuerySolution solution) {
 		if (solution.getLiteral("orgName") == null || solution.getLiteral("otherOrgName") == null)
 			return false;
 		return true;
 	}
 
 	@Override
 	protected void addEdgeMarkersOf(String confAcronym) {
 		ResultSet rs = RDFModel.getAllOrganisationPairsThatWroteAPaperTogetherFromGivenConference(confAcronym);
 		// ResultSetFormatter.out(rs);
 		QuerySolution sol;
 		while (rs.hasNext()) {
 			sol = rs.next();
 			if (!isValidSolutionForMarker(sol))
 				continue;
 			String orgName = StringUtil.getString(sol.getLiteral("orgName"));
 			String otherOrgName = StringUtil.getString(sol.getLiteral("otherOrgName"));
 			int coopCount = sol.getLiteral("coopCount").getInt();
 			NamedMarker start = getNodeMarkerWithName(orgName);
 			NamedMarker end = getNodeMarkerWithName(otherOrgName);
 			if (start == null || end == null)
 				continue;
 			EdgeMarker<NamedMarker> m = new EdgeMarker<>(start, end);
 			m.setColor(0x50505050);
 			m.setHighlightColor(0xFFFF0000);
 			m.setStrokeWeight(coopCount);
 			edgeMarkerManager.addOriginalMarker(m);
 			logger.debug("Common papers for %s to %s:%d", orgName, otherOrgName, coopCount);
 		}
 	}
 }
