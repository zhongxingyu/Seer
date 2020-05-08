 package com.fnz.dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.Set;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.collections.ObservableMap;
 
 import org.sqlite.SQLiteConfig;
 
 import com.fnz.VO.StockVO;
 import com.fnz.VO.ItemTypeVO;
 import com.fnz.VO.ItemVO;
 import com.fnz.common.CommonConstants;
 import com.fnz.common.SQLConstants;
 
 public class OutgoingStockDAO
 {
 	public String deleteOutgoingStock(String date, ObservableList<ItemVO> listData, String categoryId) throws Exception 
 	{
 		Connection conn = null;
 		ResultSet resultSet = null;
 		SQLiteConfig config = null;
 		Statement statement = null;
 		PreparedStatement prep = null;
 		Class.forName(CommonConstants.DRIVERNAME);
 		String msg = CommonConstants.UPDATE_MSG;
 		ObservableList<ItemVO> itemList = FXCollections.observableArrayList();
 		itemList = new StockDetailsDAO().viewStock(categoryId);
 		ObservableList<String> tempItemId = FXCollections.observableArrayList();
 		ObservableList<String> tempTypeId = FXCollections.observableArrayList();
 		
 		String sDbUrl = CommonConstants.sJdbc + ":" + CommonConstants.DB_LOCATION + CommonConstants.sTempDb;
 		
 		try 
 		{
 			config = new SQLiteConfig();
 			config.enforceForeignKeys(true);
 			conn = DriverManager.getConnection(sDbUrl, config.toProperties());
 			statement = conn.createStatement();
 			
 			String splitsDate[] = date.split("/");
 			date = splitsDate[2]+"-"+splitsDate[1]+"-"+splitsDate[0];
 			
 			prep = conn.prepareStatement(SQLConstants.FETCH_OUTGOING_BY_DATE);
 			prep.setString(1, date);
 			resultSet = prep.executeQuery();
 			
 			while(resultSet.next())
 			{
 				tempItemId.add(resultSet.getString(1));
 				tempTypeId.add(resultSet.getString(2));
 			}
 			
 			for(ItemVO itemVO : listData)
 			{
 				for(ItemVO tempVO : itemList)
 				{
 					if(itemVO.getItemId().equals(tempVO.getItemId()))
 					{
 						ObservableMap<String, ItemTypeVO> map = FXCollections.observableHashMap();
 						map=itemVO.getListType();
 						Set<String> keySet = map.keySet();
 						for(Iterator<String> iter=keySet.iterator();iter.hasNext();)
 						{
 							ItemTypeVO itemTypeVO = new ItemTypeVO();
 							itemTypeVO = map.get(iter.next());
 							Integer tempQuantity = tempVO.getListType().get(itemTypeVO.getTypeId()).getQuantity();
 							if(tempQuantity>=itemTypeVO.getQuantity())
 							{
 								if(itemTypeVO.getQuantity()>0)
 								{
									
 									if(tempItemId.contains(itemTypeVO.getItemId()))
 									{
 										if(tempTypeId.contains(itemTypeVO.getTypeId()))
 										{
 											statement.addBatch(SQLConstants.UPDATE_OUTGOING_STOCK_DETAILS_1+itemTypeVO.getQuantity()+
 													SQLConstants.UPDATE_OUTGOING_STOCK_DETAILS_2+itemTypeVO.getItemId()+
 													SQLConstants.UPDATE_OUTGOING_STOCK_DETAILS_3+itemTypeVO.getTypeId()+
 													SQLConstants.UPDATE_OUTGOING_STOCK_DETAILS_4+date+
 													SQLConstants.UPDATE_OUTGOING_STOCK_DETAILS_5);
 										}
 									}
 									else
 									{
 									statement.addBatch(SQLConstants.INSERT_OUTGOING_STOCK_DETAILS_1+date+SQLConstants.INSERT_OUTGOING_STOCK_DETAILS_2+
 											itemTypeVO.getItemId()+SQLConstants.INSERT_OUTGOING_STOCK_DETAILS_2+itemTypeVO.getTypeId()+SQLConstants.INSERT_OUTGOING_STOCK_DETAILS_3+
 											itemTypeVO.getQuantity()+SQLConstants.INSERT_OUTGOING_STOCK_DETAILS_4);
 									}
									statement.addBatch(SQLConstants.UPDATE_DEL_ITEMS_TYPES_1 + itemTypeVO.getQuantity() + SQLConstants.UPDATE_DEL_ITEMS_TYPES_2 +
											itemVO.getItemId() + SQLConstants.UPDATE_DEL_ITEMS_TYPES_3 + itemTypeVO.getTypeId() + SQLConstants.UPDATE_DEL_ITEMS_TYPES_4);
 								}
 							}
 							else
 							{
 								return itemVO.getItemName()+"*&^"+itemTypeVO.getTypeId();
 							}
 						}
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
 	
 	public ObservableList<StockVO> fetchOutgoingStockDetails(String initialDate, String finalDate) throws Exception 
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
 			pstmt = conn.prepareStatement(SQLConstants.FETCH_OUTGOING_DETAILS_BY_DATE);
 			
 			
 			
 			String splitsInitialDate[] = initialDate.split("/");
 			initialDate = splitsInitialDate[2]+"-"+splitsInitialDate[1]+"-"+splitsInitialDate[0];
 			
 			String splitsFinalDate[] = finalDate.split("/");
 			finalDate = splitsFinalDate[2]+"-"+splitsFinalDate[1]+"-"+splitsFinalDate[0];
 			
 			pstmt.setString(1,  initialDate);
 			pstmt.setString(2, finalDate);
 			
 			
 			resultSet = pstmt.executeQuery();
 			
 			while(resultSet.next())
 			{
 				StockVO StockVO = new StockVO();
 				String splitsDate[] = resultSet.getString(1).split("-");
 				StockVO.setDate(splitsDate[2]+"/"+splitsDate[1]+"/"+splitsDate[0]);
 				StockVO.setItemId(resultSet.getString(2));
 				StockVO.setItemName(resultSet.getString(3));
 				StockVO.setTypeId(resultSet.getString(4));
 				StockVO.setTypeName(resultSet.getString(5));
 				StockVO.setQuantity(resultSet.getInt(6));
 				listIncoming.add(StockVO);
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
 	
 	public ObservableList<StockVO> fetchOutgoingStockDetails(String invoiceId) throws Exception 
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
 			pstmt = conn.prepareStatement(SQLConstants.FETCH_OUTGOING_DETAILS_BY_INVOICE);
 			
 		
 			
 			pstmt.setString(1,  invoiceId);
 			
 			
 			resultSet = pstmt.executeQuery();
 			
 			while(resultSet.next())
 			{
 				StockVO StockVO = new StockVO();
 				String splitsDate[] = resultSet.getString(1).split("-");
 				StockVO.setDate(splitsDate[2]+"/"+splitsDate[1]+"/"+splitsDate[0]);
 				StockVO.setItemName(resultSet.getString(2));
 				StockVO.setTypeName(resultSet.getString(3));
 				StockVO.setQuantity(resultSet.getInt(4));
 				listIncoming.add(StockVO);
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
