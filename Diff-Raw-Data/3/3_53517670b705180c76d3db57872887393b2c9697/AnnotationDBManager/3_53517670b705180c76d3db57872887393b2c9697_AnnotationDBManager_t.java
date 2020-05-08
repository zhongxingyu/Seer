 package org.bioinfo.infrared.funcannot.dbsql;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bioinfo.commons.utils.ListUtils;
 import org.bioinfo.db.api.PreparedQuery;
 import org.bioinfo.db.api.Query;
 import org.bioinfo.db.handler.BeanArrayListHandler;
 import org.bioinfo.db.handler.MatrixHandler;
 import org.bioinfo.db.handler.ScalarArrayListHandler;
 import org.bioinfo.infrared.common.dbsql.DBConnector;
 import org.bioinfo.infrared.common.dbsql.DBManager;
 import org.bioinfo.infrared.common.feature.FeatureList;
 import org.bioinfo.infrared.funcannot.AnnotationItem;
 import org.bioinfo.infrared.funcannot.AnnotationUtils;
 import org.bioinfo.infrared.funcannot.filter.BiocartaFilter;
 import org.bioinfo.infrared.funcannot.filter.FunctionalFilter;
 import org.bioinfo.infrared.funcannot.filter.GOFilter;
 import org.bioinfo.infrared.funcannot.filter.GOSlimFilter;
 import org.bioinfo.infrared.funcannot.filter.InterproFilter;
 import org.bioinfo.infrared.funcannot.filter.JasparFilter;
 import org.bioinfo.infrared.funcannot.filter.KeggFilter;
 import org.bioinfo.infrared.funcannot.filter.MiRnaTargetFilter;
 import org.bioinfo.infrared.funcannot.filter.OregannoFilter;
 import org.bioinfo.infrared.funcannot.filter.ReactomeFilter;
 
 public class AnnotationDBManager extends DBManager {
 	
 	public static final String GET_GO_ANNOTATION_CONSTRAINT_FILTERED = "SELECT gpr.gene_stable_id, gpr.acc FROM go_info gi, go_propagated gpr WHERE gpr.acc=gi.acc ";
 	public static final String GET_GO_ANNOTATION_CONSTRAINT_BY_IDS = "select gi.* from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, go_info gi, dbname db where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and db.dbname='go' and x2.dbname_id=db.dbname_id and x2.display_id=gi.acc ";
 	public static final String GET_GO_ANNOTATION_CONSTRAINT_BY_IDS_PROPAGATED = "select gi.* from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, go_info gi, go_parent gp, dbname db where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and db.dbname='go' and x2.dbname_id=db.dbname_id and x2.display_id=gp.child_acc and gp.parent_acc=gi.acc ";
 	public static final String GET_KEGG_ANNOTATION_BY_IDS =  "select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='kegg' ";
 	
 	public static final String GET_GENERIC_ANNOTATION_BY_IDS = "select x2.display_id from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname=";
 	public static final String NESTED_SELECT= "select count(distinct(t.gene_id)) from xref x, transcript2xref tx, transcript t where x.display_id=x2.display_id and x.xref_id=tx.xref_id and x.dbname_id=db.dbname_id and tx.transcript_id=t.transcript_id";
 	
 	public static final String GET_KEGG_ANNOTATION_NAMES = "SELECT k.pathway_id, k.name FROM xref x, transcript t, gene g, transcript2xref tx, kegg_info k, dbname db  WHERE db.dbname = 'kegg' and db.dbname_id=x.dbname_id and x.xref_id=tx.xref_id and tx.transcript_id=t.transcript_id and t.gene_id=g.gene_id and x.display_id=k.pathway_id group by g.stable_id, x.display_id";
 	public static final String GET_GENERIC_ANNOTATION_TERM_NAMES = "select x.display_id, x.description from xref x, dbname db  WHERE db.dbname_id=x.dbname_id and db.dbname =";
 
 //	public static final String GET_GO_ANNOTATION = "SELECT gpr.gene_stable_id, gpr.acc FROM go_propagated gpr ";
 //	public static final String GET_GO_ANNOTATION_BY_NAMESPACE = "SELECT gpr.gene_stable_id, gpr.acc FROM go_info gi, go_propagated gpr WHERE gpr.acc=gi.acc and gi.namespace = ? group by gpr.gene_stable_id, gpr.acc";
 //	public static final String GET_GO_ANNOTATION_CONSTRAINT = "SELECT gpr.gene_stable_id, gpr.acc FROM go_info gi, go_propagated gpr WHERE gpr.acc=gi.acc and gi.namespace = ? ";
 //	public static final String GET_GENERIC_ANNOTATION_GENOME = "SELECT g.stable_id, x.display_id, x.description FROM xref x, transcript t, gene g, transcript2xref tx, dbname db  WHERE db.dbname_id=x.dbname_id and x.xref_id=tx.xref_id and tx.transcript_id=t.transcript_id and t.gene_id=g.gene_id group by g.stable_id, x.display_id AND db.dbname = ";
 //	public static final String GET_KEGG_ANNOTATION = "SELECT g.stable_id, k.pathway_id, k.name, k.description FROM xref x, transcript t, gene g, transcript2xref tx, kegg_info k, dbname db  WHERE db.dbname = 'kegg' and db.dbname_id=x.dbname_id and x.xref_id=tx.xref_id and tx.transcript_id=t.transcript_id and t.gene_id=g.gene_id and x.display_id=k.pathway_id group by g.stable_id, x.display_id";
 //	public static final String GET_INTERPRO_ANNOTATION_BY_IDS = "select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='interpro' ";
 //	public static final String GET_REACTOME_ANNOTATION_BY_IDS = "select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='reactome' ";
 //	public static final String GET_BIOCARTA_ANNOTATION_BY_IDS = "select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='biocarta' ";
 //	public static final String GET_JASPAR_ANNOTATION_BY_IDS = "select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='jaspar' ";
 //	public static final String GET_OREGANNO_ANNOTATION_BY_IDS = "select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = ? and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='oreganno' ";
 //	select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = 'ENST00000418975' and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='interpro' and (select  count(tx.transcript_id) from xref x, transcript2xref tx where x.dbname_id=46 and x.display_id=x2.display_id and x.xref_id=tx.xref_id group by x.xref_id) between 29 and 50 limit 5;
 //	select x2.display_id, x2.description from transcript2xref tx1, transcript2xref tx2, xref x1, xref x2, dbname db  where x1.display_id = 'ENST00000418975' and x1.xref_id=tx1.xref_id and tx1.transcript_id=tx2.transcript_id and tx2.xref_id=x2.xref_id and x2.dbname_id=db.dbname_id and db.dbname='interpro' and (select  count(tx.transcript_id) from xref x, transcript2xref tx where x.display_id=x2.display_id and x.xref_id=tx.xref_id and x.dbname_id=db.dbname_id group by x.xref_id) between 29 and 50 group by x2.display_id limit 5;
 
 	public AnnotationDBManager(DBConnector dBConnector) {
 		super(dBConnector);
 	}
 	
 //	@SuppressWarnings("unchecked")
 //	public AnnotationList getGOAnnotation() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 //		return new AnnotationList((List<AnnotationItem>)getFeatureList(GET_GO_ANNOTATION, new BeanArrayListHandler(AnnotationItem.class)).getElements());
 //	}
 //	@SuppressWarnings("unchecked")
 //	public AnnotationList getGOAnnotation(String namespace) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 //		return new AnnotationList((List<AnnotationItem>)getFeatureList(GET_GO_ANNOTATION_BY_NAMESPACE, namespace, new BeanArrayListHandler(AnnotationItem.class)).getElements());
 //	}
 
 	public FeatureList<AnnotationItem> getAnnotation(List<String> ids, FunctionalFilter functionalFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		if(functionalFilter instanceof GOFilter) {
 			return getGOAnnotation(ids, (GOFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof GOSlimFilter) {
 			return getGOSlimAnnotation(ids, (GOSlimFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof InterproFilter) {
 			return getInterproAnnotation(ids, (InterproFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof KeggFilter) {
 			return getKeggAnnotation(ids, (KeggFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof ReactomeFilter) {
 			return getReactomeAnnotation(ids, (ReactomeFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof BiocartaFilter) {
 			return getBiocartaAnnotation(ids, (BiocartaFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof MiRnaTargetFilter) {
 			return getMiRnaTargetAnnotation(ids, (MiRnaTargetFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof JasparFilter) {
 			return getJasparAnnotation(ids, (JasparFilter)functionalFilter);
 		}
 		if(functionalFilter instanceof OregannoFilter) {
 			return getOregannoAnnotation(ids, (OregannoFilter)functionalFilter);
 		}
 		return null;
 	}
 	
 	@Deprecated
 	public FeatureList<AnnotationItem> getGOAnnotation() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getGOAnnotation(new GOFilter("biological_process"));
 	}
 	
 	public FeatureList<AnnotationItem> getGOAnnotation(List<String> ids) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getGOAnnotation(ids, new GOFilter("biological_process"));
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public FeatureList<AnnotationItem> getGOAnnotation(GOFilter goFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 //		return new AnnotationList((List<AnnotationItem>)getFeatureList(GET_GO_ANNOTATION_CONSTRAINT_FILTERED + goFilter.getWhereClause("gi.")+" group by gpr.gene_stable_id, gpr.acc", new BeanArrayListHandler(AnnotationItem.class)).getElements());
 		return getFeatureList(GET_GO_ANNOTATION_CONSTRAINT_FILTERED + " and "+ goFilter.getSQLWhereClause("gi.")+" group by gpr.gene_stable_id, gpr.acc", new BeanArrayListHandler(AnnotationItem.class));
 	}
 	
 	public FeatureList<AnnotationItem> getGOAnnotation(List<String> ids, GOFilter goFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException{
 		goFilter.setGenomicNumberOfGenes(false);
 		
 		String sqlQuery;
 		if(goFilter.isPropagated()) {
 			sqlQuery = GET_GO_ANNOTATION_CONSTRAINT_BY_IDS_PROPAGATED + " and "+ goFilter.getSQLWhereClause("gi.")+" group by gi.acc";
 		}else {
 			sqlQuery = GET_GO_ANNOTATION_CONSTRAINT_BY_IDS + " and "+ goFilter.getSQLWhereClause("gi.")+" group by gi.acc";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 
 		if(goFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, goFilter.getMinNumberGenes(), goFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public FeatureList<AnnotationItem> getGOSlimAnnotation(List<String> ids, GOSlimFilter goSlimFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(goSlimFilter.isGenomicNumberOfGenes()) {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'goslim_goa_accession' and ("+NESTED_SELECT+") between " + goSlimFilter.getMinNumberGenes() + " and " + goSlimFilter.getMaxNumberGenes() + " group by x2.display_id";
 		}else {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'goslim_goa_accession' group by x2.display_id";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(goSlimFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, goSlimFilter.getMinNumberGenes(), goSlimFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public FeatureList<AnnotationItem> getInterproAnnotation(List<String> ids, InterproFilter interproFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(interproFilter.isGenomicNumberOfGenes()) {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'interpro' and ("+NESTED_SELECT+") between " + interproFilter.getMinNumberGenes() + " and " + interproFilter.getMaxNumberGenes() + " group by x2.display_id";
 		}else {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'interpro' group by x2.display_id";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(interproFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, interproFilter.getMinNumberGenes(), interproFilter.getMaxNumberGenes());
 		}
 	}
 	
 //	@SuppressWarnings("unchecked")
 //	public FeatureList<AnnotationItem> getKeggAnnotation(KeggFilter keggFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 ////		return new AnnotationList((List<AnnotationItem>)getFeatureList(GET_KEGG_ANNOTATION, new BeanArrayListHandler(AnnotationItem.class)).getElements());
 //		return getFeatureList(GET_KEGG_ANNOTATION, new BeanArrayListHandler(AnnotationItem.class));
 //	}
 	public FeatureList<AnnotationItem> getKeggAnnotation(List<String> ids, KeggFilter keggFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		keggFilter.setGenomicNumberOfGenes(false);
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, GET_KEGG_ANNOTATION_BY_IDS);
 
 		return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, keggFilter.getMinNumberGenes(), keggFilter.getMaxNumberGenes());
 	}
 	
 	public FeatureList<AnnotationItem> getReactomeAnnotation(List<String> ids, ReactomeFilter reactomeFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(reactomeFilter.isGenomicNumberOfGenes()) {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'reactome' and ("+NESTED_SELECT+") between " + reactomeFilter.getMinNumberGenes() + " and " + reactomeFilter.getMaxNumberGenes() + " group by x2.display_id";
 		}else {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'reactome' group by x2.display_id";
 		}
 	
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(reactomeFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, reactomeFilter.getMinNumberGenes(), reactomeFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public FeatureList<AnnotationItem> getBiocartaAnnotation(List<String> ids, BiocartaFilter biocartaFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(biocartaFilter.isGenomicNumberOfGenes()) {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'biocarta' and ("+NESTED_SELECT+") between " + biocartaFilter.getMinNumberGenes() + " and " + biocartaFilter.getMaxNumberGenes() + " group by x2.display_id";
 		}else {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'biocarta' group by x2.display_id";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(biocartaFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, biocartaFilter.getMinNumberGenes(), biocartaFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public FeatureList<AnnotationItem> getMiRnaTargetAnnotation(List<String> ids, MiRnaTargetFilter mirnaTargetFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(mirnaTargetFilter.isGenomicNumberOfGenes()) {
 			sqlQuery = "select mt.mirna_id from xref x, transcript2xref tx, mirna_target mt, transcript t, gene g where x.display_id= ? and x.xref_id=tx.xref_id and tx.transcript_id= mt.transcript_id and tx.transcript_id=t.transcript_id and t.gene_id=g.gene_id and (select count(distinct(tn.gene_id)) from mirna_target mtn, transcript tn where mtn.mirna_id=mt.mirna_id and mtn.transcript_id=tn.transcript_id) between " + mirnaTargetFilter.getMinNumberGenes() + " and " + mirnaTargetFilter.getMaxNumberGenes() + " group by mt.mirna_id;";
 		}else {
 			sqlQuery = "select mt.mirna_id from xref x, transcript2xref tx, mirna_target mt, transcript t, gene g where x.display_id= ? and x.xref_id=tx.xref_id and tx.transcript_id= mt.transcript_id and tx.transcript_id=t.transcript_id and t.gene_id=g.gene_id group by mt.mirna_id;";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(mirnaTargetFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, mirnaTargetFilter.getMinNumberGenes(), mirnaTargetFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public FeatureList<AnnotationItem> getJasparAnnotation(List<String> ids, JasparFilter jasparFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(jasparFilter.isGenomicNumberOfGenes()) {
 //			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'jaspar' and ("+NESTED_SELECT+") between " + jasparFilter.getMinNumberGenes() + " and " + jasparFilter.getMaxNumberGenes() + " group by x2.display_id";
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'jaspar' and (select count(distinct(jt.gene_id)) from jaspar_tfbs jt, transcript t, gene g where tx2.transcript_id=t.transcript_id and t.gene_id=jt.gene_id group by tf_factor_name) between " + jasparFilter.getMinNumberGenes() + " and " + jasparFilter.getMaxNumberGenes() + " group by x2.display_id";
 		}else {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'jaspar' group by x2.display_id";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(jasparFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, jasparFilter.getMinNumberGenes(), jasparFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public FeatureList<AnnotationItem> getOregannoAnnotation(List<String> ids, OregannoFilter oregannoFilter) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		String sqlQuery;
 		if(oregannoFilter.isGenomicNumberOfGenes()) {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'oreganno' and ("+NESTED_SELECT+") between " + oregannoFilter.getMinNumberGenes() + " and " + oregannoFilter.getMaxNumberGenes() + " group by x2.display_id";
 		}else {
 			sqlQuery = GET_GENERIC_ANNOTATION_BY_IDS+"'oreganno' group by x2.display_id";
 		}
 		
 		ids = ListUtils.unique(ids);
 		FeatureList<AnnotationItem> al = getFeatureListOfAnnotationItems(ids, sqlQuery);
 		
 		if(oregannoFilter.isGenomicNumberOfGenes()) {
 			return al;
 		}else {
 			return AnnotationUtils.filterByNumberOfAnnotationsPerId(al, oregannoFilter.getMinNumberGenes(), oregannoFilter.getMaxNumberGenes());
 		}
 	}
 	
 	public Map<String, String> getAnnotationTermNames(String dbname) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		Map<String, String> annotationTermsSize = new HashMap<String, String>(15000);
 		Query query = null;
 		Object[][] matrix = null;
 		String sqlQuery = "";
		if(dbname.equalsIgnoreCase("go-slim") || dbname.equalsIgnoreCase("goslim")) {
			dbname = "goslim_goa_accession";
		}
 		if(dbname.equalsIgnoreCase("go")) {
 			sqlQuery = "select acc, name from go_info";
 		}else {
 			if(dbname.equalsIgnoreCase("kegg")) {
 				sqlQuery = "select k.pathway_id, k.name from xref x, kegg_info k, dbname db  WHERE db.dbname = 'kegg' and db.dbname_id=x.dbname_id and x.display_id=k.pathway_id group by k.pathway_id;";	//GET_GENERIC_ANNOTATION_TERM_NAMES+"'"+dbname+"';";
 			}else {
 				sqlQuery = GET_GENERIC_ANNOTATION_TERM_NAMES+"'"+dbname+"';";
 			}
 		}
 		query = getDBConnector().getDbConnection().createSQLQuery(sqlQuery);
 		matrix = (Object[][])query.execute(sqlQuery, new MatrixHandler());
 		if(matrix != null) {
 			for(int row=0; row<matrix.length; row++) {
 				annotationTermsSize.put((String)matrix[row][0], (String)matrix[row][1]);
 			}
 		}
 		query.close();
 		return annotationTermsSize;
 	}
 	
 	public Map<String, Integer> getAnnotationTermsSize(String dbname) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		Map<String, Integer> annotationTermsSize = new HashMap<String, Integer>(15000);
 		Query query = null;
 		Object[][] matrix = null;
 		String sqlQuery = "";
 		if(dbname.equalsIgnoreCase("go-slim") || dbname.equalsIgnoreCase("goslim")) {
 			dbname = "goslim_goa_accession";
 		}
 		if(dbname.equalsIgnoreCase("go")) {
 			sqlQuery = "select acc, propagated_number_genes from go_info;";
 		}else {
 			if(dbname.equalsIgnoreCase("jaspar")) {	// performance improvement
 				sqlQuery = "select tf_factor_name, count(distinct(gene_id)) from jaspar_tfbs group by tf_factor_name;";
 			}else {
 				if(dbname.equalsIgnoreCase("mirna")) {
 					sqlQuery = "select mt.mirna_id, count(distinct(t.gene_id))  from mirna_target mt, transcript t where mt.transcript_id=t.transcript_id group by mt.mirna_id;";
 				}else {
 					sqlQuery = "select x.display_id, count(distinct(g.gene_id)) from xref x, transcript2xref tx, transcript t, gene g, dbname db where db.dbname='" + dbname + "' and db.dbname_id=x.dbname_id and x.xref_id=tx.xref_id and tx.transcript_id=t.transcript_id and t.gene_id=g.gene_id group by x.display_id;";
 				}
 			}
 		}
 		query = getDBConnector().getDbConnection().createSQLQuery(sqlQuery);
 		matrix = (Object[][])query.execute(sqlQuery, new MatrixHandler());
 		if(matrix != null) {
 			for(int row=0; row<matrix.length; row++) {
 				annotationTermsSize.put((String)matrix[row][0], Integer.parseInt(""+matrix[row][1]));
 			}
 		}
 		query.close();
 		return annotationTermsSize;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	private FeatureList<AnnotationItem> getFeatureListOfAnnotationItems(List<String> ids, String sqlQuery) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		getDBConnector().getDbConnection().setAutoConnectAndDisconnect(false);
 		getDBConnector().getDbConnection().connect();
 		
 		FeatureList<AnnotationItem> al = new FeatureList<AnnotationItem>(ids.size());
 		PreparedQuery prepQuery = getDBConnector().getDbConnection().createSQLPrepQuery(sqlQuery);
 		for(String id: ids) {
 			prepQuery.setParams(id);
 			List<String> list = (List<String>) prepQuery.execute(new ScalarArrayListHandler());
 			if(list != null) {
 				for(String s: list) {
 					if(s != null)  {
 						al.add(new AnnotationItem(id, s));
 					}
 				}
 			}
 		}
 		
 		getDBConnector().getDbConnection().disconnect();
 		getDBConnector().getDbConnection().setAutoConnectAndDisconnect(true);
 		
 		return al;
 	}
 }
 
