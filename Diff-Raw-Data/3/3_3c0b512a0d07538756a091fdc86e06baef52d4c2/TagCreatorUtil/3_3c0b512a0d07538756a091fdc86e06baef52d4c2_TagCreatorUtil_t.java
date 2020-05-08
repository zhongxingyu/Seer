 package org.tothought.spring.utilities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.math.NumberUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.tothought.entities.Tag;
 import org.tothought.repositories.TagRepository;
 
 @Component
 public class TagCreatorUtil {
 	
 	@Autowired
 	private TagRepository tagRepository;
 	
 	public Tag createTag(String tag){
		List<Tag> tags =  this.createTags(tag);
		return (tags.isEmpty()) ? null : tags.get(0);
 	}
 	
 	public List<Tag> createTags(String csvTags) {
 		List<Tag> tags = new ArrayList<Tag>();
 		String[] tagValues = csvTags.split(",");
 
 		for (String tag : tagValues) {
 			if (!StringUtils.isEmpty(tag)) {
 				if (!NumberUtils.isNumber(tag)) {
 					tags.add(new Tag(tag));
 				} else {
 					tags.add(tagRepository.findOne(new Integer(tag)));
 				}
 			}
 		}
 		return tags;
 	}
 
 }
