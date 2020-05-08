 /**
  * 
  */
 package org.infoglue.deliver.externalsearch;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.Collection;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.infoglue.deliver.externalsearch.IndexableField.IndexingType;
 
 /**
  * @author Erik Stenbäcka
  *
  */
 public class IndexerSupport
 {
 	private static final Logger logger = Logger.getLogger(IndexerSupport.class);
 
 	private Locale swedish = new Locale("sv");
 	private Locale english = Locale.ENGLISH;
 
 	public void addField(Document doc, Map<String, Object> entity, IndexableField field, Locale language)
 	{
 		String key = field.getName();
 		Object value = entity.get(key);
 		if (value == null)
 		{
 			value = "";
 		}
 		registerField(doc, field, value.toString(), language);
 	}
 
 	public void registerField(Document document, IndexableField field, String value, Locale language)
 	{
 		if (value == null)
 		{
 			value = "";
 		}
 		try
 		{
 			String key = field.getFieldName(language);
 			value = value.toLowerCase();
 			if (logger.isDebugEnabled())
 			{
 				logger.debug("Adding field to index. Field: " + field + ". Value: " + value);
 			}
			Index indexingType = Field.Index.TOKENIZED;
			if (field.getIndexingType().equals(IndexingType.UN_TOKENIZED))
			{
				indexingType = Field.Index.UN_TOKENIZED;
			}
			else
			{
				indexingType = Field.Index.TOKENIZED;
			}
			document.add(new Field(key, value, Field.Store.NO, indexingType));
 		}
 		catch (NullPointerException nex)
 		{
 			logger.warn("An attempt to index an unregistered field was made. Field: " + field);
 			throw new IllegalArgumentException("An attempt to index an unregistered field was made. Field: " + field);
 		}
 	}
 
 	public void storeObject(Document document, Locale language, byte[] value)
 	{
 		document.add(new Field(SearchResult.getResultLabel(language), value, Field.Store.YES));
 	}
 
 	public void storeLanguageObject(Locale swedish, Document doc, Map<String, Object> entity) throws IOException
 	{
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(baos);
 		oos.writeObject(entity);
 		oos.flush();
 		byte[] swedishEntityAsBytes = baos.toByteArray();
 
 		storeObject(doc, swedish, swedishEntityAsBytes);
 	}
 
 	public void addFieldsToDocument(Document document, Collection<IndexableField> indexableFields, Map<String, Object> swedishEntity, Map<String, Object> englishEntity) throws IOException
 	{
 		for (IndexableField field : indexableFields)
 		{
 			if (field.isLanguageDependent())
 			{
 				addField(document, swedishEntity, field, swedish);
 				addField(document, englishEntity, field, english);
 			}
 			else
 			{
 				addField(document, swedishEntity, field, swedish);
 			}
 		}
 	}
 }
