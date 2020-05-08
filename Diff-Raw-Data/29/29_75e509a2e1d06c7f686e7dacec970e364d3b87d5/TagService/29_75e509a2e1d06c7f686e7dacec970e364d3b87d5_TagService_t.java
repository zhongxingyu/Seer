 package grids.service;
 
 import grids.entity.Tag;
 import grids.repos.TagRepos;
 import grids.transfer.TagCard;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class TagService {
 	@Autowired
 	TagRepos tagRepos;
 	
 	public long newTag(String name, long parentId) {
 		Tag tag = new Tag(name, tagRepos.load(parentId));
 		if (tagRepos.findByNameAndParent(name, parentId) == null) {
 			tagRepos.save(tag);
 			return tag.getId();
 		}
 		else return -1;
 	}
 	
 	@Transactional(readOnly=true)
 	public TagCard buildTagCard(long tagId) {
 		return new TagCard(tagRepos.get(tagId));
 	}
 	
 	@Transactional(readOnly=true)
 	public List<Tag> chainUp(long tagId) {
 		return tagRepos.get(tagId).chainUp();
 	}
 	
 	@Transactional(readOnly=true)
 	public Collection<Tag> children(long tagId) {
 		return tagRepos.get(tagId).getChildren();
 	}
 	
 	@Transactional(readOnly=true)
 	public Collection<Tag> descendants(long tagId) {
		return tagRepos.get(tagId).descendants();
 	}
 
 	public void init() {
 		if (needInitialize) {
 			tagRepos.save(new Tag("root", null));
 			needInitialize = false;
 		}
 	}
 	private static boolean needInitialize = true;
 }
