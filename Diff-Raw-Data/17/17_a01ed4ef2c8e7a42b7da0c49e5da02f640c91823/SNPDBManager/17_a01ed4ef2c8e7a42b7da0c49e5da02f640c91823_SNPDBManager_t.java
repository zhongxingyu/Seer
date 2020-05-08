 package org.bioinfo.infrared.variation.dbsql;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.bioinfo.db.handler.BeanArrayListHandler;
 import org.bioinfo.db.handler.BeanHandler;
 import org.bioinfo.infrared.common.dbsql.DBConnector;
 import org.bioinfo.infrared.common.dbsql.DBManager;
 import org.bioinfo.infrared.common.feature.FeatureList;
 import org.bioinfo.infrared.variation.SNP;
 
 public class SNPDBManager extends DBManager {
 
 	public static final String SELECT_FIELDS = " s.name, s.chromosome, s.start, s.end, s.strand, s.allele_string, s.map_weight, s.consequence_type, s.transcript_consequence_type, s.sequence ";
 
 	public static final String GET_ALL_SNP_IDS = "SELECT s.name FROM snp s";
 	public static final String GET_ALL_SNPS = "SELECT " + SELECT_FIELDS + " FROM snp s";
 	public static final String GET_ALL_SNPS_WITH_CONSEQUENCE_IN_TRANSCRIPTS = "SELECT * FROM snp s, snp2transcripts ";
	public static final String GET_SNP_BY_NAME = "SELECT " + SELECT_FIELDS + " FROM snp s WHERE s.name = ? group by s.name";
 	public static final String GET_SNPS_BY_EXTERNAL_ID = "select "+SELECT_FIELDS+" from xref x, transcript2xref tx, snp2transcript st, snp s where x.display_id= ? and x.xref_id=tx.xref_id and tx.transcript_id=st.transcript_id and st.snp_id=s.snp_id group by s.snp_id";
 	public static final String GET_SNPS_BY_CONSEQUENCE_TYPE = "select "+SELECT_FIELDS+" from snp2transcript st, snp s, consequence_type cq where cq.consequence_type_name= ? and cq.consequence_type_id=st.consequence_type_id and st.snp_id=s.snp_id group by s.name";
 	public static final String GET_SNPS_FILTERED_BY_CONSEQUENCE_TYPE= "select "+SELECT_FIELDS+" from snp2transcript st, snp s, consequence_type cq where s.name = ? AND cq.consequence_type_id=st.consequence_type_id and st.snp_id=s.snp_id AND cq.consequence_type_name = '";
 	
 	// OLD select to delete
 //	private static final String SELECT_FIELDS_OLD = " s.name, s.chromosome, s.start, s.end, s.strand, s.allele_string ";
 //	public static final String GET_SNPS_BY_EXTERNAL_ID_OLD = "select s.* from xref x, transcript2xref tx, snp2transcript st, snp s where x.display_id= ? and x.xref_id=tx.xref_id and tx.transcript_id=st.transcript_id and st.snp_id=s.snp_id group by s.snp_id";
 //	public static final String GET_SNPS_BY_CONSEQUENCE_TYPE_OLD = "select s.* from snp2transcript st, snp s, consequence_type cq where cq.consequence_type_name= ? and cq.consequence_type_id=st.consequence_type_id and st.snp_id=s.snp_id group by s.name";
 	
 	public SNPDBManager(DBConnector dBConnector) {
 		super(dBConnector);
 	}
 	
 	public List<String> getAllNames() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getStringList(GET_ALL_SNP_IDS);
 	}
 
 	public List<String> getAllNamesByRegion(String chromosome) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getStringList(GET_ALL_SNP_IDS + " where s.chromosome = '"+chromosome+"'");
 	}
 	
 	public List<String> getAllNamesByRegion(String chromosome, int start, int end) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getStringList(GET_ALL_SNP_IDS + " where s.chromosome = '"+chromosome+"' and s.start > "+start +" and s.end < "+end+" ");
 	}
 	
 	public List<String> getAllNamesByConsequenceType(String consequence) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getStringList(GET_ALL_SNP_IDS + " where s.consequence_type = '"+ consequence +"' ");
 	}	
 	
 	public List<String> getAllConsequenceTypes() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		return getStringList("select consequence_type_name from consequence_type");
 	}
 	
 	@Deprecated
 	public List<String> getAllNamesByLocation(String chromosome, int start, int end) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getStringList(GET_ALL_SNP_IDS + " where s.chromosome = '"+chromosome+"' and s.start > "+start +" and s.end < "+end+" ");
 	}
 
 
 	
 	public SNP getByName(String snpId) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return (SNP)getFeatureById(GET_SNP_BY_NAME, snpId, new BeanHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getByName(List<String> snpIds) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
		return getFeatureListByIds(GET_SNP_BY_NAME, snpIds, new BeanHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public FeatureList<SNP> getSNPListByIds(List<String> snpIds) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureListByIds("SELECT s.* FROM snp s WHERE s.name = ? ", snpIds, new BeanHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getAll() throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList(GET_ALL_SNPS, new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getAllByRegion(String chromosome) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList(GET_ALL_SNPS  + " where s.chromosome = '"+chromosome+"' ", new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getAllByRegion(String chromosome, int start, int end) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList(GET_ALL_SNPS  + " where s.chromosome = '"+chromosome+"' and s.start > "+start +" and s.end < "+end+" ", new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getAllByPosition(String chromosome, int position) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList(GET_ALL_SNPS  + " where s.chromosome = '"+chromosome+"' and s.start = "+position, new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public FeatureList<SNP> getAllByLocation(String chromosome, int start, int end) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureList(GET_ALL_SNPS  + " where s.chromosome = '"+chromosome+"' and s.start > "+start +" and s.end < "+end+" ", new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getAllByConsequenceType(String consequence) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureListById(GET_SNPS_BY_CONSEQUENCE_TYPE, consequence, new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getAllFilteredByConsequenceType(List<String> snpIds, String consequence) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		FeatureList<SNP> f = getFeatureListByIds(GET_SNPS_FILTERED_BY_CONSEQUENCE_TYPE+consequence+"' group by s.name", snpIds, new BeanHandler(SNP.class));
 //		f.removeNullElements();
 		return f;
 	}
 	
 	public FeatureList<SNP> getAllFilteredByConsequenceType(List<String> snpIds, List<String> consequenceTypes) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		FeatureList<SNP> snpFeatureList = new FeatureList<SNP>();
 		for(String consequenceType: consequenceTypes) {
 			snpFeatureList.addAll(getAllFilteredByConsequenceType(snpIds, consequenceType));
 		}
 		return snpFeatureList;
 	}
 	
 	public void writeAllFilteredByConsequenceType(String consequence, String outfile) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException, IOException {
 		writeFeatureList("select "+SELECT_FIELDS+" from snp2transcript st, snp s, consequence_type cq where cq.consequence_type_name='"+consequence+"' and cq.consequence_type_id=st.consequence_type_id and st.snp_id=s.snp_id group by s.name", new File(outfile));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public FeatureList<SNP> getByExternalId(String externalId) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getFeatureListById(GET_SNPS_BY_EXTERNAL_ID, externalId, new BeanArrayListHandler(SNP.class));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<FeatureList<SNP>> getByExternalId(List<String> externalIds) throws SQLException, IllegalAccessException, ClassNotFoundException, InstantiationException {
 		return getListOfFeatureListByIds(GET_SNPS_BY_EXTERNAL_ID, externalIds, new BeanArrayListHandler(SNP.class));
 	}
 
 }
