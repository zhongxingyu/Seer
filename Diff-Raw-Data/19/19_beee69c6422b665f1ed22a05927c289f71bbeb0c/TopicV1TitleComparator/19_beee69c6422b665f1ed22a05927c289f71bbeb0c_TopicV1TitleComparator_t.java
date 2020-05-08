 package com.redhat.ecs.services.docbookcompiling.xmlprocessing.sort;
 
 import java.util.Comparator;
 
 import com.redhat.topicindex.rest.entities.interfaces.RESTTopicV1;
 
 public class TopicV1TitleComparator implements Comparator<RESTTopicV1>
 {
 	public int compare(final RESTTopicV1 o1, final RESTTopicV1 o2)
 	{
 		if (o1 == null && o2 == null)
 			return 0;
 		if (o1 == null)
 			return -1;
 		if (o2 == null)
 			return 1;
 		
 		if (o1.getTitle() == null && o2.getTitle() == null)
 			return 0;
 		if (o1.getTitle() == null)
 			return -1;
 		if (o2.getTitle() == null)
 			return 1;
 		
 		return o1.getTitle().compareTo(o2.getTitle());
 	}
 }
