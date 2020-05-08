 package ecologylab.serialization.serializers.stringformats;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.FieldTypes;
 import ecologylab.serialization.Format;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.serialization.XMLTools;
 import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;
 
 /***
  * JSONSerializaton. Guides serialization of data in JSON. Contains code that is specific to
  * creating a valid JSON of the provided object. Supports graph handling.
  * 
  * @author nabeel
  * 
  */
 public class JSONSerializer extends StringSerializer implements FieldTypes
 {
 	public JSONSerializer()
 	{
 
 	}
 
 	@Override
 	public void serialize(Object object, Appendable appendable, TranslationContext translationContext)
 			throws SIMPLTranslationException
 	{
 		translationContext.resolveGraph(object);
 
 		ClassDescriptor<? extends FieldDescriptor> rootObjectClassDescriptor = ClassDescriptor
 				.getClassDescriptor(object.getClass());
 
 		try
 		{
 			writeStart(appendable);
 
 			serialize(object, rootObjectClassDescriptor.pseudoFieldDescriptor(), appendable,
 					translationContext, true);
 
 			writeClose(appendable);
 		}
 		catch (IOException e)
 		{
 			throw new SIMPLTranslationException("IO Exception occurred", e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param rootObjectFieldDescriptor
 	 * @param appendable
 	 * @param translationContext
 	 * @param withTag
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	private void serialize(Object object, FieldDescriptor rootObjectFieldDescriptor,
 			Appendable appendable, TranslationContext translationContext, boolean withTag)
 			throws SIMPLTranslationException, IOException
 	{
 		if (alreadySerialized(object, translationContext))
 		{
 			writeSimplRef(object, rootObjectFieldDescriptor, withTag, appendable);
 			return;
 		}
 
 		translationContext.mapObject(object);
 
 		serializationPreHook(object, translationContext);
 
 		writeObjectStart(rootObjectFieldDescriptor, appendable, withTag);
 
 		ArrayList<? extends FieldDescriptor> allFieldDescriptors = getClassDescriptor(object)
 				.allFieldDescriptors();
 
 		serializeFields(object, appendable, translationContext, allFieldDescriptors);
 
 		writeClose(appendable);
 
 		serializationPostHook(object, translationContext);
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param appendable
 	 * @param translationContext
 	 * @param allFieldDescriptors
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	private void serializeFields(Object object, Appendable appendable,
 			TranslationContext translationContext,
 			ArrayList<? extends FieldDescriptor> allFieldDescriptors) throws SIMPLTranslationException,
 			IOException
 	{
 		int numOfFields = 0;
 
 		if (TranslationScope.graphSwitch == GRAPH_SWITCH.ON)
 		{
 			if (translationContext.needsHashCode(object))
 			{
 				writeSimplIdAttribute(object, appendable, allFieldDescriptors.size() <= 0);
 			}
 		}
 
 		for (FieldDescriptor childFd : allFieldDescriptors)
 		{
 
 			if (isSerializable(childFd, object))
 			{
 				if (numOfFields++ > 0)
 					appendable.append(',');
 
 				switch (childFd.getType())
 				{
 				case SCALAR:
 					serializeScalar(object, childFd, appendable, translationContext);
 					break;
 				case COMPOSITE_ELEMENT:
 					serializeComposite(object, appendable, translationContext, childFd);
 					break;
 				case COLLECTION_SCALAR:
 				case MAP_SCALAR:
 					serializeScalarCollection(object, appendable, translationContext, childFd);
 					break;
 				case COLLECTION_ELEMENT:
 				case MAP_ELEMENT:
 					if (childFd.isPolymorphic())
 						serializePolymorphicCollection(object, appendable, translationContext, childFd);
 					else
 						serializeCompositeCollection(object, appendable, translationContext, childFd);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * check if the fild is of default value or null. we don't have to serialize that field
 	 * 
 	 * @param childFd
 	 * @param object
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	private boolean isSerializable(FieldDescriptor childFd, Object object)
 			throws SIMPLTranslationException
 	{
 		switch (childFd.getType())
 		{
 		case SCALAR:
 			if (childFd.isDefaultValueFromContext(object))
 				return false;
 			break;
 		case COMPOSITE_ELEMENT:
 		case COLLECTION_ELEMENT:
 		case MAP_ELEMENT:
 			Object obj = childFd.getObject(object);
 			if (obj == null)
 				return false;
 			break;
 		case COLLECTION_SCALAR:
 		case MAP_SCALAR:
			Collection<?> scalarCollection = XMLTools.getCollection(object);
 			if (scalarCollection == null || scalarCollection.size() <= 0)
 				return false;
 			break;
 		}
 
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param appendable
 	 * @param translationContext
 	 * @param childFd
 	 * @throws SIMPLTranslationException
 	 * @throws IOException
 	 */
 	private void serializeComposite(Object object, Appendable appendable,
 			TranslationContext translationContext, FieldDescriptor childFd)
 			throws SIMPLTranslationException, IOException
 	{
 		Object compositeObject = childFd.getObject(object);
 		FieldDescriptor compositeObjectFieldDescriptor = childFd.isPolymorphic() ? getClassDescriptor(
 				compositeObject).pseudoFieldDescriptor() : childFd;
 		serialize(compositeObject, compositeObjectFieldDescriptor, appendable, translationContext, true);
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param appendable
 	 * @param translationContext
 	 * @param childFd
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	private void serializeCompositeCollection(Object object, Appendable appendable,
 			TranslationContext translationContext, FieldDescriptor childFd) throws IOException,
 			SIMPLTranslationException
 	{
 		Object collectionObject = childFd.getObject(object);
 		Collection<?> compositeCollection = XMLTools.getCollection(collectionObject);
 		int numberOfItems = 0;
 
 		writeWrap(childFd, appendable, false);
 		writeCollectionStart(childFd, appendable);
 		for (Object collectionComposite : compositeCollection)
 		{
 			FieldDescriptor collectionObjectFieldDescriptor = childFd.isPolymorphic() ? getClassDescriptor(
 					collectionComposite).pseudoFieldDescriptor()
 					: childFd;
 
 			serialize(collectionComposite, collectionObjectFieldDescriptor, appendable,
 					translationContext, false);
 
 			if (++numberOfItems < compositeCollection.size())
 				appendable.append(',');
 		}
 		writeCollectionEnd(appendable);
 		writeWrap(childFd, appendable, true);
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param appendable
 	 * @param translationContext
 	 * @param childFd
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	private void serializePolymorphicCollection(Object object, Appendable appendable,
 			TranslationContext translationContext, FieldDescriptor childFd) throws IOException,
 			SIMPLTranslationException
 	{
 		Object collectionObject = childFd.getObject(object);
 		Collection<?> compositeCollection = XMLTools.getCollection(collectionObject);
 		int numberOfItems = 0;
 
 		writePolymorphicCollectionStart(childFd, appendable);
 		for (Object collectionComposite : compositeCollection)
 		{
 			FieldDescriptor collectionObjectFieldDescriptor = childFd.isPolymorphic() ? getClassDescriptor(
 					collectionComposite).pseudoFieldDescriptor()
 					: childFd;
 
 			writeStart(appendable);
 			serialize(collectionComposite, collectionObjectFieldDescriptor, appendable,
 					translationContext, true);
 			writeClose(appendable);
 
 			if (++numberOfItems < compositeCollection.size())
 				appendable.append(',');
 		}
 		writeCollectionEnd(appendable);
 
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param appendable
 	 * @param translationContext
 	 * @param childFd
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	private void serializeScalarCollection(Object object, Appendable appendable,
 			TranslationContext translationContext, FieldDescriptor childFd) throws IOException,
 			SIMPLTranslationException
 	{
		Collection<?> scalarCollection = XMLTools.getCollection(object);
 		int numberOfItems = 0;
 
 		writeWrap(childFd, appendable, false);
 		writeCollectionStart(childFd, appendable);
 		for (Object collectionObject : scalarCollection)
 		{
 			writeCollectionScalar(collectionObject, childFd, appendable, translationContext);
 			if (++numberOfItems < scalarCollection.size())
 				appendable.append(',');
 		}
 		writeCollectionEnd(appendable);
 		writeWrap(childFd, appendable, true);
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param fd
 	 * @param appendable
 	 * @param translationContext
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	private void serializeScalar(Object object, FieldDescriptor fd, Appendable appendable,
 			TranslationContext translationContext) throws IOException, SIMPLTranslationException
 	{
 		appendable.append('"');
 		appendable.append(fd.getTagName());
 		appendable.append('"');
 		appendable.append(':');
 		appendable.append('"');
 		fd.appendValue(appendable, object, translationContext, Format.JSON);
 		appendable.append('"');
 	}
 
 	/**
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void writeCollectionEnd(Appendable appendable) throws IOException
 	{
 		appendable.append(']');
 	}
 
 	private void writeCollectionStart(FieldDescriptor fd, Appendable appendable) throws IOException
 	{
 		appendable.append('"').append(fd.elementStart()).append('"');
 		appendable.append(':');
 		appendable.append('[');
 	}
 
 	/**
 	 * 
 	 * @param fd
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void writePolymorphicCollectionStart(FieldDescriptor fd, Appendable appendable)
 			throws IOException
 	{
 		appendable.append('"').append(fd.getTagName()).append('"');
 		appendable.append(':');
 		appendable.append('[');
 
 	}
 
 	/**
 	 * 
 	 * @param fd
 	 * @param appendable
 	 * @param close
 	 * @throws IOException
 	 */
 	private void writeWrap(FieldDescriptor fd, Appendable appendable, boolean close)
 			throws IOException
 	{
 		if (fd.isWrapped())
 		{
 			if (!close)
 			{
 				appendable.append('"');
 				appendable.append(fd.getTagName());
 				appendable.append('"').append(':');
 				appendable.append('{');
 			}
 			else
 			{
 				appendable.append('}');
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param fd
 	 * @param appendable
 	 * @param translationContext
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	private void writeCollectionScalar(Object object, FieldDescriptor fd, Appendable appendable,
 			TranslationContext translationContext) throws IOException, SIMPLTranslationException
 	{
 		appendable.append('"');
		fd.appendValue(appendable, object, translationContext, Format.JSON);
 		appendable.append('"');
 	}
 
 	/**
 	 * 
 	 * @param fd
 	 * @param appendable
 	 * @param withTag
 	 * @throws IOException
 	 */
 	private void writeObjectStart(FieldDescriptor fd, Appendable appendable, boolean withTag)
 			throws IOException
 	{
 		if (withTag)
 		{
 			appendable.append('"').append(fd.elementStart()).append('"');
 			appendable.append(':');
 		}
 		appendable.append('{');
 	}
 
 	/**
 	 * 
 	 * @param object
 	 * @param rootObjectFieldDescriptor
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void writeSimplRef(Object object, FieldDescriptor fd, boolean withTag,
 			Appendable appendable) throws IOException
 	{
 		writeObjectStart(fd, appendable, withTag);
 		writeSimplRefAttribute(object, appendable);
 		writeClose(appendable);
 	}
 
 	private void writeSimplRefAttribute(Object object, Appendable appendable) throws IOException
 	{
 		appendable.append('"');
 		appendable.append(TranslationContext.JSON_SIMPL_REF);
 		appendable.append('"');
 		appendable.append(':');
 		appendable.append('"');
 		appendable.append(((Integer) object.hashCode()).toString());
 		appendable.append('"');
 	}
 
 	private void writeSimplIdAttribute(Object object, Appendable appendable, boolean last)
 			throws IOException
 	{
 		appendable.append('"');
 		appendable.append(TranslationContext.JSON_SIMPL_ID);
 		appendable.append('"');
 		appendable.append(':');
 		appendable.append('"');
 		appendable.append(((Integer) object.hashCode()).toString());
 
 		if (!last)
 		{
 			appendable.append('"');
 			appendable.append(',');
 		}
 	}
 
 	/**
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void writeStart(Appendable appendable) throws IOException
 	{
 		appendable.append('{');
 	}
 
 	/**
 	 * 
 	 * @param appendable
 	 * @throws IOException
 	 */
 	private void writeClose(Appendable appendable) throws IOException
 	{
 		appendable.append('}');
 	}
 }
