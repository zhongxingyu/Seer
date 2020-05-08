 package com.fnz.dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.collections.ObservableMap;
 
 import org.sqlite.SQLiteConfig;
 
 import com.fnz.VO.CategoryTypeVO;
 import com.fnz.VO.ItemTypeVO;
 import com.fnz.VO.ItemVO;
 import com.fnz.VO.StockVO;
 import com.fnz.common.CommonConstants;
 import com.fnz.common.SQLConstants;
 
 public class IncomingStockDAO 
 {
 	public String addIncomingStock(String invoiceNo, String date, ObservableList<ItemVO> listData, ObservableList<CategoryTypeVO> typeList) throws Exception 
 	{
 		Connection conn = null;
 		ResultSet resultSet = null;
 		SQLiteConfig config = null;
 		java.sql.Statement statement = null;
 		Class.forName(CommonConstants.DRIVERNAME);
 		String msg = CommonConstants.UPDATE_MSG;
 		
 		String sDbUrl = CommonConstants.sJdbc + ":" + CommonConstants.DB_LOCATION + CommonConstants.sTempDb;
 		
 		try 
 		{
 			config = new SQLiteConfig();
 			config.enforceForeignKeys(true);
 			conn = DriverManager.getConnection(sDbUrl, config.toProperties());
 			statement = conn.createStatement();
 			
 			String splitsDate[] = date.split("/");
 			date = splitsDate[2]+"-"+splitsDate[1]+"-"+splitsDate[0];
 			
 			
 			/*statement.addBatch(SQLConstants.INSERT_INCOMING_STOCK_1+invoiceNo+SQLConstants.INSERT_INCOMING_STOCK_2+date+SQLConstants.INSERT_INCOMING_STOCK_2
 					+""+SQLConstants.INSERT_INCOMING_STOCK_3);*/
 			for(ItemVO itemVO : listData)
 			{
 				ObservableMap<String, ItemTypeVO> map = FXCollections.observableHashMap();
 				map=itemVO.getListType();
 				Set<String> keySet = map.keySet();
 				for(Iterator<String> iter=keySet.iterator();iter.hasNext();)
 				{
 					ItemTypeVO itemTypeVO = new ItemTypeVO();
 					String tempTypeName ="";
 					itemTypeVO = map.get(iter.next());
 					/*for(CategoryTypeVO type:typeList)
 					{
 						if(type.getTypeId().equals(itemTypeVO.getTypeId()))
 						{
 							tempTypeName = type.getTypeName();
 							break;
 						}
 					}*/
 					if(itemTypeVO.getQuantity()>0)
 					{
 						statement.addBatch(SQLConstants.UPDATE_ADD_ITEMS_TYPES_1 + itemTypeVO.getQuantity()*CommonConstants.CASE_SIZE + SQLConstants.UPDATE_ADD_ITEMS_TYPES_2 +
 								itemVO.getItemId() + SQLConstants.UPDATE_ADD_ITEMS_TYPES_3 + itemTypeVO.getTypeId() + SQLConstants.UPDATE_ADD_ITEMS_TYPES_4);
 						statement.addBatch(SQLConstants.INSERT_INCOMING_STOCK_DETAILS_1+invoiceNo+SQLConstants.INSERT_INCOMING_STOCK_DETAILS_2+
 								date + SQLConstants.INSERT_INCOMING_STOCK_DETAILS_3+
 								itemVO.getItemId()+SQLConstants.INSERT_INCOMING_STOCK_DETAILS_3+itemTypeVO.getTypeId()+SQLConstants.INSERT_INCOMING_STOCK_DETAILS_4+
								itemTypeVO.getQuantity()*CommonConstants.CASE_SIZE+SQLConstants.INSERT_INCOMING_STOCK_DETAILS_5);
 					}
 				}
 			}
 			
 			statement.executeBatch();
 		}
 		catch (Exception e) 
 		{
 			throw e;
 		}
 		finally
 		{
 			if(conn !=null )
 			{
 				conn.close();
 			}
 			if(statement != null )
 			{
 				statement.close();
 			}
 			if(resultSet != null)
 			{
 				resultSet.close();
 			}
 		}
 		
 		return msg;
 	}
 	
 	
 
 	
 	public ObservableList<StockVO> fetchIncomingStockDetails(String initialDate, String finalDate) throws Exception 
 	{
 		Connection conn = null;
 		PreparedStatement pstmt = null;
 		ResultSet resultSet = null;
 		SQLiteConfig config = null;
 		ObservableList<StockVO> listIncoming = FXCollections.observableArrayList();
 		
 		
 		Class.forName(CommonConstants.DRIVERNAME);
 		
 		String sDbUrl = CommonConstants.sJdbc + ":" + CommonConstants.DB_LOCATION + CommonConstants.sTempDb;
 		
 		try 
 		{
 			config = new SQLiteConfig();
 			config.enforceForeignKeys(true);
 			conn = DriverManager.getConnection(sDbUrl, config.toProperties());
 			pstmt = conn.prepareStatement(SQLConstants.FETCH_INCOMING_DETAILS_BY_DATE);
 			
 			
 			
 			String splitsInitialDate[] = initialDate.split("/");
 			initialDate = splitsInitialDate[2]+"-"+splitsInitialDate[1]+"-"+splitsInitialDate[0];
 			
 			String splitsFinalDate[] = finalDate.split("/");
 			finalDate = splitsFinalDate[2]+"-"+splitsFinalDate[1]+"-"+splitsFinalDate[0];
 			
 			pstmt.setString(1,  initialDate);
 			pstmt.setString(2, finalDate);
 			
 			
 			resultSet = pstmt.executeQuery();
 			
 			while(resultSet.next())
 			{
 				StockVO incomingStockVO = new StockVO();
 				incomingStockVO.setInvoiceId(resultSet.getString(1));
 				String splitsDate[] = resultSet.getString(2).split("-");
 				incomingStockVO.setDate(splitsDate[2]+"/"+splitsDate[1]+"/"+splitsDate[0]);
 				incomingStockVO.setItemId(resultSet.getString(3));
 				incomingStockVO.setItemName(resultSet.getString(4));
 				incomingStockVO.setTypeId(resultSet.getString(5));
 				incomingStockVO.setTypeName(resultSet.getString(6));
 				incomingStockVO.setQuantity(resultSet.getInt(7));
 				incomingStockVO.setCheck(false);
 				listIncoming.add(incomingStockVO);
 			}
 			
 		}
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(conn !=null )
 			{
 				conn.close();
 			}
 			if(pstmt != null )
 			{
 				pstmt.close();
 			}
 			if(resultSet != null)
 			{
 				resultSet.close();
 			}
 		}
 		return listIncoming;
 	}
 	
 	public ObservableList<StockVO> fetchIncomingStockDetails(String invoiceId) throws Exception 
 	{
 		Connection conn = null;
 		PreparedStatement pstmt = null;
 		ResultSet resultSet = null;
 		SQLiteConfig config = null;
 		ObservableList<StockVO> listIncoming = FXCollections.observableArrayList();
 		
 		
 		Class.forName(CommonConstants.DRIVERNAME);
 		
 		String sDbUrl = CommonConstants.sJdbc + ":" + CommonConstants.DB_LOCATION + CommonConstants.sTempDb;
 		
 		try 
 		{
 			config = new SQLiteConfig();
 			config.enforceForeignKeys(true);
 			conn = DriverManager.getConnection(sDbUrl, config.toProperties());
 			pstmt = conn.prepareStatement(SQLConstants.FETCH_INCOMING_DETAILS_BY_INVOICE);
 			
 			
 			pstmt.setString(1,  invoiceId);
 			
 			
 			resultSet = pstmt.executeQuery();
 			
 			while(resultSet.next())
 			{
 				StockVO incomingStockVO = new StockVO();
 				incomingStockVO.setInvoiceId(resultSet.getString(1));
 				String splitsDate[] = resultSet.getString(2).split("-");
 				incomingStockVO.setDate(splitsDate[2]+"/"+splitsDate[1]+"/"+splitsDate[0]);
 				incomingStockVO.setItemId(resultSet.getString(3));
 				incomingStockVO.setItemName(resultSet.getString(4));
 				incomingStockVO.setTypeId(resultSet.getString(5));
 				incomingStockVO.setTypeName(resultSet.getString(6));
 				incomingStockVO.setQuantity(resultSet.getInt(7));
 				listIncoming.add(incomingStockVO);
 			}
 			
 		}
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(conn !=null )
 			{
 				conn.close();
 			}
 			if(pstmt != null )
 			{
 				pstmt.close();
 			}
 			if(resultSet != null)
 			{
 				resultSet.close();
 			}
 		}
 		return listIncoming;
 	}
 }
