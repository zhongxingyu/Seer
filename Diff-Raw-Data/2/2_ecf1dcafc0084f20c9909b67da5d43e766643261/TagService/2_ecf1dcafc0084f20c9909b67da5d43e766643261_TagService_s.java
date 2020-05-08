 package cz.cvut.fel.bupro.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fel.bupro.dao.TagGroupRepository;
 import cz.cvut.fel.bupro.dao.TagRepository;
 import cz.cvut.fel.bupro.model.Tag;
 import cz.cvut.fel.bupro.model.TagGroup;
 
 @Service
 public class TagService {
 
 	@Autowired
 	private TagRepository tagRepository;
 	@Autowired
 	private TagGroupRepository tagGroupRepository;
 
 	@Transactional
 	public List<Tag> getAllTags() {
 		List<Tag> tags = tagRepository.findAll();
 		Tag.sortByRanking(tags);
 		return tags;
 	}
 
 	@Transactional
 	public Map<String, List<String>> getTagNameMap() {
 		Map<String, List<String>> map = new HashMap<String, List<String>>();
 		List<TagGroup> tagGroups = tagGroupRepository.findAll(new Sort(new Sort.Order("name")));
 		for (TagGroup tagGroup : tagGroups) {
 			List<Tag> tags = new ArrayList<Tag>(tagGroup.getTags());
 			Tag.sortByRanking(tags);
 			List<String> list = new ArrayList<String>(tags.size());
 			for (Tag tag : tags) {
 				list.add(String.valueOf(tag));
 			}
 			map.put(tagGroup.getName(), list);
 		}
 		return map;
 	}
 
 	@Transactional
 	public Set<Tag> refresh(Set<Tag> tags) {
 		if (tags.isEmpty()) {
 			return tags;
 		}
 		Set<Tag> set = new HashSet<Tag>();
 		for (Tag tag : tags) {
 			if (tag == null) {
 				continue;
 			}
 			if (tag.getId() == null) {
 				set.add(tag);
 				continue;
 			}
 			set.add(tagRepository.findOne(tag.getId()));
 		}
 		return set;
 	}
 
 	@Transactional
 	public TagGroup refresh(TagGroup tagGroup) {
		return tagGroupRepository.findOne(tagGroup.getId());
 	}
 
 	@Transactional
 	public void removeUnusedTags() {
 		tagRepository.removeUnusedTags();
 	}
 
 	public TagGroup createNewTagGroup(TagGroup tagGroup) {
 		return tagGroupRepository.save(tagGroup);
 	}
 
 }
