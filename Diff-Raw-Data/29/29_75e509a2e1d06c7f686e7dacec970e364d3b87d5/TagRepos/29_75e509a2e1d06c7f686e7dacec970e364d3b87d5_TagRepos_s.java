 package grids.repos;
 
 import grids.entity.Tag;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class TagRepos extends BaseRepos<Tag> {
 	public Tag findByNameAndParent(String name, long parentId) {
 		for (Tag child : get(parentId).getChildren()) {
 			if (child.getName().equals(name)) {
 				return child;
 			}
 		}
 		return null;
 	}
 	
 //	public Set<Tag> loadTags(long[] ids) {
 //		Set<Tag> tags = new HashSet<>();
 //		for (long id : ids) {
 //			tags.add(load(id));
 //		}
 //		return tags;
 //	}
 	
 	public Set<Tag> getTags(long[] ids) {
 		Set<Tag> tags = new HashSet<>();
 		for (long id : ids) {
 			tags.add(get(id));
 		}
 		return tags;
 	}
 	
 	public Set<Tag> getQueryTags(long[] ids) {
 		Set<Tag> tags = getTags(ids);
 		Set<Tag> queryTags = new HashSet<>();
 		for (Tag node : tags) {
 			queryTags.add(node);
			queryTags.addAll(node.descandants());
 		}
 		return queryTags;
 	}
 	
 	@Override
 	protected Class<Tag> getEntityClass() {
 		return Tag.class;
 	}
 }
