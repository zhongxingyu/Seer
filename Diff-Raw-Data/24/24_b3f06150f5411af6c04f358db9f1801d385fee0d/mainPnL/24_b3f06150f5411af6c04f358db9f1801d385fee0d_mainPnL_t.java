 package com.ben.service;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ParseException;
 import org.json.simple.JSONObject;
 
 public class mainPnL {
 
 	ResultSet rs = null;
 //	Connection con = null;
 	Statement st = null;
//	String url = "jdbc:mysql://ben512.no-ip.org:3306/Stocks";
 	
	String url = "jdbc:mysql://192.168.0.6:3306/Stocks";
 //	String url = "jdbc:mysql://localhost:3306/Stocks";
 	String user = "root";
 	String password = "root";
 	
 	Map<String, String> _TimeZoneMap = new HashMap<String, String>();
 	List<String> _TickersOfInterest = new ArrayList<String>();
     Map<String, Map<String, String>> _mapsTickers=new HashMap<String, Map<String,String>>();
     Object _maptmp = new HashMap<String, Object>();
     
 	public mainPnL(String _env) throws SQLException
 	{
 		
 		getInterestList();
 	
 		if (_env.equals("PROD"))
 		{
 			if (_TimeZoneMap.isEmpty())
 			{
 				getTimeZones();
 			getAdvCurve();
 			}
 		}
 		
 	}
 	
 	public String Value_pie_json(Boolean live) throws SQLException {
 		JSONObject obj = new JSONObject();
 		JSONObject obj_cols_1 = new JSONObject();
 		JSONObject obj_cols_2 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "Stock");
 		obj_cols_1.put("type", "string");
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "$Value");
 		obj_cols_2.put("type", "number");
 
 		LinkedList l_cols = new LinkedList();
 
 		l_cols.add(obj_cols_1);
 		l_cols.add(obj_cols_2);
 		obj.put("cols", l_cols);
 		try {
 
 			rs = LoadData("Select distinct Ticker from currentholdings");
 		} catch (Exception e) {
 			System.out.println(e.toString());
 
 		}
 		String Ticker;
 		String LastPx = "";
 		String Qty;
 		String AvgPx;
 
 		Double Value;
 		GoogleScrape gs = new GoogleScrape();
 		List<String> Tickers = new ArrayList<String>();
 		LinkedList l_final = new LinkedList();
 		while (rs.next()) {
 			// Ticker = (rs.getString(1));
 			Tickers.add(rs.getString(1));
 		}
 		for (String name : Tickers) {
 			Ticker = (name);
 
 			Qty = LoadData_str("Select Max(Quantity) from currentholdings where Ticker = '"
 					+ Ticker + "' limit 1");
 
 			if (live == true) {
 				try
 				{
 				LastPx = LoadData_str("select LastPx from pnl  where Ticker='"
 						+ Ticker + "' order by date desc limit 1");
 				// LastPx = gs.getLast(Ticker);
 
 				Value = Double.valueOf(LastPx) * Double.valueOf(Qty);
 				}
 				catch (Exception e)
 				{
 					AvgPx = LoadData_str("Select AvgPx from currentholdings where Ticker = '"
 							+ Ticker + "'");
 					Value = Double.valueOf(AvgPx) * Double.valueOf(Qty);
 					
 				}
 			} else {
 				AvgPx = LoadData_str("Select AvgPx from currentholdings where Ticker = '"
 						+ Ticker + "'");
 				Value = Double.valueOf(AvgPx) * Double.valueOf(Qty);
 			}
 
 			
 
 			
 			Value = Convert_to_USD(Value,Ticker);
 						
 			JSONObject obj2 = new JSONObject();
 			JSONObject obj3 = new JSONObject();
 			JSONObject obj4 = new JSONObject();
 			JSONObject obj_col = new JSONObject();
 
 			obj3.put("v", Ticker);
 			obj3.put("f", null);
 			obj4.put("v", Value);
 			obj4.put("f", null);
 
 			LinkedList l1 = new LinkedList();
 			LinkedHashMap m1 = new LinkedHashMap();
 			l1.add(obj3);
 			l1.add(obj4);
 			m1.put("c", l1);
 
 			l_final.add(m1);
 		}
 		obj.put("rows", l_final);
 
 		System.out.println("Pie Chart: " + obj.toJSONString());
 
 		return obj.toJSONString();
 
 	}
 
 	public String Value_Line_json() throws SQLException {
 		List<String> Tickers = new ArrayList<String>();
 		rs = LoadData("Select distinct Ticker from currentholdings");
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 		while (rs.next()) {
 			// Ticker = (rs.getString(1));
 			Tickers.add(rs.getString(1));
 		}
 		for (String name : Tickers) {
 
 			JSONObject obj_cols_1 = new JSONObject();
 
 			obj_cols_1.put("id", "");
 			obj_cols_1.put("label", name);
 			obj_cols_1.put("type", "number");
 
 			l_cols.add(obj_cols_1);
 		}
 		JSONObject obj_cols_1 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "Total");
 		obj_cols_1.put("type", "number");
 		l_cols.add(obj_cols_1);
 
 		JSONObject obj_cols_2 = new JSONObject();
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "dates");
 		obj_cols_2.put("type", "string");
 
 		l_cols.add(0, obj_cols_2);
 
 		obj.put("cols", l_cols);
 
 		try {
 
 			rs = LoadData("Select distinct date  from pnl order by date desc limit 20");
 		} catch (Exception e) {
 			System.out.println(e.toString());
 
 		}
 		String Ticker;
 		String LastPx;
 		String Qty;
 		String PnL;
 		Double Value;
 		GoogleScrape gs = new GoogleScrape();
 		List<String> Date_list = new ArrayList<String>();
 		LinkedList l_final = new LinkedList();
 
 		while (rs.next())
 
 		{
 			// Ticker = (rs.getString(1));
 			Date_list.add(rs.getString(1));
 			
 			
 			
 		}
 
 		for (String date : Date_list) {
 			Double _Total=0.0;
 			LinkedList l1 = new LinkedList();
 			for (String ticker : Tickers) {
 
 				Ticker = (ticker);
 				// LastPx = gs.getLast(Ticker);
 				try {
 					PnL = LoadData_str("Select PL from pnl  where date = '"
 							+ date + "' and Ticker = '" + ticker + "'");
 					
 				PnL = Convert_to_USD(Double.valueOf(PnL),ticker).toString();
 					
 					
 					
 				} catch (Exception e) {
 					PnL = "0";
 				}
 
 				JSONObject obj_col = new JSONObject();
 				JSONObject obj_val = new JSONObject();
 
 				obj_val.put("v", Double.valueOf(PnL));
 				obj_val.put("f", null);
 
 				l1.add(obj_val);
 				_Total += Double.valueOf(PnL);
 			}
 
 			// Total PNL calculation
 			Double TotalPnL;
 			
 			
 			TotalPnL = _Total;
 			
 			
 			try {
 				//TotalPnL = LoadData_str("Select SUM(PL) from (select distinct * from PnL) as T1 where date = '"
 			//			+ date + "'");
 				
 				
 			} catch (Exception e) {
 				TotalPnL = 0.0;
 			}
 
 			JSONObject obj_val = new JSONObject();
 
 			obj_val.put("v", Double.valueOf(TotalPnL));
 			obj_val.put("f", null);
 
 			l1.add(obj_val);
 
 			// End
 
 			JSONObject obj_date = new JSONObject();
 			LinkedHashMap m1 = new LinkedHashMap();
 			obj_date.put("v", date);
 			obj_date.put("f", null);
 
 			l1.add(0, obj_date);
 
 			m1.put("c", l1);
 			l_final.add(m1);
 
 			// l1.clear();
 			// m1.clear();
 		}
 
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 
 	}
 
 	public String Value_Line_json2() throws SQLException {
 		List<String> Tickers = new ArrayList<String>();
 		List<String> pl_list = new ArrayList<String>();
 		rs = LoadData("Select distinct Ticker from currentholdings");
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 		while (rs.next()) {
 			// Ticker = (rs.getString(1));
 			Tickers.add(rs.getString(1));
 		}
 		for (String name : Tickers) {
 
 			JSONObject obj_cols_1 = new JSONObject();
 
 			obj_cols_1.put("id", "");
 			obj_cols_1.put("label", name);
 			obj_cols_1.put("type", "number");
 
 			l_cols.add(obj_cols_1);
 		}
 		JSONObject obj_cols_1 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "Total");
 		obj_cols_1.put("type", "number");
 		l_cols.add(obj_cols_1);
 
 		JSONObject obj_cols_2 = new JSONObject();
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "dates");
 		obj_cols_2.put("type", "string");
 
 		l_cols.add(0, obj_cols_2);
 
 		obj.put("cols", l_cols);
 
 		try {
 			for (String name : Tickers) {
 			rs = LoadData("Select distinct date,pl  from pnl where Ticker='"+name+"'");
 			pl_list.clear();
 				while (rs.next())
 	
 				{
 					// Ticker = (rs.getString(1));
 					pl_list.add(rs.getString(1));
 					
 				}
 					for (String pl : pl_list) 
 					{
 			
 						
 						
 					}
 			}
 		}
 			
 			
 		 catch (Exception e) {
 			System.out.println(e.toString());
 
 		}
 		String Ticker;
 		String LastPx;
 		String Qty;
 		String PnL;
 		Double Value;
 		GoogleScrape gs = new GoogleScrape();
 		List<String> Date_list = new ArrayList<String>();
 		LinkedList l_final = new LinkedList();
 
 		while (rs.next())
 
 		{
 			// Ticker = (rs.getString(1));
 			Date_list.add(rs.getString(1));
 			
 			
 			
 		}
 
 		for (String date : Date_list) {
 			Double _Total=0.0;
 			LinkedList l1 = new LinkedList();
 			for (String ticker : Tickers) {
 
 				Ticker = (ticker);
 				// LastPx = gs.getLast(Ticker);
 				try {
 					PnL = LoadData_str("Select PL from pnl  where date = '"
 							+ date + "' and Ticker = '" + ticker + "'");
 					
 				PnL = Convert_to_USD(Double.valueOf(PnL),ticker).toString();
 					
 					
 					
 				} catch (Exception e) {
 					PnL = "0";
 				}
 
 				JSONObject obj_col = new JSONObject();
 				JSONObject obj_val = new JSONObject();
 
 				obj_val.put("v", Double.valueOf(PnL));
 				obj_val.put("f", null);
 
 				l1.add(obj_val);
 				_Total += Double.valueOf(PnL);
 			}
 
 			// Total PNL calculation
 			Double TotalPnL;
 			
 			
 			TotalPnL = _Total;
 			
 			
 			try {
 				//TotalPnL = LoadData_str("Select SUM(PL) from (select distinct * from PnL) as T1 where date = '"
 			//			+ date + "'");
 				
 				
 			} catch (Exception e) {
 				TotalPnL = 0.0;
 			}
 
 			JSONObject obj_val = new JSONObject();
 
 			obj_val.put("v", Double.valueOf(TotalPnL));
 			obj_val.put("f", null);
 
 			l1.add(obj_val);
 
 			// End
 
 			JSONObject obj_date = new JSONObject();
 			LinkedHashMap m1 = new LinkedHashMap();
 			obj_date.put("v", date);
 			obj_date.put("f", null);
 
 			l1.add(0, obj_date);
 
 			m1.put("c", l1);
 			l_final.add(m1);
 
 			// l1.clear();
 			// m1.clear();
 		}
 
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 
 	}
 	
 	
 	public String FXTable() throws SQLException {
 
 		// Columns
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 		JSONObject obj_cols_1 = new JSONObject();
 		JSONObject obj_cols_2 = new JSONObject();
 		JSONObject obj_cols_3 = new JSONObject();
 		JSONObject obj_cols_4 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "GBP");
 		obj_cols_1.put("type", "number");
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "USD");
 		obj_cols_2.put("type", "number");
 
 		obj_cols_3.put("id", "");
 		obj_cols_3.put("label", "AvgFX");
 		obj_cols_3.put("type", "number");
 
 		obj_cols_4.put("id", "");
 		obj_cols_4.put("label", "FX_PnL");
 		obj_cols_4.put("type", "number");
 
 		// obj_cols_4.put("id","");
 		// obj_cols_4.put("label","BreakevenFx");
 		// obj_cols_4.put("type","number");
 
 		l_cols.add(obj_cols_1);
 		l_cols.add(obj_cols_2);
 		l_cols.add(obj_cols_3);
 		l_cols.add(obj_cols_4);
 		obj.put("cols", l_cols);
 		// rows
 
 		Double  USD_IN = get_usd_total();
 		
 		Double GBP_IN = get_gbp_total();
 		
 		Double Fx_Rate = get_avg_fx();
 
 		
 
 		Double FX_last = get_FX_gbpusd();
 		DecimalFormat df = new DecimalFormat("#.##");
 
 		String FX_change = df.format(((Double.valueOf(Fx_Rate) / Double
 				.valueOf(FX_last)) - 1) * 100);
 		String FX_pct;
 		/*
 		 * if (Double.valueOf(Fx_Rate) > Double.valueOf(FX_last)) { FX_pct =
 		 * Fx_last+" ("+ FX_change+")";
 		 * 
 		 * } else { FX_pct = Fx_last+" ("+ FX_change+")";
 		 * 
 		 * }
 		 */
 
 		JSONObject obj_row1 = new JSONObject();
 		JSONObject obj_row2 = new JSONObject();
 		JSONObject obj_row3 = new JSONObject();
 		JSONObject obj_row4 = new JSONObject();
 		obj_row1.put("v", GBP_IN);
 		obj_row1.put("f", null);
 		obj_row2.put("v", USD_IN);
 		obj_row2.put("f", null);
 		obj_row3.put("v", Fx_Rate);
 		obj_row3.put("f", null);
 		obj_row4.put("v", FX_last + " (" + FX_change + ")");
 		obj_row4.put("f", null);
 
 		// obj_row4.put("v",d_Change);
 		// obj_row4.put("f", null);
 
 		LinkedList l1_rows = new LinkedList();
 		l1_rows.add(obj_row1);
 		l1_rows.add(obj_row2);
 		l1_rows.add(obj_row3);
 		l1_rows.add(obj_row4);
 		LinkedHashMap m1 = new LinkedHashMap();
 		LinkedList l_final = new LinkedList();
 		m1.put("c", l1_rows);
 		l_final.add(m1);
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 	}
 
 	public String Table_holdings() throws SQLException {
 
 		// Column names
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 
 		JSONObject obj_cols_1 = new JSONObject();
 		JSONObject obj_cols_2 = new JSONObject();
 		JSONObject obj_cols_3 = new JSONObject();
 		JSONObject obj_cols_4 = new JSONObject();
 		JSONObject obj_cols_5 = new JSONObject();
 		JSONObject obj_cols_6 = new JSONObject();
 		JSONObject obj_cols_7 = new JSONObject();
 		JSONObject obj_cols_8 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "Ticker");
 		obj_cols_1.put("type", "String");
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "Qty");
 		obj_cols_2.put("type", "number");
 
 		obj_cols_3.put("id", "");
 		obj_cols_3.put("label", "AvgPx");
 		obj_cols_3.put("type", "number");
 
 		obj_cols_4.put("id", "");
 		obj_cols_4.put("label", "LastPx");
 		obj_cols_4.put("type", "number");
 
 		obj_cols_5.put("id", "");
 		obj_cols_5.put("label", "CCY");
 		obj_cols_5.put("type", "String");
 		
 		
 		obj_cols_6.put("id", "");
 		obj_cols_6.put("label", "UPnLvLast");
 		obj_cols_6.put("type", "number");
 
 		obj_cols_7.put("id", "");
 		obj_cols_7.put("label", "%");
 		obj_cols_7.put("type", "number");
 
 		obj_cols_8.put("id", "");
 		obj_cols_8.put("label", "Date");
 		obj_cols_8.put("type", "String");
 
 		l_cols.add(obj_cols_1);
 		l_cols.add(obj_cols_2);
 		l_cols.add(obj_cols_3);
 		l_cols.add(obj_cols_4);
 		l_cols.add(obj_cols_5);
 		l_cols.add(obj_cols_6);
 		l_cols.add(obj_cols_7);
 		l_cols.add(obj_cols_8);
 		obj.put("cols", l_cols);
 		// End Columns
 
 		rs = LoadData("Select distinct Ticker from currentholdings");
 		ArrayList<String> l_Tickers = new ArrayList<String>();
 		LinkedList l_final = new LinkedList();
 		while (rs.next()) {
 			l_Tickers.add(rs.getString(1));
 		}
 
 		String Qty;
 		String AvgPx;
 		String LastPx;
 		String UPnLvLast;
 		String Pcnt;
 		String Date;
 		String Ccy;
 		for (String name : l_Tickers) {
 			LinkedList l1_rows = new LinkedList();
 			rs = LoadData("Select Quantity, AvgPx,Ccy from currentholdings where Ticker ='"
 					+ name + "'");
 			rs.next();
 
 			Qty = rs.getString(1);
 			AvgPx = rs.getString(2);
 			Ccy = rs.getString(3);
 			try
 			{
 			rs = LoadData("Select LastPx, PL,PL_Percent,date from pnl where Ticker ='"
 					+ name + "' order by date desc limit 1");
 			rs.next();
 			LastPx = rs.getString(1);
 			UPnLvLast = rs.getString(2);
 			Pcnt = rs.getString(3);
 			Date = rs.getString(4);
 			UPnLvLast = Convert_to_USD(Double.valueOf(UPnLvLast),name).toString();
 			}
 			catch (Exception e)
 			{
 				LastPx = "0";
 				UPnLvLast ="0";
 				Pcnt = "0";
 				Date = "-No Data-";
 				UPnLvLast = "0";
 				
 				
 				
 			}
 			
 
 			JSONObject obj_row1 = new JSONObject();
 			JSONObject obj_row2 = new JSONObject();
 			JSONObject obj_row3 = new JSONObject();
 			JSONObject obj_row4 = new JSONObject();
 			JSONObject obj_row5 = new JSONObject();
 			JSONObject obj_row6 = new JSONObject();
 			JSONObject obj_row7 = new JSONObject();
 			JSONObject obj_row8 = new JSONObject();
 			obj_row1.put("v", name);
 			obj_row1.put("f", null);
 			obj_row2.put("v", Qty);
 			obj_row2.put("f", null);
 			obj_row3.put("v", AvgPx);
 			obj_row3.put("f", null);
 			obj_row4.put("v", LastPx);
 			obj_row4.put("f", null);
 			obj_row5.put("v", Ccy);
 			obj_row5.put("f", null);
 			
 			obj_row6.put("v", UPnLvLast);
 			obj_row6.put("f", null);
 			obj_row7.put("v", Pcnt);
 			obj_row7.put("f", Pcnt + "%");
 			obj_row8.put("v", Date);
 			obj_row8.put("f", null);
 
 			l1_rows.add(obj_row1);
 			l1_rows.add(obj_row2);
 			l1_rows.add(obj_row3);
 			l1_rows.add(obj_row4);
 			l1_rows.add(obj_row5);
 			l1_rows.add(obj_row6);
 			l1_rows.add(obj_row7);
 			l1_rows.add(obj_row8);
 			LinkedHashMap m1 = new LinkedHashMap();
 
 			m1.put("c", l1_rows);
 			l_final.add(m1);
 
 		}
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 
 	}
 
 	public String Table_PnL() {
 		// Column names
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 
 		JSONObject obj_cols_1 = new JSONObject();
 		JSONObject obj_cols_2 = new JSONObject();
 		JSONObject obj_cols_3 = new JSONObject();
 		JSONObject obj_cols_4 = new JSONObject();
 		JSONObject obj_cols_5 = new JSONObject();
 		JSONObject obj_cols_6 = new JSONObject();
 		JSONObject obj_cols_7 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "RPnL");
 		obj_cols_1.put("type", "number");
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "UPnL");
 		obj_cols_2.put("type", "number");
 
 		obj_cols_3.put("id", "");
 		obj_cols_3.put("label", "TotalPnL");
 		obj_cols_3.put("type", "number");
 
 		obj_cols_4.put("id", "");
 		obj_cols_4.put("label", "%USD");
 		obj_cols_4.put("type", "String");
 
 		obj_cols_5.put("id", "");
 		obj_cols_5.put("label", "%GBP");
 		obj_cols_5.put("type", "String");
 
 		l_cols.add(obj_cols_1);
 		l_cols.add(obj_cols_2);
 		l_cols.add(obj_cols_3);
 		l_cols.add(obj_cols_4);
 		l_cols.add(obj_cols_5);
 
 		obj.put("cols", l_cols);
 		// End Columns
 
 		double _rpnl = get_RPnL();
 		double _upnl = get_UPnL();
 		double _totalpnl = _rpnl + _upnl;
 		double _usd = get_usd_total();
 		double _gbp = get_gbp_total();
 		double _pctusd = (_totalpnl / _usd) * 100;
 		double _gbpusd = get_FX_gbpusd();
 		double _pctgbp = ((_totalpnl / _gbpusd) / _gbp) * 100;
 		DecimalFormat df = new DecimalFormat("#.##");
 		String _USD = df.format(_pctusd)+"% ($"+df.format(_pctusd*_usd/100)+")";
 		String _GBP = df.format(_pctgbp)+"% ("+df.format(_pctgbp*_gbp/100)+")";
 		
 		LinkedList l1_rows = new LinkedList();
 		LinkedList l_final = new LinkedList();
 		JSONObject obj_row1 = new JSONObject();
 		JSONObject obj_row2 = new JSONObject();
 		JSONObject obj_row3 = new JSONObject();
 		JSONObject obj_row4 = new JSONObject();
 		JSONObject obj_row5 = new JSONObject();
 		JSONObject obj_row6 = new JSONObject();
 		JSONObject obj_row7 = new JSONObject();
 		obj_row1.put("v", _rpnl);
 		obj_row1.put("f", null);
 		obj_row2.put("v", _upnl);
 		obj_row2.put("f", null);
 		obj_row3.put("v", _totalpnl);
 		obj_row3.put("f", null);
 		obj_row4.put("v", _USD);
 		obj_row4.put("f", null);
 		obj_row5.put("v", _GBP);
 		obj_row5.put("f", null);
 
 		l1_rows.add(obj_row1);
 		l1_rows.add(obj_row2);
 		l1_rows.add(obj_row3);
 		l1_rows.add(obj_row4);
 		l1_rows.add(obj_row5);
 
 		LinkedHashMap m1 = new LinkedHashMap();
 
 		m1.put("c", l1_rows);
 		l_final.add(m1);
 
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 
 	}
 
 	//
 	public String Table_PL_Realised() throws SQLException
 
 	{
 		// Column names
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 
 		JSONObject obj_cols_1 = new JSONObject();
 		JSONObject obj_cols_2 = new JSONObject();
 		JSONObject obj_cols_3 = new JSONObject();
 		JSONObject obj_cols_4 = new JSONObject();
 		JSONObject obj_cols_5 = new JSONObject();
 		JSONObject obj_cols_6 = new JSONObject();
 		JSONObject obj_cols_7 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "Ticker");
 		obj_cols_1.put("type", "string");
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "$P/L");
 		obj_cols_2.put("type", "number");
 
 		obj_cols_3.put("id", "");
 		obj_cols_3.put("label", "$Total");
 		obj_cols_3.put("type", "number");
 
 		l_cols.add(obj_cols_1);
 		l_cols.add(obj_cols_2);
 		l_cols.add(obj_cols_3);
 		// l_cols.add(obj_cols_4);
 		// l_cols.add(obj_cols_5);
 		// l_cols.add(obj_cols_6);
 		// l_cols.add(obj_cols_7);
 		obj.put("cols", l_cols);
 		// End Columns
 
		rs = LoadData(" select Ticker, SUM(Quantity),SUM(Quantity*Px)/SUM(Quantity) from holdingshistory where Direction = 'S' group by Ticker order by Date Asc");
 		ArrayList<String> l_Tickers = new ArrayList<String>();
 		ArrayList<String> l_Qty = new ArrayList<String>();
 		ArrayList<String> l_avSellPx = new ArrayList<String>();
 
 		LinkedList l_final = new LinkedList();
 		while (rs.next()) {
 			l_Tickers.add(rs.getString(1));
 			l_Qty.add(rs.getString(2));
 			l_avSellPx.add(rs.getString(3));
 		}
 
 		String Qty;
 		String Buy_Px;
 		String Sell_Px;
 		Double RPnL;
 		String Pcnt;
 		String Date;
 		Double Total = 0.0;
 		ResultSet rs_buy;
 		for (int i = 0; i < l_Tickers.size(); i++) {
 			rs_buy = LoadData(" select SUM(Quantity),SUM(Quantity*Px)/SUM(Quantity) from holdingshistory where Direction = 'B' and Ticker = '"
 					+ l_Tickers.get(i) + "' group by Ticker");
 			ArrayList<String> l_Tickers_qty = new ArrayList<String>();
 			ArrayList<String> l_Tickers_px = new ArrayList<String>();
 			if (rs.next()) {
 				LinkedList l1_rows = new LinkedList();
 				Qty = rs_buy.getString(1);
 				Buy_Px = rs_buy.getString(2);
 				/*
 				 * rs_sell =
 				 * LoadData("Select Px from holdingshistory where Ticker ='"
 				 * +name+"' and Direction ='S' and Quantity ='"+Qty+"'"); if
 				 * (rs_sell.next()) { // rs_sell.next();
 				 */
 				Sell_Px = l_avSellPx.get(i);
 
				RPnL = Convert_to_USD((Double.valueOf(Qty)* (Double.valueOf(Sell_Px) - Double.valueOf(Buy_Px))),l_Tickers.get(i));
 				Total = Total + RPnL;
 
 				JSONObject obj_row1 = new JSONObject();
 				JSONObject obj_row2 = new JSONObject();
 				JSONObject obj_row3 = new JSONObject();
 				obj_row1.put("v", l_Tickers.get(i));
 				obj_row1.put("f", null);
 				obj_row2.put("v", RPnL);
 				obj_row2.put("f", null);
 				obj_row3.put("v", Total);
 				obj_row3.put("f", null);
 
 				l1_rows.add(obj_row1);
 				l1_rows.add(obj_row2);
 				l1_rows.add(obj_row3);
 
 				LinkedHashMap m1 = new LinkedHashMap();
 
 				m1.put("c", l1_rows);
 				l_final.add(m1);
 			} else {
 			}
 
 		}
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 
 	}
 
 	private double get_UPnL() {
 		List<String> Tickers = new ArrayList<String>();
 		List<String> PL = new ArrayList<String>();
 		Double Total = 0.0;
 		try {
 
 	//		rs = LoadData("Select SUM(PL) from (select distinct * pnl) as T1 where date = (select max(date) from PnL)");
 
 			rs = LoadData("select distinct Ticker,PL from pnl  where date = (select max(date) from pnl ) and Ticker in (select distinct ticker from currentholdings)");
 		
 			while (rs.next()) {
 				Tickers.add(rs.getString(1));
 				PL.add(rs.getString(2));
 			}
 			for(int i=0;i<Tickers.size();i++)
 			{
 				String Ticker = Tickers.get(i);
 				String Pnl = PL.get(i);
 			
 			Pnl = Convert_to_USD(Double.valueOf(Pnl),Ticker).toString();
 				
 				Total +=Double.valueOf(Pnl);
 			}
 			
 			
 		
 
 			return Total;
 		} catch (Exception e) {
 			System.out.println(e.toString());
 			return 0;
 
 		}
 
 	}
 
 	private double get_RPnL() {
 		Double Total = 0.0;
 		try {
 			rs = LoadData(" select Ticker, SUM(Quantity),SUM(Quantity*Px)/SUM(Quantity) from holdingshistory where Direction = 'S' group by Ticker");
 			ArrayList<String> l_Tickers = new ArrayList<String>();
 			ArrayList<String> l_Qty = new ArrayList<String>();
 			ArrayList<String> l_avSellPx = new ArrayList<String>();
 
 			LinkedList l_final = new LinkedList();
 			while (rs.next()) {
 				l_Tickers.add(rs.getString(1));
 				l_Qty.add(rs.getString(2));
 				l_avSellPx.add(rs.getString(3));
 			}
 
 			String Qty;
 			String Buy_Px;
 			String Sell_Px;
 			Double RPnL;
 			String Pcnt;
 			String Date;
 
 			ResultSet rs_buy;
 			for (int i = 0; i < l_Tickers.size(); i++) {
 				rs_buy = LoadData(" select SUM(Quantity),SUM(Quantity*Px)/SUM(Quantity) from holdingshistory where Direction = 'B' and Ticker = '"
 						+ l_Tickers.get(i) + "' group by Ticker");
 				ArrayList<String> l_Tickers_qty = new ArrayList<String>();
 				ArrayList<String> l_Tickers_px = new ArrayList<String>();
 				if (rs.next()) {
 					LinkedList l1_rows = new LinkedList();
 					Qty = rs_buy.getString(1);
 					Buy_Px = rs_buy.getString(2);
 					/*
 					 * rs_sell =
 					 * LoadData("Select Px from holdingshistory where Ticker ='"
 					 * +name+"' and Direction ='S' and Quantity ='"+Qty+"'"); if
 					 * (rs_sell.next()) { // rs_sell.next();
 					 */
 					Sell_Px = l_avSellPx.get(i);
					RPnL = Convert_to_USD((Double.valueOf(Qty)* (Double.valueOf(Sell_Px) - Double.valueOf(Buy_Px))),l_Tickers.get(i));
					//RPnL = Double.valueOf(Qty)* (Double.valueOf(Sell_Px) - Double.valueOf(Buy_Px));
 					Total = Total + RPnL;
 				}
 			}
 		} catch (Exception e) {
 			return 0;
 		}
 		return Total;
 
 	}
 
 	private double get_avg_fx()
 	{
 		try {
 			return Double.valueOf(LoadData_str("Select sum(FxRate*dollar_value)/sum(dollar_value) from fx"));
 		} catch (Exception e) {
 			return 0.0;
 			
 			
 		}
 		
 		
 		
 	}
 	
 	
 	private double get_usd_total() {
 		Double Total = 0.0;
 		try {
 
 			String _in = LoadData_str("Select SUM(dollar_value) from fx where Direction = 'IN'");
 			String _out = LoadData_str("Select SUM(dollar_value) from fx where Direction = 'OUT'");
 			String _cash = LoadData_str("Select Quantity from cash where date = (select Max(date) from cash)");
 			
 			String GBP_in = LoadData_str("Select SUM(pound_value) from fx where fxrate is Null");
 			
 			Double AvgFx = get_avg_fx();
 			
 			 Total = (Double.valueOf(Double.valueOf(_in) - Double.valueOf(_out))+Double.valueOf(_cash))+(Double.valueOf(GBP_in)*Double.valueOf(AvgFx));
 			return Total;
 		} catch (Exception e) {
 			return 0;
 
 		}
 
 	}
 	private double get_usd_in() {
 		Double Total = 0.0;
 		try {
 
 			String _in = LoadData_str("Select SUM(dollar_value) from fx where Direction = 'IN'");
 			String _out = LoadData_str("Select SUM(dollar_value) from fx where Direction = 'OUT'");
 			
 			 Total = (Double.valueOf(Double.valueOf(_in) - Double.valueOf(_out)));
 			return Total;
 		} catch (Exception e) {
 			return 0;
 
 		}
 
 	}
 	private double get_gbp_total() {
 		Double Total = 0.0;
 		try {
 
 			String _in = LoadData_str("Select SUM(pound_value) from fx where Direction = 'IN'");
 			String _out = LoadData_str("Select SUM(pound_value) from fx where Direction = 'OUT'");
 			
 			return Double.valueOf(Double.valueOf(_in) - Double.valueOf(_out));
 		} catch (Exception e) {
 			return 0;
 
 		}
 
 	}
 
 	private double get_FX_gbpusd() {
 		Double Total = 0.0;
 		try {
 
 			String _FX = LoadData_str("select Rate from fx_rate order by Date Desc limit 1");
 
 			return Double.valueOf(_FX);
 		} catch (Exception e) {
 			return 0;
 
 		}
 
 	}
 
 	private Double Convert_to_USD(Double Value,String Ticker)
 	{
 	try{
 			
			String Ccy = LoadData_str("Select Ccy from interestlist where Ticker = '"
 						+ Ticker + "' limit 1");			
 			
			if (Ccy.equals("GBX"))
 			{
 				String Fx = LoadData_str("Select Rate from fx_rate order by date desc limit 1");
 			
 				Value = (Value / 100)*Double.valueOf(Fx);
 			return Value;
 			}
 			else
 			{
 			return Value;
 			}
 		}
 		catch(Exception e)
 		{
 		return Value;	
 		}
 	
 	
 	}
 	
 	public String LoadData_str(String Message) throws SQLException {
 		LogOutput(Message);
 		PreparedStatement pst = null;
 try
 {
 		Connection con = DriverManager.getConnection(url, user, password);
 		// st = con.createStatement();
 		// rs = st.executeQuery("SELECT VERSION()");
 		// rs.next();
 		// System.out.println(rs.getString(1));
 
 		pst = con.prepareStatement(Message);
 		rs = pst.executeQuery();
 		rs.next();
 		// while (rs.next()) {
 		// System.out.print(rs.getString(1));
 		// System.out.print(": ");
 		// System.out.println(rs.getString(2));
 		// }
 	
 		return rs.getString(1);
 }
 catch (Exception e)
 {
 	return "0";
 	
 	
 }
 
 }
 
 	public ResultSet LoadData(String Message) throws SQLException {
 		LogOutput(Message);
 		PreparedStatement pst = null;
 
 	Connection con = DriverManager.getConnection(url, user, password);
 		// st = con.createStatement();
 		// rs = st.executeQuery("SELECT VERSION()");
 		// rs.next();
 		// System.out.println(rs.getString(1));
 		pst = con.prepareStatement(Message);
 		rs = pst.executeQuery();
 
 		// while (rs.next()) {
 		// System.out.print(rs.getString(1));
 		// System.out.print(": ");
 		// System.out.println(rs.getString(2));
 		// }
 		
 		return rs;
 	}
 
 	public void ExecuteQuery(String Message) throws SQLException
 	{
 		LogOutput(Message);
 	//	int j = 0;
     //    for (int i=0;i<5;i++)
         //{
       //   j=   Message.indexOf(",",j+1);           
        // }
        // String test = Message.substring(j+1, 1);
        // if (Message.substring(j+1,1)!=",")
       //  {
 				
 		Connection con = DriverManager.getConnection(url, user, password);
 		
 		PreparedStatement pst = null;
 		 pst = con.prepareStatement(Message);
             pst.executeUpdate();
 		
      //   }
 	}	
 	
 	
 	public String Insert_Trader(String Ticker, double Qty,double Px, String Side,String Ccy) throws SQLException
 	{
 
 
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		Date date = new Date();
 		
 				
 		//Check if position Exists already
 		String _resp = LoadData_str("Select distinct Ticker from currentholdings  where Ticker = '"+Ticker+"'");
 		//
 		if (_resp.equals(Ticker))
 		{
 			//Position exits
 			if (Side.equals("S"))
 			{
 				//Sell position
 				ExecuteQuery("Insert into holdingshistory values ('"+Ticker+"','"+Qty+"','"+Px+"','"+dateFormat.format(date)+"','"+Side+"')");
 				
 				
 				String _quantity = LoadData_str("Select sum(Quantity) from currentholdings where Ticker = '"+Ticker+"'");
 				Double q1 = Double.valueOf(_quantity);
 				Double q2 = Double.valueOf(Qty);
 				
 				if (q1.equals(q2))
 				{
 					//Liquidating position
 					ExecuteQuery("Delete from currentholdings where Ticker = '"+Ticker+"' and Quantity = '"+Qty+"'");
 					
 					return "Position updated";
 					
 				}
 				else if (Double.valueOf(_quantity).equals(Double.valueOf(Qty)))
 				{
 					Double _Qty = Double.valueOf(_quantity) - Double.valueOf(Qty);
 					//Sold part of position
 					ExecuteQuery("update currentholdings set Quantity='"+_Qty+"' where Ticker='"+Ticker+"'");
 					return "Position updated";
 					
 				}
 				else
 					return "Cannot have a net short position";
 				
 			}
 			else
 			{
 				//Add to position
 				ExecuteQuery("Insert into holdingshistory values ('"+Ticker+"','"+Qty+"','"+Px+"','"+dateFormat.format(date)+"','"+Side+"')");
 				
 				
 			}
 		}
 		else
 		{
 			if (Side.equals("S"))
 			{
 				//Short sell
 				
 				return "Error - Short Sale not allowed";
 			}
 			else
 			{
 				//New BUY Position
 				ExecuteQuery("Insert into holdingshistory values ('"+Ticker+"',"+Qty+","+Px+",'"+dateFormat.format(date)+"','"+Side+"')");
 				ExecuteQuery("Insert into currentholdings values ('"+Ticker+"','"+Qty+"','"+Px+"','"+Ccy+"')");
 				
 				
 			}
 			
 		}
 		return "0";
 			
 			
 		
 		
 	}
 	
 	
 	public void LogOutput(String Message) {
 
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		DateFormat dateFormat_log = new SimpleDateFormat("yyyy.MM.dd");
 		Date date = new Date();
 		System.out.println(dateFormat.format(date) + " : " + Message);
 		// System.out.printf("%D %R : ",date + Message);
 		/*
 		 * try {
 		 * 
 		 * 
 		 * 
 		 * File file = new
 		 * File("/home/pi/logs/"+dateFormat_log.format(date)+".PiFinance.log.txt"
 		 * ); //File file = new
 		 * File("c:\\"+dateFormat_log.format(date)+".PiFinance.log.txt"); // if
 		 * file doesnt exists, then create it if (!file.exists()) {
 		 * file.createNewFile(); }
 		 * 
 		 * FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
 		 * BufferedWriter bw = new BufferedWriter(fw);
 		 * bw.write(dateFormat.format(date)+" : "+Message+"\n"); bw.close();
 		 * 
 		 * 
 		 * 
 		 * } catch (IOException e) { System.out.println(e.toString()); }
 		 */
 
 	}
 	
 	public String WatchListTable() throws SQLException {
 
 		// Columns
 		LinkedList l_cols = new LinkedList();
 		JSONObject obj = new JSONObject();
 		JSONObject obj_cols_1 = new JSONObject();
 		JSONObject obj_cols_2 = new JSONObject();
 		JSONObject obj_cols_3 = new JSONObject();
 		JSONObject obj_cols_4 = new JSONObject();
 		obj_cols_1.put("id", "");
 		obj_cols_1.put("label", "Symbol");
 		obj_cols_1.put("type", "string");
 
 		obj_cols_2.put("id", "");
 		obj_cols_2.put("label", "Category");
 		obj_cols_2.put("type", "string");
 
 		obj_cols_3.put("id", "");
 		obj_cols_3.put("label", "Price");
 		obj_cols_3.put("type", "number");
 
 		
 
 		l_cols.add(obj_cols_1);
 		l_cols.add(obj_cols_2);
 		l_cols.add(obj_cols_3);
 		
 		obj.put("cols", l_cols);
 		// rows
 
 		rs = LoadData("Select distinct Symbol from  AlertKeyLevels");
 		ArrayList<String> l_Tickers = new ArrayList<String>();
 		ArrayList<String> l_Resistance = new ArrayList<String>();
 		ArrayList<String> l_Value = new ArrayList<String>();
 		LinkedList l_final = new LinkedList();
 		while (rs.next()) {
 			l_Tickers.add(rs.getString(1));
 		}
 
 		String Qty;
 		String AvgPx;
 		String LastPx;
 		String UPnLvLast;
 		String Pcnt;
 		String Date;
 		String Ccy;
 		for (String name : l_Tickers) {
 			
 			rs = LoadData("Select Category,value from AlertKeyLevels where Symbol = '"+name+"'");
 					while (rs.next()) {
 						l_Resistance.add(rs.getString(1));
 						l_Value.add(rs.getString(2));
 					}
 
 			
 			
 			for(int i=0;i<l_Resistance.size();i++)
 			{
 				LinkedList l1_rows = new LinkedList();
 			JSONObject obj_row1 = new JSONObject();
 			JSONObject obj_row2 = new JSONObject();
 			JSONObject obj_row3 = new JSONObject();
 			
 			obj_row1.put("v", name);
 			obj_row1.put("f", null);
 			obj_row2.put("v", l_Resistance.get(i));
 			obj_row2.put("f", null);
 			obj_row3.put("v", l_Value.get(i));
 			obj_row3.put("f", null);
 			
 			l1_rows.add(obj_row1);
 			l1_rows.add(obj_row2);
 			l1_rows.add(obj_row3);
 			
 			LinkedHashMap m1 = new LinkedHashMap();
 
 			m1.put("c", l1_rows);
 			l_final.add(m1);
 			
 			}
 			l_Resistance.clear();
 			l_Value.clear();
 		}
 		obj.put("rows", l_final);
 		System.out.println(obj);
 
 		return obj.toJSONString();
 	}
 
 	
 	
 
 public String Vol_Chart(String Ticker) throws SQLException, java.text.ParseException
 {
 	
 	List<String> _ivol = new ArrayList<String>();
 	List<String> _time = new ArrayList<String>();
 	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	Date date = new Date();
 	String ADV ="";
 //	rs = LoadData("Select ivol,time from volume where Ticker='"+Ticker+"' and date = '"+dateFormat.format(date)+"' and time > '12:00:00'");
 	try
 	{
 		
 	
 	 ADV = LoadData_str("Select adv from volume where Ticker='"+Ticker+"' and date = '"+dateFormat.format(date)+"' limit 1");
 //		ADV = LoadData_str("Select adv from volume where Ticker='"+Ticker+"' and date = '2013-08-27' limit 1");
 	 
 	 String _Region = _TimeZoneMap.get(Ticker);
 	 
 	 if (_Region.equals("EU"))
 	 {
 	 rs = LoadData("Select ivol,time from volume where Ticker='"+Ticker+"' and date = '"+dateFormat.format(date)+"' and time < '20:10:00'");
 //		 rs = LoadData("Select ivol,time from volume where Ticker='"+Ticker+"' and date = '2013-08-27' and time < '20:10:00'");
 	 
 	 }
 	 else
 	 {
 	rs = LoadData("Select ivol,time from volume where Ticker='"+Ticker+"' and date = '"+dateFormat.format(date)+"' and time > '11:00:00' and time < '20:10:00'");	 
 		 
 	 }
 	}
 	catch (Exception e)
 	{
 		return "ND";
 		
 	}
 	
 	
 	Map<String, String> _rs = new HashMap<String, String>();
 	LinkedList l_cols = new LinkedList();
 	JSONObject obj = new JSONObject();
 	while (rs.next()) {
 		
 		_rs.put(rs.getString(1),rs.getString(2));
 		 _ivol.add(rs.getString(1));
 		_time.add(rs.getString(2));
 	}
 	if (_ivol.size() > 0)
 	{
 	JSONObject obj_cols_1 = new JSONObject();
 	JSONObject obj_cols_2 = new JSONObject();
 	obj_cols_2.put("id", "");
 	obj_cols_2.put("label", "Volume");
 	obj_cols_2.put("type", "number");
 	
 
 	
 
 	obj_cols_1.put("id", "");
 	obj_cols_1.put("label", "Time");
 	obj_cols_1.put("type", "string");
 
 	l_cols.add(0, obj_cols_1);
 	l_cols.add(obj_cols_2);
 	JSONObject obj_cols_3 = new JSONObject();
 
 	obj_cols_3.put("id", "");
 	obj_cols_3.put("label", "Vwap");
 	obj_cols_3.put("type", "number");
 
 	l_cols.add(obj_cols_3);
 	
 
 	obj.put("cols", l_cols);
 		
 	LinkedList l_final = new LinkedList();
 	String _ivol_latest = "0";
 	for (int i=0;i<_time.size();i++) 
 	{
 
 		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
 		Date date_c = new Date(formatter.parse(_time.get(i)).getTime());
 		TimeZone tz = TimeZone.getTimeZone("UK");
 		boolean inDs = tz.inDaylightTime(new Date());
 		tz.getDefault().inDaylightTime( new Date() );
 		
 		Date date_plusone;
 		 if (!inDs)
 		 {
 		 date_plusone = new Date(formatter.parse(_time.get(i)).getTime()+3600000); 
 			 
 		 }
 		 else
 		 {
 		  date_plusone = date_c;
 		 }
 		 
 		 
 		JSONObject obj3 = new JSONObject();
 		JSONObject obj4 = new JSONObject();
 		JSONObject obj5 = new JSONObject();
 		JSONObject obj_col = new JSONObject();
 
 		
 		double _min  = Double.valueOf(formatter.format(date_c).substring(formatter.format(date_c).length()-4,formatter.format(date_c).length()-3));
 		String _ftime="";
 		String _ftime_plusone="";
 		if (!(_min==0) && !(_min==5))
 		{
 			if (_min<5)
 			{
 		//		System.out.println(formatter.format(date_c));
 		//		System.out.println(formatter.format(date_c).substring(0,formatter.format(date_c).length()-4));
 				
 				_ftime = formatter.format(date_c).substring(0,formatter.format(date_c).length()-4)+"0:00";
 				_ftime_plusone = formatter.format(date_plusone).substring(0,formatter.format(date_plusone).length()-4)+"0:00";
 			}
 			else
 			{
 				_ftime = formatter.format(date_c).substring(0,formatter.format(date_c).length()-4)+"5:00";
 				_ftime_plusone = formatter.format(date_plusone).substring(0,formatter.format(date_plusone).length()-4)+"5:00";
 			}
 			
 		}
 		else
 		{
 			_ftime = formatter.format(date_c).substring(0,formatter.format(date_c).length()-2)+"00";
 			_ftime_plusone = formatter.format(date_plusone).substring(0,formatter.format(date_plusone).length()-2)+"00";
 		}
 		System.out.println(_ftime);
 			
 		obj3.put("v", _ftime_plusone);
 		obj3.put("f", null);
 		obj4.put("v", _ivol.get(i));
 		obj4.put("f", null);
 	
 //		
 		try
 		{
 		System.out.println(_mapsTickers.get(Ticker).get(_ftime.substring(0,5)));
 		obj5.put("v", _mapsTickers.get(Ticker).get(_ftime.substring(0,5)));
 		}
 		catch ( Exception e)
 		{
 			
 			obj5.put("v",0);
 			
 		}
 		
 		obj5.put("f", null);
 		
 		LinkedList l1 = new LinkedList();
 		LinkedHashMap m1 = new LinkedHashMap();
 		l1.add(obj3);
 		l1.add(obj4);
 		l1.add(obj5);
 		m1.put("c", l1);
 
 		l_final.add(m1);
 	}
 	obj.put("rows", l_final);
 	_ivol_latest = _ivol.get(_ivol.size()-1);
 	
 	System.out.println("Vol Chart: " + obj.toJSONString());
 
 	
 	  double d = Double.parseDouble(_ivol_latest); 
 	   
 	  NumberFormat formatter = new DecimalFormat("###.#####");  
 	     
 	  _ivol_latest = formatter.format(d);  
 
 	  if (d > 1000000)
 	  {
 		  d=d/1000000;
 		  _ivol_latest = formatter.format(d)+"M";  
 	  }
 	  else
 	  {
 		  _ivol_latest = formatter.format(d);  
 		  
 	  }
 	return Ticker+";"+ADV+";"+_ivol_latest+";"+obj.toJSONString();
 	}
 	else
 		return "ND";
 	}
 	
 	private void getTimeZones()
 	{
 		
 		 try {
 			rs = LoadData("Select Ticker,Country from interestlist");
 		
 		 while(rs.next())
 		 {
 			 _TimeZoneMap.put(rs.getString(1), rs.getString(2));
 			 
 		 }
 		 } catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		
 	}
 	
 	private void getAdvCurve() throws SQLException
 	{
 		List<String> Tickers = new ArrayList<String>();
 		rs = LoadData("Select Ticker from interestlist where Volume='Y'");	 
 	//	rs = LoadData("Select Ticker from interestlist where Ticker = 'LLOY'");	 
 		while (rs.next()) {
 			
 			Tickers.add(rs.getString(1));
 			
 		}
 		for (String name : Tickers)
 		{
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.HOUR_OF_DAY,7);
 		cal.set(Calendar.MINUTE,0);
 		cal.set(Calendar.SECOND,0);
 		cal.set(Calendar.MILLISECOND,0);
 		Date date = cal.getTime();
 		
 		Calendar calf = Calendar.getInstance();
 		calf.set(Calendar.HOUR_OF_DAY,21);
 		calf.set(Calendar.MINUTE,0);
 		calf.set(Calendar.SECOND,0);
 		calf.set(Calendar.MILLISECOND,0);
 		Date datef = calf.getTime();
 	     SimpleDateFormat ft =  new SimpleDateFormat ("HH:mm:ss");
 	
 	//	long t=date.getTime();
 		Date _datef=new Date(date.getTime() + (5 * 60000));
 		System.out.println(ft.format(_datef));
 			
 	    Map<String, String> _mapadv = new HashMap<String, String>();		
 	while (date.before(datef))
 	{
 				
 	String vol = LoadData_str("select avg(ivol) from volume where Ticker = '"+name+"' and time >= '"+ft.format(date)+"' and time < '"+ft.format(_datef)+"'");
 		System.out.println(vol);
 	 _mapadv.put(ft.format(date).substring(0, 5), vol);
 		
  		date = _datef;
 		
 		 _datef=new Date(_datef.getTime() + (5 * 60000));
 			
 	}
 	
 		_mapsTickers.put(name,_mapadv);
 		
 		}
 	}
 	
 	private void getInterestList() throws SQLException
 	{
 		rs = LoadData("Select distinct Ticker from interestlist where Volume='Y'");	 
 		while (rs.next()) 
 			{
 			_TickersOfInterest.add(rs.getString(1));
 			
 			}	
 		
 		
 		
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	public String getFavourites() throws SQLException
 	{
 	
 		
 		String _return ="";
 		for (String Ticker : _TickersOfInterest)
 		{
 			_return +=Ticker+",";
 			
 		}
 	
 		return _return;
 	}
 	
 }
