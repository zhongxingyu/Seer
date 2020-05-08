 package com.redhat.topicindex.component.docbookrenderer.structures;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.topicindex.rest.entities.ComponentBaseTopicV1;
import com.redhat.topicindex.rest.entities.ComponentTranslatedTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTBaseTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTranslatedTopicV1;
 
 /**
  * Provides a central location for storing and adding messages that are
  * generated while compiling to docbook.
  */
 public class TopicErrorDatabase<T extends RESTBaseTopicV1<T>>
 {
 	public static final Integer ERROR = 1;
 	public static final Integer WARNING = 5;
 
 	private Map<String, List<TopicErrorData<T>>> errors = new HashMap<String, List<TopicErrorData<T>>>();
 
 	public int getErrorCount(final String locale)
 	{
 		return errors.containsKey(locale) ? errors.get(locale).size() : 0;
 	}
 
 	public boolean hasItems()
 	{
 		return errors.size() != 0;
 	}
 	
 	public boolean hasItems(final String locale)
 	{
 		return errors.containsKey(locale) ? errors.get(locale).size() != 0 : false;
 	}
 
 	public void addError(final T topic, final String error)
 	{
 		addItem(topic, error, ERROR);
 	}
 
 	public void addWarning(final T topic, final String error)
 	{
 		addItem(topic, error, WARNING);
 	}
 	
 	/**
 	 * Add a error for a topic that was included in the TOC
 	 * @param topic
 	 * @param error
 	 */
 	public void addTocError(final T topic, final String error)
 	{
 		addItem(topic, error, ERROR);
 	}
 
 	public void addTocWarning(final T topic, final String error)
 	{
 		addItem(topic, error, WARNING);
 	}
 
 	private void addItem(final T topic, final String item, final Integer level)
 	{
 		final TopicErrorData<T> topicErrorData = addOrGetTopicErrorData(topic);
 		/* don't add duplicates */
 		if (!(topicErrorData.getErrors().containsKey(level) && topicErrorData.getErrors().get(level).contains(item)))
 			topicErrorData.addError(item, level);
 	}
 
 	private TopicErrorData<T> getErrorData(final T topic)
 	{
 		for (final String locale : errors.keySet())
 			for (final TopicErrorData<T> topicErrorData : errors.get(locale))
 			{
 				if (ComponentBaseTopicV1.returnIsDummyTopic(topic))
 				{
 					if (topic.getClass() == RESTTranslatedTopicV1.class && topicErrorData.getTopic() instanceof RESTTranslatedTopicV1)
 					{
 						if (((RESTTranslatedTopicV1) topicErrorData.getTopic()).getTopicId().equals(((RESTTranslatedTopicV1) topic).getTopicId()))
 							return topicErrorData;
 					}
 				}
 				else
 				{
 					if (topicErrorData.getTopic().getId().equals(topic.getId()))
 						return topicErrorData;
 				}
 			}
 		return null;
 	}
 
 	private TopicErrorData<T> addOrGetTopicErrorData(final T topic)
 	{
 		TopicErrorData<T> topicErrorData = getErrorData(topic);
 		if (topicErrorData == null)
 		{
 			topicErrorData = new TopicErrorData<T>();
 			topicErrorData.setTopic(topic);
 			if (!errors.containsKey(topic.getLocale()))
 				errors.put(topic.getLocale(), new ArrayList<TopicErrorData<T>>());
 			errors.get(topic.getLocale()).add(topicErrorData);
 		}
 		return topicErrorData;
 	}
 	
 	public List<String> getLocales()
 	{
 		return CollectionUtilities.toArrayList(errors.keySet());
 	}
 
 	public List<TopicErrorData<T>> getErrors(final String locale)
 	{
 		return errors.containsKey(locale) ? errors.get(locale) : null;
 	}
 
 	public void setErrors(final String locale, final List<TopicErrorData<T>> errors)
 	{
 		this.errors.put(locale, errors);
 	}
 }
