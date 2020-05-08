 /**
  *
  */
 package org.ocha.hdx.persistence.dao.metadata;
 
 import java.util.List;
 
 import org.ocha.hdx.model.DataSerie;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.persistence.entity.i18n.Text;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata.MetadataName;
 
 /**
  * @author alexandru-m-g
  * 
  */
 public interface DataSerieMetadataDAO {
 	public List<DataSerieMetadata> listDataSerieMetadata();
 
 	public List<DataSerieMetadata> listDataSerieMetadataByIndicatorTypeIdAndSourceId(long indicatorTypeId, long sourceId);
 
 	public List<DataSerieMetadata> listDataSerieMetadataByIndicatorTypeCodeAndSourceCode(final DataSerie dataSerie);
 
 	public List<DataSerieMetadata> listDataSerieMetadataByIndicatorTypeCode(String indicatorTypeCode);
 
 	public List<DataSerieMetadata> listDataSerieDataValidatorsByIndicatorTypeCodeAndSourceCode(final DataSerie dataSerie);
 
 	public DataSerieMetadata createDataSerieMetadata(IndicatorType type, Source source, MetadataName key, Text value);
 
	/**
	 * creates an empty DATASET_SUMMARY to initialize a DataSerie
	 * 
	 * @param type
	 * @param source
	 * @return
	 */
	public DataSerieMetadata initializeDataSerie(IndicatorType type, Source source);

 	public void deleteDataSerieMetadata(long id);
 
 	public DataSerieMetadata updateDataSerieMetadata(long id, Text value);
 
 	public DataSerieMetadata updateDataSerieMetadata(String indicatorTypeCode, String sourceCode, String entryKey, String defaultValue);
 
 	public DataSerieMetadata getDataSerieMetadataById(long id);
 
 	public DataSerieMetadata getDataSerieMetadataByIndicatorTypeCodeAndSourceCodeAndEntryKey(final String indicatorTypeCode, final String sourceCode, final MetadataName entryKey);
 
 }
