 /**
  *
  */
 package q.dao.ibatis;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import q.dao.DaoHelper;
 import q.dao.GroupDao;
 import q.dao.page.GroupJoinCategoryPage;
 import q.dao.page.GroupPage;
 import q.dao.page.GroupRecommendPage;
 import q.dao.page.PeopleJoinGroupPage;
 import q.domain.Group;
 import q.domain.GroupJoinCategory;
 import q.domain.PeopleJoinGroup;
 import q.domain.Status;
 import q.util.CollectionKit;
 import q.util.IdCreator;
 
 /**
  * @author Zhehao
  * @author seanlinwang
  * @date Feb 15, 2011
  *
  */
 
 public class GroupDaoImpl extends AbstractDaoImpl implements GroupDao {
 
 	@Override
 	public void addGroup(Group group) throws SQLException {
 		this.sqlMapClient.insert("insertGroup", group);
 
 	}
 
 	@Override
 	public void addGroupJoinCategory(long groupId, long categoryId) throws SQLException {
 		GroupJoinCategory join = new GroupJoinCategory();
 		join.setCategoryId(categoryId);
 		join.setGroupId(groupId);
 
 		join.setId(IdCreator.getLongId());
 		this.sqlMapClient.insert("insertGroupJoinCategory", join);
 	}
 
 	@Override
 	public GroupJoinCategory selectGroupIdByCatIdAndGroupId(long categoryId, long groupId) throws SQLException {
 		GroupJoinCategory join = new GroupJoinCategory();
 		join.setCategoryId(categoryId);
 		join.setGroupId(groupId);
 
 		Object gjc = this.sqlMapClient.queryForObject("selectGroupIdByCatIdAndGroupId", join);
 		if (gjc != null) {
 			return (GroupJoinCategory) gjc;
 		}
 		return null;
 	}
 
 	@Override
 	public void updateGroupJoinCategoryForAdmin(long groupId, long categoryId, int promote) throws SQLException {
 		GroupJoinCategory join = new GroupJoinCategory();
 		join.setCategoryId(categoryId);
 		join.setGroupId(groupId);
 		join.setPromote(promote);
 		this.sqlMapClient.update("updateCategoryJoinGroupByGroupIdAndCategoryId", join);
 	}
 
 	@Override
 	public void addGroupJoinCategoryForAdmin(long groupId, long categoryId, int promote) throws SQLException {
 		GroupJoinCategory join = new GroupJoinCategory();
 		join.setCategoryId(categoryId);
 		join.setGroupId(groupId);
 		join.setPromote(promote);
 		join.setId(IdCreator.getLongId());
 		this.sqlMapClient.insert("insertGroupJoinCategory", join);
 	}
 
 	@Override
 	public void deleteGroupJoinCategoryForAdmin(long groupId, long categoryId) throws SQLException {
 		GroupJoinCategory join = new GroupJoinCategory();
 		join.setCategoryId(categoryId);
 		join.setGroupId(groupId);
 		join.setPromote(0);
 		this.sqlMapClient.update("updateCategoryJoinGroupByGroupIdAndCategoryId", join);
 	}
 
 	@Override
 	public void addPeopleJoinGroup(long peopleId, long groupId) throws SQLException {
 		PeopleJoinGroup join = new PeopleJoinGroup();
 		join.setPeopleId(peopleId);
 		join.setGroupId(groupId);
 		join.setStatus(Status.COMMON.getValue());
 		join.setId(IdCreator.getLongId());
 		this.sqlMapClient.insert("insertPeopleJoinGroup", join);
 	}
 
 	@Override
 	public Group getGroupById(long gid) throws SQLException {
 		return (Group) this.sqlMapClient.queryForObject("selectGroupById", gid);
 	}
 
 	@Override
 	public PeopleJoinGroup getJoinPeopleByGroupIdPeopleId(long peopleId, long groupId) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setGroupId(groupId);
 		page.setPeopleId(peopleId);
 		return (PeopleJoinGroup) this.sqlMapClient.queryForObject("selectPeopleJoinGroupByPage", page);
 	}
 
 	@Override
 	public List<Long> getJoinPeopleIdsByGroupId(long groupId, int limit, int start) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setSize(limit);
 		page.setStartIndex(start);
 		page.setGroupId(groupId);
 		return DaoHelper.convertPeopleJoinGroupsToPeopleIds(selectPeopleJoinGroupsByPage(page));
 	}
 
 	@Override
 	public List<Long> getJoinPeopleIdsByGroupIds(List<Long> groupIds, int limit, int start) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setSize(limit);
 		page.setStartIndex(start);
 		page.setGroupIds(groupIds);
 		return DaoHelper.convertPeopleJoinGroupsToPeopleIds(selectPeopleJoinGroupsByPage(page));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<PeopleJoinGroup> selectPeopleJoinGroupsByPage(PeopleJoinGroupPage page) throws SQLException {
 		List<PeopleJoinGroup> joins = (List<PeopleJoinGroup>) this.sqlMapClient.queryForList("selectPeopleJoinGroupsByPage", page);
 		return joins;
 	}
 
 	@Override
 	public List<Group> getGroupsByJoinPeopleId(long peopleId) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setPeopleId(peopleId);
 		List<Long> groupIds = this.getGroupIdsByPeopleJoinGroupPage(page);
 		List<Group> groups = null;
 		if (CollectionKit.isNotEmpty(groupIds)) {
 			groups = this.getGroupsByIds(groupIds);
 		}
 		return groups;
 	}
 
 	@Override
 	public List<Long> getGroupIdsByJoinPeopleId(long peopleId) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setPeopleId(peopleId);
 		return this.getGroupIdsByPeopleJoinGroupPage(page);
 	}
 
 	@Override
 	public List<Long> getGroupIdsByJoinPeopleIdAndGroupIds(long peopleId, List<Long> groupIds) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setPeopleId(peopleId);
 		page.setGroupIds(groupIds);
 		return this.getGroupIdsByPeopleJoinGroupPage(page);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Long> getGroupIdsByPeopleJoinGroupPage(PeopleJoinGroupPage page) throws SQLException {
 		return this.sqlMapClient.queryForList("selectJoinGroupIdsByPage", page);
 	}
 
 	@Override
 	public List<Long> getJoinPeopleIdsByHotAndGroupId(long groupId, int limit, int start) throws SQLException {
 		return this.getJoinPeopleIdsByGroupId(groupId, limit, start);
 	}
 
 	@Override
 	public List<Long> getJoinPeopleIdsByHotAndGroupIds(List<Long> groupIds, int limit, int start) throws SQLException {
 		return this.getJoinPeopleIdsByGroupIds(groupIds, limit, start);
 	}
 
 	@Override
 	public List<Group> getAllGroupsByCatId(long catId) throws SQLException {
 		@SuppressWarnings("unchecked")
 		List<Long> groupIds = this.sqlMapClient.queryForList("selectGroupIdsByCatId", catId);
 		List<Group> groups = this.getGroupsByIds(groupIds);
 		return groups;
 	}
 
 	@Override
 	public List<Long> getExsitGroupIdsByIds(List<Long> groupIds) throws SQLException {
 		if (CollectionKit.isEmpty(groupIds)) {
 			return null;
 		}
 		@SuppressWarnings("unchecked")
 		List<Long> ids = this.sqlMapClient.queryForList("selectGroupIdsByIds", groupIds);
 		return ids;
 	}
 
 	@Override
 	public List<Group> getGroupsByIds(List<Long> groupIds) throws SQLException {
 		if (CollectionKit.isEmpty(groupIds)) {
 			return null;
 		}
 		@SuppressWarnings("unchecked")
 		List<Group> groups = this.sqlMapClient.queryForList("selectGroupsByIds", groupIds);
 		return groups;
 	}
 
 	@Override
 	public List<Group> getGroupsByLocation(Group myLocation) throws SQLException {
 		@SuppressWarnings("unchecked")
 		List<Group> groups = this.sqlMapClient.queryForList("selectGroupsByLocation", myLocation);
 		return groups;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see q.dao.CategoryDao#getNewGroups(int)
 	 */
 	@Override
 	public List<Group> getNewGroups(int limit) throws SQLException {
 		GroupPage page = new GroupPage();
 		page.setSize(limit);
 		return (List<Group>) this.getGroupsByPage(page);
 	}
 
 	@Override
 	public List<Group> getHotGroups(int limit) throws SQLException {
 		return this.getNewGroups(limit);
 	}
 
 	@Override
 	public Map<Long, String> getGroupIdNameMapByIds(Set<Long> groupIds) throws SQLException {
 		@SuppressWarnings("unchecked")
 		List<Group> groups = (List<Group>) this.sqlMapClient.queryForList("selectIdNamesByIds", groupIds.toArray());
 		if (CollectionKit.isEmpty(groups)) {
 			return null;
 		}
 		Map<Long, String> IdNameMap = new HashMap<Long, String>(groups.size());
 		for (Group group : groups) {
 			IdNameMap.put(group.getId(), group.getName());
 		}
 		return IdNameMap;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see q.dao.GroupDao#getAllPromotedGroups(java.util.List)
 	 */
 	@Override
 	public List<Group> getAllPromotedGroups(List<Long> categoryIds) throws SQLException {
 		GroupJoinCategoryPage page = new GroupJoinCategoryPage();
 		page.setCategoryIds(categoryIds);
 		@SuppressWarnings("unchecked")
 		List<GroupJoinCategory> joins = this.sqlMapClient.queryForList("getPromotedGroupJoinCategoriesByCatIds", page);
 		if (CollectionKit.isEmpty(joins)) {
 			return null;
 		}
 		Map<Long, GroupJoinCategory> groupId2CatMap = new HashMap<Long, GroupJoinCategory>(joins.size());
 		for (GroupJoinCategory join : joins) {
 			groupId2CatMap.put(join.getGroupId(), join);
 		}
 		Set<Long> groupIdSet = groupId2CatMap.keySet();
 		List<Group> groups = this.getGroupsByIds(new ArrayList<Long>(groupIdSet));
 		for (Group group : groups) {
 			GroupJoinCategory join = groupId2CatMap.get(group.getId());
 			if (join != null) {
 				group.setCategoryId(join.getCategoryId());
 				group.setGroupJoinCategory(join);
 			}
 		}
 		List<Group> sortGroups = new ArrayList<Group>(joins.size());
 		for (GroupJoinCategory join : joins) {
 			sortGroups.add(join.getGroup());
 		}
 		return sortGroups;
 	}
 
 	public List<Group> getGroupsByPage(GroupPage page) throws SQLException {
 		@SuppressWarnings("unchecked")
 		List<Group> groups = this.sqlMapClient.queryForList("getGroupsByPage", page);
 		return groups;
 	}
 
 	@Override
 	public List<Group> getRecommendGroupsByPage(GroupRecommendPage page) throws SQLException {
 		// FIXME sean, use new groups instead
 		GroupPage gpage = new GroupPage();
		gpage.setSize(9);
 		return this.getGroupsByPage(gpage);
 	}
 
 	@Override
 	public List<GroupJoinCategory> getGroupJoinCategoriesByGroupIdAndStatus(long groupId, Status status) throws SQLException {
 		GroupJoinCategoryPage page = new GroupJoinCategoryPage();
 		if (status != null) {
 			page.setStatus(status.getValue());
 		}
 		page.setGroupId(groupId);
 		@SuppressWarnings("unchecked")
 		List<GroupJoinCategory> joins = this.sqlMapClient.queryForList("getGroupJoinCategoriesByPage", page);
 		return joins;
 	}
 
 	@Override
 	public List<Long> getGroupIdsByGroupJoinCategoryPageOrderByGroupId(GroupJoinCategoryPage page) throws SQLException {
 		@SuppressWarnings("unchecked")
 		List<Long> groupIds = this.sqlMapClient.queryForList("getGroupIdsByGroupJoinCategoryPageOrderByGroupId", page);
 		return groupIds;
 	}
 
 	@Override
 	public int deleteGroupJoinCategoriesByjoinIdsAndGroupId(final long groupId, final List<Long> ids) throws SQLException {
 		Object temp = new Object() {
 			@SuppressWarnings("unused")
 			public long getGroupId() {
 				return groupId;
 			}
 
 			@SuppressWarnings("unused")
 			public List<Long> getIds() {
 				return ids;
 			}
 		};
 		return this.sqlMapClient.update("deleteGroupJoinCategoriesByjoinIdsAndGroupId", temp);
 	}
 
 	@Override
 	public int updateGroup(Group group) throws SQLException {
 		return this.sqlMapClient.update("updateGroupById", group);
 	}
 
 	@Override
 	public int updateGroupJoinCategoryStatusByIdAndOldStatus(long joinId, Status newStatus, Status oldStatus) throws SQLException {
 		GroupJoinCategoryPage page = new GroupJoinCategoryPage();
 		page.setId(joinId);
 		page.setNewStatus(newStatus.getValue());
 		page.setOldStatus(oldStatus.getValue());
 		return this.sqlMapClient.update("updateGroupJoinCategoryStatusByPage", page);
 	}
 
 	@Override
 	public int updatePeopleJoinGroupStatusByIdAndOldStatus(long joinId, Status newStatus, Status oldStatus) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setId(joinId);
 		page.setNewStatus(newStatus.getValue());
 		page.setOldStatus(oldStatus.getValue());
 		return this.sqlMapClient.update("updatePeopleJoinGroupStatusByIdAndOldStatus", page);
 	}
 
 	@Override
 	public int updatePeopleJoinGroupStatusByPeopleIdAndGroupIdAndOldStatus(long peopleId, long groupId, Status newStatus, Status oldStatus) throws SQLException {
 		PeopleJoinGroupPage page = new PeopleJoinGroupPage();
 		page.setPeopleId(peopleId);
 		page.setGroupId(groupId);
 		page.setNewStatus(newStatus.getValue());
 		page.setOldStatus(oldStatus.getValue());
 		return this.sqlMapClient.update("updatePeopleJoinGroupStatusByPeopleIdAndGroupIdAndOldStatus", page);
 	}
 
 	@Override
 	public int incrGroupJoinNumByGroupId(long groupId) throws SQLException {
 		return this.sqlMapClient.update("incrGroupJoinNumByGroupId", groupId);
 	}
 
 	@Override
 	public int decrGroupJoinNumByGroupId(long groupId) throws SQLException {
 		return this.sqlMapClient.update("decrGroupJoinNumByGroupId", groupId);
 	}
 }
