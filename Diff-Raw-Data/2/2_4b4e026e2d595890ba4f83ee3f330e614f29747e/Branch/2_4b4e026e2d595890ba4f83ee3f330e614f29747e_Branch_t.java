 package com.guotingchao.model.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.guotingchao.model.BaseModel;
 import com.guotingchao.model.IBranchDao;
 import com.jfinal.plugin.activerecord.Model;
 
 public class Branch extends BaseModel<Branch> implements IBranchDao {
 	
 	public static Branch branchDao  = new Branch(); 
 	
 	public Map<String, Object> getAttrs(){
 	    return super.getAttrs();
 	}
 	
 	@Override
 	public List<BaseModel<Branch>> findBrachBytid(Long tid) {
 		return branchDao.find("select * from branch where tid=?",tid);
 	}
 
 	@Override
 	public List<BaseModel<Branch>> branchInfoBytid(Long tid) {
 		
 		return branchDao.find("SELECT u.uname,b.* FROM `user` u,branch b WHERE b.tid=? and u.id=b.uid",tid);
 	}
 
 	@Override
 	public Branch findBranchInfoBytid(Long tid) {
 		// TODO Auto-generated method stub
		return (Branch) branchDao.find("SELECT u.uname,b.* FROM `user` u,branch b WHERE b.id=? and u.id=b.uid",tid).get(0);
 	}
 
 
 }
