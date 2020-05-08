 package org.genedb.crawl.elasticsearch.mappers;
 
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.index.query.xcontent.QueryBuilders;
 import org.genedb.crawl.CrawlException;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.model.OrganismProp;
 import org.gmod.cat.OrganismsMapper;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ElasticSearchOrganismsMapper extends ElasticSearchBaseMapper implements OrganismsMapper {
 	
 	private Logger logger = Logger.getLogger(ElasticSearchOrganismsMapper.class);
 	
 	@Override
	public List<Organism> list() throws CrawlException {
 		
 		SearchResponse response = connection.getClient().prepareSearch("organisms")
 			.setQuery(QueryBuilders.matchAllQuery())
 			.execute()
 			.actionGet();
 		
 		return this.getAllMatches(response, Organism.class);
 		
 	}
 
 	@Override
	public Organism getByID(int ID) throws CrawlException {
 		return (Organism) this.getFirstMatch("organisms", "ID", String.valueOf(ID), Organism.class);
 	}
 
 	@Override
	public Organism getByTaxonID(String taxonID) throws CrawlException {
 		return (Organism) this.getFirstMatch("organisms", "taxonID", taxonID, Organism.class);
 	}
 
 	@Override
	public Organism getByCommonName(String commonName) throws CrawlException {
 		return (Organism) this.getFirstMatch("organisms", "common_name", commonName, Organism.class);
 	}
 
 	@Override
 	public OrganismProp getOrganismProp(int ID, String cv, String cvterm) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public void createOrUpdate(Organism organism) {
 		
 		try {
 			String source = jsonIzer.toJson(organism);
 			connection.getClient().prepareIndex("organisms", "Organism", organism.common_name).setSource(source).execute().actionGet();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 		
 	}
 
 }
