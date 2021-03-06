 package marubinotto.piggydb.model;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import marubinotto.piggydb.model.FragmentsOptions.SortOption;
 import marubinotto.piggydb.model.entity.RawFragment;
 import marubinotto.piggydb.model.entity.RawTag;
 import marubinotto.piggydb.model.enums.FragmentField;
 import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
 import marubinotto.piggydb.model.exception.DuplicateException;
 import marubinotto.piggydb.model.exception.NoSuchEntityException;
 import marubinotto.util.Assert;
 import marubinotto.util.paging.Page;
 import marubinotto.util.time.Interval;
 import marubinotto.util.time.Month;
 
 public interface FragmentRepository extends Repository<Fragment> {
 	
 	public Fragment newInstance(User user);
 	
 	// NOTE: "updateTimestamp = false" will disable optimistic lock. 
 	// That means the update might be overwritten by another, without notice
 	public boolean update(Fragment fragment, boolean updateTimestamp) 
 	throws BaseDataObsoleteException, Exception;
 	
 	public TagRepository getTagRepository();
 	
 	public boolean containsId(Long id) throws Exception;
 	
 	public Fragment get(long id, boolean fetchingRelations) throws Exception;
 	
 	public void setFileRepository(FileRepository fileRepository);
 	
 	public void refreshClassifications(List<? extends Fragment> fragments) throws Exception;
 
 	public void deleteTrashes(User user) throws Exception;
 
 	public Page<Fragment> getFragments(FragmentsOptions options) throws Exception;
 	
 	public Set<Integer> getDaysOfMonth(FragmentField field, Month month) 
 	throws Exception;
 
 	public Page<Fragment> findByTime(
 		Interval interval, 
 		FragmentField field, 
 		FragmentsOptions options)
 	throws Exception;
 
 	public Page<Fragment> findByFilter(Filter filter, FragmentsOptions options) 
 	throws Exception;
 	
 	public RelatedTags getRelatedTags(Filter filter) throws Exception;
 	
 	public Page<Fragment> findByKeywords(String keywords, FragmentsOptions options)
 	throws Exception;
 	
 	public Page<Fragment> findByUser(String userName, FragmentsOptions options)
 	throws Exception;
 	
 	public List<Fragment> getByIds(
 		Collection<Long> fragmentIds, 
 		SortOption sortOption, 
 		boolean eagerFetching) 
 	throws Exception;
 	
 	public Fragment getUserFragment(String userName) throws Exception;
 
 	public long createRelation(long from, long to, User user)
 	throws NoSuchEntityException, DuplicateException, Exception;
 	
 	public FragmentRelation getRelation(long relationId) throws Exception;
 	
 	public FragmentRelation deleteRelation(long relationId, User user) throws Exception;
 	
 	public Long countRelations() throws Exception;
 	
 	public void updateChildRelationPriorities(Fragment parent, List<Long> relationOrder, User user)
 	throws Exception;
 	
 	
 	public static abstract class Base 
 	extends Repository.Base<Fragment, RawFragment> implements FragmentRepository {
 		
 		protected FileRepository fileRepository;
 
 		public RawFragment newRawEntity() {
 			return new RawFragment();
 		}
 
 		public Fragment newInstance(User user) {
 			Assert.Arg.notNull(user, "user");
 			return new RawFragment(user);
 		}
 		
 		public abstract TagRepository.Base getTagRepository();
 
 		public void setFileRepository(FileRepository fileRepository) {
 			this.fileRepository = fileRepository;
 		}
 
 		public FileRepository getFileRepository() {
 			return fileRepository;
 		}
 		
 		public Fragment get(long id) throws Exception {
 			return get(id, true);
 		}
 		
 		public final boolean update(Fragment fragment) throws Exception {
 			return update(fragment, true); 
 		}
 		
 		public final boolean update(Fragment fragment, boolean updateTimestamp) 
 		throws Exception {
 			Assert.Arg.notNull(fragment, "fragment");
 			Assert.require(fragment instanceof RawFragment, "fragment instanceof RawFragment");
 			Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
 			Assert.Property.requireNotNull(fileRepository, "fileRepository");
 			
 			// Check preconditions
 			if (!containsId(fragment.getId())) return false;
 			if (fragment.getUpdateDatetime() == null) throw new BaseDataObsoleteException();
 			
 			// Update the tag role
 			updateTagRole((RawFragment)fragment);
 			
 			// Update the fragment
 			updateFragment(fragment, updateTimestamp);
 			
 			return true;
 		}
 		
 		public abstract void updateFragment(Fragment fragment, boolean updateTimestamp) 
 		throws Exception;
 		
 		protected void updateTagRole(RawFragment fragment) throws Exception {
 			Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
 			
 			if (fragment.isTag()) {
 				RawTag tag = (RawTag)fragment.asTag();
 				// skip the update if the tag object is null
 				if (tag != null) {
 					// new
 					if (tag.getId() == null) {
 						tag.setFragmentId(fragment.getId());
 						Long tagId = getTagRepository().register(tag);
 						fragment.setTagId(tagId);
 					}
 					// update
 					else {
 						getTagRepository().updateTag(tag);
 					}
 				}
 			}
 			else {
 				Long tagId = fragment.getTagId();
 				// delete
 				if (tagId != null) {
 					getTagRepository().delete(tagId);
 					fragment.setTagId(null);
 				}
 			}
 		}
 		
 		public final long createRelation(long from, long to, User user)
 		throws NoSuchEntityException, DuplicateException, Exception {
 			Assert.Arg.notNull(user, "user");
 			Assert.require(from != to, "from != to");
 			
 			FragmentRelation.ensureCanCreate(user);
 			return doCreateRelation(from, to, user);
 		}
 		
 		protected abstract long doCreateRelation(long from, long to, User user)
 		throws NoSuchEntityException, DuplicateException, Exception;
 		
 		public final FragmentRelation deleteRelation(long relationId, User user) throws Exception {
 			Assert.Arg.notNull(user, "user");
 			
 			FragmentRelation relation = getRelation(relationId);
 			if (relation == null) return null;
 			
 			relation.ensureCanDelete(user);
 			doDeleteRelation(relation.getId());
 			
 			return relation;
 		}
 		
 		protected abstract void doDeleteRelation(long relationId) throws Exception;
 		
 		public final void updateChildRelationPriorities(Fragment parent, List<Long> relationOrder, User user)
 		throws Exception {
 			Assert.Arg.notNull(parent, "parent");
 			Assert.Arg.notNull(relationOrder, "relationOrder");
 			Assert.Arg.notNull(user, "user");
 			
 			parent.ensureCanChange(user);
 			
 			if (relationOrder.isEmpty()) return;
 			doUpdateChildRelationPriorities(relationOrder);
 		}
 		
 		protected abstract void doUpdateChildRelationPriorities(List<Long> relationOrder)
 		throws Exception;		
 	}
 }
