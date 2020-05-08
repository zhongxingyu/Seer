 package org.ocha.hdx.persistence.dao.metadata;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.ocha.hdx.model.DataSerie;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.persistence.entity.i18n.Text;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata.MetadataName;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata.MetadataType;
 import org.springframework.transaction.annotation.Transactional;
 
 public class DataSerieMetadataDAOImpl implements DataSerieMetadataDAO {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@Override
 	public List<DataSerieMetadata> listDataSerieMetadata() {
 		final TypedQuery<DataSerieMetadata> query = em.createQuery("SELECT ad FROM DataSerieMetadata ad ORDER BY ad.id", DataSerieMetadata.class);
 		return query.getResultList();
 	}
 
 	@Override
 	public List<DataSerieMetadata> listDataSerieMetadataByIndicatorTypeIdAndSourceId(final long indicatorTypeId, final long sourceId) {
 		final TypedQuery<DataSerieMetadata> query = em
 				.createQuery("SELECT ad FROM DataSerieMetadata ad WHERE ad.indicatorType.id=:indicatorTypeId AND ad.source.id=:sourceId", DataSerieMetadata.class);
 		query.setParameter("indicatorTypeId", indicatorTypeId);
 		query.setParameter("sourceId", sourceId);
 		return query.getResultList();
 	}
 
 	@Override
 	public List<DataSerieMetadata> listDataSerieMetadataByIndicatorTypeCodeAndSourceCode(final DataSerie dataSerie) {
 		final String indicatorTypeCode = dataSerie.getIndicatorCode();
 		final String sourceCode = dataSerie.getSourceCode();
 		final TypedQuery<DataSerieMetadata> query = em.createQuery("SELECT ad FROM DataSerieMetadata ad WHERE ad.indicatorType.code=:indicatorTypeCode AND ad.source.code=:sourceCode",
 				DataSerieMetadata.class);
 		query.setParameter("indicatorTypeCode", indicatorTypeCode);
 		query.setParameter("sourceCode", sourceCode);
 		return query.getResultList();
 	}
 
 	@Override
 	public List<DataSerieMetadata> listDataSerieMetadataByIndicatorTypeCode(final String indicatorTypeCode) {
		final TypedQuery<DataSerieMetadata> query = em.createQuery("SELECT ad FROM DataSerieMetadata ad WHERE ad.indicatorType.code=:indicatorTypeCode",
				DataSerieMetadata.class);
 		query.setParameter("indicatorTypeCode", indicatorTypeCode);
 		return query.getResultList();
 	}
 
 	@Override
 	public List<DataSerieMetadata> listDataSerieDataValidatorsByIndicatorTypeCodeAndSourceCode(final DataSerie dataSerie) {
 		final String indicatorTypeCode = dataSerie.getIndicatorCode();
 		final String sourceCode = dataSerie.getSourceCode();
 		final TypedQuery<DataSerieMetadata> query = em.createQuery(
 				"SELECT ad FROM DataSerieMetadata ad WHERE ad.indicatorType.code=:indicatorTypeCode AND ad.source.code=:sourceCode AND ad.entryKey IN :validators", DataSerieMetadata.class);
 		query.setParameter("indicatorTypeCode", indicatorTypeCode);
 		query.setParameter("sourceCode", sourceCode);
 		query.setParameter("validators", MetadataName.getByType(MetadataType.VALIDATOR));
 		return query.getResultList();
 	}
 
 	@Override
 	@Transactional
 	public DataSerieMetadata createDataSerieMetadata(final IndicatorType type, final Source source, final MetadataName key, final Text value) {
 		final DataSerieMetadata dataSerieMetadata = new DataSerieMetadata(type, source, key, value);
 		em.persist(dataSerieMetadata);
 		return dataSerieMetadata;
 
 	}
 
 	@Override
 	@Transactional
 	public void deleteDataSerieMetadata(final long id) {
 		final DataSerieMetadata dataSerieMetadata = getDataSerieMetadataById(id);
 		em.remove(dataSerieMetadata);
 
 	}
 
 	@Override
 	public DataSerieMetadata getDataSerieMetadataById(final long id) {
 		return em.find(DataSerieMetadata.class, id);
 	}
 
 	@Override
 	@Transactional
 	public DataSerieMetadata updateDataSerieMetadata(final long id, final Text value) {
 		final DataSerieMetadata dataSerieMetadata = getDataSerieMetadataById(id);
 		dataSerieMetadata.setEntryValue(value);
 		return dataSerieMetadata;
 	}
 
 	@Override
 	@Transactional
 	public DataSerieMetadata updateDataSerieMetadata(final String indicatorTypeCode, final String sourceCode, final String entryKey, final String defaultValue) {
 		final DataSerieMetadata dataSerieMetadata = getDataSerieMetadataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, MetadataName.valueOf(entryKey));
 		dataSerieMetadata.getEntryValue().setDefaultValue(defaultValue);
 		return dataSerieMetadata;
 	}
 
 	@Override
 	public DataSerieMetadata getDataSerieMetadataByIndicatorTypeCodeAndSourceCodeAndEntryKey(final String indicatorTypeCode, final String sourceCode, final MetadataName entryKey) {
 		final TypedQuery<DataSerieMetadata> query = em.createQuery(
 				"SELECT ad FROM DataSerieMetadata ad WHERE ad.indicatorType.code=:indicatorTypeCode AND ad.source.code=:sourceCode AND ad.entryKey = :entryKey", DataSerieMetadata.class);
 		query.setParameter("indicatorTypeCode", indicatorTypeCode);
 		query.setParameter("sourceCode", sourceCode);
 		query.setParameter("entryKey", entryKey);
 		try {
 			return query.getSingleResult();
 		} catch (final NoResultException e) {
 			return null;
 		}
 	}
 }
