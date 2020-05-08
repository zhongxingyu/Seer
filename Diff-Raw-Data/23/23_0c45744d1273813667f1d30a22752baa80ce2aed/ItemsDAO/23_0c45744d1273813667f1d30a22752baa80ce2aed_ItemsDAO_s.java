 package com.vaps.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Map;
 import javax.sql.DataSource;
 import org.mybatis.spring.support.SqlSessionDaoSupport;
 import com.vaps.bean.Items;
 
 public class ItemsDAO extends SqlSessionDaoSupport implements ItemsInterface {
 	private static final Statement ConnUtil = null;
 	DataSource ds;
 	Connection con=null;
 	PreparedStatement pstmt=null;
 	ResultSet rs=null;
 	@Override
 	public List<Items> getItemsList() {
 		// TODO Auto-generated method stub
 		return getSqlSession().selectList("ItemsInterface.getItemsList");
 	}
 	@Override
 	public int getPageCount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public Items getContents(int num) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public int setItems(Items item) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public int delItems(int num) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public Items getItemsModi(int num) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public int setItemsModi(Items item) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	
 }
