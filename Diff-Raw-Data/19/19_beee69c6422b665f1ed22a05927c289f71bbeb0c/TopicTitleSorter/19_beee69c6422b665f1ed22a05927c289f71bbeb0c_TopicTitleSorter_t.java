 package com.redhat.ecs.services.docbookcompiling.xmlprocessing.sort;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.structures.InjectionTopicData;
 import com.redhat.ecs.sort.ExternalListSort;
 
 import com.redhat.topicindex.rest.entities.interfaces.RESTTopicV1;
 
 public class TopicTitleSorter implements ExternalListSort<Integer, RESTTopicV1, InjectionTopicData>
 {
 	public void sort(final List<RESTTopicV1> topics, final List<InjectionTopicData> list) 
 	    {
 	        if (topics == null || list == null)
 	        	return;
 		 
 		 	Collections.sort(list, new Comparator<InjectionTopicData>() 
 	        {
 				public int compare(final InjectionTopicData o1, final InjectionTopicData o2)
 	            {
 					RESTTopicV1 topic1 = null;
 					RESTTopicV1 topic2 = null;
 	            	
 	            	for (final RESTTopicV1 topic : topics)
 	            	{
 	            		if (topic.getId().equals(o1.topicId))
 	            			topic1 = topic;
 	            		if (topic.getId().equals(o2.topicId))
 	            			topic2 = topic;
 	            		
 	            		if (topic1 != null && topic2 != null)
 	            			break;
 	            	}
 	            	
 	            	final boolean v1Exists = topic1 != null;
 	            	final boolean v2Exists = topic2 != null;
 	            	
 	            	if (!v1Exists && !v2Exists)
 	            		return 0;
 	            	if (!v1Exists)
 	            		return -1;
 	            	if (!v2Exists)
 	            		return 1;
 	            	
 	            	final RESTTopicV1 v1 = topic1;
 	            	final RESTTopicV1 v2 = topic2;
 	            	
 	            	if (v1 == null && v2 == null)
 	            		return 0;
 	            	
 	            	if (v1 == null)
 	            		return -1;
 	            	
 	            	if (v2 == null)
 	            		return 1;
 	            	
 	            	if (v1.getTitle() == null && v2.getTitle() == null)
 	            		return 0;
 	            	
 	            	if (v1.getTitle() == null)
 	            		return -1;
 	            	
 	            	if (v2.getTitle() == null)
 	            		return 1;
 	            	
 	            	return v1.getTitle().toLowerCase().compareTo(v2.getTitle().toLowerCase());
 	            }
 	        });
 	    }
 }
 
